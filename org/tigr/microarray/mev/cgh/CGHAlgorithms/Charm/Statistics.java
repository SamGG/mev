package org.tigr.microarray.mev.cgh.CGHAlgorithms.Charm;

import java.util.Arrays;

/**
* This class implements all of the statistics-related functionality
 * required for the ChARM project.
*
 * <p>Title: Statistics</p>
 * <p>Description: This class implements all of the statistics-related functionality
 * required for the ChARM project.</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: Princeton University</p>
 * @author Chad Myers, Xing Chen
 * @version 1.0
 */

public class Statistics {

  /**
   * Class constructor.
   */
  public Statistics() {
  }

  /**
   * Returns mean of the supplied double array.
   * @param arr double[]
   * @return double
   */
  public static double getMean(double[] arr) {
    double sum = 0;
    int size = 0;

    for (int i = 0; i < arr.length; i++) {
      if (!Double.isNaN(arr[i])) {
        sum += arr[i];
        size++;
      }
    }

    if (size == 0) {
      return 0;
    }

    return (sum / size);
  }
  
  public static float getMean(float[] arr) {
	    float sum = 0;
	    int size = 0;

	    for (int i = 0; i < arr.length; i++) {
	      if (!Float.isNaN(arr[i])) {
	        sum += arr[i];
	        size++;
	      }
	    }

	    if (size == 0) {
	      return 0;
	    }

	    return (sum / size);
	  }

  /**
   * Returns mean of the supplied int array.
   * @param arr int[]
   * @return double
   */
  public static double getMean(int[] arr) {
    double sum = 0;
    int size = 0;

    for (int i = 0; i < arr.length; i++) {
      if (!Double.isNaN(arr[i])) {
        sum += arr[i];
        size++;
      }
    }

    if (size == 0) {
      return 0;
    }

    return ((double)sum / (double)size);
  }


  /**
   * Returns mean of the sub-array between the specified indices.
   * @param arr double[]
   * @param start int
   * @param end int
   * @return double
   */
  public static double getMean(double[] arr, int start, int end) {
    double sum = 0;
    int size = 0;
    if (start < 0) {
      start = 0;
    }
    if (end > arr.length) {
      end = arr.length;
    }
    for (int i = start; i < end; i++) {
      // if (!Double.isNaN(arr[i]) ){
      sum += arr[i];
      size++;
      // }
    }
    if (size == 0) {
      return 0;
    }
    return (sum / size);
  }
  
  public static float getMean(float[] arr, int start, int end) {
	    float sum = 0;
	    int size = 0;
	    if (start < 0) {
	      start = 0;
	    }
	    if (end > arr.length) {
	      end = arr.length;
	    }
	    for (int i = start; i < end; i++) {
	      // if (!float.isNaN(arr[i]) ){
	      sum += arr[i];
	      size++;
	      // }
	    }
	    if (size == 0) {
	      return 0;
	    }
	    return (sum / size);
	  }

  /**
   * Returns the variance of the sub-array between the specified indices.
   *
   * @param arr double[]
   * @param start int
   * @param end int
   * @return double
   */
  public static double getVariance(double[] arr, int start, int end) {

    double mean = getMean(arr, start, end);

    if (start < 0) {
      start = 0;
    }
    if (end > arr.length) {
      end = arr.length;
    }

    double sum_of_diffs = 0;

    for (int i = start; i < end; i++) {
      sum_of_diffs += Math.pow(Math.abs(arr[i] - mean), 2);
    }

    int number = (end - start - 1);
    if (number == 0) {
      return 0;
    }
    else {
      return (sum_of_diffs / number);
    }

  }

  /**
   * Returns the variance of the sub-array between the specified indices.
   *
   * @param arr float[]
   * @param start int
   * @param end int
   * @return float
   */
  public static float getVariance(float[] arr, int start, int end) {

    float mean = getMean(arr, start, end);

    if (start < 0) {
      start = 0;
    }
    if (end > arr.length) {
      end = arr.length;
    }

    float sum_of_diffs = 0;

    for (int i = start; i < end; i++) {
      sum_of_diffs += Math.pow(Math.abs(arr[i] - mean), 2);
    }

    int number = (end - start - 1);
    if (number == 0) {
      return 0;
    }
    else {
      return (sum_of_diffs / number);
    }

  }

