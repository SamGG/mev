/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
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

