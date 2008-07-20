/* This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
package org.tigr.microarray.mev.cluster.gui.impl.bn.getInteractions;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.tigr.microarray.mev.cluster.gui.impl.bn.BNConstants;
import org.tigr.microarray.mev.cluster.gui.impl.bn.GetUnionOfInters;
import org.tigr.microarray.mev.cluster.gui.impl.bn.NullArgumentException;
import org.tigr.microarray.mev.cluster.gui.impl.bn.OutOfRangeException;
import org.tigr.microarray.mev.cluster.gui.impl.bn.Useful;
import org.tigr.microarray.mev.cluster.gui.impl.bn.UsefulInteractions;
import org.tigr.microarray.mev.cluster.gui.impl.bn.algs.AllPairsShortestPaths;
import org.tigr.microarray.mev.cluster.gui.impl.bn.algs.TransitiveClosure;
/**
 * The class <code>GetInteractionsModule</code> gets gene interactions from the literature or
 * from protein protein interactions (PPI) or both. If interactions are obtained from the literature,
 * one can obtain gene interactions from literature co-occurrences by combining 3 sources of articles 
 * (Resourcerer, Entrez Gene, Pubmed). If interactions are obtained from PPI, one can obtain interactions using
 * PPI directly and only the among the given set of genes, or directly with possibly other genes not in the 
 * given set of genes. In the latter case, PPI can be obtained between the nodes in the given set of genes 
 * and the nodes at distance K from them or from the nodes in the given set of genes and the nodes reachable from them
 *
 * @author <a href="mailto:amira@jimmy.harvard.edu"></a>
 */
public class GetInteractionsModule {
	public static boolean debug = false;
	static String sep = BNConstants.SEP;
	static String path;
	public GetInteractionsModule(String basepath){
		path=basepath;
		//System.out.println("Base PAth Loc....................: " + path);
	}
	/**
	 * The <code>getInteractionsFromLiterature</code> method gets interactions from co-occurrences 
	 * in the literature by combining 3 sources of articles (Resourcerer, Entrez Gene, Pubmed)
	 *
	 * @param props a <code>Properties</code> containing required properties such as resourcererFileName, 
	 * gbAccessionsFileName, symbolsArticlesFromPubmedFileName, symbolsArticlesFromGeneDbFileName,
	 * and some optional properties such as articleRemovalThreshold(default = 2)
	 * @return an <code>ArrayList</code> of <code>SimpleGeneEdge</code> objects corresponding to the union of 
	 * the gene interactions found from co-occurrences of genes in articles from Resourcerer, Entrez Gene and Pubmed
	 * @exception NullArgumentException if an error occurs if the given properties are null
	 * @exception OutOfRangeException if an error occurs if the given threshold is out of range 
	 * (any threshold less than 2 is out of range).
	 */

