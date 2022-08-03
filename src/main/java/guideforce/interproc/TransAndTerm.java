package guideforce.interproc;

import guideforce.transformation.Term;
import guideforce.transformation.Transformation;
import soot.Local;

import java.util.Objects;
import java.util.Set;

public class TransAndTerm {
    private final Transformation trans;
    private final Term term;

    public TransAndTerm(Transformation trans, Term type) {
        this.trans = trans;
        this.term = type;
    }

    public TransAndTerm join(TransAndTerm other) {
        return new TransAndTerm(trans.join(other.trans), term.join(other.term));
    }

    public TransAndTerm clean(Set<Local> toKeep) {
        Transformation cleaned = trans.removeLocals(toKeep);
        return new TransAndTerm(cleaned, term);
    }

    public Transformation getTrans() {
        return trans;
    }

    public Term getTerm() {
        return term;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransAndTerm that = (TransAndTerm) o;
        return Objects.equals(trans, that.trans) && Objects.equals(term, that.term);
    }

    @Override
    public int hashCode() {
        return Objects.hash(trans, term);
    }

    @Override
    public String toString() {
        return trans.toString() + " & " + term.toString();
    }
}
