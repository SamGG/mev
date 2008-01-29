/* This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
/* FixStates.java
 * Copyright (C) 2005 Amira Djebbari
 */
//package edu.harvard.dfci.compbio.bnUsingLit.prepareArrayData.bootstrap;
package org.tigr.microarray.mev.cluster.gui.impl.bn;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * The class <code>FixStates</code> is given an input file name in ARFF format 
 * checks and makes sure that every attribute has 3 states associated with it
 *
 * @author <a href="mailto:amirad@jimmy.harvard.edu"></a>
 */
public class FixStates {
    /**
     * The <code>fixStates</code> method is given an input file name containing data in ARFF format
     * checks and fixes any problem with states associated with attributes  
     * such that every attribute has bin labels associated with it
     * (example: state1, state2, state3)
     * @param inFileName a <code>String</code> denoting the name of the input file containing data in ARFF format
     * @param binLabelsToLookFor an <code>ArrayList</code> corresponding to the the bin labels that will be associated 
     * with each attribute if it's not already the case.
     * @param extension a <code>String</code> denoting the desired extension of the output file
     */
    public static void fixStates(String inFileName, ArrayList binLabelsToLookFor, String extension){
		try {
		    FileReader fr = new FileReader(inFileName);
		    LineNumberReader lnr = new LineNumberReader(fr);
		    String s = null;
		    FileOutputStream fos = new FileOutputStream(inFileName+extension);
		    PrintWriter pw = new PrintWriter(fos,true);
		    String binLabelsToLookForStr = "{";
		    for(int i = 0; i < binLabelsToLookFor.size()-1; i++){
			binLabelsToLookForStr += (String) binLabelsToLookFor.get(i)+ ",";
		    }
		    binLabelsToLookForStr += (String) binLabelsToLookFor.get(binLabelsToLookFor.size()-1)+ "}";
		    while((s = lnr.readLine())!=null){
			s = s.trim();
			if(s.startsWith("@attribute") && !s.startsWith("@attribute CLASS") && !s.endsWith(binLabelsToLookForStr)){
			    s = s.substring(0,s.indexOf("{"))+" "+binLabelsToLookForStr;
			}
			pw.println(s);
		    }
		}
		catch(IOException ioe){
		    System.out.println(ioe);
		}
    }


    /**
     * The <code>test</code> method tests the <code>fixStates</code> method
     *
     * @param inRootFileName a <code>String</code> corresponding the root name of the input files containing
     * data in ARFF format. 
     * For example, the name of the file for iteration 0 should be inRootFileName0.arff
     * @param numIterations an <code>int</code> corresponding the the number of iterations for which to fix the states.
     * @param binLabelsToLookFor an <code>ArrayList</code> corresponding to the the bin labels that will be associated 
     * with each attribute if it's not already the case.
     */
    public static void test(String inRootFileName, int numIterations, ArrayList binLabelsToLookFor){
		for(int i = 0 ; i < numIterations; i++){
		    fixStates(inRootFileName+i, binLabelsToLookFor, ".arff");
		}
    }

    /**
     * The <code>usage</code> method displays the usage.
     *
     */
    public static void usage(){
		System.out.println("Usage: java FixStates inArffRootFileName numIterations\nExample: java FixStates boot/myArffRootFileName 100");
		System.exit(0);
    }

    public static void main(String[] argv){
		if(argv.length != 2){
		    usage();
		}	    
		String inRootFileName = argv[0];
		int numIterations = Integer.parseInt(argv[1]);
		ArrayList binLabelsToLookFor = new ArrayList();
		binLabelsToLookFor.add("state1");
		binLabelsToLookFor.add("state2");
		binLabelsToLookFor.add("state3");
		test(inRootFileName, numIterations, binLabelsToLookFor);
    }
}
