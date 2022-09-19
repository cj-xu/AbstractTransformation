package regiontypeinference;

import regiontypeinference.policy.ABCPolicy;
import regiontypeinference.policy.Policy;
import soot.G;

import java.io.File;

public class Main {
    private static String classPath = "build/classes/java/test/" + File.pathSeparator +
            "build/classes/java/main/" + File.pathSeparator +
            "lib/cos.jar" + File.pathSeparator +
            "lib/j2ee.jar" + File.pathSeparator +
            "lib/java2html.jar";
    private static Policy abcPolicy = new ABCPolicy();

    public static void main(String[] args) {
        System.out.println("======================================== Test Case 1 ========================================");
        System.out.println("File location: testcases/paperexamples/RunningExample.java");
        System.out.println("The running example code is given in the body of the method foo:\n    x = y.f;\n    y = new C();\n    y.f = x;");
        System.out.println();
        // analyze the method foo()
        runTestCase("testcases.paperexamples.RunningExample", "foo");
        System.out.println("The above generated abstract transformation contains the following as discussed in the paper:");
        System.out.println("  [ x#3 := {y.f},\n" +
                "    y#4 := {<created at .(RunningExample.java:14)>},\n" +
                "    <created at .(RunningExample.java:14)>.f :> {y.f} ]");
        System.out.println("where x#3 and y#4 are copies of the variables x and y.");
        System.out.println();
        System.out.println("The method f calls the method foo and thus the following environment is fed to the running example code:");
        System.out.println("  ( x: {<created at .(RunningExample.java:8)>},\n" +
                "    y: {<created at .(RunningExample.java:6)>},\n" +
                "    <created at .(RunningExample.java:6)>.f: {<created at .(RunningExample.java:7)>} )");
        // analyze the method f()
        runTestCase("testcases.paperexamples.RunningExample", "f");
        System.out.println("From the above analysis result of the method f, we get the environment after executing the running example code:");
        System.out.println("  ( x#3: {<created at .(RunningExample.java:7)>},\n" +
                "    y#4: {<created at .(RunningExample.java:14)>},\n" +
                "    <created at .(RunningExample.java:6)>.f: {<created at .(RunningExample.java:7)>},\n" +
                "    <created at .(RunningExample.java:14)>.f: {<created at .(RunningExample.java:7)>, null} )");
        System.out.println();
        System.out.println("We obtain the same result for the running example given in Fig. 1 of the paper, if we take");
        System.out.println("    A = <created at .(RunningExample.java:6)>,\n" +
                "    B = <created at .(RunningExample.java:7)>,\n" +
                "    C = <created at .(RunningExample.java:14)>.");
        System.out.println("==================================== End of Test Case 1 =====================================");

        System.out.println();
        System.out.println();
        System.out.println("======================================== Test Case 2 ========================================");
        System.out.println("File location: testcases/paperexamples/Node.java");
        System.out.println("An example of linked lists given in Appendix B");
        System.out.println();
        // analyze the method last()
        runTestCase("testcases.paperexamples.Node", "last");
        System.out.println("Ignoring the Jimple variables, the generated abstract transformation for the method last is:\n" +
                "    [this := {this.next, this.(next,[(next, next)],next), this}]");
        System.out.println("and the return type is given by the term:\n" +
                "    {this.next, this.(next,[(next, next)],next), this}");
        System.out.println();
        // analyze the method linear()
        runTestCase("testcases.paperexamples.Test", "linear");
        System.out.println("The return type of the method linear is:\n" +
                "    {<created at .(Node.java:23)>, <created at .(Node.java:24)>}");
        System.out.println();
        // analyze the method cyclic()
        runTestCase("testcases.paperexamples.Test", "cyclic");
        System.out.println("The return type of the method cyclic is:\n" +
                "    {<created at .(Node.java:30)>}");
        System.out.println("==================================== End of Test Case 2 =====================================");

        System.out.println();
        System.out.println();
        System.out.println("======================================== Test Case 3 ========================================");
        System.out.println("File location: testcases/paperexamples/Parameter.java");
        System.out.println("A simple test case to check if parameter types are passed correctly.");
        System.out.println();
        // analyze the method f()
        runTestCase("testcases.paperexamples.Parameters", "f");
        System.out.println("The object created at line 6 is passed via the methods id and id2 and in the end returned by f.");
        System.out.println("==================================== End of Test Case 3 =====================================");

        System.out.println();
        System.out.println();
        System.out.println("======================================== Test Case 4 ========================================");
        System.out.println("File location: testcases/paperexamples/Variable.java");
        System.out.println("A simple test case to check the strong update for variables.");
        System.out.println();
        // analyze the method h()
        runTestCase("testcases.paperexamples.Variable", "h");
        System.out.println("There are multiple assignments for the variable x.\n" +
                "The return type indicates that in the end x points to the object created in line 8.");
        System.out.println("==================================== End of Test Case 4 =====================================");

        System.out.println();
        System.out.println();
        System.out.println("======================================== Test Case 5 ========================================");
        System.out.println("File location: testcases/paperexamples/Field.java");
        System.out.println("A simple test case to check the weak update for fields.");
        System.out.println();
        // analyze the method h()
        runTestCase("testcases.paperexamples.Field", "h");
        System.out.println("There are multiple assignments for the field <created at .(Field.java:18)>.f.\n" +
                "Thus it has the type {<created at .(Field.java:19)>, <created at .(Field.java:21)>} due to the weak update.");
        System.out.println("==================================== End of Test Case 5 =====================================");
    }

    private static void runTestCase(String className, String methodName) {
        G.reset();
        TA ta = new TA(classPath, className);
        ta.run(abcPolicy, 1, methodName);
    }
}
