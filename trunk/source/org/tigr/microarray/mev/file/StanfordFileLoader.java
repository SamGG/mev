/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: StanfordFileLoader.java,v $
 * $Revision: 1.17 $
 * $Date: 2008-01-16 22:45:31 $
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
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.NoSuchElementException;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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
import org.tigr.microarray.mev.ISlideDataElement;
import org.tigr.microarray.mev.MultipleArrayData;
import org.tigr.microarray.mev.MultipleArrayViewer;
import org.tigr.microarray.mev.SlideData;
import org.tigr.microarray.mev.SlideDataElement;
import org.tigr.microarray.mev.TMEV;
import org.tigr.microarray.mev.annotation.AffymetrixAnnotationParser;
import org.tigr.microarray.mev.annotation.AnnotationDialog;
import org.tigr.microarray.mev.annotation.AnnotationFileReader;
import org.tigr.microarray.mev.annotation.MevAnnotation;
import org.tigr.microarray.mev.annotation.PublicURL;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.HelpWindow;
import org.tigr.microarray.mev.resources.ISupportFileDefinition;
import org.tigr.microarray.mev.resources.ResourcererAnnotationFileDefinition;
import org.tigr.microarray.mev.resources.SupportFileAccessError;
import org.tigr.microarray.util.ExpressionFileTableCellRenderer;
import org.tigr.microarray.util.FileLoaderUtility;

public class StanfordFileLoader extends ExpressionFileLoader {

	private GBA gba;
	private int dataType = IData.DATA_TYPE_AFFY_ABS;
	private boolean stop = false;
	private StanfordFileLoaderPanel sflp;
	ExpressionFileTableCellRenderer myCellRenderer;
	protected String[] moreFields = new String[] {};

	private ISlideData[] slideDataArray = null;
	private ISlideDataElement sde;

	private Hashtable _tempAnno = new Hashtable();
	private MultipleArrayViewer mav;

	protected MevAnnotation mevAnno = new MevAnnotation();


    public void setFilePath(String path) {
    	sflp.setFileName(path);
    	processStanfordFile(new File(path));
    }

	public StanfordFileLoader(SuperExpressionFileLoader superLoader) {
		super(superLoader);
		this.mav = superLoader.getArrayViewer();
		gba = new GBA();
		sflp = new StanfordFileLoaderPanel();
	}
	
    public Vector<ISlideData> loadStanfordExpressionFile(File f) throws IOException {
        final int selectedPreSpotRows = this.sflp.getXRow()+1;
        final int selectedPreExperimentColumns = this.sflp.getXColumn();         
    	return loadStanfordExpressionFile(f, selectedPreSpotRows, selectedPreExperimentColumns);
    }

    public Vector<ISlideData> loadExpressionFiles() throws IOException {
        return loadStanfordExpressionFile(new File(this.sflp.fileNameTextField.getText()), 
        		this.sflp.getXRow()+1,
        		this.sflp.getXColumn());
	}

	public ISlideData loadExpressionFile(File f) {
		return null;
	}
	public boolean canAutoLoad(File f) {return true;}
	
	/*
	 *  Handling of Stanford data has been altered in version 3.0 to permit loading of
	 *  "ratio" input without the creation of false cy3 and cy5.  cy5 values in data structures
	 *  are used to hold the input value.
	 *
	 *  getRatio methods are altered to return the value (held in cy5) rather than
	 *  taking log2(cy5/cy3).
	 */

