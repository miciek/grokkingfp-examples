import cats.effect.{Deferred, IO, Ref}
import cats.implicits._
import cats.effect.unsafe.implicits.global
import fs2.Stream

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration

object ch10_CheckIns extends App {

  /**
    * PREREQUISITE: model
    */
  case class City(name: String) extends AnyVal
  case class CityStats(city: City, checkIns: Int)

  /**
    * PREREQUISITE: a stream of user check-ins
    */
  val checkIns: Stream[IO, City] =
    Stream(City("Sydney"), City("Dublin"), City("Cape Town"), City("Lima"), City("Singapore"))
      .repeatN(100_000)
      .append(Stream.range(0, 100_000).map(i => City(s"City $i")))
      .append(Stream(City("Sydney"), City("Sydney"), City("Lima")))
      .covary[IO]

  check.withoutPrinting(checkIns.map(_.name).compile.toList.unsafeRunSync()).expect { allCheckIns =>
    allCheckIns.size == 600_003 && allCheckIns.count(_ == "Sydney") == 100_002 && allCheckIns
      .count(_ == "Lima") == 100_001 && allCheckIns.count(_ == "Cape Town") == 100_000 && allCheckIns
      .count(_ == "City 27") == 1
  }

  /**
    * STEP 1: sequential & few ranking updates
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

  { // Coffee Break: Many things in a single thread
    val checkInsSmall: Stream[IO, City] =
      Stream(
        City("Sydney"),
        City("Sydney"),
        City("Cape Town"),
        City("Singapore"),
        City("Cape Town"),
        City("Sydney")
      ).covary[IO]

    def processCheckIns(checkIns: Stream[IO, City]): IO[Unit] = {
      checkIns
        .scan(Map.empty[City, Int])((cityCheckIns, city) =>
          cityCheckIns.updatedWith(city)(_.map(_ + 1).orElse(Some(1)))
        ) // introduce updated
        .map(topCities)
        .foreach(ranking => IO.delay(println(ranking))) // introduce IO.println
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

  check
    .timed {
      Version1.processCheckIns(checkIns).unsafeRunSync()
    }

  // PROBLEMS: the current version is updated only every 100k elements (if you make it lower, it takes a lot longer)

  /**
    * STEP 2: concurrent & up-to-date, no chunking
    */
  def storeCheckIn(storedCheckIns: Ref[IO, Map[City, Int]])(city: City): IO[Unit] = {
    storedCheckIns.update(_.updatedWith(city)(_ match { // introduce updatedWith
      case None           => Some(1)
      case Some(checkIns) => Some(checkIns + 1)
    }))
  }

  { // Ref intro
    val example: IO[Int] = for {
      counter <- Ref.of[IO, Int](0)
      _       <- counter.update(_ + 3)
      result  <- counter.get
    } yield result

    check(example.unsafeRunSync()).expect(3)

    def processCheckIns(checkIns: Stream[IO, City]): IO[Map[City, Int]] = {
      for {
        storedCheckIns <- Ref.of[IO, Map[City, Int]](Map.empty)
        _              <- checkIns.evalMap(storeCheckIn(storedCheckIns)).compile.drain // introduce evalMap
        checkIns       <- storedCheckIns.get
      } yield checkIns
    }

    check.withoutPrinting(processCheckIns(checkIns).unsafeRunSync()).expect(_.get(City("Sydney")).contains(100_002))
  }

  /**
    * See [[ch10_CastingDieConcurrently]] for fiber exercises
    */
  { // Fiber intro
    val example: IO[Int] = for {
      counter <- Ref.of[IO, Int](0)
      fiber1  <- counter.update(_ + 2).start
      fiber2  <- IO.sleep(FiniteDuration(1, TimeUnit.SECONDS)).flatMap(_ => counter.update(_ + 3)).start
      fiber3  <- counter.update(_ + 4).start
      _       <- fiber1.join.flatMap(_ => fiber2.join).flatMap(_ => fiber3.join) // this is needed to get 9!
      result  <- counter.get
    } yield result

    check(example.unsafeRunSync()).expect(9)
  }

  case class ProcessingCheckIns(currentRanking: IO[List[CityStats]], stop: IO[Unit])

