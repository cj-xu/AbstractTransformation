package guideforce.intraproc;

import guideforce.interproc.*;
import guideforce.region.AllocationSiteRegion;
import guideforce.region.Region;
import guideforce.region.SpecialRegion;
import guideforce.transformation.*;
import soot.*;
import soot.jimple.*;
import soot.toolkits.graph.UnitGraph;

import java.util.*;

public class TransformationAnalysis extends ForwardFlowAnalysis<Unit, TransformationFlow> {

    private static boolean DEBUGGING = false;
    private final AbstractMethodTable table;
    private final SootMethodRef currentRef;
    private final SootMethod currentMethod;
    private final Body body;

    /**
     * Construct the analysis from a DirectedGraph representation of a Body.
     *
     * @param graph
     */
    public TransformationAnalysis(UnitGraph graph, AbstractMethodTable table, SootMethodRef currentRef) {
        super(graph);
        this.table = table;
        this.currentRef = currentRef;
        this.body = table.getBody(currentRef);
        this.currentMethod = body.getMethod();

        if (DEBUGGING) {
            System.out.println("\nAnalyzing method " + currentRef);
        }
        doAnalysis();
    }

    @Override
    protected TransformationFlow newInitialFlow() {
        return TransformationFlow.bottom();
    }

    @Override
    protected TransformationFlow entryInitialFlow() {
        return TransformationFlow.identity();
    }

    @Override
    protected void copy(TransformationFlow source, TransformationFlow dest) {
        TransformationFlow.copy(source, dest);
    }

    @Override
    protected void merge(TransformationFlow in1, TransformationFlow in2, TransformationFlow out) {
        TransformationFlow.merge(in1, in2, out);
    }

    @Override
    protected Map<Unit, TransformationFlow> flowThrough(TransformationFlow in, Unit d) {
        if (!(d instanceof Stmt))
            throw new RuntimeException("unhandled unit: " + d);

        // Compute the type of node d
        FlowThroughStmtVisitor visitor = new FlowThroughStmtVisitor();
        d.apply(visitor);
        Transformation trans = visitor.getTrans().concat(in.getTrans());

        TransformationFlow out = new TransformationFlow(trans);

        if (DEBUGGING) {
            System.out.println("Node: " + d);
            System.out.println("  in: " + in);
            System.out.println("  d: " + visitor.getTrans() + " & " + visitor.term);
            System.out.println("  out: " + out);
        }

        Map<Unit,TransformationFlow> outs = new HashMap<>();

        // out flow for each normal successor of d
        for (Unit u : graph.getSuccsOf(d)) {
            outs.put(u, out);
        }

        // If d is an exit node, then update the method table
        if(graph.getTails().contains(d)) {
            Term term = visitor.getTerm().substitute(in.getTrans());
            TransAndTerm updated = new TransAndTerm(trans, term);
            table.ensurePresent(currentRef);
            TransAndTerm cleaned = clean(updated);
            table.joinIfPresent(currentRef, cleaned);
            if (DEBUGGING) {
                System.out.println("Exit node of " + currentMethod.getName());
                System.out.println("  Transformation: " + updated);
                System.out.println("  Cleaned: " + cleaned);
            }
        }

        return outs;
    }

    private TransAndTerm clean(TransAndTerm trans) {
        Set<Local> toKeep = new HashSet<>();
        toKeep.add(body.getThisLocal());
        toKeep.addAll(body.getParameterLocals());
        return trans.clean(toKeep);
    }

    private boolean comparable(SootClass c1, SootClass c2) {
        if (c1.getName().equals("java.lang.Object") || (c2.getName().equals("java.lang.Object")))
            return true;

        FastHierarchy h = Scene.v().getOrMakeFastHierarchy();
        if (c1.isInterface() && h.getAllImplementersOfInterface(c1).contains(c2)) {
            return true;
        }
        if (c2.isInterface()) {
            return h.getAllImplementersOfInterface(c2).contains(c1);
        } else {
            return h.isSubclass(c1, c2) || h.isSubclass(c2, c1);
        }
    }

