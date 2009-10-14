/*******************************************************************************
 * Copyright (c) 1999-2005, The Institute for Genomic Research (TIGR).
 * Copyright (c) 1999, 2008, the Dana-Farber Cancer Institute (DFCI), 
 * the J. Craig Science Foundataion (JCVI), and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
package org.tigr.microarray.mev.resources;

import java.awt.Frame;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Vector;


import com.jcraft.jsch.SftpProgressMonitor;

import ftp.FtpBean;
import ftp.FtpException;
import ftp.FtpListResult;

public class FTPFileDownloader extends FileDownloader {
	FtpBean ftp = null;
	boolean disposeProgress1= false;
	boolean disposeProgress2= false;
	boolean disposeProgress3= false;
	int overallLength;
	URL url;
	
	public FTPFileDownloader(URL host) {
		super(host);
	}
	

	@Override
	public boolean connect() throws IOException {

		Thread thread = new Thread(new Runnable(){
			public void run(){
				try{
					Thread.sleep(500);
				}catch(Exception e){
					e.printStackTrace();
				}
				if (disposeProgress1){
					disposeProgress1=false;
					return;
				}
				progress = new RMProgress(new Frame(), "Connecting to Host", new DownloadProgressListener());
				
					
				progress.setModal(true);
				progress.setIndeterminate(true);
				progress.setAlwaysOnTop(true);
				progress.setIndeterminantString("Connecting...");
				progress.setFocusable(true);
				progress.init(SftpProgressMonitor.GET, "Connecting to " + hostURL, "??", 0);

			}
		});

		thread.setPriority(Thread.MAX_PRIORITY);
		thread.start();
		
		ftp = new FtpBean();
		try {
			ftp.ftpConnect(hostURL.getHost(), "anonymous");
			if(progress!=null){
				if(progress.wasCancelled)  {
					return false;
				}
			}
		} catch (FtpException ftpe) {
			throw new IOException(ftpe.getMessage());
		} catch (NullPointerException npe) {
			throw new IOException(npe.getMessage());
		}finally {
			disposeProgress1=true;
			if (progress!=null){
				progress.dispose();
				disposeProgress1=false;
			}
		}
		return true;
	}


	@Override
	public void disconnect() {
		assert ftp != null : "FtpFileDownloader is not connected in disconnect()";
		try {
			if(ftp != null)
				ftp.close();
		} catch (FtpException ftpe) {
			//no need to handle
		} catch (IOException ioe) {
			//no need to handle
		}
		
	}


	@Override
	public Date getLastModifiedDate(String path) {
		
		assert ftp != null : "FtpFileDownloader is not connected when queried by getLastModifiedDate";

		Date serverDateLastModified = null;
		try {
			String ftpDir = path.substring(0, path.lastIndexOf('/') + 1);
			URL url = null;
			try {
				url = new URL(hostURL + path);
			} catch (MalformedURLException mue) {
				return null;
			}
			ftp.setDirectory(ftpDir);
			
			FtpListResult list = ftp.getDirectoryContent();
			String filename = url.getFile();
			Calendar currentDate = Calendar.getInstance();
			if (list != null) {
				while (list.next()) {
					if (list.getName().equals(filename.substring(filename.lastIndexOf('/')+1))) {
						try {
							String dateString = list.getDate();
							DateFormat df;
							if(dateString.contains(":")) {
								df = new SimpleDateFormat("MMM dd kk:mm", Locale.US);
								//Date is for this year, contains time information
								Calendar c = Calendar.getInstance();
								c.setTime(df.parse(dateString));
								c.set(Calendar.YEAR, currentDate.get(Calendar.YEAR));
								serverDateLastModified = c.getTime();
							} else {
								df = new SimpleDateFormat("MMM dd yyyy",Locale.US);
								serverDateLastModified = df.parse(dateString);
							}
	
						} catch (Exception e) {
							serverDateLastModified = new Date(0);
							e.printStackTrace();
						}
					}
				}
			} 
		} catch (IOException ioe) {
			serverDateLastModified = null;
		} catch (FtpException ftpe) {
			serverDateLastModified = null;
		}
		return serverDateLastModified;

	}

	private int getSize(String path) throws FtpException, IOException {
		if(ftp == null)
			return 0;
		String ftpDir = path.substring(0, path.lastIndexOf('/') + 1);
		URL url = null;
		try {
			url = new URL(hostURL + path);
		} catch (MalformedURLException mue) {
			return 0;
		}
		ftp.setDirectory(ftpDir);
		
		FtpListResult list = ftp.getDirectoryContent();
		String filename = url.getFile();
		Calendar currentDate = Calendar.getInstance();
		if (list != null) {
			while (list.next()) {
				if (list.getName().equals(filename.substring(filename.lastIndexOf('/')+1))) {
					return (int) list.getSize();
				}
			}
		}
		return 0;
	}

	@Override
	public File getTempFile(String path) throws SupportFileAccessError {
		disposeProgress2=false;
		url = null;
		try {
			url = new URL(hostURL + path);
		} catch (MalformedURLException mue) {
			return null;
		}
		File newFile = null;
		try {
			overallLength = getSize(path);
			progress = new RMProgress(new Frame(), "Downloading " + url.getPath(), new DownloadProgressListener());
			
			Thread thread = new Thread(new Runnable() {
				public void run() {
					try {
						try{
							Thread.sleep(500);
						} catch(Exception e) {
								e.printStackTrace();
						}
						if (disposeProgress2){
							return;
						}
						progress.setModal(true);
						progress.setAlwaysOnTop(true);
						progress.init(SftpProgressMonitor.GET, url.toString(), "??", new Long(overallLength).longValue());

					} catch (Exception ioe) {
//						ioe.printStackTrace();
						
					}
				}
				
			});

			thread.setPriority(Thread.MAX_PRIORITY);
			thread.start();

			
			BufferedOutputStream bos = null;
			newFile = File.createTempFile("mev_resource", "");
			bos = new BufferedOutputStream(new FileOutputStream(newFile));
			
			if (overallLength > 0)
				bos.write(ftp.getBinaryFile(url.getFile(), progress), 0, overallLength);
			else {
				bos.write(ftp.getBinaryFile(url.getFile(), progress));
			}
			
			bos.flush();
			
			if(progress!=null){
				if(progress.wasCancelled)  {
					SupportFileAccessError sfae = new SupportFileAccessError("Connection was cancelled by user.");
					sfae.setCancelledConnection(true);
					throw sfae;
				}
			}
		} catch (IOException ioe) {
			SupportFileAccessError sfae = new SupportFileAccessError("File not found", ioe);
			throw sfae;
		} catch (FtpException ftpe) {
			SupportFileAccessError sfae = new SupportFileAccessError("File not found", ftpe);
			throw sfae;
		} finally {
			disposeProgress2=true;
			if (progress!=null)
				progress.dispose();
		}
		return newFile;
	}


	@Override
	public String[] getFileList(String path) {
		Vector<String> fileList = null;
		try {
			URL url = new URL(hostURL+path);
			progress = new RMProgress(new Frame(), "Getting file list", new DownloadProgressListener());
			progress.init(SftpProgressMonitor.GET, "Getting file list " + url.getHost(), "??", 0);
			progress.setIndeterminate(true);
			progress.show();
			
			String ftpPath = url.getPath();
			String ftpDir = ftpPath.substring(0, ftpPath.lastIndexOf('/') + 1);
			
			ftp.setDirectory(ftpDir);
	
			FtpListResult list = ftp.getDirectoryContent();
			fileList = new Vector<String>();
			if (list != null) {
				while (list.next()) {
					fileList.add(list.getName());
//					System.out.println("Name: " + list.getName());
				}
			} else {
				return null;
			}
		} catch (MalformedURLException mue) {
			mue.printStackTrace();
			return null;
		} catch (FtpException ftpe) {
			ftpe.printStackTrace();
			return null;
		} catch (IOException ioe) {
			ioe.printStackTrace();
			return null;
		} finally {
			//disposeProgress=true;
			if(progress != null) {
				progress.end();
				progress.dispose();
			}
		}
		return fileList.toArray(new String[fileList.size()]);
	
	}
	

	
	/**
	 * Creates a temp file and downloads the file located at url to that
	 * file.
	 * 
	 * @param url
	 *                the url describing the location of the file to
	 *                download
	 * @return a temporary file containing the contents of url
	 * @throws IOException
	 *                 if a problem is encountered when writing the file to
	 *                 disk
	 * @throws FtpException
	 *                 if a problem is encountered while communicating with
	 *                 the FTP server
	 
	private File getFTPFile(URL url, Date cachedDateLastModified) throws SupportFileAccessError {
		FtpBean ftp = null;
		BufferedOutputStream bos = null;
		try {
			boolean downloadFile = true;
			String ftpPath = url.getPath();

			progress = new RMProgress(new Frame(), "Connecting to Host", new DownloadProgressListener());
			progress.init(SftpProgressMonitor.GET, "Connecting to " + url.getHost(), "??", 0);
			progress.setIndeterminate(true);
			progress.show();

			ftp = new FtpBean();
			ftp.ftpConnect(url.getHost(), "anonymous");
			
			String ftpDir = ftpPath.substring(0, ftpPath.lastIndexOf('/') + 1);
			ftp.setDirectory(ftpDir);

			int overallLength = 0;
			Date serverDateLastModified = null;
			
			FtpListResult list = ftp.getDirectoryContent();
			String filename = url.getFile();
			Calendar currentDate = Calendar.getInstance();
			if (list != null) {
				while (list.next()) {
					if (list.getName().equals(filename.substring(filename.lastIndexOf('/')+1))) {
						overallLength = (int) list.getSize();
						try {
							String dateString = list.getDate();
							DateFormat df;
							if(dateString.contains(":")) {
								df = new SimpleDateFormat("MMM dd kk:mm", Locale.US);
								//Date is for this year, contains time information
								Calendar c = Calendar.getInstance();
								c.setTime(df.parse(dateString));
								c.set(Calendar.YEAR, currentDate.get(Calendar.YEAR));
								serverDateLastModified = c.getTime();
							} else {
								df = new SimpleDateFormat("MMM dd yyyy",Locale.US);
								serverDateLastModified = df.parse(dateString);
							}

						} catch (Exception e) {
							serverDateLastModified = new Date(0);
							e.printStackTrace();
						}
					}
				}
			} 
			//only download file if it's newer than the cached file.
			if(cachedDateLastModified==null || serverDateLastModified == null) {
				downloadFile = true;
			} else if(dateIsBefore(cachedDateLastModified, serverDateLastModified)) { 
				downloadFile = true;
			} else {
				downloadFile = false;
			}
//			System.out.println("new date: " + serverDateLastModified + " old date: " + cachedDateLastModified);
			
			progress.end();
			progress.dispose();
			if(downloadFile) {
				progress = new RMProgress(new Frame(), "Downloading " + url.getPath(), new DownloadProgressListener());
				progress.init(SftpProgressMonitor.GET, ftpPath, "??", new Long(overallLength).longValue());
				
				File newFile = File.createTempFile("mev_resource", "");
				bos = new BufferedOutputStream(new FileOutputStream(newFile));
				
				if (overallLength > 0)
					bos.write(ftp.getBinaryFile(url.getFile(), progress), 0, overallLength);
				else {
					bos.write(ftp.getBinaryFile(url.getFile(), progress));
				}
				
				bos.flush();
				
				progress.end();
				
				this.serverLastModifiedDate = serverDateLastModified;
				if(progress.wasCancelled)  {
					SupportFileAccessError sfae = new SupportFileAccessError("Connection was cancelled by user.");
					sfae.setCancelledConnection(true);
					throw sfae;
				}
				return newFile;
			}
			return null;
		} catch (InterruptedIOException iioe) {
			SupportFileAccessError sfae = new SupportFileAccessError("Connection timed out.", iioe);
			throw sfae;
		} catch (IOException ioe) {
			SupportFileAccessError sfae = new SupportFileAccessError("Error downloading file.", ioe);
			throw sfae;
		} catch (FtpException ftpe) {
			SupportFileAccessError sfae = new SupportFileAccessError("Error downloading file.", ftpe);
			throw sfae;
		} finally {
			try {
				if(ftp != null)
					ftp.close();
			} catch (IOException ioe) {
				//Ok, just cleanup
			} catch (FtpException ftpe) {
				//Ok, just cleanup
			} catch (NullPointerException npe) {
				//Ok, just cleanup
			}
			try {
				if(bos != null) 
					bos.close();
			} catch (IOException ioe) {
				//Ok, just cleanup
			}
			
		}
	}
*/
}
