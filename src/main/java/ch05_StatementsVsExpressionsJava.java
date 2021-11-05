import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ch05_StatementsVsExpressionsJava {
    public static void main(String[] args) {
        // Java uses statements:
        List<Integer> xs = Arrays.asList(1, 2, 3, 4, 5);
        List<Integer> result = new ArrayList<>();

        for (Integer x: xs) {
            result.add(x * x);
        }
        assert(result.toString().equals("[1, 4, 9, 16, 25]"));

        // but you can use expressions that embrace immutability:
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);
        List<Integer> doubledNumbers = numbers.stream().map(n -> n * 2).collect(Collectors.toList());
        assert(doubledNumbers.toString().equals("[2, 4, 6, 8, 10]"));
    }
}
