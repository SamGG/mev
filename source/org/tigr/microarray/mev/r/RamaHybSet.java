/*
 * Created on Aug 22, 2005
 */
package org.tigr.microarray.mev.r;

import java.util.Vector;

/**
 * RamaHybSet is the group of slides loaded into MeV and assigned to be "Ramafied"
 * 
 * @author iVu
 */
public class RamaHybSet {
	private Vector vRamaHyb;
	private boolean isFlip;
	
	
	public RamaHybSet( Vector vRamaHybP ) {
		this.vRamaHyb = vRamaHybP;
		this.determineFlipness( vRamaHybP );
	}//constructor
	
	
	/**
	 * Just look through the RamaHybs to see if they're all the same, or if there
	 * are flipped ones.
	 * @param vRamaHybP
	 */
	private void determineFlipness( Vector vRamaHybP ) {
		this.isFlip = false;
		boolean controlCy3 = false;
		for( int i = 0; i < vRamaHybP.size(); i ++ ) {
			RamaHyb hyb = ( RamaHyb ) vRamaHybP.elementAt( i );
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
	
	
	public boolean isFlip() {
		return this.isFlip;
	}
	public Vector getVRamaHyb() {
		return this.vRamaHyb;
	}
}//end class