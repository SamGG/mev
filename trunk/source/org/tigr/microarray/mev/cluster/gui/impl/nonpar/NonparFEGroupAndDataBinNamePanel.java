/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * Created on May 25, 2007
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.tigr.microarray.mev.cluster.gui.impl.nonpar;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.GroupNumberAndNameSelectionPanel;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.IWizardParameterPanel;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.StatProcessWizard;


/**
 * @author braisted
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class NonparFEGroupAndDataBinNamePanel extends
		GroupNumberAndNameSelectionPanel implements IWizardParameterPanel {

	private JTextField binOneNameField;
	private JTextField binTwoNameField;
	private AlgorithmData aData;
	
	
	public NonparFEGroupAndDataBinNamePanel(AlgorithmData algData, StatProcessWizard w) {
		super(algData, w, false);
		aData = algData;
		
		initialize(true, false, 2);
		
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "Select Data Bin Names"));
		
		//have main controls, need to add data bin controls
		JLabel label = new JLabel("Data Bin 1 Label:");
		panel.add(label, new GridBagConstraints(0,0,1,1,0,0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(10,15,25,10),0,0));
		binOneNameField = new JTextField("Bin 1");	
		Dimension fieldDimension = new Dimension(80,20);
		binOneNameField.setPreferredSize(fieldDimension);
		panel.add(binOneNameField, new GridBagConstraints(1,0,1,1,1,0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(10,0,25,15),0,0));
		
		label = new JLabel("Data Bin 2 Label:");
		panel.add(label, new GridBagConstraints(0,1,1,1,0,0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(10,15,25,10),0,0));
		binTwoNameField = new JTextField("Bin 2");
		binTwoNameField.setPreferredSize(fieldDimension);
		panel.add(binTwoNameField, new GridBagConstraints(1,1,1,1,1,0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(10,0,25,15),0,0));	

		add(panel, new GridBagConstraints(0,1,1,1,1,1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));					
	}
	
	public NonparFEGroupAndDataBinNamePanel() {
		super(false);
		initialize(true, false, 2);
		
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "Select Data Bin Names"));
		
		//have main controls, need to add data bin controls
		JLabel label = new JLabel("Data Bin 1 Label:");
		panel.add(label, new GridBagConstraints(0,0,1,1,0,0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(10,15,25,10),0,0));
		binOneNameField = new JTextField("Bin 1");
		Dimension fieldDimension = new Dimension(80,25);
		binOneNameField.setPreferredSize(fieldDimension);
		panel.add(binOneNameField, new GridBagConstraints(1,0,1,1,1,0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(10,0,25,15),0,0));
		
		label = new JLabel("Data Bin 2 Label:");
		panel.add(label, new GridBagConstraints(0,1,1,1,0,0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(10,15,25,10),0,0));
		binTwoNameField = new JTextField("Bin 2");		
		binTwoNameField.setPreferredSize(fieldDimension);
		panel.add(binTwoNameField, new GridBagConstraints(1,1,1,1,1,0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(10,0,25,15),0,0));	

		add(panel, new GridBagConstraints(0,1,1,1,1,1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));		
	}
	
	public void populateAlgorithmData() {
		//add group names
		super.populateAlgorithmData();
		
		String [] binNames = new String[2];
		binNames[0] = binOneNameField.getText();
		binNames[1] = binTwoNameField.getText();
		
		//add data bin names
		aData.addStringArray("fisher-exact-bin-names", binNames);
	}

	public void clearValuesFromAlgorithmData() {
		//clear group names
		super.clearValuesFromAlgorithmData();
		//remove bin names
		aData.getParams().getMap().remove("fisher-exact-bin-names");
	}
	
	
	/*
	 * Intercepts the super's method to verify group and data bin names
	 */
	public boolean areGroupAndFactorNamesUnique() {
		boolean unique = true;
				
		String [] groupNames = new String[textFieldVector.size()+1];		
		for(int i = 0; i < groupNames.length-1; i++)
			groupNames[i] = ((JTextField)(textFieldVector.get(i))).getText();
		
		//add an option to exclude a sample
		groupNames[groupNames.length-1] = "Exclude";				
		
		for(int i = 0; i < groupNames.length; i++) {
			for(int j = i+1; j < groupNames.length; j++) {
				if(groupNames[i].equals(groupNames[j]))
					return false;
			}
		}
		
		//check data bin names
		String binOneName = binOneNameField.getText();
		String binTwoName = binTwoNameField.getText();

		if(binOneName == null || binTwoName == null ||
				binOneName.equals(binTwoName))
			return false;
												
		return unique;
	}
	
	public static void main(String[] args) {
		NonparFEGroupAndDataBinNamePanel p = new NonparFEGroupAndDataBinNamePanel();
		p.setVisible(true);
		
		JFrame frame = new JFrame();
		frame.getContentPane().add(p);
		frame.setSize(300,450);		
		//frame.getContentPane().add(p);
//		frame.setVisible(true);
		frame.setVisible(true);
	}
}
