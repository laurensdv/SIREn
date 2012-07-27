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
package org.sindice.siren.benchmark.query;

import java.io.File;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

import org.sindice.siren.benchmark.wrapper.IndexWrapperFactory.IndexWrapperType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueryExecutorCLI {

  protected final OptionParser parser;
  protected OptionSet          opts;

  public static final String       HELP                = "help";

  public static final String       INDEX_WRAPPER       = "index";
  public static final String       INDEX_DIRECTORY     = "input";

  public static final String       LEXICON_DIR         = "lexicon";
  public static final String       QUERY_SPEC          = "query";

  public static final String       COLD_CACHE          = "cold-cache";
  public static final String       SEED                = "seed";

  final Logger logger = LoggerFactory.getLogger(QueryExecutor.class);

  public QueryExecutorCLI () {
    parser = new OptionParser();
    parser.accepts(HELP, "print this help");
    parser.accepts(INDEX_WRAPPER, "The type of index wrapper to use: [Siren10]")
        .withRequiredArg().ofType(IndexWrapperType.class);
    parser.accepts(INDEX_DIRECTORY, "Specify the indexing directory.")
        .withRequiredArg().ofType(File.class);
    parser.accepts(COLD_CACHE, "The cache will be flushed before executing the query.")
        .withRequiredArg().ofType(Boolean.class).defaultsTo(false);
    parser.accepts(SEED, "The seed for generating random keywords")
        .withRequiredArg().ofType(Integer.class).defaultsTo(42);
    parser.accepts(LEXICON_DIR, "The directory where the groups terms " +
    		"files for the Indexed Data are stored.")
        .withRequiredArg().ofType(File.class);
    parser.accepts(QUERY_SPEC, "The specification for the generated queries, " +
    		"in JSON format")
        .withRequiredArg().ofType(File.class);
  }

  public final void parseAndExecute(final String[] cmds) throws Exception {
    opts = parser.parse(cmds);
    if (opts.has(HELP)) {
      parser.printHelpOn(System.out);
      return;
    }

    // Index directory
    File indexDirectory = null;
    if (opts.has(INDEX_DIRECTORY)) {
      indexDirectory = (File) opts.valueOf(INDEX_DIRECTORY);
    }
    else {
      parser.printHelpOn(System.err);
      throw new IllegalArgumentException("You must specify an index " +
          "directory (--input)");
    }

    // Index wrapper
    IndexWrapperType wrapperType = null;
    if (opts.has(INDEX_WRAPPER)) {
      wrapperType = (IndexWrapperType) opts.valueOf(INDEX_WRAPPER);
    }
    else {
      parser.printHelpOn(System.err);
      throw new IllegalArgumentException("You must specify a type of index" +
          " (--index)");
    }

    // Terms Specification
    File querySpec = null;
    if (opts.has(QUERY_SPEC)) {
      querySpec = (File) opts.valueOf(QUERY_SPEC);
    }
    else {
      parser.printHelpOn(System.err);
      throw new IllegalArgumentException("You must specify a query specification" +
          " file (--query)");
    }

    // Term lexicon directory
    File lexiconDir = null;
    if (opts.has(LEXICON_DIR)) {
      lexiconDir = (File) opts.valueOf(LEXICON_DIR);
    }
    else {
      parser.printHelpOn(System.err);
      throw new IllegalArgumentException("You must specify the term lexicon" +
          " directory (--lexicon)");
    }

    // Cold Cache
    final boolean coldCache = (Boolean) opts.valueOf(COLD_CACHE);

    // TermLexiconReader seed
    final int seed = (Integer) opts.valueOf(SEED);

    // Configure the benchmark
    final File indexPath = new File(indexDirectory, "index");
    final QueryExecutor qe = new QueryExecutor(wrapperType, indexPath,
      querySpec, lexiconDir);
    qe.setColdCache(coldCache);
    qe.setSeed(seed);

    // Execute the benchmark
    qe.execute();

    // Export benchmark results
    final File resultDir = new File(indexDirectory, "benchmark");
    qe.exportHits(resultDir);
    qe.exportQueryTimes(resultDir);
    qe.exportMeasurementTimes(resultDir);
    qe.exportQueryRates(resultDir);

    // close resources
    qe.close();
  }

  public static void main(final String[] args) throws Exception {
    final QueryExecutorCLI cmd = new QueryExecutorCLI();
    cmd.parseAndExecute(args);
  }

}
