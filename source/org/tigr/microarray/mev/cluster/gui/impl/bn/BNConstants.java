/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
package org.tigr.microarray.mev.cluster.gui.impl.bn;

import java.util.Hashtable;


public class BNConstants {
	
	/**
	 * Cytoscape jnlp file Name
	 */
	public final static String CYTOSCAPE_URI = "mycyto.jnlp";
	
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
	public final static String OUT_XML_BIF_FILE_FINAL = "out_bif_final.xml";
	
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
	public final static String OUT_XML_BIF_FILE_FINAL_NAME = ""; //TODO
    public final static String SIF_FILE_NAME = "sifFileName";
    
    public final static String KEGG_SPECIES = "kegg_sp";
	
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
	 * BN TMEV properties name that points to last used file location
	 */
	public static String BN_LM_LOC_PROP = "bn_lm_loc_prop";
	/**
	 * Base Location where BN/LIT DB files reside
	 * BN TMEV properties as well
	 */
	private static String BN_LM_BASE_PATH = ".";
	
	/**
	 * Params
	 */
	public final static String ART_REM_THRESH_VAL = "2";	
	public final static String DIST_K_VAL = "3.0";
	
	public static void setBaseFileLocation(String path){
		BN_LM_BASE_PATH = path;
	}
	
	public static String getBaseFileLocation(){
		return BN_LM_BASE_PATH;
	}
	
	/**
	 * KEGG base location for interactions
	 */
	public final static String KEGG_FILE_BASE = System.getProperty("user.dir") + BNConstants.SEP + "data" + BNConstants.SEP + "BN_files" + BNConstants.SEP + "kegg";

	/**
	 * Maximum number of genes allowed for BN analysis.
	 * It sets the max size of the transpose matrix in Transpose.java as well.
	 * This variable corresponds to the max length of matrix in either dimension.
	 */
	public static final int MAX_GENES = 250;
	
	/**
	 * List of organisims that currently has data for KEGG priors support
	 */
	public static final String KEGG_ORG[] = new String[]{"Human", "Mouse", "Rat" };

	public static final String BN_NET_SEED_LOC_PROP = "bn_net_seed_loc_prop";
	
	/**
	 * Cytoscape webstart params and getters & setters
	 */
	private static String CODE_BASE = "";
	private static String LIB_DIR = "";
	private static String PLUGINS_DIR = "";
	private static boolean CODE_BASE_SET = false;
	private static boolean LIB_DIR_SET = false;
	private static boolean PLUGINS_DIR_SET = false;
	
	public static void setCodeBaseLocation(String path){
		CODE_BASE = path;
		CODE_BASE_SET = true;
	}
	
	public static String getCodeBaseLocation(){
		return CODE_BASE;
	}
	
	public static void setLibDirLocation(String path){
		LIB_DIR = path;
		LIB_DIR_SET = true;
	}
	
	public static String getLibDirLocation(){
		return LIB_DIR;
	}
	
	public static boolean isSetCytoscapeParams() {
		return CODE_BASE_SET | LIB_DIR_SET | PLUGINS_DIR_SET;
	}

	public static String gePluginsDirLocation() {
		return PLUGINS_DIR;
	}
	
	public static void setPluginsDirLocation(String path){
		PLUGINS_DIR = path;
		PLUGINS_DIR_SET = true;
	}
}
