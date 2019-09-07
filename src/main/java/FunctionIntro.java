import java.util.function.Function;

public class FunctionIntro {
    public static void main(String[] args) {
        Function<String, Integer> scoreFunction = w -> w.replaceAll("a", "").length();
        System.out.println(scoreFunction.apply("java"));
        Function<String, Boolean> scoreHigherThan5 = w -> scoreFunction.apply(w) > 5;
        System.out.println(scoreHigherThan5.apply("java"));
    }
}
