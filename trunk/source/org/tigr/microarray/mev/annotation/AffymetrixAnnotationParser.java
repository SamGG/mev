package org.tigr.microarray.mev.annotation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JOptionPane;

import org.tigr.util.StringSplitter;

import org.tigr.microarray.util.CSVReader;

public class AffymetrixAnnotationParser {
	
	Vector columnNames=new Vector();
	Hashtable<String, MevAnnotation> annoHash;
	int num_of_skippedLines=0;
	
	
	public static AffymetrixAnnotationParser createAnnotationFileParser(File affyFile) throws IOException {
		AffymetrixAnnotationParser newParser = new AffymetrixAnnotationParser();
       	newParser.loadAffyAnnotation(affyFile);
		return newParser;
	}

	/**
	 *  
	 * @author Sarita Nair
	 */
    private AffymetrixAnnotationParser() {}

    private void loadAffyAnnotation(File annotationFile)throws IOException{
    	
    	String[] currentLine;
    	String probeID;
    	int counter = 0;
    	MevAnnotation annotationObj; 
    	this.columnNames=getColumnHeader(annotationFile);
    	String _temp=ChipAnnotationFieldConstants.NOT_AVAILABLE;
    	Vector<String> _tmpGo=new Vector<String>();

    	int numLines = this.getCountOfLines(annotationFile);
    	annoHash = new Hashtable<String, MevAnnotation>(numLines);
    	BufferedReader reader = new BufferedReader(new FileReader(annotationFile));
    	
    	CSVReader csvreader = new CSVReader( reader, CSVReader.DEFAULT_SEPARATOR, CSVReader.DEFAULT_QUOTE_CHARACTER, get_Lines_To_Skip());
 
    	while((currentLine=csvreader.readNext())!=null){
    		annotationObj = new MevAnnotation(); 
    		probeID="";
    		 _tmpGo = new Vector<String>();
    		
    		for(int i = 0; i < columnNames.size(); i++){

    		 _temp=(String)currentLine[i];	
    			String field=((String)columnNames.get(i));
    			int index=columnNames.indexOf((Object)field);
    		
    			
    			if(field.equalsIgnoreCase(AnnotationFieldConstants.CLONE_ID)&&index==i){
    				probeID=_temp;
    				annotationObj.setCloneID(_temp);
    		//		System.out.println("clone id:"+probeID+":"+index);
    			}else if(field.equalsIgnoreCase(AnnotationFieldConstants.GENBANK_ACC)&&index==i){
    				if(_temp==null){
    					_temp=ChipAnnotationFieldConstants.NOT_AVAILABLE;
    				}
    				annotationObj.setGenBankAcc(_temp);
    				//System.out.println("Genbank acc:"+_temp+":"+index);
    			}else if(field.equalsIgnoreCase(AnnotationFieldConstants.UNIGENE_ID)&&index==i){
    				if(_temp==null){
    					_temp=ChipAnnotationFieldConstants.NOT_AVAILABLE;
    				}
    				annotationObj.setUnigeneID(_temp);
    				//System.out.println("Unigene id:"+_temp);
    			}else if(field.equalsIgnoreCase(AnnotationFieldConstants.GENE_TITLE)&&index==i){ 
    				if(_temp==""){
    					_temp=ChipAnnotationFieldConstants.NOT_AVAILABLE;
    				}
    				annotationObj.setGeneTitle(_temp);
    				//System.out.println("Gene_Title:"+_temp+":"+index);
    			}else if(field.equalsIgnoreCase(AnnotationFieldConstants.GENE_SYMBOL)&&index==i){ 
    				if(_temp==null){
    					_temp=ChipAnnotationFieldConstants.NOT_AVAILABLE;
    				}
    				annotationObj.setGeneSymbol(_temp);
    			//	System.out.println("Gene Symbol:"+_temp);
    			}else if(field.equalsIgnoreCase(AnnotationFieldConstants.CYTOBAND)&&index==i){
    				if(_temp==null){
    					_temp=ChipAnnotationFieldConstants.NOT_AVAILABLE;
    				}
    				setAlignmentInfo(_temp, annotationObj);
    				//	System.out.println("Cytoband:"+_temp);
    			}else if(field.equalsIgnoreCase(AnnotationFieldConstants.ENTREZ_ID)&&index==i){
    				if(_temp==null){
    					_temp=ChipAnnotationFieldConstants.NOT_AVAILABLE;
    				}
    				annotationObj.setLocusLinkID(_temp);
    		//		System.out.println("Entrez id:"+_temp+":"+index);
    			}else if(field.equalsIgnoreCase(AnnotationFieldConstants.PROTEIN_ACC)&&index==i){
    				if(_temp==null){
    					_temp=ChipAnnotationFieldConstants.NOT_AVAILABLE;
    				}
    				String[] proteinAcc = parseProteinIds(_temp);
    				annotationObj.setRefSeqProtAcc(proteinAcc);
    				
    			}else if(field.equalsIgnoreCase("GeneOntologyMolecularFunction")&&index==i || 
    					field.equalsIgnoreCase("GeneOntologyCellularComponent")&&index==i ||
    					field.equalsIgnoreCase("GeneOntologyBiologicalProcess")&&index==i){
    				if(_temp==null){
    					_temp=ChipAnnotationFieldConstants.NOT_AVAILABLE;
    				}
    				Vector <String>temp=parseGoTerms(_temp, "///");
    				_tmpGo.addAll(parseGoTerms(_temp, "///"));
    				//	System.out.println("GO_Terms:"+_temp);
    			}else if(field.equalsIgnoreCase(AnnotationFieldConstants.TGI_TC)&&index==i){
    				if(_temp==null){
    					_temp=ChipAnnotationFieldConstants.NOT_AVAILABLE;
    				}
    				annotationObj.setTgiTC(_temp);
    				//	System.out.println("TGI_TC:"+_temp);
    			}


    		}//For loop ends...
    		//GO term set here because Affymetrix annotation files have multiple columns with GO Terms
    		 annotationObj.setGoTerms((String[]) _tmpGo.toArray(new String[_tmpGo.size()]));
    		if(probeID!=null)
    			annoHash.put(probeID, annotationObj);
    		else{
    			String eMsg = "<html>Probe ID Missing..This is a REQUIRED field <br>" +
    			"<html>The following descriptor was found for this probe<br> "+
    			annotationObj.getProbeDesc();
    			JOptionPane.showMessageDialog(null, eMsg, "Error", JOptionPane.INFORMATION_MESSAGE);

    		}	

    	}


      
    }
    
    
    public Hashtable<String, MevAnnotation> getAffyAnnotation(){
    	return annoHash;
    }
    
    
    
