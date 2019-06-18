import java.util.ArrayList;
import java.util.List;

public class ItineraryCopying {
    static List<String> replan(List<String> plan,
                               String splitPoint,
                               List<String> newEnding) {
        int splitAt = plan.indexOf(splitPoint);
        List<String> replanned = plan.subList(0, splitAt + 1);
        replanned.addAll(newEnding);
        return replanned;
    }

    static List<String> replanPure(List<String> plan,
                               String splitPoint,
                               List<String> newEnding) {
        int splitAt = plan.indexOf(splitPoint);
        List<String> replanned = new ArrayList<>();
        replanned.addAll(plan.subList(0, splitAt + 1));
        replanned.addAll(newEnding);
        return replanned;
    }

    public static void main(String[] args) {
        List<String> planA = new ArrayList<>();
        planA.add("Paris");
        planA.add("Berlin");
        planA.add("Kraków");
        System.out.println("Plan A: " + planA);

        List<String> endingB = new ArrayList<>();
        endingB.add("Vienna");
        endingB.add("Budapest");
        List<String> planB = replan(planA, "Berlin", endingB);
        System.out.println("Plan B: " + planB);

        System.out.println("Plan A: " + planA);

        List<String> alphabet = new ArrayList<>();
        alphabet.add("A");
        alphabet.add("B");
        alphabet.add("C");

        List<String> subList = alphabet.subList(0, 2);
        System.out.println(subList);
        subList.add("D");
        System.out.println(subList);
        System.out.println(alphabet);

        List<String> planAPure = new ArrayList<>();
        planAPure.add("Paris");
        planAPure.add("Berlin");
        planAPure.add("Kraków");
        System.out.println("Plan A (pure): " + planAPure);

        List<String> endingBPure = new ArrayList<>();
        endingBPure.add("Vienna");
        endingBPure.add("Budapest");
        List<String> planBPure = replanPure(planAPure, "Berlin", endingBPure);
        System.out.println("Plan B (pure): " + planBPure);

        System.out.println("Plan A (pure): " + planAPure);
    }

}
