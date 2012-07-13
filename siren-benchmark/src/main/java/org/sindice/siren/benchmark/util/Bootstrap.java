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
package org.sindice.siren.benchmark.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import JSci.maths.statistics.NormalDistribution;

/**
* Performs statistical bootstrap calculations.
* <p>
* Concerning the quality of the results: bootstrapping is no magic bullet.
* So, if you supply garbage inputs, the outputs are also garbage.
* One example is insufficient data: the
* {@link #Bootstrap(double[], int, double, Estimator[])} fundamental constructor
* will accept a sample which has but one element in it, but the statistical
* results that are calculated are almost certainly worthless.
* <p>
* This class is multithread safe: it is immutable (both its immediate state, as
* well as the deep state of its fields).
* <p>
* @author Brent Boyer
*/
public class Bootstrap {

  /**
  * Default value for {@link #numberResamples}.
  * Its value of 100,000 was recommended by Tim Hesterberg (private communication)
  * as providing the best balance between high accuracy and reasonable computation time.
  */
  public static final int numberResamples_default = 100 * 1000;

  /**
  * Default value for {@link #confidenceLevel}.
  * Its value of 0.95 is the usual 95% confidence level used in statistics.
  */
  private static final double confidenceLevel_default = 0.95;

  /**
  * Default value for the estimators param of the fundamental constructor.
  * Its value is the mean, median, and standard deviation estimators typically used in statistics.
  */
  private static final Estimator[] estimators_default = new Estimator[] {new EstimatorMean(), new EstimatorSd()};

  private final double[] sample;

  private final int numberResamples;

  private final double confidenceLevel;

  private final ConcurrentHashMap<Estimator,Estimate> estimatorToEstimate;

  private final Logger logger = LoggerFactory.getLogger(Bootstrap.class);

  /**
  * Convenience constructor that simply calls
  * <code>{@link #Bootstrap(double[], int) this}(sample, {@link #numberResamples_default})</code>.
  * <p>
  * @throws IllegalArgumentException if sample == null; sample.length == 0
  */
  public Bootstrap(final double[] sample) throws IllegalArgumentException {
    this(sample, numberResamples_default);
  }

  /**
  * Convenience constructor that simply calls
  * <code>{@link #Bootstrap(double[], int, double) this}(sample, numberResamples, {@link #confidenceLevel_default})</code>.
  * <p>
  * @throws IllegalArgumentException if sample == null; sample.length == 0
  */
  public Bootstrap(final double[] sample, final int numberResamples) throws IllegalArgumentException {
    this(sample, numberResamples, confidenceLevel_default);
  }

  /**
  * Convenience constructor that simply calls
  * <code>{@link #Bootstrap(double[], int, double, Estimator[]) this}(sample, numberResamples, confidenceLevel, {@link #estimators_default})</code>.
  * <p>
  * @throws IllegalArgumentException if sample == null; sample.length == 0; numberResamples < 1;
  * confidenceLevel <= 0 or confidenceLevel >= 1
  */
  public Bootstrap(final double[] sample, final int numberResamples, final double confidenceLevel) throws IllegalArgumentException {
    this(sample, numberResamples, confidenceLevel, estimators_default);
  }

  /**
  * Fundamental constructor.
  * <p>
  * @throws IllegalArgumentException if sample == null; sample.length < 1;
  * numberResamples < 1; confidenceLevel <= 0 or confidenceLevel >= 1;
  * estimators == null; estimators.length == 0; the names of estimators fail to
  * all be unique
  */
  public Bootstrap(final double[] sample, final int numberResamples, final double confidenceLevel, final Estimator... estimators) throws IllegalArgumentException {
    if (sample == null || sample.length == 0 || numberResamples < 1) {
      throw new IllegalArgumentException();
    }
    if ((confidenceLevel <= 0) || (confidenceLevel >= 1) || Double.isNaN(confidenceLevel)) {
      throw new IllegalArgumentException("confidenceLevel = " + confidenceLevel + " is an illegal value");
    }
    if (estimators == null || estimators.length == 0) {
      throw new IllegalArgumentException();
    }
    final Set<String> estimatorNames = new HashSet<String>();
    for (final Estimator estimator : estimators) {
      estimatorNames.add(estimator.getName());
    }
    if (estimatorNames.size() < estimators.length) {
      throw new IllegalArgumentException("estimatorNames.size() = " +
        estimatorNames.size() + " < estimators.length = " + estimators.length +
        " (i.e. there is duplication of Estimator names)");
    }

    this.sample = sample;
    this.numberResamples = numberResamples;
    this.confidenceLevel = confidenceLevel;
    this.estimatorToEstimate = this.calcEstimates(estimators);
  }

