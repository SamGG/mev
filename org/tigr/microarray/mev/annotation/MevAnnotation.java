package org.tigr.microarray.mev.annotation;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Vector;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Field;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
//import java.lang.reflect.Method;
//import java.lang.reflect.Member;

import org.tigr.microarray.mev.MultipleArrayData;
import org.tigr.microarray.mev.MultipleArrayViewer;
import org.tigr.microarray.mev.cgh.CGHUtil.CGHUtility;
import org.tigr.microarray.mev.cgh.DBObj.DSqlHandler;
import org.tigr.util.StringSplitter;

/**
 * @author Raktim
 *
 *
 *
 */

public class MevAnnotation implements IAnnotation, Comparable {

	private Hashtable<String, Object> annotHash;
	private MultipleArrayViewer viewer;
	
	/*	Added by Sarita: This variable would be set to true, in
	 * the respective file loader class, when annotation is loaded .
	 * The purpose of having this variable is to enable displaying
	 * only the available annotation fields in the Display Menu->Gene/Row Labels
	 * 
	 * This status of this variable is checked in the MultipleArrayViewer:fireDataLoaded()
	 */
	
	public MevAnnotation() {
		annotHash = new Hashtable<String, Object>();
		
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
			_temp[0]="NA";
			return _temp;
		}else
			return _temp; 
	}
	
	public String getGenBankAcc(){
		String _temp=(String)annotHash.get(AnnotationFieldConstants.GENBANK_ACC);
		if(_temp==null) {
			_temp="NA";
			return _temp;
		}else
			return _temp;
		//return (String)annotHash.get(AnnotationFieldConstants.GENBANK_ACC);
	}
	
	public String getGeneID(){
		String _temp=(String)annotHash.get(AnnotationFieldConstants.ENTREZ_ID);
		if(_temp==null) {
			_temp="NA";
			return _temp;
		}else
			return _temp;

		
	//	return (String)annotHash.get(AnnotationFieldConstants.ENTREZ_ID);
	}
	
	public String getEntrezGeneID(){
		String _temp=(String)annotHash.get(AnnotationFieldConstants.ENTREZ_ID);
		if(_temp==null) {
			_temp="NA";
			return _temp;
		}else
			return _temp;

		//return (String)annotHash.get(AnnotationFieldConstants.ENTREZ_ID);
	}
	
	public String getLocusLinkID(){
		String _temp=(String)annotHash.get(AnnotationFieldConstants.ENTREZ_ID);
		if(_temp==null) {
			_temp="NA";
			return _temp;
		}else
			return _temp;

		//return (String)annotHash.get(AnnotationFieldConstants.ENTREZ_ID);
	}
	
		
	public String getGeneSymbol(){
		String _temp=(String)annotHash.get(AnnotationFieldConstants.GENE_SYMBOL);
		if(_temp==null) {
			_temp="NA";
			return _temp;
		}else
			return _temp;

		//return (String)annotHash.get(AnnotationFieldConstants.GENE_SYMBOL);
	}
	
	public String getGeneTitle(){
		String _temp=(String)annotHash.get(AnnotationFieldConstants.GENE_TITLE);
		if(_temp==null) {
			_temp="NA";
			return _temp;
		}else
			return _temp;

	//	return (String)annotHash.get(AnnotationFieldConstants.GENE_TITLE);
	}
	
	public String getProbeCytoband(){
		String _temp=(String)annotHash.get(AnnotationFieldConstants.CYTOBAND);
		if(_temp==null) {
			_temp="NA";
			return _temp;
		}else
			return _temp;

		//return (String)annotHash.get(AnnotationFieldConstants.CYTOBAND);
	}
	
	
	
	public String getSpeciesName(){
		return (String)speciesName.get(this.getMavInstance());
	}
	

	
	public String getChipName(){
		return (String)ChipName.get(this.getMavInstance());
	}
	
	//public static String getChipType2(){
		//return (String)ChipType.get(this.getMavInstance());
	//}
	
	public String getChipType(){
		return (String)ChipType.get(this.getMavInstance());
	}

		
	public String getGenomeBuild() {
		return (String)genomeBuild.get(this.getMavInstance());
	}
	
	public String getCloneID(){
		return (String)annotHash.get(AnnotationFieldConstants.CLONE_ID);
	}
	
	public String getProbeChromosome(){
		String _temp=(String)annotHash.get(AnnotationFieldConstants.CHR);
		if(_temp==null) {
			_temp="NA";
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
				_temp="NA";
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
			_temp="NA";
			return _temp;
		}else
			return _temp;

		//return (String)annotHash.get(AnnotationFieldConstants.SEQUENCE);
	}
  
	public String[] getRefSeqProtAcc(){
		String[] _temp=(String[])annotHash.get(AnnotationFieldConstants.PROTEIN_ACC);
		if(_temp==null) {
			_temp=new String[1];
			_temp[0]="NA";
			return _temp;
		}else
			return _temp;

	//	return (String[])annotHash.get(AnnotationFieldConstants.PROTEIN_ACC);
	}
 
 	
	public int getProbeTxLengthInBP() throws Exception {
		String _temp=(String)annotHash.get(AnnotationFieldConstants.TX_END);
		if(_temp==null) {
			_temp="NA";
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
			_temp[0]="NA";
			return _temp;
		}else
			return _temp;

	//	return (String[])annotHash.get(AnnotationFieldConstants.BIO_CARTA);
	}
	
	public String[] getKeggPathways() {
		String[] _temp=(String[])annotHash.get(AnnotationFieldConstants.KEGG);
		if(_temp==null) {
			_temp=new String[1];
			_temp[0]="NA";
			return _temp;
		}else
			return _temp;

		//return (String[])annotHash.get(AnnotationFieldConstants.KEGG);
	}
	
	
	
	public String getProbeDesc(){
		String _temp=(String)annotHash.get(AnnotationFieldConstants.DESC);
		if(_temp==null) {
			_temp="NA";
			return _temp;
		}else
			return _temp;

		//return (String)annotHash.get(AnnotationFieldConstants.DESC);
	}
	
	public String getUnigeneID() {
		String _temp=(String)annotHash.get(AnnotationFieldConstants.UNIGENE_ID);
		if(_temp==null) {
			_temp="NA";
			return _temp;
		}else
			return _temp;

	//	return (String)annotHash.get(AnnotationFieldConstants.UNIGENE_ID);
	}

	

	
	public String[] getGoTerms() {
		String[] _temp=(String[])annotHash.get(AnnotationFieldConstants.GO_TERMS);
		if(_temp==null) {
			_temp=new String[1];
			_temp[0]="NA";
			return _temp;
		}else
			return _temp;

	//	return(String[]) annotHash.get(AnnotationFieldConstants.GO_TERMS);
	}

	
	
	/* Setters  */
	public void setChipName(String _temp) {	
		if (ChipName.size() < this.getMavInstance()-1)
			ChipName.setSize(ChipName.size() + 5);
		ChipName.setElementAt(_temp, this.getMavInstance());
	}

	public void setChipType(String _temp) {
		if (ChipType.size() < this.getMavInstance()-1)
			ChipType.setSize(ChipType.size() + 5);
		ChipType.setElementAt(_temp, this.getMavInstance());
	}
	
	public void setSpeciesName(String _temp) {
		if (speciesName.size() < this.getMavInstance()-1)
			speciesName.setSize(speciesName.size() + 5);
		speciesName.setElementAt(_temp, this.getMavInstance());
	}
	
	public void setGenomeBuild(String _temp) {
		if (genomeBuild.size() < this.getMavInstance()-1)
			genomeBuild.setSize(genomeBuild.size() + 5);
		genomeBuild.setElementAt(_temp, this.getMavInstance());
	}
	
	public void setCloneID(String _temp) {
		//String[] _tmp = {_temp};
		String _tmp = _temp;
		annotHash.put(AnnotationFieldConstants.CLONE_ID, _tmp);
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
	
	/**
	 * Static Functions to extract Genome Information
	 * E.g. Genes, RefSeq etc for a given co-ordinate range 
	 */
	public static Vector<String[]> getGenomeAnnotation(String[] colNames, int chr, int st_bp, int end_bp) throws SQLException {
		Vector<String[]> annoVec = new Vector<String[]>();
		
		if(validateColNames(colNames)) {
			//String db = getDBNameBySpecies(getSpeciesName2(mevInstance));
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
	 */
	public static String[] getFieldNames() {
		Vector<String> names = new Vector<String>();
		Class c = fieldConsts.getClass();
		Field[] fields = c.getFields();
		
		for(int j = 0; j < fields.length; j++) {
			names.add(fields[j].getName());
			//System.out.println("Field: " + j + " " + fields[j].getName());
		}
	
		return names.toArray(new String[0]);
	}

	/**
	 * 
	 */
	public String[] getAttribute(String attr) {
		
	//	if(annotHash.size()>1) {
		if((annotHash.get(attr)) instanceof String) {
			//System.out.println("getAttribute():"+annotHash.get(attr).getClass().getName());
			String[]_temp=new String[1];
			String temp;
			temp=(String)annotHash.get(attr);
			_temp[0]=temp;
			
			if(temp==null) {
				_temp[0]="na";
			}
			return _temp;


		}else {

			String[]_temp;
			_temp = (String[])annotHash.get(attr);
			if (_temp == null) {
				_temp = new String[1];
				_temp[0] = "na";
				//	System.out.println("getAttribute():MevAnnotation "+_temp[0] );

			}
			return _temp;
		}
		
		/*}else {
			String[]_temp=new String[1];
			_temp[0]="na";
			return _temp;
		}*/
			
		
	}
	
	/**
	 * 
	 */
	public AnnoAttributeObj getAttributeObj(String attr) {
		String[] _temp;
		AnnoAttributeObj _tempAttr;
		_temp = (String[])annotHash.get(attr);
		if (_temp == null) {
			_temp = new String[1];
			_temp[0] = "na";
			_tempAttr = new AnnoAttributeObj(attr, _temp);
			return _tempAttr;
		}
		_tempAttr = new AnnoAttributeObj(attr, _temp);
		return _tempAttr;
	}

	/**
	 * 
	 * @param mav
	 */
	public void setViewer(MultipleArrayViewer mav) {
		//System.out.println("MevAnno setViewer()");
		this.viewer = mav;	
	}
	
	/**
	 * 
	 * @return
	 */
	private int getMavInstance(){
		if (this.viewer == null) System.out.println("Null MAV");
		return this.viewer.getInstanceIndex();
	}

	
/*	public static boolean getAnnotationStatus() {
		return MultipleArrayData.isAnnotationLoaded;
	}*/


	


	
	
	/*****************************************************
	 * Implementation of Annotation URL class functions
	 *****************************************************/

}
