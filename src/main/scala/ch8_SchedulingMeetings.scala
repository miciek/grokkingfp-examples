import cats.effect.IO
import cats.implicits._

import scala.util.Random

/**
  * Goals:
  * - how to use data from unsafe/unstable sources?
  * - how to write data to unsafe/unstable sources?
  * - indicating your code does side-effects
  * - separating pure and impure code
  * - refactoring ability without changing behavior
  *
  * TODO:
  * - side-effects, IO - different approaches in FP (F#, Haskell, Scala)
  * - but one thing in common - separate pure and impure (side-effecting code) (lying signatures)
  * - example approach: IO from cats-effect (but there are different approaches with similar goals-let's learn intuition)
  * - error handling + requirements as types (+ referential transparency)
  * - outside world we can't change but need to work with it (not around!)
  * - FP API vs standard void API
  * - nondeterministic actions: api calls, db calls, random, tim
  * - actions that do something visible: println, draw on screen
  * - laziness vs strictness (IO vs Either)
  * - why do we need IO (ref transparency, deal with wait, changing data, exceptions)
  * - testing
  * - IO type
  * - interpreter (like high-level conditions in ch7 that can be folded into low level conditions)
  *   - verbs as case classes
  * - 99% of data is not known at compile time
  *   - Either with types, not strings!
  *   - recovering from Either left (leftMap?)
  * - ADT as error side in Either
  *  - many threads intro: what if there are several meetings being scheduled at one time?
  *  - attempt can be useful
  *  - program is just a value (it can be taken as argument and returned, it can be mapped) - see retries or many attendees
  *  - refer to ch6 (errors) and ch7 (requirements as types) to make sure the big picture is understood
  * - mapping lists inside IO (map inside map)
  * - separating concerns using currying (retries on different parameter list, passing already retried functions)
  */
object ch8_SchedulingMeetings extends App {
  // MODEL
  case class Meeting(startHour: Int, endHour: Int)

  def meetingsOverlap(meeting1: Meeting, meeting2: Meeting): Boolean = {
    meeting1.endHour > meeting2.startHour && meeting2.endHour > meeting1.startHour
  }

