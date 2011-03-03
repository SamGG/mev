/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: RelNetInitDialog.java,v $
 * $Revision: 1.3 $
 * $Date: 2005-03-10 20:39:03 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.rn;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;

import javax.swing.AbstractButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;

import org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.ParameterPanel;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.SampleSelectionPanel;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.HelpWindow;

public class RelNetInitDialog extends AlgorithmDialog {
    private int result;
    private JCheckBox usePermutationBox;
    private JTextField minThresholdField;
    private JTextField maxThresholdField;
    private JTextField entropyField;
    private JCheckBox useEntropyBox;
    private SampleSelectionPanel sampleSelectionPanel;
    
    
    /**
     * Constructs a <code>RelNetInitDialog</code> with default
     * initial parameters.
     */
    public RelNetInitDialog(Frame parent) {
        super(parent, "RN: Relevance Networks", true);
        
        Listener listener = new Listener();
        addWindowListener(listener);
        
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new GridBagLayout());
        
        //sample selection panel
        sampleSelectionPanel = new SampleSelectionPanel(Color.white, UIManager.getColor("Label.foreground"),true,"Sample Selection");
        
        //parameter panel
        ParameterPanel parameters = new ParameterPanel();
        parameters.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        
        usePermutationBox = new JCheckBox("Use Permutation Test");
        usePermutationBox.setFocusPainted(false);
        usePermutationBox.setBackground(Color.white);
        usePermutationBox.setForeground(UIManager.getColor("Label.foreground"));
        usePermutationBox.setActionCommand("permutation-command");
        usePermutationBox.addActionListener(listener);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; gbc.insets.bottom = 5;
        parameters.add(usePermutationBox, gbc);
        
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.insets.bottom = 5;
        parameters.add(new JLabel("Min Threshold:"), gbc);
        gbc.gridy = 2;
        parameters.add(new JLabel("Max Threshold:"), gbc);
        gbc.gridy = 3;
        useEntropyBox = new JCheckBox("Use Filter, %");
        useEntropyBox.setFocusPainted(false);
        useEntropyBox.setBackground(Color.white);
        useEntropyBox.setForeground(UIManager.getColor("Label.foreground"));
        useEntropyBox.setActionCommand("entropy-command");
        useEntropyBox.addActionListener(listener);
        parameters.add(useEntropyBox, gbc);
        
        minThresholdField = new JTextField(String.valueOf(0.97f), 5);
        maxThresholdField = new JTextField(String.valueOf(1f), 5);
        entropyField = new JTextField("95", 5);
        entropyField.setEnabled(false);
        gbc.insets = new Insets(0, 10, 5, 0);
        gbc.gridx = 1;
        gbc.gridy = 1;
        parameters.add(minThresholdField, gbc);
        gbc.gridy = 2;
        parameters.add(maxThresholdField, gbc);
        gbc.gridy = 3;
        parameters.add(entropyField, gbc);
        
        contentPanel.add(sampleSelectionPanel, new GridBagConstraints(0,0,1,1,1.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0));
        contentPanel.add(parameters, new GridBagConstraints(0,1,1,1,1.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0));
        
        setActionListeners(listener);
        addContent(contentPanel);
        pack();
        setResizable(false);
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
     * Returns true if entropy check box is selected.
     */
    public boolean usePermutation() {
        return usePermutationBox.isSelected();
    }
    
    /**
     * Returns min threshold value.
     */
    public float getMinThreshold() {
        return Float.parseFloat(minThresholdField.getText());
    }
    
    /**
     * Returns max threshold value.
     */
    public float getMaxThreshold() {
        return Float.parseFloat(maxThresholdField.getText());
    }
    
    /**
     * Returns true if entropy check box is selected.
     */
    public boolean useEntropy() {
        return useEntropyBox.isSelected();
    }
    
    /**
     * Returns the entropy value.
     */
    public float getEntropy() {
        if (useEntropy()) {
            return Float.parseFloat(entropyField.getText());
        }
        return 0f;
    }
    
    /**
     * Returns true if genes radio button was selected.
     */
    public boolean isClusterGenes() {
        return sampleSelectionPanel.isClusterGenesSelected();
    }
    
