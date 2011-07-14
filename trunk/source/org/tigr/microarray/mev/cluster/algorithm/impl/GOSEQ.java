/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/**
 * @author  dschlauch
 * @version
 */
package org.tigr.microarray.mev.cluster.algorithm.impl;


import org.rosuda.JRI.REXP;
import org.rosuda.JRI.Rengine;
import org.tigr.rhook.RConstants;
import org.tigr.rhook.RHook;
import org.tigr.util.FloatMatrix;
import org.tigr.microarray.mev.cluster.Cluster;
import org.tigr.microarray.mev.cluster.algorithm.AbortException;
import org.tigr.microarray.mev.cluster.algorithm.AbstractAlgorithm;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmEvent;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmException;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmParameters;
import org.tigr.microarray.mev.cluster.algorithm.impl.ease.EaseAlgorithmData;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import java.util.Vector;

public class GOSEQ extends AbstractAlgorithm{
	private FloatMatrix expMatrix;
	private boolean stop = false;
	private boolean isGO;
	private static int MSigDBFile = 1;
	private int binSize = 10;
	private int numGenes, numExps, numGroups, iteration,progress,binCount, numPerms, geneSetOrigin;
	private int[] transcriptLengths, binIndex, diffGeneCluster, groupAssignments;
	private int[][] geneLists;
	private float[] diffGenesCollapsed, transcriptLengthsCollapsed, diffExpressedPerBin, aveLengthPerBin, pwfByBin, expSigPerGeneSet, sigPerGeneSet, genesetPValues, pwf;
	private AlgorithmEvent event;
	private String[] geneSetFilePaths, geneListsNames, geneNames, collapsedGeneNames, categoryNames;
	ArrayList<String> geneNameAL = new ArrayList<String>();
	
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
		AlgorithmParameters map = data.getParams();
        numPerms = map.getInt("numPerms");
        binSize = map.getInt("numGenesPerBin");
        isGO = map.getBoolean("isGO");
		expMatrix = data.getMatrix("experiment");
		diffGeneCluster = data.getIntArray("diffGeneCluster");
		
		geneSetFilePaths = data.getStringArray("geneSetFilePaths");
		geneSetOrigin = map.getInt("geneSetOrigin", 0);
		geneNames = data.getStringArray("geneLabels");
		if (isGO){
			geneLists = ((EaseAlgorithmData)data).getClusterMatrix();
			geneListsNames = new String[((EaseAlgorithmData)data).getResultMatrix().length];
			for (int i=0; i<geneListsNames.length; i++){
				geneListsNames[i] = ((EaseAlgorithmData)data).getResultMatrix()[i][2];
			}
		}
        categoryNames = new String[4];
        categoryNames[0] = "Null";
        categoryNames[1] = "GO Biological Process";
        categoryNames[2] = "GO Cellular Component";
        categoryNames[3] = "GO Molecular Function";
        
        
		transcriptLengths = data.getIntArray("transcriptLengths");
		progress=0;
		event = null;
		event = new AlgorithmEvent(this, AlgorithmEvent.SET_UNITS, 100, "Calculating...");
		// set progress limit
		fireValueChanged(event);
		event.setId(AlgorithmEvent.PROGRESS_VALUE);
		event.setIntValue(0);
		fireValueChanged(event);

		numGenes= expMatrix.getRowDimension();
		numExps = expMatrix.getColumnDimension();
		
		runAlgorithm();
		
		if (stop)
			throw new AbortException();

		Cluster result_cluster = new Cluster();

