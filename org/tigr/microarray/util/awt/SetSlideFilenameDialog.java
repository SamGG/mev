/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: SetSlideFilenameDialog.java,v $
 * $Revision: 1.2 $
 * $Date: 2005-02-24 20:24:11 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.microarray.util.awt;

import java.awt.Frame;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.Dimension;
import java.awt.Container;
import java.awt.GridBagLayout;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.JOptionPane;

import org.tigr.util.awt.GBA;

public class SetSlideFilenameDialog extends JDialog {
    
    private int result;
    private JTextField filenameTextField;
    
    public SetSlideFilenameDialog(Frame parent, String[] filenames) {
	super(parent, "Select Experiment", true);
	
	Listener listener = new Listener();
	addWindowListener(listener);
	
	JLabel filenameLabel = new JLabel("Filename: ");
	
	filenameTextField = new JTextField(15);
	filenameTextField.addKeyListener(listener);
	
	JComboBox filenameChoice = new JComboBox(filenames);
	filenameChoice.addItemListener(listener);
	filenameChoice.addKeyListener(listener);
	
	JButton okButton = new JButton("OK");
	okButton.setActionCommand("ok-command");
	okButton.addActionListener(listener);
	
	JButton cancelButton = new JButton("Cancel");
	cancelButton.setActionCommand("cancel-command");
	cancelButton.addActionListener(listener);
	
	Container contentPane = getContentPane();
	contentPane.setLayout(new GridBagLayout());
	GBA gba = new GBA();
	gba.add(contentPane, filenameLabel, 0, 0, 1, 1, 0, 0, GBA.NONE, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
	gba.add(contentPane, filenameTextField, 1, 0, 1, 1, 1, 1, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
	gba.add(contentPane, okButton, 2, 0, 1, 1, 0, 0, GBA.NONE, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
	gba.add(contentPane, filenameChoice, 1, 1, 1, 1, 1, 1, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
	gba.add(contentPane, cancelButton, 2, 1, 1, 1, 0, 0, GBA.NONE, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
	
	pack();
	setResizable(false);
	this.filenameTextField.grabFocus();
    }
    
    public int showModal() {
	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);
	show();
	return result;
    }
    
    public String getFileName() {
	return filenameTextField.getText();
    }
    
    private class Listener extends WindowAdapter implements ActionListener, ItemListener, KeyListener {
	
	public void actionPerformed(ActionEvent event) {
	    String command = event.getActionCommand();
	    if (command.equals("ok-command")) {
		onOk();
	    } else if (command.equals("cancel-command")) {
		result = JOptionPane.CANCEL_OPTION;
		dispose();
	    }
	}
	
	public void itemStateChanged(ItemEvent event) {
	    filenameTextField.setText((String)event.getItem());
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
	
	private void onOk() {
	    result = JOptionPane.OK_OPTION;
	    dispose();
	}
	
	public void keyReleased(KeyEvent event) {;}
	public void keyTyped(KeyEvent event) {;}
    }
}