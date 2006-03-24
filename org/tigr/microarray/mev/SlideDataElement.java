/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: SlideDataElement.java,v $
 * $Revision: 1.9 $
 * $Date: 2006-03-24 15:49:45 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev;

import org.tigr.microarray.mev.cluster.gui.IData;


public class SlideDataElement extends ArrayElement implements ISlideDataElement {
    
    protected String UID;
    protected int[] rows;
    protected int[] columns;
    protected float[] currentIntensity, trueIntensity;
    protected String[] extraFields;
    protected boolean isNull = false;
    protected boolean isNonZero = true;
     protected float pvalue=0;
    protected int flags=0;
    private SlideDataElement(int[] rows, int[] cols, String[] extraFields, String uid){
    	this.rows = rows;
    	this.columns = cols;
    	this.extraFields = extraFields;
    	this.UID = uid;
    	this.currentIntensity = new float[2];
    	this.trueIntensity = new float[2];
    }
    public SlideDataElement(int[] rows, int[] cols, String[] extraFields, String uid, boolean isNull, boolean isNonZero){
    	this(rows, cols, extraFields, uid);
    	this.isNull = isNull;
    	this.isNonZero = isNonZero;
    }
    public  SlideDataElement(int[] rows, int[] columns, float[] currentIntensities, float[] trueIntensities, String[] values) {
        this.rows = copyArray(rows);
        this.columns = copyArray(columns);
        this.currentIntensity = copyArray(currentIntensities);
        this.trueIntensity = copyArray(trueIntensities);
        this.extraFields = copyArray(values);    	
    }

    public SlideDataElement() {
    	this.currentIntensity = new float[2];
    	this.trueIntensity = new float[2];
    }
    /**
     * Constructs a <code>SlideDataElement</code> with specified meta rows,
     * meta columns, intensities and descriptions.
     */
    public SlideDataElement(String UID, int[] rows, int[] columns, float[] intensities, String[] values) {
        this.UID = UID;
        this.rows = copyArray(rows);
        this.columns = copyArray(columns);
        this.currentIntensity = copyArray(intensities);
        this.trueIntensity = copyArray(intensities);
        this.extraFields = copyArray(values);
    }
    
    /**
     * Constructs a <code>SlideDataElement</code> with specified meta rows,
     * meta columns, intensities and descriptions.
     */
    public SlideDataElement(int[] rows, int[] columns, float[] intensities, String[] values) {
    	this("", rows, columns, intensities, values);
    }
    
    /**
     * Copy constructor.
     */
    public SlideDataElement(ISlideDataElement sde) {
        this.UID = sde.getUID();
        this.rows = sde.getRows();
        this.columns = sde.getColumns();
        this.currentIntensity = copyArray(sde.getCurrentIntensity());
        this.trueIntensity = copyArray(sde.getTrueIntensity());
        this.extraFields = sde.getExtraFields();
    }

    public void setRows(int[] r){this.rows = copyArray(r);}
    public void setColumns(int[] c){this.columns = copyArray(c);}
    public String[] getValues() {return extraFields;}
    /**
     * Sets the extra fields (annotation)
     */
    public void setExtraFields(String [] values){
        if(values == null) return;
        if(this.extraFields == null)
            this.extraFields = values;
        else{
            String [] newFields = new String[this.extraFields.length+values.length];
            for(int i = 0; i < this.extraFields.length; i++)
                newFields[i] = this.extraFields[i];
            for(int i = 0; i < values.length; i++)
                newFields[i+this.extraFields.length] = values[i];
            this.extraFields = newFields;
        }
    }
    
    /**
     * Creates clone of a string array.
     */
    private String[] copyArray(String[] array) {
        if (array == null) {
            return null;
        }
        String[] result = new String[array.length];
        System.arraycopy(array, 0, result, 0, array.length);
        return result;
    }
    
    /**
     * Creates clone of an int array.
     */
    private int[] copyArray(int[] array) {
        if (array == null) {
            return null;
        }
        int[] result = new int[array.length];
        System.arraycopy(array, 0, result, 0, array.length);
        return result;
    }
    
    /**
     * Creates clone of a float array.
     */
    private float[] copyArray(float[] array) {
        if (array == null) {
            return null;
        }
        float[] result = new float[array.length];
        System.arraycopy(array, 0, result, 0, array.length);
        return result;
    }
    
    /**
     * Returns an array of spot meta rows.
     */
    public int[] getRows() {
        return rows;
    }
    
    /**
     * Returns an array of spot meta column.
     */
    public int[] getColumns() {
        return columns;
    }
    
    /**
     * Returns an array of spot descriptions.
     */
    public String[] getExtraFields() {
    	if(extraFields == null)
    		return new String[0];
        return extraFields;
    }
    
