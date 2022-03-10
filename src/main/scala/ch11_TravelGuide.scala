import cats.effect.{IO, Ref, Resource}
import cats.implicits._
import cats.effect.unsafe.implicits.global
import ch11_WikidataDataAccess.getSparqlDataAccess
import org.apache.jena.query.{QueryExecution, QueryFactory, QuerySolution}
import org.apache.jena.rdfconnection.{RDFConnection, RDFConnectionRemote}

import scala.concurrent.duration._
import scala.jdk.javaapi.CollectionConverters.asScala

object ch11_TravelGuide {

  /** STEP 1
    * MODEL: just immutable values (reused in the whole application).
    *
    * Note that we have a single model for the whole application, but sometimes it may be beneficial
    * to have separate ones for data access and business domain (see the book).
    */
  object model {
    opaque type LocationId = String
    object LocationId:
      def apply(value: String): LocationId        = value
      extension (a: LocationId) def value: String = a

    case class Location(id: LocationId, name: String, population: Int)
    case class Attraction(name: String, description: Option[String], location: Location)
    enum PopCultureSubject {
      case Artist(name: String, followers: Int)
      case Movie(name: String, boxOffice: Int)
    }

    case class TravelGuide(attraction: Attraction, subjects: List[PopCultureSubject])
  }

  import model._, model.PopCultureSubject._

  /** STEP 2
    * DATA ACCESS (just an interface providing pure functions, implementation completely separated)
    */
  enum AttractionOrdering {
    case ByName
    case ByLocationPopulation
  }
  import AttractionOrdering._

  trait DataAccess             {
    def findAttractions(name: String, ordering: AttractionOrdering, limit: Int): IO[List[Attraction]]
    def findArtistsFromLocation(locationId: LocationId, limit: Int): IO[List[Artist]]
    def findMoviesAboutLocation(locationId: LocationId, limit: Int): IO[List[Movie]]
  }
  object DataAccessAlternative { // alternatively DataAccess can be modeled as a product type:
    case class DataAccess( // NOTE: WE DON'T USE IT IN THE BOOK, we use the type above
        findAttractions: (String, AttractionOrdering, Int) => IO[List[Attraction]],
        findArtistsFromLocation: (LocationId, Int) => IO[List[Artist]],
        findMoviesAboutLocation: (LocationId, Int) => IO[List[Movie]]
    )
  }

  /** STEP 3: first version of a TravelGuide finder
    */
  object Version1 {
    def travelGuide(data: DataAccess, attractionName: String): IO[Option[TravelGuide]] = {
      for {
        attractions <- data.findAttractions(attractionName, ByLocationPopulation, 1)
        guide       <- attractions.headOption match {
                         case None             => IO.pure(None)
                         case Some(attraction) => for {
                             artists <- data.findArtistsFromLocation(attraction.location.id, 2)
                             movies  <- data.findMoviesAboutLocation(attraction.location.id, 2)
                           } yield Some(TravelGuide(attraction, artists.appendedAll(movies)))
                       }
      } yield guide
    }
  }

