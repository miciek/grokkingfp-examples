#!/bin/sh

# sbt console regression test
# ---------------------------
# The book asks readers to follow along in `sbt console`. Each case below starts `sbt console` and types
# a single line (sbt console only reliably reads the first line of piped input), the way a reader would.
#
# Requires network access (the Wikidata case).

cd "$(dirname "$0")" || exit 1

failed=0

# Given: an `sbt console` session in this directory (with the imports from build.sbt's initialCommands)
# When:  we enter the given line
# Then:  the console prints the expected result, and no errors or exceptions
run_case() {
  name=$1
  line=$2
  expected=$3
  output=$(echo "$line" | sbt console 2>&1)
  if echo "$output" | grep -qF "$expected" && ! echo "$output" | grep -qE "Exception|Error|\[error\]"; then
    echo "PASSED: $name"
  else
    echo "FAILED: $name. Output:"
    echo "$output" | grep -E "CONSOLE-TEST|Exception|Error|\[error\]" | head -10
    failed=1
  fi
}

# Chapters 1-10 and the README: plain Scala, cats-effect and fs2 (imported by initialCommands),
# importing a module from this repository, and using top-level functions defined in this repository
run_case "Scala, cats-effect, fs2, and repository code" \
  '{ def increment(x: Int): Int = x + 1; assert(increment(6) == 7); assert(IO.delay(1 + 1).unsafeRunSync() == 2); assert(IO.sleep(10.millis).as(42).unsafeRunSync() == 42); assert(Stream(1, 2, 3).repeat.take(5).toList == List(1, 2, 3, 1, 2)); import ch09_CurrencyExchange.model._; assert(Currency("USD").name == "USD"); assert(wordScore("Scala") == 5); println("CONSOLE-TEST-" + "RESULT: all assertions passed") }' \
  "CONSOLE-TEST-RESULT: all assertions passed"

# Chapter 11: querying Wikidata with Jena
run_case "Querying Wikidata with Jena" \
  '{ val connection = RDFConnectionRemote.create.destination("https://query.wikidata.org/").queryEndpoint("sparql").build; val solutions = asScala(connection.query(QueryFactory.create("SELECT ?label WHERE { <http://www.wikidata.org/entity/Q351> <http://www.w3.org/2000/01/rdf-schema#label> ?label FILTER(LANG(?label) = \"en\") }")).execSelect()).toList; connection.close(); println("CONSOLE-TEST-" + "RESULT: " + solutions.head.getLiteral("label").getString) }' \
  "CONSOLE-TEST-RESULT: Yellowstone National Park"

exit $failed
