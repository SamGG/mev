package org.tigr.microarray.mev.file;

/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: FileTreePaneEvent.java,v $
 * $Revision: 1.1.1.1 $
 * $Date: 2003-08-21 21:04:24 $
 * $Author: braisted $
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