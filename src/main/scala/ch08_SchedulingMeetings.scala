// note that we need two imports (see build.sbt for details)
import cats.effect.IO
import cats.implicits._

object ch08_SchedulingMeetings extends App {

  /**
    * PREREQUISITE 1: MeetingTime model
    *
    * We use MeetingTime defined in Java: [[ch08_SchedulingMeetingsImpure.MeetingTime]]
    * We add apply method to be able to use it exactly like we would use a case class in Scala:
    * case class MeetingTime(start: Int, end: Int)
    */
  import ch08_SchedulingMeetingsImpure.MeetingTime
  object MeetingTime {
    def apply(start: Int, end: Int): MeetingTime = new MeetingTime(start, end)
    // we can now write MeetingTime(6, 10) instead of new MeetingTime(6, 10)
  }

  /**
    * PREREQUISITE 2: Impure, unsafe and side-effectful API calls
    * See [[ch08_SchedulingMeetingsImpure.calendarEntriesApiCall()]]
    * and [[ch08_SchedulingMeetingsImpure.createMeetingApiCall()]]
    *
    * We wrap them here to be able to use Scala immutable collections.
    */
  def calendarEntriesApiCall(name: String): List[MeetingTime] = {
    import scala.jdk.CollectionConverters._
    ch08_SchedulingMeetingsImpure.calendarEntriesApiCall(name).asScala.toList
  }

  def createMeetingApiCall(names: List[String], meetingTime: MeetingTime): Unit = {
    import scala.jdk.CollectionConverters._
    ch08_SchedulingMeetingsImpure.createMeetingApiCall(names.asJava, meetingTime)
  }

  // STEP 0: imperative implementation of the happy path (assuming no failures)
  {
    import ch08_SchedulingMeetingsImpure.scheduleNoFailures

    check { scheduleNoFailures("Alice", "Bob", 1) }.expect {
      MeetingTime(10, 11)
    }
    check { scheduleNoFailures("Alice", "Bob", 2) }.expect {
      MeetingTime(12, 14)
    }
    check { scheduleNoFailures("Alice", "Bob", 3) }.expect {
      MeetingTime(12, 15)
    }
    check { scheduleNoFailures("Alice", "Bob", 4) }.expect {
      MeetingTime(12, 16)
    }
    check { scheduleNoFailures("Alice", "Bob", 5) }.expect {
      null.asInstanceOf[MeetingTime]
    }
    check { scheduleNoFailures("Alice", "Charlie", 2) }.expect { _ =>
      true // it's random so it may be null or a random MeetingTime
    }
  } // PROBLEMS: multiple responsibilities, no failure handling, signature lies

  /**
    * STEP 1: Introduce IO to disentangle concerns
    * See [[ch08_CastingDie]] first
    */
  def calendarEntries(name: String): IO[List[MeetingTime]] = {
    IO.delay(calendarEntriesApiCall(name))
  }

  def createMeeting(names: List[String], meeting: MeetingTime): IO[Unit] = {
    IO.delay(createMeetingApiCall(names, meeting))
  }

  def scheduledMeetings(person1: String, person2: String): IO[List[MeetingTime]] = {
    for {
      person1Entries <- calendarEntries(person1)
      person2Entries <- calendarEntries(person2)
    } yield person1Entries.appendedAll(person2Entries)
  }

  check.potentiallyFailing { scheduledMeetings("Alice", "Bob").unsafeRunSync() }.expect {
    List(MeetingTime(8, 10), MeetingTime(11, 12), MeetingTime(9, 10))
  }

  val scheduledMeetingsProgram = scheduledMeetings("Alice", "Bob")
  check.potentiallyFailing(scheduledMeetingsProgram).expect(_.isInstanceOf[IO[List[MeetingTime]]])

  check.potentiallyFailing { scheduledMeetingsProgram.unsafeRunSync() }.expect {
    List(MeetingTime(8, 10), MeetingTime(11, 12), MeetingTime(9, 10))
  }

  // Coffee Break: Working with values
  def meetingsOverlap(meeting1: MeetingTime, meeting2: MeetingTime): Boolean = {
    meeting1.endHour > meeting2.startHour && meeting2.endHour > meeting1.startHour
  }

