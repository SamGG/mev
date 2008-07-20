/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
package org.tigr.microarray.mev.annotation;


public class GO {

	String GOid;
	String description;
	String memeberOfDomain;
	
	public GO(String id, String desc, String membr) {
		GOid = id;
		description  = desc;
		memeberOfDomain = membr;
	}

	public GO(){
		GOid = "";
		description  = "";
		memeberOfDomain = "";
	}
	
	public String getID() {
		return GOid;
	}
	
	public String getDescription() {
		return description;
	}
	
	public String getDomain() {
		return memeberOfDomain;
	}
	
	public void  setID(String id) {
		GOid = id;
	}
	
	public void  getDescription(String desc) {
		description = desc;
	}
	
	public void getDomain(String domain) {
		memeberOfDomain = domain;
	}
}
