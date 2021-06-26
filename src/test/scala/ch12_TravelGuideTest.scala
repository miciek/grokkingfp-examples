import cats.effect.{IO, Resource}
import cats.effect.unsafe.implicits.global
import cats.implicits._
import ch11_TravelGuide._
import ch11_TravelGuide.Version3.travelGuide
import ch11_WikidataDataAccess.getSparqlDataAccess
import ch12_TravelGuide.{SearchReport, Version4, Version5}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalacheck.Arbitrary._
import org.scalacheck.Gen
import org.apache.jena.fuseki.main.FusekiServer
import org.apache.jena.query.DatasetFactory
import org.apache.jena.rdfconnection.RDFConnection
import org.apache.jena.riot.RDFDataMgr

/** @see [[ch11_TravelGuide]] to verify requirements
  */
class ch12_TravelGuideTest extends AnyFunSuite with ScalaCheckPropertyChecks {

  /** STEP 1: testing by providing examples
    */
  test("score of a guide with a description, 0 artists, and 2 popular movies should be 65") {
    val guide = TravelGuide(
      Attraction(
        "Yellowstone National Park",
        Some("first national park in the world"),
        Location(LocationId("Q1214"), "Wyoming", 586107)
      ),
      List(Movie("The Hateful Eight", 155760117), Movie("Heaven's Gate", 3484331))
    )

    // 30 (description) + 0 (0 artists) + 20 (2 movies) + 15 (159 million box office)
    assert(guideScore(guide) == 65)
  }

  // Practicing testing by example
  test("score of a guide with no description, 0 artists, and 0 movies should be 0") {
    val guide = TravelGuide(
      Attraction(
        "Yellowstone National Park",
        None,
        Location(LocationId("Q1214"), "Wyoming", 586107)
      ),
      List.empty
    )

    // 0 (description) + 0 (0 artists) + 0 (0 movies)
    assert(guideScore(guide) == 0)
  }

  test("score of a guide with no description, 0 artists, and 2 movies with no box office earnings should be 20") {
    val guide = TravelGuide(
      Attraction(
        "Yellowstone National Park",
        None,
        Location(LocationId("Q1214"), "Wyoming", 586107)
      ),
      List(Movie("The Hateful Eight", 0), Movie("Heaven's Gate", 0))
    )

    // 0 (description) + 0 (0 artists) + 20 (2 movies) + 0 (0 million box office)
    assert(guideScore(guide) == 20)
  }

  /** STEP 2: testing by providing properties
    */
  test("guide score should not depend on its attraction's name and description strings") {
    forAll((name: String, description: String) => {
      val guide = TravelGuide(
        Attraction(
          name, // introduce: empty strings and shorter/longer sizes with different characters
          Some(description),
          Location(LocationId("Q1214"), "Wyoming", 586107)
        ),
        List(Movie("The Hateful Eight", 155760117), Movie("Heaven's Gate", 3484331))
      )

      // 30 (description) + 0 (0 artists) + 20 (2 movies) + 15 (159 million box office)
      assert(guideScore(guide) == 65)
    })
  }

  test("guide score should always be between 30 and 70 if it has a description and some bad movies") {
    forAll((amountOfMovies: Byte) => {
      val guide = TravelGuide(
        Attraction(
          "Yellowstone National Park",
          Some("first national park in the world"),
          Location(LocationId("Q1214"), "Wyoming", 586107)
        ),
        if (amountOfMovies > 0) List.fill(amountOfMovies)(Movie("Random Movie", 0))
        else List.empty
      )

      val score = guideScore(guide)

      // min. 30 (description) and no more than 70 (upper limit with no artists and 0 box office)
      assert(score >= 30 && score <= 70)
    })
  }

