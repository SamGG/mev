package org.tigr.microarray.mev;

import org.tigr.microarray.mev.annotation.MevAnnotation;

public class RNASeqElement extends SlideDataElement {
	/** Class Code **/
	private String classcode;


	/** length of transcript **/ 
	private int transcriptLength;
	
	/** raw count of transcripts **/
	private int count;
	
	// Raktim added constructor
	public RNASeqElement(String valueOf, int[] rows, int[] columns, float[] fs,
			String[] moreFields, MevAnnotation mevAnno) {
		super(valueOf, rows, columns, fs, moreFields, mevAnno);
	}
	/**
	 * State-saving constructor
	 * @param sde
	 */
	public RNASeqElement(SlideDataElement sde, String classcode, int transcriptLength, int count) {
		super(sde);
		setClasscode(classcode);
		setTranscriptLength(transcriptLength);
		setCount(count);
	}
	
	public int getTranscriptLength() {
		return transcriptLength;
	}
	public int getCount() {
		return count;
	}
	public void setTranscriptLength(int transcriptLength) {
		this.transcriptLength = transcriptLength;
	}
	public void setCount(int count) {
		this.count = count;
	}

	public String getClasscode() {
		return classcode;
	}
	public void setClasscode(String classcode) {
		this.classcode = classcode;
	}
	
}
