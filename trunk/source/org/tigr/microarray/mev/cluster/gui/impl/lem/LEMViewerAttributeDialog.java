/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
package org.tigr.microarray.mev.cluster.gui.impl.lem;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;

import org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.ParameterPanel;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.HelpWindow;

/**
 * @author braisted
 * 
 * The LEMViewerAttributDialog presents controls to permit customization of
 * the LEM.  This includes fixed vs. scaled arrow length, gradient vs. binned
 * arrow color, fixed vs. scaled/constrained intergenic length, showing replicates 
 */
public class LEMViewerAttributeDialog extends AlgorithmDialog {


	private LinearExpressionMapViewer lem;
	
	private boolean isFixedArrowLength;
	private boolean isFixedOpenLength;
	private int fixedArrowLength;
	private int bpPerPixel;
	private int minArrowLength;
	private int maxArrowLength;
	private int maxOpenLength;
	private boolean showReps;
	
	private JRadioButton fixedArrowButton;	
	private JRadioButton scaledArrowButton;
	
	private JLabel minArrowLabel;
	private JTextField minArrowField;

	private JLabel maxArrowLabel;
	private JTextField maxArrowField;

	private JLabel fixedArrowLabel;
	private JTextField fixedArrowField;

	private JLabel basePairScaleLabel;
	private JTextField basePairScaleField;
	
	private JCheckBox fixedOpenBox;
	private JLabel maxOpenLabel;
	private JTextField maxOpenField;

	private JCheckBox showReplicatesBox;

	/**
	 * Constructs a LEMViewerAttributDialog
	 * @param parent parent frame
	 * @param lem associated LEM
	 * @param isFixedArrowLength current arrow length mode (fixed/scaled)
	 * @param isFixedOpenLength current intergenic mode (fixed/scaled)
	 * @param fixedArrowLength current arrow length (when fixed)
	 * @param minArrowLength current min. arrow length when scaled
	 * @param maxArrowLength current max. arrow length when scaled
	 * @param maxOpenLength current max. intergenic lenght when scaled
	 * @param bpPerPixel scaling factor, bases per pixel
	 * @param showReps indicates state of replicate display mode
	 */
	public LEMViewerAttributeDialog(Frame parent, LinearExpressionMapViewer lem, boolean isFixedArrowLength, boolean isFixedOpenLength,
			int fixedArrowLength, int minArrowLength, int maxArrowLength, int maxOpenLength, int bpPerPixel, boolean showReps) {
		
		super(parent, "Customize LEM Viewer", true);
		this.lem = lem;
		this.isFixedArrowLength = isFixedArrowLength;
		this.isFixedOpenLength = isFixedOpenLength;
		this.fixedArrowLength = fixedArrowLength;
		this.minArrowLength = minArrowLength;
		this.maxArrowLength = maxArrowLength; 
		this.maxOpenLength = maxOpenLength;
		this.bpPerPixel = bpPerPixel;
		this.showReps = showReps;

		Listener listener = new Listener();
		
		ParameterPanel arrowPanel = new ParameterPanel("Locus Arrow Dimensions");
		arrowPanel.setLayout(new GridBagLayout());
		
		ButtonGroup bg = new ButtonGroup();
		
		fixedArrowButton = createRadioButton("Use Fixed Arrow Length", "fixed-arrow-length", listener, this.isFixedArrowLength, bg);
	
		fixedArrowLabel = new JLabel("Fixed Arrow Length (pixels, >= 15))");		
		fixedArrowField = new JTextField(String.valueOf(this.fixedArrowLength),10);
	
		Dimension dim = new Dimension(60,22);
		
		fixedArrowField.setPreferredSize(dim);
		fixedArrowField.setSize(dim);
		
		this.scaledArrowButton = createRadioButton("Use Scaled Arrow Length", "scale-arrow-command", listener, !this.isFixedArrowLength, bg);
		
		basePairScaleLabel = new JLabel("Scaling Factor (bases/pixel)");
		basePairScaleField = new JTextField(String.valueOf(this.bpPerPixel));		
		basePairScaleField.setPreferredSize(dim);
		basePairScaleField.setSize(dim);
		
		minArrowLabel = new JLabel("Minimum Scaled Arrow Length (>=15)");				
		minArrowField = new JTextField(String.valueOf(this.minArrowLength));
		minArrowField.setPreferredSize(dim);
		minArrowField.setSize(dim);
		
		maxArrowLabel = new JLabel("Maximum Scaled Arrow Length");
		maxArrowField = new JTextField(String.valueOf(this.maxArrowLength));
		maxArrowField.setPreferredSize(dim);
		maxArrowField.setSize(dim);
		
		//validate state
		if(fixedArrowButton.isSelected()) {
			minArrowField.setEnabled(false);
			minArrowLabel.setEnabled(false);
			maxArrowField.setEnabled(false);
			maxArrowLabel.setEnabled(false);

			basePairScaleLabel.setEnabled(false);
			basePairScaleField.setEnabled(false);
		} else {
			fixedArrowField.setEnabled(false);
		}
				
		arrowPanel.add(fixedArrowButton, new GridBagConstraints(0,0,2,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10,10,0,0), 0,0));
		arrowPanel.add(fixedArrowLabel, new GridBagConstraints(0,1,1,1,1,0,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(10,10,10,0), 0,0));
		arrowPanel.add(fixedArrowField, new GridBagConstraints(1,1,1,1,0,0,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(10,5,10,40), 0,0));
		