    private class FlowThroughStmtVisitor extends AbstractStmtSwitch {
        private Transformation trans;
        private Term term;

        public FlowThroughStmtVisitor() {
            trans = Transformation.identity();
            term = new Term();
        }

        public Transformation getTrans() {
            return trans;
        }

        public Term getTerm() {
            return term;
        }

        /**
         * Analysis of an invoke statement.
         * <p>
         * The relevant Jimple grammar production is:
         * <pre>
         * invokeStm ::= invoke invokeExpr
         * invokeExpr ::= specialinvoke local.m(imm_1,...,imm_n)
         *              | interfaceinvoke local.m(imm_1,...,imm_n)
         *              | virtualinvoke local.m(imm_1,...,imm_n)
         *              | staticinvoke m(imm_1,...,imm_n)
         * </pre>
         */
        @Override
        public void caseInvokeStmt(InvokeStmt stmt) {
            InvokeExpr e = stmt.getInvokeExpr();
            TypeOfValue sw = new TypeOfValue(stmt);
            e.apply(sw);
            trans = sw.getTrans();
            term = sw.getTerm();
        }

        /**
         * Analysis of an assignment statement.
         * <p>
         * The relevant Jimple grammar productions are:
         * <pre>
         *   assignStmt ::= local = rvalue;
         *                | field = imm;
         *                | local.field = imm;
         *                | local[imm] = imm;
         *       rvalue ::= concreteRef
         *                | imm
         *                | expr
         *  concreteRef ::= field
         *                | local.field
         *                | field[imm]
         * </pre>
         */
        @Override
        public void caseAssignStmt(AssignStmt stmt) {
            Value lv = stmt.getLeftOp();
            Value rv = stmt.getRightOp();

            TypeOfValue sw = new TypeOfValue(stmt);
            rv.apply(sw);

            if (lv instanceof Local) {
                // case local = rvalue
                Key key = new VariableAtom((Local) lv);
                trans = sw.trans.concat(Transformation.singleton(key, sw.term));
                term = sw.term;
            } else if (lv instanceof InstanceFieldRef) {
                // case local.field = imm
                InstanceFieldRef f = (InstanceFieldRef) lv;
                Key key = new VariableFieldAtom((Local) f.getBase(), f.getField());
                trans = Transformation.singleton(key, sw.term);
                // no need to update term and expr because the rhs is imm
            } else if (lv instanceof StaticFieldRef) {
                // case field = imm
                StaticFieldRef f = (StaticFieldRef) lv;
                Key key = new RegionFieldAtom(SpecialRegion.STATIC_REGION, f.getField());
                trans = Transformation.singleton(key, sw.term);
                // no need to update term and expr because the rhs is imm
            } else if (lv instanceof ArrayRef) {
                // case local[imm] = imm
                // not supported yet
            } else {
                assert false;
                throw new RuntimeException("unhandled lv in caseAssignStmt");
            }
        }

        /**
         * Analysis of an identity statement.
         * <p>
         * The relevant Jimple grammar productions are:
         * <pre>
         * identityStmt ::= local := @this: types;
         *                | local := @parameter_n: types;
         *                | local := @exception;
         * </pre>
         */
        @Override
        public void caseIdentityStmt(IdentityStmt stmt) {
            Value lv = stmt.getLeftOp();
            Value rv = stmt.getRightOp();

            TypeOfValue sw = new TypeOfValue(stmt);
            rv.apply(sw);

            Key key = new VariableAtom((Local) lv);
            trans = Transformation.singleton(key, sw.term);
        }

        @Override
        public void caseRetStmt(RetStmt stmt) {
            assert false;
        }

        /**
         * Analysis of a return statement.
         * <p>
         * The relevant Jimple grammar production is:
         * <pre>
         * returnStm ::= return imm
         * </pre>
         */
        @Override
        public void caseReturnStmt(ReturnStmt stmt) {
            Value v = stmt.getOp();
            if (v instanceof Local) {
                term = new Term((Local) v);
            } else if (v instanceof Constant) {
                term = new Term(SpecialRegion.BASETYPE_REGION);
            }
        }

