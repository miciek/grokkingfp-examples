object ch05_Events extends App {
  case class Event(name: String, start: Int, end: Int) { // using nulls
    def parseAdHoc(name: String, start: Int, end: Int): Event = { // named parse in the book
      if (name.size > 0 && end < 3000 & start <= end) Event(name, start, end)
      else null
    }

    assert(parseAdHoc("Apollo Program", 1961, 1972) == Event("Apollo Program", 1961, 1972))
    assert(parseAdHoc("", 1939, 1945) == null)
    assert(parseAdHoc("Event", 1949, 1945) == null)
  }

  { // using Option
    def parse(name: String, start: Int, end: Int): Option[Event] = {
      if (name.size > 0 && end < 3000 & start <= end) Some(Event(name, start, end))
      else None
    }

    assert(parse("Apollo Program", 1961, 1972) == Some(Event("Apollo Program", 1961, 1972)))
    assert(parse("", 1939, 1945) == None)
    assert(parse("Event", 1949, 1945) == None)
  }

  { // parsing as a pipeline
    def validateName(name: String): Option[String] = if (name.size > 0) Some(name) else None

    def validateEnd(end: Int): Option[Int] = if (end < 3000) Some(end) else None

    def validateStart(start: Int, end: Int): Option[Int] = if (start <= end) Some(start) else None

    def parse(name: String, start: Int, end: Int): Option[Event] = for {
      validName  <- validateName(name)
      validEnd   <- validateEnd(end)
      validStart <- validateStart(start, end)
    } yield Event(validName, validStart, validEnd)

    assert(parse("Apollo Program", 1961, 1972) == Some(Event("Apollo Program", 1961, 1972)))
    assert(parse("", 1939, 1945) == None)
    assert(parse("Event", 1949, 1945) == None)

    // Coffee break: Parsing with Option
    def validateLength(start: Int, end: Int, minLength: Int): Option[Int] =
      if (end - start >= minLength) Some(end - start) else None

    def parseLongEvent(name: String, start: Int, end: Int, minLength: Int): Option[Event] = for {
      validName   <- validateName(name)
      validEnd    <- validateEnd(end)
      validStart  <- validateStart(start, end)
      validLength <- validateLength(start, end, minLength)
    } yield Event(validName, validStart, validEnd)

    assert(parseLongEvent("Apollo Program", 1961, 1972, 10) == Some(Event("Apollo Program", 1961, 1972)))
    assert(parseLongEvent("World War II", 1939, 1945, 10) == None)
    assert(parseLongEvent("", 1939, 1945, 10) == None)
    assert(parseLongEvent("Apollo Program", 1972, 1961, 10) == None)
  }
}
