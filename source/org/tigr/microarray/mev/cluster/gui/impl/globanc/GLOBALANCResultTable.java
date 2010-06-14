package org.tigr.microarray.mev.cluster.gui.impl.globanc;


import java.awt.*;
import java.awt.event.*;
import java.awt.datatransfer.Clipboard;
import java.awt.image.BufferedImage;
import java.beans.Expression;
import java.util.Arrays;

import javax.swing.*;
import javax.swing.table.*;
import org.tigr.microarray.mev.cluster.gui.IViewer;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IDisplayMenu;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentUtil;
import org.tigr.microarray.mev.cluster.gui.impl.GUIFactory;
import org.tigr.util.QSort;

/**
 *
 * @author  dschlauch
 */
public class GLOBALANCResultTable implements IViewer {
    
    protected static final String STORE_CLUSTER_CMD = "store-cluster-cmd";
    protected static final String STORE_SELECTED_ROWS_CMD = "store-selected-rows-cmd";
    protected static final String SET_DEF_COLOR_CMD = "set-def-color-cmd";
    protected static final String SAVE_CLUSTER_CMD = "save-cluster-cmd";
    protected static final String SAVE_ALL_CLUSTERS_CMD = "save-all-clusters-cmd";
    protected static final String LAUNCH_NEW_SESSION_CMD = "launch-new-session-cmd";    
    protected static final String LAUNCH_NEW_SESSION_WITH_SEL_ROWS_CMD = "launch-new-session-with-sel-rows-cmd"; 
    protected static final String SEARCH_CMD = "search-cmd";
    protected static final String CLEAR_ALL_CMD = "clear-all-cmd";
    protected static final String COPY_CMD = "copy-cells";
    protected static final String SELECT_ALL_CMD = "select-all-cmd";
    protected static final String SORT_ORIG_ORDER_CMD = "sort-orig-order-cmd";  
    protected static final String BROADCAST_MATRIX_GAGGLE_CMD = "broadcast-matrix-to-gaggle";
    protected static final String BROADCAST_SELECTED_MATRIX_GAGGLE_CMD = "broadcast-selected-matrix-to-gaggle";
    protected static final String BROADCAST_NAMELIST_GAGGLE_CMD = "broadcast-namelist-to-gaggle";
    public static final String BROADCAST_MATRIX_GENOME_BROWSER_CMD = "broadcast-matrix-to-genome-browser";
    
    public static final int INTEGER_TYPE = 10;
    public static final int FLOAT_TYPE = 11;
    public static final int DOUBLE_TYPE = 12;
    public static final int STRING_TYPE = 13;
    public static final int BOOLEAN_TYPE = 14;    
    
    private JComponent header;
    private JPopupMenu popup;    
    private Object[][] data;
    private int[] indices;
    private int[] sortedIndices;
    private String[] columnTitles;
    private boolean[] sortedAscending;  
    private JTable clusterTable;
    private ClusterTableModel clusterModel;  
    private int exptID = 0;
    
    public GLOBALANCResultTable(Object[][] data, String[] auxTitles) {
        this.data = data;
        indices = new int[data.length];
        this.sortedIndices = new int[data.length];
        for (int i=0; i<data.length; i++){
        	indices[i]=i;
        	sortedIndices[i]=i;
        }
        this.columnTitles = auxTitles;
        this.clusterModel = new ClusterTableModel();
        this.clusterTable = new JTable(clusterModel);
        clusterTable.setCellSelectionEnabled(true);
        TableColumn column = null;
        for (int i = 0; i < clusterModel.getColumnCount(); i++) {
            column = clusterTable.getColumnModel().getColumn(i);
            column.setMinWidth(30);
        } 
        
        this.sortedAscending = new boolean[clusterModel.getColumnCount()];
        for (int j = 0; j < sortedAscending.length; j++) {
            sortedAscending[j] = false;
        }
        
        addMouseListenerToHeaderInTable(clusterTable);
        header  = clusterTable.getTableHeader();        
        
        setMaxWidth(getContentComponent(), getHeaderComponent());  
        
		Listener listener = new Listener();
		this.popup = createJPopupMenu(listener);
        clusterTable.addMouseListener(listener);
        
    }    

