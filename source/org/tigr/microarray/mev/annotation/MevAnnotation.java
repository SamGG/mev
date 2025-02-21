/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
package org.tigr.microarray.mev.annotation;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Vector;

import org.tigr.microarray.mev.cgh.CGHUtil.CGHUtility;
import org.tigr.microarray.mev.cgh.DBObj.DSqlHandler;

/**
 * @author Raktim
 *
 */

public class MevAnnotation implements IAnnotation, Comparable {

	private Hashtable<String, Object> annotHash;
	
	
	
	public MevAnnotation() {
		annotHash = new Hashtable<String, Object>();
		
	}
	private MevAnnotation(Hashtable<String, Object> newhash) {
		annotHash = newhash;
		
	}
	
	
	 public int compareTo(Object arg0) {
		IAnnotation _Itemp = (MevAnnotation)arg0;
		try {
			
			if(this.getProbeChromosomeAsInt()==-1) {
				return -1;
			}
			
			if(this.getProbeChromosomeAsInt() > _Itemp.getProbeChromosomeAsInt()){
				return -1;
			}
			if(this.getProbeChromosomeAsInt() < _Itemp.getProbeChromosomeAsInt()){
				return 1;
			}
			if(this.getProbeChromosomeAsInt() == _Itemp.getProbeChromosomeAsInt()){
				if(this.getProbeTxStartBP() > _Itemp.getProbeTxEndBP()){
					return -1;
				}
				if(this.getProbeTxStartBP() < _Itemp.getProbeTxEndBP()){
					return 1;
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	public String[] getRefSeqTxAcc(){
		String[]_temp=(String[])annotHash.get(AnnotationFieldConstants.REFSEQ_ACC);
		if(_temp==null) {
			_temp=new String[1];
			_temp[0]=ChipAnnotationFieldConstants.NOT_AVAILABLE;
			return _temp;
		}else
			return _temp; 
	}
	
	public String getGenBankAcc(){
		String _temp=(String)annotHash.get(AnnotationFieldConstants.GENBANK_ACC);
		if(_temp==null) {
			_temp=ChipAnnotationFieldConstants.NOT_AVAILABLE;
			return _temp;
		}else
			return _temp;
		//return (String)annotHash.get(AnnotationFieldConstants.GENBANK_ACC);
	}
	
	public String getGeneID(){
		String _temp=(String)annotHash.get(AnnotationFieldConstants.ENTREZ_ID);
		if(_temp==null) {
			_temp=ChipAnnotationFieldConstants.NOT_AVAILABLE;
			return _temp;
		}else
			return _temp;

		
	//	return (String)annotHash.get(AnnotationFieldConstants.ENTREZ_ID);
	}
	
	public String getEntrezGeneID(){
		String _temp=(String)annotHash.get(AnnotationFieldConstants.ENTREZ_ID);
		if(_temp==null) {
			_temp=ChipAnnotationFieldConstants.NOT_AVAILABLE;
			return _temp;
		}else
			return _temp;

		//return (String)annotHash.get(AnnotationFieldConstants.ENTREZ_ID);
	}
	
	public String getLocusLinkID(){
		String _temp=(String)annotHash.get(AnnotationFieldConstants.ENTREZ_ID);
		if(_temp==null) {
			_temp=ChipAnnotationFieldConstants.NOT_AVAILABLE;
			return _temp;
		}else
			return _temp;

		//return (String)annotHash.get(AnnotationFieldConstants.ENTREZ_ID);
	}
	
		
	public String getGeneSymbol(){
		String _temp=(String)annotHash.get(AnnotationFieldConstants.GENE_SYMBOL);
		if(_temp==null) {
			_temp=ChipAnnotationFieldConstants.NOT_AVAILABLE;
			return _temp;
		}else
			return _temp;

		//return (String)annotHash.get(AnnotationFieldConstants.GENE_SYMBOL);
	}
	
	public String getGeneTitle(){
		String _temp=(String)annotHash.get(AnnotationFieldConstants.GENE_TITLE);
		if(_temp==null) {
			_temp=ChipAnnotationFieldConstants.NOT_AVAILABLE;
			return _temp;
		}else
			return _temp;

	//	return (String)annotHash.get(AnnotationFieldConstants.GENE_TITLE);
	}
	
	public String getProbeCytoband(){
		String _temp=(String)annotHash.get(AnnotationFieldConstants.CYTOBAND);
		if(_temp==null) {
			_temp=ChipAnnotationFieldConstants.NOT_AVAILABLE;
			return _temp;
		}else
			return _temp;

		//return (String)annotHash.get(AnnotationFieldConstants.CYTOBAND);
	}

	public String getChrLocation(){
		String _temp=(String)annotHash.get(AnnotationFieldConstants.CYTOBAND);
		if(_temp == null) {
			String chr;
			String chrLocation = "";
			int start, end;
			chr = "chr" + getProbeChromosome();
			if(chr.equals(ChipAnnotationFieldConstants.NOT_AVAILABLE))
				return ChipAnnotationFieldConstants.NOT_AVAILABLE;
			chrLocation = chr + ":";
			try {
				start = getProbeTxStartBP();
				end = getProbeTxEndBP();
				chrLocation += start + "-" + end;
			} catch (Exception e) {
				//Failed to get start and end values, leave them off
			}
			return chrLocation;
		}
		return _temp;
	}

	public String getCloneID() {
		return (String)annotHash.get(AnnotationFieldConstants.PROBE_ID);
	}
	
	public String getProbeChromosome(){
		String _temp=(String)annotHash.get(AnnotationFieldConstants.CHR);
		if(_temp==null) {
			_temp=ChipAnnotationFieldConstants.NOT_AVAILABLE;
			return _temp;
		}else
			return _temp;
	//	return (String)annotHash.get(AnnotationFieldConstants.CHR);
	}
	
	public int getProbeChromosomeAsInt() throws Exception {
		String _temp=(String)annotHash.get(AnnotationFieldConstants.CHR);
		if(_temp==null) {
			return -1;
		}else
		return Integer.parseInt((String)annotHash.get(AnnotationFieldConstants.CHR));
	}
	
	 public String getProbeStrand(){
		 String _temp=(String)annotHash.get(AnnotationFieldConstants.STRAND);
			if(_temp==null) {
				_temp=ChipAnnotationFieldConstants.NOT_AVAILABLE;
				return _temp;
			}else
				return _temp;

	//	return (String)annotHash.get(AnnotationFieldConstants.STRAND);
		}
	 
	 public int getProbeTxStartBP() throws Exception {
		 String _temp=(String)annotHash.get(AnnotationFieldConstants.TX_START);
			if(_temp==null) {
				return -1;
			}else
				return Integer.parseInt((String)annotHash.get(AnnotationFieldConstants.TX_START));
			
		//	return Integer.parseInt((String)annotHash.get(AnnotationFieldConstants.TX_START));
		}
		
		public int getProbeTxEndBP() throws Exception {
			String _temp=(String)annotHash.get(AnnotationFieldConstants.TX_END);
			if(_temp==null) {
				return -1;
			}else
				return Integer.parseInt((String)annotHash.get(AnnotationFieldConstants.TX_END));
			
			//return Integer.parseInt((String)annotHash.get(AnnotationFieldConstants.TX_END));
		}
	

	public String getProbeSequence(){
		String _temp=(String)annotHash.get(AnnotationFieldConstants.SEQUENCE);
		if(_temp==null) {
			_temp=ChipAnnotationFieldConstants.NOT_AVAILABLE;
			return _temp;
		}else
			return _temp;

		//return (String)annotHash.get(AnnotationFieldConstants.SEQUENCE);
	}
  
	public String[] getRefSeqProtAcc(){
		String[] _temp=(String[])annotHash.get(AnnotationFieldConstants.PROTEIN_ACC);
		if(_temp==null) {
			_temp=new String[1];
			_temp[0]=ChipAnnotationFieldConstants.NOT_AVAILABLE;
			return _temp;
		}else
			return _temp;

	//	return (String[])annotHash.get(AnnotationFieldConstants.PROTEIN_ACC);
	}
 
 	
	public int getProbeTxLengthInBP() throws Exception {
		String _temp=(String)annotHash.get(AnnotationFieldConstants.TX_END);
		if(_temp==null) {
			_temp=ChipAnnotationFieldConstants.NOT_AVAILABLE;
			//System.out.println("if na:"+Integer.parseInt(_temp));
			return Integer.parseInt(_temp);
		}else
			return Integer.parseInt((String)annotHash.get(AnnotationFieldConstants.TX_END)) - Integer.parseInt((String)annotHash.get(AnnotationFieldConstants.TX_START));
		
		
		//return Integer.parseInt((String)annotHash.get(AnnotationFieldConstants.TX_END)) - Integer.parseInt((String)annotHash.get(AnnotationFieldConstants.TX_START));
	}
	
	public int getProbeCdsStartBP() throws Exception {
		return Integer.parseInt((String)annotHash.get(AnnotationFieldConstants.CDS_START));
	}
	
	public int getProbeCdsEndBP()throws Exception {
		return Integer.parseInt((String)annotHash.get(AnnotationFieldConstants.CDS_END));
	}
	
	public int getProbeCdsLengthInBP()throws Exception {
		return Integer.parseInt((String)annotHash.get(AnnotationFieldConstants.CDS_END)) - Integer.parseInt((String)annotHash.get(AnnotationFieldConstants.CDS_START));
	}
	
	public String[] getBioCartaPathways() {
		String[] _temp=(String[])annotHash.get(AnnotationFieldConstants.BIO_CARTA);
		if(_temp==null) {
			_temp=new String[1];
			_temp[0]=ChipAnnotationFieldConstants.NOT_AVAILABLE;
			return _temp;
		}else
			return _temp;

	//	return (String[])annotHash.get(AnnotationFieldConstants.BIO_CARTA);
	}
	
	public String[] getKeggPathways() {
		String[] _temp=(String[])annotHash.get(AnnotationFieldConstants.KEGG);
		if(_temp==null) {
			_temp=new String[1];
			_temp[0]=ChipAnnotationFieldConstants.NOT_AVAILABLE;
			return _temp;
		}else
			return _temp;

		//return (String[])annotHash.get(AnnotationFieldConstants.KEGG);
	}
	
	
	
	public String getProbeDesc(){
		String _temp=(String)annotHash.get(AnnotationFieldConstants.DESC);
		if(_temp==null) {
			_temp=ChipAnnotationFieldConstants.NOT_AVAILABLE;
			return _temp;
		}else
			return _temp;

		//return (String)annotHash.get(AnnotationFieldConstants.DESC);
	}
	
	public String getUnigeneID() {
		String _temp=(String)annotHash.get(AnnotationFieldConstants.UNIGENE_ID);
		if(_temp==null) {
			_temp=ChipAnnotationFieldConstants.NOT_AVAILABLE;
			return _temp;
		}else
			return _temp;

	//	return (String)annotHash.get(AnnotationFieldConstants.UNIGENE_ID);
	}

	

	
	public String[] getGoTerms() {
		String[] _temp=(String[])annotHash.get(AnnotationFieldConstants.GO_TERMS);
		if(_temp==null) {
			_temp=new String[1];
			_temp[0]=ChipAnnotationFieldConstants.NOT_AVAILABLE;
			return _temp;
		}else
			return _temp;

	//	return(String[]) annotHash.get(AnnotationFieldConstants.GO_TERMS);
	}

	public String getTgiTC(){
		String _temp=(String)annotHash.get(AnnotationFieldConstants.TGI_TC);
		if(_temp==null) {
			_temp=ChipAnnotationFieldConstants.NOT_AVAILABLE;
			return _temp;
		}else
			return _temp;
		//return (String)annotHash.get(AnnotationFieldConstants.TGI_TC);
	}
	
	
	/* Setters */
	public void setCloneID(String _temp) {
		//String[] _tmp = {_temp};
		String _tmp = _temp;
		annotHash.put(AnnotationFieldConstants.PROBE_ID, _tmp);
	}

	public void setEntrezGeneID(String _temp) {
		//String[] _tmp ={ _temp};
		String _tmp = _temp;
		annotHash.put(AnnotationFieldConstants.ENTREZ_ID, _tmp);
	}


	
	public void setGenBankAcc(String _temp) {
	//	String[] _tmp = {_temp};
		String _tmp = _temp;
		annotHash.put(AnnotationFieldConstants.GENBANK_ACC, _tmp);
	}
	
	

	public void setGeneID(String _temp) {
		//String[] _tmp = {_temp};
		String _tmp = _temp;
		annotHash.put(AnnotationFieldConstants.ENTREZ_ID, _tmp);
	}

	public void setGeneSymbol(String _temp) {
		//String[] _tmp = {_temp};
		String _tmp = _temp;
		annotHash.put(AnnotationFieldConstants.GENE_SYMBOL, _tmp);
	}

	public void setGeneTitle(String _temp) {
		//String[] _tmp = {_temp};
		String _tmp = _temp;
		annotHash.put(AnnotationFieldConstants.GENE_TITLE, _tmp);
	}

	public void setLocusLinkID(String _temp) {
		//String[] _tmp = {_temp};
		String _tmp = _temp;
		annotHash.put(AnnotationFieldConstants.ENTREZ_ID, _tmp);
	}

	public void setProbeCdsEndBP(String _temp) throws Exception {
		//String[] _tmp = {_temp};
		String _tmp = _temp;
		annotHash.put(AnnotationFieldConstants.CDS_END, _tmp);
	}

	public void setProbeCdsStartBP(String _temp) throws Exception {
		//String[] _tmp = {_temp};
		String _tmp = _temp;
		annotHash.put(AnnotationFieldConstants.CDS_START, _tmp);
	}

	public void setProbeChromosome(String _temp) {
		//String[] _tmp = {_temp};
		String _tmp = _temp;
		annotHash.put(AnnotationFieldConstants.CHR, _tmp);
	}

	public void setProbeCytoband(String _temp) {
		//String[] _tmp = {_temp};
		String _tmp = _temp;
		annotHash.put(AnnotationFieldConstants.CYTOBAND, _tmp);
	}

	public void setProbeDesc(String _temp) {
		//String[] _tmp = {_temp};
		String _tmp = _temp;
		annotHash.put(AnnotationFieldConstants.DESC, _tmp);
	}
	
	public void setProbeStrand(String _temp) {
		//String[] _tmp = {_temp};
		String _tmp = _temp;
		annotHash.put(AnnotationFieldConstants.STRAND, _tmp);
	}

	public void setProbeTxEndBP(String _temp) throws Exception {
	//	String[] _tmp = {_temp};
		String _tmp = _temp;
		annotHash.put(AnnotationFieldConstants.TX_END, _tmp);
	}

	public void setProbeTxStartBP(String _temp) throws Exception {
		//String[] _tmp = {_temp};
		String _tmp = _temp;
		annotHash.put(AnnotationFieldConstants.TX_START, _tmp);
	}

	public void setProbeSequence(String _temp) {
		//String[] _tmp = {_temp};
		String _tmp = _temp;
		annotHash.put(AnnotationFieldConstants.SEQUENCE, _tmp);
	}

	

	public void setUnigeneID(String _temp) {
	//	String[] _tmp = {_temp};
		String _tmp = _temp;
		annotHash.put(AnnotationFieldConstants.UNIGENE_ID, _tmp);
	}
	
	public void setRefSeqProtAcc(String[] _temp) {
		annotHash.put(AnnotationFieldConstants.PROTEIN_ACC, _temp);
	}

	
	public void setRefSeqTxAcc(String[] _temp) {
		annotHash.put(AnnotationFieldConstants.REFSEQ_ACC, _temp);
	}

	public void setBioCartaPathways(String[] _temp) {
		annotHash.put(AnnotationFieldConstants.BIO_CARTA, _temp);
	}

	

	public void setKeggPathways(String[] _temp) {
		annotHash.put(AnnotationFieldConstants.KEGG, _temp);
	}
	
	
	public void setGoTerms(String[] _temp) {
		annotHash.put(AnnotationFieldConstants.GO_TERMS, _temp);
	}
	
	public void setTgiTC(String _temp) {
		annotHash.put(AnnotationFieldConstants.TGI_TC, _temp);
	}
	
	
	/**
	 * Static Functions to extract Genome Information
	 * E.g. Genes, RefSeq etc for a given co-ordinate range 
	 */
	public static Vector<String[]> getGenomeAnnotation(String[] colNames, int chr, int st_bp, int end_bp) throws SQLException {
		Vector<String[]> annoVec = new Vector<String[]>();
		
		if(validateColNames(colNames)) {
			String db = "Hs_RefGenesMapped";
			String sql = "SELECT ";
			for(int j = 0; j < colNames.length; j++){
				if(j < colNames.length-1)
					sql += colNames[j] + ", ";
				else
					sql += colNames[j] + " ";
			}
			sql += "FROM \""+db+"\"";
			sql += " WHERE CHR = " +  CGHUtility.encap("chr"+chr);
			sql += " AND TX_START >= " + st_bp + " AND TX_END <= " + end_bp;
			
		//	System.out.println("Query: " + sql);
			DSqlHandler objPersist = new DSqlHandler();
	        ResultSet rs = objPersist.fetchItemsCSV(sql);
	        
	        String[] results;
	        try {
		        while(rs.next()) {
		        	results = new String[colNames.length];
			        for(int i=0; i < colNames.length; i++){
			        	results[i] = rs.getString(colNames[i]);
			        }
			        annoVec.add(results);
		        }
	        } catch (SQLException e) {
	        	throw e;
	        }
		}
		else  {
			throw new SQLException("Invalid Column Names");
		}
        return annoVec;
	}
	
	/**
	 * TODO
	 * @param species
	 * @return
	 */
	private static String getDBNameBySpecies(String species) {
    	String dbName = "";
    	if (species.equals("HS")) {
    		dbName = "Hs_RefGenesMapped";
    	}
    	if (species.equals("MM")) {
    		dbName = "Mm_RefGenesMapped";
    	}
    	return dbName;
    }
	
	
	/**
	 * A function that uses Java reflection to look-up the  
	 * columns that are avialable for the API's to query on.
	 */
	private static Boolean validateColNames(String[] _temp){
		Boolean ret = false;
		Class c = fieldConsts.getClass();
		Field[] fields = c.getFields();
		for(int i = 0; i < _temp.length; i++) {
			for(int j = 0; j < fields.length; j++) {
				if(_temp[i].equals(fields[j].getName())) {
					ret = true;
					break;
				}
			}
			if(ret == false) break;
		}
		return ret;
	}
	
	/**
	 * 
	 * @return
	 * @deprecated
	
	public static String[] getFieldNames() {
		Vector<String> names = new Vector<String>();
		Class c = fieldConsts.getClass();
		Field[] fields = c.getFields();
		
		for(int j = 0; j < fields.length; j++) {
			names.add(fields[j].getName());
//			System.out.println("Field: " + j + " " + fields[j].getName());
		}
	
		return names.toArray(new String[0]);
	}
 */
	/**
	 * 
	 */
	public String[] getAttribute(String attr) {
		if(attr == AnnotationFieldConstants.CHR_LOCATION) {
			return new String[]{getChrLocation()};
		}
	//	if(annotHash.size()>1) {
		if((annotHash.get(attr)) instanceof String) {
			//System.out.println("getAttribute():"+annotHash.get(attr).getClass().getName());
			String[]_temp=new String[1];
			String temp;
			temp=(String)annotHash.get(attr);
			_temp[0]=temp;
			
			if(temp==null) {
				_temp[0]=ChipAnnotationFieldConstants.NOT_AVAILABLE;
			}
			return _temp;


		} else {

			String[] _temp;
			_temp = (String[])annotHash.get(attr);
			if (_temp == null) {
				_temp = new String[1];
				_temp[0] = ChipAnnotationFieldConstants.NOT_AVAILABLE;
				//System.out.println("getAttribute():MevAnnotation "+_temp[0] );

			}
			return _temp;
		}
		
		/*}else {
			String[]_temp=new String[1];
			_temp[0]=ChipAnnotationFieldConstants.NOT_AVAILABLE;
			return _temp;
		}*/
			
		
	}
	
	/**
	 * 
	 */
	public AnnoAttributeObj getAttributeObj(String attr) {
		if(attr.equals(AnnotationFieldConstants.CHR_LOCATION))
			return new AnnoAttributeObj(AnnotationFieldConstants.CHR_LOCATION, new String[]{getChrLocation()});
		
		String[] _temp;
		AnnoAttributeObj _tempAttr;
		try {
			_temp = (String[])annotHash.get(attr);
		} catch (ClassCastException cce) {
			_temp = new String[]{(String)annotHash.get(attr)};
		}
		if (_temp == null) {
			_temp = new String[1];
			_temp[0] = ChipAnnotationFieldConstants.NOT_AVAILABLE;
			_tempAttr = new AnnoAttributeObj(attr, _temp);
			return _tempAttr;
		}
		_tempAttr = new AnnoAttributeObj(attr, _temp);
		return _tempAttr;
	}


	public IAnnotation clone() {
		Hashtable<String, Object> newhash = new Hashtable<String, Object>(annotHash);
		return new MevAnnotation(newhash);
	}
	


}
