/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * Created on Feb 16, 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.tigr.microarray.mev.cluster.gui.impl.sota;

/**
 * Contains information about a SOTA result set.
 * @author eleanora
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SOTAInfoStats {
	int c1, clusterPop1, c2, clusterPop2; 
	float div1, dist, div2;
	
	public SOTAInfoStats(){}
	
	/**
	 * Convenience constructor that populates object
	 * @param c1
	 * @param clusterPop1
	 * @param div1
	 * @param dist
	 * @param c2
	 * @param clusterPop2
	 * @param div2
	 */
	//public SOTAInfoStats(int c1, int clusterPop1, float div1, float dist, int c2, int clusterPop2, float div2){
		
	//}
	
	/**
	 * @return Returns the c1.
	 */
	public int getC1() {
		return c1;
	}
	/**
	 * @param c1 The c1 to set.
	 */
	public void setC1(int c1) {
		this.c1 = c1;
	}
	/**
	 * @return Returns the c2.
	 */
	public int getC2() {
		return c2;
	}
	/**
	 * @param c2 The c2 to set.
	 */
	public void setC2(int c2) {
		this.c2 = c2;
	}
	/**
	 * @return Returns the clusterPop1.
	 */
	public int getClusterPop1() {
		return clusterPop1;
	}
	/**
	 * @param clusterPop1 The clusterPop1 to set.
	 */
	public void setClusterPop1(int clusterPop1) {
		this.clusterPop1 = clusterPop1;
	}
	/**
	 * @return Returns the clusterPop2.
	 */
	public int getClusterPop2() {
		return clusterPop2;
	}
	/**
	 * @param clusterPop2 The clusterPop2 to set.
	 */
	public void setClusterPop2(int clusterPop2) {
		this.clusterPop2 = clusterPop2;
	}
	/**
	 * @return Returns the dist.
	 */
	public float getDist() {
		return dist;
	}
	/**
	 * @param dist The dist to set.
	 */
	public void setDist(float dist) {
		this.dist = dist;
	}
	/**
	 * @return Returns the div1.
	 */
	public float getDiv1() {
		return div1;
	}
	/**
	 * @param div1 The div1 to set.
	 */
	public void setDiv1(float div1) {
		this.div1 = div1;
	}
	/**
	 * @return Returns the div2.
	 */
	public float getDiv2() {
		return div2;
	}
	/**
	 * @param div2 The div2 to set.
	 */
	public void setDiv2(float div2) {
		this.div2 = div2;
	}
}
