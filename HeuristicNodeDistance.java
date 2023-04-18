package com.epam.healenium.treecomparing;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.similarity.LevenshteinDistance;

import java.util.Objects;
import java.util.Set;

public class HeuristicNodeDistance implements NodeDistance {

    private static final double POINTS_FOR_TAG = 100.0;
    private static final double POINTS_FOR_LCS = 100.0;
    private static final double POINTS_FOR_ID = 50.0;
    private static final double POINTS_FOR_CLASS = 40.0;
    private static final double POINTS_FOR_VALUE = 30.0;
    private static final double POINTS_FOR_INDEX = 0;
    private static final double POINTS_FOR_OTHER_ATTRIBUTE = 30.0;

    @Override
    public double distance(Node node1, Node node2, int LCSDistance, int curPathHeight) {
        double score = 0;
        if (curPathHeight == 0 || (curPathHeight > 5 && (double)LCSDistance / curPathHeight < 0.7)){
            return 0.0;
        }
        else {
            score += (double)LCSDistance / curPathHeight * POINTS_FOR_LCS;
        }

        Set<String> propertyNames = Utils.union(node1.getOtherAttributes().keySet(), node2.getOtherAttributes().keySet());
        Set<String> classNames = Utils.union(node1.getClasses(), node2.getClasses());
        double maximumScore = POINTS_FOR_TAG +
                POINTS_FOR_ID +
                POINTS_FOR_INDEX +
                POINTS_FOR_VALUE +
                POINTS_FOR_LCS +
                POINTS_FOR_CLASS +
                POINTS_FOR_OTHER_ATTRIBUTE;

        if (Stri
        ngUtils.equalsIgnoreCase(node1.getTag(), node2.getTag())) {
            score += POINTS_FOR_TAG;
        }
        if (Objects.equals(node1.getIndex(), node2.getIndex())) {
            score += POINTS_FOR_INDEX;
        }
        if (node1.getId() != null && node2.getId() != null) {
            score += POINTS_FOR_ID * calculateLevenshteinScore(node1.getId(), node2.getId(), 0.3);
        }
        score += POINTS_FOR_VALUE * calculateLevenshteinScore(node1.getInnerText(), node2.getInnerText(), 0.3);

        Set<String> classesIntersect = Utils.intersect(node1.getClasses(), node2.getClasses());
        double intersectScore = classesIntersect.size() * POINTS_FOR_CLASS;
        if (classNames.size() > 0) {
            intersectScore /= classNames.size();
            score += intersectScore;
        }
        else {
            score += POINTS_FOR_CLASS;
        }

        Set<String> node1classesDifference = Utils.difference(node1.getClasses(), node2.getClasses());
        Set<String> node2classesDifference = Utils.difference(node2.getClasses(), node1.getClasses());
        int lengthDifference = Utils.union(node1classesDifference, node2classesDifference).size();
        if (lengthDifference > 0) {
            double classesScore = 0;
            if (node1classesDifference.size() > 0){
                classesScore = calculateClassesIntersectionByLevenshtein(
                        node1classesDifference, node2.getClasses()
                );
            }
            else {
                classesScore = calculateClassesIntersectionByLevenshtein(
                        node1.getClasses(), node2classesDifference
                );
            }
            if (classNames.size() > 0) {
                classesScore /= classNames.size();
            }
            score += lengthDifference * POINTS_FOR_CLASS * classesScore;
        }
        double otherAttributesScore = 0;
        for (String propertyName : propertyNames) {
            otherAttributesScore += POINTS_FOR_OTHER_ATTRIBUTE * calculateLevenshteinScore(
                    node1.getOtherAttributes().get(propertyName),
                    node2.getOtherAttributes().get(propertyName),
                    0.75);
        }
        if (propertyNames.size() > 0) {
            otherAttributesScore /= propertyNames.size();
            score += otherAttributesScore;
        }
        else {
            score += POINTS_FOR_OTHER_ATTRIBUTE;
        }

        return score / maximumScore;
    }

    private double calculateClassesIntersectionByLevenshtein(Set<String> nodeClasses1, Set<String> nodeClasses2) {
        int comparisonsNumber = 0;
        double scores = 0;
        for (String classNameFirst : nodeClasses1) {
            for (String classNameSecond : nodeClasses2) {
                scores += calculateLevenshteinScore(classNameFirst, classNameSecond, 0.75);
                comparisonsNumber += 1;
            }
        }
        if (comparisonsNumber == 0) {
            return 0;
        }
        return scores / comparisonsNumber;
    }

    private double calculateLevenshteinScore(String innerText1, String innerText2, double thresholdPercent) {
        if (innerText1 == null || innerText2 == null) {
            return 0;
        }
        innerText1 = innerText1.toLowerCase();
        innerText2 = innerText2.toLowerCase();
        int length = Math.max(innerText1.length(), innerText2.length());
        if (length == 0) {
            return 1;
        }
        int threshold = calculateLevenshteinThreshold(length, thresholdPercent);
        LevenshteinDistance levenshtein = new LevenshteinDistance(threshold);

        Integer distance = levenshtein.apply(innerText1, innerText2);
        if (distance < 0) {
            return 0;
        }
        return (length - distance.doubleValue()) / length;
    }

    /**
     * The threshold is picked in such a way to not make the algorithm processing too heavy on the text that is most
     * likely not our case. It has been assumed that if the third part of the text does not match, there's no reason to
     * go further.
     *
     * @param maxTextLength max text length
     * @return levenshtein threshold
     */
    private int calculateLevenshteinThreshold(int maxTextLength, double thresholdPercent) {
        return (int) (maxTextLength * thresholdPercent + 1);
    }
}
