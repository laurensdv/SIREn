/**
 * Copyright (c) 2009-2012 National University of Ireland, Galway. All Rights Reserved.
 *
 * Project and contact information: http://www.siren.sindice.com/
 *
 * This file is part of the SIREn project.
 *
 * SIREn is a free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * SIREn is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public
 * License along with SIREn. If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * @project siren-benchmark
 * @author Renaud Delbru [ 6 Jul 2012 ]
 * @link http://renaud.delbru.fr/
 */
package org.sindice.siren.benchmark.generator.lexicon;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TermLexiconWriter {

  public enum TermGroup {
    HIGH, MEDIUM, LOW
  };

  private long                                  nTerms  = 0;
  private long                                  wordOcc = 0;
  private final File                            outputDir;
  private final Map<TermGroup, TermFileWriter>  writers;

  private final boolean                         keepFile;
  private final String[]                        ranges;

  final Logger logger = LoggerFactory.getLogger(TermLexiconWriter.class);

  public TermLexiconWriter(final File outputDir, final String selectivityRange, final Boolean keepSortedFile) {
    this.outputDir = outputDir;
    writers = new HashMap<TermGroup, TermFileWriter>(3);
    this.ranges = selectivityRange.split("-");
    this.keepFile = keepSortedFile;

    if (!this.outputDir.exists()) {
      logger.info("Creating directory {}", this.outputDir);
      this.outputDir.mkdirs();
    }
  }

  /**
   * Close the associated resources.
   */
  public void close() throws IOException {
    for (final TermFileWriter writer : writers.values()) {
      writer.close();
    }
  }

  /**
   * Create the term lexicon given a iterator over the term-freq of the index.
   * The term lexicon is created as follow:
   * <ul>
   * <li>A temporary local file is created for storing the list of term-freq.
   * <li>The temporary file is sorted by descending frequency.
   * <li>The range of each term group is computed.
   * <li>A term file is created for each term group.
   * <ul>
   */
  public void create(final TermFreqIterator iterator) throws IOException {
    final File tmp1 = new File(outputDir, "term-freq.tmp");
    tmp1.deleteOnExit();
    final File tmp2 = new File(outputDir, "term-freq.sort.tmp");
    logger.info("Is the sorted file being removed? {}", !keepFile);
    if (!keepFile)
      tmp2.deleteOnExit();

    // Create the temporary file that will contain the term and freq
    this.createTempTermFreqFile(tmp1, iterator);
    // Order the terms by descending frequencies
    this.sort(tmp1, tmp2);
    // Find the range of each term group, and create the corresponding term file
    // writers
    this.findRanges(tmp2);
    // Create the term files for each term group
    this.writeTermFiles(tmp2);
  }

  public void create(final File sortFrq)
  throws IOException {
    // update nTerms and wordOcc variables
    this.getFileOccurences(sortFrq);
    // Find the range of each term group, and create the corresponding term file
    // writers
    this.findRanges(sortFrq);
    // Create the term files for each term group
    this.writeTermFiles(sortFrq);
  }

  private void getFileOccurences(final File sortedFile)
  throws IOException {
    final BufferedReader reader = new BufferedReader(new FileReader(sortedFile));
    String line;

    for (nTerms = 0, wordOcc = 0; (line = reader.readLine()) != null; nTerms++) {
      wordOcc += Integer.parseInt(line.split("\\t")[0]);
    }
    reader.close();
  }

  /**
   * Iterate over the list of term and frequency, and write them in a file. Each
   * pair term-freq is written on one line, the frequency begin the first column
   * and the term the second column (separated by a space).
   */
  private void createTempTermFreqFile(final File tmp,
                                      final TermFreqIterator iterator)
  throws IOException {
    logger.info("Creating term frequency file: {}", tmp.getAbsolutePath());
    final BufferedWriter writer = new BufferedWriter(new FileWriter(tmp));
    TermFreq tf = null;
    while (iterator.hasNext()) {
      tf = iterator.next();
      writer.append(Integer.toString(tf.freq));
      writer.append("\t");
      writer.append(tf.term);
      writer.newLine();
      wordOcc += tf.freq;
      nTerms++;
    }
    iterator.close();
    writer.close();
  }

  /**
   * Sort the file containing the term and their frequency by descending
   * frequency. <br>
   * The sort is made by a call to Linux sort, a sorted file is created and the
   * unsorted file is deleted.
   */
  private void sort(final File input, final File output)
  throws IOException {
    final String cmd = "sort " + input.getAbsolutePath() + " -r -n -o " +
                       output.getAbsolutePath() + " -T " + outputDir.getAbsolutePath();
    logger.info("Executing: {}", cmd);
    final Process sort = Runtime.getRuntime().exec(cmd);
    try {
      final int code = sort.waitFor();
      if (code != 0) {
        final BufferedReader stdError = new BufferedReader(
          new InputStreamReader(sort.getErrorStream()));
        String s;
        while ((s = stdError.readLine()) != null) {
          logger.error("Sort error output: {}", s);
        }
        throw new RuntimeException(
          "Cannot create lexicon. Sort returned code " + code);
      }
    } catch (final InterruptedException e) {
      throw new RuntimeException("Cannot create lexicon", e);
    }
    input.delete(); // delete first temporary file
  }

  /**
   * Find the ranges of each term group. The ranges are obtained as follows:
   * <ul>
   * <li>the words are ordered by descending frequency;
   * <li>the high range contains the first k words whose cumulative frequency is
   * 90% of all word occurrences;
   * <li>the range of words whose cumulative frequency accounts for the next 5%
   * constitutes the medium range;
   * <li>all remaining words fall within the low range.
   * <ul>
   */
  private void findRanges(final File sortedFile) throws IOException {
    final BufferedReader reader = new BufferedReader(new FileReader(sortedFile));
    logger.info("Using the Selectivity range: {}-{}-{}", new Object[] { ranges[0], ranges[1], ranges[2] });
    this.findRange(reader, TermGroup.HIGH, (wordOcc * Integer.valueOf(ranges[0])) / 100);
    this.findRange(reader, TermGroup.MEDIUM, (wordOcc * Integer.valueOf(ranges[1])) / 100);
    this.findLowRange(reader, TermGroup.LOW);
  }

  private void findRange(final BufferedReader reader, final TermGroup group,
                         final long maxCumulativeFreq)
  throws NumberFormatException, IOException {
    String line;
    String[] tokens;
    long cpt = 0;
    long cumulativeFrequency = 0;

    while ((line = reader.readLine()) != null) {
      cpt++;
      tokens = line.split("\\t");
      cumulativeFrequency += Integer.parseInt(tokens[0]);
      if (cumulativeFrequency >= maxCumulativeFreq) {
        writers.put(group, new TermFileWriter(new File(outputDir, group.toString()), cpt));
        return;
      }
    }
  }

  private void findLowRange(final BufferedReader reader, final TermGroup group)
  throws IOException {
    long cpt = 0;

    while (reader.readLine() != null) {
      cpt++;
    }
    writers.put(group, new TermFileWriter(new File(outputDir, group.toString()), cpt));
  }

  private void writeTermFiles(final File input)
  throws IOException {
    final BufferedReader reader = new BufferedReader(new FileReader(input));
    logger.info("Average frequency for the group {}:", TermGroup.HIGH);
    this.writeTermFile(reader, writers.get(TermGroup.HIGH));
    logger.info("Average frequency for the group {}:", TermGroup.MEDIUM);
    this.writeTermFile(reader, writers.get(TermGroup.MEDIUM));
    logger.info("Average frequency for the group {}:", TermGroup.LOW);
    this.writeTermFile(reader, writers.get(TermGroup.LOW));
    reader.close();
  }

  private void writeTermFile(final BufferedReader reader,
                             final TermFileWriter writer)
  throws IOException {
    final long nTerms = writer.getNumberTerms();
    String line = "";
    String[] tokens;
    long cumulativeFRQ = 0;
    long max = 0;
    long min = Long.MAX_VALUE;

    for (long i = 0; i < nTerms && (line = reader.readLine()) != null; i++) {
      tokens = line.split("\\t");
      writer.add(tokens[1]);
      final long frq = Integer.parseInt(tokens[0]);
      if (frq > max)
        max = frq;
      if (frq < min)
        min = frq;
      cumulativeFRQ += frq;
    }
    logger.info("Average frequency: {}", nTerms == 0 ? 0 : cumulativeFRQ / nTerms);
    logger.info("Min-Max values: [ {}, {} ]", new Object[] { min, max });
  }

  @Override
  public final String toString() {
    final StringBuilder builder = new StringBuilder();

    for (final Entry<TermGroup, TermFileWriter> entry : writers.entrySet()) {
      builder.append(entry.getKey().name());
      builder.append(": ");
      builder.append(entry.getValue().getNumberTerms());
      builder.append(" terms, ");
    }
    builder.append("total: ");
    builder.append(this.nTerms);
    builder.append(" terms, ");
    builder.append("occurrences: ");
    builder.append(this.wordOcc);
    return builder.toString();
  }

}
