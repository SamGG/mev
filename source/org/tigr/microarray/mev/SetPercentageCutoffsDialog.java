/*
Copyright @ 1999-2004, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: SetPercentageCutoffsDialog.java,v $
 * $Revision: 1.5 $
 * $Date: 2006-02-23 20:59:41 $
 * $Author: caliente $
 * $State: Exp $
 */
package org.tigr.microarray.mev;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.HelpWindow;
import org.tigr.util.awt.GBA;


public class SetPercentageCutoffsDialog extends AlgorithmDialog {
    
    private float originalValue;
    private int result;
    private JTextField textField;
    private JCheckBox enableCheckBox;
    private JLabel percentageLabel;
    
    public SetPercentageCutoffsDialog(JFrame parent, float percentage) {
	super(parent, "Set Percentage Cutoff", true);	
	Listener listener = new Listener();
	originalValue = percentage;
        
	GBA gba = new GBA();
	
        enableCheckBox = new JCheckBox("Enable Percentage Cutoff Filter", true);
        enableCheckBox.setActionCommand("enable-check-box-command");
        enableCheckBox.setOpaque(false);
        enableCheckBox.setFocusPainted(false);        
        enableCheckBox.addActionListener(listener);
        
	percentageLabel = new JLabel("Percentage Cutoff (" + percentage + "%): ");
	percentageLabel.setHorizontalTextPosition(JLabel.RIGHT);
        percentageLabel.setHorizontalAlignment(JLabel.RIGHT);
   
	textField = new JTextField(10);
        textField.setText(String.valueOf(percentage));
        textField.selectAll();
	textField.addKeyListener(listener);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.white);
        panel.setBorder(BorderFactory.createLineBorder(Color.black));

        gba.add(panel, enableCheckBox, 0, 0, 2, 1, 0, 0, GBA.V, GBA.C, new Insets(25, 5, 0, 5), 0, 0);
	gba.add(panel, percentageLabel, 0, 1, 1, 1, 1, 0, GBA.B, GBA.E, new Insets(10, 5, 35, 5), 0, 0);
	gba.add(panel, textField, 1, 1, 1, 1, 1, 0, GBA.NONE, GBA.W, new Insets(10, 5, 35, 5), 0, 0);

        addContent(panel);
        setActionListeners(listener);
        addWindowListener(listener);
	pack();
	setResizable(false);
	textField.grabFocus();        
    }
    
    public int showModal() {
	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);
	show();
	return result;
    }
    
    public boolean isCutoffFilterEnabled() {
        return enableCheckBox.isSelected();
    }
    
    public float getPercentageCutoff() {
	return Float.parseFloat(textField.getText());
    }
    
    public static void main(String [] args) {
        SetPercentageCutoffsDialog dialog = new SetPercentageCutoffsDialog(new JFrame(), 0f);
        dialog.showModal();
    }
    
    private class Listener extends WindowAdapter implements ActionListener, KeyListener {
	public void actionPerformed(ActionEvent event) {
	    String command = event.getActionCommand();
	    if (command.equals("ok-command")) {
		try {
		    Float.parseFloat(textField.getText());
		    result = JOptionPane.OK_OPTION;
		} catch (Exception exception) {
		    result = JOptionPane.CANCEL_OPTION;
		}
		dispose();
	    } else if (command.equals("cancel-command")) {
		result = JOptionPane.CANCEL_OPTION;
		dispose();
	    } else if (command.equals("reset-command")) {
                enableCheckBox.setSelected(true);
                textField.setEnabled(true);
                percentageLabel.setEnabled(true);
                textField.setText(String.valueOf(originalValue));
                textField.selectAll();
                textField.grabFocus();
            } else if (command.equals("info-command")){
                HelpWindow hw = new HelpWindow(SetPercentageCutoffsDialog.this, "Set Percentage Cutoff");
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
            } else if (command.equals("enable-check-box-command")) {
                boolean enabled = enableCheckBox.isSelected();
                percentageLabel.setEnabled(enabled);
                textField.setEnabled(enabled);
            }
	}
	
	public void windowClosing(WindowEvent e) {
	    result = JOptionPane.CLOSED_OPTION;
	    dispose();
	}
	
	public void keyPressed(KeyEvent event) {
	    if (event.getKeyCode() == KeyEvent.VK_ENTER) {
		try {
		    Float.parseFloat(textField.getText());
		    result = JOptionPane.OK_OPTION;
		} catch (Exception exception) {
		    result = JOptionPane.CANCEL_OPTION;
		}
		dispose();
	    }
	}
	
	public void keyReleased(KeyEvent event) {;}
	public void keyTyped(KeyEvent event) {;}
    }
}