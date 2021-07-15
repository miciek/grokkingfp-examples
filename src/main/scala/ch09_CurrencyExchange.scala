import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.implicits._
import fs2.Stream

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration
import scala.jdk.CollectionConverters.MapHasAsScala

object ch09_CurrencyExchange {

  /**
    * PREREQUISITE: model
    */
  object model:
    opaque type Currency = String
    object Currency:
      def apply(name: String): Currency = name
      extension(currency: Currency) def name: String = currency
  import model._

  /**
    * PREREQUISITE: retry function from ch8
    */
  import ch08_SchedulingMeetings.retry

  /**
    * PREREQUISITE: Impure, unsafe and side-effectful API call
    *
    * See [[ch09_CurrencyExchangeImpure.exchangeRatesTableApiCall]]
    *
    * We wrap them here to be able to use Scala immutable collections and BigDecimal.
    */
  import ch09_CurrencyExchangeImpure.exchangeRatesTableApiCall

  def exchangeTable(from: Currency): IO[Map[Currency, BigDecimal]] = {
    IO.delay(exchangeRatesTableApiCall(from.name).asScala.map {
      case (currencyName, rate) => (Currency(currencyName), BigDecimal(rate))
    }.toMap) // implementation is not important, we just use the signature in the book
  }

  /**
    * STEP 0: Using immutable Maps
    */
  private def runStep0 = {
    // create
    val noRates: Map[Currency, BigDecimal]  = Map.empty
    val usdRates: Map[Currency, BigDecimal] = Map(Currency("EUR") -> BigDecimal(0.82))
    val eurRates: Map[Currency, BigDecimal] = Map(
      Currency("USD") -> BigDecimal(1.22),
      Currency("JPY") -> BigDecimal(126.34)
    )
    println(noRates)
    println(usdRates)
    println(eurRates)

    // updated
    val updatedUsdRates = usdRates.updated(Currency("JPY"), BigDecimal(103.91))
    println(usdRates)
    println(updatedUsdRates)
    check(usdRates.updated(Currency("EUR"), BigDecimal(0.83))).expect(Map(Currency("EUR") -> BigDecimal(0.83)))

    // removed
    check(usdRates.removed(Currency("EUR"))).expect(Map.empty[Currency, BigDecimal])
    check(usdRates.removed(Currency("JPY"))).expect(usdRates)

    // get
    check(usdRates.get(Currency("EUR"))).expect(Some(BigDecimal(0.82)))
    check(usdRates.get(Currency("JPY"))).expect(None)

    { // Practicing immutable maps
      // a map which contains a single pair: "key" -> "value":
      val m1: Map[String, String] = Map("key" -> "value")
      check(m1).expect(Map("key" -> "value"))

      // a map which updates m1 and stores "value2" under "key2"
      val m2: Map[String, String] = m1.updated("key2", "value2")
      check(m2).expect(Map("key" -> "value", "key2" -> "value2"))

      // a map which updates m2 and stores "another2" under "key2"
      val m3: Map[String, String] = m2.updated("key2", "another2")
      check(m3).expect(Map("key" -> "value", "key2" -> "another2"))

      // a map which updates m2 and removes the "key"
      val m4: Map[String, String] = m3.removed("key")
      check(m4).expect(Map("key2" -> "another2"))

      // a String value stored under "key" in m3
      val valueFromM3: Option[String] = m3.get("key")
      check(valueFromM3).expect(Some("value"))

      // a String value stored under "key" in m4
      val valueFromM4: Option[String] = m4.get("key")
      check(valueFromM4).expect(None)
    }
  }

  private def bottomUpDesign = {
    val usdTable: IO[Map[Currency, BigDecimal]] = retry(exchangeTable(Currency("USD")), 10)
    println(s"Current USD currency exchange table: ${usdTable.unsafeRunSync()}")
  }

  def naiveTrending(rates: List[BigDecimal]): Boolean = {
    rates(0) < rates(1) && rates(1) < rates(2)
  }

