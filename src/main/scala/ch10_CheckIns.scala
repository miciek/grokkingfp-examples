import cats.effect.{IO, Ref}
import cats.implicits._
import cats.effect.unsafe.implicits.global
import fs2.Stream

import java.util.concurrent.TimeUnit
import scala.concurrent.duration._

object ch10_CheckIns {

  /** PREREQUISITE: model
    */
  object model:
    opaque type City = String
    object City:
      def apply(name: String): City           = name
      extension (city: City) def name: String = city
    case class CityStats(city: City, checkIns: Int)
  import model._

  /** PREREQUISITE: a stream of user check-ins
    */
  val checkIns: Stream[IO, City] =
    Stream(City("Sydney"), City("Dublin"), City("Cape Town"), City("Lima"), City("Singapore"))
      .repeatN(100_000)
      .append(Stream.range(0, 100_000).map(i => City(s"City $i")))
      .append(Stream(City("Sydney"), City("Sydney"), City("Lima")))
      .covary[IO]

  private def showCheckIns = {
    check.withoutPrinting(checkIns.map(_.name).compile.toList.unsafeRunSync()).expectThat { allCheckIns =>
      allCheckIns.size == 600_003 && allCheckIns.count(_ == "Sydney") == 100_002 && allCheckIns
        .count(_ == "Lima") == 100_001 && allCheckIns.count(_ == "Cape Town") == 100_000 && allCheckIns
        .count(_ == "City 27") == 1
    }
  }

  /** STEP 1: sequential & few ranking updates
    * (or more ranking updates, but slow)
    */
  def topCities(cityCheckIns: Map[City, Int]): List[CityStats] = {
    cityCheckIns.toList
      .map {
        case (city, checkIns) => CityStats(city, checkIns)
      }
      .sortBy(_.checkIns)
      .reverse
      .take(3)
  }

  private def step1 = { // Coffee Break: Many things in a single thread
    val checkInsSmall: Stream[IO, City] = Stream(
      City("Sydney"),
      City("Sydney"),
      City("Cape Town"),
      City("Singapore"),
      City("Cape Town"),
      City("Sydney")
    ).covary[IO]

    def processCheckInsRaw(checkIns: Stream[IO, City]): IO[Unit] = {
      checkIns
        .scan(Map.empty[City, Int])((cityCheckIns, city) => {
          val newCheckIns = cityCheckIns.get(city) match {
            case None           => 1
            case Some(checkIns) => checkIns + 1
          }
          cityCheckIns.updated(city, newCheckIns)
        })
        .map(topCities)
        .foreach(ranking => IO.delay(println(ranking)))
        .compile
        .drain
    }

    check.timed(processCheckInsRaw(checkInsSmall).unsafeRunSync())

    def processCheckIns(checkIns: Stream[IO, City]): IO[Unit] = {
      checkIns
        .scan(Map.empty[City, Int])((cityCheckIns, city) =>
          cityCheckIns.updatedWith(city)(_.map(_ + 1).orElse(Some(1)))
        )                    // introduce updatedWith
        .map(topCities)
        .foreach(IO.println) // introduce IO.println
        .compile
        .drain
    }

    check.timed(processCheckIns(checkInsSmall).unsafeRunSync())
    // check.timed(processCheckIns(checkIns).unsafeRunSync()) // a long, long time...
  }

  object Version1 {
    def processCheckIns(checkIns: Stream[IO, City]): IO[Unit] = {
      checkIns
        .scan(Map.empty[City, Int])((cityCheckIns, city) =>
          cityCheckIns.updatedWith(city)(_.map(_ + 1).orElse(Some(1)))
        )
        .chunkN(100_000) // smaller chunks = longer processing time
        .map(_.last)
        .unNone
        .map(topCities)
        .foreach(IO.println)
        .compile
        .drain
    }
  }

  private def runVersion1 = {
    check
      .timed {
        Version1.processCheckIns(checkIns).unsafeRunSync()
      }
  }

  // PROBLEMS: the current version is updated only every 100k elements (if you make it lower, it takes a lot longer)

