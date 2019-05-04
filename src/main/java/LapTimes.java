import java.util.Arrays;
import java.util.List;

public class LapTimes {
    static int totalTime(List<Integer> lapTimes) {
        lapTimes.remove(0); // warm-up lap
        int sum = 0;
        for (int x : lapTimes) {
            sum += x;
        }
        return sum;
    }

    static int avgTime(List<Integer> lapTimes) {
        int laps = lapTimes.size();
        if(laps == 0) return 0;
        return totalTime(lapTimes) / laps;
    }

    public static void main(String[] args) {
        List<Integer> lapTimes = Arrays.asList(33, 21, 21, 22);

        System.out.println(totalTime(lapTimes));
        System.out.println(avgTime(lapTimes));
    }
}
