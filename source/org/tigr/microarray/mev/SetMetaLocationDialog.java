/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: SetMetaLocationDialog.java,v $
 * $Revision: 1.4 $
 * $Date: 2006-02-23 20:59:41 $
 * $Author: caliente $
 * $State: Exp $
 */
package org.tigr.microarray.mev;

import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Hashtable;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.tigr.util.awt.ActionInfoDialog;
import org.tigr.util.awt.ActionInfoEvent;
import org.tigr.util.awt.GBA;

public class SetMetaLocationDialog extends ActionInfoDialog {
    private JFrame parent;
    JLabel rowLabel, columnLabel;
    JTextField rowTextField, columnTextField;
    JButton okButton, cancelButton;
    GBA gba;
    
    public SetMetaLocationDialog(JFrame parent) {
	super(parent, true);
	
	try {
	    this.parent = parent;
	    gba = new GBA();
	    
	    rowLabel = new JLabel("Meta Row: ");
	    rowLabel.addKeyListener(new EventListener());
	    
	    rowTextField = new JTextField(10);
	    rowTextField.addKeyListener(new EventListener());
	    
	    columnLabel = new JLabel("Meta Column: ");
	    columnLabel.addKeyListener(new EventListener());
	    
	    columnTextField = new JTextField(10);
	    columnTextField.addKeyListener(new EventListener());
	    
	    cancelButton = new JButton("Cancel");
	    cancelButton.addActionListener(new EventListener());
	    
	    okButton = new JButton("Okay");
	    okButton.addActionListener(new EventListener());
	    
	    contentPane.setLayout(new GridBagLayout());
	    gba.add(contentPane, rowLabel, 0, 0, 1, 1, 0, 0, GBA.NONE, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
	    gba.add(contentPane, rowTextField, 1, 0, 1, 1, 1, 1, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
	    gba.add(contentPane, columnLabel, 0, 1, 1, 1, 0, 0, GBA.NONE, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
	    gba.add(contentPane, columnTextField, 1, 1, 1, 1, 1, 1, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
	    gba.add(contentPane, cancelButton, 0, 2, 1, 1, 0, 0, GBA.NONE, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
	    gba.add(contentPane, okButton, 1, 2, 1, 1, 0, 0, GBA.NONE, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
	    
	    pack();
	    setResizable(false);
	    setTitle("Select MetaBlock");
	    rowTextField.grabFocus();
	    setLocation(225, 225);
	} catch (Exception e) {
	    System.out.println("Exception (SetMetaLocation.const()): " + e);
	}
    }
    
    class EventListener implements ActionListener, KeyListener {
	public void actionPerformed(ActionEvent event) {
	    if (event.getSource() == okButton) {
		String row = rowTextField.getText();
		String column = columnTextField.getText();
		
		//((DisplayFrame) parent).getDisplayApplet().createRegion(Integer.parseInt(row), Integer.parseInt(column));
		Hashtable hash = new Hashtable();
		hash.put(new String("row"), row);
		hash.put(new String("column"), column);
		fireEvent(new ActionInfoEvent(this, hash));
		
		dispose();
	    } else if (event.getSource() == cancelButton) dispose();
	}
	
	public void keyPressed(KeyEvent event) {
	    if (event.getKeyCode() == KeyEvent.VK_ENTER) {
		String row = rowTextField.getText();
		String column = columnTextField.getText();
		
		//((DisplayFrame) parent).getDisplayApplet().createRegion(Integer.parseInt(row), Integer.parseInt(column));
		Hashtable hash = new Hashtable();
		hash.put(new String("row"), row);
		hash.put(new String("column"), column);
		fireEvent(new ActionInfoEvent(this, hash));
		
		dispose();
	    }
	}
	
	public void keyReleased(KeyEvent event) {;}
	public void keyTyped(KeyEvent event) {;}
    }
}
