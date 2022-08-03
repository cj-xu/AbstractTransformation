package guideforce.intraproc;

import guideforce.region.Regions;
import soot.Local;

import java.util.HashMap;
import java.util.stream.Collectors;

public class Environment extends HashMap<Local, Regions> {

  public Environment() {
    super();
  }

  public Environment(Environment env) {
    super(env);
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("(");
    builder.append(entrySet().stream()
            .map(entry -> entry.getKey() + ": " + entry.getValue())
            .collect(Collectors.joining(", ")));
    builder.append(")");
    return builder.toString();
  }
}
