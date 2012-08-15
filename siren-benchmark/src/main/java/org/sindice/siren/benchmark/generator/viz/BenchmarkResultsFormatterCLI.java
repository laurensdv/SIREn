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
package org.sindice.siren.benchmark.generator.viz;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Arrays;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

import org.sindice.siren.benchmark.generator.viz.BenchmarkResultsFormatterFactory.FormatterType;
import org.sindice.siren.benchmark.wrapper.IndexWrapperFactory.IndexWrapperType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BenchmarkResultsFormatterCLI {

  private static final Logger  logger           = LoggerFactory
                                                .getLogger(BenchmarkResultsFormatterCLI.class);

  protected final OptionParser parser;
  protected OptionSet          opts;

  public static final String   HELP             = "help";

  public static final String   INDEX_WRAPPER    = "index";
  public static final String   INDEX_DIR        = "index-dir";
  public static final String   Q_RESULTS_DIR    = "q-results-dir";
  public static final String   CAPTION          = "caption";
  public static final String   ROUND            = "round";
  public static final String   QUERY_SPEC_URL   = "query-spec-url";
  public static final String   FORMATTER_TYPE   = "formatter-type";
  public static final String   QUERY_SPEC_REGEX = "query-spec-regex";
  public static final String   WITHOUT_WARM     = "without-warm";
  public static final String   WITHOUT_COLD     = "without-cold";
  public static final String   OUTPUT           = "output";

  public BenchmarkResultsFormatterCLI() {
    parser = new OptionParser();
    parser.accepts(HELP, "print this help");
    parser.accepts(INDEX_WRAPPER, "The type of index wrapper to use: "
        + Arrays.toString(IndexWrapperType.values()) + ". If it is not specified," +
        " it is expected that there are subfolders with the Index names.")
        .withRequiredArg().ofType(IndexWrapperType.class);
    parser.accepts(Q_RESULTS_DIR, "The path to the query results folder")
          .withRequiredArg().ofType(File.class);
    parser.accepts(INDEX_DIR, "The path to the index folders.")
          .withRequiredArg().ofType(File.class);
    parser.accepts(CAPTION, "Add a caption to the table")
          .withRequiredArg().ofType(String.class);
    parser.accepts(ROUND, "Round measure values")
          .withRequiredArg().ofType(Integer.class).defaultsTo(2);
    parser.accepts(QUERY_SPEC_REGEX, "Specify a regular expression to keep only desired query specs")
          .withRequiredArg().ofType(String.class);
    parser.accepts(WITHOUT_WARM, "Remove WARM Cache results");
    parser.accepts(WITHOUT_COLD, "Remove COLD Cache results");
    parser.accepts(OUTPUT, "The output file the table is writen to. " +
                           "If not specified, it is printed to the Std Out")
          .withRequiredArg().ofType(String.class);
    parser.accepts(QUERY_SPEC_URL, "The URL to use as an ankor link for the query" +
           " specification. If it contains \"{}\", it is replaced by the folder name.")
           .withRequiredArg().ofType(String.class);
    parser.accepts(FORMATTER_TYPE, "The formatter to export benchmark results with: " +
          Arrays.toString(FormatterType.values()))
          .withRequiredArg().ofType(FormatterType.class).defaultsTo(FormatterType.HTML);
  }

  public final void parseAndExecute(final String[] cmds) throws Exception {
    opts = parser.parse(cmds);
    if (opts.has(HELP)) {
      parser.printHelpOn(System.out);
      return;
    }

    // Index wrapper
    IndexWrapperType wrapperType = null;
    if (opts.has(INDEX_WRAPPER)) {
      wrapperType = (IndexWrapperType) opts.valueOf(INDEX_WRAPPER);
    }
    // Q_RESULTS_DIR
    File qResultsDir = null;
    if (opts.has(Q_RESULTS_DIR)) {
      qResultsDir = (File) opts.valueOf(Q_RESULTS_DIR);
    } else {
      parser.printHelpOn(System.err);
      throw new IllegalArgumentException("You must specify the path to the " +
        "query results directory");
    }
    // INDEX_DIR
    File indexDir = null;
    if (opts.has(INDEX_DIR)) {
      indexDir = (File) opts.valueOf(INDEX_DIR);
    } else {
      parser.printHelpOn(System.err);
      throw new IllegalArgumentException("You must specify the path to the " +
        "index directory");
    }
    // QUERY_SPEC_REGEX
    String regex = null;
    if (opts.has(QUERY_SPEC_REGEX)) {
      regex = (String) opts.valueOf(QUERY_SPEC_REGEX);
    }
    // OUTPUT
    Writer output = null;
    if (opts.has(OUTPUT)) {
      output = new FileWriter((String) opts.valueOf(OUTPUT));
    } else {
      output = new PrintWriter(System.out);
    }

    final BenchmarkResultsIterator it = new BenchmarkResultsIterator(wrapperType, indexDir, qResultsDir, regex);
    // WARM CACHE
    if (opts.has(WITHOUT_COLD)) {
      it.setWithCold(false);
    }
    // COLD CACHE
    if (opts.has(WITHOUT_WARM)) {
      it.setWithWarm(false);
    }
    // FORMATTER_TYPE
    final FormatterType ft = (FormatterType) opts.valueOf(FORMATTER_TYPE);

    logger.info("Exporting results from {} using formatter={}", qResultsDir, ft);
    final BenchmarkResultsFormatter formatter = BenchmarkResultsFormatterFactory.getFormatter(ft, output, it);
    // ROUND
    formatter.setRound((Integer) opts.valueOf(ROUND));
    // QUERY_SPEC_REGEX
    if (opts.has(QUERY_SPEC_URL)) {
      formatter.setQuerySpecUrl((String) opts.valueOf(QUERY_SPEC_URL));
    }
    // CAPTION
    if (opts.has(CAPTION)) {
      formatter.setCaption((String) opts.valueOf(CAPTION));
    }
    formatter.format();
  }

  public static void main(String[] args)
  throws Exception {
    BenchmarkResultsFormatterCLI cli = new BenchmarkResultsFormatterCLI();
    cli.parseAndExecute(args);
  }

}
