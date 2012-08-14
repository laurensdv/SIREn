package org.sindice.siren.benchmark.generator.viz.index;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.sindice.siren.benchmark.generator.viz.AbstractFormatter;
import org.sindice.siren.benchmark.generator.viz.BenchmarkResults;

public abstract class IndexFormatter
extends AbstractFormatter {

  protected final ArrayList<BenchmarkResults> brIndexList = new ArrayList<BenchmarkResults>();

  @Override
  public void collect(BenchmarkResults br) {
    brIndexList.add(br);
  }

  @Override
  public List<BenchmarkResults> getSortedList() {
    Collections.sort(brIndexList, new Comparator<BenchmarkResults>() {
      @Override
      public int compare(BenchmarkResults o1, BenchmarkResults o2) {
        return o1.getDirectoryName().compareTo(o2.getDirectoryName());
      }
    });
    return brIndexList;
  }

}
