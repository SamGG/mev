/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: SlideData.java,v $
 * $Revision: 1.19 $
 * $Date: 2007-12-19 21:39:35 $
 * $Author: saritanair $
 * $State: Exp $
 */
package org.tigr.microarray.mev;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import org.tigr.microarray.mev.annotation.AnnoAttributeObj;
import org.tigr.microarray.mev.annotation.AnnotationFieldConstants;
import org.tigr.microarray.mev.annotation.ChipAnnotationFieldConstants;
import org.tigr.microarray.mev.annotation.IAnnotation;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.file.AgilentFileParser;
import org.tigr.microarray.mev.sampleannotation.SampleAnnotation;
import org.tigr.midas.engine.IterativeLinReg;
import org.tigr.midas.engine.IterativeLogMean;
import org.tigr.midas.engine.RatioStats;
import org.tigr.midas.engine.TotInt;
import org.tigr.midas.util.ColumnWorker;
import org.tigr.util.Xcon;
import org.tigr.util.math.LinearEquation;

import java.lang.reflect.Field;


public class SlideData implements ISlideData, ISlideMetaData {
    private Vector allSlideDataElements;
    private String slideDataName;
    private String slideFileName;
    private SpotInformationData spotInfoData;
    private int normalizedState = 0;
    private int sortState = 0;
    private int rows, columns;
    private boolean isNonZero = true;
    private boolean abbrName = false;
    private int dataType = IData.DATA_TYPE_TWO_INTENSITY;
    private String[] fieldNames = new String[0]; 

    //Support multiple sample labels
    private String sampleLabelKey = IData.DEFAULT_SAMPLE_ANNOTATION_KEY;
    private Hashtable sampleLabels;
    private Vector sampleLabelKeys;
    
    /*
     * @author Sarita Nair
     * Instance of new SampleAnnotation object
     */
    private SampleAnnotation sampAnnotation;
    private boolean isSampleAnnotationLoaded=false;
    
    
    private String[] loadedAnnotationFields;

    /**
     * Raktim Oct 31, 2005
     * CGH FlankingRegions, and Slide Constants
     * Vector of vectors of FlankingRegions corresponding to flanking regions
     * for each chromosome
     */
    private Vector[] flankingRegions;

    public boolean isCGHData(){return allSlideDataElements.get(0) instanceof CGHSlideDataElement;}
    
    
    
    
    /**
     * @author sarita 
     * SampleAnnotation constructor
     * Constructs a <code>SlideData</code> with specified dimension.
     */
    public SlideData(int rows, int columns, SampleAnnotation sampAnn) {
    	allSlideDataElements = new Vector(rows * columns);
        this.rows = rows;
        this.columns = columns;
        this.sampAnnotation=sampAnn;

    }

    
    
    
    
    /**
     * Constructs a <code>SlideData</code> which is a copy of specified original.
     */
    public SlideData(ISlideData original) {
        super();
        this.allSlideDataElements = new Vector();
        this.rows = original.getSlideMetaData().getRows();
        this.columns = original.getSlideMetaData().getColumns();
        this.sortState = original.getSortState();
        this.normalizedState = original.getNormalizedState();
        this.slideDataName = original.getSlideDataName();

        for (int i = 0; i < original.getSize(); i++) {
            addElement(original.getSlideDataElement(i).copy());
        }
        sampleLabelKey = "Default Slide Name";
        sampleLabelKeys = original.getSlideDataKeys();
        sampleLabels = original.getSlideDataLabels();
    	if(this.fieldNames == null)
    		this.fieldNames = new String[0];   
    	updateFilledAnnFields();
    }

    /**
     * Constructs a <code>SlideData</code> with specified dimension.
     */
    public SlideData(int rows, int columns) {
    	allSlideDataElements = new Vector(rows * columns);
        this.rows = rows;
        this.columns = columns;
        sampleLabelKey = "Default Slide Name";
        sampleLabelKeys = new Vector();
        sampleLabels = new Hashtable();
    	if(this.fieldNames == null)
    		this.fieldNames = new String[0];
    	updateFilledAnnFields();

    }

    /**
     * Constructs a <code>SlideData</code> with specified size.
     */
    public SlideData(int size) {
    	allSlideDataElements = new Vector(size);
        rows = -1;
        columns = -1;
        sampleLabelKey = "Default Slide Name";
        sampleLabelKeys = new Vector();
        sampleLabels = new Hashtable();
    	if(this.fieldNames == null)
    		this.fieldNames = new String[0];
    	updateFilledAnnFields();

    }

    /**
     * Constructs a <code>SlideData</code> instance.
     */
    public SlideData() {
    	allSlideDataElements = new Vector();
    	if(this.fieldNames == null)
    		this.fieldNames = new String[0];
    	updateFilledAnnFields();

    }
    
    //
    //EH begin classes added for state-saving update
    //
    /**
     * This constructor is designed to re-create a previously built SlideData
     * from parameters stored in an XML file (this file is written by the MultipleArrayViewer.saveState() method).
     * The String[] returned by the method FloatSlideData.getPersistenceDelegateArgs() 
     * should reflect the names and order of the values passed to this constructor.  
     * 
     */
      public SlideData(String slideDataName, Vector sampleLabelKeys, String sampleLabelKey,
    		Hashtable sampleLabels, String slideFileName, Boolean isNonZero, Integer rows, Integer columns,
			Integer normalizedState, Integer sortState, SpotInformationData spotInfoData, 
			String[] fieldNames, Integer dataType/*, String annotationFileName, String inputFileName*/) throws IOException {
      	this();
      	this.rows = rows.intValue();
    	this.columns = columns.intValue();
    	this.slideDataName = slideDataName;
    	this.sampleLabelKeys = sampleLabelKeys;
    	this.sampleLabelKey = sampleLabelKey;
    	this.sampleLabels = sampleLabels;
    	this.slideFileName = slideFileName;
    	this.normalizedState = normalizedState.intValue();
    	this.sortState = sortState.intValue();
    	this.spotInfoData = spotInfoData;
    	this.isNonZero = isNonZero.booleanValue();
    	this.fieldNames = fieldNames;
    	this.dataType = dataType.intValue();
    	//loadAnnotation(new DataInputStream(new FileInputStream(System.getProperty("java.io.tmpdir") + MultipleArrayViewer.CURRENT_TEMP_DIR + System.getProperty("file.separator") + annotationFileName)));
    	//readDataFromInputFile(inputFileName);
    	updateFilledAnnFields();

    }
      
    /**
	* Returns the list of "old data model" field names loaded as part of 
	* outside of the MevAnnotation objects.
	**/  
    public String[] getOldModelFieldNames() {
    	return fieldNames;
    }
      
    

