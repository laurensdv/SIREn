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
package org.sindice.siren.benchmark.query.task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.TotalHitCountCollector;
import org.sindice.siren.benchmark.Measurement;
import org.sindice.siren.benchmark.query.provider.Query;
import org.sindice.siren.benchmark.util.SirenQueryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SirenMultiQueryTask extends MultiQueryTask {

  private final List<org.apache.lucene.search.Query> queries;
  private final SearcherManager mgr;

  protected final Logger logger = LoggerFactory.getLogger(SirenMultiQueryTask.class);

  public SirenMultiQueryTask(final List<Query> queries, final SearcherManager mgr)
  throws IOException {
    super(queries);
    this.mgr = mgr;
    this.queries = new ArrayList<org.apache.lucene.search.Query>(queries.size());
    for (final Query query : queries) {
      logger.debug("Received query: {}", query.toString());
      final org.apache.lucene.search.Query q = SirenQueryUtil.convertQuery(query);
      logger.debug("Converted query into: {}", q.toString());
      this.queries.add(q);
    }
  }

  @Override
  public Measurement call() throws Exception {
    IndexSearcher searcher = this.mgr.acquire();
    int hits = 0;

    try {
      for (final org.apache.lucene.search.Query query : queries) {
        final TotalHitCountCollector collector = new TotalHitCountCollector();
        searcher.search(query, collector);
        hits += collector.getTotalHits();
      }

      return new Measurement(hits);
    }
    finally {
      this.mgr.release(searcher);
      searcher = null;
    }
  }

  @Override
  public void close() throws IOException {}

}