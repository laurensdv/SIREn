/**
 * Copyright (c) 2009-2011 Sindice Limited. All Rights Reserved. Project and
 * contact information: http://www.siren.sindice.com/ This file is part of the
 * SIREn project. SIREn is a free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version. SIREn is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details. You should have received a
 * copy of the GNU Affero General Public License along with SIREn. If not, see
 * <http://www.gnu.org/licenses/>.
 */
/**
 * @project siren
 * @author Renaud Delbru [ 10 Dec 2009 ]
 * @link http://renaud.delbru.fr/
 * @copyright Copyright (C) 2009 by Renaud Delbru, All rights reserved.
 */
package org.sindice.siren.search.primitive;

import org.apache.lucene.util.PriorityQueue;

/**
 * Code taken from {@link PhraseQueue} and adapted for the Siren use case.
 */
final class SirenPhraseQueue
extends PriorityQueue<SirenPhrasePositions> {

  SirenPhraseQueue(final int size) {
    super(size);
  }

  @Override
  protected final boolean lessThan(final SirenPhrasePositions pp1,
                                   final SirenPhrasePositions pp2) {
    assert pp1.node().length == pp2.node().length;
    
    if (pp1.doc() == pp2.doc()) {
      for (int i = 0; i < pp1.node().length; i++) {
        if (pp1.node()[i] != pp2.node()[i]) {
          return pp1.node()[i] < pp2.node()[i];
        }
      }
      if (pp1.pos() == pp2.pos()) {
        // same entity and pp.pos(), so decide by actual term positions.
        // rely on: pp.pos() == termPosition.getPosition() - offset.
        return pp1.offset() < pp2.offset();
      } else {
        return pp1.pos() < pp2.pos();
      }
    } else {
      return pp1.doc() < pp2.doc();
    }
  }
}
