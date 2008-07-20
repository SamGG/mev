/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * DAMSearchDialog.java
 *
 */

package org.tigr.microarray.mev.cluster.gui.impl.dam;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import org.tigr.microarray.mev.cluster.gui.impl.GUIFactory;


public class DAMSearchDialog extends javax.swing.JDialog {
    
    JTable table;
    String searchStr;
    int numSearchCols;
    int numRows;
    int numCols;
    int numClasses;
    Vector foundRowIndices;
    ButtonGroup searchButtonGroup;
    
    /** Creates new form DAMSearchDialog
     * @param parent parent frame
     * @param table parent table
     * @param modal modal selection
     */
    public DAMSearchDialog(java.awt.Frame parent, JTable table, int numClasses, boolean modal) {
	super(parent, modal);
	this.table = table;
	this.numClasses = numClasses;
	numRows = table.getRowCount();
	numCols = table.getColumnCount();
	numSearchCols = numCols - (numClasses + 2);
	foundRowIndices = new Vector();

	initComponents();
        this.geneBarContainerLabel.setIcon(GUIFactory.getIcon("dialog_banner2.gif"));
	
	searchButtonGroup = new ButtonGroup();
	searchButtonGroup.add(this.selectAllButton);
	searchButtonGroup.add(this.selectIncrButton);

	this.setSize( 250, 200 );
	this.pack();
	
	this.jComboBox1.setMaximumRowCount(10);
	
	searchStr = null;
	
	this.jComboBox1.setEditable(true);
	this.jComboBox1.insertItemAt(new String(""), 0);
	
	this.findButton.addActionListener(new ActionListener(){
	    public void actionPerformed( ActionEvent ae){
		findNextButton.setEnabled(false);
		runSearch();
		if(foundRowIndices.size() > 0 && selectIncrButton.isSelected())
		    findNextButton.setEnabled(true);
		else
		    findNextButton.setEnabled(false);
	    }
	});
	
	this.findNextButton.addActionListener(new ActionListener(){
	    public void actionPerformed(ActionEvent ae){
		findNext();
	    }
	});
	
	this.closeButton.addActionListener(new ActionListener(){
	    public void actionPerformed( ActionEvent ae){
		foundRowIndices.removeAllElements();
		findNextButton.setEnabled(false);
		selectAllButton.setSelected(true);
		matchCaseChkBox.setSelected(true);
		setVisible(false);
	    }
	});
	
	this.jComboBox1.addFocusListener(new FocusListener(){
	    public void focusGained(FocusEvent fe){
		findNextButton.setEnabled(false);
		findNextButton.repaint();
		foundRowIndices.removeAllElements();
	    }
	    
	    public void focusLost(FocusEvent fe){
		
	    }
	});
	
    }
    
    
    /** 
     * Runs search within table
     * @return Returns true if found
     */    
    private boolean runSearch(){
	
	ListSelectionModel lsm = table.getSelectionModel();
	foundRowIndices = new Vector();
	boolean selectAll = this.selectAllButton.isSelected();
	boolean found = false;
	
	searchStr = (String)this.jComboBox1.getSelectedItem();
	if(searchStr == null || numSearchCols == 0 || searchStr.equals(""))
	    return found;
	
	table.clearSelection();
	this.jComboBox1.insertItemAt(searchStr, 0);
	
	if( this.matchCaseChkBox.isSelected() ){
	    for(int row = 0; row < numRows; row++){
		
		for(int col = (numClasses + 2); col < numCols; col++){
		    if( ((String)table.getValueAt(row, col)).indexOf( searchStr ) != -1){
			//select row;
			if(selectAll || !found){
			    table.addRowSelectionInterval(row, row);
			    //first occurance
			    if(!found){
				table.scrollRectToVisible(table.getCellRect(row, 0, true));
				found = true;
			    }
			}
			else
			    foundRowIndices.add(new Integer(row));
			break;
		    }
		}
	    }
	}
	
	else{
	    
	    String upperCaseStr = searchStr.toUpperCase();
	    
	    for(int row = 0; row < numRows; row++){
		
		for(int col = (numClasses + 2); col < numCols; col++){
		    if( (((String)table.getValueAt(row, col)).toUpperCase()).indexOf( upperCaseStr ) != -1){
			//select row;
			if(selectAll || !found){
			    table.addRowSelectionInterval(row, row);
			    //first occurance
			    if(!found){
				table.scrollRectToVisible(table.getCellRect(row, 0, true));
				found = true;
			    }
			}
			else
			    foundRowIndices.add(new Integer(row));
			break;
		    }
		}
	    }
	}
	
	return found;
    }
    
    
    
    /** 
     * Selects next row in table
     */    
    public void findNext(){
	
	if(foundRowIndices.isEmpty())
	    return;
	
	int row = ((Integer)foundRowIndices.remove(0)).intValue(); //take off first element
	table.clearSelection();
	table.addRowSelectionInterval(row,row);
	table.scrollRectToVisible(table.getCellRect(row, 0, true));
	if(foundRowIndices.size() == 0)
	    findNextButton.setEnabled(false);
	
    }
    
    
    
