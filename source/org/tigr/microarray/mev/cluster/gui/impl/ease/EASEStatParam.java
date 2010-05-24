/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).

 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/

package org.tigr.microarray.mev.cluster.gui.impl.ease;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.HelpWindow;

/**
 * Gathers statistical parameters for execution of EASE analysis.
 * 
 * @author hgomez
 */
public class EASEStatParam extends AlgorithmDialog {

	private static final long serialVersionUID = -8638108335416140953L;

	protected int result = JOptionPane.CANCEL_OPTION;
	// Stats
	protected JCheckBox fisherBox;
	protected JCheckBox easeBox;
	// Multiple corrections
	protected JCheckBox bonferroniBox;
	protected JCheckBox sidakBox;
	protected JCheckBox hochbergBox;
	protected JCheckBox bonferroniStepBox;
	protected JCheckBox permBox;
	protected JTextField permField;
	protected JLabel permLabel;
	// Trim parameters
	protected JCheckBox trimBox;
	protected JCheckBox trimNBox;
	protected JLabel trimNLabel;
	protected JTextField trimNField;
	protected JCheckBox trimPercentBox;
	protected JLabel trimPercentLabel;
	protected JTextField trimPercentField;

	// Others
	protected Frame parent;
	protected Font font;
	protected EventListener listener;

