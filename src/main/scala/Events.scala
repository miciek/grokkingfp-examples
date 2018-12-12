object Events extends App {
  case class Event(name: String, start: Int, end: Int)

  def parseEvent1(name: String, start: Int, end: Int): Event = {
    if (name.size > 0 && end < 3000 & start <= end)
      Event(name, start, end)
    else
      Event("INVALID EVENT", 0, 0)
  }

  assert(parseEvent1("World War II", 1939, 1945) == Event("World War II", 1939, 1945))
  assert(parseEvent1("", 1939, 1945) == Event("INVALID EVENT", 0, 0))
  assert(parseEvent1("Event", 1949, 1945) == Event("INVALID EVENT", 0, 0))

  def parseEvent2(name: String, start: Int, end: Int): Option[Event] = {
    if (name.size > 0 && end < 3000 & start <= end)
      Some(Event(name, start, end))
    else
      None
  }

  assert(parseEvent2("World War II", 1939, 1945) == Some(Event("World War II", 1939, 1945)))
  assert(parseEvent2("", 1939, 1945) == None)
  assert(parseEvent2("Event", 1949, 1945) == None)

  def validateName(name: String): Option[String] =
    if (name.size > 0) Some(name) else None

  def validateEnd(end: Int): Option[Int] =
    if (end < 3000) Some(end) else None

  def validateStart(start: Int, end: Int): Option[Int] =
    if (start <= end) Some(start) else None

  def parseEvent(name: String, start: Int, end: Int): Option[Event] =
    for {
      validName  <- validateName(name)
      validEnd   <- validateEnd(end)
      validStart <- validateStart(start, end)
    } yield Event(validName, validStart, validEnd)

  assert(parseEvent("World War II", 1939, 1945) == Some(Event("World War II", 1939, 1945)))
  assert(parseEvent("", 1939, 1945) == None)
  assert(parseEvent("Event", 1949, 1945) == None)
}
