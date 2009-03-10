/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * Created on Jul 12, 2005
 *
 */
package org.tigr.microarray.mev.cluster.algorithm.impl.tease;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import org.tigr.microarray.mev.cluster.algorithm.impl.ease.HypergeometricProbability;

/**
 * TEASEBench contains all the methods required for GO term matching.
 * This class works like JEASEStatistics and is used by TEASE
 * 
 * @author Annie Liu
 * @version Jul 12, 2005
 */
public class TEASEBench {
	
//	private HashMap categoryMap;           
//	private HashMap categoryHitsMap;
//	private HashMap impliedCategory;
	
    public String population_file_name = "";  //name of the population file
    public String list_file_name = ""; //the name of the input file that contains a list of gene identifiers
    public String output_file_name;  //the name of the file which the result will be written to
    public Vector annotation_file_names;  //a vector that contains names of the annotation files.
    public int population_total;  //the total number of genes that are imported from a file and 
                                  //belong to categories in the annotation files
    public int list_total;  //the total number of genes from the sample list that belong 
                            //to categories in the annotation files
    public Hashtable categories;  //the categories from the annotation files
    public Hashtable categories_population;  //categories and hits in each of the 
    														   //categories for the population
    public Hashtable categories_list;  //categories and hits in each of the categories for the sample
    public Hashtable hitAccumulator; //stores category names and associated locus id hits 
                                     //as java.lang.String in java.util.Vector
    public HypergeometricProbability hgp_computation;  //An instance of HypergeometricProbability class
    public BufferedReader read_in_stream;  //A BufferedReader object used to read an external file
    public String[][] records;  // two dimension array to store the result
    public String [] categoryNames;  //ategory names
    public String [][] listHitMatrix;  //Category list hits
    public String[] orders;  //the order in which the results should be listed
    public boolean reportEaseScore; //indicates statistic to report
    private Hashtable pop_totals;  //accumulates totals for various systems 
    private Hashtable sample_totals;  //accumulates totals for various systems 
    private String impliesFileLocation;
	
    private String sep = //"/";  //os file separator
    	System.getProperty("file.separator");
	/**
	 * Create an instance of TEASEBench
	 * create all global variables
	 */
	public TEASEBench() {
//		this.categoryMap  = new HashMap();        //create the hashmap categoryMap
//		this.impliedCategory = new HashMap();
//		this.categoryHitsMap = new HashMap();

		initiation();
		this.pop_totals = new Hashtable();
		this.categories = new Hashtable();
		this.sample_totals = new Hashtable();
		this.annotation_file_names = new Vector();
		this.categories_population = new Hashtable();
        
        this.reportEaseScore = true;
	}
	
	public void initiation() {
		this.hitAccumulator = new Hashtable();
		this.hgp_computation = new HypergeometricProbability();
		
		this.population_total = 0;
        this.list_total =0;
	}
	
