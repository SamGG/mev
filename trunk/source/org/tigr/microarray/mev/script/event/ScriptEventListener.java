/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * ScriptEventListener.java
 *
 * Created on March 14, 2004, 12:13 PM
 */

package org.tigr.microarray.mev.script.event;

/**
 *
 * @author  braisted
 */
public interface ScriptEventListener {
    
    public void documentChanged(ScriptDocumentEvent event);

}
