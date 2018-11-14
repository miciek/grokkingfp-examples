import scala.collection.mutable

/**
  * Given the list of points and radiuses,
  * calculate which points are inside circles defined by these radiuses.
  *
  * SKILLS:
  * b) writing algorithms using flatMap instead of for loops
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
    val result = mutable.MutableList[String]()
    for (point <- points) {
      for (r <- radiuses) {
        if (isInside(point, r)) {
          result += s"$point is inside circle with radius $r"
        }
      }
    }
    result
  }

  val b1 =
    points.flatMap(point => {
      radiuses.map(r => {
        s"$point is inside circle with radius $r: ${isInside(point, r)}"
      })
    })
  println(b1)
  assert(
    b1 == List(
      "Point(5,2) is inside circle with radius 2: false",
      "Point(5,2) is inside circle with radius 1: false",
      "Point(1,1) is inside circle with radius 2: true",
      "Point(1,1) is inside circle with radius 1: false"
    )
  )

  val c1 = for {
    point <- points
    r     <- radiuses
  } yield s"$point is inside circle with radius $r: ${isInside(point, r)}"
  assert(b1 == c1)

  val d1 = for {
    point <- points
    r     <- radiuses
    if isInside(point, r)
  } yield s"$point is inside circle with radius $r"
  println(d1)
  assert(d1 == List("Point(1,1) is inside circle with radius 2"))
  assert(d1 == imperative.toList)

  val riskyRadiuses = List(-10, 0, 2)
  val d2 = for {
    point <- points
    r     <- riskyRadiuses if r > 0
    if isInside(point, r)
  } yield s"$point is inside circle with radius $r"
  println(d2)
  assert(d1 == d2)
}
