lazy val root = (project in file("."))
  .settings(
    name := "grokkingfp-examples",
    organization := "Michał Płachta (Manning)",
    version := "1.0",
    scalaVersion := "2.13.1",
    scalacOptions ++= List(
        "-unchecked",
        "-Xfatal-warnings",
        "-language:higherKinds",
        "-Xlint"
      ),
    fork in run := true,
    javaOptions in run += "-ea",
    addCommandAlias(
      "runAll",
      ";runMain Intro" +
      ";runMain TestingPureFunctions" +
      ";runMain PureFunctions" +
      ";runMain ShoppingCartDiscounts" +
      ";runMain DeletingMutability" +
      ";runMain TipCalculation" +
      ";runMain ShoppingCartDiscountsScala" +
      ";runMain TipCalculationScala" +
      ";runMain ItineraryCopying" +
      ";runMain LapTimes" +
      ";runMain Restaurant" +
      ";runMain WordScoring" +
      ";runMain WordScoringScala" +
      ";runMain PassingFunctions" +
      ";runMain ReturningFunctions" +
      ";runMain BookAdaptations" +
      ";runMain BookFriendRecommendations" +
      ";runMain Points2d3d" +
      ";runMain PointsInsideCircles" +
      ";runMain SequencedNestedFlatMaps" +
      ";runMain Events" +
      ";runMain TvShows" +
      ";runMain RandomForComprehensions"
    )
  )