  test("guide score should always be between 20 and 50 if there is an artist and a movie, but no description") {
    forAll((followers: Int, boxOffice: Int) => {
      val guide = TravelGuide(
        Attraction(
          "Yellowstone National Park",
          None,
          Location(LocationId("Q1214"), "Wyoming", 586107)
        ),
        List(Artist("Chris LeDoux", followers), Movie("The Hateful Eight", boxOffice))
      )

      val score = guideScore(guide)

      // the score needs to be at least: 20 = 0 (no description) + 10 (1 artist) + 10 (10 movie)
      // but maximum of 50 in a case when there are lots of followers and high box office earnings
      // PROBLEM: the following will sometimes fail when boxOffice < -10_000_000
      // assert(score >= 20 && score <= 50)
      println(s"Testing against an artist with $followers followers and a movie with $boxOffice box office: $score")
    })

    // we need to decide is it a TEST PROBLEM or IMPLEMENTATION BUG
    // if it's an IMPLEMENTATION BUG, we can use defensive programming in the implementation
    // if it's a TEST PROBLEM, we need to tweak the test to not include negative values!
    // SOLUTION: use generators
    val nonNegativeInt: Gen[Int] = Gen.chooseNum(0, Int.MaxValue)

    forAll(nonNegativeInt, nonNegativeInt)((followers: Int, boxOffice: Int) => {
      val guide = TravelGuide(
        Attraction(
          "Yellowstone National Park",
          None,
          Location(LocationId("Q1214"), "Wyoming", 586107)
        ),
        List(Artist("Chris LeDoux", followers), Movie("The Hateful Eight", boxOffice))
      )

      val score = guideScore(guide)

      // the score needs to be at least: 20 = 0 (no description) + 10 (1 artist) + 10 (10 movie)
      // but maximum of 50 in a case when there are lots of followers and high box office earnings
      assert(score >= 20 && score <= 50)
    })
  }

  // we can compose different generators using flatMap
  val nonNegativeInt: Gen[Int] = Gen.chooseNum(0, Int.MaxValue)

  val randomArtist: Gen[Artist] = for {
    name      <- Gen.identifier // introduce Gen.identifier
    followers <- nonNegativeInt
  } yield Artist(name, followers)

  test("guide score should always be between 10 and 25 if there is just a single artist") {
    forAll(randomArtist)((artist: Artist) => {
      val guide = TravelGuide(
        Attraction("Yellowstone National Park", None, Location(LocationId("Q1214"), "Wyoming", 586107)),
        List(artist)
      )

      val score = guideScore(guide)

      // there is no description and just a single artist (10) with random number of followers (0-15)
      assert(score >= 10 && score <= 25)
    })
  }

  // we can build big generators from small ones in the same way
  val randomArtists: Gen[List[Artist]] = for {
    numberOfArtists <- Gen.chooseNum(0, 100)
    artists         <- Gen.listOfN(numberOfArtists, randomArtist)
  } yield artists

  test("guide score should always be between 0 and 55 if there is no description and no movies") {
    forAll(randomArtists)((artists: List[Artist]) => {
      val guide = TravelGuide(
        Attraction("Yellowstone National Park", None, Location(LocationId("Q1214"), "Wyoming", 586107)),
        artists
      )

      // 40 points if 4 artists or more + 15 if 1_500_000 followers or more

      // the following will fail:
      // val score = guideScore(guide)
      // assert(score >= 0 && score <= 55)

      // fixed version will not:
      val score = ch12_TravelGuide.guideScore(guide)
      assert(score >= 0 && score <= 55)
    })
  }

  // Coffee Break: Property-based tests
  val randomMovie: Gen[Movie] = for {
    name      <- Gen.identifier
    boxOffice <- nonNegativeInt
  } yield Movie(name, boxOffice)

  val randomMovies: Gen[List[Movie]] = for {
    numberOfMovies <- Gen.chooseNum(0, 100)
    movies         <- Gen.listOfN(numberOfMovies, randomMovie)
  } yield movies

  val randomPopCultureSubjects: Gen[List[PopCultureSubject]] = for {
    movies  <- randomMovies
    artists <- randomArtists
  } yield movies.appendedAll(artists)

  test("guide score should always be between 0 and 70 if it only contains pop culture subjects") {
    forAll(randomPopCultureSubjects)((popCultureSubjects: List[PopCultureSubject]) => {
      val guide = TravelGuide(
        Attraction("Yellowstone National Park", None, Location(LocationId("Q1214"), "Wyoming", 586107)),
        popCultureSubjects
      )

      // min. 0 if the list of pop culture subjects is empty (there is never any description)
      // max. 70 if there are more than four subjects with big followings
      val score = ch12_TravelGuide.guideScore(guide)
      assert(score >= 0 && score <= 70)
    })
  }

