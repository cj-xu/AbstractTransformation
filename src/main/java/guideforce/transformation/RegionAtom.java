package guideforce.transformation;

import guideforce.interproc.FieldTable;
import guideforce.intraproc.Environment;
import guideforce.region.Region;
import guideforce.region.Regions;

import java.util.Objects;

public class RegionAtom implements Atom {
    private Region region;

    public RegionAtom(Region region) {
        this.region = region;
    }

    @Override
    public Regions instantiate(Environment env, FieldTable talbe) {
        return Regions.singleton(region);
    }

    @Override
    public Atom concat(FieldGraph graph) {
        return new RegionFieldAtom(region, graph);
    }

    @Override
    public Term substitute(Transformation trans) {
        return new Term(this);
    }

    public Region getRegion() {
        return region;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RegionAtom that = (RegionAtom) o;
        return region.equals(that.region);
    }

    @Override
    public int hashCode() {
        return Objects.hash(region);
    }

    @Override
    public String toString() {
        return region.toString();
    }
}
