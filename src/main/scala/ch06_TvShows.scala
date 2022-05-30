object ch06_TvShows extends App {
  case class TvShow(title: String, start: Int, end: Int)

  val shows = List(TvShow("Breaking Bad", 2008, 2013), TvShow("The Wire", 2002, 2008), TvShow("Mad Men", 2007, 2015))

  def sortShows(shows: List[TvShow]): List[TvShow] = {
    shows
      .sortBy(tvShow => tvShow.end - tvShow.start) // sortBy gets a function that returns an Int for a given TvShow
      .reverse                                     // sortBy sorts in natural order (from the smallest Int to the highest one), so we return a reverse of the List
  }

  sortShows(shows).map(_.title) === List("Mad Men", "The Wire", "Breaking Bad")

  val rawShows = List("Breaking Bad (2008-2013)", "The Wire (2002-2008)", "Mad Men (2007-2015)")

  {
    def parseShows(rawShows: List[String]): List[TvShow] = ??? // we need to implement it next

    def sortRawShows(rawShows: List[String]): List[TvShow] = {
      val tvShows = parseShows(rawShows)
      sortShows(tvShows)
    }

    println(sortRawShows)
  }

  val invalidRawShows = List("Breaking Bad, 2008-2013", "The Wire (from 2002 until 2008)", "Mad Men (9/10)")

  // STEP 0: ad-hoc solution (PROBLEM: exceptions)
  {
    def parseShow(rawShow: String): TvShow = {
      // first get the indices of the separator characters
      val bracketOpen  = rawShow.indexOf('(')
      val bracketClose = rawShow.indexOf(')')
      val dash         = rawShow.indexOf('-')

      // then use these separators to extract 3 pieces of information we need
      val name      = rawShow.substring(0, bracketOpen).trim
      val yearStart = Integer.parseInt(rawShow.substring(bracketOpen + 1, dash))
      val yearEnd   = Integer.parseInt(rawShow.substring(dash + 1, bracketClose))

      TvShow(name, yearStart, yearEnd)
    }

    def parseShows(rawShows: List[String]): List[TvShow] = {
      rawShows.map(parseShow)
    }

    parseShows(rawShows).map(_.title) === List("Breaking Bad", "The Wire", "Mad Men")

    // INVALID INPUT
    // parseShows(invalidRawShows)
  }

  // STEP 1a: using Option without implementing it yet
  // let's first see how we would use and test it:
  def testOptionBasedParseShow(parseShow: String => Option[TvShow]) = {
    parseShow("The Wire (2002-2008)") === Some(TvShow("The Wire", 2002, 2008))
    parseShow("The Wire aired from 2002 to 2008") === None
    parseShow("Breaking Bad (2008-2013)") === Some(TvShow("Breaking Bad", 2008, 2013))
    parseShow("Mad Men (2007-2015)") === Some(TvShow("Mad Men", 2007, 2015))
    parseShow("Scrubs (2001-2010)") === Some(TvShow("Scrubs", 2001, 2010))

    // INVALID INPUT
    parseShow("Breaking Bad ()") === None
    parseShow("()") === None
    parseShow(") - (Breaking Bad, 2008-2013") === None
    parseShow("Mad Men (-2015)") === None
    parseShow("The Wire ( 2002 - 2008 )") === None
    parseShow("Stranger Things (2016-)") === None
  }

  // STEP 1b: using Option, but implementing smaller functions that return Options first:
  def extractYearStart(rawShow: String): Option[Int] = {
    val bracketOpen = rawShow.indexOf('(')
    val dash        = rawShow.indexOf('-')
    for {
      yearStr <- if (bracketOpen != -1 && dash > bracketOpen + 1) Some(rawShow.substring(bracketOpen + 1, dash))
                 else None
      year    <- yearStr.trim.toIntOption
    } yield year
  }

  extractYearStart("Breaking Bad (2008-2013)") === Some(2008)
  extractYearStart("Mad Men (-2015)") === None
  extractYearStart("(2002- N/A ) The Wire") === Some(2002)

  def extractName(rawShow: String): Option[String] = {
    val bracketOpen = rawShow.indexOf('(')
    if (bracketOpen > 0) Some(rawShow.substring(0, bracketOpen).trim)
    else None
  }

  def extractYearEnd(rawShow: String): Option[Int] = {
    val dash         = rawShow.indexOf('-')
    val bracketClose = rawShow.indexOf(')')
    for {
      yearStr <- if (dash != -1 && bracketClose > dash + 1) Some(rawShow.substring(dash + 1, bracketClose))
                 else None
      year    <- yearStr.toIntOption
    } yield year
  }

  { // STEP 1c: using Option and composing smaller function into bigger one:
    def parseShow(rawShow: String): Option[TvShow] = {
      for {
        name      <- extractName(rawShow)
        yearStart <- extractYearStart(rawShow)
        yearEnd   <- extractYearEnd(rawShow)
      } yield TvShow(name, yearStart, yearEnd)
    }

    // it passes all tests:
    testOptionBasedParseShow(parseShow)

    // but it doesn't work with some exceptional undocumented cases:
    parseShow("Chernobyl (2019)") === None
  }

  { // (alternative) Step 1d: as a reminder, we can also implement it using bare flatMaps/maps (see chapter 5):
    def parseShow(rawShow: String): Option[TvShow] = {
      extractName(rawShow).flatMap(name =>
        extractYearStart(rawShow).flatMap(yearStart =>
          extractYearEnd(rawShow).map(yearEnd =>
            TvShow(name, yearStart, yearEnd)
          )
        )
      )
    }

    // it passes all tests:
    testOptionBasedParseShow(parseShow)

    // but it doesn't work with some exceptional undocumented cases:
    parseShow("Chernobyl (2019)") === None
  }

  def extractSingleYear(rawShow: String): Option[Int] = {
    val dash         = rawShow.indexOf('-')
    val bracketOpen  = rawShow.indexOf('(')
    val bracketClose = rawShow.indexOf(')')
    for {
      yearStr <- if (dash == -1 && bracketOpen != -1 && bracketClose > bracketOpen + 1)
                   Some(rawShow.substring(bracketOpen + 1, bracketClose))
                 else None
      year    <- yearStr.toIntOption
    } yield year
  }

  // STEP 1d: using Option and orElse
  def parseShow(rawShow: String): Option[TvShow] = {
    for {
      name      <- extractName(rawShow)
      yearStart <- extractYearStart(rawShow).orElse(extractSingleYear(rawShow))
      yearEnd   <- extractYearEnd(rawShow).orElse(extractSingleYear(rawShow))
    } yield TvShow(name, yearStart, yearEnd)
  }

  // it passes all tests:
  testOptionBasedParseShow(parseShow)

  // and is able to parse Chernobyl, too:
  parseShow("Chernobyl (2019)") === Some(TvShow("Chernobyl", 2019, 2019))

  { // introducing orElse
    val seven: Option[Int] = Some(7)
    val eight: Option[Int] = Some(8)
    val none: Option[Int]  = None

    seven.orElse(eight) === Some(7)
    none.orElse(eight) === Some(8)
    seven.orElse(none) === Some(7)
    none.orElse(none) === None

    val chernobyl = "Chernobyl (2019)"
    extractYearStart(chernobyl) === None
    extractSingleYear(chernobyl) === Some(2019)
    extractYearStart(chernobyl).orElse(extractSingleYear(chernobyl)) === Some(2019)
    extractYearStart(chernobyl).orElse(extractSingleYear("not-a-year")) === None
  }

  { // Practicing functional error handling
    def extractSingleYearOrYearEnd(rawShow: String): Option[Int] =
      extractSingleYear(rawShow).orElse(extractYearEnd(rawShow))

    def extractAnyYear(rawShow: String): Option[Int] =
      extractYearStart(rawShow).orElse(extractYearEnd(rawShow)).orElse(extractSingleYear(rawShow))

    def extractSingleYearIfNameExists(rawShow: String): Option[Int] =
      extractName(rawShow).flatMap(name => extractSingleYear(rawShow))

    def extractAnyYearIfNameExists(rawShow: String): Option[Int] =
      extractName(rawShow).flatMap(name => extractAnyYear(rawShow))

    extractSingleYearOrYearEnd("A (1992-)") === None
    extractSingleYearOrYearEnd("B (2002)") === Some(2002)
    extractSingleYearOrYearEnd("C (-2012)") === Some(2012)
    extractSingleYearOrYearEnd("(2022)") === Some(2022)
    extractSingleYearOrYearEnd("E (-)") === None

    extractAnyYear("A (1992-)") === Some(1992)
    extractAnyYear("B (2002)") === Some(2002)
    extractAnyYear("C (-2012)") === Some(2012)
    extractAnyYear("(2022)") === Some(2022)
    extractAnyYear("E (-)") === None

    extractSingleYearIfNameExists("A (1992-)") === None
    extractSingleYearIfNameExists("B (2002)") === Some(2002)
    extractSingleYearIfNameExists("C (-2012)") === None
    extractSingleYearIfNameExists("(2022)") === None
    extractSingleYearIfNameExists("E (-)") === None

    extractAnyYearIfNameExists("A (1992-)") === Some(1992)
    extractAnyYearIfNameExists("B (2002)") === Some(2002)
    extractAnyYearIfNameExists("C (-2012)") === Some(2012)
    extractAnyYearIfNameExists("(2022)") === None
    extractAnyYearIfNameExists("E (-)") === None
  }

  { // introducing toList
    Some(7).toList === List(7)
    None.toList === List()
  }

  val rawShowsWithOneInvalid = List("Breaking Bad (2008-2013)", "The Wire 2002 2008", "Mad Men (2007-2015)")

  { // STEP 2a: trying to blindly implement parseShows by following only the compiler ("best-effort" error handling strategy)
    def parseShows(rawShows: List[String]): List[TvShow] = {
      rawShows          // List[String]
        .map(parseShow) // List[Option[TvShow]]
        .map(_.toList)  // List[List[TvShow]]
        .flatten        // List[TvShow]
    }

    parseShows(rawShowsWithOneInvalid) === List(TvShow("Breaking Bad", 2008, 2013), TvShow("Mad Men", 2007, 2015))
    parseShows(List("Chernobyl [2019]", "Breaking Bad (2008-2013)")) === List(TvShow("Breaking Bad", 2008, 2013))
    parseShows(List("Chernobyl [2019]", "Breaking Bad")) === List.empty
  }

  // Coffee Break: error handling strategies
  def addOrResign(parsedShows: Option[List[TvShow]], newParsedShow: Option[TvShow]): Option[List[TvShow]] = {
    for {
      shows      <- parsedShows
      parsedShow <- newParsedShow
    } yield shows.appended(parsedShow)
  }

  addOrResign(Some(List.empty), Some(TvShow("Chernobyl", 2019, 2019))) === Some(List(TvShow("Chernobyl", 2019, 2019)))
  addOrResign(Some(List(TvShow("Chernobyl", 2019, 2019))), Some(TvShow("The Wire", 2002, 2008))) === Some(List(
    TvShow("Chernobyl", 2019, 2019),
    TvShow("The Wire", 2002, 2008)
  ))
  addOrResign(Some(List(TvShow("Chernobyl", 2019, 2019))), None) === None
  addOrResign(None, Some(TvShow("Chernobyl", 2019, 2019))) === None
  addOrResign(None, None) === None

  // STEP 2b: implementing the "all-or-nothing" error handling strategy
  def parseShows(rawShows: List[String]): Option[List[TvShow]] = {
    val initialResult: Option[List[TvShow]] = Some(List.empty)
    rawShows
      .map(parseShow)
      .foldLeft(initialResult)(addOrResign)
  }

  parseShows(rawShows).map(_.map(_.title)) === Some(List("Breaking Bad", "The Wire", "Mad Men"))
  parseShows(rawShowsWithOneInvalid) === None
  parseShows(List("Chernobyl [2019]", "Breaking Bad (2008-2013)")) === None
  parseShows(List("Chernobyl [2019]", "Breaking Bad")) === None
  parseShows(List("Chernobyl (2019)", "Breaking Bad")) === None
  parseShows(List("Chernobyl (2019)")) === Some(List(TvShow("Chernobyl", 2019, 2019)))
  parseShows(List.empty) === Some(List.empty)

  // STEP 3: using Either to return descriptive errors
  {
    // introducing Either
    {
      def extractName(show: String): Either[String, String] = {
        val bracketOpen = show.indexOf('(')
        if (bracketOpen > 0) Right(show.substring(0, bracketOpen).trim)
        else Left(s"Can't extract name from $show")
      }

      extractName("(2022)") === Left("Can't extract name from (2022)")
    }
    {
      def extractName(show: String): Option[String] = {
        val bracketOpen = show.indexOf('(')
        if (bracketOpen > 0) Some(show.substring(0, bracketOpen).trim)
        else None
      }

      extractName("(2022)") === None
    }

    { // Understanding Either.map, flatten, toRight
      Right("1985").map(_.toIntOption) === Right(Some(1985))
      val yearStrEither: Either[String, String] = Left("Error")
      yearStrEither.map(_.toIntOption) === Left("Error")

      {
        val e: Either[String, String] = Right("1985")
        e.map(_.toIntOption) === Right(Some(1985))
      }

      {
        val e: Either[String, String] = Left("Error")
        e.map(_.toIntOption) === Left("Error")
      }

      Some(1985).toRight("Can't parse it") === Right(1985)
      None.toRight("Can't parse it") === Left("Can't parse it")

      List(List(1985)).flatten === List(1985)
      List(List()).flatten === List()
      Some(None).flatten === None
      Right(Left("Error")).flatten === Left("Error")
    }

    def extractYearStart(rawShow: String): Either[String, Int] = {
      val bracketOpen = rawShow.indexOf('(')
      val dash        = rawShow.indexOf('-')
      for {
        yearStr <- if (bracketOpen != -1 && dash > bracketOpen + 1) Right(rawShow.substring(bracketOpen + 1, dash))
                   else Left(s"Can't extract start year from $rawShow")
        year    <- yearStr.toIntOption.toRight(s"Can't parse $yearStr")
      } yield year
    }

    extractYearStart("The Wire (2002-2008)") === Right(2002)
    extractYearStart("The Wire (-2008)") === Left("Can't extract start year from The Wire (-2008)")
    extractYearStart("The Wire (oops-2008)") === Left("Can't parse oops")
    extractYearStart("The Wire (2002-)") === Right(2002)

    def extractName(rawShow: String): Either[String, String] = {
      val bracketOpen = rawShow.indexOf('(')
      if (bracketOpen > 0) Right(rawShow.substring(0, bracketOpen).trim)
      else Left(s"Can't extract name from $rawShow")
    }

    def extractYearEnd(rawShow: String): Either[String, Int] = {
      val dash         = rawShow.indexOf('-')
      val bracketClose = rawShow.indexOf(')')
      for {
        yearStr <- if (dash != -1 && bracketClose > dash + 1) Right(rawShow.substring(dash + 1, bracketClose))
                   else Left(s"Can't extract end year from $rawShow")
        year    <- yearStr.toIntOption.toRight(s"Can't parse $yearStr")
      } yield year
    }

    def extractSingleYear(rawShow: String): Either[String, Int] = {
      val dash         = rawShow.indexOf('-')
      val bracketOpen  = rawShow.indexOf('(')
      val bracketClose = rawShow.indexOf(')')

      for {
        yearStr <-
          if (
            dash == -1 && bracketOpen != -1 &&
            bracketClose > bracketOpen + 1
          ) Right(rawShow.substring(bracketOpen + 1, bracketClose))
          else Left(s"Can't extract single year from $rawShow")
        year    <- yearStr.toIntOption.toRight(s"Can't parse $yearStr")
      } yield year
    }

    def parseShow(rawShow: String): Either[String, TvShow] = {
      for {
        name      <- extractName(rawShow)
        yearStart <- extractYearStart(rawShow).orElse(extractSingleYear(rawShow))
        yearEnd   <- extractYearEnd(rawShow).orElse(extractSingleYear(rawShow))
      } yield TvShow(name, yearStart, yearEnd)
    }

    parseShow("Mad Men ()") === Left("Can't extract single year from Mad Men ()")
    parseShow("The Wire (-)") === Left("Can't extract single year from The Wire (-)")
    parseShow("The Wire (oops)") === Left("Can't parse oops")
    parseShow("(2002-2008)") === Left("Can't extract name from (2002-2008)")
    parseShow("The Wire (2002-2008)") === Right(TvShow("The Wire", 2002, 2008))

    { // Practicing functional error handling with Either
      def extractSingleYearOrYearEnd(rawShow: String): Either[String, Int] =
        extractSingleYear(rawShow).orElse(extractYearEnd(rawShow))

      def extractAnyYear(rawShow: String): Either[String, Int] =
        extractYearStart(rawShow).orElse(extractYearEnd(rawShow)).orElse(extractSingleYear(rawShow))

      def extractSingleYearIfNameExists(rawShow: String): Either[String, Int] =
        extractName(rawShow).flatMap(name => extractSingleYear(rawShow))

      def extractAnyYearIfNameExists(rawShow: String): Either[String, Int] =
        extractName(rawShow).flatMap(name => extractAnyYear(rawShow))

      extractSingleYearOrYearEnd("A (1992-)") === Left("Can't extract end year from A (1992-)")
      extractSingleYearOrYearEnd("B (2002)") === Right(2002)
      extractSingleYearOrYearEnd("C (-2012)") === Right(2012)
      extractSingleYearOrYearEnd("(2022)") === Right(2022)
      extractSingleYearOrYearEnd("E (-)") === Left("Can't extract end year from E (-)")

      extractAnyYear("A (1992-)") === Right(1992)
      extractAnyYear("B (2002)") === Right(2002)
      extractAnyYear("C (-2012)") === Right(2012)
      extractAnyYear("(2022)") === Right(2022)
      extractAnyYear("E (-)") === Left("Can't extract single year from E (-)")

      extractSingleYearIfNameExists("A (1992-)") === Left("Can't extract single year from A (1992-)")
      extractSingleYearIfNameExists("B (2002)") === Right(2002)
      extractSingleYearIfNameExists("C (-2012)") === Left("Can't extract single year from C (-2012)")
      extractSingleYearIfNameExists("(2022)") === Left("Can't extract name from (2022)")
      extractSingleYearIfNameExists("E (-)") === Left("Can't extract single year from E (-)")

      extractAnyYearIfNameExists("A (1992-)") === Right(1992)
      extractAnyYearIfNameExists("B (2002)") === Right(2002)
      extractAnyYearIfNameExists("C (-2012)") === Right(2012)
      extractAnyYearIfNameExists("(2022)") === Left("Can't extract name from (2022)")
      extractAnyYearIfNameExists("E (-)") === Left("Can't extract single year from E (-)")
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

    addOrResign(Right(List.empty), Right(TvShow("Chernobyl", 2019, 2019))) === Right(List(TvShow(
      "Chernobyl",
      2019,
      2019
    )))
    addOrResign(Right(List(TvShow("Chernobyl", 2019, 2019))), Right(TvShow("The Wire", 2002, 2008))) === Right(List(
      TvShow("Chernobyl", 2019, 2019),
      TvShow("The Wire", 2002, 2008)
    ))
    addOrResign(Left("something happened before Chernobyl"), Right(TvShow("Chernobyl", 2019, 2019))) === Left(
      "something happened before Chernobyl"
    )
    addOrResign(Right(List(TvShow("Chernobyl", 2019, 2019))), Left("invalid show")) === Left("invalid show")
    addOrResign(Left("hopeless"), Left("no hope")) === Left("hopeless")

    def parseShows(rawShows: List[String]): Either[String, List[TvShow]] = {
      val initialResult: Either[String, List[TvShow]] = Right(List.empty)
      rawShows
        .map(parseShow)
        .foldLeft(initialResult)(addOrResign)
    }

    parseShows(List("Chernobyl (2019)", "Breaking Bad (2008-2013)")).map(_.map(_.title)) === Right(List(
      "Chernobyl",
      "Breaking Bad"
    ))
    parseShows(List("Chernobyl [2019]", "Breaking Bad")) === Left("Can't extract name from Chernobyl [2019]")

    parseShows(List("The Wire (2002-2008)", "[2019]")) === Left("Can't extract name from [2019]")
    parseShows(List("The Wire (-)", "Chernobyl (2019)")) === Left("Can't extract single year from The Wire (-)")
    parseShows(List("The Wire (2002-2008)", "Chernobyl (2019)")) === Right(List(
      TvShow("The Wire", 2002, 2008),
      TvShow("Chernobyl", 2019, 2019)
    ))
  }

  { // Working with Option and Either: Option
    val year: Option[Int]   = Some(996)
    val noYear: Option[Int] = None

    // map
    year.map(_ * 2) === Some(1992)
    noYear.map(_ * 2) === None

    // flatten
    Some(year).flatten === Some(996)
    Some(noYear).flatten === None

    // flatMap
    year.flatMap(y => Some(y * 2)) === Some(1992)
    noYear.flatMap(y => Some(y * 2)) === None
    year.flatMap(y => None) === None
    noYear.flatMap(y => None) === None

    // orElse
    year.orElse(Some(2020)) === Some(996)
    noYear.orElse(Some(2020)) === Some(2020)
    year.orElse(None) === Some(996)
    noYear.orElse(None) === None

    // toRight
    year.toRight("no year given") === Right(996)
    noYear.toRight("no year given") === Left("no year given")
  }

  { // Working with Option and Either: Either
    val year: Either[String, Int]   = Right(996)
    val noYear: Either[String, Int] = Left("no year")

    // map
    year.map(_ * 2) === Right(1992)
    noYear.map(_ * 2) === Left("no year")

    // flatten
    Right(year).flatten === Right(996)
    Right(noYear).flatten === Left("no year")

    // flatMap
    year.flatMap(y => Right(y * 2)) === Right(1992)
    noYear.flatMap(y => Right(y * 2)) === Left("no year")
    year.flatMap(y => Left("can't progress")) === Left("can't progress")
    noYear.flatMap(y => Left("can't progress")) === Left("no year")

    // orElse
    year.orElse(Right(2020)) === Right(996)
    noYear.orElse(Right(2020)) === Right(2020)
    year.orElse(Left("can't recover")) === Right(996)
    noYear.orElse(Left("can't recover")) === Left("can't recover")

    // toOption
    year.toOption === Some(996)
    noYear.toOption === None
  }
}
