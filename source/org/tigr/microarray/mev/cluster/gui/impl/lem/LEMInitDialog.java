/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * Created on Apr 6, 2005
 */
package org.tigr.microarray.mev.cluster.gui.impl.lem;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.ParameterPanel;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.HelpWindow;

/**
 * @author braisted
 * 
 * LEMInitDialog collects parameters for the LEM algorithm.
 */
public class LEMInitDialog extends AlgorithmDialog {

	private int result = JOptionPane.CANCEL_OPTION;
	
	private JCheckBox fileInput;
	private JCheckBox multipleChr;
	
	private JComboBox locusBox;
	private JComboBox startBox;
	private JComboBox endBox;
	private JComboBox chrBox;
	private JLabel chrLabel;
	private JLabel startLabel;
	private JLabel endLabel;

	private JLabel coorInfoLabel;
	
	/**
	 * Constructor
	 * @param parent parent component
	 * @param fieldNames annotation field names
	 */
	public LEMInitDialog(Frame parent, String [] fieldNames) {
		super(parent, "Linear Expression Map", true);
		EventListener listener = new EventListener();
		
		//Identifier selection
		JLabel locusLabel = new JLabel("Select Locus Identifier Field");
		locusBox = new JComboBox(fieldNames);
		
		ParameterPanel geneIDPanel = new ParameterPanel("Locus Identifier Selection");
		geneIDPanel.setLayout(new GridBagLayout());
		geneIDPanel.add(locusLabel, new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10,0,15,0),0,0));
		//geneIDPanel.add(locusLabel, new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
		geneIDPanel.add(locusBox, new GridBagConstraints(1,0,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10,15,15,0),0,0));
		
		
		//coordinate data input
		fileInput = new JCheckBox("Use Coordinate File", false);		
		fileInput.setOpaque(false);
		fileInput.setFocusPainted(false);
		fileInput.setHorizontalAlignment(JCheckBox.CENTER);
		fileInput.setActionCommand("use-coord-file-command");
		fileInput.addActionListener(listener);
		JLabel fileInputLabel = new JLabel("<html><body>(Hit the \"i\" button (lower left corner) for <b>File Format Information</b>)</body></html>)");
		fileInputLabel.setHorizontalAlignment(JLabel.CENTER);
		
		String text = "<html>Coordinate information can be supplied via a file input or<br>"
			+"via information in the annotation loaded into MeV.  If using a coordinates<br>"
			+"file please refer to the help page by hitting the information button (lower left).<br>"
			+"If using coordinate information in the MeV annoation please indicate the appropriate<br>"
			+"annotation fields for coordinate parameters.<html>";
		
		JLabel textLabel = new JLabel(text);		
		
		multipleChr = new JCheckBox("Multiple Chromosomes or Plasmids", false);
		multipleChr.setOpaque(false);
		multipleChr.setFocusPainted(false);
		multipleChr.setHorizontalAlignment(JCheckBox.CENTER);
		multipleChr.setActionCommand("multiple-chr-command");
		multipleChr.addActionListener(listener);
		
		chrLabel = new JLabel("Select Chromosome ID Field");
		chrLabel.setEnabled(false);
		chrLabel.setHorizontalAlignment(JLabel.CENTER);
						
		chrBox = new JComboBox(fieldNames);
		chrBox.setEnabled(false);

		startLabel = new JLabel("Select Start Coordinate (5' End) Field");	
		startBox = new JComboBox(fieldNames);
		
		endLabel = new JLabel("Select End Coordinate (3' End) Field");		
		endBox = new JComboBox(fieldNames);
				
		ParameterPanel coordPanel = new ParameterPanel("Coordinate Data Selections");
		coordPanel.setLayout(new GridBagLayout());
		
		coordPanel.add(fileInput, new GridBagConstraints(0,0,2,1,1,1,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10,0,5,0),0,0));
		coordPanel.add(fileInputLabel, new GridBagConstraints(0,1,2,1,1,1,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,10,0),0,0));
		JSeparator sep = new JSeparator(JSeparator.HORIZONTAL);
		sep.setPreferredSize(new Dimension(300,2));
		sep.setSize(100,2);
		coordPanel.add(sep, new GridBagConstraints(0,2,2,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0,0,0,0),0,0));

		//coordPanel.add(textLabel, new GridBagConstraints(0,3,2,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));

		coordPanel.add(multipleChr, new GridBagConstraints(0,3,2,1,1,1,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10,0,10,0),0,0));
	
		coordPanel.add(chrLabel, new GridBagConstraints(0,4,1,1,1,0,GridBagConstraints.EAST, GridBagConstraints.BOTH, new Insets(0,40,15,0),0,0));
		coordPanel.add(chrBox, new GridBagConstraints(1,4,1,1,0,0,GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0,0,15,40),0,0));

		JSeparator sep1 = new JSeparator(JSeparator.HORIZONTAL);
		sep1.setPreferredSize(new Dimension(300,2));
		sep1.setSize(100,2);
		coordPanel.add(sep1, new GridBagConstraints(0,5,2,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0,0,0,0),0,0));

		
		coordPanel.add(startLabel, new GridBagConstraints(0,6,1,1,1,0,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(20,40,0,10),0,0));
		coordPanel.add(startBox, new GridBagConstraints(1,6,1,1,0,0,GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(20,0,0,40),0,0));

		coordPanel.add(endLabel, new GridBagConstraints(0,7,1,1,1,0,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(10,40,25,10),0,0));
		coordPanel.add(endBox, new GridBagConstraints(1,7,1,1,0,0,GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(10,0,25,40),0,0));

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new GridBagLayout());
		mainPanel.setBackground(Color.white);

		mainPanel.add(geneIDPanel, new GridBagConstraints(0,0,1,1,1,1,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
		mainPanel.add(coordPanel, new GridBagConstraints(0,1,1,1,1,1,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
				
		addContent(mainPanel);
		setActionListeners(listener);
		
		pack();	
	}
	
    /** Shows the dialog.
     * @return  */
    public int showModal() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);
        show();
        return result;
    }
    
    /**
     * Returns the locus field name
     */
    public String getLocusField() {
    	return (String)(this.locusBox.getSelectedItem());    
    }
    
    /**
     * Return if a coord. file should be used
     * @return
     */
    public boolean useFileInput() {
    	return this.fileInput.isSelected();    
    }
    
    /**
     * Returns true if there are multiple chr. or plasmids 
     * @return true for multiple chromosomes
     */    
    public boolean hasMultipleChr() {
    	return this.multipleChr.isSelected();    	
    }

    /**
     * Returns the chr field name
     * @return chr field name
     */
    public String getChrIDField() {
    	return (String)(this.chrBox.getSelectedItem());
    }
    
    /**
     * Returns the start coord field name
     * @return coord start field name
     */
    public String getStartField() {
    	return (String)(this.startBox.getSelectedItem());        	        
    }

    /**
     * Returns the end coord field name
     * @return coord end field name
     */    
    public String getEndField() {
    	return (String)(this.endBox.getSelectedItem());        	        
    }

    /**
     * Updates the dialog for file input selection
     */
	private void fireUseFileInputState() {
		boolean enable = !this.fileInput.isSelected();
		if(enable && this.multipleChr.isSelected()) {
			this.chrLabel.setEnabled(true);
			this.chrBox.setEnabled(true);
		} else if(!enable) {
			this.chrLabel.setEnabled(false);
			this.chrBox.setEnabled(false);						
		}
		this.startLabel.setEnabled(enable);
		this.startBox.setEnabled(enable);		
		this.endLabel.setEnabled(enable);
		this.endBox.setEnabled(enable);			
	}
	
	/**
	 * Updates the dialog for multiple chr selection
	 */
	private void fireMultipleChrState() {
		boolean enable = this.multipleChr.isSelected();
		if(this.fileInput.isSelected()) {
			this.chrLabel.setEnabled(false);
			this.chrBox.setEnabled(false);		
		} else {
			this.chrLabel.setEnabled(enable);
			this.chrBox.setEnabled(enable);				
		}
	}

	/**
	 * Resets the dialog controls
	 */
	private void resetControls() {
		this.locusBox.setSelectedIndex(0);
		this.fileInput.setSelected(false);
		fireUseFileInputState();
		this.chrBox.setSelectedIndex(0);
		this.multipleChr.setSelected(false);
		fireMultipleChrState();
		this.startBox.setSelectedIndex(0);
		this.endBox.setSelectedIndex(0);
	}

    /**
     * The class to listen to the dialog and check boxes items events.
     */
    private class EventListener extends DialogListener implements ItemListener {
        
    	/**
    	 * handles action events
    	 */
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            if(command.equals("ok-command")) {
            	result = JOptionPane.OK_OPTION;
            	dispose();
            } else if (command.equals("cancel-command")) {
            	result = JOptionPane.CANCEL_OPTION;
            	dispose();
            } else if (command.equals("reset-command")) {
            	resetControls();
            } else if(command.equals("use-coord-file-command")) {
            	fireUseFileInputState();
            } else if (command.equals("multiple-chr-command")) {
            	fireMultipleChrState();
            } else if (command.equals("info-command")) {                        
                HelpWindow hw = new HelpWindow(LEMInitDialog.this, "LEM Initialization Dialog");
                result = JOptionPane.CANCEL_OPTION;
                if(hw.getWindowContent()){
                    hw.setSize(600,600);
                    hw.setLocation();
                    hw.show();
                }
                else {
                    hw.setVisible(false);
                    hw.dispose();
                }
            }
        }
        
        public void itemStateChanged(ItemEvent e) {
            //okButton.setEnabled(genes_box.isSelected() || cluster_box.isSelected());
        }
        
        public void windowClosing(WindowEvent e) {
            result = JOptionPane.CLOSED_OPTION;
            dispose();
        }
    }

    /* test launch */
	public static void main(String [] args) {
		String [] fields = {"Locus", "Chr#", "Start", "End"};
		
		LEMInitDialog dialog = new LEMInitDialog(new Frame(), fields);
		dialog.showModal();
		
	}
}
