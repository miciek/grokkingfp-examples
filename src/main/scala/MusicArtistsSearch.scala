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

    check { searchArtists(artists, List("Pop"), List("England"), true, 1950, 2020) }.expect {
      List(Artist("Bee Gees", "Pop", "England", 1958, false, 2003))
    }
    check { searchArtists(artists, List.empty, List("England"), true, 1950, 2020) }.expect {
      List(
        Artist("Led Zeppelin", "Hard Rock", "England", 1968, false, 1980),
        Artist("Bee Gees", "Pop", "England", 1958, false, 2003)
      )
    }
    check { searchArtists(artists, List.empty, List.empty, true, 1950, 2020) }.expect {
      List(
        Artist("Metallica", "Heavy Metal", "U.S.", 1983, true, 0),
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
    check { searchArtists(artists, List.empty, List.empty, true, 1950, 1959) }.expect {
      List(
        Artist("Bee Gees", "Pop", "England", 1958, false, 2003)
      )
    }
    check { searchArtists(artists, List.empty, List("U.S."), true, 1950, 1959) }.expect {
      List.empty
    }
    check { searchArtists(artists, List.empty, List("U.S."), false, 0, 0) }.expect {
      List(
        Artist("Metallica", "Heavy Metal", "U.S.", 1983, true, 0)
      )
    }
    check { searchArtists(artists, List.empty, List.empty, false, 2019, 2021) }.expect {
      List(
        Artist("Metallica", "Heavy Metal", "U.S.", 1983, true, 0),
        Artist("Led Zeppelin", "Hard Rock", "England", 1968, false, 1980),
        Artist("Bee Gees", "Pop", "England", 1958, false, 2003)
      )
    }
  }

  // TODO: next steps

  case class Location(name: String) extends AnyVal

  // LAST STEP:
  {
    sealed trait MusicGenre
    case object HeavyMetal extends MusicGenre
    case object Pop        extends MusicGenre
    case object HardRock   extends MusicGenre

    sealed trait YearsActive
    case class StillActive(since: Int)               extends YearsActive
    case class ActiveBetween(start: Int, until: Int) extends YearsActive

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

  }

  // NEW REQUIREMENTS:
  {
    sealed trait MusicGenre
    case object HeavyMetal extends MusicGenre
    case object Pop        extends MusicGenre
    case object HardRock   extends MusicGenre

    case class Location(name: String)

    case class PeriodInYears(start: Int, end: Option[Int])

    case class Artist(name: String, genre: MusicGenre, origin: Location, active: List[PeriodInYears])

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
              artist.active.exists(period => period.start <= end && period.end.forall(_ >= start))
            case SearchByActivityLength(howLong, until) =>
              val totalActive =
                artist.active.map(period => period.end.getOrElse(until) - period.start).foldLeft(0)(_ + _)
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