	/**
	 * alternative constructor that sets reportEaseScore
	 * @param reportEaseScore
	 */
	public TEASEBench(boolean reportEaseScore) {
		this();
        this.reportEaseScore = reportEaseScore;
	}
	
//	/**
//	 * Reads in the file containing annotation information and create
//	 * a hashmap that maps categories to locus IDs
//	 * key: category, value: ArrayList of locus IDs
//	 * @param filePath
//	 */
//	private HashMap getCategories(String fileName) {
//		String term = fileName.substring(fileName.lastIndexOf(sep)+1, fileName.lastIndexOf("."));
//		String line = "";
//		String id = "";
//		String cat = "";
//		int ind;
//		int in2;
//		try {
//			BufferedReader buff = new BufferedReader(new FileReader(fileName));
//			
//			while ((line = buff.readLine()) != null) {
//				//System.out.println(line);
//				ind = line.indexOf("\t");
//				if (ind == -1)
//					break;
//				id = line.substring(0, ind);
//				cat = line.substring(ind+1);
//				in2 = cat.indexOf("\t");
//				if (in2 == -1)
//					in2 = cat.length();
//				if (this.categoryMap.containsKey(id) == true) {       //if gene id already existed
//					((ArrayList)this.categoryMap.get(id)).add(term + "\t" + cat.substring(0,in2).trim());   //add the new category to the arraylist
//				}
//				else {                                           //if gene first appear
//					ArrayList catList = new ArrayList();         //create a new arraylist for it
//					catList.add(term + "\t" + cat.substring(0,in2).trim());    //add category in the arraylist
//					this.categoryMap.put(id, catList);                //put the <id, categories> set in the categoryMap
//				}
//			}
//		}catch (IOException e) {
//			e.printStackTrace();
//		}			
//		return this.categoryMap;
//	}
//	
//	/**
//	 * store implies category in a map using category (column 1) as key
//	 * implied categories (column 0) as value
//	 * <key, value> = <string, arraylist> 
//	 * @param filePath file path to the implies category
//	 * @return impliedCategory hashmap
//	 */
//	private HashMap getImpliedCategory (String fileName) {
//		String term = fileName.substring(fileName.lastIndexOf(sep)+1, fileName.lastIndexOf("."));
//		int stringIndex = fileName.lastIndexOf(sep);
//        String impliesFile = fileName.substring(0, stringIndex) + sep + "Implies" + sep;
//        impliesFile += fileName.substring(stringIndex+1, fileName.length());
//		String line = "";
//		String imp = "";
//		String cat = "";
//		int ind;
//		try {
//			BufferedReader buff = new BufferedReader(new FileReader(impliesFile));
//			while ((line = buff.readLine()) != null) {
//				ind = line.indexOf("\t");           //in case incomplete entry
//				if (ind == -1) 
//					break;
//				imp = term + "\t" + line.substring(0, ind);
//				cat = term + "\t" + line.substring(ind+1).trim();
//
//				if (this.impliedCategory.containsKey(cat) == true) {       //if category already exists
//					((ArrayList)this.impliedCategory.get(cat)).add(imp);   //add the new implied category to the arraylist
//				}
//				else {                                           //if category first appear
//					ArrayList impList = new ArrayList();         //create a new arraylist for it
//					impList.add(imp);                            //add implied category in the arraylist
//					this.impliedCategory.put(cat, impList);          //put the <category, implied categories> set in the categoryMap
//				}
//			}
//		}catch (IOException e) {
//			e.printStackTrace();
//		}	
//		return this.impliedCategory;
//	}
//	
//
//	/**
//	 * Search both the list of genes and find matching categories. 
//	 * Store all categories that has been hit at least once in categoryHitsMap.
//	 * value is an arraylist of genes
//	 *  
//	 * @param geneList
//	 * @return categoryHitsMap
//	 */
//	private HashMap getCategoryHitsMap(ArrayList geneList) {
//		String id;
//		String category;
//		String impcat;
//		ArrayList categoryList;
//		ArrayList impliedList;
//		for (int i = 0; i < geneList.size(); i++) {    //iterate through geneList
//			id = (String)geneList.get(i);
//			if (this.categoryMap.containsKey(id)) {         //if gene is listed in categoryMap
//				categoryList = (ArrayList)this.categoryMap.get(id);    //pull out all the categories it belongs to
//				
//				for (int j = 0; j < categoryList.size(); j++) {
//					category = (String)categoryList.get(j);
//					checkAndAddCategory(category, id);             //check if category already exist in hitMap
//					if (this.impliedCategory.containsKey(category)) {
//						impliedList = (ArrayList)this.impliedCategory.get(category);
//						for(int k = 0; k < impliedList.size(); k++) {
//							impcat = (String)impliedList.get(k);
//							checkAndAddCategory(impcat, id);
//						}
//					}
//				}
//			}
//		}
//		return this.categoryHitsMap;
//	}
//
//	/**
//	 * Check if category already exist. If yes, extrack the arraylist of 
//	 * genes and add in a new id. If not, create a new arraylist and push 
//	 * <category, list> in categoryHitsMap
//	 * @param category
//	 * @param id
//	 */
//	private void checkAndAddCategory(String category, String id) {
//		ArrayList hitList;
//		if(this.categoryHitsMap.containsKey(category)) {
//			hitList = (ArrayList)this.categoryHitsMap.get(category);
//			hitList.add(id);
//		}
//		else {
//			hitList = new ArrayList();
//			hitList.add(id);
//			this.categoryHitsMap.put(category, hitList);
//		}
//	}
//	
//	/**
//	 * returen a reference of categoryMap
//	 * @return categoryMap
//	 */
//	private HashMap getCategoryMap() {
//		return this.categoryMap;
//	}
//	
//	public static void main(String[] args) {
//		TEASEBench bench = new TEASEBench();
//		ArrayList geneList = new ArrayList();
//		geneList.add("6352");
//		geneList.add("7849");
//		String fileName = "C:/MeV3.1/data/ease/Data/Class/GO Biological Process.txt";
//		HashMap map = bench.getCategories(fileName);
//		HashMap map1 = bench.getImpliedCategory(fileName);
//		HashMap hits = bench.getCategoryHitsMap(geneList);
//		
//
//		Set keys = hits.keySet();
//		Iterator it = keys.iterator();
//
//		while (it.hasNext()) {
//			String id = (String)it.next();
//			System.out.println(id + ": " + hits.get(id));
//		}
//
//	}
    
