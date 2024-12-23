import java.util.*;

public class ch08_SchedulingMeetingsAPI {
	/*
	 * PREREQUISITE: unsafe functions that we'll use to simulate external API calls
	 *
	 * They have *ApiCall suffix: - call an external service to all current calendar
	 * entries for a given name. - call an external service to create a new meeting
	 * in $name's calendar.
	 *
	 * These are not an example of FP code, but a simplistic simulation of how some
	 * an external API may behave. Note that we can't change an external API and
	 * need to work with how it works (so no change in these functions!). Note that
	 * we don't consider any security pitfalls her for the sake of a cleaner
	 * presentation. Note that most likely this should return a raw JSON, not a
	 * List[Meeting], but it doesn't matter here, plus we already know how to deal
	 * with parsing.
	 */
	static List<MeetingTime> calendarEntriesApiCall(String name) {
		Random rand = new Random();
		if (rand.nextFloat() < 0.25)
			throw new RuntimeException("Connection error");
		if (name.equals("Alice"))
			return List.of(new MeetingTime(8, 10), new MeetingTime(11, 12));
		else if (name.equals("Bob"))
			return List.of(new MeetingTime(9, 10));
		else { // random meeting starting between 8 and 12, and ending between 13 and 16
			return List.of(new MeetingTime(rand.nextInt(5) + 8, rand.nextInt(4) + 13));
		}
	}

	static void createMeetingApiCall(List<String> names, MeetingTime meetingTime) {
		// Note that it also may fail fail, similarly to calendarEntriesApiCall, but we
		// don't show it in the book:
		// Random rand = new Random();
		// if(rand.nextFloat() < 0.25) throw new RuntimeException("💣");
		System.out.printf("SIDE-EFFECT: Created meeting %s for %s\n", meetingTime, Arrays.toString(names.toArray()));
	}

	static void createMeetingApiCall(List<String> names, int startHour, int endHour) {
		// Note that it also may fail fail, similarly to calendarEntriesApiCall, but we
		// don't show it in the book:
		// Random rand = new Random();
		// if(rand.nextFloat() < 0.25) throw new RuntimeException("💣");
		System.out.printf("SIDE-EFFECT: Created meeting %s for %s\n", new MeetingTime(startHour, endHour),
				Arrays.toString(names.toArray()));
	}
}