   /**
    * @author sarita
    * @return SampleAnnotation object
    *  
    */
      
   public SampleAnnotation getSampleAnnotation(){
	   return this.sampAnnotation;
   }
   
   
   /**
    * @author sarita 
    * @param sampAnn
    */
   
   public void setSampleAnnotation(SampleAnnotation sampAnn){
	   this.sampAnnotation=sampAnn;
   }
   
   

	public String getSampleLabelKey() {return sampleLabelKey;}


    public Vector getAllElements() {
    	return allSlideDataElements;
    }
    
    public void setAllElements(Vector v){
    	this.allSlideDataElements = v;
    }
    public void add(SlideDataElement newElement) {
    	this.allSlideDataElements.add(newElement);
    }
    
    public int size() {
    	return allSlideDataElements.size();
    }
    public Object elementAt(int i){
    	return allSlideDataElements.elementAt(i);
    }
    public void addElement(Object o){
    	allSlideDataElements.addElement(o);
    }
    public void add(Object o){
    	allSlideDataElements.add(o);
    }
    public Object set(int index, Object element){
    	return allSlideDataElements.set(index, element);
    }
    public void setElementAt(Object element, int index){
    	allSlideDataElements.setElementAt(element, index);
    }
    public void insertElementAt(Object o, int i){
    	allSlideDataElements.insertElementAt(o, i);
    }
    //
    // EH - end methods added for state-saving update
    //
    // begin methods brought in from TMEV.java
    public void setFieldNames(String [] fieldNames){
        this.fieldNames = fieldNames;
    }
    public void appendFieldNames(String [] fieldNames){
        if(this.fieldNames == null || fieldNames == null)  //trying to set to null or initial set
            this.fieldNames = fieldNames;
        else {                  //names exist and new names exist, APPEND (ie. mev format, extra ann load)
            String [] newNames = new String[this.fieldNames.length+fieldNames.length];
            System.arraycopy(this.fieldNames, 0, newNames, 0, this.fieldNames.length);
            System.arraycopy(fieldNames, 0, newNames, this.fieldNames.length, fieldNames.length);
            this.fieldNames=new String[newNames.length];
            this.fieldNames = newNames;
        }
    }
    public void clearFieldNames(){
        this.fieldNames = null;
    }
    // end methods brought in from TMEV
    


    /**
     * Sets the data type attribute see static type variables in <code>IData</code>
     */
    public void setDataType(int type){
        this.dataType = type;
    }

    /**
     * Returns the data type attribute
     */
    public int getDataType(){
        return this.dataType;
    }

    /**
     *  Sets the spot information data and associated column headers.
     */
    public void setSpotInformationData(String [] columnHeaders, String [][] spotInfoData){
        this.spotInfoData = new SpotInformationData(columnHeaders, spotInfoData);
    }

    /**
     *  Sets the spot information data and associated column headers.
     */
    public void setSpotInformationData(SpotInformationData spotInfoData){
        this.spotInfoData = spotInfoData;
    }
    
    
    /**
     * 
     * @returns the annotations contained in the expressiondata
     */
    public String[]getOldFieldNames(){
    	return this.fieldNames;
    }
    
    
    public String[] getFieldNames() {
	    List<String> allAnns = new ArrayList<String>();
	    Collections.addAll(allAnns, fieldNames);
	    Collections.addAll(allAnns, loadedAnnotationFields);
	    return allAnns.toArray(new String[] {});
    }
    /**
     * Checks through the annotation loaded into the ISlideDataElements in allSlideDataElements
     * and updates {@link loadedAnnotationFields} with those field names.
     * @return
     */
    public void updateFilledAnnFields() {
	    HashMap<String,String> filledFields = new HashMap<String,String>();
	    IAnnotation anno;
	    Field[] temp = AnnotationFieldConstants.class.getFields();
	    for(int i=0; i<temp.length; i++) {
		    String thisFieldName = temp[i].getName();
		    for(int j=0; j<allSlideDataElements.size(); j++) {
			    anno = ((ISlideDataElement)allSlideDataElements.get(j)).getElementAnnotation();
			    if(anno != null && !anno.getAttribute(thisFieldName)[0].equals(ChipAnnotationFieldConstants.NOT_AVAILABLE)) {
				    filledFields.put(thisFieldName, "");
				    break;
			    }
		    }
	    }
		
	    this.loadedAnnotationFields = filledFields.keySet().toArray(new String[filledFields.keySet().size()]);
    }
    
    /**
     * Returns the <code>SpotInformationData</code> object.
     */
    public SpotInformationData getSpotInformationData(){
        return this.spotInfoData;
    }

    /**
     * Returns an <code>ISlideDataElement</code> by specified index.
     */
    public ISlideDataElement getSlideDataElement(int index) {
        return(ISlideDataElement)elementAt(index);
    }

    /**
     * Returns non-zero attribute of an element with specified index.
     */
    public boolean hasNoZeros(int index) {
        ISlideDataElement sde = (ISlideDataElement)elementAt(index);
        return sde.hasNoZeros();
    }

    /**
     * Returns copy of an element with specified index.
     */
    public ISlideDataElement toSlideDataElement(int index) {
        return((ISlideDataElement)elementAt(index)).copy();
    }

    /**
     * Returns CY3 value for specified index.
     */
    public float getCY3(int index) {
        ISlideDataElement sde = (ISlideDataElement)elementAt(index);
        if(normalizedState == ISlideData.NO_NORMALIZATION)
            return sde.getTrueIntensity(ISlideDataElement.CY3);
        else
            return sde.getIntensity(ISlideDataElement.CY3);
    }

    /**
     * Returns CY5 value for specified index.
     */
    public float getCY5(int index) {
        ISlideDataElement sde = (ISlideDataElement)elementAt(index);
        if(normalizedState == ISlideData.NO_NORMALIZATION){
            //  System.out.println("making experiment, no norm");
            return sde.getTrueIntensity(ISlideDataElement.CY5);
        }
        else{
            //  System.out.println("making experiment, NORM!!");
            return sde.getIntensity(ISlideDataElement.CY5);
        }
    }

    /**
     * Sets intensities for a spot with specified index.
     */
    public void setIntensities(int index, float cy3, float cy5) {
        ISlideDataElement sde = (ISlideDataElement)elementAt(index);
        sde.setTrueIntensity(ISlideDataElement.CY3, cy3);
        sde.setTrueIntensity(ISlideDataElement.CY5, cy5);
    }

    /**
     * Returns a ratio value with specified index and log state.
     */
    public float getRatio(int index, int logState) {
        return getRatio(getCY5(index), getCY3(index), logState);
    }

