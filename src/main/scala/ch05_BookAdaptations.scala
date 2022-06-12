import ch05_BookAdaptations.books

/** Given the list of all interesting books,
  * return a feed of movie recommendations.
  */
object ch05_BookAdaptations extends App {
  case class Book(title: String, authors: List[String])
  case class Movie(title: String)

  {
    val books = List(
      Book("FP in Scala", List("Chiusano", "Bjarnason")),
      Book("The Hobbit", List("Tolkien")),
      Book("Modern Java in Action", List("Urma", "Fusco", "Mycroft"))
    )

    val scalaBooksQty1 = books
      .map(_.title)
      .filter(_.contains("Scala"))
      .size
    check(scalaBooksQty1).expect(1)

    val scalaBooksQty2 = books
      .map(book => book.title)
      .filter(title => title.contains("Scala"))
      .size
    check(scalaBooksQty2).expect(1)
  }

  val books = List(
    Book("FP in Scala", List("Chiusano", "Bjarnason")),
    Book("The Hobbit", List("Tolkien"))
  )

  def bookAdaptations(author: String): List[Movie] = {
    if (author == "Tolkien") List(Movie("An Unexpected Journey"), Movie("The Desolation of Smaug"))
    else List.empty
  }

  val a1 = books.map(_.authors)
  check(a1).expect(List(
    List("Chiusano", "Bjarnason"),
    List("Tolkien")
  ))

  val a2 = books.map(_.authors).flatten
  check(a2).expect(List("Chiusano", "Bjarnason", "Tolkien"))

  val a3 = books.flatMap(_.authors)
  check(a2).expect(a3)

  val authors    = List("Chiusano", "Bjarnason", "Tolkien")
  val movieLists = authors.map(bookAdaptations)
  check(movieLists).expect(List(
    List.empty,
    List.empty,
    List(Movie("An Unexpected Journey"), Movie("The Desolation of Smaug"))
  ))

  val b1 = movieLists.flatten

  val movies = books
    .flatMap(_.authors)
    .flatMap(bookAdaptations)

  check(movies).expect(List(Movie("An Unexpected Journey"), Movie("The Desolation of Smaug")))
  check(b1).expect(movies)

  { // flatMap and changing the size of the list
    check(List(1, 2, 3).flatMap(i => List(i, i + 10))).expectThat(_.size == 6)
    check(List(1, 2, 3).flatMap(i => List(i * 2))).expectThat(_.size == 3)
    check(List(1, 2, 3).flatMap(i =>
      if (i % 2 == 0) List(i) else List.empty
    )).expectThat(_.size == 1)
  }

  // see ch05_BookFriendRecommendations
  // see ch05_SequencedNestedFlatMaps

  val c1 = books
    .flatMap(book =>
      book.authors.flatMap(author =>
        bookAdaptations(author).map(movie =>
          s"You may like ${movie.title}, " +
            s"because you liked $author's ${book.title}"
        )
      )
    )

  check(c1).expect(List(
    "You may like An Unexpected Journey, because you liked Tolkien's The Hobbit",
    "You may like The Desolation of Smaug, because you liked Tolkien's The Hobbit"
  ))

  def recommendationFeed(books: List[Book]) = {
    books.flatMap(book =>
      book.authors.flatMap(author =>
        bookAdaptations(author).map(movie =>
          s"You may like ${movie.title}, " +
            s"because you liked $author's ${book.title}"
        )
      )
    )
  }

  check(recommendationFeed(books)).expect(List(
    "You may like An Unexpected Journey, because you liked Tolkien's The Hobbit",
    "You may like The Desolation of Smaug, because you liked Tolkien's The Hobbit"
  ))

  // see "Practicing nested flatMaps" in ch05_Points2d3d

  val c2 = for {
    book   <- books
    author <- book.authors
    movie  <- bookAdaptations(author)
  } yield s"You may like ${movie.title}, " + s"because you liked $author's ${book.title}"

  check(c1).expect(c2)

  // see "flatMaps vs. for comprehensions" in ch05_Points2d3d
}
