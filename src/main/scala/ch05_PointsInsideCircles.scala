import scala.collection.mutable

/** Given a list of points and radiuses,
  * calculate which points are inside circles defined by these radiuses.
  */
object ch05_PointsInsideCircles extends App {
  case class Point(x: Int, y: Int)

  val points   = List(Point(5, 2), Point(1, 1))
  val radiuses = List(2, 1)

  def isInside(point: Point, radius: Int): Boolean = {
    radius * radius >= point.x * point.x + point.y * point.y
  }

  check(for {
    r     <- radiuses
    point <- points
  } yield s"$point is within a radius of $r: " + isInside(point, r).toString).expect(
    List(
      "Point(5,2) is within a radius of 2: false",
      "Point(1,1) is within a radius of 2: true",
      "Point(5,2) is within a radius of 1: false",
      "Point(1,1) is within a radius of 1: false"
    )
  )

  // FILTERING TECHNIQUES

  // using filter
  check(for {
    r     <- radiuses
    point <- points.filter(p => isInside(p, r))
  } yield s"$point is within a radius of $r").expect(
    List("Point(1,1) is within a radius of 2")
  )

  // using a guard expression
  check(for {
    r     <- radiuses
    point <- points
    if isInside(point, r)
  } yield s"$point is within a radius of $r").expect(
    List("Point(1,1) is within a radius of 2")
  )

  // using flatMap
  def insideFilter(point: Point, radius: Int): List[Point] = if (isInside(point, radius)) List(point) else List.empty

  check(for {
    r       <- radiuses
    point   <- points
    inPoint <- insideFilter(point, r)
  } yield s"$inPoint is within a radius of $r").expect(
    List("Point(1,1) is within a radius of 2")
  )

  // Coffee Break: Filtering Techniques
  val riskyRadiuses = List(-10, 0, 2)

  check(for {
    r     <- riskyRadiuses
    point <- points.filter(p => isInside(p, r))
  } yield s"$point is within a radius of $r").expect(
    List(
      "Point(5,2) is within a radius of -10",
      "Point(1,1) is within a radius of -10",
      "Point(1,1) is within a radius of 2"
    )
  )

  // using filter
  check(for {
    r     <- riskyRadiuses.filter(r => r > 0)
    point <- points.filter(p => isInside(p, r))
  } yield s"$point is within a radius of $r").expect(
    List("Point(1,1) is within a radius of 2")
  )

  // using a guard expression
  check(for {
    r     <- riskyRadiuses
    if r > 0
    point <- points
    if isInside(point, r)
  } yield s"$point is within a radius of $r").expect(
    List("Point(1,1) is within a radius of 2")
  )

  // using flatMap
  def validateRadius(radius: Int): List[Int] = if (radius > 0) List(radius) else List.empty

  check(for {
    r           <- riskyRadiuses
    validRadius <- validateRadius(r)
    point       <- points
    inPoint     <- insideFilter(point, validRadius)
  } yield s"$inPoint is within a radius of $r").expect(
    List("Point(1,1) is within a radius of 2")
  )

  check(riskyRadiuses.flatMap(r =>
    validateRadius(r).flatMap(validRadius =>
      points.flatMap(point =>
        insideFilter(point, validRadius).map(inPoint => s"$inPoint is within a radius of $r")
      )
    )
  )).expect(
    List("Point(1,1) is within a radius of 2")
  )
}
