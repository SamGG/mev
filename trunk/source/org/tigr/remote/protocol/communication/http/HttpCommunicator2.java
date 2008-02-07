/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: HttpCommunicator2.java,v $
 * $Revision: 1.4 $
 * $Date: 2005-12-06 16:26:51 $
 * $Author: wwang67 $
 * $State: Exp $
 */
package org.tigr.remote.protocol.communication.http;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;

import org.tigr.remote.protocol.util.TempFile;
import org.tigr.util.ConfMap;

import HTTPClient.CookieModule;
import HTTPClient.HTTPConnection;
import HTTPClient.HttpURLConnection;

public class HttpCommunicator2 {

    private URL sendURL;
    private URL receiveURL;
    private HttpURLConnection connection;
    private BufferedOutputStream bufferedOutputStream;
    private InputStream inputStream;
    private int contentLength = 0;
    private String context;
    private String fileName;
    private boolean fileCreated;
    private boolean keepTempFile;

    private static long countInstances = 0;

    /**
     * Returns an unique name for context.
     */
    public synchronized static String getUniqueContext() {
        return("context" + (++countInstances));
    }

    /**
     * Constructs a <code>HttpCommunicator2</code> with specified url.
     */
    private HttpCommunicator2(String serverURL) throws MalformedURLException {
        sendURL = new URL(serverURL + "?post-request");
        receiveURL = new URL(serverURL + "?get-response");
        context = getUniqueContext();
    }

    /**
     * Constructs a <code>HttpCommunicator2</code> with specified configuration.
     */
    public HttpCommunicator2(ConfMap map) throws MalformedURLException, IOException {
        this(map.getString("remote.server"));
        fileName = (new TempFile(map)).getName();
        keepTempFile = map.getBoolean("remote.debug.keep-request-file", false);
    }

    /**
     * Send request with specified properties.
     */
    public OutputStream send(Properties properties) throws IOException {
        createSendConnection();
        setRequestProperties(properties);
        bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(fileName));
        fileCreated = true;
        return bufferedOutputStream;
    }

    /**
     * Returns response as an input stream.
     */
    public InputStream receive() throws IOException {
        createReceiveConnection();
        inputStream = new BufferedInputStream(connection.getInputStream());
        checkResponseCode();
        return inputStream;
    }

    /**
     * Clean up resources after responce was received.
     */
    public void cleanupAfterReceive() throws IOException {
        inputStream.close();
        inputStream = null;
        checkResponseCode();
        connection.disconnect();
        connection = null;
    }

    /**
     * Clean up resources after request was sent.
     */
    public void cleanupAfterSend() throws IOException {
        File file = null;
        InputStream in = null;
        try {
            bufferedOutputStream.flush();
            bufferedOutputStream.close();
            bufferedOutputStream = null;
            file = new File(fileName);
            setContentLength((int)file.length());
            if (contentLength < 1)
                throw new IOException("HttpCommunicator: wrong content length");
            connection.setRequestProperty("Content-Length",(new Integer(contentLength)).toString());
            in = new BufferedInputStream(new FileInputStream(file));
            OutputStream out = new BufferedOutputStream(connection.getOutputStream());
            byte[] b = new byte[1024*100];
            int cnt;
            while ((cnt = in.read(b)) >= 0) {
                out.write(b, 0, cnt);
            }
            in.close();
            in = null;
            out.flush();
            out.close();
            out = null;
            deleteTempFile( file );
            fileCreated = false;
            checkResponseCode();
            connection.disconnect();
            connection = null;
        } catch (IOException ex) {
            if (fileCreated) {
                if (in != null) {
                    in.close();
                    in = null;
                }
                if (file == null) file = new File(fileName);
                deleteTempFile( file );
            }
            throw ex;
        }
    }

    /**
     * Creates a connection to send request.
     */
    private void createSendConnection() throws IOException {
        try {
            connection = new HttpURLConnection(sendURL);
            connection.setContext(context);
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            HTTPConnection.addDefaultModule(Class.forName("HTTPClient.CookieModule", true, cl),0);
            CookieModule.setCookiePolicyHandler(null);
            connection.setRequestMethod("POST");
            connection.setAllowUserInteraction(true);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type","text/plain");
            connection.setRequestProperty("Connection","close");
        } catch (Exception ex) {
            throw new IOException("HttpCommunicator: cannot create send connection");
        }
    }

    /**
     * Creates a connection to receive response.
     */
    private void createReceiveConnection() throws IOException {
        connection = new HttpURLConnection(receiveURL);
        connection.setContext(context);
        connection.setRequestMethod("GET");
        connection.setAllowUserInteraction(true);
        connection.setRequestProperty("Connection","close");
    }

    /**
     * Sends the specified properties.
     */
    private void setRequestProperties(Properties props) {
        String name;
        String value;
        for (Enumeration _enum = props.propertyNames(); _enum.hasMoreElements();) {
            name = (String)_enum.nextElement();
            value = props.getProperty(name, "");
            connection.setRequestProperty(name, value);
        }
    }

    /**
     * Returns the response properties.
     */
    private Properties getResponseProperties() {
        Properties props = new Properties();
        int i = 1;
        String name;
        String value;
        while (true) {
            name = connection.getHeaderFieldKey(i);
            value = connection.getHeaderField(i);
            if (name == null) {
                break;
            }
            props.setProperty(name, value);
            i++;
        }
        return props;
    }

    /**
     * Sets the specified length of http content.
     */
    private void setContentLength(int length) {
        contentLength = length;
    }

    /**
     * Checkes response code.
     * @throws IOException if the code is not 200.
     */
    public void checkResponseCode() throws IOException {
        int code = connection.getResponseCode();
        if (code != HttpURLConnection.HTTP_OK) {
            throw new IOException("Response code is other than 200 OK. Url: \n" + connection.toString() + "\n Code: " + code);
        }
    }

    /**
     * Removes temporary file.
     */
    private void deleteTempFile( File file ) throws IOException {
        if (this.keepTempFile == false) {
            if (!file.delete()) throw new IOException("HttpCommunicator: deleting temp file error");
        }
    }
}
