# Chapter 5: Flattening the Immutable

## .flatten and .flatMap

```tut:book:silent
case class Book(title: String, authors: List[String])

val scalaBooks = List(
  Book("Scala in Depth", List("Suereth")),
  Book("Functional Programming in Scala", List("Chiusano", "Bjarnason")),
  Book("Get Programming with Scala", List("Sfregola"))
)
```

```tut:invisible
val a1 = scalaBooks.map(_.authors)
assert(
  a1 == List(
    List("Suereth"),
    List("Chiusano", "Bjarnason"),
    List("Sfregola")
  )
)
```

```tut:book
scalaBooks.map(_.authors)
```

```tut:invisible
val a2 = scalaBooks.map(_.authors).flatten
assert(a2 == List("Suereth", "Chiusano", "Bjarnason", "Sfregola"))
```

```tut:book
scalaBooks.map(_.authors).flatten
```

```tut:invisible
val a3 = scalaBooks.flatMap(_.authors)
assert(a2 == a3)
```

```tut:book
scalaBooks.flatMap(_.authors)
```

## Coffee Break: Dealing with lists of lists

```tut:book:silent
def recommendedBooks(friend: String): List[Book] = {
  val scala = List(
    Book("FP in Scala", List("Chiusano", "Bjarnason")),
    Book("Scala in Depth", List("Suereth")))

  val fiction = List(
    Book("Harry Potter", List("Rowling")),
    Book("The Lord of the Rings", List("Tolkien")))

  if(friend == "Alice") scala
  else if(friend == "Bob") fiction
  else List.empty
}
```

```tut
val friends = List("Alice", "Bob")
val friendBooks = friends.map(recommendedBooks)
```

```tut
val recommendations = friendBooks.flatten
```

```tut:book
friends.flatMap(recommendedBooks)
```

```tut
val authors = recommendations.flatMap(_.authors)
```

```tut:book
friends.flatMap(recommendedBooks).flatMap(_.authors)
```

## Practical use case of using more flatMaps

```tut:book
friends.flatMap(recommendedBooks).flatMap(_.authors)
```

```tut:book:silent
def moreBooksByAuthor(author: String): List[Book] = {
  if(author == "Tolkien") List(Book("The Hobbit", List("Tolkien")))
  else List.empty
}
```

```tut:book
friends.flatMap(recommendedBooks).flatMap(_.authors).flatMap(moreBooksByAuthor)
```

## .flatMap and changing the size of the list

```tut:book:silent
val authors = List("Chiusano", "Bjarnason", "Suereth", "Rowling", "Tolkien")
```

```tut:book
val moreBooks = authors.map(moreBooksByAuthor)
moreBooks.flatten
```

```tut
List(1, 2, 3).flatMap(i => List(i, i + 10))
List(1, 2, 3).flatMap(i => List(i * 2))
List(1, 2, 3).flatMap(i =>
  if(i % 2 == 0) List(i) else List.empty)
```

## Values that depend on other values

```tut:book
friends.flatMap(recommendedBooks).flatMap(_.authors).flatMap(moreBooksByAuthor)
```

```tut:book
val recommendations = friends.flatMap(friend =>
  recommendedBooks(friend)
).flatMap(recommendation =>
  recommendation.authors
).flatMap(author =>
  moreBooksByAuthor(author)
)
```

```tut:book
recommendations.map(book => s"You may like ${book.title}")
```

```tut:book
val feed = friends.flatMap(friend => {
  recommendedBooks(friend).flatMap(recommendation => {
    recommendation.authors.flatMap(author => {
      moreBooksByAuthor(author).map(book => {
        s"You may like ${book.title}, " +
        s"because $friend recommended you " +
        s"another $author's book"
      })
    })
  })
})
```

```tut:invisible
assert(
  feed == List("You may like The Hobbit, because Bob recommended you another Tolkien's book")
)
```

## Better syntax for nested flatMaps

```tut:book
friends.flatMap(friend => {
  recommendedBooks(friend).flatMap(recommendation => {
    recommendation.authors.flatMap(author => {
      moreBooksByAuthor(author).map(book => {
        s"You may like ${book.title}, " +
        s"because $friend recommended you " +
        s"another $author's book"
      })
    })
  })
})
```

```tut:book
val feed = for {
  friend         <- friends
  recommendation <- recommendedBooks(friend)
  author         <- recommendation.authors
  book           <- moreBooksByAuthor(author)
} yield
  (s"You may like ${book.title}, " +
   s"because $friend recommended you " +
   s"another $author's book")
```

```tut:invisible
assert(
  feed == List("You may like The Hobbit, because Bob recommended you another Tolkien's book")
)
```

## Coffee Break: flatMaps vs for-comprehensions

```tut:book:silent
case class Point(x: Int, y: Int, color: String)

val xs     = List(1)
val ys     = List(-2, 7)
val colors = List("pink")
```

```tut:book
xs.flatMap(x => {
  ys.flatMap(y => {
    colors.map(color => {
      Point(x, y, color)
    })
  })
})
```

```tut
for {
  x     <- xs
  y     <- ys
  color <- colors
} yield Point(x, y, color)
```

```tut:book
def pointColors(x: Int, y: Int): List[String] = {
  if (x < 0 || y < 0) List("red", "blue")
  else List("pink")
}
```

```tut:book
xs.flatMap(x => {
  ys.flatMap(y => {
    pointColors(x, y).map(color => {
      Point(x, y, color)
    })
  })
})
```

```tut
for {
  x     <- xs
  y     <- ys
  color <- pointColors(x, y)
} yield Point(x, y, color)
```

## It's not the for you are looking for

```tut
val xs = List(1, 2, 3, 4, 5)
val result = for {
  x <- xs
} yield x * x
```

## Defining for comprehension

```tut:book
for {
  x <- Set(1, 2)
  y <- List(10, 100)
} yield x * y            // Set(10, 100, 20, 200)
```

```tut:book
for {
  x <- List(1, 2)
  y <- Set(10, 100)
} yield x * y            // List(10, 100, 20, 200)
```

## Practicing for comprehensions

```tut:book
for {
  x <- List(1, 2, 3)
  y <- Set(1)
} yield x * y           // List(1, 2, 3)
```

```tut:book
for {
  x <- Set(1, 2, 3)
  y <- List(1)
} yield x * y           // Set(1, 2, 3)
```

```tut:book
for {
  x <- List(1, 2, 3)
  y <- Set(1)
  z <- Set(0)
} yield x * y * z       // List(0, 0, 0)
```
