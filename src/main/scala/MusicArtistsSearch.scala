object MusicArtistsSearch extends App {

  /**
    * TODO:
    * - .copy
    * - songs by artist
    * - albums?
    * - types: Song vs Artist as String (stringly-typed)
    *  - Map
    *  - can you pick a song before the artist?
    *  - coffee breaks: modeling user, POI? tv show?
    *  - maybe something more dynamic, like event sourcing?
    *  - concert/festival?
    *   - conditions: foldLeft! instead of raw boolean error-prone expressions
    *   - artist type: band/singer?
    *   - genres/subgenres?
    */
  // STEP 1: Design using what we know (modeling with Option/String)
  {
    case class Artist(
        name: String,
        genre: String,
        origin: String,
        yearsActiveStart: Int,
        yearsActiveEnd: Option[Int]
    )

    def searchArtists(
        artists: List[Artist],
        genres: List[String],
        locations: List[String],
        activeAfter: Int,
        activeBefore: Int
    ): List[Artist] = {
      artists.filter(artist =>
        (genres.isEmpty || genres.contains(artist.genre)) &&
        (locations.isEmpty || locations.contains(artist.origin)) &&
        ((artist.yearsActiveEnd
          .forall(_ > activeAfter) && artist.yearsActiveStart < activeBefore) || (artist.yearsActiveStart < activeBefore && artist.yearsActiveStart > activeAfter))
      )
    }

    val artists = List(
      Artist("Metallica", "Thrash Metal", "U.S.", 1983, None),
      Artist("Led Zeppelin", "Hard Rock", "England", 1968, Some(1980)),
      Artist("Bee Gees", "Pop", "England", 1958, Some(2003))
    )

    check(searchArtists(artists, List("Pop"), List("England"), 1950, 2020)).expect {
      List(Artist("Bee Gees", "Pop", "England", 1958, Some(2003)))
    }
    check(searchArtists(artists, List.empty, List("England"), 1950, 2020)).expect {
      List(
        Artist("Led Zeppelin", "Hard Rock", "England", 1968, Some(1980)),
        Artist("Bee Gees", "Pop", "England", 1958, Some(2003))
      )
    }
    check(searchArtists(artists, List.empty, List.empty, 1950, 2020)).expect {
      List(
        Artist("Metallica", "Thrash Metal", "U.S.", 1983, None),
        Artist("Led Zeppelin", "Hard Rock", "England", 1968, Some(1980)),
        Artist("Bee Gees", "Pop", "England", 1958, Some(2003))
      )
    }
    check(searchArtists(artists, List.empty, List.empty, 1983, 2003)).expect {
      List(
        Artist("Metallica", "Thrash Metal", "U.S.", 1983, None),
        Artist("Bee Gees", "Pop", "England", 1958, Some(2003))
      )
    }
    check(searchArtists(artists, List.empty, List.empty, 2019, 2020)).expect {
      List(
        Artist("Metallica", "Thrash Metal", "U.S.", 1983, None)
      )
    }
    check(searchArtists(artists, List.empty, List.empty, 1950, 1959)).expect {
      List(
        Artist("Bee Gees", "Pop", "England", 1958, Some(2003))
      )
    }
  }
  // PROBLEM:

  // TODO: next steps

  // intermediate:
  // sealed trait Period
  // case class ActivePeriod(start: Int) extends Period
  // case class PreviousPeriod(start: Int, end: Int) extends Period

  // case class YearsActive(currentPeriod: ActivePeriod, previousPeriods: List[PreviousPeriod] = List.empty)

  // LAST STEP:
  {
    sealed trait MusicGenre
    case object ThrashMetal extends MusicGenre
    case object Pop         extends MusicGenre
    case object HardRock    extends MusicGenre

    case class Location(name: String)

    case class ActivePeriod(start: Int, end: Int)

    sealed trait YearsActive
    case class ActiveNow(since: Int, previousActivePeriods: List[ActivePeriod] = List.empty) extends YearsActive
    case class Inactive(activePeriods: List[ActivePeriod])                                   extends YearsActive

    case class Artist(name: String, genre: MusicGenre, origin: Location, yearsActive: YearsActive)

    sealed trait SearchCondition
    case class PossibleGenres(genres: List[MusicGenre])     extends SearchCondition
    case class PossibleLocations(locations: List[Location]) extends SearchCondition
    case class ActiveBefore(year: Int)                      extends SearchCondition
    case class ActiveAfter(year: Int)                       extends SearchCondition

    def wasArtistActiveBefore(artist: Artist, year: Int): Boolean = {
      artist.yearsActive match {
        case ActiveNow(activeSince, previousActivePeriods) =>
          activeSince < year || previousActivePeriods.exists(_.start < year)
        case Inactive(activePeriods) => activePeriods.exists(_.start < year)
      }
    }

    def wasArtistActiveAfter(artist: Artist, year: Int): Boolean = {
      artist.yearsActive match {
        case ActiveNow(_, _) => true
        case Inactive(activePeriods) =>
          activePeriods.exists(_.end > year)
      }
    }

    def searchArtists(
        artists: List[Artist],
        requiredConditions: List[SearchCondition]
    ): List[Artist] = {
      artists.filter(artist =>
        requiredConditions.foldLeft(true) { (satisfiedSoFar, nextCondition) =>
          satisfiedSoFar && (nextCondition match {
            case PossibleGenres(genres)       => genres.contains(artist.genre) // type checked!
            case PossibleLocations(locations) => locations.contains(artist.origin)
            case ActiveBefore(year)           => wasArtistActiveBefore(artist, year)
            case ActiveAfter(year)            => wasArtistActiveAfter(artist, year)
          })
        }
      )
    }

    val artists = List(
      Artist("Metallica", ThrashMetal, Location("U.S."), ActiveNow(since = 1983)),
      Artist("Led Zeppelin", HardRock, Location("England"), Inactive(List(ActivePeriod(1968, 1980)))),
      Artist("Bee Gees", Pop, Location("England"), Inactive(List(ActivePeriod(1958, 2003), ActivePeriod(2009, 2012))))
    )

    check(
      searchArtists(
        artists,
        List(
          PossibleGenres(List(Pop)),
          PossibleLocations(List(Location("England"))),
          ActiveAfter(1950),
          ActiveBefore(2020)
        )
      )
    ).expect {
      List(
        Artist("Bee Gees", Pop, Location("England"), Inactive(List(ActivePeriod(1958, 2003), ActivePeriod(2009, 2012))))
      )
    }

    check(
      searchArtists(
        artists,
        List(
          PossibleLocations(List(Location("England"))),
          ActiveAfter(1950),
          ActiveBefore(2020)
        )
      )
    ).expect {
      List(
        Artist("Led Zeppelin", HardRock, Location("England"), Inactive(List(ActivePeriod(1968, 1980)))),
        Artist("Bee Gees", Pop, Location("England"), Inactive(List(ActivePeriod(1958, 2003), ActivePeriod(2009, 2012))))
      )
    }

    check(
      searchArtists(
        artists,
        List(
          ActiveAfter(1950),
          ActiveBefore(2020)
        )
      )
    ).expect {
      List(
        Artist("Metallica", ThrashMetal, Location("U.S."), ActiveNow(since = 1983)),
        Artist("Led Zeppelin", HardRock, Location("England"), Inactive(List(ActivePeriod(1968, 1980)))),
        Artist("Bee Gees", Pop, Location("England"), Inactive(List(ActivePeriod(1958, 2003), ActivePeriod(2009, 2012))))
      )
    }

    check(
      searchArtists(
        artists,
        List(
          ActiveAfter(1983),
          ActiveBefore(2003)
        )
      )
    ).expect {
      List(
        Artist("Metallica", ThrashMetal, Location("U.S."), ActiveNow(since = 1983)),
        Artist("Bee Gees", Pop, Location("England"), Inactive(List(ActivePeriod(1958, 2003), ActivePeriod(2009, 2012))))
      )
    }

    check(
      searchArtists(
        artists,
        List(
          ActiveAfter(2019),
          ActiveBefore(2020)
        )
      )
    ).expect {
      List(
        Artist("Metallica", ThrashMetal, Location("U.S."), ActiveNow(since = 1983))
      )
    }

    check(
      searchArtists(
        artists,
        List(
          ActiveAfter(1950),
          ActiveBefore(1959)
        )
      )
    ).expect {
      List(
        Artist("Bee Gees", Pop, Location("England"), Inactive(List(ActivePeriod(1958, 2003), ActivePeriod(2009, 2012))))
      )
    }

    // we have more possibilities:
    // 1. inactive periods support:

    check(
      searchArtists(
        artists,
        List(
          ActiveAfter(2004)
        )
      )
    ).expect {
      List(
        Artist("Metallica", ThrashMetal, Location("U.S."), ActiveNow(since = 1983)),
        Artist("Bee Gees", Pop, Location("England"), Inactive(List(ActivePeriod(1958, 2003), ActivePeriod(2009, 2012))))
      )
    }
  }

  // TODO: 2. adding a condition is just adding a new case class and compiler will tell us what's missing!
}
