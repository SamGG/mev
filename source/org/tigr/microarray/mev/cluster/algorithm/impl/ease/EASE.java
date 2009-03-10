/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: EASE.java,v $
 * $Revision: 1.11 $
 * $Date: 2007-12-06 19:45:07 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */

package org.tigr.microarray.mev.cluster.algorithm.impl.ease;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.tigr.microarray.mev.cluster.algorithm.*;
import org.tigr.util.FloatMatrix;
import org.tigr.util.QSort;

/** Manages EASE analysis and raw result manipulation and return.
 * @author braisted
 */

public class EASE extends AbstractAlgorithm {
    
    protected JEASEStatistics jstats;
    protected Vector<String> sampleVector;
    protected Vector<String> populationVector;

    protected String [] annotationFileList;
    protected String impliesFileLocation;
    protected String [][] result;
    protected String [][] hitList;
    protected String [] categoryNames;
    /** True if accession numbers are appended.
     */
    protected boolean haveAccessionNumbers = false;
    protected boolean reportEaseScore = false;
    protected AlgorithmEvent event;
    protected Vector<String> headerNames;
    protected DecimalFormat format;
    protected FloatMatrix expData;
    
    protected boolean stop = false;
    protected boolean performClusterAnalysis;
    protected boolean isRecursedEaseRun = false;
    
    String tagsFileLocation;    
    protected EaseElementList populationElementList;        
    protected static final String HAVE_ACCESSIONS_OPTION = "have-accession-numbers";        
    long start;
    
    /** Creates a new instance of ease (Default)
     */
    public EASE() {
    }
    
    public void abort() {
        stop = true;
    }
    

    /** Recieves parameters, executes algorithm and returns the result in the <CODE>AlgorithmData</CODE> object.
     * @param algorithmData Intput data and parameters
     * @throws AlgorithmException Reports errors or abort requests
     * @return Returns result in <CODE>AlgorithmData</CODE>
     */
    public AlgorithmData execute(AlgorithmData algData) throws AlgorithmException {
        EaseAlgorithmData algorithmData = (EaseAlgorithmData)algData;
        AlgorithmParameters params = algorithmData.getParams();
        performClusterAnalysis = params.getBoolean("perform-cluster-analysis", true);
        expData = algorithmData.getMatrix("expression");
        isRecursedEaseRun = params.getBoolean("is-recursed-run", false);
        
        
        if(performClusterAnalysis) {
        	if(!algorithmData.isRunNease()) {
        		return performClusterAnnotationAnalysis(algorithmData);
        } else {
        		EaseAlgorithmData temp = performClusterAnnotationAnalysis(algorithmData);
        		
        		return performNestedEaseAnalysis(temp);
        	}	
        } else {
            return performSlideAnnotationSurvey(algorithmData);
        }
    }
    
