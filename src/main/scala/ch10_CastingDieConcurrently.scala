import cats.effect.{IO, Ref}
import cats.implicits._

object ch10_CastingDieConcurrently extends App {
  import ch08_CastingDieImpure.NoFailures.castTheDieImpure

  def castTheDie(): IO[Int] = IO.delay(castTheDieImpure())

  // Practicing fibers:
  // 1. cast two dies concurrently and print each result to the console (println)
  check.executedIO(for {
    fiber1 <- castTheDie().map(println).start
    fiber2 <- castTheDie().map(println).start
    _      <- fiber1.join
    _      <- fiber2.join
  } yield ())

  // 2. cast two dies concurrently and store each result in a mutable reference that holds a List
  check.executedIO(for {
    storedCasts <- Ref.of[IO, List[Int]](List.empty)
    singleCast  = castTheDie().flatMap(result => storedCasts.update(_.appended(result))).start
    fiber1      <- singleCast
    fiber2      <- singleCast
    _           <- fiber1.join
    _           <- fiber2.join
    casts       <- storedCasts.get
  } yield casts)

  // 3. cast three dies concurrently and store each result in a mutable reference that holds a List (use sequence for both starting and joining to make it concise)
  check.executedIO(for {
    storedCasts <- Ref.of[IO, List[Int]](List.empty)
    singleCast  = castTheDie().flatMap(result => storedCasts.update(_.appended(result))).start
    fibers      <- List.fill(3)(singleCast).sequence // introduce fill
    _           <- fibers.map(_.join).sequence
    casts       <- storedCasts.get
  } yield casts)

  // 4. cast one hundred dies concurrently and return the total number of sixes
  check.executedIO(for {
    storedCasts <- Ref.of[IO, Int](0)
    singleCast  = castTheDie().flatMap(result => if (result == 6) storedCasts.update(_ + 1) else IO.unit).start
    fibers      <- List.fill(100)(singleCast).sequence
    _           <- fibers.map(_.join).sequence
    casts       <- storedCasts.get
  } yield casts)
}
