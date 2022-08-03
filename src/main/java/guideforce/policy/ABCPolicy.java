package guideforce.policy;

import guideforce.region.SpecialRegion;
import guideforce.region.Regions;
import soot.SootMethodRef;
import soot.VoidType;

import javax.annotation.Nonnull;

public class ABCPolicy implements Policy {
  public ABCPolicy() { }

  @Override
  public Intrinsic getIntrinsicMethod(SootMethodRef method) {
    switch (method.getSignature()) {
      case "<ourlib.nonapp.TaintAPI: void emitA()>":
        return emitIntrinsic(method, Token.A);
      case "<ourlib.nonapp.TaintAPI: void emitB()>":
        return emitIntrinsic(method, Token.B);
      case "<ourlib.nonapp.TaintAPI: void emitC()>":
        return emitIntrinsic(method, Token.C);
      default:
        return null;
    }
  }

  private Intrinsic emitIntrinsic(SootMethodRef method, Token token) {
    return new Intrinsic() {
      @Nonnull
      @Override
      public Regions getReturnType() {
        assert method.getReturnType().equals(VoidType.v());
        return Regions.singleton(SpecialRegion.BASETYPE_REGION);
      }
    };
  }

  public enum Token {
    A, B, C
  }
}
