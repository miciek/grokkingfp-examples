object PassingFunctions extends App {
  {
    def inc(x: Int): Int = x + 1

    println(inc(2))

    def score(word: String): Int =
      word.replaceAll("a", "").length

    println(score("java"))
    assert(score("java") == 2)

    val words = List("rust", "java")

    println(words.sortBy(score))
    assert(words.sortBy(score) == List("java", "rust"))
  }

  // PRACTICING FUNCTION PASSING
  {
    def len(s: String): Int = s.length

    val byLength = List("scala", "rust", "ada").sortBy(len)
    println(byLength)
    assert(byLength == List("ada", "rust", "scala"))

    def numberOfS(s: String): Int =
      s.length - s.replaceAll("s", "").length

    val byNumberOfS = List("rust", "ada").sortBy(numberOfS)
    println(byNumberOfS)
    assert(byNumberOfS == List("ada", "rust"))

    def negative(i: Int): Int = -i

    val ascending = List(5, 1, 2, 4, 3).sortBy(negative)
    println(ascending)
    assert(ascending == List(5, 4, 3, 2, 1))

    def negativeNumberOfS(s: String): Int = -numberOfS(s)

    val byNegativeNumberOfS = List("ada", "rust").sortBy(negativeNumberOfS)
    println(byNegativeNumberOfS)
    assert(byNegativeNumberOfS == List("rust", "ada"))
  }
}
