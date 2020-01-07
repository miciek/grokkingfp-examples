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

    // RETURNING FUNCTIONS #3: PROBLEM
    {
      def highScoringWords(words: List[String], wordScore: String => Int): Int => List[String] = { higherThan =>
        words.filter(word => wordScore(word) > higherThan)
      }

      val words2 = List("football", "f1", "hockey", "basketball")

      val wordsWithScoreHigherThan: Int => List[String] = highScoringWords(words, w => score(w) + bonus(w) - penalty(w))

      val words2WithScoreHigherThan: Int => List[String] =
        highScoringWords(words2, w => score(w) + bonus(w) - penalty(w))

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
