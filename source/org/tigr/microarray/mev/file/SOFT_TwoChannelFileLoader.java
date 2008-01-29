

/*
Copyright @ 1999-2005, The Institute for Genomic Research (TIGR).
All rights reserved.
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
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
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
import org.tigr.microarray.mev.ISlideMetaData;
import org.tigr.microarray.mev.SlideData;
import org.tigr.microarray.mev.SlideDataElement;
import org.tigr.microarray.mev.TMEV;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.util.MyCellRenderer;

/**
 * @author Sarita Nair
 * @date  Sep 18, 2007
 * SOFT_TwoChannelFileLoader1 was developed to enable loading SOFT files by platform (GPL)
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



public class SOFT_TwoChannelFileLoader extends ExpressionFileLoader {
		
		private GBA gba;
	    private SOFT_TwoChannelFileLoaderPanel softflp;
	    private int dataType;
	    
	    
		private int data_row_count=0;
	    private int numOfSamplesinFile=0;
	    private Vector sampleNames=new Vector();
	   
	    private boolean loadEnable=false;
		
	    private Vector rawLines=new Vector();
	    private Hashtable platformMatrix=new Hashtable();
	    private Vector platformHeaders=new Vector();
	    private String[] moreFields=new String[]{};
	    private String[] fieldNames=new String[] {};
	    
   public SOFT_TwoChannelFileLoader(SuperExpressionFileLoader superLoader) {
	 
	   super(superLoader);
       gba = new GBA();
       softflp = new SOFT_TwoChannelFileLoaderPanel();
	   
   }
   
   
   public FileFilter getFileFilter() {

       FileFilter affymetrixFileFilter = new FileFilter() {

           public boolean accept(File f) {
               if (f.isDirectory()) return true;
               if (f.getName().endsWith(".txt") || f.getName().endsWith(".TXT") ) return true;
               else return false;
           }

           public String getDescription() {
               return "GEO SOFT Two Channel Format Files (*.txt)";
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
 * @author Sarita Nair
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
 * 
 * 
 */

	private Vector loadSOFT_AffyFile(File file) throws IOException{
			
		setDataType(IData.DATA_TYPE_RATIO_ONLY);

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
        
		if(this.platformMatrix.size()!=0) {
			moreFields = new String[this.platformHeaders.size()];
			fieldNames = new String[this.platformHeaders.size()];

			for(int m=0;m<this.platformHeaders.size();m++) {
				fieldNames[m]=(String)this.platformHeaders.elementAt(m);
			}

		} else {
			moreFields = new String[preExperimentColumns];
			fieldNames = new String[preExperimentColumns];

			for(int i = 0; i < preExperimentColumns; i++){
				fieldNames[i] = "ID_REF";
			}
		}
		
		
		String[] extraFields=new String[1];

		ISlideData[]slideData=new ISlideData[sampleNames.size()];
		SlideDataElement sde=null;

		slideData[0]=new SlideData(spotCount, rColumns);
		slideData[0].setSlideFileName(file.getAbsolutePath());


		for (int i=1; i<slideData.length; i++) {
			slideData[i] = new FloatSlideData(slideData[0].getSlideMetaData(), spotCount);
			slideData[i].setSlideFileName(file.getPath());

		}


		slideData[0].getSlideMetaData().appendFieldNames(fieldNames);

		for(int i=0; i<slideData.length;i++) {
			slideData[i].setSlideDataName((String)sampleNames.get(i));

		}


		for(int i=0;i<spotCount;i++){

			String probeID=(String)this.softflp.expressionTable.getValueAt(i, 0);
						
			if(this.platformMatrix.size()!=0) {
				String Val=(String)this.platformMatrix.get(probeID);
				StringSplitter pSplit=new StringSplitter(':');
				pSplit.init(Val);
				for(int j=0;j<platformHeaders.size();j++) {
					if(pSplit.hasMoreTokens())
						moreFields[j]=(String)pSplit.nextToken();

				}
			} else {
				for (int k=0; k<preExperimentColumns; k++) {
					moreFields[k] = probeID;
					}
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
			sde = new SlideDataElement(String.valueOf(i), rows, columns, new float[2], moreFields);
			slideData[0].addSlideDataElement(sde);


			int col=1;
			for ( int k=0; k<slideData.length;k++ ) {  
				
				try {	
					// Intensity
					intensities[0] = 1.0f;
					intensities[1] = Float.parseFloat((String)this.softflp.expressionTable.getValueAt(i, col));

				} catch (Exception e) {
					intensities[1] = Float.NaN;
				}
				slideData[k].setIntensities(i, intensities[0], intensities[1]);

				col=col+1;

			}

		}




		Vector data=new Vector();
		for(int i=0; i<slideData.length;i++)
			data.add(slideData[i]);
		return data;


      
	}
	
	
	    

/**
 * @processSOFT_TwoChannelFile
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
 * 
 */
	 
	 
	 
	
	 public void processSOFT_TwoChannelFile(File targetFile) {
		
		 Hashtable dataMatrix=new Hashtable();
		 Vector columnHeaders=null;
		 Vector Headers=new Vector();
		 Headers.add(0, "ID_REF");
		 BufferedReader reader = null;
		 String currentLine = null;
		 int index=0;


		 softflp.setFileName(targetFile.getAbsolutePath());

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

					 columnHeaders= new Vector();
					 columnHeaders.clear();
					 columnHeaders.add(0, sample);
				 }

				 if(currentLine.contains("sample_table_begin")){
					 int VALUEcolumnPosition=0;
					 int ID_REFcolumnPosition=0;

					 String colName="";

					 currentLine = reader.readLine(); 
					 ss.init(currentLine);

					 while(ss.hasMoreTokens()) {
						 columnHeaders.add(ss.nextToken());
					 }
					 

					 for(int i=0; i<columnHeaders.size();i++) {
						
						 if((colName=(String)columnHeaders.get(i)).equalsIgnoreCase("VALUE")) {
							 VALUEcolumnPosition=i-1;
							 String sample=(String)sampleNames.elementAt(index);
							 Headers.add(sample);
						 }
						 if((colName=(String)columnHeaders.get(i)).equalsIgnoreCase("ID_REF")) {
							 ID_REFcolumnPosition=i-1;
						 }

					 }



					 while (!(currentLine=reader.readLine()).contains("sample_table_end")) {
						 ss.init(currentLine);
						 String key=null, value=null;
						 //System.out.println("!Sample table end");
						 int i=0;
						 while( i<columnHeaders.size()) {

							 if(i==ID_REFcolumnPosition) {
								 key=ss.nextToken();
								 i=i+1;
							 }

							 if(ss.hasMoreTokens()) {
							 if(i==VALUEcolumnPosition) {
									 value=((String)this.sampleNames.elementAt(sampleNames.size()-1));
									 value=value.concat(":");
									 value=value.concat(ss.nextToken());
									// System.out.println("value:"+value);
									 i=i+1;

								 }else {
									 ss.nextToken();
									 i=i+1;
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
		
		 generateExpressionTable(dataMatrix, Headers);
		}

/**
* @generateExpressionTable 
* @param dataMatrix
* @param Headers
* 
* This function populates the expression table. The "Headers" parameter is used to 
* set the column headers of the expression table. The key and corresponding values in the dataMatrix
* hashtable are used to populate the "rowVector". The "rowVector" is subsequently added
* to the table model.
* If a particular probe is found to be absent from some of the samples, 
 an error is flagged.
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

				if(sName.equalsIgnoreCase((String)sampleNames.elementAt(i))) {	
					try {
						rowVector.add(split.nextToken());
					}catch(Exception e) {
						rowVector.add("");
					}


				}else {
					String eMsg = "<html>The following probes are missing from some samples<br>" +
					"<html>Probes:<br> "+key+
					"<html>Sample<br>"+sName+" </html>";
					JOptionPane.showMessageDialog(null, eMsg, "Error", JOptionPane.ERROR_MESSAGE);
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

  //Two Channel File Loader Panel begins 
	 private class SOFT_TwoChannelFileLoaderPanel extends JPanel {
	    	
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
	  	
	        JLabel instructionsLabel;
	    
	         private int xRow = -1;
	         private int xColumn = -1;
	        
	      
	        
	       
	        public SOFT_TwoChannelFileLoaderPanel() {
	            
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
	            fileSelectionPanel.setBorder(new TitledBorder(new EtchedBorder(), "File    (GEO SOFT Two Channel Format Files) "));
	            
	            gba.add(fileSelectionPanel, dataSelection, 0, 0, 1, 1, 0, 0, GBA.B,GBA.C, new Insets(5, 5, 5, 5), 0, 0);
	    		gba.add(fileSelectionPanel, fileNameTextField, 1, 0, 1, 1, 1, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
	    		gba.add(fileSelectionPanel, browseButton1, 2, 0, GBA.RELATIVE, 1, 0,0, GBA.NONE, GBA.C, new Insets(5, 5, 5, 5), 0, 0);

	    		gba.add(fileSelectionPanel, fileSelectionLabel, 0, 2, 2, 1, 0, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
	            gba.add(fileSelectionPanel, selectedFiles, 1, 2, 1, 1, 2, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0); 
	              
	    		
	           
	            
	            expressionTable = new JTable();
	            expressionTable.setDefaultRenderer(Object.class, new MyCellRenderer());
				expressionTable.setGridColor(Color.LIGHT_GRAY);
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
	     
	           
	           gba.add(fileLoaderPanel,fileSelectionPanel, 	0, 0, 1, 1, 1, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
	           gba.add(fileLoaderPanel, tablePanel, 		0, 7, 1, 6, 3, 6, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
	          
	           gba.add(this, fileLoaderPanel,0, 0, 1, 1, 1, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
	          
	            
	            
	        }
	        
	        public void openDataPath() {
	         
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
	        
	        public void selectSOFT_TwoChannelFile() {
	            JFileChooser jfc = new JFileChooser(SuperExpressionFileLoader.DATA_PATH);
	            jfc.setFileFilter(getFileFilter());
	            int activityCode = jfc.showDialog(this, "Select");
	            
	            if (activityCode == JFileChooser.APPROVE_OPTION) {
	                File target = jfc.getSelectedFile();
	                processSOFT_TwoChannelFile(target);
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
	        
	        
	        private class ListRenderer extends DefaultListCellRenderer {
	            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
	                 return null;
	            }
	        }
	        
	        
	        
	        private class ListListener implements javax.swing.event.ListSelectionListener {
	            
	            public void valueChanged(ListSelectionEvent lse) {
	            	
	               
	            }
	            
	        }
	        
	        private class EventHandler implements ActionListener {
	            public void actionPerformed(ActionEvent event) {
	                Object source = event.getSource();
	                if (source == browseButton1) {
	                	selectSOFT_TwoChannelFile();
	                    
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
    
   	}