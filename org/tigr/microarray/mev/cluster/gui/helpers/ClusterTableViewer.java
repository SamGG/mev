/*
 * ClusterTableViewer.java
 *
 * Created on March 9, 2004, 11:47 AM
 */

package org.tigr.microarray.mev.cluster.gui.helpers;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;

import javax.swing.*;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.JComponent;
import javax.swing.table.*;
import javax.swing.event.TableModelEvent;

import org.tigr.microarray.mev.cluster.gui.IViewer;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IDisplayMenu;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.impl.GUIFactory;

import org.tigr.microarray.mev.cluster.clusterUtil.*;
import org.tigr.util.QSort;

/**
 *
 * @author  nbhagaba
 */
public class ClusterTableViewer implements IViewer {
    
    private static final String NO_GENES_STR = "No Genes in Cluster!";
    private static final Font ERROR_FONT = new Font("monospaced", Font.BOLD, 20);
    protected static final String STORE_CLUSTER_CMD = "store-cluster-cmd";
    protected static final String SET_DEF_COLOR_CMD = "set-def-color-cmd";
    protected static final String SAVE_CLUSTER_CMD = "save-cluster-cmd";
    protected static final String SAVE_ALL_CLUSTERS_CMD = "save-all-clusters-cmd";
    protected static final String LAUNCH_NEW_SESSION_CMD = "launch-new-session-cmd";    
    
    public static final int INTEGER_TYPE = 10;
    public static final int FLOAT_TYPE = 11;
    public static final int DOUBLE_TYPE = 12;
    public static final int STRING_TYPE = 13;
    public static final int BOOLEAN_TYPE = 14;    
    
    private JComponent header;
    private Experiment experiment;
    private IFramework framework;
    private IData data;
    private int clusterIndex;
    private int[][] clusters;
    private int[][] sortedClusters;
    private int[] samplesOrder;
    private String[] auxTitles, fieldNames;
    private Object[][] auxData;
    private Object[][] origData;
    private boolean[][] sortedAscending;  
    private JTable clusterTable;
    private ClusterTableModel clusterModel;  
    //private JPanel clusterTablePanel;
    
    /** Creates a new instance of ClusterTableViewer */
    /*
    public ClusterTableViewer(Experiment experiment, int[][] clusters) {
    }
     */
    
    public ClusterTableViewer(Experiment experiment, int[][] clusters, IData data, String[] auxTitles, Object[][] auxData) {
        if (experiment == null) {
            throw new IllegalArgumentException("experiment == null");
        }
        this.data = data;
        this.experiment = experiment;
        this.clusters = clusters;  
        this.fieldNames = data.getFieldNames();
        this.auxTitles = auxTitles;
        this.auxData = auxData;
        
        this.sortedClusters = new int[clusters.length][];
        
        for (int i = 0; i < sortedClusters.length; i++) {
            sortedClusters[i] = new int[clusters[i].length];
        }
        
        for (int i = 0; i < sortedClusters.length; i++) {
            for (int j = 0; j < sortedClusters[i].length; j++) {
                sortedClusters[i][j] = clusters[i][j];
            }
        }
        
        this.clusterModel = new ClusterTableModel();
        this.clusterTable = new JTable(clusterModel);
        
        TableColumn column = null;
        for (int i = 0; i < clusterModel.getColumnCount(); i++) {
            column = clusterTable.getColumnModel().getColumn(i);
            column.setMinWidth(30);
        } 
        
        this.sortedAscending = new boolean[clusters.length][clusterModel.getColumnCount()];
        for (int i = 0; i < sortedAscending.length; i++) {
            for (int j = 0; j < sortedAscending[i].length; j++) {
                sortedAscending[i][j] = false;
            }
        }
        addMouseListenerToHeaderInTable(clusterTable);
        header  = clusterTable.getTableHeader();        
        setMaxWidth(getContentComponent(), getHeaderComponent());  
              
    }    
    
    public ClusterTableViewer(Experiment experiment, int[][] clusters, IData data) {
        this(experiment, clusters, data, new String[0], new Object[0][0]);
    }
    

    
    /**
     * Returns a component to be inserted into scroll pane view port.
     *
     */
    public JComponent getContentComponent() {
        
        JPanel panel = new JPanel();
        panel.setBackground(Color.gray);
        GridBagConstraints constraints = new GridBagConstraints();
        GridBagLayout gridbag = new GridBagLayout();
        panel.setLayout(gridbag);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.NORTH;
        buildConstraints(constraints, 0, 0, 1, 1, 100, 100);
        gridbag.setConstraints(clusterTable, constraints);
        panel.add(clusterTable);
        
        return panel;
    }
    
    /**
     * Returns the corner component corresponding to the indicated corner,
     *
     * posibly null
     *
     */
    public JComponent getCornerComponent(int cornerIndex) {
        return null;
    }
    
  
  
    
    /**
     * Returns a component to be inserted into scroll pane header.
     *
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
    /**
     * Invoked by the framework to save or to print viewer image.
     *
     */
    public BufferedImage getImage() {
        return null;
    }
    
