/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * Created on Aug 22, 2005
 */
package org.tigr.microarray.mev.r;

import java.util.Vector;

/**
 * RHybSet is a group of RHyb objects representing the slides loaded into MeV
 * 
 * @author iVu
 */
public class RHybSet {
	private Vector vRHyb;
	private boolean isFlip;
	
	
	public RHybSet( Vector vRHybP ) {
		this.vRHyb = vRHybP;
		this.determineFlipness( vRHybP );
	}//constructor
	
	
	/**
	 * Just look through the RHybs to see if they're all the same, or if there
	 * are flipped ones.
	 * @param vRHybP
	 */
	private void determineFlipness( Vector vRHybP ) {
		this.isFlip = false;
		boolean controlCy3 = false;
		for( int i = 0; i < vRHybP.size(); i ++ ) {
			RHyb hyb = ( RHyb ) vRHybP.elementAt( i );
			if( i == 0 ) {
				controlCy3 = hyb.controlCy3();
			} else {
				boolean nextControlCy3 = hyb.controlCy3();
				if( controlCy3 != nextControlCy3 ) {
					this.isFlip = true;
					break;
				}
			}
			
		}
	}//determineFlipness()
	
	/**
	 * Returns true if this was a flip color experiment
	 * @return
	 */
	public boolean isFlip() {
		return this.isFlip;
	}
	/**
	 * Returns a Vector of RHyb objects
	 * @return
	 */
	public Vector getVRHyb() {
		return this.vRHyb;
	}
}//end class