package org.tigr.microarray.mev.cluster.gui.impl.surv;

import java.util.Vector;


import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.util.FloatMatrix;

public class SURVAlgorithmData extends AlgorithmData {

	private static final String CROSS_VALIDATION_LIKELIHOOD = "cross-validation-likelihood";
	private static final String LAMBDA_START = "lambda-start";
	private static final String IS_EMPTY = "is-empty";
	private static final String COEFFICIENT_INDICES = "zeroed-indices";
	private static final String IS_COMPARISON = "is-comparison";
	private static final String PENALIZED_COEFFICIENTS = "penalized-coefficients";
	private static final String UNPENALIZED_COEFFICIENTS = "unpenalized-coefficients";
	private static final String BASESURV_SURVIVAL = "basesurvSurvival";
	private static final String BASESURV_TIME = "basesurvTime";
	private static final String BASEHAZPLOTX = "basehazplotx";
	private static final String BASEHAZPLOTY = "basehazploty";
	private static final String FITTED_VALUES = "fittedValues";
	private static final String CURVESURVIVAL = "curvesurvival";
	private static final String CURVETIME = "curvetime";
	private static final String NAMES = "residualsNames";
	private static final String LOG_LIKELIHOOD = "logLikelihood";
	private static final String L1PENALTY = "l1penalty";
	private static final String L2PENALTY = "l2penalty";
	private static final String GENE_INDICES = "geneIndices";
	private static final String EXPERIMENT = "experiment";
	private static final String COEFFICIENT = "coefficient";
	private static final String VARIANCE = "variance";
	private static final String INITIALLOGLIK = "initialloglik";
	private static final String FINALLOGLIK = "finalloglik";
	private static final String NATIVEVAR = "nativevar";
	private static final String SCORE = "score";
	private static final String RSCORE = "rscore";
	private static final String WALDTEST = "waldtest";
	private static final String MEAN = "mean";
	private static final String NUMBER = "number";
	private static final String WEIGHTS = "weights";
	private static final String METHOD = "method";
	private static final String LINEAR_PREDICTORS = "linearPredictors";
	private static final String RESIDUALS = "residuals";
	private static final String RESIDUALS_NAMES = NAMES;
	private static final String ITERATION_COUNT = "iteration-count";
	private static final String P_VALUE = "p-value";
	private static final String CHI_SQUARED = "chi-squared";
	private static final String EXPECTED = "expected";
	private static final String OBSERVED = "observed";
	private static final String SIZES = "group-sizes";
	private static final String EVENT_TIMES1 = "event-times1";
	private static final String EVENT_TIMES2 = "event-times2";
	private static final String MED_SURVIVAL = "median-survival-times";
	private static final String EVENT_STATUSES1 = "event-statuses1";
	private static final String EVENT_STATUSES2 = "event-statuses2";
	private static final String SAMPLE_LABELS = "sample-labels";
	private static final String ORIGINAL_INDICES_1 = "original-indices-1";
	private static final String ORIGINAL_INDICES_2 = "original-indices-2";
	
	public SURVAlgorithmData() {
		
	}
	public void setPValue(float pvalue) {
		addParam(P_VALUE, new Float(pvalue).toString());
	}
	public float getPValue() {
		return getParams().getFloat(P_VALUE);
	}
	public float getChiSquare() {
		return getParams().getFloat(CHI_SQUARED);
	}	
	public void setChiSquare(float chisq) {
		addParam(CHI_SQUARED, new Double(chisq).toString());
	}
	public void setExpected(double[] expected) {
		addVector(EXPECTED, doubleArrayToVector(expected));
	}
	@SuppressWarnings("unchecked")
	public Vector<Double> getExpected() {
		return getVector(EXPECTED);
	}
	public void setObserved(double[] observed) {
		addVector(OBSERVED, doubleArrayToVector(observed));
	}
	@SuppressWarnings("unchecked")
	public Vector<Double> getObserved() {
		return getVector(OBSERVED);
	}
	public void setSizes(int[] sizes) {
		Vector<Integer> sizesv = new Vector<Integer>();
		for(int i=0; i<sizes.length; i++) {
			sizesv.add(sizes[i]);
		}
		addVector(SIZES, sizesv);
	}
	@SuppressWarnings("unchecked")
	public Vector<Integer> getSizes() {
		return getVector(SIZES);
	}

