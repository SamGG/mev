package uk.ac.ebi.arrayexpress2.magetab.parser;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import uk.ac.ebi.arrayexpress2.magetab.datamodel.MAGETABInvestigation;
import uk.ac.ebi.arrayexpress2.magetab.exception.ParseException;
import uk.ac.ebi.arrayexpress2.magetab.datamodel.*;
import uk.ac.ebi.arrayexpress2.magetab.datamodel.sdrf.node.HybridizationNode;
import uk.ac.ebi.arrayexpress2.magetab.datamodel.sdrf.node.SourceNode;
import uk.ac.ebi.arrayexpress2.magetab.datamodel.sdrf.node.attribute.CharacteristicsAttribute;
import uk.ac.ebi.arrayexpress2.magetab.datamodel.sdrf.node.attribute.FactorValueAttribute;
import static org.junit.Assert.*;

import org.junit.*;

public class TestMAGETABParser  {

	/**
	 * @param args
	 */
	private MAGETABParser mageTabParser=new MAGETABParser();
    private MAGETABInvestigation investigation;
	
    @Test
	public  void testSetupParse(){
    	URL fileURL;
		try{
		fileURL=new URL("http://mev-tm4.svn.sourceforge.net/viewvc/mev-tm4/trunk/data/magetab/E-ATMX-12.idf.txt");
		if(fileURL.getFile().isEmpty()){
			fail("The file does not exist OR you provided the wrong path");
		}
		
		
		investigation=mageTabParser.parse(fileURL);
		 
		if(investigation==null){
			fail("File could not be parsed");
		}else
			this.investigation=investigation;
		
		
		
		}catch(ParseException pe){ 
			fail(pe.getMessage());
		}catch(Exception e){
		 fail(e.getMessage());	
		}

	}
	
