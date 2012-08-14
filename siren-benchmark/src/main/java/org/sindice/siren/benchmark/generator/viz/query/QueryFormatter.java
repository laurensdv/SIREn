package org.sindice.siren.benchmark.generator.viz.query;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.sindice.siren.benchmark.generator.viz.AbstractFormatter;
import org.sindice.siren.benchmark.generator.viz.BenchmarkResults;

public abstract class QueryFormatter
extends AbstractFormatter {

  final ArrayList<BenchmarkResults> brQueryList = new ArrayList<BenchmarkResults>();
  final HashMap<String, Integer>    indexes     = new HashMap<String, Integer>();

  @Override
  public void collect(BenchmarkResults br) {
    final String dirName = br.getDirectoryName();

    indexes.put(dirName, indexes.containsKey(dirName) ? indexes.get(dirName) + 1 : 1);
    brQueryList.add(br);
  }

  @Override
  public List<BenchmarkResults> getSortedList() {
    Collections.sort(brQueryList, new Comparator<BenchmarkResults>() {
      @Override
      public int compare(BenchmarkResults o1, BenchmarkResults o2) {
        final int indexCmp;
        final int qSpecCmp;

        if ((indexCmp = o1.getDirectoryName().toString().compareTo(o2.getDirectoryName().toString())) != 0) {
          return indexCmp;
        }
        if ((qSpecCmp = ((QueryBenchmarkResults) o1).getQuerySpec().compareTo(((QueryBenchmarkResults) o2).getQuerySpec())) != 0) {
          return qSpecCmp;
        }
        return ((QueryBenchmarkResults) o1).isWarm() ? 1 : -1;
      }
    });
    return brQueryList;
  }

  @Override
  protected void addBenchmarkResult(Writer out, BenchmarkResults br)
  throws IOException {
    addBenchmarkResult(out, indexes.remove(br.getDirectoryName()), br);
  }

  protected abstract void addBenchmarkResult(final Writer out, Integer nbIndexRes, BenchmarkResults br) throws IOException;

}