    public Vector<ISlideData> loadStanfordExpressionFile(File f, int rowcoord, int colcoord) throws IOException {
    	int preSpotRows, preExperimentColumns;
    		preSpotRows = rowcoord;
    		preExperimentColumns = colcoord; 
		int numLines = this.getCountOfLines(f);

		int spotCount = numLines - preSpotRows;

		if (spotCount <= 0) {
            JOptionPane.showMessageDialog(superLoader.getFrame(),  "There is no spot data available.",  "TDMS Load Error", JOptionPane.INFORMATION_MESSAGE);
		}

		int[] rows = new int[] { 0, 1, 0 };
		int[] columns = new int[] { 0, 1, 0 };
		String value;
		float cy3, cy5;
		moreFields = new String[preExperimentColumns];

		final int rColumns = 1;
		final int rRows = spotCount;

		BufferedReader reader = new BufferedReader(new FileReader(f));
		StringSplitter ss = new StringSplitter((char) 0x09);
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
		 * "isAnnotationLoaded" is a boolean variable, which is set
		 * to "true" in the function onAnnotationFileBrowse().
		 * 
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
		if (PublicURL.loadURLs(TMEV.getConfigurationFile("annotation_URLs.txt")) != 0) {
			JOptionPane.showMessageDialog(new JFrame(), "URLs will not be loaded", "Warning", JOptionPane.WARNING_MESSAGE);
		}

		while ((currentLine = reader.readLine()) != null) {
			try {
				if (stop) {
					return null;
				}
				while (currentLine.endsWith("\t")) {
					currentLine = currentLine.substring(0, currentLine.length() - 1);
				}

				ss.init(currentLine);
				if (counter == 0) { // parse header
					int experimentCount = ss.countTokens() + 1 - preExperimentColumns;
					slideDataArray = new ISlideData[experimentCount];
					slideDataArray[0] = new SlideData(rRows, rColumns);
					slideDataArray[0].setSlideFileName(f.getPath());
					for (int i = 1; i < slideDataArray.length; i++) {
						slideDataArray[i] = new FloatSlideData(slideDataArray[0].getSlideMetaData(), spotCount);
						slideDataArray[i].setSlideFileName(f.getPath());
					}
					//get Field Names
					String[] fieldNames = new String[preExperimentColumns];
					for (int i = 0; i < preExperimentColumns; i++) {
						fieldNames[i] = ss.nextToken();
					}
					slideDataArray[0].getSlideMetaData().setFieldNames(fieldNames);

					for (int i = 0; i < experimentCount; i++) {
						slideDataArray[i].setSlideDataName(ss.nextToken());
					}
				} else if (counter >= preSpotRows) { // data rows
					rows[0] = rows[2] = row;
					columns[0] = columns[2] = column;
					if (column == rColumns) {
						column = 1;
						row = row + 1;
					} else {
						column++;
					}
					for (int i = 0; i < preExperimentColumns; i++) {
						moreFields[i] = ss.nextToken();
					}

					String cloneName = moreFields[0];
					if (_tempAnno.size() != 0 && getDataType() == TMEV.DATA_TYPE_AFFY) {

             	
						if (((MevAnnotation) _tempAnno.get(cloneName)) != null) {
							MevAnnotation mevAnno = (MevAnnotation) _tempAnno.get(cloneName);

                		sde = new AffySlideDataElement(String.valueOf(row+1), rows, columns, new float[2], moreFields, mevAnno);
						} else {
                 /*
               	  * Sarita: clone ID explicitly set here because if the data file
               	  * has a probe (for eg. Affy house keeping probes) for which Resourcerer
               	  * does not have annotation, MeV would still work fine. NA will be
               	  * appended for the rest of the fields. 
							 */
							MevAnnotation mevAnno = new MevAnnotation();
							mevAnno.setCloneID(cloneName);
                    sde = new AffySlideDataElement(String.valueOf(row+1), rows, columns, new float[2], moreFields, mevAnno);

						}
                } else {
						sde = new SlideDataElement(String.valueOf(row + 1), rows, columns, new float[2], moreFields);
					}

                
                
					slideDataArray[0].addSlideDataElement(sde);

					for (int i = 0; i < slideDataArray.length; i++) {
						cy3 = 1f; //set cy3 to a default value of 1.
						try {
							value = ss.nextToken();
							cy5 = Float.parseFloat(value); //set cy5 to hold the value
							//getRatio methods will return cy5
							//for Stanford data type
						} catch (Exception e) {
							cy3 = 0;
							cy5 = Float.NaN;
						}
						slideDataArray[i].setIntensities(counter - preSpotRows, cy3, cy5);
					}
				} else {
					//we have additional sample annoation

					//advance to sample key
					for (int i = 0; i < preExperimentColumns - 1; i++) {
						ss.nextToken();
					}
					String key = ss.nextToken();

					for (int j = 0; j < slideDataArray.length; j++) {
						slideDataArray[j].addNewSampleLabel(key, ss.nextToken());
					}
				}

				this.setFileProgress(counter);

				counter++;
			} catch (NoSuchElementException nsee) {
				//Blank or corrupted line. Ignore.
				//System.out.println("caught a blank line");
			}
		}
		reader.close();

        Vector<ISlideData> data = new Vector<ISlideData>(slideDataArray.length);

		for (int i = 0; i < slideDataArray.length; i++)
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
				return "Tab Delimited, Multiple Sample Files (TDMS) (*.txt)";
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
			//  System.out.print(model.getColumnName(i) + (i + 1 == tableColumn ? "\n" : ", "));
			fieldSummary += model.getColumnName(i) + (i + 1 == tableColumn ? "" : ", ");
		}

        
		if (!sflp.twoColorArray.isSelected() & !sflp.singleColorArray.isSelected()) {
			String eMsg = "<html>Please select an array type..<br>";
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

	public boolean validateFile(File targetFile) {
		return true; // For now, no validation on Stanford Files
	}

	public JPanel getFileLoaderPanel() {
		return sflp;
	}

	public void processStanfordFile(File targetFile) {

		Vector<String> columnHeaders = new Vector<String>();
		Vector<Vector<String>> dataVector = new Vector<Vector<String>>();
		Vector<String> rowVector = null;
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
			while (currentLine.endsWith("\t")) {
				currentLine = currentLine.substring(0, currentLine.length() - 1);
			}
			ss.init(currentLine);

			for (int i = 0; i < ss.countTokens() + 1; i++) {
				columnHeaders.add(ss.nextToken());
			}

			model.setColumnIdentifiers(columnHeaders);
			int cnt = 0;
			while ((currentLine = reader.readLine()) != null && cnt < 100) {
				cnt++;
				ss.init(currentLine);
				rowVector = new Vector<String>();
				for (int i = 0; i < ss.countTokens() + 1; i++) {
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
        Point p = getFirstExpressionCell(dataVector);
        sflp.setSelectedCell(p.x, p.y);
	}

	public String getFilePath() {
		return this.sflp.fileNameTextField.getText();
	}

	public String getAnnotationFilePath() {
		return this.sflp.getAnnFilePath();
	}

	public void openDataPath() {
	}


	public void setDataType(int data_Type) {
		if (data_Type != -1)
			dataType = data_Type;
		else
			dataType = IData.DATA_TYPE_RATIO_ONLY;
	}

	public int getDataType() {
		return dataType;
	}
	public boolean isAnnotationSelected() {
		return sflp.adh.annotationSelected;
	}
    
	/**
	 * 
	 * @author SARITA NAIR
	 * 
	 * 
	 */

	private class StanfordFileLoaderPanel extends JPanel {

		JTextField fileNameTextField;

		JTextField selectedFiles;
		JTable expressionTable;
		JLabel instructionsLabel;
		JScrollPane tableScrollPane;
		JPanel tablePanel;
		JPanel fileLoaderPanel;
		JTextField fieldsTextField;
		JPanel fieldsPanel;
		JSplitPane splitPane;

		JList availableList;
		JScrollPane availableScrollPane;

		private int xRow = -1;
		private int xColumn = -1;

		JPanel fileSelectionPanel;
		JLabel fileSelectionLabel, dataSelection;
		JButton browseButton1;
		JPanel buttonPanel;
		JRadioButton twoColorArray;
		JRadioButton singleColorArray;
		
		AnnotationDownloadHandler adh;

		protected EventListener eventListener;

		public StanfordFileLoaderPanel() {
			super();
			
			adh = new AnnotationDownloadHandler(superLoader.viewer.getResourceManager(), superLoader.annotationLists, superLoader.defaultSpeciesName, superLoader.defaultArrayName);
			
			eventListener = new EventListener();
			setLayout(new GridBagLayout());

			fileNameTextField = new JTextField();
			fileNameTextField.setEditable(false);
			fileNameTextField.setForeground(Color.black);
			fileNameTextField.setFont(new Font("monospaced", Font.BOLD, 12));

			selectedFiles = new JTextField();
			selectedFiles.setEditable(false);
			selectedFiles.setForeground(Color.black);
			selectedFiles.setFont(new Font("monospaced", Font.BOLD, 12));

			fileSelectionLabel = new JLabel();
			fileSelectionLabel.setForeground(java.awt.Color.BLACK);
			String fileTypeChoices = "<html> Selected files </html>";
			fileSelectionLabel.setText(fileTypeChoices);

			dataSelection = new JLabel();
			dataSelection.setForeground(java.awt.Color.BLACK);
			String chooseFile = "<html>Select expression data file</html>";
			dataSelection.setText(chooseFile);

			fileSelectionPanel = new JPanel();
			fileSelectionPanel.setLayout(new GridBagLayout());
			fileSelectionPanel.setBorder(new TitledBorder(new EtchedBorder(), "File    (Tab Delimited Multiple Sample (*.*))"));

			browseButton1 = new JButton("Browse");
			browseButton1.addActionListener(eventListener);
			browseButton1.setSize(100, 30);
			browseButton1.setPreferredSize(new Dimension(100, 30));

			buttonPanel = new JPanel();
			buttonPanel.setLayout(new GridBagLayout());

			twoColorArray = new JRadioButton("Two-color Array", false);
			twoColorArray.setFocusPainted(false);
			twoColorArray.addActionListener(new EventListener());

			singleColorArray = new JRadioButton("Single-color Array");
			singleColorArray.setFocusPainted(false);
			singleColorArray.addActionListener(new EventListener());
			singleColorArray.setSelected(true);

			ButtonGroup bg = new ButtonGroup();
			bg.add(twoColorArray);
			bg.add(singleColorArray);

			gba.add(buttonPanel, twoColorArray, 0, 0, 1, 1, 1, 0, GBA.H, GBA.C,
					new Insets(0, 20, 0, 5), 0, 0);
			gba.add(buttonPanel, singleColorArray, 1, 0, 1, 1, 1, 0, GBA.H,
					GBA.C, new Insets(0, 20, 0, 5), 0, 0);

			gba.add(fileSelectionPanel, dataSelection, 0, 0, 1, 1, 0, 0, GBA.B, GBA.C, new Insets(2, 2, 2, 2), 0, 0);


			gba.add(fileSelectionPanel, fileNameTextField, 1, 0, 1, 1, 1, 0, GBA.H, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
			gba.add(fileSelectionPanel, browseButton1, 2, 0, GBA.RELATIVE, 1, 0, 0, GBA.NONE, GBA.C, new Insets(2, 2, 2, 2), 0, 0);

			gba.add(fileSelectionPanel, fileSelectionLabel, 0, 2, 2, 1, 0, 0, GBA.H, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
			gba.add(fileSelectionPanel, selectedFiles, 1, 2, 1, 1, 2, 0, GBA.H, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
			gba.add(fileSelectionPanel, buttonPanel, 0, 3, 0, 0, 1, 0, GBA.H, GBA.C, new Insets(2, 2, 2, 2), 0, 0);

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

			
			gba.add(fileLoaderPanel, fileSelectionPanel, 0, 0, 1, 1, 1, 1, GBA.B, GBA.C, new Insets(2, 2, 2, 2), 0, 0);
			gba.add(fileLoaderPanel, adh.getAnnotationLoaderPanel(gba), 0, 2, 1, 1, 1, 1, GBA.B, GBA.C, new Insets(2, 2, 2, 2), 0, 0);//Uncomment when you add annotation for non affy
			gba.add(fileLoaderPanel, tablePanel, 0, 3, 1, 6, 3, 6, GBA.B, GBA.C, new Insets(2, 2, 2, 2), 0, 0);

			gba.add(this, fileLoaderPanel, 0, 0, 1, 1, 1, 1, GBA.B, GBA.C, new Insets(2, 2, 2, 2), 0, 0);

		}
		public boolean isAnnotationSelected() {
			return adh.annotationSelected;
		}
		public String getAnnFilePath() {
			return adh.getAnnFilePath();
		}
        private void setSelectedCell(int xR, int xC) {
			xRow = xR;
			xColumn = xC;
			myCellRenderer.setSelected(xRow, xColumn);
			expressionTable.repaint();
			checkLoadEnable();
		}
        
		public void onBrowse() {
			JFileChooser fileChooser = new JFileChooser(SuperExpressionFileLoader.DATA_PATH);
			int retVal = fileChooser.showOpenDialog(StanfordFileLoaderPanel.this);

			if (retVal == JFileChooser.APPROVE_OPTION) {
				File selectedFile = fileChooser.getSelectedFile();
				processStanfordFile(selectedFile);
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

		public void setFileName(String fileName) {
			fileNameTextField.setText(fileName);
			selectedFiles.setText(fileName);
		}

		public void setTableModel(TableModel model) {
			expressionTable.setModel(model);
			int numCols = expressionTable.getColumnCount();
			for (int i = 0; i < numCols; i++) {
				expressionTable.getColumnModel().getColumn(i).setMinWidth(75);
			}
		}

		public void setFieldsText(String fieldsText) {
			//  fieldsTextField.setText(fieldsText);
		}

		private class EventListener implements ActionListener {
			public void actionPerformed(ActionEvent event) {
				Object source = event.getSource();
				if (source == browseButton1) {
					onBrowse();
				} else if (source == twoColorArray) {
					dataType = IData.DATA_TYPE_RATIO_ONLY;
					setDataType(dataType);
					adh.setDownloadEnabled(!twoColorArray.isSelected());
				} else if (source == singleColorArray) {
					dataType = IData.DATA_TYPE_AFFY_ABS;
					setDataType(dataType);
					adh.setDownloadEnabled(singleColorArray.isSelected());
				}
			}
		}

	}

	@Override
	public void setAnnotationFilePath(String filePath) {
		sflp.adh.setAnnFilePath(filePath);
		sflp.adh.annotationSelected = true;
		
	}
}
