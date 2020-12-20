object ch01_IntroScala extends App {
  def increment(x: Int): Int = {
    x + 1
  }

  def getFirstCharacter(s: String): Char = {
    s.charAt(0)
  }

  def wordScore(word: String): Int = {
    word.length()
  }

  assert(increment(6) == 7)
  println(increment(6))

  assert(getFirstCharacter("Ola") == 'O')
  println(getFirstCharacter("Ola"))

  assert(wordScore("Scala") == 5)
  println(wordScore("Scala"))
}
