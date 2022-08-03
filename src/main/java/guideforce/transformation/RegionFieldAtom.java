package guideforce.transformation;

import guideforce.interproc.FieldTable;
import guideforce.intraproc.Environment;
import guideforce.region.Region;
import guideforce.region.Regions;
import soot.SootField;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class RegionFieldAtom implements Atom, Key {
    private Region region;
    private FieldGraph graph;

    public RegionFieldAtom(Region region, FieldGraph graph) {
        this.region = region;
        this.graph = graph;
    }

    public RegionFieldAtom(Region region, SootField field) {
        this.region = region;
        this.graph = new FieldGraph(field);
    }

    /**
     * Instantiating the region field w.r.t. the given field table
     *
     * @param table  Field table
     * @return
     */
    @Override
    public Regions instantiate(Environment env, FieldTable table) {
        Set<Region> regions = new HashSet<>();
        Set<FieldTable.Key> reachables = reachableFields(table);
        for (Map.Entry<FieldTable.Key, Regions> entry : table.entrySet()) {
            if (reachables.contains(entry.getKey())) {
                regions.addAll(entry.getValue().toSet());
            }
        }
        return Regions.fromSet(regions);
    }

    @Override
    public Atom concat(FieldGraph graph) {
        return new RegionFieldAtom(region, this.graph.concat(graph));
    }

    @Override
    public Term substitute(Transformation trans) {
        return new Term(this);
    }

    /**
     * The set of fields that are reachable w.r.t. the given field table
     *
     * @param table  Field table
     * @return
     */
    public Set<FieldTable.Key> reachableFields(FieldTable table) {
        FieldTable.Key key = new FieldTable.Key(region, graph.getHead());
        Set<FieldTable.Key> result = new HashSet<>();
        result.add(key);
        boolean stable = false;
        while (!stable) {
            stable = true;
            for (FieldTable.Key s : result) {
                Set successors = successorFields(table, s);
                boolean changed = result.addAll(successors);
                stable = stable && !changed;
            }
        }
        return result;
    }

    /**
     * The set of immediate successors of a given field w.r.t. the given field table
     *
     * @param table  Field table
     * @param key    field key.getField() (of objects in region key.getRegion())
     * @return
     */
    public Set<FieldTable.Key> successorFields(FieldTable table, FieldTable.Key key) {
        Set<FieldTable.Key> result = new HashSet<>();
        for (Map.Entry<FieldTable.Key, Regions> entry : table.entrySet()) {
            if (graph.contains(key.getField(), entry.getKey().getField())
            && entry.getValue().toSet().contains(key.getRegion())) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    public Region getRegion() {
        return region;
    }

    public FieldGraph getGraph() {
        return graph;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RegionFieldAtom that = (RegionFieldAtom) o;
        return region.equals(that.region) && graph.equals(that.graph);
    }

    @Override
    public int hashCode() {
        return Objects.hash(region, graph);
    }

    @Override
    public String toString() {
        return region + "." + graph;
    }
}
