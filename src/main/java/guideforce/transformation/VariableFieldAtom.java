package guideforce.transformation;

import guideforce.interproc.FieldTable;
import guideforce.intraproc.Environment;
import guideforce.region.Region;
import guideforce.region.Regions;
import soot.Local;
import soot.SootField;

import java.util.*;

public class VariableFieldAtom implements Atom, Key {
    private Local var;
    private FieldGraph graph;

    public VariableFieldAtom(Local var, FieldGraph graph) {
        this.var = var;
        this.graph = graph;
    }

    public VariableFieldAtom(Local var, SootField field) {
        this.var = var;
        this.graph = new FieldGraph(field);
    }

    @Override
    public Regions instantiate(Environment env, FieldTable ft) {
        Regions regions = Regions.fromSet(new HashSet());
        for (Region r : env.getOrDefault(var, regions).toSet()){
            RegionFieldAtom rfa = new RegionFieldAtom(r , graph);
            regions = regions.join(rfa.instantiate(env, ft));
        }
        return regions;
    }

    @Override
    public Atom concat(FieldGraph graph) {
        return new VariableFieldAtom(var, this.graph.concat(graph));
    }

    @Override
    public Term substitute(Transformation trans) {
        Key varKey = new VariableAtom(var);
        if (trans.containsKey(varKey)) {
            return trans.get(varKey).concat(graph);
        }
        return new Term(this);
    }

    public Local getVar() {
        return var;
    }

    public FieldGraph getGraph() {
        return graph;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VariableFieldAtom that = (VariableFieldAtom) o;
        return var.equals(that.var) && graph.equals(that.graph);
    }

    @Override
    public int hashCode() {
        return Objects.hash(var, graph);
    }

    @Override
    public String toString() {
        return var.getName() + "." + graph;
    }
}
