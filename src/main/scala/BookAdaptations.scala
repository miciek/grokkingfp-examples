/**
  * Given the list of all interesting books,
  * return a list of movie recommendations.
  */
object BookAdaptations extends App {
  case class Book(title: String, authors: List[String])
  case class Movie(title: String)

  val books = List(
    Book("FP in Scala", List("Chiusano", "Bjarnason")),
    Book("The Hobbit", List("Tolkien"))
  )

  def movieAdaptations(author: String): List[Movie] = {
    if (author == "Tolkien")
      List(Movie("An Unexpected Journey"), Movie("The Desolation of Smaug"))
    else
      List.empty
  }

  val a1 = books.map(_.authors)
  assert(
    a1 == List(
      List("Chiusano", "Bjarnason"),
      List("Tolkien")
    )
  )

  val a2 = books.map(_.authors).flatten
  assert(
    a2 == List("Chiusano", "Bjarnason", "Tolkien")
  )

  val a3 = books.flatMap(_.authors)
  assert(a2 == a3)

  val b1 = books
    .flatMap(book => book.authors)
    .flatMap(author => movieAdaptations(author))
  println(b1)
  assert(
    b1 == List(Movie("An Unexpected Journey"), Movie("The Desolation of Smaug"))
  )

  val authors   = List("Chiusano", "Bjarnason", "Tolkien")
  val moreBooks = authors.map(author => movieAdaptations(author))
  assert(
    moreBooks == List(List.empty, List.empty, List(Movie("An Unexpected Journey"), Movie("The Desolation of Smaug")))
  )

  val b2 = moreBooks.flatten
  assert(b1 == b2)

  val c1 = books
    .flatMap(book => {
      book.authors.flatMap(author => {
        movieAdaptations(author).map(movie => {
          s"You may like ${movie.title}, " +
          s"because you liked $author's ${book.title}"
        })
      })
    })
  println(c1)

  assert(
    c1 == List(
      "You may like An Unexpected Journey, because you liked Tolkien's The Hobbit",
      "You may like The Desolation of Smaug, because you liked Tolkien's The Hobbit",
    )
  )

  val c2 = for {
    book   <- books
    author <- book.authors
    movie  <- movieAdaptations(author)
  } yield
    s"You may like ${movie.title}, " +
    s"because you liked $author's ${book.title}"

  assert(c1 == c2)
}