	public static ArrayList getInteractionsFromLiterature(Properties props) throws NullArgumentException, OutOfRangeException{
		try {
			if(props == null){
				throw new NullArgumentException("The given properties were null");
			}
			debug=true;
			//path=path+sep; //Raktim - Use tmp Dir
			String fileLoc=path+BNConstants.SEP+BNConstants.TMP_DIR+BNConstants.SEP;
			String resFileLoc = path+BNConstants.SEP;

			System.out.println("PATH Paiso: " + fileLoc);
			String resFileName = resFileLoc+props.getProperty(BNConstants.RES_FILE_NAME);

			//System.out.print(resFileName);
			String gbAccessionsFileName = fileLoc+props.getProperty(BNConstants.GB_ACC_FILE_NAME);
			//String gbAccessionsFileName = resFileLoc+props.getProperty("gbAccessionsFileName");
			String symbolsArticlesFromPubmedFileName = resFileLoc+props.getProperty(BNConstants.SYM_ARTICLES_FRM_PUBMED, null);
			String symbolsArticlesFromGeneDbFileName = resFileLoc+props.getProperty(BNConstants.SYM_ARTICLES_FRM_GENEDB, null);

			int articleRemovalThreshold = Integer.parseInt(props.getProperty(BNConstants.ART_REM_THRESH));
			HashMap gbSymbols = GetInteractionsUtil.getOfficialGeneSymbols(resFileName, gbAccessionsFileName);
			//System.exit(0);
			if(debug){
				Useful.writeHashMapToFile(gbSymbols, gbAccessionsFileName.substring(0, gbAccessionsFileName.length()-4)+"gbSymbols_test.txt");
			}
			HashMap gbGOs = GetInteractionsUtil.getGOs(resFileName, gbAccessionsFileName);
			if(debug){
				Useful.writeHashMapToFile(gbGOs, gbAccessionsFileName.substring(0, gbAccessionsFileName.length()-4)+"gbGOs_test.txt");
			}
			HashMap gbArticlesFromRes = GetInteractionsUtil.getResourcererArticles(resFileName, gbAccessionsFileName);
			if(debug){
				Useful.writeHashMapToFile(gbArticlesFromRes, gbAccessionsFileName.substring(0, gbAccessionsFileName.length()-4)+"gbArticlesFromRes_test.txt");
			}
			HashSet uniqueSymbols = GetInteractionsUtil.getUniqueSymbols(gbSymbols);
			HashMap allSymbolsArticlesFromPubmed = Useful.readHashMapFromFile(symbolsArticlesFromPubmedFileName);
			HashMap symbolsArticlesFromPubmed = GetInteractionsUtil.getSubsetSymbolsArticlesFromSymbolsArticles(uniqueSymbols, allSymbolsArticlesFromPubmed);      
			if(debug){
				Useful.writeHashMapToFile(symbolsArticlesFromPubmed, gbAccessionsFileName.substring(0, gbAccessionsFileName.length()-4)+"symbolsArticlesFromPubmed_test.txt");
			}
			HashMap allSymbolsArticlesFromGeneDb = Useful.readHashMapFromFile(symbolsArticlesFromGeneDbFileName);
			HashMap symbolsArticlesFromGeneDb = GetInteractionsUtil.getSubsetSymbolsArticlesFromSymbolsArticles(uniqueSymbols, allSymbolsArticlesFromGeneDb);
			if(debug){
				Useful.writeHashMapToFile(symbolsArticlesFromGeneDb, gbAccessionsFileName.substring(0, gbAccessionsFileName.length()-4)+"symbolsArticlesFromGeneDb_test.txt");
			}
			HashMap gbArticlesFromPubmed = GetInteractionsUtil.replaceSymbolsWithGbsInSymbolsArticles(gbSymbols, symbolsArticlesFromPubmed);
			if(debug){
				Useful.writeHashMapToFile(gbArticlesFromPubmed, gbAccessionsFileName.substring(0, gbAccessionsFileName.length()-4)+"gbArticlesFromPubmed_test.txt");
			}
			HashMap gbArticlesFromGeneDb = GetInteractionsUtil.replaceSymbolsWithGbsInSymbolsArticles(gbSymbols, symbolsArticlesFromGeneDb);
			if(debug){
				Useful.writeHashMapToFile(gbArticlesFromGeneDb, gbAccessionsFileName.substring(0, gbAccessionsFileName.length()-4)+"gbArticlesFromGeneDb_test.txt");
			}
			
			GeneInteractions gI = new GeneInteractions();
			
			HashMap articlesGbs = gI.backwards(gbArticlesFromGeneDb);
			gbArticlesFromGeneDb = gI.filter(articlesGbs, gbArticlesFromGeneDb, articleRemovalThreshold);
			ArrayList interGeneDb = (ArrayList) gI.createInteractions(gbArticlesFromGeneDb);
			if(articlesGbs != null && gbArticlesFromGeneDb != null && interGeneDb != null){
				//if(articlesGbs.size() == 0)
					//System.out.println("articlesGbs size is 0");
				//else
					System.out.println("articlesGbs size is " + articlesGbs.size());
				//if (gbArticlesFromGeneDb.size() == 0)
					//System.out.println("gbArticlesFromGeneDb size is 0");
				//else
					System.out.println("gbArticlesFromGeneDb size is " + gbArticlesFromGeneDb.size());
				//if (interGeneDb.size() == 0)
					//System.out.println("interGeneDb size is 0");
				//else
					System.out.println("interGeneDb size is " + interGeneDb.size());
			} else {
				System.out.println("One of the vectors for GeneDB is null");
			}
			
			articlesGbs = null;
			articlesGbs = gI.backwards(gbArticlesFromPubmed);
			gbArticlesFromPubmed = gI.filter(articlesGbs, gbArticlesFromPubmed, articleRemovalThreshold);	
			ArrayList interPubmed = (ArrayList) gI.createInteractions(gbArticlesFromPubmed);
			if(articlesGbs != null && gbArticlesFromPubmed != null && interPubmed != null){
				//if(articlesGbs.size() == 0)
					//System.out.println("articlesGbs size is 0");
				//else
					System.out.println("articlesGbs size is " + articlesGbs.size());
				//if (gbArticlesFromPubmed.size() == 0)
					//System.out.println("gbArticlesFromPubmed size is 0");
				//else
					System.out.println("gbArticlesFromPubmed size is " + gbArticlesFromPubmed.size());
				//if (interPubmed.size() == 0)
					//System.out.println("interPubmed size is 0");
				//else
					System.out.println("interPubmed size is " + interPubmed.size());
			} else {
				System.out.println("One of the vectors for Pubmed is null");
			}
			
			articlesGbs = null;
			articlesGbs = gI.backwards(gbArticlesFromRes);
			gbArticlesFromRes = gI.filter(articlesGbs, gbArticlesFromRes, articleRemovalThreshold);		
			ArrayList interRes = (ArrayList) gI.createInteractions(gbArticlesFromRes);	
			if(articlesGbs != null && gbArticlesFromRes != null && interRes != null){
				//if(articlesGbs.size() == 0)
					//System.out.println("articlesGbs size is 0");
				//else
					System.out.println("articlesGbs size is " + articlesGbs.size());
				//if (gbArticlesFromRes.size() == 0)
					//System.out.println("gbArticlesFromRes size is 0");
				//else
					System.out.println("gbArticlesFromRes size is " + gbArticlesFromRes.size());
				//if (interRes.size() == 0)
					//System.out.println("interRes size is 0");
				//else
					System.out.println("interRes size is " + interRes.size());
			} else {
				System.out.println("One of the vectors for Res is null");
			}
			
			debug=false;
			if(debug){
				UsefulInteractions.writeSifFileUndirWithWeights(interGeneDb,gbAccessionsFileName.substring(0, gbAccessionsFileName.length()-4)+"interGeneDb");
				UsefulInteractions.writeSifFileUndirWithWeights(interPubmed,gbAccessionsFileName.substring(0, gbAccessionsFileName.length()-4)+"interPubmed");
				UsefulInteractions.writeSifFileUndirWithWeights(interRes,gbAccessionsFileName.substring(0, gbAccessionsFileName.length()-4)+"interRes");	    
			}
			
			System.out.println("******* Merging interGeneDb, interRes, interPubmed ********");
			ArrayList unionOfInter = GetInteractionsUtil.uniquelyMergeArrayLists(interGeneDb, interRes, interPubmed);
			if(unionOfInter == null || unionOfInter.size() == 0){
				//TODO
				return new ArrayList();
			}
			//System.out.println("Edges Before KEGG " + unionOfInter.size());
			//for(int i = 0; i < unionOfInter.size(); i++){
				//System.out.println(unionOfInter.get(i).toString());
			//}
			//Raktim - New function to remove reverse edges between 2 nodes (cycles) from Lit mining interaction.
			//E.g - if there is an edge A -> B, there *cannot be an Edge B -> A to make it a DAG
			//unionOfInter = UsefulInteractions.removeReverseEdge(unionOfInter);
			unionOfInter = UsefulInteractions.removeReverseEdge(unionOfInter);
			return unionOfInter;
		}
		catch(IOException ioe){
			System.out.println(ioe);
		}
		return null;
	}


