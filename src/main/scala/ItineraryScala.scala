object ItineraryScala extends App {
  def replan(plan: List[String], newCity: String, beforeCity: String): List[String] = {
    val newCityIndex = plan.indexOf(beforeCity)
    plan.patch(newCityIndex, List(newCity), 0)
  }

  val planA = List("Paris", "Berlin", "Kraków")
  println("Plan A: " + planA)

  val planB = replan(planA, "Vienna", "Kraków")
  assert(planB == List("Paris", "Berlin", "Vienna", "Kraków"))
  println("Plan B: " + planB)

  assert(planA == List("Paris", "Berlin", "Kraków"))
  println("Plan A: " + planA)
}
