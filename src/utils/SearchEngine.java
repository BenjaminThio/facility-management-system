package src.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SearchEngine {
    public static class SearchResult implements Comparable<SearchResult> {
        public final int originalIndex;
        public final String text;
        public final double similarityScore;

        public SearchResult(int originalIndex, String text, double similarityScore) {
            this.originalIndex = originalIndex;
            this.text = text;
            this.similarityScore = similarityScore;
        }

        @Override
        public int compareTo(SearchResult other) {
            return Double.compare(other.similarityScore, this.similarityScore);
        }

        @Override
        public String toString() {
            return String.format("Score: %.0f%% | Index: %d | Text: '%s'", similarityScore * 100, originalIndex, text);
        }
    }

    public static int levenshteinDistance(String s1, String s2) {
        if (s1.length() < s2.length()) {
            return levenshteinDistance(s2, s1);
        }
        if (s2.isEmpty()) {
            return s1.length();
        }

        int[] previousRow = new int[s2.length() + 1];
        for (int i = 0; i <= s2.length(); i++) {
            previousRow[i] = i;
        }

        for (int i = 0; i < s1.length(); i++) {
            int[] currentRow = new int[s2.length() + 1];
            currentRow[0] = i + 1;
            
            for (int j = 0; j < s2.length(); j++) {
                int insertions = previousRow[j + 1] + 1;
                int deletions = currentRow[j] + 1;
                int substitutions = previousRow[j] + (s1.charAt(i) == s2.charAt(j) ? 0 : 1);
                
                currentRow[j + 1] = Math.min(Math.min(insertions, deletions), substitutions);
            }
            previousRow = currentRow;
        }
        return previousRow[s2.length()];
    }

    public static String normalizeText(String text) {
        if (text == null) return "";
        return text.toLowerCase().replaceAll("\\s+", " ").trim();
    }

    public static SearchResult[] searchSimilar(String targetString, String[] stringArray, double minThreshold) {
        List<SearchResult> results = new ArrayList<>();
        if (stringArray == null || stringArray.length == 0) {
            return results.toArray(SearchResult[]::new);
        }

        String normalizedTarget = normalizeText(targetString);

        for (int i = 0; i < stringArray.length; i++) {
            String normalizedCurrent = normalizeText(stringArray[i]);
            int distance = levenshteinDistance(normalizedTarget, normalizedCurrent);
            int maxLength = Math.max(normalizedTarget.length(), normalizedCurrent.length());
            double similarity = (maxLength == 0) ? 1.0 : (double) (maxLength - distance) / maxLength;

            if (similarity >= minThreshold) {
                results.add(new SearchResult(i, stringArray[i], similarity));
            }
        }

        Collections.sort(results);

        return results.toArray(SearchResult[]::new);
    }

    public static void main(String[] args) {
        String[] database = {
            "Apple iPhone 14 Pro",
            "Apple iPhone 14",
            "Apple iPhone 13 Pro",
            "Samsung Galaxy S23 Ultra",
            "Sony PlayStation 5",
            "  aple iphone 14  ", 
            "Nintendo Switch OLED"
        };

        String searchQuery = " aPple iphne 14 ";
        double threshold = 0.50;
        SearchResult[] matches = searchSimilar(searchQuery, database, threshold);

        System.out.println("Search Query: '" + searchQuery + "'\n");
        System.out.println("Top Matches:");
        
        if (matches.length == 0) {
            System.out.println("No matches found above the threshold.");
        } else {
            for (SearchResult match : matches) {
                System.out.println(match.toString());
            }
        }
    }
}