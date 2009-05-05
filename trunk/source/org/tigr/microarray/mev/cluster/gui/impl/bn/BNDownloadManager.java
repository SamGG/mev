/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
package org.tigr.microarray.mev.cluster.gui.impl.bn;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.tigr.microarray.mev.TMEV;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.HTMLMessageFileChooser;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.Progress;

import ftp.FtpBean;
import ftp.FtpListResult;
import ftp.FtpObserver;

public class BNDownloadManager {

	// Generic FTP related Constants
	private String FTP_REMOTE_FILE_OR_DIR;
	private boolean isDir = false;
	//TODO Change testing URL
	private static String FTP_CONFIG_URL = "http://compbio.dfci.harvard.edu/webstart/cytoscape/cyto_props.txt"; //"http://www.tm4.org/mev/mev_url.properties";
	private static String FTP_CONFIG_URL_TEST = "http://web-dev.dfci.harvard.edu/webstart/cytoscape/cyto_props.txt";
	private String FTP_SERVER;    
	private String REPOSITORY_ROOT;

	private JFrame frame;
	private int BUFFERSIZE = 1024;
	private Progress progress;
	private boolean okStatus = true;

	private ProgressListener listener;
	String destPath;
	/** Creates a new instance of BNDownloadManager */
	public BNDownloadManager(JFrame parent, String destPath, String title, String resourceName, boolean isDir) {        
		frame = parent;
		this.destPath = destPath;
		this.FTP_REMOTE_FILE_OR_DIR = resourceName;
		this.isDir = isDir;
		
		listener = new ProgressListener();
		//Initialize the progress bar
		progress = new Progress(frame, title, listener);
		progress.setLocationRelativeTo(frame.getOwner());
		progress.setAlwaysOnTop(true);
	}
	
	/**
	 * 
	 */
	public boolean updateFiles() {    	
		try {

			//prepare progress to show repository config progress
			progress.setDescription("Retreving Repository Information");
			progress.setUnits(2);
			progress.show();
			
			//TODO
			//Just to show the progress
			//Thread.sleep(3000);
			
			Hashtable propertyHashes = getRepositoryInfo();
			//prepare to visit repositories
			progress.setDescription("Visiting Repository for Resource Checks");
			progress.setUnits(propertyHashes.size());
			//TODO
			//Just to show the progress
			//Thread.sleep(3000);
			
			progress.dispose();

			//if we have repository information and repository content
			//information, we are ready to construct the dialog and get selected files

			if(okStatus) {
					//get the selected server, repository root, and implies zip name
					this.FTP_SERVER = ((String)propertyHashes.get("kegg_server")).trim();
					if(this.FTP_SERVER.endsWith("/"))
						this.FTP_SERVER = this.FTP_SERVER.substring(0, this.FTP_SERVER.length()-1);
					this.REPOSITORY_ROOT = ((String)propertyHashes.get("kegg_dir")).trim();
					if(this.REPOSITORY_ROOT.endsWith("/"))
						this.REPOSITORY_ROOT = this.REPOSITORY_ROOT.substring(0, this.REPOSITORY_ROOT.length()-1);
					return updateBNFiles(this.FTP_REMOTE_FILE_OR_DIR);
			} else
				return false;
		} catch (Exception e) {
			e.printStackTrace();
			System.out.print("Message"+e.getMessage());
			JOptionPane.showMessageDialog(frame, "<html>An error occurred when retrieving information on" +
					"available<br>species and clone set files.  Update request cannot be fulfilled.", "BN Update Error", JOptionPane.ERROR_MESSAGE);
			okStatus = false;
			progress.dispose();
			return okStatus;
		}      
	}

	public static Hashtable getConfigInfo() {
		Hashtable<String, String> props = new Hashtable<String, String>();
		try {
			URLConnection conn = new URL(FTP_CONFIG_URL).openConnection();

			//add repository property hashes to the vector
			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String [] keyValue;
			
			String line;
			//loop through the file to parse into 
			while((line = br.readLine())!= null) {

				//comment line
				if(line.startsWith("#"))
					continue;
				keyValue = line.split("=");
				//add the current property
				props.put(keyValue[0], keyValue[1]);
			}	
		} catch (Exception e) {
			e.printStackTrace();
		}
		return props;
	}
	
