import java.util.ArrayList;
import java.util.List;

public class ch01_Intro {
    static class Author {}

    class Book {
        private String title;
        private List<Author> authors;

        public Book(String title) {
            this.title = title;
            this.authors = new ArrayList<Author>();
        }

        public void addAuthor(Author author) {
            this.authors.add(author);
        }
    }

    static class NotEnoughIngredientsException extends RuntimeException {}

    private static void add(String ingredient) {}
    private static void heatUpUntilBoiling() {}
    private static void addVegetablesUsing(List<String> ingredients) {}
    private static void waitMinutes(int minutes) {}

    public void makeSoup(List<String> ingredients)  {
        if(ingredients.contains("water")) {
            add("water");
        } else throw new NotEnoughIngredientsException();
        heatUpUntilBoiling();
        addVegetablesUsing(ingredients);
        waitMinutes(20);
    }

    public static class Soup {}

    public static int add(int a, int b) {
        return a + b;
    }

    public static char getFirstCharacter(String s) {
        return s.charAt(0);
    }

    public static int divide(int a, int b) {
        return a / b;
    }

    public static void eatSoup(Soup soup) {
        // TODO: “eating the soup” algorithm
    }

    public static int increment(int x) {
        return x + 1;
    }

    public static String concatenate(String a, String b) {
        return a + b;
    }

    public static int calculateScore(String word)  {
        int score = 0;
        for(char c : word.toCharArray()) {
            score++;
        }
        return score;
    }

    public static int wordScore(String word)  {
        return word.length();
    }

    // Coffee Break exercise
    public static int calculateScore2(String word) { // named calculateScore in the book
        int score = 0;
        for(char c : word.toCharArray()) {
            if(c != 'a')
                score++;
        }
        return score;
    }

    public static int wordScore2(String word) { // named wordScore in the book
        return word.replace("a", "").length();
    }

    public static String stringWithoutChar(String s, char c) {
        return s.replace(Character.toString(c), "");
    }

    public static int wordScore3(String word) { // named wordScore in the book
        return stringWithoutChar(word, 'a').length();
    }

    public static void main(String[] args) {
        System.out.println(add(2, 4));
        assert(add(2, 4) == 6);

        System.out.println(getFirstCharacter("Ola"));
        assert(getFirstCharacter("Ola") == 'O');

        System.out.println(divide(12, 2));
        assert(divide(12, 2) == 6);

        eatSoup(new Soup());

        System.out.println(increment(6));
        assert(increment(6) == 7);

        System.out.println(concatenate("6", "7"));
        assert(concatenate("6", "7").equals("67"));

        System.out.println(calculateScore("imperative"));
        assert(wordScore("imperative") == 10);

        System.out.println(wordScore("declarative"));
        assert(wordScore("declarative") == 11);

        assert(calculateScore2("imperative") == 9 && wordScore2("declarative") == 9 && wordScore3("declarative") == 9);
    }
}
