/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/**
 * 
 */
package org.tigr.microarray.mev.annotation;

import java.util.Arrays;

import org.tigr.microarray.mev.file.StringSplitter;

/**
 * @author Raktim
 *
 */
public class AnnoAttributeObj {

	int attribCount;
	String attribName;
	String[] attribValue;
	
	AnnoAttributeObj(){
		attribCount = -1;
		attribName = "";
	}
	
	public AnnoAttributeObj(String attribName, String[] attribVals){
		setAttribute(attribVals);
		this.attribName = attribName;
	}
	
	public void setAttribute(String[] attribs) {
		if(attribs[0].contains("///")) {
			String[] newvals = attribs[0].split("///");
			attribs = newvals;
		}
		attribValue = attribs;
		attribCount = attribValue.length;
	}
	
	public int getAttribCount(){
		return attribCount;
	}
	
	public String getAttribName() {
		return attribName;
	}
	
		
	
	public Object getAttributeAt(int index){
		if (index <= attribCount-1)
			return attribValue[index];
		else 
			return null;
	}
	
	
	
	
	public String toString(){
		String _temp = new String();
		String delim = " | ";
		for(int i=0; i < attribCount; i++){
			_temp += attribValue[i];
			if ((i < attribCount-1)){
				_temp = _temp + delim;
			}
		}
		return _temp;
	}
}
