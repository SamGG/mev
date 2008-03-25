package org.tigr.remote.soap;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;

import javax.swing.JOptionPane;


import org.tigr.microarray.mev.annotation.AnnotationDialog;

import ftp.FtpBean;
import ftp.FtpBean;
import ftp.FtpListResult;
import ftp.FtpObserver;
//





/**
 *  
 * @author SARITA NAIR
 *ResourcererFTPClient class is designed to connect to the DFCI-Resourcerer 
 *FTP site. It has two constructors which require the username and password 
 *to log on to the FTP site. One of the constructors also requires a AnnotationDialog
 *object. 
 *
 *
 */

public class ResourcererFTPClient {
	
	/**
	 * @param args
	 */
	private String ChipType;
	private String Organism;
	private String userName;
	private String password;
	private String AnnotationFileName;
	private AnnotationDialog dialog;
	private String dataPath="./data/Annotation/";
	private String home="/pub/bio/tgi/data/Resourcerer/new/";
	private String EASE_BN_remotedir="/pub/bio/tgi/data/Resourcerer/";
	
	public ResourcererFTPClient(String chipType, String organism,String userName, String password) {
		this.ChipType=chipType;
		this.Organism=organism;
		this.userName=userName;
		this.password=password;
		
	}
	
	public ResourcererFTPClient(String chipType, String organism,String userName, 
			String password, AnnotationDialog dialog) {
		this.ChipType=chipType;
		this.Organism=organism;
		this.userName=userName;
		this.password=password;
		this.dialog=dialog;
				
		
	}
	/***
	 * connectToResourcerer() function uses the Apache FTPClient to connect 
	 * to the DFCI-Resourcerer FTP site. 
	 * 
	 * An error message is generated if, log-in attempt was unsuccessful.
	 * Upon successful login, the client searches for the requested annotation
	 * file using the ChipType and organismName information.
	 * (The annotation files are zip files, named after the Affy/Agilent chip they 
	 * contain information about. The files are arranged by the Organism Name)
	 * 
	 * A progress panel is used to show the current status of the file download
	 * to the user. In addition, the AnnotationDialog also contains a status panel,
	 * which shows the location of the downloaded file.
	 * 
	 * The downloaded zip files are extracted and saved as .txt files.
	 *
	 */
	public void connectToResourcerer() {
		
		try {
						
		
			int overallLength = 0;            
			int currentLength = 0;        
			FtpBean ftp = new FtpBean();
        	ftp.ftpConnect("occams.dfci.harvard.edu", "anonymous");
        	        	
        	ftp.setDirectory(home+this.Organism);        	
        	FtpListResult list = ftp.getDirectoryContent();
        	
        	while(list.next()) {		
        		if(list.getName().equals(this.ChipType+".zip")) {
        			overallLength = (int)list.getSize();
        		}
        	}
        	
        	//Checks if the Annotation directory exists, if not creates it
        	File f=new File(dataPath);
			if(!f.exists()) {
				f.mkdir();
				
			}
			
			File newFile=new File(dataPath+this.ChipType+".zip");
        	BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(newFile));        	
            //get binary file to byte array, use listener
            bos.write(ftp.getBinaryFile(this.ChipType+".zip"), 0, overallLength);
			        	
            bos.flush();
            bos.close();
           
        	//String fileName=this.ChipType+".zip";
        	UnzipAnnotationFile unzip=new UnzipAnnotationFile(dataPath,newFile.getName());
        	boolean download=unzip.unZipResourcererFiles(new File(dataPath+newFile.getName()));
        	
        	if(download==true) {
        		setAnnotationFileName(dataPath+this.ChipType+".txt");
        		downloadEASE_BNFiles(ftp);
        		String Msg = "<html>File download completed..<br>"+
    			"<html>You can find the file here: <br> "+
    			this.dataPath+this.ChipType+".txt";
    			AnnotationDialog.statusChange(Msg);
    			
    			
        	}else {
        		setAnnotationFileName("NA");
        		String Msg = "<html>File download could not be completed..</html>";
    			AnnotationDialog.statusChange(Msg);
        	}
        	
        	
        	 ftp.close();
			
		}catch(Exception e) {
			e.printStackTrace();	
		}
	}
	
	/**
	 * 
	 * 
	 * 
	 * @param ftp
	 * @throws IOException
	 */
	public void downloadEASE_BNFiles(FtpBean ftp)throws IOException{
		System.out.println("download EASE and BN files");
		try {
			int overallLength = 0;            
			int currentLength = 0;        
			String localEASEdir="./data/Ease/";
			String localBNdir="./data/BN/";
			ftp.setDirectory(EASE_BN_remotedir+this.Organism+"/");

			// List the zip files in the directory
			FtpListResult list = ftp.getDirectoryContent();



			while(list.next()) {		
				if(list.getName().equals(this.ChipType+"_EASE.zip")) {
					overallLength = (int)list.getSize();

					File f=new File(localEASEdir);
					if(!f.exists()) {
						f.mkdir();

					}

					File newFile=new File(localEASEdir+this.ChipType+"_EASE.zip");
					BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(newFile));        	
					//get binary file to byte array, use listener
					bos.write(ftp.getBinaryFile(newFile.getName()), 0, overallLength);

					bos.flush();
					bos.close();

					//String fileName=this.ChipType+".zip";
					UnzipAnnotationFile unzip=new UnzipAnnotationFile(newFile.getParent(),newFile.getName());
					boolean download=unzip.extractZipFile(newFile);
				}
				//Checks for BN files
				if(list.getName().equals(this.ChipType+"_BN.zip")) {
					File f=new File(localBNdir);
					if(!f.exists()) {
						f.mkdir();

					}

					overallLength = (int)list.getSize();


					File newFile=new File(localBNdir+this.ChipType+"_BN.zip");
					BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(newFile));        	
					//get binary file to byte array, use listener
					bos.write(ftp.getBinaryFile(newFile.getName()), 0, overallLength);

					bos.flush();
					bos.close();


					UnzipAnnotationFile unzip=new UnzipAnnotationFile(newFile.getParent(),newFile.getName());
					boolean download=unzip.extractZipFile(newFile);


				}

			}}catch(Exception e) {
				e.printStackTrace();
			}


	}


	
	
	// TODO Auto-generated method stub
	
	public String getAnnotationFileName() {
		return AnnotationFileName;
	}
	
	public void setAnnotationFileName(String fileName) {
		this.AnnotationFileName=fileName;
	}
	
	
}
