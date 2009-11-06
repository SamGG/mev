/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
package org.tigr.microarray.mev.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

public class GEOPlatformfileParser {
	private Vector rawLines=new Vector();
	private Vector platformHeaders;
	
	
	public GEOPlatformfileParser() {
		
	}
	
	 public Hashtable parsePlatformData(File targetFile){
		// System.out.println("parsing platform data");
	    	StringSplitter split=new StringSplitter('\t');
	    	String currentLine=null;
	    	BufferedReader reader;
	    	platformHeaders=new Vector();
	    	Hashtable pMatrix=new Hashtable();
	    	
	    	try {
	    	reader=new BufferedReader(new FileReader(targetFile));
	    	currentLine=reader.readLine();
	    	
	    	while((currentLine=reader.readLine())!=null){
	    	
	    		if(currentLine.contains("platform_table_begin")) {
	    			currentLine=reader.readLine();
	    			split.init(currentLine);
	    			rawLines.add(currentLine);
	    			
	    			while(split.hasMoreTokens()) {
	    				platformHeaders.add(split.nextToken());
	    			}
	    			
	    			for(int count=0; (!(currentLine=reader.readLine()).contains("platform_table_end")); count++) {
	    				rawLines.add(currentLine);
	    			}
	    		 
	    			if(currentLine.contains("platform_table_end")) {
	    				 pMatrix=getPlatformMatrix(rawLines);
	    		    	break;
	    			}
	    		}
	
	    	}
	    
	   	}catch(Exception e) {
	    		e.printStackTrace();
	    	}
	   //	System.out.println("platform parsing ends");
	   	
	   
	   	
	    	if(pMatrix.size()!=0)
	    	return pMatrix;
	    	else
	    		return null;
	    }
	    
	    
	   Hashtable getPlatformMatrix(Vector rawLines){
	    	Hashtable pMatrix=new Hashtable();
		    int size=rawLines.size();
		    StringSplitter split=new StringSplitter('\t');
		    
		    for(int i=1; i<size; i++) {
		    	String Line=(String)rawLines.elementAt(i);
		    	split.init(Line);
		    	String key=split.nextToken();
		    	String values="";
		    	
		    	while(split.hasMoreTokens()) {
		    		String temp=split.nextToken();
		    		if(temp!=" "|temp!=null) {
		    		values=values.concat(temp);
		    		values=values.concat(":");
		    		}else {
		    			values=values.concat("NA");
		    		    values=values.concat(":");
		    		}
		    	}
		    	
		    	if(!pMatrix.containsKey(key)) {
		    		pMatrix.put(key, values);
		    	}
		    	
	
		    }
		    
			
		   	
		    if(pMatrix.size()==0) {
		    	pMatrix=null;
		    	return pMatrix;
		    }else
		      	return pMatrix;
	    	
	    }
	    
	    
	
	   public Vector getColumnHeaders() {
		    return platformHeaders;
	   }
	
	
	
	
	
}