  // Tuples, zip, & drop:
  private def tuplesZipDrop = {
    val rates = List(BigDecimal(0.81), BigDecimal(0.82), BigDecimal(0.83))
    val ratePairs: List[(BigDecimal, BigDecimal)] = List(
      (BigDecimal(0.81), BigDecimal(0.82)),
      (BigDecimal(0.82), BigDecimal(0.83))
    )

    { // analogical to a case class
      case class RatePair(previousRate: BigDecimal, rate: BigDecimal)
      val tuple: (BigDecimal, BigDecimal) = (BigDecimal(2), BigDecimal(1))
      val caseClass: RatePair             = RatePair(BigDecimal(2), BigDecimal(1))

      println(tuple)
      println(caseClass)
    }

    val ints: List[Int]       = List(1, 2, 3)
    val strings: List[String] = List("a", "b", "c")
    check(ints.zip(strings)).expect(List((1, "a"), (2, "b"), (3, "c")))

    check(rates.zip(rates)).expect(
      List(
        (BigDecimal(0.81), BigDecimal(0.81)),
        (BigDecimal(0.82), BigDecimal(0.82)),
        (BigDecimal(0.83), BigDecimal(0.83))
      )
    )

    check(rates.drop(1)).expect(List(BigDecimal(0.82), BigDecimal(0.83)))

    check(rates.zip(rates.drop(1))).expect(ratePairs)
  }

  // zip + tuples + tuple pattern matching intro
  def trending(rates: List[BigDecimal]): Boolean = {
    rates.size > 1 &&
    rates
      .zip(rates.drop(1))
      .forall(ratePair =>
        ratePair match {
          case (previousRate, rate) => rate > previousRate
        }
      )
  }

  private def runTrending = {
    check(trending(List.empty)).expect(false)
    check(trending(List(BigDecimal(1), BigDecimal(2), BigDecimal(3), BigDecimal(8)))).expect(true)
    check(trending(List(BigDecimal(1), BigDecimal(4), BigDecimal(3), BigDecimal(8)))).expect(false)
    check(trending(List(BigDecimal(1), BigDecimal(2), BigDecimal(9), BigDecimal(8)))).expect(false)
  }

  def extractSingleCurrencyRate(currencyToExtract: Currency)(table: Map[Currency, BigDecimal]): Option[BigDecimal] = {
    table
      .filter(kv =>
        kv match {
          case (currency, rate) => currency == currencyToExtract
        }
      )
      .values
      .headOption
  }

  private def runExtractSingleCurrencyRate = {
    val usdExchangeTables = List(
      Map(Currency("EUR") -> BigDecimal(0.82)),
      Map(Currency("EUR") -> BigDecimal(0.83)),
      Map(Currency("JPY") -> BigDecimal(104))
    )
    check(usdExchangeTables.map(extractSingleCurrencyRate(Currency("EUR"))))
      .expect(List(Some(BigDecimal(0.82)), Some(BigDecimal(0.83)), None))
    check(usdExchangeTables.map(extractSingleCurrencyRate(Currency("JPY"))))
      .expect(List(None, None, Some(BigDecimal(104))))
    check(usdExchangeTables.map(extractSingleCurrencyRate(Currency("BTC"))))
      .expect(List(None, None, None))
    check(List.empty.map(extractSingleCurrencyRate(Currency("EUR"))))
      .expect(List.empty)

    { // alternative implementation
      def extractSingleCurrencyRate2(
          currencyToExtract: Currency
      )(table: Map[Currency, BigDecimal]): Option[BigDecimal] = {
        table.get(currencyToExtract)
      }

      check(usdExchangeTables.map(extractSingleCurrencyRate2(Currency("EUR"))))
        .expect(List(Some(BigDecimal(0.82)), Some(BigDecimal(0.83)), None))
      check(usdExchangeTables.map(extractSingleCurrencyRate2(Currency("JPY"))))
        .expect(List(None, None, Some(BigDecimal(104))))
      check(usdExchangeTables.map(extractSingleCurrencyRate2(Currency("BTC"))))
        .expect(List(None, None, None))
      check(List.empty.map(extractSingleCurrencyRate2(Currency("EUR"))))
        .expect(List.empty)
    }
  }

  /**
    * STEP 1: Using IO
    */
  {
    for {
      table <- exchangeTable(Currency("USD"))
    } yield extractSingleCurrencyRate(Currency("EUR"))(table)
  }

  object Version1 {
    def lastRates(from: Currency, to: Currency): IO[List[BigDecimal]] = {
      for {
        table1     <- retry(exchangeTable(from), 10)
        table2     <- retry(exchangeTable(from), 10)
        table3     <- retry(exchangeTable(from), 10)
        lastTables = List(table1, table2, table3)
      } yield lastTables.flatMap(extractSingleCurrencyRate(to))
    }

    def exchangeIfTrending(amount: BigDecimal, from: Currency, to: Currency): IO[Option[BigDecimal]] = {
      lastRates(from, to)
        .map(rates => if (trending(rates)) Some(amount * rates.last) else None)
    }
  }

