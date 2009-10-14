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

#************************************
#
# R specific variables
#
#************************************
CurrDIR=`pwd`
echo ${CurrDIR}

# Set RHOME
R_HOME=${CurrDIR}/R-2.9.1

R_SHARE_DIR=${CurrDIR}/R-2.9.1/share
export R_SHARE_DIR
R_INCLUDE_DIR=${CurrDIR}/R-2.9.1/include
export R_INCLUDE_DIR

# For R shared libs
PATH=${PATH}:${CurrDIR}/R-2.9.1/bin:${CurrDIR}/R-2.9.1/lib
JRI_LD_PATH=${CurrDIR}/lib:${CurrDIR}/R-2.9.1/lib
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
