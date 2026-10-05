package com.example;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

/**
 * Reducer for the document similarity job.
 *
 * Input:  whatever your mapper emits, grouped by key by the shuffle/sort phase.
 *
 * Output: one line per pair of documents that share at least one word, in exactly this
 *         format (see README.md):
 *
 *             Doc01, Doc02 Similarity: 0.18
 *
 *         where the two IDs are in ascending String order (Doc01 before Doc02), and the
 *         Jaccard similarity  |A ∩ B| / |A ∪ B|  is printed with two decimals, e.g.
 *         String.format("%.2f", similarity). Note that "%.2f" uses the machine's locale;
 *         use  String.format(java.util.Locale.US, "%.2f", similarity)  to be safe.
 *
 * Hint: in the design suggested in README.md all documents reach a single reducer, one per
 *       reduce() call. You cannot compare documents until you have seen all of them, so
 *       reduce() only stores each document, and the pairwise comparison happens in
 *       cleanup(), which Hadoop calls once after the last reduce() call.
 */
public class DocumentSimilarityReducer extends Reducer<Text, Text, Text, Text> {

    private final Map<String, Set<String>> allDocs = new TreeMap<>();

    @Override
    protected void reduce(Text key, Iterable<Text> values, Context context)
            throws IOException, InterruptedException {
        Set<String> docWords = new TreeSet<>();
        for (Text value : values) {
            for (String token : value.toString().split("\\s+")) {
                if (!token.isEmpty()) {
                    docWords.add(token);
                }
            }
        }
        allDocs.put(key.toString(), docWords);

    }

    @Override
    protected void cleanup(Context context) throws IOException, InterruptedException {
        List<String> docIds = new ArrayList<>(allDocs.keySet());

        for (int i = 0; i < docIds.size(); i++) {
            for (int j = i + 1; j < docIds.size(); j++) {
                String leftDoc = docIds.get(i);
                String rightDoc = docIds.get(j);
                Set<String> leftWords = allDocs.get(leftDoc);
                Set<String> rightWords = allDocs.get(rightDoc);
                Set<String> shared = new TreeSet<>(leftWords);
                shared.retainAll(rightWords);
                if (shared.isEmpty()) {continue;}
                Set<String> union = new TreeSet<>(leftWords);
                union.addAll(rightWords);
                double similarity = shared.size() / (double) union.size();
                String pair = leftDoc + ", " + rightDoc;
                String output = "Similarity: " + String.format(Locale.US, "%.2f", similarity);
                context.write(new Text(pair), new Text(output));
            }
        }
    }
}
