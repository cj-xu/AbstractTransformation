package regiontypeinference.transformation;

import regiontypeinference.interproc.FieldTable;
import regiontypeinference.intraproc.Environment;
import regiontypeinference.region.AllocationSiteRegion;
import regiontypeinference.region.Region;
import regiontypeinference.region.SpecialRegion;
import regiontypeinference.region.Regions;
import soot.Local;
import soot.SootClass;
import soot.SootField;

import java.util.*;

// TODO: check the operations with bottom

public class Transformation {
    boolean bottom;
    private Map<Key, Term> assigns;

    public Transformation(Map<Key, Term> map) {
        this.assigns = map;
        bottom = map == null;
    }

    public Transformation(Transformation other) {
        this.assigns = other.assigns;
        this.bottom = other.bottom;
    }

    public static Transformation bottom(){
        return new Transformation((Map) null);
    }

    public static Transformation identity(){
        return new Transformation(new HashMap());
    }

    public static Transformation singleton(Key key, Term term) {
        Map map = new HashMap();
        map.put(key, term);
        return new Transformation(map);
    }

    public boolean containsKey(Key key) {
        if (bottom) {
            return true;
        }
        return assigns.containsKey(key);
    }

    public Term get(Key key) {
        if (bottom) {
            return new Term();
        }
        return assigns.getOrDefault(key, new Term());
    }

    public boolean isBottom() {
        return bottom;
    }

    /**
     * Concatenate with the transformation next on the right.
     * @param next
     * @return      the concatenated transformation
     */
    public Transformation concat(Transformation next) {
        // TODO: concatenation with bottom?
        if (bottom) {
            return new Transformation(next);
        }
        Map<Key, Term> result = new HashMap();
        for (Map.Entry<Key, Term> entry : assigns.entrySet()) {
            Key key = entry.getKey();
            Term updatedValue = entry.getValue().substitute(next);
            if (key instanceof VariableAtom) {
                // case x
                result.put(key, updatedValue);
            } else if (key instanceof VariableFieldAtom) {
                // cases x.G
                VariableAtom va = new VariableAtom(((VariableFieldAtom) key).getVar());
                FieldGraph graph = ((VariableFieldAtom) key).getGraph();
                Term term = next.get(va);
                for (Atom a : term.getAtoms()) {
                    Key newKey = (Key) a.concat(graph);
                    Term value = updatedValue.join(result.getOrDefault(newKey, new Term()));
                    result.put(newKey, value);
                }
            } else if (key instanceof RegionFieldAtom) {
                // case r.G
                Term value = updatedValue.join(result.getOrDefault(key, new Term()));
                result.put(key, value);
            }
        }
        for (Map.Entry<Key, Term> entry : next.assigns.entrySet()) {
            Key key = entry.getKey();
            Term value = entry.getValue();
            if (result.containsKey(key)) {
                if (! (key instanceof VariableAtom)) {
                    Term joinedValue = result.get(key).join(value);
                    result.put(key, joinedValue);
                }
            } else {
                result.put(key, value);
            }
        }
        return createAfterCleanup(result);
    }

    public Transformation join(Transformation other) {

        //System.out.println("  Joining " + this + " and " + other);

        if (this.bottom) {
            return new Transformation(other);
        }
        if (other.bottom) {
            return new Transformation(this);
        }
        Map<Key, Term> result = new HashMap();
        for (Map.Entry<Key, Term> entry : this.assigns.entrySet()) {
            Key key = entry.getKey();
            if (other.containsKey(key)) {
                Term term = entry.getValue().join(other.get(key));
                result.put(key, term);
            } else {
                if (key instanceof VariableAtom) {
                    Term term = entry.getValue().join(new Term((Atom) key));
                    result.put(key, term);
                } else {
                    Term term = entry.getValue();
                    result.put(key, term);
                }
            }
        }
        for (Map.Entry<Key, Term> entry : other.assigns.entrySet()) {
            Key key = entry.getKey();
            if (!this.assigns.containsKey(key)) {
                if (key instanceof VariableAtom) {
                    Term term = entry.getValue().join(new Term((Atom) key));
                    result.put(key, term);
                } else {
                    Term term = entry.getValue();
                    result.put(key, term);
                }
            }
        }
        return createAfterCleanup(result);
    }

    public static Transformation createAfterCleanup(Map<Key, Term> map) {
        Map<Key, Term> result = new HashMap(map);
        for (Map.Entry<Key, Term> entry : map.entrySet()) {
            Key key = entry.getKey();
            Term rhs = entry.getValue();
            if (key instanceof VariableAtom) {
                Term lhs = new Term ((Atom) key);
                if (lhs.equals(rhs)) {
                    result.remove(key);
                }
            } else {
                if (rhs.getAtoms().isEmpty()) {
                    result.remove(key);
                }
            }
        }
        return new Transformation(result);
    }

    public EnvironmentAndFieldTable instantiate() {
        return instantiate(new Environment(), new FieldTable());
    }

    public EnvironmentAndFieldTable instantiate(Environment env, FieldTable ft) {
        Environment oldEnv = new Environment(env);
        FieldTable oldFT = new FieldTable(ft);
        boolean stable = false;
        while (!stable) {
            Environment newEnv = envInstantiate(env, oldFT);
            FieldTable newFT = fieldInstantiate(env, oldFT);
            if (oldEnv.equals(newEnv) && oldFT.equals(newFT)) {
                stable = true;
            } else {
                oldEnv = new Environment(newEnv);
                oldFT = new FieldTable(newFT);
            }
        }
        return new EnvironmentAndFieldTable(oldEnv, oldFT);
    }

