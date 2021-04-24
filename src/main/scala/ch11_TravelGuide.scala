import cats.effect.{IO, Ref, Resource}
import cats.implicits._
import cats.effect.unsafe.implicits.global
import ch11_WikidataDataAccess.SparqlDataAccess
import org.apache.jena.query.{QueryExecution, QueryFactory, QuerySolution}
import org.apache.jena.rdfconnection.{RDFConnection, RDFConnectionRemote}

import scala.jdk.javaapi.CollectionConverters.asScala

object ch11_TravelGuide extends App {

  /**
    * STEP 1
    * MODEL: just immutable values (reused in the whole application).
    *
    * Note that we have a single model for the whole application, but sometimes it may be beneficial
    * to have separate ones for data access and business domain (see the book).
    */
  case class LocationId(value: String) extends AnyVal
  case class Location(id: LocationId, name: String, population: Long)
  case class Attraction(name: String, description: Option[String], location: Location)

  sealed trait PopCultureSubject
  case class Artist(name: String, followers: Long) extends PopCultureSubject
  case class Movie(name: String, boxOffice: Long)  extends PopCultureSubject

  case class TravelGuide(attraction: Attraction, subjects: List[PopCultureSubject])

  /**
    * STEP 2
    * DATA ACCESS (just an interface providing pure functions, implementation completely separated)
    */
  sealed trait AttractionOrdering
  case object ByName               extends AttractionOrdering
  case object ByLocationPopulation extends AttractionOrdering

  trait DataAccess {
    def fetchAttractions(name: String, ordering: AttractionOrdering, limit: Int): IO[List[Attraction]]
    def fetchArtistsFromLocation(locationId: LocationId, limit: Int): IO[List[Artist]]
    def fetchMoviesAboutLocation(locationId: LocationId, limit: Int): IO[List[Movie]]
  }

  /**
    * STEP 3: first version of a TravelGuide fetcher
    */
  object Version1 {
    def travelGuide(data: DataAccess, attractionName: String): IO[Option[TravelGuide]] = {
      for {
        attractions <- data.fetchAttractions(attractionName, ByLocationPopulation, 1)
        guide <- attractions.headOption match {
                  case None => IO.pure(None)
                  case Some(attraction) =>
                    for {
                      artists <- data.fetchArtistsFromLocation(attraction.location.id, 2)
                      movies  <- data.fetchMoviesAboutLocation(attraction.location.id, 2)
                    } yield Some(TravelGuide(attraction, artists ++ movies))
                }
      } yield guide
    }
  }

  /**
    * STEP 4: implementing real data access
    * @see [[ch11_WikidataDataAccess]] for a Wikidata Sparql endpoint implementation using Apache Jena.
    */
  /**
    * STEP 5: connecting the dots
    */
  // mostly IMPURE CODE, out of the functional core
  val connection = RDFConnectionRemote.create // we will make it better, see at the end
    .destination("https://query.wikidata.org/")
    .queryEndpoint("sparql")
    .build

  {
    // introduce closure
    def execQuery(query: String): IO[List[QuerySolution]] = IO.blocking { // introduce blocking
      asScala(connection.query(QueryFactory.create(query)).execSelect()).toList // it looks OK, but it's not and we'll see why
    }

    val sparql = new SparqlDataAccess(execQuery)

    // now we can execute our program using the real Wikidata data access!
    check.executedIO(Version1.travelGuide(sparql, "Yosemite"))
    // PROBLEM with Version1: for a very popular attraction, like "Yosemite", the returned TravelGuide doesn't contain any pop culture subjects
    // we only check the first result, even though there may be better choices and better locations with similar names
  }

