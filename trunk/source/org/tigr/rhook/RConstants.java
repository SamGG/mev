/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/

/*
 * RConstants.java
 *
 * Created on Sep 23, 2009, 11:15:47 PM
 * 
 * @author raktim
 */

package org.tigr.rhook;

public class RConstants {
	
	public final static String RHOOK_BASE_URL = "ftp://occams.dfci.harvard.edu/pub/bio/MeV_Etc/R_MeV_Support/";
	public final static String RHOOK_PROP_URL = "ftp://occams.dfci.harvard.edu/pub/bio/MeV_Etc/R_MeV_Support/rhook.txt";
	
	public final static int WINDOWS_OS = 1;
	public final static int LINUX_OS = 2;
	public final static int MAC_OS = 3;
	public final static int UNKNOWN_OS = -1;
	
	public final static int OS_ARCH_32 = 1;
	public final static int OS_ARCH_64 = 2;
	public final static int UNKNOWN_ARCH = -2;
	public final static String PROP_DELIM = ":";
	
	//Location of R packages
	public static String R_PACKAGE_DIR = "RPackages";
		
	public final static String MAC_MEV_RES_LOC = "MeV.app/Contents/Resources/Java";
	public final static String MAC_R_PATH = "/Library/Frameworks/R.framework/Versions/Current";
	
	// property names
	protected final static String PROP_NAME_CUR_R_VER = "cur_r_ver";
	protected final static String PROP_NAME_CUR_MAC_R_VER = "cur_mac_r_ver";
}
