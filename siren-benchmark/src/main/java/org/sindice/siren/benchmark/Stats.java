/**
 * Copyright (c) 2009-2012 National University of Ireland, Galway. All Rights Reserved.
 *
 * Project and contact information: http://www.siren.sindice.com/
 *
 * This file is part of the SIREn project and is derived from the "benchmarking
 * framework" of Elliptic Group, Inc. You can find the original source code on
 * <http://www.ellipticgroup.com/html/benchmarkingArticle.html>.
 *
 * This file is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This file is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * @project siren-benchmark
 * @author Renaud Delbru [ 6 Jul 2012 ]
 * @link http://renaud.delbru.fr/
 */
package org.sindice.siren.benchmark;

/**
* Holds execution time statistics.
* <p>
* Every time field has these features:
* <ol>
*  <li>Units: seconds</li>
*  <li>Contract: passes {@link #checkTimeValue checkTimeValue}</li>
* </ol>
*/
public class Stats {

  /** Arithmetic mean of the execution time. */
  protected double mean;

  /** Lower bound for {@link #mean}'s confidence interval. */
  protected double meanLower;

  /** Upper bound for the {@link #mean}'s confidence interval. */
  protected double meanUpper;

  /** Standard deviation of the execution time. */
  protected double sd;

  /** Lower bound for the {@link #sd standard deviation}'s confidence interval. */
  protected double sdLower;

  /** Upper bound for the {@link #sd standard deviation}'s confidence interval. */
  protected double sdUpper;

  /**
  * Records any issues with the standard deviation.
  * <p>
  * Contract: either <code>null</code> else is nonblank.
  */
  protected String sdIssues;

  /**
  * Constructor.
  * <p>
  * @throws IllegalStateException if any parameter violates the contract of the field it is assigned to
  */
  public Stats(final double mean, final double meanLower, final double meanUpper, final double sd, final double sdLower, final double sdUpper, final String sdIssues) throws IllegalStateException {
    this.checkTimeValue(mean, "mean");
    this.checkTimeValue(meanLower, "meanLower");
    this.checkTimeValue(meanUpper, "meanUpper");
    this.checkTimeValue(sd, "sd");
    this.checkTimeValue(sdLower, "sdLower");
    this.checkTimeValue(sdUpper, "sdUpper");
    if ((sdIssues != null) && (sdIssues == "")) {
      throw new IllegalStateException("sdIssues != null but is blank");
    }

    this.mean = mean;
    this.meanLower = meanLower;
    this.meanUpper = meanUpper;
    this.sd = sd;
    this.sdLower = sdLower;
    this.sdUpper = sdUpper;
    this.sdIssues = sdIssues;
  }

  /**
  * Checks that t is a valid time value.
  * <p>
  * @throws IllegalStateException if t is NaN, infinite, or < 0
  */
  protected void checkTimeValue(final double t, final String name) throws IllegalStateException {
    if (Double.isNaN(t)) throw new IllegalStateException(name + " = " + t + " is NaN");
    if (Double.isInfinite(t)) throw new IllegalStateException(name + " = " + t + " is infinite");
    if (t < 0) throw new IllegalStateException(name + " = " + t + " < 0");
  }

  /** Accessor for {@link #mean}. */
  public double getMean() { return mean; }

  /** Accessor for {@link #meanLower}. */
  public double getMeanLower() { return meanLower; }

  /** Accessor for {@link #meanUpper}. */
  public double getMeanUpper() { return meanUpper; }

  /** Accessor for {@link #sd}. */
  public double getSd() { return sd; }

  /** Accessor for {@link #sdLower}. */
  public double getSdLower() { return sdLower; }

  /** Accessor for {@link #sdUpper}. */
  public double getSdUpper() { return sdUpper; }

  /** Accessor for {@link #sdIssues}. */
  public String getSdIssues() { return sdIssues; }

  /**
  * Calculates action statistics from block statistics.
  * <i>This method should only be called if this instance does, in fact, represent block statistics.</i>
  * <p>
  * See the "Block statistics versus action statistics" section of the
  * <a href="http://www.ellipticgroup.com/html/benchmarkingArticle.html">article supplement</a> for more details.
  * <p>
  * @throws IllegalArgumentException if a <= 0
  */
  public Stats forActions(final long a) throws IllegalArgumentException {
    if (a <= 0) {
      throw new IllegalArgumentException();
    }

    final double meanFactor = 1.0 / a;
    final double sdFactor = 1.0 / Math.sqrt(a);
    return new Stats(
      this.getMean() * meanFactor, this.getMeanLower() * meanFactor, this.getMeanUpper() * meanFactor,
      this.getSd() * sdFactor, this.getSdLower() * sdFactor, this.getSdUpper() * sdFactor,
      this.diagnoseSdOfActions(a)
    );
  }

