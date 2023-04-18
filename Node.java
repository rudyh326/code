package com.epam.healenium.treecomparing;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;

public class Node {

    private final String tag;
    private final String id;
    private final Set<String> classes;
    private final Integer index;
    private final Map<String, String> otherAttributes;
    private final String innerText;

    private Node parent;
    private List<Node> children;

    Node(String tag, String id, Set<String> classes, Integer index,
                Map<String, String> otherAttributes, List<Node> children, String innerText) {
        this.tag = tag;
        this.id = id;
        this.classes = classes;
        this.index = index;
        this.otherAttributes = otherAttributes;
        this.children = children;
        this.innerText = innerText;
    }

    public String getTag() {
        return tag;
    }

    public String getId() {
        return id;
    }

    public Set<String> getClasses() {
        return classes;
    }

    public Integer getIndex() {
        return index;
    }

    public Map<String, String> getOtherAttributes() {
        return otherAttributes;
    }

    public String getInnerText() {
        return innerText;
    }

    public List<Node> getChildren() {
        return children;
    }

    public Node getParent() {
        return parent;
    }

    void setParent(Node parent) {
        this.parent = parent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return Objects.equals(tag, node.tag) &&
                Objects.equals(id, node.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tag, id);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Node.class.getSimpleName() + "[", "]")
            .add("tag='" + tag + "'")
            .add("id='" + id + "'")
            .add("classes=" + classes)
            .add("index=" + index)
            .add("innerText='" + innerText + "'")
            .add("otherAttributes=" + otherAttributes)
            .toString();
    }
}
