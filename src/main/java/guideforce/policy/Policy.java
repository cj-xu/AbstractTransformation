package guideforce.policy;

import soot.SootMethodRef;
import soot.jimple.AnyNewExpr;
import soot.jimple.StringConstant;
import guideforce.interproc.CallingContext;
import guideforce.interproc.Location;
import guideforce.region.Region;

public interface Policy {
  Intrinsic getIntrinsicMethod(SootMethodRef method);
}
