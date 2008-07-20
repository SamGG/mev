package org.tigr.microarray.mev.file;

/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: FileTreePaneEvent.java,v $
 * $Revision: 1.4 $
 * $Date: 2005-03-10 15:39:40 $
 * $Author: braistedj $
 * $State: Exp $
 */

//package org.tigr.util.awt;

import java.util.EventObject;
import java.util.Hashtable;

public class FileTreePaneEvent extends EventObject {
	
	private Hashtable hashtable;
	
	public FileTreePaneEvent(Object source, Hashtable hashtable) {
		super(source);
		this.hashtable = hashtable;
	}
	
	public Hashtable getHashtable() {
		return this.hashtable;
	}
	
	public Object getValue(Object key) {
		return hashtable.get(key);
	}
}
