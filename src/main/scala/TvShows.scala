object TvShows extends App {
  case class TvShow(title: String, start: Int, end: Int)

  {
    val shows = List(TvShow("Breaking Bad", 2008, 2013), TvShow("The Wire", 2002, 2008), TvShow("Mad Men", 2007, 2015))

    def sortShows(shows: List[TvShow]): List[TvShow] = {
      shows
        .sortBy(tvShow => tvShow.end - tvShow.start) // sortBy gets a function that returns an Int for a given TvShow
        .reverse // sortBy sorts in natural order (from the smallest Int to the highest one), so we return a reverse of the List
    }

    println(sortShows(shows))
    assert(sortShows(shows).map(_.title) == List("Mad Men", "The Wire", "Breaking Bad"))
  }

  val rawShows = List("Breaking Bad (2008-2013)", "The Wire (2002-2008)", "Mad Men (2007-2015)")

  val invalidRawShows = List("Breaking Bad, 2008-2013", "The Wire (from 2002 until 2008)", "Mad Men (9/10)")

  {
    def parseShow(rawShow: String): TvShow = {
      // first get the indices of the separator characters
      val bracketOpen  = rawShow.indexOf('(')
      val bracketClose = rawShow.indexOf(')')
      val dash         = rawShow.indexOf('-')

      // then use these separators to extract 3 pieces of information we need
      val name      = rawShow.substring(0, bracketOpen).trim
      val yearStart = rawShow.substring(bracketOpen + 1, dash).toInt
      val yearEnd   = rawShow.substring(dash + 1, bracketClose).toInt

      TvShow(name, yearStart, yearEnd)
    }

    def parseShows(rawShows: List[String]): List[TvShow] = {
      rawShows.map(parseShow)
    }

    println(parseShows(rawShows))
    assert(parseShows(rawShows).map(_.title) == List("Breaking Bad", "The Wire", "Mad Men"))

    // INVALID INPUT
    // parseShows(invalidRawShows)
  }

  {
    def parseShow(rawShow: String): Option[TvShow] = {
      val bracketOpen  = rawShow.indexOf('(')
      val bracketClose = rawShow.indexOf(')')
      val dash         = rawShow.indexOf('-')

      for {
        name <- Option.when(bracketOpen != -1)(rawShow.substring(0, bracketOpen).trim)
        yearStart <- Option.when(bracketOpen != -1 && dash > bracketOpen + 1)(
                      rawShow.substring(bracketOpen + 1, dash).toInt
                    )
        yearEnd <- Option.when(dash != -1 && bracketClose > dash + 1)(rawShow.substring(dash + 1, bracketClose).toInt)
      } yield TvShow(name, yearStart, yearEnd)
    }

    println(parseShow("Breaking Bad, 2008-2013"))
    assert(parseShow("Breaking Bad (2008-2013)").contains(TvShow("Breaking Bad", 2008, 2013)))

    // INVALID INPUT
    assert(parseShow(") - (Breaking Bad, 2008-2013").isEmpty)
    assert(parseShow("Mad Men (-2015)").isEmpty)
  }

  {

    def extractName(rawShow: String): Option[String] = {
      val bracketOpen = rawShow.indexOf('(')
      Option.when(bracketOpen != -1)(rawShow.substring(0, bracketOpen).trim)
    }

    def extractYearStart(rawShow: String): Option[Int] = {
      val bracketOpen = rawShow.indexOf('(')
      val dash        = rawShow.indexOf('-')
      Option.when(bracketOpen != -1 && dash > bracketOpen + 1)(rawShow.substring(bracketOpen + 1, dash).toInt)
    }

    def extractYearEnd(rawShow: String): Option[Int] = {
      val dash         = rawShow.indexOf('-')
      val bracketClose = rawShow.indexOf(')')
      Option.when(dash != -1 && bracketClose > dash + 1)(rawShow.substring(dash + 1, bracketClose).toInt)
    }

    def parseShow(rawShow: String): Option[TvShow] = {
      for {
        name      <- extractName(rawShow)
        yearStart <- extractYearStart(rawShow)
        yearEnd   <- extractYearEnd(rawShow)
      } yield TvShow(name, yearStart, yearEnd)
    }

    val chernobyl = parseShow("Chernobyl (2019)")

    println(chernobyl)
    assert(chernobyl.isEmpty)

    def extractSingleYear(rawShow: String): Option[Int] = {
      val dash         = rawShow.indexOf('-')
      val bracketOpen  = rawShow.indexOf('(')
      val bracketClose = rawShow.indexOf(')')
      if (dash == -1 && bracketOpen != -1 && bracketClose != -1)
        Some(rawShow.substring(bracketOpen + 1, bracketClose).toInt)
      else None
    }

    def parseShow2(rawShow: String): Option[TvShow] = {
      for {
        name      <- extractName(rawShow)
        yearStart <- extractYearStart(rawShow).orElse(extractSingleYear(rawShow))
        yearEnd   <- extractYearEnd(rawShow).orElse(extractSingleYear(rawShow))
      } yield TvShow(name, yearStart, yearEnd)
    }

    val chernobyl2 = parseShow2("Chernobyl (2019)")

    println(chernobyl2)
    assert(chernobyl2.contains(TvShow("Chernobyl", 2019, 2019)))

    def parseShows(rawShows: List[String]): List[TvShow] = {
      rawShows // List[String]
        .map(parseShow) // List[Option[TvShow]]
        .map(_.toList) // List[List[TvShow]]
        .flatten // List[TvShow]
    }

    println(parseShows(List("Chernobyl [2019]", "Breaking Bad (2008-2013)")))
    println(parseShows(List("Chernobyl [2019]", "Breaking Bad")))

    def parseShows2(rawShows: List[String]): Option[List[TvShow]] = {
      val parsedShows: List[Option[TvShow]] = rawShows.map(parseShow)
      parsedShows.foldLeft(Option(List.empty[TvShow])) { (result, parsedShow) =>
        if (parsedShow.isEmpty || result.isEmpty) None
        else result.map(_.appendedAll(parsedShow.toList))
      }
    }

    println(parseShows2(List("Chernobyl (2019)", "Breaking Bad (2008-2013)")))
    println(parseShows2(List("Chernobyl [2019]", "Breaking Bad")))
  }

}
