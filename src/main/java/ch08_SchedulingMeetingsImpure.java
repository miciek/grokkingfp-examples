import java.util.*;

public class ch08_SchedulingMeetingsImpure {
    /**
     * PREREQUISITE 1: MeetingTime model
     * <p>
     * We use MeetingTime defined here in both Java and Scala versions.
     * Note this is written in Java, but in the spirit of immutability.
     * <p>
     * In Scala, this looks like:
     * case class MeetingTime(startHour: Int, endHour: Int)
     */
    static class MeetingTime { // or: record MeetingTime(int startHour, int endHour) {};
        public final int startHour;
        public final int endHour;

        MeetingTime(int startHour, int endHour) {
            this.startHour = startHour;
            this.endHour = endHour;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MeetingTime that = (MeetingTime) o;
            return startHour == that.startHour &&
                    endHour == that.endHour;
        }

        @Override
        public int hashCode() {
            return Objects.hash(startHour, endHour);
        }

        @Override
        public String toString() {
            return "MeetingTime[" +
                    "startHour=" + startHour +
                    ", endHour=" + endHour +
                    ']';
        }
    }
    // In modern Java you can also replace this class with the record type
    // record MeetingTime(int startHour, int endHour) {};

    /*
     * PREREQUISITE 2: unsafe functions that we'll use to simulate external API calls
     *
     * They have *ApiCall suffix:
     * - call an external service to all current calendar entries for a given name.
     * - call an external service to create a new meeting in $name's calendar.
     *
     * These are not an example of FP code, but a simplistic simulation of how some an external API may behave.
     * Note that we can't change an external API and need to work with how it works (so no change in these functions!).
     * Note that we don't consider any security pitfalls her for the sake of a cleaner presentation.
     * Note that most likely this should return a raw JSON, not a List[Meeting], but it doesn't matter here, plus
     * we already know how to deal with parsing.
     */
    static List<MeetingTime> calendarEntriesApiCall(String name) {
        Random rand = new Random();
        if (rand.nextFloat() < 0.25) throw new RuntimeException("Connection error");
        if (name.equals("Alice")) return List.of(new MeetingTime(8, 10), new MeetingTime(11, 12));
        else if (name.equals("Bob")) return List.of(new MeetingTime(9, 10));
        else { // random meeting starting between 8 and 12, and ending between 13 and 16
            return List.of(new MeetingTime(rand.nextInt(5) + 8, rand.nextInt(4) + 13));
        }
    }

    static void createMeetingApiCall(List<String> names, MeetingTime meetingTime) {
        // Note that it also may fail fail, similarly to calendarEntriesApiCall, but we don't show it in the book:
        // Random rand = new Random();
        // if(rand.nextFloat() < 0.25) throw new RuntimeException("💣");
        System.out.printf("SIDE-EFFECT: Created meeting %s for %s\n", meetingTime, Arrays.toString(names.toArray()));
    }

    /**
     * Less evil version of calendarEntriesApiCall used to show the business logic of an imperatively
     * written schedule function.
     */
    private static List<MeetingTime> calendarEntriesApiCallNoFailures(String name) {
        if (name.equals("Alice")) return List.of(new MeetingTime(8, 10), new MeetingTime(11, 12));
        else if (name.equals("Bob")) return List.of(new MeetingTime(9, 10));
        else { // random meeting starting between 8 and 12, and ending between 13 and 16
            Random rand = new Random();
            return List.of(new MeetingTime(rand.nextInt(5) + 8, rand.nextInt(4) + 13));
        }
    }

