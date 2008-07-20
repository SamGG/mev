/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: SetSingleLowerCutoffsDialog.java,v $
 * $Revision: 1.1 $
 * $Date: 2006-02-24 17:00:22 $
 * $Author: wwang67 $
 * $State: Exp $
 */
package org.tigr.microarray.mev;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.GridBagLayout;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JOptionPane;

import org.tigr.util.awt.GBA;

import org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.HelpWindow;


public class SetSingleLowerCutoffsDialog extends AlgorithmDialog {
    
    private int result;
    private float originalCy3, originalCy5;
    private JTextField cy3TextField, cy5TextField;
    private JCheckBox enableCheckBox;
    private JLabel cy3Label, cy5Label;
    
    public SetSingleLowerCutoffsDialog(JFrame parent, float cy5) {
	super(parent, "Set Lower Cutoffs", true);
	//originalCy3 = cy3;
        originalCy5 = cy5;
        
	Listener listener = new Listener();
	GBA gba = new GBA();
	
        enableCheckBox = new JCheckBox("Enable Lower Cutoff Filter", true);
        enableCheckBox.setActionCommand("enable-check-box-command");
        enableCheckBox.setOpaque(false);
        enableCheckBox.setFocusPainted(false);        
        enableCheckBox.addActionListener(listener);
        
        
      //  cy3Label = new JLabel("Cy3 Lower Cutoff (" + cy3 + "): ");
	//cy3Label.setHorizontalAlignment(JLabel.RIGHT);
	/*
        cy3TextField = new JTextField(10);
	cy3TextField.addKeyListener(listener);
	cy3TextField.setText("" + cy3);
	*/
        cy5Label = new JLabel("Intensity Lower Cutoff (" + cy5 + "): ");
	
	cy5TextField = new JTextField(10);
	cy5TextField.addKeyListener(listener);
	cy5TextField.setText("" + cy5);
	
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.white);
        panel.setBorder(BorderFactory.createLineBorder(Color.black));
        
        gba.add(panel, enableCheckBox, 0, 0, 2, 1, 0, 0, GBA.B, GBA.C, new Insets(20, 0, 0, 0), 0, 0);
	//gba.add(panel, cy3Label, 0, 1, 1, 1, 0, 0, GBA.H, GBA.E, new Insets(15, 5, 5, 5), 0, 0);
	//gba.add(panel, cy3TextField, 1, 1, 1, 1, 0, 0, GBA.NONE, GBA.W, new Insets(15, 5, 5, 5), 0, 0);
	gba.add(panel, cy5Label, 0, 1, 1, 1, 0, 0, GBA.H, GBA.E, new Insets(5, 5, 25, 5), 0, 0);
	gba.add(panel, cy5TextField, 1, 1, 2, 1, 0, 0, GBA.NONE, GBA.W, new Insets(5, 5, 25, 5), 0, 0);

        addContent(panel);
        setActionListeners(listener);
        addWindowListener(listener);
	pack();
	setResizable(false);

        cy5TextField.grabFocus();
        cy5TextField.setCaretPosition(0);
        cy5TextField.selectAll();     
    }
    
    public int showModal() {
	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);
	show();
	return result;
    }
    
    public boolean isLowerCutoffEnabled() {
        return enableCheckBox.isSelected();
    }
    
    public float getLowerCY5Cutoff() {
	return Float.parseFloat(cy5TextField.getText());
    }
    
    private class Listener extends WindowAdapter implements ActionListener, KeyListener {
	public void actionPerformed(ActionEvent event) {
	    String command = event.getActionCommand();
	    if (command.equals("ok-command")) {
		onOk();
	    } else if (command.equals("cancel-command")) {
		result = JOptionPane.CANCEL_OPTION;
		dispose();
	    } else if (command.equals("reset-command")) {               
                cy5TextField.setText(String.valueOf(originalCy5));
                //cy3TextField.setText(String.valueOf(originalCy3));
                cy5TextField.grabFocus();                                
                cy5TextField.setCaretPosition(0);
                cy5TextField.selectAll();
            } else if (command.equals("info-command")){
                HelpWindow hw = new HelpWindow(SetSingleLowerCutoffsDialog.this, "Set Lower Cutoffs");
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
                boolean enable = enableCheckBox.isSelected();
                //cy3Label.setEnabled(enable);
                cy5Label.setEnabled(enable);
                //cy3TextField.setEnabled(enable);
                cy5TextField.setEnabled(enable);
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
		//Float.parseFloat(cy3TextField.getText());
		Float.parseFloat(cy5TextField.getText());
		result = JOptionPane.OK_OPTION;
	    } catch (Exception exception) {
		result = JOptionPane.CANCEL_OPTION;
	    }
	    dispose();
	}
    }
    /*
    public static void main(String [] args) {
        SetLowerCutoffsDialog d = new SetLowerCutoffsDialog(new javax.swing.JFrame(), 0.0f, 0.0f);
        d.showModal();
    }
    */
}
