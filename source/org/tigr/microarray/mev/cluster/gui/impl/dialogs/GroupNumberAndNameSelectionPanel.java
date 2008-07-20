/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * Created on Jan 11, 2007
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.tigr.microarray.mev.cluster.gui.impl.dialogs;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;

/**
 * @author braisted
 *
 * Selects the number of groups and the group names.  Handles one
 * and two factor scenarios.
 *
 */
public class GroupNumberAndNameSelectionPanel extends JPanel implements
		IWizardParameterPanel {

	//boolean for one or two factor designs
	private boolean isOneFactor;	
	//boolean for fixed for variable group counts (2 or n groups)
	private boolean isVarGroupCount;
	//paramters
	private AlgorithmData algData;
	//group names fields
	private JTextField [] groupNameFields; 
	//number of groups
	private JTextField countField;
	//number of factor a levels
	private JTextField countAField;	
	//number of factor b levels
	private JTextField countBField;	
	//group names text field vector
	protected Vector textFieldVector;
	//factor A text field vector names
	private Vector factorATextFieldVector;	
	//factor B text field vector names
	private Vector factorBTextFieldVector;
	//factor A name field
	private JTextField factorAField;
	//factor b name field
	private JTextField factorBField;
	//panels to hold factor a and b names
	private JPanel factorANamePanel;
	private JPanel factorBNamePanel;
	//group names
	private JPanel groupNamePanel;
	//minimum number of groups
	private int minGroupCnt;
	//listener for update events
	private Listener listener;
	//the parent stat process wizard, possibly null
	private StatProcessWizard wiz;
	
	private JSpinner factorALevelCountSpinner;
	private JSpinner factorBLevelCountSpinner;
	private int currFactorACount;
	private int currFactorBCount;
	
	private boolean mevWhiteBackground;
	
	/**
	 * Basic constructor
	 */
	public GroupNumberAndNameSelectionPanel(boolean whiteBackground) { 
		super(new GridBagLayout()); 
		listener = new Listener();	
		mevWhiteBackground = whiteBackground;
		//prepare for 1 or two factor controls
		factorALevelCountSpinner = new JSpinner(new SpinnerNumberModel(2, 2, 20, 1));
		factorALevelCountSpinner.addChangeListener(listener);
		factorBLevelCountSpinner = new JSpinner(new SpinnerNumberModel(2, 2, 20, 1));
		factorBLevelCountSpinner.addChangeListener(listener);
	}
			

	/**
	 * Constuctor
	 * @param paramData AlgorithmData to populate
	 * @param w StatProcessWizard parent, possibly null
	 */public GroupNumberAndNameSelectionPanel(AlgorithmData paramData, StatProcessWizard w, boolean whiteBackground) {
		super(new GridBagLayout());
		algData = paramData;
		wiz = w;
		mevWhiteBackground = whiteBackground;
		
		listener = new Listener();	
		
		//prepare for 1 or two factor controls
		factorALevelCountSpinner = new JSpinner(new SpinnerNumberModel(2, 2, 20, 1));
		factorALevelCountSpinner.addChangeListener(listener);
		factorBLevelCountSpinner = new JSpinner(new SpinnerNumberModel(2, 2, 20, 1));
		factorBLevelCountSpinner.addChangeListener(listener);
	}
	 
	/**
	 * Constructor
	 * @param isOneFactor false if for two factor
	 * @param isVariableGroupCount can group count vary or exactly two groups
	 * @param minNumGroups 
	 * @param paramData AlgorithmData parameters to modify
	 * @param w StatProcessWizard parent, possibly null
	 */
	public GroupNumberAndNameSelectionPanel(boolean isOneFactor, boolean isVariableGroupCount, int minNumGroups, AlgorithmData paramData, StatProcessWizard w, boolean whiteBackground) {
		super(new GridBagLayout());
		mevWhiteBackground = whiteBackground;
		
		initialize(isOneFactor, isVarGroupCount, minNumGroups);
		algData = paramData;
		wiz = w;
		
		currFactorACount = currFactorBCount = minNumGroups;
		this.minGroupCnt = minNumGroups;		
		
		listener = new Listener();	
		
		//prepare for 1 or two factor controls
		factorALevelCountSpinner = new JSpinner(new SpinnerNumberModel(2, 2, 20, 1));
		factorALevelCountSpinner.addChangeListener(listener);
		factorBLevelCountSpinner = new JSpinner(new SpinnerNumberModel(2, 2, 20, 1));
		factorBLevelCountSpinner.addChangeListener(listener);
	}

	/**
	 * Initializes the panel
	 */
	private void initialize() {
		this.initialize(isOneFactor, isVarGroupCount, minGroupCnt);
	}
	
	/**
	 * Initializes the panel base on params
	 * @param isOneFactor true if only one factor
	 * @param isVariableGroupCount true for two way or one way with n groups
	 * @param minNumGroups minimun number of groups
	 */
	public void initialize(boolean isOneFactor, boolean isVariableGroupCount, int minNumGroups) {
		this.removeAll();
		
		textFieldVector = new Vector();	
		this.isOneFactor = isOneFactor;
		this.isVarGroupCount = isVariableGroupCount;
		
		currFactorACount = currFactorBCount = minNumGroups;
		this.minGroupCnt = minNumGroups;
		factorALevelCountSpinner.setValue(new Integer(minNumGroups));
		factorBLevelCountSpinner.setValue(new Integer(minNumGroups));
		
		if(!isOneFactor) {
			initTwoFactorPanel();
		} else {
			initOneFactorPanel();			
		} 
		validate();
	}

	/**
	 * Initializes a one factor panel
	 */
	private void initOneFactorPanel() {
		
		//remove any components in case this isn't new 'initialization' :)
		this.removeAll();
		
		if(mevWhiteBackground)
			this.setBackground(Color.white);
		
		textFieldVector = new Vector();
		JPanel groupCountPanel;
		groupNamePanel = new JPanel(new GridBagLayout());
		groupNamePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "Select Group Names"));
		
		if(mevWhiteBackground)
			groupNamePanel.setBackground(Color.white);
		
		if(isVarGroupCount) {
			groupCountPanel = new JPanel(new GridBagLayout());
			groupCountPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "Select Number of Groups"));

			if(mevWhiteBackground)
				groupCountPanel.setBackground(Color.white);
			
			JLabel countLabel = new JLabel("Number of Groups:");
			countLabel.setOpaque(false);
			
			countField = new JTextField(String.valueOf(minGroupCnt));
			JButton countUpdateButton = new JButton("Update Count");
			countUpdateButton.addActionListener(listener);			
			groupCountPanel.add(countLabel, new GridBagConstraints(0,0,1,1,1,1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10,10,5,5),0,0));
			groupCountPanel.add(factorALevelCountSpinner, new GridBagConstraints(1,0,1,1,1,1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10,10,5,5),0,0));
			
			//groupCountPanel.add(countField, new GridBagConstraints(1,0,1,1,1,1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10,10,5,5),0,0));
			//groupCountPanel.add(countUpdateButton, new GridBagConstraints(0,1,2,1,1,1, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10,10,5,5),0,0));

			for(int i = 0; i < currFactorACount; i++) {
				JLabel group1Label = new JLabel("Group "+String.valueOf(i+1)+" Name:");
				group1Label.setOpaque(false);
				group1Label.setPreferredSize(new Dimension(100, 25));
				group1Label.setMinimumSize(new Dimension(100,25));
				group1Label.setMaximumSize(new Dimension(100,25));
				
				JTextField group1Field = new JTextField("Group "+String.valueOf(i+1));
				group1Field.setPreferredSize(new Dimension(100, 25));
				group1Field.setMinimumSize(new Dimension(100,25));
				group1Field.setMaximumSize(new Dimension(100,25));
				
				groupNamePanel.add(group1Label, new GridBagConstraints(0,i,1,1,1,1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10,15,5,10),0,0));			
				groupNamePanel.add(group1Field, new GridBagConstraints(1,i,1,1,1,1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(10,0,5,10),0,0));
				textFieldVector.add(group1Field);
			}
			add(groupCountPanel, new GridBagConstraints(0,0,1,1,1,0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
			add(groupNamePanel, new GridBagConstraints(0,1,1,1,1,1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
		} else {			
			//two groups			
			JLabel group1Label = new JLabel("Group 1 Name:");
			group1Label.setOpaque(false);
			JLabel group2Label = new JLabel("Group 2 Name:");
			group2Label.setOpaque(false);
			JTextField group1Field = new JTextField("Group 1");			
			JTextField group2Field = new JTextField("Group 2");		
			textFieldVector.add(group1Field);
			textFieldVector.add(group2Field);			
			groupNamePanel.add(group1Label, new GridBagConstraints(0,0,1,1,0,0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(10,15,25,10),0,0));
			groupNamePanel.add(group1Field, new GridBagConstraints(1,0,1,1,1,0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(10,0,25,15),0,0));
			groupNamePanel.add(group2Label, new GridBagConstraints(0,1,1,1,0,0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0,15,20,10),0,0));
			groupNamePanel.add(group2Field, new GridBagConstraints(1,1,1,1,1,0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0,0,20,15),0,0));		
			add(groupNamePanel, new GridBagConstraints(0,0,1,1,1,1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));		
		}

	}

	
	/**
	 * Initialize for two factors
	 */
	private void initTwoFactorPanel() {
		
		//remove any components in case this isn't new 'initialization' :)
		this.removeAll();	
		JLabel factorALabel = new JLabel("Factor A Name:");
		factorAField = new JTextField("Factor A");
		factorAField.setPreferredSize(new Dimension(100, 25));
		factorAField.setMinimumSize(new Dimension(100,25));
		factorAField.setMaximumSize(new Dimension(100,25));

		JLabel factorBLabel = new JLabel("Factor B Name:");
		factorBField = new JTextField("Factor B");
		factorBField.setPreferredSize(new Dimension(100, 25));
		factorBField.setMinimumSize(new Dimension(100,25));
		factorBField.setMaximumSize(new Dimension(100,25));

		factorATextFieldVector = new Vector();
		factorBTextFieldVector = new Vector();
				
		JPanel factorACountPanel;
				
		factorANamePanel = new JPanel(new GridBagLayout());
		factorANamePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "Factor A Level Names"));
		
		factorACountPanel = new JPanel(new GridBagLayout());
		factorACountPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "Factor A"));
		
		JLabel countLabel = new JLabel("Number of Levels:");
		countAField = new JTextField(String.valueOf(minGroupCnt));
		
		JButton countUpdateButton = new JButton("Update Count");
		countUpdateButton.setActionCommand("factor-A-count-update-command");
		countUpdateButton.addActionListener(listener);

		factorACountPanel.add(factorALabel, new GridBagConstraints(0,0,1,1,1,1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10,10,5,5),0,0));
		factorACountPanel.add(factorAField, new GridBagConstraints(1,0,1,1,1,1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10,10,5,5),0,0));

		factorACountPanel.add(countLabel, new GridBagConstraints(0,1,1,1,1,1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10,10,5,5),0,0));
		
		factorACountPanel.add(factorALevelCountSpinner, new GridBagConstraints(1,1,1,1,1,1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10,10,5,5),0,0));
		
		//factorACountPanel.add(countAField, new GridBagConstraints(1,1,1,1,1,1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10,10,5,5),0,0));
		//factorACountPanel.add(countUpdateButton, new GridBagConstraints(0,2,2,1,1,1, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10,10,5,5),0,0));
		
		for(int i = 0; i < currFactorACount; i++) {
			JLabel group1Label = new JLabel("Level "+String.valueOf(i+1)+" Name:");
			group1Label.setPreferredSize(new Dimension(150, 25));
			group1Label.setMinimumSize(new Dimension(150,25));
			group1Label.setMaximumSize(new Dimension(150,25));
			group1Label.setOpaque(false);
			
			JTextField group1Field = new JTextField("Level "+String.valueOf(i+1));
			group1Field.setPreferredSize(new Dimension(100, 25));
			group1Field.setMinimumSize(new Dimension(100,25));
			group1Field.setMaximumSize(new Dimension(100,25));
			
			factorANamePanel.add(group1Label, new GridBagConstraints(0,i,1,1,1,1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10,10,5,5),0,0));
			
			factorANamePanel.add(group1Field, new GridBagConstraints(1,i,1,1,1,1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(10,0,5,10),0,0));
			factorATextFieldVector.add(group1Field);
		}
				
		JPanel factorBCountPanel;
		
		factorBNamePanel = new JPanel(new GridBagLayout());
		factorBNamePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "Factor B Level Names"));
		
		factorBCountPanel = new JPanel(new GridBagLayout());
		factorBCountPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "Factor B"));
		
		countLabel = new JLabel("Number of Levels:");
		countBField = new JTextField(String.valueOf(minGroupCnt));
		
		countUpdateButton = new JButton("Update Count");
		countUpdateButton.setActionCommand("factor-B-count-update-command");
		countUpdateButton.addActionListener(listener);

		factorBCountPanel.add(factorBLabel, new GridBagConstraints(0,0,1,1,1,1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10,10,5,5),0,0));
		factorBCountPanel.add(factorBField, new GridBagConstraints(1,0,1,1,1,1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10,10,5,5),0,0));
		
		factorBCountPanel.add(countLabel, new GridBagConstraints(0,1,1,1,1,1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10,10,5,5),0,0));
		factorBCountPanel.add(factorBLevelCountSpinner, new GridBagConstraints(1,1,1,1,1,1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10,10,5,5),0,0));
		
		//factorBCountPanel.add(countBField, new GridBagConstraints(1,1,1,1,1,1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10,10,5,5),0,0));
		//factorBCountPanel.add(countUpdateButton, new GridBagConstraints(0,2,2,1,1,1, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10,10,5,5),0,0));
		
		for(int i = 0; i < currFactorBCount; i++) {
			JLabel group1Label = new JLabel("Level "+String.valueOf(i+1)+" Name:");
			group1Label.setPreferredSize(new Dimension(150, 25));
			group1Label.setMinimumSize(new Dimension(150,25));
			group1Label.setMaximumSize(new Dimension(150,25));
			group1Label.setOpaque(false);
			
			JTextField group1Field = new JTextField("Level "+String.valueOf(i+1));
			group1Field.setPreferredSize(new Dimension(100, 25));
			group1Field.setMinimumSize(new Dimension(100,25));
			group1Field.setMaximumSize(new Dimension(100,25));
			
			factorBNamePanel.add(group1Label, new GridBagConstraints(0,i,1,1,1,1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10,10,5,5),0,0));
			
			factorBNamePanel.add(group1Field, new GridBagConstraints(1,i,1,1,1,1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(10,0,5,10),0,0));
			factorBTextFieldVector.add(group1Field);
		}

		if(mevWhiteBackground) {
			factorACountPanel.setBackground(Color.white);
			factorANamePanel.setBackground(Color.white);
			factorBCountPanel.setBackground(Color.white);
			factorBNamePanel.setBackground(Color.white);
		}

		
		add(factorACountPanel, new GridBagConstraints(0,0,1,1,1,0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,5,0,5),0,0));
		add(factorANamePanel, new GridBagConstraints(0,1,1,1,1,1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,5,5,5),0,0));							
		add(factorBCountPanel, new GridBagConstraints(1,0,1,1,1,0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,5,0,5),0,0));
		add(factorBNamePanel, new GridBagConstraints(1,1,1,1,1,1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,5,5,5),0,0));							

	}
	
	/**
	 * Update the panel to reflect the group count
	 *
	 */
	private void updatePanel() {		
		currFactorACount = ((Integer)(factorALevelCountSpinner.getValue())).intValue();
		currFactorBCount = ((Integer)(factorBLevelCountSpinner.getValue())).intValue();
		
		initOneFactorPanel();
		validate();
		if(wiz != null)
			wiz.updateWizard();
	}
	
	
	/**
	 * Triggers the panel to capture the current data
	 */
 	public void populateAlgorithmData() {
		//get group names
 		if(isOneFactor) {
 			String [] groupNames = new String[textFieldVector.size()+1];		
 			for(int i = 0; i < groupNames.length-1; i++)
 				groupNames[i] = ((JTextField)(textFieldVector.get(i))).getText();
 			
 			//add an option to exclude a sample
 			groupNames[groupNames.length-1] = "Exclude";				
 			algData.addStringArray("group-names", groupNames);
 		} else {
 			
			algData.addParam("factor-A-name", factorAField.getText());
			algData.addParam("factor-B-name", factorBField.getText());

			String [] factorLabels = new String[this.factorATextFieldVector.size()];
			for(int i = 0; i < factorATextFieldVector.size(); i++) {
				factorLabels[i] = ((JTextField)(factorATextFieldVector.get(i))).getText();
			}
			
			algData.addStringArray("factor-A-level-names", factorLabels);

			factorLabels = new String[this.factorBTextFieldVector.size()];
			for(int i = 0; i < factorBTextFieldVector.size(); i++) {
				factorLabels[i] = ((JTextField)(factorBTextFieldVector.get(i))).getText();
			}

			algData.addStringArray("factor-B-level-names", factorLabels);
			
		}
 	}

 	/**
 	 * Clears the values from algorithm data
 	 */
 	public void clearValuesFromAlgorithmData() {
 		algData.getParams().getMap().remove("factor-A-name");
 		algData.getParams().getMap().remove("factor-B-name");
 		algData.getParams().getMap().remove("factor-A-level-names");
 		algData.getParams().getMap().remove("factor-B-level-names"); 		
	}

 	
 	public void onDisplayed() {
		if(wiz != null)
			wiz.updateWizard();
 	}
 	
 	/**
 	 * Updates a two factor panel 
 	 * @param panelIndex factor panel index to update
 	 * @param numFields number of feilds to display
 	 */
 	public void updateTwoFactorPanel(int panelIndex, int numFields) {
 		if(panelIndex == 0) {
 			remove(this.factorANamePanel);
 			factorATextFieldVector = new Vector();
 			factorANamePanel = new JPanel(new GridBagLayout());
 			factorANamePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "Factor A Level Names"));
 			
 			for(int i = 0; i < numFields; i++) {
 				
 				JLabel group1Label = new JLabel("Level "+String.valueOf(i+1)+" Name:");
 				group1Label.setPreferredSize(new Dimension(150, 25));
 				group1Label.setMinimumSize(new Dimension(150,25));
 				group1Label.setMaximumSize(new Dimension(150,25));
 				
 				JTextField group1Field = new JTextField("Level "+String.valueOf(i+1));
 				group1Field.setPreferredSize(new Dimension(100, 25));
 				group1Field.setMinimumSize(new Dimension(100,25));
 				group1Field.setMaximumSize(new Dimension(100,25));
 				
 				factorANamePanel.add(group1Label, new GridBagConstraints(0,i,1,1,1,1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10,10,5,5),0,0));
 				
 				factorANamePanel.add(group1Field, new GridBagConstraints(1,i,1,1,1,1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(10,0,5,10),0,0));
 				factorATextFieldVector.add(group1Field);
 			}
 			
 			add(factorANamePanel, new GridBagConstraints(0,1,1,1,1,1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,5,5,5),0,0));							
 		} else {
 			remove(this.factorBNamePanel);
 			factorBTextFieldVector = new Vector(); 			
 			factorBNamePanel = new JPanel(new GridBagLayout());
 			factorBNamePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "Factor B Level Names"));
 			
 			for(int i = 0; i < numFields; i++) {
 				JLabel group1Label = new JLabel("Level "+String.valueOf(i+1)+" Name:");
 				group1Label.setPreferredSize(new Dimension(150, 25));
 				group1Label.setMinimumSize(new Dimension(150,25));
 				group1Label.setMaximumSize(new Dimension(150,25));
 				
 				JTextField group1Field = new JTextField("Level "+String.valueOf(i+1));
 				group1Field.setPreferredSize(new Dimension(100, 25));
 				group1Field.setMinimumSize(new Dimension(100,25));
 				group1Field.setMaximumSize(new Dimension(100,25));
 				
 				factorBNamePanel.add(group1Label, new GridBagConstraints(0,i,1,1,1,1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10,10,5,5),0,0));
 				
 				factorBNamePanel.add(group1Field, new GridBagConstraints(1,i,1,1,1,1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(10,0,5,10),0,0));
 				factorBTextFieldVector.add(group1Field);
 			}
 			add(factorBNamePanel, new GridBagConstraints(1,1,1,1,1,1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,5,5,5),0,0));							 			
 		}
 		validate();
 		if(wiz != null)
 			wiz.updateWizard();
 	}
 	
 	public boolean areGroupAndFactorNamesUnique() {
 		boolean unique = true;
 		
 		if(this.isOneFactor) {
 			String [] groupNames = new String[textFieldVector.size()+1];		
 			for(int i = 0; i < groupNames.length-1; i++)
 				groupNames[i] = ((JTextField)(textFieldVector.get(i))).getText();
 			
 			//add an option to exclude a sample
 			groupNames[groupNames.length-1] = "Exclude";				
 		
 			for(int i = 0; i < groupNames.length; i++)
 				for(int j = i+1; j < groupNames.length; j++)
 					if(groupNames[i].equals(groupNames[j]))
 						return false;
 		
 		} else {
 			//two factor case... need to check factor names and factor level names
 			
 			if(this.factorAField.getText().equals(this.factorBField.getText())) {
 				return false;
 			}

 			for(int i = 0; i < factorATextFieldVector.size(); i++)
 				for(int j = i+1; j < factorATextFieldVector.size(); j++)
 					if(((JTextField)(factorATextFieldVector.get(i))).getText().equals(((JTextField)(factorATextFieldVector.get(j))).getText()))
 						return false;

 			for(int i = 0; i < factorBTextFieldVector.size(); i++)
 				for(int j = i+1; j < factorBTextFieldVector.size(); j++)
 					if(((JTextField)(factorBTextFieldVector.get(i))).getText().equals(((JTextField)(factorBTextFieldVector.get(j))).getText()))
 						return false;
 		}
 		
 		return unique;
 	}
 	
 	
 	/**
 	 * 
 	 * @author braisted
 	 *
 	 * The listener
 	 */
	private class Listener implements ActionListener, ChangeListener {

		public void actionPerformed(ActionEvent e) {
			if(isOneFactor)
				updatePanel();
				//initOneFactorPanel();
			else {
				if(e.getActionCommand().equals("factor-A-count-update-command"))
					updateTwoFactorPanel(0, ((Integer)(factorALevelCountSpinner.getValue())).intValue());
				else
					updateTwoFactorPanel(1, ((Integer)(factorBLevelCountSpinner.getValue())).intValue());				
			}
		}

		
		public void stateChanged(ChangeEvent e) {		
			if(isOneFactor) {
				currFactorACount = ((Integer)(factorALevelCountSpinner.getValue())).intValue();
				//initOneFactorPanel();
				updatePanel();
			} else { 
				if(e.getSource() == factorALevelCountSpinner)					
					updateTwoFactorPanel(0, ((Integer)(factorALevelCountSpinner.getValue())).intValue());
				else
					updateTwoFactorPanel(1, ((Integer)(factorBLevelCountSpinner.getValue())).intValue());				
			}
		}
	}
	
	public static void main(String[] args) {
		JFrame frame = new JFrame();		
		GroupNumberAndNameSelectionPanel p = new GroupNumberAndNameSelectionPanel(true);
		p.initialize(true, true, 2);		
		frame.getContentPane().add(p);		
		frame.setSize(600, 500);		
		frame.setVisible(true);
	}
}
