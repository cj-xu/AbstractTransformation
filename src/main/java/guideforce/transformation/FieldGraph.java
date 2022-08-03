package guideforce.transformation;

import soot.SootField;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Nonempty field graphs
 */
public class FieldGraph {
    private SootField head, tail;
    private Set<Edge> edges;

    /**
     * Singleton graph, representing the field
     * @param field
     */
    public FieldGraph(SootField field) {
        this.head = field;
        this.tail = field;
        this.edges = new HashSet<>();
    }

    public FieldGraph(SootField head, SootField tail, Set<Edge> edges) {
        this.head = head;
        this.tail = tail;
        this.edges = edges;
    }

    public boolean contains(SootField source, SootField target) {
        return edges.contains(new Edge(source, target));
    }

    /**
     * Concatenate with another field graph on the right
     * @param next
     */
    public FieldGraph concat(FieldGraph next) {
        Set<Edge> edgeSet = new HashSet<>(edges);
        edgeSet.add(new Edge(this.tail, next.head));
        edgeSet.addAll(next.edges);
        return new FieldGraph(this.head, next.tail, edgeSet);
    }

    public SootField getHead() {
        return head;
    }

    public SootField getTail() {
        return tail;
    }

    public Set<Edge> getEdges() {
        return edges;
    }

    public boolean isSingleton(SootField field) {
        return edges.isEmpty() && head.equals(field);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FieldGraph that = (FieldGraph) o;
        return head.equals(that.head) && tail.equals(that.tail) && edges.equals(that.edges);
    }

    @Override
    public int hashCode() {
        return Objects.hash(head, tail, edges);
    }

    @Override
    public String toString() {
        if (edges.isEmpty()) {
            return head.getName();
        }
        return "(" + head.getName() + "," + edges + "," + tail.getName() + ")";
    }

    static class Edge {
        final SootField source, target;

        Edge(SootField source, SootField target) {
            this.source = source;
            this.target = target;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Edge edge = (Edge) o;
            return source.equals(edge.source) && target.equals(edge.target);
        }

        @Override
        public int hashCode() {
            return Objects.hash(source, target);
        }

        public String toString() {
            return String.format("(%s, %s)", source.getName(), target.getName());
        }
    }
}
