package task2;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Paths;
import java.util.ArrayList;

public class task2 {

    private static class Point {
        public double x;
        public double y;

        public Point(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }

    public static void main(String[] args) {
        double x1 = 0;
        double y1 = 0;
        double radius = 0;

        ArrayList<Point> points = new ArrayList<>();

        try(
                BufferedReader readerFile1 = new BufferedReader(new FileReader(Paths.get(args[0]).toAbsolutePath().toString()));
                BufferedReader readerFile2 = new BufferedReader(new FileReader(Paths.get(args[1]).toAbsolutePath().toString()));
                ) {

            String line;
            if ((line = readerFile1.readLine()) != null) {
                x1 = Double.parseDouble(line.split(" ")[0]);
                y1 = Double.parseDouble(line.split(" ")[1]);
            }
            if ((line = readerFile1.readLine()) != null) {
                radius = Integer.parseInt(line);
            }

            int count = 0;
            while (((line = readerFile2.readLine()) != null) && count++ < 100) {
                points.add(new Point(Double.parseDouble(line.split(" ")[0]), Double.parseDouble(line.split(" ")[1])));
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        for (Point point : points) {
            System.out.println(distance(x1, y1, point.x, point.y, radius));
        }
    }

    public static int distance(double x1, double y1, double x2, double y2, double r) {
        double distance = Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
        if (r == distance) {
            return 0;
        } else if (r > distance) {
            return 1;
        } else {
            return 2;
        }
    }
}
