package com.epam.healenium.treecomparing;

/**
 * Metric based on Longest common subsequence.
 * See https://en.wikipedia.org/wiki/Longest_common_subsequence_problem
 */
public class LCSPathDistance implements PathDistance {

    @Override
    public int distance(Path path1, Path path2) {
        return lcs(path1.getNodes(), path2.getNodes());
    }

    private int lcs(Node[] X, Node[] Y) {
        int m = X.length;
        int n = Y.length;
        int[][] L = new int[m + 1][n + 1];

        for (int i = 0; i <= m; i++) {
            for (int j = 0; j <= n; j++) {
                if (i == 0 || j == 0) {
                    L[i][j] = 0;
                } else if (X[i - 1].equals(Y[j - 1])) {
                    L[i][j] = L[i - 1][j - 1] + 1;
                } else {
                    L[i][j] = Math.max(L[i - 1][j], L[i][j - 1]);
                }
            }
        }
        return L[m][n];
    }
}
