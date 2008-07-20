/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * Created on Sep 27, 2005
 */
package org.tigr.microarray.mev.r;

import java.util.Vector;

import org.tigr.microarray.mev.cluster.gui.IData;

/**
 * This is a generic data formatting class.  If the data is dye-swapped, use 
 * rSwapString().  If not, use rNonSwapString().
 * 
 * @author iVu
 */
public class RDataFormatter {
	private IData data;
	
	
	/**
	 * Constructor.
	 * @param dataP 
	 */
	public RDataFormatter( IData dataP ) {
		this.data = dataP;
	}
	
	
	/**
	 * This method returns a string that can be inserted into an Rconnection.eval() 
	 * statement.  It takes 2 Vectors as parameters.  vTreatCy3 is the Vector of 
	 * RHyb objects in which the Treated sample was labeled with Cy3.  vTreatCy5 
	 * is naturally the opposite case.  The returned String is a comma-delimited, 
	 * concatenation of [ treatedCy3, treatedCy5, controlCy5, controlCy3 ].
	 * @param dataName	Name to be assigned for this data object in R
	 * @param vTreatCy3	Vector of RHyb objects where the treated sample is Cy3
	 * @param vTreatCy5	Vector of RHyb objects where the treated sample is Cy5
	 * @return String [ treatedCy3, treatedCy5, controlCy5, controlCy3 ]
	 */
	public String rSwapString( String dataName, Vector vTreatCy3, Vector vTreatCy5 ) {
		//System.out.println("ramaSwapString()");
		StringBuffer sbTreat = new StringBuffer( dataName + " <- c(" );
		StringBuffer sbControl = new StringBuffer();
		
		int iTreat3 = vTreatCy3.size();
		int iTreat5 = vTreatCy5.size();
		
		int iGene = this.data.getExperiment().getNumberOfGenes();
		
		//first gather all treat3
		for( int i = 0; i < iTreat3; i ++ ) {
			if( i > 0 ) {
				sbTreat.append( Rama.COMMA );
			}
			RHyb hyb = ( RHyb ) vTreatCy3.elementAt( i );
			int hybIndex = hyb.getHybIndex();
			
			//loop through the genes of this hyb
			for( int g = 0; g < iGene; g ++ ) {
				if( g > 0 ) {
					sbTreat.append( Rama.COMMA );
				}
				
				float cy3 = this.data.getCY3( hybIndex, g );
				cy3 ++;
				if( cy3 < 0 ) {
					cy3 = 0.0001f;
				}
				sbTreat.append( cy3 );
				//sbTreat.append( this.data.getCY3( hybIndex, g ) );
				//System.out.println( "TreatCy3("+i+","+g+"):" + data.getCY3( hybIndex, g ) );
			}//g
		}//i
		sbTreat.append( Rama.COMMA );
		//next do all treat5
		for( int i = 0; i < iTreat5; i ++ ) {
			if( i > 0 ) {
				sbTreat.append( Rama.COMMA );
			}
			RHyb hyb = ( RHyb ) vTreatCy5.elementAt( i );
			int hybIndex = hyb.getHybIndex();
			
			//loop through the genes of this hyb
			for( int g = 0; g < iGene; g ++ ) {
				if( g > 0 ) {
					sbTreat.append( Rama.COMMA );
				}
				
				float cy5 = this.data.getCY5( hybIndex, g );
				cy5 ++;
				if( cy5 < 0 ) {
					cy5 = 0.0001f;
				}
				sbTreat.append( cy5 );
				//sbTreat.append( this.data.getCY5( hybIndex, g ) );
				//System.out.println( "TreatCy5("+i+","+g+"):" + data.getCY5( hybIndex, g ) );
			}//g
		}//i
		sbTreat.append( Rama.COMMA );
		
		
		//next control5
		for( int i = 0; i < iTreat3; i ++ ) {
			if( i > 0 ) {
				sbTreat.append( Rama.COMMA );
			}
			RHyb hyb = ( RHyb ) vTreatCy3.elementAt( i );
			int hybIndex = hyb.getHybIndex();
			
			//loop through the genes of this hyb
			for( int g = 0; g < iGene; g ++ ) {
				if( g > 0 ) {
					sbTreat.append( Rama.COMMA );
				}
				
				float cy5 = this.data.getCY5( hybIndex, g );
				cy5++;
				if( cy5 < 0 ) {
					cy5 = 0.0001f;
				}
				sbTreat.append( cy5 );
				//sbTreat.append( this.data.getCY5( hybIndex, g ) );
				//System.out.println( "ControlCy3("+i+","+g+"):" + data.getCY5( hybIndex, g ) );
			}//g
		}//i
		sbTreat.append( Rama.COMMA );
		//finally control3
		for( int i = 0; i < iTreat5; i ++ ) {
			if( i > 0 ) {
				sbTreat.append( Rama.COMMA );
			}
			RHyb hyb = ( RHyb ) vTreatCy5.elementAt( i );
			int hybIndex = hyb.getHybIndex();
			
			//loop through the genes of this hyb
			for( int g = 0; g < iGene; g ++ ) {
				if( g > 0 ) {
					sbTreat.append( Rama.COMMA );
				}
				
				float cy3 = this.data.getCY3( hybIndex, g );
				cy3 ++;
				if( cy3 < 0 ) {
					cy3 = 0.0001f;
				}
				sbTreat.append( cy3 );
				//sbTreat.append( this.data.getCY3( hybIndex, g ) );
				//System.out.println( "ControlCy5("+i+","+g+"):" + data.getCY3( hybIndex, g ) );
			}//g
		}//i
		sbTreat.append( ")" );
		//System.out.println(sbTreat.toString());
		return sbTreat.toString();
	}//ramaSwapString()
	
