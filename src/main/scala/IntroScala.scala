object IntroScala extends App {
  def increment(x: Int): Int = {
    x + 1
  }

  def getFirstCharacter(s: String): Char = {
    s.charAt(0)
  }

  def score(word: String): Int = {
    word.length()
  }

  assert(increment(6) == 7)
  println(increment(6))

  assert(getFirstCharacter("Ola") == 'O')
  println(getFirstCharacter("Ola"))

  assert(score("Scala") == 5)
  println(score("Scala"))
}
