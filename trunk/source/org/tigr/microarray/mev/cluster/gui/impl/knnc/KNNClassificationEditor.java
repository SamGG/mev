/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * KNNClassificationEditor.java
 *
 * Created on September 8, 2003, 4:05 PM
 */

package org.tigr.microarray.mev.cluster.gui.impl.knnc;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Date;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
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
import javax.swing.border.EtchedBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.tigr.microarray.mev.TMEV;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.util.StringSplitter;

/**
 *
 * @author  nbhagaba
 */
public class KNNClassificationEditor extends javax.swing.JDialog {// JFrame {
    
    IFramework framework;
    IData data;
    boolean classifyGenes;
    private boolean stopHere = true;
    private boolean nextPressed = false;
    private boolean incompatible = false;
    private boolean fileSaved = false;
    int numClasses;
    String[] fieldNames;
    int numGenes, numExps;
    JTable knnClassTable;
    KNNClassTableModel kModel;
    JMenuBar menuBar;
    JMenu fileMenu, editMenu, toolsMenu, assignSubMenu, sortAscMenu, sortDescMenu;
    JMenuItem saveItem, closeItem, selectAllItem, searchItem, sortByClassItem, origOrderItem;
    JMenuItem[] classItem, labelsAscItem, labelsDescItem;
    JRadioButton saveButton, doNotSaveButton;
    JButton nextButton;
    JFrame mainFrame;
    
    KNNCSearchDialog searchDialog;    
    
    //SortListener sorter;
    
    Object[][] origData;
    
