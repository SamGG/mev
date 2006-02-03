/*
 * CGHViewerDataModel.java
 *
 * Created on June 15, 2003, 1:42 AM
 */

package org.tigr.microarray.mev.cgh.CGHDataModel;

import java.awt.Color;
import java.awt.image.BufferedImage;

import org.tigr.microarray.mev.cluster.gui.ICGHCloneValueMenu;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IFramework;

/**
 *
 * @author  Adam Margolin
 * @author Raktim Sinha
 */

public class CGHViewerDataModel {

    public static final int CLONE_VALUES_DISCRETE_DETERMINATION = 0;
    public static final int CLONE_VALUES_CONTINUOUS = 1;

    public static final int BAD_CLONE = -10;
    public static final int NO_COPY_CHANGE = -11;

    /**
    protected Color COLOR_NULL = Color.darkGray;
    protected Color COLOR_AMP = Color.green;
    protected Color COLOR_AMP_2_COPY = Color.yellow;
    protected Color COLOR_DEL = Color.red;
    protected Color COLOR_DEL_2_COPY = Color.pink;
    protected Color COLOR_DEFAULT = Color.blue;
    protected Color COLOR_ERROR = Color.white;
    */

    /**
     * New Color Defs for CGH
     * Raktim CGH Colors
     * Nov 22, 2005
     */
    protected Color COLOR_NULL = Color.darkGray;
    protected Color COLOR_AMP = Color.red;
    protected Color COLOR_AMP_2_COPY = Color.pink;
    protected Color COLOR_DEL = Color.green;
    protected Color COLOR_DEL_2_COPY = Color.yellow;
    protected Color COLOR_DEFAULT = Color.blue;
    protected Color COLOR_ERROR = Color.white;

    protected BufferedImage negColorImage;
    protected BufferedImage posColorImage;
    protected float maxRatioScale;
    protected float minRatioScale;

    protected int cloneValueType;

    IData data;
    //CGHMultipleArrayDataFcd fcd;

    /** Creates a new instance of CGHViewerDataModel */
    //public CGHViewerDataModel(CGHMultipleArrayDataFcd fcd) {
    public CGHViewerDataModel(IFramework framework) {
        //this.fcd = fcd;
        //this.data = fcd.getData();
    	this.data = framework.getData();

        this.negColorImage = framework.getDisplayMenu().getNegativeGradientImage();
        this.posColorImage = framework.getDisplayMenu().getPositiveGradientImage();
    }


    public Color getColor(float value){

        if (Float.isNaN(value) || value == BAD_CLONE) {
            return COLOR_NULL;
        }

        if(this.cloneValueType == CLONE_VALUES_DISCRETE_DETERMINATION){
            return getDiscreteColor((int)value);
        }else{
            return getContinuousColor(value);
        }
    }

    public Color getContinuousColor(float value){
        float maximum = value < 0 ? minRatioScale : maxRatioScale;
        int colorIndex = (int)(255*value/maximum);
        colorIndex = colorIndex > 255 ? 255 : colorIndex;
        colorIndex = colorIndex < 0 ? 0 : colorIndex;
        int rgb = value < 0 ? negColorImage.getRGB(255-colorIndex, 0) : posColorImage.getRGB(colorIndex, 0);
        return new Color(rgb);

    }

    public Color getDiscreteColor(int copyNumber){

        if(copyNumber == BAD_CLONE){
            return COLOR_NULL;
        }

        if(copyNumber == NO_COPY_CHANGE){
            return COLOR_DEFAULT;
        }

        if(copyNumber < 0){
            if(copyNumber < -1){
                return COLOR_DEL_2_COPY;
            }else{
                return COLOR_DEL;
            }
        }

        if(copyNumber > 0){
            if(copyNumber > 1){
                return COLOR_AMP_2_COPY;
            }else{
                return COLOR_AMP;
            }
        }

        return COLOR_ERROR;
    }

    /** Getter for property negColorImage.
     * @return Value of property negColorImage.
     */
    public java.awt.image.BufferedImage getNegColorImage() {
        return negColorImage;
    }

    /** Setter for property negColorImage.
     * @param negColorImage New value of property negColorImage.
     */
    public void setNegColorImage(java.awt.image.BufferedImage negColorImage) {
        this.negColorImage = negColorImage;
    }

    /** Getter for property posColorImage.
     * @return Value of property posColorImage.
     */
    public java.awt.image.BufferedImage getPosColorImage() {
        return posColorImage;
    }

    /** Setter for property posColorImage.
     * @param posColorImage New value of property posColorImage.
     */
    public void setPosColorImage(java.awt.image.BufferedImage posColorImage) {
        this.posColorImage = posColorImage;
    }


    /** Getter for property maxRatioScale.
     * @return Value of property maxRatioScale.
     */
    public float getMaxRatioScale() {
        return maxRatioScale;
    }

    /** Setter for property maxRatioScale.
     * @param maxRatioScale New value of property maxRatioScale.
     */
    public void setMaxRatioScale(float maxRatioScale) {
        this.maxRatioScale = maxRatioScale;
    }

    /** Getter for property minRatioScale.
     * @return Value of property minRatioScale.
     */
    public float getMinRatioScale() {
        return minRatioScale;
    }

    /** Setter for property minRatioScale.
     * @param minRatioScale New value of property minRatioScale.
     */
    public void setMinRatioScale(float minRatioScale) {
        this.minRatioScale = minRatioScale;
    }

    /** Getter for property cloneValueType.
     * @return Value of property cloneValueType.
     */
    public int getCloneValueType() {
        return cloneValueType;
    }

    /** Setter for property cloneValueType.
     * @param cloneValueType New value of property cloneValueType.
     */
    public void setCloneValueType(int cloneValueType) {
        this.cloneValueType = cloneValueType;
    }

    public void onCloneValuesChanged(ICGHCloneValueMenu menu) {
        int type = menu.getCloneValueType();
        if(type == ICGHCloneValueMenu.CLONE_VALUE_DISCRETE_DETERMINATION ||
            type == ICGHCloneValueMenu.CLONE_VALUE_LOG_CLONE_DISTRIBUTION ||
            type == ICGHCloneValueMenu.CLONE_VALUE_THRESHOLD_OR_CLONE_DISTRIBUTION){
            this.cloneValueType = CLONE_VALUES_DISCRETE_DETERMINATION;
        }else{
            this.cloneValueType = CLONE_VALUES_CONTINUOUS;
        }
    }

    public int getCGHSpecies(){
    	return data.getCGHSpecies();
    }
}
