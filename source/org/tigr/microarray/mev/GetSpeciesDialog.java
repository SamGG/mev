/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * AnalysisSaveDialog.java
 *
 * Created on January 29, 2004, 4:06 PM
 */

package org.tigr.microarray.mev;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;

/**
 *
 * @author  braisted
 */
public class GetSpeciesDialog extends org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog {

    public static final String SPECIES_NAME_KEY = "speciesname";
    String result = "unknown";
    JCheckBox saveSpeciesName;    
    JLabel speciesNameLabel;
    JTextField speciesNameTextField;
    
    /** Creates a new instance of AnalysisSaveDialog */
    public GetSpeciesDialog(JFrame frame, String currentName) {
        super(frame, "Change Species Name", true);
        Listener listener = new Listener();
        
        speciesNameLabel = new JLabel("Please provide a species name: ");

	    speciesNameTextField = new JTextField(currentName, 10);
        
        JPanel buttonPanel = new JPanel(new GridBagLayout());
        JButton okButton = new JButton("Ok");
        okButton.setFocusPainted(true);
        okButton.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        okButton.setPreferredSize(new Dimension(60, 30));
        okButton.setActionCommand("yes");
        okButton.addActionListener(listener);
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setFocusPainted(false);
        cancelButton.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        cancelButton.setPreferredSize(new Dimension(60, 30));
        cancelButton.setActionCommand("no");
        cancelButton.addActionListener(listener);
        
        buttonPanel.add(okButton, new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,20), 0,0));
        buttonPanel.add(cancelButton, new GridBagConstraints(1,0,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,20,0,0), 0,0));
        
        JPanel paramPanel = new JPanel(new GridBagLayout());
        paramPanel.setBackground(Color.white);
        paramPanel.setBorder(BorderFactory.createLineBorder(Color.black));
        JLabel promptLabel = new JLabel("Gaggle needs to broadcast a species name for the selected data.");
        promptLabel.setHorizontalAlignment(JLabel.CENTER);
        
        saveSpeciesName = new JCheckBox("Save this species name for the session.", true);
        saveSpeciesName.setBackground(Color.white);
        saveSpeciesName.setFocusPainted(false);
        //															 x y w h wxwy	anchor					fill
        paramPanel.add(promptLabel, new GridBagConstraints			(0,0,1,1,0,0,GridBagConstraints.WEST,GridBagConstraints.BOTH, new Insets(20,0,0,0), 0, 0));
        paramPanel.add(speciesNameLabel, new GridBagConstraints		(0,1,1,1,0,0,GridBagConstraints.WEST,GridBagConstraints.BOTH, new Insets(20,0,0,0), 0, 0));
        paramPanel.add(speciesNameTextField, new GridBagConstraints	(0,2,1,1,0,0,GridBagConstraints.WEST,GridBagConstraints.BOTH, new Insets(20,0,0,0), 0, 0));
        
        paramPanel.add(saveSpeciesName, new GridBagConstraints		(0,3,1,1,0,0,GridBagConstraints.WEST,GridBagConstraints.BOTH, new Insets(20,0,0,0), 0, 0));
        
        this.supplantButtonPanel(buttonPanel);
        this.addContent(paramPanel);
        
        this.setSize(480, 260);
    }
    
    
    public String showModal(){
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);        
        this.show();        
        return result;
    }
    
    public void disposeDialog() { this.dispose(); }
    
    public boolean saveSpeciesName() {
        return this.saveSpeciesName.isSelected();
    }
    
    public class Listener implements ActionListener {
        public void actionPerformed(ActionEvent ae) {
            String command = ae.getActionCommand();
            if(command.equals("yes"))
            	result = speciesNameTextField.getText();
            disposeDialog();
        }
        
    }
    
    
    public static void main(String [] args) {
        GetSpeciesDialog dialog = new GetSpeciesDialog(new JFrame(), "previous species name");
        String result = dialog.showModal();
        System.out.println("result = " + result);
        System.out.println("save species name = " + dialog.saveSpeciesName());
        System.exit(0);
    }
    
}
