/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: RemoteAlgorithm.java,v $
 * $Revision: 1.4 $
 * $Date: 2006-02-23 21:00:00 $
 * $Author: caliente $
 * $State: Exp $
 */
package org.tigr.remote;

import org.tigr.microarray.mev.cluster.algorithm.AbstractAlgorithm;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmEvent;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmException;
import org.tigr.remote.communication.ClientCommunicator;
import org.tigr.remote.communication.CommunicatorFactory;
import org.tigr.remote.communication.JobControl;
import org.tigr.remote.protocol.ExecutedJob;
import org.tigr.remote.protocol.FailedJob;
import org.tigr.remote.protocol.FinishedJob;
import org.tigr.remote.protocol.JobData;
import org.tigr.remote.protocol.JobVisitor;
import org.tigr.remote.protocol.StartJob;
import org.tigr.remote.protocol.SuccessfulJob;

public class RemoteAlgorithm extends AbstractAlgorithm {
    
    private String name;
    private JobControl control;
    
    /**
     * Constructs a <code>RemoteAlgorithm</code> with specified
     * algorithm name.
     *
     * @param name the name of an algorithm to be executed.
     */
    public RemoteAlgorithm(String name) {
	this.name = name;
    }
    
    /**
     * Executes the remote algorithm with a specified <code>AlgorithmData</code>.
     * @see AlgorithmData
     * @throws AlgorithmException
     */
    public AlgorithmData execute(AlgorithmData data) throws AlgorithmException {
	try {
	    ClientCommunicator comm = CommunicatorFactory.getCommunicator();
	    StartJob startJob = new StartJob(comm.getNewJobId(), new JobData(data), name);
	    this.control = comm.postJob(startJob);
	    JobExecution exec = new JobExecution(this);
	    FinishedJob finishedJob;
	    while (true) {
		finishedJob = this.control.getResult();
		finishedJob.accept(exec);
		if (exec.getResult() != null) // that means, that SuccessfullJob received, we can exit a loop execution
		    return exec.getResult();
	    }
	} catch (Exception ex) {
	    throw new AlgorithmException(ex);
	}
    }
    
    /**
     * Tried to interrupt remote calculation.
     */
    public void abort() {
	if (control == null)
	    throw new RuntimeException("Not started yet");
	else
	    try {
		control.terminate();
	    } catch (RemoteException ex) {
		throw new RuntimeException("Abort error");
	    }
    }
    
    /**
     *  The class to accept progress or result of the remote execution.
     */
    public class JobExecution implements JobVisitor {
	
	private RemoteAlgorithm ra;
	private AlgorithmData result;
	
	/**
	 * Constructs a <code>JobExecution</code> instance for
	 * the specified remote algorithm.
	 */
	public JobExecution( RemoteAlgorithm ra ) {
	    this.ra = ra;
	}
	
	/**
	 * Returns the result of an algorithm execution.
	 */
	public AlgorithmData getResult() {
	    return result;
	}
	
	/**
	 * Invoked when job is successfully executed.
	 */
	public void visitSuccessfulJob( SuccessfulJob job ) {
	    this.result = job.getData().getData();
	}
	
	/**
	 * Invoked if job is failed.
	 */
	public void visitFailedJob( FailedJob job ) {
	    throw new RuntimeException( "Server error: " + job.getFail().getDescription() );
	}
	
	/**
	 * Invoked for an executed job to update progress value.
	 */
	public void visitExecutedJob( ExecutedJob job ) {
	    AlgorithmEvent ev = job.getEvent();
	    ra.fireValueChanged(new AlgorithmEvent(this, ev.getId(), ev.getIntValue(), ev.getFloatValue(), ev.getDescription()));
	}
    }
}
