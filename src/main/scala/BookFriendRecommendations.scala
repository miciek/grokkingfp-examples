/**
  * SKILLS:
  * b) writing algorithms using flatMap instead of for loops
  */
object BookFriendRecommendations extends App {
  case class Book(title: String, authors: List[String])

  def recommendedBooks(friend: String): List[Book] = {
    val scala = List(Book("FP in Scala", List("Chiusano", "Bjarnason")), Book("Scala in Depth", List("Suereth")))

    val fiction = List(Book("Harry Potter", List("Rowling")), Book("The Lord of the Rings", List("Tolkien")))

    if (friend == "Alice") scala
    else if (friend == "Bob") fiction
    else List.empty
  }

  val friends     = List("Alice", "Bob")
  val friendBooks = friends.map(recommendedBooks)
  assert(
    friendBooks == List(
      List(Book("FP in Scala", List("Chiusano", "Bjarnason")), Book("Scala in Depth", List("Suereth"))),
      List(Book("Harry Potter", List("Rowling")), Book("The Lord of the Rings", List("Tolkien")))
    )
  )

  val recommendations = friends.flatMap(recommendedBooks)
  assert(
    recommendations == List(
      Book("FP in Scala", List("Chiusano", "Bjarnason")),
      Book("Scala in Depth", List("Suereth")),
      Book("Harry Potter", List("Rowling")),
      Book("The Lord of the Rings", List("Tolkien"))
    )
  )
}
