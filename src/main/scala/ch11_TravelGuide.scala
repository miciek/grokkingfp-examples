import cats.effect.IO
import cats.implicits._
import cats.effect.unsafe.implicits.global
import org.apache.jena.query.{QueryFactory, QuerySolution}
import org.apache.jena.rdfconnection.{RDFConnection, RDFConnectionRemote}

import scala.jdk.javaapi.CollectionConverters.asScala

/**
  * https://query.wikidata.org/
  */
object ch11_TravelGuide extends App {
  def execQuery(connection: RDFConnection, query: String): List[QuerySolution] = {
    asScala(connection.query(QueryFactory.create(query)).execSelect()).toList
  }

  sealed trait PlaceOrdering
  case object ByName               extends PlaceOrdering
  case object ByLocationPopulation extends PlaceOrdering

  case class LocationId(value: String) extends AnyVal
  case class Location(id: LocationId, name: String, population: Int)
  case class Place(name: String, location: Location)

  def fetchPlaces(connection: RDFConnection)(name: String, ordering: PlaceOrdering, limit: Int): IO[List[Place]] = {
    val orderBy = ordering match {
      case ByName               => "?placeLabel"
      case ByLocationPopulation => "DESC(?population)"
    }

    val query = s"""
      |PREFIX wd: <http://www.wikidata.org/entity/>
      |PREFIX wdt: <http://www.wikidata.org/prop/direct/>
      |PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
      |
      |SELECT DISTINCT ?place ?placeLabel ?location ?locationLabel ?population WHERE {
      |  ?place wdt:P948 ?banner;
      |         rdfs:label ?placeLabel;
      |         wdt:P131 ?location;
      |         wdt:P17 wd:Q30.
      |  FILTER(LANG(?placeLabel) = "en").
      |  
      |  ?location wdt:P1082 ?population;
      |            rdfs:label ?locationLabel;
      |  FILTER(LANG(?locationLabel) = "en").
      |  
      |  FILTER(CONTAINS(?placeLabel, "$name")). # sometimes the place is the location, sometimes it's not (Route 66)
      |} ORDER BY $orderBy LIMIT $limit
      |""".stripMargin

    IO.delay {
      execQuery(connection, query).map(s =>
        Place( // introduce named parameters
          name = s.getLiteral("placeLabel").getString,
          location = Location(
            LocationId(s.getResource("location").getLocalName),
            s.getLiteral("locationLabel").getString,
            s.getLiteral("population").getInt
          )
        )
      )
    }
  }

  case class Artist(name: String, followers: Int)
  def fetchArtistsFromLocation(connection: RDFConnection)(locationId: LocationId, limit: Int): IO[List[Artist]] = {
    val query = s"""
      |PREFIX wd: <http://www.wikidata.org/entity/>
      |PREFIX wdt: <http://www.wikidata.org/prop/direct/>
      |PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
      |
      |SELECT DISTINCT ?artist ?artistLabel ?followers WHERE {
      |  ?artist wdt:P136 ?genre;
      |          # wdt:P18 ?image; surprise me mode
      |          wdt:P8687 ?followers;
      |          rdfs:label ?artistLabel.
      |  FILTER(LANG(?artistLabel) = "en").
      |
      |  ?artist wdt:P740 wd:${locationId.value}
      |
      |} ORDER BY DESC(?followers) LIMIT $limit
      |""".stripMargin
    IO.delay {
      execQuery(connection, query).map(s =>
        Artist(
          name = s.getLiteral("artistLabel").getString,
          followers = s.getLiteral("followers").getInt
        )
      )
    }
  }

  case class Movie(name: String, boxOffice: Int)
  def fetchMoviesAboutLocation(connection: RDFConnection)(locationId: LocationId, limit: Int): IO[List[Movie]] = {
    // TODO: Q1085 Prague, Montenegro, Casino Royale?
    val query = s"""
      |PREFIX wd: <http://www.wikidata.org/entity/>
      |PREFIX wdt: <http://www.wikidata.org/prop/direct/>
      |PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
      |
      |SELECT DISTINCT ?subject ?subjectLabel ?boxOffice WHERE {
      |  ?subject wdt:P31 wd:Q11424; # video games, books?
      |           wdt:P2142 ?boxOffice;
      |           rdfs:label ?subjectLabel.
      |
      |  ?subject wdt:P840 wd:${locationId.value}
      |
      |  FILTER(LANG(?subjectLabel) = "en").
      |
      |} ORDER BY DESC(?boxOffice) LIMIT $limit
      |""".stripMargin

    IO.delay {
      execQuery(connection, query).map(s =>
        Movie(
          name = s.getLiteral("subjectLabel").getString,
          boxOffice = s.getLiteral("boxOffice").getInt
        )
      )
    }
  }

  def travelGuide(connection: RDFConnection)(subject: String): IO[Unit] =
    for {
      places        <- fetchPlaces(connection)(subject, ByLocationPopulation, 5)
      _             = println(places)
      topLocationId = places.head.location.id
      artists       <- fetchArtistsFromLocation(connection)(topLocationId, 5)
      _             = println(artists)
      movies        <- fetchMoviesAboutLocation(connection)(topLocationId, 5)
      _             = println(movies)
    } yield ()

  { // IMPURE CODE, out of the functional core
    val conn = RDFConnectionRemote.create // we could use Resource for this, TODO: example
      .destination("https://query.wikidata.org/")
      .queryEndpoint("sparql")
      .build

    try {
      travelGuide(conn)("Downtown Seattle").unsafeRunSync()
    } finally if (conn != null) conn.close()
  }
}
