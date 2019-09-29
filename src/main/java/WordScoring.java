import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public class WordScoring {
    static int score(String word) {
        return word.replaceAll("a", "").length();
    }

    static int scoreWithBonus(String word) {
        int base = score(word);
        if (word.contains("c"))
            return base + 5;
        else
            return base;
    }

    static Comparator<String> scoreComparator = new Comparator<String>() {
        @Override
        public int compare(String w1, String w2) {
            return Integer.compare(score(w2), score(w1));
        }
    };

    static List<String> rankedWordsMutable(List<String> words) {
        words.sort(scoreComparator);
        return words;
    }

    static List<String> rankedWords(List<String> words) {
        return words.stream().sorted(scoreComparator).collect(Collectors.toList());
    }

    static List<String> rankedWords(List<String> words, Comparator<String> comparator) {
        return words.stream().sorted(comparator).collect(Collectors.toList());
    }

    static List<String> rankedWords(List<String> words, Function<String, Integer> wordScore) {
        Comparator<String> comparator = (w1, w2) -> Integer.compare(wordScore.apply(w2), wordScore.apply(w1));
        return words.stream().sorted(comparator).collect(Collectors.toList());
    }

    static List<String> highScoringWords(List<String> words) {
        List<String> result = new ArrayList<>();
        for (String word : words) {
            if (score(word) > 3)
                result.add(word);
        }
        return result;
    }

    public static void main(String[] args) {
        {
            List<String> words = Arrays.asList("ada", "haskell", "scala", "java", "rust");
            List<String> ranking = rankedWordsMutable(words);
            System.out.println(ranking);
            assert (ranking.toString().equals("[haskell,rust,scala,java,ada]"));

            System.out.println(words);
            assert (words.toString().equals("[haskell,rust,scala,java,ada]"));
        }

        List<String> words = Arrays.asList("ada", "haskell", "scala", "java", "rust");

        {
            List<String> ranking = rankedWords(words);
            System.out.println(ranking);
            assert (ranking.toString().equals("[haskell,rust,scala,java,ada]"));
            System.out.println(words);
            assert (words.toString().equals("[ada,haskell,scala,java,rust]"));
        }

        {
            List<String> ranking = rankedWords(words, scoreComparator);
            assert (ranking.toString().equals("[haskell,rust,scala,java,ada]"));
        }

        {
            Comparator<String> scoreWithBOnusComparator = new Comparator<String>() {
                @Override
                public int compare(String w1, String w2) {
                    return Integer.compare(scoreWithBonus(w2), scoreWithBonus(w1));
                }
            };

            List<String> ranking = rankedWords(words, scoreWithBOnusComparator);
            assert (ranking.toString().equals("[scala,haskell,rust,java,ada]"));
        }

        {
            List<String> ranking = rankedWords(words, (w1, w2) -> Integer.compare(score(w2), score(w1)));
            assert (ranking.toString().equals("[haskell,rust,scala,java,ada]"));

            Comparator<String> scoreComparator2 = (w1, w2) -> Integer.compare(score(w2), score(w1));
        }

        {
            List<String> ranking = rankedWords(words, (w1, w2) -> Integer.compare(scoreWithBonus(w2), scoreWithBonus(w1)));
            System.out.println(ranking);
            assert (ranking.toString().equals("[scala,haskell,rust,java,ada]"));
        }

        {
            List<String> ranking = rankedWords(words, w -> score(w));
            System.out.println(ranking);
            assert (ranking.toString().equals("[scala,haskell,rust,java,ada]"));

            List<String> bonusRanking = rankedWords(words, w -> scoreWithBonus(w));
            System.out.println(bonusRanking);
            assert (bonusRanking.toString().equals("[scala,haskell,rust,java,ada]"));
        }
    }
}
