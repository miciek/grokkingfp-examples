import java.util.*;

public class ch08_ConsoleInterface {
    /*
     * PREREQUISITE 3: potentially unsafe, side-effectful functions that print and read lines from stdout/in
     * 
     * We define them here to be consistent with the style of the book: imperative code is written in Java.
     */
    static void consolePrint(String message) {
        System.out.println(message);
    }

    static String consoleGet() {
        return new Scanner(System.in).nextLine();
    }
}
