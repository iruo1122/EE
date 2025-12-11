// InputMismatchException用于处理输入类型不匹配异常
import java.util.InputMismatchException;
// Scanner用于获取用户输入
import java.util.Scanner;

class Student {
    // 私有属性，封装学生的基本信息
    private String name;    // 学生姓名
    private String id;      // 学号
    private int age;        // 年龄
    private int score;      // 成绩

    // 构造方法，用于创建Student对象时初始化属性
    public Student(String name, String id, int age, int score) {
        this.name = name;   // 将参数name赋值给当前对象的name属性
        this.id = id;       // 将参数id赋值给当前对象的id属性
        this.age = age;     // 将参数age赋值给当前对象的age属性
        this.score = score; // 将参数score赋值给当前对象的score属性
    }

    // 以下是getter和setter方法，用于访问和修改私有属性

    // 获取学号
    public String getId() {return id;}
    // 设置学号
    public void setId(String id) {this.id = id;}

    // 获取姓名
    public String getName() {return name;}
    // 设置姓名
    public void setName(String name) {this.name = name;}

    // 获取年龄
    public int getAge() {return age;}
    // 设置年龄
    public void setAge(int age) {this.age = age;}

    // 获取成绩
    public int getScore() {return score;}
    // 设置成绩
    public void setScore(int score) {this.score = score;}

    // 显示学生信息的方法
    public void display() {
        System.out.println("姓名：" + name + "\t学号：" + id + "\t年龄：" + age + "\t成绩：" + score);
    }
}

public class Sims {

    // 显示系统菜单的方法
    public static void showMenu() {
        System.out.println("===== 学生信息管理系统 =====");
        System.out.println("1.添加学生");
        System.out.println("2.显示所有学生");
        System.out.println("3.查找学生");
        System.out.println("4.统计信息");
        System.out.println("5.退出系统");
    }

    // 获取有效字符串输入的方法
    // 参数scanner：Scanner对象用于获取输入
    // 参数prompt：提示信息，告诉用户需要输入什么
    private static String getValidString(Scanner scanner, String prompt) {
        System.out.print(prompt);  // 显示提示信息
        return scanner.next();     // 获取用户输入的字符串
    }

    // 获取有效整数输入的方法
    // 使用循环确保用户必须输入正确的整数
    private static int getValidInt(Scanner scanner, String prompt) {
        while (true) {  // 无限循环，直到获取到有效的整数
            System.out.print(prompt);  // 显示提示信息
            try {
                return scanner.nextInt();  // 尝试获取整数输入
            } catch (InputMismatchException e) {
                // 如果输入的不是整数，捕获异常
                System.out.println("非法输入，请输入数字！");
                scanner.next();  // 清除错误的输入，避免死循环
            }
        }
    }

    // 主方法，程序的入口点
    public static void main(String[] args) {
        // 创建学生数组，最多存储100个学生对象
        Student[] students = new Student[100];
        int count = 0;  // 记录当前已存储的学生数量
        Scanner scanner = new Scanner(System.in);

        // 主循环，持续运行直到用户选择退出
        while (true) {
            showMenu();  // 显示菜单
            int choice = getValidInt(scanner, "请选择操作：");
            switch (choice) {
                case 1:  // 添加学生
                    if (count >= 100) {  // 检查数组是否已满
                        System.out.println("学生数量已达上限");
                        break;  // 跳出switch，返回主循环
                    }
                    // 获取学生信息
                    String name = getValidString(scanner, "请输入姓名：");
                    String id = getValidString(scanner, "请输入学号：");
                    int age = getValidInt(scanner, "请输入年龄：");
                    int score = getValidInt(scanner, "请输入成绩：");

                    // 创建新的Student对象并存储到数组中
                    students[count] = new Student(name, id, age, score);
                    count++;  // 学生数量加1
                    break;

                case 2:  // 显示所有学生
                    // 遍历数组中所有已存储的学生
                    for (int i = 0; i < count; i++) {
                        students[i].display();  // 调用display()方法显示学生信息
                    }
                    break;

                case 3:  // 查找学生
                    if (count == 0) {  // 检查是否有学生信息
                        System.out.println("暂无学生信息！");
                        break;
                    }

                    System.out.print("请输入要查找的学号：");
                    String searchId = scanner.next();  // 获取要查找的学号
                    boolean found = false;  // 标记是否找到学生

                    // 遍历学生数组查找匹配的学号
                    for (int i = 0; i < count; i++) {
                        if (students[i].getId().equals(searchId)) {  // 比较学号是否相等
                            students[i].display();  // 显示找到的学生信息
                            found = true;  // 标记为已找到
                        }
                    }

                    if (!found) {  // 如果循环结束仍未找到
                        System.out.println("未找到该学生！");
                    }
                    break;

                case 4:  // 统计信息
                    if (count == 0) {  // 检查是否有学生信息
                        System.out.println("暂无学生信息！");
                    } else {
                        // 初始化统计变量
                        int totalScore = 0;  // 总分
                        int maxScore = Integer.MIN_VALUE;  // 最高分，初始化为最小整数
                        int minScore = Integer.MAX_VALUE;  // 最低分，初始化为最大整数

                        // 遍历所有学生计算统计信息
                        for (int i = 0; i < count; i++) {
                            int studentScore = students[i].getScore();  // 获取当前学生成绩
                            totalScore += studentScore;  // 累加到总分

                            // 更新最高分
                            if (studentScore > maxScore) {
                                maxScore = studentScore;
                            }

                            // 更新最低分
                            if (studentScore < minScore) {
                                minScore = studentScore;
                            }
                        }

                        // 计算平均分
                        double average = (double) totalScore / count;

                        // 输出统计结果
                        System.out.println("学生总数：" + count);
                        System.out.println("平均分：" + average);
                        System.out.println("最高分：" + maxScore);
                        System.out.println("最低分" + minScore);
                    }
                    break;

                case 5:  // 退出系统
                    System.out.println("感谢使用，再见");
                    scanner.close();  // 关闭Scanner对象，释放资源
                    return;  // 结束main方法，退出程序

                default:  // 处理无效的菜单选择
                    System.out.println("输入错误，请重新输入！");
            }  // switch语句结束
        }  // while循环结束
    }  // main方法结束
}  // Sims类结束