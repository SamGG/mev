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
	
	AnnoAttributeObj(String attribName, String[] attribVals){
		attribCount = attribVals.length;
		this.attribName = attribName;
		attribValue = attribVals;
	}
	
	public void setAttribute(String[] attribs) {
		attribValue = attribs;
	}
	
	public int getAttribCount(){
		return attribCount;
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
