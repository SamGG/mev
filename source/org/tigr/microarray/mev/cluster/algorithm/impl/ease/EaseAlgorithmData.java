package org.tigr.microarray.mev.cluster.algorithm.impl.ease;

import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.util.FloatMatrix;

public class EaseAlgorithmData extends AlgorithmData {
	private static final String NESTING_TERM_OPTION = "nesting-term"; //$NON-NLS-1$
	private static final String HAVE_ACCESSIONS_OPTION = "have-accession-numbers";
	private static final String RUN_NEASE_OPTION = "run-nease";
	private static final String PERFORM_CLUSTER_ANALYSIS_OPTION = "perform-cluster-analysis";
	private static final String EXPRESSION_OPTION = "expression";
	private static final String IS_RECURSED_RUN_OPTION = "is-recursed-run";
	private static final String RESULT_MATRIX_OPTION = "result-matrix";
	private static final String POPULATION_LIST_OPTION = "population-list";
	private static final String ANNOTATION_FILE_LIST_OPTION = "annotation-file-list";
	private static final String IMPLIES_FILE_LOCATION_OPTION = "implies-file-location";
	private static final String TAG_FILE_LOCATION_OPTION = "tag-file-location";
	private static final String RUN_PERMUTATION_OPTION = "run-permutation-analysis";
	private static final String HOCHBERG_CORRECTION_OPTION = "hochberg-correction";
	private static final String SELECTED_NESTED_TERMS_OPTION = "selected-nested-ease-terms";
	private static final String REPORT_EASE_SCORE_OPTION = "report-ease-score";
	private static final String TRIM_OPTION = "trim-option";
	private static final String TRIM_VALUE = "trim-value";
	private static final String NEASE_CONSOLIDATED_RESULTS_OPTION = "nease-consolidated-results";
	private static final String NESTED_EASE_COUNT = "nested-ease-count";
	private static final String NEASE_HEADER_NAMES_OPTION = "nease-headers";
	private static final String CLUSTER_MATRIX_OPTION = "cluster-matrix";
	private static final String HIT_LIST_MATRIX_OPTION = "hit-list-matrix";
	private static final String SAMPLE_LIST_OPTION = "sample-list";
	private static final String SAMPLE_INDICES_OPTION = "sample-indices";
	private static final String CONVERTER_FILE_NAME_OPTION = "converter-file-name";
	private static final String HEADER_NAMES_OPTION = "header-names";
	private static final String P_VALUE_CORRECTIONS_OPTION = "p-value-corrections";
	/*
	 * getInt("permutation-count", 1)
	 * a.addStringArray("category-names
	 * algorithmData.addMatrix("means", means);
	 * algorithmData.addMatrix("variances", getVariances(expData, means, clusters));
	 */
//TODO add getNeaseGeneEnrichment(int index)
	//double neaseGeneEnrichment = neaseListHits - (neaseListSize / neasePopSize) * neasePopHits;
	public String getPvalueCorrectionsOption() {
		return getParams().getString(P_VALUE_CORRECTIONS_OPTION);
	}
	public void setPvalueCorrectionsOption(String value) {
		getParams().setProperty(P_VALUE_CORRECTIONS_OPTION, value);
	}
	public void setNeaseHeaderNames(String[] values) {
		addStringArray(NEASE_HEADER_NAMES_OPTION, values);
	}
	public String[] getNeaseHeaderNames() {
		return getStringArray(NEASE_HEADER_NAMES_OPTION);
	}
	public void setHeaderNames(String[] values) {
		addStringArray(HEADER_NAMES_OPTION, values);
	}
	public String[] getHeaderNames() {
		return getStringArray(HEADER_NAMES_OPTION);
	}
	public String getConverterFileName() {
		return getParams().getString(CONVERTER_FILE_NAME_OPTION);
	}
	public void setConverterFileName(String value) {
		getParams().setProperty(CONVERTER_FILE_NAME_OPTION, value);
	}
	public void setSampleIndices(int[] values) {
		addIntArray(SAMPLE_INDICES_OPTION, values);
	}
	public int[] getSampleIndices() {
		return getIntArray(SAMPLE_INDICES_OPTION);
	}
	public String[] getSampleList() {
		return getStringArray(SAMPLE_LIST_OPTION);
	}
	public void setSampleList(String[] value) {
		addStringArray(SAMPLE_LIST_OPTION, value);
	}
	public String[][] getHitListMatrix() {
		return (String[][])getObjectMatrix(HIT_LIST_MATRIX_OPTION);
	}
	public void setHitListMatrix(String[][] value) {
		addObjectMatrix(HIT_LIST_MATRIX_OPTION, value);
	}
	public void setTrimValue(float value) {
		getParams().setProperty(TRIM_VALUE, new Float(value).toString());
	}
	public float getTrimValue() {
		return getParams().getFloat(TRIM_VALUE);
	}
	public void setTrimOption(String value) {
		getParams().setProperty(TRIM_OPTION, value);
	}
	public String getTrimOption() {
		return getParams().getString(TRIM_OPTION);
	}
	public void setReportEaseScore(boolean value) {
		getParams().setProperty(REPORT_EASE_SCORE_OPTION, new Boolean(value).toString());
	}
	public boolean isReportEaseScore() {
		return getParams().getBoolean(REPORT_EASE_SCORE_OPTION);
	}
	public void setSelectedNestedTerms(String[] value) {
		addStringArray(SELECTED_NESTED_TERMS_OPTION, value);
	}
	public String[] getSelectedNestedTerms() {
		return getStringArray(SELECTED_NESTED_TERMS_OPTION);
	}
	public int[][] getClusterMatrix() {
		return getIntMatrix(CLUSTER_MATRIX_OPTION);
	}
	public void setClusterMatrix(int[][] matrix) {
		addIntMatrix(CLUSTER_MATRIX_OPTION, matrix);
	}
	public void setHochbergCorrection(boolean value) {
		getParams().setProperty(HOCHBERG_CORRECTION_OPTION, new Boolean(value).toString());
	}
	public boolean getHochbergCorrection() {
		return getParams().getBoolean(HOCHBERG_CORRECTION_OPTION);
	}
	public void setRunPermutationAnalysis(boolean value) {
		getParams().setProperty(RUN_PERMUTATION_OPTION, new Boolean(value).toString());
	}
	public boolean isRunPermutationAnalysis() {
		return getParams().getBoolean(RUN_PERMUTATION_OPTION, false);
	}
	public void setAnnotationFileList(String[] annFileList) {
		addStringArray(ANNOTATION_FILE_LIST_OPTION, annFileList);
	}
	public String[] getAnnotationFileList() {
		return getStringArray(ANNOTATION_FILE_LIST_OPTION);
	}
	public void setImpliesFileLocation(String impliesFileLocation) {
		getParams().setProperty(IMPLIES_FILE_LOCATION_OPTION, impliesFileLocation);
	}
	public String getImpliesFileLocation() {
		return getParams().getString(IMPLIES_FILE_LOCATION_OPTION);
	}
	public void setTagFileLocation(String tagFileLocation) {
		getParams().setProperty(TAG_FILE_LOCATION_OPTION, tagFileLocation);
	}
	public String getTagFileLocation() {
		return getParams().getString(TAG_FILE_LOCATION_OPTION);
	}
	public EaseAlgorithmData getNEASEResults(int index) {
		return (EaseAlgorithmData)getResultAlgorithmData(new Integer(index));
	}
	public int getNestedEaseCount() {
		return new Integer(getParams().getString(NESTED_EASE_COUNT)).intValue();
	}
	public void setNestedEaseCount(int value) {
		getParams().setProperty(NESTED_EASE_COUNT, new Integer(value).toString());
	}
	public String[][] getNeaseConsolidatedResults() {
		return (String[][])getObjectMatrix(NEASE_CONSOLIDATED_RESULTS_OPTION);
	}
	public void setNeaseConsolidatedResults(String[][] value) {
		addObjectMatrix(NEASE_CONSOLIDATED_RESULTS_OPTION, value);
	}
	public String getNestingTerm() {
		return getParams().getString(NESTING_TERM_OPTION);
	}
	public void setNestingTerm(String value) {
		getParams().setProperty(NESTING_TERM_OPTION, value);
	}
	
