import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Itinerary {
    private List<String> plan = new ArrayList<>();

    public void replan(String newCity, String beforeCity) {
        int newCityIndex = plan.indexOf(beforeCity);
        plan.add(newCityIndex, newCity);
    }

    public void add(String city) {
        plan.add(city);
    }

    public List<String> getPlan() {
        return Collections.unmodifiableList(plan);
    }

    public static void main(String[] args) {
        Itinerary plan = new Itinerary();
        plan.add("Paris");
        plan.add("Berlin");
        plan.add("Kraków");
        assert(plan.getPlan().toString().equals("[Paris, Berlin, Kraków]"));
        System.out.println("Plan: " + plan.getPlan());

        plan.replan("Vienna", "Kraków");
        assert(plan.getPlan().toString().equals("[Paris, Berlin, Vienna, Kraków]"));
        System.out.println("Plan: " + plan.getPlan());
    }
}
