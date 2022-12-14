package regiontypeinference.interproc;

import regiontypeinference.region.Region;
import regiontypeinference.region.Regions;
import soot.SootField;

import javax.annotation.concurrent.Immutable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Data structure for the field table.
 * <p>
 * This class is meant purely for data representation.
 * It does not enforce any well-formedness invariants.
 */
public final class FieldTable extends HashMap<FieldTable.Key, Regions> {

  public FieldTable() {
    super();
  }

  public FieldTable(FieldTable other) {
    super(other);
  }

  @Override
  public String toString() {
    StringBuilder buffer = new StringBuilder();
    buffer.append("(");
    buffer.append(entrySet().stream()
            .map(entry -> entry.getKey().getRegion() + "." + entry.getKey().getField().getName() + ": " + entry.getValue())
            .collect(Collectors.joining(", ")));
    buffer.append(")");
    return buffer.toString();
  }

  // Note that the field also contains the information, which class it's defined in.
  // Only the signature of the field is relevant for equality.
  @Immutable
  public static final class Key {
    private final Region region;
    private final SootField field;

    public Key(Region region, SootField field) {
      this.region = Objects.requireNonNull(region);
      this.field = Objects.requireNonNull(field);
    }

    public Region getRegion() {
      return region;
    }

    public SootField getField() {
      return field;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Key key = (Key) o;
      return region.equals(key.region) &&
              field.getSignature().equals(key.field.getSignature());
    }

    @Override
    public int hashCode() {
      return Objects.hash(region, field.getSignature());
    }

    @Override
    public String toString() {
      return "Key{" + "regions=" + region +
              ", field=" + field +
              '}';
    }
  }
}
