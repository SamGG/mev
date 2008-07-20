/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * Created on Jun 1, 2005
 */
package org.tigr.microarray.mev.cluster.gui.impl.usc;

import java.util.Comparator;
import java.util.Vector;

/**
 * @author iVu
 */
public class USCTableSorter implements Comparator {
    //
    private int colIndex;
    private boolean ascending;
    
    
    /**
     * 
     * @param colIndexP
     * @param ascendingP
     */
    public USCTableSorter(int colIndexP, boolean ascendingP) {
        this.colIndex = colIndexP;
        this.ascending = ascendingP;
    }//end constructor
    
    
    
    public int compare(Object a, Object b) {            
        Vector v1 = (Vector) a;
		Vector v2 = (Vector) b;
		Object o1 = v1.get(colIndex);
		Object o2 = v2.get(colIndex);
		
		if( o1 instanceof String && ((String)o1).length() == 0 ) {
		    o1 = null;
		}
		if( o2 instanceof String && ((String)o2).length() == 0 ) {
		    o2 = null;
		}
		
		if( o1 == null && o2 == null ) {
		    return 0;
		} else if(o1 == null) {
		    return 1;
		} else if(o2 == null) {
		    return -1;
		} else if(o1 instanceof Comparable) {
		    if(ascending) {
		        return ((Comparable) o1).compareTo(o2);
		    } else {
		        return ((Comparable) o2).compareTo(o1);
		    }
		} else {
		    if(ascending) {
		        return o1.toString().compareTo(o2.toString());
		    } else {
		        return o2.toString().compareTo(o1.toString());
		    }
		}
    }//compare
}//end class