  /**
  * See the "Standard deviation outlier model" section of the
  * <a href="http://www.ellipticgroup.com/html/benchmarkingArticle.html">article supplement</a> for more details.
  */
  protected String diagnoseSdOfActions(final double a) {  // CRITICAL: note that I am converting "a" from a long to a double in order to ensure that all math below is carried out in double precision, because I have seen errors with integer arithmetic spilling over into negative values when a gets large enough otherwise
    if (a < 16) return null;

      // calculate some of the key quantities
    final double muB = this.getMean();
    final double sigmaB = this.getSd();
    if (sigmaB == 0) return null;

    final double muA = muB / a;
    final double sigmaA = sigmaB / Math.sqrt(a);

    final double tMin = 0;
    final double muGMin = (muA + tMin) / 2;
    assert (muGMin >= 0) : "muGMin = " + muGMin + " < 0" + "; getMean() = " + this.getMean() + ", getSd() = " + this.getSd() + ", a = " + a;
    final double sigmaG = Math.min( (muGMin - tMin) / 4, sigmaA );
    assert (sigmaG >= 0) : "sigmaG = " + sigmaG + " < 0" + "; getMean() = " + this.getMean() + ", getSd() = " + this.getSd() + ", a = " + a;

      // calculate cMax:
    final long cMax1 = this.cMaxSolver(a, muB, sigmaB, muA, sigmaG, tMin);
    final long cMax2 = this.cMaxSolver(a, muB, sigmaB, muA, sigmaG, muGMin);
    final long cMax = Math.min(cMax1, cMax2);
    if (cMax == 0) return null;

      // calculate the minimum variance caused by the outliers:
    final double var1 = this.varianceOutliers(a, sigmaB, sigmaG, 1);
    final double var2 = this.varianceOutliers(a, sigmaB, sigmaG, cMax);
    long cOutMin;
    double varOutMin;
    if (var1 < var2) {
      cOutMin = 1L;
      varOutMin = var1;
    }
    else {
      cOutMin = cMax;
      varOutMin = var2;
    }

      // calculate muG and U at cOutMin just for the diagnostics below
    final double varBG_outMin = (sigmaB * sigmaB) - ((a - cOutMin) * (sigmaG * sigmaG));
    final double muG_outMin = muA - Math.sqrt( (cOutMin * varBG_outMin) / (a * (a - cOutMin)) );
    assert (muA > muG_outMin) : "muA = " + muA + " <= muG_outMin = " + muG_outMin + "; getMean() = " + this.getMean() + ", getSd() = " + this.getSd() + ", a = " + a;
    assert (muG_outMin > 0) : "muG_outMin = " + muG_outMin + " <= 0" + "; getMean() = " + this.getMean() + ", getSd() = " + this.getSd() + ", a = " + a;
    final double U_outMin = muA + Math.sqrt( ((a - cOutMin) * varBG_outMin) / (a * cOutMin) );
    assert (U_outMin > muA) : "U_outMin = " + U_outMin + " <= muA = " + muA + "; getMean() = " + this.getMean() + ", getSd() = " + this.getSd() + ", a = " + a;

      // issue warning if outlier variance contribution exceeds 1%:
    final double fractionVarOutlierMin = varOutMin / (sigmaB * sigmaB);
    assert (fractionVarOutlierMin >= 0) : "fractionVarOutlierMin = " + fractionVarOutlierMin + " < 0" + "; getMean() = " + this.getMean() + ", getSd() = " + this.getSd() + ", a = " + a;
    String msg;
    if (fractionVarOutlierMin < 0.01) msg = null;
    else if (fractionVarOutlierMin < 0.10) msg = "might be somewhat inflated";
    else if (fractionVarOutlierMin < 0.50) msg = "likely INFLATED";
    else msg = "ALMOST CERTAINLY GROSSLY INFLATED";
    if (msg != null) return
      "--action sd values " + msg + " by outliers" + "\n" +
        "--they cause at least " + (100 * fractionVarOutlierMin) + "% of the measured VARIANCE according to a equi-valued outlier model" + "\n" +
        "--model quantities: a = " + a + ", muB = " + muB + ", sigmaB = " + sigmaB + ", muA = " + muA + ", sigmaA = " + sigmaA +
          ", tMin = " + tMin + ", muGMin = " + muGMin + ", sigmaG = " + sigmaG +
          ", cMax1 = " + cMax1 + ", cMax2 = " + cMax2 + ", cMax = " + cMax +
          ", cOutMin = " + cOutMin + ", varOutMin = " + varOutMin +
          ", muG(cOutMin) = " + muG_outMin + ", U(cOutMin) = " + U_outMin;

    return null;
  }

