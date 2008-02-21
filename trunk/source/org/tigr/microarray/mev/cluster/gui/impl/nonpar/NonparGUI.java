/*
 * Created on Jan 9, 2007
 */
package org.tigr.microarray.mev.cluster.gui.impl.nonpar;

import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;

import org.tigr.microarray.mev.cluster.Cluster;
import org.tigr.microarray.mev.cluster.Node;
import org.tigr.microarray.mev.cluster.NodeList;
import org.tigr.microarray.mev.cluster.NodeValueList;
import org.tigr.microarray.mev.cluster.algorithm.Algorithm;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmEvent;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmException;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmListener;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IClusterGUI;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IDistanceMenu;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.IViewer;
import org.tigr.microarray.mev.cluster.gui.LeafInfo;
import org.tigr.microarray.mev.cluster.gui.helpers.CentroidUserObject;
import org.tigr.microarray.mev.cluster.gui.helpers.ClusterTableViewer;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.Progress;
import org.tigr.microarray.mev.cluster.gui.impl.hcl.HCLInitDialog;
import org.tigr.microarray.mev.cluster.gui.impl.hcl.HCLTreeData;
import org.tigr.microarray.mev.cluster.gui.impl.hcl.HCLViewer;
import org.tigr.util.FloatMatrix;

/**
 * @author braisted
 *
 * NonparGUI controls parameter collection, calls for execution of Nonpar
 * and prepares result for display.
 */
public class NonparGUI implements IClusterGUI {

	private Algorithm nonpar;	
	private Progress progress;

