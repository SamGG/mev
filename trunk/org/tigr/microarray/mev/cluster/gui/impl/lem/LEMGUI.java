/*
Copyright @ 1999-2006, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
package org.tigr.microarray.mev.cluster.gui.impl.lem;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.NoSuchElementException;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;

import org.tigr.microarray.mev.TMEV;
import org.tigr.microarray.mev.cluster.algorithm.Algorithm;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmEvent;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmException;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmListener;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IClusterGUI;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.LeafInfo;
import org.tigr.microarray.mev.cluster.gui.helpers.ClusterTableViewer;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.HTMLMessageFileChooser;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.Logger;
import org.tigr.microarray.mev.file.StringSplitter;
import org.tigr.util.FloatMatrix;

/**
 * @author braisted
 * 
 * Handles control of the LEM algorithm operation and result construction
 * LEM, Linear Expression Maps, constructs viewers to visualize expression
 * data presented ordered by chromosomal location.  Chromosome ids and gene
 * coordinates can be supplied in the annotation file or in a coordinages
 * file.  For multiple chromosomes or plasmids, one viewer is constructed for
 * each chromosome.
 */
public class LEMGUI implements IClusterGUI { //IScriptGUI
	
	private Algorithm algorithm;
	private Logger logger;
	private boolean stop = false;
	private String singleChrName = "Single Chromosome";
	private IFramework framework;
	
