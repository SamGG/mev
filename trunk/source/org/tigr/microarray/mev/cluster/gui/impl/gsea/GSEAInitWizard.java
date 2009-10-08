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
	private DataPanel dataPanel;
	private ParameterPanel parameterPanel;

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
		dataPanel = new DataPanel(idata, algData, parent,
				clusterRepository, framework);
		currentPanel = dataPanel;
		super.setInitialPanel(dataPanel);
		parameterPanel = new ParameterPanel(algData, parent, framework);

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
			
			if(currAlgData.getIntMatrix("factor-assignments")!=null){
			nextPanel = parameterPanel;
			currentPanel = nextPanel;
			}else{
				String eMsg=new String();
				eMsg="<html>You have to specify factor levels and sample group assignments before you " +
						"can proceed further <br></html>";
			
				JOptionPane.showMessageDialog(null, eMsg, "Error", JOptionPane.INFORMATION_MESSAGE);
				

			}
			
			//validate();
		}
		if (currentStepIndex == 1) {
			//super.result = JOptionPane.OK_OPTION;
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
		
		//Added additional AND condition, to make sure that the wizard does not close, if user has not
		//entered all the requisite parameters
		if (!super.nextStep() && super.showModal()==JOptionPane.OK_OPTION)
			dispose();

		
		return true;
	}

}
