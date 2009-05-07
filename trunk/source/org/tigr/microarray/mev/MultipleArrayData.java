/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: MultipleArrayData.java,v $
 * $Revision: 1.31 $
 * $Date: 2007-12-19 21:39:34 $
 * $Author: saritanair $
 * $State: Exp $
 */

package org.tigr.microarray.mev;

import java.awt.Color;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.beans.PersistenceDelegate;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JOptionPane;

import org.tigr.microarray.mev.annotation.AnnoAttributeObj;
import org.tigr.microarray.mev.annotation.AnnotationFieldConstants;
import org.tigr.microarray.mev.annotation.IAnnotation;
import org.tigr.microarray.mev.annotation.IChipAnnotation;
import org.tigr.microarray.mev.annotation.MevAnnotation;
import org.tigr.microarray.mev.annotation.MevChipAnnotation;
import org.tigr.microarray.mev.cgh.CGHDataGenerator.CGHCopyNumberCalculator;
import org.tigr.microarray.mev.cgh.CGHDataGenerator.CGHCopyNumberCalculatorNoDyeSwap;
import org.tigr.microarray.mev.cgh.CGHDataObj.AlterationRegion;
import org.tigr.microarray.mev.cgh.CGHDataObj.AlterationRegions;
import org.tigr.microarray.mev.cgh.CGHDataObj.AlterationRegionsComparator;
import org.tigr.microarray.mev.cgh.CGHDataObj.CGHClone;
import org.tigr.microarray.mev.cgh.CGHDataObj.Distribution;
import org.tigr.microarray.mev.cgh.CGHDataObj.FlankingRegion;
import org.tigr.microarray.mev.cgh.CGHDataObj.GeneDataSet;
import org.tigr.microarray.mev.cgh.CGHDataObj.ICGHDataRegion;
import org.tigr.microarray.mev.cgh.CGHDataObj.IGeneData;
import org.tigr.microarray.mev.cgh.CGHDataObj.Cluster.Experiment.BacClonesExperimentParameters;
import org.tigr.microarray.mev.cgh.CGHDataObj.Cluster.Experiment.CGHExperiment;
import org.tigr.microarray.mev.cgh.CGHDataObj.Cluster.Experiment.DataRegionsExperimentParameters;
import org.tigr.microarray.mev.cgh.CGHDataObj.Cluster.Experiment.GenesExperimentParameters;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmParameters;
import org.tigr.microarray.mev.cluster.clusterUtil.Cluster;
import org.tigr.microarray.mev.cluster.clusterUtil.ClusterRepository;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.ICGHCloneValueMenu;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.Progress;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.normalization.IterativeLogMCNormInitDialog;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.normalization.LinRegNormInitDialog;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.normalization.RatioStatsNormInitDialog;
import org.tigr.microarray.mev.file.StringSplitter;
import org.tigr.microarray.mev.persistence.MultipleArrayDataPersistenceDelegate;
import org.tigr.microarray.mev.persistence.MultipleArrayDataState;
import org.tigr.microarray.mev.sampleannotation.MageIDF;
import org.tigr.microarray.util.Adjustment;
import org.tigr.microarray.util.SlideDataSorter;
import org.tigr.midas.engine.Parameter;
import org.tigr.util.FloatMatrix;
import org.tigr.util.QSort;


import cern.jet.math.Arithmetic;
import cern.jet.stat.Probability;


public class MultipleArrayData implements IData {

    private ArrayList featuresList = new ArrayList();
    private ArrayList indicesList  = new ArrayList(); // array of int[]'s

    private ArrayList spotColors = new ArrayList(); // array of Colors
    private int[] colorIndices;
    private ArrayList experimentColors = new ArrayList(); //array of experiment colors
    private int [] experimentColorIndices;

    private Experiment experiment = null;

    private int dataType;

    private float maxCy3 = 0f;
    private float maxCy5 = 0f;
    private float maxRatio = 0f;
    private float minRatio = 0f;

    private float percentageCutoff = 0f;
    private boolean usePercentageCutoff = false;
    private boolean usePvaluePercentageCutoff = false;
    private boolean usePresentCallCutoff = false;
    private boolean useGCOSPercentCutoff = false;
    
    private boolean useVarianceFilter = false;
    private Properties varianceFilterProps;

    private float lowerCY3Cutoff = 0f;
    private float lowerCY5Cutoff = 0f;
    private boolean useLowerCutoffs = false;
    
//wwang add for genepix filter
    private float genePixCutoff = 80f;
    private boolean useGenePixCutoffs = false;
    

    private Progress progressBar;
    private boolean normalizationAbort = false;

    // pcahan
    private DetectionFilter detectionFilter;
    private FoldFilter foldFilter;
    private boolean useDetectionFilter = false;
    private boolean dfSet = false;
    private boolean ffSet = false;
    private boolean useFoldFilter = false;

    private ClusterRepository geneClusterRepository;
    private ClusterRepository expClusterRepository;

    // Raktim 10/01. Changed access to protected for State SAving 
    int logState = LOG;

    private boolean isMedianIntensities = false;
    
    //fields for maintaining the 'experiment to use' statu
    private boolean useMainData = true;
    private Experiment alternateExperiment = null;

	MultipleArrayDataState mads;

	/* Stores annotation data that applies to the entire loaded dataset, such as species names and chip types. */
	IChipAnnotation chipAnnotation; 
	
	/**
     * List of all clones ordered by chromosome and then start position.
     * Raktim OCt 3, 2005
     */
    ArrayList clones = new ArrayList();
    /**
     * Start and stop index of each chromosome in clones ArrayList
     * Raktim OCt 3, 2005
     */
    int[][] chromosomeIndices = new int[24][];
    /**
     * CGH Order that the samples are displayed
     * Raktim Oct 3, 2005
     */
    int[] samplesOrder;
    /**
     * CGH private int cloneValueType and Distribution array;
     * Imported from Facade Class
     */
    int cloneValueType;
    Distribution[] cloneDistributions;
    CGHCopyNumberCalculator copyNumberCalculator;
    ICGHDataRegion[][] annotations = new ICGHDataRegion[0][0];
    /**
     * Raktim
     * Very Important CGH variable, imported from older FCD class
     * gets set in fcd.getRatio(..), ctl.OnShowBrowser(..), CGHViewer.OnExperimentsINitialized(..)
     */
    boolean hasDyeSwap = false;
    public boolean CGHData = false;
    public boolean log2Data = false;
    public int CGH_SPECIES = TMEV.CGH_SPECIES_Undef;
    public boolean hasCloneDistribution = false;
    
    
    /**
     * @author Sarita Nair
     * organismName and chipType added here to facilitate
     * gaggle and EASE get this information independently of the
     * Annotation model
     */
    public String gaggleOrganismName;
    
    //IDF object
   public MageIDF IDFObject=new MageIDF();
    
    public MultipleArrayData(){
    	mads = new MultipleArrayDataState();
    	this.chipAnnotation = new MevChipAnnotation();
    }
    /**
     * PersistenceDelegate constructor.  This constructor can be used to recreate a
     * previously-stored MultipleArrayData. 
     * 
     * @param useMainData
     * @param percentageCutoff
     * @param usePercentageCutoffs
     * @param useVarianceFilter
     * @param useDetectionFilter
     * @param useFoldFilter
     * @param dfSet
     * @param ffSet
     * @param df
     * @param ff
     * @param isMedianIntensities
     * @param useLowerCutoffs
     * @param lowerCY3Cutoff
     * @param lowerCY5Cutoff
     * @param experimentColors
     * @param spotColors
     * @param currentSampleLabelKey
     * @param featuresList
     * @param hasDyeSwap
     * @param CGHData
     * @param log2Data
     * @param cgh_Sp
     * @param hasCloneDistribution
     * @param clones
     */
    public MultipleArrayData(
    		Experiment experiment, 
    		Boolean useMainData, Experiment alternateExperiment, Float percentageCutoff, Boolean usePercentageCutoffs, 
			Boolean useVarianceFilter, Boolean useDetectionFilter, Boolean useFoldFilter,
			Boolean dfSet, Boolean ffSet, DetectionFilter df, FoldFilter ff, Boolean isMedianIntensities, 
			Boolean useLowerCutoffs, Float lowerCY3Cutoff, Float lowerCY5Cutoff, 
			ArrayList experimentColors, ArrayList spotColors, 
			String currentSampleLabelKey, ArrayList featuresList, Integer dataType,
			int[] samplesOrder, Boolean hasDyeSwap, Boolean CGHData, Boolean log2Data, ArrayList clones, Integer cgh_Sp, 
			MultipleArrayDataState mads){
    	this.experiment = experiment;
    	this.setFeaturesList(featuresList);
    	this.alternateExperiment = alternateExperiment;
    	this.useMainData = useMainData.booleanValue();
    	this.percentageCutoff = percentageCutoff.floatValue();
    	this.usePercentageCutoff = usePercentageCutoffs.booleanValue();
    	this.useVarianceFilter = useVarianceFilter.booleanValue(); 
        this.useDetectionFilter = useDetectionFilter.booleanValue();
        this.useFoldFilter = useFoldFilter.booleanValue();
        this.samplesOrder = samplesOrder;
        this.dfSet = dfSet.booleanValue();
        if(dfSet.booleanValue())
        	this.detectionFilter = df;
        this.ffSet = ffSet.booleanValue();
        if(ffSet.booleanValue())
        	this.foldFilter = ff;
        this.isMedianIntensities = isMedianIntensities.booleanValue();
        this.useLowerCutoffs = useLowerCutoffs.booleanValue();
        this.lowerCY3Cutoff = lowerCY3Cutoff.floatValue();
        this.lowerCY5Cutoff = lowerCY5Cutoff.floatValue();
        this.experimentColors = experimentColors;
        this.spotColors = spotColors;
        setSampleLabelKey(currentSampleLabelKey);
//        System.out.println("MAD Cons() currentSampleLabelKey: " + currentSampleLabelKey);
        try{
        	setDataType(dataType.intValue());
        } catch (Exception e){e.printStackTrace();}
        
        //Raktim 4/11. SS modifications
        this.hasDyeSwap = hasDyeSwap.booleanValue();
        this.CGHData = CGHData.booleanValue();
        this.log2Data = log2Data.booleanValue();
        this.clones = clones;
        this.CGH_SPECIES = cgh_Sp.intValue();
        loadMADS(mads);
    }
    
    public MultipleArrayData(
    		Experiment experiment, 
    		Boolean useMainData, Experiment alternateExperiment, Float percentageCutoff, Boolean usePercentageCutoffs, 
			Boolean useVarianceFilter, Boolean useDetectionFilter, Boolean useFoldFilter,
			Boolean dfSet, Boolean ffSet, DetectionFilter df, FoldFilter ff, Boolean isMedianIntensities, 
			Boolean useLowerCutoffs, Float lowerCY3Cutoff, Float lowerCY5Cutoff, 
			ArrayList experimentColors, ArrayList spotColors, 
			String currentSampleLabelKey, ArrayList featuresList, Integer dataType,
			int[] samplesOrder, Boolean hasDyeSwap, Boolean CGHData, Boolean log2Data, ArrayList clones, Integer cgh_Sp, 
			int[][] chromosomeIndices, 
			Integer cloneValueType, 
			//Integer logState,
			MultipleArrayDataState mads){
    		this(experiment, 
        		 useMainData,  alternateExperiment,  percentageCutoff,  usePercentageCutoffs, 
    			 useVarianceFilter,  useDetectionFilter,  useFoldFilter,
    			 dfSet,  ffSet,  df,  ff,  isMedianIntensities, 
    			 useLowerCutoffs,  lowerCY3Cutoff,  lowerCY5Cutoff, 
    			 experimentColors,  spotColors, 
    			 currentSampleLabelKey,  featuresList,  dataType,
    			 samplesOrder, hasDyeSwap, CGHData, log2Data, clones, cgh_Sp, mads);
        //Raktim 10/01. SS Modifications
//    		System.out.println("using Raktim's new MAD constructor");
        this.chromosomeIndices = chromosomeIndices;
        this.cloneValueType = cloneValueType.intValue();
        //this.logState = logState.intValue();
    }
    
    /**
     * State-saving constructor for MultipleArrayData. New as of 6/13/08. Simply calls the original
     * state-saving constructor and then sets the chipAnnotation object.
     * @param experiment
     * @param useMainData
     * @param alternateExperiment
     * @param percentageCutoff
     * @param usePercentageCutoffs
     * @param useVarianceFilter
     * @param useDetectionFilter
     * @param useFoldFilter
     * @param dfSet
     * @param ffSet
     * @param df
     * @param ff
     * @param isMedianIntensities
     * @param useLowerCutoffs
     * @param lowerCY3Cutoff
     * @param lowerCY5Cutoff
     * @param experimentColors
     * @param spotColors
     * @param currentSampleLabelKey
     * @param featuresList
     * @param dataType
     * @param samplesOrder
     * @param hasDyeSwap
     * @param CGHData
     * @param log2Data
     * @param clones
     * @param cgh_Sp
     * @param chromosomeIndices
     * @param cloneValueType
     * @param mads
     * @param chipAnnotation
     */
    public MultipleArrayData(
    		Experiment experiment, 
    		Boolean useMainData, Experiment alternateExperiment, Float percentageCutoff, Boolean usePercentageCutoffs, 
			Boolean useVarianceFilter, Boolean useDetectionFilter, Boolean useFoldFilter,
			Boolean dfSet, Boolean ffSet, DetectionFilter df, FoldFilter ff, Boolean isMedianIntensities, 
			Boolean useLowerCutoffs, Float lowerCY3Cutoff, Float lowerCY5Cutoff, 
			ArrayList experimentColors, ArrayList spotColors, 
			String currentSampleLabelKey, ArrayList featuresList, Integer dataType,
			int[] samplesOrder, Boolean hasDyeSwap, Boolean CGHData, Boolean log2Data, ArrayList clones, Integer cgh_Sp, 
			int[][] chromosomeIndices, 
			Integer cloneValueType, 
			//Integer logState,
			MultipleArrayDataState mads, IChipAnnotation chipAnnotation){
    	this(experiment, 
       		 useMainData,  alternateExperiment,  percentageCutoff,  usePercentageCutoffs, 
   			 useVarianceFilter,  useDetectionFilter,  useFoldFilter,
   			 dfSet,  ffSet,  df,  ff,  isMedianIntensities, 
   			 useLowerCutoffs,  lowerCY3Cutoff,  lowerCY5Cutoff, 
   			 experimentColors,  spotColors, 
   			 currentSampleLabelKey,  featuresList,  dataType,
   			 samplesOrder, hasDyeSwap, CGHData, log2Data, clones, cgh_Sp, chromosomeIndices, cloneValueType, mads);
    	setChipAnnotation(chipAnnotation);
    }
    
    /**
	 * @param mads2
	 */
	private void loadMADS(MultipleArrayDataState mads) {
    	this.mads = mads;
    	setMaxCy3(mads.getMaxCY3());
    	setMaxCy5(mads.getMaxCY5());
	}
	public MultipleArrayDataState getMultipleArrayDataState(){return mads;}
	
	/**
	 * Sets MultipleArrayData's IChipAnnotation object. If the input parameter is null, 
	 * a new, blank one is created.
	 */
	public void setChipAnnotation(IChipAnnotation annot) {
		if(annot == null)
			this.chipAnnotation = new MevChipAnnotation();
		this.chipAnnotation = annot;
	}
	/**
	 * Returns MultipleArrayData's IChipAnnotation object. If the current object is null, 
	 * a new, empty one is created and set, then returned. 
	 */
	public IChipAnnotation getChipAnnotation(){
		if(chipAnnotation == null)
			chipAnnotation = new MevChipAnnotation();
		return chipAnnotation;
	}
	

	
	/*
	 * Setter for Gaggle-specific organism name. 
	 */
	public void setGaggleOrganismName(String name) {
		this.gaggleOrganismName=name;	
	}	
	/*
	 * Getter for gaggle-specific organism name. If no gaggle-specific organism name
	 * has been set (by setGaggleOrganismName) then returns Annotation model's organism name.
	 */
	public String getGaggleOrganismName() {
		if(gaggleOrganismName != null)
			return gaggleOrganismName;
		else 
			return chipAnnotation.getSpeciesName();
	}
	

	
    /**
     *  Sets the data objects feature list
     */
    public void setFeaturesList(ArrayList list) {
        this.featuresList = list;
        for(int i=0; i<featuresList.size(); i++){
        	indicesList.add(createIndices((ISlideData)featuresList.get(i)));
        }
        updateSpotColors();
        updateExperimentColors();
        this.experiment = this.createExperiment();
    }
 
