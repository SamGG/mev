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
