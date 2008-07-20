/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: SetConfidenceDialog.java,v $
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
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.tigr.util.awt.ActionInfoDialog;
import org.tigr.util.awt.ActionInfoEvent;
import org.tigr.util.awt.GBA;

public class SetConfidenceDialog extends ActionInfoDialog {
    private JFrame parent;
    GBA gba;
    JLabel confidenceLabel;
    JComboBox confidenceChoice;
    JButton okButton, cancelButton;
    
    public SetConfidenceDialog(JFrame parent) {
	super(parent, true);
	try {
	    this.parent = parent;
	    gba = new GBA();
	    
	    confidenceLabel = new JLabel("Select a confidence level (%): ");
	    confidenceLabel.addKeyListener(new EventListener());
	    confidenceChoice = new JComboBox();
	    confidenceChoice.addItem("95");
	    confidenceChoice.addItem("99");
	    confidenceChoice.addKeyListener(new EventListener());
	    okButton = new JButton("Okay");
	    okButton.addActionListener(new EventListener());
	    cancelButton = new JButton("Cancel");
	    cancelButton.addActionListener(new EventListener());
	    
	    contentPane.setLayout(new GridBagLayout());
	    gba.add(contentPane, confidenceLabel, 0, 0, 1, 1, 0, 0, GBA.NONE, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
	    gba.add(contentPane, confidenceChoice, 1, 0, 1, 1, 1, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
	    gba.add(contentPane, cancelButton, 0, 1, 1, 1, 0, 0, GBA.NONE, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
	    gba.add(contentPane, okButton, 1, 1, 1, 1, 0, 0, GBA.NONE, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
	    
	    pack();
	    setResizable(false);
	    setTitle("Set Confidence Level");
	    confidenceChoice.requestFocus();
	    setLocation(400,250);
	} catch (Exception e) {
	    System.out.println("Exception (SetConfidenceDialog.const()): " + e);
	}
    }
    
    class EventListener implements ActionListener, KeyListener {
	public void actionPerformed(ActionEvent event) {
	    if (event.getSource() == okButton) {
		String confidenceString = (String) confidenceChoice.getSelectedItem();
		
		Hashtable hash = new Hashtable();
		
		if (confidenceString.equals("99")) {
		    //((DisplayApplet) parent.getSlideApplet()).normalizeData(SlideData.RATIO_STATISTICS_99);
		    hash.put(new String("confidence"), new Integer(99));
		} else {
		    //((DisplayApplet) parent.getSlideApplet()).normalizeData(SlideData.RATIO_STATISTICS_95);
		    hash.put(new String("confidence"), new Integer(95));
		}
		fireEvent(new ActionInfoEvent(this, hash));
		dispose();
	    } else if (event.getSource() == cancelButton) dispose();
	}
	
	public void keyPressed(KeyEvent event) {
	    if (event.getKeyCode() == KeyEvent.VK_ENTER) {
		String confidenceString = (String) confidenceChoice.getSelectedItem();
		
		Hashtable hash = new Hashtable();
		
		if (confidenceString.equals("99")) {
		    //((DisplayApplet) parent.getSlideApplet()).normalizeData(SlideData.RATIO_STATISTICS_99);
		    hash.put(new String("confidence"), new Integer(99));
		} else {
		    //((DisplayApplet) parent.getSlideApplet()).normalizeData(SlideData.RATIO_STATISTICS_95);
		    hash.put(new String("confidence"), new Integer(95));
		}
		fireEvent(new ActionInfoEvent(this, hash));
		dispose();
	    }
	}
	
	public void keyReleased(KeyEvent event) {;}
	public void keyTyped(KeyEvent event) {;}
    }
}
