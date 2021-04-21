import cats.effect.IO
import cats.implicits._
import cats.effect.unsafe.implicits.global
import ch11_WikidataDataAccess.SparqlDataAccess
import org.apache.jena.query.{QueryFactory, QuerySolution}
import org.apache.jena.rdfconnection.RDFConnectionRemote

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
  case class Location(id: LocationId, name: String, population: Int)
  case class Place(name: String, description: Option[String], location: Location)

  sealed trait PopCultureSubject
  case class Artist(name: String, followers: Int) extends PopCultureSubject
  case class Movie(name: String, boxOffice: Long) extends PopCultureSubject

  case class TravelGuide(place: Place, subjects: List[PopCultureSubject])

  /**
    * STEP 2
    * DATA ACCESS (just an interface providing pure functions, implementation completely separated)
    */
  sealed trait PlaceOrdering
  case object ByName               extends PlaceOrdering
  case object ByLocationPopulation extends PlaceOrdering

  trait DataAccess {
    def fetchPlaces(name: String, ordering: PlaceOrdering, limit: Int): IO[List[Place]]
    def fetchArtistsFromLocation(locationId: LocationId, limit: Int): IO[List[Artist]]
    def fetchMoviesAboutLocation(locationId: LocationId, limit: Int): IO[List[Movie]]
  }

  /**
    * STEP 3: first version of a TravelGuide fetcher
    */
  object Version1 {
    def travelGuide(data: DataAccess, placeName: String): IO[Option[TravelGuide]] = {
      for {
        places <- data.fetchPlaces(placeName, ByLocationPopulation, 5)
        guide <- places.headOption match {
                  case None => IO.pure(None)
                  case Some(place) =>
                    for {
                      artists <- data.fetchArtistsFromLocation(place.location.id, 2)
                      movies  <- data.fetchMoviesAboutLocation(place.location.id, 2)
                    } yield Some(TravelGuide(place, artists ++ movies))
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
  val connection = RDFConnectionRemote.create // we could use Resource for this, TODO: example
    .destination("https://query.wikidata.org/")
    .queryEndpoint("sparql")
    .build

  // introduce closure
  def execQuery(query: String): IO[List[QuerySolution]] = IO.delay {
    asScala(connection.query(QueryFactory.create(query)).execSelect()).toList
  }

  val sparql = new SparqlDataAccess(execQuery)

  // now we can execute our program using the real Wikidata data access!
  check(Version1.travelGuide(sparql, "Yosemite").unsafeRunSync()) // TODO
  // PROBLEM with Version1: for a very popular attraction, like "Yosemite", the returned TravelGuide doesn't contain any pop culture subjects

  /**
    * STEP 6: searching for the best travel guide
    */
  def guideAttractiveness(guide: TravelGuide): Int = {
    val descriptionScore = guide.place.description.map(_ => 30).getOrElse(0)
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

    val followersScore = Math.min(15, totalFollowers / 100_000)
    val boxOfficeScore = Math.min(15, totalBoxOffice / 1_000_000).toInt
    val r              = descriptionScore + quantityScore + followersScore + boxOfficeScore
    println(s"${guide.place.name} has score $r")
    r
  }

  object Version2 {
    def travelGuide(data: DataAccess, placeName: String): IO[Option[TravelGuide]] = {
      for {
        places <- data.fetchPlaces(placeName, ByLocationPopulation, 3)
        guides <- places
                   .map(place =>
                     for {
                       artists <- data.fetchArtistsFromLocation(place.location.id, 2)
                       movies  <- data.fetchMoviesAboutLocation(place.location.id, 2)
                     } yield TravelGuide(place, artists ++ movies)
                   )
                   .sequence
        _ = println(guides.mkString("\n"))
      } yield guides.sortBy(guideAttractiveness).reverse.headOption
    }
  }

  // check(Version2.travelGuide(sparql, "Yosemite").unsafeRunSync()) // TODO: doesn't work

  // PROBLEM: we can make parallel queries in two places
  // PROBLEM: we don't have to execute queries, we can cache them locally
  // PROBLEM: sometimes there will be problems (not enough data, or problems with data access, we should return smaller guide nonetheless)

  connection.close() // again: It'd be better to use Resource, TODO: example
}