    public JTable getTable(){
    	return clusterTable;
    }
    
    public Expression getExpression(){
    	return null;
  }

    protected String[] getAuxTitles() {
    	return columnTitles;
    }	
	public void setExperiment(Experiment e) {
		this.exptID = e.getId();
	}
	
	public int getExperimentID() {
		return this.exptID;
	}
	public void setExperimentID(int id) {
		this.exptID = id;
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

    }
    
    /**
     * Invoked by the framework when this viewer was deselected.
     *
     */
    public void onDeselected() {

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
    }
    
    /**
     * Sets cluster index to be displayed.
     */

    public void setClusterIndex(int clusterIndex) {

    }

    /**
     * Returns index of current cluster.
     */

    public int getClusterIndex() {
        return 0;
    }    

    /**
     * Returns indices of current cluster.
     */
    public int[] getCluster() {
        return indices;
    }
    
    /**
     * Returns all the clusters.
     */
    public int[][] getClusters() {
    	int[][] cls = new int[1][];
    	cls[0] = indices;
        return cls;
    }
    
    public int[] getSortedCluster() {
        return sortedIndices;
    }
    
    /**
     * Returns index of a gene in the current cluster.
     */

    protected int getProbe(int row) {
        return this.indices[row];
    }    
   
    
    /**
     * Returns wrapped experiment.
     */

    public Experiment getExperiment() {
    	return null;
    }
    
    /**
     * Returns the data.
     */
    public IData getData() {
    	return null;
    }  
    
    class ClusterTableModel extends AbstractTableModel {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		String[] columnNames;
        boolean hasAnnotation = true;
        
        public ClusterTableModel() {
            columnNames = new String[columnTitles.length];  
            for (int i = 0; i < columnNames.length; i++) {
                columnNames[i] = columnTitles[i];
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
			return String.valueOf(data[getSortedCluster()[row]][col]);
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
        final JTable tableView = table;
        tableView.setColumnSelectionAllowed(true);
        MouseAdapter listMouseListener = new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                TableColumnModel columnModel = tableView.getColumnModel();
                int viewColumn = columnModel.getColumnIndexAtX(e.getX());
                int column = tableView.convertColumnIndexToModel(viewColumn);
                if (e.getClickCount() == 1 && column != -1) {
                    int controlPressed = e.getModifiers()&InputEvent.CTRL_MASK;
                    boolean originalOrder = (controlPressed != 0);
                    sortByColumn(column, !(sortedAscending[column]), originalOrder);
                    sortedAscending[column] = !(sortedAscending[column]);
                    if (originalOrder) {
                        for (int i = 0; i < clusterModel.getColumnCount(); i++)
                        sortedAscending[i] = false;
                    }               
                }
            }
        };
        JTableHeader th = tableView.getTableHeader();
        th.addMouseListener(listMouseListener);
    }    
    
