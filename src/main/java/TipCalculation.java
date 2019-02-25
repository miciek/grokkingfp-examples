import java.util.ArrayList;
import java.util.List;

class TipCalculatorBad {
    private List<String> names = new ArrayList<>();
    private int tipPercentage = 0;

    public int addPerson(String name) {
        names.add(name);
        if(names.size() > 5) {
            tipPercentage = 20;
        } else if(names.size() > 0) {
            tipPercentage = 10;
        }
        return tipPercentage;
    }

    public List<String> getNames() {
        return names;
    }
}

class TipCalculatorResponsibility {
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

class TipCalculatorLessMutableState {
    private List<String> names = new ArrayList<>();

    public void addPerson(String name) {
        names.add(name);
    }

    public List<String> getNames() {
        return names;
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

    public int getTipPercentage(List<String> names) {
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
        TipCalculatorBad tipCalculator = new TipCalculatorBad();
        assert(tipCalculator.addPerson("first") == 10);
        assert(tipCalculator.addPerson("second") == 10);
        assert(tipCalculator.addPerson("third") == 10);
        assert(tipCalculator.addPerson("fourth") == 10);
        assert(tipCalculator.addPerson("fifth") == 10);
        assert(tipCalculator.addPerson("sixth") == 20);
    }
}
