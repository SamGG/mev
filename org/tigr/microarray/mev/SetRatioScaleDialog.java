/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/

/*
 * $RCSfile: SetRatioScaleDialog.java,v $
 * $Revision: 1.2 $
 * $Date: 2003-12-09 20:56:49 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.microarray.mev;

import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.Dimension;
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

public class SetRatioScaleDialog extends JDialog {
    
    private int result;
    private JTextField upperTextField, lowerTextField;
    
    public SetRatioScaleDialog(JFrame parent, float upper, float lower) {
	super(parent, "Set Ratio Scale", true);
	
	//TEMPORARY MEASURE UNTIL TWO RATIO LIMITS ARE INTRODUCED!
	//lower = -1 * upper;
	
	EventListener listener = new EventListener();
	
	JLabel upperLabel = new JLabel("Upper value (" + upper + "): ");
	upperLabel.addKeyListener(listener);
	
	upperTextField = new JTextField(10);
	upperTextField.addKeyListener(listener);
	upperTextField.setText("" + upper);
	
	JLabel lowerLabel = new JLabel("Lower value (" + lower + "): ");
	lowerLabel.addKeyListener(listener);
	
	lowerTextField = new JTextField(10);
	lowerTextField.addKeyListener(listener);
	lowerTextField.setText("" + lower);
	//lowerTextField.setEnabled(false);
	
	JButton okButton = new JButton("Ok");
	okButton.setActionCommand("ok-command");
	okButton.addActionListener(listener);
	
	JButton cancelButton = new JButton("Cancel");
	cancelButton.setActionCommand("cancel-command");
	cancelButton.addActionListener(listener);
	
	getContentPane().setLayout(new GridBagLayout());
	GBA gba = new GBA();
	gba.add(getContentPane(), upperLabel, 0, 0, 1, 1, 0, 0, GBA.NONE, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
	gba.add(getContentPane(), upperTextField, 1, 0, 1, 1, 1, 1, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
	gba.add(getContentPane(), lowerLabel, 0, 1, 1, 1, 0, 0, GBA.NONE, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
	gba.add(getContentPane(), lowerTextField, 1, 1, 1, 1, 1, 1, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
	gba.add(getContentPane(), cancelButton, 0, 2, 1, 1, 0, 0, GBA.NONE, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
	gba.add(getContentPane(), okButton, 1, 2, 1, 1, 0, 0, GBA.NONE, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
	
	pack();
	addWindowListener(listener);
	setResizable(false);
	upperTextField.grabFocus();
    }
    
    public int showModal() {
	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);
	show();
	return result;
    }
    
    public float getUpperRatio() {
	return Float.parseFloat(upperTextField.getText());
    }
    
    public float getLowerRatio() {
	return Float.parseFloat(lowerTextField.getText());
    }
    
    private class EventListener extends WindowAdapter implements ActionListener, KeyListener {
	public void actionPerformed(ActionEvent event) {
	    String command = event.getActionCommand();
	    if (command.equals("ok-command")) {
		try {
		    Float.parseFloat(lowerTextField.getText());
		    Float.parseFloat(upperTextField.getText());
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
		    Float.parseFloat(lowerTextField.getText());
		    Float.parseFloat(upperTextField.getText());
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