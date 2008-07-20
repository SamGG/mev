/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * ICGHDataRegionGeneData.java
 *
 * Created on December 26, 2002, 12:56 AM
 */

package org.tigr.microarray.mev.cgh.CGHDataObj;

import java.sql.ResultSet;
import java.util.Hashtable;
import java.util.Vector;

import org.tigr.microarray.mev.TMEV;
import org.tigr.microarray.mev.cgh.CGHUtil.CGHUtility;
import org.tigr.microarray.mev.cgh.DBObj.DSqlHandler;

/**
 *
 * @author  Adam Margolin
 * @author Raktim Sinha
 */

public class DataRegionGeneData implements IGeneDataSet{
    ICGHDataRegion dataRegion;
    Vector geneData;

    /** Creates a new instance of ICGHDataRegionGeneData */
    public DataRegionGeneData(ICGHDataRegion dataRegion) {
        this.dataRegion = dataRegion;
    }

    public void loadGeneData(int species){
    	String db = getDBName(species);
    	int regionSt = dataRegion.getStart() - 1000000;
    	if(regionSt <= 0) regionSt = 0;
    	int regionStop = dataRegion.getStop() + 1000000;
        String sql = "SELECT chrom, txStart, txEnd, name, locusLinkId FROM \""+db+"\"" +
                     " WHERE chrom = " + CGHUtility.encap(CGHUtility.convertChromToString(dataRegion.getChromosomeIndex() + 1, species)) +
                     " AND txStart >= " + regionSt + " AND txEnd <= " + regionStop +
                     " GROUP BY chrom, txStart, txEnd, name, locusLinkId";

        System.out.println("Show Genes Sql: " + sql);
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
    /**
     * Raktim
     * Modified Function to use FLat File based JDBC connection
     * Trial Version of Driver used.
     * Max queries = 20
     * Max Rows Fetched 5000
     * @param sql
     */
    private void loadGenes(String sql, int species){
        Hashtable uniqueGenes = new Hashtable();
        this.geneData = new Vector();
        DSqlHandler objPersist = new DSqlHandler();

        //ResultSet rs = objPersist.fetchItemsOracle(sql);
        ResultSet rs = objPersist.fetchItemsCSV(sql);
        /**
         * Remove Display Stmts later
         */
        /*
        try {
        for (int j = 1; j <= rs.getMetaData().getColumnCount(); j++) {
            System.out.print(rs.getMetaData().getColumnName(j) + "\t");
          }
          System.out.println();

          while (rs.next()) {
            for (int j = 1; j <= rs.getMetaData().getColumnCount(); j++) {
              System.out.print(rs.getObject(j) + "\t");
            }
            System.out.println();
          }
        } catch (Exception e) {}
        */
        //End Display Statements

        RefGeneLinkData curGeneData = null;
        try {
        	//this resultset is TYPE_FORWARD_ONLY
        	//if(!rs.isFirst()) rs.first();
            while(rs.next()){
                //int chromosome = CGHUtility.convertStringToChrom(rs.getString("chrom"));
                //int start = rs.getInt("txStart");
                //int stop = rs.getInt("txEnd");
                String geneName = rs.getString("name");
                IGeneData testGeneData = (IGeneData)uniqueGenes.get(geneName);
                //if(testGeneData == null || (testGeneData.getChromosomeIndex() + 1) != CGHUtility.convertStringToChrom(rs.getString("chrom")) ||
                //testGeneData.getStart() < start - 300 || testGeneData.getStop() > stop + 300){
                if(testGeneData == null) {
                	//System.out.println("Adding Gene Record");
                    curGeneData = new RefGeneLinkData();
                    curGeneData.populate(rs, species);
                    geneData.add(curGeneData);
                    uniqueGenes.put(curGeneData.getName(), curGeneData);
                }
            }
        } catch(Exception e){
            e.printStackTrace();
        }

    }

    /** Getter for property geneData.
     * @return Value of property geneData.
     */
    public java.util.Vector getGeneData() {
//    	Raktim
        //System.out.println("Vector geneData size: " + geneData.size());
        return geneData;
    }

    /** Setter for property geneData.
     * @param geneData New value of property geneData.
     */
    public void setGeneData(java.util.Vector geneData) {
        this.geneData = geneData;
    }

    /** Getter for property dataRegion.
     * @return Value of property dataRegion.
     */
    public ICGHDataRegion getDataRegion() {
        return dataRegion;
    }

    /** Setter for property dataRegion.
     * @param dataRegion New value of property dataRegion.
     */
    public void setDataRegion(ICGHDataRegion dataRegion) {
        this.dataRegion = dataRegion;
    }

}
