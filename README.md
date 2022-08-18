# Inferring Region Types

This is a prototype implementation of the type inference algorithm introduced in the paper

> U. Schöpp and C. Xu. **Inferring Region Types via an Abstract Notion of Environment Transformation**. To appear at APLAS'22.

based on the [Soot](http://soot-oss.github.io/soot/) framework. It takes a Java (bytecode) program as input and computes the region type of the given method in the program.

Among the others, access graphs and abstract transformations are implemented in the
[regiontypeinference.transformation](src/main/java/regiontypeinference/transformation/)
package, and the type inference algorithm in the
[regiontypeinference.intraproc.TransformationAnalysis](src/main/java/regiontypeinference/intraproc/TransformationAnalysis.java)
class.

## Experiments

There are examples from the paper in the
[testcases.paperexamples](src/test/java/testcases/paperexamples/) package in the test module. To
play with them, one can run the main method in
[regiontypeinference.Main](src/main/java/regiontypeinference/Main.java) in the
main module, by inputting the corresponding class name and method name.

To display more information about the analysis, one can set the following environment variables to be `true`:
- `SHOW_TABLE` &mdash; to print the table of abstract transformations at each iteration of the fix-point procedure,
- `DEBUGGING` &mdash; to print the abstract transformation at each node of the control flow graph of the analyzed method.

## Running with Docker

Supposing the repository has been cloned to the current directory, a Docker
container can be built with:

```
cd AbstractTransformation
docker build . -t abstracttransformation
```

The artifact can then be run as follows:

```
docker run abstracttransformation
```

This will start the analysis for the examples from the paper and output the
results.


Environment variables can be set like so:
```
docker run -e SHOW_TABLE=true -e DEBUGGING=true abstracttransformation
```

## Building with Gradle

### Prerequisites

- Java 8

The artifact can also be built using [Gradle](https://gradle.org/). A configuration
file [build.gradle](build.gradle) is provided in the repository. For example,
in an IDE (e.g. [IntelliJ IDEA](https://www.jetbrains.com/idea/)), one can setup
a project for the artifact by opening the build.gradle file as a project.

## Previous Versions
- [GuideForceJava](https://github.com/cj-xu/GuideForceJava), based on our [PPDP'21](https://dl.acm.org/doi/10.1145/3479394.3479413) paper.
- [TSA](https://github.com/ezal/TSA), developed by Zălinescu et al., based on their [APLAS'17](https://doi.org/10.1007/978-3-319-71237-6_5) paper.

## Code Contributors

- [Ulrich Schöpp](https://ulrichschoepp.de/)
- [Chuangjie Xu](https://cj-xu.github.io/)
- Eugen Zălinescu
- Jakob Knauer
