object Point extends App {
  case class Point(x: Int, y: Int, color: String)

  val xs     = List(1)
  val ys     = List(-2, 7)
  val colors = List("pink")

  val a1 = xs.flatMap(x => {
    ys.flatMap(y => {
      colors.map(color => {
        Point(x, y, color)
      })
    })
  })
  println(a1)
  assert(
    a1 == List(Point(1, -2, "pink"), Point(1, 7, "pink"))
  )

  val a2 = for {
    x     <- xs
    y     <- ys
    color <- colors
  } yield Point(x, y, color)
  assert(a1 == a2)

  def pointColors(x: Int, y: Int): List[String] = {
    if (x < 0 || y < 0) List("red", "blue")
    else List("pink")
  }

  val b1 = xs.flatMap(x => {
    ys.flatMap(y => {
      pointColors(x, y).map(color => {
        Point(x, y, color)
      })
    })
  })
  println(b1)
  assert(
    b1 == List(Point(1, -2, "red"), Point(1, -2, "blue"), Point(1, 7, "pink"))
  )

  val b2 = for {
    x     <- xs
    y     <- ys
    color <- pointColors(x, y)
  } yield Point(x, y, color)
  assert(b1 == b2)

  val b3 = for {
    x     <- xs if x >= 0
    y     <- ys if y >= 0
    color <- pointColors(x, y)
  } yield Point(x, y, color)
  println(b3)
  assert(b3 == List(Point(1, 7, "pink")))
}
