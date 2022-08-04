package regiontypeinference.intraproc;

import regiontypeinference.interproc.TransAndTerm;
import regiontypeinference.transformation.Transformation;

import java.util.Objects;

public final class TransformationFlow {

    private Transformation trans;

    public TransformationFlow(Transformation trans) {
        this.trans = trans;
    }

    public TransformationFlow(TransAndTerm tt) {
        trans = tt.getTrans();
    }

    static TransformationFlow bottom() {
        return new TransformationFlow(Transformation.bottom());
    }

    static TransformationFlow identity() {
        return new TransformationFlow(Transformation.identity());
    }

    static void copy(TransformationFlow from, TransformationFlow to) {
        to.trans = new Transformation(from.trans);
    }

    static void merge(TransformationFlow in1, TransformationFlow in2, TransformationFlow out) {
        out.trans = in1.trans.join(in2.trans);
    }

    public Transformation getTrans() {
        return trans;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransformationFlow that = (TransformationFlow) o;
        return Objects.equals(trans, that.trans);
    }

    @Override
    public int hashCode() {
        return Objects.hash(trans);
    }

    @Override
    public String toString() {
        return trans.toString();
    }
}
