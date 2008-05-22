/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * $RCSfile: JEASEStatistics.java,v $
 * $Revision: 1.8 $
 * $Date: 2007-12-05 22:18:32 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
/* 
 * Developed by NIAID LTB Lab.
 */
/*
 * Modified at TIGR for use within MeV v. 2.2 and later.
 */
package org.tigr.microarray.mev.cluster.algorithm.impl.ease;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

public class JEASEStatistics {
    
    /** the name of the population file */
    public String population_file_name = "";
    
    /** the name of the input file that contains a list of gene identifiers. */
    public String list_file_name = "";
    
    /** the name of the file which the result will be written to. */
    public String output_file_name;
    
    /** a vector that contains names of the annotation files. */
    public Vector<String> annotation_file_names = new Vector<String>();
    
    /** the total number of genes that are imported from a file and belong to categories in the annotation files */
    public int population_total;
    
    /** the total number of genes from the sample list that belong to categories in the annotation files. */
    public int list_total;
    
    /** the categories from the annotation files*/
    public Hashtable<String, Hashtable<String, String>> categories = new Hashtable<String, Hashtable<String, String>>();
    
    /** categories and hits in each of the categories for the population  */
    public Hashtable<String, String> categories_population = new Hashtable<String, String>();
    
    /** categories and hits in each of the categories for the sample  */
    public Hashtable<String, String> categories_list = new Hashtable<String, String>();
    
    /** stores category names and associated locus id hits as java.lang.String in java.util.Vector */
    public Hashtable<String, Vector<String>> hitAccumulator = new Hashtable<String, Vector<String>>(); //jcb
    
    /** An instance of HypergeometricProbability class. */
    public HypergeometricProbability hgp_computation = new HypergeometricProbability();
    
    /** A BufferedReader object used to read an external file. */
    public BufferedReader read_in_stream;
    
    /** a two dimension array to store the result */
    public String[][] records;
    
    /** Category names */  //jcb
    public String [] categoryNames;
    
    /** Category list hits */
    public String [][] listHitMatrix;  //jcb
    
    /** the order in which the results should be listed. */
    public String[] orders;
    
    /** indicates statistic to report */
    public boolean reportEaseScore = true; //jcb
    
    /** accumulates totals for various systems  **/
    private Hashtable<String, Hashtable<String, String>> pop_totals = new Hashtable<String, Hashtable<String, String>>();
        
    /** accumulates totals for various systems  **/
    private Hashtable<String, Hashtable<String, String>> sample_totals = new Hashtable<String, Hashtable<String, String>>();
    
    /** OS file separator **/
    private String sep;
    
    /**  Constructor */
    public JEASEStatistics() {
        population_total = 0;
        list_total =0;
        sep = System.getProperty("file.separator");
    }
    
    /**  Constructor */ //jcb
    public JEASEStatistics(boolean reportEaseScore) {
        this.reportEaseScore = reportEaseScore;
        population_total = 0;
        list_total =0;
        sep = System.getProperty("file.separator");
    }
    
    /** Choose a new population file
     * @param file_name file name
     */
    public void UseNewPopulationFileName(String file_name) {
        population_file_name =  file_name;
    }
    
    /** Remove all of the annotation files from the list currently being used. */
    public void RemoveCurrentAnnotationFileNames() {
        annotation_file_names.removeAllElements();
    }
    
    /** Add a new annotation file name into the annotation file name list. */
    public void AddAnnotationFileName(String file_name) {
        annotation_file_names.addElement(file_name);
    }
    
    /** Use a new list file name. */
    public void UseNewListFileName(String file_name) {
        list_file_name = file_name;
    }
    
    /** set the name of the file that stores the results.*/
    public void SetOutputFileName(String file_name){
        output_file_name = file_name;
    }
    
