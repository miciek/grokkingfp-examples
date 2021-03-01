import cats.effect.IO
import cats.implicits._
import fs2.{Pure, Stream}

import java.util.concurrent.TimeUnit
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration
import scala.jdk.CollectionConverters.MapHasAsScala

object ch09_CastingDieStream extends App {
  { // creating values
    val numbers    = Stream(1, 2, 3)
    val oddNumbers = numbers.filter(_ % 2 != 0)

    check(oddNumbers.toList).expect(List(1, 3))
    check(numbers.toList).expect(List(1, 2, 3))
    check(oddNumbers.map(_ + 17).take(1).toList).expect(List(18))
  }

  { // append & take
    val stream1 = Stream(1, 2, 3)
    val stream2 = Stream(4, 5, 6)

    val stream3 = stream1.append(stream2)
    check(stream3.toList).expect(List(1, 2, 3, 4, 5, 6))

    val stream4 = stream1.append(stream1)
    check(stream4.toList).expect(List(1, 2, 3, 1, 2, 3))

    val stream5 = stream4.take(4)
    check(stream5.toList).expect(List(1, 2, 3, 1))
  }

  { // infinite streams
    def numbers(): Stream[Pure, Int] = {
      Stream(1, 2, 3).append(numbers())
    }

    val infinite123s = numbers()
    check(infinite123s.take(8).toList).expect(List(1, 2, 3, 1, 2, 3, 1, 2))
  }

  { // infinite streams using .repeat
    val numbers = Stream(1, 2, 3).repeat

    check(numbers.take(8).toList).expect(List(1, 2, 3, 1, 2, 3, 1, 2))
  }

  { // streams of IO values
    import ch08_CastingDieImpure.NoFailures.castTheDieImpure

    def castTheDie(): IO[Int] = IO.delay(castTheDieImpure())

    val dieCast: Stream[IO, Int]         = Stream.eval(castTheDie())
    val oneDieCastProgram: IO[List[Int]] = dieCast.compile.toList

    check(oneDieCastProgram.unsafeRunSync()).expect(_.size == 1)

    val infiniteDieCasts: Stream[IO, Int]      = Stream.eval(castTheDie()).repeat
    val infiniteDieCastsProgram: IO[List[Int]] = infiniteDieCasts.compile.toList
    // println(infiniteDieCastsProgram.unsafeRunSync()) // will never finish

    val infiniteDieCastsProgramDrain: IO[Unit] = infiniteDieCasts.compile.drain
    // println(infiniteDieCastsProgramDrain.unsafeRunSync()) // will never finish

    val firstThreeCasts: IO[List[Int]] = infiniteDieCasts.take(3).compile.toList
    check(firstThreeCasts.unsafeRunSync()).expect(_.size == 3)

    val six: IO[List[Int]] = infiniteDieCasts.filter(_ == 6).take(1).compile.toList
    check(six.unsafeRunSync()).expect(List(6))

    // Practicing stream operations:

  }

}
