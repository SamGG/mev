/*
 * VarianceFilterDialog.java
 *
 * Created on January 17, 2005, 9:38 PM
 */


 
package org.tigr.microarray.mev;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.JOptionPane;

import java.util.Properties;

import org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.ParameterPanel;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.HelpWindow;

/**
 *
 * @author  braisted
 */
public class VarianceFilterDialog extends AlgorithmDialog {

    private int result;
    private JCheckBox enableFilterBox;
    private JTextField valueField;
    private JRadioButton percentButton, numberButton, sdValueButton;
    private JLabel valueLabel;
    
   /** Creates a new instance of VarianceFilterDialog */
    public VarianceFilterDialog(JFrame parent) {
    
	super(parent, "Variance Filter", true);	
        
	Listener listener = new Listener();

        ParameterPanel panel = new ParameterPanel("Filter Settings");
        panel.setLayout(new GridBagLayout());
        
        enableFilterBox = new JCheckBox("Enable Variance Filter", true);
        enableFilterBox.setOpaque(false);
        enableFilterBox.setFocusPainted(false);
        enableFilterBox.setActionCommand("enable-box-command");        
        enableFilterBox.addActionListener(listener);
        
        ButtonGroup group = new ButtonGroup();
        
        percentButton = new JRadioButton("Percentage of Highest SD Genes (1-100)", true);
        percentButton.setFocusPainted(false);
        percentButton.setOpaque(false);
        group.add(percentButton);
        
        numberButton = new JRadioButton("Number of Desired High SD Genes");
        numberButton.setFocusPainted(false);
        numberButton.setOpaque(false);
        group.add(numberButton);

        sdValueButton = new JRadioButton("SD Cutoff Value", true);
        sdValueButton.setFocusPainted(false);
        sdValueButton.setOpaque(false);
        group.add(sdValueButton);

        valueLabel = new JLabel("Value: ");
        valueField = new JTextField("50",10);
        valueField.selectAll();

        panel.add(enableFilterBox, new GridBagConstraints(0,0,2,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(10,0,20,0),0,0));
        panel.add(percentButton, new GridBagConstraints(0,1,2,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
        panel.add(numberButton, new GridBagConstraints(0,2,2,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
        panel.add(sdValueButton, new GridBagConstraints(0,3,2,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
        panel.add(valueLabel, new GridBagConstraints(0,4,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(20,0,30,0),0,0));
        panel.add(valueField, new GridBagConstraints(1,4,1,1,0,0,GridBagConstraints.WEST,GridBagConstraints.VERTICAL, new Insets(20,10,30,0),0,0));
        
        addContent(panel);
        setActionListeners(listener);
        addWindowListener(listener);
	pack();
	setResizable(false);     
    }
    
    public int showModal() {
	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);
	show();
	return result;
    }
    
    private void enableControls(boolean enable) {
        this.percentButton.setEnabled(enable);
        this.numberButton.setEnabled(enable);
        this.sdValueButton.setEnabled(enable);
        this.valueField.setEnabled(enable);
        this.valueLabel.setEnabled(enable);
    }
    
    private void resetControls() {
        enableFilterBox.setSelected(true);
        percentButton.setSelected(true);
        valueField.setText("50");
        valueField.selectAll();
        enableControls(true);
    }
    
    public Properties getProperties() {
        Properties props = new Properties();
        
        if(enableFilterBox.isSelected()) {
            props.setProperty("Filter Enabled", "true");
            if(percentButton.isSelected())
                props.setProperty("Filter Mode", "percent mode");
            else if(numberButton.isSelected())
                props.setProperty("Filter Mode", "number of genes mode");
            else
                props.setProperty("Filter Mode", "sd value mode");
            
            props.setProperty("Value", valueField.getText());
        } else {  
            props.setProperty("Filter Enabled", "false");
        }
        return props;
        
    }
    
    private boolean validateValues() {
        boolean valid = true;
        if(percentButton.isSelected()) {
            
            try {
                Float.parseFloat(valueField.getText());
                
            } catch (NumberFormatException nfe) {
                valid = false;
                JOptionPane.showMessageDialog(this, "Improper input format.", "Input Format Error", JOptionPane.ERROR_MESSAGE);                
            }            
            if(valid) {
                float value = Float.parseFloat(valueField.getText());
                if(value <= 0 || value > 100) {
                    valid = false;
                    JOptionPane.showMessageDialog(this, "Improper value range. (valid range is 0 < x <= 100)", "Input Format Error", JOptionPane.ERROR_MESSAGE);                                    
                }
            }            
        } else if(numberButton.isSelected()) {
            try {
                Integer.parseInt(valueField.getText());
            } catch (NumberFormatException nfe) {
                valid = false;
                JOptionPane.showMessageDialog(this, "Improper input format. Value should be an integer.", "Input Format Error", JOptionPane.ERROR_MESSAGE);
            }
            if(valid) {
                int value = Integer.parseInt(valueField.getText());
                if(value <= 0) {
                    valid = false;
                    JOptionPane.showMessageDialog(this, "Improper value range. (valid range is x > 0)", "Input Format Error", JOptionPane.ERROR_MESSAGE);                                    
                }
            }            
        } else {
            try {
                Float.parseFloat(valueField.getText());
            } catch (NumberFormatException nfe) {
                valid = false;
                JOptionPane.showMessageDialog(this, "Improper input format.", "Input Format Error", JOptionPane.ERROR_MESSAGE);
            }
            if(valid) {
                float value = Float.parseFloat(valueField.getText());
                if(value < 0) {
                    valid = false;
                    JOptionPane.showMessageDialog(this, "Improper value range. (valid range is x >= 0)", "Input Format Error", JOptionPane.ERROR_MESSAGE);                                    
                }
            }                        
        }
        return valid;
    }
    
    private class Listener extends WindowAdapter implements ActionListener, KeyListener {
	public void actionPerformed(ActionEvent event) {
	    String command = event.getActionCommand();
	    if (command.equals("ok-command")) {
                if(!validateValues())
                    return;
		onOk();
	    } else if (command.equals("cancel-command")) {
		result = JOptionPane.CANCEL_OPTION;
		dispose();
	    } else if (command.equals("reset-command")) {               
                resetControls();
            } else if (command.equals("enable-box-command")) {
                enableControls(enableFilterBox.isSelected());
            } else if (command.equals("info-command")){
                HelpWindow hw = new HelpWindow(VarianceFilterDialog.this, "Variance Filter Dialog");
                result = JOptionPane.CANCEL_OPTION;
                if(hw.getWindowContent()){
                    hw.setSize(550,600);
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
	
	public void keyPressed(KeyEvent event) {
	    if (event.getKeyCode() == KeyEvent.VK_ENTER) {
		onOk();
	    }
	}
	
	public void windowClosing(WindowEvent e) {
	    result = JOptionPane.CLOSED_OPTION;
	    dispose();
	}
	
	public void keyReleased(KeyEvent event) {;}
	public void keyTyped(KeyEvent event) {;}
	
	private void onOk() {
	    try {
	
		result = JOptionPane.OK_OPTION;
	    } catch (Exception exception) {
		result = JOptionPane.CANCEL_OPTION;
	    }
	    dispose();
	}
    }
    
    public static void main(String [] args) {
        VarianceFilterDialog d = new VarianceFilterDialog(new JFrame());
        d.showModal();
    }
}