  def possibleMeetings(
      existingMeetings: List[MeetingTime],
      startHour: Int,
      endHour: Int,
      lengthHours: Int
  ): List[MeetingTime] = {
    val slots =
      List.range(startHour, endHour - lengthHours + 1).map(startHour => MeetingTime(startHour, startHour + lengthHours))
    slots.filter(slot => existingMeetings.forall(meeting => !meetingsOverlap(meeting, slot)))
  }

  object Version1 {
    def schedule(person1: String, person2: String, lengthHours: Int): IO[Option[MeetingTime]] = {
      for {
        existingMeetings <- scheduledMeetings(person1, person2)
        meetings         = possibleMeetings(existingMeetings, 8, 16, lengthHours)
      } yield meetings.headOption
    }
  }

  {
    import Version1.schedule
    val program = schedule("Alice", "Bob", 1)
    check(program).expect(_.isInstanceOf[IO[Option[MeetingTime]]])
    check.potentiallyFailing { program.unsafeRunSync() }.expect {
      Some(MeetingTime(10, 11))
    }
  }
  // PROBLEM SOLVED: entangled concerns

  // PROBLEMS: no failure handling, signature lies
  {
    // handling possible failures imperatively:
    import ch08_SchedulingMeetingsImpure.schedule
    check.potentiallyFailing { schedule("Alice", "Bob", 1) }.expect {
      MeetingTime(10, 11)
    }
    check.potentiallyFailing { schedule("Alice", "Bob", 2) }.expect {
      MeetingTime(12, 14)
    }
    check.potentiallyFailing { schedule("Alice", "Bob", 3) }.expect {
      MeetingTime(12, 15)
    }
    check.potentiallyFailing { schedule("Alice", "Bob", 4) }.expect {
      MeetingTime(12, 16)
    }
    check.potentiallyFailing { schedule("Alice", "Bob", 5) }.expect {
      null.asInstanceOf[MeetingTime]
    }

    // when we execute IO, the program can still fail:
    val program = Version1.schedule("Alice", "Bob", 1)

    println(s"running program $program three times (it should throw an exception!)")
    try {
      program.unsafeRunSync()
      program.unsafeRunSync()
      program.unsafeRunSync()
    } catch {
      case e: Throwable => println(s"Exception thrown: ${e.getMessage}")
    }
  }

  // STEP 2: Introduce orElse
  {
    val year: IO[Int]   = IO.delay(996)
    val noYear: IO[Int] = IO.delay(throw new Exception("no year"))

    val program1 = year.orElse(IO.delay(2020))
    val program2 = noYear.orElse(IO.delay(2020))
    val program3 = year.orElse(IO.delay(throw new Exception("can't recover")))
    val program4 = noYear.orElse(IO.delay(throw new Exception("can't recover")))

    check(program1.unsafeRunSync()).expect(996)
    check(program2.unsafeRunSync()).expect(2020)
    check(program3.unsafeRunSync()).expect(996)

    try {
      program4.unsafeRunSync()
    } catch {
      case e: Throwable => assert(e.getMessage == "can't recover")
    }
  }

  // lazy evaluation
  {
    val program = IO.pure(2021).orElse(IO.delay(throw new Exception()))
    check(program).expect(_.isInstanceOf[IO[Int]])
    check(program.unsafeRunSync()).expect(2021)
  }

  // eager evaluation
  {
    try {
      val program = IO.pure(2021).orElse(IO.pure(throw new Exception()))
      check(program).expect(_.isInstanceOf[IO[Int]])
    } catch {
      case e: Throwable => assert(e.getMessage == null)
    }
  }

  // recovery strategies
  {
    calendarEntries("Alice").orElse(calendarEntries("Alice")).orElse(IO.pure(List.empty))

    // described behaviour: call the side-effectful IO action and return results if successful; if it fails—call it again:
    calendarEntries("Alice").orElse(calendarEntries("Alice"))

    // described behaviour: call the side-effectful IO action and return results if successful; if it fails, retry max two times;
    // if all retries fail, return an `IO` value that will always succeed and return an empty List:
    calendarEntries("Alice")
      .orElse(calendarEntries("Alice"))
      .orElse(calendarEntries("Alice"))
      .orElse(IO.pure(List.empty))
  }

