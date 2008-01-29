/*
Copyright @ 1999-2004, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: FStatsTableViewer.java,v $
 * $Revision: 1.7 $
 * $Date: 2006-03-24 15:51:02 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.owa;

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
import org.tigr.microarray.mev.cluster.gui.impl.GUIFactory;
import org.tigr.microarray.mev.cluster.gui.impl.ViewerAdapter;
import org.tigr.util.QSort;

/**
 *
 * @author  nbhagaba
 * @version 
 */
public class FStatsTableViewer extends ViewerAdapter implements java.io.Serializable {
    public static final long serialVersionUID = 202010010001L;
    
    private JComponent header;
    private JComponent content;    
    private Experiment experiment;
    private int[][] clusters;
    private boolean sig;
    private int[] rows;
    private String[] fieldNames;    

    private FValuesTableModel fModel;
    private JTable fValuesTable;

    private Vector pValues, fValues, dfNumValues, dfDenomValues, ssGroups, ssError;    
    private float[][] geneGroupMeans, geneGroupSDs;
    private IData data; 
    private JPopupMenu popup;
    private Object[][] origData;    
    private int univCnt, univCnt2, univCnt3;
    private boolean sortedAscending[];    
    
    /** Creates new FStatsTableViewer */
    public FStatsTableViewer(Experiment experiment, int[][] clusters, IData data, float[][] geneGroupMeans, float[][] geneGroupSDs, Vector pValues, Vector fValues, Vector ssGroups, Vector ssError, Vector dfNumValues, Vector dfDenomValues, boolean sig) {
        this.experiment = experiment;
        this.clusters = clusters;
        this.data = data;
        fieldNames = data.getFieldNames();
        this.geneGroupMeans = geneGroupMeans;
        this.geneGroupSDs = geneGroupSDs;
        this.pValues = pValues;
        this.fValues = fValues;
        this.ssGroups = ssGroups;
        this.ssError = ssError;
        this.dfNumValues = dfNumValues;
        this.dfDenomValues = dfDenomValues;
        this.sig = sig;
        if (sig) {
            rows = clusters[0];
        } else {
            rows =clusters[1];
        }   
        
        fModel = new FValuesTableModel(); 
        fValuesTable = new JTable(fModel); 
        origData = new Object[fModel.getRowCount()][fModel.getColumnCount()];
        for (int i = 0; i < origData.length; i++) {
            for (int j = 0; j < origData[i].length; j++) {
                origData[i][j] = fModel.getValueAt(i, j);
            }
        } 
        this.sortedAscending = new boolean[fModel.getColumnCount()];
        for (int i = 0; i < sortedAscending.length; i++) {
            sortedAscending[i] = false;
        }        
        TableColumn column = null;
        for (int i = 0; i < fModel.getColumnCount(); i++) {
            column = fValuesTable.getColumnModel().getColumn(i);
            column.setMinWidth(30);
        }  
        addMouseListenerToHeaderInTable(fValuesTable);        
        header  = fValuesTable.getTableHeader();  
        setMaxWidth(getContentComponent(), getHeaderComponent());         
    }
    
