/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
package org.tigr.microarray.mev.cluster.gui.helpers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.tigr.microarray.mev.ShowThrowableDialog;
import org.tigr.microarray.mev.TMEV;

/**
 * Webstart utility file for Genome Browser
 * Copied from CytoscapeWebstart by Raktim
 * @author eleanorahowe
 *
 */
public class GenomeBrowserWebstart {

	public static Process runtimeProc;
	
	/**
     * Public static function invokes webstart and creates the jnlp file.
     * Assumes that MeV is connected to the Boss.
     */
    public static void onWebstartGenomeBrowser(String organism) {
   	
    	String jnlpContent = createGenomeBrowserJNLP(organism);

    	try {
	    	File jnlpFile = File.createTempFile("genome_browser_launch", ".jnlp");
            BufferedWriter out = new BufferedWriter(new FileWriter(jnlpFile));
            out.write(jnlpContent);
            out.close();
            startGenomeBrowser(jnlpFile.getAbsolutePath());
        } catch (IOException e) {
        	JOptionPane.showMessageDialog(new JFrame(), "Error creating jnlp file", "Cytoscape will not launch", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * private Static function to fork Java Webstart based on a JNLP file
     */
    private static void startGenomeBrowser(String jnlpURI) {
        String command = System.getProperty("java.home");
        if(System.getProperty("os.name").toLowerCase().contains("win")) {
             //jnlpURI in quotes incase there are spaces in file path on Win
             command += File.separator +  "bin" + File.separator + "javaws \"" + jnlpURI+"\"";
        } else {
             command += File.separator +  "bin" + File.separator + "javaws " + jnlpURI;    
        }
        try {
        	runtimeProc = Runtime.getRuntime().exec(command);
        } catch (IOException e) {
        	JOptionPane.showMessageDialog(new JFrame(), "Error launching Cytoscape", "Webstart Could not launch properly", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    /**
     * @param organism
     * @return
     */
    private static String createGenomeBrowserJNLP(String organism) {
    	String xml = 	"";
    	//TODO Put in species-specific organism arguments
        String args = 	"    <argument>-d</argument>\n" +
        				"    <argument>http://gaggle.systemsbiology.net/projects/halo/2007-04/genomebrowser/data/halo_tiling.hbgb</argument>\n";
		try {
			String thisLine;
			InputStream in = TMEV.class.getClassLoader().getResourceAsStream("org/tigr/microarray/mev/cluster/gui/helpers/genomebrowser_jnlp_template.txt");
        	BufferedReader br = new BufferedReader(new InputStreamReader(in));
        	while ((thisLine = br.readLine()) != null) {
        		xml += thisLine + "\n";
            }
        	xml = xml.replaceFirst("@args@", args);
		} catch (IOException ioe) {
			ShowThrowableDialog.show(new JFrame(), "Could not launch Genome Browser", true, ShowThrowableDialog.ERROR, ioe, 
					"Could not launch the Genome Browser. Please try launching it manually.");
		} catch (NullPointerException npe) {
			ShowThrowableDialog.show(new JFrame(), "Could not launch Genome Browser", true, ShowThrowableDialog.ERROR, npe, 
					"Could not launch the Genome Browser. Please try launching it manually.");
		}
    	return xml;
    } 
    public static boolean isCompleteChrLocation(String chrlocation) {
    	if(chrlocation.startsWith("chrNA"))
    		return false;
    	if(Pattern.matches("^chr\\w{1,2}:\\d+-\\d+$", chrlocation))
    		return true;
    	return false;
    }
}
