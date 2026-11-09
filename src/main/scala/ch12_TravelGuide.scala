import cats.effect.{IO, Resource}
import cats.implicits.*
import org.apache.jena.rdfconnection.{RDFConnection, RDFConnectionRemote}

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration

object ch12_TravelGuide {

  /** PREREQUISITES: data model and the guideScore function.  We don't import the travelGuide
    * function here because we want to develop a new version of it. However, its dependencies
    * stay the same and we import them and use them directly.
    *
    * Note that we mainly reuse what we've implemented so far. That's the power of designing
    * with small functions and immutable values that describe small functionalities.
    */
  import ch11_TravelGuide._
  import ch11_TravelGuide.model._, PopCultureSubject._
  import ch11_TravelGuide.AttractionOrdering._
  import ch11_WikidataDataAccess.getSparqlDataAccess

  /** STEPS 1-3: see ch12_TravelGuideTest.scala in the test directory.
    *
    * The tests showed a bug in the ch11 implementation (Int overflow).
    * Here is a fixed version that passes the tests.
    */
  def guideScore(guide: TravelGuide): Int = {
    val descriptionScore     = guide.attraction.description.map(_ => 30).getOrElse(0)
    val quantityScore        = Math.min(40, guide.subjects.size * 10)
    val totalFollowers: Long = guide.subjects
      .map(_ match {
        case Artist(_, followers) => followers.toLong
        case _                    => 0
      })
      .sum
    val totalBoxOffice: Long = guide.subjects
      .map(_ match {
        case Movie(_, boxOffice) => boxOffice.toLong
        case _                   => 0
      })
      .sum

    val followersScore = Math.min(15, totalFollowers / 100_000).toInt
    val boxOfficeScore = Math.min(15, totalBoxOffice / 10_000_000).toInt
    descriptionScore + quantityScore + followersScore + boxOfficeScore
  }

  // BONUS: if we changed the generators to Longs, we'd need to deal with Long overflows too:
  def guideScoreBigInt(guide: TravelGuide): Int = {
    val descriptionScore = guide.attraction.description.map(_ => 30).getOrElse(0)
    val quantityScore    = Math.min(40, guide.subjects.size * 10)
    val totalFollowers   = guide.subjects
      .map(_ match {
        case Artist(_, followers) => BigInt(followers)
        case _                    => BigInt(0)
      })
      .sum
    val totalBoxOffice   = guide.subjects
      .map(_ match {
        case Movie(_, boxOffice) => BigInt(boxOffice)
        case _                   => BigInt(0)
      })
      .sum

    val followersScore = if (totalFollowers > 15 * 100_000) 15 else totalFollowers.toInt / 100_000
    val boxOfficeScore = if (totalBoxOffice > 15 * 10_000_000) 15 else totalBoxOffice.toInt / 10_000_000
    descriptionScore + quantityScore + followersScore + boxOfficeScore
  }

  /** We needed to parametrize the connection with a different URL. This can be used in tests and production
    * and avoids hardcoding the URLs (they can be provided from an external config)
    */
  def connectionResource(address: String, endpoint: String): Resource[IO, RDFConnection] = Resource.make(
    IO.blocking(
      RDFConnectionRemote.create
        .destination(address)
        .queryEndpoint(endpoint)
        .build
    )
  )(connection => IO.blocking(connection.close()))

  /** STEP 4: develop new functionalities in a test-driven way.
    */
  // PROBLEM: sometimes there will be problems (not enough data, or problems with data access, we should return smaller
  // guide nonetheless)
  case class SearchReport(badGuides: List[TravelGuide], problems: List[String])

  object Version4 {
    def travelGuideForAttraction(dataAccess: DataAccess, attraction: Attraction): IO[TravelGuide] = {
      List(
        dataAccess.findArtistsFromLocation(attraction.location.id, 2),
        dataAccess.findMoviesAboutLocation(attraction.location.id, 2)
      ).parSequence.map(_.flatten).map(subjects => TravelGuide(attraction, subjects))
    }

    def findGoodGuide(guides: List[TravelGuide]): Either[SearchReport, TravelGuide] = {
      guides.sortBy(guideScore).reverse.headOption match {
        case Some(bestGuide) =>
          if (guideScore(bestGuide) > 55) Right(bestGuide) else Left(SearchReport(guides, List.empty))
        case None            => Left(SearchReport(List.empty, List.empty))
      }
    }

    def travelGuide(dataAccess: DataAccess, attractionName: String): IO[Either[SearchReport, TravelGuide]] = {
      dataAccess
        .findAttractions(attractionName, ByLocationPopulation, 3)
        .attempt
        .flatMap(_ match {
          case Left(exception)    => IO.pure(Left(SearchReport(List.empty, List(exception.getMessage))))
          case Right(attractions) => attractions
              .map(attraction => travelGuideForAttraction(dataAccess, attraction))
              .parSequence
              .map(findGoodGuide)
        })
    }
  }

