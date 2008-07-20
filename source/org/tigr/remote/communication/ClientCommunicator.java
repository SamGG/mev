/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: ClientCommunicator.java,v $
 * $Revision: 1.3 $
 * $Date: 2005-03-10 15:28:24 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.remote.communication;

import org.tigr.remote.RemoteException;
import org.tigr.remote.protocol.StartingJob;

public interface ClientCommunicator {
    /**
     * Starts a remote execution.
     * @return the JobControl of a started job.
     */
    public JobControl postJob(StartingJob job) throws RemoteException;
    
    /**
     * Returns unique identifier of a started job.
     */
    public String getNewJobId();
}

