package org.tigr.microarray.mev.sampleannotation;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.Vector;

public class SampleAnnotation implements ISampleAnnotation{
    private Hashtable<String, String> sampleAnn;
    
    
    public SampleAnnotation(){
    	sampleAnn=new Hashtable<String, String>();
    }
    
    public SampleAnnotation(Hashtable<String, String> ann){
    	this.sampleAnn=ann;
    }
    
    
	
	public String getAnnotation(String Key) {
		return sampleAnn.get(Key);
	}

	
	public void setAnnotation(String key, String value) {
		sampleAnn.put(key, value);		
	}
	
	
	public Hashtable<String, String> getSampleAnnoHash(){
		return this.sampleAnn;
	}
	
	public void setSampleAnnoHash(Hashtable<String, String> ann){
		this.sampleAnn=ann;
	}
	
	
	/**
	 * getAnnotationKeys returns a list of available sample annotation keys
	 * The order of keys is important. "Default Slide Name" MUST be the first key.
	 * This is enforced in the function.
	 * 
	 * @return
	 */
	
	public Vector getAnnotationKeys(){
		Vector<String> annKeys=new Vector<String>();
		//Zeroth element will always be the Default Slide Name
		annKeys.add(0, "Default Slide Name");
		Set keys=this.sampleAnn.keySet();
		Iterator it=keys.iterator();
		int index=1;
		while(it.hasNext()){
			String nextElement=(String)it.next();
			
			if(!annKeys.contains(nextElement)){
				annKeys.add(index, nextElement);
			}
			
			
			
		}
		
		return annKeys;
		
	}
	
	
	
	

}