        /**
         * Analysis of a return statement.
         * <p>
         * The relevant Jimple grammar production is:
         * <pre>
         * returnStm ::= return
         * </pre>
         */
        @Override
        public void caseReturnVoidStmt(ReturnVoidStmt stmt) {
            term = new Term(SpecialRegion.BASETYPE_REGION);
        }

        @Override
        public void caseIfStmt(IfStmt stmt) {
            // nothing to be done
        }

        @Override
        public void caseGotoStmt(GotoStmt stmt) {
            // nothing to be done
        }

        @Override
        public void caseNopStmt(NopStmt stmt) {
            // nothing to be done
        }

        /**
         * Analysis of a throw statement.
         * <p>
         * The relevant Jimple grammar production is:
         * <pre>
         * throwStm ::= throw imm
         * </pre>
         */
        @Override
        public void caseThrowStmt(ThrowStmt stmt) {
            // not supported yet
        }

        @Override
        public void caseLookupSwitchStmt(LookupSwitchStmt stmt) {
            // nothing to be done
        }

        @Override
        public void caseTableSwitchStmt(TableSwitchStmt stmt) {
            // nothing to be done
        }

        @Override
        public void defaultCase(Object obj) {
            throw new RuntimeException("Unhandled case in FlowThroughStmtVisitor.");
        }
    }

    /**
     * Visitor to compute types and effect of all possible Jimple values
     */
    class TypeOfValue extends AbstractJimpleValueSwitch {
        private final Stmt stmt; // needed for generating created_at region
        private Transformation trans;
        private Term term;

        public TypeOfValue(Stmt stmt) {
            this.stmt = stmt;
            this.trans = Transformation.identity();
            this.term = new Term();
        }

        public Transformation getTrans() {
            return trans;
        }

        public Term getTerm() {
            return term;
        }

        @Override
        public void caseArrayRef(ArrayRef v) {
            // not supported yet
        }

        /**
         * We handle only the case  (type) imm
         */
        @Override
        public void caseCastExpr(CastExpr v) {
            if (!(v.getType() instanceof RefType)) {
                throw new RuntimeException("Unhandled case of CastExpr.");
            }

            RefType toType = (RefType) v.getCastType();
            SootClass toClass = toType.getSootClass();

            // If the cast is from a non-class types, it will fail and
            // we use empty set of regions
            if (!(v.getType() instanceof RefType)) {
                // cast will fail at runtime; use empty set of regions
                return;
            }

            RefType fromType = (RefType) v.getType();
            SootClass fromClass = fromType.getSootClass();

            // If the cast is between incomparable classes, it cannot be performed and
            // we use empty set of regions
            if (!comparable(fromClass, toClass)
                    & !fromClass.toString().equals(toClass.toString())) { // class is not comparable to its mockup class
                return;
            }

            TypeOfValue sw = new TypeOfValue(stmt);
            v.getOp().apply(sw);
            this.trans = sw.trans;
            this.term = sw.term;
        }

        @Override
        public void caseInstanceOfExpr(InstanceOfExpr v) {
            // result is boolean
            term = new Term(SpecialRegion.BASETYPE_REGION);
        }

        @Override
        public void caseNewArrayExpr(NewArrayExpr v) {
            // not supported yet
        }

        @Override
        public void caseNewMultiArrayExpr(NewMultiArrayExpr v) {
            // not supported yet
        }

        @Override
        public void caseNewExpr(NewExpr v) {
            Location loc = new Location(currentMethod, stmt);
            Region region = new AllocationSiteRegion(v, new CallingContext(0), loc);
            term = new Term(region);
        }

