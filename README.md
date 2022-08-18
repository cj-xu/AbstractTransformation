# Inferring Region Types

This is a prototype implementation of the type inference algorithm introduced in the paper

> Inferring Region Types via an Abstract Notion of Environment Transformation

based on the [Soot](http://soot-oss.github.io/soot/) framework.

Among the others, access graphs and abstract transformations are implemented in
the
[regiontypeinference.transformation](src/main/java/regiontypeinference/transformation/)
package, and the type inference algorithm in the
[regiontypeinference.intraproc.TransformationAnalysis](src/main/java/regiontypeinference/intraproc/TransformationAnalysis.java)
class.

## Experiments

There are examples from the paper in the
[testcases.paperexamples](src/test/java/testcases/paperexamples/) package. To
play with them, one can run the main method in
[regiontypeinference.Main](src/test/java/regiontypeinference/Main.java) in the
test module, by inputting the corresponding class name and method name.

To display more information about the analysis, one can set the following
environment variables to be `true`:
- `SHOW_TABLE`
- `DEBUGGING`

## Running with Docker

Supposing the repository has been cloned to the directory
`AbstractTransformation`, a Docker container can be built with:

```
docker build AbstractTransformation -t abstracttransformation
```

The artifact can then be run as follows:

```
docker run abstracttransformation
```

This will start the analysis for the examples from the paper an output the
results.


Environment variables can be set like so:
```
docker run -e SHOW_TABLE=true -e DEBUGGING=true abstracttransformation
```

## Building with Gradle

### Prerequisites

- Java 8

The artifact can also be built using [Gradle](https://gradle.org/). A configuration
file "[build.gradle](build.gradle)" is provided in the repository. For example,
in an IDE (e.g. [IntelliJ IDEA](https://www.jetbrains.com/idea/)), one can setup
a project for the artifact by opening the build.gradle file as a project.