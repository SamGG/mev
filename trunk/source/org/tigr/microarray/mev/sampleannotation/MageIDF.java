package org.tigr.microarray.mev.sampleannotation;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import org.tigr.microarray.mev.annotation.ChipAnnotationFieldConstants;
/**
 * IDF provides implementation for the methods defined in
 * IDFInterface
 * @author Sarita Nair
 *
 */
public class MageIDF implements IDFInterface{
	/**
	 * idfHash will contain key value pairs of the fields 
	 * present in an idf file
	 */
	private Hashtable<String, List<String>> idfHash;
	private ArrayList tabspaces=new ArrayList();
	
	
	
	public MageIDF(){
		idfHash=new Hashtable<String, List<String>>();
	}
	
	public MageIDF(Hashtable<String, List<String>> hash){
		idfHash=hash;
	}
	
	
	public List getComments() {
		return null;
	}

	
	public List<String> getDateofExperiment() {
		
//		String temp=(String)idfHash.get(IDFConstants.DATE_OF_EXPERIMENT);
//		ArrayList<String> _temp=new ArrayList();
//		_temp.add(temp);
//		return _temp;
		return idfHash.get(IDFConstants.DATE_OF_EXPERIMENT);
	}

	
	public List getExperimentalFactorName() {
		List temp=(List)idfHash.get(IDFConstants.EXPERIMENTAL_FACTOR_NAME);
		return temp;
	}


	public List<String> getExperimentDescription() {
//		String temp=(String)idfHash.get(IDFConstants.EXPERIMENT_DESCRIPTION);
//		ArrayList<String> _temp=new ArrayList();
//		_temp.add(temp);
		return idfHash.get(IDFConstants.EXPERIMENT_DESCRIPTION);
	}
		
