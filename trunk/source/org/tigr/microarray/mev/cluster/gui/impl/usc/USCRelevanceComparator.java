/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * Created on Nov 16, 2004
 */
package org.tigr.microarray.mev.cluster.gui.impl.usc;

import java.util.Comparator;

/**
 * Compares 2 USCOrder objects by beta, returning 1 if USCOrder1.beta > USCOrder2.beta
 * @author vu
 */
public class USCRelevanceComparator implements Comparator {
	public int compare(Object o1, Object o2) {
		USCOrder order1 = ( USCOrder ) o1;
		USCOrder order2 = ( USCOrder ) o2;
		
		if( order1.getBeta() < order2.getBeta() ) {
			return 1;
		} else if( order1.getBeta() > order2.getBeta() ) {
			return -1;
		} else {
			return 0;
		}
	}//end compare()
}//end class