	/**
	 * The <code>getInteractionsFromPpi</code> method gets protein protein interactions from given properties
	 *
	 * @param props a <code>Properties</code> containing required properties such as ppiFileName, resourcererFileName,
	 * gbAccessionsFileName, and some optional properties such as usePpiDirectly (default = true), usePpiOnlyWithin 
	 * (default = true), useTransitiveClosure (default = false), distanceK (default = 3)
	 * @return an <code>ArrayList</code> corresponding to the protein protein interactins obtained 
	 * according to the optional properties: if usePpiDirectly, the ppi are returned directly from the set of unique symbols
	 * associated with the given GenBank accessions and the given protein protein interactions file name. 
	 * <br>
	 * If usePpiOnlyWithin, the ppi returned edges between nodes among symbols included in the set of unique symbols
	 * corresponding to the given GenBank accessions, otherwise, they may include other symbols not included 
	 * in the set of unique symbols corresponding to the given GenBank accessions. 
	 * <br>
	 * If useTransitiveClosure, the ppi returned contain edges between nodes in the set of unique symbols 
	 * corresponding to the given GenBank accessions and nodes reachable from these nodes 
	 * in the given protein protein interactions file, otherwise, the ppi returned contain edges
	 * between nodes in the set of unique symbols corresponding to the given GenBank accesions
	 * and nodes at distanceK from these nodes in the given protein protein interactions file.
	 * @exception NullArgumentException if an error occurs if the given properties are null
	 */