  /**
    *  STEP 3a: test side effects without using any mocking libraries
    *  - testing using stubs
    */
  test("travel guide should include artists originating from the attraction's location") {
    // given an external data source with an attraction named "Tower Bridge"
    // at a location that brought us "Queen"
    val attractionName = "Tower Bridge"
    val london         = Location(LocationId("Q84"), "London", 8_908_081)
    val queen          = Artist("Queen", 2_050_559)
    val dataAccess = new DataAccess {
      def findAttractions(name: String, ordering: AttractionOrdering, limit: Int): IO[List[Attraction]] =
        IO.pure(List(Attraction(attractionName, None, london)))

      def findArtistsFromLocation(locationId: LocationId, limit: Int): IO[List[Artist]] =
        if (locationId == london.id) IO.pure(List(queen)) else IO.pure(List.empty)

      def findMoviesAboutLocation(locationId: LocationId, limit: Int): IO[List[Movie]] = IO.pure(List.empty)
    }

    // when we want to get a travel guide for this attraction
    val guide: Option[TravelGuide] = travelGuide(dataAccess, attractionName).unsafeRunSync()

    // then we get a travel guide with "Queen"
    assert(guide.exists(_.subjects == List(queen)))
  }

  // Practicing stubbing external data using IO
  test("travel guide should include movies set in the attraction's location") {
    // given an external data source with an attraction named "Golden Gate Bridge"
    // at a location where "Inside Out" was taking place in
    val attractionName = "Golden Gate Bridge"
    val sanFrancisco   = Location(LocationId("Q62"), "San Francisco", 883_963)
    val insideOut      = Movie("Inside Out", 857_611_174)
    val dataAccess = new DataAccess {
      def findAttractions(name: String, ordering: AttractionOrdering, limit: Int): IO[List[Attraction]] =
        IO.pure(List(Attraction(attractionName, None, sanFrancisco)))

      def findArtistsFromLocation(locationId: LocationId, limit: Int): IO[List[Artist]] = IO.pure(List.empty)

      def findMoviesAboutLocation(locationId: LocationId, limit: Int): IO[List[Movie]] =
        if (locationId == sanFrancisco.id) IO.pure(List(insideOut)) else IO.pure(List.empty)
    }

    // when we want to get a travel guide for this attraction
    val guide: Option[TravelGuide] = travelGuide(dataAccess, attractionName).unsafeRunSync()

    // then we get a travel guide that includes the "Inside Out" movie
    assert(guide.exists(_.subjects == List(insideOut)))
  }

  /**
    *  STEP 3b: test side effects without using any mocking libraries
    *  - testing using a real SPARQL server
    */
  def localSparqlServer: Resource[IO, FusekiServer] = {
    val start: IO[FusekiServer] = IO.blocking {
      val model  = RDFDataMgr.loadModel(getClass.getResource("testdata.ttl").toString)
      val ds     = DatasetFactory.create(model)
      val server = FusekiServer.create.add("/test", ds).build
      server.start()
      server
    }

    Resource.make(start)(server => IO.blocking(server.stop()))
  }

  def testServerConnection: Resource[IO, RDFConnection] =
    for {
      localServer <- localSparqlServer
      connection  <- ch12_TravelGuide.connectionResource(localServer.serverURL(), "test")
    } yield connection

  test("data access layer should fetch attractions from a real SPARQL server") {
    val result: List[Attraction] = testServerConnection
      .use(connection => {
        val dataAccess = getSparqlDataAccess(execQuery(connection))
        dataAccess.findAttractions("Bridge of Sighs", ByLocationPopulation, 5)
      })
      .unsafeRunSync()

    assert(result.exists(_.name == "Bridge of Sighs") && result.size <= 5)
  }

  test("data access layer should allow requesting attractions sorted by location population") {
    val attractions: List[Attraction] = testServerConnection
      .use(connection => {
        val dataAccess = getSparqlDataAccess(execQuery(connection))
        dataAccess.findAttractions("Yellowstone", ByLocationPopulation, 5)
      })
      .unsafeRunSync()

    val locations = attractions.map(_.location)

    assert(locations.size == 3 && locations == locations.sortBy(_.population).reverse)
  }

  test("data access layer should allow requesting attractions sorted by name") {
    val attractions: List[Attraction] = testServerConnection
      .use(connection => {
        val dataAccess = getSparqlDataAccess(execQuery(connection))
        dataAccess.findAttractions("National Park", ByName, 5) // looking for national parks
      })
      .unsafeRunSync()

    assert(attractions.size == 5 && attractions.map(_.name) == attractions.sortBy(_.name).map(_.name))
  }

  test("data access layer should fetch artists from a real SPARQL server") {
    val artists: List[Artist] = testServerConnection
      .use(connection => {
        val dataAccess = getSparqlDataAccess(execQuery(connection))
        dataAccess.findArtistsFromLocation(LocationId("Q641"), 1) // Venice id
      })
      .unsafeRunSync()

    assert(artists.map(_.name) == List("Talco"))
  }

