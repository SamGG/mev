/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: Communicator.java,v $
 * $Revision: 1.4 $
 * $Date: 2006-02-23 21:00:01 $
 * $Author: caliente $
 * $State: Exp $
 */
package org.tigr.remote.protocol.communication.http;

import org.tigr.remote.RemoteException;
import org.tigr.remote.communication.ClientCommunicator;
import org.tigr.remote.communication.JobControl;
import org.tigr.remote.protocol.FinishedJob;
import org.tigr.remote.protocol.Request;
import org.tigr.remote.protocol.Response;
import org.tigr.remote.protocol.StartingJob;
import org.tigr.remote.protocol.StopJob;
import org.tigr.remote.protocol.communication.Presentation;
import org.tigr.util.ConfMap;

public class Communicator implements ClientCommunicator {

    private ConfMap m_conf;
    private Presentation m_presentation;
    private int i = 0;

    /**
     * Constructs a <code>Communicator</code> with specified configuration.
     */
    public Communicator(ConfMap conf) throws RemoteException {
        m_conf = conf;
        m_presentation = new Presentation( conf,  new HttpTransport( conf ) );
    }

    /**
     * Starts a remote execution.
     * @return the JobControl of a started job.
     */
    public JobControl postJob( StartingJob job ) throws RemoteException {
        Request req = new Request( job );
        m_presentation.sendRequest( req );
        return new SyncJobControl( this, job.getId(), m_conf.getInt("remote.polling", 10) );
    }

    /**
     * Returns result of a remote execution.
     */
    protected FinishedJob getResult() throws RemoteException {
        Response response = m_presentation.getResponse();
        return response.getJob();
    }

    /**
     * Returns unique identifier for a new job.
     */
    public String getNewJobId() { return ++i + "";}

    /**
     * Class to control synchronized communication.
     */
    private class SyncJobControl implements JobControl {

        private Communicator m_comm;
        private String m_jobId;
        private int m_polling;
        private FinishedJob m_result = null;

        /**
         * Constructs a <code>SyncJobControl</code> for specified communicator,
         * id and polling parameter. 
         * @param polling the polling interval in seconds.
         */
        public SyncJobControl( Communicator comm, String id, int polling ) {
            m_comm = comm;
            m_jobId = id;
            m_polling = polling;
        }

        /**
         * Waiting for a server reply.
         */
        private void waitForReply() throws RemoteException {
            while (m_result == null) {
                try {
                    Thread.sleep( m_polling*1000 );
                } catch (InterruptedException e) {
                    throw new RemoteException("Thread was interrupted", e);
                }
                m_result = m_comm.getResult();
            }
        }

        /**
         * Sends an instance of <code>StopJob</code> to a server.
         */
        public void terminate() throws RemoteException {
            m_comm.postJob( new StopJob( getJobId() ) );
        }

        /**
         * Returns the result of remote execution.
         */
        public FinishedJob getResult() throws RemoteException {
            waitForReply();
            if (m_result == null)
                throw new RemoteException("No result available");
            FinishedJob result = m_result;
            m_result = null;
            return result;
        }

        /**
         * Returns job unique identifier.
         */
        private String getJobId() { return m_jobId;}
    }
}