    /**
	 * Runs EASE on each set of previously-generated EASE results, if the
	 * p-value of that result is less than the Fisher's Cutoff. Attaches the
	 * additional EASE results (AlgorithmDatas) to the returned
	 * AlgorithmData object.
     * 
     * @param algorithmData
     * @return The AlgorithmData argument, with new data added. 
     * @throws AlgorithmException
	 */
	protected EaseAlgorithmData performNestedEaseAnalysis(EaseAlgorithmData algorithmData) throws AlgorithmException {
        int[] termlist;
        double fishersCutoff=5e-2;
        if(algorithmData == null)
        	return null;
		String[][] initialResultMatrix = algorithmData.getResultMatrix();

        //If initial EASE run had zero results
        if(initialResultMatrix == null || initialResultMatrix.length == 0)
        	return algorithmData;
        
        Vector<String[]> selectionGroup = new Vector<String[]>();
        for(int i=0; i<initialResultMatrix.length; i++) {
			double compareValue;
			//If a column with corrected p-values is available, use that as the cutoff to determine whether the 
			//term should be included in nested ease analysis.
			try {
				compareValue = new Double(initialResultMatrix[i][9]).doubleValue();
			} catch (ArrayIndexOutOfBoundsException aioobe) {
				compareValue = new Double(initialResultMatrix[i][8]).doubleValue();
			}
			if (compareValue < fishersCutoff && new Double(initialResultMatrix[i][6]).doubleValue() > 4) {
        		selectionGroup.add(initialResultMatrix[i]);
        	}
        }

		termlist = new int[selectionGroup.size()];
		for (int k = 0; k < selectionGroup.size(); k++)
			termlist[k] = k;
  
		algorithmData.setNestedEaseCount(termlist.length);
		int[][] clusterMatrix = algorithmData.getClusterMatrix();
        int[] thisCluster;
        
        //Storage for each of the AlgorithmDatas that will result from each EASE run
		EaseAlgorithmData[] nestedAlgorithmResults = new EaseAlgorithmData[termlist.length];

        //The list of terms that nested EASE is run on.
        String[] nestedEaseTerms = new String[termlist.length];

		//Holds the consolidated results from all nested ease runs. Includes all NEASE 
		//results whose neaseEnrichment value is >.001
		Vector<String[]> consolidatedNeaseTable = new Vector<String[]>();

        //Now loop through each item in termlist and do nested EASE on it.
        for (int i=0; i<termlist.length; i++) {
        	//A term selected by the user to run Nested EASE on
        	String[] thisTerm = selectionGroup.get(termlist[i]);

        	//the group of genes associated with this term.
			//System.out.println("termlist[i]: " + termlist[i]);
            thisCluster = clusterMatrix[termlist[i]];
        	
        	//These two objects (nData and nParams) *must* be declared anew in each iteration of this for loop.
        	//Do not refactor to put declaration outside of this for loop.
			EaseAlgorithmData nData = duplicateEASEAlgorithmData(algorithmData);
		
			if (algorithmData.isHaveAccessions()) {
				event.setDescription("Running Nested EASE analysis on " + thisTerm[3] + "\n");
			} else {
				event.setDescription("Running Nested EASE analysis on " + thisTerm[2] + "\n");
			}

            fireValueChanged(event);
            
            EASE nEase = new EASE();

			nData.setRecursedRun(true);
			nData.setRunNease(false);
			if (algorithmData.isHaveAccessions()) {
				nData.setNestingTerm(thisTerm[1] + "	" + thisTerm[3]);
				nestedEaseTerms[i] = thisTerm[1] + ": " + thisTerm[3];
			} else {
				nData.setNestingTerm(thisTerm[1] + "	" + thisTerm[2]);
				nestedEaseTerms[i] = thisTerm[1] + ": " + thisTerm[2];
			}

			String[] oldClusterKeys = algorithmData.getSampleList();
			int[] oldCluster = algorithmData.getSampleIndices();
            
            String[] newClusterKeys = new String[thisCluster.length];

			//What the heck does this block do? 
            for(int j=0; j<oldClusterKeys.length; j++) {
            	for(int k=0; k<thisCluster.length; k++) {
	            	if(oldCluster[j] == thisCluster[k]) {
	            		newClusterKeys[k] = oldClusterKeys[j];
	            	}
            	}
            }

			nData.setSampleList(newClusterKeys);
			nData.setSampleIndices(thisCluster);

			Vector<String> _temp1 = jstats.getSubPopulationForCategory(nData.getNestingTerm());
//				System.out.println("sub population " + nData.getNestingTerm() + "size " + _temp1.size());
			String[] newPopKeys = new String[_temp1.size()];
			String thisKey;

			for (int k = 0; k < _temp1.size(); k++) {
				thisKey = _temp1.get(k);
				for (int j = 0; j < populationElementList.size(); j++) {
					if (populationElementList.get(j).getEaseKeys().contains(thisKey)) {
						newPopKeys[k] = populationElementList.get(j).getMevKey();
    }
				}
			}

			//This block finds the name of the annotation file that this term came from
			//and stores that annotation file only in the 'annotation-file' input parameter
			//for the nested ease run
			String[] annFileList = new String[1];
			String[] oldAnnFileList = algorithmData.getAnnotationFileList();//getStringArray("annotation-file-list");
			for (int k = 0; k < oldAnnFileList.length; k++) {
				String oldAnnFile = oldAnnFileList[k].substring(
						oldAnnFileList[k].lastIndexOf(System.getProperty("file.separator")) + 1, oldAnnFileList[k]
								.lastIndexOf("."));
				if (thisTerm[1].equalsIgnoreCase(oldAnnFile)) {
					annFileList[0] = oldAnnFileList[k];
					break;
				}
			}
			nData.setPopulationList(newPopKeys);
			nData.setAnnotationFileList(annFileList);
			nData.setRunPermutationAnalysis(false);
			nData.setHochbergCorrection(false);

			
			nData = (EaseAlgorithmData) nEase.execute(nData);
			
			if (nData.getResultMatrix() != null) {
				nData.setResultMatrix(expandResults(nData, algorithmData));
				consolidatedNeaseTable.addAll(getFilteredSignificantData(nData, algorithmData));
			} else {

			}
			nestedAlgorithmResults[i] = nData;
		
		}//end loop through each selected EASE term

        //add nested results to algorithmData
        algorithmData.setSelectedNestedTerms(nestedEaseTerms);
        for(int i=0; i<nestedAlgorithmResults.length; i++) {
            algorithmData.addResultAlgorithmData(i, nestedAlgorithmResults[i]);        	
        }
        
        //put consolidated nested ease table here.
        //table should include all nease terms, ranked on their nease enrichment result. 
        //include everything with .1% or greater
        String[][] neases = new String[consolidatedNeaseTable.size()][];
        for(int k=0; k<consolidatedNeaseTable.size(); k++) {
        	String[] consolidatedTemp = consolidatedNeaseTable.get(k);
        	neases[k] = consolidatedTemp;
        }
        
        String[] temp = algorithmData.getHeaderNames();
        String[] neaseSummaryHeaders = new String[15];
        for(int j=0; j<9; j++) {
        	neaseSummaryHeaders[j] = temp[j];
        }
        neaseSummaryHeaders[neaseSummaryHeaders.length - 6] = "Gene Enrich.";
        neaseSummaryHeaders[neaseSummaryHeaders.length - 5] = "p-value log diff.";
        neaseSummaryHeaders[neaseSummaryHeaders.length - 4] = "nEASE Gene Enrich.";
        neaseSummaryHeaders[neaseSummaryHeaders.length - 3] = "Percent Gene Enrich.";
        neaseSummaryHeaders[neaseSummaryHeaders.length - 2] = "EASE Acc";
        neaseSummaryHeaders[neaseSummaryHeaders.length - 1] = "EASE Term";

        algorithmData.setNeaseConsolidatedResults(neases);
        algorithmData.setNeaseHeaderNames(neaseSummaryHeaders);//addStringArray("nease-headers", neaseSummaryHeaders);
        return algorithmData;
    }

	/**
	 * @param nData
    */
	private Vector<String[]> getFilteredSignificantData(EaseAlgorithmData nData, EaseAlgorithmData eData) {
		String[][] longResults = nData.getResultMatrix();
		Vector<String[]> filteredResults = new Vector<String[]>();
		for(int resultsIndex=0; resultsIndex<longResults.length; resultsIndex++) {
			String[] thisRow = longResults[resultsIndex];
			int rowlength = thisRow.length;
			double neaseGeneEnrichment = new Double(longResults[resultsIndex][rowlength-4]); //write methods EaseAlgorithmData.getNeaseGeneEnrichment(int), etc.
			double neasePvalueDiff = new Double(longResults[resultsIndex][rowlength-3]);
			double newNeaseGeneEnrich = new Double(longResults[resultsIndex][rowlength-2]);
			double neaseScore = new Double(longResults[resultsIndex][rowlength-1]);
			double percentGeneEnrich = new Double(longResults[resultsIndex][rowlength-1]) * 100;
			double neaseFishers = nData.getFishers(resultsIndex);
			//For terms that meet the enrichment cutoff, put their summary data into the NEASE summary table
			if (neaseScore > 0 && neaseGeneEnrichment > 0 && neasePvalueDiff > 0 && newNeaseGeneEnrich > 0 && neaseFishers < .05) {
				String[] consolidatedTemp = new String[15];
				for (int k = 0; k < 9; k++)
					consolidatedTemp[k] = thisRow[k];
    
    
				//String thisNeaseResultTerm = nData.getTerm(resultsIndex);
				//int easeRowIndexForThisTerm = eData.getIndexForTerm(thisNeaseResultTerm);
				String condensedNestedTerm = nData.getNestingTerm().substring(nData.getNestingTerm().lastIndexOf("	")+1, nData.getNestingTerm().length());
				//System.out.println("CondensedNestedTerm: " + condensedNestedTerm);
				int eDataResultsIndex = eData.getIndexForTerm(condensedNestedTerm);
				
				consolidatedTemp[consolidatedTemp.length - 6] = String.valueOf(neaseGeneEnrichment);
				consolidatedTemp[consolidatedTemp.length - 5] = String.valueOf(neasePvalueDiff);
				consolidatedTemp[consolidatedTemp.length - 4] = String.valueOf(newNeaseGeneEnrich);
				consolidatedTemp[consolidatedTemp.length - 3] = String.valueOf(percentGeneEnrich);	
				
				consolidatedTemp[consolidatedTemp.length - 2] = eData.getFileName(eDataResultsIndex);

				consolidatedTemp[consolidatedTemp.length - 1] = nData.getNestingTerm();
				consolidatedTemp[consolidatedTemp.length - 1] = condensedNestedTerm;
				
				filteredResults.add(consolidatedTemp);
			} else {
				//Data doesn't meet cutoff
			}
		}
		return filteredResults;
	}
    
