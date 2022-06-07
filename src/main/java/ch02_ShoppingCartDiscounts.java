import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ch02_ShoppingCartDiscounts {
    static class ShoppingCartBad { // named ShoppingCart in the book
        private List<String> items = new ArrayList<>();
        private boolean bookAdded = false;

        public void addItem(String item) {
            items.add(item);
            if(item.equals("Book")) {
                bookAdded = true;
            }
        }

        public int getDiscountPercentage() {
            if(bookAdded) {
                return 5;
            } else {
                return 0;
            }
        }

        public List<String> getItems() {
            return items;
        }
    }

    static class ShoppingCartCopying { // named ShoppingCart in the book
        private List<String> items = new ArrayList<>();
        private boolean bookAdded = false;

        public void addItem(String item) {
            items.add(item);
            if(item.equals("Book")) {
                bookAdded = true;
            }
        }

        public int getDiscountPercentage() {
            if(bookAdded) {
                return 5;
            } else {
                return 0;
            }
        }

        public List<String> getItems() {
            return new ArrayList<>(items);
        }
    }

    static class ShoppingCartWithRemove { // named ShoppingCart in the book
        private List<String> items = new ArrayList<>();
        private boolean bookAdded = false;

        public void addItem(String item) {
            items.add(item);
            if(item.equals("Book")) {
                bookAdded = true;
            }
        }

        public int getDiscountPercentage() {
            if(bookAdded) {
                return 5;
            } else {
                return 0;
            }
        }

        public List<String> getItems() {
            return new ArrayList<>(items);
        }

        public void removeItem(String item) {
            items.remove(item);
            if(item.equals("Book")) {
                bookAdded = false;
            }
        }
    }

    static class ShoppingCartRecalculating { // named ShoppingCart in the book
        private List<String> items = new ArrayList<>();

        public void addItem(String item) {
            items.add(item);
        }

        public int getDiscountPercentage() {
            if(items.contains("Book")) {
                return 5;
            } else {
                return 0;
            }
        }

        public List<String> getItems() {
            return new ArrayList<>(items);
        }

        public void removeItem(String item) {
            items.remove(item);
        }
    }

    static class ShoppingCart {
        public static int getDiscountPercentage(List<String> items) {
            if(items.contains("Book")) {
                return 5;
            } else {
                return 0;
            }
        }
    }

    public static void main(String[] args) {
        {
            ShoppingCartBad cart = new ShoppingCartBad(); // ShoppingCart in the book
            cart.addItem("Apple");
            assert (cart.getDiscountPercentage() == 0);
            System.out.println(cart.getDiscountPercentage());

            cart.addItem("Lemon"); // not in the book, but lemons are great!
            assert (cart.getDiscountPercentage() == 0);
            System.out.println(cart.getDiscountPercentage());

            cart.addItem("Book");
            assert (cart.getDiscountPercentage() == 5);
            System.out.println(cart.getDiscountPercentage());

            // PROBLEM 1:
            List<String> itemsBad = cart.getItems();
            itemsBad.remove("Book");

            assert(!cart.getItems().contains("Book")); // no book is in the cart
            assert(cart.getDiscountPercentage() == 5); // BUT THE DISCOUNT IS 5!
        }

        // SOLUTION 1: COPYING
        {
            ShoppingCartCopying cart = new ShoppingCartCopying(); // ShoppingCart in the book
            cart.addItem("Apple");
            assert (cart.getDiscountPercentage() == 0);

            cart.addItem("Lemon"); // not in the book, adding anything that isn't a "Book" shouldn't affect the result
            assert (cart.getDiscountPercentage() == 0);

            cart.addItem("Book");
            assert (cart.getDiscountPercentage() == 5);

            List<String> itemsCopying = cart.getItems();
            itemsCopying.remove("Book");

            assert (cart.getItems().contains("Book")); // book is in the cart
            assert (cart.getDiscountPercentage() == 5); // so the discount is 5
        }

        // PROBLEM 2:
        {
            ShoppingCartWithRemove cart = new ShoppingCartWithRemove(); // ShoppingCart in the book
            cart.addItem("Book");
            cart.addItem("Book"); // adding a second book
            assert (cart.getDiscountPercentage() == 5); // calling getDiscountPercentage() returns 5
            cart.removeItem("Book");

            assert (cart.getItems().contains("Book")); // a book is in the cart
            assert (cart.getDiscountPercentage() == 0); // BUT THE DISCOUNT IS 0!
        }

        // SOLUTION 2: RECALCULATING
        {
            ShoppingCartRecalculating cart = new ShoppingCartRecalculating(); // ShoppingCart in the book
            cart.addItem("Book");
            cart.addItem("Book"); // adding a second book
            assert (cart.getDiscountPercentage() == 5); // calling getDiscountPercentage() returns 5
            cart.removeItem("Book");

            assert (cart.getItems().contains("Book")); // a book is in the cart
            assert (cart.getDiscountPercentage() == 5); // and the discount is 5
        }

        // PROBLEM 3
        // so much code to calculate a simple discount...

        // SOLUTION 3: JUST A FUNCTION
        List<String> empty = new ArrayList<>();
        assert(ShoppingCart.getDiscountPercentage(empty) == 0);

        List<String> justApple = Arrays.asList("Apple");
        assert(ShoppingCart.getDiscountPercentage(justApple) == 0);

        List<String> appleAndBook = Arrays.asList("Apple", "Book");
        assert(ShoppingCart.getDiscountPercentage(appleAndBook) == 5);

        // imperative usage
        List<String> items = new ArrayList<>();
        assert(ShoppingCart.getDiscountPercentage(items) == 0);
        System.out.println(ShoppingCart.getDiscountPercentage(items));

        items.add("Apple");
        assert(ShoppingCart.getDiscountPercentage(items) == 0);
        System.out.println(ShoppingCart.getDiscountPercentage(items));

        items.add("Book");
        assert(ShoppingCart.getDiscountPercentage(items) == 5);
        System.out.println(ShoppingCart.getDiscountPercentage(items));
    }
}
