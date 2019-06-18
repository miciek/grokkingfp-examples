import java.util.ArrayList;
import java.util.List;

public class Itinerary {
    private List<String> plan = new ArrayList<>();

    public void replan(String splitPoint, List<String> newEnding) {
        int splitAt = plan.indexOf(splitPoint);
        List<String> replanned = plan.subList(0, splitAt + 1);
        replanned.addAll(newEnding);
        plan = replanned;
    }

    public void add(String city) {
        plan.add(city);
    }

    public String toString() {
        return plan.toString();
    }

    public static void main(String[] args) {
        Itinerary plan = new Itinerary();
        plan.add("Paris");
        plan.add("Berlin");
        plan.add("Kraków");
        assert(plan.toString().equals("[Paris, Berlin, Kraków]"));
        System.out.println("Plan: " + plan);

        List<String> endingB = new ArrayList<>();
        endingB.add("Vienna");
        endingB.add("Budapest");
        plan.replan("Berlin", endingB);
        assert(plan.toString().equals("[Paris, Berlin, Vienna, Budapest]"));
        System.out.println("Plan: " + plan);
    }
}
