import java.util
import scala.jdk.CollectionConverters._

/** Given the list of all interesting books,
  * return a feed of movie recommendations.
  *
  * SKILLS:
  * a) dealing with lists of lists using .flatten
  * b) writing algorithms using flatMap instead of for loops
  * c) writing algorithms in a readable way using for comprehensions
  */
object ch05_BookAdaptations extends App {
  case class Book(title: String, authors: List[String])
  case class Movie(title: String)

  val books = List(
    Book("FP in Scala", List("Chiusano", "Bjarnason")),
    Book("The Hobbit", List("Tolkien"))
  )

  val scalaBooksQty = books.map(book => book.title).filter(title => title.contains("Scala")).size
  assert(scalaBooksQty == 1)

  def bookAdaptations(author: String): List[Movie] = {
    if (author == "Tolkien") List(Movie("An Unexpected Journey"), Movie("The Desolation of Smaug"))
    else List.empty
  }

  def recommendationFeed(books: List[Book]) = {
    val result = new util.ArrayList[String]()
    for (book <- books) {
      for (author <- book.authors) {
        for (movie <- bookAdaptations(author)) {
          result.add(
            s"You may like ${movie.title}, " +
            s"because you liked $author's ${book.title}"
          )
        }
      }
    }
    result
  }

  val a1 = books.map(book => book.authors)
  assert(
    a1 == List(
      List("Chiusano", "Bjarnason"),
      List("Tolkien")
    )
  )

  val a2 = books.map(book => book.authors).flatten
  assert(
    a2 == List("Chiusano", "Bjarnason", "Tolkien")
  )

  val a3 = books.flatMap(book => book.authors)
  assert(a2 == a3)

  val authors    = List("Chiusano", "Bjarnason", "Tolkien")
  val movieLists = authors.map(author => bookAdaptations(author))
  assert(
    movieLists == List(List.empty, List.empty, List(Movie("An Unexpected Journey"), Movie("The Desolation of Smaug")))
  )

  val b1 = movieLists.flatten

  val b2 = books
    .flatMap(book => book.authors)
    .flatMap(author => bookAdaptations(author))
  println(b2)
  assert(
    b2 == List(Movie("An Unexpected Journey"), Movie("The Desolation of Smaug"))
  )
  assert(b1 == b2)

  val c1 = books
    .flatMap(book => {
      book.authors.flatMap(author => {
        bookAdaptations(author).map(movie => {
          s"You may like ${movie.title}, " +
          s"because you liked $author's ${book.title}"
        })
      })
    })
  println(c1)

  assert(
    c1 == List(
      "You may like An Unexpected Journey, because you liked Tolkien's The Hobbit",
      "You may like The Desolation of Smaug, because you liked Tolkien's The Hobbit"
    )
  )

  val c2 = for {
    book   <- books
    author <- book.authors
    movie  <- bookAdaptations(author)
  } yield s"You may like ${movie.title}, " + s"because you liked $author's ${book.title}"

  assert(c1 == c2)
  assert(c1 == recommendationFeed(books).asScala.toList)
}