	public static ArrayList getInteractionsFromPpi(Properties props) throws NullArgumentException {	
		try {
			if(props == null){
				throw new NullArgumentException("The given properties were null");
			}
			// get props of ppi list, gbs, res
			//path=path+sep;
			String fileLoc=path+BNConstants.SEP+BNConstants.TMP_DIR+BNConstants.SEP;
			String resFileLoc = path+BNConstants.SEP;
			//System.out.print(path);
			String ppiFileName = resFileLoc+props.getProperty(BNConstants.PPI_FILE_NAME, null);
			String resFileName = resFileLoc+props.getProperty(BNConstants.RES_FILE_NAME, null);
			String gbAccessionsFileName = fileLoc+props.getProperty(BNConstants.GB_ACC_FILE_NAME, null);
			//Useful.checkFile(ppiFileName);
			//Useful.checkFile(resFileName);
			//Useful.checkFile(gbAccessionsFileName);
			// convert to unique symbols
			//System.exit(1);
			HashMap gbSymbols = GetInteractionsUtil.getOfficialGeneSymbols(resFileName, gbAccessionsFileName);
			System.out.println("gbSymbol "+gbSymbols.size());  
			//System.exit(1);
			HashSet uniqueSymbols = GetInteractionsUtil.getUniqueSymbols(gbSymbols);
			System.out.println("uniSymbol "+uniqueSymbols.size());
			//System.exit(1);
			ArrayList queryNodes = new ArrayList();
			queryNodes.addAll(uniqueSymbols);
			ArrayList ppi = UsefulInteractions.readInteractions(ppiFileName);
			System.out.println("# PPI Interaction: "+uniqueSymbols.size());
			//System.exit(1);
			//return getInteractionsFromPpi(ppi,queryNodes,props);
			ArrayList ppiInterActions = getInteractionsFromPpi(ppi,queryNodes,props);
			//Raktim - New function to remove reverse edges between 2 nodes (cycles) from Lit mining interaction.
			//E.g - if there is an edge A -> B, there *cannot be an Edge B -> A to make it a DAG
			//ppiInterActions = UsefulInteractions.removeReverseEdge(ppiInterActions);
			return ppiInterActions;
		}
		catch(IOException ioe){
			System.out.println(ioe);
			ioe.printStackTrace();
		}
		catch(NullArgumentException nae){
			System.out.println(nae);
			nae.printStackTrace();
		}
		catch(OutOfRangeException oore){
			System.out.println(oore);
			oore.printStackTrace();
		}
		return null;
	}
	/*
    public static ArrayList getInteractionsFromPpi(Properties props) throws NullArgumentException {	
    	try {
    	    if(props == null){
    		throw new NullArgumentException("The given properties were null");
    	    }
    	    // get props of ppi list, gbs, res
    	    // path=GetInteractionParemeterPPIDialog.path;
    	    String ppiFileName = props.getProperty("ppiFileName", null);
    	    String resFileName = props.getProperty("resourcererFileName", null);
    	    String gbAccessionsFileName = props.getProperty("gbAccessionsFileName", null);
    	    Useful.checkFile(ppiFileName);
    	    Useful.checkFile(resFileName);
    	    Useful.checkFile(gbAccessionsFileName);
    	    // convert to unique symbols
    	    HashMap gbSymbols = GetInteractionsUtil.getOfficialGeneSymbols(resFileName, gbAccessionsFileName);
    	    HashSet uniqueSymbols = GetInteractionsUtil.getUniqueSymbols(gbSymbols);
    	    ArrayList queryNodes = new ArrayList();
    	    queryNodes.addAll(uniqueSymbols);
    	    ArrayList ppi = UsefulInteractions.readInteractions(ppiFileName);
    	    return getInteractionsFromPpi(ppi,queryNodes,props); 
    	}
    	catch(IOException ioe){
    	    System.out.println(ioe);
    	    ioe.printStackTrace();
    	}
    	catch(NullArgumentException nae){
    	    System.out.println(nae);
    	    nae.printStackTrace();
    	}
    	catch(OutOfRangeException oore){
    	    System.out.println(oore);
    	    oore.printStackTrace();
    	}
    	return null;
        }
	 */
	/**
	 * The <code>getInteractionsFromPpi</code> method gets protein protein interactions from
	 * given <code>ArrayList</code> representing ppi, given <code>ArrayList</code> representing 
	 * nodes to query and given properties
	 *
	 * @param ppi an <code>ArrayList</code> of <code>SimpleGeneEdge</code> objects representing ppi interactions
	 * @param queryNodes an <code>ArrayList</code> of <code>String</code>s representing official gene symbols
	 * @param props a <code>Properties</code> containing some optional properties such as 
	 * usePpiDirectly (default = true), usePpiOnlyWithin (default = true), useTransitiveClosure (default = false),
	 * distanceK (default = 3.0)
	 * @return an <code>ArrayList</code> corresponding to the protein protein interactins obtained 
	 * according to the optional properties: if usePpiDirectly, the ppi are returned directly 
	 * from the set of unique symbols associated with the given GenBank accessions 
	 * and the given protein protein interactions file name. 
	 * <br>
	 * If usePpiOnlyWithin, the ppi returned edges between nodes among symbols included 
	 * in the set of unique symbols corresponding to the given GenBank accessions, 
	 * otherwise, they may include other symbols not included in the set of unique symbols 
	 * corresponding to the given GenBank accessions. 
	 * <br>
	 * If useTransitiveClosure, the ppi returned contain edges between nodes in the set of unique symbols
	 * corresponding to the given GenBank accessions and nodes reachable from these nodes 
	 * in the given protein protein interactions file, otherwise, the ppi returned contain edges 
	 * between nodes in the set of unique symbols corresponding to the given GenBank accesions 
	 * and nodes at distanceK from these nodes in the given protein protein interactions file.
	 * @exception NullArgumentException if an error occurs if the given properties are null
	 * @exception OutOfRangeException if an error occurs if distanceK parameter is out of bounds (namely negative)
	 */

	public static ArrayList getInteractionsFromPpi(ArrayList ppi, ArrayList queryNodes, Properties props) throws NullArgumentException, OutOfRangeException{	
		if(ppi == null || queryNodes == null){
			System.out.println("getInteractionsFromPpi");	
			throw new NullArgumentException("At least one of ppi or queryNodes was null\nppi="+ppi+"\nqueryNodes="+queryNodes);
		}
		//path=GetInteractionParemeterPPIDialog.path;
		// get props whether to construct ppi directly from subset	
		// or from all pairs shortest paths algorithm, or from transitive closure algorithm
		//path=path+sep;
		String usePpiDirectlyStr = props.getProperty(BNConstants.USE_PPI_DIRECT, "true");
		String usePpiOnlyWithinStr = props.getProperty(BNConstants.USE_PPI_WITHIN, "true");
		String useTransitiveClosureStr = props.getProperty(BNConstants.USE_TRANSITIVE_CLOSURE, "false");
		double distanceK = Double.parseDouble(props.getProperty(BNConstants.DIST_K, BNConstants.DIST_K_VAL));
		if(distanceK < 0){
			throw new OutOfRangeException("DistanceK is out of range (should be positive or equal to zero)!\ndistanceK="+distanceK);
		}
		ArrayList ppiIner = null;
		if(usePpiDirectlyStr.equals("true")){
			if(usePpiOnlyWithinStr.equals("true")){
				//System.exit(1);
				//return UsefulInteractions.getSubsetInteractionsGivenNodesOnlyWithin(ppi, queryNodes);
				ppiIner = UsefulInteractions.getSubsetInteractionsGivenNodesOnlyWithin(ppi, queryNodes);
			}
			else {
				//System.exit(1);
				//return UsefulInteractions.getSubsetInteractionsGivenNodes(ppi, queryNodes);
				ppiIner = UsefulInteractions.getSubsetInteractionsGivenNodes(ppi, queryNodes);
			}
		}
		else {
			if(useTransitiveClosureStr.equals("true")){
				//return TransitiveClosure.getInteractionsWithReachableNodes(ppi, queryNodes);
				ppiIner = TransitiveClosure.getInteractionsWithReachableNodes(ppi, queryNodes);
			}
			else {		
				//return AllPairsShortestPaths.getInteractionsWithNodesAtDistanceK(ppi, queryNodes, distanceK);
				ppiIner = AllPairsShortestPaths.getInteractionsWithNodesAtDistanceK(ppi, queryNodes, distanceK);
			}
		}
		if(ppiIner == null)
			System.out.println("GetInteractionsModule.getInteractionsFromPpi() NO interactions!!");
		else
			System.out.println("GetInteractionsModule.getInteractionsFromPpi() " +  ppiIner.size() + " interaction");
		return ppiIner;
	}