	/**
	 * Constructs a new EASEStatParam. This window contains the advanced
	 * statistical parameter selection boxes for EASE analysis.
	 * 
	 * @param frame
	 *            the parent Frame
	 */
	public EASEStatParam(Frame parent) {

		super(parent, "EASE: Statistical Parameters", true);
		this.parent = parent;

		font = new Font("Dialog", Font.BOLD, 12);
		setBackground(Color.white);
		listener = new EventListener();
		addWindowListener(listener);

		// STAT PANEL
		JPanel statPanel = new JPanel(new GridBagLayout());
		statPanel.setBackground(Color.white);
		statPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(EtchedBorder.LOWERED),
				"Reported Statistic", TitledBorder.DEFAULT_JUSTIFICATION,
				TitledBorder.DEFAULT_POSITION, font, Color.black));

		ButtonGroup bg = new ButtonGroup();
		fisherBox = new JCheckBox("Fisher Exact Probability", true);
		fisherBox.setBackground(Color.white);
		fisherBox.setFocusPainted(false);
		bg.add(fisherBox);

		easeBox = new JCheckBox("EASE Score", false);
		easeBox.setBackground(Color.white);
		easeBox.setFocusPainted(false);
		bg.add(easeBox);

		statPanel.add(fisherBox, new GridBagConstraints(0, 0, 1, 1, 0, 0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						0, 0, 10, 0), 0, 0));
		statPanel.add(easeBox, new GridBagConstraints(1, 0, 1, 1, 0, 0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						0, 0, 10, 0), 0, 0));

		// P-value Correction Panel
		JPanel correctionPanel = new JPanel(new GridBagLayout());
		correctionPanel.setBackground(Color.white);
		correctionPanel.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
				"Multiplicity Corrections", TitledBorder.DEFAULT_JUSTIFICATION,
				TitledBorder.DEFAULT_POSITION, font, Color.black));

		bonferroniBox = new JCheckBox("Bonferroni Correction", false);
		bonferroniBox.setBackground(Color.white);
		bonferroniBox.setFocusPainted(false);
		bonferroniStepBox = new JCheckBox("Bonferroni Step Down Correction",
				false);
		bonferroniStepBox.setBackground(Color.white);
		bonferroniStepBox.setFocusPainted(false);

		sidakBox = new JCheckBox("Sidak Method", false);
		sidakBox.setBackground(Color.white);
		sidakBox.setFocusPainted(false);

		hochbergBox = new JCheckBox("Benjamini-Hochberg Method", true);
		hochbergBox.setBackground(Color.white);
		hochbergBox.setFocusPainted(false);

		permBox = new JCheckBox("Resampling Probability Analysis", false);
		permBox.setActionCommand("permutation-analysis-command");
		permBox.setBackground(Color.white);
		permBox.setFocusPainted(false);
		permBox.addActionListener(listener);

		permField = new JTextField("1000", 10);
		permField.setBackground(Color.white);
		permField.setEnabled(false);

		permLabel = new JLabel("Number of Permutations");
		permLabel.setBackground(Color.white);
		permLabel.setEnabled(false);

		correctionPanel.add(bonferroniBox, new GridBagConstraints(0, 0, 3, 1,
				0, 0, GridBagConstraints.WEST, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));
		correctionPanel.add(bonferroniStepBox, new GridBagConstraints(0, 1, 3,
				1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.BOTH,
				new Insets(0, 0, 10, 0), 0, 0));
		correctionPanel.add(sidakBox, new GridBagConstraints(0, 2, 3, 1, 0, 0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						0, 0, 0, 0), 0, 0));
		correctionPanel.add(hochbergBox, new GridBagConstraints(0, 3, 3, 1, 0,
				0, GridBagConstraints.WEST, GridBagConstraints.BOTH,
				new Insets(0, 0, 10, 0), 0, 0));

		correctionPanel.add(permBox, new GridBagConstraints(4, 0, 2, 1, 0, 0,
				GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0,
						0, 0, 0), 0, 0));
		correctionPanel.add(permLabel, new GridBagConstraints(4, 1, 1, 1, 0, 0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						0, 0, 15, 5), 0, 0));
		correctionPanel.add(permField, new GridBagConstraints(5, 1, 1, 1, 0, 0,
				GridBagConstraints.CENTER, GridBagConstraints.VERTICAL,
				new Insets(0, 0, 15, 0), 0, 0));

		// Trim Panel
		JPanel trimPanel = new JPanel(new GridBagLayout());
		trimPanel.setBackground(Color.white);
		trimPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(EtchedBorder.LOWERED), "Trim Parameters",
				TitledBorder.DEFAULT_JUSTIFICATION,
				TitledBorder.DEFAULT_POSITION, font, Color.black));

		trimBox = new JCheckBox("Trim Resulting Groups", false);
		trimBox.setActionCommand("trim-result-command");
		trimBox.addActionListener(listener);
		trimBox.setBackground(Color.white);
		trimBox.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		trimBox.setFocusPainted(false);

		bg = new ButtonGroup();

		trimNBox = new JCheckBox("Select Minimum Hit Number", true);
		trimNBox.setActionCommand("trim-result-command");
		trimNBox.addActionListener(listener);
		trimNBox.setEnabled(false);
		trimNBox.setBackground(Color.white);
		trimNBox.setFocusPainted(false);
		bg.add(trimNBox);

		trimNLabel = new JLabel("Min. Hits");
		trimNLabel.setBackground(Color.white);
		trimNLabel.setEnabled(false);

		trimNField = new JTextField("5", 10);
		trimNField.setEnabled(false);

		trimPercentBox = new JCheckBox("Select Minimum Hit Percentage", false);
		trimPercentBox.setActionCommand("trim-result-command");
		trimPercentBox.addActionListener(listener);
		trimPercentBox.setEnabled(false);
		trimPercentBox.setBackground(Color.white);
		trimPercentBox.setFocusPainted(false);
		bg.add(trimPercentBox);

		trimPercentLabel = new JLabel("Percent Hits");
		trimPercentLabel.setBackground(Color.white);
		trimPercentLabel.setEnabled(false);

		trimPercentField = new JTextField("5", 10);
		trimPercentField.setEnabled(false);

		trimPanel.add(trimBox, new GridBagConstraints(0, 0, 3, 1, 0, 0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						0, 0, 20, 0), 0, 0));

		trimPanel.add(trimNBox, new GridBagConstraints(0, 1, 1, 1, 0, 0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						0, 0, 0, 0), 0, 0));
		trimPanel.add(trimNLabel, new GridBagConstraints(1, 1, 1, 1, 0, 0,
				GridBagConstraints.EAST, GridBagConstraints.BOTH, new Insets(0,
						20, 0, 15), 0, 0));
		trimPanel.add(trimNField, new GridBagConstraints(2, 1, 2, 1, 0, 0,
				GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(
						0, 0, 0, 0), 0, 0));

		trimPanel.add(trimPercentBox, new GridBagConstraints(0, 2, 1, 1, 0, 0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						10, 0, 0, 0), 0, 0));
		trimPanel.add(trimPercentLabel, new GridBagConstraints(1, 2, 1, 1, 0,
				0, GridBagConstraints.EAST, GridBagConstraints.BOTH,
				new Insets(10, 20, 0, 15), 0, 0));
		trimPanel.add(trimPercentField, new GridBagConstraints(2, 2, 2, 1, 0,
				0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
				new Insets(10, 0, 0, 0), 0, 0));

		JPanel parameters = new JPanel(new GridBagLayout());
		parameters.setBackground(Color.white);

		// Add panels to main panel
		parameters.add(statPanel, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						0, 0, 0, 0), 0, 0));
		parameters.add(correctionPanel, new GridBagConstraints(0, 1, 1, 1, 1.0,
				1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));
		parameters.add(trimPanel, new GridBagConstraints(0, 2, 1, 1, 1.0, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						0, 0, 0, 0), 0, 0));

		addContent(parameters);
		setActionListeners(listener);
		pack();
	}

	/**
	 * Shows the EASEStatParam Dialog.
	 * 
	 * @return an Integer depending on a window event
	 */
	public int showModal() {

		/* Fetches the screen size and places the window in the center */
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((screenSize.width - getSize().width) / 2,
				(screenSize.height - getSize().height) / 2);
		setVisible(true);
		return result;
	}

	/**
	 * @author hgomez
	 * 
	 *         This class listens to the dialog and check boxes items events
	 * 
	 */
	protected class EventListener extends DialogListener implements

	ItemListener {

		public void actionPerformed(ActionEvent e) {

			String command = e.getActionCommand();

			if (command.equals("permutation-analysis-command")) {
				setEnablePermutations();
			} else if (command.equals("trim-result-command")) {
				validateTrimOptions();
			} else if (command.equals("ok-command")) {
				result = JOptionPane.OK_OPTION;
				dispose();
			} else if (command.equals("cancel-command")) {
				result = JOptionPane.CANCEL_OPTION;
				dispose();
			} else if (command.equals("reset-command")) {
				resetControls();
				result = JOptionPane.CANCEL_OPTION;
				return;
			} else if (command.equals("info-command")) {
				HelpWindow hw = new HelpWindow(EASEStatParam.this,
						"EASE StatisticalParams Dialog");
				result = JOptionPane.CANCEL_OPTION;
				if (hw.getWindowContent()) {
					hw.setSize(600, 600);
					hw.setLocation();
					hw.show();
				} else {
					hw.setVisible(false);
					hw.dispose();
				}
			}
		}

		public void itemStateChanged(ItemEvent arg0) {
		}

		public void windowClosing(WindowEvent e) {

			result = JOptionPane.CLOSED_OPTION;
			dispose();
		}
	}

	/**
	 * Resets dialog controls to default values
	 * */
	protected void resetControls() {
		/* Reported Statistics */
		fisherBox.setSelected(true);
		/* Multiplicity corrections */
		hochbergBox.setSelected(true);
		trimBox.setSelected(false);
		bonferroniBox.setSelected(false);
		bonferroniStepBox.setSelected(false);
		sidakBox.setSelected(false);
		permBox.setSelected(false);
		permLabel.setEnabled(false);
		permField.setText("1000");
		permField.setEnabled(false);
		/* Trim parameters */
		trimBox.setSelected(false);
		trimNBox.setEnabled(false);
		trimNBox.setSelected(true);
		trimNLabel.setEnabled(false);
		trimNField.setText("5");
		trimNField.setEnabled(false);
		trimPercentBox.setEnabled(false);
		trimPercentLabel.setEnabled(false);
		trimPercentField.setText("5");
		trimPercentField.setEnabled(false);
	}

	/**
	 *Indicates if permutations are selected for EASE analysis.
	 * 
	 *@return a boolean flag to signal permutations
	 * 
	 */
	public boolean performPermutations() {
		return permBox.isSelected();
	}

	/**
	 * Allows specification of permutation number for EASE analysis.
	 * */
	public void setEnablePermutations() {
		permLabel.setEnabled(permBox.isSelected());
		permField.setEnabled(permBox.isSelected());
	}

	/**
	 * Enables fields so that analysis results can be filtered based on the
	 * number of hits or the fraction of genes in the cluster that are
	 * represented by an annotation term.
	 * */
	public void validateTrimOptions() {

		if (this.trimBox.isSelected()) {
			trimNBox.setEnabled(true);
			trimPercentBox.setEnabled(true);

			trimNLabel.setEnabled(trimNBox.isSelected());
			trimNField.setEnabled(trimNBox.isSelected());
			trimPercentLabel.setEnabled(!trimNBox.isSelected());
			trimPercentField.setEnabled(!trimNBox.isSelected());
		} else {
			trimNBox.setEnabled(false);
			trimPercentBox.setEnabled(false);

			trimNLabel.setEnabled(false);
			trimNField.setEnabled(false);
			trimPercentLabel.setEnabled(false);
			trimPercentField.setEnabled(false);
		}
	}

	public static void main(String[] args) {
		EASEStatParam stats = new EASEStatParam(new JFrame());
		stats.showModal();
	}
}
