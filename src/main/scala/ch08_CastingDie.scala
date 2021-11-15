import cats.effect.IO
import cats.implicits._
import cats.effect.unsafe.implicits.global

object ch08_CastingDie {
  def castTheDieImpure(): Int = {
    ch08_CastingDieImpure.NoFailures.castTheDieImpure()
  }

  object WithFailures {
    def castTheDieImpure(): Int = {
      ch08_CastingDieImpure.WithFailures.castTheDieImpure()
    }
  }

  def main(args: Array[String]): Unit = {
    import ch08_CastingDieImpure.getIntUnsafely
    val existingInt: IO[Int]        = IO.pure(6)
    val intFromUnsafePlace: IO[Int] = IO.delay(getIntUnsafely())
    check(existingInt.unsafeRunSync()).expect(6)
    check.potentiallyFailing(intFromUnsafePlace.unsafeRunSync()).expectThat(r => r > 0 && r < 7)

    println(castTheDieImpure())
    println(castTheDieImpure())

    // Introducing IO
    {
      def castTheDie(): IO[Int] = IO.delay(castTheDieImpure())

      println(castTheDie())

      val dieCast: IO[Int] = castTheDie()
      println(dieCast)
      println(dieCast.unsafeRunSync())

      println(castTheDieImpure() + castTheDieImpure())
      // Can't add two IO values:
      // println(castTheDie() + castTheDie())
    }

    // Introducing unsafe side-effectful actions
    {
      def castTheDie(): IO[Int] = IO.delay(WithFailures.castTheDieImpure())

      println(castTheDie()) // no error thrown, because nothing is executed

      // we need to execute
      try {
        castTheDie().unsafeRunSync()
      } catch {
        case e: Throwable => println(s"Exception thrown: ${e.getMessage}")
      }
    }

    // Combining two IO results (+ Option recap)
    {
      val aOption: Option[Int] = Some(2)
      val bOption: Option[Int] = Some(4)
      // Can't add two Option values:
      // aOption + bOption

      // we need flatMap (for comprehension)
      val result: Option[Int] = for {
        a <- aOption
        b <- bOption
      } yield a + b

      println(result)

      // we can do the same with IO:
      def castTheDie(): IO[Int] = IO.delay(WithFailures.castTheDieImpure())
      // Can't add two IO values:
      // println(castTheDie() + castTheDie())

      // we can if we use flatMap (for comprehension)
      def castTheDieTwice(): IO[Int] = {
        for {
          firstCast  <- castTheDie()
          secondCast <- castTheDie()
        } yield firstCast + secondCast
      }

      println(castTheDieTwice()) // still just an IO
    }
  }
}
