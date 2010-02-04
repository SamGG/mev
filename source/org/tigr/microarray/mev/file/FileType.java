package org.tigr.microarray.mev.file;

import java.util.Hashtable;
//TODO add file descriptions from SuperExpressionFileLoader.getFileDescription?

import org.tigr.microarray.mev.file.agilent.AgilentMevFileLoader;

public enum FileType {
  	STANFORD				("tdms",	 			0,	false, 	StanfordFileLoader.class, 			"Tab Delimited, Multiple Sample Files (TDMS) (*.*)"), 
  	MEV_TARBALL				("mev", 				1,	true,	MevFileLoader.class,				"MeV Files (*.mev and *.ann)"), 
  	TAV						("tav", 				2,	false,	TavFileLoader.class,				"TIGR ArrayViewer Files (*.tav)"),
  	AFFY_GCOS				("affy-gcos", 			3,	false,	AffyGCOSFileLoader.class,			"Affymetrix GCOS(using MAS5) Files"),
  	DCHIP					("dchip", 				4,	true,	DFCI_CoreFileLoader.class,			"dChip/DFCI_Core Format Files"), 
  	GW_AFFY					("gw-affy", 			5,	true,	AffymetrixFileLoader.class,			"GW Affymetrix Files"),
  	BIOCONDUCTOR_MAS5		("bioconductor-mas5",	6,	false,	Mas5FileLoader.class,				"Bioconductor(using MAS5) Files"),
  	RMA						("rma", 				7,	false,	RMAFileLoader.class,				"RMA Files"),
  	CGH						("cgh", 				8,	false,	CGHStanfordFileLoader.class,		"CGH Tab Delimited, Multiple Sample"), 
  	AFFY_GPL				("affy-gp", 			9,	false,	SOFT_AffymetrixFileLoader.class,	"GEO SOFT Affymetrix Format Files"),
  	TWO_CHANNEL_GPL			("two-channel-gpl",		10,	false,	SOFT_TwoChannelFileLoader.class,	"GEO SOFT Two Channel Format Files"),
  	GENEPIX					("genepix", 			11,	true,	GenePixFileLoader.class,			"GenePix Format Files"),
  	AGILENT					("agilent",				12,	true,	AgilentFileLoader.class,			"Agilent Files"),
  	GEO_SERIES_MATRIX		("geo-series-matrix",	13,	false,	GEOSeriesMatrixLoader.class,		"GEO Series Matrix Files"),
  	GEO_GDS					("geo-gds", 			14,	false,	GEO_GDSFileLoader.class,			"GEO GDS Format Files"), 
  	MAGETAB					("mage-tab", 			15,	false,	MAGETABFileLoader.class,			"MAGE-TAB Format Files"),;

 	
  	private String commandArg;
  	private int loaderIndex;
  	private boolean multifile;
  	private String description;
  	private Class<ExpressionFileLoader> loader;
  	
  	private static Hashtable<String, FileType> commandArgOptions = new Hashtable<String, FileType>();
  	static {
  		for(FileType f: FileType.values())
			commandArgOptions.put(f.commandArg, f);
  	}
  	
  	FileType(String commandArg, int loaderIndex, boolean multifile, Class loader, String description) {
  		this.commandArg = commandArg;
  		this.loaderIndex = loaderIndex;
  		this.multifile = multifile;
  		this.loader = loader;
  		this.description = description;
  	}
  	
  	public static FileType getTypeFromLoaderIndex(int loaderIndex){
  		for(FileType f: FileType.values()) {
  			if(f.loaderIndex == loaderIndex)
  				return f;
  		}
  		return null;
  	}
  	public Class<ExpressionFileLoader> getFileLoader() {
  		return loader;
  	}

  	
  	public static FileType getFileType(String commandArg) {
  		return commandArgOptions.get(commandArg);
  	}
  	
  	public static String getAvailableOptions() {
  		return commandArgOptions.keys().toString();
  	}
  	
  	public int getLoaderIndex() {
  		return loaderIndex;
  	}
  	public String getDescription() {
  		return description;
  	}
  	public boolean isMultifile() {
  		return multifile;
  	}
  	public String getCommandArg() {
  		return commandArg;
  	}
}