    /**
     * Returns an array of current intensities.
     */
    public float[] getCurrentIntensity() {
        return currentIntensity;
    }
    
    /**
     * Returns an array of true intensities.
     */
    public float[] getTrueIntensity() {
        return trueIntensity;
    }
    
    /**
     * Sets value for specified row type.
     */
    public void setRow(int rowType, int value) {
        switch (rowType) {
            case BASE: rows[0] = value; break;
            case META: rows[1] = value; break;
            case SUB : rows[2] = value; break;
        }
    }
    
    /**
     * Sets value for specified column type.
     */
    public void setColumn(int columnType, int value) {
        switch (columnType) {
            case BASE: columns[0] = value; break;
            case META: columns[1] = value; break;
            case SUB:  columns[2] = value; break;
        }
    }
    
    //Replaces getRow, getMetaRow, getSubRow
    public int getRow(int rowType) {
        int targetRow = -1;
        switch (rowType) {
            case BASE: targetRow = rows[0]; break;
            case META: targetRow = rows[1]; break;
            case SUB: targetRow  = rows[2]; break;
        }
        return targetRow;
    }
    
    /**
     * Returns index of specified column type.
     */
    public int getColumn(int columnType) {
        int targetColumn = -1;
        switch (columnType) {
            case BASE: targetColumn = columns[0]; break;
            case META: targetColumn = columns[1]; break;
            case SUB: targetColumn  = columns[2]; break;
        }
        return targetColumn;
    }
    
    /**
     * Returns a spot location.
     */
    public int getLocation(int positionType, int features) {
        int location = -1;
        switch (positionType) {
            case BASE: location = columns[0] + (rows[0] - 1) * features; break;
            case META: location = columns[1] + (rows[1] - 1) * features; break;
            case SUB : location = columns[2] + (rows[2] - 1) * features; break;
        }
        return location;
    }
    
    /**
     * Returns intensity value by its specified type.
     */
    public float getIntensity(int intensityType) {
        float targetIntensity = -1;
        switch (intensityType) {
            case CY3:
                targetIntensity = currentIntensity[0];
                break;
            case CY5:
                targetIntensity = currentIntensity[1];
                break;
        }
        return (float)targetIntensity;
    }
    
    /**
     * Sets an intensity value.
     */
    public void setIntensity(int intensityType, float value) {
        switch (intensityType) {
            case CY3:{
                currentIntensity[0] = value;
                break;
            }
            case CY5:{
                currentIntensity[1] = value;
                break;
            }
        }
    }
    
    /**
     *  Sets current (altered) intensity
     */
    public void setCurrentIntensity(int intensityType, float value){
        switch (intensityType) {
            case CY3:{
                currentIntensity[0] = value;
                break;
            }
            case CY5:{
                currentIntensity[1] = value;
                break;
            }
        }
    }
    /**
     * Sets true intensity value.
     */
    public void setTrueIntensity(int intensityType, float value) {
        switch (intensityType) {
            case CY3: trueIntensity[0] = value; break;
            case CY5: trueIntensity[1] = value; break;
        }
    }
    
    //Replaces getTrueCy3, getTrueCy5
    public float getTrueIntensity(int intensityType) {
        float targetIntensity = -1;
        switch (intensityType) {
            case CY3: try {targetIntensity = trueIntensity[0]; break;} catch (NullPointerException npe) {targetIntensity = 0; break;}
            case CY5: try {targetIntensity = trueIntensity[1]; break;} catch (NullPointerException npe) {targetIntensity = 0; break;}
            //TODO remove try/catch blocks above.
            //They fix saving/loading problems when using XMLEncoder, but probably screw things up for normal functioning.
        }
        return targetIntensity;
    }
    
    /**
     * Returns a ratio value for specified intensities.
     */
    public float getRatio(int intensityIndex1, int intensityIndex2, int logState) {
        return getRatio(intensityIndex1, intensityIndex2, logState, isNonZero);
    }
    
    public float getRatio(int intensityIndex1, int intensityIndex2, int logState, boolean nonZero) {
        float ratio;
        if (nonZero) {
            if ((getIntensity(intensityIndex1) == 0) && (getIntensity(intensityIndex2) == 0)) {
                return Float.NaN;
            } else if (getIntensity(intensityIndex1) == 0) {
                ratio = 1f / (float)getIntensity(intensityIndex2);
            } else if (getIntensity(intensityIndex2) == 0) {
                ratio = (float)getIntensity(intensityIndex1)/1f;
            } else {
                ratio = (float)getIntensity(intensityIndex1)/(float)getIntensity(intensityIndex2);
            }
        } else {
            if (getIntensity(intensityIndex1) == 0) return Float.NaN;
            if (getIntensity(intensityIndex2) == 0) return Float.NaN;
            ratio = (float)getIntensity(intensityIndex1)/(float)getIntensity(intensityIndex2);
        }
        if (logState == IData.LOG)
            //LOG
            //ratio = (float) Xcon.log2(ratio);
            ratio = (float)(Math.log(ratio)/Math.log(2.0));
        return ratio;
    }
    