  private ConcurrentHashMap<Estimator,Estimate> calcEstimates(final Estimator[] estimators) {
    return this.calcEstimates_BCa(estimators);
  }

  /**
  * Performs a bootstrap calculation, determining one {@link Estimate} for each
  * element of estimators. The Bias Corrected accelerated (BCa) bootstrap
  * percentile method is used to calculate the confidence intervals.
  * See pp. 185ff of "An Introduction to the Bootstrap", B. Efron and R.
  * Tibshirani, Chapman and Hall, 1993 for a description of the calculation
  * technique.
  */
  private ConcurrentHashMap<Estimator,Estimate> calcEstimates_BCa(final Estimator[] estimators) {
    final Map<Estimator,double[]> resampleMap = this.doResampling(estimators);

    logger.info("calculating an Estimate for each Estimator...");
    final ConcurrentHashMap<Estimator,Estimate> resultMap = new ConcurrentHashMap<Estimator,Estimate>();
    final double alpha = 1 - confidenceLevel; // WARNING: my definition of alpha is the normal one, but Efron uses a value that is half of this one
    final NormalDistribution normalDistribution = new NormalDistribution();
    final double z1 = normalDistribution.inverse( alpha / 2 );  // Efron writes this as z^(alpha)
    final double z2 = -z1;  // my z2 is normalDistribution.inverse( 1 - (alpha / 2) ) = -normalDistribution.inverse( alpha / 2 ) = -z1 by a mathematical identity and is what Efron writes as z^(1 - alpha)
    for (final Estimator estimator : estimators) {
      final double point = estimator.calculate(sample);

      if (sample.length == 1) { // must detect this special case here, since the calcAcceleration/calcJackknifeEsts calls below will crash on this case
        resultMap.put( estimator, new Estimate(point, point, point, confidenceLevel) );
        continue;
      }

      final double[] resampleEsts = resampleMap.get(estimator);
          // calculate the bias and acceleration estimates:
      final double b = this.calcBias(point, resampleEsts, normalDistribution); // Efron writes this as z-hat-sub0
      final double a = this.calcAcceleration(estimator); // Efron writes this as a-hat
          // calculate the percentile correction factors; see the 2 formulas in Eq. 14.10
      final double b_z1 = b + z1;
      final double a1 = normalDistribution.cumulative(b + (b_z1 / (1 - (a * b_z1))) );  // Efron writes this as alpha-sub1
      final double b_z2 = b + z2;
      final double a2 = normalDistribution.cumulative(b + (b_z2 / (1 - (a * b_z2))) );  // Efron writes this as alpha-sub2
          // calculate the nearest indices where the CI bounds occur:
      final int indexLower = (int) Math.max( Math.round( a1 * numberResamples ), 0 ); // lower bound of 0 ensures valid array subscript below
      final int indexUpper = (int) Math.min( Math.round( a2 * numberResamples ), numberResamples - 1 ); // upper bound of numberResamples - 1  ensures valid array subscript below

      resultMap.put( estimator, new Estimate(point, resampleEsts[indexLower], resampleEsts[indexUpper], confidenceLevel) );
    }
    return resultMap;
  }

  /**
  * Generates {@link #numberResamples} bootstrap resamples.
  * For each resample, determines one point estimate for each element of estimators.
  * The result is a Map from each Estimator to its array of resampled point estimates.
  * Each array is sorted before return.
  */
  private Map<Estimator,double[]> doResampling(final Estimator[] estimators) {
    logger.info("performing bootstrap resampling...");
    final int length = sample.length;
    final double[] resample = new double[length]; // reuse this array in loop below to save on allocation costs
    final MersenneTwisterFast random = RandomUtil.get();
    final Map<Estimator,double[]> resampleMap = new HashMap<Estimator,double[]>();
    for (int i = 0; i < numberResamples; i++) {
      if (i % 1000 == 0) {
        logger.info("executing bootstrap resample #" + i + "/" + numberResamples);
      }
      for (int j = 0; j < resample.length; j++) {
        resample[j] = sample[ random.nextInt(length) ];
      }
      for (final Estimator estimator : estimators) {
        double[] resampleEsts = resampleMap.get(estimator);
        if (resampleEsts == null) { // lazy initialize the Map
          resampleEsts = new double[numberResamples];
          resampleMap.put(estimator, resampleEsts);
        }
        resampleEsts[i] = estimator.calculate(resample);
      }
    }

    logger.info("sorting bootstrap resamples...");
    for (final Estimator estimator : estimators) {
      Arrays.sort( resampleMap.get(estimator) );
    }

    return resampleMap;
  }


