/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: SetUpperLimitsDialog.java,v $
 * $Revision: 1.4 $
 * $Date: 2006-02-23 20:59:41 $
 * $Author: caliente $
 * $State: Exp $
 */
package org.tigr.microarray.mev;

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

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import org.tigr.util.awt.GBA;

public class SetUpperLimitsDialog extends JDialog {
    
    private int result;
    private JTextField cy3TextField, cy5TextField;
    
    public SetUpperLimitsDialog(JFrame parent, float xValue, float yValue) {
	super(parent, "Set Upper Thresholds", true);
	
	EventListener listener = new EventListener();
	
	JLabel cy3Label = new JLabel("Max Cy3 (" + xValue + "): ");
	cy3Label.addKeyListener(listener);
	
	cy3TextField = new JTextField(10);
	cy3TextField.addKeyListener(listener);
	cy3TextField.setText("" + xValue);
	
	JLabel cy5Label = new JLabel("Max Cy5 (" + yValue + "): ");
	cy5Label.addKeyListener(listener);
	
	cy5TextField = new JTextField(10);
	cy5TextField.addKeyListener(listener);
	cy5TextField.setText("" + yValue);
	
	JButton okButton = new JButton("Ok");
	okButton.setActionCommand("ok-command");
	okButton.addActionListener(listener);
	
	JButton cancelButton = new JButton("Cancel");
	cancelButton.setActionCommand("cancel-command");
	cancelButton.addActionListener(listener);
	
	getContentPane().setLayout(new GridBagLayout());
	GBA gba = new GBA();
	gba.add(getContentPane(), cy3Label, 0, 0, 1, 1, 0, 0, GBA.NONE, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
	gba.add(getContentPane(), cy3TextField, 1, 0, 1, 1, 1, 1, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
	gba.add(getContentPane(), cy5Label, 0, 1, 1, 1, 0, 0, GBA.NONE, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
	gba.add(getContentPane(), cy5TextField, 1, 1, 1, 1, 1, 1, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
	gba.add(getContentPane(), cancelButton, 0, 2, 1, 1, 0, 0, GBA.NONE, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
	gba.add(getContentPane(), okButton, 1, 2, 1, 1, 0, 0, GBA.NONE, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
	addWindowListener(listener);
	pack();
	setResizable(false);
	cy3TextField.grabFocus();
    }
    
    public int showModal() {
	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);
	show();
	return result;
    }
    
    public float getUpperCY3() {
	return Float.parseFloat(cy3TextField.getText());
    }
    
    public float getUpperCY5() {
	return Float.parseFloat(cy5TextField.getText());
    }
    
    private class EventListener extends WindowAdapter implements ActionListener, KeyListener {
	public void actionPerformed(ActionEvent event) {
	    String command = event.getActionCommand();
	    if (command.equals("ok-command")) {
		try {
		    Float.parseFloat(cy3TextField.getText());
		    Float.parseFloat(cy5TextField.getText());
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
		    Float.parseFloat(cy3TextField.getText());
		    Float.parseFloat(cy5TextField.getText());
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