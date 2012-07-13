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


/**
* This class offers additional static mathematical methods beyond the ones
* offered in {@link Math}.
* <p>
* This class is multithread safe: it is immutable (both its immediate state, as
* well as the deep state of its fields).
* <p>
* @see <a href="http://math.nist.gov/javanumerics/#libraries">NIST Java numerics page</a>
* @author Brent Boyer
*/
public class MathUtil {

  // -------------------- constants --------------------

  /**  Stores the value of <code>1 / sqrt(2*pi)</code>. */
  public static final double inverseSqrt2pi = 1.0 / Math.sqrt( 2*Math.PI );

  /** A default value for the errorTolerance param of the {@link #normalize(double[], double) normalize} method. */
  public static final double normalizationErrorTolerance_default = 1e-6;

  // -------------------- low level statistics calculations: mean, sd, variance, sst --------------------

  /**
  * Returns the arithmetic mean of numbers.
  * <p>
  * In the terminology of statistics, if numbers is the population, then the result is the population's mean.
  * But if numbers is merely a sample from the population, then the result is the sample mean,
  * which is an <a href="http://en.wikipedia.org/wiki/Unbiased_estimator">unbiased estimate</a> of the population's mean.
  * <p>
  * Contract: the result is never NaN, but may be infinite.
  * <p>
  * @throws IllegalArgumentException if numbers == null; numbers.length == 0; any element of numbers is NaN
  */
  public static double mean(final double[] numbers) throws IllegalArgumentException {
    if (numbers == null || numbers.length == 0) {
      throw new IllegalArgumentException();
    }

    double sum = 0.0;
    for (final double number : numbers) {
      if (Double.isNaN(number)) {
        throw new IllegalArgumentException();
      }
      sum += number;
    }

    if (Double.isNaN(sum)) {
      throw new IllegalStateException("calculated a NaN sum, but failed to find a NaN element; this should never happen");  // this should never happen, so am not declaring throws IllegalStateException
    }

    return sum / numbers.length;
  }

  /**
  * Returns <code>{@link #sd(double[], boolean) sd}(numbers, true)</code>.
  * <p>
  * <i>Use this version only when mean has not previously been calculated,</i>
  * since this method will internally calculate it.
  * <p>
  * @throws IllegalArgumentException if numbers == null; numbers.length == 0; any element of numbers is NaN
  */
  public static double sd(final double[] numbers) throws IllegalArgumentException {
    return sd(numbers, true);
  }

  /**
  * Returns <code>{@link #sd(double[], double, boolean) sd}(numbers, {@link #mean mean}(numbers), biased)</code>.
  * <p>
  * <i>Use this version only when mean has not previously been calculated,</i>
  * since this method will internally calculate it.
  * <p>
  * @throws IllegalArgumentException if numbers == null; numbers.length == 0; any element of numbers is NaN
  */
  public static double sd(final double[] numbers, final boolean biased) throws IllegalArgumentException {
    return sd(numbers, mean(numbers), biased);
  }

  /**
  * Returns <code>{@link #sd(double[], double, boolean) sd}(numbers, mean, true)</code>.
  * <p>
  * @throws IllegalArgumentException if numbers == null; numbers.length == 0;
  * mean or any element of numbers is NaN
  */
  public static double sd(final double[] numbers, final double mean) throws IllegalArgumentException {
    return sd(numbers, mean, true);
  }

  /**
  * Returns the standard deviation of numbers.
  * <p>
  * In the terminology of statistics, if numbers is the population, then the result is the population's standard deviation.
  * But if numbers is merely a sample from the population, then the result is the sample standard deviation,
  * which is a <a href="http://en.wikipedia.org/wiki/Unbiased_estimator">biased estimate</a> of the population's standard deviation.
  * <p>
  * This method simply returns the square root of {@link #variance(double[], double, boolean) variance}(numbers, mean, biased).
  * <i>Therefore, the mean and biased parameters must have the exact meanings expected by variance.</i>
  * In particular, the biased parameter will control whether or not the <i>variance</i> estimate is biased.
  * <i>It does not control the bias of the standard deviation estimate returned by this method.
  * In fact, the estimate returned by this method is always biased regardless of the value of the biased parameter.</i>
  * The effect of biased == true is that variance will use the "divide by N" rule,
  * while biased == false causes variance to use the "divide by N - 1" rule.
  * <p>
  * A <a href="http://en.wikipedia.org/wiki/Unbiased_estimation_of_standard_deviation">correction</a>
  * exists to get an unbiased estimator for the standard deviation if normality is assumed.
  * This method does not implement this correction, however, for two reasons.
  * First, that unbiased estimator is inferior to the simple "divide by N" estimator:
  * <blockquote>
  * [the "divide by N" estimator has] uniformly smaller mean squared error than does the unbiased estimator,
  * and is the maximum-likelihood estimate when the population is normally distributed.<br/>
  * <a href="http://en.wikipedia.org/wiki/Standard_deviation#Estimating_population_standard_deviation_from_sample_standard_deviation">Reference</a>
  * </blockquote>
  * Second, it is better to avoid assumptions like normality.
  * <p>
  * Summary of the above: use biased == true if want the most accurate result.
  * Only use biased == false if there is some other requirement for it
  * (e.g if are computing confidence intervals, the standard theory which leads to Student's t-distribution
  * was developed using the biased == false, "divide by N - 1", estimator).
  * <p>
  * Contract: the result is always >= 0 (including positive infinity), and is never NaN.
  * <p>
  * @throws IllegalArgumentException if numbers == null; numbers.length == 0; mean or any element of numbers is NaN
  * @see <a href="http://mathworld.wolfram.com/StandardDeviationDistribution.html">Mathworld article on the sample standard deviation distribution</a>
  */
  public static double sd(final double[] numbers, final double mean, final boolean biased) throws IllegalArgumentException {
    return Math.sqrt( variance(numbers, mean, biased) );
  }