    public void sortByColumn(int column, boolean ascending, boolean originalOrder) {
        if (originalOrder) {
            for (int i = 0; i < getSortedCluster().length; i++) {
                sortedIndices[i] = getCluster()[i];
            }
            
            clusterTable.repaint();
            clusterTable.clearSelection();
            return;            
        }
        
        int[] sortedArray = new int[getCluster().length];
        int obType = getObjectType(data[0][column]);
        if ((obType == ExperimentUtil.DOUBLE_TYPE) || (obType == ExperimentUtil.FLOAT_TYPE) || (obType == ExperimentUtil.INTEGER_TYPE)) {
            double[] origArray = new double[getCluster().length];
            for (int i = 0; i < origArray.length; i++) {
                if (obType == ExperimentUtil.DOUBLE_TYPE) {
                    origArray[i] = ((Double)(data[getCluster()[i]][column])).doubleValue();
                } else if (obType == ExperimentUtil.FLOAT_TYPE) {
                    origArray[i] = ((Float)(data[getCluster()[i]][column ])).doubleValue();
                } else if (obType == ExperimentUtil.INTEGER_TYPE) {
                    origArray[i] = ((Integer)(data[getCluster()[i]][column])).doubleValue();
                }
            }
            QSort sortArray = new QSort(origArray);
            int[] sortedPrimaryIndices = sortArray.getOrigIndx();
            for (int i = 0; i < sortedPrimaryIndices.length; i++) {
                sortedArray[i] = getCluster()[sortedPrimaryIndices[i]];
            }
        } else if (obType == ExperimentUtil.STRING_TYPE) {
            SortableField[] sortFields = new SortableField[getCluster().length];
            for (int i = 0; i < sortFields.length; i++) {
                int currIndex = getCluster()[i];
                String currField = (String)(data[getCluster()[i]][column]);
                sortFields[i] = new SortableField(currIndex, currField);
            }
            
            Arrays.sort(sortFields);
            for (int i = 0; i < sortFields.length; i++) {
                sortedArray[i] = sortFields[i].getIndex();
            }                 
        }
        
        
        if (!ascending) {
            sortedArray = reverse(sortedArray);
        }
        
        for (int i = 0; i < getSortedCluster().length; i++) {
            sortedIndices[i] = sortedArray[i];
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
    
    /**
     * Launches a new <code>MultipleExperimentViewer</code> containing the current cluster
     */
    public void launchNewSession(){

    }    
    
    public void launchNewSessionWithSelectedRows() {

   }
    
    public void copyCells(){
		TransferHandler th = clusterTable.getTransferHandler();
		if (th != null) {
		Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
		th.exportToClipboard(clusterTable, cb, TransferHandler.COPY);
		}
    }
    
    public void sortInOrigOrder() {
        for (int i = 0; i < getSortedCluster().length; i++) {
            sortedIndices[i] = getCluster()[i];
        }
        
        clusterTable.repaint();  
        clusterTable.clearSelection();
        for (int i = 0; i < clusterModel.getColumnCount(); i++)
            sortedAscending[i] = false;        
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
//        menuItem = new JMenuItem("Store entire cluster", GUIFactory.getIcon("new16.gif"));
//        menuItem.setActionCommand(STORE_CLUSTER_CMD);
//        menuItem.addActionListener(listener);
//        menu.add(menuItem);
//        
//        //menu.addSeparator();
//        
//        menuItem = new JMenuItem("Store selected rows as cluster", GUIFactory.getIcon("new16.gif"));
//        menuItem.setActionCommand(STORE_SELECTED_ROWS_CMD);
//        menuItem.addActionListener(listener);
//        menu.add(menuItem);
//        
//        menuItem = new JMenuItem("Delete cluster composed of selected rows", GUIFactory.getIcon("delete16.gif"));
//        menuItem.setActionCommand(SET_DEF_COLOR_CMD);
//        menuItem.addActionListener(listener);
//        menu.add(menuItem);        
//        
//        menu.addSeparator();        
//        
//        menuItem = new JMenuItem("Launch new session with entire cluster", GUIFactory.getIcon("launch_new_mav.gif"));
//        menuItem.setActionCommand(LAUNCH_NEW_SESSION_CMD);
//        menuItem.addActionListener(listener);
//        menu.add(menuItem);       
//        
//        //menu.addSeparator();
//        
//        menuItem = new JMenuItem("Launch new session with selected rows", GUIFactory.getIcon("launch_new_mav.gif"));
//        menuItem.setActionCommand(LAUNCH_NEW_SESSION_WITH_SEL_ROWS_CMD);
//        menuItem.addActionListener(listener);
//        menu.add(menuItem);       
//        
//        //menu.addSeparator();        
//        
//        menu.addSeparator();
//        
//        
//        menuItem = new JMenuItem("Save cluster...", GUIFactory.getIcon("save16.gif"));
//        menuItem.setActionCommand(SAVE_CLUSTER_CMD);
//        menuItem.addActionListener(listener);
//        menu.add(menuItem);
//        
//        menuItem = new JMenuItem("Save all clusters...", GUIFactory.getIcon("save16.gif"));
//        menuItem.setActionCommand(SAVE_ALL_CLUSTERS_CMD);
//        menuItem.addActionListener(listener);
//        menu.add(menuItem);
        
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

        menuItem = new JMenuItem("Copy", GUIFactory.getIcon("TableViewerResult.gif"));
        menuItem.setActionCommand(COPY_CMD);
        menuItem.addActionListener(listener);
        menu.add(menuItem);
        
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
        
//        menuItem = new JMenuItem("Toggle global/gene sparkline scale ", GUIFactory.getIcon("TableViewerResult.gif"));
//        menuItem.setActionCommand(TOGGLE_SCALE);
//        menuItem.addActionListener(listener);
//        menu.add(menuItem);
        
        //menuItem.addActionListener(listener);
        //menu.add(menuItem);   
     
//        menu.addSeparator();
//        
//        urlMenuItem = new JMenuItem("Link to URL ...", GUIFactory.getIcon("ClusterInformationResult.gif"));
//        urlMenuItem.setActionCommand(LINK_TO_URL_CMD);
//        urlMenuItem.addActionListener(listener);
//        //if (clusterTable.getSelectedRows().length != 1) 
//        menu.add(urlMenuItem);        

        
        menu.addSeparator();
//
//        menuItem = new JMenuItem("Broadcast Gene List to Gaggle", GUIFactory.getIcon("gaggle_icon_16.gif"));
//        menuItem.setActionCommand(BROADCAST_NAMELIST_GAGGLE_CMD);
//        menuItem.addActionListener(listener);
//        menu.add(menuItem);
//        
//        menuItem = new JMenuItem("Broadcast Selected Rows as Matrix to Gaggle", GUIFactory.getIcon("gaggle_icon_16.gif"));
//        menuItem.setActionCommand(BROADCAST_SELECTED_MATRIX_GAGGLE_CMD);
//        menuItem.addActionListener(listener);
//        menu.add(menuItem);
//
//        menuItem = new JMenuItem("Broadcast Matrix to Gaggle", GUIFactory.getIcon("gaggle_icon_16.gif"));
//        menuItem.setActionCommand(BROADCAST_MATRIX_GAGGLE_CMD);
//        menuItem.addActionListener(listener);
//        menu.add(menuItem);
//
//        menuItem = new JMenuItem("Broadcast Matrix to Genome Browser", GUIFactory.getIcon("gaggle_icon_16.gif"));
//        menuItem.setActionCommand(BROADCAST_MATRIX_GENOME_BROWSER_CMD);
//        menuItem.addActionListener(listener);
//        menu.add(menuItem);
       
    }    

    /**
     * The class to listen to mouse and action events.
     */
    private class Listener extends MouseAdapter implements ActionListener {
	
	public void actionPerformed(ActionEvent e) {
	    String command = e.getActionCommand();
        if(command.equals(LAUNCH_NEW_SESSION_WITH_SEL_ROWS_CMD)){

        }  else if (command.equals(SEARCH_CMD)) {

        } else if (command.equals(CLEAR_ALL_CMD)) {

        } else if (command.equals(COPY_CMD)) {
            copyCells();
        } else if (command.equals(SELECT_ALL_CMD)) {
            clusterTable.selectAll();
        } else if (command.equals(SORT_ORIG_ORDER_CMD)) {
            sortInOrigOrder();
        } else if (command.equals(BROADCAST_MATRIX_GAGGLE_CMD)) {

        } else if (command.equals(BROADCAST_SELECTED_MATRIX_GAGGLE_CMD)) {

        } else if (command.equals(BROADCAST_NAMELIST_GAGGLE_CMD)) {

		} else if (command.equals(BROADCAST_MATRIX_GENOME_BROWSER_CMD)) {

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
 
    /** Returns int value indicating viewer type
     * Cluster.GENE_CLUSTER, Cluster.EXPERIMENT_CLUSTER, or -1 for both or unspecified
     */
    public int getViewerType() {
        return -1;
    }    
    protected void broadcastClusterGaggle() {

	}
    protected void broadcastSelectedClusterGaggle() {

	}
    protected void broadcastNamelistGaggle() {

    }
    public void broadcastGeneClusterToGenomeBrowser() {

    }
}
