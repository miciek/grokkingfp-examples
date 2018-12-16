object RandomForComprehensions extends App {
  val r = for {
    a <- List[Int](1, 2)
    b <- List[Int](10, 100)
    c <- List[Double](0.5, 0.7)
    d <- List[Int](3)
  } yield (a * b * c + d).toString + "km"
  println(r)
  assert(
    r == List("8.0km", "10.0km", "53.0km", "73.0km", "13.0km", "17.0km", "103.0km", "143.0km")
  )

  val s = for {
    greeting <- Set("Hello", "Hi there")
    name     <- Set("Alice", "Bob")
  } yield s"$greeting, $name!"
  println(s)
  assert(
    s == Set("Hello, Alice!", "Hello, Bob!", "Hi there, Alice!", "Hi there, Bob!")
  )

  val dlist = for {
    a <- List(1, 2)
    b <- List(2, 1)
  } yield a * b
  println(dlist)
  assert(
    dlist == List(2, 1, 4, 2)
  )

  val dset = for {
    a <- Set(1, 2)
    b <- Set(2, 1)
  } yield a * b
  println(dset)
  assert(
    dset == Set(2, 1, 4)
  )

  val dmixedlist = for {
    a <- List(1, 2)
    b <- Set(2, 1)
  } yield a * b
  println(dmixedlist)
  assert(
    dmixedlist == List(2, 1, 4, 2)
  )

  val dmixedset = for {
    a <- Set(1, 2)
    b <- List(2, 1)
  } yield a * b
  println(dmixedset)
  assert(
    dmixedset == Set(2, 1, 4)
  )

  val dex1 = for {
    x <- List(1, 2, 3)
    y <- Set(1)
  } yield x * y
  assert(
    dex1 == List(1, 2, 3)
  )

  val dex2 = for {
    x <- Set(1, 2, 3)
    y <- List(1)
  } yield x * y
  assert(
    dex2 == Set(1, 2, 3)
  )

  val dex3 = for {
    x <- List(1, 2, 3)
    y <- Set(1)
    z <- Set(0)
  } yield x * y * z
  assert(
    dex3 == List(0, 0, 0)
  )
}
