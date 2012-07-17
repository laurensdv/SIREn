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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

import org.sindice.siren.benchmark.generator.document.DocumentProviderFactory.DocumentProviderType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TermLexiconGeneratorCLI {

  private final OptionParser parser;
  private OptionSet opts;
  final Logger logger = LoggerFactory.getLogger(TermLexiconGeneratorCLI.class);

  private final String HELP                = "help";
  private final String OUTPUT_DIR          = "output";
  private final String DOCUMENT_PROVIDER   = "document";
  private final String INPUT_DIR           = "input";

  private File outputDir = null;
  private File dataDir = null;
  private DocumentProviderType providerType = null;

  public TermLexiconGeneratorCLI () {
    parser = new OptionParser();
    parser.accepts(HELP, "print this help");
    parser.accepts(OUTPUT_DIR, "The directory where the lexicon files will be " +
    		  "written to.")
          .withRequiredArg().ofType(File.class);
    parser.accepts(DOCUMENT_PROVIDER, "Specify the type of the document: " +
    		  "[Sindice]")
          .withRequiredArg().ofType(DocumentProviderType.class);
    parser.accepts(INPUT_DIR, "Specify the document directory " +
    		  "containing tar.gz files.")
    		  .withRequiredArg().ofType(File.class);
  }

  public void parseAndExecute (final String[] cmds)
  throws IOException {
    opts = parser.parse(cmds);
    if (opts.has(HELP)) {
      parser.printHelpOn(System.out);
      return;
    }

    // OutPut Directory
    if (opts.has(OUTPUT_DIR)) {
      outputDir = (File) opts.valueOf(OUTPUT_DIR);
    }
    else {
      parser.printHelpOn(System.err);
      throw new IllegalArgumentException("You must specify an output " +
      		"directory (--output)");
    }
    // Data Directory
    if (opts.has(INPUT_DIR)) {
      dataDir = (File) opts.valueOf(INPUT_DIR);
      if (!dataDir.exists()) {
        throw new FileNotFoundException("The input directory does not exist");
      }
    }
    else {
      parser.printHelpOn(System.err);
      throw new IllegalArgumentException("You must specify an input " +
          "directory (--input)");
    }
    // Data Provider
    if (opts.has(DOCUMENT_PROVIDER)) {
      providerType = (DocumentProviderType) opts.valueOf(DOCUMENT_PROVIDER);
    }
    else {
      parser.printHelpOn(System.err);
      throw new IllegalArgumentException("You must specify a type of document" +
          " (--document)");
    }

    // Create the lexicons
    final TermLexiconGenerator generator = new TermLexiconGenerator(providerType, dataDir);
    generator.generate(outputDir);
    generator.close();
  }

  public static void main(final String[] args) throws IOException, ClassNotFoundException {
    final TermLexiconGeneratorCLI cmd = new TermLexiconGeneratorCLI();
    cmd.parseAndExecute(args);
  }

}