	public void setGroup1OriginalIndexes(Vector<Integer> indexes) {
		addVector(ORIGINAL_INDICES_1, indexes);
	}
	@SuppressWarnings("unchecked")
	public Vector<Integer> getGroup1OriginalIndexes() {
		return getVector(ORIGINAL_INDICES_1);
	}

	public void setGroup2OriginalIndexes(Vector<Integer> indexes) {
		addVector(ORIGINAL_INDICES_2, indexes);
	}
	@SuppressWarnings("unchecked")
	public Vector<Integer> getGroup2OriginalIndexes() {
		return getVector(ORIGINAL_INDICES_2);
	}
	public void setGroup1Events(Vector<Float> times) {
		addVector(EVENT_TIMES1, times);
	}
	@SuppressWarnings("unchecked")
	public Vector<Float> getGroup1Events() {
		return getVector(EVENT_TIMES1);
	}
	public void setGroup2Events(Vector<Float> times) {
		addVector(EVENT_TIMES2, times);
	}
	@SuppressWarnings("unchecked")
	public Vector<Float> getGroup2Events() {
		return getVector(EVENT_TIMES2);
	}
	public void setGroup1Statuses(Vector<Boolean> statuses) {
		addVector(EVENT_STATUSES1, statuses);
	}
	@SuppressWarnings("unchecked")
	public Vector<Boolean> getGroup1Statuses() {
		return getVector(EVENT_STATUSES1);
	}
	public void setGroup2Statuses(Vector<Boolean> statuses) {
		addVector(EVENT_STATUSES2, statuses);
	}
	@SuppressWarnings("unchecked")
	public Vector<Boolean> getGroup2Statuses() {
		return getVector(EVENT_STATUSES2);
	}
	public Double getCoefficient() {
		return new Double(getParams().getDouble(COEFFICIENT));
	}
	public void setCoefficient(double c) {
		getParams().setProperty(COEFFICIENT, new Double(c).toString());
	}
	public Double getVariance() {
		return new Double(getParams().getDouble(VARIANCE));
	}
	public void setVariance(double c) {
		getParams().setProperty(VARIANCE, new Double(c).toString());
	}
	public Double getInitialLogLik() {
		return new Double(getParams().getDouble(INITIALLOGLIK));
	}
	public void setInitialLogLik(double c) {
		getParams().setProperty(INITIALLOGLIK, new Double(c).toString());
	}
	public Double getFinalLogLik() {
		return new Double(getParams().getDouble(FINALLOGLIK));
	}
	public void setFinalLogLik(double c) {
		getParams().setProperty(FINALLOGLIK, new Double(c).toString());
	}
	public Double getNativeVar() {
		return new Double(getParams().getDouble(NATIVEVAR));
	}
	public void setNativeVar(double c) {
		getParams().setProperty(NATIVEVAR, new Double(c).toString());
	}
	public Double getScore() {
		return new Double(getParams().getDouble(SCORE));
	}
	public void setScore(double c) {
		getParams().setProperty(SCORE, new Double(c).toString());
	}
	public Double getRScore() {
		return new Double(getParams().getDouble(RSCORE));
	}
	public void setRScore(double c) {
		getParams().setProperty(RSCORE, new Double(c).toString());
	}
	public Double getWaldTest() {
		return new Double(getParams().getDouble(WALDTEST));
	}
	public void setWaldTest(double c) {
		getParams().setProperty(WALDTEST, new Double(c).toString());
	}
	public Double getMean() {
		return new Double(getParams().getDouble(MEAN));
	}
	public void setMean(double c) {
		getParams().setProperty(MEAN, new Double(c).toString());
	}
	public Double getNumber() {
		return new Double(getParams().getDouble(NUMBER));
	}
	public void setNumber(double c) {
		getParams().setProperty(NUMBER, new Double(c).toString());
	}
	public Vector<Double> getWeights() {
		return getVector(WEIGHTS);
	}
	public void setWeights(double[] c) {
		if(c != null)
			addVector(WEIGHTS, doubleArrayToVector(c));
	}
	public String getMethod() {
		return getParams().getString(METHOD);
	}
	public void setMethod(String c) {
		getParams().setProperty(METHOD, c);
	}
	