    /** Creates a new instance of KNNClassificationEditor */
    public KNNClassificationEditor(IFramework framework, boolean classifyGenes, int numClasses) {
        super(framework.getFrame(), true);
        this.setTitle("KNN Classification Editor");
        mainFrame = (JFrame)(framework.getFrame());
        //super((JFrame)(framework.getFrame()), "KNN Classification Editor", true);
        setBounds(0, 0, 550, 800);
        setBackground(Color.white);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.framework = framework;
        this.data = framework.getData();
        this.numGenes = data.getFeaturesSize();
        this.numExps = data.getFeaturesCount();
        this.fieldNames = data.getFieldNames();
        this.classifyGenes = classifyGenes;
        this.numClasses = numClasses;
        
        menuBar = new JMenuBar();
        this.setJMenuBar(menuBar);
        
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        
        JPanel pane = new JPanel();
        pane.setLayout(gridbag);
        
        JPanel tablePanel = new JPanel();
        GridBagLayout grid1 = new GridBagLayout();
        tablePanel.setLayout(grid1);
        
        kModel = new KNNClassTableModel();
        knnClassTable = new JTable(kModel);
        knnClassTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        TableColumn column = null;
        for (int i = 0; i < kModel.getColumnCount(); i++) {
            column = knnClassTable.getColumnModel().getColumn(i);
            column.setMinWidth(30);
        }
        knnClassTable.setColumnModel(new KNNClassTableColumnModel(knnClassTable.getColumnModel()));
        knnClassTable.getModel().addTableModelListener(new ClassSelectionListener());
        
        searchDialog = new KNNCSearchDialog(this, knnClassTable, numClasses, false); //persistent search dialog     
        //JOptionPane.getFrameForComponent(this)
        JScrollPane scroll = new JScrollPane(knnClassTable);
        buildConstraints(constraints, 0, 0, 1, 1, 100, 100);
        grid1.setConstraints(scroll, constraints);
        tablePanel.add(scroll);
        
        buildConstraints(constraints, 0, 0, 1, 1, 100, 90);
        gridbag.setConstraints(tablePanel, constraints);
        pane.add(tablePanel);
        
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBorder(new EtchedBorder());
        bottomPanel.setBackground(Color.white);
        GridBagLayout grid2 = new GridBagLayout();
        bottomPanel.setLayout(grid2);
        
        saveButton = new JRadioButton("Save classification to file", true);
        saveButton.setBackground(Color.white);
        doNotSaveButton = new JRadioButton("Do not save classification to file", false);
        doNotSaveButton.setBackground(Color.white);
        ButtonGroup saveOrNot = new ButtonGroup();
        saveOrNot.add(saveButton);
        saveOrNot.add(doNotSaveButton);
        
        final JFileChooser fc = new JFileChooser(TMEV.getDataPath());
        fc.setDialogTitle("Save classification");
        
        nextButton = new JButton("Next >");
        nextButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                if (doNotSaveButton.isSelected()) {
                    KNNClassificationEditor.this.dispose();
                    stopHere = false;
                    nextPressed = true;                    
                    //KNNClassificationEditor.this.dispose();
                } else {
                    int returnVal = fc.showSaveDialog(KNNClassificationEditor.this);  
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        File file = fc.getSelectedFile();  
                        saveToFile(file);
                        KNNClassificationEditor.this.dispose();
                        KNNCSecondDialog kSecDialog = new KNNCSecondDialog(mainFrame, true);
                        kSecDialog.setVisible(true);
                        if (!kSecDialog.proceed()) {
                            stopHere = true;
                        } else {
                            stopHere = false;
                        }
                        fileSaved = true;
                        nextPressed = true;                        
                        /*
                        KNNClassificationEditor.this.dispose();
                        KNNCSecondDialog kSecDialog = new KNNCSecondDialog(mainFrame, true);
                        kSecDialog.setVisible(true);
                        if (!kSecDialog.proceed()) {
                            stopHere = true;
                        }
                         */
                    }                    
                }
            }
        });
        constraints.fill = GridBagConstraints.NONE;

        buildConstraints(constraints, 0, 0, 1, 1, 100, 33);
        grid2.setConstraints(saveButton, constraints);
        bottomPanel.add(saveButton);
        
        buildConstraints(constraints, 0, 1, 1, 1, 0, 33);
        grid2.setConstraints(doNotSaveButton, constraints);
        bottomPanel.add(doNotSaveButton);
        
        buildConstraints(constraints, 0, 2, 1, 1, 0, 34);
        grid2.setConstraints(nextButton, constraints);
        bottomPanel.add(nextButton);        
        
        constraints.fill = GridBagConstraints.BOTH;
        buildConstraints(constraints, 0, 1, 1, 1, 0, 10);
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
        
        classItem = new JMenuItem[numClasses + 1];
        
        for (int i = 0; i < numClasses; i++) {
            classItem[i] = new JMenuItem("Class " + (i + 1));
        }
        
        classItem[numClasses] = new JMenuItem("Neutral");
        
        for (int i = 0; i < classItem.length; i++) {
            classItem[i].addActionListener(new AssignListener());
        }
        /*
        fileMenu = new JMenu("File");
        saveItem = new JMenuItem("Save classification");
        fileMenu.add(saveItem);
        closeItem = new JMenuItem("Close editor");
        
        closeItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                KNNClassificationEditor.this.dispose();
            }
        });
        
        fileMenu.add(closeItem);
        menuBar.add(fileMenu);
        */
        editMenu = new JMenu("Edit");
        selectAllItem = new JMenuItem("Select all rows");
        selectAllItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                knnClassTable.selectAll();
            }
        });
        editMenu.add(selectAllItem);
        assignSubMenu = new JMenu("Assign selected rows to");
        for (int i = 0; i < classItem.length; i++) {
            assignSubMenu.add(classItem[i]);
        }
        editMenu.add(assignSubMenu);
        menuBar.add(editMenu);
        
        toolsMenu = new JMenu("Tools");
        searchItem = new JMenuItem("Search");
        searchItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                searchTable();
            }
        });
        toolsMenu.add(searchItem);
        sortAscMenu = new JMenu("Sort ascending by");
        for (int i = 0; i < labelsAscItem.length; i++) {
            sortAscMenu.add(labelsAscItem[i]);
        }
        toolsMenu.add(sortAscMenu);
        sortDescMenu = new JMenu("Sort descending by");
        for (int i = 0; i < labelsDescItem.length; i++) {
            sortDescMenu.add(labelsDescItem[i]);
        }
        toolsMenu.add(sortDescMenu);
        sortByClassItem = new JMenuItem("Sort by classification");
        sortByClassItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                sortByClassification();
            }
        });
        toolsMenu.add(sortByClassItem);
        origOrderItem = new JMenuItem("Restore original ordering");
        origOrderItem.addActionListener(new SortListener(true, true));
        toolsMenu.add(origOrderItem);
        
        menuBar.add(toolsMenu);
    }
    
    
    void buildConstraints(GridBagConstraints gbc, int gx, int gy,
    int gw, int gh, int wx, int wy) {
        
        gbc.gridx = gx;
        gbc.gridy = gy;
        gbc.gridwidth = gw;
        gbc.gridheight = gh;
        gbc.weightx = wx;
        gbc.weighty = wy;
    }
    
    public void showModal(boolean visible) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);
        showWarningMessage();     
        super.setVisible(visible);        
    }
    
    class KNNClassifierTable extends JTable {
    }
    
    class KNNClassTableModel extends AbstractTableModel {
        String[] columnNames;
        Object tableData[][];
        int indexLastClass;
        
        public KNNClassTableModel() {
            indexLastClass = numClasses;
            if (classifyGenes) {
                columnNames = new String[fieldNames.length + numClasses + 2];
                columnNames[0] = "Index";
                for (int i = 0; i < numClasses; i++) {
                    columnNames[i + 1] = "Class " + (i+1);
                }
                columnNames[numClasses + 1] = "Neutral";
                
                for (int i = 0; i < fieldNames.length; i++) {
                    columnNames[numClasses + 2 + i] = fieldNames[i];
                }
                
                tableData = new Object[numGenes][columnNames.length];
                
                for (int i = 0; i < tableData.length; i++) {
                    for (int j = 0; j < columnNames.length; j++) {
                        if (j == 0) {
                            tableData[i][j] = new Integer(i);
                        } else if ((j > 0) && (j < (numClasses + 1))) {
                            tableData[i][j] = new Boolean(false);
                        } else if (j == numClasses + 1) {
                            tableData[i][j] = new Boolean(true);
                        } else {
                            tableData[i][j] = data.getElementAttribute(i, j - (numClasses + 2));
                        }
                    }
                }
                
            } else { // (!classifyGenes)
                columnNames = new String[numClasses + 3];
                columnNames[0] = "Index";
                for (int i = 0; i < numClasses; i++) {
                    columnNames[i + 1] = "Class " + (i+1);
                }
                columnNames[numClasses + 1] = "Neutral";
                columnNames[numClasses + 2] = "Sample Name";
                tableData = new Object[numExps][columnNames.length];
                
                for (int i = 0; i < tableData.length; i++) {
                    for (int j = 0; j < columnNames.length; j++) {
                        if (j == 0) {
                            tableData[i][j] = new Integer(i);
                        } else if ((j > 0) && (j < (numClasses + 1))) {
                            tableData[i][j] = new Boolean(false);
                        } else if (j == numClasses + 1) {
                            tableData[i][j] = new Boolean(true);
                        } else if (j == numClasses + 2) {
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
            } else if ((c > 0) && (c <= (numClasses + 1))) {
                return java.lang.Boolean.class;
            } else {
                return getValueAt(0, c).getClass();
            }
        }
        
        
        public boolean isCellEditable(int row, int col) {
            //Note that the data/cell address is constant,
            //no matter where the cell appears onscreen.
            if ((col > 0) && (col <= (numClasses + 1))) {
                return true;
            } else {
                return false;
            }
        }
    }
    
    
    class KNNClassTableColumnModel implements TableColumnModel {
        
        TableColumnModel tcm;
        
        public KNNClassTableColumnModel(TableColumnModel TCM) {
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
            if (from <= (numClasses + 1) || to <= (numClasses + 1)) {
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
            
            if ((selectedCol < 1) || (selectedCol > (numClasses + 1) )) {
                return;
            }
            
            if( verifySelected(selectedRow, selectedCol)){
                changeNeighbors(selectedRow, selectedCol);
            }
            
            int origDataRow = ((Integer)(kModel.getValueAt(selectedRow, 0))).intValue();
            
            origData[origDataRow][selectedCol] = new Boolean(true);
            
            for (int i = 1; i <= (numClasses + 1); i++) {
                if (i != selectedCol) {
                    origData[origDataRow][i] = new Boolean(false);
                }
            }
        }
        
        private void changeNeighbors(int first, int col){
            for (int i = 1; i <= (numClasses + 1); i++) {
                if (i != col) {
                    knnClassTable.setValueAt(new Boolean(false), first, i);
                    //origData[first][i] = new Boolean(false); 
                }
            }
        }
        
        private boolean verifySelected(int row, int col){
            
            boolean selVal = ((Boolean)knnClassTable.getValueAt(row,col)).booleanValue();
            //boolean value1, value2;
            
            if(selVal == true){
                return true;
            } else {
                Vector truthValues = new Vector();
                for (int i = 1; i <=(numClasses + 1); i++) {
                    if (i != col) {
                        boolean value = ((Boolean)(knnClassTable.getValueAt(row,i))).booleanValue();
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
                    knnClassTable.setValueAt(new Boolean(true), row, col);
                    //origData[row][col] = new Boolean(true);
                }
                
            }
            return false;
            
            /*else {
                knnClassTable.setValueAt(new Boolean(true), selectedRow, selectedCol);
             
                for (int i = 1; i <= (numClasses + 1); i++) {
                    if (i != selectedCol) {
                        knnClassTable.setValueAt(new Boolean(false), selectedRow, i);
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
        
        knnClassTable.removeRowSelectionInterval(0, knnClassTable.getRowCount() - 1);
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
        Vector[] classVectors = new Vector[numClasses + 1];
        for (int i = 0; i < classVectors.length; i++) {
            classVectors[i] = new Vector();
        }
        
        for (int i = 0; i < kModel.getRowCount(); i++) {
            for (int j = 1; (j <= numClasses + 1); j++) {
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
        
        knnClassTable.removeRowSelectionInterval(0, knnClassTable.getRowCount() - 1);        
    }
    
    private void validateTable(Object[][] tabData, int row) {
        for (int i = 1; i <= (numClasses + 1); i++) {
            boolean check = ((Boolean)(tabData[row][i])).booleanValue();
            if (check) {
                kModel.setValueAt(new Boolean(true), row, i);
                break;
            }
        }
    }
    
    private void searchTable(){
        
        searchDialog.setVisible(true);
        searchDialog.toFront();
        //searchDialog.requestFocus();
        searchDialog.setLocation(this.getLocation().x + 100, this.getLocation().y +100);
        
    }    
    
    private void saveToFile(File file) {
    	try {
			PrintWriter pw = new PrintWriter(new FileWriter(file));
			//comment row
			Date currDate = new Date(System.currentTimeMillis());			
			String dateString = currDate.toString();;
			String userName = System.getProperty("user.name");
			
			pw.println("# Assignment File");
			pw.println("# User: "+userName+" Save Date: "+dateString);
			pw.println("#");
			
			//save group names..?
			
			pw.print("Module:\t");
			pw.println("KNN");
			for (int i=1; i< numClasses+1;i++){
	   			pw.print("Group "+i+" Label:\t");
				pw.println("Class "+i);
			}
							
			pw.println("#");
			
			pw.println("Index\tName\tGroup Assignment");
			
//   				int[] groupAssgn = getGroupAssignments();

	        int numRows = kModel.getRowCount(); 
			for(int sample = 0; sample < numRows; sample++) {
				pw.print(String.valueOf(sample+1)+"\t"); //sample index
				pw.print(kModel.getValueAt(sample, numClasses + 2)+"\t");
				for (int j = 1; j <= numClasses; j++) {
                    if (((Boolean)(kModel.getValueAt(sample, j))).booleanValue()) {
                        pw.println("Class "+j);
                        break;
                    }
                }
                if (((Boolean)(kModel.getValueAt(sample, numClasses + 1))).booleanValue()) {
                    pw.println("Exclude");
                }
			}
			pw.flush();
			pw.close();			
		} catch (FileNotFoundException fnfe) {
			fnfe.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
    }
    
	/**
	 * Loads file based assignments
	 */
    public void loadFromFile(File file){
    	
		try {						
			//first grab the data and close the file
			BufferedReader br = new BufferedReader(new FileReader(file));
			Vector<String> data = new Vector<String>();
			String line;
			while( (line = br.readLine()) != null)
				data.add(line.trim());
			
			br.close();
				
			//build structures to capture the data for assingment information and for *validation
			
			//factor names
			Vector<String> groupNames = new Vector<String>();
			
			
			Vector<Integer> sampleIndices = new Vector<Integer>();
			Vector<String> sampleNames = new Vector<String>();
			Vector<String> groupAssignments = new Vector<String>();		
			
			//parse the data in to these structures
			String [] lineArray;
			//String status = "OK";
			for(int row = 0; row < data.size(); row++) {
				line = (String)(data.get(row));

				//if not a comment line, and not the header line
				if(!(line.startsWith("#")) && !(line.startsWith("SampleIndex"))) {
					
					lineArray = line.split("\t");
					
					//check what module saved the file
					if(lineArray[0].startsWith("Module:")) {
						if (!lineArray[1].equals("KNN")){
							Object[] optionst = { "Continue", "Cancel" };
							if (JOptionPane.showOptionDialog(null, 
		    						"The saved file was saved using a different module, "+lineArray[1]+". \n Would you like MeV to try to load it anyway?", 
		    						"File type warning", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, 
		    						optionst, optionst[0])==0)
								continue;
							return;
						}
						continue;
					}
					
					//pick up group names
					if(lineArray[0].startsWith("Group ") && lineArray[0].endsWith("Label:")) {
						groupNames.add(lineArray[1]);
						continue;
					}
						

					//non-comment line, non-header line and not a group label line
					
					try {
						Integer.parseInt(lineArray[0]);
					} catch ( NumberFormatException nfe) {
						//if not parsable continue
						continue;
					}
					
					sampleIndices.add(new Integer(lineArray[0]));
					sampleNames.add(lineArray[1]);
					groupAssignments.add(lineArray[2]);	
				}				
			}
			
			//we have the data parsed, now validate, assign current data
			

	        int numRows = kModel.getRowCount(); 
			if( numRows != sampleNames.size()) {
				System.out.println(numRows+"  "+sampleNames.size());
				//status = "number-of-samples-mismatch";
				System.out.println(numRows+ " s length " + sampleNames.size());
				//warn and prompt to continue but omit assignments for those not represented				

				JOptionPane.showMessageDialog(this, "<html>Error -- number of samples designated in assignment file ("+String.valueOf(sampleNames.size())+")<br>" +
						                                   "does not match the number of samples loaded in MeV ("+numRows+").<br>" +
						                                   	"Assignments are not set.</html>", "File Compatibility Error", JOptionPane.ERROR_MESSAGE);
				incompatible = true;
				return;
			}
			if (numClasses!= groupNames.size()){
				Object[] optionst = { "Continue", "Cancel" };
				if (JOptionPane.showOptionDialog(null, 
					"The saved file was saved using "+groupNames.size()+" classes.\nWould you like MeV to attempt to load selections into " +numClasses + " classes?", 
					"Class number warning", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, 
					optionst, optionst[0])==1){
					return;
				}
			}
			int fileSampleIndex = 0;
			int groupIndex = 0;
			String groupName;
			for(int sample = 0; sample < numRows; sample++) {
				boolean doIndex = false;
				for (int i=0;i<numRows; i++){
					if (i==sample)
						continue;
					if (((String)kModel.getValueAt(sample, numClasses + 2)).equals(
							((String)kModel.getValueAt(i, numClasses + 2)))){
						doIndex=true;
					}
				}
				try{
					fileSampleIndex = sampleNames.indexOf((String)kModel.getValueAt(sample, numClasses+2));
				}catch (Exception e){
					e.printStackTrace();
					fileSampleIndex=-1;
				}
				if (fileSampleIndex==-1){
					doIndex=true;
				}
				if (doIndex){
					setStateBasedOnIndex(groupAssignments,groupNames);
					break;
				}
				
				groupName = (String)(groupAssignments.get(fileSampleIndex));
				groupIndex = groupNames.indexOf(groupName);
				
				//set all the extra and neither groups to 'neither'
				if (groupIndex==-1||groupIndex>numClasses)
					groupIndex = numClasses;
				//set state
				
				try{
    				kModel.setValueAt(new Boolean(true), sample, groupIndex+1);
				}catch (Exception e){
					e.printStackTrace();
					kModel.setValueAt(new Boolean(true), sample, numClasses+1);  //set to last state... excluded
				}
			}
	        KNNClassificationEditor.this.showModal(true);		
			//need to clear assignments, clear assignment booleans in sample list and re-init
			//maybe a specialized inti for the sample list panel.
		} catch (Exception e) {
			e.printStackTrace();
			incompatible = true;
			JOptionPane.showMessageDialog(this, "<html>The file format cannot be read.</html>", "File Compatibility Error", JOptionPane.ERROR_MESSAGE);
		}
    }

	private void setStateBasedOnIndex(Vector<String>groupAssignments,Vector<String>groupNames){
		Object[] optionst = { "Continue", "Cancel" };
		if (JOptionPane.showOptionDialog(null, 
				"The saved file was saved using a different sample annotation or has duplicate annotation. \n Would you like MeV to try to load it by index order?", 
				"File type warning", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, 
				optionst, optionst[0])==1)
			return;
		int numRows = kModel.getRowCount(); 
		for(int sample = 0; sample < numRows; sample++) {

			//set state
			try{
				kModel.setValueAt(new Boolean(true), sample, groupNames.indexOf(groupAssignments.get(sample)+1));
			}catch (Exception e){
				e.printStackTrace();
				kModel.setValueAt(new Boolean(true), sample, numClasses+1);
			}
		}
	}
    
    
    public Vector[] getClassification() {
        Vector indicesVector = new Vector();
        Vector classVector = new Vector();
        Vector[] vectArray = new Vector[2];
        
        for (int i = 0; i < kModel.getRowCount(); i++) {
            if (((Boolean)(kModel.getValueAt(i, numClasses + 1))).booleanValue()) {
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
        for (i = 1; i <= numClasses + 1; i++) {
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
    
    public void showWarningMessage() {
        JTextArea area = new JTextArea();
        area.append("The editor displays all genes or expts in the data set loaded into MeV,");
        area.append("\nincluding those that have been removed from analysis by variance filtering"); 
        area.append("\nin the previous stage, or by applying cutoffs under the Adjust Data"); 
        area.append("\nmenu. If you designate such genes  or samples as classifiers,");  
        area.append("\nthey will not be used for classification");
        area.setEditable(false);
        area.setBackground(Color.gray.brighter());
        JOptionPane.showMessageDialog(KNNClassificationEditor.this, area, "Warning", JOptionPane.WARNING_MESSAGE);
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
                int[] selectedRows = knnClassTable.getSelectedRows();
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
    
}
