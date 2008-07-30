@echo off
goto CMD

Modified for MeV v. 4.2, 07.31.2000

***********************************************************************
Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
J. Craig Venter Institute (JCVI) and the University of Washington.
All rights reserved.
***********************************************************************
 $RCSfile: TMEV.bat,v $
 $Revision: 1.1 $
 $Date: 2007-12-12 21:52:48 $
 $Author: eleanorahowe $
 $State: Exp $
***********************************************************************

:CMD

set ClassPath=lib/*;
java -Xss1M -Xmx1024m -cp %ClassPath% org.tigr.microarray.mev.TMEV