import ch05_BookAdaptations.books

/** Given the list of all interesting books,
  * return a feed of movie recommendations.
  */
object ch05_BookAdaptations extends App {
  case class Book(title: String, authors: List[String])
  case class Movie(title: String) {
    val books = List(
      Book("FP in Scala", List("Chiusano", "Bjarnason")),
      Book("The Hobbit", List("Tolkien")),
      Book("Modern Java in Action", List("Urma", "Fusco", "Mycroft"))
    )

    val scalaBooksQty1 = books
      .map(_.title)
      .filter(_.contains("Scala"))
      .size
    assert(scalaBooksQty1 == 1)

    val scalaBooksQty2 = books
      .map(book => book.title)
      .filter(title => title.contains("Scala"))
      .size
    assert(scalaBooksQty2 == 1)
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
  assert(a1 == List(List("Chiusano", "Bjarnason"), List("Tolkien")))

  val a2 = books.map(_.authors).flatten
  assert(a2 == List("Chiusano", "Bjarnason", "Tolkien"))

  val a3 = books.flatMap(_.authors)
  assert(a2 == a3)

  val authors    = List("Chiusano", "Bjarnason", "Tolkien")
  val movieLists = authors.map(bookAdaptations)
  assert(movieLists == List(
    List.empty,
    List.empty,
    List(Movie("An Unexpected Journey"), Movie("The Desolation of Smaug"))
  ))

  val b1 = movieLists.flatten

  val movies = books
    .flatMap(_.authors)
    .flatMap(bookAdaptations)

  assert(movies == List(Movie("An Unexpected Journey"), Movie("The Desolation of Smaug")))
  assert(b1 == movies)

  { // flatMap and changing the size of the list
    assert(List(1, 2, 3).flatMap(i => List(i, i + 10)).size == 6)
    assert(List(1, 2, 3).flatMap(i => List(i * 2)).size == 3)
    assert(List(1, 2, 3).flatMap(i => if (i % 2 == 0) List(i) else List.empty).size == 1)
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

  assert(c1 == List(
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

  assert(recommendationFeed(books) == (List(
    "You may like An Unexpected Journey, because you liked Tolkien's The Hobbit",
    "You may like The Desolation of Smaug, because you liked Tolkien's The Hobbit"
  )))

  // see "Practicing nested flatMaps" in ch05_Points2d3d

  val c2 = for {
    book   <- books
    author <- book.authors
    movie  <- bookAdaptations(author)
  } yield s"You may like ${movie.title}, " + s"because you liked $author's ${book.title}"

  assert(c1 == c2)

  // see "flatMaps vs. for comprehensions" in ch05_Points2d3d
}
