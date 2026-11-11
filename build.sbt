lazy val root = (project in file("."))
  .settings(
    name                             := "grokkingfp-examples",
    organization                     := "Michał Płachta (Manning)",
    version                          := "1.0",
    scalaVersion                     := "3.10.1",
    scalacOptions ++= List(
      "-unchecked",
      "-deprecation",
      "-explain",
      // the book's examples use `extends App` and pass implicits explicitly; silence only these two warnings
      "-Wconf:msg=trait App in package scala is deprecated:s",
      "-Wconf:msg=Implicit parameters should be provided with a `using` clause:s"
    ),
    libraryDependencies ++= Seq(
      "org.typelevel"     %% "cats-effect"      % "3.7.1",
      "co.fs2"            %% "fs2-core"         % "3.14.0",
      "org.scalatest"     %% "scalatest"        % "3.2.20"   % Test,
      "org.scalatestplus" %% "scalacheck-1-19"  % "3.2.20.0" % Test,
      // imperative libraries:
      "com.typesafe.akka"  % "akka-actor_2.13"  % "2.6.20",
      "org.apache.jena"    % "apache-jena-libs" % "6.2.0",
      "org.apache.jena"    % "jena-fuseki-main" % "6.2.0",
      "org.slf4j"          % "slf4j-nop"        % "2.0.20"
    ),
    // The last lines work around a problem in sbt 2's console: the REPL's class loader defines JDK-internal XML
    // classes itself, so Jena fails to initialize (IllegalAccessError). We initialize Jena once, using the project's
    // class loader, before the reader types anything. While doing it, we hide the "no SLF4J binding" message that
    // sbt's own (older) SLF4J prints to stderr; initialization errors are still thrown. See test_sbt_console.sh.
    initialCommands                  := s"""
      import fs2._, cats.effect._, cats.implicits._, cats.effect.unsafe.implicits.global
      import scala.concurrent.duration._, java.util.concurrent._
      import scala.jdk.javaapi.CollectionConverters.asScala
      import org.apache.jena.query._, org.apache.jena.rdfconnection._
      {
        Thread.currentThread.setContextClassLoader(Thread.currentThread.getContextClassLoader.getParent)
        val stderr = System.err
        System.setErr(new java.io.PrintStream(java.io.OutputStream.nullOutputStream))
        try org.apache.jena.sys.JenaSystem.init() finally System.setErr(stderr)
      }
    """,
    // JDK 24+ warnings in the console's JVM: JLine loads a native library, and Scala's LazyVals use sun.misc.Unsafe:
    Compile / console / javaOptions ++=
      (if (java.lang.Runtime.version.feature >= 24)
         List("--enable-native-access=ALL-UNNAMED", "--sun-misc-unsafe-memory-access=allow")
       else Nil),
    // the console uses the packaged project jar; don't look for a main class for its manifest
    // (avoids the "multiple main classes detected" warning; `sbt run` still lets you choose):
    Compile / packageBin / mainClass := None,
    run / fork                       := true,
    // sbt 2 defaults to true; tests load testdata.ttl via a resource URL that Jena cannot read from a JAR:
    exportJars                       := false,
    // sbt 2 made `test` incremental; keep running all tests (as in the book):
    Test / test                      := (Test / testFull).value,
    run / javaOptions += "-ea",
    // JDK 24+ warns about sun.misc.Unsafe used by Akka and Scala's LazyVals; older JDKs don't know this flag:
    run / javaOptions ++=
      (if (java.lang.Runtime.version.feature >= 24) List("--sun-misc-unsafe-memory-access=allow")
       else Nil),
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
        ";runMain ch03_ItineraryCopying" +
        ";runMain ch03_ItineraryOO" +
        ";runMain ch03_LapTimes" +
        ";runMain ch03_ListVsString" +
        ";runMain ch03_ListVsStringScala" +
        ";runMain ch03_AbbreviateNames" +
        ";runMain ch03_ItineraryScala" +
        ";runMain ch03_SlicingAndAppending" +
        ";runMain ch04_WordScoring" +
        ";runMain ch04_JavaFunctionIntro" +
        ";runMain ch04_WordScoringScala" +
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
        ";runMain ch05_StatementsVsExpressionsJava" +
        ";runMain ch05_StatementsVsExpressions" +
        ";runMain ch05_Events" +
        ";runMain ch06_TvShows" +
        ";runMain ch06_TvShowsJava" +
        ";runMain ch07_MusicArtistsSearch" +
        ";runMain ch07_Playlist" +
        ";runMain ch08_SchedulingMeetingsImpure" +
        ";runMain ch08_SchedulingMeetings" +
        ";runMain ch08_CardGame" +
        ";runMain ch08_CastingDie" +
        ";runMain ch09_CurrencyExchange" +
        ";runMain ch09_Stream123s" +
        ";runMain ch09_CastingDieStream" +
        ";runMain ch10_CheckIns" +
        ";runMain ch10_CheckInsImperative" +
        ";runMain ch10_CastingDieConcurrently" +
        ";runMain ch11_TravelGuide" +
        ";runMain ch12_TravelGuide" +
        ";runMain chA_ScalaCheatSheet"
    )
  )
