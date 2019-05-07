import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class LapTimes {
    static double totalTime(List<Double> lapTimes) {
        lapTimes.remove(0); // remove warm-up lap
        double sum = 0;
        for (double x : lapTimes) {
            sum += x;
        }
        return sum;
    }

    static double avgTime(List<Double> lapTimes) {
        double time = totalTime(lapTimes);
        int laps = lapTimes.size();
        return time / laps;
    }

    public static void main(String[] args) {
        ArrayList<Double> lapTimes = new ArrayList<>();
        lapTimes.add(31.0); // warm-up lap
        lapTimes.add(20.9);
        lapTimes.add(21.1);
        lapTimes.add(21.3);

        System.out.printf("Total: %.1fs\n", totalTime(lapTimes));
        System.out.printf("Avg: %.1fs", avgTime(lapTimes));
    }
}
