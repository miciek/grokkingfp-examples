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
      //      ";runMain DeletingMutability" +
      ";runMain JavaFunctionIntro" +
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
      ";runMain Playlist" +
      ";runMain ch08_SchedulingMeetings" +
      ";runMain ch08_CastingDie"
    )
  )
