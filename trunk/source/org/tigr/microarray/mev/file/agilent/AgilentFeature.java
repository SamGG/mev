/*
 * Created on Jan 22, 2004
 */
package org.tigr.microarray.mev.file.agilent;

/**
 * Base class for all Pattern Features.  Agilent patterns are notoriously inconsistent, but
 * every pattern should at least contain:
 * 
 * column
 * row
 * refnumber
 * sampleid
 * geneid
 * genedescription
 * controltype
 * 
 * All other fields should be dealt with in subclasses.
 * 
 * @author vu
 */
public class AgilentFeature {
	//featureType values
	static final int REPORTER = 1;
	static final int POSITIVE_CONTROL = 2;
	static final int NEGATIVE_CONTROL = 3;
	static final int IGNORE = 4;
	
	//Strings that appear regularly in Agilent Pattern File
	static final String sPos = "pos";
	static final String sNeg = "neg";
	static final String sFalse = "FALSE";
	static final String sIgnore = "ignore";
	static final String sNa = "NA";
	
	private int col;
	private int row;
	private int refNumber;
	private int featureType;
	private String name;
	private String geneName;
	private String sysName;
	private String desc;
	private String controlType;
	
	
	/**
	 * Compares controlType to Feature.strings to see what type of Feature this is.
	 */
	public void assignFeatureType() {
		if(this.getControlType().toLowerCase().equals(AgilentFeature.sPos.toLowerCase())) {
			this.setFeatureType(AgilentFeature.POSITIVE_CONTROL);
		} else if(this.getControlType().toLowerCase().equals(AgilentFeature.sNeg.toLowerCase())) {
			this.setFeatureType(AgilentFeature.NEGATIVE_CONTROL);
		} else if(this.getControlType().toLowerCase().equals(AgilentFeature.sIgnore.toLowerCase())) {
			this.setFeatureType(AgilentFeature.IGNORE);
		} else if(this.getControlType().toLowerCase().equals(AgilentFeature.sFalse.toLowerCase())) {
			this.setFeatureType(AgilentFeature.REPORTER);
		} else {
			//problem
			System.out.println("ProblemWithFeatureType");
			this.setFeatureType(AgilentFeature.REPORTER);
		}
	}

	public void setCol(int col) {
		this.col = col;
	}
	public int getCol() {
		return col;
	}
	public void setRow(int row) {
		this.row = row;
	}
	public int getRow() {
		return row;
	}
	public void setRefNumber(int fNum) {
		this.refNumber = fNum;
	}
	public int getRefNumber() {
		return refNumber;
	}
	public void setName(String internalId) {
		this.name = internalId;
	}
	public String getName() {
		return name;
	}
	public void setGeneName(String geneId) {
		this.geneName = geneId;
	}
	public String getGeneName() {
		return geneName;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public String getDesc() {
		if(this.desc.equals("MOTHRA")) {
			return "";
		} else {
			return desc;
		}
	}
	public void setControlType(String controlType) {
		this.controlType = controlType;
	}
	public String getControlType() {
		return controlType;
	}

	public void setFeatureType(int featureType) {
		this.featureType = featureType;
	}

	public int getFeatureType() {
		return featureType;
	}

	public void setSysName(String sysName) {
		this.sysName = sysName;
	}

	public String getSysName() {
		return sysName;
	}
}