  test("data access layer should fetch movies from a real SPARQL server") {
    val movies: List[Movie] = testServerConnection
      .use(connection => {
        val dataAccess = getSparqlDataAccess(execQuery(connection))
        dataAccess.findMoviesAboutLocation(LocationId("Q641"), 2) // Venice id
      })
      .unsafeRunSync()

    assert(movies.map(_.name) == List("Spider-Man: Far from Home", "Casino Royale"))
    assert(
      movies.forall(_.boxOffice > 0)
    ) // TODO: another test, maybe an exercise/practicing? maybe Resource[IO, DataAccess]?
  }

  test("data access layer should accept and relay limit values to a real SPARQL server") {
    // this test shows that you can use property-based checks in integrations tests as well
    // it will fail with and internal SPARQL server error if we use a generator with negative ints
    // (negative limits are not supported) and the test itself is longer than others but it may be useful in some cases
    forAll(Gen.chooseNum(0, Int.MaxValue))((limit: Int) => {
      val movies: List[Movie] = testServerConnection
        .use(connection => {
          val dataAccess = getSparqlDataAccess(execQuery(connection))
          dataAccess.findMoviesAboutLocation(LocationId("Q641"), limit) // Venice id
        })
        .unsafeRunSync()

      assert(movies.size <= limit)
    })
  }

  /**
    * STEP 4: develop new functionalities in a test-driven way
    */
  // this function is very straightforward and cleans up the following tests significantly.
  // What's important is what's left in the tests, how they document the function and convey this new functionality.
  // again: using such a helper function make the tests more readable, because readers can focus on the functionality
  def dataAccessStub(
      attractions: IO[List[Attraction]],
      artists: IO[List[Artist]],
      movies: IO[List[Movie]]
  ): DataAccess =
    new DataAccess {
      def findAttractions(name: String, ordering: AttractionOrdering, limit: Int): IO[List[Attraction]] = attractions
      def findArtistsFromLocation(locationId: LocationId, limit: Int): IO[List[Artist]]                 = artists
      def findMoviesAboutLocation(locationId: LocationId, limit: Int): IO[List[Movie]]                  = movies
    }

  // using a named value makes tests more readable, too!
  val yellowstone: Attraction = Attraction(
    "Yellowstone National Park",
    Some("first national park in the world"),
    Location(LocationId("Q1214"), "Wyoming", 586107)
  )

  val hatefulEight: Movie = Movie("The Hateful Eight", 155760117)
  val heavensGate: Movie  = Movie("Heaven's Gate", 3484331)

  test("travelGuide should return a search report if it can't find a good-enough guide") {
    // given an external data source with a single attraction, no movies, no artists and no IO failures
    val dataAccess = dataAccessStub(IO.pure(List(yellowstone)), IO.pure(List.empty), IO.pure(List.empty))

    // when we execute the travelGuide function for "Yellowstone" (it's just for the show, because no matter what we put here, the data is stubbed)
    val result: Either[SearchReport, TravelGuide] = Version4.travelGuide(dataAccess, "Yellowstone").unsafeRunSync()

    // then we don't get a travel guide but a search report (not enough points)
    assert(result == Left(SearchReport(List(TravelGuide(yellowstone, List.empty)), errors = List.empty)))
  }

  test("travelGuide should return a travel guide if there is enough data in the external data source") {
    // given an external data source with a single attraction, two movies, no artists and no IO failures
    val dataAccess =
      dataAccessStub(IO.pure(List(yellowstone)), IO.pure(List.empty), IO.pure(List(hatefulEight, heavensGate)))

    // when we execute the travelGuide function for "Yellowstone"
    val result: Either[SearchReport, TravelGuide] = Version4.travelGuide(dataAccess, "Yellowstone").unsafeRunSync()

    // then we get a proper travel guide because it has a high score (> 55 points)
    assert(result == Right(TravelGuide(yellowstone, List(hatefulEight, heavensGate))))
  }

  test("travelGuide should return a search report with no guides if it can't fetch the attractions due to IO failures") {
    // given an external data source that fails when trying to fetch attractions
    val dataAccess =
      dataAccessStub(IO.delay(throw new Exception("fetching failed")), IO.pure(List.empty), IO.pure(List.empty))

    // when we execute the travelGuide function for "Yellowstone"
    val result: Either[SearchReport, TravelGuide] = Version4.travelGuide(dataAccess, "Yellowstone").unsafeRunSync()

    // then we don't get a search report with no bad guides and list of errors
    assert(result == Left(SearchReport(badGuides = List.empty, errors = List("fetching failed"))))
  }

  val yosemite: Attraction = Attraction(
    "Yosemite National Park",
    Some("national park in California, United States"),
    Location(LocationId("Q109661"), "Madera County", 157327)
  )

