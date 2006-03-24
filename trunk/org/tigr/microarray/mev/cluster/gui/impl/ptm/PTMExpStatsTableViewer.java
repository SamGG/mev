
/*
Copyright @ 1999-2004, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * PTMExpStatsTableViewer.java
 *
 * Created on December 2, 2003, 4:07 PM
 */

package org.tigr.microarray.mev.cluster.gui.impl.ptm;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Arrays;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.impl.GUIFactory;
import org.tigr.microarray.mev.cluster.gui.impl.ViewerAdapter;
import org.tigr.util.QSort;


/**
 *
 * @author  nbhagaba
 */
public class PTMExpStatsTableViewer extends ViewerAdapter implements java.io.Serializable {
    
    private JComponent header;
    private JComponent content;
    private Experiment experiment;
    private int[][] expClusters;
    private boolean sig;
    private int[] cols;
 
    private JTable pAndRValuesTable;
    private PAndRValuesTableModel pAndRModel;   
    private String[] auxTitles;
    private Object[][] auxData;
    
    private IData data;
    private JPopupMenu popup;    
    private Object[][] origData;
    private boolean sortedAscending[];   
    
    /** Creates a new instance of PTMExpStatsTableViewer */
    public PTMExpStatsTableViewer(Experiment experiment, int[][] expClusters, IData data,  String[] auxTitles, Object[][] auxData, boolean sig) {
        this.experiment = experiment;
        this.expClusters = expClusters;
        this.data = data;  
        this.auxTitles = auxTitles;
        this.auxData = auxData;
        this.sig = sig;
        if (sig) {
            cols = expClusters[0];
        } else {
            cols = expClusters[1];
        }  
        pAndRModel = new PAndRValuesTableModel();
        pAndRValuesTable = new JTable(pAndRModel);
        origData = new Object[pAndRModel.getRowCount()][pAndRModel.getColumnCount()];
        for (int i = 0; i < origData.length; i++) {
            for (int j = 0; j < origData[i].length; j++) {
                origData[i][j] = pAndRModel.getValueAt(i, j);
            }
        }
        this.sortedAscending = new boolean[pAndRModel.getColumnCount()];
        for (int i = 0; i < sortedAscending.length; i++) {
            sortedAscending[i] = false;
        }   
        
        TableColumn column = null;
        for (int i = 0; i < pAndRModel.getColumnCount(); i++) {
            column = pAndRValuesTable.getColumnModel().getColumn(i);
            column.setMinWidth(30);
        }
        addMouseListenerToHeaderInTable(pAndRValuesTable);
        header  = pAndRValuesTable.getTableHeader();
        //header.setBackground(Color.white);
        //content = createContent();
        setMaxWidth(getContentComponent(), getHeaderComponent());          
    }
    public PTMExpStatsTableViewer(JComponent content, JComponent header){
    	this.content = content;
    	this.header = header;
        setMaxWidth(getContentComponent(), getHeaderComponent());         
    }      
    
