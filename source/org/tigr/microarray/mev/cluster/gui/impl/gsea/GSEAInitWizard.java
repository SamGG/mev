package org.tigr.microarray.mev.cluster.gui.impl.gsea;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.clusterUtil.ClusterRepository;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.GSEAWizard;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.IWizardParameterPanel;

public class GSEAInitWizard extends GSEAWizard {

	/*
	 * Module specific IWizardParmeterPanels 
	 */

	private StepsPanel stepsPanel;
	private GSEADataPanel dataPanel;
	private GSEAParameterPanel parameterPanel;

	private IWizardParameterPanel currentPanel;
	private IData idata;

	/**
	 * @param parent parent JFrame
	 * @param title main dialog title
	 * @param modal	boolean to indicate if dialog is to be modal, almost always true.
	 * @param algData <code>AlgorithmData</code> to encapsulate parameters
	 * @param clusterRepository TODO
	 * @param steps String array of process steps.  Convention is sentence case and end all steps with a . to help delimit.
	 * @param stepComponents initial JPanel to display
	 */
	public GSEAInitWizard(IData idata, JFrame parent, String title,
			boolean modal, AlgorithmData algData, String[] stepTitles,
			int stepCount, JPanel initPanel,
			ClusterRepository clusterRepository, IFramework framework) {
		super(parent, title, modal, algData, stepTitles, stepCount, initPanel);

		this.idata = idata;
		dataPanel = new GSEADataPanel(idata, algData, parent,
				clusterRepository, framework);
		currentPanel = dataPanel;
		super.setInitialPanel(dataPanel);
		parameterPanel = new GSEAParameterPanel(algData, this);

	}

	/**
	 * Prepares the next component to display.  A combination of current algorithm parameters
	 * in AlgorithmData and step index is used to prepare the returned component for display.
	 * 
	 * @param currAlgData <code>AlgorithmData</code> containing the current parameters
	 * @param currentStepIndex the current step index which is incremented and decremented with next and prev
	 * @return returns the IWizardParameterPanel (JPanel extension) to display
	 */
	protected IWizardParameterPanel prepareAndDeliverNextParameterPanel(
			AlgorithmData currAlgData, int currentStepIndex) {

		IWizardParameterPanel nextPanel = null;

		//current panel should capture

		currentPanel.populateAlgorithmData();

		if (currentStepIndex == 0) {

			nextPanel = parameterPanel;
			//nextPanel.populateAlgorithmData();

			pack();
			currentPanel = nextPanel;
		}
		if (currentStepIndex == 1) {
			super.result = JOptionPane.OK_OPTION;
		}

		return nextPanel;
	}

	/**
	 * Prepares the next component to display.  A combination of current algorithm parameters
	 * in AlgorithmData and step index is used to prepare the returned component for display.
	 * 
	 * @param currAlgData <code>AlgorithmData</code> containing the current parameters
	 * @param currentStepIndex the current step index which is incremented and decremented with next and prev
	 * @return returns the IWizardParameterPanel (JPanel extension) to display
	 */
	protected IWizardParameterPanel prepareAndDeliverPreviousParameterPanel(
			AlgorithmData currAlgData, int currentStepIndex) {

		IWizardParameterPanel prevPanel = null;

		//current panel should capture
		currentPanel.clearValuesFromAlgorithmData();

		if (currentStepIndex == 1) {
			prevPanel = dataPanel;
			currentPanel = prevPanel;
		}

		return prevPanel;
	}

	protected boolean nextStep() {
		if (!super.nextStep())
			dispose();

		return true;
	}

}
