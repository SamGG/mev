

/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/

package org.tigr.microarray.mev.file;

import java.awt.Color;
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
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
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
import org.tigr.microarray.mev.MultipleArrayViewer;
import org.tigr.microarray.mev.SlideData;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.sampleannotation.SampleAnnotation;
import org.tigr.microarray.util.ExpressionFileTableCellRenderer;
import org.tigr.util.swing.SOFTFileFilter;
import org.tigr.util.swing.TXTFileFilter;

/**
 * @author Sarita Nair
 * @date  Sep 18, 2007
 * SOFT_AffymetrixFileLoader1 was developed to enable loading GEO SOFT files by platform (GPL)
 * 
 * 
 * (ref: ftp://ftp.ncbi.nih.gov/pub/geo/README.TXT) 
 * GPLxxx_family files contain SOFT-formatted data for all Samples (GSM) 
	processed using one Platform (GPL), and all Series (GSE) associated 
	with those Samples.	GPLxxx_family files are updated on a monthly basis.
	The current implementation requires the user to choose the appropriate loader
	based on the platform (Affymetrix or Two color) to load GPL files.
	
 * 
 */



public class SOFT_AffymetrixFileLoader extends ExpressionFileLoader {
		
		private GBA gba;
	    private SOFT_AffymetrixFileLoaderPanel softflp;
	    private int dataType;
	    private String[]fieldNames=new String[] {};
	    private String[]moreFields=new String[] {};
	    
		private int data_row_count=0;
	    private int numOfSamplesinFile=0;
	    private Vector sampleNames=new Vector();
	   
	    private boolean loadEnable=false;
		
	    private Vector rawLines=new Vector();
	    private Hashtable platformMatrix=new Hashtable();
	    private Vector platformHeaders=new Vector();
	    private boolean IntensitywithDetection=false;
	    private boolean OnlyIntensity=false;
	    private MultipleArrayViewer mav;
	    
   public SOFT_AffymetrixFileLoader(SuperExpressionFileLoader superLoader) {
	 
	   super(superLoader);
	   this.mav=superLoader.getArrayViewer();
       gba = new GBA();
       softflp = new SOFT_AffymetrixFileLoaderPanel();
	   
   }
   
   //TODO EH
   public void setFilePath(String path) {
   	softflp.setFileName(path);
	processSOFT_AffymetrixFile(new File(path));
   }

   public FileFilter getFileFilter() {

       FileFilter affymetrixFileFilter = new FileFilter() {

           public boolean accept(File f) {
               if (f.isDirectory()) return true;
               if (f.getName().endsWith(".soft") || f.getName().endsWith(".SOFT") ) return true;
               if (f.getName().endsWith(".txt") || f.getName().endsWith(".TXT") ) return true;
               else return false;
           }

           public String getDescription() {
               return "GEO SOFT Affymetrix Format Files (*.soft, *.txt)";
           }
       };

       return affymetrixFileFilter;
   }
   
   
   