  private def runVersion1 = {
    check(Version1.exchangeIfTrending(BigDecimal(1000), Currency("USD"), Currency("EUR")).unsafeRunSync())
      .expectThat(_.isInstanceOf[Option[BigDecimal]])
  }

  // PROBLEMS: just one decision, we'd like to repeat until successful + hardcoded 3 currencyTable fetches

  /**
    * STEP 2: Using IO + recursion
    */
  private def runStep2 = { // recursion
    import Version1.lastRates

    def exchangeIfTrendingForComp(amount: BigDecimal, from: Currency, to: Currency): IO[Option[BigDecimal]] = {
      for {
        rates <- lastRates(from, to)
      } yield if (trending(rates)) Some(amount * rates.last) else None
    }

    check(exchangeIfTrendingForComp(BigDecimal(1000), Currency("USD"), Currency("EUR")).unsafeRunSync())
      .expectThat(_.isInstanceOf[Option[BigDecimal]])

    def exchangeIfTrending(amount: BigDecimal, from: Currency, to: Currency): IO[Option[BigDecimal]] = {
      for {
        rates  <- lastRates(from, to)
        result <- if (trending(rates)) IO.pure(Some(amount * rates.last)) else exchangeIfTrending(amount, from, to)
      } yield result
    }

    check(exchangeIfTrending(BigDecimal(1000), Currency("USD"), Currency("EUR")).unsafeRunSync())
      .expectThat(_.exists(_ > 750))
  }

  private def lazinessAndInfinity = {
    import Version1.lastRates

    // crashes:
    // def exchangeCrash(amount: BigDecimal, from: Currency, to: Currency): IO[Option[BigDecimal]] = {
    //  exchangeCrash(amount, from, to)
    // }
    // println(exchangeCrash(BigDecimal(100), Currency("USD"), Currency("EUR")))

    def exchangeInfinitely(amount: BigDecimal, from: Currency, to: Currency): IO[Option[BigDecimal]] = {
      for {
        rates  <- lastRates(from, to) // here we use flatMap, so no code below will be executed until we get rates
        result <- exchangeInfinitely(amount, from, to)
      } yield result
    }

    // doesn't crash (returns IO):
    println(exchangeInfinitely(BigDecimal(100), Currency("USD"), Currency("EUR")))
  }

  private def gettingRidOfOption = {
    import Version1.lastRates

    def exchangeIfTrending(amount: BigDecimal, from: Currency, to: Currency): IO[BigDecimal] = {
      for {
        rates  <- lastRates(from, to)
        result <- if (trending(rates)) IO.pure(amount * rates.last) else exchangeIfTrending(amount, from, to)
      } yield result
    }

    check(exchangeIfTrending(BigDecimal(1000), Currency("USD"), Currency("EUR")).unsafeRunSync())
      .expectThat(_ > 750)
  }

  def currencyRate(from: Currency, to: Currency): IO[BigDecimal] = {
    for {
      table <- retry(exchangeTable(from), 10)
      rate <- extractSingleCurrencyRate(to)(table) match {
               case Some(value) => IO.pure(value)
               case None        => currencyRate(from, to)
             }
    } yield rate
  }

  def lastRatesCh8(from: Currency, to: Currency, n: Int): IO[List[BigDecimal]] = {
    List.range(0, n).map(_ => currencyRate(from, to)).sequence
  }

  private def runLastRatesCh8 = {
    check(lastRatesCh8(Currency("USD"), Currency("EUR"), 0)).expectThat(_.unsafeRunSync().size == 0)
    check(lastRatesCh8(Currency("USD"), Currency("EUR"), 10)).expectThat(_.unsafeRunSync().size == 10)
  }

  object Version2 {
    def lastRates(from: Currency, to: Currency, n: Int): IO[List[BigDecimal]] = {
      if (n < 1) {
        IO.pure(List.empty)
      } else {
        for {
          currencyRate   <- currencyRate(from, to)
          remainingRates <- if (n == 1) IO.pure(List.empty) else lastRates(from, to, n - 1)
        } yield remainingRates.prepended(currencyRate)
      }
    }

    def exchangeIfTrending(amount: BigDecimal, from: Currency, to: Currency): IO[BigDecimal] = {
      for {
        rates  <- lastRates(from, to, 3)
        result <- if (trending(rates)) IO.pure(amount * rates.last) else exchangeIfTrending(amount, from, to)
      } yield result
    }
  }

