/*
Copyright @ 1999-2007, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * Created on Oct 19, 2006
 * braisted
 * 
 */
package org.tigr.microarray.mev.cluster.algorithm.impl.nonpar;

import java.awt.Frame;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.tigr.microarray.mev.cluster.Cluster;
import org.tigr.microarray.mev.cluster.Node;
import org.tigr.microarray.mev.cluster.NodeList;
import org.tigr.microarray.mev.cluster.NodeValue;
import org.tigr.microarray.mev.cluster.NodeValueList;
import org.tigr.microarray.mev.cluster.algorithm.AbstractAlgorithm;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmEvent;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmException;
import org.tigr.microarray.mev.cluster.algorithm.impl.HCL;
import org.tigr.microarray.mev.cluster.gui.impl.nonpar.NonparFDRDialog;
import org.tigr.microarray.mev.r.REXP;
import org.tigr.microarray.mev.r.RList;
import org.tigr.microarray.mev.r.RSrvException;
import org.tigr.microarray.mev.r.Rconnection;
import org.tigr.microarray.mev.r.RconnectionManager;
import org.tigr.util.FloatMatrix;
import org.tigr.util.QSort;

import JSci.maths.DoubleMatrix;
import JSci.maths.DoubleSquareMatrix;
import JSci.maths.statistics.ChiSqrDistribution;
import JSci.maths.statistics.NormalDistribution;

/**
 * @author braisted
 *
 * 1.30.07
 * 
 * updated 4.18.2007  //modified after non-R implementations were made
 * 
 * NOTE: INITIAL IMPLEMENTATIONS OF WILCOXON AND KRUSKAL-WALLIS WERE USING R AND RSERVE.
 * BOTH OF THESE WERE IMPLEMENTED IN METHODS IN THIS CLASS SO THAT THERE IS NO RELIANCE ON R
 * AND RSERVE.  THE ORIGINAL METHODS USING R AND RSERVE AND SUPPORTING METHODS ARE GROUPED AT
 * THE END OF THIS SECTION IN THE EVENT THAT ONE WOULD WANT TO UTILIZE THIS OPTION OF USING R.
 * 
 * 
 * Nonpar contains code to execute code to perform non-parameteric tests
 * Wilcoxon Rank Sum, Kruskal-Wallis, or Mack-Skillings (perhpas more to come)
 * 
 * All tests are described in Hallander and Wolfe, Nonparameteric Statistical Methods
 * 1999. 2nd Edition, Wiley and Sons, NY.
 * 
 * A general word on non-parametric tests.  These specific methods each have assumptions
 * that are outlined in Hollander and Wolfe but include the fact that the observations
 * are take from samples from populations that are assumed to have the same population
 * distribution but that the underlying distribution need not necessarily be normal.
 * Sometime refered to 'distribution free' these methods are distinct from the erronious
 * idea of 'distribution-less'.  There is a single underlying distribution for all populations
 * sampled but it is not constrained to be the normal distribution.
 *  
 * Test Descriptions:
 *
 * Wilcoxon Rank Sum (simlar to Mann Whitney, linear relationship between statistics)
 * is for two samples, described in Hollander and Wolfe.  This is a two group test 
 * that is roughly analogous to a ttest but uses a statistic based on ranking the observations
 * in the group.  For instance, if all of the values in one group are greater or all less than
 * the other group then the statistic falls away from an expected score if the rankings were
 * interspersed.  The basic W statistic is the sum of all rankings in one group.
 * 
 * The Kruskal-Wallis test is used for n experimental groups.
 * This is a non-parametric analog to one-way ANOVA
 * 
 * The Friedman test implemented in R is only applicable to a complete design with
 * no replication.  The Mack-Skillings test described in Hollander and Wolfe allows for
 * replication with balanced designs *and* a generalization for cases where the design
 * is unbalanced.  The Mack-Skillings test is implemented in this class and
 * uses either the balanced design method or the generalization for unbalanced design
 * when needed.
 */

public class Nonpar extends AbstractAlgorithm {

	//execution modes
	private String MODE_WILCOXON_MANN_WHITNEY = "nonpar-mode-wilcoxon-mann-whitney";
	private String MODE_KRUSKAL_WALLIS = "nonpar-mode-kruscal-wallis";
	private String MODE_MACK_SKILLINGS = "nonpar-mode-mack-skillings";	
	private String MODE_FISHER_EXACT = "nonpar-fisher-exact";	

	//fraction of the data that should be run on each batch run
	private float FRACTION_PER_BATCH = 0.1f;
	private int MAX_BATCH_SIZE = 1000;
	
	private String INSUFFICIENT_N = "Skipped -- Insufficient Data";
		
	//design descriptions for two-way designs
	private String DESIGN_DESC_INCOMPLETE = "Incomplete Design";
	private String DESIGN_DESC_BALANCED = "Balanced Design";
	private String DESIGN_DESC_UNBALANCED = "Unbalanced Design";
	
	//two way meothd descriptions
	private String METHOD_MACK_SKILLINGS = "Mack-Skillings";
	private String METHOD_GENERALIZED_MACK_SKILLINGS = "Mack-Skillings (generalized for unbal.)";
	private String METHOD_NONE_INCOMPLETE = "None (incomplete design)";
			
	//algorithm data with parameters and ready to take results
	private AlgorithmData algData;
	
	
	
	/**
	 * Executes Wilcoxon, Kruskal, or Friedman test
	 * @param data algorithm data (parameters needed for Nonpar)
	 */
	public AlgorithmData execute(AlgorithmData data) throws AlgorithmException {

		//algorithm parameters
		algData = data;
		FloatMatrix matrix = data.getMatrix("matrix");

		//test mode
		String mode = data.getParams().getString("nonpar-mode");

		//send the matrix to a test
		//algData is global so everyone can grab parameters
		if(mode.equals(MODE_WILCOXON_MANN_WHITNEY)) {
			executeWilcoxonTest(matrix);
		} else if(mode.equals(MODE_KRUSKAL_WALLIS)) {
			executeKruskalWallisTest(matrix);
		} else if(mode.equals(MODE_MACK_SKILLINGS)) {
			executeMackSkillings(matrix);
		} else if(mode.equals(MODE_FISHER_EXACT)) {
			executeFisherExactTest(matrix);
		}
		return algData;
	}
	
	public void abort() {
		
	}

	
	/** Executes the Wilcoxon Rank Sum test on a matrix of data.
	 * Precondtions: global AlgorithmData (algData) has parameters for group selections
	 * 
	 * Postcondition: global algData recieves FloatMatrix resultMatrix with result values
	 * and a String [] called methods that describes the method.
	 * 
	 * @param m <code>FloatMatrix</code> that contains the data values
	 */
	private void executeWilcoxonTest(FloatMatrix m) throws AlgorithmException {
		
		boolean useAlpha = algData.getParams().getBoolean("use-alpha-criterion", true);
		Vector sigGenes, nonSigGenes; //for accumulation of gene sets
		
		int [] groups = algData.getIntArray("group-assignments");
		
		//arrays for cluster indices
		int [][] clusters = new int[2][];
		
		//indicate the methods used, important if there are ties
		String [] method = new String[m.getRowDimension()];
		
		// capture n, m, W, W*, pValue, possibly adjPValue
		FloatMatrix resultMatrix = new FloatMatrix(m.getRowDimension(), useAlpha ? 5 : 6);
		
		this.fireValueChanged(new AlgorithmEvent(this, AlgorithmEvent.PROGRESS_VALUE, 0, "Executing Wilcoxon Rank Sum"));
		
		
		int numRows = m.getRowDimension();
		float [] dataVals;
		int [] groupCounts;
		float [] p_values = new float[m.getRowDimension()]; 
		double [] resultVals;
		
		//data organizer
		NonparOneWayData dataObj;
		
		for(int iter = 0; iter < numRows; iter++ ) {				
			
			//grabs the non-NaN values and makes a new group array (if needed)
			dataObj = new NonparOneWayData(m.A[iter], groups, 2);
			
			groupCounts = dataObj.getGroupNs();
			
			//add group counts
			resultMatrix.set(iter, 0, groupCounts[0]);
			resultMatrix.set(iter, 1, groupCounts[1]);
			
			//check to make sure both groups are represented
			if(!dataObj.areGroupsNull) {
				//grab the values
				dataVals = dataObj.getValues();				
				
				//comput an array of result values
				resultVals = this.wilcoxonImpl(dataVals, dataObj.getGroups(), groupCounts[0], groupCounts[1]);
				
				//first is W
				resultMatrix.set(iter, 2, (float)resultVals[0]);
				//next is W*, large sample normal approx.
				resultMatrix.set(iter, 3, (float)resultVals[1]);
				//next is p-value
				p_values[iter] = (float)resultVals[2];
				resultMatrix.set(iter, 4, (float)p_values[iter]);
				
				//if we are adjusting p-values, set adjustment for NaN's to NaN
				if(Float.isNaN(p_values[iter])&& !useAlpha)
					resultMatrix.set(iter, 5, Float.NaN);
				
				method[iter] = "(W*) Large Sample Approx.";
				
				//numerical flag indicates tied ranks... append a note
				if(resultVals[3] == 1.0)
					method[iter] += " w/ corr. for tied ranks";
				
			} else {
				//can't run test, empty group, set p to 1?					
				resultMatrix.set(iter, 0, groupCounts[0]);
				resultMatrix.set(iter, 1, groupCounts[1]);
				resultMatrix.set(iter, 2, Float.NaN);
				resultMatrix.set(iter, 3, Float.NaN);
				resultMatrix.set(iter, 4, Float.NaN);
				p_values[iter] = Float.NaN;
				if(!useAlpha)
					resultMatrix.set(iter, 5, Float.NaN);
				
				method[iter] = "None, empty group(s)";
			}				
			
			this.fireValueChanged(new AlgorithmEvent(this, AlgorithmEvent.PROGRESS_VALUE, (int)(((float)iter/(float)numRows)*100f), "Executing Wilcoxon Rank Sum (finished batch "+iter+")"));				
		} //end iterations
		
		this.fireValueChanged(new AlgorithmEvent(this, AlgorithmEvent.PROGRESS_VALUE, 100, "Processing Results"));
		
		//holds indices
		int [] sigGenesArray;
		int [] nonSigGenesArray;
		
		//if using FDR
		if(!useAlpha) {
			
			//need to cull out NaN p_values... from flat genes or genes with 0 group sizes
			//grab valid and invalid (NaN) indices
			Vector validP = new Vector();
			Vector validInd = new Vector();
			Vector invalidInd = new Vector();
			
			for(int i = 0; i < p_values.length; i++) {
				if(!Float.isNaN(p_values[i])) {
					validP.add(new Float(p_values[i]));
					validInd.add(new Integer(i));						
				} else {
					invalidInd.add(new Integer(i));
				}					
			}
			
			int validCnt = validP.size();
			p_values = new float[validCnt];
			
			//put the valid indices into an array
			for(int i = 0; i < validCnt; i++) {
				p_values[i] = ((Float)(validP.get(i))).floatValue();
			}
			
			//execute bh and grab the result in a vector
			Vector bhResult = this.benjaminiHochberg(p_values);
			
			//first array contains adjusted p-values
			float [] adjPValues = (float [])(bhResult.get(0));				
			
			//ranking order on adjusted p-value... based on valid list
			int [] bhOrderedIndices = (int [])(bhResult.get(1));
			
			//build an ordered indices that skips over invalid p indices
			int [] orderedIndices = new int[bhOrderedIndices.length];
			
			//populate the result matrix with the adjusted values
			//NaN adjusted p values have already been set
			for(int i = 0; i < adjPValues.length; i++ ) {
				//add the result to result values, order by the valid indices
				orderedIndices[i] = ((Integer)(validInd.get(bhOrderedIndices[i]))).intValue();
				resultMatrix.set(orderedIndices[i], 5, (float)adjPValues[i]);
			}
			
			//if use the fdr graph
			if(algData.getParams().getBoolean("use-fdr-graph")) {
				
				//get the frame object
				Object [][] frameMatrix = algData.getObjectMatrix("main-frame");
				
				//construct the fdr dialog
				NonparFDRDialog fdrDialog = new NonparFDRDialog((JFrame)(frameMatrix[0][0]), adjPValues, orderedIndices);;
				if(fdrDialog.showModal() == JOptionPane.OK_OPTION) {
					//grab the list of significant genes
					sigGenesArray = fdrDialog.getSelectedIndices();
					//and the rest..
					nonSigGenesArray = fdrDialog.getNonSelectedIndices();
					
					//need to add NaN genes to nonSigGenesArray
					int [] nonSig = new int[nonSigGenesArray.length+invalidInd.size()];						
					
					//add all ofthe non-significant genes
					for(int i = 0; i < nonSigGenesArray.length; i++)
						nonSig[i] = nonSigGenesArray[i];
					
					//add NaN genes
					for(int i = 0; i < invalidInd.size(); i++)
						nonSig[nonSigGenesArray.length+i] = ((Integer)(invalidInd.get(i))).intValue();
					
					//dry clusters
					clusters[0] = sigGenesArray;
					clusters[1] = nonSig;	
				}
				
				algData.addParam("fdr", String.valueOf(fdrDialog.getFDRLimit()));
				
				//else using a fixed FDR
			} else {
				//just used the fixed fdr
				float fdrLimit = algData.getParams().getFloat("fdr");
				
				sigGenes = new Vector();
				nonSigGenes = new Vector();
				
				for(int i = 0; i < adjPValues.length; i++) {
					if(adjPValues[i] <= fdrLimit)
						sigGenes.add(new Integer(orderedIndices[i]));
					else //put non conforming and NaN's in NS
						nonSigGenes.add(new Integer(orderedIndices[i]));
				}
				
				//add the NaN's to nonsig
				for(int i = 0; i < invalidInd.size(); i++) {
					nonSigGenes.add(invalidInd.get(i));
				}
				
				sigGenesArray = new int [sigGenes.size()];
				nonSigGenesArray = new int [nonSigGenes.size()];
				
				//from vectors to arrays
				for(int i = 0; i < sigGenesArray.length; i++)
					sigGenesArray[i] = ((Integer)(sigGenes.get(i))).intValue();
				for(int i = 0; i < nonSigGenesArray.length; i++)
					nonSigGenesArray[i] = ((Integer)(nonSigGenes.get(i))).intValue();
				
				//add to clusters
				clusters[0] = sigGenesArray;
				clusters[1] = nonSigGenesArray;	
			}
			
			//lets capture the estimated fdr for the signficant genes
			//last sorted adjusted fdr based on clusters[0] size
			if(clusters[0].length > 0)
				algData.addParam("estimated-fdr", String.valueOf(adjPValues[clusters[0].length-1]));
			else
				algData.addParam("estimated-fdr", String.valueOf(Float.NaN));					
			
		} else {
			//just use alpha as criterion for significcance
			
			//get cluster partiions to get means and variances
			sigGenes = new Vector();
			nonSigGenes = new Vector();
			//grab the alpha value
			float alpha = algData.getParams().getFloat("alpha");
			int numResultCols = resultMatrix.getColumnDimension();
			
			//add results to the sig and nonsig vectors
			for(int i = 0; i < numRows; i++) {
				if(!Float.isNaN(resultMatrix.A[i][numResultCols-1]) && resultMatrix.A[i][numResultCols-1] <= alpha)
					sigGenes.add(new Integer(i));
				else
					nonSigGenes.add(new Integer(i));
			}			
			
			//arrayify
			sigGenesArray = new int [sigGenes.size()];
			nonSigGenesArray = new int [nonSigGenes.size()];				
			for(int i = 0; i < sigGenesArray.length; i++)
				sigGenesArray[i] = ((Integer)(sigGenes.get(i))).intValue();
			for(int i = 0; i < nonSigGenesArray.length; i++)
				nonSigGenesArray[i] = ((Integer)(nonSigGenes.get(i))).intValue();
			
			//add to clusters
			clusters[0] = sigGenesArray;
			clusters[1] = nonSigGenesArray;					
		}
		
		//add result float matrix and method array
		algData.addMatrix("result-matrix", resultMatrix);
		algData.addStringArray("method-array", method);
		
		//add clusters, means, and variances
		FloatMatrix clusterMeans = this.getMeans(m, clusters);
		FloatMatrix clusterVars = this.getVariances(m, clusterMeans, clusters);
		algData.addIntMatrix("clusters", clusters);
		algData.addMatrix("cluster-means", clusterMeans);
		algData.addMatrix("cluster-variances", clusterVars);
		
		
		//if we are to run HCL			
		//precondition... we have selected significant genes into cluster int [][]
		if(algData.getParams().getBoolean("hcl-execution")) {
			
			int linkageMethod = algData.getParams().getInt("method-linkage");
			int metric = algData.getParams().getInt("hcl-distance-function");
			boolean genes = algData.getParams().getBoolean("calculate-genes");
			boolean experiments = algData.getParams().getBoolean("calculate-samples");
			boolean absoluteDistance = algData.getParams().getBoolean("hcl-distance-absolute");
			
			this.fireValueChanged(new AlgorithmEvent(this, AlgorithmEvent.PROGRESS_VALUE, 100, "Constructing Hierarchical Trees"));									
			
			NodeValueList nodeValueList =  calculateHierarchicalTree(m, clusters[0], linkageMethod, metric, absoluteDistance, genes, experiments);
			
			Node node = new Node(clusters[0]);
			node.setValues(nodeValueList);
			
			NodeList nodeList = new NodeList();
			nodeList.addNode(node);
			
			Cluster hclCluster = new Cluster();
			hclCluster.setNodeList(nodeList);
			
			algData.addCluster("hcl-clusters", hclCluster);
		}
		
		this.fireValueChanged(new AlgorithmEvent(this, AlgorithmEvent.PROGRESS_VALUE, 110, "Processing Results"));							
	}
	
	
	
