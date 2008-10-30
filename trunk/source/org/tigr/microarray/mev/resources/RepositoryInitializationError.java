package org.tigr.microarray.mev.resources;

public class RepositoryInitializationError extends Exception {
	public RepositoryInitializationError(String msg) {
		super(msg);
	}
	public RepositoryInitializationError(Exception e) {
		super(e);
	}
} 
