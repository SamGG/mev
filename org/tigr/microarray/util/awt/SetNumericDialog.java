/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: SetNumericDialog.java,v $
 * $Revision: 1.1.1.1 $
 * $Date: 2003-08-21 21:04:23 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.microarray.util.awt;

import java.awt.*;
import java.awt.event.*;
import java.util.Hashtable;
import java.util.Vector;
import javax.swing.*;
import org.tigr.util.awt.*;

public class SetNumericDialog extends ActionInfoDialog {
    protected JFrame parent;
    JLabel numberLabel;
    JTextField numberTextField;
    JComboBox numberChoice;
    JButton cancelButton, okButton;
    GBA gba;
    
    public SetNumericDialog(JFrame parent, Vector numberListVector) {
	super(parent, false);
	try {
	    this.parent = parent;
	    gba = new GBA();
	    
	    numberLabel = new JLabel("Analysis_id: ");
	    numberLabel.addKeyListener(new EventListener());
	    
	    numberTextField = new JTextField(15);
	    numberTextField.addKeyListener(new EventListener());
	    
	    numberChoice = new JComboBox();
	    numberChoice.addItemListener(new EventListener());
	    numberChoice.addKeyListener(new EventListener());
	    
	    String[] numbers;
	    for (int j = 1; j < numberListVector.size(); j++) {
		numbers = ((String[]) numberListVector.elementAt(j));
		
		for (int i = 0; i < numbers.length; i++) {
		    numberChoice.addItem(numbers[i]);
		}
	    }
	    
	    cancelButton = new JButton("Cancel");
	    cancelButton.addActionListener(new EventListener());
	    
	    okButton = new JButton("OK");
	    okButton.addActionListener(new EventListener());
	    
	    contentPane.setLayout(new GridBagLayout());
	    gba.add(contentPane, numberLabel, 0, 0, 1, 1, 0, 0, GBA.NONE, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
	    gba.add(contentPane, numberTextField, 1, 0, 1, 1, 1, 1, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
	    gba.add(contentPane, okButton, 2, 0, 1, 1, 0, 0, GBA.NONE, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
	    gba.add(contentPane, numberChoice, 1, 1, 1, 1, 1, 1, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
	    gba.add(contentPane, cancelButton, 2, 1, 1, 1, 0, 0, GBA.NONE, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
	    
	    pack();
	    setResizable(false);
	    setTitle("Select Analysis_ID");
	    numberTextField.grabFocus();
	    setLocation(250, 250);
	} catch (Exception e) {
	    System.out.println("Exception (SetNumericDialog.const()): " + e);
	}
    }
    
    class EventListener implements ActionListener, ItemListener, KeyListener {
	public void actionPerformed(ActionEvent event) {
	    if (event.getSource() == okButton) {
		String number = numberTextField.getText();
		hide();
		
		Hashtable hash = new Hashtable();
		hash.put(new String("number"), number);
		fireEvent(new ActionInfoEvent(this, hash));
		
		dispose();
	    }
	}
	
	public void itemStateChanged(ItemEvent event) {
	    if (event.getSource() == numberChoice) {
		numberTextField.setText((String) numberChoice.getSelectedItem());
	    }
	}
	
	public void keyPressed(KeyEvent event) {
	    if (event.getKeyCode() == KeyEvent.VK_ENTER) {
		String number = numberTextField.getText();
		hide();
		
		Hashtable hash = new Hashtable();
		hash.put(new String("number"), number);
		fireEvent(new ActionInfoEvent(this, hash));
		
		dispose();
	    }
	}
	
	public void keyReleased(KeyEvent event) {;}
	public void keyTyped(KeyEvent event) {;}
    }
}