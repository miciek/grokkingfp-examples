object ch04_ProgrammingLanguages extends App {
  case class ProgrammingLanguage(name: String, year: Int)

  val javalang  = ProgrammingLanguage("Java", 1995)
  val scalalang = ProgrammingLanguage("Scala", 2004)

  assert(javalang.name == "Java")
  println(javalang.name)

  println(scalalang.name.length)
  assert(scalalang.name.length == 5)

  println((scalalang.year + javalang.year) / 2)
  assert((scalalang.year + javalang.year) / 2 == 1999)

  val languages = List(javalang, scalalang)

  println(languages)
  assert(languages == List(ProgrammingLanguage("Java", 1995), ProgrammingLanguage("Scala", 2004)))

  val names = languages.map(lang => lang.name)
  println(names)
  assert(names == List("Java", "Scala"))

  val young = languages.filter(lang => lang.year > 2000)
  println(young)
  assert(young == List(scalalang))

  assert(languages.map(_.name) == List("Java", "Scala"))

  assert(languages.filter(_.year > 2000) == List(scalalang))
}