  /**
   * Returns variance of the sub-array between the specified indices with the supplied mean.
   * @param arr double[]
   * @param start int
   * @param end int
   * @param mean double
   * @return double
   */
  public static double getVariance(double[] arr, int start, int end, double mean) {

    double sum_of_diffs = 0;

    if (start < 0) {
      start = 0;
    }
    if (end > arr.length) {
      end = arr.length;
    }

    for (int i = start; i < end; i++) {
      sum_of_diffs += Math.pow(Math.abs(arr[i] - mean), 2);
    }

    int number = (end - start - 1);
    if (number == 0) {
      return 0;
    }
    else {
      return (sum_of_diffs / number);
    }
  }

  /**
   * Returns variance of the sub-array between the specified indices with the supplied mean.
   * @param arr double[]
   * @param start int
   * @param end int
   * @param mean double
   * @return double
   */
  public static float getVariance(float[] arr, int start, int end, float mean) {

	float sum_of_diffs = 0;

    if (start < 0) {
      start = 0;
    }
    if (end > arr.length) {
      end = arr.length;
    }

    for (int i = start; i < end; i++) {
      sum_of_diffs += Math.pow(Math.abs(arr[i] - mean), 2);
    }

    int number = (end - start - 1);
    if (number == 0) {
      return 0;
    }
    else {
      return (sum_of_diffs / number);
    }
  }

  /**
   * Returns median of the input double array (assumes no NaN values)
   * @param inputArr double[]
   * @return double
   */
  public static double getMedian(double[] inputArr) {

    double[] arr = new double[inputArr.length];

    System.arraycopy(inputArr, 0, arr, 0, arr.length);

    if (arr.length > 8) {
      Arrays.sort(arr);
    }
    else {
      //perform a simple selection sort
      for (int i = 0; i < arr.length; i++) {

        int min = i;

        for (int j = i + 1; j < arr.length; j++) {
          if (arr[j] < arr[min]) {
            min = j;
          }
        }

        double temp = arr[min];
        arr[min] = arr[i];
        arr[i] = temp;
      }
    }

    return getMedianOfSorted(arr);
  }
  
  public static float getMedian(float[] inputArr) {

	    float[] arr = new float[inputArr.length];

	    System.arraycopy(inputArr, 0, arr, 0, arr.length);

	    if (arr.length > 8) {
	      Arrays.sort(arr);
	    }
	    else {
	      //perform a simple selection sort
	      for (int i = 0; i < arr.length; i++) {

	        int min = i;

	        for (int j = i + 1; j < arr.length; j++) {
	          if (arr[j] < arr[min]) {
	            min = j;
	          }
	        }

	        float temp = arr[min];
	        arr[min] = arr[i];
	        arr[i] = temp;
	      }
	    }

	    return getMedianOfSorted(arr);
	  }

  /**
   * Returns median of the sub-array between the specified indices (not inclusive of end index).
   * @param inputArr double[]
   * @param start int
   * @param end int
   * @return double
   */
  public static double getMedian(double[] inputArr, int start, int end) {

    if (start < 0) {
      start = 0;
    }
    if (end > inputArr.length) {
      end = inputArr.length;
    }

    double[] arr = new double[ (end - start)];

    System.arraycopy(inputArr, start, arr, 0, arr.length);

    if (arr.length > 8) {
      Arrays.sort(arr);
    }
    else {
      //perform a simple selection sort
      for (int i = 0; i < arr.length; i++) {

        int min = i;

        for (int j = i + 1; j < arr.length; j++) {
          if (arr[j] < arr[min]) {
            min = j;
          }
        }

        double temp = arr[min];
        arr[min] = arr[i];
        arr[i] = temp;
      }
    }

    return getMedianOfSorted(arr);
  }
  
