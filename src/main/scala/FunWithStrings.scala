import java.util
import java.util.function.UnaryOperator

/**
  * Given the list of all interesting books,
  * return a feed of movie recommendations.
  *
  * SKILLS:
  * a) knowing how FP differs
  */
object FunWithStrings extends App {
  val s = "F u n c t i o n"

  val resultBuilder = new StringBuilder()
  for(c <- s) {
    if(c != ' ') {
      resultBuilder.append(c)
    }
  }
  val imperativeResult = resultBuilder.toString()
  assert(imperativeResult == "Function")

  val functionalResult = s.replaceAll(" ", "")
  assert(functionalResult == "Function")
}
