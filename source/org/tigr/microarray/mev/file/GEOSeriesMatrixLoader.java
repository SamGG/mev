/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
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
import org.tigr.microarray.mev.MultipleArrayViewer;
import org.tigr.microarray.mev.SlideData;
import org.tigr.microarray.mev.SlideDataElement;
import org.tigr.microarray.mev.TMEV;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.sampleannotation.SampleAnnotation;
import org.tigr.microarray.util.ExpressionFileTableCellRenderer;

/**
 * @author SARITA NAIR
 * @date Sep, 12, 2007 
 * GEOSeriesMatrixLoader was developed to enable loading the Series Matrix type files supported by GEO.
 * 
 * (ref: ftp://ftp.ncbi.nih.gov/pub/geo/README.TXT) 
 * Series_matrix files are
 * summary text files that include a tab-delimited value-matrix table generated
 * from the 'VALUE' column of each Sample record, headed by Sample and Series
 * metadata. These files include SOFT attribute labels. Data generated from
 * multiple Platforms are contained in separate files. It is recommended to view
 * Series_matrix files in a spreadsheet application like Excel. CAUTION: value
 * data are extracted directly from the original records with no consideration
 * as to whether the values are directly comparable.
 * 
 * 
 * 
 */



public class GEOSeriesMatrixLoader extends ExpressionFileLoader {
	private GBA gba;
	private boolean stop = false;

	private int dataType;
	private GEO_SeriesMatrixFileLoaderPanel smatrixflp;
	private ExpressionFileTableCellRenderer myCellRenderer;
	private Vector datainfo=new Vector();//	   store sample info
	private Vector platforminfo=new Vector();//store platform info
	private boolean unload=false;
	private MultipleArrayViewer mav;

    public void setFilePath(String path) {
    	smatrixflp.setFileName(path);
    	process_GEOSeriesMatrixFile(new File(path));
    }

	public GEOSeriesMatrixLoader(SuperExpressionFileLoader superLoader) {
		super(superLoader);
		this.mav = superLoader.getArrayViewer();
		gba = new GBA();
		smatrixflp = new GEO_SeriesMatrixFileLoaderPanel();
	}

	public Vector loadExpressionFiles() throws IOException {
		return load_GEOSeriesExpressionFile(new File(this.smatrixflp.fileNameTextField.getText()));
	}

	public ISlideData loadExpressionFile(File f){
		return null;
	}


