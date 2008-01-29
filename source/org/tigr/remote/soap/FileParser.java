package org.tigr.remote.soap;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Vector;

import org.tigr.microarray.util.*;
import org.tigr.util.StringSplitter;

public class FileParser {

	/**
	 * @param args
	 */
	File AnnotationFile_Revised;
	public  FileParser(){
		
	}
	
	public String parse(String annotationFile, String directory){
			
		String newFileName=annotationFile.replaceAll(".txt", "_Parsed.txt");
		AnnotationFile_Revised=new File(newFileName);
		
		System.out.println("AnnotationFileName"+annotationFile);
		
		try{
		BufferedReader reader = new BufferedReader(new FileReader(directory+"/"+annotationFile));
		PrintWriter writer  = new PrintWriter(new BufferedWriter(new FileWriter(directory+"/"+AnnotationFile_Revised)));
		
		String Line=null;
		StringSplitter ss=new StringSplitter('\t');
		Line=reader.readLine();
		int pass=0;
		while((Line=reader.readLine())!=null) {
			
			
			while((Line.startsWith("#"))) {
				writer.write(Line);
				writer.println();
				Line=reader.readLine();
			}
			
			ss.init(Line);
			
			
			if(pass==0) {
				while(ss.hasMoreTokens()) {
				String field=(String)ss.nextToken();
	    		if(field.equalsIgnoreCase("CLONE_ID")){
    				writer.write("CLONE_ID");
    				writer.append('\t');
    			}else if(field.equalsIgnoreCase("GENBANK_ACC")){
    				writer.write("GENBANK_ACC");
    				writer.append('\t');
    			}else if(field.equalsIgnoreCase("UNIGENE_ID")){
    				writer.write("UNIGENE_ID");
    				writer.append('\t');
    			}else if(field.equalsIgnoreCase("GENE_TITLE")){ 
    				writer.write("GENE_TITLE");
    				writer.append('\t');
    			}else if(field.equalsIgnoreCase("GENE_SYMBOL")){ 
    				writer.write("GENE_SYMBOL");
    				writer.append('\t');
    			}else if(field.equalsIgnoreCase("ENTREZ_ID")){
    				writer.write("ENTREZ_ID");
    				writer.append('\t');
    			}else if(field.equalsIgnoreCase("REFSEQ_ACC")){
    				writer.write("REFSEQ_ACC");
    				writer.append('\t');
    			}else if(field.equalsIgnoreCase("GO_TERMS")){
    				writer.write("GO_TERMS");
    				writer.append('\t');
				}else if(field.equalsIgnoreCase("CHR:TX_START-TX_END(STRAND)")){
    				writer.write("CHR:TX_START-TX_END(STRAND)");
    				writer.append('\t');
				}else {
					writer.write(field);
					writer.append('\t');
				}
					
		}
				pass=pass+1;
				writer.println();
				
				
		}else {
			while(ss.hasMoreTokens()) {
				String field=(String)ss.nextToken();
				writer.write(field);
    			writer.append('\t');
			}
			
			writer.println();
		}
		
		}
		writer.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		return newFileName;
	}
	
	public static void main(String[] args) {
		FileParser fp=new FileParser();
		fp.parse("affy_HG_U95B.txt", "C:/Documents and Settings/sarita/Desktop/RANDOM");
	}
	
	
	
}
