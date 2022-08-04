# Inferring Region Types

This is a prototype implementation of the type inference algorithm introduced in the paper

> Inferring Region Types via an Abstract Notion of Environment Transformation

based on the [Soot](http://soot-oss.github.io/soot/) framework.

Among the others, access graphs and abstract transformations are implemented in the [regiontypeinference.transformation](src/main/java/regiontypeinference/transformation/) package, and the type inference algorithm in the [regiontypeinference.intraproc.TransformationAnalysis](src/main/java/regiontypeinference/intraproc/TransformationAnalysis.java) class.

## Prerequisites
- Java 8

## Building
The tool can be built using [Gradle](https://gradle.org/). A configuration file "[build.gradle](build.gradle)" is provided in the repository. For example, in an IDE (e.g. [IntelliJ IDEA](https://www.jetbrains.com/idea/)), one can setup a project for the tool by opening the build.gradle file as a project.

## Experiment
There are examples from the paper in the [testcases.paperexamples](src/test/java/testcases/paperexamples/) package. To play with them, one can run the main method in [regiontypeinference.Main](src/test/java/regiontypeinference/Main.java) in the test module, by inputting the corresponding class name and method name.

To display more information about the analysis, one can set the following flags to be `true`:
- `SHOW_TABLE` in [regiontypeinference.interproc.InterProcTransAnalysis.java](src/main/java/regiontypeinference/interproc/InterProcTransAnalysis.java) and
- `DEBUGGING` in [regiontypeinference.intraproc.TransformationAnalysis.java](src/main/java/regiontypeinference/intraproc/TransformationAnalysis.java).
