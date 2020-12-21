#!/bin/sh

sbt runAll
sbt clean
rm -rf project/target
rm -rf project/project/target
zip -r GrokFP_examples src build.sbt project README.md
