/**
 * @project index-generator
 * @author Renaud Delbru [ 11 Apr 2010 ]
 * @link http://renaud.delbru.fr/
 * @copyright Copyright (C) 2009 by Renaud Delbru, All rights reserved.
 */
package org.sindice.siren.benchmark.util;

import org.sindice.siren.benchmark.util.Bootstrap.Estimator;

public class HarmonicMeanEstimator
implements Estimator {

  @Override
  public double calculate(final double[] sample)
  throws IllegalArgumentException {
    double agg = 0; // agg stands for aggregate
    for (final double element : sample)
      agg += (1 / element);
    return sample.length / agg;
  }

  @Override
  public String getName() { return "harmonic mean"; }

}
