import java.util.function.Function;

public class ch04_JavaFunctionIntro {
    static int score(String word) {
        return word.replaceAll("a", "").length();
    }

    static boolean isHighScoringWord(String word) {
        return score(word) > 5;
    }

    public static void main(String[] args) {
        Function<String, Integer> scoreFunction = w -> w.replaceAll("a", "").length();
        assert(scoreFunction.apply("java") == score("java"));
        System.out.println(scoreFunction.apply("java"));

        Function<String, Integer> f = scoreFunction;
        assert(f.apply("java") == score("java"));
        System.out.println(f.apply("java"));

        Function<String, Boolean> isHighScoringWordFunction = w -> scoreFunction.apply(w) > 5;
        assert(isHighScoringWordFunction.apply("java") == isHighScoringWord("java"));
        System.out.println(isHighScoringWordFunction.apply("java"));
    }
}
