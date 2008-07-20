/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * EaseFileUpdateDialog.java
 *
 * Created on January 19, 2005, 4:25 PM
 */
package org.tigr.microarray.mev.cluster.gui.impl.bn;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.HelpWindow;
/**
 *
 * 
 * Constructs controls to sepecify BN file system to update
 * High level divisions are represneted on different tabs
 * On each tab there are two dropdonw menus.  One for upper level
 * folder, often species, and one to specify files
 * 
 * The constructor takes a Vector of Hashtables that have server information
 * but more importantly they contain content to popultate dialog controls
 */
public class BNFileUpdateDialog extends AlgorithmDialog {
    private JTabbedPane pane;    
    private int result = JOptionPane.CANCEL_OPTION;        
    private Vector repositoryHashes;
  
    public BNFileUpdateDialog(JFrame parent, Vector repositoryPropertyHashes) {
        super(parent, "BN File Update Selection", true);
        pane = new JTabbedPane();
        Listener listener = new Listener();
        //add tabbed panes
        addTabs(pane, repositoryPropertyHashes, listener); 
        addContent(pane);
        setActionListeners(listener);
        pack();
    }
    
    /**
     * Adds a tabbed pane for each HashTable in the propVector
     * 
     * @param pane Object to which panes should be added
     * @param propVector Vector of Hashtables (repository properties)
     * @param listener  Listener
     */
    public void addTabs(JTabbedPane pane, Vector propVector, Listener listener) {    	
    	int numTabs = propVector.size();
    	
    	for(int i = 0; i < numTabs; i++) {
    		addNewTab(pane, i, (Hashtable)propVector.get(i), listener);
    	}
    }
    
    
    /**
     * Extracts properties from the properties Hashtable to construct the tab
     * @param pane The tabbed pane to receive the tab
     * @param tabIndex the tab index
     * @param props Hashtable of properties
     * @param listener ActionListener for constructed controls
     */
    public void addNewTab(JTabbedPane pane, int tabIndex, Hashtable props, Listener listener) {
    	//label for the tab
    	String tabLabel = (String)props.get("tab-label");
    	//vector of level labels 
    	String directoryLabel = (String)props.get("level-1-label");
    	String fileLabel = (String)props.get("level-2-label");
    	
    	//vector of upper level menu items keys
    	Vector mainKeys = (Vector)props.get("main-keys");
    	
    	Hashtable itemHash = (Hashtable)props.get("menu-hash");
    	
    	//list of all hashtables
    	//repositoryHashes.add(itemHash);
    	
    	//box contains the directories
    	JComboBox box1 = new JComboBox(mainKeys);
    	//trigger to update lower list from hash of arrays or species
    	box1.setActionCommand("upper-level-selection");
    	box1.addActionListener(listener);
    	//box will contain files for a given drectory after update
    	JComboBox box2 = new JComboBox();    	
    	//trigger just a new lower level selection
    	box2.setActionCommand("lower-level-selection");
    	box2.addActionListener(listener);
    	
    	//make a TabPanel given some labels, JComboBoxes, and a properties hash
    	pane.addTab(tabLabel, new TabPanel(directoryLabel,box1,fileLabel,box2, itemHash, props));        
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
    
    
    
    private void resetControls() {
    	//we can implement this but probably not needed
    	//maybe switch to tab 0?
    }
    /**
     * Returns the selected species (upper level or folder name)
     * @return species or folder names
     */
    public String getSpeciesName() {    	
     	TabPanel panel = (TabPanel) (pane.getSelectedComponent());
     	return (String)panel.getBox1().getSelectedItem();
    }
    /**
     * Returns the selected file name (these are usually specified by array)
     * @return array of file name
     */
    public String getArrayName() {
     	TabPanel panel = (TabPanel) (pane.getSelectedComponent());
     	return (String)panel.getBox2().getSelectedItem();
    }    
    
    /**
     * Returns the index of the desired repository
     * @return
     */
    public int getRepositoryIndex() {
    	//pane's seletected tab index
    	return pane.getSelectedIndex();
    }
    
    /**
     * Returns the properties for the seleted repository
     * @return repository properties for file retrieval
     */
    public Hashtable getRepositoryProperties() {
     	TabPanel panel = (TabPanel) (pane.getSelectedComponent());
    	return panel.getRepositoryProperties();
    }
    
    /**
     * This class holds the controls for one tab and a repository hash of
     * properties
     * 
     * @author braisted
     *
     * TODO To change the template for this generated type comment go to
     * Window - Preferences - Java - Code Style - Code Templates
     */
    private class TabPanel extends JPanel {
    	//directory and file lists
    	private JComboBox box1;
    	private JComboBox box2;
    	//this gets updated by selection in box1
    	private JLabel box2JLabel;
    	 // base text for box 2's label
    	private String box2Label; 
    	// updates box two's entries    	 
    	private Hashtable box1ToBox2Hash;    	
    	//hashtable of repository properties    	 
    	private Hashtable repositoryProperties;
    	
    	/**
    	 * Constructs a new TabPanel
    	 * @param box1Label label for box 1
    	 * @param b1 box 1
    	 * @param box2Label label for box 2
    	 * @param b2 box 2
    	 * @param menuHash hashtable to map directories in box 1 to a Vector of files
    	 * 				   to populate box 2
    	 * @param repProps  prepository properties for these files and tab
    	 */
    	TabPanel(String box1Label, JComboBox b1, String box2Label, JComboBox b2, Hashtable menuHash, Hashtable repProps) {
        	setLayout(new GridBagLayout());
        	setBackground(Color.white);
        	box1 = b1;
        	box2 = b2;
        	box1ToBox2Hash = menuHash; 
        	this.box2Label = box2Label;
        	repositoryProperties = repProps;
        	
        	JLabel label = new JLabel(box1Label);
        	add(label, new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(20,0,5,0),0,0));
        	add(box1, new GridBagConstraints(0,1,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,20,0),0,0));        	  
        	box2JLabel = new JLabel(box2Label);
        	add(box2JLabel, new GridBagConstraints(0,2,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,5,0),0,0));
        	add(box2, new GridBagConstraints(0,3,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,20,0),0,0));        	          	        	
    	
        	updateBox2();
    	}
    	
    	/**
    	 * Return the upper level selection (Directory)
    	 * @return selected directory
    	 */
    	public JComboBox getBox1() {
    		return box1;
    	}
    	/**
    	 * Returns selected file
    	 * @return selected file
    	 */
    	public JComboBox getBox2() {
    		return box2;
    	}
    	/**
    	 * Repository props for the selection
    	 * @return props 
    	 */
    	public Hashtable getRepositoryProperties() {
    		return repositoryProperties;
    	}
    	
    	/**
    	 * Updates file box base on directory box selection
    	 */
    	public void updateBox2() {
       		box2.removeAllItems();
    		String key = (String)box1.getSelectedItem();    		
    		Vector box2Items = (Vector)(this.box1ToBox2Hash.get(key));
    		for(int i = 0; i < box2Items.size(); i++)
                box2.addItem(box2Items.elementAt(i));
    		box2JLabel.setText(box2Label+" "+key);
    	}
    }
    
    
        /**
     * The class to listen to the dialog events.
     */
    private class Listener extends DialogListener {
        
        public void actionPerformed(ActionEvent e) {
            Object source = e.getSource();
            String command = e.getActionCommand();
            if(command.equals("upper-level-selection")) {
            	//get source
            	TabPanel panel = (TabPanel) (pane.getSelectedComponent());
            	panel.updateBox2();
            } else if(command.equals("lower-level-selection")) {
            	//no need to act on this at this time
            } else if (source == okButton) {
                result = JOptionPane.OK_OPTION;
                dispose();
            } else if (source == cancelButton) {
                result = JOptionPane.CANCEL_OPTION;
                dispose();
            } else if (source == resetButton) {
                resetControls();
            } else if (source == infoButton){
                HelpWindow hw = new HelpWindow(BNFileUpdateDialog.this, "BN File Update Dialog");
                if(hw.getWindowContent()){
                    hw.setSize(450,650);
                    hw.setLocation();
                    hw.show();
                }
                else {
                    hw.setVisible(false);
                    hw.dispose();
                }                    
            }            
        }
        
        public void windowClosing(WindowEvent e) {
            result = JOptionPane.CLOSED_OPTION;
            dispose();
        }
    }
}
