/*
Copyright @ 1999-2004, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * $RCSfile: ClusterBrowser.java,v $
 * $Revision: 1.8 $
 * $Date: 2006-02-23 20:59:48 $
 * $Author: caliente $
 * $State: Exp $
 */
/*
 * ClusterBrowser.java
 *
 * Created on August 25, 2003, 11:51 AM
 */
package org.tigr.microarray.mev.cluster.gui.helpers;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.tigr.graph.GraphCanvas;
import org.tigr.graph.GraphLine;
import org.tigr.microarray.mev.cluster.clusterUtil.Cluster;
import org.tigr.microarray.mev.cluster.clusterUtil.ClusterList;
import org.tigr.microarray.mev.cluster.clusterUtil.ClusterRepository;
import org.tigr.util.FloatMatrix;

/**
 * @author  braisted
 */
public class ClusterBrowser extends JPanel {
    
    JTable table;
    ClusterRepository repository;
    JScrollPane pane;
    String clusterTypeStr = "Gene";
    ClusterTableModel model;
    GraphCanvas profileDisplayPanel;
    FloatMatrix matrix;
    
    /** Creates a new instance of ClusterBrowser */
    public ClusterBrowser(ClusterRepository repository) {
        this.repository = repository;
        
        if(repository == null){
            add(new JLabel("Empty Cluster Repository"), java.awt.BorderLayout.CENTER);
            return;
        }
        
        if(!repository.isGeneClusterRepository()){
            clusterTypeStr = "Sample";
        }
        
        Font font = new Font("Dialog", Font.BOLD, 12);
        JPanel tablePanel = new JPanel(new GridBagLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), clusterTypeStr+" Clusters", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, font, Color.black));
        tablePanel.add(initializeTable(), new GridBagConstraints(0,0,1,1,1.0,1.0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));

        if(table.getRowCount() > 0)
            table.addRowSelectionInterval(0,0); //select first cluster
        
        JPanel insetPanel = new JPanel(new GridBagLayout());
        insetPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        
        JPanel graphPanel = new JPanel(new GridBagLayout());
        graphPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Cluster Graph", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, font, Color.black));
        insetPanel.add(initializeGraph(), new GridBagConstraints(0,0,1,1,1.0,1.0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
        graphPanel.add(insetPanel, new GridBagConstraints(0,0,1,1,1.0,1.0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
        
        graphPanel.setPreferredSize(new Dimension(100, 175));
        graphPanel.setMinimumSize(new Dimension(100, 175));
        
        this.setLayout(new GridBagLayout());
        this.add(graphPanel, new GridBagConstraints(0,0,1,1,1.0,0.0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
        this.add(tablePanel, new GridBagConstraints(0,1,1,1,1.0,1.0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
    }
   
    public Cluster getSelectedCluster(){
       int serialNumber = ((ClusterTableModel)(this.table.getModel())).getClusterSerialNumber(this.table.getSelectedRow());
       return this.repository.getCluster(serialNumber);
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
            column = table.getColumn(headerName);
            width = 50;
            column.setWidth(width);
            column.setMaxWidth(width);
            column.setMinWidth(width);
            column.setPreferredWidth(width);
        } else if(headerName.equals("Source")){
            column = table.getColumn(headerName);
            width = 100;
            column.setWidth(width);
            column.setPreferredWidth(width);
        } else if(headerName.equals("Color")){
            column = table.getColumn(headerName);
            width = 60;
            column.setWidth(width);
            column.setPreferredWidth(width);
        } else if(headerName.equals("Size")){
            column = table.getColumn(headerName);
            width = 50;
            column.setWidth(width);
            column.setMaxWidth(width);
            column.setMinWidth(width);
            column.setPreferredWidth(width);
        }
    }
    
    private GraphCanvas initializeGraph(){
        profileDisplayPanel = new GraphCanvas();
        profileDisplayPanel.setGraphBounds(0, 10, -3, 3);  //xRange == 10, until refreshGraph sets to proper size
        profileDisplayPanel.setGraphSpacing(20, 20, 20, 20);
        profileDisplayPanel.setXAxisValue(0);
        profileDisplayPanel.setYAxisValue(0);
        
        refreshGraph();
        return profileDisplayPanel;
    }
    
    
    private void refreshGraph(){
        
        int index = table.getSelectedRow();
        if(index < 0)
            return;
        if (table.getRowCount() <= 0) return;
        int serialNumber = model.getClusterSerialNumber(index);
        Cluster cluster = repository.getCluster(serialNumber);
        this.matrix = cluster.getExperiment().getMatrix();
        if(this.clusterTypeStr.equals("Sample"))
            this.matrix = this.matrix.transpose();
        
        int [] indices = cluster.getExperimentIndices();  //get the indices that map to the experiment
        Color clusterColor = cluster.getClusterColor();
        int xRange = matrix.getColumnDimension();
        
        
        
        //debug
       /*System.out.println("cluster indices");
        for(int i = 0; i < indices.length; i++)
            System.out.println("cluster index = "+indices[i]);
        
        int [] expindices = cluster.getExperiment().getRowMappingArrayCopy();
        System.out.println("\nExp indices indices");
        for(int i = 0; i < expindices.length; i++)
            System.out.println("exp index = "+expindices[i]);
        */
        
        float min = Float.POSITIVE_INFINITY;
        float max = Float.NEGATIVE_INFINITY;
        float maxRange;
        profileDisplayPanel.removeAllGraphElements();
        int currIndex;
        GraphLine gL;
        
        for (int j = 0; j < indices.length; j++) {
            currIndex = indices[j];
            
            max = Math.max(max, getMax(currIndex));
            min = Math.min(min, getMin(currIndex));
            maxRange = Math.max(max, Math.abs(min));
            profileDisplayPanel.setGraphBounds(0, xRange-1, -maxRange, maxRange);
            
            for (int i = 0; i < xRange - 1; i++) {
                if ((Float.isNaN((matrix.get(currIndex,i))))||(Float.isNaN(matrix.get(currIndex, i +1)))) {
                    continue;
                }
                gL = new GraphLine(i /*+ 1*/, matrix.get(currIndex,i),
                i + 1, matrix.get(currIndex, i+1), clusterColor);
                profileDisplayPanel.addGraphElement(gL);
            }
        }
            /*
            for (int i = 0; i < meanProfile.size(); i++) {
                if (!Float.isNaN(((Float) meanProfile.elementAt(i)).floatValue())) {
                    GraphPoint gp = new GraphPoint(i, ((Float) meanProfile.elementAt(i)).floatValue(), Color.red, 5);
                    profileDisplayPanel.addGraphElement(gp);
                }
            }
             
            for (int i = 0; i < meanProfile.size() - 1; i++) {
                gL = new GraphLine(i , ((Float) meanProfile.elementAt(i)).floatValue(),
                i + 1, ((Float) meanProfile.elementAt(i + 1)).floatValue(), Color.blue);
                profileDisplayPanel.addGraphElement(gL);
            }
             */
            /*
            if(setTemplate){
             
                for (int i = 0; i < meanProfile.size() - 1; i++) {
                    if ((Float.isNaN(((Float) meanProfile.elementAt(i)).floatValue())) || (Float.isNaN(((Float) meanProfile.elementAt(i+1)).floatValue()))) {
                        continue;
                    }
                    gL = new GraphLine(i, ((Float) meanProfile.elementAt(i)).floatValue(),
                    i + 1, ((Float) meanProfile.elementAt(i + 1)).floatValue(), Color.green);
                    profileDisplayPanel.addGraphElement(gL);
                }
            }
             */
        profileDisplayPanel.repaint();
    }
    
    private float getMax(int index){
        float max = Float.NEGATIVE_INFINITY;
        int cols = matrix.getColumnDimension();
        float val;
        for(int i = 0; i < cols; i++){
            val = matrix.get(index, i);
            if(!Float.isNaN(val))
                if(max < val)
                    max = val;
        }
        return max;
    }
    
    private float getMin(int index){
        float min = Float.POSITIVE_INFINITY;
        int cols = matrix.getColumnDimension();
        float val;
        for(int i = 0; i < cols; i++){
            val = matrix.get(index, i);
            if(!Float.isNaN(val))
                if(min > val)
                    min = val;
        }
        return min;
    }
    
    private JScrollPane initializeTable(){
        
        Cluster cluster;
        ClusterList list;
        JLabel colorLabel;
        JTextArea remarksTextArea;
        
        Vector headerVector = new Vector();
        headerVector.add("Serial #");
        headerVector.add("Source");
        headerVector.add("Algorithm Node");
        headerVector.add("Cluster Node");
        headerVector.add("Cluster Label");
        headerVector.add("Remarks");
        headerVector.add("Size");
        headerVector.add("Color");
        
        Vector dataVector = new Vector();
        
        int row = 0;
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
                row++;
            }
        }
        model = new ClusterTableModel(headerVector, dataVector);
        table = new JTable(model);
        ClusterCellRenderer renderer = new ClusterCellRenderer();
        table.setDefaultRenderer(Color.class, renderer);
        table.setDefaultRenderer(JLabel.class, renderer);
        table.setPreferredScrollableViewportSize(new Dimension(450, 175));
        table.addMouseListener(new TableListener());
        table.setBackground(Color.white);
        table.setRowHeight(table.getRowHeight() + 10);
        
        table.setRowSelectionAllowed(true);
        table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        setInitialColumnWidths();
        
        model.addTableModelListener(new TableListener());
        table.setRowHeight(30);
        pane = new JScrollPane(table);
        pane.setBackground(Color.white);
        
        validate();
        return pane;
    }
    
    
    public class ClusterTableModel extends AbstractTableModel{
        
        private String [] columnNames;
        private Object [][] rowData;
        private Row [] rows;
        private int colToSort = 0;
        
        
        public ClusterTableModel(Vector columnNames, Vector rowData){
            initializeHeader(columnNames);
            initializeData(rowData);
            rows = new Row[this.rowData.length];
            for(int i = 0; i < rows.length; i++){
                rows[i] = new Row();
                rows[i].index = i;
            }
        }
        
        private void initializeHeader(Vector headerNames){
            columnNames = new String[headerNames.size()];
            for(int i = 0; i < headerNames.size(); i++){
                columnNames[i] = (String)headerNames.elementAt(i);
            }
        }
        
        private void initializeData(Vector data){
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
            return (col == 7);
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
            table.repaint();
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
            table.removeColumn(table.getColumn(columnName));
        }
        
        public void addColumn(String columnName){
            table.addColumn( new TableColumn(getColumnIndex(columnName)));
            moveColumnFromEnd(getColumnIndex(columnName));
        }
        
        public void addRow(Vector data){
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
            for(int i = table.getColumnCount()-1; i > finalLocation; i--)
                table.moveColumn(i-1,i);
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
        
        
        private class Row implements Comparable{
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
                if(table.isRowSelected(row))
                    label.setBackground(table.getSelectionBackground());
                return label;
            } else if(obj instanceof JTextArea){
                textArea = (JTextArea)obj;
                if(table.isRowSelected(row))
                    textArea.setBackground(table.getSelectionBackground());
                return textArea;
            }
            colorPanel.setBackground(Color.white);
            return colorPanel;
        }
    }
    
    public class TableListener extends MouseAdapter implements TableModelListener {
        
        public void tableChanged(javax.swing.event.TableModelEvent tableModelEvent) {
            refreshGraph();
        }
        
        public void mousePressed(MouseEvent evt){
            refreshGraph();
        }
        
        public void mouseReleased(MouseEvent evt){
            refreshGraph();
        }
        
    }
    
}