  public static float getMedian(float[] inputArr, int start, int end) {

	    if (start < 0) {
	      start = 0;
	    }
	    if (end > inputArr.length) {
	      end = inputArr.length;
	    }

	    float[] arr = new float[ (end - start)];

	    System.arraycopy(inputArr, start, arr, 0, arr.length);

	    if (arr.length > 8) {
	      Arrays.sort(arr);
	    }
	    else {
	      //perform a simple selection sort
	      for (int i = 0; i < arr.length; i++) {

	        int min = i;

	        for (int j = i + 1; j < arr.length; j++) {
	          if (arr[j] < arr[min]) {
	            min = j;
	          }
	        }

	        float temp = arr[min];
	        arr[min] = arr[i];
	        arr[i] = temp;
	      }
	    }

	    return getMedianOfSorted(arr);
	  }

  /**
   * Returns median of sorted double array.
   * @param arr double[]
   * @return double
   */
  public static double getMedianOfSorted(double[] arr) {

    if (arr.length == 0) {
      return 0;
    }

    double median = 0;

    if (arr.length % 2 == 0) {
      median = (arr[arr.length / 2] + arr[arr.length / 2 - 1]) / 2;
    }
    else {
      median = arr[arr.length / 2];
    }

    return median;
  }
  
  public static float getMedianOfSorted(float[] arr) {

	    if (arr.length == 0) {
	      return 0;
	    }

	    float median = 0;

	    if (arr.length % 2 == 0) {
	      median = (arr[arr.length / 2] + arr[arr.length / 2 - 1]) / 2;
	    }
	    else {
	      median = arr[arr.length / 2];
	    }

	    return median;
	  }

  /**
   * Returns median of sorted double sub-array between specified indices.
   * @param arr double[]
   * @param start int
   * @param end int
   * @return double
   */
  public static double getMedianOfSorted(double[] arr, int start, int end) {

    if (start < 0) {
      start = 0;
    }
    if (end > arr.length) {
      end = arr.length;
    }

    double median = 0;

    if ( (end - start) % 2 == 0) {
      median = (arr[ (start + end) / 2] + arr[ (start + end) / 2 - 1]) / 2;
    }
    else {
      median = arr[ (start + end) / 2];
    }

    return median;
  }

  /**
   * Returns the magnitude percentile of a target double within an array (absolute value).
   * @param arr double[]
   * @param target double
   * @return double
   */
  public static double getPercentile(double[] arr, double target) {
    target = Math.abs(target);
    int numGreater = 0;

    for (int i = 0; i < arr.length; i++) {
      if (target >= Math.abs(arr[i])) {
        numGreater++;
      }
    }
    return ( (double) (numGreater) / (double) arr.length);
  }
  /**
   * Returns the magnitude percentile of a target double within an array (absolute value).
   * @param arr float[]
   * @param target float
   * @return float
   */
  public static float getPercentile(float[] arr, float target) {
    target = Math.abs(target);
    int numGreater = 0;

    for (int i = 0; i < arr.length; i++) {
      if (target >= Math.abs(arr[i])) {
        numGreater++;
      }
    }
    return ( (float) (numGreater) / (float) arr.length);
  }
  /**
   * Gaussian probability density function.
   * @param x double
   * @param mean double
   * @param stdev double
   * @return double
   */
  public static double getGaussianDensity(double x, double mean, double stdev) {

    double exponent = ( -1 * Math.pow( (x - mean), 2) / (2 * Math.pow(stdev, 2)));
    double denom = (Math.sqrt(2 * Math.PI * Math.pow(stdev, 2)));
    double coeff;

    coeff = 1 / denom;

    if (Double.isNaN(coeff * Math.exp(exponent))) {
      return 0;
    }
    else {
      return (coeff * Math.exp(exponent));
    }
  }

  /**
   * Gaussian probability density function.
   * @param x float
   * @param mean float
   * @param stdev float
   * @return float
   */
  public static float getGaussianDensity(float x, float mean, float stdev) {

    double exponent = ( -1 * Math.pow( (x - mean), 2) / (2 * Math.pow(stdev, 2)));
    double denom = (Math.sqrt(2 * Math.PI * Math.pow(stdev, 2)));
    double coeff;

    coeff = 1 / denom;

    if (Double.isNaN(coeff * Math.exp(exponent))) {
      return 0.0f;
    }
    else {
      return (float)(coeff * Math.exp(exponent));
    }
  }
  /**
   * Returns the sum of the input double array.
   * @param arr double[]
   * @return double
   */
  public static double getSum(double[] arr) {

    double sum = 0;
    for (int i = 0; i < arr.length; i++) {
      sum += arr[i];
    }

    return sum;
  }
  
