/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
* $RCSfile: GDMInitDialog.java,v $
* $Revision: 1.1 $
* $Date: 2004-02-06 22:53:43 $
* $Author: braisted $
* $State: Exp $
*/
package org.tigr.microarray.mev.cluster.gui.impl.gdm;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import org.tigr.microarray.mev.cluster.gui.impl.GUIFactory;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.ParameterPanel;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.HelpWindow;

public class GDMInitDialog extends AlgorithmDialog {
    private int result;

    private JRadioButton genRadio;
    private JRadioButton expRadio;
    private JTextField textField;
    private JLabel displayLabel;

  
    /**
     * Constructs a <code>GDMInitDialog</code> with default
     * initial parameters.
     */
    public GDMInitDialog(Frame parent, boolean useGenes) {
        super(new JFrame(), "Gene Distance Matrix Initialization", true);
      
        Listener listener = new Listener();
        addWindowListener(listener);

        ParameterPanel parameters = new ParameterPanel();
        parameters.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;

        gbc.insets = new Insets(0, 0, 0, 0);
        genRadio = new JRadioButton("Genes");
        genRadio.setBackground(Color.white);
        genRadio.setFocusPainted(false);
        gbc.gridx = 0; gbc.gridy = 4;
        parameters.add(genRadio, gbc);

        expRadio = new JRadioButton("Experiments");
        expRadio.setBackground(Color.white);
        expRadio.setFocusPainted(false);
        gbc.gridx = 1; gbc.gridy = 4;
        parameters.add(expRadio, gbc);

        ButtonGroup bg = new ButtonGroup();
        bg.add(genRadio);
        bg.add(expRadio);
        
        genRadio.setActionCommand("gene-radio-command");
        genRadio.addActionListener(listener);
        expRadio.setActionCommand("gene-radio-command");
        expRadio.addActionListener(listener);        
        genRadio.setEnabled(useGenes);
        genRadio.setSelected(useGenes);
        expRadio.setEnabled(!useGenes);
        expRadio.setSelected(!useGenes);

        gbc.gridx = 0; gbc.gridy = 10;
        displayLabel = new JLabel("  Display Every    ");
	parameters.add(displayLabel, gbc);	

	gbc.gridx = 1; gbc.gridy = 10;
	textField = new JTextField(String.valueOf(1), 7);
	parameters.add(textField, gbc);

        this.addContent(parameters);
        this.setActionListeners(listener);
        pack();
    }

    /**
     * Constructs a <code>GDMInitDialog</code> with default
     * initial parameters.
     */
    public GDMInitDialog(Frame parent) {
        super(new JFrame(), "Gene Distance Matrix Initialization", true);

        Listener listener = new Listener();
        addWindowListener(listener);

        ParameterPanel parameters = new ParameterPanel();
        parameters.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;

        gbc.insets = new Insets(0, 0, 0, 0);
        genRadio = new JRadioButton("Genes");
        genRadio.setBackground(Color.white);
        genRadio.setFocusPainted(false);
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.insets = new Insets(0, 0, 25, 20);
        parameters.add(genRadio, gbc);

        expRadio = new JRadioButton("Experiments");
        expRadio.setBackground(Color.white);
        expRadio.setFocusPainted(false);
        gbc.gridx = 1; gbc.gridy = 4;
        gbc.insets = new Insets(0, 20, 25, 0);
        parameters.add(expRadio, gbc);

        ButtonGroup bg = new ButtonGroup();
        bg.add(genRadio);
        bg.add(expRadio);
        genRadio.setSelected(true);
        genRadio.setActionCommand("gene-radio-command");
        genRadio.addActionListener(listener);
        expRadio.setActionCommand("gene-radio-command");
        expRadio.addActionListener(listener);

        
        gbc.gridx = 0; gbc.gridy = 10;
        displayLabel = new JLabel("  Display Every    ");
	parameters.add( displayLabel, gbc);	

	gbc.gridx = 1; gbc.gridy = 10;
	textField = new JTextField(String.valueOf(1), 7);
	parameters.add(textField, gbc);

        this.addContent(parameters);
        this.setActionListeners(listener);
        pack();
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

    /**
     * Returns true if genes radio button was selected.
     */
    public boolean isUseGenes() {
        return genRadio.isSelected();
    }

    /**
     * Set genes radio button 
     */
    public void setGeneSelected(boolean selected) {
        genRadio.setSelected(selected);
    }

    /**
     * Set experiment radio button 
     */
    public void setExpSelected(boolean selected) {
        expRadio.setSelected(selected);
    }

    public int getDisplayInterval () {
        return Integer.parseInt(textField.getText());
    }

    
    public static void main (String [] args){
        GDMInitDialog dialog = new GDMInitDialog(new java.awt.Frame());
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
       dialog.showModal();
    }
    /**
     * The class to listen to the dialog events.
     */
    private class Listener extends DialogListener {

        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            if (command.equals("gene-radio-command")) {
                displayLabel.setEnabled(genRadio.isSelected());
                textField.setEnabled(genRadio.isSelected());                    
            } else if (command.equals("ok-command")) {
                try {
                    Integer.parseInt(textField.getText());
                    result = JOptionPane.OK_OPTION;  
                } catch (Exception exception) {
                    result = JOptionPane.CANCEL_OPTION;
                }
                dispose();
            } else if (command.equals("cancel-command")) {
                result = JOptionPane.CANCEL_OPTION;
                dispose();
            } else if (command.equals("reset-command")) {
                if(GDMInitDialog.this.genRadio.isEnabled() && GDMInitDialog.this.expRadio.isEnabled())
                    GDMInitDialog.this.genRadio.setSelected(true);                    
                GDMInitDialog.this.textField.setText("1");
            } else if (command.equals("info-command")) {
                HelpWindow hw = new HelpWindow(GDMInitDialog.this, "GDM Initialization Dialog");
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