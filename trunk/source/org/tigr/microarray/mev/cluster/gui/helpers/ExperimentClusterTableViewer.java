/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * ExperimentClusterTableViewer.java
 *
 * Created on April 26, 2004, 1:59 PM
 */

package org.tigr.microarray.mev.cluster.gui.helpers;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.beans.Expression;
import java.util.Arrays;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.*;

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
public class ExperimentClusterTableViewer implements IViewer {
    
    private static final String NO_GENES_STR = "No Genes in Cluster!";
    private static final Font ERROR_FONT = new Font("monospaced", Font.BOLD, 20);
    protected static final String STORE_CLUSTER_CMD = "store-cluster-cmd";
    protected static final String STORE_SELECTED_ROWS_CMD = "store-selected-rows-cmd";
    protected static final String SET_DEF_COLOR_CMD = "set-def-color-cmd";
    protected static final String SAVE_CLUSTER_CMD = "save-cluster-cmd";
    protected static final String SAVE_ALL_CLUSTERS_CMD = "save-all-clusters-cmd";
    protected static final String LAUNCH_NEW_SESSION_CMD = "launch-new-session-cmd";    
    protected static final String LAUNCH_NEW_SESSION_WITH_SEL_ROWS_CMD = "launch-new-session-with-sel-rows-cmd"; 
    protected static final String SEARCH_CMD = "search-cmd";
    protected static final String CLEAR_ALL_CMD = "clear-all-cmd";
    protected static final String SELECT_ALL_CMD = "select-all-cmd";
    protected static final String SORT_ORIG_ORDER_CMD = "sort-orig-order-cmd";
    public static final String BROADCAST_MATRIX_GAGGLE_CMD = "broadcast-matrix-to-gaggle";
    public static final String BROADCAST_SELECTED_MATRIX_GAGGLE_CMD = "broadcast-selected-matrix-to-gaggle";
    public static final String BROADCAST_NAMELIST_GAGGLE_CMD = "broadcast-namelist-to-gaggle";
    
    public static final int INTEGER_TYPE = 10;
    public static final int FLOAT_TYPE = 11;
    public static final int DOUBLE_TYPE = 12;
    public static final int STRING_TYPE = 13;
    public static final int BOOLEAN_TYPE = 14;    
    
    private JComponent header;
    private JPopupMenu popup;    
    private Experiment experiment;
    private IFramework framework;
    private IData data;
    private int clusterIndex;
    private int[][] clusters;
    private int[][] sortedClusters;
    //private int[] samplesOrder;
    private String[] auxTitles;//, fieldNames;
    private Object[][] auxData;
    //private Object[][] origData;
    private boolean[][] sortedAscending;  
    private JTable clusterTable;
    private ExperimentClusterTableModel clusterModel;  
    private ClusterTableSearchDialog searchDialog;    
    private int exptID = 0;

//    public String[] getAuxTitles() {return auxTitles;}
//    public Object[][] getAuxData() {return auxData;}
//    public int[][] getSortedClusters() {return sortedClusters;}
    public int getExptID() {return exptID;}
    /**
     * This constructor creates a new object identical to the one saved to the
     * xml file by XMLEncoder and IViewerPersistenceDelegate
     * 
     * @param clusters
     * @param auxTitles
     * @param auxData
     * @param sortedClusters
     * @param exptID
     
    public ExperimentClusterTableViewer(int[][] clusters, String[] auxTitles, 
    		Object[][] auxData, int[][] sortedClusters, Integer exptID) {
    	this.clusters = clusters;
    	this.auxTitles = auxTitles;
    	this.auxData = auxData;
    	this.sortedClusters = sortedClusters;
    	this.exptID = exptID.intValue();
        
        this.clusterModel = new ExperimentClusterTableModel();
        this.clusterTable = new JTable(clusterModel);
        clusterTable.setDefaultRenderer(Color.class, new ColorRenderer(true));
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
        
        searchDialog = new ClusterTableSearchDialog(JOptionPane.getFrameForComponent(clusterTable), clusterTable, false);  
        setMaxWidth(getContentComponent(), getHeaderComponent());  
        
		Listener listener = new Listener();
		this.popup = createJPopupMenu(listener);
		//getContentComponent().addMouseListener(listener);  
        clusterTable.addMouseListener(listener);   
        
    }*/
    //EH testing
    /**
     * @inheritdoc 
     */
    public Expression getExpression(){
    	return new Expression(this, this.getClass(), "new",
//			new Object[]{clusters, auxTitles, auxData, this.sortedClusters, new Integer(this.exptID)});  
    		new Object[]{experiment, clusters, data, auxTitles, auxData});
    }
    
