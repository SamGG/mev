
/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: AffyGCOSFileLoader.java,v $
 * $Revision: 1.12 $
 * $Date: 2007-12-20 19:55:12 $
 * $Author: Joseph White, Sarita Nair $
 * $State: Exp $
 */

package org.tigr.microarray.mev.file;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.tigr.microarray.mev.AffySlideDataElement;
import org.tigr.microarray.mev.FloatSlideData;
import org.tigr.microarray.mev.ISlideData;
import org.tigr.microarray.mev.ISlideDataElement;
import org.tigr.microarray.mev.MultipleArrayViewer;
import org.tigr.microarray.mev.SlideData;
import org.tigr.microarray.mev.SlideDataElement;
import org.tigr.microarray.mev.TMEV;
import org.tigr.microarray.mev.annotation.AffymetrixAnnotationParser;
import org.tigr.microarray.mev.annotation.AnnotationFileReader;
import org.tigr.microarray.mev.annotation.MevAnnotation;
import org.tigr.microarray.mev.annotation.PublicURL;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.sampleannotation.MageIDF;
import org.tigr.microarray.mev.sampleannotation.SampleAnnotation;
import org.tigr.microarray.util.ExpressionFileTableCellRenderer;

import uk.ac.ebi.arrayexpress2.magetab.datamodel.IDF;
import uk.ac.ebi.arrayexpress2.magetab.datamodel.MAGETABInvestigation;
import uk.ac.ebi.arrayexpress2.magetab.datamodel.SDRF;
import uk.ac.ebi.arrayexpress2.magetab.datamodel.sdrf.node.HybridizationNode;
import uk.ac.ebi.arrayexpress2.magetab.datamodel.sdrf.node.SDRFNode;
import uk.ac.ebi.arrayexpress2.magetab.datamodel.sdrf.node.SourceNode;
import uk.ac.ebi.arrayexpress2.magetab.datamodel.sdrf.node.attribute.CharacteristicsAttribute;
import uk.ac.ebi.arrayexpress2.magetab.datamodel.sdrf.node.attribute.FactorValueAttribute;
import uk.ac.ebi.arrayexpress2.magetab.exception.ParseException;
import uk.ac.ebi.arrayexpress2.magetab.handler.sdrf.node.attribute.CharacteristicsHandler;
import uk.ac.ebi.arrayexpress2.magetab.parser.MAGETABParser;


//import junit.framework.*;

public class MAGETABFileLoader extends ExpressionFileLoader {
    
    private GBA gba;
    private boolean stop = false;
    private MAGETABFileLoaderPanel sflp;
    ExpressionFileTableCellRenderer myCellRenderer;
    private int DataType;
    protected String[] dataTypes;
    protected Hashtable<String, String> columnDataTypes = new Hashtable<String,String>();
    private String SDRFFilePath;
    private String IDFFilePath;
//    protected Vector<String> quantHeaders;
    protected Hashtable<String, Integer> quantTypes;
    //sampleNames holds the list of experiment names in order
    ArrayList sampleNames = new ArrayList();
    public enum matrixType {
    	NONE, INTENSITY, DETECTION, PVALUE, LOG_RATIO, RATIO, CHANNEL1,
        CHANNEL2, INTENSITY_DETECTION, INTENSITY_DETECT_PVAL,
        LOG_CHANNELS, RATIO_CHANNELS, CHANNELS, INTENSITY_PVAL;
    }
    private matrixType matrixState;
    /**
     * getter/setter for matrixState
     * matrixState indicates the type of data in the array data matrix
     * This class var. replaces the radio button group Intensity etc.
     * 
     * @param state
     */
    protected void setMatrixState (matrixType state) {
    	this.matrixState = state;
    }
    protected matrixType getMatrixState () {
    	return this.matrixState;
    }
    
    public void setFilePath(String path) {
    	sflp.setDataFileName(path);
    	processAffyGCOSFile(new File(path));
    }


    /**
     * Raktim - Annotation Specific
     * Place Holder for reading in Affy Anno 
     * MAV needed to pass ont he the ref to MevAnnotation Obj for MAV Index
     **/
    private Hashtable _tempAnno=new Hashtable();

    private MultipleArrayViewer mav;
    protected MevAnnotation mevAnno=new MevAnnotation();
   private MAGETABParser mageTabParser=new MAGETABParser();
    private MAGETABInvestigation investigation;
    private MageIDF mageIDFObject=new MageIDF();
   
	
    
    public MAGETABFileLoader(SuperExpressionFileLoader superLoader) {
    	
    	super(superLoader);
    	this.mav = superLoader.getArrayViewer();
        gba = new GBA();
        sflp = new MAGETABFileLoaderPanel();
    }
    
    public Vector loadExpressionFiles() throws IOException {
    	 /**
         * TODO
         * Raktim - Annotation Addition. 
         * Code to load Affy Annotation File into a Indexed Object
         */
        
        
        /*Loop added by Sarita to check if Annotation has been loaded
         
         * The loop was included so as to enable loading data
         * irrespective of whether annotation was loaded or not
         * 
         */
    	if(isAnnotationSelected()) {
    		
    		this.mav.getData().setAnnotationLoaded(true);
			File annoFile=new File(getAnnotationFilePath());
			String extension=(annoFile.getName()).substring((annoFile.getName()).lastIndexOf('.')+1, annoFile.getName().length());
			
			if(annoFile.getName().endsWith("annot.csv")){
				//System.out.println("Ends with annot.csv");
				AffymetrixAnnotationParser aafp = AffymetrixAnnotationParser.createAnnotationFileParser(new File(getAnnotationFilePath()));
				_tempAnno = aafp.getAffyAnnotation();
				//chipAnno = aafp.getAffyChipAnnotation();
			}
				
			if(extension.equalsIgnoreCase("txt")){
			AnnotationFileReader afr = AnnotationFileReader.createAnnotationFileReader(new File(getAnnotationFilePath()));
			_tempAnno = afr.getAffyAnnotation();
			chipAnno = afr.getAffyChipAnnotation();
			}
    		        	
        }
        
     
        /**
         * TODO
         * Raktim - Annotation Demo Only. 
         * Good Place to initialize URLS.
         */
        if(PublicURL.loadURLs(TMEV.getConfigurationFile("annotation_URLs.txt")) != 0){
        	JOptionPane.showMessageDialog(new JFrame(), "URLs will not be loaded", "Warning", JOptionPane.WARNING_MESSAGE);
        }
       
        
        try {
        	//System.out.println("1: " + PublicURL.getURL(AnnotationURLConstants.NCBI_GENE, new String[] {"MYC"}));
        }catch(Exception e){
        	e.printStackTrace();
        }
        try {
        	//System.out.println("1: " + PublicURL.getURL(AnnotationURLConstants.NCBI_MAPVIEWER, new String[] {"9606", "16Abc", "12345", "223456"}));
        } catch(Exception e){
        	e.printStackTrace();
        }

        return loadAffyGCOSExpressionFile(new File(this.sflp.selectedDataMatrix.getText()));
    }
    
    public ISlideData loadExpressionFile(File f){
        return null;
    }

    public void setDataType(int dataType){
        if(dataType!= -1){
        	this.DataType=dataType;
        }else
        	this.DataType=IData.DATA_TYPE_RATIO_ONLY;
     }

    /**
     * switch block for matrixState branching
     * @param matrixState 

                switch (matrixState) {
            	case INTENSITY:
            		break;
            	case INTENSITY_DETECTION:
            		break;
            	case INTENSITY_DETECT_PVAL:
            		break;
            	case LOG_RATIO:
            		break;
            	case RATIO:
            		break;
            	case CHANNELS:
            		break;
            	case RATIO_CHANNELS:
            		break;
            	case CHANNEL1:
            		break;
            	case CHANNEL2:
            		break;
            	case DETECTION:
            		break;
            	case LOG_CHANNELS:
            		break;
            	case PVALUE:
            		break;
            	case NONE:
            		break;
            	default:
            		break;
            	}
     */
    
    
    /*
     *  Handling of Mas5 data has been altered in version 3.0 to permit loading of
     *  "ratio" input without the creation of false cy3 and cy5.  cy5 values in data structures
     *  are used to hold the input value.
     *
     *  getRatio methods are altered to return the value (held in cy5) rather than
     *  taking log2(cy5/cy3).
     */
    
