package org.tigr.microarray.mev.annotation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JOptionPane;

import org.tigr.microarray.mev.MultipleArrayViewer;
import org.tigr.util.StringSplitter;

public class AnnotationFileReader {
	String[] fieldNames=new String[0];
	Vector columnNames=new Vector();
	Hashtable<String, MevAnnotation> annoHash;
	IChipAnnotation chipAnnotation;

	
	public static AnnotationFileReader createAnnotationFileReader(File affyFile) throws IOException {
		AnnotationFileReader newReader = new AnnotationFileReader();
       	newReader.loadAffyAnnotation(affyFile);
		return newReader;
	}

	/**
	 * Private constructer forces use of createAnnotationFileReader to 
	 * create a reader and load annotation data. 
	 * @author eleanorahowe
	 */
    private AnnotationFileReader() {}
    
    public void setAnnotation(String[] newAnnotation) {
    	fieldNames=newAnnotation;	
    }
    
    
    
    public String[] getAnnotation() {
    	return this.fieldNames;
    }
    
    public IChipAnnotation getAffyChipAnnotation() {
    	return chipAnnotation;
    }
    public Hashtable<String, MevAnnotation> getAffyAnnotation(){
    	return annoHash;
    }
    
    private void loadAffyAnnotation(File affyFIle) throws IOException {
    	//System.out.println("loadAffyAnnotation");
    	int numLines = this.getCountOfLines(affyFIle);
    	BufferedReader reader = new BufferedReader(new FileReader(affyFIle));
    	annoHash = new Hashtable<String, MevAnnotation>(numLines);
    	chipAnnotation = new MevChipAnnotation();
    	chipAnnotation.setAnnFileName(affyFIle.getAbsolutePath());
    	StringSplitter ss = new StringSplitter('\t');
    	String currentLine, probeID;

    	int counter = 0;

    	MevAnnotation annotationObj; 
    	this.columnNames=getColumnHeader(affyFIle);
    	String _temp=AnnotationFieldConstants.NOT_AVAILABLE;
    	Vector<String> _tmpGO=new Vector<String>();
    	String orgName="";
    	String chipType="";
    	
    	
    	while ((currentLine = reader.readLine()) != null) {

    		annotationObj = new MevAnnotation(); //TODO
    		probeID = "";
    		
    		while(currentLine.startsWith("#")) {

    			StringSplitter split=new StringSplitter(':');//commented out till the mav.getInstance gets solved
    			split.init(currentLine);

    			/**
    			 * The first two lines of the annotation file are Array name and Organism name
    			 * The if loop parses and populates the local variables orgName and chipType.
    			 * These variables are used to setChipType and setspeciesName later
    			 * in the code.
    			 * 
    			 * 
    			 */
    			if(counter==0) {
    				split.nextToken();
    				chipType=split.nextToken();
    				
    				//Remove leading space - hack
    				//TODO should be fixed in a non-hack way
    				chipType = chipType.substring(chipType.indexOf(' ')+1);

    			}else if(counter==1) {
    				split.nextToken();
    				orgName=split.nextToken();
    				
    				//Remove leading space - hack
    				//TODO should be fixed in a non-hack way
    				orgName = orgName.substring(orgName.indexOf(' ')+1);

    			}

        		chipAnnotation.setChipType(chipType);
        		chipAnnotation.setSpeciesName(orgName);
        		
    			counter=counter+1;
    			currentLine=reader.readLine();
    			
    		}

    	    counter=0;
    		ss.init(currentLine);
          
    		for(int i = 0; i < columnNames.size(); i++){
    			
    			if(ss.hasMoreTokens()){
    			 _temp = ss.nextToken();
    			}
    			String field=(String)columnNames.get(i);
    			int index=columnNames.indexOf((Object)field);
    		
    			Vector<String> _tmpGo = new Vector<String>();
    			if(field.equalsIgnoreCase(AnnotationFieldConstants.CLONE_ID)&&index==i){
    				probeID=_temp;
    				annotationObj.setCloneID(_temp);
    				//System.out.println("clone id:"+probeID);
    			}else if(field.equalsIgnoreCase(AnnotationFieldConstants.GENBANK_ACC)&&index==i){
    				if(_temp==null){
    					_temp=AnnotationFieldConstants.NOT_AVAILABLE;
    				}
    				annotationObj.setGenBankAcc(_temp);
    				//System.out.println("Genbank acc:"+_temp);
    			}else if(field.equalsIgnoreCase(AnnotationFieldConstants.UNIGENE_ID)&&index==i){
    				if(_temp==null){
    					_temp=AnnotationFieldConstants.NOT_AVAILABLE;
    				}
    				annotationObj.setUnigeneID(_temp);
    			//	System.out.println("Unigene id:"+_temp);
    			}else if(field.equalsIgnoreCase(AnnotationFieldConstants.GENE_TITLE)&&index==i){ 
    				if(_temp==""){
    					_temp=AnnotationFieldConstants.NOT_AVAILABLE;
    				}
    				annotationObj.setGeneTitle(_temp);
    				//System.out.println("Gene_Title:"+_temp);
    			}else if(field.equalsIgnoreCase(AnnotationFieldConstants.GENE_SYMBOL)&&index==i){ 
    				if(_temp==null){
    					_temp=AnnotationFieldConstants.NOT_AVAILABLE;
    				}
    				annotationObj.setGeneSymbol(_temp);
    			//	System.out.println("Gene Symbol:"+_temp);
    			}else if(field.equalsIgnoreCase(AnnotationFieldConstants.CHR_CYTOBAND)&&index==i){
    				if(_temp==null){
    					_temp=AnnotationFieldConstants.NOT_AVAILABLE;
    				}
    				setAlignmentInfo(_temp, annotationObj);
    			//	System.out.println("Cytoband:"+_temp);
    			}else if(field.equalsIgnoreCase(AnnotationFieldConstants.ENTREZ_ID)&&index==i){
    				if(_temp==null){
    					_temp=AnnotationFieldConstants.NOT_AVAILABLE;
    				}
    				annotationObj.setLocusLinkID(_temp);
    			//	System.out.println("Entrez id:"+_temp);
    			}else if(field.equalsIgnoreCase(AnnotationFieldConstants.REFSEQ_ACC)&&index==i){
    				if(_temp==null){
    					_temp=AnnotationFieldConstants.NOT_AVAILABLE;
    				}
    				String mRnaRefSeqs[] = parsemRnaIds(_temp);
			 		annotationObj.setRefSeqTxAcc(mRnaRefSeqs);
    			//	System.out.println("RefSeq Acc:"+_temp);
    			}else if(field.equalsIgnoreCase(AnnotationFieldConstants.GO_TERMS)&&index==i){
    				if(_temp==null){
    					_temp=AnnotationFieldConstants.NOT_AVAILABLE;
    				}
    				 _tmpGo = parseGoTerms(_temp, "///"); 
    				annotationObj.setGoTerms((String[]) _tmpGo.toArray(new String[_tmpGo.size()]));
    			//	System.out.println("GO_Terms:"+_temp);
    			}else if(field.equalsIgnoreCase(AnnotationFieldConstants.TGI_TC)&&index==i){
    				if(_temp==null){
    					_temp=AnnotationFieldConstants.NOT_AVAILABLE;
    				}
    				annotationObj.setTgiTC(_temp);
    			//	System.out.println("TGI_TC:"+_temp);
    			}


    		}

    		//  System.out.println("Clone name:"+cloneName);
    		if(probeID!=null)
    			annoHash.put(probeID, annotationObj);
    		else{
    			String eMsg = "<html>Probe ID Missing..This is a REQUIRED field <br>" +
    			"<html>The following descriptor was found for this probe<br> "+
    			annotationObj.getProbeDesc();
    			JOptionPane.showMessageDialog(null, eMsg, "Error", JOptionPane.INFORMATION_MESSAGE);

    		}	

    		counter++;
    	}
    	reader.close();
    }

  
    