	/**
	 * Pulls the config file and parses the repository information
	 * into a vector of repository properties Hashtables
	 * @return
	 */
	private Hashtable getRepositoryInfo() {
		//get the repository information from the repository config File            
		Hashtable<String, String> repHash = new Hashtable<String, String>();

		try {
			URLConnection conn = new URL(FTP_CONFIG_URL).openConnection();    		    		
			progress.setValue(1);

			//add repository property hashes to the vector
			repHash = parseConfig(conn.getInputStream());			
			
			//Cache property results in TMEV props
			Enumeration keySet = repHash.keys();
			while(keySet.hasMoreElements()) {
				String key = (String)keySet.nextElement();
				TMEV.storeProperty(key, (String)repHash.get(key));
			}
		} catch (Exception e) {
			System.out.println("Could not retreive Web Repository Info. Using cached value instead.");
			//e.printStackTrace();
			//JOptionPane.showMessageDialog(new Frame(), "An error occurred when retrieving Web Repository Info.\n  Update request cannot be fulfilled.", "Cytoscape Launch Error", JOptionPane.ERROR_MESSAGE);
			// Try to use cached values from TMEV props
			String cytoURL = TMEV.getSettingForOption("cytoscape_webstart").trim();
			String cytoLibDir = TMEV.getSettingForOption("cytoscape_lib_dir").trim();
			String keggServer = TMEV.getSettingForOption("kegg_server").trim();
			String keggDir = TMEV.getSettingForOption("kegg_dir").trim();
			if(cytoURL.equals("") || cytoURL == null 
					|| cytoLibDir.equals("") || cytoLibDir == null 
					|| keggServer.equals("") || keggServer == null
					|| keggDir.equals("") || keggDir == null) {
				JOptionPane.showMessageDialog(new Frame(), "An error occurred when retrieving Web Repository Info.\n  Previously cached info not avaialble", "Cytoscape Launch Error", JOptionPane.ERROR_MESSAGE);
			} else {
				repHash.put("cytoscape_webstart", cytoURL);
				repHash.put("cytoscape_lib_dir", cytoLibDir);
				repHash.put("kegg_server", keggServer);
				repHash.put("kegg_dir", keggDir);
				return repHash;
			}
		}

		//return the vector of repository hashes
		return repHash;    	
	}

	/**
	 * 
	 * @return
	 */
	public static Hashtable getRepositoryInfoCytoscape() {
		Hashtable<String, String> repHash = new Hashtable<String, String>();

		try {
			URLConnection conn = new URL(FTP_CONFIG_URL).openConnection();    		    		

			//add repository property hashes to the vector
			repHash = parseConfigCytoscape(conn.getInputStream());			
			
			//Cache property results in TMEV props
			Enumeration keySet = repHash.keys();
			while(keySet.hasMoreElements()) {
				String key = (String)keySet.nextElement();
				TMEV.storeProperty(key, (String)repHash.get(key));
			}
		} catch (Exception e) {
			System.out.println("Could not tereive Web Repository Info. Using cached value instead.");
			//e.printStackTrace();
			//JOptionPane.showMessageDialog(new Frame(), "An error occurred when retrieving Web Repository Info.\n  Update request cannot be fulfilled.", "Cytoscape Launch Error", JOptionPane.ERROR_MESSAGE);
			// Try to use cached values from TMEV props
			String cytoURL = TMEV.getSettingForOption("cytoscape_webstart").trim();
			String cytoLibDir = TMEV.getSettingForOption("cytoscape_lib_dir").trim();
			String keggServer = TMEV.getSettingForOption("kegg_server").trim();
			String keggDir = TMEV.getSettingForOption("kegg_dir").trim();
			if(cytoURL.equals("") || cytoURL == null 
					|| cytoLibDir.equals("") || cytoLibDir == null 
					|| keggServer.equals("") || keggServer == null
					|| keggDir.equals("") || keggDir == null) {
				JOptionPane.showMessageDialog(new Frame(), "An error occurred when retrieving Web Repository Info.\n  Previously cached info not avaialble", "Cytoscape Launch Error", JOptionPane.ERROR_MESSAGE);
			} else {
				repHash.put("cytoscape_webstart", cytoURL);
				repHash.put("cytoscape_lib_dir", cytoLibDir);
				repHash.put("kegg_server", keggServer);
				repHash.put("kegg_dir", keggDir);
				return repHash;
			}
		}

		//return the vector of repository hashes
		return repHash; 
	}
	
