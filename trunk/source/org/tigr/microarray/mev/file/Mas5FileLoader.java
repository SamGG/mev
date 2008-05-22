    /*
Copyright @ 1999-2005, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * $RCSfile: Mas5FileLoader.java,v $
 * $Revision: 1.6 $
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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
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
import org.tigr.microarray.mev.annotation.AnnotationDialog;
import org.tigr.microarray.mev.annotation.AnnotationFileReader;
import org.tigr.microarray.mev.annotation.MevAnnotation;
import org.tigr.microarray.mev.annotation.PublicURL;
import org.tigr.microarray.util.FileLoaderUtility;


public class Mas5FileLoader extends ExpressionFileLoader {
    
    private GBA gba;
    private boolean stop = false;
    private Mas5FileLoaderPanel sflp;
    private int affyDataType = TMEV.DATA_TYPE_AFFY;
    /**
     * Annotation Specific
     * Place Holder for reading in Affy Anno 
     * MAV needed to pass on the ref to MevAnnotation Obj for MAV Index
     **/
    private Hashtable _tempAnno=new Hashtable();
    private MultipleArrayViewer mav;
    private File selectedAnnoFile;
    protected MevAnnotation mevAnno=new MevAnnotation();
    private String annotationFileName;
    
   
    
    
    
    
    public Mas5FileLoader(SuperExpressionFileLoader superLoader) {
        super(superLoader);
        this.mav = superLoader.getArrayViewer();
        gba = new GBA();
        sflp = new Mas5FileLoaderPanel();
    }
    
    public Vector loadExpressionFiles() throws IOException {
    	
        return loadMas5ExpressionFile(new File(this.sflp.selectedFiles.getText()),this.sflp.selectedCallFileTextField.getText());
    }
    
    public ISlideData loadExpressionFile(File f){
        return null;
    }
    
   
    /**
     *  Handling of Mas5 data has been altered in version 3.0 to permit loading of
     *  "ratio" input without the creation of false cy3 and cy5.  cy5 values in data structures
     *  are used to hold the input value.
     *
     *  getRatio methods are altered to return the value (held in cy5) rather than
     *  taking log2(cy5/cy3).
     */
    
   /*by wwang 
    * set datatype =DATA_TYPE_AFFY
    */ 
    
    public void setTMEVDataType(){
        TMEV.setDataType(TMEV.DATA_TYPE_AFFY);
    }
    
    public int getAffyDataType(){
        return this.affyDataType;
    }
    
    /**
     * 
     * Loads Affy Annotation from a File
     */

    private Hashtable loadAffyAnno(File affyFile) {
    	Hashtable _temp = null;
    	//AnnotationFileReader reader = new AnnotationFileReader();
    	AnnotationFileReader reader = new AnnotationFileReader(this.mav);
    	try {
    		_temp = reader.loadAffyAnnotation(affyFile);


    	} catch (Exception e) {
    		// TODO Auto-generated catch block
    		e.printStackTrace();
    	}
    	return _temp;
    }
    
    
    public Vector loadMas5ExpressionFile(File f,String callfile) throws IOException {
        
        final int preSpotRows = this.sflp.getXRow()+1;
        final int preExperimentColumns = this.sflp.getXColumn();
        this.setTMEVDataType();
        int numLines = this.getCountOfLines(f);
        
        int spotCount = numLines - preSpotRows;

        if (spotCount <= 0) {
            JOptionPane.showMessageDialog(superLoader.getFrame(),  "There is no spot data available.",  "TDMS Load Error", JOptionPane.INFORMATION_MESSAGE);
        }
        
        int[] rows = new int[] {0, 1, 0};
        int[] columns = new int[] {0, 1, 0};
        String value;
        float cy3, cy5;
        String[] moreFields = new String[preExperimentColumns];
        
        final int rColumns = 1;
        final int rRows = spotCount;
        float[] intensities = new float[2];
        
        
        ISlideData[] slideDataArray = null;
        AffySlideDataElement sde=null;
                      
        BufferedReader reader = new BufferedReader(new FileReader(f));
        StringSplitter ss = new StringSplitter((char)0x09);
        String currentLine;
        int counter, row, column;
        counter = 0;
        row = column = 1;
        this.setFilesCount(1);
        this.setRemain(1);
        this.setFilesProgress(0);
        this.setLinesCount(numLines);
        this.setFileProgress(0);
        
        
        /*Loop added by Sarita to check if Annotation has been loaded
         * The loop was included so as to enable loading data
         * irrespective of whether annotation was loaded or not
         * 
         */
        if(this.mav.getData().isAnnotationLoaded()) {
        	_tempAnno = loadAffyAnno(new File(getAnnotationFileName()));
        	//this.mav.getData().setAnnotationLoaded(true);
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

      
        
        while ((currentLine = reader.readLine()) != null) {
            if (stop) {
                return null;
            }
            ss.init(currentLine);
            if (counter == 0) { // parse header
                int experimentCount = ss.countTokens()+2- preExperimentColumns;
                slideDataArray = new ISlideData[experimentCount];
                slideDataArray[0] = new SlideData(rRows, rColumns);
                slideDataArray[0].setSlideFileName(f.getPath());
                for (int i=1; i<slideDataArray.length; i++) {
                    slideDataArray[i] = new FloatSlideData(slideDataArray[0].getSlideMetaData(), spotCount);
                    slideDataArray[i].setSlideFileName(f.getPath());
                }
                //get Field Names and add one for List 
                String [] fieldNames = new String[preExperimentColumns+1];
                fieldNames[0]="ChipID";
                
                for(int i = 1; i < preExperimentColumns; i++){
                    fieldNames[i] = ss.nextToken();
                }               
                fieldNames[preExperimentColumns]=callfile;           
                slideDataArray[0].getSlideMetaData().appendFieldNames(fieldNames);
     
                for (int i=0; i<experimentCount; i++) {
                    slideDataArray[i].setSlideDataName(ss.nextToken());
                }
            } else if (counter >= preSpotRows) { // data rows
                rows[0] = rows[2] = row;
                columns[0] = columns[2] = column;
                if (column == rColumns) {
                    column = 1;
                    row++;
                } else {
                    column++;
                }
                for (int i=0; i<preExperimentColumns; i++) {
                    moreFields[i] = ss.nextToken();
                }
                
                String cloneName = moreFields[0];
                if(_tempAnno.size()!=0) {
             	   
             	           	   
                	if(((MevAnnotation)_tempAnno.get(cloneName))!=null) {
                		MevAnnotation mevAnno = (MevAnnotation)_tempAnno.get(cloneName);

                		sde = new AffySlideDataElement(String.valueOf(row+1), rows, columns, intensities, moreFields, mevAnno);
                	} else {
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
                		mevAnno.setViewer(this.mav);
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
                 
             
                slideDataArray[0].addSlideDataElement(sde);
                
                
                
                
                
                for (int i=0; i<slideDataArray.length; i++) {
                    
                    cy3 = 1f;  //set cy3 to a default value of 1.
                    
                    try {
                        value = ss.nextToken();
                        cy5 = Float.parseFloat(value);  //set cy5 to hold the value
                        //getRatio methods will return cy5
                        //for Mas5 data type
                    } catch (Exception e) {
                        cy3 = 0;
                        cy5 = Float.NaN;
                    }
                    slideDataArray[i].setIntensities(counter - preSpotRows, cy3, cy5);
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
        }
        reader.close();
        
        Vector data = new Vector(slideDataArray.length);
        
        for(int i = 0; i < slideDataArray.length; i++)
            data.add(slideDataArray[i]);
        
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
                return "Mas5 Files(*.txt)";
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
    
    public void loadCallFile(File targetFile) {
    	 sflp.setCallFileName(targetFile.getAbsolutePath());	
    }
    
    
    public void processMas5File(File targetFile) {
        
        Vector columnHeaders = new Vector();
        Vector dataVector = new Vector();
        Vector rowVector = null;
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
            ss.init(currentLine);
            columnHeaders.add("ChipID\t");
            for (int i = 0; i < ss.countTokens()+1; i++) {
                columnHeaders.add(ss.nextToken());
            }
            model.setColumnIdentifiers(columnHeaders);
            int cnt = 0;
            while ((currentLine = reader.readLine()) != null && cnt < 100) {
                cnt++;
                ss.init(currentLine);
                rowVector = new Vector();
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
    }
    
    
    public String getFilePath() {
       // return this.sflp.pathTextField.getText();
    	return this.sflp.fileNameTextField.getText();
    }
    
    public void openDataPath() {
      //  this.sflp.openDataPath();
    }
    
    public String getAnnotationFileName() {
    	return this.annotationFileName;
    }
    
    public void setAnnotationFileName(String name) {
    	this.annotationFileName=name;
    }
    
    
    
    
    
/*
//
//	Mas5FileLoader - Internal Classes
//
*/
    
    private class Mas5FileLoaderPanel extends JPanel {
    	
        
    	JTextField pathTextField;
        JPanel pathPanel;
        
        JTable expressionTable;
        JLabel instructionsLabel;
        JScrollPane tableScrollPane;
        JPanel tablePanel;
        
        JPanel mas5ListPanel;
        JList mas5AvailableList;
        JScrollPane mas5AvailableScrollPane;
        
        JPanel refListPanel;
        JList refAvailableList;
        JScrollPane refAvailableScrollPane;
        
        JTextField refTextField;
        JPanel refPanel;
        
        JTextField annoTextField;
        JPanel annoPanel;
        
        JPanel fileLoaderPanel;
        JSplitPane splitPane;
        
        //Added by Sarita
        JTextField fileNameTextField, callFileNameTextField,  selectedCallFileTextField;
        JPanel  additionalRequirements, fileSelectionPanel;
        JLabel selectedFileLabel, selectedCallFile,dataSelection, callFileSelection;
        JButton browseButton1;
        JButton browseButton3;
        JComboBox fileTypeList;
        JTextField selectedFiles;
        
    	/**
    	 * Annotation Panel lets user choose additional annotations from
    	 * Resourcerer. This feature is currently available only for Affymetrix files.
    	 */
    	JPanel annotationPanel;
    	JLabel getAnnotation,  customAnnotation;
    	JButton connectButton, browseButton2;
    	JTextField annFileListTextField;
        
        
        protected EventListener eventListener;
  
 
        private int xRow = -1;
        private int xColumn = -1;

	

		private JTextField annFileNameTextField;
        
        
        public Mas5FileLoaderPanel() {                
        	eventListener = new EventListener();
            setLayout(new GridBagLayout());
            
       
            
            fileNameTextField = new JTextField();
            fileNameTextField.setEditable(false);
            fileNameTextField.setForeground(Color.black);
            fileNameTextField.setFont(new Font("monospaced", Font.BOLD, 12));
         
     
            
 //Added by Sarita    
            
            selectedFiles = new JTextField();
            selectedFiles.setEditable(false);
            selectedFiles.setForeground(Color.black);
            selectedFiles.setFont(new Font("monospaced", Font.BOLD, 12));
         
            
            selectedFileLabel=new JLabel();
            selectedFileLabel.setForeground(java.awt.Color.BLACK);
            String fileTypeChoices = "<html> Selected files </html>";
            selectedFileLabel.setText(fileTypeChoices);

            
            dataSelection=new JLabel();
            dataSelection.setForeground(java.awt.Color.BLACK);
            String chooseFile="<html>Select expression data file</html>";
            dataSelection.setText(chooseFile);
           
            
            
            fileSelectionPanel = new JPanel();
            fileSelectionPanel.setLayout(new GridBagLayout());
            fileSelectionPanel.setBorder(new TitledBorder(new EtchedBorder(), "File    (Mas5 Format Files)"));
            
            browseButton1=new JButton("Browse");
            browseButton1.addActionListener(eventListener);
           	browseButton1.setSize(100, 30);
    		browseButton1.setPreferredSize(new Dimension(100, 30));
    		
    		
    		
    		gba.add(fileSelectionPanel, dataSelection, 0, 0, 1, 1, 1, 0, GBA.B,GBA.C, new Insets(5, 5, 5, 5), 0, 0);
    		gba.add(fileSelectionPanel, fileNameTextField, 1, 0, 1, 1, 0, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
    		gba.add(fileSelectionPanel, browseButton1, 2, 0, GBA.RELATIVE, 1, 0,0, GBA.NONE, GBA.C, new Insets(5, 5, 5, 5), 0, 0);

    		gba.add(fileSelectionPanel, selectedFileLabel, 0, 2, 2, 1, 0, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
            gba.add(fileSelectionPanel, selectedFiles, 1, 2, 1, 1, 2, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0); 
              

            annotationPanel = new JPanel();
    		annotationPanel.setLayout(new GridBagLayout());
    		annotationPanel.setBorder(new TitledBorder(new EtchedBorder(),
    		"Annotation"));

    		getAnnotation=new JLabel("Retrieve  Annotation  from  Resourcerer");


    		connectButton = new JButton("Connect");
    		connectButton.setSize(new Dimension(100, 30));
    		connectButton.setPreferredSize(new Dimension(100, 30));
    		connectButton.addActionListener(new EventListener());

    		
    		customAnnotation=new JLabel("Upload annotation");
    		
    		annFileListTextField=new JTextField();
    		annFileListTextField.setEditable(false);
    		annFileListTextField.setForeground(Color.black);
    		annFileListTextField.setFont(new Font("monospaced", Font.BOLD, 12));
    		
    		browseButton2 = new JButton("Browse");
    		browseButton2.setSize(new Dimension(100, 30));
    		browseButton2.setPreferredSize(new Dimension(100, 30));
    		browseButton2.addActionListener(new EventListener());

    		

    		 
    		gba.add(annotationPanel, getAnnotation, 0, 0, 2, 1, 0, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
    		//gba.add(annotationPanel, connectButton, 1, 0, 1, 0, 1, 1,GBA.NONE, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
    		gba.add(annotationPanel, connectButton, 1, 0, GBA.RELATIVE, 1, 0, 0,GBA.NONE, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
    		
    		
    		gba.add(annotationPanel, customAnnotation, 0, 1, 1, 1, 0, 0, GBA.H, GBA.C, new Insets(5,5,5,5),0,0);
    		gba.add(annotationPanel, annFileListTextField, 1, 1, 1, 0, 1, 0, GBA.H,	GBA.C, new Insets(5, 5, 5, 5), 0, 0);
    		gba.add(annotationPanel, browseButton2, 2, 1, GBA.RELATIVE, 1, 0,0, GBA.NONE, GBA.C, new Insets(5, 5, 10, 5), 0, 0);
    	

        	
             
          /*  gba.add(annotationPanel, customAnnotation, 0, 0, 1, 1, 0, 0, GBA.B,GBA.C, new Insets(5, 5, 5, 5), 0, 0);
     		gba.add(annotationPanel, annFileNameTextField, 1, 0, 1, 1, 1, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
     		gba.add(annotationPanel, browseButton2, 2, 0, GBA.RELATIVE, 1, 0,0, GBA.NONE, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
			*/
        	
        	
        	additionalRequirements=new JPanel();
        	additionalRequirements.setLayout(new GridBagLayout());
        	additionalRequirements.setBorder(new TitledBorder(new EtchedBorder(), "Additional Requirements"));
        	

        	callFileSelection=new JLabel();
        	callFileSelection.setForeground(java.awt.Color.BLACK);
        	String callFileName = "<html> Select Call file </html>";
        	callFileSelection.setText(callFileName);


        	callFileNameTextField = new JTextField();
        	callFileNameTextField.setEditable(false);
        	callFileNameTextField.setForeground(Color.black);
        	callFileNameTextField.setFont(new Font("monospaced", Font.BOLD, 12));

        	browseButton3=new JButton("Browse");
        	browseButton3.addActionListener(eventListener);
        	
        	browseButton3.setSize(100, 30);
        	browseButton3.setPreferredSize(new Dimension(100, 30));

        	
        
        	selectedCallFile=new JLabel();
        	selectedCallFile.setForeground(java.awt.Color.BLACK);
        	String selectedCallFileName = "<html> Selected Call file </html>";
        	selectedCallFile.setText(selectedCallFileName);

        	selectedCallFileTextField = new JTextField();
        	selectedCallFileTextField.setEditable(false);
        	selectedCallFileTextField.setForeground(Color.black);
        	selectedCallFileTextField.setFont(new Font("monospaced", Font.BOLD, 12));



        	
        	gba.add(additionalRequirements, callFileSelection, 0, 0, 1, 1, 1, 0, GBA.B,GBA.C, new Insets(5, 5, 5, 5), 0, 0);
    		gba.add(additionalRequirements, callFileNameTextField, 1, 0, 1, 1, 0, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
    		gba.add(additionalRequirements, browseButton3, 2, 0, GBA.RELATIVE, 1, 0,0, GBA.NONE, GBA.C, new Insets(5, 5, 5, 5), 0, 0);

    		gba.add(additionalRequirements, selectedCallFile, 0, 2, 2, 1, 0, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
            gba.add(additionalRequirements, selectedCallFileTextField, 1, 2, 1, 1, 2, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0); 
              
       


        	expressionTable = new JTable();
        	expressionTable.setCellSelectionEnabled(true);
        	expressionTable.setColumnSelectionAllowed(false);
        	expressionTable.setRowSelectionAllowed(false);
        	expressionTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        	expressionTable.getTableHeader().setReorderingAllowed(false);

        	expressionTable.addMouseListener(new MouseAdapter() {
        		public void mousePressed(MouseEvent event) {
        			xRow = expressionTable.rowAtPoint(event.getPoint());
        			xColumn = expressionTable.columnAtPoint(event.getPoint());
        			checkLoadEnable();
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

        	gba.add(tablePanel, tableScrollPane, 0, 0, 1, 2, 1, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
        	gba.add(tablePanel, instructionsLabel, 0, 2, 1, 1, 1, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);



        	//gba.add(fileLoaderPanel, filePanel, 0, 0, 1, 4, 1, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
        	fileLoaderPanel = new JPanel();
        	fileLoaderPanel.setLayout(new GridBagLayout());


        	gba.add(fileLoaderPanel, fileSelectionPanel, 1, 0, 1, 1, 3, 0, GBA.H, GBA.C, new Insets(5, 5, 5,5), 0, 0);
        	gba.add(fileLoaderPanel, annotationPanel, 1, 1, 1, 1, 3, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
        	gba.add(fileLoaderPanel, additionalRequirements, 1, 2, 1, 1, 3, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
        	gba.add(fileLoaderPanel, tablePanel, 1, 3, 1, 2, 3, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);



        	gba.add(this,fileLoaderPanel,0,0,1,1,1,1,GBA.B,GBA.C, new Insets(5, 5, 5, 5), 0, 0);

        }


        public void onAnnotationFileBrowse() {
         	FileLoaderUtility fileLoad = new FileLoaderUtility();
            	File selectedFile;
            	JFileChooser fileChooser = new JFileChooser(
            			SuperExpressionFileLoader.DATA_PATH);
            	fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            	int retVal = fileChooser.showOpenDialog(Mas5FileLoaderPanel.this);

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
        	JFileChooser fileChooser=new JFileChooser(SuperExpressionFileLoader.DATA_PATH);
        	int retVal=fileChooser.showOpenDialog(Mas5FileLoaderPanel.this);
        	
        	if(retVal==JFileChooser.APPROVE_OPTION) {
        	File selectedFile=fileChooser.getSelectedFile();
        	processMas5File(selectedFile);
        	}
           		
    	}
        
        public void onCallFileBrowse() {
        	JFileChooser fileChooser=new JFileChooser(SuperExpressionFileLoader.DATA_PATH);
        	int retVal=fileChooser.showOpenDialog(Mas5FileLoaderPanel.this);
        	
        	if(retVal==JFileChooser.APPROVE_OPTION) {
        	File selectedFile=fileChooser.getSelectedFile();
        	callFileNameTextField.setText(selectedFile.getAbsolutePath());
        	selectedCallFileTextField.setText(selectedFile.getAbsolutePath());
        	loadCallFile(selectedFile);
       
        	}
           		
    	}
        
        public void openDataPath() {
          //  fileTreePane.openDataPath();
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
        
        public void selectMas5File() {
            JFileChooser jfc = new JFileChooser(SuperExpressionFileLoader.DATA_PATH);
            jfc.setFileFilter(getFileFilter());
            int activityCode = jfc.showDialog(this, "Select");
            
            if (activityCode == JFileChooser.APPROVE_OPTION) {
                File target = jfc.getSelectedFile();
                processMas5File(target);
            }
        }
        
        public void setDataFileName(String fileName) {
           fileNameTextField.setText(fileName);
           selectedFiles.setText(fileName);
        }
        public void setCallFileName(String fileName) {
        	callFileNameTextField.setText(fileName);
           selectedCallFileTextField.setText(fileName);
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
      //      annoTextField.setText(fieldsText);
        }
        
        private class EventListener implements ActionListener {
    		public void actionPerformed(ActionEvent event) {
    			Object source = event.getSource();
    			if (source == browseButton1) {
    				onDataFileBrowse();
    			} if (source == browseButton2) {
    				onAnnotationFileBrowse();
    			} if (source == browseButton3) {
    				onCallFileBrowse();
    			} if (source==connectButton){
        			onConnect();  
        		}
    	    			
    			}
    		}
    	
       
        
        
        private class ListListener implements javax.swing.event.ListSelectionListener {
            
            public void valueChanged(ListSelectionEvent lse) {
            	
            }
        }
        
     
        
        private class FileTreePaneEventHandler implements FileTreePaneListener {
            
            public void nodeSelected(FileTreePaneEvent event) {
                
               
            }
            
            public void nodeCollapsed(FileTreePaneEvent event) {}
            public void nodeExpanded(FileTreePaneEvent event) {}
        }
    }  
    }

