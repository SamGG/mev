/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: SetPercentageCutoffsDialog.java,v $
 * $Revision: 1.1.1.1 $
 * $Date: 2003-08-21 21:04:24 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.microarray.mev;

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

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JTextField;
import javax.swing.JOptionPane;

import org.tigr.util.awt.GBA;

public class SetPercentageCutoffsDialog extends JDialog {
    
    private int result;
    private JTextField textField;
    
    public SetPercentageCutoffsDialog(JFrame parent, float percentage) {
	super(parent, "Set Percentage Cutoff", true);
	
	GBA gba = new GBA();
	
	JLabel percentageLabel = new JLabel("Percentage Cutoff (" + percentage + "%): ");
	
	Listener listener = new Listener();
	
	textField = new JTextField(10);
	textField.addKeyListener(listener);
	textField.setText(String.valueOf(percentage));
	
	JButton okButton = new JButton("Ok");
	okButton.setActionCommand("ok-command");
	okButton.addActionListener(listener);
	
	JButton cancelButton = new JButton("Cancel");
	cancelButton.setActionCommand("cancel-command");
	cancelButton.addActionListener(listener);
	
	getContentPane().setLayout(new GridBagLayout());
	gba.add(getContentPane(), percentageLabel, 0, 0, 1, 1, 0, 0, GBA.NONE, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
	gba.add(getContentPane(), textField, 1, 0, 1, 1, 1, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
	gba.add(getContentPane(), cancelButton, 0, 1, 1, 1, 0, 0, GBA.NONE, GBA.W, new Insets(5, 5, 5, 5), 0, 0);
	gba.add(getContentPane(), okButton, 1, 1, 1, 1, 0, 0, GBA.NONE, GBA.E, new Insets(5, 5, 5, 5), 0, 0);
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
    
    public float getPercentageCutoff() {
	return Float.parseFloat(textField.getText());
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