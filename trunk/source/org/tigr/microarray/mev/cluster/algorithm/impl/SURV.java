/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/**
 * @author  dschlauch
 * @author  raktim
 * @version
 */
package org.tigr.microarray.mev.cluster.algorithm.impl;


import org.rosuda.JRI.REXP;
import org.rosuda.JRI.Rengine;
import org.tigr.rhook.RHook;
import org.tigr.util.FloatMatrix;
import org.tigr.microarray.mev.cluster.Cluster;
import org.tigr.microarray.mev.cluster.Node;
import org.tigr.microarray.mev.cluster.NodeList;
import org.tigr.microarray.mev.cluster.NodeValue;
import org.tigr.microarray.mev.cluster.NodeValueList;
import org.tigr.microarray.mev.cluster.algorithm.AbortException;
import org.tigr.microarray.mev.cluster.algorithm.AbstractAlgorithm;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmEvent;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmException;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmParameters;
import org.tigr.microarray.mev.cluster.gui.impl.bn.getInteractions.GeneInteractions;
import org.tigr.microarray.mev.cluster.gui.impl.surv.SURVAlgorithmData;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JOptionPane;
import java.util.Vector;

public class SURV extends AbstractAlgorithm{
	private int progress;
	private FloatMatrix expMatrix;
	private boolean stop = false;
	private int[] groupAssignments;
	private int[][] sigGenesArrays;

	private int numGenes, numExps, numGroups, iteration, numAGroups, numBGroups;
	private int hcl_function;
	private boolean hcl_absolute;
	private boolean hcl_genes_ordered;  
	private boolean hcl_samples_ordered; 

	private AlgorithmEvent event;

	//input parameters
	private boolean robust, useWeights = false;
	private Vector<Float> eventTimesGroup1;
	private Vector<Float> eventTimesGroup2;
	
	private Vector<Boolean> eventStatusGroup1;
	private Vector<Boolean> eventStatusGroup2;
	
	private Vector<Integer> origIndexGroup1;
	private Vector<Integer> origIndexGroup2;
	
	//output variables for differential survival
	double[] observed;
	double[] expected;
	int[] sizes;
	double chisq;
	double[][] varianceMatrix;
	double pvalue;
	int degFreedom;
	
	//Cox proportional hazards model variables
	double coefficient;
	double variance;
	double initialloglik;
	double finalloglik;		
	double nativevar;
	double score;
	double rscore;
	double waldtest;
	int iterationcount;
	double[] linearPredictors;
	double[] residuals;
	String[] residualsNames;
	double mean;
	double number;
	double[] weights;
	String method;
	FloatMatrix expression;
	int[] geneIndices;
	String[] sampleNames;
	double lambda1 = 10;
	double crossValidationLikelihood = 0;
	boolean comparison;

//	Cox model.
	double[] penalizedCoefficients;
	int[] nonzeroCoefficientsIndexes;
	int[] penalizedCoefficientIndexes;
	double[] unpenalizedCoefficients;
	double[] basesurvTime;
	double[] basesurvSurvival;
	double[] basehazplotx;
	double[] basehazploty;
	double logLikelihood;
	double l1penalty;
	double l2penalty;
	double[] fittedValues;
	double[] curvesurvival;
	double[] curvetime;

	SURVAlgorithmData result = new SURVAlgorithmData();
	
	/**
	 * This method should interrupt the calculation.
	 */
	public void abort() {
		stop = true;        
	}
	/**
	 * This method execute calculation and return result,
	 * stored in <code>AlgorithmData</code> class.
	 *
	 * @param data the data to be calculated.
	 */
	public AlgorithmData execute(AlgorithmData data) throws AlgorithmException {
		return execute((SURVAlgorithmData)data);
	}


