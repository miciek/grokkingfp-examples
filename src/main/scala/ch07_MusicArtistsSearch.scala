object ch07_MusicArtistsSearch extends App {
  // STEP 0: Design using what we know (primitive types)
  object Version0 {
    case class Artist(
        name: String,
        genre: String,
        origin: String,
        yearsActiveStart: Int,
        isActive: Boolean,
        yearsActiveEnd: Int
    )

    val artists = List(
      Artist("Metallica", "Heavy Metal", "U.S.", 1981, true, 0),
      Artist("Led Zeppelin", "Hard Rock", "England", 1968, false, 1980),
      Artist("Bee Gees", "Pop", "England", 1958, false, 2003)
    )

    def searchArtists(
        artists: List[Artist],
        genres: List[String],
        locations: List[String],
        searchByActiveYears: Boolean,
        activeAfter: Int,
        activeBefore: Int
    ): List[Artist] = artists.filter(artist =>
      (genres.isEmpty || genres.contains(artist.genre)) &&
        (locations.isEmpty || locations.contains(artist.origin)) &&
        (!searchByActiveYears || (artist.isActive || artist.yearsActiveEnd >= activeAfter) &&
          artist.yearsActiveStart <= activeBefore)
    )
  }

  {
    import Version0._

    // coffee break cases:
    searchArtists(artists, List("Pop"), List("England"), true, 1950, 2022) === List(Artist(
      "Bee Gees",
      "Pop",
      "England",
      1958,
      false,
      2003
    ))

    searchArtists(artists, List.empty, List("England"), true, 1950, 2022) ===
      List(
        Artist("Led Zeppelin", "Hard Rock", "England", 1968, false, 1980),
        Artist("Bee Gees", "Pop", "England", 1958, false, 2003)
      )

    searchArtists(artists, List.empty, List.empty, true, 1981, 2003) ===
      List(
        Artist("Metallica", "Heavy Metal", "U.S.", 1981, true, 0),
        Artist("Bee Gees", "Pop", "England", 1958, false, 2003)
      )

    searchArtists(artists, List.empty, List("U.S."), false, 0, 0) ===
      List(
        Artist("Metallica", "Heavy Metal", "U.S.", 1981, true, 0)
      )

    searchArtists(artists, List.empty, List.empty, false, 2019, 2022) ===
      List(
        Artist("Metallica", "Heavy Metal", "U.S.", 1981, true, 0),
        Artist("Led Zeppelin", "Hard Rock", "England", 1968, false, 1980),
        Artist("Bee Gees", "Pop", "England", 1958, false, 2003)
      )

    // additional cases:
    searchArtists(artists, List.empty, List("U.S."), true, 1950, 1959) === List.empty

    searchArtists(artists, List.empty, List.empty, true, 1950, 1979) ===
      List(
        Artist("Led Zeppelin", "Hard Rock", "England", 1968, false, 1980),
        Artist("Bee Gees", "Pop", "England", 1958, false, 2003)
      )

    searchArtists(artists, List.empty, List.empty, true, 1950, 1959) ===
      List(
        Artist("Bee Gees", "Pop", "England", 1958, false, 2003)
      )

    searchArtists(artists, List("Heavy Metal"), List.empty, true, 2019, 2022) ===
      List(
        Artist("Metallica", "Heavy Metal", "U.S.", 1981, true, 0)
      )

  }

  // STEP 1: newtypes
  // In Scala, you could also use opaque types to encode newtypes:
  object model {
    opaque type Location = String
    object Location {
      def apply(value: String): Location       = value // <- you can use a String as a Location only in the scope of model
      extension (a: Location) def name: String = a
    }

    // Practicing newtypes
    opaque type Genre = String
    object Genre {
      def apply(value: String): Genre       = value
      extension (a: Genre) def name: String = a
    }

    opaque type YearsActiveStart = Int
    object YearsActiveStart {
      def apply(value: Int): YearsActiveStart        = value
      extension (a: YearsActiveStart) def value: Int = a
    }

    opaque type YearsActiveEnd = Int
    object YearsActiveEnd {
      def apply(value: Int): YearsActiveEnd        = value
      extension (a: YearsActiveEnd) def value: Int = a
    }
  }

  import model._
  val us: Location = Location("U.S.")
  // val wontCompile: Location = "U.S." // <- String can't be used as a Location outside of the scope of model

  object Version1 {
    case class Artist(
        name: String,
        genre: Genre,
        origin: Location,
        yearsActiveStart: YearsActiveStart,
        isActive: Boolean,
        yearsActiveEnd: YearsActiveEnd
    )

    val artists = List(
      Artist("Metallica", Genre("Heavy Metal"), Location("U.S."), YearsActiveStart(1981), true, YearsActiveEnd(0)),
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

    def searchArtists(
        artists: List[Artist],
        genres: List[String],
        locations: List[String],
        searchByActiveYears: Boolean,
        activeAfter: Int,
        activeBefore: Int
    ): List[Artist] = artists.filter(artist =>
      (genres.isEmpty || genres.contains(artist.genre.name)) &&              // <- using Genre
        (locations.isEmpty || locations
          .contains(artist.origin.name)) &&                                  // <- using Location
        (!searchByActiveYears ||
          (artist.isActive || artist.yearsActiveEnd.value >= activeAfter) && // <- using YearsActiveEnd
          artist.yearsActiveStart.value <= activeBefore)                     // <- using YearsActiveStart
    )
  }

  {
    import Version1._

    searchArtists(artists, List("Pop"), List("England"), true, 1950, 2022) ===
      List(Artist("Bee Gees", Genre("Pop"), Location("England"), YearsActiveStart(1958), false, YearsActiveEnd(2003)))
  }

  // STEP 2a: Option type (reverted all newtypes except of origin, because we'll make them better)
  object Version2a {
    case class Artist(
        name: String,
        genre: String,
        origin: Location,
        yearsActiveStart: Int,
        yearsActiveEnd: Option[Int]
    )

    val artists = List(
      Artist("Metallica", "Heavy Metal", Location("U.S."), 1981, None),
      Artist("Led Zeppelin", "Hard Rock", Location("England"), 1968, Some(1980)),
      Artist("Bee Gees", "Pop", Location("England"), 1958, Some(2003))
    )

    def searchArtists(
        artists: List[Artist],
        genres: List[String],
        locations: List[String],
        searchByActiveYears: Boolean,
        activeAfter: Int,
        activeBefore: Int
    ): List[Artist] = artists.filter(artist =>
      (genres.isEmpty || genres.contains(artist.genre)) &&
        (locations.isEmpty || locations.contains(artist.origin.name)) &&
        (!searchByActiveYears ||
          artist.yearsActiveEnd.forall(_ >= activeAfter) && // <- using Option.forall
          artist.yearsActiveStart <= activeBefore)
    )
  }

  {
    import Version2a._

    searchArtists(artists, List("Pop"), List("England"), true, 1950, 2022) ===
      List(Artist("Bee Gees", "Pop", Location("England"), 1958, Some(2003)))
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
    def f1(users: List[User]): List[User] = users.filter(_.city.forall(_ == "Melbourne"))

    check(f1(users)).expectThat(_.map(_.name) == List("Alice", "Mallory"))

    // 2. users that live in Lagos
    def f2(users: List[User]): List[User] = users.filter(_.city.contains("Lagos"))

    check(f2(users)).expectThat(_.map(_.name) == List("Bob"))

    // 3. users that like Bee Gees
    def f3(users: List[User]): List[User] = users.filter(_.favoriteArtists.contains("Bee Gees"))

    check(f3(users)).expectThat(_.map(_.name) == List("Alice", "Bob", "Mallory"))

    // 4. users that live in cities that start with a letter T
    def f4(users: List[User]): List[User] = users.filter(_.city.exists(_.startsWith("T")))

    check(f4(users)).expectThat(_.map(_.name) == List("Eve"))

    // 5. users that only like artists that have a name longer than 8 characters (or no favorite artists at all)
    def f5(users: List[User]): List[User] = users.filter(_.favoriteArtists.forall(_.length > 8))

    check(f5(users)).expectThat(_.map(_.name) == List("Eve", "Trent"))

    // 6. users that like some artists whose names start with M
    def f6(users: List[User]): List[User] = users.filter(_.favoriteArtists.exists(_.startsWith("M")))

    check(f6(users)).expectThat(_.map(_.name) == List("Mallory"))
  }

  // STEP 2b: new product type
  object Version2b_Data {
    case class PeriodInYears(start: Int, end: Option[Int])

    case class Artist(
        name: String,
        genre: String,
        origin: Location,
        yearsActive: PeriodInYears
    )

    val artists = List(
      Artist("Metallica", "Heavy Metal", Location("U.S."), PeriodInYears(1981, None)),
      Artist("Led Zeppelin", "Hard Rock", Location("England"), PeriodInYears(1968, Some(1980))),
      Artist("Bee Gees", "Pop", Location("England"), PeriodInYears(1958, Some(2003)))
    )
  }

  object Version2b_Behavior {
    import Version2b_Data._

    def searchArtists(
        artists: List[Artist],
        genres: List[String],
        locations: List[String],
        searchByActiveYears: Boolean,
        activeAfter: Int,
        activeBefore: Int
    ): List[Artist] = artists.filter(artist =>
      (genres.isEmpty || genres.contains(artist.genre)) &&
        (locations.isEmpty || locations.contains(artist.origin.name)) &&
        (!searchByActiveYears ||
          artist.yearsActive.end.forall(_ >= activeAfter) && // <- using new product type (end)
          artist.yearsActive.start <= activeBefore)          // <- using new product type (start)
    )
  }

  {
    import Version2b_Data._
    import Version2b_Behavior._

    searchArtists(artists, List("Pop"), List("England"), true, 1950, 2022) ===
      List(Artist("Bee Gees", "Pop", Location("England"), PeriodInYears(1958, Some(2003))))
  }

  // STEP 3: sum type
  enum MusicGenre {
    case HeavyMetal
    case Pop
    case HardRock
  }

  import MusicGenre._

  object Version3 {
    import Version2b_Data.PeriodInYears

    case class Artist(
        name: String,
        genre: MusicGenre,
        origin: Location,
        yearsActive: PeriodInYears
    )

    val artists = List(
      Artist("Metallica", HeavyMetal, Location("U.S."), PeriodInYears(1981, None)),
      Artist("Led Zeppelin", HardRock, Location("England"), PeriodInYears(1968, Some(1980))),
      Artist("Bee Gees", Pop, Location("England"), PeriodInYears(1958, Some(2003)))
    )

    def searchArtists(
        artists: List[Artist],
        genres: List[MusicGenre], // <- now we need to make sure only valid genres are searched for
        locations: List[String],
        searchByActiveYears: Boolean,
        activeAfter: Int,
        activeBefore: Int
    ): List[Artist] = artists.filter(artist =>
      (genres.isEmpty || genres.contains(artist.genre)) && // no change needed
        (locations.isEmpty || locations.contains(artist.origin.name)) &&
        (!searchByActiveYears ||
          artist.yearsActive.end.forall(_ >= activeAfter) &&
          artist.yearsActive.start <= activeBefore)
    )
  }

  {
    import Version2b_Data.PeriodInYears
    import Version3._

    searchArtists(artists, List(Pop), List("England"), true, 1950, 2022) === List(Artist(
      "Bee Gees",
      Pop,
      Location("England"),
      PeriodInYears(1958, Some(2003))
    ))
  }

  // STEP 4: Algebraic Data Type (ADT) = product type + sum type
  enum YearsActive {
    case StillActive(since: Int)
    case ActiveBetween(start: Int, end: Int)
  }

  import YearsActive._

  object Version4_Data {
    case class Artist(name: String, genre: MusicGenre, origin: Location, yearsActive: YearsActive)

    val artists = List(
      Artist("Metallica", HeavyMetal, Location("U.S."), StillActive(since = 1981)),
      Artist("Led Zeppelin", HardRock, Location("England"), ActiveBetween(1968, 1980)),
      Artist("Bee Gees", Pop, Location("England"), ActiveBetween(1958, 2003))
    )
  }

  object Version4_Behavior {
    import Version4_Data._

    def wasArtistActive(artist: Artist, yearStart: Int, yearEnd: Int): Boolean = artist.yearsActive match {
      case StillActive(since)        => since <= yearEnd
      case ActiveBetween(start, end) => start <= yearEnd && end >= yearStart
    }

    def searchArtists(
        artists: List[Artist],
        genres: List[MusicGenre],
        locations: List[Location],
        searchByActiveYears: Boolean,
        activeAfter: Int,
        activeBefore: Int
    ): List[Artist] = artists.filter(artist =>
      (genres.isEmpty || genres.contains(artist.genre)) &&
        (locations.isEmpty || locations.contains(artist.origin)) &&
        (!searchByActiveYears || wasArtistActive(artist, activeAfter, activeBefore))
    )
  }

  {
    import Version4_Data._
    import Version4_Behavior._

    searchArtists(artists, List(Pop), List(Location("England")), true, 1950, 2022) === List(
      Artist("Bee Gees", Pop, Location("England"), ActiveBetween(1958, 2003))
    )
  }

  { // Practicing pattern matching
    import Version4_Data._

    def activeLength(artist: Artist, currentYear: Int): Int = artist.yearsActive match {
      case StillActive(since)        => currentYear - since
      case ActiveBetween(start, end) => end - start
    }

    activeLength(Artist("Metallica", HeavyMetal, Location("U.S."), StillActive(1981)), 2022) === 41
    activeLength(Artist("Led Zeppelin", HardRock, Location("England"), ActiveBetween(1968, 1980)), 2022) === 12
    activeLength(Artist("Bee Gees", Pop, Location("England"), ActiveBetween(1958, 2003)), 2022) === 45
  }

  { // STEP 5: modeling behaviors
    import Version4_Data._
    import Version4_Behavior.wasArtistActive

    // Modeling conditions as ADTs:
    enum SearchCondition {
      case SearchByGenre(genres: List[MusicGenre])
      case SearchByOrigin(locations: List[Location])
      case SearchByActiveYears(start: Int, end: Int)
    }

    import SearchCondition._

    def searchArtists(
        artists: List[Artist],
        requiredConditions: List[SearchCondition]
    ): List[Artist] = artists.filter(artist =>
      requiredConditions.forall(condition =>
        condition match {
          case SearchByGenre(genres)           => genres.contains(artist.genre)
          case SearchByOrigin(locations)       => locations.contains(artist.origin)
          case SearchByActiveYears(start, end) => wasArtistActive(artist, start, end)
        }
      )
    )

    searchArtists(
      artists,
      List(
        SearchByGenre(List(Pop)),
        SearchByOrigin(List(Location("England"))),
        SearchByActiveYears(1950, 2022)
      )
    ) === List(
      Artist("Bee Gees", Pop, Location("England"), ActiveBetween(1958, 2003))
    )

    searchArtists(
      artists,
      List(
        SearchByActiveYears(1950, 2022)
      )
    ) === List(
      Artist("Metallica", HeavyMetal, Location("U.S."), StillActive(since = 1981)),
      Artist("Led Zeppelin", HardRock, Location("England"), ActiveBetween(1968, 1980)),
      Artist("Bee Gees", Pop, Location("England"), ActiveBetween(1958, 2003))
    )

    searchArtists(
      artists,
      List(
        SearchByGenre(List(Pop)),
        SearchByOrigin(List(Location("England")))
      )
    ) === List(
      Artist("Bee Gees", Pop, Location("England"), ActiveBetween(1958, 2003))
    )

    searchArtists(artists, List.empty) === artists

    // additional examples:
    searchArtists(
      artists,
      List(
        SearchByActiveYears(1983, 2003)
      )
    ) === List(
      Artist("Metallica", HeavyMetal, Location("U.S."), StillActive(since = 1981)),
      Artist("Bee Gees", Pop, Location("England"), ActiveBetween(1958, 2003))
    )

    searchArtists(
      artists,
      List(
        SearchByGenre(List(HeavyMetal)),
        SearchByActiveYears(2019, 2022)
      )
    ) === List(
      Artist("Metallica", HeavyMetal, Location("U.S."), StillActive(since = 1981))
    )

    searchArtists(
      artists,
      List(
        SearchByActiveYears(1950, 1959)
      )
    ) === List(
      Artist("Bee Gees", Pop, Location("England"), ActiveBetween(1958, 2003))
    )

    searchArtists(
      artists,
      List(
        SearchByOrigin(List(Location("U.S."))),
        SearchByActiveYears(1950, 1959)
      )
    ) === List.empty
  }

  // NEW REQUIREMENTS:
  {
    case class PeriodInYears(start: Int, end: Int)

    enum YearsActive {
      case StillActive(since: Int, previousPeriods: List[PeriodInYears])
      case ActiveInPast(periods: List[PeriodInYears])
    }

    case class Artist(name: String, genre: MusicGenre, origin: Location, yearsActive: YearsActive)

    enum SearchCondition {
      case SearchByGenre(genres: List[MusicGenre])
      case SearchByOrigin(locations: List[Location])
      case SearchByActiveYears(period: PeriodInYears)
      case SearchByActiveLength(howLong: Int, until: Int)
    }

    import SearchCondition._, YearsActive._

    def periodOverlapsWithPeriods(checkedPeriod: PeriodInYears, periods: List[PeriodInYears]): Boolean =
      periods.exists(p => p.start <= checkedPeriod.end && p.end >= checkedPeriod.start)

    def wasArtistActive(artist: Artist, searchedPeriod: PeriodInYears): Boolean = artist.yearsActive match {
      case StillActive(since, previousPeriods) =>
        since <= searchedPeriod.end || periodOverlapsWithPeriods(searchedPeriod, previousPeriods)
      case ActiveInPast(periods)               => periodOverlapsWithPeriods(searchedPeriod, periods)
    }

    def activeLength(artist: Artist, currentYear: Int): Int = {
      val periods = artist.yearsActive match {
        case StillActive(since, previousPeriods) => previousPeriods.appended(PeriodInYears(since, currentYear))
        case ActiveInPast(periods)               => periods
      }
      periods.map(p => p.end - p.start).foldLeft(0)((x, y) => x + y)
    }

    def searchArtists(artists: List[Artist], requiredConditions: List[SearchCondition]): List[Artist] = artists.filter(
      artist =>
        requiredConditions.forall(condition =>
          condition match {
            case SearchByGenre(genres)                => genres.contains(artist.genre)
            case SearchByOrigin(locations)            => locations.contains(artist.origin)
            case SearchByActiveYears(period)          => wasArtistActive(artist, period)
            case SearchByActiveLength(howLong, until) => activeLength(artist, until) >= howLong
          }
        )
    )

    val artists = List(
      Artist("Metallica", HeavyMetal, Location("U.S."), StillActive(1981, List.empty)),
      Artist("Led Zeppelin", HardRock, Location("England"), ActiveInPast(List(PeriodInYears(1968, 1980)))),
      Artist(
        "Bee Gees",
        Pop,
        Location("England"),
        ActiveInPast(List(PeriodInYears(1958, 2003), PeriodInYears(2009, 2012)))
      )
    )

    check {
      searchArtists(
        artists,
        List(
          SearchByGenre(List(Pop)),
          SearchByOrigin(List(Location("England"))),
          SearchByActiveYears(PeriodInYears(1950, 2022))
        )
      )
    }.expect {
      List(
        Artist(
          "Bee Gees",
          Pop,
          Location("England"),
          ActiveInPast(List(PeriodInYears(1958, 2003), PeriodInYears(2009, 2012)))
        )
      )
    }

    check {
      searchArtists(
        artists,
        List(
          SearchByOrigin(List(Location("England"))),
          SearchByActiveYears(PeriodInYears(1950, 2022))
        )
      )
    }.expect {
      List(
        Artist("Led Zeppelin", HardRock, Location("England"), ActiveInPast(List(PeriodInYears(1968, 1980)))),
        Artist(
          "Bee Gees",
          Pop,
          Location("England"),
          ActiveInPast(List(PeriodInYears(1958, 2003), PeriodInYears(2009, 2012)))
        )
      )
    }

    check {
      searchArtists(
        artists,
        List(
          SearchByActiveYears(PeriodInYears(1950, 2022))
        )
      )
    }.expect {
      List(
        Artist("Metallica", HeavyMetal, Location("U.S."), StillActive(1981, List.empty)),
        Artist("Led Zeppelin", HardRock, Location("England"), ActiveInPast(List(PeriodInYears(1968, 1980)))),
        Artist(
          "Bee Gees",
          Pop,
          Location("England"),
          ActiveInPast(List(PeriodInYears(1958, 2003), PeriodInYears(2009, 2012)))
        )
      )
    }

    check {
      searchArtists(
        artists,
        List(
          SearchByActiveYears(PeriodInYears(1983, 2003))
        )
      )
    }.expect {
      List(
        Artist("Metallica", HeavyMetal, Location("U.S."), StillActive(1981, List.empty)),
        Artist(
          "Bee Gees",
          Pop,
          Location("England"),
          ActiveInPast(List(PeriodInYears(1958, 2003), PeriodInYears(2009, 2012)))
        )
      )
    }

    check {
      searchArtists(
        artists,
        List(
          SearchByActiveYears(PeriodInYears(2019, 2022))
        )
      )
    }.expect {
      List(
        Artist("Metallica", HeavyMetal, Location("U.S."), StillActive(1981, List.empty))
      )
    }

    check {
      searchArtists(
        artists,
        List(
          SearchByActiveYears(PeriodInYears(1950, 1959))
        )
      )
    }.expect {
      List(
        Artist(
          "Bee Gees",
          Pop,
          Location("England"),
          ActiveInPast(List(PeriodInYears(1958, 2003), PeriodInYears(2009, 2012)))
        )
      )
    }

    check {
      searchArtists(
        artists,
        List(
          SearchByActiveLength(48, 2022)
        )
      )
    }.expect {
      List(
        Artist(
          "Bee Gees",
          Pop,
          Location("England"),
          ActiveInPast(List(PeriodInYears(1958, 2003), PeriodInYears(2009, 2012)))
        )
      )
    }

    check {
      searchArtists(
        artists,
        List(
          SearchByActiveLength(48, 2022)
        )
      )
    }.expect {
      List(
        Artist(
          "Bee Gees",
          Pop,
          Location("England"),
          ActiveInPast(List(PeriodInYears(1958, 2003), PeriodInYears(2009, 2012)))
        )
      )
    }

    check {
      searchArtists(
        artists,
        List(
          SearchByOrigin(List(Location("U.S."))),
          SearchByActiveLength(48, 2022)
        )
      )
    }.expect {
      List.empty
    }

    check {
      searchArtists(
        artists,
        List(
          SearchByOrigin(List(Location("U.S."))),
          SearchByActiveLength(40, 2022)
        )
      )
    }.expect {
      List(Artist("Metallica", HeavyMetal, Location("U.S."), StillActive(1981, List.empty)))
    }

    check {
      searchArtists(
        artists,
        List(
          SearchByOrigin(List(Location("U.S."), Location("England"))),
          SearchByActiveLength(40, 2022)
        )
      )
    }.expect {
      List(
        Artist("Metallica", HeavyMetal, Location("U.S."), StillActive(1981, List.empty)),
        Artist(
          "Bee Gees",
          Pop,
          Location("England"),
          ActiveInPast(List(PeriodInYears(1958, 2003), PeriodInYears(2009, 2012)))
        )
      )
    }
  }
}