	public Vector load_GEOSeriesExpressionFile(File f) throws IOException {
		 float[] intensities = new float[2];
         
	       
	        final int rColumns = 1;
	        final int totalRows =smatrixflp.expressionTable.getRowCount();
	        final int totalColumns =smatrixflp.expressionTable.getColumnCount();
	      
	        int[] rows = new int[] {0, 1, 0};
	        int[] columns = new int[] {0, 1, 0};
	        int row,column;
	        row=column=1;
	        
	        
	        final int preSpotRows = this.smatrixflp.getXRow();
	      
	        final int preExperimentColumns = this.smatrixflp.getXColumn();
	        
	        final int spotCount=totalRows-preSpotRows; 
	       
	        int experimentCount = totalColumns - preExperimentColumns;
	      //  System.out.println("experiment count:"+experimentCount);
	        if (spotCount <= 0) {
	            JOptionPane.showMessageDialog(superLoader.getFrame(),  "There is no spot data available.",  "TDMS Load Error", JOptionPane.INFORMATION_MESSAGE);
	        }
	        
	       String[] moreFields = new String[preExperimentColumns];
	       String[] fieldNames = new String[preExperimentColumns];
	       
	       ISlideData[]slideData=new ISlideData[experimentCount];
	       SlideDataElement sde=null;
	       AffySlideDataElement affysde=null;
	       
	       SampleAnnotation sampAnn=new SampleAnnotation();
	       slideData[0] = new SlideData(spotCount, rColumns, sampAnn);
	       slideData[0].setSlideFileName(f.getAbsolutePath());
	      // slideData[0].setSlideDataName(sflp.expressionTable.getColumnName(0));
	       
	       for (int i=1; i<slideData.length; i++) {
	    	   sampAnn=new SampleAnnotation();
				slideData[i] = new FloatSlideData(slideData[0].getSlideMetaData(), spotCount, sampAnn);
	            slideData[i].setSlideFileName(f.getPath());
	           
	       }
	       
	       
	        //FieldNames    
	        for(int m=0;m<preExperimentColumns;m++) {
	       	 fieldNames[m]=(String)smatrixflp.expressionTable.getColumnName(m);
	        }
	        
	       
	        slideData[0].getSlideMetaData().appendFieldNames(fieldNames);
	      
	        
	        for(int i=0; i<slideData.length;i++) {
	        	String val=smatrixflp.expressionTable.getColumnName(i+preExperimentColumns);
				slideData[i].setSampleAnnotationLoaded(true);
				slideData[i].getSampleAnnotation().setAnnotation("Default Slide Name", val);
							
	        	//System.out.println("slidedataName:"+smatrixflp.expressionTable.getColumnName(i+preExperimentColumns));
	        	slideData[i].setSlideDataName(smatrixflp.expressionTable.getColumnName(i+preExperimentColumns));
				this.mav.getData().setSampleAnnotationLoaded(true);
	        	//System.out.println("slidedataName:"+sflp.expressionTable.getColumnName(i));
	        	//slideData[i].setSlideDataName(smatrixflp.expressionTable.getColumnName(i+preExperimentColumns));
	        	
	        }
	      //  System.out.println("prespotrows:"+preSpotRows);
	        for(int i=0;i<spotCount;i++){
	        	
	        	 for(int j=0;j<preExperimentColumns;j++) {
	             	moreFields[j]=(String)this.smatrixflp.expressionTable.getValueAt(i, j);
	             	//System.out.println("morefields:"+moreFields[j]);
	             }
	        	
	        	rows[0] = rows[2] = row;
	            columns[0] = columns[2] = column;
	            if (column == rColumns) {
	                column = 1;
	                //row++;
	                row=row+1;
	               
	                
	            } else {
	                //column++;
	            	column=column+1;
	            	//System.out.println("column:"+column);
	            }
	        	if(this.smatrixflp.twoColorArray.isSelected()) {
	        		sde = new SlideDataElement(String.valueOf(row+1),rows, columns, new float[2], moreFields);
	 	        	slideData[0].addSlideDataElement(sde);	
	        		
	        	}else {
	        		affysde = new AffySlideDataElement(String.valueOf(row+1),rows, columns, new float[2], moreFields);
	 	        	slideData[0].addSlideDataElement(affysde);
	        	
	        	}
	        	
	           
	            for(int j=0;j<slideData.length;j++) {
	            	
	            try {
	            intensities[0] = 1.0f;
	            intensities[1] =Float.parseFloat((String)smatrixflp.expressionTable.getValueAt(i,j+preExperimentColumns));
	            }catch(Exception e) {
	            	 intensities[1] =Float.NaN;	
	            }
	            
	           // System.out.println("intensities1:"+(String)sflp.expressionTable.getValueAt(i,j+1));
	          
	            slideData[j].setIntensities(i, intensities[0], intensities[1]);
	           
	            }
	            
	        }

	        Vector data=new Vector();
	        for(int i=0; i<slideData.length;i++)
	        data.add(slideData[i]);
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
				return "GEO Series Matrix File (*.txt)";
			}
		};

		return mevFileFilter;
	}

	public boolean checkLoadEnable() {

		// Currently, the only requirement is that a cell has been highlighted

		int tableRow = smatrixflp.getXRow() + 1; // Adjusted by 1 to account for the table header
		int tableColumn = smatrixflp.getXColumn();

		if (tableColumn < 0) return false;

		TableModel model = smatrixflp.getTable().getModel();
		
		if(!smatrixflp.twoColorArray.isSelected()&!smatrixflp.affymetrixArray.isSelected()) {
   		 String eMsg = "<html>Please select an array type..<br>" ;
		     JOptionPane.showMessageDialog(null, eMsg, "Warning", JOptionPane.INFORMATION_MESSAGE);
        	
		}
		
		if (tableRow >= 1 && tableColumn >= 0) {
			setLoadEnabled(true);
			return true;
		} else {
			setLoadEnabled(false);
			return false;
		}
	}


	public void setDataType(int data_Type){
		this.dataType=data_Type;
	}

	public int getDataType(){
		return this.dataType;
	}

	public boolean validateFile(File targetFile) {
		return true; // For now, no validation on SOFT_Affy Files
	}

	public JPanel getFileLoaderPanel() {
		return smatrixflp;
	}


	
	/**
	 * @process_GEOSeriesMatrixFile
	 * @param targetFile
	 * This function reads the input GEO Series Matrix file . It uses the
	 * label provided in the GEO files namely "sample_matrix_table_begin"
	 * and "sample_matrix_table_end" to figure out where the data starts
	 * and ends respectively.
	 * 
	 * 
	 * 
	 * If it is a two color array, The data type is set to
	 * IData.DATA_TYPE_RATIO_ONLY.
	 * 
	 * else data type is set to TMEV.DATA_TYPE_AFFY.
	 * 
	 * 
	 */
	
	

	public void process_GEOSeriesMatrixFile(File targetFile) {        
		
		this.smatrixflp.selectedFiles.setText(targetFile.getAbsolutePath());
		Vector<Vector<String>> dataVector = new Vector<Vector<String>>();
		Vector<String> rowVector = null;
		BufferedReader reader = null;
		String currentLine = null;
		//String tmp=null;
		Vector<String> columnHeaders = new Vector<String>();//store tabel header
		if (! validateFile(targetFile)) return;

		smatrixflp.setFileName(targetFile.getAbsolutePath());

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
			
			//Keep reading until you get to "series_matrix_table_begin"
			while(!currentLine.contains("series_matrix_table_begin")) {
				currentLine=reader.readLine();

			}


			//filter!sample_table_begin
			currentLine = reader.readLine(); 
			ss.init(currentLine);

			for (int i = 0; i < ss.countTokens()+1; i++) {
				columnHeaders.add(ss.nextToken());
			}

			model.setColumnIdentifiers(columnHeaders);
			int cnt = 0;
			while ((currentLine = reader.readLine()) != null && !currentLine.contains("series_matrix_table_end")) {
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

		smatrixflp.setTableModel(model);
        Point p = getFirstExpressionCell(dataVector);
        smatrixflp.setSelectedCell(p.x, p.y);
	}

	public String getFilePath() {
		return this.smatrixflp.fileNameTextField.getText();
	}

	public void openDataPath() {
		this.smatrixflp.openDataPath();
	}

	private class GEO_SeriesMatrixFileLoaderPanel extends JPanel {

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
		
		
		JPanel buttonPanel;
		JRadioButton twoColorArray;
		JRadioButton affymetrixArray;

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

		public GEO_SeriesMatrixFileLoaderPanel() {

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
			
			//Added to accomodate data type selection
			buttonPanel = new JPanel();
			buttonPanel.setLayout(new GridBagLayout());
			
			twoColorArray = new JRadioButton("Spotted DNA/cDNA Array");
			twoColorArray.setFocusPainted(false);
			twoColorArray.addActionListener(new EventHandler());
			affymetrixArray = new JRadioButton("Affymetrix Array");
			affymetrixArray.setFocusPainted(false);
			affymetrixArray.setSelected(true);
			affymetrixArray.addActionListener(new EventHandler());
			ButtonGroup bg = new ButtonGroup();
			bg.add(twoColorArray);
			bg.add(affymetrixArray);

			gba.add(buttonPanel, twoColorArray, 0, 0, 1, 1, 1, 0, GBA.H, GBA.C,
					new Insets(0, 20, 0, 5), 0, 0);
			gba.add(buttonPanel, affymetrixArray, 1, 0, 1, 1, 1, 0, GBA.H,
					GBA.C, new Insets(0, 20, 0, 5), 0, 0);



			fileSelectionPanel = new JPanel();
			fileSelectionPanel.setLayout(new GridBagLayout());
			fileSelectionPanel.setBorder(new TitledBorder(new EtchedBorder(), "File    (GEO Series Matrix Files)"));

			gba.add(fileSelectionPanel, dataSelection, 0, 0, 1, 1, 0, 0, GBA.B,GBA.C, new Insets(5, 5, 5, 5), 0, 0);
			gba.add(fileSelectionPanel, fileNameTextField, 1, 0, 1, 1, 1, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
			gba.add(fileSelectionPanel, browseButton1, 2, 0, GBA.RELATIVE, 1, 0,0, GBA.NONE, GBA.C, new Insets(5, 5, 5, 5), 0, 0);

			gba.add(fileSelectionPanel, fileSelectionLabel, 0, 2, 2, 1, 0, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
			gba.add(fileSelectionPanel, selectedFiles, 1, 2, 1, 1, 2, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0); 
			//Added to accomodate datatype selection
			gba.add(fileSelectionPanel, buttonPanel, 0, 3, 0, 0, 1, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
			
			expressionTable = new JTable();
			myCellRenderer = new ExpressionFileTableCellRenderer();
    		expressionTable.setDefaultRenderer(Object.class, myCellRenderer);
			expressionTable.setIntercellSpacing(new Dimension(1, 1));
			expressionTable.setShowHorizontalLines(false);
			expressionTable.setShowVerticalLines(true);
			expressionTable.setGridColor(Color.LIGHT_GRAY);
			expressionTable.setCellSelectionEnabled(true);
			expressionTable.setColumnSelectionAllowed(false);
			expressionTable.setRowSelectionAllowed(false);
			expressionTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			expressionTable.getTableHeader().setReorderingAllowed(false);

//			expressionTable = new JTable();
//			expressionTable.setDefaultRenderer(Object.class, myCellRenderer);
//			expressionTable.setGridColor(Color.LIGHT_GRAY);
//			expressionTable.setSize(300, 300);
//			expressionTable.setCellSelectionEnabled(true);
//			expressionTable.setColumnSelectionAllowed(false);
//			expressionTable.setRowSelectionAllowed(false);
//			expressionTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
//			expressionTable.getTableHeader().setReorderingAllowed(true);
			expressionTable.addMouseListener(new MouseAdapter() {
				public void mousePressed(MouseEvent event) {
				    setSelectedCell(expressionTable.rowAtPoint(event.getPoint()), expressionTable.columnAtPoint(event.getPoint()));
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
			gba.add(fileLoaderPanel, tablePanel, 		0, 7, 1, 6, 3, 6, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);



			gba.add(this, fileLoaderPanel,0, 0, 1, 1, 1, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);



		}
        private void setSelectedCell( int xR, int xC) {
            xRow = xR;
            xColumn = xC;
        myCellRenderer.setSelected(xRow, xColumn);
        expressionTable.repaint();
        checkLoadEnable();
    }
		public void openDataPath() {

		}

		public void onBrowse() {
			JFileChooser fileChooser=new JFileChooser(SuperExpressionFileLoader.DATA_PATH);
			int retVal=fileChooser.showOpenDialog(GEO_SeriesMatrixFileLoaderPanel.this);

			if(retVal==JFileChooser.APPROVE_OPTION) {
				File selectedFile=fileChooser.getSelectedFile();
				process_GEOSeriesMatrixFile(selectedFile);

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

		public void select_GEOSeriesMatrixFile() {
			JFileChooser jfc = new JFileChooser(SuperExpressionFileLoader.DATA_PATH);
			jfc.setFileFilter(getFileFilter());
			int activityCode = jfc.showDialog(this, "Select");

			if (activityCode == JFileChooser.APPROVE_OPTION) {
				File target = jfc.getSelectedFile();
				process_GEOSeriesMatrixFile(target);
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





		private class EventHandler implements ActionListener {
			public void actionPerformed(ActionEvent event) {
				Object source = event.getSource();
				if (source == browseButton1) {
					onBrowse();

				}else if (source == twoColorArray) {
					dataType=IData.DATA_TYPE_RATIO_ONLY;
					setDataType(dataType);
				} else if (source == affymetrixArray) {
					dataType=IData.DATA_TYPE_AFFY_ABS;
					setDataType(dataType);
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