	public void setLinearPredictors(double[] linearPredictors) {
		addVector(LINEAR_PREDICTORS, doubleArrayToVector(linearPredictors));
	}
	@SuppressWarnings("unchecked")
	public Vector<Double> getLinearPredictors() {
		return getVector(LINEAR_PREDICTORS);
	}
	public void setResiduals(double[] residuals) {
		addVector(RESIDUALS, doubleArrayToVector(residuals));
	}
	@SuppressWarnings("unchecked")
	public Vector<Double> getResiduals() {
		return getVector(RESIDUALS);
	}
	public void setResidualsNames(String[] residuals) {
		addVector(RESIDUALS_NAMES, stringArrayToVector(residuals));
	}
	@SuppressWarnings("unchecked")
	public Vector<String> getResidualsNames() {
		return getVector(RESIDUALS_NAMES);
	}
	public void setIterationCount(int iterationcount) {
		addParam(ITERATION_COUNT, new Integer(iterationcount).toString());
	}
	public float getIterationCount() {
		return getParams().getInt(ITERATION_COUNT);
	}
	public FloatMatrix getExpressionMatrix() {
		return getMatrix(EXPERIMENT);
	}
	public void setExpressionMatrix(FloatMatrix fm) {
		addMatrix(EXPERIMENT, fm);
	}
	public int[] getGeneIndices() {
		return getIntArray(GENE_INDICES);
		
	}
	public void setGeneIndices(int[] gl) {
		addIntArray(GENE_INDICES, gl);
	}
	public String[] getSampleLabels() {
		return getStringArray(SAMPLE_LABELS);
		
	}
	public void setSampleLabels(String[] gl) {
		addStringArray(SAMPLE_LABELS, gl);
	}
	
	public boolean isComparison() {
		return getParams().getBoolean(IS_COMPARISON);
	}
	public void setComparison(boolean b) {
		addParam(IS_COMPARISON, new Boolean(b).toString());
	}
	
	//Variable selection items
	public void setPenalizedCoefficients(double[] pc) {
		addVector(PENALIZED_COEFFICIENTS, doubleArrayToVector(pc));
	}
	public Vector<Double> getPenalizedCoefficients() {
		return getVector(PENALIZED_COEFFICIENTS);
	}
	public void setUnpenalizedCoefficients(double[] pc) {
		addVector(UNPENALIZED_COEFFICIENTS, doubleArrayToVector(pc));
	}
	public Vector<Double> getUnpenalizedCoefficients() {
		return getVector(UNPENALIZED_COEFFICIENTS);
	}

	public void setBasesurvTime(double[] pc) {
		addVector(BASESURV_TIME, doubleArrayToVector(pc));
	}
	public Vector<Double> getBasesurvTime() {
		return getVector(BASESURV_TIME);
	}

	public void setBasesurvSurvival(double[] pc) {
		addVector(BASESURV_SURVIVAL, doubleArrayToVector(pc));
	}
	public Vector<Double> getBasesurvSurvival() {
		return getVector(BASESURV_SURVIVAL);
	}
	
	public void setBasehazplotx(double[] pc) {
		addVector(BASEHAZPLOTX, doubleArrayToVector(pc));
	}
	public Vector<Double> getBasehazplotx() {
		return getVector(BASEHAZPLOTX);
	}

