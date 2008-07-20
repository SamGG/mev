package org.tigr.microarray.mev.file;

/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: FileTreePaneListener.java,v $
 * $Revision: 1.4 $
 * $Date: 2005-03-10 15:39:38 $
 * $Author: braistedj $
 * $State: Exp $
 */

//package org.tigr.util.awt;

import java.util.EventListener;

public interface FileTreePaneListener extends EventListener {
	public void nodeCollapsed(FileTreePaneEvent event);
	public void nodeExpanded(FileTreePaneEvent event);
	public void nodeSelected(FileTreePaneEvent event);
}