	/**
	 * getInteractionsFromKegg
	 * @param props
	 * @return
	 * @throws NullArgumentException
	 */
	public static ArrayList getInteractionsFromKegg(Properties props) throws NullArgumentException {	
		try {
			if(props == null){
				throw new NullArgumentException("The given properties were null");
			}
			String fileLoc=path+BNConstants.SEP+BNConstants.TMP_DIR+BNConstants.SEP;
			//String resFileLoc = path+BNConstants.SEP;

			System.out.println("PATH Kegg Paiso: " + fileLoc);
			//String resFileName = resFileLoc+props.getProperty("resourcererFileName");

			//System.out.print(resFileName);
			String gbAccessionsFileName = fileLoc+props.getProperty(BNConstants.GB_ACC_FILE_NAME);
			String kegg_sp = fileLoc+props.getProperty(BNConstants.KEGG_SPECIES).trim();
			// Code to load ALL Kegg Interactions
			//ArrayList keggListAll = GetInteractionsUtil.loadKeggInteractions("hsa", path+BNConstants.SEP);
			//TODO
			//Remove hard coded organism name
			ArrayList keggListAll = GetInteractionsUtil.loadKeggInteractions(kegg_sp,BNConstants.SEP+BNConstants.KEGG_FILE_BASE+BNConstants.SEP);
			// Find INteractions that corresponds to the Data
			ArrayList keggList = GetInteractionsUtil.getEdgesfromKegg(keggListAll, gbAccessionsFileName);
			System.out.println("Edges From KEGG " + keggList.size());
			//for(int i = 0; i < keggList.size(); i++){
				//System.out.println(keggList.get(i).toString());
			//}

			//Raktim - New function to remove reverse edges between 2 nodes (cycles) from Lit mining interaction.
			//E.g - if there is an edge A -> B, there *cannot be an Edge B -> A to make it a DAG
			//unionOfInter = UsefulInteractions.removeReverseEdge(unionOfInter);
			keggList = UsefulInteractions.removeReverseEdge(keggList);
			return keggList;
		}
		catch(NullArgumentException nae){
			System.out.println(nae);
			nae.printStackTrace();
		}
		return null;
	}