  object Version2 {
    def updateRanking(
        storedCheckIns: Ref[IO, Map[City, Int]],
        storedRanking: Ref[IO, List[CityStats]]
    ): IO[Unit] = { // TODO: introduce Nothing
      for {
        newRanking <- storedCheckIns.get.map(topCities)
        _          <- storedRanking.set(newRanking)
        _          <- updateRanking(storedCheckIns, storedRanking)
      } yield ()
    }

    // Coffee Break
    def processCheckIns(checkIns: Stream[IO, City]): IO[ProcessingCheckIns] = {
      for {
        storedCheckIns <- Ref.of[IO, Map[City, Int]](Map.empty)
        storedRanking  <- Ref.of[IO, List[CityStats]](List.empty)
        rankingFiber   <- updateRanking(storedCheckIns, storedRanking).start
        checkInsFiber  <- checkIns.evalMap(storeCheckIn(storedCheckIns)).compile.drain.start
      } yield ProcessingCheckIns(storedRanking.get, rankingFiber.cancel.flatMap(_ => checkInsFiber.cancel))
    }
  }

  check
    .timed {
      (for {
        processing <- Version2.processCheckIns(checkIns)
        _          <- IO.sleep(FiniteDuration(1, TimeUnit.SECONDS))
        ranking    <- processing.currentRanking
        _          <- processing.stop
      } yield ranking).unsafeRunSync()
    }
    .expect(_.size == 3)

  // PROBLEM: ranking fiber spins a lot unnecessarily waiting for a first check-in

  /**
    * STEP 3: concurrent, up-to-date, asynchronous waiting for a possibly long prerequisite
    */
  object Version3 {

    /**
      * PREREQUISITE: API call to fetch the current RankingConfig
      * (potentially a long one)
      */
    case class RankingConfig(minCheckIns: Int, topN: Int)

    def fetchRankingConfig(): IO[RankingConfig] = {
      IO.sleep(FiniteDuration(100, TimeUnit.MILLISECONDS)).map(_ => RankingConfig(100_001, 2))
    }

    // new version:
    def topCities(config: RankingConfig)(cityCheckIns: Map[City, Int]): List[CityStats] = {
      cityCheckIns.toList
        .map {
          case (city, checkIns) => CityStats(city, checkIns)
        }
        .filter(_.checkIns >= config.minCheckIns)
        .sortBy(_.checkIns)
        .reverse
        .take(config.topN)
    }

    // new version:
    def updateRanking(
        storedCheckIns: Ref[IO, Map[City, Int]],
        storedRanking: Ref[IO, List[CityStats]],
        fetchedConfig: Deferred[IO, RankingConfig]
    ): IO[Nothing] = {
      (for {
        config     <- fetchedConfig.get
        newRanking <- storedCheckIns.get.map(topCities(config))
        _          <- storedRanking.set(newRanking)
      } yield ()).foreverM // introduce foreverM
    }

    def processCheckIns(checkIns: Stream[IO, City]): IO[ProcessingCheckIns] = {
      for {
        storedCheckIns   <- Ref.of[IO, Map[City, Int]](Map.empty)
        storedRanking    <- Ref.of[IO, List[CityStats]](List.empty)
        fetchedConfig    <- Deferred[IO, RankingConfig]
        fetchConfigFiber <- fetchRankingConfig().flatMap(fetchedConfig.complete).start
        rankingFiber     <- updateRanking(storedCheckIns, storedRanking, fetchedConfig).start
        checkInsFiber    <- checkIns.evalMap(storeCheckIn(storedCheckIns)).compile.drain.start
      } yield ProcessingCheckIns(
        storedRanking.get,
        List(fetchConfigFiber, rankingFiber, checkInsFiber).traverse_(_.cancel)
      )
    }
  }

  check
    .timed {
      (for {
        processing <- Version3.processCheckIns(checkIns.repeat) // note repeat (infinite!)
        _          <- IO.sleep(FiniteDuration(1, TimeUnit.SECONDS))
        ranking    <- processing.currentRanking
        _          <- processing.stop
      } yield ranking).unsafeRunSync()
    }
    .expect(_.size == 2)
}
