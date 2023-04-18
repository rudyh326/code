package com.epam.healenium.treecomparing;

import java.util.Arrays;

public class Path {

    private Node[] nodes;

    public Path(Node[] nodes) {
        this.nodes = nodes;
    }

    public Path(Node node) {
        this.nodes = new Node[]{node};
    }

    public Node[] getNodes() {
        return nodes;
    }

    public void setNodes(Node[] nodes) {
        this.nodes = nodes;
    }

    public Node getLastNode() throws ArrayIndexOutOfBoundsException {
        return nodes[nodes.length - 1];
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Path path = (Path) o;
        return Arrays.equals(nodes, path.nodes);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(nodes);
    }

    @Override
    public String toString() {
        return "Path{" +
                "nodes=" + Arrays.toString(nodes) +
                '}';
    }
}
