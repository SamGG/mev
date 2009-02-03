package org.tigr.microarray.mev.file;

import org.tigr.microarray.mev.SessionOptions;

public class FileLoadInfo {
	protected FileType fileType;
	protected String dataFileURL;
	protected String arrayType;
	protected String speciesName;
	protected int firstRow = -1;
	protected int firstColumn = -1;
	protected boolean isMultiFile = false;
	protected boolean isDownloadDatafile = false;
	protected String localFile;
	

	public boolean isDownloadDatafile() {
		return isDownloadDatafile;
	}
	public void setDownloadDatafile(boolean isDownloadDatafile) {
		this.isDownloadDatafile = isDownloadDatafile;
	}
	public String getLocalFile() {
		return localFile;
	}
	public FileLoadInfo(SessionOptions so) throws InvalidFileArgumentsException {
		fileType = so.getFileType();
		setDataFileURL(so.getDataFile());
		setArrayType(so.getArrayType());
		setFirstRow(so.getFirstRow());
		setFirstColumn(so.getFirstColumn());
		setMultiFile(fileType.isMultifile());
		if(dataFileURL != null && !dataFileURL.equals(""))
			isDownloadDatafile = true;
		
	}
	public String getArrayType() {
		return arrayType;
	}
	public void setArrayType(String arrayType) {
		this.arrayType = arrayType;
	}
	public String getDataFileURL() {
		return dataFileURL;
	}
	public void setDataFileURL(String dataFilePath) {
		this.dataFileURL = dataFilePath;
	}
	public FileLoadInfo(FileType type) {
		this.fileType = type;
	}
	public String getDataFile() {
		return dataFileURL;
	}

	public void setDataFile(String dataFile) {
		this.dataFileURL = dataFile;
	}

	public FileType getFileType() {
		return fileType;
	}

	public void setFileType(FileType fileType) {
		this.fileType = fileType;
	}

	public int getFirstColumn() {
		return firstColumn;
	}

	public void setFirstColumn(int firstColumn) {
		this.firstColumn = firstColumn;
	}

	public int getFirstRow() {
		return firstRow;
	}

	public void setFirstRow(int firstRow) {
		this.firstRow = firstRow;
	}

	public String getSpeciesName() {
		return speciesName;
	}

	public void setSpeciesName(String speciesName) {
		this.speciesName = speciesName;
	}
	public boolean isMultiFile() {
		return isMultiFile;
	}
	public void setMultiFile(boolean isMultiFile) {
		this.isMultiFile = isMultiFile;
	}
	
}
