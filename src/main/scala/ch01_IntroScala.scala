def increment(x: Int): Int = {
  x + 1
}

def getFirstCharacter(s: String): Char = {
  s.charAt(0)
}

def wordScore(word: String): Int = {
  word.length()
}

object ch01_IntroScala extends App {
  assert(increment(6) == 7)
  assert(getFirstCharacter("Ola") == 'O')
  assert(wordScore("Scala") == 5)
}