    /**
     * Returns a ratio of specified values.
     */
    public final float getRatio(float numerator, float denominator, int logState) {
    	
    	
        if(dataType == IData.DATA_TYPE_RATIO_ONLY)
            return numerator;

        float ratio;
        if(denominator < 0 || numerator < 0)
            return numerator;
        if (isNonZero) {
            if (denominator == 0 && numerator == 0) {
                return Float.NaN;
            } else if (numerator == 0) {
                ratio = 1f/denominator;
            } else if (denominator == 0) {
                ratio = numerator;
            } else {
                ratio = numerator/denominator;
            }
        } else {
            if (denominator <= 0)
                return Float.NaN;
            if (numerator <= 0)
                return Float.NaN;
            ratio = numerator/denominator;
        }
        if (logState == IData.LOG)
            ratio = (float)(Math.log(ratio)/Math.log(2.0));
        return ratio;
    }

    /**
     * Returns reference to a microarray meta data.
     */
    public ISlideMetaData getSlideMetaData() {
        return this;
    }

    /**
     * Adds an <code>ISlideDataElement</code> to a microarray.
     */
    public void addSlideDataElement(ISlideDataElement element) {
        addElement(element);
    }

    /**
     * Returns an element value of specified type.
     * @deprecated
     */
    public String getValueAt(int index, int valueType) {
        ISlideDataElement sde = (ISlideDataElement)elementAt(index);
        return sde.getFieldAt(valueType);
    }
    
    /**
     * Returns the annotation values corresponding to attr for the row indicated by index. Includes both
     * the new and old annotation model values. 
     * @param index the row of the gene for which annotation should be returned
     * @param attr the type of annotation to be returned
     * @return the annotation values for row index, type attr.
     */
    public String[] getAnnotationValue(int index, String attr) {
    	
		ISlideDataElement element = getSlideDataElement(index);
		IAnnotation annot = element.getElementAnnotation();
		
		if (annot != null && annot.getAttribute(attr) != null && annot.getAttribute(attr).length > 0 && !annot.getAttribute(attr)[0].equalsIgnoreCase("NA")) {
			return (annot.getAttribute(attr));
		}else {
			//Had to write up getOldFieldNames because getFieldNames now returns all annotation fields including ones from new annotation model
			//However, getFieldAt only checks for the extra gene annotations that were within the file and throws arrayindex out of bounds
			int attribIndex=Arrays.asList(getOldFieldNames()).indexOf(attr);
			if(attribIndex!=-1)
				return new String[] { element.getFieldAt(attribIndex) };
			else
				return new String[] { "NA" };
		}

//		String[] allFields = getFieldNames();
//		for (int i = 0; i < allFields.length; i++) {
//		
//			 if (allFields[i].equals(attr)) {
//				if (!element.getFieldAt(i).equals("")) {
//			//	if (element!=null && element.getFieldAt(i).trim().length()!=0 && element.getFieldAt(i).length()!=0 && element.getFieldAt(i)!=null) {
//					return new String[] { element.getFieldAt(i) };
//				} else
//					return new String[] { "NA" };
//			}
//		}
		//return new String[] { "NA" };
	}
    
    public AnnoAttributeObj getAnnotationObj(int index, String attr) {
		ISlideDataElement element = getSlideDataElement(index);

		//Try to get an annotation object from the slidedataelement and return the appropriate annotation from there
		IAnnotation annot = element.getElementAnnotation();
		if (annot != null && annot.getAttribute(attr) != null && annot.getAttribute(attr).length > 0 && !annot.getAttribute(attr)[0].equalsIgnoreCase("NA")) {
			return (annot.getAttributeObj(attr));
		}

		//If no annotation object exists in the slidedataelement, or that 
		//annotation object doesn't contain the attribute specified, 
		//check the other annotation types
		String[] allFields = getFieldNames();
		String[] values = new String[] { "NA" };
		for (int i = 0; i < allFields.length; i++) {
			if (allFields[i].equals(attr)) {
				//if (!element.getFieldAt(i).equals("")) {
				//System.out.println("key:"+attr);
				if (element!=null && element.getFieldAt(i).trim().length()!=0 && element.getFieldAt(i).length()!=0 && element.getFieldAt(i)!=null) {
					values = new String[] { element.getFieldAt(i) };
					//System.out.println("values:"+values);
				} else
					values = new String[] { "NA" };
				
			}
		}
		return new AnnoAttributeObj(attr, values);
		
    }

    /**
     * Returns a microarray size.
     */
    public int getSize() {
        return size();
    }

    /**
     * Sets a microarray non-zero attribute.
     */
    public void setNonZero(boolean state) {
        this.isNonZero = state;
    }

    /**
     * Returns a spot meta row.
     */
    public int getRow(int spot) {
        return getSlideDataElement(spot).getRow(SlideDataElement.BASE);
    }

    /**
     * Returns a spot meta column.
     */
    public int getColumn(int spot) {
        return getSlideDataElement(spot).getColumn(SlideDataElement.BASE);
    }

    /**
     * Returns the non-zero attribute.
     */
    public boolean isNonZero() {return isNonZero;}

    /**
     * Returns number of a microarray meta rows.
     */
    public int getRows() {return this.rows;}

    /**
     * Returns number of a microarray meta column.
     */
    public int getColumns() {return this.columns;}

    /**
     * Sets a microarray name.
     */
    public void setSlideDataName(String slideDataName) {
        this.slideDataName = slideDataName;
        
           
        if(this.isSampleAnnotationLoaded){
        	if(!getSampleAnnotation().getSampleAnnoHash().containsKey(IData.DEFAULT_SAMPLE_ANNOTATION_KEY))
        		getSampleAnnotation().setAnnotation(IData.DEFAULT_SAMPLE_ANNOTATION_KEY, this.slideDataName);
        }else{
        	String key = IData.DEFAULT_SAMPLE_ANNOTATION_KEY;
        	sampleLabelKey = key;        
        	sampleLabelKeys.addElement(key);
        	sampleLabels.put(key, slideDataName);
        }
        
    }


    /**
     *  Sets the slide label keys and hash table
     */
    public void setSlideDataLabels(Vector keys, Hashtable namesHash) {
        this.sampleLabelKeys = keys;
        this.sampleLabels = namesHash;
    }


    /**
     *  sets boolean to indicate to abbr file and data name
     */
    public void toggleNameLength(){
        this.abbrName = (!this.abbrName);
    }


    /**
     * Returns the name of a microarray.
     */
    public String getSlideDataName() {
       
    	String name = "";
    	if(this.isSampleAnnotationLoaded)	
    		name=getSampleAnnotation().getAnnotation(this.sampleLabelKey);
    	else	
    		name=(String)this.sampleLabels.get(this.sampleLabelKey);

        if(name == null)
            return " ";

        if(!this.abbrName)
            return name;
        else{
            if(name.length() < 26)
                return name;
            return name.substring(0, 25)+"...";
        }
    	
    	
    	
    }

