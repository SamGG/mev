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
	private MAGETABParser mageTabParser1=new MAGETABParser();
	private MAGETABParser mageTabParser2=new MAGETABParser();
    private MAGETABInvestigation affyInvestigation;
    private MAGETABInvestigation twocolorInvestigation;
	
   
	public  void testSetupParse(){
    	URL affyFileURL;
    	URL twocolorFileURL;
    	
		try{
		affyFileURL=new URL("http://mev-tm4.svn.sourceforge.net/viewvc/mev-tm4/trunk/data/magetab/E-ATMX-12.idf.txt");
		if(affyFileURL.getFile().length()==0){
			fail("The file does not exist OR you provided the wrong path");
		}
		
		
		affyInvestigation=mageTabParser1.parse(affyFileURL);
		 
		if(affyInvestigation==null){
			fail("File could not be parsed");
		}else
			this.affyInvestigation=affyInvestigation;
		
		
		
		}catch(ParseException pe1){ 
			fail(pe1.getMessage());
		}catch(Exception e1){
		 fail(e1.getMessage());	
		}
		
	
		//Testing two color array file
		try{
		twocolorFileURL=new URL("file:///"+"C://mage-files//E-JCVI-1.idf.txt");
		if(twocolorFileURL.getFile().length()==0){
			fail("The file does not exist OR you provided the wrong path");
		}
		
		
		twocolorInvestigation=mageTabParser2.parse(twocolorFileURL);
		 
		if(twocolorInvestigation==null){
			fail("File could not be parsed");
		}else
			this.twocolorInvestigation=twocolorInvestigation;
		
		
		
		}catch(ParseException pe2){ 
			fail(pe2.getMessage());
		}catch(Exception e2){
		 fail(e2.getMessage());	
		}
		
		
		
		

	}
	
 
	public void testCheckIDFObject(){
    	URL affyFileURL;
    	URL twocolorFileURL;
    	
    	try{
    		affyFileURL=new URL("http://mev-tm4.svn.sourceforge.net/viewvc/mev-tm4/trunk/data/magetab/E-ATMX-12.idf.txt");
    		if(affyFileURL.getFile().length()==0){
    			fail("The file does not exist OR you provided the wrong path");
    		}
    		
    		
    		affyInvestigation=mageTabParser1.parse(affyFileURL);
    		 
    		if(affyInvestigation==null){
    			fail("File could not be parsed");
    		}
    	}catch(ParseException pe){
			fail(pe.getMessage());
		}catch(Exception e){
		 fail(e.getMessage());	
		}
    	IDF idfObject1=affyInvestigation.IDF;
    	assertTrue(idfObject1.investigationTitle.equalsIgnoreCase("K.Yamada-Arabidopsis-Heat shock No.1"));
    	assertTrue((idfObject1.experimentalDesign.get(0)).equalsIgnoreCase("physiological_process_design"));
    	assertTrue(idfObject1.experimentalFactorName.get(0).equalsIgnoreCase("temperature"));
    	assertTrue(idfObject1.experimentalFactorType.get(1).equalsIgnoreCase("compound"));
    	assertTrue(idfObject1.personLastName.get(0).equalsIgnoreCase("Yamada"));
    	assertTrue(idfObject1.personEmail.get(0).equalsIgnoreCase("kyamada@nibb.ac.jp"));
    	assertTrue(idfObject1.personFirstName.get(0).equalsIgnoreCase("Kenji"));
    	assertTrue(idfObject1.personAffiliation.get(0).equalsIgnoreCase("Cell Biology, National Institute for Basic Biology"));
    	assertTrue(idfObject1.protocolName.get(4).equalsIgnoreCase("P-CAGE-25365"));
    	assertTrue(idfObject1.sdrfFile.get(0).equalsIgnoreCase("E-ATMX-12.sdrf.txt"));
    	assertTrue(idfObject1.personRoles.get(0).equalsIgnoreCase("submitter"));
    //	assertTrue(idfObject.protocolDescription.get(0).equalsIgnoreCase("Each well of 96-well microtiter plates received 10 surface-sterilized seeds in 150 µl of liquid medium "));
    	assertTrue((idfObject1.protocolParameters.get(0)).equalsIgnoreCase("light hours;day humidity;night humidity;light intensity;light source;media;night temperature;day temperature"));
    	
    	assertTrue(idfObject1.experimentDescription.equalsIgnoreCase("Effect of heat shock or HSP90 inhibitors (geldanamycin and radicicol) on wild type Arabidopsis thaliana."));
    	assertTrue(idfObject1.termSourceName.get(0).equalsIgnoreCase("Arabidopsis Biological Resource Center"));
    	assertTrue(idfObject1.termSourceName.get(2).equalsIgnoreCase("ArrayExpress"));
	
	
    	//Two color array testing
    	
    	
    	try{
    		twocolorFileURL=new URL("file:///"+"C://mage-files//E-JCVI-1.idf.txt");
    		if(twocolorFileURL.getFile().length()==0){
    			fail("The file does not exist OR you provided the wrong path");
    		}
    		
    		
    		twocolorInvestigation=mageTabParser2.parse(twocolorFileURL);
    		 
    		if(twocolorInvestigation==null){
    			fail("File could not be parsed");
    		}
    	}catch(ParseException pe){
			fail(pe.getMessage());
		}catch(Exception e){
		 fail(e.getMessage());	
		}
    	IDF idfObject2=twocolorInvestigation.IDF;
    	assertTrue(idfObject2.investigationTitle.equalsIgnoreCase("Transcription profiling of Geobacter sulfurreducens wild type and pilR mutants with acetate as the electron donor and ferric citrate as the electron acceptor"));
    	assertTrue((idfObject2.experimentalDesign.get(0)).equalsIgnoreCase("dye_swap_design"));
    	assertTrue(idfObject2.experimentalFactorName.get(0).equalsIgnoreCase("ExperimentalFactor:259_wildtype"));
    	assertTrue(idfObject2.experimentalFactorType.get(1).equalsIgnoreCase("PilR::Km  DLJK3"));
    	assertTrue(idfObject2.personLastName.get(0).equalsIgnoreCase("Methe"));
    	assertTrue(idfObject2.personEmail.get(0).equalsIgnoreCase("bmethe@jcvi.org"));
    	assertTrue(idfObject2.personFirstName.get(0).equalsIgnoreCase("Barbara"));
    	assertTrue(idfObject2.personAffiliation.get(0).equalsIgnoreCase("The J. Craig Venter Institute"));
    	assertTrue(idfObject2.protocolName.get(2).equalsIgnoreCase("jcvi.org:mad:Protocol_GGS Standard Protocol"));
    	assertTrue(idfObject2.sdrfFile.get(0).equalsIgnoreCase("E-JCVI-1.sdrf.txt"));
    	assertTrue(idfObject2.personRoles.get(0).equalsIgnoreCase("submitter"));
    //	assertTrue(idfObject.protocolDescription.get(0).equalsIgnoreCase("Each well of 96-well microtiter plates received 10 surface-sterilized seeds in 150 µl of liquid medium "));
    //	assertTrue((idfObject2.protocolParameters.get(0)).equalsIgnoreCase(" "));
    	
    	assertTrue(idfObject2.experimentDescription.equalsIgnoreCase("G. sulfurreducens wild type and pilR mutant (GSU1495) were grown in chemostats for RNA extraction used for microarray analysis and qRT-PCR. The electron donor (acetate 5mM) was limiting at a dilution rate of 0.05 h-1 and ferric citrate(55mM) was used as the electron acceptor at 30C, as previously described (Esteve-Nunez et al., 2005). Analysis of acetate, Fe(II) and protein were performed as previously described (Esteve-Nunez et al., 2005). Disruption of the pilR gene (GSU1495) was made in G. sulfurreducens strain DL1 (ATCC 51573) by the recombinant PCR and single-step recombination method (Murphy et al., 2000), essentially as described (Lloyd et al., 2003). To disrupt the pilR gene a 2.25 kb DNA fragment was constructed by PCR in which 0.39 kb of the pilR coding sequence (codons 192 - 323) were replaced with the kanamycin resistance cassette (Knr) of pBBR1MCS-2 (Kovach et al., 1995). This fragment consisted of 29 bp of upstream sequence together with the first 575 bp of the pilR gene, followed by the kanr cassette (1.1 kb), and the last 409 bp of the pilR gene and 132 bp of downstream sequence. The mutant was selected in NBAF plates supplemented with kanamycin and incubated at 30C in an anaerobic chamber containing a mixture of 7% H2, 10% CO2, 83% N2. A single kanamycin-resistant colony was selected, tested for the insertion of the Knr cassette by PCR, and designated DLJK3."));
    	assertTrue(idfObject2.termSourceName.get(0).equalsIgnoreCase("The MGED Ontology"));
    	assertTrue(idfObject2.termSourceName.get(2).equalsIgnoreCase("mo"));
	
    	
    	
    	
    	
    	
    	
    	
	
	
    	}	
    
    
    @Test
    public void testCheckSDRF(){
    	URL fileURL;
    	URL twocolorFileURL;

    	try {
    		fileURL = new URL("file:///"+"C:/MeV4.4/data/magetab/E-ATMX-12.idf.txt");
    		if (fileURL.getFile().length()==0) {
    			fail("The file does not exist OR you provided the wrong path");
    		}

    		affyInvestigation = mageTabParser1.parse(fileURL);

    		if (affyInvestigation == null) {
    			fail("File could not be parsed");
    		}
    	} catch (ParseException pe) {
    		fail(pe.getMessage());
    	} catch (Exception e) {
    		fail(e.getMessage());
    	}

    	ArrayList sourcenodes=(ArrayList)affyInvestigation.SDRF.sourceNodes;

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
    		
    	
    	List nodes=(affyInvestigation.SDRF).lookupNodes(HybridizationNode.class);
		
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
    	
    	
        	
   
    
    	//Two color array  testing		
		try{
			twocolorFileURL=new URL("file:///"+"C://mage-files//E-JCVI-1.idf.txt");
			if(twocolorFileURL.getFile().length()==0){
				fail("The file does not exist OR you provided the wrong path");
			}


			twocolorInvestigation=mageTabParser2.parse(twocolorFileURL);

			if(twocolorInvestigation==null){
				fail("File could not be parsed");
			}
		}catch(ParseException pe){
			fail(pe.getMessage());
		}catch(Exception e){
			fail(e.getMessage());	
		}


		sourcenodes=(ArrayList)twocolorInvestigation.SDRF.sourceNodes;

    	for(int i=0; i<sourcenodes.size(); i++){

    		List <CharacteristicsAttribute> characteristic=((SourceNode)sourcenodes.get(i)).characteristics;

    		if(i==0){
    			assertTrue(characteristic.get(0).getNodeName().equalsIgnoreCase("Geobacter sulfurreducens"));
    		}else if(i==1){
    			assertTrue(characteristic.get(0).getNodeName().equalsIgnoreCase("Geobacter sulfurreducens"));
    		}else if( i==2){
    			assertTrue(characteristic.get(0).getNodeName().equalsIgnoreCase("Geobacter sulfurreducens"));
    		}


    		if(i==0){
    			assertTrue(characteristic.get(1).getNodeName().equalsIgnoreCase("PilR::Km  DLJK3"));
    			
    		}else if(i==1){
    			assertTrue(characteristic.get(1).getNodeName().equalsIgnoreCase("DL1 (ATCC 51573)"));
    		}else if( i==2){
    			assertTrue(characteristic.get(1).getNodeName().equalsIgnoreCase("PilR::Km  DLJK3"));
    		}

    		if(i==0){
    			assertTrue(characteristic.get(2).getNodeName().equalsIgnoreCase("Bacterial cells"));
    		}else if(i==1){
    			assertTrue(characteristic.get(2).getNodeName().equalsIgnoreCase("Bacterial cells"));
    		}else if( i==2){
    			assertTrue(characteristic.get(2).getNodeName().equalsIgnoreCase("Bacterial cells"));
    		}

    		


	}
	
    	 nodes=(twocolorInvestigation.SDRF).lookupNodes(HybridizationNode.class);
		
		for(int index=0; index<nodes.size(); index++){
			
		    HybridizationNode node = (HybridizationNode)nodes.get(index);
		    List<FactorValueAttribute> fvalist = node.factorValues;
		   		    
		    if(index==0){
				assertTrue(fvalist.get(0).getNodeName().equalsIgnoreCase("wild_type"));
			}else if(index==0){
				assertTrue(fvalist.get(1).getNodeName().equalsIgnoreCase("PilR::Km  DLJK3"));
			}
		    
		     
		    if(index==1){
				assertTrue(fvalist.get(0).getNodeName().equalsIgnoreCase("wild_type"));
			}else if(index==1){
				assertTrue(fvalist.get(1).getNodeName().equalsIgnoreCase("PilR::Km  DLJK3"));
			}
		    
		    if(index==2){
				assertTrue(fvalist.get(0).getNodeName().equalsIgnoreCase("wild_type"));
			}else if(index==2){
				assertTrue(fvalist.get(1).getNodeName().equalsIgnoreCase("PilR::Km  DLJK3"));
			}
		    
		    
		    if(index==3){
				assertTrue(fvalist.get(0).getNodeName().equalsIgnoreCase("wild_type"));
			}else if(index==3){
				assertTrue(fvalist.get(1).getNodeName().equalsIgnoreCase("PilR::Km  DLJK3"));
			}
		    
		    
		    
		 }
    	
    }
}
