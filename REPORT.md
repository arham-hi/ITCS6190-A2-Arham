# Assignment #2 — Report

**Name: Arham Hussain Inamdar**

**Student ID: 801404110**

**Email: ainamda3@charlotte.edu**

---

## Design

Which design did you choose (A, B, or your own)? Explain in your own words:

- What your **Mapper** emits as key and value, and why that is the right thing to emit.
- What your **Reducer** receives for one key, what it does with it, and where the Jaccard
  similarity is computed.
- What you had to set in the **Driver** beyond what L4's `Controller` set, and why.

I used Design A. The Mapper reads one document at a time and emits the document ID as the key and its set of cleaned, unique words as the value. This gives the reducer the information it needs to compare each document.

The Rreducer receives each document ID and its words. It stores every document in a map. In cleanup, it compares every pair of documents, finds their intersection and union, and calculates the similarity by dividing the intersection size by the union size.

In the driver, I set the number of reducers to one so every document reaches the same reducer. I also changed the output separator to a space so the output matched the required format. I did not use a combiner because it wouldnt work correctly with this design.

---

## How I ran it

The commands you used, in the order you used them. If you deviated from the steps in the
README, say where and why.

```bash
docker compose up -d
mvn clean package

docker cp target/DocumentSimilarity-0.0.1-SNAPSHOT.jar resourcemanager:/tmp/
docker cp shared-folder/input/data/small_dataset.txt resourcemanager:/tmp/
docker cp shared-folder/input/data/dataset.txt resourcemanager:/tmp/

docker exec -it resourcemanager bash
cd /tmp

hadoop fs -mkdir -p /input/data
hadoop fs -put ./small_dataset.txt /input/data
hadoop fs -put ./dataset.txt /input/data
hadoop fs -ls /input/data

hadoop jar /tmp/DocumentSimilarity-0.0.1-SNAPSHOT.jar \
  com.example.controller.DocumentSimilarityDriver \
  /input/data/small_dataset.txt \
  /output/small_dataset

hadoop fs -cat /output/small_dataset/*

hadoop jar /tmp/DocumentSimilarity-0.0.1-SNAPSHOT.jar \
  com.example.controller.DocumentSimilarityDriver \
  /input/data/dataset.txt \
  /output/dataset

hadoop fs -cat /output/dataset/*

hdfs dfs -get /output /tmp/
exit

docker cp resourcemanager:/tmp/output/. shared-folder/output/
docker compose down
```

---

## Output

### `small_dataset.txt` (3 lines)

```
Document1, Document2 Similarity: 0.18
Document1, Document3 Similarity: 0.20
Document2, Document3 Similarity: 0.10
```

### `dataset.txt` (66 lines)

