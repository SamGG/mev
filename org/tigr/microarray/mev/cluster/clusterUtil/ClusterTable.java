/*
Copyright @ 1999-2004, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * $RCSfile: ClusterTable.java,v $
 * $Revision: 1.9 $
 * $Date: 2005-02-24 20:24:12 $
 * $Author: braistedj $
 * $State: Exp $
 */

package org.tigr.microarray.mev.cluster.clusterUtil;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.event.CellEditorListener;
import javax.swing.event.EventListenerList;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.tree.DefaultMutableTreeNode;

import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IDisplayMenu;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.IViewer;
import org.tigr.microarray.mev.cluster.gui.impl.GUIFactory;

public class ClusterTable extends JPanel implements IViewer {
    
    private JTable table;
    private JPopupMenu menu;
    private JScrollPane pane;
    private ClusterRepository repository;
    private ClusterTableModel model;
    private IFramework framework;
    private boolean geneClusterTable;
    
    /** Creates new ClusterTablePanel */
    public ClusterTable(ClusterRepository rep, IFramework framework) {
        super(new GridBagLayout());
        this.framework = framework;
        this.repository = rep;
        this.geneClusterTable = rep.isGeneClusterRepository();
        setBackground(Color.white);
        MenuListener menuListener = new MenuListener();
        initializeTable();
        initializeMenu(menuListener);
    }
    
    
    
