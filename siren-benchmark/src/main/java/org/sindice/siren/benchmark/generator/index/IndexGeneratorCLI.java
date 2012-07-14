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
import java.io.IOException;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

import org.sindice.siren.benchmark.generator.document.DocumentProviderFactory.DocumentProviderType;
import org.sindice.siren.benchmark.wrapper.IndexWrapperFactory.IndexWrapperType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IndexGeneratorCLI {

  private final OptionParser parser;
  private OptionSet          opts;
  final Logger               logger              = LoggerFactory.getLogger(IndexGeneratorCLI.class);

  private final File outputFile = null;
  private IndexWrapperType wrapperType = null;
  private File outputDir = null;
  private DocumentProviderType providerType = null;
  private File inputDir = null;

  private final String HELP                = "help";
  private final String DOCUMENT_PROVIDER   = "document-provider";
  private final String INPUT_DIR           = "input-dir";
  private final String INDEX_WRAPPER       = "index-wrapper";
  private final String OUTPUT_DIR          = "output-dir";

  public IndexGeneratorCLI() {
    parser = new OptionParser();
    parser.accepts(HELP, "Print this help.");
    parser.accepts(DOCUMENT_PROVIDER,
      "Specifiy the type of the document Provider.")
      .withRequiredArg().ofType(DocumentProviderType.class);
    parser.accepts(INPUT_DIR, "Specify the document provider directory " +
      "containing tar.gz files.")
      .withRequiredArg().ofType(File.class);
    parser.accepts(INDEX_WRAPPER, "Specify the class name of Index Wrapper.")
      .withRequiredArg().ofType(IndexWrapperType.class);
    parser.accepts(OUTPUT_DIR, "Specify the output directory where the index " +
    		"and time logs will be generated.")
      .withRequiredArg().ofType(File.class);
  }

  public final void parseAndIndex(final String[] cmds) throws IOException {
    opts = parser.parse(cmds);
    if (opts.has(HELP)) {
      parser.printHelpOn(System.out);
      return;
    }

    // Index directory
    if (opts.has(OUTPUT_DIR)) {
      outputDir = (File) opts.valueOf(OUTPUT_DIR);
    }
    else {
      parser.printHelpOn(System.err);
      throw new IllegalArgumentException("You must specify an output " +
          "directory (--output-dir)");
    }

    // Index wrapper
    if (opts.has(INDEX_WRAPPER)) {
      wrapperType = (IndexWrapperType) opts.valueOf(INDEX_WRAPPER);
    }
    else {
      parser.printHelpOn(System.err);
      throw new IllegalArgumentException("You must specify a index wrapper" +
          " (--index-wrapper)");
    }

    // Input Directory
    if (opts.has(INPUT_DIR)) {
      inputDir = (File) opts.valueOf(INPUT_DIR);
    }
    else {
      parser.printHelpOn(System.err);
      throw new IllegalArgumentException("You must specify an input " +
          "directory (--input-dir)");
    }

    // Data Provider
    if (opts.has(DOCUMENT_PROVIDER)) {
      providerType = (DocumentProviderType) opts.valueOf(DOCUMENT_PROVIDER);
    }
    else {
      parser.printHelpOn(System.err);
      throw new IllegalArgumentException("You must specify a document provider" +
          " (--document-provider)");
    }

    final File indexDir = new File(outputDir, "index");
    final File timeDir = new File(outputDir, "time-logs");

    final IndexGenerator indexGenerator = new IndexGenerator(wrapperType,
      indexDir, providerType, inputDir);

    logger.info("Start index generation using document provider '{}' " +
    		"and index wrapper '{}'", providerType, wrapperType);

    indexGenerator.generateIndex();

    logger.info("Finished index generation");

    indexGenerator.exportCommitTimes(timeDir);
    indexGenerator.exportOptimiseTime(timeDir);

    logger.info("Time logs exported to {}", timeDir);

    indexGenerator.close();
  }

  public static void main(final String[] args)
  throws IOException, ClassNotFoundException {
    final IndexGeneratorCLI cmd = new IndexGeneratorCLI();
    cmd.parseAndIndex(args);
  }

}
