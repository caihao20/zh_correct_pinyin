package com.pcauto.component;

public class StringEditMetric implements Metric<String> {
    @Override
    public int getMetric(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];
        for (int i = 1; i <= s1.length(); i++) {
            dp[i][0] = i;
        }
        for (int i = 1; i <= s2.length(); i++) {
            dp[0][i] = i;
        }
        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                dp[i][j] = Math.min(
                        dp[i - 1][j - 1] + (s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1),
                        Math.min(dp[i][j - 1], dp[i - 1][j]) + 1
                );
            }
        }

        return dp[s1.length()][s2.length()];
    }
}
