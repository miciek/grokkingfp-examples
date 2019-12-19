object ReturningFunctions extends App {
  {
    def largerThan(n: Int): Int => Boolean = i => i > n

    val large = List(5, 1, 2, 4, 0).filter(largerThan(4))
    println(large)
    assert(large == List(5))
    assert(List(5, 1, 2, 4, 0).filter(largerThan(1)) == List(5, 2, 4))

    def divisibleBy(n: Int): Int => Boolean = i => i % n == 0

    val odds = List(5, 1, 2, 4, 15).filter(divisibleBy(5))
    println(odds)
    assert(odds == List(5, 15))
    assert(List(5, 1, 2, 4, 15).filter(divisibleBy(2)) == List(2, 4))

    def shorterThan(n: Int): String => Boolean = s => s.length < n

    val longWords = List("scala", "ada").filter(shorterThan(4))
    println(longWords)
    assert(longWords == List("ada"))
    assert(List("scala", "ada").filter(shorterThan(7)) == List("scala", "ada"))

    def numberOfS(s: String): Int =
      s.length - s.replaceAll("s", "").length

    def containsS(moreThan: Int): String => Boolean =
      s => numberOfS(s) > moreThan

    val withLotsS = List("rust", "ada").filter(containsS(2))
    println(withLotsS)
    assert(withLotsS == List.empty)
    assert(List("rust", "ada").filter(containsS(0)) == List("rust"))
  }

  // CURRYING
  {
    def largerThan(n: Int)(i: Int): Boolean = i > n

    val large = List(5, 1, 2, 4, 0).filter(largerThan(4))
    println(large)
    assert(large == List(5))
    assert(List(5, 1, 2, 4, 0).filter(largerThan(1)) == List(5, 2, 4))

    def divisibleBy(n: Int)(i: Int): Boolean = i % n == 0

    val odds = List(5, 1, 2, 4, 15).filter(divisibleBy(5))
    println(odds)
    assert(odds == List(5, 15))
    assert(List(5, 1, 2, 4, 15).filter(divisibleBy(2)) == List(2, 4))

    def shorterThan(n: Int)(s: String): Boolean = s.length < n

    val longWords = List("scala", "ada").filter(shorterThan(4))
    println(longWords)
    assert(longWords == List("ada"))
    assert(List("scala", "ada").filter(shorterThan(7)) == List("scala", "ada"))

    def numberOfS(s: String): Int =
      s.length - s.replaceAll("s", "").length

    def containsS(moreThan: Int)(s: String): Boolean =
      numberOfS(s) > moreThan

    val withLotsS = List("rust", "ada").filter(containsS(2))
    println(withLotsS)
    assert(withLotsS == List.empty)
    assert(List("rust", "ada").filter(containsS(0)) == List("rust"))
  }
}
