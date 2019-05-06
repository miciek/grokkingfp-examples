object TipCalculatorScala {
  def getTipPercentage(names: List[String]): Int = {
    if (names.size > 5) 20
    else if (names.size > 0) 10
    else 0
  }
}

object TipCalculationScala extends App {
  assert(TipCalculatorScala.getTipPercentage(List.empty) == 0)

  val smallGroup = List("Alice", "Bob", "Charlie")
  assert(TipCalculatorScala.getTipPercentage(smallGroup) == 10)

  val largeGroup = List("Alice", "Bob", "Charlie", "Daniel", "Emily", "Frank")
  assert(TipCalculatorScala.getTipPercentage(largeGroup) == 20)
}
