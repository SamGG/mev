#!/bin/sh
#***********************************************************************
#
# Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
# Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
# J. Craig Venter Institute (JCVI) and the University of Washington.
# All rights reserved.
#
# This software is OSI Certified Open Source Software.
# OSI Certified is a certification mark of the Open Source Initiative.
#
#***********************************************************************
#
# $RCSfile: tmev.sh,v $
# $Revision: 1.1 $
# $Date: 2007/12/12 21:52:48 $
# $Author: eleanorahowe $
# $State: Exp $
#
#***********************************************************************
for jar in lib/*.jar 
do 
# make sure CLASSPATH is defined before we reference it 
if [ -z "$CLASSPATH" ] 
then 
CLASSPATH=.:$jar 
else 
CLASSPATH=$jar:$CLASSPATH 
fi 
done 
export CLASSPATH

#**************************************
# R specific variables & compilations #
#**************************************
CurrDIR=`pwd`
echo ${CurrDIR}

# Try to auto detect libjri.so file, if not found then try compiling. 
# This ensures the same libs are not compiled every time MeV is run
if [ -a ${CurrDIR}/lib/libjri.so ]
   then
     echo "${CurrDIR}/lib/libjri.so exists"
   else 
     echo "Attempting to build jri Library"
	 ./makejrilib.sh
         if [ $? -ne 0 ]
         then
           echo "ERROR generating jri library...Aborting"
           exit 1;
         fi
fi

# Set RHOME etc 
R_HOME=/usr/lib/R
R_SHARE_DIR=/usr/share/R
export R_SHARE_DIR
R_INCLUDE_DIR=/usr/include/R
export R_INCLUDE_DIR

# For R shared libs
PATH=${PATH}:${R_HOME}/bin:${R_HOME}/lib
JRI_LD_PATH=${CurrDIR}/lib:${R_HOME}/lib
if test -z "$LD_LIBRARY_PATH"; then
  LD_LIBRARY_PATH=$JRI_LD_PATH
else
  LD_LIBRARY_PATH=$JRI_LD_PATH:$LD_LIBRARY_PATH
fi
export R_HOME
# echo R HOME: ${R_HOME}
export LD_LIBRARY_PATH
# echo LD LIBRARY PATH: ${LD_LIBRARY_PATH}
export PATH

java -Djava.library.path=lib -Xss1M -Xmx1024m -cp $CLASSPATH org.tigr.microarray.mev.TMEV