		JSeparator sep = new JSeparator(JSeparator.HORIZONTAL);		
		sep.setPreferredSize(new Dimension(150, 2));
		sep.setSize(150,2);
		
		arrowPanel.add(sep, new GridBagConstraints(0,2,2,1,1,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,15,0,15), 0,0));

		arrowPanel.add(scaledArrowButton, new GridBagConstraints(0,3,2,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10,10,0,0), 0,0));

		arrowPanel.add(basePairScaleLabel, new GridBagConstraints(0,4,1,1,1,0,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(10,10,0,0), 0,0));
		arrowPanel.add(basePairScaleField, new GridBagConstraints(1,4,1,1,0,0,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(10,5,0,40), 0,0));

		arrowPanel.add(minArrowLabel, new GridBagConstraints(0,5,1,1,1,0,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(10,10,0,0), 0,0));
		arrowPanel.add(minArrowField, new GridBagConstraints(1,5,1,1,0,0,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(10,5,0,40), 0,0));

		arrowPanel.add(maxArrowLabel, new GridBagConstraints(0,6,1,1,1,0,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(10,10,0,0), 0,0));
		arrowPanel.add(maxArrowField, new GridBagConstraints(1,6,1,1,0,0,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(10,5,0,40), 0,0));
				
		
		ParameterPanel openPanel = new ParameterPanel("Intergenic or Unsampled Region Dimensions");
		openPanel.setLayout(new GridBagLayout());
						
		this.fixedOpenBox = createCheckBox("Use Fixed Intergenic Length (1 pixel)", "fixed-open-length", listener, this.isFixedOpenLength);
		this.maxOpenLabel = new JLabel("Max Intergenic (or unsampled) Length");
		this.maxOpenField = new JTextField(String.valueOf(this.maxOpenLength));
		maxOpenField.setPreferredSize(dim);
		maxOpenField.setSize(dim);
		
		boolean fixedOpen = this.fixedOpenBox.isSelected();
		
		this.maxOpenField.setEnabled(!fixedOpen);		
		this.maxOpenLabel.setEnabled(!fixedOpen);
		
		openPanel.add(fixedOpenBox, new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10,10,0,0),0,0));
		openPanel.add(maxOpenLabel, new GridBagConstraints(0,1,1,1,1,0,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(10,10,10,0),0,0));
		openPanel.add(maxOpenField, new GridBagConstraints(1,1,1,1,0,0,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(10,5,10,40),0,0));
		
		ParameterPanel repPanel = new ParameterPanel("Locus Replicate Rendering");
		repPanel.setLayout(new GridBagLayout());
		
		String label = "<html><body>This option will display an arrow";
		label += " for each of the spots related to the locus.<br>";
		label += "Because of the complex structure, arrow lengths and intergenic lengths <br>will be fixed when this option is selected.</body><html>";	
		
		JLabel repLabel = new JLabel(label);
		this.showReplicatesBox = createCheckBox("Show Locus Replicates (representative spots)", "show-reps-command", listener, showReps);
		
		repPanel.add(repLabel, new GridBagConstraints(0,0,1,1,1,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10,10,0,0), 0,0));
		repPanel.add(showReplicatesBox, new GridBagConstraints(0,1,1,1,1,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10,10,10,0), 0,0));
	   		
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBackground(Color.white);
		panel.add(arrowPanel, new GridBagConstraints(0,0,1,1,1,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,0,0,0), 0,0));
		panel.add(openPanel, new GridBagConstraints(0,1,1,1,1,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,0,0,0), 0,0));
		panel.add(repPanel, new GridBagConstraints(0,2,1,1,1,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,0,0,0), 0,0));

		reconfigureButtons(listener);
		
		this.addContent(panel);
		setActionListeners(listener);

		pack();		
	}
	
	/**
	 * Shows the dialog centered on screen
	 */
	public void showModal() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);
        show();
	}
	
	/**
	 * Returns the value for fixed arrow length
	 * @return arrow length
	 */
	public int getFixedArrowLength() {
		return Integer.parseInt(this.fixedArrowField.getText());
	}
	
	/**
	 * returns the minium arrow length
	 * @return returns the minimum arrow length
	 */
	public int getMinArrowLength() {
		return Integer.parseInt(this.minArrowField.getText());
	}
	
	/**
	 * Returns the maximum arrow length
	 * @return max arrow length
	 */
	public int getMaxArrowLength() {
		return Integer.parseInt(this.maxArrowField.getText());
	}
	
	/**
	 * base per pixel scaling factor
	 * @return scaling factor (base/pixel)
	 */
	public int getScalingFactor() {
		return Integer.parseInt(this.basePairScaleField.getText());
	}
	
	/**
	 * Returns the maximum intergenic length value
	 * @return intergenic length constraint
	 */
	public int getMaxIntergenicLength() {
		return Integer.parseInt(this.maxOpenField.getText());
	}
	
	/**
	 * Returns the state of arrow lengths, true if fixed, else false (scaled)
	 * @return true if fixed, else false (fixed)
	 */
	public boolean areArrowsFixed() {
		return this.fixedArrowButton.isSelected();
	}
	
	/**
	 * Returns the state of intergenic (open) lengths, 
	 * true if fixed, else false (scaled)
	 * @return true if fixed, else false (fixed)
	 */
	public boolean areOpenAreasFixed() {
		return this.fixedOpenBox.isSelected();
	}
	
	/**
	 * Returns the state of showing all replicate data vs. hiding
	 * @return true if replicates should be displayed
	 */
	public boolean showAllReplicates() {
		return showReplicatesBox.isSelected();
	}
	
	/**
	 * Sets the current values into a <code>Properties</code> object and
	 * updates lem as a preview
 	 */
	private void previewSettings() {

		//check validity
		if(!validateValues())
			return;
		
		System.out.println("preview method, fixed arrows = "+this.fixedArrowButton.isSelected());
		System.out.println("preview method, fixed arrows, methodCall = "+this.areArrowsFixed());
		Properties props = new Properties();
		props.setProperty("fixed-arrows", String.valueOf(areArrowsFixed()));
		props.setProperty("fixed-arrow-length", String.valueOf(getFixedArrowLength()));
		props.setProperty("scaling-factor", String.valueOf(getScalingFactor()));
		props.setProperty("min-arrow-length", String.valueOf(getMinArrowLength()));
		props.setProperty("max-arrow-length", String.valueOf(getMaxArrowLength()));
		props.setProperty("fixed-open", String.valueOf(areOpenAreasFixed()));
		props.setProperty("max-open-length", String.valueOf(getMaxIntergenicLength()));
		props.setProperty("show-replicates", String.valueOf(showAllReplicates()));
		
		//apply to viewer
		lem.setViewerSettings(props);
	}
	
	/**
	 * Reconfigures buttons based on current settings.
	 * @param listener
	 */
	private void reconfigureButtons(Listener listener) {
		JButton previewButton = new JButton("Preview");		
		previewButton.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		previewButton.setFocusPainted(false);
		previewButton.setActionCommand("preview-command");
		previewButton.addActionListener(listener);
		Dimension dim = new Dimension(65, 30);
		previewButton.setPreferredSize(dim);
		previewButton.setSize(dim);
		
		okButton.setText("Apply");
		
		Component [] comp = buttonPanel.getComponents();
		
		//remove buttons
		for(int i = 0; i < comp.length; i++) {
			buttonPanel.remove(comp[i]);
		}

		buttonPanel.add(comp[0], new GridBagConstraints(0,0,1,1,1,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2,5,2,0), 0,0));
		buttonPanel.add(comp[1], new GridBagConstraints(1,0,1,1,1,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2,10,2,0), 0,0));
		buttonPanel.add(previewButton, new GridBagConstraints(2,0,1,1,1,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2,5,2,0), 0,0));
		buttonPanel.add(comp[2], new GridBagConstraints(3,0,1,1,1,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2,5,2,0), 0,0));
		buttonPanel.add(comp[3], new GridBagConstraints(4,0,1,1,1,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2,5,2,0), 0,0));
		buttonPanel.add(comp[4], new GridBagConstraints(5,0,1,1,1,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2,5,2,5), 0,0));
	}	

	/**
	 * Validates the input values
	 * @return true if values are valid based on parsing and values
	 */
	private boolean validateValues() {
		boolean valid = true;
		//fixed length arrows, check validity of fixed lenght
		int val, minVal, maxVal;
		int level = 0;
		if(areArrowsFixed()) {

			try {				
				val = Integer.parseInt(this.fixedArrowField.getText());
				if(val < 15) {
					JOptionPane.showMessageDialog(this, "The Fixed Arrow Length should be >= 15 pixels. Please try again.", "Invalid Entry", JOptionPane.ERROR_MESSAGE);
					this.fixedArrowField.grabFocus();
					this.fixedArrowField.selectAll();		
					return false;
				}
					
			} catch (NumberFormatException nfe) {
				JOptionPane.showMessageDialog(this, "The Fixed Arrow Length value format is not a valid entry. Please try again.", "Invalid Entry", JOptionPane.ERROR_MESSAGE);
				this.fixedArrowField.grabFocus();
				this.fixedArrowField.selectAll();
				return false;				
			}
		} else {
			try {
				val = Integer.parseInt(this.basePairScaleField.getText());
				if(val < 1) {
					JOptionPane.showMessageDialog(this, "The Scaling Factor should be > 1 base/pixel. Please try again.", "Invalid Entry", JOptionPane.ERROR_MESSAGE);
					this.basePairScaleField.grabFocus();
					this.basePairScaleField.selectAll();
					return false;									
				}
				level++;				
				minVal = Integer.parseInt(this.minArrowField.getText());				
				level++;
				maxVal = Integer.parseInt(this.maxArrowField.getText());				
				level++;
				if(minVal < 15 || maxVal < 15 || maxVal <= minVal) {
					JOptionPane.showMessageDialog(this, "The min and max values are invalid. (Valid Range: 15 <= min < max). Please try again.", "Invalid Entry", JOptionPane.ERROR_MESSAGE);
					return false;														
				}				
			} catch (NumberFormatException nfe) {
				String fieldName = "";
				if(level == 0) {
					fieldName = "The Scaling Factor ";
					this.basePairScaleField.grabFocus();
					this.basePairScaleField.selectAll();
				} else if(level == 1) {
					fieldName = "The Minium Arrow Length ";
					this.minArrowField.grabFocus();
					this.minArrowField.selectAll();
				} else if(level == 2) {
					fieldName = "The Maximum Arrow Length ";
					this.maxArrowField.grabFocus();
					this.maxArrowField.selectAll();
				}
				JOptionPane.showMessageDialog(this, fieldName+"value format is not a valid entry. Please try again.", "Invalid Entry", JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
		
		//if intergenic length is not fixed check the max field
		if(!this.fixedOpenBox.isSelected()) {
			try {
				val = Integer.parseInt(this.maxOpenField.getText());			
				if(val <= 0) {
					JOptionPane.showMessageDialog(this, "Maximum Intergenic Length must be > 0. Please try again.", "Invalid Entry", JOptionPane.ERROR_MESSAGE);							
					return false;
				}
			} catch (NumberFormatException nfe) {
				JOptionPane.showMessageDialog(this, "Maximum Intergenic Length value format is not a valid entry. Please try again.", "Invalid Entry", JOptionPane.ERROR_MESSAGE);			
				this.maxOpenField.grabFocus();
				this.maxOpenField.selectAll();
				return false;
			}
		}
			
		return valid;
	}
	
	/**
	 * Constructs a <code>JCheckBox</code>
	 * @param text label
	 * @param command action command
	 * @param listener Listener
	 * @param selected indicates if should be selected
	 * @return returns the <code>JCheckBox</code>
	 */
	private JCheckBox createCheckBox(String text, String command, Listener listener, boolean selected) {
		JCheckBox box = new JCheckBox(text, selected);
		box.setOpaque(false);
		box.setFocusPainted(false);
		box.setActionCommand(command);
		box.addActionListener(listener);
		return box;
	}
	
	/**
	 * Constructs a <code>JRadioButton</code>
	 * @param text label
	 * @param command action command
	 * @param listener Listener
	 * @param selected indicates if should be selected
	 * @return returns the <code>JRadioButton</code>
	 */
	private JRadioButton createRadioButton(String text, String command, Listener listener, boolean selected, ButtonGroup bg) {
		JRadioButton button = new JRadioButton(text, selected);
		bg.add(button);
		button.setOpaque(false);		
		button.setFocusPainted(false);
		button.setActionCommand(command);
		button.addActionListener(listener);
		return button;
	}
	
	/**
	 * Validates the values of the controls based on selections
	 * enables or disables controls as needed
	 */
	private void validateControls() {
		boolean showReplicates = showAllReplicates();
		//if we are showing replicates, fix arrow lenght and open areas
		if (showReplicates) {
			this.fixedArrowButton.setSelected(true);			
			this.fixedOpenBox.setSelected(true);
		}
		
		boolean enable = this.fixedArrowButton.isSelected();
		this.fixedArrowLabel.setEnabled(enable);
		this.fixedArrowField.setEnabled(enable);
		this.minArrowLabel.setEnabled(!enable);
		this.minArrowField.setEnabled(!enable);
		this.maxArrowLabel.setEnabled(!enable);
		this.maxArrowField.setEnabled(!enable);
		this.basePairScaleLabel.setEnabled(!enable);
		this.basePairScaleField.setEnabled(!enable);
		
		enable = this.fixedOpenBox.isSelected();
		this.maxOpenField.setEnabled(!enable);
		this.maxOpenLabel.setEnabled(!enable);
	}
	
	/**
	 * Resets the controls to intial settings
	 */
	private void resetControls() {
		this.fixedArrowButton.setSelected(this.isFixedArrowLength);
		this.fixedArrowField.setText(String.valueOf(this.fixedArrowLength));

		this.basePairScaleField.setText(String.valueOf(this.bpPerPixel));
		this.minArrowField.setText(String.valueOf(this.minArrowLength));
		this.maxArrowField.setText(String.valueOf(this.maxArrowLength));
		
		this.fixedOpenBox.setSelected(this.isFixedOpenLength);
		this.maxOpenField.setText(String.valueOf(this.fixedArrowLength));
		
		this.showReplicatesBox.setSelected(this.showReps);
	}

	/**
	 * Handles events
	 * @author braisted
	 *
	 * TODO To change the template for this generated type comment go to
	 * Window - Preferences - Java - Code Style - Code Templates
	 */
	public class Listener extends WindowAdapter implements ActionListener {

		public void actionPerformed(ActionEvent ae) {
			validateControls();
			
			String command = ae.getActionCommand();
			if(command.equals("ok-command")) {				
				previewSettings();
				dispose();
			} else if(command.equals("reset-command")) {
				//roll back controls
				resetControls();
				//roll back viewer
				previewSettings();
			} else if(command.equals("preview-command")) {
				previewSettings();
			} else if(command.equals("cancel-command")) {
				resetControls();
				previewSettings();
				dispose();
			} else if(command.equals("info-command")) {
                HelpWindow hw = new HelpWindow(LEMViewerAttributeDialog.this, "LEM Customization Dialog");
                if(hw.getWindowContent()){
                    hw.setSize(450,600);
                    hw.setLocation();
                    hw.show();
                }
                else {
                    hw.setVisible(false);
                    hw.dispose();
                }
			}
		}
		
		public void windowClosed(WindowEvent we) {
			resetControls();
			previewSettings();
			dispose();
		}		
	}
	
}