    public String getFullSlideDataName() {
    	String name=""; 
    	if(this.isSampleAnnotationLoaded){
    		name=getSampleAnnotation().getAnnotation(this.sampleLabelKey);
       	}else  
       		name = (String)this.sampleLabels.get(this.sampleLabelKey);
       		
    	
    	if(name == null)
             return " ";
         else
             return name;
    	
    }

    /**
     * Sets a microarray file name.
     */
    public void setSlideFileName(String slideFileName) {this.slideFileName = slideFileName;}

    /**
     * Returns a microarray file name.
     */
    public String getSlideFileName() {
        if(!this.abbrName)
            return this.slideFileName;
        else{
            if(this.slideFileName.length() < 26)
                return this.slideFileName;
            return this.slideFileName.substring(0, 25)+"...";
        }
    }

    /**
     * Sets a microarray normalization state.
     */
    public void setNormalizedState(int normalizedState) {this.normalizedState = normalizedState;}

    /**
     * Returns a microarray normalization state.
     */
    public int getNormalizedState() {return this.normalizedState;}

    /**
     * Sets a microarray sort state.
     */
    public void setSortState(int sortState) {this.sortState = sortState;}

    /**
     * Returns a microarray sort state.
     */
    public int getSortState() {return this.sortState;}

    /**
     * Returns description of a microarray normalization state.
     */
    public String getNormalizationString() {return normalizationString(normalizedState);}

    //Change to use class constants
    public static String normalizationString(int normalization) {
        String normalizationString;
        switch (normalization) {
            case 0: normalizationString = "No Normalization"; break;
            case 1: normalizationString = "Total Intensity"; break;
            case 2: normalizationString = "Least Squares"; break;
            case 3: normalizationString = "Linear Regression"; break;
            case 4: normalizationString = "Ratio Statistics"; break;
            case 5: normalizationString = "Ratio Statistics"; break;
            case 6: normalizationString = "Iterative Log"; break;
            case 7: normalizationString = "Lowess"; break;

            case 101: normalizationString = "Total Intensity (list)"; break;
            case 102: normalizationString = "Least Squares (list)"; break;
            case 103: normalizationString = "Linear Regression (list)"; break;
            case 104: normalizationString = "Ratio Statistics - 95% CI (list)"; break;
            case 105: normalizationString = "Ratio Statistics - 99% CI (list)"; break;
            case 106: normalizationString = "Iterative Log (list)"; break;
            case 107: normalizationString = "Lowess (list)"; break;
            default: normalizationString = "No Normalization"; break;
        }

        return normalizationString;
    }

    /**
     * Returns a microarray max CY3 value.
     */
    public float getMaxCY3() {
        return getMaxIntensity(ISlideDataElement.CY3);
    }

    /**
     * Returns a microarray max CY5 value.
     */
    public float getMaxCY5() {
        return getMaxIntensity(ISlideDataElement.CY5);
    }

    // Replaces getMaxCy3, getMaxCy5
    public float getMaxIntensity(int intensityType) {
        float intensity, maxIntensity = 0;
        for (int i = 0; i < size(); i++) {
            if(this.normalizedState == ISlideData.NO_NORMALIZATION)
                intensity = getSlideDataElement(i).getTrueIntensity(intensityType);
            else
                intensity = getSlideDataElement(i).getIntensity(intensityType);
            maxIntensity = Math.max(maxIntensity, intensity);
        }
        return maxIntensity;
    }

    /**
     * Returns max intencity of specified type.
     */
    public float getMinIntensity(int intensityType) {return getMinIntensity(intensityType, true);}

    /**
     * Returns min intencity of specified type.
     */
    public float getMinIntensity(int intensityType, boolean acceptZeros) {
        float intensity, minIntensity = Float.MAX_VALUE;

        for (int i = 0; i < size(); i++) {
            if(this.normalizedState == ISlideData.NO_NORMALIZATION)
                intensity = getSlideDataElement(i).getTrueIntensity(intensityType);
            else
                intensity = getSlideDataElement(i).getIntensity(intensityType);
            if (! acceptZeros) {
                if ((intensity < minIntensity) && (intensity != 0)) minIntensity = intensity;
            } else {
                if (intensity < minIntensity) minIntensity = intensity;
            }
        }
        return minIntensity;
    }

    /**
     * Returns a microarray max ratio value.
     */
    public float getMaxRatio() {
        return getMaxRatio(IData.LINEAR);
    }

    /**
     * Returns a microarray min ratio value.
     */
    public float getMinRatio() {
        return getMinRatio(IData.LINEAR);
    }

    /**
     * Returns a microarray max ratio value, with specified log state.
     */
    public float getMaxRatio(int logState) {
        return(float)getMaxRatio(ISlideDataElement.CY5, ISlideDataElement.CY3, logState);
    }

    /**
     * Returns a microarray min ratio value, with specified log state.
     */
    public float getMinRatio(int logState) {
        return(float)getMinRatio(ISlideDataElement.CY5, ISlideDataElement.CY3, logState);
    }

    /**
     * Returns a microarray max ratio value of specified intensities.
     */
    public float getMaxRatio(int intensityIndex1, int intensityIndex2, int logState) {
        float ratio, maxRatio = Float.MIN_VALUE;

        for (int i = 0; i < size(); i++) {
            ratio = getSlideDataElement(i).getRatio(intensityIndex1, intensityIndex2, logState);
            if (ratio > maxRatio)
                maxRatio = ratio;
        }
        return maxRatio;
    }

    /**
     * Returns a microarray min ratio value of specified intensities.
     */
    public float getMinRatio(int intensityIndex1, int intensityIndex2, int logState) {
        float ratio, minRatio = Float.MAX_VALUE;
        for (int i = 0; i < size(); i++) {
            ratio = getSlideDataElement(i).getRatio(intensityIndex1, intensityIndex2, logState);
            if (ratio < minRatio) minRatio = ratio;
        }
        return minRatio;
    }

    /**
     * Returns a microarray max product value of specified intensities.
     */
    public float getMaxProduct(int intensityIndex1, int intensityIndex2) {
        float product = 0, maxProduct = 0;
        ISlideDataElement sde;

        for (int i = 0; i < size(); i++) {
            sde = getSlideDataElement(i);
            product = sde.getIntensity(intensityIndex1) * sde.getIntensity(intensityIndex2);
            if (product > maxProduct)
                maxProduct = product;
        }
        return maxProduct;
    }

    /**
     * Returns a microarray min product value of specified intensities.
     */
    public float getMinProduct(int intensityIndex1, int intensityIndex2, boolean acceptZeros) {
        return getMinProduct(intensityIndex1, intensityIndex2, acceptZeros, 0);
    }