	/**
	 * getInteractionsFromLiteratureKegg
	 * @param props
	 * @return
	 * @throws NullArgumentException
	 */
	public static ArrayList getInteractionsFromLiteratureKegg(Properties props) throws NullArgumentException {
		try {
			if(props == null){
				throw new NullArgumentException("The given properties were null");
			}
			
			ArrayList unionLitKegg = null;
			ArrayList kegg = getInteractionsFromKegg(props);
			ArrayList lit = getInteractionsFromLiterature(props);
			if(kegg == null || kegg.size() == 0) {
				System.out.println("No valid KEGG interactions found");
				if(lit == null || lit.size() == 0) {
					System.out.println("No valid LIT interactions found");
					System.out.println("No valid KEGG-LIT interactions found");
					return null;
				} else {
					//KEGG is null but LIT is valid
					unionLitKegg = GetUnionOfInters.uniquelyMergeArrayLists(new ArrayList(), lit);
					unionLitKegg = UsefulInteractions.removeReverseEdge(unionLitKegg);
					//return unionLitKegg;
				}
			} else {
				//KEGG is valid but LIT is not
				if(lit == null || lit.size() == 0) {
					System.out.println("No valid LIT interactions found");
					unionLitKegg = GetUnionOfInters.uniquelyMergeArrayLists(kegg, new ArrayList());
					unionLitKegg = UsefulInteractions.removeReverseEdge(unionLitKegg);
					//return unionLitKegg;
				} else {
					//KEGG is valid and LIT is valid
					unionLitKegg = GetUnionOfInters.uniquelyMergeArrayLists(kegg, lit);
					unionLitKegg = UsefulInteractions.removeReverseEdge(unionLitKegg);
					//return unionLitKegg;
				}
			}
			
			System.out.println("\nEdges From KEGG & LIT " + unionLitKegg.size());
			//for(int i = 0; i < unionLitKegg.size(); i++){
				//System.out.println(unionLitKegg.get(i).toString());
			//}
			return unionLitKegg;
		}
		catch(OutOfRangeException oore){
			System.out.println(oore);
			oore.printStackTrace();
		}
		catch(NullArgumentException nae){
			System.out.println(nae);
			nae.printStackTrace();
		}
		return null;
	}
	/*
    public static ArrayList getInteractionsFromPpi(ArrayList ppi, ArrayList queryNodes, Properties props) throws NullArgumentException, OutOfRangeException{	
    	if(ppi == null || queryNodes == null){
    	    throw new NullArgumentException("At least one of ppi or queryNodes was null\nppi="+ppi+"\nqueryNodes="+queryNodes);
    	}
    	//path=GetInteractionParemeterPPIDialog.path;
    	// get props whether to construct ppi directly from subset	
    	// or from all pairs shortest paths algorithm, or from transitive closure algorithm
    	String usePpiDirectlyStr = props.getProperty("usePpiDirectly", "true");
    	String usePpiOnlyWithinStr = props.getProperty("usePpiOnlyWithin", "true");
    	String useTransitiveClosureStr =props.getProperty("useTransitiveClosure", "false");
            double distanceK = Double.parseDouble(props.getProperty("distanceK", "3.0"));
    	if(distanceK < 0){
    	    throw new OutOfRangeException("DistanceK is out of range (should be positive or equal to zero)!\ndistanceK="+distanceK);
    	}
    	if(usePpiDirectlyStr.equals("true")){
    	    if(usePpiOnlyWithinStr.equals("true")){
    		return UsefulInteractions.getSubsetInteractionsGivenNodesOnlyWithin(ppi, queryNodes);
    	    }
    	    else {
    		return UsefulInteractions.getSubsetInteractionsGivenNodes(ppi, queryNodes);
    	    }
    	}
    	else {
    	    if(useTransitiveClosureStr.equals("true")){
    		return TransitiveClosure.getInteractionsWithReachableNodes(ppi, queryNodes);
    	    }
    	    else {		
    		return AllPairsShortestPaths.getInteractionsWithNodesAtDistanceK(ppi, queryNodes, distanceK);
    	    }
    	}
        }
	 */
	/**
	 * The <code>getInteractions</code> method gets interactions from the given properties, 
	 * corresponding to interactions obtained either from the literature by co-occurrences 
	 * of genes from articles using 3 sources (Resourcerer, Entrez Gene, Pubmed), 
	 * from the protein protein interactions data or from both.
	 *
	 * @param props a <code>Properties</code> containing optional properties
	 * such as fromLiterature (default = true) and fromPpi (default = false).
	 * <br>
	 * If fromLiterature, required properties are:
	 * <ul>
	 * <li> resourcererFileName denoting the name of the Resourcerer file
	 * <li> gbAccessionsFileName denoting the name of the file containing GenBank accessions of interest
	 * <li> symbolsArticlesFromPubmedFileName denoting the name of the file containing the official gene symbols
	 * and their corresponding articles from Pubmed in tab-delimited format: gene_1\tarticle_1,article_2,...,article_n     
	 * <li> symbolsArticlesFromGeneDbFileName denoting the name of the file containing the official gene symbols
	 * and their corresponding articles from Entrez Gene database in tab-delimited format: 
	 * gene_1\tarticle_1,article_2,...,article_n  
	 * </ul>
	 * and some optional properties such as articleRemovalThreshold which corresponds to a number such that articles 
	 * that are associated with greater than threshold number of genes will be removed.
	 * Possible values of this threshold are any integer >= 2. The default is 2.
	 * <br> 
	 * If fromPpi, required properties are:
	 * <ul>
	 * <li> ppiFileName denoting the name of the file containing protein protein interactions (PPI) in undirected 
	 * Cytoscape SIF format:  "node1 pp node2"
	 * <li> resourcererFileName denoting the name of the Resourcerer file
	 * <li> gbAccessionsFileName denoting the name of the file containing GenBank accessions of interest
	 * </ul>
	 * <br>
	 * and some optional properties such as 
	 * <ul>
	 * <li> usePpiDirectly denoting a flag according to which if it is true, the ppi are returned directly 
	 * from the set of unique symbols associated with the given GenBank accessions and the given PPI file name.
	 * The default is true.
	 * <li> usePpiOnlyWithin denoting a flag according to which if it is true, the resulting ppi edges are between
	 * nodes among symbols included in the set of unique symbols corresponding to the given GenBank accessions,
	 * otherwise, they may include other symbols not included 
	 * in the set of unique symbols corresponding to the given GenBank accessions. The default is true. 
	 * <li> useTransitiveClosure denoting a flag according to which if is true, the ppi returned contain edges
	 * between nodes in the set of unique symbols corresponding to the given GenBank accessions and nodes 
	 * reachable from these nodes in the given PPI file. The default is false.
	 * <li> distanceK denoting a flag according to which if is true, the ppi returned contain edges 
	 * between nodes in the set of unique symbols corresponding to the given GenBank accesions 
	 * and nodes at distanceK from these nodes in the given PPI file. The default is 3.
	 * <li> newGbAccessionsFileName name of the file where the new GenBank accessions are to be written
	 * (in the case if usePpiOnlyWithin is false OR usePpiDirectly is false). The default is "newGBs.txt".
	 * </ul>
	 * @return an <code>ArrayList</code>  of <code>SimpleGeneEdge</code> objects. 
	 * <br>
	 * If fromLiterature, this <code>ArrayList</code> corresponds to the union of the gene interactions 
	 * found from co-occurrences of genes in articles from Resourcerer, Entrez Gene and Pubmed
	 * @exception NullArgumentException if an error occurs if the given properties are null
	 * @exception OutOfRangeException if an error occurs if fromLiterature=true and if article removal threshold 
	 * is provided in the given properties and is less than 2 OR if fromPpi=true and if useTransitiveClosure=false 
	 * and distanceK parameter is out of bounds (namely negative)
	 */
	public static ArrayList getInteractions(Properties props) throws NullArgumentException, OutOfRangeException {
		if(props == null){
			throw new NullArgumentException("The given properties were null");
		}
		//System.out.println(props);
		String isLiteratureStr = props.getProperty(BNConstants.FRM_LIT, "true").trim();
		String isPpiStr = props.getProperty(BNConstants.FRM_PPI, "false").trim();
		String isKeggStr = props.getProperty(BNConstants.FRM_KEGG, "false").trim();

		ArrayList interFromLit = null;
		ArrayList interFromPpi = null;	
		ArrayList interFromPpiSyms = null;	
		ArrayList interFromKegg = null;	

		// get interactions from all -  kegg, literature and ppi
		if(isLiteratureStr.equals("true") && isPpiStr.equals("true") && isKeggStr.equals("true")){
			//TODO
			return null;
		}
		// get interactions from both literature and ppi NOT kegg
		else if(isLiteratureStr.equals("true") && isPpiStr.equals("true") && !isKeggStr.equals("true")){
			System.out.println("Only Lit & PPI");
			interFromPpiSyms = getInteractionsFromPpi(props);
			System.out.println("getInteractions()interFromPpiSyms Size: " + interFromPpiSyms.size());
			// replace symbols with gbs in interFromPpi
			interFromPpi = GetInteractionsUtil.replaceSymsWithGBsInInter(path + BNConstants.SEP+props.getProperty(BNConstants.RES_FILE_NAME,null), interFromPpiSyms);
			if(props.getProperty(BNConstants.USE_PPI_WITHIN, "true").equals("false")|| props.getProperty(BNConstants.USE_PPI_DIRECT, "true").equals("false")){
				prepareGBsForPpiNotDirectly(interFromPpi, props);	
				interFromLit = getInteractionsFromLiterature(props);
				return GetUnionOfInters.uniquelyMergeArrayLists(interFromLit, interFromPpi);
			}
			else { // usePpiDirectlyOnlyWithin was true
				interFromLit = getInteractionsFromLiterature(props);
				return GetUnionOfInters.uniquelyMergeArrayLists(interFromLit, interFromPpi);
			}
		}
		// get interactions from both literature and kegg and NOT ppi
		else if(isLiteratureStr.equals("true") && !isPpiStr.equals("true") && isKeggStr.equals("true")){
			System.out.println("Only KEGG & LIT");
			ArrayList keggAndLitInterActions = null;
			keggAndLitInterActions = getInteractionsFromLiteratureKegg(props);
			return keggAndLitInterActions;
		}
		// get interactions from both kegg and ppi and NOT literature 
		else if(!isLiteratureStr.equals("true") && isPpiStr.equals("true") && isKeggStr.equals("true")){
			//TODO
			System.out.println("Only KEGG & PPI");
			return null;
		}
		// get interactions from literature but NOT from ppi & kegg
		else if(isLiteratureStr.equals("true") && !isPpiStr.equals("true") && !isKeggStr.equals("true")){
			interFromLit = getInteractionsFromLiterature(props);
			System.out.println("Only LIT");
			//System.exit(1);
			return interFromLit;
		}
		// get interactions from ppi but not from literature and Kegg
		else if(isPpiStr.equals("true") && !isLiteratureStr.equals("true") && !isKeggStr.equals("true")){	
			System.out.println("Only PPI");
			interFromPpiSyms = getInteractionsFromPpi(props);
			//System.out.println(interFromPpiSyms.size());
			//System.exit(1);
			// replace symbols with gbs in interFromPpi
			interFromPpi = GetInteractionsUtil.replaceSymsWithGBsInInter(path  + BNConstants.SEP + props.getProperty(BNConstants.RES_FILE_NAME), interFromPpiSyms);
			//System.exit(1);
			if(props.getProperty(BNConstants.USE_PPI_WITHIN, "true").equals("false") || props.getProperty(BNConstants.USE_PPI_DIRECT, "true").equals("false")){
				prepareGBsForPpiNotDirectly(interFromPpi, props);	
			}
			return interFromPpi;
		}
		//	 get interactions from only  kegg and  and NOT ppi and literature 
		else if(!isLiteratureStr.equals("true") && !isPpiStr.equals("true") && isKeggStr.equals("true")){
			System.out.println("Only KEGG");
			interFromKegg = getInteractionsFromKegg(props);
			return interFromKegg;
		}
		else {
			throw new NullArgumentException("At least one of fromPpi or fromLiterature was neither true nor false!\nfromPpi="+isPpiStr+"\nfromLiterature="+isLiteratureStr);
		}
	}