	/**
	 * Executes the Kruskal-Wallis test and puts the result into the AlgorithmData
	 * @param m the FloatMatrix of data
	 * @throws AlgorithmException
	 */
	private void executeKruskalWallisTest(FloatMatrix m) throws AlgorithmException {
		
		
		boolean useAlpha = algData.getParams().getBoolean("use-alpha-criterion", true);
		
		//group assignments
		int [] groups = algData.getIntArray("group-assignments");
		
		//subtract 1 for 'Excluded' group for numGroups
		int numGroups = algData.getStringArray("group-names").length-1;
		Vector sigGenes, nonSigGenes; //for accumulation of gene sets
		
		//for each row record statistic, df, and p-value	
		FloatMatrix resultMatrix = new FloatMatrix(m.getRowDimension(), useAlpha ? 3 : 4);
		
		//arrays for cluster indices
		int [][] clusters = new int[2][];
		
		//indicate the methods used, important if there are ties
		String [] method = new String[m.getRowDimension()];
				
		//loop through data matrix and pull chunks
		int numRows = m.getRowDimension();
	
		this.fireValueChanged(new AlgorithmEvent(this, AlgorithmEvent.PROGRESS_VALUE, 0, "Executing Kruskal-Wallis Test"));
		
		//p value array
		float [] p_values = new float[m.getRowDimension()]; 
		
		//array to hold an organize the data
		NonparOneWayData data;
		
		//array of test results
		float [] result;

		//iterate to generate stats
		for(int iter = 0; iter < numRows; iter++ ) {
			
			//build the data object
			data = new NonparOneWayData(m.A[iter], groups, numGroups);
			
			//check to see that all groups are represented
			if(!data.getAreGroupsNull()) {
				
				//get test results, pass in all non-NaN values and assoc. grouping
				result = kruskalImpl(data.getValues(), data.getGroups(), numGroups);
				
				//check for ties and report the method
				if(result[3] != 1.0f)
					method[iter] = "(H) Kruskal-Wallis";
				else
					method[iter] = "(H') Kruskal-Wallis w/ Ties Corr.";
				
				//grab the df, H, p-value
				resultMatrix.A[iter][0] = result[0];
				resultMatrix.A[iter][1] = result[1];
				resultMatrix.A[iter][2] = result[2];
				p_values[iter] = result[2];
			} else {
				//missing data for one or more group
				resultMatrix.A[iter][0] = Float.NaN;
				resultMatrix.A[iter][1] = Float.NaN;
				resultMatrix.A[iter][2] = Float.NaN;										
				p_values[iter] = Float.NaN;
				if(!useAlpha)
					resultMatrix.A[iter][3] = Float.NaN;
				method[iter] = "None, empty group(s)";
			}
			
			this.fireValueChanged(new AlgorithmEvent(this, AlgorithmEvent.PROGRESS_VALUE, (int)(((float)iter/(float)numRows)*100f), "Executing Kruskal-Wallis Test (finished batch "+iter+")"));				
		}	
		
		//holds indices
		int [] sigGenesArray;
		int [] nonSigGenesArray;
		
		//if using FDR			
		if(!useAlpha) {
			
			//need to cull out NaN p_values... from flat genes or genes with 0 group sizes
			Vector validP = new Vector();
			Vector validInd = new Vector();
			Vector invalidInd = new Vector();
			
			//partition on NaN values
			for(int i = 0; i < p_values.length; i++) {
				if(!Float.isNaN(p_values[i])) {
					validP.add(new Float(p_values[i]));
					validInd.add(new Integer(i));						
				} else {
					invalidInd.add(new Integer(i));
				}					
			}
			
			//grab p-values (!NaN) to pass on to bh
			int validCnt = validP.size();
			p_values = new float[validCnt];
			
			for(int i = 0; i < validCnt; i++) {
				p_values[i] = ((Float)(validP.get(i))).floatValue();
			}
			
			//perform bh on non-NaN p-value set, returns a vector of result arrays
			Vector bhResult = this.benjaminiHochberg(p_values);			
			
			//adjusted P values
			float [] adjPValues = (float [])(bhResult.get(0));				
			
			//ranking order on adjusted p-value... based on valid list
			int [] bhOrderedIndices = (int [])(bhResult.get(1));
			
			//build an ordering array that skips NaN's
			int [] orderedIndices = new int[bhOrderedIndices.length];
			
			for(int i = 0; i < adjPValues.length; i++ ) {
				//add the result to result values, order by the valid indices
				orderedIndices[i] = ((Integer)(validInd.get(bhOrderedIndices[i]))).intValue();
				resultMatrix.set(orderedIndices[i], 3, (float)adjPValues[i]);
			}
			
			//if use the fdr graph
			if(algData.getParams().getBoolean("use-fdr-graph")) {
				
				//get the frame
				Object [][] frameMatrix = algData.getObjectMatrix("main-frame");
				
				NonparFDRDialog fdrDialog = new NonparFDRDialog((JFrame)(frameMatrix[0][0]), adjPValues, orderedIndices);;
				if(fdrDialog.showModal() == JOptionPane.OK_OPTION) {
					sigGenesArray = fdrDialog.getSelectedIndices();
					nonSigGenesArray = fdrDialog.getNonSelectedIndices();
					
					//need to add NaN genes to nonSigGenesArray
					int [] nonSig = new int[nonSigGenesArray.length+invalidInd.size()];
					
					for(int i = 0; i < nonSigGenesArray.length; i++)
						nonSig[i] = nonSigGenesArray[i];
					
					//add NaN genes
					for(int i = 0; i < invalidInd.size(); i++)
						nonSig[nonSigGenesArray.length+i] = ((Integer)(invalidInd.get(i))).intValue();
					
					clusters[0] = sigGenesArray;
					clusters[1] = nonSig;	
				}
				
				algData.addParam("fdr", String.valueOf(fdrDialog.getFDRLimit()));
				
				//else using a fixed FDR
			} else {
				//just used the fixed fdr
				float fdrLimit = algData.getParams().getFloat("fdr");
				
				sigGenes = new Vector();
				nonSigGenes = new Vector();
				
				for(int i = 0; i < adjPValues.length; i++) {
					if(adjPValues[i] <= fdrLimit)
						sigGenes.add(new Integer(orderedIndices[i]));
					else //put non conforming and NaN's in NS
						nonSigGenes.add(new Integer(orderedIndices[i]));
				}
				
				for(int i = 0; i < invalidInd.size(); i++) {
					nonSigGenes.add(invalidInd.get(i));
				}
				
				sigGenesArray = new int [sigGenes.size()];
				nonSigGenesArray = new int [nonSigGenes.size()];
				
				for(int i = 0; i < sigGenesArray.length; i++)
					sigGenesArray[i] = ((Integer)(sigGenes.get(i))).intValue();
				for(int i = 0; i < nonSigGenesArray.length; i++)
					nonSigGenesArray[i] = ((Integer)(nonSigGenes.get(i))).intValue();
				
				clusters[0] = sigGenesArray;
				clusters[1] = nonSigGenesArray;	
			}
			
			//lets capture the estimated fdr for the signficant genes
			//last sorted adjusted fdr based on clusters[0] size
			if(clusters[0].length > 0)
				algData.addParam("estimated-fdr", String.valueOf(adjPValues[clusters[0].length-1]));
			else
				algData.addParam("estimated-fdr", String.valueOf(Float.NaN));					
		} else {
			//just use alpha as criterion for significcance
			
			//get cluster partiions to get means and variances
			sigGenes = new Vector();
			nonSigGenes = new Vector();			
			float alpha = algData.getParams().getFloat("alpha");
			int numResultCols = resultMatrix.getColumnDimension();
			
			for(int i = 0; i < numRows; i++) {
				if(!Float.isNaN(resultMatrix.A[i][numResultCols-1]) && resultMatrix.A[i][numResultCols-1] <= alpha)
					sigGenes.add(new Integer(i));
				else
					nonSigGenes.add(new Integer(i));
			}			
			
			sigGenesArray = new int [sigGenes.size()];
			nonSigGenesArray = new int [nonSigGenes.size()];
			
			for(int i = 0; i < sigGenesArray.length; i++)
				sigGenesArray[i] = ((Integer)(sigGenes.get(i))).intValue();
			for(int i = 0; i < nonSigGenesArray.length; i++)
				nonSigGenesArray[i] = ((Integer)(nonSigGenes.get(i))).intValue();
			
			clusters[0] = sigGenesArray;
			clusters[1] = nonSigGenesArray;	
			
		}
		
		algData.addMatrix("result-matrix", resultMatrix);
		algData.addStringArray("method-array", method);
		
		FloatMatrix clusterMeans = this.getMeans(m, clusters);
		FloatMatrix clusterVars = this.getVariances(m, clusterMeans, clusters);
		
		algData.addIntMatrix("clusters", clusters);
		algData.addMatrix("cluster-means", clusterMeans);
		algData.addMatrix("cluster-variances", clusterVars);
		
		
		//if we are to run HCL			
		//precondition... we have selected significant genes into cluster int [][]
		if(algData.getParams().getBoolean("hcl-execution")) {
			
			int linkageMethod = algData.getParams().getInt("method-linkage");
			int metric = algData.getParams().getInt("hcl-distance-function");
			boolean genes = algData.getParams().getBoolean("calculate-genes");
			boolean experiments = algData.getParams().getBoolean("calculate-samples");
			boolean absoluteDistance = algData.getParams().getBoolean("hcl-distance-absolute");
			
			this.fireValueChanged(new AlgorithmEvent(this, AlgorithmEvent.PROGRESS_VALUE, 100, "Constructing Hierarchical Trees"));									
			
			NodeValueList nodeValueList =  calculateHierarchicalTree(m, clusters[0], linkageMethod, metric, absoluteDistance, genes, experiments);
			
			Node node = new Node(clusters[0]);
			node.setValues(nodeValueList);
			
			NodeList nodeList = new NodeList();
			nodeList.addNode(node);
			
			Cluster hclCluster = new Cluster();
			hclCluster.setNodeList(nodeList);
			
			algData.addCluster("hcl-clusters", hclCluster);
		}
		
		this.fireValueChanged(new AlgorithmEvent(this, AlgorithmEvent.PROGRESS_VALUE, 110, "Processing Results"));												
	}
	
	
	
