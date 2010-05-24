/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * BNClassificationEditor.java
 *
 */
package org.tigr.microarray.mev.cluster.gui.impl.bn;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.tigr.microarray.mev.TMEV;
import org.tigr.microarray.mev.cgh.CGHGuiObj.CharmDialogs.ExampleFileFilter;
import org.tigr.microarray.mev.cluster.clusterUtil.Cluster;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.impl.bn.prepareXMLBif.PrepareXMLBifModule;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog;
import org.tigr.util.StringSplitter;
import org.xml.sax.SAXException;

import weka.classifiers.Evaluation;
import weka.classifiers.bayes.BayesNet;
import weka.estimators.Estimator;
//import weka.gui.GUIChooser;
import weka.classifiers.bayes.net.search.SearchAlgorithm;
import weka.classifiers.bayes.net.estimate.BayesNetEstimator;
/**
 *
 */
public class BNClassificationEditor extends JDialog {// JFrame {

	private int result = JOptionPane.CANCEL_OPTION;
	final IFramework framework;
	IData data;
	boolean classifyGenes;
	private boolean stopHere = true;
	private boolean nextPressed = false;
	private boolean incompatible = false;
	//public static GUIChooser m_chooser;
	
	private JTable BNClassTable;
	private BNClassTableModel kModel;
	private JMenuItem[] classItem, labelsAscItem, labelsDescItem;
	private JButton nextButton, cancelButton, loadButton, saveSettingsButton;
	//SortListener sorter;
	private Object[][] origData;
	private final String basePath;
	private int numClasses, numExps, numGenes;
	private String[] fieldNames;
	private File labelFile = null;
	
