object Book extends App {
  case class Book(title: String, authors: List[String])

  val scalaBooks = List(
    Book("Scala in Depth", List("Suereth")),
    Book("Functional Programming in Scala", List("Chiusano", "Bjarnason")),
    Book("Get Programming with Scala", List("Sfregola"))
  )

  val a1 = scalaBooks.map(_.authors)
  assert(
    a1 == List(
      List("Suereth"),
      List("Chiusano", "Bjarnason"),
      List("Sfregola")
    )
  )

  val a2 = scalaBooks.map(_.authors).flatten
  assert(
    a2 == List("Suereth", "Chiusano", "Bjarnason", "Sfregola")
  )

  val a3 = scalaBooks.flatMap(_.authors)
  assert(a2 == a3)

  def friendRecommendations(friend: String): List[Book] = {
    val fiction = List(Book("Harry Potter", List("Rowling")), Book("The Lord of the Rings", List("Tolkien")))
    val scala   = List(Book("FP in Scala", List("Chiusano", "Bjarnason")), Book("Scala in Depth", List("Suereth")))

    if (friend == "Alice") scala
    else if (friend == "Bob") fiction
    else List.empty
  }

  def moreBooksByAuthor(author: String): List[Book] = {
    if (author == "Tolkien")
      List(Book("The Hobbit", List("Tolkien")))
    else List.empty
  }

  val friends = List("Alice", "Bob")

  val b1 = friends
    .flatMap(friendRecommendations)
    .flatMap(_.authors)
    .flatMap(moreBooksByAuthor)
  println(b1)
  assert(
    b1 == List(Book("The Hobbit", List("Tolkien")))
  )

  val b2 = friends
    .flatMap(friend => friendRecommendations(friend))
    .flatMap(recommendation => recommendation.authors)
    .flatMap(author => moreBooksByAuthor(author))
  assert(b1 == b2)

  val authors   = List("Chiusano", "Bjarnason", "Suereth", "Rowling", "Tolkien")
  val moreBooks = authors.map(author => moreBooksByAuthor(author))
  assert(
    moreBooks == List(List.empty, List.empty, List.empty, List.empty, List(Book("The Hobbit", List("Tolkien"))))
  )

  val b3 = moreBooks.flatten
  assert(b2 == b3)

  val c1 = friends
    .flatMap(friend => {
      friendRecommendations(friend).flatMap(recommendation => {
        recommendation.authors.flatMap(author => {
          moreBooksByAuthor(author).map(book => {
            s"You may like ${book.title}, because $friend recommended you another $author's book"
          })
        })
      })
    })
  println(c1)

  assert(
    c1 == List("You may like The Hobbit, because Bob recommended you another Tolkien's book")
  )

  val c2 = for {
    friend         <- friends
    recommendation <- friendRecommendations(friend)
    author         <- recommendation.authors
    book           <- moreBooksByAuthor(author)
  } yield s"You may like ${book.title}, because $friend recommended you another $author's book"

  assert(c1 == c2)
}
