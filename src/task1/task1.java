package task1;

public class task1 {
    public static void main(String[] args) {
        try {
            if (args.length != 2) {
                throw new IllegalArgumentException("Передано неверное количество аргументов");
            }
            int n = Integer.parseInt(args[0]);
            int m = Integer.parseInt(args[1]);
            circle(n, m);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static void circle(int n, int m) {
        int[] arr = new int[n];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = i + 1;
        }
        int curr = 0;
        do {
            System.out.print(arr[curr]);
            curr = (curr + m - 1) % arr.length;
        } while(curr != 0);
    }
}
