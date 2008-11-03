/*******************************************************************************
 * Copyright (c) 1999-2005, The Institute for Genomic Research (TIGR).
 * Copyright (c) 1999, 2008, the Dana-Farber Cancer Institute (DFCI), 
 * the J. Craig Science Foundataion (JCVI), and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
package org.tigr.microarray.mev.resources;

import java.io.File;
import java.util.Collection;
import java.util.Hashtable;

public interface IResourceManager {
	public File getSupportFile(ISupportFileDefinition def, boolean getOnline) throws SupportFileAccessError;
	public Hashtable<ISupportFileDefinition, File> getSupportFiles(Collection<ISupportFileDefinition> defs, boolean getOnline) throws SupportFileAccessError;
	public Hashtable<ISupportFileDefinition, File> getMultipleSupportFiles(IMultiSupportFileDefinition def) throws SupportFileAccessError;
	public boolean fileIsInRepository(ISupportFileDefinition def);
	public boolean checkForUpdate(ISupportFileDefinition def);
	public void setAskToGetOnline(boolean b);
}
