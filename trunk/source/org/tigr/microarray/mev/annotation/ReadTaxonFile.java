

package org.tigr.microarray.mev.annotation;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JOptionPane;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPReply;
import org.tigr.remote.soap.UnzipAnnotationFile;
import org.tigr.util.StringSplitter;

import ftp.FtpBean;
import ftp.FtpListResult;


/**
 * 
 * @author Sarita Nair
 * ReadTaxonFile class was created to fetch and read the file
 * "kingdom_species_cloneset_list.txt" hosted at occams.dfci.harvard.edu.
 * The location of the file is denoted by the variable "home" (see below).
 * 
 * This file contains information about the array types, organisms supported by Resourcerer.
 * This file is read and used to populate a hashtable; which in turn helps dynamically populate the 
 * JComboboxes in "AnnotationDialog" class.
 * 
 *
 */



public class ReadTaxonFile {

	private String userName;
	private String password;
	private String eMsg;
	private String home = "/pub/bio/tgi/data/Resourcerer/";
	private String taxonFile = "kingdom_species_cloneset_list.txt";
	private String dataPath = "./data/Annotation/";
	private String taxonFilePath;

	public ReadTaxonFile(String userName, String password) {
		this.userName = userName;
		this.password = password;
	}

	/**
	 * @author sarita
	 * 
	 * Establishes connection to Resourcerer, change to the appropriate
	 * directory and download the taxonfile to local directory
	 * 
	 */


	public void connectToResourcerer() {

		try {

			int overallLength = 0;            
			int currentLength = 0;
			FtpBean ftp = new FtpBean();
			ftp.ftpConnect("occams.dfci.harvard.edu", "anonymous");

			ftp.setDirectory(home);        	
			FtpListResult list = ftp.getDirectoryContent();

			while(list.next()) {		
				if(list.getName().equals(this.taxonFile)) {
					overallLength = (int)list.getSize();
				}
			}
			
			
			File f = new File(dataPath);
			if (!f.exists()) {
				f.mkdir();

			}

			File newFile = new File(dataPath + taxonFile);
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(newFile));        	
			//get binary file to byte array, use listener
			bos.write(ftp.getBinaryFile(newFile.getName()), 0, overallLength);

			bos.flush();
			bos.close();
			ftp.close();


			setTaxonFilePath(dataPath, taxonFile);

		} catch (Exception e) {
			if (eMsg != null) {
				JOptionPane.showMessageDialog(null, eMsg, "Error",
						JOptionPane.ERROR_MESSAGE);
			} else {
				e.printStackTrace();
				String eMsg = "<html>There seems to be a problem with the FTP connection...<br>"
					+ "<html>Please report this to the MeV developers (mev@jimmy.harvard.edu) <br> "
					+ "<html>We apologize for the inconvenience.<br> </html>";
				JOptionPane.showMessageDialog(null, eMsg, "Error",
						JOptionPane.ERROR_MESSAGE);
				System.exit(1);
			}

		}

	}


	/**
	 * 
	 * @param taxonfile
	 * @return
	 * @throws IOException
	 * 
	 * This function first generates a Vector, which is a list of organism Names 
	 * and a hashtable (key=organismName, Value=arrays associated with the key).
	 * These two values are then put in to a hash with keys "OrganismList" and
	 * "Org2ChipType".
	 * 
	 */





	public Hashtable Org2ChipType(File taxonfile) throws IOException {

		BufferedReader breader = new BufferedReader(new FileReader(taxonfile));
		Hashtable<String, Vector> MapValues = new Hashtable<String, Vector>();
		Vector orgNameList = new Vector();
		Hashtable AllHash = new Hashtable();
		String currentLine;
		StringSplitter ss = new StringSplitter('\t');

		while ((currentLine = breader.readLine()) != null) {
			ss.init(currentLine);
			Vector values = new Vector();
			while (ss.hasMoreTokens()) {
				ss.nextToken();
				String orgName = (String) ss.nextToken();
				String chipType = (String) ss.nextToken();
				if (!MapValues.containsKey(orgName)) {
					orgNameList.add(orgName);
					values.add(chipType);

					MapValues.put(orgName, values);
				} else {
					Vector tempVec = MapValues.get(orgName);
					tempVec.add(chipType);
					MapValues.put(orgName, tempVec);
				}

			}

		}

		AllHash.put("OrganismList", orgNameList);
		AllHash.put("Org2ChipType", MapValues);

		return AllHash;
	}

	public void setTaxonFilePath(String datapath, String fileName) {
		taxonFilePath = datapath + fileName;
	}

	public String getTaxonFilePath() {
		return taxonFilePath;

	}

}