    /**
     * Sets the geneClusterRepository
     */
    public void setGeneClusterRepository(ClusterRepository rep){
        this.geneClusterRepository = rep;
    }

    /**
     * Sets the experimentClusterRepository
     */
    public void setExperimentClusterRepository(ClusterRepository rep){
        this.expClusterRepository = rep;
    }


    /**
     * Sets the main data marker
     */    
    public void setUseMainData(boolean useMainDataSelection) {
        this.useMainData = useMainDataSelection;
        if(this.useMainData)
            this.alternateExperiment = null;
    }
    public boolean getUseMainData() {
    	return useMainData;
    }
    /**
     * Set alternate experiment
     */
    public void setAlternateExperiment(Experiment e) {
        this.alternateExperiment = e;
    }
    /**
    * EH - used by MulipleArrayViewer to get AlternateExperiment
    * so it can be stored in a saved state file
    */
    public Experiment getAlternateExperiment() {
    	return alternateExperiment;
    }

    /**
     * Raktim 10/01
     * For State SAving
     */
    public void setLogState(int state){
    	logState = state;
    }
    
    public int getLogState(){
    	return logState;
    }
    
    public void constructAndSetAlternateExperiment(Experiment coreExperiment, int [] clusterIndices, int clusterType) {
        int [] origRowIndices = coreExperiment.getRowMappingArrayCopy();
        int [] origColIndices = coreExperiment.getColumnIndicesCopy();
        FloatMatrix coreMatrix = coreExperiment.getMatrix();
        
        FloatMatrix newMatrix;
        int [] newRowIndices;
        int [] newColIndices;
        
        if(clusterType == Cluster.GENE_CLUSTER) {
            newMatrix = new FloatMatrix(clusterIndices.length, origColIndices.length);
            newRowIndices = new int[clusterIndices.length];
            
            for(int row = 0; row < clusterIndices.length; row++) {
                for(int col = 0; col < origColIndices.length; col++) {
                    newMatrix.set(row, col, coreMatrix.get(clusterIndices[row], col));                    
                }                
                newRowIndices[row] = origRowIndices[clusterIndices[row]];
            }
            //EH
            setAlternateExperiment(new Experiment(newMatrix, origColIndices, newRowIndices));
            this.useMainData = false;
            
        } else {
            
            newMatrix = new FloatMatrix(origRowIndices.length, clusterIndices.length);
            newColIndices = new int[clusterIndices.length];
            int colCount = 0;
            
            for(int col = 0; col < clusterIndices.length; col++) {
                for(int row = 0; row < origRowIndices.length; row++) {
                    newMatrix.set(row, col, coreMatrix.get(row, clusterIndices[col]));
                }
                newColIndices[col] = origColIndices[clusterIndices[col]];
            }
            //EH
            setAlternateExperiment(new Experiment(newMatrix, newColIndices, origRowIndices));
            this.useMainData = false;
        }
    
    }
    
    /**
     * Returns number of loaded microarrays.
     */
    public int getFeaturesCount() {
        return featuresList.size();
    }

    /**
     * Returns a size of first loaded microarray.
     */
    public int getFeaturesSize() {
        if (featuresList.size() == 0) {
            return 0;
        }
        return((ISlideData)featuresList.get(0)).getSize();
    }
    /**
     * Raktim. For CGH Functions
     * Returns a reference to the data objects feature list
     * Also used for state-saving.
     */
    public ArrayList getFeaturesList() {
        return this.featuresList;
    }

    /**
     * Returns the percentage cutoff value.
     */
    public float getPercentageCutoff() {
        return percentageCutoff;
    }

    /**
     * Sets a percentage cutoff value.
     */
    public void setPercentageCutoff(float value) {
        percentageCutoff = value;
        if (isPercentageCutoff()) {
            this.experiment = createExperiment();
        }
    }
    public void setPvaluePercentageCutoff(float value) {
        percentageCutoff = value;
        if (isPvaluePercentageCutoff()) {
            this.experiment = createExperiment();
        }
    }
    
    public void setPresenCallCutoff(float value) {
        percentageCutoff = value;
        if (isPresentCallCutoff()) {
        	//System.out.print("hooooo");
            this.experiment = createExperiment();
        }
    }
    
    
    public void setVarianceFilter(Properties props) {
        this.varianceFilterProps = props; 
        if(props.getProperty("Filter Enabled").equals("true"))
            this.useVarianceFilter = true;
        else
            this.useVarianceFilter = false;            
        this.experiment = createExperiment();
    }

    /**
     * Sets a use percentage cutoff value.
     */
    public void setUsePercentageCutoff(boolean value) {
        if (usePercentageCutoff == value) {
            return;
        }   
        usePercentageCutoff = value;
        this.experiment = createExperiment();
    }
    
    public void setUsePvaluePercentageCutoff(boolean value) {
        if (usePvaluePercentageCutoff == value) {
            return;
        }   
        usePvaluePercentageCutoff = value;
        this.experiment = createExperiment();
    }
    public void setUsePresentCutoff(boolean value) {
        if (usePresentCallCutoff == value) {
            return;
        }
        usePresentCallCutoff = value;
        this.experiment = createExperiment();
    }
 
    /**
     *wwang
     * Sets a use percentage cutoff value.
     */
    
    public void setUseGCOSPercentageCutoff(boolean value) {
        if (useGCOSPercentCutoff == value) {
            return;
        }
        useGCOSPercentCutoff = value;
        this.experiment = createExperiment();
    }
    public void setUseGenePixCutoff(boolean value) {
        if (useGenePixCutoffs == value) {
            return;
        }
        useGenePixCutoffs = value;
        this.experiment = createExperiment();
    }
    //pcahan for affy ********************************************

    /**
     * Sets a use detection Filter value.
     */
    public void setUseDetectionFilter(boolean value) {
        if (useDetectionFilter == value) {
            return;
        }
        useDetectionFilter = value;
        this.experiment = createExperiment();
    }

    public void setUseFoldFilter(boolean value) {
        if (useFoldFilter == value) {
            return;
        }
        useFoldFilter = value;
        this.experiment = createExperiment();
    }

    public void setdfSet(boolean b) {
        dfSet = b;
    }

    public boolean getdfSet(){
        return dfSet;
    }

    public void setffSet(boolean b) {
        ffSet = b;
    }

    public boolean getffSet(){
        return ffSet;
    }

    /**
     * Returns the use  DetectionFilter.
     */
    public boolean isDetectionFilter() {
        return useDetectionFilter;
    }

    public boolean isFoldFilter() {
        return useFoldFilter;
    }
  
    public boolean isGenePixFilter() {
        return useGenePixCutoffs;
    }
    /**
     * Returns the detection filter. Change from bool -> detection filter class.
     */
    public DetectionFilter getDetectionFilter() {
        return detectionFilter;
    }

    public FoldFilter getFoldFilter() {
        return foldFilter;
    }

    /**
     * Sets the detection filter values.
     *
     * public void setDetectionFilter(boolean use_filter) {
     * detectionFilter = use_filter;
     * if (isDetectionFilter()) {
     * this.experiment = createExperiment();
     * }
     * }
     */
    public void setDetectionFilter(DetectionFilter filter) {
        detectionFilter = filter;
        if (isDetectionFilter()) {
            this.experiment = createExperiment();
        }
    }
    //add by wwang
    public void setGenePixCutoff(float value){
    	useGenePixCutoffs=true;
    	percentageCutoff = value;
    	if(isGenePixFilter())
    	 this.experiment = createExperiment();
    }
    public void setFoldFilter(FoldFilter filter) {
        foldFilter = filter;
        if (isFoldFilter()) {
            this.experiment = createExperiment();
        }
    }

        /*
     pcahan
     Also want for affy data, want to retrieve detection for each chip*/
    public String getDetection(int column, int row) {
        if (featuresList.size() == 0) {
            return "";
        }

        return ((ISlideData)(featuresList.get(column))).getDetection(row);        
    }

    // end affy specific methods ********************************************

    //wwang add for p-value filter
    public float getPvalue(int column, int row) {
        if (featuresList.size() == 0) {
            return 0.0f;
        }
        return ((ISlideData)(featuresList.get(column))).getPvalue(row);
    }
    public int getGenePixFlags(int column, int row) {
        if (featuresList.size() == 0) {
            return 0;
        }

        return ((ISlideData)(featuresList.get(column))).getGenePixFlags(row);        
    }
    /**
     * Returns the use percentage cutoff value.
     */
    public boolean isPercentageCutoff() {
        return usePercentageCutoff;
    }
    public boolean isPvaluePercentageCutoff() {
        return usePvaluePercentageCutoff;
    }  
    /**
     * Returns the use percentage cutoff value.
     */
    public boolean isPresentCallCutoff() {
        return usePresentCallCutoff;
    }
     
    /**
     * Returns the use percentage cutoff value.
     */
    public boolean isGCOSPercentCutoff() {
        return useGCOSPercentCutoff;
    }
       
    /**
     * Returns the use percentage cutoff value.
     */
    public boolean isVarianceFilter() {
        return useVarianceFilter;
    }

    /**
     * Returns the lower CY3 cutoff value.
     */
    public float getLowerCY3Cutoff() {
        return lowerCY3Cutoff;
    }

    /**
     * Sets marker for median intensities
     */
    public void setMedianIntensities(boolean areMedians) {
        this.isMedianIntensities = areMedians;
    }

    /**
     * Returns true if intensities are median intensities
     */
    public boolean areMedianIntensities() {
        return isMedianIntensities;
    }

    /**
     * Sets the lower cutoff values.
     */
    public void setLowerCutoffs(float lowerCY3, float lowerCY5) {
        lowerCY3Cutoff = lowerCY3;
        lowerCY5Cutoff = lowerCY5;
        if (isLowerCutoffs()) {
            this.experiment = createExperiment();
        }
    }

    /**
     * Returns the lower CY5 cutoff value.
     */
    public float getLowerCY5Cutoff() {
        return lowerCY5Cutoff;
    }

    /**
     * Sets the use lower cutoff attribute flag.
     */
    public void setUseLowerCutoffs(boolean value) {
        if (useLowerCutoffs == value) {
            return;
        }
        useLowerCutoffs = value;
        this.experiment = createExperiment();
    }

    /**
     * Return the use lower cutoff flag.
     */
    public boolean isLowerCutoffs() {
        return useLowerCutoffs;
    }

    /**
     * Returns CY3 value for specified row and column.
     */
    public float getCY3(int column, int row) {
        ISlideData slideData = (ISlideData)featuresList.get(column);
        return slideData.getCY3(row);
    }

    /**
     * Returns CY3 value for specified row and column.
     */
    public float getCY5(int column, int row) {
        ISlideData slideData = (ISlideData)featuresList.get(column);
        return slideData.getCY5(row);
    }

    /**
     * Returns ratio value for specified row, column and log state.
     */
    public float getRatio(int column, int row, int logState) {
        ISlideData slideData = (ISlideData)featuresList.get(column);
        logState = this.logState;  //set to current log state
        return slideData.getRatio(row, logState);
    }

    /**
     * Returns a microarray name for specified column.
     */
    public String getSampleName(int column) {
        return((ISlideData)featuresList.get(column)).getSlideDataName();
    }

    /** Sets the experiment label index for the collection of features
     */
    public void setSampleLabelKey(String key) {
        for(int i = 0; i < featuresList.size(); i++) {
            ((ISlideData)featuresList.get(i)).setDataLabelKey(key);
        }
    }

    /**
     * Returns full feature name.
     */
    public String getFullSampleName(int column) {
        return((ISlideData)featuresList.get(column)).getFullSlideDataName();
    }

    public Vector getSlideDataNameKeys(int column) {
        return((ISlideData)featuresList.get(column)).getSlideDataKeys();
    }

    /**
     * Returns the key vector for the sample with the longest sample name key list
     */
    public Vector getSlideNameKeyVectorUnion() {
    	
        Vector keyVector;
        Vector fullKeyVector = new Vector();
        String key;
        for( int i = 0; i < featuresList.size(); i++) {
            keyVector = ((ISlideData)featuresList.get(i)).getSlideDataKeys();
            for(int j = 0; j < keyVector.size(); j++) {
                key = (String)(keyVector.elementAt(j));
                if(!fullKeyVector.contains(key))
                    fullKeyVector.addElement(key);
            }
        }
        return fullKeyVector;
    }


        /**
     * Returns the key vector for the sample with the longest sample name key list
     */
    public Vector getSampleAnnotationFieldNames() {
        Vector keyVector;
        Vector fullKeyVector = new Vector();
        String key;
        for( int i = 0; i < featuresList.size(); i++) {
            keyVector = ((ISlideData)featuresList.get(i)).getSlideDataKeys();
            for(int j = 0; j < keyVector.size(); j++) {
                key = (String)(keyVector.elementAt(j));
                if(!fullKeyVector.contains(key))
                    fullKeyVector.addElement(key);
            }
        }
        return fullKeyVector;
    }
    
    /**
     * Returns the key vector for the sample with the longest sample name key list
     */
    public String [] getSlideNameKeyArray() {
        Vector keyVector;
        Vector fullKeyVector = new Vector();
        String key;
        for( int i = 0; i < featuresList.size(); i++) {
            keyVector = ((ISlideData)featuresList.get(i)).getSlideDataKeys();
            for(int j = 0; j < keyVector.size(); j++) {
                key = (String)(keyVector.elementAt(j));
                if(!fullKeyVector.contains(key))
                    fullKeyVector.addElement(key);
            }
        }

        String [] keys = new String[fullKeyVector.size()];
        for(int i = 0 ; i < keys.length; i++) {
            keys[i] = (String)(fullKeyVector.elementAt(i));
        }
        return keys;
    }


    public void addNewExperimentLabel(String key, String [] values) {
        ISlideData slideData;

        for(int i = 0; i < featuresList.size(); i++) {
            getFeature(i).addNewSampleLabel(key, values[i]);
        }
    }
    
    /**
     * Adds sample labels from <code>file</code>.  
     * @param parent the parent frame
     * @param file the text file containing the labels to associate with samples
     * @return true if successful
     * @throws IOException if there is something wrong with <code>file</code>
     */
    public boolean addNewSampleLabels(Frame parent, File file) throws IOException {
		int sampleCount = this.getFeaturesCount();
		String line;        
       BufferedReader reader = new BufferedReader(new FileReader(file));
		StringTokenizer stok;
		boolean readingFirstRow = true;
		String [] annKeys = null; //a list of the headers for each column in file
		String[] annotationRow;
		Vector data = new Vector();
		int fieldCount;
		int annCnt = 0;    
		int rowCnt = 0;
		Hashtable annotation = new Hashtable();
		String fileName;
		
		line=reader.readLine();
		stok = new StringTokenizer(line, "\t");
		annKeys = new String[stok.countTokens()];
		for(int i = 0; i < annKeys.length; i++) {
			annKeys[i] = stok.nextToken();
		}
		fieldCount = annKeys.length;              

		while( (line = reader.readLine()) != null) {
			stok = new StringTokenizer(line, "\t");
			annCnt = 0;
			annotationRow = new String[fieldCount];
			while(stok.hasMoreTokens()) {
				annotationRow[annCnt] = stok.nextToken();
				annCnt++;
			}
			annotation.put(annotationRow[0], annotationRow);
			data.add(annotationRow);
			rowCnt++;
		}
		
		String thisSlideFileName;
		String thisAnnotationValue;
		for(int i = 0; i < featuresList.size(); i++) {
			ISlideData thisSlide = getFeature(i);
			thisSlideFileName = thisSlide.getSlideFileName();
			thisSlideFileName = thisSlide.getSlideFileName().substring(thisSlideFileName.lastIndexOf(System.getProperty("file.separator"))+1);
			if(annotation.containsKey(thisSlideFileName)) {
				for(int j=0; j< annKeys.length; j++) {
					thisAnnotationValue = ((String[])annotation.get(thisSlideFileName))[j];
					thisSlide.addNewSampleLabel(annKeys[j], thisAnnotationValue);
				}
			} else {
				System.out.println("No annotation for " + thisSlideFileName);
			}
		}
		return true;
	}    
    
