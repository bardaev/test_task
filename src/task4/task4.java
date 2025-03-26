package task4;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Paths;
import java.util.ArrayList;

public class task4 {

    public static void main(String[] args) {

        try(BufferedReader reader = new BufferedReader(new FileReader(Paths.get(args[0]).toAbsolutePath().toString()))) {

            ArrayList<Integer> list = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                list.add(Integer.parseInt(line));
            }

            int[] arr = new int[list.size()];
            for (int i = 0; i < arr.length; i++) {
                arr[i] = list.get(i);
            }

            sort(arr);

            int m = arr[arr.length / 2];
            int sum = 0;
            for (int j : arr) {
                sum += Math.abs(m - j);
            }

            System.out.println(sum);

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static void sort(int[] arr) {
        for (int i = 0; i < arr.length - 1; i++) {
            int minNumber = i;
            for (int j = i + 1; j < arr.length; j++) {
                if (arr[j] < arr[minNumber]) {
                    minNumber = j;
                }
            }
            if (minNumber != i) {
                int tmp = arr[i];
                arr[i] = arr[minNumber];
                arr[minNumber] = tmp;
            }
        }
    }
}