  /**
  * Returns <code>{@link #variance(double[], boolean) variance}(numbers, true)</code>.
  * <p>
  * Here, the default value of true for biased is supplied because that yields the most accurate results
  * (see {@link #variance(double[], double, boolean) variance}).
  * <p>
  * <i>Use this version only when mean has not previously been calculated,</i>
  * since this method will internally calculate it.
  * <p>
  * @throws IllegalArgumentException if numbers == null; numbers.length == 0;
  * mean is calculated to be or any element of numbers is NaN
  */
  public static double variance(final double[] numbers) throws IllegalArgumentException {
    return variance(numbers, true);
  }

  /**
  * Returns <code>{@link #variance(double[], double, boolean) variance}(numbers, {@link #mean mean}(numbers), biased)</code>.
  * <p>
  * <i>Use this version only when mean has not previously been calculated,</i>
  * since this method will internally calculate it.
  * <p>
  * @throws IllegalArgumentException if numbers == null; numbers.length == 0;
  * mean is calculated to be or any element of numbers is NaN
  */
  public static double variance(final double[] numbers, final boolean biased) throws IllegalArgumentException {
    return variance(numbers, mean(numbers), biased);
  }

  /**
  * Returns <code>{@link #variance(double[], double, boolean) variance}(numbers, mean, true)</code>.
  * <p>
  * @throws IllegalArgumentException if numbers == null; numbers.length == 0;
  * mean or any element of numbers is NaN
  */
  public static double variance(final double[] numbers, final double mean) throws IllegalArgumentException {
    return variance(numbers, mean, true);
  }

