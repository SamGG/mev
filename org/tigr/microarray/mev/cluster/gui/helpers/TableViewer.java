/*
 * TableViewer.java
 *
 * Created on September 5, 2003, 10:17 AM
 */

package org.tigr.microarray.mev.cluster.gui.helpers;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableModel;

import java.util.Arrays;

import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IDisplayMenu;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.IViewer;
/** The TableViewer class is provided as a JPanel extention to be used
 * as a means of presenting tabular data.  The TableViewer can be added
 * to any component and can recieve a variety of data types.
 * The JTable, TableModel, and JTableHeader are all returned by methods
 * so that interaction can be direct.
 * @author braisted
 */


public class TableViewer extends JPanel implements IViewer, java.io.Serializable {
    
    protected JTable table;
    protected TableModel model;
    protected JScrollPane pane;
    protected IFramework framework;
    
    /** Creates a new instance of TableViewer */
    public TableViewer() { }
    
    /** Creates a new TableViewer with header names and data.
     * @param headerNames Header name strings.
     * @param data table data
     */    
    public TableViewer(String [] headerNames, Object [][] data) {
        model = new DefaultViewerTableModel(headerNames, data);
        
        table = new JTable(model);
        table.getTableHeader().addMouseListener(new TableHeaderMouseListener());
        
        pane = new JScrollPane(table);
      
        this.setLayout(new GridBagLayout());
        add(pane, new GridBagConstraints(0,0,1,1,1.0,1.0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));
    }
    
    private void writeObject(java.io.ObjectOutputStream oos) throws java.io.IOException {
        oos.writeObject(this.model);
    }
    
    
    private void readObject(java.io.ObjectInputStream ois) throws java.io.IOException, ClassNotFoundException {                
        this.model = (TableModel)ois.readObject();
        this.table = new JTable(model);
        this.table.getTableHeader().addMouseListener(new TableHeaderMouseListener());       
        pane = new JScrollPane(table);      
        this.setLayout(new GridBagLayout());
        add(pane, new GridBagConstraints(0,0,1,1,1.0,1.0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));        
    }
    
    /** Allows the substitution of a specific table model.
     * @param model This model replaces the TableViewer's default TableModel.
     */    
    public void setTableModel(TableModel model){
        this.model = model;
        table.setModel(model);
    }
    
    /** Returns the table.
     * @return table component
     */    
    public JTable getTable(){
        return this.table;
    }
    
    /** Returns the active TableModel.
     * @return The tables data model
     */    
    public TableModel getTableModel(){
        return this.table.getModel();
    }
    
    /** Returns the table's header component
     * @return table header
     */    
    public JTableHeader getTableHeader(){
        return this.table.getTableHeader();
    }
    
    /** Indicates that the indexed column should be set to numerical
     * regardles of the object type.  This will assist in proper sorting
     * if a numerical column is represented by Strings.
     * (By default columns are not numerical)
     * @param columnIndex index to the table column
     * @param setting sets as numerical.
     */    
    public void setNumerical(int columnIndex, boolean setting){
        if(this.model instanceof DefaultViewerTableModel)
            ((DefaultViewerTableModel)this.model).setNumerical(columnIndex, setting);
    }
    
    public int getSelectedRow(){
        int index = table.getSelectedRow();
        if(index < 0)
            index = -1;
        else
            index = ((DefaultViewerTableModel) this.model).getRow(index);
        return index;        
    }
    
    
    /**
     *  Internal Classes
     *
     */    
    public class DefaultViewerTableModel extends AbstractTableModel implements java.io.Serializable {
        String[] columnNames;
        Object[][] tableData;
        boolean [] numerical;
        Row [] rows;
        int colToSort = 0;
        boolean ascending = false;
        
        /** This inner class is used to support basic manipulation of the table.
         * The table helps to support ascending and descending row sorting based
         * on numerical or alphabetical column contents.
         * @param headerNames header names
         * @param data data matrix
         */        
        public DefaultViewerTableModel(String [] headerNames, Object [][] data){
            columnNames = headerNames;
            tableData = data;
            numerical = new boolean[headerNames.length];
            rows = new Row[data.length];
            for(int i = 0; i < rows.length; i++){
                rows[i] = new Row();
                rows[i].index = i;
            }
        }
        
        /** Sets column as numerical for sorting.
         * @param col column index
         * @param numericalBool sets as numerical or not numerical
         */        
        public void setNumerical(int col, boolean numericalBool){
            if(col > -1 && col < numerical.length)
                numerical[col] = numericalBool;
        }
        
        public int getColumnCount() {
            return columnNames.length;
        }
        
        public int getRowCount() {
            if(tableData == null)
                return 0;
            else
                return tableData.length;
        }
        
        public Object getValueAt(int param, int param1) {
            if(tableData != null && param < tableData.length && param1 < tableData[param].length)
                return tableData[rows[param].index][param1];
            return null;
        }
        