		// prepare the result
		FloatMatrix returnMatrix = new FloatMatrix(genesetPValues.length,2);
		for (int i=0; i<genesetPValues.length; i++){
			returnMatrix.A[i][0] = geneLists[i].length;
			returnMatrix.A[i][1] = genesetPValues[i];
		}
		FloatMatrix pwfM = new FloatMatrix(1,pwf.length);
		pwfM.A[0]=pwf;
		FloatMatrix pwfM2 = new FloatMatrix(3,aveLengthPerBin.length);
		pwfM2.A[0]=aveLengthPerBin;
		pwfM2.A[1]=diffExpressedPerBin;
		pwfM2.A[2]=pwfByBin;
		FloatMatrix sigPerGS_FM = new FloatMatrix(2,expSigPerGeneSet.length);
		sigPerGS_FM.A[0]=expSigPerGeneSet;
		sigPerGS_FM.A[1]=sigPerGeneSet;
		data.addParam("iterations", String.valueOf(iteration-1));
		data.addCluster("cluster", result_cluster);
		data.addParam("number-of-clusters", "1"); 
		data.addMatrix("geneGroupMeansMatrix", getAllGeneGroupMeans());
		data.addMatrix("geneGroupSDsMatrix", getAllGeneGroupSDs());
		data.addMatrix("resultsMatrix", returnMatrix);
		data.addMatrix("pwf", pwfM);
		data.addStringArray("gene-list-names",this.geneListsNames);
		data.addMatrix("pwfM2",pwfM2);
		data.addMatrix("geneSetSigs",sigPerGS_FM);
		data.addStringArray("category-names", categoryNames);
		data.addIntMatrix("gene-lists", geneLists);
		return data;   
	}


	/**
	 * Converts an array of transcript lengths to an int array of the length gene_symbol 
	 * describing which bin each gene_symbol falls into
	 * @param transcriptLengths2
	 * @return
	 */
	private int[] convertTranscriptLengthToBinIndex(int[] transcriptLengths2) {
		float[] sumOfLengthOfGene = new float[this.geneNameAL.size()];
		float[] countOfGene = new float[this.geneNameAL.size()];
		for (int i=0; i<transcriptLengths2.length; i++){
			int mapped = geneNameAL.indexOf(geneNames[i]);
			if (mapped==-1)
				continue;
			countOfGene[mapped]++;
			sumOfLengthOfGene[mapped] = sumOfLengthOfGene[mapped]+transcriptLengths2[i];
		}
		
		transcriptLengthsCollapsed = new float[geneNameAL.size()];
		for (int i=0; i<transcriptLengthsCollapsed.length; i++){
			transcriptLengthsCollapsed[i] = sumOfLengthOfGene[i]/countOfGene[i];
		}
		
		double[] indices = new double[transcriptLengthsCollapsed.length];
		for (int i=0; i<indices.length; i++){
			indices[i] = i;
		}
		double[] lengths = new double[transcriptLengthsCollapsed.length];
		for (int i=0; i<lengths.length; i++){
			lengths[i] = transcriptLengthsCollapsed[i];
		}
		double[] bins = new double[transcriptLengthsCollapsed.length];
		for (int i=0; i<bins.length; i++){
			bins[i] = i/binSize;
		}
		ExperimentUtil.sort2(lengths, indices);
		ExperimentUtil.sort2(indices, bins);
		
		int[] rtrn = new int[bins.length];
		for (int i=0; i<rtrn.length; i++){
			rtrn[i] = (int)bins[i];
		}
		return rtrn;
	}
	
	public static void main(String[] args){
		GOSEQ gs = new GOSEQ();
		gs.binSize = 2;
		gs.getBinDetails();
		for (int i=0; i<gs.binCount; i++){
			System.out.println(gs.aveLengthPerBin[i]);
			System.out.println(gs.diffExpressedPerBin[i]);
		}
	}
	
	private void runAlgorithm() {
		collapseProbesAndRemoveNaNs();
		if (!isGO)
			if (!populateGeneListMatrix(geneSetFilePaths)){
				stop = true;
				return;
			}
		diffGenesCollapsed = reformatDiffGenes(this.diffGeneCluster);			
		binIndex = convertTranscriptLengthToBinIndex(transcriptLengths);
		getBinDetails();
		
		try {
			runRAlg();
		} catch (AbortException e) {
			e.printStackTrace();
		}
		
		LengthBiasPValues lbpv = new LengthBiasPValues(diffGenesCollapsed, pwf, geneLists);
		genesetPValues = lbpv.getPValuesForGeneSets(numPerms);
		expSigPerGeneSet = lbpv.getExpectedSigPerGeneSet();
		sigPerGeneSet = lbpv.getSigPerGeneSet();
	}
	@SuppressWarnings("unchecked")
	private boolean populateGeneListMatrix(String[] geneSetFilePaths) {
		int titleIndex = (geneSetOrigin == MSigDBFile) ? 0:1;
		ArrayList<ArrayList> al = new ArrayList<ArrayList>();
		ArrayList<String> namesal = new ArrayList<String>();
		try {			
			for (int fileIndex=0; fileIndex<geneSetFilePaths.length; fileIndex++){			
				BufferedReader br = new BufferedReader(new FileReader(geneSetFilePaths[fileIndex]));
				String line;
				while( (line = br.readLine()) != null){
					line.trim();
					String[] genes = line.split("\t");
					
					if (genes.length<3)
						continue;
					//if enough genes...
					al.add(new ArrayList<Integer>());
					boolean first = true;
					for (int i=2; i<genes.length; i++){
						if (geneNameAL.contains(genes[i])){
							al.get(al.size()-1).add(geneNameAL.indexOf(genes[i]));		
							first = false;
						}
					}
					if (first){  //checks to see if there were no matching indices
						al.remove(al.size()-1);
					} else {
						namesal.add(genes[titleIndex]);
					}
				}
				br.close();
			}			
			this.geneLists = new int[al.size()][];
			this.geneListsNames = new String[namesal.size()];
			
			for (int i=0; i<al.size(); i++){
				geneLists[i]=new int[al.get(i).size()];
				for (int j=0; j<al.get(i).size(); j++){
					geneLists[i][j] = (Integer)al.get(i).get(j);
				}
			}
			for (int i=0; i<namesal.size(); i++){
				geneListsNames[i]=namesal.get(i);
			}
			if (geneLists.length==0||geneListsNames.length==0)
				return false;
			else	
				return true;
		} catch (Exception e){
			e.printStackTrace();
			return false;
		}
	}
	/**
	 * Convert an cluster of DE genes into an array of differential expressed of length 'genes' 
	 * and then to one of length 'gene symbols' (or other collapsed annotation) which
	 * describes percent of each symbol that are DE
	 * @param diffGeneCluster
	 * @return float[] that is percent DE mapped to geneNameAL
	 */
	private float[] reformatDiffGenes(int[] diffGeneCluster) {
		int[] diffGenes = new int[this.numGenes];
		for (int i=0; i<diffGenes.length; i++)
			diffGenes[i]=0;
		for (int i=0; i<diffGeneCluster.length; i++)
			diffGenes[diffGeneCluster[i]]=1;
		

		float[] numProbesPerGeneSymbol = new float[geneNameAL.size()];
		float[] numSigProbesPerGeneSymbol = new float[geneNameAL.size()];
		for (int i=0; i<diffGenes.length; i++){
			int mapped = geneNameAL.indexOf(geneNames[i]);
			if (mapped==-1)
				continue;
			numProbesPerGeneSymbol[mapped]++;
			numSigProbesPerGeneSymbol[mapped] = numSigProbesPerGeneSymbol[mapped]+diffGenes[i];
		}
		
		float[] diffGenesCollapsed = new float[geneNameAL.size()];
		for (int i=0; i<diffGenesCollapsed.length; i++){
			diffGenesCollapsed[i] = numSigProbesPerGeneSymbol[i]/numProbesPerGeneSymbol[i];
		}
		return diffGenesCollapsed;
	}
	
	private void collapseProbesAndRemoveNaNs() {
		geneNameAL = new ArrayList<String>();
		ArrayList<Integer> indicesMap = new ArrayList<Integer>();
		for (int i=0; i<expMatrix.getRowDimension(); i++){
			//determine if gene has NaNs
			boolean hasNaNs = false;
			for (int j=0; j<expMatrix.getColumnDimension(); j++){
				if (Float.isNaN(expMatrix.A[i][j])){
					hasNaNs = true;
					break;
				}
			}
			if (hasNaNs)
				continue;
			
			//add to data if not already added
			if (!geneNameAL.contains(geneNames[i])){
				geneNameAL.add(geneNames[i]);
				indicesMap.add(i);
			}
		}
		collapsedGeneNames = new String[indicesMap.size()];
		float[][] tempCollapsed = new float[indicesMap.size()][expMatrix.A[0].length];
		for (int i=0; i<indicesMap.size(); i++){
			collapsedGeneNames[i] = geneNames[indicesMap.get(i)].equals("NA")?"Unknown":geneNames[indicesMap.get(i)];
			for (int j=0; j<expMatrix.A[i].length; j++){
				tempCollapsed[i][j] = expMatrix.A[indicesMap.get(i)][j];
			}
		}
		
	}

	private float[] getGeneGroupMeans(int gene) {
		float[] geneValues = new float[numExps];
		for (int i = 0; i < numExps; i++) {
			geneValues[i] = expMatrix.A[gene][i];
		} 

		float[][] geneValuesByGroups = new float[numGroups][];

		for (int i = 0; i < numGroups; i++) {
			geneValuesByGroups[i] = getGeneValuesForGroup(geneValues, i+1);
		} 

		float[] geneGroupMeans = new float[numGroups];
		for (int i = 0; i < numGroups; i++) {
			geneGroupMeans[i] = getMean(geneValuesByGroups[i]);
		}
		return geneGroupMeans;
	}

	private float[] getGeneGroupSDs(int gene) {
		float[] geneValues = new float[numExps];        
		for (int i = 0; i < numExps; i++) {
			geneValues[i] = expMatrix.A[gene][i];
		}   
		float[][] geneValuesByGroups = new float[numGroups][];
		for (int i = 0; i < numGroups; i++) {
			geneValuesByGroups[i] = getGeneValuesForGroup(geneValues, i+1);
		}  
		float[] geneGroupSDs = new float[numGroups];
		for (int i = 0; i < numGroups; i++) {
			geneGroupSDs[i] = getStdDev(geneValuesByGroups[i]);
		}
		return geneGroupSDs;        
	}

	private float[] getGeneValuesForGroup(float[] geneValues, int group) {
		Vector<Float> groupValuesVector = new Vector<Float>();

		for (int i = 0; i < groupAssignments.length; i++) {
			if (groupAssignments[i] == group) {
				groupValuesVector.add(new Float(geneValues[i]));
			}
		}
		float[] groupGeneValues = new float[groupValuesVector.size()];
		for (int i = 0; i < groupValuesVector.size(); i++) {
			groupGeneValues[i] = ((groupValuesVector.get(i))).floatValue();
		}

		return groupGeneValues;
	}

	private FloatMatrix getAllGeneGroupMeans() {
		FloatMatrix means = new FloatMatrix(numGenes, numGroups);
		for (int i = 0; i < means.getRowDimension(); i++) {
			means.A[i] = getGeneGroupMeans(i);
		}
		return means;
	}
	private FloatMatrix getAllGeneGroupSDs() {
		FloatMatrix sds = new FloatMatrix(numGenes, numGroups);
		for (int i = 0; i < sds.getRowDimension(); i++) {
			sds.A[i] = getGeneGroupSDs(i);
		}
		return sds;        
	}


	private float getMean(float[] group) {
		float sum = 0;
		int n = 0;

		for (int i = 0; i < group.length; i++) {
			if (!Float.isNaN(group[i])) {
				sum = sum + group[i];
				n++;
			}
		}
		if (n == 0) {
			return Float.NaN;
		}
		float mean =  sum / n;

		if (Float.isInfinite(mean)) {
			return Float.NaN;
		}
		return mean;
	}

	private float getStdDev(float[] group) {
		float mean = getMean(group);
		int n = 0;

		float sumSquares = 0;

		for (int i = 0; i < group.length; i++) {
			if (!Float.isNaN(group[i])) {
				sumSquares = (float)(sumSquares + Math.pow((group[i] - mean), 2));
				n++;
			}
		}

		if (n < 2) {
			return Float.NaN;
		}

		float var = sumSquares / (n - 1);
		if (Float.isInfinite(var)) {
			return Float.NaN;
		}
		return (float)(Math.sqrt(var));
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
				throw new AbortException();
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, e.getMessage(), "REngine", JOptionPane.ERROR_MESSAGE);
			throw new AbortException();
		}

		try {
//		System.out.println("Testing GOSEQ install");
		//RHook.testPackage("goseq");
		if (RHook.getOS() == RConstants.MAC_OS ||
				RHook.getOS() == RConstants.WINDOWS_OS) {
			RHook.testPackage("goseq");
		} else {
			RHook.installModule("mgcv");
		}
//		System.out.println("Loading Lib goseq");
		RHook.log("Starting R Algorithim");
		
		String rCmd = "library(mgcv)";
		RHook.evalR(rCmd);

		String fileLoc = System.getProperty("user.dir")+System.getProperty("file.separator")+"tmpfile.txt";
		fileLoc = fileLoc.replace("\\", "/");
		
		String rCmdx = "x <- c(";
		String rCmdy = "y <- c(";
		for (int i=0; i<binCount; i++){
			rCmdx = rCmdx + aveLengthPerBin[i];
			rCmdy = rCmdy + diffExpressedPerBin[i];
			if (i<binCount-1){
				rCmdx = rCmdx + ",";
				rCmdy = rCmdy + ",";
			}
		}
		rCmdx = rCmdx + ")";
		rCmdy = rCmdy + ")";
		RHook.evalR(rCmdx);
		RHook.evalR(rCmdy);
		RHook.evalR("dat<-data.frame(x=x,y=y)");
		RHook.evalR("f.ug<-gam(y~s(x,bs='cr'))");
		RHook.evalR("sm<-smoothCon(s(x,bs='cr'),dat,knots=NULL)[[1]]");
		RHook.evalR("F<-mono.con(sm$xp)");
		RHook.evalR("G<-list(X=sm$X,C=matrix(0,0,0),sp=f.ug$sp,p=sm$xp,y=y,w=y*0+1)");
		RHook.evalR("G$Ain<-F$A");
		RHook.evalR("G$bin<-F$b");
		RHook.evalR("G$S<-sm$S");
		RHook.evalR("G$off<-0");
		RHook.evalR("p<-pcls(G)");
		RHook.evalR("fv<-Predict.matrix(sm,data.frame(x=x))%*%p");
		
		REXP e = RHook.evalR("fv");
		double[][] matrix = e.asMatrix();
		pwf = new float[diffGenesCollapsed.length];
		for (int i=0; i<pwf.length; i++){
			pwf[i] = Math.max(0.0f,(float)matrix[binIndex[i]][0]);
		}
		pwfByBin = new float[matrix.length];
		for (int i=0; i<matrix.length; i++){
			pwfByBin[i] = (float)matrix[i][0];
		}
		
		} catch (Exception e) {
			RHook.log(e);
			try {
				RHook.endRSession();
				e.printStackTrace();
				JOptionPane.showMessageDialog(null, e.getMessage(), "REngine", JOptionPane.ERROR_MESSAGE);
				//throw new AlgorithmException(e);
				throw new AbortException();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}
	private void getBinDetails() {
		binCount = (diffGenesCollapsed.length-1)/binSize+1;
		diffExpressedPerBin = new float[binCount];
		aveLengthPerBin = new float[binCount];
		for (int i=0; i<binCount; i++){
			int numGenesInBin = 0;
			int numSigGenesInBin = 0;
			int totalLengthInBin = 0;
			for (int j=0; j<diffGenesCollapsed.length; j++){
				if (this.binIndex[j] == i){
					numGenesInBin++;
					totalLengthInBin = totalLengthInBin + (int)this.transcriptLengthsCollapsed[j];
					if (diffGenesCollapsed[j]==1)
						numSigGenesInBin++;
				}
			}
			aveLengthPerBin[i] = (float)totalLengthInBin/(float)numGenesInBin;
			diffExpressedPerBin[i] = (float)numSigGenesInBin/(float)numGenesInBin;
		}		
	}
	public void updateProgressBar(){

		progress++;
		event.setId(AlgorithmEvent.PROGRESS_VALUE);
		event.setIntValue((100*progress)/(progress+7));
	}
}
