package org.tigr.microarray.mev.cluster.gui.impl.GSEA;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.Expression;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;

import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.tree.DefaultMutableTreeNode;

import org.tigr.microarray.mev.TMEV;
import org.tigr.microarray.mev.cluster.clusterUtil.ClusterRepository;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.GSEAExperiment;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.helpers.TableViewer;
import org.tigr.microarray.mev.cluster.gui.helpers.TableViewer.DefaultViewerTableModel;
import org.tigr.microarray.mev.cluster.gui.impl.GUIFactory;

public class GSEATableViewer extends TableViewer implements Serializable {

	private static final String SAVE_GSEA_TABLE_COMMAND = "save-gsea-table-command";
	private static final String SAVE_PVALUES_TABLE_COMMAND ="save_pvalues_table_command";
	private static final String STORE_CLUSTER_COMMAND="store_cluster_command";
	protected static final String LAUNCH_EXPRESSION_IMAGE_COMMAND = "launch-expression-image-command";
	
	protected DefaultMutableTreeNode gseaRoot;
    protected JPopupMenu menu;
    
    protected GSEAExperiment experiment;
    protected int [][] clusters;
    protected String [] headerNames;
    protected boolean clusterAnalysis;
    protected boolean haveAccessionNumbers;
    protected JMenuItem launchMenuItem;
    protected Object[][] data;
    
    
    public GSEATableViewer(String[] headerNames, Object[][] data, DefaultMutableTreeNode analysisNode, GSEAExperiment experiment) {
        super(headerNames, data);
        this.headerNames = headerNames;
        this.data = data;
        
        setNumerical(0, true);
        gseaRoot = analysisNode;
        menu = createPopupMenu();
        this.experiment = experiment;
        this.clusters = clusters;
        table.addMouseListener(new Listener());
        if(table.getRowCount() > 0)
            table.getSelectionModel().setSelectionInterval(0,0);
    }
    
    
    public GSEATableViewer(String [] headerNames, Object [][] data) {
   		super(headerNames, data);
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
      
        
        item = new JMenuItem("Save pValues Table");
        item.setActionCommand(SAVE_PVALUES_TABLE_COMMAND);
        item.addActionListener(listener);
        menu.add(item);
      
        
        JMenu launchMenu = new JMenu("Open Viewer");
        
        item = new JMenuItem("Expression Image");
        item.setActionCommand(LAUNCH_EXPRESSION_IMAGE_COMMAND);
        item.addActionListener(listener);
        launchMenu.add(item);
        
     /*   item = new JMenuItem("Centroid Graph");
        item.setActionCommand(LAUNCH_CENTROID_GRAPH_COMMAND);
        item.addActionListener(listener);
        launchMenu.add(item);
        
        item = new JMenuItem("Expression Graph");
        item.setActionCommand(LAUNCH_EXPRESSION_GRAPH_COMMAND);
        item.addActionListener(listener);
        launchMenu.add(item);*/
        
        menu.add(launchMenu);
        
                 
        return menu;
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
    
    /** Handles opening cluster viewers.
     */
    protected void onOpenViewer(String viewerType){
        int index = getSelectedRow();
        
        if(index == -1 || gseaRoot == null)
            return;
        
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)gseaRoot.getChildAt(1);
        if(node.getChildCount() < index)
            return;
        node = (DefaultMutableTreeNode)(node.getChildAt(index));
        
        if(viewerType.equals("expression image")){
            node = (DefaultMutableTreeNode)(node.getChildAt(0));
        } /*else if(viewerType.equals("centroid graph")){
            node = (DefaultMutableTreeNode)(node.getChildAt(1));
        } else if(viewerType.equals("expression graph")){
            node = (DefaultMutableTreeNode)(node.getChildAt(2));
        }*/
        
        if(framework != null)
            framework.setTreeNode(node);
    }
    
    
    /**
     *  Handles storage of clusters from selected line.
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
    
    
    /** Saves the ease table to file
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
        this.launchMenuItem.setEnabled( this.table.getValueAt(row, 1) != null && !this.table.getValueAt(row, 1).equals(" ") );
    }
    
    /**
     * @inheritDoc
     */
    public Expression getExpression(){
    	return new Expression(this, this.getClass(), "new", 
    			new Object[]{this.headerNames, this.data, this.gseaRoot, this.experiment, this.clusters, this.haveAccessionNumbers, this.clusterAnalysis});
    }  
    
    /** Handles events
     */
    protected class Listener extends MouseAdapter implements ActionListener{
        
        public void actionPerformed(ActionEvent ae) {
            String command = ae.getActionCommand();
            if(command.equals(LAUNCH_EXPRESSION_IMAGE_COMMAND)){
                onOpenViewer("expression image");
            } else if(command.equals(STORE_CLUSTER_COMMAND)){
            	onStoreSelectedRows();
            } else if(command.equals(SAVE_PVALUES_TABLE_COMMAND)){
                onSavepValuesTable();
            }
        }
        
        public void mousePressed(MouseEvent me){
            if(me.isPopupTrigger()){
                if(launchMenuItem != null)
                    validateMenuOptions();
                menu.show(me.getComponent(), me.getX(), me.getY());
            }
        }
        
        public void mouseReleased(MouseEvent me){
            if(me.isPopupTrigger()){
                if(launchMenuItem != null)
                    validateMenuOptions();
                menu.show(me.getComponent(), me.getX(), me.getY());
            }
        }
        
    }
  
	
	
	
	
	
	
}
