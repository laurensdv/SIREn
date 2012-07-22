/**
 * Copyright (c) 2009-2011 Sindice Limited. All Rights Reserved.
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
 * @project siren
 * @author Renaud Delbru [ 8 Feb 2010 ]
 * @link http://renaud.delbru.fr/
 * @copyright Copyright (C) 2009 by Renaud Delbru, All rights reserved.
 */
package org.sindice.siren.search.node;

import static org.sindice.siren.search.AbstractTestSirenScorer.NodeBooleanClauseBuilder.must;
import static org.sindice.siren.search.AbstractTestSirenScorer.NodeBooleanQueryBuilder.nbq;
import static org.sindice.siren.search.AbstractTestSirenScorer.TupleQueryBuilder.tuple;

import java.io.IOException;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.junit.Test;
import org.sindice.siren.index.codecs.RandomSirenCodec.PostingsFormatType;
import org.sindice.siren.util.BasicSirenTestCase;

public class TestBooleanQuery extends BasicSirenTestCase {

  @Override
  protected void configure() throws IOException {
    this.setAnalyzer(AnalyzerType.TUPLE);
    this.setPostingsFormat(PostingsFormatType.RANDOM);
  }

  @Test
  public void testReqTuple() throws CorruptIndexException, IOException {
    for (int i = 0; i < 10; i++) {
      this.addDocument("<subj> <aaa> <bbb> . <subj> <ccc> <ddd> . ");
      this.addDocument("<subj> <aaa> <bbb> . ");
    }

    final Query nested1 = tuple().with(nbq(must("aaa")).bound(1,1))
                                 .with(nbq(must("bbb")).bound(2,2))
                                 .getDocumentQuery();

    final Query nested2 = tuple().with(nbq(must("ccc")).bound(1,1))
                                 .with(nbq(must("ddd")).bound(2,2))
                                 .getDocumentQuery();

    final BooleanQuery q = new BooleanQuery();
    q.add(nested1, Occur.MUST);
    q.add(nested2, Occur.MUST);

    assertEquals(10, searcher.search(q, 10).totalHits);
  }

  @Test
  public void testReqOptTuple() throws CorruptIndexException, IOException {
    for (int i = 0; i < 10; i++) {
      this.addDocument("<subj> <aaa> <bbb> . <subj> <ccc> <ddd> . ");
      this.addDocument("<subj> <aaa> <bbb> . ");
    }

    final Query nested1 = tuple().with(nbq(must("aaa")).bound(1,1))
                                 .with(nbq(must("bbb")).bound(2,2))
                                 .getDocumentQuery();

    final Query nested2 = tuple().with(nbq(must("ccc")).bound(1,1))
                                 .with(nbq(must("ddd")).bound(2,2))
                                 .getDocumentQuery();

    final BooleanQuery q = new BooleanQuery();
    q.add(nested1, Occur.MUST);
    q.add(nested2, Occur.SHOULD);

    assertEquals(20, searcher.search(q, 10).totalHits);
  }

  @Test
  public void testReqExclTuple() throws CorruptIndexException, IOException {
    for (int i = 0; i < 10; i++) {
      this.addDocument("<subj> <aaa> <bbb> . <subj> <ccc> <ddd> . <subj> <eee> <fff> . ");
      this.addDocument("<subj> <aaa> <bbb> . <subj> <ccc> <ddd> . <subj> <eee> <ggg> . ");
    }

    final Query nested1 = tuple().with(nbq(must("eee")).bound(1,1))
                                 .with(nbq(must("ggg")).bound(2,2))
                                 .getDocumentQuery();

    final Query nested2 = tuple().with(nbq(must("aaa")).bound(1,1))
                                 .with(nbq(must("bbb")).bound(2,2))
                                 .getDocumentQuery();

    final Query nested3 = tuple().with(nbq(must("ccc")).bound(1,1))
                                 .with(nbq(must("ddd")).bound(2,2))
                                 .getDocumentQuery();

    final BooleanQuery q = new BooleanQuery();
    q.add(nested1, Occur.MUST_NOT);
    q.add(nested2, Occur.MUST);
    q.add(nested3, Occur.MUST);

    assertEquals(10, searcher.search(q, 10).totalHits);
  }

  @Test
  public void testReqExclCell() throws CorruptIndexException, IOException {
    for (int i = 0; i < 10; i++) {
      this.addDocument("<subj> <aaa> <bbb> . <subj> <ccc> <ddd> . <subj> <eee> <fff> . ");
      this.addDocument("<subj> <aaa> <bbb> . <subj> <ccc> <ddd> . <subj> <eee> <ggg> . ");
    }

    final Query nested1 = nbq(must("aaa")).bound(1,1).getDocumentQuery();
    final Query nested2 = nbq(must("bbb")).bound(2,2).getDocumentQuery();
    final Query nested3 = nbq(must("ggg")).bound(2,2).getDocumentQuery();

    final BooleanQuery q = new BooleanQuery();
    q.add(nested3, Occur.MUST_NOT);
    q.add(nested1, Occur.MUST);
    q.add(nested2, Occur.MUST);

    assertEquals(10, searcher.search(q, 10).totalHits);
  }

}
