package org.tigr.remote.soap;
import java.io.File;
import java.io.FileOutputStream;
import java.net.InetAddress;

import javax.swing.JOptionPane;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPReply;
import org.tigr.microarray.mev.annotation.AnnotationDialog;
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
			FTPClient ftp = new FTPClient();
			
			FTPClientConfig conf = new FTPClientConfig(FTPClientConfig.SYST_UNIX);
			ftp.configure(conf);
			
			ftp.connect(InetAddress.getByName("occams.dfci.harvard.edu"));
			int reply = ftp.getReplyCode();
			
			if(reply==FTPReply.SERVICE_NOT_AVAILABLE) {
				String eMsg = "<html>The ftp link seems to be broken...<br>" +
				"<html>Please report this to the MeV developers (mev@jimmy.harvard.edu) <br> "+
				"<html>We apologize for the inconvenience.<br> </html>";
				JOptionPane.showMessageDialog(null, eMsg, "Warning", JOptionPane.INFORMATION_MESSAGE);
				System.exit(1);
			}
			
			if(!FTPReply.isPositiveCompletion(reply)) {
				
				ftp.disconnect();
				System.exit(1);
				
			}
			
			if(!ftp.login(this.userName, this.password)) {
				String eMsg = "<html>Unable to login to the FTP server..<br>" +
				"<html>Please report this to the MeV developers (mev@jimmy.harvard.edu) <br> "+
				"<html>We apologize for the inconvenience.<br> </html>";
				JOptionPane.showMessageDialog(null, eMsg, "Warning", JOptionPane.INFORMATION_MESSAGE);
				
			}else {
				if(this.dialog!=null) {
					//System.out.println("dialog is not null");
					this.dialog.progressPanel.update("Logged on to the FTP site");
					this.dialog.progressPanel.setIndeterminate(true);
					//this.dialog.progressPanel.increment();
				}
			}
			
			ftp.changeWorkingDirectory(home+this.Organism+"/");
			
			// List the zip files in the directory
			String[]names=ftp.listNames("*.zip");
			String targetFile="";
			
			for(int i=0;i<names.length;i++) {
				
				//System.out.println("Zip file names:"+names[i]);
				//System.out.println((names[i].substring(0, names[i].indexOf('.'))));
				if(names[i].substring(0, names[i].indexOf('.')).equalsIgnoreCase(this.ChipType)) {
					targetFile=names[i];
					if(this.dialog!=null) {
						this.dialog.progressPanel.update("Located "+targetFile);
						this.dialog.progressPanel.setIndeterminate(true);
						this.dialog.progressPanel.setMaximum(ftp.stat(names[i]));
						
					}
					//System.out.println("targetFileName:"+targetFile);
				}
			}
			
			if(targetFile.equals("")) {
				String eMsg = "<html>The file" +this.ChipType+".zip"+"does not exist.<br>"+
				"<html> Please check if you selected the CORRECT <br> "+
				"<html>ORGANISM and CHIP TYPE<br> </html>";
				if(this.dialog!=null)
					this.dialog.progressPanel.dispose();
				JOptionPane.showMessageDialog(null, eMsg, "Warning", JOptionPane.INFORMATION_MESSAGE);
				
			}
			
			
			
			File f=new File(dataPath+targetFile);
			FileOutputStream temp=new FileOutputStream(f);
			ftp.type(FTPClient.BINARY_FILE_TYPE);
			ftp.retrieveFile(f.getName(), temp);
			if(this.dialog!=null) {
				this.dialog.progressPanel.setIndeterminate(false);
				this.dialog.progressPanel.update("Unzipping the files");
				//this.dialog.progressPanel.increment();
			}
			
			
			//System.out.println("downloaded file name:"+f.getName());
			//System.out.println("size of downloaded file is:"+f.length());
			temp.close();
			ftp.logout();
			
			//processing the downloaded zip file
			if(this.dialog!=null) {
				this.dialog.progressPanel.setIndeterminate(true);
				this.dialog.progressPanel.update("Download was Successful");
			}
			UnzipAnnotationFile unzip=new UnzipAnnotationFile(f.getParent(),f.getName());
			String unzippedFile=unzip.unZipFiles();
			
			String finalFileName=(unzippedFile.substring(0,unzippedFile.indexOf('.'))).concat(".txt");
			setAnnotationFileName(this.dataPath+finalFileName);
			
			String Msg = "<html>File download completed..<br>"+
			"<html>You can find the file here: <br> "+
			this.dataPath+finalFileName;
			
			AnnotationDialog.statusChange(Msg);
			//System.out.println("");
			
		}catch(Exception e) {
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