	public SURVAlgorithmData execute(SURVAlgorithmData data) throws AlgorithmException {

		
		eventStatusGroup1 = data.getGroup1Statuses();
		eventStatusGroup2 = data.getGroup2Statuses();
		eventTimesGroup1 = data.getGroup1Events();
		eventTimesGroup2 = data.getGroup2Events();
		origIndexGroup1 = data.getGroup1OriginalIndexes();
		origIndexGroup2 = data.getGroup2OriginalIndexes();
		
		this.expression = data.getExpressionMatrix();
		this.geneIndices = data.getGeneIndices();
		this.sampleNames = data.getSampleLabels();
		
		
		this.comparison = data.isComparison();
		result.setComparison(data.isComparison());
		

		progress=0;
		event = null;
		event = new AlgorithmEvent(this, AlgorithmEvent.SET_UNITS, 100, "Calculating...");
		// set progress limit
		fireValueChanged(event);
		event.setId(AlgorithmEvent.PROGRESS_VALUE);
		event.setIntValue(0);
		fireValueChanged(event);

		//TODO Add check to see if this method returned successfully.
		runRAlg();
		
		if (stop) {
			throw new AbortException();
		}

		//Check for valid result for comparison analysis
		if(comparison && observed == null)
			throw new AbortException();
		//TODO write check for valid cox model run.
		
		if(comparison) {
			Vector<Float> medianSurvival = new Vector<Float>();
			medianSurvival.add(0, median(eventTimesGroup1.toArray(new Float[eventTimesGroup1.size()])));
			medianSurvival.add(1, median(eventTimesGroup2.toArray(new Float[eventTimesGroup2.size()])));
	        result.setMedians(medianSurvival);
	        
			Vector<Double> observedv = new Vector<Double>();
			for(int i=0; i<observed.length; i++) {
				observedv.add(observed[i]);
			}
	        if(observed.length <=0) {
	        	result.setEmptyResults(true);
	        	return result;
	        } 
	        result.setEmptyResults(false);
			Vector<Double> expectedv = new Vector<Double>();
			for(int i=0; i<expected.length; i++) {
				expectedv.add(expected[i]);
			}
			Double[][] variancematrix = new Double[varianceMatrix.length][];
			for(int i=0; i<varianceMatrix.length; i++) {
				variancematrix[i] = new Double[varianceMatrix[i].length];
				for(int j=0; j<varianceMatrix[i].length; j++) {
					variancematrix[i][j] = new Double(varianceMatrix[i][j]);
				}
			}

			result.addObjectMatrix("variances", variancematrix);
			//Add differential survival data to result object.
			result.setExpected(expected);
			result.setObserved(observed);
			result.setSizes(sizes);
			result.setChiSquare(new Float(chisq));
			result.setPValue(new Float(pvalue));
			result.addParam("degrees-freedom", new Integer(degFreedom).toString());
	        result.setLinearPredictors(linearPredictors);
	        result.setResiduals(residuals);
	        result.setResidualsNames(residualsNames);
			result.setCoefficient(coefficient);
			
		} else {
			
			result.setLambda(this.lambda1);
	        int[] allCoefficientIndices = result.getPenalizedCoefficientIndexes();
	        if(allCoefficientIndices.length <=0) {
	        	result.setEmptyResults(true);
	        	return result;
	        } 
	        result.setEmptyResults(false);
	        Vector<Double> allCoefficients = result.getPenalizedCoefficients();
	        int[][] clusters = separateCoefficients(allCoefficientIndices, allCoefficients);
	        result.setBasehazplotx(basehazplotx);
	        result.setBasehazploty(basehazploty);
	        result.setBasesurvSurvival(basesurvSurvival);
	        result.setBasesurvTime(basesurvTime);
	        result.setL1penalty(l1penalty);
	        result.setL2penalty(l2penalty);
	        result.setlogLikelihood(logLikelihood);
	        result.setResiduals(residuals);
	        result.setResidualsNames(residualsNames);
	        result.setFittedValues(fittedValues);
	        result.setLinearPredictors(linearPredictors);
			//Add Cox proportional hazard model data to result object.
			result.setCoefficient(coefficient);
			result.setVariance(variance);
			result.setInitialLogLik(initialloglik);
			result.setFinalLogLik(finalloglik);
			result.setNativeVar(nativevar);
			result.setScore(score);
			result.setRScore(rscore);
			result.setWaldTest(waldtest);
			result.setIterationCount(iterationcount);
			result.setResiduals(residuals);
			result.setMean(mean);
			result.setNumber(number);
			result.setCrossValLik(crossValidationLikelihood);
			if(weights != null && weights.length >0)
				result.setWeights(weights);
//			result.setMethod(method);
			result.setResClusters(clusters);
		}

				
		return result;   
	}
	//TODO Rename, refactor. 
	private int[][] separateCoefficients (int[] allCoefficientIndices, Vector<Double> allCoefficients) {
        int[][] clusters = new int[2][];
        int numZeroes = 0;
        for(int i=0; i<allCoefficientIndices.length; i++) {
        	if(allCoefficients.get(i) == 0)
        		numZeroes = numZeroes+1;
        }
        //Non-zero coefficients go in the first cluster.
        clusters[0] = new int[penalizedCoefficients.length - numZeroes];
        clusters[1] = new int[numZeroes];
        int zeroesCounter = 0; 
        int nonZeroesCounter = 0;
        for(int i=0; i<allCoefficientIndices.length; i++) {
        	if(allCoefficients.get(i) == 0) {
        		clusters[1][zeroesCounter] = allCoefficientIndices[i];
        		zeroesCounter++;
        	} else {
        		clusters[0][nonZeroesCounter] = allCoefficientIndices[i];
        		nonZeroesCounter++;
        	}
        }
        return clusters;
        

	}
	/**
	 * Function to create R session in memory and execute LIMMA
	 * @throws AbortException 
	 */
	public void runRAlg() throws AbortException {
		progress++;
		event.setId(AlgorithmEvent.PROGRESS_VALUE);
		event.setIntValue(10);
		fireValueChanged(event);

		Rengine re;
		try {
			re = RHook.startRSession();
			if(re == null) {
				JOptionPane.showMessageDialog(null, "Error creating R Engine",  "REngine", JOptionPane.ERROR_MESSAGE);
				//logger.writeln("Could not get REngine");
				throw new AbortException();
				//return;
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, e.getMessage(), "REngine", JOptionPane.ERROR_MESSAGE);
			//logger.writeln("Could not get REngine");
			throw new AbortException();
			//return;
		}

		try {
			System.out.println("Testing Survival install");
			RHook.testPackage("survival");
			// Raktim - testPackage() tests for all pkgs associated with a module
			// RHook.testPackage("penalized");
			RHook.log("Starting R Algorithim");
	
			String rCmd = "library(survival)";
			RHook.evalR(rCmd);
			rCmd = "library(penalized)";
			RHook.evalR(rCmd);
			File tempfile = File.createTempFile("mev_surv", ".txt");
			tempfile.deleteOnExit();
			writeEventDataToFile(tempfile.getAbsolutePath());
			

			
			String filename = tempfile.getAbsolutePath();
			filename = filename.replace("\\", "/");
			RHook.evalR("sampleAnns <- read.delim(\'" + filename + "\', header=TRUE, check.names=FALSE, sep='\\t', fill=FALSE, na.strings=c(\"N/A\", \"null\"))");
			
//			RHook.evalR("print(dim(sampleAnns))");
			if(comparison) {
				//Run comparison of the survival between the two groups. Get the data out.
				RHook.evalR("survdata <- survdiff(Surv(eventtime, censoredflag) ~ group, data=sampleAnns)");
				observed = RHook.evalR("survdata$obs").asDoubleArray();
				expected = RHook.evalR("survdata$exp").asDoubleArray();
				sizes = RHook.evalR("survdata$n").asIntArray();
				chisq = RHook.evalR("survdata$chisq").asDouble();
				varianceMatrix = RHook.evalR("survdata$var").asDoubleMatrix();
				pvalue = RHook.evalR("pchisq(survdata$chisq, 1, lower.tail=FALSE)").asDouble();
				degFreedom = 1;
				
				//Build Cox model of survival based on "group" variable. Get the data out.
				RHook.evalR("model <- coxph(Surv(eventtime, censoredflag) ~ group, data=sampleAnns)");
				coefficient = RHook.evalR("model$coefficients").asDouble();
				variance = RHook.evalR("model$var").asDouble();
				
				//a vector of length 2 containing the log-likelihood with the initial values and with the final values of the coefficients. 
				double[] loglikelihoods = RHook.evalR("model$loglik").asDoubleArray();
				initialloglik = loglikelihoods[0];
				finalloglik = loglikelihoods[1];		
				score = RHook.evalR("model$score").asDouble();
				if(robust) {
					nativevar = RHook.evalR("model$native.var").asDouble();
					rscore = RHook.evalR("model$rscore").asDouble();
				}
				waldtest = RHook.evalR("model$wald.test").asDouble();
				iterationcount = RHook.evalR("model$wald.iter").asInt();
				linearPredictors = RHook.evalR("model$linear.predictors").asDoubleArray();
				//TODO map residuals and their names into hashmap? 
				residuals = RHook.evalR("model$residuals").asDoubleArray();
				residualsNames = RHook.evalR("names(model$residuals)").asStringArray();
				mean = RHook.evalR("model$means").asDouble();
				number = RHook.evalR("model$n").asDouble();
				if(useWeights)
					weights = RHook.evalR("model$weights").asDoubleArray();
				method = RHook.evalR("model$method").asString();
	
			} else {
				//TODO
				//Write temp file with expression data in it
				File tempexprfile = File.createTempFile("mev_surv_expr", ".txt");
				tempexprfile.deleteOnExit();
				String[] geneLabels = new String[geneIndices.length];
				for(int i=0; i<geneIndices.length; i++)
					geneLabels[i] = new Integer(geneIndices[i]).toString();
				String fileLoc = writeMatrixToFile(tempexprfile.getAbsolutePath(), expression, geneLabels);
				fileLoc = fileLoc.replace("\\", "/");
				RHook.createRDataMatrixFromFile("expressionMatrix", fileLoc, true, sampleNames);
				RHook.evalR("alldata <- cbind(eventtime=sampleAnns$eventtime, censoredflag=sampleAnns$censoredflag, t(expressionMatrix))");
	
				RHook.evalR("alldata <- as.data.frame(alldata)");
				
				RHook.evalR(
						"pen <- penalized(Surv(alldata$eventtime, alldata$censoredflag), " + 
										"penalized = data.matrix(alldata[,c(3:dim(alldata)[[2]])])," + 
										"data = alldata, " + 
										"lambda1 = " + lambda1 + ", " + 
										"model='cox'" + 
						")"
					);
				int nonzero = RHook.evalR("length(coefficients(pen, 'nonzero'))").asInt();
				System.out.println("Console users: Do not trust the above printed number (# nonzero coefficients: XX)\n" +
						"This number may not be correctly reported to the console by R. \n" +
						"The actual number of nonzero coefficients calculated by R is " + nonzero);
	
				int fold=100;
				RHook.evalR(
						"cvl <- cvl(Surv(alldata$eventtime, alldata$censoredflag), " + 
										"penalized = data.matrix(alldata[,c(3:dim(alldata)[[2]])])," + 
										"data = alldata, " + 
										"lambda1 = " + lambda1 + ", " + 
										"model='cox'," + 
										"fold = " + fold + 
						")"
					);
				crossValidationLikelihood = RHook.evalR("cvl$cvl").asDouble();
				
				penalizedCoefficients = RHook.evalR("coefficients(pen, 'penalized')").asDoubleArray();
				//The gene row indexes (zero-indexed), used as names for the columns in the expressionMatrix R data structure.
				penalizedCoefficientIndexes = RHook.evalR("as.integer(names(coefficients(pen, 'penalized')))").asIntArray();
				basesurvTime = RHook.evalR("as.data.frame(basesurv(pen))$time").asDoubleArray();
				basesurvSurvival = RHook.evalR("as.data.frame(basesurv(pen))$survival").asDoubleArray();
				basehazplotx = RHook.evalR("basehaz(pen)$time").asDoubleArray();
				basehazploty = RHook.evalR("basehaz(pen)$hazard").asDoubleArray();
				logLikelihood = RHook.evalR("loglik(pen)").asDouble();
				l1penalty = RHook.evalR("penalty(pen)[[1]]").asDouble();
				l2penalty = RHook.evalR("penalty(pen)[[2]]").asDouble();
				residuals = RHook.evalR("residuals(pen)").asDoubleArray();
				fittedValues = RHook.evalR("fitted.values(pen)").asDoubleArray();
				linearPredictors = RHook.evalR("linear.predictors(pen)").asDoubleArray();
				weights = RHook.evalR("weights(pen)").asDoubleArray();
				
	
				curvesurvival = RHook.evalR("as.data.frame(basesurv(pen))$survival").asDoubleArray();
				curvetime = RHook.evalR("as.data.frame(basesurv(pen))$time").asDoubleArray();
				
				residualsNames = RHook.evalR("names(residuals(pen))").asStringArray();
				
				result.setPenalizedCoefficientIndexes(penalizedCoefficientIndexes);
				result.setPenalizedCoefficients(penalizedCoefficients);

				
			}
			
			RHook.endRSession();
		} catch (Exception e) {
			//TODO
			e.printStackTrace();
			RHook.log(e);
			try {
				RHook.endRSession();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		} 
		
	}

	private FloatMatrix getSubExperiment(FloatMatrix experiment, int[] features) {
		FloatMatrix subExperiment = new FloatMatrix(features.length, experiment.getColumnDimension());
		for (int i=0; i<features.length; i++) {
			subExperiment.A[i] = experiment.A[features[i]];
		}
		return subExperiment;
	}


	/**
	 * Checking the result of hcl algorithm calculation.
	 * @throws AlgorithmException, if the result is incorrect.
	 */
	private void validate(AlgorithmData result) throws AlgorithmException {
		if (result.getIntArray("child-1-array") == null) {
			throw new AlgorithmException("parameter 'child-1-array' is null");
		}
		if (result.getIntArray("child-2-array") == null) {
			throw new AlgorithmException("parameter 'child-2-array' is null");
		}
		if (result.getIntArray("node-order") == null) {
			throw new AlgorithmException("parameter 'node-order' is null");
		}
		if (result.getMatrix("height") == null) {
			throw new AlgorithmException("parameter 'height' is null");
		}
	}
	private NodeValueList calculateHierarchicalTree(int[] features, int method, boolean genes, boolean experiments) throws AlgorithmException {
		NodeValueList nodeList = new NodeValueList();
		AlgorithmData data = new AlgorithmData();
		FloatMatrix experiment = getSubExperiment(expMatrix, features);
		data.addMatrix("experiment", experiment);
		data.addParam("hcl-distance-function", String.valueOf(this.hcl_function));
		data.addParam("hcl-distance-absolute", String.valueOf(this.hcl_absolute));
		data.addParam("method-linkage", String.valueOf(method));
		HCL hcl = new HCL();
		AlgorithmData result;
		if (genes) {
			data.addParam("calculate-genes", String.valueOf(true));
			data.addParam("optimize-gene-ordering", String.valueOf(hcl_genes_ordered));
			result = hcl.execute(data);
			validate(result);
			addNodeValues(nodeList, result);
		}
		if (experiments) {
			data.addParam("calculate-genes", String.valueOf(false));
			data.addParam("optimize-sample-ordering", String.valueOf(hcl_samples_ordered));
			result = hcl.execute(data);
			validate(result);
			addNodeValues(nodeList, result);
		}
		return nodeList;
	}

	private void addNodeValues(NodeValueList target_list, AlgorithmData source_result) {
		target_list.addNodeValue(new NodeValue("child-1-array", source_result.getIntArray("child-1-array")));
		target_list.addNodeValue(new NodeValue("child-2-array", source_result.getIntArray("child-2-array")));
		target_list.addNodeValue(new NodeValue("node-order", source_result.getIntArray("node-order")));
		target_list.addNodeValue(new NodeValue("height", source_result.getMatrix("height").getRowPackedCopy()));
	}


	int validN;


	private String writeMatrixToFile(String fileLoc, FloatMatrix fm, String[] rowNames) {
		
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(fileLoc));

			int row = fm.getRowDimension();
			int col = fm.getColumnDimension();
			String srtVector = "";
			for(int iRow = 0; iRow < row; iRow++) {
				srtVector = rowNames[iRow] + "\t";
				for(int jCol = 0; jCol < col; jCol++) {
					if(jCol == col-1)
						srtVector += fm.get(iRow, jCol) + "\n";
					else 
						srtVector += fm.get(iRow, jCol) + "\t";
				}
				out.write(srtVector);
				srtVector = "";
			}
			out.close();
		} catch(IOException e) {
			return null;
		}
		return fileLoc;
	}

	private String writeEventDataToFile(String fileLoc) {
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(fileLoc));
			
			//TODO change these literals to static variables
			out.write("eventtime\tcensoredflag\tgroup\n");
			
			String rowContents = "";
			Boolean flag;
			for(int i=0; i<eventStatusGroup1.size(); i++) {
				//invert flag because R is weird.
				flag = !eventStatusGroup1.get(i);
				rowContents = origIndexGroup1.get(i) + "\t" + eventTimesGroup1.get(i) + "\t" + flag.toString().toUpperCase() + "\t" + "group1\n";
				out.write(rowContents);
			}

			for(int i=0; i<eventStatusGroup2.size(); i++) {
				//Invert flag because R is weird.
				flag = !eventStatusGroup2.get(i);
				rowContents = origIndexGroup2.get(i) + "\t" + eventTimesGroup2.get(i) + "\t" + flag.toString().toUpperCase() + "\t" + "group2\n";
				out.write(rowContents);
			}

			out.close();
		} catch(IOException e) {
			return null;
		}
		return fileLoc;
	}


	private String getOneClassDesign(){
		String str = "design <- c(";
		for (int i=0; i<groupAssignments.length; i++){
			str = str + Integer.toString(groupAssignments[i]);
			if (i<groupAssignments.length-1)
				str = str  + ", ";
		}
		str = str + ")";
		//System.out.println("str design: " + str);
		return str;
	}

	private String getTwoClassDesign(){
		String grp1 = "";
		String grp2 = "";
		for (int i=0; i<groupAssignments.length; i++){
			if (groupAssignments[i]==1){
				grp1 = grp1 + "1";
				grp2 = grp2 + "0";
			} else if (groupAssignments[i]==2){
				grp1 = grp1 + "1";
				grp2 = grp2 + "1";
			} else {
				grp1 = grp1 + "0";
				grp2 = grp2 + "0";
			}
			if (i<groupAssignments.length-1){
				grp1 = grp1 + ", ";
				grp2 = grp2 + ", ";
			}
		}
		String str = "design <- cbind(Grp1=c(" + grp1 + "),Grp2=c(" + grp2 + "))";
		//System.out.println("str design: " + str);
		return str;
	}

	private String getMultiClassDesign(){
		String[] grpArray = new String[numGroups];
		for (int i=0; i<numGroups; i++){
			grpArray[i] = "";
		}
		for (int i=0; i<groupAssignments.length; i++){
			for (int j=0; j<numGroups; j++){
				if (groupAssignments[i]==j+1)
					grpArray[j] = grpArray[j] + "1";
				else
					grpArray[j] = grpArray[j] + "0";
			}

			if (i<groupAssignments.length-1){
				for (int j=0; j<numGroups; j++){
					grpArray[j] = grpArray[j] + ", ";
				}
			}
		}
		String str = "design <- cbind(";
		for (int i=0; i<numGroups; i++){
			str = str + "Grp"+(i+1)+"=c("+grpArray[i]+")";
			if (i<numGroups-1){
				str = str + ", ";
			}
		}
		str = str +")";
		//System.out.println("str design: " + str);
		return str;
	}

	private String getTwoFactorDesign(){
		String str = "design <- model.matrix(~0 + TS)";

		return str;		
	}

	private String getTimeCourseDesign(){
		String str = "design <- model.matrix(~0 + TS)";

		return str;		
	}


	public void updateProgressBar(){

		progress++;
		event.setId(AlgorithmEvent.PROGRESS_VALUE);
		event.setIntValue((100*progress)/(progress+7));
	}
	public static Float median(Float a[]) {
		Float[] b = new Float[a.length];
		System.arraycopy(a, 0, b, 0, b.length);
		Arrays.sort(b);

		if (a.length % 2 == 0) {
			return new Float((b[(b.length / 2) - 1] + b[b.length / 2]) / 2.0);
		} else {
			return new Float(b[b.length / 2]);
		}
	}
	

}