	/**
	 *	Gathers parameters, kicks off algorithm, builds output nodes and viewers
	 */
	public DefaultMutableTreeNode execute(IFramework framework) throws AlgorithmException {
		this.framework = framework;
		IData idata = framework.getData();
		Experiment experiment = idata.getExperiment();
		FloatMatrix matrix = experiment.getMatrix();
		int [] origIndices = experiment.getRowMappingArrayCopy();
		DefaultMutableTreeNode algNode = new DefaultMutableTreeNode("LEM");

		//LEM dialog
		LEMInitDialog dialog = new LEMInitDialog(framework.getFrame(), framework.getData().getFieldNames());		

		if(dialog.showModal() != JOptionPane.OK_OPTION)			
			return null; //if anything but OK, return null
		
		//log events
		Listener listener = new Listener();
		logger = new Logger(framework.getFrame(), "LEM Processing", listener);
		
		//Algorithm data to capture parameters
		AlgorithmData data = new AlgorithmData();
		
		String locusField = dialog.getLocusField();
		String startField = "5' End";
		String endField = "3' End";		
		String chrField = null;
		String fileName = "None";
		
		boolean hasMultipleChr = dialog.hasMultipleChr();
		
		logger.show();
		logger.append("LEM Processing\n");
		logger.append("Retrieving Locus List\n");
		
		String [] locusList = idata.getAnnotationList(locusField, origIndices);

		String [] chrList;
		String [] startList;
		String [] endList;
		int [] startArray;
		int [] endArray;
		int [] origIndexArray;
		boolean [] mappedSpots = new boolean[origIndices.length];

		int locusInfoCount = 0;
		int incompleteCount = 0;
		int [] chrLocusCount;
		int globalIncompleteCount = 0;
		int totalNumberOfMappedSpots = 0;

		//accumulate result nodes based on chr name, iff hasMultipleChr
		Hashtable lemResultHash = new Hashtable();

		if(hasMultipleChr) {
			chrField = dialog.getChrIDField();
		}
		
		//get coord information from file if that is the selected mode
		if(dialog.useFileInput()) {
			
			String [] info = null;
			
			locusField = "Locus ID";
			
			String msg = "<html><body><h1><center>Select Chromosomal Location File</center></h1><hr size = 3>";
			msg += "Please select a file to supply information containing chromosomal coordinates for your array.";
			msg += "<br><br>The file format should be tab delimited text with:<br><br><center><b> gene_id | [chromosome ID] | 5'-end | 3'-end</b></center>";
			msg += "</body></html>";
			
			HTMLMessageFileChooser chooser = new HTMLMessageFileChooser(framework.getFrame(), "Select Chromosomal Location File",msg,TMEV.getDataPath(),true);
			if(chooser.showModal() == JOptionPane.OK_OPTION) {
				
				Hashtable hash = new Hashtable();
				logger.append("Reading Coordinate File: "+chooser.getSelectedFile().getName()+"\n");
				fileName = chooser.getSelectedFile().getName();
				
				//parse coord file
				try {
					BufferedReader bfr = new BufferedReader(new FileReader(chooser.getSelectedFile()));				
					
					String line;
					StringSplitter ss = new StringSplitter('\t');
					int cnt = 0;
					int tokenCount;					
										
					String locus;
					
					while( (line = bfr.readLine()) != null) {
						ss.init(line);						
						locus = ss.nextToken();
						
						if(!hasMultipleChr) {
							info = new String[3];
							info[0] = singleChrName;
							info[1] = ss.nextToken();  //5'
							info[2] = ss.nextToken();  //3'
						} else {
							info = new String[3];
							info[0] = ss.nextToken();  //chr
							info[1] = ss.nextToken();  //5'
							info[2] = ss.nextToken();  //3'						
						}
						//populate hash
						hash.put(locus, info);										
					}								
				} catch (FileNotFoundException ioe) {
					JOptionPane.showMessageDialog(framework.getFrame(),"File Not Found Error using input file: "+fileName, "Error", JOptionPane.ERROR_MESSAGE);
					return null;
				} catch (IOException fnfe) {
					JOptionPane.showMessageDialog(framework.getFrame(),"I/O Error using input file: "+fileName, "Error", JOptionPane.ERROR_MESSAGE);
					return null;					
				} catch (NoSuchElementException nsee) {
					JOptionPane.showMessageDialog(framework.getFrame(),"File Parsing Error: "+fileName, "Error", JOptionPane.ERROR_MESSAGE);
					return null;
				}
									
				//create lists
				startList = new String[locusList.length];
				endList = new String[locusList.length];
				chrList = new String[locusList.length];
				
				for(int i = 0; i < locusList.length; i++) {
					info = (String [])(hash.get(locusList[i]));
					if(info != null) {
						chrList[i] = info[0];
						startList[i] = info[1];
						endList[i] = info[2];		
					} else {
						chrList[i] = "";
						startList[i] = "";
						endList[i] = "";
					}
				}
				
			} else {  //no file selected
				return null;
			}
			
		} else { 			
			//Not a file input, get info from annotation, not file
			chrField = dialog.getChrIDField();
			startField = dialog.getStartField();
			endField = dialog.getEndField();			
			chrList = idata.getAnnotationList(chrField, origIndices);
			startList = idata.getAnnotationList(startField, origIndices);
			endList = idata.getAnnotationList(endField, origIndices);
		}
		
		//reset origIndices to be a default ordering of loci names
		//to point to FloatMatrix rows from the current Experiment
		for(int i = 0; i < origIndices.length; i++)
			origIndices[i] = i;
		
		Vector [] chrData;

		//Contains multiple Vectors related to particular chrs
		Hashtable chrHash = new Hashtable();	
		//contains chr names in order of occurance
		Vector chrNames = new Vector();
		//array of chr names that will be sorted by name for output organization
		String [] chrKeys = new String[1];
		
		if(hasMultipleChr) {			
			for(int i = 0; i < chrList.length; i++) {			
				
				//skip values missing chr == ""				
				if(chrList[i].equals(""))
					continue;
				
				if(chrHash.containsKey(chrList[i])) {
					
					chrData = (Vector [])chrHash.get(chrList[i]);
					chrData[0].add(locusList[i]);
					chrData[1].add(startList[i]);
					chrData[2].add(endList[i]);
					
					
					//chrData[3].add(new Integer(origIndices[i]));
					chrData[3].add(new Integer(origIndices[i]));
					
				} else {
					chrNames.add(chrList[i]);
					
					chrData = new Vector[4];
					chrData[0] = new Vector();
					chrData[1] = new Vector();
					chrData[2] = new Vector();
					chrData[3] = new Vector();
					
					chrData[0].add(locusList[i]);
					chrData[1].add(startList[i]);
					chrData[2].add(endList[i]);
					
					
					chrData[3].add(new Integer(origIndices[i]));
					
					chrHash.put(chrList[i], chrData);
				}				
			}			
		} else {
			String singleChrName = "Chromosome";
			chrNames.add(singleChrName);
			chrKeys[0] = singleChrName;
			
			chrData = new Vector[4];
			chrData[0] = new Vector();
			chrData[1] = new Vector();
			chrData[2] = new Vector();
			chrData[3] = new Vector();
			
			for(int i = 0; i < locusList.length; i++) {
				chrData[0].add(locusList[i]);
				chrData[1].add(startList[i]);
				chrData[2].add(endList[i]);					
				chrData[3].add(new Integer(origIndices[i]));
			}
			
			chrHash.put(singleChrName, chrData);				
		}
		
		//catch incomplete information
		if(startList.length != endList.length) {
			JOptionPane.showMessageDialog(framework.getFrame(), "Coordinate information is incomplete.  Some coordinates are not paired (one 3' per 5')", "Coordinate Information Error", JOptionPane.ERROR_MESSAGE);
			return null;
		}
		
		startArray = new int[startList.length];
		endArray = new int[endList.length];
		chrLocusCount = new int[chrNames.size()];
		
		String chrName;
		int locusCount;
		String [] chrLocusList;
		int [] mappingData = new int[chrNames.size()];
		int [] locusCounts = new int[chrNames.size()];
		Hashtable mappingDataTable = new Hashtable();
		Hashtable locusCountsTable = new Hashtable();
		
		for(int chr = 0; chr < chrNames.size(); chr++) {
			//get chr name				
			chrName = (String)chrNames.get(chr);
			
			if(chrName.equals(""))
				continue;
			
			//get associated vectors
			chrData = (Vector [])chrHash.get(chrName);
			
			//original
			locusCount = chrData[0].size();

			//initialize arrays
			chrLocusList = new String[chrData[0].size()];
			startArray = new int[chrData[1].size()];
			endArray = new int[chrData[2].size()];
			origIndexArray = new int[locusCount];
			
			String locus, start, end;
			int origIndex;
			incompleteCount = 0;
			
			//build arrays
			for(int locusIndex = 0; locusIndex < locusCount; locusIndex++) {
				locus = (String)chrData[0].get(locusIndex);
				start = (String)chrData[1].get(locusIndex);
				end = (String)chrData[2].get(locusIndex);
				origIndex = ((Integer)chrData[3].get(locusIndex)).intValue();
				
				chrLocusList[locusIndex] = locus;

				if(!locus.equals("") && !start.equals("") && !end.equals("")) {	
					startArray[locusIndex] = Integer.parseInt(start);
					endArray[locusIndex] = Integer.parseInt(end);
					origIndexArray[locusIndex] = origIndex;
				} else {
					//flag missing data
					startArray[locusIndex] = -1;
					endArray[locusIndex] = -1;
					origIndexArray[locusIndex] = origIndex;
					globalIncompleteCount++;
				}
			}
			
			//build locus list, startArray, and endArray;
			data.addIntArray("idata-indices", origIndices);
			data.addIntArray("original-indices", origIndexArray);			
			data.addMatrix("expression-matrix", matrix);									
			data.addStringArray("locus-array", chrLocusList);			
			data.addIntArray("start-array", startArray);			
			data.addIntArray("end-array", endArray);
			
			//execute LEM on chromosome
			logger.append("Start LEM Construction Operations (LEM.java)\n");
			logger.append("Working on Chromosome: "+chrName+"\n");
			algorithm = framework.getAlgorithmFactory().getAlgorithm("LEM");
			algorithm.addAlgorithmListener(listener);
			data = algorithm.execute(data);
			
			logger.append("Constructing LEM Viewer\n");
			
			LinearExpressionMapViewer viewer = createViewer(data, experiment, locusField);
			LinearExpressionGraphViewer graphViewer = createGraphViewer(data, experiment, locusField, chrName);
			//build node
			DefaultMutableTreeNode node, legNode, chrNode;
			
			if(!hasMultipleChr) {
				node = new DefaultMutableTreeNode(new LeafInfo("LEM Viewer", viewer));			
				legNode = new DefaultMutableTreeNode(new LeafInfo("LEG Viewer", graphViewer));							
				algNode.add(node);
				algNode.add(legNode);
				
				lemResultHash.put(chrName, algNode);								
			} else {				
				chrNode = new DefaultMutableTreeNode(new LeafInfo(chrName));				
				node = new DefaultMutableTreeNode(new LeafInfo("LEM Viewer - "+ chrName, viewer));			
				chrNode.add(node);
				legNode = new DefaultMutableTreeNode(new LeafInfo("LEG Viewer - "+ chrName, graphViewer));											
				chrNode.add(legNode);

				lemResultHash.put(chrName, chrNode);				
			}
			
			//get number of mapped spots, also indicate mapped spots
			int [][] replicateArrays = data.getIntMatrix("replication-indices-matrix");
			int totalMapped = 0;
			for(int i = 0; i < replicateArrays.length; i++) {
				totalMapped += replicateArrays[i].length;
				for(int j = 0; j < replicateArrays[i].length; j++)
					mappedSpots[replicateArrays[i][j]] = true;
			}
			
			locusCountsTable.put(chrName, new Integer(replicateArrays.length));
			locusCounts[chr] = replicateArrays.length;
			
			mappingDataTable.put(chrName, new Integer(totalMapped));
			mappingData[chr] = totalMapped;
			
			totalNumberOfMappedSpots += totalMapped;

		}
		
		//sort result nodes if have multiple chr
		if(hasMultipleChr) {
			chrKeys = new String[lemResultHash.size()];

			Enumeration e = lemResultHash.keys();
			int cnt = 0;
			while(e.hasMoreElements()) {
				chrKeys[cnt] = (String)(e.nextElement());
				cnt++;
			}
			
			//sort on chr names
			Arrays.sort(chrKeys);
			
			for(int i = 0; i < chrKeys.length; i++) {
				//add in sorted order, by name
				algNode.add((DefaultMutableTreeNode)(lemResultHash.get(chrKeys[i])));
				locusCounts[i] = ((Integer)(locusCountsTable.get(chrKeys[i]))).intValue();
				mappingData[i] = ((Integer)(mappingDataTable.get(chrKeys[i]))).intValue();
			}			
		}
				
		DefaultMutableTreeNode unMappedTableNode = createTableOfUnmappedSpots(experiment, idata, origIndices, mappedSpots);
		
		if(unMappedTableNode != null)
			algNode.add(unMappedTableNode);
			
		DefaultMutableTreeNode summaryNode = createSummaryNode(locusField, startField, endField, hasMultipleChr, (hasMultipleChr ? chrField : null), dialog.useFileInput(), fileName, 
				framework.getData().getFullExperiment().getNumberOfGenes(), experiment.getNumberOfGenes(), totalNumberOfMappedSpots, chrKeys, mappingData, locusCounts);
		algNode.add(summaryNode);
				
		logger.dispose();
		return algNode;		
	}
	