        /**
         * Common case for all invoke expressions.
         */
        private void caseInvoke(Atom calleeAtom, InvokeExpr e) {
            SootMethodRef m = e.getMethodRef();
            table.ensurePresent(m);
            TransAndTerm tt = table.get(m);
            trans = tt.getTrans();
            term = tt.getTerm();
            List<Value> args = e.getArgs();
            List<Atom> argAtoms = new LinkedList<>();
            for (Value v : args) {
                if (v instanceof Local) {
                    argAtoms.add(new VariableAtom((Local) v));
                } else {
                    // the argument is a constant
                    argAtoms.add(new RegionAtom(SpecialRegion.BASETYPE_REGION));
                }
            }

            Body calleeBody = table.getBody(e.getMethodRef());
            if (calleeBody != null) {
                // Transformation mapping parameters to arguments
                Map<Key,Term> map = new HashMap();
                List<Local> pars = calleeBody.getParameterLocals();
                for (int i = 0; i < pars.size(); i++) {
                    Key key = new VariableAtom(pars.get(i));
                    map.put(key, new Term(argAtoms.get(i)));
                }
                // Add the mapping from thisVar to calleeTerm
                Local thisVar = calleeBody.getThisLocal();
                map.put(new VariableAtom(thisVar), new Term(calleeAtom));
                Transformation parsToArgs = Transformation.createAfterCleanup(map);
                trans = trans.concat(parsToArgs);
                term = term.substitute(parsToArgs);
                if (DEBUGGING) {
                    System.out.println("  " + m.getName() + " in the table: " + tt);
                    System.out.println("  concatenating with: " + parsToArgs);
                    System.out.println("  resulting in: " + trans + " & " + term );
                }
            }
        }

        @Override
        public void caseInterfaceInvokeExpr(InterfaceInvokeExpr v) {
            Local local = (Local) v.getBase();
            caseInvoke(new VariableAtom(local), v);
        }

        @Override
        public void caseSpecialInvokeExpr(SpecialInvokeExpr v) {
            Local local = (Local) v.getBase();
            caseInvoke(new VariableAtom(local), v);
        }

        @Override
        public void caseStaticInvokeExpr(StaticInvokeExpr v) {
            caseInvoke(new RegionAtom(SpecialRegion.STATIC_REGION), v);
        }

        @Override
        public void caseVirtualInvokeExpr(VirtualInvokeExpr v) {
            Local local = (Local) v.getBase();
            caseInvoke(new VariableAtom(local), v);
        }

        @Override
        public void caseDynamicInvokeExpr(DynamicInvokeExpr v) {
            throw new RuntimeException("caseDynamicInvokeExpr is not handled yet.");
        }

        @Override
        public void caseLengthExpr(LengthExpr v) {
            // expressions of base types are safe to ignore
            term = new Term(SpecialRegion.BASETYPE_REGION);
        }

        @Override
        public void caseNegExpr(NegExpr v) {
            // expressions of base types are safe to ignore
            term = new Term(SpecialRegion.BASETYPE_REGION);
        }

        @Override
        public void caseInstanceFieldRef(InstanceFieldRef v) {
            Local obj = (Local) v.getBase();
            SootField f = v.getField();
            term = new Term(new VariableFieldAtom(obj, f));
        }

        @Override
        public void caseLocal(Local v) {
            term = new Term(v);
        }

        @Override
        public void caseParameterRef(ParameterRef v) {
            Local var = body.getParameterLocal(v.getIndex());
            term = new Term(var);
        }

        @Override
        public void caseCaughtExceptionRef(CaughtExceptionRef v) {
            // not supported yet
        }

        @Override
        public void caseThisRef(ThisRef v) {
            Local var = body.getThisLocal();
            term = new Term(var);
        }

        @Override
        public void caseStaticFieldRef(StaticFieldRef v) {
            SootField f = v.getField();
            term = new Term(new RegionFieldAtom(SpecialRegion.STATIC_REGION, f));
        }

        @Override
        public void caseStringConstant(StringConstant v) {
            // not supported yet
        }

        @Override
        public void defaultCase(Object v) {
            // only cases for constants and binary operations are not covered
            if (v instanceof Constant || v instanceof BinopExpr) {
                term = new Term(SpecialRegion.BASETYPE_REGION);
            } else {
                assert false;
            }
        }
    }
}
