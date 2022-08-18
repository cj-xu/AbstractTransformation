package regiontypeinference.interproc;

import regiontypeinference.intraproc.Environment;
import regiontypeinference.intraproc.TransformationAnalysis;
import regiontypeinference.policy.Policy;
import regiontypeinference.transformation.Term;
import regiontypeinference.transformation.Transformation;
import regiontypeinference.region.Regions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soot.Body;
import soot.SootMethod;
import soot.SootMethodRef;
import soot.toolkits.graph.BriefUnitGraph;


public class InterProcTransAnalysis {

    private static boolean SHOW_TABLE = Boolean.valueOf(System.getenv().getOrDefault("SHOW_TABLE", "false"));
    private AbstractMethodTable table;
    private SootMethodRef entryPointRef;

    private final Logger logger = LoggerFactory.getLogger(InterProcTransAnalysis.class);

    public InterProcTransAnalysis(Policy policy, int maxContextDepth, SootMethod entryPoint) {
        table = new AbstractMethodTable(policy, maxContextDepth, entryPoint);
        entryPointRef = entryPoint.makeRef();
        table.ensurePresent(entryPointRef);
    }

    public boolean analyze(int maximumIteration) {
        int iteration = 0;
        while (true) {
            AbstractMethodTable old = new AbstractMethodTable(table);
            logger.trace("======== Iteration: " + iteration + "\n");
            logger.trace("At the beginning of iteration, old table: \n" + old);

            for (SootMethodRef m : old.keySet()) {
                Body body = table.getBody(m);
                if (body != null) {
                    BriefUnitGraph graph = new BriefUnitGraph(body);
                    new TransformationAnalysis(graph, table, m);
                }
                // Methods without a body already have their default transformations in the table.
            }
            if (SHOW_TABLE) {
                System.out.println("Table at iteration " + iteration + ":");
                System.out.println(table);
            }
            // Reach the fixed point
            if (old.equals(table)) {
                if (SHOW_TABLE) {
                    System.out.println("Resulting table:");
                    System.out.println(table);
                }

                System.out.println("Analysis result of the method " + entryPointRef);

                TransAndTerm tt = table.get(entryPointRef);
                Transformation trans = tt.getTrans();
                Term term = tt.getTerm();
                Transformation.EnvironmentAndFieldTable envft = trans.instantiate();
                Environment env = envft.getEnvironment();
                FieldTable ft = envft.getFieldTable();
                Regions r = tt.getTerm().instantiate(env, ft);
                //if (! r.toSet().isEmpty()) {
                System.out.println("  Transformation: " + trans);
                System.out.println("  Type term: " + term);
                System.out.println("  Environment: " + env);
                System.out.println("  Field table: " + ft);
                System.out.println("  Type: " + r);
                //}
                return true;
            }
            if (iteration++ >= maximumIteration) {
                System.out.println("Failed: the computation doesn't converge within " + maximumIteration + "iterations.");
                return false;
            }
        }
    }

    public AbstractMethodTable getTable() {
        return table;
    }
}
