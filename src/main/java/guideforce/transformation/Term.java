package guideforce.transformation;

import guideforce.interproc.FieldTable;
import guideforce.intraproc.Environment;
import guideforce.region.Region;
import guideforce.region.Regions;
import soot.Local;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A term is a finite joint of atoms.
 */
public class Term {
    Set<Atom> atoms;

    /**
     * The bottom term
     */
    public Term() {
        atoms = new HashSet();
    }

    /**
     * Singleton term containing atom.
     * @param atom
     */
    public Term(Atom atom) {
        atoms = new HashSet(Collections.singleton(Objects.requireNonNull(atom)));
    }

    public Term(Local var) {
        atoms = new HashSet(Collections.singleton(new VariableAtom(var)));
    }

    public Term(Region region) {
        atoms = new HashSet(Collections.singleton(new RegionAtom(region)));
    }

    public Term(Regions regions) {
        atoms = new HashSet();
        for(Region r : regions.toSet()) {
            atoms.add(new RegionAtom(r));
        }
    }

    public Term(Set<Atom> atoms) {
        this.atoms = atoms;
    }

    /**
     * Join with another term.
     * Because terms are represented by sets of atoms, duplications are already handled.
     * @param term
     */
    public Term join(Term term) {
        Set<Atom> result = new HashSet(atoms);
        result.addAll(term.getAtoms());
        return new Term(result);
    }

    public Regions instantiate(Environment env, FieldTable table) {
        Set<Region> regions = new HashSet();
        for (Atom atom : atoms) {
            regions.addAll(atom.instantiate(env,table).toSet());
        }
        return Regions.fromSet(regions);
    }

    public Term concat(FieldGraph graph) {
        Set<Atom> result = new HashSet();
        for (Atom a : atoms) {
            result.add(a.concat(graph));
        }
        return new Term(result);
    }

    public Term substitute(Transformation trans) {
        Term result = new Term();
        for (Atom a : atoms) {
            result = result.join(a.substitute(trans));
        }
        return result;
    }

    public Set<Atom> getAtoms() {
        return atoms;
    }

    @Override
    public String toString() {
        return atoms.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Term term = (Term) o;
        return atoms.equals(term.atoms);
    }

    @Override
    public int hashCode() {
        return Objects.hash(atoms);
    }
}
