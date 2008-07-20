/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * EaseUpdateManager.java
 *
 * Created on January 19, 2005, 2:53 PM
 */
package org.tigr.microarray.mev.cluster.gui.impl.ease;

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

/**
 *
 * @author  braisted
 */
public class EASEUpdateManager {

	private String FTP_CONFIG_URL = "http://www.tm4.org/ease/ftp_config.txt";

    private String FTP_SERVER;    
    private String REPOSITORY_ROOT;
    
	//private String ORGANISIM_LIST_FILE = "kingdom_species_cloneset_list.txt";
	private String UNIVERSAL_IMPLIES_FILE = "Implies_and_URL_data.zip";
    
    private JFrame frame;
    private Hashtable animalHash;
    private Hashtable plantHash;
    private Vector animalKeys;
    private Vector plantKeys;
	private String destLoc;
    private int BUFFERSIZE = 1024;
    private byte [] buffer;
    private Progress progress;
    private boolean needSeparateImpliesZip = true;
    private boolean okStatus = true;
    
    private ProgressListener listener;
    
    /** Creates a new instance of EaseUpdateManager */
    public EASEUpdateManager(JFrame parent) {        
        frame = parent;
        listener = new ProgressListener();
        
        //Initialize the progress bar
        progress = new Progress(parent, "Ease File Update Progress", listener);                
    }
    
    public void updateFiles() {
        //build the species and arrays lists using the supplied file.
        //these hashes will be used to build the selection dialog.
    	
        try {
        	
        	//prepare progress to show repository config progress
            progress.setDescription("Retreving Repository Information");
            progress.setUnits(2);
            progress.show();
        	
        	//construct a vector of repository information hashes
            //each hash on the vector is a tab and repository
            //and contains properties for that repository
            //(sets progress 1 and 2 during execution)
            Vector tabPropertyHashes = getRepositoryInfo();

            //prepare to visit repositories
            progress.setDescription("Visiting Repsoitory for Resource Checks");
            progress.setUnits(tabPropertyHashes.size());

            //go to the repository, get a list of directories and
            //files under each directory
            //(This uses the list held by the repository (taxon-file)
            populateMenuHashes(tabPropertyHashes);
            
            progress.dispose();
            
            //if we have repository information and repository content
            //information (to populate the dialog), we are ready
            //to construct the dialog and get selected files
            
            if(okStatus) {          
            	
            	//construct dialog given properties for each dialog tab
            	EASEFileUpdateDialog dialog = new EASEFileUpdateDialog(this.frame, tabPropertyHashes);

            	//if ok
            	if(dialog.showModal() == JOptionPane.OK_OPTION) {
            		
            		//get the selected server, repository root, and implies zip name
            		this.FTP_SERVER = (String)(dialog.getRepositoryProperties().get("ftp-server"));
            		this.REPOSITORY_ROOT = (String)(dialog.getRepositoryProperties().get("base-dir"));
            		this.UNIVERSAL_IMPLIES_FILE = (String)(dialog.getRepositoryProperties().get("implies-zip"));
            		
            		//if implies files are already inside ease zip or does not exist clear boolean
            		if(this.UNIVERSAL_IMPLIES_FILE.equalsIgnoreCase("none"))
            			this.needSeparateImpliesZip = false;
            		
            		//start the thead to get the files based on species (**Folder name)
            		//and array name (file name)
            		//Note that we have already set the repository server name,
            		//the repository file root for ease files, and the optional implies zip file name
            		updateEaseFiles(dialog.getSpeciesName(), dialog.getArrayName());
            	}
            }
            
        } catch (Exception e) {
        	e.printStackTrace();
        	System.out.print("Message"+e.getMessage());
        	JOptionPane.showMessageDialog(frame, "<html>An error occurred when retrieving information on" +
            "available<br>species and clone set files.  Update request cannot be fulfilled.", "EASE Update Error", JOptionPane.ERROR_MESSAGE);
            okStatus = false;
            progress.dispose();
        }      
    }
    