	/** Creates a new instance of BNClassificationEditor */
	public BNClassificationEditor(final IFramework framework, boolean classifyGenes,int numClasses, String path) {
		super(framework.getFrame(), true);
		this.setTitle("Classification Editor: Assign Samples to group(s)");
		//mainFrame = (JFrame)(framework.getFrame());
		//setBounds(0, 0, 550, 800);
		int width = 300;
		int height = 300;

		setBackground(Color.white);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.framework = framework;
		this.data = framework.getData();
		this.numGenes = data.getFeaturesSize();
		this.numExps = data.getFeaturesCount();
		this.fieldNames = data.getFieldNames();
		this.classifyGenes = classifyGenes;
		this.numClasses = numClasses;
		//this.clust = cl;
		if(numClasses <= 1)
			width = 360;
		else if (numClasses > 1 && numClasses <= 2)
			width = 390;
		else if (numClasses > 2 && numClasses <= 3)
			width = 450;
		else if (numClasses > 3 && numClasses <= 5)
			width = 550;
		else
			width = 600;

		if(this.numExps <= 5)
			height = 200;
		else if (this.numExps > 5 && this.numExps <= 10)
			height = 250;
		else if (this.numExps > 10 && this.numExps <= 15)
			height = 300;
		else if (this.numExps > 15 && this.numExps < 20)
			height = 350;
		else 
			height = 450;

		setBounds(0,0,width,height);

		
		basePath = path+System.getProperty("file.separator");
		
		//menuBar = new JMenuBar();
		//this.setJMenuBar(menuBar);  

		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;

		JPanel pane = new JPanel();
		pane.setLayout(gridbag);

		JPanel tablePanel = new JPanel();
		GridBagLayout grid1 = new GridBagLayout();
		tablePanel.setLayout(grid1);

		kModel = new BNClassTableModel();
		BNClassTable = new JTable(kModel);
		BNClassTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		TableColumn column = null;
		for (int i = 0; i < kModel.getColumnCount(); i++) {
			column = BNClassTable.getColumnModel().getColumn(i);
			if(i == (kModel.getColumnCount()-1))
				//Resize the last column to make it bigger for sample names.
				column.setMinWidth(190);
			else
				column.setMinWidth(30);
		}
		BNClassTable.setColumnModel(new BNClassTableColumnModel(BNClassTable.getColumnModel()));
		BNClassTable.getModel().addTableModelListener(new ClassSelectionListener());

		// searchDialog = new KNNCSearchDialog(this, BNClassTable, numClasses, false); //persistent search dialog     
		//JOptionPane.getFrameForComponent(this)
		JScrollPane scroll = new JScrollPane(BNClassTable);
		buildConstraints(constraints, 0, 0, 1, 1, 100, 100);
		grid1.setConstraints(scroll, constraints);
		tablePanel.add(scroll);

		buildConstraints(constraints, 0, 0, 2, 1, 100, 90);
		gridbag.setConstraints(tablePanel, constraints);
		pane.add(tablePanel);

		final JFileChooser fc1 = new JFileChooser();
		final JFileChooser fc2 = new JFileChooser();
		ExampleFileFilter filter = new ExampleFileFilter("txt");
		fc2.setFileFilter(filter);

		//TODO The following block may not be needed
		String dataPath = TMEV.getDataPath();
		File pathFile = TMEV.getFile("data/bn");
		if(dataPath != null) {
			pathFile = new File(dataPath);
			if(!pathFile.exists())
				pathFile = TMEV.getFile("data/bn");
		}
		//End unnecessary Block

		//fc1.setCurrentDirectory(new File(pathFile.getAbsolutePath()));
		fc1.setCurrentDirectory(new File(basePath));
		fc1.setDialogTitle("Open Classification");

		//fc2.setCurrentDirectory(new File(pathFile.getAbsolutePath()));
		fc2.setCurrentDirectory(new File(basePath));
		fc2.setDialogTitle("Save Classification");

		JPanel bottomPanel = new JPanel();
		bottomPanel.setBorder(new EtchedBorder());
		bottomPanel.setBackground(Color.white);
		GridBagLayout grid2 = new GridBagLayout();
		bottomPanel.setLayout(grid2);
		loadButton=new JButton("Load Settings");
		loadButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt) {
				int returnVal = fc1.showOpenDialog(BNClassificationEditor.this);  
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fc1.getSelectedFile();  
					loadFromFile(file);
				}
			}
		});
		saveSettingsButton=new JButton("Save Settings");
		saveSettingsButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt) {
				int returnVal = fc2.showOpenDialog(BNClassificationEditor.this);  
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					labelFile = fc2.getSelectedFile();
					//saveToFile(labelFile);
					//fileOpened = true;
					//nextPressed = false;                        

				}
			}
		});
		cancelButton=new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt) {
				result = JOptionPane.CANCEL_OPTION;
				dispose();
			}
		});
		nextButton = new JButton("OK");
		nextButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				onOk();
			}
		});
		//cleanUpFile();
		constraints.fill = GridBagConstraints.HORIZONTAL; 

		buildConstraints(constraints, 0, 0, 1, 1, 0, 0);
		grid2.setConstraints(loadButton, constraints);
		bottomPanel.add(loadButton);

		buildConstraints(constraints, 1, 0, 1, 1, 0, 0);
		grid2.setConstraints(saveSettingsButton, constraints);
		bottomPanel.add(saveSettingsButton);
		buildConstraints(constraints, 2, 0, 1, 1, 0, 0);
		grid2.setConstraints(cancelButton, constraints);
		bottomPanel.add(cancelButton); 

		buildConstraints(constraints, 3, 0, 1, 1, 0, 0);
		grid2.setConstraints(nextButton, constraints);
		bottomPanel.add(nextButton);        

		constraints.fill = GridBagConstraints.HORIZONTAL;
		buildConstraints(constraints, 0, 1, 1, 1, 0, 0);
		gridbag.setConstraints(bottomPanel, constraints);
		pane.add(bottomPanel);

		this.setContentPane(pane);

		if (classifyGenes) {
			labelsAscItem = new JMenuItem[fieldNames.length];
			labelsDescItem =  new JMenuItem[fieldNames.length];
			for (int i = 0; i < fieldNames.length; i++) {
				labelsAscItem[i] = new JMenuItem(fieldNames[i]);
				labelsDescItem[i] = new JMenuItem(fieldNames[i]);
			}
		} else {
			labelsAscItem = new JMenuItem[1];
			labelsAscItem[0] = new JMenuItem("Sample Name");
			labelsDescItem = new JMenuItem[1];
			labelsDescItem[0] = new JMenuItem("Sample Name");
		}

		for (int i = 0; i < labelsAscItem.length; i++) {
			labelsAscItem[i].addActionListener(new SortListener(true, false));
			labelsDescItem[i].addActionListener(new SortListener(false, false));
		}

		//classItem = new JMenuItem[numClasses + 1];
		classItem = new JMenuItem[numClasses];

		for (int i = 0; i < numClasses; i++) {
			classItem[i] = new JMenuItem("Class " + (i + 1));
		}

		//classItem[numClasses] = new JMenuItem("Neutral");

		for (int i = 0; i < classItem.length; i++) {
			classItem[i].addActionListener(new AssignListener());
		}
	}
	/** Closes the dialog */
	private void closeDialog(WindowEvent evt) {
		setVisible(false);
		//cancelPressed = true;                        
		dispose();
	}

	/**
	 * Core function to run BN with weka on the selected cluster
	 * @param cl
	 */
	protected void onOk() {
		result = JOptionPane.OK_OPTION;
		dispose();
	}

	/**
	 * 
	 * @param binNum
	 * @param path
	 * @param bootStrap
	 * @param numIter
	 * @return
	 */
	public Properties tranSaveWeka(String binNum,String path, boolean bootStrap, int numIter){
		return PrepareArrayDataModule.prepareArrayData(path+BNConstants.SEP+"wekaData", binNum, bootStrap, numIter, this.numClasses); 
	}

	/**
	 * 
	 * @param gbc
	 * @param gx
	 * @param gy
	 * @param gw
	 * @param gh
	 * @param wx
	 * @param wy
	 */
	void buildConstraints(GridBagConstraints gbc, int gx, int gy, int gw, int gh, int wx, int wy) {
		gbc.gridx = gx;
		gbc.gridy = gy;
		gbc.gridwidth = gw;
		gbc.gridheight = gh;
		gbc.weightx = wx;
		gbc.weighty = wy;
	}

	/**
	 * 
	 * @param visible
	 */
	public int showModal(boolean visible) {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);
		//showWarningMessage();     
		super.setVisible(visible);
		return result;
	}

	@SuppressWarnings("serial")
	class BNClassTableModel extends AbstractTableModel {
		String[] columnNames;
		Object tableData[][];
		int indexLastClass;

		public BNClassTableModel() {
			//indexLastClass = numClasses;
			indexLastClass = numClasses-1;
			if (classifyGenes) {
				//columnNames = new String[fieldNames.length + numClasses + 2];
				columnNames = new String[fieldNames.length + numClasses + 1];
				columnNames[0] = "Index";
				for (int i = 0; i < numClasses; i++) {
					columnNames[i + 1] = "Class " + (i+1);
				}
				
				//columnNames[numClasses + 1] = "Neutral";

				for (int i = 0; i < fieldNames.length; i++) {
					//columnNames[numClasses + 2 + i] = fieldNames[i];
					columnNames[numClasses + 1 + i] = fieldNames[i];
				}

				tableData = new Object[numGenes][columnNames.length];

				for (int i = 0; i < tableData.length; i++) {
					for (int j = 0; j < columnNames.length; j++) {
						if (j == 0) {
							tableData[i][j] = new Integer(i);
						//} else if ((j > 0) && (j < (numClasses + 1))) {
						} else if ((j > 0) && (j < (numClasses))) {
							tableData[i][j] = new Boolean(false);
						//} else if (j == numClasses + 1) {
						} else if (j == numClasses) {
							tableData[i][j] = new Boolean(true);
						} else {
							//tableData[i][j] = data.getElementAttribute(i, j - (numClasses + 2));
							tableData[i][j] = data.getElementAttribute(i, j - (numClasses + 1));
						}
					}
				}

			} else { // (!classifyGenes)
				//columnNames = new String[numClasses + 3];
				columnNames = new String[numClasses + 2];
				columnNames[0] = "Index";
				for (int i = 0; i < numClasses; i++) {
					columnNames[i + 1] = "Class " + (i+1);
				}
				//columnNames[numClasses + 1] = "Neutral";
				//columnNames[numClasses + 2] = "Sample Name";
				columnNames[numClasses + 1] = "Sample Name";
				tableData = new Object[numExps][columnNames.length];

				for (int i = 0; i < tableData.length; i++) {
					for (int j = 0; j < columnNames.length; j++) {
						if (j == 0) {
							tableData[i][j] = new Integer(i);
						//} else if ((j > 0) && (j < (numClasses + 1))) {
						} else if ((j > 0) && (j < (numClasses))) {
							tableData[i][j] = new Boolean(false);
						//} else if (j == numClasses + 1) {
						} else if (j == numClasses) {
							tableData[i][j] = new Boolean(true);
						//} else if (j == numClasses + 2) {
						} else if (j == numClasses + 1) {
							tableData[i][j] = data.getFullSampleName(i);
						}
					}
				}
			}

			origData = new Object[tableData.length][tableData[0].length];

			for (int i = 0; i < tableData.length; i++) {
				for (int j = 0; j < tableData[0].length; j++) {
					origData[i][j] = tableData[i][j];
				}
			}
		}


		public int getColumnCount() {
			return columnNames.length;
		}

		public int getRowCount() {
			return tableData.length;
		}

		public String getColumnName(int col) {
			return columnNames[col];
		}

		public int getColumnIndex(String name) {
			int i;
			for (i = 0; i < columnNames.length; i++) {
				if (columnNames[i].equals(name)) {
					break;
				}
			}
			if (i < columnNames.length) {
				return i;
			} else {
				return -1;
			}
		}

		public Object getValueAt(int row, int col) {
			return tableData[row][col];
		}

		public void setValueAt(Object value, int row, int col) {
			tableData[row][col] = value;
			//fireTableCellUpdated(row, col);
			this.fireTableChanged(new TableModelEvent(this, row, row, col));
		}


		public Class getColumnClass(int c) {
			if (c == 0) {
				return java.lang.Integer.class;
			//} else if ((c > 0) && (c <= (numClasses + 1))) {
			} else if ((c > 0) && (c <= (numClasses))) {
				return java.lang.Boolean.class;
			} else {
				return getValueAt(0, c).getClass();
			}
		}


		public boolean isCellEditable(int row, int col) {
			//Note that the data/cell address is constant,
			//no matter where the cell appears onscreen.
			//if ((col > 0) && (col <= (numClasses + 1))) {
			if ((col > 0) && (col <= (numClasses))) {
				return true;
			} else {
				return false;
			}
		}
	}


	class BNClassTableColumnModel implements TableColumnModel {

		TableColumnModel tcm;

		public BNClassTableColumnModel(TableColumnModel TCM) {
			this.tcm = TCM;
		}

		public void addColumn(javax.swing.table.TableColumn tableColumn) {
			tcm.addColumn(tableColumn);
		}

		public void addColumnModelListener(javax.swing.event.TableColumnModelListener tableColumnModelListener) {
			tcm.addColumnModelListener(tableColumnModelListener);
		}

		public javax.swing.table.TableColumn getColumn(int param) {
			return tcm.getColumn(param);
		}

		public int getColumnCount() {
			return tcm.getColumnCount();
		}

		public int getColumnIndex(Object obj) {
			return tcm.getColumnIndex(obj);
		}

		public int getColumnIndexAtX(int param) {
			return tcm.getColumnIndexAtX(param);
		}

		public int getColumnMargin() {
			return tcm.getColumnMargin();
		}

		public boolean getColumnSelectionAllowed() {
			return tcm.getColumnSelectionAllowed();
		}

		public java.util.Enumeration getColumns() {
			return tcm.getColumns();
		}

		public int getSelectedColumnCount() {
			return tcm.getSelectedColumnCount();
		}

		public int[] getSelectedColumns() {
			return tcm.getSelectedColumns();
		}

		public javax.swing.ListSelectionModel getSelectionModel() {
			return tcm.getSelectionModel();
		}

		public int getTotalColumnWidth() {
			return tcm.getTotalColumnWidth();
		}

		public void moveColumn(int from, int to) {
			//if (from <= (numClasses + 1) || to <= (numClasses + 1)) {
			if (from <= (numClasses) || to <= (numClasses)) {
				return;
			} else {
				tcm.moveColumn(from, to);
			}
		}

		public void removeColumn(javax.swing.table.TableColumn tableColumn) {
			tcm.removeColumn(tableColumn);
		}

		public void removeColumnModelListener(javax.swing.event.TableColumnModelListener tableColumnModelListener) {
			tcm.removeColumnModelListener(tableColumnModelListener);
		}

		public void setColumnMargin(int param) {
			tcm.setColumnMargin(param);
		}

		public void setColumnSelectionAllowed(boolean param) {
			tcm.setColumnSelectionAllowed(param);
		}

		public void setSelectionModel(javax.swing.ListSelectionModel listSelectionModel) {
			tcm.setSelectionModel(listSelectionModel);
		}

	}    


	class ClassSelectionListener implements TableModelListener {

		public void tableChanged(TableModelEvent tme) {
			//TableModel tabMod = (TableModel)tme.getSource();
			int selectedCol = tme.getColumn(); //
			int selectedRow = tme.getFirstRow(); //

			//if ((selectedCol < 1) || (selectedCol > (numClasses + 1) )) {
			if ((selectedCol < 1) || (selectedCol > (numClasses) )) {
				return;
			}

			if( verifySelected(selectedRow, selectedCol)){
				changeNeighbors(selectedRow, selectedCol);
			}

			int origDataRow = ((Integer)(kModel.getValueAt(selectedRow, 0))).intValue();

			origData[origDataRow][selectedCol] = new Boolean(true);

			//for (int i = 1; i <= (numClasses + 1); i++) {
			for (int i = 1; i <= (numClasses); i++) {
				if (i != selectedCol) {
					origData[origDataRow][i] = new Boolean(false);
				}
			}
		}

		private void changeNeighbors(int first, int col){
			//for (int i = 1; i <= (numClasses + 1); i++) {
			for (int i = 1; i <= (numClasses); i++) {
				if (i != col) {
					BNClassTable.setValueAt(new Boolean(false), first, i);
					//origData[first][i] = new Boolean(false); 
				}
			}
		}

		private boolean verifySelected(int row, int col){

			boolean selVal = ((Boolean)BNClassTable.getValueAt(row,col)).booleanValue();
			//boolean value1, value2;

			if(selVal == true){
				return true;
			} else {
				Vector truthValues = new Vector();
				//for (int i = 1; i <=(numClasses + 1); i++) {
				for (int i = 1; i <=(numClasses); i++) {
					if (i != col) {
						boolean value = ((Boolean)(BNClassTable.getValueAt(row,i))).booleanValue();
						truthValues.add(new Boolean(value));
					}
				}
				boolean val1 = true;
				for (int i = 0; i < truthValues.size(); i++) {
					boolean val2 = ((Boolean)(truthValues.get(i))).booleanValue();
					if (val2 == true) {
						val1 = false;
						break;
					}
				}

				if (val1 == true) {
					BNClassTable.setValueAt(new Boolean(true), row, col);
					//origData[row][col] = new Boolean(true);
				}

			}
			return false;

			/*else {
                BNClassTable.setValueAt(new Boolean(true), selectedRow, selectedCol);

                for (int i = 1; i <= (numClasses + 1); i++) {
                    if (i != selectedCol) {
                        BNClassTable.setValueAt(new Boolean(false), selectedRow, i);
                    }
                }

            }
			 */

		}

	}

	public void sortByColumn(int column, boolean ascending, boolean originalOrder) {
		if (originalOrder) {
			//double[] indices = new int[kModel.getRowCount()];
			//for (int i = 0; i < kModel.getRowCount(); i++) {
			//indices[i] = ((Integer)(kModel.getValueAt(i, 0))).doubleValue();
			/*
                QSort sortIndices = new QSort(indices);
                int[] sorted = sortIndices.getOrigIndx();
			 */
			Object[][] sortedData = new Object[kModel.getRowCount()][kModel.getColumnCount()];

			for (int i = 0; i < sortedData.length; i++) {
				for (int j = 0; j < sortedData[0].length; j++) {
					sortedData[i][j] = origData[i][j];
				}
			}

			for (int i = 0; i < sortedData.length; i++) {
				for (int j = 0; j < sortedData[0].length; j++) {
					kModel.setValueAt(sortedData[i][j], i, j);
				}
				validateTable(sortedData, i);
			}
			return;
			//}
			/*
            for (int i = 0; i < kModel.getRowCount(); i++) {
                for (int j = 0; j < kModel.getColumnCount(); j++) {
                    kModel.setValueAt(origData[i][j], i, j);
                }
                validateTable(origData, i);
            }
            return;
			 */
		}
		if ((column < 0)|| (column > kModel.getColumnCount())) {
			return;
		}
		Object[][] sortedData = new Object[kModel.getRowCount()][kModel.getColumnCount()];
		//float[] origArray = new float[kModel.getRowCount()];
		SortableField[] sortFields = new SortableField[kModel.getRowCount()];

		for (int i = 0; i < sortFields.length; i++) {
			int origDataRow = ((Integer)(kModel.getValueAt(i, 0))).intValue();
			sortFields[i] = new SortableField(origDataRow, column);
		}
		Arrays.sort(sortFields);
		int[] sortedIndices = new int[sortFields.length];
		for (int i = 0; i < sortedIndices.length; i++) {
			sortedIndices[i] = sortFields[i].getIndex();
		}
		if (!ascending) {
			sortedIndices = reverse(sortedIndices);
		}

		for (int i = 0; i < sortedData.length; i++) {
			for (int j = 0; j < sortedData[i].length; j++) {
				//sortedData[i][j] = tModel.getValueAt(sortedMeansAIndices[i], j);
				sortedData[i][j] = origData[sortedIndices[i]][j];
			}
		}

		for (int i = 0; i < sortedData.length; i++) {
			for (int j = 0; j < sortedData[i].length; j++) {
				kModel.setValueAt(sortedData[i][j], i, j);
			}
			validateTable(sortedData, i);
		}

		BNClassTable.removeRowSelectionInterval(0, BNClassTable.getRowCount() - 1);
	}

	private int[] reverse(int[] arr) {
		int[] revArr = new int[arr.length];
		int  revCount = 0;
		int count = arr.length - 1;
		for (int i=0; i < arr.length; i++) {
			revArr[revCount] = arr[count];
			revCount++;
			count--;
		}
		return revArr;
	}

	private void sortByClassification() {
		//Vector[] classVectors = new Vector[numClasses + 1];
		Vector[] classVectors = new Vector[numClasses];
		for (int i = 0; i < classVectors.length; i++) {
			classVectors[i] = new Vector();
		}

		for (int i = 0; i < kModel.getRowCount(); i++) {
			//for (int j = 1; (j <= numClasses + 1); j++) {
			for (int j = 1; (j <= numClasses); j++) {
				boolean b = ((Boolean)(kModel.getValueAt(i, j))).booleanValue();
				if (b) {
					classVectors[j - 1].add(new Integer(i));
					break;
				}
			}
		}

		int[] sortedIndices = new int[kModel.getRowCount()];
		int counter = 0;

		for (int i = 0; i < classVectors.length; i++) {
			for (int j = 0; j < classVectors[i].size(); j++) {
				sortedIndices[counter] = ((Integer)(classVectors[i].get(j))).intValue();
				counter++;
			}
		}

		Object sortedData[][] = new Object[kModel.getRowCount()][kModel.getColumnCount()];

		for (int i = 0; i < sortedData.length; i++) {
			for (int j = 0; j < sortedData[0].length; j++) {
				sortedData[i][j] = kModel.getValueAt(sortedIndices[i], j);
			}
		}

		for (int i = 0; i < kModel.getRowCount(); i++) {
			for (int j = 0; j < kModel.getColumnCount(); j++) {
				kModel.setValueAt(sortedData[i][j], i, j);
			}
			validateTable(sortedData, i);
		}

		BNClassTable.removeRowSelectionInterval(0, BNClassTable.getRowCount() - 1);        
	}

	private void validateTable(Object[][] tabData, int row) {
		//for (int i = 1; i <= (numClasses + 1); i++) {
		for (int i = 1; i <= (numClasses); i++) {
			boolean check = ((Boolean)(tabData[row][i])).booleanValue();
			if (check) {
				kModel.setValueAt(new Boolean(true), row, i);
				break;
			}
		}
	}

	public void loadFromFile (File file) {
		Vector indicesVector = new Vector();
		Vector classVector = new Vector();
		try {
			BufferedReader buff = new BufferedReader(new FileReader(file)); 
			String line = new String();
			StringSplitter st;           

			while ((line = buff.readLine()) != null) {
				st = new StringSplitter('\t');
				st.init(line);
				String currIndex = st.nextToken();
				indicesVector.add(new Integer(currIndex));
				String currClass = st.nextToken();
				classVector.add(new Integer(currClass));
			}

			for (int i = 0; i < indicesVector.size(); i++) {
				int currInd = ((Integer)(indicesVector.get(i))).intValue();
				int currCl = ((Integer)(classVector.get(i))).intValue();

				if (currCl == (-1)) {
					//kModel.setValueAt(new Boolean(true), currInd, (numClasses + 1));
					kModel.setValueAt(new Boolean(true), currInd, (numClasses));
				} else {
					kModel.setValueAt(new Boolean(true), currInd, currCl);
				}
			}  
			BNClassificationEditor.this.showModal(true);

			//BNClassificationEditor.this.setVisible(true);
			// showWarningMessage();
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(framework.getFrame(), "Incompatible file!", "Error", JOptionPane.WARNING_MESSAGE);
			incompatible = true;
			//BNClassificationEditor.this.dispose();
			//e.printStackTrace();
		}
		/*
        for (int i = 0; i < indicesVector.size(); i++) {
            int currInd = ((Integer)(indicesVector.get(i))).intValue();
            int currCl = ((Integer)(classVector.get(i))).intValue();
        }
		 */
	}

	public Vector[] getClassification() {
		Vector indicesVector = new Vector();
		Vector classVector = new Vector();
		Vector[] vectArray = new Vector[2];

		for (int i = 0; i < kModel.getRowCount(); i++) {
			//if (((Boolean)(kModel.getValueAt(i, numClasses + 1))).booleanValue()) {
			if (((Boolean)(kModel.getValueAt(i, numClasses))).booleanValue()) {
				continue;
			} else {
				indicesVector.add((Integer)(kModel.getValueAt(i, 0)));
				classVector.add(new Integer(getClass(i)));
			}
		}

		vectArray[0] = indicesVector;
		vectArray[1] = classVector;
		return vectArray;
	}

	public boolean isNextPressed() {
		return nextPressed;
	}

	private int getClass(int row) {
		int i;
		//for (i = 1; i <= numClasses + 1; i++) {
		for (i = 1; i <= numClasses; i++) {
			if (((Boolean)(kModel.getValueAt(row, i))).booleanValue()) {
				break;
			}
		}

		return i;
	}

	public boolean proceed() {
		return !(stopHere);
	}

	public boolean fileIsIncompatible() {
		return incompatible;
	}

	private class SortableField implements Comparable {
		private String field;
		private int index;

		SortableField(int index, int column) {
			this.index = index;
			this.field = (String)(origData[index][column]);
			//System.out.println("SortableField[" + index + "][" + column + "]: index = " + index + ", field = " + field);
		}

		public int compareTo(Object other) {
			SortableField otherField = (SortableField)other;
			return this.field.compareTo(otherField.getField());
		}

		public int getIndex() {
			return this.index;
		}
		public String getField() {
			return this.field;
		}
	}

	public class AssignListener implements ActionListener {

		public void actionPerformed(ActionEvent evt) {
			Object source = evt.getSource();

			if (source instanceof JMenuItem) {
				String key = ((JMenuItem)source).getText();
				int classCol = kModel.getColumnIndex(key);
				int[] selectedRows = BNClassTable.getSelectedRows();
				int[] selectedIndices = new int[selectedRows.length];

				for (int i = 0; i < selectedRows.length; i++) {
					kModel.setValueAt(new Boolean(true), selectedRows[i], classCol);
					//int currIndex = ((Integer)(kModel.getValueAt(selectedRows[i], 0))).intValue();
					//origData[currIndex][classCol] = new Boolean(true);
				}
			}
		}

	}

	public class SortListener implements ActionListener {
		boolean asc, origOrd;
		public SortListener(boolean asc, boolean origOrd) {
			this.asc = asc;
			this.origOrd = origOrd;
		}

		public void actionPerformed(ActionEvent evt) {
			Object source = evt.getSource();

			if (source instanceof JMenuItem) {
				String key = ((JMenuItem)source).getText();
				int colToSort = kModel.getColumnIndex(key);
				sortByColumn(colToSort, asc, origOrd);
			}
		}
	}

	
	public File getLabelFile() {
		return this.labelFile;
	}
	
	public BNClassTableModel getClassTableModel() {
		return (BNClassTableModel)this.BNClassTable.getModel();
	}

}
