object ch05_RandomForComprehensions extends App {
  assert((for {
    a <- List[Int](1, 2)
    b <- List[Int](10, 100)
    c <- List[Double](0.5, 0.7)
    d <- List[Int](3)
  } yield (a * b * c + d).toString + "km") ==
    List("8.0km", "10.0km", "53.0km", "73.0km", "13.0km", "17.0km", "103.0km", "143.0km"))

  assert((for {
    greeting <- Set("Hello", "Hi there")
    name     <- Set("Alice", "Bob")
  } yield s"$greeting, $name!") ==
    Set("Hello, Alice!", "Hello, Bob!", "Hi there, Alice!", "Hi there, Bob!"))

  assert((for {
    a <- List(1, 2)
    b <- List(2, 1)
  } yield a * b) ==
    List(2, 1, 4, 2))

  assert((for {
    a <- Set(1, 2)
    b <- Set(2, 1)
  } yield a * b) ==
    Set(2, 1, 4))

  assert((for {
    a <- List(1, 2)
    b <- Set(2, 1)
  } yield a * b) ==
    List(2, 1, 4, 2))

  assert((for {
    a <- Set(1, 2)
    b <- List(2, 1)
  } yield a * b) ==
    Set(2, 1, 4))

  // Practicing for comprehensions
  assert((for {
    x <- List(1, 2, 3)
    y <- Set(1)
  } yield x * y) ==
    List(1, 2, 3))

  assert((for {
    x <- Set(1, 2, 3)
    y <- List(1)
  } yield x * y) ==
    Set(1, 2, 3))

  assert((for {
    x <- List(1, 2, 3)
    y <- Set(1)
    z <- Set(0)
  } yield x * y * z) ==
    List(0, 0, 0))
}
