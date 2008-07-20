/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: TransferDialog.java,v $
 * $Revision: 1.2 $
 * $Date: 2006-02-23 21:00:04 $
 * $Author: caliente $
 * $State: Exp $
 */
package org.tigr.util.awt;

import java.awt.AWTEventMulticaster;
import java.awt.Container;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class TransferDialog extends JDialog {
    private List availableList, selectedList;
    private JButton addButton, removeButton, addAllButton, removeAllButton, okButton, cancelButton;
    private JLabel availableLabel, selectedLabel;
    private EventListener eventListener;
    private ActionListener listeners;
    private String[] selectedItems;
    private GBA gba;
    
    //Add tooltip text?
    
    public TransferDialog(JFrame parentFrame, String title, boolean modal, Vector availableVector, Vector selectedVector) {
	super(parentFrame, title, modal);
	
	gba = new GBA();
	eventListener = new EventListener();
	addWindowListener(eventListener);
	Container cc = getContentPane();
	cc.setLayout(new GridBagLayout());
	
	selectedItems = new String[0];
	
	//Create components
	//Put the lists in scrollpanes!
	availableList = new List();
	availableList.setMultipleMode(true);
	try {
	    for (int i = 0; i < availableVector.size(); i++) availableList.add((String) availableVector.elementAt(i));
	} catch (NullPointerException npe) {
	    ;
	}
	selectedList = new List();
	selectedList.setMultipleMode(true);
	try {
	    for (int i = 0; i < selectedVector.size(); i++) selectedList.add((String) selectedVector.elementAt(i));
	} catch (NullPointerException npe) {
	    ;
	}
	addButton = new JButton("Add (->)");
	addButton.addActionListener(eventListener);
	addAllButton = new JButton("Add All (=>>)");
	addAllButton.addActionListener(eventListener);
	removeAllButton = new JButton("Remove All (<<=)");
	removeAllButton.addActionListener(eventListener);
	removeButton = new JButton("Remove (<-)");
	removeButton.addActionListener(eventListener);
	cancelButton = new JButton("Cancel");
	cancelButton.addActionListener(eventListener);
	okButton = new JButton("OK");
	okButton.addActionListener(eventListener);
	availableLabel = new JLabel("Available");
	selectedLabel = new JLabel("Selected");
	
	//Add components to layout
	gba.add(cc, availableList, 0, 0, 1, 6, 1, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 0), 0, 0);
	gba.add(cc, selectedList, 2, 0, 1, 6, 1, 1, GBA.B, GBA.C, new Insets(5, 0, 5, 5), 0, 0);
	gba.add(cc, addButton, 1, 0, 1, 1, 0, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
	gba.add(cc, addAllButton, 1, 1, 1, 1, 0, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
	gba.add(cc, removeAllButton, 1, 2, 1, 1, 0, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
	gba.add(cc, removeButton, 1, 3, 1, 1, 0, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
	gba.add(cc, cancelButton, 1, 4, 1, 1, 0, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
	gba.add(cc, okButton, 1, 6, 1, 1, 0, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
	gba.add(cc, availableLabel, 0, 6, 1, 1, 0, 0, GBA.N, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
	gba.add(cc, selectedLabel, 2, 6, 1, 1, 0, 0, GBA.N, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
	pack();
    }
    
    public boolean hasSelectedItems() {
	if (selectedItems.length < 1) return false;
	else return true;
    }
    
    public String[] getSelectedItems() {return this.selectedItems;}
    
    public void addActionListener(ActionListener al) {listeners = AWTEventMulticaster.add(listeners, al);}
    public void removeActionListener(ActionListener al) {listeners = AWTEventMulticaster.remove(listeners, al);}
    
    private class EventListener implements ActionListener, WindowListener {
	public void actionPerformed(ActionEvent event) {
	    //Do the lists need to be sorted with each action?
	    
	    if (event.getSource() == addButton) {
		String[] chosen = availableList.getSelectedItems();
		for (int i = 0; i < chosen.length; i++) {
		    availableList.remove(chosen[i]);
		    selectedList.add(chosen[i]);
		}
	    } else if (event.getSource() == addAllButton) {
		String[] available = availableList.getItems();
		for (int i = 0; i < available.length; i++) {
		    availableList.remove(available[i]);
		    selectedList.add(available[i]);
		}
	    } else if (event.getSource() == removeAllButton) {
		String[] selected = selectedList.getItems();
		for (int i = 0; i < selected.length; i++) {
		    selectedList.remove(selected[i]);
		    availableList.add(selected[i]);
		}
	    } else if (event.getSource() == removeButton) {
		String[] chosen = selectedList.getSelectedItems();
		for (int i = 0; i < chosen.length; i++) {
		    selectedList.remove(chosen[i]);
		    availableList.add(chosen[i]);
		}
	    } else if (event.getSource() == cancelButton) {
		dispose();
	    } else if (event.getSource() == okButton) {
		String[] selected = selectedList.getItems();
		selectedItems = selected;
		dispose();
		
		if (listeners != null) listeners.actionPerformed(new ActionEvent(this, event.getID(), event.getActionCommand()));
	    }
	}
	
	public void windowClosing(WindowEvent event) {dispose();}
	public void windowOpened(WindowEvent event) {;}
	public void windowClosed(WindowEvent event) {;}
	public void windowIconified(WindowEvent event) {;}
	public void windowDeiconified(WindowEvent event) {;}
	public void windowActivated(WindowEvent event) {;}
	public void windowDeactivated(WindowEvent event) {;}
    }
}