  /** STEP 2: concurrent & up-to-date (real time, no batching)
    */
  private def step2 = { // Ref intro
    { // update intro
      // we don't know how to create and use Refs
      // so let's use unsafeRunSync, but note it's not a proper usage, just for demonstration purpose
      val ref = Ref.of[IO, Int](0).unsafeRunSync()

      // run concurrently
      ref.update(_ + 1).unsafeToFuture() // again, we use Future to run it concurrently
      ref.update(_ + 2).unsafeToFuture() // because we don't know fibers yet
      Thread.sleep(100)

      check(ref.get.unsafeRunSync()).expect(3)
    }

    val example: IO[Int] = for {
      counter <- Ref.of[IO, Int](0)
      _       <- counter.update(_ + 1)
      _       <- counter.update(_ + 2)
      result  <- counter.get
    } yield result

    check(example.unsafeRunSync()).expect(3)
  }

  private def parSequenceIntro = {
    val exampleSequential: IO[Int] = for {
      counter <- Ref.of[IO, Int](0)
      _       <- List(counter.update(_ + 2), counter.update(_ + 3), counter.update(_ + 4)).sequence
      result  <- counter.get
    } yield result

    check.timed(exampleSequential.unsafeRunSync()).expect(9)

    val exampleConcurrent: IO[Int] = for {
      counter <- Ref.of[IO, Int](0)
      _       <- List(counter.update(_ + 2), counter.update(_ + 3), counter.update(_ + 4)).parSequence
      result  <- counter.get
    } yield result
    check.timed(exampleConcurrent.unsafeRunSync()).expect(9)
  }

  private def parSequenceWithSleepingIntro = { // parSequence with sleeping intro
    val exampleSequential: IO[Int] = for {
      counter <- Ref.of[IO, Int](0)
      program1 = counter.update(_ + 2)
      program2 = IO.sleep(FiniteDuration(1, TimeUnit.SECONDS)).flatMap(_ => counter.update(_ + 3)) // introduce IO.sleep
      program3 = IO.sleep(FiniteDuration(1, TimeUnit.SECONDS)).flatMap(_ => counter.update(_ + 4))
      _       <- List(program1, program2, program3).sequence
      result  <- counter.get
    } yield result

    println("The following will run for around 2 seconds")
    check.timed(exampleSequential.unsafeRunSync()).expect(9)

    val exampleConcurrent: IO[Int] = for {
      counter <- Ref.of[IO, Int](0)
      program1 = counter.update(_ + 2)
      program2 = IO.sleep(FiniteDuration(1, TimeUnit.SECONDS)).flatMap(_ => counter.update(_ + 3))
      program3 = IO.sleep(FiniteDuration(1, TimeUnit.SECONDS)).flatMap(_ => counter.update(_ + 4))
      _       <- List(program1, program2, program3).parSequence
      result  <- counter.get
    } yield result

    println("The following will run for around 1 second")
    check.timed(exampleConcurrent.unsafeRunSync()).expect(9)
  }

  /** See [[ch10_CastingDieConcurrently]] for parSequence exercises
    */
  // final version
  def storeCheckIn(storedCheckIns: Ref[IO, Map[City, Int]])(city: City): IO[Unit] = {
    storedCheckIns.update(_.updatedWith(city)(_ match { // introduce updatedWith
      case None           => Some(1)
      case Some(checkIns) => Some(checkIns + 1)
    }))
  }

  def differentUpdateRankingApproaches: Unit = {
    def updateRankingRecursion(
        storedCheckIns: Ref[IO, Map[City, Int]],
        storedRanking: Ref[IO, List[CityStats]]
    ): IO[Unit] = {
      for {
        newRanking <- storedCheckIns.get.map(topCities)
        _          <- storedRanking.set(newRanking)
        _          <- updateRankingRecursion(storedCheckIns, storedRanking)
      } yield ()
    }

    def updateRankingRecursionNothing(
        storedCheckIns: Ref[IO, Map[City, Int]],
        storedRanking: Ref[IO, List[CityStats]]
    ): IO[Nothing] = { // TODO: introduce Nothing
      for {
        newRanking <- storedCheckIns.get.map(topCities)
        _          <- storedRanking.set(newRanking)
        nothing    <- updateRankingRecursionNothing(storedCheckIns, storedRanking)
      } yield nothing
    }

    def updateRankingForeverM(
        storedCheckIns: Ref[IO, Map[City, Int]],
        storedRanking: Ref[IO, List[CityStats]]
    ): IO[Nothing] = { // TODO: introduce foreverM
      (for {
        newRanking <- storedCheckIns.get.map(topCities)
        _          <- storedRanking.set(newRanking)
      } yield ()).foreverM
    }
  }

