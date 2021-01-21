import cats.effect.IO
import cats.implicits._
import fs2.Stream

import java.util.concurrent.TimeUnit
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration
import scala.jdk.CollectionConverters.MapHasAsScala

object ch09_CurrencyExchange extends App {

  /**
    * PREREQUISITE: model
    */
  case class Currency(name: String) extends AnyVal

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
  {
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
  }

  // Bottom-up design
  {
    val usdTable: IO[Map[Currency, BigDecimal]] = retry(exchangeTable(Currency("USD")), 10)
    println(s"Current USD currency exchange table: ${usdTable.unsafeRunSync()}")
  }

  def naiveTrending(rates: List[BigDecimal]): Boolean = {
    rates(0) < rates(1) && rates(1) < rates(2)
  }

  // Tuples, zip, drop:
  {
    val rates = List(BigDecimal(0.81), BigDecimal(0.82), BigDecimal(0.83))
    val ratePairs: List[(BigDecimal, BigDecimal)] = List(
      (BigDecimal(0.81), BigDecimal(0.82)),
      (BigDecimal(0.82), BigDecimal(0.83))
    )

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

  check(trending(List.empty)).expect(false)
  check(trending(List(BigDecimal(1), BigDecimal(2), BigDecimal(3), BigDecimal(8)))).expect(true)
  check(trending(List(BigDecimal(1), BigDecimal(4), BigDecimal(3), BigDecimal(8)))).expect(false)
  check(trending(List(BigDecimal(1), BigDecimal(2), BigDecimal(9), BigDecimal(8)))).expect(false)

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

  val exchangeTables = List(
    Map(Currency("EUR") -> BigDecimal(0.82)),
    Map(Currency("EUR") -> BigDecimal(0.83)),
    Map(Currency("JPY") -> BigDecimal(104))
  )
  check(exchangeTables.map(extractSingleCurrencyRate(Currency("EUR"))))
    .expect(List(Some(BigDecimal(0.82)), Some(BigDecimal(0.83)), None))

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

  check(Version1.exchangeIfTrending(BigDecimal(1000), Currency("USD"), Currency("EUR")).unsafeRunSync())
    .expect(_.isInstanceOf[Option[BigDecimal]])

  // PROBLEMS: just one decision, we'd like to repeat until successful + hardcoded 3 currencyTable fetches

  /**
    * STEP 2: Using IO + recursion
    */
  { // recursion
    import Version1.lastRates

    def exchangeIfTrendingForComp(amount: BigDecimal, from: Currency, to: Currency): IO[Option[BigDecimal]] = {
      for {
        rates <- lastRates(from, to)
      } yield if (trending(rates)) Some(amount * rates.last) else None
    }

    check(exchangeIfTrendingForComp(BigDecimal(1000), Currency("USD"), Currency("EUR")).unsafeRunSync())
      .expect(_.isInstanceOf[Option[BigDecimal]])

    def exchangeIfTrending(amount: BigDecimal, from: Currency, to: Currency): IO[Option[BigDecimal]] = {
      for {
        rates  <- lastRates(from, to)
        result <- if (trending(rates)) IO.pure(Some(amount * rates.last)) else exchangeIfTrending(amount, from, to)
      } yield result
    }

    check(exchangeIfTrending(BigDecimal(1000), Currency("USD"), Currency("EUR")).unsafeRunSync())
      .expect(_.exists(_ > 750))
  }

  { // laziness and infinity
    import Version1.lastRates

    def exchangeIfTrendingCrash(amount: BigDecimal, from: Currency, to: Currency): IO[Option[BigDecimal]] = {
      exchangeIfTrendingCrash(amount, from, to)
    }

    // crashes:
    // println(exchangeIfTrendingCrash(BigDecimal(100), Currency("USD"), Currency("EUR")))

    def exchangeIfTrendingInfinity(amount: BigDecimal, from: Currency, to: Currency): IO[Option[BigDecimal]] = {
      for {
        rates  <- lastRates(from, to) // here we use flatMap, so no code below will be executed until we get rates
        result <- exchangeIfTrendingInfinity(amount, from, to)
      } yield result
    }

    // doesn't crash (returns IO):
    println(exchangeIfTrendingInfinity(BigDecimal(100), Currency("USD"), Currency("EUR")))
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

  object Version2 {
    def lastRatesCh8(from: Currency, to: Currency, n: Int): IO[List[BigDecimal]] = {
      List.range(0, n).map(_ => currencyRate(from, to)).sequence
    }
    check(lastRatesCh8(Currency("USD"), Currency("EUR"), 0)).expect(_.unsafeRunSync().size == 0)
    check(lastRatesCh8(Currency("USD"), Currency("EUR"), 10)).expect(_.unsafeRunSync().size == 10)

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

    check(lastRates(Currency("USD"), Currency("EUR"), 0)).expect(_.unsafeRunSync().size == 0)
    check(lastRates(Currency("USD"), Currency("EUR"), 10)).expect(_.unsafeRunSync().size == 10)

    def exchangeIfTrending(amount: BigDecimal, from: Currency, to: Currency): IO[BigDecimal] = {
      for {
        rates  <- lastRates(from, to, 3)
        result <- if (trending(rates)) IO.pure(amount * rates.last) else exchangeIfTrending(amount, from, to)
      } yield result
    }
  }

  check(Version2.exchangeIfTrending(BigDecimal(1000), Currency("USD"), Currency("EUR")).unsafeRunSync())
    .expect(_ > 750)

  // PROBLEMS: we analyse three elements and discard them, we don't use a sliding window, each computation is isolated, no time between calls

  /**
    * STEP 3: Using Stream
    * See [[ch09_Stream123s]] first for a Java Stream introduction.
    * See [[ch09_CastingDieStream]] for fs2 Stream introduction.
    */
  def rates(from: Currency, to: Currency): Stream[IO, BigDecimal] = {
    Stream
      .eval(exchangeTable(from))
      .map(extractSingleCurrencyRate(to))
      .unNone
      .repeat
      .orElse(rates(from, to))
  }

  val firstThreeRates: IO[List[BigDecimal]] = rates(Currency("USD"), Currency("EUR")).take(3).compile.toList
  check(firstThreeRates.unsafeRunSync).expect(_.size == 3)

  object Version3 {
    def exchangeIfTrending(amount: BigDecimal, from: Currency, to: Currency): IO[BigDecimal] = {
      rates(from, to)
        .sliding(3)
        .map(_.toList)
        .filter(trending)
        .map(_.lastOption)
        .unNone
        .head
        .compile
        .lastOrError
        .map(_ * amount)
    }
  }

  check(Version3.exchangeIfTrending(BigDecimal(1000), Currency("USD"), Currency("EUR")).unsafeRunSync())
    .expect(_ > 750)

  /**
    * STEP 4: Combining streams
    * TODO: timer/EC intro, timeout in the next chapter
    */
  object Version4 {
    val timer                   = IO.timer(ExecutionContext.global)
    val ticks: Stream[IO, Unit] = Stream.fixedRate[IO](FiniteDuration(1, TimeUnit.SECONDS))(timer)

    {
      val firstThreeRates: IO[List[(BigDecimal, Unit)]] =
        rates(Currency("USD"), Currency("EUR")).zip(ticks).take(3).compile.toList
      check(firstThreeRates.unsafeRunSync).expect(_.size == 3)
    }

    {
      val firstThreeRates: IO[List[BigDecimal]] =
        rates(Currency("USD"), Currency("EUR")).zipLeft(ticks).take(3).compile.toList
      check(firstThreeRates.unsafeRunSync).expect(_.size == 3)
    }

    def exchangeIfTrending(amount: BigDecimal, from: Currency, to: Currency): IO[BigDecimal] = {
      rates(from, to)
        .zipLeft(ticks)
        .sliding(3)
        .map(_.toList)
        .filter(trending)
        .map(_.lastOption)
        .unNone
        .head
        .map(_ * amount)
        .compile
        .lastOrError
    }
  }

  check(Version4.exchangeIfTrending(BigDecimal(1000), Currency("USD"), Currency("EUR")).unsafeRunSync())
    .expect(_ > 750)

  // STRETCH:
  // - stream program as a small building block in larger IO program (stream-based architecture)
  //
}
