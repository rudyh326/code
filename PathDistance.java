package com.epam.healenium.treecomparing;


/**
 * Distance between two {@link Path}'s from 0 - there are no similar elements, to 1 - paths are the same
 */
@FunctionalInterface
public interface PathDistance {

    int distance(Path path1, Path path2);
}
