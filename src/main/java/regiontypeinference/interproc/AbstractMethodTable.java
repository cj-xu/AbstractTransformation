package regiontypeinference.interproc;

import regiontypeinference.MockInfo;
import regiontypeinference.policy.Intrinsic;
import regiontypeinference.policy.Policy;
import regiontypeinference.region.Region;
import regiontypeinference.region.SpecialRegion;
import regiontypeinference.transformation.Term;
import regiontypeinference.transformation.Transformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soot.*;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class AbstractMethodTable extends HashMap<SootMethodRef, TransAndTerm> {

    private final Policy policy;
    private final TypePool typePool;
    private final CFGCache cfgCache;
    private final MockInfo typeMap;
    private final Logger logger = LoggerFactory.getLogger(AbstractMethodTable.class);

    AbstractMethodTable(Policy policy, int maxContextDepth, SootMethod entryPoint) {
        super();
        this.policy = policy;
        this.typeMap = new MockInfo();
        this.cfgCache = new CFGCache(this.typeMap);
        this.typePool = new TypePool(this.cfgCache, maxContextDepth, entryPoint.makeRef());
    }

    AbstractMethodTable(AbstractMethodTable other) {
        super(other);
        this.policy = other.policy;
        this.typeMap = other.typeMap;
        this.cfgCache = other.cfgCache;
        this.typePool = other.typePool;
    }

    public TypePool getTypePool() {
        return typePool;
    }

    private enum MethodKind {
        APPLICATION_METHOD,
        INTRINSIC,
        EMPTY_DEFAULT_CONSTRUCTOR,
        MOCKED_LIBRARY_METHOD, // Library method for which we have a body (because there is a mock class)
        OPAQUE_LIBRARY_METHOD
    }

    private MethodKind getKind(SootMethodRef m) {
        // Intrinsic methods are specified by the policy
        if (policy.getIntrinsicMethod(m) != null) {
            return MethodKind.INTRINSIC;
        }
        // library methods are opaque
        if(m.getDeclaringClass().isLibraryClass()) {
            // Default constructor
            SootClass objectClass = Scene.v().getSootClass("java.lang.Object");
            SootMethodRef objectConstructor = Scene.v().makeConstructorRef(objectClass,
                    Collections.emptyList());
            if (m.getSignature().equals(objectConstructor.getSignature())) {
                return MethodKind.EMPTY_DEFAULT_CONSTRUCTOR;
            }
            // Mocked methods
            SootMethodRef mockedMethod = typeMap.mockMethodRef(m);
            if (m != mockedMethod && cfgCache.getOrCreate(mockedMethod) != null) {
                return MethodKind.MOCKED_LIBRARY_METHOD;
            }
            // Anything else is treated conservatively
            return MethodKind.OPAQUE_LIBRARY_METHOD;
        }
        // default construct without body
        if (cfgCache.getOrCreate(m) == null && m.getName().equals("<init>")) {
            return MethodKind.EMPTY_DEFAULT_CONSTRUCTOR;
        }
        // application method
        return MethodKind.APPLICATION_METHOD;
    }

    public Body getBody(SootMethodRef m) {
        MethodKind kind = getKind(m);
        if (kind == MethodKind.APPLICATION_METHOD || kind == MethodKind.MOCKED_LIBRARY_METHOD) {
            return cfgCache.getOrCreate(m);
        } else {
            return null;
        }
    }

    /**
     * Join the entry at key {@code m} with the transformation {@code tt},
     * if the table has an entry for that key. It does nothing if no key is present.
     * @param m  entry key
     * @param tt transformation to be joined
     */
    public void joinIfPresent(SootMethodRef m, TransAndTerm tt) {
        Objects.requireNonNull(m);
        Objects.requireNonNull(tt);
        this.computeIfPresent(m, (k, v) -> v.join(tt));
        // maintain the subtyping invariant
        SootClass c = m.getDeclaringClass();
        Consumer<SootClass> joinAt = mr -> {
            SootMethodRef newRef = Scene.v().makeMethodRef(mr, m.getName(), m.getParameterTypes(),
                    m.getReturnType(), m.isStatic());
            this.computeIfPresent(newRef, (k, v) -> v.join(tt));
        };
        // update entries of all classes that inherit the method
        Hierarchy h = Scene.v().getActiveHierarchy();
        Deque<SootClass> queue = new LinkedList<>(c.isInterface() ? h.getDirectImplementersOf(c) :
                h.getDirectSubclassesOf(c));
        while (!queue.isEmpty()) {
            SootClass d = queue.pop();
            if (!typePool.contains(d.getType())) {
                continue;
            }
            if (!d.declaresMethod(m.getSubSignature())) {
                joinAt.accept(d);
                queue.addAll(d.isInterface() ? h.getDirectImplementersOf(d) : h.getDirectSubclassesOf(d));
            }
        }
        // update the entry of any superclass entry to ensure invariant
        while (!c.isInterface() && c.hasSuperclass()) {
            queue.addAll(c.getInterfaces());
            c = c.getSuperclass();
            joinAt.accept(c);
        }
        queue.addAll(c.getInterfaces());
        while (!queue.isEmpty()) {
            SootClass d = queue.pop();
            joinAt.accept(d);
            queue.addAll(d.getInterfaces());
        }
    }

    /**
     * Ensure that the table has an entry for {@code m}.
     * If it does not, then the default transformation is inserted.
     * Well-formedness is maintained: an entry for each relevant subclasses is added.
     * @param m  method to ensure
     */
    public void ensurePresent(SootMethodRef m) {
        Objects.requireNonNull(m);
        if (this.containsKey(m)) {
            return;
        }
        // ensure that any possible implementation is included
        if (getKind(m) != MethodKind.OPAQUE_LIBRARY_METHOD) {
            SootMethod method = m.tryResolve();
            if (method != null) {
                m = method.makeRef();
            } else {
                logger.error("cannot resolve method " + m + " all bets are off");
            }
        }
        // add the default transformation to the method
        put(m, defaultTransAndTerm(m));
        // close under subtyping
        // closure is not needed for constructors
        if (m.getName().equals("<init>")) {
            return;
        }
        // add all subclasses of c to the table to maintain invariant
        SootClass c = m.getDeclaringClass();
        Hierarchy h = Scene.v().getActiveHierarchy();
        List<SootClass> all = new LinkedList<>();
        if (c.isInterface()) {
            all.addAll(h.getImplementersOf(c));
            all.addAll(h.getSubinterfacesOf(c));
        } else {
            all.addAll(h.getSubclassesOf(c));
        }
        for (SootClass subC : all) {
            RefType t = subC.getType();
            if (!typePool.contains(t)) {
                logger.trace("typePool does not contain " + t);
                continue;
            }
            SootMethodRef newRef = Scene.v().makeMethodRef(subC,
                    m.getName(), m.getParameterTypes(), m.getReturnType(),
                    m.isStatic());
            putIfAbsent(newRef, defaultTransAndTerm(newRef));
        }
    }

    /**
     * Returns the default transformation for the given method.
     * @param m method to be given a default transformation
     * @return  the default transformation for {@code m}
     */
    private TransAndTerm defaultTransAndTerm(SootMethodRef m) {
        // methods that will be analysed can get the bottom annotation
        Supplier<TransAndTerm> bottom = () -> {
            return new TransAndTerm(Transformation.bottom(), new Term());
        };
        // unknown methods get a conservative approximation
        Supplier<TransAndTerm> unknown = () -> {
            Region r = m.getReturnType() instanceof RefType ?
                    SpecialRegion.UNKNOWN_REGION : SpecialRegion.BASETYPE_REGION;
            return new TransAndTerm(Transformation.identity(), new Term(r));
        };
        switch (getKind(m)) {
            case INTRINSIC: {
                Intrinsic i = policy.getIntrinsicMethod(m);
                return new TransAndTerm(Transformation.identity(), new Term(i.getReturnType()));
            }
            case EMPTY_DEFAULT_CONSTRUCTOR: {
                Term t = new Term(SpecialRegion.BASETYPE_REGION);
                return new TransAndTerm(Transformation.identity(), t);
            }
            case OPAQUE_LIBRARY_METHOD: {
                return unknown.get();
            }
            case MOCKED_LIBRARY_METHOD:
            case APPLICATION_METHOD: {
                return bottom.get();
            }
        }
        throw new RuntimeException("non-exhaustive cases");
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        for (Map.Entry<SootMethodRef, TransAndTerm> entry : entrySet()) {
            buffer.append("  " + entry.getKey().getName())
                    .append(": " + entry.getValue())
                    .append("\n");
        }
        return buffer.toString();
    }
}