	/*
	 *  (non-Javadoc)
	 * @see org.tigr.microarray.mev.cluster.gui.IClusterGUI#execute(org.tigr.microarray.mev.cluster.gui.IFramework)
	 */
	public DefaultMutableTreeNode execute(IFramework framework) throws AlgorithmException {

		DefaultMutableTreeNode resultNode = null;
		
		IData idata = framework.getData();
		Experiment experiment = idata.getExperiment();
		FloatMatrix matrix = experiment.getMatrix();
		int numSamples = experiment.getNumberOfSamples();
		
		AlgorithmData algData = new AlgorithmData();
		
		algData.addMatrix("matrix",matrix);
		//JFrame [][] frame = new JFrame[1][1];
		//frame[0][0] = (JFrame)(framework.getFrame());
		
		String [] sampleNames = new String[numSamples];
		
		
		for (int i = 0; i < numSamples; i++) {
            sampleNames[i] = idata.getSampleName(experiment.getSampleIndex(i));
        }
		
		algData.addStringArray("sample-names", sampleNames);

		String [] steps = {"Mode Selection.","Group name selection.", "Group assignment.",
				"Parameter selection.", "Execute"};		
		
		JFrame mainFrame = (JFrame)(framework.getFrame());

		Object [][] frameArray = new Object[1][1];
		frameArray[0][0] = mainFrame;		
		algData.addObjectMatrix("main-frame", frameArray);  //used by Nonpar.java for fdr dialog
		
		NonparInitWizard wiz = new NonparInitWizard(idata, mainFrame, "Nonparameteric Tests Initialization", true, algData, steps, 3, new NonparModePanel(algData, new JDialog()));
	
		if(wiz.showModal() == JOptionPane.OK_OPTION) {

			//grab the HCL parameters if needed
			if(algData.getParams().getBoolean("hcl-execution")) {
				IDistanceMenu menu = framework.getDistanceMenu();				
				   int function = menu.getDistanceFunction();        
			        if (function == Algorithm.DEFAULT) {        
			            function = Algorithm.EUCLIDEAN;            
			        }
			        
	            HCLInitDialog hclDialog = new HCLInitDialog(framework.getFrame(), menu.getFunctionName(function), false, true);
				
	            if(hclDialog.showModal() == JOptionPane.OK_OPTION) {
					int metric = hclDialog.getDistanceMetric();
					boolean runGenes = hclDialog.isClusterGenes();
					boolean runSamples = hclDialog.isClusterExperiments();
					boolean absDistance = hclDialog.getAbsoluteSelection();
					
					algData.addParam("hcl-distance-function", String.valueOf(metric));
					algData.addParam("calculate-genes", String.valueOf(runGenes));
					algData.addParam("calculate-samples", String.valueOf(runSamples));					
					algData.addParam("hcl-distance-absolute", String.valueOf(absDistance));
					algData.addParam("method-linkage", String.valueOf(hclDialog.getMethod()));
				} else {
					algData.addParam("hcl-execution", String.valueOf(false));
				}
			}
			
			//posibly swap group and bin names
			if(algData.getParams().getString("nonpar-mode").equals(NonparConstants.MODE_FISHER_EXACT)) {

				 //change order reflect the contigency matrix 
				//only changes output viewers
				if(algData.getParams().getBoolean("swap-groups")) {					
					//exchage two group names
					String [] groupNames = algData.getStringArray("group-names");
					String temp = groupNames[0];
					groupNames[0] = groupNames[1];
					groupNames[1] = temp;
				}

				//change order reflect the contigency matrix 
				//only changes output viewers
				if(algData.getParams().getBoolean("swap-bins")) {
					//exchage two group names
					String [] binNames = algData.getStringArray("fisher-exact-bin-names");
					String temp = binNames[0];
					binNames[0] = binNames[1];
					binNames[1] = temp;										
				}
				
			}
			
			Listener listener = new Listener();
			progress = new Progress(framework.getFrame(), "NonpaR Progress", listener);
			progress.setUnits(110);
			progress.setDescription("Initialization");
			progress.show();
			
			nonpar = framework.getAlgorithmFactory().getAlgorithm("NONPAR");
			nonpar.addAlgorithmListener(listener);
			AlgorithmData result = nonpar.execute(algData);	

			progress.dispose();
			
			String mode = algData.getParams().getString("nonpar-mode");
	
			int [][] clusters;
			
			if(mode.equals(NonparConstants.MODE_WILCOXON_MANN_WHITNEY)
					|| mode.equals(NonparConstants.MODE_KRUSKAL_WALLIS)
							|| mode.equals(NonparConstants.MODE_FISHER_EXACT)) {
				clusters = algData.getIntMatrix("clusters");				
			} else {
				clusters = algData.getIntMatrix("clusters");												
			}
			
			resultNode = createResultNode(mode, result, idata, clusters, experiment);	
		}
		
		return resultNode;
	}

	
	/**
	 * Constructs aux. data structure
	 * @param data
	 * @param methodArray
	 * @return
	 */
	private String [][] createAuxDataMatrix(FloatMatrix data) {
		String [][] auxData;
		
		auxData = new String[data.getRowDimension()][data.getColumnDimension()];
		
		for(int j = 0; j < auxData.length; j++) {				
			for(int k = 0; k < auxData[j].length; k++) {
				auxData[j][k] = String.valueOf(data.A[j][k]);
			}								
		}
		
		return auxData;
	}
	
	
	/**
	 * Constructs aux. data structure
	 * @param data
	 * @param methodArray
	 * @return
	 */
	private String [][] createAuxDataMatrix(FloatMatrix data, String [] methodArray) {
		String [][] auxData;
	
			auxData = new String[data.getRowDimension()][data.getColumnDimension()+1];
			
			for(int j = 0; j < auxData.length; j++) {

				//get the method for row
				auxData[j][0]= methodArray[j];
				
				for(int k = 0; k < auxData[j].length-1; k++) {
					auxData[j][k+1] = String.valueOf(data.A[j][k]);
				}								
			}
				
		return auxData;
	}
	
	
	private String [][] createAuxDataMatrix(FloatMatrix data, String [] designDescArray, String [] methodArray) {
		String [][] auxData;
	
			auxData = new String[data.getRowDimension()][data.getColumnDimension()+2];
			
			for(int j = 0; j < auxData.length; j++) {

				//get the method for row
				auxData[j][0]= designDescArray[j];
				auxData[j][1]= methodArray[j];
				
				for(int k = 0; k < auxData[j].length-2; k++) {
					auxData[j][k+2] = String.valueOf(data.A[j][k]);
				}								
			}
				
		return auxData;
	}

	
	private DefaultMutableTreeNode createResultNode(String mode, AlgorithmData result, IData idata, int [][] clusters, Experiment experiment) {
		DefaultMutableTreeNode node = null;
		
		if(mode.equals(NonparConstants.MODE_WILCOXON_MANN_WHITNEY)) {
			node = new DefaultMutableTreeNode("NonpaR [Wilcoxon Rank Sum]");
			addExpressionImages(node, mode, result, clusters, experiment);			
			if(result.getParams().getBoolean("hcl-execution", false))
				addHierarchicalTrees(result, mode, node, result.getCluster("hcl-clusters"), experiment, 
						result.getParams().getBoolean("calculate-genes"), result.getParams().getBoolean("calculate-samples"));			
			addCentroidViews(node, mode, result, experiment, clusters);
			addTableViews(mode, node, result, experiment, idata, clusters);
			addInfoViewer(node, mode, clusters, result);
		} else if(mode.equals(NonparConstants.MODE_KRUSKAL_WALLIS)) {
			node = new DefaultMutableTreeNode("NonpaR [Kruskal-Wallis Test]");
			addExpressionImages(node, mode, result, clusters, experiment);
			if(result.getParams().getBoolean("hcl-execution", false))
				addHierarchicalTrees(result, mode, node, result.getCluster("hcl-clusters"), experiment, 
						result.getParams().getBoolean("calculate-genes"), result.getParams().getBoolean("calculate-samples"));			
			addCentroidViews(node, mode, result, experiment, clusters);
			addTableViews(mode, node, result, experiment, idata, clusters);
			addInfoViewer(node, mode, clusters, result);
		} else if(mode.equals(NonparConstants.MODE_MACK_SKILLINGS)) {
			node = new DefaultMutableTreeNode("NonpaR [Mack-Skilligs Test]");
			addExpressionImages(node, mode, result, clusters, experiment);
			if(result.getParams().getBoolean("hcl-execution", false))
				addHierarchicalTrees(result, mode, node, result.getCluster("hcl-clusters"), experiment, 
						result.getParams().getBoolean("calculate-genes"), result.getParams().getBoolean("calculate-samples"));	
			
			addCentroidViews(node, mode, result, experiment, clusters);
			addTableViews(mode, node, result, experiment, idata, clusters);
			addInfoViewer(node, mode, clusters, result);
		} else if(mode.equals(NonparConstants.MODE_FISHER_EXACT)) {
			node = new DefaultMutableTreeNode("NonpaR [Fisher Exact Test]");
			addExpressionImages(node, mode, result, clusters, experiment);
			if(result.getParams().getBoolean("hcl-execution", false))
				addHierarchicalTrees(result, mode, node, result.getCluster("hcl-clusters"), experiment, 
						result.getParams().getBoolean("calculate-genes"), result.getParams().getBoolean("calculate-samples"));	
			
			addCentroidViews(node, mode, result, experiment, clusters);
			addTableViews(mode, node, result, experiment, idata, clusters);
			addInfoViewer(node, mode, clusters, result);			
		}
		
		return node;
	}
	
	
	private void addExpressionImages(DefaultMutableTreeNode root, String mode, AlgorithmData result, int [][] clusters, Experiment experiment) {

		NonparExperimentViewer viewer;	
		
		DefaultMutableTreeNode node = new DefaultMutableTreeNode("Expression Images");
			       
		FloatMatrix resultMatrix = result.getMatrix("result-matrix");
           
		String [] methodArray = result.getStringArray("method-array");        	

		String [][] auxData;
    	String [] auxTitles = constructAuxTitles(result, mode);
    	
		
		if(mode.equals(NonparConstants.MODE_WILCOXON_MANN_WHITNEY)) {
			
	    	auxData = createAuxDataMatrix(resultMatrix, methodArray);

	    	viewer = new NonparExperimentViewer(experiment, clusters, auxTitles, auxData);
	    	
			DefaultMutableTreeNode viewerNode = new DefaultMutableTreeNode(new LeafInfo("Significant Genes", viewer, new Integer(0)));
			node.add(viewerNode);			
			viewerNode = new DefaultMutableTreeNode(new LeafInfo("Non-significant Genes", viewer, new Integer(1)));
			node.add(viewerNode);						
		
		} else if(mode.equals(NonparConstants.MODE_KRUSKAL_WALLIS)) {
			
      	
	    	auxData = createAuxDataMatrix(resultMatrix, methodArray);

	    	viewer = new NonparExperimentViewer(experiment, clusters, auxTitles, auxData);
	    	
			DefaultMutableTreeNode viewerNode = new DefaultMutableTreeNode(new LeafInfo("Significant Genes", viewer, new Integer(0)));
			node.add(viewerNode);			
			viewerNode = new DefaultMutableTreeNode(new LeafInfo("Non-significant Genes", viewer, new Integer(1)));
			node.add(viewerNode);						
		} else if(mode.equals(NonparConstants.MODE_MACK_SKILLINGS)) {

			String [] designArray = result.getStringArray("design-array");

			auxData = createAuxDataMatrix(resultMatrix, designArray, methodArray);

	    	viewer = new NonparExperimentViewer(experiment, clusters, auxTitles, auxData);

			String factorAName = result.getParams().getString("factor-A-name");
			String factorBName = result.getParams().getString("factor-B-name");			

	    	DefaultMutableTreeNode viewerNode = new DefaultMutableTreeNode(new LeafInfo(factorAName+" Significant Genes", viewer, new Integer(0)));
			node.add(viewerNode);
			viewerNode = new DefaultMutableTreeNode(new LeafInfo(factorAName+" Non-significant Genes", viewer, new Integer(1)));
			node.add(viewerNode);
			viewerNode = new DefaultMutableTreeNode(new LeafInfo(factorBName+" Significant Genes", viewer, new Integer(2)));
			node.add(viewerNode);
			viewerNode = new DefaultMutableTreeNode(new LeafInfo(factorBName+" Non-significant Genes", viewer, new Integer(3)));
			node.add(viewerNode);
			viewerNode = new DefaultMutableTreeNode(new LeafInfo("Incomplete Design (untested)", viewer, new Integer(4)));
			node.add(viewerNode);
		} else if(mode.equals(NonparConstants.MODE_FISHER_EXACT)) {			  	
		    	auxData = createAuxDataMatrix(resultMatrix);
		    	viewer = new NonparExperimentViewer(experiment, clusters, auxTitles, auxData);		    	
				DefaultMutableTreeNode viewerNode = new DefaultMutableTreeNode(new LeafInfo("Significant Genes", viewer, new Integer(0)));
				node.add(viewerNode);			
				viewerNode = new DefaultMutableTreeNode(new LeafInfo("Significant Genes (left tail lower)", viewer, new Integer(1)));
				node.add(viewerNode);						
				viewerNode = new DefaultMutableTreeNode(new LeafInfo("Significant Genes (right tail lower)", viewer, new Integer(2)));
				node.add(viewerNode);						
				viewerNode = new DefaultMutableTreeNode(new LeafInfo("Non-significant Genes", viewer, new Integer(3)));
				node.add(viewerNode);										
		}
		
		root.add(node);
	}