   public boolean checkLoadEnable() {
	     // Currently, the only requirement is that a cell has been highlighted
       
       int tableRow = softflp.getXRow() + 1; // Adjusted by 1 to account for the table header
       int tableColumn = softflp.getXColumn();
       
       if (tableColumn < 0) return false;
       
       TableModel model = softflp.getTable().getModel();
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


   public void markLoadEnabled(boolean state) {
       this.loadEnable = state;
       setLoadEnabled(loadEnable);
      
   }



	@Override
	public JPanel getFileLoaderPanel() {
		return softflp;
		}



	@Override
	public String getFilePath() {
	return null;
	}



	@Override
	public ISlideData loadExpressionFile(File f) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	  @Override
	
	  
	  
public Vector loadExpressionFiles() throws IOException {
		
	Vector data=new Vector();
	 data=loadSOFT_AffyFile(new File(this.softflp.fileNameTextField.getText()));
	 return data;
	}

/**
 * @loadSOFT_AffyFile
 * @param file
 * @return
 * @throws IOException
 * 
 * This function fills the appropriate data structure with the data values.
 * I have tried to implement the function such that:
 * 
 * 1. The probes can be in any order within the samples.
 * 2. The probes in the platform data can be in a different order.   
 * 
 * 
 * Implementation
 * The expression Table is read row by row.
 * The platformMatrix hashtable is queried with the current probeID and
 * the corresponding value is populated in the field "moreFields". Also,
 * depending on whether the data contains only "intensity" values or "intensityWithDetection"
 * values, the field "col" is incremented accordingly.
 * 
 * 
 */

	  private Vector loadSOFT_AffyFile(File file) throws IOException{

		//  setDataType(TMEV.DATA_TYPE_AFFY);
		  setDataType(IData.DATA_TYPE_AFFY_ABS);

		  float[] intensities = new float[2];
		  final int rColumns = 1;
		  final int totalRows =softflp.expressionTable.getRowCount();
		  final int totalColumns =softflp.expressionTable.getColumnCount();


		  int[] rows = new int[] {0, 1, 0};
		  int[] columns = new int[] {0, 1, 0};
		  int row,column;
		  row=column=1;


		  final int preSpotRows = this.softflp.getXRow();
		  final int preExperimentColumns = this.softflp.getXColumn();
		  final int spotCount=totalRows-preSpotRows; 



		  if (spotCount <= 0) {
			  JOptionPane.showMessageDialog(superLoader.getFrame(),  "There is no spot data available.",  "TDMS Load Error", JOptionPane.INFORMATION_MESSAGE);
		  }
		  
		  if(this.platformMatrix!=null) {
			  moreFields = new String[this.platformHeaders.size()];
			  fieldNames = new String[this.platformHeaders.size()];

			  for(int m=0;m<this.platformHeaders.size();m++) {
				  fieldNames[m]=(String)this.platformHeaders.elementAt(m);
				 //  System.out.println("FieldNames:"+fieldNames[m]);

			  }
		  } else {
			  moreFields=new String[1];
			  fieldNames = new String[1];
			  fieldNames[0]="ID_REF";
		  
		  }
		  String[] extraFields=new String[1];

		  ISlideData[]slideData=new ISlideData[sampleNames.size()];
		  AffySlideDataElement affysde=null;
		  
		  SampleAnnotation sampAnn=new SampleAnnotation();
	      slideData[0] = new SlideData(spotCount, rColumns, sampAnn);
		  slideData[0].setSlideFileName(file.getAbsolutePath());


		  for (int i=1; i<slideData.length; i++) {
			  sampAnn=new SampleAnnotation();
			  slideData[i] = new FloatSlideData(slideData[0].getSlideMetaData(), spotCount, sampAnn);
			  slideData[i].setSlideFileName(file.getPath());

		  }


		

		  slideData[0].getSlideMetaData().appendFieldNames(fieldNames);

		  for(int i=0; i<slideData.length;i++) {
			  slideData[i].setSampleAnnotationLoaded(true);
			  slideData[i].getSampleAnnotation().setAnnotation("Default Slide Name",(String)sampleNames.get(i));
							
			  slideData[i].setSlideDataName((String)sampleNames.get(i));
			  this.mav.getData().setSampleAnnotationLoaded(true);

		  }


		  for(int i=0;i<spotCount;i++){

			  String probeID=(String)this.softflp.expressionTable.getValueAt(i, 0);
			  

			  if(this.platformMatrix!=null) {
				  String Val=(String)this.platformMatrix.get(probeID);
				  StringSplitter pSplit=new StringSplitter(':');
				  pSplit.init(Val);
				  moreFields[0]=probeID;
				  for(int j=1;j<platformHeaders.size();j++) {
					  if(pSplit.hasMoreTokens())
						  moreFields[j]=(String)pSplit.nextToken();
					  
				  }
			  }else {
				  moreFields[0]=probeID;
			  }

			  rows[0] = rows[2] = row;
			  columns[0] = columns[2] = column;
			  if (column == rColumns) {
				  column = 1;
				  //row++;
				  row=row+1;


			  } else {

				  column=column+1;

			  }
			  affysde = new AffySlideDataElement(String.valueOf(i), rows, columns, new float[2], moreFields);
			  slideData[0].addSlideDataElement(affysde);

			  // System.out.println("column count:"+this.softflp.expressionTable.getColumnCount());
			  int col=1;
			  for ( int k=0; k<slideData.length;k++ ) {  
				 // System.out.println("col:"+col);
				  try {	
					  
					  // Intensity
					  intensities[0] = 1.0f;
					  intensities[1] = Float.parseFloat((String)this.softflp.expressionTable.getValueAt(i, col));
					 
					  if(this.IntensitywithDetection){
						  extraFields[0]=(String)this.softflp.expressionTable.getValueAt(i, col+1);//detection
					    }

				  } catch (Exception e) {
					  intensities[1] = Float.NaN;
				  }
				  if(k==0){

					  slideData[k].setIntensities(i, intensities[0], intensities[1]);

					  if(this.IntensitywithDetection){
						  affysde.setDetection(extraFields[0]);
					  }
				  }else{
					  if(i==1){
						  meta = slideData[0].getSlideMetaData();                    	
					  }
					  slideData[k].setIntensities(i,intensities[0],intensities[1]);

					  if(this.IntensitywithDetection){
						  ((FloatSlideData)slideData[k]).setDetection(i,extraFields[0]);
					  }
				  }

				  if(this.IntensitywithDetection) {
					  col=col+2;
				  }else {
					  col=col+1;
				  }
			  }

		  }




		  Vector data=new Vector();
		  for(int i=0; i<slideData.length;i++)
			  data.add(slideData[i]);
		  return data;



	  }
	
	
	    

/**
 * @processSOFT_AffyFile
 * @param targetFile
 * @return void
 * 
 * This function takes in the input GPL format file(Affymetrix platform).
 * 
 * Assumption
 * 1. The ID_REF (gene identifier) column has unique values. 
 * 2. The file complies to the format specifed by GEO for GPL files as of the date of
 * writing the loader (Sep 18, 2007). I have provided  the GEO URL at the top.
 * 3. Consistency in the format/order of sample headers. As of now the loader expects the headers
 * to be in the order (ID_REF, VALUE and ABS_CALL) 
 * 4. Files have to be in a tab delimited format with the extension .txt
 *  
 * Multiple samples in a file are treated as seperate files. Each sample MUST have atleast two columns namely,
 * (ID_REF and VALUE). The default behavior of the loader is to assume that all the three columns
 * namely (ID_REF, VALUE and ABS_CALL) are present. This behavior is reflected in the
 * "intensityWithDetectionRadioButton" being checked by default
 * 
 * 
 *  
 *  Implementation:
 *  GPL format files ALWAYS have platform (annotation) information in them. The platform information is 
 *  between the headers "platform_table_begin" and "platform_table_end".
 *  The input file is provided as argument to the function parsePlatformData(){GEOPlatformFileParser class}
 *  This function returns a Hashtable (Key: ID, Value- ":" seperated list of values). The GEOPlatformParser
 *  class is also used to get the headers present in the Platform data (eg ID, SPOT_ID,etc).
 *  
 *  After the platform data parsing, begins the data parsing. The sample data is delimited by the
 *  headers "sample_table_begin" and "sample_table_end". 
 *  The three variables "VALUEcolumnPosition", "ID_REFcolumnPosition" and "ABS_CALLcolumnPosition"
 *  are used to note the column position in the file.
 *  
 */
	 
	 
	 
	
	 public void processSOFT_AffymetrixFile(File targetFile) {
		
		 Hashtable dataMatrix=new Hashtable();
		 Vector _tempcolumnHeaders=null;
		 Vector eTableHeaders=new Vector();
		 eTableHeaders.add(0, "ID_REF");
		 BufferedReader reader = null;
		 String currentLine = null;
		//index is used to keep a track of the number of samples in the file. We do not
		//have this information apriori. 
		 int index=0;


		 softflp.setFileName(targetFile.getName());
		 GEOPlatformfileParser parser=new GEOPlatformfileParser();
		 this.platformMatrix=parser.parsePlatformData(targetFile);
		 this.platformHeaders=parser.getColumnHeaders();

		 try {
			 reader = new BufferedReader(new FileReader(targetFile));
		 } catch (FileNotFoundException fnfe) {
			 fnfe.printStackTrace();
		 }

		 try {
			 StringSplitter ss=new StringSplitter('\t');
			 StringSplitter split=new StringSplitter('=');
			 while((currentLine=reader.readLine())!=null) {

				 if(currentLine.contains("^SERIES"))
					 break;

				 if(currentLine.contains("^SAMPLE")){
					 split.init(currentLine);
					 split.nextToken();
					 String sample=split.nextToken();
					 this.sampleNames.add(index, sample);

					 _tempcolumnHeaders= new Vector();
					 _tempcolumnHeaders.clear();
					 _tempcolumnHeaders.add(0, sample);
				 }

				 if(currentLine.contains("sample_table_begin")){
					 int VALUEcolumnPosition=0;
					 int ID_REFcolumnPosition=0;
					 int ABS_CALLcolumnPosition=0; 
					 String colName="";

					 currentLine = reader.readLine(); 
					 ss.init(currentLine);

					 while(ss.hasMoreTokens()) {
						 _tempcolumnHeaders.add(ss.nextToken());
					 }

					 for(int i=0; i<_tempcolumnHeaders.size();i++) {
						 if((colName=(String)_tempcolumnHeaders.get(i)).equalsIgnoreCase("VALUE")) {
							 VALUEcolumnPosition=i-1;
							 String sample=(String)sampleNames.elementAt(index);
							 eTableHeaders.add(sample);
						 }
						 if((colName=(String)_tempcolumnHeaders.get(i)).equalsIgnoreCase("ID_REF")) {
							 ID_REFcolumnPosition=i-1;
						 }

						 if((colName=(String)_tempcolumnHeaders.get(i)).equalsIgnoreCase("ABS_CALL")) {
							 ABS_CALLcolumnPosition=i-1;
							 eTableHeaders.add(_tempcolumnHeaders.get(i));
							 this.IntensitywithDetection=true;
						 }


					 }
					 
					 if(!this.IntensitywithDetection) {
						 this.OnlyIntensity=true;
					 }




					 while (!(currentLine=reader.readLine()).contains("sample_table_end")) {
						 ss.init(currentLine);
						 String key=null, value=null;
						 //System.out.println("!Sample table end");
						 int i=0;
						 while(i<_tempcolumnHeaders.size()) {
							 if(i==ID_REFcolumnPosition) {
								 key=ss.nextToken();
								 i=i+1;
							 }

							 if(ss.hasMoreTokens()) {
								 
								 if(this.OnlyIntensity) {
									 if(i==VALUEcolumnPosition) {
										 value=((String)this.sampleNames.elementAt(sampleNames.size()-1));
										 value=value.concat(":");
										 value=value.concat(ss.nextToken());
										 i=i+1;
									 }else {
										 ss.nextToken();
										 i=i+1;
									 }
								 }else if(this.IntensitywithDetection) {
									 //System.out.println("Intensity with detection selected");
									 if(i==VALUEcolumnPosition) {
										 value=((String)this.sampleNames.elementAt(sampleNames.size()-1));
										 value=value.concat(":");
										 value=value.concat(ss.nextToken());
										 value=value.concat(":");
										 i=i+1;

									 }else if(i==ABS_CALLcolumnPosition) {
										 value=value.concat(ss.nextToken());
										 i=i+1;

									 }else {
										 ss.nextToken();
										 i=i+1;
									 }
								 }

							 }else
								 i=i+1;

						 }

						 if(!dataMatrix.containsKey(key)) {
							 dataMatrix.put(key, value);
						 }else {
							 String val=(String)dataMatrix.get(key);
							 val=val.concat(":");
							 val=val.concat(value);
							 dataMatrix.put(key, val);
							 //  System.out.println("Key:"+key); 
							 //System.out.println("hashtable value:"+val);
							 //System.out.println("value added:"+value);
						 }

					 }

					 index=index+1;	 

				 }
			 }

		 }catch(Exception e) {
			 e.printStackTrace();
		 }
		 
		 generateExpressionTable(dataMatrix, eTableHeaders);
		}

	/**
	 * generateExpressionTable 
	 * @param dataMatrix
	 * @param Headers
	 * 
	 * This function populates the expression table. The "Headers" parameter is used to 
	 * set the column headers of the expression table. The key and corresponding values in the dataMatrix
	 * hashtable are used to populate the "rowVector". The "rowVector" is subsequently added
	 * to the table model.
	 * 
	 * If a particular probe is found to be absent from some of the samples, 
	 * an error is flagged.
	 * 
	 * 
	 */
	 
	 
	public void generateExpressionTable(Hashtable dataMatrix, Vector Headers) {
		
		Vector rowVector;
		Enumeration eKeys=dataMatrix.keys();
		DefaultTableModel model = new DefaultTableModel() {
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		model.setColumnIdentifiers(Headers);

		while(eKeys.hasMoreElements()) {
			rowVector=new Vector();
			StringSplitter split=new StringSplitter(':');
			String key=(String)eKeys.nextElement();
			
			String value=(String)dataMatrix.get(key);
			String sName="";
			rowVector.add(key);
			split.init(value);

			
			for(int i=0; i<sampleNames.size();i++) {
				if(split.hasMoreTokens())
					sName=split.nextToken();


				if(this.IntensitywithDetection) {

					if(sName.equalsIgnoreCase((String)sampleNames.elementAt(i))) {	

						try {
							rowVector.add(split.nextToken());
							rowVector.add(split.nextToken());
						}catch(Exception e) {
							rowVector.add("");
							rowVector.add("NA");
						}

					}else {
						String eMsg = "<html>The following probes are missing from some samples<br>" +
						"<html>Probes: "+key+"<br>"+
						"<html>Sample: "+sName+"<br>"+" </html>";
						JOptionPane.showMessageDialog(null, eMsg, "Error", JOptionPane.ERROR_MESSAGE);
					}
				}else if(this.OnlyIntensity) {
					if(sName.equalsIgnoreCase((String)sampleNames.elementAt(i))) {
						try {
							rowVector.add(split.nextToken());
						}catch(Exception e) {
							rowVector.add("");
						}
					}else {
						String eMsg = "<html>The following probes are missing from some samples<br>" +
						"<html>Probes: "+key+"<br>"+
						"<html>Sample: "+sName+"<br>"+" </html>";
						JOptionPane.showMessageDialog(null, eMsg, "Error", JOptionPane.ERROR_MESSAGE);
					}
				}


			}

			model.addRow(rowVector);     
		}        
		softflp.setTableModel(model);
		
	}
	 


	private boolean validateFile(File targetFile) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void openDataPath() {
		// TODO Auto-generated method stub
		
	}    

	public void setDataType(int data_Type){
		this.dataType=data_Type;
	}

	public int getDataType(){
		return this.dataType;
	}

   

    
    private class SOFT_AffymetrixFileLoaderPanel extends JPanel {
    	/**
		 * FileLoaderPanel contains two different panels namely,
		 * 1. fileLoaderPanel
		 * 2. tablePanel
		 * 
		 * 
		 */

		JPanel fileLoaderPanel;

		/**
		 * "fileSelectionPanel lies within "fileLoaderPanel" and
		 * helps user choose an expression data file. The components
		 * within this panel being,
		 * 
		 * 1. dataSelection
		 * 2. fileNameTextField
		 * 3. browseButton1
		 * 4. selectedFiles
		 */
		JPanel fileSelectionPanel;
		JLabel dataSelection;
		JTextField fileNameTextField;
		JButton browseButton1;
		JLabel fileSelectionLabel;
		JTextField selectedFiles;
		
		/**
		 * additionalRequirementPanel is to find out more about the data
		 * MeV expects affymetrix data to have 
		 * 1.Intensity values &
		 * 2.Detection values  OR both.
		 * 
		 * 
		 */
		  JPanel additionalRequirementPanel;
		  ButtonGroup optionsButtonGroup;
	      JRadioButton onlyIntensityRadioButton;
	      JRadioButton intensityWithDetectionRadioButton;

		/**
		 * "tablePanel" shows the expression data loaded by the user
		 * as a table. The components within this panel being,
		 * 
		 * 1. expressionTable
		 * 2. tableScrollPane
		 * 
		 */
		JPanel tablePanel;
		JTable expressionTable;
		JScrollPane tableScrollPane;



		private int xRow = -1;
		private int xColumn = -1;

		public SOFT_AffymetrixFileLoaderPanel() {

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
			browseButton1.addActionListener(new EventHandler());
			browseButton1.setSize(100, 30);
			browseButton1.setPreferredSize(new Dimension(100, 30));



			fileSelectionPanel = new JPanel();
			fileSelectionPanel.setLayout(new GridBagLayout());
			fileSelectionPanel.setBorder(new TitledBorder(new EtchedBorder(), "File    (GEO GPL Family Format Files *Affymetrix*)"));

			gba.add(fileSelectionPanel, dataSelection, 0, 0, 1, 1, 0, 0, GBA.B,GBA.C, new Insets(5, 5, 5, 5), 0, 0);
			gba.add(fileSelectionPanel, fileNameTextField, 1, 0, 1, 1, 1, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
			gba.add(fileSelectionPanel, browseButton1, 2, 0, GBA.RELATIVE, 1, 0,0, GBA.NONE, GBA.C, new Insets(5, 5, 5, 5), 0, 0);

			gba.add(fileSelectionPanel, fileSelectionLabel, 0, 2, 2, 1, 0, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
			gba.add(fileSelectionPanel, selectedFiles, 1, 2, 1, 1, 2, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0); 

			/*additionalRequirementPanel = new JPanel();
            additionalRequirementPanel.setLayout(new GridBagLayout());
            additionalRequirementPanel.setBorder(new TitledBorder(new EtchedBorder(), "Additional Requirements"));
            optionsButtonGroup = new ButtonGroup();
            onlyIntensityRadioButton = new JRadioButton("Only Intensity");
            optionsButtonGroup.add(onlyIntensityRadioButton);

            intensityWithDetectionRadioButton = new JRadioButton("Intensity With Detection", true);
            optionsButtonGroup.add(intensityWithDetectionRadioButton);

            gba.add(additionalRequirementPanel, onlyIntensityRadioButton, 0, 0, 1, 1, 1, 0, GBA.H, GBA.C, new Insets(5, 5, 0, 5), 0, 0);
            gba.add(additionalRequirementPanel, intensityWithDetectionRadioButton, 1, 0, 1, 1, 1, 0, GBA.H, GBA.C, new Insets(5, 5, 0, 5), 0, 0);
              */



			expressionTable = new JTable();
			expressionTable.setDefaultRenderer(Object.class, new ExpressionFileTableCellRenderer());
			expressionTable.setGridColor(Color.LIGHT_GRAY);
			expressionTable.setSize(300, 300);
			expressionTable.setCellSelectionEnabled(true);
			expressionTable.setColumnSelectionAllowed(false);
			expressionTable.setRowSelectionAllowed(false);
			expressionTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			expressionTable.getTableHeader().setReorderingAllowed(true);
			
			expressionTable.addMouseListener(new MouseAdapter() {
				public void mousePressed(MouseEvent event) {
					xRow = expressionTable.rowAtPoint(event.getPoint());
					xColumn = expressionTable.columnAtPoint(event.getPoint());
					checkLoadEnable();
				}
			});           
			tableScrollPane = new JScrollPane(expressionTable, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);           



			tablePanel = new JPanel();
			tablePanel.setLayout(new GridBagLayout());
			tablePanel.setBorder(new TitledBorder(new EtchedBorder(), "Expression Table"));
			gba.add(tablePanel, tableScrollPane, 0, 0, 1, 2, 1, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);




			fileLoaderPanel = new JPanel();
			fileLoaderPanel.setLayout(new GridBagLayout());

			gba.add(fileLoaderPanel,fileSelectionPanel, 0, 0, 1, 1, 1, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
			gba.add(fileLoaderPanel, tablePanel, 0, 7, 1, 6, 3, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
			
			gba.add(fileLoaderPanel,fileSelectionPanel, 0, 0, 1, 1, 1, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
          //  gba.add(fileLoaderPanel, additionalRequirementPanel, 0, 2, 1, 1, 1, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
            gba.add(fileLoaderPanel, tablePanel, 0, 4, 1, 6, 3, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
            


			gba.add(this, fileLoaderPanel,0, 0, 1, 1, 1, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);


            
        }
        
        public void openDataPath() {
           
        }
        
        public void onBrowse() {
			JFileChooser fileChooser=new JFileChooser(SuperExpressionFileLoader.DATA_PATH);
			fileChooser.addChoosableFileFilter(new TXTFileFilter());
	        fileChooser.addChoosableFileFilter(new SOFTFileFilter());
			
			int retVal=fileChooser.showOpenDialog(this);

			if(retVal==JFileChooser.APPROVE_OPTION) {
				File selectedFile=fileChooser.getSelectedFile();
				softflp.fileNameTextField.setText(selectedFile.getAbsolutePath());
				softflp.selectedFiles.setText(selectedFile.getAbsolutePath());
						
				processSOFT_AffymetrixFile(selectedFile);
			}

		}

       

		public void setFileName(String fileName) {
			fileNameTextField.setText(fileName);
		}

		public void setTableModel(TableModel model) {
			expressionTable.setModel(model);
			int numCols = expressionTable.getColumnCount();
			for(int i = 0; i < numCols; i++){
				expressionTable.getColumnModel().getColumn(i).setMinWidth(75);
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


		private class EventHandler implements ActionListener {
			public void actionPerformed(ActionEvent event) {
				Object source = event.getSource();
				if (source == browseButton1) {
					onBrowse();
				}
			}
		}     

       
    }




	@Override
	public String getAnnotationFilePath() {
		// TODO Auto-generated method stub
		return null;
	}
	/**
	 * Not useful in this loader because it doesn't use the annotation model yet.
	 */
	public void setAnnotationFilePath(String filePath) {
//		sflp.adh.setAnnFilePath(filePath);
	}
}