    /**
     * Returns a microarray min product value of specified intensities and lower cutoffs.
     */
    public float getMinProduct(int intensityIndex1, int intensityIndex2, boolean acceptZeros, int lowCutoff) {
        float product = 0, minProduct = Float.MAX_VALUE;
        ISlideDataElement sde;

        for (int i = 0; i < size(); i++) {
            sde = getSlideDataElement(i);
            product = sde.getIntensity(intensityIndex1) * sde.getIntensity(intensityIndex2);
            if (product >= lowCutoff) {
                if (!acceptZeros) {
                    if ((product < minProduct) && (product != 0)) minProduct = product;
                } else {
                    if (product < minProduct) minProduct = product;
                }
            }
        }
        return minProduct;
    }

    /**
     * Returns intensities sum of specified type.
     */
    public long getSumIntensity(int intensityType) {
        long totalIntensity = 0;

        for (int i = 0; i < size(); i++) {
            totalIntensity += getSlideDataElement(i).getIntensity(intensityType);
        }
        return totalIntensity;
    }

    /**
     * Returns non-zero intensities sum of specified type.
     */
    public long getSumNonZeroIntensity(int intensityType) {
        long totalIntensity = 0;
        ISlideDataElement sde;

        for (int i = 0; i < size(); i++) {
            try {
                sde = getSlideDataElement(i);
                if (sde.hasNoZeros()) totalIntensity += sde.getIntensity(intensityType);
            } catch (NullPointerException npe) {
                ;
            }
        }
        return totalIntensity;
    }

    /**
     * Calculates a linear equation.
     */
    public LinearEquation getRegressionEquation(boolean useTrueValues) {
        LinearEquation linearEquation;
        ISlideDataElement sde;
        double x = 0, y = 0;
        double sum = 0, sumX = 0, sumY = 0;
        double sumX2 = 0, sumY2 = 0, sumXY = 0;
        double weight = 1; //Change when set dynamically
        double delta = 0, a = 0, b = 0;
        double variance = 1; //Length of potence
        double regressionCoefficient = 0;
        double sigmaA = 0, sigmaB = 0;

        for (int i = 0; i < size(); i++) {
            sde = getSlideDataElement(i);
            if (sde != null) {
                if (useTrueValues == true) {
                    x = (double) sde.getTrueIntensity(ISlideDataElement.CY3);
                    y = (double) sde.getTrueIntensity(ISlideDataElement.CY5);
                } else {
                    x = (double) sde.getIntensity(ISlideDataElement.CY3);
                    y = (double) sde.getIntensity(ISlideDataElement.CY5);
                }

                if (x != 0 && y != 0) {
                    sum += weight;
                    sumX += (weight * x);
                    sumY += (weight * y);
                    sumX2 += (weight * x * x);
                    sumY2 += (weight * y * y);
                    sumXY += (weight * x * y);
                }
            }
        }

        delta = (sum * sumX2) - (sumX * sumX);
        a = ((sumX2 * sumY) - (sumX * sumXY)) / delta;
        b = ((sumXY * sum) - (sumX * sumY)) / delta;
        sigmaA = Math.sqrt(variance * sumX2 / delta);
        sigmaB = Math.sqrt(variance * sum / delta);
        regressionCoefficient = ((sum * sumXY - sumX * sumY) / Math.sqrt(delta * (sum * sumY2) - (sumY * sumY)));

        linearEquation = new LinearEquation(b, a, regressionCoefficient);
        return linearEquation;
    }


    /**********************************************************************
     * Data Normalization Code.  04-2003 MeV will support only Total Intensity
     * Iterative Linear Regression, Ratio Statistics, and Iterative Log Mean Centering.
     * Support classes and normalization algorithms have been incorporated from
     * TIGR-MIDAS 2.16 (Author: Wei Liang, The Institute for Genomic Research)
     * which remains the primary tool for data normalization
     *
     * ColumnWorker is the main data structure used by the algorithms
     *
     * A typical normalization proceeds as follows:
     * 1.) "True Intensities" are accumulated into double arrays and a ColumnWorker is
     *      constructed.
     * 2.) The ColumnWorker is passed via constructor into a normalization algorithm
     *     which performs the required manipulations. (parameters are also in the constructor)
     * 3.) Data is retrieved from the resulting ColumnWorker and the data is placed
     *     into the appropriate SlideDataElements (or arrays) as "Current Intensities"
     *     (normalized).
     *
     * Conventions for normalization:
     *
     *  "normalizedState" a class variable is used to indicate which intensities and
     *  ratio's to deliver.  ISlideData.NO_NORMALIZATION forces the return of "True"
     *  intensities, meaning unaltered,  while any other state indicates that "Current"
     *  intensities (altered) should be returned.
     *
     * Handling zeros:
     *
     * A zero in one or both channels would greatly impact the normalization of points
     * containing two good intensities.  For this reason the following convention for handling
     * one or two zero intensities has been adopted.  If one or both intensities are zero this
     * data is not passed on to the ColumnWorker for normalization and following the normalization
     * the "current" intensities are set to equal the "true", original, intensities.
     *
     * By this convention the points are preserved AND the data that has been normalized
     * matches MIDAS output.
     *
     ************************************************************************************/

    /**
     * Normalize a microarray data.
     */
    public void applyNormalization(int normalizationMode, Properties props) {
        switch (normalizationMode) {
            case SlideData.NO_NORMALIZATION: applyNoNormalization(); break;
            case SlideData.TOTAL_INTENSITY: applyTotalIntensity(); break;
            case SlideData.LEAST_SQUARES: applyLeastSquares(); break;
            case SlideData.LINEAR_REGRESSION: applyLinearRegression(props); break;
            case SlideData.RATIO_STATISTICS_95: applyRatioStatistics(props); break;
            case SlideData.RATIO_STATISTICS_99: applyRatioStatistics(props); break;
            case SlideData.ITERATIVE_LOG: applyIterativeLog(props); break;
            case SlideData.LOWESS: applyLowess(10); break;
        }
    }

    /**
     * Normalize a microarray data.
     */
    public void applyNormalizationList(int normalizationMode) {
        switch (normalizationMode) {
            //Don't forget to change over to the list versions of the functions
            case SlideData.TOTAL_INTENSITY_LIST: applyTotalIntensity(); break;
            case SlideData.LEAST_SQUARES_LIST: applyLeastSquares(); break;
            case SlideData.LINEAR_REGRESSION_LIST: applyLinearRegression(new Properties()); break;
            case SlideData.RATIO_STATISTICS_95_LIST: applyRatioStatistics(new Properties()); break;
            case SlideData.RATIO_STATISTICS_99_LIST: applyRatioStatistics(new Properties()); break;
            case SlideData.ITERATIVE_LOG_LIST: applyIterativeLog(new Properties()); break;
            case SlideData.LOWESS_LIST: applyLowess(10); break;
        }
    }


