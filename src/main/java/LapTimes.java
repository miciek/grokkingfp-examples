import java.util.ArrayList;
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

    static double totalTimePure(List<Double> lapTimes) {
        List<Double> withoutWarmUp =
                new ArrayList<>(lapTimes);
        withoutWarmUp.remove(0); // remove warm-up lap
        double sum = 0;
        for (double x : withoutWarmUp) {
            sum += x;
        }
        return sum;
    }

    static double avgTimePure(List<Double> lapTimes) {
        double time = totalTimePure(lapTimes);
        List<Double> withoutWarmUp =
                new ArrayList<>(lapTimes);
        withoutWarmUp.remove(0); // remove warm-up lap
        int laps = withoutWarmUp.size();
        return time / laps;
    }

    public static void main(String[] args) {
        ArrayList<Double> lapTimes = new ArrayList<>();
        lapTimes.add(31.0);
        lapTimes.add(20.9);
        lapTimes.add(21.1);
        lapTimes.add(21.3);

        System.out.printf("Avg: %.1fs\n", avgTime(lapTimes));

        ArrayList<Double> lapTimes2 = new ArrayList<>();
        lapTimes2.add(31.0); // warm-up lap
        lapTimes2.add(20.9);
        lapTimes2.add(21.1);
        lapTimes2.add(21.3);

        System.out.printf("Total: %.1fs\n", totalTimePure(lapTimes2));
        System.out.printf("Avg: %.1fs", avgTimePure(lapTimes2));
    }
}
