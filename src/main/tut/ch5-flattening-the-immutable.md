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
def friendRecommendations(friend: String): List[Book] = {
  val fiction = List(
    Book("Harry Potter", List("Rowling")),
    Book("The Lord of the Rings", List("Tolkien")))

  val scala = List(
    Book("FP in Scala", List("Chiusano", "Bjarnason")),
    Book("Scala in Depth", List("Suereth")))

  if(friend == "Alice") scala
  else if(friend == "Bob") fiction
  else List.empty
}
```

```tut
val friends = List("Alice", "Bob")
val fr = friends.map(friendRecommendations)
```

```tut
val recommendations = fr.flatten
```

```tut
friends.flatMap(friendRecommendations)
```

```tut
recommendations.flatMap(_.authors)
```

```tut
friends.flatMap(friendRecommendations).flatMap(_.authors)
```

## Practical case of using more flatMaps

```tut:book:silent
def moreBooksByAuthor(author: String): List[Book] =
  if(author == "Tolkien") List(Book("The Hobbit", List("Tolkien"))) else List.empty
```

```tut
friends.flatMap(friendRecommendations).flatMap(_.authors).flatMap(moreBooksByAuthor)
```

## .flatMap and the size of the list

```tut:book:silent
val authors   = List("Chiusano", "Bjarnason", "Suereth", "Rowling", "Tolkien")
```

```tut:book
val moreBooks = authors.map(author => moreBooksByAuthor(author))
moreBooks.flatten
```

## Values that depend on other values

```tut
friends.flatMap(friendRecommendations).flatMap(_.authors).flatMap(moreBooksByAuthor)
```

```tut:book:fail:silent
friends.flatMap(friend =>
  friendRecommendations(friend)
).flatMap(recommendation =>
  recommendation.authors
).flatMap(author =>
  moreBooksByAuthor(author)
).flatMap(book => ???)
```

```tut:book
val recommendations = friends.flatMap(friend => {
  friendRecommendations(friend).flatMap(recommendation => {
    recommendation.authors.flatMap(author => {
      moreBooksByAuthor(author).map(book => {
        s"You may like ${book.title}, because $friend recommended you another $author's book"
      })
    })
  })
})
```

```tut:invisible
assert(
  recommendations == List("You may like The Hobbit, because Bob recommended you another Tolkien's book")
)
```
