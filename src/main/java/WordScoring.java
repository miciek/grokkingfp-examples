import java.util.*;
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

    static int scoreWithBonusAndPenalty(String word) {
        int base = score(word);
        int bonus = word.contains("c") ? 5 : 0;
        int penalty = word.contains("s") ? 7 : 0;
        return base + bonus - penalty;
    }

    static int bonus(String word) {
        return word.contains("c") ? 5 : 0;
    }

    static int penalty(String word) {
        return word.contains("s") ? 7 : 0;
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

    static List<Integer> wordScores(List<String> words, Function<String, Integer> wordScore) {
        List<Integer> result = new ArrayList<>();
        for(String word : words) {
            result.add(wordScore.apply(word));
        }
        return result;
    }

    static List<Integer> wordScoresStreams(List<String> words, Function<String, Integer> wordScore) {
        return words.stream().map(wordScore).collect(Collectors.toList());
    }

    static List<String> highScoringWords(List<String> words, Function<String, Integer> wordScore) {
        List<String> result = new ArrayList<>();
        for (String word : words) {
            if (wordScore.apply(word) > 1)
                result.add(word);
        }
        return result;
    }

    static int cumulativeScore(List<String> words, Function<String, Integer> wordScore) {
        int result = 0;
        for (String word : words) {
            result += wordScore.apply(word);
        }
        return result;
    }

    public static void main(String[] args) {
        {
            List<String> words = Arrays.asList("ada", "haskell", "scala", "java", "rust");
            List<String> ranking = rankedWordsMutable(words);
            System.out.println(ranking);
            assert (ranking.toString().equals("[haskell, rust, scala, java, ada]"));

            System.out.println(words);
            assert (words.toString().equals("[haskell, rust, scala, java, ada]"));
        }

        List<String> words = Arrays.asList("ada", "haskell", "scala", "java", "rust");

        {
            List<String> ranking = rankedWords(words);
            System.out.println(ranking);
            assert (ranking.toString().equals("[haskell, rust, scala, java, ada]"));
            System.out.println(words);
            assert (words.toString().equals("[ada, haskell, scala, java, rust]"));
        }

        {
            List<String> ranking = rankedWords(words, scoreComparator);
            assert (ranking.toString().equals("[haskell, rust, scala, java, ada]"));
        }

        {
            Comparator<String> scoreWithBonusComparator = new Comparator<String>() {
                @Override
                public int compare(String w1, String w2) {
                    return Integer.compare(scoreWithBonus(w2), scoreWithBonus(w1));
                }
            };

            List<String> ranking = rankedWords(words, scoreWithBonusComparator);
            assert (ranking.toString().equals("[scala, haskell, rust, java, ada]"));
        }

        {
            Comparator<String> scoreComparator2 = (w1, w2) -> Integer.compare(score(w2), score(w1));
            List<String> ranking = rankedWords(words, scoreComparator2);
            assert (ranking.toString().equals("[haskell, rust, scala, java, ada]"));

            Comparator<String> scoreWithBonusComparator = (w1, w2) -> Integer.compare(scoreWithBonus(w2), scoreWithBonus(w1));
            List<String> rankingWithBonus = rankedWords(words, scoreWithBonusComparator);
            assert (rankingWithBonus.toString().equals("[scala, haskell, rust, java, ada]"));
        }

        {
            Function<String, Integer> scoreFunction = w -> score(w);
            List<String> ranking = rankedWords(words, scoreFunction);
            assert (ranking.toString().equals("[haskell, rust, scala, java, ada]"));

            Function<String, Integer> scoreWithBonusFunction = w -> scoreWithBonus(w);
            List<String> rankingWithBonus = rankedWords(words, scoreWithBonusFunction);
            assert (rankingWithBonus.toString().equals("[scala, haskell, rust, java, ada]"));
        }

        {
            List<String> ranking = rankedWords(words, w -> score(w));
            System.out.println(ranking);
            assert (ranking.toString().equals("[haskell, rust, scala, java, ada]"));

            List<String> bonusRanking = rankedWords(words, w -> scoreWithBonus(w));
            System.out.println(bonusRanking);
            assert (bonusRanking.toString().equals("[scala, haskell, rust, java, ada]"));

            List<String> bonusPenaltyRanking = rankedWords(words, w -> scoreWithBonusAndPenalty(w));
            System.out.println(bonusPenaltyRanking);
            assert (bonusPenaltyRanking.toString().equals("[java, ada, scala, haskell, rust]"));

            List<String> bonusPenaltyRanking2 = rankedWords(words, w -> score(w) + bonus(w) - penalty(w));
            System.out.println(bonusPenaltyRanking2);
            assert (bonusPenaltyRanking2.toString().equals("[java, ada, scala, haskell, rust]"));
        }

        {
            System.out.println(words);
            List<Integer> ranking = wordScores(words, w -> score(w) + bonus(w) - penalty(w));
            System.out.println(ranking);
            assert (ranking.toString().equals("[1, -1, 1, 2, -3]"));
        }

        {
            System.out.println(words);
            List<Integer> ranking = wordScoresStreams(words, w -> score(w) + bonus(w) - penalty(w));
            System.out.println(ranking);
            assert (ranking.toString().equals("[1, -1, 1, 2, -3]"));
        }

        {
            System.out.println(words);
            List<String> result = highScoringWords(words, w -> score(w) + bonus(w) - penalty(w));
            System.out.println(result);
            assert (result.toString().equals("[java]"));
        }

        {
            System.out.println(words);
            int result = cumulativeScore(words, w -> score(w) + bonus(w) - penalty(w));
            System.out.println(result);
            assert (result == 0);
        }
    }
}