    /**
     * Resets defaults
     */
    private void resetControls(){
        sampleSelectionPanel.setClusterGenesSelected(true);
        useEntropyBox.setSelected(false);
        usePermutationBox.setSelected(false);
        this.entropyField.setText("95");
        this.entropyField.setEnabled(false);
        this.maxThresholdField.setText(String.valueOf(1f));
        this.minThresholdField.setText(String.valueOf(0.97f));
        this.minThresholdField.setEnabled(true);
    }
    /**
     *  Input validation
     */
    
    private boolean validInput(float min, float max, float entropy){
        boolean valid = true;
        if(!this.usePermutationBox.isSelected() && (min <= 0 || min > 1.0)){
            JOptionPane.showMessageDialog(RelNetInitDialog.this, "Min. Treshold must be > 0 and <= 1.0", "Input Error!", JOptionPane.ERROR_MESSAGE);
            this.minThresholdField.requestFocus();
            this.minThresholdField.selectAll();
            valid = false;
        }
        else if(max <= 0 || max > 1.0){
            JOptionPane.showMessageDialog(RelNetInitDialog.this, "Max. Treshold must be > 0 and <= 1.0", "Input Error!", JOptionPane.ERROR_MESSAGE);
            this.maxThresholdField.requestFocus();
            this.maxThresholdField.selectAll();
            valid = false;
        }
        else if(max <= min){
            JOptionPane.showMessageDialog(RelNetInitDialog.this, "Max. threshold must be > Min. threshold", "Input Error!", JOptionPane.ERROR_MESSAGE);
            
            valid = false;
        }
        else if(useEntropy() && (entropy <=0 || entropy > 100)){
            JOptionPane.showMessageDialog(RelNetInitDialog.this, "Entropy must be > 0 and <= 100", "Input Error!", JOptionPane.ERROR_MESSAGE);
            this.entropyField.requestFocus();
            this.entropyField.selectAll();
            valid = false;
        }
        return valid;
        
    }
    
    /**
     * The class to listen to the dialog events.
     */
    private class Listener extends DialogListener {
        
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            if (command.equals("ok-command")) {
                int progress = 0;
                float min, max, entropy = 0;
                try {
                    min = Float.parseFloat(minThresholdField.getText());
                    progress++;
                    max = Float.parseFloat(maxThresholdField.getText());
                    progress++;
                    if (useEntropy()) {
                        entropy = Float.parseFloat(entropyField.getText());
                        progress++;
                    }
                    if(validInput(min,max,entropy)){
                        result = JOptionPane.OK_OPTION;
                        dispose();
                    }
                } catch (NumberFormatException nfe) {
                    JOptionPane.showMessageDialog(RelNetInitDialog.this, "Not a number: " + nfe.getMessage(), "Input Error!", JOptionPane.ERROR_MESSAGE);
                    if(progress == 0){
                        minThresholdField.requestFocus();
                        minThresholdField.selectAll();
                    } else if(progress == 1) {
                        maxThresholdField.requestFocus();
                        maxThresholdField.selectAll();
                    } else if(progress == 2){
                        entropyField.requestFocus();
                        entropyField.selectAll();
                    }
                }
            } else if (command.equals("cancel-command")) {
                result = JOptionPane.CANCEL_OPTION;
                dispose();
            } else if (command.equals("entropy-command")) {
                entropyField.setEnabled(((AbstractButton)e.getSource()).isSelected());
            } else if (command.equals("permutation-command")) {
                minThresholdField.setEnabled(!((AbstractButton)e.getSource()).isSelected());
            }else if (command.equals("reset-command")){
                resetControls();
            }else if(command.equals("info-command")){
            	HelpWindow.launchBrowser(RelNetInitDialog.this, "RN Initialization Dialog");
            }
        }
        
        public void windowClosing(WindowEvent e) {
            result = JOptionPane.CLOSED_OPTION;
            dispose();
        }
    }
    
    public static void main(String[] args) {
        RelNetInitDialog dlg = new RelNetInitDialog(new javax.swing.JFrame());
        if (dlg.showModal() == JOptionPane.OK_OPTION) {
            System.out.println("ok");
        }
        System.exit(0);
    }
}