    /**
     * Returns component to be inserted into the framework scroll pane.
     */
    public JComponent getContentComponent() {
        JPanel panel = new JPanel();
        panel.setBackground(Color.white);
        GridBagConstraints constraints = new GridBagConstraints();
        GridBagLayout gridbag = new GridBagLayout();
        panel.setLayout(gridbag);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.NORTH;
        buildConstraints(constraints, 0, 0, 1, 1, 100, 100);
        gridbag.setConstraints(pAndRValuesTable, constraints);
        panel.add(pAndRValuesTable);
        
        final JFileChooser fc = new JFileChooser();
        fc.setCurrentDirectory(new File("Data"));
        fc.setDialogTitle("Save p and R values");
        
        popup = new JPopupMenu();
        JMenuItem menuItem = new JMenuItem("Save p and R values", GUIFactory.getIcon("save16.gif"));
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                int returnVal = fc.showSaveDialog(PTMExpStatsTableViewer.this.getHeaderComponent());
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    try {
                        PrintWriter out = new PrintWriter(new FileOutputStream(file));
                        out.print("Sample\t");
                        for (int i = 0; i < auxTitles.length; i++) {
                            out.print(auxTitles[i]);
                            if (i < auxTitles.length - 1) {
                                
                                out.print("\t");
                            }
                        }
                        out.println();                        

                        for (int i = 0; i < cols.length; i++) {                           
                            out.print(data.getFullSampleName(experiment.getSampleIndex(cols[i])));                        

                            for (int j = 0; j < auxData[cols[i]].length; j++) {
                                out.print("\t" + ((Float)(auxData[cols[i]][j])).floatValue());
                            }
                            out.print("\n");                            
                        }
                        out.println();
                        out.flush();
                        out.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    //this is where a real application would save the file.
                    //log.append("Saving: " + file.getName() + "." + newline);
                } else {
                    //log.append("Save command cancelled by user." + newline);
                }
            }
        });
        
        popup.add(menuItem);
        
        pAndRValuesTable.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                maybeShowPopup(e);
            }
            
            public void mouseReleased(MouseEvent e) {
                maybeShowPopup(e);
            }
            
            private void maybeShowPopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    popup.show(e.getComponent(),
                    e.getX(), e.getY());
                }
            }
            
        });
        
        return panel;
        //return tValuesTable;
        //return content;
    }  
    
    /**
     * Returns the viewer header.
     */
    public JComponent getHeaderComponent() {
        JPanel panel = new JPanel();
        panel.setBackground(Color.white);
        GridBagConstraints constraints = new GridBagConstraints();
        GridBagLayout gridbag = new GridBagLayout();
        panel.setLayout(gridbag);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        buildConstraints(constraints, 0, 0, 1, 1, 100, 100);
        gridbag.setConstraints(header, constraints);
        panel.add(header);
        return panel;
        //return header;
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
    
    class PAndRValuesTableModel extends AbstractTableModel {
        String[] columnNames;
        Object[][] tableData;
        
        public PAndRValuesTableModel() {
            //else if (design == TtestInitDialog.ONE_CLASS) {
            columnNames = new String[1 + auxTitles.length];
            //int counter;
            columnNames[0] = "Samples";
            for (int i = 1; i < columnNames.length; i++) {
                columnNames[i] = auxTitles[i - 1];
            }

            tableData = new Object[cols.length][columnNames.length];
            
            //int j;
            for(int i = 0; i < tableData.length; i++) {
                //for (j = 0; j < fieldNames.length; j++) {
                    
                    tableData[i][0] = data.getFullSampleName(experiment.getSampleIndex(cols[i]));
                //}
                
                for (int k = 1; k < tableData[i].length; k++) {
                    float f = ((Float)(auxData[cols[i]][k - 1])).floatValue();
                    if (Float.isNaN(f)) {
                        tableData[i][k] = "N/A";
                    } else {
                        tableData[i][k] = new Float(f);
                    }
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
        
        public Object getValueAt(int row, int col) {
            return tableData[row][col];
        }
        
        public void setValueAt(Object value, int row, int col) {
            tableData[row][col] = value;
            fireTableCellUpdated(row, col);
        }
        
        /*
        public Class getColumnClass(int c) {
            if (c < fieldNames.length) {
                return (new String()).getClass();
            } else {
                return (new Float(1.0f)).getClass();
            }
            //return getValueAt(0, c).getClass();
        }
         */        
        
    }  
    
    /**
     * Synchronize content and header sizes.
     */
    private void setMaxWidth(JComponent content, JComponent header) {
        int c_width = content.getPreferredSize().width;
        int h_width = header.getPreferredSize().width;
        if (c_width > h_width) {
            header.setPreferredSize(new Dimension(c_width, header.getPreferredSize().height));
        } else {
            content.setPreferredSize(new Dimension(h_width, content.getPreferredSize().height));
        }
    }   
    
    public void addMouseListenerToHeaderInTable(JTable table) {
        //final TableSorter sorter = this;
        final JTable tableView = table;
        tableView.setColumnSelectionAllowed(false);
        MouseAdapter listMouseListener = new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                TableColumnModel columnModel = tableView.getColumnModel();
                int viewColumn = columnModel.getColumnIndexAtX(e.getX());
                int column = tableView.convertColumnIndexToModel(viewColumn);
                if (e.getClickCount() == 1 && column != -1) {
                    //System.out.println("Sorting ...");
                    int shiftPressed = e.getModifiers()&InputEvent.SHIFT_MASK;
                    int controlPressed = e.getModifiers()&InputEvent.CTRL_MASK;
                    //boolean ascending = (shiftPressed == 0);
                    boolean originalOrder = (controlPressed != 0);
                    //sortedAscending[column] = !(sortedAscending[column]);
                    //sortByColumn(column, ascending, originalOrder);
                    sortByColumn(column, !(sortedAscending[column]), originalOrder);
                    sortedAscending[column] = !(sortedAscending[column]);
                    //System.out.println("sortedAscending[" + column + "] = " + sortedAscending[column]);
                    if (originalOrder) {
                        for (int i = 0; i < pAndRModel.getColumnCount(); i++)
                        sortedAscending[i] = false;
                    } 
                    //System.out.println("sortedAscending[" + column + "] = " + sortedAscending[column]);                    
                }
            }
        };
        JTableHeader th = tableView.getTableHeader();
        th.addMouseListener(listMouseListener);
    }    
    
    public void sortByColumn(int column, boolean ascending, boolean originalOrder) {
        if (originalOrder) {
            for (int i = 0; i < pAndRModel.getRowCount(); i++) {
                for (int j = 0; j < pAndRModel.getColumnCount(); j++) {
                    pAndRModel.setValueAt(origData[i][j], i, j);
                }
            }
            //sortedAscending = false;
            return;
        } /*else {
            sortedAscending = !(sortedAscending);
        }*/
        //int[] sortedIndices;
        Object[][] sortedData = new Object[pAndRValuesTable.getRowCount()][pAndRValuesTable.getColumnCount()];
        float[] origArray = new float[cols.length];
        SortableField[] sortFields = new SortableField[cols.length];
        if (column == 0) {
            //SortableField[] sortFields = new SortableField[rows.length];
            for (int i = 0; i < sortFields.length; i++) {
                sortFields[i] = new SortableField(i, column);
                //Arrays.sort(sortFields);
            }
            Arrays.sort(sortFields);
            //return;
        } else {
            for (int i = 0; i < origArray.length; i++) {
                origArray[i] = ((Float)(auxData[cols[i]][column - 1])).floatValue();
            }
        }

        //if ( ((ascending) && (!meansASortedAsc)) || ((!ascending) && (!meansASortedDesc)) ) {
        int[] sortedIndices = new int[cols.length];
        if (column > 0) {
            QSort sortArray = new QSort(origArray);
            sortedIndices = sortArray.getOrigIndx();
        } else if (column == 0) {
            for (int i = 0; i < sortedIndices.length; i++) {
                sortedIndices[i] = sortFields[i].getIndex();
            }
        }
        if (!ascending) {
            sortedIndices = reverse(sortedIndices);
        }
        //sortedIndices = sortNaNs(sortedIndices); //QSort does not appear to handle NaN's well, they show up in the middle of the sorted array
        for (int i = 0; i < sortedData.length; i++) {
            for (int j = 0; j < sortedData[i].length; j++) {
                //sortedData[i][j] = tModel.getValueAt(sortedMeansAIndices[i], j);
                sortedData[i][j] = origData[sortedIndices[i]][j];
            }
        }
        
        for (int i = 0; i < sortedData.length; i++) {
            for (int j = 0; j < sortedData[i].length; j++) {
                pAndRModel.setValueAt(sortedData[i][j], i, j);
            }
        }

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
    
}






