  /**
    * STEP 6: searching for the best travel guide
    * requirements:
    * - 30 points for a description
    * - 10 points for each artist and movie (max 40pts)
    * - 1 point for each 100_000 followers (all artists combined, max 15pts)
    * - 1 point for each 10_000_000 dollars in box-office totals (all movies combined, max 15pts)
    */
  def guideScore(guide: TravelGuide): Int = {
    val descriptionScore = guide.attraction.description.map(_ => 30).getOrElse(0)
    val quantityScore    = Math.min(40, guide.subjects.size * 10)
    val totalFollowers = guide.subjects
      .map(_ match {
        case Artist(_, followers) => followers
        case _                    => 0
      })
      .sum
    val totalBoxOffice = guide.subjects
      .map(_ match {
        case Movie(_, boxOffice) => boxOffice
        case _                   => 0
      })
      .sum

    val followersScore = Math.min(15, totalFollowers / 100_000).toInt
    val boxOfficeScore = Math.min(15, totalBoxOffice / 10_000_000).toInt
    descriptionScore + quantityScore + followersScore + boxOfficeScore
  }

  object Version2 {
    def travelGuide(data: DataAccess, attractionName: String): IO[Option[TravelGuide]] = {
      for {
        attractions <- data.fetchAttractions(attractionName, ByLocationPopulation, 3)
        guides <- attractions
                   .map(attraction =>
                     for {
                       artists <- data.fetchArtistsFromLocation(attraction.location.id, 2)
                       movies  <- data.fetchMoviesAboutLocation(attraction.location.id, 2)
                     } yield TravelGuide(attraction, artists ++ movies)
                   )
                   .sequence
      } yield guides.sortBy(guideScore).reverse.headOption
    }
  }
  // PROBLEM: it may not work, because we are leaking closable resources (in this case query executions)

  /**
    * STEP 7: handle resource leaks (query execution)
    */
  // introduce Resource
  def execQuery(query: String): IO[List[QuerySolution]] = {
    val createExecution: IO[QueryExecution] = IO.blocking(connection.query(QueryFactory.create(query)))
    val executionResource: Resource[IO, QueryExecution] =
      Resource.make(createExecution)(execution => IO.blocking(execution.close())) // or Resource.fromAutoCloseable(createExecution) (see below)
    executionResource.use(execution => IO.blocking(asScala(execution.execSelect()).toList))
  }

  val sparql = new SparqlDataAccess(execQuery)

  check.executedIO(Version2.travelGuide(sparql, "Yellowstone")) // this will not leak, even if there are errors

  // PROBLEM: we can make parallel queries in two attractions

  /**
    * STEP 8: make it concurrent (and fast)
    */
  object Version3 {
    // Coffee Break: making it concurrent
    def travelGuide(data: DataAccess, attractionName: String): IO[Option[TravelGuide]] = {
      for {
        attractions <- data.fetchAttractions(attractionName, ByLocationPopulation, 3)
        guides <- attractions
                   .map(attraction =>
                     List(
                       data.fetchArtistsFromLocation(attraction.location.id, 2),
                       data.fetchMoviesAboutLocation(attraction.location.id, 2)
                     ).parSequence.map(_.flatten).map(TravelGuide(attraction, _))
                   )
                   .parSequence
      } yield guides.sortBy(guideScore).reverse.headOption
    }
  }
  check.executedIO(Version3.travelGuide(sparql, "Yellowstone")) // this will take a lot less time than Version2!

  // PROBLEM: we don't have to execute queries, we can cache them locally

  /**
    * STEP 9: make it faster
    */
  def cachedExecQuery(cache: Ref[IO, Map[String, List[QuerySolution]]])(query: String): IO[List[QuerySolution]] = {
    // you may want to use a hybrid flatMap/for comprehension approach
    // if you need to use one of the earlier values, like result to save it to cache, for comprehension may be a more readable choice
    cache.get.flatMap(_.get(query) match {
      case Some(cachedSolutions) => IO.pure(cachedSolutions)
      case None =>
        for {
          result <- execQuery(query)
          _      <- cache.update(_.updated(query, result))
        } yield result
    })
  }

