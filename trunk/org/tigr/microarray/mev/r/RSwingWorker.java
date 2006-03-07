/*
 * Created on Feb 6, 2006
 */
package org.tigr.microarray.mev.r;

import java.util.Calendar;
import java.util.GregorianCalendar;


/**
 * @author iVu
 */
public class RSwingWorker extends SwingWorker {
	private Rconnection rc;
	
	private RamaResult ramaResult;
	
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
	
	private boolean allOut;
	private boolean ok;
	private boolean done;
	
	private RProgress progress;
	
	private Rama rama;
	
	
	public RSwingWorker( Rconnection rcP, String sClearP, String sLibraryP,  
			String sDataP, String sReformP, String sMcmcP, boolean allOutP,
			String sAvgGamma1P, String sAvgGamma2P, String sQLoP, String sQUpP,
			String sShiftP, RProgress progressP, Rama ramaP ) {
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
		
		//record the start time
		Calendar startCal = new GregorianCalendar();
		int startHour = startCal.get( Calendar.HOUR_OF_DAY );
		int startMin = startCal.get( Calendar.MINUTE );
		String startTime = startHour + ":" + startMin;
		
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
			
			//retrieve shift
			double shift = rc.eval( this.sShift ).asDouble();
			
			//record the end time
			Calendar endCal = new GregorianCalendar();
			int endHour = endCal.get( Calendar.HOUR_OF_DAY );
			int endMin = endCal.get( Calendar.MINUTE );
			String endTime = endHour + ":" + endMin;
			
			//if allOut, gammas need to be averaged and qLo+qUp need to be fetched
			double[] qLo;
			double[] qUp;
			double[] gamma1;
			double[] gamma2;
			if( this.allOut ) {
				//avg gamms first
				this.rc.voidEval( this.sAvgGamma1 );
				this.rc.voidEval( this.sAvgGamma2 );
				
				//retrieve gammas
				gamma1 = rc.eval( "gamma1" ).asDoubleArray();
				gamma2 = rc.eval( "gamma2" ).asDoubleArray();
				
				//get credible intervals
				qLo = this.rc.eval( this.sQLo ).asDoubleArray();
				qUp = this.rc.eval( this.sQUp ).asDoubleArray();
				
				this.ramaResult = new RamaResult( gamma1, gamma2, 
						qLo, qUp, shift, startTime, endTime );
			} else {
				//retrieve gammas
				gamma1 = rc.eval( "gamma1" ).asDoubleArray();
				gamma2 = rc.eval( "gamma2" ).asDoubleArray();
				
				this.ramaResult = new RamaResult( gamma1, gamma2, shift,
						startTime, endTime );
			}
			
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
		this.rama.fireThreadFinished( this, this.ramaResult );
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