  /**
   * Returns the sum of the input double array.
   * @param arr float[]
   * @return float
   */
  public static float getSum(float[] arr) {

	float sum = 0;
    for (int i = 0; i < arr.length; i++) {
      sum += arr[i];
    }

    return sum;
  }

  /**
   * Returns the max of the input double array.
   * @param arr double[]
   * @return double
   */
  public static double getMax(double[] arr) {
    double currMax= 0;
    for (int i = 0; i < arr.length; i++) {
      if(Math.abs(arr[i]) > currMax) currMax = Math.abs(arr[i]);
    }
    return currMax;
  }

  /**
   * Returns the max of the input double array.
   * @param arr double[]
   * @return double
   */
  public static float getMax(float[] arr) {
    float currMax= 0;
    for (int i = 0; i < arr.length; i++) {
      if(Math.abs(arr[i]) > currMax) currMax = Math.abs(arr[i]);
    }
    return currMax;
  }
  /**
   * Returns factorial (n!) result.
   * @param n int
   * @return double
   */
  public static double getFactorial(int n) {
    double total = 1;

    for (int i = n; i > 1; i--){
      total *= i;
    }
    return total;
  }

  /**
   * Returns the binomial cumulative distribution function (assumes p = q =.5).
   * @param x int
   * @param total int
   * @return double
   */
  public static double getBinomialCDF(int x, int total){

    double result = 0;
    double coeff = 0;

    for (int i = 0; i < x; i++){
      coeff = getFactorial(total) / (getFactorial(i+1) * getFactorial(total-i-1));
      result += coeff * Math.pow(0.5, total);
    }

    return result;
  }

  /**
   * Erf(z)
   * Borrowed from Robert Sedgewick and Kevin Wayne's MyMath.java 2004,
   * <http://www.cs.princeton.edu/introcs/26function/MyMath.java.html>   *
   * @param z double
   * @return double
   */
  static double erf(double z) {
    double t = 1.0 / (1.0 + 0.5 * Math.abs(z));

    // use Horner's method
    double ans = 1 - t * Math.exp( -z*z   -   1.26551223 +
                                        t * ( 1.00002368 +
                                        t * ( 0.37409196 +
                                        t * ( 0.09678418 +
                                        t * (-0.18628806 +
                                        t * ( 0.27886807 +
                                        t * (-1.13520398 +
                                        t * ( 1.48851587 +
                                        t * (-0.82215223 +
                                        t * ( 0.17087277))))))))));
    if (z >= 0) {
      return ans;
    }
    else {
      return -ans;
    }
}

  /**
   * Gaussian cumulative distribution function.
   * Borrowed from Robert Sedgewick and Kevin Wayne's MyMath.java 2004,
   * <http://www.cs.princeton.edu/introcs/26function/MyMath.java.html>
   * @param z double
   * @return double
   */
  public static double getNormalCDF(double z){

    return 0.5 * (1.0 + erf(z / (Math.sqrt(2.0))));
  }

  /**
   * Gaussian cumulative distribution function.
   * Borrowed from Robert Sedgewick and Kevin Wayne's MyMath.java 2004,
   * <http://www.cs.princeton.edu/introcs/26function/MyMath.java.html>
   * @param z double
   * @return double
   */
  public static float getNormalCDF(float z){

    return 0.5f * (1.0f + (float)erf(z / (Math.sqrt(2.0))));
  }
  
  public static void main(String[] args) {
    int[] array = new int[] {
        4, 5, 2, 6};

    double result = Statistics.getBinomialCDF(13,25);

    System.out.println("RESULT = " + (result));

    System.out.println("RESULT = " + result + " ORIG. ARRAY: ");
    for (int i = 0; i < array.length; i++) {
      System.out.println(array[i]);
    }
  }
}
