/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * cytoBands.java
 *
 * Created on January 23, 2003, 5:47 PM
 */

package org.tigr.microarray.mev.cgh.CGHDataObj;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 *
 * @author  Adam Margolin
 * @author Raktim Sinha
 */

public class CytoBands {

    /** Holds value of property cytoBands. */
    private Vector[] cytoBands = new Vector[24];

    /** Creates a new instance of cytoBands */
    public CytoBands() {

        for(int i = 0; i < cytoBands.length; i++){
            cytoBands[i] = new Vector();
        }
    }

    /** Getter for property cytoBands.
     * @return Value of property cytoBands.
     */
    public Vector[] getCytoBands() {
        return this.cytoBands;
    }

    public Vector getDataElementsAt(int chromosomeIndex){
        return this.cytoBands[chromosomeIndex];
    }

    /** Setter for property cytoBands.
     * @param cytoBands New value of property cytoBands.
     */
    public void setCytoBands(Vector[] cytoBands) {
        this.cytoBands = cytoBands;
    }
    /*
    public void loadAllCytoBands(){

        DSqlHandler objPersist = new DSqlHandler();

        String sql = "select * from \"Mapping_31\".dbo.tblCytoBand";

        ResultSet rs = objPersist.fetchItems(sql);
        CytoBand curCytoBand = null;
        Vector allCytoBands = new Vector();
        try{
            while(rs.next()){
                curCytoBand = new CytoBand();
                curCytoBand.populate(rs);
                allCytoBands.add(curCytoBand);
            }

            Iterator it = allCytoBands.iterator();
            while(it.hasNext()){
                curCytoBand = (CytoBand)it.next();
                if(curCytoBand.getChromosome() > 0){
                    cytoBands[curCytoBand.getChromosome() - 1].add(curCytoBand);
                }
            }

        }catch(Exception e){
            e.printStackTrace();
        }
    }
     */

    public void loadAllCytoBands(File file, int species){
        try{
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            Vector allCytoBands = new Vector();
            CytoBand curCytoBand = null;
            while((line = reader.readLine()) != null){
                StringTokenizer st = new StringTokenizer(line, "\t");
                String chromosome = st.nextToken();
                int chromStart = Integer.parseInt(st.nextToken());
                int chromEnd = Integer.parseInt(st.nextToken());
                String name = st.nextToken();
                String stain = st.nextToken();

                curCytoBand = new CytoBand(chromosome, chromStart, chromEnd, name, stain, species);
                allCytoBands.add(curCytoBand);
            }
            reader.close();

            Iterator it = allCytoBands.iterator();
            while(it.hasNext()){
                curCytoBand = (CytoBand)it.next();
                if(curCytoBand.getChromosome() > 0){
                    cytoBands[curCytoBand.getChromosome() - 1].add(curCytoBand);
                }
            }


        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