    public Vector loadAffyGCOSExpressionFile(File f) throws IOException {
    	
        final int preSpotRows = this.sflp.getXRow()+1;
        final int preExperimentColumns = this.sflp.getXColumn();
        int numLines = this.getCountOfLines(f);
        int spotCount = numLines - preSpotRows;

        if (spotCount <= 0) {
            JOptionPane.showMessageDialog(superLoader.getFrame(), 
            "There is no spot data available.",  "TDMS Load Error", 
            JOptionPane.INFORMATION_MESSAGE);
        }
        
        if(sflp.oneChannelRadioButton.isSelected()) {
        	setDataType(TMEV.DATA_TYPE_AFFY);
        } else {
        	setDataType(TMEV.DATA_TYPE_TWO_DYE);
        }
                
        int[] rows = new int[] {0, 1, 0};
        int[] columns = new int[] {0, 1, 0};
        String detection;
        float cy3, cy5;

        String[] moreFields = new String[1];
        String[] extraFields=null;
        final int rColumns = 1;
        final int rRows = spotCount;
        
        ISlideData slideDataArray[]=null;
        ISlideDataElement sde=null;
        FloatSlideData slideData=null;
        
        BufferedReader reader = new BufferedReader(new FileReader(f));
        StringSplitter ss = new StringSplitter((char)0x09);
        String currentLine;
        int counter, row, column,experimentCount=0;
        counter = 0;
        row = column = 1;
        this.setFilesCount(1);
        this.setRemain(1);
        this.setFilesProgress(0);
        this.setLinesCount(numLines);
        this.setFileProgress(0);
        float[] intensities = new float[2];
        
        setListsAndButtons();

        while ((currentLine = reader.readLine()) != null) {
            if (stop) {
                return null;
            }
//          fix empty tabbs appending to the end of line by wwang
            while(currentLine.endsWith("\t")){
            	currentLine=currentLine.substring(0,currentLine.length()-1);
            }
            ss.init(currentLine);

            if (counter == 0) { // parse header

        		experimentCount = (ss.countTokens()+1- preExperimentColumns)/ dataTypes.length;
/*
System.out.println("expt cnt: " + experimentCount + " ; tokens: " 
	+ ss.countTokens() + " ; preExpt cols: " + preExperimentColumns
    + " ; data types: " + dataTypes.length);
*/
            	
            	slideDataArray = new ISlideData[experimentCount];
            	SampleAnnotation sampAnn=new SampleAnnotation();
            	slideDataArray[0] = new SlideData(rRows, rColumns, sampAnn);//Added by Sarita to include SampleAnnotation model.
            	
                slideDataArray[0].setSlideFileName(f.getPath());
                for (int i=1; i<experimentCount; i++) {
                	sampAnn=new SampleAnnotation();
                	slideDataArray[i] = new FloatSlideData(slideDataArray[0].getSlideMetaData(),spotCount, sampAnn);//Added by Sarita 
                	slideDataArray[i].setSlideFileName(f.getPath());
                	//System.out.println("slideDataArray[i].slide file name: "+ f.getPath());
                }
                String [] fieldNames;
            	switch (matrixState) {
            	case INTENSITY_PVAL:
            	case INTENSITY:
                	fieldNames = new String[1];
                	fieldNames[0]="AffyID";
                	slideDataArray[0].getSlideMetaData().appendFieldNames(fieldNames);
            		break;
            	case INTENSITY_DETECTION:
                	fieldNames = new String[2];
                	extraFields = new String[1];
                    fieldNames[0]="AffyID";
                    fieldNames[1]="Detection";
                    slideDataArray[0].getSlideMetaData().appendFieldNames(fieldNames);
            		break;
            	case INTENSITY_DETECT_PVAL:
                	fieldNames = new String[3];
                	extraFields = new String[2];
                    fieldNames[0]="AffyID";
                    fieldNames[1]="Detection";
                    fieldNames[2]="P-value";
                    slideDataArray[0].getSlideMetaData().appendFieldNames(fieldNames);
            		break;
            	case LOG_RATIO:
            	case RATIO:
                	fieldNames = new String[1];	//new String[dataTypes.length];
                    fieldNames[0]="ReporterID";
                    //extraFields = new String[1];
                    slideDataArray[0].getSlideMetaData().appendFieldNames(fieldNames);
            		break;
            	case LOG_CHANNELS:
            	case RATIO_CHANNELS:
                    if(dataTypes.length > 2) {
                    	fieldNames = new String[3];	//new String[dataTypes.length];
                    	extraFields = new String[2];
                    	fieldNames[0]="ReporterID";
                    	fieldNames[1]="Channel1";
                    	fieldNames[2]="Channel2";
                    } else {
                    	fieldNames = new String[1];	//new String[dataTypes.length];
                    	fieldNames[0]="ReporterID";
                    }
                    slideDataArray[0].getSlideMetaData().appendFieldNames(fieldNames);
            		break;
            	case CHANNELS:
                	fieldNames = new String[1];	//new String[dataTypes.length];
                    fieldNames[0]="ReporterID";
                    slideDataArray[0].getSlideMetaData().appendFieldNames(fieldNames);
            		break;
            	default:
            		break;
            	}
//System.out.println("matrixState" + matrixState.toString());
//	System.out.println("Sample size:"+sampleNames.size());
//	System.out.println("Experiment Count:"+experimentCount);
                ss.nextToken();//parse the blank on header
                int numqts = dataTypes.length;
                for (int i=0; i<experimentCount; i++) {
//                	String oldSlideDataName=ss.nextToken();
                	String slideDataName= (String) sampleNames.get(i);
                	slideDataArray[i].setSampleAnnotationLoaded(true);
					slideDataArray[i].getSampleAnnotation().setAnnotation("Default Slide Name", slideDataName);
                	slideDataArray[i].setSlideDataName(slideDataName);//commented by sarita
                }
                try{ Thread.sleep(10000); } catch (Exception e){}
                
            } else if (counter >= preSpotRows) { // data rows
            	
            	rows[0] = rows[2] = row;
            	columns[0] = columns[2] = column;
            	if (column == rColumns) {
            		column = 1;
            		row++;//commented by sarita
            	} else {
            		column++;//commented by sarita
            	}
            	//affy ID
            	moreFields[0] = ss.nextToken();
                String cloneName = moreFields[0];
//System.out.println("clone ID: " + cloneName);
				MevAnnotation mevAnno = new MevAnnotation();
                if(_tempAnno.size()!=0) {
                	if(((MevAnnotation)_tempAnno.get(cloneName))!=null) {
            		   mevAnno = (MevAnnotation)_tempAnno.get(cloneName);
                	}else {
            	 /**
            	  * Sarita: clone ID explicitly set here because if the data file
            	  * has a probe (for eg. Affy house keeping probes) for which Resourcerer
            	  * does not have annotation, MeV would still work fine. NA will be
            	  * appended for the rest of the fields. 
            	  */
//                		mevAnno = new MevAnnotation();
                		mevAnno.setCloneID(cloneName);
                		//jaw: in order to initialize sde after this block need to reset intensities here
                		intensities = new float[2];
                	}
                    //jaw: need to use either Affysde or sde; should be done after previous block . .
                    if(getDataType() == TMEV.DATA_TYPE_AFFY) {
    					sde = new AffySlideDataElement(String.valueOf(row + 1), rows, columns, intensities, moreFields, mevAnno);
    				} else {
    					sde = new SlideDataElement(String.valueOf(row + 1), rows, columns, intensities, moreFields, mevAnno);
    				}
               } else {
                /* Added by Sarita
                 * Checks if annotation was loaded and accordingly use
                 * the appropriate constructor.
                 */
                   //jaw: need to use either Affysde or sde; should be done after previous block . .
                   if(getDataType() == TMEV.DATA_TYPE_AFFY) {
                	   sde = new AffySlideDataElement(String.valueOf(row + 1), rows, columns, intensities, moreFields);
                   } else {
                	   sde = new SlideDataElement(String.valueOf(row + 1), rows, columns, intensities, moreFields);
                   }
                }

            	slideDataArray[0].addSlideDataElement(sde);
            	int i=0;
            	String[] currData;
/*
System.out.println(slideDataArray.length);
System.out.println("dT.len: " + dataTypes.length);
System.out.println("quantTypes: " + quantTypes.toString());
for(int k=0;k<dataTypes.length;k++) {
	System.out.println(k + " dataTypes: " + dataTypes[k]);	
}
*/
            	for ( i=0; i<slideDataArray.length; i++) {
            		try {	
/**this is where cy3 and cy5 should be set in SlideDataElement
 * copy code from Stnaford file loader if need be.
 * Must check that radio buttons are set to false.
 */
            			// Intensity
            			intensities[0] = 1.0f;
/**
 * 	Create an array with length equal the number of different column types
 * then read that many items from the current line (ss).  Based on the 
 * column positions and data types, collect only that information that is 
 * needed.  The remaining data will be discarded on the next iteration of 
 * the for loop for slideDataArray.  
 */
        				currData = new String[dataTypes.length];
        				for(int j=0; j<dataTypes.length; j++) {
        					if(currentLine.length() < dataTypes.length) {
        						//need to do something about that!
        					}
        					currData[j] = ss.nextToken(); 
        				}
/*
        				//retrieves a column from ordered array of column data types 
        				String ckey = dataTypes[j];
        				//retrieves an item from the current set of data columns
        				String value = currData[j];
        				//retrieves users' column name based on MEV tag (MEV:signal)
        				String key = (String) columnDataTypes.get("MEV:signal");
        				//retrieves the int value of the order of a column in users' columns
        				int ord = quantTypes.get(key);
        				//retrieves the current signal value
        				//NOTE: currData[ord - 1]; because columns are 1-based and currData is 0-based 
        				currData[quantTypes.get(columnDataTypes.get("MEV:signal")) -1];
*/
                    	switch (matrixState) {
                    	case INTENSITY_PVAL:
                    	case INTENSITY:
							intensities[1] = Float.parseFloat(currData[quantTypes.get((String) columnDataTypes.get("MEV:signal")) -1]);
                    		break;
                    	case INTENSITY_DETECTION:
							intensities[1] = Float.parseFloat(currData[quantTypes.get(columnDataTypes.get("MEV:signal")) -1]);
							extraFields[0] = currData[quantTypes.get(columnDataTypes.get("MEV:detection")) -1];
                    		break;
                    	case INTENSITY_DETECT_PVAL:
            				intensities[1] = Float.parseFloat(currData[quantTypes.get(columnDataTypes.get("MEV:signal")) -1]);
        					extraFields[0] = currData[quantTypes.get(columnDataTypes.get("MEV:detection")) -1];
            				extraFields[1] = currData[quantTypes.get(columnDataTypes.get("MEV:pvalue")) -1];
                    		break;
                    	case LOG_CHANNELS:
            				intensities[1] = Float.parseFloat(currData[quantTypes.get(columnDataTypes.get("MEV:log")) -1]);
            				extraFields[0] = currData[quantTypes.get(columnDataTypes.get("MEV:channel1")) -1];
            				extraFields[1] = currData[quantTypes.get(columnDataTypes.get("MEV:channel2")) -1];
            				break;
                    	case LOG_RATIO:
            			//	intensities[1] = Float.parseFloat(currData[quantTypes.get(columnDataTypes.get("MEV:log")) -1]);
            				intensities[1]= new Float((NumberFormat.getInstance()).parse(currData[quantTypes.get(columnDataTypes.get("MEV:log")) -1]).floatValue());
            				/**
            				 * Notes:
            				 * fieldNames = column header names
            				 * extraFields = the column values
            				 * There should be as many extraFields as fieldNames
            				 * SlideData includes as many SlideDataElement objects are there are genes
            				 * FloatSlideData includes a float array with as many elements as there genes
            				 * AffySlideDataElement should be used for affy data, and SlideDataElement for 2-color data
            				 * extraFields are stored in the SlideDataElement objects
            				 *             				 * 
            				 */
            				break;
                    	case RATIO_CHANNELS:
            				intensities[1] = Float.parseFloat(currData[quantTypes.get(columnDataTypes.get("MEV:ratio")) -1]);
            				extraFields[0] = currData[quantTypes.get(columnDataTypes.get("MEV:channel1")) -1];
            				extraFields[1] = currData[quantTypes.get(columnDataTypes.get("MEV:channel2")) -1];
                    		break;
                    	case RATIO:
            				intensities[1] = Float.parseFloat(currData[quantTypes.get(columnDataTypes.get("MEV:ratio")) -1]);
                    		break;
                    	case CHANNELS:
            				intensities[0] = Float.parseFloat(currData[quantTypes.get(columnDataTypes.get("MEV:channel1")) -1]);
            				intensities[1] = Float.parseFloat(currData[quantTypes.get(columnDataTypes.get("MEV:channel2")) -1]);
                    		break;
                    	default:
                    		break;
                    	}
            		} catch (ArrayIndexOutOfBoundsException iob) {
            			iob.printStackTrace();
            		} catch (NumberFormatException nfe) {
            			//do nothing
//            			System.out.println("Cell in row " + row + ", column " + column + " is NAN");
            		} catch (java.text.ParseException jtp) {
            			//do nothing
            		} catch (Exception e) {
            			System.out.println("General exeception: " + i);
            			e.printStackTrace();
            			intensities[1] = Float.NaN;
            		}
            		if(i==0){
            			slideDataArray[i].setIntensities(counter - preSpotRows, intensities[0], intensities[1]);
            			//sde.setExtraFields(extraFields);
                    	switch (matrixState) {
                    	case INTENSITY_PVAL:
                    		break;
                    	case INTENSITY_DETECT_PVAL:
            				sde.setDetection(extraFields[0]);
            				try{
            				sde.setPvalue(new Float((NumberFormat.getInstance()).parse(extraFields[1]).floatValue()));
            				}catch(Exception e){
            					
            				}
                    		break;
                    	case INTENSITY_DETECTION:
            				sde.setDetection(extraFields[0]);
                    		break;
                    	default:
                    		break;
                    	}

            		}else{
            			if(i==1){
            				meta = slideDataArray[0].getSlideMetaData();                    	
            			}
            			slideDataArray[i].setIntensities(counter-preSpotRows,intensities[0],intensities[1]);
                    	switch (matrixState) {
                    	case INTENSITY_PVAL:
                    		break;
                    	case INTENSITY_DETECT_PVAL:
            				((FloatSlideData)slideDataArray[i]).setDetection(counter-preSpotRows,extraFields[0]);
            				try{
            					((FloatSlideData)slideDataArray[i]).setPvalue(counter-preSpotRows,new Float((NumberFormat.getInstance()).parse(extraFields[1]).floatValue()));
            				}catch(Exception e){
                					
                				}
            				
                    		break;
                    	case INTENSITY_DETECTION:
            				((FloatSlideData)slideDataArray[i]).setDetection(counter-preSpotRows,extraFields[0]);
                    		break;
                    	default:
                    		break;
                    	}
            		}
            	}
            } else {
				//we have additional sample annotation. 
				//Add the additional sample annotation to the SampleAnnotation object

/*
				//advance to sample key
				for (int i = 0; i < preExperimentColumns - 1; i++) {
					ss.nextToken();
				}
				String key = ss.nextToken();

				for (int j = 0; j < slideDataArray.length; j++) {
					
					if(slideDataArray[j].getSampleAnnotation()!=null){
					
						String val=ss.nextToken();
						slideDataArray[j].getSampleAnnotation().setAnnotation(key, val);
						
					}else{
							SampleAnnotation sampAnn=new SampleAnnotation();
							sampAnn.setAnnotation(key, ss.nextToken());
							slideDataArray[j].setSampleAnnotation(sampAnn);
							slideDataArray[j].setSampleAnnotationLoaded(true);
					}
				}
*/
			}
            	
            this.setFileProgress(counter);
           	counter++;
           	//System.out.print(counter);
        }
        reader.close();
        //Added by Sarita to populate SampleAnnotation model with fields from SDRF
       // System.out.println("IDF file path:"+getIDFFilePath());
        if(getIDFFilePath()!=null){
        	URL fileURL;
        	try {
        		fileURL = new URL("file:///" +getIDFFilePath());
        		investigation=mageTabParser.parse(fileURL);
        	} catch(IOException ioe) {
        		String text= "<html><body><font face=arial size=4><b><center>We could not load the IDF and SDRF files you provided</center><b><hr size=3><br>";//<hr size=3>";
        		text += "<font face=arial size=4>The IDF or SDRF file could not be located. <br>";
        		text += "Check that both files are in the same directory.<br><br>";
        		text+="<br><br></body></html>";
        		JOptionPane.showMessageDialog(null,text , "Unable to locate MAGE-TAB files.", JOptionPane.WARNING_MESSAGE);
        	} catch(ParseException pe){
          		String text=
          		"<html><body><font face=arial size=4><b><center>We could not load the IDF and SDRF files you provided</center><b><hr size=3><br>";//<hr size=3>";
                text += "<font face=arial size=4>1. The IDF and SDRF files seem to be MAGE TAB version 1.1. We support 1.0<br>";
                text += "2. Check the column names of SDRF file. If Protocol Ref has any prefixes, delete the prefix.<br><br>";
                text += "3. Check if the SDRF file tag in IDF contains the correct SDRF file name" ;
                text+="<br><br></body></html>";
          		JOptionPane.showMessageDialog(null,text , "Loader Parse failure", JOptionPane.WARNING_MESSAGE);
          	} catch (Exception e) {
          		e.printStackTrace();
        	}
        	if(investigation != null) {
        		try {
        			populateIDFObject(investigation.IDF);
        		} catch (NullPointerException npe) {
        			String text= "<html><body><font face=arial size=4><b><center>Parse failure</center><b><hr size=3><br>";//<hr size=3>";
        			text += "<font face=arial size=4>The IDF file could not be parsed due to an incorrect MAGE-TAB version (We support v.1.0). <br>";
        			text += "Check the MAGE-TAB specification to ensure that the files are correctly formated.<br><br>";
        			text+="<br><br></body></html>";
        			JOptionPane.showMessageDialog(null,text , "Unable to parse MAGE-TAB files.", JOptionPane.WARNING_MESSAGE);        		  
        		} catch (Exception e) {
        			e.printStackTrace();
        		}
        		try {
        			populateSampleAnnotationfromSDRF(slideDataArray);
        		} catch (NullPointerException npe) {
    				String text= "<html><body><font face=arial size=4><b><center>Parse failure</center><b><hr size=3><br>";//<hr size=3>";
    				text += "<font face=arial size=4>The SDRF file could not be parsed due to inconsistencies between IDF <br>";
    				text += "and SDRF files or an incorrect MAGE-TAB version (We support v.1.0). <br>";
    				text += "1. Check that the BioMaterial names in the SDRF file match the column headers in the data matrix.<br><br>";
    				text += "2. Check that the Experimental Factor Values match those listed in the IDF.<br><br>";
    				text += "3. Check that the Protocols names and Parameter names match those listed in the IDF.<br><br>";
    				text += "4. Check to make sure that there are no blank cells in your file.<br><br>";
    				text += "5. Lastly, check the MAGE-TAB specification to ensure that the files are correctly formated.<br><br>";
    				text+="<br><br></body></html>";
    				JOptionPane.showMessageDialog(null,text , "Unable to parse MAGE-TAB files.", JOptionPane.WARNING_MESSAGE);        		  
        		} catch (Exception e) {
               		e.printStackTrace();
        		}
        	}
        }

        Vector data = new Vector(slideDataArray.length);
        for(int j = 0; j < slideDataArray.length; j++)
        	data.add(slideDataArray[j]);
        this.setFilesProgress(1);
        return data;
    }
    
