/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
package org.tigr.util.awt;

import java.util.Hashtable;

public class ActionInfoEvent extends java.util.EventObject
	{
	private Hashtable hashtable;
	
	public ActionInfoEvent(Object source, Hashtable hashtable)
		{
		super(source);
		this.hashtable = hashtable;
		}
	
	public Hashtable getHashtable() {return this.hashtable;}
	public Object getValue(Object key) {return hashtable.get(key);}
	}