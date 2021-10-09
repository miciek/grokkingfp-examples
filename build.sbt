lazy val root = (project in file("."))
  .settings(
    name := "grokkingfp-examples",
    organization := "Michał Płachta (Manning)",
    version := "1.0",
    scalaVersion := "3.0.2",
    scalacOptions ++= List("-unchecked"),
    libraryDependencies ++= Seq(
        "org.typelevel"     %% "cats-effect"     % "3.2.9",
        "co.fs2"            %% "fs2-core"        % "3.1.5",
        "org.scalatest"     %% "scalatest"       % "3.2.10" % Test,
        "org.scalatestplus" %% "scalacheck-1-15" % "3.2.10.0" % Test,
        // imperative libraries:
        "com.typesafe.akka" % "akka-actor_2.13"  % "2.6.16",
        "org.apache.jena"   % "apache-jena-libs" % "4.2.0",
        "org.apache.jena"   % "jena-fuseki-main" % "4.2.0",
        "org.slf4j"         % "slf4j-simple"     % "1.7.32"
      ),
    run / fork := true,
    run / javaOptions += "-ea",
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
      ";runMain ch06_TvShows" +
      ";runMain ch06_TvShowsJava" +
      ";runMain ch07_MusicArtistsSearch" +
      ";runMain ch07_Playlist" +
      ";runMain ch08_SchedulingMeetingsImpure" +
      ";runMain ch08_SchedulingMeetings" +
      ";runMain ch08_CastingDie" +
      ";runMain ch09_CurrencyExchange" +
      ";runMain ch09_Stream123s" +
      ";runMain ch09_CastingDieStream" +
      ";runMain ch10_CheckIns" +
      ";runMain ch10_CheckInsImperative" +
      ";runMain ch10_CastingDieConcurrently" +
      ";runMain ch11_TravelGuide" +
      ";runMain ch12_TravelGuide"
    )
  )
