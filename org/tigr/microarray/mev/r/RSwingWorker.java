/*
 * Created on Feb 6, 2006
 */
package org.tigr.microarray.mev.r;


/**
 * @author iVu
 */
public class RSwingWorker extends SwingWorker {
	private Rconnection rc;
	
	private String sClear;
	private String sLibrary;
	private String sData;
	private String sReform;
	private String sMcmc;
	private String sAvgGamma1;
	private String sAvgGamma2;
	private String sQLo;
	private String sQUp;
	private String sShift;
	
	private int iGene;
	
	private double shift;
	
	private double[] gamma1;
	private double[] gamma2;
	private double[] qLo;
	private double[] qUp;
	
	private boolean allOut;
	private boolean ok;
	private boolean done;
	
	private RamaProgress progress;
	
	private Rama rama;
	
	
	public RSwingWorker( Rconnection rcP, String sClearP, String sLibraryP,  
			String sDataP, String sReformP, String sMcmcP, boolean allOutP,
			String sAvgGamma1P, String sAvgGamma2P, String sQLoP, String sQUpP,
			String sShiftP, RamaProgress progressP, Rama ramaP ) {
		this.rc = rcP;
		this.sClear = sClearP;
		this.sLibrary = sLibraryP;
		this.sData = sDataP;
		this.sReform = sReformP;
		this.sMcmc = sMcmcP;
		this.sQLo = sQLoP;
		this.sQUp = sQUpP;
		this.sShift = sShiftP;
		this.allOut = allOutP;
		this.sAvgGamma1 = sAvgGamma1P;
		this.sAvgGamma2 = sAvgGamma2P;
		this.progress = progressP;
		this.rama = ramaP;
	}//constructor()
	
	

	/**
	 * 
	 */
	public Object construct() {
		this.done = false;
		
		try {
			//should clear R
			this.rc.voidEval( this.sClear );
			
			//load rama
			this.rc.voidEval( this.sLibrary );
			
			//load data as vector
			this.rc.voidEval( this.sData );
			
			//reform vector data into matrix
			this.rc.voidEval( this.sReform );
			
			//call fit.model()
			this.rc.voidEval( this.sMcmc );
			
			//if allOut, gammas need to be averaged and qLo+qUp need to be fetched
			if( this.allOut ) {
				this.rc.voidEval( this.sAvgGamma1 );
				this.rc.voidEval( this.sAvgGamma2 );
				
				this.qLo = this.rc.eval( this.sQLo ).asDoubleArray();
				this.qUp = this.rc.eval( this.sQUp ).asDoubleArray();
			}
			
			//retrieve gammas
			this.gamma1 = rc.eval( "gamma1" ).asDoubleArray();
			this.gamma2 = rc.eval( "gamma2" ).asDoubleArray();
			
			//retrieve shift
			this.shift = rc.eval( this.sShift ).asDouble();
			
			this.ok = true;
			this.done = true;
		
		} catch ( RSrvException e ) {
			e.printStackTrace();
			this.rama.error( e.getMessage() );
			this.ok = false;
			this.done = false;
		} finally {
			this.progress.kill();
		}
		
		return null;
	}//construct
	
	
	public void finished() {
		//System.out.println( "Finished" );
		this.rama.fireThreadFinished( this );
	}
	
	
	public double getShift() {
		return this.shift;
	}
	public double[] getQLo() {
		return this.qLo;
	}
	public double[] getQUp() { 
		return this.qUp;
	}
	public double[] getGamma1() {
		return this.gamma1;
	}
	public double[] getGamma2() {
		return this.gamma2;
	}
	public boolean isOk() {
		return this.ok;
	}
	public boolean isDone() {
		return this.done;
	}
	public boolean isAllOut() {
		return this.allOut;
	}

}
