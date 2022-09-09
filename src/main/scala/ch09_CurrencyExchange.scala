import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.implicits._
import fs2.Stream

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration
import scala.jdk.CollectionConverters.MapHasAsScala

object ch09_CurrencyExchange {

  /** PREREQUISITE: model
    */
  object model {
    opaque type Currency = String
    object Currency {
      def apply(name: String): Currency               = name
      extension (currency: Currency) def name: String = currency
    }
  }
  import model._

  /** PREREQUISITE: retry function from ch8
    */
  import ch08_SchedulingMeetings.retry

  /** PREREQUISITE: Impure, unsafe and side-effectful API call
    *
    * See [[ch09_CurrencyExchangeImpure.exchangeRatesTableApiCall]]
    *
    * We wrap them here to be able to use Scala immutable collections and BigDecimal.
    */
  def exchangeRatesTableApiCall(currency: String): Map[String, BigDecimal] = {
    ch09_CurrencyExchangeImpure.exchangeRatesTableApiCall(currency).asScala.view.mapValues(BigDecimal(_)).toMap
  }

  /** STEP 0: Using immutable Maps
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
    assert(updatedUsdRates == Map(Currency("EUR") -> BigDecimal(0.82), Currency("JPY") -> BigDecimal(103.91)))
    println(usdRates)
    println(updatedUsdRates)
    { // Practicing immutable maps
      // a map which contains a single pair: "key" -> "value":
      val m1: Map[String, String] = Map("key" -> "value")
      assert(m1 == Map("key" -> "value"))

      // a map which updates m1 and stores "value2" under "key2"
      val m2: Map[String, String] = m1.updated("key2", "value2")
      assert(m2 == Map("key" -> "value", "key2" -> "value2"))

      // a map which updates m2 and stores "another2" under "key2"
      val m3: Map[String, String] = m2.updated("key2", "another2")
      assert(m3 == Map("key" -> "value", "key2" -> "another2"))

      // a map which updates m2 and removes the "key"
      val m4: Map[String, String] = m3.removed("key")
      assert(m4 == Map("key2" -> "another2"))

      // a String value stored under "key" in m3
      val valueFromM3: Option[String] = m3.get("key")
      assert(valueFromM3 == Some("value"))

      // a String value stored under "key" in m4
      val valueFromM4: Option[String] = m4.get("key")
      assert(valueFromM4 == None)

      // working with currency maps
      assert(usdRates.updated(Currency("EUR"), BigDecimal(0.83)) == Map(Currency("EUR") -> BigDecimal(0.83)))

      // removed
      assert(usdRates.removed(Currency("EUR")) == Map.empty[Currency, BigDecimal])
      assert(usdRates.removed(Currency("JPY")) == usdRates)

      // get
      assert(usdRates.get(Currency("EUR")) == Some(BigDecimal(0.82)))
      assert(usdRates.get(Currency("JPY")) == None)
    }
  }

  private def bottomUpDesign = {
    val usdTable: IO[Map[String, BigDecimal]] = retry(IO.delay(exchangeRatesTableApiCall("USD")), 10)
    println(s"Current USD currency exchange table: ${usdTable.unsafeRunSync()}")
  }

  def naiveTrending(rates: List[BigDecimal]): Boolean = {
    rates(0) < rates(1) && rates(1) < rates(2)
  }

  // Tuples, zip, & drop:
  private def tuplesZipDrop = {
    val rates: List[BigDecimal]                   = List(BigDecimal(0.81), BigDecimal(0.82), BigDecimal(0.83))
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
    assert(ints.zip(strings) == List((1, "a"), (2, "b"), (3, "c")))

    assert(rates.zip(rates) ==
      List(
        (BigDecimal(0.81), BigDecimal(0.81)),
        (BigDecimal(0.82), BigDecimal(0.82)),
        (BigDecimal(0.83), BigDecimal(0.83))
      ))

    assert(rates.drop(1) == List(BigDecimal(0.82), BigDecimal(0.83)))

    assert(rates.zip(rates.drop(1)) == ratePairs)
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
    assert(trending(List.empty) == false)
    assert(trending(List(BigDecimal(1), BigDecimal(2), BigDecimal(3), BigDecimal(8))) == true)
    assert(trending(List(BigDecimal(1), BigDecimal(4), BigDecimal(3), BigDecimal(8))) == false)
    assert(trending(List(BigDecimal(1), BigDecimal(2), BigDecimal(9), BigDecimal(8))) == false)
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
      Map(Currency("EUR") -> BigDecimal(0.88)),
      Map(Currency("EUR") -> BigDecimal(0.89), Currency("JPY") -> BigDecimal(114.62)),
      Map(Currency("JPY") -> BigDecimal(114))
    )
    assert(usdExchangeTables.map(extractSingleCurrencyRate(Currency("EUR"))) == List(
      Some(BigDecimal(0.88)),
      Some(BigDecimal(0.89)),
      None
    ))
    assert(usdExchangeTables.map(extractSingleCurrencyRate(Currency("JPY"))) == List(
      None,
      Some(BigDecimal(114.62)),
      Some(BigDecimal(114))
    ))
    assert(usdExchangeTables.map(extractSingleCurrencyRate(Currency("BTC"))) == List(None, None, None))
    assert(List.empty.map(extractSingleCurrencyRate(Currency("EUR"))) == List.empty)

    { // alternative implementation
      def extractSingleCurrencyRate2(
          currencyToExtract: Currency
      )(table: Map[Currency, BigDecimal]): Option[BigDecimal] = {
        table.get(currencyToExtract)
      }

      assert(usdExchangeTables.map(extractSingleCurrencyRate2(Currency("EUR"))) == List(
        Some(BigDecimal(0.88)),
        Some(BigDecimal(0.89)),
        None
      ))
      assert(usdExchangeTables.map(extractSingleCurrencyRate2(Currency("JPY"))) == List(
        None,
        Some(BigDecimal(114.62)),
        Some(BigDecimal(114))
      ))
      assert(usdExchangeTables.map(extractSingleCurrencyRate2(Currency("BTC"))) == List(None, None, None))
      assert(List.empty.map(extractSingleCurrencyRate2(Currency("EUR"))) == List.empty)
    }
  }

  /** STEP 1: Using IO
    */
  def exchangeTable(from: Currency): IO[Map[Currency, BigDecimal]] = {
    IO.delay(exchangeRatesTableApiCall(from.name)).map(table =>
      table.map(kv =>
        kv.match {
          case (currencyName, rate) => (Currency(currencyName), rate)
        }
      )
    )
  }