  // different levels of handling potential failures
  {
    // first option:
    def calendarEntries(name: String): IO[List[MeetingTime]] = {
      IO.delay(calendarEntriesApiCall(name))
        .orElse(IO.delay(calendarEntriesApiCall(name)))
        .orElse(IO.pure(List.empty))
    }

    // second option:
    def scheduledMeetings(person1: String, person2: String): IO[List[MeetingTime]] = {
      for {
        person1Entries <- calendarEntries(person1)
                           .orElse(calendarEntries(person1))
                           .orElse(IO.pure(List.empty))
        person2Entries <- calendarEntries(person2)
                           .orElse(calendarEntries(person2))
                           .orElse(IO.pure(List.empty))
      } yield person1Entries.appendedAll(person2Entries)
    }

    // third option:
    def schedule(person1: String, person2: String, lengthHours: Int): IO[Option[MeetingTime]] = {
      for {
        existingMeetings <- scheduledMeetings(person1, person2)
                             .orElse(scheduledMeetings(person1, person2))
                             .orElse(IO.pure(List.empty))
        meetings = possibleMeetings(existingMeetings, 8, 16, lengthHours)
      } yield meetings.headOption
    }

    check(schedule _).expect(_.isInstanceOf[(String, String, Int) => IO[Option[MeetingTime]]])
  }

  object Version2 {
    // we chose the third option to handle failures on the top level:
    def schedule(person1: String, person2: String, lengthHours: Int): IO[Option[MeetingTime]] = {
      for {
        existingMeetings <- scheduledMeetings(person1, person2)
                             .orElse(scheduledMeetings(person1, person2))
                             .orElse(IO.pure(List.empty))
        meetings = possibleMeetings(existingMeetings, 8, 16, lengthHours)
      } yield meetings.headOption
    }
  }

  {
    import Version2.schedule

    val program = schedule("Alice", "Bob", 1)
    check(program).expect(_.isInstanceOf[IO[Option[MeetingTime]]])

    // we can execute it many times and it won't fail!
    program.unsafeRunSync()
    program.unsafeRunSync()
    program.unsafeRunSync()

    // note we can't assert on fixed results because
    // there may be different results since in this version of schedule
    // we sometime use List.empty fallback!
    check { schedule("Alice", "Bob", 1).unsafeRunSync() }.expect {
      _.forall(meeting => meeting.endHour - meeting.startHour == 1)
    }
    check { schedule("Alice", "Bob", 2).unsafeRunSync() }.expect {
      _.forall(meeting => meeting.endHour - meeting.startHour == 2)
    }
    check { schedule("Alice", "Bob", 3).unsafeRunSync() }.expect {
      _.forall(meeting => meeting.endHour - meeting.startHour == 3)
    }
    check { schedule("Alice", "Bob", 4).unsafeRunSync() }.expect {
      _.forall(meeting => meeting.endHour - meeting.startHour == 4)
    }
  }
  // PROBLEM SOLVED: no failure handling

  // PROBLEMS: signature lies

  // STEP 3: using signatures to indicate that function describes IO (+ functional core example)
  {
    import Version2.schedule

    def schedulingProgram(getName: IO[String], showMeeting: Option[MeetingTime] => IO[Unit]): IO[Unit] = {
      for {
        name1           <- getName
        name2           <- getName
        possibleMeeting <- schedule(name1, name2, 2)
        _               <- showMeeting(possibleMeeting)
      } yield ()
    }

    // note that schedulingProgram doesn't know anything about consoleGet/Print
    // we import then now:
    import ch08_SchedulingMeetingsImpure.consoleGet
    import ch08_SchedulingMeetingsImpure.consolePrint

    // and use them to "configure" schedulingProgram to use console as IO
    schedulingProgram(IO.delay(consoleGet()), meeting => IO.delay(consolePrint(meeting.toString)))
  }

  // Coffee Break: Using IO to store data
  object Version3 {
    def schedule(person1: String, person2: String, lengthHours: Int): IO[Option[MeetingTime]] = {
      for {
        existingMeetings <- scheduledMeetings(person1, person2)
                             .orElse(scheduledMeetings(person1, person2))
                             .orElse(IO.pure(List.empty))
        meetings        = possibleMeetings(existingMeetings, 8, 16, lengthHours)
        possibleMeeting = meetings.headOption
        _ <- possibleMeeting match {
              case Some(meeting) => createMeeting(List(person1, person2), meeting)
              case None          => IO.unit // same as IO.pure(())
            }
      } yield possibleMeeting
    }
  }

