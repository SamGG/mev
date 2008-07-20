/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: RequestSerializerImpl.java,v $
 * $Revision: 1.3 $
 * $Date: 2005-03-10 15:35:13 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.remote.protocol.serializer;

import java.io.OutputStream;
import java.io.PrintStream;

import org.tigr.remote.protocol.Request;
import org.tigr.remote.protocol.StartJob;
import org.tigr.remote.protocol.StartingJob;
import org.tigr.remote.protocol.StartingJobVisitor;
import org.tigr.remote.protocol.StopJob;

class RequestSerializerImpl extends RequestSerializer {

    /**
     * Constructs a <code>RequestSerializerImpl</code>.
     */
    public RequestSerializerImpl() {
    }

    /**
     * Serialize a request into specified output stream.
     */
    public void serializeRequest( Request req, OutputStream out ) throws SerializerException {
        PrintStream p = new PrintStream( out );
        XMLIndent ind = new XMLIndent();
        p.println("<?xml version=\"1.0\"?>");
        p.println("<request>");
        ind.inc();
        StartingJobSerializer ser = new StartingJobSerializer( p, ind );
        StartingJob job = req.getJob();
        if (job != null) {
            job.accept( ser );
        }
        ind.dec();
        p.println("</request>");
    }

    /**
     * Start and stop jobs serializer.
     */
    class StartingJobSerializer implements StartingJobVisitor {

        /**
         * Constructs a <code>StartingJobSerializer</code> with specified 
         * print stream.
         */
        public StartingJobSerializer( PrintStream out, XMLIndent ind ) {
            m_out = out;
            m_ind = ind;
        }

        /**
         * Serializes a start job.
         */
        public void visitStartJob( StartJob job ) {
            m_ind.print( m_out );
            m_out.println("<start-job id=\"" + Util.escape( job.getId() ) +
                          "\"  type=\"" + Util.escape( job.getType() ) + "\"   >"  );
            m_ind.inc(); m_ind.print( m_out );
            m_out.println("<job-data>");

            BreakFilterStream bfs = new BreakFilterStream( m_out );
            PrintStream out = new PrintStream( bfs );
            MAGESerializer serializer = new MAGESerializer( out );
            serializer.serialize( job.getData().getData() );
            out.flush();
            out = null;
            m_out.println();
            m_out.println("</job-data>");
            m_ind.dec();
            m_ind.print( m_out );
            m_out.println("</start-job>");
        }

        /**
         * Serializes a stop job.
         */
        public void visitStopJob( StopJob job )  {
            m_ind.print( m_out );
            m_out.println("<stop-job id=\"" + Util.escape( job.getId() ) + "\"/>"  );
        }

        private PrintStream m_out;
        private XMLIndent   m_ind;
    }
}

