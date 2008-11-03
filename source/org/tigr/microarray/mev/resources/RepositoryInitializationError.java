/*******************************************************************************
 * Copyright (c) 1999-2005, The Institute for Genomic Research (TIGR).
 * Copyright (c) 1999, 2008, the Dana-Farber Cancer Institute (DFCI), 
 * the J. Craig Science Foundataion (JCVI), and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
package org.tigr.microarray.mev.resources;

public class RepositoryInitializationError extends Exception {
	public RepositoryInitializationError(String msg) {
		super(msg);
	}
	public RepositoryInitializationError(Exception e) {
		super(e);
	}
} 
