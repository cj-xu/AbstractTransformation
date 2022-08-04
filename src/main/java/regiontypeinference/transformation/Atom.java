package regiontypeinference.transformation;

import regiontypeinference.interproc.FieldTable;
import regiontypeinference.intraproc.Environment;
import regiontypeinference.region.Regions;

public interface Atom {
    public Regions instantiate(Environment env, FieldTable table);
    public Atom concat(FieldGraph graph);
    public Term substitute(Transformation trans);
}
