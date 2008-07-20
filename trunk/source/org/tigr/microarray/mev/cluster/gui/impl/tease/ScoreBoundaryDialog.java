/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * Created on Sep 2, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.tigr.microarray.mev.cluster.gui.impl.tease;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener;

/**
 * @author Annie Liu
 * @version Sep 2, 2005
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ScoreBoundaryDialog extends AlgorithmDialog {
	private int result;
	private JTextField upperField;
	private JTextField lowerField;

	public ScoreBoundaryDialog(Frame frame, double upperBound, double lowerBound){
		super(frame, "Score Boundary", true);
		
		JPanel panel = new JPanel(new GridBagLayout());
		JLabel upperLabel = new JLabel("Upper bound score (Blue -> not significant)");
		JLabel lowerLabel = new JLabel("Lower bound score (Red -> significant)");
		this.upperField = new JTextField(String.valueOf(upperBound), 8);
		this.lowerField = new JTextField(String.valueOf(lowerBound), 8);
		
        panel.add(upperLabel, new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.CENTER,
        		GridBagConstraints.BOTH, new Insets(5,0,5,0),0,0));
        panel.add(this.upperField, new GridBagConstraints(1,0,1,1,0,0,GridBagConstraints.CENTER,
        		GridBagConstraints.BOTH, new Insets(0,0,5,0),0,0));
        panel.add(lowerLabel, new GridBagConstraints(0,1,1,1,0,0,GridBagConstraints.CENTER,
        		GridBagConstraints.BOTH, new Insets(0,0,5,0),0,0));
        panel.add(this.lowerField, new GridBagConstraints(1,1,1,1,0,0,GridBagConstraints.CENTER,
        		GridBagConstraints.BOTH, new Insets(0,0,5,0),0,0));
        addContent(panel);
        setActionListeners(new Listener());
        this.pack();
	}
	
    /**
     * Show the dialog in screen's center.
     */
    public int showModal() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);
        show();
        return result;
    }
	
	public String getUpperBound() {
		return this.upperField.getText();
	}
	
	public String getLowerBound() {
		return this.lowerField.getText();
	}
	
	//*****************************INNER CLASS***************************************************//
	
	private class Listener extends DialogListener {
		
		public void actionPerformed(ActionEvent e) {
			if (e.getActionCommand() == "ok-command") {
				result = JOptionPane.OK_OPTION;
				dispose();
			}
			if (e.getActionCommand() == "cancel-command") {
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