```
Doc01, Doc02 Similarity: 0.16
Doc01, Doc03 Similarity: 0.13
Doc01, Doc04 Similarity: 0.07
Doc01, Doc05 Similarity: 0.10
Doc01, Doc06 Similarity: 0.09
Doc01, Doc07 Similarity: 0.11
Doc01, Doc08 Similarity: 0.10
Doc01, Doc09 Similarity: 0.11
Doc01, Doc10 Similarity: 0.09
Doc01, Doc11 Similarity: 0.07
Doc01, Doc12 Similarity: 0.19
Doc02, Doc03 Similarity: 0.20
Doc02, Doc04 Similarity: 0.13
Doc02, Doc05 Similarity: 0.10
Doc02, Doc06 Similarity: 0.09
Doc02, Doc07 Similarity: 0.06
Doc02, Doc08 Similarity: 0.09
Doc02, Doc09 Similarity: 0.05
Doc02, Doc10 Similarity: 0.10
Doc02, Doc11 Similarity: 0.06
Doc02, Doc12 Similarity: 0.14
Doc03, Doc04 Similarity: 0.17
Doc03, Doc05 Similarity: 0.11
Doc03, Doc06 Similarity: 0.08
Doc03, Doc07 Similarity: 0.16
Doc03, Doc08 Similarity: 0.11
Doc03, Doc09 Similarity: 0.07
Doc03, Doc10 Similarity: 0.10
Doc03, Doc11 Similarity: 0.12
Doc03, Doc12 Similarity: 0.11
Doc04, Doc05 Similarity: 0.09
Doc04, Doc06 Similarity: 0.11
Doc04, Doc07 Similarity: 0.18
Doc04, Doc08 Similarity: 0.09
Doc04, Doc09 Similarity: 0.08
Doc04, Doc10 Similarity: 0.10
Doc04, Doc11 Similarity: 0.09
Doc04, Doc12 Similarity: 0.09
Doc05, Doc06 Similarity: 0.20
Doc05, Doc07 Similarity: 0.14
Doc05, Doc08 Similarity: 0.15
Doc05, Doc09 Similarity: 0.07
Doc05, Doc10 Similarity: 0.13
Doc05, Doc11 Similarity: 0.14
Doc05, Doc12 Similarity: 0.11
Doc06, Doc07 Similarity: 0.17
Doc06, Doc08 Similarity: 0.15
Doc06, Doc09 Similarity: 0.08
Doc06, Doc10 Similarity: 0.10
Doc06, Doc11 Similarity: 0.12
Doc06, Doc12 Similarity: 0.13
Doc07, Doc08 Similarity: 0.15
Doc07, Doc09 Similarity: 0.07
Doc07, Doc10 Similarity: 0.08
Doc07, Doc11 Similarity: 0.12
Doc07, Doc12 Similarity: 0.11
Doc08, Doc09 Similarity: 0.19
Doc08, Doc10 Similarity: 0.13
Doc08, Doc11 Similarity: 0.22
Doc08, Doc12 Similarity: 0.12
Doc09, Doc10 Similarity: 0.13
Doc09, Doc11 Similarity: 0.12
Doc09, Doc12 Similarity: 0.13
Doc10, Doc11 Similarity: 0.12
Doc10, Doc12 Similarity: 0.12
Doc11, Doc12 Similarity: 0.11
```

---

## Analysis

Look at the results for `dataset.txt`.

- Which pairs are the most similar, and which the least?
- Do the most similar pairs make sense given what the documents are about?
- The values are all fairly low and close together. Why? What one change to the tokenization
  rules would make the numbers more meaningful?

The most similar pair was Doc08 and Doc11 with a similarity of 0.22. This makes sense because Doc08 is about Spark and Doc11 is about Spark's MLlib. Doc02 and Doc03, and Doc05 and Doc06, were also among the most similar pairs with a score of 0.20. The least similar pair was Doc02 and Doc09 with a score of 0.05

The scores are low and close together because the documents have many different topic specific words, while common words such as “the,” “and,” and “a” appear in many documents. One improvement would be to remove these common stop words before calculating similarity. This would make the comparison focus more on the important topic words

---

## Scalability

**If you used Design A:** it relies on a single reducer that holds every document in memory.
What concretely breaks when the collection has a million documents? Sketch how Design B
avoids the problem.

**If you used Design B:** why did it need more than one pass (or how did you avoid that)?
What is its own bottleneck?

Design A would not work well with 1 million documents because one reducer would have to store every document and its words in memory. It would also need to compare billions of document pairs. This would take too much memory and time, and the single reducer would become a bottleneck

Design B avoids storing every document in one reducer. The mapper emits each word with its document ID, and different Reducers can process different words in parallel. A second job can then combine the shared-word counts for each document pair. Its main bottleneck is that very common words can create a large number of document pairs and a lot of shuffle data

---

## Problems and fixes

Anything that went wrong and what resolved it. Paste the actual error message. If nothing
went wrong, say so.

Nothing went wrong

---

## Use of generative AI

If you used a generative AI tool, include the acknowledgment statement from the syllabus and
say specifically what you used it for. If you did not use one, say so.

I used ChatGPT to help troubleshoot Docker and HDFS commands, verify the output files, and organize my report. I reviewed the answers and verified the final output myself
