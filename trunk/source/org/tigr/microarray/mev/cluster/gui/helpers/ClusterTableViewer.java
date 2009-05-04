/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * ClusterTableViewer.java
 *
 * Created on March 9, 2004, 11:47 AM
 */

package org.tigr.microarray.mev.cluster.gui.helpers;

import java.awt.*;
import java.awt.event.*;
import java.awt.datatransfer.Clipboard;
import java.awt.image.BufferedImage;
import java.beans.Expression;
import java.io.IOException;
import java.util.Arrays;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.*;

import org.tigr.microarray.mev.annotation.AnnoAttributeObj;
import org.tigr.microarray.mev.annotation.AnnotationConstants;
import org.tigr.microarray.mev.annotation.ChipAnnotationFieldConstants;
import org.tigr.microarray.mev.annotation.InsufficientArgumentsException;
import org.tigr.microarray.mev.annotation.PublicURL;
import org.tigr.microarray.mev.annotation.URLNotFoundException;
import org.tigr.microarray.mev.cluster.gui.IViewer;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IDisplayMenu;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.impl.GUIFactory;

import org.tigr.microarray.mev.cluster.clusterUtil.*;
import org.tigr.util.BrowserLauncher;
import org.tigr.util.QSort;

/**
 *
 * @author  nbhagaba
 */
public class ClusterTableViewer implements IViewer {
    
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
    protected static final String LINK_TO_URL_CMD = "link-to-url-cmd";    
    protected static final String BROADCAST_MATRIX_GAGGLE_CMD = "broadcast-matrix-to-gaggle";
    protected static final String BROADCAST_SELECTED_MATRIX_GAGGLE_CMD = "broadcast-selected-matrix-to-gaggle";
    protected static final String BROADCAST_NAMELIST_GAGGLE_CMD = "broadcast-namelist-to-gaggle";
    
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
    private int clusterIndex, xColumn;
    private int xRow;
    private int[][] clusters;
    private int[][] sortedClusters;
    private int[] samplesOrder;
    private int[] lastSelectedAnnotationIndices;
    private String[] auxTitles, fieldNames;
    private Object[][] auxData;
    private boolean[][] sortedAscending;  
    private JTable clusterTable;
    private ClusterTableModel clusterModel;  
    private ClusterTableSearchDialog searchDialog;
    private JMenuItem urlMenuItem;
    private int exptID = 0;
    
    protected LinkRenderer linkRenderer;
    
    /** DJS Main View Table Constructor
     * 
     */
    public ClusterTableViewer(IFramework framework){
    	this(framework.getData().getExperiment(),defGenesOrder(framework.getData().getExperiment().getNumberOfGenes()), framework.getData());
    	this.framework = framework;
    }
    
