package org.tigr.microarray.mev;

import org.kohsuke.args4j.Option;
import org.tigr.microarray.mev.file.FileType;

public class SessionOptions {
	public static final String DATAFILE_STANFORD_ARG = FileType.STANFORD.getCommandArg();
	private String helpText = 
			"\n" +
			"MeV Command-line options \n\n" +
			
			"-help \nPrint this help text and exit MeV. \n\n" + 
			
			"-gaggle \nIf this flag is present, MeV will automatically try to\n" +
			" connect to the Gaggle boss on startup. It will start the boss if\n" +
			" one is not already started. This option requires an internet\n" +
			" connection.\n\n" +
			
			"-fileType TYPE \n" +
			" This flag specifies the type of datafile to be loaded.\n" +
			" Options are:\n" +
			getFileTypeOptions() + "\n" + 
			
			"-fileUrl URL \n" + 
			" The URL of the data file to be preloaded. This must be a complete\n" +
			" url, including the http:// or ftp:// protocol indicators. Relative\n" +
			" urls are not supported. Local filesystem files are also not \n" +
			" supported at this time.\n";


	public SessionOptions(){}

	@Option(name="-help", usage="Prints help text and exits.")
    private boolean sendHelpText = false;

    @Option(name="-gaggle", usage="Connect to Gaggle network on startup.")
    private boolean connectToGaggle;
    
    @Option(name="-fileUrl", metaVar="URL", usage="URL for data file to load.")
    private String dataFile = null;

    @Option(name="-fileType", metaVar="FILE_FORMAT", usage="Format of input file. ")    
    private String fileType = DATAFILE_STANFORD_ARG;

    /* This option specifies an array name. Annotation for this array should be loaded. */
    @Option(name="-arrayType", metaVar="ARRAY_NAME", usage="Name of array type (optional).")
    private String arrayType = null;
   
    /* This option specifies the first non-header row of the data file in -fileURL. */
    @Option(name="-firstRow", metaVar="FIRST_ROW", usage="Index of first non-annotation row of the file in -fileURL (optional).")
    private int firstRow = -1;

    /* This option specifies the first non-header column of the data file in -fileURL. */
    @Option(name="-firstColumn", metaVar="FIRST_COLUMN", usage="Index of first non-annotation column of the file in -fileURL (optional).")
    private int firstColumn = -1;
    
	public String toString() {
		String outString = "";
		outString += "Connect To Gaggle: " 		+ "\t" + connectToGaggle + "\n";
		outString += "Data File URL: " 			+ "\t" + dataFile + "\n";
		outString += "File Loader Type: " 		+ "\t" + fileType + "\n";
		outString += "Array Name or Type: " 	+ "\t" + arrayType + "\n";
		outString += "First row of data: " 		+ "\t" + firstRow + "\n";
		outString += "First column of data: " 	+ "\t" + firstColumn + "\n";
		return outString;
	}
	
	public String getFileTypeOptions() {
		String options = "";
		for(FileType f: FileType.values()) {
			options += "\t" + f.getCommandArg() + "\t" + f.getDescription() + "\n";
		}
		return options;
	}
	
	public boolean sendHelpText() {
		return sendHelpText;
	}
	
	public String getHelpText() {
		return helpText;
	}
	
	public String getArrayType() {
		return arrayType;
	}

	public void setArrayType(String arrayType) {
		this.arrayType = arrayType;
	}

	public boolean isConnectToGaggle() {
		return connectToGaggle;
	}

	public void setConnectToGaggle(boolean connectToGaggle) {
		this.connectToGaggle = connectToGaggle;
	}

	public String getDataFile() {
		return dataFile;
	}

	public void setDataFile(String dataFile) {
		this.dataFile = dataFile;
	}

	public FileType getFileType() {
		return FileType.getFileType(fileType);
	}

	public void setFileType(String dataType) {
		this.fileType = dataType;
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

}
