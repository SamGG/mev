/*
 * Created on Jan 24, 2005
 */
package org.tigr.microarray.mev.cluster.gui.impl.usc;

import java.util.Comparator;

/**
 * Sorts the USCOrder[] from least to greatest IOriginal
 * 
 * @author vu
 */
public class USCOrderSorter implements Comparator {
	public int compare(Object o1, Object o2) {
		USCOrder order1 = ( USCOrder ) o1;
		USCOrder order2 = ( USCOrder ) o2;
		
		if( order1.getIOriginal() > order2.getIOriginal() ) {
			return 1;
		} else if( order1.getIOriginal() < order2.getIOriginal() ) {
			return -1;
		} else {
			return 0;
		}
	}

}