  {
    exchangeTable(Currency("USD")).map(extractSingleCurrencyRate(Currency("EUR")))
  }

  object Version1 {
    def lastRates(from: Currency, to: Currency): IO[List[BigDecimal]] = {
      for {
        table1    <- retry(exchangeTable(from), 10)
        table2    <- retry(exchangeTable(from), 10)
        table3    <- retry(exchangeTable(from), 10)
        lastTables = List(table1, table2, table3)
      } yield lastTables.flatMap(extractSingleCurrencyRate(to))
    }

    def exchangeIfTrending(amount: BigDecimal, from: Currency, to: Currency): IO[Option[BigDecimal]] = {
      lastRates(from, to)
        .map(rates => if (trending(rates)) Some(amount * rates.last) else None)
    }
  }

  private def runVersion1 = {
    assert(Version1.exchangeIfTrending(BigDecimal(1000), Currency("USD"), Currency("EUR")).unsafeRunSync().isInstanceOf[
      Option[BigDecimal]
    ])
  }

  // PROBLEMS: just one decision, we'd like to repeat until successful + hardcoded 3 currencyTable fetches

  /** STEP 2: Using IO + recursion
    */
  private def runStep2 = { // recursion
    import Version1.lastRates

    def exchangeIfTrendingForComp(amount: BigDecimal, from: Currency, to: Currency): IO[Option[BigDecimal]] = {
      for {
        rates <- lastRates(from, to)
      } yield if (trending(rates)) Some(amount * rates.last) else None
    }

    assert(exchangeIfTrendingForComp(BigDecimal(1000), Currency("USD"), Currency("EUR")).unsafeRunSync().isInstanceOf[
      Option[BigDecimal]
    ])

    def exchangeIfTrending(amount: BigDecimal, from: Currency, to: Currency): IO[Option[BigDecimal]] = {
      for {
        rates  <- lastRates(from, to)
        result <- if (trending(rates)) IO.pure(Some(amount * rates.last)) else exchangeIfTrending(amount, from, to)
      } yield result
    }

    assert(exchangeIfTrending(BigDecimal(1000), Currency("USD"), Currency("EUR")).unsafeRunSync().exists(_ > 750))
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

    assert(exchangeIfTrending(BigDecimal(1000), Currency("USD"), Currency("EUR")).unsafeRunSync() > 750)
  }