    /**
     * Obtain the categories from the annotation files and create a hashtable 
     * using these categories as keys.
     * 
     */
    public void GetCategories() {
        BufferedReader in = null;
        Hashtable<String, Vector<String>> implied_associations = new Hashtable<String, Vector<String>>();
        String term;
        String line="", category="", file_name="";
        int idx, idx2;        
        
        try{
            for(Enumeration e = annotation_file_names.elements(); e.hasMoreElements();){
                file_name = e.nextElement().toString();
                in = new BufferedReader(new FileReader(file_name));
                
                //use last index of . in case a user has a . in the path.
                term = file_name.substring(file_name.lastIndexOf(sep)+1, file_name.lastIndexOf("."));
                
                //store terms in total hits accumulators
                this.sample_totals.put(term, new Hashtable<String, String>());
                this.pop_totals.put(term, new Hashtable<String, String>());
                
                while((line = in.readLine()) != null){
                    
                    idx = line.indexOf("\t");
                    
                    if( idx >= line.length() || idx < 1){
                        continue;
                    }
                    
                    //put this in to guard against no trailing tab (jcb)
                    idx2 = line.indexOf("\t", idx+1);
                    if(idx2 >= line.length() || idx2 < 1)
                        idx2 = line.length();
                    
                    category = term + "\t" + line.substring(idx+1, idx2).trim();
                    
                    if(!categories.containsKey(category)) {
                        categories.put(category, new Hashtable<String, String>());
                        (categories.get(category)).put(line.substring(0,idx).trim(), "");
                    }else{
                        (categories.get(category)).put(line.substring(0,idx).trim(), "");
                    }
                }
            }
            
            //(jcb)
            //create hash table for implies using (implies_associator)
            //This will then be used to add implied categories
            String fileName, impliesFile;
            int stringIndex;
            for(int i = 0; i < annotation_file_names.size(); i++){
                fileName = (String)annotation_file_names.elementAt(i);
                stringIndex = fileName.lastIndexOf(sep);
                impliesFile = fileName.substring(0, stringIndex) + sep+"Implies"+sep;
                impliesFile += fileName.substring(stringIndex+1, fileName.length());
                
                //System.out.println("implies file = "+impliesFile);
                
                File file = new File(impliesFile);
                if(!file.exists() || !file.isFile())  //if implies file is missing move on
                    continue;
                
                in = new BufferedReader(new FileReader(impliesFile));
                //term = fileName.substring(file_name.lastIndexOf("/")+1, file_name.indexOf("."));
                term = fileName.substring(fileName.lastIndexOf(sep)+1, fileName.lastIndexOf("."));
                
                
                while((line = in.readLine()) != null){
                    idx = line.indexOf('\t');
                    
                    if(idx >= line.length() || idx < 1)  //must include a tab
                        continue;
                    
                    if(!implied_associations.containsKey(term + "\t" +line.substring(0,idx).trim())){
                        implied_associations.put(term + "\t" +line.substring(0,idx).trim(), new Vector<String>());
                        ((implied_associations.get(term + "\t" +line.substring(0,idx).trim()))).addElement(term + "\t" + line.substring(idx, line.length()).trim());
                    } else {
                        ((implied_associations.get(term + "\t" +line.substring(0,idx).trim()))).addElement(term + "\t" + line.substring(idx, line.length()).trim());
                    }
                }
            }
            
            boolean end = false;
            //(jcb) append associated categories to the list
            for(int k = 0; k < 10 && !end; k++){  // !end will short circuit if stable
                String cat="";
                Hashtable<String, String> catHash;
                Vector impVector;
                String impCat;

                end = true;  //start at true until no new indices are inserted
                
                for( Enumeration<String> _enum = implied_associations.keys(); _enum.hasMoreElements(); ){
                    
                    cat = _enum.nextElement();
                    
                    //if the category is represented, add the implied associations if they don't exist
                    if(categories.containsKey(cat)){
                        catHash = categories.get(cat);
                        
                        //associated categories
                        impVector = ((Vector)implied_associations.get(cat));
                        
                        for(int i = 0; i < impVector.size(); i++){
                            
                            impCat = ((String)impVector.elementAt(i));
                            
                            if(!categories.containsKey(impCat)){
                                end = false;
                                categories.put(impCat, new Hashtable<String, String>());
                                for(Enumeration<String> categoryEnum = catHash.keys(); categoryEnum.hasMoreElements();){
                                    (categories.get(impCat)).put(categoryEnum.nextElement(), "");
                                }
                            } else {  //category exists, need to append locus link numbers
                                for(Enumeration categoryEnum = catHash.keys(); categoryEnum.hasMoreElements();){
                                    String indexString = (String)categoryEnum.nextElement();
                                    if(!(categories.get(impCat)).containsKey(indexString) ){
                                        // ((Hashtable)categories.get(impCat)).put((String)categoryEnum.nextElement(), "");
                                        (categories.get(impCat)).put(indexString, "");
                                        
                                        end = false;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
        }catch(Exception error){
            System.out.println("Error occured collecting categories");
            System.out.println(error.getMessage());
            error.printStackTrace();
        }
    }

    
    /**
     * Obtain the categories from the annotation files and create a hashtable using 
     * these categories as keys.
     */
    public void GetCategories(Vector popVector) {
        BufferedReader in = null;
        Hashtable<String, Vector<String>> implied_associations = new Hashtable<String, Vector<String>>();
        String term;
        String line="", category="", file_name="";
        int idx, idx2;        
        
        try{
            for(Enumeration e = annotation_file_names.elements(); e.hasMoreElements();){
                file_name = e.nextElement().toString();
                in = new BufferedReader(new FileReader(file_name));
                
                //use last index of . in case a user has a . in the path.
                term = file_name.substring(file_name.lastIndexOf(sep)+1, file_name.lastIndexOf("."));
                
                //store terms in total hits accumulators
                this.sample_totals.put(term, new Hashtable<String, String>());
                this.pop_totals.put(term, new Hashtable<String, String>());
                
                while((line = in.readLine()) != null){
                    
                    idx = line.indexOf("\t");
                    
                    if( idx >= line.length() || idx < 1){
                        continue;
                    }
                    
                    //put this in to guard against no trailing tab (jcb)
                    idx2 = line.indexOf("\t", idx+1);
                    if(idx2 >= line.length() || idx2 < 1)
                        idx2 = line.length();
                    
                    category = term + "\t" + line.substring(idx+1, idx2).trim();
                    if(popVector.contains(line.substring(0,idx).trim())) {
                        if(!categories.containsKey(category)){
                            categories.put(category, new Hashtable<String, String>());
                            (categories.get(category)).put(line.substring(0,idx).trim(), "");
                        }else{
                            (categories.get(category)).put(line.substring(0,idx).trim(), "");
                        }
                    }
                }
            }
                        
            //(jcb)
            //create hash table for implies using (implies_associator)
            //This will then be used to add implied categories
            String fileName, impliesFile;
            int stringIndex;
            for(int i = 0; i < annotation_file_names.size(); i++){
                fileName = (String)annotation_file_names.elementAt(i);
                stringIndex = fileName.lastIndexOf(sep);
                impliesFile = fileName.substring(0, stringIndex) + sep+"Implies"+sep;
                impliesFile += fileName.substring(stringIndex+1, fileName.length());
                 
                File file = new File(impliesFile);
                if(!file.exists() || !file.isFile())  //if implies file is missing move on
                    continue;
                
                in = new BufferedReader(new FileReader(impliesFile));
                //term = fileName.substring(file_name.lastIndexOf("/")+1, file_name.indexOf("."));
                term = fileName.substring(fileName.lastIndexOf(sep)+1, fileName.lastIndexOf("."));
                
                
                while((line = in.readLine()) != null){
                    idx = line.indexOf('\t');
                    
                    if(idx >= line.length() || idx < 1)  //must include a tab
                        continue;
                    
                    if(!implied_associations.containsKey(term + "\t" +line.substring(0,idx).trim())){
                        implied_associations.put(term + "\t" +line.substring(0,idx).trim(), new Vector<String>());
                        (implied_associations.get(term + "\t" +line.substring(0,idx).trim())).addElement(term + "\t" + line.substring(idx, line.length()).trim());
                    } else {
                        (implied_associations.get(term + "\t" +line.substring(0,idx).trim())).addElement(term + "\t" + line.substring(idx, line.length()).trim());
                    }
                }
            }
            
            boolean end = false;
            //(jcb) append associated categories to the list
            for(int k = 0; k < 10 && !end; k++){  // !end will short circuit if stable
                String cat="";
                Hashtable<String, String> catHash;
                Vector impVector;
                String impCat;

                end = true;  //start at true until no new indices are inserted
                
                for( Enumeration<String> _enum = implied_associations.keys(); _enum.hasMoreElements(); ){
                    
                    cat = _enum.nextElement();
                    
                    //if the category is represented, add the implied associations if they don't exist
                    if(categories.containsKey(cat)){
                        catHash = categories.get(cat);
                        
                        //associated categories
                        impVector = ((Vector)implied_associations.get(cat));
                        
                        for(int i = 0; i < impVector.size(); i++){
                            
                            impCat = ((String)impVector.elementAt(i));
                            
                            if(!categories.containsKey(impCat)){
                                end = false;
                                categories.put(impCat, new Hashtable<String, String>());
                                for(Enumeration<String> categoryEnum = catHash.keys(); categoryEnum.hasMoreElements();){
                                    (categories.get(impCat)).put((String)categoryEnum.nextElement(), "");
                                }
                            } else {  //category exists, need to append locus link numbers
                                for(Enumeration<String> categoryEnum = catHash.keys(); categoryEnum.hasMoreElements();){
                                    String indexString = categoryEnum.nextElement();
                                    if(!(categories.get(impCat)).containsKey(indexString) ){
                                        // ((Hashtable)categories.get(impCat)).put((String)categoryEnum.nextElement(), "");
                                        (categories.get(impCat)).put(indexString, "");
                                        
                                        end = false;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
        }catch(Exception error){
            System.out.println("Error occured collecting categories");
            System.out.println(error.getMessage());
            error.printStackTrace();
        }
    }
    
    /**
     * Returns a list of gene indices that correspond to the functional class "term". Used by the 
     * Nested EASE resampling functions.
     * @param term the term to find the matching genes for
     * @return the list of gene indices
     */
    public Vector<String> getSubPopulationForCategory(String term) {
        String key="";
        Hashtable<String, String> hash_table = new Hashtable<String, String>();
        Vector<String> returnVector = new Vector<String>();
        
        System.out.println("Testing for term " + term);
        
        //for each annotation category (such as go terms)
        for(Enumeration<String> _enum = categories.keys(); _enum.hasMoreElements();){
            key = _enum.nextElement();
            hash_table = (Hashtable<String, String>)categories.get(key);
        	
        	if(key.equals(term)) {
        		//System.out.println("getting category values for key " + key);
        		for(Enumeration<String> e = hash_table.keys(); e.hasMoreElements();) {
	            	String categoryKey = e.nextElement();
	            	System.out.println(" value: " + categoryKey);
	            	returnVector.add(categoryKey);
	            }
        	}
        }
        return returnVector;
    }    
    
    
    /** Get the number of the genes in the population for each category that exists in the annotation files. */
    public void GetPopulationHitsByCategory() {
        BufferedReader in = null;
        String line="", hits="", key="", locus_id;
        Hashtable<String, String> locus_ids = new Hashtable<String, String>();
        Hashtable<String, String> hash_table = new Hashtable<String, String>();
        Hashtable<String, String> count_ids = new Hashtable<String, String>();
        
        try{
        	//Read in population file full of locuslink ids
            in = new BufferedReader(new FileReader(population_file_name));
            
            while((line = in.readLine()) != null){
                locus_ids.put(line.trim(), "");
            }
            
            //For each gene (locuslink id) in the population file
            for(Enumeration enum1 = locus_ids.keys(); enum1.hasMoreElements();) {
                locus_id = (String)(enum1.nextElement());
                
                //for each annotation category (such as go terms)
                for(Enumeration _enum = categories.keys(); _enum.hasMoreElements();){
                    key = (String)(_enum.nextElement());
                    hash_table = categories.get(key);
                    
                    if(hash_table.containsKey(locus_id)){
                        if(!categories_population.containsKey(key)){
                            categories_population.put(key, "1");
                        }else{
                            hits = String.valueOf(Integer.parseInt((String) categories_population.get(key)) + 1);
                            categories_population.put(key, hits);
                        }
                        
                        count_ids.put(locus_id, "");
                    }
                }
            }
            
            population_total = count_ids.size();
            
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }
    
    /** Get the number of the genes in the sample for each category that exists in the annotation files. */
    public void GetListHitsByCategory() {
        BufferedReader in = null;
        String line="", hits="", key="", locus_id;
        Hashtable<String, String> locus_ids = new Hashtable<String, String>();
        Hashtable hash_table = new Hashtable();
        Hashtable<String, String> count_ids = new Hashtable<String, String>();
        
        try{
            in = new BufferedReader(new FileReader(list_file_name));
            
            while((line = in.readLine()) != null){
                locus_ids.put(line.trim(), "");
            }
            
            for(Enumeration<String> enum1 = locus_ids.keys(); enum1.hasMoreElements();){
                locus_id = (String)(enum1.nextElement());
                for(Enumeration _enum = categories.keys(); _enum.hasMoreElements();){
                    key = (String)(_enum.nextElement());
                    hash_table = (Hashtable) (categories.get(key));
                    
                    if(hash_table.containsKey(locus_id)){
                        if(!categories_list.containsKey(key)){
                            categories_list.put(key, "1");
                        }else{
                            hits = String.valueOf(Integer.parseInt((String) (categories_list.get(key))) + 1);
                            categories_list.put(key, hits);
                        }
                        count_ids.put(locus_id, "");
                    }
                }
            }
            
            list_total = count_ids.size();
            
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }
    
    
    /** Creates the tab-delimited results containing information on system, category, population hits, population total, sample hits
     *  and sample totoal, and the one-tailed hypergeometric probability score of over-representation.
     */
    public void ConstructResults() {
        
        String key="", list_hit="", population_hit="", temp="";
        records = new String[categories_list.size()][7];
        orders = new String[categories_list.size()];
        
        int c=0;
        
        for(Enumeration e = categories_list.keys(); e.hasMoreElements();){
            key = (String)(e.nextElement());
            list_hit="";
            population_hit="";
            
            temp = key.substring(0, key.indexOf('\t')).trim();
            records[c][0] = temp.substring(temp.lastIndexOf(sep)+1).trim();
            records[c][1] = key.substring(key.indexOf('\t')).trim();
            
            list_hit = (String)(categories_list.get(key));
            
            records[c][2] = list_hit;
            
            list_total = ((Hashtable)(sample_totals.get(temp))).size();
            
            records[c][3] = String.valueOf(list_total);
            
            if(categories_population.containsKey(key)){
                population_hit = (String)(categories_population.get(key));
                records[c][4] = population_hit;
            }else{
                records[c][4] = "";
            }
            
            population_total = (pop_totals.get(temp)).size();
            
            records[c][5] = String.valueOf(population_total);
            
            if(!list_hit.equals("") && !population_hit.equals("")){
                double p;
                if(reportEaseScore){  //report ease score (jcb) added option of EASEScore
                    if(Integer.parseInt(list_hit) > 1){
                        p = hgp_computation.SumHGP(population_total, Integer.parseInt(population_hit), list_total-1, Integer.parseInt(list_hit)-1);
                    } else{
                        p = 1.0;
                    }
                } else {  //else report Fisher Exact Prob.
                    p = hgp_computation.SumHGP(population_total, Integer.parseInt(population_hit), list_total, Integer.parseInt(list_hit));
                }
                records[c][6] = String.valueOf(p);
            }else{
                records[c][6] = "1.0";
            }
            c++;
        }
        
        //jcb
        //accumulates hits for the result matrix and the category names
        categoryNames = new String[hitAccumulator.size()];
        listHitMatrix = new String[categoryNames.length][];
        
        int cnt = 0;
        Vector locusVector;
        
        for(Enumeration e = hitAccumulator.keys(); e.hasMoreElements();){
            String cat = (String)(e.nextElement());
            categoryNames[cnt] = cat;
            locusVector = ((Vector)hitAccumulator.get(cat));
            listHitMatrix[cnt] = new String[locusVector.size()];
            for(int i = 0; i < listHitMatrix[cnt].length; i++){
                listHitMatrix[cnt][i] = ((String)locusVector.elementAt(i));
            }
            cnt++;
        }
    }
    
    
    
    /** Sort the results in the ascending order of p-score and write the tab-delimited result to a file and standard IO output*/
    public void SortRecords() {
        
        BigDecimal big1, big2;
        String holder;
        
        int len = records.length;
        
        for(int i0=0; i0 < len; i0++){
            for(int i=0; i < len-1 - i0; i++){
                big1 = new BigDecimal(records[i][6]);
                big2 = new BigDecimal(records[i+1][6]);
                
                if(big1.compareTo(big2) > 0){
                    holder = records[i][0];
                    records[i][0] = records[i+1][0];
                    records[i+1][0] = holder;
                    
                    holder = records[i][1];
                    records[i][1] = records[i+1][1];
                    records[i+1][1] = holder;
                    
                    holder = records[i][2];
                    records[i][2] = records[i+1][2];
                    records[i+1][2] = holder;
                    
                    holder = records[i][3];
                    records[i][3] = records[i+1][3];
                    records[i+1][3] = holder;
                    
                    holder = records[i][4];
                    records[i][4] = records[i+1][4];
                    records[i+1][4] = holder;
                    
                    holder = records[i][5];
                    records[i][5] = records[i+1][5];
                    records[i+1][5] = holder;
                    
                    holder = records[i][6];
                    records[i][6] = records[i+1][6];
                    records[i+1][6] = holder;
                }
            }
        }
        //jcb, elected not to output file by default here.
        //DisplayResults();
    }
    
    /** Writes the tab-delimited results to the specified output file and to the standout IO output as well */
    public void DisplayResults() {
        try{
            File output_file = new File(output_file_name);
            FileWriter output_writer = new FileWriter(output_file); //, false);
            
            for(int i=0; i < records.length; i++){
                System.out.print(records[i][0] + "\t");
                output_writer.write(records[i][0] + "\t");
                
                System.out.print(records[i][1] + "\t");
                output_writer.write(records[i][1] + "\t");
                
                System.out.print(records[i][2] + "\t");
                output_writer.write(records[i][2] + "\t");
                
                System.out.print(records[i][3] + "\t");
                output_writer.write(records[i][3] + "\t");
                
                System.out.print(records[i][4] + "\t");
                output_writer.write(records[i][4] + "\t");
                
                System.out.println(records[i][5] + "\t");
                output_writer.write(records[i][5] + "\t");
                
                output_writer.write("\n");
            }
            output_writer.close();
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        
        JEASEStatistics jstatistics = new JEASEStatistics();
        System.out.println(new Date(System.currentTimeMillis()));
        
        jstatistics.UseNewPopulationFileName("C:/01_Work_Test_4/Files/JStatistics/All Homo sapiens.txt");
        jstatistics.RemoveCurrentAnnotationFileNames();
        jstatistics.AddAnnotationFileName("C:/01_Work_Test_4/Files/JStatistics/GO Biological Process.txt");
        jstatistics.UseNewListFileName("C:/01_Work_Test_4/Files/JStatistics/Bigger_HepC_chimp.txt");
        jstatistics.SetOutputFileName("C:/del.txt");
        
        jstatistics.GetCategories();
        jstatistics.GetListHitsByCategory();
        jstatistics.GetPopulationHitsByCategory();
        jstatistics.ConstructResults();
        jstatistics.SortRecords();
        
        System.out.println(new Date(System.currentTimeMillis()));
        System.out.println("\nCompleted.");
    }
    
    
    /***
     * (jcb)Modified Hit collection methods to accept a vector argument of annotation keys
     * Modification of code to enable integration into MeV Ease module.
     */
    
    /** Get the number of the genes in the population for each category that exists in the annotation files.
     * @param list population list.
     */
    public void GetPopulationHitsByCategory(Vector<String> list) {
        String  hits="", key="", locus_id;
        Hashtable<String, String> locus_ids = new Hashtable<String, String>();
        Hashtable<String, String> hash_table = new Hashtable<String, String>();
        Hashtable<String, String> count_ids = new Hashtable<String, String>();
        
        try{
            int size = list.size();
            for(int i = 0; i < size; i++)
                locus_ids.put(list.elementAt(i), "");
            
            for(Enumeration<String> enum1 = locus_ids.keys(); enum1.hasMoreElements();){
                locus_id = enum1.nextElement();
                
                for(Enumeration<String> _enum = categories.keys(); _enum.hasMoreElements();){
                    key = _enum.nextElement();
                    hash_table = categories.get(key);
                    
                    if(hash_table.containsKey(locus_id)){
                        if(!categories_population.containsKey(key)){
                            categories_population.put(key, "1");
                        }else{
                            hits = String.valueOf(Integer.parseInt(categories_population.get(key)) + 1);
                            categories_population.put(key, hits);
                        }
                        count_ids.put(locus_id, "");
                        
                        //accumulate the pop total hits
                        key = key.substring(0, key.indexOf("\t")).trim();
                        (pop_totals.get(key)).put(locus_id,"");
                    }
                }
            }
            population_total = count_ids.size();
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }
    
    /** Get the number of the genes in the sample for each category that exists 
     * in the annotation files.
     * @param list The list of locuslink accessions identifying the genes in the cluster list
     */
    public void GetListHitsByCategory(Vector<String> list) {
        String hits="", key="", locus_id;
        Hashtable<String, String> locus_ids = new Hashtable<String, String>();
        Hashtable<String, String> hash_table = new Hashtable<String, String>();
        Hashtable<String, String> count_ids = new Hashtable<String, String>();
        
        try {
            int size = list.size();
            for(int i = 0; i < size; i++)
                locus_ids.put(list.elementAt(i), "");
            
            //for each gene in the cluster list
            for(Enumeration<String> enum1 = locus_ids.keys(); enum1.hasMoreElements();){
                locus_id = enum1.nextElement();

                //for each category (GO Term or Kegg pathway or whatever annotation)
                for(Enumeration<String> _enum = categories.keys(); _enum.hasMoreElements();){
                    key = _enum.nextElement();
                    hash_table = categories.get(key);
                    
                    //If the current category includes the current gene
                    if(hash_table.containsKey(locus_id)){
                        if(!categories_list.containsKey(key)){
                            categories_list.put(key, "1");
                            //jcb
                            hitAccumulator.put(key, new Vector<String>());
                            hitAccumulator.get(key).add(locus_id);
                            //end mod
                        }else {
                            hits = String.valueOf(Integer.parseInt((String) (categories_list.get(key))) + 1);
                            categories_list.put(key, hits);
                            //jcb
                            hitAccumulator.get(key).add(locus_id);
                            //end mod
                        }
                        count_ids.put(locus_id, "");
                        
                        //accumulate the sample total hits
                        key = key.substring(0, key.indexOf("\t")).trim();
                        sample_totals.get(key).put(locus_id,"");
                    }
                }
            }
            list_total = count_ids.size();
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }
    
    String [][] getResults(){
        return this.records;
    }
    
    String [][] getSurveyResults(){
        return this.records;
    }
    
    String [][] getListHitMatrix(){
        return this.listHitMatrix;
    }
    
    String [] getCategoryNames(){
        return this.categoryNames;
    }
    
    /** Resets the list total and list hit accumulators.
     */    
    public void resetForNewList(){
        this.categories_list = new Hashtable<String, String>();
        this.sample_totals = new Hashtable<String, Hashtable<String, String>>();
        String file_name, term;
   
        for(Enumeration e = annotation_file_names.elements(); e.hasMoreElements();){
            file_name = e.nextElement().toString();
            term = file_name.substring(file_name.lastIndexOf(sep)+1, file_name.lastIndexOf("."));
            
            //store terms in total hits accumulators
            this.sample_totals.put(term, new Hashtable<String, String>());
        }
    }
    
    
    /***
     * Modified Hit collection methods to accept a vector argument of annotation keys
     * Modification of code to enable integration into TIGR MeV EASE module.
     */
    
    /** Get the number of the genes in the population for each category that exists in the annotation files.
     * @param list Input population list.
     */
    public void GetPopulationHitsByCategoryForSurvey(Vector<String> list) {
        String hits="", key="", locus_id;
        Hashtable<String, String> locus_ids = new Hashtable<String, String>();
        Hashtable<String, String> hash_table = new Hashtable<String, String>();
        Hashtable<String, String> count_ids = new Hashtable<String, String>();
        //jcb
        
        try{
            int size = list.size();
            for(int i = 0; i < size; i++)
                locus_ids.put(list.elementAt(i), "");
            
            for(Enumeration<String> enum1 = locus_ids.keys(); enum1.hasMoreElements();){
                locus_id = (String)(enum1.nextElement());
                
                for(Enumeration<String> _enum = categories.keys(); _enum.hasMoreElements();){
                    key = (String)(_enum.nextElement());
                    hash_table = categories.get(key);
                    
                    if(hash_table.containsKey(locus_id)){
                        if(!categories_population.containsKey(key)){
                            categories_population.put(key, "1");
                            
                            hitAccumulator.put(key, new Vector<String>());
                            (hitAccumulator.get(key)).add(locus_id);
                        }else{
                            hits = String.valueOf(Integer.parseInt(categories_population.get(key)) + 1);
                            categories_population.put(key, hits);
                            
                            (hitAccumulator.get(key)).add(locus_id);
                        }
                        count_ids.put(locus_id, "");
                        
                        //accumulate the pop total hits
                        key = key.substring(0, key.indexOf("\t")).trim();
                        (pop_totals.get(key)).put(locus_id,"");
                    }
                }
            }
            
            population_total = count_ids.size();
            
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }
    
    
    /** Creates the tab-delimited results containing information on system, category, population hits, population total, sample hits
     *  and sample totoal, and the one-tailed hypergeometric probability score of over-representation.
     */
    public void ConstructSurveyResults() {
        
        String key="", list_hit="", population_hit="", temp="";        
        
        records = new String[categories_population.size()][4];
        orders = new String[categories_population.size()];
        
        int c=0;
        
        for(Enumeration e = categories_population.keys(); e.hasMoreElements();){
            key = (String)(e.nextElement());
            list_hit="";
            population_hit="";
            
            temp = key.substring(0, key.indexOf('\t')).trim();
            records[c][0] = temp.substring(temp.lastIndexOf(sep)+1).trim();
            records[c][1] = key.substring(key.indexOf('\t')).trim();
            
            list_hit = (String)(categories_population.get(key));
            
            records[c][2] = list_hit;
            
            population_total = ((Hashtable)(pop_totals.get(temp))).size();
            
            records[c][3] = String.valueOf(population_total);
            
            c++;
        }
        
        //jcb
        //accumulates hits for the result matrix.
        categoryNames = new String[hitAccumulator.size()];
        listHitMatrix = new String[categoryNames.length][];
        
        int cnt = 0;
        Vector locusVector;
        
        for(Enumeration e = hitAccumulator.keys(); e.hasMoreElements();){
            String cat = (String)(e.nextElement());
            categoryNames[cnt] = cat;
            locusVector = ((Vector)hitAccumulator.get(cat));
            listHitMatrix[cnt] = new String[locusVector.size()];
            for(int i = 0; i < listHitMatrix[cnt].length; i++){
                listHitMatrix[cnt][i] = ((String)locusVector.elementAt(i));
            }
            cnt++;
        }
    }
    
}

