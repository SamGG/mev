/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: SetGreenRedThresholdDialog.java,v $
 * $Revision: 1.3 $
 * $Date: 2005-03-10 15:44:14 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.microarray.mev;

import java.awt.*;
import java.awt.event.*;
import java.util.Hashtable;
import javax.swing.*;
import org.tigr.util.awt.*;

public class SetGreenRedThresholdDialog extends ActionInfoDialog {
    private JFrame parent;
    JLabel factorLabel;
    JTextField factorTextField;
    JButton okButton, cancelButton;
    GBA gba;
    
    public SetGreenRedThresholdDialog(JFrame parent, Double factor) {
	super(parent, true);
	
	try {
	    this.parent = parent;
	    gba = new GBA();
	    
	    factorLabel = new JLabel("Factor (" + factor + "): ");
	    factorLabel.addKeyListener(new EventListener());
	    
	    factorTextField = new JTextField(10);
	    factorTextField.setText("" + factor);
	    factorTextField.addKeyListener(new EventListener());
	    
	    okButton = new JButton("Okay");
	    okButton.addActionListener(new EventListener());
	    
	    cancelButton = new JButton("Cancel");
	    cancelButton.addActionListener(new EventListener());
	    
	    contentPane.setLayout(new GridBagLayout());
	    gba.add(contentPane, factorLabel, 0, 0, 1, 1, 0, 0, GBA.NONE, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
	    gba.add(contentPane, factorTextField, 1, 0, 1, 1, 1, 1, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
	    gba.add(contentPane, cancelButton, 0, 1, 1, 1, 0, 0, GBA.NONE, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
	    gba.add(contentPane, okButton, 1, 1, 1, 1, 0, 0, GBA.NONE, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
	    
	    pack();
	    setResizable(false);
	    setTitle("Set Expression Ratio");
	    factorTextField.grabFocus();
	    setLocation(200,200);
	} catch (Exception e) {
	    System.out.println("Exception (SetGreenRedThresholdDialog.const()): " + e);
	}
    }
    
    class EventListener implements ActionListener, KeyListener {
	public void actionPerformed(ActionEvent event) {
	    if (event.getSource() == okButton) {
		String factorString = factorTextField.getText();
		//parent.getSlideApplet().setGreenRedThreshold(factorString);
		Hashtable hash = new Hashtable();
		hash.put(new String("factor"), factorString);
		fireEvent(new ActionInfoEvent(this, hash));
		dispose();
	    } else if (event.getSource() == cancelButton) dispose();
	}
	
	public void keyPressed(KeyEvent event) {
	    if (event.getKeyCode() == KeyEvent.VK_ENTER) {
		String factorString = factorTextField.getText();
		//parent.getSlideApplet().setGreenRedThreshold(factorString);
		Hashtable hash = new Hashtable();
		hash.put(new String("factor"), factorString);
		fireEvent(new ActionInfoEvent(this, hash));
		dispose();
	    }
	}
	
	public void keyReleased(KeyEvent event) {;}
	public void keyTyped(KeyEvent event) {;}
    }
}