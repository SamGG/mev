/*
 * ClusterTableSearchDialog.java
 *
 * Created on March 31, 2004, 1:34 PM
 */

package org.tigr.microarray.mev.cluster.gui.helpers;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.TitledBorder;

import org.tigr.microarray.mev.cluster.gui.impl.GUIFactory;

/**
 *
 * @author  nbhagaba
 */
public class ClusterTableSearchDialog extends javax.swing.JDialog {
    
    JTable table;
    String searchStr;
    //int numSearchCols;
    int numRows;
    int numCols;
    int numClasses;
    Vector foundRowIndices;
    ButtonGroup searchButtonGroup;    
    
    /** Creates a new instance of ClusterTableSearchDialog */
    public ClusterTableSearchDialog(java.awt.Frame parent, JTable table, boolean modal) {
	super(parent, modal);
	this.table = table;
	
	numRows = table.getRowCount();
	numCols = table.getColumnCount();
	//numSearchCols = numCols - 4;
	foundRowIndices = new Vector();

	initComponents();
        this.geneBarContainerLabel.setIcon(GUIFactory.getIcon("dialog_banner2.gif"));
	
	searchButtonGroup = new ButtonGroup();
	searchButtonGroup.add(this.selectAllButton);
	searchButtonGroup.add(this.selectIncrButton);

	this.setSize(300, 500);
	//this.pack();
	
	this.jComboBox1.setMaximumRowCount(10);
	
	searchStr = null;
	
	this.jComboBox1.setEditable(true);
	this.jComboBox1.insertItemAt(new String(""), 0);
	
	this.findButton.addActionListener(new ActionListener(){
	    public void actionPerformed( ActionEvent ae){
                updateRowCount();
                //numRows = table.getRowCount();
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
                updateRowCount();
                //numRows = table.getRowCount();
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
    
    private void updateRowCount() {
        numRows = table.getRowCount();
    }
    
    /** 
     * Runs search within table
     * @return Returns true if found
     */    
    private boolean runSearch(){
        
        int[] colsToSearch = colPanel.getSelectedCols();        
	
	ListSelectionModel lsm = table.getSelectionModel();
	foundRowIndices = new Vector();
	boolean selectAll = this.selectAllButton.isSelected();
	boolean found = false;
        
        table.clearSelection();
	
	searchStr = (String)this.jComboBox1.getSelectedItem();

	if(searchStr == null || colsToSearch.length == 0 || searchStr.equals(""))
	    return found;
	
	
	this.jComboBox1.insertItemAt(searchStr, 0);
	
	if( this.matchCaseChkBox.isSelected() ){
	    for(int row = 0; row < numRows; row++){
		
		for(int i = 0; i < colsToSearch.length; i++){
		    if( ((String)table.getModel().getValueAt(row, colsToSearch[i])).indexOf( searchStr ) != -1){
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
		
		for(int i = 0; i < colsToSearch.length; i++){
		    if( (((String)table.getModel().getValueAt(row, colsToSearch[i])).toUpperCase()).indexOf( upperCaseStr ) != -1){
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
        java.awt.GridBagConstraints gridBagConstraints;

        colPanel = new ColumnNamesPanel();
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

        setTitle("Table Search");
        setName("Table Search");
        //setResizable(false);
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
        
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.weighty = 80;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        getContentPane().add(colPanel, gridBagConstraints);        

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.weighty = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        getContentPane().add(jComboBox1, gridBagConstraints);

        jLabel2.setForeground(java.awt.Color.black);
        jLabel2.setText("Find what:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        getContentPane().add(jLabel2, gridBagConstraints);

        matchCaseChkBox.setText("Match Case");
        matchCaseChkBox.setFocusPainted(false);
        matchCaseChkBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                matchCaseChkBoxActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.weighty = 4;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        getContentPane().add(matchCaseChkBox, gridBagConstraints);

        selectAllButton.setSelected(true);
        selectAllButton.setText("Select All Rows Found");
        selectAllButton.setToolTipText("Select all finds at once.");
        selectAllButton.setFocusPainted(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.weighty = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        getContentPane().add(selectAllButton, gridBagConstraints);

        selectIncrButton.setText("Select Incrementally");
        selectIncrButton.setToolTipText("Move throgh finds sequentially.");
        selectIncrButton.setFocusPainted(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        getContentPane().add(selectIncrButton, gridBagConstraints);

        findButton.setText("Find");
        buttonPanel.add(findButton);

        findNextButton.setText("Find Next");
        findNextButton.setEnabled(false);
        buttonPanel.add(findNextButton);

        closeButton.setText("Close");
        buttonPanel.add(closeButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.weighty = 4;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
        getContentPane().add(buttonPanel, gridBagConstraints);

        geneBarContainerLabel.setToolTipText("The Institute for Genomic Research");
        geneBarContainerLabel.setIconTextGap(0);
        this.geneBarContainerLabel.setIcon(GUIFactory.getIcon("genebar2.gif"));

        geneBarPanel.add(geneBarContainerLabel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 4;
        getContentPane().add(geneBarPanel, gridBagConstraints);

        //pack();
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
    
    
    private class ColumnNamesPanel extends JPanel {
        JCheckBox[] columnNameBoxes;
        JButton selectAllButton, clearAllButton;
        ColumnNamesPanel() {
            ColumnNamesPanel.this.setBorder(new TitledBorder("Select fields to search"));
            ColumnNamesPanel.this.setBackground(Color.white);
            String[] columnNames = new String[table.getColumnCount() - 1];
            for (int i = 0; i < columnNames.length; i++) {
                columnNames[i] = table.getColumnName(i + 1);
            }
            
            columnNameBoxes = new JCheckBox[columnNames.length];
            for (int i= 0; i < columnNameBoxes.length; i++) {
                columnNameBoxes[i] = new JCheckBox(columnNames[i], true);
                columnNameBoxes[i].setBackground(Color.white);
            }
            
            JPanel checkBoxPanel = createCheckBoxPanel();
            JScrollPane scroll = new JScrollPane(checkBoxPanel);
            scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
            scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
            
            JButton selectAllButton = new JButton("Select All");
            JButton clearAllButton = new JButton("Clear All");
            
            selectAllButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    for (int i = 0; i < columnNameBoxes.length; i++) {
                        columnNameBoxes[i].setSelected(true);
                    }
                }
            });
            
            clearAllButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    for (int i = 0; i < columnNameBoxes.length; i++) {
                        columnNameBoxes[i].setSelected(false);
                    }
                }
            });    
            
            GridBagConstraints constraints = new GridBagConstraints();
            GridBagLayout gridbag = new GridBagLayout();
            ColumnNamesPanel.this.setLayout(gridbag);
            
            buildConstraints(constraints, 0, 0, 1, 1, 50, 10);
            gridbag.setConstraints(selectAllButton, constraints);
            ColumnNamesPanel.this.add(selectAllButton);
            
            buildConstraints(constraints, 1, 0, 1, 1, 50, 0);            
            gridbag.setConstraints(clearAllButton, constraints);
            ColumnNamesPanel.this.add(clearAllButton);    
            
            buildConstraints(constraints, 0, 1, 2, 1, 100, 90);
            constraints.fill = GridBagConstraints.BOTH;
            gridbag.setConstraints(scroll, constraints);
            ColumnNamesPanel.this.add(scroll);            
            
        }
        
        private JPanel createCheckBoxPanel() {
            JPanel panel1 = new JPanel();
            panel1.setBackground(Color.white);
            GridBagConstraints constraints = new GridBagConstraints();
            GridBagLayout gridbag = new GridBagLayout();
            panel1.setLayout(gridbag);    
            constraints.anchor = GridBagConstraints.WEST;
            for (int i = 0; i < columnNameBoxes.length; i++) {
                buildConstraints(constraints, 0, i, 1, 1, 100, 0);
                gridbag.setConstraints(columnNameBoxes[i], constraints);
                panel1.add(columnNameBoxes[i]);
            }
            return panel1;
        }
        
        public int[] getSelectedCols() {
            Vector selColsVector = new Vector();
            for (int i = 0; i < columnNameBoxes.length; i++) {
                if (columnNameBoxes[i].isSelected()) {
                    selColsVector.add(new Integer(i + 1));
                }                
            }
            
            int[] selCols = new int[selColsVector.size()];
            for (int i = 0; i < selCols.length; i++) {
                selCols[i] = ((Integer)(selColsVector.get(i))).intValue();
            }
            
            return selCols;
        }
        
        public String[] getSelectedColNames() {
            Vector selColNamesVector = new Vector();
            for (int i = 0; i < columnNameBoxes.length; i++) {
                if (columnNameBoxes[i].isSelected()) {
                    selColNamesVector.add(table.getColumnName(i + 1));
                }                
            }    
            
            String[] selColNames = new String[selColNamesVector.size()];
            for (int i = 0; i < selColNames.length; i++) {
                selColNames[i] = (String)(selColNamesVector.get(i));
            }
            
            return selColNames;
        }
        
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
    // Variables declaration - do not modify
    private javax.swing.JCheckBox matchCaseChkBox;
    private javax.swing.JRadioButton selectIncrButton;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel geneBarContainerLabel;
    private javax.swing.JButton findButton;
    private javax.swing.JPanel geneBarPanel;
    private javax.swing.JButton findNextButton;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JRadioButton selectAllButton;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JButton closeButton;
    private ColumnNamesPanel colPanel;
    // End of variables declaration    
    
    public static void main(String [] args){
        ClusterTableSearchDialog dialog = new ClusterTableSearchDialog(new java.awt.Frame(), new JTable(), false);
        dialog.show(); 
    }    
    
}
