import cats.effect.IO
import ch11_TravelGuide._
import org.apache.jena.query.QuerySolution
import scala.util.Try

object ch11_WikidataDataAccess extends App {

  /**
    * Sparql Wikidata Data Access (separated)
    */
  class SparqlDataAccess(execQuery: String => IO[List[QuerySolution]]) extends DataAccess {
    val prefixes = """
        |PREFIX wd: <http://www.wikidata.org/entity/>
        |PREFIX wdt: <http://www.wikidata.org/prop/direct/>
        |PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
        |PREFIX schema: <http://schema.org/>
        |""".stripMargin

    def fetchPlaces(name: String, ordering: PlaceOrdering, limit: Int): IO[List[Place]] = {
      val orderBy = ordering match {
        case ByName               => "?placeLabel"
        case ByLocationPopulation => "DESC(?population)"
      }

      val query = s"""
        |$prefixes
        |SELECT DISTINCT ?place ?placeLabel ?description ?location ?locationLabel ?population WHERE {
        |  ?place wdt:P31 wd:Q570116;
        |         rdfs:label ?placeLabel;
        |         wdt:P131 ?location.
        |  FILTER(LANG(?placeLabel) = "en").
        |
        |  OPTIONAL {
        |    ?place schema:description ?description.
        |    FILTER(LANG(?description) = "en").
        |  }
        |
        |  ?location wdt:P1082 ?population;
        |            rdfs:label ?locationLabel;
        |  FILTER(LANG(?locationLabel) = "en").
        |
        |  FILTER(CONTAINS(?placeLabel, "$name")).
        |} ORDER BY $orderBy LIMIT $limit
        |""".stripMargin

      for {
        solutions <- execQuery(query)
        places <- IO.delay(
                   solutions.map(s =>
                     Place( // introduce named parameters
                       name = s.getLiteral("placeLabel").getString,
                       description = Try(s.getLiteral("description").getString).toOption,
                       location = Location(
                         LocationId(s.getResource("location").getLocalName),
                         s.getLiteral("locationLabel").getString,
                         s.getLiteral("population").getInt
                       )
                     )
                   )
                 )
      } yield places
    }

    def fetchArtistsFromLocation(locationId: LocationId, limit: Int): IO[List[Artist]] = {
      val query = s"""
        |$prefixes
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

      for {
        solutions <- execQuery(query)
        artists <- IO.delay(
                    solutions.map(s =>
                      Artist(
                        name = s.getLiteral("artistLabel").getString,
                        followers = s.getLiteral("followers").getInt
                      )
                    )
                  )
      } yield artists
    }

    def fetchMoviesAboutLocation(locationId: LocationId, limit: Int): IO[List[Movie]] = {
      // TODO: Q1085 Prague, Montenegro, Casino Royale?
      val query = s"""
        |$prefixes
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

      for {
        solutions <- execQuery(query)
        movies <- IO.delay(
                   solutions.map(s =>
                     Movie(
                       name = s.getLiteral("subjectLabel").getString,
                       boxOffice = s.getLiteral("boxOffice").getLong
                     )
                   )
                 )
      } yield movies
    }
  }
}
