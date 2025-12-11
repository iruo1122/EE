import java.util.InputMismatchException;  // 用于处理输入类型不匹配的异常
import java.util.Scanner;                 // 用于接收用户输入

public class SimpleCalculator {
    public static int add(int a,int b){return a+b;}
    public static int subtract(int a,int b){return a-b;}
    public static int multiply(int a,int b){return a*b;}
    public static int divide(int a,int b){
        if(b == 0){  // 检查除数是否为0
            // 如果除数为0，抛出异常，防止程序崩溃
            throw new IllegalArgumentException("除数不能为0！");
        }
        return a/b;  // 计算并返回a/b的结果
    }

    // 获取有效整数输入的方法
    private static int getValidInt(Scanner scanner, String prompt) {
        // 使用无限循环，直到用户输入正确的整数
        while (true) {
            System.out.print(prompt);  // 显示提示信息
            try {
                // 尝试读取一个整数
                return scanner.nextInt();
            } catch (InputMismatchException e) {
                // 如果输入的不是整数，捕获异常
                System.out.println("非法输入，请输入数字！");
                scanner.next();  // 清空扫描器缓冲区中的错误输入，防止死循环
            }
        }
    }

    public static void main(String[] args) {
        // 使用try-with-resources语句自动管理Scanner资源
        // 这样可以确保Scanner在使用完毕后被正确关闭
        try (Scanner scanner = new Scanner(System.in)) {
            // 主循环：让计算器可以重复使用
            while (true) {
                System.out.println("===== 简易计算器 =====");
                System.out.println("1.加法 " + "2.减法 " + "3.乘法 "+"4.除法 "+"5.退出");
                int choice = getValidInt(scanner,"请选择运算类型：");

                // 验证选择是否在有效范围内（1-5）
                if (choice < 1 || choice > 5) {
                    System.out.println("请输入1-5之间的数字！");
                    continue;  // 跳过本次循环剩余部分，重新开始
                }

                // 如果选择5，退出程序
                if (choice == 5) {
                    System.out.println("感谢使用，再见");
                    break;  // 跳出while循环，结束程序
                }
                int num1 = getValidInt(scanner, "请输入第一个数字：");
                int num2 = getValidInt(scanner, "请输入第二个数字：");
                switch (choice) {
                    case 1:  // 加法
                        System.out.println(num1 + "+" + num2 + "=" + add(num1, num2);  // 显示结果
                        break;  // 跳出switch语句

                    case 2:  // 减法
                        System.out.println(num1 + "-" + num2 + "=" + subtract(num1, num2));  // 显示结果
                        break;

                    case 3:  // 乘法
                        System.out.println(num1 + "*" + num2 + "=" + multiply(num1,num2));  // 显示结果
                        break;

                    case 4:  // 除法
                        try {
                            System.out.println(num1 + "/" + num2 + "=" + divide(num1, num2));  // 显示结果
                        } catch (IllegalArgumentException e) {
                            // 捕获除数为0的异常
                            System.out.println("错误：" + e.getMessage());  // 显示错误信息
                        }
                        break;

                    default:  // 理论上不会执行到这里，因为前面已经验证了choice的范围
                        System.out.println("程序错误：无效的选择");
                        break;
                }
            }
        }
        // try-with-resources会自动调用scanner.close()，无需手动关闭
    }
}