	/**
	 * Builds the expression map viewer
	 * @param data parameters and input data
	 * @param experiment Experiment object
	 * @param locusFieldName locus id field name (annotation label)
	 * @return returns the LinearExpressionMapViewer
	 */
	private LinearExpressionMapViewer createViewer(AlgorithmData data, Experiment experiment, String locusFieldName) {
		
		String [] sortedLociNames = data.getStringArray("sorted-loci-names");
		int [][] replicationMatrix = data.getIntMatrix("replication-indices-matrix");
		
		//Matrix and indices can be used to build a new Experiment for the viewer
		FloatMatrix condensedMatrix = data.getMatrix("condensed-matrix");
		int [] sortedIDataIndices = data.getIntArray("sorted-idata-indices");
		
		Experiment newExperiment = new Experiment(condensedMatrix, experiment.getColumnIndicesCopy(), sortedIDataIndices);

		//sorted start and end points (min and max coord regardless of direction)
		int [] sortedStartCoordinates = data.getIntArray("sorted-start");
		int [] sortedEndCoordinates = data.getIntArray("sorted-end");
		
		//direction indicator, 1 == forward, -1 == back
		int [] directionIntArray = data.getIntArray("direction-array");
		
		//convert to boolean array			
		boolean [] isForward = new boolean[directionIntArray.length];
		for(int i = 0; i < directionIntArray.length;i++) {
			isForward[i] = (directionIntArray[i] == 1);
		}
		
		//offset for overlaps
		int [] strata = data.getIntArray("strata-array");
				
		LinearExpressionMapViewer viewer = new LinearExpressionMapViewer(experiment,
				newExperiment, sortedLociNames, sortedStartCoordinates, sortedEndCoordinates,
				replicationMatrix, isForward, strata, "Chromosome", locusFieldName);
		
		return viewer;
	}
	
