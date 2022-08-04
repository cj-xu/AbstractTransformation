package regiontypeinference.policy;

import regiontypeinference.region.Regions;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

/**
 * Type information for an intrinsic method.
 */
@Immutable
public interface Intrinsic {

  /**
   * Specifies the refined return type of the method.
   * <p>
   * If the method is an intrinsic constructor "void <init>(...)", then the return type
   * is interpreted as the type of the object itself on which the constructor is called.
   * This allows one to specify the region of the constructed object.
   *
   * @param region        Region of the declaring class of the intrinsic method.
   * @param argumentTypes Refined argument types of the method.
   * @return Refined return type of method, or refined type of object itself for ctors.
   */
  @Nonnull
  Regions getReturnType();
}
