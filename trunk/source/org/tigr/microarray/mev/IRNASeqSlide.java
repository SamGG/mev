package org.tigr.microarray.mev;

public interface IRNASeqSlide extends ISlideData {

	public void setCount(int index, int countValue);
	public int getCount(int index);
	public int getLibrarySize();
	public void setLibrarySize(int size);
}