  test(
    "travelGuide should return a search report with some guides if it can't sometimes fetch artists due to IO failures"
  ) {
    // given an external data source that fails when trying to fetch artists for "Yosemite"
    val dataAccess =
      new DataAccess { // it's more complicated case, so it's better to define it directly, without helpers
        def findAttractions(name: String, ordering: AttractionOrdering, limit: Int): IO[List[Attraction]] =
          IO.pure(List(yosemite, yellowstone))
        def findArtistsFromLocation(locationId: LocationId, limit: Int): IO[List[Artist]] =
          if (locationId == yosemite.location.id) IO.delay(throw new Exception("Yosemite artists fetching failed"))
          else IO.pure(List.empty)
        def findMoviesAboutLocation(locationId: LocationId, limit: Int): IO[List[Movie]] =
          IO.pure(List.empty)
      }

    // when we execute the travelGuide function for "National Park"
    val result: Either[SearchReport, TravelGuide] = Version5.travelGuide(dataAccess, "National Park").unsafeRunSync()

    // then we get a search report with one bad guide (< 55) and list of errors
    assert(
      result == Left(
          SearchReport(
            badGuides = List(TravelGuide(yellowstone, List.empty)),
            errors = List("Yosemite artists fetching failed")
          )
        )
    )
  } // this test will fail for Version4, but passes for Version5 which was written after Version4 and this test (TDD)

  /**
    * BONUS: semi-end-to-end tests. Tests that test the whole application without the real external service.
    * It can verify that everything works correctly by looking at some simple happy paths.
    */
  def testLocation(id: Int): Location =
    Location(
      LocationId(s"location-id-$id"),
      s"location-name-$id",
      1000
    ) // using such a helper function makes the test more readable (see below for more examples)

  test("locations that have artists should be preferred") {
    // given an external data source with an attraction named "test-attraction" at many locations,
    // only one of them being an origin for some artists
    val attractionName      = "test-attraction"
    val locations           = List.range(0, 100).map(testLocation) // one hundred locations
    val locationWithArtists = testLocation(1) // only the second one has artists in the data access layer below
    val dataAccess =
      new DataAccess { // we could use a helper function that takes locations and return dataAccess, but this is more straightforward: tests should not use too much magic
        def findAttractions(name: String, ordering: AttractionOrdering, limit: Int): IO[List[Attraction]] =
          IO.pure(locations.map(location => Attraction(attractionName, None, location)))

        def findArtistsFromLocation(locationId: LocationId, limit: Int): IO[List[Artist]] =
          if (locationId == locationWithArtists.id)
            IO.pure(List.range(0, limit).map(number => Artist(s"artist-$number", number * 1000)))
          else
            IO.pure(List.empty)

        def findMoviesAboutLocation(locationId: LocationId, limit: Int): IO[List[Movie]] = IO.pure(List.empty)
      }

    // when we want to get a travel guide for this attraction
    val guide: Option[TravelGuide] = travelGuide(dataAccess, attractionName).unsafeRunSync()

    // then we get a travel guide for a location that has artists (location.id == 1)
    assert(guide.exists(_.attraction.location.id == locationWithArtists.id))
  }

  test("locations that were used in movies should be preferred") {
    // given an external data source with an attraction named "test-attraction" at many locations,
    // only one of them being used in multiple movies
    val attractionName     = "test-attraction"
    val locations          = List.range(0, 100).map(testLocation) // one hundred locations
    val locationWithMovies = testLocation(1) // only the second one has movies in the data access layer below
    val dataAccess =
      new DataAccess {
        def findAttractions(name: String, ordering: AttractionOrdering, limit: Int): IO[List[Attraction]] =
          IO.pure(locations.map(location => Attraction(attractionName, None, location)))

        def findArtistsFromLocation(locationId: LocationId, limit: Int): IO[List[Artist]] = IO.pure(List.empty)

        def findMoviesAboutLocation(locationId: LocationId, limit: Int): IO[List[Movie]] =
          if (locationId == locationWithMovies.id)
            IO.pure(List.range(0, limit).map(number => Movie(s"movie-$number", number * 1_000_000)))
          else
            IO.pure(List.empty)
      }

    // when we want to get a travel guide for this attraction
    val guide: Option[TravelGuide] = travelGuide(dataAccess, attractionName).unsafeRunSync()

    // then we get a travel guide for a location that has movies (location.id == 1)
    assert(guide.exists(_.attraction.location.id == locationWithMovies.id))
  }
}
