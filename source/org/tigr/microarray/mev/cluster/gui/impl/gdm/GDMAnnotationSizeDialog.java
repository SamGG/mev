/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: GDMAnnotationSizeDialog.java,v $
 * $Revision: 1.2 $
 * $Date: 2006-02-23 20:59:51 $
 * $Author: caliente $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.gdm;

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

public class GDMAnnotationSizeDialog extends JDialog {
    private int result;
    private int size=100;
    private JLabel sizeLabel;
    private JTextField sizeTextField;
    private GBA gba;
    private final int maxSize = 300;
 	  
    public GDMAnnotationSizeDialog(JFrame parent) {
		super(parent, true);
		setTitle("Set Annotation Size");
		gba = new GBA();
		sizeLabel = new JLabel("Annotation Size (max = 300):  ");
		sizeTextField = new JTextField(10);
//		sizeTextField.setText("" + size );
		JButton okButton = new JButton("Okay");
		okButton.setActionCommand("ok-command");
		okButton.addActionListener(new Listener());
		JButton cancelButton = new JButton("Cancel");
		cancelButton.setActionCommand("cancel-command");
		cancelButton.addActionListener(new Listener());
	
		Container content = getContentPane();
		content.setLayout(new GridBagLayout());
		gba.add(content, sizeLabel, 0, 0, 1, 1, 0, 0, GBA.NONE, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
		gba.add(content, sizeTextField, 1, 0, 1, 1, 1, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
		gba.add(content, cancelButton, 0, 2, 1, 1, 0, 0, GBA.NONE, GBA.W, new Insets(5, 5, 5, 5), 0, 0);
		gba.add(content, okButton, 1, 2, 1, 1, 0, 0, GBA.NONE, GBA.E, new Insets(5, 5, 5, 5), 0, 0);
	
		setResizable(false);
		sizeTextField.grabFocus();
		getRootPane().setDefaultButton(okButton);
		pack();
    }
    
    public int showModal() {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);
		show();
		return result;
    }
    
    public int getAnnotationSize() {
		return size;
    }
    
    private class Listener extends WindowAdapter implements ActionListener {
	
		public void actionPerformed(ActionEvent event) {
	    	String command = event.getActionCommand();
	    	if (command.equals("ok-command")) {
				String sz = sizeTextField.getText();
				try {
					int i_sz = Integer.parseInt(sz);
					if (i_sz > maxSize) {
						i_sz = maxSize;
					} else if (i_sz < 1) {
						i_sz = 1;
					}
					
		    		size = i_sz;
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