  /**
  * Calculates the bias estimation for a BCa bootstrap.
  * See Eq. 14.14 p. 186 of "An Introduction to the Bootstrap", B. Efron and R.
  * Tibshirani, Chapman and Hall, 1993.
  */
  private double calcBias(final double point, final double[] resampleEsts,
                          final NormalDistribution normalDistribution) {
    int count = 0;
    for (final double d : resampleEsts) count += (d < point) ? 1 : 0;
    final double probability = ((double) count) / numberResamples;
    return normalDistribution.inverse( probability );
  }


  /**
  * Calculates the acceleration estimation for a BCa bootstrap.
  * See Eq. 14.15 p. 186 of "An Introduction to the Bootstrap", B. Efron and R.
  * Tibshirani, Chapman and Hall, 1993.
  */
  private double calcAcceleration(final Estimator estimator) {
    final double[] jackknifeEsts = this.calcJackknifeEsts(estimator);
    final double jackknifeMean = MathUtil.mean(jackknifeEsts);
    double sumOfSquares = 0.0;
    double sumOfCubes = 0.0;
    for (final double d : jackknifeEsts) {
      final double diff = jackknifeMean - d;
      final double diffSquared = diff * diff;
      final double diffCubed = diff * diffSquared;

      sumOfSquares += diffSquared;
      sumOfCubes += diffCubed;
    }
    return sumOfCubes / (6 * Math.pow(sumOfSquares, 1.5));  // NOTE: there is an error in source's formula: he squares only T-bar, when the correct thing to do is square the whole difference e.g. see p. 24 of http://www.meb.ki.se/~aleplo/Compstat/CS05_7.pdf
// +++ Note: this jackknife calculation of a is the usual simple calculation, but more sophisticated and accurate estimates are discussed in Efron as well as "Bootstrap Methods and their Application" by Davison AC, Hinkley DV.
  }

  /**
   * Jackknifes {@link #sample}, calculating a point estimate for each jackknife
   * resample using estimator.
   **/
  private double[] calcJackknifeEsts(final Estimator estimator) {
    final double[] jackknifeEsts = new double[sample.length];
    for (int i = 0; i < jackknifeEsts.length; i++) {
      final double[] jackknifeSample = new double[sample.length - 1];
      int k = 0;
      for (int j = 0; j < sample.length; j++) {
        if (j == i) continue; // this is the jackknife: the ith jackknifeSample skips sample[i]
        jackknifeSample[k++] = sample[j]; // otherwise retain all the other samples
      }
      jackknifeEsts[i] = estimator.calculate(jackknifeSample);
    }
    return jackknifeEsts;
  }

  /**
  * Returns the {@link Estimate} which corresponds to estimator.
  * <p>
  * @throws IllegalArgumentException if estimator == null; if there is no result for it
  */
  public Estimate getEstimate(final Estimator estimator)
  throws IllegalArgumentException {
    if (estimator == null) {
      throw new IllegalArgumentException();
    }

    return this.getEstimate(estimator.getName());
  }


  /**
  * Returns the first {@link Estimate} which corresponds to estimator with the
  * same name as estimatorName.
  * <p>
  * @throws IllegalArgumentException if there is no result for it
  */
  public Estimate getEstimate(final String estimatorName)
  throws IllegalArgumentException {
    for (final Estimator estimator : estimatorToEstimate.keySet()) {
      if (estimatorName.equals(estimator.getName())) {
        return estimatorToEstimate.get(estimator);
      }
    }
    throw new IllegalArgumentException("estimatorName = " + estimatorName +
      " does not correspond to any Estimator that this instance was constructed with");
  }


  // -------------------- Estimator (static inner interface) and common implementations --------------------


