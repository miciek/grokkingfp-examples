public class TvShowsJava {
    public static String extractName(String rawShow) throws Exception {
        int bracketOpen = rawShow.indexOf('(');
        if(bracketOpen != -1) return rawShow.substring(0, bracketOpen).trim();
        else throw new Exception();
    }

    public static int extractYearStart(String rawShow) throws Exception {
        int bracketOpen = rawShow.indexOf('(');
        int dash        = rawShow.indexOf('-');
        if(bracketOpen != -1 && dash > bracketOpen + 1) return Integer.parseInt(rawShow.substring(bracketOpen + 1, dash));
        else throw new Exception();
    }

    public static int extractYearEnd(String rawShow) throws Exception {
        int dash         = rawShow.indexOf('-');
        int bracketClose = rawShow.indexOf(')');
        if(dash != -1 && bracketClose > dash + 1) return Integer.parseInt(rawShow.substring(dash + 1, bracketClose));
        else throw new Exception();
    }

    public static TvShow parseShow(String rawShow) throws Exception {
        String name = extractName(rawShow);
        int yearStart = extractYearStart(rawShow);
        int yearEnd = extractYearEnd(rawShow);
        return new TvShow(name, yearStart, yearEnd);
    }

    public static int extractSingleYear(String rawShow) throws Exception {
        int dash         = rawShow.indexOf('-');
        int bracketOpen  = rawShow.indexOf('(');
        int bracketClose = rawShow.indexOf(')');
        if (dash == -1 && bracketOpen != -1 && bracketClose > bracketOpen + 1)
            return Integer.parseInt(rawShow.substring(bracketOpen + 1, bracketClose));
        else throw new Exception();
    }

    public static TvShow parseShow2(String rawShow) throws Exception {
        String name = extractName(rawShow);
        Integer yearStart = null;
        try {
            yearStart = extractYearStart(rawShow);
        } catch(Exception e) {
            yearStart = extractSingleYear(rawShow);
        }
        Integer yearEnd = null;
        try {
            yearEnd = extractYearEnd(rawShow);
        } catch(Exception e) {
            yearEnd = extractSingleYear(rawShow);
        }
        return new TvShow(name, yearStart, yearEnd);
    }
}

class TvShow {
    public final String name;
    public final int yearStart;
    public final int yearEnd;

    public TvShow(String name, int yearStart, int yearEnd) {
        this.name = name;
        this.yearStart = yearStart;
        this.yearEnd = yearEnd;
    }
}