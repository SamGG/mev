/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * $RCSfile: MultipleArrayData.java,v $
 * $Revision: 1.6 $
 * $Date: 2004-02-27 22:19:13 $
 * $Author: braisted $
 * $State: Exp $
 */

package org.tigr.microarray.mev;

import java.awt.Color;
import java.awt.event.*;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Properties;

import java.io.Serializable;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;

import javax.swing.JOptionPane;

import org.tigr.microarray.util.Adjustment;
import org.tigr.util.FloatMatrix;
import org.tigr.microarray.util.SlideDataSorter;

import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.Progress;

import org.tigr.microarray.mev.cluster.clusterUtil.*;
import org.tigr.microarray.mev.cluster.gui.impl.normalization.LinRegNormInitDialog;
import org.tigr.microarray.mev.cluster.gui.impl.normalization.RatioStatsNormInitDialog;
import org.tigr.microarray.mev.cluster.gui.impl.normalization.IterativeLogMCNormInitDialog;

import org.tigr.midas.engine.Parameter;

public class MultipleArrayData implements IData, java.io.Serializable {
    
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
    
    private float lowerCY3Cutoff = 0f;
    private float lowerCY5Cutoff = 0f;
    private boolean useLowerCutoffs = false;
    
    private Progress progressBar;
    private boolean normalizationAbort = false;
    
    // pcahan
    private DetectionFilter detectionFilter;
    private FoldFilter foldFilter;
    private boolean useDetectionFilter = false;
    private boolean dfSet = false;
    private boolean useFoldFilter = false;
    
    private ClusterRepository geneClusterRepository;
    private ClusterRepository expClusterRepository;
    
    private int logState = LOG;
    
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
    
    /**
     * Returns the use  DetectionFilter.
     */
    public boolean isDetectionFilter() {
        return useDetectionFilter;
    }
    
    public boolean isFoldFilter() {
        return useFoldFilter;
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
        
        String detection_call;
        
        if (column > 0){
            FloatSlideData slideData = (FloatSlideData)featuresList.get(column);
            detection_call = slideData.getDetection(row);
        }
        else {
            
            SlideData slideData = (SlideData)featuresList.get(column);
            ISlideDataElement element = slideData.getSlideDataElement(row);
            detection_call = element.getDetection();
            
        }
        return detection_call;
    }
    
    // end affy specific methods ********************************************
    
    /**
     * Returns the use percentage cutoff value.
     */
    public boolean isPercentageCutoff() {
        return usePercentageCutoff;
    }
    