    /**
     * Returns a true ratio value for specified intensities.
     */
    public double getTrueRatio(int intensityIndex1, int intensityIndex2, int logState) {
        return getTrueRatio(intensityIndex1, intensityIndex2, logState, isNonZero);
    }
    
    public double getTrueRatio(int intensityIndex1, int intensityIndex2, int logState, boolean nonZero) {
        double ratio;
        if (nonZero) {
            if ((getTrueIntensity(intensityIndex1) == 0) && (getTrueIntensity(intensityIndex2) == 0)) {
                return Float.NaN;
            } else if (getTrueIntensity(intensityIndex1) == 0) {
                ratio = (double) 1.0 / (double) getTrueIntensity(intensityIndex2);
            } else if (getTrueIntensity(intensityIndex2) == 0) {
                ratio = (double) getTrueIntensity(intensityIndex1) / (double) 1.0;
            } else {
                ratio = (double) getTrueIntensity(intensityIndex1) / (double) getTrueIntensity(intensityIndex2);
            }
        } else {
            if (getTrueIntensity(intensityIndex1) == 0) return Float.NaN;
            if (getTrueIntensity(intensityIndex2) == 0) return Float.NaN;
            ratio = (double) getTrueIntensity(intensityIndex1) / (double) getTrueIntensity(intensityIndex2);
        }
        
        if (logState == IData.LOG)
            //LOG
            //ratio = (float) Xcon.log2(ratio);
            ratio = (float)(Math.log(ratio)/Math.log(2.0));
        return ratio;
    }
    
    public float getIntensityMean(int intensityType) {
        return(float) 0.0;
    }
    
    /**
     * Sets the non-zero flag.
     */
    public void setNonZero(boolean state) {this.isNonZero = state;}
    
    //pcahan, to implement ISlideDataElement changes for Affy
    public void setDetection(String value){}
    public String getDetection(){
        return "";
}
    
    //wwang add p-value for affy
    public void setPvalue(float value){
    	pvalue=value;
    	}
    
    public float getPvalue(){
    	return pvalue;
    }
    public void setGenePixFlags(int value){
    	flags=value;
    	}
    
    public int getGenePixFlags(){
    	return flags;
    }
    
    /**
     * Returns the non-zero flag.
     */
    public boolean isNonZero() {return this.isNonZero;}
    
    /**
     * Sets the isNull flag.
     */
    public void setIsNull(boolean state) {this.isNull = state;}
    
    /**
     * Returns the isNull flag.
     */
    public boolean getIsNull() {return this.isNull;}
    
    /**
     * @return true, if spot contains no zero intensities.
     */
    public boolean hasNoZeros() {
        if ((getTrueIntensity(CY3) != 0) && (getTrueIntensity(CY5) != 0)) return true;
        else return false;
    }
    
    public String getAttributeString(int number) {
        String retVal = "";
        int coordinatePairs = TMEV.getCoordinatePairCount() * 2; //Did you see the (* 2)?
        int intensities = TMEV.getIntensityCount();
        int extraFields = this.extraFields.length;
        
        if (number < coordinatePairs) {
            if (number % 2 == 0)
                retVal = String.valueOf(rows[number/2]);
            else
                retVal = String.valueOf(columns[(number+1)/2]);
        } else if (number < coordinatePairs + intensities) {
            retVal = String.valueOf(currentIntensity[number - (coordinatePairs)]);
        } else if (number < coordinatePairs + intensities + extraFields) {
            retVal = getFieldAt(number - (coordinatePairs + intensities));
        } else retVal = "";
        return retVal;
    }
    
    /**
     * Returns a description by specified index. Or empty string if feild index
     * does not exist
     */
    public String getFieldAt(int index) {
        if(extraFields == null || index < 0 || index > (extraFields.length - 1)) {
            return "";
        }
        return extraFields[index];
    }
    
    /**
     * Returns clone on this element.
     */
    public ISlideDataElement copy() {
        return new SlideDataElement(this);
    }
    
    public String toString() {
        return "SDE " + getRow(BASE) + ", " + getColumn(BASE);
    }
    
    /** Sets the UID field
     */
    public void setUID(String uid) {
        this.UID = uid;
    }
    
    /** Returns the UID field
     */
    public String getUID() {
        return this.UID;
    }
    
}