	/**
	 * Creates the graph viewer
	 * @param data Parameters and input data
	 * @param experiment Experiment object
	 * @param locusFieldName locus field name
	 * @param chrName character field name
	 * @return returns a LinearExpressionGraphViewer
	 */
	private LinearExpressionGraphViewer createGraphViewer(AlgorithmData data, Experiment experiment, String locusFieldName, String chrName) {
		
		String [] sortedLociNames = data.getStringArray("sorted-loci-names");
		int [][] replicationMatrix = data.getIntMatrix("replication-indices-matrix");
		
		//Matrix and indices can be used to build a new Experiment for the viewer
		FloatMatrix condensedMatrix = data.getMatrix("condensed-matrix");
		int [] sortedIDataIndices = data.getIntArray("sorted-idata-indices");
	
		Experiment newExperiment = new Experiment(condensedMatrix, experiment.getColumnIndicesCopy(), sortedIDataIndices);

		//sorted start and end points (min and max coord regardless of direction)
		int [] sortedStartCoordinates = data.getIntArray("sorted-start");
		int [] sortedEndCoordinates = data.getIntArray("sorted-end");
		
		//direction indicator, 1 == forward, -1 == back
		int [] directionIntArray = data.getIntArray("direction-array");
		
		//convert to boolean array			
		boolean [] isForward = new boolean[directionIntArray.length];
		for(int i = 0; i < directionIntArray.length;i++) {
			isForward[i] = (directionIntArray[i] == 1);
		}
		
		//offset for overlaps
		int [] strata = data.getIntArray("strata-array");
				
		LinearExpressionGraphViewer viewer = new LinearExpressionGraphViewer(framework.getData(), experiment,
				newExperiment, sortedLociNames, sortedStartCoordinates, sortedEndCoordinates,
				replicationMatrix, chrName, locusFieldName);
		
		return viewer;
	}
	
