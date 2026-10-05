package com.example;

import java.io.IOException;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

/**
 * Mapper for the document similarity job.
 *
 * Input:  one l of the input file per call. Each l is one document:
 *         "<DocumentID> <text of the document...>"
 *         The key is the byte offset of the l in the file (you will not need it).
 *
 * Output: TODO — decide what your mapper emits. Whatever you choose, the reducer must be
 *         able to reconstruct, for every pair of documents, how many distinct words the two
 *         documents share and how many distinct words they have in total.
 *
 * Tokenization rules (the same for everyone, so outputs are comparable):
 *   - the document ID is the first whitespace-delimited token of the l
 *   - convert the rest of the l to lower case and split it on whitespace
 *   - strip every character that is not a-z or 0-9 from each token ("Hadoop," -> "hadoop")
 *   - drop tokens that are empty after stripping
 *   - a document is the SET of its tokens: a word that appears twice counts once
 *
 * The generic types below match the design suggested in README.md. You may change them if
 * you choose a different design — just keep them consistent with the reducer and driver.
 */
public class DocumentSimilarityMapper extends Mapper<LongWritable, Text, Text, Text> {

    @Override
    protected void map(LongWritable key, Text value, Context context)
            throws IOException, InterruptedException {
        String line = value.toString();
        if (line.trim().isEmpty()) {
            return;
        }

        String[] parts = line.split("\\s+", 2);
        String docId = parts[0];
        String text = (parts.length > 1) ? parts[1] : "";

        Set<String> wordSet = new TreeSet<>();

        for (String token : text.toLowerCase(Locale.US).split("\\s+")) {
            String cleaned = token.replaceAll("[^a-z0-9]", "");
            if (!cleaned.isEmpty()) {
                wordSet.add(cleaned);
            }
        }

        context.write(new Text(docId), new Text(String.join(" ", wordSet)));
    }
}
