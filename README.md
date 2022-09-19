# Inferring Region Types via Abstract Transformations

This is a prototype implementation of the type inference algorithm introduced in the paper

> U. Schöpp and C. Xu. **Inferring Region Types via an Abstract Notion of Environment Transformation**. To appear at APLAS'22.

based on the [Soot](http://soot-oss.github.io/soot/) framework. It takes a Java (bytecode) program as input and computes the region type of the given method in the program.

The arXiv version of the above paper is available [here](https://arxiv.org/abs/2209.02147).

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

The artifact can also be built using [Gradle](https://gradle.org/). A configuration file [build.gradle](build.gradle) is provided in the repository. For example, in an IDE (e.g. [IntelliJ IDEA](https://www.jetbrains.com/idea/)), one can set up a project for the artifact by opening the build.gradle file as a project.

### Interpreting the Analysis Result
The following Java code is taken from the example of linked lists given in Appendix B of the paper and available [here](src/test/java/testcases/paperexamples/Node.java).
```java
class Node {
    Node next;
    Node last() {
        TaintAPI.emitA();
        if (next == null) {
            return this;
        } else {
            return next.last();
        }
    }
}
```
Running our tool with it will print the following result:
```
Analysis result of the method <testcases.paperexamples.Node: testcases.paperexamples.Node last()>
  Transformation: [$stack1 := {this.next, this.(next,[(next, next)],next)}, $stack3 := {$stack3, this.next, this.(next,[(next, next)],next)}, $stack2 := {this.next, this.(next,[(next, next)],next), $stack2}, this := {this.next, this.(next,[(next, next)],next), this}]
  Transformation ignoring the Jimple variables: [this := {this.next, this.(next,[(next, next)],next), this}]
  Type term: {this.next, this.(next,[(next, next)],next), this}
  Input environment: ()
  Output environment: ()
  Output field table: ()
  Output type: ⊥
```
We explain how to interpret the above analysis result:
* `Transformation` &mdash; the abstract transformation of the method
* `Transformation ignoring the Jimple variables` &mdash; The abstract transformation may contain additional variables due to the translation of Java Bytecode to Jimple programs in the Soot framework. We remove these variables in the result for the sake of readability.
* `Type term` &mdash; the term to be instantiated with a given environment into a output type for the method
* `Input environment` &mdash; typing environment to execute the method, by default set to be the empty environment `()` where all variables and fields have the bottom type `⊥`
* `Output environment` &mdash; typing environment after execute the method with the input environment
* `Output field table` &mdash; field table after execute the method with the input environment
* `Output type` &mdash; output type of the method, obtained by instantiating the type term with the output environment and field table

The abstract transformation of `last()` is `[this := {this.next, this.(next,[(next, next)],next), this}]`. It performs no update to the empty environment and thus the output type of `last()` with the empty environment is `⊥`, i.e. there is no region for the output value.

For the following method
```java
class Test {
    Node linear () {
        Node x = new Node();
        Node y = new Node();
        y.next = x;
        return y.last();
    }
    ...
}
```
the analysis result is
```
Analysis result of the method <testcases.paperexamples.Test: testcases.paperexamples.Node linear()>
  Transformation: [$stack4 := {<created at .(Node.java:24)>}, this := {<created at .(Node.java:24)>, <created at .(Node.java:24)>.(next,[(next, next)],next), <created at .(Node.java:24)>.next}, $stack2 := {$stack2, <created at .(Node.java:24)>.(next,[(next, next)],next), <created at .(Node.java:24)>.next}, $stack5 := {<created at .(Node.java:24)>, <created at .(Node.java:24)>.(next,[(next, next)],next), <created at .(Node.java:24)>.next}, $stack3 := {$stack3, <created at .(Node.java:24)>.(next,[(next, next)],next), <created at .(Node.java:24)>.next}, y := {<created at .(Node.java:24)>}, $stack3 := {<created at .(Node.java:23)>}, <created at .(Node.java:24)>.next :> {<created at .(Node.java:23)>}, this := {<created at .(Node.java:24)>}, x := {<created at .(Node.java:23)>}, $stack1 := {<created at .(Node.java:24)>.(next,[(next, next)],next), <created at .(Node.java:24)>.next, $stack1}]
  Transformation ignoring the Jimple variables: [this := {<created at .(Node.java:24)>, <created at .(Node.java:24)>.(next,[(next, next)],next), <created at .(Node.java:24)>.next}, y := {<created at .(Node.java:24)>}, <created at .(Node.java:24)>.next :> {<created at .(Node.java:23)>}, this := {<created at .(Node.java:24)>}, x := {<created at .(Node.java:23)>}]
  Type term: {<created at .(Node.java:24)>, <created at .(Node.java:24)>.(next,[(next, next)],next), <created at .(Node.java:24)>.next}
  Input environment: ()
  Output environment: ($stack4: {<created at .(Node.java:24)>}, this: {<created at .(Node.java:24)>, <created at .(Node.java:23)>}, $stack2: {<created at .(Node.java:23)>}, x: {<created at .(Node.java:23)>}, $stack5: {<created at .(Node.java:24)>, <created at .(Node.java:23)>}, $stack3: {<created at .(Node.java:23)>}, y: {<created at .(Node.java:24)>}, $stack3: {<created at .(Node.java:23)>}, $stack1: {<created at .(Node.java:23)>}, this: {<created at .(Node.java:24)>})
  Output field table: (<created at .(Node.java:24)>.next: {<created at .(Node.java:23)>}, <created at .(Node.java:23)>.next: {null})
  Output type: {<created at .(Node.java:24)>, <created at .(Node.java:23)>}
```
where the output type `{<created at .(Node.java:24)>, <created at .(Node.java:23)>}` means that the returned value of the method `linear()` is created either in line 24 or line 23 of [Node.java](src/test/java/testcases/paperexamples/Node.java).

## Structure of the Source Code

### Overall Structure
The [src](src) folder consists of a [main](src/main) module for the development of the tool and a [test](src/test) module containing five test cases. The main module is structured as follows:
* [main/java](src/main/java)
  * [mockup](src/main/java/mockup): mock code of some library methods (not needed for the included test cases)
  * [ourlib](src/main/java/ourlib): our library to support mock code and test cases
  * [regiontypeinference](src/main/java/regiontypeinference): implementation of the region type inference algorithm
    * [interproc](src/main/java/regiontypeinference/interproc): an [interprocedural analysis](src/main/java/regiontypeinference/interproc/InterProcTransAnalysis.java) to infer the type of each method of the program
    * [intraproc](src/main/java/regiontypeinference/intraproc): a [forward flow analysis](src/main/java/regiontypeinference/intraproc/TransformationAnalysis.java) to compute an abstract transformation from the control flow graph of the method
    * [policy](src/main/java/regiontypeinference/policy): policies specifying the default types of intrinsic methods
    * [region](src/main/java/regiontypeinference/region): various regions representing properties of program values
    * [transformation](src/main/java/regiontypeinference/transformation): development of abstract transformations
    * [Main.java](src/main/java/regiontypeinference/Main.java): main method to run the tool with the [test cases](src/test/java/testcases/paperexamples)
    * [MockInfo.java](src/main/java/regiontypeinference/MockInfo.java): information about which classes have mock code
    * [TA.java](src/main/java/regiontypeinference/TA.java): wrap-up of the tool, including Soot environment configuration

### Linking the Results of the Paper
The algorithm to infer region types for Featherweight Java programs introduced in the paper has been fully implemented. Here is a mapping from the [paper](https://arxiv.org/abs/2209.02147) to the artifact:

* **Section 2. Background**
  * Access graphs &mdash; [regiontypeinference.transformation.FieldGraph](src/main/java/regiontypeinference/transformation/FieldGraph.java)
* **Section 3. A Theory of Abstract Transformations**
  * Running example (Fig. 1) &mdash; [testcases.paperexamples.RunningExample](src/test/java/testcases/paperexamples/RunningExample.java)  
    One can run e.g. the [main](src/main/java/regiontypeinference/Main.java) method to get the analysis result of the running example.
  * Term &mdash; [regiontypeinference.transformation.Term](src/main/java/regiontypeinference/transformation/Term.java)  
    A term is a set of [atoms](src/main/java/regiontypeinference/transformation/Atom.java) (e.g. [VariableAtom](src/main/java/regiontypeinference/transformation/VariableAtom.java), [VariableFieldAtom](src/main/java/regiontypeinference/transformation/VariableFieldAtom.java), [RegionAtom](src/main/java/regiontypeinference/transformation/RegionAtom.java) and [RegionFieldAtom](src/main/java/regiontypeinference/transformation/RegionFieldAtom.java)).
  * Abstract transformation &mdash; [regiontypeinference.transformation.Transformation](src/main/java/regiontypeinference/transformation/Transformation.java)  
    An abstract transformation is a mapping from [keys](src/main/java/regiontypeinference/transformation/Key.java) to [terms](src/main/java/regiontypeinference/transformation/Term.java), containing assignments (i.e. elements from [VariableAtom](src/main/java/regiontypeinference/transformation/VariableAtom.java)) and constraints (i.e. elements from [VariableFieldAtom](src/main/java/regiontypeinference/transformation/VariableFieldAtom.java) or [RegionFieldAtom](src/main/java/regiontypeinference/transformation/RegionFieldAtom.java)).
* **Section 4. Type Inference via Abstract Transformations**
  * Region &mdash; [regiontypeinference.region](src/main/java/regiontypeinference/region)  
    In particular, the region $\mathsf{Null}$ is implemented as a [SpecialRegion](src/main/java/regiontypeinference/region/SpecialRegion.java) and $\mathsf{CreatedAt}$ as [AllocationSiteRegion](src/main/java/regiontypeinference/region/AllocationSiteRegion.java)
  * Abstract method table &mdash; [regiontypeinference.interproc.AbstractMethodTable](src/main/java/regiontypeinference/interproc/AbstractMethodTable.java)  
    An abstract method table assigns an abstract transformation and a term ([TransAndTerm](src/main/java/regiontypeinference/interproc/TransAndTerm.java)) to each method.
  * $[[-]]$ &mdash; [regiontypeinference.intraproc.TransformationAnalysis](src/main/java/regiontypeinference/intraproc/TransformationAnalysis.java)  
    The core of the type inference algorithm is the $[[-]]$ function that computes an abstract transformation and a type term for the given Featherweight Java expression. It becomes a forward flow analysis for the control flow graphs of the Java program. In particular, the [TransformationAnalysis](src/main/java/regiontypeinference/intraproc/TransformationAnalysis.java) has a `flowThrough` method that computes an abstract transformation for each node in the control flow graph and then concatenates it with the one generated from the previous nodes.
  * Computing the abstract method table $T$ &mdash; [regiontypeinference.interproc.InterProcTransAnalysis](AbstractTransformation/src/main/java/regiontypeinference/interproc/InterProcTransAnalysis.java)  
    The fixed point procedure that computes an abstract transformation for each method in the program is implemented as an [interprocedural analysis](AbstractTransformation/src/main/java/regiontypeinference/interproc/InterProcTransAnalysis.java) that uses the above [TransformationAnalysis](src/main/java/regiontypeinference/intraproc/TransformationAnalysis.java).
* **Appendix A. Composition and Join of Abstract Transformations**
  * Operations on [terms](src/main/java/regiontypeinference/transformation/Term.java) and [abstract transformations](src/main/java/regiontypeinference/transformation/Transformation.java) are implemented in the corresponding classes.
* **Appendix B. An Example of Inferring Region Types**
  * The example of linked lists is implemented in [testcases.paperexamples.Node](src/test/java/testcases/paperexamples/Node.java).  
    When running the tool with this example, one can set the environment variable `SHOW_TABLE` to `true` to print the abstract transformations of the methods in each iteration.

## Reusing and Extending the Artifact

### Working with Other Regions or Types
Our development of abstract transformations is independent of our selection of region types (as discussed in Section 3 of the paper). Therefore, to adapt the algorithm to infer other types, one only needs to
* Add new (region) types by implementing the [Region](src/main/java/regiontypeinference/region/Region.java) interface
* Specify the types of intrinsic methods in a new policy by implementing the [Policy](src/main/java/regiontypeinference/policy/Policy.java) interface
* Implement mock code for the library with the `@Replaces` annotation in the [mockup](src/main/java/mockup) package if needed

### Supporting More Features of Java
The current implementation covers only the core features of Java. To support others such as arrays, strings and dynamic invocation, we will implement the methods for the corresponding Jimple expressions or values in the [TransformationAnalysis](src/main/java/regiontypeinference/intraproc/TransformationAnalysis.java) class. For example, we need to implement the `caseDynamicInvokeExpr` method to support analysis of dynamic invocation of methods. To support exception handling, we also need to extend the `flowThrough` method to analyze also the code of the exception handlers (see [here](https://github.com/cj-xu/GuideForceJava/blob/800ad3af6c88282adeb4105338190e15f530a03f/src/main/java/guideforce/intraproc/FinitaryEffectAnalysis.java#L127) for an example).

### Extension with Trace Effects
We plan to extend the inference algorithm to a region-sensitive trace property analysis. As discussed in Section 5 of the paper, we need to implement some form of formal expressions to summarize the behaviour of the program. Then we need to extend [TransformationAnalysis](src/main/java/regiontypeinference/intraproc/TransformationAnalysis.java) to compute also a formal expression for the control flow graph of the given method as well as [InterProcTransAnalysis](src/main/java/regiontypeinference/interproc/InterProcTransAnalysis.java) to compute the method table which now contains also a formal expression for each method.

## Previous Versions
- [GuideForceJava](https://github.com/cj-xu/GuideForceJava), based on our [PPDP'21](https://dl.acm.org/doi/10.1145/3479394.3479413) paper.
- [TSA](https://github.com/ezal/TSA), developed by Zălinescu et al., based on their [APLAS'17](https://doi.org/10.1007/978-3-319-71237-6_5) paper.

## Code Contributors
- [Ulrich Schöpp](https://ulrichschoepp.de/)
- [Chuangjie Xu](https://cj-xu.github.io/)
- Eugen Zălinescu

## License
[MIT](LICENSE.md)
