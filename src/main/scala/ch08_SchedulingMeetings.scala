// note that we need two imports (see build.sbt for details)
import cats.effect.IO
import cats.implicits.*
import cats.effect.unsafe.implicits.global

import scala.util.Try

/** PREREQUISITE 1: MeetingTime model
  */
case class MeetingTime(startHour: Int, endHour: Int)

object ch08_SchedulingMeetings {

  /** PREREQUISITE 2: Impure, unsafe and side-effectful API calls
    * Defined in Java code to simulate an imperative client library we don't control.
    *
    * See [[ch08_SchedulingMeetingsAPI.calendarEntriesApiCall()]]
    * and [[ch08_SchedulingMeetingsAPI.createMeetingApiCall()]]
    *
    * We wrap them here to be able to use Scala immutable collections.
    * They randomly fail.
    */
  def calendarEntriesApiCall(name: String): List[MeetingTime] = {
    import scala.jdk.CollectionConverters._
    ch08_SchedulingMeetingsAPI.calendarEntriesApiCall(name).asScala.toList
  }

  def createMeetingApiCall(names: List[String], meetingTime: MeetingTime): Unit = {
    import scala.jdk.CollectionConverters._
    ch08_SchedulingMeetingsAPI.createMeetingApiCall(names.asJava, meetingTime)
  }

  /** STEP 0: imperative implementation of the happy-path: see [[ch08_SchedulingMeetingsImpure.scheduleNoFailures()]]
    */

  /** STEP 1: Introduce IO to disentangle concerns
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

  private def runStep1 = {
    // may randomly fail to return a value, but when it returns a value the assertion holds:
    // assert(scheduledMeetings("Alice", "Bob").unsafeRunSync() == List(
    //   MeetingTime(8, 10),
    //   MeetingTime(11, 12),
    //   MeetingTime(9, 10)
    // ))
    // we will be able to assert potentially failing IOs later in the chapter (see `runStep1Again` below)

    val scheduledMeetingsProgram = scheduledMeetings("Alice", "Bob")
    assert(scheduledMeetingsProgram.isInstanceOf[IO[List[MeetingTime]]])

    // may randomly fail to return a value, but when it returns a value, the assertion holds:
    // assert(scheduledMeetingsProgram.unsafeRunSync() == List(
    //   MeetingTime(8, 10),
    //   MeetingTime(11, 12),
    //   MeetingTime(9, 10)
    // ))
    // we will be able to assert potentially failing IOs later in the chapter (see `runStep1Again` below)
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
        meetings          = possibleMeetings(existingMeetings, 8, 16, lengthHours)
      } yield meetings.headOption
    }
  }

  private def runVersion1 = {
    import Version1.schedule
    val program = schedule("Alice", "Bob", 1)
    assert(program.isInstanceOf[IO[Option[MeetingTime]]])

    // may randomly fail to return a value, but when it returns a value, the assertion holds:
    // assert(program.unsafeRunSync() == Some(MeetingTime(10, 11)))
    // we will be able to assert potentially failing IOs later in the chapter (see `runep1Again` below)
  }
  // PROBLEM SOLVED: entangled concerns

  // PROBLEMS: no failure handling, signature lies
  private def imperativeErrorHandling = {

    /** Handling possible failures imperatively: see [[ch08_SchedulingMeetingsImpure.schedule]]
      */

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
  private def introduceOrElse = {
    val year: IO[Int]   = IO.delay(996)
    val noYear: IO[Int] = IO.delay(throw new Exception("no year"))

    val program1 = year.orElse(IO.delay(2020))
    val program2 = noYear.orElse(IO.delay(2020))
    val program3 = year.orElse(IO.delay(throw new Exception("can't recover")))
    val program4 = noYear.orElse(IO.delay(throw new Exception("can't recover")))

    assert(program1.unsafeRunSync() == 996)
    assert(program2.unsafeRunSync() == 2020)
    assert(program3.unsafeRunSync() == 996)

    try {
      program4.unsafeRunSync()
    } catch {
      case e: Throwable => assert(e.getMessage == "can't recover")
    }
  }

  private def lazyEvaluation = {
    val program = IO.delay(throw new Exception).orElse(IO.pure(2022))
    assert(program.isInstanceOf[IO[Int]])
    assert(program.unsafeRunSync() == 2022)
  }

  private def eagerEvaluation = {
    assert(Try {
      val program = IO.pure(throw new Exception).orElse(IO.pure(2022))
      assert(program.isInstanceOf[IO[Int]])
    }.isFailure)
  }

