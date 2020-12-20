import java.util.ArrayList;
import java.util.List;

public class ch02_PureFunctions {
    static double f(double x) {
        return x * 95.0 / 100.0;
    }

    static int f1(String s) {
        return s.length() * 3;
    }

    static double f2(double x) {
        return x * Math.random();
    }

    static int increment(int x) {
        return x + 1;
    }

    static double randomPart(double x) {
        return x * Math.random();
    }

    static int add(int a, int b) {
        return a + b;
    }

    static class ShoppingCart {
        private List<String> items = new ArrayList<>();

        public int addItem(String item) {
            items.add(item);
            return items.size() + 5;
        }
    }

    static char getFirstCharacter(String s) {
        return s.charAt(0);
    }

    public static void main(String[] args) {
        assert(f(20) == 19);
        assert(f(100) == 95);
        assert(f(10) == 9.5);

        assert(f1("Scala") == 15);
        assert(f2(20) <= 20);

        assert(increment(2) == 3);
        assert(randomPart(10) >= 0);
        assert(add(2, 3) == 5);

        ShoppingCart cart = new ShoppingCart();
        assert(cart.addItem("item") == 6);
    }
}
