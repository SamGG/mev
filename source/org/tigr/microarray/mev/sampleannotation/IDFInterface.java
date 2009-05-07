package org.tigr.microarray.mev.sampleannotation;

import java.util.List;

/**
 * Getters and setters for all possible fields in an IDF file
 * @author sarita nair
 *
 */
public interface IDFInterface {
	
	public IDFConstants idfConstants=new IDFConstants();
 /**
  * getters for IDF file
  * 
  */
	public List<String> getInvestigationTitle();
	public List<String> getExperimentalDesign();
	public List<String> getExperimentalFactorName();
	public List<String> getExperimentalFactorTypes();
	public List<String> getExperimentalFactorTermSourceRef();
	public List<String> getPersonLastName();
	public List<String> getPersonFirstName();
	public List<String> getPersonMidInitials();
	public List<String> getPersonEmail();
	public List<String> getPersonPhone();
	public List<String> getPersonFax();
	public List<String> getPersonAddress();
	public List<String> getPersonAffiliation();
	public List<String> getPersonRoles();
	public List<String> getPersonRolesTermSourceREF();
	public List<String> getQualityControlType();
	public List<String> getQualityControlTermSourceRef();
	public List<String> getReplicateType();
	public List<String> getReplicateTypeTermSourceRef();
	public List<String> getNormalizationType();
	public List<String> getNormalizationTermSourceRef();
	public List<String> getDateofExperiment();
	public List<String> getPublicReleaseDate();
	public List<String> getPubMedID();
	public List<String> getPublicationDOI();
	public List<String> getPublicationAuthorList();
	public List<String> getPublicationTitle();
	public List<String> getPublicationStatus();
	public List<String> getPublicationStatusTermSourceRef();
	public List<String> getExperimentDescription();
	public List<String> getProtocolName();
	public List<String> getProtocolType();
	public List<String> getProtocolDescription();
	public List<String> getProtocolParameters();
	public List<String> getProtocolHardware();
	public List<String> getProtocolSoftware();
	public List<String> getProtocolContact();
	public List<String> getProtocolTermSourceRef();
	public List<String> getSDRFFile();
	public List<String> getTermSourceName();
	public List<String> getTermSourceFile();
	public List<String> getTermSourceVersion();
	public List<String> getComments();
	
	/**
	 * Setters for IDF file
	 * 
	 * 
	 */
	
	public void setInvestigationTitle(List<String> temp);
	public void setExperimentalDesign(List<String> temp);
	
	public void setExperimentalFactorName(List<String> temp);
	public void setExperimentalFactorTypes(List<String> temp);
	public void setExperimentalFactorTermSourceRef(List<String> temp);
	public void setPersonLastName(List<String> temp);
	public void setPersonFirstName(List<String> temp);
	public void setPersonMidInitials(List<String> temp);
	public void setPersonEmail(List<String> temp);
	public void setPersonPhone(List<String> temp);
	public void setPersonFax(List<String> temp);
	public void setPersonAddress(List<String> temp);
	public void setPersonAffiliation(List<String> temp);
	public void setPersonRoles(List<String> temp);
	public void setPersonRolesTermSourceREF(List<String> temp);
	public void setQualityControlType(List<String> temp);
	public void setQualityControlTermSourceRef(List<String> temp);
	public void setReplicateType(List<String> temp);
	public void setReplicateTypeTermSourceRef(List<String> temp);
	public void setNormalizationType(List<String> temp);
	public void setNormalizationTermSourceRef(List<String> temp);
	public void setDateofExperiment(List<String> temp);
	public void setPublicReleaseDate(List<String> date);
	public void setPubMedID(List<String> temp);
	public void setPublicationDOI(List<String> temp);
	public void setPublicationAuthorList(List<String> temp);
	public void setPublicationTitle(List<String> temp);
	public void setPublicationStatus(List<String> temp);
	public void setPublicationStatusTermSourceRef(List<String> temp);
	public void setExperimentDescription(List<String> temp);
	public void setProtocolName(List<String> temp);
	public void setProtocolType(List<String> temp);
	public void setProtocolDescription(List<String> temp);
	public void setProtocolParameters(List<String> temp);
	public void setProtocolHardware(List<String> temp);
	public void setProtocolSoftware(List<String> temp);
	public void setProtocolContact(List<String> temp);
	public void setProtocolTermSourceRef(List<String> temp);
	public void setSDRFFile(List<String> temp);
	public void setTermSourceName(List<String> temp);
	public void setTermSourceFile(List<String> temp);
	public void setTermSourceVersion(List<String> temp);
	public void setComments(List<String> temp);
	
	
	
	
}