  check.executedIO(
    for {
      cache        <- Ref.of[IO, Map[String, List[QuerySolution]]](Map.empty)
      cachedSparql = new SparqlDataAccess(cachedExecQuery(cache))
      result1      <- Version3.travelGuide(cachedSparql, "Yellowstone")
      result2      <- Version3.travelGuide(cachedSparql, "Yellowstone")
      result3      <- Version3.travelGuide(cachedSparql, "Yellowstone")
    } yield result1.toList.appendedAll(result2).appendedAll(result3)
  ) // the second and third execution will take a lot less time because all queries are cached!

  // PROBLEM: sometimes there will be problems (not enough data, or problems with data access, we should return smaller guide nonetheless)

  /**
    * STEP 10: make it resilient and avoid unnecessary work
    */
  case class SearchReport(badGuides: List[TravelGuide], errors: List[Throwable])

  /**
    * TODO: Practicing type-level exceptions (attempt)
    */
  object Version4 {
    def findGoodGuide(
        data: DataAccess,
        attractions: List[Attraction],
        report: SearchReport
    ): IO[Either[SearchReport, TravelGuide]] = {
      attractions.headOption match {
        case Some(attraction) =>
          for {
            attractionResult <- List(
                                 data.fetchArtistsFromLocation(attraction.location.id, 2),
                                 data.fetchMoviesAboutLocation(attraction.location.id, 2)
                               ).parSequence.map(_.flatten).map(TravelGuide(attraction, _)).attempt
            result <- attractionResult match {
                       case Left(error) =>
                         findGoodGuide(data, attractions.tail, report.copy(errors = report.errors.appended(error)))
                       case Right(guide) =>
                         if (guideScore(guide) > 55) { // we found a good-enough guide, we return it without searching for better ones
                           IO.pure(Right(guide))
                         } else {
                           findGoodGuide(
                             data,
                             attractions.tail,
                             report.copy(badGuides = report.badGuides.appended(guide))
                           )
                         }
                     }
          } yield result
        case None => IO.pure(Left(report))
      }
    }

    def travelGuide(data: DataAccess, attractionName: String): IO[Either[SearchReport, TravelGuide]] = {
      for {
        attractions     <- data.fetchAttractions(attractionName, ByLocationPopulation, 3)
        guideOrProblems <- findGoodGuide(data, attractions, SearchReport(List.empty, List.empty))
      } yield guideOrProblems
    }
    // TODO: BONUS: can you do it using foldLeft?
  }
  check.executedIO(Version4.travelGuide(sparql, "Yellowstone")) // Right
  check.executedIO(Version4.travelGuide(sparql, "Yosemite"))    // Left without errors
  // check.executedIO(Version4.travelGuide(sparql, "Hacking attempt \"")) // exception
  // how do we test that exceptions in fetching artists or movies do not crash the program? (see chapter 12 for proper tests)

  /**
    * STEP 11: Making sure the connection is always closed
    */
  connection.close() // we need to do it when we stop needing it, even when there has been a failure

  {
    val connectionResource = Resource.fromAutoCloseable(
      IO.blocking(
        RDFConnectionRemote.create
          .destination("https://query.wikidata.org/")
          .queryEndpoint("sparql")
          .build
      )
    )

    def queryExecutionResource(connection: RDFConnection, query: String): Resource[IO, QueryExecution] = {
      Resource.fromAutoCloseable(IO.blocking(connection.query(QueryFactory.create(query))))
    }

    def travelGuideProgram(attractionName: String): IO[Either[SearchReport, TravelGuide]] =
      connectionResource.use(connection => {
        def execQuery(query: String): IO[List[QuerySolution]] = {
          queryExecutionResource(connection, query).use(execution =>
            IO.blocking(asScala(execution.execSelect()).toList)
          )
        }
        val sparql = new SparqlDataAccess(execQuery)
        Version4.travelGuide(sparql, attractionName)
      })

    check.executedIO(travelGuideProgram("Bridge of Sighs"))
  }
}
