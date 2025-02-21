/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: EASETableViewer.java,v $
 * $Revision: 1.11 $
 * $Date: 2007-11-29 16:30:12 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
/*
 * EASETableViewer.java
 *
 * Created on October 8, 2003, 2:02 PM
 */

package org.tigr.microarray.mev.cluster.gui.impl.ease;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.Expression;
import java.beans.PersistenceDelegate;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Vector;

import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.tree.DefaultMutableTreeNode;

import org.tigr.microarray.mev.TMEV;
import org.tigr.microarray.mev.cluster.ClusterWrapper;
import org.tigr.microarray.mev.cluster.clusterUtil.ClusterRepository;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.helpers.TableViewer;
import org.tigr.microarray.mev.cluster.gui.impl.GUIFactory;
import org.tigr.microarray.mev.persistence.EASETableViewerPersistenceDelegate;
import org.tigr.util.BrowserLauncher;

/** Displays ease results
 */
public class EASETableViewer extends TableViewer implements Serializable {
    
    private static final String LAUNCH_BROWSER_COMMAND = "launch-browser-command";
	private static final String SAVE_EASE_TABLE_COMMAND = "save-ease-table-command";
	private static final String LAUNCH_EXPRESSION_GRAPH_COMMAND = "launch-expression-graph-command";
	protected static final String LAUNCH_CENTROID_GRAPH_COMMAND = "launch-centroid-graph-command";
	protected static final String LAUNCH_EXPRESSION_IMAGE_COMMAND = "launch-expression-image-command";
	protected static final String STORE_CLUSTER_COMMAND = "store-cluster-command";
	protected DefaultMutableTreeNode easeRoot;
    protected JPopupMenu menu;
    
    protected Experiment experiment;
    protected int [][] clusters;
    protected String [] headerNames;
    protected boolean clusterAnalysis;
    protected boolean haveAccessionNumbers;
    protected JMenuItem launchMenuItem;
    protected Object[][] data;
    protected boolean isEaseConsolidatedResult = false;

    /**
     * Kept for state-saving backwards-compatibility
     * @param headerNames
     * @param data
     * @param analysisNode
     * @param experiment
     * @param clusters
     * @param haveAccessionNumbers
     * @param clusterAnalysis
     */
    public EASETableViewer(String [] headerNames, Object [][] data, DefaultMutableTreeNode analysisNode, Experiment experiment, ClusterWrapper clusters, boolean haveAccessionNumbers, boolean clusterAnalysis, boolean isEaseConsolidatedResult) {
    	this(headerNames, data, analysisNode, experiment, clusters.getClusters(), haveAccessionNumbers, clusterAnalysis, isEaseConsolidatedResult);
    }
    
    /**
     * Kept for state-saving backwards-compatibility
     * @param headerNames
     * @param data
     * @param analysisNode
     * @param experiment
     * @param clusters
     * @param haveAccessionNumbers
     * @param clusterAnalysis
     */
    public EASETableViewer(String [] headerNames, Object [][] data, DefaultMutableTreeNode analysisNode, Experiment experiment, int [][] clusters, boolean haveAccessionNumbers, boolean clusterAnalysis) {
    	this(headerNames, data, analysisNode, experiment, clusters, haveAccessionNumbers, clusterAnalysis, false);
    }
    /** Creates a new instance of EASETableViewer
     * @param headerNames Header names
     * @param data Primary data structure
     * @param analysisNode EASE analysis node.  This permits references to cluster viewers.
     * @param experiment The <CODE>Experiment</CODE> object encapsultes index mapping to <CODE>IData</CODE>
     * @param clusters Cluster indices
     * @param haveAccessionNumbers True if acc. numbers are appended
     * @param clusterAnalysis true if result is cluter analysis, else result is a survey
     * @param isEaseConsolidatedResult true if this is a summary table for nEASE results - slightly different class. 
     */
    public EASETableViewer(String [] headerNames, Object [][] data, DefaultMutableTreeNode analysisNode, Experiment experiment, int [][] clusters, boolean haveAccessionNumbers, boolean clusterAnalysis, boolean isEaseConsolidatedResult) {
        super(headerNames, data);
        this.headerNames = headerNames;
        this.clusterAnalysis = clusterAnalysis;
        this.haveAccessionNumbers = haveAccessionNumbers;
        this.data = data;
        
        setNumerical(0, true);
        if(clusterAnalysis){
            if(!haveAccessionNumbers){
                setNumerical(3, true);
            }
            //set the rest to numerical for proper sorting
            for(int i = 4; i < headerNames.length; i++)
                setNumerical(i, true);
        } else {
            if(!haveAccessionNumbers){
                setNumerical(3, true);
            } else {
                setNumerical(5, true);
            }
            setNumerical(4, true);
        }
        this.isEaseConsolidatedResult = isEaseConsolidatedResult;
        if(this.isEaseConsolidatedResult) {
        	if(haveAccessionNumbers) {
        		setNumerical(12, true);
        	} else {
        		setNumerical(8, true);
        	}
        	setNumerical(9, true);
        	setNumerical(10, true);
			setNumerical(11, true);
        }       
        easeRoot = analysisNode;
        menu = createPopupMenu();
        this.experiment = experiment;
        this.clusters = clusters;
        table.addMouseListener(new Listener());
        if(table.getRowCount() > 0)
            table.getSelectionModel().setSelectionInterval(0,0);
    }
    
    
    public EASETableViewer(String [] headerNames, Object [][] data) {
   		super(headerNames, data);
    }
    
