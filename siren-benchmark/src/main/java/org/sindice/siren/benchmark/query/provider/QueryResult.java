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
package org.sindice.siren.benchmark.query.provider;

public class QueryResult {

  private long cpuTime;
  private long userTime;
  private long systemTime;

  private int  nHits;

  private long bytesRead;

  public QueryResult(final long cpuTime, final int nHits) {
    this.setCPUTime(cpuTime);
    this.setHits(nHits);
  }

  public QueryResult(final long cpuTime, final long userTime,
                     final long systemTime, final int nHits) {
    this.setCPUTime(cpuTime);
    this.setUserTime(userTime);
    this.setSystemTime(systemTime);
    this.setHits(nHits);
  }

  public QueryResult(final long cpuTime, final long userTime,
                     final long systemTime, final int nHits, final long bytesRead) {
    this.setCPUTime(cpuTime);
    this.setUserTime(userTime);
    this.setSystemTime(systemTime);
    this.setHits(nHits);
    this.setBytesRead(bytesRead);
  }

  public void setCPUTime(final long cpuTime) {
    this.cpuTime = cpuTime;
  }

  public long getCPUTime() {
    return this.cpuTime;
  }

  public void setUserTime(final long userTime) {
    this.userTime = userTime;
  }

  public long getUserTime() {
    return this.userTime;
  }

  public void setSystemTime(final long systemTime) {
    this.systemTime = systemTime;
  }

  public long getSystemTime() {
    return this.systemTime;
  }

  public void setHits(final int nHits) {
    this.nHits = nHits;
  }

  public int getHits() {
    return this.nHits;
  }

  public void setBytesRead(final long bytesRead) {
    this.bytesRead = bytesRead;
  }

  public long getBytesRead() {
    return this.bytesRead;
  }

  @Override
  public final String toString() {
    final StringBuilder builder = new StringBuilder();
    builder.append("cpu=");
    builder.append(this.cpuTime);
    builder.append(",user=");
    builder.append(this.userTime);
    builder.append(",system=");
    builder.append(this.systemTime);
    builder.append(",hits=");
    builder.append(this.nHits);
    builder.append(",bytes=");
    builder.append(this.bytesRead);
    return builder.toString();
  }

}
