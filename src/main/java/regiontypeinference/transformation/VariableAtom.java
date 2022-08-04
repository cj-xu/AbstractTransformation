package regiontypeinference.transformation;

import regiontypeinference.interproc.FieldTable;
import regiontypeinference.intraproc.Environment;
import regiontypeinference.region.Regions;
import soot.Local;

import java.util.HashSet;
import java.util.Objects;

public class VariableAtom implements Atom, Key {
    private Local var;

    public VariableAtom(Local var) {
        this.var = var;
    }

    @Override
    public Regions instantiate(Environment env, FieldTable table) {
        // TODO: if env doesn't contain var?
        return env.getOrDefault(var, Regions.fromSet(new HashSet()));
    }

    @Override
    public Atom concat(FieldGraph graph) {
        return new VariableFieldAtom(var, graph);
    }

    @Override
    public Term substitute(Transformation trans) {
        if (trans.containsKey(this)) {
            return trans.get(this);
        }
        return new Term(this);
    }

    public Local getVar() {
        return var;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VariableAtom that = (VariableAtom) o;
        return var.equals(that.var);
        // return var.equivTo(that.var);
    }

    @Override
    public int hashCode() {
        return Objects.hash(var);
    }

    @Override
    public String toString() {
        return var.toString();
    }
}
