package com.pcauto.bktree;

public final class DistanceFunctions {

    private DistanceFunctions() {
    }

    /**
     * @return A case sensitive Hamming Distance function to compare CharSequence objects.
     */
    public static DistanceFunction<CharSequence> hammingDistance() {
        return hammingDistance(true);
    }

    /**
     * @param isCaseSensitive Toggle the case sensitivity of the function.
     * @return A Hamming Distance function to compare CharSequence objects.
     */
    public static DistanceFunction<CharSequence> hammingDistance(boolean isCaseSensitive) {
        return new HammingDistanceFunction(isCaseSensitive);
    }

    /**
     * @return A case sensitive Levenshtein Distance function to compare CharSequence objects.
     */
    public static DistanceFunction levenshteinDistance() {
        return levenshteinDistance(true);
    }

    /**
     * @param isCaseSensitive Toggle the case sensitivity of the function.
     * @return A Levenshtein Distance function to compare CharSequence objects.
     */
    public static DistanceFunction<CharSequence> levenshteinDistance(boolean isCaseSensitive) {
        return new LevenshteinDistanceFunction(isCaseSensitive);
    }

    // Compare characters a and b for equality, comparing upper case variants when case insensitive.
    private static boolean charEquals(char a, char b, boolean isCaseSensitive) {
        if (isCaseSensitive)
            return a == b;

        return Character.toUpperCase(a) == Character.toUpperCase(b);
    }

    /**
     * Word distance by letter changes only.
     */
    private static class HammingDistanceFunction implements DistanceFunction<CharSequence> {

        private final boolean isCaseSensitive;

        public HammingDistanceFunction(boolean isCaseSensitive) {
            this.isCaseSensitive = isCaseSensitive;
        }

        @Override
        public int distance(CharSequence left, CharSequence right) {
            if (left.length() != right.length())
                throw new IllegalArgumentException(left + " and " + right + " are not different lengths.");
            int wordDistance = 0;

            for (int i = 0; i < left.length(); i++) {
                if (!charEquals(left.charAt(i), right.charAt(i), isCaseSensitive))
                    wordDistance++;
            }

            return wordDistance;
        }
    }

    /**
     * Word distance by letter substitutions, insertions, and deletions
     * See https://en.wikipedia.org/wiki/Levenshtein_distance#Iterative_with_two_matrix_rows
     */
    private static class LevenshteinDistanceFunction implements DistanceFunction<CharSequence> {

        private final boolean isCaseSensitive;

        public LevenshteinDistanceFunction(boolean isCaseSensitive) {
            this.isCaseSensitive = isCaseSensitive;
        }

        @Override
        public int distance(CharSequence left, CharSequence right) {
            int leftLength = left.length(), rightLength = right.length();

            // special cases.
            if (leftLength == 0)
                return rightLength;
            if (rightLength == 0)
                return leftLength;

            // Use the iterative matrix method.
            int[] currentRow = new int[rightLength + 1];
            int[] nextRow = new int[rightLength + 1];

            // Fill first row with all edit counts.
            for (int i = 0; i <= rightLength; i++)
                currentRow[i] = i;

            for (int i = 1; i <= leftLength; i++) {
                nextRow[0] = i;

                for (int j = 1; j <= rightLength; j++) {
                    int subDistance = currentRow[j - 1]; // Distance without insertions or deletions.
                    if (!charEquals(left.charAt(i - 1), right.charAt(j - 1), isCaseSensitive))
                        subDistance++; // Add one edit if letters are different.
                    nextRow[j] = Math.min(Math.min(nextRow[j - 1], currentRow[j]) + 1, subDistance);
                }

                // Swap rows, use last row for next row.
                int[] t = currentRow;
                currentRow = nextRow;
                nextRow = t;
            }

            return currentRow[rightLength];
        }

    }
}
