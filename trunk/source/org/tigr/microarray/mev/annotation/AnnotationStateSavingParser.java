/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
package org.tigr.microarray.mev.annotation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JOptionPane;

import org.tigr.util.StringSplitter;

public class AnnotationStateSavingParser {
	
	String[] fieldNames=new String[0];
	Vector columnNames=new Vector();
	
	
	public AnnotationStateSavingParser() {
		// TODO Auto-generated constructor stub
	}
	
	
	//public Vector<IAnnotation> readSavedAnnotation(String annotationFileName, String directory)throws Exception {
	public Vector<IAnnotation> readSavedAnnotation(File iAnnotFile)throws Exception {
		//public Vector<IAnnotation> readSavedAnnotation(FileReader freader)throws Exception {
		/**
		 * Exactly the same functionality as loadSlideDataAnnotation in the
		 * PersistenceObjectFactory class and the AnnotationFileReader1.
		 * The reason being, AnnotationFileReader1 reads an annotation file into a Hashtable,
		 * this hash is later used in fileloaders to load the MevAnnotation object into
		 * the AffySlideDataElement constructor.
		 * 
		 * But, when we are reading a saved analysis file, we want to skip the hashtable step
		 * and instead load directly into the 
		 * AffySlideDataElement (refer to loadSlideDataAnnotation mentioned above).
		 * 
		 * NOTE:
		 * The  file that is being read is 
		 * the ascii annotation file saved in the .anl file. This ascii file was written
		 * out by the function writeAnnotationFile(see below)
		 * 
		 */
		
//	    System.out.println("ASSP:AnnotationFileName:"+annotationFileName);
//	    System.out.println("ASSP:directory:"+directory);
	    
    	Vector<IAnnotation> annovEC  = new Vector<IAnnotation>();
       	//BufferedReader reader = new BufferedReader(new FileReader(directory));
    	BufferedReader reader = new BufferedReader(new FileReader(iAnnotFile));
    //	BufferedReader reader = new BufferedReader(freader);
    	StringSplitter ss = new StringSplitter('\t');
    	String currentLine, cloneID;
 
    	int counter = 0;

    	MevAnnotation annotationObj; 
//    	this.columnNames=getColumnHeader(new File(directory));
    	this.columnNames=getColumnHeader(iAnnotFile);
    
    	//this.columnNames=getColumnHeader(freader);

    	String _temp="NA";
    	//System.out.println("currentLine:"+reader.readLine());
    	while ((currentLine = reader.readLine()) != null) {

			annotationObj = new MevAnnotation(); //TODO
    		cloneID = "";
    		
    		while(currentLine.startsWith("#")) {
    			//System.out.println(currentLine);
    			currentLine=reader.readLine();
    			
    		}
    	
    		
    		ss.init(currentLine);
          
    		for(int i = 0; i < columnNames.size(); i++){
    		
    			if(ss.hasMoreTokens()){
       			 _temp = ss.nextToken();
       			}
       			String field=(String)columnNames.get(i);
       			int index=columnNames.indexOf((Object)field);
       		
       			Vector<String> _tmpGo = new Vector<String>();
       			if(field.equalsIgnoreCase(AnnotationFieldConstants.CLONE_ID)&&index==i){
       				cloneID=_temp;
       				annotationObj.setCloneID(_temp);
       				//System.out.println("clone id:"+probeID);
       			}else if(field.equalsIgnoreCase(AnnotationFieldConstants.GENBANK_ACC)&&index==i){
       				if(_temp==null){
       					_temp="NA";
       				}
       				annotationObj.setGenBankAcc(_temp);
       				//System.out.println("Genbank acc:"+_temp);
       			}else if(field.equalsIgnoreCase(AnnotationFieldConstants.UNIGENE_ID)&&index==i){
       				if(_temp==null){
       					_temp="NA";
       				}
       				annotationObj.setUnigeneID(_temp);
       			//	System.out.println("Unigene id:"+_temp);
       			}else if(field.equalsIgnoreCase(AnnotationFieldConstants.GENE_TITLE)&&index==i){ 
       				if(_temp==""){
       					_temp="NA";
       				}
       				annotationObj.setGeneTitle(_temp);
       				//System.out.println("Gene_Title:"+_temp);
       			}else if(field.equalsIgnoreCase(AnnotationFieldConstants.GENE_SYMBOL)&&index==i){ 
       				if(_temp==null){
       					_temp="NA";
       				}
       				annotationObj.setGeneSymbol(_temp);
       			//	System.out.println("Gene Symbol:"+_temp);
       			}else if(field.equalsIgnoreCase(AnnotationFieldConstants.CHR_CYTOBAND)&&index==i){
       				if(_temp==null){
       					_temp="NA";
       				}
       				setAlignmentInfo(_temp, annotationObj);
       			//	System.out.println("Cytoband:"+_temp);
       			}else if(field.equalsIgnoreCase(AnnotationFieldConstants.ENTREZ_ID)&&index==i){
       				if(_temp==null){
       					_temp="NA";
       				}
       				annotationObj.setLocusLinkID(_temp);
       			//	System.out.println("Entrez id:"+_temp);
       			}else if(field.equalsIgnoreCase(AnnotationFieldConstants.REFSEQ_ACC)&&index==i){
       				if(_temp==null){
       					_temp="NA";
       				}
       				String mRnaRefSeqs[] = parsemRnaIds(_temp);
   			 		annotationObj.setRefSeqTxAcc(mRnaRefSeqs);
       			//	System.out.println("RefSeq Acc:"+_temp);
       			}else if(field.equalsIgnoreCase(AnnotationFieldConstants.GO_TERMS)&&index==i){
       				if(_temp==null){
       					_temp="NA";
       				}
       				 _tmpGo = parseGoTerms(_temp, "///"); 
       				annotationObj.setGoTerms((String[]) _tmpGo.toArray(new String[_tmpGo.size()]));
       			//	System.out.println("GO_Terms:"+_temp);
       			}else if(field.equalsIgnoreCase(AnnotationFieldConstants.TGI_TC)&&index==i){
       				if(_temp==null){
       					_temp="NA";
       				}
       				annotationObj.setTgiTC(_temp);
       			//	System.out.println("TGI_TC:"+_temp);
       			}


    		}
        //  }


    		annovEC.add(annotationObj);
    		
    	}
    	reader.close();
    return annovEC;
		
	}
	
	
	
	
	 private Vector getColumnHeader(File targetFile)throws Exception {
		//private Vector getColumnHeader(FileReader freader)throws Exception {
		 	//System.out.println("getColumnHeader: file name is:"+freader.getAbsolutePath());
	    	BufferedReader reader = new BufferedReader(new FileReader(targetFile));
	    	StringSplitter split = new StringSplitter(' ');
	    	StringSplitter ss = new StringSplitter('\t');
	    	String currentLine;
	    	Vector<String> columnNames=new Vector<String>();
	    	while((currentLine=reader.readLine()).contains("#")) {
	    		//System.out.println("getColumnHeader()::currentLine:"+currentLine);
	    		//while(!currentLine.equals("# Fields:")){
	    			//currentLine=reader.readLine();
	    		//}
	    		
	    		if(currentLine.contains("# Fields:")) {
	    			String temp;
	    			//System.out.println("currentLine is:"+currentLine);
	    			while((currentLine=reader.readLine()).contains("#")) {
	    				//System.out.println("currentLine is:"+currentLine);
		    			split.init(currentLine);
		    			split.nextToken();
		    			split.nextToken();
		    			split.nextToken();
		    			split.nextToken();
		    			
		    			temp=split.nextToken();
		    			int index=Integer.parseInt(temp.substring(0, temp.indexOf(".")));
		    			String ann=split.nextToken();
		    		//	System.out.println("index:"+index);
		    		//	System.out.println("ann:"+ann);
		    			columnNames.add(index-1, ann);
	    			}
	    			break;
	    		}
	    	
	    	}
	    //	reader.close();
	    //	System.out.println("columnNames size:"+columnNames.size());
	    	return columnNames;
	    }
	   
	 
	 private Vector<String> parseGoTerms(String _temp, String delim) {
	    	Vector<String> terms = new Vector<String>();
	    	StringTokenizer tokens = new StringTokenizer(_temp, delim);
	    	while(tokens.hasMoreTokens()){
	    		terms.add(tokens.nextToken().trim());
	    	}
	    	return terms;
		}

	 
	 private String[] parsemRnaIds(String _temp){
	    	Vector<String> mrna = new Vector<String>();
	    	StringTokenizer tokens = new StringTokenizer(_temp, "///");
	    	while(tokens.hasMoreTokens()){
	    		mrna.add(tokens.nextToken().trim());
	    	}
	    	
	    	String[] strArray = new String[mrna.size()];
	    	mrna.toArray(strArray);
	    	return strArray;
	    }
	 
	 
	 private void setAlignmentInfo(String temp, MevAnnotation obj){
	    	/*
	    	 * Template to parse
	    	 */
	    	//chr6:30964144-30975910 (+) // 95.63 // p21.33 /// chr6_cox_hap1:2304770-2316538 (+) // 95.56 // /// chr6_qbl_hap2:2103099-2114867 (+) // 95.45 //
	    	if(!temp.trim().startsWith("chr")) { //Alignment info not available
	    		obj.setProbeStrand("na");
	        	obj.setProbeChromosome("na");
	        	try {
	        		obj.setProbeTxStartBP("-1");
	        		obj.setProbeTxEndBP("-1");
	        	} catch (Exception e) {
	        		
	        	}
	    		return;
	    	}
	    	
	    	int index = temp.indexOf("(");
	    	String strand = temp.substring(index+1, index+2).trim();
	    	//System.out.println("Strand" + strand);
	    	
	    	String _temp = temp.substring(0,index-1).trim();
	    	int chrInd = _temp.indexOf(":");
	    	String chr = _temp.substring(0, chrInd);
	    	chr = chr.substring(3, chr.length());
	    	//System.out.println("chr" + chr);
	    	
	    	int txInd = _temp.indexOf("-");
	    	String txSt = _temp.substring(chrInd+1, txInd);
	    	String txEnd = _temp.substring(txInd+1, _temp.length());
	    	//System.out.println("txSt, txEnd " + txSt + " " + txEnd);
	    	
	    	obj.setProbeStrand(strand);
	    	obj.setProbeChromosome(chr);
	    	try {
	    		obj.setProbeTxStartBP(txSt);
	    		obj.setProbeTxEndBP(txEnd);
	    	} catch (Exception e) {
	    		System.out.println("Contains Illegal Char: " + txSt + ", " + txEnd);
	    	}
	    }

	    
	
