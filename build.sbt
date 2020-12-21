lazy val root = (project in file("."))
  .settings(
    name := "grokkingfp-examples",
    organization := "Michał Płachta (Manning)",
    version := "1.0",
    scalaVersion := "2.13.4",
    scalacOptions ++= List(
        "-unchecked",
        "-Xfatal-warnings",
        "-language:higherKinds",
        "-Xlint"
      ),
    libraryDependencies += "org.typelevel" %% "cats-effect" % "2.3.0",
    fork in run := true,
    javaOptions in run += "-ea",
    addCommandAlias(
      "runAll",
      ";runMain ch01_Intro" +
      ";runMain ch01_IntroScala" +
      ";runMain ch02_ShoppingCartDiscounts" +
      ";runMain ch02_TipCalculation" +
      ";runMain ch02_PureFunctions" +
      ";runMain ch02_ShoppingCartDiscountsScala" +
      ";runMain ch02_TipCalculationScala" +
      ";runMain ch02_TestingPureFunctions" +
      ";runMain ch03_Itinerary" +
      ";runMain ch03_ItineraryCopying" +
      ";runMain ch03_LapTimes" +
      ";runMain ch03_ListVsString" +
      ";runMain ch03_ListVsStringScala" +
      ";runMain ch03_AbbreviateNames" +
      ";runMain ch03_ItineraryScala" +
      ";runMain ch03_SlicingAndAppending" +
      ";runMain ch04_WordScoring" +
      ";runMain ch04_WordScoringScala" +
      ";runMain ch04_JavaFunctionIntro" +
      ";runMain ch04_PassingFunctions" +
      ";runMain ch04_ReturningFunctions" +
      ";runMain ch04_ProgrammingLanguages" +
      ";runMain ch05_BookAdaptations" +
      ";runMain ch05_BookFriendRecommendationsJava" +
      ";runMain ch05_BookFriendRecommendations" +
      ";runMain ch05_SequencedNestedFlatMaps" +
      ";runMain ch05_Points2d3d" +
      ";runMain ch05_RandomForComprehensions" +
      ";runMain ch05_PointsInsideCircles" +
      ";runMain ch05_Events" +
      ";runMain TvShows" +
      ";runMain TvShowsJava" +
      ";runMain MusicArtistsSearch" +
      ";runMain Playlist" +
      ";runMain ch08_SchedulingMeetings" +
      ";runMain ch08_CastingDie"
    )
  )