	/**
	 * 
	 * @param is
	 * @return
	 * @throws IOException
	 */
	private static Hashtable<String, String> parseConfigCytoscape(InputStream is) throws IOException{
		
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String [] keyValue;

		Hashtable<String, String> currHash = new Hashtable<String, String>();
		String line;
		//loop through the file to parse into 
		while((line = br.readLine())!= null) {
			//comment line, if any
			if(line.startsWith("#"))
				continue;
			keyValue = line.split("=");
			//add the current property
			System.out.println("URL Config: " + keyValue[0] + "-" + keyValue[1]);
			currHash.put(keyValue[0], keyValue[1]);
		}
		return currHash;
	}

	/**
	 * Populates a vector of Hashtables that contain properties
	 * about the availible BN repositories and how the dialog
	 * should be convfigured
	 * 
	 * @param is the InputStream from the HTTP connection and config file
	 * @return returns a Vector of Hashtables
	 * @throws IOException
	 */
	private Hashtable<String, String> parseConfig(InputStream is) throws IOException{
		
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String [] keyValue;

		Hashtable<String, String> currHash = new Hashtable<String, String>();
		String line;
		//loop through the file to parse into 
		while((line = br.readLine())!= null) {
			//comment line, if any
			if(line.startsWith("#"))
				continue;
			keyValue = line.split("=");
			//add the current property
			System.out.println("URL Config: " + keyValue[0] + "-" + keyValue[1]);
			currHash.put(keyValue[0], keyValue[1]);
		}
		progress.setValue(2);

		return currHash;
	}

	/** 
	 * Kicks off the thread to update the file system given species and array
	 */ 
	private boolean updateBNFiles(String file) {
		//Thread thread = new Thread(new Runner(file));
		//thread.start();
		return getBaseFiles(file);
	}

	/** 
	 * Controls the update process by calling for downloads and extractions
	 */
	private boolean getBaseFiles(String file) {

		boolean pass1 = true;
		//boolean pass2 = true;

		File baseDir = new File(this.destPath);
		//if(!baseDir.exists())
		//baseDir.mkdir();

		File outputFile = new File(baseDir.getAbsolutePath() + "/" + file);

		progress.setTitle("BN Download");
		progress.setDescription("Download KEGG Interaction File");        
		progress.show();
		pass1 = downloadFile(file, outputFile);
		//if(pass1){
			//pass1 = extractZipFile(outputFile);
		//}
		//outputFile=new File(baseDir.getAbsolutePath()+"/"+array+"_Ar.zip");
		//pass2 = downloadFile(species, "symArts.zip", outputFile);
		//if(pass2)
			//pass2 = extractZipFile(outputFile);
		progress.dispose();

		if(pass1)
			JOptionPane.showMessageDialog(frame, "The BN file system update is complete.", "BN File System Update", JOptionPane.INFORMATION_MESSAGE);
		else
			JOptionPane.showMessageDialog(frame, "The BN file system update was terminated due to the reported error.", "BN File System Update", JOptionPane.ERROR_MESSAGE);
		
		return pass1;
	}

