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
Set JAVA_HOME **Only 32 Bit JVM is not default.
Assumes you have already installed 32 Bit JRE following instructions at 
MeV SourceForge forum.
This not required on a 32 Bit Windows machine and only required if your
64 Bit Windows machine does not have a default 32 bit JVM.
NOTE: This does not alter any of your system settings. All changes are 
local to this file/environemnt
***********************************************************************
:CMD
if not exist "C:\Program Files (x86)\Java\jre6" echo "32 bit Win"
if exist "C:\Program Files (x86)\Java\jre6" echo "64 bit Win"
if exist "C:\Program Files (x86)\Java\jre6" set JAVA_HOME="C:\Program Files (x86)\Java\jre6"
if exist "C:\Program Files (x86)\Java\jre6" set PATH=%JAVA_HOME%;%JAVA_HOME%\bin;%PATH%

set CurrDIR=%cd%
echo %CurrDIR%

REM Set RHOME
set R_HOME=%CurrDIR%\R-2.11.1

REM For R Dlls
set PATH=%PATH%;%CurrDIR%\R-2.11.1\bin;%CurrDIR%\R-2.11.1\lib

set ClassPath=lib/*;
java -Djava.library.path=lib -Xss1M -Xmx768m -cp %ClassPath% org.tigr.microarray.mev.TMEV