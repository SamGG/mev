/*
Copyright @ 1999-2004, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * $RCSfile: SearchResultDialog.java,v $
 * $Revision: 1.2 $
 * $Date: 2005-03-10 15:44:14 $
 * $Author: braistedj $
 * $State: Exp $
 */
/*
 * SearchResultDialog.java
 *
 * Created on September 8, 2004, 3:56 PM
 */

package org.tigr.microarray.mev;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.Hashtable;
import java.util.Vector;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.ParameterPanel;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.HelpWindow;

/**
 *
 * @author  braisted
 */
public class SearchResultDialog extends AlgorithmDialog {
    
    private int result = JOptionPane.CANCEL_OPTION;
    
    private IFramework framework;
    private JTree nodeTree;
    private JList expViewNodeList;
    private JList tableViewNodeList;
    private Hashtable expViewHash;
    private Hashtable tableViewHash;
    private int [] indices;
    private boolean geneResult;
    private ResultTable resultTable;
    private boolean noViewers = false;
    private AlgorithmData searchCriteria;
    private String [] inputIds;
    private boolean isListImportResult = false;
    
    /** Creates a new instance of SearchResultDialog */
    public SearchResultDialog(IFramework framework, AlgorithmData searchCriteria, JTree resultTree, Hashtable expViewNodeHash, Hashtable tableViewNodeHash, int [] indices) {
        super(framework.getFrame(), "Search Result", false);
        this.framework = framework;
        nodeTree = resultTree;
        this.indices = indices;
        this.searchCriteria = searchCriteria;
        this.geneResult = searchCriteria.getParams().getBoolean("gene-search");
        
        TreeListener listener = new TreeListener();
        
        nodeTree.addTreeSelectionListener(listener);
        
        this.expViewHash = expViewNodeHash;
        this.tableViewHash = tableViewNodeHash;
        
        if(resultTree.getModel().getRoot() == null || ((TreeNode)resultTree.getModel().getRoot()).getChildCount() < 1)
            initElementOnlyResult();
        else {
            initFullResult();
            
            expViewNodeList.addListSelectionListener(listener);
            tableViewNodeList.addListSelectionListener(listener);
        }
        
        //use setListData to update
        
        okButton.setText("Close");
        okButton.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                result = JOptionPane.CANCEL_OPTION;
                dispose();
            }
        });
        
        infoButton.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                HelpWindow hw = new HelpWindow(SearchResultDialog.this, "Search Result Dialog");
                result = JOptionPane.CANCEL_OPTION;
                if(hw.getWindowContent()){
                    hw.setSize(450,600);
                    hw.setLocation();
                    hw.show();
                    return;
                }
                else {
                    hw.setVisible(false);
                    hw.dispose();
                    return;
                }
            }
        });
        resetButton.setVisible(false);
        cancelButton.setVisible(false);
        pack();
        setSize(550, 500);
    }
    
    
    /** Creates a new instance of SearchResultDialog */
    public SearchResultDialog(IFramework framework, AlgorithmData searchCriteria, int [] indices) {
        super(framework.getFrame(), "Search Result", false);
        this.framework = framework;
        
        this.indices = indices;
        this.geneResult = searchCriteria.getParams().getBoolean("gene-search");
        
        this.noViewers = true;
        
        initElementOnlyResult();
        
        this.expViewHash = null;
        this.tableViewHash = null;
        
        okButton.setText("Close");
        okButton.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                result = JOptionPane.CANCEL_OPTION;
                dispose();
            }
        });
        
        infoButton.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                HelpWindow hw = new HelpWindow(SearchResultDialog.this, "Search Result Dialog");
                result = JOptionPane.CANCEL_OPTION;
                if(hw.getWindowContent()){
                    hw.setSize(450,600);
                    hw.setLocation();
                    hw.show();
                    return;
                }
                else {
                    hw.setVisible(false);
                    hw.dispose();
                    return;
                }
            }
        });
        
        resetButton.setVisible(false);
        cancelButton.setVisible(false);
        pack();
        setSize(550, 400);
    }
    
    
    
    /** Creates a new SearchResultDialog for list input results
     */
    public SearchResultDialog(IFramework framework, int [] indices, String [] idList, boolean [] foundTerms, boolean geneResult) {
        super(framework.getFrame(), "Import Results", true);
        this.framework = framework;
        this.isListImportResult = true;
        this.indices = indices;
        this.inputIds = idList;
        
        this.geneResult = geneResult;
        
        this.noViewers = true;
        
        initListImportResult(idList, foundTerms);
        
        this.expViewHash = null;
        this.tableViewHash = null;
        
        //modify buttons
        resetButton.setVisible(false);
        okButton.setText("Store Cluster");
        okButton.setSize(100,30);
        okButton.setPreferredSize(new Dimension(100,30));
        
        okButton.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                result = JOptionPane.OK_OPTION;
                dispose();
            }
        });
        
        cancelButton.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                result = JOptionPane.CANCEL_OPTION;
                dispose();
            }
        });
        
        
        
        infoButton.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                HelpWindow hw = new HelpWindow(SearchResultDialog.this, "Import Result Dialog");
                hw.setTitle("Identifier List Import Result");
                result = JOptionPane.CANCEL_OPTION;
                if(hw.getWindowContent()){
                    hw.setSize(450,600);
                    hw.setLocation();
                    hw.show();
                    return;
                }
                else {
                    hw.setVisible(false);
                    hw.dispose();
                    return;
                }
            }
        });
        
        super.validate();
        
        pack();
        setSize(550, 500);
    }
    
    
    private void initFullResult() {
        
        expViewNodeList = new JList(new Vector());
        tableViewNodeList = new JList(new Vector());
        
        JScrollPane treePane = new JScrollPane(nodeTree);
        JScrollPane expViewPane = new JScrollPane(expViewNodeList);
        JScrollPane tableViewPane = new JScrollPane(tableViewNodeList);
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        mainPanel.setBackground(Color.white);
        //border?
        
        //  ParameterPanel primaryPanel = new ParameterPanel("Result Nodes");
        JPanel primaryPanel = new JPanel();
        primaryPanel.setBackground(Color.white);
        primaryPanel.setLayout(new GridBagLayout());
        primaryPanel.add(treePane, new GridBagConstraints(0,0,1,1,1,1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
        
        ParameterPanel expViewPanel = new ParameterPanel("Expression Viewers");
        expViewPanel.setLayout(new GridBagLayout());
        expViewPanel.add(expViewPane, new GridBagConstraints(0,0,1,1,1,1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
        
        ParameterPanel tableViewPanel = new ParameterPanel("Table Viewers");
        tableViewPanel.setLayout(new GridBagLayout());
        tableViewPanel.add(tableViewPane, new GridBagConstraints(0,0,1,1,1,1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
        
        if(geneResult)
            resultTable = new ResultTable("Genes Found", framework.getData(), indices, geneResult, false);
        else
            resultTable = new ResultTable("Samples Found", framework.getData(), indices, geneResult, false);
        
        ParameterPanel shortCutPanel = new ParameterPanel("Viewer Shortcuts");
        shortCutPanel.setLayout(new GridBagLayout());
        
        shortCutPanel.add(primaryPanel, new GridBagConstraints(0,0,1,2,1.0,1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
        shortCutPanel.add(expViewPanel, new GridBagConstraints(1,0,1,1,1.0,0.5, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
        shortCutPanel.add(tableViewPanel, new GridBagConstraints(1,1,1,1,1.0,0.5, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
        
        
        mainPanel.add(resultTable, new GridBagConstraints(0,0,1,1,1.0,1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
        
        mainPanel.add(shortCutPanel, new GridBagConstraints(0,1,1,1,1.0,1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10,0,0,0),0,0));
        
        addContent(mainPanel);
    }
    
    /**
     * Shows the dialog.
     */
    public int showModal() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);
        show();
        return result;
    }
    
    
    private void initListImportResult(String [] inputIDs, boolean [] foundIDs) {
        expViewNodeList = new JList(new Vector());
        tableViewNodeList = new JList(new Vector());
        
        JScrollPane treePane = new JScrollPane(nodeTree);
        JScrollPane expViewPane = new JScrollPane(expViewNodeList);
        JScrollPane tableViewPane = new JScrollPane(tableViewNodeList);
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        mainPanel.setBackground(Color.white);
        
        Vector found = new Vector();
        Vector lost = new Vector();
        
        for(int i = 0; i < foundIDs.length; i++) {
            if(foundIDs[i])
                found.addElement(inputIDs[i]);
            else
                lost.addElement(inputIDs[i]);
        }
        
        String resultDescription = "<html><center>"+String.valueOf(found.size())+" of "+String.valueOf(inputIDs.length)+" input IDs were matched.<br>"+
            String.valueOf("(List length = "+indices.length+")</center><html>");

        if(geneResult) {
            resultTable = new ResultTable("Genes Matched", framework.getData(), indices, geneResult, true);
            resultTable.setResultText(resultDescription);      
        } else {
            resultTable = new ResultTable("Samples Matched", framework.getData(), indices, geneResult, true);
            resultTable.setResultText(resultDescription);
        }

        //construct the found and not found lists
        ParameterPanel lostAndFoundPanel = new ParameterPanel("Matching Results");
        lostAndFoundPanel.setLayout(new GridBagLayout());
                
        JLabel foundLabel = new JLabel("IDs Found ("+found.size()+" of "+inputIDs.length+")");
        JScrollPane foundPane = new JScrollPane(new JList(found));
        
        JLabel lostLabel = new JLabel("IDs Not Found ("+lost.size()+" of "+inputIDs.length+")");
        JScrollPane lostPane = new JScrollPane(new JList(lost));
        
        lostAndFoundPanel.add(foundLabel, new GridBagConstraints(0,0,1,1,1,.5, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,5,0,15), 0,0));
        lostAndFoundPanel.add(lostLabel, new GridBagConstraints(1,0,1,1,1,.5, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,15,0,5), 0,0));
        lostAndFoundPanel.add(foundPane, new GridBagConstraints(0,1,1,1,1,1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,5,0,15), 0,0));
        lostAndFoundPanel.add(lostPane, new GridBagConstraints(1,1,1,1,1,1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,15,0,5), 0,0));
                
        mainPanel.add(resultTable, new GridBagConstraints(0,0,1,1,1.0,1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
        mainPanel.add(lostAndFoundPanel, new GridBagConstraints(0,1,1,1,1.0,0.5, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
        
        addContent(mainPanel);
    }
    
    private void initElementOnlyResult() {
        expViewNodeList = new JList(new Vector());
        tableViewNodeList = new JList(new Vector());
        
        JScrollPane treePane = new JScrollPane(nodeTree);
        JScrollPane expViewPane = new JScrollPane(expViewNodeList);
        JScrollPane tableViewPane = new JScrollPane(tableViewNodeList);
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        mainPanel.setBackground(Color.white);
        //border?
        
        ParameterPanel emptyViewerPanel = new ParameterPanel("Viewers");
        emptyViewerPanel.setLayout(new GridBagLayout());
        
        JLabel label;
        
        if(geneResult)
            label = new JLabel("<html><body><center>No expression or table viewers were found<br>containing the matching genes.</center></body></html>");
        else
            label = new JLabel("<html><body><center>No expression or table viewers were found<br>containing the matching samples.</center></body></html>");
        
        label.setOpaque(false);
        label.setHorizontalAlignment(JLabel.CENTER);
        emptyViewerPanel.add(label, new GridBagConstraints(0,0,1,1,1.0,1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
        
        if(geneResult)
            resultTable = new ResultTable("Genes Found", framework.getData(), indices, geneResult, false);
        else
            resultTable = new ResultTable("Samples Found", framework.getData(), indices, geneResult, false);
        
        mainPanel.add(resultTable, new GridBagConstraints(0,0,1,1,1.0,1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
        mainPanel.add(emptyViewerPanel, new GridBagConstraints(0,1,1,1,1.0,0.3, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
        
        addContent(mainPanel);
    }
    
    public static void main(String [] args) {
        JFrame frame = new JFrame();
        frame.setSize(400,400);
        frame.setVisible(true);
        
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Analysis Results");
        DefaultMutableTreeNode a1 = new DefaultMutableTreeNode("KMC - genes(1)");
        DefaultMutableTreeNode a2 = new DefaultMutableTreeNode("SOM - genes(2)");
        root.add(a1);
        root.add(a2);
        
        DefaultMutableTreeNode c1 = new DefaultMutableTreeNode("Cluster 2");
        DefaultMutableTreeNode c2 = new DefaultMutableTreeNode("Cluster 8");
        
        Vector expVector = new Vector();
        expVector.add(c1);
        expVector.add(c2);
        
        Hashtable expTable = new Hashtable();
        expTable.put(a1, expVector);
        expTable.put(a2, expVector);
        
        Hashtable tabTable = new Hashtable();
        tabTable.put(a1, expVector);
        tabTable.put(a2, expVector);
        
        
        ResultTree tree = new ResultTree(root);
        
        // SearchResultDialog d = new SearchResultDialog(frame, new AlgorithmData(), tree, expTable, tabTable);
        //d.showModal();
    }
    
    private void updateLists(DefaultMutableTreeNode node) {
        
        Vector v1 = ((Vector)expViewHash.get(node));
        if(v1 == null)
            v1= new Vector();
        
        expViewNodeList.setListData(v1);
        
        Vector v2 = ((Vector)tableViewHash.get(node));
        if(v2 ==null)
            v2 = new Vector();
        tableViewNodeList.setListData(v2);
    }
    
    private void updateViewerResults(int [] ind) {
        if(ind.length == 0) {
            JOptionPane.showMessageDialog(this, "No element indices are selected in the table as a basis for viewer update.", "Empty Element List", JOptionPane.INFORMATION_MESSAGE);
            return;
        } else if(ind.length == this.indices.length) {
            repaint();
            return;
        }
        
        ResultTree resultTree = framework.getResultTree();
        Vector result =  resultTree.findViewerCollection(ind, this.geneResult);
        
        if(result != null) {
            
            Vector primaryNodes = (Vector)(result.elementAt(0));
            Hashtable eViewHash = (Hashtable)(result.elementAt(1));
            Hashtable tViewHash = (Hashtable)(result.elementAt(2));
            
            DefaultMutableTreeNode root = new DefaultMutableTreeNode("Analysis Results");
            for(int i = 0; i < primaryNodes.size(); i++) {
                root.add((DefaultMutableTreeNode)(primaryNodes.elementAt(i)));
            }
            
            JTree tree = new JTree(root);
            
            SearchResultDialog resultDialog = new SearchResultDialog(this.framework, searchCriteria, tree, eViewHash, tViewHash, ind);
            resultDialog.showModal();            
        }
        
    }
    
    
    private void storeCluster() {
        if(geneResult)
            framework.storeOperationCluster("Search Result", "Selected Genes", this.resultTable.getSelectedIndices(), geneResult);
        else
            framework.storeOperationCluster("Search Result", "Selected Samples", this.resultTable.getSelectedIndices(), geneResult);
        
        repaint();
    }
    
    
    public int [] getSelectedIndices() {
        return resultTable.getSelectedIndices();
    }
    
    
    private class TreeListener implements TreeSelectionListener, ListSelectionListener {
        
        public void valueChanged(TreeSelectionEvent treeSelectionEvent) {
            
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)nodeTree.getLastSelectedPathComponent();
            if(node == null)
                return;
            
            updateLists(node);
        }
        
        public void valueChanged(ListSelectionEvent lse) {
            JList list = (JList)lse.getSource();
            if(list == expViewNodeList)
                tableViewNodeList.clearSelection();
            else
                expViewNodeList.clearSelection();
            
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)(list.getSelectedValue());
            if(node != null && framework != null)
                framework.setTreeNode(node);
        }
        
    }
    
    
    
    private class ResultTable extends ParameterPanel {
        
        private JTable table;
        private ResultDataModel model;
        private JLabel elementLabel;
        private JButton updateViewerListsButton;
        private JButton saveClusterButton;
        
        public ResultTable(String title, IData data, int [] indices, boolean geneResult, boolean listImportResult) {
            super(title);
            setLayout(new GridBagLayout());
            
            String [] headerNames;
            String [] fields;
            if(geneResult) {
                fields = data.getFieldNames();
                headerNames = new String[fields.length+3];
                headerNames[0] = "Selected";
                headerNames[1] = "File Index";
                headerNames[2] = "Color";
                for(int i = 0; i < fields.length; i++) {
                    headerNames[i+3] = fields[i];
                }
            } else {
                Vector slideKeys = getSlideNameKeyVectorUnion(data);
                headerNames = new String[slideKeys.size()+3];
                headerNames[0] = "Selected";
                headerNames[1] = "File Index";
                headerNames[2] = "Color";
                for(int i = 0; i < slideKeys.size(); i++) {
                    headerNames[i+3] = (String)(slideKeys.elementAt(i));
                }
            }
            
            model = new ResultDataModel(data, indices, headerNames, geneResult);
            table = new JTable(model);
            table.setCellSelectionEnabled(true);
            table.getColumn("Color").setCellRenderer(new SearchTableCellRenderer());
            
            JScrollPane pane = new JScrollPane(table);
            pane.setColumnHeaderView(table.getTableHeader());
            
            if(!listImportResult) {
                if(geneResult)
                    elementLabel = new JLabel("Number of genes matching the search: "+indices.length);
                else
                    elementLabel = new JLabel("Number of samples matching the search: "+indices.length);
            } else {
                if(geneResult)
                    elementLabel = new JLabel("<html>Number of genes matching the "+inputIds.length+" input ids : "+indices.length+"<br>(Note that replicates for an id may exist)</html>");
                else
                    elementLabel = new JLabel("<html>Number of samples matching the "+inputIds.length+" input ids : "+indices.length);
            }
            elementLabel.setBorder(BorderFactory.createLineBorder(Color.black));
            elementLabel.setOpaque(true);
            elementLabel.setBackground(Color.lightGray);
            elementLabel.setHorizontalAlignment(JLabel.CENTER);
            
            //buttons to clear, select all, and modify viewer results on subset selected.
            ButtonListener listener = new ButtonListener();
            
            JButton clearButton = new JButton("Clear");
            clearButton.setActionCommand("clear-all-command");
            clearButton.addActionListener(listener);
            clearButton.setFocusPainted(false);
            clearButton.setPreferredSize(new Dimension(80, 30));
            clearButton.setSize(80,30);
            
            JButton selectAllButton = new JButton("Select All");
            selectAllButton.setActionCommand("select-all-command");
            selectAllButton.addActionListener(listener);
            selectAllButton.setFocusPainted(false);
            selectAllButton.setPreferredSize(new Dimension(80, 30));
            selectAllButton.setSize(80,30);
            
            if(!listImportResult) {
                saveClusterButton = new JButton("Store Cluster");
                saveClusterButton.setActionCommand("store-cluster-command");
                saveClusterButton.addActionListener(listener);
                saveClusterButton.setFocusPainted(false);
                saveClusterButton.setPreferredSize(new Dimension(100, 30));
                saveClusterButton.setSize(100,30);
                
                updateViewerListsButton = new JButton("Update Shortcuts");
                updateViewerListsButton.setEnabled(false);
                updateViewerListsButton.setActionCommand("update-viewer-lists-command");
                updateViewerListsButton.addActionListener(listener);
                updateViewerListsButton.setFocusPainted(false);
                updateViewerListsButton.setPreferredSize(new Dimension(100, 30));
                updateViewerListsButton.setSize(100,30);
            }
            
            JPanel buttonPanel = new JPanel(new GridBagLayout());
            buttonPanel.setOpaque(false);
            
            if(!listImportResult) {
                buttonPanel.add(selectAllButton, new GridBagConstraints(0,0,1,1,0.5,1.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0,5,0,0), 0,0));
                buttonPanel.add(clearButton, new GridBagConstraints(1,0,1,1,0.5,1.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0,5,0,0), 0,0));                
                buttonPanel.add(saveClusterButton, new GridBagConstraints(2,0,1,1,0.5,1.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0,20,0,0), 0,0));
                buttonPanel.add(updateViewerListsButton, new GridBagConstraints(3,0,1,1,1.0,1.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0,30,0,0), 0,0));
                
                add(elementLabel, new GridBagConstraints(0,0,1,1,1.0,0.3, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,5,5,5), 0,0));
                add(pane, new GridBagConstraints(0,1,1,1,1.0,1.3, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,5,5,5), 0,0));
                add(buttonPanel, new GridBagConstraints(0,2,1,1,1.0,0.3, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,5,5,5), 0,0));

                setPreferredSize(new Dimension(450, 150));                 
                setSize(450, 150);

            } else {
                buttonPanel.add(selectAllButton, new GridBagConstraints(0,0,1,1,0.5,1.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0,25,0,15), 0,0));
                buttonPanel.add(clearButton, new GridBagConstraints(1,0,1,1,0.5,1.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0,15,0,25), 0,0));
     
                add(elementLabel, new GridBagConstraints(0,0,1,1,1.0,0.3, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,5,5,5), 0,0));                          
                add(pane, new GridBagConstraints(0,1,1,1,1.0,1.3, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,5,5,5), 0,0));
                add(buttonPanel, new GridBagConstraints(0,2,1,1,1.0,0.3, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,5,5,5), 0,0));

                setPreferredSize(new Dimension(450, 200));                 
                setSize(450, 200);
            }
            
            
        }
        
        public void setResultText(String text) {
            elementLabel.setText(text);
            validate();
        }
        
        /**
         * Returns the key vector for the sample with the longest sample name key list
         */
        public Vector getSlideNameKeyVectorUnion(IData data) {
            Vector keyVector;
            Vector fullKeyVector = new Vector();
            String key;
            for( int i = 0; i < data.getFeaturesCount(); i++) {
                keyVector = (data.getFeature(i)).getSlideDataKeys();
                for(int j = 0; j < keyVector.size(); j++) {
                    key = (String)(keyVector.elementAt(j));
                    if(!fullKeyVector.contains(key))
                        fullKeyVector.addElement(key);
                }
            }
            return fullKeyVector;
        }
        
        private int [] getSelectedIndices() {
            return model.getSelectedIndices();
        }
        
        private class ResultDataModel extends AbstractTableModel {
            private IData data;
            private int [] indices;
            private boolean [] selected;
            private String [] headerNames;
            private int columnCount, rowCount;
            private String value;
            private boolean geneResult;
            private JCheckBox checkBox;
            private JLabel colorLabel;
            
            public ResultDataModel(IData data, int [] indices, String [] headerNames, boolean geneResult) {
                this.data = data;
                this.indices = indices;
                this.headerNames = headerNames;
                this.geneResult = geneResult;
                this.checkBox = new JCheckBox();
                colorLabel = new JLabel();
                colorLabel.setOpaque(true);
                columnCount = headerNames.length;
                rowCount = indices.length;
                selected = new boolean[rowCount];
                for(int i = 0; i < selected.length; i++)
                    selected[i] = true;
            }
            
            
            
            
            public Object getValueAt(int row, int col) {
                if(col == 0) {
                    return new Boolean(selected[row]);
                } else if(col == 1) {
                    return Integer.toString(indices[row]);
                } else if(col == 2) {
                    Color color = data.getProbeColor(indices[row]);
                    if(color != null)
                        return color;
                    else
                        return Color.white;
                }
                
                if(geneResult)
                    value = data.getElementAttribute(indices[row], col-3);
                else
                    value = (String)(data.getFeature(indices[row]).getSlideDataLabels().get(headerNames[col]));
                
                if(value == null)
                    value = " ";
                return value;
            }
            
            public void setValueAt(Object value, int row, int col) {
                if(col == 0) {
                    selected[row] = ((Boolean)value).booleanValue();
                    if(!isListImportResult)
                        updateViewerListsButton.setEnabled(true);
                }
            }
            
            /** Accesses column class information.
             * @param columnIndex int column index
             * @return Returns the class associated with the column index.
             */
            public Class getColumnClass(int columnIndex) {
                if(columnIndex == 0)
                    return Boolean.class;
                else
                    return String.class;
            }
            
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return (columnIndex == 0);
            }
            
            public int getColumnCount() {
                return columnCount;
            }
            
            public int getRowCount() {
                return rowCount;
            }
            
            public String getColumnName(int col) {
                return headerNames[col];
            }
            
            public void selectAll() {
                for(int i = 0; i < selected.length; i++)
                    selected[i] = true;
                table.repaint();
            }
            
            public void clearAll() {
                for(int i = 0; i < selected.length; i++)
                    selected[i] = false;
                table.repaint();
            }
            
            public int [] getSelectedIndices() {
                Vector indices = new Vector();
                for(int i = 0; i < selected.length; i++) {
                    if(selected[i])
                        indices.add(new Integer((String)(model.getValueAt(i, 1))));
                }
                
                int [] newIndices = new int[indices.size()];
                
                for(int i = 0; i < newIndices.length; i++) {
                    newIndices[i] = ((Integer)(indices.elementAt(i))).intValue();
                }
                return newIndices;
            }
        }
        
        private class SearchTableCellRenderer implements TableCellRenderer {
            JPanel colorPanel;
            
            public SearchTableCellRenderer() {
                colorPanel = new JPanel();
                colorPanel.setBackground(Color.white);
            }
            
            public Component getTableCellRendererComponent(JTable jTable, Object obj, boolean param, boolean param3, int param4, int param5) {
                colorPanel.setBackground((Color)obj);
                return colorPanel;
            }
        }
        
        
        
        private class ButtonListener implements ActionListener {
            public void actionPerformed(ActionEvent ae) {
                String command = ae.getActionCommand();
                if(command.equals("select-all-command")) {
                    model.selectAll();
                    if(!isListImportResult)
                        updateViewerListsButton.setEnabled(true);
                } else if(command.equals("clear-all-command")) {
                    model.clearAll();
                    if(!isListImportResult)
                        updateViewerListsButton.setEnabled(false);
                } else if(command.equals("update-viewer-lists-command")) {
                    updateViewerResults(model.getSelectedIndices());
                    //updateViewerListsButton.setEnabled(false);
                } else if(command.equals("store-cluster-command")) {
                    storeCluster();
                }
                
            }
        }
    }
    
}