    /**
     * Restore an original microarray data.
     */
    public void applyNoNormalization() {
        ISlideDataElement sde;
        if (normalizedState != SlideData.NO_NORMALIZATION) {
            for (int i = 0; i < size(); i++) {
                sde = getSlideDataElement(i);
                sde.setIntensity(ISlideDataElement.CY3, sde.getTrueIntensity(ISlideDataElement.CY3));
                sde.setIntensity(ISlideDataElement.CY5, sde.getTrueIntensity(ISlideDataElement.CY5));
            }
            normalizedState = SlideData.NO_NORMALIZATION;
        }
    }

    /**
     * Applies total intensity normalization.
     */
    public void applyTotalIntensity() {
        ISlideDataElement sde = getSlideDataElement(10);
        boolean [] goodValues = new boolean[this.getSize()];
        ColumnWorker cw = constructColumnWorker(goodValues);
        cw = ((new TotInt(cw, "Cy3", false)).getFileTotIntColumnWorker());
        setNormalizedIntensities(cw, goodValues);
        normalizedState = ISlideData.TOTAL_INTENSITY;
    }

    /**
     * Applies linear regression normalization.
     */
    public void applyLinearRegression(Properties properties) {
        boolean [] goodValues = new boolean[this.getSize()];
        ColumnWorker cw = constructColumnWorker(goodValues);
        try{
            String  mode = (String)properties.get("mode");
            float sd = Float.parseFloat((String)properties.get("standard-deviation"));
            cw = ((new IterativeLinReg(cw, sd, mode, "Cy3")).getIterLinRegColumnWorker());
            setNormalizedIntensities(cw, goodValues);
            normalizedState = ISlideData.LINEAR_REGRESSION;
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(new javax.swing.JFrame(), "Error Performing Normalization: Data Unchanged", "Normalization Error : Aborted", javax.swing.JOptionPane.WARNING_MESSAGE);
            normalizedState = ISlideData.NO_NORMALIZATION;
        }
    }

    /**
     * Applies ratio statistics normalization.
     */
    public void applyRatioStatistics(Properties properties) {
        boolean [] goodValues = new boolean[this.getSize()];
        ColumnWorker cw = constructColumnWorker(goodValues);
        try{
            int confInt = Integer.parseInt((String)properties.get("confidence-interval"));
            cw = ((new RatioStats(cw, true, confInt, "Cy3")).getRatioStatsColumnWorker());
            setNormalizedIntensities(cw, goodValues);
            normalizedState = ISlideData.LINEAR_REGRESSION;
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(new javax.swing.JFrame(), "Error Performing Normalization: Data Unchanged", "Normalization Error : Aborted", javax.swing.JOptionPane.WARNING_MESSAGE);
            normalizedState = ISlideData.NO_NORMALIZATION;
            e.printStackTrace();
        }
    }

    /**
     * Applies iterative log normalization.
     */
    public void applyIterativeLog(Properties properties) {
        boolean [] goodValues = new boolean[this.getSize()];
        ColumnWorker cw = constructColumnWorker(goodValues);
        try{
            float sd = Float.parseFloat((String)properties.get("standard-deviation"));
            //System.out.println("iter log sd = "+sd);
            cw = ((new IterativeLogMean(cw, sd, "Cy3")).getIterLogMeanColumnWorker());
            setNormalizedIntensities(cw, goodValues);
            normalizedState = ISlideData.ITERATIVE_LOG;
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(new javax.swing.JFrame(), "Error Performing Normalization: Data Unchanged", "Normalization Error : Aborted", javax.swing.JOptionPane.WARNING_MESSAGE);
            normalizedState = ISlideData.NO_NORMALIZATION;
        }
    }

    /**
     * Creates a ColumnWorker object for normalization
     */
    private ColumnWorker constructColumnWorker(boolean [] goodValues){
        int n = this.getSize();
        int size = n;
        float CY3;
        float CY5;
        float [] cy3 = new float[n];
        float [] cy5 = new float[n];
        String [] metaCombo = new String[n];
        int metaRow, metaColumn;
        float [] goodCy3;
        float [] goodCy5;

        n = 0;
        for(int i = 0; i < size; i++){
            metaRow = this.getSlideDataElement(i).getRow(ISlideDataElement.META);
            metaColumn = this.getSlideDataElement(i).getColumn(ISlideDataElement.META);

            CY3 = (float)this.getSlideDataElement(i).getTrueIntensity(ISlideDataElement.CY3);
            CY5 = (float)this.getSlideDataElement(i).getTrueIntensity(ISlideDataElement.CY5);

            //if Stanford file is normalized then we need to generate cy3 and cy5
            if (this.dataType == IData.DATA_TYPE_RATIO_ONLY) {
                CY3 = 100000;
                CY5 = (float)(100000.0*Math.pow(2.0d, CY5));
                // if CY5 overflows, then set to zero and force no evaluation
                if(CY5 == Float.POSITIVE_INFINITY || CY5 == Float.NEGATIVE_INFINITY)
                    CY3 = CY5 = 0;
            }

            if(CY3 != 0 && CY5 != 0){
                goodValues[i] = true;
                cy3[n] = CY3;
                cy5[n] = CY5;
                metaCombo[n] = Integer.toString(metaRow)+"_"+Integer.toString(metaColumn);
                n++;
            }
        }
        goodCy3 = new float[n];
        goodCy5 = new float[n];
        System.arraycopy(cy3, 0, goodCy3, 0, n);
        System.arraycopy(cy5, 0, goodCy5, 0, n);
        System.arraycopy(metaCombo, 0, metaCombo, 0, n);

        // ColumnWorker cw = new ColumnWorker(goodCy3.length);
        //cw.setColOneArray(goodCy3);
        //cw.setColTwoArray(goodCy5);

        // return cw;
        return new ColumnWorker(goodCy3, goodCy5, metaCombo);
    }

