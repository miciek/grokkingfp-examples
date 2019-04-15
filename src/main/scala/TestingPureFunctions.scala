object TestingPureFunctions extends App {
  def increment(x: Int): Int = {
    x + 1
  }

  assert(increment(6) == 7)
  assert(increment(0) == 1)
  assert(increment(-6) == -5)
  assert(increment(Integer.MAX_VALUE - 1) == Integer.MAX_VALUE)

  def add(a: Int, b: Int): Int = {
    a + b
  }

  assert(add(2, 5) == 7)
  assert(add(-2, 5) == 3)

  def score(word: String): Int = {
    word.replaceAll("a", "").length
  }

  assert(score("Scala") == 3)
  assert(score("function") == 8)
  assert(score("") == 0)

  def getTipPercentage(names: List[String]): Int = {
    if (names.size > 5) 20
    else if (names.size > 0) 10
    else 0
  }

  assert(getTipPercentage(List("Alice", "Bob")) == 10)
  assert(getTipPercentage(List("Alice", "Bob", "Charlie", "Danny", "Emily", "Frank")) == 20)
  assert(getTipPercentage(List.empty) == 0)

  def getFirstCharacter(s: String): Char = {
    if (s.length > 0) s.charAt(0)
    else ' '
  }

  assert(getFirstCharacter("Ola") == 'O')
  assert(getFirstCharacter("") == ' ')
  assert(getFirstCharacter(" Ha! ") == ' ')
}
