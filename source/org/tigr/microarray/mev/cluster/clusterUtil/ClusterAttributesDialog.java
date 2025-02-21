/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: ClusterAttributesDialog.java,v $
 * $Revision: 1.4 $
 * $Date: 2007-12-19 21:39:36 $
 * $Author: saritanair $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.clusterUtil;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;

import org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.HelpWindow;
import org.tigr.microarray.mev.cluster.gui.impl.kmc.KMCInitDialog;
import org.tigr.microarray.util.awt.AccessibleColorPalette;


public class ClusterAttributesDialog extends AlgorithmDialog {

	JTextArea textArea;
	JLabel colorLabel;
	JTextField clusterLabelField;
	
	int result = JOptionPane.CANCEL_OPTION;

	String clusterLabelStr;
	String clusterDescriptionStr;
	Color clusterColor;
	
//Added by Sarita Nair
	JColorChooser colorChooser;
	AccessibleColorPalette colorBlind;
	

	public ClusterAttributesDialog(String frameTitle, String analysis, String clusterID){
		//Commented by sarita to include a default cluster color
		//this(frameTitle, analysis, clusterID, null, null, null);    
		this(frameTitle, analysis, clusterID, null, null, Color.GREEN);
	}


	/** Creates new ClusterSaveDialog */
	public ClusterAttributesDialog(String frameTitle, String analysis, String clusterID, String clusterLabelStr, String clusterDescription, Color clusterColor) {
		super(new JFrame(), frameTitle, true);
		this.clusterLabelStr = clusterLabelStr;
		this.clusterDescriptionStr = clusterDescription;
		//this.clusterColor = clusterColor;
		//Added by Sarita, to set a default color
		this.clusterColor=Color.GREEN;
		JPanel parameters = new JPanel();
		parameters.setLayout(new GridBagLayout());
		parameters.setBackground(Color.white);
		parameters.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

		EventListener listener = new EventListener();

		JLabel analysisName = new JLabel("Analysis Node");
		JTextField analysisField = new JTextField(analysis);
		analysisField.setEditable(false);
		analysisField.setHorizontalAlignment(SwingConstants.CENTER);

		JLabel clusterNode = new JLabel("Cluster Node");
		JTextField clusterIDField = new JTextField(clusterID);
		clusterIDField.setEditable(false);
		clusterIDField.setHorizontalAlignment(SwingConstants.CENTER);

		JLabel clusterLabel = new JLabel("Cluster Label*");
		clusterLabelField = new JTextField(15);
		if(this.clusterLabelStr != null)
			clusterLabelField.setText(this.clusterLabelStr);

		JLabel notes = new JLabel("Remarks: *");
		textArea = new JTextArea(2,30);
		textArea.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED, Color.lightGray, Color.gray));
		textArea.setMargin(new Insets(0,3,0,3));
		textArea.setSize(350,50);
		textArea.setPreferredSize(new Dimension(370,50));
		textArea.setMargin(new Insets(2,3,2,3));
		if(this.clusterDescriptionStr != null)
			textArea.setText(this.clusterDescriptionStr);

		JButton setColorButton = new JButton("Select Color");
		setColorButton.setFocusPainted(false);
		setColorButton.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED, Color.gray, Color.darkGray));
		setColorButton.setSize(80, 25);
		setColorButton.setPreferredSize(new Dimension(100,25));
		setColorButton.setActionCommand("set-color-command");
		setColorButton.addActionListener(listener);

		colorLabel = new JLabel("Preview: No Color");
		colorLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		colorLabel.setBorder(BorderFactory.createLineBorder(Color.black, 2));
		colorLabel.setBackground(Color.lightGray);
		colorLabel.setOpaque(true);
		colorLabel.setEnabled(false);

		if(this.clusterColor != null){
			colorLabel.setBackground(clusterColor);
			colorLabel.setText(" ");
		}

		JLabel optionalLabel = new JLabel("(* = optional fields)");

		parameters.add(analysisName, new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10,0,20,0),0,0));
		parameters.add(analysisField, new GridBagConstraints(1,0,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10,20,20,0),0,0));
		parameters.add(clusterNode, new GridBagConstraints(0,1,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,20,0),0,0));
		parameters.add(clusterIDField, new GridBagConstraints(1,1,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,20,20,0),0,0));
		parameters.add(clusterLabel, new GridBagConstraints(0,2,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,20,0),0,0));
		parameters.add(clusterLabelField, new GridBagConstraints(1,2,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,20,20,0),0,0));
		parameters.add(notes, new GridBagConstraints(0,3,2,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,5,0),0,0));
		parameters.add(textArea, new GridBagConstraints(0,4,2,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,20,0),0,0));
		parameters.add(setColorButton, new GridBagConstraints(0,5,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,10,0),0,0));
		parameters.add(colorLabel, new GridBagConstraints(1,5,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,50,10,50),0,0));
		parameters.add(optionalLabel, new GridBagConstraints(0,6,2,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));

		addContent(parameters);
		clusterLabelField.requestFocusInWindow();
		setActionListeners(listener);
		if(this.clusterColor == null)
			okButton.setEnabled(false);
		pack();
		setResizable(false);
	}

	/**
	 * Shows the dialog.
	 */
	public int showModal() {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);
		show();
		return result;
	}

	/**
	 * Returns selected cluster color
	 */
	public Color getColor(){
		return this.colorLabel.getBackground();
	}

	/**
	 * Returns the cluster label
	 */
	public String getLabel(){
		return this.clusterLabelField.getText();
	}

	/**
	 * Returns the description of the cluster
	 */
	public String getDescription(){
		return this.textArea.getText();
	}

	public class EventListener implements ActionListener {

		public void actionPerformed(ActionEvent event){
			String command = event.getActionCommand();
			if(command == "set-color-command"){


				//Added by Sarita Nair

			/*	colorChooser=new JColorChooser();
				colorBlind=new ColorBlindPalette();
				colorChooser.addChooserPanel(colorBlind);

				
				Dialog dialog=JColorChooser.createDialog(ClusterAttributesDialog.this, "Color", true, colorChooser, null, null);
				dialog.setVisible(true);
				Color color=colorChooser.getColor();*/

				//Added for testing	
				Color color=JColorChooser.showDialog(ClusterAttributesDialog.this,"Color", null);


				if(color != null){
					okButton.setEnabled(true);
					colorLabel.setEnabled(true);
					colorLabel.setText(" ");
					colorLabel.setBackground(color);
				}
				else{
					if(!colorLabel.isEnabled())
						okButton.setEnabled(false);
				}


			} else if(command == "ok-command"){
				result = JOptionPane.OK_OPTION;
				dispose();
			} else if(command == "cancel-command"){
				result = JOptionPane.CANCEL_OPTION;
				dispose();
			} else if(command == "reset-command"){
				if(clusterDescriptionStr != null){
					textArea.setText(clusterDescriptionStr);
					textArea.selectAll();
				} else {
					textArea.setText("");
				}
				textArea.setCaretPosition(0);

				if(clusterColor != null)
					colorLabel.setBackground(clusterColor);
				else{
					colorLabel.setBackground(Color.lightGray);
					colorLabel.setText("Preview: No Color");
					colorLabel.setEnabled(false);
				}
				if(clusterLabelStr != null){
					clusterLabelField.setText(clusterLabelStr);
					clusterLabelField.selectAll();
				} else {
					clusterLabelField.setText("");                    
				}
				clusterLabelField.requestFocus();
				clusterLabelField.setCaretPosition(0);

			} else if(command == "info-command"){
				HelpWindow.launchBrowser(ClusterAttributesDialog.this, "Cluster Save Dialog");
			}

		}





	}







	public static void main(String [] args){
		ClusterAttributesDialog d = new ClusterAttributesDialog("Store Cluster Attributes","KMC-genes (2)", "Cluster 8");
		d.show();
		//  System.exit(0);
	}


}
