/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/**
 * @author  raktim
 * @version 1.0
 */

package org.tigr.rhook;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class RLogger {

    private String      logFileName  = null;
    private PrintWriter pw           = null;
    private Date        startTime    = null;
    private Date        stopTime     = null;


    /**
     * Used to write a given String object (without a newline) to the log file.
     * @param s String that will be written to the log file.
     */
    public void write (String s) {
        pw.print(s);
    }


    /**
     * Used to write a given String object (with a newline) to the log file.
     * @param s String that will be written to the log file.
     */
    public void writeln (String s) {
        pw.println(s);
    }

    public void writeln(Exception e) {
    	e.printStackTrace(pw);
    }

    /**
     * Used to write a newline character to the log file.
     */
    public void writeln () {
        pw.println();
    }


    /**
     * Used to open the log file for writing.  This method MUST be called
     * before writing to the log file.
     */
    public void start() {
        startTime = Calendar.getInstance().getTime();

        try {
            pw = new PrintWriter(new FileWriter(logFileName, true));
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        
        pw.println("# --------------------------------------------");
        pw.println("# RHook LOG FILE     : " + logFileName);
        pw.println("# START TIME   : " + startTime);
        pw.println("# --------------------------------------------");
        pw.println();
    }


    /**
     * Used to flush and close the log file.  This method MUST be called in 
     * order for all data to be flushed and saved to the log file.
     */
    public void stop() {
        stopTime = Calendar.getInstance().getTime();
        long diff = stopTime.getTime() - startTime.getTime();

        pw.println();
        pw.println("# --------------------------------------------");
        pw.println("#           << END OF SESSION >>              ");
        pw.println("# --------------------------------------------");
        pw.println("# STOP TIME    : " + stopTime);
        pw.println("# ELAPSED TIME : " + (diff / (1000L)) + " seconds.");
        pw.println("# --------------------------------------------");
        pw.close();
    }

    public static String getLogFileName() {
		String file = "limma.log";
		System.out.println("user.home " + System.getProperty("user.home"));
		System.out.println("user.dir " + System.getProperty("user.dir"));

		File f = new File(System.getProperty("user.dir")+System.getProperty("file.separator")+file);
		if(f.exists()) {
			//Check size, rename if more than 500 KB
			if(f.length()/1000 > 500) {
				Date now = new Date();
				//String dateString = now.toString();
				SimpleDateFormat formatDt = new SimpleDateFormat("MM_dd_yy_HHmmss");
				String dateString = formatDt.format(now);
				File nf = new File(System.getProperty("user.home")+System.getProperty("file.separator")+file+dateString);
				if(!f.renameTo(nf)) {
					System.out.println("Unable to rename limma.log file");
				}
			}
		} else {
			try {
				f.createNewFile();
			} catch (IOException e) {
				System.out.println("Unable to create limma.log file");
				e.printStackTrace();
			}
		}
		return file;
	}

    /**
     * Constructor used to create this object.  Responsible for setting
     * this object's log file name.
     * @param logFileName Name of the path/logfile to create.
     */
    public RLogger (String logFileName) {
        this.logFileName = logFileName;
    }

}
