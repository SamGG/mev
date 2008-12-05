
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
 * $Author: eleanorahowe $
 * $State: Exp $
 */

package org.tigr.microarray.mev.file;

import java.awt.Color;
import java.awt.Component;
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
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.tigr.microarray.mev.AffySlideDataElement;
import org.tigr.microarray.mev.FloatSlideData;
import org.tigr.microarray.mev.ISlideData;
import org.tigr.microarray.mev.MultipleArrayData;
import org.tigr.microarray.mev.MultipleArrayViewer;
import org.tigr.microarray.mev.SlideData;
import org.tigr.microarray.mev.TMEV;
import org.tigr.microarray.mev.annotation.AffymetrixAnnotationParser;
import org.tigr.microarray.mev.annotation.AnnotationDialog;
import org.tigr.microarray.mev.annotation.AnnotationFileReader;
import org.tigr.microarray.mev.annotation.IChipAnnotation;
import org.tigr.microarray.mev.annotation.MevAnnotation;
import org.tigr.microarray.mev.annotation.PublicURL;
import org.tigr.microarray.mev.cluster.gui.IData;


import org.tigr.microarray.util.ExpressionFileTableCellRenderer;
import org.tigr.microarray.util.FileLoaderUtility;
import org.tigr.microarray.util.ExpressionFileTableCellRenderer;


public class AffyGCOSFileLoader extends ExpressionFileLoader {
    
    private GBA gba;
    private boolean stop = false;
    private AffyGCOSFileLoaderPanel sflp;
    ExpressionFileTableCellRenderer myCellRenderer;
    private int affyDataType = IData.DATA_TYPE_AFFY_ABS;
    
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

	
    public AffyGCOSFileLoader(SuperExpressionFileLoader superLoader) {
    	
    	super(superLoader);
    	this.mav = superLoader.getArrayViewer();
        gba = new GBA();
        sflp = new AffyGCOSFileLoaderPanel();
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

        return loadAffyGCOSExpressionFile(new File(this.sflp.selectedFiles.getText()));
    }
    
    public ISlideData loadExpressionFile(File f){
        return null;
    }

    public void setTMEVDataType(){
         TMEV.setDataType(TMEV.DATA_TYPE_AFFY);
     }
          
    /*
     *  Handling of Mas5 data has been altered in version 3.0 to permit loading of
     *  "ratio" input without the creation of false cy3 and cy5.  cy5 values in data structures
     *  are used to hold the input value.
     *
     *  getRatio methods are altered to return the value (held in cy5) rather than
     *  taking log2(cy5/cy3).
     */
    
