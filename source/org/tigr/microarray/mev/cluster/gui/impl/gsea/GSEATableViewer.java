package org.tigr.microarray.mev.cluster.gui.impl.gsea;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.Expression;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Arrays;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import javax.swing.tree.DefaultMutableTreeNode;

import org.tigr.microarray.mev.TMEV;
import org.tigr.microarray.mev.cluster.gui.GSEAExperiment;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentUtil;
import org.tigr.microarray.mev.cluster.gui.helpers.GSEAURLLinkDialog;
import org.tigr.microarray.mev.cluster.gui.helpers.TableViewer;
import org.tigr.microarray.mev.cluster.gui.impl.GUIFactory;
import org.tigr.util.QSort;

public class GSEATableViewer extends TableViewer implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final String SAVE_PVALUES_TABLE_COMMAND ="save_pvalues_table_command";
	private static final String STORE_CLUSTER_COMMAND="store_cluster_command";
	private static final String LINK_TO_URL_COMMAND = "link-to-url-command";
	private static final String CLEAR_ALL_COMMAND = "clear-all-cmd";
	private static final String SELECT_ALL_COMMAND = "select-all-cmd";
	private static final String LAUNCH_EXPRESSION_GRAPH_COMMAND = "launch-expression-graph-command";
	private static final String LAUNCH_CENTROID_GRAPH_COMMAND = "launch-centroid-graph-command";
	private static final String LAUNCH_EXPRESSION_IMAGE_COMMAND = "launch-expression-image-command";
	private static final String LAUNCH_TEST_STATISTIC_GRAPH_COMMAND = "launch-test-statistic-graph-command";
	private static final String LAUNCH_LEADING_EDGE_STATISTIC_GRAPH_COMMAND = "launch-leading-edge-statistic-graph-command";
	 
	
	protected DefaultMutableTreeNode gseaRoot;
    protected JPopupMenu menu;
    
    protected GSEAExperiment experiment;
    protected int [][] clusters;
    protected boolean clusterAnalysis;
    protected boolean haveAccessionNumbers;
    private JMenuItem urlMenuItem;
    int xColumn;
	private int[] sortedIndices;
	private int[] indices;
    private boolean[] sortedAscending; 
    
    
    public GSEATableViewer(String[] headerNames, Object[][] data, DefaultMutableTreeNode analysisNode, GSEAExperiment experiment) {
        super(headerNames, data);
   
        indices = new int[data.length];
        this.sortedIndices = new int[data.length];
        for (int i=0; i<data.length; i++){
        	indices[i]=i;
        	sortedIndices[i]=i;
        }
        xColumn=-1;
        setNumerical(0, true);
        gseaRoot = analysisNode;
        menu = createPopupMenu();
        this.experiment = experiment;
        
        table.setRowSelectionAllowed(true);
              
        this.sortedAscending = new boolean[table.getModel().getColumnCount()];
        for (int j = 0; j < sortedAscending.length; j++) {
            sortedAscending[j] = false;
        }
        table.addMouseListener(new Listener());
        addMouseListenerToHeaderInTable(table);
        if(table.getRowCount() > 0)
            table.getSelectionModel().setSelectionInterval(0,0);
    }
    
    
    public GSEATableViewer(String [] headerNames, Object [][] data) {
   		super(headerNames, data);
    }
   
	
    public Expression getExpression(){
    	return new Expression(this, this.getClass(), "new", 
    			new Object[]{headerNames, data});
    }
   
   
    /** Creates the context menu
     * @return  */
    protected JPopupMenu createPopupMenu(){
        Listener listener = new Listener();
        JPopupMenu menu = new JPopupMenu();
        JMenuItem item;
        
        
        item = new JMenuItem("Store Selection as Cluster");
        item.setActionCommand(STORE_CLUSTER_COMMAND);
        item.addActionListener(listener);
        menu.add(item);
        
        menu.addSeparator();
        
  JMenu launchMenu = new JMenu("Open Viewer");
        
        item = new JMenuItem("Expression Image");
        item.setActionCommand(LAUNCH_EXPRESSION_IMAGE_COMMAND);
        item.addActionListener(listener);
        launchMenu.add(item);
        
        item = new JMenuItem("Centroid Graph");
        item.setActionCommand(LAUNCH_CENTROID_GRAPH_COMMAND);
        item.addActionListener(listener);
        launchMenu.add(item);
        
        item = new JMenuItem("Expression Graph");
        item.setActionCommand(LAUNCH_EXPRESSION_GRAPH_COMMAND);
        item.addActionListener(listener);
        launchMenu.add(item);
        
        item = new JMenuItem("Leading Edge Graph");
        item.setActionCommand(LAUNCH_LEADING_EDGE_STATISTIC_GRAPH_COMMAND);
        item.addActionListener(listener);
        launchMenu.add(item);
        
        item = new JMenuItem("Test Statistic Graph");
        item.setActionCommand(LAUNCH_TEST_STATISTIC_GRAPH_COMMAND);
        item.addActionListener(listener);
        launchMenu.add(item);
        
        
        menu.add(launchMenu);
        
        menu.addSeparator();
                
        item = new JMenuItem("Save pValues Table");
        item.setActionCommand(SAVE_PVALUES_TABLE_COMMAND);
        item.addActionListener(listener);
        menu.add(item);
      
        menu.addSeparator();
        
        item = new JMenuItem("Select all rows...", GUIFactory.getIcon("TableViewerResult.gif"));
        item.setActionCommand(SELECT_ALL_COMMAND);
        item.addActionListener(listener);
        menu.add(item);
               
        menu.addSeparator();
        
        item = new JMenuItem("Clear all selections...", GUIFactory.getIcon("TableViewerResult.gif"));
        item.setActionCommand(CLEAR_ALL_COMMAND);
        item.addActionListener(listener);
        menu.add(item);
        
        menu.addSeparator();
        
        urlMenuItem = new JMenuItem("Link to URL");
        urlMenuItem.setActionCommand(LINK_TO_URL_COMMAND);
        urlMenuItem.addActionListener(listener);
        menu.add(urlMenuItem);
        
        
                 
        return menu;
    }
    
    
    /** Handles opening cluster viewers.
     */
    protected void onOpenViewer(String viewerType){
        int index = this.table.getSelectedRow();
       
        if(index == -1 || gseaRoot == null)
            return; 

        //This is the node marked "Expression Viewers"
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)gseaRoot.getChildAt(3);

    
        
       index = new Integer((String)this.table.getValueAt(index, 0)) -1;
        System.out.println("1 "+index);
        if(node.getChildCount() < index) {
            return;
        }
        
        //index marks which of the expression folders to go to (Term 1: extracellular region, for example)
       node = (DefaultMutableTreeNode)(node.getChildAt(index));
       
       System.out.println("2 "+index);
        if(viewerType.equals("expression image")){
            node = (DefaultMutableTreeNode)(node.getChildAt(0));
        } else if(viewerType.equals("centroid graph")){
            node = (DefaultMutableTreeNode)(node.getChildAt(1));
        } else if(viewerType.equals("expression graph")){
            node = (DefaultMutableTreeNode)(node.getChildAt(2));
        }else if(viewerType.equals("Test statistics graph")){
            node = (DefaultMutableTreeNode)(node.getChildAt(3));
        }else if(viewerType.equals("leading edge graph")){
            node = (DefaultMutableTreeNode)(node.getChildAt(5));
        }
        
        if(framework != null)
            framework.setTreeNode(node);
    }
    
    
    
    public void onSelected(IFramework framework) {
        super.onSelected(framework);
        if(this.gseaRoot == null){
            try {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode)framework.getCurrentNode().getParent();
                Object userObject = node.getUserObject();
                if(userObject instanceof String){
                    if(((String)userObject).indexOf("GSEA") != -1) {
                        this.gseaRoot = node;
                    }
                }
            } catch (Exception e) {
                System.out.println("selection exception");
                e.printStackTrace();
            }
        }
    }
    
    private void linkToURL2() {
    	  JFrame frame = (JFrame)(JOptionPane.getFrameForComponent(table));   
    	  //System.out.println("Column selection allowed:"+table.getColumnSelectionAllowed());
    	  xColumn=table.getSelectedColumn();
    	 // System.out.println("Selected column is:"+xColumn);
          String colName = table.getColumnName(xColumn);
          String Annotation=(String)table.getValueAt(table.getSelectedRow(), xColumn);
          //System.out.println("Annotation:"+Annotation);
          File file = TMEV.getConfigurationFile("annotation_URLs.txt");
          
          GSEAURLLinkDialog adialog=new GSEAURLLinkDialog(frame, false, Annotation, table.getSelectedRow(), colName,file); 
          adialog.setVisible(true);
    	
    }
   
    
    
    /**
     *  Handles the storage of selected rows from the 
     *  table.  
     *  
     */
    protected void onStoreSelectedRows(){
        int [] tableIndices = table.getSelectedRows();
        if(tableIndices == null || tableIndices.length == 0)
            return;
        
        //convert to possibly sorted table indices
        for(int i = 0; i < tableIndices.length; i++)
            tableIndices[i] = ((DefaultViewerTableModel) model).getRow( tableIndices[i] );
        
        JFileChooser chooser = new JFileChooser(TMEV.getFile("/Data"));
        String fileName = "";
        if(chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION){
            File file = chooser.getSelectedFile();
            fileName = file.getName();
            try{
                PrintWriter pw = new PrintWriter(new FileOutputStream(file));
                int rows = tableIndices.length;
                int cols = table.getColumnCount();
                
                for(int row = 0; row < rows ; row++){
                    for(int col = 0; col < cols; col++){
                        pw.print(((String)(table.getValueAt(tableIndices[row], col))) + "\t");
                    }
                    pw.print("\n");
                }
                pw.flush();
                pw.close();
            } catch ( IOException ioe) {
                ioe.printStackTrace();
                javax.swing.JOptionPane.showMessageDialog(this, ("Error Saving Table to file: "+fileName), "Output Error", JOptionPane.WARNING_MESSAGE);
            }
            
        }
    
     }
    
    
    /** Saves the pvalues table to file
     */
    protected void onSavepValuesTable(){
        JFileChooser chooser = new JFileChooser(TMEV.getFile("/Data"));
        String fileName = "";
        if(chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION){
            File file = chooser.getSelectedFile();
            fileName = file.getName();
            try{
                PrintWriter pw = new PrintWriter(new FileOutputStream(file));
                int rows = table.getRowCount();
                int cols = table.getColumnCount();
                
                for(int row = 0; row < rows; row++){
                    for(int col = 0; col < cols; col++){
                        pw.print(((String)(table.getValueAt(row, col))) + "\t");
                    }
                    pw.print("\n");
                }
                pw.flush();
                pw.close();
            } catch ( IOException ioe) {
                ioe.printStackTrace();
                javax.swing.JOptionPane.showMessageDialog(this, ("Error Saving Table to file: "+fileName), "Output Error", JOptionPane.WARNING_MESSAGE);
            }
            
        }
    }
    
    /** Returns genes indices on selected row.
     * @param rows Selected rows
     * @return Associated indices
     */
    protected int [] getGeneIndices(int [] rows){
        int numGenes = 0;
        for(int i = 0; i < rows.length; i++)
            numGenes += clusters[rows[i]].length;
        int [] indices = new int [numGenes];
        int cnt = 0;
        for(int i = 0; i < rows.length; i++){
            for(int j = 0; j < clusters[rows[i]].length; j++){
                indices[cnt] = clusters[rows[i]][j];
                cnt++;
            }
        }
        return indices;
    }
    
    protected int [] mapExperimentIndicesToGeneData(int [] indices){
        int [] idataIndices = new int [indices.length];
        for(int i = 0; i < indices.length; i++)
            idataIndices[i] = this.experiment.getGeneIndexMappedToData(indices[i]);
        return idataIndices;
    }
    
    protected void validateMenuOptions(){
        int row = this.getSelectedRow();
        if(row < 0)
            return;
        //know that accessions exist
     //   this.launchMenuItem.setEnabled( this.table.getValueAt(row, 1) != null && !this.table.getValueAt(row, 1).equals(" ") );
    }
    
    
    /** 
     * Returns indices of current cluster.
     */
    public int[] getCluster() {
        return indices;
    }
    public int[] getSortedCluster() {
        return sortedIndices;
    }
    public void sortInOrigOrder() {
        for (int i = 0; i < getSortedCluster().length; i++) {
            sortedIndices[i] = getCluster()[i];
        }
        
        table.repaint();  
        table.clearSelection();
        for (int i = 0; i < table.getModel().getColumnCount(); i++)
            sortedAscending[i] = false;        
    }
    public void addMouseListenerToHeaderInTable(JTable table1) {
        final JTable tableView = table1;
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
                        for (int i = 0; i < table.getModel().getColumnCount(); i++)
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
            
            table.repaint();
            table.clearSelection();
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
        table.repaint();
        table.removeRowSelectionInterval(0, table.getRowCount() - 1);
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
        
        SortableField(int index, String field) {
            this.index = index;
            this.field = field;
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
     * Handles events
     */
    protected class Listener extends MouseAdapter implements ActionListener{
        
        public void actionPerformed(ActionEvent ae) {
            String command = ae.getActionCommand();
            
            
            if(command.equals(LINK_TO_URL_COMMAND)){
              linkToURL2();
            } else if(command.equals(STORE_CLUSTER_COMMAND)){
            	onStoreSelectedRows();
            } else if(command.equals(SAVE_PVALUES_TABLE_COMMAND)){
                onSavepValuesTable();
            }else if(command.equals(CLEAR_ALL_COMMAND)){
            	table.clearSelection();
            }else if(command.equals(SELECT_ALL_COMMAND)){
            	table.selectAll();
            }else if(command.equals(LAUNCH_EXPRESSION_IMAGE_COMMAND)){
                onOpenViewer("expression image");
            } else if(command.equals(LAUNCH_CENTROID_GRAPH_COMMAND)){
                onOpenViewer("centroid graph");
            } else if(command.equals(LAUNCH_EXPRESSION_GRAPH_COMMAND)){
                onOpenViewer("expression graph");
            }else if(command.equals(LAUNCH_LEADING_EDGE_STATISTIC_GRAPH_COMMAND)){
                onOpenViewer("leading edge graph");
            }else if(command.equals(LAUNCH_TEST_STATISTIC_GRAPH_COMMAND)){
                onOpenViewer("Test statistics graph");
            }
        }
        
        public void mousePressed(MouseEvent me){
            if(me.isPopupTrigger()){
            	
            	 if (table.getSelectedRowCount() != 1) {
                     urlMenuItem.setEnabled(false);
                 } else {
                	             	
                     urlMenuItem.setEnabled(true);
                 }
     	    menu.show(me.getComponent(), me.getX(), me.getY());
            }
        }
        
        public void mouseReleased(MouseEvent me){
            if(me.isPopupTrigger()){
                menu.show(me.getComponent(), me.getX(), me.getY());
            }
        }
        
    }
  
	
	
	
	
	
	
}
