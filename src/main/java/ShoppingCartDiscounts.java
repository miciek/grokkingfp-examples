import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class ShoppingCartBad {
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

class ShoppingCartCopying {
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

class ShoppingCartWithRemove {
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

class ShoppingCartRecalculating {
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

class ShoppingCart {
    public static int getDiscountPercentage(List<String> items) {
        if(items.contains("Book")) {
            return 5;
        } else {
            return 0;
        }
    }
}

public class ShoppingCartDiscounts {
    public static void main(String[] args) {
        ShoppingCartBad cartBad = new ShoppingCartBad();
        cartBad.addItem("Apple");
        assert(cartBad.getDiscountPercentage() == 0);

        cartBad.addItem("Lemon");
        assert(cartBad.getDiscountPercentage() == 0);

        cartBad.addItem("Book");
        assert(cartBad.getDiscountPercentage() == 5);

        // PROBLEM 1:
        List<String> itemsBad = cartBad.getItems();
        itemsBad.remove("Book");

        assert(!cartBad.getItems().contains("Book")); // no book is in the cart
        assert(cartBad.getDiscountPercentage() == 5); // BUT DISCOUNT IS 5!

        // SOLUTION 1: COPYING
        ShoppingCartCopying cartCopying = new ShoppingCartCopying();
        cartCopying.addItem("Apple");
        assert(cartCopying.getDiscountPercentage() == 0);

        cartCopying.addItem("Lemon");
        assert(cartCopying.getDiscountPercentage() == 0);

        cartCopying.addItem("Book");
        assert(cartCopying.getDiscountPercentage() == 5);

        List<String> itemsCopying = cartCopying.getItems();
        itemsCopying.remove("Book");

        assert(cartCopying.getItems().contains("Book")); // book is in the cart
        assert(cartCopying.getDiscountPercentage() == 5); // so the discount is 5

        // PROBLEM 2:
        ShoppingCartWithRemove cartWithRemove = new ShoppingCartWithRemove();
        cartWithRemove.addItem("Book");
        cartWithRemove.addItem("Book"); // adding second book
        assert(cartWithRemove.getDiscountPercentage() == 5); // calling getDiscountPercentage() returns 5
        cartWithRemove.removeItem("Book");

        assert(cartWithRemove.getItems().contains("Book")); // a book is in the cart
        assert(cartWithRemove.getDiscountPercentage() == 0); // BUT THE DISCOUNT IS 0!

        // SOLUTION 2: RECALCULATING
        ShoppingCartRecalculating cartRecalculating = new ShoppingCartRecalculating();
        cartRecalculating.addItem("Book");
        cartRecalculating.addItem("Book"); // adding second book
        assert(cartRecalculating.getDiscountPercentage() == 5); // calling getDiscountPercentage() returns 5
        cartRecalculating.removeItem("Book");

        assert(cartRecalculating.getItems().contains("Book")); // a book is in the cart
        assert(cartRecalculating.getDiscountPercentage() == 5); // and the discount is 5

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

        items.add("Apple");
        assert(ShoppingCart.getDiscountPercentage(items) == 0);

        items.add("Book");
        assert(ShoppingCart.getDiscountPercentage(items) == 5);
    }
}