    /**
     * Returns a component to be inserted into the scroll pane row header
     *
     */
    public JComponent getRowHeaderComponent() {
        return null;
    }
    
    /**
     * Invoked when the framework is going to be closed.
     *
     */
    public void onClosed() {
    }
    
    /**
     * Invoked by the framework when data is changed,
     *
     * if this viewer is selected.
     *
     * @see IData
     *
     */
    public void onDataChanged(IData data) {
        this.data = data;
        //clusterTable.repaint();        
    }
    
    /**
     * Invoked by the framework when this viewer was deselected.
     *
     */
    public void onDeselected() {
        //clusterTable.repaint();
    }
    
    /**
     * Invoked by the framework when display menu is changed,
     *
     * if this viewer is selected.
     *
     * @see IDisplayMenu
     *
     */
    public void onMenuChanged(IDisplayMenu menu) {
    }
    
    /**
     * Invoked by the framework when this viewer is selected.
     *
     */
    public void onSelected(IFramework framework) {
        this.framework = framework;
        this.data = framework.getData();        
        IDisplayMenu menu = framework.getDisplayMenu();
        Integer userObject = (Integer)framework.getUserObject();        
        setClusterIndex(userObject == null ? 0 : userObject.intValue());       
    }
    
    /**
     * Sets cluster index to be displayed.
     */

    public void setClusterIndex(int clusterIndex) {
        this.clusterIndex = clusterIndex;
    }

    /**
     * Returns index of current cluster.
     */

    public int getClusterIndex() {
        return clusterIndex;
    }    

    /**
     * Returns indices of current cluster.
     */
    public int[] getCluster() {
        return clusters[this.clusterIndex];
    }
    
    /**
     * Returns all the clusters.
     */
    public int[][] getClusters() {
        return clusters;
    }
    
    public int[] getSortedCluster() {
        return sortedClusters[this.clusterIndex];
    }
    
    /**

     *	Returns the row index in the experiment's <code>FloatMatrix<\code>

     *  corresponding to the passed index to the clusters array

     */

    private int getExperimentRow(int row){
        return this.clusters[this.clusterIndex][row];
    }  
   
    
    private int getColumn(int column) {
        return samplesOrder[column];
    }
    
    /**
     * Returns wrapped experiment.
     */

    public Experiment getExperiment() {
        return experiment;
    }
    
    /**
     * Returns the data.
     */
    public IData getData() {
        return data;
    }  
    
    /**
     * Converts cluster indicies from the experiment to IData rows which could be different
     */

    private int [] getIDataRowIndices(int [] expIndices){
        int [] dataIndices = new int[expIndices.length];
        for(int i = 0; i < expIndices.length; i++){
            dataIndices[i] = this.getMultipleArrayDataRow(i);
        }
        return dataIndices;
    }    
    
    /**
     *	Returns the row (index) within the main iData which corresponds to
     *  the passed index to the clusters array
     */
    private int getMultipleArrayDataRow(int clusterArrayRow) {
        return experiment.getGeneIndexMappedToData(this.clusters[this.clusterIndex][clusterArrayRow]);
    }    
    
    class ClusterTableModel extends AbstractTableModel {
        String[] columnNames;
        //Object[][] tableData;
        //int[] rows = getCluster();
        
        public ClusterTableModel() {
            columnNames = new String[fieldNames.length + auxTitles.length];  
           int counter;
            for (counter = 0; counter < fieldNames.length; counter++) {
                columnNames[counter] = fieldNames[counter];
            }
            for (int i = counter; i < columnNames.length; i++) {
                columnNames[i] = auxTitles[i - counter];
            }
        }

        public int getColumnCount() {
            return columnNames.length;
        }
        
        public int getRowCount() {
            //return tableData.length;
            //System.out.println("Row count = " + getCluster().length);            
            return getCluster().length;
        }
        
        public String getColumnName(int col) {
            return columnNames[col];            
        }
        
