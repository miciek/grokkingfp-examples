import cats.effect.IO
import cats.effect.unsafe.IORuntime
import cats.implicits._

/** This file is not part of the book. It's just a helper used to
  * print results and assert on their values in book examples.
  */
object check {
  def apply[A](result: A): Assert[A] = {
    println(result)
    new Assert(result)
  }

  def executedIO[A](io: IO[A])(implicit runtime: IORuntime = IORuntime.global): Assert[A] = {
    timed(io.unsafeRunSync())
  }

  def withoutPrinting[A](result: A): Assert[A] = {
    new Assert(result)
  }

  def potentiallyFailing[A](code: => A)(implicit runtime: IORuntime = IORuntime.global): Assert[A] = {
    val result = retry(IO(code), 100).unsafeRunSync()
    println(result)
    new Assert(result)
  }

  def timed[A](code: => A): Assert[A] = {
    val start  = System.currentTimeMillis()
    val result = code
    val end    = System.currentTimeMillis()
    println(s"$result (took ${end - start}ms)")
    new Assert(result)
  }

  class Assert[A](result: A) {
    def expect(expected: A): A = {
      assert(result == expected, s"$result != $expected")
      result
    }

    def expectThat(checkResult: A => Boolean): A = {
      assert(checkResult(result))
      result
    }
  }

  private def retry[A](action: IO[A], maxRetries: Int): IO[A] = {
    List.range(0, maxRetries).foldLeft(action)((program, _) => program.orElse(action))
  }
}

implicit class BookOutputChecker[A](value: => A) {

  /** Checks that the value on the left is exactly the same as the value on the right.
    * (Needed to verify all the code snippets in the book.)
    */
  def ===(expected: A): A = {
    check(value).expect(expected)
  }

  /** Checks that the value on the left satisfies the condition on the right.
    * (Needed to verify all the code snippets in the book.)
    */
  def ===(checkResult: A => Boolean): A = {
    check(value).expectThat(checkResult)
  }
}
