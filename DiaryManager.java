// 导入Java工具包，包含集合、日期等工具类
import java.util.*;
// 导入Java输入输出包，用于文件操作
import java.io.*;
// 导入Java时间包，用于处理日期和时间
import java.time.*;
// 导入日期时间格式化类，用于格式化日期时间字符串
import java.time.format.DateTimeFormatter;

// 定义日记管理类
public class DiaryManager {
    // 定义常量：日记文件存储的目录名（当前目录下的diaries文件夹）
    private static final String DIARY_DIR = "diaries";
    // 定义日期格式化器，将日期格式化为"年-月-日"的形式
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");
    // 定义时间格式化器，将时间格式化为"时:分:秒"的形式
    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm:ss");

    /**
     * 初始化日记目录
     * 检查日记文件夹是否存在，不存在则创建
     */
    public static void initDiaryDirectory() {
        // 创建File对象，表示日记目录
        File diary = new File(DIARY_DIR);
        if (!diary.exists()) {
            // 尝试创建目录
            if (diary.mkdir()) {
                System.out.println("日记文件夹创建成功：" + DIARY_DIR);
            } else {
                // 创建失败，输出提示信息
                System.out.println("无法创建日记文件夹");
            }
        }
    }
    public static void showMenu() {
        System.out.println("===== 个人日记管理系统 =====");
        System.out.println("1. 写新日记");
        System.out.println("2. 查看所有日记");
        System.out.println("3. 按日期查看日记");
        System.out.println("4. 搜索日记");
        System.out.println("5. 删除日记");
        System.out.println("6. 退出");
        System.out.print("请选择操作：");
    }
    private static void writeNewDiary(Scanner scanner) {
        System.out.println("\n=== 写新日记 ===");  // 显示功能标题，\n表示换行

        // 获取当前的日期和时间
        LocalDateTime now = LocalDateTime.now();
        // 将当前日期格式化为字符串（按照"yyyy-MM-dd"格式）
        String dateString = now.format(DATE_FORMATTER);
        // 将当前时间格式化为字符串（按照"HH:mm:ss"格式）
        String timeString = now.format(TIME_FORMATTER);
        // 生成日记文件名，格式为：diaries/日记_年-月-日.txt
        String fileName = DIARY_DIR + "/日记_" + dateString + ".txt";
        // 创建File对象，表示日记文件
        File diaryFile = new File(fileName);

        // 检查今天是否已经写过日记（文件是否存在）
        if (diaryFile.exists()) {
            System.out.println("今天已经写过日记了，是否覆盖？(Y/N):");
            // 获取用户输入，并转换为小写（方便比较）
            String answer = scanner.nextLine().toLowerCase();
            if (!answer.equals("y") && !answer.equals("是")) {
                System.out.println("取消写日记");
                return;  // 退出方法，返回到主菜单
            }
        }

        System.out.println("空格换行，END结束，回车完成：");

        // 使用StringBuilder来高效拼接日记内容（比String直接拼接更节省内存）
        StringBuilder content = new StringBuilder();
        String line;  // 用于存储每一行的输入

        // 循环读取用户的每一行输入
        while (true) {
            line = scanner.nextLine();  // 读取一行输入
            if (line.equals("END")) {   // 如果输入END，结束输入
                break;
            }
            // 将输入的内容追加到StringBuilder中，并添加换行符
            content.append(line).append("\n");
        }

        // 将日记内容保存到文件
        // 使用try-with-resources语句，自动关闭FileWriter，避免资源泄漏
        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write("日期：" + dateString + " 时间：" + timeString + "\n");
            writer.write("-".repeat(30) + "\n");
            // 写入用户输入的日记内容
            writer.write(content.toString());
            System.out.println("日记保存成功！文件名：" + fileName);
        } catch (IOException e) {
            // 捕获并处理IO异常
            System.out.println("保存失败：" + e.getMessage());
        }
    }
    private static void listAllDiaries() {
        System.out.println("\n--- 所有日记列表 ---");

        // 创建日记目录的File对象
        File diary = new File(DIARY_DIR);
        // 使用过滤器列出所有以"日记_"开头、以".txt"结尾的文件
        File[] diaryFiles = diary.listFiles((d, name) ->
                name.startsWith("日记_") && name.endsWith(".txt"));

        // 检查是否有日记文件
        if (diaryFiles == null || diaryFiles.length == 0) {
            System.out.println("暂无日记");
            return;  // 没有日记，直接返回
        }

        // 按日期排序（最近的在前），通过比较文件名实现
        Arrays.sort(diaryFiles, (f1, f2) ->
                f2.getName().compareTo(f1.getName()));

        // 输出表头
        System.out.println("序号\t日期\t\t\t文件名");
        System.out.println("-".repeat(50));  // 输出50个减号作为分隔线

        // 遍历所有日记文件并显示
        for (int i = 0; i < diaryFiles.length; i++) {
            String name = diaryFiles[i].getName();
            // 从文件名中提取日期：去掉前面的"日记_"和后面的".txt"
            String date = name.substring(3, name.length() - 4);
            // 格式化输出：序号左对齐占4位，日期左对齐占12位
            System.out.printf("%-4d\t%-12s\t%s%n", i + 1, date, name);
        }
    }
    private static void viewDiaryByDate(Scanner scanner) {
        System.out.println("\n--- 按日期查看日记 ---");
        System.out.println("请输入日期（格式：2006-11-22）:");
        // 获取用户输入的日期，并去除首尾空格
        String dateString = scanner.nextLine().trim();

        // 构建完整的文件路径
        String fileName = DIARY_DIR + "/日记_" + dateString + ".txt";
        File diaryFile = new File(fileName);

        // 检查文件是否存在
        if (!diaryFile.exists()) {
            System.out.println("这一天没有日记");
            return;  // 文件不存在，直接返回
        }

        // 输出日记标题
        System.out.println("\n" + "=".repeat(50));  // 输出50个等号
        System.out.println("日期：" + dateString);
        System.out.println("=".repeat(50));

        // 读取并显示日记内容
        try (BufferedReader reader = new BufferedReader(new FileReader(diaryFile))) {
            String line;
            // 逐行读取文件内容
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            // 捕获并处理IO异常
            System.out.println("读取失败：" + e.getMessage());
        }
    }
    private static void searchDiaries(Scanner scanner) {
        System.out.println("\n--- 搜索日记 ---");
        System.out.println("请输入要搜索的关键词：");
        // 获取关键词，转换为小写（实现不区分大小写搜索）
        String keyword = scanner.nextLine().trim().toLowerCase();

        // 检查关键词是否为空
        if (keyword.isEmpty()) {
            System.out.println("关键词不能为空！");
            return;
        }

        // 获取日记目录下的所有日记文件
        File diary = new File(DIARY_DIR);
        File[] diaryFiles = diary.listFiles((d, name) ->
                name.startsWith("日记_") && name.endsWith(".txt"));

        // 检查是否有日记文件
        if (diaryFiles == null || diaryFiles.length == 0) {
            System.out.println("暂无日记");
            return;
        }

        // 创建列表存储找到的日记日期
        List<String> foundDiaries = new ArrayList<>();

        // 遍历所有日记文件
        for (File file : diaryFiles) {
            // 使用try-with-resources自动关闭BufferedReader
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                boolean found = false;  // 标记是否找到关键词
                String line;

                // 逐行读取文件内容
                while ((line = reader.readLine()) != null) {
                    // 检查当前行是否包含关键词（不区分大小写）
                    if (line.toLowerCase().contains(keyword)) {
                        found = true;  // 找到关键词
                        break;  // 跳出循环
                    }
                }

                // 如果找到关键词，将日期添加到列表中
                if (found) {
                    // 从文件名中提取日期
                    String date = file.getName().substring(3, file.getName().length() - 4);
                    foundDiaries.add(date);
                }
            } catch (IOException e) {
                // 捕获并处理读取文件的异常
                System.out.println("读取文件失败：" + file.getName());
            }
        }

        // 输出搜索结果
        if (foundDiaries.isEmpty()) {
            System.out.println("未找到包含关键词 '" + keyword + "' 的日记");
        } else {
            System.out.println("找到 " + foundDiaries.size() + " 篇包含关键词的日记：");
            // 遍历并输出所有找到的日记日期
            for (String date : foundDiaries) {
                System.out.println(" • " + date);
            }
        }
    }
    private static void deleteDiary(Scanner scanner) {
        System.out.println("\n--- 删除日记 ---");
        System.out.println("请输入要删除的日记日期（格式：2006-11-22）:");
        // 获取用户输入的日期
        String dateString = scanner.nextLine().trim();

        // 构建完整的文件路径
        String fileName = DIARY_DIR + "/日记_" + dateString + ".txt";
        File diaryFile = new File(fileName);

        // 检查文件是否存在
        if (!diaryFile.exists()) {
            System.out.println("这一天没有日记");
            return;
        }

        // 确认是否删除
        System.out.println("确定要删除 " + dateString + " 的日记吗？（Y/N）:");
        String confirm = scanner.nextLine().toLowerCase();

        // 根据用户选择执行操作
        if (confirm.equals("y") || confirm.equals("是")) {
            // 删除文件
            if (diaryFile.delete()) {
                System.out.println("日记删除成功");
            } else {
                System.out.println("删除失败");
            }
        } else {
            System.out.println("取消删除");
        }
    }

    public static void main(String[] args) {
        // 创建Scanner对象，用于接收用户输入
        Scanner scanner = new Scanner(System.in);
        // 初始化日记目录
        initDiaryDirectory();
        // 主循环，让程序持续运行直到用户选择退出
        while (true) {
            showMenu();
            try {
                // 读取用户输入的选择，并转换为整数
                int choice = Integer.parseInt(scanner.nextLine());
                switch (choice) {
                    case 1:
                        writeNewDiary(scanner);  // 写新日记
                        break;
                    case 2:
                        listAllDiaries();  // 查看所有日记
                        break;
                    case 3:
                        viewDiaryByDate(scanner);  // 按日期查看
                        break;
                    case 4:
                        searchDiaries(scanner);  // 搜索日记
                        break;
                    case 5:
                        deleteDiary(scanner);  // 删除日记
                        break;
                    case 6:
                        System.out.println("\n感谢使用，再见！");
                        scanner.close();
                        return;
                    default:
                        System.out.println("请输入1~6之间的数字");
                }
            } catch (NumberFormatException e) {
                // 捕获数字格式异常
                System.out.println("请输入有效的数字！");
            }
        }
    }
}