    public Vector loadAffyGCOSExpressionFile(File f) throws IOException {
    	this.setTMEVDataType();
        final int preSpotRows = this.sflp.getXRow()+1;
        final int preExperimentColumns = this.sflp.getXColumn();
        int numLines = this.getCountOfLines(f);
        int spotCount = numLines - preSpotRows;

        if (spotCount <= 0) {
            JOptionPane.showMessageDialog(superLoader.getFrame(),  "There is no spot data available.",  "TDMS Load Error", JOptionPane.INFORMATION_MESSAGE);
        }
        
        int[] rows = new int[] {0, 1, 0};
        int[] columns = new int[] {0, 1, 0};
        //String value,pvalue;
         String detection;

        float cy3, cy5;

        String[] moreFields = new String[1];
        String[] extraFields=null;
        final int rColumns = 1;
        final int rRows = spotCount;
        
        ISlideData slideDataArray[]=null;
        AffySlideDataElement sde=null;
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
            	
            	if(sflp.onlyIntensityRadioButton.isSelected()) 
            		experimentCount = ss.countTokens()- preExperimentColumns;
            		
            	if(sflp.intensityWithDetectionRadioButton.isSelected()) 
            		experimentCount = (ss.countTokens()+1- preExperimentColumns)/2;
            		
            	if(sflp.intensityWithDetectionPvalRadioButton.isSelected()) 
            		experimentCount = (ss.countTokens()+1- preExperimentColumns)/3;
            	
            	
            	slideDataArray = new ISlideData[experimentCount];
                slideDataArray[0] = new SlideData(rRows, rColumns);
                slideDataArray[0].setSlideFileName(f.getPath());
                for (int i=1; i<experimentCount; i++) {
                	slideDataArray[i] = new FloatSlideData(slideDataArray[0].getSlideMetaData(),spotCount);
                	slideDataArray[i].setSlideFileName(f.getPath());
                	//System.out.println("slideDataArray[i].slide file name: "+ f.getPath());
                }
                if(sflp.onlyIntensityRadioButton.isSelected()){
                	String [] fieldNames = new String[1];
                	//extraFields = new String[1];
                	fieldNames[0]="AffyID";
                	slideDataArray[0].getSlideMetaData().appendFieldNames(fieldNames);
                }else if(sflp.intensityWithDetectionRadioButton.isSelected()){
                	String [] fieldNames = new String[2];
                	extraFields = new String[1];
                    fieldNames[0]="AffyID";
                    fieldNames[1]="Detection";
                    slideDataArray[0].getSlideMetaData().appendFieldNames(fieldNames);
                }else{
                	String [] fieldNames = new String[3];
                	extraFields = new String[2];
                    fieldNames[0]="AffyID";
                    fieldNames[1]="Detection";
                    fieldNames[2]="P-value";
                    slideDataArray[0].getSlideMetaData().appendFieldNames(fieldNames);
                   
                }
                ss.nextToken();//parse the blank on header
                for (int i=0; i<experimentCount; i++) {
                	slideDataArray[i].setSlideDataName(ss.nextToken());//commented by sarita
                	 
                    if(sflp.intensityWithDetectionPvalRadioButton.isSelected()){
                    	ss.nextToken();//parse the detection
                        ss.nextToken();//parse the pvalue
                     }else if(sflp.intensityWithDetectionRadioButton.isSelected()){
                        	ss.nextToken();//parse the detection  
                     }            
                }
                
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
               if(_tempAnno.size()!=0) {
            	   
            	           	   
            	   if(((MevAnnotation)_tempAnno.get(cloneName))!=null) {
            		   MevAnnotation mevAnno = (MevAnnotation)_tempAnno.get(cloneName);

            		   sde = new AffySlideDataElement(String.valueOf(row+1), rows, columns, intensities, moreFields, mevAnno);
            	   }else {
            	 /**
            	  * Sarita: clone ID explicitly set here because if the data file
            	  * has a probe (for eg. Affy house keeping probes) for which Resourcerer
            	  * does not have annotation, MeV would still work fine. NA will be
            	  * appended for the rest of the fields. 
            	  * 
            	  * 
            	  */
            		MevAnnotation mevAnno = new MevAnnotation();
            		mevAnno.setCloneID(cloneName);
                    sde = new AffySlideDataElement(String.valueOf(row+1), rows, columns, new float[2], moreFields, mevAnno);
            	   		 
               }
               }
                /* Added by Sarita
                 * Checks if annotation was loaded and accordingly use
                 * the appropriate constructor.
                 * 
                 * 
                 */
                
               else {
                sde = new AffySlideDataElement(String.valueOf(row+1), rows, columns, intensities, moreFields);
                }
                
            	
            	//sde = new AffySlideDataElement(String.valueOf(row+1), rows, columns, new float[2], moreFields);

            	slideDataArray[0].addSlideDataElement(sde);
            	int i=0;

            	for ( i=0; i<slideDataArray.length; i++) {                   
            		try {	

            			// Intensity
            			intensities[0] = 1.0f;
            			intensities[1] = ss.nextFloatToken(0.0f);
            		
            			if(sflp.intensityWithDetectionPvalRadioButton.isSelected()){
            				
            				extraFields[0]=ss.nextToken();//detection
            				extraFields[1]=ss.nextToken();//p-value
            				
            			}else if(sflp.intensityWithDetectionRadioButton.isSelected()){
            				extraFields[0]=ss.nextToken();//detection
            			}

            		} catch (Exception e) {
            			
            			intensities[1] = Float.NaN;
            		}
            		if(i==0){
            			
            			slideDataArray[i].setIntensities(counter - preSpotRows, intensities[0], intensities[1]);
            			//sde.setExtraFields(extraFields);
            			if(sflp.intensityWithDetectionPvalRadioButton.isSelected()){
            				sde.setDetection(extraFields[0]);
            				sde.setPvalue(new Float(extraFields[1]).floatValue());
            			}else if(sflp.intensityWithDetectionRadioButton.isSelected()){
            				sde.setDetection(extraFields[0]);
            			}
            		}else{
            			if(i==1){
            				meta = slideDataArray[0].getSlideMetaData();                    	
            			}
            			slideDataArray[i].setIntensities(counter-preSpotRows,intensities[0],intensities[1]);
            			if(sflp.intensityWithDetectionPvalRadioButton.isSelected()){
            				((FloatSlideData)slideDataArray[i]).setDetection(counter-preSpotRows,extraFields[0]);
            				((FloatSlideData)slideDataArray[i]).setPvalue(counter-preSpotRows,new Float(extraFields[1]).floatValue());
            			}
            			if(sflp.intensityWithDetectionRadioButton.isSelected()){
            				((FloatSlideData)slideDataArray[i]).setDetection(counter-preSpotRows,extraFields[0]);
            			}
            		}
            	}

            } else {
                //we have additional sample annoation
                
                //advance to sample key
            		for(int i = 0; i < preExperimentColumns-1; i++) {
            			ss.nextToken();
            		}
            		String key = ss.nextToken();
            		
                
            		for(int j = 0; j < slideDataArray.length; j++) {
            			slideDataArray[j].addNewSampleLabel(key, ss.nextToken());
            		}
            	}
            	