	/**
	 * Performs the MackSkillings test in generalized form where the design is complete
	 * but need not be balanced.
	 * 
	 * Precondition: AlgortihmData is populated with needed params
	 * Postcondition: AlgorithmData receives a result matrix, methods, and clusters
	 * 
	 * @param matrix the data matrix
	 * @throws AlgorithmException
	 */
		
	private void executeMackSkillings(FloatMatrix matrix) throws AlgorithmException {

		int [] rawFactorAGroupings = this.algData.getIntArray("factor-A-group-assignments");
		int [] rawFactorBGroupings = this.algData.getIntArray("factor-B-group-assignments");
	
		int numFactorALevels = this.algData.getStringArray("factor-A-level-names").length;
		int numFactorBLevels = this.algData.getStringArray("factor-B-level-names").length;

		NonparTwoWayData currentData;
		int numRows = matrix.getRowDimension();
		float pValue, msStat;
		float [] sVals;

		ChiSqrDistribution factorAChiDist = new ChiSqrDistribution(numFactorALevels-1);
		ChiSqrDistribution factorBChiDist = new ChiSqrDistribution(numFactorBLevels-1);

		//result data
		float [][] resultData = new float[numRows][7];
		String [] designDesc = new String[numRows];
		String [] method = new String[numRows];
		
		this.fireValueChanged(new AlgorithmEvent(this, AlgorithmEvent.PROGRESS_VALUE, 0, "Executing Kruskal-Wallis Test"));
		
		for(int row = 0; row < numRows; row++) {
						
			//accumulate and organize data
			currentData = new NonparTwoWayData(matrix.A[row], numFactorALevels, numFactorBLevels, rawFactorAGroupings, rawFactorBGroupings);
		
			
			resultData[row][0] = currentData.nVals;

			if(currentData.isComplete && !currentData.isFlat) {
				if(currentData.isBalanced) {
					//balanced, apply standard Mack-Skillings
				
					designDesc[row] = DESIGN_DESC_BALANCED;
					method[row] = METHOD_MACK_SKILLINGS;
										
					//factor significance (by row in design matrix)
					sVals = this.computeSValues(currentData.values, currentData.repTotals[0][0], numFactorALevels, numFactorBLevels, currentData.factorAGrouping, currentData.factorBGrouping);
				
					msStat = this.getMackSkillingsStatistic(sVals, currentData.repTotals[0][0], numFactorALevels, numFactorBLevels);
			
					pValue = (float)(1d-factorAChiDist.cumulative((double)msStat));
					resultData[row][1] = numFactorALevels;
					resultData[row][2] = msStat;
					resultData[row][3] = pValue;
					
					//condition significance (by column in design matrix)
					//swap assignment vectors, num. conds and factors, use condChiDist with df = numConds-1
					currentData.transposeRepTots();
					sVals = this.computeSValues(currentData.values, currentData.repTotals[0][0], numFactorBLevels, numFactorALevels, currentData.factorBGrouping, currentData.factorAGrouping);
					msStat = this.getMackSkillingsStatistic(sVals, currentData.repTotals[0][0], numFactorBLevels, numFactorALevels);
					pValue = (float)(1d-factorBChiDist.cumulative((double)msStat));					
					resultData[row][4] = numFactorBLevels;
					resultData[row][5] = msStat;
					resultData[row][6] = pValue;
				
				} else {
					//unbalanced, apply generalized Mack-Skillings
					designDesc[row] = DESIGN_DESC_UNBALANCED;
					method[row] = METHOD_GENERALIZED_MACK_SKILLINGS;
					
					//factor significance (by row in design matrix)
					msStat = (float)computeGeneralizedMS(currentData.values, currentData.repTotals, 
							numFactorALevels, numFactorBLevels, currentData.factorAGrouping, currentData.factorBGrouping);
					pValue = (float)(1d-factorAChiDist.cumulative((double)msStat));
					resultData[row][1] = numFactorALevels;
					resultData[row][2] = msStat;
					resultData[row][3] = pValue;

					//condition significance (by column in design matrix)
					//swap assignment vectors, num. conds and factors, use condChiDist with df = numConds-1	
					//invert repTotals
					currentData.transposeRepTots();
					msStat = (float)computeGeneralizedMS(currentData.values, currentData.repTotals, 
							numFactorBLevels, numFactorALevels, currentData.factorBGrouping, currentData.factorAGrouping);
					pValue = (float)(1d-factorBChiDist.cumulative((double)msStat));
					resultData[row][4] = numFactorBLevels;
					resultData[row][5] = msStat;
					resultData[row][6] = pValue;
				}
			} else {
				//incomplete, at least one cell with no replications
				designDesc[row] = this.DESIGN_DESC_INCOMPLETE;
				method[row] = this.METHOD_NONE_INCOMPLETE;
				resultData[row][1] = Float.NaN;
				resultData[row][2] = Float.NaN;
				resultData[row][3] = Float.NaN;
				resultData[row][4] = Float.NaN;
				resultData[row][5] = Float.NaN;				
				resultData[row][6] = Float.NaN;				
			}
			
			this.fireValueChanged(new AlgorithmEvent(this, AlgorithmEvent.PROGRESS_VALUE, (int)(((float)row/(float)numRows)*100f), "Executing Mack-Skillings Test"));
			
		}
		
		algData.addMatrix("result-matrix", new FloatMatrix(resultData));
		algData.addStringArray("design-array", designDesc);
		algData.addStringArray("method-array", method);	
		
		//pull significant indices to build cluster structure
		//clusters will have five arrays, factor sign, factor non-sign, 
		//cond. signif., cond. non-signif., incomplete skipped
		float factorAAlpha = algData.getParams().getFloat("alpha");
		float factorBAlpha = algData.getParams().getFloat("alpha");
	
		int [][] clusters = getTwoWaySignificantGenes(resultData, factorAAlpha, factorBAlpha, 3, 6);
		algData.addIntMatrix("clusters", clusters);	
		
		FloatMatrix means = this.getMeans(matrix, clusters);
		FloatMatrix vars = this.getVariances(matrix, means, clusters);
		
		algData.addMatrix("cluster-means", means);
		algData.addMatrix("cluster-variances", vars);
		
		//precondition... we have selected significant genes into cluster int [][]
		if(algData.getParams().getBoolean("hcl-execution")) {
			
			int linkageMethod = algData.getParams().getInt("method-linkage");
			int metric = algData.getParams().getInt("hcl-distance-function");
			boolean genes = algData.getParams().getBoolean("calculate-genes");
			boolean experiments = algData.getParams().getBoolean("calculate-samples");
			boolean absoluteDistance = algData.getParams().getBoolean("hcl-distance-absolute");
			
			this.fireValueChanged(new AlgorithmEvent(this, AlgorithmEvent.PROGRESS_VALUE, 100, "Constructing Hierarchical Trees"));									

			NodeList nodeList = new NodeList();

			//factor significant
			NodeValueList nodeValueList =  calculateHierarchicalTree(matrix, clusters[0], linkageMethod, metric, absoluteDistance, genes, experiments);
			Node node = new Node(clusters[0]);
			node.setValues(nodeValueList);			
			nodeList.addNode(node);

			//cond sign
			nodeValueList =  calculateHierarchicalTree(matrix, clusters[2], linkageMethod, metric, absoluteDistance, genes, experiments);
			node = new Node(clusters[2]);
			node.setValues(nodeValueList);			
			nodeList.addNode(node);
			
			Cluster hclCluster = new Cluster();
			hclCluster.setNodeList(nodeList);
		
			algData.addCluster("hcl-clusters", hclCluster);
		}
	}
	
	
	private void executeFisherExactTest(FloatMatrix matrix) throws AlgorithmException {
		
		//segregates data into bins
		float binCutoff = algData.getParams().getFloat("fisher-exact-bin-cutoff");
		//sets group assignments (0, 1 or -1 for exclude)
		int [] groups = algData.getIntArray("group-assignments");
		int numRows = matrix.getRowDimension();		
		boolean useAlpha = algData.getParams().getBoolean("use-alpha-criterion", true);
		
		float [] pValuesUpper = new float[numRows];
		float [] pValuesLower = new float[numRows];
		float [] p_values = new float[numRows];
		float [] currPs;
		double exactPforMatrix;
		
		int [] currGroups;
		float [] currData;
		
		//configuration details
		boolean swapGroupLoc = algData.getParams().getBoolean("swap-groups");
		boolean swapBinLoc = algData.getParams().getBoolean("swap-bins");
		int upperBinIndex = algData.getParams().getInt("upper-bin-index");
		
		FloatMatrix resultMatrix = new FloatMatrix(numRows, useAlpha ? 7 : 8);		
		Vector sigGenes, nonSigGenes; //for accumulation of gene sets
		Vector rightSig, leftSig;
		
		int [][] clusters = new int[4][]; //all sig, left sig, right sig, non-sig
		
		int [] groupNs;
		
		boolean [] invalidP = new boolean[numRows];
		
		NonparHypergeometricProbability hyperG = new NonparHypergeometricProbability();
		
		boolean [] binPartition;
		boolean [] groupPartition;
		
		//contingency matrix counts
		int m11, m12, m21, m22;
		
		for(int i = 0; i < numRows; i++) {
			NonparOneWayData data = new NonparOneWayData(matrix.A[i], groups, 2);

			if(!data.areGroupsNull) {
				
				m11 = m12 = m21 = m22 = 0;
				
				currGroups = data.getGroups();
				currData = data.getValues();
				groupNs = data.getGroupNs();
				
				
				//partition bins
				binPartition = new boolean[currData.length];
				
				for(int j = 0; j < binPartition.length; j++) {
					if(currData[j] >= binCutoff) {
						if(upperBinIndex == 0)
							binPartition[j] = true;						
					} else {
						if(upperBinIndex == 1)
							binPartition[j] = true;												
					}
				}
				
				
				/*
				 * The accumulation of matrix entries depends on the orientation requested
				 * in terms of colunm and row associations with bin and grouping.
				 * 
				 * Upper left is m11, upper right is m12, LL is m21, LR is m22
				 * (row,col).  
				 * 
				 */
				
				if(!swapGroupLoc) {  //if we don't swap groups
					
					for(int j = 0; j < currData.length; j++) {
						if(currGroups[j] == 0) {
							if(!swapBinLoc) {
								if(binPartition[j])
									m11++;
								else
									m21++;		
							} else {
								if(binPartition[j])
									m21++;
								else
									m11++;	
							}
						} else {
							if(!swapBinLoc) {
								if(binPartition[j])
									m12++;
								else
									m22++;
							} else {
								if(binPartition[j])
									m22++;
								else
									m12++;								
							}
						}
					}
				} else {  //if we do swap groups
					
					for(int j = 0; j < currData.length; j++) {
						if(currGroups[j] == 1) { //change group index orientation
							if(!swapBinLoc) {
								if(binPartition[j])
									m11++;
								else
									m21++;		
							} else {
								if(binPartition[j])
									m21++;
								else
									m11++;	
							}					
						} else {
							if(!swapBinLoc) {
								if(binPartition[j])
									m12++;
								else
									m22++;
							} else {
								if(binPartition[j])
									m22++;
								else
									m12++;								
							}																
						}
					}
				}

/*
 * Non-swapped accumulation
				for(int j = 0; j < currData.length; j++) {
					if(groups[j] == 0) {
						if(currData[j] > binCutoff)
							m11++;
						else
							m21++;						
					} else {
						if(currData[j] > binCutoff)
							m12++;
						else
							m22++;																
					}
				}
	*/			
				resultMatrix.set(i, 0, m11);
				resultMatrix.set(i, 1, m12);
				resultMatrix.set(i, 2, m21);
				resultMatrix.set(i, 3, m22);
				
				//System.out.println(m11+" "+m12+" "+ m21+" "+ m22);
				//pValuesUpper[i] = (float)hyperG.upperSumHGP(m11,m21,m12,m22);
				pValuesUpper[i] = (float)hyperG.upperSumHGP(m11+m21+m12+m22, m11+m21, m11+m12, m11);
				
				//System.out.println("upper = " +pValuesUpper[i]);
				//pValuesLower[i] = (float)hyperG.lowerSumHGP(m11,m21,m12,m22);
				pValuesLower[i] = (float)hyperG.lowerSumHGP(m11+m21+m12+m22, m11+m21, m11+m12, m11);
				//System.out.println("lower = " +pValuesLower[i]);
				
				//now compute two tailed

				//get the exact p for the matrix
				exactPforMatrix = hyperG.pExactForMatrix(m11, m12, m21, m22);

				if(pValuesUpper[i] < pValuesLower[i]) {
					p_values[i] = pValuesUpper[i] + (float)hyperG.conditionalLowerSumHGP(m11+m21+m12+m22, m11+m21, m11+m12, m11, exactPforMatrix);
					//System.out.println("two tailed = "+p_values[i]);
				} else {
					p_values[i] = pValuesLower[i] + (float)hyperG.conditionalUpperSumHGP(m11+m21+m12+m22, m11+m21, m11+m12, m11, exactPforMatrix);					
					//System.out.println("two tailed = "+p_values[i]);
				}

				resultMatrix.set(i, 4, pValuesLower[i]);
				resultMatrix.set(i, 5, pValuesUpper[i]);
				resultMatrix.set(i, 6, p_values[i]);

				
			} else {
				invalidP[i] = true;
				pValuesUpper[i] = Float.NaN;
				pValuesLower[i] = Float.NaN;
				p_values[i] = Float.NaN;
				
				resultMatrix.set(i, 4, Float.NaN);
				resultMatrix.set(i, 5, Float.NaN);
				resultMatrix.set(i, 6, Float.NaN);
			}						
		}
		
		
		
		
		//holds indices
		int [] sigGenesArray;
		int [] nonSigGenesArray;
		
		//if using FDR			
		if(!useAlpha) {
			
			//need to cull out NaN p_values... from flat genes or genes with 0 group sizes
			Vector validP = new Vector();
			Vector validInd = new Vector();
			Vector invalidInd = new Vector();
			
			//partition on NaN values
			for(int i = 0; i < p_values.length; i++) {
				if(!Float.isNaN(p_values[i])) {
					validP.add(new Float(p_values[i]));
					validInd.add(new Integer(i));						
				} else {
					invalidInd.add(new Integer(i));
				}					
			}
			
			//grab p-values (!NaN) to pass on to bh
			int validCnt = validP.size();
			p_values = new float[validCnt];
			
			for(int i = 0; i < validCnt; i++) {
				p_values[i] = ((Float)(validP.get(i))).floatValue();
			}
			
			//perform bh on non-NaN p-value set, returns a vector of result arrays
			Vector bhResult = this.benjaminiHochberg(p_values);			
			
			//adjusted P values
			float [] adjPValues = (float [])(bhResult.get(0));				
			
			//ranking order on adjusted p-value... based on valid list
			int [] bhOrderedIndices = (int [])(bhResult.get(1));
			
			//build an ordering array that skips NaN's
			int [] orderedIndices = new int[bhOrderedIndices.length];
			
			for(int i = 0; i < adjPValues.length; i++ ) {
				//add the result to result values, order by the valid indices
				orderedIndices[i] = ((Integer)(validInd.get(bhOrderedIndices[i]))).intValue();
				resultMatrix.set(orderedIndices[i], 7, (float)adjPValues[i]);
			}
			
			//if use the fdr graph
			if(algData.getParams().getBoolean("use-fdr-graph")) {
				
				//get the frame
				Object [][] frameMatrix = algData.getObjectMatrix("main-frame");
				
				NonparFDRDialog fdrDialog = new NonparFDRDialog((JFrame)(frameMatrix[0][0]), adjPValues, orderedIndices);;
				if(fdrDialog.showModal() == JOptionPane.OK_OPTION) {
					sigGenesArray = fdrDialog.getSelectedIndices();
					nonSigGenesArray = fdrDialog.getNonSelectedIndices();
					
					//need to add NaN genes to nonSigGenesArray
					int [] nonSig = new int[nonSigGenesArray.length+invalidInd.size()];
					
					for(int i = 0; i < nonSigGenesArray.length; i++)
						nonSig[i] = nonSigGenesArray[i];
					
					//add NaN genes
					for(int i = 0; i < invalidInd.size(); i++)
						nonSig[nonSigGenesArray.length+i] = ((Integer)(invalidInd.get(i))).intValue();
					
					clusters[0] = sigGenesArray;
					//add non-sig to last array
					clusters[3] = nonSig;										
				}
				
				algData.addParam("fdr", String.valueOf(fdrDialog.getFDRLimit()));
				
				//else using a fixed FDR
			} else {
				//just used the fixed fdr
				float fdrLimit = algData.getParams().getFloat("fdr");
				
				sigGenes = new Vector();
				nonSigGenes = new Vector();
				
				for(int i = 0; i < adjPValues.length; i++) {
					if(adjPValues[i] <= fdrLimit)
						sigGenes.add(new Integer(orderedIndices[i]));
					else //put non conforming and NaN's in NS
						nonSigGenes.add(new Integer(orderedIndices[i]));
				}
				
				for(int i = 0; i < invalidInd.size(); i++) {
					nonSigGenes.add(invalidInd.get(i));
				}
				
				sigGenesArray = new int [sigGenes.size()];
				nonSigGenesArray = new int [nonSigGenes.size()];
				
				for(int i = 0; i < sigGenesArray.length; i++)
					sigGenesArray[i] = ((Integer)(sigGenes.get(i))).intValue();
				for(int i = 0; i < nonSigGenesArray.length; i++)
					nonSigGenesArray[i] = ((Integer)(nonSigGenes.get(i))).intValue();
				
				clusters[0] = sigGenesArray;
				clusters[3] = nonSigGenesArray;	
			}
			
			//lets capture the estimated fdr for the signficant genes
			//last sorted adjusted fdr based on clusters[0] size
			if(clusters[0].length > 0)
				algData.addParam("estimated-fdr", String.valueOf(adjPValues[clusters[0].length-1]));
			else
				algData.addParam("estimated-fdr", String.valueOf(Float.NaN));					
		} else {
			//just use alpha as criterion for significcance
			
			//get cluster partiions to get means and variances
			sigGenes = new Vector();
			nonSigGenes = new Vector();			
			float alpha = algData.getParams().getFloat("alpha");
			int numResultCols = resultMatrix.getColumnDimension();
			
			for(int i = 0; i < numRows; i++) {
				if(!Float.isNaN(resultMatrix.A[i][numResultCols-1]) && resultMatrix.A[i][numResultCols-1] <= alpha)
					sigGenes.add(new Integer(i));
				else
					nonSigGenes.add(new Integer(i));
			}			
			
			sigGenesArray = new int [sigGenes.size()];
			nonSigGenesArray = new int [nonSigGenes.size()];
			
			for(int i = 0; i < sigGenesArray.length; i++)
				sigGenesArray[i] = ((Integer)(sigGenes.get(i))).intValue();
			for(int i = 0; i < nonSigGenesArray.length; i++)
				nonSigGenesArray[i] = ((Integer)(nonSigGenes.get(i))).intValue();
			
			clusters[0] = sigGenesArray;
			clusters[3] = nonSigGenesArray;	
			
		}
	
		//grab left and right sig clusters
		int [][] sigClusters = getFisherExactTailClusters(clusters[0], pValuesLower, pValuesUpper);
		clusters[1] = sigClusters[0];
		clusters[2] = sigClusters[1];  
		
		algData.addMatrix("result-matrix", resultMatrix);
			
		FloatMatrix clusterMeans = this.getMeans(matrix, clusters);
		FloatMatrix clusterVars = this.getVariances(matrix, clusterMeans, clusters);
		
		algData.addIntMatrix("clusters", clusters);
		algData.addMatrix("cluster-means", clusterMeans);
		algData.addMatrix("cluster-variances", clusterVars);
		
		
		//if we are to run HCL			
		//precondition... we have selected significant genes into cluster int [][]
		if(algData.getParams().getBoolean("hcl-execution")) {
			
			int linkageMethod = algData.getParams().getInt("method-linkage");
			int metric = algData.getParams().getInt("hcl-distance-function");
			boolean genes = algData.getParams().getBoolean("calculate-genes");
			boolean experiments = algData.getParams().getBoolean("calculate-samples");
			boolean absoluteDistance = algData.getParams().getBoolean("hcl-distance-absolute");
			
			this.fireValueChanged(new AlgorithmEvent(this, AlgorithmEvent.PROGRESS_VALUE, 100, "Constructing Hierarchical Trees"));									
			
			NodeValueList nodeValueList =  calculateHierarchicalTree(matrix, clusters[0], linkageMethod, metric, absoluteDistance, genes, experiments);
			
			Node node = new Node(clusters[0]);
			node.setValues(nodeValueList);
			
			NodeList nodeList = new NodeList();
			nodeList.addNode(node);
			
			Cluster hclCluster = new Cluster();
			hclCluster.setNodeList(nodeList);
			
			algData.addCluster("hcl-clusters", hclCluster);
		}
		
		this.fireValueChanged(new AlgorithmEvent(this, AlgorithmEvent.PROGRESS_VALUE, 110, "Processing Results"));												

	}
	
	
	public int [][] getFisherExactTailClusters(int [] sigGenes, float [] leftTailP, float [] rightTailP) {
		
		int [][] leftAndRightClusters = new int[2][];

		if(sigGenes.length < 1) {
			leftAndRightClusters [0] = new int[0];
			leftAndRightClusters [1] = new int[0];			
			return leftAndRightClusters;
		}

		Vector leftSig = new Vector();
		Vector rightSig = new Vector();
		
		for(int i = 0; i < sigGenes.length; i++) {
			if(leftTailP[sigGenes[i]] < rightTailP[sigGenes[i]]) {
				leftSig.add(new Integer(sigGenes[i]));
			} else {
				rightSig.add(new Integer(sigGenes[i]));				
			}
		}
		
		leftAndRightClusters[0] = new int[leftSig.size()];
		leftAndRightClusters[1] = new int[rightSig.size()];
		
		for(int i = 0; i < leftAndRightClusters[0].length; i++)
			leftAndRightClusters[0][i] = ((Integer)(leftSig.get(i))).intValue();

		for(int i = 0; i < leftAndRightClusters[1].length; i++)
			leftAndRightClusters[1][i] = ((Integer)(rightSig.get(i))).intValue();

		return leftAndRightClusters;
	}
	
	
	/**
	 * Returns 5 clusters of genes, factorA sig., factorB sig. non-sig for each factor and incomplete genes
	 * that couldn't be tested
	 * 
	 * @param data The resutling data matrix
	 * @param factorAAlpha alpha for factor A
	 * @param factorBAlpha alpha for factor B
	 * @param factorAPCol column index of factorA p-value
	 * @param factorBPCol column index of factorA p-value
	 * @return 5 int arrays for each relevant cluster (above)
	 */
	private int [][] getTwoWaySignificantGenes(float [][] data, float factorAAlpha, float factorBAlpha, int factorAPCol, int factorBPCol) {

		int [][] clusters = new int[5][];
		Vector factorASig = new Vector();
		Vector factorANonSig = new Vector();
		Vector factorBSig = new Vector();
		Vector factorBNonSig = new Vector();
		Vector incomp = new Vector();
		Integer index;
		
		for(int i = 0; i < data.length; i++) {
			index = new Integer(i);
			
			//check for incomplete design (by checking for NaN pvalue)
			if(data[i][factorAPCol] != Float.NaN) {

				//check factor sign.
				if(data[i][factorAPCol] <= factorAAlpha) {
					factorASig.add(index);
				} else {
					factorANonSig.add(index);				
				}
				
				//check cond. sign.
				if(data[i][factorBPCol] <= factorBAlpha) {
					factorBSig.add(index);
				} else {
					factorBNonSig.add(index);				
				}				
			} else {
				incomp.add(index);
			}
		}
		
		//collect index arrays
		clusters[0] = toIntArray(factorASig); 
		clusters[1] = toIntArray(factorANonSig); 
		clusters[2] = toIntArray(factorBSig); 
		clusters[3] = toIntArray(factorBNonSig); 
		clusters[4] = toIntArray(incomp); 
		
		return clusters;		
	}
	
