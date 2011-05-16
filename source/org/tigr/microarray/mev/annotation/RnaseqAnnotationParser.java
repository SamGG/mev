package org.tigr.microarray.mev.annotation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JOptionPane;

import org.tigr.util.StringSplitter;

import org.tigr.microarray.util.CSVReader;

public class RnaseqAnnotationParser {

	Vector columnNames=new Vector();
	Hashtable<String, ArrayList<MevAnnotation>> annoHash;
	int num_of_skippedLines=0;


	public static RnaseqAnnotationParser createAnnotationFileParser(File file) throws IOException {
		RnaseqAnnotationParser newParser = new RnaseqAnnotationParser();
		newParser.loadAnnotation(file);
		return newParser;
	}

	/**
	 *  
	 * @author Raktim
	 */
	private RnaseqAnnotationParser() {}

	private void loadAnnotation(File annotationFile)throws IOException {
		//System.out.println("Parsing annotaiton File: " + annotationFile.getAbsolutePath());
		String[] currentLine;
		String probeID;
		ArrayList<MevAnnotation> annotationObjList; 
		this.columnNames=getColumnHeader(annotationFile);
		String _temp="";

		int numLines = this.getCountOfLines(annotationFile);
		annoHash = new Hashtable<String, ArrayList<MevAnnotation>>(numLines);
		BufferedReader reader = new BufferedReader(new FileReader(annotationFile));

		CSVReader csvreader = new CSVReader( reader, '\t', CSVReader.DEFAULT_QUOTE_CHARACTER);
		csvreader.readNext(); //skip header
		
		// Each item in the annotation List will have a unique ID. It will be unique across
		// the whole collection of annotation objects for the file.
		int fakeCloneID = 1;
		while((currentLine=csvreader.readNext())!=null){
			
			MevAnnotation annotationObj = new MevAnnotation();
			
			// In rnaseq we do not have a unique probe id, the ref id or transcript id is
			// used. But it is not unique (i.e. there can be multiple rows with same ref)
			// So  ref is is used as a key which points to a list of Annotation Objs
			probeID="";

			for(int i = 0; i < columnNames.size(); i++){
				_temp=(String)currentLine[i];	
				String field=((String)columnNames.get(i));
				int index=columnNames.indexOf((Object)field);

				//The match to CLONE_ID is here for backwards-compatibility with old state-saving analysis files
				//that used CLONE_ID.
				// The above comment is inherited. I do not know what it means - Raktim
				// Probe Id is expected as the first field in the file.
				if((field.equalsIgnoreCase(AnnotationFieldConstants.PROBE_ID ) || field.equalsIgnoreCase("CLONE_ID")) && index==i){
					probeID=_temp;				
					annotationObj.setCloneID(String.valueOf(fakeCloneID++));
					//System.out.println("clone id:"+probeID+":"+index);
				}else if(field.equalsIgnoreCase(AnnotationFieldConstants.REFSEQ_ACC)&&index==i){
			 		annotationObj.setRefSeqTxAcc(new String[] {_temp});
			 		// For rnaseq data CLONE_ID = REFSEQ_ACC = GENBANK_ACC
			 		annotationObj.setGenBankAcc(_temp);
			 		//System.out.println("RefSeq Acc:"+_temp);
				}else if(field.equalsIgnoreCase(AnnotationFieldConstants.CHR)&&index==i){
					annotationObj.setProbeChromosome(parseChr(_temp));
					//System.out.println("Chr:"+parseChr(_temp));
				}else if(field.equalsIgnoreCase(AnnotationFieldConstants.TX_START)&&index==i){
					try {
						annotationObj.setProbeTxStartBP(_temp);
						//System.out.println("Start:"+_temp);
					} catch (Exception e) {
						System.out.println("Bad rec Id: " + probeID);
						e.printStackTrace();
						continue;
					}
				}else if(field.equalsIgnoreCase(AnnotationFieldConstants.TX_END)&&index==i){
					try {
						annotationObj.setProbeTxEndBP(_temp);
						//System.out.println("End:"+_temp);
					} catch (Exception e) {
						System.out.println("Bad rec Id: " + probeID);
						e.printStackTrace();
						continue;
					}
				}else if(field.equalsIgnoreCase(AnnotationFieldConstants.GENE_TITLE)&&index==i){ 
					annotationObj.setGeneTitle(_temp);
					//System.out.println("Gene_Title:"+_temp+":"+index);
				}else if(field.equalsIgnoreCase(AnnotationFieldConstants.GENE_SYMBOL)&&index==i){ 
					if(_temp==null){
						_temp="";
					}
					annotationObj.setGeneSymbol(_temp);
					//System.out.println("Gene Symbol:"+_temp);
				}else if(field.equalsIgnoreCase(AnnotationFieldConstants.ENTREZ_ID)&&index==i){
					if(_temp==null){
						_temp="";
					}
					annotationObj.setLocusLinkID(_temp);
					//System.out.println("Entrez id:"+_temp+":"+index);
				}else if(field.equalsIgnoreCase(AnnotationFieldConstants.PROTEIN_ACC)&&index==i){
					if(_temp==null){
						_temp="";
					}
					annotationObj.setRefSeqProtAcc(new String[] {_temp});
				}
			}//For loop ends...
		
			///chr6:30964144-30975910 (+)
			try {
				annotationObj.setProbeCytoband(
						annotationObj.getChrLocation() +
						":" +
						annotationObj.getProbeTxStartBP() +
						"-" +
						annotationObj.getProbeTxEndBP() +
						" (" +
						annotationObj.getProbeStrand());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			// Blanks for the rest of the fields
			// All fields must have at least empty strings other wise there i problem
			// someone has hacked the annotation model beyond imagination
			
			annotationObj.setGoTerms(new String[]{""});
			annotationObj.setUnigeneID("");
			annotationObj.setTgiTC("");
			
			// System.out.println("Current Rec ID Parsed:" + probeID);
			
			if(probeID!=null) {
				// Check if key exist by the name
				//if (probeID.equals("NM_031921"))
					//System.out.println("NM_031921 found and loaded");
				if(annoHash.containsKey(probeID)) {
					annoHash.get(probeID).add(annotationObj);
				} else {
					annotationObjList = new ArrayList<MevAnnotation>();
					annotationObjList.add(annotationObj);
					annoHash.put(probeID, annotationObjList);
				}
			}
			else{
				String eMsg = "<html>Probe ID Missing..This is a REQUIRED field <br>" +
				"<html>The following descriptor was found for this probe<br> "+
				annotationObj.getProbeDesc();
				JOptionPane.showMessageDialog(null, eMsg, "Error", JOptionPane.INFORMATION_MESSAGE);
			}	
		}
		
		// TODO Denug only
		/* TBR
		int annoCount = 0;
		Enumeration<String> en = annoHash.keys();
		while(en.hasMoreElements()) {
			String t = (String)en.nextElement();
			annoCount += ((ArrayList<MevAnnotation>)annoHash.get(t)).size();
		}
		System.out.println("Annotaiton DB HashSize, size: " + annoHash.size() + " #of keys with List " + annoCount);
		*/ //End Debug
	}

	private String parseChr(String temp) {
		if (temp.toLowerCase().startsWith("chr"))
			temp = temp.substring(3);
		
		if (temp.length() > 2) 
			try {
				return String.valueOf(Integer.parseInt(temp.substring(0, 2)));
			} catch (NumberFormatException nfe) {
				return temp.substring(0, 1);
			}
		else 
			return temp.toUpperCase();
	}

	public Hashtable<String, ArrayList<MevAnnotation>> getAnnotation(){
		return annoHash;
	}

	private Vector<String> getColumnHeader(File targetFile) throws IOException {

		BufferedReader reader = new BufferedReader(new FileReader(targetFile));
		StringSplitter split = new StringSplitter('\t');
		String currentLine;

		currentLine=reader.readLine();

		//Extracting column names and column positions 
		//Remove any leading and trailing spaces
		currentLine=currentLine.trim();
		split.init(currentLine);
		int columnNumber=0;
		Vector<String> columnNames=new Vector<String>(split.countTokens());	
		while(split.hasMoreTokens()){
			String _temp=split.nextToken().trim();
			_temp=_temp.replace('"', ' ');
			_temp=removeAllSpaces(_temp);

			if(_temp.contains("PROBE_ID")){
				columnNames.add(columnNumber,AnnotationFieldConstants.PROBE_ID);
			}else if(_temp.equalsIgnoreCase("UNIGENE_ID")){
				columnNames.add(columnNumber, AnnotationConstants.UNIGENE_ID);
			}else if(_temp.contentEquals("GENE_TITLE")){
				columnNames.add(columnNumber, AnnotationConstants.GENE_TITLE);
			}else if(_temp.contentEquals("GENE_SYMBOL")){
				columnNames.add(columnNumber, AnnotationFieldConstants.GENE_SYMBOL);
			}else if(_temp.contentEquals("CYTOBAND")){
				columnNames.add(columnNumber, AnnotationFieldConstants.CYTOBAND);
			}else if(_temp.contentEquals("ENTREZ_ID")){
				columnNames.add(columnNumber, AnnotationFieldConstants.ENTREZ_ID);
			}else if(_temp.equalsIgnoreCase("PROTEIN_ACC")){
				columnNames.add(columnNumber, AnnotationFieldConstants.PROTEIN_ACC);
			}else if(_temp.equalsIgnoreCase("CHR")){
				columnNames.add(columnNumber, AnnotationFieldConstants.CHR);
			}else if(_temp.equalsIgnoreCase("TX_END")){
				columnNames.add(columnNumber, AnnotationFieldConstants.TX_END);
			}else if(_temp.equalsIgnoreCase("TX_START")){
				columnNames.add(columnNumber, AnnotationFieldConstants.TX_START);
			}else if(_temp.equalsIgnoreCase("REFSEQ_ACC")){
				columnNames.add(columnNumber, AnnotationFieldConstants.REFSEQ_ACC);
			}else if(_temp.equalsIgnoreCase("GENBANK_ACC")){
				columnNames.add(columnNumber, AnnotationFieldConstants.GENBANK_ACC);
			}else{
				columnNames.add(columnNumber, _temp);
			}
			columnNumber=columnNumber+1;
		}
		reader.close();

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

		String _temp = temp.substring(0,index).trim();
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
			e.printStackTrace();
		}
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
