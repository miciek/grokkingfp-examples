import java.util.ArrayList;
import java.util.List;

public class Itinerary {
    static List<String> reroute(List<String> route, String splitPoint, List<String> newEnding) {
        int splitIndex = route.indexOf(splitPoint);
        List<String> rerouted = route.subList(0, splitIndex + 1);
        rerouted.addAll(newEnding);
        return rerouted;
    }

    public static void main(String[] args) {
        List<String> route = new ArrayList<>();
        route.add("Paris");
        route.add("Berlin");
        route.add("Prague");
        route.add("Kraków");
        System.out.println("Original route: " + route);

        List<String> altEnding = new ArrayList<>();
        altEnding.add("Vienna");
        altEnding.add("Kraków");
        List<String> rerouted = reroute(route, "Prague", altEnding);
        System.out.println("Rerouted route: " + rerouted);

        List<String> joesEnding = new ArrayList<>();
        joesEnding.add("Budapest");
        joesEnding.add("Kraków");
        List<String> joesRoute = reroute(route, "Prague", joesEnding);
        System.out.println("Joe's route: " + joesRoute);

        System.out.println("Original route: " + route);
    }
}
