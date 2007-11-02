/*
 * Created on Jan 10, 2007
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.tigr.microarray.mev.cluster.gui.impl.nonpar;

import java.util.Vector;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.GroupNumberAndNameSelectionPanel;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.GroupSelectionColorPanel;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.IWizardParameterPanel;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.StatProcessWizard;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.TwoWaySelectionPanel;

/**
 * @author braisted
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class NonparInitWizard extends StatProcessWizard {

	/*
	 * Module specific IWizardParmeterPanels 
	 */
	/**
	 * Nonpar mode panel
	 */
	private NonparModePanel modePanel;

	//group name and number panel
	private GroupNumberAndNameSelectionPanel groupNumberAndNamesPanel;
	
	//modified group names panel for Fisher Exact
	NonparFEGroupAndDataBinNamePanel feGroupAndBinNamesPanel;
	
	//group selection panel
	private GroupSelectionColorPanel groupSelectionPanel;

	//parameter panels
	private NonparWilcoxonPanel wilcoxonPanel;
	//private IWizardParameterPanel kruscalWallacePanel;
	//private IWizardParameterPanel mackSkillingsPanel;
	private NonparFisherPanel fisherExactPanel;	
	
	private IWizardParameterPanel twoWayPanel;	
	private IWizardParameterPanel currentPanel;

	/**
	 * @param parent parent JFrame
	 * @param title main dialog title
	 * @param modal	boolean to indicate if dialog is to be modal, almost always true.
	 * @param algData <code>AlgorithmData</code> to encapsulate parameters
	 * @param steps String array of process steps.  Convention is sentence case and end all steps with a . to help delimit.
	 * @param stepComponents initial JPanel to display
	 */
	public NonparInitWizard(IData idata, JFrame parent, String title, boolean modal, AlgorithmData algData, String[] steps, int stepCount, JPanel initPanel) {
		super(parent, title, modal, algData, steps, stepCount, initPanel);

		modePanel = new NonparModePanel(algData, this);
		super.setInitialPanel(modePanel);
		currentPanel = modePanel;
		groupNumberAndNamesPanel = new GroupNumberAndNameSelectionPanel(algData, this, false);
		groupSelectionPanel = new GroupSelectionColorPanel(algData);
		wilcoxonPanel = new NonparWilcoxonPanel(algData, this);
		feGroupAndBinNamesPanel = new NonparFEGroupAndDataBinNamePanel (algData, this);
		fisherExactPanel = new NonparFisherPanel(algData, this);
		
		Vector fieldNameVector = idata.getSampleAnnotationFieldNames();
		String [] fieldNames = new String[fieldNameVector.size()];
		for(int i = 0; i < fieldNames.length; i++) {
			fieldNames[i] = (String)(fieldNameVector.get(i));
		}
		
		int numSamples = idata.getFeaturesList().size();
		String [][] sampleAnn = new String[fieldNames.length][numSamples];
		for(int i = 0; i < sampleAnn.length; i++) {
			for(int j = 0; j < sampleAnn[i].length; j++) {
				sampleAnn[i][j] = idata.getSampleAnnotation(j, fieldNames[i]);
			}						
		}
		
		twoWayPanel = new TwoWaySelectionPanel(algData, fieldNames, sampleAnn);
	}


	/**
	 * Prepares the next component to display.  A combination of current algorithm parameters
	 * in AlgorithmData and step index is used to prepare the returned component for display.
	 * 
	 * @param currAlgData <code>AlgorithmData</code> containing the current parameters
	 * @param currentStepIndex the current step index which is incremented and decremented with next and prev
	 * @return returns the IWizardParameterPanel (JPanel extension) to display
	 */
	protected IWizardParameterPanel prepareAndDeliverNextParameterPanel(AlgorithmData currAlgData, int currentStepIndex) {

		IWizardParameterPanel nextPanel = null;
		
		//current panel should capture
		currentPanel.populateAlgorithmData();
		
		//get mode selection
		String mode = currAlgData.getParams().getString("nonpar-mode");
		
		//if current step is 0 (mode panel) next component is group number and names panel
		//need to prep
		if(currentStepIndex == 0) {
			
			if(mode.equals(NonparConstants.MODE_WILCOXON_MANN_WHITNEY)) {
				groupNumberAndNamesPanel.initialize(true, false, 2);
				nextPanel = groupNumberAndNamesPanel;
				pack();
			//groupNumberAndNamePanel.setStyle(GROUP_INFO_STYLE_TWO_SAMPLES);
			} else if(mode.equals(NonparConstants.MODE_KRUSKAL_WALLIS)) {
				groupNumberAndNamesPanel.initialize(true, true, 2);
				nextPanel = groupNumberAndNamesPanel;
				pack();
				//groupNumberAndNamePanel.setStyle(GROUP_INFO_STYLE_N_SAMPLES);
			} else if(mode.equals(NonparConstants.MODE_MACK_SKILLINGS)) {
				groupNumberAndNamesPanel.initialize(false, true, 2);				
				nextPanel = groupNumberAndNamesPanel;
				//groupNumberAndNamePanel.setStyle(GROUP_INFO_STYLE_2_FACTORS);
			} else if(mode.equals(NonparConstants.MODE_FISHER_EXACT)) {
				//modified panel to capture bin names
				nextPanel = feGroupAndBinNamesPanel;
			}
			
			currentPanel = nextPanel;			
		}
		
		// if current step is 1 then we need to specify group membership
		else if(currentStepIndex == 1) {
			boolean uniqueNames = true;
			
			if(mode.equals(NonparConstants.MODE_FISHER_EXACT))
				uniqueNames = feGroupAndBinNamesPanel.areGroupAndFactorNamesUnique();
			else 
				uniqueNames = groupNumberAndNamesPanel.areGroupAndFactorNamesUnique();
			
			if(!uniqueNames) {
				//if not unique we need notify and go back one panel
				
				//move step back
				super.currentStepIndex--;
				
				//warn
				JOptionPane.showMessageDialog(this, "Please select unique groupa or factor names. Group or factor names are not unique.", "Ambiguous Naming Error", JOptionPane.ERROR_MESSAGE);
				
				//return the current panel
				return currentPanel;
				//this.prepareAndDeliverPreviousParameterPanel(currAlgData, currentStepIndex);
			}

			currentPanel.populateAlgorithmData();
			
			String [] sampleNames = currAlgData.getStringArray("sample-names");
			String [] groupNames = currAlgData.getStringArray("group-names");
			
			
			
			//need to pass number of groups, group names, and indicate if it should
			//be for two factor
			if(mode.equals(NonparConstants.MODE_WILCOXON_MANN_WHITNEY)) {			
				//groupSelectionPanel.setStyle(GroupSelectionPanel.GROUP_SELECTION_DIALOG_ONE_FACTOR);				
				groupSelectionPanel.initializeOneFactor(groupNames, sampleNames);
				nextPanel = groupSelectionPanel;
			} else if(mode.equals(NonparConstants.MODE_KRUSKAL_WALLIS)) {
				groupSelectionPanel.initializeOneFactor(groupNames, sampleNames);
				nextPanel = groupSelectionPanel;
				//groupSelectionPanel.setStyle(GroupSelectionPanel.GROUP_SELECTION_DIALOG_ONE_FACTOR);
			} else if(mode.equals(NonparConstants.MODE_MACK_SKILLINGS)) {
				//groupSelectionPanel.setStyle(GroupSelectionPanel.GROUP_SELECTION_DIALOG_TWO_FACTOR);
				String factorAName = currAlgData.getParams().getString("factor-A-name");
				String factorBName = currAlgData.getParams().getString("factor-B-name");				
				String [] factorANames = currAlgData.getStringArray("factor-A-level-names");
				String [] factorBNames = currAlgData.getStringArray("factor-B-level-names");

				//initialize the two way panel
				((TwoWaySelectionPanel)twoWayPanel).initialize(factorAName, factorBName, factorANames, factorBNames);
				nextPanel = twoWayPanel;
			} if(mode.equals(NonparConstants.MODE_FISHER_EXACT)) {
				groupSelectionPanel.initializeOneFactor(groupNames, sampleNames);
				nextPanel = groupSelectionPanel;
			}
			
			currentPanel = nextPanel;
		} 
		
		//if current step is 2the we present parameter panels
		else if(currentStepIndex == 2) {
			if(mode.equals(NonparConstants.MODE_WILCOXON_MANN_WHITNEY)) {		
				nextPanel = wilcoxonPanel;
				wilcoxonPanel.initializePanel(mode);
				  
			} else if(mode.equals(NonparConstants.MODE_KRUSKAL_WALLIS)) {
				//same parameter set
				nextPanel = wilcoxonPanel;
				wilcoxonPanel.initializePanel(mode);
			} else if(mode.equals(NonparConstants.MODE_MACK_SKILLINGS)) {
				nextPanel = wilcoxonPanel;
				wilcoxonPanel.initializePanel(mode);
			} if(mode.equals(NonparConstants.MODE_FISHER_EXACT)) {
				//change to fe parameter panel
				nextPanel = fisherExactPanel;								
				fisherExactPanel.initializePanel();			  
			}	
		} else if(currentStepIndex == 3) {
			if(mode.equals(NonparConstants.MODE_FISHER_EXACT)) {
				fisherExactPanel.populateAlgorithmData();
			} else {
				wilcoxonPanel.populateAlgorithmData();
			}
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
	protected IWizardParameterPanel prepareAndDeliverPreviousParameterPanel(AlgorithmData currAlgData, int currentStepIndex) {
		
		IWizardParameterPanel  prevPanel = null;
		
		//current panel should capture
		currentPanel.clearValuesFromAlgorithmData();
		
		//get mode selection
		String mode = currAlgData.getParams().getString("nonpar-mode");
		
		if(currentStepIndex == 1) {
			prevPanel = modePanel;
			currentPanel = prevPanel;			
		} else if(currentStepIndex == 2) {
			//if current step is 1 then we need to specify group membership
			//currentPanel.populateAlgorithmData();	
			if(mode.equals(NonparConstants.MODE_FISHER_EXACT)) {
				prevPanel = feGroupAndBinNamesPanel;
			} else {
				prevPanel = groupNumberAndNamesPanel;
			}
			currentPanel = prevPanel;
		} else if(currentStepIndex == 3) {
			//if current step is 2the we present parameter panels
			if(mode.equals(NonparConstants.MODE_MACK_SKILLINGS)) {
				prevPanel = twoWayPanel;
			} else {
				prevPanel = groupSelectionPanel;
			}
		} 
		
		return prevPanel;
	}
	
	
	protected boolean nextStep() {
		if(!super.nextStep())
			dispose();
		
		//adjust the size of the panel to the screen resolution
		//if(currentPanel == groupSelectionPanel) {
		//	groupSelectionPanel.adjustSizeToResolution();
		//}

//		pack();

		return true;
	}
	
	

}