  /**
  * Specifies the api for classes that calculate an estimate for a statistic from a sample.
  * <p>
  * Implementations must be multithread safe.
  */
  public static interface Estimator {

    /**
    * Returns the name of the Estimator.
    * <p>
    * Contract: the result is never blank (null or empty).
    */
    String getName();

    /**
    * Calculates a point estimate for the statistic based on sample.
    * <p>
    * @throws IllegalArgumentException if sample is null or zero-length; any element of sample is NaN
    */
    double calculate(double[] sample) throws IllegalArgumentException;

  }

  /**
  * Calculates a point estimate for the population's arithmetic mean from sample.
  * <p>
  * This class is multithread safe: it is stateless.
  */
  public static class EstimatorMean implements Estimator {

    public EstimatorMean() {}

    public String getName() { return "mean"; }

    public double calculate(final double[] sample) throws IllegalArgumentException {
      return MathUtil.mean(sample);
    }

  }

  /**
  * Calculates a point estimate for the population's standard deviation from sample.
  * <p>
  * This class is multithread safe: it is stateless.
  */
  public static class EstimatorSd implements Estimator {

    public EstimatorSd() {}

    public String getName() { return "sd"; }

    public double calculate(final double[] sample) throws IllegalArgumentException {
      return MathUtil.sd(sample);
    }

  }


  // -------------------- Estimate (static inner class) --------------------


  /**
  * Holds a complete (point and interval) estimate for some {@link Estimator}.
  * <p>
  * This class is multithread safe: it is immutable (both its immediate state, as well as the deep state of its fields).
  */
  public static class Estimate {

    /**
    * Records a single value ("point") estimate.
    * This value may or may not be the maximum likelihood estimate (it totally depends on the Estimator used).
    * <p>
    * Contract: may be any value, including NaN (if undefined) or infinity.
    */
    protected final double point;

    /**
    * Is the lower bound (start of the confidence interval) of the estimate.
    * <p>
    * Contract: may be any value, including NaN (if undefined) or infinity.
    * However, if NaN, then {@link #upper} must also be NaN,
    * and if not NaN, then upper must also be not NaN and lower must be <= upper.
    */
    protected final double lower;

    /**
    * Is the upper bound (end of the confidence interval) of the estimate.
    * <p>
    * Contract: same as {@link #lower}.
    */
    protected final double upper;

    /**
    * Specifies the confidence level of the confidence intervals.
    * <p>
    * Units: none; is a dimensionless fractional number in the range (0, 1).
    * <p>
    * Note: the <i>percent</i> confidence level is 100 times this quantity.
    * <p>
    * Contract: must be inside the open interval (0, 1), and is never NaN or infinite.
    * <p>
    * @see <a href="http://en.wikipedia.org/wiki/Confidence_level">article on confidence intervals</a>
    */
    protected final double confidenceLevel;

    public Estimate(final double point, final double lower, final double upper, final double confidenceLevel) throws IllegalArgumentException {
      if (Double.isNaN(lower)) {
        if (!Double.isNaN(upper)) {
          throw new IllegalArgumentException("lower is NaN, but upper = " + upper + " != NaN");
        }
      }
      else {
        if (Double.isNaN(upper)) {
          throw new IllegalArgumentException("lower = " + lower + " != NaN, but upper is NaN");
        }
        if (lower > upper) {
          throw new IllegalArgumentException("lower = " + lower + " > upper = " + upper);
        }
      }
      if ((confidenceLevel <= 0) || (confidenceLevel >= 1) || Double.isNaN(confidenceLevel)) {
        throw new IllegalArgumentException("confidenceLevel = " + confidenceLevel + " is an illegal value");
      }

      this.point = point;
      this.lower = lower;
      this.upper = upper;
      this.confidenceLevel = confidenceLevel;
    }

    public double getPoint() { return point; }
    public double getLower() { return lower; }
    public double getUpper() { return upper; }
    public double getConfidenceLevel() { return confidenceLevel; }

    @Override public String toString() { return point + " CI = [" + lower + ", " + upper + "]"; }

    public boolean confidenceIntervalContains(final double value) throws IllegalArgumentException {
      if (Double.isNaN(value)) {
        throw new IllegalArgumentException("value is NaN");
      }

      return (lower <= value) && (value <= upper);
    }

  }

}