            this.setFileProgress(counter);
           	counter++;
           	//System.out.print(counter);
        	}
        reader.close();
        
       
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
                return "AffyGCOS Files(*.txt)";
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
        return this.affyDataType;
    }

	@Override
	public String getAnnotationFilePath() {
		return this.sflp.getAnnFilePath();
	}  
	public boolean isAnnotationSelected() {
		return sflp.isAnnotationSelected();
	}
   
    public void processAffyGCOSFile(File targetFile) {
        
        Vector<String> columnHeaders = new Vector<String>();
        Vector<Vector<String>> dataVector = new Vector<Vector<String>>();
        Vector<String> rowVector = null;
        BufferedReader reader = null;
        String currentLine = null;
        
        if (! validateFile(targetFile)) return;
        sflp.setDataFileName(targetFile.getAbsolutePath());
        DefaultTableModel model = new DefaultTableModel() {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
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
                columnHeaders.add(ss.nextToken());
            }
            //for (int i = 0; i < columnHeaders.size(); i++) {
            //    System.out.print(columnHeaders.get(i));
           // }
            model.setColumnIdentifiers(columnHeaders);
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
        sflp.setSelectedCell(p.x, p.y);
    }
    
    
    public String getFilePath() {
        return this.sflp.selectedFiles.getText();
    }
    
    
    public void openDataPath() {
        this.sflp.openDataPath();
    }
    
 
