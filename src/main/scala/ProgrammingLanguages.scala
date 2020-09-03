object ProgrammingLanguages extends App {
  case class ProgrammingLanguage(name: String, year: Int)

  val java  = ProgrammingLanguage("Java", 1995)
  val scala = ProgrammingLanguage("Scala", 2004)

  println(java.name)

  println(scala.name.length)

  println((scala.year + java.year) / 2)

  val languages = List(java, scala)

  println(languages)

  val names = languages.map(lang => lang.name)
  println(names)

  val young = languages.filter(lang => lang.year > 2000)
  println(young)
}
