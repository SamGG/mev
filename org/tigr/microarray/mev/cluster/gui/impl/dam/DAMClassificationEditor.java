/*
Copyright @ 1999-2005, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * DAMClassificationEditor.java
 *
 */

package org.tigr.microarray.mev.cluster.gui.impl.dam;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.Arrays;
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

import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.util.StringSplitter;

public class DAMClassificationEditor extends JFrame {
    
    IFramework framework;
    IData data;
    boolean classifyGenes;
    private boolean stopHere = true;
    private boolean nextPressed = false;
    private boolean cancelPressed = false;
    private boolean incompatible = false;
    private boolean fileSaved = false;
    private boolean fileOpened = false;
    int numberOfClasses;
    String[] fieldNames;
    int numGenes, numExps;
    JTable damClassTable;
    DAMClassTableModel damTabModel;
    JMenuBar menuBar;
    JMenu fileMenu, editMenu, toolsMenu, assignSubMenu, sortAscMenu, sortDescMenu;
    JMenuItem openItem, closeItem, selectAllItem, searchItem, sortByClassItem, origOrderItem;
    JMenuItem[] classItem, labelsAscItem, labelsDescItem;
    JRadioButton saveButton, doNotSaveButton;
    JButton nextButton;
    JButton cancelButton;
    JFrame mainFrame;
    
    DAMSearchDialog searchDialog;    
    
    //SortListener sorter;
    
    Object[][] origData;
    
