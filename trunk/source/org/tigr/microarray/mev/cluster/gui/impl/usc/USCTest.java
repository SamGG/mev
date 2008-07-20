/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
 /*
 * Created on Feb 17, 2005
 */
package org.tigr.microarray.mev.cluster.gui.impl.usc;

import org.tigr.microarray.mev.r.REXP;
import org.tigr.microarray.mev.r.RSrvException;
import org.tigr.microarray.mev.r.Rconnection;

/**
 * @author vu
 */
public class USCTest {
	//
	public USCTest() {
		try {
			Rconnection rc = new Rconnection();
			REXP x = rc.eval("R.version.string");
			System.out.println(x.asString());
			
		} catch( RSrvException e ) {
			e.printStackTrace();
		}
	}
	
	
	public static void main( String[] args ) {
		USCTest u = new USCTest();
	}
	
	
	private int computeCombinations( int n, int c ) {
		int toReturn = 0;
		
		int nFactorial = n;
		for( int i = n - 1; i > 0; i -- ) {
			nFactorial = nFactorial * i;
		}
		
		int cFactorial = c;
		for( int i = ( c - 1 ); i > 0; i -- ) {
			cFactorial = cFactorial * i;
		}
		
		int diff = ( n - c );
		int diffFactorial = diff;
		for( int i = ( diff - 1 ); i > 0; i -- ) {
			diffFactorial = diffFactorial * i;
		}
		
		toReturn = ( nFactorial / ( cFactorial * diffFactorial ) );
		System.out.println( toReturn + " combinations of " + n );
		
		return toReturn;
	}//computeCombinations
}