	/** 
	 * Downloads the file at sourceURL to output file (dest), returns true if successful
	 */
	private boolean downloadFile(String file, File dest) {//String sourceURL, File dest) {
		BufferedInputStream bis;
		BufferedOutputStream bos;

		//buffer = new byte [BUFFERSIZE];
		int length = 0;

		try {
			int overallLength = 0;            
			int currentLength = 0;            
			progress.setValue(0);

			FtpBean ftp = new FtpBean();
			ftp.ftpConnect(FTP_SERVER, "anonymous");
			ftp.setDirectory(REPOSITORY_ROOT);
			//ftp.ftpConnect("occams.dfci.harvard.edu", "anonymous");
			//ftp.setDirectory("pub/bio/MeV/kegg");
			FtpListResult list = ftp.getDirectoryContent();

			while(list.next()) {		
				if(list.getName().equals(file)) {
					overallLength = (int)list.getSize();
				}
			}
			progress.setUnits(overallLength);
			listener.reset();
			listener.setMax(overallLength);
			
			bos = new BufferedOutputStream(new FileOutputStream(dest));        	
			//get binary file to byte array, use listener
			bos.write(ftp.getBinaryFile(file, listener), 0, overallLength);

			bos.flush();
			bos.close();
			ftp.close();
		} catch (Exception ioe) {
			progress.dispose();
			ioe.printStackTrace();
			JOptionPane.showMessageDialog(frame, "<html>An Error occured when downloading "+file+".<br>The update request cannot be fulfilled.</html>", "BN Update Download Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}

	/** Extracts the specified zip file, returns true is successful
	 */
	private boolean extractZipFile(File outputFile) {
		BufferedInputStream bis;
		BufferedOutputStream bos;
		progress.setTitle("Extracting zip file");
		progress.setDescription("Extracting zip file: "+outputFile.getAbsolutePath());

		try {
			ZipFile zipFile = new ZipFile(outputFile);
			progress.setUnits(zipFile.size());

			Enumeration entries = zipFile.entries();
			File baseDir = outputFile.getParentFile();
			byte [] buffer = new byte [BUFFERSIZE];
			int length = 0;
			int cnt = 0;

			while(entries.hasMoreElements()) {

				progress.setValue(cnt);

				ZipEntry entry = (ZipEntry)entries.nextElement();

				if(entry.isDirectory()) {
					cnt++;
					continue;
				}

				String entryName = entry.getName();
				String entryFolder = (new File(entryName)).getParent();
				File entryDirectory = new File(baseDir.getAbsolutePath()+"/"+entryFolder);
				if(!entryDirectory.exists()) {
					entryDirectory.mkdirs();
				}
				bos = new BufferedOutputStream(new FileOutputStream(baseDir.getAbsolutePath()+"/"+entry.getName()));
				bis = new BufferedInputStream(zipFile.getInputStream(entry));

				while( (length = bis.read(buffer, 0, BUFFERSIZE)) > 0 ) {
					bos.write(buffer, 0, length);
				}

				cnt++;
				bos.flush();
				bos.close();
				bis.close();
			}
		} catch (Exception e) {
			progress.dispose();
			e.printStackTrace();
			JOptionPane.showMessageDialog(frame, "<html>An Error occured when extracting "+outputFile.getAbsolutePath()+".<br>The update request cannot be fulfilled.</html>", "BN Update Download Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}


	private class Runner implements Runnable {
		private String file;

		public Runner(String file) {
			this.file = file;
		}

		public void run() {
			getBaseFiles(file);
		}        
	}


	/**
	 * The class to listen to progress, monitor and algorithms events.
	 */
	private class ProgressListener extends DialogListener implements WindowListener, FtpObserver {
		private int maxProgress = 0;
		private int currProgress = 0;
		private boolean isStopped = false;

		public void setMax(int max) {
			maxProgress = 0;
		}

		public void reset() {
			maxProgress = 0;
			currProgress = 0;
		}

		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			if (command.equals("cancel-command")) {                
				isStopped = true;
				progress.dispose();                
			}
		}

		public void windowClosing(WindowEvent e) {
			isStopped = true;
			progress.dispose();
		}
		/* (non-Javadoc)
		 * @see ftp.FtpObserver#byteRead(int)
		 */
		public void byteRead(int bytes) {
			currProgress += bytes;			
			if( progress != null)
				progress.setValue(currProgress);


		}
		/* (non-Javadoc)
		 * @see ftp.FtpObserver#byteWrite(int)
		 */
		public void byteWrite(int bytes) {
			currProgress += bytes;
			if( progress != null)
				progress.setValue(bytes);			
		}        
		public boolean isStopped() {
			return isStopped;
		}
	}



	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
