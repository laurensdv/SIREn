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
 * @project siren-core
 * @author Renaud Delbru [ 25 May 2012 ]
 * @link http://renaud.delbru.fr/
 */
package org.sindice.siren.index;

import java.security.InvalidParameterException;

import org.apache.lucene.index.DocsAndPositionsEnum;
import org.apache.lucene.index.MultiDocsAndPositionsEnum;
import org.sindice.siren.index.codecs.MultiDocsNodesAndPositionsEnum;

public abstract class SirenDocsEnum extends DocsAndPositionsEnum {

  public abstract DocsNodesAndPositionsEnum getDocsNodesAndPositionsEnum();

  public static DocsNodesAndPositionsEnum map(final DocsAndPositionsEnum docsEnum) {
    if (docsEnum instanceof MultiDocsAndPositionsEnum) {
      final MultiDocsAndPositionsEnum multiDocsEnum = (MultiDocsAndPositionsEnum) docsEnum;
      return new MultiDocsNodesAndPositionsEnum(multiDocsEnum.getSubs(), multiDocsEnum.getNumSubs());
    }
    else if (docsEnum instanceof SirenDocsEnum) {
      return ((SirenDocsEnum) docsEnum).getDocsNodesAndPositionsEnum();
    }
    else {
      throw new InvalidParameterException("Unknown DocsAndPositionsEnum received");
    }
  }

}
