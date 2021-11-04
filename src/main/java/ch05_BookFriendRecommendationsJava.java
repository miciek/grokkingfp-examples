import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ch05_BookFriendRecommendationsJava {
    public static List<Movie> bookAdaptations(String author) {
        if (author.equals("Tolkien")) {
            return Arrays.asList(new Movie("An Unexpected Journey"),
                    new Movie("The Desolation of Smaug"));
        } else return Collections.emptyList();
    }

    public static List<String> recommendationFeed(List<Book> books) {
        List<String> result = new ArrayList<>();
        for (Book book : books)
            for (String author : book.authors)
                for (Movie movie : bookAdaptations(author)) {
                    result.add(String.format(
                            "You may like %s, because you liked %s's %s",
                            movie.title, author, book.title));
                }
        return result;
    }

    public static void main(String[] args) {
        List<Book> books = Arrays.asList(new Book("FP in Scala", Arrays.asList("Chiusano", "Bjarnason")),
                new Book("The Hobbit", Arrays.asList("Tolkien")));
        System.out.println(recommendationFeed(books));
        assert(recommendationFeed(books).toString().equals(
            "[You may like An Unexpected Journey, because you liked Tolkien's The Hobbit, "+
            "You may like The Desolation of Smaug, because you liked Tolkien's The Hobbit]"
        ));

        // Java uses statements:
        List<Integer> xs = Arrays.asList(1, 2, 3, 4, 5);
        List<Integer> result = new ArrayList<>();

        for (Integer x: xs) {
            result.add(x * x);
        }
        assert(result.toString().equals("[1, 4, 9, 16, 25]"));
    }
}

class Book {
    final String title;
    final List<String> authors;

    Book(String title, List<String> authors) {
        this.title = title;
        this.authors = authors;
    }
}

class Movie {
    final String title;

    Movie(String title) {
        this.title = title;
    }
}
