import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.implicits.*

object ch08_CardGame {
  def castTheDie(): Int = {
    ch08_CastingDieImpure.WithFailures.castTheDieImpure()
  }

  def drawAPointCard(): Int = {
    ch08_CastingDieImpure.drawAPointCard()
  }

  def main(args: Array[String]): Unit = {
    // Practicing failure recovery in IO values

    // Cast the die and if it fails to produce a result, return 0.
    IO.delay(castTheDie()).orElse(IO.pure(0))

    // Draw a card and, if it fails, cast the die.
    IO.delay(drawAPointCard()).orElse(IO.delay(castTheDie()))

    // Cast the die and if it fails—retry once. If it fails again, return 0.
    IO.delay(castTheDie())
      .orElse(IO.delay(castTheDie()))
      .orElse(IO.pure(0))

    // Cast the die and draw a card, using a fallback of 0 for each of them. Return the sum of both.
    for {
      die  <- IO.delay(castTheDie()).orElse(IO.pure(0))
      card <- IO.delay(drawAPointCard()).orElse(IO.pure(0))
    } yield die + card

    // Draw a card and cast the die twice. Return the sum of all three or 0 if any of them fails.
    (for {
      card <- IO.delay(drawAPointCard())
      die1 <- IO.delay(castTheDie())
      die2 <- IO.delay(castTheDie())
    } yield card + die1 + die2).orElse(IO.pure(0))
  }
}
