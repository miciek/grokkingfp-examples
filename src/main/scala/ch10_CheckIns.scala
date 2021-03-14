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
    Stream(City("Tokyo"), City("London"), City("Moscow"), City("Lima"), City("São Paulo"))
      .repeatN(100_000)
      .append(Stream.range(0, 100_000).map(i => City(s"City $i")))
      .append(Stream(City("Tokyo"), City("Tokyo"), City("Lima")))
      .covary[IO]

  check.withoutPrinting(checkIns.map(_.name).compile.toList.unsafeRunSync()).expect { allCheckIns =>
    allCheckIns.size == 600_003 && allCheckIns.count(_ == "Tokyo") == 100_002 && allCheckIns
      .count(_ == "Lima") == 100_001 && allCheckIns.count(_ == "Moscow") == 100_000 && allCheckIns
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
  case class ProcessingCheckIns(currentRanking: IO[List[CityStats]], stop: IO[Unit])

  def storeCheckIn(storedCheckIns: Ref[IO, Map[City, Int]])(city: City): IO[Unit] = {
    storedCheckIns.update(_.updatedWith(city)(_ match {
      case None           => Some(1)
      case Some(checkIns) => Some(checkIns + 1)
    }))
  }

  object Version2 {
    def updateRanking(
        storedCities: Ref[IO, Map[City, Int]],
        storedRanking: Ref[IO, List[CityStats]]
    ): IO[Unit] = {
      for {
        newRanking <- storedCities.get.map(topCities)
        _          <- storedRanking.set(newRanking)
        _          <- updateRanking(storedCities, storedRanking)
      } yield ()
    }

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
        storedCities: Ref[IO, Map[City, Int]],
        storedRanking: Ref[IO, List[CityStats]],
        fetchedConfig: Deferred[IO, RankingConfig]
    ): IO[Unit] = {
      for {
        config     <- fetchedConfig.get
        newRanking <- storedCities.get.map(topCities(config))
        _          <- storedRanking.set(newRanking)
        _          <- updateRanking(storedCities, storedRanking, fetchedConfig)
      } yield ()
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
