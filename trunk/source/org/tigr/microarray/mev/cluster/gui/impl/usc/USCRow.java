/*
 * Created on Dec 9, 2004
 */
package org.tigr.microarray.mev.cluster.gui.impl.usc;

import java.text.DecimalFormat;

import javax.swing.JRadioButton;

/**
 * 
 * 
 * @author vu
 */
public class USCRow {
	private JRadioButton button;
	private String numErrors;
	private String numCalls;
	private String numGenes;
	private String delta;
	private String rho;
	private int iDeltaBin;
	private int iRhoBin;
	
	
	public USCRow( int iErrors, int iCalls, double fGenes, double dDelta, 
	double dRho ) {
		DecimalFormat df = new DecimalFormat( "###.#" );
		
		this.button = new JRadioButton();
		this.numErrors = df.format( iErrors );
		this.numCalls = df.format( iCalls );
		this.numGenes = df.format( fGenes );
		this.delta = df.format( dDelta );
		this.rho = df.format( dRho );
	}
	public USCRow( String sErrors, String sNumGenes, String sDelta, String sRho ) {
		this.numErrors = sErrors;
		this.numGenes = sNumGenes;
		this.delta = sDelta;
		this.rho = sRho;
		this.button = new JRadioButton();
	}
	
	
	public JRadioButton getButton() {
		return this.button;
	}
	public String getNumErrors() {
		return this.numErrors;
	}
	public int getIErrors() {
		return new Integer( this.numErrors ).intValue();
	}
	public String getNumCalls() {
		return this.numCalls;
	}
	public int getICalls() {
		return new Integer( this.numCalls ).intValue();
	}
	public String getNumGenes() {
		return this.numGenes;
	}
	public int getIGenes() {
		return new Integer( this.numGenes ).intValue();
	}
	public String getDelta() {
		return this.delta;
	}
	public double getFDelta() {
		return new Double( this.delta ).doubleValue();
	}
	public String getRho() {
		return this.rho;
	}
	public double getFRho() {
		return new Double( this.rho ).doubleValue();
	}
}