/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
package org.tigr.microarray.mev.cluster.gui.helpers;


import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.*;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.tigr.microarray.mev.cluster.clusterUtil.Cluster;
import org.tigr.microarray.mev.cluster.clusterUtil.ClusterList;
import org.tigr.microarray.mev.cluster.clusterUtil.ClusterRepository;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentClusterTableViewer.ColorRenderer;
import org.tigr.util.FloatMatrix;

/**
 * @author  dschlauch
 */
public class ClusterSelector extends JPanel {
    
    JTable clusterTable;
    JTable clusterSamplesTable;
    ClusterRepository repository;
    JScrollPane pane;
    JScrollPane scrollPane;
    String clusterType = "Sample";
    ClusterTableModel model;
    FloatMatrix matrix;
    Boolean hasGraph;
    JComboBox[] groupComboBoxes;
    int numGroups;
    SampleTableModel clusterModel;
    JPanel tablePanel;
    /** Creates a new instance of ClusterSelector */
    public ClusterSelector(ClusterRepository repository, int numGroups) {
    	if(repository == null || repository.isEmpty()){
            add(new JLabel("Empty Cluster Repository"), java.awt.BorderLayout.CENTER);
            return;
        }
    	this.numGroups = numGroups;
        this.repository = repository;
        groupComboBoxes = new JComboBox[repository.getClusterSerialCounter()];
        for (int i=0;i<repository.getClusterSerialCounter();i++){
        	groupComboBoxes[i] = new JComboBox();
        	groupComboBoxes[i].addItem("Unassigned");
        	groupComboBoxes[i].addItem("Group A");
        	groupComboBoxes[i].addItem("Group B");
        	groupComboBoxes[i].setSelectedIndex(0);
        	groupComboBoxes[i].addActionListener(new Listener());
        }
        this.hasGraph = false;
        
        Font font = new Font("Dialog", Font.BOLD, 12);
        tablePanel = new JPanel(new GridBagLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), clusterType+" Clusters", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, font, Color.black));
        tablePanel.add(createClusterTable(), new GridBagConstraints(0,0,1,1,1.0,1.0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));

        if(clusterTable.getRowCount() > 0)
            clusterTable.addRowSelectionInterval(0,0); //select first cluster
        
        JPanel insetPanel = new JPanel(new GridBagLayout());
        insetPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        
        this.setLayout(new GridBagLayout());
        this.add(tablePanel, new GridBagConstraints(0,1,1,1,1.0,1.0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
        
        this.clusterModel = new SampleTableModel();
        this.clusterSamplesTable = new JTable(clusterModel);
        this.clusterSamplesTable.addMouseListener(new TableListener());
        this.clusterSamplesTable.setDefaultRenderer(Color.class, new ColorRenderer(true));
        scrollPane = new JScrollPane(clusterSamplesTable);
        this.add(scrollPane, new GridBagConstraints(0,2,1,1,1.0,1.0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
        
    }
    
    public void setClusterType(String clusterType){
    	if (repository==null) 
    		return;
    	this.clusterType = clusterType;
        Font font = new Font("Dialog", Font.BOLD, 12);
        tablePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), clusterType+" Clusters", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, font, Color.black));
        
    }
    
    public Cluster getSelectedCluster(){
       int serialNumber = ((ClusterTableModel)(this.clusterTable.getModel())).getClusterSerialNumber(this.clusterTable.getSelectedRow());
       return this.repository.getCluster(serialNumber);
    }
    public ArrayList<Integer> getGroupSamples(String key){
    	ClusterList list;
    	Cluster cluster;
    	ArrayList<Integer> groupsamps = new ArrayList<Integer>();
    
        for(int i = 0; i < repository.size(); i++){
            list = repository.getClusterList(i);
            for(int j = 0; j < list.size(); j++){
                if (key.equals(clusterTable.getModel().getValueAt(j, 8))){
                	cluster = list.getClusterAt(j);
                	for (int index=0; index<cluster.getIndices().length; index++){
                		if (!groupsamps.contains(cluster.getIndices()[index])){
                			groupsamps.add(cluster.getIndices()[index]);
                		}
                	}
                	
                }
            }
        }
        int[] groupA = new int[groupsamps.size()];
        for(int i=0; i<groupsamps.size(); i++){
        	groupA[i] = groupsamps.get(i);
        }
        
        return groupsamps;
    }
    
    private void setInitialColumnWidths(){
        String [] columnNames = model.getColumnNames();
        for(int i = 0; i < columnNames.length; i++){
            setColumnWidth(columnNames[i]);
        }
    }
    
   
    private void setColumnWidth(String headerName){
        TableColumn column;
        int width = 10;
        if(headerName.equals("Serial #")){
            column = clusterTable.getColumn(headerName);
            width = 50;
            column.setWidth(width);
            column.setMaxWidth(width);
            column.setPreferredWidth(width);
        } else if(headerName.equals("Source")){
            column = clusterTable.getColumn(headerName);
            width = 100;
            column.setWidth(width);
            column.setPreferredWidth(width);
        } else if(headerName.equals("Color")){
            column = clusterTable.getColumn(headerName);
            width = 60;
            column.setWidth(width);
            column.setPreferredWidth(width);
        } else if(headerName.equals("Size")){
            column = clusterTable.getColumn(headerName);
            width = 50;
            column.setWidth(width);
            column.setMaxWidth(width);
            column.setPreferredWidth(width);
        } else if (headerName.equals("Remarks")){
            column = clusterTable.getColumn(headerName);
            width = 75;
            column.setWidth(width);
        } else if (headerName.equals("Group Assignment")){
            column = clusterTable.getColumn(headerName);
            width = 125;
            column.setWidth(width);
        }
    }
    
    private JScrollPane createClusterTable(){
        
        Cluster cluster;
        ClusterList list;
        
        Vector<String> header = new Vector<String>();
        header.add("Serial #");
        header.add("Source");
        header.add("Algorithm Node");
        header.add("Cluster Node");
        header.add("Cluster Label");
        header.add("Remarks");
        header.add("Size");
        header.add("Color");
        header.add("Group Assignment");
        
        Vector<Object> dataVector = new Vector<Object>();
        
        int row = 0;
        /*
        for(int i = 0; i < repository.size(); i++){
            list = repository.getClusterList(i);
            for(int j = 0; j < list.size(); j++){
                cluster = list.getClusterAt(j);
                dataVector.add(new JLabel(String.valueOf(cluster.getSerialNumber())));
                dataVector.add(new JLabel(String.valueOf(cluster.getSource())));
                dataVector.add(new JLabel(String.valueOf(cluster.getAlgorithmName())));
                dataVector.add(new JLabel(String.valueOf(cluster.getClusterID())));
                dataVector.add(cluster.getClusterLabel());
                dataVector.add(cluster.getClusterDescription());
                dataVector.add(new JLabel(String.valueOf(cluster.getSize())));
                dataVector.add(cluster.getClusterColor());
                dataVector.add("Unassigned");
                //System.out.println("j: " + j);
                row++;
            }
        }*/
        
        int clustersFound = 0;
        int serialInt = 0;
        while (serialInt<repository.getClusterSerialCounter()){
        	serialInt++;
        	if (repository.getCluster(serialInt)==null)
        		continue;
        	cluster = repository.getCluster(serialInt);
            dataVector.add(new JLabel(String.valueOf(cluster.getSerialNumber())));
            dataVector.add(new JLabel(String.valueOf(cluster.getSource())));
            dataVector.add(new JLabel(String.valueOf(cluster.getAlgorithmName())));
            dataVector.add(new JLabel(String.valueOf(cluster.getClusterID())));
            dataVector.add(cluster.getClusterLabel());
            dataVector.add(cluster.getClusterDescription());
            dataVector.add(new JLabel(String.valueOf(cluster.getSize())));
            dataVector.add(cluster.getClusterColor());
            dataVector.add("Unassigned");
        	clustersFound++;
        }
        
        model = new ClusterTableModel(header, dataVector);
        clusterTable = new JTable(model);
        ClusterCellRenderer renderer = new ClusterCellRenderer();
        clusterTable.setDefaultRenderer(Color.class, renderer);
        clusterTable.setDefaultRenderer(JLabel.class, renderer);
        clusterTable.setPreferredScrollableViewportSize(new Dimension(450, 175));
        
        clusterTable.addMouseListener(new TableListener());
        clusterTable.setBackground(new Color(225,225,225));
        clusterTable.setBackground(new Color(225,225,225));
        
        clusterTable.setRowHeight(clusterTable.getRowHeight() + 10);
        
        clusterTable.setRowSelectionAllowed(true);
        clusterTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        String[] cbox = new String[numGroups+1];
        cbox[0] = "Unassigned";
        for (int i=0; i<numGroups;i++){
        	cbox[i+1]="Group "+(i+1);
        }
		clusterTable.getColumnModel().getColumn(8).setCellEditor(new DefaultCellEditor(new JComboBox(cbox)));
		
        setInitialColumnWidths();
        
        model.addTableModelListener(new TableListener());
        clusterTable.setRowHeight(30);
        pane = new JScrollPane(clusterTable);
        pane.setBackground(Color.white);
        
        validate();
        return pane;
    }

    class SampleTableModel extends AbstractTableModel {
        String[] columnNames;
        
        public SampleTableModel() {
            columnNames = new String[repository.getFramework().getData().getSampleAnnotationFieldNames().size()];// + auxTitles.length];

            for (int i = 0; i < columnNames.length; i++) {
                columnNames[i] = (String)repository.getFramework().getData().getSampleAnnotationFieldNames().get(i);
            }
        }
        
        public int getColumnCount() {
        	return repository.getFramework().getData().getSampleAnnotationFieldNames().size();
        }
        
        public int getRowCount() {
        	//System.out.println("row count: "+repository.getCluster(clusterTable.getSelectedRow()+1).getSize());
        	return repository.getCluster(clusterTable.getSelectedRow()+1).getSize();
        }
        
        public String getColumnName(int col) {
            return columnNames[col];            
        }        
        
        public Object getValueAt(int row, int col) {
        	//row=row+1;
            //if (col == 0) {
                //return Color.red;//repository.getFramework().getData().getExperimentColor(repository.getCluster(clusterTable.getSelectedRow()+1).getExperimentIndices()[row]) == null? Color.white : repository.getFramework().getData().getExperimentColor(repository.getCluster(clusterTable.getSelectedRow()+1).getExperimentIndices()[row]);
            if (col >= 0) {
            	try{
	            	String before = repository.getFramework().getData().getCurrentSampleLabelKey();
	            	repository.getFramework().getData().setSampleLabelKey((String)repository.getFramework().getData().getSampleAnnotationFieldNames().get(col));
	            	String value = repository.getFramework().getData().getSampleName((repository.getFramework().getData().getExperiment().getSampleIndex(repository.getCluster(clusterTable.getSelectedRow()+1).getIndices()[row])));
	            	repository.getFramework().getData().setSampleLabelKey(before);
            	
	            	return value;
            	}catch(Exception e){
            		e.printStackTrace();
            	}
            	return "error";
            } else {
                return null;//String.valueOf(auxData[getSortedCluster()[row]][col - 2]);
            }
        }
        
       public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }        
    }
    public class ColorRenderer extends JLabel implements TableCellRenderer {
        Border unselectedBorder = null;
        Border selectedBorder = null;
        boolean isBordered = true;
        
        public ColorRenderer(boolean isBordered) {
            this.isBordered = isBordered;
            setOpaque(true); //MUST do this for background to show up.
        }
        
        public Component getTableCellRendererComponent(JTable table, Object color, boolean isSelected, boolean hasFocus, int row, int column) {
            Color newColor = (Color)color;
            setBackground(newColor);
            if (isBordered) {
                if (isSelected) {
                    if (selectedBorder == null) {
                        selectedBorder = BorderFactory.createMatteBorder(2,5,2,5,
                        table.getSelectionBackground());
                    }
                    setBorder(selectedBorder);
                } else {
                    if (unselectedBorder == null) {
                        unselectedBorder = BorderFactory.createMatteBorder(2,5,2,5,
                        table.getBackground());
                    }
                    setBorder(unselectedBorder);
                }
            }          
            //setToolTipText(...); //Discussed in the following section
            return this;
        }
    }    
    
    
    public class ClusterTableModel extends AbstractTableModel{
        
        private String [] columnNames;
        private Object [][] rowData;
        private Row [] rows;
        private int colToSort = 0;
        
        
        public ClusterTableModel(Vector<String> columnNames, Vector<Object> rowData){
            initializeHeader(columnNames);
            initializeData(rowData);
            rows = new Row[this.rowData.length];
            for(int i = 0; i < rows.length; i++){
                rows[i] = new Row();
                rows[i].index = i;
            }
        }
        
        private void initializeHeader(Vector<String> headerNames){
            columnNames = new String[headerNames.size()];
            for(int i = 0; i < headerNames.size(); i++){
                columnNames[i] = (String)headerNames.elementAt(i);
            }
        }
        
        private void initializeData(Vector<Object> data){
            int cnt = 0;
            rowData = new Object[(int)(data.size()/columnNames.length)][columnNames.length];
            while(cnt < data.size()){
                for(int j = 0; j < columnNames.length; j++){
                    rowData[(int)(cnt/columnNames.length)][j] = data.elementAt(cnt);
                    cnt++;
                }
            }
        }
        
        
        public String getColumnName(int col) {
            return columnNames[col];
        }
        
        public int getRowCount() { return rowData.length; }
        
        public int getColumnCount() { return columnNames.length; }
        
        public Object getValueAt(int row, int col) {
            return rowData[rows[row].index][col];
        }
        
        public boolean isCellEditable(int row, int col) {
            return (col==8);
        }
        
        public void setValueAt(Object value, int row, int col) {
        	rowData[rows[row].index][col] = value;
            this.fireTableChanged(new TableModelEvent(this, row, row, col));
        }
        
        public Class getColumnClass(int col){
            if(col == 7) return Color.class;
            if(col == 4 || col == 5) return String.class;
            else return JLabel.class;
        }
        
        public int getClusterSerialNumber( int row ){
            if(isLegalRow(row)){
                return (Integer.parseInt(((JLabel)(getValueAt(row, 0))).getText()));
            }
            return -1;
        }
        
        public boolean isLegalRow(int row){
            return (row > -1 && row < getRowCount());
        }
        
        public boolean isLegalColumn(int col){
            return (col > -1 && col < getColumnCount());
        }
        
        /** Sorts table rows based on column index.
         * @param c Column to use as sort key when sorting columns
         */
        public void sort(int c){
            colToSort = c;
            Arrays.sort(rows);
            clusterTable.repaint();
        }
        
        /** Sorts table rows by column header key.
         * @param key <CODE>String</CODE> key to identify sort column.
         */
        public void sortBy(String key){
            int col = getColumnIndex(key);
            
            if(col >= 0){
                sort(col);
                colToSort = col;
            }
        }
        
        private int getColumnIndex(String key){
            for(int i = 0; i < columnNames.length; i++){
                if(columnNames[i] == key)
                    return i;
            }
            return 0;
        }
        
        public String [] getColumnNames(){ return this.columnNames; }
        
        public void hide(String columnName){
            clusterTable.removeColumn(clusterTable.getColumn(columnName));
        }
        
        public void addColumn(String columnName){
            clusterTable.addColumn( new TableColumn(getColumnIndex(columnName)));
            moveColumnFromEnd(getColumnIndex(columnName));
        }
        
        public void addRow(Vector<Object> data){
            Object [][] newData = new Object[rowData.length+1][rowData[0].length];
            for(int i = 0; i < rowData.length; i++){
                for(int j = 0; j < rowData[i].length; j++){
                    newData[i][j] = rowData[i][j];
                }
            }
            for(int i = 0; i < this.columnNames.length; i++){
                newData[newData.length -1][i] = data.elementAt(i);
            }
            rowData = newData;
            
            Row [] newRows = new Row[this.rowData.length];
            
            for(int i = 0; i < rows.length; i++){
                newRows[i] = rows[i];
            }
            newRows[newRows.length-1] = new Row();
            newRows[newRows.length-1].index = newRows.length-1;
            rows = newRows;
            this.fireTableRowsInserted(rows.length-1, rows.length-1);
        }
        
        public void removeRow(int tableRow){
            int row = rows[tableRow].index;
            Object [][] newData = new Object[rowData.length-1][rowData[0].length];
            int currRow = -1;
            for(int i = 0; i < rowData.length; i++){
                if(i != row){
                    currRow++;
                    for(int j = 0; j < rowData[i].length; j++){
                        newData[currRow][j] = rowData[i][j];
                    }
                }
            }
            rowData = newData;
            Row [] newRows = new Row[this.rowData.length];
            currRow = -1;
            for(int i = 0; i < rows.length; i++){
                if(i != row){
                    currRow++;
                    newRows[currRow] = rows[i];
                    newRows[currRow].index = currRow;
                }
            }
            rows = newRows;
            this.fireTableRowsDeleted(row,row);
        }
        
        public void removeAllRows(){
            int numRows = rows.length;
            rowData = new Object[0][0];
            rows = new Row[0];
            this.fireTableRowsDeleted(0, numRows);
        }
        
        private void moveColumnFromEnd(int finalLocation){
            for(int i = clusterTable.getColumnCount()-1; i > finalLocation; i--)
                clusterTable.moveColumn(i-1,i);
        }
        
        public int getSerialNumber(int row){
            JLabel serialLabel = (JLabel)(rowData[row][0]);
            return Integer.parseInt(serialLabel.getText());
        }
        
        public void setClusterColor(int tableRow, Color clusterColor){
            int row = rows[tableRow].index;
            int col = this.getColumnIndex("Color");
            setValueAt(clusterColor, row, col);
            this.fireTableCellUpdated(tableRow, col);
        }
        
        public void setClusterLabel(int tableRow, String clusterLabel){
            int row = rows[tableRow].index;
            int col = this.getColumnIndex("Cluster Label");
            setValueAt(clusterLabel, row, col);
            this.fireTableCellUpdated(tableRow, col);
        }
        
        public void setClusterDescription(int tableRow, String clusterDescription){
            int row = rows[tableRow].index;
            int col = this.getColumnIndex("Remarks");
            setValueAt(clusterDescription, row, col);
            this.fireTableCellUpdated(tableRow, col);
        }
        
        
        
        private class Row implements Comparable<Object>{
            public int index;
            private String myString, otherString;
            
            public int compareTo(Object other){
                Row otherRow = (Row)other;
                Object myObject = rowData[index][colToSort];
                Object otherObject = rowData[otherRow.index][colToSort];
                if(model.getColumnName(colToSort).equals("Serial #") ||
                model.getColumnName(colToSort).equals("Size")){
                    Integer i;
                    i = new Integer(((JLabel)rowData[index][colToSort]).getText());
                    return i.compareTo(new Integer(((JLabel)(otherObject)).getText()));
                }
                if( myObject instanceof Comparable )
                    return ((Comparable)myObject).compareTo(otherObject);
                if(myObject instanceof JLabel){
                    myString = ((JLabel)(myObject)).getText();
                    otherString = ((JLabel)(otherObject)).getText();
                    return myString.compareTo(otherString);
                }
                else return index - otherRow.index;
            }
        }
    }
    
    
    public class ClusterCellRenderer implements TableCellRenderer{
        
        private JPanel colorPanel = new JPanel();
        private JLabel label;
        private JTextArea textArea;
        
        public ClusterCellRenderer(){
            
        }
        
        public java.awt.Component getTableCellRendererComponent(JTable jTable, Object obj, boolean param, boolean param3, int row, int col) {
            
        	if(obj instanceof Color){
                colorPanel.setBackground((Color)obj);
                return colorPanel;
            } else if(obj instanceof JLabel){
                label = (JLabel)obj;
                label.setOpaque(true);
                label.setFont(new Font("Arial", Font.PLAIN, 12));
                label.setBackground(new Color(225, 225, 225));
                label.setForeground(Color.black);
                label.setHorizontalAlignment(JLabel.CENTER);
                if(clusterTable.isRowSelected(row))
                    label.setBackground(clusterTable.getSelectionBackground());
                return label;
            } else if(obj instanceof JTextArea){
                textArea = (JTextArea)obj;
                textArea.setBackground(new Color(225, 225, 225));
                if(clusterTable.isRowSelected(row))
                    textArea.setBackground(clusterTable.getSelectionBackground());
                return textArea;
            } else if(obj instanceof JComboBox){
            	return (JComboBox)obj;
            } else if (obj instanceof String){
            	String[] str = new String[1];
            	str[0] = (String)obj;
            	JComboBox tempCombo =new JComboBox(str);
            	tempCombo.setAlignmentX(JComboBox.CENTER_ALIGNMENT);
            	tempCombo.setBackground(Color.white);
            	return tempCombo;
            }
            colorPanel.setBackground(Color.white);
            return colorPanel;
        }
    }
    
    public class TableListener extends MouseAdapter implements TableModelListener {
        
        public void tableChanged(javax.swing.event.TableModelEvent tableModelEvent) {
        }
        
        public void mousePressed(MouseEvent evt){
            clusterSamplesTable.repaint();
            scrollPane.updateUI();
          
        }
        
        public void mouseReleased(MouseEvent evt){
        }
        
    }
    private class Listener implements ActionListener {
        
        public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
        }        

    }
    
}
