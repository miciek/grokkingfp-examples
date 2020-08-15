object MusicArtistsSearch extends App {

  // STEP 1: Design using what we know
  {
    case class Artist(
        name: String,
        genre: String,
        origin: String,
        yearsActiveStart: Int,
        isActive: Boolean,
        yearsActiveEnd: Int
    )

    def searchArtists(
        artists: List[Artist],
        genres: List[String],
        locations: List[String],
        searchByActiveYears: Boolean,
        activeAfter: Int,
        activeBefore: Int
    ): List[Artist] = {
      artists.filter(artist =>
        (genres.isEmpty || genres.contains(artist.genre)) &&
        (locations.isEmpty || locations.contains(artist.origin)) &&
        (!searchByActiveYears || ((artist.isActive || artist.yearsActiveEnd >= activeAfter) &&
        (artist.yearsActiveStart <= activeBefore)))
      )
    }

    val artists = List(
      Artist("Metallica", "Heavy Metal", "U.S.", 1983, true, 0),
      Artist("Led Zeppelin", "Hard Rock", "England", 1968, false, 1980),
      Artist("Bee Gees", "Pop", "England", 1958, false, 2003)
    )

    // coffee break cases:
    check { searchArtists(artists, List("Pop"), List("England"), true, 1950, 2020) }.expect {
      List(Artist("Bee Gees", "Pop", "England", 1958, false, 2003))
    }
    check { searchArtists(artists, List.empty, List("England"), true, 1950, 2020) }.expect {
      List(
        Artist("Led Zeppelin", "Hard Rock", "England", 1968, false, 1980),
        Artist("Bee Gees", "Pop", "England", 1958, false, 2003)
      )
    }
    check { searchArtists(artists, List.empty, List.empty, true, 1950, 1982) }.expect {
      List(
        Artist("Led Zeppelin", "Hard Rock", "England", 1968, false, 1980),
        Artist("Bee Gees", "Pop", "England", 1958, false, 2003)
      )
    }
    check { searchArtists(artists, List.empty, List.empty, true, 1983, 2003) }.expect {
      List(
        Artist("Metallica", "Heavy Metal", "U.S.", 1983, true, 0),
        Artist("Bee Gees", "Pop", "England", 1958, false, 2003)
      )
    }
    check { searchArtists(artists, List("Heavy Metal"), List.empty, true, 2019, 2020) }.expect {
      List(
        Artist("Metallica", "Heavy Metal", "U.S.", 1983, true, 0)
      )
    }
    check { searchArtists(artists, List.empty, List("U.S."), true, 1950, 1959) }.expect {
      List.empty
    }
    check { searchArtists(artists, List.empty, List.empty, false, 2019, 2021) }.expect {
      List(
        Artist("Metallica", "Heavy Metal", "U.S.", 1983, true, 0),
        Artist("Led Zeppelin", "Hard Rock", "England", 1968, false, 1980),
        Artist("Bee Gees", "Pop", "England", 1958, false, 2003)
      )
    }

    // additional cases:
    check { searchArtists(artists, List.empty, List.empty, true, 1950, 1959) }.expect {
      List(
        Artist("Bee Gees", "Pop", "England", 1958, false, 2003)
      )
    }
    check { searchArtists(artists, List.empty, List("U.S."), false, 0, 0) }.expect {
      List(
        Artist("Metallica", "Heavy Metal", "U.S.", 1983, true, 0)
      )
    }
  }

  // STEP 1: value classes
  case class Location(name: String)       extends AnyVal
  case class Genre(name: String)          extends AnyVal
  case class YearsActiveStart(value: Int) extends AnyVal
  case class YearsActiveEnd(value: Int)   extends AnyVal

  {
    case class Artist(
        name: String,
        genre: Genre,
        origin: Location,
        yearsActiveStart: YearsActiveStart,
        isActive: Boolean,
        yearsActiveEnd: YearsActiveEnd
    )
    def searchArtists(
        artists: List[Artist],
        genres: List[String],
        locations: List[String],
        searchByActiveYears: Boolean,
        activeAfter: Int,
        activeBefore: Int
    ): List[Artist] = {
      artists.filter(artist =>
        (genres.isEmpty || genres.contains(artist.genre.name)) && // <- using Genre
        (locations.isEmpty || locations
          .contains(artist.origin.name)) && // <- using Location
        (!searchByActiveYears ||
        ((artist.isActive || artist.yearsActiveEnd.value >= activeAfter) && // <- using YearsActiveEnd
        (artist.yearsActiveStart.value <= activeBefore)))                   // <- using YearsActiveStart
      )
    }

    val artists = List(
      Artist("Metallica", Genre("Heavy Metal"), Location("U.S."), YearsActiveStart(1983), true, YearsActiveEnd(0)),
      Artist(
        "Led Zeppelin",
        Genre("Hard Rock"),
        Location("England"),
        YearsActiveStart(1968),
        false,
        YearsActiveEnd(1980)
      ),
      Artist("Bee Gees", Genre("Pop"), Location("England"), YearsActiveStart(1958), false, YearsActiveEnd(2003))
    )

    check { searchArtists(artists, List("Pop"), List("England"), true, 1950, 2020) }.expect {
      List(Artist("Bee Gees", Genre("Pop"), Location("England"), YearsActiveStart(1958), false, YearsActiveEnd(2003)))
    }
  }

  // STEP 2a: Option type
  {
    case class Artist(
        name: String,
        genre: String,
        origin: Location,
        yearsActiveStart: Int,
        yearsActiveEnd: Option[Int]
    )

    def searchArtists(
        artists: List[Artist],
        genres: List[String],
        locations: List[String],
        searchByActiveYears: Boolean,
        activeAfter: Int,
        activeBefore: Int
    ): List[Artist] = {
      artists.filter(artist =>
        (genres.isEmpty || genres.contains(artist.genre)) &&
        (locations.isEmpty || locations.contains(artist.origin.name)) &&
        (!searchByActiveYears ||
        (artist.yearsActiveEnd.forall(_ >= activeAfter) && // <- using Option.forall
        (artist.yearsActiveStart <= activeBefore)))
      )
    }

    val artists = List(
      Artist("Metallica", "Heavy Metal", Location("U.S."), 1983, None),
      Artist("Led Zeppelin", "Hard Rock", Location("England"), 1968, Some(1980)),
      Artist("Bee Gees", "Pop", Location("England"), 1958, Some(2003))
    )

    check { searchArtists(artists, List("Pop"), List("England"), true, 1950, 2020) }.expect {
      List(Artist("Bee Gees", "Pop", Location("England"), 1958, Some(2003)))
    }
  }

  { // Option higher-order functions (see TvShows as well)
    val year: Option[Int]   = Some(996)
    val noYear: Option[Int] = None

    // map
    check(year.map(_ * 2)).expect(Some(1992))
    check(noYear.map(_ * 2)).expect(None)

    // flatMap
    check(year.flatMap(y => Some(y * 2))).expect(Some(1992))
    check(noYear.flatMap(y => Some(y * 2))).expect(None)
    check(year.flatMap(y => None)).expect(None)
    check(noYear.flatMap(y => None)).expect(None)

    // filter
    check(year.filter(_ < 2020)).expect(Some(996))
    check(noYear.filter(_ < 2020)).expect(None)
    check(year.filter(_ > 2020)).expect(None)
    check(noYear.filter(_ > 2020)).expect(None)

    // forall
    check(year.forall(_ < 2020)).expect(true)
    check(noYear.forall(_ < 2020)).expect(true)
    check(year.forall(_ > 2020)).expect(false)
    check(noYear.forall(_ > 2020)).expect(true)

    // exists
    check(year.exists(_ < 2020)).expect(true)
    check(noYear.exists(_ < 2020)).expect(false)
    check(year.exists(_ > 2020)).expect(false)
    check(noYear.exists(_ > 2020)).expect(false)
  }

  { // Coffee Break: forall/exists/contains
    case class User(name: String, city: Option[String], favoriteArtists: List[String])

    val users = List(
      User("Alice", Some("Melbourne"), List("Bee Gees")),
      User("Bob", Some("Lagos"), List("Bee Gees")),
      User("Eve", Some("Tokyo"), List.empty),
      User("Mallory", None, List("Metallica", "Bee Gees")),
      User("Trent", Some("Buenos Aires"), List("Led Zeppelin"))
    )

    // 1. users that haven't specified their city or live in Melbourne
    def f1(users: List[User]): List[User] = {
      users.filter(_.city.forall(_ == "Melbourne"))
    }
    check(f1(users)).expect(_.map(_.name) == List("Alice", "Mallory"))

    // 2. users that live in Lagos
    def f2(users: List[User]): List[User] = {
      users.filter(_.city.contains("Lagos"))
    }
    check(f2(users)).expect(_.map(_.name) == List("Bob"))

    // 3. users that like Bee Gees
    def f3(users: List[User]): List[User] = {
      users.filter(_.favoriteArtists.contains("Bee Gees"))
    }
    check(f3(users)).expect(_.map(_.name) == List("Alice", "Bob", "Mallory"))

    // 4. users that live in cities that start with a letter T
    def f4(users: List[User]): List[User] = {
      users.filter(_.city.exists(_.startsWith("T")))
    }
    check(f4(users)).expect(_.map(_.name) == List("Eve"))

    // 5. users that only like artists that have a name longer than 8 characters (or no favorite artists at all)
    def f5(users: List[User]): List[User] = {
      users.filter(_.favoriteArtists.forall(_.length > 8))
    }
    check(f5(users)).expect(_.map(_.name) == List("Eve", "Trent"))

    // 6. users that like some artists whose names start with M
    def f6(users: List[User]): List[User] = {
      users.filter(_.favoriteArtists.exists(_.startsWith("M")))
    }
    check(f6(users)).expect(_.map(_.name) == List("Mallory"))
  }

  // STEP 2b: new case class
  case class PeriodInYears(start: Int, end: Option[Int])

  {
    case class Artist(
        name: String,
        genre: String,
        origin: Location,
        yearsActive: PeriodInYears
    )

    def searchArtists(
        artists: List[Artist],
        genres: List[String],
        locations: List[String],
        searchByActiveYears: Boolean,
        activeAfter: Int,
        activeBefore: Int
    ): List[Artist] = {
      artists.filter(artist =>
        (genres.isEmpty || genres.contains(artist.genre)) &&
        (locations.isEmpty || locations.contains(artist.origin.name)) &&
        (!searchByActiveYears ||
        (artist.yearsActive.end.forall(_ >= activeAfter) && // <- using Option.forall
        (artist.yearsActive.start <= activeBefore)))
      )
    }

    val artists = List(
      Artist("Metallica", "Heavy Metal", Location("U.S."), PeriodInYears(1983, None)),
      Artist("Led Zeppelin", "Hard Rock", Location("England"), PeriodInYears(1968, Some(1980))),
      Artist("Bee Gees", "Pop", Location("England"), PeriodInYears(1958, Some(2003)))
    )

    check { searchArtists(artists, List("Pop"), List("England"), true, 1950, 2020) }.expect {
      List(Artist("Bee Gees", "Pop", Location("England"), PeriodInYears(1958, Some(2003))))
    }
  }

  // STEP 3: sealed trait
  sealed trait MusicGenre
  case object HeavyMetal extends MusicGenre
  case object Pop        extends MusicGenre
  case object HardRock   extends MusicGenre

  {
    case class Artist(
        name: String,
        genre: MusicGenre,
        origin: Location,
        yearsActive: PeriodInYears
    )

    def searchArtists(
        artists: List[Artist],
        genres: List[MusicGenre], // <- now we need to make sure only valid genres are searched for
        locations: List[String],
        searchByActiveYears: Boolean,
        activeAfter: Int,
        activeBefore: Int
    ): List[Artist] = {
      artists.filter(artist =>
        (genres.isEmpty || genres.contains(artist.genre)) && // no change needed
        (locations.isEmpty || locations.contains(artist.origin.name)) &&
        (!searchByActiveYears ||
        (artist.yearsActive.end.forall(_ >= activeAfter) &&
        (artist.yearsActive.start <= activeBefore)))
      )
    }

    val artists = List(
      Artist("Metallica", HeavyMetal, Location("U.S."), PeriodInYears(1983, None)),
      Artist("Led Zeppelin", HardRock, Location("England"), PeriodInYears(1968, Some(1980))),
      Artist("Bee Gees", Pop, Location("England"), PeriodInYears(1958, Some(2003)))
    )

    check { searchArtists(artists, List(Pop), List("England"), true, 1950, 2020) }.expect {
      List(Artist("Bee Gees", Pop, Location("England"), PeriodInYears(1958, Some(2003))))
    }
  }

  // STEP 4: ADT
  sealed trait YearsActive
  case class StillActive(since: Int)             extends YearsActive
  case class ActiveBetween(start: Int, end: Int) extends YearsActive

  {
    case class Artist(name: String, genre: MusicGenre, origin: Location, yearsActive: YearsActive)

    def wasArtistActive(artist: Artist, yearStart: Int, yearEnd: Int): Boolean = {
      artist.yearsActive match {
        case StillActive(since)        => since <= yearEnd
        case ActiveBetween(start, end) => start <= yearEnd && end >= yearStart
      }
    }

    def searchArtistsRaw(
        artists: List[Artist],
        genres: List[MusicGenre],
        locations: List[Location],
        searchByActiveYears: Boolean,
        activeAfter: Int,
        activeBefore: Int
    ): List[Artist] = {
      artists.filter(artist =>
        (genres.isEmpty || genres.contains(artist.genre)) &&
        (locations.isEmpty || locations.contains(artist.origin)) &&
        (!searchByActiveYears || wasArtistActive(artist, activeAfter, activeBefore))
      )
    }

    // Modeling conditions as ADT:
    sealed trait SearchCondition
    case class SearchByGenre(genres: List[MusicGenre])   extends SearchCondition
    case class SearchByOrigin(locations: List[Location]) extends SearchCondition
    case class SearchByActiveYears(start: Int, end: Int) extends SearchCondition

    def searchArtists(
        artists: List[Artist],
        requiredConditions: List[SearchCondition]
    ): List[Artist] = {
      artists.filter(artist =>
        requiredConditions.forall(condition =>
          condition match {
            case SearchByGenre(genres)           => genres.contains(artist.genre)
            case SearchByOrigin(locations)       => locations.contains(artist.origin)
            case SearchByActiveYears(start, end) => wasArtistActive(artist, start, end)
          }
        )
      )
    }

    val artists = List(
      Artist("Metallica", HeavyMetal, Location("U.S."), StillActive(1983)),
      Artist("Led Zeppelin", HardRock, Location("England"), ActiveBetween(1968, 1980)),
      Artist("Bee Gees", Pop, Location("England"), ActiveBetween(1958, 2003))
    )

    // searchArtistsRaw check
    check(
      searchArtistsRaw(
        artists,
        List(Pop),
        List(Location("England")),
        true,
        1950,
        2020
      )
    ).expect {
      List(
        Artist("Bee Gees", Pop, Location("England"), ActiveBetween(1958, 2003))
      )
    }

    check(
      searchArtists(
        artists,
        List(
          SearchByGenre(List(Pop)),
          SearchByOrigin(List(Location("England"))),
          SearchByActiveYears(1950, 2020)
        )
      )
    ).expect {
      List(
        Artist("Bee Gees", Pop, Location("England"), ActiveBetween(1958, 2003))
      )
    }

    check(
      searchArtists(
        artists,
        List(
          SearchByOrigin(List(Location("England"))),
          SearchByActiveYears(1950, 2020)
        )
      )
    ).expect {
      List(
        Artist("Led Zeppelin", HardRock, Location("England"), ActiveBetween(1968, 1980)),
        Artist("Bee Gees", Pop, Location("England"), ActiveBetween(1958, 2003))
      )
    }

    check(
      searchArtists(
        artists,
        List(
          SearchByActiveYears(1950, 2020)
        )
      )
    ).expect {
      List(
        Artist("Metallica", HeavyMetal, Location("U.S."), StillActive(1983)),
        Artist("Led Zeppelin", HardRock, Location("England"), ActiveBetween(1968, 1980)),
        Artist("Bee Gees", Pop, Location("England"), ActiveBetween(1958, 2003))
      )
    }

    check(
      searchArtists(
        artists,
        List(
          SearchByActiveYears(1983, 2003)
        )
      )
    ).expect {
      List(
        Artist("Metallica", HeavyMetal, Location("U.S."), StillActive(1983)),
        Artist("Bee Gees", Pop, Location("England"), ActiveBetween(1958, 2003))
      )
    }

    check(
      searchArtists(
        artists,
        List(
          SearchByGenre(List(HeavyMetal)),
          SearchByActiveYears(2019, 2020)
        )
      )
    ).expect {
      List(
        Artist("Metallica", HeavyMetal, Location("U.S."), StillActive(1983))
      )
    }

    check(
      searchArtists(
        artists,
        List(
          SearchByActiveYears(1950, 1959)
        )
      )
    ).expect {
      List(
        Artist("Bee Gees", Pop, Location("England"), ActiveBetween(1958, 2003))
      )
    }

    check(
      searchArtists(
        artists,
        List(
          SearchByOrigin(List(Location("U.S."))),
          SearchByActiveYears(1950, 1959)
        )
      )
    ).expect {
      List.empty
    }

    { // Practicing pattern matching
      def activeLength(artist: Artist, currentYear: Int): Int = {
        artist.yearsActive match {
          case StillActive(since)        => currentYear - since
          case ActiveBetween(start, end) => end - start
        }
      }

      check {
        activeLength(Artist("Metallica", HeavyMetal, Location("U.S."), StillActive(1983)), 2020)
      }.expect(37)
      check {
        activeLength(Artist("Led Zeppelin", HardRock, Location("England"), ActiveBetween(1968, 1980)), 2020)
      }.expect(12)
      check {
        activeLength(Artist("Bee Gees", Pop, Location("England"), ActiveBetween(1958, 2003)), 2020)
      }.expect(45)
    }
  }

  // NEW REQUIREMENTS:
  {
    case class Artist(name: String, genre: MusicGenre, origin: Location, yearsActive: List[PeriodInYears])

    sealed trait SearchCondition
    case class SearchByGenre(genres: List[MusicGenre])          extends SearchCondition
    case class SearchByOrigin(locations: List[Location])        extends SearchCondition
    case class SearchByActiveYears(start: Int, end: Int)        extends SearchCondition
    case class SearchByActivityLength(howLong: Int, until: Int) extends SearchCondition

    def searchArtists(
        artists: List[Artist],
        requiredConditions: List[SearchCondition]
    ): List[Artist] = {
      artists.filter(artist =>
        requiredConditions.forall(nextCondition =>
          nextCondition match {
            case SearchByGenre(genres)     => genres.contains(artist.genre)
            case SearchByOrigin(locations) => locations.contains(artist.origin)
            case SearchByActiveYears(start, end) =>
              artist.yearsActive.exists(period => period.start <= end && period.end.forall(_ >= start))
            case SearchByActivityLength(howLong, until) =>
              val totalActive =
                artist.yearsActive.map(period => period.end.getOrElse(until) - period.start).foldLeft(0)(_ + _)
              totalActive >= howLong
          }
        )
      )
    }

    val artists = List(
      Artist("Metallica", HeavyMetal, Location("U.S."), List(PeriodInYears(1983, None))),
      Artist("Led Zeppelin", HardRock, Location("England"), List(PeriodInYears(1968, Some(1980)))),
      Artist(
        "Bee Gees",
        Pop,
        Location("England"),
        List(PeriodInYears(1958, Some(2003)), PeriodInYears(2009, Some(2012)))
      )
    )

    check(
      searchArtists(
        artists,
        List(
          SearchByGenre(List(Pop)),
          SearchByOrigin(List(Location("England"))),
          SearchByActiveYears(1950, 2020)
        )
      )
    ).expect {
      List(
        Artist(
          "Bee Gees",
          Pop,
          Location("England"),
          List(PeriodInYears(1958, Some(2003)), PeriodInYears(2009, Some(2012)))
        )
      )
    }

    check(
      searchArtists(
        artists,
        List(
          SearchByOrigin(List(Location("England"))),
          SearchByActiveYears(1950, 2020)
        )
      )
    ).expect {
      List(
        Artist("Led Zeppelin", HardRock, Location("England"), List(PeriodInYears(1968, Some(1980)))),
        Artist(
          "Bee Gees",
          Pop,
          Location("England"),
          List(PeriodInYears(1958, Some(2003)), PeriodInYears(2009, Some(2012)))
        )
      )
    }

    check(
      searchArtists(
        artists,
        List(
          SearchByActiveYears(1950, 2020)
        )
      )
    ).expect {
      List(
        Artist("Metallica", HeavyMetal, Location("U.S."), List(PeriodInYears(1983, None))),
        Artist("Led Zeppelin", HardRock, Location("England"), List(PeriodInYears(1968, Some(1980)))),
        Artist(
          "Bee Gees",
          Pop,
          Location("England"),
          List(PeriodInYears(1958, Some(2003)), PeriodInYears(2009, Some(2012)))
        )
      )
    }

    check(
      searchArtists(
        artists,
        List(
          SearchByActiveYears(1983, 2003)
        )
      )
    ).expect {
      List(
        Artist("Metallica", HeavyMetal, Location("U.S."), List(PeriodInYears(1983, None))),
        Artist(
          "Bee Gees",
          Pop,
          Location("England"),
          List(PeriodInYears(1958, Some(2003)), PeriodInYears(2009, Some(2012)))
        )
      )
    }

    check(
      searchArtists(
        artists,
        List(
          SearchByActiveYears(2019, 2020)
        )
      )
    ).expect {
      List(
        Artist("Metallica", HeavyMetal, Location("U.S."), List(PeriodInYears(1983, None)))
      )
    }

    check(
      searchArtists(
        artists,
        List(
          SearchByActiveYears(1950, 1959)
        )
      )
    ).expect {
      List(
        Artist(
          "Bee Gees",
          Pop,
          Location("England"),
          List(PeriodInYears(1958, Some(2003)), PeriodInYears(2009, Some(2012)))
        )
      )
    }

    check(
      searchArtists(
        artists,
        List(
          SearchByActivityLength(48, 2020)
        )
      )
    ).expect {
      List(
        Artist(
          "Bee Gees",
          Pop,
          Location("England"),
          List(PeriodInYears(1958, Some(2003)), PeriodInYears(2009, Some(2012)))
        )
      )
    }

    check(
      searchArtists(
        artists,
        List(
          SearchByActivityLength(48, 2020)
        )
      )
    ).expect {
      List(
        Artist(
          "Bee Gees",
          Pop,
          Location("England"),
          List(PeriodInYears(1958, Some(2003)), PeriodInYears(2009, Some(2012)))
        )
      )
    }

    check(
      searchArtists(
        artists,
        List(
          SearchByOrigin(List(Location("U.S."))),
          SearchByActivityLength(40, 2020)
        )
      )
    ).expect {
      List.empty
    }

    check(
      searchArtists(
        artists,
        List(
          SearchByOrigin(List(Location("U.S."))),
          SearchByActivityLength(37, 2020)
        )
      )
    ).expect {
      List(Artist("Metallica", HeavyMetal, Location("U.S."), List(PeriodInYears(1983, None))))
    }

    check(
      searchArtists(
        artists,
        List(
          SearchByOrigin(List(Location("U.S."), Location("England"))),
          SearchByActivityLength(37, 2020)
        )
      )
    ).expect {
      List(
        Artist("Metallica", HeavyMetal, Location("U.S."), List(PeriodInYears(1983, None))),
        Artist(
          "Bee Gees",
          Pop,
          Location("England"),
          List(PeriodInYears(1958, Some(2003)), PeriodInYears(2009, Some(2012)))
        )
      )
    }
  }
}
