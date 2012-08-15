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
package org.sindice.siren.benchmark.generator.viz.index;

import org.sindice.siren.benchmark.generator.viz.BenchmarkResults;

/**
 * Specfic measures for an Index
 * @author Stephane Campinas [15 Aug 2012]
 * @email stephane.campinas@deri.org
 *
 */
public class IndexBenchmarkResults
extends BenchmarkResults {

  private double commitTime;
  private double optimiseTime;
  private long   docSizeInBytes;
  private long   skpSizeInBytes;
  private long   nodSizeInBytes;
  private long   posSizeInBytes;

  public IndexBenchmarkResults(double commitTime,
                               double optimiseTime,
                               long docSizeInBytes,
                               long skpSizeInBytes,
                               long nodSizeInBytes,
                               long posSizeInBytes) {
    this.commitTime = commitTime;
    this.optimiseTime = optimiseTime;
    this.docSizeInBytes = docSizeInBytes;
    this.skpSizeInBytes = skpSizeInBytes;
    this.nodSizeInBytes = nodSizeInBytes;
    this.posSizeInBytes = posSizeInBytes;
  }

  public IndexBenchmarkResults() {
  }

  public void setCommitTime(double commitTime) {
    this.commitTime = commitTime;
  }

  public void setOptimiseTime(double optimiseTime) {
    this.optimiseTime = optimiseTime;
  }

  public void setDocSizeInBytes(long docSizeInBytes) {
    this.docSizeInBytes = docSizeInBytes;
  }

  public void setSkpSizeInBytes(long skpSizeInBytes) {
    this.skpSizeInBytes = skpSizeInBytes;
  }

  public void setNodSizeInBytes(long nodSizeInBytes) {
    this.nodSizeInBytes = nodSizeInBytes;
  }

  public void setPosSizeInBytes(long posSizeInBytes) {
    this.posSizeInBytes = posSizeInBytes;
  }

  public double getCommitTime() {
    return commitTime;
  }

  public double getOptimiseTime() {
    return optimiseTime;
  }

  public long getDocSizeInBytes() {
    return docSizeInBytes;
  }

  public long getSkpSizeInBytes() {
    return skpSizeInBytes;
  }

  public long getNodSizeInBytes() {
    return nodSizeInBytes;
  }

  public long getPosSizeInBytes() {
    return posSizeInBytes;
  }

}
