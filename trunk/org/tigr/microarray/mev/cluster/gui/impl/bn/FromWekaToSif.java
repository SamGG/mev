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
/* FromWekaToSif.java
 * Copyright (C) 2006 Amira Djebbari
 */
package org.tigr.microarray.mev.cluster.gui.impl.bn;
import java.io.*;
public class FromWekaToSif {
    // Pre: Name of input file from WEKA containing network structure
    // in the format: variable: parent1,...,parentn
    //      PrintWriter where to write the network in directed SIF format: node1 pd node2
    // Post: Read a network structure from WEKA output from the given input file name and writes the network in SIF format to the give output PrintWriter
    public static void fromWekaToSif(String evalStr, PrintWriter pw){
	    String[] evalSubstrings = evalStr.split("\n");
	    String s = null;
	    // Process lines after reading Network Structure and before reading LogScore
	    boolean toProcess = false;
	    for(int i = 0; i < evalSubstrings.length; i++){
		s = evalSubstrings[i];
		s = s.trim();
		if(s.startsWith("LogScore")){
		    toProcess = false;
		}
		if(toProcess){
		    fromWekaToSifOneLine(s, pw);
		}
		if(s.startsWith("Network structure")){
		    toProcess = true;
		}
	    }
    }
    // Pre: String containing variable: parent1,...,parentn
    //      PrintWriter where to write the network in directed SIF format: node1 pd node2
    // Post: Reads a variable parent1,...,parentn String and writes in directed SIF format node1 pd node2 to the PrintWriter
    public static void fromWekaToSifOneLine(String s, PrintWriter pw){			if(s.endsWith(":")){
	    return;
	}
	int colon = s.indexOf("(");
	String to = s.substring(0,colon).trim();
	int startIndex = s.indexOf("): ")+3;
	int index = 0;
	String from;
	while(index != s.lastIndexOf(" ")){
	    index = s.indexOf(" ", startIndex+1);
	    if(index != -1){
		from = s.substring(startIndex,index).trim();
		if(!from.equals("CLASS") || !to.equals("CLASS")){
		    pw.println(from + " pd "+ to);
		}
		startIndex = index;	    
	    }	
	    else {
		break;
	    }
	}
	from = s.substring(startIndex, s.length()).trim();
	if(!from.equals("CLASS") || !to.equals("CLASS")){
	    pw.println(from + " pd "+ to);
	}
    }
    
    // For testing
    public static void main(String[] argv){
	if(argv.length != 2){
	    System.out.println("Usage: java FromWekaToSif inWekaFileName outSifFileName\nExample: java FromWekaToSif testFromWekaToSif.weka testFromWekaToSif.sif");
	    System.exit(0);
	}
	try {	    
	    FileOutputStream fos = new FileOutputStream(argv[1]);
	    PrintWriter pw = new PrintWriter(fos, true);
	    fromWekaToSif(argv[0], pw);
	}
	catch(FileNotFoundException fnfe){
	    System.out.println(fnfe);
	}
    }
}

	    
	  