    public ClusterTableViewer(Experiment experiment, int[][] clusters, IData data) {
        this(experiment, clusters, data, new String[0], new Object[0][0]);
    }
    
    
    public ClusterTableViewer(Experiment experiment, int[][] clusters, IData data, String[] auxTitles, Object[][] auxData) {
        if (experiment == null) {
            throw new IllegalArgumentException("experiment == null");
        }
        this.data = data;
        this.experiment = experiment;
        this.exptID = experiment.getId();
        this.clusters = clusters;  
        this.fieldNames = data.getFieldNames();
        this.auxTitles = auxTitles;
        if(this.auxTitles == null) {
        	this.auxTitles = new String[0];
        }
        this.auxData = auxData;
        this.lastSelectedAnnotationIndices = new int[2];
        //this.xRow = -1;
        this.xColumn = -1;
        for (int i = 0; i < lastSelectedAnnotationIndices.length; i++) {
            lastSelectedAnnotationIndices[1] = 0;
        }
        
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
        clusterTable.setCellSelectionEnabled(true);
        clusterTable.setDefaultRenderer(Color.class, new ColorRenderer(true));
        linkRenderer = new LinkRenderer();
        clusterTable.setDefaultRenderer(AnnoAttributeObj.class, linkRenderer);
      
        TableColumn column = null;
        for (int i = 0; i < clusterModel.getColumnCount(); i++) {
            column = clusterTable.getColumnModel().getColumn(i);
            column.setMinWidth(30);
            //TODO
//            System.out.println("column " + i + ": " + column.getModelIndex() + ": " + clusterTable.getValueAt(100,i) + ": " + clusterTable.getValueAt(100,i).getClass().toString());
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
        clusterTable.addMouseListener(listener);
        clusterTable.addMouseListener(new LinkListener());
//        System.out.println("set up clipboard");
//        TransferHandler th = clusterTable.getTransferHandler();
//        if (th != null) {
//        Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
//        th.exportToClipboard(clusterTable, cb, TransferHandler.COPY);
//        }
        
    }    
    
    public JTable getTable(){
    	return clusterTable;
    }
    
    //DJS creates clusters if parameter is null.  For Main View Table.
    private static int[][] defGenesOrder(int size) {
        int[][] order = new int[1][size];
        for (int i=0; i<order[0].length; i++) {
            order[0][i] = i;
        }
        return order;
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
    
    public Expression getExpression(){
    	return new Expression(this, this.getClass(), "new",
				new Object[]{this.experiment, this.clusters, this.data, this.auxTitles, this.auxData});

    }
	
	public void setExperiment(Experiment e) {
		this.experiment = e;
		this.exptID = e.getId();
	}
	
	public int getExperimentID() {
		return this.exptID;
	}
	/* (non-Javadoc)
	 * @see org.tigr.microarray.mev.cluster.gui.IViewer#setExperimentID(int)
	 */
	public void setExperimentID(int id) {
		this.exptID = id;
	}    
    //EH end state-saving additions

    
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
        //panel.addMouseListener(listener);
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

     *	Returns the row index in the experiment's <code>FloatMatrix<\code>

     *  corresponding to the passed index to the clusters array

     */

    private int getExperimentRow(int row){
        return this.clusters[this.clusterIndex][row];
    }  
    
    /**
     * Returns index of a gene in the current cluster.
     */

    protected int getProbe(int row) {
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
        private String NO_ANNOTATION = "No Annotation Loaded";
        boolean hasAnnotation = true;
        
        public ClusterTableModel() {
        	if(fieldNames.length + auxTitles.length == 0){ //No annotation loaded
        		columnNames = new String[2];
        		columnNames[1] = NO_ANNOTATION;
        		hasAnnotation = false;
        	} else {
           columnNames = new String[1 + fieldNames.length + auxTitles.length];  
           int counter;
            for (counter = 1; counter < fieldNames.length + 1; counter++) {
                columnNames[counter] = fieldNames[counter - 1];
            }
            for (int i = counter; i < columnNames.length; i++) {
                columnNames[i] = auxTitles[i - counter];
            }
        }
	        columnNames[0] = "Stored Color";
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
                return data.getProbeColor(experiment.getGeneIndexMappedToData(getSortedCluster()[row])) == null? Color.white : data.getProbeColor(experiment.getGeneIndexMappedToData(getSortedCluster()[row]));
            } else if(!hasAnnotation){
            	return new Integer(row+1);
            } else if (col < fieldNames.length + 1) {
            	return data.getElementAnnotationObject(experiment.getGeneIndexMappedToData(getSortedCluster()[row]), fieldNames[col-1]);
            } else {
				return String.valueOf(auxData[getSortedCluster()[row]][col - (fieldNames.length + 1)]);

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
        //if (column == 0) return;
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
                Color currColor = data.getProbeColor(experiment.getGeneIndexMappedToData(getCluster()[i])) == null? Color.white : data.getProbeColor(experiment.getGeneIndexMappedToData(getCluster()[i]));
                origArray[i] = (double)(currColor.getRGB());
            }
            QSort sortArray = new QSort(origArray);
            int[] sortedPrimaryIndices = sortArray.getOrigIndx();
            for (int i = 0; i < sortedPrimaryIndices.length; i++) {
                sortedIndices[i] = getCluster()[sortedPrimaryIndices[i]];
            }            
            
        } else if (column < fieldNames.length +1) {
            SortableField[] sortFields = new SortableField[getCluster().length];
            for (int i = 0; i < sortFields.length; i++) {
                int currIndex = getCluster()[i];
                String currField = data.getElementAttribute(experiment.getGeneIndexMappedToData(getCluster()[i]), column - 1);
                sortFields[i] = new SortableField(currIndex, currField);
            }
            
            Arrays.sort(sortFields);            
            for (int i = 0; i < sortFields.length; i++) {
                sortedIndices[i] = sortFields[i].getIndex();
            }
        } else {
            int obType = getObjectType(auxData[0][column - (fieldNames.length +1)]);
            if ((obType == ExperimentUtil.DOUBLE_TYPE) || (obType == ExperimentUtil.FLOAT_TYPE) || (obType == ExperimentUtil.INTEGER_TYPE)) {
                double[] origArray = new double[getCluster().length];
                for (int i = 0; i < origArray.length; i++) {
                    if (obType == ExperimentUtil.DOUBLE_TYPE) {
                        origArray[i] = ((Double)(auxData[getCluster()[i]][column - (fieldNames.length + 1)])).doubleValue();
                    } else if (obType == ExperimentUtil.FLOAT_TYPE) {
                        origArray[i] = ((Float)(auxData[getCluster()[i]][column - (fieldNames.length + 1)])).doubleValue();
                    } else if (obType == ExperimentUtil.INTEGER_TYPE) {
                        origArray[i] = ((Integer)(auxData[getCluster()[i]][column - (fieldNames.length + 1)])).doubleValue();
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
                    String currField = ((Boolean)(auxData[getCluster()[i]][column - (fieldNames.length + 1)])).toString();
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
                    String currField = (String)(auxData[getCluster()[i]][column - (fieldNames.length + 1)]);
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

    public void setClusterColor(Color color) {
        if(color ==null){  //indicates removal of cluster
            //framework.removeCluster(getArrayMappedToData(), experiment, ClusterRepository.GENE_CLUSTER);
            boolean success = framework.removeSubCluster(getArrayMappedToSelectedIndices(), experiment, ClusterRepository.GENE_CLUSTER);
            if (!success) {
                JOptionPane.showMessageDialog(null, "Cluster not deleted! Selected rows must exactly correspond to a cluster created in the current algorithm run", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }    

    /**
     *  Sets cluster color
     */
    public void storeCluster(){
        framework.storeSubCluster(getArrayMappedToData(), experiment, ClusterRepository.GENE_CLUSTER);
        onDataChanged(this.data);
    }    
//    public void storeSelectedRowsAsCluster2(){
//        if (getArrayMappedToSelectedIndices().length == 0) {
//            JOptionPane.showMessageDialog(null, "No rows selected! Cluster will not be saved", "Error", JOptionPane.ERROR_MESSAGE);
//        } else {
//            framework.storeSubCluster(getArrayMappedToSelectedIndices(), experiment, ClusterRepository.GENE_CLUSTER);
//            
//        }
//    	
//    }
    
    public void storeSelectedRowsAsCluster() {
        if (getArrayMappedToSelectedIndices().length == 0) {
            JOptionPane.showMessageDialog(null, "No rows selected! Cluster will not be saved", "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            framework.storeSubCluster(getArrayMappedToSelectedIndices(), experiment, ClusterRepository.GENE_CLUSTER);
            onDataChanged(this.data);
        }
    }
    /**
     * Launches a new <code>MultipleExperimentViewer</code> containing the current cluster
     */
    public void launchNewSession(){
        framework.launchNewMAV(getArrayMappedToData(), this.experiment, "Multiple Experiment Viewer - Cluster Viewer", Cluster.GENE_CLUSTER);
    }    
    
    public void launchNewSessionWithSelectedRows() {
        framework.launchNewMAV(getArrayMappedToSelectedIndices(), this.experiment, "Multiple Experiment Viewer - Cluster Viewer", Cluster.GENE_CLUSTER);        
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

    
    private int [] getArrayMappedToData(){
        int [] clusterIndices = getCluster();
        if(clusterIndices == null || clusterIndices.length < 1)
            return clusterIndices;       

        int [] dataIndices = new int [clusterIndices.length];
        for(int i = 0; i < clusterIndices.length; i++){
            dataIndices[i] = this.experiment.getGeneIndexMappedToData(clusterIndices[i]);
        }
        return dataIndices;
    }   
    
    private int[] getArrayMappedToSelectedIndices() {
        int[] selectedRows = clusterTable.getSelectedRows();
        if ((selectedRows == null) || (selectedRows.length == 0)) {
            return new int[0];
        }
        
        int[] dataIndices = new int[selectedRows.length];        
        for (int i = 0; i < dataIndices.length; i++) {
            dataIndices[i] = this.experiment.getGeneIndexMappedToData(getSortedCluster()[selectedRows[i]]);
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
        
        //menuItem.addActionListener(listener);
        //menu.add(menuItem);
        
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
        
        //menuItem.addActionListener(listener);
        //menu.add(menuItem);      
        
        menu.addSeparator();
        
        menuItem = new JMenuItem("Sort table in original gene order...", GUIFactory.getIcon("TableViewerResult.gif"));
        menuItem.setActionCommand(SORT_ORIG_ORDER_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);
        
        //menuItem.addActionListener(listener);
        //menu.add(menuItem);   
     
        menu.addSeparator();
        
        urlMenuItem = new JMenuItem("Link to URL ...", GUIFactory.getIcon("ClusterInformationResult.gif"));
        urlMenuItem.setActionCommand(LINK_TO_URL_CMD);
        urlMenuItem.addActionListener(listener);
        //if (clusterTable.getSelectedRows().length != 1) 
        menu.add(urlMenuItem);        

        
        menu.addSeparator();

        menuItem = new JMenuItem("Broadcast Gene List to Gaggle", GUIFactory.getIcon("gaggle_icon_16.gif"));
        menuItem.setActionCommand(BROADCAST_NAMELIST_GAGGLE_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);
        
        menuItem = new JMenuItem("Broadcast Selected Rows as Matrix to Gaggle", GUIFactory.getIcon("gaggle_icon_16.gif"));
        menuItem.setActionCommand(BROADCAST_SELECTED_MATRIX_GAGGLE_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);

        menuItem = new JMenuItem("Broadcast Matrix to Gaggle", GUIFactory.getIcon("gaggle_icon_16.gif"));
        menuItem.setActionCommand(BROADCAST_MATRIX_GAGGLE_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);

    }    
    
    /**
     * Saves all clusters.
     */
    private void onSaveClusters() {
	//Frame frame = JOptionPane.getFrameForComponent(getContentComponent());
        Frame frame = JOptionPane.getFrameForComponent(clusterTable);
        try {
            if (auxTitles.length == 0) {
                ExperimentUtil.saveExperiment(frame, getExperiment(), getData(), getClusters());
            } else {
                ExperimentUtil.saveAllGeneClustersWithAux(frame, this.getExperiment(), this.getData(), this.getClusters(), auxTitles, auxData);  
            }
            //getContentComponent().repaint();
	} catch (Exception e) {
	    JOptionPane.showMessageDialog(frame, "Can not save clusters!", e.toString(), JOptionPane.ERROR_MESSAGE);
	    e.printStackTrace();
	}
        //clusterTable.repaint();
    }    

    /**
     * Save the viewer cluster.
     */
    private void onSaveCluster() {
	//Frame frame = JOptionPane.getFrameForComponent(getContentComponent());
        Frame frame = JOptionPane.getFrameForComponent(clusterTable);
	try {
            if (auxTitles.length == 0) {
                ExperimentUtil.saveExperiment(frame, getExperiment(), getData(), getCluster());
            } else {
                ExperimentUtil.saveGeneClusterWithAux(frame, this.getExperiment(), this.getData(), this.getCluster(), auxTitles, auxData); 
            }
            //getContentComponent().repaint();
	} catch (Exception e) {
	    JOptionPane.showMessageDialog(frame, "Can not save cluster!", e.toString(), JOptionPane.ERROR_MESSAGE);
	    e.printStackTrace();
	}
        //clusterTable.repaint();
    }   
    
    private void linkToURL() {
        JFrame frame = (JFrame)(JOptionPane.getFrameForComponent(clusterTable));
        //System.out.println("Before linkToURL: ClusterTablerViewer.lastSelectedAnnotationIndices = " + lastSelectedAnnotationIndices[0] + " " + lastSelectedAnnotationIndices[1]);        
        ExperimentUtil.linkToURL(frame, getExperiment(), getData(), getArrayMappedToSelectedIndices()[0], ExperimentUtil.lastSelectedAnnotationIndices);
        //System.out.println("After linkToURL: ClusterTablerViewer.lastSelectedAnnotationIndices = " + lastSelectedAnnotationIndices[0] + " " + lastSelectedAnnotationIndices[1]);
        //lastSelectedAnnotationIndices = ExperimentUtil.getLastSelectedAnnotationIndices();
    }
    
    private void linkToURL2() {
        JFrame frame = (JFrame)(JOptionPane.getFrameForComponent(clusterTable));        
        String colName = clusterTable.getColumnName(xColumn);
        ExperimentUtil.linkToURL(frame, getExperiment(), getData(), getArrayMappedToSelectedIndices()[0], colName, ExperimentUtil.lastSelectedAnnotationIndices);      
    }
    
    /**
     * Removes a public color.
     */
    private void onSetDefaultColor() {
	setClusterColor(null);
    }  
    
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
            } else if (command.equals(LINK_TO_URL_CMD)) {
                linkToURL2();
            } else if (command.equals(BROADCAST_MATRIX_GAGGLE_CMD)) {
                broadcastClusterGaggle();
            } else if (command.equals(BROADCAST_SELECTED_MATRIX_GAGGLE_CMD)) {
                broadcastSelectedClusterGaggle();
            } else if (command.equals(BROADCAST_NAMELIST_GAGGLE_CMD)) {
                broadcastNamelistGaggle();
            }
	}
	   
	public void mouseReleased(MouseEvent event) {
            //System.out.println("Mouse released");
	    maybeShowPopup(event);
	}
	
	public void mousePressed(MouseEvent event) {
            //System.out.println("Mouse pressed");
	    maybeShowPopup(event);
	}
	
	private void maybeShowPopup(MouseEvent e) {
	    if (!e.isPopupTrigger() || getCluster() == null || getCluster().length == 0) {
                //xRow = clusterTable.rowAtPoint(e.getPoint());
                //xColumn = clusterTable.columnAtPoint(e.getPoint());
                xColumn = clusterTable.getSelectedColumn();
                //System.out.println("xRow = " + xRow + ", xCol = " + xColumn);               
		return;
	    }
            
            if (clusterTable.getSelectedRowCount() != 1) {
                urlMenuItem.setEnabled(false);
            } else {
                urlMenuItem.setEnabled(true);
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
        oos.writeObject(samplesOrder);
        oos.writeObject(auxTitles);
        oos.writeObject(fieldNames);
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
        this.samplesOrder = (int[])ois.readObject();
        this.auxTitles = (String[])ois.readObject();        
        this.fieldNames = (String[])ois.readObject(); // need to serilaize this?
        this.auxData = (Object[][])ois.readObject();
        //this.origData = 
        this.sortedAscending = (boolean[][])ois.readObject();
        this.clusterTable = (JTable)ois.readObject();
        this.clusterModel = (ClusterTableModel)ois.readObject();
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
        clusterTable.addMouseListener(listener);
    }
    
    /** Returns int value indicating viewer type
     * Cluster.GENE_CLUSTER, Cluster.EXPERIMENT_CLUSTER, or -1 for both or unspecified
     */
    public int getViewerType() {
        return Cluster.GENE_CLUSTER;
    }    
    protected void broadcastClusterGaggle() {
    	framework.broadcastGeneCluster(getExperiment(), getCluster(), null);
	}
    protected void broadcastSelectedClusterGaggle() {
    	framework.broadcastGeneCluster(getExperiment(), getArrayMappedToSelectedIndices(), null);
	}
    protected void broadcastNamelistGaggle() {
    	framework.broadcastNamelist(getExperiment(), getCluster());
    }

    public class LinkRenderer extends DefaultTableCellRenderer {
        public void setValue(Object pValue) {
        		if(pValue instanceof AnnoAttributeObj && PublicURL.hasUrlForKey(((AnnoAttributeObj)pValue).getAttribName())) {

                    AnnoAttributeObj anno = (AnnoAttributeObj)pValue;
                    if(anno != null && 
                    		anno.getAttributeAt(0) != null && 
                    		!anno.getAttributeAt(0).toString().equalsIgnoreCase("na")) 
                    {
	        			String link = "<html>";
	
	                	//These links are in place for appearance's sake. They don't actually work.
	                	//linking is handled by the LinkListener
	                	link += "<a href=\"\">" + anno.getAttributeAt(0);
	                	for(int i=1; i<anno.getAttribCount(); i++) {
	                		link+= ", " + anno.getAttributeAt(i);
	                	}
                    //TODO
                    //This block is saved hopefully to user later if we can get multi-hyperlinks working in these tables.
//                    String linkUrl, linkText;
//                    for(int i=0; i<anno.getAttribCount(); i++) {
//                    	linkText = anno.getAttributeAt(i).toString();
//
//                    	if(linkText == null || linkText.equalsIgnoreCase("NA") || linkText.equals("")) {
//                    		setText("NA");
//                    		
//                
//                    	} else {
//	                    	try {
//	                    		if(anno.getAttribName().equalsIgnoreCase(AnnotationConstants.TGI_TC) ||
//	                    				anno.getAttribName().equalsIgnoreCase(AnnotationConstants.TGI_GC) ||
//	                    				anno.getAttribName().equalsIgnoreCase(AnnotationConstants.TGI_ORTH)
//	                    			) {
//	                    			linkUrl = PublicURL.getURL(anno.getAttribName(), new String[]{linkText, framework.getData().getChipAnnotation().getSpeciesName()}).toString();
//	                    		} else {
//	                    			linkUrl = PublicURL.getURL(anno.getAttribName(), new String[]{linkText}).toString();
//	                    		}
//	                        	link += "<a href=\"" + linkUrl + "\">" + linkText + "</a>";
//	                    	} catch (URLNotFoundException unfe) {
//	                    		link += linkText;
//	                    	} catch (InsufficientArgumentsException iae) {
//	                    		link += linkText;
//	                    	}
//	                    	if(i < anno.getAttribCount()-1)
//	                    		link += " | ";
//	                    }
//                    }
//                    if(anno.getAttribCount() <1)
//                    	link += "NA";

	                    link += "</a>";
	                    link += "</html>";
	                    setText(link); 
                    }else {
	                	setText("NA");
                    }
                } else {
                	setText(pValue.toString());                }
        }
    }

    public class LinkListener extends MouseAdapter {
        public void mouseEntered(MouseEvent event) {
        	xRow = clusterTable.rowAtPoint(event.getPoint());
            xColumn = clusterTable.columnAtPoint(event.getPoint()); 
            Object value = clusterTable.getValueAt(xRow, xColumn);

            if (	value != null && 
            		value instanceof AnnoAttributeObj &&
            		((AnnoAttributeObj)value).getAttribCount() > 0 &&
            		!((AnnoAttributeObj)value).getAttributeAt(0).equals(ChipAnnotationFieldConstants.NOT_AVAILABLE))
            {	
            	Cursor c = new Cursor(Cursor.HAND_CURSOR);
            	JOptionPane.getFrameForComponent(clusterTable).setCursor(c);
            } else {
            	Cursor c = Cursor.getDefaultCursor();
            	JOptionPane.getFrameForComponent(clusterTable).setCursor(c);
            }
            event.consume();
        }
        public void mouseExited(MouseEvent event) {
        	Cursor c = Cursor.getDefaultCursor();
        	JOptionPane.getFrameForComponent(clusterTable).setCursor(c);
        }
        
        public void mouseMoved(MouseEvent event){
        	xRow = clusterTable.rowAtPoint(event.getPoint());
            xColumn = clusterTable.columnAtPoint(event.getPoint()); 
            Object value = clusterTable.getValueAt(xRow, xColumn);
            if (	value != null && 
            		value instanceof AnnoAttributeObj &&
            		((AnnoAttributeObj)value).getAttribCount() > 0 &&
            		!((AnnoAttributeObj)value).getAttributeAt(0).equals(ChipAnnotationFieldConstants.NOT_AVAILABLE))
            {	
            	Cursor c = new Cursor(Cursor.HAND_CURSOR);
            	JOptionPane.getFrameForComponent(clusterTable).setCursor(c);
            } else {
            	Cursor c = Cursor.getDefaultCursor();
            	JOptionPane.getFrameForComponent(clusterTable).setCursor(c);
            }
            event.consume();
        }
        
        public void mousePressed(MouseEvent event) {

        	xRow = clusterTable.rowAtPoint(event.getPoint());
            xColumn = clusterTable.columnAtPoint(event.getPoint()); 
            Object value = clusterTable.getValueAt(xRow, xColumn);
            
            if (	value != null && 
            		value instanceof AnnoAttributeObj &&
            		((AnnoAttributeObj)value).getAttribCount() > 0 &&
            		!((AnnoAttributeObj)value).getAttributeAt(0).equals(ChipAnnotationFieldConstants.NOT_AVAILABLE) &&
            		PublicURL.hasUrlForKey(((AnnoAttributeObj)value).getAttribName())) 
            {
            	String url = "http://www.tm4.org/search?" + ((AnnoAttributeObj)value).getAttributeAt(0);
            	
            	String[] attribvalue = new String[((AnnoAttributeObj)value).getAttribCount()];
            	for(int i=0; i<attribvalue.length; i++) {
            		attribvalue[i] = ((AnnoAttributeObj)value).getAttributeAt(i).toString();
            	}
            	
            	try {
            		if(((AnnoAttributeObj)value).getAttribName().equalsIgnoreCase(AnnotationConstants.TGI_TC) ||
            				((AnnoAttributeObj)value).getAttribName().equalsIgnoreCase(AnnotationConstants.TGI_GC) ||
            				((AnnoAttributeObj)value).getAttribName().equalsIgnoreCase(AnnotationConstants.TGI_ORTH)		
            			) {
            			String[] newString = new String[attribvalue.length+1];
            			newString[0] = framework.getData().getChipAnnotation().getSpeciesName().toString();
            			for(int i=0; i<attribvalue.length; i++) {
            				newString[i+1] = attribvalue[i];
            			}
                		url = PublicURL.getURL(((AnnoAttributeObj)value).getAttribName(), newString);
            		} else {
            			url = PublicURL.getURL(((AnnoAttributeObj)value).getAttribName(), attribvalue);
            		}
               		BrowserLauncher.openURL(url);
            	} catch (InsufficientArgumentsException iae) {
            		iae.printStackTrace();
            	} catch (URLNotFoundException unfe) {
            		unfe.printStackTrace();
            	} catch (IOException ioe) {
            		ioe.printStackTrace();
               	}
            }
            event.consume();
        }
    } 
}
