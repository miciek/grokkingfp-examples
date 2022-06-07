import java.util.ArrayList;
import java.util.List;

// Coffee Break exercise
public class ch02_TipCalculation {
    class TipCalculatorBad { // named TipCalculator in the book
        private List<String> names = new ArrayList<>();
        private int tipPercentage = 0;

        public void addPerson(String name) {
            names.add(name);
            if(names.size() > 5) {
                tipPercentage = 20;
            } else if(names.size() > 0) {
                tipPercentage = 10;
            }
        }

        public List<String> getNames() {
            return names;
        }

        public int getTipPercentage() {
            return tipPercentage;
        }
    }

    class TipCalculatorCopying { // named TipCalculator in the book
        private List<String> names = new ArrayList<>();
        private int tipPercentage = 0;

        public void addPerson(String name) {
            names.add(name);
            if(names.size() > 5) {
                tipPercentage = 20;
            } else if(names.size() > 0) {
                tipPercentage = 10;
            }
        }

        public List<String> getNames() {
            return new ArrayList<>(names);
        }

        public int getTipPercentage() {
            return tipPercentage;
        }
    }

    class TipCalculatorRecalculating { // named TipCalculator in the book
        private List<String> names = new ArrayList<>();

        public void addPerson(String name) {
            names.add(name);
        }

        public List<String> getNames() {
            return new ArrayList<>(names);
        }

        public int getTipPercentage() {
            if(names.size() > 5) {
                return 20;
            } else if(names.size() > 0) {
                return 10;
            }
            return 0;
        }
    }

    static class TipCalculator {
        public List<String> addPerson(List<String> names, String name) {
            List<String> updated = new ArrayList<>(names);
            updated.add(name);
            return updated;
        }

        public static int getTipPercentage(List<String> names) {
            if(names.size() > 5) {
                return 20;
            } else if(names.size() > 0) {
                return 10;
            }
            return 0;
        }
    }

    static class TipCalculatorV2 { // named TipCalculator in the book
        public static int getTipPercentage(List<String> names) {
            if(names.size() > 5) {
                return 20;
            } else if(names.size() > 0) {
                return 10;
            } else return 0;
        }
    }

    public static void main(String[] args) {
        List<String> names = new ArrayList<>();
        assert(TipCalculator.getTipPercentage(names) == 0);
        System.out.println(TipCalculator.getTipPercentage(names));
        assert(TipCalculatorV2.getTipPercentage(names) == 0);
        System.out.println(TipCalculatorV2.getTipPercentage(names));

        names.add("Alice");
        names.add("Bob");
        names.add("Charlie");
        assert(TipCalculator.getTipPercentage(names) == 10);
        System.out.println(TipCalculator.getTipPercentage(names));
        assert(TipCalculatorV2.getTipPercentage(names) == 10);
        System.out.println(TipCalculatorV2.getTipPercentage(names));

        names.add("Daniel");
        names.add("Emily");
        names.add("Frank");
        assert(TipCalculator.getTipPercentage(names) == 20);
        System.out.println(TipCalculator.getTipPercentage(names));
        assert(TipCalculatorV2.getTipPercentage(names) == 20);
        System.out.println(TipCalculatorV2.getTipPercentage(names));
    }
}