	/**
	 * The <code>prepareGBsForPpiNotDirectly</code> method takes in a given graph of ppi, and given properties 
	 * and gets the new GenBank accessions for the official gene symbols obtained from not directly getting ppi
	 * in Resourcerer file.
	 * <br>
	 * If there are such new GenBank accessions, renames the original gbAccessionsFileName 
	 * to "original_"+gbAccessionsFileName
	 * writes their union with the original GenBank accessions to gbAccessionsFileName 
	 * so that it's ready for <code>getInteractionsFromLiterature</code> method
	 *
	 * @param interFromPpi an <code>ArrayList</code> value
	 * @param props a <code>Properties</code> containing optional properties for the case of getInteractions where
	 * both properties fromLiterature and fromPpi are true and usePpiDirectly is false
	 * <br>
	 * Required properties for literature are resourcererFileName, gbAccessionsFileName,
	 * symbolsArticlesFromPubmedFileName, symbolsArticlesFromGeneDbFileName, 
	 * and some optional properties such as articleRemovalThreshold (default = 2).
	 * <br> 
	 * Required properties for ppi are ppiFileName, resourcererFileName, gbAccessionsFileName, 
	 * and some optional properties in this case are:
	 * useTransitiveClosure (default = false), distanceK (default = 3)
	 * If fromLiterature, this <code>ArrayList</code> corresponds to the union of the gene interactions 
	 * found from co-occurrences of genes in articles from Resourcerer, Entrez Gene and Pubmed
	 */
	public static void prepareGBsForPpiNotDirectly(ArrayList interFromPpi, Properties props){
		try {
			ArrayList newGeneSymbols = null;
			HashSet origGBs = null;
			ArrayList newGBs = null;
			HashSet uniqueNewGBs = null;
			HashSet origAndUniqueNewGBs = new HashSet();
			Iterator it = null;
			String gbAccessionsFileName = path+props.getProperty(BNConstants.GB_ACC_FILE_NAME,null);
			String newGbAccessionsFileName = path+props.getProperty(BNConstants.NEW_GB_ACC_FILE_NAME,BNConstants.NEW_ACCESSION_FILE);
			String toWrite = "";
			origGBs = Useful.readUniqueNamesFromFile(gbAccessionsFileName);
			newGBs = UsefulInteractions.getNodes(interFromPpi);
			//getAccessions and write them to file
			if(newGBs != null && newGBs.size() != 0){
				origAndUniqueNewGBs.addAll(origGBs);
				origAndUniqueNewGBs.addAll(newGBs);
				it = origAndUniqueNewGBs.iterator();
				while(it.hasNext()){
					toWrite += it.next()+"\n";
				}
				Useful.writeStrToFile(toWrite, newGbAccessionsFileName);
			}
		}
		catch(FileNotFoundException fnfe){
			System.out.println(fnfe);
		}
		catch(NullArgumentException nae){
			System.out.println(nae);
		}
	}

