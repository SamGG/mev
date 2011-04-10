package org.tigr.microarray.mev;

import org.tigr.microarray.mev.cluster.ClusterWrapper;
import org.tigr.microarray.mev.sampleannotation.SampleAnnotation;

public class RNASeqFloatSlideData extends FloatSlideData implements IRNASeqSlide {

	protected int[] counts;
	protected int librarySize;
	

	public RNASeqFloatSlideData(ISlideMetaData slideMetaData, int[] counts) {
		super(slideMetaData);
		this.counts = counts;
	}
	public RNASeqFloatSlideData(ISlideMetaData slideMetaData, SampleAnnotation sampAnn, int[] counts) {
		super(slideMetaData, sampAnn);
		this.counts = counts;
	}

	// Raktim added constructor
	public RNASeqFloatSlideData(ISlideMetaData slideMetaData, int spotCount, SampleAnnotation sampAnn) {
		super(slideMetaData, spotCount, sampAnn);
		this.counts = new int[spotCount];
	}
	
	/**
	 * State-saving constructor. 
	 * @param fsd
	 * @param countsWrapper
	 * @param librarySize
	 */
	public RNASeqFloatSlideData(FloatSlideData fsd, ClusterWrapper countsWrapper, Integer librarySize) {
		super(fsd);
		this.counts = countsWrapper.getClusters()[0];
		this.librarySize = librarySize.intValue();
	}
	
	public int[] getCounts() {
		return counts;
	}		 
	public int getCount(int i) {
		return counts[i];
	}
	public void setCount(int index, int countValue) {
		this.counts[index] = countValue;
	}
	public int getLibrarySize() {
		return librarySize;
	}
	public void setLibrarySize(int size) {
		this.librarySize = size;
		
	}
}
