import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ListVsString {
    public static void main(String[] args) {
        List<String> listA = new ArrayList<>();
        listA.add("A");

        List<String> listB = new ArrayList<>();
        listB.add("B");

        listA.addAll(listB);

        assert(listA.equals(Arrays.asList("A", "B")) && listB.equals(Arrays.asList("B")));

        String stringA = "A";
        String stringB = "B";

        String stringAB = stringA.concat(stringB);

        assert(stringA.equals("A") && stringB.equals("B") && stringAB.equals("AB"));

        List<String> listXY = new ArrayList<>();
        listXY.add("X");
        listXY.add("Y");

        List<String> listY = listXY.subList(1, 2);

        assert(listXY.equals(Arrays.asList("X", "Y")) && listY.equals(Arrays.asList("Y")));

        String stringXY = "XY";

        String stringY = stringXY.substring(1, 2);

        assert(stringXY.equals("XY") && stringY.equals("Y"));
    }
}
