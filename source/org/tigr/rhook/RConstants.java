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
	
	public static int WINDOWS_OS = 1;
	public static int LINUX_OS = 2;
	public static int MAC_OS = 3;
	public static int UNKNOWN_OS = -1;
	
	public static int OS_ARCH_32 = 1;
	public static int OS_ARCH_64 = 2;
	public static final int UNKNOWN_ARCH = -2;
	
	//Location of R packages
	public static String R_PACKAGE_DIR = "RPackages";
	
	//LIMMA package Names
	public static String LIMMA_WIN = "limma_2.16.5.zip";
	public static String LIMMA_LINUX_32 = "limma_2.18.2.tar.gz";
	public static String LIMMA_LINUX_64 = "limma_2.18.2.tar.gz";
	public static String LIMMA_MAC = "limma_2.18.2.tgz";
}
