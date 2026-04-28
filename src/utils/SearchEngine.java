package src.utils;

import java.util.ArrayList;

public class SearchEngine {

    // Wrapper class so we can sort by best match
    public static class SearchResult {
        public String text;
        public double score;

        public SearchResult(String text, double score) {
            this.text = text;
            this.score = score;
        }
    }

    // --- Utility: Lowercase + strip punctuation ---
    private static String normalize(String s) {
        StringBuilder out = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (Character.isLetterOrDigit(c)) {
                out.append(Character.toLowerCase(c));
            }
        }
        return out.toString();
    }

    // --- Levenshtein Distance (1D Array Optimization) ---
    private static int levenshtein(String s1, String s2) {
        int n = s1.length();
        int m = s2.length();
        int[] prev = new int[m + 1];
        int[] curr = new int[m + 1];

        for (int j = 0; j <= m; j++) {
            prev[j] = j;
        }

        for (int i = 1; i <= n; i++) {
            curr[0] = i;
            for (int j = 1; j <= m; j++) {
                if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                    curr[j] = prev[j - 1];
                } else {
                    curr[j] = 1 + Math.min(prev[j], Math.min(curr[j - 1], prev[j - 1]));
                }
            }
            int[] temp = prev;
            prev = curr;
            curr = temp;
        }
        return prev[m];
    }

    // --- Similarity Ratio ---
    private static double similarityRatio(String a, String b) {
        if (a.isEmpty() && b.isEmpty()) return 1.0;
        if (a.isEmpty() || b.isEmpty()) return 0.0;
        int dist = levenshtein(a, b);
        return 1.0 - ((double) dist / Math.max(a.length(), b.length()));
    }

    // --- Main Tokenized Search Function ---
    public static SearchResult[] searchSimilar(String query, String[] list, double threshold) {
        if (query == null || query.trim().isEmpty()) {
            SearchResult[] results = new SearchResult[list.length];
            for (int i = 0; i < list.length; i++) {
                results[i] = new SearchResult(list[i], 1.0);
            }
            return results;
        }

        // Split query into keywords
        String[] keywords = query.split("\\s+");
        for (int i = 0; i < keywords.length; i++) {
            keywords[i] = normalize(keywords[i]);
        }

        ArrayList<SearchResult> results = new ArrayList<>();

        for (String item : list) {
            // Split target item into text segments
            String[] textSegments = item.split("\\s+");
            for (int i = 0; i < textSegments.length; i++) {
                textSegments[i] = normalize(textSegments[i]);
            }

            boolean isMatch = false;
            double totalKwScore = 0.0;
            int validKws = 0;

            for (String kw : keywords) {
                if (kw.isEmpty()) continue;
                validKws++;
                double bestSegScore = 0.0;
                
                // Compare keyword against EVERY segment in the target string
                for (String seg : textSegments) {
                    if (seg.isEmpty()) continue;
                    double sim = similarityRatio(kw, seg);
                    if (sim > bestSegScore) {
                        bestSegScore = sim;
                    }
                }
                
                totalKwScore += bestSegScore;
                
                // If ANY keyword matches a segment >= threshold, we include it! (Matches your C++ logic)
                if (bestSegScore >= threshold) {
                    isMatch = true;
                }
            }

            // Average score so perfect full-name matches float above partial matches
            double finalScore = validKws > 0 ? (totalKwScore / validKws) : 0.0;

            // Fallback: Check for raw substring inclusion (e.g. searching "lec" matching "Lecture")
            String normItem = normalize(item);
            String normQuery = normalize(query.replaceAll("\\s+", ""));
            if (!normQuery.isEmpty() && normItem.contains(normQuery)) {
                isMatch = true;
                finalScore = Math.max(finalScore, 0.9); 
            }

            if (isMatch) {
                results.add(new SearchResult(item, finalScore));
            }
        }

        // Sort results so the absolute best matches are at index 0
        results.sort((a, b) -> Double.compare(b.score, a.score));

        return results.toArray(new SearchResult[0]);
    }
}