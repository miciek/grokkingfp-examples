object ch06_TvShows extends App {
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
      val yearStart = Integer.parseInt(rawShow.substring(bracketOpen + 1, dash))
      val yearEnd   = Integer.parseInt(rawShow.substring(dash + 1, bracketClose))

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
        name      <- Option.when(bracketOpen > 0)(rawShow.substring(0, bracketOpen).trim)
        yearStart <- Option.when(bracketOpen != -1 && dash > bracketOpen + 1)(
                       rawShow.substring(bracketOpen + 1, dash).toInt
                     )
        yearEnd   <- Option.when(dash != -1 && bracketClose > dash + 1)(rawShow.substring(dash + 1, bracketClose).toInt)
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
      if (bracketOpen > 0) Some(rawShow.substring(0, bracketOpen).trim)
      else None
    }

    def extractYearStart(rawShow: String): Option[Int] = {
      val bracketOpen = rawShow.indexOf('(')
      val dash        = rawShow.indexOf('-')
      for {
        yearStr <- if (bracketOpen != -1 && dash > bracketOpen + 1) Some(rawShow.substring(bracketOpen + 1, dash))
                   else None
        year    <- yearStr.trim.toIntOption
      } yield year
    }

    check(extractYearStart("Breaking Bad (2008-2013)")).expect(Some(2008))
    check(extractYearStart("Mad Men (-2015)")).expect(None)
    check(extractYearStart("(2002- 2008 ) The Wire")).expect(Some(2002))

    def extractYearEnd(rawShow: String): Option[Int] = {
      val dash         = rawShow.indexOf('-')
      val bracketClose = rawShow.indexOf(')')
      for {
        yearStr <- if (dash != -1 && bracketClose > dash + 1) Some(rawShow.substring(dash + 1, bracketClose))
                   else None
        year    <- yearStr.toIntOption
      } yield year
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

    println(parseShow1("Breaking Bad, 2008-2013"))
    assert(parseShow1("Breaking Bad (2008-2013)").contains(TvShow("Breaking Bad", 2008, 2013)))

    // INVALID INPUT
    check(parseShow1("Breaking Bad (2008-2013)")).expect(Some(TvShow("Breaking Bad", 2008, 2013)))
    check(parseShow1("Mad Men (-2015)")).expect(None)
    check(parseShow1("The Wire ( 2002 - 2008 )")).expect(None)

    def extractSingleYear(rawShow: String): Option[Int] = {
      val dash         = rawShow.indexOf('-')
      val bracketOpen  = rawShow.indexOf('(')
      val bracketClose = rawShow.indexOf(')')
      for {
        yearStr <- if (dash == -1 && bracketOpen != -1 && bracketClose > bracketOpen)
                     Some(rawShow.substring(bracketOpen + 1, bracketClose))
                   else None
        year    <- yearStr.toIntOption
      } yield year
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

      def extractAnyYearIfNameExists(rawShow: String): Option[Int] =
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
      println(extractAnyYearIfNameExists("A(2021)"))
      println(extractAnyYearIfNameExists("(2021)"))
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
    }.expect(Some(List(TvShow("Chernobyl", 2019, 2019))))
    check {
      addOrResign(Some(List(TvShow("Chernobyl", 2019, 2019))), Some(TvShow("The Wire", 2002, 2008)))
    }.expect(Some(List(TvShow("Chernobyl", 2019, 2019), TvShow("The Wire", 2002, 2008))))
    check {
      addOrResign(None, Some(TvShow("Chernobyl", 2019, 2019)))
    }.expect(None)
    check {
      addOrResign(Some(List(TvShow("Chernobyl", 2019, 2019))), None)
    }.expect(None)
    check {
      addOrResign(None, None)
    }.expect(None)

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
    { // Understanding Either.map, flatten, toRight
      check(Right("1985").map(_.toIntOption)).expect(Right(Some(1985)))
      val yearStrEither: Either[String, String] = Left("Error")
      check(yearStrEither.map(_.toIntOption)).expect(Left("Error"))

      check {
        val e: Either[String, String] = Right("1985")
        e.map(_.toIntOption)
      }.expect(Right(Some(1985)))

      check {
        val e: Either[String, String] = Left("Error")
        e.map(_.toIntOption)
      }.expect(Left("Error"))

      check(Some(1985).toRight("Can't parse it")).expect(Right(1985))
      check(None.toRight("Can't parse it")).expect(Left("Can't parse it"))

      check(List(List(1985)).flatten).expect(List(1985))
      check(List(List()).flatten).expect(List())
      check(Some(None).flatten).expect(None)
      check(Right(Left("Error")).flatten).expect(Left("Error"))
    }

    def extractName(rawShow: String): Either[String, String] = {
      val bracketOpen = rawShow.indexOf('(')
      if (bracketOpen > 0) Right(rawShow.substring(0, bracketOpen).trim)
      else Left(s"Can't extract name from $rawShow")
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

    check(extractYearStart("The Wire (2002-2008)")).expect(Right(2002))
    check(extractYearStart("The Wire (-2008)")).expect(Left("Can't extract start year from The Wire (-2008)"))
    check(extractYearStart("The Wire (oops-2008)")).expect(Left("Can't parse oops"))
    check(extractYearStart("The Wire (2002-)")).expect(Right(2002))

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
            bracketClose > bracketOpen
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

    check(parseShow("The Wire (-)")).expect(Left("Can't extract single year from The Wire (-)"))
    check(parseShow("The Wire (oops)")).expect(Left("Can't parse oops"))
    check(parseShow("(2002-2008)")).expect(Left("Can't extract name from (2002-2008)"))
    check(parseShow("The Wire (2002-2008)")).expect(Right(TvShow("The Wire", 2002, 2008)))

    { // Practicing functional error handling with Either
      def extractSingleYearOrYearEnd(rawShow: String): Either[String, Int] =
        extractSingleYear(rawShow).orElse(extractYearEnd(rawShow))

      def extractAnyYear(rawShow: String): Either[String, Int] =
        extractYearStart(rawShow).orElse(extractYearEnd(rawShow)).orElse(extractSingleYear(rawShow))

      def extractYearIfNameExists(rawShow: String): Either[String, Int] =
        extractName(rawShow).flatMap(name => extractSingleYear(rawShow))

      def extractAnyYearIfNameExists(rawShow: String): Either[String, Int] =
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
      println(extractAnyYearIfNameExists("A(2021)"))
      println(extractAnyYearIfNameExists("(2021)"))
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

    check(addOrResign(Right(List.empty), Right(TvShow("Chernobyl", 2019, 2019))))
      .expect(Right(List(TvShow("Chernobyl", 2019, 2019))))
    check(addOrResign(Right(List(TvShow("Chernobyl", 2019, 2019))), Right(TvShow("The Wire", 2002, 2008))))
      .expect(Right(List(TvShow("Chernobyl", 2019, 2019), TvShow("The Wire", 2002, 2008))))
    check(addOrResign(Left("something happened before Chernobyl"), Right(TvShow("Chernobyl", 2019, 2019))))
      .expect(Left("something happened before Chernobyl"))
    check(addOrResign(Right(List(TvShow("Chernobyl", 2019, 2019))), Left("invalid show"))).expect(Left("invalid show"))
    check(addOrResign(Left("hopeless"), Left("no hope"))).expect(Left("hopeless"))

    def parseShows(rawShows: List[String]): Either[String, List[TvShow]] = {
      val initialResult: Either[String, List[TvShow]] = Right(List.empty)
      rawShows
        .map(parseShow)
        .foldLeft(initialResult)(addOrResign)
    }

    println(parseShows(List("Chernobyl (2019)", "Breaking Bad (2008-2013)")))
    println(parseShows(List("Chernobyl [2019]", "Breaking Bad")))

    check(parseShows(List("The Wire (2002-2008)", "[2019]"))).expect(Left("Can't extract name from [2019]"))

    check(parseShows(List("The Wire (-)", "Chernobyl (2019)")))
      .expect(Left("Can't extract single year from The Wire (-)"))

    check(parseShows(List("The Wire (2002-2008)", "Chernobyl (2019)")))
      .expect(Right(List(TvShow("The Wire", 2002, 2008), TvShow("Chernobyl", 2019, 2019))))
  }

  { // Working with Option and Either: Option
    val year: Option[Int]   = Some(996)
    val noYear: Option[Int] = None

    // map
    check(year.map(_ * 2)).expect(Some(1992))
    check(noYear.map(_ * 2)).expect(None)

    // flatten
    check(Some(year).flatten).expect(Some(996))
    check(Some(noYear).flatten).expect(None)

    // flatMap
    check(year.flatMap(y => Some(y * 2))).expect(Some(1992))
    check(noYear.flatMap(y => Some(y * 2))).expect(None)
    check(year.flatMap(y => None)).expect(None)
    check(noYear.flatMap(y => None)).expect(None)

    // orElse
    check(year.orElse(Some(2020))).expect(Some(996))
    check(noYear.orElse(Some(2020))).expect(Some(2020))
    check(year.orElse(None)).expect(Some(996))
    check(noYear.orElse(None)).expect(None)

    // toRight
    check(year.toRight("no year given")).expect(Right(996))
    check(noYear.toRight("no year given")).expect(Left("no year given"))
  }

  { // Working with Option and Either: Either
    val year: Either[String, Int]   = Right(996)
    val noYear: Either[String, Int] = Left("no year")

    // map
    check(year.map(_ * 2)).expect(Right(1992))
    check(noYear.map(_ * 2)).expect(Left("no year"))

    // flatten
    check(Right(year).flatten).expect(Right(996))
    check(Right(noYear).flatten).expect(Left("no year"))

    // flatMap
    check(year.flatMap(y => Right(y * 2))).expect(Right(1992))
    check(noYear.flatMap(y => Right(y * 2))).expect(Left("no year"))
    check(year.flatMap(y => Left("can't progress"))).expect(Left("can't progress"))
    check(noYear.flatMap(y => Left("can't progress"))).expect(Left("no year"))

    // orElse
    check(year.orElse(Right(2020))).expect(Right(996))
    check(noYear.orElse(Right(2020))).expect(Right(2020))
    check(year.orElse(Left("can't recover"))).expect(Right(996))
    check(noYear.orElse(Left("can't recover"))).expect(Left("can't recover"))

    // toOption
    check(year.toOption).expect(Some(996))
    check(noYear.toOption).expect(None)
  }
}
