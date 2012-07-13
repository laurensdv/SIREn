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
 * @author Renaud Delbru [ 11 Jul 2012 ]
 * @link http://renaud.delbru.fr/
 */
package org.sindice.siren.benchmark.generator.lexicon;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.sindice.siren.benchmark.generator.document.BenchmarkDocument;
import org.sindice.siren.benchmark.generator.document.DocumentProvider;
import org.sindice.siren.benchmark.generator.document.DocumentProviderFactory;
import org.sindice.siren.benchmark.generator.document.DocumentProviderFactory.DocumentProviderType;
import org.sindice.siren.benchmark.wrapper.IndexWrapper;
import org.sindice.siren.benchmark.wrapper.NGramLucene40IndexWrapper;
import org.sindice.siren.benchmark.wrapper.StandardLucene40IndexWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TermLexiconGenerator {

  private final DocumentProvider docProvider;
  private final IndexWrapper objectIndex;
  private final IndexWrapper objectNGramIndex;
  private final IndexWrapper predicateIndex;

  private static File TMP_PATH = new File("./target/tmp-index/");

  protected final Logger logger = LoggerFactory.getLogger(TermLexiconGenerator.class);

  public TermLexiconGenerator(final DocumentProviderType type, final File path) throws IOException {
    docProvider = DocumentProviderFactory.createDataProvider(type, path);
    objectIndex = new StandardLucene40IndexWrapper(new File(TMP_PATH, "object"));
    objectNGramIndex = new NGramLucene40IndexWrapper(new File(TMP_PATH, "object-ngram"));
    predicateIndex = new StandardLucene40IndexWrapper(new File(TMP_PATH, "predicate"));
  }

  public void generate(final File outputDir) throws IOException {
    logger.info("Generating indexes");
    this.createIndexes();
    logger.info("Generating lexicons");
    this.createLexicons(outputDir);
  }

  private void createIndexes() throws IOException {
    final ObjectMapper mapper = new ObjectMapper();

    BenchmarkDocument doc;
    JsonNode node;

    // Index
    while (docProvider.hasNext()) {
      doc = docProvider.next();

      // parse original json content
      node = mapper.readTree(doc.getContent().get(0));

      // Filter objects from json tree and index them
      doc = this.filterObject(node, doc);
      objectIndex.addDocument(doc);
      objectNGramIndex.addDocument(doc);

      // Filter predicates from json tree and index them
      doc = this.filterPredicate(node, doc);
      predicateIndex.addDocument(doc);
    }

    // Commit
    objectIndex.commit();
    objectNGramIndex.commit();
    predicateIndex.commit();
  }

  private void createLexicons(final File outputDir) throws IOException {
    logger.info("Writing term lexicon files for objects");
    this.createLexicon(objectIndex, new File(outputDir, "object"), "40-30-20");
    logger.info("Writing term lexicon files for object n-grams");
    this.createLexicon(objectNGramIndex, new File(outputDir, "object-ngram"), "40-25-25");
    logger.info("Writing term lexicon files for predicates");
    this.createLexicon(predicateIndex, new File(outputDir, "predicate"), "90-9-1");
  }

  private void createLexicon(final IndexWrapper indexWrapper, final File outputDir,
                             final String selectivityRange) throws IOException {
    final TermLexiconWriter tlWriter = new TermLexiconWriter(outputDir, selectivityRange, true);
    // filter out all terms with a frequency of 1
    tlWriter.create(indexWrapper.getTermFreqIterator(2));
    logger.info("Term Lexicon - " + tlWriter.toString());
    tlWriter.close();
  }

  private BenchmarkDocument filterPredicate(final JsonNode node, final BenchmarkDocument doc) throws IOException {
    final ArrayList<String> filteredContent = new ArrayList<String>();
    final Iterator<JsonNode> subjects = node.getElements();
    JsonNode subject;
    Iterator<String> predicates;

    // Iterate over each subject
    while (subjects.hasNext()) {
      subject = subjects.next();
      predicates = subject.getFieldNames();

      // Iterate over each predicate
      while (predicates.hasNext()) {
        filteredContent.add(predicates.next());
      }
    }

    // Overwrite previous document content with the filtered content
    doc.putContent(filteredContent.toArray(new String[filteredContent.size()]));

    return doc;
  }

  private BenchmarkDocument filterObject(final JsonNode node, final BenchmarkDocument doc) throws IOException {
    final ArrayList<String> filteredContent = new ArrayList<String>();
    final Iterator<JsonNode> subjects = node.getElements();
    JsonNode subject;
    Iterator<JsonNode> predicates;
    JsonNode predicate;
    Iterator<JsonNode> objects;
    JsonNode object;

    // Iterate over each subject
    while (subjects.hasNext()) {
      subject = subjects.next();
      predicates = subject.getElements();

      // Iterate over each predicate
      while (predicates.hasNext()) {
        predicate = predicates.next();

        // Iterate over each object
        objects = predicate.getElements();
        while (objects.hasNext()) {
          object = objects.next();
          filteredContent.add(object.asText());
        }
      }
    }

    // Overwrite previous document content with the filtered content
    doc.putContent(filteredContent.toArray(new String[filteredContent.size()]));

    return doc;
  }

  public void close() throws IOException {
    try {
      docProvider.close();
    }
    finally {
      try {
        objectIndex.close();
      }
      finally {
        try {
          objectNGramIndex.close();
        }
        finally {
          try {
            predicateIndex.close();
          }
          finally {
            logger.info("Deleting indexes");
            FileUtils.deleteDirectory(TMP_PATH);
          }
        }
      }
    }
  }

  public static void main(final String[] args) throws IOException {
    final File path = new File("./target/sindice-dataset/");
    final TermLexiconGenerator generator = new TermLexiconGenerator(DocumentProviderType.Sindice, path);
    generator.generate(new File("./target/sindice-lexicon/"));
    generator.close();

  }

}