    private String[][] expandResults(EaseAlgorithmData neaseData, EaseAlgorithmData easeData) {

	    String[][] nResults = neaseData.getResultMatrix();
		
		for (int neaseResultsIndex = 0; neaseResultsIndex < nResults.length; neaseResultsIndex++) {
			String thisNeaseResultTerm = neaseData.getTerm(neaseResultsIndex);
			//String thisNeaseFileName = neaseData.getFileName(neaseResultsIndex);
			int easeRowIndexForThisTerm = easeData.getIndexForTerm(thisNeaseResultTerm);

			String[] originalShortNeaseResultRow = nResults[neaseResultsIndex];
			String[] newLongNeaseResultRow = new String[originalShortNeaseResultRow.length + 4];
			for (int x = 0; x < originalShortNeaseResultRow.length; x++) {
				newLongNeaseResultRow[x] = originalShortNeaseResultRow[x];
			}
			
			double neaseListHits = neaseData.getListHits(neaseResultsIndex);
			double neaseListSize = neaseData.getListSize(neaseResultsIndex);
			double neasePopHits = neaseData.getPopHits(neaseResultsIndex);
			double neasePopSize = neaseData.getPopSize(neaseResultsIndex);
			double neaseFishers = neaseData.getFishers(neaseResultsIndex);

			double easeListHits = easeData.getListHits(easeRowIndexForThisTerm);
			double easeListSize = easeData.getListSize(easeRowIndexForThisTerm);
			double easePopHits = easeData.getPopHits(easeRowIndexForThisTerm);
			double easePopSize = easeData.getPopSize(easeRowIndexForThisTerm);
			double easeFishers = easeData.getFishers(easeRowIndexForThisTerm);

			double percentGeneEnrich = (neaseListHits / neasePopHits) - (neaseListSize / neasePopSize);
			double neaseGeneEnrichment = neaseListHits - (neaseListSize / neasePopSize) * neasePopHits;
			double easeEnrichment = (easeListHits / easePopHits - easeListSize / easePopSize);

			double neaseEnrichment = (percentGeneEnrich - easeEnrichment) / easeEnrichment;

			double easeMinusLog = -Math.log10(easeFishers);
			double neaseMinusLog = -Math.log10(neaseFishers);
			double neasePvalueDiff = neaseMinusLog - easeMinusLog;

			double easeGeneEnrichment = easeListHits - (easeListSize / easePopSize) * easePopHits;
			double newNeaseGeneEnrich = neaseGeneEnrichment - easeGeneEnrichment;
			double neaseScore = neasePvalueDiff + newNeaseGeneEnrich;

			newLongNeaseResultRow[newLongNeaseResultRow.length - 4] = String.valueOf(neaseGeneEnrichment);
			newLongNeaseResultRow[newLongNeaseResultRow.length - 3] = String.valueOf(neasePvalueDiff);
			newLongNeaseResultRow[newLongNeaseResultRow.length - 2] = String.valueOf(newNeaseGeneEnrich);
			newLongNeaseResultRow[newLongNeaseResultRow.length - 1] = String.valueOf(percentGeneEnrich);

			nResults[neaseResultsIndex] = newLongNeaseResultRow;
		}

		String[] temp = neaseData.getHeaderNames();
		String[] temp2 = new String[temp.length + 4];

		for (int j = 0; j < temp.length; j++) {
			temp2[j] = temp[j];
		}

		temp2[temp2.length - 4] = "Gene Enrich.";
		temp2[temp2.length - 3] = "p-value log diff.";
		temp2[temp2.length - 2] = "nEASE Gene Enrich.";
		temp2[temp2.length - 1] = "Percent Gene Enrich.";
		neaseData.addStringArray("header-names", temp2);
		
		return nResults;
    }
    
    /**
     * Creates a new AlgorithmData object with the same input parameters as the supplied 
     * algorithmData object. Uses copy-by-value, not by reference. This method is specifically
     * written for the EASE module and will not work for other modues' AlgorithmData objects.
     */
    protected EaseAlgorithmData duplicateEASEAlgorithmData(EaseAlgorithmData oldData) {
    	EaseAlgorithmData newData = new EaseAlgorithmData();

        newData.setExpression(oldData.getExpression());
        newData.setPerformClusterAnalysis(oldData.isPerformClusterAnalysis());
        newData.setRunNease(false);
    	newData.setReportEaseScore(oldData.isReportEaseScore());
    	if(oldData.getConverterFileName() != null)
    		newData.setConverterFileName(oldData.getConverterFileName());
    	if(oldData.getAnnotationFileList() != null) {
    		newData.setAnnotationFileList(oldData.getAnnotationFileList());
    	}
    	newData.setImpliesFileLocation(oldData.getImpliesFileLocation());
    	newData.setTagFileLocation(oldData.getTagFileLocation());
    	newData.setPopulationList(oldData.getPopulationList());
    	newData.setHaveAccessions(oldData.isHaveAccessions());
    	newData.setHochbergCorrection(false);
    	newData.setTrimOption(oldData.getTrimOption());
    	newData.setTrimValue(oldData.getTrimValue());
    	return newData;
    }
    
