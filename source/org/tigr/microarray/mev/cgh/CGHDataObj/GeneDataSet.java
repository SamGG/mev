/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * GeneDataSet.java
 *
 * Created on December 27, 2002, 3:02 AM
 */

package org.tigr.microarray.mev.cgh.CGHDataObj;

import java.sql.ResultSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import org.tigr.microarray.mev.TMEV;
import org.tigr.microarray.mev.cgh.CGHUtil.CGHUtility;
import org.tigr.microarray.mev.cgh.DBObj.DSqlHandler;

/**
 * @author  Adam Margolin
 * @author Raktim Sinha
 */

public class GeneDataSet implements IGeneDataSet {
    private Vector geneData;
    /** Creates a new instance of GeneDataSet */
    public GeneDataSet() {
    }

    public void loadGeneDataByGeneName(String geneName, int species){
        String db = getDBName(species);
        String sql = "SELECT chrom, txStart, txEnd, name, locusLinkId FROM \""+db+"\"" +
        " GROUP BY chrom, txStart, txEnd, name, locusLinkId HAVING UPPER(name)= " + CGHUtility.encap(geneName.toUpperCase());

        System.out.println(sql);

        loadGenes(sql, species);
    }

    public void loadGeneDataByGeneNames(Vector geneNames, int species){
    	String db = getDBName(species);
        String strGeneNames = "";
        Iterator it = geneNames.iterator();

        if(it.hasNext()){
            strGeneNames += CGHUtility.encap( ((String) it.next()).toUpperCase() );
        }

        while(it.hasNext()){
            strGeneNames += ", " + CGHUtility.encap( ((String) it.next()).toUpperCase() );
        }

        String sql = "SELECT chrom, txStart, txEnd, name, locusLinkId FROM \""+db+"\"" +
        " GROUP BY chrom, txStart, txEnd, name, locusLinkId HAVING UPPER(name) IN (" + strGeneNames + ")";

        System.out.println(sql);

        loadGenes(sql, species);
    }

    public void loadAllGenes(int species){
    	String db = getDBName(species);
        String sql = "select chrom, txStart, txEnd, name, locusLinkId from \""+db+"\"" +
        " GROUP BY chrom, txStart, txEnd, name, locusLinkId";

        System.out.println(sql);

        loadGenes(sql, species);
    }

    private String getDBName(int species){
    	String dbName = "";
    	switch(species) {
    	case TMEV.CGH_SPECIES_HS:
    		dbName = "Hs_RefGenesMapped";
    		break;
    	case TMEV.CGH_SPECIES_MM:
    		dbName = "Mm_RefGenesMapped";
    		break;
    	}
    	return dbName;
    }

    private void loadGenes(String sql, int species){
        Hashtable uniqueGenes = new Hashtable();
        this.geneData = new Vector();
        DSqlHandler objPersist = new DSqlHandler();

        //ResultSet rs = objPersist.fetchItemsOracle(sql);
        ResultSet rs = objPersist.fetchItemsCSV(sql);
        RefGeneLinkData curGeneData = null;
        try{
            while(rs.next()){
                int chromosome = CGHUtility.convertStringToChrom(rs.getString("chrom"), species);
                //int start = rs.getInt("txStart");
                //int stop = rs.getInt("txEnd");
                String geneName = rs.getString("name");

                if(chromosome != CGHClone.NOT_FOUND && geneName != null){
                    IGeneData testGeneData = (IGeneData)uniqueGenes.get(geneName);
                    //if(testGeneData == null || (testGeneData.getChromosomeIndex() + 1) != CGHUtility.convertStringToChrom(rs.getString("chrom")) ||
                    //testGeneData.getStart() < start - 300 || testGeneData.getStop() > stop + 300){
                    if(testGeneData == null) {
                        curGeneData = new RefGeneLinkData();
                        curGeneData.populate(rs, species);
                        geneData.add(curGeneData);
                        uniqueGenes.put(curGeneData.getName(), curGeneData);
                    }
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public Vector getGeneData() {
        return geneData;
    }

    public void setGeneData(Vector geneData){
        this.geneData = geneData;
    }

}