	 public void writeAnnotationFile(Vector IAnnotations, PrintWriter writer) throws Exception {
		/**
		 * This fucntion would take in a vector containing MevAnnotation objects,
		 * (corresponding to the respective AffySlideDataElements). Writes the content of
		 * this vector in to a file.
		 * 
		 * NOTE: Everything that is preceded by a Hash in the sample annotation file
		 * for eg (#Organism, #Genome build etc) is not contained in the IAnnotation
		 * vector; hence we need to write a loop to write out that ino into the file as well.
		 * 
		 * 
		 */
		
		//System.out.println("annotation file name:"+iAnnotationFile.getName());
		
		File AnnotationFile_saved;

		String[] fieldNames = new String[]{	AnnotationFieldConstants.CLONE_ID, 
											AnnotationFieldConstants.GENBANK_ACC, 
											AnnotationFieldConstants.REFSEQ_ACC, 
											AnnotationFieldConstants.ENTREZ_ID, 
											AnnotationFieldConstants.UNIGENE_ID,
											AnnotationFieldConstants.GENE_SYMBOL, 
											AnnotationFieldConstants.GENE_TITLE,
											AnnotationFieldConstants.CHR_CYTOBAND, 
											AnnotationFieldConstants.GO_TERMS, 
											AnnotationFieldConstants.TGI_TC};

		writer.write("#");
		writer.println();
		writer.write("# Fields:"); 
		writer.println();
		writer.write("#    1. CLONE_ID");
		writer.println();
		writer.write("#    2. GENBANK_ACC");
		writer.println();
		writer.write("#    3. REFSEQ_ACC");
		writer.println();
		writer.write("#    4. ENTREZ_ID");
		writer.println();
		writer.write("#    5. UNIGENE_ID");
		writer.println();
		writer.write("#    6. GENE_SYMBOL");
		writer.println();
		writer.write("#    7. GENE_TITLE");
		writer.println();
		writer.write("#    8. CHR:TX_START-TX_END(STRAND)");
		writer.println();
		writer.write("#    9. GO_TERMS");
		writer.println();
		writer.write("#    10. TGI_TC");
		writer.println();
		//TODO
//		System.out.println("AnnotationStateSavingParser.writeAnnotationFile: IAnnotations.size(): " + IAnnotations.size());
		for(int i=0; i<IAnnotations.size(); i++) {
			MevAnnotation _obj=(MevAnnotation)IAnnotations.get(i);
			
//			for(int index=0; index<MevAnnotation.getFieldNames().length; index++) {
			for(int index=0; index<fieldNames.length; index++) {
				String _tempRefSeq="";
				String _tmpGO="";
				String[] _temparray;
				String _tempStr;
				
				if(index==0) {
					writer.write(_obj.getCloneID());
					writer.write('\t');
				} else if(index==1) {
					_tempStr=_obj.getGenBankAcc();
					if(_tempStr!="NA") {
						writer.write(_obj.getGenBankAcc());
						writer.write('\t');
					} else {
						writer.write("NA");
						writer.write('\t');
					}
				} else if(index==2) {
					_temparray=_obj.getRefSeqTxAcc();
					if(_temparray[0]!="NA") {
						String[]_temp=_obj.getRefSeqTxAcc();
						for(int j=0;j<_temp.length;j++) {
							_tempRefSeq=_tempRefSeq.concat(_temp[j]);
							if(j<_temp.length-1)
								_tempRefSeq=_tempRefSeq.concat("///");
							
						}
						writer.write(_tempRefSeq);
						writer.write('\t');
					} else {
						writer.write("NA");
						writer.write('\t');
					}
				} else if(index==3) {
					_tempStr=_obj.getEntrezGeneID();
					if(_tempStr!="NA") {
						writer.write(_obj.getEntrezGeneID());
						writer.write('\t');
					} else {
						writer.write("NA");
						writer.write('\t');
					}
				} else if(index==4) {
					_tempStr=_obj.getUnigeneID();
					if(_tempStr!="NA") {
						writer.write(_obj.getUnigeneID());
						writer.write('\t');
					} else {
						writer.write("NA");
						writer.write('\t');
					}
				} else if(index==5) {
					_tempStr=_obj.getGeneSymbol();
					if(_tempStr!="NA") {
						writer.write(_obj.getGeneSymbol());
						writer.write('\t');
					} else {
						writer.write("NA");
						writer.write('\t');
					}
				} else if(index==6) {
					_tempStr=_obj.getGeneTitle();
					if(_tempStr!="NA") {
						writer.write(_obj.getGeneTitle());
						writer.write('\t');
					} else {
						writer.write("NA");
						writer.write('\t');
					}
				} else if(index==7) {
					_tempStr=_obj.getProbeChromosome();
					if(_tempStr!="NA") {
						writer.write("chr"+_obj.getProbeChromosome()+":"+_obj.getProbeTxStartBP()+"-"+
								_obj.getProbeTxEndBP()+"("+_obj.getProbeStrand()+")");
						writer.write('\t');
					} else {
						writer.write("NA");
						writer.write('\t');
						
					}
				} else if(index==8) {	
					_temparray=_obj.getGoTerms();
					if(_temparray[0]!=null) {
						String[]_temp=_obj.getGoTerms();
						for(int j=0;j<_temp.length;j++) {
							_tmpGO=_tmpGO.concat(_temp[j]);
							if(j<_temp.length-1)
								_tmpGO=_tmpGO.concat("///");
							
						}
						writer.write(_tmpGO);
						writer.write('\t');
					} else {
						writer.write("NA");
						writer.write('\t');
					}
				} else if(index==9) {	
					_tempStr=_obj.getTgiTC();
					if(_tempStr!="NA") {
					writer.write(_obj.getTgiTC());
					writer.println();
				
				} else {
					writer.write("NA");
					writer.println();
				}
				
				
				}

			}
		
		}

		writer.close();
		//System.out.println("File size:"+iAnnotationFile.length());
		

	
	 }
}
