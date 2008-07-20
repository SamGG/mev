/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: JobControl.java,v $
 * $Revision: 1.4 $
 * $Date: 2006-02-23 21:00:00 $
 * $Author: caliente $
 * $State: Exp $
 */
package org.tigr.remote.communication;

import org.tigr.remote.RemoteException;
import org.tigr.remote.protocol.FinishedJob;

public interface JobControl {
    /**
     * Returns the result of remote execution.
     * @see FinishedJob
     */
    public FinishedJob getResult() throws RemoteException;
    
    /**
     * A JobControl should terminate execution process.
     */
    public void terminate() throws RemoteException;
    
}