	private String [] constructAuxTitles(AlgorithmData result, String mode) {

		String [] auxTitles = null;
		String [] groupNames = result.getStringArray("group-names"); 
		boolean useAlpha;
		
		if(mode.equals(NonparConstants.MODE_WILCOXON_MANN_WHITNEY)) {
			useAlpha = result.getParams().getBoolean("use-alpha-criterion");
			
			if(useAlpha) {
				auxTitles = new String[6];
			} else {
				auxTitles = new String[7];				
			}
			
			auxTitles[0] = "Method";
			auxTitles[1] = "n ("+groupNames[0]+")";
			auxTitles[2] = "n ("+groupNames[1]+")";
			auxTitles[3] = "W";
			auxTitles[4] = "W*";
			auxTitles[5] = "p-value";
			if(!useAlpha)
				auxTitles[6] = "adj. p (Benjamini/Hochberg)";
		} else if(mode.equals(NonparConstants.MODE_KRUSKAL_WALLIS)) {
			useAlpha = result.getParams().getBoolean("use-alpha-criterion");
			
			if(useAlpha)			
				auxTitles = new String[4];
			else
				auxTitles = new String[5];
				
			auxTitles[0] = "Method";
			auxTitles[1] = "df";
			auxTitles[2] = "Kruskal-Wallis Statistic (H)";
			auxTitles[3] = "p-value";
			if(!useAlpha)
				auxTitles[4] = "adj. p (Benjamini/Hochberg)";
		} else if(mode.equals(NonparConstants.MODE_MACK_SKILLINGS)) {
			String factorAName = result.getParams().getString("factor-A-name");
			String factorBName = result.getParams().getString("factor-B-name");			
			auxTitles = new String[9];
			auxTitles[0] = "Design Descr.";
			auxTitles[1] = "Method";
			auxTitles[2] = "n vals";
			auxTitles[3] = "n "+factorAName+" Levels";
			auxTitles[4] = "MS Stat ("+factorAName+")";
			auxTitles[5] = "p-value ("+factorAName+")";
			auxTitles[6] = "n "+factorBName+" Levels";
			auxTitles[7] = "MS Stat ("+factorBName+")";
			auxTitles[8] = "p-value ("+factorBName+")";			
		} else if(mode.equals((NonparConstants.MODE_FISHER_EXACT))) {
			useAlpha = result.getParams().getBoolean("use-alpha-criterion");
			
			if(useAlpha)			
				auxTitles = new String[7];
			else
				auxTitles = new String[8];
			
			String [] binNames = result.getStringArray("fisher-exact-bin-names");
			
				
			auxTitles[0] = groupNames[0]+"/"+binNames[0];
			auxTitles[1] = groupNames[1]+"/"+binNames[0];
			auxTitles[2] = groupNames[0]+"/"+binNames[1];
			auxTitles[3] = groupNames[1]+"/"+binNames[1];
			
			auxTitles[4] = "Left Tail p-value";
			auxTitles[5] = "Right Tail p-value";
			auxTitles[6] = "2-Tail p-value";
						
			if(!useAlpha)
				auxTitles[7] = "adj. p (Benjamini/Hochberg)";
		}
		
		return auxTitles;		
	}
	
	
    /**
     * Adds nodes to display hierarchical trees.
     */
    private void addHierarchicalTrees(AlgorithmData result, String mode, DefaultMutableTreeNode root, Cluster result_cluster, Experiment experiment, boolean hclGenes, boolean hclSamples) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("Hierarchical Trees");
        NodeList nodeList= result_cluster.getNodeList();

