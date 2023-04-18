package com.epam.healenium.treecomparing;

/**
 * Represents metrics (p) between two nodes
 * p ∈ [0, 1],
 * where 0 - elements are strongly different
 *       1 - elements are the same
 */
@FunctionalInterface
public interface NodeDistance {

    double distance(Node node1, Node node2, int LCSDistance, int curPathHeight);

}