    public FileFilter getFileFilter() {
        
        FileFilter mevFileFilter = new FileFilter() {
            
            public boolean accept(File f) {
                if (f.isDirectory()) return true;
                if (f.getName().endsWith(".txt")) return true;
                else return false;
            }
            
            public String getDescription() {
                return "MAGE-TAB Files(*idf.txt, *sdrf.txt, *adf.txt, *data.txt)";
            }
        };
        
        return mevFileFilter;
    }
    
    public boolean checkLoadEnable() {
        
        // Currently, the only requirement is that a cell has been highlighted
        
        int tableRow = sflp.getXRow() + 1; // Adjusted by 1 to account for the table header
        int tableColumn = sflp.getXColumn();
        if (tableColumn < 0) return false;
        
        TableModel model = sflp.getTable().getModel();
        String fieldSummary = "";
        for (int i = 0; i < tableColumn; i++) {
            // System.out.print(model.getColumnName(i) + (i + 1 == tableColumn ? "\n" : ", "));
            fieldSummary += model.getColumnName(i) + (i + 1 == tableColumn ? "" : ", ");
        }
        
        sflp.setFieldsText(fieldSummary);
        
        if (tableRow >= 1 && tableColumn >= 0) {
            setLoadEnabled(true);
            return true;
        } else {
            setLoadEnabled(false);
            return false;
        }
    }
    
    public boolean validateFile(File targetFile) {
        return true; // For now, no validation on Mas5 Files
    }
    
    public JPanel getFileLoaderPanel() {
        return sflp;
    }
    public int getDataType(){
        return this.DataType;
    }


	public String getAnnotationFilePath() {
		return this.sflp.getAnnFilePath();
	}  
	public boolean isAnnotationSelected() {
		return sflp.isAnnotationSelected();
	}
   
