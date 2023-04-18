package com.epam.healenium.treecomparing;

import lombok.extern.slf4j.Slf4j;

import java.lang.Integer;
import java.util.*;
import java.util.stream.Collectors;

import static com.epam.healenium.treecomparing.StreamUtils.logFiltered;

@Slf4j
public class PathFinder {

    private final PathDistance pathDistance;
    private final NodeDistance nodeDistance;

    /**
     * Creates a path finder which operates certain algorithms of distance by path and by node comparing
     *
     * @param pathDistance an algorithm to evaluate node likeness by their path
     * @param nodeDistance an algorithm to evaluate node likeness by content
     */
    public PathFinder(PathDistance pathDistance, NodeDistance nodeDistance) {
        this.pathDistance = pathDistance;
        this.nodeDistance = nodeDistance;
    }

    /**
     * Finds the nearest node that most likely is our searched one
     *
     * @param path      a collection of nodes that represents the total searched node path; the first node is html tag,
     *                  the last node is the searched node. We assume that all nodes in the path have their full info.
     * @param newSource the parsed DOM of the changed page in which we will look for the searched node
     * @return most likely node or null if there isn't any
     */
    public Node findNearest(Path path, Node newSource) {
        List<Scored<Node>> found = find(path, newSource, 1);
        return found.isEmpty() ? null : found.get(0).getValue();
    }

    /**
     * Finds the list of nodes that most likely are our searched one, ordered by likeness descending
     *
     * @param path             a collection of nodes that represents the total searched node path; the first node is
     *                         html tag, the last node is the searched node.  We assume that all nodes in the path have
     *                         their full info.
     * @param newSource        the parsed DOM of the changed page in which we will look for the searched node
     * @param bestGuessesCount the size of the result collection, i.e. the number of similar nodes to return
     * @return a list of probably similar nodes, ordered by likeness descending, with the size of bestGuessesCount
     */
    public List<Scored<Node>> find(Path path, Node newSource, int bestGuessesCount) {
        return getSortedNodes(findScoresToNodes(path, newSource).getValue(), bestGuessesCount, -1);
    }

    /**
     * Finds the Map of all nodes that most likely are our searched one, ordered by likeness descending
     *
     * @param path      a collection of nodes that represents the total searched node path; the first node is
     *                  html tag, the last node is the searched node.  We assume that all nodes in the path have
     *                  their full info.
     * @param newSource the parsed DOM of the changed page in which we will look for the searched node
     * @return a Map of probably similar nodes, with the size of bestGuessesCount.
     */
    public AbstractMap.SimpleImmutableEntry<Integer, Map<Double, List<AbstractMap.SimpleImmutableEntry<Node, Integer>>>> findScoresToNodes(
            Path path, Node newSource) {

        List<Path> destinationLeaves = findAllLeafPaths(newSource);

        Node byPath = path.getLastNode();
        int pathLength = path.getNodes().length;

        List<AbstractMap.SimpleImmutableEntry<Path, Integer>> paths = new ArrayList<>();
        int maxLCSDistance = 0;
        for (Path destinationLeaf : destinationLeaves) {
            int distance = pathDistance.distance(path, destinationLeaf);
            if (distance < 1) {
                continue;
            }
            maxLCSDistance = Math.max(maxLCSDistance, distance);
            paths.add(new AbstractMap.SimpleImmutableEntry<>(destinationLeaf, distance));
        }

        int pathLengthToCheck = Math.min(maxLCSDistance, pathLength);
        Map<Double, List<AbstractMap.SimpleImmutableEntry<Node, Integer>>> scoresToNodes = paths.stream()
                .map(pathPair -> new AbstractMap.SimpleImmutableEntry<>(Arrays.copyOfRange(pathPair.getKey().getNodes(), pathPair.getValue() - 1, pathPair.getKey().getNodes().length), pathPair.getValue()))
                .flatMap(pathPair -> Arrays.stream(pathPair.getKey()).map(it -> new AbstractMap.SimpleImmutableEntry<Node, Integer>(it, pathPair.getValue())))
                .collect(Collectors.groupingBy(nodePair -> nodeDistance.distance(byPath, nodePair.getKey(), nodePair.getValue(), pathLengthToCheck)));

        return new AbstractMap.SimpleImmutableEntry<>(pathLengthToCheck, scoresToNodes);
    }

    /**
     * Sort the Map of all nodes that most likely are our searched one, ordered by likeness descending
     *
     * @param scoresToNodes the source Map of nodes with score
     * @param bestGuessesCount the size of the result collection, i.e. the number of similar nodes to return
     * @param guessCap         a min score, that path must exceed to be selected
     * @return a list of probably similar nodes, ordered by likeness descending, with the size of bestGuessesCount.
     */
    public List<Scored<Node>> getSortedNodes(Map<Double, List<AbstractMap.SimpleImmutableEntry<Node, Integer>>> scoresToNodes, int bestGuessesCount, double guessCap) {
        // normalize params
        final int nodeLimit = normalizeLimit(bestGuessesCount);
        final double scoreLimit = normalizeScoreCap(guessCap);
        return scoresToNodes.keySet().stream()
                .sorted(Comparator.reverseOrder())
                .filter(logFiltered(score -> score >= scoreLimit, score -> log.debug("Skipping nodes, because their score={} less then {}", score, scoreLimit)))
                .flatMap(score -> scoresToNodes.get(score).stream().map(it -> new Scored<>(score, it.getKey())))
                .limit(nodeLimit)
                .collect(Collectors.toList());
    }

    private List<Path> findAllLeafPaths(Node node) {
        List<Path> paths = new ArrayList<>();
        addLeafPath(paths, new Path(node));
        return paths;
    }

    private void addLeafPath(List<Path> leaves, Path current) {
        Deque<Path> paths = new ArrayDeque<>();
        paths.addFirst(current);

        while (!paths.isEmpty()) {
            Path path = paths.removeLast();
            Node node = path.getLastNode();
            if (node.getChildren() == null || node.getChildren().isEmpty()) {
                leaves.add(path);
            } else {
                for (Node child : node.getChildren()) {
                    paths.addFirst(Utils.addNode(path, child));
                }
            }
        }
    }

    /**
     * Validate given score cap
     *
     * @param value must be between 0 and 1
     * @return -1 if passed value was out of range, otherwise use given
     */
    private double normalizeScoreCap(double value) {
        if (value > 1) {
            log.warn("Required min score value={} will be ignored, because exceed allowed value. It must be in range [0..1]", value);
            return -1;
        }
        return value;
    }

    /**
     * Validate parameter of result node collection size
     *
     * @param value desired amount of nodes in result collection
     * @return 1 - if value is zero or negative, otherwise use given
     */
    private int normalizeLimit(int value) {
        if (value < 0) {
            log.warn("Desired number of result nodes={} will be reset to 1, because it must be positive", value);
            return 1;
        }
        return value;
    }
}
