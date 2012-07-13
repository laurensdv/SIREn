/**
 * @project index-generator
 * @author Renaud Delbru [ 4 Apr 2010 ]
 * @link http://renaud.delbru.fr/
 * @copyright Copyright (C) 2009 by Renaud Delbru, All rights reserved.
 */
package org.sindice.siren.benchmark;

import java.util.concurrent.Callable;

public abstract class Task
implements Callable<Measurement> {

  @Override
  public abstract Measurement call() throws Exception;

}
