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

    // MAP
    def wordScores(words: List[String], wordScore: String => Int): List[Int] = {
      words.map(wordScore)
    }

    {
      val scores = wordScores(words, w => score(w) + bonus(w) - penalty(w))
      println(scores)
      assert(scores == List(1, -1, 1, 2, -3))
    }

    // FILTER
    {
      def highScoringWords(words: List[String], wordScore: String => Int): List[String] = {
        words.filter(word => wordScore(word) > 1)
      }

      val result = highScoringWords(words, w => score(w) + bonus(w) - penalty(w))
      println(result)
      assert(result == List("java"))
    }

    // RETURNING FUNCTIONS #0: problem
    {
      def highScoringWords(words: List[String], wordScore: String => Int): List[String] = {
        words.filter(word => wordScore(word) > 1)
      }

      def highScoringWords0(words: List[String], wordScore: String => Int): List[String] = {
        words.filter(word => wordScore(word) > 0)
      }

      def highScoringWords5(words: List[String], wordScore: String => Int): List[String] = {
        words.filter(word => wordScore(word) > 5)
      }

      val result = highScoringWords(words, w => score(w) + bonus(w) - penalty(w))
      println(result)
      assert(result == List("java"))
      val result2 = highScoringWords0(words, w => score(w) + bonus(w) - penalty(w))
      println(result2)
      assert(result2 == List("ada", "scala", "java"))
      val result3 = highScoringWords5(words, w => score(w) + bonus(w) - penalty(w))
      println(result3)
      assert(result3 == List.empty)
    }

    // RETURNING FUNCTIONS #1: adding a new parameter
    {
      def highScoringWords(words: List[String], wordScore: String => Int, higherThan: Int): List[String] = {
        words.filter(word => wordScore(word) > higherThan)
      }

      // PROBLEM still there:
      val result = highScoringWords(words, w => score(w) + bonus(w) - penalty(w), 1)
      println(result)
      assert(result == List("java"))
      val result2 = highScoringWords(words, w => score(w) + bonus(w) - penalty(w), 0)
      println(result2)
      assert(result2 == List("ada", "scala", "java"))
      val result3 = highScoringWords(words, w => score(w) + bonus(w) - penalty(w), 5)
      println(result3)
      assert(result3 == List.empty)
    }

    // RETURNING FUNCTIONS #2: function returns a function
    {
      def highScoringWords(words: List[String], wordScore: String => Int): Int => List[String] = { higherThan =>
        words.filter(word => wordScore(word) > higherThan)
      }

      val wordsWithScoreHigherThan: Int => List[String] = highScoringWords(words, w => score(w) + bonus(w) - penalty(w))

      val result = wordsWithScoreHigherThan(1)
      println(result)
      assert(result == List("java"))

      val result2 = wordsWithScoreHigherThan(0)
      println(result2)
      assert(result2 == List("ada", "scala", "java"))

      val result3 = wordsWithScoreHigherThan(5)
      println(result3)
      assert(result3 == List.empty)
    }

    // FIXME: RETURNING FUNCTIONS #3: partially applying 1 of 2 arguments
    {
      def scoreHigherThan(wordScore: String => Int, higherThan: Int): String => Boolean = { word =>
        wordScore(word) > higherThan
      }

      def highScoringWords(words: List[String], highScoreWord: String => Boolean): List[String] = {
        words.filter(highScoreWord)
      }

      def totalScoreHigherThan(higherThan: Int): String => Boolean = {
        scoreHigherThan(w => score(w) + bonus(w) - penalty(w), higherThan)
      }

      val result = highScoringWords(words, totalScoreHigherThan(1))
      println(result)
      assert(result == List("java"))

      val result2 = highScoringWords(words, totalScoreHigherThan(0))
      println(result2)
      assert(result2 == List("ada", "scala", "java"))

      val result3 = highScoringWords(words, totalScoreHigherThan(5))
      println(result3)
      assert(result3 == List.empty)
    }

    def cumulativeScore(words: List[String], wordScore: String => Int): Int = {
      words.foldLeft(0)((total, word) => {
        total + wordScore(word)
      })
    }

    {
      val result = cumulativeScore(words, w => score(w) + bonus(w) - penalty(w))
      println(result)
      assert(result == 0)
    }

    {
      val result = cumulativeScore(List("rust", "java"), w => score(w) + bonus(w) - penalty(w))
      println(result)
      assert(result == -1)
    }
  }
}