    @Test
	public void testCheckIDFObject(){
    	URL fileURL;
    	try{
    		fileURL=new URL("http://mev-tm4.svn.sourceforge.net/viewvc/mev-tm4/trunk/data/magetab/E-ATMX-12.idf.txt");
    		if(fileURL.getFile().isEmpty()){
    			fail("The file does not exist OR you provided the wrong path");
    		}
    		
    		
    		investigation=mageTabParser.parse(fileURL);
    		 
    		if(investigation==null){
    			fail("File could not be parsed");
    		}
    	}catch(ParseException pe){
			fail(pe.getMessage());
		}catch(Exception e){
		 fail(e.getMessage());	
		}
    	IDF idfObject=investigation.IDF;
    	assertTrue(idfObject.investigationTitle.equalsIgnoreCase("K.Yamada-Arabidopsis-Heat shock No.1"));
    	assertTrue((idfObject.experimentalDesign.get(0)).equalsIgnoreCase("physiological_process_design"));
    	assertTrue(idfObject.experimentalFactorName.get(0).equalsIgnoreCase("temperature"));
    	assertTrue(idfObject.experimentalFactorType.get(1).equalsIgnoreCase("compound"));
    	assertTrue(idfObject.personLastName.get(0).equalsIgnoreCase("Yamada"));
    	assertTrue(idfObject.personEmail.get(0).equalsIgnoreCase("kyamada@nibb.ac.jp"));
    	assertTrue(idfObject.personFirstName.get(0).equalsIgnoreCase("Kenji"));
    	assertTrue(idfObject.personAffiliation.get(0).equalsIgnoreCase("Cell Biology, National Institute for Basic Biology"));
    	assertTrue(idfObject.protocolName.get(4).equalsIgnoreCase("P-CAGE-25365"));
    	assertTrue(idfObject.sdrfFile.get(0).equalsIgnoreCase("E-ATMX-12.sdrf.txt"));
    	assertTrue(idfObject.personRoles.get(0).equalsIgnoreCase("submitter"));
    //	assertTrue(idfObject.protocolDescription.get(0).equalsIgnoreCase("Each well of 96-well microtiter plates received 10 surface-sterilized seeds in 150 µl of liquid medium "));
    	assertTrue((idfObject.protocolParameters.get(0)).equalsIgnoreCase("light hours;day humidity;night humidity;light intensity;light source;media;night temperature;day temperature"));
    	
    	assertTrue(idfObject.experimentDescription.equalsIgnoreCase("Effect of heat shock or HSP90 inhibitors (geldanamycin and radicicol) on wild type Arabidopsis thaliana."));
    	assertTrue(idfObject.termSourceName.get(0).equalsIgnoreCase("Arabidopsis Biological Resource Center"));
    	assertTrue(idfObject.termSourceName.get(2).equalsIgnoreCase("ArrayExpress"));
	
	
	
	
    	}	
    
    
    @Test
    public void testCheckSDRF(){
    	URL fileURL;
    	try {
    		fileURL = new URL("file:///"+"C:/MeV4.4/data/magetab/E-ATMX-12.idf.txt");
    		if (fileURL.getFile().isEmpty()) {
    			fail("The file does not exist OR you provided the wrong path");
    		}

    		investigation = mageTabParser.parse(fileURL);

    		if (investigation == null) {
    			fail("File could not be parsed");
    		}
    	} catch (ParseException pe) {
    		fail(pe.getMessage());
    	} catch (Exception e) {
    		fail(e.getMessage());
    	}
    	
    	ArrayList sourcenodes=(ArrayList)investigation.SDRF.sourceNodes;
    	
    	for(int i=0; i<sourcenodes.size(); i++){
    	
    		List <CharacteristicsAttribute> characteristics=((SourceNode)sourcenodes.get(i)).characteristics;
    		
    			if(i==0){
    				assertTrue(characteristics.get(0).getNodeName().equalsIgnoreCase("Arabidopsis thaliana"));
    			}else if(i==1){
    				assertTrue(characteristics.get(0).getNodeName().equalsIgnoreCase("Arabidopsis thaliana"));
    			}else if( i==2){
    				assertTrue(characteristics.get(0).getNodeName().equalsIgnoreCase("Arabidopsis thaliana"));
    			}
    			
    				
    			if(i==0){
    				assertTrue(characteristics.get(1).getNodeName().equalsIgnoreCase("Columbia-0"));
    			}else if(i==1){
    				assertTrue(characteristics.get(1).getNodeName().equalsIgnoreCase("Columbia-0"));
    			}else if( i==2){
    				assertTrue(characteristics.get(1).getNodeName().equalsIgnoreCase("Columbia-0"));
    			}
    			
    			if(i==0){
    				assertTrue(characteristics.get(2).getNodeName().equalsIgnoreCase("CS1092"));
    			}else if(i==1){
    				assertTrue(characteristics.get(2).getNodeName().equalsIgnoreCase("CS1092"));
    			}else if( i==2){
    				assertTrue(characteristics.get(2).getNodeName().equalsIgnoreCase("CS1092"));
    			}
    			
    			if(i==0){
    				assertTrue(characteristics.get(3).getNodeName().equalsIgnoreCase("1.0 Boyes key"));
    			}else if(i==1){
    				assertTrue(characteristics.get(3).getNodeName().equalsIgnoreCase("1.0 Boyes key"));
    			}else if( i==2){
    				assertTrue(characteristics.get(3).getNodeName().equalsIgnoreCase("1.0 Boyes key"));
    			}
    		
    		
    	}
    	
    	
    	List nodes=(investigation.SDRF).lookupNodes(HybridizationNode.class);
		
		for(int index=0; index<nodes.size(); index++){
			
		    HybridizationNode node = (HybridizationNode)nodes.get(index);
		    List<FactorValueAttribute> fvalist = node.factorValues;
		   		    
		    if(index==0){
				assertTrue(fvalist.get(0).getNodeName().equalsIgnoreCase("22"));
			}else if(index==0){
				assertTrue(fvalist.get(1).getNodeName().equalsIgnoreCase("2.8"));
			}else if( index==0){
				assertTrue(fvalist.get(2).getNodeName().equalsIgnoreCase("dimethyl sulfoxide"));
			}
		    
		     
		    if(index==1){
				assertTrue(fvalist.get(0).getNodeName().equalsIgnoreCase("22"));
			}else if(index==1){
				assertTrue(fvalist.get(1).getNodeName().equalsIgnoreCase("50"));
			}else if( index==1){
				assertTrue(fvalist.get(2).getNodeName().equalsIgnoreCase("geldanamycin"));
			}
		    
		    if(index==2){
				assertTrue(fvalist.get(0).getNodeName().equalsIgnoreCase("22"));
			}else if(index==2){
				assertTrue(fvalist.get(1).getNodeName().equalsIgnoreCase("n/a"));
			}else if( index==2){
				assertTrue(fvalist.get(2).getNodeName().equalsIgnoreCase("none"));
			}
		    
		    
		    if(index==3){
				assertTrue(fvalist.get(0).getNodeName().equalsIgnoreCase("22"));
			}else if(index==3){
				assertTrue(fvalist.get(1).getNodeName().equalsIgnoreCase("50"));
			}else if( index==3){
				assertTrue(fvalist.get(2).getNodeName().equalsIgnoreCase("radicicol"));
			}
		    
		    
		    
		 }
    	
    	
    	
    	
    	
    	
    	
    	
    	
    }
    
    
    
    
    
    
    

}
