/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: SAXResponseHandler.java,v $
 * $Revision: 1.2 $
 * $Date: 2005-02-24 20:24:08 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.remote.protocol.parser;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.tigr.microarray.mev.cluster.algorithm.AlgorithmEvent;
import org.tigr.remote.protocol.ExecutedJob;
import org.tigr.remote.protocol.Fail;
import org.tigr.remote.protocol.FailedJob;
import org.tigr.remote.protocol.FinishedJob;
import org.tigr.remote.protocol.JobData;
import org.tigr.remote.protocol.Response;
import org.tigr.remote.protocol.SuccessfulJob;
import org.tigr.remote.protocol.util.TempFile;
import org.tigr.util.ConfMap;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.mnl.java.io.base64.Base64InputStream;

class SAXResponseHandler extends ResponseHandlerBase {

    // General data
    private FinishedJob m_job = null;
    private Response m_result = new Response( null );
    private boolean m_inJobData = false;

    // successful-job data
    private Writer m_mageStorage = null;
    private String m_mageFileName;
    // failed job data
    private Fail m_fail;
    // executed job data
    private AlgorithmEvent m_event;
    // characters section buffer
    private StringBuffer m_chars = new StringBuffer();

    /**
     * Constructs a <code>SAXResponseHandler</code>
     */
    public SAXResponseHandler(ConfMap cfg) {
        super( cfg );
    }

    /**
     * Document if finished.
     */
    public void endDocument() {
        m_job = null;
        m_mageStorage = null;
        m_mageFileName = null;
        m_fail = null;
        m_event = null;
        m_chars = null;
    }

    /**
     * An element is started.
     */
    public void startElement(String uri, String localName, String name, Attributes attrs) throws SAXException {
        super.startElement(uri, localName, name, attrs );
        try {
            m_chars = new StringBuffer();
            // Root element
            if (name.equals("response" )) {
                // do the response processing
            } else
                /* Job Types */
                if (name.equals("successful-job" )) {
                String[] path = {"response","successful-job"};
                m_path.checkFromTopThrow( path );
                String jobId = attrs.getValue("id");
                m_job = new SuccessfulJob( jobId, new JobData( null ) );
                m_result = new Response( m_job );
            } else
                if (name.equals("failed-job" )) {
                String[] path = {"response","failed-job"};
                m_path.checkFromTopThrow( path );
                String jobId = attrs.getValue("id");
                m_job = new FailedJob( jobId, null );
                m_result = new Response( m_job );
            } else
                if (name.equals("executed-job" )) {
                String[] path = {"response","executed-job"};
                m_path.checkFromTopThrow( path );
                String jobId = attrs.getValue("id");
                m_job = new ExecutedJob( jobId, null );
                m_result = new Response( m_job );
            } else
                /* SuccessfulJob descendants */
                if (name.equals("job-data" )) {
                String[] path = {"response","successful-job","job-data"};
                m_path.checkFromTopThrow( path );

                TempFile tmp = new TempFile( m_config );
                m_mageFileName = tmp.getName("MAGE.base64");
                m_mageStorage = new BufferedWriter( new OutputStreamWriter( new FileOutputStream( m_mageFileName ) ) );
            } else
                /* FailedJob descendants */
                if (name.equals("fail")) {
                String[] path = {"response","failed-job","fail"};
                m_path.checkFromTopThrow( path );

                String id = attrs.getValue("id");
                m_fail = new Fail( id, null );
                ((FailedJob)m_job).setFail(  m_fail );
            } else
                /* ExecutedJob descendants */
                if (name.equals("event")) {
                String[] path = {"response","executed-job","event"};
                m_path.checkFromTopThrow( path );

                String id = attrs.getValue("id");
                int i = 0;
                try {
                    i = Integer.parseInt( id );
                } catch (NumberFormatException ex) {
                }
                m_event = new AlgorithmEvent( this, i );
                ((ExecutedJob)m_job).setEvent( m_event );
            } else
                if (name.equals("progress")) {
                String[] path = {"response","executed-job","event","progress"};
                m_path.checkFromTopThrow( path );

                // do the progress processing
            } else
                if (name.equals("float-value")) {
                String[] path = {"response","executed-job","event","float-value"};
                m_path.checkFromTopThrow( path );

                // do the float value processing
            } else
                if (name.equals("description")) {
                String[] path = {"response","executed-job","event","description"};
                m_path.checkFromTopThrow( path );

                // do the description processing
            } else throw new Exception("Unexpected element: " + name );
        } catch (Exception ex) {
            ex.printStackTrace( System.out );
            processError( ex );
        }
    }

    /**
     * An element is finished.
     */
    public void endElement(String uri, String localName, String name) throws SAXException {
        try {
            /* FailedJob descendants */
            if (name.equals("fail")) {
                m_fail.setDescription( m_chars.toString() );
            }
            /* SuccessfulJob descendants */
            else
                if (name.equals("job-data")) {
                boolean keepFiles = m_config.getBoolean("remote.debug.keep-response-files", false);
                m_mageStorage.close();
                m_mageStorage = null;
                TempFile tmp = new TempFile( m_config );
                String fname = tmp.getName("MAGE");
                InputStream in = new Base64InputStream(new BufferedInputStream(new FileInputStream(m_mageFileName)));
                OutputStream out = new BufferedOutputStream(new FileOutputStream(fname));
                byte[] b = new byte[1024*100];
                int cnt;
                while ((cnt = in.read (b)) >= 0) {
                    out.write(b, 0, cnt);
                }
                in.close();
                out.close();
                in = null;
                out = null;
                if (! keepFiles)
                    new File( m_mageFileName ).delete();
                XMLMAGEParser parser = new XMLMAGEParser( m_config );
                parser.parse( fname );
                ((SuccessfulJob)m_job).getData().setData(  parser.getResult()  );
                if (! keepFiles)
                    new File( fname ).delete();
            }
            /* ExecutedJob descendants */
            else
                if (name.equals("progress")) {
                String str = m_chars.toString();
                try {
                    int i = Integer.parseInt( str );
                    m_event.setIntValue( i );
                } catch (NumberFormatException ex) {
                    throw new Exception("invalid progress integer value: " + str );
                }
            } else
                if (name.equals("float-value")) {
                String str = m_chars.toString();
                try {
                    float i = Float.parseFloat( str );
                    m_event.setFloatValue( i );
                } catch (NumberFormatException ex) {
                    throw new Exception("invalid float value: " + str );
                }
            } else
                if (name.equals("description")) {
                m_event.setDescription( m_chars.toString() );
            }
        } catch (Exception ex) {
            processError(ex);
        } finally {
            super.endElement( uri, localName, name );
        }
    }

    /**
     * Invoked to handle a chunk of characters.
     */
    public void characters(char ch[], int start, int length) throws SAXException {
        try {
            if (getCurrentElement().equals("job-data")) {
                m_mageStorage.write( ch, start, length );
            } else {
                m_chars.append( ch, start, length );
            }
        } catch (Exception ex) {
            processError( ex );
        }
    }

    /**
     * Ignore the ignorable.
     */
    public void ignorableWhitespace(char ch[], int start, int length) {}

    /**
     * Returns the build result.
     */
    public Response getResponse() { return m_result;}

}