	/**
	 * From Integer Vector to int array
	 * @param intVector
	 * @return
	 */
	private int [] toIntArray(Vector intVector) {
		int [] arr = new int[intVector.size()];
		for(int i = 0; i < intVector.size(); i++)
			arr[i] = ((Integer)(intVector.get(i))).intValue();
		return arr;
	}
		
    /**
     *  Calculates means for the clusters
     */
    private FloatMatrix getMeans(FloatMatrix data, int [][] clusters){
        FloatMatrix means = new FloatMatrix(clusters.length, data.getColumnDimension());
        for(int i = 0; i < clusters.length; i++){
            means.A[i] = getMeans(data, clusters[i]);
        }
        return means;
    }
    
    /**
     *  Returns a set of means for an element
     */
    private float [] getMeans(FloatMatrix data, int [] indices){
        int nSamples = data.getColumnDimension();
        float [] means = new float[nSamples];
        float sum = 0;
        float n = 0;
        float value;
        for(int i = 0; i < nSamples; i++){
            n = 0;
            sum = 0;
            for(int j = 0; j < indices.length; j++){
                value = data.get(indices[j],i);
                if(!Float.isNaN(value)){
                    sum += value;
                    n++;
                }
            }
            if(n > 0)
                means[i] = sum/n;
            else
                means[i] = Float.NaN;
        }
        return means;
    }
    
    
	
    /** Returns a matrix of standard deviations grouped by cluster and element
     * @param data Expression data
     * @param means calculated means
     * @param clusters cluster indices
     * @return
     */
    private FloatMatrix getVariances(FloatMatrix data, FloatMatrix means, int [][] clusters){
        int nSamples = data.getColumnDimension();
        FloatMatrix variances = new FloatMatrix(clusters.length, nSamples);
        for(int i = 0; i < clusters.length; i++){
            variances.A[i] = getVariances(data, means, clusters[i], i);
        }
        return variances;
    }
    
    /** Calculates the standard deviation for a set of genes.  One SD for each experiment point
     * in the expression vectors.
     * @param data Expression data
     * @param means previously calculated means
     * @param indices gene indices for cluster members
     * @param clusterIndex the index for the cluster to work upon
     * @return
     */
    private float [] getVariances(FloatMatrix data, FloatMatrix means, int [] indices, int clusterIndex){
        int nSamples = data.getColumnDimension();
        float [] variances = new float[nSamples];
        float sse = 0;
        float mean;
        float value;
        int n = 0;
        for(int i = 0; i < nSamples; i++){
            mean = means.get(clusterIndex, i);
            n = 0;
            sse = 0;
            for(int j = 0; j < indices.length; j++){
                value = data.get(indices[j], i);
                if(!Float.isNaN(value)){
                    sse += (float)Math.pow((value - mean),2);
                    n++;
                }
            }
            if(n > 1)
                variances[i] = (float)Math.sqrt(sse/(n-1));
            else
                variances[i] = 0.0f;
        }
        return variances;
    }
    