  private def runVersion2 = {
    import Version2._
    check(lastRates(Currency("USD"), Currency("EUR"), 0)).expectThat(_.unsafeRunSync().size == 0)
    check(lastRates(Currency("USD"), Currency("EUR"), 10)).expectThat(_.unsafeRunSync().size == 10)

    check(exchangeIfTrending(BigDecimal(1000), Currency("USD"), Currency("EUR")).unsafeRunSync())
      .expectThat(_ > 750)
  }

  // PROBLEMS: we analyse three elements and discard them, we don't use a sliding window, each computation is isolated, no time between calls

  /**
    * STEP 3: Using Stream
    * See [[ch09_Stream123s]] first for a Java Stream introduction.
    * See [[ch09_CastingDieStream]] for fs2 Stream introduction.
    */
  def rates(from: Currency, to: Currency): Stream[IO, BigDecimal] = {
    Stream
      .eval(exchangeTable(from))
      .repeat
      .map(extractSingleCurrencyRate(to))
      .unNone
      .orElse(rates(from, to))
  }

  private def introduceOrElse = {
    val year: Stream[IO, Int]   = Stream.eval(IO.pure(996))
    val noYear: Stream[IO, Int] = Stream.raiseError[IO](new Exception("no year"))

    val stream1 = year.orElse(Stream.eval(IO.delay(2020)))
    val stream2 = noYear.orElse(Stream.eval(IO.delay(2020)))
    val stream3 = year.orElse(Stream.raiseError[IO](new Exception("can't recover")))
    val stream4 = noYear.orElse(Stream.raiseError[IO](new Exception("can't recover")))

    check(stream1.compile.toList.unsafeRunSync()).expect(List(996))
    check(stream2.compile.toList.unsafeRunSync()).expect(List(2020))
    check(stream3.compile.toList.unsafeRunSync()).expect(List(996))

    try {
      stream4.compile.toList.unsafeRunSync()
    } catch {
      case e: Throwable => assert(e.getMessage == "can't recover")
    }
  }

  val firstThreeRates: IO[List[BigDecimal]] = rates(Currency("USD"), Currency("EUR")).take(3).compile.toList

  private def runFirstThreeRates = {
    check(firstThreeRates.unsafeRunSync()).expectThat(_.size == 3)
  }

  object Version3 {
    def exchangeIfTrending(amount: BigDecimal, from: Currency, to: Currency): IO[BigDecimal] = {
      rates(from, to)
        .sliding(3)
        .map(_.toList)
        .filter(trending)
        .map(_.last)
        .take(1)
        .compile
        .lastOrError
        .map(_ * amount)
    }
  }

  private def runVersion3 = {
    check(Version3.exchangeIfTrending(BigDecimal(1000), Currency("USD"), Currency("EUR")).unsafeRunSync())
      .expectThat(_ > 750)
  }

  /**
    * STEP 4: Combining streams
    */
  val delay: FiniteDuration   = FiniteDuration(1, TimeUnit.SECONDS)
  val ticks: Stream[IO, Unit] = Stream.fixedRate[IO](delay)

  private def ratesZipTicks = {
    val firstThreeRates: IO[List[(BigDecimal, Unit)]] =
      rates(Currency("USD"), Currency("EUR")).zip(ticks).take(3).compile.toList
    check(firstThreeRates.unsafeRunSync()).expectThat(_.size == 3)
  }

  private def ratesZipLeftTicks = {
    val firstThreeRates: IO[List[BigDecimal]] =
      rates(Currency("USD"), Currency("EUR")).zipLeft(ticks).take(3).compile.toList
    check(firstThreeRates.unsafeRunSync()).expectThat(_.size == 3)
  }

  object Version4 {
    def exchangeIfTrending(amount: BigDecimal, from: Currency, to: Currency): IO[BigDecimal] = {
      rates(from, to)
        .zipLeft(ticks)
        .sliding(3)
        .map(_.toList)
        .filter(trending)
        .map(_.last)
        .take(1)
        .compile
        .lastOrError
        .map(_ * amount)
    }
  }

  private def runVersion4 = {
    check(Version4.exchangeIfTrending(BigDecimal(1000), Currency("USD"), Currency("EUR")).unsafeRunSync())
      .expectThat(_ > 750)
  }

  def main(args: Array[String]): Unit = {
    runStep0
    bottomUpDesign
    tuplesZipDrop
    runTrending
    runExtractSingleCurrencyRate
    runVersion1
    runStep2
    lazinessAndInfinity
    gettingRidOfOption
    runLastRatesCh8
    runVersion2
    introduceOrElse
    runFirstThreeRates
    runVersion3
    ratesZipTicks
    ratesZipLeftTicks
    runVersion4
  }
}
