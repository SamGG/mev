/*
Copyright @ 1999-2005, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * $RCSfile: TStatsTableViewer.java,v $
 * $Revision: 1.9 $
 * $Date: 2006-05-02 16:57:56 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.ttest;

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
import java.util.Vector;

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
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.impl.GUIFactory;
import org.tigr.microarray.mev.cluster.gui.impl.ViewerAdapter;
import org.tigr.util.QSort;

/**
 *
 * @author  nbhagaba
 * @version
 */
public class TStatsTableViewer extends ViewerAdapter {
    
    private JComponent header;
    private JComponent content;
    private Experiment experiment;
    private int[][] clusters;
    private boolean sig;
    private int[] rows;
    private String[] fieldNames;
    
    private JTable tValuesTable;
    private TValuesTableModel tModel;
    private int tTestDesign;
    private Vector pValues, tValues, dfValues, meansA, meansB, sdA, sdB, oneClassMeans, oneClassSDs;
    private IData data;
    private JPopupMenu popup;
    //boolean meansASortedAsc, meansASortedDesc;
    private Object[][] origData;
    private boolean sortedAscending[];//, sortedDescending;
    
    /** Creates new TStatsTableViewer */
    public TStatsTableViewer(Experiment experiment, int[][] clusters, IData data, int tTestDesign, Vector oneClassMeans, Vector oneClassSDs, Vector meansA, Vector meansB, Vector sdA, Vector sdB, Vector pValues, Vector tValues, Vector dfValues, boolean sig) {
        this.experiment = experiment;
        this.clusters = clusters;
        this.data = data;
        this.fieldNames = data.getFieldNames();
        this.tTestDesign = tTestDesign;
        this.oneClassMeans = oneClassMeans;
        this.oneClassSDs = oneClassSDs;
        this.pValues = pValues;
        this.tValues = tValues;
        this.dfValues = dfValues;
        this.meansA = meansA;
        this.meansB = meansB;
        this.sdA = sdA;
        this.sdB = sdB;
        this.sig = sig;
        //this.meansASortedAsc = false;
        //this.meansASortedDesc =false;
        if (sig) {
            rows = clusters[0];
        } else {
            rows =clusters[1];
        }
        //this.sortedAscending = false;
        //this.sortedDescending = false;
        tModel = new TValuesTableModel(tTestDesign);
        tValuesTable = new JTable(tModel);
        origData = new Object[tModel.getRowCount()][tModel.getColumnCount()];
        for (int i = 0; i < origData.length; i++) {
            for (int j = 0; j < origData[i].length; j++) {
                origData[i][j] = tModel.getValueAt(i, j);
            }
        }
        this.sortedAscending = new boolean[tModel.getColumnCount()];
        for (int i = 0; i < sortedAscending.length; i++) {
            sortedAscending[i] = false;
        }
        //tValuesTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        //tValuesTable.setBackground(Color.white);
        //tValuesTable.setMinimumSize(new Dimension(500, tModel.getRowCount()*tValuesTable.getRowHeight()));
        //tValuesTable.setPreferredScrollableViewportSize(new Dimension(500, 600));
        //tValuesTable.setM
        TableColumn column = null;
        for (int i = 0; i < tModel.getColumnCount(); i++) {
            column = tValuesTable.getColumnModel().getColumn(i);
            column.setMinWidth(30);
        }
        addMouseListenerToHeaderInTable(tValuesTable);
        header  = tValuesTable.getTableHeader();
        //header.setBackground(Color.white);
        //content = createContent();
        setMaxWidth(getContentComponent(), getHeaderComponent());
    }
    
