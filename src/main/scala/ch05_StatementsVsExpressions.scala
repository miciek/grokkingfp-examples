/** @see [[ch05_StatementsVsExpressionsJava]]
  */
object ch05_StatementsVsExpressions extends App {
  // in FP, we use expressions:
  val xs     = List(1, 2, 3, 4, 5)
  val result = for {
    x <- xs
  } yield x * x
  check(result).expect(List(1, 4, 9, 16, 25))

  // the above is equivalent to:
  val numbers = List(1, 2, 3, 4, 5)
  check(numbers.map(_ * 2)).expect(List(2, 4, 6, 8, 10))
}
