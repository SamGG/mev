/*
 * Created on Nov 12, 2005
 *
 * 
 */
package org.tigr.microarray.mev.persistence;

import java.beans.Beans;
import java.util.Date;

import org.tigr.microarray.mev.TMEV;

/**
 * @author eleanora
 *
 * This class stores MultipleArrayViewer session-specific data.  Used for
 * versioning of state-saving data.
 */
public class SessionMetaData extends Beans {

	Date startDate;
	Date saveDate;
	String JREVersion;
	String JVMVersion;
	int mevMajorVersion;
	int mevMinorVersion;
	int mevMicroVersion;
	MEVSessionPrefs mevSessionPrefs;
	boolean isBeta;
	

	public SessionMetaData(){
		if(startDate == null)
			startDate = new Date();
		mevSessionPrefs = new MEVSessionPrefs();
	}
	
	/**
	 * @return Returns the startDate.
	 */
	public Date getStartDate() {
		return startDate;
	}
	/**
	 * @return Returns the isBeta.
	 */
	public boolean isBeta() {
		return isBeta;
	}
	/**
	 * @param isBeta The isBeta to set.
	 */
	public void setBeta(boolean isBeta) {
		this.isBeta = isBeta;
	}
	/**
	 * @return Returns the mevMajorVersion.
	 */
	public int getMevMajorVersion() {
		return mevMajorVersion;
	}
	/**
	 * @param mevMajorVersion The mevMajorVersion to set.
	 */
	public void setMevMajorVersion(int mevMajorVersion) {
		this.mevMajorVersion = mevMajorVersion;
	}
	/**
	 * @return Returns the mevMicroVersion.
	 */
	public int getMevMicroVersion() {
		return mevMicroVersion;
	}
	/**
	 * @param mevMicroVersion The mevMicroVersion to set.
	 */
	public void setMevMicroVersion(int mevMicroVersion) {
		this.mevMicroVersion = mevMicroVersion;
	}
	/**
	 * @return Returns the mevMinorVersion.
	 */
	public int getMevMinorVersion() {
		return mevMinorVersion;
	}
	/**
	 * @param mevMinorVersion The mevMinorVersion to set.
	 */
	public void setMevMinorVersion(int mevMinorVersion) {
		this.mevMinorVersion = mevMinorVersion;
	}
	
	public void setDate(Date date){
		this.saveDate = date;
	}
	/**
	 * @return Returns the jREVersion.
	 */
	public String getJREVersion() {
		return JREVersion;
	}
	/**
	 * @param version The jREVersion to set.
	 */
	public void setJREVersion(String version) {
		JREVersion = version;
	}
	/**
	 * @return Returns the jVMVersion.
	 */
	public String getJVMVersion() {
		return JVMVersion;
	}
	/**
	 * @param version The jVMVersion to set.
	 */
	public void setJVMVersion(String version) {
		JVMVersion = version;
	}
	/**
	 * @return Returns the mevVersion.
	 */
	public String getMevVersion() {
		return mevMajorVersion + "." + mevMinorVersion + mevMicroVersion;
	}
	/**
	 * @return Returns the saveDate.
	 */
	public Date getSaveDate() {
		return saveDate;
	}
	/**
	 * @param saveDate The saveDate to set.
	 */
	public void setSaveDate(Date saveDate) {
		this.saveDate = saveDate;
	}
	/**
	 * @return Returns the msp.
	 */
	public MEVSessionPrefs getMevSessionPrefs() {
		return mevSessionPrefs;
	}
	/**
	 * @param msp The msp to set.
	 */
	public void setMevSessionPrefs(MEVSessionPrefs msp) {
		this.mevSessionPrefs = msp;
	}
	/**
	 * @param startDate The startDate to set.
	 */
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
}
