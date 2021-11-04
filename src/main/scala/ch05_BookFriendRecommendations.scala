object ch05_BookFriendRecommendations extends App {
  case class Book(title: String, authors: List[String])

  def recommendedBooks(friend: String): List[Book] = {
    val scala =
      List(Book("FP in Scala", List("Chiusano", "Bjarnason")), Book("Get Programming with Scala", List("Sfregola")))

    val fiction = List(Book("Harry Potter", List("Rowling")), Book("The Lord of the Rings", List("Tolkien")))

    if (friend == "Alice") scala
    else if (friend == "Bob") fiction
    else List.empty
  }

  val friends      = List("Alice", "Bob", "Charlie")
  val friendsBooks = friends.map(recommendedBooks)
  check(friendsBooks).expect(List(
    List(Book("FP in Scala", List("Chiusano", "Bjarnason")), Book("Get Programming with Scala", List("Sfregola"))),
    List(Book("Harry Potter", List("Rowling")), Book("The Lord of the Rings", List("Tolkien"))),
    List.empty
  ))

  val recommendations = friends.flatMap(recommendedBooks)
  check(recommendations).expect(List(
    Book("FP in Scala", List("Chiusano", "Bjarnason")),
    Book("Get Programming with Scala", List("Sfregola")),
    Book("Harry Potter", List("Rowling")),
    Book("The Lord of the Rings", List("Tolkien"))
  ))

  val authors = recommendations.flatMap(_.authors)
  check(authors).expect(List("Chiusano", "Bjarnason", "Sfregola", "Rowling", "Tolkien"))

  val recommendedAuthors = friends
    .flatMap(recommendedBooks)
    .flatMap(_.authors)
  check(recommendedAuthors).expect(List("Chiusano", "Bjarnason", "Sfregola", "Rowling", "Tolkien"))

  // in FP, we use expressions:
  val xs     = List(1, 2, 3, 4, 5)
  val result = for {
    x <- xs
  } yield x * x
  check(result).expect(List(1, 4, 9, 16, 25))
}