	public List getExperimentalDesign() {
		List temp=(List)idfHash.get(IDFConstants.EXPERIMENTAL_DESIGN);
		return temp;
	}

	
	public List getExperimentalFactorTermSourceRef() {
		ArrayList<String> temp=(ArrayList<String>)idfHash.get(IDFConstants.EXPERIMENTAL_FACTOR_TERM_SOURCE_REF);
		return temp;
	}

	
	public List<String> getExperimentalFactorTypes() {
		List<String> temp=(List<String>)idfHash.get(IDFConstants.EXPERIMENTAL_FACTOR_TYPE);
		return temp;
	}

	
	public List<String> getInvestigationTitle() {
//		String temp=(String)idfHash.get(IDFConstants.INVESTIGATION_TITLE);
//		ArrayList<String> _temp=new ArrayList();
//		_temp.add(temp);
		return idfHash.get(IDFConstants.INVESTIGATION_TITLE) ;
	}

	
	public List getNormalizationTermSourceRef() {
		ArrayList temp=(ArrayList)idfHash.get(IDFConstants.NORMALIZATION_TERM_SOURCE_REF);
		return temp;
	}

	
	public List getNormalizationType() {
		List temp=(List)idfHash.get(IDFConstants.NORMALIZATION_TYPE);
		return temp;
	}

	
	public List getPersonAddress() {
		List temp=(List)idfHash.get(IDFConstants.PERSON_ADDRESS);
		return temp;
	}

	
	public List getPersonAffiliation() {
		List temp=(List)idfHash.get(IDFConstants.PERSON_AFFILIATION);
		return temp;
	}

	
	public List getPersonEmail() {
		ArrayList temp=(ArrayList)idfHash.get(IDFConstants.PERSON_EMAIL);
		return temp;
	}

	
	public List getPersonFax() {
		List temp=(List)idfHash.get(IDFConstants.PERSON_FAX);
		return temp;
	}

	
	public List getPersonFirstName() {
		List temp=(List)idfHash.get(IDFConstants.PERSON_FIRST_NAME);
		return temp;
	}

	
	public List getPersonLastName() {
		List temp=(List)idfHash.get(IDFConstants.PERSON_LAST_NAME);
		return temp;
	}

	
	public List getPersonMidInitials() {
		List temp=(List)idfHash.get(IDFConstants.PERSON_MID_INITIALS);
		return temp;
	}

	
	public List getPersonPhone() {
		List temp=(List)idfHash.get(IDFConstants.PERSON_PHONE);
		return temp;
	}

	
	public List getPersonRoles() {
		List temp=(List)idfHash.get(IDFConstants.PERSON_ROLES);
		return temp;
	}

	
	public List getPersonRolesTermSourceREF() {
		List temp=(List)idfHash.get(IDFConstants.PERSON_ROLES_TERM_SOURCE_REF);
		return temp;
	}

	
	public List getProtocolContact() {
		List temp=(List)idfHash.get(IDFConstants.PROTOCOL_CONTACT);
		return temp;
	}

	
	public List getProtocolDescription() {
		List temp=(List)idfHash.get(IDFConstants.PROTOCOL_DESCRIPTION);
		return temp;
	}

	
	public List getProtocolHardware() {
		List temp=(List)idfHash.get(IDFConstants.PROTOCOL_HARDWARE);
		return temp;
	}

	
	public List getProtocolName() {
		List temp=(List)idfHash.get(IDFConstants.PROTOCOL_NAME);
		return temp;
	}

	
	public List getProtocolParameters() {
		List temp=(List)idfHash.get(IDFConstants.PROTOCOL_PARAMETERS);
		return temp;
	}

	
	public List getProtocolSoftware() {
		List temp=(List)idfHash.get(IDFConstants.PROTOCOL_SOFTWARE);
		return temp;
	}

	
	public List getProtocolTermSourceRef() {
		List temp=(List)idfHash.get(IDFConstants.PROTOCOL_TERM_SOURCE_REF);
		return temp;
	}

	
	public List getProtocolType() {
	
		List temp=(List)idfHash.get(IDFConstants.PROTOCOL_TYPE);
//		System.out.println("mageidf"+temp.get(0));
	//	new Exception().printStackTrace();
		return temp;
	}

	
	public List getPubMedID() {
		List temp=(List)idfHash.get(IDFConstants.PUBMED_ID);
		return temp;
	}

	
	public List<String> getPublicReleaseDate() {
//		String temp=(String)idfHash.get(IDFConstants.PUBLIC_RELEASE_DATE);
//		ArrayList<String> _temp=new ArrayList();
//		_temp.add(temp);
		return idfHash.get(IDFConstants.PUBLIC_RELEASE_DATE);
	}

	
	public List getPublicationAuthorList() {
		List temp=(List)idfHash.get(IDFConstants.PUBLICATION_AUTHOR_LIST);
		return temp;
	}

	
	public List getPublicationDOI() {
		List temp=(List)idfHash.get(IDFConstants.PUBLICATION_DOI);
		return temp;
	}

	
	public List getPublicationStatus() {
		List temp=(List)idfHash.get(IDFConstants.PUBLICATION_STATUS);
		return temp;
	}

	
	public List getPublicationStatusTermSourceRef() {
		List temp=(List)idfHash.get(IDFConstants.PUBLICATION_STATUS_TERM_SOURCE_REF);
		return temp;
	}

	
	public List getPublicationTitle() {
		List temp=(List)idfHash.get(IDFConstants.PUBLICATION_TITLE);
		return temp;
	}

	
	public List getQualityControlTermSourceRef() {
		List temp=(List)idfHash.get(IDFConstants.QUALITY_CONTROL_TERM_SOURCE_REF);
		return temp;
	}

	
	public List<String> getQualityControlType() {
		List<String> temp=(List<String>)idfHash.get(IDFConstants.QUALITY_CONTROL_TYPE);
		return temp;
	}

	
	public List<String> getReplicateType() {
		List<String> temp=(List<String>)idfHash.get(IDFConstants.REPLICATE_TYPE);
		return temp;
	}

	
	public List<String> getReplicateTypeTermSourceRef() {
		List<String> temp=(List)idfHash.get(IDFConstants.REPLICATE_TYPE_TERM_SOURCE_REF);
		return temp;
	}

	
	public List<String> getSDRFFile() {
//		String temp=(String)idfHash.get(IDFConstants.SDRF_FILE);
//		ArrayList<String> _temp=new ArrayList<String>();
//		_temp.add(temp);
		return idfHash.get(IDFConstants.SDRF_FILE);
	}

	
	public List<String> getTermSourceFile() {
		List<String> temp=(List)idfHash.get(IDFConstants.TERM_SOURCE_FILE);
		return temp;
	}

	
	public List<String> getTermSourceName() {
		List<String> temp=(List<String>)idfHash.get(IDFConstants.TERM_SOURCE_NAME);
		return temp;
	}

	
	public List<String> getTermSourceVersion() {
		List<String> temp=(List)idfHash.get(IDFConstants.TERM_SOURCE_VERSION);
		return temp;
	}

