/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/**
 * @author Sarita Nair
 * @Revision Date Jun 18, 2007
 * 
 * The RMA File Loader  
 * 
 * 
 * 
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

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;

import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.tigr.microarray.mev.AffySlideDataElement;
import org.tigr.microarray.mev.FloatSlideData;
import org.tigr.microarray.mev.MultipleArrayData;
import org.tigr.microarray.mev.MultipleArrayViewer;

import org.tigr.microarray.mev.ISlideData;
import org.tigr.microarray.mev.SlideData;
import org.tigr.microarray.mev.SlideDataElement;
import org.tigr.microarray.mev.TMEV;
import org.tigr.microarray.mev.annotation.AnnotationDialog;
import org.tigr.microarray.mev.annotation.AnnotationFileReader;
import org.tigr.microarray.mev.annotation.IChipAnnotation;
import org.tigr.microarray.mev.annotation.MevAnnotation;
import org.tigr.microarray.mev.annotation.PublicURL;
import org.tigr.microarray.util.FileLoaderUtility;
import org.tigr.microarray.util.MyCellRenderer;


public class RMAFileLoader extends ExpressionFileLoader {
    private GBA gba;
    private boolean stop = false;
    private RMAFileLoaderPanel sflp;
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
    
    public RMAFileLoader(SuperExpressionFileLoader superLoader) {
        super(superLoader);
        this.mav = superLoader.getArrayViewer();
        gba = new GBA();
        sflp = new RMAFileLoaderPanel();
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
        if(this.mav.getData().isAnnotationLoaded()) {
//        	_tempAnno = loadAffyAnno(new File(getAnnotationFileName()));

        	//EH testing chip annotation change
        	AnnotationFileReader afr = AnnotationFileReader.createAnnotationFileReader(new File(getAnnotationFileName()));
        	_tempAnno = afr.getAffyAnnotation();
        	chipAnno = afr.getAffyChipAnnotation();  
        }
        
     
        /**
         * TODO
         * Raktim - Annotation Demo Only. 
         * Good Place to initialize URLS.
         */
        if(PublicURL.loadURLs(TMEV.getConfigurationFile("annotation_URLs.txt")) != 0){
        	JOptionPane.showMessageDialog(new JFrame(), "URLs will not be loaded", "Warning", JOptionPane.WARNING_MESSAGE);
        }
        
        return loadRMAExpressionFile(new File(this.sflp.fileNameTextField.getText()));
    }
    
    public ISlideData loadExpressionFile(File f){
        return null;
    }
  
    /*
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

*/

    

    /**
     *  Handling of RMA data has been altered in version 3.0 to permit loading of
     *  "ratio" input without the creation of false cy3 and cy5.  cy5 values in data structures
     *  are used to hold the input value.
     *  getRatio methods are altered to return the value (held in cy5) rather than
     *  taking log2(cy5/cy3).
     */
    
    public Vector loadRMAExpressionFile(File f) throws IOException {

    	final int preSpotRows = this.sflp.getXRow()+1;
    	final int preExperimentColumns = this.sflp.getXColumn();
    	int numLines = this.getCountOfLines(f);
    	int spotCount = numLines - preSpotRows;

    	int[] rows = new int[] {0, 1, 0};
    	int[] columns = new int[] {0, 1, 0};
    	String value;
    	float cy3, cy5;
    	String[] moreFields = new String[preExperimentColumns];
    	final int rColumns = 1;
    	final int rRows = spotCount;

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

    	if (spotCount <= 0) {
    		JOptionPane.showMessageDialog(superLoader.getFrame(),  "There is no spot data available.",  "RMA Load Error", JOptionPane.INFORMATION_MESSAGE);
    	}

    	while ((currentLine = reader.readLine()) != null) {
    		if (stop) {
    			return null;

    		}

    		//fix empty tabbs appending to the end of line by wwang

    		while(currentLine.endsWith("\t")){
    			currentLine=currentLine.substring(0,currentLine.length()-1);
    		}

    		ss.init(currentLine);

    		if (counter == 0) { // parse header
    			int experimentCount = ss.countTokens()+1 - preExperimentColumns;
    			slideDataArray = new ISlideData[experimentCount];
    			slideDataArray[0] = new SlideData(rRows, rColumns);
    			slideDataArray[0].setSlideFileName(f.getPath());

    			for (int i=1; i<slideDataArray.length; i++) {
    				slideDataArray[i] = new FloatSlideData(slideDataArray[0].getSlideMetaData(), spotCount);
    				slideDataArray[i].setSlideFileName(f.getPath());

    			}

    			//get Field Names

    			String [] fieldNames = new String[preExperimentColumns];
    			for(int i = 0; i < preExperimentColumns; i++){
    				fieldNames[i] = ss.nextToken();

    			}

    			//EH field names are saved in SlideData rather than TMEV

    			slideDataArray[0].getSlideMetaData().setFieldNames(fieldNames);

    			//TMEV.setFieldNames(fieldNames);

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

                		 sde = new AffySlideDataElement(String.valueOf(row+1), rows, columns, new float[2], moreFields, mevAnno);
                	 } else {

                		 MevAnnotation mevAnno = new MevAnnotation();
                		 mevAnno.setCloneID(cloneName);
// EH testing chip annotation changes                		 
//                		 mevAnno.setViewer(this.mav);
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
                  sde = new AffySlideDataElement(String.valueOf(row+1), rows, columns, new float[2], moreFields);
                  }
 
    			slideDataArray[0].addSlideDataElement(sde);



    			for (int i=0; i<slideDataArray.length; i++) {
    				cy3 = 1f;  //set cy3 to a default value of 1.
    				try {

    					value = ss.nextToken();
    					cy5 = Float.parseFloat(value);  //set cy5 to hold the value

    					//getRatio methods will return cy5
    					//for RMA data type

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
                if (f.getName().endsWith(".txt")||f.getName().endsWith(".rma")||f.getName().endsWith(".RMA"))

                	return true;
                else return false;
            }
            

            public String getDescription() {
                return "RMA(*.txt)";

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
        return true; // For now, no validation on RMA Files

    }

    

    public JPanel getFileLoaderPanel() {
        return sflp;

    }

    

    public void processRMAFile(File targetFile) {

        Vector columnHeaders = new Vector();
        Vector dataVector = new Vector();
        Vector rowVector = null;
        BufferedReader reader = null;
        String currentLine = null;

        if (! validateFile(targetFile)) return;
        sflp.setFileName(targetFile.getAbsolutePath());
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

			//fix empty tabbs appending to the end of line by wwang

            while(currentLine.endsWith("\t")){
            	currentLine=currentLine.substring(0,currentLine.length()-1);

            }

            ss.init(currentLine);

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
        return this.sflp.fileNameTextField.getText();

    }

    

    public void openDataPath() {

       // this.sflp.openDataPath();

    }
    public String getAnnotationFileName() {
    	return this.annotationFileName;
    }
    
    public void setAnnotationFileName(String name) {
    	this.annotationFileName=name;
    }

/***
 * 
 * 
 * 
 * @author Sarita Nair
 * RMAFileLoaderPanel--The GUI has been re-written as a part of
 * the adding the new Annotation Model to MeV
 *
 */

    
    private class RMAFileLoaderPanel extends JPanel {

    	JPanel fileLoaderPanel;
    	
    	
    	/**
    	 * fileSelectionPanel is a panel within the fileLoaderPanel.
    	 * It allows the user to select an expression data file.  
    	 */
    	JPanel fileSelectionPanel;
    	JLabel fileSelectionLabel, dataSelection;
    	JTextField fileNameTextField;
    	JTextField selectedFiles;
    	JButton browseButton1;
       
    	JPanel tablePanel;
    	JTable expressionTable;
        JLabel instructionsLabel;
        JScrollPane tableScrollPane;
      
    	
    	/**
    	 * Annotation Panel lets user choose additional annotations from
    	 * Resourcerer. This feature is currently available only for Affymetrix files.
    	 */
    	JPanel annotationPanel;
    	JLabel getAnnotation,  customAnnotation;
    	JButton connectButton, browseButton3;
    	JTextField annFileListTextField;
    	
    	
        protected EventListener eventListener;
  
        private int xRow = -1;
        private int xColumn = -1;
		
		

        

      public RMAFileLoaderPanel() {

            
    	  eventListener=new EventListener();
            setLayout(new GridBagLayout());

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
            browseButton1.setBorder(BorderFactory
    				.createBevelBorder(BevelBorder.RAISED));
    		browseButton1.setSize(100, 30);
    		browseButton1.setPreferredSize(new Dimension(100, 30));
    	

            fileSelectionPanel = new JPanel();
            fileSelectionPanel.setLayout(new GridBagLayout());
            fileSelectionPanel.setBorder(new TitledBorder(new EtchedBorder(), "File    (RMA Format Files)"));

        	
    		gba.add(fileSelectionPanel, dataSelection, 0, 0, 1, 1, 0, 0, GBA.B,GBA.C, new Insets(5, 5, 5, 5), 0, 0);
    		gba.add(fileSelectionPanel, fileNameTextField, 1, 0, 1, 1, 0, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
    		gba.add(fileSelectionPanel, browseButton1, 2, 0, GBA.RELATIVE, 1, 0,0, GBA.NONE, GBA.C, new Insets(5, 5, 5, 5), 0, 0);

    		gba.add(fileSelectionPanel, fileSelectionLabel, 0, 2, 2, 1, 0, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
            gba.add(fileSelectionPanel, selectedFiles, 1, 2, 1, 1, 2, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0); 
              
            annotationPanel = new JPanel();
            annotationPanel.setLayout(new GridBagLayout());
            annotationPanel.setBorder(new TitledBorder(new EtchedBorder(), "Annotation"));
              
            
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
    		
    		browseButton3 = new JButton("Browse");
    		browseButton3.setSize(new Dimension(100, 30));
    		browseButton3.setPreferredSize(new Dimension(100, 30));
    		browseButton3.addActionListener(new EventListener());

    		 
    		gba.add(annotationPanel, getAnnotation, 0, 0, 2, 1, 0, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
    		gba.add(annotationPanel, connectButton, 1, 0, GBA.RELATIVE, 1, 0, 0,GBA.NONE, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
    	    		
    		gba.add(annotationPanel, customAnnotation, 0, 1, 1, 1, 0, 0, GBA.H, GBA.C, new Insets(5,5,5,5),0,0);
    		gba.add(annotationPanel, annFileListTextField, 1, 1, 1, 0, 1, 0, GBA.H,	GBA.C, new Insets(5, 5, 5, 5), 0, 0);
    		gba.add(annotationPanel, browseButton3, 2, 1, GBA.RELATIVE, 1, 0,0, GBA.NONE, GBA.C, new Insets(5, 5, 10, 5), 0, 0);
    	
    	
            expressionTable = new JTable();
            expressionTable.setDefaultRenderer(Object.class, new MyCellRenderer());
            expressionTable.setIntercellSpacing(new Dimension(1, 1));
    		expressionTable.setShowHorizontalLines(false);
    		expressionTable.setShowVerticalLines(true);
    		expressionTable.setGridColor(Color.LIGHT_GRAY);
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

        
            fileLoaderPanel = new JPanel();
            fileLoaderPanel.setLayout(new GridBagLayout());
       
            gba.add(fileLoaderPanel,fileSelectionPanel, 0, 0, 1, 1, 1, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
            gba.add(fileLoaderPanel, annotationPanel, 	0, 2, 1, 1, 1, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
            gba.add(fileLoaderPanel, tablePanel, 		0, 4, 1, 6, 3, 6, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
           
            gba.add(this, fileLoaderPanel, 0, 0, 1, 1, 1, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);  

        }

       

        public void openDataPath() {
        }
        public void onBrowse() {
        	JFileChooser fileChooser=new JFileChooser(SuperExpressionFileLoader.DATA_PATH);
        	int retVal=fileChooser.showOpenDialog(RMAFileLoaderPanel.this);
        	
        	if(retVal==JFileChooser.APPROVE_OPTION) {
        	File selectedFile=fileChooser.getSelectedFile();
        	processRMAFile(selectedFile);
        	}
           		
    	}
        
        public void onAnnotationFileBrowse() {
        	FileLoaderUtility fileLoad = new FileLoaderUtility();
        	File selectedFile;
        	JFileChooser fileChooser = new JFileChooser(
        			SuperExpressionFileLoader.ANNOTATION_PATH);
        	fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        	int retVal = fileChooser.showOpenDialog(RMAFileLoaderPanel.this);

        	if (retVal == JFileChooser.APPROVE_OPTION) {

        		selectedFile = fileChooser.getSelectedFile();
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
        
   
        
        public JTable getTable() {
            return expressionTable;

        }

        

        public int getXColumn() {
            return xColumn;

        }

        

        public int getXRow() {
            return xRow;

        }

        

        public void selectRMAFile() {
            JFileChooser jfc = new JFileChooser(SuperExpressionFileLoader.DATA_PATH);
            jfc.setFileFilter(getFileFilter());
            int activityCode = jfc.showDialog(this, "Select");

            if (activityCode == JFileChooser.APPROVE_OPTION) {
                File target = jfc.getSelectedFile();
                processRMAFile(target);

            }

        }

        

        public void setFileName(String fileName) {
            fileNameTextField.setText(fileName);
            selectedFiles.setText(fileName);
        }

        

        public void setTableModel(TableModel model) {
            expressionTable.setModel(model);
            int numCols = expressionTable.getColumnCount();
            for(int i = 0; i < numCols; i++){
                expressionTable.getColumnModel().getColumn(i).setMinWidth(75);
            }
        }

        

        public void setFieldsText(String fieldsText) {
           // fieldsTextField.setText(fieldsText);

        }

        private class EventListener implements ActionListener {
    		public void actionPerformed(ActionEvent event) {
    			Object source = event.getSource();
    			if (source == browseButton1) {
    				onBrowse();
    			} else if (source==browseButton3){
        			onAnnotationFileBrowse();  
        		}
        		else if (source==connectButton){
        			onConnect();  
        		}
        		   
    			
    			}
    		}
    	
        

        private class FileTreePaneEventHandler implements FileTreePaneListener {

            public void nodeSelected(FileTreePaneEvent event) {
      
            }
            
            public void nodeCollapsed(FileTreePaneEvent event) {}
            public void nodeExpanded(FileTreePaneEvent event) {}
        }
    }

@Override
public String getAnnotationFilePath() {
	return this.sflp.annFileListTextField.getText();
	
}    
}