    public void processAffyGCOSFile(File targetFile) {

    	//columnHeaders includes all row1 items including duplicates
        Vector<String> columnHeaders = new Vector<String>();
        //quantHeaders includes all row2 items including duplicates
        Vector<String> quantHeaders = new Vector<String>();
        //dataVector holds the expression data for the table model
        Vector<Vector<String>> dataVector = new Vector<Vector<String>>();
        Vector<String> rowVector = null;
        BufferedReader reader = null;
        //for reading and processing ...
        String currentLine = null;
        String current = "";
        String previous = "";
        //sampleNames holds the list of experiment names in order
        sampleNames = new ArrayList();
        //quantTypes holds the unique data types and their ordinal
        quantTypes = new Hashtable<String, Integer>();

        if (! validateFile(targetFile)) return;
        sflp.setDataFileName(targetFile.getAbsolutePath());
        DefaultTableModel model = new DefaultTableModel();
        
        try {
            reader = new BufferedReader(new FileReader(targetFile), 1024 * 128);
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        }
        
        try {
            StringSplitter ss = new StringSplitter('\t');
            currentLine = reader.readLine();
//          fix empty tabbs appending to the end of line by wwang
            while(currentLine.endsWith("\t")){
            	currentLine=currentLine.substring(0,currentLine.length()-1);
            }
            ss.init(currentLine);

            for (int i = 0; i < ss.countTokens()+1; i++) {
            	current = ss.nextToken();
               	columnHeaders.add(current);
               	if(! previous.equalsIgnoreCase(current)) {
               		if(i > 0) {
               			sampleNames.add(current);
//               			System.out.println("sample name:"+current);
               		}
               	}
               	previous = current;
            }
//            System.out.println("columnHeader" + columnHeaders.toString());
//            System.out.println("sampleNames" + sampleNames.toString());

            currentLine = reader.readLine();
            while(currentLine.endsWith("\t")){
            	currentLine=currentLine.substring(0,currentLine.length()-1);
            }
            ss.init(currentLine);
            boolean done = false;

            // clear quantTypes prior to adding items; previous data are
            // irrelevant to current file
            quantTypes.clear();
            // adding none may not be what I need if it increases the counter by 1 as well.
//            quantTypes.put("none", 0);
            int counter = 0;
            int numDataTypes = 0;
            for (int i = 0; i < ss.countTokens()+1; i++) {
            	current = ss.nextToken();
            	quantHeaders.add(current);
            	if(quantTypes.containsKey(current)) {
            		done = true;
            	} else {
            		if(i > 0 && ! done) {
            			quantTypes.put(current, i);
            			counter++;
            		}
            	}
            }
            numDataTypes = counter;
//            System.out.println("Columns per hyb: " + numDataTypes);
            String QTs = quantTypes.toString();
//            System.out.println(QTs);
            
            dataTypes = new String[quantTypes.keySet().size()];
/*
            dataTypes = (new Vector<String>(quantTypes.keySet()))
            		.toArray(new String[quantTypes.keySet().size()]
            );
 */            
            for(Enumeration i= quantTypes.keys(); i.hasMoreElements();) {
            	String key = (String) i.nextElement();
            	int ordinal = quantTypes.get(key);
            	dataTypes[ordinal - 1] = key;
            }
            
        	sflp.signalComboBox.removeAllItems();
        	sflp.detectionComboBox.removeAllItems();
        	sflp.pValueComboBox.removeAllItems();
        	sflp.dye1ComboBox.removeAllItems();
        	sflp.dye2ComboBox.removeAllItems();
        	sflp.ratioComboBox.removeAllItems();
        	sflp.signalComboBox.addItem("none");
        	sflp.detectionComboBox.addItem("none");
        	sflp.pValueComboBox.addItem("none");
        	sflp.dye1ComboBox.addItem("none");
        	sflp.dye2ComboBox.addItem("none");
        	sflp.ratioComboBox.addItem("none");

        	for(int i=0; i<dataTypes.length; i++) {
            	sflp.signalComboBox.addItem(dataTypes[i]);
            	sflp.detectionComboBox.addItem(dataTypes[i]);
            	sflp.pValueComboBox.addItem(dataTypes[i]);
            	sflp.dye1ComboBox.addItem(dataTypes[i]);
            	sflp.dye2ComboBox.addItem(dataTypes[i]);
            	sflp.ratioComboBox.addItem(dataTypes[i]);
            }
            refillDataTypeTable();
            //NOTE: the columnHeaders vector is not used in the guess... method
            //can probably remove the argument from that method
//            columnDataTypes = guessFileFormat(columnHeaders,dataTypes);
            columnDataTypes = guessFileFormat(dataTypes);

            /**@author jwhite
             * NOTES:
             * done 0. We will not put the drop-downs in the JTable, but in panels above it.
             * done 1. channel selection radio buttons
             * done 2. different panels for 1 or 2 color arrays with different comboboxes
             * done 3. 6 distinct comboboxes, each with list of datatypes from file
             * done 4. User selects
             * 5. We guess based on column headers in file which is signal and pre-populate lists
             * 6. Color code the columns once selected.
             * 7. create a 'guessing algorithm' to devine the measurements of interest
             * done 8. take out the ADF selection item; we will not need to deal with it
             */
            
            model.setColumnIdentifiers(columnHeaders);
            model.addRow(quantHeaders);

            int cnt = 0;
            while ((currentLine = reader.readLine()) != null && cnt < 100) {
                cnt++;
                ss.init(currentLine);
                rowVector = new Vector<String>();
                for (int i = 0; i < ss.countTokens()+1; i++) {
                    try {
                        rowVector.add(ss.nextToken());
                    } catch (java.util.NoSuchElementException nsee) {
                        rowVector.add(" ");
                    }
                }
                
                dataVector.add(rowVector);
                model.addRow(rowVector);
            }
            
            reader.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        
        sflp.setTableModel(model);
        Point p = guessFirstExpressionCell(dataVector);
        // points to selected table cell row and column
        sflp.setSelectedCell(1, 1);
        p.setLocation(1,1);
        checkLoadEnable(); //called in setSelectedCell
//        sflp.expressionTable.setCellSelectionEnabled(true);
        ////System.out.println(p.toString());
    }

    
     /**
     * refillDataTypeTable
     * @author jwhite
     * 
     * redraws the data types panel based on channel selection radio
     * buttons.  In order to display properly, the remove() and validate()
     * methods of the container panel must be called.  
     *
     */
    public void refillDataTypeTable () {

        if(sflp.oneChannelRadioButton.isSelected()) { 
        	////System.out.println("one channel");
			sflp.dataTypePanel.remove(0);
			sflp.dataTypePanel.add(sflp.oneChannelDataTypes,0);
			setDataType(IData.DATA_TYPE_AFFY_ABS);
			columnDataTypes.remove("MEV:channel1");
			columnDataTypes.remove("MEV:channel2");
			columnDataTypes.remove("MEV:ratio");
			columnDataTypes.remove("MEV:log");
			sflp.dye1ComboBox.setSelectedItem("none");
			sflp.dye2ComboBox.setSelectedItem("none");
			sflp.ratioComboBox.setSelectedItem("none");
		} else if(sflp.twoChannelRadioButton.isSelected()) {
        	////System.out.println("two channel");
			sflp.dataTypePanel.remove(0);
			sflp.dataTypePanel.add(sflp.twoChannelDataTypes,0);
        	setDataType(IData.DATA_TYPE_TWO_INTENSITY);
			columnDataTypes.remove("MEV:signal");
			columnDataTypes.remove("MEV:detection");
			columnDataTypes.remove("MEV:pvalue");
			sflp.signalComboBox.setSelectedItem("none");
			sflp.detectionComboBox.setSelectedItem("none");
			sflp.pValueComboBox.setSelectedItem("none");
        }
		sflp.dataTypePanel.validate();
        sflp.dataTypePanel.repaint();
    }
    
    /**
     * guessFileFormat
     * @author jwhite
     * 
     * guessFileFormat decides, based on column headers in rows 1 & 2
     * what type of data file has been selected.  It evaluates these items
     * a) channels
     * b) number of data types per hyb
     * c) number of hybs
     * d) which data type is signal
     * e) which data types are dye based channel intensities
     * f) whether p-value and detection fields are present
     * g) whether log(ratio) fields are present
     * 
     * guessFileFormat sets MAGETabFileLoaderPanel components based on 
     * its findings.  
     * 
     * hashtable foundItems map:
     * 		signal		1	MEV:signal
     * 		pvalue		2	MEV:pvalue
     * 		detecion	3	MEV:detection
     * 		channel1	4	MEV:channel1
     * 		channel2	5	MEV:channel2
     * 		ratio		6	MEV:ratio
     * 		log(ratio)	7	MEV:log
     * 
     * Combobox selected items and channel panel radio buttons are set
     * based on the MEV: items in the columnDataTypes hash
     * 
     */

    //  protected Hashtable guessFileFormat (Vector<String> row1, String [] row2) {
    protected Hashtable guessFileFormat (String [] row2) {
    	//create regex Pattern objects and a container for them
    	Vector<Pattern> patterns = new Vector<Pattern>();
    	Pattern sigPat = Pattern.compile(".*signal.*", Pattern.CASE_INSENSITIVE);
    	patterns.add(sigPat);	// 0
    	Pattern pValuePat = Pattern.compile(".*p.value.*", Pattern.CASE_INSENSITIVE);
    	patterns.add(pValuePat);	// 1
    	Pattern pCallPat = Pattern.compile(".*call.*", Pattern.CASE_INSENSITIVE);
    	patterns.add(pCallPat);	// 2
    	Pattern detectPat = Pattern.compile(".*detect.*", Pattern.CASE_INSENSITIVE);
    	patterns.add(detectPat);	//3
    	Pattern channelPat = Pattern.compile(".*ch.*1.*", Pattern.CASE_INSENSITIVE);
    	patterns.add(channelPat);	//4
    	Pattern channe2Pat = Pattern.compile(".*ch.*2.*", Pattern.CASE_INSENSITIVE);
    	patterns.add(channe2Pat);	//5
    	Pattern dyePat = Pattern.compile(".*dye.*", Pattern.CASE_INSENSITIVE);
    	patterns.add(dyePat);	//6
    	Pattern cy3Pat = Pattern.compile(".*cy3.*", Pattern.CASE_INSENSITIVE);
    	patterns.add(cy3Pat);	//7
    	Pattern cy5Pat = Pattern.compile(".*cy5.*", Pattern.CASE_INSENSITIVE);
    	patterns.add(cy5Pat);	//8
    	Pattern ratioPat = Pattern.compile(".*rat.*", Pattern.CASE_INSENSITIVE);
    	patterns.add(ratioPat);	//9
    	Pattern logPat = Pattern.compile(".*log.*", Pattern.CASE_INSENSITIVE);
    	patterns.add(logPat);	//10
		//for each data type string match to the current pattern
   		for(int i=0; i< row2.length; i++) {
           	String str = row2[i];
           	CharSequence chr = (CharSequence) str;
           	//loop through the patterns
           	for(int j=0;j< patterns.size() ;j++) {
       			Pattern p = patterns.get(j);
    			Matcher m = p.matcher(chr);
    			if(m.find()) {
    				//if we find something, set the data in  
    				//each of our containers.  
    				switch (j) {
    				case 0:	// signal
    					columnDataTypes.put(str,"signal");
    					columnDataTypes.put("MEV:signal",str);
    					break;
    				case 1:	// p-value
    					columnDataTypes.put(str,"pvalue");
    					columnDataTypes.put("MEV:pvalue",str);
    					break;
    				case 2:	// p-call
    				case 3:	// detect(ion)
    					columnDataTypes.put(str,"detection");
    					columnDataTypes.put("MEV:detection",str);
    					break;
    				case 4:	// ch(annel) 1
    				case 7:	// cy3
    					columnDataTypes.put(str,"channel1");
    					columnDataTypes.put("MEV:channel1",str);
    					//foundItems.set(4);
    					break;
    				case 5: // ch(annel) 2
    				case 8:	// cy5
    					columnDataTypes.put(str,"channel2");
    					columnDataTypes.put("MEV:channel2",str);
    					break;
    				case 6:	// dye
    					if(str.indexOf("1") > 0) {
        					columnDataTypes.put(str,"channel1");
        					columnDataTypes.put("MEV:channel1",str);
    					} else if (str.indexOf("2") > 0) {
        					columnDataTypes.put("MEV:channel2",str);
        					columnDataTypes.put(str,"channel2");
    					}
    					break;
    				case 9:	// rat(io)
    					columnDataTypes.put(str,"ratio");
    					columnDataTypes.put("MEV:ratio",str);
    					break;
    				case 10:	// log
    					columnDataTypes.put(str,"log");
    					columnDataTypes.put("MEV:log",str);
    					break;
    				default:
    					break;
    				}
//    				System.out.println(str + ", " + j + ": " + p.toString());
    			} else {
//    				System.out.println(p.toString() + " not found in " + str);
    			}
    			if(! columnDataTypes.containsKey(str)) {
    				columnDataTypes.put(str,"none");
    			}
    		}
       	}
   		String stateStr = setListsAndButtons();
//    	System.out.println(columnDataTypes.toString());
//    	System.out.println("matrixState set to: " + stateStr);
    	
    	return columnDataTypes;
    }
    
    public String setListsAndButtons () {
    	//check the containers for 'MEV:...' keys to set
    	//the channel radio buttons and the selected items for 
    	//the combo boxes
    	//Originally this was in the guessFileFormat method but was 
    	//split out in order to call it from the EventListener
    	
    	if(columnDataTypes.containsKey("MEV:signal") ||
    		columnDataTypes.containsKey("MEV:pvalue") ||
    		columnDataTypes.containsKey("MEV:detection")) {
			sflp.oneChannelRadioButton.doClick();
			if(columnDataTypes.containsKey("MEV:signal")) {
				sflp.signalComboBox.setSelectedItem(columnDataTypes.get("MEV:signal"));
				setMatrixState(matrixType.INTENSITY);
			} else {
				sflp.signalComboBox.setSelectedItem("none");
			}
			if(columnDataTypes.containsKey("MEV:detection")) {
				sflp.detectionComboBox.setSelectedItem(columnDataTypes.get("MEV:detection"));
				setMatrixState(matrixType.INTENSITY_DETECTION);
			} else {
				sflp.detectionComboBox.setSelectedItem("none");
			}
			if(columnDataTypes.containsKey("MEV:pvalue") && columnDataTypes.containsKey("MEV:detection")) {
				sflp.pValueComboBox.setSelectedItem(columnDataTypes.get("MEV:pvalue"));
				setMatrixState(matrixType.INTENSITY_DETECT_PVAL);
			} else if(columnDataTypes.containsKey("MEV:pvalue")) {
				sflp.pValueComboBox.setSelectedItem(columnDataTypes.get("MEV:pvalue"));
				setMatrixState(matrixType.INTENSITY_PVAL);				
			} else {
				sflp.pValueComboBox.setSelectedItem("none");				
			}
    	} else if(columnDataTypes.containsKey("MEV:log")) {
    		sflp.twoChannelRadioButton.doClick();
			sflp.ratioComboBox.setSelectedItem(columnDataTypes.get("MEV:log"));
			if(columnDataTypes.containsKey("MEV:channel1") &&
				columnDataTypes.containsKey("MEV:channel2")) {
	   				sflp.dye1ComboBox.setSelectedItem(columnDataTypes.get("MEV:channel1"));
	   				sflp.dye2ComboBox.setSelectedItem(columnDataTypes.get("MEV:channel2"));
	   				setMatrixState(matrixType.LOG_CHANNELS);
				} else {
					sflp.dye1ComboBox.setSelectedItem(columnDataTypes.get("none"));
					sflp.dye2ComboBox.setSelectedItem(columnDataTypes.get("none"));
					setMatrixState(matrixType.LOG_RATIO);
				}
    	} else if(columnDataTypes.containsKey("MEV:ratio")) {
    		sflp.twoChannelRadioButton.doClick();
			sflp.ratioComboBox.setSelectedItem(columnDataTypes.get("MEV:ratio"));
			if(columnDataTypes.containsKey("MEV:channel1") &&
				columnDataTypes.containsKey("MEV:channel2")) {
   					sflp.dye1ComboBox.setSelectedItem(columnDataTypes.get("MEV:channel1"));
   					sflp.dye2ComboBox.setSelectedItem(columnDataTypes.get("MEV:channel2"));
   					setMatrixState(matrixType.RATIO_CHANNELS);
			} else {
					sflp.dye1ComboBox.setSelectedItem(columnDataTypes.get("none"));
					sflp.dye2ComboBox.setSelectedItem(columnDataTypes.get("none"));
					setMatrixState(matrixType.RATIO);
			}
    	} else if(columnDataTypes.containsKey("MEV:channel1") &&
				columnDataTypes.containsKey("MEV:channel2")) {
    		sflp.twoChannelRadioButton.doClick();
			sflp.ratioComboBox.setSelectedItem(columnDataTypes.get("none"));
			sflp.dye1ComboBox.setSelectedItem(columnDataTypes.get("MEV:channel1"));
			sflp.dye2ComboBox.setSelectedItem(columnDataTypes.get("MEV:channel2"));
   			setMatrixState(matrixType.CHANNELS);
    	} else {
    		sflp.oneChannelRadioButton.doClick();
			sflp.signalComboBox.setSelectedItem("none");
			sflp.detectionComboBox.setSelectedItem("none");
			sflp.pValueComboBox.setSelectedItem("none");
			sflp.ratioComboBox.setSelectedItem("none");
			sflp.dye1ComboBox.setSelectedItem("none");
			sflp.dye2ComboBox.setSelectedItem("none");
			setMatrixState(matrixType.NONE);
    	}
//    	System.out.println("Guess, matrixState: " + matrixState.name());
    	String matrixStateStr = getMatrixState().name();
    	return matrixStateStr;
    }

    public String getFilePath() {
        return this.sflp.selectedDataMatrix.getText();
    }
    
    
    public void openDataPath() {
        this.sflp.openDataPath();
    }
    
/*
//
//	AffyGCOSFileLoader - Internal Classes
//
 */
    
    private class MAGETABFileLoaderPanel extends JPanel {
    	
        
    	JPanel fileSelectionPanel;
        JTable expressionTable;
        JLabel instructionsLabel;
        JScrollPane tableScrollPane;
        JPanel tablePanel;
        
        JPanel channelPanel;
        JPanel dataTypePanel;
        public JPanel oneChannelDataTypes;
        public JPanel twoChannelDataTypes;
        JPanel channelButtonPanel;

        JTextField selectedIDF;
        JTextField selectedSDRF;
        JTextField selectedADF;
        JTextField selectedDataMatrix;

        ButtonGroup channelButtonGroup;
        JRadioButton oneChannelRadioButton;
        JRadioButton twoChannelRadioButton;

        JPanel  annotationPanel, mainPanel;
        JLabel IDFSelectionLabel, SDRFSelectionLabel, dataSelection; //ADFSelectionLabel, 
        JLabel signalLabel,dye1Label,dye2Label,ratioLabel,pValueLabel,
        	detectionLabel;
        JComboBox signalComboBox,dye1ComboBox,dye2ComboBox,ratioComboBox,
        	pValueComboBox,detectionComboBox;
    	        
        JButton browseButton1;
        protected EventListener eventListener;
        JButton IDFbrowseButton1;
        protected EventListener IDFeventListener;
        JButton SDRFbrowseButton1;
        protected EventListener SDRFeventListener;

        JPanel fileLoaderPanel;
       
        private int xRow = -1;
        private int xColumn = -1;
        
        AnnotationDownloadHandler adh;
        
        public MAGETABFileLoaderPanel() {                
                setLayout(new GridBagLayout());

                adh = new AnnotationDownloadHandler(superLoader.viewer.getResourceManager(), superLoader.annotationLists, superLoader.defaultSpeciesName, superLoader.defaultArrayName);
                
                IDFeventListener=new EventListener();
                SDRFeventListener=new EventListener();
                eventListener=new EventListener();

                selectedDataMatrix = new JTextField(50);
                selectedDataMatrix.setEditable(true);
                selectedDataMatrix.setForeground(Color.black);
                selectedDataMatrix.setFont(new Font("monospaced", Font.BOLD, 12));
                
                selectedIDF = new JTextField(50);
                selectedIDF.setEditable(true);
                selectedIDF.setForeground(Color.black);
                selectedIDF.setFont(new Font("monospaced", Font.BOLD, 12));
             
                selectedSDRF = new JTextField(50);
                selectedSDRF.setEditable(true);
                selectedSDRF.setForeground(Color.black);
                selectedSDRF.setFont(new Font("monospaced", Font.BOLD, 12));

                IDFSelectionLabel=new JLabel();
                IDFSelectionLabel.setForeground(java.awt.Color.BLACK);
                String IDFTypeChoices = "<html> Select IDF </html>";
                IDFSelectionLabel.setText(IDFTypeChoices);

                SDRFSelectionLabel=new JLabel();
                SDRFSelectionLabel.setForeground(java.awt.Color.BLACK);
                String SDRFTypeChoices = "<html> Select SDRF </html>";
                SDRFSelectionLabel.setText(SDRFTypeChoices);

                dataSelection=new JLabel();
                dataSelection.setForeground(java.awt.Color.BLACK);
                String chooseFile="<html>Select data matrix file</html>";
                dataSelection.setText(chooseFile);
                               
                IDFbrowseButton1=new JButton("Browse");
                IDFbrowseButton1.addActionListener(IDFeventListener);
                IDFbrowseButton1.setSize(100, 30);
                IDFbrowseButton1.setPreferredSize(new Dimension(100, 30));
        		
                SDRFbrowseButton1=new JButton("Browse");
                SDRFbrowseButton1.addActionListener(SDRFeventListener);
                SDRFbrowseButton1.setSize(100, 30);
                SDRFbrowseButton1.setPreferredSize(new Dimension(100, 30));

                browseButton1=new JButton("Browse");
                browseButton1.addActionListener(eventListener);
               	browseButton1.setSize(100, 30);
        		browseButton1.setPreferredSize(new Dimension(100, 30));

        		fileSelectionPanel = new JPanel();
                fileSelectionPanel.setLayout(new GridBagLayout());
                fileSelectionPanel.setBorder(new TitledBorder(new EtchedBorder(), "File    (MAGE-TAB Format Files)"));
           
                gba.add(fileSelectionPanel, IDFSelectionLabel, 0, 0, 1, 1, 0, 0, GBA.B,GBA.C, new Insets(2, 2, 2, 2), 0, 0);
                gba.add(fileSelectionPanel, selectedIDF, 1, 0, 1, 1, 1, 0, GBA.H, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
        		gba.add(fileSelectionPanel, IDFbrowseButton1, 2, 0, GBA.RELATIVE, 1, 0,0, GBA.NONE, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
                gba.add(fileSelectionPanel, SDRFSelectionLabel, 0, 1, 1, 1, 0, 0, GBA.B,GBA.C, new Insets(2, 2, 2, 2), 0, 0);
                gba.add(fileSelectionPanel, selectedSDRF, 1, 1, 1, 1, 1, 0, GBA.H, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
        		gba.add(fileSelectionPanel, SDRFbrowseButton1, 2, 1, GBA.RELATIVE, 1, 0,0, GBA.NONE, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
                gba.add(fileSelectionPanel, dataSelection, 0, 2, 1, 1, 0, 0, GBA.B,GBA.C, new Insets(2, 2, 2, 2), 0, 0);
                gba.add(fileSelectionPanel, selectedDataMatrix, 1, 2, 1, 1, 1, 0, GBA.H, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
        		gba.add(fileSelectionPanel, browseButton1, 2, 2, GBA.RELATIVE, 1, 0,0, GBA.NONE, GBA.C, new Insets(2, 2, 2, 2), 0, 0);

                annotationPanel = adh.getAnnotationLoaderPanel(gba);
              //  adh.setDownloadEnabled(true);--commented by Sarita
                
                /**@author jwhite
                 * NOTES:
                 * 0. We will not put the drop-downs in the JTable, but in panels above it.
                 * 1. x channel selection radio buttons
                 * 2. x different panels for 1 or 2 color arrays with different comboboxes
                 * 3. x 6 distinct comboboxes, each with list of datatypes from file
                 * 4. x User selects
                 * 5. We guess based on column headers in file which is signal and pre-populate lists
                 * 6. Color code the columns once selected.
                 * 7. create a 'guessing algorithm' to devine the measurements of interest
                 * 8. x take out the ADF selection item; we will not need to deal with it
                 * 
                 * REVERT to version 1576 to regain table definitions
                 */
                
        		channelPanel = new JPanel();
                channelPanel.setLayout(new GridBagLayout());
                channelPanel.setBorder(new TitledBorder(new EtchedBorder(), "Select Channel Data"));
                channelButtonGroup = new ButtonGroup();
                oneChannelRadioButton = new JRadioButton("1 Channel", true);
                oneChannelRadioButton.addActionListener(new EventListener());
                channelButtonGroup.add(oneChannelRadioButton);
                twoChannelRadioButton = new JRadioButton("2 Channels");
                twoChannelRadioButton.addActionListener(new EventListener());
                channelButtonGroup.add(twoChannelRadioButton);
                channelButtonPanel = new JPanel();
                channelButtonPanel.setLayout(new GridBagLayout());
                channelButtonPanel.add(oneChannelRadioButton);
                channelButtonPanel.add(twoChannelRadioButton);
                gba.add(channelButtonPanel, oneChannelRadioButton, 0, 0, 1, 1, 0, 0, GBA.H, GBA.C, new Insets(2, 2, 0, 2), 0, 0);
                gba.add(channelButtonPanel, twoChannelRadioButton, 0, 1, 1, 1, 0, 0, GBA.H, GBA.C, new Insets(2, 2, 0, 2), 0, 0);
                dataTypePanel = new JPanel();

                oneChannelDataTypes = new JPanel();
                oneChannelDataTypes.setLayout(new GridBagLayout());
                signalLabel = new JLabel("Signal");
                pValueLabel = new JLabel("p-value");
                detectionLabel = new JLabel("P/A Call");
                signalComboBox = new JComboBox();
                signalComboBox.addActionListener(this.eventListener);
                pValueComboBox = new JComboBox();
                pValueComboBox.addActionListener(this.eventListener);
                detectionComboBox = new JComboBox();
                detectionComboBox.addActionListener(this.eventListener);
                signalLabel.setPreferredSize(new Dimension(200,20));
                pValueLabel.setPreferredSize(new Dimension(200,20));
                detectionLabel.setPreferredSize(new Dimension(200,20));
                signalComboBox.setPreferredSize(new Dimension(200,20));
                pValueComboBox.setPreferredSize(new Dimension(200,20));
                detectionComboBox.setPreferredSize(new Dimension(200,20));
                gba.add(oneChannelDataTypes, signalLabel, 		0, 0, 1, 1, 1, 0, GBA.H, GBA.C, new Insets(2, 2, 0, 2), 0, 0);
                gba.add(oneChannelDataTypes, detectionLabel, 	1, 0, 1, 1, 1, 0, GBA.H, GBA.C, new Insets(2, 2, 0, 2), 0, 0);
                gba.add(oneChannelDataTypes, pValueLabel, 		2, 0, 1, 1, 1, 0, GBA.H, GBA.C, new Insets(2, 2, 0, 2), 0, 0);
                gba.add(oneChannelDataTypes, signalComboBox, 	0, 1, 1, 1, 1, 0, GBA.H, GBA.C, new Insets(2, 2, 0, 2), 0, 0);
                gba.add(oneChannelDataTypes, detectionComboBox, 1, 1, 1, 1, 1, 0, GBA.H, GBA.C, new Insets(2, 2, 0, 2), 0, 0);
                gba.add(oneChannelDataTypes, pValueComboBox, 	2, 1, 1, 1, 1, 0, GBA.H, GBA.C, new Insets(2, 2, 0, 2), 0, 0);

                twoChannelDataTypes = new JPanel();
                twoChannelDataTypes.setLayout(new GridBagLayout());
                dye1Label = new JLabel("Intensity 1");
                dye2Label = new JLabel("Intensity 2");
                ratioLabel = new JLabel("Ratio / Log(ratio)");
                dye1ComboBox = new JComboBox();
                dye1ComboBox.addActionListener(this.eventListener);
                dye2ComboBox = new JComboBox();
                dye2ComboBox.addActionListener(this.eventListener);
                ratioComboBox = new JComboBox();
                ratioComboBox.addActionListener(this.eventListener);
                dye1Label.setPreferredSize(new Dimension(200,20));
                dye2Label.setPreferredSize(new Dimension(200,20));
                ratioLabel.setPreferredSize(new Dimension(200,20));
                dye1ComboBox.setPreferredSize(new Dimension(200,20));
                dye2ComboBox.setPreferredSize(new Dimension(200,20));
                ratioComboBox.setPreferredSize(new Dimension(200,20));
                gba.add(twoChannelDataTypes, dye1Label, 	0, 0, 1, 1, 1, 0, GBA.H, GBA.C, new Insets(2, 2, 0, 2), 0, 0);
                gba.add(twoChannelDataTypes, dye2Label, 	1, 0, 1, 1, 1, 0, GBA.H, GBA.C, new Insets(2, 2, 0, 2), 0, 0);
                gba.add(twoChannelDataTypes, ratioLabel, 	2, 0, 1, 1, 1, 0, GBA.H, GBA.C, new Insets(2, 2, 0, 2), 0, 0);
                gba.add(twoChannelDataTypes, dye1ComboBox, 	0, 1, 1, 1, 1, 0, GBA.H, GBA.C, new Insets(2, 2, 0, 2), 0, 0);
                gba.add(twoChannelDataTypes, dye2ComboBox, 	1, 1, 1, 1, 1, 0, GBA.H, GBA.C, new Insets(2, 2, 0, 2), 0, 0);
                gba.add(twoChannelDataTypes, ratioComboBox, 2, 1, 1, 1, 1, 0, GBA.H, GBA.C, new Insets(2, 2, 0, 2), 0, 0);

                if(oneChannelRadioButton.isSelected()) { 
                	dataTypePanel.add(oneChannelDataTypes);
        		} else { 
                	dataTypePanel.add(twoChannelDataTypes);
        		}
                dataTypePanel.validate();

                gba.add(channelPanel, channelButtonPanel, 0, 0, 1, 2, 0, 0, GBA.NONE, GBA.C, new Insets(2, 2, 0, 2), 0, 0);
                gba.add(channelPanel, dataTypePanel, 1, 0, 5, 2, 1, 1, GBA.H, GBA.C, new Insets(2, 2, 0, 2), 0, 0);

        		myCellRenderer = new ExpressionFileTableCellRenderer();
                expressionTable = new JTable();
                expressionTable.setDefaultRenderer(Object.class, myCellRenderer);
    			expressionTable.setGridColor(Color.LIGHT_GRAY);
                expressionTable.setCellSelectionEnabled(true);
                expressionTable.setColumnSelectionAllowed(false);
                expressionTable.setRowSelectionAllowed(false);
                expressionTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
                expressionTable.getTableHeader().setReorderingAllowed(false);
                
                expressionTable.addMouseListener(new MouseAdapter() {
                    public void mousePressed(MouseEvent event) {
                        setSelectedCell(expressionTable.rowAtPoint(event.getPoint()), 
                        		expressionTable.columnAtPoint(event.getPoint()));
                    }
                });
                
                tableScrollPane = new JScrollPane(expressionTable, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
                
                instructionsLabel = new JLabel();
                instructionsLabel.setForeground(java.awt.Color.red);
                String instructions = "<html>Select the appropriate number of channels, then select appropriate data columns from the dropdown lists.</html>";
                instructionsLabel.setText(instructions);
                
                tablePanel = new JPanel();
                tablePanel.setLayout(new GridBagLayout());
                tablePanel.setBorder(new TitledBorder(new EtchedBorder(), "Expression Table"));
                  
                gba.add(tablePanel, tableScrollPane, 0, 0, 1, 2, 1, 1, GBA.B, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
                gba.add(tablePanel, instructionsLabel, 0, 2, 1, 1, 1, 0, GBA.H, GBA.C, new Insets(2, 2, 2, 2), 0, 0);

                fileLoaderPanel = new JPanel();
                fileLoaderPanel.setLayout(new GridBagLayout());
                
                gba.add(fileLoaderPanel,fileSelectionPanel, 0, 0, 1, 1, 1, 1, GBA.B, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
                gba.add(fileLoaderPanel, annotationPanel, 	0, 2, 1, 1, 1, 1, GBA.B, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
                gba.add(fileLoaderPanel, channelPanel, 		0, 4, 1, 2, 3, 0, GBA.H, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
                gba.add(fileLoaderPanel, tablePanel, 		0, 7, 1, 6, 3, 6, GBA.B, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
                gba.add(this, fileLoaderPanel,	0, 0, 1, 1, 1, 1, GBA.B, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
        }

		public boolean isAnnotationSelected() {
			return adh.isAnnotationSelected();
		}
		public String getAnnFilePath() {
			return adh.getAnnFilePath();
		}
        private void setSelectedCell( int xR, int xC) {
            xRow = xR;
            xColumn = xC;
            myCellRenderer.setSelected(xRow, xColumn);
            expressionTable.repaint();
            checkLoadEnable();
        }
        public void openDataPath() {
            //fileTreePane.openDataPath();
        }     

        public void onBrowse(String fileType) {
        	File dir;
        	String pathName;
        	String fileName;
        	JFileChooser fileChooser=new JFileChooser(SuperExpressionFileLoader.DATA_PATH);
        	dir = new File(TMEV.getDataPath());
        	fileChooser.setCurrentDirectory(dir);
    		if(fileType.equalsIgnoreCase("IDF")) {
    			fileChooser.setDialogTitle("Select IDF");
    		} else if(fileType.equalsIgnoreCase("SDRF")) {
    			fileChooser.setDialogTitle("Select SDRF");
    		} else if(fileType.equalsIgnoreCase("ADF")) {
    			fileChooser.setDialogTitle("Select ADF");
    		} else if(fileType.equalsIgnoreCase("DATA")) {
    			fileChooser.setDialogTitle("Select Data Matrix");
    		}
        	int retVal=fileChooser.showOpenDialog(MAGETABFileLoaderPanel.this);
        	
        	if(retVal==JFileChooser.APPROVE_OPTION) {
        		File selectedFile=fileChooser.getSelectedFile();
				fileName = selectedFile.getAbsolutePath();
				String baseName = "";
				String textS = "";
        		if(fileType.equalsIgnoreCase("IDF")) {
    				selectedIDF.setText(fileName);
    				selectedIDF.setForeground(Color.BLACK);
    				if(fileName.endsWith("idf.txt")) {
    					textS = "idf.txt";
    				} else if(fileName.endsWith("IDF.txt")) {
    					textS = "IDF.txt";
    				}
    				if(selectedFile.exists())
    					setIDFFilePath(selectedFile.getAbsolutePath());
        		} else if(fileType.equalsIgnoreCase("SDRF")) {
    				selectedSDRF.setText(selectedFile.getAbsolutePath());
    				selectedSDRF.setForeground(Color.BLACK);
    				if(fileName.endsWith("sdrf.txt")) {
    					textS = "sdrf.txt";
    				} else if(fileName.endsWith("SDRF.txt")) {
    					textS = "SDRF.txt";
    				}
        		} else if(fileType.equalsIgnoreCase("ADF")) {
    				selectedADF.setText(selectedFile.getAbsolutePath());
    				selectedADF.setForeground(Color.BLACK);
    				if(fileName.endsWith("adf.txt")) {
    					textS = "adf.txt";
    				} else if(fileName.endsWith("ADF.txt")) {
    					textS = "ADF.txt";
    				}
        		} else if(fileType.equalsIgnoreCase("DATA")) {
        			selectedDataMatrix.setText(selectedFile.getAbsolutePath());
        			selectedDataMatrix.setForeground(Color.BLACK);
    				if(fileName.endsWith("data.txt")) {
    					textS = "data.txt";
    				} else if(fileName.endsWith("DATA.txt")) {
    					textS = "DATA.txt";
    				}
//    				if(selectedFile.exists()) {
//    					processAffyGCOSFile(selectedFile);
//    					setLoadEnabled(true);
//    				}
        		} else {
        			baseName = new String("Logic error; you will have to select each file .");
        		}
        		//basename is the common name string of the MAGE-TAB files
				baseName = fileName.substring(0,fileName.lastIndexOf(textS)); 

				//If field is null or color is red, then process, otherwise the file exists
        		if(selectedSDRF.getText().equals("") || selectedSDRF.getForeground().equals(Color.RED)) {
        			//look for filename with sdrf.txt ending (per spec.)
        			String testName = baseName + "sdrf.txt";
					selectedSDRF.setText(testName);
					File testSDRF = new File(testName);
	        		if(! testSDRF.exists()) {
	        			selectedSDRF.setText("File " + testName + " does not exist.");
	        			selectedSDRF.setForeground(Color.RED);
	        			setLoadEnabled(false);
	        		} else {
	        			selectedSDRF.setForeground(Color.BLACK);
	        		}
        		}

        		if(selectedIDF.getText().equals("") || selectedIDF.getForeground().equals(Color.RED)) { 
        			//look for filename with idf.txt ending (per spec.)
        			String testName = baseName + "idf.txt";
        			selectedIDF.setText(testName);
        			File testIDF = new File(testName);
        			if(! testIDF.exists()) {
        				selectedIDF.setText("File " + testName + " does not exist.");
	        			selectedIDF.setForeground(Color.RED);
        				setLoadEnabled(false);
	        		} else {
	        			selectedIDF.setForeground(Color.BLACK);
        			}
				}

        		if(selectedDataMatrix.getText().equals("") || selectedDataMatrix.getForeground().equals(Color.RED)) {
        			//look for filename with data.txt ending (per spec.)
					String testName = baseName + "data.txt";
					selectedDataMatrix.setText(testName);
        			File dataFile = new File(testName);
        			if(! dataFile.exists()) {
        				selectedDataMatrix.setText("File " + testName + " does not exist.");
	        			selectedDataMatrix.setForeground(Color.RED);
        				setLoadEnabled(false);
        			} else {
        				selectedDataMatrix.setForeground(Color.BLACK);
        			}
				} else {
    				//process here; should work in all circumstances
					File dataFile = new File(selectedDataMatrix.getText());
    				processAffyGCOSFile(dataFile);
					//check for SOME form of 'signal' in the comboboxes
					if(sflp.signalComboBox.equals("none") && 
    						sflp.ratioComboBox.equals("none") &&
    						sflp.dye1ComboBox.equals("none") &&
    						sflp.dye2ComboBox.equals("none")) {
    						setLoadEnabled(false);
    				} else {
    					setLoadEnabled(true);
    				}
				}
        	}
        	dir = fileChooser.getCurrentDirectory();
        	pathName = dir.getPath();
        	TMEV.setDataPath(pathName);
    	}
         
       
        public JTable getTable() {
            return expressionTable;
        }
        
        public int getXColumn() {
            return xColumn;
        }
        
        public int getXRow() {
            return xRow;
        }
        
        public void setDataFileName(String fileName) {
            selectedDataMatrix.setText(fileName);
           // System.out.println(pathTextField);
        }
        
        public void setTableModel(TableModel model) {
            expressionTable.setModel(model);
            int numCols = expressionTable.getColumnCount();
            //System.out.print(numCols);
            for(int i = 0; i < numCols; i++){
                expressionTable.getColumnModel().getColumn(i).setMinWidth(75);
            }
        }
        
        public void setFieldsText(String fieldsText) {
           
        }

        public class EventListener implements ActionListener {
    		public void actionPerformed(ActionEvent event) {
    			Object source = event.getSource();
    			String fileType;
    			if (source == browseButton1) {
    				fileType = "DATA";
    				onBrowse(fileType);
    			} else if (source == IDFbrowseButton1) {
    				fileType = "IDF";
    				onBrowse(fileType);
    			} else if (source == SDRFbrowseButton1) {
    				fileType = "SDRF";
    				onBrowse(fileType);
    			} else if (source == signalComboBox) {
    				String columnHeader = (String) signalComboBox.getSelectedItem();
   					if(columnHeader != null) {
   						//When user selects an item from the dropdown list, add
   						//that item to the columnDataTypes--even if it's already 
   						//there, otherwise NullPointerException when the item is 
   						//missing from the hash because it was never put there in the first place
           				if(! columnHeader.equals("none")) {
    						columnDataTypes.put("MEV:signal", columnHeader);
    						columnDataTypes.put(columnHeader, "signal");
    					} else {
    						try{
    							String header = (String) columnDataTypes.get("MEV:signal");
        						columnDataTypes.put(header,"none");
        						columnDataTypes.remove("MEV:signal");
    						} catch (NullPointerException npe) {
    							//no need to do anything, just continue
    						}
    					}
//						System.out.println(columnDataTypes.toString());
    				}
    			} else if (source == detectionComboBox) {
    				String columnHeader = (String) detectionComboBox.getSelectedItem();
   					if(columnHeader != null) {
           				if(! columnHeader.equals("none")) {
    						columnDataTypes.put("MEV:detection", columnHeader);
    						columnDataTypes.put(columnHeader, "detection");
    					} else {
    						try{
    							String header = (String) columnDataTypes.get("MEV:detection");
        						columnDataTypes.put(header,"none");
        						columnDataTypes.remove("MEV:detection");
    						} catch (NullPointerException npe) {
    							//no need to do anything, just continue
//    							npe.printStackTrace();
    						}
    					}
//						System.out.println(columnDataTypes.toString());
    				}
    			} else if (source == pValueComboBox) {
    				String columnHeader = (String) pValueComboBox.getSelectedItem();
    				if(columnHeader != null) {
        				if(! columnHeader.equals("none")) {
    						columnDataTypes.put("MEV:pvalue", columnHeader);
    						columnDataTypes.put(columnHeader, "pvalue");
    					} else {
    						try{
    							String header = (String) columnDataTypes.get("MEV:pvalue");
        						columnDataTypes.put(header,"none");
        						columnDataTypes.remove("MEV:pvalue");
    						} catch (NullPointerException npe) {
    							//no need to do anything, just continue
    						}
        				}
//						System.out.println(columnDataTypes.toString());
					}
    			} else if (source == dye1ComboBox) {
    				String columnHeader = (String) dye1ComboBox.getSelectedItem();
   					if(columnHeader != null) {
           				if(! columnHeader.equals("none")) {
    						columnDataTypes.put("MEV:channel1", columnHeader);
   							columnDataTypes.put(columnHeader, "channel1");
    					} else {
    						try{
    							String header = (String) columnDataTypes.get("MEV:channel1");
        						columnDataTypes.put(header,"none");
        						columnDataTypes.remove("MEV:channel1");
    						} catch (NullPointerException npe) {
    							//no need to do anything, just continue
    						}
    					}
//						System.out.println(columnDataTypes.toString());
    				}
    			} else if (source == dye2ComboBox) {
    				String columnHeader = (String) dye2ComboBox.getSelectedItem();
   					if(columnHeader != null) {
           				if(! columnHeader.equals("none")) {
    						columnDataTypes.put("MEV:channel2", columnHeader);
    						columnDataTypes.put(columnHeader, "channel2");
    					} else {
    						try{
    							String header = (String) columnDataTypes.get("MEV:channel2");
        						columnDataTypes.put(header,"none");
        						columnDataTypes.remove("MEV:channel2");
    						} catch (NullPointerException npe) {
    							//no need to do anything, just continue
    						}
        				}
//						System.out.println(columnDataTypes.toString());
    				}
    			} else if (source == ratioComboBox) {
    				String columnHeader = (String) ratioComboBox.getSelectedItem();
   					if(columnHeader != null) {
   						if(columnHeader.contains("Log") || columnHeader.contains("log")
   								|| columnHeader.contains("LOG")) {
							columnDataTypes.put("MEV:log", columnHeader);
							columnDataTypes.put(columnHeader, "log");    				
   						} else if(! columnHeader.equals("none")) {
							columnDataTypes.put("MEV:ratio", columnHeader);
							columnDataTypes.put(columnHeader, "ratio");    				
   						} else {
   							try{
   								String header = (String) columnDataTypes.get("MEV:log");
   								columnDataTypes.put(header,"none");
   								columnDataTypes.remove("MEV:log");
   								header = (String) columnDataTypes.get("MEV:ratio");
   								columnDataTypes.put(header,"none");
  								columnDataTypes.remove("MEV:ratio");
   							} catch (NullPointerException npe) {
    						//no need to do anything, just continue
   							}   							
    					}
//						System.out.println(columnDataTypes.toString());
    				}
    			} else if (source == oneChannelRadioButton) {
//    				repaint the mtflp with one-channel fields
    				refillDataTypeTable();
    			} else if (source == twoChannelRadioButton) {
//    				repaint the mtflp with two-channel fields
    				refillDataTypeTable();
    			} 
    		}
    	}
    }

//getFramework.IFramework.addHistory

    public void populateSampleAnnotationfromSDRF(ISlideData[] slideDataArray){
    //	SDRF sdrfObj=investigation.SDRF;
    	
    	List<SourceNode> sourcenodes= (List<SourceNode>)((SDRF)investigation.SDRF).sourceNodes;
    	//System.out.println("slidedataarray length:"+slideDataArray.length);
    	//System.out.println("sourcenodes length:"+sourcenodes.size());
		//Number of source nodes should be equal to the number of samples in the file 
		for(int i=0; i<slideDataArray.length; i++){
			SourceNode src=sourcenodes.get(i);
			List<CharacteristicsAttribute> characteristicsList = src.characteristics;
			if(characteristicsList.size()!=0){
			for(int j=0; j<characteristicsList.size(); j++){
				slideDataArray[i].getSampleAnnotation().setAnnotation(characteristicsList.get(j).type, characteristicsList.get(j).getNodeName());
				
//				System.out.print("Characteristic:"+characteristicsList.get(j).type);
//				System.out.print('\t');
//				System.out.print(characteristicsList.get(j).getNodeName());
//				System.out.println();
			}
			}
			   
			
			
		}
		
		
		//FactorValueAttribute fValueAttrib=new FactorValueAttribute();
		List nodes=(investigation.SDRF).lookupNodes(HybridizationNode.class);
		
		for(int index=0; index<nodes.size(); index++){
		//for(int index=0; index<slideDataArray.length; index++){
	
		    HybridizationNode node = (HybridizationNode)nodes.get(index);
		    List<FactorValueAttribute> fvalist = node.factorValues;
		    for(int i=0; i<fvalist.size(); i++){
		    	FactorValueAttribute fva=(FactorValueAttribute)fvalist.get(i);
//		    	System.out.println("Factor value node type"+fva.getNodeType());
//		    	System.out.println("Factor value node name:"+fva.getNodeName());
//		    	
		    	slideDataArray[index].getSampleAnnotation().setAnnotation(fvalist.get(i).getNodeType(), fvalist.get(i).getNodeName());
		    	
		    	
		    }
		    //System.out.print("factorvalue:"+fva.getNodeName());
		}
       	
    	
    	

    }

    public void populateIDFObject(IDF idfObj){
    	ArrayList<String>alist=new ArrayList<String>();
    	alist.add(idfObj.dateOfExperiment);
    	mageIDFObject.setDateofExperiment(alist);
    	mageIDFObject.setExperimentalDesign(idfObj.experimentalDesign);
    	mageIDFObject.setExperimentalFactorName(idfObj.experimentalFactorName);
    	mageIDFObject.setExperimentalFactorTermSourceRef(idfObj.experimentalFactorTermSourceREF);
    	mageIDFObject.setExperimentalFactorTypes(idfObj.experimentalFactorType);
    	alist=new ArrayList<String>();
    	alist.add(idfObj.experimentDescription);
    	mageIDFObject.setExperimentDescription(alist);
    	alist=new ArrayList<String>();
    	alist.add(idfObj.investigationTitle);
    	mageIDFObject.setInvestigationTitle(alist);
    	mageIDFObject.setNormalizationTermSourceRef(idfObj.normalizationTermSourceREF);
    	mageIDFObject.setNormalizationType(idfObj.normalizationType);
    	mageIDFObject.setPersonAddress(idfObj.personAddress);
    	mageIDFObject.setPersonAffiliation(idfObj.personAffiliation);
    	mageIDFObject.setPersonEmail(idfObj.personEmail);
    	mageIDFObject.setPersonFax(idfObj.personFax);
    	mageIDFObject.setPersonFirstName(idfObj.personFirstName);
    	mageIDFObject.setPersonLastName(idfObj.personLastName);
    	mageIDFObject.setPersonMidInitials(idfObj.personMidInitials);
    	mageIDFObject.setPersonPhone(idfObj.personPhone);
    	mageIDFObject.setPersonRoles(idfObj.personRoles);
    	mageIDFObject.setPersonRolesTermSourceREF(idfObj.personRolesTermSourceREF);
    	mageIDFObject.setProtocolContact(idfObj.protocolContact);
    	mageIDFObject.setProtocolDescription(idfObj.protocolDescription);
    	mageIDFObject.setProtocolHardware(idfObj.protocolHardware);
    	mageIDFObject.setProtocolSoftware(idfObj.protocolSoftware);
    	mageIDFObject.setProtocolTermSourceRef(idfObj.protocolTermSourceREF);
    	mageIDFObject.setProtocolType(idfObj.protocolType);
    	mageIDFObject.setPublicationAuthorList(idfObj.publicationAuthorList);
    	mageIDFObject.setPublicationDOI(idfObj.publicationDOI);
    	mageIDFObject.setPublicationStatus(idfObj.publicationStatus);
    	mageIDFObject.setPublicationStatusTermSourceRef(idfObj.publicationStatusTermSourceREF);
    	mageIDFObject.setPublicationTitle(idfObj.publicationTitle);
    	alist=new ArrayList<String>();
    	alist.add(idfObj.publicReleaseDate);
    	mageIDFObject.setPublicReleaseDate(alist);
    	mageIDFObject.setPubMedID(idfObj.pubMedId);
    	mageIDFObject.setQualityControlTermSourceRef(idfObj.qualityControlTermSourceREF);
    	mageIDFObject.setQualityControlType(idfObj.qualityControlType);
    	mageIDFObject.setReplicateType(idfObj.replicateType);
    	mageIDFObject.setReplicateTypeTermSourceRef(idfObj.replicateTermSourceREF);
    	
    	String fname=(String)idfObj.sdrfFile.get(0);
    	alist=new ArrayList<String>();
    	alist.add(fname);
    	mageIDFObject.setSDRFFile(alist);
    	mageIDFObject.setTermSourceFile(idfObj.termSourceFile);
    	mageIDFObject.setTermSourceName(idfObj.termSourceName);
    	mageIDFObject.setTermSourceVersion(idfObj.termSourceVersion);
    	this.mav.setIDF(mageIDFObject);
    	
    }

    
    

/**
	 * Make a guess as to which of the data values represents the
	 * upper-leftmost expression value. Select that cell as the default.
	 */

    public Point guessFirstExpressionCell(Vector<Vector<String>> dataVector) {
		int guessCol = 0, guessRow = 0;
		Vector<String> lastRow = dataVector.get(dataVector.size() - 1);
		for (int j = lastRow.size() - 2; j >= 0; j--) {
			String thisEntry = lastRow.get(j);
			try {
				Float temp = new Float(thisEntry);
			} catch (Exception e) {
				guessCol = j + 1;
				break;
			}
		}
	
		for (int i = dataVector.size() - 1; i >= 0; i--) {
			Vector<String> thisRow = dataVector.get(i);
			try {
				String thisEntry = thisRow.get(guessCol);
				Float temp = new Float(thisEntry);
			} catch (Exception e) {
				guessRow = i + 1;
				break;
			}
		}
		return new Point(guessRow, guessCol);
	}


public void setAnnotationFilePath(String filePath) {
	// TODO Auto-generated method stub
	
}
public String getSDRFFilePath() {
	return SDRFFilePath;
}
public void setSDRFFilePath(String filePath) {
	SDRFFilePath = filePath;
}
public String getIDFFilePath() {
	return IDFFilePath;
}
public void setIDFFilePath(String filePath) {
	IDFFilePath = filePath;
}

}