	/**
	 * The <code>test</code> method test the GetInteractionsModule using the parameters in the given properties file
	 * (see <code>getInteractions</code> method for more on properties) and writes the result to the outInteractionsFileName 
	 * if provided in the properties (default = "outInteractions.txt")
	 *
	 * @param propertiesFileName a <code>String</code> corresponding to the given properties file
	 * containing optional properties such as fromLiterature (default = true) and fromPpi (default = false).
	 * <br>
	 * If fromLiterature, required properties are resourcererFileName, gbAccessionsFileName, 
	 * symbolsArticlesFromPubmedFileName, symbolsArticlesFromGeneDbFileName, 
	 * and some optional properties such as articleRemovalThreshold (default = 2).
	 * <br>
	 * If fromPpi, required properties are ppiFileName, resourcererFileName, gbAccessionsFileName, 
	 * and some optional properties such as usePpiDirectly (default = true), usePpiOnlyWithin (default = true),
	 * useTransitiveClosure (default = false), distanceK (default = 3)
	 */
	public static int test(String propertiesFileName){
		try {
			//System.out.print(propertiesFileName);
			//System.exit(1);
			Properties props = new Properties();
			props.load(new FileInputStream(propertiesFileName));
			System.out.print(props.getProperty(BNConstants.RES_FILE_NAME));
			ArrayList interactions = getInteractions(props); //Magic Happens Here !!!
			if(interactions == null || interactions.size() == 0){
				System.out.print("Oh no NULL Interaction object. Bad...");
				JOptionPane.showMessageDialog(
						new JFrame() , 
						"No interactions found, aborting ....",
						"Info", JOptionPane.INFORMATION_MESSAGE);
				return -1;
			}
			String outInteractionsFileName = props.getProperty(BNConstants.OUT_INTER_FILE_NAME, BNConstants.OUT_INTERACTION_FILE);
			//System.out.print(outInteractionsFileName);
			//Raktim - Modified. Name File(s) uniquely
			//String fname_cyto= "liter_mining_alone_network.sif"; // Raktim - Old Way
			String fname_cyto= Useful.getUniqueFileID() +"_"+ "liter_mining_alone_network.sif";
			//System.out.println("fname_cyto " + fname_cyto);
			System.setProperty("LM_ONLY", fname_cyto);
			UsefulInteractions.writeSifFileUndir(interactions, fname_cyto);	
			UsefulInteractions.writeSifFileUndirWithWeights(interactions, outInteractionsFileName);
			return interactions.size();
		}
		catch(IOException ioe){
			System.out.println(ioe);
			ioe.printStackTrace();
			return -1;
		}

		catch(NullArgumentException nae){
			System.out.println(nae);
			nae.printStackTrace(); 
			return -1;
		}
		
		catch(OutOfRangeException oore){
			System.out.println(oore);
			oore.printStackTrace();
			return -1;
		}
	}

	/**
	 * The <code>usage</code> method displays usage.
	 *
	 */
	public static void usage(){
		System.out.println("java GetInteractionsModule propsFileName\njava GetInteractionsModule getInteractions.props");
		System.exit(0);
	}
	public static void main(String[] argv){
		if(argv.length !=1){
			usage();
		}
		String propsFileName = argv[0];
		//System.out.print(propsFileName);
		test(propsFileName);
	}
}