    /** Creats the context menu
     * @return  */
    protected JPopupMenu createPopupMenu(){
        Listener listener = new Listener();
        JPopupMenu menu = new JPopupMenu();
        JMenuItem item;
        
        
        item = new JMenuItem("Store Selection as Cluster");
        item.setActionCommand(STORE_CLUSTER_COMMAND);
        item.addActionListener(listener);
        menu.add(item);
        
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
        
        menu.add(launchMenu);
        
        menu.addSeparator();
        
        /*Correctly sets the pop-up panel label depending on what table is being built*/
        if(isEaseConsolidatedResult){
        	item = new JMenuItem("Save Nested EASE Summary Table");
        }else{
        	if(checkHeaderFor("nEASE Gene Enrich.")){
        		item = new JMenuItem("Save Nested EASE Table");
        	}else{
        		item = new JMenuItem("Save EASE Table");
        	}
        }
        
        item.setActionCommand(SAVE_EASE_TABLE_COMMAND);
        item.addActionListener(listener);
        menu.add(item);
        
        
        menu.addSeparator();
        
        item = new JMenuItem("Broadcast Selection as Matrix to Gaggle", GUIFactory.getIcon("gaggle_icon_16.gif"));
        item.setActionCommand(BROADCAST_MATRIX_GAGGLE_CMD);
        item.addActionListener(listener);
        menu.add(item);
        
        item = new JMenuItem("Broadcast Selection as Gene List to Gaggle", GUIFactory.getIcon("gaggle_icon_16.gif"));
        item.setActionCommand(BROADCAST_NAMELIST_GAGGLE_CMD);
        item.addActionListener(listener);
        menu.add(item);
        
        menu.addSeparator();
        
        if(this.haveAccessionNumbers){
            this.launchMenuItem = new JMenuItem("Open Web Page");
            this.launchMenuItem.setActionCommand(LAUNCH_BROWSER_COMMAND);
            this.launchMenuItem.addActionListener(listener);
            menu.add(this.launchMenuItem);
        }
        
        return menu;
    }
    
    /**
     * @param targetHeader table header to be found
     * @return flag indicating whether a table header was found
     * */
    public boolean checkHeaderFor(String targetHeader) {
    	for(int i=0;i<headerNames.length;i++){
    		if(targetHeader.equalsIgnoreCase(headerNames[i])){
    			return true;
    		}    		
    	}
    	return false;
	}

