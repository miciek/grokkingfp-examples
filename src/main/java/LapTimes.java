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
        int laps = lapTimes.size();
        if(laps <= 1) return 0; // just warm-up lap
        return totalTime(lapTimes) / laps;
    }

    public static void main(String[] args) {
        ArrayList<Double> lapTimes = new ArrayList<>();
        lapTimes.add(31.0); // warm-up lap
        lapTimes.add(20.9);
        lapTimes.add(21.1);
        lapTimes.add(20.3);

        System.out.println(totalTime(lapTimes));
        System.out.println(avgTime(lapTimes));
    }
}
