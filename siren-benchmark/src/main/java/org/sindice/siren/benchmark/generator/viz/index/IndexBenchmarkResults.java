package org.sindice.siren.benchmark.generator.viz.index;

import org.sindice.siren.benchmark.generator.viz.BenchmarkResults;

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
