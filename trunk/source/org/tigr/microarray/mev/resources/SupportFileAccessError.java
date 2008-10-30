package org.tigr.microarray.mev.resources;

public class SupportFileAccessError extends Exception {
	boolean cancelledConnection = false;
	boolean internetAccessWasAllowed = true;
	boolean fileNotFound = false;
	
	public SupportFileAccessError(String msg) {
		super(msg);
	}
	public SupportFileAccessError(Throwable t) {
		super(t);
	}	
	public SupportFileAccessError(String msg, Throwable t) {
		super(msg, t);
	}
	public boolean isCancelledConnection() {
		return cancelledConnection;
	}
	public void setCancelledConnection(boolean b) {
		cancelledConnection = b;
	}
	public void setInternetAccessWasAllowed(boolean b) {
		internetAccessWasAllowed = b;
	}
	public boolean isInternetAccessWasAllowed() {
		return internetAccessWasAllowed;
	}
	public boolean isFileNotFound() {
		return fileNotFound;
	}
	public void setFileNotFound(boolean fileNotFound) {
		this.fileNotFound = fileNotFound;
	}
	
}
