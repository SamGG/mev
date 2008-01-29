/*
 * Created on Dec 5, 2005
 */
package org.tigr.microarray.mev.cluster.gui.impl.bridge;

import java.util.Vector;


/**
 * @author iVu
 */
public class BridgeResult {
	private double[] gamma1;
	private double[] gamma2;
	private double[] postP;
	private double threshold;
	private int[] sigIndices;
	private int[] nonIndices;
	private String[] geneNames;
	private String[] headers;
	private String[][] auxData;	//[ 4 ][ numGenes ]
	
	
	
	public BridgeResult( double[] g1, double[] g2, double[] pp, double t ) {
		this.gamma1 = g1;
		this.gamma2 = g2;
		this.postP = pp;
		this.threshold = t;
		this.parseSigGenes();
		this.packageAuxData();
	}
	
	
	private void packageAuxData() {
		this.headers = new String[ 4 ];
		this.headers[ 0 ] = "Gamma1-Gamma2";
		this.headers[ 1 ] = "Posterior Probability";
		this.headers[ 2 ] = "Gamma1";
		this.headers[ 3 ] = "Gamma2";
		
		int numGenes = this.gamma1.length;
		this.auxData = new String[ numGenes ][ 4 ];
		
		//loop through the genes
		for( int i = 0; i < numGenes; i ++ ) {
			this.auxData[ i ][ 0 ] = Double.toString( this.gamma1[ i ] - this.gamma2[ i ] );
			this.auxData[ i ][ 1 ] = Double.toString( this.postP[ i ] );
			this.auxData[ i ][ 2 ] = Double.toString( this.gamma1[ i ] );
			this.auxData[ i ][ 3 ] = Double.toString( this.gamma2[ i ] );
		}//i
	}
	
	
	private void parseSigGenes() {
		Vector vSig = new Vector();
		Vector vNon = new Vector();
		
		for( int i = 0; i < this.postP.length; i ++ ) {
			if( this.postP[ i ] >= this.threshold ) {
				vSig.add( new Integer( i ) );
			} else {
				vNon.add( new Integer( i ) );
			}
		}//i
		
		this.sigIndices = new int[ vSig.size() ];
		for( int i = 0; i < vSig.size(); i ++ ) {
			Integer I = ( Integer ) vSig.elementAt( i );
			int iSig = I.intValue();
			this.sigIndices[ i ] = iSig;
		}
		
		this.nonIndices = new int[ vNon.size() ];
		for( int i = 0; i < vNon.size(); i ++ ) {
			Integer I = ( Integer ) vNon.elementAt( i );
			int iNon = I.intValue();
			this.nonIndices[ i ] = iNon;
		}
	}//parseSigGenes()
	
	
	public double[] getGamma1() {
		return this.gamma1;
	}
	public double[] getGamma2() {
		return this.gamma2;
	}
	public double[] getPostP() {
		return this.postP;
	}
	public void setGeneNames( String[] gn ) {
		this.geneNames = gn;
	}
	public String[] getGeneNames() {
		return this.geneNames;
	}
	public int[] getSigIndices() {
		return this.sigIndices;
	}
	public int[] getNonIndices() {
		return this.nonIndices;
	}
	public int getSigKount() {
		return this.sigIndices.length;
	}
	public int getNonKount() {
		return this.nonIndices.length;
	}
	public String[] getHeaders() {
		return this.headers;
	}
	public String[][] getAuxData() {
		return this.auxData;
	}
}
