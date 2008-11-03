/*******************************************************************************
 * Copyright (c) 1999-2005, The Institute for Genomic Research (TIGR).
 * Copyright (c) 1999, 2008, the Dana-Farber Cancer Institute (DFCI), 
 * the J. Craig Science Foundataion (JCVI), and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * AnalysisSaveDialog.java
 *
 * Created on January 29, 2004, 4:06 PM
 */

package org.tigr.microarray.mev.resources;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener;

/**
 *
 * @author  eleanorahowe
 */
public class AllowConnectionsDialog extends org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog {

	int result = JOptionPane.NO_OPTION;
	JCheckBox rememberSelectionBox;

	/** Creates a new instance of AnalysisSaveDialog */
	public AllowConnectionsDialog(JFrame frame) {
		super(frame, "Allow connection to the internet", true);
		ActionListener listener = new Listener();
		addWindowListener((WindowListener)listener);

		JPanel buttonPanel = new JPanel(new GridBagLayout());
		JButton okButton = new JButton("Allow");
		okButton.setFocusPainted(false);
		okButton.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		okButton.setPreferredSize(new Dimension(50, 30));
		okButton.setActionCommand("yes");
		okButton.addActionListener(listener);

		JButton noButton = new JButton("Forbid");
		noButton.setFocusPainted(false);
		noButton.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		noButton.setPreferredSize(new Dimension(50, 30));
		noButton.setActionCommand("no");
		noButton.addActionListener(listener);

		buttonPanel.add(okButton, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 20),
				0, 0));
		buttonPanel.add(noButton, new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 20, 0, 0),
				0, 0));

		JPanel paramPanel = new JPanel(new GridBagLayout());
		paramPanel.setBackground(Color.white);
		paramPanel.setBorder(BorderFactory.createLineBorder(Color.black));
		JLabel promptLabel = new JLabel("MeV would like to connect to the internet.");
		promptLabel.setHorizontalAlignment(JLabel.CENTER);
		JLabel promptLabel2 = new JLabel("Connections are anonymous and allow MeV to offer enhanced functionality. ");
		promptLabel2.setHorizontalAlignment(JLabel.CENTER);
		JLabel promptLabel3 = new JLabel("Please see the manual for more details.");
		promptLabel3.setHorizontalAlignment(JLabel.CENTER);
		JLabel promptLabel4 = new JLabel("Allow MeV to connect?");
		promptLabel4.setHorizontalAlignment(JLabel.CENTER);
		JLabel directionsLabel = new JLabel("You can change this setting in the main menubar's Preferences menu.");
		directionsLabel.setHorizontalAlignment(JLabel.CENTER);

		rememberSelectionBox = new JCheckBox("Always use this option", false);
		rememberSelectionBox.setBackground(Color.white);
		rememberSelectionBox.setFocusPainted(false);

		paramPanel.add(promptLabel, 			new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 0, 5), 0, 0));
		paramPanel.add(promptLabel2, 			new GridBagConstraints(0, 1, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 0, 5), 0, 0));
		paramPanel.add(promptLabel3, 			new GridBagConstraints(0, 2, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 0, 5), 0, 0));
		paramPanel.add(promptLabel4, 			new GridBagConstraints(0, 3, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 0, 5), 0, 0));
		paramPanel.add(directionsLabel, 		new GridBagConstraints(0, 4, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 0, 5), 0, 0));
		paramPanel.add(rememberSelectionBox, 	new GridBagConstraints(0, 5, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(20, 0, 0, 0), 0, 0));

		this.supplantButtonPanel(buttonPanel);
		this.addContent(paramPanel);

		this.setSize(470, 290);
	}

	public int showModal() {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((screenSize.width - getSize().width) / 2, (screenSize.height - getSize().height) / 2);
		this.setVisible(true);
		return result;
	}

	public void disposeDialog() {
		this.dispose();
	}

	public boolean askAgain() {
		return !this.rememberSelectionBox.isSelected();
	}

	public class Listener extends DialogListener implements ActionListener {
		public void actionPerformed(ActionEvent ae) {
			String command = ae.getActionCommand();
			if (command.equals("yes"))
				result = JOptionPane.YES_OPTION;
			else
				result = JOptionPane.NO_OPTION;
			disposeDialog();
		}
	}


	public static void main(String[] args) {
		AllowConnectionsDialog dialog = new AllowConnectionsDialog(new JFrame());
		int result = dialog.showModal();
		System.out.println("result = " + result);
		System.out.println("ask again = " + dialog.askAgain());
	}

}
