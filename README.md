# Grokking Functional Programming - source code from the book

This project contains all the source code included in the ["Grokking Functional Programming"](https://michalplachta.com/book) book. **It's your learning companion**. It helps you understand the code in the book, serves as an additional reference, and allows you to explore many functional programming ideas on your own. 

The best way to use this project (and the book!) is to follow along—page after page—on your computer. To do that, **execute `sbt console` in this directory and start reading the book** (if you don't have `sbt` installed, read the [Installing the tools section](#installing-the-tools) below.) Additionally, after each chapter in the book, we remind you that the source code is up for you to explore. You can then open an editor (or an IDE) and read through the files that contain this chapter's code. Note that some chapters have more than one file, in both `java` and `scala` subdirectories. Each file is prefixed with the chapter number for your convenience.

#### Imperative and functional examples

The book contains examples from both worlds: [imperative/OO are written in Java](./src/main/java) while [functional are written in Scala](./src/main/scala). This convention is used in both the book and this repository.

#### The book uses simplified Scala

This book teaches functional programming, not Scala, so we don't use all features of Scala, just the ones that are more universal (i.e., you can find them in other FP languages) and allow us to get a point across. Review Appendix A in the book to see which features of Scala are used in the book (alternatively, you can look at the [source code of Appendix A here](./src/main/scala/chA_ScalaCheatSheet.scala)).

We also use only the features that we have introduced so far. That's why some things are done differently earlier in the book. Then, in later chapters, when we introduce a particular technique that helps to solve the same problem in a better, more readable way, we start using this technique instead.

Additionally, we use plain old `assert` in the source code to be as lean as possible when we check that the code does exactly what we want. There is a chapter about testing, where we use a fully-featured testing library but its usages are constrained only to this chapter. 

All three reasons mentioned earlier: using a simplified Scala subset, using only what we've introduced so far, and using both `assert` and a proper testing library at the same time, may contribute to a feeling that the codebase as a whole is not consistent. This is done on purpose since the project has been optimised for learning experience—it is meant to be a learning aid!

#### :warning: What to do when you face problems?

The code repository was thoroughly tested when the book was published. All code samples in the book are runnable and their results are `assert`ed. However, some things may change after the book is published. For example, in the last part of the book we use the real Wikidata API that does real queries. It's possible that it changes in the future. Additionally, there can be new tools, some tools may get deprecated, or simply stop working. In any case, please make sure to visit [the book webpage](https://michalplachta.com/book) to obtain the newest version of this code repository, which most likely will have fixes for all the issues above!

If you face any other problems, like compilation errors, runtime errors, IDE problems, let us know there as well and we will try to guide you through them.

## Using this repository

#### Installing the tools

###### Installing the Java Development Kit (JDK)

JDK will allow your to run Java and Scala (which is a JVM language) code. If you are unsure, please run `javac -version` in your terminal, and you should get something like `javac 17`. If you don’t, please visit https://jdk.java.net/17/. 

###### Installing **sbt** (Scala build tool)

`sbt` is a build tool used in the Scala ecosystem. It can be used to create, build, and test projects. Visit https://www.scala-sbt.org/download.html to get the instructions of how to install the `sbt` on your platform.

###### Installing required libraries

We use several external libraries in later chapters of the book. Most importantly, we use [cats-effect](https://github.com/typelevel/cats-effect) in chapters 8-12, [fs2](https://github.com/typelevel/fs2) in chapters 9 & 10, and [scalatest](https://github.com/scalatest/scalatest) in chapter 12. They will automatically be installed in this directory when you run `sbt` the first time. All external libraries and their versions are defined in the [build.sbt](./build.sbt) file.

#### Running the examples

To make sure everything is set up properly, you may want to run all the examples from all chapters. The [build.sbt](./build.sbt) file defines an alias that helps you do that. You can execute all examples by running `sbt runAll`. You can also run examples from specific chapters by providing the name of a file to run, e.g., `sbt 'runMain ch08_CastingDie'`. Alternatively, you can execute `sbt run` and you will need to manually choose which file to run.

#### Using the `sbt console`

The preferred way of using the source code repository with the book is the `sbt console` command. Just execute `sbt console` in the root directory and you are set! Now, you can write the code from the book in your `sbt console` session and see the results for yourself. The snippets in the book that are runnable are marked with a grey box that contains a single `>` prompt. Each chapter starts from scratch so make sure to enter `:reset` before starting writing code in a new chapter. 

###### How does it work?

If you wonder how `sbt console` works, all the answers are in the [build.sbt](./build.sbt) file included in this repository. It defines all the required libraries that we use in the book and adds some automatic imports so that snippets from the book work flawlessly in your `sbt console`.

###### Importing examples into `sbt console`

You can import existing functions and modules into your `sbt console` sessions. We use this feature later in the book, where examples get more advanced and readers are not expected to write everything themselves. You can use the normal `import` statement to import specific modules or the `:load` command to import files into your session, e.g., `:load src/main/scala/ch01_IntroScala.scala` will import all top-level functions/objects from the `ch01_IntroScala.scala` file, while `import ch09_CurrencyExchange.model._` will import all types required to implement the currency exchange application in chapter 9.

#### Using the IDE

When you get familiar with how the book and its exercises work, you are free to import `build.sbt` file to your IDE (e.g. [IntelliJ IDEA](https://www.jetbrains.com/idea)) and do the exercises and exploration there. This is the preferred way of following along in chapter 12.

When you open your IDE, click `Open` in the project chooser dialog and choose the `build.sbt` file from this repository. The IDE should import the project automatically.

###### How to use this repository in chapter 12?

Chapter 12, the last chapter of the book, teaches you how to test functional programs. You will learn the most if you try to write all the tests yourself! To do that, import the project into the IDE, open the `src/test/scala/ch12_TravelGuideTest.scala` file and remove all the tests from there (or alternatively, move the file elsewhere so that `sbt test` doesn't use it.) Then, write all your tests in the `src/test/scala/ch12_BasicTest.scala` file while reading the chapter. Remember that you run your tests by executing `sbt test` in the terminal (not the `sbt console`).

###### Why do I see warnings in my IDE?

You may see many warnings in the IDE when you import the project. That's completely fine because we use a simplified version of Scala! We also don't use all the features at the beginning of the book (they are all introduced gradually), so you may see some IDE hints that are completely OK for an advanced Scala programmer, but may not be as helpful if your objective is to learn some universal FP concepts. If you are using the IDE for the whole book (not just in chapter 12, as recommended above), please ignore these warnings, but make sure you come back to them once you finish the book (including [Appendix A](./src/main/scala/chA_ScalaCheatSheet.scala)!). I am sure you will by then be familiar with many of these warnings and will be able to understand and apply them all!

## Make sure to visit the book's web page for more!

I hope you will find this repository very useful when reading the book. Again, if you face any problems, visit [the book webpage](https://michalplachta.com/book) which should contain some useful tips, new materials, new solutions, bonus exercises, and a way to contact me when things go awry!

Enjoy and good luck!

-- Michał Płachta