    /** Creates a new instance of DAMClassificationEditor */
    public DAMClassificationEditor(IFramework framework, boolean classifyGenes, int numberOfClasses) {
        this.setTitle("DAM Classification Editor");
        mainFrame = (JFrame)(framework.getFrame());
        //super((JFrame)(framework.getFrame()), "DAM Classification Editor", true);
        setBounds(0, 0, 550, 800);
        setBackground(Color.white);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
                closeDialog(evt);
            }
        });
       
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.framework = framework;
        this.data = framework.getData();
        this.numGenes = data.getFeaturesSize();
        this.numExps = data.getFeaturesCount();
        this.fieldNames = data.getFieldNames();
        this.classifyGenes = classifyGenes;
        this.numberOfClasses = numberOfClasses;
        
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
        
        damTabModel = new DAMClassTableModel();
        damClassTable = new JTable(damTabModel);
        damClassTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        TableColumn column = null;
        for (int i = 0; i < damTabModel.getColumnCount(); i++) {
            column = damClassTable.getColumnModel().getColumn(i);
            column.setMinWidth(30);
        }
        damClassTable.setColumnModel(new DAMClassTableColumnModel(damClassTable.getColumnModel()));
        damClassTable.getModel().addTableModelListener(new ClassSelectionListener());
        
        searchDialog = new DAMSearchDialog(JOptionPane.getFrameForComponent(this), damClassTable, numberOfClasses, false); //persistent search dialog     
        
        JScrollPane scroll = new JScrollPane(damClassTable);
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
        
        final JFileChooser fc = new JFileChooser();
        fc.setCurrentDirectory(new File("Data"));
        fc.setDialogTitle("Save classification");
        

        cancelButton = new JButton("Cancel >");
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
	        dispose();
            }
        });


        nextButton = new JButton("Next >");
        nextButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                if (doNotSaveButton.isSelected()) {
                    DAMClassificationEditor.this.dispose();
                    stopHere = false;
                    nextPressed = true;                    
                } else {
                    int returnVal = fc.showSaveDialog(DAMClassificationEditor.this);  
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        File file = fc.getSelectedFile();  

                        saveToFile(file);

                        DAMSecondDialog kSecDialog = new DAMSecondDialog(mainFrame, true);
                        kSecDialog.setVisible(true);
                        if (!kSecDialog.proceed()) {
                            stopHere = true;
                        } else {
                            stopHere = false;
                        }

	                incompatible = false;
                        fileSaved = true;

                        if (stopHere == true) {
                            nextPressed = false;                        
                            DAMClassificationEditor.this.setVisible(true);
                        } else {
                            nextPressed = true;                        
                            DAMClassificationEditor.this.dispose();
                        }

/*
                        DAMSecondDialog kSecDialog = new DAMSecondDialog(mainFrame, true);
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
        
/*
        buildConstraints(constraints, 0, 3, 1, 1, 0, 34);
        grid2.setConstraints(cancelButton, constraints);
        bottomPanel.add(cancelButton);        
*/
        
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
            labelsAscItem[0] = new JMenuItem("Sample/Experiment Name");
            labelsDescItem = new JMenuItem[1];
            labelsDescItem[0] = new JMenuItem("Sample/Experiment Name");
        }
        
        for (int i = 0; i < labelsAscItem.length; i++) {
            labelsAscItem[i].addActionListener(new SortListener(true, false));
            labelsDescItem[i].addActionListener(new SortListener(false, false));
        }
        
        classItem = new JMenuItem[numberOfClasses + 1];
        
        for (int i = 0; i < numberOfClasses; i++) {
            classItem[i] = new JMenuItem("Class " + (i + 1));
        }
        
        classItem[numberOfClasses] = new JMenuItem("Neutral");
        
        for (int i = 0; i < classItem.length; i++) {
            classItem[i].addActionListener(new AssignListener());
        }

        final JFileChooser fc1 = new JFileChooser();
        fc1.setCurrentDirectory(new File("Data"));
        fc1.setDialogTitle("Open Classification");
        
        fileMenu = new JMenu("File");
        openItem = new JMenuItem("Apply File");
        openItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {

                  int returnVal = fc1.showOpenDialog(DAMClassificationEditor.this);  
                  if (returnVal == JFileChooser.APPROVE_OPTION) {
                        File file = fc1.getSelectedFile();  
                        loadFromFile(file);

                        fileOpened = true;
                        nextPressed = false;                        

                   }
            }
        });


        fileMenu.add(openItem);

        menuBar.add(fileMenu);

        editMenu = new JMenu("Edit");
        selectAllItem = new JMenuItem("Select all rows");
        selectAllItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                damClassTable.selectAll();
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
    
    /** Closes the dialog */
    private void closeDialog(WindowEvent evt) {
        setVisible(false);
        cancelPressed = true;                        
        dispose();
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
    
    public void setVisible(boolean visible) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);
        
        super.setVisible(visible);
    }
    
    class DAMClassifierTable extends JTable {
    }
    
    class DAMClassTableModel extends AbstractTableModel {
        String[] columnNames;
        Object tableData[][];
        int indexLastClass;
        
        public DAMClassTableModel() {
            indexLastClass = numberOfClasses;
            if (classifyGenes) {
                columnNames = new String[fieldNames.length + numberOfClasses + 2];
                columnNames[0] = "Index";
                for (int i = 0; i < numberOfClasses; i++) {
                    columnNames[i + 1] = "Class " + (i+1);
                }
                columnNames[numberOfClasses + 1] = "Neutral";
                
                for (int i = 0; i < fieldNames.length; i++) {
                    columnNames[numberOfClasses + 2 + i] = fieldNames[i];
                }
                
                tableData = new Object[numGenes][columnNames.length];
                
                for (int i = 0; i < tableData.length; i++) {
                    for (int j = 0; j < columnNames.length; j++) {
                        if (j == 0) {
                            tableData[i][j] = new Integer(i);
                        } else if ((j > 0) && (j < (numberOfClasses + 1))) {
                            tableData[i][j] = new Boolean(false);
                        } else if (j == numberOfClasses + 1) {
                            tableData[i][j] = new Boolean(true);
                        } else {
                            tableData[i][j] = data.getElementAttribute(i, j - (numberOfClasses + 2));
                        }
                    }
                }
                
            } else { // (!classifyGenes)
                columnNames = new String[numberOfClasses + 3];
                columnNames[0] = "Index";
                for (int i = 0; i < numberOfClasses; i++) {
                    columnNames[i + 1] = "Class " + (i+1);
                }
                columnNames[numberOfClasses + 1] = "Neutral";
                columnNames[numberOfClasses + 2] = "Sample/Experiment Name";
                tableData = new Object[numExps][columnNames.length];
                
                for (int i = 0; i < tableData.length; i++) {
                    for (int j = 0; j < columnNames.length; j++) {
                        if (j == 0) {
                            tableData[i][j] = new Integer(i);
                        } else if ((j > 0) && (j < (numberOfClasses + 1))) {
                            tableData[i][j] = new Boolean(false);
                        } else if (j == numberOfClasses + 1) {
                            tableData[i][j] = new Boolean(true);
                        } else if (j == numberOfClasses + 2) {
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
            } else if ((c > 0) && (c <= (numberOfClasses + 1))) {
                return java.lang.Boolean.class;
            } else {
                return getValueAt(0, c).getClass();
            }
        }
        
        
        public boolean isCellEditable(int row, int col) {
            //Note that the data/cell address is constant,
            //no matter where the cell appears onscreen.
            if ((col > 0) && (col <= (numberOfClasses + 1))) {
                return true;
            } else {
                return false;
            }
        }
    }
    
    
    class DAMClassTableColumnModel implements TableColumnModel {
        
        TableColumnModel tcm;
        
        public DAMClassTableColumnModel(TableColumnModel TCM) {
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
            if (from <= (numberOfClasses + 1) || to <= (numberOfClasses + 1)) {
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
            
            if ((selectedCol < 1) || (selectedCol > (numberOfClasses + 1) )) {
                return;
            }
            
            if( verifySelected(selectedRow, selectedCol)){
                changeNeighbors(selectedRow, selectedCol);
            }
            
            int origDataRow = ((Integer)(damTabModel.getValueAt(selectedRow, 0))).intValue();
            
            origData[origDataRow][selectedCol] = new Boolean(true);
            
            for (int i = 1; i <= (numberOfClasses + 1); i++) {
                if (i != selectedCol) {
                    origData[origDataRow][i] = new Boolean(false);
                }
            }
        }
        
        private void changeNeighbors(int first, int col){
            for (int i = 1; i <= (numberOfClasses + 1); i++) {
                if (i != col) {
                    damClassTable.setValueAt(new Boolean(false), first, i);
                    //origData[first][i] = new Boolean(false); 
                }
            }
        }
        
        private boolean verifySelected(int row, int col){
            
            boolean selVal = ((Boolean)damClassTable.getValueAt(row,col)).booleanValue();
            //boolean value1, value2;
            
            if(selVal == true){
                return true;
            } else {
                Vector truthValues = new Vector();
                for (int i = 1; i <=(numberOfClasses + 1); i++) {
                    if (i != col) {
                        boolean value = ((Boolean)(damClassTable.getValueAt(row,i))).booleanValue();
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
                    damClassTable.setValueAt(new Boolean(true), row, col);
                    //origData[row][col] = new Boolean(true);
                }
                
            }
            return false;
            
            /*else {
                damClassTable.setValueAt(new Boolean(true), selectedRow, selectedCol);
             
                for (int i = 1; i <= (numberOfClasses + 1); i++) {
                    if (i != selectedCol) {
                        damClassTable.setValueAt(new Boolean(false), selectedRow, i);
                    }
                }
             
            }
             */
            
        }
        
    }
    
    public void sortByColumn(int column, boolean ascending, boolean originalOrder) {
        if (originalOrder) {
            //double[] indices = new int[damTabModel.getRowCount()];
            //for (int i = 0; i < damTabModel.getRowCount(); i++) {
                //indices[i] = ((Integer)(damTabModel.getValueAt(i, 0))).doubleValue();
                /*
                QSort sortIndices = new QSort(indices);
                int[] sorted = sortIndices.getOrigIndx();
                 */
            Object[][] sortedData = new Object[damTabModel.getRowCount()][damTabModel.getColumnCount()];
            
            for (int i = 0; i < sortedData.length; i++) {
                for (int j = 0; j < sortedData[0].length; j++) {
                    sortedData[i][j] = origData[i][j];
                }
            }
            
            for (int i = 0; i < sortedData.length; i++) {
                for (int j = 0; j < sortedData[0].length; j++) {
                    damTabModel.setValueAt(sortedData[i][j], i, j);
                }
                validateTable(sortedData, i);
            }
            return;
            //}
            /*
            for (int i = 0; i < damTabModel.getRowCount(); i++) {
                for (int j = 0; j < damTabModel.getColumnCount(); j++) {
                    damTabModel.setValueAt(origData[i][j], i, j);
                }
                validateTable(origData, i);
            }
            return;
             */
        }
        if ((column < 0)|| (column > damTabModel.getColumnCount())) {
            return;
        }
        Object[][] sortedData = new Object[damTabModel.getRowCount()][damTabModel.getColumnCount()];
        //float[] origArray = new float[damTabModel.getRowCount()];
        SortableField[] sortFields = new SortableField[damTabModel.getRowCount()];
        
        for (int i = 0; i < sortFields.length; i++) {
            int origDataRow = ((Integer)(damTabModel.getValueAt(i, 0))).intValue();
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
                damTabModel.setValueAt(sortedData[i][j], i, j);
            }
            validateTable(sortedData, i);
        }
        
        damClassTable.removeRowSelectionInterval(0, damClassTable.getRowCount() - 1);
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
        Vector[] classVectors = new Vector[numberOfClasses + 1];
        for (int i = 0; i < classVectors.length; i++) {
            classVectors[i] = new Vector();
        }
        
        for (int i = 0; i < damTabModel.getRowCount(); i++) {
            for (int j = 1; (j <= numberOfClasses + 1); j++) {
                boolean b = ((Boolean)(damTabModel.getValueAt(i, j))).booleanValue();
                if (b) {
                    classVectors[j - 1].add(new Integer(i));
                    break;
                }
            }
        }
        
        int[] sortedIndices = new int[damTabModel.getRowCount()];
        int counter = 0;
        
        for (int i = 0; i < classVectors.length; i++) {
            for (int j = 0; j < classVectors[i].size(); j++) {
                sortedIndices[counter] = ((Integer)(classVectors[i].get(j))).intValue();
                counter++;
            }
        }
        
        Object sortedData[][] = new Object[damTabModel.getRowCount()][damTabModel.getColumnCount()];
        
        for (int i = 0; i < sortedData.length; i++) {
            for (int j = 0; j < sortedData[0].length; j++) {
                sortedData[i][j] = damTabModel.getValueAt(sortedIndices[i], j);
            }
        }
        
        for (int i = 0; i < damTabModel.getRowCount(); i++) {
            for (int j = 0; j < damTabModel.getColumnCount(); j++) {
                damTabModel.setValueAt(sortedData[i][j], i, j);
            }
            validateTable(sortedData, i);
        }
        
        damClassTable.removeRowSelectionInterval(0, damClassTable.getRowCount() - 1);        
    }
    
    private void validateTable(Object[][] tabData, int row) {
        for (int i = 1; i <= (numberOfClasses + 1); i++) {
            boolean check = ((Boolean)(tabData[row][i])).booleanValue();
            if (check) {
                damTabModel.setValueAt(new Boolean(true), row, i);
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
            PrintWriter out = new PrintWriter(new FileOutputStream(file));
            for (int i = 0; i < damTabModel.getRowCount(); i++) {
                out.print(((Integer)(damTabModel.getValueAt(i, 0))).intValue());
                out.print("\t");
                for (int j = 1; j <= numberOfClasses; j++) {
                    if (((Boolean)(damTabModel.getValueAt(i, j))).booleanValue()) {
                        out.print(j);
                        break;
                    }
                }
                if (((Boolean)(damTabModel.getValueAt(i, numberOfClasses + 1))).booleanValue()) {
                    out.print(-1);
                }
                //out.print("\t");
                for (int j = numberOfClasses + 2; j < damTabModel.getColumnCount(); j++) {
                    out.print("\t");
                    out.print(damTabModel.getValueAt(i, j));
                }
                out.print("\r");
                out.print("\n");
            }
            out.flush();
            out.close();            
            
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }


    public void loadFromFile (File file) {
        Vector indicesVector = new Vector();
        Vector classVector = new Vector();

        incompatible = false;

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
           
           if (indicesVector.size() != damTabModel.getRowCount()) {

	       JOptionPane.showMessageDialog(framework.getFrame(), "Number of rows mismatch!", "Error", JOptionPane.WARNING_MESSAGE);
	       incompatible = true;
               DAMClassificationEditor.this.setVisible(true);

               return;
           }

           for (int i = 0; i < indicesVector.size(); i++) {
               int currCl = ((Integer)(classVector.get(i))).intValue();

	       if (currCl > numberOfClasses){

		   JOptionPane.showMessageDialog(framework.getFrame(), "Class index larger than number of classes!", "Error", JOptionPane.WARNING_MESSAGE);
		   incompatible = true;
                   DAMClassificationEditor.this.setVisible(true);

		   return;
	       } 
           }

           for (int i = 0; i < indicesVector.size(); i++) {
               int currInd = ((Integer)(indicesVector.get(i))).intValue();
               int currCl = ((Integer)(classVector.get(i))).intValue();

               if (currCl == (-1)) {
                   damTabModel.setValueAt(new Boolean(true), currInd, (numberOfClasses + 1));
                   damClassTable.setValueAt(new Boolean(true), currInd, (numberOfClasses+1));
               } else {
                   damTabModel.setValueAt(new Boolean(true), currInd, currCl);
                   damClassTable.setValueAt(new Boolean(true), currInd, currCl);
               }
           }  
           
           // DAMClassificationEditor.this.setVisible(true);

           //showWarningMessage();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(framework.getFrame(), "Incompatible file!", "Error", JOptionPane.WARNING_MESSAGE);
            incompatible = true;
            //DAMClassificationEditor.this.dispose();
            //e.printStackTrace();
        }

    }

  
    public Vector[] getClassification() {
        Vector indicesVector1 = new Vector();
        Vector indicesVector2 = new Vector();
        Vector classVector = new Vector();
        Vector[] classVectorArray = new Vector[3];
        
        for (int i = 0; i < damTabModel.getRowCount(); i++) {
            if (((Boolean)(damTabModel.getValueAt(i, numberOfClasses + 1))).booleanValue()) {
                indicesVector2.add((Integer)(damTabModel.getValueAt(i, 0)));
            } else {
                indicesVector1.add((Integer)(damTabModel.getValueAt(i, 0)));
                classVector.add(new Integer(getClass(i)));
            }
        }
        
        classVectorArray[0] = indicesVector1;
        classVectorArray[1] = classVector;
        classVectorArray[2] = indicesVector2;
        return classVectorArray;
    }

    
    public boolean isNextPressed() {
        return nextPressed;
    }

    public boolean isCancelPressed() {
        return cancelPressed;
    }
    
    
    private int getClass(int row) {
        int i;
        for (i = 1; i <= numberOfClasses + 1; i++) {
            if (((Boolean)(damTabModel.getValueAt(row, i))).booleanValue()) {
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
        area.append("Proceed with this file?");
        area.append("\n");
        area.setEditable(false);
        area.setBackground(Color.gray.brighter());
        JOptionPane.showMessageDialog(DAMClassificationEditor.this, area, "Warning", JOptionPane.WARNING_MESSAGE);
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
                int classCol = damTabModel.getColumnIndex(key);
                int[] selectedRows = damClassTable.getSelectedRows();
                int[] selectedIndices = new int[selectedRows.length];
                
                for (int i = 0; i < selectedRows.length; i++) {
                    damTabModel.setValueAt(new Boolean(true), selectedRows[i], classCol);
                    //int currIndex = ((Integer)(damTabModel.getValueAt(selectedRows[i], 0))).intValue();
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
                int colToSort = damTabModel.getColumnIndex(key);
                sortByColumn(colToSort, asc, origOrd);
            }
        }
    }
    
}
