import java.sql.*;
import java.util.Scanner;
import java.util.InputMismatchException;

class Student {
    private String name;
    private String studentId;
    private int age;
    private int score;

    public Student(String name, String studentId, int age, int score) {
        this.name = name;
        this.studentId = studentId;
        this.age = age;
        this.score = score;
    }

    // Getter和Setter方法
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public void display() {
        System.out.println("姓名：" + name + "\t学号：" + studentId + "\t年龄：" + age + "\t成绩：" + score);
    }
}

public class Sims {
    // 数据库连接信息 - 请根据你的实际情况修改
    private static final String DB_URL = "jdbc:mysql://localhost:3306/sims";
    private static final String DB_USER = "root";  // 默认用户名
    private static final String DB_PASSWORD = "iruo1122";  // 修改为你的MySQL密码

    // 获取数据库连接
    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    // 初始化数据库表（如果不存在则创建）
    private static void initializeDatabase() {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS students (" +
                "id INT PRIMARY KEY AUTO_INCREMENT," +
                "student_id VARCHAR(20) NOT NULL UNIQUE," +
                "name VARCHAR(50) NOT NULL," +
                "age INT NOT NULL," +
                "score INT NOT NULL," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(createTableSQL);
            System.out.println("数据库初始化成功！");
        } catch (SQLException e) {
            System.out.println("数据库初始化失败：" + e.getMessage());
        }
    }

    // 添加学生到数据库
    private static void addStudentToDB(Student student) {
        String sql = "INSERT INTO students (student_id, name, age, score) VALUES (?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, student.getStudentId());
            pstmt.setString(2, student.getName());
            pstmt.setInt(3, student.getAge());
            pstmt.setInt(4, student.getScore());

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("学生信息已成功保存到数据库！");
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) { // 重复学号错误
                System.out.println("错误：学号 " + student.getStudentId() + " 已存在！");
            } else {
                System.out.println("数据库错误：" + e.getMessage());
            }
        }
    }

    // 从数据库读取所有学生
    private static void displayAllStudentsFromDB() {
        String sql = "SELECT student_id, name, age, score FROM students ORDER BY id";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                String studentId = rs.getString("student_id");
                String name = rs.getString("name");
                int age = rs.getInt("age");
                int score = rs.getInt("score");

                Student student = new Student(name, studentId, age, score);
                student.display();
            }

            if (!hasData) {
                System.out.println("数据库中暂无学生信息！");
            }
        } catch (SQLException e) {
            System.out.println("读取数据失败：" + e.getMessage());
        }
    }

    // 从数据库查找学生
    private static void searchStudentInDB(String searchId) {
        String sql = "SELECT student_id, name, age, score FROM students WHERE student_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, searchId);
            ResultSet rs = pstmt.executeQuery();

            boolean found = false;
            while (rs.next()) {
                found = true;
                String studentId = rs.getString("student_id");
                String name = rs.getString("name");
                int age = rs.getInt("age");
                int score = rs.getInt("score");

                Student student = new Student(name, studentId, age, score);
                student.display();
            }

            if (!found) {
                System.out.println("未找到学号为 " + searchId + " 的学生！");
            }

            rs.close();
        } catch (SQLException e) {
            System.out.println("查找失败：" + e.getMessage());
        }
    }

    // 从数据库统计信息
    private static void showStatisticsFromDB() {
        String countSQL = "SELECT COUNT(*) as total FROM students";
        String scoreSQL = "SELECT AVG(score) as avg_score, MAX(score) as max_score, MIN(score) as min_score FROM students";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // 获取学生总数
            ResultSet rs1 = stmt.executeQuery(countSQL);
            int total = 0;
            if (rs1.next()) {
                total = rs1.getInt("total");
            }
            rs1.close();

            if (total == 0) {
                System.out.println("数据库中暂无学生信息！");
                return;
            }

            // 获取统计信息
            ResultSet rs2 = stmt.executeQuery(scoreSQL);
            if (rs2.next()) {
                double avgScore = rs2.getDouble("avg_score");
                int maxScore = rs2.getInt("max_score");
                int minScore = rs2.getInt("min_score");

                System.out.println("学生总数：" + total);
                System.out.printf("平均分：%.2f\n", avgScore);
                System.out.println("最高分：" + maxScore);
                System.out.println("最低分：" + minScore);
            }
            rs2.close();

        } catch (SQLException e) {
            System.out.println("统计失败：" + e.getMessage());
        }
    }

    // 获取有效字符串输入
    private static String getValidString(Scanner scanner, String prompt) {
        System.out.print(prompt);
        return scanner.next();
    }

    // 获取有效整数输入
    private static int getValidInt(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return scanner.nextInt();
            } catch (InputMismatchException e) {
                System.out.println("非法输入，请输入数字！");
                scanner.next();
            }
        }
    }

    // 显示菜单
    public static void showMenu() {
        System.out.println("\n===== 学生信息管理系统 =====");
        System.out.println("1. 添加学生");
        System.out.println("2. 显示所有学生");
        System.out.println("3. 查找学生");
        System.out.println("4. 统计信息");
        System.out.println("5. 删除学生");
        System.out.println("6. 退出系统");
    }

    // 删除学生
    private static void deleteStudentFromDB(String studentId) {
        String sql = "DELETE FROM students WHERE student_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, studentId);
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("学号为 " + studentId + " 的学生信息已删除！");
            } else {
                System.out.println("未找到学号为 " + studentId + " 的学生！");
            }
        } catch (SQLException e) {
            System.out.println("删除失败：" + e.getMessage());
        }
    }

    public static void main(String[] args) {
        // 加载MySQL驱动
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("MySQL驱动加载成功！");
        } catch (ClassNotFoundException e) {
            System.out.println("找不到MySQL驱动！");
            System.out.println("请确保已添加mysql-connector-java.jar到项目中");
            return;
        }

        // 初始化数据库
        initializeDatabase();

        Scanner scanner = new Scanner(System.in);

        while (true) {
            showMenu();
            int choice = getValidInt(scanner, "请选择操作：");

            switch (choice) {
                case 1: // 添加学生
                    String name = getValidString(scanner, "请输入姓名：");
                    String studentId = getValidString(scanner, "请输入学号：");
                    int age = getValidInt(scanner, "请输入年龄：");
                    int score = getValidInt(scanner, "请输入成绩：");

                    Student student = new Student(name, studentId, age, score);
                    addStudentToDB(student);
                    break;

                case 2: // 显示所有学生
                    displayAllStudentsFromDB();
                    break;

                case 3: // 查找学生
                    String searchId = getValidString(scanner, "请输入要查找的学号：");
                    searchStudentInDB(searchId);
                    break;

                case 4: // 统计信息
                    showStatisticsFromDB();
                    break;

                case 5: // 删除学生
                    String deleteId = getValidString(scanner, "请输入要删除的学生学号：");
                    deleteStudentFromDB(deleteId);
                    break;

                case 6: // 退出系统
                    System.out.println("感谢使用，再见！");
                    scanner.close();
                    return;

                default:
                    System.out.println("输入错误，请重新输入！");
            }
        }
    }
}