# Inferring Region Types

This is a prototype implementation of the type inference algorithm introduced in the paper

> Inferring Region Types via an Abstract Notion of Environment Transformation

based on the [Soot](http://soot-oss.github.io/soot/) framework.

Among the others, access graphs and abstract transformations are implemented in the [guideforce.transformation](src/main/java/guideforce/transformation/) package, and the type inference algorithm in the [guideforce.intraproc.TransformationAnalysis](src/main/java/guideforce/intraproc/TransformationAnalysis.java) class.

## Prerequisites
- Java 8

## Building
The tool can be built using [Gradle](https://gradle.org/). A configuration file "[build.gradle](build.gradle)" is provided in the repository. For example, in an IDE (e.g. [IntelliJ IDEA](https://www.jetbrains.com/idea/)), one can setup a project for the tool by opening the build.gradle file as a project.

## Experiment
There are examples from the paper in the [testcases.paperexamples](src/test/java/testcases/paperexamples/) package. To play with them, one can run the main method in [guideforce.Main](src/test/java/guideforce/Main.java) in the test module, by inputting the corresponding class name and method name.

To display more information about the analysis, one can set the following flags to be `true`:
- `SHOW_TABLE` in [guideforce.interproc.InterProcTransAnalysis.java](src/main/java/guideforce/interproc/InterProcTransAnalysis.java) and
- `DEBUGGING` in [guideforce.intraproc.TransformationAnalysis.java](src/main/java/guideforce/intraproc/TransformationAnalysis.java).
