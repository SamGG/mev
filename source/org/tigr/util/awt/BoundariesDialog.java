/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: BoundariesDialog.java,v $
 * $Revision: 1.4 $
 * $Date: 2006-02-23 21:00:04 $
 * $Author: caliente $
 * $State: Exp $
 */
package org.tigr.util.awt;

import java.awt.Font;
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

public class BoundariesDialog extends ActionInfoDialog {
    private JFrame parent;
    JLabel lowerxLabel, upperxLabel, loweryLabel, upperyLabel;
    JTextField lowerxTextField, upperxTextField, loweryTextField, upperyTextField;
    JButton okButton, cancelButton;
    Font boundariesDialogFont;
    GBA gba;
    
    public BoundariesDialog(JFrame parent, double lowerx, double upperx, double lowery, double uppery) {
	super(parent, true);
	try {
	    this.parent = parent;
	    gba = new GBA();
	    
	    lowerxLabel = new JLabel("Lower X: (" + lowerx + ")");
	    upperxLabel = new JLabel("Upper X: (" + upperx + ")");
	    loweryLabel = new JLabel("Lower Y: (" + lowery + ")");
	    upperyLabel = new JLabel("Upper Y: (" + uppery + ")");
	    
	    lowerxTextField = new JTextField(12);
	    lowerxTextField.setText("" + lowerx);
	    upperxTextField = new JTextField(12);
	    upperxTextField.setText("" + upperx);
	    loweryTextField = new JTextField(12);
	    loweryTextField.setText("" + lowery);
	    upperyTextField = new JTextField(12);
	    upperyTextField.setText("" + uppery);
	    
	    okButton = new JButton("OK");
	    okButton.addActionListener(new EventListener());
	    cancelButton = new JButton("Cancel");
	    cancelButton.addActionListener(new EventListener());
	    
	    contentPane.setLayout(new GridBagLayout());
	    
	    gba.add(contentPane, upperyLabel, 1, 0, 1, 1, 1, 1, GBA.NONE, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
	    gba.add(contentPane, lowerxLabel, 0, 1, 1, 1, 1, 1, GBA.NONE, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
	    gba.add(contentPane, upperyTextField, 1, 1, 1, 1, 1, 1, GBA.NONE, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
	    gba.add(contentPane, upperxLabel, 2, 1, 1, 1, 1, 1, GBA.NONE, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
	    gba.add(contentPane, lowerxTextField, 0, 2, 1, 1, 1, 1, GBA.NONE, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
	    gba.add(contentPane, loweryLabel, 1, 2, 1, 1, 1, 1, GBA.NONE, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
	    gba.add(contentPane, upperxTextField, 2, 2, 1, 1, 1, 1, GBA.NONE, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
	    gba.add(contentPane, cancelButton, 0, 3, 1, 1, 1, 1, GBA.NONE, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
	    gba.add(contentPane, loweryTextField, 1, 3, 1, 1, 1, 1, GBA.NONE, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
	    gba.add(contentPane, okButton, 2, 3, 1, 1, 1, 1, GBA.NONE, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
	    
	    pack();
	    setResizable(false);
	    setTitle("Set Graph Boundaries");
	    okButton.grabFocus();
	    setLocation(300, 300);
	} catch (Exception e) {
	    System.out.println("Exception (DatabaseLoginDialog.const()): " + e);
	}
    }
    
    class EventListener implements ActionListener, KeyListener {
	public void actionPerformed(ActionEvent event) {
	    if (event.getSource() == okButton) {
		String lowerx = lowerxTextField.getText();
		String upperx = upperxTextField.getText();
		String lowery = loweryTextField.getText();
		String uppery = upperyTextField.getText();
		hide();
		
		Hashtable hash = new Hashtable();
		hash.put(new String("lowerx"), lowerx);
		hash.put(new String("upperx"), upperx);
		hash.put(new String("lowery"), lowery);
		hash.put(new String("uppery"), uppery);
		
		fireEvent(new ActionInfoEvent(this, hash));
		
		dispose();
	    } else if (event.getSource() == cancelButton) dispose();
	}
	
	public void keyPressed(KeyEvent event) {
	    if (event.getKeyCode() == KeyEvent.VK_ENTER) {
		String lowerx = lowerxTextField.getText();
		String upperx = upperxTextField.getText();
		String lowery = loweryTextField.getText();
		String uppery = upperyTextField.getText();
		hide();
		
		Hashtable hash = new Hashtable();
		hash.put(new String("lowerx"), lowerx);
		hash.put(new String("upperx"), upperx);
		hash.put(new String("lowery"), lowery);
		hash.put(new String("uppery"), uppery);
		
		fireEvent(new ActionInfoEvent(this, hash));
		
		dispose();
	    }
	}
	
	public void keyReleased(KeyEvent event) {}
	public void keyTyped(KeyEvent event) {}
    }
}