	public void setBasehazploty(double[] pc) {
		addVector(BASEHAZPLOTY, doubleArrayToVector(pc));
	}
	public Vector<Double> getBasehazploty() {
		return getVector(BASEHAZPLOTY);
	}

	public void setFittedValues(double[] pc) {
		addVector(FITTED_VALUES, doubleArrayToVector(pc));
	}
	public Vector<Double> getFittedValues() {
		return getVector(FITTED_VALUES);
	}

	public void setCurvesurvival(double[] pc) {
		addVector(CURVESURVIVAL, doubleArrayToVector(pc));
	}
	public Vector<Double> getCurvesurvival() {
		return getVector(CURVESURVIVAL);
	}
	public void setCurvetime(double[] pc) {
		addVector(CURVETIME, doubleArrayToVector(pc));
	}
	public Vector<Double> getCurvetime() {
		return getVector(CURVETIME);
	}
	public void setresidualsNames(String[] pc) {
		addVector(NAMES, stringArrayToVector(pc));
	}
	public Vector<String> getresidualsNames() {
		return getVector(NAMES);
	}

	public void setlogLikelihood(double pc) {
		addParam(LOG_LIKELIHOOD, new Double(pc).toString());
	}
	public double getlogLikelihood() {
		return getParams().getDouble(LOG_LIKELIHOOD);
	}
	public void setL1penalty(double pc) {
		addParam(L1PENALTY, new Double(pc).toString());
	}
	public double getL1penalty() {
		return new Double(getParams().getString(L1PENALTY));
	}
	public void setL2penalty(double pc) {
		addParam(L2PENALTY, new Double(pc).toString());
	}
	public double getL2penalty() {
		return new Double(getParams().getString(L2PENALTY));
	}
	public void setResClusters(int[][] clusters) {
		if(clusters != null && clusters.length > 0)
			addIntMatrix("clusters", clusters);
		else {
			addIntMatrix("clusters", new int[0][]);
		}
	}
	public int[][] getResClusters() {
		return getIntMatrix("clusters");
	}
	public void setPenalizedCoefficientIndexes(int[] indexes) {
		if(indexes != null && indexes.length > 0)
			addIntArray(COEFFICIENT_INDICES, indexes);
		else {
			System.out.println("Zeroed Indexes is null");
			addIntArray(COEFFICIENT_INDICES, new int[0]);
		}
	}
	public int[] getPenalizedCoefficientIndexes() {
		return getIntArray(COEFFICIENT_INDICES);
	}
	public boolean isEmptyResults() {
		return getParams().getBoolean(IS_EMPTY);
	}
	public void setEmptyResults(boolean b) {
		addParam(IS_EMPTY, new Boolean(b).toString());
	}

	public double getCrossValLik() {
		return Double.parseDouble(getParams().getString(CROSS_VALIDATION_LIKELIHOOD));
	}
	public void setCrossValLik(double crossValLik) {
		addParam(CROSS_VALIDATION_LIKELIHOOD, new Double(crossValLik).toString());
	}
	public Vector<Float> getMedians() {
		return (getVector(MED_SURVIVAL));
	}
	public void setMedians(Vector<Float> medSurv) {
		addVector(MED_SURVIVAL, medSurv);
	}
	public double getLambda() {
		return Double.parseDouble(getParams().getString(LAMBDA_START));
	}
	public void setLambda(double lambda1) {
		addParam(LAMBDA_START, new Double(lambda1).toString());
	}

	private Vector<Double> doubleArrayToVector(double[] inarray) {
		Vector<Double> outvector = new Vector<Double>();
		for(int i=0; i<inarray.length; i++) {
			outvector.add(inarray[i]);
		}
		return outvector;
	}
	private Vector<String> stringArrayToVector(String[] inarray) {
		Vector<String> outvector = new Vector<String>();
		for(int i=0; i<inarray.length; i++) {
			outvector.add(inarray[i]);
		}
		return outvector;
	}
}