    /** Creates a new instance of ExperimentClusterTableViewer */
    public ExperimentClusterTableViewer(Experiment experiment, int[][] clusters, IData data, String[] auxTitles, Object[][] auxData) {
        if (experiment == null) {
            throw new IllegalArgumentException("experiment == null");
        }
        this.data = data;
        this.experiment = experiment;
        this.clusters = clusters;  
        //this.fieldNames = data.getFieldNames();
        this.auxTitles = auxTitles;
        this.auxData = auxData;
        this.exptID = experiment.getId();
        
        this.sortedClusters = new int[clusters.length][];
        
        for (int i = 0; i < sortedClusters.length; i++) {
            sortedClusters[i] = new int[clusters[i].length];
        }
        
        for (int i = 0; i < sortedClusters.length; i++) {
            for (int j = 0; j < sortedClusters[i].length; j++) {
                sortedClusters[i][j] = clusters[i][j];
            }
        }
        
        this.clusterModel = new ExperimentClusterTableModel();
        this.clusterTable = new JTable(clusterModel);
        clusterTable.setDefaultRenderer(Color.class, new ColorRenderer(true));
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
        
        searchDialog = new ClusterTableSearchDialog(JOptionPane.getFrameForComponent(clusterTable), clusterTable, false);  
        setMaxWidth(getContentComponent(), getHeaderComponent());  
        
	Listener listener = new Listener();
	this.popup = createJPopupMenu(listener);
	//getContentComponent().addMouseListener(listener);  
        clusterTable.addMouseListener(listener);        
    }
    /** DJS Main View Table Constructor
     * 
     */
    public ExperimentClusterTableViewer(IFramework framework){
    	this(framework.getData().getExperiment(),defSamplesOrder(framework.getData().getExperiment().getNumberOfSamples()), framework.getData(), framework.getData().getSlideNameKeyArray(),framework.getData().getSampleAnnotationMatrix());
    	this.framework = framework;
    }
    public ExperimentClusterTableViewer(Experiment experiment, int[][] clusters, IData data) {
        this(experiment, clusters, data, new String[0], new Object[0][0]);        
    }
    
    //DJS creates clusters if parameter is null.  For Main View Table.
    private static int[][] defSamplesOrder(int size) {
        int[][] order = new int[1][size];
        for (int i=0; i<order[0].length; i++) {
            order[0][i] = i;
        }
        return order;
    }
    
    public JTable getTable(){
    	return clusterTable;
    }
    
    public void setTableClusters(int[][] mat){
    	sortedClusters = new int[mat.length][mat[0].length];
    	clusters = new int[mat.length][mat[0].length];
    	for (int i=0; i<mat.length; i++){
    		for (int j=0; j<mat[i].length; j++){
    			this.clusters[i][j]=mat[i][j];
    			this.sortedClusters[i][j]=mat[i][j];
    		}
    	}
        clusterTable.clearSelection();        
    }
    
    /** Returns a component to be inserted into scroll pane view port.
     */
    public JComponent getContentComponent() {
        
        JPanel panel = new JPanel();
        panel.setBackground(Color.gray);
        GridBagConstraints constraints = new GridBagConstraints();
        GridBagLayout gridbag = new GridBagLayout();
        panel.setLayout(gridbag);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.NORTH;
        
        //clusterTable.addMouseListener(new Listener());
        
        buildConstraints(constraints, 0, 0, 1, 1, 100, 100);
        gridbag.setConstraints(clusterTable, constraints);
        
        panel.add(clusterTable);
        //panel.addMouseListener(listener);
        return panel;        
    }
    
