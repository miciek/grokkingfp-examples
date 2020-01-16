object WordScoringScala extends App {

  def score(word: String): Int = word.replaceAll("a", "").length

  val words = List("ada", "haskell", "scala", "java", "rust")

  {
    val wordRanking = words.sortBy(score).reverse
    println(wordRanking)
    assert(wordRanking == List("haskell", "rust", "scala", "java", "ada"))
  }

  {
    def rankedWords(wordScore: String => Int, words: List[String]): List[String] = {
      def negativeScore(word: String): Int = -wordScore(word)

      words.sortBy(negativeScore)
    }

    val wordRanking = rankedWords(score, words)
    println(wordRanking)
    assert(wordRanking == List("haskell", "rust", "scala", "java", "ada"))
  }

  {
    def rankedWords(wordScore: String => Int, words: List[String]): List[String] = {
      words.sortBy(wordScore).reverse
    }

    {
      val wordRanking = rankedWords(score, words)
      println(wordRanking)
      assert(wordRanking == List("haskell", "rust", "scala", "java", "ada"))
    }

    def scoreWithBonus(word: String): Int = {
      val base = score(word)
      if (word.contains("c")) base + 5 else base
    }

    {
      val wordRanking = rankedWords(scoreWithBonus, words)
      println(wordRanking)
      assert(wordRanking == List("scala", "haskell", "rust", "java", "ada"))
    }

    def bonus(word: String): Int = if (word.contains("c")) 5 else 0

    {
      val wordRanking = rankedWords(w => score(w) + bonus(w), words)
      println(wordRanking)
      assert(wordRanking == List("scala", "haskell", "rust", "java", "ada"))
    }

    def penalty(word: String): Int = if (word.contains("s")) 7 else 0

    {
      val wordRanking = rankedWords(w => score(w) + bonus(w) - penalty(w), words)
      println(wordRanking)
      assert(wordRanking == List("java", "scala", "ada", "haskell", "rust"))
    }

    // MAP
    def wordScores(wordScore: String => Int, words: List[String]): List[Int] = {
      words.map(wordScore)
    }

    {
      val scores = wordScores(w => score(w) + bonus(w) - penalty(w), words)
      println(scores)
      assert(scores == List(1, -1, 1, 2, -3))
    }

    // FILTER
    {
      def highScoringWords(wordScore: String => Int, words: List[String]): List[String] = {
        words.filter(word => wordScore(word) > 1)
      }

      val result = highScoringWords(w => score(w) + bonus(w) - penalty(w), words)
      println(result)
      assert(result == List("java"))
    }

    // RETURNING FUNCTIONS #0: problem
    {
      def highScoringWords(wordScore: String => Int, words: List[String]): List[String] = {
        words.filter(word => wordScore(word) > 1)
      }

      def highScoringWords0(wordScore: String => Int, words: List[String]): List[String] = {
        words.filter(word => wordScore(word) > 0)
      }

      def highScoringWords5(wordScore: String => Int, words: List[String]): List[String] = {
        words.filter(word => wordScore(word) > 5)
      }

      val result = highScoringWords(w => score(w) + bonus(w) - penalty(w), words)
      println(result)
      assert(result == List("java"))
      val result2 = highScoringWords0(w => score(w) + bonus(w) - penalty(w), words)
      println(result2)
      assert(result2 == List("ada", "scala", "java"))
      val result3 = highScoringWords5(w => score(w) + bonus(w) - penalty(w), words)
      println(result3)
      assert(result3 == List.empty)
    }

    // RETURNING FUNCTIONS #1: adding a new parameter
    {
      def highScoringWords(wordScore: String => Int, words: List[String], higherThan: Int): List[String] = {
        words.filter(word => wordScore(word) > higherThan)
      }

      // PROBLEM still there:
      val result = highScoringWords(w => score(w) + bonus(w) - penalty(w), words, 1)
      println(result)
      assert(result == List("java"))
      val result2 = highScoringWords(w => score(w) + bonus(w) - penalty(w), words, 0)
      println(result2)
      assert(result2 == List("ada", "scala", "java"))
      val result3 = highScoringWords(w => score(w) + bonus(w) - penalty(w), words, 5)
      println(result3)
      assert(result3 == List.empty)
    }

    // RETURNING FUNCTIONS #2: function returns a function
    {
      def highScoringWords(wordScore: String => Int, words: List[String]): Int => List[String] = { higherThan =>
        words.filter(word => wordScore(word) > higherThan)
      }

      val wordsWithScoreHigherThan: Int => List[String] = highScoringWords(w => score(w) + bonus(w) - penalty(w), words)

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

    // RETURNING FUNCTIONS #3: PROBLEM
    {
      def highScoringWords(wordScore: String => Int, words: List[String]): Int => List[String] = { higherThan =>
        words.filter(word => wordScore(word) > higherThan)
      }

      val words2 = List("football", "f1", "hockey", "basketball")

      val wordsWithScoreHigherThan: Int => List[String] = highScoringWords(w => score(w) + bonus(w) - penalty(w), words)

      val words2WithScoreHigherThan: Int => List[String] =
        highScoringWords(w => score(w) + bonus(w) - penalty(w), words2)

      val result = wordsWithScoreHigherThan(1)
      println(result)
      assert(result == List("java"))

      val result2 = wordsWithScoreHigherThan(0)
      println(result2)
      assert(result2 == List("ada", "scala", "java"))

      val result3 = wordsWithScoreHigherThan(5)
      println(result3)
      assert(result3 == List.empty)

      val result4 = words2WithScoreHigherThan(1)
      println(result4)
      assert(result4 == List("football", "f1", "hockey"))

      val result5 = words2WithScoreHigherThan(0)
      println(result5)
      assert(result5 == List("football", "f1", "hockey", "basketball"))

      val result6 = words2WithScoreHigherThan(5)
      println(result6)
      assert(result6 == List("football", "hockey"))
    }

    // RETURNING FUNCTIONS #4: returning functions from functions that return functions
    {
      def highScoringWords(wordScore: String => Int): Int => List[String] => List[String] = { higherThan => words =>
        words.filter(word => wordScore(word) > higherThan)
      }

      val words2 = List("football", "f1", "hockey", "basketball")

      val wordsWithScoreHigherThan: Int => List[String] => List[String] = highScoringWords(
        w => score(w) + bonus(w) - penalty(w)
      ) // just one function!

      val result = wordsWithScoreHigherThan(1)(words) // more readable
      println(result)
      assert(result == List("java"))

      val result2 = wordsWithScoreHigherThan(0)(words)
      println(result2)
      assert(result2 == List("ada", "scala", "java"))

      val result3 = wordsWithScoreHigherThan(5)(words)
      println(result3)
      assert(result3 == List.empty)

      val result4 = wordsWithScoreHigherThan(1)(words2)
      println(result4)
      assert(result4 == List("football", "f1", "hockey"))

      val result5 = wordsWithScoreHigherThan(0)(words2)
      println(result5)
      assert(result5 == List("football", "f1", "hockey", "basketball"))

      val result6 = wordsWithScoreHigherThan(5)(words2)
      println(result6)
      assert(result6 == List("football", "hockey"))
    }

    // RETURNING FUNCTIONS #5: currying
    {
      def highScoringWords(wordScore: String => Int)(higherThan: Int)(words: List[String]): List[String] = {
        words.filter(word => wordScore(word) > higherThan)
      }

      val words2 = List("football", "f1", "hockey", "basketball")

      val wordsWithScoreHigherThan: Int => List[String] => List[String] = highScoringWords(
        w => score(w) + bonus(w) - penalty(w)
      ) // just one function!

      val result = wordsWithScoreHigherThan(1)(words) // more readable
      println(result)
      assert(result == List("java"))

      val result2 = wordsWithScoreHigherThan(0)(words)
      println(result2)
      assert(result2 == List("ada", "scala", "java"))

      val result3 = wordsWithScoreHigherThan(5)(words)
      println(result3)
      assert(result3 == List.empty)

      val result4 = wordsWithScoreHigherThan(1)(words2)
      println(result4)
      assert(result4 == List("football", "f1", "hockey"))

      val result5 = wordsWithScoreHigherThan(0)(words2)
      println(result5)
      assert(result5 == List("football", "f1", "hockey", "basketball"))

      val result6 = wordsWithScoreHigherThan(5)(words2)
      println(result6)
      assert(result6 == List("football", "hockey"))
    }

    def cumulativeScore(wordScore: String => Int, words: List[String]): Int = {
      words.foldLeft(0)((total, word) => {
        total + wordScore(word)
      })
    }

    {
      val result = cumulativeScore(w => score(w) + bonus(w) - penalty(w), words)
      println(result)
      assert(result == 0)
    }

    {
      val result = cumulativeScore(w => score(w) + bonus(w) - penalty(w), List("rust", "java"))
      println(result)
      assert(result == -1)
    }
  }
}
