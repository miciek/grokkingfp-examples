object ch04_PassingFunctions extends App {
  {
    def inc(x: Int): Int = x + 1

    println(inc(2))

    def score(word: String): Int = word.replaceAll("a", "").length

    println(score("java"))
    assert(score("java") == 2)

    val words = List("rust", "java")

    println(words.sortBy(score))
    assert(words.sortBy(score) == List("java", "rust"))
  }

  // sortBy
  {
    def score(word: String): Int = word.replaceAll("a", "").length
    val words                    = List("rust", "java")
    val sortedWords              = words.sortBy(score)
    assert(sortedWords == List("java", "rust"))
    assert(List("rust", "java").sortBy(score) == sortedWords)
  }

  // Practicing function passing
  {
    def len(s: String): Int = s.length

    val byLength = List("scala", "rust", "ada").sortBy(len)
    println(byLength)
    assert(byLength == List("ada", "rust", "scala"))

    def numberOfS(s: String): Int = s.length - s.replaceAll("s", "").length

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

  // Practicing map
  {
    def len(s: String): Int = s.length

    val lengths = List("scala", "rust", "ada").map(len)
    println(lengths)
    assert(lengths == List(5, 4, 3))

    def numberOfS(s: String): Int = s.length - s.replaceAll("s", "").length

    val numberOfSs = List("rust", "ada").map(numberOfS)
    println(numberOfSs)
    assert(numberOfSs == List(1, 0))

    def negative(i: Int): Int = -i

    val negatives = List(5, 1, 2, 4, 0).map(negative)
    println(negatives)
    assert(negatives == List(-5, -1, -2, -4, 0))

    def double(i: Int): Int = 2 * i

    val doubles = List(5, 1, 2, 4, 0).map(double)
    println(doubles)
    assert(doubles == List(10, 2, 4, 8, 0))
  }

  // Practicing filter
  {
    def len(s: String): Int = s.length

    val longWords = List("scala", "rust", "ada").filter(word => len(word) < 5)
    println(longWords)
    assert(longWords == List("rust", "ada"))

    def numberOfS(s: String): Int = s.length - s.replaceAll("s", "").length

    val withLotsS = List("rust", "ada").filter(word => numberOfS(word) > 2)
    println(withLotsS)
    assert(withLotsS == List.empty)

    def odd(i: Int): Boolean = i % 2 == 1

    val odds = List(5, 1, 2, 4, 0).filter(odd)
    println(odds)
    assert(odds == List(5, 1))

    def largerThan4(i: Int): Boolean = i > 4

    val large = List(5, 1, 2, 4, 0).filter(largerThan4)
    println(large)
    assert(large == List(5))
  }

  // Practicing foldLeft
  {
    val sum = List(5, 1, 2, 4, 100).foldLeft(0)((sum, i) => sum + i)
    println(sum)
    assert(sum == 112)

    def len(s: String): Int = s.length

    val totalLength = List("scala", "rust", "ada").foldLeft(0)((total, s) => total + len(s))
    println(totalLength)
    assert(totalLength == 12)

    def numberOfS(s: String): Int = s.length - s.replaceAll("s", "").length

    val totalS = List("scala", "haskell", "rust", "ada").foldLeft(0)((total, str) => total + numberOfS(str))
    println(totalS)
    assert(totalS == 3)

    val max = List(5, 1, 2, 4, 15).foldLeft(Int.MinValue)((max, i) => if (i > max) i else max)
    println(max)
    assert(max == 15)

    val max2 = List(5, 1, 2, 4, 15).foldLeft(Int.MinValue)((max, i) => Math.max(max, i))
    println(max2)
    assert(max2 == 15)
  }
}
