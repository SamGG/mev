package org.tigr.microarray.mev.resources;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.tigr.microarray.mev.file.FileType;

public class ExpressionDataSupportDataFile extends ISupportFileDefinition {
//	private static String rootURL = getBaseResourceURL("gsea_support_file_location");
	private String fileURL;
	private boolean isMultiFile;
	private FileType fileType;
	
	public ExpressionDataSupportDataFile(String fileURL, boolean isMultiFile, FileType ft) {
		this.fileURL = fileURL;
		this.isMultiFile = isMultiFile;
		this.fileType = ft;
	}
	@Override
	public URL getURL() throws MalformedURLException {
		return new URL(fileURL);
	}

	@Override
	public String getUniqueName() {
		try {
			URL temp = new URL(fileURL);
			String temp2 = temp.getFile();
			String temp3 = temp2.substring(temp2.lastIndexOf('/')+1);
//			System.out.println("got URL, returning " + temp3);
			return temp3;
		} catch (MalformedURLException mue) {
			String temp = fileURL.substring(fileURL.lastIndexOf('/')+1);
//			System.out.println("couldn't get URL, returning " + temp);
			return temp;
		}
	}

	@Override
	public boolean isValid(File f) {
		// TODO write this
		return true;
	}
	public boolean isVersioned() {
		return true;
	}
	public boolean isSingleFile() {
		return !isMultiFile;
	}
	public FileType getFileType() {
		return fileType;
	}
}
