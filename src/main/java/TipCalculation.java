import java.util.ArrayList;
import java.util.List;

class TipCalculatorBad {
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

class TipCalculatorCopying {
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

class TipCalculatorRecalculating {
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

class TipCalculator {
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

public class TipCalculation {
    public static void main(String[] args) {
        List<String> names = new ArrayList<>();
        assert(TipCalculator.getTipPercentage(names) == 0);

        names.add("Alice");
        names.add("Bob");
        names.add("Charlie");
        assert(TipCalculator.getTipPercentage(names) == 10);

        names.add("Daniel");
        names.add("Emily");
        names.add("Frank");
        assert(TipCalculator.getTipPercentage(names) == 20);
    }
}