    private Environment envInstantiate(Environment env, FieldTable ft) {
        Environment updated = new Environment();
        for (Map.Entry<Key, Term> entry : assigns.entrySet()) {
            Key key = entry.getKey();
            if (key instanceof VariableAtom) {
                Local var = ((VariableAtom) key).getVar();
                Regions rs = entry.getValue().instantiate(env, ft);
                updated.put(var, rs);
            }
        }
        for (Local var : env.keySet()) {
            if (!updated.containsKey(var)) {
                updated.put(var, env.get(var));
            }
        }
        return updated;
    }

    private FieldTable fieldInstantiate(Environment env, FieldTable ft) {
        FieldTable updated = new FieldTable();
        Set<FieldTable.Key> fields = getFields(getRegions(env, ft));
        fields.addAll(ft.keySet());
        for (FieldTable.Key key : fields){
            Regions regions = Regions.fromSet(new HashSet());
            if (ft.containsKey(key)) {
                regions = regions.join(ft.get(key));
            }
            for (Map.Entry<Key, Term> entry : assigns.entrySet()) {
                if (entry.getKey() instanceof RegionFieldAtom) {
                    RegionFieldAtom rfa = (RegionFieldAtom) entry.getKey();
                    if (rfa.reachableFields(ft).contains(key)) {
                        regions = regions.join(entry.getValue().instantiate(env,ft));
                    }
                } else if (entry.getKey() instanceof VariableFieldAtom) {
                    VariableFieldAtom vfa = (VariableFieldAtom) entry.getKey();
                    Local var = vfa.getVar();
                    if (env.containsKey(var)) {
                        for (Region r : env.get(var).toSet()) {
                            RegionFieldAtom rfa = new RegionFieldAtom(r, vfa.getGraph());
                            if (rfa.reachableFields(ft).contains(key)) {
                                regions = regions.join(entry.getValue().instantiate(env,ft));
                            }
                        }
                    }
                }
            }
            if (regions.toSet().isEmpty()) {
                regions = Regions.singleton(SpecialRegion.NULL_REGION);
            }
            updated.put(key, regions);
        }
        return updated;
    }

    private static Set<FieldTable.Key> getFields(Set<Region> regions) {
        Set<FieldTable.Key> fields = new HashSet();
        for (Region r : regions) {
            if (r instanceof AllocationSiteRegion) {
                SootClass c = ((AllocationSiteRegion) r).getSootClass();
                for (SootField f : c.getFields()) {
                    fields.add(new FieldTable.Key(r, f));
                }
            }
        }
        return fields;
    }

    private Set<Region> getRegions(Environment env, FieldTable ft) {
        Set<Region> regions = new HashSet();
        for (Key k : assigns.keySet()) {
            if(k instanceof RegionFieldAtom) {
                regions.add(((RegionFieldAtom) k).getRegion());
            }
        }
        for (Term t : assigns.values()) {
            for (Atom a : t.getAtoms()) {
                if (a instanceof RegionAtom) {
                    regions.add(((RegionAtom) a).getRegion());
                } else if (a instanceof RegionFieldAtom) {
                    regions.add(((RegionFieldAtom) a).getRegion());
                }
            }
        }
        for (Regions rs : env.values()) {
            regions.addAll(rs.toSet());
        }
        for (Map.Entry<FieldTable.Key, Regions> entry : ft.entrySet()) {
            regions.add(entry.getKey().getRegion());
            regions.addAll(entry.getValue().toSet());
        }
        return regions;
    }

    // TODO: why "_ -> \bot" is invalid?
//    public boolean hasInvalidEntry() {
//        return assigns.containsValue(new Term());
//    }

    public Transformation copy() {
        return new Transformation(new HashMap(assigns));
    }

    public Transformation removeLocals() {
        Map<Key, Term> map = new HashMap<>();
        for(Key key : assigns.keySet()) {
            if (! (key instanceof VariableAtom)) {
                map.put(key, assigns.get(key));
            }
        }
        return new Transformation(map);
    }

    public Transformation removeLocals(Set<Local> toKeep) {
        Map<Key, Term> map = new HashMap<>();
        for(Key key : assigns.keySet()) {
            if ((key instanceof VariableAtom)) {
                if (toKeep.contains(((VariableAtom) key).getVar())) {
                    map.put(key, assigns.get(key));
                }
            } else {
                map.put(key, assigns.get(key));
            }
        }
        return new Transformation(map);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transformation that = (Transformation) o;
        return bottom == that.bottom && Objects.equals(assigns, that.assigns);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bottom, assigns);
    }

    @Override
    public String toString() {
        if (assigns != null) {
            StringBuffer sb = new StringBuffer();
            sb.append("{");
            boolean first = true;
            for(Key key : assigns.keySet()) {
                if (first) {
                    first = false;
                } else {
                    sb.append(", ");
                }
                sb.append(key.toString());
                if ((key instanceof VariableAtom)) {
                    sb.append(" := ");
                } else {
                    sb.append(" :> ");
                }
                sb.append(assigns.get(key).toString());
            }
            sb.append("}");
            return sb.toString();
        } else {
            char bot = '\u22A5';
            return bot + "";
        }
    }

    public class EnvironmentAndFieldTable {
        private Environment env;
        private FieldTable ft;

        EnvironmentAndFieldTable (Environment env, FieldTable ft) {
            this.env = env;
            this.ft = ft;
        }

        public Environment getEnvironment() {
            return env;
        }

        public FieldTable getFieldTable() {
            return ft;
        }
    }
}