    private Vector getColumnHeader(File targetFile)throws IOException {
    	
    	BufferedReader reader = new BufferedReader(new FileReader(targetFile));
    	StringSplitter split = new StringSplitter(',');
    	String currentLine;
    	
    	
    	
    	//Skip the lines that begin with #
    	while((currentLine=reader.readLine()).contains("#")){
    		num_of_skippedLines=num_of_skippedLines+1;
    		   		
    	}
    	
    	set_Lines_To_Skip(num_of_skippedLines+1);
    	
    	//Extracting column names and column positions 
    	//Remove any leading and trailing spaces
    		currentLine=currentLine.trim();
    		split.init(currentLine);
    		int columnNumber=0;
    		Vector columnNames=new Vector(split.countTokens());	
    		while(split.hasMoreTokens()){
    			String _temp=split.nextToken().trim();
    			_temp=_temp.replace('"', ' ');
    			_temp=removeAllSpaces(_temp);
    			
    			if(_temp.contains("ProbeSetID")){
    				columnNames.add(columnNumber,AnnotationFieldConstants.CLONE_ID);
    				
    			}else if(_temp.equalsIgnoreCase("UniGeneID")){
    				columnNames.add(columnNumber, AnnotationConstants.UNIGENE_ID);
    				    				
    			}else if(_temp.contentEquals("GeneTitle")){
    				columnNames.add(columnNumber, AnnotationConstants.GENE_TITLE);
    				    				
    			}else if(_temp.contentEquals("GeneSymbol")){
    				columnNames.add(columnNumber, AnnotationFieldConstants.GENE_SYMBOL);
    				    				
    			}else if(_temp.contentEquals("Alignments")){
    				columnNames.add(columnNumber, AnnotationFieldConstants.CYTOBAND);
    				    				
    			}else if(_temp.contentEquals("EntrezGene")){
    				columnNames.add(columnNumber, AnnotationFieldConstants.ENTREZ_ID);
    				    				
    			}else if(_temp.equalsIgnoreCase("RefSeqProteinID")){
    				columnNames.add(columnNumber, AnnotationFieldConstants.PROTEIN_ACC);
    			}else if(_temp.equalsIgnoreCase("GeneOntologyBiologicalProcess")){
    					columnNames.add(columnNumber, "GeneOntologyBiologicalProcess");
    			}else if(_temp.equalsIgnoreCase("GeneOntologyCellularComponent")){
    				columnNames.add(columnNumber, "GeneOntologyCellularComponent");
    			}else if(_temp.equalsIgnoreCase("GeneOntologyMolecularFunction")){
    				columnNames.add(columnNumber, "GeneOntologyMolecularFunction");
    			}else{
    				columnNames.add(columnNumber, "No element");
    			}
    			
    			_temp=new String();
    			columnNumber=columnNumber+1;
    		}
    		
      	
    	reader.close();
    	
    	return columnNames;
    }
    
