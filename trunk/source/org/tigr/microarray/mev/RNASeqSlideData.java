package org.tigr.microarray.mev;

import java.io.IOException;

import org.tigr.microarray.mev.sampleannotation.SampleAnnotation;

public class RNASeqSlideData extends SlideData implements IRNASeqSlide {
	int librarySize = 0;
	
	public RNASeqSlideData(int rRows, int rColumns, SampleAnnotation sampAnn) {
		super(rRows, rColumns, sampAnn);
	}
	/**
	 * State-saving constructor. 
	 * @param sd
	 * @param librarySize
	 * @throws IOException
	 */
    public RNASeqSlideData(SlideData sd, Integer librarySize) throws IOException {
    	super(sd);
    	this.librarySize = librarySize.intValue();   
    }
    
	public void setCount(int index, int countValue) {
		((RNASeqElement)this.getAllElements().get(index)).setCount(countValue);
	}

	public int getCount(int index) {
		return ((RNASeqElement)this.getAllElements().get(index)).getCount();
	}
	public int getLibrarySize() {
		return librarySize;
	}
	public void setLibrarySize(int size) {
		this.librarySize = size;
	}
}
