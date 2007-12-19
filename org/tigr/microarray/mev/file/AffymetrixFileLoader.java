/*
Copyright @ 1999-2005, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * $RCSfile: AffymetrixFileLoader.java,v $
 * $Revision: 1.7 $
 * $Date: 2007-12-19 21:39:36 $
 * $Author: saritanair $
 * $State: Exp $
 */
package org.tigr.microarray.mev.file;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Vector;
import java.util.Hashtable;

import javax.swing.JFrame;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;

import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;
import javax.xml.soap.SOAPMessage;

import org.tigr.microarray.mev.AffySlideDataElement;
import org.tigr.microarray.mev.FloatSlideData;
import org.tigr.microarray.mev.ISlideData;
import org.tigr.microarray.mev.ISlideMetaData;
import org.tigr.microarray.mev.MultipleArrayData;
import org.tigr.microarray.mev.MultipleArrayViewer;

import org.tigr.microarray.mev.SlideData;
import org.tigr.microarray.mev.TMEV;
import org.tigr.microarray.mev.cluster.gui.IData;


import org.tigr.microarray.mev.annotation.AnnotationDialog;
import org.tigr.microarray.mev.annotation.AnnotationFieldConstants;
import org.tigr.microarray.mev.annotation.AnnotationFileReader;
import org.tigr.microarray.mev.annotation.AnnotationURLConstants;
import org.tigr.microarray.mev.annotation.MevAnnotation;
import org.tigr.microarray.mev.annotation.PublicURL;
import org.tigr.microarray.mev.cluster.gui.IData;


import org.tigr.microarray.util.FileLoaderUtility;
import org.tigr.remote.soap.*;

import com.sun.org.apache.xml.internal.utils.URI;


public class AffymetrixFileLoader extends ExpressionFileLoader {

    private GBA gba;
    private AffymetrixFileLoaderPanel aflp;

    private boolean loadEnabled = false;
    private File refChipFile;// = new File(".", "Data/");
    private String mode = "";
    
    
    private File [] files;
    private int affyDataType = IData.DATA_TYPE_AFFY_ABS;
    private String[] fieldNames = new String[]{"Affy_ID", "Detection", "Description"};
  
    /**
     * Raktim - Annotation Specific
     * Place Holder for reading in Affy Anno 
     * MAV needed to pass on the ref to MevAnnotation Obj for MAV Index
     **/
    private Hashtable _tempAnno=new Hashtable();
    private MultipleArrayViewer mav;
    private File selectedAnnoFile;
    protected MevAnnotation mevAnno=new MevAnnotation();
    private String annotationFileName;

    public AffymetrixFileLoader(SuperExpressionFileLoader superLoader) {
        super(superLoader);
        this.mav = superLoader.getArrayViewer(); //Raktim - Annotation Specific
     //   System.out.println("Loader getInstanceIndex(): " + this.mav.getInstanceIndex());

        gba = new GBA();
        aflp = new AffymetrixFileLoaderPanel();
    }