/*
//
//	AffyGCOSFileLoader - Internal Classes
//
 */
    
    private class AffyGCOSFileLoaderPanel extends JPanel {
    	
        
    	JPanel fileSelectionPanel;
        JTable expressionTable;
        JLabel instructionsLabel;
        JScrollPane tableScrollPane;
        JPanel tablePanel;
        
        JPanel additionalRequirementPanel;
       
        

        JTextField fileNameTextField;
        JTextField selectedFiles;
        //

        ButtonGroup optionsButtonGroup;
        JRadioButton onlyIntensityRadioButton;
        JRadioButton intensityWithDetectionRadioButton;
        JRadioButton intensityWithDetectionPvalRadioButton;
        
        JPanel  annotationPanel, mainPanel;
//        JLabel getAnnotation,  customAnnotation;
        JLabel fileSelectionLabel, dataSelection;
        
    	
//    	JButton connectButton, browseButton2;
    	
//    	JTextField annFileListTextField;
    
    	        
        JButton browseButton1;
        protected EventListener eventListener;
        JPanel fileLoaderPanel;
       
        private int xRow = -1;
        private int xColumn = -1;
        
        AnnotationDownloadHandler adh;
        
        public AffyGCOSFileLoaderPanel() {                
                setLayout(new GridBagLayout());
                
                adh = new AnnotationDownloadHandler(superLoader.viewer.getResourceManager(), superLoader.annotationLists, superLoader.defaultSpeciesName, superLoader.defaultArrayName);
                
                eventListener=new EventListener();
                fileNameTextField = new JTextField();
                fileNameTextField.setEditable(false);
                fileNameTextField.setForeground(Color.black);
                fileNameTextField.setFont(new Font("monospaced", Font.BOLD, 12));
                
                selectedFiles = new JTextField();
                selectedFiles.setEditable(false);
                selectedFiles.setForeground(Color.black);
                selectedFiles.setFont(new Font("monospaced", Font.BOLD, 12));
             
                
                fileSelectionLabel=new JLabel();
                fileSelectionLabel.setForeground(java.awt.Color.BLACK);
                String fileTypeChoices = "<html> Selected files </html>";
                fileSelectionLabel.setText(fileTypeChoices);

                
                dataSelection=new JLabel();
                dataSelection.setForeground(java.awt.Color.BLACK);
                String chooseFile="<html>Select expression data file</html>";
                dataSelection.setText(chooseFile);
               
                               
                browseButton1=new JButton("Browse");
                browseButton1.addActionListener(eventListener);
               	browseButton1.setSize(100, 30);
        		browseButton1.setPreferredSize(new Dimension(100, 30));
        		
 
                
                fileSelectionPanel = new JPanel();
                fileSelectionPanel.setLayout(new GridBagLayout());
                fileSelectionPanel.setBorder(new TitledBorder(new EtchedBorder(), "File    (Affy GCOS Format Files)"));
           
                gba.add(fileSelectionPanel, dataSelection, 0, 0, 1, 1, 0, 0, GBA.B,GBA.C, new Insets(2, 2, 2, 2), 0, 0);
        		gba.add(fileSelectionPanel, fileNameTextField, 1, 0, 1, 1, 1, 0, GBA.H, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
        		gba.add(fileSelectionPanel, browseButton1, 2, 0, GBA.RELATIVE, 1, 0,0, GBA.NONE, GBA.C, new Insets(2, 2, 2, 2), 0, 0);

        		gba.add(fileSelectionPanel, fileSelectionLabel, 0, 2, 2, 1, 0, 0, GBA.H, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
                gba.add(fileSelectionPanel, selectedFiles, 1, 2, 1, 1, 2, 0, GBA.H, GBA.C, new Insets(2, 2, 2, 2), 0, 0); 

                
                
                annotationPanel = adh.getAnnotationLoaderPanel(gba);
                adh.setDownloadEnabled(true);
                
        		additionalRequirementPanel = new JPanel();
                additionalRequirementPanel.setLayout(new GridBagLayout());
                additionalRequirementPanel.setBorder(new TitledBorder(new EtchedBorder(), "Additional Requirements"));
                optionsButtonGroup = new ButtonGroup();
                onlyIntensityRadioButton = new JRadioButton("Only Intensity", true);
                //absoluteRadioButton.addActionListener(new EventHandler());
                optionsButtonGroup.add(onlyIntensityRadioButton);

                intensityWithDetectionRadioButton = new JRadioButton("Intensity With Detection");
                //absMeanRadioButton.addActionListener(new EventHandler());
                optionsButtonGroup.add(intensityWithDetectionRadioButton);

                intensityWithDetectionPvalRadioButton = new JRadioButton("Intensity with Detection and P-value");
                optionsButtonGroup.add(intensityWithDetectionPvalRadioButton);
                
                gba.add(additionalRequirementPanel, onlyIntensityRadioButton, 0, 0, 1, 1, 1, 0, GBA.H, GBA.C, new Insets(2, 2, 0, 2), 0, 0);
                gba.add(additionalRequirementPanel, intensityWithDetectionPvalRadioButton, 1, 0, 1, 1, 1, 0, GBA.H, GBA.C, new Insets(2, 2, 0, 2), 0, 0);
                gba.add(additionalRequirementPanel, intensityWithDetectionRadioButton, 0, 1, 1, 1, 1, 0, GBA.H, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
                
                
                expressionTable = new JTable();
        		myCellRenderer = new ExpressionFileTableCellRenderer();
                expressionTable.setDefaultRenderer(Object.class, myCellRenderer);
    			expressionTable.setGridColor(Color.LIGHT_GRAY);
                expressionTable.setCellSelectionEnabled(true);
                expressionTable.setColumnSelectionAllowed(false);
                expressionTable.setRowSelectionAllowed(false);
                expressionTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
                expressionTable.getTableHeader().setReorderingAllowed(false);
                
             
                expressionTable.addMouseListener(new MouseAdapter() {
                    public void mousePressed(MouseEvent event) {
                        setSelectedCell(expressionTable.rowAtPoint(event.getPoint()), expressionTable.columnAtPoint(event.getPoint()));
              
                    }
                });
                
                
                tableScrollPane = new JScrollPane(expressionTable, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
                
                instructionsLabel = new JLabel();
                instructionsLabel.setForeground(java.awt.Color.red);
                String instructions = "<html>Click the upper-leftmost expression value. Click the <b>Load</b> button to finish.</html>";
                instructionsLabel.setText(instructions);
                
                tablePanel = new JPanel();
                tablePanel.setLayout(new GridBagLayout());
                tablePanel.setBorder(new TitledBorder(new EtchedBorder(), "Expression Table"));
                 
                gba.add(tablePanel, tableScrollPane, 0, 0, 1, 2, 1, 1, GBA.B, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
                gba.add(tablePanel, instructionsLabel, 0, 2, 1, 1, 1, 0, GBA.H, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
                
               
                fileLoaderPanel = new JPanel();
                fileLoaderPanel.setLayout(new GridBagLayout());
                
                gba.add(fileLoaderPanel,fileSelectionPanel, 			0, 0, 1, 1, 1, 1, GBA.B, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
                gba.add(fileLoaderPanel, annotationPanel, 				0, 2, 1, 1, 1, 1, GBA.B, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
                gba.add(fileLoaderPanel, additionalRequirementPanel, 	0, 4, 1, 2, 3, 0, GBA.H, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
                gba.add(fileLoaderPanel, tablePanel, 					0, 7, 1, 6, 3, 6, GBA.B, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
                
                gba.add(this, fileLoaderPanel,0, 0, 1, 1, 1, 1, GBA.B, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
              
                
                
                
                
                
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
        
        
        

        
        public void onBrowse() {
        	JFileChooser fileChooser=new JFileChooser(SuperExpressionFileLoader.DATA_PATH);
        	int retVal=fileChooser.showOpenDialog(AffyGCOSFileLoaderPanel.this);
        	
        	if(retVal==JFileChooser.APPROVE_OPTION) {
        	File selectedFile=fileChooser.getSelectedFile();
        	fileNameTextField.setText(selectedFile.getAbsolutePath());
        	processAffyGCOSFile(selectedFile);
        	}
           		
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
            selectedFiles.setText(fileName);
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
        
        private class EventListener implements ActionListener {
    		public void actionPerformed(ActionEvent event) {
    			Object source = event.getSource();
    			if (source == browseButton1) {
    				onBrowse();
    			} 
    		}
    	}
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

}

