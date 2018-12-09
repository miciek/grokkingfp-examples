/**
  * Given lists of coordinates,
  * create all possible 2d & 3d points.
  *
  * SKILLS:
  * b) writing algorithms using flatMap instead of for loops
  * c) writing algorithms in a readable way using for comprehensions
  */
object Points2d3d extends App {
  case class Point(x: Int, y: Int)

  val p = List(1).flatMap { x =>
    List(-2, 7).map { y =>
      Point(x, y)
    }
  }
  println(p)
  assert(p == List(Point(1, -2), Point(1, 7)))

  val xs = List(1)
  val ys = List(-2, 7)
  val zs = List(3, 4)

  case class Point3d(x: Int, y: Int, z: Int)

  val p3d = for {
    x <- xs
    y <- ys
    z <- zs
  } yield Point3d(x, y, z)
  println(p3d)

  val p3dfm = xs.flatMap { x =>
    ys.flatMap { y =>
      zs.map { z =>
        Point3d(x, y, z)
      }
    }
  }
  assert(p3dfm == p3d)

  val r = for {
    a <- List(1, 2)
    b <- List(10, 100)
    c <- List(0.5, 0.7)
    d <- List(3)
  } yield (a * b * c + d).toString + "km"
  println(r)
}
