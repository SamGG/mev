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