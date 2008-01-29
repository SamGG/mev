/*
 * CGHTableDataModel.java
 *
 * Created on December 28, 2002, 11:14 PM
 */

package org.tigr.microarray.mev.cgh.CGHDataModel;

import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;

import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IFramework;
/*
import java.util.Vector;
import java.util.Iterator;
import java.awt.Color;
import org.tigr.microarray.mev.cgh.CGHDataObj.*;
import java.util.Hashtable;
import cern.jet.math.Arithmetic;
import cern.jet.stat.Probability;
*/
/**
 *
 * @author  Adam Margolin
 * @author Raktim Sinha
 */

public abstract class CGHTableDataModel extends AbstractTableModel implements ChangeListener{

    //CGHMultipleArrayDataFcd fcd;
    IData data;
    //IFramework framework;

    CGHBrowserModelAdaptor adaptor;

    public CGHTableDataModel(/*CGHMultipleArrayDataFcd fcd,*/ IFramework framework){
        this(/*fcd,*/ framework, 0, 0);
    }

    public CGHTableDataModel(/*CGHMultipleArrayDataFcd fcd,*/ IFramework framework, int experimentIndex, int chromosomeIndex){
        //this.fcd = fcd;
        this.data = framework.getData();
    }

    public abstract int getColumnCount();


    /**
     *  @return the number of columns before the data values
     */
    public int getNumAnnotationCols(){
        return 4;
    }

    public int getRowCount() {
        return getSeriesSize();
    }

    public Object getValueAt(int row, int col) {

        try{
            switch(col){
                case 0:
                    return data.getCloneAt(adaptor.getCloneIndex(row)).getName();
                case 1:
                    return new Integer(data.getCloneAt(adaptor.getCloneIndex(row)).getChromosome());
                case 2:
                    return new Integer(data.getCloneAt(adaptor.getCloneIndex(row)).getStart());
                case 3:
                    return new Integer(data.getCloneAt(adaptor.getCloneIndex(row)).getStop());
            }

            return getDataValueAt(row, col);

        }catch(NullPointerException e){
            return "";
        }
    }

    public abstract Object getDataValueAt(int row, int col);




    public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }

    public abstract String getColumnDataName(int labelIndex);



    public String getColumnName(int col){
        switch(col){
            case 0:
                return "Clone Name";
            case 1:
                return "Chromosome";
            case 2:
                return "Start";
            case 3:
                return "Stop";

        }
        return getColumnDataName(col - getNumAnnotationCols());
    }

    public void addListDataListener(javax.swing.event.ListDataListener listDataListener) {
    }

    public void removeListDataListener(javax.swing.event.ListDataListener listDataListener) {
    }

    private int getSeriesSize(){
        return adaptor.getSeriesSize();
    }

    public void stateChanged(javax.swing.event.ChangeEvent changeEvent) {
        fireTableStructureChanged();
    }

    public void setExperimentIndex(int experimentIndex){
        adaptor.setExperimentIndex(experimentIndex);
    }

    public void setChromosomeIndex(int chromosomeIndex){
        adaptor.setChromosomeIndex(chromosomeIndex);
    }

    public void setCloneValueType(int cloneValueType){
        adaptor.setCloneValueType(cloneValueType);
    }

    /** Getter for property adaptor.
     * @return Value of property adaptor.
     */
    public CGHBrowserModelAdaptor getAdaptor() {
        return adaptor;
    }

    /** Setter for property adaptor.
     * @param adaptor New value of property adaptor.
     */
    public void setAdaptor(CGHBrowserModelAdaptor adaptor) {
        this.adaptor = adaptor;
    }

}