    /** Main method for cluster analysis.
     * @param algorithmData Input data and parameters.
     * @throws AlgorithmException
     * @return
     */
    protected EaseAlgorithmData performClusterAnnotationAnalysis(EaseAlgorithmData algorithmData) throws AlgorithmException {
        AlgorithmParameters params = algorithmData.getParams();
        
        headerNames = new Vector<String>();
        reportEaseScore = algorithmData.isReportEaseScore();//params.getBoolean("report-ease-score", false);
        intializeHeaderNames();
        
        format = new DecimalFormat("0.###E00");
        
        event = new AlgorithmEvent(this, AlgorithmEvent.MONITOR_VALUE, 0);
        event.setDescription("Start EASE Analysis\n");
        fireValueChanged(event);
        
        String converterFileName = algorithmData.getConverterFileName();
        
        int [] clusterIndices = algorithmData.getSampleIndices();//getIntArray("sample-indices");
        String [] sampleList = algorithmData.getSampleList();//getStringArray("sample-list");
        String [] populationList = algorithmData.getPopulationList();//getStringArray("population-list");
        annotationFileList = algorithmData.getAnnotationFileList();//getStringArray("annotation-file-list");
        impliesFileLocation = algorithmData.getImpliesFileLocation();
        tagsFileLocation = algorithmData.getTagFileLocation();

        EaseElementList sampleElementList = new EaseElementList(clusterIndices, sampleList);
        populationElementList = new EaseElementList(populationList);
   
        if(stop)
            return null;
        
        //If a converter file is specified, use that converter file to map the genes in 
        //the selected cluster to the annotation that's in the GO term file. Set those values
        //in the populationElementList and sampleElementList. If no conversion file is 
        //specified, set the default values instead.
        try {
            if(converterFileName != null){
                event.setDescription("Loading Cluster Annotation List\n");
                fireValueChanged(event);
                
                sampleElementList.loadValues(converterFileName);
                

                event.setDescription("Loading Population Annotation List\n");
                fireValueChanged(event);
                
                populationElementList.loadValues(converterFileName);

            } else {
            	event.setDescription("Preparing Annotation Lists (no conversion file)\n");
                sampleElementList.setDefaultValues();
                populationElementList.setDefaultValues();
            }
        } catch (FileNotFoundException fnfe) {
            throw new AlgorithmException("Annotation Conversion File Not Found\n"+converterFileName+"\n"+fnfe.getMessage());
        } catch (IOException ioe) {
            throw new AlgorithmException("Error Reading File: "+converterFileName+"\n"+ioe.getMessage());
        }
        event.setDescription("Extracting Unique Cluster Annotation List\n");
        fireValueChanged(event);
        sampleVector = sampleElementList.getUniqueValueList();
        
        if(stop)
            return null;
        
        event.setDescription("Extracting Unique Population Annotation List\n");
        fireValueChanged(event);
        populationVector = populationElementList.getUniqueValueList();
        
        if(stop)
            return null;
        
        jstats = new JEASEStatistics(reportEaseScore);
        
        for(int i = 0; i < annotationFileList.length; i++){
            jstats.AddAnnotationFileName(annotationFileList[i]);
        }
        jstats.setImpliesFileLocation(impliesFileLocation);
        
        event.setDescription("Loading Annotation Category Files\n");
        fireValueChanged(event);
        
        jstats.GetCategories(populationVector);

        if(stop)
            return null;
        
        //alter to take the keys as args
        
        event.setDescription("Finding Sample Category Hits\n");
        fireValueChanged(event);
        
        jstats.GetListHitsByCategory(sampleVector);
        
        if(stop)
            return null;
        
        //alter to take keys as arguments
        event.setDescription("Finding Population Category Hits\n");
        fireValueChanged(event);
        
        jstats.GetPopulationHitsByCategory(populationVector);
        
        event.setDescription("Statistical Testing and Result Prep.\n");
        fireValueChanged(event);
        jstats.ConstructResults();
        
        result = jstats.getResults();
        
        //if the result set is empty, return.
        if(result.length < 1) {
            return algorithmData;
        }
        
        hitList = jstats.getListHitMatrix();
        categoryNames = jstats.getCategoryNames();
        
        event.setDescription("Sorting Result on p-value\n");
        fireValueChanged(event);
        sortResults();
        
        if(stop)
            return null;
        
        if(algorithmData.getParams().getBoolean("p-value-corrections", false)){
            event.setDescription("Applying p-value Multiplicity Corrections\n");
            fireValueChanged(event);
            pValueCorrections(algorithmData);
        }
        
        if(algorithmData.isRunPermutationAnalysis()){
        	event.setDescription("Resampling Analysis\n");
        	fireValueChanged(event);
        	Vector<String> sourcePop = null;
        	
        	if(isRecursedEaseRun) {
        		String nestingTerm = algorithmData.getNestingTerm();
        		sourcePop = jstats.getSubPopulationForCategory(nestingTerm);
        	}
    		permutationAnalysis(algorithmData.getParams().getInt("permutation-count", 1), sourcePop);
        }
        
        if(stop)
            return null;
        
        event.setDescription("Appending Accessions\n");
        fireValueChanged(event);
        result = appendAccessions(result, annotationFileList);
        algorithmData.setHaveAccessions(haveAccessionNumbers);
        
        
        //apply trim options
        String trimOption = algorithmData.getTrimOption();
        float trimValue;
        if(!(trimOption.equals("NO_TRIM"))){
            event.setDescription("Trim Result\n");
            fireValueChanged(event);
            trimValue = algorithmData.getTrimValue();
            trimResult(trimOption, trimValue);
        }
        
        event.setDescription("Indexing Result\n");
        fireValueChanged(event);
        indexResult();
        
        algorithmData.setResultMatrix(result);
        algorithmData.setHitListMatrix(hitList);
        
        //get sorted clusters
        event.setDescription("Extracting Cluster Indices and Stats\n");
        fireValueChanged(event);
        int [][] clusters = getClusters(sampleElementList, hitList);        
        
        algorithmData.addStringArray("category-names", categoryNames);
        algorithmData.setClusterMatrix(clusters);
        
        algorithmData.setHeaderNames(getHeaderNames());
        
        FloatMatrix means = getMeans(expData, clusters);
        algorithmData.addMatrix("means", means);
        algorithmData.addMatrix("variances", getVariances(expData, means, clusters));
        
        if(stop)
            return null;
        
        return algorithmData;
    }
    
