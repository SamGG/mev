package org.tigr.microarray.mev.file;

/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: FileTreePaneListener.java,v $
 * $Revision: 1.2 $
 * $Date: 2004-02-27 22:16:51 $
 * $Author: braisted $
 * $State: Exp $
 */

//package org.tigr.util.awt;

import java.util.EventListener;

public interface FileTreePaneListener extends EventListener {
	public void nodeCollapsed(FileTreePaneEvent event);
	public void nodeExpanded(FileTreePaneEvent event);
	public void nodeSelected(FileTreePaneEvent event);
}