  private def recoveryStrategies = {
    calendarEntries("Alice").orElse(calendarEntries("Alice")).orElse(IO.pure(List.empty))

    // described behaviour: call the side-effectful IO action and return results if successful; if it fails—call it again:
    calendarEntries("Alice").orElse(calendarEntries("Alice"))

    calendarEntries("Alice").orElse(IO.pure(List.empty))

    // described behaviour: call the side-effectful IO action and return results if successful; if it fails, retry max two times;
    // if all retries fail, return an `IO` value that will always succeed and return an empty List:
    calendarEntries("Alice")
      .orElse(calendarEntries("Alice"))
      .orElse(calendarEntries("Alice"))
      .orElse(IO.pure(List.empty))
  }

  private def differentLevelsOfHandlingPotentialFailures = {
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
        meetings          = possibleMeetings(existingMeetings, 8, 16, lengthHours)
      } yield meetings.headOption
    }

    assert(schedule.isInstanceOf[(String, String, Int) => IO[Option[MeetingTime]]])
  }

  object Version2 {
    // we chose the third option to handle failures on the top level:
    def schedule(person1: String, person2: String, lengthHours: Int): IO[Option[MeetingTime]] = {
      for {
        existingMeetings <- scheduledMeetings(person1, person2)
                              .orElse(scheduledMeetings(person1, person2))
                              .orElse(IO.pure(List.empty))
        meetings          = possibleMeetings(existingMeetings, 8, 16, lengthHours)
      } yield meetings.headOption
    }
  }

  private def runVersion2 = {
    import Version2.schedule

    val program = schedule("Alice", "Bob", 1)
    assert(program.isInstanceOf[IO[Option[MeetingTime]]])

    // we can execute it many times and it won't fail!
    program.unsafeRunSync()
    program.unsafeRunSync()
    program.unsafeRunSync()

    // note we can't assert on fixed results because
    // there may be different results since in this version of schedule
    // we sometime use List.empty fallback!
    assert(schedule("Alice", "Bob", 1).unsafeRunSync().forall(meeting => meeting.endHour - meeting.startHour == 1))
    assert(schedule("Alice", "Bob", 2).unsafeRunSync().forall(meeting => meeting.endHour - meeting.startHour == 2))
    assert(schedule("Alice", "Bob", 3).unsafeRunSync().forall(meeting => meeting.endHour - meeting.startHour == 3))
    assert(schedule("Alice", "Bob", 4).unsafeRunSync().forall(meeting => meeting.endHour - meeting.startHour == 4))
  }
  // PROBLEM SOLVED: no failure handling

  // PROBLEMS: signature lies

  // STEP 3: using signatures to indicate that function describes IO (+ functional core example)

  // note that schedulingProgram doesn't know anything about consoleGet/Print
  // we import then now:
  def consolePrint(message: String): Unit = ch08_ConsoleInterface.consolePrint(message)
  def consoleGet(): String                = ch08_ConsoleInterface.consoleGet()

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
        meetings          = possibleMeetings(existingMeetings, 8, 16, lengthHours)
        possibleMeeting   = meetings.headOption
        _                <- possibleMeeting match {
                              case Some(meeting) => createMeeting(List(person1, person2), meeting)
                              case None          => IO.unit // same as IO.pure(())
                            }
      } yield possibleMeeting
    }
  }

  private def runVersion3 = {
    import Version3.schedule

    // note we can't assert on fixed results because
    // there may be different results since in this version of schedule
    // we sometime use List.empty fallback!

    assert(schedule("Alice", "Bob", 1).unsafeRunSync().forall(meeting => meeting.endHour - meeting.startHour == 1))
    assert(schedule("Alice", "Bob", 2).unsafeRunSync().forall(meeting => meeting.endHour - meeting.startHour == 2))
    assert(schedule("Alice", "Bob", 3).unsafeRunSync().forall(meeting => meeting.endHour - meeting.startHour == 3))
    assert(schedule("Alice", "Bob", 4).unsafeRunSync().forall(meeting => meeting.endHour - meeting.startHour == 4))
  }
  // PROBLEM SOLVED: signature lies

  // a non-implemented example of caching from a client perspective (see ch10 for more)
  trait Caching {
    def cachedCalendarEntries(name: String): IO[List[MeetingTime]] // fails if no value found
    def updateCachedEntries(name: String, newEntries: List[MeetingTime]): IO[Unit]

    def calendarEntriesWithCache(name: String): IO[List[MeetingTime]] = {
      val getEntriesAndUpdateCache: IO[List[MeetingTime]] = for {
        currentEntries <- calendarEntries(name)
        _              <- updateCachedEntries(name, currentEntries)
      } yield currentEntries

      cachedCalendarEntries(name).orElse(getEntriesAndUpdateCache)
    }
  }

  // STEP 4: configurable number of retries
  def retry[A](action: IO[A], maxRetries: Int): IO[A] = {
    List
      .range(0, maxRetries)
      .map(_ => action)
      .foldLeft(action)((program, retryAction) =>
        program.orElse(retryAction)
      )
  }

  private def runRetry = {
    var calls = 0
    retry(
      IO.delay {
        calls = calls + 1
        throw new Exception("failed")
      },
      10
    ).attempt.unsafeRunSync()
    assert(calls == 11)
  }

  object Version4 {
    def schedule(person1: String, person2: String, lengthHours: Int): IO[Option[MeetingTime]] = {
      for {
        existingMeetings <- retry(scheduledMeetings(person1, person2), 10)
                              .orElse(IO.pure(List.empty))
        meetings          = possibleMeetings(existingMeetings, 8, 16, lengthHours)
        possibleMeeting   = meetings.headOption
        _                <- possibleMeeting match {
                              case Some(meeting) => retry(createMeeting(List(person1, person2), meeting), 10)
                              case None          => IO.unit
                            }
      } yield possibleMeeting
    }
  }

  private def runVersion4 = {
    import Version4.schedule

    assert(schedule("Alice", "Bob", 1).unsafeRunSync() == Some(MeetingTime(10, 11)))
    assert(schedule("Alice", "Bob", 2).unsafeRunSync() == Some(MeetingTime(12, 14)))
    assert(schedule("Alice", "Bob", 3).unsafeRunSync() == Some(MeetingTime(12, 15)))
    assert(schedule("Alice", "Bob", 4).unsafeRunSync() == Some(MeetingTime(12, 16)))
  }

  // STEP 5: any number of people attending
  def scheduledMeetings(attendees: List[String]): IO[List[MeetingTime]] = {
    attendees
      .map(attendee => retry(calendarEntries(attendee), 10))
      .sequence
      .map(_.flatten)
  }

  private def runScheduledMeetings = {
    assert(scheduledMeetings(List("Alice", "Bob")).unsafeRunSync() == List(
      MeetingTime(8, 10),
      MeetingTime(11, 12),
      MeetingTime(9, 10)
    ))
    assert(scheduledMeetings(List("Alice", "Bob", "Charlie")).unsafeRunSync().size == 4)
    assert(scheduledMeetings(List.empty).unsafeRunSync() == List.empty)
  }

  object Version5 { // FINAL VERSION: also presented at the beginning of the chapter
    def schedule(attendees: List[String], lengthHours: Int): IO[Option[MeetingTime]] = {
      for {
        existingMeetings <- scheduledMeetings(attendees)
        possibleMeeting   = possibleMeetings(existingMeetings, 8, 16, lengthHours).headOption
        _                <- possibleMeeting match {
                              case Some(meeting) => createMeeting(attendees, meeting)
                              case None          => IO.unit
                            }
      } yield possibleMeeting
    }
  }

  private def runVersion5 = {
    import Version5.schedule

    // note we can assert on fixed results here because
    // there is only a very very small chance we'll use a fallback
    // (because there is a 10-retry strategy in this version)

    assert(schedule(List("Alice", "Bob"), 1).unsafeRunSync() == Some(MeetingTime(10, 11)))
    assert(schedule(List("Alice", "Bob"), 2).unsafeRunSync() == Some(MeetingTime(12, 14)))
    assert(schedule(List("Alice", "Bob"), 3).unsafeRunSync() == Some(MeetingTime(12, 15)))
    assert(schedule(List("Alice", "Bob"), 4).unsafeRunSync() == Some(MeetingTime(12, 16)))
    assert(schedule(List("Alice", "Bob", "Charlie"), 1).unsafeRunSync().forall(m => m.endHour - m.startHour == 1))
  }

  // BONUS: scheduledMeetings using foldLeft instead of sequence:
  private def bonus1 = {
    def scheduledMeetings(attendees: List[String]): IO[List[MeetingTime]] = {
      attendees
        .map(attendee => retry(calendarEntries(attendee), 10))
        .foldLeft(IO.pure(List.empty[MeetingTime]))((allMeetingsProgram, attendeeMeetingsProgram) => {
          for {
            allMeetings      <- allMeetingsProgram
            attendeeMeetings <- attendeeMeetingsProgram
          } yield allMeetings.appendedAll(attendeeMeetings)
        })
    }

    assert(scheduledMeetings(List("Alice", "Bob")).unsafeRunSync() == List(
      MeetingTime(8, 10),
      MeetingTime(11, 12),
      MeetingTime(9, 10)
    ))

    assert(scheduledMeetings(List("Alice", "Bob", "Charlie")).unsafeRunSync().size == 4)

    assert(scheduledMeetings(List.empty).unsafeRunSync() == List.empty)
  }

  // BONUS: scheduledMeetings using traverse
  private def bonus2 = {
    def scheduledMeetings(attendees: List[String]): IO[List[MeetingTime]] = {
      attendees
        .traverse(attendee => retry(calendarEntries(attendee), 10))
        .map(_.flatten)
    }

    assert(scheduledMeetings(List("Alice", "Bob")).unsafeRunSync() == List(
      MeetingTime(8, 10),
      MeetingTime(11, 12),
      MeetingTime(9, 10)
    ))

    assert(scheduledMeetings(List("Alice", "Bob", "Charlie")).unsafeRunSync().size == 4)

    assert(scheduledMeetings(List.empty).unsafeRunSync() == List.empty)
  }

  // BONUS: scheduledMeetings using flatTraverse
  private def bonus3 = {
    def scheduledMeetings(attendees: List[String]): IO[List[MeetingTime]] = {
      attendees.flatTraverse(attendee => retry(calendarEntries(attendee), 10))
    }

    assert(scheduledMeetings(List("Alice", "Bob")).unsafeRunSync() == List(
      MeetingTime(8, 10),
      MeetingTime(11, 12),
      MeetingTime(9, 10)
    ))

    assert(scheduledMeetings(List("Alice", "Bob", "Charlie")).unsafeRunSync().size == 4)

    assert(scheduledMeetings(List.empty).unsafeRunSync() == List.empty)
  }

  // BONUS: sequence works on other types as well! e.g. on List[Option]
  private def bonus4 = {
    val years: List[Option[Int]]      = List(Some(2019), None, Some(2021))
    val resultNone: Option[List[Int]] = years.sequence
    assert(resultNone == None)

    val goodYears: List[Option[Int]]  = List(Some(2019), Some(2021))
    val resultSome: Option[List[Int]] = goodYears.sequence
    assert(resultSome == Some(List(2019, 2021)))
  }

  private def runStep1Again = {
    // we now know the retry function so we can run proper assertions for our Step 1 functions
    assert(retry(scheduledMeetings("Alice", "Bob"), 100).unsafeRunSync() == List(
      MeetingTime(8, 10),
      MeetingTime(11, 12),
      MeetingTime(9, 10)
    )) // may randomly fail to return a value, but when it returns a value the assertion holds

    val scheduledMeetingsProgram = scheduledMeetings("Alice", "Bob")
    assert(scheduledMeetingsProgram.isInstanceOf[IO[List[MeetingTime]]])

    assert(retry(scheduledMeetingsProgram, 100).unsafeRunSync() == List(
      MeetingTime(8, 10),
      MeetingTime(11, 12),
      MeetingTime(9, 10)
    )) // may randomly fail to return a value, but when it returns a value, the assertion holds
  }

  private def runVersion1Again = {
    // we now know the retry function so we can run proper assertions for our Version 1 functions
    import Version1.schedule
    val program = schedule("Alice", "Bob", 1)
    assert(program.isInstanceOf[IO[Option[MeetingTime]]])

    // may randomly fail to return a value, but when it returns a value, the assertion holds:
    assert(retry(program, 100).unsafeRunSync() == Some(MeetingTime(10, 11)))
  }

  def main(args: Array[String]): Unit = {
    runStep1
    runVersion1
    imperativeErrorHandling
    introduceOrElse
    lazyEvaluation
    eagerEvaluation
    recoveryStrategies
    differentLevelsOfHandlingPotentialFailures
    runVersion2
    runVersion3
    runRetry
    runVersion4
    runScheduledMeetings
    runVersion5
    bonus1
    bonus2
    bonus3
    bonus4
    runStep1Again
    runVersion1Again
  }
}