    /** Adds new gene annotation present in the annMatrix, Note: annMatrix contains headers with field names
     */
    public int addNewGeneAnnotation(String [][] annMatrix, String dataKey, String annFileKey, String [] fieldsToAppend) {

        int updateCount = 0;

        //get annFile key column number
        int keyCol;
        for(keyCol = 0; keyCol < annMatrix[0].length; keyCol++){
            if(annMatrix[0][keyCol].equals(annFileKey))
                break;
        }


        if(keyCol > annMatrix[0].length)
            return updateCount;
      
        //get current field index for datakey
        String [] fieldNames = getFieldNames();
        int dataKeyCol = -1;
        if(!dataKey.equals("UID")) {
            for(dataKeyCol = 0; dataKeyCol < fieldNames.length; dataKeyCol++) {
                if(dataKey.equals(fieldNames[dataKeyCol]))
                    break;
            }
        }
        
        if(dataKeyCol > fieldNames.length)
            return updateCount;

        
            /*
             *  Probably want to make the hash contain only fields that are 
             *  specified, maybe pass in String [] fieldsToAppend             
             */
                        
        //build hash of annFileKeys and String [] rows
        Hashtable annotationHash = new Hashtable();
        
        // appending all annotation fields
        if(fieldsToAppend.length == annMatrix[0].length) {            
            for(int row = 1; row < annMatrix.length; row++) {
                annotationHash.put(annMatrix[row][keyCol], annMatrix[row]);
            }
        } else {
        
            //Just build a hash with selected fields
            Vector testVector = new Vector();
            
            for(int i = 0; i < fieldsToAppend.length;i++)
                testVector.add(fieldsToAppend[i]);
            
            int subsetIndex = 0;
            String [] annSubset;
            for(int row = 1; row < annMatrix.length; row++) {
                subsetIndex = 0;
    
                annSubset = new String[fieldsToAppend.length];
                
                for(int fieldIndex = 0; fieldIndex < annMatrix[0].length; fieldIndex++)
                    if(testVector.contains(annMatrix[0][fieldIndex])) {
                        annSubset[subsetIndex] = annMatrix[row][fieldIndex];
                        subsetIndex++;
                    }
                
                annotationHash.put(annMatrix[row][keyCol], annSubset);
            }            
        }
        
        
        //loop through IData to get ISlideDataElements, get dataKey, get values from hash to apppend to sde
        //if hash returns null append "" for each field.
        ISlideData slideData = (ISlideData)getSlideMetaData();
        int rows = getFeaturesSize();
        ISlideDataElement sde;
        String dataID = "";
        String [] newFields;
        
        //check for id matches, and count updates
        for(int row = 0; row < rows; row++) {
            //get sde
            sde = slideData.getSlideDataElement(row);
            
            //get the data key
            if(dataKeyCol == -1) //use UID
                dataID = sde.getUID();
            else {
//                dataID = sde.getFieldAt(dataKeyCol);
                dataID = getElementAnnotation(row, dataKey)[0];
            }
                
            if(annotationHash.containsKey(dataID))
                updateCount++;
        }

        if(updateCount > 0) {
            for(int row = 0; row < rows; row++) {
                //get sde
                sde = slideData.getSlideDataElement(row);
                
                //get the data key
                if(dataKeyCol == -1) //use UID
                    dataID = sde.getUID();
                else
                    dataID = sde.getFieldAt(dataKeyCol);
                
                
                newFields = (String []) annotationHash.get(dataID);
                
                if(newFields != null) {
                    sde.setExtraFields(newFields);
                    updateCount++;
                } else {
                    sde.setExtraFields(new String[annMatrix[0].length]);
                }
            }
        }
        
        return updateCount;
    }

    /**
     * Added by Sarita Nair
     * 
     * 
     * 
     * 
     * 
     */
    
    public void addResourcererGeneAnnotation(String dataKey, Hashtable annotationHash) {

        int updateCount = 0;

        //get annFile key column number
       
        
        //get current field index for datakey
        String [] fieldNames = getFieldNames();
        int dataKeyCol = -1;
        
            for(dataKeyCol = 0; dataKeyCol < fieldNames.length; dataKeyCol++) {
                if(dataKey.equals(fieldNames[dataKeyCol]))
                    break;
            }
            /*
             *  Probably want to make the hash contain only fields that are 
             *  specified, maybe pass in String [] fieldsToAppend             
             */
                        
        //build hash of annFileKeys and String [] rows
       
     
        
        //loop through IData to get ISlideDataElements, get dataKey, get values from hash to apppend to sde
        //if hash returns null append "" for each field.
        ISlideData slideData = (ISlideData)getSlideMetaData();
        int rows = getFeaturesSize();
        ISlideDataElement sde;
        String dataID = "";
        String [] newFields;
        
        //check for id matches, and count updates
        for(int row = 0; row < rows; row++) {
            //get sde
            sde = slideData.getSlideDataElement(row);
            
//            dataID = sde.getFieldAt(dataKeyCol);
            dataID = getElementAnnotation(row, dataKey)[0];
             String cloneName = dataID;
             //System.out.println("cloneName:"+cloneName);
                if(annotationHash.size()!=0) {
             	   
             	
                	if(((MevAnnotation)annotationHash.get(cloneName))!=null) {
                		MevAnnotation mevAnno = (MevAnnotation)annotationHash.get(cloneName);
                		
                		
                	     sde.setElementAnnotation(mevAnno);
                		
                	}else {
                /**
               	  * Sarita: clone ID explicitly set here because if the data file
               	  * has a probe (for eg. Affy house keeping probes) for which Resourcerer
               	  * does not have annotation, MeV would still work fine. NA will be
               	  * appended for the rest of the fields. 
               	  * 
               	  * 
               	  */
                	MevAnnotation mevAnno = new MevAnnotation();
                	mevAnno.setCloneID(cloneName);
                    sde.setElementAnnotation(mevAnno);
                	
                }
                }
                
                
             }
        this.setAnnotationLoaded(true);
    	slideData.getSlideMetaData().updateFilledAnnFields();

        }
        
    /**
     * Returns an element attribute for specified row and attribute index. If 
     * more than one value is available for this annotation/gene combination, 
     * the values are concatenated into a single "///"-delimited string.
     * @deprecated use String[] getElementAnnotation(int row, String attr) instead
     */
    public String getElementAttribute(int row, int attr) {
	    String[] allResults = getElementAnnotation(row, getFieldNames()[attr]);
	    String annotationConcatenated;
	    if(allResults == null || allResults.length < 1)
		    annotationConcatenated = "NA";
	    else 
		    annotationConcatenated = allResults[0];
	    for(int i=1; i<allResults.length; i++) {
		    annotationConcatenated += "///" + allResults[i];
        }
	    return annotationConcatenated;
    }
    
    /**
     * Raktim - Annotation Model Method
     */
    public String[] getElementAnnotation(int row, String attr) {
	    return ((ISlideData)featuresList.get(0)).getSlideMetaData().getAnnotationValue(row, attr);
        }
    public String[][] getElementAnnotation(int[] rows, String attr) {
    	String[][] _temp = new String[rows.length][];
    	for(int i=0; i<_temp.length; i++) {
    		_temp[i] = ((ISlideData)featuresList.get(0)).getSlideMetaData().getAnnotationValue(rows[i], attr);
    	}
    	return _temp;
    }
      
    /**
     * Raktim - Annotation Model Method
     */
    public AnnoAttributeObj getElementAnnotationObject(int row, String attr) {
    	if (featuresList.size() == 0) {
            return null;
        }
        ISlideData slideData = (ISlideData)featuresList.get(0);
        return slideData.getSlideMetaData().getAnnotationObj(row, attr);
	}
    
    /**
     * Returns a gene unique id.
     */
    public String getUniqueId(int row) {
        return getElementAttribute(row, TMEV.getUniqueIDIndex());
    }

    /**
     * Returns a gene name.
     */
    public String getGeneName(int row) {
        return getElementAttribute(row, TMEV.getNameIndex());
    }

    /**
     * 
     * Returns all annotation fields
     */
    public String[] getFieldNames() {
    	return ((ISlideData)featuresList.get(0)).getSlideMetaData().getFieldNames();
    }
    
    /**
     * Returns a combination of annotation fields from original annotation model and new model,
     * excluding annotation fields from the new model that have no values loaded.
     * @return the list of fieldnames
     * @deprecated
     */
    public String[] getAllFilledAnnotationFields() {
	    return ((ISlideData)featuresList.get(0)).getSlideMetaData().getFieldNames(); 
	    }

    /**
     * Returns a spot base row.
     */
    public int getProbeRow(int column, int row) {
        ISlideMetaData meta = getFeature(column).getSlideMetaData();
        return meta.getRow(row);
    }

    /**
     * Returns a spot base column.
     */
    public int getProbeColumn(int column, int row) {
        ISlideMetaData meta = getFeature(column).getSlideMetaData();
        return meta.getColumn(row);


    }
    /**
     * Returns an array of Colors associated with every cluster that gene "index" belongs to
     */
    public Color[] getGeneColorArray(int index) {
    	return geneClusterRepository.getColors(index);
    }
    /**
     * Returns an array of Colors associated with every cluster that sample "index" belongs to
     */
    public Color[] getSampleColorArray(int index) {
    	return expClusterRepository.getColors(index);
    }
    /**
     * Returns a boolean for whether two clusters represented by colors have overlapping genes
     */
    public boolean isColorOverlap(int index, Color color, Color c, boolean isGeneCR){
    	if (isGeneCR)
    	return geneClusterRepository.isColorOverlap(index, color, c);
    	return expClusterRepository.isColorOverlap(index, color, c);
    }
    /**
     * Returns the name of a cluster
     */
    public String getClusterLabel(int index, boolean gene){
    	if (gene){
//    		System.out.println("geneClusterRepository = "+geneClusterRepository);
//    		System.out.println("geneClusterRepository.getCluster("+index+") = "+geneClusterRepository.getCluster(index));
	    	if (geneClusterRepository==null) return null;
	    	if (geneClusterRepository.getCluster(index)==null) return null;
	    	return geneClusterRepository.getCluster(index).getClusterLabel();
    	}
    	else{
    		if (expClusterRepository==null) return null;
	    	if (expClusterRepository.getCluster(index)==null) return null;
    		return expClusterRepository.getCluster(index).getClusterLabel();
    	}
    }
    /**
     * Returns the number of clusters visible
     */
    public int getVisibleClusters(){
    	if (geneClusterRepository==null) return 0;
    	return geneClusterRepository.getVisibleClusters();
    }
    /**
     * Returns the index of a cluster of a given color
     */
    public int getVisibleCluster(Color color, boolean gene){
    	if (gene){
    		if (geneClusterRepository==null) return 0;
    		int i=0;  
    		while (true){
    			i++;
    			if (geneClusterRepository.getCluster(i)==null)
    				continue;
    			if( geneClusterRepository.getCluster(i).getClusterColor()==color)
    				break;
    			
    			if (i>geneClusterRepository.getClusterSerialCounter()) 
    				return -1;
    		}
    		return i;
    	}else{
    		if (expClusterRepository==null) return 0;
    		int i=0;  
    		while (true){
    			i++;
    			if (expClusterRepository.getCluster(i)==null)
    				continue;
    			if (expClusterRepository.getCluster(i).getClusterColor()==color)
    				break;
    			if (i>expClusterRepository.getClusterSerialCounter()) 
    				return -1;
    		}
    		return i;
    	}
    }

    /**
     * Returns array of published colors.
     */
    public Color[] getColors() {
        updateSpotColors();
        Color[] colors = new Color[spotColors.size()];
        return(Color[])spotColors.toArray(colors);
    }
    /**
    * EH - added for state-saving. May not be necessary
    */
    public ArrayList getSpotColors() {
    	return spotColors;
    }
    /**
    * EH - added for state-saving. May not be necessary
    */
    public void setSpotColors(ArrayList sp) {
    	this.spotColors = sp;
    }

    /**
     * Returns a spot public color by specified row.
     */
    public Color getProbeColor(int row) {
        if(this.geneClusterRepository == null)
            return null;
        return this.geneClusterRepository.getColor(row);
    }

    public void updateSpotColors(){
        this.colorIndices = new int[this.getFeaturesSize()];
        spotColors = new ArrayList();
        Color color;
        int index;
        int count = 0;
        for( int i = 0; i < colorIndices.length ; i++){
            //  index = null;
            if(geneClusterRepository == null)
                color = null;
            else
                color = geneClusterRepository.getColor(i);
            if(color == null){
                colorIndices[i] = -1;
            } else{
                index = spotColors.indexOf(color);
                if(index < 0){
                    spotColors.add(color);
                    this.colorIndices[i] = count;
                    count++;
                } else {
                    this.colorIndices[i] = index;
                }
            }
        }
    }

    public void updateExperimentColors(){
        this.experimentColorIndices = new int[this.getFeaturesCount()];
        this.experimentColors = new ArrayList();
        Color color;
        int index;
        int count = 0;
        for( int i = 0; i < experimentColorIndices.length ; i++){
            if(expClusterRepository == null)
                color = null;
            else
                color = expClusterRepository.getColor(i);
            if(color == null){
                experimentColorIndices[i] = -1;
            } else{
                index = experimentColors.indexOf(color);
                if(index < 0 ){
                    experimentColors.add(color);
                    this.experimentColorIndices[i] = count;
                    count++;
                }
                else{
                    this.experimentColorIndices[i] = index;
                }
            }
        }
    }

    public int [] getColorIndices(){
        this.updateSpotColors();
        return this.colorIndices;
    }

    public int [] getExperimentColorIndices(){
        this.updateExperimentColors();
        return this.experimentColorIndices;
    }

    /**
    * EH - added for state-saving
    */
    public void setColorIndices(int[] ci) {
    	colorIndices = ci;
    }
    /**
    * EH - added for state-saving
    */
    public void setExperimentColorIndices(int[] eci) {
    	experimentColorIndices = eci;
    }
    
    /**
     * Sets a spot public color for specified rows.
     */
    public void setProbesColor(int[] rows, Color color) {
        int colorIndex;
        if (color == null) {
            colorIndex = -1;
        } else {
            colorIndex = spotColors.indexOf(color);
            if (colorIndex < 0) {
                colorIndex = spotColors.size();
                spotColors.add(color);
            }
        }
        for (int i=0; i<rows.length; i++) {
            this.colorIndices[rows[i]] = colorIndex;
        }
        removeUnusedColors();
    }

    /**
     * Returns index of the public color for specified row.
     */
    public int getProbeColorIndex(int row) {
        return colorIndices[row];
    }

    /**
     * Returns count of rows which have public color index equals to colorIndex.
     */
    public int getColoredProbesCount(int colorIndex) {
        int count = 0;
        for (int i=0; i<colorIndices.length; i++) {
            if (colorIndices[i] == colorIndex) {
                count++;
            }
        }
        return count;
    }


    public Color getExperimentColor(int index){
        if(this.expClusterRepository == null){
            return null;
        }
        return this.expClusterRepository.getColor(index);
    }


    public void setExperimentColor(int [] indices, Color color){
        if(this.experimentColorIndices == null){
            this.experimentColorIndices = createExperimentColorIndices();
        }

        int colorIndex;
        if (color == null) {
            colorIndex = -1;
        } else {
            colorIndex = experimentColors.indexOf(color);
            if (colorIndex < 0) {
                colorIndex = experimentColors.size();
                experimentColors.add(color);
            }
        }
        for (int i=0; i<indices.length; i++) {
            this.experimentColorIndices[indices[i]] = colorIndex;
        }
        removeUnusedExperimentColors();
    }

	/**
	* EH - added for state-saving
	*/
    public void setExperimentColors(Color[] ec) {
    	ArrayList al = new ArrayList(ec.length);
    	for(int i=0; i<al.size(); i++) {
    		al.set(i, ec[i]);
    	}
    	this.experimentColors = al;
    }

    /**
     * Returns count of columns which have public color index equals to colorIndex.
     */
    public int getColoredExperimentsCount(int colorIndex) {
        int count = 0;
        if(this.experimentColorIndices == null){
            this.experimentColorIndices = createExperimentColorIndices();
        }
        for (int i=0; i<experimentColorIndices.length; i++) {
            if (experimentColorIndices[i] == colorIndex) {
                count++;
            }
        }
        return count;
    }

    /**
     * Returns array of published experiment colors.
     */
    public Color[] getExperimentColors() {
        this.updateExperimentColors();
        Color[] colors = new Color[experimentColors.size()];
        return(Color[])experimentColors.toArray(colors);
    }

    /**
     * Returns index of the public color for specified column.
     */
    public int getExperimentColorIndex(int col) {
        if(this.experimentColorIndices == null){
            this.experimentColorIndices = createExperimentColorIndices();
        }
        return experimentColorIndices[col];
    }

    public void clearExperimentColor(int index){
        if(index > this.experimentColorIndices.length)
            return;
        this.experimentColors.remove(this.experimentColorIndices[index]);
        this.experimentColorIndices[index] = -1;
    }

