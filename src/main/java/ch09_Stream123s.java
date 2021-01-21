import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ch09_Stream123s {
    public static void main(String[] args) {
        Stream<Integer> numbers = Stream.of(1, 2, 3);
        Stream<Integer> oddNumbers = oddNumbers(numbers);
        List<Integer> result = oddNumbers.collect(Collectors.toList());
        check.apply(result).expect(List.of(1, 3));

        Stream<Integer> infiniteNumbers = Stream.iterate(0, i -> i + 1);
        Stream<Integer> infiniteOddNumbers = oddNumbers(infiniteNumbers);

        // List<Integer> infiniteResult = infiniteOddNumbers.collect(Collectors.toList());
        Stream<Integer> limitedStream = infiniteOddNumbers.limit(3);
        List<Integer> limitedResult = limitedStream.collect(Collectors.toList());
        check.apply(limitedResult).expect(List.of(1, 3, 5));

        Stream<Integer> randomNumbers = Stream.generate(new Random()::nextInt);
        List<Integer> randomResult = oddNumbers(randomNumbers).limit(3).collect(Collectors.toList());
        System.out.println(randomResult);

        // TODO: add Kotlin example
        Stream<Map<String, BigDecimal>> usdRates = Stream.generate(() -> ch09_CurrencyExchangeImpure.exchangeRatesTableApiCall("USD"));
        try {
            System.out.println("The following will throw: ");
            usdRates.limit(10).collect(Collectors.toList());
            System.out.println("Most probably won't happen!");
        } catch(Exception e) {
            System.out.println("Exception thrown: " + e.getMessage());
        }
    }

    static Stream<Integer> oddNumbers(Stream<Integer> numbers) {
        return numbers.filter(n -> n % 2 != 0);
    }

    /** in Python:
     * import itertools
     *
     * def infinite_numbers():
     *     x=0
     *     while(True):
     *         x=x+1
     *         yield x
     *
     *
     * def odd_numbers(numbers):
     *     return filter(lambda i: i%2 != 0, numbers)
     *
     * infinite_odd_numbers = odd_numbers(infinite_numbers())
     *
     * limited_result = itertools.islice(infinite_odd_numbers, 3)
     *
     * print(list(limited_result))
     * [1, 3, 5]
     */
}
