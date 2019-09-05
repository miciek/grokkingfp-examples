object WordScoringScala extends App {
  def score(word: String): Int = word.replaceAll("a", "").length

  val words       = List("ada", "haskell", "scala", "java", "rust")
  val wordRanking = words.sortWith((w1, w2) => score(w1) > score(w2))
  println(wordRanking)
  assert(wordRanking == List("haskell", "rust", "scala", "java", "ada"))
}
