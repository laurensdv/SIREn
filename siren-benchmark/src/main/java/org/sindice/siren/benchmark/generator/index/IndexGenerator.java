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
package org.sindice.siren.benchmark.generator.index;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.index.CorruptIndexException;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.sindice.siren.benchmark.generator.document.BenchmarkDocument;
import org.sindice.siren.benchmark.generator.document.DocumentProvider;
import org.sindice.siren.benchmark.generator.document.DocumentProviderFactory;
import org.sindice.siren.benchmark.generator.document.DocumentProviderFactory.DocumentProviderType;
import org.sindice.siren.benchmark.wrapper.IndexWrapper;
import org.sindice.siren.benchmark.wrapper.IndexWrapperFactory;
import org.sindice.siren.benchmark.wrapper.IndexWrapperFactory.IndexWrapperType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IndexGenerator {

	private final IndexWrapper wrapper;

	private final DocumentProvider provider;

	private final List<Long> commitTimes;

	private long             optimiseTime;

	final Logger logger = LoggerFactory.getLogger(IndexGenerator.class);

	/**
	 * Generate an index given the index wrapper and the document provider.
	 *
	 * @param wrapperType
	 *            the {@link IndexWrapperType} to use for generating the index
	 * @param indexDir
	 *            the directory path of the generated index
	 * @param providerType
	 *            the {@link DocumentProviderType} to use for generating documents
	 *            to index
	 * @param inputDir
	 *            the directory path containing archives for the document provider
	 */
	public IndexGenerator(final IndexWrapperType wrapperType,
	                      final File indexDir,
	                      final DocumentProviderType providerType,
	                      final File inputDir)
	throws IOException {
		this.wrapper = IndexWrapperFactory.createIndexWrapper(wrapperType, indexDir);
		this.provider = DocumentProviderFactory.createDataProvider(providerType, inputDir);
		commitTimes = new ArrayList<Long>(128);
	}

	/**
	 * Index the given documents using SIRen
	 *
	 * @throws RDFParseException
	 * @throws RDFHandlerException
	 * @throws IOException
	 */
	public void generateIndex()
	throws IOException {
	  BenchmarkDocument doc = null;

		for (int i = 0; provider.hasNext(); i++) {
			doc = provider.next();
			this.addDocument(doc);
			this.doCommit(i + 1);
		}
		this.doForceMerge();
	}

	/**
	 * Add a document to the index through the index wrapper.
	 * <br>
	 * If an exception is catched, the document is skipped.
	 */
	protected void addDocument(final BenchmarkDocument doc) {
    try {
      wrapper.addDocument(doc);
    }
    // If exception, just log it at error level, and continue indexing
    catch (final Exception e) {
      logger.error("Cannot index document " + doc.getUrl() + ": " + doc.getContent(), e);
    }
	}

	 /**
   * Perform a commit every 100000 documents. Record the time necessary to
   * perform the task.
   */
	protected void doCommit(final int counter) throws IOException {
	  if (counter % 100000 == 0) {
	    final long startMillis = System.currentTimeMillis();
	    wrapper.commit();
	    final long testMillis = System.currentTimeMillis() - startMillis;
	    commitTimes.add(testMillis);
	    logger.debug("Count = {}. Performed commit in {} ms", counter, testMillis);
	  }
	}

	/**
	 * Perform an optimise. Record the time necessary to perform the task.
	 */
	protected void doForceMerge() throws IOException {
	  final long startMillis = System.currentTimeMillis();
	  wrapper.forceMerge();
	  optimiseTime = System.currentTimeMillis() - startMillis;
    logger.debug("Performed optimised in {} ms", optimiseTime);
	}

	/**
	 * Export the list of commit times in a file.
	 * <br>
	 * Each commit times is exported on a line.
	 *
	 * @param output The output file where the commit times will be exported.
	 */
	public void exportCommitTimes(final File output) throws IOException {
	  final FileWriter writer = new FileWriter(new File(output, "commit.out"));
	  for (final long time : commitTimes) {
	    writer.append(Long.toString(time));
	    writer.append('\n');
	  }
	  writer.close();
	}

  /**
   * Export the optimise time
   *
   * @param output The output file where the optimise time will be exported.
   */
  public void exportOptimiseTime(final File output) throws IOException {
    final FileWriter writer = new FileWriter(new File(output, "optimise.out"));
    writer.append(Long.toString(optimiseTime));
    writer.close();
  }

	/**
	 * Close the indexer. See Indexer.java for more details
	 * @throws CorruptIndexException
	 * @throws IOException
	 */
	public final void close() throws IOException {
		wrapper.close();
	}

	public final IndexWrapper getWrapper() {
		return wrapper;
	}

}
