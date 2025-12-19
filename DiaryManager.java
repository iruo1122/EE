import java.util.*;
import java.io.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.sql.*;

public class DiaryManager {
    private static final String DIARY_DIR = "diaries";
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String DB_URL = "jdbc:mysql://localhost:3306/diary?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "iruo1122";
    private static Connection connection = null;

    /**
     * 数据库操作工具类
     */
    private static class DatabaseUtil {
        static void init() {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                System.out.println("✅ 数据库连接成功！");
                createTable();
            } catch (Exception e) {
                System.out.println("⚠️ 数据库连接失败，将使用文件模式：" + e.getMessage());
            }
        }

        private static void createTable() throws SQLException {
            String sql = "CREATE TABLE IF NOT EXISTS diaries (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "diary_date DATE NOT NULL UNIQUE, " +
                    "content TEXT NOT NULL, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP)";
            try (Statement stmt = connection.createStatement()) {
                stmt.execute(sql);
            }
        }

        static void close() {
            if (connection != null) {
                try { connection.close(); System.out.println("✅ 数据库连接已关闭"); }
                catch (SQLException e) { System.out.println("❌ 关闭连接失败：" + e.getMessage()); }
            }
        }

        static boolean isAvailable() { return connection != null; }

        static void save(String date, String content) {
            if (!isAvailable()) return;
            try (PreparedStatement pstmt = connection.prepareStatement(
                    "INSERT INTO diaries (diary_date, content) VALUES (?, ?) ON DUPLICATE KEY UPDATE content = ?")) {
                pstmt.setDate(1, java.sql.Date.valueOf(date));
                pstmt.setString(2, content.trim());
                pstmt.setString(3, content.trim());
                pstmt.executeUpdate();
            } catch (SQLException e) {
                System.out.println("❌ 保存失败：" + e.getMessage());
            }
        }

        static String read(String date) {
            if (!isAvailable()) return null;
            try (PreparedStatement pstmt = connection.prepareStatement("SELECT content FROM diaries WHERE diary_date = ?")) {
                pstmt.setDate(1, java.sql.Date.valueOf(date));
                try (ResultSet rs = pstmt.executeQuery()) {
                    return rs.next() ? rs.getString("content") : null;
                }
            } catch (SQLException e) {
                System.out.println("❌ 读取失败：" + e.getMessage());
                return null;
            }
        }

        static boolean delete(String date) {
            if (!isAvailable()) return false;
            try (PreparedStatement pstmt = connection.prepareStatement("DELETE FROM diaries WHERE diary_date = ?")) {
                pstmt.setDate(1, java.sql.Date.valueOf(date));
                return pstmt.executeUpdate() > 0;
            } catch (SQLException e) {
                System.out.println("❌ 删除失败：" + e.getMessage());
                return false;
            }
        }