	/** Alternative analysis mode (slide survey)
     * @param algorithmData
     * @throws AlgorithmException
     * @return  */
    protected EaseAlgorithmData performSlideAnnotationSurvey(EaseAlgorithmData algorithmData) throws AlgorithmException {
        AlgorithmParameters params = algorithmData.getParams();
        
        headerNames = new Vector<String>();
        intializeHeaderNames();
        format = new DecimalFormat("0.###E00");
        
        AlgorithmEvent event = new AlgorithmEvent(this, AlgorithmEvent.MONITOR_VALUE, 0);
        event.setDescription("Start Survey\n");
        fireValueChanged(event);
        
        
        String converterFileName = algorithmData.getConverterFileName();
        String [] populationList = algorithmData.getPopulationList();//getStringArray("population-list");
        String [] annotationFileList = algorithmData.getAnnotationFileList();//getStringArray("annotation-file-list");
        populationElementList = new EaseElementList(populationList);
        
        try{
            if(converterFileName != null){
                event.setDescription("Loading Population Annotation List\n");
                fireValueChanged(event);
                populationElementList.loadValues(converterFileName);
            } else {
                event.setDescription("Preparing Annotation Lists (no conversion file)\n");
                populationElementList.setDefaultValues();
            }
        } catch (FileNotFoundException fnfe) {
            throw new AlgorithmException("Annotation Conversion File Not Found\n"+converterFileName+"\n"+fnfe.getMessage());
        } catch (IOException ioe) {
            throw new AlgorithmException("Error Reading File: "+converterFileName+"\n"+ioe.getMessage());
        }
        
        event.setDescription("Extracting Unique Population Annotation List\n");
        fireValueChanged(event);
        populationVector = populationElementList.getUniqueValueList();
        
        jstats = new JEASEStatistics();
        
        for(int i = 0; i < annotationFileList.length; i++){
            jstats.AddAnnotationFileName(annotationFileList[i]);
        }
        jstats.setImpliesFileLocation(impliesFileLocation);
        
        event.setDescription("Reading Annotation Category Files into Memory\n");
        fireValueChanged(event);
        jstats.GetCategories();
        
        event.setDescription("Finding Population Category Hits\n");
        fireValueChanged(event);
        jstats.GetPopulationHitsByCategoryForSurvey(populationVector);
        
        event.setDescription("Result Prep.\n");
        fireValueChanged(event);
        jstats.ConstructSurveyResults();
        
        result = jstats.getSurveyResults();
        
        //if the result set is empty, return.
        if(result.length < 1)
            return algorithmData;
        
        hitList = jstats.getListHitMatrix();
        categoryNames = jstats.getCategoryNames();
        
        event.setDescription("Sorting Result on hit count\n");
        fireValueChanged(event);
        sortSurveyResults();
        
        event.setDescription("Appending Accessions\n");
        fireValueChanged(event);
        result = appendAccessions(result, annotationFileList);
        algorithmData.setHaveAccessions(haveAccessionNumbers);
        algorithmData.setHeaderNames(getHeaderNames());
        
        //apply trim options
        String trimOption = algorithmData.getTrimOption();
        float trimValue;
        if(!(trimOption.equals("NO_TRIM"))){
            event.setDescription("Trim Result\n");
            fireValueChanged(event);
            trimValue = algorithmData.getTrimValue();
            trimResult(trimOption, trimValue);
        }
        
        event.setDescription("Indexing Result\n");
        fireValueChanged(event);
        indexResult();
        
        algorithmData.setResultMatrix(result);
        algorithmData.addObjectMatrix("hit-list-matrix", hitList);
        
        //get sorted clusters
        event.setDescription("Extracting Cluster Indices and Stats\n");
        fireValueChanged(event);
        int [][] clusters = getClusters(populationElementList, hitList);
        
        algorithmData.addStringArray("category-names", categoryNames);
        algorithmData.setClusterMatrix(clusters);
        
        FloatMatrix means = getMeans(expData, clusters);
        algorithmData.addMatrix("means", means);
        algorithmData.addMatrix("variances", getVariances(expData, means, clusters));
        
        return algorithmData;
    }
    
    /** Creates header names based on analysis mode.
     */
    protected void intializeHeaderNames(){
        if(performClusterAnalysis){
            headerNames.add("Index");
            headerNames.add("File");
            headerNames.add("Term");
            headerNames.add("List Hits");
            headerNames.add("List Size");
            headerNames.add("Pop. Hits");
            headerNames.add("Pop. Size");
            if(reportEaseScore)
                headerNames.add("EASE Score");
            else
                headerNames.add("Fisher's Exact");
        } else {
            headerNames.add("Index");
            headerNames.add("File");
            headerNames.add("Term");
            headerNames.add("Pop. Hits");
            headerNames.add("Pop. Size");
        }
    }
    
