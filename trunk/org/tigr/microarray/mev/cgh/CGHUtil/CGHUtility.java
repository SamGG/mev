/*
 * Utility.java
 *
 * Created on October 6, 2002, 12:22 AM
 */

package org.tigr.microarray.mev.cgh.CGHUtil;

import java.util.List;

import org.tigr.microarray.mev.TMEV;

/**
 *
 * @author  Adam Margolin
 */

public class CGHUtility {
    
    /** Creates a new instance of Utility */
    public CGHUtility() {
    }
    
    public static String encap(String str){
        return "'" + str + "'";
    }
    
    public static int convertStringToChrom(String strChrom, int species){
        if(strChrom.endsWith("_random")){
            strChrom = strChrom.substring(0, strChrom.length() - 7);
        }
        if("chr1".equalsIgnoreCase(strChrom) || "1".equalsIgnoreCase(strChrom)){
            return 1;
        }else if("chr2".equalsIgnoreCase(strChrom) || "2".equalsIgnoreCase(strChrom)){
            return 2;
        }else if("chr3".equalsIgnoreCase(strChrom) || "3".equalsIgnoreCase(strChrom)){
            return 3;
        }else if("chr4".equalsIgnoreCase(strChrom) || "4".equalsIgnoreCase(strChrom)){
            return 4;
        }else if("chr5".equalsIgnoreCase(strChrom) || "5".equalsIgnoreCase(strChrom)){
            return 5;
        }else if("chr6".equalsIgnoreCase(strChrom) || "6".equalsIgnoreCase(strChrom)){
            return 6;
        }else if("chr7".equalsIgnoreCase(strChrom) || "7".equalsIgnoreCase(strChrom)){
            return 7;
        }else if("chr8".equalsIgnoreCase(strChrom) || "8".equalsIgnoreCase(strChrom)){
            return 8;
        }else if("chr9".equalsIgnoreCase(strChrom) || "9".equalsIgnoreCase(strChrom)){
            return 9;
        }else if("chr10".equalsIgnoreCase(strChrom) || "10".equalsIgnoreCase(strChrom)){
            return 10;
        }else if("chr11".equalsIgnoreCase(strChrom) || "11".equalsIgnoreCase(strChrom)){
            return 11;
        }else if("chr12".equalsIgnoreCase(strChrom) || "12".equalsIgnoreCase(strChrom)){
            return 12;
        }else if("chr13".equalsIgnoreCase(strChrom) || "13".equalsIgnoreCase(strChrom)){
            return 13;
        }else if("chr14".equalsIgnoreCase(strChrom) || "14".equalsIgnoreCase(strChrom)){
            return 14;
        }else if("chr15".equalsIgnoreCase(strChrom) || "15".equalsIgnoreCase(strChrom)){
            return 15;
        }else if("chr16".equalsIgnoreCase(strChrom) || "16".equalsIgnoreCase(strChrom)){
            return 16;
        }else if("chr17".equalsIgnoreCase(strChrom) || "17".equalsIgnoreCase(strChrom)){
            return 17;
        }else if("chr18".equalsIgnoreCase(strChrom) || "18".equalsIgnoreCase(strChrom)){
            return 18;
        }else if("chr19".equalsIgnoreCase(strChrom) || "19".equalsIgnoreCase(strChrom)){
            return 19;
        }else if("chr20".equalsIgnoreCase(strChrom) || "20".equalsIgnoreCase(strChrom)){
            return 20;
        }else if("chr21".equalsIgnoreCase(strChrom) || "21".equalsIgnoreCase(strChrom)){
            return 21;
        }else if("chr22".equalsIgnoreCase(strChrom) || "22".equalsIgnoreCase(strChrom)){
            return 22;
        }else if("chrX".equalsIgnoreCase(strChrom) || "X".equalsIgnoreCase(strChrom)){
        	//return 23;
        	
        	int chr = 0;
        	switch (species){
        		case TMEV.CGH_SPECIES_HS: {chr = 23; break;}
        		case TMEV.CGH_SPECIES_MM: {chr = 20; break;}
        		default: chr = -1; break;
        	}
        	return chr;
        	
        }else if("chrY".equalsIgnoreCase(strChrom) || "Y".equalsIgnoreCase(strChrom)){
        	//return 24;
        	int chr = 0;
        	switch (species){
	        	case TMEV.CGH_SPECIES_HS: {chr = 24; break;}
	        	case TMEV.CGH_SPECIES_MM: {chr = 21; break;}
	        	default: chr = -2; break;
        	}
        	return chr;
        	
        }else{
            //System.out.println("Util convert chrom, not found: " + strChrom);
            return org.tigr.microarray.mev.cgh.CGHDataObj.CGHClone.NOT_FOUND;
        }
    }
    
    private int XChrToSpecies(int species) {
    	int chr = 0;
    	switch (species){
        	case TMEV.CGH_SPECIES_HS: {chr = 23; break;}
        	case TMEV.CGH_SPECIES_MM: {chr = 20; break;}
        	default: chr = -1; break;
    	}
    	return chr;
    }
    
    public static String convertChromToString(int chrom, int species){
    	switch (species){
	    	case TMEV.CGH_SPECIES_HS:
		        if(chrom < 23 && chrom > 0) return "chr" + chrom;   
		        if(chrom == 23) return "chrX";
		        if(chrom == 24) return "chrY";
	    	case TMEV.CGH_SPECIES_MM: 
	    		if(chrom < 20 && chrom > 0) return "chr" + chrom;     
		        if(chrom == 20) return "chrX";
		        if(chrom == 21) return "chrY";
	    	case TMEV.CGH_SPECIES_Undef:
	    		return "chr" + chrom;
	    	default:
		        System.out.println("Util convert chrom, not found index: " + chrom);
		        return "";
    	}
    }
    
    public static String convertChromToLongString(int chrom, int species){
    	switch (species){
	    	case TMEV.CGH_SPECIES_HS:
		        if(chrom < 23 && chrom > 0) return "Chromosome " + chrom;   
		        if(chrom == 23) return "Chromosome X";
		        if(chrom == 24) return "Chromosome Y";
	    	case TMEV.CGH_SPECIES_MM: 
	    		if(chrom < 20 && chrom > 0) return "Chromosome " + chrom;     
		        if(chrom == 20) return "Chromosome X";
		        if(chrom == 21) return "Chromosome Y";
	    	case TMEV.CGH_SPECIES_Undef:
	    		return "Chromosome " + chrom;
	    	default:
		        System.out.println("Util convert chrom, not found index: " + chrom);
		        return "";
    	}
    }
    
    public static String createQueryString(List values){
        String queryString = "(";
        if(values == null || values.size() < 1){
            return null;
        }
       
        queryString += encap(values.get(0).toString());
        for(int i = 1; i < values.size(); i++){
            queryString += ", " + encap(values.get(i).toString());
        }
        return queryString + ")";
    }
    
    public static String createIntegerQueryString(List values){
        String queryString = "(";
        if(values == null || values.size() < 1){
            return null;
        }
       
        queryString += values.get(0);
        for(int i = 1; i < values.size(); i++){
            queryString += ", " + values.get(i);
        }
        return queryString + ")";
    }
    
}
