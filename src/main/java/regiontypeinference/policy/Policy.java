package regiontypeinference.policy;

import soot.SootMethodRef;

public interface Policy {
  Intrinsic getIntrinsicMethod(SootMethodRef method);
}
