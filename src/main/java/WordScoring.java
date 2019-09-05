import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class WordScoring {
    static int score(String word) {
        return word.replaceAll("a", "").length();
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
        List<String> words = Arrays.asList("ada", "haskell", "scala", "java", "rust");
        {
            Comparator<String> scoreComparator = new Comparator<String>() {
                @Override
                public int compare(String w1, String w2) {
                    return Integer.compare(score(w2), score(w1));
                }
            };

            List<String> wordsCopy = new ArrayList<>(words);
            wordsCopy.sort(scoreComparator);
            System.out.println(wordsCopy);
            assert (wordsCopy.toString().equals("[haskell,rust,scala,java,ada]"));

            List<String> wordRanking = words.stream().sorted(scoreComparator).collect(Collectors.toList());
            System.out.println(wordRanking);
            assert (wordRanking.toString().equals("[haskell,rust,scala,java,ada]"));
        }

        {
            List<String> wordRanking = words
                            .stream()
                            .sorted((w1, w2) -> Integer.compare(score(w2), score(w1)))
                            .collect(Collectors.toList());
            assert (wordRanking.toString().equals("[haskell,rust,scala,java,ada]"));
        }

        {
            Comparator<String> scoreComparator = (w1, w2) -> Integer.compare(score(w2), score(w1));

            List<String> wordRanking = words.stream().sorted(scoreComparator).collect(Collectors.toList());
            assert (wordRanking.toString().equals("[haskell,rust,scala,java,ada]"));
        }

        {
            List<String> topWords = highScoringWords(words);

        }
    }
}
