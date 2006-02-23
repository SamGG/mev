/*
 * NumberOfAlterationsDataModel.java
 *
 * Created on May 19, 2003, 12:45 AM
 */

package org.tigr.microarray.mev.cgh.CGHGuiObj.AlgorithmResultsViewers.NumberOfAlterationsViewers;

import javax.swing.table.AbstractTableModel;

import org.tigr.microarray.mev.cgh.CGHDataObj.AlterationRegion;
import org.tigr.microarray.mev.cgh.CGHDataObj.ICGHDataRegion;

/**
 *
 * @author  Adam Margolin
 * @author Raktim Sinha
 */

public class NumberOfAlterationsDataModel extends AbstractTableModel{
    AlterationRegion[] alterationRegions;

    /** Creates a new instance of NumberOfAlterationsDataModel */
    public NumberOfAlterationsDataModel() {
    }

    /** Getter for property alterationRegions.
     * @return Value of property alterationRegions.
     */
    public AlterationRegion[] getAlterationRegions() {
        return this.alterationRegions;
    }

    /** Setter for property alterationRegions.
     * @param alterationRegions New value of property alterationRegions.
     */
    public void setAlterationRegions(AlterationRegion[] alterationRegions) {
        this.alterationRegions = alterationRegions;
    }

    public int getColumnCount() {
        return 6;
    }

    public int getRowCount() {
        return alterationRegions.length;
    }

    public Object getValueAt(int row, int col) {
        switch(col){
            case 0:
                return alterationRegions[row].getDataRegion().getName();
            case 1:
                return new Integer(alterationRegions[row].getDataRegion().getChromosomeIndex() + 1);
            case 2:
                return new Integer(alterationRegions[row].getDataRegion().getStart());
            case 3:
                return new Integer(alterationRegions[row].getDataRegion().getStop());
            case 4:
                return new Integer(alterationRegions[row].getNumAlterations());
            case 5:
                return new Float(alterationRegions[row].getPercentAltered());
        }
        return null;
    }

    public String getColumnName(int column){
        switch(column){
            case 0:
                return "Name";
            case 1:
                return "Chrom";
            case 2:
                return "Start";
            case 3:
                return "Stop";
            case 4:
                return "# Alterations";
            case 5:
                return "% Altered";
        }
        return null;
    }
    /*
    public String getColumnName(int col){
        return results.getHeaderAt(col);
    }
     */
    public Class getColumnClass(int c) {
        if(getValueAt(0, c) == null){
          return String.class;
        }else{
            return getValueAt(0, c).getClass();
        }
    }

    public ICGHDataRegion getDataRegionAt(int index){
        return alterationRegions[index].getDataRegion();
    }

    /**
     * Raktim
     * Added to support viewing a range of Genes in NCBI etc from GenomeBrowser
     * @param indices
     * @return
     */
    public ICGHDataRegion[] getDataRegionAt(int[] indices){
    	ICGHDataRegion[] alteredRegions = new ICGHDataRegion[indices.length];
    	for(int i = 0; i < indices.length; i++) {
    		alteredRegions[i] = alterationRegions[indices[i]].getDataRegion();
    	}
        return alteredRegions;
    }

}