	public boolean isPerformClusterAnalysis() {
		return getParams().getBoolean(PERFORM_CLUSTER_ANALYSIS_OPTION, true);
	}
	public void setPerformClusterAnalysis(boolean value) {
		getParams().setProperty(PERFORM_CLUSTER_ANALYSIS_OPTION, Boolean.toString(value));
	}
	public FloatMatrix getExpression() {
		return getMatrix(EXPRESSION_OPTION);
	}
	public void setExpression(FloatMatrix value) {
		addMatrix(EXPRESSION_OPTION, value);
	}
	public boolean isHaveAccessions() {
		return getParams().getBoolean(HAVE_ACCESSIONS_OPTION);
	}
	public void setHaveAccessions(boolean value) {
		getParams().setProperty(HAVE_ACCESSIONS_OPTION, Boolean.toString(value));
	}
	/**
	 * Returns the Fisher's Exact score for the index specified
	 * @param index
	 * @return
	 */
	public double getFishers(int index) {
		if(isHaveAccessions())
			return new Double(getResultMatrix()[index][8]).doubleValue();
		else 
			return new Double(getResultMatrix()[index][7]).doubleValue();
	}
	public String getFileName(int index) {
		if(isHaveAccessions())
			return getResultMatrix()[index][2];
		else 
			return getResultMatrix()[index][1];
	}
	public String getTerm(int index) {
		if(isHaveAccessions())
			return getResultMatrix()[index][3];
		else 
			return getResultMatrix()[index][2];
	}
	public String[][] getResultMatrix() {
		return (String[][])getObjectMatrix(RESULT_MATRIX_OPTION);
	}
	public void setResultMatrix(String[][] matrix) {
		addObjectMatrix(RESULT_MATRIX_OPTION, matrix);
	}
	public int getIndexForTerm(String term) {
		String[][] result = getResultMatrix();
            	for(int k=0; k<result.length; k++) {
            		if(getTerm(k).equals(term)) {
            			return k;
            		}
            	}
            	return -1;
	}
	public int getPopSize(int index) {
		if(isHaveAccessions())
			return new Integer(getResultMatrix()[index][7]).intValue();
		else 
			return new Integer(getResultMatrix()[index][6]).intValue();
	}
	public int getPopHits(int index) {
		if(isHaveAccessions())
			return new Integer(getResultMatrix()[index][6]).intValue();
		else 
			return new Integer(getResultMatrix()[index][5]).intValue();
	}
	public int getListSize(int index) {
		if(isHaveAccessions())
			return new Integer(getResultMatrix()[index][5]).intValue();
		else 
			return new Integer(getResultMatrix()[index][4]).intValue();
	}

	public int getListHits(int index) {
		if(isHaveAccessions())
			return new Integer(getResultMatrix()[index][4]).intValue();
		else 
			return new Integer(getResultMatrix()[index][3]).intValue();
	}
	public boolean isRecursedRun() {
		return getParams().getBoolean(IS_RECURSED_RUN_OPTION);
	}
	public void setRecursedRun(boolean value) {
		getParams().setProperty(IS_RECURSED_RUN_OPTION, Boolean.toString(value));
	}
	public boolean isRunNease() {
		return getParams().getBoolean(RUN_NEASE_OPTION);
	}
	public void setRunNease(boolean value) {
		getParams().setProperty(RUN_NEASE_OPTION, Boolean.toString(value));
	}
	public String[] getPopulationList() {
		return getStringArray(POPULATION_LIST_OPTION);
	}
	public void setPopulationList(String[] stringArray) {
		addStringArray(POPULATION_LIST_OPTION, stringArray);
	}
	

}