  {
    import Version3.schedule

    // note we can't assert on fixed results because
    // there may be different results since in this version of schedule
    // we sometime use List.empty fallback!

    check { schedule("Alice", "Bob", 1).unsafeRunSync() }.expect {
      _.forall(meeting => meeting.endHour - meeting.startHour == 1)
    }
    check { schedule("Alice", "Bob", 2).unsafeRunSync() }.expect {
      _.forall(meeting => meeting.endHour - meeting.startHour == 2)
    }
    check { schedule("Alice", "Bob", 3).unsafeRunSync() }.expect {
      _.forall(meeting => meeting.endHour - meeting.startHour == 3)
    }
    check { schedule("Alice", "Bob", 4).unsafeRunSync() }.expect {
      _.forall(meeting => meeting.endHour - meeting.startHour == 4)
    }
  }
  // PROBLEM SOLVED: signature lies

  // TODO: STEP 4: cache, sync vs async

  // STEP 5/Coffee Break: configurable number of retries
  def retry[A](action: IO[A], maxRetries: Int): IO[A] = {
    List.range(0, maxRetries).foldLeft(action)((program, _) => program.orElse(action))
  }

  {
    def schedule(person1: String, person2: String, lengthHours: Int): IO[Option[MeetingTime]] = {
      for {
        existingMeetings <- retry(scheduledMeetings(person1, person2), 10)
                             .orElse(IO.pure(List.empty))
        meetings        = possibleMeetings(existingMeetings, 8, 16, lengthHours)
        possibleMeeting = meetings.headOption
        _ <- possibleMeeting match {
              case Some(meeting) => retry(createMeeting(List(person1, person2), meeting), 10)
              case None          => IO.unit
            }
      } yield possibleMeeting
    }

    check { schedule("Alice", "Bob", 1).unsafeRunSync() }.expect {
      Some(MeetingTime(10, 11))
    }
    check { schedule("Alice", "Bob", 2).unsafeRunSync() }.expect {
      Some(MeetingTime(12, 14))
    }
    check { schedule("Alice", "Bob", 3).unsafeRunSync() }.expect {
      Some(MeetingTime(12, 15))
    }
    check { schedule("Alice", "Bob", 4).unsafeRunSync() }.expect {
      Some(MeetingTime(12, 16))
    }
  }

  // STEP 6: any number of people attending
  object Version6 {
    def scheduledMeetings(attendees: List[String]): IO[List[MeetingTime]] = {
      attendees.flatTraverse(attendee => retry(calendarEntries(attendee), 10))
    }

    // TODO:
    object FinalVersion { // presented at the beginning of the chapter (without failure handling on writes)
      def schedule(attendees: List[String], lengthHours: Int): IO[Option[MeetingTime]] = {
        for {
          existingMeetings <- scheduledMeetings(attendees)
          possibleMeeting  = possibleMeetings(existingMeetings, 8, 16, lengthHours).headOption
          _ <- possibleMeeting match {
                case Some(meeting) => createMeeting(attendees, meeting)
                case None          => IO.unit
              }
        } yield possibleMeeting
      }
    }

    def schedule(attendees: List[String], lengthHours: Int): IO[Option[MeetingTime]] = {
      for {
        existingMeetings <- scheduledMeetings(attendees)
        possibleMeeting  = possibleMeetings(existingMeetings, 8, 16, lengthHours).headOption
        _ <- possibleMeeting match {
              case Some(meeting) => retry(createMeeting(attendees, meeting), 10)
              case None          => IO.unit
            }
      } yield possibleMeeting
    }
  }

  {
    import Version6.schedule

    // note we can assert on fixed results here because
    // there is only a very very small chance we'll use a fallback
    // (because there is a 10-retry strategy in this version)

    check { schedule(List("Alice", "Bob"), 1).unsafeRunSync() }.expect {
      Some(MeetingTime(10, 11))
    }
    check { schedule(List("Alice", "Bob"), 2).unsafeRunSync() }.expect {
      Some(MeetingTime(12, 14))
    }
    check { schedule(List("Alice", "Bob"), 3).unsafeRunSync() }.expect {
      Some(MeetingTime(12, 15))
    }
    check { schedule(List("Alice", "Bob"), 4).unsafeRunSync() }.expect {
      Some(MeetingTime(12, 16))
    }
    check { schedule(List("Alice", "Bob", "Charlie"), 1).unsafeRunSync() }.expect { r =>
      r.forall(m => m.endHour - m.startHour == 1)
    }
  }
}
