/*
Copyright @ 1999-2004, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * EaseThresholdPanel.java
 *
 * Created on August 20, 2004, 2:40 PM
 */

package org.tigr.microarray.mev.cluster.gui.impl.ease.gotree;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;

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
public class EaseThresholdDialog extends AlgorithmDialog {
    
    private int result = JOptionPane.CANCEL_OPTION;
    private double origTOne, origTTwo;
    
    private JTextField tOneField;
    private JTextField tTwoField;
    
    /** Creates a new instance of EaseThresholdPanel */
    public EaseThresholdDialog(JFrame parent, double T1, double T2) {
        super(parent, "EASE Tree Thresholds", true);
        origTOne = T1;
        origTTwo = T2;
        ParameterPanel params = new ParameterPanel("Threshold Selection");
        params.setLayout(new GridBagLayout());
        
        JLabel label = new JLabel("Lower Threshold");        
        tOneField = new JTextField("0.01", 8);
        
        params.add(label, new GridBagConstraints(0,0,1,1,0.0,0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10,0,0,15), 0, 0));
        params.add(tOneField, new GridBagConstraints(1,0,1,1,0.0,0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10,0,0,0), 0, 0));
                
        label = new JLabel("Upper Threshold");
        tTwoField = new JTextField("0.05", 8);

        params.add(label, new GridBagConstraints(0,1,1,1,0.0,0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(15,0,10,15), 0, 0));
        params.add(tTwoField, new GridBagConstraints(1,1,1,1,0.0,0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(15,0,10,0), 0, 0));

        addContent(params);
        setActionListeners(new EventListener());
        pack();
    }
    
    
    public double getLowerThreshold() {
        return Double.parseDouble(tOneField.getText());
    }

    public double getUpperThreshold() {
        return Double.parseDouble(tTwoField.getText());
    }

    
    /** Shows the dialog.
     * @return  */
    public int showModal() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);
        show();
        return result;
    }
    
    private void resetControls() {
        this.tOneField.setText(String.valueOf(this.origTOne));
        this.tTwoField.setText(String.valueOf(this.origTTwo));
        this.tOneField.grabFocus();
        this.tOneField.selectAll();
    }
    
    private boolean validate(String a, String b) {
        double t1, t2;
        
        try {
            t1 = Double.parseDouble(a);
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Number format error, entered value is not recognized as a number", "Input Error", JOptionPane.ERROR_MESSAGE);
            this.tOneField.grabFocus();
            this.tOneField.selectAll();
            return false;
        }
         
        try {
            t2 = Double.parseDouble(b);
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Number format error, entered value is not recognized as a number", "Input Error", JOptionPane.ERROR_MESSAGE);
            this.tTwoField.grabFocus();
            this.tTwoField.selectAll();
            return false;
        }
        
        if(t1 <= 0 || t1 >= 1.0) {
            JOptionPane.showMessageDialog(this, "Threshold should be > 0 and < 1.0", "Input Error", JOptionPane.ERROR_MESSAGE);
            this.tOneField.grabFocus();
            this.tOneField.selectAll();
            return false;     
        }
        
        if(t2 <= 0 || t2 >= 1.0) {
            JOptionPane.showMessageDialog(this, "Threshold should be > 0 and < 1.0", "Input Error", JOptionPane.ERROR_MESSAGE);
            this.tTwoField.grabFocus();
            this.tTwoField.selectAll();
            return false;     
        }
        
        if(t1 >= t2) {
            JOptionPane.showMessageDialog(this, "The lower threshold should be less that the upper threshold.", "Input Error", JOptionPane.ERROR_MESSAGE);
            this.tOneField.grabFocus();
            this.tOneField.selectAll();
            return false;            
        }
        
        return true;
    }
        
    
    /**
     * The class to listen to the dialog and check boxes items events.
     */
    private class EventListener extends DialogListener {
        
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
    
            if (command.equals("ok-command")) {
                if(validate(tOneField.getText(), tTwoField.getText())) {
                    result = JOptionPane.OK_OPTION;                    
                      dispose();
                }
            } else if (command.equals("cancel-command")) {
                result = JOptionPane.CANCEL_OPTION;
                dispose();
            } else if (command.equals("reset-command")){
                resetControls();
                result = JOptionPane.CANCEL_OPTION;
                return;
            } else if (command.equals("info-command")){
                HelpWindow hw = new HelpWindow(EaseThresholdDialog.this, "EASE Threshold Dialog");
                result = JOptionPane.CANCEL_OPTION;
                if(hw.getWindowContent()){
                    hw.setSize(450,600);
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
    
    
    
 /*   public static void main(String [] args) {
        EaseThresholdDialog dialog = new EaseThresholdDialog();
        dialog.showModal();
    }   
  */ 
}