  /** STEP 4: implementing real data access
    * @see [[ch11_QueryingWikidata]] for a simple Wikidata query using Apache Jena imperatively
    */
  private def runStep4 = {
    val getConnection: IO[RDFConnection] = IO.delay(
      RDFConnectionRemote.create // we will make it better, see at the end
        .destination("https://query.wikidata.org/")
        .queryEndpoint("sparql")
        .build
    )

    def execQuery(getConnection: IO[RDFConnection], query: String): IO[List[QuerySolution]] = {
      getConnection.flatMap(c =>
        IO.delay(
          asScala(c.query(QueryFactory.create(query)).execSelect()).toList
        )
      )
    }

    def parseAttraction(s: QuerySolution): IO[Attraction] = {
      IO.delay(
        Attraction(
          name = s.getLiteral("attractionLabel").getString,
          description = if (s.contains("description")) Some(s.getLiteral("description").getString) else None,
          location = Location(
            id = LocationId(s.getResource("location").getLocalName),
            name = s.getLiteral("locationLabel").getString,
            population = s.getLiteral("population").getInt
          )
        )
      )
    }

    def findAttractions(name: String, ordering: AttractionOrdering, limit: Int): IO[List[Attraction]] = {
      val orderBy = ordering match {
        case ByName               => "?attractionLabel"
        case ByLocationPopulation => "DESC(?population)"
      }

      val query = s"""
                     PREFIX wd: <http://www.wikidata.org/entity/>
                     PREFIX wdt: <http://www.wikidata.org/prop/direct/>
                     PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
                     PREFIX schema: <http://schema.org/>
                     SELECT DISTINCT ?attraction ?attractionLabel ?description ?location ?locationLabel ?population WHERE {
                       ?attraction wdt:P31 wd:Q570116;
                                   rdfs:label ?attractionLabel;
                                   wdt:P131 ?location.
                       FILTER(LANG(?attractionLabel) = "en").
                     
                       OPTIONAL {
                         ?attraction schema:description ?description.
                         FILTER(LANG(?description) = "en").
                       }
                     
                       ?location wdt:P1082 ?population;
                                 rdfs:label ?locationLabel;
                       FILTER(LANG(?locationLabel) = "en").
                     
                       FILTER(CONTAINS(?attractionLabel, "$name")).
                     } ORDER BY $orderBy LIMIT $limit
                     """

      for {
        solutions   <- execQuery(getConnection, query)
        attractions <- solutions.traverse(parseAttraction) // or map(parseAttraction).sequence
      } yield attractions
    }

    def configuringAFunction = { // currying discussion (configuring a function)
      def findAttractions(
          connection: RDFConnection
      )(name: String, ordering: AttractionOrdering, limit: Int): IO[List[Attraction]] = ???

      val connection: RDFConnection                                    = null
      val f: (String, AttractionOrdering, Int) => IO[List[Attraction]] = findAttractions(connection)
      f // you should be able to use f now without knowing anything about the connection (which you should close() later)
    }

    check
      .executedIO(findAttractions("Bridge of Sighs", ByLocationPopulation, 1))
      .expectThat(_.map(_.name) == List("Bridge of Sighs"))

    // connection.close() // PROBLEM we are not able to close each connection used by the getConnection clients
    // and we can't pass connection directly because it's a mutable, stateful value

    /** @see [[ch11_WikidataDataAccess]] for a Wikidata Sparql endpoint implementation using Apache Jena
      *      and final version of all DataAccess functions
      */
  }

  /** STEP 5: connecting the dots
    */
  // mostly IMPURE CODE, out of the functional core
  private def runStep5 = {
    def execQuery(connection: RDFConnection)(query: String): IO[List[QuerySolution]] = IO.blocking(
      asScala(connection.query(QueryFactory.create(query)).execSelect()).toList
    ) // it looks OK, but it's not and we'll see why

    val connection: RDFConnection = RDFConnectionRemote.create
      .destination("https://query.wikidata.org/")
      .queryEndpoint("sparql")
      .build // we will make it better, see STEP 7

    val wikidata = getSparqlDataAccess(execQuery(connection))

    // now we can execute our program using the real Wikidata data access!
    check.executedIO(Version1.travelGuide(wikidata, "Yosemite"))
    // PROBLEM with Version1: for a very popular attraction, like "Yosemite", the returned TravelGuide doesn't contain any pop culture subjects
    // we only check the first result, even though there may be better choices and better locations with similar names

    connection.close()
  }

