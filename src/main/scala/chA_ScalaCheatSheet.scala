import model.Artist

object chA_ScalaCheatSheet extends App {
  // defining a value
  val x: Int    = 2022
  val y: String = "YYY"
  val z         = true // Boolean inferred

  // defining a function
  def f(a: Int): Int = {
    a + 1
  }

  def g(a: Int, b: String): Boolean = {
    a == b.length && z
  }

  // calling a function
  f(x) === 2023
  g(x, y) === false

  // creating immutable collections (e.g. Lists)
  val list: List[Int]  = List(1, 2, 3)
  val set: Set[String] = Set("Hello", "World")

  // passing function by name ch4 p82
  list.map(f) === List(2, 3, 4)

  // passing an anonymous function
  list.filter(i => i > 1) === List(2, 3) // double-arrow syntax for anonymous functions

  // passing an anonymous 2-parameter function
  // higher-order functions (ch4): e.g. foldLeft p123
  // - anonymous function with two parameters ch4 p123
  list.foldLeft(2020)((sum, i) => sum + i) === 2026

  // multiple parameter lists (currying) ch4 p117
  def h(a: Int)(b: List[Int]): Boolean = {
    b.contains(a)
  }

  val foo: List[Int] => Boolean = h(2020)

  // Math:
  // - Int.MinValue ch4 p125
  // - Math.max ch4 p125
  Math.max(Int.MinValue, 2022) === 2022

  // case class (product type) ch4 p126
  case class Book(title: String, numberOfChapters: Int)
  val grokkingFp = Book("Grokking Functional Programming", 12)

  // the dot syntax ch4 p126
  val books: List[Book] = List(grokkingFp, Book("Ferdydurke", 14))
  books.filter(book => book.numberOfChapters > 13) === List(Book("Ferdydurke", 14))

  // the underscore syntax ch4 p128
  books.filter(_.numberOfChapters > 13) === List(Book("Ferdydurke", 14))

  // missing implementation: ??? ch5 p133 (but not sure if it’s not used in ch1-3?)
  def isThisBookAnyGood(book: Book) = ???

  // string interpolation ch5 p140
  s"Reading ${grokkingFp.title} now!" === "Reading Grokking Functional Programming now!"

  // passing multi-line functions as arguments ch5 p141
  books.map(book =>
    if (book.numberOfChapters > 12) s"${book.title} is a long book"
    else s"${book.title} is a short book"
  ) === List("Grokking Functional Programming is a short book", "Ferdydurke is a long book")

  // type inference and empty list of ints ch5 p143
  val emptyList1            = List.empty[Int] // or:
  val emptyList2: List[Int] = List.empty

  // type inference and helping the compiler set the type of List ch5 p150
  val listOfDoubles1               = List[Double](1, 2, 3)
  val listOfDoubles2: List[Double] = List(1, 2, 3)

  // for comprehension ch5 p150
  (for {
    i    <- List(1, 2)
    book <- books
  } yield s"Person #$i read ${book.title}") === List(
    "Person #1 read Grokking Functional Programming",
    "Person #1 read Ferdydurke",
    "Person #2 read Grokking Functional Programming",
    "Person #2 read Ferdydurke"
  )

  // objects as modules, objects as bags for types and functions, importing
  object things {
    case class Thing(value: Int, description: String)
    def inflate(thing: Thing): Thing = thing.copy(value = thing.value + 2030)
  }
  things.inflate(things.Thing(3, "Just a thing")) === things.Thing(2033, "Just a thing")

  // opaque type (newtype)
  object model {
    opaque type BookRating = Int

    object BookRating {
      // creating new value
      def apply(rawRating: Int): BookRating    = Math.max(0, Math.min(5, rawRating))
      // extension functions p233
      extension (a: BookRating) def value: Int = a
    }
  }

  // importing everything from an object, import wildcard p235, p245, p257
  import model._

  // creating and using a value of an opaque type
  val rating: BookRating = BookRating(5)
  // rating / 2
  // Error: rating / 2 value / is not a member of model.BookRating
  val i: Int             = rating.value / 2
  i === 2

  // sum with case objects (singletons) p245, with product types p247 (ADTs)
  enum BookState {
    case ToRead
    case Reading(currentChapter: Int)
    case Finished(rating: BookRating)
  }
  import BookState._

  // pattern matching p251
  def updateBookState(book: Book, bookState: BookState): String = {
    bookState match {
      case ToRead                  => s"I want to read ${book.title}"
      case Finished(rating)        => s"I just finished ${book.title}! It's $rating/5!"
      case Reading(currentChapter) =>
        if (currentChapter <= book.numberOfChapters / 2) s"I have started reading ${book.title}"
        else s"I am finishing reading ${book.title}"
    }
  }

  // naming parameters in case class constructor or functions (ch7 p248)
  val b = Book(title = "Grokking Functional Programming", numberOfChapters = 12)
  updateBookState(
    book = b,
    bookState = Reading(currentChapter = 13)
  ) === "I am finishing reading Grokking Functional Programming"

  // trait, bag of functions p397
  trait BagOfFunctions {
    def f(x: Int): Boolean
    def g(y: Book): Int
  }

  // creating instance of a trait p408
  val bagOfFunctions = new BagOfFunctions {
    def f(x: Int): Boolean = x == 1
    def g(y: Book): Int    = y.numberOfChapters * 2
  }
  bagOfFunctions.f(2020) === false

  // turn Unit value in Scala p266, p267 (if a function returns Unit it means it does some impure things inside)
  val unit: Unit = ()

  //  Map type intro p313
  val book1                            = Book("Grokking Functional Programming", 12)
  val book2                            = Book("Ferdydurke", 14)
  val bookStates: Map[Book, BookState] = Map(
    book1 -> Reading(currentChapter = 13),
    book2 -> ToRead
  )

  // writing function that pattern match p322. we decided to use basic syntax in the book, but here are other ways you ca implement that in Scala
  bookStates.values.filter(bookState =>
    bookState match {
      case ToRead      => false
      case Reading(_)  => false
      case Finished(_) => true
    }
  ) === List.empty

  bookStates.values.filter(_ match {
    case ToRead      => false
    case Reading(_)  => false
    case Finished(_) => true
  }) === List.empty

  bookStates.values.filter {
    case ToRead      => false
    case Reading(_)  => false
    case Finished(_) => true
  } === List.empty

  // _ inside for comprehension p283
  //  _ as an unnamed value in for comprehension, Unit, p297
  (for {
    _    <- List(1, 2, 3)
    book <- List(Book("A", 7), Book("B", 13))
  } yield book.numberOfChapters) === List(7, 13, 7, 13, 7, 13)

  import scala.concurrent.duration._

  // - scala finiteduration 1.second 2.seconds mention
  val duration: FiniteDuration        = 1.second
  val durations: List[FiniteDuration] =
    List(100.millis, 2.seconds, 5.minutes, 500_000.hours) //  big numbers in scala 400_000 p365

  // multi-line strings p404
  """
    |Thanks
    |for
    |making
    |it
    |this
    |far!
    |""".stripMargin
}