    /**
     * Returns the lower CY3 cutoff value.
     */
    public float getLowerCY3Cutoff() {
        return lowerCY3Cutoff;
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
    
    /**
     * Returns full feature name.
     */
    public String getFullSampleName(int column) {
        return((ISlideData)featuresList.get(column)).getFullSlideDataName();
    }
    
    /**
     * Returns an element attribute for specified row and
     * attribute index.
     */
    public String getElementAttribute(int row, int attr) {
        if (featuresList.size() == 0) {
            return "";
        }
        ISlideData slideData = (ISlideData)featuresList.get(0);
        ISlideDataElement element = slideData.getSlideDataElement(row);
        return element.getFieldAt(attr);
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
     * Returns all annotation fields
     */
    public String[] getFieldNames() {
        return TMEV.getFieldNames();
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
     * Returns array of published colors.
     */
    public Color[] getColors() {
        updateSpotColors();
        Color[] colors = new Color[spotColors.size()];
        return(Color[])spotColors.toArray(colors);
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
                }
                else{
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
                    System.out.println("Abort");
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
        if ((isLowerCutoffs() || isPercentageCutoff()) || ( (TMEV.getDataType() == TMEV.DATA_TYPE_AFFY) && (isDetectionFilter() || isFoldFilter())) ) {
            //features = createCutoffFeatures(featuresSize, probesSize);
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
    
    //modified from original above ( by pcahan)
    
    private int[] createCutoffGeneList(final int featuresSize, final int probesSize) {
        ISlideData[] slideData = new ISlideData[featuresSize];
        //float cy3, cy5;
        ArrayList list = new ArrayList();
        boolean lowerCutoffCriterion = true;
        boolean percentageCutoffCriterion = true;
        boolean detectionCriterion = true;
        boolean foldCriterion = true;
        
        int percentageCount = 0;
        int absentCount = 0;
        
        // get each chip
        for (int i = 0; i < slideData.length; i++) {
            slideData[i] = getFeature(i);
        }
        
        // iterate over each gene
        for (int probe = 0; probe < probesSize; probe++) {
            
            // arrays of length = number of chips
            float[] cy3 = new float[featuresSize];
            float[] cy5 = new float[featuresSize];
            String[] detection = new String[featuresSize];
            
            percentageCount = 0;
            absentCount = 0;
            lowerCutoffCriterion = true;
            percentageCutoffCriterion = true;
            detectionCriterion = true;
            foldCriterion = true;
            
            // iterate over each chip
            for (int j = 0; j < cy3.length; j++) {
                cy3[j] = slideData[j].getCY3(probe);
                cy5[j] = slideData[j].getCY5(probe);
                detection[j] = getDetection(j, probe);
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
                    if ((cy3[j] > 0) && (cy5[j] > 0)) {
                        percentageCount++;
                    }
                }
                
                if ((float)percentageCount/(float)featuresSize*100f < percentageCutoff) {
                    percentageCutoffCriterion = false;
                }
                
            }
            
            // pcahan
            if (isDetectionFilter()){
                detectionCriterion = detectionFilter.keep_gene(detection);
            }
            
            if (isFoldFilter() ){
                foldCriterion = foldFilter.keep_gene(cy5);
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
            if (TMEV.getDataType() == TMEV.DATA_TYPE_AFFY){
                for (int row = rows; --row >= 0; ) {
                    fm.A[row][columns[i]] = sd.getRatio(row, LINEAR);
                }
            } else {
                for (int row = rows; --row >= 0;) {
                    fm.A[row][columns[i]] = sd.getRatio(row, this.logState);
                }
            }
        }
        return new Experiment(fm, columns);
    }
    
    //The following method was added to correct the way "set Lower Cutoffs" and "Set %age cutoffs" is handled, i.e., to trim out rows (genes), rather than columns (experiments)
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
          if (TMEV.getDataType() == TMEV.DATA_TYPE_AFFY) {

            for (int j = 0; j < rows.length; j++) {
              fm.A[j][i] = sd.getRatio(rows[j], LINEAR);
            }
          } else {                      
              for (int j = 0; j < rows.length; j++) {              
                  fm.A[j][i] = sd.getRatio(rows[j], this.logState);                
              }
          }
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
        data.setDataType(this.dataType);
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
                    slideData.setSlideDataName(this.getSampleName(slide));
                    ((SlideData) slideData).setSlideFileName(this.getSampleName(slide));
                    
                    toggleExptNameLength();
                } else{
                    slideData.setSlideDataName(this.getSampleName(slide));
                    ((SlideData) slideData).setSlideFileName(this.getSampleName(slide));
                }
                
                for(int spot = 0; spot < indices.length; spot++){
                    index = indices[spot];
                    sde = new SlideDataElement(this.getSlideDataElement(slide, index));
                    slideData.addSlideDataElement(sde);
                }
                
                metaData = (ISlideMetaData)slideData;
                
            } else{
                slideData = new FloatSlideData(metaData);
                ((FloatSlideData) slideData).createCurrentIntensityArrays();
                
                name = this.getSampleName(slide);
                if(name.endsWith("...")){
                    toggleExptNameLength();
                    slideData.setSlideDataName(this.getSampleName(slide));
                    ((FloatSlideData) slideData).setSlideFileName(this.getSampleName(slide));
                    
                    toggleExptNameLength();
                } else{
                    slideData.setSlideDataName(this.getSampleName(slide));
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
        }
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
        data.setDataType(this.dataType);
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
                    slideData.setSlideDataName(this.getSampleName(slideIndex));
                    ((SlideData) slideData).setSlideFileName(this.getSampleName(slideIndex));
                    toggleExptNameLength();
                } else{
                    slideData.setSlideDataName(this.getSampleName(slideIndex));
                    ((SlideData) slideData).setSlideFileName(this.getSampleName(slideIndex));
                }
                
                for(int spot = 0; spot < rowIndices.length; spot++){
                    sde = new SlideDataElement(this.getSlideDataElement(slideIndex, rowIndices[spot]));
                    slideData.addSlideDataElement(sde);
                }
                metaData = (ISlideMetaData)slideData;
                
            } else{
                slideData = new FloatSlideData(metaData);
                ((FloatSlideData) slideData).createCurrentIntensityArrays();
                
                name = this.getSampleName(slideIndex);
                if(name.endsWith("...")){
                    toggleExptNameLength();
                    slideData.setSlideDataName(this.getSampleName(slideIndex));
                    ((FloatSlideData) slideData).setSlideFileName(this.getSampleName(slideIndex));
                    toggleExptNameLength();
                } else{
                    slideData.setSlideDataName(this.getSampleName(slideIndex));
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
    public Experiment getExperiment() {
        return experiment;
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
        } 

        if(this.getFeaturesCount() > 0)
            this.experiment = createExperiment();
    }
    
    /** Returns an annotation array for the provided indices based on annotation key
     */
    public String[] getAnnotationList(String fieldName, int[] indices) {
        String [] fieldNames = this.getFieldNames();
        int fieldIndex;
        for(fieldIndex = 0; fieldIndex < fieldNames.length; fieldIndex++){
            if(fieldName == fieldNames[fieldIndex])
                break;
        }
        if(fieldIndex > fieldNames.length)
            return null;
        
        String [] annot = new String[indices.length];
        
        for(int i = 0; i < annot.length; i++){
            annot[i] = this.getElementAttribute(indices[i], fieldIndex);
        }
        
        return annot;
    }
    
    private void writeObject(ObjectOutputStream oos) throws IOException, ClassNotFoundException{
        oos.writeObject(TMEV.getFieldNames());
        
        oos.writeObject(featuresList);  //ArrayList
        oos.writeObject(indicesList); //ArrayList
        
        oos.writeObject(spotColors);  //ArrayList
        oos.writeObject(colorIndices); //int []
        
        oos.writeObject(experimentColors); //ArrayList
        oos.writeObject(experimentColorIndices); // int []
        oos.writeObject(experiment); //Experiment
        oos.writeInt(dataType); //int
        
        oos.writeFloat(maxCy3);
        oos.writeFloat(maxCy5);
        oos.writeFloat(maxRatio);
        oos.writeFloat(minRatio);
        
        oos.writeFloat(percentageCutoff);
        oos.writeBoolean(usePercentageCutoff);
        
        oos.writeFloat(lowerCY3Cutoff);
        oos.writeFloat(lowerCY5Cutoff);
        oos.writeBoolean(useLowerCutoffs);
        
        // pcahan
        if(dataType == TMEV.DATA_TYPE_AFFY){
        /*
        private DetectionFilter detectionFilter;
    private FoldFilter foldFilter;
    private boolean useDetectionFilter = false;
    private boolean dfSet = false;
    private boolean useFoldFilter = false;
         **/
        }
        //private ClusterRepository geneClusterRepository;
        //private ClusterRepository expClusterRepository;
    }
    
    private void readObject(ObjectInputStream ois)throws IOException, ClassNotFoundException{
        TMEV.setFieldNames((String [])ois.readObject());
        
        featuresList = (ArrayList)ois.readObject();  //ArrayList
        indicesList = (ArrayList)ois.readObject(); //ArrayList
        
        spotColors = (ArrayList)ois.readObject();  //ArrayList
        colorIndices = (int [])ois.readObject(); //int []
        
        experimentColors = (ArrayList)ois.readObject(); //ArrayList
        experimentColorIndices = (int [])ois.readObject(); // int []
        experiment = (Experiment)ois.readObject(); //Experiment
        dataType = ois.readInt(); //int
        
        
      /*
    private float maxCy3 = 0f;
    private float maxCy5 = 0f;
    private float maxRatio = 0f;
    private float minRatio = 0f;
       */
        maxCy3 = ois.readFloat();
        maxCy5 = ois.readFloat();
        maxRatio = ois.readFloat();
        minRatio = ois.readFloat();
        
        percentageCutoff = ois.readFloat();
        usePercentageCutoff = ois.readBoolean();
        //    private float percentageCutoff = 0f;
        //    private boolean usePercentageCutoff = false;
        
        lowerCY3Cutoff = ois.readFloat();
        lowerCY5Cutoff = ois.readFloat();
        useLowerCutoffs = ois.readBoolean();
        //    private float lowerCY3Cutoff = 0f;
        //   private float lowerCY5Cutoff = 0f;
        // private boolean useLowerCutoffs = false;
        
        // private Progress progressBar;
        //    private boolean normalizationAbort = false;
        
        // pcahan
        if(dataType == TMEV.DATA_TYPE_AFFY){
            
        /*
        private DetectionFilter detectionFilter;
    private FoldFilter foldFilter;
    private boolean useDetectionFilter = false;
    private boolean dfSet = false;
    private boolean useFoldFilter = false;
         **/
        }
        //private ClusterRepository geneClusterRepository;
        //private ClusterRepository expClusterRepository;
    }
    
    
}
