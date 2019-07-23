object ItineraryScala extends App {
  def replan(plan: List[String], newCity: String, beforeCity: String): List[String] = {
    val newCityIndex                = plan.indexOf(beforeCity)
    val (citiesBefore, citiesAfter) = plan.splitAt(newCityIndex)
    citiesBefore.appended(newCity).appendedAll(citiesAfter)
  }

  val planA = List("Paris", "Berlin", "Kraków")
  println("Plan A: " + planA)

  val planB = replan(planA, "Vienna", "Kraków")
  assert(planB == List("Paris", "Berlin", "Vienna", "Kraków"))
  println("Plan B: " + planB)

  assert(planA == List("Paris", "Berlin", "Kraków"))
  println("Plan A: " + planA)
}