    public void clearExperimentColors(int [] indices){
        for(int i = 0 ; i < indices.length ; i++){
            clearExperimentColor(indices[i]);
        }
    }

    public void deleteExperimentColors(){
        // reinit colors state
        experimentColors.clear();
        experimentColorIndices = createExperimentColorIndices();
    }

    private int[] createExperimentColorIndices(){
        int [] indices = new int[featuresList.size()];
        for(int i = 0; i < featuresList.size(); i++){
            indices[i] = -1;
        }
        return indices;
    }

    /**
     * Removes all does'nt used colors.
     */
    private void removeUnusedColors() {
        boolean unused;
        final int size = spotColors.size();
        for (int i = size; --i >= 0;) {
            unused = true;
            for (int j = colorIndices.length; --j >= 0;) {
                if (colorIndices[j] == i) {
                    unused = false;
                    break;
                }
            }
            if (unused) {
                spotColors.remove(i);
                // adjust indices
                for (int j = colorIndices.length; --j >= 0;) {
                    if (colorIndices[j] > i) {
                        colorIndices[j]--;
                    }
                }
            }
        }
    }

    private void removeUnusedExperimentColors(){
        boolean unused;
        final int size = experimentColors.size();
        for (int i = size; --i >= 0;) {
            unused = true;
            for (int j = experimentColorIndices.length; --j >= 0;) {
                if (experimentColorIndices[j] == i) {
                    unused = false;
                    break;
                }
            }
            if (unused) {
                experimentColors.remove(i);
                // adjust indices
                for (int j = experimentColorIndices.length; --j >= 0;) {
                    if (experimentColorIndices[j] > i) {
                        experimentColorIndices[j]--;
                    }
                }
            }
        }
    }

    /**
     * Delete all the published colors.
     */
    public void deleteColors() {
        // reinit colors state
        spotColors.clear();
        if (colorIndices != null) {
            colorIndices = createColorIndices(colorIndices.length);
        }
    }


    /**
     * Creates an array of ordered integers.
     */
    private int[] createIndices(ISlideData slideData) {
        int[] indices = new int[slideData.getSize()];
        for (int i=0; i<indices.length; i++) {
            indices[i] = i;
        }
        return indices;
    }

    /**
     * Adds a microarray data.
     */
    void addFeature(ISlideData slideData) {
        featuresList.add(slideData);
        slideData.setDataType(this.dataType);
        indicesList.add(createIndices(slideData));
        this.experiment = createExperiment();
        if (this.colorIndices == null) {
            this.colorIndices = createColorIndices(slideData.getSize());
        }
        updateMaxValues(slideData);
    }

    /**
     * Adds an array of microarrays data.
     */
    void addFeatures(ISlideData[] slideData) {
	slideData[0].getSlideMetaData().updateFilledAnnFields();
        for (int i = 0; i < slideData.length; i++) {
            featuresList.add(slideData[i]);
            slideData[i].setDataType(this.dataType);
            indicesList.add(createIndices(slideData[i]));
            updateMaxValues(slideData[i]);
        }
        this.experiment = createExperiment();
        if (this.colorIndices == null) {
            this.colorIndices = createColorIndices(slideData[0].getSize());
        }
    }

    /**
     * Creates an array of color indices.
     */
    private int[] createColorIndices(int size) {
        int[] indices = new int[size];
        for (int i=0; i<size; i++) {
            indices[i] = -1;
        }
        return indices;
    }

    /**
     * Updates the data CY3 and CY5 max values.
     */
    private void updateMaxValues(ISlideData slideData) {
        float value;
        value = slideData.getMaxCY3();
        if (value > maxCy3) {
            setMaxCY3(value);
        }
        value = slideData.getMaxCY5();
        if (value > maxCy5) {
            setMaxCY5(value);
        }
        updateMaxMinRatios(slideData);
    }

    /**
     * Returns a meta data.
     */
    ISlideMetaData getSlideMetaData() {
        if (featuresList.size() > 0) {
            return((ISlideData)featuresList.get(0)).getSlideMetaData();
        }
        return null;
    }

    /**
     * Returns a microarray data by specified column.
     */
    public ISlideData getFeature(int column) {
        return(ISlideData)featuresList.get(column);
    }

    /**
     * Returns an element by specified row and column.
     */
    public ISlideDataElement getSlideDataElement(int column, int row) {
        ISlideData slideData = getFeature(column);
        return slideData.getSlideDataElement(row);
    }

    /**
     * Sets the non zero flag.
     */
    void setNonZero(boolean value) {
        resetMaxValues();
        ISlideData slideData;
        final int size = getFeaturesCount();
        for (int i=0; i<size; i++) {
            slideData = getFeature(i);
            slideData.setNonZero(value);
            updateMaxValues(slideData);
        }
    }

    /**
     * The class to allow run loading process in a separate thread.
     */
    private class Normalizer{
        Properties properties;
        int mode;
        int size;
        MultipleArrayViewer viewer;
        public Normalizer(int mode, Properties properties, int size, MultipleArrayViewer viewer) {
            this.mode = mode;
            this.properties = properties;
            this.size = size;
            this.viewer = viewer;
        }

        public void runNormalization() {
            try {
                ISlideData slideData;
                normalizationAbort = false;
                progressBar = new Progress(new java.awt.Frame(), "Normalization Progress", new NormalizationListener());
                progressBar.show();
                progressBar.setUnits(size);
                progressBar.setValue(0);
                for (int feature=0; feature<size; feature++) {
                    progressBar.setValue(feature);
                    progressBar.setDescription("Normalizing Experiment "+Integer.toString(feature));
                    progressBar.repaint();
                    slideData = getFeature(feature);
                    if(normalizationAbort)
                        break;
                    slideData.applyNormalization(mode, properties);
                    viewer.fireDataChanged();
                    updateMaxValues(slideData);
                }
                if(normalizationAbort){
                  //  System.out.println("Abort");
                    progressBar.setVisible(false);
                    progressBar.dispose();
                    for (int feature=0; feature<size; feature++) {
                        slideData = getFeature(feature);
                        slideData.applyNormalization(ISlideData.NO_NORMALIZATION, properties);
                        updateMaxValues(slideData);
                    }
                }
                progressBar.dispose();
            } catch (Exception e) {

            } finally {

            }
        }
    }

    /**
     * Normalize the data according to a specified mode.
     */
    String normalize(int mode, MultipleArrayViewer viewer) {
        resetMaxValues();
        ISlideData slideData;
        final int size = getFeaturesCount();
        Properties properties = new Properties();
        //construct normalization parameter defaults
        new Parameter();
        if(mode == ISlideData.NO_NORMALIZATION){
            for (int feature=0; feature<size; feature++) {
                slideData = getFeature(feature);
                slideData.applyNormalization(ISlideData.NO_NORMALIZATION, properties);
                updateMaxValues(slideData);
            }
            this.experiment = createExperiment();
            return "normalized";  //actually signals altered state
        }
        if(mode == ISlideData.LINEAR_REGRESSION){
            LinRegNormInitDialog dialog = new LinRegNormInitDialog();
            if(dialog.showModal() == JOptionPane.OK_OPTION){
                properties.setProperty("standard-deviation", Float.toString(dialog.getSD()));
                properties.setProperty("mode", dialog.getMode());
                dialog.dispose();
            }
            else{
                return "no_change";
            }
        }
        else if(mode == ISlideData.RATIO_STATISTICS_95 || mode == ISlideData.RATIO_STATISTICS_99){
            RatioStatsNormInitDialog dialog = new RatioStatsNormInitDialog();
            if(dialog.showModal() == JOptionPane.OK_OPTION){
                properties.setProperty("confidence-interval", Integer.toString(dialog.getCI()));
                dialog.dispose();
            }
            else{
                return "no_change";
            }
        }
        else if(mode == ISlideData.ITERATIVE_LOG){
            IterativeLogMCNormInitDialog dialog = new IterativeLogMCNormInitDialog();
            if(dialog.showModal() == JOptionPane.OK_OPTION){
                properties.setProperty("standard-deviation", Float.toString(dialog.getSD()));
                dialog.dispose();
            }
            else{
                return "no_change";
            }
        }
        //  Thread thread = new Thread(new Normalizer(mode, properties, size));
        //  thread.setPriority(Thread.MIN_PRIORITY);
        //  thread.start();
        Normalizer normalizer = new Normalizer(mode, properties, size, viewer);
        normalizer.runNormalization();
        this.experiment = createExperiment();
        if(!normalizationAbort)
            return "normalized";
        else
            return "process_abort_reset";
    }

    /**
     * The class to listen to algorithm events.
     */
    private class NormalizationListener extends DialogListener {

        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            if (command.equals("cancel-command")) {
                normalizationAbort = true;
                progressBar.dispose();
            }
        }

