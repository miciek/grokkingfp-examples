import scala.collection.mutable

/**
  * Given the list of points and radiuses,
  * calculate which points are inside circles defined by these radiuses.
  *
  * SKILLS:
  * c) writing algorithms in a readable way using for comprehensions
  * d) using conditions in algorithms inside for comprehensions
  */
object PointsInsideCircles extends App {
  case class Point(x: Int, y: Int)

  val points   = List(Point(5, 2), Point(1, 1))
  val radiuses = List(2, 1)

  def isInside(point: Point, radius: Int): Boolean = {
    radius * radius >= point.x * point.x + point.y * point.y
  }

  def imperative = {
    val result = mutable.ListBuffer[String]()
    for (r <- radiuses) {
      for (point <- points) {
        if (isInside(point, r)) {
          result += s"$point is within a radius of $r"
        }
      }
    }
    result
  }

  val c1 = for {
    r     <- radiuses
    point <- points
  } yield
    s"$point is within a radius of $r: " +
    isInside(point, r).toString
  println(c1)
  assert(
    c1 == List(
      "Point(5,2) is within a radius of 2: false",
      "Point(1,1) is within a radius of 2: true",
      "Point(5,2) is within a radius of 1: false",
      "Point(1,1) is within a radius of 1: false"
    )
  )

  val d1 = for {
    r     <- radiuses
    point <- points.filter(p => isInside(p, r))
  } yield s"$point is within a radius of $r"
  println(d1)
  assert(d1 == List("Point(1,1) is within a radius of 2"))
  assert(d1 == imperative.toList)

  val d2 = for {
    r     <- radiuses
    point <- points
    if isInside(point, r)
  } yield s"$point is within a radius of $r"
  println(d2)
  assert(d2 == List("Point(1,1) is within a radius of 2"))
  assert(d2 == imperative.toList)

  def insideFilter(point: Point, radius: Int): List[Point] =
    if (isInside(point, radius)) List(point) else List.empty

  val d3 = for {
    r       <- radiuses
    point   <- points
    inPoint <- insideFilter(point, r)
  } yield s"$inPoint is within a radius of $r"
  println(d3)
  assert(d3 == List("Point(1,1) is within a radius of 2"))
  assert(d3 == imperative.toList)

  val riskyRadiuses = List(-10, 0, 2)
  val d1r_intro = for {
    r     <- riskyRadiuses
    point <- points.filter(p => isInside(p, r))
  } yield s"$point is within a radius of $r"
  assert(
    d1r_intro ==
      List(
        "Point(5,2) is within a radius of -10",
        "Point(1,1) is within a radius of -10",
        "Point(1,1) is within a radius of 2"
      )
  )
  println(d1r_intro)

  val d1r = for {
    r     <- riskyRadiuses.filter(r => r > 0)
    point <- points.filter(p => isInside(p, r))
  } yield s"$point is within a radius of $r"
  assert(d1r == List("Point(1,1) is within a radius of 2"))
  println(d1r)

  val d2r = for {
    r <- riskyRadiuses
    if r > 0
    point <- points
    if isInside(point, r)
  } yield s"$point is within a radius of $r"
  assert(d2r == List("Point(1,1) is within a radius of 2"))
  println(d2r)

  def validateRadius(radius: Int): List[Int] =
    if (radius > 0) List(radius) else List.empty

  val d3r = for {
    r           <- riskyRadiuses
    validRadius <- validateRadius(r)
    point       <- points
    inPoint     <- insideFilter(point, validRadius)
  } yield s"$inPoint is within a radius of $r"
  assert(d3r == List("Point(1,1) is within a radius of 2"))
  println(d3r)
}
