import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

class ShoppingCartBad {
    private List<String> items = new ArrayList<>();
    private boolean bookAdded = false;

    public int addItem(String item) {
        items.add(item);
        if(item.equals("Book")) {
            bookAdded = true;
        }
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

class ShoppingCartResponsibility {
    private List<String> items = new ArrayList<>();
    private boolean bookAdded = false;

    public void addItem(String item) {
        items.add(item);
        if(item.equals("Book")) {
            bookAdded = true;
        }
    }

    public List<String> getItems() {
        return items;
    }

    public int getDiscountPercentage() {
        if(bookAdded) {
            return 5;
        } else {
            return 0;
        }
    }
}

class ShoppingCartLessMutableState {
    private List<String> items = new ArrayList<>();

    public void addItem(String item) {
        items.add(item);
    }

    public List<String> getItems() {
        return items;
    }

    public int getDiscountPercentage() {
        if(items.contains("Book")) {
            return 5;
        } else {
            return 0;
        }
    }
}

class ShoppingCart {
    public List<String> addItem(List<String> items, String item) {
        List<String> updated = new ArrayList<>(items);
        updated.add(item);
        return updated;
    }

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
        String apple = "Apple";
        String lemon = "Lemon";
        String mango = "Mango";
        String book = "Book";

        // STEP 0: first implementation
        ShoppingCartBad cart1 = new ShoppingCartBad();
        assert(cart1.addItem(apple) == 0);
        assert(cart1.addItem(lemon) == 0);
        assert(cart1.addItem(book) == 5);
        assert(cart1.addItem(mango) == 5);

        // PROBLEM 1: We cannot do any FP if function does more than one thing

        // STEP 1: SINGLE RESPONSIBILITY
        ShoppingCartResponsibility cart2 = new ShoppingCartResponsibility();
        cart2.addItem(apple);
        assert(cart2.getDiscountPercentage() == 0);

        cart2.addItem(lemon);
        assert(cart2.getDiscountPercentage() == 0);

        cart2.addItem(book);
        assert(cart2.getDiscountPercentage() == 5);

        // PROBLEM 2:
        List<String> items2 = cart2.getItems();
        items2.remove(book);

        assert(!cart2.getItems().contains(book)); // NO BOOK IN THE CART
        assert(cart2.getDiscountPercentage() == 5); // SO SHOULD BE 0!

        // STEP 2: LESS MUTABLE STATE
        ShoppingCartLessMutableState cart3 = new ShoppingCartLessMutableState();
        cart3.addItem(apple);
        assert(cart3.getDiscountPercentage() == 0);

        cart3.addItem(lemon);
        assert(cart3.getDiscountPercentage() == 0);

        cart3.addItem(book);
        assert(cart3.getDiscountPercentage() == 5);

        // PROBLEM 2 SOLVED:
        List<String> items3 = cart3.getItems();
        items3.remove(book);

        assert(!cart3.getItems().contains(book)); // NO BOOK IN THE CART
        assert(cart3.getDiscountPercentage() == 0); // SO DISCOUNT IS 0

        // PROBLEM 3:
        assert(cart3.getDiscountPercentage() == 0); // calling getDiscountPercentage() returns 0
        cart3.addItem(book);
        assert(cart3.getDiscountPercentage() == 5); // calling getDiscountPercentage() returns 5

        // STEP 3: IMMUTABLE
        ShoppingCart cart4 = new ShoppingCart();
        List<String> empty = new ArrayList<>();
        List<String> justApple = cart4.addItem(empty,apple);
        List<String> appleAndBook = cart4.addItem(justApple,book);
        assert(ShoppingCart.getDiscountPercentage(appleAndBook) == 5);

        // PROBLEM 3 SOLVED:
        assert(ShoppingCart.getDiscountPercentage(justApple) == 0); // calling getDiscountPercentage(justApple) returns 0
        assert(ShoppingCart.getDiscountPercentage(appleAndBook) == 5); // calling getDiscountPercentage(appleAndBook) returns 5
    }
}