  /** STEP 6: searching for the best travel guide
    * requirements:
    * - 30 points for a description
    * - 10 points for each artist and movie (max 40pts)
    * - 1 point for each 100_000 followers (all artists combined, max 15pts)
    * - 1 point for each 10_000_000 dollars in box-office totals (all movies combined, max 15pts)
    */
  def guideScore(guide: TravelGuide): Int = {
    val descriptionScore = guide.attraction.description.map(_ => 30).getOrElse(0)
    val quantityScore    = Math.min(40, guide.subjects.size * 10)
    val totalFollowers   = guide.subjects
      .map(_ match {
        case Artist(_, followers) => followers
        case _                    => 0
      })
      .sum
    val totalBoxOffice   = guide.subjects
      .map(_ match {
        case Movie(_, boxOffice) => boxOffice
        case _                   => 0
      })
      .sum

    val followersScore = Math.min(15, totalFollowers / 100_000)
    val boxOfficeScore = Math.min(15, totalBoxOffice / 10_000_000)
    descriptionScore + quantityScore + followersScore + boxOfficeScore
  }

  object Version2 {
    def travelGuide(data: DataAccess, attractionName: String): IO[Option[TravelGuide]] = {
      for {
        attractions <- data.findAttractions(attractionName, ByLocationPopulation, 3)
        guides      <- attractions
                         .map(attraction =>
                           for {
                             artists <- data.findArtistsFromLocation(attraction.location.id, 2)
                             movies  <- data.findMoviesAboutLocation(attraction.location.id, 2)
                           } yield TravelGuide(attraction, artists.appendedAll(movies))
                         )
                         .sequence
      } yield guides.sortBy(guideScore).reverse.headOption
    }
  }
  // PROBLEM: it may not work, because we are leaking closable resources (in this case query executions)

  /** STEP 7: handle resource leaks (query execution and connection)
    */
  def createExecution(connection: RDFConnection, query: String): IO[QueryExecution] = IO.blocking(
    connection.query(QueryFactory.create(query))
  )
  def closeExecution(execution: QueryExecution): IO[Unit]                           = IO.blocking(
    execution.close()
  )

  private def runVersion2 = { // handling resource release manually
    def execQuery(connection: RDFConnection)(query: String): IO[List[QuerySolution]] = {
      for {
        execution <- createExecution(connection, query)
        solutions <- IO.blocking(asScala(execution.execSelect()).toList) // orElse needed too
        _         <- closeExecution(execution)
      } yield solutions
    }

    val connection: RDFConnection = RDFConnectionRemote.create
      .destination("https://query.wikidata.org/")
      .queryEndpoint("sparql")
      .build

    val wikidata = getSparqlDataAccess(execQuery(connection))
    check.executedIO(Version2.travelGuide(wikidata, "Yosemite"))
    connection.close()

    // PROBLEM: this will not leak in happy-path, but may leak on errors (we need orElse(closeExecution), too)
    // we need something better, we need Resource!
  }

  // introduce Resource
  def execQuery(connection: RDFConnection)(query: String): IO[List[QuerySolution]] = {
    val executionResource: Resource[IO, QueryExecution] = Resource.make(createExecution(connection, query))(
      closeExecution
    ) // or Resource.fromAutoCloseable(createExecution)

    executionResource.use(execution => IO.blocking(asScala(execution.execSelect()).toList))
  }

  val connectionResource: Resource[IO, RDFConnection] = Resource.make(
    IO.blocking(
      RDFConnectionRemote.create
        .destination("https://query.wikidata.org/")
        .queryEndpoint("sparql")
        .build
    )
  )(connection => IO.blocking(connection.close()))

  private def runVersion2UsingResource = {
    val program: IO[Option[TravelGuide]] = connectionResource.use(connection => {
      val wikidata = getSparqlDataAccess(execQuery(connection))
      Version2.travelGuide(wikidata, "Yosemite") // this will not leak, even if there are errors
    })

    check.executedIO(program)
    check.executedIO(program) // you can execute it as many times as you want
  }

  // Resource has map! TODO: Practicing section, Resource.use, flatMap, map (chapter 5), fromAutocloseable
  val queryExecResource: Resource[IO, String => IO[List[QuerySolution]]] = connectionResource.map(execQuery)
  val dataAccessResource: Resource[IO, DataAccess]                       =
    connectionResource.map(connection => getSparqlDataAccess(execQuery(connection)))

