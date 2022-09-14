# Inferring Region Types via Abstract Transformations

This is a prototype implementation of the type inference algorithm introduced in the paper

> U. Schöpp and C. Xu. **Inferring Region Types via an Abstract Notion of Environment Transformation**. To appear at APLAS'22.

based on the [Soot](http://soot-oss.github.io/soot/) framework. It takes a Java (bytecode) program as input and computes the region type of the given method in the program.

The arXiv version of the above paper is availale [here](https://arxiv.org/abs/2209.02147).

## Running the Experiments

### Test Cases
There are a few examples including those from the paper in the [testcases.paperexamples](src/test/java/testcases/paperexamples/) package in the test module. To play with them, one can run the main method in [regiontypeinference.Main](src/main/java/regiontypeinference/Main.java) in the main module.

To display more information about the analysis, one can set the following environment variables to be `true`:
- `SHOW_TABLE` &mdash; to print the table of abstract transformations at each iteration of the fix-point procedure,
- `DEBUGGING` &mdash; to print the abstract transformation at each node of the control flow graph of the analyzed method.

### Running with Docker

Supposing the repository has been cloned to the current directory, a Docker container can be built with:

```
cd AbstractTransformation
docker build . -t abstracttransformation
```

The artifact can then be run as follows:

```
docker run abstracttransformation
```

This will start the analysis for the examples from the paper and output the results.

Environment variables can be set like so:
```
docker run -e SHOW_TABLE=true -e DEBUGGING=true abstracttransformation
```

### Building with Gradle

The artifact can also be built using [Gradle](https://gradle.org/). A configuration file [build.gradle](build.gradle) is provided in the repository. For example, in an IDE (e.g. [IntelliJ IDEA](https://www.jetbrains.com/idea/)), one can setup a project for the artifact by opening the build.gradle file as a project.

### Example and Analysis Result
TODO

## Structure of the Source Code

### Overall Structure
The [src](src) folder consists of a [main](src/main) module for the development of the tool and a [test](src/test) module containing five test cases. The main module is structured as follows:
* [main/java](src/main/java)
  * [mockup](src/main/java/mockup): mock code of some library methods (not needed for the included test cases)
  * [ourlib](src/main/java/ourlib): our library to support mock code and test cases
  * [regiontypeinference](src/main/java/regiontypeinference): implementation of the region type inference algorithm
    * [interproc](src/main/java/regiontypeinference/interproc): an interprocedural analysis to infer the type of each method of the program
    * [intraproc](src/main/java/regiontypeinference/intraproc): a forward flow analysis to compute an abstract transformation from the control flow graph of the method
    * [policy](src/main/java/regiontypeinference/policy): policies specifying the default types of intrinsic methods
    * [region](src/main/java/regiontypeinference/region): various regions representing properties of program values
    * [transformation](src/main/java/regiontypeinference/transformation): development of abstract transformation
    * [Main.java](src/main/java/regiontypeinference/Main.java): main method to run the tool with the [test cases](src/test/java/testcases/paperexamples)
    * [MockInfo.java](src/main/java/regiontypeinference/MockInfo.java): information about which classes have mock code
    * [TA.java](src/main/java/regiontypeinference/TA.java): wrap-up of the tool, including Soot environment configuration

### Link to the Paper
The algorithm to infer region types for Featherweight Java programs introduced in the paper has been fully implemented.

TODO

Among the others, abstract transformations and their operations are implemented in the
[regiontypeinference.transformation](src/main/java/regiontypeinference/transformation/)
package, and the core of the type inference algorithm in the
[regiontypeinference.intraproc.TransformationAnalysis](src/main/java/regiontypeinference/intraproc/TransformationAnalysis.java)
class.

## Previous Versions
- [GuideForceJava](https://github.com/cj-xu/GuideForceJava), based on our [PPDP'21](https://dl.acm.org/doi/10.1145/3479394.3479413) paper.
- [TSA](https://github.com/ezal/TSA), developed by Zălinescu et al., based on their [APLAS'17](https://doi.org/10.1007/978-3-319-71237-6_5) paper.

## Code Contributors
- [Ulrich Schöpp](https://ulrichschoepp.de/)
- [Chuangjie Xu](https://cj-xu.github.io/)
- Eugen Zălinescu

## License
[MIT](LICENSE.md)
