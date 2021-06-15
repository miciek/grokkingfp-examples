#!/bin/sh

sbt scalafmtSbtCheck
sbt scalafmtCheckAll
sbt runAll
sbt clean
rm -rf project/target
rm -rf project/project/target
zip -r GrokFP_examples src build.sbt project download_wikidata_ttl_files.sh README.md
