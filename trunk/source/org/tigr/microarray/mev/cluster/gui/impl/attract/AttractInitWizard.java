package org.tigr.microarray.mev.cluster.gui.impl.attract;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.clusterUtil.ClusterRepository;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.IWizardParameterPanel;
import org.tigr.microarray.mev.cluster.gui.impl.gsea.DataPanel;
import org.tigr.microarray.mev.cluster.gui.impl.attract.ParameterPanel;

public class AttractInitWizard extends org.tigr.microarray.mev.cluster.gui.impl.gsea.GSEAInitWizard{

	private DataPanel dataPanel;
	private IWizardParameterPanel parameterPanel;
	private IWizardParameterPanel currentPanel;
	private IData idata;

	
	public AttractInitWizard(IData idata, JFrame parent, String title, boolean modal, AlgorithmData algData, String[] stepTitles,
			int stepCount, JPanel initPanel,ClusterRepository clusterRepository, IFramework framework) {
		super(idata, parent, title, modal, algData, stepTitles, stepCount, initPanel,
				clusterRepository, framework);
		super.getDataPanel().setMaxFactors(1);
		
			
		parameterPanel = new ParameterPanel(algData, parent, framework);
		super.setParameterPanel(parameterPanel);
		
	}

}
