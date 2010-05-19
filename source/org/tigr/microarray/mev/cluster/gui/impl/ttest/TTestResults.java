package org.tigr.microarray.mev.cluster.gui.impl.ttest;

import org.tigr.util.BooleanArray;
import org.tigr.util.DoubleArray;
import org.tigr.util.FloatMatrix;

/**
 * Container class for results of a TTEST run.
 * @author Eleanor
 *
 */

public class TTestResults {
	private int tTestDesign;
	private FloatMatrix rawPValuesMatrix,qValuesMatrix,
		adjPValuesMatrix, tValuesMatrix, dfMatrix, meansAMatrix, 
		meansBMatrix, sdAMatrix, sdBMatrix, 
		oneClassMeansMatrix, oneClassSDsMatrix;
	private BooleanArray isSig;
	private DoubleArray diffMeansBA, negLog10PValues;
	private int significanceMethod;
	private boolean calculateAdjFDRPVals;

	public int getSignificanceMethod() {
		return significanceMethod;
	}
	public void setSignificanceMethod(int significanceMethod) {
		this.significanceMethod = significanceMethod;
	}
	public boolean isCalculateAdjFDRPVals() {
		return calculateAdjFDRPVals;
	}
	public void setCalculateAdjFDRPVals(boolean calculateAdjFDRPVals) {
		this.calculateAdjFDRPVals = calculateAdjFDRPVals;
	}
	public void setIsSig(BooleanArray isSig) {
		this.isSig = isSig;
	}
	public void setIsSig(boolean[] isSig) {
		this.isSig = new BooleanArray(isSig);
	}
	public void setDiffMeansBA(DoubleArray array) {
		diffMeansBA = array;
	}
	public void setDiffMeansBA(double[] array) {
		diffMeansBA = new DoubleArray(array);
	}
	public DoubleArray getDiffMeansBA() {
		return diffMeansBA;
	}
	public DoubleArray getNegLog10PValues() {
		return negLog10PValues;
	}
	public void setNegLog10PValues(double[] array) {
		negLog10PValues = new DoubleArray(array);
	}
	public void setNegLog10PValues(DoubleArray array) {
		negLog10PValues = array;
	}

	public BooleanArray getIsSig() {
		return isSig;
	}

	public TTestResults(){}
	
	public int getTTestDesign() {
		return tTestDesign;
	}
	public void setTTestDesign(int testDesign) {
		tTestDesign = testDesign;
	}
	public FloatMatrix getRawPValuesMatrix() {
		return rawPValuesMatrix;
	}
	public FloatMatrix getQValuesMatrix() {
		return qValuesMatrix;
	}
	public void setRawPValuesMatrix(FloatMatrix rawPValuesMatrix) {
		this.rawPValuesMatrix = rawPValuesMatrix;
	}
	public void setQValuesMatrix(FloatMatrix qValuesMatrix) {
		this.qValuesMatrix = qValuesMatrix;
	}
	public FloatMatrix getAdjPValuesMatrix() {
		return adjPValuesMatrix;
	}
	public void setAdjPValuesMatrix(FloatMatrix adjPValuesMatrix) {
		this.adjPValuesMatrix = adjPValuesMatrix;
	}
	public FloatMatrix getTValuesMatrix() {
		return tValuesMatrix;
	}
	public void setTValuesMatrix(FloatMatrix valuesMatrix) {
		tValuesMatrix = valuesMatrix;
	}
	public FloatMatrix getDfMatrix() {
		return dfMatrix;
	}
	public void setDfMatrix(FloatMatrix dfMatrix) {
		this.dfMatrix = dfMatrix;
	}
	public FloatMatrix getMeansAMatrix() {
		return meansAMatrix;
	}
	public void setMeansAMatrix(FloatMatrix meansAMatrix) {
		this.meansAMatrix = meansAMatrix;
	}
	public FloatMatrix getMeansBMatrix() {
		return meansBMatrix;
	}
	public void setMeansBMatrix(FloatMatrix meansBMatrix) {
		this.meansBMatrix = meansBMatrix;
	}
	public FloatMatrix getSdAMatrix() {
		return sdAMatrix;
	}
	public void setSdAMatrix(FloatMatrix sdAMatrix) {
		this.sdAMatrix = sdAMatrix;
	}
	public FloatMatrix getSdBMatrix() {
		return sdBMatrix;
	}
	public void setSdBMatrix(FloatMatrix sdBMatrix) {
		this.sdBMatrix = sdBMatrix;
	}
	public FloatMatrix getOneClassMeansMatrix() {
		return oneClassMeansMatrix;
	}
	public void setOneClassMeansMatrix(FloatMatrix oneClassMeansMatrix) {
		this.oneClassMeansMatrix = oneClassMeansMatrix;
	}
	public FloatMatrix getOneClassSDsMatrix() {
		return oneClassSDsMatrix;
	}
	public void setOneClassSDsMatrix(FloatMatrix oneClassSDsMatrix) {
		this.oneClassSDsMatrix = oneClassSDsMatrix;
	}
	
}