    /** Returns cluster indices
     * @param clusterList list of cluster indices
     * @param hitList List of acc. in each category
     * @return
     */
    protected int [][] getClusters(EaseElementList clusterList, String [][] hitList){
        int [][] clusters = new int[hitList.length][];
        for(int i = 0; i < hitList.length; i++){
            clusters[i] = clusterList.getIndices(hitList[i]);
        }
        return clusters;
    }
    
    
    /** Sorts analysis results on stat.
     */
    protected void sortResults(){
        double [] stat = new double[result.length];
        int pValueIndex;
        if(reportEaseScore)
            pValueIndex =  headerNames.indexOf("EASE Score");
        else
            pValueIndex =  headerNames.indexOf("Fisher's Exact");
        
        pValueIndex--;  //subtract one since result is not indexed until after sort

        for(int i = 0; i < result.length; i++){
            stat[i] = Double.parseDouble(result[i][pValueIndex]);
        }
        QSort qsorter = new QSort(stat);
        stat = qsorter.getSortedDouble();
        
        int [] orderedIndices = qsorter.getOrigIndx();
        
        String [] newCatNames = new String[categoryNames.length];
        String [][] newHitList = new String[hitList.length][];
        String [][] newResult = new String[result.length][];
        
        for(int i = 0; i < orderedIndices.length; i++){
            newCatNames[i] = categoryNames[orderedIndices[i]];
            newHitList[i] = hitList[orderedIndices[i]];
            newResult[i] = result[orderedIndices[i]];
        }
        
        //format the pvalues
        for(int i = 0; i < newResult.length; i++) {
        	newResult[i][pValueIndex] = format.format(Double.parseDouble(newResult[i][pValueIndex]));
        	
        	//handles international conventions where comma denotes decimal
        	newResult[i][pValueIndex] = newResult[i][pValueIndex].replace(',','.');
        }
        
        categoryNames = newCatNames;
        hitList = newHitList;
        result = newResult;
    }
    
    
    /** Sorts survey analysis results on population hits (high --> low)
     */
    protected void sortSurveyResults(){
        double [] hitCounts = new double[result.length];
        int hitIndex = this.headerNames.indexOf("Pop. Hits");
        hitIndex--; //decrement since no indexes inserted yet.
        for(int i = 0; i < result.length; i++){
            hitCounts[i] = Double.parseDouble(result[i][hitIndex]);
        }
        QSort qsorter = new QSort(hitCounts);
        hitCounts = qsorter.getSortedDouble();
        
        int [] orderedIndices = qsorter.getOrigIndx();
        
        String [] newCatNames = new String[categoryNames.length];
        String [][] newHitList = new String[hitList.length][];
        String [][] newResult = new String[result.length][];
        
        //order high to low
        int index = 0;
        for(int i = orderedIndices.length-1; i >= 0; i--){
            newCatNames[index] = categoryNames[orderedIndices[i]];
            newHitList[index] = hitList[orderedIndices[i]];
            newResult[index] = result[orderedIndices[i]];
            index++;
        }
        
        categoryNames = newCatNames;
        hitList = newHitList;
        result = newResult;
    }
    
    
    /** Appends accessions
     * @param resultMatrix Result matrix input.
     * @param fileNames File names.
     * @return
     */
    protected String [][] appendAccessions(String [][] resultMatrix, String [] fileNames){

        if(resultMatrix == null || resultMatrix.length < 1)
            return resultMatrix;
        
        String [][] newResult = null;
        File file = null;
        haveAccessionNumbers = false;
        try{
            for(int i = 0; i < fileNames.length; i++){
                file = getAccessionFile(fileNames[i]);
                
                if(file.isFile()){
                    if(!haveAccessionNumbers){
                        newResult = new String[resultMatrix.length][resultMatrix[0].length+1];
                        initializeNewResult(newResult, resultMatrix);
                        headerNames.insertElementAt("Acc.", 2);  //add header name
                    }
                    insertAccessions(file, newResult);
                    haveAccessionNumbers = true;
                    resultMatrix = newResult;  // added to handle multiple files
                }
            }
        }  catch (IOException ioe){
            JOptionPane.showMessageDialog(new JFrame(), "Error in collecting accessions following analysis" +
            " from file: "+file.getName()+"\n Results will not have accessions.  Please check file location"+
            " and format", "File Error", JOptionPane.WARNING_MESSAGE);
            return result;  // return original result
        }
        if(haveAccessionNumbers)
            return newResult;
        else
            return resultMatrix;
        }
    
    /** Builds a result copy
     */
    protected void initializeNewResult(String [][] newResult, String [][] oldResult){
        for(int i = 0; i < newResult.length; i++){
            for(int j = 0; j < oldResult[0].length; j++){
                if(j < 1)
                    newResult[i][j] = oldResult[i][j];
                else
                    newResult[i][j+1] = oldResult[i][j];
            }
        }
        //initialize the acc column
        for(int i = 0; i < newResult.length; i++)
            newResult[i][1] = " ";
    }
    
    
    /** Inserts an index for each record in the result after sorting
     */
    protected void indexResult(){
        if(result == null || result.length < 1)
            return;
        String [][] newResult = new String[result.length][result[0].length+1];
        for(int i = 0; i < result.length; i++){
            newResult[i][0] = String.valueOf(i+1);
            for(int j = 1; j < newResult[0].length; j++){
                newResult[i][j] = result[i][j-1];
            }
        }
        result = newResult;
    }
    
    
    /** Insert accession numbers if they exist.
     * @param file file object
     * @param result Result data
     * @throws IOException
     */
    protected void insertAccessions(File file, String [][] result) throws IOException {
        if(file == null)
            return;
        
        BufferedReader fr = new BufferedReader(new FileReader(file));
        String line;
        Hashtable<String, String> accHash = new Hashtable<String, String>();
        StringTokenizer stok;
        while( (line = fr.readLine()) != null){
            stok = new StringTokenizer(line, "\t");
            accHash.put(stok.nextToken(), stok.nextToken());
        }
        String acc;
        
        for(int i = 0; i < result.length; i++){
            acc = (String)accHash.get(result[i][2]);
            if(acc != null)
                result[i][1] = acc;
        }
        
    }
    /** Creates the <CODE>File</CODE> object containing the
     * accessions (or indices)
     * @param fileName File name String
     * @return
     */
    protected File getAccessionFile(String fileName) {
        File file = new File(fileName);
        String accFileName = file.getName();
    	return new File(tagsFileLocation, accFileName);
    }
    
    /** Returns header names based on criteria of the analysis mode and
     * depending on if accessions are found.
     * @return  */
    protected String [] getHeaderNames(){
        String [] headerNamesArray = new String[headerNames.size()];
        for(int i = 0; i < headerNamesArray.length; i++){
            headerNamesArray[i] = (String)(headerNames.elementAt(i));
        }
        return headerNamesArray;
    }
    
    /**
     *  Calculates means for the clusters
     */
    protected FloatMatrix getMeans(FloatMatrix data, int [][] clusters){
        FloatMatrix means = new FloatMatrix(clusters.length, data.getColumnDimension());
        for(int i = 0; i < clusters.length; i++){
            means.A[i] = getMeans(data, clusters[i]);
        }
        return means;
    }
    