	/**
	 * setters
	 * 
	 * 
	 */
	public void setComments(List<String> temp) {
		
		
	}

	
	public void setDateofExperiment(List<String> temp) {
		idfHash.put(IDFConstants.DATE_OF_EXPERIMENT, temp);
		
	}

	
	public void setExperimentalFactorName(List<String> temp) {
		idfHash.put(IDFConstants.EXPERIMENTAL_FACTOR_NAME, temp);
		
	}

	
		
	public void setExperimentalDesign(List<String> temp) {
		idfHash.put(IDFConstants.EXPERIMENTAL_DESIGN, temp);
		
	}
	
	
	public void setExperimentDescription(List<String> temp) {
		idfHash.put(IDFConstants.EXPERIMENT_DESCRIPTION, temp);
	}

	
	public void setExperimentalFactorTermSourceRef(List<String> temp) {
		idfHash.put(IDFConstants.EXPERIMENTAL_FACTOR_TERM_SOURCE_REF, temp);
		
	}

	
	public void setExperimentalFactorTypes(List<String> temp) {
		idfHash.put(IDFConstants.EXPERIMENTAL_FACTOR_TYPE, temp);
		
	}

	
	public void setInvestigationTitle(List<String> temp) {
		idfHash.put(IDFConstants.INVESTIGATION_TITLE, temp);
		
	}

	
	public void setNormalizationTermSourceRef(List<String> temp) {
		
		idfHash.put(IDFConstants.NORMALIZATION_TERM_SOURCE_REF, temp);
		
	}

	
	public void setNormalizationType(List<String> temp) {
		idfHash.put(IDFConstants.NORMALIZATION_TYPE, temp);
		
	}

	
	public void setPersonAddress(List<String> temp) {
		idfHash.put(IDFConstants.PERSON_ADDRESS, temp);
		
	}

	
	public void setPersonAffiliation(List<String> temp) {
		idfHash.put(IDFConstants.PERSON_AFFILIATION, temp);
		
	}

	
	public void setPersonEmail(List<String> temp) {
		idfHash.put(IDFConstants.PERSON_EMAIL, temp);
		
	}

	
	public void setPersonFax(List<String> temp) {
		idfHash.put(IDFConstants.PERSON_FAX, temp);
		
	}

	
	public void setPersonFirstName(List<String> temp) {
		idfHash.put(IDFConstants.PERSON_FIRST_NAME, temp);
		
		
	}

	
	public void setPersonLastName(List<String> temp) {
		idfHash.put(IDFConstants.PERSON_LAST_NAME, temp);
		
	}

	
	public void setPersonMidInitials(List<String> temp) {
		idfHash.put(IDFConstants.PERSON_MID_INITIALS, temp);
		
	}

	
	public void setPersonPhone(List<String> temp) {
		idfHash.put(IDFConstants.PERSON_PHONE, temp);
		
	}

	
	public void setPersonRoles(List<String> temp) {
		idfHash.put(IDFConstants.PERSON_ROLES, temp);
		
	}

	
	public void setPersonRolesTermSourceREF(List<String> temp) {
		idfHash.put(IDFConstants.PERSON_ROLES_TERM_SOURCE_REF, temp);
		
	}

	
	public void setProtocolContact(List<String> temp) {
		idfHash.put(IDFConstants.PROTOCOL_CONTACT, temp);
		
	}

	
	public void setProtocolDescription(List<String> temp) {
		idfHash.put(IDFConstants.PROTOCOL_DESCRIPTION, temp);
		
	}

	
	public void setProtocolHardware(List<String> temp) {
		idfHash.put(IDFConstants.PROTOCOL_HARDWARE, temp);
	}

	
	public void setProtocolName(List<String> temp) {
		idfHash.put(IDFConstants.PROTOCOL_NAME, temp);
		
	}

	
	public void setProtocolParameters(List<String> temp) {
		idfHash.put(IDFConstants.PROTOCOL_PARAMETERS, temp);
		
	}

	
	public void setProtocolSoftware(List<String> temp) {
		idfHash.put(IDFConstants.PROTOCOL_SOFTWARE, temp);
		
	}

	
	public void setProtocolTermSourceRef(List<String> temp) {
		idfHash.put(IDFConstants.PROTOCOL_TERM_SOURCE_REF, temp);
		
	}

	
	public void setProtocolType(List<String> temp) {
		idfHash.put(IDFConstants.PROTOCOL_TYPE, temp);
		//System.out.println("mageid setter:"+temp.get(0));
		//new Exception().printStackTrace();
		
	}

	
	public void setPubMedID(List<String> temp) {
		idfHash.put(IDFConstants.PUBMED_ID, temp);
		
	}

	
	public void setPublicReleaseDate(List<String> date) {
		idfHash.put(IDFConstants.PUBLIC_RELEASE_DATE, date);
		
	}

	
	public void setPublicationAuthorList(List<String> temp) {
		idfHash.put(IDFConstants.PUBLICATION_AUTHOR_LIST, temp);
		
	}

	
	public void setPublicationDOI(List<String> temp) {
		idfHash.put(IDFConstants.PUBLICATION_DOI, temp);
		
	}

	
	public void setPublicationStatus(List<String> temp) {
		idfHash.put(IDFConstants.PUBLICATION_STATUS, temp);
		
	}

	
	public void setPublicationStatusTermSourceRef(List<String> temp) {
		idfHash.put(IDFConstants.PUBLICATION_STATUS_TERM_SOURCE_REF, temp);
		
	}

	
	public void setPublicationTitle(List<String> temp) {
		idfHash.put(IDFConstants.PUBLICATION_TITLE, temp);
		
	}

	
	public void setQualityControlTermSourceRef(List<String> temp) {
		idfHash.put(IDFConstants.QUALITY_CONTROL_TERM_SOURCE_REF, temp);
		
	}

	
	public void setQualityControlType(List<String> temp) {
		idfHash.put(IDFConstants.QUALITY_CONTROL_TYPE, temp);
		
	}

	
	public void setReplicateType(List<String> temp) {
		idfHash.put(IDFConstants.REPLICATE_TYPE, temp);
	}

	
	public void setReplicateTypeTermSourceRef(List temp) {
		 idfHash.put(IDFConstants.REPLICATE_TYPE_TERM_SOURCE_REF, temp);
		
	}

	
	public void setSDRFFile(List<String> temp) {
		idfHash.put(IDFConstants.SDRF_FILE, temp); 
		
	}

	
	public void setTermSourceFile(List<String> temp) {
		idfHash.put(IDFConstants.TERM_SOURCE_FILE, temp);
		
	}

	
	public void setTermSourceName(List<String> temp) {
		idfHash.put(IDFConstants.TERM_SOURCE_NAME, temp); 
		
	}

	
	public void setTermSourceVersion(List<String> temp) {
		 idfHash.put(IDFConstants.TERM_SOURCE_VERSION, temp);
		
	}
	
	
	public Hashtable getIDFHash(){
		return this.idfHash;
	}
	
	
	public void setIDFHash(Hashtable temp){
		this.idfHash=temp;
	}
	
	public List<String> getAttribute(String attr) {
	
				ArrayList<String> _temp=new ArrayList<String>();
				_temp = (ArrayList)getIDFHash().get(attr);
				
				
				if (_temp == null) {
					_temp=new ArrayList();
					_temp.add(ChipAnnotationFieldConstants.NOT_AVAILABLE);
					
				}
		
				return _temp;
	
			
		}

	

	

	
	

}