    /** This method is called from within the constructor to
     * initialize the dialog.
     */
    private void initComponents() {
        buttonGroup1 = new javax.swing.ButtonGroup();
        jComboBox1 = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        matchCaseChkBox = new javax.swing.JCheckBox();
        selectAllButton = new javax.swing.JRadioButton();
        selectIncrButton = new javax.swing.JRadioButton();
        buttonPanel = new javax.swing.JPanel();
        findButton = new javax.swing.JButton();
        findNextButton = new javax.swing.JButton();
        closeButton = new javax.swing.JButton();
        geneBarPanel = new javax.swing.JPanel();
        geneBarContainerLabel = new javax.swing.JLabel();
        
        
        getContentPane().setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gridBagConstraints1;
        
        setTitle("Search");
        setName("Search");
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });
        
        jComboBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox1ActionPerformed(evt);
            }
        });
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 1;
        gridBagConstraints1.gridy = 1;
        gridBagConstraints1.gridwidth = 2;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints1.insets = new java.awt.Insets(10, 10, 10, 10);
        getContentPane().add(jComboBox1, gridBagConstraints1);
        
        jLabel2.setText("Find what:");
        jLabel2.setForeground(java.awt.Color.black);
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 1;
        gridBagConstraints1.insets = new java.awt.Insets(10, 10, 10, 10);
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.EAST;
        getContentPane().add(jLabel2, gridBagConstraints1);
        
        matchCaseChkBox.setText("Match Case");
        matchCaseChkBox.setFocusPainted(false);
        matchCaseChkBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                matchCaseChkBoxActionPerformed(evt);
            }
        });
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 1;
        gridBagConstraints1.gridy = 3;
        gridBagConstraints1.insets = new java.awt.Insets(0, 0, 0, 10);
        getContentPane().add(matchCaseChkBox, gridBagConstraints1);
        
        selectAllButton.setToolTipText("Select all finds at once.");
        selectAllButton.setSelected(true);
        selectAllButton.setText("Select All Rows Found");
        selectAllButton.setFocusPainted(false);
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 2;
        gridBagConstraints1.gridy = 2;
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        getContentPane().add(selectAllButton, gridBagConstraints1);
        
        selectIncrButton.setToolTipText("Move throgh finds sequentially.");
        selectIncrButton.setText("Select Incrementally");
        selectIncrButton.setFocusPainted(false);
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 2;
        gridBagConstraints1.gridy = 3;
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        getContentPane().add(selectIncrButton, gridBagConstraints1);
        
        findButton.setText("Find");
        buttonPanel.add(findButton);
        
        findNextButton.setText("Find Next");
        findNextButton.setEnabled(false);
        buttonPanel.add(findNextButton);
        
        closeButton.setText("Close");
        buttonPanel.add(closeButton);
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 1;
        gridBagConstraints1.gridy = 4;
        gridBagConstraints1.gridwidth = 2;
        gridBagConstraints1.insets = new java.awt.Insets(10, 0, 0, 0);
        getContentPane().add(buttonPanel, gridBagConstraints1);
        
        geneBarContainerLabel.setToolTipText("The Institute for Genomic Research");
        geneBarContainerLabel.setIconTextGap(0);
        this.geneBarContainerLabel.setIcon(GUIFactory.getIcon("genebar2.gif"));
        
        geneBarPanel.add(geneBarContainerLabel);
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 0;
        gridBagConstraints1.gridwidth = 3;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints1.weightx = 1.0;
        getContentPane().add(geneBarPanel, gridBagConstraints1);
        
        pack();
    }
    
    private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {
	foundRowIndices.removeAllElements();
	findNextButton.setEnabled(false);
    }
    
    /** Handles checkbox event
     */    
    private void matchCaseChkBoxActionPerformed(java.awt.event.ActionEvent evt) {
	// Add your handling code here:
    }
    
    /** Closes the dialog */
    private void closeDialog(java.awt.event.WindowEvent evt) {
	setVisible(false);
    }
    
    
    
    // Variables declaration - do not modify
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JCheckBox matchCaseChkBox;
    private javax.swing.JRadioButton selectAllButton;
    private javax.swing.JRadioButton selectIncrButton;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton findButton;
    private javax.swing.JButton findNextButton;
    private javax.swing.JButton closeButton;
    private javax.swing.JPanel geneBarPanel;
    private javax.swing.JLabel geneBarContainerLabel;
    // End of variables declaration
    
    public static void main(String [] args){
        //DAMSearchDialog dialog = new DAMSearchDialog(new java.awt.Frame(), new JTable(), numClasses, false);
        //dialog.show(); 
    }
    
}