	public void onSelected(IFramework framework) {
        super.onSelected(framework);
        if(this.easeRoot == null){
            try {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode)framework.getCurrentNode().getParent();
                Object userObject = node.getUserObject();
                if(userObject instanceof String){
                    if(((String)userObject).indexOf("EASE") != -1) {
                        this.easeRoot = node;
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
        int index = this.table.getSelectedRow();

        if(index == -1 || easeRoot == null)
            return; 

        //This is the node marked "Expression Viewers"
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)easeRoot.getChildAt(1);
        
    	//Jump to nEASE sub-result for term that matches summary results
        if(isEaseConsolidatedResult) {
        	int fileindex = 1;
        	int termindex = 14;
        	String term = (String) this.table.getValueAt(index, fileindex) + ": " + (String) this.table.getValueAt(index, termindex);
        	int neaseindex = 0;
        	for(int i=1; i<easeRoot.getChildCount(); i++) {
        		if(((DefaultMutableTreeNode)easeRoot.getChildAt(i)).getUserObject().toString().endsWith(term)) {
        			neaseindex = i;
        			break;
        		}
        	}

        	DefaultMutableTreeNode neasenode = (DefaultMutableTreeNode)easeRoot.getChildAt(neaseindex);
        	node = (DefaultMutableTreeNode)neasenode.getChildAt(1);//Expression Viewers folder within nease result number neaseindex	
        }
        
        index = new Integer((String)this.table.getValueAt(index, 0)) -1;
        
        if(node.getChildCount() < index) {
            return;
        }
        
        //index marks which of the expression folders to go to (Term 1: extracellular region, for example)
        node = (DefaultMutableTreeNode)(node.getChildAt(index));
        if(viewerType.equals("expression image")){
            node = (DefaultMutableTreeNode)(node.getChildAt(0));
        } else if(viewerType.equals("centroid graph")){
            node = (DefaultMutableTreeNode)(node.getChildAt(1));
        } else if(viewerType.equals("expression graph")){
            node = (DefaultMutableTreeNode)(node.getChildAt(2));
        }
        
        if(framework != null)
            framework.setTreeNode(node);
    }
    
    
    /** Handles storage of clusters from selected line.
     */
    protected void onStoreCluster(){
        int [] tableIndices = table.getSelectedRows();
        if(tableIndices == null || tableIndices.length == 0)
            return;
        //convert to possibly sorted table indices
        for(int i = 0; i < tableIndices.length; i++)
            tableIndices[i] = ((DefaultViewerTableModel) model).getRow( tableIndices[i] );
        
        int [] geneIndices = getGeneIndices(tableIndices);
        geneIndices = mapExperimentIndicesToIData(geneIndices);
        //storing as sub-cluster allows storing various sets of indices as separate clusters from
        //the same viewer.
        framework.storeSubCluster(geneIndices, experiment, ClusterRepository.GENE_CLUSTER);
    }
    
    /** Handles opening browser on accessions.
     */
    protected void onOpenBrowser(){
        int [] tableIndices = table.getSelectedRows();
        if( tableIndices == null || tableIndices.length < 1)
            return;
        String file = (String) this.table.getValueAt(tableIndices[0], 1);
        String acc = (String) this.table.getValueAt(tableIndices[0], 2);
        
        if(acc == null || acc.equals("") || acc.equals(" ")){
            JOptionPane.showMessageDialog(this.framework.getFrame(), "No accession exists for this entry.", "Web access denial", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        String url = EASEURLFactory.constructURL(file,acc);
        if(url == null)
            return;
        try{
            BrowserLauncher.openURL(url);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }
    
    /** Saves the ease table to file
     */
    protected void onSaveEaseTable(){
        JFileChooser chooser = new JFileChooser(TMEV.getDataPath());
        String fileName = "";
        if(chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION){
            File file = chooser.getSelectedFile();
            fileName = file.getName();
            try{
                PrintWriter pw = new PrintWriter(new FileOutputStream(file));
                int rows = table.getRowCount();
                int cols = table.getColumnCount();

                for(String headerName : headerNames) {
                	pw.print(headerName + "\t");
                }
                pw.print("\n");
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
    
    protected int [] mapExperimentIndicesToIData(int [] indices){
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
    public Expression getExpression(PrintWriter pw, String filename){
    	writeData(pw);
    	return new Expression(this, this.getClass(), "new", 
    			new Object[]{this.headerNames, this.easeRoot, this.experiment, ClusterWrapper.wrapClusters(this.clusters), this.haveAccessionNumbers, this.clusterAnalysis, this.isEaseConsolidatedResult, filename});
    }  
    public static PersistenceDelegate getPersistenceDelegate(){
    	return new EASETableViewerPersistenceDelegate();
    }
    public void writeData(PrintWriter pw) {
    	if(data != null)
    	for(int i=0; i<data.length; i++) {
    		for(int j=0; j<data[i].length; j++) {
    			pw.print(data[i][j]);
    			if(j == data[i].length-1) {
    				pw.print("\n");
    			} else {
    				pw.print("\t");
    			}
    		}
    	}
    }

	public static Object[][] readData(BufferedReader br) {
    	Vector<String[]> datatemp = new Vector<String[]>();
    	try {
	    	while(br.ready()) {
	    		datatemp.add(br.readLine().split("\t"));
	    	}
	    	return datatemp.toArray(new Object[datatemp.size()][]);
    	} catch (IOException ioe) {
    		System.out.println("Unable to read EASETableViewer data.");
    		return null;
    	}
    }
	
    /** Handles events
     */
    protected class Listener extends MouseAdapter implements ActionListener{
        
        public void actionPerformed(ActionEvent ae) {
            String command = ae.getActionCommand();
            if(command.equals(STORE_CLUSTER_COMMAND)){
                onStoreCluster();
            } else if(command.equals(LAUNCH_EXPRESSION_IMAGE_COMMAND)){
                onOpenViewer("expression image");
            } else if(command.equals(LAUNCH_CENTROID_GRAPH_COMMAND)){
                onOpenViewer("centroid graph");
            } else if(command.equals(LAUNCH_EXPRESSION_GRAPH_COMMAND)){
                onOpenViewer("expression graph");
            } else if(command.equals(LAUNCH_BROWSER_COMMAND)){
                onOpenBrowser();
            } else if(command.equals(SAVE_EASE_TABLE_COMMAND)){
                onSaveEaseTable();
            } else if (command.equals(BROADCAST_MATRIX_GAGGLE_CMD)) {
                broadcastClusterGaggle();
            } else if (command.equals(BROADCAST_NAMELIST_GAGGLE_CMD)) {
                broadcastNamelistGaggle();
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
  
    protected void broadcastClusterGaggle() {
    	framework.broadcastGeneCluster(framework.getData().getExperiment(), getGeneIndices(table.getSelectedRows()), null);
	}
    protected void broadcastNamelistGaggle() {
    	framework.broadcastNamelist(framework.getData().getExperiment(), getGeneIndices(table.getSelectedRows()));
    }

    
}
