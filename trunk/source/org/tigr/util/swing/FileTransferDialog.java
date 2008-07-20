/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: FileTransferDialog.java,v $
 * $Revision: 1.2 $
 * $Date: 2006-02-23 21:00:04 $
 * $Author: caliente $
 * $State: Exp $
 */
package org.tigr.util.swing;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;

public class FileTransferDialog extends JDialog {
    
    private int result;
    
    private FileTransferPanel transferPanel;
    
    /**
     * Constructs a <code>FileTransferDialog</code> with specified
     * initial directory and set of file filters.
     */
    public FileTransferDialog(JFrame parent, String currentDirectory, FileFilter[] fileFilters) {
	super(parent, "Select Files", true);
	Listener listener = new Listener();
	transferPanel = new FileTransferPanel(currentDirectory, fileFilters);
	JPanel btnsPanel = createBtnsPanel(listener);
	
	Container content = getContentPane();
	content.setLayout(new GridBagLayout());
	content.add(transferPanel, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
	,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
	content.add(btnsPanel, new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0
	,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(4, 8, 4, 8), 0, 0));
	
	addWindowListener(listener);
	pack();
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
     * Returns the choosed files.
     */
    public File[] getFiles() {
	return transferPanel.getFiles();
    }
    
    /**
     * Creates a panel with 'OK' and 'Cancel' buttons.
     */
    private JPanel createBtnsPanel(ActionListener listener) {
	GridLayout gridLayout = new GridLayout();
	JPanel panel = new JPanel(gridLayout);
	
	JButton okButton = new JButton("OK");
	okButton.setActionCommand("ok-command");
	okButton.addActionListener(listener);
	panel.add(okButton);
	
	JButton cancelButton = new JButton("Cancel");
	cancelButton.setActionCommand("cancel-command");
	cancelButton.addActionListener(listener);
	gridLayout.setHgap(4);
	panel.add(cancelButton);
	
	getRootPane().setDefaultButton(okButton);
	
	return panel;
    }
    
    /**
     * Listener to listen to window and action events.
     */
    private class Listener extends WindowAdapter implements ActionListener {
	
	public void actionPerformed(ActionEvent e) {
	    String command = e.getActionCommand();
	    if (command.equals("ok-command")) {
		result = JOptionPane.OK_OPTION;
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