    public void onSelected(IFramework framework){
        this.data = framework.getData();
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
        gridbag.setConstraints(tValuesTable, constraints);
        panel.add(tValuesTable);
        
        final JFileChooser fc = new JFileChooser();
        fc.setCurrentDirectory(new File("Data"));
        fc.setDialogTitle("Save gene t-statistics");
        
        popup = new JPopupMenu();
        JMenuItem menuItem = new JMenuItem("Save Gene t-statistics", GUIFactory.getIcon("save16.gif"));
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                int returnVal = fc.showSaveDialog(TStatsTableViewer.this.getHeaderComponent());
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    try {
                        PrintWriter out = new PrintWriter(new FileOutputStream(file));
                        for (int i = 0; i < fieldNames.length; i++) {
                            out.print(fieldNames[i]);
                            if (i < fieldNames.length - 1) {
                                
                                out.print("\t");
                            }
                        }
                        if (tTestDesign == TtestInitDialog.BETWEEN_SUBJECTS) {
                            out.print("\tGroupA mean\tGroupA std.dev.\tGroupB mean\tGroupB std.dev.\tt-ratio\tdf\tp-value\n");
                        } else if (tTestDesign == TtestInitDialog.ONE_CLASS) {
                            out.print("\tGene mean\tGene std.dev.\tt-ratio\tdf\tp-value\n");
                        }
                        for (int i = 0; i < rows.length; i++) {
                            for (int k = 0; k < fieldNames.length; k++) {
                                out.print(data.getElementAttribute(experiment.getGeneIndexMappedToData(rows[i]), k));
                                if (k < fieldNames.length - 1) {
                                    out.print("\t");
                                }
                            }
                            if (tTestDesign == TtestInitDialog.BETWEEN_SUBJECTS) {
                                out.print("\t" + ((Float)(meansA.get(rows[i]))).floatValue());
                                out.print("\t" + ((Float)(sdA.get(rows[i]))).floatValue());
                                out.print("\t" + ((Float)(meansB.get(rows[i]))).floatValue());
                                out.print("\t" + ((Float)(sdB.get(rows[i]))).floatValue());
                            } else if (tTestDesign == TtestInitDialog.ONE_CLASS) {
                                out.print("\t" + ((Float)(oneClassMeans.get(rows[i]))).floatValue());
                                out.print("\t" + ((Float)(oneClassSDs.get(rows[i]))).floatValue());                                
                            }
                            out.print("\t" + ((Float)(tValues.get(rows[i]))).floatValue());
                            out.print("\t" + ((Float)(dfValues.get(rows[i]))).intValue());
                            out.print("\t" + ((Float)(pValues.get(rows[i]))).floatValue());
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
        
        tValuesTable.addMouseListener(new MouseAdapter() {
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
    
    
    
    class TValuesTableModel extends AbstractTableModel implements java.io.Serializable {
        String[] columnNames;
        Object[][] tableData;
        
        public TValuesTableModel(int design) {
            if (design == TtestInitDialog.BETWEEN_SUBJECTS) {
                columnNames = new String[fieldNames.length + 7];
                int counter;
                for (counter = 0; counter < fieldNames.length; counter++) {
                    columnNames[counter] = fieldNames[counter];
                }
                columnNames[counter] = "GroupA mean";
                columnNames[counter + 1] = "GroupA std.dev";
                columnNames[counter + 2] = "GroupB mean";
                columnNames[counter + 3] = "GroupB std.dev.";
                columnNames[counter + 4] = "t-ratio";
                columnNames[counter + 5] = "df";
                columnNames[counter + 6] = "p-value";
                
                tableData = new Object[rows.length][columnNames.length];
                
                for (int i = 0; i < tableData.length; i++) {
                    for (int j = 0; j < tableData[i].length; j++) {
                        if (j < fieldNames.length) {
                            tableData[i][j] = data.getElementAttribute(experiment.getGeneIndexMappedToData(rows[i]), j);
                        } else if (j == fieldNames.length) {
                            float f = ((Float)(meansA.get(rows[i]))).floatValue();
                            if (Float.isNaN(f)) {
                                tableData[i][j] = "N/A";
                            } else {
                                tableData[i][j] = (Float)(meansA.get(rows[i]));
                            }
                            
                        } else if (j == fieldNames.length + 1) {
                            
                            float f = ((Float)(sdA.get(rows[i]))).floatValue();
                            if (Float.isNaN(f)) {
                                tableData[i][j] = "N/A";
                            } else {
                                tableData[i][j] = (Float)(sdA.get(rows[i]));
                            }
                            
                        } else if (j == fieldNames.length + 2) {
                            
                            float f = ((Float)(meansB.get(rows[i]))).floatValue();
                            if (Float.isNaN(f)) {
                                tableData[i][j] = "N/A";
                            } else {
                                tableData[i][j] = (Float)(meansB.get(rows[i]));
                            }
                            
                        } else if (j == fieldNames.length + 3) {
                            
                            float f = ((Float)(sdB.get(rows[i]))).floatValue();
                            if (Float.isNaN(f)) {
                                tableData[i][j] = "N/A";
                            } else {
                                tableData[i][j] = (Float)(sdB.get(rows[i]));
                            }
                            
                        } else if (j == fieldNames.length + 4) {
                            float f = ((Float)(tValues.get(rows[i]))).floatValue();
                            if (Float.isNaN(f)) {
                                tableData[i][j] = "N/A";
                            } else {
                                tableData[i][j] = (Float)(tValues.get(rows[i]));
                            }
                        } else if (j == fieldNames.length + 5) {
                            float f = ((Float)(dfValues.get(rows[i]))).floatValue();
                            if (Float.isNaN(f)) {
                                tableData[i][j] = "N/A";
                            } else {
                                tableData[i][j] = (Float)(dfValues.get(rows[i]));
                            }
                        } else if (j == fieldNames.length + 6) {
                            float f = ((Float)(pValues.get(rows[i]))).floatValue();
                            if (Float.isNaN(f)) {
                                tableData[i][j] = "N/A";
                            } else {
                                tableData[i][j] = (Float)(pValues.get(rows[i]));
                            }
                        }
                    }
                }
                
            } else if (design == TtestInitDialog.ONE_CLASS) {
                columnNames = new String[fieldNames.length + 5];
                int counter;
                for (counter = 0; counter < fieldNames.length; counter++) {
                    columnNames[counter] = fieldNames[counter];
                }
                columnNames[counter] = "Gene mean";
                columnNames[counter + 1] = "Gene std.dev";
                columnNames[counter + 2] = "t-ratio";
                columnNames[counter + 3] = "df";
                columnNames[counter + 4] = "p-value";
                
                tableData = new Object[rows.length][columnNames.length];
                
                for (int i = 0; i < tableData.length; i++) {
                    for (int j = 0; j < tableData[i].length; j++) {
                        if (j < fieldNames.length) {
                            tableData[i][j] = data.getElementAttribute(experiment.getGeneIndexMappedToData(rows[i]), j);
                        } else if (j == fieldNames.length) {
                            float f = ((Float)(oneClassMeans.get(rows[i]))).floatValue();
                            if (Float.isNaN(f)) {
                                tableData[i][j] = "N/A";
                            } else {
                                tableData[i][j] = (Float)(oneClassMeans.get(rows[i]));
                            }
                            
                        } else if (j == fieldNames.length + 1) {
                            
                            float f = ((Float)(oneClassSDs.get(rows[i]))).floatValue();
                            if (Float.isNaN(f)) {
                                tableData[i][j] = "N/A";
                            } else {
                                tableData[i][j] = (Float)(oneClassSDs.get(rows[i]));
                            }
                            
                        } else if (j == fieldNames.length + 2) {
                            float f = ((Float)(tValues.get(rows[i]))).floatValue();
                            if (Float.isNaN(f)) {
                                tableData[i][j] = "N/A";
                            } else {
                                tableData[i][j] = (Float)(tValues.get(rows[i]));
                            }
                        } else if (j == fieldNames.length + 3) {
                            float f = ((Float)(dfValues.get(rows[i]))).floatValue();
                            if (Float.isNaN(f)) {
                                tableData[i][j] = "N/A";
                            } else {
                                tableData[i][j] = (Float)(dfValues.get(rows[i]));
                            }
                        } else if (j == fieldNames.length + 4) {
                            float f = ((Float)(pValues.get(rows[i]))).floatValue();
                            if (Float.isNaN(f)) {
                                tableData[i][j] = "N/A";
                            } else {
                                tableData[i][j] = (Float)(pValues.get(rows[i]));
                            }
                        }
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
                        for (int i = 0; i < tModel.getColumnCount(); i++)
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
            for (int i = 0; i < tModel.getRowCount(); i++) {
                for (int j = 0; j < tModel.getColumnCount(); j++) {
                    tModel.setValueAt(origData[i][j], i, j);
                }
            }
            //sortedAscending = false;
            return;
        } /*else {
            sortedAscending = !(sortedAscending);
        }*/
        //int[] sortedIndices;
        Object[][] sortedData = new Object[tValuesTable.getRowCount()][tValuesTable.getColumnCount()];
        float[] origArray = new float[rows.length];
        SortableField[] sortFields = new SortableField[rows.length];
        if (column < fieldNames.length) {
            //SortableField[] sortFields = new SortableField[rows.length];
            for (int i = 0; i < sortFields.length; i++) {
                sortFields[i] = new SortableField(i, column);
                //Arrays.sort(sortFields);
            }
            Arrays.sort(sortFields);
            //return;
        } else if (tTestDesign == TtestInitDialog.BETWEEN_SUBJECTS) {
            if (column == fieldNames.length) {
                for (int i = 0; i < origArray.length; i++) {
                    origArray[i] = ((Float)(meansA.get(rows[i]))).floatValue();
                }
            } else if (column == fieldNames.length + 1) {
                for (int i = 0; i < origArray.length; i++) {
                    origArray[i] = ((Float)(sdA.get(rows[i]))).floatValue();
                }
            } else if (column == fieldNames.length + 2) {
                for (int i = 0; i < origArray.length; i++) {
                    origArray[i] = ((Float)(meansB.get(rows[i]))).floatValue();
                }
            } else if (column == fieldNames.length + 3) {
                for (int i = 0; i < origArray.length; i++) {
                    origArray[i] = ((Float)(sdB.get(rows[i]))).floatValue();
                }
            } else if (column == fieldNames.length + 4) {
                for (int i = 0; i < origArray.length; i++) {
                    origArray[i] = ((Float)(tValues.get(rows[i]))).floatValue();
                }
            } else if (column == fieldNames.length + 5) {
                for (int i = 0; i < origArray.length; i++) {
                    origArray[i] = ((Float)(dfValues.get(rows[i]))).floatValue();
                }
            } else if (column == fieldNames.length + 6) {
                for (int i = 0; i < origArray.length; i++) {
                    origArray[i] = ((Float)(pValues.get(rows[i]))).floatValue();
                }
            }
        } else if (tTestDesign == TtestInitDialog.ONE_CLASS) {
            if (column == fieldNames.length) {
                for (int i = 0; i < origArray.length; i++) {
                    origArray[i] = ((Float)(oneClassMeans.get(rows[i]))).floatValue();
                }
            } else if (column == fieldNames.length + 1) {
                for (int i = 0; i < origArray.length; i++) {
                    origArray[i] = ((Float)(oneClassSDs.get(rows[i]))).floatValue();
                }
            } else if (column == fieldNames.length + 2) {
                for (int i = 0; i < origArray.length; i++) {
                    origArray[i] = ((Float)(tValues.get(rows[i]))).floatValue();
                }
            } else if (column == fieldNames.length + 3) {
                for (int i = 0; i < origArray.length; i++) {
                    origArray[i] = ((Float)(dfValues.get(rows[i]))).floatValue();
                }
            } else if (column == fieldNames.length + 4) {
                for (int i = 0; i < origArray.length; i++) {
                    origArray[i] = ((Float)(pValues.get(rows[i]))).floatValue();
                }
            }            
        }
        //if ( ((ascending) && (!meansASortedAsc)) || ((!ascending) && (!meansASortedDesc)) ) {
        int[] sortedIndices = new int[rows.length];
        if (column >= fieldNames.length) {
            QSort sortArray = new QSort(origArray);
            sortedIndices = sortArray.getOrigIndx();
        } else {
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
                tModel.setValueAt(sortedData[i][j], i, j);
            }
        }
        //}
        
                /*
            if (ascending) {
                if (column == fieldNames.length) {
                    meansASortedAsc = true;
                    meansASortedDesc = false;
                }
                 
            } else {
                if (column == fieldNames.length) {
                meansASortedAsc = false;
                meansASortedDesc = true;
                }
                 
            }
                 */
        
        //}
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