        public void windowClosing(WindowEvent e) {
            normalizationAbort = true;
            progressBar.dispose();
        }
    }




    /**
     * Normalize the data according to a specified mode.
     */
    void normalizeList(int mode) {
        resetMaxValues();
        ISlideData slideData;
        final int size = getFeaturesCount();
        for (int i=0; i<size; i++) {
            slideData = getFeature(i);
            slideData.applyNormalizationList(mode);
            updateMaxValues(slideData);
        }
        this.experiment = createExperiment();
    }

    /**
     * Returns an array of sorted indices for specified column.
     */
    public int[] getSortedIndices(int column) {
        return(int[])indicesList.get(column);
    }

    /**
     * Creates an experiment data.
     * @see Experiment
     */
    private Experiment createExperiment() {
        final int featuresSize = featuresList.size();
        if (featuresSize < 1) {
            return null;
        }
        final int probesSize = getFeature(0).getSize();
        Experiment experiment = null;
        int[] features = null;
        int[] probes = null;

        // pcahan affy detection filter or fold filter
        if ((isLowerCutoffs()||isGenePixFilter() || isPercentageCutoff()) ||isPvaluePercentageCutoff()|| isPresentCallCutoff()||isGCOSPercentCutoff()||isVarianceFilter() || ( (getDataType() == TMEV.DATA_TYPE_AFFY) && (isDetectionFilter() || isFoldFilter())) ) {
            probes = createCutoffGeneList(featuresSize, probesSize);
            experiment = createExperiment(featuresSize, probes);
        } else {
            // all features used for experiment
            features = createDefaultFeatures(featuresSize, probesSize);
            experiment = createExperiment(features, probesSize);
        }
        return experiment;
    }



    /**
     * Creates an array of ordered integers.
     */
    private int[] createDefaultFeatures(final int featuresSize, final int probesSize) {
        int[] features = new int[featuresSize];
        for (int i=0; i<featuresSize; i++) {
            features[i] = i;
        }
        return features;
    }

    /**
     * Creates an array of indices of used microarrays.
     */
    private int[] createCutoffFeatures(final int featuresSize, final int probesSize) {
        ISlideData slideData;
        float cy3, cy5;
        ArrayList list = new ArrayList();
        boolean criteria = true;
        int percentageCount = 0;
        for (int feature=0; feature<featuresSize; feature++) {
            slideData = getFeature(feature);
            for (int probe=0; probe<probesSize; probe++) {
                cy3 = slideData.getCY3(probe);
                cy5 = slideData.getCY5(probe);
                if (isLowerCutoffs()) {
                    if ((cy3 < lowerCY3Cutoff) || (cy5 < lowerCY5Cutoff)) {
                        criteria = false;
                        break;
                    }
                }
                if (isPercentageCutoff()) {
                    if ((cy3 > 0) && (cy5 > 0)) {
                        percentageCount++;
                    }
                }
            }
            if (criteria && isPercentageCutoff()) {
                if ((float)percentageCount/(float)probesSize*100f < percentageCutoff) {
                    criteria = false;
                }
            }
            if (criteria) {
                list.add(new Integer(feature));
            }
        }
        int[] features = new int[list.size()];
        for (int i=0; i<features.length; i++) {
            features[i] = ((Integer)list.get(i)).intValue();
        }
        return features;
    }


    //Below: new createCutoffGeneList() that trims off genes instead of experiments. 8/2/2002, N. Bhagabati

    /*
     * Retired for MeV 2.2.  Replaced by method to handle affy cutoff criteria (below, from pcahan)
     *
    private int[] createCutoffGeneList(final int featuresSize, final int probesSize) {
        ISlideData[] slideData = new ISlideData[featuresSize];
        //float cy3, cy5;
        ArrayList list = new ArrayList();
        boolean lowerCutoffCriterion = true;
        boolean percentageCutoffCriterion = true;
        int percentageCount = 0;

        for (int i = 0; i < slideData.length; i++) {
            slideData[i] = getFeature(i);
        }

        for (int probe = 0; probe < probesSize; probe++) {
            float[] cy3 = new float[featuresSize];
            float[] cy5 = new float[featuresSize];
            percentageCount = 0;
            lowerCutoffCriterion = true;
            percentageCutoffCriterion = true;

            for (int j = 0; j < cy3.length; j++) {
                cy3[j] = slideData[j].getCY3(probe);
                cy5[j] = slideData[j].getCY5(probe);
            }

            if (isLowerCutoffs()) {
                for (int j = 0; j < cy3.length; j++) {
                    if ((cy3[j] < lowerCY3Cutoff) || (cy5[j] < lowerCY5Cutoff)) {
                        lowerCutoffCriterion = false;
                        break;
                    }
                }
            }

            if (isPercentageCutoff()) {
                for (int j = 0; j < cy3.length; j++) {
                    if ((cy3[j] > 0) && (cy5[j] > 0)) {
                        percentageCount++;
                    }
                }

                if ((float)percentageCount/(float)featuresSize*100f < percentageCutoff) {
                    percentageCutoffCriterion = false;
                }
            }

            if (lowerCutoffCriterion && percentageCutoffCriterion) {
                list.add(new Integer(probe));
            }

            if (lowerCutoffCriterion && percentageCutoffCriterion && detectionCriterion && foldCriterion) {
                list.add(new Integer(probe));
            }
        }

        int[] retainedProbes = new int[list.size()];
        for (int i = 0; i < retainedProbes.length; i++) {
            retainedProbes[i] = ((Integer)list.get(i)).intValue();
        }

        return retainedProbes;
    }
     *
     */
    //add for mas5 filter some genes according A/(P+m+A) percentage
    private int[]filterAbsentCall(int generows,float percentage)throws IOException{
    	int counter=0;
    	int tag=0;
    	int list[]=new int[generows+1];
    	boolean head=true;
    	String [] fields=this.getFieldNames();
    	
    	BufferedReader reader= new BufferedReader(new FileReader(fields[this.getFieldNames().length-1]));
    	StringSplitter ss = new StringSplitter((char)0x09);
    	String currentLine;
    	while((currentLine=reader.readLine())!=null){
    		counter=0;
    		ss.init(currentLine);
    		if(head) head=false;
    		else{
    			for(int i=0;i<ss.countTokens();i++){
    				if(ss.nextToken().compareTo("A")==0){
    					counter++;	
    				}
    				
    			}
    			if(counter*100/ss.countTokens()>percentage)
    				list[tag]=1;
    			tag++;
       			}
    	}
       	reader.close();
    	return list;
    }
    
    

    //modified from original above ( by pcahan)

    private int[] createCutoffGeneList(final int featuresSize, final int probesSize) {
        ISlideData[] slideData = new ISlideData[featuresSize];
        //float cy3, cy5;
        ArrayList list = new ArrayList();
        boolean lowerCutoffCriterion = true;
        boolean percentageCutoffCriterion = true;
        boolean varianceFilterCutoffCriterion = true;
        boolean detectionCriterion = true;
        boolean foldCriterion = true;
        boolean presentCallCutoffCriterion = true,genePixCutoffCriterion=true;
        boolean GCOSCutoffCriterion=true;
        boolean pvaluepercentageCutoffCriterion = true;
        int mas5CallList[]=new int[slideData.length];
        int pvaluepercentCount=0,percentageCount = 0;
        int absentCount = 0;
        int percentCount=0;
        int genepixCount=0;
        int[]tagList=new int[probesSize];
      
        if(isPresentCallCutoff()){
        	try{
        		tagList=filterAbsentCall(probesSize,percentageCutoff);
        	}catch(IOException e){
        		System.out.print("Call file wrong");
        	}
        }
        // get each chip
        for (int i = 0; i < slideData.length; i++) {
            slideData[i] = getFeature(i);
        }
       //System.out.print(featuresSize);
        // iterate over each gene
        for (int probe = 0; probe < probesSize; probe++) {

            // arrays of length = number of chips
            float[] cy3 = new float[featuresSize];
            float[] cy5 = new float[featuresSize];
            
            String[] detection = new String[featuresSize];
            float[] pvalue=new float[featuresSize];
            float[] flags=new float[featuresSize];
            pvaluepercentCount=0;
            percentageCount = 0;
            percentCount=0;
            absentCount = 0;
            genepixCount=0;
            lowerCutoffCriterion = true;
            percentageCutoffCriterion = true;
            detectionCriterion = true;
            foldCriterion = true;
            presentCallCutoffCriterion = true;
            GCOSCutoffCriterion=true;
            pvaluepercentageCutoffCriterion = true;
            genePixCutoffCriterion=true;
            
            if(tagList[probe]==1){
            	presentCallCutoffCriterion = false;
            }
            // iterate over each chip
         
            for (int j = 0; j < cy3.length; j++) {
                cy3[j] = slideData[j].getCY3(probe);
                cy5[j] = slideData[j].getCY5(probe);
                detection[j] = getDetection(j, probe);
                pvalue[j]=getPvalue(j,probe);
                flags[j]=getGenePixFlags(j,probe);   
            }
            
            if(isGenePixFilter()){
            	for (int j = 0; j < cy3.length; j++) {
            	if(flags[j]<0)
            		genepixCount++;
            	}
            	if((float)genepixCount/(float)featuresSize*100f >percentageCutoff)
            		genePixCutoffCriterion=false;    
            }

            // run tests

            if (isLowerCutoffs()) {
                for (int j = 0; j < cy3.length; j++) {
                    if ((cy3[j] < lowerCY3Cutoff) || (cy5[j] < lowerCY5Cutoff)) {
                        lowerCutoffCriterion = false;
                        break;
                    }
                }
            }

            if (isPercentageCutoff()) {
                for (int j = 0; j < cy3.length; j++) {
                    if ((cy3[j] > 0) && (cy5[j] > 0) || (dataType == IData.DATA_TYPE_RATIO_ONLY && !Float.isNaN(cy5[j]))) {
                        percentageCount++;
                    }
                }
                if ((float)percentageCount/(float)featuresSize*100f < percentageCutoff) {
                    percentageCutoffCriterion = false;
                	}
            }
         
            //fiter on present calls A/(P+M+A)
            if(isGCOSPercentCutoff()){
            	for(int j = 0; j < cy3.length; j++) {                
                    detection[j] = getDetection(j, probe);
                    if(((String)detection[j]).compareTo("A")==0||((String)detection[j]).compareTo("a")==0)
                    	percentCount++;
                    
            	}
            	if ((float)percentCount/(float)featuresSize*100f >percentageCutoff) {
                    GCOSCutoffCriterion = false;
                	}
            }
        /*    if(isVarianceFilter()) {
                varianceCriterion = retainBasedOnSD;
            }
        */
            // pcahan
            if (isDetectionFilter()){
                detectionCriterion = detectionFilter.keep_gene(detection);
            }

            if (isFoldFilter() ){
                foldCriterion = foldFilter.keep_gene(cy5);
            }
            
            //wwang add for affy p-value filter:
            //choose a percent below a particular
            //p-value cutoff - for example 80% below p=0.001
            if(isPvaluePercentageCutoff()){
            	for(int j = 0; j < cy3.length; j++) {                
                    pvalue[j] = getPvalue(j, probe);
                    if(pvalue[j]>0.01)
                    	pvaluepercentCount++;
                    
            	}
            	if ((float)pvaluepercentCount/(float)featuresSize*100f >20) {
            		pvaluepercentageCutoffCriterion = false;
                	}
            }
            
            if (lowerCutoffCriterion &&pvaluepercentageCutoffCriterion&& percentageCutoffCriterion&&GCOSCutoffCriterion&&presentCallCutoffCriterion&& detectionCriterion &&genePixCutoffCriterion&& foldCriterion) {
                list.add(new Integer(probe));
            }
        }

         
        //handle variance cuttoffs after other cutoffs, 
        
        boolean [] retainBasedOnSD = null;

        if(isVarianceFilter()) {
            //generate a new list now applying var. filter
            list = imposeVarianceFilter(list);                
        }        
        
        int[] retainedProbes = new int[list.size()];
        for (int i = 0; i < retainedProbes.length; i++) {
            retainedProbes[i] = ((Integer)list.get(i)).intValue();
        }

        return retainedProbes;
    }

    
    /**
     * Determines which genes pass the variance filter
     */
    private float [] getStandardDeviations() {
        int numGenes = this.getFeaturesSize();
        int numSamples = this.getFeaturesCount();
        float [] vars = new float[numGenes];
        
        float val;
        int validN;
        float sum;
        float sse;
        float mean;

        float [] sds = new float[numGenes];
        boolean [] retentionList = new boolean[numGenes];
        
        for(int i = 0; i < numGenes; i++) {

            validN = 0;
            sum = 0;
            sse = 0;
            
            for(int j = 0; j < numSamples; j++) {
                val = this.getRatio(j,i,this.logState);
                if(!Float.isNaN(val)) {
                    sum += val;
                    validN++;
                }
            }
            
            if(validN > 0) {
               mean = sum/validN++;
               //System.out.println("mean = "+mean);
            } else {                
                sds[i] = -1; //marker for a gene with no values
                continue;
            }
            
            for(int j = 0; j < numSamples; j++) {
                val = this.getRatio(j,i,this.logState);
                if(!Float.isNaN(val)) {
                    sse += Math.pow(val - mean,2.0);
                }
            }            
            if(validN > 0)
                sds[i] =(float)Math.sqrt(sse/validN);          
            else
                sds[i] = 0;
        }        
        return sds;
    }
    
 
    
    public ArrayList imposeVarianceFilter( ArrayList listOfIndices) {

        String mode = varianceFilterProps.getProperty("Filter Mode");
        float [] sds = getStandardDeviations();
        boolean [] retentionList = new boolean[sds.length];

        ArrayList newList = new ArrayList();
        
        if(mode.equals("sd value mode")) { //cutoff = hard
            float sdCut = Float.parseFloat(varianceFilterProps.getProperty("Value"));
            for(int i = 0; i < sds.length; i++) {
                if(sds[i] >= sdCut && listOfIndices.contains(new Integer(i)))
                    newList.add(new Integer(i));
            }            
        } else if(mode.equals("percent mode")) { //top x percent
            float percent = Float.parseFloat(varianceFilterProps.getProperty("Value"));            
            QSort sorter = new QSort(sds);
            float [] sortedSDs = sorter.getSorted();
            int [] origOrder = sorter.getOrigIndx();
            int targetSize = (int)(listOfIndices.size()*(percent/100f));
            
            for(int i = origOrder.length-1; i >= 0 && newList.size() < targetSize; i--) {
                if(listOfIndices.contains(new Integer(origOrder[i])))
                    newList.add(new Integer(origOrder[i]));
            }
            
        } else {
            int targetSize = Integer.parseInt(varianceFilterProps.getProperty("Value"));
            QSort sorter = new QSort(sds);
            float [] sortedSDs = sorter.getSorted();
            int [] origOrder = sorter.getOrigIndx();
            
            for(int i = origOrder.length-1; i >= 0 && newList.size() < targetSize; i--) {
                if(listOfIndices.contains(new Integer(origOrder[i])))
                    newList.add(new Integer(origOrder[i]));
            }
        }
        
        return newList;
    }

    
    /**
     * @param indices the indices of used experiments.
     */
    private Experiment createExperiment(final int[] columns, final int rows) {
        ISlideData sd;
        FloatMatrix fm = new FloatMatrix(rows, columns.length);
        float[][] matrix = fm.A;
        for (int i = 0; i < columns.length; i++) {
            sd = (ISlideData)featuresList.get(columns[i]);

            //pcahan --  don't log2 transform affy data
            /*
            if (TMEV.getDataType() == TMEV.DATA_TYPE_AFFY){
                for (int row = rows; --row >= 0; ) {
                    fm.A[row][columns[i]] = sd.getRatio(row, LINEAR);
                }
            } else {
}*/
   for (int row = rows; --row >= 0;) {
                    fm.A[row][columns[i]] = sd.getRatio(row, this.logState);
                }
   //         }
        }
        return new Experiment(fm, columns);
    }

    //The following method was added to correct the way "set Lower Cutoffs" 
    //and "Set %age cutoffs" is handled, i.e., to trim out rows (genes), 
    //rather than columns (experiments)
    //this is called if cutoffs have been used
    private Experiment createExperiment(final int columns, final int[] rows) {
        ISlideData sd;
        FloatMatrix fm = new FloatMatrix(rows.length, columns);
        int[] columnArray = new int[columns];
        float[][] matrix = fm.A;
        for (int i = 0; i < columns; i++) {
            columnArray[i] = i;
            sd = (ISlideData)featuresList.get(i);

            //pcahan --  don't log2 transform affy data
            /*
            if (TMEV.getDataType() == TMEV.DATA_TYPE_AFFY) {

                for (int j = 0; j < rows.length; j++) {
                    fm.A[j][i] = sd.getRatio(rows[j], LINEAR);
                }
            } else {*/

                for (int j = 0; j < rows.length; j++) {
                    fm.A[j][i] = sd.getRatio(rows[j], this.logState);
                }
            //}
        }
        return new Experiment(fm, columnArray, rows);
    }


    /**
     *  Returns the normalization state of the data set
     */
    public int getNormalizationState(){
        if(this.featuresList == null || this.featuresList.size() < 1)
            return ISlideData.NO_NORMALIZATION;
        else
            return ((ISlideData)this.featuresList.get(0)).getNormalizedState();
    }

    /**
     * Returns a MultipleArrayData object comprised of a subset of elements
     */
    public MultipleArrayData getDataSubset(int [] indices){

        ISlideData slideData;
        ISlideMetaData metaData = null;
        ISlideDataElement sde;
        String name;
        MultipleArrayData data = new MultipleArrayData();
        int normalizedState = this.getNormalizationState();

        if(indices.length < 1)
            return null;

        int index;

        for(int slide = 0; slide < this.getFeaturesCount(); slide++){
            if(slide == 0){
                slideData = new SlideData();
                name = this.getSampleName(slide);
                if(name.endsWith("...")){
                    toggleExptNameLength();

                    slideData.setSlideDataLabels(this.getFeature(slide).getSlideDataKeys(), this.getFeature(slide).getSlideDataLabels());
                    ((SlideData) slideData).setSlideFileName(this.getSampleName(slide));

                    toggleExptNameLength();
                } else{

                    slideData.setSlideDataLabels(this.getFeature(slide).getSlideDataKeys(), this.getFeature(slide).getSlideDataLabels());
                    ((SlideData) slideData).setSlideFileName(this.getSampleName(slide));
                }

                for(int spot = 0; spot < indices.length; spot++){
                    index = indices[spot];
                    sde = this.getSlideDataElement(slide, index).clone();
                    slideData.addSlideDataElement(sde);
                }

                metaData = (ISlideMetaData)slideData;
 
                //7/10/06 jcb added to set field names in new data
               metaData.clearFieldNames();
               metaData.setFieldNames(this.getFieldNames());

            } else{
                slideData = new FloatSlideData(metaData);
                ((FloatSlideData) slideData).createCurrentIntensityArrays();

                name = this.getSampleName(slide);
                if(name.endsWith("...")){
                    toggleExptNameLength();
                    slideData.setSlideDataLabels(this.getFeature(slide).getSlideDataKeys(), this.getFeature(slide).getSlideDataLabels());
                    ((FloatSlideData) slideData).setSlideFileName(this.getSampleName(slide));

                    toggleExptNameLength();
                } else{
                    slideData.setSlideDataLabels(this.getFeature(slide).getSlideDataKeys(), this.getFeature(slide).getSlideDataLabels());
                    ((FloatSlideData) slideData).setSlideFileName(this.getSampleName(slide));
                }

                for(int spot = 0; spot < indices.length; spot++){
                    index = indices[spot];
                    sde = this.getSlideDataElement(slide, index);
                    ((FloatSlideData)slideData).setIntensities(spot, sde.getTrueIntensity(ISlideDataElement.CY3), sde.getTrueIntensity(ISlideDataElement.CY5));
                    ((FloatSlideData)slideData).setCurrentIntensities(spot, sde.getCurrentIntensity()[0], sde.getCurrentIntensity()[1]);
                }
            }
            slideData.setNormalizedState(normalizedState);
            data.addFeature(slideData);   
            data.setDataType(this.dataType);      
        }
        data.setChipAnnotation(this.getChipAnnotation());
        data.setAnnotationLoaded(this.isAnnotationLoaded());
        return data;
    }


    /**
     * Returns a MultipleArrayData object comprised of a subset of column indices and rows indices
     */
    public MultipleArrayData getDataSubset(int [] columnIndices, int [] rowIndices){
        ISlideData slideData;
        ISlideMetaData metaData = (ISlideMetaData)this.featuresList.get(0);
        ISlideDataElement sde;
        String name;
        MultipleArrayData data = new MultipleArrayData();
        int normalizedState = this.getNormalizationState();

        if(columnIndices.length < 1 || rowIndices.length < 1)
            return null;

        int index;
        int slideIndex;

        for(int slide = 0; slide < columnIndices.length; slide++){
            slideIndex = columnIndices[slide];

            if(slideIndex == 0){
                slideData = new SlideData();

                name = this.getSampleName(slideIndex);
                if(name.endsWith("...")){
                    toggleExptNameLength();
                    slideData.setSlideDataLabels(this.getFeature(slideIndex).getSlideDataKeys(), this.getFeature(slideIndex).getSlideDataLabels());
                    ((SlideData) slideData).setSlideFileName(this.getSampleName(slideIndex));
                    toggleExptNameLength();
                } else{
                    slideData.setSlideDataLabels(this.getFeature(slideIndex).getSlideDataKeys(), this.getFeature(slideIndex).getSlideDataLabels());
                    ((SlideData) slideData).setSlideFileName(this.getSampleName(slideIndex));
                }

                for(int spot = 0; spot < rowIndices.length; spot++){
                    sde = new SlideDataElement(this.getSlideDataElement(slideIndex, rowIndices[spot]));
                    slideData.addSlideDataElement(sde);
                }
                metaData = (ISlideMetaData)slideData;
                
                //7/10/06 jcb added to set field names in new data
                metaData.clearFieldNames();                
                metaData.setFieldNames(this.getFieldNames());

            } else {
                slideData = new FloatSlideData(metaData);
                ((FloatSlideData) slideData).createCurrentIntensityArrays();

                name = this.getSampleName(slideIndex);
                if(name.endsWith("...")){
                    toggleExptNameLength();
                    slideData.setSlideDataLabels(this.getFeature(slideIndex).getSlideDataKeys(), this.getFeature(slideIndex).getSlideDataLabels());
                    ((FloatSlideData) slideData).setSlideFileName(this.getSampleName(slideIndex));
                    toggleExptNameLength();
                } else{
                    slideData.setSlideDataLabels(this.getFeature(slideIndex).getSlideDataKeys(), this.getFeature(slideIndex).getSlideDataLabels());
                    ((FloatSlideData) slideData).setSlideFileName(this.getSampleName(slideIndex));
                }
                for(int spot = 0; spot < rowIndices.length; spot++){
                    sde = this.getSlideDataElement(slideIndex, rowIndices[spot]);
                    ((FloatSlideData)slideData).setIntensities(spot, sde.getTrueIntensity(ISlideDataElement.CY3), sde.getTrueIntensity(ISlideDataElement.CY5));
                    ((FloatSlideData)slideData).setCurrentIntensities(spot, sde.getCurrentIntensity()[0], sde.getCurrentIntensity()[1]);
                }

            }
            slideData.setNormalizedState(normalizedState);
            data.addFeature(slideData);
        }
        data.setDataType(this.dataType);
        return data;
    }


    /**
     * Sets initial max values.
     */
    private void resetMaxValues() {
        setMaxCY3(0f);
        setMaxCY5(0f);
        setMaxRatio(0f);
        setMinRatio(0f);
    }

    /**
     * Updates min and max ratio values.
     */
    private void updateMaxMinRatios(ISlideData slideData) {
        float min = minRatio;
        float max = maxRatio;
        float value;
        final int features = featuresList.size();
        final int probes = ((ISlideData)featuresList.get(0)).getSize();
        for (int probe=0; probe<probes; probe++) {
            value = slideData.getRatio(probe, this.logState);
            max = Math.max(max, value);
            min = Math.min(min, value);
        }
        setMaxRatio(max);
        setMinRatio(max);
    }

    /**
     * Toggles the length of the displayed file name.
     */
    public void toggleExptNameLength(){
        if(this.getFeaturesCount() < 1)
            return;
        for(int i = 0; i < this.getFeaturesCount(); i++){
            ((ISlideData)(this.getFeature(i))).toggleNameLength();
        }
    }

    /**
     * Sort the data with specified style.
     */
    void sort(int style) {
        SlideDataSorter sorter = new SlideDataSorter();
        if (style == SlideDataSorter.SORT_BY_RATIO) { // ratio values are unique for slides
            for (int i = 0; i < featuresList.size(); i++) {
                sorter.setSlideData((ISlideData)featuresList.get(i));
                sorter.sort((int[])indicesList.get(i), style);
            }
        } else { // all the other values are shared
            if (featuresList.size() < 1) {
                return;
            }
            sorter.setSlideData((ISlideData)featuresList.get(0));
            int[] src = (int[])indicesList.get(0);
            sorter.sort(src, style);
            int[] dst;
            for (int i = 1; i < featuresList.size(); i++) {
                dst = (int[])indicesList.get(i);
                System.arraycopy(src, 0, dst, 0, src.length);
            }
        }
    }



    /**
     * Returns ratio values of the data.
     */
    public Experiment getFullExperiment() {
        int featuresSize = this.getFeaturesCount();
        int probesSize = this.getFeaturesSize();        
        int []  features = createDefaultFeatures(featuresSize, probesSize);
        Experiment experiment = createExperiment(features, probesSize);        
        return experiment;
    }
    
    /**
     * Returns ratio values of the data.
     */
    public Experiment getExperiment() {
        if(useMainData)
           return experiment;
        return alternateExperiment;
    }
    
    private void setMaxCY3(float value) {this.maxCy3 = value;}
    private void setMaxCY5(float value) {this.maxCy5 = value;}
    public float getMaxCY3() {return this.maxCy3;}
    public float getMaxCY5() {return this.maxCy5;}

    private void setMaxRatio(float value) {this.maxRatio = value;}
    private void setMinRatio(float value) {this.minRatio = value;}
    public float getMaxRatio() {return this.maxRatio;}
    public float getMinRatio() {return this.minRatio;}

    //////////////////////////////////
    //                              //
    //  adjust experiment methods   //
    //                              //
    //////////////////////////////////
    void log2Transform() {
        Adjustment.log2Transform(experiment.getMatrix());
    }

    void unlog2Transform() {
        Adjustment.unlog2Transform(experiment.getMatrix());
    }
    void normalizeSpots() {
        Adjustment.normalizeSpots(experiment.getMatrix());
    }

    void divideSpotsRMS() {
        Adjustment.divideSpotsRMS(experiment.getMatrix());
    }

    void divideSpotsSD() {
        Adjustment.divideSpotsSD(experiment.getMatrix());
    }

    // pcahan -- affy- abs specific
    void divideGenesMedian() {
        Adjustment.divideGenesMedian(experiment.getMatrix());
    }

    void divideGenesMean() {
        Adjustment.divideGenesMean(experiment.getMatrix());
    }

    void meanCenterSpots() {
        Adjustment.meanCenterSpots(experiment.getMatrix());
    }

    void medianCenterSpots() {
        Adjustment.medianCenterSpots(experiment.getMatrix());
    }

    void digitalSpots() {
        Adjustment.digitalSpots(experiment.getMatrix());
    }

    void normalizeExperiments() {
        Adjustment.normalizeExperiments(experiment.getMatrix());
    }

    void divideExperimentsRMS() {
        Adjustment.divideExperimentsRMS(experiment.getMatrix());
    }

    void divideExperimentsSD() {
        Adjustment.divideExperimentsSD(experiment.getMatrix());
    }

    void meanCenterExperiments() {
        Adjustment.meanCenterExperiments(experiment.getMatrix());
    }

    void medianCenterExperiments() {
        Adjustment.medianCenterExperiments(experiment.getMatrix());
    }

    void digitalExperiments() {
        Adjustment.digitalExperiments(experiment.getMatrix());
    }

    void log10toLog2() {
        Adjustment.log10toLog2(experiment.getMatrix());
    }
    void log2toLog10() {
        Adjustment.log2toLog10(experiment.getMatrix());
    }

    // pcahan
    private static float getGeneMean(float[] row) {
        float mean = 0f;
        for (int i = 0; i < row.length; i++){
            mean += row[i];
        }
        return (float)mean/row.length;
    }

    public int getDataType() {
        return this.dataType;
    }

    public void setDataType(int type){
        this.dataType = type;
        ISlideData slideData;
        for(int i = 0; i < this.getFeaturesCount(); i++){
            slideData = this.getFeature(i);
            slideData.setDataType(type);
        }
        if(this.dataType == IData.DATA_TYPE_RATIO_ONLY || this.dataType == IData.DATA_TYPE_AFFY_ABS ){
            this.logState = LINEAR;
        } else {
            this.logState = LOG;
        }
        if(this.dataType == IData.DATA_TYPE_AFFY_ABS){
        	convertToAffy();
        }
        if(this.getFeaturesCount() > 0)
            this.experiment = createExperiment();
    }

    private void convertToAffy(){
    	SlideData ismd = new SlideData(getFeature(0));
    	Vector allSlideDataElements = ismd.getAllElements();
    	ISlideDataElement sde;
    	for(int i=0; i<allSlideDataElements.size(); i++){
    		sde = (ISlideDataElement)allSlideDataElements.get(i);
    		if(!(sde instanceof AffySlideDataElement)){
    			//If this slidedataelement isn't already an Affy slidedataelement, turn it into one
    			AffySlideDataElement asde = new AffySlideDataElement(sde);
    			allSlideDataElements.remove(i);
    			allSlideDataElements.add(i, asde);
    		}
    	}
    }
    
    /** Returns gene or sample indices related to search terms.
     */
    public int [] search(AlgorithmData criteria) {
        AlgorithmParameters params = criteria.getParams();
        boolean geneSearch = params.getBoolean("gene-search");
        boolean caseSens = params.getBoolean("case-sensitive");
        boolean fullTerm = params.getBoolean("full-term");
        String searchTerm = params.getString("search-term");
        String upperSearchString = searchTerm.toUpperCase();
        String [] fields = criteria.getStringArray("field-names");
        String annot;
        String [] fullFieldNames;
        boolean hit = false;        
        int n;
        //need to find indices of fields to check.
        //the input fields should be in order
        Vector fieldIndices = new Vector();
        
        Vector indexVector = new Vector();
        
        int [] indices;
       
        
        Hashtable keys = new Hashtable();
        
        if(getFeaturesCount() < 1 || getFeaturesSize() < 1)
            return new int[0]; //empty result
        
        
        if(geneSearch && this.isAnnotationLoaded()){
        	 fullFieldNames = this.getAllFilledAnnotationFields();
             
             for(int i = 0; i < fields.length; i++) {
                 for(int j = 0; j < fullFieldNames.length; j++) {
                     if(fields[i].equals(fullFieldNames[j])) {
                         fieldIndices.addElement(fullFieldNames[j] );
                         break;
                     }
                 }        
             } 
     
             n = getFeaturesSize();
             ISlideDataElement sde;
             ISlideData slide = this.getFeature(0);
             int annotIndex;
             String[]annotation;
             String fieldName;
             for(int i = 0; i < n; i++) {
                 hit = false;
                 for(int j = 0; j < fieldIndices.size(); j++) {
                     fieldName = (String)fieldIndices.elementAt(j);
                     annotation = getElementAnnotation(i, fieldName);
                     
                     if(fullTerm) {
                         if(caseSens) {
                             if(annotation[0].equals(searchTerm)) {
                                 hit = true;
                               //  break;
                             }
                         } else {
                             if(annotation[0].equalsIgnoreCase(searchTerm)) {
                                 hit = true;
                               break;
                             }                            
                         }
                         
                     } else {  //able to look within a term
                        
                         if(caseSens) {                                                              
                             if(annotation[0].indexOf(searchTerm) != -1) {
                                 hit = true;
                                 break;
                             }                  
                         } else {
                             if((annotation[0].toUpperCase()).indexOf(upperSearchString) != -1) {
                                 hit = true;
                                break;
                             } 
                             
                         }                           
                          
                     }
                     
           
                     
                 }
                 if(hit == true) {
                     indexVector.addElement(new Integer(i));
                 }
             }//End of for loop
           
             //Loop to take care of the condition when there is already annotation present in the data 
             //AND user has loaded extra annotations
             if(this.getFieldNames().length!=0){
            	 fieldIndices.clear();
            	 fullFieldNames = this.getFieldNames();

            	 for(int i = 0; i < fields.length; i++) {
            		 for(int j = 0; j < fullFieldNames.length; j++) {
            			 if(fields[i].equals(fullFieldNames[j])) {
            				 fieldIndices.addElement(new Integer(j));
            				 break;
            			 }
            		 }        
            	 } 

            	 n = getFeaturesSize();

            	 slide = this.getFeature(0);

            	 for(int i = 0; i < n; i++) {
            		 hit = false;
            		 for(int j = 0; j < fieldIndices.size(); j++) {
            			 annotIndex = ((Integer)fieldIndices.elementAt(j)).intValue();
            			 annot = getElementAttribute(i, annotIndex);

            			 if(fullTerm) {
            				 if(caseSens) {
            					 if(annot.equals(searchTerm)) {
            						 hit = true;
            						 //  break;
            					 }
            				 } else {
            					 if(annot.equalsIgnoreCase(searchTerm)) {
            						 hit = true;
            						 break;
            					 }                            
            				 }

            			 } else {  //able to look within a term

            				 if(caseSens) {                                                              
            					 if(annot.indexOf(searchTerm) != -1) {
            						 hit = true;
            						 break;
            					 }                  
            				 } else {
            					 if((annot.toUpperCase()).indexOf(upperSearchString) != -1) {
            						 hit = true;
            						 break;
            					 } 

            				 }                           

            			 }



            		 }
            		 if(hit == true) {
            			 indexVector.addElement(new Integer(i));
            		 }
            	 }

             }


             //End of adding
             indices = new int[indexVector.size()];
  
             for(int i = 0; i < indices.length; i++) {
                 indices[i] = ((Integer)(indexVector.elementAt(i))).intValue();
             }
  
        	
        }
        
        
        //Annotations NOT loaded. Just using ones in the expression data
        if(geneSearch && !this.isAnnotationLoaded()) {
        
            fullFieldNames = this.getFieldNames();
                    
            for(int i = 0; i < fields.length; i++) {
                for(int j = 0; j < fullFieldNames.length; j++) {
                    if(fields[i].equals(fullFieldNames[j])) {
                        fieldIndices.addElement(new Integer(j));
                        break;
                    }
                }        
            } 
    
            n = getFeaturesSize();
            ISlideDataElement sde;
            ISlideData slide = this.getFeature(0);
            int annotIndex;            
            for(int i = 0; i < n; i++) {
                hit = false;
                for(int j = 0; j < fieldIndices.size(); j++) {
                    annotIndex = ((Integer)fieldIndices.elementAt(j)).intValue();
                    annot = getElementAttribute(i, annotIndex);
                    
                    if(fullTerm) {
                        if(caseSens) {
                            if(annot.equals(searchTerm)) {
                                hit = true;
                              //  break;
                            }
                        } else {
                            if(annot.equalsIgnoreCase(searchTerm)) {
                                hit = true;
                              break;
                            }                            
                        }
                        
                    } else {  //able to look within a term
                       
                        if(caseSens) {                                                              
                            if(annot.indexOf(searchTerm) != -1) {
                                hit = true;
                                break;
                            }                  
                        } else {
                            if((annot.toUpperCase()).indexOf(upperSearchString) != -1) {
                                hit = true;
                               break;
                            } 
                            
                        }                           
                         
                    }
                    
          
                    
                }
                if(hit == true) {
                    indexVector.addElement(new Integer(i));
                }
            }
            
           
            indices = new int[indexVector.size()];
 
            for(int i = 0; i < indices.length; i++) {
                indices[i] = ((Integer)(indexVector.elementAt(i))).intValue();
            }
            
            //Sample search
        } else { 
            
            n = getFeaturesCount();
            Hashtable sampleNameHash;
            
            
            for(int i = 0; i < n; i++) {
                hit = false;
                sampleNameHash = this.getFeature(i).getSlideDataLabels();
                
                for(int j = 0; j < fields.length; j++) {
                      annot = (String)(sampleNameHash.get(fields[j]));
                      if(annot == null)
                          continue;
                    
                    if(fullTerm) {
                        if(caseSens) {
                            if(annot.equals(searchTerm)) {
                                hit = true;
                                break;
                            }
                        } else {
                            if(annot.equalsIgnoreCase(searchTerm)) {
                                hit = true;
                                break;
                            }                            
                        }
                        
                    } else {  //able to look within a term
                       
                        if(caseSens) {                                                              
                            if(annot.indexOf(searchTerm) != -1) {
                                hit = true;
                                break;
                            }                  
                        } else {
                            if((annot.toUpperCase()).indexOf(upperSearchString) != -1) {
                                hit = true;
                                break;
                            } 
                            
                        }                           
                         
                    }
                }
                 if(hit == true) {
                        indexVector.addElement(new Integer(i));
                    }
            }
                        
            indices = new int[indexVector.size()];
            
            for(int i = 0; i < indices.length; i++) {
                indices[i] = ((Integer)(indexVector.elementAt(i))).intValue();
            }
        }
       return indices; 
    }
    
    public String[][] getSampleAnnotationMatrix(){
    	String[] s = getSlideNameKeyArray();
    	String[][] m = new String[getFeaturesCount()][s.length];
    	for (int i=0; i<s.length; i++){
    		for (int j=0; j<getFeaturesCount(); j++){
    			m[j][i] = getSampleAnnotation(j, s[i]);
    		}
    	}
    	return m;                               
    }
    
    /** 
     * Returns an annotation array for the provided indices based on annotation key
     */
    public String[] getAnnotationList(String fieldName, int[] indices) {
	    String[] annot = new String[indices.length];
        	for(int i=0; i<indices.length; i++) {
        		String thisAnnot = this.getElementAnnotation(indices[i], fieldName)[0];
		    annot[i] = thisAnnot;
        	}
        return annot;
    }
    
    /**
     * Overload getAnnotationList, using the full indices list if no int[] input
     * @param fieldName
     * @return 
     */
    public String[] getAnnotationList(String fieldName) {
    	//System.out.println("MultipleArrayData, size of indicesList = " + this.experiment.getNumberOfGenes());
    	int[] indices = new int[this.experiment.getNumberOfGenes()];
    	for (int i = 0 ; i < indices.length; i ++) {
    		indices[i] = i;
    	}
    	return this.getAnnotationList(fieldName, indices);
    }

    /** Returns the slected sample annotation
     */
    public String getSampleAnnotation(int column, String key) {
    	
        
    	if(this.getFeature(0).isSampleAnnotationLoaded())
    		return (String)(this.getFeature(column).getSampleAnnotation().getAnnotation(key));
    	else
    		return (String)(this.getFeature(column).getSlideDataLabels().get(key));
    	
    	
    	
    	
    }    
	/*************************************************************************
     * Raktim CGH Functions
     * Oct 3rd, 2005
     ************************************************************************/
    public void setCGHCopyNumberCalculator(){
    	copyNumberCalculator = new CGHCopyNumberCalculator(this);
    }
    public CGHCopyNumberCalculator getCGHCopyNumberCalculator(){
    	return this.copyNumberCalculator;
    }

    /**
     * CGH Function
     */
    public int getFeaturesSize(int chromosome){
    	return chromosomeIndices[chromosome][1] - chromosomeIndices[chromosome][0];
    }
    /**
     * CGH Returns CY3 value.
     * For dye swap experiments, returns the value of the experiment
     * with test DNA labeled with cy3 dye
     * @param column the experiment index
     * @param row the relative index of the probe on the specified chromosome
     * @param chromosome the chromosome index
     * @return
     */
    public float getCY3(int column, int row, int chromosome){
    	return getCY3(column, chromosomeIndices[chromosome][0] + row);
    }
    /**
     * CGH Returns CY5 value.
     * For dye swap experiments, returns the value of the experiment
     * with test DNA labeled with cy3 dye
     * @param column the experiment index
     * @param row the relative index of the probe on the specified chromosome
     * @param chromosome the chromosome index
     * @return
    */
    public float getCY5(int column, int row, int chromosome){
    	return getCY5(column, chromosomeIndices[chromosome][0] + row);
    }
    /**
     * Returns an element attribute.
     */
    public String getElementAttribute(int row, int attr, int chromosome){
    	return getCloneAt(row).getName();
    }
    /**
     *  CGH Returns the number of chromosomes
     */
    public int getNumChromosomes(){
    	return chromosomeIndices.length;
    }
    /**
     * CGH Returns the number of data points in a given chromosome
     */
    /**
     * CGH Function
     */
    public int getNumDataPointsInChrom(int chromosome){
    	return chromosomeIndices[chromosome][1] - chromosomeIndices[chromosome][0];
    }
    /**
     * CGH Function
     */
    public int getCloneIndex(int relativeIndex, int chromosome){
    	return chromosomeIndices[chromosome][0] + relativeIndex;
    }
    /**
     * CGH Function
     */
    public int getRelativeIndex(int cloneIndex, int chromosome){
    	return cloneIndex - chromosomeIndices[chromosome][0];
    }
    /** Getter for property chromosomeIndices.
     * @return Value of property chromosomeIndices.
     */
    public int[][] getChromosomeIndices() {
    	//System.out.println("getChromosomeIndices(): " + this.chromosomeIndices.length);
        return this.chromosomeIndices;
    }
    /** Setter for property chromosomeIndices.
     * @param chromosomeIndices New value of property chromosomeIndices.
     */
    public void setChromosomeIndices(int[][] chromosomeIndices) {
        this.chromosomeIndices = chromosomeIndices;
        System.out.println("Chr Indices.len : " + chromosomeIndices.length + ", " + chromosomeIndices[0].length);
        /*
        for(int i=0; i < chromosomeIndices.length; i++){
        	int j = 0;
        	System.out.print("chromosomeIndices["+i+"]["+j+"]: ");
        	for(; j < chromosomeIndices[0].length; j++){
        		System.out.print(chromosomeIndices[i][j] + ", ");
        	}
        	System.out.println();
        }
        */
    }
    /**
     * CGH Function
     */
    public int getChromosomeStartIndex(int chromosomeIndex){
        return this.chromosomeIndices[chromosomeIndex][0];
    }
    
    /**
     * For Sate Saving
     * CGH Function
     */
    public void setChromosomeStartIndex(int chromosomeIndex, int val){
        this.chromosomeIndices[chromosomeIndex][0] = val;
    }
    /**
     * CGH Function
     */
    public int getChromosomeEndIndex(int chromosomeIndex){
        return this.chromosomeIndices[chromosomeIndex][1];
    }
    /**
     * For Sate Saving
     * CGH Function
     */
    public void setChromosomeEndIndex(int chromosomeIndex, int val){
        this.chromosomeIndices[chromosomeIndex][1] = val;
    }
    /**
     * CGH Function
     */
    public CGHClone getCloneAt(int index){
    	/*
    	CGHSlideDataElement sde_T1 = (CGHSlideDataElement)((ISlideData)featuresList.get(0)).getSlideDataElement(index);
    	CGHClone clone_T1 = sde_T1.getClone();
    	return clone_T1;
    	*/
    	return (CGHClone)clones.get(index);
    }
    /**
     * CGH Function
     */
    public CGHClone getCloneAt(int index, int chromosome){
    	return getCloneAt(getCloneIndex(index, chromosome));
    }
    /**
     * CGH Function
     * ChARM
     */
    public CGHClone[] getClonesWithinIndices(int stIndex, int endIndex, int chromosome){
    	CGHClone[] clones = new CGHClone[endIndex - stIndex];
    	int i = 0;
    	for (int index = stIndex; index <= endIndex; index++,i++){
    		clones[i] = getCloneAt(getCloneIndex(index, chromosome));
    	}
    	return clones;
    }
    
    /**
     * CGH Function
     * ChARM
     */
    public CGHClone[] getClonesWithinIndices(int stIndex, int endIndex, String experiment, int chromosome){
    	CGHClone[] clones = new CGHClone[endIndex - stIndex + 1];
    	int exprCol = getExperimentIndex(experiment);
    	int i = 0;
    	for (int index = stIndex; index <= endIndex; index++,i++){
    		clones[i] = getCloneAt(getCloneIndex(index, chromosome-1));
    		float ratio = this.getRatio(exprCol, index, IData.LOG);
    		clones[i].setRatio(ratio);
    	}
    	return clones;
    }
    
    public int getExperimentIndex(String expr) {
  	  ArrayList featuresList = this.getFeaturesList();
  	  for (int column = 0; column < featuresList.size(); column++){
  			String name = (String)((ISlideData)featuresList.get(column)).getSlideDataName();
  			//System.out.println("exprNames " + name);
  			if(name.equals(expr)) return column;
  		}
  	  return -1;
    }
    /**
     * CGH Function
     * corresponds to ISlideData function
     */
    public int getNumFlankingRegions(int experimentIndex, int chromosomeIndex){
    	return ((ISlideData)featuresList.get(experimentIndex)).getNumFlankingRegions(chromosomeIndex);
    }
    /**
     * CGH Function
     * corresponds to ISlideData function
     */
    public void setFlankingRegions(int experimentIndex, Vector[] flankingRegions){
    	((ISlideData)featuresList.get(experimentIndex)).setFlankingRegions(flankingRegions);
    }
    /**
     * CGH Function
     * Setter for property samplesOrder.
     * @param samplesOrder New value of property samplesOrder.
     */
    public void setSamplesOrder(int[] samplesOrder) {
        this.samplesOrder = samplesOrder;
    }
    /**
     * CGH
     * Getter for property samplesOrder.
     * @return Value of property samplesOrder.
     */
    public int[] getSamplesOrder() {
        return this.samplesOrder;
    }
    /*
     * UN-used
    public Vector getSlides(){
        Vector slides = new Vector();
        Iterator it = featuresList.iterator();
        while(it.hasNext()){
            CGHSampleData sampleData = (CGHSampleData)it.next();
            Iterator cy3It = sampleData.getCy3Slides().iterator();
            while(cy3It.hasNext()){
                slides.add( (CGHSlideData) cy3It.next() );
            }
            Iterator cy5It = sampleData.getCy5Slides().iterator();
            while(cy5It.hasNext()){
                slides.add( (CGHSlideData) cy5It.next() );
            }
        }
        return slides;
    }
    */
    // Addition - Raktim, Oct 31, 05
    /**
     * @param experiment
     * @param clone
     * @param chromosome
     * @return
     */
    public float getValue(int experiment, int clone, int chromosome){
        //return getValue(experiment, data.getCloneIndex(clone, chromosome));
    	return getValue(experiment, getCloneIndex(clone, chromosome));
    }
    /** Returns the appropriate data value based on
     * the user selected clone value type.  This can be.
     * for example, the log average inverted value, or
     * any number of discrete copy number determination
     * methods
     * @param experiment
     * @param clone
     * @return
     */
    public float getValue(int experiment, int clone){
    	//System.out.println("Current Clone Value: getValue()." + cloneValueType);
    	//System.out.println("Current Clone Value: getValue()." + this.logState);
    	
        if(cloneValueType == ICGHCloneValueMenu.CLONE_VALUE_DISCRETE_DETERMINATION){
            return getCopyNumberDetermination(experiment, clone);
        }else if(cloneValueType == ICGHCloneValueMenu.CLONE_VALUE_LOG_AVERAGE_INVERTED){
            //return getLogAverageInvertedValue(experiment, clone);
            //return dataValues[clone][experiment];
        	//System.out.println("expr, clone: " + experiment + ":" + clone);
            return getRatio(experiment, clone, this.logState);
        }else if(cloneValueType == ICGHCloneValueMenu.CLONE_VALUE_LOG_CLONE_DISTRIBUTION){
            return getCopyNumberDeterminationByLogCloneDistribution(experiment, clone);
        }else if(cloneValueType == ICGHCloneValueMenu.CLONE_VALUE_THRESHOLD_OR_CLONE_DISTRIBUTION){
            return getCopyNumberDeterminationByThresholdOrCloneDistribution(experiment, clone);
        }
        return Float.NaN;
    }
    /**
     * For State Saving
     * CGH Function
     * @return
     */
    public int getCloneValueType(){
    	return this.cloneValueType;
    }
    
    /**
     * For State Saving
     * CGH Function
     * @param valType
     */
    public void setCloneValueType(int valType){
    	this.cloneValueType = valType;
    }
    /**
     * CGH Function
     * Calculates the log average inverted value
     * for a dye swap experiment.  Returns the log
     * value if the experiment was not done in dye swap
     */
    public float getLogAverageInvertedValue(int experiment, int clone){
        if(!hasDyeSwap){
            return getRatio(experiment, clone, LOG);
        }
        float cy3Ratio = getCY3(experiment, clone);
        float cy5Ratio = getCY5(experiment, clone);
        if(cy3Ratio == IData.BAD_CLONE || cy5Ratio == IData.BAD_CLONE){
            return Float.NaN;
        }
        if(cy3Ratio == 0 || cy5Ratio == 0){
            return Float.NaN;
        }
        try{
            float ratio = (float) (Arithmetic.log2(cy3Ratio) - Arithmetic.log2(cy5Ratio)) / 2;
            return ratio;
        }catch (Exception e){
            return Float.NaN;
        }
    }
    /** Alterts that the method of calculating a probe copy number
     * has changed.  This can be either a change in the determination
     * method or in the thresholds appropriate for classification
     *
     * @param menu
     */
    public void onCopyDeterminationChanged(ICGHCloneValueMenu menu){
        this.cloneValueType = menu.getCloneValueType();
        System.out.println("Print onCopyDeterminationChanged().this.cloneValueType " + this.cloneValueType);
        this.copyNumberCalculator.onCopyDeterminationChanged(menu);
    }
    /**
     * Raktim OCt 31, 05, CGH Function
     * Calculates the copy number of a probe based
     * on probe value thresholding
     * @param experiment
     * @param clone
     * @return
     */
    public int getCopyNumberDetermination(int experiment, int clone){
        return copyNumberCalculator.getCopyNumberDetermination(experiment, clone);
    }
    /**
     * Raktim Oct 31, 2005
     * @param experiment
     * @param clone
     * @param chromosome
     * @return  */
    public int getCopyNumberDetermination(int experiment, int clone, int chromosome){
        return copyNumberCalculator.getCopyNumberDetermination(experiment, clone, chromosome);
    }
    /**
     * Raktim OCt 31, 05, CGH Function
     * Calculates the copy number of a probe based
     * on the probe's normal distribution
     * @param experiment
     * @param clone
     * @return
     */
    public int getCopyNumberDeterminationByLogCloneDistribution(int experiment, int clone){
        return copyNumberCalculator.getCopyNumberDeterminationByLogCloneDistribution(experiment, clone);
    }
    /**
     * Raktim OCt 31, 05, CGH Function
     * Calculates the copy number of a probe based
     * on the probe's normal distribution or probe value thresholding
     * @param experiment
     * @param clone
     * @return
     */
    public int getCopyNumberDeterminationByThresholdOrCloneDistribution(int experiment, int clone){
        return copyNumberCalculator.getCopyNumberDeterminationByThresholdOrCloneDistribution(experiment, clone);
    }
    /** Calculates the p-value of the probe's value
     * based on the normal distribution curve corresponding
     * to the probe, for an experiment not done in dye swap
     * @param experiment
     * @param clone
     * @param logState
     * @return  */
    public float getPValueByLogCloneDistribution(int experiment, int clone){
        double ratio = getRatio(experiment, clone, this.logState);
        Distribution dist = getDistributionAt(clone);
        if(dist == null){
            return Float.NaN;
        }else{
            double mean = dist.getMean();
            double sd = dist.getSd();
            //double z = Probability.normal(mean, Descriptive.variance(sd), ratio);
            double z = (ratio - mean) / sd;
            double p = Probability.normal(z);
            return (float)p;
        }
    }
    /**
     * Raktim Oct31, 2005
     * CGH Function
     * Getter for property cloneDistributions.
     * @return Value of property cloneDistributions.
     */
    public Distribution[] getCloneDistributions() {
        return cloneDistributions;
    }
    /**
     * Raktim Oct31, 2005
     * CGH Function
     * Setter for property cloneDistributions.
     * @param cloneDistributions New value of property cloneDistributions.
     */
    public void setCloneDistributions(Distribution[] cloneDistributions) {
        this.cloneDistributions = cloneDistributions;
    }
    /**
     * Raktim Oct31, 2005
     * CGH Function
     * @param index
     * @return
     */
    public Distribution getDistributionAt(int index){
        try{
            return cloneDistributions[index];
        }catch (Exception e){
            return null;
        }
    }
    /**
     * Raktim October 31, 2005
     * CGH createExperiment Function
     */
    public Experiment createExperiment(Object results){
    	/*
        ExperimentWizard wiz = new ExperimentWizard(framework.getFrame());
        if(wiz.showModal() == javax.swing.JOptionPane.OK_OPTION){
        */
            //Object results = wiz.getResults();
            if(results instanceof BacClonesExperimentParameters){
                return createBacClonesExperiment((BacClonesExperimentParameters)results);
            }else if(results instanceof GenesExperimentParameters){
                return createGenesExperiment((GenesExperimentParameters)results);
            }else if(results instanceof DataRegionsExperimentParameters){
                return createDataRegionsExperiment((DataRegionsExperimentParameters)results);
            }
        /*
        }else{
            System.out.println("cancelled");
        }
        wiz = null;
        */
        return null;
    }
    /**
     * Raktim
     * Helper Method
     * @param chromosomeIndices
     * @return
     */
    private int getNumSelectedDataPoints(int[] chromosomeIndices){
        int numBacs = 0;
        for(int i = 0; i < chromosomeIndices.length; i++){
            if(chromosomeIndices[i] == BacClonesExperimentParameters.ALL_CHROMOSOMES){
                return getFeaturesSize();
            }
            numBacs += getNumDataPointsInChrom(chromosomeIndices[i]);
        }
        return numBacs;
    }
    /**
     * Raktim
     * Helper Method
     * @return
     */
    public int[] createDefaultColumns(){
        int[] defaultCols = new int[getFeaturesCount()];
        for(int i = 0; i < defaultCols.length; i++){
            defaultCols[i] = i;
        }
        return defaultCols;
    }
    /**
     * Raktim
     * Helper Method
     * @param row
     * @return
     */
    public boolean isMissingData(int row){
        for(int col = 0; col < getFeaturesCount(); col++){
            if(Float.isNaN(getRatio(row, col, this.logState))){
                return true;
            }
        }
        return false;
    }
    /**
     * Raktim Oct 31, 2005
     * @param parameters
     * @return
     */
    private CGHExperiment createBacClonesExperiment(BacClonesExperimentParameters parameters){
        if(parameters.isIncludeMissingBacs()){
            return createBacClonesExperimentAllValues(parameters);
        }else{
            return createBacClonesExperimentNoMissing(parameters);
        }
    }
    /**
     *
     * @param parameters
     * @return
     */
    private CGHExperiment createBacClonesExperimentAllValues(BacClonesExperimentParameters parameters){
        int[] chromosomeIndices = parameters.getChromosomeIndices();
        int numSelectedBacs = getNumSelectedDataPoints(chromosomeIndices);
        if(numSelectedBacs == getFeaturesSize()){
            return createBacClonesExperimentAllValuesAllChromosomes();
        }
        float[][] fmData = new float[numSelectedBacs][getFeaturesCount()];
        String[] annotations = new String[numSelectedBacs];
        int counter = 0;
        for(int chrom = 0; chrom < chromosomeIndices.length; chrom++){
            int chromosomeIndex = chromosomeIndices[chrom];
            for(int cloneIndex = 0; cloneIndex < getNumDataPointsInChrom(chromosomeIndex); cloneIndex++){
                for(int col = 0; col < getFeaturesCount(); col++){
                    fmData[counter][col] = getRatio(getCloneIndex(cloneIndex, chromosomeIndex), col, this.logState); //data.getRatio(col, cloneIndex, chromosomeIndex, 1);
                    annotations[counter] = getCloneAt(cloneIndex, chromosomeIndex).getName();
                }
                counter++;
            }
        }
        FloatMatrix fm = new FloatMatrix(fmData);
        CGHExperiment exp = new CGHExperiment(fm, createDefaultColumns(), annotations);
        return exp;
    }
    /**
     * Raktim
     * @return
     */
    private CGHExperiment createBacClonesExperimentAllValuesAllChromosomes(){
        float[][] fmData = new float[getFeaturesSize()][getFeaturesCount()];
        String[] annotations = new String[getFeaturesSize()];
        for(int row = 0; row < getFeaturesSize(); row++){
            for(int col = 0; col < getFeaturesCount(); col++){
                fmData[row][col] = getRatio(row,col,this.logState);
                annotations[row] = getCloneAt(row).getName();
            }
        }
        FloatMatrix fm = new FloatMatrix(fmData);
        CGHExperiment exp = new CGHExperiment(fm, createDefaultColumns(), annotations);
        return exp;
    }
    /**
     *
     * @param parameters
     * @return
     */
    private CGHExperiment createBacClonesExperimentNoMissing(BacClonesExperimentParameters parameters){
        int[] chromosomeIndices = parameters.getChromosomeIndices();
        int numSelectedBacs = getNumSelectedDataPoints(chromosomeIndices);
        if(numSelectedBacs == getFeaturesSize()){
            return createBacClonesExperimentAllChromosomesNoMissing();
        }
        float[][] fmDataTmp = new float[numSelectedBacs][getFeaturesCount()];
        String[] annotationsTmp = new String[numSelectedBacs];
        int counter = 0;
        for(int chrom = 0; chrom < chromosomeIndices.length; chrom++){
            int chromosomeIndex = chromosomeIndices[chrom];
            for(int cloneIndex = 0; cloneIndex < getNumDataPointsInChrom(chromosomeIndex); cloneIndex++){
                if(!isMissingData(getCloneIndex(cloneIndex, chromosomeIndex))){
                    for(int col = 0; col < getFeaturesCount(); col++){
                        //fmDataTmp[counter][col] = data.getRatio(col, cloneIndex, chromosomeIndex, 1);
                        fmDataTmp[counter][col] = getRatio(getCloneIndex(cloneIndex, chromosomeIndex), col, this.logState);
                        annotationsTmp[counter] = getCloneAt(cloneIndex, chromosomeIndex).getName();
                    }
                    counter++;
                }
            }
        }
        float[][] fmData = new float[counter][getFeaturesCount()];
        String[] annotations = new String[counter];
        for(int row = 0; row < counter; row++){
            for(int col = 0; col < getFeaturesCount(); col++){
                fmData[row][col] = fmDataTmp[row][col];
                annotations[row] = annotationsTmp[row];
            }
        }
        FloatMatrix fm = new FloatMatrix(fmData);
        CGHExperiment exp = new CGHExperiment(fm, createDefaultColumns(), annotations);
        return exp;
    }
    /**
     * Raktim
     * @return
     */
    private CGHExperiment createBacClonesExperimentAllChromosomesNoMissing(){
        float[][] fmDataTmp = new float[getFeaturesSize()][getFeaturesCount()];
        String[] annotationsTmp = new String[getFeaturesSize()];
        int counter = 0;
        for(int row = 0; row < getFeaturesSize(); row++){
            if(!isMissingData(row)){
                for(int col = 0; col < getFeaturesCount(); col++){
                    //fmDataTmp[counter][col] = data.getRatio(col, counter, 1);
                    fmDataTmp[counter][col] = getRatio(row, col, this.logState);
                    annotationsTmp[counter] = getCloneAt(counter).getName();
                }
                counter++;
            }
        }
        float[][] fmData = new float[counter][getFeaturesCount()];
        String[] annotations = new String[counter];
        for(int row = 0; row < counter; row++){
            for(int col = 0; col < getFeaturesCount(); col++){
                fmData[row][col] = fmDataTmp[row][col];
                annotations[row] = annotationsTmp[row];
            }
        }
        FloatMatrix fm = new FloatMatrix(fmData);
        CGHExperiment exp = new CGHExperiment(fm, createDefaultColumns(), annotations);
        return exp;
    }
    /**
     * Raktim
     * Should **NOT** be used without verification
     * @param parameters
     * @return
     */
    private CGHExperiment createGenesExperiment(GenesExperimentParameters parameters){
        File genesFile = parameters.getGenesFile();
        try{
            BufferedReader reader = new BufferedReader(new FileReader(genesFile));
            String line;
            Vector geneNames = new Vector();
            while((line = reader.readLine()) != null){
                geneNames.add(line);
            }
            GeneDataSet geneDataSet = new GeneDataSet();
            geneDataSet.loadGeneDataByGeneNames(geneNames, this.getCGHSpecies());
            Vector geneData = geneDataSet.getGeneData();
            float[][] fmData = new float[geneData.size()][getFeaturesCount()];
            String[] annotations = new String[geneData.size()];
            for(int row = 0; row < geneData.size(); row++){
                for(int col = 0; col < getFeaturesCount(); col++){
                    fmData[row][col] = getExperimentGeneValue((IGeneData)geneData.get(row), col);
                    annotations[row] = ((IGeneData)geneData.get(row)).getName();
                }
            }
            FloatMatrix fm = new FloatMatrix(fmData);
            CGHExperiment exp = new CGHExperiment(fm, createDefaultColumns(), annotations);
            return exp;
        }catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }
    /**
     * Helper for above
     * Should **NOT** be used
     * @param geneData
     * @param experiment
     * @return
     */
    private float getExperimentGeneValue(IGeneData geneData, int experiment){
        int geneStart = geneData.getStart();
        int geneEnd = geneData.getStop();
        if(geneData.getChromosomeIndex() < 0){
            System.out.println("Gene " + geneData.getName() + " Chrom = " + geneData.getChromosomeIndex());
            return 0;
        }
        ISlideData featureData = (ISlideData)getFeaturesList().get(experiment);
        Vector[] allFrs = featureData.getFlankingRegions();
        Vector chromFrs = allFrs[geneData.getChromosomeIndex()];
        Iterator it = chromFrs.iterator();
        while(it.hasNext()){
            FlankingRegion fr = (FlankingRegion)it.next();
            int frStart = fr.getStart();
            int frStop = fr.getStop();
            if( (geneStart < frStop && geneStart > frStart) || (geneEnd < frStop && geneStart > frStart)  ){
                if(fr.getType() == FlankingRegion.DELETION){
                    return -1;
                }else if(fr.getType() == FlankingRegion.AMPLIFICATION){
                    return 1;
                }
            }
        }
        return 0;
    }
    /**
     * Raktim
     * @param parameters
     * @return
     */
    private CGHExperiment createDataRegionsExperiment(DataRegionsExperimentParameters parameters){
        int[] chromosomeIndices = parameters.getChromosomeIndices();
        Vector dataRegions = new Vector();
        dataRegions.addAll(getDataRegionsValues(FlankingRegion.DELETION, chromosomeIndices));
        dataRegions.addAll(getDataRegionsValues(FlankingRegion.AMPLIFICATION, chromosomeIndices));
        float[][] fmData = new float[dataRegions.size()][getFeaturesCount()];
        String[] annotations = new String[dataRegions.size()];
        for(int row = 0; row < fmData.length; row++){
            fmData[row] = ((AlterationRegion)dataRegions.get(row)).getAlteredExperimentValues();
            annotations[row] = ((AlterationRegion)dataRegions.get(row)).getName();
        }
        FloatMatrix fm = new FloatMatrix(fmData);
        CGHExperiment exp = new CGHExperiment(fm, createDefaultColumns(), annotations);
        return exp;
    }
    /**
     * Helper for createDataRegionsExperiment(...)
     * @param flankingRegionType
     * @param chromosomeIndices
     * @return
     */
    public Vector getDataRegionsValues(int flankingRegionType, int[] chromosomeIndices){
        Vector allAlterationRegions = new Vector();
        //for(int chromIndex = 0; chromIndex < data.getNumChromosomes(); chromIndex++){
        for(int curChrom = 0; curChrom < chromosomeIndices.length; curChrom++){
            int chromIndex = chromosomeIndices[curChrom];
            AlterationRegions curAlterationRegions = new AlterationRegions(chromIndex);
            Vector curFlankingRegions = new Vector(getFeaturesCount());
            Iterator featuresIt = getFeaturesList().iterator();
            while(featuresIt.hasNext()){
                Vector curFrs = ((ISlideData)featuresIt.next()).getFlankingRegions()[chromIndex];
                curFlankingRegions.add( curFrs.clone() );
            }
            int[] indices = new int[getFeaturesCount()];
            for(int i = 0; i < getFeaturesCount(); i++){
                indices[i] = 0;
            }
            boolean more = true;
            while(more){
                for(int i = 0; i < indices.length; i++){
                    Vector expRegions = (Vector) curFlankingRegions.get(i);
                    while(indices[i] < expRegions.size() && ((FlankingRegion)expRegions.get(indices[i])).getType() != flankingRegionType  ){
                        indices[i] += 1;
                    }
                }
                int minStartIndex = getMinStartIndex(curFlankingRegions, indices);
                int minEndIndex = getMinEndIndex(curFlankingRegions, indices, minStartIndex);
                for(int i = 0; i < indices.length; i++){
                    Vector expRegions = (Vector) curFlankingRegions.get(i);
                    if(indices[i] < expRegions.size()){
                        FlankingRegion fr = (FlankingRegion) expRegions.get(indices[i]);
                        int start = fr.getStart();
                        int stop = fr.getStop();
                        if(start == minStartIndex){
                            AlterationRegion curAlterationRegion = curAlterationRegions.getAlterationRegion(minStartIndex, minEndIndex, flankingRegionType, getFeaturesCount());
                            curAlterationRegion.incrementAlterations();
                            if(flankingRegionType == FlankingRegion.AMPLIFICATION){
                                curAlterationRegion.getAlteredExperimentValues()[i] = 1;
                            }else{
                                curAlterationRegion.getAlteredExperimentValues()[i] = -1;
                            }
                            if(stop == minEndIndex){
                                indices[i] += 1;
                            }else{
                                FlankingRegion tmp = new FlankingRegion(minEndIndex, fr.getStop(),
                                flankingRegionType, chromIndex);
                                expRegions.set(indices[i], tmp);
                            }
                        }
                    }
                }
                more = false;
                for(int i = 0; i < indices.length; i++){
                    Vector expRegions = (Vector) curFlankingRegions.get(i);
                    if(indices[i] < expRegions.size()){
                        more = true;
                    }
                }
            }
            allAlterationRegions.addAll(curAlterationRegions.getAlterationRegions());
        }
        java.util.Collections.sort(allAlterationRegions, new AlterationRegionsComparator());
        return allAlterationRegions;
    }
    /**
     * Raktim
     * Helper for getDataRegionsValues
     * @param flankingRegions
     * @param indices
     * @return
     */
    private int getMinStartIndex(Vector flankingRegions, int[] indices){
        int min = Integer.MAX_VALUE;
        for(int i = 0; i < indices.length; i++){
            Vector expRegions = (Vector) flankingRegions.get(i);
            if(indices[i] < expRegions.size()){
                min = Math.min(min, ((FlankingRegion) expRegions.get(indices[i])).getStart());
            }
        }
        return min;
    }
    /**
     * Raktim
     * Helper for getDataRegionsValues
     * @param flankingRegions
     * @param indices
     * @param minStartIndex
     * @return
     */
    private int getMinEndIndex(Vector flankingRegions, int[] indices, int minStartIndex) {
        int min = Integer.MAX_VALUE;
        for(int i = 0; i < indices.length; i++){
            Vector expRegions = (Vector) flankingRegions.get(i);
            if(indices[i] < expRegions.size()){
                int start = ((FlankingRegion) expRegions.get(indices[i])).getStart();
                if(start == minStartIndex){
                    start = ((FlankingRegion) expRegions.get(indices[i])).getStop();
                }
                min = Math.min(min, start);
            }
        }
        return min;
    }
    /**
     * CGH Getter for property clones.
     * @return Value of property clones.
     */
    public java.util.ArrayList getClones() {
        return clones;
    }
    /**
     * CGH Setter for property clones.
     * @param clones New value of property clones.
     */
    public void setClones(java.util.ArrayList clones) {
        this.clones = clones;
    }
    /**
     * CGH
     * Getter for property annotations.
     * @return Value of property annotations.
     */
    public ICGHDataRegion[][] getAnnotations() {
        return this.annotations;
    }
    /**
     * CGH
     * Setter for property annotations.
     * @param annotations New value of property annotations.
     */
    public void setAnnotations(ICGHDataRegion[][] annotations) {
        this.annotations = annotations;
    }
    /**
     * CGH
     * Getter for data swap status.
     * @return
     */
    public boolean isHasDyeSwap() {
    	return this.hasDyeSwap;
    }
    /**
     * CGH
     * Setter for property hasDyeSwap.
     * @param hasDyeSwap New value of property hasDyeSwap.
     */
    public void setHasDyeSwap(boolean hasDyeSwap) {
        this.hasDyeSwap = hasDyeSwap;
        if(hasDyeSwap){
            copyNumberCalculator = new CGHCopyNumberCalculator(this);
        }else{
            copyNumberCalculator = new CGHCopyNumberCalculatorNoDyeSwap(this);
        }
    }
    /**
     * Data is set to CGH Type
     */
    public void setCGHData(){
    	CGHData = true;
    }
    /**
     * Return true if data is CGH type
     * @return
     */
    public boolean isCGHData() {
    	return CGHData;
    }
    /**
     * Data is set to CGH Type
     */
    public void setLog2Data(boolean isLog2){
    	log2Data = isLog2;
    }
    /**
     * Return true if data is CGH type
     * @return
     */
    public boolean isLog2Data() {
    	return log2Data;
    }
    /**
     * Setter for CGH Data. Species type.
     * @param species
     */
    public void setCGHSpecies(int species){
    	if (species == TMEV.CGH_SPECIES_HS){
    		CGH_SPECIES = TMEV.CGH_SPECIES_HS;
    	} else if (species == TMEV.CGH_SPECIES_MM){
    		CGH_SPECIES = TMEV.CGH_SPECIES_MM;
    	} else {
    		CGH_SPECIES = TMEV.CGH_SPECIES_Undef;
    	}
    }
    /**
     * Return CGH data Species
     * @return
     */
    public int getCGHSpecies(){
    	return CGH_SPECIES;
    }
    public void setHasCloneDistribution(boolean cloneDistribution) {
    	hasCloneDistribution = cloneDistribution;
    }
    public boolean hasCloneDistribution() {
    	return hasCloneDistribution;
    }
    
    /**
     * ChARM CGH function
     * Uses 0 based Chr number
     * @return
     */
    public float getChromWidth(int chr) {
    	float width = 0.0f;
    	int st = getChromosomeStartIndex(chr-1);
        int end = getChromosomeEndIndex(chr-1);
        width = ((CGHClone)this.getCloneAt(end)).getStop() - ((CGHClone)this.getCloneAt(st)).getStart();
        return width;
    }
    /*******************************************************************************
     * Raktim
     * End CGH Functions
     ******************************************************************************/

	//EH state-saving
    public String getCurrentSampleLabelKey() {
    	//return ((ISlideData)featuresList.get(0)).getSlideDataName();
    	//Raktim 10.4. Commented out the above code. It was returning the SampleLabel instead of the key
    	
    	return ((ISlideData)featuresList.get(0)).getSampleLabelKey();
    }
    public static PersistenceDelegate getPersistenceDelegate() {
    	return new MultipleArrayDataPersistenceDelegate();
    }
    public ClusterRepository getExperimentClusterRepository() {
    	return expClusterRepository;
    }
    public ClusterRepository getGeneClusterRepository() {
    	return geneClusterRepository;
    }
    public MultipleArrayData(boolean useMainData) {
    	this.useMainData = useMainData;
    }
    public Experiment getAltExperiment() {
    	return alternateExperiment;
    }

    public ArrayList getExperimentColorsSaved(){return experimentColors;}
    //EH end state-saving
	/**
	 * @return Returns the maxCy3.
	 */
	public float getMaxCy3() {
		return maxCy3;
	}
	/**
	 * @param maxCy3 The maxCy3 to set.
	 */
	public void setMaxCy3(float maxCy3) {
		this.maxCy3 = maxCy3;
	}
	/**
	 * @return Returns the maxCy5.
	 */
	public float getMaxCy5() {
		return maxCy5;
	}
	/**
	 * @param maxCy5 The maxCy5 to set.
	 */
	public void setMaxCy5(float maxCy5) {
		this.maxCy5 = maxCy5;
	}
	public boolean isAnnotationLoaded() {
		return mads.isAnnotationLoaded();
	}
	
	public void setAnnotationLoaded(boolean isAnnotationLoaded) {
		this.mads.setAnnotationLoaded(isAnnotationLoaded);
	}
	
	public boolean isSampleAnnotationLoaded() {
		return mads.isSampleAnnotationLoaded;
	}
	
	public void setSampleAnnotationLoaded(boolean isAnnLoaded) {
		this.mads.setSampleAnnotationLoaded(isAnnLoaded);
		
	}
	
	public MageIDF getIDFObject() {

		return mads.getMageIDFObject();
	}
	
	public void setIDF(MageIDF idfObj) {
		
	   this.IDFObject=idfObj;
		mads.setMageIDFObject(idfObj);
	}
	

}
