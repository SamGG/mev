/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: JobControl.java,v $
 * $Revision: 1.2 $
 * $Date: 2005-02-24 20:24:11 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.remote.communication;

import org.tigr.remote.protocol.FinishedJob;
import org.tigr.remote.RemoteException;

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

