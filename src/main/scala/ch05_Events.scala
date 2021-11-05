object ch05_Events extends App {
  case class Event(name: String, start: Int, end: Int)

  { // using nulls
    def parseEventAdHoc(name: String, start: Int, end: Int): Event = {
      if (name.size > 0 && end < 3000 & start <= end) Event(name, start, end)
      else null
    }

    check(parseEventAdHoc("Apollo Program", 1961, 1972)).expect(Event("Apollo Program", 1961, 1972))
    check(parseEventAdHoc("", 1939, 1945)).expect(null)
    check(parseEventAdHoc("Event", 1949, 1945)).expect(null)
  }

  { // using Option
    def parseEvent(name: String, start: Int, end: Int): Option[Event] = {
      if (name.size > 0 && end < 3000 & start <= end) Some(Event(name, start, end))
      else None
    }

    check(parseEvent("Apollo Program", 1961, 1972)).expect(Some(Event("Apollo Program", 1961, 1972)))
    check(parseEvent("", 1939, 1945)).expect(None)
    check(parseEvent("Event", 1949, 1945)).expect(None)
  }

  { // parsing as a pipeline
    def validateName(name: String): Option[String] = if (name.size > 0) Some(name) else None

    def validateEnd(end: Int): Option[Int] = if (end < 3000) Some(end) else None

    def validateStart(start: Int, end: Int): Option[Int] = if (start <= end) Some(start) else None

    def parseEvent(name: String, start: Int, end: Int): Option[Event] = for {
      validName  <- validateName(name)
      validEnd   <- validateEnd(end)
      validStart <- validateStart(start, end)
    } yield Event(validName, validStart, validEnd)

    check(parseEvent("Apollo Program", 1961, 1972)).expect(Some(Event("Apollo Program", 1961, 1972)))
    check(parseEvent("", 1939, 1945)).expect(None)
    check(parseEvent("Event", 1949, 1945)).expect(None)

    def validateLength(start: Int, end: Int, minLength: Int): Option[Int] =
      if (end - start >= minLength) Some(end - start) else None

    def parseLongEvent(name: String, start: Int, end: Int, minLength: Int): Option[Event] = for {
      validName   <- validateName(name)
      validEnd    <- validateEnd(end)
      validStart  <- validateStart(start, end)
      validLength <- validateLength(start, end, minLength)
    } yield Event(validName, validStart, validEnd)

    check(parseLongEvent("Apollo Program", 1961, 1972, 10)).expect(Some(Event("Apollo Program", 1961, 1972)))
    check(parseLongEvent("World War II", 1939, 1945, 10)).expect(None)
    check(parseLongEvent("", 1939, 1945, 10)).expect(None)
    check(parseLongEvent("Apollo Program", 1972, 1961, 10)).expect(None)
  }
}