  def currencyRate(from: Currency, to: Currency): IO[BigDecimal] = {
    for {
      table <- retry(exchangeTable(from), 10)
      rate  <- extractSingleCurrencyRate(to)(table) match {
                 case Some(value) => IO.pure(value)
                 case None        => currencyRate(from, to)
               }
    } yield rate
  }

  def lastRatesCh8(from: Currency, to: Currency, n: Int): IO[List[BigDecimal]] = {
    List.range(0, n).map(_ => currencyRate(from, to)).sequence
  }

  private def runLastRatesCh8 = {
    assert(lastRatesCh8(Currency("USD"), Currency("EUR"), 0).unsafeRunSync().size == 0)
    assert(lastRatesCh8(Currency("USD"), Currency("EUR"), 10).unsafeRunSync().size == 10)
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
    assert(lastRates(Currency("USD"), Currency("EUR"), 0).unsafeRunSync().size == 0)
    assert(lastRates(Currency("USD"), Currency("EUR"), 10).unsafeRunSync().size == 10)

    assert(exchangeIfTrending(BigDecimal(1000), Currency("USD"), Currency("EUR")).unsafeRunSync() > 750)
  }

  // PROBLEMS: we analyse three elements and discard them, we don't use a sliding window, each computation is isolated, no time between calls

  /** STEP 3: Using Stream
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

    assert(stream1.compile.toList.unsafeRunSync() == List(996))
    assert(stream2.compile.toList.unsafeRunSync() == List(2020))
    assert(stream3.compile.toList.unsafeRunSync() == List(996))

    try {
      stream4.compile.toList.unsafeRunSync()
    } catch {
      case e: Throwable => assert(e.getMessage == "can't recover")
    }
  }

  val firstThreeRates: IO[List[BigDecimal]] = rates(Currency("USD"), Currency("EUR")).take(3).compile.toList

  private def runFirstThreeRates = {
    assert(firstThreeRates.unsafeRunSync().size == 3)
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
    assert(Version3.exchangeIfTrending(BigDecimal(1000), Currency("USD"), Currency("EUR")).unsafeRunSync() > 750)
  }

  /** STEP 4: Combining streams
    */
  val delay: FiniteDuration   = FiniteDuration(1, TimeUnit.SECONDS)
  val ticks: Stream[IO, Unit] = Stream.fixedRate[IO](delay)

  private def ratesZipTicks = {
    val firstThreeRates: IO[List[(BigDecimal, Unit)]] =
      rates(Currency("USD"), Currency("EUR")).zip(ticks).take(3).compile.toList
    assert(firstThreeRates.unsafeRunSync().size == 3)
  }

  private def ratesZipLeftTicks = {
    val firstThreeRates: IO[List[BigDecimal]] =
      rates(Currency("USD"), Currency("EUR")).zipLeft(ticks).take(3).compile.toList
    assert(firstThreeRates.unsafeRunSync().size == 3)
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
    assert(Version4.exchangeIfTrending(BigDecimal(1000), Currency("USD"), Currency("EUR")).unsafeRunSync() > 750)
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