  // final version
  def updateRanking(
      storedCheckIns: Ref[IO, Map[City, Int]],
      storedRanking: Ref[IO, List[CityStats]]
  ): IO[Nothing] = {
    storedCheckIns.get
      .map(topCities)
      .flatMap(storedRanking.set)
      .foreverM
  }

  object Version2 {
    // before Coffee Break:
    def processCheckInsNoOutput(checkIns: Stream[IO, City]): IO[Unit] = {
      for {
        storedCheckIns <- Ref.of[IO, Map[City, Int]](Map.empty)
        storedRanking  <- Ref.of[IO, List[CityStats]](List.empty)
        rankingProgram  = updateRanking(storedCheckIns, storedRanking)
        checkInsProgram = checkIns.evalMap(storeCheckIn(storedCheckIns)).compile.drain
        _              <- List(rankingProgram, checkInsProgram).parSequence
      } yield ()
    }

    // Coffee Break: Concurrent programs
    def processCheckIns(checkIns: Stream[IO, City]): IO[Unit] = {
      for {
        storedCheckIns <- Ref.of[IO, Map[City, Int]](Map.empty)
        storedRanking  <- Ref.of[IO, List[CityStats]](List.empty)
        rankingProgram  = updateRanking(storedCheckIns, storedRanking)
        checkInsProgram = checkIns.evalMap(storeCheckIn(storedCheckIns)).compile.drain
        outputProgram   = IO.sleep(1.second).flatMap(_ => storedRanking.get).flatMap(IO.println).foreverM
        _              <- List(rankingProgram, checkInsProgram, outputProgram).parSequence
      } yield ()
    }
  }

  private def runVersion2 = {
    println("The following should print ranking every 1 second")
    // check(Version2.processCheckIns(checkIns).unsafeRunSync()) // won't finish because it's an infinite program
    check(Version2.processCheckIns(checkIns).unsafeRunTimed(3.seconds)) // run for max 3 seconds
  }

  // PROBLEM: our program doesn't return so we need to decide the way we want to consume rankings (here, println every 1 second)

  /** STEP 3: concurrent & up-to-date, return immediately and pass the controls to the caller
    */
  case class ProcessingCheckIns(currentRanking: IO[List[CityStats]], stop: IO[Unit])

  object Version3 {
    def processCheckIns(checkIns: Stream[IO, City]): IO[ProcessingCheckIns] = {
      for {
        storedCheckIns <- Ref.of[IO, Map[City, Int]](Map.empty)
        storedRanking  <- Ref.of[IO, List[CityStats]](List.empty)
        rankingProgram  = updateRanking(storedCheckIns, storedRanking)
        checkInsProgram = checkIns.evalMap(storeCheckIn(storedCheckIns)).compile.drain
        fiber          <- List(rankingProgram, checkInsProgram).parSequence.start
      } yield ProcessingCheckIns(storedRanking.get, fiber.cancel)
    }
  }

  private def runVersion3 = {
    println("The following should print two rankings")
    check
      .executedIO {
        for {
          processing <- Version3.processCheckIns(checkIns)
          ranking    <- processing.currentRanking
          _          <- IO.println(ranking)
          _          <- IO.sleep(1.second)
          newRanking <- processing.currentRanking
          _          <- processing.stop
        } yield newRanking
      }
      .expectThat(_.size == 3)
  }

  // Quick quiz: fibers
  // What will this program do? How long will it run?
  private def quiz = {
    check.executedIO(for {
      fiber <- IO.sleep(300.millis).flatMap(_ => IO.println("hello")).foreverM.start
      _     <- IO.sleep(1.second)
      _     <- fiber.cancel
      _     <- IO.sleep(1.second)
    } yield ())
  }

  def main(args: Array[String]): Unit = {
    showCheckIns
    step1
    runVersion1
    step2
    parSequenceIntro
    parSequenceWithSleepingIntro
    runVersion2
    runVersion3
    quiz
  }
}