    public Vector loadExpressionFiles() throws IOException {

        Object[] affymetrixFiles = aflp.getAffymetrixSelectedListModel().toArray();
        Object[] refFiles = aflp.getRefSelectedListModel().toArray();
        ISlideData [] data = null;
        
       
        files = new File[affymetrixFiles.length];
        for(int j = 0; j < affymetrixFiles.length ; j++) {
        	 File file=new File(this.aflp.pathTextField.getText(),((File) affymetrixFiles[j]).getName());
           // files[j] = (File)affymetrixFiles[j];
        	 files[j] = file;
        }

        /**
         * TODO
         * Raktim - Annotation Demo Only. 
         * Code to load Affy Annotation File into a Indexed Object
         */
        
        
        /*Loop added by Sarita to check if Annotation has been loaded
         * "isAnnotationLoaded" is a boolean variable, which is set
         * to "true" in the function onAnnotationFileBrowse().
         * 
         * The loop was included so as to enable loading data
         * irrespective of whether annotation was loaded or not
         * 
         */
        if(mav.getData().isAnnotationLoaded()) {
        	_tempAnno = loadAffyAnno(new File(getAnnotationFileName()));
        	//mav.getData().setAnnotationLoaded(true);
        }
        
     
        /**
         * TODO
         * Raktim - Annotation Demo Only. 
         * Good Place to initialize URLS.
         */
        if(PublicURL.loadURLs(new File("config/annotation_URLs.txt")) != 0){
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

 /************************************************************************************************************/
 /************************************************************************************************************/
        
        
 
        

        if(aflp.absoluteRadioButton.isSelected()){
            data = loadAffyAbsolute(files);
            this.affyDataType = IData.DATA_TYPE_AFFY_ABS;
        } else if (aflp.absMeanRadioButton.isSelected()){
              data = loadAffyAbsMean(files);
              this.affyDataType = IData.DATA_TYPE_AFFY_MEAN;


/*
        } else if (aflp.meanRadioButton.isSelected()){
            data = loadAffyMean(files);
            this.affyDataType = IData.DATA_TYPE_AFFY_MEAN;
        } else if (aflp.medianRadioButton.isSelected()){
            data = loadAffyMedian(files);
            this.affyDataType = IData.DATA_TYPE_AFFY_MEDIAN;

 */
        } else if ( aflp.referenceRadioButton.isSelected()){
           /* DefaultListModel list = aflp.getRefSelectedListModel();
            DefaultListModel dataList = aflp.getAffymetrixSelectedListModel();*/
        	
        	Object[] list = aflp.getRefSelectedListModel().toArray();
            Object[] dataList = aflp.getAffymetrixSelectedListModel().toArray();
        	
            if(list.length < 1)
                return null;
            if(list.length >= 1){
                if(dataList.length < 1)
                    return null;
                File refFile = new File(this.aflp.refFileListTextField.getText(),((File) list[0]).getName());
                File [] dataFiles = toFileArray(dataList);
                data = loadAffyReference(refFile, dataFiles);
                this.affyDataType = IData.DATA_TYPE_AFFY_REF;
            }
        }
        Vector carrier = new Vector();
        if(data != null){
            TMEV.setDataType(TMEV.DATA_TYPE_AFFY);
            for(int i = 0; i < data.length; i++)
                carrier.add(data[i]);
        }
        return carrier;
    }

    // converter
    private File [] toFileArray(Object [] files){
        File [] dataFiles = new File[files.length];
        for(int i = 0; i < files.length; i++)
            //
        	dataFiles[i] =new File(this.aflp.pathTextField.getText(),((File) files[i]).getName()); ;
        return dataFiles;
    }

    // Affy_ID:Detection:Description

    public ISlideData loadExpressionFile(File currentFile) throws IOException {

        return null;
    }

    public Vector loadReferenceFile(File currentFile) throws IOException {

        return new Vector();
    }

    private void setAffyDataType(int type){
        this.affyDataType = type;
    }

    public int getAffyDataType(){
        return this.affyDataType;
    }
    
    
    /**
     * Raktim - Annotation Demo. 
     * Loads Affy Annotation from a File
     */

    private Hashtable loadAffyAnno(File affyFile) {
   // private void loadAffyAnno(File affyFile) {
    	//System.out.println("loadAffyAnno");
    	Hashtable _temp = null;
    	AnnotationFileReader reader = new AnnotationFileReader();
    	try {
    		_temp = reader.loadAffyAnnotation(affyFile);
    		
    		
    		//reader.loadAffyAnnotation(affyFile);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return _temp;
    }
    
    
    
    
    

    /**
     * Loads microarrays data compared to a specified ref chip
     */
    private ISlideData[] loadAffyReference(File refFile, File [] files) throws IOException {
       
    	//System.out.println("I havent loaded reference");
    	if (files.length < 1) {
            return null;
        }

        BufferedReader reader = new BufferedReader(new FileReader(refFile));
        int numTokens = 0;
        StringSplitter ss = new StringSplitter((char)'\t');
        final int preSpotRows = 1; //TMEV.getHeaderRowCount();
        int countOfLines = getCountOfLines(refFile);

        // ref values
        float[] refSignals = new float[countOfLines];
        String currentLine;
        int header_row = 0;
        int index  = 0;
        while ((currentLine = reader.readLine()) != null) {
            if (header_row < preSpotRows) {
                header_row++;
                continue;
            }
            ss.init(currentLine);
            ss.passTokens(1);
            refSignals[index] = ss.nextFloatToken(0.0f);
            index++;
        }
        reader.close();

        ISlideData[] slideData = new ISlideData[files.length];
        countOfLines = 0;
        int numOfProbesets = 0;
        setFilesCount(files.length);
        for (int i = 0; i < files.length; i++) {
            if (stop) {
                return null;
            }
            setFilesProgress(i);
            setRemain(files.length-i);
            setFileName(files[i].getPath());

            if (i == 0) {
                countOfLines = getCountOfLines(files[i]);
                setLinesCount(countOfLines);
                numOfProbesets = countOfLines-1;
            }

            if (meta == null) {
                slideData[i] = loadAffySlideData(files[i]);
                meta = slideData[i].getSlideMetaData();
            }
            else {
                slideData[i] = loadAffyFloatSlideData(files[i], numOfProbesets, meta);
            }
        }

        // set ref values
        for (int i = 0; i < files.length; i++){
            for (int j = 0; j < numOfProbesets; j++){
                slideData[i].setIntensities(j, refSignals[j],  slideData[i].getCY5(j));
            }
        }
        return slideData;
    }

    // loads abs/(mean of all probesets)
    private ISlideData[] loadAffyAbsMean(File [] files) throws IOException {

       if (files.length < 1) {
           return null;
       }

       //System.out.println("loadAffyAbsMean");

       // each element is an ongoing total of the signal for that probeset
       float totalOfSignals = 0;
       float[] mean_signal = new float[files.length];

       float big_mean = 0;

       ISlideData[] slideData = new ISlideData[files.length];
       int countOfLines = 0;
       int numOfProbesets = 0;
       setFilesCount(files.length);
       for (int i = 0; i < files.length; i++) {
           if (stop) {
               return null;
           }
	   mean_signal[i] = 0;
	   totalOfSignals = 0;

           setFilesProgress(i);
           setRemain(files.length-i);
           setFileName(files[i].getPath());

           if (i == 0) {
               countOfLines = getCountOfLines(files[i]);
               setLinesCount(countOfLines);
               numOfProbesets = countOfLines-1; // one header
           }

           if (meta == null) {
               slideData[i] = loadAffySlideData(files[i]);
               meta = slideData[i].getSlideMetaData();

               for (int j = 0; j < numOfProbesets; j++){
                   totalOfSignals += slideData[i].getCY5(j);
               }


           }
           else {
               slideData[i] = loadAffyFloatSlideData(files[i], numOfProbesets, meta);//countOfLines, meta);
               for (int j = 0; j < numOfProbesets; j++){
                   totalOfSignals += slideData[i].getCY5(j);
               }
           }

	   mean_signal[i] = totalOfSignals / numOfProbesets;
	   //System.out.println("Mean " + i + ":" + mean_signal[i]);
       }

       for (int i = 0; i < mean_signal.length; i++){
	   big_mean += mean_signal[i];
       }

       big_mean = big_mean / mean_signal.length;


       // for each slidedataelement setCY5(totalofSignals[j]/numOfFiles)
       // this will screw up data displayed if files are loaded one-by-one
       // assumptions include probesets present (not MAS (P)resent, just that it exists)  in all datafiles among many others
       for (int i = 0; i < files.length; i++){
           for (int j = 0; j < numOfProbesets; j++){
               slideData[i].setIntensities(j, big_mean,  slideData[i].getCY5(j));
	   }
       }
       return slideData;
   }


    /* moved to Adjust Data per discussion with Nirmal --pcahan
    /**
     * Loads microarrays data.
     */


/*    private ISlideData[] loadAffyMean(File [] files) throws IOException {

        if (files.length < 1) {
            return null;
        }

        // each element is an ongoing total of the signal for that probeset
        float[] totalOfSignals = new float[1];

        ISlideData[] slideData = new ISlideData[files.length];
        int countOfLines = 0;
        int numOfProbesets = 0;
        setFilesCount(files.length);
        for (int i = 0; i < files.length; i++) {
            if (stop) {
                return null;
            }
            setFilesProgress(i);
            setRemain(files.length-i);
            setFileName(files[i].getPath());

            if (i == 0) {
                countOfLines = getCountOfLines(files[i]);
                setLinesCount(countOfLines);
                numOfProbesets = countOfLines-1; // one header
            }

            // a hack -- how else to tell???
            if (totalOfSignals.length < 2 ) { totalOfSignals = new float[numOfProbesets];}

            if (meta == null) {
                slideData[i] = loadAffySlideData(files[i]);
                meta = slideData[i].getSlideMetaData();

                for (int j = 0; j < numOfProbesets; j++){
                    totalOfSignals[j] = slideData[i].getCY5(j);
                }


            }
            else {
                slideData[i] = loadAffyFloatSlideData(files[i], numOfProbesets, meta);//countOfLines, meta);
                for (int j = 0; j < numOfProbesets; j++){
                    totalOfSignals[j] += slideData[i].getCY5(j);
                }
            }


        }

        // for each slidedataelement setCY5(totalofSignals[j]/numOfFiles)
        // this will screw up data displayed if files are loaded one-by-one
        // assumptions include probesets present (not MAS (P)resent, just that it exists)  in all datafiles among many others
        for (int i = 0; i < files.length; i++){
            for (int j = 0; j < numOfProbesets; j++){
                slideData[i].setIntensities(j, (float)totalOfSignals[j]/files.length,  slideData[i].getCY5(j));
            }
        }
        return slideData;
    }



    private ISlideData[] loadAffyMedian(File [] files) throws IOException {

        if (files.length < 1) {
            return null;
        }

        ISlideData[] slideData = new ISlideData[files.length];
        int countOfLines = 0;
        int numOfProbesets = 0;
        ArrayList float_array = new ArrayList(files.length);
        setFilesCount(files.length);
        for (int i = 0; i < files.length; i++) {
            if (stop) {
                return null;
            }
            setFilesProgress(i);
            setRemain(files.length-i);
            setFileName(files[i].getPath());

            if (i == 0) {
                countOfLines = getCountOfLines(files[i]);
                setLinesCount(countOfLines);
                numOfProbesets = countOfLines-1;  //header
            }

            if (meta == null) {
                slideData[i] = loadAffySlideData(files[i]);
                meta = slideData[i].getSlideMetaData();
            }
            else {
                slideData[i] = loadAffyFloatSlideData(files[i], numOfProbesets, meta);
            }
        }

        // get a more suitable data structure here  float[] median_array = new float[files.length];
        for (int i = 0; i < numOfProbesets; i++){

            // create median array
            for (int j = 0; j < files.length; j++){
                float_array.add( j, new Float(slideData[j].getCY5(i) ));
            }

            // set median values as CY3
            float median = get_Median(float_array);

            for (int j = 0; j < files.length; j++){
                slideData[j].setIntensities(i, median,
                slideData[j].getCY5(i));
            }
            float_array.clear();
        }
        return slideData;
    }
    */


    /**
     * Loads microarrays data.
     */
    private ISlideData[] loadAffyAbsolute(File [] files) throws IOException {

        if (files.length < 1) {
            return null;
        }

        ISlideData[] slideData = new ISlideData[files.length];
        int countOfLines = 0;
        int numOfProbesets = 0;
        setFilesCount(files.length);
        for (int i = 0; i < files.length; i++) {
            if (stop) {
                return null;
            }
            setFilesProgress(i);
            setRemain(files.length-i);
            setFileName(files[i].getPath());

            if (i == 0) {
                countOfLines = getCountOfLines(files[i]);
                setLinesCount(countOfLines);
                numOfProbesets = countOfLines-1;
            }


            if (meta == null) {
                slideData[i] = loadAffySlideData(files[i]);
                meta = slideData[i].getSlideMetaData();
            }
            else {
                slideData[i] = loadAffyFloatSlideData(files[i], numOfProbesets, meta);
            }
        }
        return slideData;
    }

    /**
     * Creates a SlideData object containing the data for the first experiment
     * listed in the Affy file plus metadata.
     * 
     * @param file
     * @return
     * @throws IOException
     */
    private ISlideData loadAffySlideData(final File file) throws IOException {

        AffySlideDataElement slideDataElement=null;
        String currentLine;

        if (TMEV.indicesAdjusted() == false) {
            TMEV.setUniqueIDIndex(0);
            TMEV.setNameIndex(3);
            TMEV.setIndicesAdjusted(true);
        }

        int[] rows = new int[] {0, 1, 0};
        int[] columns = new int[] {0, 1, 0};
        float cy3, cy5;
        String[] moreFields  = new String[5];
        String detection;
        final int preSpotRows = 1;

        float[] intensities = new float[2];

        int maxRows = getCountOfLines(this.files[0]);
        int maxColumns = 1;

        SlideData slideData = new SlideData(maxRows, maxColumns);

        BufferedReader reader = new BufferedReader(new FileReader(file));
        int header_row = 0;
        int curpos = 0;
        int row = 1;
        int column = 1;

        moreFields[2] = moreFields[3] = moreFields[4]  = "na";
        int numTokens = 0;

        StringSplitter ss = new StringSplitter((char)'\t');

        while ((currentLine = reader.readLine()) != null) {
            if (header_row < preSpotRows) {
                header_row++;
                continue;
            }
            setFileProgress(curpos++);
            ss.init(currentLine);

            //countTokens returns the number of delimiters, not neccessarily num of fields
            numTokens = ss.countTokens() + 1;
            moreFields = new String[numTokens];
            rows[0] = rows[2] = row++;
            columns[0] = columns[2] = column;

            // Affy_ID
            moreFields[0] = ss.nextToken();

            // Intensity
            intensities[0] = 1.0f;
            intensities[1] = ss.nextFloatToken(0.0f);

            // Detection
            detection = moreFields[1] = ss.nextToken();

            // Sometimes the genome annotation hasn't been loaded into MAS
            if (numTokens > 3 ){

              // altered to skip intervening whitespace (ie tabs)
              // MAS 5.0 and 5.1 save txt files differently.  5.1 does not have the 2 extra tabs before description
              //ss.nextToken();
              //ss.nextToken();
              int j = 2;

              while (ss.hasMoreTokens()) {

                  String temp = ss.nextToken();
                  temp = temp.trim();
                  if (temp.length() != 0){
                      moreFields[j] = temp;
                      j++;
                  }
              }
            }
              /* Uses absolute classes
               if (getMode().equals(MODE_ABSOLUTE) ) {
                   slideDataElement = new AbsoluteAffySlideDataElement(rows, columns, intensities, moreFields);
               }
               else {
                   slideDataElement = new AffySlideDataElement(rows, columns, intensities, moreFields);
               }
               */
            
            /**
             * Raktim - Annotation Demo ONly
             * Create Annotation Object for SlideDataElement here
             * Use New AffySlideDataElement constructor
             * NOTE - In other cases this MevAnnotaiton Obejct would go into SlidataElement/any derived class
             * there-off or any class that implements ISlideDataElement
             */ 
            String cloneName = moreFields[0];
            if(_tempAnno.size()!=0) {
            	
            	
            	if(((MevAnnotation)_tempAnno.get(cloneName))!=null) {
            		MevAnnotation mevAnno = (MevAnnotation)_tempAnno.get(cloneName);
            		mevAnno.setViewer(this.mav);
            		slideDataElement = new AffySlideDataElement(String.valueOf(curpos), rows, columns, intensities, moreFields, mevAnno);
            	}else {
            		MevAnnotation mevAnno = new MevAnnotation();
            		mevAnno.setCloneID(cloneName);
            		mevAnno.setViewer(this.mav);
            		slideDataElement = new AffySlideDataElement(String.valueOf(row+1), rows, columns, new float[2], moreFields, mevAnno);
            		/* String eMsg = "<html>The Probes IDs in your data <br>"+
            		 "<html>must be a subset or match all the Probe ID's<br>" +
            		 "<html>in the Annotation files. This does not seem to be the case..<br></html>";
            		 JOptionPane.showMessageDialog(null, eMsg, "ERROR", JOptionPane.ERROR_MESSAGE);
            		 */
            	}
            	
       
            
  }
            /* Added by Sarita
             * Checks if annotation was loaded and accordingly use
             * the appropriate constructor.
             * 
             * 
             */
            
           else {
            slideDataElement = new AffySlideDataElement(String.valueOf(curpos), rows, columns, intensities, moreFields);
            }
            
            
            slideDataElement.setDetection(detection);

            slideData.addSlideDataElement(slideDataElement);

        }
        reader.close();
        slideData.setSlideDataName(file.getName());
        slideData.setSlideFileName(file.getPath());
        //EH field names added to SlideData instead of TMEV.java
        slideData.setFieldNames(this.fieldNames);
        return slideData;
    }



    private ISlideData loadAffyFloatSlideData(final File file, final int countOfLines, ISlideMetaData slideMetaData) throws IOException {

        //final int coordinatePairCount = TMEV.getCoordinatePairCount()*2;
        final int intensityCount = 2; //TMEV.getIntensityCount();
        final int preSpotRows = 1;//TMEV.getHeaderRowCount();

        if (TMEV.indicesAdjusted() == false) {
            TMEV.setUniqueIDIndex(0);
            TMEV.setNameIndex(3);
            TMEV.setIndicesAdjusted(true);
        }
        FloatSlideData slideData;

               /* uses Absolute classes -- uncomment when ready
               if (getMode().equals(MODE_ABSOLUTE) ) {
                   slideData = new AbsoluteFloatSlideData(slideMetaData);
               }
               else{
                   slideData = new FloatSlideData(slideMetaData);
               }
                */

        slideData = new FloatSlideData(slideMetaData);
        BufferedReader reader = new BufferedReader(new FileReader(file), BUFFER_SIZE);
                
        String currentLine;
        StringSplitter ss = new StringSplitter((char)0x09);
        float[] intensities = new float[2];
        String detection_call;
        int header_row = 0;
        int index  = 0;
        while ((currentLine = reader.readLine()) != null) {
            if (header_row < preSpotRows) {
                header_row++;
                continue;
            }
            setFileProgress(index);
            ss.init(currentLine);
            ss.passTokens(1);
            intensities[0] = 1.0f;
            intensities[1] = ss.nextFloatToken(0.0f);

            // Detection
            detection_call = ss.nextToken();
            slideData.setIntensities(index, intensities[0], intensities[1]);
            slideData.setDetection(index, detection_call);
            index++;
        }
        reader.close();
        slideData.setSlideDataName(file.getName());
        slideData.setSlideFileName(file.getPath());
        return slideData;
    }

    public float get_Median( ArrayList float_array ) {

        Collections.sort(float_array);

        Float median;

        if (float_array.size() == 1){
            return ( (Float) float_array.get(0)).floatValue();
        }

        int center = float_array.size() / 2;

        if (float_array.size() % 2 == 0) {
            Float a, b;
            a = (Float) float_array.get(center);
            b = (Float) float_array.get(center - 1);
            median = new Float(( a.floatValue() + b.floatValue() )/2);
        }
        else {
            median = (Float)float_array.get( center );
        }
        return median.floatValue();
    }



    public FileFilter getFileFilter() {

        FileFilter affymetrixFileFilter = new FileFilter() {

            public boolean accept(File f) {
                if (f.isDirectory()) return true;
                if (f.getName().endsWith(".txt") || f.getName().endsWith(".TXT") ) return true;
                else return false;
            }

            public String getDescription() {
                return "Affymetrix Data Files (*.txt)";
            }
        };

        return affymetrixFileFilter;
    }

    public boolean checkLoadEnable() {
        setLoadEnabled(loadEnabled);
        return this.loadEnabled;
    }

    public void markLoadEnabled(boolean state) {
        loadEnabled = state;
        setLoadEnabled(loadEnabled);
        // checkLoadEnable();
    }

    public JPanel getFileLoaderPanel() {
        return aflp;
    }

    public void processFileList(String filePath, Vector fileNames) {

        aflp.setPath(filePath);

        if (fileNames == null) return; // Don't process files if there aren't any

        FileFilter affymetrixFileFilter = getFileFilter();

        aflp.getAffymetrixAvailableListModel().clear();
        aflp.getRefAvailableListModel().clear();

        for (int i = 0; i < fileNames.size(); i++) {

            File targetFile = new File((String) fileNames.elementAt(i));

            if (affymetrixFileFilter.accept(targetFile)) {
                aflp.getAffymetrixAvailableListModel().addElement(new File((String) fileNames.elementAt(i)));
                aflp.getRefAvailableListModel().addElement(new File((String) fileNames.elementAt(i)));
            }
        }
    }

    public String getFilePath() {
        if(this.aflp.getAffymetrixSelectedListModel().getSize() <1)
            return null;
        return ((File)(aflp.getAffymetrixSelectedListModel().getElementAt(0))).getAbsolutePath();
    }
    
    public void openDataPath() {
     //   this.aflp.openDataPath();
    }
    
  
    public String getAnnotationFileName() {
    	return this.annotationFileName;
    }
    
    public void setAnnotationFileName(String name) {
    	this.annotationFileName=name;
    }
    
    
/*
//
//	AffymetrixFileLoader - Internal Classes
//
 */

    private class AffymetrixFileLoaderPanel extends JPanel {

    
    	JPanel selectionPanel;
    	JPanel fileLoaderPanel;

    	
    	/**
    	 * affymetrixSelectionPanel contains three panels which are
    	 * 1. selectFilePanel
    	 * 2. affymetrixListPanel
    	 */
    	JPanel affymetrixSelectionPanel;
    	
    	/**
    	 * SelectFilePanel is a panel within the affymetrixSelectionPanel.
    	 * It allows the user to select an expression data file.  
    	 */
    	  JPanel selectFilePanel;
    	  JLabel selectFile;
    	  JButton browseButton1;
    	  JTextField pathTextField;
    	    	
    	
    	/**
    	 * affymetrixListPanel displays the available affymetrix files and the user selected 
    	 * affymetrix files for analysis.  User can select/de select  multiple files
    	 * using the Add, AddAll, Remove and RemoveAll buttons. It contains the following components
    	 * 1. affymetrixAvailbleLabel
    	 * 2. affymetrixSelectedLabel
    	 * 3. affymetrixAvailableList
    	 * 4. affymetrixSelectedList
    	 * 5. affymetrixAvailableScollPane
    	 * 6. affymetrixSelectedScrollPane
    	 * 7. affymetrixButtonPanel
    	 */
    	  JPanel affymetrixListPanel;
    	  
    	    	
    	 /**
    	  * Label for the list of available affymetrix files
    	  */
    	JLabel affymetrixAvailableLabel;
    	
    	/**
    	 * Label for the list of selected affymetrix files
    	 */
    	JLabel affymetrixSelectedLabel;
    	
    	/**
    	 * List showing the available affymetrix format files 
    	 */
    	JList affymetrixAvailableList;
    	
    	/**
    	 * List showing the selected affymetrix files
    	 */
    	JList affymetrixSelectedList;
    	
    	/**
    	 * Scrollpane containing the affymetrixAvailableList
    	 */
    	JScrollPane affymetrixAvailableScrollPane;
    	
    	/**
    	 * Scrollpane containing the affymetrixSelectedList
    	 */
    	JScrollPane affymetrixSelectedScrollPane;
    	
    	
    	/**
    	 * Panel containing the Add, AddAll, Remove and RemoveAll buttons
    	 */
    	JPanel affymetrixButtonPanel;
    	
    	JButton affymetrixAddButton;
    	JButton affymetrixAddAllButton;
    	JButton affymetrixRemoveButton;
    	JButton affymetrixRemoveAllButton;
    	


    	/**
    	 * AdditionalRequirement Panel allows user to choose whether to use reference, 
    	 * absolute or absolute/mean intensities. It provides provision for selecting
    	 * reference files.
    	 * This panel has three components
    	 * 1. refPanel
    	 * 2. buttonPanel
    	 * 3. refListPanel
    	 */
    	JPanel additionalReqPanel;
    	
    	/**
    	 * refPanel allows user to select the reference file
    	 */
    	
    	JPanel refPanel;
    	JTextField refFileListTextField;
    	JLabel selectReference;
    	JButton  browseButton2;
    	
    	
    	
    	/**
    	 * buttonPanel contains the three radio buttons namely, Absolute,
    	 * Absolute/Mean and Reference. buttonPanel lies within affymetrixSelectionPanel
    	 */
    	
    	  JPanel buttonPanel;
    	  
    	  
    	/**
    	 * Absolute, Reference and Absolute/Mean radio button
    	 */  
    	ButtonGroup optionsButtonGroup;  
    	JRadioButton absMeanRadioButton;
      	JRadioButton referenceRadioButton;
       	JRadioButton absoluteRadioButton;
    
    	
    	
    	
    	/**
    	 * refListPanel allows listing the available reference files and the user selected
    	 * reference files.  User can select/de select  multiple files
    	 * using the Add, AddAll, Remove and RemoveAll buttons. It contains the following components
    	 * 1. refAvailbleLabel
    	 * 2. refSelectedLabel
    	 * 3. refAvailableList
    	 * 4. refSelectedList
    	 * 5. refAvailableScollPane
    	 * 6. refSelectedScrollPane
    	 * 7. refButtonPanel
    	 */
    	
    	JPanel refListPanel;
    	
    	/**
    	 * Label for the list of available reference files 
    	 */
    	JLabel refAvailableLabel;
    	
    	/**
    	 * Label for the list of selected reference files
    	 */
    	JLabel refSelectedLabel;
    	
    	/**
    	 * List of available reference files
    	 */
    	JList refAvailableList;
    	
    	/**
    	 * List of selected reference files
    	 */
    	JList refSelectedList;
    	
    	/**
    	 * Scroll Pane containing the available list of reference files
    	 */
    	JScrollPane refAvailableScrollPane;
    	
    	/**
    	 * ScrollPane containing the selected list of reference files
    	 */
    	JScrollPane refSelectedScrollPane;
    	
    	/**
    	 * refButtonPanel contains the Add, AddAll, Remove and RemoveAll buttons
    	 */
    	
    	JPanel refButtonPanel;
    	JButton refAddButton;
    	JButton refAddAllButton;
    	JButton refRemoveButton;
    	JButton refRemoveAllButton;
    
    

    	/**
    	 * Annotation Panel lets user choose additional annotations from
    	 * Resourcerer. This feature is currently available only for Affymetrix files.
    	 */
    	JPanel annotationPanel;
    	JLabel getAnnotation,  customAnnotation;
    	JButton connectButton, browseButton3;
    	JTextField annFileListTextField;
    


    	public AffymetrixFileLoaderPanel() {

    		setLayout(new GridBagLayout());
    		
    		
    		selectionPanel = new JPanel();
    		selectionPanel.setLayout(new GridBagLayout());


    		affymetrixSelectionPanel = new JPanel();
    		affymetrixSelectionPanel.setLayout(new GridBagLayout());

    		affymetrixSelectionPanel.setBorder(new TitledBorder(new EtchedBorder(),
    		"File    (GW Affymetrix Format Files)"));


    		// selectFilePanel

    		selectFilePanel = new JPanel();
    		selectFilePanel.setLayout(new GridBagLayout());

    		selectFile = new JLabel("Select expression data directory");

    		browseButton1 = new JButton("Browse");
    		browseButton1.setSize(new Dimension(100, 30));
    		browseButton1.setPreferredSize(new Dimension(100, 30));
    		browseButton1.addActionListener(new EventHandler());

    		pathTextField = new JTextField();
    		pathTextField.setEditable(false);
    		pathTextField.setForeground(Color.black);
    		pathTextField.setFont(new Font("monospaced", Font.BOLD, 12));
    		
    	/*	gba.add(selectFilePanel, selectFile, 0, 0, 1, 1, 0, 0, GBA.B,GBA.C, new Insets(5, 5, 5, 5), 0, 0);
    		gba.add(selectFilePanel, pathTextField, 1, 0, 1, 1, 1, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
    		gba.add(selectFilePanel, browseButton1, 2, 0, GBA.RELATIVE, 1, 0,0, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
              */
    		
    		gba.add(selectFilePanel, selectFile, 0, 0, 1, 1, 0, 0, GBA.B,GBA.C, new Insets(2, 2, 2, 2), 0, 0);
    		gba.add(selectFilePanel, pathTextField, 1, 0, 1, 1, 1, 0, GBA.H, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
    		gba.add(selectFilePanel, browseButton1, 2, 0, GBA.RELATIVE, 1, 0,0, GBA.B, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
            
    		
    		//affymetrixListPanel
    		
    		affymetrixListPanel = new JPanel();
    		affymetrixListPanel.setLayout(new GridBagLayout());

    		
    		affymetrixAvailableLabel = new JLabel("Available");
    		affymetrixSelectedLabel = new JLabel("Selected");
    		affymetrixAvailableList = new JList(new DefaultListModel());
    		affymetrixSelectedList = new JList(new DefaultListModel());

    		affymetrixAvailableScrollPane = new JScrollPane(affymetrixAvailableList);
    		affymetrixSelectedScrollPane = new JScrollPane(affymetrixSelectedList);
    		
    		
    		
    		affymetrixButtonPanel = new JPanel();
    		affymetrixButtonPanel.setLayout(new GridBagLayout());
    		    		
    		
    		affymetrixAddButton = new JButton("Add");
    		affymetrixAddButton.setPreferredSize(new Dimension(100, 20));

    		affymetrixAddButton.addActionListener(new EventHandler());
    		affymetrixAddAllButton = new JButton("Add All");

    		affymetrixAddAllButton.setPreferredSize(new Dimension(100, 20));

    		affymetrixAddAllButton.addActionListener(new EventHandler());
    		affymetrixRemoveButton = new JButton("Remove");

    		affymetrixRemoveButton.setPreferredSize(new Dimension(100, 20));

    		affymetrixRemoveButton.addActionListener(new EventHandler());
    		affymetrixRemoveAllButton = new JButton("Remove All");

    		affymetrixRemoveAllButton.setPreferredSize(new Dimension(100, 20));

    		affymetrixRemoveAllButton.addActionListener(new EventHandler());

    		
    		this.affymetrixAddAllButton.setFocusPainted(false);
    		this.affymetrixAddButton.setFocusPainted(false);
    		this.affymetrixRemoveAllButton.setFocusPainted(false);
    		this.affymetrixRemoveButton.setFocusPainted(false);
    		
    		
    		
    		//Setting layout for affymetrixButtonPanel
    		gba.add(affymetrixButtonPanel, affymetrixAddButton, 0, 0, 1, 1, 0, 0, GBA.N,
    				GBA.C, new Insets(5, 5, 5, 5), 0, 0);
    		gba.add(affymetrixButtonPanel, affymetrixAddAllButton, 0, 1, 1, 1, 0, 0, GBA.N,
    				GBA.C, new Insets(5, 5, 5, 5), 0, 0);
    		gba.add(affymetrixButtonPanel, affymetrixRemoveButton, 0, 2, 1, 1, 0, 0, GBA.N,
    				GBA.C, new Insets(5, 5, 5, 5), 0, 0);
    		gba.add(affymetrixButtonPanel, affymetrixRemoveAllButton, 0, 3, 1, 1, 0, 0,
    				GBA.N, GBA.C, new Insets(5, 5, 5, 5), 0, 0);

    	
    		
    
    		//Setting the layout for affymetrixListPanel
    		
    	/*	gba.add(affymetrixListPanel, affymetrixAvailableLabel, 0, 0, 1, 1, 0, 0, GBA.N,
    				GBA.C, new Insets(5, 5, 5, 5), 0, 0);
    		gba.add(affymetrixListPanel, affymetrixAvailableScrollPane, 0, 1, 1, 4, 5, 1,
    				GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
    		gba.add(affymetrixListPanel, new JPanel(), 1, 0, 1, 1, 0, 0, GBA.B, GBA.C,
    				new Insets(0, 0, 0, 0), 0, 0);
    		gba.add(affymetrixListPanel, affymetrixButtonPanel, 1, 1, 1, 4, 1, 1, GBA.B,
    				GBA.C, new Insets(5, 5, 5, 5), 0, 0);
    		gba.add(affymetrixListPanel, affymetrixSelectedLabel, 2, 0, 1, 1, 0, 0, GBA.N,
    				GBA.C, new Insets(5, 5, 5, 5), 0, 0);
    		gba.add(affymetrixListPanel, affymetrixSelectedScrollPane, 2, 1, 1, 4, 5, 1,
    				GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);

    		gba.add(affymetrixSelectionPanel, selectFilePanel, 0, 0, 1, 1, 1, 1,
    				GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
    		//gba.add(affymetrixSelectionPanel, buttonPanel, 0, 1, 1, 1, 1, 1, GBA.B,
    			//	GBA.C, new Insets(5, 5, 5, 5), 0, 0);
    		gba.add(affymetrixSelectionPanel, affymetrixListPanel, 0, 2, 1, 1, 1, 1, GBA.B,
    				GBA.C, new Insets(5, 5, 5, 5), 0, 0);
*/
    		
    		gba.add(affymetrixListPanel, affymetrixAvailableLabel, 0, 0, 1, 1, 0, 0, GBA.N,
    				GBA.C, new Insets(2, 2, 2, 2), 0, 0);
    		gba.add(affymetrixListPanel, affymetrixAvailableScrollPane, 0, 1, 1, 4, 5, 1,
    				GBA.B, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
    		gba.add(affymetrixListPanel, new JPanel(), 1, 0, 1, 1, 0, 0, GBA.B, GBA.C,
    				new Insets(0, 0, 0, 0), 0, 0);
    		gba.add(affymetrixListPanel, affymetrixButtonPanel, 1, 1, 1, 4, 1, 1, GBA.B,
    				GBA.C, new Insets(2, 2, 2, 2), 0, 0);
    		gba.add(affymetrixListPanel, affymetrixSelectedLabel, 2, 0, 1, 1, 0, 0, GBA.N,
    				GBA.C, new Insets(2, 2, 2, 2), 0, 0);
    		gba.add(affymetrixListPanel, affymetrixSelectedScrollPane, 2, 1, 1, 4, 5, 1,
    				GBA.B, GBA.C, new Insets(2, 2, 2, 2), 0, 0);

    		gba.add(affymetrixSelectionPanel, selectFilePanel, 0, 0, 1, 1, 1, 1,
    				GBA.B, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
    		//gba.add(affymetrixSelectionPanel, buttonPanel, 0, 1, 1, 1, 1, 1, GBA.B,
    			//	GBA.C, new Insets(5, 5, 5, 5), 0, 0);
    		gba.add(affymetrixSelectionPanel, affymetrixListPanel, 0, 2, 1, 1, 1, 1, GBA.B,
    				GBA.C, new Insets(2, 2, 2, 2), 0, 0);

    		
    		
    		
    		// Annotation Panel
    		   		
    		    		
    		annotationPanel = new JPanel();
    		annotationPanel.setLayout(new GridBagLayout());
    		annotationPanel.setBorder(new TitledBorder(new EtchedBorder(),
    		"Annotation"));

    		getAnnotation=new JLabel("Retrieve  Annotation  from  Resourcerer");


    		connectButton = new JButton("Connect");
    		connectButton.setSize(new Dimension(100, 30));
    		connectButton.setPreferredSize(new Dimension(100, 30));
    		connectButton.addActionListener(new EventHandler());

    		
    		customAnnotation=new JLabel("Upload annotation");
    		
    		annFileListTextField=new JTextField();
    		annFileListTextField.setEditable(false);
    		annFileListTextField.setForeground(Color.black);
    		annFileListTextField.setFont(new Font("monospaced", Font.BOLD, 12));
    		
    		browseButton3 = new JButton("Browse");
    		browseButton3.setSize(new Dimension(100, 30));
    		browseButton3.setPreferredSize(new Dimension(100, 30));
    		browseButton3.addActionListener(new EventHandler());

    		

    	/*	 
    		gba.add(annotationPanel, getAnnotation, 0, 0, 2, 1, 0, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
    		//gba.add(annotationPanel, connectButton, 1, 0, 1, 0, 1, 1,GBA.NONE, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
    		gba.add(annotationPanel, connectButton, 1, 0, GBA.RELATIVE, 1, 0, 0,GBA.NONE, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
    		
    		
    		gba.add(annotationPanel, customAnnotation, 0, 1, 1, 1, 0, 0, GBA.H, GBA.C, new Insets(5,5,5,5),0,0);
    		gba.add(annotationPanel, annFileListTextField, 1, 1, 1, 0, 1, 0, GBA.H,	GBA.C, new Insets(5, 5, 5, 5), 0, 0);
    		gba.add(annotationPanel, browseButton3, 2, 1, GBA.RELATIVE, 1, 0,0, GBA.NONE, GBA.C, new Insets(5, 5, 10, 5), 0, 0);
    	*/
    		
    		
    		gba.add(annotationPanel, getAnnotation, 0, 0, 2, 1, 0, 0, GBA.H, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
    		//gba.add(annotationPanel, connectButton, 1, 0, 1, 0, 1, 1,GBA.NONE, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
    		gba.add(annotationPanel, connectButton, 1, 0, GBA.RELATIVE, 1, 0, 0,GBA.NONE, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
    		
    		
    		gba.add(annotationPanel, customAnnotation, 0, 1, 1, 1, 0, 0, GBA.H, GBA.C, new Insets(2,2,2,2),0,0);
    		gba.add(annotationPanel, annFileListTextField, 1, 1, 1, 0, 1, 0, GBA.H,	GBA.C, new Insets(2, 2, 2, 2), 0, 0);
    		gba.add(annotationPanel, browseButton3, 2, 1, GBA.RELATIVE, 1, 0,0, GBA.NONE, GBA.C, new Insets(2, 2, 10, 2), 0, 0);
    	

    	//Additional Requirement Panel	
    		additionalReqPanel = new JPanel();
    		additionalReqPanel.setLayout(new GridBagLayout());
    		additionalReqPanel.setBorder(new TitledBorder(new EtchedBorder(),
    		"Additional Requirements"));
        		
    		
    		refPanel = new JPanel();
    		refPanel.setLayout(new GridBagLayout());

    		refFileListTextField = new JTextField();
    		refFileListTextField.setEditable(false);
    		refFileListTextField.setForeground(Color.black);
    		refFileListTextField.setFont(new Font("monospaced", Font.BOLD, 12));


    		browseButton2 = new JButton("Browse");
    		browseButton2.addActionListener(new EventHandler());
    		browseButton2.setSize(new Dimension(100, 30));
    		browseButton2.setPreferredSize(new Dimension(100, 30));

    		selectReference = new JLabel("Select Reference data directory");

    		
    	/*	gba.add(refPanel, selectReference, 0, 0, 1, 1, 0, 0, GBA.B,GBA.C, new Insets(5, 5, 5, 5), 0, 0);
    		gba.add(refPanel, refFileListTextField, 1, 0, 1, 1, 1, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
    		gba.add(refPanel, browseButton2, 2, 0, GBA.RELATIVE, 1, 0,0, GBA.NONE, GBA.C, new Insets(5, 5, 5, 5), 0, 0);

    	*/
    		
    		gba.add(refPanel, selectReference, 0, 0, 1, 1, 0, 0, GBA.B,GBA.C, new Insets(2, 2, 2, 2), 0, 0);
    		gba.add(refPanel, refFileListTextField, 1, 0, 1, 1, 1, 0, GBA.H, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
    		gba.add(refPanel, browseButton2, 2, 0, GBA.RELATIVE, 1, 0,0, GBA.NONE, GBA.C, new Insets(2, 2, 2, 2), 0, 0);

    		
    		refButtonPanel = new JPanel();
    		refButtonPanel.setLayout(new GridBagLayout());
    		
    		refAddButton = new JButton("Add");
    		refAddButton.addActionListener(new EventHandler());
    		refAddAllButton = new JButton("Add All");
    		refAddAllButton.addActionListener(new EventHandler());
    		refRemoveButton = new JButton("Remove");
    		refRemoveButton.addActionListener(new EventHandler());
    		refRemoveAllButton = new JButton("Remove All");
    		refRemoveAllButton.addActionListener(new EventHandler());

    		Dimension buttonSize = new Dimension(100, 20);

    		Dimension largestAnnButtonSize = refRemoveAllButton
    		.getPreferredSize();
    		refAddButton.setPreferredSize(buttonSize);
    		refAddAllButton.setPreferredSize(buttonSize);
    		refRemoveButton.setPreferredSize(buttonSize);
    		refRemoveAllButton.setPreferredSize(buttonSize);
	

    		this.refAddAllButton.setFocusPainted(false);
    		this.refAddButton.setFocusPainted(false);
    		this.refRemoveAllButton.setFocusPainted(false);
    		this.refRemoveButton.setFocusPainted(false);

    		

    	/*	gba.add(refButtonPanel, refAddButton, 0, 0, 1, 1, 0, 0, GBA.N,
    				GBA.C, new Insets(5, 5, 5, 5), 0, 0);
    		gba.add(refButtonPanel, refAddAllButton, 0, 1, 1, 1, 0, 0, GBA.N,
    				GBA.C, new Insets(5, 5, 5, 5), 0, 0);
    		gba.add(refButtonPanel, refRemoveButton, 0, 2, 1, 1, 0, 0, GBA.N,
    				GBA.C, new Insets(5, 5, 5, 5), 0, 0);
    		gba.add(refButtonPanel, refRemoveAllButton, 0, 3, 1, 1, 0, 0,
    				GBA.N, GBA.C, new Insets(5, 5, 5, 5), 0, 0);*/
    		
    		gba.add(refButtonPanel, refAddButton, 0, 0, 1, 1, 0, 0, GBA.N,
    				GBA.C, new Insets(2, 2, 2, 2), 0, 0);
    		gba.add(refButtonPanel, refAddAllButton, 0, 1, 1, 1, 0, 0, GBA.N,
    				GBA.C, new Insets(2, 2, 2, 2), 0, 0);
    		gba.add(refButtonPanel, refRemoveButton, 0, 2, 1, 1, 0, 0, GBA.N,
    				GBA.C, new Insets(2, 2, 2, 2), 0, 0);
    		gba.add(refButtonPanel, refRemoveAllButton, 0, 3, 1, 1, 0, 0,
    				GBA.N, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
    		
    		
    		refListPanel = new JPanel();
    		refListPanel.setLayout(new GridBagLayout());

    		
    		refAvailableLabel = new JLabel("Available Reference Files");
    		refSelectedLabel = new JLabel("Selected Reference Files");
    		refAvailableList = new JList(new DefaultListModel());
    		refSelectedList=new JList(new DefaultListModel());

    		refAvailableScrollPane = new JScrollPane(refAvailableList);
    		refSelectedScrollPane = new JScrollPane(refSelectedList);
    		
    	/*	gba.add(refListPanel, refAvailableLabel, 0, 0, 1, 1, 0, 0, GBA.N,
    				GBA.C, new Insets(5, 5, 5, 5), 0, 0);
    		gba.add(refListPanel, refSelectedLabel, 2, 0, 1, 1, 0, 0, GBA.N,
    				GBA.C, new Insets(5, 5, 5, 5), 0, 0);
    		gba.add(refListPanel, refAvailableScrollPane, 0, 1, 1, 4, 5, 1,
    				GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
    		gba.add(refListPanel, new JPanel(), 1, 0, 1, 1, 0, 0, GBA.B, GBA.C,
    				new Insets(0, 0, 0, 0), 0, 0);
    		gba.add(refListPanel, refButtonPanel, 1, 1, 1, 4, 1, 1, GBA.B,
    				GBA.C, new Insets(5, 5, 5, 5), 0, 0);
    		gba.add(refListPanel, refSelectedScrollPane, 2, 1, 1, 4, 5, 1,
    				GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);*/

    		gba.add(refListPanel, refAvailableLabel, 0, 0, 1, 1, 0, 0, GBA.N,
    				GBA.C, new Insets(2, 2, 2, 2), 0, 0);
    		gba.add(refListPanel, refSelectedLabel, 2, 0, 1, 1, 0, 0, GBA.N,
    				GBA.C, new Insets(2, 2, 2, 2), 0, 0);
    		gba.add(refListPanel, refAvailableScrollPane, 0, 1, 1, 4, 5, 1,
    				GBA.B, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
    		gba.add(refListPanel, new JPanel(), 1, 0, 1, 1, 0, 0, GBA.B, GBA.C,
    				new Insets(0, 0, 0, 0), 0, 0);
    		gba.add(refListPanel, refButtonPanel, 1, 1, 1, 4, 1, 1, GBA.B,
    				GBA.C, new Insets(2, 2, 2, 2), 0, 0);
    		gba.add(refListPanel, refSelectedScrollPane, 2, 1, 1, 4, 5, 1,
    				GBA.B, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
    		

    		//Button Panel	
    		buttonPanel = new JPanel();
    		buttonPanel.setLayout(new GridBagLayout());
    		
    		
    		// Medians vs. Integrate intensities
    		absoluteRadioButton = new JRadioButton("Absolute",true);
    		absoluteRadioButton.setFocusPainted(false);

    		absMeanRadioButton = new JRadioButton("Absolute/Mean Intensity");
    		absMeanRadioButton.setFocusPainted(false);

    		referenceRadioButton = new JRadioButton("Reference (Select reference files below)");
    		referenceRadioButton.setFocusPainted(false);
    		
    		

    		ButtonGroup bg = new ButtonGroup();
    		bg.add(absoluteRadioButton);
    		bg.add(absMeanRadioButton);
    		bg.add(referenceRadioButton);
    		
    	
    		
    	/*	gba.add(buttonPanel, absoluteRadioButton, 0, 0, 1, 1, 1, 0, GBA.H, GBA.C, new Insets(5, 50, 0, 5), 0, 0);
    		gba.add(buttonPanel, referenceRadioButton, 0, 1, 1, 1, 1, 0, GBA.H, GBA.C, new Insets(5, 50, 0, 5), 0, 0);
    		gba.add(buttonPanel, absMeanRadioButton, 1, 0, 1, 1, 1, 0, GBA.H, GBA.C, new Insets(5, 5, 0, 5), 0, 0);


    		//Setting layout for AdditionalRequirement panel
    		gba.add(additionalReqPanel, buttonPanel, 0, 0, 1, 1, 1, 1, GBA.B,
    				GBA.C, new Insets(5, 5, 5, 5), 0, 0);
    		gba.add(additionalReqPanel, refPanel, 0, 2, 1, 1, 1, 1,
    				GBA.B, GBA.C, new Insets(5, 0, 5, 0), 0, 0);
    		gba.add(additionalReqPanel, refListPanel, 0, 3, 1, 2, 1, 1, GBA.B,
    				GBA.C, new Insets(5, 5, 0, 5), 0, 0);

    	
    	   		
    		
    		gba.add(selectionPanel, affymetrixSelectionPanel, 0, 1, 2, 2, 1, 1, GBA.B,
    				GBA.C, new Insets(5, 5, 5, 5), 0, 0);
    		gba.add(selectionPanel, annotationPanel, 0, 3, 2, 3, 1, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
    		gba.add(selectionPanel, additionalReqPanel, 0, 6, 2, 2, 1, 1, GBA.B,
    				GBA.C, new Insets(5, 5, 5, 5), 0, 0);//commented temporarily by sarita

    		
    		
    		fileLoaderPanel = new JPanel();
    		fileLoaderPanel.setLayout(new GridBagLayout());

    		gba.add(fileLoaderPanel, selectionPanel, 0, 0, 1, 1, 1, 1, GBA.B,
    				GBA.C, new Insets(5, 5, 5, 5), 0, 0);//commented temporarily by sarita
    	
    		 gba.add(this, fileLoaderPanel, 0, 0, 1, 1, 1, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
			*/
    		
    		gba.add(buttonPanel, absoluteRadioButton, 0, 0, 1, 1, 1, 0, GBA.H, GBA.C, new Insets(2, 50, 0, 2), 0, 0);
    		gba.add(buttonPanel, referenceRadioButton, 0, 1, 1, 1, 1, 0, GBA.H, GBA.C, new Insets(2, 50, 0, 2), 0, 0);
    		gba.add(buttonPanel, absMeanRadioButton, 1, 0, 1, 1, 1, 0, GBA.H, GBA.C, new Insets(2, 2, 0, 2), 0, 0);


    		//Setting layout for AdditionalRequirement panel
    		gba.add(additionalReqPanel, buttonPanel, 0, 0, 1, 1, 1, 1, GBA.B,
    				GBA.C, new Insets(2, 2, 2, 2), 0, 0);
    		gba.add(additionalReqPanel, refPanel, 0, 2, 1, 1, 1, 1,
    				GBA.B, GBA.C, new Insets(2, 0, 2, 0), 0, 0);
    		gba.add(additionalReqPanel, refListPanel, 0, 3, 1, 2, 1, 1, GBA.B,
    				GBA.C, new Insets(2, 2, 0, 2), 0, 0);

    	
    	   		
    		
    		gba.add(selectionPanel, affymetrixSelectionPanel, 0, 1, 2, 2, 1, 1, GBA.B,
    				GBA.C, new Insets(2, 2, 2, 2), 0, 0);
    		gba.add(selectionPanel, annotationPanel, 0, 3, 2, 3, 1, 1, GBA.B, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
    		gba.add(selectionPanel, additionalReqPanel, 0, 6, 2, 2, 1, 1, GBA.B,
    				GBA.C, new Insets(2, 2, 2, 2), 0, 0);//commented temporarily by sarita

    		
    		
    		fileLoaderPanel = new JPanel();
    		fileLoaderPanel.setLayout(new GridBagLayout());

    		gba.add(fileLoaderPanel, selectionPanel, 0, 0, 1, 1, 1, 1, GBA.B,
    				GBA.C, new Insets(2, 2, 2, 2), 0, 0);
    	
    		 gba.add(this, fileLoaderPanel, 0, 0, 1, 1, 1, 1, GBA.B, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
    		
    		

        }
    	
    	 
    	
    	
        public void setPath(String path) {
            pathTextField.setText(path);
        }
        
        public void openDataPath(){
        //   this.fileTreePane.openDataPath();
        }
        
        
        public void onAnnotationFileBrowse() {
     	FileLoaderUtility fileLoad = new FileLoaderUtility();
        	File selectedFile;
        	JFileChooser fileChooser = new JFileChooser(
        			SuperExpressionFileLoader.DATA_PATH);
        	fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        	int retVal = fileChooser.showOpenDialog(AffymetrixFileLoaderPanel.this);

        	if (retVal == JFileChooser.APPROVE_OPTION) {
        		
        		selectedFile = fileChooser.getSelectedFile();
        		//setAnnotationFile(selectedFile);
        		setAnnotationFileName(selectedFile.getAbsolutePath());
        		annFileListTextField.setText(selectedFile.getAbsolutePath());
        		mav.getData().setAnnotationLoaded(true);
        		
        	}
			
        }
        
        
        
        public void onConnect() {
        	AnnotationDialog annDialog=new AnnotationDialog(new JFrame());
        	if(annDialog.showModal()==JOptionPane.OK_OPTION) {
            	setAnnotationFileName(annDialog.getAnnotationFileName());
            	mav.getData().setAnnotationLoaded(true);
            	}else {
            		mav.getData().setAnnotationLoaded(false);
            	}
        	
        
            } 
        
  
        
        
        public void onDataFileBrowse() {
        	FileLoaderUtility fileLoad = new FileLoaderUtility();
        	Vector retrievedFileNames = new Vector();
        	JFileChooser fileChooser = new JFileChooser(
        			SuperExpressionFileLoader.DATA_PATH);
        	fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        	int retVal = fileChooser.showOpenDialog(AffymetrixFileLoaderPanel.this);

        	if (retVal == JFileChooser.APPROVE_OPTION) {
        		((DefaultListModel) affymetrixAvailableList.getModel()).clear();
        		((DefaultListModel) affymetrixSelectedList.getModel()).clear();

        		File selectedFile = fileChooser.getSelectedFile();
        		String path = selectedFile.getAbsolutePath();
        		pathTextField.setText(path);
        		retrievedFileNames = fileLoad.getFileNameList(selectedFile
        				.getAbsolutePath());

        		for (int i = 0; i < retrievedFileNames.size(); i++) {
        			Object fileName = retrievedFileNames.get(i);
        			boolean acceptFile = getFileFilter()
        			.accept((File) fileName);

        			if (acceptFile) {
        				String Name=fileChooser.getName((File) fileName);
        				
        				
        				/*Object addItem = fileName;
        				((DefaultListModel) affymetrixAvailableList.getModel())
        				.addElement(addItem);*/
        				
        				((DefaultListModel) affymetrixAvailableList.getModel())
        				.addElement(new File(Name));
        				
        			}
        		}

        	}


        }
        
        
        public void onRefFileBrowse() {
        	FileLoaderUtility fileLoad = new FileLoaderUtility();
        	Vector retrievedFileNames = new Vector();
        	JFileChooser fileChooser = new JFileChooser(
        			SuperExpressionFileLoader.DATA_PATH);
        	fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        	int retVal = fileChooser.showOpenDialog(AffymetrixFileLoaderPanel.this);

        	if (retVal == JFileChooser.APPROVE_OPTION) {
        		((DefaultListModel) refAvailableList.getModel()).clear();
        		((DefaultListModel) refSelectedList.getModel()).clear();


        		File selectedFile = fileChooser.getSelectedFile();
        		String path = selectedFile.getAbsolutePath();
        		refFileListTextField.setText(path);
        		retrievedFileNames = fileLoad.getFileNameList(selectedFile
        				.getAbsolutePath());

        		for (int i = 0; i < retrievedFileNames.size(); i++) {
        			Object fileName = retrievedFileNames.get(i);
        			boolean acceptFile = getFileFilter()
        			.accept((File) fileName);

        			if (acceptFile) {
        				String Name=fileChooser.getName((File) fileName);
        				
        				/*Object addItem = fileName;
        				((DefaultListModel) refAvailableList.getModel())
        				.addElement(addItem);*/
        				((DefaultListModel) refAvailableList.getModel())
        				.addElement(new File(Name));

        			}
        		}

        	}
			
        }
        
        
        
        public void validateLists() {

        	// Currently, a minimum of one Affymetrix file must be selected to enable absoluteRadiong.
        	// If the reference option is selected, a minimum of one Affymetrix file must also
        	// be chosen as a reference.

        	if (((DefaultListModel) affymetrixSelectedList.getModel()).size() > 0) {
        		if (referenceRadioButton.isSelected()) {
        			if (((DefaultListModel) refSelectedList.getModel()).size() > 0) {
        				markLoadEnabled(true);
        			} else {
        				markLoadEnabled(false);
        			}
        		} else {
        			markLoadEnabled(true);
        		}
        	} else {
        		markLoadEnabled(false);
        	}
        }

        
        public void onAffymetrixAdd() {
        	int[] chosenIndices = affymetrixAvailableList.getSelectedIndices();
        	Object[] chosenObjects = new Object[chosenIndices.length];

        	for (int i = chosenIndices.length - 1; i >= 0; i--) {
        		// For remove-then-add functionality
        		//Object addItem = ((DefaultListModel) affymetrixAvailableList.getModel()).remove(chosenIndices[i]);
        		// For copy-then-add functionality
        		Object addItem = ((DefaultListModel) affymetrixAvailableList.getModel()).getElementAt(chosenIndices[i]);
        		chosenObjects[i] = addItem;
        	}

        	for (int i = 0; i < chosenIndices.length; i++) {
        		((DefaultListModel) affymetrixSelectedList.getModel()).addElement(chosenObjects[i]);
        	}

        	validateLists();
        }

        public void onAffymetrixAddAll() {
        	int elementCount = ((DefaultListModel) affymetrixAvailableList.getModel()).size();
        	for (int i = 0; i < elementCount; i++) {
        		Object addItem = ((DefaultListModel) affymetrixAvailableList.getModel()).getElementAt(i);
        		((DefaultListModel) affymetrixSelectedList.getModel()).addElement(addItem);
        	}

        	validateLists();
        }

        public void onAffymetrixRemove() {
        	int[] chosenIndices = affymetrixSelectedList.getSelectedIndices();

        	// Designed with copy-then-add functionality in mind
        	for (int i = chosenIndices.length - 1; i >= 0; i--) {
        		((DefaultListModel) affymetrixSelectedList.getModel()).remove(chosenIndices[i]);
        	}

        	validateLists();
        }

        public void onAffymetrixRemoveAll() {
        	// Designed with copy-then-add functionality in mind
        	((DefaultListModel) affymetrixSelectedList.getModel()).removeAllElements();

        	validateLists();
        }

        public void onRefAdd() {
        	int[] chosenIndices = refAvailableList.getSelectedIndices();
        	Object[] chosenObjects = new Object[chosenIndices.length];

        	for (int i = chosenIndices.length - 1; i >= 0; i--) {
        		// For remove-then-add functionality
        		//Object addItem = ((DefaultListModel) refAvailableList.getModel()).remove(chosenIndices[i]);
        		// For copy-then-add functionality
        		Object addItem = ((DefaultListModel) refAvailableList.getModel()).getElementAt(chosenIndices[i]);
        		chosenObjects[i] = addItem;
        	}

        	for (int i = 0; i < chosenIndices.length; i++) {
        		((DefaultListModel) refSelectedList.getModel()).addElement(chosenObjects[i]);
        	}

        	validateLists();
        }

        public void onRefAddAll() {
        	int elementCount = ((DefaultListModel) refAvailableList.getModel()).size();
        	for (int i = 0; i < elementCount; i++) {
        		Object addItem = ((DefaultListModel) refAvailableList.getModel()).getElementAt(i);
        		((DefaultListModel) refSelectedList.getModel()).addElement(addItem);
        	}

        	validateLists();
        }

        public void onRefRemove() {
        	int[] chosenIndices = refSelectedList.getSelectedIndices();

        	// Designed with copy-then-add functionality in mind
        	for (int i = chosenIndices.length - 1; i >= 0; i--) {
        		((DefaultListModel) refSelectedList.getModel()).remove(chosenIndices[i]);
        	}

        	validateLists();
        }

        public void onRefRemoveAll() {
        	// Designed with copy-then-add functionality in mind
        	((DefaultListModel) refSelectedList.getModel()).removeAllElements();

        	validateLists();
        }

        public DefaultListModel getAffymetrixAvailableListModel() {
        	return (DefaultListModel) affymetrixAvailableList.getModel();
        }

        public DefaultListModel getRefAvailableListModel() {
        	return (DefaultListModel) refAvailableList.getModel();
        }

        public DefaultListModel getAffymetrixSelectedListModel() {
        	return (DefaultListModel) affymetrixSelectedList.getModel();
        }

        public DefaultListModel getRefSelectedListModel() {
        	return (DefaultListModel) refSelectedList.getModel();
        }

   /*     private class ListRenderer extends DefaultListCellRenderer {
        	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        		super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        		File file = (File) value;
        		setText(file.getName());
        		return this;
        	}
        }*/

        private class EventHandler implements ActionListener {
        	public void actionPerformed(ActionEvent event) {

        		Object source = event.getSource();

        		if (source == affymetrixAddButton) {
        			onAffymetrixAdd();
        		} else if (source == affymetrixAddAllButton) {
        			onAffymetrixAddAll();
        		} else if (source == affymetrixRemoveButton) {
        			onAffymetrixRemove();
        		} else if (source == affymetrixRemoveAllButton) {
        			onAffymetrixRemoveAll();
        		} else if (source == refAddButton) {
        			onRefAdd();
        		} else if (source == refAddAllButton) {
        			onRefAddAll();
        		} else if (source == refRemoveButton) {
        			onRefRemove();
        		} else if (source == refRemoveAllButton) {
        			onRefRemoveAll();
        		} else if (source instanceof JRadioButton){
        			aflp.validateLists();  //check state
        		}
        		else if (source==browseButton1){
        			onDataFileBrowse();  
        		}
        		else if (source==browseButton2){
        			onRefFileBrowse();  
        		}
        		else if (source==browseButton3){
        			onAnnotationFileBrowse();  
        		}
        		else if (source==connectButton){
        			onConnect();  
        		}
        		   
        		       		
        		


        	}
        }

    
    }
}
