/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * GDMMemberSelectionDialog.java
 *
 * Created on October 6, 2003, 3:57 PM
 */

package org.tigr.microarray.mev.cluster.gui.impl.gdm;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog;

public class GDMMemberSelectionDialog extends AlgorithmDialog {
    
    JTextField kField;
    JCheckBox allBox;
    JPanel selectPanel;
    JLabel label1;
    JLabel label2;
    JLabel label3;
    int k;
    
    int result = JOptionPane.CANCEL_OPTION;
    
    /** Creates a new instance of GDMMemberSelectionDialog */
    public GDMMemberSelectionDialog(JFrame parent, int num_elements) {
        super(parent, "Select number of neighbors",true);
        k = num_elements;
        
        Listener listener = new Listener();
        
        allBox = new JCheckBox("Save all neighbors", true);
        allBox.setBackground(Color.white);
        allBox.setFocusPainted(false);
        allBox.setActionCommand("save-all-command");
        allBox.setHorizontalAlignment(JCheckBox.CENTER);
        allBox.addActionListener(listener);
        
        kField = new JTextField("20", 6);
        kField.setEnabled(false);
        label1 = new JLabel("Save the nearest ");
        label1.setEnabled(false);
        label2 = new JLabel(" neighbors");
        label2.setEnabled(false);
        label3 = new JLabel("( There are "+num_elements+" elements in the matrix. )");
        label3.setEnabled(false);
        
        selectPanel = new JPanel(new GridBagLayout());
        selectPanel.setBackground(Color.white);
        selectPanel.add(label1, new GridBagConstraints(0,0,1,1,0.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0));
        selectPanel.add(kField, new GridBagConstraints(1,0,1,1,0.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0));
        selectPanel.add(label2, new GridBagConstraints(2,0,1,1,0.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0));
        selectPanel.add(label3, new GridBagConstraints(0,1,3,1,0.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(5,0,0,0),0,0));
        
        JPanel parameters = new JPanel(new GridBagLayout());
        parameters.setBackground(Color.white);
        parameters.setBorder(BorderFactory.createLineBorder(Color.black));
        parameters.add(allBox, new GridBagConstraints(0,0,1,1,0.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(10,0,15,0),0,0));
        parameters.add(selectPanel, new GridBagConstraints(0,1,1,1,0.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,0,10,0),0,0));
        
        addContent(parameters);
        setActionListeners(listener);
        pack();
    }
    
    private void resetControls(){
        label1.setEnabled(false);
        label2.setEnabled(false);
        label3.setEnabled(false);
        kField.setText("20");
        allBox.setSelected(true);
    }
    
    private void setControls(boolean setting){
        label1.setEnabled(setting);
        label2.setEnabled(setting);
        label3.setEnabled(setting);
        kField.setEnabled(setting);
    }
    
    public int getK(){
        if(allBox.isSelected())
            return k;
        return Integer.parseInt(kField.getText());
    }
    
    private boolean validateInput(){
        int n;
        try{
            n = Integer.parseInt(kField.getText());
            if(n < 0){
                JOptionPane.showMessageDialog(this, "Input must be > 0", "Input Warning", JOptionPane.WARNING_MESSAGE);
                return false;
            }
            if(n > k){
                JOptionPane.showMessageDialog(this, "Input must be <= the number of matrix elements ("+k+")", "Input Warning", JOptionPane.WARNING_MESSAGE);
                return false;
            }
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Invalid number format.", "Input Warning", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
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
    
    private class Listener implements ActionListener{
        
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            String command = evt.getActionCommand();
            if (command.equals("ok-command")) {
                if(!allBox.isSelected()){
                    if(validateInput()){
                        result = JOptionPane.OK_OPTION;
                    } else {
                        return;
                    }
                } else {
                    result = JOptionPane.OK_OPTION;
                }
            } else if (command.equals("cancel-command")) {
                result = JOptionPane.CANCEL_OPTION;
                dispose();
            } else if (command.equals("reset-command")){
                resetControls();
                result = JOptionPane.CANCEL_OPTION;
                return;
            }
            else if (command.equals("info-command")){
              /*  HelpWindow hw = new HelpWindow(HCLInitDialog.this, "HCL Initialization Dialog");
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
               */
            } else if(command.equals("save-all-command")){
                setControls(!allBox.isSelected());
                return;
            }
            dispose();
        }
        
    }
    
    public static void main(String [] args){
        GDMMemberSelectionDialog d = new GDMMemberSelectionDialog(new JFrame(), 1080);
        d.showModal();
        System.out.println("save "+d.getK()+" neighbors");
    }
    
}
