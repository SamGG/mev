/*
 * Created on Feb 24, 2006
 */
package org.tigr.microarray.mev.cluster.gui.impl.bridge;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.tigr.microarray.mev.r.RProgress;
import org.tigr.microarray.mev.r.RSrvException;
import org.tigr.microarray.mev.r.Rconnection;
import org.tigr.microarray.mev.r.SwingWorker;

/**
 * @author iVu
 */
public class BridgeWorker extends SwingWorker {
	private Rconnection rc;
	
	private String sClear;
	private String sLibrary;
	private String sData;
	private String sReform;
	private String sMcmc;
	private String sAvg1;
	private String sAvg2;
	private String sPost;
	
	private double threshold;
	
	private boolean ok;
	private boolean done;

	private RProgress progress;
	
	private BridgeResult result;
	
	
	public BridgeWorker( Rconnection rcP, String clearP, String libraryP, 
			String dataP, String reformP, String mcmcP, String avg1P, 
			String avg2P, String postP, double thresholdP, RProgress progressP ) {
		this.rc = rcP;
		this.sClear = clearP;
		this.sLibrary = libraryP;
		this.sData = dataP;
		this.sReform = reformP;
		this.sMcmc = mcmcP;
		this.sAvg1 = avg1P;
		this.sAvg2 = avg2P;
		this.sPost = postP;
		this.threshold = thresholdP;
		this.progress = progressP;
	}//constructor()


    /**
     * Write the String s to File f
     * 
     * @param f
     * @param s
     */
    private void writeFile(File f, String s) {
        try {
            FileWriter fw = new FileWriter(f);
            fw.write(s);
            fw.flush();
            fw.close();
        } catch( IOException e ) {
            e.printStackTrace();
        }
    }//writeFile()http://www.youtube.com/watch?v=XpO2U-mXvsk
	
	
	public Object construct() {
		this.ok = false;
		this.done = false;
		
		//record the start time
		Calendar startCal = new GregorianCalendar();
		int startHour = startCal.get( Calendar.HOUR_OF_DAY );
		int startMin = startCal.get( Calendar.MINUTE );
		String startTime = startHour + ":" + startMin;
		System.out.println( "Bridge started at " + startTime );
		
		try {
			//should clear R
			this.rc.voidEval( this.sClear );
			
			//load rama
			this.rc.voidEval( this.sLibrary );
			
			//load data as vector
			this.rc.voidEval( this.sData );
			
			//reform vector data into matrix
			this.rc.voidEval( this.sReform );
			
			//for testing
			double[][] matrix = this.rc.eval( "bData" ).asDoubleMatrix();
			StringBuffer sb = new StringBuffer();
			for( int i = 0; i < matrix.length; i ++ ) {
				if( i > 0 ) {
					sb.append( "\r\n" );
				}
				
				for( int j = 0; j < matrix[ i ].length; j ++ ) {
					//System.out.println(i+":"+j + "="+matrix[ i ][j ]);
					if( j > 0 ) {
						sb.append( "\t" );
					}
					sb.append( matrix[ i ][ j ] );
				}
			}
			//File f = new File("/Users/iVu/Documents/Dev/MeV/out.txt");
			//this.writeFile( f, sb.toString() );
			
			
			//call fit.model()
			this.rc.voidEval( this.sMcmc );
			
			//record the end time
			Calendar endCal = new GregorianCalendar();
			int endHour = endCal.get( Calendar.HOUR_OF_DAY );
			int endMin = endCal.get( Calendar.MINUTE );
			String endTime = endHour + ":" + endMin;
			System.out.println( "Bridge ended at " + endTime );
			
			//avg gammas first
			this.rc.voidEval( this.sAvg1 );
			this.rc.voidEval( this.sAvg2 );
			
			//retrieve gammas
			double[] gamma1 = (double[]) rc.eval("gamma1").getContent();
			//double[] gamma1 = rc.eval( "gamma1" ).asDoubleArray();
			double[] gamma2 = rc.eval( "gamma2" ).asDoubleArray();
			
			double[] postP = rc.eval( this.sPost ).asDoubleArray();
			
			this.result = new BridgeResult( gamma1, gamma2, postP, this.threshold );
			
			//seems to have worked
			this.ok = true;
			this.done = true;
		} catch ( RSrvException e ) {
			e.printStackTrace();
			//this.rama.error( e.getMessage() );
			this.ok = false;
			this.done = false;
			this.error( e.getMessage() );
		} finally {
			this.progress.kill();
		}
		
		//System.out.println( "Done with construct()" );
		return this.result;
	}//construct()
	
	
	public void finished() {
		this.ok = true;
		this.done = true;
		//System.out.println( "Finished" );
	}
	
	
	/**
	 * Displays an error dialog
	 * @param message
	 */
	public void error( String message ) {
		JOptionPane.showMessageDialog( new JFrame(), 
				message, "Input Error", JOptionPane.ERROR_MESSAGE );
	}//end error()
	
	
	
	public boolean isOk() {
		return this.ok;
	}
	public boolean isDone() {
		return this.done;
	}
	public BridgeResult getResult() {
		return this.result;
	}
}//end class