    public FStatsTableViewer(JComponent content, JComponent header){
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
        gridbag.setConstraints(fValuesTable, constraints);
        panel.add(fValuesTable);    

        final JFileChooser fc = new JFileChooser();
        fc.setCurrentDirectory(new File("Data"));   
        fc.setDialogTitle("Save F-Ratio information");
        
        popup = new JPopupMenu();
        JMenuItem menuItem = new JMenuItem("Save F-Ratio information", GUIFactory.getIcon("save16.gif"));
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                    int returnVal = fc.showSaveDialog(FStatsTableViewer.this.getHeaderComponent());
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        File file = fc.getSelectedFile();
                        try {
                            PrintWriter out = new PrintWriter(new FileOutputStream(file));
                            for (int i = 0; i < fieldNames.length; i++) {
                                out.print(fieldNames[i]);
                                //if (i < fieldNames.length - 1) {
                                    
                                    out.print("\t");
                                //}
                            }
                            for (int i = 0; i < geneGroupMeans[0].length; i++) {
                                out.print("Group" + (i+1) + " mean\t");
                                out.print("Group" + (i + 1) + " std.dev.\t");
                            }
                            out.print("F-ratio\tSS(Groups)\tSS(Error)\tdf(Groups)\tdf(Error)\tp-value\n");                      
                            for (int i = 0; i < rows.length; i++) {
                                for (int k = 0; k < fieldNames.length; k++) {
                                    out.print(data.getElementAttribute(experiment.getGeneIndexMappedToData(rows[i]), k));
                                    //if (k < fieldNames.length - 1) {
                                        out.print("\t");
                                    //}
                                }
                                for (int j = 0; j < geneGroupMeans[i].length; j++) {
                                    out.print(geneGroupMeans[rows[i]][j] + "\t");
                                    out.print(geneGroupSDs[rows[i]][j] + "\t");
                                }
                                out.print(((Float)(fValues.get(rows[i]))).floatValue());
                                out.print("\t" + ((Float)(ssGroups.get(rows[i]))).floatValue());
                                out.print("\t" + ((Float)(ssError.get(rows[i]))).floatValue());
                                out.print("\t" + ((Float)(dfNumValues.get(rows[i]))).intValue());
                                out.print("\t" + ((Float)(dfDenomValues.get(rows[i]))).intValue());
                                out.print("\t" + ((Float)(pValues.get(rows[i]))).floatValue());
                                out.print("\n");
                            }
                            out.println();
                            out.flush();
                            out.close();
                       } catch (Exception e) {
                            //e.printStackTrace();
                        }
                        //this is where a real application would save the file.
                        //log.append("Saving: " + file.getName() + "." + newline);
                    } else {
                        //log.append("Save command cancelled by user." + newline);
                    }                
            }
        });
        
        popup.add(menuItem);
        
        fValuesTable.addMouseListener(new MouseAdapter() {
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

    
    class FValuesTableModel extends AbstractTableModel {
        String[] columnNames;
        Object[][] tableData;
        
        public FValuesTableModel() {
            columnNames = new String[fieldNames.length + 2*geneGroupMeans[0].length + 6];
            int counter;
            for (counter = 0; counter < fieldNames.length; counter++) {
                columnNames[counter] = fieldNames[counter];
            }
            univCnt = counter;
            int counter2;
            int groupNum = 1;
            for (counter2 = 0; counter2 < 2*geneGroupMeans[0].length; counter2++) {
                columnNames[counter + counter2] = "Group" + groupNum + " mean";
                counter2++;
                columnNames[counter + counter2] = "Group" + groupNum + " std.dev.";
                groupNum++;
            }
            univCnt2 = counter2;
            int counter3 = counter + counter2;
            univCnt3 = counter3;
            columnNames[counter3] = "F-Ratio";
            columnNames[counter3 + 1] = "SS (Groups)";
            columnNames[counter3 + 2] = "SS (Error)";
            columnNames[counter3 + 3] = "df (Groups)";
            columnNames[counter3 + 4] = "df (Error)";
            columnNames[counter3 + 5] = "p-value";
                        
            tableData = new Object[rows.length][columnNames.length];
            
            //groupNum = 0;
            //System.out.println("counter = " + counter + ", counter3 = " + counter3);
            for (int i = 0; i < tableData.length; i++) {
                groupNum = 0;
                for (int j = counter; j < counter3;) {
                    //if ((j >= counter) && (j < counter3)) {
                    //System.out.println("i = " + i + ", j = " + j + ", groupNum = " + groupNum);
                    if (Float.isNaN(geneGroupMeans[rows[i]][groupNum])) {
                        tableData[i][j] = "N/A";
                    } else {
                        tableData[i][j] = new Float(geneGroupMeans[rows[i]][groupNum]);
                    }
                    j++;
                    
                    if (Float.isNaN(geneGroupSDs[rows[i]][groupNum])) {
                        tableData[i][j] = "N/A";
                    } else {
                        tableData[i][j] = new Float(geneGroupSDs[rows[i]][groupNum]);
                    }
                    groupNum++;
                    j++;
                    //}
                }
            }
             
            
            for (int i = 0; i < tableData.length; i++) {
                for (int j = 0; j < tableData[i].length; j++) {
                    if (j < counter) {
                        tableData[i][j] = data.getElementAttribute(experiment.getGeneIndexMappedToData(rows[i]), j);
                    } else if ((j >= counter) && (j < counter3)) {
                        //do nothing
                    } else if (j == counter3) {
                        float f = ((Float)(fValues.get(rows[i]))).floatValue();
                        if (Float.isNaN(f)) {
                            tableData[i][j] = "N/A";
                        } else {
                            tableData[i][j] = (Float)(fValues.get(rows[i]));
                        }
                        
                    } else if (j == counter3 + 1) {
                        
                        float f = ((Float)(ssGroups.get(rows[i]))).floatValue();
                        if (Float.isNaN(f)) {
                            tableData[i][j] = "N/A";
                        } else {
                            tableData[i][j] = (Float)(ssGroups.get(rows[i]));
                        }

                    } else if (j == counter3 + 2) {
                        
                        float f = ((Float)(ssError.get(rows[i]))).floatValue();
                        if (Float.isNaN(f)) {
                            tableData[i][j] = "N/A";
                        } else {
                            tableData[i][j] = (Float)(ssError.get(rows[i]));
                        }
                       
                    } else if (j == counter3 + 3) {
                        
                        float f = ((Float)(dfNumValues.get(rows[i]))).floatValue();
                        if (Float.isNaN(f)) {
                            tableData[i][j] = "N/A";
                        } else {
                            tableData[i][j] = (Float)(dfNumValues.get(rows[i]));
                        }

                    } else if (j == counter3 + 4) {
                        float f = ((Float)(dfDenomValues.get(rows[i]))).floatValue();
                        if (Float.isNaN(f)) {
                            tableData[i][j] = "N/A";
                        } else {
                            tableData[i][j] = (Float)(dfDenomValues.get(rows[i]));
                        }
                    } else if (j == counter3 + 5) {
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
                    sortByColumn(column, !(sortedAscending[column]), originalOrder);
                    sortedAscending[column] = !(sortedAscending[column]);  
                    if (originalOrder) {
                        for (int i = 0; i < fModel.getColumnCount(); i++)
                        sortedAscending[i] = false;
                    }                    
                    //sortByColumn(column, ascending, originalOrder); 
                }
            }
        };
        JTableHeader th = tableView.getTableHeader(); 
        th.addMouseListener(listMouseListener); 
    } 
    
    public void sortByColumn(int column, boolean ascending, boolean originalOrder) {
        if (originalOrder) {
            for (int i = 0; i < fModel.getRowCount(); i++) {
                for (int j = 0; j < fModel.getColumnCount(); j++) {
                    fModel.setValueAt(origData[i][j], i, j);
                }
            } 
            return;
        }
        Object[][] sortedData = new Object[fValuesTable.getRowCount()][fValuesTable.getColumnCount()]; 
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
        } else if (column == univCnt3) {
            for (int i = 0; i < origArray.length; i++) {
                origArray[i] = ((Float)(fValues.get(rows[i]))).floatValue();
            }
        } else if (column == univCnt3 + 1) {
            for (int i = 0; i < origArray.length; i++) {
                origArray[i] = ((Float)(ssGroups.get(rows[i]))).floatValue();
            }
        } else if (column == univCnt3 + 2) {
            for (int i = 0; i < origArray.length; i++) {
                origArray[i] = ((Float)(ssError.get(rows[i]))).floatValue();
            }            
        } else if (column == univCnt3 + 3) {
            for (int i = 0; i < origArray.length; i++) {
                origArray[i] = ((Float)(dfNumValues.get(rows[i]))).floatValue();
            }            
        } else if (column == univCnt3 + 4) {
            for (int i = 0; i < origArray.length; i++) {
                origArray[i] = ((Float)(dfDenomValues.get(rows[i]))).floatValue();
            }            
        } else if (column == univCnt3 + 5) {
            for (int i = 0; i < origArray.length; i++) {
                origArray[i] = ((Float)(pValues.get(rows[i]))).floatValue();
            }            
        } else {
            int currentIndex = column - fieldNames.length;
            int newIndex = 0;
            if ((int)(currentIndex%2) == 0) {
                newIndex = (int)(currentIndex/2);
                for (int i = 0; i < origArray.length; i++) {
                    origArray[i] = geneGroupMeans[rows[i]][newIndex];
                }
            } else {
                newIndex = (int)((currentIndex - 1)/2);
                for (int i = 0; i < origArray.length; i++) {
                    origArray[i] = geneGroupSDs[rows[i]][newIndex];
                }
            }
        }
        
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
                fModel.setValueAt(sortedData[i][j], i, j);
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
