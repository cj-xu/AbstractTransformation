package guideforce.transformation;

import guideforce.interproc.FieldTable;
import guideforce.intraproc.Environment;
import guideforce.region.Regions;

public interface Atom {
    public Regions instantiate(Environment env, FieldTable table);
    public Atom concat(FieldGraph graph);
    public Term substitute(Transformation trans);
}