  /*
   * PREREQUISITES: we use unsafe functions that we'll use to simulate external API calls
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
  object ApiCallFunctions {
    object AlwaysSucceeding {
      def calendarEntriesApiCall(name: String): List[Meeting] = {
        if (name == "Alice") List(Meeting(8, 10), Meeting(11, 12))
        else if (name == "Bob") List(Meeting(9, 10))
        else List(Meeting(Random.between(8, 12), Random.between(12, 16)))
      }

      def createMeetingApiCall(names: List[String], meeting: Meeting): Unit = {
        println(s"Created meeting from ${meeting.startHour} to ${meeting.endHour} for $names")
      }
    }

    object RandomErrors {
      def calendarEntriesApiCall(name: String): List[Meeting] = {
        if (Random.nextFloat() < 0.25) throw new Exception("Connection error")
        if (name == "Alice") List(Meeting(8, 10), Meeting(11, 12))
        else if (name == "Bob") List(Meeting(9, 10))
        else List(Meeting(Random.between(8, 12), Random.between(12, 16)))
      }

      def createMeetingApiCall(names: List[String], meeting: Meeting): Unit = {
        if (Random.nextFloat() < 0.25) throw new Exception("Connection error")
        println(s"Created meeting from ${meeting.startHour} to ${meeting.endHour} for $names")
      }
    }

  }

  println("STEP 0")

  // STEP 0: use what we know (maybe in Java?)
  {
    import ApiCallFunctions.AlwaysSucceeding._

    def schedule(person1: String, person2: String, lengthHours: Int): Option[Meeting] = {
      val person1Entries = calendarEntriesApiCall(person1)
      val person2Entries = calendarEntriesApiCall(person2)

      val slots             = List.range(8, 16 - lengthHours + 1).map(startHour => Meeting(startHour, startHour + lengthHours))
      val scheduledMeetings = person1Entries.appendedAll(person2Entries)
      val possibleMeetings  = slots.filter(slot => scheduledMeetings.forall(meeting => !meetingsOverlap(meeting, slot)))

      if (possibleMeetings.nonEmpty) {
        createMeetingApiCall(List(person1, person2), possibleMeetings.head)
        Some(possibleMeetings.head)
      } else None
    }

    check { schedule("Alice", "Bob", 1) }.expect {
      Some(Meeting(10, 11))
    }
    check { schedule("Alice", "Bob", 2) }.expect {
      Some(Meeting(12, 14))
    }
    check { schedule("Alice", "Bob", 3) }.expect {
      Some(Meeting(12, 15))
    }
    check { schedule("Alice", "Bob", 4) }.expect {
      Some(Meeting(12, 16))
    }
  } // PROBLEM: ref transparency, signature lies (there is a lot going on we don't know about), what about errors?

  println("STEP 1")

  // STEP 1: handle errors (different ones-not like parsing!)
  {
    import ApiCallFunctions.RandomErrors._

    def schedule(person1: String, person2: String, lengthHours: Int): Option[Meeting] = {
      try {
        val person1Entries = calendarEntriesApiCall(person1)
        val person2Entries = calendarEntriesApiCall(person2)

        val slots             = List.range(8, 16 - lengthHours + 1).map(startHour => Meeting(startHour, startHour + lengthHours))
        val scheduledMeetings = person1Entries.appendedAll(person2Entries)
        val possibleMeetings =
          slots.filter(slot => scheduledMeetings.forall(meeting => !meetingsOverlap(meeting, slot)))

        if (possibleMeetings.nonEmpty) {
          createMeetingApiCall(List(person1, person2), possibleMeetings.head)
          Some(possibleMeetings.head)
        } else None
      } catch {
        case _: Exception => None
      }
    }

    check { schedule("Alice", "Bob", 1) }.expect { r => r.forall(_ == Meeting(10, 11)) }
    check { schedule("Alice", "Bob", 2) }.expect { r => r.forall(_ == Meeting(12, 14)) }
    check { schedule("Alice", "Bob", 3) }.expect { r => r.forall(_ == Meeting(12, 15)) }
    check { schedule("Alice", "Bob", 4) }.expect { r => r.forall(_ == Meeting(12, 16)) }
  }

  def possibleMeetings(
      scheduledMeetings: List[Meeting],
      startHour: Int,
      endHour: Int,
      lengthHours: Int
  ): List[Meeting] = {
    val slots =
      List.range(startHour, endHour - lengthHours + 1).map(startHour => Meeting(startHour, startHour + lengthHours))
    slots.filter(slot => scheduledMeetings.forall(meeting => !meetingsOverlap(meeting, slot)))
  }

  println("STEP LAST-1")

  // LAST-1 STEP:
  {
    import ApiCallFunctions.AlwaysSucceeding._

    def calendarEntries(name: String): IO[List[Meeting]] = {
      IO.delay(calendarEntriesApiCall(name))
    }

    def createMeeting(names: List[String], meeting: Meeting): IO[Unit] = {
      IO.delay(createMeetingApiCall(names, meeting))
    }

    def schedule(person1: String, person2: String, lengthHours: Int): IO[Option[Meeting]] = {
      for {
        person1Entries    <- calendarEntries(person1)
        person2Entries    <- calendarEntries(person2)
        scheduledMeetings = person1Entries.appendedAll(person2Entries)
        possibleMeeting   = possibleMeetings(scheduledMeetings, 8, 16, lengthHours).headOption
        _ <- possibleMeeting match {
              case Some(meeting) => createMeeting(List(person1, person2), meeting)
              case None          => IO.unit
            }
      } yield possibleMeeting
    }

    check { schedule("Alice", "Bob", 1).unsafeRunSync() }.expect {
      Some(Meeting(10, 11))
    }
    check { schedule("Alice", "Bob", 2).unsafeRunSync() }.expect {
      Some(Meeting(12, 14))
    }
    check { schedule("Alice", "Bob", 3).unsafeRunSync() }.expect {
      Some(Meeting(12, 15))
    }
    check { schedule("Alice", "Bob", 4).unsafeRunSync() }.expect {
      Some(Meeting(12, 16))
    }
  }

  println("STEP LAST")

  // LAST STEP: handle errors (different ones-not like parsing!)
  {
    import ApiCallFunctions.RandomErrors._

    def calendarEntries(name: String): IO[List[Meeting]] = {
      IO.delay(calendarEntriesApiCall(name))
    }

    def createMeeting(names: List[String], meeting: Meeting): IO[Unit] = {
      IO.delay(createMeetingApiCall(names, meeting))
    }

    def retry[A](action: IO[A], maxRetries: Int): IO[A] = {
      List.range(0, maxRetries).foldLeft(action)((program, _) => program.handleErrorWith(_ => action))
    }

    def schedule(person1: String, person2: String, lengthHours: Int): IO[Option[Meeting]] = {
      for {
        person1Entries    <- retry(calendarEntries(person1), 10)
        person2Entries    <- retry(calendarEntries(person2), 10)
        scheduledMeetings = person1Entries.appendedAll(person2Entries)
        possibleMeeting   = possibleMeetings(scheduledMeetings, 8, 16, lengthHours).headOption
        _ <- possibleMeeting match {
              case Some(meeting) => retry(createMeeting(List(person1, person2), meeting), 10)
              case None          => IO.unit
            }
      } yield possibleMeeting
    }

    // we don't expect any Nones here (compare to the non FP version)
    check { schedule("Alice", "Bob", 1).unsafeRunSync() }.expect {
      Some(Meeting(10, 11))
    }
    check { schedule("Alice", "Bob", 2).unsafeRunSync() }.expect {
      Some(Meeting(12, 14))
    }
    check { schedule("Alice", "Bob", 3).unsafeRunSync() }.expect {
      Some(Meeting(12, 15))
    }
    check { schedule("Alice", "Bob", 4).unsafeRunSync() }.expect {
      Some(Meeting(12, 16))
    }
  }

  println("STRETCH")

  // STRETCH GOALS: any number of people attending
  {
    import ApiCallFunctions.RandomErrors._

    def calendarEntries(name: String): IO[List[Meeting]] = {
      IO.delay(calendarEntriesApiCall(name))
    }

    def createMeeting(names: List[String], meeting: Meeting): IO[Unit] = {
      IO.delay(createMeetingApiCall(names, meeting))
    }

    def retry[A](action: IO[A], maxRetries: Int): IO[A] = {
      List.range(0, maxRetries).foldLeft(action)((program, _) => program.handleErrorWith(_ => action))
    }

    def scheduledMeetings(attendees: List[String]): IO[List[Meeting]] = {
      attendees.flatTraverse(attendee => retry(calendarEntries(attendee), 10))
    }

    def schedule(attendees: List[String], lengthHours: Int): IO[Option[Meeting]] = {
      for {
        scheduledMeetings <- scheduledMeetings(attendees)
        _                 = println(scheduledMeetings)
        possibleMeeting   = possibleMeetings(scheduledMeetings, 8, 16, lengthHours).headOption
        _ <- possibleMeeting match {
              case Some(meeting) => retry(createMeeting(attendees, meeting), 10)
              case None          => IO.unit
            }
      } yield possibleMeeting
    }

    check { schedule(List("Alice", "Bob"), 1).unsafeRunSync() }.expect {
      Some(Meeting(10, 11))
    }
    check { schedule(List("Alice", "Bob"), 2).unsafeRunSync() }.expect {
      Some(Meeting(12, 14))
    }
    check { schedule(List("Alice", "Bob"), 3).unsafeRunSync() }.expect {
      Some(Meeting(12, 15))
    }
    check { schedule(List("Alice", "Bob"), 4).unsafeRunSync() }.expect {
      Some(Meeting(12, 16))
    }
    check { schedule(List("Alice", "Bob", "Charlie"), 1).unsafeRunSync() }.expect { r =>
      r.forall(m => m.endHour - m.startHour == 1)
    }
  }
}