    private Vector getColumnHeader(File targetFile)throws IOException {
    	BufferedReader reader = new BufferedReader(new FileReader(targetFile));
    	StringSplitter split = new StringSplitter(' ');
    	StringSplitter ss = new StringSplitter('\t');
    	String currentLine;
    	Vector columnNames=new Vector();
    	while((currentLine=reader.readLine())!=null) {
    		while(!currentLine.equals("# Fields: ")){
    			currentLine=reader.readLine();
    		}
    		
    		if(currentLine.equals("# Fields: ")) {
    			String temp;
    			while((currentLine=reader.readLine()).startsWith("#")) {
    				//System.out.println("currentLine is:"+currentLine);
    			split.init(currentLine);
    			split.nextToken();
    			split.nextToken();
    			split.nextToken();
    			split.nextToken();
    			
    			temp=split.nextToken();
    			int index=Integer.parseInt(temp.substring(0, temp.indexOf(".")));
    			String ann=split.nextToken();
    			//System.out.println("index:"+index);
    		//	System.out.println("ann:"+ann);
    			columnNames.add(index-1, ann);
    			}
    			break;
    		}
    		
    	}
    	
    	
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

    
	
 
    private void setAlignmentInfo(String temp, MevAnnotation obj){
    	/*
    	 * Template to parse
    	 */
    	//chr6:30964144-30975910 (+) // 95.63 // p21.33 /// chr6_cox_hap1:2304770-2316538 (+) // 95.56 // /// chr6_qbl_hap2:2103099-2114867 (+) // 95.45 //
    	if(!temp.trim().startsWith("chr")) { //Alignment info not available
    		obj.setProbeStrand(AnnotationFieldConstants.NOT_AVAILABLE);
        	obj.setProbeChromosome(AnnotationFieldConstants.NOT_AVAILABLE);
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

    
    private String[] parseProteinIds(String _temp){
    	Vector<String> prots = new Vector<String>();
    	StringTokenizer tokens = new StringTokenizer(_temp, "///");
    	while(tokens.hasMoreTokens()){
    		prots.add(tokens.nextToken().trim());
    	}
    	
    	String[] strArray = new String[prots.size()];
    	prots.toArray(strArray);
    	return strArray;
    }
    //Function modified by Sarita
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

    
    public int getCountOfLines(File f) {
    	int numLines = 0;
    	try {
	    	BufferedReader reader = new BufferedReader(new FileReader(f));
	        while (reader.readLine() != null) {
	        	numLines++;
	        }
    	} catch (IOException e){
    		
    	}
    	return numLines;
    }
    

}