        static List<String> search(String keyword) {
            List<String> results = new ArrayList<>();
            if (!isAvailable()) return results;
            try (PreparedStatement pstmt = connection.prepareStatement("SELECT diary_date FROM diaries WHERE content LIKE ? ORDER BY diary_date DESC")) {
                pstmt.setString(1, "%" + keyword + "%");
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) results.add(rs.getDate("diary_date").toString());
                }
            } catch (SQLException e) {
                System.out.println("❌ 搜索失败：" + e.getMessage());
            }
            return results;
        }

        static List<String> getAllDates() {
            List<String> dates = new ArrayList<>();
            if (!isAvailable()) return dates;
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT diary_date FROM diaries ORDER BY diary_date DESC")) {
                while (rs.next()) dates.add(rs.getDate("diary_date").toString());
            } catch (SQLException e) {
                System.out.println("❌ 获取列表失败：" + e.getMessage());
            }
            return dates;
        }
    }

    /**
     * 文件操作工具类
     */
    private static class FileUtil {
        static void initDir() {
            File dir = new File(DIARY_DIR);
            if (!dir.exists() && dir.mkdir()) System.out.println("✅ 创建文件夹：" + DIARY_DIR);
        }

        static File[] getSortedFiles() {
            File[] files = new File(DIARY_DIR).listFiles((d, n) -> n.startsWith("日记_") && n.endsWith(".txt"));
            if (files == null || files.length == 0) return null;
            Arrays.sort(files, (f1, f2) ->
                    f2.getName().substring(3, f2.getName().length() - 4)
                            .compareTo(f1.getName().substring(3, f1.getName().length() - 4)));
            return files;
        }

        static boolean save(String date, String content, boolean withHeader) {
            String filename = DIARY_DIR + "/日记_" + date + ".txt";
            String fileContent = withHeader ? "📅 日期：" + date + "\n════════════════════════════════════════\n" + content : content;
            try (FileWriter writer = new FileWriter(filename)) {
                writer.write(fileContent);
                return true;
            } catch (IOException e) {
                System.out.println("❌ 保存文件失败：" + e.getMessage());
                return false;
            }
        }

        static String getDateFromFilename(String filename) {
            return filename.substring(3, filename.length() - 4);
        }

        static List<String> searchInFiles(String keyword) {
            List<String> results = new ArrayList<>();
            File[] files = getSortedFiles();
            if (files == null) return results;

            for (File file : files) {
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.toLowerCase().contains(keyword.toLowerCase())) {
                            results.add(getDateFromFilename(file.getName()));
                            break;
                        }
                    }
                } catch (IOException e) {
                    System.out.println("❌ 读取失败：" + file.getName());
                }
            }
            return results;
        }
    }

    /**
     * 界面和交互工具类
     */
    private static class UIUtil {
        static void showMenu() {
            System.out.println("\n════════════════════════════════════════");
            System.out.println("         📒 个人日记管理系统 📒");
            System.out.println("════════════════════════════════════════");
            System.out.println("当前模式: " + (DatabaseUtil.isAvailable() ? "✅ 数据库模式" : "📁 文件模式"));
            System.out.println("1. 📝 写新日记");
            System.out.println("2. 📋 查看所有日记");
            System.out.println("3. 🔍 搜索日记");
            System.out.println("4. ✏️ 修改日记");
            System.out.println("5. 🗑️ 删除日记");
            System.out.println("6. 🚪 退出系统");
            System.out.print("👉 请选择操作：");
        }

        static void showTitle(String title) {
            System.out.println("\n════════════════════════════════════════");
            System.out.println("            " + title);
            System.out.println("════════════════════════════════════════");
        }

        static String getDateInput(Scanner scanner) {
            System.out.println("\n📅 请选择日期：");
            System.out.println("1. 使用当前日期");
            System.out.println("2. 输入自定义日期");
            System.out.print("👉 请选择: ");

            try {
                if (Integer.parseInt(scanner.nextLine()) == 2) {
                    while (true) {
                        System.out.print("📅 请输入日期（格式: yyyy-MM-dd）: ");
                        String inputDate = scanner.nextLine().trim();
                        if (isValidDate(inputDate)) return inputDate;
                        System.out.println("❌ 日期格式不正确，请重新输入！");
                    }
                }
            } catch (NumberFormatException e) {}
            return LocalDate.now().format(DATE_FORMATTER);
        }

        static boolean isValidDate(String dateStr) {
            try { LocalDate.parse(dateStr, DATE_FORMATTER); return true; }
            catch (Exception e) { return false; }
        }

        static String getContentInput(Scanner scanner) {
            System.out.println("\n📝 请输入日记内容（空行表示结束）：");
            System.out.println("   （输入完毕后，请按两次回车完成输入）");
            System.out.println("════════════════════════════════════════");

            StringBuilder content = new StringBuilder();
            String line;
            while (!(line = scanner.nextLine()).trim().isEmpty()) {
                content.append(line).append("\n");
            }
            return content.toString().trim();
        }
    }

    /**
     * 主要功能方法
     */
    private static void writeDiary(Scanner scanner) {
        UIUtil.showTitle("📝 写新日记");
        String date = UIUtil.getDateInput(scanner);

        // 检查是否已存在
        boolean exists = new File(DIARY_DIR + "/日记_" + date + ".txt").exists() ||
                (DatabaseUtil.isAvailable() && DatabaseUtil.read(date) != null);

        if (exists) {
            System.out.print("⚠️ " + date + " 已有日记，是否覆盖？（Y/N）: ");
            if (!scanner.nextLine().toLowerCase().matches("y|是")) {
                System.out.println("❌ 取消写日记"); return;
            }
        }

        String content = UIUtil.getContentInput(scanner);
        if (content.isEmpty()) {
            System.out.println("❌ 内容不能为空"); return;
        }

        if (FileUtil.save(date, content, true))
            System.out.println("✅ 日记保存到文件成功！");
        if (DatabaseUtil.isAvailable()) {
            DatabaseUtil.save(date, content);
            System.out.println("✅ 日记已保存到数据库");
        }
    }

    private static void listDiaries() {
        UIUtil.showTitle("📋 所有日记列表");

        int total = 0;
        if (DatabaseUtil.isAvailable()) {
            List<String> dates = DatabaseUtil.getAllDates();
            if (!dates.isEmpty()) {
                System.out.println("📊 数据库中的日记（按日期倒序排列）:");
                System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                System.out.println("序号\t日期");
                System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                for (int i = 0; i < dates.size(); i++)
                    System.out.printf("%-4d\t%-12s%n", i + 1, dates.get(i));
                System.out.println("📈 共 " + dates.size() + " 篇日记");
                total += dates.size();
            }
        }

        File[] files = FileUtil.getSortedFiles();
        if (files != null && files.length > 0) {
            String title = DatabaseUtil.isAvailable() ? "\n📁 文件系统中的日记:" : "📁 所有日记:";
            System.out.println(title);
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            System.out.println("序号\t日期\t\t文件名");
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

            for (int i = 0; i < files.length; i++) {
                String name = files[i].getName();
                String date = FileUtil.getDateFromFilename(name);
                long size = files[i].length();
                String sizeStr = size > 1024 ? String.format("%.1fKB", size / 1024.0) : size + "B";
                System.out.printf("%-4d\t%-12s\t%s (%-6s)%n", i + 1, date, name, sizeStr);
            }
            System.out.println("📈 共 " + files.length + " 篇日记");
            total += DatabaseUtil.isAvailable() ? 0 : files.length;
        }

        if (total == 0) System.out.println("📭 暂无日记");
    }

    private static void searchDiaries(Scanner scanner) {
        UIUtil.showTitle("🔍 搜索日记");
        System.out.print("🔍 请输入要搜索的关键词：");
        String keyword = scanner.nextLine().trim().toLowerCase();

        if (keyword.isEmpty()) {
            System.out.println("❌ 关键词不能为空！"); return;
        }

        System.out.println("\n🔍 搜索结果：");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        int resultCount = 0;
        boolean found = false;

        if (DatabaseUtil.isAvailable()) {
            List<String> dbResults = DatabaseUtil.search(keyword);
            if (!dbResults.isEmpty()) {
                System.out.println("💾 数据库中找到 " + dbResults.size() + " 篇：");
                dbResults.forEach(date -> System.out.println("   • " + date));
                resultCount += dbResults.size();
                found = true;
            }
        }

        List<String> fileResults = FileUtil.searchInFiles(keyword);
        if (!fileResults.isEmpty()) {
            System.out.println((DatabaseUtil.isAvailable() ? "\n📁 文件系统中" : "📁") +
                    "找到 " + fileResults.size() + " 篇：");
            fileResults.forEach(date -> System.out.println("   • " + date));
            resultCount += fileResults.size();
            found = true;
        }

        System.out.println(found ? "📈 总计找到 " + resultCount + " 篇日记" :
                "❌ 未找到包含关键词 '" + keyword + "' 的日记");
    }

    private static void modifyDiary(Scanner scanner) {
        UIUtil.showTitle("✏️ 修改日记");
        String date = selectDiary(scanner, "修改");
        if (date == null) return;

        // 显示原内容
        System.out.println("\n📄 原日记内容：");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        File file = new File(DIARY_DIR + "/日记_" + date + ".txt");
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                reader.lines().limit(5).forEach(System.out::println);
            } catch (IOException e) { System.out.println("❌ 读取失败：" + e.getMessage()); }
        }
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        System.out.print("⚠️ 确定要修改吗？（Y/N）: ");
        if (!scanner.nextLine().toLowerCase().matches("y|是")) {
            System.out.println("❌ 取消修改"); return;
        }

        String newContent = UIUtil.getContentInput(scanner);
        if (newContent.isEmpty()) {
            System.out.println("❌ 内容不能为空"); return;
        }

        if (FileUtil.save(date, newContent, true))
            System.out.println("✅ 文件修改成功！");
        if (DatabaseUtil.isAvailable()) {
            DatabaseUtil.save(date, newContent);
            System.out.println("✅ 数据库修改成功！");
        }
    }

    private static void deleteDiary(Scanner scanner) {
        UIUtil.showTitle("🗑️ 删除日记");
        String date = selectDiary(scanner, "删除");
        if (date == null) return;

        File file = new File(DIARY_DIR + "/日记_" + date + ".txt");
        if (file.exists()) {
            System.out.println("\n⚠️ 要删除的日记摘要：");
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                reader.lines().limit(5).forEach(System.out::println);
            } catch (IOException e) { System.out.println("❌ 读取失败：" + e.getMessage()); }
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        }

        System.out.print("⚠️ 确定要删除 " + date + " 的日记吗？（Y/N）: ");
        if (!scanner.nextLine().toLowerCase().matches("y|是")) {
            System.out.println("❌ 取消删除"); return;
        }

        boolean deleted = false;
        if (file.exists() && file.delete()) {
            System.out.println("✅ 文件删除成功");
            deleted = true;
        }
        if (DatabaseUtil.isAvailable() && DatabaseUtil.delete(date)) {
            System.out.println("✅ 数据库记录删除成功");
            deleted = true;
        }
        System.out.println(deleted ? "✅ 删除完成" : "⚠️ 没有进行任何删除操作");
    }

    private static String selectDiary(Scanner scanner, String action) {
        File[] files = FileUtil.getSortedFiles();
        if (files == null || files.length == 0) {
            System.out.println("📭 没有日记可" + action); return null;
        }

        System.out.println("📅 最近的日记：");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━");
        for (int i = 0; i < Math.min(files.length, 5); i++) {
            String date = FileUtil.getDateFromFilename(files[i].getName());
            System.out.printf("%d. %s%n", i + 1, date);
        }
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.print("👉 请选择要" + action + "的日记序号（或输入日期 yyyy-MM-dd）: ");

        String input = scanner.nextLine().trim();
        try {
            int index = Integer.parseInt(input);
            if (index >= 1 && index <= files.length)
                return FileUtil.getDateFromFilename(files[index - 1].getName());
            System.out.println("❌ 序号超出范围"); return null;
        } catch (NumberFormatException e) {
            if (UIUtil.isValidDate(input)) return input;
            System.out.println("❌ 日期格式不正确！"); return null;
        }
    }

    public static void main(String[] args) {
        DatabaseUtil.init();
        FileUtil.initDir();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            UIUtil.showMenu();
            try {
                switch (Integer.parseInt(scanner.nextLine())) {
                    case 1 -> writeDiary(scanner);
                    case 2 -> listDiaries();
                    case 3 -> searchDiaries(scanner);
                    case 4 -> modifyDiary(scanner);
                    case 5 -> deleteDiary(scanner);
                    case 6 -> {
                        System.out.println("\n════════════════════════════════════════");
                        System.out.println("         🙏 感谢使用，再见！");
                        System.out.println("════════════════════════════════════════");
                        DatabaseUtil.close();
                        scanner.close();
                        return;
                    }
                    default -> System.out.println("❌ 请输入1~6之间的数字");
                }
            } catch (NumberFormatException e) {
                System.out.println("❌ 请输入有效的数字！");
            }
        }
    }
}