  private def runVersion4 = {
    // We need a DataAccess to run the travelGuide function so let's wrap it in Resource
    val connectionResource: Resource[IO, RDFConnection] = Resource.make(
      IO.blocking(
        RDFConnectionRemote.create
          .destination("https://query.wikidata.org/")
          .queryEndpoint("sparql")
          .build
      )
    )(connection => IO.blocking(connection.close()))

    val dataAccessResource: Resource[IO, DataAccess] =
      connectionResource.map(connection => getSparqlDataAccess(execQuery(connection)))

    unsafeRunTimedIO(dataAccessResource.use(dataAccess =>
      Version4.travelGuide(dataAccess, "Yellowstone")
    )) // Right
    unsafeRunTimedIO(
      dataAccessResource.use(dataAccess => Version4.travelGuide(dataAccess, "Yosemite"))
    )  // Left without errors
    unsafeRunTimedIO(
      dataAccessResource.use(dataAccess => Version4.travelGuide(dataAccess, "Hacking attempt \""))
    )  // Left with errors
  }

  object Version5 {
    import Version4.travelGuideForAttraction

    def findGoodGuide(errorsOrGuides: List[Either[Throwable, TravelGuide]]): Either[SearchReport, TravelGuide] = {
      val guides: List[TravelGuide] = errorsOrGuides.collect(_ match {
        case Right(travelGuide) => travelGuide
      })
      val errors: List[String]      = errorsOrGuides.collect(_ match {
        case Left(exception) => exception.getMessage
      })
      guides.sortBy(guideScore).reverse.headOption match {
        case Some(bestGuide) => if (guideScore(bestGuide) > 55) Right(bestGuide) else Left(SearchReport(guides, errors))
        case None            => Left(SearchReport(List.empty, errors))
      }
    }

    def travelGuide(dataAccess: DataAccess, attractionName: String): IO[Either[SearchReport, TravelGuide]] = {
      dataAccess
        .findAttractions(attractionName, ByLocationPopulation, 3)
        .attempt
        .flatMap(_ match {
          case Left(exception)    => IO.pure(Left(SearchReport(List.empty, List(exception.getMessage))))
          case Right(attractions) => attractions
              .map(attraction => travelGuideForAttraction(dataAccess, attraction))
              .map(_.attempt) // note that we attempt on individual IO values, so it needs to be before parSequence
              .parSequence
              .map(findGoodGuide)
        })
    }

  }

  // not in the book, just mentioned:
  object Version6 {
    // BONUS: you can also make the findGoodGuide function more concise by using the separate function:
    def findGoodGuide(errorsOrGuides: List[Either[Throwable, TravelGuide]]): Either[SearchReport, TravelGuide] = {
      val (errors, guides) = errorsOrGuides.separate
      val errorMessages    = errors.map(_.getMessage)
      guides.sortBy(guideScore).reverse.headOption match {
        case Some(bestGuide) =>
          if (guideScore(bestGuide) > 55) Right(bestGuide) else Left(SearchReport(guides, errorMessages))
        case None            => Left(SearchReport(List.empty, errorMessages))
      }
    }

    import Version4.travelGuideForAttraction
    def travelGuide(dataAccess: DataAccess, attractionName: String): IO[Either[SearchReport, TravelGuide]] = {
      dataAccess
        .findAttractions(attractionName, ByLocationPopulation, 3)
        .attempt
        .flatMap { // BONUS: we can use a simpler syntax to pass functions that use pattern matching (see Appendix A)
          case Left(exception)    => IO.pure(Left(SearchReport(List.empty, List(exception.getMessage))))
          case Right(attractions) => attractions
              .map(attraction => travelGuideForAttraction(dataAccess, attraction))
              .map(_.attempt) // note that we attempt on individual IO values, so it needs to be before parSequence
              .parSequence
              .map(findGoodGuide)
        }
    }
  }

  private def runVersion5 = {
    unsafeRunTimedIO(dataAccessResource.use(dataAccess =>
      Version5.travelGuide(dataAccess, "Yellowstone")
    )) // Right
    unsafeRunTimedIO(
      dataAccessResource.use(dataAccess => Version5.travelGuide(dataAccess, "Yosemite"))
    )  // Left without errors
    unsafeRunTimedIO(
      dataAccessResource.use(dataAccess => Version5.travelGuide(dataAccess, "Hacking attempt \""))
    )  // Left with errors
  }

  private def runVersion6 = {
    unsafeRunTimedIO(dataAccessResource.use(dataAccess =>
      Version6.travelGuide(dataAccess, "Yellowstone")
    )) // Right
    unsafeRunTimedIO(
      dataAccessResource.use(dataAccess => Version6.travelGuide(dataAccess, "Yosemite"))
    )  // Left without errors
    unsafeRunTimedIO(
      dataAccessResource.use(dataAccess => Version6.travelGuide(dataAccess, "Hacking attempt \""))
    )  // Left with errors
  }

  def main(args: Array[String]): Unit = {
    runVersion4
    runVersion5
    runVersion6
  }

  /** Helper function that runs the given IO[A], times its execution, prints it, and returns it
    */
  private def unsafeRunTimedIO[A](io: IO[A]): A = {
    import cats.effect.unsafe.implicits.global
    val start  = System.currentTimeMillis()
    val result = io.unsafeRunSync()
    val end    = System.currentTimeMillis()
    println(s"$result (took ${end - start}ms)")
    result
  }
}
