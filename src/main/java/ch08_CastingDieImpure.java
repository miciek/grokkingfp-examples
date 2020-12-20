import java.util.Random;

public class ch08_CastingDieImpure {
    static int getIntUnsafely() {
        Random rand = new Random();
        if (rand.nextBoolean()) throw new RuntimeException();
        return rand.nextInt(6) + 1;
    }

    public static class NoFailures {
        static int castTheDieImpure() {
            System.out.println("The die is cast");
            Random rand = new Random();
            return rand.nextInt(6) + 1;
        }
    }

    public static class WithFailures {
        static int castTheDieImpure() {
            Random rand = new Random();
            if (rand.nextBoolean()) throw new RuntimeException("Die fell off");
            return rand.nextInt(6) + 1;
        }
    }

    static int drawAPointCard() {
        Random rand = new Random();
        if (rand.nextBoolean()) throw new RuntimeException("No cards");
        return rand.nextInt(14) + 1;
    }
}