	/**
	 * This method returns a string that can be inserted into an Rconnection.eval()
	 * statement.  It takes a Vector of RHyb objects as a parameter.
	 * @param dataName	Name to be assigned for this data object in R
	 * @param vRamaHyb	Vector RHyb objects
	 * @return	String 
	 */
	public String rNonSwapString( String dataName, Vector vRamaHyb ) {
		//System.out.println("ramaNonSwapString()");
		StringBuffer sbTreat = new StringBuffer( dataName + " <- c(" );
		StringBuffer sbControl = new StringBuffer();
		
		//figure out which color is which
		RHyb firstHyb = ( RHyb ) vRamaHyb.elementAt( 0 );
		boolean controlCy3 = firstHyb.controlCy3();
		
		//loop through all the hybs, they will all be the same color state
		for( int i = 0; i < vRamaHyb.size(); i ++ ) {
			if( i > 0 ) { 
				sbTreat.append( "," );
				sbControl.append( "," );
			}
			
			RHyb hyb = ( RHyb ) vRamaHyb.elementAt( i );
			int iHyb = hyb.getHybIndex();
			
			//loop through the genes
			int iGene = this.data.getExperiment().getNumberOfGenes();
			for( int g = 0; g < iGene; g ++ ) {
				if( g > 0 ) {
					sbTreat.append( "," );
					sbControl.append( "," );
				}
				
				if( controlCy3 ) {
					//treatment is Cy5 for all hybs since they're all the same
					sbTreat.append( this.data.getCY5( iHyb, g ) );
					sbControl.append( this.data.getCY3( iHyb, g ) );
					//System.out.println( i + "," + g + ":" + data.getCY5( iHyb, g ) );
				} else {
					//treatment is Cy3 for all hybs since they're all the same
					sbTreat.append( this.data.getCY3( iHyb, g ) );
					sbControl.append( this.data.getCY5( iHyb, g ) );
					//System.out.println( i + "," + g + ":" + data.getCY3( iHyb, g ) );
				}
			}//g
		}//i
		
		sbTreat.append( "," );
		sbTreat.append( sbControl );
		sbTreat.append( ")" );
		
		return sbTreat.toString();
	}//ramaNonSwapString()
}
