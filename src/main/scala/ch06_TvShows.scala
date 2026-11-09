import ch06_TvShows.extractYearStart

object ch06_TvShows extends App {
  case class TvShow(title: String, start: Int, end: Int)

  val shows = List(TvShow("Breaking Bad", 2008, 2013), TvShow("The Wire", 2002, 2008), TvShow("Mad Men", 2007, 2015))

  def sortShows(shows: List[TvShow]): List[TvShow] = {
    shows
      .sortBy(tvShow => tvShow.end - tvShow.start) // sortBy gets a function that returns an Int for a given TvShow
      .reverse                                     // sortBy sorts in natural order (from the smallest Int to the highest one), so we return a reverse of the List
  }

  assert(sortShows(shows).map(_.title) == List("Mad Men", "The Wire", "Breaking Bad"))

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
      val yearStart = Integer.parseInt(rawShow.substring(bracketOpen + 1, dash))  // or: .toInt
      val yearEnd   = Integer.parseInt(rawShow.substring(dash + 1, bracketClose)) // or: .toInt

      TvShow(name, yearStart, yearEnd)
    }

    assert(parseShow("Breaking Bad (2008-2013)") == TvShow("Breaking Bad", 2008, 2013))

    def parseShows(rawShows: List[String]): List[TvShow] = {
      rawShows.map(parseShow)
    }

    assert(parseShows(rawShows) == List(
      TvShow("Breaking Bad", 2008, 2013),
      TvShow("The Wire", 2002, 2008),
      TvShow("Mad Men", 2007, 2015)
    ))

    // INVALID INPUT
    // val invalidRawShow = "Breaking Bad, 2008-2013"
    // parseShow(invalidRawShow)
    // parseShows(invalidRawShows)

    try {
      parseShow("Chernobyl (2019)")
    } catch {
      case exception: Exception => println(exception)
    }

    // see ch06_TvShowsJava for more imperative examples
  }

  // STEP 1a: using Option without implementing it yet
  // let's first see how we would use and test it:
  def testOptionBasedParseShow(parseShow: String => Option[TvShow]) = {
    assert(parseShow("The Wire (2002-2008)") == Some(TvShow("The Wire", 2002, 2008)))
    assert(parseShow("The Wire aired from 2002 to 2008") == None)
    assert(parseShow("Breaking Bad (2008-2013)") == Some(TvShow("Breaking Bad", 2008, 2013)))
    assert(parseShow("Mad Men (2007-2015)") == Some(TvShow("Mad Men", 2007, 2015)))
    assert(parseShow("Scrubs (2001-2010)") == Some(TvShow("Scrubs", 2001, 2010)))

    // INVALID INPUT
    assert(parseShow("Breaking Bad ()") == None)
    assert(parseShow("()") == None)
    assert(parseShow(") - (Breaking Bad, 2008-2013") == None)
    assert(parseShow("Mad Men (-2015)") == None)
    assert(parseShow("The Wire ( 2002 - 2008 )") == None)
    assert(parseShow("Stranger Things (2016-)") == None)
  }

  // STEP 1b: using Option, but implementing smaller functions that return Options first:
  { // the step-by-step implementation before the for-comprehension refactoring
    def extractYearStart(rawShow: String): Option[Int] = {
      val bracketOpen = rawShow.indexOf('(')
      val dash        = rawShow.indexOf('-')
      val yearStrOpt  =
        if (bracketOpen != -1 && dash > bracketOpen + 1) Some(rawShow.substring(bracketOpen + 1, dash)) else None
      yearStrOpt.map(yearStr => yearStr.toIntOption).flatten
    }

    assert(extractYearStart("Breaking Bad (2008-2013)") == Some(2008))
    assert(extractYearStart("Mad Men (-2015)") == None)
    assert(extractYearStart("(2002- N/A ) The Wire") == Some(2002))
  }

  // we will use a for-comprehension version
  def extractYearStart(rawShow: String): Option[Int] = {
    val bracketOpen = rawShow.indexOf('(')
    val dash        = rawShow.indexOf('-')
    for {
      yearStr <- if (bracketOpen != -1 && dash > bracketOpen + 1) Some(rawShow.substring(bracketOpen + 1, dash))
                 else None
      year    <- yearStr.toIntOption
    } yield year
  }

  assert(extractYearStart("Breaking Bad (2008-2013)") == Some(2008))
  assert(extractYearStart("Mad Men (-2015)") == None)
  assert(extractYearStart("(2002- N/A ) The Wire") == Some(2002))

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
    assert(parseShow("Chernobyl (2019)") == None)
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
    assert(parseShow("Chernobyl (2019)") == None)
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
  assert(parseShow("Chernobyl (2019)") == Some(TvShow("Chernobyl", 2019, 2019)))

  { // introducing orElse
    val seven: Option[Int] = Some(7)
    val eight: Option[Int] = Some(8)
    val none: Option[Int]  = None

    assert(seven.orElse(eight) == Some(7))
    assert(none.orElse(eight) == Some(8))
    assert(seven.orElse(none) == Some(7))
    assert(none.orElse(none) == None)

    val chernobyl = "Chernobyl (2019)"
    assert(extractYearStart(chernobyl) == None)
    assert(extractSingleYear(chernobyl) == Some(2019))
    assert(extractYearStart(chernobyl).orElse(extractSingleYear(chernobyl)) == Some(2019))
    assert(extractYearStart(chernobyl).orElse(extractSingleYear("not-a-year")) == None)
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

    assert(extractSingleYearOrYearEnd("A (1992-)") == None)
    assert(extractSingleYearOrYearEnd("B (2002)") == Some(2002))
    assert(extractSingleYearOrYearEnd("C (-2012)") == Some(2012))
    assert(extractSingleYearOrYearEnd("(2022)") == Some(2022))
    assert(extractSingleYearOrYearEnd("E (-)") == None)

    assert(extractAnyYear("A (1992-)") == Some(1992))
    assert(extractAnyYear("B (2002)") == Some(2002))
    assert(extractAnyYear("C (-2012)") == Some(2012))
    assert(extractAnyYear("(2022)") == Some(2022))
    assert(extractAnyYear("E (-)") == None)

    assert(extractSingleYearIfNameExists("A (1992-)") == None)
    assert(extractSingleYearIfNameExists("B (2002)") == Some(2002))
    assert(extractSingleYearIfNameExists("C (-2012)") == None)
    assert(extractSingleYearIfNameExists("(2022)") == None)
    assert(extractSingleYearIfNameExists("E (-)") == None)

    assert(extractAnyYearIfNameExists("A (1992-)") == Some(1992))
    assert(extractAnyYearIfNameExists("B (2002)") == Some(2002))
    assert(extractAnyYearIfNameExists("C (-2012)") == Some(2012))
    assert(extractAnyYearIfNameExists("(2022)") == None)
    assert(extractAnyYearIfNameExists("E (-)") == None)
  }

  { // introducing toList
    assert(Some(7).toList == List(7))
    assert(None.toList == List())
  }

  val rawShowsWithOneInvalid = List("Breaking Bad (2008-2013)", "The Wire 2002 2008", "Mad Men (2007-2015)")

  { // STEP 2a: trying to blindly implement parseShows by following only the compiler ("best-effort" error handling strategy)
    def parseShows(rawShows: List[String]): List[TvShow] = {
      rawShows          // List[String]
        .map(parseShow) // List[Option[TvShow]]
        .map(_.toList)  // List[List[TvShow]]
        .flatten        // List[TvShow]
    }

    { // example from the introduction to this section
      val rawShows = List("The Wire (2002-2008)", "Chernobyl (2019)")
      assert(parseShows(rawShows) == List(TvShow("The Wire", 2002, 2008), TvShow("Chernobyl", 2019, 2019)))
    }

    assert(parseShows(rawShowsWithOneInvalid) == List(
      TvShow("Breaking Bad", 2008, 2013),
      TvShow("Mad Men", 2007, 2015)
    ))
    assert(parseShows(List("Chernobyl [2019]", "Breaking Bad (2008-2013)")) == List(TvShow("Breaking Bad", 2008, 2013)))
    assert(parseShows(List("Chernobyl [2019]", "Breaking Bad")) == List.empty)

    {
      val rawShows = List("Breaking Bad (2008-2013)", "The Wire 2002 2008", "Mad Men (2007-2015)")
      assert(parseShows(rawShows) == List(TvShow("Breaking Bad", 2008, 2013), TvShow("Mad Men", 2007, 2015)))
    }
  }

  // Coffee Break: error handling strategies
  def addOrResign(parsedShows: Option[List[TvShow]], newParsedShow: Option[TvShow]): Option[List[TvShow]] = {
    for {
      shows      <- parsedShows
      parsedShow <- newParsedShow
    } yield shows.appended(parsedShow)
  }

  assert(addOrResign(Some(List.empty), Some(TvShow("Chernobyl", 2019, 2019))) == Some(List(TvShow(
    "Chernobyl",
    2019,
    2019
  ))))
  assert(addOrResign(Some(List(TvShow("Chernobyl", 2019, 2019))), Some(TvShow("The Wire", 2002, 2008))) == Some(List(
    TvShow("Chernobyl", 2019, 2019),
    TvShow("The Wire", 2002, 2008)
  )))
  assert(addOrResign(Some(List(TvShow("Chernobyl", 2019, 2019))), None) == None)
  assert(addOrResign(None, Some(TvShow("Chernobyl", 2019, 2019))) == None)
  assert(addOrResign(None, None) == None)

  // STEP 2b: implementing the "all-or-nothing" error handling strategy
  def parseShows(rawShows: List[String]): Option[List[TvShow]] = {
    val initialResult: Option[List[TvShow]] = Some(List.empty)
    rawShows
      .map(parseShow)
      .foldLeft(initialResult)(addOrResign)
  }

  assert(parseShows(rawShows).map(_.map(_.title)) == Some(List("Breaking Bad", "The Wire", "Mad Men")))
  assert(parseShows(rawShowsWithOneInvalid) == None)
  assert(parseShows(List("Chernobyl (2019)", "Breaking Bad (2008-2013)")) == Some(List(
    TvShow("Chernobyl", 2019, 2019),
    TvShow("Breaking Bad", 2008, 2013)
  )))
  assert(parseShows(List("Chernobyl [2019]", "Breaking Bad (2008-2013)")) == None)
  assert(parseShows(List("Chernobyl [2019]", "Breaking Bad")) == None)
  assert(parseShows(List("Chernobyl (2019)", "Breaking Bad")) == None)
  assert(parseShows(List("Chernobyl (2019)")) == Some(List(TvShow("Chernobyl", 2019, 2019))))
  assert(parseShows(List.empty) == Some(List.empty))

  // STEP 3: using Either to return descriptive errors
  {
    // introducing Either
    {
      def extractName(show: String): Either[String, String] = {
        val bracketOpen = show.indexOf('(')
        if (bracketOpen > 0) Right(show.substring(0, bracketOpen).trim)
        else Left(s"Can't extract name from $show")
      }

      assert(extractName("(2022)") == Left("Can't extract name from (2022)"))
    }
    {
      def extractName(show: String): Option[String] = {
        val bracketOpen = show.indexOf('(')
        if (bracketOpen > 0) Some(show.substring(0, bracketOpen).trim)
        else None
      }

      assert(extractName("(2022)") == None)
    }

    { // the step-by-step implementation before the for-comprehension refactoring
      def extractYearStart(rawShow: String): Either[String, Int] = {
        val bracketOpen   = rawShow.indexOf('(')
        val dash          = rawShow.indexOf('-')
        val yearStrEither =
          if (bracketOpen != -1 && dash > bracketOpen + 1) Right(rawShow.substring(bracketOpen + 1, dash))
          else Left(s"Can't extract start year from $rawShow")
        yearStrEither.map(yearStr => yearStr.toIntOption.toRight(s"Can't parse $yearStr")).flatten
      }

      assert(extractYearStart("The Wire (2002-2008)") == Right(2002))
      assert(extractYearStart("The Wire (-2008)") == Left("Can't extract start year from The Wire (-2008)"))
      assert(extractYearStart("The Wire (oops-2008)") == Left("Can't parse oops"))
      assert(extractYearStart("The Wire (2002-)") == Right(2002))

      // see the for-comprehension version below
    }

    { // Understanding Either.map, flatten, toRight
      assert(Right("1985").map(_.toIntOption) == Right(Some(1985)))
      val yearStrEither: Either[String, String] = Left("Error")
      assert(yearStrEither.map(_.toIntOption) == Left("Error"))

      {
        val e: Either[String, String] = Right("1985")
        assert(e.map(_.toIntOption) == Right(Some(1985)))
      }

      {
        val e: Either[String, String] = Left("Error")
        assert(e.map(_.toIntOption) == Left("Error"))
      }

      assert(Some(1985).toRight("Can't parse it") == Right(1985))
      assert(None.toRight("Can't parse it") == Left("Can't parse it"))

      assert(List(List(1985)).flatten == List(1985))
      assert(List(List()).flatten == List())
      assert(Some(Some(1985)).flatten == Some(1985))
      assert(Some(None).flatten == None)
      assert(Right(Right(1985)).flatten == Right(1985))
      assert(Right(Left("Error")).flatten == Left("Error"))
    }

    // we will use a for-comprehension version
    def extractYearStart(rawShow: String): Either[String, Int] = {
      val bracketOpen = rawShow.indexOf('(')
      val dash        = rawShow.indexOf('-')
      for {
        yearStr <- if (bracketOpen != -1 && dash > bracketOpen + 1) Right(rawShow.substring(bracketOpen + 1, dash))
                   else Left(s"Can't extract start year from $rawShow")
        year    <- yearStr.toIntOption.toRight(s"Can't parse $yearStr")
      } yield year
    }

    assert(extractYearStart("The Wire (2002-2008)") == Right(2002))
    assert(extractYearStart("The Wire (-2008)") == Left("Can't extract start year from The Wire (-2008)"))
    assert(extractYearStart("The Wire (oops-2008)") == Left("Can't parse oops"))
    assert(extractYearStart("The Wire (2002-)") == Right(2002))

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

    assert(parseShow("Mad Men ()") == Left("Can't extract single year from Mad Men ()"))
    assert(parseShow("The Wire (-)") == Left("Can't extract single year from The Wire (-)"))
    assert(parseShow("The Wire (oops)") == Left("Can't parse oops"))
    assert(parseShow("(2002-2008)") == Left("Can't extract name from (2002-2008)"))
    assert(parseShow("The Wire (2002-2008)") == Right(TvShow("The Wire", 2002, 2008)))

    { // Practicing functional error handling with Either
      def extractSingleYearOrYearEnd(rawShow: String): Either[String, Int] =
        extractSingleYear(rawShow).orElse(extractYearEnd(rawShow))

      def extractAnyYear(rawShow: String): Either[String, Int] =
        extractYearStart(rawShow).orElse(extractYearEnd(rawShow)).orElse(extractSingleYear(rawShow))

      def extractSingleYearIfNameExists(rawShow: String): Either[String, Int] =
        extractName(rawShow).flatMap(name => extractSingleYear(rawShow))

      def extractAnyYearIfNameExists(rawShow: String): Either[String, Int] =
        extractName(rawShow).flatMap(name => extractAnyYear(rawShow))

      assert(extractSingleYearOrYearEnd("A (1992-)") == Left("Can't extract end year from A (1992-)"))
      assert(extractSingleYearOrYearEnd("B (2002)") == Right(2002))
      assert(extractSingleYearOrYearEnd("C (-2012)") == Right(2012))
      assert(extractSingleYearOrYearEnd("(2022)") == Right(2022))
      assert(extractSingleYearOrYearEnd("E (-)") == Left("Can't extract end year from E (-)"))

      assert(extractAnyYear("A (1992-)") == Right(1992))
      assert(extractAnyYear("B (2002)") == Right(2002))
      assert(extractAnyYear("C (-2012)") == Right(2012))
      assert(extractAnyYear("(2022)") == Right(2022))
      assert(extractAnyYear("E (-)") == Left("Can't extract single year from E (-)"))

      assert(extractSingleYearIfNameExists("A (1992-)") == Left("Can't extract single year from A (1992-)"))
      assert(extractSingleYearIfNameExists("B (2002)") == Right(2002))
      assert(extractSingleYearIfNameExists("C (-2012)") == Left("Can't extract single year from C (-2012)"))
      assert(extractSingleYearIfNameExists("(2022)") == Left("Can't extract name from (2022)"))
      assert(extractSingleYearIfNameExists("E (-)") == Left("Can't extract single year from E (-)"))

      assert(extractAnyYearIfNameExists("A (1992-)") == Right(1992))
      assert(extractAnyYearIfNameExists("B (2002)") == Right(2002))
      assert(extractAnyYearIfNameExists("C (-2012)") == Right(2012))
      assert(extractAnyYearIfNameExists("(2022)") == Left("Can't extract name from (2022)"))
      assert(extractAnyYearIfNameExists("E (-)") == Left("Can't extract single year from E (-)"))
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

    assert(addOrResign(Right(List.empty), Right(TvShow("Chernobyl", 2019, 2019))) == Right(List(TvShow(
      "Chernobyl",
      2019,
      2019
    ))))
    assert(
      addOrResign(Right(List(TvShow("Chernobyl", 2019, 2019))), Right(TvShow("The Wire", 2002, 2008))) == Right(List(
        TvShow("Chernobyl", 2019, 2019),
        TvShow("The Wire", 2002, 2008)
      ))
    )
    assert(addOrResign(Left("something happened before Chernobyl"), Right(TvShow("Chernobyl", 2019, 2019))) == Left(
      "something happened before Chernobyl"
    ))
    assert(addOrResign(Right(List(TvShow("Chernobyl", 2019, 2019))), Left("invalid show")) == Left("invalid show"))
    assert(addOrResign(Left("hopeless"), Left("no hope")) == Left("hopeless"))

    def parseShows(rawShows: List[String]): Either[String, List[TvShow]] = {
      val initialResult: Either[String, List[TvShow]] = Right(List.empty)
      rawShows
        .map(parseShow)
        .foldLeft(initialResult)(addOrResign)
    }

    assert(parseShows(List("Chernobyl (2019)", "Breaking Bad (2008-2013)")).map(_.map(_.title)) == Right(List(
      "Chernobyl",
      "Breaking Bad"
    )))
    assert(parseShows(List("Chernobyl [2019]", "Breaking Bad")) == Left("Can't extract name from Chernobyl [2019]"))

    assert(parseShows(List("The Wire (2002-2008)", "[2019]")) == Left("Can't extract name from [2019]"))
    assert(parseShows(List("The Wire (-)", "Chernobyl (2019)")) == Left("Can't extract single year from The Wire (-)"))
    assert(parseShows(List("The Wire (2002-2008)", "Chernobyl (2019)")) == Right(List(
      TvShow("The Wire", 2002, 2008),
      TvShow("Chernobyl", 2019, 2019)
    )))
  }

  { // Working with Option and Either: Option
    val year: Option[Int]   = Some(996)
    val noYear: Option[Int] = None

    // map
    assert(year.map(_ * 2) == Some(1992))
    assert(noYear.map(_ * 2) == None)

    // flatten
    assert(Some(year).flatten == Some(996))
    assert(Some(noYear).flatten == None)

    // flatMap
    assert(year.flatMap(y => Some(y * 2)) == Some(1992))
    assert(noYear.flatMap(y => Some(y * 2)) == None)
    assert(year.flatMap(y => None) == None)
    assert(noYear.flatMap(y => None) == None)

    // orElse
    assert(year.orElse(Some(2020)) == Some(996))
    assert(noYear.orElse(Some(2020)) == Some(2020))
    assert(year.orElse(None) == Some(996))
    assert(noYear.orElse(None) == None)

    // toRight
    assert(year.toRight("no year given") == Right(996))
    assert(noYear.toRight("no year given") == Left("no year given"))
  }

  { // Working with Option and Either: Either
    val year: Either[String, Int]   = Right(996)
    val noYear: Either[String, Int] = Left("no year")

    // map
    assert(year.map(_ * 2) == Right(1992))
    assert(noYear.map(_ * 2) == Left("no year"))

    // flatten
    assert(Right(year).flatten == Right(996))
    assert(Right(noYear).flatten == Left("no year"))

    // flatMap
    assert(year.flatMap(y => Right(y * 2)) == Right(1992))
    assert(noYear.flatMap(y => Right(y * 2)) == Left("no year"))
    assert(year.flatMap(y => Left("can't progress")) == Left("can't progress"))
    assert(noYear.flatMap(y => Left("can't progress")) == Left("no year"))

    // orElse
    assert(year.orElse(Right(2020)) == Right(996))
    assert(noYear.orElse(Right(2020)) == Right(2020))
    assert(year.orElse(Left("can't recover")) == Right(996))
    assert(noYear.orElse(Left("can't recover")) == Left("can't recover"))

    // toOption
    assert(year.toOption == Some(996))
    assert(noYear.toOption == None)
  }
}