	/**
	 * Constructs HCL tree
	 * @param m data matrix
	 * @param features cluster, set of indices to run
	 * @param method linkage method
	 * @param metric distance metric
	 * @param absoluteDistance boolean for abs. distance
	 * @param genes	boolean to cluster genes vs. experiments
	 * @param experiments boolean to cluster experiments
	 * @return NodeValueList of cluster data
	 * @throws AlgorithmException
	 */
    private NodeValueList calculateHierarchicalTree(FloatMatrix m, int[] features, int method, int metric, boolean absoluteDistance, boolean genes, boolean experiments) throws AlgorithmException {
    	NodeValueList nodeList = new NodeValueList();
        AlgorithmData data = new AlgorithmData();
        FloatMatrix experiment;

        experiment = getSubExperiment(m, features);

        data.addMatrix("experiment", experiment);
        data.addParam("hcl-distance-function", String.valueOf(metric));
        data.addParam("hcl-distance-absolute", String.valueOf(absoluteDistance));
        data.addParam("method-linkage", String.valueOf(method));
        HCL hcl = new HCL();
        AlgorithmData result;
        
        if (genes) {
            data.addParam("calculate-genes", String.valueOf(true));
            result = hcl.execute(data);
            validate(result);
            addNodeValues(nodeList, result);
        }
        if (experiments) {
            data.addParam("calculate-genes", String.valueOf(false));
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
    
    private FloatMatrix getSubExperiment(FloatMatrix experiment, int[] features) {
        FloatMatrix subExperiment = new FloatMatrix(features.length, experiment.getColumnDimension());
        for (int i=0; i<features.length; i++) {
            subExperiment.A[i] = experiment.A[features[i]];
        }
        return subExperiment;
    }
    
    /**
     *  Creates a matrix with reduced columns (samples) as during experiment clustering
     */
    private FloatMatrix getSubExperimentReducedCols(FloatMatrix experiment, int[] features) {
        FloatMatrix copyMatrix = experiment.copy();
        FloatMatrix subExperiment = new FloatMatrix(features.length, copyMatrix.getColumnDimension());
        for (int i=0; i<features.length; i++) {
            subExperiment.A[i] = copyMatrix.A[features[i]];
        }
        subExperiment = subExperiment.transpose();
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
	

	//used for testing 
	//public void setAlgData(AlgorithmData data) {
	//	algData = data;
	//}
	
	
	/*
	 * Mack-Shilling implementation methods
	 * 
	 * Terminology
	 * Grouping information is based on 'factors' sometimes called blocks in the
	 * literature (Hollander-Wolfe text) and 'conditions' which are called treatments in the
	 * Holland description.
	 * 
	 * Here we refer to the two dimensions as factors A and B
	 * 
	 * The design can be considered in matrix or table from with factor A levels as rows
	 * and factor B levels as columns.  Formulae and background information is from 
	 * Nonparameteric Statistical Mathods, 2nd Ed., Hollander and Wolfe Eds.
	 * John Whiley & Sons, Inc., 1999,  pp 329-, section 7.9
	 * 
	 * The test assums or requires at least one replicate per grid cell (factorA-factorB combination).
	 *
	 */
	private double computeGeneralizedMS(float [] vals, int [][] numRepsPerCell, int numFactorALevels, int numFactorBLevels, int [] factors, int [] conds) {

		//*****calculate q(i), number of obs for each condition
		int [] q = new int[numFactorBLevels];
		int [] blockCountArray;
		for(int i = 0; i < numFactorALevels; i++) {
			blockCountArray = numRepsPerCell[i];
			for(int j = 0; j < blockCountArray.length; j++) {
				q[j] += blockCountArray[j];
			}
		}

		//*****calculate ranking arrays for each *'block'*...
		
		//accumulate the values for each condition
		float [] condVals;
		int valIndex = 0;
		QSort qsort;
		
		//ranking arrays for the n blocks (factors)
		float [] sortedVals;
		float [][] rankingArrays = new float[numFactorBLevels][];
		float [] rankArray;
		int [][] sortedRankIndices = new int[numFactorBLevels][];
		int [] sortedIndices;
		int [] valueIndices;
		int [] tempValueIndices;
		
		for(int cond = 0; cond < numFactorBLevels; cond++) {
			condVals = new float[q[cond]]; //use num vals per block to set size
			tempValueIndices = new int[q[cond]]; //grab vector indices entering ranking
			valueIndices = new int[q[cond]]; //grab vector indices entering ranking
			
			
			valIndex = 0;
			for(int i = 0; i < vals.length; i++) {
				if(conds[i] == (cond)) {
					condVals[valIndex] = vals[i];
					tempValueIndices[valIndex] = i;
					valIndex++;
				}
			}
		
			qsort = new QSort(condVals);
			sortedVals = qsort.getSorted();
			sortedIndices = qsort.getOrigIndx();
			
			//get the valueIndices that map to vector organized by rank sorting
			for(int i = 0; i < sortedIndices.length; i++) {
				valueIndices[i] = tempValueIndices[sortedIndices[i]];
			}
						
			//get the sorted indices that map to the v vector
			sortedRankIndices[cond] = valueIndices;
			
			//get the ranking array
			rankArray = this.getRankings(sortedVals);
			
			rankingArrays[cond] = rankArray;
		}
		
		//validate the ranking array
		rankArray = rankingArrays[0];
		sortedIndices = sortedRankIndices[0];	
		
		//*****compute V(j).... sum of cellwise weighted averaged ranks		
		float [] condRankSums = new float[numFactorALevels];
		
		for(int factor = 0; factor < numFactorALevels; factor++) {			
			for(int cond = 0; cond < numFactorBLevels; cond++) {				
				
				//traverse all rankings but just pull those assocciated with cond x
				rankArray = rankingArrays[cond];
				sortedIndices = sortedRankIndices[cond];
				
				for(int rankIndex = 0; rankIndex < rankArray.length; rankIndex++) {

					//verify that the rank score is for the current condition
					//get the membership for the current rank index mapped by the rank sorting
					if(factors[sortedIndices[rankIndex]] == (factor)) {
						condRankSums[factor] += rankArray[rankIndex]/((float)q[cond]);
					}				
				}								
			}
		}
		
		
		//*****Construct the vector V using weighted averaged ranks
		double [] V = new double[numFactorALevels-1];
		double sum;	
	
		for(int i = 0; i < V.length; i++) {
			sum = 0;
			for(int cond = 0; cond < numFactorBLevels; cond++) {
				sum += (((double)numRepsPerCell[i][cond])*((double)q[cond] + 1f))
				/(2d*q[cond]);
			}
			V[i] = condRankSums[i] - sum;
		}

		
		//*****Build the covariance matrix
		double [][] A = new double[numFactorALevels-1][numFactorALevels-1];
		double repsPerCell;
		double repsPerCellAlt;
		
		for(int i = 0; i < numFactorALevels-1; i++) {
			for(int j = 0; j < numFactorALevels-1; j++) {
				
				if(i != j) {
					for(int cond = 0; cond < numFactorBLevels; cond++) {

						repsPerCell = (double)numRepsPerCell[i][cond];						
						repsPerCellAlt = (double)numRepsPerCell[j][cond];
						
						A[i][j] += ((double)(repsPerCell * repsPerCellAlt * (q[cond]+1)))
						/(12d*Math.pow(q[cond],2));
					}
					A[i][j] = (-1d)*A[i][j];
				} else { //main diagonal
					for(int cond = 0; cond < numFactorBLevels; cond++) {
						repsPerCell = numRepsPerCell[i][cond];
						A[i][j] +=  ((float)(repsPerCell * (q[cond]-repsPerCell)*(q[cond]+1)))
						/(12d*Math.pow(q[cond],2));
					}
				}				
			}			
		}
	
		//****get coVar inverse
		DoubleSquareMatrix coVarDoubleMatrix = new DoubleSquareMatrix(A);
		DoubleSquareMatrix invCoVar = coVarDoubleMatrix.inverse();
		
		double [][] dm = new double[1][];
		dm[0] = V;
		
		DoubleMatrix vVec = new DoubleMatrix(dm);
		DoubleMatrix vVecTranspose = (DoubleMatrix)(vVec.transpose());
		DoubleMatrix ms = (vVec.multiply(invCoVar)).multiply(vVecTranspose);

		return ms.getElement(0,0);
	}
	
	
	
	/**
	 * Returns the sums of cell-wise averages, one for each factor
	 * 
	 * @param v data array
	 * @param numFactorsALevels number of factor A levels
	 * @param factorAGroups factor A group specification
	 * @param condAGroups condition group specification
	 * @return computed S values ( Sums of cell wise averages) for each 
	 */
	private float [] computeSValues(float [] v, int numRep, int numFactorALevels, int numFactorBLevels, int [] factorAGrouping, int [] factorBGrouping) {
		
		float []sArray = new float[numFactorALevels];
	
		QSort qsort;
		float [] sortedV; 
		int [] sortedIndices; 
		float [] vals;
		int valIndex;
		
		float [] rankings;
		float [][] condRankings = new float[numFactorBLevels][];
		int [][] sortedRankingIndices = new int [numFactorBLevels][];

			vals = new float[numRep*numFactorALevels];
			
			for(int cond = 0; cond < numFactorBLevels; cond++) {

				int [] valueIndices = new int[vals.length];
				int [] tempValIndices = new int[vals.length];
				
				//accumulate the data for this condition
				valIndex = 0;
				
				//get values for condition j
				for(int k = 0; k < v.length; k++) {					
					if(factorBGrouping[k] == (cond)) {
						vals[valIndex] = v[k];
						tempValIndices[valIndex] = k;
						valIndex++;
					}
				}
				
				//now we have all data for one condition
				//sort and get indices
				qsort = new QSort(vals);				
				sortedV = qsort.getSorted();
				sortedIndices = qsort.getOrigIndx();								

				for(int i = 0; i < sortedIndices.length; i++) {
					valueIndices[i] = tempValIndices[sortedIndices[i]];
				}
				
				//valueIndices holds rank sorted indices back to value array
				sortedRankingIndices[cond] = valueIndices;
				
				rankings = getRankings(sortedV);
				condRankings[cond] = rankings;
			}

			//ranking sums for each factor
			float [] rankingSums = new float[numFactorALevels];
			float [] currCondRanking;
			int [] currSortedIndices;
		
			//accumulate sums from condRankings for each factor
			for(int factor = 0; factor < numFactorALevels; factor++) {
				
				for(int cond = 0; cond < numFactorBLevels; cond++) {					
					currCondRanking = condRankings[cond];
					currSortedIndices = sortedRankingIndices[cond];
					
					for(int rankIndex = 0; rankIndex < currCondRanking.length; rankIndex++) {
						if(factorAGrouping[currSortedIndices[rankIndex]] == (factor)) {
							rankingSums[factor] += currCondRanking[rankIndex];
						}												
					}
				}				
				//sArray[factor] = rankingSums[factor]/(float)numCond;			
				sArray[factor] = rankingSums[factor]/((float)numRep);			
			}
		
		return sArray;		
	}
	
	
	
	private float [] getRankings(float [] sortedVals) {
		float [] ranks = new float[sortedVals.length];
		
		float accRank = 0f;
		int numAcc = 1;
		float currRank = 1;
		boolean acc = false;
		
		for(int i = 0; i < sortedVals.length; i++) {
			
			if(i < sortedVals.length-1) {
				
				if(sortedVals[i] != sortedVals[i+1]) {					
					
					if(!acc) {
						ranks[i] = i+1;
						accRank = ranks[i];
						numAcc = 1;
					} else {
						acc=false;
						
						accRank += (float)(i+1);
						
						//need to go back fill in this for ealears
						for(int j = 0; j < numAcc; j++) {
							ranks[i-j] = ((float)accRank)/((float)numAcc);
						}
						accRank = ranks[i];
						numAcc = 1;
					}
				} else {
					if(acc)
						accRank += (float)(i+1);
					else
						accRank = (float)(i+1);
					numAcc++;
					acc = true;
				}
			} else {
			
				//set last rank
				if(!acc) {
					ranks[ranks.length-1] = ranks.length;
					//accRank += (float)(i+1);
				} else {
					accRank += (float)(i+1);
					for(int j = 0; j < numAcc; j++) {
						ranks[ranks.length-1-j] = ((float)accRank)/(float)numAcc;
					}
				}
			}
		}
		return ranks;
	}
	
	private float getMackSkillingsStatistic(float [] sArray, int numReps, int numFactorALevels, int numFactorBLevels) {	
		float sumSquaredS = 0;
		float numVals = (float)(numReps*numFactorALevels*numFactorBLevels);		
		for(int i = 0; i < sArray.length; i++)
			sumSquaredS += Math.pow(sArray[i],2);		
		float ms = (((float)12)/(float)(numFactorALevels*(numVals+numFactorBLevels))) * sumSquaredS - 3*(numVals+numFactorBLevels);		
		return ms;
	}
	
	private float getPValue(float msStat, float df) {
		ChiSqrDistribution d = new ChiSqrDistribution(df);
		return (float)(1-d.cumulative(msStat));
	}
	
	
	private double [] wilcoxonImpl(float [] data, int [] grouping, int n, int m) {
		
		QSort sort = new QSort(data);
		float [] sortedData = sort.getSorted();
		int [] sortedOrder = sort.getOrigIndx();		
		float [] rankings = this.getRankings(sortedData);
		
		//W, W*, p-value, 1=ties else !ties
		double [] results = new double[4];
		//get the W statistic for group 2
		//group assignments start at 0 ?
		
		double W = 0d;
		
		if(true) { // used to check for flat... !isFlat(sortedData)) {	
			
			for(int i = 0; i < sortedData.length; i++) {
				//sum rankings for one group
				if(grouping[sortedOrder[i]] == 1)
					W += rankings[i];
			}
			
			//since we are using the normal prob. large sample
			//approximation we need to modify W to get W_Star
			
			
			double varNotW; 
			
			if(!areThereRankingTies(rankings)) {
			
				varNotW = (n*m*(n+m+1))/12d;

			} else {
				//there are ties... continutity correction
				
				//number of tied sets are given by the size of ties
				//values are size (number of tied values)
				//note single non-ties are considered tied group of size 1.
				double [] ties = getNumberAndSizeOfTies(rankings);
				double tiedSum = 0;
				for(int i = 0; i < ties.length; i++)
					tiedSum += (ties[i]-1)*ties[i]*(ties[i]+1);
				
				int N = m+n;								
				varNotW = ((n*m*(N+1d))/12d)-(n*m*tiedSum)/(12d*N*(N-1));
				//System.out.println("tiedSum="+tiedSum);
				//System.out.println("varNotW="+varNotW);
				results[3] = 1.0d;
			}
							
			double W_Star = (W - ((m*(n+m+1))/2d))/Math.sqrt(varNotW);

			//System.out.println("W="+W);
			//System.out.println("W*="+W_Star);
			NormalDistribution normDist = new NormalDistribution();
			normDist.cumulative(W_Star);
			//System.out.println("W*pvalue"+normDist.cumulative(W_Star));
		
			results[0] = W;
			results[1] = W_Star;
			results[2] = 2d*(1d-normDist.cumulative(Math.abs(W_Star)));			
			//System.out.println("W*pvalue reported"+results[2]);
			
		}
		return results;
	}

	private boolean isFlat(float [] data) {
		for(int i = 0; i < data.length-1; i++) {
			if( !Float.isNaN(data[i]) && !Float.isNaN(data[i+1]))
				if(data[i] != data[i+1])
					return false;
		}
		return true;
	}
	
	private boolean areThereRankingTies(float [] rankings) {		
		for(int i = 0; i < rankings.length-1; i++)
			if(rankings[i] == rankings[i+1])
				return true;
		return false;
	}

	
	private double [] getNumberAndSizeOfTies(float [] rankings) {
		Vector tiedCounts = new Vector();
		int currCnt = 1;
		boolean buildingTies = false;
		
		for(int i = 0; i < rankings.length-1; i++) {
			if(rankings[i] == rankings[i+1]) {
				currCnt++;
				buildingTies = true;
			
			} else {
					
				tiedCounts.add(new Integer(currCnt));
				
				currCnt = 1;
				buildingTies = false;
			}
		}
		
		if(rankings[rankings.length-2] == rankings[rankings.length-1])
			tiedCounts.add(new Integer(currCnt));
		else
			tiedCounts.add(new Integer(1));
		
		double [] counts = new double[tiedCounts.size()];
		for(int i = 0; i < counts.length; i++) {
			counts[i] = ((Integer)(tiedCounts.get(i))).doubleValue();
			//System.out.println(counts[i]);
		}
		return counts;
	}
	
	
	
	private float [] kruskalImpl(float [] data, int [] groups, int numGroups) {
		float [] results = new float[4];
		
		boolean isFlat = this.isFlat(data);		
		QSort sort = new QSort(data);
		float [] sortedData = sort.getSorted();
		int [] sortedOrder = sort.getOrigIndx();
		float [] rankings = this.getRankings(sortedData);		
		boolean hasTies = this.areThereRankingTies(rankings);
		
		double H;
		
		int [] groupCounts = getGroupCounts(groups, numGroups);
		
		
		double [] R = getKruskalRvalues(rankings, sortedOrder, groups, numGroups);
		
		//check
		//for(int i = 0; i < R.length; i++)
			//System.out.println("R ="+R[i]);
		
		double sumNormRsquared = 0d;
		
		for(int group = 0; group < numGroups; group++)
			sumNormRsquared += Math.pow(R[group],2d)/groupCounts[group];
		
		int N = rankings.length;
		
		H = 12*sumNormRsquared/(N*(N+1))-3*(N+1);
		
		//System.out.println("H = "+H);
		
		if(hasTies) {
			results[3] = 1f;
			double [] ties = getNumberAndSizeOfTies(rankings);			
			double tiesSumVal = 0d;			
			for(int i = 0; i < ties.length; i++)
				tiesSumVal += Math.pow(ties[i],3)-ties[i];			
			double HPrime = H/(1-(tiesSumVal/(Math.pow(N,3)-N)));		
			H = HPrime;
		} 
		double pValue = getPValue((float)H, (float)(numGroups-1));

		results[0] = (float)(numGroups-1);		
		results[1] = (float)H;
		results[2] = (float)pValue;
		return results;
	}
	
	private int [] getGroupCounts(int [] groups, int numGroups){
		int [] counts = new int[numGroups];
		
		for(int group = 0; group < numGroups; group++) {
			for(int i = 0; i < groups.length; i++) {
				if(groups[i] == group) {
					counts[group]++;
				}
			}
		}		
		return counts;	
	}
	
	private Vector benjaminiHochberg(float [] pvalues) {
		Vector valuesAndOrderArrays = new Vector();
		
		QSort sort = new QSort(pvalues);
		float [] sortedP = sort.getSorted();

		int [] sortedIndices = sort.getOrigIndx();
		float [] adjustedP = new float[pvalues.length];


		//adjust pvalues
		for(int i = 0; i < sortedP.length; i++) {
			adjustedP[i] = (sortedP[i]*(float)sortedP.length)/(float)(i+1);		
			//System.out.println(adjustedP[i]);		
		}
		
		if(sortedP.length > 0) {
		//stepdown procedure, store in sortedP
		sortedP[sortedP.length-1] = adjustedP[sortedP.length-1];
		
		for(int i = sortedP.length-2; i >= 0; i--) {
			sortedP[i] = Math.min(sortedP[i+1],adjustedP[i]);
	//		if(adjustedP[i] > adjustedP[i+1])
		//		sortedP[i] = adjustedP[i+1];
			//else
				//sortedP[i] = adjustedP[i];
		}
		}
		
		valuesAndOrderArrays.add(sortedP);
		valuesAndOrderArrays.add(sortedIndices);	
		return valuesAndOrderArrays;		
	}
	
	private double [] getKruskalRvalues(float [] rankings, int [] sortedOrder, int [] groups, int numGroups) {
		double [] R = new double[numGroups];
		
		for(int group = 0; group < numGroups; group++){
			R[group] = 0d;
			for(int valIndex = 0; valIndex < rankings.length; valIndex++) {
				if(groups[sortedOrder[valIndex]] == group) {
					R[group] += rankings[valIndex];
				}
			}
		}
		
		return R;
	}
	
	public class NonparOneWayData {
		private boolean isFlat = true;
		private boolean areGroupsNull = false;
		
		private float [] values;
		private int [] groups;
		private int [] nPerGroup;
		private int numGroups;
		
		private NonparOneWayData(float [] rawValues, int [] initGroupings, int nGroups) {
			numGroups = nGroups;
			
			Vector valueVector = new Vector();
			Vector groupVector = new Vector();
			
			//System.out.println("****** Initgroupins length = "+initGroupings.length);
			
			for(int i = 0; i < rawValues.length; i++) {
				if(!Float.isNaN(rawValues[i]) && initGroupings[i] != -1) {
					valueVector.add(new Float(rawValues[i]));
					groupVector.add(new Integer(initGroupings[i]));
				}
			}
			
			values = new float[valueVector.size()];
			groups = new int[groupVector.size()];
			
			for(int i = 0; i < values.length; i++) {
				values[i] = ((Float)(valueVector.get(i))).floatValue();
				groups[i] = ((Integer)(groupVector.get(i))).intValue();
			}
			
			//check for flat
			isFlat = isFlat(values);
			
			//check counts (for a group with no representation
			nPerGroup = getGroupCounts(groups, numGroups);
			
			for(int i = 0; i < nPerGroup.length; i++)
				if(nPerGroup[i] == 0) {
					areGroupsNull = true;
					break;
				}					
		}
		
		public float [] getValues() { return values; }
		
		public int [] getGroups() { return groups; }
		
		public int getNumGroups() { return numGroups; }
		
		public boolean getAreGroupsNull() { return areGroupsNull; }
		
		public boolean getIsFlat() { return isFlat; }
		
		public int [] getGroupNs() { return nPerGroup; }
	}
	
	/**
	 *NonparTwoWayData is used to encapsulate data structures related to a two way design
	 */
	public class NonparTwoWayData {
		
		private boolean isComplete = true;
		private boolean isBalanced = true;
		private boolean isFlat = true;		
		private float [] values;
		private int [] factorAGrouping;
		private int [] factorBGrouping;
		private int [][] repTotals;
		private int nVals;
 
		
		public NonparTwoWayData(float [] rawValues, int numFactorALevels, int numFactorBLevels, int [] rawFactorAGrouping, int [] rawFactorBGrouping) {
			repTotals = new int[numFactorALevels][numFactorBLevels];
			isFlat = true;
			Vector valueVector = new Vector();
			Vector factorAVector = new Vector();
			Vector factorBVector = new Vector();
			Float value;
			
			//get the good values
			for(int i = 0; i < rawValues.length ; i++) {
				if(!Float.isNaN(rawValues[i]) && rawFactorAGrouping[i] != -1 && rawFactorBGrouping[i] != -1) {
					value = new Float(rawValues[i]);

					//check for a flat vector unless we already have two different values
					if(isFlat && valueVector.size() > 0 && !valueVector.contains(value));
						isFlat = false;
						
					valueVector.add(new Float(rawValues[i]));
					factorAVector.add(new Integer(rawFactorAGrouping[i]));
					factorBVector.add(new Integer(rawFactorBGrouping[i]));		
				}
			}

			nVals = valueVector.size();	
			values = new float[nVals];
			factorAGrouping = new int[nVals];
			factorBGrouping = new int[nVals];
			
			//make arrays and replicate totals for each cell
			for(int i = 0; i < nVals; i++ ){
				values[i] = ((Float)(valueVector.get(i))).floatValue();
				factorAGrouping[i] = ((Integer)(factorAVector.get(i))).intValue();
				factorBGrouping[i] = ((Integer)(factorBVector.get(i))).intValue();
				repTotals[factorAGrouping[i]][factorBGrouping[i]]++;			
			}			
			
			//check for completeness and balance
			int checkValue = repTotals[0][0];
			for(int i = 0; i < repTotals.length; i++) {
				for(int j = 0; j < repTotals[i].length; j++) {
					if(repTotals[i][j] == 0) {
						isBalanced = false;
						isComplete = false;
						break;
					}
					if(repTotals[i][j] != checkValue) {
						isBalanced = false;
					}					
				}
			}					
		}
		
		public void transposeRepTots() {
			int [][] newRepTots = new int[repTotals[0].length][repTotals.length];
			for(int i = 0; i < repTotals.length; i++)
				for(int j = 0; j < repTotals[i].length;j++)
					newRepTots[j][i] = repTotals[i][j];
			repTotals = newRepTots;
		}
	}
	
	
	
	
	
	/**********************************************************************************
	***********************************************************************************
	*
	*	R implementation methods and R support methods
	*
	***********************************************************************************
	***********************************************************************************
	*/
	
	
	/** Executes the Wilcoxon Rank Sum test on a matrix of data.
	 * Precondtions: global AlgorithmData (algData) has parameters for group selections
	 * 
	 * Postcondition: global algData recieves FloatMatrix resultMatrix with result values
	 * and a String [] called methods that describes the method (i.e. if ties existed use
	 * continuity approximation of 'W'
	 * 
	 * @param m <code>FloatMatrix</code> that contains the data values
	 */
	private void executeWilcoxon(FloatMatrix m) throws AlgorithmException {
		
		boolean useAlpha = algData.getParams().getBoolean("use-alpha-criterion", true);
		Vector sigGenes, nonSigGenes; //for accumulation of gene sets
		
		//for each row record n group1, n group2, 
		//estimate (diff in mean), lower and upper 95% CI, U, W, p.value, if not useAlpha take 9 for BH adjustment
		FloatMatrix resultValues = new FloatMatrix(m.getRowDimension(), useAlpha ? 8 : 9);
		
		//arrays for cluster indices
		int [][] clusters = new int[2][];
		
		//indicate the methods used, important if there are ties
		String [] method = new String[m.getRowDimension()];
		
		//get the R command (function def.) for checking for a flat vector 
		String isFlatFunctionCommand = getIsFlatFunction();
	
		//get the R command (function def.) for runing the wilcoxon in batch mode
		String wilcoxFunction = getWilcoxFunction();

		this.fireValueChanged(new AlgorithmEvent(this, AlgorithmEvent.PROGRESS_VALUE, 0, "Prepating R for Exectution"));
		
		//open the manager and get a connection
		RconnectionManager manager = new RconnectionManager(new Frame(), "localhost", 6311);		
		Rconnection rc = manager.getConnection();
		
		
		long start,finish;
		
		try {
			
			//clear all from session
			rc.voidEval("rm(list = ls(all=TRUE))");
			
			//select the stats lib
			rc.voidEval("library(stats)");
			rc.voidEval("library(multtest)");

			
			//set the group assignment vector, from alg data			
			rc.assign("groups", algData.getIntArray("group-assignments"));
			
			
			//define the function in R to check for a flat vector
			rc.voidEval(isFlatFunctionCommand);
			
			//define the function in R to run wilcox.test in batch
			rc.voidEval(wilcoxFunction);
			
			
			//Now R has the group assignments, and function definitios
			//now loop through data matrix to pull values 
						
			//loop through data matrix and pull chunks
			int numRows = m.getRowDimension();
			int firstIndex = 0;
			int lastIndex = -1;

			//set a number of interations that is a fraction of the full data set
			int totIterations = (int)(1/FRACTION_PER_BATCH);
			
			//take this number or set iterations so that the batch size is constrained
			totIterations = Math.max(totIterations, numRows/MAX_BATCH_SIZE);

			//in case of just a few rows of data
			if(numRows < totIterations)
				totIterations = 1;
			
			double [] vals;
			
			this.fireValueChanged(new AlgorithmEvent(this, AlgorithmEvent.PROGRESS_VALUE, 0, "Executing Wilcoxon Rank Sum"));

			double [] p_values = new double[m.getRowDimension()]; 
														
			for(int iter = 1; iter <= totIterations; iter++ ) {				

				if(totIterations == 1) {
					//if < 1000 rows do one interation
					float [] floatVals = m.getRowPackedCopy();
					vals = new double[floatVals.length];			
					for(int i = 0; i < vals.length; i++) {
						vals[i] = (double)(floatVals[i]);
					}
				} else {

					firstIndex = lastIndex+1;
					lastIndex = (int)(numRows*FRACTION_PER_BATCH) * iter;	
					
					if(lastIndex-firstIndex > MAX_BATCH_SIZE)
						lastIndex = firstIndex+MAX_BATCH_SIZE-1;
							
					//get the values for the iteration		
					vals = getDataChunk(m, firstIndex, lastIndex);	
				}
				
				//assign the current data as double array
				rc.assign("data", vals);
				
				//re-form a matrix from the double array
				rc.voidEval("m<-matrix(data, nrow="+vals.length/m.getColumnDimension()+ ", ncol="+Integer.toString(m.getColumnDimension())+
				", byrow=TRUE)");
				
				rc.voidEval("rm(data)");
				
				start = System.currentTimeMillis();
				
				rc.voidEval("res<-wilcoxFunction()");
				
				finish = System.currentTimeMillis();

				//execute the function and grab the return as a vector (of RList objects)
				Vector resultVector = rc.eval("res;").asVector();;
				
				//get the matrix indicating group N ***Last result appended to the vector***
				double [][] groupNMatrix = ((REXP)(resultVector.get(resultVector.size()-1))).asDoubleMatrix();
				
				RList res;
				double [] lowerUpper95CI;
				REXP ciRes;				
		
				for(int i = 0 ; i < resultVector.size()-1; i++) {
					res = ((REXP)(resultVector.get(i))).asList();	
					
					resultValues.A[i][0] = (int)groupNMatrix[i][0];
					resultValues.A[i][1] = (int)groupNMatrix[i][1];
					
					ciRes = res.at("conf.int");
					
					if(ciRes != null ) {
						lowerUpper95CI = res.at("conf.int").asDoubleArray();
						//estimate of mean difference
						resultValues.A[i+firstIndex][2] = (float)(res.at("estimate").asDouble());				
						//lower 95% CI if mean diff
						resultValues.A[i+firstIndex][3] = (float)(lowerUpper95CI[0]);
						//upper 95% CI of mean diff
						resultValues.A[i+firstIndex][4] = (float)(lowerUpper95CI[1]);
					} else {
						//no CI was determined one or both vectors were flat
						
						//estimate of mean difference
						resultValues.A[i+firstIndex][2] = Float.NaN;				
						//lower 95% CI if mean diff
						resultValues.A[i+firstIndex][3] = Float.NaN;
						//upper 95% CI of mean diff
						resultValues.A[i+firstIndex][4] = Float.NaN;						
					}
					
					//Mann Whitney's U statistic
					resultValues.A[i+firstIndex][5] = (float)(res.at("statistic").asDouble());
					//Wilcoxon Rank Sum dirived from U + n(n+1)/2, n is size of group 1
					resultValues.A[i+firstIndex][6] = resultValues.A[i][5]+(resultValues.A[i][0]*(resultValues.A[i][0]+1f))/2f;
					//p value
					resultValues.A[i+firstIndex][7] = (float)(res.at("p.value").asDouble());
					
					//grab the pvalue for the array
					p_values[i+firstIndex] = resultValues.A[i+firstIndex][7];
					
					//indicates test, 'continuity correction indicates that there were tied ranks'
					//in the case of ties a normal approximation is used to resplve 
					method[i+firstIndex] = res.at("method").asString();															
				}

				this.fireValueChanged(new AlgorithmEvent(this, AlgorithmEvent.PROGRESS_VALUE, (int)(((float)iter/(float)totIterations)*100f), "Executing Wilcoxon Rank Sum (finished batch "+iter+")"));				
			} //end iterations

			this.fireValueChanged(new AlgorithmEvent(this, AlgorithmEvent.PROGRESS_VALUE, 100, "Processing Results"));

			//holds indices
			int [] sigGenesArray;
			int [] nonSigGenesArray;
			
			//if using FDR
			if(!useAlpha) {
				rc.assign("rawp",p_values);
				rc.voidEval("adjpV<-mt.rawp2adjp(rawp, proc=\"BH\");");
				REXP adjRes = rc.eval("adjpV");

				RList adjList = adjRes.asList();
				
				//first column is raw p, second column is the adjusted p in order
				double [][] adjP = adjList.at("adjp").asDoubleMatrix();
			
				float [] adjPValues = new float[adjP.length];

				int resultWidth = resultValues.getColumnDimension();
				
				//ranking order on adjusted p-value
				int [] orderedIndices = adjList.at("index").asIntArray();

				for(int i = 0; i < adjP.length; i++ ) {
					//A[i][0] = (float)(adjP[i][1]);
					adjPValues[i] = (float)(adjP[i][1]);

					//add the result to result values
					resultValues.A[orderedIndices[i]-1][resultWidth-1] = adjPValues[i];
				}
				
				//if use the fdr graph
				if(algData.getParams().getBoolean("use-fdr-graph")) {
					

					//get the frame
					Object [][] frameMatrix = algData.getObjectMatrix("main-frame");
					
					NonparFDRDialog fdrDialog = new NonparFDRDialog((JFrame)(frameMatrix[0][0]), adjPValues, orderedIndices);;
					if(fdrDialog.showModal() == JOptionPane.OK_OPTION) {
						sigGenesArray = fdrDialog.getSelectedIndices();
						nonSigGenesArray = fdrDialog.getNonSelectedIndices();
						
						clusters[0] = sigGenesArray;
						clusters[1] = nonSigGenesArray;	
					}
					
					algData.addParam("fdr", String.valueOf(fdrDialog.getFDRLimit()));
					
					//else using a fixed FDR
				} else {
					//just used the fixed fdr
					float fdrLimit = algData.getParams().getFloat("fdr");
					
					sigGenes = new Vector();
					nonSigGenes = new Vector();
					
					for(int i = 0; i < adjPValues.length; i++) {
						if(adjPValues[i] <= fdrLimit)
							sigGenes.add(new Integer(orderedIndices[i]-1));
						else //put non conforming and NaN's in NS
							nonSigGenes.add(new Integer(orderedIndices[i]-1));
					}

					sigGenesArray = new int [sigGenes.size()];
					nonSigGenesArray = new int [nonSigGenes.size()];
					
					for(int i = 0; i < sigGenesArray.length; i++)
						sigGenesArray[i] = ((Integer)(sigGenes.get(i))).intValue();
					for(int i = 0; i < nonSigGenesArray.length; i++)
						nonSigGenesArray[i] = ((Integer)(nonSigGenes.get(i))).intValue();

					clusters[0] = sigGenesArray;
					clusters[1] = nonSigGenesArray;	
				}
				
				//lets capture the estimated fdr for the signficant genes
				//last sorted adjusted fdr based on clusters[0] size
				algData.addParam("estimated-fdr", String.valueOf(adjPValues[clusters[0].length-1]));
				
			} else {
				//just use alpha as criterion for significcance

				//get cluster partiions to get means and variances
				sigGenes = new Vector();
				nonSigGenes = new Vector();			
				float alpha = algData.getParams().getFloat("alpha");
				int numResultCols = resultValues.getColumnDimension();
				for(int i = 0; i < numRows; i++) {
					if(resultValues.A[i][numResultCols-1] <= alpha)
						sigGenes.add(new Integer(i));
					else
						nonSigGenes.add(new Integer(i));
				}			
				
				sigGenesArray = new int [sigGenes.size()];
				nonSigGenesArray = new int [nonSigGenes.size()];
				
				for(int i = 0; i < sigGenesArray.length; i++)
					sigGenesArray[i] = ((Integer)(sigGenes.get(i))).intValue();
				for(int i = 0; i < nonSigGenesArray.length; i++)
					nonSigGenesArray[i] = ((Integer)(nonSigGenes.get(i))).intValue();

				clusters[0] = sigGenesArray;
				clusters[1] = nonSigGenesArray;	
				
			}
						
			algData.addMatrix("result-matrix", resultValues);
			algData.addStringArray("method-array", method);
			
			FloatMatrix clusterMeans = this.getMeans(m, clusters);
			FloatMatrix clusterVars = this.getVariances(m, clusterMeans, clusters);
			
			algData.addIntMatrix("clusters", clusters);
			algData.addMatrix("cluster-means", clusterMeans);
			algData.addMatrix("cluster-variances", clusterVars);

		
			//if we are to run HCL			
			//precondition... we have selected significant genes into cluster int [][]
			if(algData.getParams().getBoolean("hcl-execution")) {
				
			    int linkageMethod = algData.getParams().getInt("method-linkage");
				int metric = algData.getParams().getInt("hcl-distance-function");
				boolean genes = algData.getParams().getBoolean("calculate-genes");
				boolean experiments = algData.getParams().getBoolean("calculate-samples");
				boolean absoluteDistance = algData.getParams().getBoolean("hcl-distance-absolute");
			
				this.fireValueChanged(new AlgorithmEvent(this, AlgorithmEvent.PROGRESS_VALUE, 100, "Constructing Hierarchical Trees"));									
				
				NodeValueList nodeValueList =  calculateHierarchicalTree(m, clusters[0], linkageMethod, metric, absoluteDistance, genes, experiments);

				Node node = new Node(clusters[0]);
				node.setValues(nodeValueList);
				
				NodeList nodeList = new NodeList();
				nodeList.addNode(node);
								
				Cluster hclCluster = new Cluster();
				hclCluster.setNodeList(nodeList);
				
				algData.addCluster("hcl-clusters", hclCluster);
			}
			
			this.fireValueChanged(new AlgorithmEvent(this, AlgorithmEvent.PROGRESS_VALUE, 110, "Processing Results"));									
			
		} catch (RSrvException e) {
			e.printStackTrace();
		}		
		rc.close();
	}
	
	/**
	 * Executes the Kruskal-Wallis test and puts the result into the AlgorithmData
	 * @param m the FloatMatrix of data
	 * @throws AlgorithmException
	 */
	private void executeKruskalWallis(FloatMatrix m) throws AlgorithmException {
		
		boolean useAlpha = algData.getParams().getBoolean("use-alpha-criterion", true);
		Vector sigGenes, nonSigGenes; //for accumulation of gene sets
		
		//for each row record statistic, df, and p-value	
		FloatMatrix resultValues = new FloatMatrix(m.getRowDimension(), useAlpha ? 3 : 4);
		
		//arrays for cluster indices
		int [][] clusters = new int[2][];
		
		//indicate the methods used, important if there are ties
		String [] method = new String[m.getRowDimension()];
		
		//get the R command (function def.) for checking for a flat vector 
		String isFlatFunctionCommand = getIsFlatFunction();
		
		//get the R command (function def.) for runing the wilcoxon in batch mode
		String kruskalFunction = getKruscalFunction();
		
		this.fireValueChanged(new AlgorithmEvent(this, AlgorithmEvent.PROGRESS_VALUE, 0, "Prepating R for Exectution"));
		
		//open the manager and get a connection
		RconnectionManager manager = new RconnectionManager(new Frame(), "localhost", 6311);		
		Rconnection rc = manager.getConnection();		
		
		long start,finish;
		
		try {			
			//clear all from session
			rc.voidEval("rm(list = ls(all=TRUE))");
			
			//select the stats lib
			rc.voidEval("library(stats)");
			
			//set the group assignment vector, from alg data			
			rc.assign("groups", algData.getIntArray("group-assignments"));
			
			//define the function in R to run kruscal.test in batch
			rc.voidEval(kruskalFunction);
			
			//Now R has the group assignments, and function definitios
			//now loop through data matrix to pull values 
			
			//loop through data matrix and pull chunks
			int numRows = m.getRowDimension();
			int firstIndex = 0;
			int lastIndex = -1;
			
			//set a number of interations that is a fraction of the full data set
			int totIterations = (int)(1/FRACTION_PER_BATCH);
			
			//take this number or set iterations so that the batch size is constrained
			totIterations = Math.max(totIterations, numRows/MAX_BATCH_SIZE);
			
			//in case of just a few rows of data
			if(numRows < totIterations)
				totIterations = 1;			
			
			double [] vals;
			
			this.fireValueChanged(new AlgorithmEvent(this, AlgorithmEvent.PROGRESS_VALUE, 0, "Executing Kruskal-Wallis Test"));
			
			double [] p_values = new double[m.getRowDimension()]; 
			
			for(int iter = 1; iter <= totIterations; iter++ ) {
				
				if(totIterations == 1) {
					//if < 1000 rows do one interation
					float [] floatVals = m.getRowPackedCopy();
					vals = new double[floatVals.length];			
					for(int i = 0; i < vals.length; i++) {
						vals[i] = (double)(floatVals[i]);
					}
				} else {
					//if > 1000 rows do iteration data chunks
					firstIndex = lastIndex+1;
					lastIndex = (int)(numRows*FRACTION_PER_BATCH) * iter;
					
					if(lastIndex-firstIndex > MAX_BATCH_SIZE)
						lastIndex = firstIndex+MAX_BATCH_SIZE-1;
					
					//get the values for the iteration		
					start = System.currentTimeMillis();
					vals = getDataChunk(m, firstIndex, lastIndex);	
					finish = System.currentTimeMillis();
					
				}
				
				
				//assign the current data as double array
				rc.assign("data", vals);
				
				//re-form a matrix from the double array
				rc.voidEval("m<-matrix(data, nrow="+vals.length/m.getColumnDimension()+ ", ncol="+Integer.toString(m.getColumnDimension())+
				", byrow=TRUE)");
				
				rc.voidEval("rm(data)");
				
				start = System.currentTimeMillis();
				
				rc.voidEval("res<-kruskalFunction()");
				
				finish = System.currentTimeMillis();
				
				//execute the function and grab the return as a vector (of RList objects)
				Vector resultVector = rc.eval("res;").asVector();;
				
				RList res;
				
				for(int i = 0 ; i < resultVector.size()-1; i++) {
					
					res = ((REXP)(resultVector.get(i))).asList();	
					
					//Kruskal Rank statistic
					resultValues.A[i+firstIndex][0] = (float)(res.at("statistic").asDouble());
					
					//parameter refers to df
					resultValues.A[i+firstIndex][1] = (float)(res.at("parameter").asDouble());
					
					//p value
					resultValues.A[i+firstIndex][2] = (float)(res.at("p.value").asDouble());
					
					//grab the pvalue for the array
					p_values[i+firstIndex] = resultValues.A[i+firstIndex][2];
					
					//method description
					method[i+firstIndex] = res.at("method").asString();															
				}
				
				this.fireValueChanged(new AlgorithmEvent(this, AlgorithmEvent.PROGRESS_VALUE, (int)(((float)iter/(float)totIterations)*100f), "Executing Kruskal-Wallis Test (finished batch "+iter+")"));				
			}	
			
			//holds indices
			int [] sigGenesArray;
			int [] nonSigGenesArray;
			
			//if using FDR
			if(!useAlpha) {
				rc.assign("rawp",p_values);
				rc.voidEval("adjpV<-mt.rawp2adjp(rawp, proc=\"BH\");");
				REXP adjRes = rc.eval("adjpV");
				
				RList adjList = adjRes.asList();
				
				//first column is raw p, second column is the adjusted p in order
				double [][] adjP = adjList.at("adjp").asDoubleMatrix();
				
				//ranking order on adjusted p-value
				int [] orderedIndices = adjList.at("index").asIntArray();
				
				//convert to FloatMatrix for export
				float [][] A = new float[adjP.length][1];				
				float [] adjPValues = new float[adjP.length];
				
				int resultWidth = resultValues.getColumnDimension();
				
				for(int i = 0; i < adjP.length; i++ ) {
					A[i][0] = (float)(adjP[i][1]);
					
					adjPValues[i] = (float)(adjP[i][1]);
					
					//add the result to result values
					resultValues.A[orderedIndices[i]-1][resultWidth-1] = adjPValues[i];
				}
				
				//if use the fdr graph
				if(algData.getParams().getBoolean("use-fdr-graph")) {
					
					//get the frame
					Object [][] frameMatrix = algData.getObjectMatrix("main-frame");
					
					NonparFDRDialog fdrDialog = new NonparFDRDialog((JFrame)(frameMatrix[0][0]), adjPValues, orderedIndices);;
					if(fdrDialog.showModal() == JOptionPane.OK_OPTION) {
						sigGenesArray = fdrDialog.getSelectedIndices();
						nonSigGenesArray = fdrDialog.getNonSelectedIndices();
						
						clusters[0] = sigGenesArray;
						clusters[1] = nonSigGenesArray;	
					}
					
					algData.addParam("fdr", String.valueOf(fdrDialog.getFDRLimit()));
					
					//else using a fixed FDR
				} else {
					//just used the fixed fdr
					float fdrLimit = algData.getParams().getFloat("fdr");
					
					sigGenes = new Vector();
					nonSigGenes = new Vector();
					
					for(int i = 0; i < adjPValues.length; i++) {						
						if(adjPValues[i] <= fdrLimit)
							sigGenes.add(new Integer(orderedIndices[i]-1));
						else //put non conforming and NaN's in NS
							nonSigGenes.add(new Integer(orderedIndices[i]-1));
					}
					
					sigGenesArray = new int [sigGenes.size()];
					nonSigGenesArray = new int [nonSigGenes.size()];
					
					for(int i = 0; i < sigGenesArray.length; i++)
						sigGenesArray[i] = ((Integer)(sigGenes.get(i))).intValue();
					for(int i = 0; i < nonSigGenesArray.length; i++)
						nonSigGenesArray[i] = ((Integer)(nonSigGenes.get(i))).intValue();
					
					clusters[0] = sigGenesArray;
					clusters[1] = nonSigGenesArray;	
				}
				
				//lets capture the estimated fdr for the signficant genes
				//last sorted adjusted fdr based on clusters[0] size
				algData.addParam("estimated-fdr", String.valueOf(adjPValues[clusters[0].length-1]));
				
			} else {
				
				
				this.fireValueChanged(new AlgorithmEvent(this, AlgorithmEvent.PROGRESS_VALUE, 100, "Processing Results"));
				
				//get cluster partiion
				sigGenes = new Vector();
				nonSigGenes = new Vector();	
				
				float alpha = algData.getParams().getFloat("alpha");
				int numResultCols = resultValues.getColumnDimension();
				
				for(int i = 0; i < numRows; i++) {
					if(resultValues.A[i][numResultCols-1] <= alpha)
						sigGenes.add(new Integer(i));
					else
						nonSigGenes.add(new Integer(i));
				}
				sigGenesArray = new int [sigGenes.size()];
				nonSigGenesArray = new int [nonSigGenes.size()];
				for(int i = 0; i < sigGenesArray.length; i++)
					sigGenesArray[i] = ((Integer)(sigGenes.get(i))).intValue();
				for(int i = 0; i < nonSigGenesArray.length; i++)
					nonSigGenesArray[i] = ((Integer)(nonSigGenes.get(i))).intValue();
				
				clusters[0] = sigGenesArray;
				clusters[1] = nonSigGenesArray;
				
			}
			
			FloatMatrix clusterMeans = this.getMeans(m, clusters);
			FloatMatrix clusterVars = this.getVariances(m, clusterMeans, clusters);
			
			
			algData.addMatrix("result-matrix", resultValues);
			algData.addStringArray("method-array", method);
			
			algData.addIntMatrix("clusters", clusters);
			algData.addMatrix("cluster-means", clusterMeans);
			algData.addMatrix("cluster-variances", clusterVars);
			
			this.fireValueChanged(new AlgorithmEvent(this, AlgorithmEvent.PROGRESS_VALUE, 110, "Processing Results"));
			
			//precondition... we have selected significant genes into cluster int [][]
			if(algData.getParams().getBoolean("hcl-execution")) {
				
				int linkageMethod = algData.getParams().getInt("method-linkage");
				int metric = algData.getParams().getInt("hcl-distance-function");
				boolean genes = algData.getParams().getBoolean("calculate-genes");
				boolean experiments = algData.getParams().getBoolean("calculate-samples");
				boolean absoluteDistance = algData.getParams().getBoolean("hcl-distance-absolute");
				
				this.fireValueChanged(new AlgorithmEvent(this, AlgorithmEvent.PROGRESS_VALUE, 100, "Constructing Hierarchical Trees"));									
				
				NodeValueList nodeValueList =  calculateHierarchicalTree(m, clusters[0], linkageMethod, metric, absoluteDistance, genes, experiments);
				
				Node node = new Node(clusters[0]);
				node.setValues(nodeValueList);
				
				NodeList nodeList = new NodeList();
				nodeList.addNode(node);
				
				Cluster hclCluster = new Cluster();
				hclCluster.setNodeList(nodeList);
				
				algData.addCluster("hcl-clusters", hclCluster);
			}
			
		} catch (RSrvException e) {
			e.printStackTrace();
		}
		
		rc.close();		
	}
	
	/**
	 * Pulls a set of rows from a float data object and dumps into
	 * a double array, this array will be reconstructed into a matrix in R
	 */
	private double [] getDataChunk(FloatMatrix m, int start, int end) {
		if(((m.getRowDimension()-1)<end)) {
			end = m.getRowDimension()-1;
		}	
		double [] vals = new double[((end-start)+1)*m.getColumnDimension()];
		int nCol = m.getColumnDimension();
		int index = 0;		
		for(int row = start; row <= end; row++) {
			for(int col = 0; col < nCol; col++) {
				vals[index] = m.A[row][col];
				index++;
			}
		}		
		return vals;
	}
	
	
	/**
	 * Builds an R expression to check for a flat vector
	 * @return R expression for checking for a flat vector
	 */
	private String getIsFlatFunction() {
		String function = "isFlat<-function(v) {"+			
		"for(i in 1:(length(v)-1)) {\n if( (v[i]!= v[i+1]) ) { \n return(FALSE); }} \n return(TRUE);"+
		"}";
		return function;
	}
	
	
	/**
	 * Constructs an R expression to execute Kruskal
	 * @return R command string
	 */
	private String getKruscalFunction() {
		String function = "kruskalFunction<-function(){\n"+ 			
		"options(warn=-1);\n"+
		"numRows<-nrow(m); numCols<-ncol(m); result<-list();\n"+
		"for(row in 1:numRows) {\n"+
		"result[[row]]<-kruskal.test(m[row, ], groups);\n"+
		"}\n"+
		"return(result);\n"+
		"}";				
		return function;
	}
	
	
	/**
	 * Constructs an R expression to define the process to run Wilcoxon
	 * @return function definition string
	 */
	private String getWilcoxFunction() {
		String function = "wilcoxFunction<-function(){"+ 			
		"options(warn=-1);"+
		"numRows<-nrow(m); numCols<-ncol(m); result<-list();"+
		"is_not_flat = TRUE;"+
		"nMatrix = matrix(nrow=numRows,ncol=2);"+
		"for(row in 1:numRows) {\n"+
		"v1=vector(\"numeric\");"+
		"v2=vector(\"numeric\");"+
			"v1Index = 1; v2Index = 1;\n"+						
			"for(col in 1:numCols) \n {"+
				"if(groups[col] == 0) { v1[v1Index] = m[row, col]; v1Index=v1Index+1; }"+ 					
				"else if(groups[col]==1) { \n v2[v2Index]=m[row,col]; \n v2Index=v2Index+1;}"+
			"}\n"+
			"is_not_flat = !(isFlat(v1) || isFlat(v2));\n"+
			
			//"is_not_flat=FALSE;"+
			
		 "result[[row]]<-try(wilcox.test(v1,v2,conf.int=is_not_flat));\n"+
		 "if(class(result[[row]]) == \"try-error\") {result[[row]]<-try(wilcox.test(v1,v2,conf.int=FALSE));}\n"+
		 
		 "nMatrix[row,1] = as.double(length(v1)); nMatrix[row,2] = as.double(length(v2));"+
		"}\n"+
		"result[[numRows+1]] = data.matrix(nMatrix);"+
		"return(result);\n"+
		"}";
		return function;
	}
	
	
	/**
	 * Pulls data and segregates it into specified group assignments
	 * @param dataVals input vector of floats
	 * @param groupAssignments	integer based grouping array
	 * @param groupNum group index to pull data
	 * @return the data values for that group
	 */
	private double [] pullGroupValues(float [] dataVals, int [] groupAssignments, int groupNum) {
		Vector valueV = new Vector();
		
		for(int i = 0; i < dataVals.length; i++) {
			if(!Float.isNaN(dataVals[i])) {
				if(groupAssignments[i] == groupNum)
					valueV.add(new Double(dataVals[i]));
			}
		}		
		double [] vals = new double[valueV.size()];

		for(int i = 0; i < vals.length; i++) {
			vals[i] = ((Double)(valueV.get(i))).doubleValue();
		}		
		return vals;
	}
	
	
	
	
	
	
	public static void main(String [] args) {
		Nonpar nonpar = new Nonpar();
	
		//float [] data = {0.8f, 0.83f, 1.89f, 1.04f, 1.45f, 1.38f, 1.91f, 1.64f, 1.15f, 1.15f,
		//		1.15f, 0.88f, 0.90f, 0.74f, 1.21f};
		
		//int [] groups = {0,0,0,0,0,0,0,0,0,0,1,1,1,1,1}; 
		/*
		float [] data = {1f,1f,1f,1f,1f,1f,1f,1f};
		int [] groups = {0,0,0,0,1,1,1,1}; 

		int n = 4;
		int m = 4;
		nonpar.wilcoxonImpl(data, groups, n, m);
		
		
		float [] data2 = {2.9f, 3f,2.5f,2.6f,3.2f,3.8f,2.7f,4f,2.4f,2.8f,3.4f,3.7f,2.2f,2f};
		int [] groups2 = {0,0,0,0,0,1,1,1,1,2,2,2,2,2};
		
		nonpar.kruskalImpl(data2,groups2,3);
				
		float [] pValues = {0.01f, 0.013f, 0.014f, 0.015f, 0.05f,0.07f,0.08f,0.09f};
		Vector res = nonpar.benjaminiHochberg(pValues);
		float [] adjP = (float []) res.get(0);
		
		for(int i = 0; i < adjP.length; i++) {
		System.out.println("p("+i+") +"+adjP[i]);
		}
		*/
		
		AlgorithmData ad = new AlgorithmData();
		
		float [] data = {0f,0f,1f,1f,1f, 0f,0f,0f,1f,1f};
		float [] data2 = {0f,0f,0f,0f,0f, 1f,1f,1f,1f,1f};

		int [] groups = {0,0,0,0,0,1,1,1,1,1}; 
		ad.addIntArray("group-assignments", groups);
		ad.addParam("fisher-exact-bin-cutoff", String.valueOf(0.5f));
		
		nonpar.setAlgData(ad);
		
		FloatMatrix matrix = new FloatMatrix(2,10);
		matrix.A[0] = data;
		matrix.A[1] = data2;
		
		try {
		nonpar.executeFisherExactTest(matrix);
		} catch (Exception e) {
			
		}
	}
	
	public void setAlgData(AlgorithmData d) {
		algData = d;
	}

}
