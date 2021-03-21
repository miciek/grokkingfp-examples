import cats.effect.{IO, Ref}

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

  // 3. return the sum of the first three casts
//  check(infiniteDieCasts.take(3).compile.toList.map(_.sum).unsafeRunSync()).expect { result =>
//    result >= 3 && result <= 18
//  }

  // 4. cast the die until there is a five and then cast it two more times, returning three last results back
//  check(infiniteDieCasts.filter(_ == 5).take(1).append(infiniteDieCasts.take(2)).compile.toList.unsafeRunSync())
//    .expect { result => result.size == 3 && result.head == 5 }

  // 5. make sure the die is cast one hundred times and values are discarded
//  check(infiniteDieCasts.take(100).compile.drain).expect(_.isInstanceOf[IO[Unit]])

  // 6. return first three casts unchanged and next three casts tripled (six in total)
//  check(infiniteDieCasts.take(3).append(infiniteDieCasts.take(3).map(_ * 3)).compile.toList.unsafeRunSync()).expect {
//    result => result.size == 6 && result.slice(0, 3).forall(_ <= 6) && result.slice(3, 6).forall(_ >= 3)
//  }

  // 7. cast the die until there are two sixes in a row
//  check(
//    infiniteDieCasts
//      .scan(0)((sixesInRow, current) => if (current == 6) sixesInRow + 1 else 0)
//      .filter(_ == 2)
//      .take(1)
//      .compile
//      .toList
//      .unsafeRunSync()
//  ).expect(List(2))
}
