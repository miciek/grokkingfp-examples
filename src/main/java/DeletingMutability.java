import java.util.HashMap;
import java.util.Map;

class CommaChecker {
    private boolean hasCommma = false;

    public CommaChecker(String s) {
        if(s.contains(",")) {
            hasCommma = true;
        }
    }

    public boolean hasComma() {
        return hasCommma;
    }
}

class CommaCheckerNoMutability {
    public static boolean hasComma(String s) {
        return s.contains(",");
    }
}

class Sums {
    private Map<Integer, Integer> squares = new HashMap<>();

    public int getSquare(int n) {
        squares.putIfAbsent(n, n * n);
        return squares.get(n);
    }
}

class SumsNoMutability {
    public static int getSquare(int n) {
        return n * n;
    }
}

public class DeletingMutability {
    public static void main(String[] args) {
      CommaChecker commaChecker = new CommaChecker("has, comma");
      assert(commaChecker.hasComma());
      assert(CommaCheckerNoMutability.hasComma("has, comma"));

      Sums sums = new Sums();
      assert(sums.getSquare(4) == 16);
      assert(SumsNoMutability.getSquare(4) == 16);
    }
}
