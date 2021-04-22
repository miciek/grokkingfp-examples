import ch11_TravelGuide._
import org.scalatest.funsuite.AnyFunSuite

/**
  * @see [[ch11_TravelGuide]] to verify requirements
  */
class ch12_TravelGuideTest extends AnyFunSuite {
  test("guide attractiveness including a description, 0 artists, and 2 popular movies should be 65") {
    val guide = TravelGuide(
      Place(
        "Yellowstone National Park",
        Some("first national park in the world, located in Wyoming, Montana and Idaho, United States"),
        Location(LocationId("Q1214"), "Wyoming", 586107)
      ),
      List(Movie("The Hateful Eight", 155760117), Movie("Heaven's Gate", 3484331))
    )

    // 30 (description) + 0 (0 artists) + 20 (2 movies) + 15 (159 million box-office)
    assert(guideScore(guide) == 65)
  }
}