    private int get_Lines_To_Skip(){
    	return num_of_skippedLines;
    }
    
    
    private void set_Lines_To_Skip(int skip_these_lines){
    	num_of_skippedLines=skip_these_lines;
    }

    private Vector<String> parseGoTerms(String _temp, String delim) {
    	Vector<String> terms = new Vector<String>();
    	StringTokenizer tokens = new StringTokenizer(_temp, delim);
    	while(tokens.hasMoreTokens()){
    		terms.add(tokens.nextToken().trim());
    	}
    	return terms;
	}
    
    
    
    
    
    
    //This will only take the first entry, in case there are multiple entries for chromosomes.
    private void setAlignmentInfo(String temp, MevAnnotation obj){
    	
    	/*
    	 * Template to parse
    	 */
    	//chr6:30964144-30975910 (+) // 95.63 // p21.33 /// chr6_cox_hap1:2304770-2316538 (+) // 95.56 // /// chr6_qbl_hap2:2103099-2114867 (+) // 95.45 //
    	if(!temp.trim().startsWith("chr")) { //Alignment info not available
    		obj.setProbeStrand(ChipAnnotationFieldConstants.NOT_AVAILABLE);
        	obj.setProbeChromosome(ChipAnnotationFieldConstants.NOT_AVAILABLE);
        	try {
        		obj.setProbeTxStartBP("-1");
        		obj.setProbeTxEndBP("-1");
        	} catch (Exception e) {
        		
        	}
    		return;
    	}
    	
    	int index = temp.indexOf("(");
    	String strand = temp.substring(index+1, index+2).trim();
    	//System.out.println("Strand:" + strand);
    	
    	String _temp = temp.substring(0,index-1).trim();
    	int chrInd = _temp.indexOf(":");
    	String chr = _temp.substring(0, chrInd);
    	chr = chr.substring(3, chr.length());
    	//System.out.println("chr:" + chr);
    	
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
    
    
    public String removeAllSpaces(String str){
    	String newString=new String();
    	StringSplitter split=new StringSplitter(' ');
    	split.init(str);
    	while(split.hasMoreTokens()){
    		newString=newString+split.nextToken().trim();
    		
    	}
    
    	return newString;
    	
    }
    
public static void main(String[] args){
	try{
	AffymetrixAnnotationParser aap=AffymetrixAnnotationParser.createAnnotationFileParser(new File("C:/Users/sarita/Desktop/HG-U133A_2.csv"));
	Hashtable temp=new Hashtable();
	temp=aap.getAffyAnnotation();
	
	System.out.println("SIze of hashtable is:"+temp.size());	
	
	}catch(Exception e){
		e.printStackTrace();
	}
}
    
    
    
    
}