    private void initializeTable(){
        
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
        headerVector.add("Show Color");
        
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
                dataVector.add(new Boolean(cluster.showColor()));
                row++;
            }
        }
        model = new ClusterTableModel(headerVector, dataVector);
        table = new JTable(model);
        ClusterCellRenderer renderer = new ClusterCellRenderer();
        table.setDefaultRenderer(Color.class, renderer);
        table.setDefaultRenderer(JLabel.class, renderer);
      //  table.setDefaultRenderer(Boolean.class, new javax.swing.table.DefaultTableCellRenderer());
        table.setPreferredScrollableViewportSize(new Dimension(450, 175));
        table.addMouseListener(new TableListener());
        table.setBackground(Color.white);
        table.setRowHeight(table.getRowHeight() + 10);
        
        table.setRowSelectionAllowed(true);
        table.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        
        setInitialColumnWidths();
        
        model.addTableModelListener(new TableListener());
        table.setRowHeight(30);
        pane = new JScrollPane(table);
        pane.setBackground(Color.white);
        
        add(pane, new GridBagConstraints(0,0,0,0,1.0,1.0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0, 0));
        validate();
    }
    
    /**
     * @param row Removes a row from the table
     */
    public void removeRow(int row){
        model.removeRow(row);
    }
    
    /**
     *  Adds a cluster to the table
     */
    public void addCluster(Cluster cluster){
        Vector dataVector = new Vector();
        
        dataVector.add(new JLabel(String.valueOf(cluster.getSerialNumber())));
        dataVector.add(new JLabel(String.valueOf(cluster.getSource())));
        dataVector.add(new JLabel(String.valueOf(cluster.getAlgorithmName())));
        dataVector.add(new JLabel(String.valueOf(cluster.getClusterID())));
        dataVector.add(cluster.getClusterLabel());
        JLabel lab = new JLabel();
        
        dataVector.add(cluster.getClusterDescription());
        dataVector.add(new JLabel(String.valueOf(cluster.getSize())));
        dataVector.add(cluster.getClusterColor());
        dataVector.add(new Boolean(cluster.showColor()));
        
        model.addRow(dataVector);
    }
    
    private void initializeMenu(MenuListener listener){
        JMenuItem item;
        this.menu = new JPopupMenu();
        
        item = new JMenuItem("Modify Attributes", GUIFactory.getIcon("empty16.gif"));
        item.setActionCommand("modify-command");
        item.addActionListener(listener);
        menu.add(item);
        menu.addSeparator();
        
        menu.add(initializeOpenMenu(listener));
        menu.addSeparator();
        
        this.menu.add( initializeClusterOperationsMenu(listener) );
        this.menu.addSeparator();
        
        this.menu.add( initializeSortMenu(listener) );
        this.menu.addSeparator();
        
        this.menu.add( initializeHideMenu(listener) );
        this.menu.addSeparator();
        
        item = new JMenuItem("Delete Selected", GUIFactory.getIcon("delete16.gif"));
        item.setActionCommand("delete-command");
        item.addActionListener(listener);
        this.menu.add(item);
        
        item = new JMenuItem("Delete All", GUIFactory.getIcon("delete16.gif"));
        item.setActionCommand("delete-all-command");
        item.addActionListener(listener);
        this.menu.add(item);
        this.menu.addSeparator();
        
        item = new JMenuItem("Save Cluster", GUIFactory.getIcon("save16.gif"));
        item.setActionCommand("save-cluster-command");
        item.addActionListener(listener);
        this.menu.add(item);
        
        this.menu.addSeparator();
        
        if(repository.isGeneClusterRepository()) {
            item = new JMenuItem("Import Gene List", GUIFactory.getIcon("empty.gif"));
        } else {
            item = new JMenuItem("Import Experiment List", GUIFactory.getIcon("empty.gif"));
        }
        item.setActionCommand("import-list-command");
        item.addActionListener(listener);
        this.menu.add(item);
        
        if(repository.isGeneClusterRepository()) {
            menu.addSeparator();
            
            item = new JMenuItem("Submit Gene List (External Repository)", GUIFactory.getIcon("empty.gif"));
            item.setActionCommand("submit-list-command");
            item.addActionListener(listener);
            this.menu.add(item);
        }
    }
    
    private JMenu initializeModifyMenu(MenuListener listener){
        JMenu menu = new JMenu("Modify Cluster");
        JMenuItem item;
        
        item = new JMenuItem("Modify Attributes");
        item.setActionCommand("modify-command");
        item.addActionListener(listener);
        menu.add(item);
        menu.addSeparator();
        
        item = new JMenuItem("Modify Membership");
        item.setActionCommand("modify-membership-command");
        item.addActionListener(listener);
        menu.add(item);
        
        
        return menu;
    }
    
    private JMenu initializeOpenMenu(MenuListener listener){
        JMenu menu = new JMenu("Open/Launch");
        menu.setIcon(GUIFactory.getIcon("open_launch.gif"));
        JMenuItem item;
        
        item = new JMenuItem("Open Cluster Viewer", GUIFactory.getIcon("Open16.gif"));
        item.setActionCommand("go-to-origin-command");
        item.addActionListener(listener);
        menu.add(item);
        menu.addSeparator();
        
        item = new JMenuItem("Launch MeV Session", GUIFactory.getIcon("launch_new_mav.gif"));
        item.setActionCommand("launch-new-command");
        item.addActionListener(listener);
        menu.add(item);
        
        return menu;
    }
    
    
    private JMenu initializeHideMenu(MenuListener listener){
        JMenu menu = new JMenu("Hide Columns");
        menu.setIcon(GUIFactory.getIcon("empty16.gif"));
        JCheckBoxMenuItem item;
        
        for(int i = 0; i < model.getColumnCount(); i++){
            item = new JCheckBoxMenuItem(model.getColumnName(i));
            item.setActionCommand("hide-command");
            item.setSelected(false);
            item.addActionListener(listener);
            menu.add(item);
        }
        menu.addSeparator();
        JMenuItem showAll = new JMenuItem("Show All");
        showAll.setActionCommand("show-all-command");
        showAll.addActionListener(listener);
        menu.add(showAll);
        return menu;
    }
    
    private JMenu initializeSortMenu(MenuListener listener){
        JMenu menu = new JMenu("Sort");
        menu.setIcon(GUIFactory.getIcon("empty16.gif"));
        JRadioButtonMenuItem item;
        ButtonGroup bg = new ButtonGroup();
        for(int i = 0; i < model.getColumnCount() - 1; i++){
            item = new JRadioButtonMenuItem(model.getColumnName(i));
            item.setActionCommand("sort-command");
            if((model.getColumnName(i)).equals("Serial #"))
                item.setSelected(true);
            else
                item.setSelected(false);
            bg.add(item);
            item.addActionListener(listener);
            menu.add(item);
        }
        return menu;
    }
    
    private JMenu initializeClusterOperationsMenu(MenuListener listener){
        JMenu menu = new JMenu("Cluster Operations");
        menu.setIcon(GUIFactory.getIcon("cluster_operations.gif"));
        JMenuItem item;
        ButtonGroup bg = new ButtonGroup();
        for(int i = 0; i < 3; i++){
            item = new JMenuItem();
            if(i == 0){
                item.setText("Intersection");
                item.setIcon(GUIFactory.getIcon("intersection.gif"));
            } else if(i == 1){
                item.setText("Union");
                item.setIcon(GUIFactory.getIcon("union.gif"));
            } else if(i == 2){
                item.setText("XOR");
                item.setIcon(GUIFactory.getIcon("xor.gif"));
            }
            item.setActionCommand("cluster-operations-command");
            bg.add(item);
            item.addActionListener(listener);
            menu.add(item);
        }
        return menu;
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
            return (col == 7 || col == 8);
        }
        
        public void setValueAt(Object value, int row, int col) {
            rowData[rows[row].index][col] = value;
            this.fireTableChanged(new TableModelEvent(this, row, row, col));
        }
        
        public Class getColumnClass(int col){
            if(col == 7) return Color.class;
            if(col == 4 || col == 5) return String.class;
            if(col == 8) return Boolean.class;
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
            Object [][] newData = new Object[rowData.length+1][columnNames.length];
            
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
            JLabel serialLabel = (JLabel)(rowData[rows[row].index][0]);
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
            } else if(obj instanceof Boolean) {
                System.out.println("Handle Boolean");
                JCheckBox box = new JCheckBox();
                box.setBackground(Color.white);
                box.setHorizontalAlignment(JCheckBox.CENTER);
                box.setSelected(((Boolean)obj).booleanValue());
                return box;
            }
            colorPanel.setBackground(Color.white);
            return colorPanel;
        }
    }
    
    
    
    public class TableListener implements TableModelListener, MouseListener{
        
        public TableListener(){
            super();
        }
        
        public void tableChanged(TableModelEvent tableModelEvent) {
        }
        
        public void mouseExited(MouseEvent mouseEvent) {
        }
        
        public void mouseReleased(MouseEvent mouseEvent) {
            if(!mouseEvent.isPopupTrigger()){
                int col = table.getSelectedColumn();
                int row = table.getSelectedRow();
                if(!model.isLegalRow(row) || !model.isLegalColumn(col))
                    return;
                if(table.getColumnClass(col) == Color.class)
                    modifyColor(row, col);
                else if(table.getColumnClass(col) == Boolean.class)
                    modifyShowColor(row, col);
            } else {
                if(mouseEvent.isPopupTrigger()){
                    int menuSize = menu.getComponentCount();
                    int selectionSize = table.getSelectedRowCount();
                    int [] selectedRows = table.getSelectedRows();
                    if(selectionSize < 1)
                        return;
                    enableAllMenuItems();
                    Component component;
                    JMenuItem item;
                    JMenu aMenu;
                    String menuString;
                    for(int i = 0; i < menuSize; i++){
                        component = menu.getComponent(i);
                        if(component instanceof JMenuItem){
                            item = (JMenuItem)component;
                            menuString = item.getText();
                            if(selectionSize != 1 && menuString.equals("Modify Attributes"))
                                component.setEnabled(false);
                            if( selectionSize != 1 && menuString.equals("Save Cluster"))
                                component.setEnabled(false);
                            if(selectionSize < 2 && menuString.equals("Cluster Operations"))
                                component.setEnabled(false);
                            if(selectionSize != 1 &&  menuString.equals("Modify Cluster"))
                                component.setEnabled(false);
                            if(selectionSize != 1 && menuString.equals("Open/Launch")){
                                ((JMenu)item).getMenuComponent(0).setEnabled(false);
                            } else if(menuString.equals("Open/Launch") && !(repository.getCluster(model.getSerialNumber(selectedRows[0])).getSource().equals("Algorithm"))) {
                                ((JMenu)item).getMenuComponent(0).setEnabled(false);
                            }
                            
                        } else if(component instanceof JMenu){
                            aMenu = (JMenu)component;
                            menuString = aMenu.getText();
                            if(selectionSize != 1 && ( menuString.equals("Modify Cluster") || menuString.equals("Open/Launch")))
                                aMenu.setEnabled(false);
                            else if(menuString.equals("Open/Lanuch") && !(repository.getCluster(model.getSerialNumber(selectedRows[0])).getSource().equals("Algorithm"))) {
                                aMenu.getMenuComponent(1).setEnabled(false);
                            } else
                                aMenu.setEnabled(true);
                        }
                    }
                    menu.show(table, mouseEvent.getX(), mouseEvent.getY());
                }
            }
            
        }
        
        public void mousePressed(MouseEvent mouseEvent) {
            String command;
            Component component;
            if(mouseEvent.isPopupTrigger()){
                int menuSize = menu.getComponentCount();
                int selectionSize = table.getSelectedRowCount();
                int [] selectedRows = table.getSelectedRows();
                if(selectionSize < 1)
                    return;
                enableAllMenuItems();
                JMenuItem item;
                JMenu aMenu;
                String menuString;
                for(int i = 0; i < menuSize; i++){
                    component = menu.getComponent(i);
                    if(component instanceof JMenuItem){
                        item = (JMenuItem)component;
                        menuString = item.getText();
                        if( selectionSize != 1 && menuString.equals("Modify Attributes"))
                            component.setEnabled(false);
                        if( selectionSize != 1 && menuString.equals("Save Cluster"))
                            component.setEnabled(false);
                        if(selectionSize < 2 && menuString.equals("Cluster Operations"))
                            component.setEnabled(false);
                        if(selectionSize != 1 &&  menuString.equals("Modify Cluster"))
                            component.setEnabled(false);
                        if(selectionSize != 1 && menuString.equals("Open/Launch")){
                            ((JMenu)item).getMenuComponent(0).setEnabled(false);
                        } else if(menuString.equals("Open/Launch") && !(repository.getCluster(model.getSerialNumber(selectedRows[0])).getSource().equals("Algorithm"))) {
                            ((JMenu)item).getMenuComponent(0).setEnabled(false);
                        }
                        
                    } else if(component instanceof JMenu){
                        aMenu = (JMenu)component;
                        menuString = aMenu.getText();
                        if(selectionSize != 1 && ( menuString.equals("Modify Cluster") || menuString.equals("Open/Launch")))
                            aMenu.setEnabled(false);
                        else if(menuString.equals("Open/Lanuch") && !(repository.getCluster(model.getSerialNumber(selectedRows[0])).getSource().equals("Algorithm"))) {
                            aMenu.getMenuComponent(1).setEnabled(false);
                        } else
                            aMenu.setEnabled(true);
                    }
                }
                menu.show(table, mouseEvent.getX(), mouseEvent.getY());
            }
        }
        
        public void mouseClicked(MouseEvent mouseEvent) {
        }
        
        public void mouseEntered(MouseEvent mouseEvent) {
        }
    }
    
    public class ClusterCellEditor implements javax.swing.table.TableCellEditor{
        
        JTextArea textArea;
        javax.swing.event.EventListenerList list = new EventListenerList();
        
        public ClusterCellEditor(JTextArea ta){
            textArea = ta;
        }
        
        public void addCellEditorListener(CellEditorListener cellEditorListener) {
            list.add(CellEditorListener.class, cellEditorListener);
        }
        
        public java.awt.Component getTableCellEditorComponent(javax.swing.JTable jTable, java.lang.Object obj, boolean param, int param3, int param4) {
            if(obj instanceof JTextArea){
                textArea.setText(((JTextArea)obj).getText());
                textArea.selectAll();
                textArea.setCaretPosition(0);
                return textArea;
            }
            else
                return (Component)obj;
        }
        
        public void cancelCellEditing() {
            textArea = null;
        }
        
        public boolean isCellEditable(java.util.EventObject eventObject) {
            return true;
        }
        
        public void removeCellEditorListener(javax.swing.event.CellEditorListener cellEditorListener) {
        }
        
        public java.lang.Object getCellEditorValue() {
            return textArea;
        }
        
        public boolean stopCellEditing() {
            if(textArea != null)
                (repository.getCluster(model.getClusterSerialNumber(table.getSelectedRow()))).setClusterDescription(textArea.getText());
            return true;
        }
        
        public boolean shouldSelectCell(java.util.EventObject eventObject) {
            return true;
        }
        
    }
    
    
    
    public class MenuListener implements ActionListener{
        
        public void actionPerformed(ActionEvent actionEvent) {
            String command = actionEvent.getActionCommand();
            String key;
            if(command.equals("hide-command")) {
                if( ((JCheckBoxMenuItem)actionEvent.getSource()).isSelected())
                    model.hide(((JCheckBoxMenuItem)actionEvent.getSource()).getText());
                else
                    model.addColumn(((JCheckBoxMenuItem)actionEvent.getSource()).getText());
            } else if(command.equals("show-all-command")) {
                showAllColumns();
            } else if(command.equals("sort-command")) {
                model.sortBy(((JRadioButtonMenuItem)actionEvent.getSource()).getText());
            } else if(command.equals("modify-command")) {
                modifyClusterAttributes();
            } else if(command.equals("modify-membership-command")) {
                
            } else if(command.equals("go-to-origin-command")) {
                openClusterNode();
            } else if(command.equals("launch-new-command")) {
                launchNewMevSession();
            } else if(command.equals("cluster-operations-command")) {
                performClusterOperation(((JMenuItem)(actionEvent.getSource())).getText());
            } else if(command.equals("delete-command")) {
                deleteSelectedRows();
            } else if(command.equals("delete-all-command")) {
                deleteAllRows();
            } else if(command.equals("save-cluster-command")){
                saveCluster();
            } else if(command.equals("import-list-command")){
                Cluster newCluster = repository.createClusterFromList();
                if(newCluster != null)
                    addCluster(newCluster);
            } else if(command.equals("submit-list-command")) {
                submitCluster();
            }
        }
    }
    
    /**
     * Invoked by the framework when this viewer was deselected.
     */
    public void onDeselected() {
    }
    
    /**
     * Invoked by the framework when data is changed,
     * if this viewer is selected.
     * @see IData
     */
    public void onDataChanged(IData data) {
        repaint();
    }
    
    /**
     * Invoked when the framework is going to be closed.
     */
    public void onClosed() {
    }
    
    /**
     * Returns a component to be inserted into scroll pane view port.
     */
    public JComponent getContentComponent() {
        return table;
    }
    
    /**
     * Invoked by the framework to save or to print viewer image.
     */
    public BufferedImage getImage() {
        return null;
    }
    
    /**
     * Invoked by the framework when this viewer is selected.
     */
    public void onSelected(IFramework framework) {
        this.table.getSelectionModel().setSelectionInterval(0,0);
        repaint();
    }
    
    /**
     * Invoked by the framework when display menu is changed,
     * if this viewer is selected.
     * @see IDisplayMenu
     */
    public void onMenuChanged(IDisplayMenu menu) {
    }
    
    /**
     * Returns a component to be inserted into scroll pane header.
     */
    public JComponent getHeaderComponent() {
        return table.getTableHeader();
    }
    
    /**
     * Updates the cluster table to reflect a change in the repository
     */
    public void onRepositoryChanged(ClusterRepository cr){
        this.repository = cr;
        initializeTable();
        imposeHideMenu();
        this.validate();
        model.sortBy("Serial #");
        model.fireTableDataChanged();
        model.fireTableChanged(new TableModelEvent(model));
    }
    
    private void imposeHideMenu(){
        Component component;
        JMenu hideMenu = (JMenu)(menu.getComponent(8));
        
        JMenuItem item;
        for(int i = 0; i < hideMenu.getMenuComponentCount(); i++){
            component = (Component)(hideMenu.getMenuComponent(i));
            
            if(component instanceof JMenuItem){
                item = (JMenuItem)component;
                if(item.isSelected()){
                    model.hide(item.getText());
                }
            }
        }
    }
    
    private void resetSortMenu(){
        Component component;
        JMenu sortMenu = (JMenu)(menu.getComponent(6));
        
        JMenuItem item;
        for(int i = 0; i < sortMenu.getMenuComponentCount(); i++){
            component = (Component)(sortMenu.getMenuComponent(i));
            
            if(component instanceof JMenuItem){
                item = (JMenuItem)component;
                if(i == 0){
                    model.sortBy(item.getText());
                }
                else{
                    item.setSelected(false);
                }
            }
        }
    }
    
    private void enableAllMenuItems(){
        int n = this.menu.getComponentCount();
        int m;
        for(int i = 0 ; i < n; i++){
            if(menu.getComponent(i) instanceof JMenuItem){
                menu.getComponent(i).setEnabled(true);
                m = 0;
                m = ((JMenuItem)(menu.getComponent(i))).getComponentCount();
                if( (((JMenuItem)menu.getComponent(i)).getText()).equals("Open/Launch") )
                    ((JMenu)menu.getComponent(i)).getMenuComponent(0).setEnabled(true);
                for(int j = 0; j < m; j++){
                    ((JMenuItem)((JMenuItem)menu.getComponent(i)).getComponent(m)).setEnabled(true);
                }
            }
        }
    }
    
    private Cluster [] getSelectedClusters(){
        int [] rows = table.getSelectedRows();
        Cluster [] clusters = new Cluster[rows.length];
        Cluster cluster;
        for(int i = 0; i < clusters.length; i++){
            clusters[i] = repository.getCluster(model.getClusterSerialNumber(rows[i]));
        }
        return clusters;
    }
    
    private void modifyColor(int row, int col){
        Color color = (Color)(table.getValueAt(row, col));       
        color = JColorChooser.showDialog(ClusterTable.this, "Reassign Color", color);
        if(color != null){
            table.setValueAt(color, row, col);
            repository.updateClusterColor(model.getClusterSerialNumber(row), color);
        }
    }
    
    private void modifyShowColor(int row, int col) {
        Cluster [] clusters = this.getSelectedClusters();
        
        if(clusters.length == 0)
            return;
        
        Boolean bool = (Boolean)(this.model.getValueAt(row, col));

        clusters[0].enableShowColor(bool.booleanValue());
        model.setValueAt( clusters[0].getClusterColor(),row, col-1);
      //  initializeTable();        
        model.fireTableDataChanged();
        model.fireTableChanged(new TableModelEvent(model));
        this.table.repaint();
        repaint();
    }
    
    private void launchNewMevSession(){
        Cluster [] clusters = getSelectedClusters();
        ClusterWorker worker = new ClusterWorker(repository);
        int [] indices = worker.getUniqueIndices(clusters);
        String [] clusterLabels = worker.getClusterLabels(clusters);
        if(clusterLabels.length == 1)
            if(this.geneClusterTable)
                framework.launchNewMAV(indices, worker.getMinExperiment(clusters), ("Cluster: "+clusterLabels[0]), Cluster.GENE_CLUSTER);
            else
                framework.launchNewMAV(indices, worker.getMinExperiment(clusters), ("Cluster: "+clusterLabels[0]), Cluster.EXPERIMENT_CLUSTER);
        else if(clusterLabels.length > 1){
            String labelString = "";
            labelString = "Clusters: ";
            for(int i = 0; i < clusterLabels.length-1; i++){
                labelString += clusterLabels[i] + " : ";
            }
            labelString += clusterLabels[clusterLabels.length-1];
            if(this.geneClusterTable)
                framework.launchNewMAV(indices, worker.getMinExperiment(clusters), ("Cluster: "+clusterLabels[0]), Cluster.GENE_CLUSTER);
            else
                framework.launchNewMAV(indices, worker.getMinExperiment(clusters), ("Cluster: "+clusterLabels[0]), Cluster.EXPERIMENT_CLUSTER);
        }
    }
    
    private void openClusterNode(){
        int row = table.getSelectedRow();
        Cluster cluster = repository.getCluster(model.getClusterSerialNumber(row));
        if( !(cluster.getSource()).equals("Algorithm") ){
            return;
        }
        
        DefaultMutableTreeNode node = cluster.getNode();
        
        if(node == null){  // no node, probably cluster loaded from saved object, set node if found
   /*         node = this.framework.findNode(cluster.getAlgorithmName(), cluster.getClusterID());
            if( node != null){
                cluster.setNode(node);
                framework.setTreeNode(node);
            }
    **/
            Object userObject = cluster.getUserObject();
            if(userObject != null) {
                node = framework.getNode(userObject);
                if( node != null) {
                    cluster.setNode(node);
                    framework.setTreeNode(node);
                }
            }
        } else {
            framework.setTreeNode(node);
        }
    }
    
    private void modifyClusterAttributes(){
        int row = table.getSelectedRow();
        if(model.isLegalRow(row)){
            Cluster cluster = repository.getCluster(model.getClusterSerialNumber(row));
            ClusterAttributesDialog dialog = new ClusterAttributesDialog("Modify Cluster Attributes", cluster.getAlgorithmName(), cluster.getClusterID(),
            cluster.getClusterLabel(), cluster.getClusterDescription(), cluster.getClusterColor());
            if(dialog.showModal() != JOptionPane.OK_OPTION){
                return;
            }
            
            Color clusterColor = dialog.getColor();
            String clusterLabel = dialog.getLabel();
            String clusterDescription = dialog.getDescription();
            
            cluster.setClusterColor(clusterColor);
            cluster.setClusterLabel(clusterLabel);
            cluster.setClusterDescription(clusterDescription);
            
            model.setClusterColor(row, clusterColor);
            model.setClusterLabel(row, clusterLabel);
            model.setClusterDescription(row, clusterDescription);
            model.fireTableDataChanged();
        }
    }
    
    private void showAllColumns(){
        Component component;
        JMenu hideMenu = (JMenu)(menu.getComponent(8));
        JMenuItem item;
        for(int i = 0; i < hideMenu.getMenuComponentCount(); i++){
            component = (Component)(hideMenu.getMenuComponent(i));
            if(component instanceof JMenuItem){
                item = (JMenuItem)component;
                if(item.isSelected()){
                    model.addColumn(item.getText());
                    item.setSelected(false);
                }
            }
        }
    }
    
    private void performClusterOperation(String clusterOp){
        Cluster [] clusters = getSelectedClusters();
        if(clusters.length < 2)
            return;
        ClusterWorker worker = new ClusterWorker(repository);
        Cluster result = null;
        if(clusterOp.equals("Intersection"))
            result = worker.intersection(clusters);
        else if(clusterOp.equals("Union"))
            result = worker.union(clusters);
        else if(clusterOp.equals("XOR"))
            result = worker.xor(clusters);
        if(result != null){
            repository.addCluster(repository.getClusterOperationsList(), result);
            addCluster(result);
        }
        if(result != null)
            this.framework.addHistory("Cluster Operation: "+result.getAlgorithmName());
    }
    
    private void deleteSelectedRows(){
        int [] rows = table.getSelectedRows();
        for(int i = 0; i < rows.length; i++){
            if(model.isLegalRow(rows[i]-i)){
                repository.removeCluster(model.getSerialNumber(rows[i]-i));
                model.removeRow(rows[i]-i);
            }
        }
        if(rows.length > 0)
            model.fireTableDataChanged();
        //try this
        this.onRepositoryChanged(this.repository);
    }
    
    private void deleteAllRows(){
        int result = JOptionPane.showConfirmDialog(ClusterTable.this, "Are you sure that you want to delete all clusters in the repository?",
        "Delete All Clusters", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if( result == JOptionPane.YES_OPTION){
            model.removeAllRows();
            repository.clearClusterLists();
        }
    }
    
    /**
     *  Removes all clusters from view and clears the repository
     */
    public void deleteAllClusters(){
        model.removeAllRows();
        repository.clearClusterLists();
    }
    
    private void saveCluster(){
        Cluster [] clusters = getSelectedClusters();
        if(clusters.length != 1){
            JOptionPane.showMessageDialog(framework.getFrame(), "One row must be selected to indicate the cluster to save.", "Save Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        repository.saveCluster(clusters[0].getSerialNumber());
    }
    
    private void submitCluster() {
        Cluster [] clusters = getSelectedClusters();
        if(clusters != null && clusters.length > 0)
            this.repository.submitCluster(clusters[0]);
    }
    
    /** Returns a component to be inserted into the scroll pane row header
     */
    public JComponent getRowHeaderComponent() {
        return null;
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
    
    /** Returns int value indicating viewer type
     * Cluster.GENE_CLUSTER, Cluster.EXPERIMENT_CLUSTER, or -1 for both or unspecified
     */
    public int getViewerType() {
        return -1;
    }
    
}