    /** 
     * Add a new annotation file name into the annotation 
     * file name list. 
     */
    public void AddAnnotationFileName(String file_name) {
        annotation_file_names.addElement(file_name);
    }
    public void setImpliesFileLocation(String impliesFileLocation) {
    	this.impliesFileLocation = impliesFileLocation;
    }
    
    /**  
     * Obtain the categories from the annotation files and create a hashtable 
     * using these categories as keys.
     * <key, value>, key = unrepeated categories, value = hashtable using 
     * locus ID (integers) as keys, value = ""
     * store all categories found in the annotation file(s)
     * called by performSlideAnnotationAnalysis() in EASEAnalysis
     */
    public void GetCategories() {
        BufferedReader in = null;
        Hashtable hash_table = new Hashtable();
        Hashtable implied_associations = new Hashtable();
        String term;
        String line="", category="", file_name="";
        int idx;
        int c =0;
        int idx2;        
        
        try{
            for(Enumeration e = annotation_file_names.elements(); e.hasMoreElements();){
                file_name = e.nextElement().toString();
                in = new BufferedReader(new FileReader(file_name));
                
                //use last index of . in case a user has a . in the path.
                term = file_name.substring(file_name.lastIndexOf(sep)+1, file_name.lastIndexOf("."));
                
                //store terms in total hits accumulators
                this.sample_totals.put(term, new Hashtable());
                this.pop_totals.put(term, new Hashtable());
                
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
                    
                    if(!categories.containsKey(category)){
                        categories.put(category, new Hashtable());   //key = locus ID, value = ""
                        ((Hashtable) categories.get(category)).put(line.substring(0,idx).trim(), "");
                    }else{
                        ((Hashtable) categories.get(category)).put(line.substring(0,idx).trim(), "");
                    }
                }
            }

            //System.out.println("categories size = "+categories.size());
            
            //(jcb)
            //create hash table for implies using (implies_associator)
            //This will then be used to add implied categories
            String fileName;
            for(int i = 0; i < annotation_file_names.size(); i++){
                fileName = (String)annotation_file_names.elementAt(i);
                
                
                //System.out.println("implies file = "+impliesFile);
                
                File file = new File(impliesFileLocation, fileName);
                if(!file.exists() || !file.isFile())  //if implies file is missing move on
                    continue;
                
                in = new BufferedReader(new FileReader(file));
                //term = fileName.substring(file_name.lastIndexOf("/")+1, file_name.indexOf("."));
                term = fileName.substring(fileName.lastIndexOf(sep)+1, fileName.lastIndexOf("."));
                
                
                while((line = in.readLine()) != null){
                    idx = line.indexOf('\t');
                    
                    if(idx >= line.length() || idx < 1)  //must include a tab
                        continue;
                    
                    if(!implied_associations.containsKey(term + "\t" +line.substring(0,idx).trim())){
                        implied_associations.put(term + "\t" +line.substring(0,idx).trim(), new Vector());
                        ((Vector)(implied_associations.get(term + "\t" +line.substring(0,idx).trim()))).addElement(term + "\t" + line.substring(idx, line.length()).trim());
                    } else {
                        ((Vector)(implied_associations.get(term + "\t" +line.substring(0,idx).trim()))).addElement(term + "\t" + line.substring(idx, line.length()).trim());
                    }
                }
            }
            boolean end = false;
            //(jcb) append associated categories to the list
            for(int k = 0; k < 10 && !end; k++){  // !end will short circuit if stable
                String cat="";
                Hashtable catHash;
                Vector impVector;
                String impCat;

                end = true;  //start at true until no new indices are inserted
                
                for( Enumeration enum6 = implied_associations.keys(); enum6.hasMoreElements(); ){
                    
                    cat = (String) enum6.nextElement();
                    
                    //if the category is represented, add the implied associations if they don't exist
                    if(categories.containsKey(cat)){
                        catHash = (Hashtable)categories.get(cat);
                        
                        //associated categories
                        impVector = ((Vector)implied_associations.get(cat));
                        
                        for(int i = 0; i < impVector.size(); i++){
                            
                            impCat = ((String)impVector.elementAt(i));
                            
                            if(!categories.containsKey(impCat)){
                                end = false;
                                categories.put(impCat, new Hashtable());
                                for(Enumeration categoryEnum = catHash.keys(); categoryEnum.hasMoreElements();){
                                    ((Hashtable)categories.get(impCat)).put((String)categoryEnum.nextElement(), "");
                                }
                            } else {  //category exists, need to append locus link numbers
                                for(Enumeration categoryEnum = catHash.keys(); categoryEnum.hasMoreElements();){
                                    String indexString = (String)categoryEnum.nextElement();
                                    if(!((Hashtable)categories.get(impCat)).containsKey(indexString) ){
                                        // ((Hashtable)categories.get(impCat)).put((String)categoryEnum.nextElement(), "");
                                        ((Hashtable)categories.get(impCat)).put(indexString, "");
                                        
                                        end = false;
                                    }
                                }
                            }
                        }
                    }
                }
            }
//            System.out.println(categories.size());
//            Set arr = (Set)categories.keySet(); //***************************************************
//            Iterator it = arr.iterator();
//            while (it.hasNext()) {
//            	Object ob = it.next();
//            	System.out.println(ob+" -----> "+categories.get(ob));
//            }
            
        }catch(Exception error){
            System.out.println("Error occured collecting categories");
            System.out.println(error.getMessage());
            error.printStackTrace();
        }
//        System.out.println("JEASESTatistics");  //*******************************************
//        Set keySet = categories.keySet();
//        Iterator it = keySet.iterator();
//        while(it.hasNext()) {
//        	Hashtable hash = (Hashtable)categories.get(it.next());
//        	Set id = hash.keySet();
//        	Iterator i = id.iterator();
//        	while(i.hasNext())
//        		System.out.println(i.next());
//        }
    }

    
    /**  
     * Obtain the categories from the annotation files and create a hashtable 
     * using these categories as keys.
     * only store categories that are present in the population list
     * called by performClusterAnnotationAnalysis() in EASEAnalysis
     * 
     * @param popVector population gene list
     * */
    public void GetCategories(Vector popVector) {
        BufferedReader in = null;
        Hashtable hash_table = new Hashtable();
        Hashtable implied_associations = new Hashtable();
        String term;
        String line="", category="", file_name="";
        int idx;
		int c = 0;
		int idx2;        
        
        try{
            for(Enumeration e = this.annotation_file_names.elements(); e.hasMoreElements();){
                file_name = e.nextElement().toString();
                in = new BufferedReader(new FileReader(file_name));
                
                //use last index of . in case a user has a . in the path.
                term = file_name.substring(file_name.lastIndexOf(this.sep)+1, file_name.lastIndexOf("."));
                
                //store terms in total hits accumulators
                this.sample_totals.put(term, new Hashtable());
                this.pop_totals.put(term, new Hashtable());
                
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
                        if(!this.categories.containsKey(category)){
                            this.categories.put(category, new Hashtable());
                            ((Hashtable) this.categories.get(category)).put(line.substring(0,idx).trim(), "");
                        }else{
                            ((Hashtable) this.categories.get(category)).put(line.substring(0,idx).trim(), "");
                        }
                    }
                }
            }
                        
            //create hash table for implies using (implies_associator)
            //This will then be used to add implied categories
            String fileName;
            for(int i = 0; i < annotation_file_names.size(); i++){
                fileName = (String)annotation_file_names.elementAt(i);
                 
                File file = new File(impliesFileLocation, fileName);
                if(!file.exists() || !file.isFile())  //if implies file is missing move on
                    continue;
                
                in = new BufferedReader(new FileReader(file));
                //term = fileName.substring(file_name.lastIndexOf("/")+1, file_name.indexOf("."));
                term = fileName.substring(fileName.lastIndexOf(sep)+1, fileName.lastIndexOf("."));
                
                
                while((line = in.readLine()) != null){
                    idx = line.indexOf('\t');
                    
                    if(idx >= line.length() || idx < 1)  //must include a tab
                        continue;
                    
                    if(!implied_associations.containsKey(term + "\t" +line.substring(0,idx).trim())){
                        implied_associations.put(term + "\t" +line.substring(0,idx).trim(), new Vector());
                        ((Vector)(implied_associations.get(term + "\t" +line.substring(0,idx).trim()))).addElement(term + "\t" + line.substring(idx, line.length()).trim());
                    } else {
                        ((Vector)(implied_associations.get(term + "\t" +line.substring(0,idx).trim()))).addElement(term + "\t" + line.substring(idx, line.length()).trim());
                    }
                }
            }
            
            boolean end = false;
            
            //(jcb) append associated categories to the list
            for(int k = 0; k < 10 && !end; k++){  // !end will short circuit if stable
                String cat="";
                Hashtable catHash;
                Vector impVector;
                String impCat;

                end = true;  //start at true until no new indices are inserted
                
                for( Enumeration enum6 = implied_associations.keys(); enum6.hasMoreElements(); ){
                    
                    cat = (String) enum6.nextElement();
                    
                    //if the category is represented, add the implied associations if they don't exist
                    if(categories.containsKey(cat)){
                        catHash = (Hashtable)categories.get(cat);
                        
                        //associated categories
                        impVector = ((Vector)implied_associations.get(cat));
                        
                        for(int i = 0; i < impVector.size(); i++){
                            
                            impCat = ((String)impVector.elementAt(i));
                            
                            if(!categories.containsKey(impCat)){
                                end = false;
                                categories.put(impCat, new Hashtable());
                                for(Enumeration categoryEnum = catHash.keys(); categoryEnum.hasMoreElements();){
                                    ((Hashtable)categories.get(impCat)).put((String)categoryEnum.nextElement(), "");
                                }
                            } else {  //category exists, need to append locus link numbers
                                for(Enumeration categoryEnum = catHash.keys(); categoryEnum.hasMoreElements();){
                                    String indexString = (String)categoryEnum.nextElement();
                                    if(!((Hashtable)categories.get(impCat)).containsKey(indexString) ){
                                        ((Hashtable)categories.get(impCat)).put(indexString, "");
                                        
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
     * Creates the tab-delimited results containing information on system, 
     * category, population hits, population total, sample hits
     * and sample totoal, and the one-tailed hypergeometric probability 
     * score of over-representation.
     * categories_list stores all categories hits in the sample
     * categories_population contains categories hits in the population file
     */
    public void ConstructResults() {
        
        String key="", list_hit="", population_hit="", temp="";
        this.records = new String[categories_list.size()][7];
        this.orders = new String[categories_list.size()];
        
        int c=0;
        //System.out.println(categories_list.isEmpty());
        for(Enumeration e = categories_list.keys(); e.hasMoreElements();){  //iterate through categories_list
            key = (String)(e.nextElement());                                //hashtable
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
            
            population_total = ((Hashtable)(pop_totals.get(temp))).size();
            
            records[c][5] = String.valueOf(population_total);
            
            if(!list_hit.equals("") && !population_hit.equals("")){
                double p;
                //System.out.println("population_total = "+ population_total+"   list_total = "+list_total);
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
        //System.out.println("JEASEStatistics 585");
        for(Enumeration e = hitAccumulator.keys(); e.hasMoreElements();){
            String cat = (String)(e.nextElement());
            categoryNames[cnt] = cat;
            //System.out.print(cat + ": ");
            locusVector = ((Vector)hitAccumulator.get(cat));
            listHitMatrix[cnt] = new String[locusVector.size()];
            for(int i = 0; i < listHitMatrix[cnt].length; i++){
                listHitMatrix[cnt][i] = ((String)locusVector.elementAt(i));

                //System.out.print(listHitMatrix[cnt][i] + " ");  //*****************************
            }
            cnt++;
            //System.out.println();
        }
    }
    
    
    
    /** Sort the results in the ascending order of p-score and write the tab-delimited 
     * result to a file and standard IO output
     */
    public void SortRecords() {
        BigDecimal big1, big2;
        String holder;
        
        for(int i0=0; i0 < records.length; i0++){
            for(int i=0; i < records.length-1 - i0; i++){
                big1 = new BigDecimal(records[i][6]);
                big2 = new BigDecimal(records[i+1][6]);
                
                if(big1.compareTo(big2) > 0){
                	for (int j = 0; j < 7; j++) {
	                    holder = records[i][j];
	                    records[i][j] = records[i+1][j];
	                    records[i+1][j] = holder;
                	}
                }
            }
        }
    }
    
    /** 
     * Writes the tab-delimited results to the specified output file and to 
     * the standout IO output as well 
     */
    public void DisplayResults() {
        try{
            File output_file = new File(output_file_name);
            FileWriter output_writer = new FileWriter(output_file); //, false);
            
            for(int i=0; i < records.length; i++){
            	for (int j = 0; j < 6; j++) {
            		System.out.print(records[i][j] + "\t");
	                output_writer.write(records[i][j] + "\t");
	                output_writer.write("\n");
            	}
            }
            output_writer.close();
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }
    
    
    /**
     * (jcb)Modified Hit collection methods to accept a vector argument of annotation keys
     * Modification of code to enable integration into MeV Ease module.
     */
    
    /** 
     * Get the number of the genes in the population for each category 
     * that exists in the annotation files.
     * @param list population list.
     */
    public void GetPopulationHitsByCategory(Vector list) {
        BufferedReader in = null;
        String category="", hits="", key="", locus_id;
        Hashtable locus_ids = new Hashtable();
        Hashtable hash_table = new Hashtable();
        Hashtable count_ids = new Hashtable();
        this.pop_totals = clear(this.pop_totals);
        
        try{
            int size = list.size();
            for(int i = 0; i < size; i++)   //used locus_ids hashtable to store all elements in population list
                locus_ids.put((String)list.elementAt(i), "");
            
            for(Enumeration enum1 = locus_ids.keys(); enum1.hasMoreElements();){  //iterate through population list
                locus_id = (String)(enum1.nextElement());
                
                for(Enumeration enum6 = categories.keys(); enum6.hasMoreElements();){  //iterate through categories
                    key = (String)(enum6.nextElement());
                    hash_table = (Hashtable) (categories.get(key));
                    
                    if(hash_table.containsKey(locus_id)){
                        if(!categories_population.containsKey(key)){
                            categories_population.put(key, "1");
                        }else{
                            hits = String.valueOf(Integer.parseInt((String) categories_population.get(key)) + 1);
                            categories_population.put(key, hits);
                        }
                        count_ids.put(locus_id, "");
                        
                        //accumulate the pop total hits
                        key = key.substring(0, key.indexOf("\t")).trim();
                        ((Hashtable)pop_totals.get(key)).put(locus_id,"");
                    }
                }
            }
            population_total = count_ids.size();
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
//        Iterator it = (categories_population.keySet()).iterator();
//        while (it.hasNext())
//        	System.out.print(it.next()+ "  ");
        
    }
    
    /**
     * Keep the key set but reset all value to blank
     * @param hash
     * @return hash 
     */
    public Hashtable clear(Hashtable hash) {
    	Iterator it = (hash.keySet()).iterator();
    	while(it.hasNext()) {
    		hash.put(it.next(), new Hashtable());
    	}
    	return hash;
    }
    /** 
     * Get the number of the genes in the sample for each category 
     * that exists in the annotation files.
     * sample list
     * @param list List indices.
     */
    public void GetListHitsByCategory(Vector list) {
    	initiation();
		this.categories_list = new Hashtable();
        BufferedReader in = null;
        String category="", hits="", key="", locus_id;
        Hashtable locus_ids = new Hashtable();
        Hashtable hash_table = new Hashtable();
        Hashtable count_ids = new Hashtable();
        this.sample_totals = clear(this.sample_totals);
        
        int c = 0;
        try{
            int size = list.size();
            for(int i = 0; i < size; i++)   //use a hashtable to store all elements in the sample list
                locus_ids.put((String)list.elementAt(i), "");
            
            for(Enumeration enum1 = locus_ids.keys(); enum1.hasMoreElements();){   //iterate through samples ids.
                locus_id = (String)(enum1.nextElement());    //locus_id is hashtable
                
                for(Enumeration enum6 = this.categories.keys(); enum6.hasMoreElements();){  //iterate through categories
                    key = (String)(enum6.nextElement());                                   //hashtable 
                    hash_table = (Hashtable) (this.categories.get(key));
                    
                    if(hash_table.containsKey(locus_id)){                   //if sample id is present in the list of genes
                        if(!this.categories_list.containsKey(key)){
                            this.categories_list.put(key, "1");
                            //jcb
                            this.hitAccumulator.put(key, new Vector());
                            ((Vector)this.hitAccumulator.get(key)).add(locus_id);
                            //end mod
                        }else {
                            hits = String.valueOf(Integer.parseInt((String) (this.categories_list.get(key))) + 1);
                            this.categories_list.put(key, hits);
                            //jcb
                            ((Vector)this.hitAccumulator.get(key)).add(locus_id);
                            //end mod
                        }
                        count_ids.put(locus_id, "");
                        
                        //accumulate the sample total hits
                        key = key.substring(0, key.indexOf("\t")).trim();
                        ((Hashtable)this.sample_totals.get(key)).put(locus_id,"");
                    }
                }
            }
            this.list_total = count_ids.size();    
            //System.out.println("list_total = " + list_total);
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
//        System.out.println("categories_list is empty" + categories_list.isEmpty());
//      Iterator it = (categories_list.keySet()).iterator();
//      while (it.hasNext())
//      	System.out.println(it.next()+ "  ");
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
        this.categories_list = new Hashtable();
        this.sample_totals = new Hashtable();
        String file_name, term;
   
        for(Enumeration e = annotation_file_names.elements(); e.hasMoreElements();){
            file_name = e.nextElement().toString();
            term = file_name.substring(file_name.lastIndexOf(sep)+1, file_name.lastIndexOf("."));
            
            //store terms in total hits accumulators
            this.sample_totals.put(term, new Hashtable());
        }
    }
    
    
    /**
     * Modified Hit collection methods to accept a vector argument of annotation keys
     * Modification of code to enable integration into TIGR MeV EASE module.
     */
    
    /** 
     * Get the number of the genes in the population for each category that exists in the annotation files.
     * for slide annotation survey
     * @param list Input population list.
     */
    public void GetPopulationHitsByCategoryForSurvey(Vector list) {
    	initiation();
    	this.categories_population = new Hashtable();
        String category="";
        String hits="";
        String key="";
        String locus_id;       
        Hashtable locus_ids = new Hashtable();  //hashtable uses locus IDs in the list as key, "" as values
        Hashtable hash_table = new Hashtable();  
        Hashtable count_ids = new Hashtable();  //no duplicate locus id in the key set
        //jcb
        String impliedCategory="";
        Vector impliedVector;
        this.pop_totals = clear(this.pop_totals);
        
        
        int c = 0;
        
        try{
//        	//************************************************************************
//            Hashtable hash = (Hashtable)categories.get("biochemical function	RNA polymerase subunit");
//            Iterator it = hash.keySet().iterator();
//            while (it.hasNext()){
//            	Object thing = it.next();
//            	System.out.println(thing);
//            }

        	int size = list.size();
            for(int i = 0; i < size; i++)
                locus_ids.put((String)list.elementAt(i), "");
            
            for(Enumeration enum1 = locus_ids.keys(); enum1.hasMoreElements();){
                locus_id = (String)(enum1.nextElement());

                for(Enumeration enum6 = categories.keys(); enum6.hasMoreElements();){
                    key = (String)(enum6.nextElement());
                    hash_table = (Hashtable) (categories.get(key));

                    //if locus ID in the list can be found in the categories list
                    if(hash_table.containsKey(locus_id)){
                        if(!categories_population.containsKey(key)){
                            categories_population.put(key, "1");   //if first locus id in this category
                            //System.out.println("TEASEBench 1042, hitAccumulator add");
                            hitAccumulator.put(key, new Vector());
                            ((Vector)hitAccumulator.get(key)).add(locus_id); //put in a list of locus id
                        }else{
                            hits = String.valueOf(Integer.parseInt((String)categories_population.get(key)) + 1);
                            categories_population.put(key, hits);
//                            System.out.println("hitAccumulator is null? "+(hitAccumulator == null));
//                            System.out.println("key is null? "+(key == null));
//                            System.out.println("locus_id is null? "+(locus_id == null));
//                            System.out.println("list is null? "+(hitAccumulator.get(key) == null));
                            ((Vector)hitAccumulator.get(key)).add(locus_id);
                        }
                        count_ids.put(locus_id, "");
                        //accumulate the pop total hits
                        key = key.substring(0, key.indexOf("\t")).trim();
                        ((Hashtable)pop_totals.get(key)).put(locus_id,"");
                    }
                }
            }
//            System.out.println("cout_ids is empty? "  +count_ids.isEmpty());
//            Set keys = count_ids.keySet();
//            Iterator it = keys.iterator();
//            while (it.hasNext()) {
//            	Object o = it.next();
//            	System.out.println("key: "+o + "  value: "+ count_ids.get(o));
//            }
            population_total = count_ids.size(); 
            //System.out.println("population total: " + population_total);  //*******************************
            
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
    
    /** 
     * Creates the tab-delimited results containing information on system, category, 
     * population hits, population total, sample hits and sample totoal, and the 
     * one-tailed hypergeometric probability score of over-representation.
     * for slide annotation survey
     */
    public void ConstructSurveyResults() {
        
        String key="", list_hit="", population_hit="", temp="";        
        
        records = new String[categories_population.size()][4];   //records is String[][]
        orders = new String[categories_population.size()];       //orders is String[]
        
        int c=0;
     //   System.out.println("TEASEBench 1101  "+categories_population.size());
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
            //System.out.println(records[c][3]);
            c++;
        }
        
        //jcb
        //accumulates hits for the result matrix.
        categoryNames = new String[hitAccumulator.size()];   //keys  -> category names
        listHitMatrix = new String[categoryNames.length][];  //values  -> list of hits
        
        int cnt = 0;
        Vector locusVector;
        
        for(Enumeration e = hitAccumulator.keys(); e.hasMoreElements();){
            String cat = (String)(e.nextElement());
            categoryNames[cnt] = cat;   //categoryNames[0] = name of a category
            locusVector = ((Vector)hitAccumulator.get(cat));
            listHitMatrix[cnt] = new String[locusVector.size()];  //listHitMatrix[0][] = list of hits of
            for(int i = 0; i < listHitMatrix[cnt].length; i++){   //corresponding category
                listHitMatrix[cnt][i] = ((String)locusVector.elementAt(i));
            }
            cnt++;
        }
        
//        for (int i = 0; i < categoryNames.length; i++) {   //*****************************************
//        	System.out.print(categoryNames[i]+" ");
//        	for (int j = 0; j < listHitMatrix[i].length; j++)
//        		System.out.print(listHitMatrix[i][j]+" ");
//        	System.out.println();
//        }
    }
}