    /**
     *  Extracts data from a ColumnWorker into ISlideData current (normalized) intensities
     */
    private void setNormalizedIntensities(ColumnWorker cw, boolean [] goodIntensity){
        float [] cy3 = cw.getColumnOneArray();
        float [] cy5 = cw.getColumnTwoArray();
        ISlideDataElement sde;
        int goodIndex= 0;
        for(int i = 0 ; i < goodIntensity.length; i++){
            sde = this.getSlideDataElement(i);
            if(goodIntensity[i]){
                if(this.dataType == IData.DATA_TYPE_RATIO_ONLY) {
                    sde.setIntensity(ISlideDataElement.CY3, (float)1.0);
                    sde.setIntensity(ISlideDataElement.CY5, (float)(Math.log(cy5[goodIndex]/cy3[goodIndex])/Math.log(2.0)));
                } else {
                    sde.setIntensity(ISlideDataElement.CY3, (float)cy3[goodIndex]);
                    sde.setIntensity(ISlideDataElement.CY5, (float)cy5[goodIndex]);
                }
                goodIndex++;
            }
            else{
                sde.setIntensity(ISlideDataElement.CY3, sde.getTrueIntensity(ISlideDataElement.CY3));
                sde.setIntensity(ISlideDataElement.CY5, sde.getTrueIntensity(ISlideDataElement.CY5));
            }
        }
    }

    /********************************************************************************
     *  End supported normalization code.
     */


    /**
     * Applies lowess normalization.
     */
    public void applyLowess(int bins) {
        //      applyIterativeLog();

        Vector binVector = new Vector();
        Vector bin;
        ISlideDataElement[] sdes = new ISlideDataElement[size()];
        float[] logRatios = new float[size()];
        float[] logProduct = new float[size()];
        int[] descendingRank = new int[size()];
        int binSize = size() / bins + 1;

        for (int i = 0; i < bins; i++) binVector.addElement(new Vector());

        for (int i = 0; i < size(); i++) {
            sdes[i] = getSlideDataElement(i);
            logRatios[i] = (float)Xcon.log2(sdes[i].getRatio(ISlideDataElement.CY5, ISlideDataElement.CY3, AC.LINEAR));
            logProduct[i] = (float)Xcon.log10(sdes[i].getIntensity(ISlideDataElement.CY3) * sdes[i].getIntensity(ISlideDataElement.CY5));
        }

        double largest = 0;
        int highestRank = 0;
        for (int i = 0; i < size(); i++) {
            for (int j = 0; j < size(); j++) {
                COMP: if (largest < logProduct[j]) {
                    for (int k = 0; k < i; k++) {
                        if (j == descendingRank[k]) break COMP;
                    }
                    largest = logProduct[j];
                    highestRank = j;
                }
            }

            descendingRank[i] = highestRank;
        }

        for (int i = 0; i < size(); i++) System.out.println("Rank: " + i + ", Element: " + descendingRank[i]);

        int binC = 0;
        int binP = 0;
        for (int i = 0; i < size(); i++, binC++) {
            ((Vector) binVector.elementAt(binP)).addElement(sdes[descendingRank[i]]);

            if (binC == binSize) {
                binC = 0;
                binP++;
            }
        }

        System.out.println(binVector.size() + " bins created");
        for (int i = 0; i < binVector.size(); i++) {
            System.out.println("Bin " + i + " has " + ((Vector) binVector.elementAt(i)).size() + " elements");
        }

        //Adjust mean(log2(r/g)) for each bin (like iterative log)
        ISlideDataElement sde;
        if (true) { //Use an adaptation of the iterative log algorithm
            for (int m = 0; m < binVector.size(); m++) {
                Vector targetBin = ((Vector) binVector.elementAt(m));
                long greensum = 0, redsum = 0, usegreensum = 0, useredsum = 0;

                for (int i = 0; i < targetBin.size(); i++) {
                    sde = ((ISlideDataElement) targetBin.elementAt(i));
                    greensum += sde.getIntensity(ISlideDataElement.CY5);
                    redsum += sde.getIntensity(ISlideDataElement.CY3);
                    if (sde.hasNoZeros()) {
                        usegreensum += sde.getIntensity(ISlideDataElement.CY5);
                        useredsum += sde.getIntensity(ISlideDataElement.CY3);
                    }
                }

                double norat = (double) redsum / (double) greensum;
                double usenorat = (double) useredsum / (double) usegreensum;
                double yzsum = 0, yz = 0, yzave = 0;
                int yzmun = 0;
                double lnx, lny, uselograt;
                double[] ratio2 = new double[size()];

                System.out.println(m + " - All: " + redsum + "\t" + greensum + "\t" + norat);
                System.out.println(m + " - Use: " + useredsum + "\t" + usegreensum + "\t" + usenorat);

                for (int i = 0; i < targetBin.size(); i++) {
                    sde = ((ISlideDataElement) targetBin.elementAt(i));
                    sde.setIntensity(ISlideDataElement.CY5, (long) ((double) sde.getIntensity(ISlideDataElement.CY5) * usenorat));
                    yz = Xcon.log2(sde.getRatio(ISlideDataElement.CY3, ISlideDataElement.CY5, AC.LINEAR) / usenorat);

                    if (sde.hasNoZeros()) {
                        yzsum += yz;
                        yzmun++;
                    }

                    lnx = Math.log(1);
                    lny = Math.log(1);
                }

                yzave = yzsum / yzmun;
                System.out.println(m + " - Mean log ratio: " + yzave);
                yzsum = 0;
                yzmun = 0;

                uselograt = Math.pow(Math.E, yzave);
                System.out.println(m + " - Scale: " + uselograt);

                for (int i = 0; i < targetBin.size(); i++) {
                    sde = ((ISlideDataElement) targetBin.elementAt(i));
                    sde.setIntensity(ISlideDataElement.CY5, (long) ((double) sde.getIntensity(ISlideDataElement.CY5) * uselograt));
                    yz = Xcon.log2(sde.getRatio(ISlideDataElement.CY3, ISlideDataElement.CY5, AC.LINEAR) / uselograt);
                }

                for (int i = 0; i < targetBin.size(); i++) {
                    sde = ((ISlideDataElement) targetBin.elementAt(i));
                    if (sde.hasNoZeros()) ratio2[i] = sde.getRatio(ISlideDataElement.CY3, ISlideDataElement.CY5, AC.LINEAR) / usenorat / uselograt;
                    else ratio2[i] = sde.getRatio(ISlideDataElement.CY3, ISlideDataElement.CY5, AC.LINEAR);
                }

                double xsum = 0;
                int xnum = 0;
                double x = 0;
                double xave = 0;
                double newlognor = 0;

                for (int it = 0; it <= 10; it++) {
                    //for (int i = 0; i < size(); i++)
                    for (int i = 0; i < targetBin.size(); i++) {
                        sde = ((ISlideDataElement) targetBin.elementAt(i));
                        if (sde.hasNoZeros()) {
                            x = Xcon.log2(ratio2[i]);
                            if ((x <= 1) && (x >= -1)) {
                                xsum += x;
                                xnum++;
                            }
                        }
                    }
                    xave = xsum / xnum;
                    //Older style, using natural logs? Conserve?
                    //newlognor = Math.pow(Math.E, xave);
                    newlognor = Math.pow(Math.E, xave);
                    System.out.println(m + " - Iteration " + it + "\tMean Log Ratio: " + xave + "\t" + newlognor);
                    for (int i = 0; i < targetBin.size(); i++) {
                        // for (int i = 0; i < size(); i++) {
                        sde = ((ISlideDataElement) targetBin.elementAt(i));
                        if (sde.hasNoZeros()) {
                            ratio2[i] = ratio2[i] / newlognor;
                        }
                        sde.setIntensity(ISlideDataElement.CY5, (long) ((double) sde.getIntensity(ISlideDataElement.CY5) * newlognor));
                    }
                }
            }
        }

        normalizedState = SlideData.LOWESS;
    }

