object RandomForComprehensions extends App {
  val r = for {
    a <- List(1, 2)
    b <- List(10, 100)
    c <- List(0.5, 0.7)
    d <- List(3)
  } yield (a * b * c + d).toString + "km"
  println(r)
}
