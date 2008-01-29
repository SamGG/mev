/*
Copyright @ 1999-2006, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * Created on Apr 7, 2005
 */
package org.tigr.microarray.mev.cluster.gui.impl.lem;

import java.util.Vector;

/**
 * @author braisted
 */
public class LEMLocusInfo implements Comparable {

	public int index;
	public String locus;
	public String chrID;
	public int start;
	public int end;
	public Vector iDataRefs;
	public Vector overLapListIDs;
	
	public boolean doWeOverlap(LEMLocusInfo other) {
		return (inRange(other.start) || inRange(other.end) || other.inRange(this.start));					
	}
	
	public boolean inRange(int coord) {
		return (coord >= start && coord <= end);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object obj) {
		LEMLocusInfo other = (LEMLocusInfo)obj;
		if(other.start < this.start)
			return -1;
		if(other.start > this.start)
			return 1;
		return 0;
	}
	
	public boolean equals(LEMLocusInfo other) {
		return (locus.equals(other.locus));	
	}

}
