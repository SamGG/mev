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
CLASSPATH=$jar 
else 
CLASSPATH=$jar:$CLASSPATH 
fi 
done 
export CLASSPATH

java -Xss1M -Xmx1024m -cp $CLASSPATH org.tigr.microarray.mev.TMEV
