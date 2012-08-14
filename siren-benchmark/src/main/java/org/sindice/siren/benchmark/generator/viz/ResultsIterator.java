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
import java.util.Iterator;

import org.apache.commons.lang.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Iterates over the results files of a directory, e.g., query benchmark times
 * or the index computation.
 * @author Stephane Campinas [14 Aug 2012]
 * @email stephane.campinas@deri.org
 *
 */
public abstract class ResultsIterator
implements Iterator<BenchmarkResults> {

  protected static final Logger logger = LoggerFactory.getLogger(ResultsIterator.class);

  /**
   * The directory where the results are
   */
  protected File directory;

  public void init(File directory) {
    this.directory = directory;
    logger.info("Processing directory {}", directory);
  }

  @Override
  public void remove() {
    throw new NotImplementedException();
  }

  /**
   * Throws an exception if one of the files is not a directory
   * @param directories
   */
  protected void checkDirectoriesExists(File...directories) {
    for (File d: directories) {
      if (!d.isDirectory()) {
        logger.error("Missing folder: {}", d);
        throw new VizException();
      }
    }
  }

  /**
   * Throws an exception if one the files is not a normal file.
   * @param files
   */
  protected void checkFilesExists(File...files) {
    for (File f: files) {
      if (!f.isFile()) {
        logger.error("Missing file: {}", f);
        throw new VizException();
      }
    }
  }

}