    /**
     *  Returns a set of means for an element
     */
    protected float [] getMeans(FloatMatrix data, int [] indices){
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
    protected FloatMatrix getVariances(FloatMatrix data, FloatMatrix means, int [][] clusters){
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
    protected float [] getVariances(FloatMatrix data, FloatMatrix means, int [] indices, int clusterIndex){
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
    
    /** Appends a result onto the main result
     * @param resultVector data to append
     */
    protected void appendResult(Vector<double[]> resultVector){
        int numCorr = resultVector.size();
        double [] currentArray;
        int rawPIndex = result[0].length;
        int resultColumns = rawPIndex+numCorr;
        String [][] newResult = new String[result.length][resultColumns];
        
        
        for(int i = 0; i < result.length; i++){
            for(int j = 0; j < result[0].length; j++){
                newResult[i][j] = result[i][j];
            }
        }
        int resultCol;
        for(int col = 0; col < numCorr; col++){
            currentArray = resultVector.elementAt(col);
            resultCol = col + rawPIndex;
            for(int row = 0; row < newResult.length; row++){
                
                newResult[row][resultCol] = format.format(currentArray[row]);
     
                //handles international conventions where comma denotes decimal
                newResult[row][resultCol] = newResult[row][resultCol].replace(',','.');
     
            }
        }
        result = newResult;
    }
    
    
    /** Selects and makes calles to various multiplicity corrections.
     */
    protected void pValueCorrections(AlgorithmData inputData){
        int k = this.result.length;
        double [] pValues = new double[k];
        
        int pIndex;
        if(reportEaseScore)
            pIndex =  headerNames.indexOf("EASE Score");
        else
            pIndex =  headerNames.indexOf("Fisher's Exact");
        
        pIndex--;  //subtract one since result is not indexed until after sort
        
        Vector<double[]> pValueCorrectionVector = new Vector<double[]>();
        for(int i = 0; i < k; i++){
            pValues[i] = Double.parseDouble(result[i][pIndex]);
        }
        AlgorithmParameters params = inputData.getParams();
        
        if(params.getBoolean("bonferroni-correction", false)){
            pValueCorrectionVector.add(bonferroniCorrection(pValues));
            headerNames.add("Bonf. Corr.");
        }
        
        if(params.getBoolean("bonferroni-step-down-correction", false)){
            pValueCorrectionVector.add(stepDownBonferroniCorrection(pValues));
            headerNames.add("Bonf. S.D. Corr.");
        }
        
        if(params.getBoolean("sidak-correction", false)){
            pValueCorrectionVector.add(sidakCorrection(pValues));
            headerNames.add("Sidak Corr.");
        }
        
        if(params.getBoolean("hochberg-correction", false)){
            pValueCorrectionVector.add(benjaminiHochbergCorrection(pValues));
            headerNames.add("Hoch. Corr.");
        }
        appendResult(pValueCorrectionVector);
    }
    
    /**
     * Returns a list of p-value probabilities corresponding to the p-values input. 
     * Lifted and modified from the NonpaR module. 
     * 
     * @param pvalues
     * @return
     */
    private double[] benjaminiHochbergCorrection(double[] pvalues) {

    	QSort sort = new QSort(pvalues);
		double [] sortedP = sort.getSortedDouble();
		double [] adjustedP = new double[pvalues.length];


		//adjust pvalues
		for(int i = 0; i < sortedP.length; i++) {
			adjustedP[i] = (sortedP[i]*(float)sortedP.length)/(float)(i+1);			
		}
		
		if(sortedP.length > 0) {
			//stepdown procedure, store in sortedP
			sortedP[sortedP.length-1] = adjustedP[sortedP.length-1];
			
			for(int i = sortedP.length-2; i >= 0; i--) {
				sortedP[i] = Math.min(sortedP[i+1],adjustedP[i]);
			}
		}
			
		return sortedP;
	}

    
    /** Performs the standard Bonferroni correction.
     * @param pValues Raw values
     * @return Returns corrected values.
     */
    protected double [] bonferroniCorrection(double [] pValues){
        int k = pValues.length;
        double [] correctedP = new double[k];
        for(int i = 0; i < k; i++){
            correctedP[i] = pValues[i]*(double)k;
            if(correctedP[i] > 1.0d)
                correctedP[i] = 1.0d;
        }
        return correctedP;
    }
    
    /** Performs the step down Bonferroni correction.
     * @param pValues input values
     * @return returns corrected values
     */
    protected double [] stepDownBonferroniCorrection(double [] pValues){
        int k = pValues.length;
        double [] correctedP = new double[k];
        int m = 0;
        
        //base case
        correctedP[0] = pValues[0]*(double)k;
        
        for(int i = 1; i < k; i++){
            if(pValues[i] > pValues[i-1])
                m = i;
            correctedP[i] = pValues[i]*(double)(k-m);
            if(correctedP[i] > 1.0d)
                correctedP[i] = 1.0d;
        }
        return correctedP;
    }
    
    /** Perform Sidak method.
     * @param pValues input
     * @return corrected output
     */
    protected double [] sidakCorrection(double [] pValues){
        int k = pValues.length;
        double [] correctedP = new double[k];
        for(int i = 0; i < k; i++){
            correctedP[i] = 1.0d-Math.pow( (1.0d-pValues[i]), (double)k );
            if(correctedP[i] > 1.0d)
                correctedP[i] = 1.0d;
        }
        return correctedP;
    }
    
    /**
	 * performs permutation analysis, bootstrapping selection of random
	 * samples from the population.
	 * 
	 * @param p number of permutations
     */
    protected void permutationAnalysis(int p, Vector<String> sourcePop){
        //Get a list of categories, have a corresponding accumulator array
        //Have a population Vector of strings
        //take k elements from here to construct a new sample list
        //then from jstats getList HitsByCategory() and Construct results
        
        //increment counters indicating the times times when a terms p value falls below
        //the minimum p value for a resampling iteration.
        
        AlgorithmEvent permEvent = new AlgorithmEvent(this, AlgorithmEvent.PROGRESS_VALUE, p);
        permEvent.setDescription("SET_UNITS");
        permEvent.setIntValue(p);
        fireValueChanged(permEvent);
        permEvent.setDescription("SET_VALUE");
        
        int k = result.length;
        int sampleSize = this.sampleVector.size();
        int [] accumulator = new int[this.result.length];
        
        for(int i = 0; i < accumulator.length; i++)
            accumulator[0]=0;
        
        Random rand = new Random(System.currentTimeMillis());
        String [][] testResult;
        
		double[] resampledLowestPValues = new double[p];
		int[] lowerPvalueCounts = new int[result.length];
        
		boolean chooseLowestPValueFromEachPermutation = false;
		boolean chooseLowestPValueFromEachFunctionalClass = true;

        for(int i = 0; i < p; i++){
            permEvent.setIntValue(i+1);
            fireValueChanged(permEvent);

            //EH Nested EASE uses a different resampling method
            if(sourcePop != null) {
            	sampleVector = getRandomSampleVector(sampleSize, rand, sourcePop);
            } else {
            	sampleVector = getRandomSampleVector(sampleSize, rand, null);
            }
            jstats.resetForNewList();

            jstats.GetListHitsByCategory(sampleVector);

            jstats.ConstructResults();

            testResult = jstats.getResults();
            
            accumulateBinHits(testResult, accumulator);


        }
			double[] prob = new double[k];       
        for(int i = 0; i < k; i++){
            prob[i] = (double)accumulator[i]/(double)p;
            if(prob[i] == 0d)
                prob[i] = 1d/(double)p;
        }
        
        permEvent.setDescription("DISPOSE");  //get rid of progress bar
        fireValueChanged(permEvent);

        Vector<double[]> probVector = new Vector<double[]>();
        probVector.add(prob);
        appendResult(probVector);

        headerNames.add("Prob. Anal.");
	}
    
    /**
	 * Returns the maximum number of population hits for any category.
     */
    protected int getMaxPopHits(String [][] result){
        int max = Integer.MIN_VALUE;
        for(int i = 0; i < result.length; i++){
            max = Math.max(max, Integer.parseInt(result[i][4]));
        }
        return max;
    }
    
	/**
	 * Returns a random sample vector of indices. If a source vector is
	 * specified in source, choose random sample vector from that source.
     */
    protected Vector<String> getRandomSampleVector(int sampleSize, Random rand, Vector<String> sourcePop) {
        Vector<String> sampleVector = new Vector<String>(sampleSize);
        Vector<String> dummyPopVector;
        if(sourcePop == null) {
        	dummyPopVector = new Vector<String>(populationVector);
        } else {
        	dummyPopVector = new Vector<String>(sourcePop);
        }
        
        int index = 0;

        for(int i = 0; i < sampleSize; i++){
            index = (int)(dummyPopVector.size()*rand.nextFloat());
            sampleVector.add(dummyPopVector.remove(index));  //enforce w/o replacement
        }
        
        return sampleVector;
    }
    
    /**
	 * accumulates list hits results from permutations
	 * 
     * @param result
     * @param keys
	 * @param accumulator
	 */
    protected void accumulateHits(String [][] result, String [] keys, int [] accumulator){
        for(int i = 0; i < result.length; i++){
            for(int j = 0; j < keys.length; j++){
                if((result[i][1]).equals(keys[j])){
                    if(Integer.parseInt(result[i][2]) > Integer.parseInt(this.result[j][2]))
                        accumulator[j]++;
                    break;
                }
            }
        }
    }
    
    /** 
     * Accumulates results from permutations.
     */
    protected void accumulateBinHits(String [][] permutedResult, int [] accumulator){
        
        double minP;
        
        //for each type of annotation used in this run (GO Molecular Function, KEGG pathway, etc)
        for(int cat = 0; cat < annotationFileList.length; cat++){
            
            minP = Double.POSITIVE_INFINITY;

            //for each term in the permuted ease results
            for(int i = 0; i < permutedResult.length; i++){
            	//if the current term is from the type of annotation referred to by the cat iterator
            	//
                if(this.annotationFileList[cat].indexOf(permutedResult[i][0]) >= 0)
                    //store in minP the pvalue of this term
                	minP = Math.min(minP, Double.parseDouble(permutedResult[i][6]));
            }

            //for each term in the original ease results
            for(int j = 0; j < result.length; j++){
            	//if this term is one of those from the current annotation type as denoted by cat
            	//and the pvalue for this permuted run is lower than the pvalue for this term in the original 
            	//ease run, increment the accumulator at index j. (The accumulator at index j stores
            	//the count of permuted pvalues that are smaller than the "real" ease run's pvalue
                if((this.annotationFileList[cat].indexOf(result[j][0]) >= 0) && (minP < Double.parseDouble(result[j][6]))){
                    accumulator[j]++;
                }
            }
        }
    }
    
    
    /**
	 * Orders the bootstrap probabilities based on raw probability order.
     */
    protected double [] orderBootStrappedProb(double [] prob){
        double [] orderedProb = new double[result.length];
        for(int i = 0; i < this.result.length; i++){
            orderedProb[i] = prob[Integer.parseInt(this.result[i][2])];
        }
        return orderedProb;
    }
    
    
    /**
	 * Trims the results according to specified criteria.
	 * 
	 * @param trimOption
	 *                Defines trim mode, "NO_TRIM", "N_TRIM", or
	 *                "PERCENT_TRIM"
	 * @param trimValue
	 *                Trim parameter.
     */
    protected void trimResult(String trimOption, float trimValue){
        
        boolean [] flagged = new boolean[result.length];
        int hitIndex;
        
        if(this.performClusterAnalysis){
            hitIndex = this.headerNames.indexOf("List Hits");
        } else {
            hitIndex = this.headerNames.indexOf("Pop. Hits");
        }
        
        hitIndex--;  //decrement since we don't have index inserted yet.
        
        int keeperCount = this.result.length;
        if(trimOption.equals("N_TRIM")){
            for(int i = 0; i < result.length; i++){
                if(Integer.parseInt(result[i][hitIndex]) < trimValue){
                    flagged[i] = true;
                    keeperCount--;
                }
            }
        } else { //Percent trim option
            trimValue /= (float)100;
            for(int i = 0; i < result.length; i++){
                if(Double.parseDouble(result[i][hitIndex])/Double.parseDouble(result[i][hitIndex+1]) < trimValue){
                    flagged[i] = true;
                    keeperCount--;
                }
            }
        }
        
        String [][] newResult = new String[keeperCount][];
        String [][] newHitList = new String[keeperCount][];
        String [] newCategoryNames = new String[keeperCount];
        int keeperIndex = 0;
        
        for(int i = 0; i < result.length; i++){
            if(!flagged[i]){
                newResult[keeperIndex] = result[i];
                newHitList[keeperIndex] = hitList[i];
                newCategoryNames[keeperIndex] = categoryNames[i];
                keeperIndex++;
            }
        }
        this.result = newResult;
        this.hitList = newHitList;
        this.categoryNames = newCategoryNames;
    }
    
}