    /**
     * Pulls the config file and parses the repository information
     * into a vector of repository properties Hashtables
     * @return
     */
    private Vector getRepositoryInfo() {
        //get the repository information from the repository config File            
    	Vector repHashes = new Vector();
    	
    	try {
    		URLConnection conn = new URL(FTP_CONFIG_URL).openConnection();    		    		
    		progress.setValue(1);
    		
    		//add repository property hashes to the vector
    		repHashes = parseConfig(conn.getInputStream());			

    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	
    	//return the vector of repository hashes
    	return repHashes;    	
    }
    
    
    /**
     * Populates a vector of Hashtables that contain properties
     * about the availible ease repositories and how the dialog
     * should be convfigured
     * 
     * @param is the InputStream from the HTTP connection and config file
     * @return returns a Vector of Hashtables
     * @throws IOException
     */
    private Vector parseConfig(InputStream is) throws IOException{
    	Vector hashVector = new Vector();
    	
    	BufferedReader br = new BufferedReader(new InputStreamReader(is));
    	String [] keyValue;
    	int numTabs = 0;
    	
    	Hashtable currHash = null;
    	String line;

    	//loop through the file to parse into 
    	while((line = br.readLine())!= null) {
    		
    		//comment line
    		if(line.startsWith("#"))
    			continue;

    		keyValue = line.split("\t");

    		//"tab-label" starts a new record
    		if(keyValue[0].equals("tab-label")) {
    			
    			//on second or higher tab, add current hash to vector
    			//else it's the first
    			if(numTabs > 0)
    				hashVector.add(currHash);
    			currHash = new Hashtable();
    			numTabs++;
    		}
    		//add the current property
    		currHash.put(keyValue[0], keyValue[1]);
    	}
    	//add the last currHash to vector
    	hashVector.add(currHash);

    	progress.setValue(2);
    	
    	return hashVector;
    }

    /**
     * Go to each repository (or tab in the dialog) and get menu information
     * @param tabHashes Vector of Repository Properties
     */
    private void populateMenuHashes(Vector tabHashes) {    	
    	for(int i  = 0; i < tabHashes.size(); i++) {
    		getMenuInfo((Hashtable)tabHashes.get(i));
    		progress.setValue(i+1);
    	}    	
    }
    
    /**
     * Vist repository servers and get information about availible
     * directories and files under each directory
     * @param tabHash a set of parameters for that repository
     * (equivalent to a 'tab' on the dialog, hense 'tabHash)
     */
    private void getMenuInfo(Hashtable tabHash) {
    	String server = (String)tabHash.get("ftp-server");
    	String baseLoc = (String)tabHash.get("base-dir");
    	String orgFile = (String)tabHash.get("taxon-file");
    	String label = (String)tabHash.get("tab-label");
    	String text;
    	
    	//to construct directory keys
    	Vector upperLevelKeys = new Vector();
    	//for each directory there will be a set of files
    	//entries in this hash map directory key to a vector of file names
    	Hashtable upperToLowerHash = new Hashtable();
    	
    	try {
    		
    		//connect, grab the small content
    		//(No listener due to the small size, no need for progress)
    		FtpBean ftp = new FtpBean();        		
    		ftp.ftpConnect(server, "anonymous");
    		byte [] content = ftp.getBinaryFile(baseLoc+orgFile);
    		text = new String(content);
    		ftp.close();
    		
    		//break on lines
    		StringTokenizer stok = new StringTokenizer(text, "\n");
    		StringTokenizer stok2;
    		
    		String tabName, upperLabel, lowerLabel;
    		while(stok.hasMoreElements()) {

    			//break lines on tabs
    			stok2 = new StringTokenizer((String)(stok.nextElement()), "\t");
    			
    			//make sure you have enough tokens to play!!! :)
    			if(stok2.countTokens() == 3) {
    				tabName = (String)stok2.nextToken();
    				
    				//ignore if not for this tab... wasteful but simple to do
    				if(!tabName.equalsIgnoreCase(label))
    					continue;
    				
    				//grab directory and file (upper and lower level in folder...)
    				upperLabel = (String)stok2.nextToken();
    				lowerLabel = (String)stok2.nextToken();
    				
    				//solve dos2unix problem if it exists
    				lowerLabel = lowerLabel.trim();

    				//if it's a new directory add it
    				if(!upperLevelKeys.contains(upperLabel))
    					upperLevelKeys.add(upperLabel);
    				
    				//if the hash already has the directory grab its file vector
    				//and add, else make a new vector, add file, put vector into hash
    				if(upperToLowerHash.containsKey(upperLabel)) {
    					((Vector)(upperToLowerHash.get(upperLabel))).add(lowerLabel);
    				} else {
    					Vector fileVector = new Vector();
    					fileVector.add(lowerLabel);
    					upperToLowerHash.put(upperLabel, fileVector);
    				}
    				
    			} else {
    				continue;
    			}
    		}
    		
    		//add keys and hash to the tab hash 
    		tabHash.put("main-keys", upperLevelKeys);
    		tabHash.put("menu-hash", upperToLowerHash);
    		
    	} catch (Exception e) {
    		e.printStackTrace();
    		System.out.print("Message"+e.getMessage());
    		JOptionPane.showMessageDialog(frame, "<html>An error occurred when retrieving information on" +
    				"available<br>species and clone set files.  Update request cannot be fulfilled.", "EASE Update Error", JOptionPane.ERROR_MESSAGE);
    		okStatus = false;
    		progress.dispose();
    	}
    	
    }
    
    
    
    /** Kicks off the thread to update the file system given species and array
     */ 
    private void updateEaseFiles(String species, String array) {
        Thread thread = new Thread(new Runner(species, array));
        thread.start();
    }
    
    
    /** retrievs the File object to receive the file system download
     */
    private File getOutputFileLocation() {
        File file = null;
        
        String msg = "<html><center><h1>EASE Destination Selection</h1></center>";
        msg += "Use this interface to select the location for zip file download and extraction. ";
        msg += "Note that the ease directory will be labeled as \"ease_\" followed by the array name.<br><br>";
        msg += "When running EASE please use the button on the first dialog page to specify this folder as the base file ";
        msg += "system for EASE analysis. ";
        msg += "This will become the default location for EASE annoation information.";
        msg += "</html>";
        
        HTMLMessageFileChooser chooser = new HTMLMessageFileChooser(frame, "EASE File Update Location", msg, TMEV.getFile("data/ease"), true);
        JFileChooser baseChooser = chooser.getFileChooser();
        baseChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setSize(550, 550);
        if(chooser.showModal() == JOptionPane.OK_OPTION) {
            file = chooser.getSelectedFile();
        }
        return file;
    }
    
    /** Controls the update process by calling for downloads and extractions
     */
    private void getBaseFiles(String species, String array) {
   
    	boolean pass = true;
        
        File outputFile = getOutputFileLocation();
        if(outputFile == null)
            return;
        
        File baseDir = new File(outputFile.getAbsolutePath()+"/ease_"+array);
        baseDir.mkdir();
        
        outputFile = new File(baseDir.getAbsolutePath()+"/"+array+"_EASE.zip");
        
        progress.setTitle("EASE File Download");
        progress.setDescription("Download Base Zip File");        
        progress.show();
        pass = downloadFile(species, array+"_EASE.zip", outputFile);
        if(pass)
            pass = extractZipFile(outputFile);
        
        if(this.needSeparateImpliesZip) {
        	String classDir = baseDir+"/Data/Class/Implies_and_URL_data.zip";
        	outputFile = new File(classDir);
        	
        	progress.setDescription("Download Implies and URL Data File");
        	progress.setValue(0);
        	if(pass)
        		pass = downloadFile("", UNIVERSAL_IMPLIES_FILE, outputFile);
        	if(pass)
        		pass = extractZipFile(outputFile);        	
        }

        progress.dispose();
        
        if(pass)
            JOptionPane.showMessageDialog(frame, "The EASE file system update is complete.", "EASE File System Update", JOptionPane.INFORMATION_MESSAGE);
        else
            JOptionPane.showMessageDialog(frame, "The EASE file system update was terminated due to the reported error.", "EASE File System Update", JOptionPane.ERROR_MESSAGE);
    }

    
    /** Downloads the file at sourceURL to output file (dest), returns true if successful
     */
    private boolean downloadFile(String species, String slide, File dest) {//String sourceURL, File dest) {
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
        	ftp.setDirectory(REPOSITORY_ROOT+"/"+species);        	
        	FtpListResult list = ftp.getDirectoryContent();
        	
        	while(list.next()) {		
        		if(list.getName().equals(slide)) {
        			overallLength = (int)list.getSize();
        		}
        	}

        	progress.setUnits(overallLength);
        	listener.reset();
        	listener.setMax(overallLength);
        	
            bos = new BufferedOutputStream(new FileOutputStream(dest));        	
            //get binary file to byte array, use listener
            bos.write(ftp.getBinaryFile(slide, listener), 0, overallLength);
			        	
            bos.flush();
            bos.close();
            ftp.close();
        } catch (Exception ioe) {
            progress.dispose();
            ioe.printStackTrace();
            JOptionPane.showMessageDialog(frame, "<html>An Error occured when downloading "+species+".<br>The update request cannot be fulfilled.</html>", "EASE Update Download Error", JOptionPane.ERROR_MESSAGE);
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
            JOptionPane.showMessageDialog(frame, "<html>An Error occured when extracting "+outputFile.getAbsolutePath()+".<br>The update request cannot be fulfilled.</html>", "EASE Update Download Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }
    
    public static void main(String [] args) {
        EASEUpdateManager manager = new EASEUpdateManager(new JFrame());
        manager.updateFiles();
    }
    
    private class Runner implements Runnable {
        private String species;
        private String array;
        
        public Runner(String species, String array) {
            this.species = species;
            this.array = array;
        }
        
        public void run() {
            getBaseFiles(species, array);
        }        
    }
    
    
    /**
     * The class to listen to progress, monitor and algorithms events.
     */
    private class ProgressListener extends DialogListener implements WindowListener, FtpObserver {

    	private int maxProgress = 0;
    	private int currProgress = 0;
    	
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
                progress.dispose();                
            }
        }
        
        public void windowClosing(WindowEvent e) {
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
    }


}