    /** Returns the corner component corresponding to the indicated corner,
     * posibly null
     */
    public JComponent getCornerComponent(int cornerIndex) {
        return null;
    }
    
    
    /** Returns a component to be inserted into scroll pane header.
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
    
    /** Invoked by the framework to save or to print viewer image.
     */
    public BufferedImage getImage() {
        return null;
    }
    
    /** Returns a component to be inserted into the scroll pane row header
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
        this.data = data;
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
     */
    public void onSelected(IFramework framework) {
        this.framework = framework;
        this.data = framework.getData();
        IDisplayMenu menu = framework.getDisplayMenu();
        Integer userObject = (Integer)framework.getUserObject();        
        setClusterIndex(userObject == null ? 0 : userObject.intValue());  
        clusterTable.clearSelection();        
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
     * Returns index of a gene in the current cluster.
     */
    protected int getProbe(int row) {
        return this.experiment.getGeneIndexMappedToData(row);
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
    
    class ExperimentClusterTableModel extends AbstractTableModel {
        String[] columnNames;
        
        public ExperimentClusterTableModel() {
            columnNames = new String[2 + auxTitles.length];
            //int counter;
            columnNames[0] = "Last Stored Color";
            columnNames[1] = "Sample Name";
            for (int i = 2; i < columnNames.length; i++) {
                columnNames[i] = auxTitles[i - 2];
            }
        }
        
        public int getColumnCount() {
            return columnNames.length;
        }
        
        public int getRowCount() {
            return getCluster().length;
        }
        
        public String getColumnName(int col) {
            return columnNames[col];            
        }        
        
        public Object getValueAt(int row, int col) {
            if (col == 0) {
                return data.getExperimentColor(getSortedCluster()[row]) == null? Color.white : data.getExperimentColor(getSortedCluster()[row]);
            } else if (col == 1) {
                return data.getFullSampleName(experiment.getSampleIndex(getSortedCluster()[row]));
            } else {
                return String.valueOf(auxData
                		[getSortedCluster()
                		 [row]]
                		 [col - 2]);
            }
        }
        
       public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
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
            clusterTable.clearSelection();
            return;            
        }     
        
        int[] sortedIndices = new int[getCluster().length];
        if (column == 0) {
            double[] origArray = new double[getCluster().length];  
            for (int i = 0; i < origArray.length; i++) {
                Color currColor = data.getExperimentColor(getCluster()[i]) == null? Color.white : data.getExperimentColor(getCluster()[i]);
                origArray[i] = (double)(currColor.getRGB());            
            }
            QSort sortArray = new QSort(origArray);
            int[] sortedPrimaryIndices = sortArray.getOrigIndx();
            for (int i = 0; i < sortedPrimaryIndices.length; i++) {
                sortedIndices[i] = getCluster()[sortedPrimaryIndices[i]];
            }  
            
        } else if (column == 1) {
            SortableField[] sortFields = new SortableField[getCluster().length];    
            for (int i = 0; i < sortFields.length; i++) {
                int currIndex = getCluster()[i];
                //String currField = data.getElementAttribute(experiment.getGeneIndexMappedToData(getCluster()[i]), column - 1);
                String currField = data.getFullSampleName(experiment.getSampleIndex(getCluster()[i]));
                sortFields[i] = new SortableField(currIndex, currField);
            }     
            
            Arrays.sort(sortFields);            
            for (int i = 0; i < sortFields.length; i++) {
                sortedIndices[i] = sortFields[i].getIndex(); 
            }
            
        } else {
            int obType = getObjectType(auxData[0][column - 2]); 
            if ((obType == ExperimentUtil.DOUBLE_TYPE) || (obType == ExperimentUtil.FLOAT_TYPE) || (obType == ExperimentUtil.INTEGER_TYPE)) {
                double[] origArray = new double[getCluster().length];
                for (int i = 0; i < origArray.length; i++) {
                    if (obType == ExperimentUtil.DOUBLE_TYPE) {
                        origArray[i] = ((Double)(auxData[getCluster()[i]][column - 2])).doubleValue();
                    } else if (obType == ExperimentUtil.FLOAT_TYPE) {
                        origArray[i] = ((Float)(auxData[getCluster()[i]][column - 2])).doubleValue();
                    } else if (obType == ExperimentUtil.INTEGER_TYPE) {
                        origArray[i] = ((Integer)(auxData[getCluster()[i]][column - 2])).doubleValue();
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
                    String currField = ((Boolean)(auxData[getCluster()[i]][column - 2])).toString();
                    sortFields[i] = new SortableField(currIndex, currField);
                }  
                
                Arrays.sort(sortFields);
                for (int i = 0; i < sortFields.length; i++) {
                    sortedIndices[i] = sortFields[i].getIndex();
                }
                
            } else if  (obType == ExperimentUtil.STRING_TYPE) {
                SortableField[] sortFields = new SortableField[getCluster().length];
                for (int i = 0; i < sortFields.length; i++) {
                    int currIndex = getCluster()[i];
                    String currField = (String)(auxData[getCluster()[i]][column - 2]);
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
        clusterTable.removeRowSelectionInterval(0, clusterTable.getRowCount() - 1);        
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
    
    private void searchTable(){
        
        searchDialog.setVisible(true);
        searchDialog.toFront();
        //searchDialog.requestFocus();
        searchDialog.setLocation(clusterTable.getLocation().x + 100, clusterTable.getLocation().y +100);
        
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
    
    /**
     * Sets public color for the current cluster.
     */
    
    /*
    public void setClusterColor(Color color) {
        if(color ==null){  //indicates removal of cluster
            framework.removeCluster(getCluster(), experiment, ClusterRepository.EXPERIMENT_CLUSTER);
        }
    }
     */
    
    /**
     * Sets public color for the current cluster.
     */

    public void setClusterColor(Color color) {
        if(color ==null){  //indicates removal of cluster
            //framework.removeCluster(getArrayMappedToData(), experiment, ClusterRepository.GENE_CLUSTER);
            boolean success = framework.removeSubCluster(getArrayMappedToSelectedIndices(), experiment, ClusterRepository.EXPERIMENT_CLUSTER);
            if (!success) {
                JOptionPane.showMessageDialog(null, "Cluster not deleted! Selected rows must exactly correspond to a cluster created in the current algorithm run", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }    
    
    /**
     *  Sets cluster color
     */
    public void storeCluster(){
        framework.storeCluster(getCluster(), experiment, ClusterRepository.EXPERIMENT_CLUSTER);       
        onDataChanged(this.data);
    }
    
    public void storeSelectedRowsAsCluster() {
        if (getArrayMappedToSelectedIndices().length == 0) {
            JOptionPane.showMessageDialog(null, "No rows selected! Cluster will not be saved", "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            framework.storeSubCluster(getArrayMappedToSelectedIndices(), experiment, ClusterRepository.EXPERIMENT_CLUSTER);
            onDataChanged(this.data);
        }
    }    
    
    /**
     * Launches a new <code>MultipleExperimentViewer</code> containing the current cluster
     */
    public void launchNewSession(){
        framework.launchNewMAV(getCluster(), this.experiment, "Multiple Experiment Viewer - Cluster Viewer", Cluster.EXPERIMENT_CLUSTER);
    }
    
    public void launchNewSessionWithSelectedRows() {
        framework.launchNewMAV(getArrayMappedToSelectedIndices(), this.experiment, "Multiple Experiment Viewer - Cluster Viewer", Cluster.EXPERIMENT_CLUSTER);        
    }    
    
    public void sortInOrigOrder() {
        for (int i = 0; i < getSortedCluster().length; i++) {
            sortedClusters[this.clusterIndex][i] = getCluster()[i];
        }
        
        clusterTable.repaint();  
        clusterTable.clearSelection();
        for (int i = 0; i < clusterModel.getColumnCount(); i++)
            sortedAscending[getClusterIndex()][i] = false;        
    }    
    
    /*
    private int [] getArrayMappedToData(){
        int [] clusterIndices = getCluster();
        if( clusterIndices == null || clusterIndices.length < 1)
            return clusterIndices;
        
        int [] dataIndices = new int [clusterIndices.length];
        for(int i = 0; i < clusterIndices.length; i++){
            dataIndices[i] = this.experiment.getGeneIndexMappedToData(clusterIndices[i]);
        }
        return dataIndices;
    }
     */    
    
    private int[] getArrayMappedToSelectedIndices() {
        int[] selectedRows = clusterTable.getSelectedRows();
        if ((selectedRows == null) || (selectedRows.length == 0)) {
            return new int[0];
        }
        
        int[] dataIndices = new int[selectedRows.length];        
        for (int i = 0; i < dataIndices.length; i++) {
            dataIndices[i] = getSortedCluster()[selectedRows[i]];
        }
        return dataIndices;
    }    
    
    /**
     * Creates a popup menu.
     */
    private JPopupMenu createJPopupMenu(Listener listener) {
	JPopupMenu popup = new JPopupMenu();
	addMenuItems(popup, listener);
	return popup;
    }    
    
    protected void addMenuItems(JPopupMenu menu, ActionListener listener) {
        JMenuItem menuItem;
        menuItem = new JMenuItem("Store entire cluster", GUIFactory.getIcon("new16.gif"));
        menuItem.setActionCommand(STORE_CLUSTER_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);
        
        //menu.addSeparator();
        
        menuItem = new JMenuItem("Store selected rows as cluster", GUIFactory.getIcon("new16.gif"));
        menuItem.setActionCommand(STORE_SELECTED_ROWS_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("Delete cluster composed of selected rows", GUIFactory.getIcon("delete16.gif"));
        menuItem.setActionCommand(SET_DEF_COLOR_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);        
        
        menu.addSeparator();        
        
        menuItem = new JMenuItem("Launch new session with entire cluster", GUIFactory.getIcon("launch_new_mav.gif"));
        menuItem.setActionCommand(LAUNCH_NEW_SESSION_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);       
        
        //menu.addSeparator();
        
        menuItem = new JMenuItem("Launch new session with selected rows", GUIFactory.getIcon("launch_new_mav.gif"));
        menuItem.setActionCommand(LAUNCH_NEW_SESSION_WITH_SEL_ROWS_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);       
        
        //menu.addSeparator();        
        
        menu.addSeparator();
        
        
        menuItem = new JMenuItem("Save cluster...", GUIFactory.getIcon("save16.gif"));
        menuItem.setActionCommand(SAVE_CLUSTER_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("Save all clusters...", GUIFactory.getIcon("save16.gif"));
        menuItem.setActionCommand(SAVE_ALL_CLUSTERS_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);
        
        menuItem.addActionListener(listener);
        menu.add(menuItem);
        
        menu.addSeparator();
        
        menuItem = new JMenuItem("Search...", GUIFactory.getIcon("ClusterInformationResult.gif"));
        menuItem.setActionCommand(SEARCH_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);
        
        menuItem.addActionListener(listener);
        menu.add(menuItem);  
        
        menu.addSeparator();
        
        menuItem = new JMenuItem("Select all rows...", GUIFactory.getIcon("TableViewerResult.gif"));
        menuItem.setActionCommand(SELECT_ALL_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);
        
        menuItem.addActionListener(listener);
        menu.add(menuItem);        
        
        menuItem = new JMenuItem("Clear all selections...", GUIFactory.getIcon("TableViewerResult.gif"));
        menuItem.setActionCommand(CLEAR_ALL_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);
        
        menuItem.addActionListener(listener);
        menu.add(menuItem);      
        
        menu.addSeparator();
        
        menuItem = new JMenuItem("Sort table in original experiment order...", GUIFactory.getIcon("TableViewerResult.gif"));
        menuItem.setActionCommand(SORT_ORIG_ORDER_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);
        
        menuItem.addActionListener(listener);
        menu.add(menuItem);           

        menu.addSeparator();
        
        menuItem = new JMenuItem("Broadcast Matrix to Gaggle", GUIFactory.getIcon("gaggle_icon_16.gif"));
        menuItem.setActionCommand(BROADCAST_MATRIX_GAGGLE_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("Broadcast Selected Rows as Matrix to Gaggle", GUIFactory.getIcon("gaggle_icon_16.gif"));
        menuItem.setActionCommand(BROADCAST_SELECTED_MATRIX_GAGGLE_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("Broadcast Gene List to Gaggle", GUIFactory.getIcon("gaggle_icon_16.gif"));
        menuItem.setActionCommand(BROADCAST_NAMELIST_GAGGLE_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);
        
    }    
    
    private void onSaveClusters() {
        Frame frame = JOptionPane.getFrameForComponent(clusterTable);
        try {
            if (auxTitles.length == 0) {
                //ExperimentUtil.saveExperiment(frame, getExperiment(), getData(), getClusters());
                ExperimentUtil.saveAllExperimentClusters(frame, getExperiment(), getData(), getClusters());
            } else {
                ExperimentUtil.saveAllExperimentClustersWithAux(frame, this.getExperiment(), this.getData(), this.getClusters(), auxTitles, auxData);  
            }
            //getContentComponent().repaint();
	} catch (Exception e) {
	    JOptionPane.showMessageDialog(frame, "Can not save clusters!", e.toString(), JOptionPane.ERROR_MESSAGE);
	    e.printStackTrace();
	}        
    }
    
    private void onSaveCluster() {
        Frame frame = JOptionPane.getFrameForComponent(clusterTable);       
        try {
            if (auxTitles.length == 0) {
                ExperimentUtil.saveExperimentCluster(frame, getExperiment(), getData(), getCluster());
            } else {
                ExperimentUtil.saveExperimentClusterWithAux(frame, this.getExperiment(), this.getData(), this.getCluster(), auxTitles, auxData); 
            }
        } catch (Exception e) {
	    JOptionPane.showMessageDialog(frame, "Can not save cluster!", e.toString(), JOptionPane.ERROR_MESSAGE);
	    e.printStackTrace();            
        }        
    }
    
    /**
     * Removes a public color.
     */
    private void onSetDefaultColor() {
	setClusterColor(null);
    }    
    /*
    private static int[][] defSamplesOrder(int size) {
        int[][] order = new int[1][size];
        for (int i=0; i<order[0].length; i++) {
            order[0][i] = i;
        }
        return order;
    } 
     */  
    
    /**
     * The class to listen to mouse and action events.
     */
    private class Listener extends MouseAdapter implements ActionListener {
	
	public void actionPerformed(ActionEvent e) {
	    String command = e.getActionCommand();
	    if (command.equals(SAVE_CLUSTER_CMD)) {
	    	onSaveCluster();
                //getContentComponent().validate();
	    } else if (command.equals(SAVE_ALL_CLUSTERS_CMD)) {
	    	onSaveClusters();
                //getContentComponent().validate();
	    } else if (command.equals(STORE_CLUSTER_CMD)) {
	    	storeCluster();
	    } else if (command.equals(STORE_SELECTED_ROWS_CMD)) {
            storeSelectedRowsAsCluster();
        } else if (command.equals(SET_DEF_COLOR_CMD)) {
            onSetDefaultColor();
	    }  else if(command.equals(LAUNCH_NEW_SESSION_CMD)){
            launchNewSession();
        }  else if(command.equals(LAUNCH_NEW_SESSION_WITH_SEL_ROWS_CMD)){
            launchNewSessionWithSelectedRows();
        }  else if (command.equals(SEARCH_CMD)) {
            searchTable();
        } else if (command.equals(CLEAR_ALL_CMD)) {
            clusterTable.clearSelection();
        } else if (command.equals(SELECT_ALL_CMD)) {
            clusterTable.selectAll();
        } else if (command.equals(SORT_ORIG_ORDER_CMD)) {
            sortInOrigOrder();
	    } else if(command.equals(BROADCAST_MATRIX_GAGGLE_CMD)){
            broadcastClusterGaggle();
	    } else if(command.equals(BROADCAST_SELECTED_MATRIX_GAGGLE_CMD)){
            broadcastSelectedClusterGaggle();
	    } else if(command.equals(BROADCAST_NAMELIST_GAGGLE_CMD)){
            broadcastNamelistGaggle();
        }
	}
	   
	public void mouseReleased(MouseEvent event) {
	    maybeShowPopup(event);
	}
	
	public void mousePressed(MouseEvent event) {
	    maybeShowPopup(event);
	}
	
	private void maybeShowPopup(MouseEvent e) {
	    if (!e.isPopupTrigger() || getCluster() == null || getCluster().length == 0) {
		return;
	    }
	    popup.show(e.getComponent(), e.getX(), e.getY());
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
    
    private void writeObject(java.io.ObjectOutputStream oos) throws java.io.IOException {
        oos.writeObject(experiment);
        oos.writeObject(clusters);
        oos.writeObject(header);
        oos.writeObject(popup);
        
        //oos.writeObject(framework);
        //oos.writeObject(data);
        oos.writeInt(clusterIndex);
        oos.writeObject(sortedClusters);
        //oos.writeObject(samplesOrder);
        oos.writeObject(auxTitles);
        //oos.writeObject(fieldNames);
        oos.writeObject(auxData);
        //oos.writeObject(origData);
        oos.writeObject(sortedAscending);
        oos.writeObject(clusterTable);
        oos.writeObject(clusterModel);
        oos.writeObject(searchDialog);
    }    
    
    private void readObject(java.io.ObjectInputStream ois) throws java.io.IOException, ClassNotFoundException {
        this.experiment = (Experiment)ois.readObject();
        this.clusters = (int [][])ois.readObject();
        this.header = (JComponent)ois.readObject();
        this.popup = (JPopupMenu)ois.readObject();
        
        //this.framework = (IFramework)ois.readObject();
        //this.data = (IData)ois.readObject();
        this.clusterIndex = ois.readInt();
        this.sortedClusters = (int[][])ois.readObject();
        //this.samplesOrder = (int[])ois.readObject();
        this.auxTitles = (String[])ois.readObject();        
        //this.fieldNames = (String[])ois.readObject(); // need to serilaize this?
        this.auxData = (Object[][])ois.readObject();
        //this.origData = 
        this.sortedAscending = (boolean[][])ois.readObject();
        this.clusterTable = (JTable)ois.readObject();
        this.clusterModel = (ExperimentClusterTableModel)ois.readObject();
        this.searchDialog = (ClusterTableSearchDialog)ois.readObject();
        
        //this.fieldNames = data.getFieldNames();
        
        clusterTable.setDefaultRenderer(Color.class, new ColorRenderer(true));
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
        
        searchDialog = new ClusterTableSearchDialog(JOptionPane.getFrameForComponent(clusterTable), clusterTable, false);  
        setMaxWidth(getContentComponent(), getHeaderComponent());  
        
	Listener listener = new Listener();
	this.popup = createJPopupMenu(listener);
	//getContentComponent().addMouseListener(listener);  
        clusterTable.addMouseListener(listener);
    }    
    
    /** Returns int value indicating viewer type
     * Cluster.GENE_CLUSTER, Cluster.EXPERIMENT_CLUSTER, or -1 for both or unspecified
     */
    public int getViewerType() {
        return Cluster.EXPERIMENT_CLUSTER;
    }
    
	/* (non-Javadoc)
	 * @see org.tigr.microarray.mev.cluster.gui.IViewer#setExperiment(org.tigr.microarray.mev.cluster.gui.Experiment)
	 */
	public void setExperiment(Experiment e) {
		this.experiment = e;
	}

	/* (non-Javadoc)
	 * @see org.tigr.microarray.mev.cluster.gui.IViewer#getExperimentID()
	 */
	public int getExperimentID() {
		return exptID;
	}

	/* (non-Javadoc)
	 * @see org.tigr.microarray.mev.cluster.gui.IViewer#setExperimentID(int)
	 */
	public void setExperimentID(int id) {
		this.exptID = id;
	}
    public void broadcastClusterGaggle() {
    	framework.broadcastGeneCluster(getExperiment(), null, getCluster());
	}
    public void broadcastSelectedClusterGaggle() {
    	framework.broadcastGeneCluster(getExperiment(), null, clusterTable.getSelectedColumns());
	}
    public void broadcastNamelistGaggle() {
    	framework.broadcastNamelist(getExperiment(), getExperiment().getRows());
    }
    
}
