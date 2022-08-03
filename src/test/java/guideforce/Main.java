package guideforce;

import guideforce.policy.ABCPolicy;
import guideforce.policy.Policy;
import soot.G;

import java.io.File;

public class Main {
    private static String classPath = "build/classes/java/test/" + File.pathSeparator +
            "build/classes/java/main/" + File.pathSeparator +
            "lib/cos.jar" + File.pathSeparator +
            "lib/j2ee.jar" + File.pathSeparator +
            "lib/java2html.jar";

    private static String className = "testcases.paperexamples.Test";
    private static String methodName = "linear";
    private static Policy abcPolicy = new ABCPolicy();

    public static void main(String[] args) {
        G.reset();
        TA ta = new TA(classPath, className);
        ta.run(abcPolicy, 1, methodName);
    }
}