	/**
	 * Builds the summary node
	 * @param locusField locus id field name
	 * @param startField start coord. field name
	 * @param endField end coord. field name
	 * @param hasMultipleChr boolean for multiple coordinates
	 * @param chrField chromosome field name
	 * @param useFileInput boolean for coord. file input or not
	 * @param fileName file name
	 * @param totSpotCount total number of input rows
	 * @param lemSpotCount number of rows entering lem
	 * @param numberOfMappedSpots number of rows (spots) mapped to loc.
	 * @param chrNames list of chr names
	 * @param mappingCounts mapping count for each chr
	 * @param locusCounts locus counts for each chr
	 * @return returns a node containing the summary viewer <code>LEMInfoViewer</code>
	 */
	private DefaultMutableTreeNode createSummaryNode(String locusField, String startField, String endField, boolean hasMultipleChr, String chrField, boolean useFileInput, String fileName,		
			int totSpotCount, int lemSpotCount, int numberOfMappedSpots, String [] chrNames, int [] mappingCounts, int [] locusCounts) {
		
		LEMInfoViewer viewer = new LEMInfoViewer(locusField, startField, endField, hasMultipleChr, chrField, useFileInput, fileName,
				totSpotCount, lemSpotCount, numberOfMappedSpots, chrNames, mappingCounts, locusCounts);		
		
		return new DefaultMutableTreeNode(new LeafInfo("Locus Mapping Summary", viewer));		
	}
	
	/**
	 * Builds a table viewer of unmapped spots
	 * @param experiment Input Experiment
	 * @param data IData object
	 * @param origIndices mapping indices
	 * @param mappedSpots spots that are mapped
	 * @return returns a table of unmapped spots
	 */
	private DefaultMutableTreeNode createTableOfUnmappedSpots(Experiment experiment, IData data, int [] origIndices, boolean [] mappedSpots) {
		
		int count = 0;
		for(int i = 0; i < mappedSpots.length; i++) {
			if(!mappedSpots[i])
				count++;
		}

		if(count == 0)
			return null;
		
		int [] cluster = new int[count];
		count = 0;
		for(int i = 0; i < mappedSpots.length; i++) {
			if(!mappedSpots[i]) {
				cluster[count] = origIndices[i];
				count++;
			}				
		}
		
		int [][] clusters = new int[1][];
		clusters[0] = cluster;

		ClusterTableViewer viewer = new ClusterTableViewer(experiment, clusters, data);
			
		return new DefaultMutableTreeNode(new LeafInfo("Unmapped Spot Table", viewer, new Integer(0)));		

	}
	
    /** Listens to algorithm events and updates the logger.
     */
    private class Listener extends DialogListener implements AlgorithmListener{
        String eventDescription;
        /** Handles algorithm events.
         * @param actionEvent event object
         */
        public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
            String command = actionEvent.getActionCommand();
            if (command.equals("cancel-command")) {
                System.out.println("abort execution");
                stop = true;
                if(algorithm!= null)
                	algorithm.abort();
                logger.dispose();
            }
        }
        
        /** Invoked when an algorithm progress value was changed.
         *
         * @param event a <code>AlgorithmEvent</code> object.
         */
       public void valueChanged(AlgorithmEvent event) {
            if(event.getId() == AlgorithmEvent.MONITOR_VALUE){
                logger.append( event.getDescription() );
            } 
        }
       
    }
    
	
}
