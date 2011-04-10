package org.tigr.microarray.mev;

import org.tigr.microarray.mev.annotation.IChipAnnotation;
import org.tigr.microarray.mev.annotation.MevChipAnnotation;

public class RNASeqChipAnnotation extends MevChipAnnotation implements IChipAnnotation {
	boolean isRPKM = false;
	boolean isFPKM = false;
	String referenceGenome = null;
	String referenceGenomeBuild = null;
	int readLength = 0;
	
	public RNASeqChipAnnotation(){
		super();
	}

	public boolean isRPKM() {
		return isRPKM;
	}
	public void setRPKM(boolean isRPKM) {
		this.isRPKM = isRPKM;
	}
	public boolean isFPKM() {
		return isFPKM;
	}
	public void setFPKM(boolean isFPKM) {
		this.isFPKM = isFPKM;
	}
	public String getReferenceGenome() {
		return referenceGenome;
	}
	public void setReferenceGenome(String referenceGenome) {
		this.referenceGenome = referenceGenome;
	}
	public String getReferenceGenomeBuild() {
		return referenceGenomeBuild;
	}
	public void setReferenceGenomeBuild(String referenceGenomeBuild) {
		this.referenceGenomeBuild = referenceGenomeBuild;
	}
	public int getReadLength() {
		return readLength;
	}
	public void setReadLength(int readLength) {
		this.readLength = readLength;
	}
	
	
}