  private def runVersion2WithMappedResource = {
    check.executedIO(dataAccessResource.use(dataAccess => Version2.travelGuide(dataAccess, "Yosemite")))
  }

  // PROBLEM: we make all queries sequentially, but we can make parallel queries in two attractions

  /** STEP 8: make it concurrent (and fast)
    */
  object Version3 {
    // Coffee Break: making it concurrent
    def travelGuide(data: DataAccess, attractionName: String): IO[Option[TravelGuide]] = {
      for {
        attractions <- data.findAttractions(attractionName, ByLocationPopulation, 3)
        guides      <- attractions
                         .map(attraction =>
                           List(
                             data.findArtistsFromLocation(attraction.location.id, 2),
                             data.findMoviesAboutLocation(attraction.location.id, 2)
                           ).parSequence.map(_.flatten).map(popCultureSubjects =>
                             TravelGuide(attraction, popCultureSubjects)
                           )
                         )
                         .parSequence
      } yield guides.sortBy(guideScore).reverse.headOption
    }
  }

  private def runVersion3 = {
    check.executedIO(
      dataAccessResource.use(dataAccess => Version3.travelGuide(dataAccess, "Yellowstone"))
    ) // this will take a lot less time than Version2!
  }

  // PROBLEM: we are repeating the same queries, but the results don't change that often.

  /** STEP 9: make it faster
    * we don't have to execute queries, we can cache them locally
    */
  def cachedExecQuery(connection: RDFConnection, cache: Ref[IO, Map[String, List[QuerySolution]]])(
      query: String
  ): IO[List[QuerySolution]] = {
    for {
      cachedQueries <- cache.get
      solutions     <- cachedQueries.get(query) match {
                         case Some(cachedSolutions) => IO.pure(cachedSolutions)
                         case None                  => for {
                             realSolutions <- execQuery(connection)(query)
                             _             <- cache.update(_.updated(query, realSolutions))
                           } yield realSolutions
                       }
    } yield solutions
  }

  private def runCachedVersion = {
    check.executedIO(
      connectionResource.use(connection =>
        for {
          cache       <- Ref.of[IO, Map[String, List[QuerySolution]]](Map.empty)
          cachedSparql = getSparqlDataAccess(cachedExecQuery(connection, cache))
          result1     <- Version3.travelGuide(cachedSparql, "Yellowstone")
          result2     <- Version3.travelGuide(cachedSparql, "Yellowstone")
          result3     <- Version3.travelGuide(cachedSparql, "Yellowstone")
        } yield result1.toList.appendedAll(result2).appendedAll(result3)
      )
    ) // the second and third execution will take a lot less time because all queries are cached!
  }

  /** BONUS: make it resilient
    * let's fail fast if requests take too long (timeout 30s)
    */
  private def runCachedVersionWithTimeouts = {
    check.executedIO(
      connectionResource.use(connection =>
        for {
          cache       <- Ref.of[IO, Map[String, List[QuerySolution]]](Map.empty)
          cachedSparql =
            getSparqlDataAccess(
              cachedExecQuery(connection, cache).map(_.timeout(30.seconds)) // note that we map over a functions result.
            )                                                               // each query will fail after 30 seconds, releasing all the resources!
          result1     <- Version3.travelGuide(cachedSparql, "Yellowstone")
          result2     <- Version3.travelGuide(cachedSparql, "Yellowstone")
          result3     <- Version3.travelGuide(cachedSparql, "Yellowstone")
        } yield result1.toList.appendedAll(result2).appendedAll(result3)
      )
    ) // the second and third execution will take a lot less time because all queries are cached!
  }

  def main(args: Array[String]): Unit = {
    System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "Error")
    runStep4
    runStep5
    runVersion2
    runVersion2UsingResource
    runVersion2WithMappedResource
    runVersion3
    runCachedVersion
    runCachedVersionWithTimeouts
  }
}