        public String getColumnName(int index){
            return columnNames[index];
        }
        
        private boolean isNumerical(int col){
            return numerical[col];
        }
        
        public void sort(int col){
            ascending = !ascending;
            colToSort = col;
            Arrays.sort(rows);
            fireTableDataChanged();
        }
        
        public int getRow(int tableRow){
            return rows[tableRow].index;
        }
       
        
        private class Row implements Comparable, java.io.Serializable {
            public int index;
            private String myString, otherString;
            
            public int compareTo(Object other){
                if(ascending)
                    return compareToOther(other);
                return compareToOther(other)*(-1);
            }
            
            public int compareToOther(Object other){
                Row otherRow = (Row)other;
                Object myObject = tableData[index][colToSort];
                Object otherObject = tableData[otherRow.index][colToSort];
                if( myObject instanceof Comparable ) {
                    if(isNumerical(colToSort)){  //catch string designation of a number
                        if(myObject instanceof String){
                            System.out.println("Sort numerical");
                            Float myFloat = new Float((String)myObject);
                            Float otherFloat = new Float((String)otherObject);
                            return myFloat.compareTo(otherFloat);
                        }
                    }
                    return ((Comparable)myObject).compareTo(otherObject);
                }
                if(myObject instanceof JLabel){
                    myString = ((JLabel)(myObject)).getText();
                    otherString = ((JLabel)(otherObject)).getText();
                    return myString.compareTo(otherString);
                }
                else return index - otherRow.index;
            }
        }
        
    }
    
    
    
    
    public class CellRenderer extends DefaultTableCellRenderer {
        
        JPanel colorPanel = new JPanel();
        JLabel label;
        JTextArea textArea;
        
        /** Renders basic data input types JLabel, Color,
         */        
        public Component getTableCellRendererComponent(JTable jTable, Object obj, boolean param, boolean param3, int row, int col) {
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
        

    
    public static void main(String [] args){
        String [] headers = new String[3];
        headers[0] = "Gene";
        headers[1] = "GenBank #";
        headers[2] = "Common Name";
        
        Object [][] data = new Object[3][3];
        for(int i = 0; i < data.length ; i++)
            for(int j = 0; j < data[0].length; j++)
                if(i == 1)
                    data[i][j] = "1000";
                else
                    data[i][j] = Integer.toString(i*j+j);
        TableViewer tv = new TableViewer(headers, data);
        tv.setNumerical(0, true);
        tv.setNumerical(1, true);
        tv.setNumerical(2, false);
        JFrame frame = new JFrame();
        frame.getContentPane().add(tv);
        frame.setSize(100,100);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
    }
    
    
    public class TableHeaderMouseListener extends MouseAdapter{
        
        public void mouseClicked(MouseEvent evt) {
            if(evt.getModifiers() == evt.BUTTON1_MASK && evt.getClickCount() > 1){
                if(model instanceof DefaultViewerTableModel){
                    JTableHeader header = (JTableHeader)evt.getSource();
                    int tableCol = header.columnAtPoint(evt.getPoint());
                    int modelCol = table.convertColumnIndexToModel(tableCol);
                    ((DefaultViewerTableModel)model).sort(modelCol);
                }
            }
        }
    }
    
    
    /**
     *      IViewer implementation Methods (Default Implementation)
     *
     */
    
    
    /** Returns a component to be inserted into scroll pane view port.
     * @return content component (JTable)
     */
    public JComponent getContentComponent() {
        return this.table;
    }
    
    /** Returns a component to be inserted into scroll pane header.
     * @return table header component.
     */
    public JComponent getHeaderComponent() {
        return table.getTableHeader();
    }
    
    /** Invoked by the framework to save or to print viewer image.
     * @return Image or Null
     */
    public BufferedImage getImage() {
        return null;
    }
    
    /** Returns a component to be inserted into the scroll pane row header
     * @return Row header component or null
     */
    public JComponent getRowHeaderComponent() {
        return null;
    }
    
    /** Invoked when the framework is going to be closed.
     */
    public void onClosed() {
    }
    
    /** Invoked by the framework when data is changed,
     * if this viewer is selected.
     * @see IData
     */
    public void onDataChanged(IData data) {
    }
    
    /** Invoked by the framework when this viewer was deselected.
     */
    public void onDeselected() {
    }
    
    /** Invoked by the framework when display menu is changed,
     * if this viewer is selected.
     * @see IDisplayMenu
     */
    public void onMenuChanged(IDisplayMenu menu) {
    }
    
    /** Invoked by the framework when this viewer is selected.
     * @param framework The IFramework implementation
     */
    public void onSelected(IFramework framework) {
        this.framework = framework;
    }
    
    /** Returns the corner component corresponding to the indicated corner,
     * posibly null
     */
    public JComponent getCornerComponent(int cornerIndex) {
        return null;
    }
    
    public int[][] getClusters() {
        return null;
    }
    
    public Experiment getExperiment() {
        return null;
    }
    
}