        if(!mode.equals(NonparConstants.MODE_MACK_SKILLINGS)) {
        	node.add(new DefaultMutableTreeNode(new LeafInfo("Significant Genes", createHCLViewer(nodeList.getNode(0), experiment, hclGenes, hclSamples, null))));
        } else {
        	String factorAName = result.getParams().getString("factor-A-name");
			String factorBName = result.getParams().getString("factor-B-name");	
			
        	//mack skillings has two possible significant clusters factor A and B.
        	node.add(new DefaultMutableTreeNode(new LeafInfo(factorAName+" Significant Genes", createHCLViewer(nodeList.getNode(0), experiment, hclGenes, hclSamples, null))));
        	node.add(new DefaultMutableTreeNode(new LeafInfo(factorBName+" Significant Genes", createHCLViewer(nodeList.getNode(1), experiment, hclGenes, hclSamples, null))));        	
        }
    	root.add(node);
    }
    
    /**
     * Creates an <code>HCLViewer</code>.
     */
    private IViewer createHCLViewer(Node clusterNode, Experiment experiment, boolean genes, boolean samples, int [][] sampleClusters) {
        HCLTreeData genes_result = genes ? getResult(clusterNode, 0) : null;
        HCLTreeData samples_result = samples ? getResult(clusterNode, genes ? 4 : 0) : null;
 
        return new HCLViewer(experiment, clusterNode.getFeaturesIndexes(), genes_result, samples_result);
   }
    
    /**
     * Returns a hcl tree data from the specified cluster node.
     */
    private HCLTreeData getResult(Node clusterNode, int pos) {
        HCLTreeData data = new HCLTreeData();
        NodeValueList valueList = clusterNode.getValues();
        data.child_1_array = (int[])valueList.getNodeValue(pos).value;
        data.child_2_array = (int[])valueList.getNodeValue(pos+1).value;
        data.node_order = (int[])valueList.getNodeValue(pos+2).value;
        data.height = (float[])valueList.getNodeValue(pos+3).value;
        return data;
    }
    
    private void addCentroidViews(DefaultMutableTreeNode root, String mode, AlgorithmData result, Experiment experiment, int [][] clusters) {
        
    	float [][] means = result.getMatrix("cluster-means").A;
    	float [][] vars = result.getMatrix("cluster-variances").A;
    	
		FloatMatrix resultMatrix = result.getMatrix("result-matrix");
        
 	
       	String [] groupNames = result.getStringArray("group-names");    	
    	String [] auxTitles = constructAuxTitles(result, mode);    	    	
    	String [] methodArray = result.getStringArray("method-array");        

		String [][] auxData;
		
    	if(mode.equals(NonparConstants.MODE_MACK_SKILLINGS)) {
      		String [] designArray = result.getStringArray("design-array");
			auxData = createAuxDataMatrix(resultMatrix, designArray, methodArray);
      	} else if(mode.equals(NonparConstants.MODE_FISHER_EXACT)){
        	auxData = createAuxDataMatrix(resultMatrix);      		
      	} else {
        	auxData = createAuxDataMatrix(resultMatrix, methodArray);      		
      	}
    	
    	DefaultMutableTreeNode centroidNode = new DefaultMutableTreeNode("Centroid Graphs");
        DefaultMutableTreeNode expressionNode = new DefaultMutableTreeNode("Expression Graphs");
                
        NonparCentroidViewer centroidViewer = new NonparCentroidViewer(experiment, clusters, auxTitles, auxData);
        centroidViewer.setMeans(means);
        centroidViewer.setVariances(vars);

        String clusterLabel = "";
        
        if(mode.equals(NonparConstants.MODE_FISHER_EXACT)) {

    		centroidNode.add(new DefaultMutableTreeNode(new LeafInfo("Significant Genes", centroidViewer, new CentroidUserObject(0, CentroidUserObject.VARIANCES_MODE))));
    		centroidNode.add(new DefaultMutableTreeNode(new LeafInfo("Significant Genes (left tail lower)", centroidViewer, new CentroidUserObject(1, CentroidUserObject.VARIANCES_MODE))));
    		centroidNode.add(new DefaultMutableTreeNode(new LeafInfo("Significant Genes (right tail lower)", centroidViewer, new CentroidUserObject(2, CentroidUserObject.VARIANCES_MODE))));
    		centroidNode.add(new DefaultMutableTreeNode(new LeafInfo("Non-significant Genes", centroidViewer, new CentroidUserObject(3, CentroidUserObject.VARIANCES_MODE))));

    		expressionNode.add(new DefaultMutableTreeNode(new LeafInfo("Significant Genes", centroidViewer, new CentroidUserObject(0, CentroidUserObject.VALUES_MODE))));            
      		expressionNode.add(new DefaultMutableTreeNode(new LeafInfo("Significant Genes (left tail lower)", centroidViewer, new CentroidUserObject(1, CentroidUserObject.VALUES_MODE))));            
      		expressionNode.add(new DefaultMutableTreeNode(new LeafInfo("Significant Genes (right tail lower)", centroidViewer, new CentroidUserObject(2, CentroidUserObject.VALUES_MODE))));            
      		expressionNode.add(new DefaultMutableTreeNode(new LeafInfo("Non-significant Genes", centroidViewer, new CentroidUserObject(3, CentroidUserObject.VALUES_MODE))));            

        	
        } else if(!mode.equals(NonparConstants.MODE_MACK_SKILLINGS)) {
        	for (int i=0; i<clusters.length; i++) {            
        		if (i == 0) {
        			clusterLabel = "Significant Genes ";
        		} else if (i == 1) {
        			clusterLabel = "Non-significant Genes ";                
        		}
        		centroidNode.add(new DefaultMutableTreeNode(new LeafInfo(clusterLabel, centroidViewer, new CentroidUserObject(i, CentroidUserObject.VARIANCES_MODE))));
        		expressionNode.add(new DefaultMutableTreeNode(new LeafInfo(clusterLabel, centroidViewer, new CentroidUserObject(i, CentroidUserObject.VALUES_MODE))));            
        	}
        } else {
        	
        	String factorAName = result.getParams().getString("factor-A-name");
			String factorBName = result.getParams().getString("factor-B-name");	
        	
            for (int i=0; i<clusters.length; i++) {            
                if (i == 0) {
                	clusterLabel = factorAName+" Significant Genes ";
                } else if (i == 1) {
                    clusterLabel = factorAName+" Non-significant Genes ";                
                } else if (i == 2) {
                	clusterLabel = factorBName+" Significant Genes ";
                } else if (i == 3) {
                    clusterLabel = factorBName+" Non-significant Genes ";                
                } else if (i == 4) {
                    clusterLabel = "Incomplete Design (untested)";                
                }
                centroidNode.add(new DefaultMutableTreeNode(new LeafInfo(clusterLabel, centroidViewer, new CentroidUserObject(i, CentroidUserObject.VARIANCES_MODE))));
                expressionNode.add(new DefaultMutableTreeNode(new LeafInfo(clusterLabel, centroidViewer, new CentroidUserObject(i, CentroidUserObject.VALUES_MODE))));            
            }        	
        }
        
        NonparCentroidsViewer centroidsViewer = new NonparCentroidsViewer(experiment, clusters, auxTitles, auxData);
        centroidsViewer.setMeans(means);
        centroidsViewer.setVariances(vars);
        
        centroidNode.add(new DefaultMutableTreeNode(new LeafInfo("All Genes", centroidsViewer, new Integer(CentroidUserObject.VARIANCES_MODE))));
        expressionNode.add(new DefaultMutableTreeNode(new LeafInfo("All Genes", centroidsViewer, new Integer(CentroidUserObject.VALUES_MODE))));
        
        root.add(centroidNode);
        root.add(expressionNode);
    }
	;
	
    private void addTableViews(String mode, DefaultMutableTreeNode root, AlgorithmData result, Experiment experiment, IData data, int [][] clusters) {
    	DefaultMutableTreeNode node = new DefaultMutableTreeNode("Table Views");
    	
    	FloatMatrix resultMatrix = result.getMatrix("result-matrix");
    	
    	String [] auxTitles = constructAuxTitles(result, mode);    	  	
      	String [] methodArray = result.getStringArray("method-array");
    	String [][] auxData;
    	      	
      	if(mode.equals(NonparConstants.MODE_MACK_SKILLINGS)) {
      		String [] designArray = result.getStringArray("design-array");
			auxData = createAuxDataMatrix(resultMatrix, designArray, methodArray);
      	} else if(mode.equals(NonparConstants.MODE_FISHER_EXACT)) {
      		auxData = createAuxDataMatrix(resultMatrix);
      	} else {
        	auxData = createAuxDataMatrix(resultMatrix, methodArray);      		
      	}
    	
    	IViewer tabViewer = new ClusterTableViewer(experiment, clusters, data, auxTitles, auxData);
    	
    	if(mode.equals(NonparConstants.MODE_FISHER_EXACT)) {
    		node.add(new DefaultMutableTreeNode(new LeafInfo("Significant Genes", tabViewer, new Integer(0))));
    		node.add(new DefaultMutableTreeNode(new LeafInfo("Significant Genes (left tail lower)", tabViewer, new Integer(1))));
    		node.add(new DefaultMutableTreeNode(new LeafInfo("Significant Genes (right tail lower)", tabViewer, new Integer(2))));
    		node.add(new DefaultMutableTreeNode(new LeafInfo("Non-significant Genes", tabViewer, new Integer(3))));    		
    	
    		//Leka asked for a full table of all genes pushed through Fisher Exact
    		int [][] allIndices = new int[1][auxData.length];
    		for(int i = 0; i < allIndices[0].length; i++)
    			allIndices[0][i] = i;
    		
    		//add one more
    		node.add(new DefaultMutableTreeNode(new LeafInfo("All Genes", new ClusterTableViewer(experiment, allIndices, data, auxTitles, auxData), new Integer(0))));     		
    		
    	} else if(!mode.equals(NonparConstants.MODE_MACK_SKILLINGS)) {
    		for (int i=0; i<clusters.length; i++) {
    			if (i < clusters.length - 1) {
    				node.add(new DefaultMutableTreeNode(new LeafInfo("Significant Genes ", tabViewer, new Integer(i))));
    			} else if (i == clusters.length - 1) {
    				node.add(new DefaultMutableTreeNode(new LeafInfo("Non-significant Genes ", tabViewer, new Integer(i))));    			
    			}
    		} 
    	} else {
    		    		
    		String clusterLabel = "";
    		
    		String factorAName = result.getParams().getString("factor-A-name");
    		String factorBName = result.getParams().getString("factor-B-name");	
    		
    		for (int i=0; i<clusters.length; i++) {            
    			if (i == 0) {
    				clusterLabel = factorAName+" Significant Genes ";
    			} else if (i == 1) {
    				clusterLabel = factorAName+" Non-significant Genes ";                
    			} else if (i == 2) {
    				clusterLabel = factorBName+" Significant Genes ";
    			} else if (i == 3) {
    				clusterLabel = factorBName+" Non-significant Genes ";                
    			} else if (i == 4) {
    				clusterLabel = "Incomplete Design (untested)";                
    			}

    			node.add(new DefaultMutableTreeNode(new LeafInfo(clusterLabel, tabViewer, new Integer(i))));
    		}
    		
    	}
    	root.add(node);
    }
    
    
	private void addInfoViewer(DefaultMutableTreeNode root, String mode, int [][] clusters, AlgorithmData result) {
				
		boolean useAlpha = result.getParams().getBoolean("use-alpha-criterion", true);
		
		float sigLevel;
		float estFDR = Float.NaN;
		
		if(useAlpha)
			sigLevel = result.getParams().getFloat("alpha");
		else {
			sigLevel = result.getParams().getFloat("fdr");
			estFDR = result.getParams().getFloat("estimated-fdr");
		}
		
		NonparInfoViewer viewer = null;
		String modeStr = null;
		
		if(mode.equals(NonparConstants.MODE_FISHER_EXACT)) {
			modeStr = "Fisher Exact Test";
			String [] groupNames = result.getStringArray("group-names");
			int [] groupAssign = result.getIntArray("group-assignments");
			int [] numPerGroup = getGroupNs(groupNames.length, groupAssign);	
			String [] binNames = result.getStringArray("fisher-exact-bin-names"); 
			float dataBinCutoff = result.getParams().getFloat("fisher-exact-bin-cutoff");
			
			viewer = new NonparInfoViewer(clusters, modeStr, 
					useAlpha, sigLevel, estFDR, groupNames, numPerGroup, binNames, dataBinCutoff);
		
		} else if(mode.equals(NonparConstants.MODE_MACK_SKILLINGS)) {
			
        	String factorAName = result.getParams().getString("factor-A-name");
			String factorBName = result.getParams().getString("factor-B-name");	
			String [] factorANames = result.getStringArray("factor-A-level-names");
			String [] factorBNames = result.getStringArray("factor-B-level-names");

			modeStr = "Mack-Skillings Test";
			viewer = new NonparInfoViewer(clusters, modeStr, 
					useAlpha, sigLevel, factorAName, factorBName, factorANames, factorBNames);
			
		} else {			
			String [] groupNames = result.getStringArray("group-names");
			int [] groupAssign = result.getIntArray("group-assignments");		
			int [] numPerGroup = getGroupNs(groupNames.length, groupAssign);
			
			if(mode.equals(NonparConstants.MODE_WILCOXON_MANN_WHITNEY)) {				
				modeStr = "Wilcoxon Rank Sum Test";
			} else if(mode.equals(NonparConstants.MODE_KRUSKAL_WALLIS)){
				modeStr = "Kruskal-Wallis Test";
			} 
			viewer = new NonparInfoViewer(clusters, modeStr, 
					useAlpha, sigLevel, estFDR, groupNames, numPerGroup);
		}
		

		DefaultMutableTreeNode infoNode = new DefaultMutableTreeNode("Cluster Information");
		infoNode.add(new DefaultMutableTreeNode(new LeafInfo("Result Overview", viewer)));
		root.add(infoNode);	
	}
    
	
	private int [] getGroupNs(int numGroups, int [] assignments) {
		int [] groupNs = new int [numGroups];
		
		for(int i = 0; i < assignments.length; i++) {
			
			//-1 signals excluded, group indices start at 1
			if(assignments[i] != -1)
				groupNs[assignments[i]]++;
			else
				groupNs[groupNs.length-1]++;
		}		
		return groupNs;
	}
	
	
    /**
     * The class to listen to progress, monitor and algorithms events.
     */
    private class Listener extends DialogListener implements AlgorithmListener {
        
        public void valueChanged(AlgorithmEvent event) {
            switch (event.getId()) {
                case AlgorithmEvent.SET_UNITS:
                    progress.setUnits(event.getIntValue());
                    progress.setDescription(event.getDescription());
                    break;
                case AlgorithmEvent.PROGRESS_VALUE:
                    progress.setValue(event.getIntValue());
                    progress.setDescription(event.getDescription());
                    break;
            }
        }
        
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            if (command.equals("cancel-command")) {
                nonpar.abort();
                progress.dispose();
            }
        }
        
        public void windowClosing(WindowEvent e) {
            nonpar.abort();
            progress.dispose();
        }
    }
    
	

    
	
	public static void main(String [] args) {
		NonparGUI gui = new NonparGUI();
		try {
		gui.execute(null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}
	

}