  /**
  * Returns the variance of numbers.
  * <p>
  * Statistically speaking, if numbers is the population, then the result is the population's variance.
  * But if numbers is merely a sample from the population, then the result is an estimate of the population's variance.
  * <p>
  * The mean parameter must be the arithmetic mean of numbers (i.e. what {@link #mean mean}(numbers) returns).
  * <p>
  * Algorithms exist for computing the variance without doing an explicit calculation of the mean.
  * For example, one can compute the sum of the squares of numbers and subtract the square of the sum of numbers,
  * with both sums efficiently calculated in a single loop;
  * see <a href="http://en.wikipedia.org/wiki/Algorithms_for_calculating_variance#Algorithm_I">Algorithm 1</a>.
  * And there is even an algorithm that does not require storing all the elements,
  * and so is good for streaming data; see: <a href="http://mathworld.wolfram.com/SampleVarianceComputation.html">this article</a>
  * <p>
  * Nevertheless, this method requires that the mean parameter be supplied
  * because the most accurate numerical algorithms use differences from the mean
  * (this method relies on {@link #sst(double[], double) sst}; see additional algorithm notes there).
  * <p>
  * The biased parameter determines whether or not the result is a biased or unbiased extimate
  * (assuming that numbers is a sample from a population).
  * Specifically, this method returns <code>{@link #sst(double[], double) sst}(numbers, mean) / denominator</code>.
  * If biased is true, then denominator is numbers.length (i.e. the "divide by N" rule),
  * else denominator is numbers.length - 1 (i.e. the "divide by N - 1" rule).
  * <p>
  * There are a few situations where one should use the unbiased (biased param == false) estimator.
  * The best example is probably the calculation of confidence intervals,
  * because the conventional theory (which leads to Student's t-distribution) was developed using this unbiased variance estimator.
  * <p>
  * In general, however, the biased estimator is more accurate:
  * <pre>
    What is the BEST estimator for the population variance given a sampling of the population?

    The naive estimator formula is simply
      variance = SST / n
    where SST is the sum of the squares of the differences of each sample from the estimated population mean, and n is the number of samples.  But this estimator is biased.

    The usual formula for the unbiased estimator is
      variance = SST / (n - 1)

    Now, here is the trickiness: while unbiasedness is nice, what you really want is a low mean squared error (MSE):
      http://en.wikipedia.org/wiki/Estimator
    In fact, the OPTIMAL estimator is one with the minimum MSE (MMSE):
      http://en.wikipedia.org/wiki/Minimum_mean_squared_error

    Now, this article, says that the
      SST / n
    estimator has a "lower estimation variability"

    http://en.wikipedia.org/wiki/Unbiased_estimator
    where I assume that "lower estimation variability" has the same meaning as "variance of the estimator" in that MSE reference above:
      http://en.wikipedia.org/wiki/Estimator

    In fact, its smaller estimator variance, combined with its negative bias (due to the larger denominator, which causes it to underestime the true variance), actually causes the simple n formula to have lower MSE than the n - 1 one:
      http://en.wikipedia.org/wiki/Mean_squared_error.htm

    That implies the n estimator is better than the n - 1 one.

    But is it the OPTIMAL one--the estimator with MMSE?

    Or is that an unknown at present in statistics?

    This article
      http://cnx.rice.edu/content/m11267/latest/
    has a seemingly related discussion, but the Example 1 that they give seems to be irrelevant (e.g. you generally do NOT know the variance-sub-n quantity, nor the mean and variance of the theta quantities, so his result in formulas 9 or 10 is practically useless).
  * </pre>
<!--
+++ further questions:

--what about the sd estimator?
Here too, the biased one is better than the unbiased sd estimator
  [the "divide by N" estimator has] uniformly smaller mean squared error than does the unbiased estimator,
  and is the maximum-likelihood estimate when the population is normally distributed.<br/>
  http://en.wikipedia.org/wiki/Standard_deviation#Estimating_population_standard_deviation_from_sample_standard_deviation
but what is the OPTIMAL sd est?
  --Note that for estimating the variance, as opposed to the sd, it looks like th edivide by n + 1 formula is not only lower in MSE than the the divide by n - 1 formula but is even lower in MSE than the divide by n formula
    http://en.wikipedia.org/wiki/Mean_squared_error#Examples

--given estimators with a negative bias,
is it possible to set things up so that the negative bias cancels out the positive definite estimator variance
and so you get MSE = 0?
  NO: because the bias enters into the MSE via its square, so it is always non-negative term for the MSE
  http://en.wikipedia.org/wiki/Mean_squared_error

  Note: the wiki reference above also makes the statement:
    The unbiased model with the smallest MSE is generally interpreted as best explaining the variability in the observations.
  WHY do they only consider unbiased estimators?



http://www.wilmott.com/


Stats questions/research ideas:

1) if know the bias of an estimator,
can you simply subtract it to obtain a new (shifted) estimator which is now unbiased
but should have the same variance (and hence lower MSE)?

and what ARE the optimal estimators for the common stats?  or are they not known?
  so far, i just know that the /n is better than /(n - 1), but do not know that it is optimal globally


2) for conf intervals, is it possible that could obtain a narrower interval
if instead of +- the SAME constant from the mean,
you did + of one amount and - of a different amount?

I think that doing +- the same amount is, in fact, optimal for certain assumptions about the probability distribution,
such as that it is symmetric about the mean (e.g. as it is for a Gaussian),
but this will not be true for other asymmetric distributions (e.g. log-normal, chi-square)

Answer: if look at the chi-squared distribution used for conf intervals for sd/var,
it turns out that the standard theory uses asymmetric + and - values from the mean.
Furthermore, at least one person has noted that the standard theory does not give minimum intervals:
  ... the resulting ... confidence interval is not the shortest that can be formed using the available data. The tables and appendixes gives solutions for a and b that yield confidence interval of minimum length for the standard deviation.
  http://cnx.org/content/m13496/latest/


3) if you have multiple estimators, can you combine them somehow to, say, get narrower conf intervals?

for mean estimators, we not only have the n vs n - 1 estimators,
but consider a geometric mean estimator that is the nth root of the product of the n samples;
  --what are the properties of this one?
    --it is nonlinear, so it almost certainly is biased...
  --could one play with the root value to obtain a better quality estimator?
  --could it have better robustness characteristics?

there have to be an infinite number of other estimators,
which may mean that it is impossible to work with the entire space of possible estimators

what possibly may work instead is to come up with an algorithm that takes a set of estimators
and generates a new estimator that is some function of the original set but is guaranteed to be a better one;
then you could add that new one back to the set and repeat the process?


4) THIS ARTICLE HAS SOME INTERESTING THOUGHTS, such as what he calls "ANTI-biased" estimators, which he achieves by leaving out samples
  http://www.mathpages.com/home/kmath497.htm
Makes me think: is there any way that you could show that you can get a better estimator using more complicated calculations?

What about doing n jacknife estimates (where each variance_i is calculated using some other estimator but with x_i left out) and then using all n of the results somehow to prove bounds on the population variance?
Maybe would need to use nonlinear operations like Maximum(variance_i) and/or Minimum(variance_i)


articles:
  Can the Sample Variance Estimator Be Improved by Using a Covariate?
  http://www.ingentaconnect.com/content/asa/jabes/2002/00000007/00000002/art00003

  A Note on an Estimator for the Variance That Utilizes the Kurtosis
  http://www.jstor.org/pss/2684353


outlines how to derive that the / n variance estimator is biased:
  http://www.ds.unifi.it/VL/VL_EN/sample/sample4.html
  http://www.math.uah.edu/stat/sample/Variance.xhtml

-->
  * <p>
  * Contract: the result is always >= 0 (including positive infinity), and is never NaN.
  * <p>
  * @throws IllegalArgumentException if numbers == null; numbers.length == 0; mean or any element of numbers is NaN
  * @see <a href="http://mathworld.wolfram.com/SampleVariance.html">Mathworld article on sample variance</a>
  * @see <a href="http://en.wikipedia.org/wiki/Unbiased_estimator">Wikipedia article on unbiased estimators</a>
  */
  public static double variance(final double[] numbers, final double mean, final boolean biased) throws IllegalArgumentException {
    final double denominator = biased ? numbers.length : numbers.length - 1;
    return sst(numbers, mean) / denominator;
  }

