package org.tigr.microarray.mev.gaggle;


public class GaggleConstants {

	public static final String ORIGINAL_GAGGLE_NAME= "Multiple Array Viewer";

	//	General Gaggle metadata values
	public static final String MEV_METADATA = "MeV-metadata";
	
	/* Marks which type of gene identifier is being used. 
	 * Actual identifiers should be taken from AnnotationConstants*/
	public static final String IDENTIFIER_TYPE = "identifier-type";
	public static final String INTERACTIVE = "user-interactive";

	
	

	/* Whether the data has been logged, log2, log10, etc */
	public static final String LOG_STATUS = "log-status";
	public static final String UNLOGGED = "unlogged";
	public static final String LOG_BASE_2 = "log2";
	public static final String LOG_BASE_10 = "log10";
	
	public static final String ALGORITHM_SOURCE = "algorithm-source";

	/* Array name, like affy-hg-u133A. */
	public static final String ARRAY_NAME = "array-name";

	/* Type of array: affy_absolute, etc. Use values listed in IDATA */
	public static final String DATA_TYPE = "data-type";
	public static final String INTENSITY = "intensities";
	public static final String RATIO = "ratios";
	
}
