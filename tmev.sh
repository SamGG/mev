#!/bin/sh
#***********************************************************************
#
# Copyright @ 1999-2007, Dana-Farber Cancer Institute (DFCI).
# All rights reserved.
#
# This software is OSI Certified Open Source Software.
# OSI Certified is a certification mark of the Open Source Initiative.
#
#***********************************************************************
#
# $RCSfile: tmev.sh,v $
# $Revision: 1.1 $
# $Date: 2007-12-12 21:52:48 $
# $Author: eleanorahowe $
# $State: Exp $
#
#***********************************************************************

java -Xss1M -Xmx512m -cp "lib/*" org.tigr.microarray.mev.TMEV