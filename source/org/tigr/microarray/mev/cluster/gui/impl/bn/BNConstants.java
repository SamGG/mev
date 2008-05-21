package org.tigr.microarray.mev.cluster.gui.impl.bn;


public class BNConstants {
	
	/**
	 * Input File Names
	 */
	public final static String PPI_FILE = "all_ppi.txt";
	public final static String KEGG_FILE = "_kegg_edges.txt";
	public final static String RESOURCERER_FILE = "res.txt";
	public final static String ACCESSION_FILE = "affyID_accession.txt";
	public final static String NEW_ACCESSION_FILE = "newGBs.txt";
	public final static String GB_GO_FILE = "gbGO.txt";
	public final static String GENE_DB_FILE = "symArtsGeneDb.txt";
	public final static String PUBMED_DB_FILE = "symArtsPubmed.txt";
	public final static String OUT_INTERACTION_FILE = "outInteractions.txt";
	public final static String OUT_XML_BIF_FILE = "out_bif.xml";
	
	/**
	 * Ouput File Names
	 */
	public final static String OUT_ACCESSION_FILE = "list.txt";
	public final static String LIT_INTER_FILE = "outInteractionsLit.txt";
	public final static String PPI_INTER_FILE = "outInteractionsPPI.txt"; 
	public final static String LIT_PPI_INTER_FILE = "outInteractionsBoth.txt";
	public final static String KEGG_INTER_FILE = "outInteractionsKegg.txt";
	public final static String LIT_KEGG_INTER_FILE = "outInteractionsLitKegg.txt";
	public final static String PPI_KEGG_INTER_FILE = "outInteractionsPpiKegg.txt";
	public final static String LIT_PPI_KEGG_INTER_FILE = "outInteractionsLitPpiKegg.txt";
	public final static String BIF_RESULT_FILE = "resultBif.xml";
	public final static String LABEL_FILE = "label";
	
	/**
	 * System Separator for file paths
	 */
	public final static String SEP = System.getProperty("file.separator");
	
	/**
	 * Temporary directory name
	 */
	public final static String TMP_DIR = "tmp";
	public final static String RESULT_DIR = "results";
	
	/**
	 * Property Names
	 */
	public final static String RES_FILE_NAME = "resourcererFileName";
	public final static String GB_ACC_FILE_NAME = "gbAccessionsFileName";
	public final static String NEW_GB_ACC_FILE_NAME = "newGbAccessionsFileName";
	public final static String SYM_ARTICLES_FRM_PUBMED = "symbolsArticlesFromPubmedFileName";
	public final static String SYM_ARTICLES_FRM_GENEDB = "symbolsArticlesFromGeneDbFileName";
		  
	public final static String FRM_LIT = "fromLiterature";
	public final static String FRM_PPI = "fromPpi";
	public final static String FRM_KEGG = "fromKegg";
	public final static String OUT_INTER_FILE_NAME = "outInteractionsFileName";
	public final static String PPI_FILE_NAME = "ppiFileName";
	public final static String USE_GO = "useGo";
	public final static String USE_PPI_DIRECT = "usePpiDirectly";
	public final static String USE_PPI_WITHIN = "usePpiOnlyWithin";
	public final static String USE_TRANSITIVE_CLOSURE = "useTransitiveClosure";
	public final static String DIST_K = "distanceK";
	public final static String GB_GO_FILE_NAME = "gbGOsFileName"; 
    
	public final static String ART_REM_THRESH = "articleRemovalThreshold";
	
	public final static String NAMES_FILE_NAME = "namesFileName";
	public final static String DISTRIBUTION_FRM_WEIGHTS = "distributionFromWeights";
	public final static String OUT_XML_BIF_FILE_NAME = "outXMLBifFileName";
    public final static String SIF_FILE_NAME = "sifFileName";
	
	/**
	 * Property file names
	 */
	public final static String LIT_INTER_MODULE_FILE = "getInterModLit.props";
	public final static String PPI_INTER_MODULE_DIRECT_FILE = "getInterModPPIDirectly.props";
	public final static String BOTH_INTER_MODULE_FILE = "getInterModBoth.props"; //PPI-LIT
	public final static String LIT_KEGG_INTER_MODULE_FILE = "getInterModLitKegg.props";
	public final static String KEGG_INTER_MODULE_FILE = "getInterModKegg.props";
	public final static String PPI_KEGG_LIT_INTER_MODULE_FILE = "getInterModLitPpiKegg.props";
	public final static String PPI_KEGG_INTER_MODULE_FILE = "getInterModPpiKegg.props";
	public final static String LIT_PPI_KEGG_INTER_MODULE_FILE = "getInterModLitPpiKegg.props";
	public final static String XML_BIF_MODULE_FILE = "prepareXMLBifMod.props";
	 
	/**
	 * Base Location where BN/LIT DB files reside
	 */
	private static String BASE_PATH = ".";
	
	/**
	 * Params
	 */
	public final static String ART_REM_THRESH_VAL = "2";	
	public final static String DIST_K_VAL = "3.0";
	
	public static void setBaseFileLocation(String path){
		BASE_PATH = path;
	}
	
	public static String getBaseFileLocation(){
		return BASE_PATH;
	}
}
