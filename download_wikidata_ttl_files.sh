#!/bin/sh

# Chapter 11/12 helper script
# ---------------------------

# Downloads and creates a ttl file for chapter 11 & 12 example which uses Wikidata as data source.
# This script downloads a tiny part of Wikidata and stores it as a testdata.ttl file
# that can be used as a data source locally without needing an access to the internet.

download() {
  wget https://www.wikidata.org/wiki/Special:EntityData/$1.ttl
}

download "Q52517"    # Bridge of Sighs (tourist attraction in Venice)
download "Q641"      # Venice
download "Q1586748"  # Talco (artist from Venice)
download "Q27985819" # Spider-Man: Far from Home (movie narrated in Venice)
download "Q151904"   # Casino Royale (movie narrated in Venice)
download "Q18192306" # Inferno (movie narrated in Venice)
download "Q334780"   # Moonraker (movie narrated in Venice)
download "Q390052"   # The Talented Mr. Ripley (movie narrated in Venice)
download "Q18703892" # Youth (movie narrated in Venice)

download "Q351"      # Yellowstone National Park
download "Q1221"     # Idaho
download "Q1212"     # Montana
download "Q1214"     # Wyoming
# there are no artists in Wikidata for the locations above
download "Q13217284" # Vampire Academy: Blood Sisters (movie narrated in Montana)
download "Q175600"   # Don't Come Knocking (movie narrated in Montana)
downlaod "Q18225084" # The Hateful Eight (movie narrated in Wyoming)
download "Q148204"   # Heaven's Gate (movie narrated in Wyoming)

download "Q180402"   # Yosemite National Park
download "Q156346"   # Tuolumne County
download "Q156191"   # Mariposa County
download "Q109661"   # Madera County
# there are no artists in Wikidata for the locations above
# there are no movies in Wikidata for the locations above

cat *.ttl > src/test/resources/testdata.ttl
rm -f *.ttl
