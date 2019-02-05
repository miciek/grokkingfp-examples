lazy val root = (project in file("."))
  .settings(
    name := "grokkingfp-examples",
    organization := "Michał Płachta (Manning)",
    version := "1.0",
    scalaVersion := "2.12.7",
    scalacOptions ++= List(
      "-unchecked",
      "-Ywarn-unused-import",
      "-Xfatal-warnings",
      "-Ypartial-unification",
      "-language:higherKinds",
      "-Xlint"
    ),
    scalafmtOnCompile := true,
    fork in run := true,
    javaOptions in run += "-ea",
    addCommandAlias("formatAll", ";sbt:scalafmt;test:scalafmt;compile:scalafmt"),
    addCommandAlias(
      "runAll",
      ";runMain Intro" +
      ";runMain IntroScala" +
      ";runMain ShoppingCartDiscounts" +
      ";runMain Restaurant" +
      ";runMain BookAdaptations" +
      ";runMain BookFriendRecommendations" +
      ";runMain Points2d3d" +
      ";runMain PointsInsideCircles" +
      ";runMain SequencedNestedFlatMaps" +
      ";runMain Events" +
      ";runMain RandomForComprehensions"
    )
  )