    /**
     * STEP 0: imperative implementation of the happy path (assuming no failures)
     *
     * For demonstration purposes we are showing a slightly different version than in the book. It's a
     * less evil version of the proper imperative schedule function (below) used to show happy path of the business logic.
     * API calls don't throw any errors, but they still may return different results for the same parameters (randomly).
     */
    static MeetingTime scheduleNoFailures(String person1, String person2, int lengthHours) {
        List<MeetingTime> person1Entries = calendarEntriesApiCallNoFailures(person1); // this is different than in the book
        List<MeetingTime> person2Entries = calendarEntriesApiCallNoFailures(person2); // the book version fails a lot!

        List<MeetingTime> scheduledMeetings = new ArrayList<>();
        scheduledMeetings.addAll(person1Entries);
        scheduledMeetings.addAll(person2Entries);

        List<MeetingTime> slots = new ArrayList<>();
        for (int startHour = 8; startHour < 16 - lengthHours + 1; startHour++) {
            slots.add(new MeetingTime(startHour, startHour + lengthHours));
        }

        List<MeetingTime> possibleMeetings = new ArrayList<>();
        for (var slot : slots) {
            var meetingPossible = true;
            for (var meeting : scheduledMeetings) {
                if (slot.endHour > meeting.startHour && meeting.endHour > slot.startHour) {
                    meetingPossible = false;
                    break;
                }
            }
            if (meetingPossible) {
                possibleMeetings.add(slot);
            }
        }

        if (!possibleMeetings.isEmpty()) {
            createMeetingApiCall(List.of(person1, person2), possibleMeetings.get(0));
            return possibleMeetings.get(0);
        } else return null;
    }

    /**
     * Proper version of imperatively written schedule function with one-retry recovery strategy.
     * Uses calendarEntriesApiCall that may fail.
     */
    static MeetingTime schedule(String person1, String person2, int lengthHours) {
        List<MeetingTime> person1Entries = null;
        try {
            person1Entries = calendarEntriesApiCall(person1);
        } catch (Exception e) {
            // retry:
            person1Entries = calendarEntriesApiCall(person1);
        }

        List<MeetingTime> person2Entries = null;
        try {
            person2Entries = calendarEntriesApiCall(person2);
        } catch (Exception e) {
            // retry:
            person2Entries = calendarEntriesApiCall(person2);
        }

        List<MeetingTime> scheduledMeetings = new ArrayList<>();
        scheduledMeetings.addAll(person1Entries);
        scheduledMeetings.addAll(person2Entries);

        List<MeetingTime> slots = new ArrayList<>();
        for (int startHour = 8; startHour < 16 - lengthHours + 1; startHour++) {
            slots.add(new MeetingTime(startHour, startHour + lengthHours));
        }

        List<MeetingTime> possibleMeetings = new ArrayList<>();
        for (var slot : slots) {
            var meetingPossible = true;
            for (var meeting : scheduledMeetings) {
                if (slot.endHour > meeting.startHour && meeting.endHour > slot.startHour) {
                    meetingPossible = false;
                    break;
                }
            }
            if (meetingPossible) {
                possibleMeetings.add(slot);
            }
        }

        if (!possibleMeetings.isEmpty()) {
            createMeetingApiCall(List.of(person1, person2), possibleMeetings.get(0));
            return possibleMeetings.get(0);
        } else return null;
    }

    public static void main(String[] args) {
        check.apply(scheduleNoFailures("Alice", "Bob", 1)).expect(new MeetingTime(10, 11));
        check.apply(scheduleNoFailures("Alice", "Bob", 2)).expect(new MeetingTime(12, 14));
        check.apply(scheduleNoFailures("Alice", "Bob", 3)).expect(new MeetingTime(12, 15));
        check.apply(scheduleNoFailures("Alice", "Bob", 4)).expect(new MeetingTime(12, 16));
        check.apply(scheduleNoFailures("Alice", "Bob", 5)).expect((MeetingTime) null);
        check.apply(scheduleNoFailures("Alice", "Charlie", 2)).expectThat(meetingTime -> true);

        try {
            check.apply(schedule("Alice", "Bob", 1)).expect(new MeetingTime(10, 11));
            check.apply(schedule("Alice", "Bob", 2)).expect(new MeetingTime(12, 14));
            check.apply(schedule("Alice", "Bob", 3)).expect(new MeetingTime(12, 15));
            check.apply(schedule("Alice", "Bob", 4)).expect(new MeetingTime(12, 16));
            check.apply(schedule("Alice", "Bob", 5)).expect((MeetingTime) null);
            check.apply(schedule("Alice", "Charlie", 2)).expectThat(meetingTime -> true);
        } catch (Throwable t) {
            System.out.println("Caught an exception in the impure version: " + t.getMessage());
        }
    }
}
