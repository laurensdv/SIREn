/**
 * @project index-generator
 * @author Renaud Delbru [ 11 Apr 2010 ]
 * @link http://renaud.delbru.fr/
 * @copyright Copyright (C) 2009 by Renaud Delbru, All rights reserved.
 */
package org.sindice.siren.benchmark;

public class RatesStats extends Stats {

  /**
   * Constructor.
   * <p>
   * @throws IllegalStateException if any parameter violates the contract of the
   * field it is assigned to
   */
   public RatesStats(final double mean, final double meanLower,
                     final double meanUpper, final double sd,
                     final double sdLower, final double sdUpper, final String sdIssues)
   throws IllegalStateException {
     super(mean, meanLower, meanUpper, sd, sdLower, sdUpper, sdIssues);
   }

   @Override
   public String toString() {
     return
       "mean = " + this.getMean() + " q/s " + this.toStringCi(this.getMean(), this.getMeanLower(), this.getMeanUpper()) +
       ", sd = " + this.getSd() + " q/s " + this.toStringCi(this.getSd(), this.getSdLower(), this.getSdUpper());
   }

  @Override
  protected String toStringCi(final double d, final double lower, final double upper) {
     if ((lower <= d) && (d <= upper)) { // usual case
       final double diffLower = d - lower;
       final double diffUpper = upper - d;
       final double diffMax = Math.max(diffLower, diffUpper);
       final double diffMin = Math.min(diffLower, diffUpper);
       final double asymmetry = (diffMax - diffMin) / diffMin;
       if (asymmetry <= 1e-3) {  // lower and upper form an approximately symmetric interval about d, then use simpler combined +- form
         return " (CI deltas: +-" + diffLower + " q/s)";
       }
       else {  // is distinctly asymmetric, must use separate - and + form
         return " (CI deltas: -" + diffLower + " q/s , +" + diffUpper + " q/s)";
       }
     }
     else {  // weird case: d is outside the interval!
       return " (CI: [" + lower + " q/s, " + upper + " q/s])";
     }
   }

}
