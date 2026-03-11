package test;

public class Test {
    public static void main(String[] args) {
        String a = "A";
        boolean b = true;

        switch (a)
        {
            case "A":
                if (b)
                {
                    System.out.println("IN1");
                    break;
                }
                System.out.println("IN2");
                break;
            case "B":
                System.out.println("IN3");
                break;
            case "C":
                System.out.println("IN3");
                break;
        }
        switch (a)
        {
            case "A" -> {
                if (b)
                {
                    System.out.println("IN1");
                    break;
                }
                System.out.println("IN2");
            }
            case "B" -> {
                System.out.println("IN3");
            }
            case "C" -> {
                System.out.println("IN3");
            }
        }
    }
}
