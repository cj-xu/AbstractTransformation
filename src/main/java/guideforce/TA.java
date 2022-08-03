package guideforce;

import guideforce.interproc.InterProcTransAnalysis;
import guideforce.policy.Policy;
import soot.*;
import soot.options.Options;
import java.util.Arrays;

public class TA {

    private static final String[] library = {
            "jdk.*", "ourlib.nonapp.*", "com.oreilly.servlet.*", "javax.servlet.http.*"
    };

    private static final int MAX_ITERATIONS = 40;

    private final SootClass mainApplicationClass;

    TA(String sootClassPath, String mainApplicationClassName, String... appClasses) {
        this.mainApplicationClass = setupSoot(sootClassPath, mainApplicationClassName, appClasses);
    }

    InterProcTransAnalysis run(Policy policy, int kCFA, String methodNameOrSubSignature) {
        SootMethod method = getMethodByNameOrSubSignature(methodNameOrSubSignature);

        InterProcTransAnalysis ana = new InterProcTransAnalysis(policy, kCFA, method);
        boolean success = ana.analyze(MAX_ITERATIONS);
        return success ? ana : null;
    }

    private SootClass setupSoot(String sootClassPath, String entryPointClass, String... appClasses) {

        // We set up various soot options:
        Options.v().set_output_format(Options.output_format_jimple);
        Options.v().set_validate(true);
        Options.v().set_app(true);

        // Retain line numbers and use the original Java variable names in Jimple code
        Options.v().set_keep_line_number(true);
        Options.v().setPhaseOption("jb", "use-original-names:true");


        // We set the Soot class path.
        Options.v().set_soot_classpath(sootClassPath);
        Options.v().set_prepend_classpath(true);

        // Exclude library classes
        Options.v().set_exclude(Arrays.asList(library));

        // Load the main class
        SootClass c = Scene.v().loadClassAndSupport(entryPointClass);
        c.setApplicationClass();

        // Load all mock classes
        // addMockClasses();

        // Make the given classes application classes
        for (String cName : appClasses) {
            Scene.v().getSootClass(cName).setApplicationClass();
        }

        // Complete class loading
        Scene.v().loadNecessaryClasses();

        return c;
    }

    private SootMethod getMethodByNameOrSubSignature(String nameOrSubSignature) {
        try {
            // Generally, we write just the name, not the sub-signature.
            // However, when the method is ambiguous, we write the signature.
            return mainApplicationClass.getMethodByName(nameOrSubSignature);
        } catch (RuntimeException e) {
            return mainApplicationClass.getMethod(nameOrSubSignature);
        }
    }
}