    /**
     * Applies least squares normalization.
     */
    public void applyLeastSquares() {
        ISlideDataElement sde;
        LinearEquation linearEquation = getRegressionEquation(true);

        if (normalizedState != SlideData.LEAST_SQUARES) {
            for (int i = 0; i < size(); i++) {
                sde = getSlideDataElement(i);
                if (sde != null) {
                    sde.setIntensity(ISlideDataElement.CY5, applyLeastSquares(sde.getTrueIntensity(ISlideDataElement.CY5), linearEquation));
                }
            }
            normalizedState = SlideData.LEAST_SQUARES;
        }
    }

    /**
     * Applies least squares normalization.
     */
    public float applyLeastSquares(float value, LinearEquation linearEquation) {
        if (value > 0) {
            return(float)((value-linearEquation.getYIntercept())*(1 / linearEquation.getSlope()));
        } else
            return 0f;
    }



    public void output() {
        String contents = "";
        try {
            contents = "\n\n\n***SlideData***\n\n";
            for (int i = 0; i < size(); i++) {
                contents += getSlideDataElement(i).toString() + "\n";
            }
        } catch (Exception e) {
            ;
        }
        System.out.println(contents);
    }

    /**
     * Returns the slide name keys.
     */
    public Vector getSlideDataKeys() {
       	//Gets the keys from the SampleAnnotation model object and returns as a Vector.
    	if(this.isSampleAnnotationLoaded)
         return new Vector(((SampleAnnotation)getSampleAnnotation()).getAnnotationKeys());
    	else  
          return this.sampleLabelKeys;
    }

    /**
     * Returns the slide name keys and pairs
     */
    public Hashtable getSlideDataLabels() {
       
    	if(this.isSampleAnnotationLoaded)
    	 return (((SampleAnnotation)getSampleAnnotation()).getSampleAnnoHash());
    	else
    		return this.sampleLabels;
    	
    }
    
    
    
    
    

    /** Sets the current label index.
     */
    public void setDataLabelKey(String key) {
           this.sampleLabelKey = key;
    }

    /** Adds a new key and label value
     */
    public void addNewSampleLabel(String label, String value) {
    	
    	System.out.println("SlideData:addNewSampleLabel");
    	if (!this.isSampleAnnotationLoaded) {
    		
			if (!sampleLabelKeys.contains(label))
				this.sampleLabelKeys.addElement(label);
			this.sampleLabels.put(label, value);

		} else {

			// Add the Key and value to the new Sample Annotation model

			/*if (getSampleAnnotation() != null){
				
				getSampleAnnotation().getSampleAnnoHash().clear();//Added to take care of insatnce where values are delted from the table
				getSampleAnnotation().setAnnotation(label, value);
			}
			else {*/
				SampleAnnotation ann = new SampleAnnotation();
				this.isSampleAnnotationLoaded=true;
				
				 ann.setAnnotation(IData.DEFAULT_SAMPLE_ANNOTATION_KEY, getSampleAnnotation().getAnnotation(IData.DEFAULT_SAMPLE_ANNOTATION_KEY));
//				 System.out.println("Key:"+label);
//	             System.out.println("Data:"+value);
				ann.setAnnotation(label, value);
				this.setSampleAnnotation(ann);
			//}
		}
    


    }

    /** Returns the detection status for the gene specified, Affy support
     */
    public String getDetection(int row) {
        return this.getSlideDataElement(row).getDetection();
    }
    public Object clone(){
    	return this.clone();
    }
    //wwang add for affy p-value filter
    public float getPvalue(int row) {
        return this.getSlideDataElement(row).getPvalue();
    }
    public int getGenePixFlags(int row) {
        return this.getSlideDataElement(row).getGenePixFlags();
    }


    /**
     * CGH IFeatureData implemetations
     * Raktim Oct 31, 2005
     */

    /**
     * Raktim, CGH
     * Setter for property flankingRegions.
     * @param flankingRegions New value of property flankingRegions.
     */
    public void setFlankingRegions(java.util.Vector[] flankingRegions) {
    	//System.out.println("SlideData setFlankingRegion()");
    	if (flankingRegions == null) System.out.println("NULL flankingRegions in SlideData.setFlankingRegion()");
        this.flankingRegions = flankingRegions;
    }

    /**
     * Raktim, CGH
     */
    public int getNumFlankingRegions(int chromosomeIndex){
    	if (flankingRegions == null) System.out.println("NULL flankingRegions in SlideData");
        return flankingRegions[chromosomeIndex].size();
    }

    /**
     * Raktim, CGH
     * Getter for property flankingRegions.
     * @return Value of property flankingRegions.
     */
    public java.util.Vector[] getFlankingRegions() {
        return this.flankingRegions;
    }

    /**
     * Raktim, CGH
     * Remember to fix this later
     */
    public boolean isMissingData(int cloneIndex){

        if(Float.isNaN(getCY3(cloneIndex)) || Float.isNaN(getCY5(cloneIndex)))
        	return true;
        return false;
    	/* Old Abramson Code.
    	 * Remember to Get Back to this Later
        Iterator it = cy3Slides.iterator();
        boolean cy3Missing = true;
        boolean cy5Missing = true;
        while(it.hasNext()){
            CGHSlideData slideData = (CGHSlideData)it.next();
            if(! Float.isNaN(slideData.getRatio(cloneIndex))){
                cy3Missing = false;
            }
        }
        if(cy3Missing){
            return true;
        }

        it = cy5Slides.iterator();
        while(it.hasNext()){
            CGHSlideData slideData = (CGHSlideData)it.next();
            if(! Float.isNaN(slideData.getRatio(cloneIndex))){
                cy5Missing = false;
            }
        }
        if(cy5Missing){
            return true;
        }
        */
    }


	public boolean isSampleAnnotationLoaded() {
		return this.isSampleAnnotationLoaded;
	}


	public void setSampleAnnotationLoaded(boolean isAnnLoaded) {
		this.isSampleAnnotationLoaded=isAnnLoaded;
		
	}
 
}
