package org.tigr.microarray.mev.resources;

import java.awt.Frame;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.MalformedURLException;
import java.net.NoRouteToHostException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;

import org.tigr.microarray.mev.resources.FileDownloader.DownloadProgressListener;

import com.jcraft.jsch.SftpProgressMonitor;


public class HTTPDownloader extends FileDownloader {
	URLConnection conn = null;
	
	protected HTTPDownloader(URL host) {
		super(host);
	}

	@Override
	public boolean connect() {
		try {
//			RMProgress monitor = progress;
			conn = hostURL.openConnection();
			conn.setConnectTimeout(10000);
			conn.setReadTimeout(10000);
			return true;
		} catch (IOException ioe) {
			return false;
		}
	}

	@Override
	public void disconnect() {
		//TODO nothing? Is there nothing that needs to be closed or cleaned up? 
		
	}

	@Override
	public String[] getFileList(String path) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Date getLastModifiedDate(String path) {
		try {
			URLConnection conn1 = new URL(hostURL, path).openConnection();
			conn1.setConnectTimeout(10000);
			conn1.setReadTimeout(10000);
			long remoteFileLastModified = conn1.getLastModified();
			if(remoteFileLastModified < 0)
				remoteFileLastModified = 0;
			return new Date(remoteFileLastModified);
		} catch (IOException ioe) {
			//TODO exceptionscheme is needed...
			return null;
		}
	}

	@Override
	public File getTempFile(String path) throws SupportFileAccessError {
		URL fileURL = null;
		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;
		try {
			fileURL = new URL(hostURL, path);
			URLConnection conn1 = fileURL.openConnection();
			conn1.setConnectTimeout(10000);
			conn1.setReadTimeout(10000);
			
			File tempFile = File.createTempFile("mev_resource", "");
			tempFile.deleteOnExit();
			
			bis = new BufferedInputStream(conn1.getInputStream());
			bos = new BufferedOutputStream(new FileOutputStream(tempFile));
			
			progress = new RMProgress(new Frame(), "Downloading " + hostURL, new DownloadProgressListener());

			RMProgress monitor = progress;
			int length = 0;
			try {
				length = conn1.getContentLength();
			} catch (NullPointerException npe) {
				length = 0;
			}
			monitor.init(0, fileURL.getPath(), "??", new Long(length).longValue());

			
			byte[] buffer = new byte[1024];
			int numRead;
			long numWritten = 0;
			while ((numRead = bis.read(buffer)) != -1) {
				bos.write(buffer, 0, numRead);
				numWritten += numRead;
				monitor.count(numRead);
			}
			monitor.end();
			bis.close();
			bos.close();
			if(progress.wasCancelled) {
				return null;
			}
			return tempFile;
		} catch (MalformedURLException mue) {
			SupportFileAccessError sfae = new SupportFileAccessError("bad URL", mue);
			throw sfae;
		} catch (NoRouteToHostException nrthe) {
			SupportFileAccessError sfae = new SupportFileAccessError("Couldn't find host " + fileURL.toString(), nrthe);
			throw sfae;		
		} catch (InterruptedIOException iioe) {
			SupportFileAccessError sfae = new SupportFileAccessError("Network timeout.", iioe);
			throw sfae;
		} catch (IOException ioe) {
			SupportFileAccessError sfae = new SupportFileAccessError("Couldn't save file to local disk.", ioe);
			throw sfae;
		} finally {
			try {
			if(bis != null)
				bis.close();
			} catch (IOException ioe) {}
			try {
			if(bos != null)
				bos.close();
			} catch (IOException ioe) {}
		}
	}
}
