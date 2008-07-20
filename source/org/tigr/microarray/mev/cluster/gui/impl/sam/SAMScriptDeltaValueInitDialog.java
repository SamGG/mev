/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * SAMScriptDeltaValueInitDialog.java
 *
 * Created on May 20, 2004, 2:11 PM
 */

package org.tigr.microarray.mev.cluster.gui.impl.sam;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.ParameterPanel;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.HelpWindow;

/**
 *
 * @author  braisted
 */
public class SAMScriptDeltaValueInitDialog extends AlgorithmDialog {
    
    int result = JOptionPane.CANCEL_OPTION;
    JTextField deltaField;
    JCheckBox deltaGraphCheckBox;
    JLabel deltaLabel;
    
    /** Creates a new instance of SAMScriptDeltaValueInitDialog */
    public SAMScriptDeltaValueInitDialog(JFrame parent) {
        super(parent, "SAM Script Delta Selection", true);
        Listener listener = new Listener();
        deltaLabel = new JLabel("Delta Value");
        deltaField = new JTextField("1.0", 5);
        deltaField.setPreferredSize(new Dimension(70, 20));
        deltaField.setSize(new Dimension(70, 20));
        deltaGraphCheckBox = new JCheckBox("Interact with SAM graph. (Delta Value Slider)", false);
        deltaGraphCheckBox.setOpaque(false);
        deltaGraphCheckBox.setFocusPainted(false);
        deltaGraphCheckBox.addItemListener(listener);
        ParameterPanel params = new ParameterPanel("SAM Delta Value Selection");
        params.setLayout(new GridBagLayout());
        params.add(deltaLabel, new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.EAST, GridBagConstraints.VERTICAL, new Insets(10,0,20,20), 0,0));
        params.add(deltaField, new GridBagConstraints(1,0,1,1,0,0,GridBagConstraints.WEST, GridBagConstraints.VERTICAL, new Insets(10,10,20,0), 0,0));
        params.add(deltaGraphCheckBox, new GridBagConstraints(0,1,2,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,20,0), 0,0));
        addContent(params);
        setActionListeners(listener);
        pack();
    }
    
    /**
     * Returns the selected delta value
     */
    public float getDeltaValue() {
        String value = deltaField.getText();
        return Float.parseFloat(value);
    }
    
    /**
     *
     */
    public boolean interactWithGraph() {
        return deltaGraphCheckBox.isSelected();
    }
    
    private boolean validateValue(String value) {
        float v;
        try {
            v = Float.parseFloat(value);
            if(v < 0) {
                JOptionPane.showMessageDialog(this, "Value must be greater than or equal to zero.", "Value Range Error", JOptionPane.ERROR_MESSAGE);
                deltaField.selectAll();
                deltaField.grabFocus();
                return false;
            }
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Value is not in a valid format.", "Value Format Error", JOptionPane.ERROR_MESSAGE);
            deltaField.selectAll();
            deltaField.grabFocus();
            return false;
        } catch (NullPointerException npe) {
            JOptionPane.showMessageDialog(this, "Null value entered.", "Null Value Error", JOptionPane.ERROR_MESSAGE);
            deltaField.selectAll();
            deltaField.grabFocus();
            return false;
        }
        return true;
    }
    
    
    /**
     * Resets controls to default initial settings
     */
    private void resetControls(){
        deltaField.setText("1.0");
        deltaField.setEnabled(true);
        deltaLabel.setEnabled(true);
        deltaGraphCheckBox.setSelected(false);
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
     * The class to listen to the dialog and check boxes items events.
     */
    private class Listener extends DialogListener implements ItemListener {
        
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            if (command.equals("ok-command")) {
                if(!deltaGraphCheckBox.isSelected()) {
                    if(validateValue(deltaField.getText())) {
                        result = JOptionPane.OK_OPTION;
                        dispose();
                    }
                } else {
                    result = JOptionPane.OK_OPTION;
                    dispose();
                }                
            } else if (command.equals("cancel-command")) {
                result = JOptionPane.CANCEL_OPTION;
                dispose();
            }
            else if (command.equals("reset-command")){
                resetControls();
                result = JOptionPane.CANCEL_OPTION;
                return;
            }
            else if (command.equals("info-command")){
                HelpWindow hw = new HelpWindow(SAMScriptDeltaValueInitDialog.this, "SAM Script Delta Dialog");
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
        }
        
        public void itemStateChanged(ItemEvent e) {
            deltaLabel.setEnabled(!deltaGraphCheckBox.isSelected());
            deltaField.setEnabled(!deltaGraphCheckBox.isSelected());
        }
        
        public void windowClosing(WindowEvent e) {
            result = JOptionPane.CLOSED_OPTION;
            dispose();
        }
    }
    
    public static void main(String [] args) {
        SAMScriptDeltaValueInitDialog dialog = new SAMScriptDeltaValueInitDialog(new JFrame());
        dialog.showModal();
    }
}