  /**
  * See the "Computer algorithm" subsection of the
  * <a href="http://www.ellipticgroup.com/html/benchmarkingArticle.html">article supplement</a> for more details.
  */
  protected long cMaxSolver(final double a, final double muB, final double sigmaB, final double muA, final double sigmaG, final double x) {
    final double muA_minus_x = muA - x;
    final double k2 = sigmaG * sigmaG;
    final double k1 = (sigmaB * sigmaB) - (a * sigmaG * sigmaG) + (a * muA_minus_x * muA_minus_x);
    final double k0 = -a * a * muA_minus_x * muA_minus_x;
    final double determinant = (k1 * k1) - (4 * k2 * k0);
    final long cMax = (long) Math.floor( -2 * k0 / (k1 + Math.sqrt(determinant)) );
    assert (cMax >= 0) : "cMax = " + cMax + " < 0" + "; getMean() = " + this.getMean() + ", getSd() = " + this.getSd() + ", a = " + a;
    if (cMax > 1) {
      assert ((k2 * cMax * cMax) + (k1 * cMax) + k0 < 0) : "calculated cMax = " + cMax + ", but the inequality fails at cMax" + "; getMean() = " + this.getMean() + ", getSd() = " + this.getSd() + ", a = " + a;
      assert ((k2 * (cMax + 1) * (cMax + 1)) + (k1 * (cMax + 1)) + k0 >= 0) : "calculated cMax = " + cMax + ", but the inequality succeeds at cMax + 1" + "; getMean() = " + this.getMean() + ", getSd() = " + this.getSd() + ", a = " + a;
    }
    return cMax;
  }

  /**
  * See the Equation (45) of the
  * <a href="http://www.ellipticgroup.com/html/benchmarkingArticle.html">article supplement</a> for more details.
  */
  protected double varianceOutliers(final double a, final double sigmaB, final double sigmaG, final double c) {
    return  ((a - c) / a) * ((sigmaB * sigmaB) - ((a - c) * (sigmaG * sigmaG)));
  }

  @Override
  public String toString() {
    final java.util.Formatter formatter = new java.util.Formatter();

    formatter.format("mean = %1.3e %2s", this.getMean(), this.toStringCi(this.getMean(), this.getMeanLower(), this.getMeanUpper()));
    formatter.format(", sd = %1.3e %2s", this.getSd(), this.toStringCi(this.getSd(), this.getSdLower(), this.getSdUpper()));

    return formatter.toString();
  }

  /** Returns a String description of the confidence interval specified by the parameters. */
  protected String toStringCi(final double d, final double lower, final double upper) {
    final java.util.Formatter formatter = new java.util.Formatter();

    if ((lower <= d) && (d <= upper)) { // usual case
      final double diffLower = d - lower;
      final double diffUpper = upper - d;
      final double diffMax = Math.max(diffLower, diffUpper);
      final double diffMin = Math.min(diffLower, diffUpper);
      final double asymmetry = (diffMax - diffMin) / diffMin;
      if (asymmetry <= 1e-3) {  // lower and upper form an approximately symmetric interval about d, then use simpler combined +- form
        formatter.format("(CI deltas: +-%1.3e)", diffLower);
        return formatter.toString();
      }
      else {  // is distinctly asymmetric, must use separate - and + form
        formatter.format("(CI deltas: -%1.3e, +%2.3e)", diffLower, diffUpper);
        return formatter.toString();
      }
    }
    else {  // weird case: d is outside the interval!
      formatter.format("(CI: [%1.3e, %2.3e])", lower, upper);
      return formatter.toString();
    }
  }

}
