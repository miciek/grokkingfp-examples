lazy val root = (project in file("."))
  .settings(
    name := "grokkingfp-examples",
    organization := "Michał Płachta (Manning)",
    version := "1.0",
    scalaVersion := "2.13.3",
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
      ";runMain IntroScala" +
      ";runMain TestingPureFunctions" +
      ";runMain PureFunctions" +
      ";runMain JavaFunctionIntro" +
      ";runMain ShoppingCartDiscounts" +
      ";runMain DeletingMutability" +
      ";runMain TipCalculation" +
      ";runMain ShoppingCartDiscountsScala" +
      ";runMain TipCalculationScala" +
      ";runMain ItineraryCopying" +
      ";runMain Itinerary" +
      ";runMain ItineraryScala" +
      ";runMain LapTimes" +
      ";runMain ListVsString" +
      ";runMain ListAndStringIntuitions" +
      ";runMain SlicingAndAppending" +
      ";runMain AbbreviateNames" +
      ";runMain WordScoring" +
      ";runMain WordScoringScala" +
      ";runMain PassingFunctions" +
      ";runMain ReturningFunctions" +
      ";runMain ProgrammingLanguages" +
      ";runMain BookAdaptations" +
      ";runMain BookFriendRecommendationsJava" +
      ";runMain BookFriendRecommendations" +
      ";runMain Points2d3d" +
      ";runMain PointsInsideCircles" +
      ";runMain SequencedNestedFlatMaps" +
      ";runMain Events" +
      ";runMain RandomForComprehensions" +
      ";runMain TvShows" +
      ";runMain TvShowsJava" +
      ";runMain MusicArtistsSearch" +
      ";runMain Playlist"
    )
  )