  /**
  * Returns <code>{@link #sst(double[], double) sst}(numbers, {@link #mean mean}(numbers))</code>.
  * <p>
  * <i>Use this version only when mean has not previously been calculated,</i>
  * since this method will internally calculate it.
  * <p>
  * @throws IllegalArgumentException if numbers == null; numbers.length == 0;
  * mean is calculated to be or any element of numbers is NaN
  */
  public static double sst(final double[] numbers) throws IllegalArgumentException {
    return sst(numbers, mean(numbers));
  }

  /**
  * Caluculates the SST (Sum of Squares, Total), that is,
  * the sum of the squares of the differences from mean of each element of numbers.
  * <p>
  * In order to obtain the highest accuracy, this method uses a form of
  * <a href="http://en.wikipedia.org/wiki/Compensated_summation">compensated summation</a>
  * (see <a href="http://en.wikipedia.org/wiki/Algorithms_for_calculating_variance#Algorithm_II_.28compensated.29">Algorithm II (compensated)</a>).
  * <p>
  * Contract: the result is always >= 0 (including positive infinity), and is never NaN.
  * <p>
  * @throws IllegalArgumentException if numbers == null; numbers.length == 0; mean or any element of numbers is NaN
  * @see <a href="http://en.wikipedia.org/wiki/Total_sum_of_squares">Wikipedia article on Total sum of squares</a>
  */
  public static double sst(final double[] numbers, final double mean) throws IllegalArgumentException {
    if (numbers == null || numbers.length == 0) {
      throw new IllegalArgumentException();
    }

    double sum2 = 0.0;
    double sumc = 0.0;  // the compensation sum, which would always be zero if had infinite precision; see the link cited in this method's javadocs for details
    for (final double number : numbers) {
      if (Double.isNaN(number)) {
        throw new IllegalArgumentException();
      }

      final double diff = number - mean;
      sum2 += diff * diff;
      sumc += diff;
    }

    final double sum = sum2 - (sumc * sumc / numbers.length);

    if (sum < 0) throw new IllegalStateException("calculated sum = " + sum + " < 0; this should never happen"); // this should never happen, so am not declaring throws IllegalStateException
    if (Double.isNaN(sum)) {
      if (Double.isNaN(mean)) throw new IllegalArgumentException("mean is NaN");
      throw new IllegalStateException("calculated a NaN sum, but failed to find a NaN element; this should never happen");  // this should never happen, so am not declaring throws IllegalStateException
    }

    return sum;
  }

  // -------------------- constructor --------------------

  /**
   * This sole private constructor suppresses the default (public) constructor,
   * ensuring non-instantiability outside of this class.
   **/
  private MathUtil() {}

}