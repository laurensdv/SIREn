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
 * @author Renaud Delbru [ 8 Jul 2012 ]
 * @link http://renaud.delbru.fr/
 */
package org.sindice.siren.benchmark.generator.document;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SindiceDatasetFetcher {

  protected final Logger logger = LoggerFactory.getLogger(SindiceDatasetFetcher.class);

  public SindiceDatasetFetcher() {}

  /**
   * Fetch a sample of the Sindice dataset to the indicated output directory.
   */
  public void fetch(final File output, final int sampleSize) throws IOException {
    logger.info("Fetching {} archives from Sindice dataset to {}", sampleSize, output);
    final List<String> sample = this.getSample(this.getContents(), sampleSize);
    for (final String url : sample) {
      logger.info("Fetching {}", url);
      this.fetchFile(new URL(url), output);
    }
  }

  /**
   * Fetch one archive to the output directory
   */
  private void fetchFile(final URL url, final File output) throws IOException {
    final int pos = url.toString().lastIndexOf('/');
    final String filename = url.toString().substring(pos + 1);
    FileUtils.copyURLToFile(url, new File(output, filename));
  }

  /**
   * Retrieve and parse the 000-CONTENTS file of the Sindice TREC 2011 dataset.
   * The file contains the URLs of the dataset archives.
   */
  private List<String> getContents() throws IOException {
    final ArrayList<String> contents = new ArrayList<String>(2611);
    final URL url = new URL("http://data.sindice.com/trec2011/resources/sindice-de/full/000-CONTENTS");
    final BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

    try {
      String inputLine;
      while ((inputLine = in.readLine()) != null) {
        if (inputLine.equals("http://data.sindice.com/trec2011/resources/sindice-de/full/000-CONTENTS")) {
          continue;
        }
        contents.add(inputLine.trim());
      }
    }
    finally {
      in.close();
    }
    return contents;
  }

  /**
   * Create a random sample of the URLs of the dataset archives.
   */
  private List<String> getSample(final List<String> contents, final int sampleSize) {
    final Random r = new Random(42);
    final ArrayList<String> sample = new ArrayList<String>(sampleSize);

    for (int i = 0; i < sampleSize; i++) {
      final int k = r.nextInt(contents.size());
      sample.add(i, contents.get(k));
    }

    return sample;
  }

}
