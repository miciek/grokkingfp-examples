object TvShows extends App {
  case class TvShow(title: String, start: Int, end: Int)

  {
    val shows = List(TvShow("Breaking Bad", 2008, 2013), TvShow("The Wire", 2002, 2008), TvShow("Mad Men", 2007, 2015))

    def sortShows(shows: List[TvShow]): List[TvShow] = {
      shows
        .sortBy(tvShow => tvShow.end - tvShow.start) // sortBy gets a function that returns an Int for a given TvShow
        .reverse                                     // sortBy sorts in natural order (from the smallest Int to the highest one), so we return a reverse of the List
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
        name <- Option.when(bracketOpen > 0)(rawShow.substring(0, bracketOpen).trim)
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
      Option.when(bracketOpen > 0)(rawShow.substring(0, bracketOpen).trim)
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

    def parseShow1(rawShow: String): Option[TvShow] = {
      for {
        name      <- extractName(rawShow)
        yearStart <- extractYearStart(rawShow)
        yearEnd   <- extractYearEnd(rawShow)
      } yield TvShow(name, yearStart, yearEnd)
    }

    val chernobyl = parseShow1("Chernobyl (2019)")

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

    def parseShow(rawShow: String): Option[TvShow] = {
      for {
        name      <- extractName(rawShow)
        yearStart <- extractYearStart(rawShow).orElse(extractSingleYear(rawShow))
        yearEnd   <- extractYearEnd(rawShow).orElse(extractSingleYear(rawShow))
      } yield TvShow(name, yearStart, yearEnd)
    }

    val chernobyl2 = parseShow("Chernobyl (2019)")

    println(chernobyl2)
    assert(chernobyl2.contains(TvShow("Chernobyl", 2019, 2019)))

    { // Practicing functional error handling
      def extractSingleYearOrYearEnd(rawShow: String): Option[Int] =
        extractSingleYear(rawShow).orElse(extractYearEnd(rawShow))

      def extractAnyYear(rawShow: String): Option[Int] =
        extractYearStart(rawShow).orElse(extractYearEnd(rawShow)).orElse(extractSingleYear(rawShow))

      def extractYearIfNameExists(rawShow: String): Option[Int] =
        extractName(rawShow).flatMap(name => extractSingleYear(rawShow))

      def extractAnyYearyIfNameExists(rawShow: String): Option[Int] =
        extractName(rawShow).flatMap(name => extractAnyYear(rawShow))

      println(extractSingleYearOrYearEnd("A (-2019)"))
      println(extractSingleYearOrYearEnd("A (2018)"))
      println(extractSingleYearOrYearEnd("B (2016-)"))
      println(extractAnyYear("B (2019)"))
      println(extractAnyYear("B (-2017)"))
      println(extractAnyYear("B (2016-)"))
      println(extractAnyYear("B (-)"))
      println(extractYearIfNameExists("C (2020)"))
      println(extractYearIfNameExists("(2020)"))
      println(extractYearIfNameExists("(2020-)"))
      println(extractAnyYearyIfNameExists("A(2021)"))
      println(extractAnyYearyIfNameExists("(2021)"))
    }

    def parseShows1(rawShows: List[String]): List[TvShow] = {
      rawShows          // List[String]
        .map(parseShow) // List[Option[TvShow]]
        .map(_.toList)  // List[List[TvShow]]
        .flatten        // List[TvShow]
    }

    println(parseShows1(List("Chernobyl [2019]", "Breaking Bad (2008-2013)")))
    println(parseShows1(List("Chernobyl [2019]", "Breaking Bad")))

    def addOrResign(parsedShows: Option[List[TvShow]], newParsedShow: Option[TvShow]): Option[List[TvShow]] = {
      for {
        shows      <- parsedShows
        parsedShow <- newParsedShow
      } yield shows.appended(parsedShow)
    }

    check {
      addOrResign(Some(List.empty), Some(TvShow("Chernobyl", 2019, 2019)))
    }(expect = Some(List(TvShow("Chernobyl", 2019, 2019))))
    check {
      addOrResign(Some(List(TvShow("Chernobyl", 2019, 2019))), Some(TvShow("The Wire", 2002, 2008)))
    }(expect = Some(List(TvShow("Chernobyl", 2019, 2019), TvShow("The Wire", 2002, 2008))))
    check {
      addOrResign(None, Some(TvShow("Chernobyl", 2019, 2019)))
    }(expect = None)
    check {
      addOrResign(Some(List(TvShow("Chernobyl", 2019, 2019))), None)
    }(expect = None)
    check {
      addOrResign(None, None)
    }(expect = None)

    def parseShows(rawShows: List[String]): Option[List[TvShow]] = {
      val initialResult: Option[List[TvShow]] = Some(List.empty)
      rawShows
        .map(parseShow)
        .foldLeft(initialResult)(addOrResign)
    }

    println(parseShows(List("Chernobyl (2019)", "Breaking Bad (2008-2013)")))
    println(parseShows(List("Chernobyl [2019]", "Breaking Bad")))
  }

  // Either
  {

    def extractName(rawShow: String): Either[String, String] = {
      val bracketOpen = rawShow.indexOf('(')
      if (bracketOpen > 0)
        Right(rawShow.substring(0, bracketOpen).trim)
      else
        Left(s"Can't extract name from $rawShow")
    }

    def extractYearStart(rawShow: String): Either[String, Int] = {
      val bracketOpen = rawShow.indexOf('(')
      val dash        = rawShow.indexOf('-')
      if (bracketOpen != -1 && dash > bracketOpen + 1) {
        Right(rawShow.substring(bracketOpen + 1, dash).toInt)
      } else {
        Left(s"Can't extract start year from $rawShow")
      }
    }

    def extractYearEnd(rawShow: String): Either[String, Int] = {
      val dash         = rawShow.indexOf('-')
      val bracketClose = rawShow.indexOf(')')
      if (dash != -1 && bracketClose > dash + 1) {
        Right(rawShow.substring(dash + 1, bracketClose).toInt)
      } else {
        Left(s"Can't extract end year from $rawShow")
      }
    }

    def extractSingleYear(rawShow: String): Either[String, Int] = {
      val dash         = rawShow.indexOf('-')
      val bracketOpen  = rawShow.indexOf('(')
      val bracketClose = rawShow.indexOf(')')
      if (dash == -1 && bracketOpen != -1 && bracketClose != -1)
        Right(rawShow.substring(bracketOpen + 1, bracketClose).toInt)
      else Left(s"Can't extract single year from $rawShow")
    }

    def parseShow(rawShow: String): Either[String, TvShow] = {
      for {
        name      <- extractName(rawShow)
        yearStart <- extractYearStart(rawShow).orElse(extractSingleYear(rawShow))
        yearEnd   <- extractYearEnd(rawShow).orElse(extractSingleYear(rawShow))
      } yield TvShow(name, yearStart, yearEnd)
    }

    val chernobyl = parseShow("Chernobyl (2019)")

    println(chernobyl)
    assert(chernobyl.contains(TvShow("Chernobyl", 2019, 2019)))

    { // Practicing functional error handling with Either
      def extractSingleYearOrYearEnd(rawShow: String): Either[String, Int] =
        extractSingleYear(rawShow).orElse(extractYearEnd(rawShow))

      def extractAnyYear(rawShow: String): Either[String, Int] =
        extractYearStart(rawShow).orElse(extractYearEnd(rawShow)).orElse(extractSingleYear(rawShow))

      def extractYearIfNameExists(rawShow: String): Either[String, Int] =
        extractName(rawShow).flatMap(name => extractSingleYear(rawShow))

      def extractAnyYearyIfNameExists(rawShow: String): Either[String, Int] =
        extractName(rawShow).flatMap(name => extractAnyYear(rawShow))

      println(extractSingleYearOrYearEnd("A (-2019)"))
      println(extractSingleYearOrYearEnd("A (2018)"))
      println(extractSingleYearOrYearEnd("B (2016-)"))
      println(extractAnyYear("B (2019)"))
      println(extractAnyYear("B (-2017)"))
      println(extractAnyYear("B (2016-)"))
      println(extractAnyYear("B (-)"))
      println(extractYearIfNameExists("C (2020)"))
      println(extractYearIfNameExists("(2020)"))
      println(extractYearIfNameExists("(2020-)"))
      println(extractAnyYearyIfNameExists("A(2021)"))
      println(extractAnyYearyIfNameExists("(2021)"))
    }

    def addOrResign(
        parsedShows: Either[String, List[TvShow]],
        newParsedShow: Either[String, TvShow]
    ): Either[String, List[TvShow]] = {
      for {
        shows      <- parsedShows
        parsedShow <- newParsedShow
      } yield shows.appended(parsedShow)
    }

    check {
      addOrResign(Right(List.empty), Right(TvShow("Chernobyl", 2019, 2019)))
    }(expect = Right(List(TvShow("Chernobyl", 2019, 2019))))
    check {
      addOrResign(Right(List(TvShow("Chernobyl", 2019, 2019))), Right(TvShow("The Wire", 2002, 2008)))
    }(expect = Right(List(TvShow("Chernobyl", 2019, 2019), TvShow("The Wire", 2002, 2008))))
    check {
      addOrResign(Left("something happened before Chernobyl"), Right(TvShow("Chernobyl", 2019, 2019)))
    }(expect = Left("something happened before Chernobyl"))
    check {
      addOrResign(Right(List(TvShow("Chernobyl", 2019, 2019))), Left("invalid show"))
    }(expect = Left("invalid show"))
    check {
      addOrResign(Left("hopeless"), Left("no hope"))
    }(expect = Left("hopeless"))

    def parseShows(rawShows: List[String]): Either[String, List[TvShow]] = {
      val initialResult: Either[String, List[TvShow]] = Right(List.empty)
      rawShows
        .map(parseShow)
        .foldLeft(initialResult)(addOrResign)
    }

    println(parseShows(List("Chernobyl (2019)", "Breaking Bad (2008-2013)")))
    println(parseShows(List("Chernobyl [2019]", "Breaking Bad")))

    check {
      parseShows(List("The Wire (2002-2008)", "[2019]"))
    }(expect = Left("Can't extract name from [2019]"))

    check {
      parseShows(List("The Wire (-)", "Chernobyl (2019)"))
    }(expect = Left("Can't extract single year from The Wire (-)"))

    check {
      parseShows(List("The Wire (2002-2008)", "Chernobyl (2019)"))
    }(expect = Right(List(TvShow("The Wire", 2002, 2008), TvShow("Chernobyl", 2019, 2019))))
  }

}
