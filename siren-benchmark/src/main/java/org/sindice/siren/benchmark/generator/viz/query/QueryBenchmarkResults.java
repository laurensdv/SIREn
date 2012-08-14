package org.sindice.siren.benchmark.generator.viz.query;

import org.sindice.siren.benchmark.Stats;
import org.sindice.siren.benchmark.generator.viz.BenchmarkResults;

public class QueryBenchmarkResults
extends BenchmarkResults {

  private String  querySpec;
  private boolean isWarm;
  private Stats   cpuTime;
  private Stats   userTime;
  private Stats   systemTime;
  private Stats   rate;
  private long    hits;

  public QueryBenchmarkResults(String querySpec,
                               boolean isWarm,
                               Stats cpuTime,
                               Stats userTime,
                               Stats systemTime,
                               Stats rate,
                               long hits) {
    this.isWarm = isWarm;
    this.querySpec = querySpec;
    this.cpuTime = cpuTime;
    this.userTime = userTime;
    this.systemTime = systemTime;
    this.rate = rate;
    this.hits = hits;
  }

  public QueryBenchmarkResults() {
  }

  public boolean isWarm() {
    return isWarm;
  }

  public Stats getCpuTime() {
    return cpuTime;
  }

  /**
   * Can be null
   * 
   * @return The user time {@link Stats}
   */
  public Stats getUserTime() {
    return userTime;
  }

  /**
   * Can be null
   * 
   * @return the system time {@link Stats}
   */
  public Stats getSystemTime() {
    return systemTime;
  }

  public String getQuerySpec() {
    return querySpec;
  }

  public Stats getRate() {
    return rate;
  }

  public long getHits() {
    return hits;
  }

  public void setQuerySpec(String querySpec) {
    this.querySpec = querySpec;
  }

  public void setWarm(boolean isWarm) {
    this.isWarm = isWarm;
  }

  public void setCpuTime(Stats cpuTime) {
    this.cpuTime = cpuTime;
  }

  public void setUserTime(Stats userTime) {
    this.userTime = userTime;
  }

  public void setSystemTime(Stats systemTime) {
    this.systemTime = systemTime;
  }

  public void setRate(Stats rate) {
    this.rate = rate;
  }

  public void setHits(long hits) {
    this.hits = hits;
  }

}
