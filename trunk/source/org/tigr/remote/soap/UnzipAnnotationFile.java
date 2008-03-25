package org.tigr.remote.soap;


import java.io.*;
import java.util.*;
import java.util.zip.*;

import javax.swing.JOptionPane;


public class UnzipAnnotationFile {

	private String directoryName;
	private String fileName;
	private String targetFileName;
	private String unzippedFile;
	
	//TO DO: Change the constructor
	public UnzipAnnotationFile(String localDirectory, String fileName ) {
		this.fileName=fileName;
		this.directoryName=localDirectory;
	}


	/**
	 * unZipResourcererFiles takes in the annotation files downloaded from Resourcerer
	 * and unzips it in to a .txt file. Unzipping a Resourcerer annotation file does
	 * not give a .txt file, hence requires the extra step. 
	 *  
	 * 
	 * @param outputFile
	 * @return
	 */


	public boolean unZipResourcererFiles(File outputFile) {
		BufferedInputStream bis;
		BufferedOutputStream bos;
		int BUFFERSIZE = 1024;

		try {
			ZipFile zipFile = new ZipFile(outputFile);

			//System.out.println("unzipFile:"+outputFile.getName());
			//System.out.println("unzipFile length:"+outputFile.length());
			Enumeration entries = zipFile.entries();

			byte [] buffer = new byte [BUFFERSIZE];
			int length = 0;
			int cnt = 0;

			while(entries.hasMoreElements()) {



				ZipEntry entry = (ZipEntry)entries.nextElement();

				if(entry.isDirectory()) {
					cnt++;
					continue;
				}

				String entryName = entry.getName();
				String entryFolder = (new File(entryName)).getParent();
				File entryDirectory = new File(this.directoryName+"/"+entryFolder);

				if(entry.isDirectory()&!entryDirectory.exists()) {
					entryDirectory.mkdirs();
				}

				bos = new BufferedOutputStream(new FileOutputStream(this.directoryName+"/"+entry.getName()+".txt"));
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
			e.printStackTrace();
			return false;
		}

		return true;


	}

	/**
	 * extractZipFiles is essentially the same as unZipResourcererFiles,
	 * minus converting the extracted files to .txt files.
	 * This function was added, to enable downloading BN and EASE files along with the annotation files. 
	 * BN and EASE zip files on extracting spit out .txt files.
	 * 
	 * 
	 * 
	 * @param outputFile
	 * @return
	 */


	public boolean extractZipFile(File outputFile) {
		BufferedInputStream bis;
		BufferedOutputStream bos;
		int BUFFERSIZE = 1024;

		try {
			ZipFile zipFile = new ZipFile(outputFile);
			Enumeration entries = zipFile.entries();

			byte [] buffer = new byte [BUFFERSIZE];
			int length = 0;
			int cnt = 0;

			while(entries.hasMoreElements()) {

				ZipEntry entry = (ZipEntry)entries.nextElement();

				if(entry.isDirectory()) {
					cnt++;
					continue;
				}

				String entryName = entry.getName();
				String entryFolder = (new File(entryName)).getParent();
				File entryDirectory = new File(this.directoryName+"/"+entryFolder);

				if(entry.isDirectory()&!entryDirectory.exists()) {
					entryDirectory.mkdirs();
				}

				bos = new BufferedOutputStream(new FileOutputStream(this.directoryName+"/"+entry.getName()));
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
			e.printStackTrace();
			return false;
		}
		return true;
	}




















}