        public Object getValueAt(int row, int col) {
            if (col < fieldNames.length) {
                return data.getElementAttribute(experiment.getGeneIndexMappedToData(getSortedCluster()[row]), col);
            } else {
                return auxData[getSortedCluster()[row]][col - fieldNames.length];
            }
            //return tableData[row][col];
        }
        /*
        public void setValueAt(Object value, int row, int col) {
            //tableData[row][col] = value;
            fireTableCellUpdated(row, col);
        }
         */
        
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
                    int shiftPressed = e.getModifiers()&InputEvent.SHIFT_MASK;
                    int controlPressed = e.getModifiers()&InputEvent.CTRL_MASK;
                    boolean originalOrder = (controlPressed != 0);
                    sortByColumn(column, !(sortedAscending[getClusterIndex()][column]), originalOrder);
                    sortedAscending[getClusterIndex()][column] = !(sortedAscending[getClusterIndex()][column]);
                    if (originalOrder) {
                        for (int i = 0; i < clusterModel.getColumnCount(); i++)
                        sortedAscending[getClusterIndex()][i] = false;
                    }               
                }
                //tableView.repaint();
            }
        };
        JTableHeader th = tableView.getTableHeader();
        //tableView.repaint();
        th.addMouseListener(listMouseListener);
    }    
    
    public void sortByColumn(int column, boolean ascending, boolean originalOrder) {
        if (originalOrder) {
            for (int i = 0; i < getSortedCluster().length; i++) {
                sortedClusters[this.clusterIndex][i] = getCluster()[i];
            }
            
            clusterTable.repaint();
            return;            
        }
        
        int[] sortedIndices = new int[getCluster().length];
        
        if (column < fieldNames.length) {
            SortableField[] sortFields = new SortableField[getCluster().length];
            for (int i = 0; i < sortFields.length; i++) {
                int currIndex = getCluster()[i];
                String currField = data.getElementAttribute(experiment.getGeneIndexMappedToData(getCluster()[i]), column);
                sortFields[i] = new SortableField(currIndex, currField);
            }
            
            Arrays.sort(sortFields);            
            for (int i = 0; i < sortFields.length; i++) {
                sortedIndices[i] = sortFields[i].getIndex();
            }
        } else {
            int obType = getObjectType(auxData[0][column - fieldNames.length]);
            if ((obType == ExperimentUtil.DOUBLE_TYPE) || (obType == ExperimentUtil.FLOAT_TYPE) || (obType == ExperimentUtil.INTEGER_TYPE)) {
                double[] origArray = new double[getCluster().length];
                for (int i = 0; i < origArray.length; i++) {
                    if (obType == ExperimentUtil.DOUBLE_TYPE) {
                        origArray[i] = ((Double)(auxData[getCluster()[i]][column - fieldNames.length])).doubleValue();
                    } else if (obType == ExperimentUtil.FLOAT_TYPE) {
                        origArray[i] = ((Float)(auxData[getCluster()[i]][column - fieldNames.length])).doubleValue();
                    } else if (obType == ExperimentUtil.INTEGER_TYPE) {
                        origArray[i] = ((Integer)(auxData[getCluster()[i]][column - fieldNames.length])).doubleValue();
                    }
                }
                QSort sortArray = new QSort(origArray);
                int[] sortedPrimaryIndices = sortArray.getOrigIndx();
                for (int i = 0; i < sortedPrimaryIndices.length; i++) {
                    sortedIndices[i] = getCluster()[sortedPrimaryIndices[i]];
                }
            } else if (obType == ExperimentUtil.BOOLEAN_TYPE) {
                SortableField[] sortFields = new SortableField[getCluster().length];
                for (int i = 0; i < sortFields.length; i++) {
                    int currIndex = getCluster()[i];
                    String currField = ((Boolean)(auxData[getCluster()[i]][column - fieldNames.length])).toString();
                    sortFields[i] = new SortableField(currIndex, currField);
                }
                
                Arrays.sort(sortFields);
                for (int i = 0; i < sortFields.length; i++) {
                    sortedIndices[i] = sortFields[i].getIndex();
                }                
            } else if (obType == ExperimentUtil.STRING_TYPE) {
                SortableField[] sortFields = new SortableField[getCluster().length];
                for (int i = 0; i < sortFields.length; i++) {
                    int currIndex = getCluster()[i];
                    String currField = (String)(auxData[getCluster()[i]][column - fieldNames.length]);
                    sortFields[i] = new SortableField(currIndex, currField);
                }
                
                Arrays.sort(sortFields);
                for (int i = 0; i < sortFields.length; i++) {
                    sortedIndices[i] = sortFields[i].getIndex();
                }                 
            }
        }
        
        if (!ascending) {
            sortedIndices = reverse(sortedIndices);
        }
        
        for (int i = 0; i < getSortedCluster().length; i++) {
            sortedClusters[this.clusterIndex][i] = sortedIndices[i];
        }
        clusterTable.repaint();
    }
    
    private class SortableField implements Comparable {
        private String field;
        private int index;
        
        SortableField(int index, String field) {
            this.index = index;
            this.field = field;
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
    
    
    private static int[] getTypes (Object[][] objData) {
        int[] types = new int[objData[0].length];
        for (int i = 0; i < types.length; i++) {
            types[i] = getObjectType(objData[0][i]);
            //Object 
        }
        return types;
    }
    
    private static int getObjectType(Object obj) {
        int obType = -1;
        if (obj instanceof Boolean) {
            return ExperimentUtil.BOOLEAN_TYPE;
        } else if (obj instanceof Double) {
            return ExperimentUtil.DOUBLE_TYPE;
        } else if (obj instanceof Float) {
            return ExperimentUtil.FLOAT_TYPE;
        } else if (obj instanceof Integer) {
            return ExperimentUtil.INTEGER_TYPE;
        } else if (obj instanceof String) {
            return ExperimentUtil.STRING_TYPE;
        } else {
            return obType;
        }
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
    
}
