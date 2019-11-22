object WordScoringScala extends App {

  def score(word: String): Int = word.replaceAll("a", "").length

  val words = List("ada", "haskell", "scala", "java", "rust")

  {
    val wordRanking = words.sortBy(score).reverse
    println(wordRanking)
    assert(wordRanking == List("haskell", "rust", "scala", "java", "ada"))
  }

  {
    def rankedWords(words: List[String], wordScore: String => Int): List[String] = {
      def negativeScore(word: String): Int = -wordScore(word)
      words.sortBy(negativeScore)
    }

    val wordRanking = rankedWords(words, score)
    println(wordRanking)
    assert(wordRanking == List("haskell", "rust", "scala", "java", "ada"))
  }

  {
    def rankedWords(words: List[String], wordScore: String => Int): List[String] = {
      words.sortBy(wordScore).reverse
    }

    {
      val wordRanking = rankedWords(words, score)
      println(wordRanking)
      assert(wordRanking == List("haskell", "rust", "scala", "java", "ada"))
    }

    def scoreWithBonus(word: String): Int = {
      val base = score(word)
      if (word.contains("c")) base + 5 else base
    }

    {
      val wordRanking = rankedWords(words, scoreWithBonus)
      println(wordRanking)
      assert(wordRanking == List("scala", "haskell", "rust", "java", "ada"))
    }

    def bonus(word: String): Int = if (word.contains("c")) 5 else 0

    {
      val wordRanking = rankedWords(words, w => score(w) + bonus(w))
      println(wordRanking)
      assert(wordRanking == List("scala", "haskell", "rust", "java", "ada"))
    }

    def penalty(word: String): Int = if (word.contains("s")) 7 else 0

    {
      val wordRanking = rankedWords(words, w => score(w) + bonus(w) - penalty(w))
      println(wordRanking)
      assert(wordRanking == List("java", "scala", "ada", "haskell", "rust"))
    }
  }
}
