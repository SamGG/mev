/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: SetElementSizeDialog.java,v $
 * $Revision: 1.4 $
 * $Date: 2006-02-23 20:59:59 $
 * $Author: caliente $
 * $State: Exp $
 */
package org.tigr.microarray.util.awt;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import org.tigr.util.awt.GBA;

public class SetElementSizeDialog extends JDialog {
    private int result;
    private Dimension size;
    private JLabel widthLabel, heightLabel;
    private JTextField widthTextField, heightTextField;
    private GBA gba;
    
    public SetElementSizeDialog(JFrame parent, Dimension elementSize) {
	super(parent, true);
	setTitle("Set Element Size");
	gba = new GBA();
	widthLabel = new JLabel("Element Width (" + elementSize.width + "): ");
	widthTextField = new JTextField(10);
	widthTextField.setText("" + elementSize.width);
	heightLabel = new JLabel("Element Height (" + elementSize.height + "): ");
	heightTextField = new JTextField(10);
	heightTextField.setText("" + elementSize.height);
	JButton okButton = new JButton("Okay");
	okButton.setActionCommand("ok-command");
	okButton.addActionListener(new Listener());
	JButton cancelButton = new JButton("Cancel");
	cancelButton.setActionCommand("cancel-command");
	cancelButton.addActionListener(new Listener());
	
	Container content = getContentPane();
	content.setLayout(new GridBagLayout());
	gba.add(content, widthLabel, 0, 0, 1, 1, 0, 0, GBA.NONE, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
	gba.add(content, widthTextField, 1, 0, 1, 1, 1, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
	gba.add(content, heightLabel, 0, 1, 1, 1, 0, 0, GBA.NONE, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
	gba.add(content, heightTextField, 1, 1, 2, 1, 1, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
	gba.add(content, cancelButton, 0, 2, 1, 1, 0, 0, GBA.NONE, GBA.W, new Insets(5, 5, 5, 5), 0, 0);
	gba.add(content, okButton, 1, 2, 1, 1, 0, 0, GBA.NONE, GBA.E, new Insets(5, 5, 5, 5), 0, 0);
	
	setResizable(false);
	widthTextField.grabFocus();
	getRootPane().setDefaultButton(okButton);
	pack();
    }
    
    public int showModal() {
	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);
	show();
	return result;
    }
    
    public Dimension getElementSize() {
	return size;
    }
    
    private class Listener extends WindowAdapter implements ActionListener {
	
	public void actionPerformed(ActionEvent event) {
	    String command = event.getActionCommand();
	    if (command.equals("ok-command")) {
		String width = widthTextField.getText();
		String height = heightTextField.getText();
		try {
		    size = new Dimension(Integer.parseInt(width), Integer.parseInt(height));
		    result = JOptionPane.OK_OPTION;
		} catch (Exception e) {
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
    }
    
}