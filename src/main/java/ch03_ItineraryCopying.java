import java.util.ArrayList;
import java.util.List;

public class ch03_ItineraryCopying {
    static List<String> replan(List<String> plan,
                               String newCity,
                               String beforeCity) {
        int newCityIndex = plan.indexOf(beforeCity);
        plan.add(newCityIndex, newCity);
        return plan;
    }

    static List<String> replanPure(List<String> plan,
                               String newCity,
                               String beforeCity) { // named replan in the book
        int newCityIndex = plan.indexOf(beforeCity);
        List<String> replanned = new ArrayList<>(plan);
        replanned.add(newCityIndex, newCity);
        return replanned;
    }

    public static void main(String[] args) {
        List<String> planA = new ArrayList<>();
        planA.add("Paris");
        planA.add("Berlin");
        planA.add("Kraków");
        System.out.println("Plan A: " + planA);

        List<String> planB = replan(planA, "Vienna", "Kraków");
        assert(planB.toString().equals("[Paris, Berlin, Vienna, Kraków]"));
        System.out.println("Plan B: " + planB);

        assert(planA.toString().equals("[Paris, Berlin, Vienna, Kraków]"));
        System.out.println("Plan A: " + planA);

        List<String> planAPure = new ArrayList<>();
        planAPure.add("Paris");
        planAPure.add("Berlin");
        planAPure.add("Kraków");
        System.out.println("Plan A (pure): " + planAPure);

        List<String> planBPure = replanPure(planAPure, "Vienna", "Kraków");
        assert(planBPure.toString().equals("[Paris, Berlin, Vienna, Kraków]"));
        System.out.println("Plan B (pure): " + planBPure);

        assert(planAPure.toString().equals("[Paris, Berlin, Kraków]"));
        System.out.println("Plan A (pure): " + planAPure);
    }
}
