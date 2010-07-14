/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: EASEInitDialog.java,v $
 * $Revision: 1.12 $
 * $Date: 2007-02-15 15:58:16 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
/*
 * EaseInitDialog.java
 *
 * Created on August 25, 2003, 11:39 AM
 */

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
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextPane;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.tigr.microarray.mev.ShowThrowableDialog;
import org.tigr.microarray.mev.TMEV;
import org.tigr.microarray.mev.annotation.AnnotationFieldConstants;
import org.tigr.microarray.mev.cluster.clusterUtil.Cluster;
import org.tigr.microarray.mev.cluster.clusterUtil.ClusterRepository;
import org.tigr.microarray.mev.cluster.gui.helpers.ClusterBrowser;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.ParameterPanel;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.HelpWindow;
import org.tigr.microarray.mev.resources.FileResourceManager;
import org.tigr.microarray.mev.resources.IResourceManager;
import org.tigr.microarray.mev.resources.PipelinedAnnotationsFileDefinition;
import org.tigr.microarray.mev.resources.RepositoryInitializationError;
import org.tigr.microarray.mev.resources.ResourcererAnnotationFileDefinition;
import org.tigr.microarray.mev.resources.SupportFileAccessError;

/**
 * Accumulates parameters for execution of EASE analysis.
 * 
 * @author braisted
 * @editor hgomez
 */
public class EASEInitDialog extends AlgorithmDialog {

	private static final long serialVersionUID = -8572561763028404439L;

	/**
	 * Result when dialog is dismissed
	 */
	private int result = JOptionPane.CANCEL_OPTION;
	
	private int resultStat= JOptionPane.CANCEL_OPTION;
	
	private int resultAdvanced= JOptionPane.CANCEL_OPTION;

	/* File updates and configuration panel */
	private ConfigPanel configPanel;
	/* Mode selection panel */
	private ModePanel modePanel;
	/* Cluster Graph and Gene cluster table panel */
	private ClusterBrowser browser;

	/* Statistical parameters panel */
	private EASEStatParam statParam;
	
	/*Extra reference to EASEStatParam*/
	private EASEStatParam statParamTemp;

	/* Frame with extra parameters */
	private EASEAdvancedFeatures advancedFeat;
	
	/* Extra reference to EASEAdvancedFeatures */
	private EASEAdvancedFeatures advancedFeatTemp;

	/* NestedEase Panel */
	private NEasePanel nEasePanel;

	private EventListener listener;
	private Font font;
	private String sep;
	private Frame parent;

	private String arrayName, speciesName;
	private Hashtable<String, Vector<String>> speciestoarrays;
	private IResourceManager resourceManager;

	private static String ANNOTATION_LINK = AnnotationFieldConstants.ENTREZ_ID;
	private boolean useLoadedAnnotationFile = false;
	protected File annotationFile;
	private String defaultFileBaseLocation;
	private String defaultFileLocation;

	/* Set of variables for default stat. parameter values */
	private boolean easeScore = false;
	private boolean bonferroni = false;
	private boolean stepDownBonf = false;
	private boolean sidak = false;
	private boolean hochberg = true;
	private boolean probAnalysis = false;
	private boolean trimSelect = false;
	private boolean percentSelect = false;
	private String percentField = "5";
	private String hitField = "5";
	private int permCount = 1000;

	/* Flag to keep track whether the user hit the select/ download button */
	private boolean isSelected = false;

	/* Variables for support file panel */
	private String supportFileLocation = null;

	/* To keep track of whether the user accessed advanced annotation params */
	private boolean isAdvancedAnnotParams = false;

	/* To keep track whether the user accessed the advanced info */
	private boolean isAdvancedStatParams = false;

	/* Annotation Labels */
	private String[] labels;

	/* Panel where a clusterBrowser is stored. */
	private JPanel popNClusterPanel;

	private File f = null;

	public File getAnnotationFile() {
		return annotationFile;
	}

	/*
	 * Saves the repository to pass it to EASEAdvancedFeatures
	 */
	ClusterRepository repository;

	/**
	 * Creates a new instance of EaseInitDialog
	 * 
	 * @param parent
	 *            Parent Frame
	 * @param repository
	 *            Cluster repository to construct <CODE>ClusterBrowser</CODE>
	 * @param annotationLabels
	 *            Annotation types
	 */
	public EASEInitDialog(Frame parent, ClusterRepository repository,
			String[] annotationLabels, String defaultFileL,
			IResourceManager rm, String speciesName, String arrayName,
			Hashtable<String, Vector<String>> speciestoarrays,
			boolean isAnnotationLoaded) {
		super(parent, "EASE: EASE Annotation Analysis", true);
		this.parent = parent;
		this.speciesName = speciesName;
		this.arrayName = arrayName;
		this.resourceManager = rm;
		this.speciestoarrays = speciestoarrays;
		this.useLoadedAnnotationFile = isAnnotationLoaded;
		this.defaultFileLocation = defaultFileL;
		this.repository = repository;
		this.labels = annotationLabels;

		if (defaultFileLocation == null) {
			defaultFileBaseLocation = TMEV
					.getSettingForOption(EASEGUI.LAST_EASE_FILE_LOCATION);
		} else {
			defaultFileBaseLocation = defaultFileLocation;
		}
		if (defaultFileBaseLocation == null
				|| !new File(defaultFileBaseLocation).canRead()) {
			defaultFileBaseLocation = "./data/ease";
		}
		
		supportFileLocation=defaultFileBaseLocation;

		sep = System.getProperty("file.separator");
		font = new Font("Dialog", Font.BOLD, 12);
		listener = new EventListener();
		addWindowListener(listener);

		// config panel
		configPanel = new ConfigPanel();

		// NeasePanel
		nEasePanel = new NEasePanel();

		popNClusterPanel = new JPanel(new GridBagLayout());
		popNClusterPanel.setBackground(Color.white);
		browser = new ClusterBrowser(repository);
		popNClusterPanel.add(browser, new GridBagConstraints(0, 1, 1, 1, 1.0,
				1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));

		JPanel parameters = new JPanel(new GridBagLayout());
		popNClusterPanel.setPreferredSize(new Dimension(500, 350));
		parameters.setBackground(Color.white);

		// mode panel
		modePanel = new ModePanel(!(repository == null || repository.isEmpty()));

		parameters.add(configPanel, new GridBagConstraints(0, 0, 1, 1, 1.0, 0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						0, 0, 0, 0), 0, 0));
		parameters.add(modePanel, new GridBagConstraints(0, 1, 1, 1, 1.0, 0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						0, 0, 0, 0), 0, 0));

		parameters.add(popNClusterPanel, new GridBagConstraints(0, 2, 1, 1,
				1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));
		parameters.add(nEasePanel, new GridBagConstraints(0, 3, 1, 1, 1.0, 0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						0, 0, 0, 0), 0, 0));

		addContent(parameters);
		setActionListeners(listener);

		if (repository == null || repository.isEmpty()) {
			disableClusterSelection();
		}
		this.setSize(550, 700);
	}

	/**
	 * Grays out the Cluster Graph and Cluster Selection Panels when either the
	 * repository is empty or when Annotation Survey is performed.
	 */
	public void disableClusterSelection() {
		JPanel temporaryPanel = popNClusterPanel;
		temporaryPanel.setLayout(new GridBagLayout());
		temporaryPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(EtchedBorder.LOWERED), "Cluster Selection",
				TitledBorder.DEFAULT_JUSTIFICATION,
				TitledBorder.DEFAULT_POSITION, font, Color.black));
		temporaryPanel.removeAll();
		temporaryPanel.validate();
		temporaryPanel.setOpaque(false);
		temporaryPanel.add(new JLabel("Empty Cluster Repository"),
				new GridBagConstraints(0, 0, 1, 1, 0, 0,
						GridBagConstraints.NORTH, GridBagConstraints.NONE,
						new Insets(15, 0, 10, 0), 0, 0));
		temporaryPanel.add(new JLabel("Only Annotation Survey is Enabled"),
				new GridBagConstraints(0, 1, 1, 1, 0, 0,
						GridBagConstraints.NORTH, GridBagConstraints.NONE,
						new Insets(0, 0, 0, 0), 0, 0));
	}

	/**
	 * Cluster selection Panel is returned to its non-grayed out state
	 * */
	public void enableClusterSelection() {
		JPanel tempPanel = popNClusterPanel;
		tempPanel.setLayout(new GridBagLayout());
		tempPanel.setBorder(BorderFactory.createEmptyBorder());
		tempPanel.removeAll();
		tempPanel.validate();
		tempPanel.setOpaque(false);
		tempPanel.add(browser, new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						0, 0, 0, 0), 0, 0));
	}

	/**
	 * Creates a new instance of EaseInitDialog
	 * 
	 * @param parent
	 *            Parent Frame
	 * @param repository
	 *            Cluster repository to construct <CODE>ClusterBrowser</CODE>
	 * @param annotationLabels
	 *            Annotation types
	 */
	public EASEInitDialog(Frame parent, String[] annotationLabels) {
		super(parent, "EASE: EASE Annotation Analysis", true);
		this.parent = parent;
		font = new Font("Dialog", Font.BOLD, 12);
		listener = new EventListener();
		addWindowListener(listener);

		// config panel
		configPanel = new ConfigPanel();

		JPanel popNClusterPanel = new JPanel(new GridBagLayout());
		popNClusterPanel.setBackground(Color.white);

		browser = new ClusterBrowser(repository);

		JPanel emptyClusterPanel = new JPanel(new GridBagLayout());
		String text = "<center><b>Note: When running EASE in script mode the cluster<br>";
		text += "under analysis is determined by the preceding algorithm<br>";
		text += "that feeds source data into EASE.</center>";
		JTextPane textArea = new JTextPane();
		textArea.setEditable(false);
		textArea.setBackground(Color.lightGray);
		textArea.setContentType("text/html");
		textArea.setText(text);
		emptyClusterPanel.add(textArea, new GridBagConstraints(0, 0, 1, 1, 0.0,
				0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));

		popNClusterPanel.add(emptyClusterPanel, new GridBagConstraints(0, 1, 1,
				1, 1.0, 1.0, GridBagConstraints.CENTER,
				GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

		JPanel parameters = new JPanel(new GridBagLayout());
		popNClusterPanel.setPreferredSize(new Dimension(500, 350));
		parameters.setBackground(Color.white);

		// mode panel
		modePanel = new ModePanel(true);

		// NeasePanel
		nEasePanel = new NEasePanel();

		parameters.add(configPanel, new GridBagConstraints(0, 0, 1, 1, 1.0, 0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						0, 0, 0, 0), 0, 0));
		parameters.add(modePanel, new GridBagConstraints(0, 1, 1, 1, 1.0, 0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						0, 0, 0, 0), 0, 0));

		parameters.add(popNClusterPanel, new GridBagConstraints(0, 2, 1, 1,
				1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));
		parameters.add(nEasePanel, new GridBagConstraints(0, 3, 1, 1, 1.0, 0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						0, 0, 0, 0), 0, 0));

		addContent(parameters);
		setActionListeners(listener);
		this.setSize(550, 700);
	}

	/**
	 * Shows the dialog.
	 * 
	 * @return
	 */
	public int showModal() {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((screenSize.width - getSize().width) / 2,
				(screenSize.height - getSize().height) / 2);
		setVisible(true);
		return result;
	}

	/**
	 * Resets dialog controls. It does not perform a master reset of all dialogs
	 */
	public void resetControls() {
		// Resets components in ConfigPanel
		if (speciestoarrays == null || speciestoarrays.size() == 0) {
			configPanel.organismListBox = new JComboBox();
			configPanel.organismListBox.addItem("No organisms listed");
			configPanel.organismListBox.setEnabled(false);

			configPanel.arrayListBox = new JComboBox();
			configPanel.arrayListBox.addItem("No species listed");
			configPanel.arrayListBox.setEnabled(false);

			configPanel.getEaseSupportFileButton.setEnabled(false);
		} else {
			try {
				if (speciesName != null && arrayName!=null) {
					configPanel.organismListBox.setSelectedItem(speciesName);
					configPanel.arrayListBox.setSelectedItem(arrayName);
				} else {
					configPanel.organismListBox.setSelectedIndex(0);
				}

			} catch (NullPointerException npe) {
				configPanel.arrayListBox.setSelectedItem(0);
			}
		}

		// Resets options in ModePanel
		if (repository == null || repository.isEmpty()) {

		} else {
			modePanel.clusterAnalysisButton.setSelected(true);
			enableClusterSelection();
		}

		// resets options in NestedEase panel
		nEasePanel.nEaseBox.setSelected(false);
	}

	/**
	 * Indicates if mode is cluster analysis, if not mode is annotation survey.
	 * 
	 * @return
	 */
	public boolean isClusterModeSelected() {
		return this.modePanel.clusterAnalysisButton.isSelected();
	}

	/**
	 * Returns the cluster selected for analysis.
	 * 
	 * @return cluster for analysis
	 */
	public Cluster getSelectedCluster() {
		return this.browser.getSelectedCluster();
	}
	/**
	 * 
	 * @return flag indicating whether the user customized EASE
	 */
	public boolean wasCustomDataSet(){
		return isAdvancedAnnotParams;
	}

	/**
	 * Returns a boolean indicating whether a file was chosen for background
	 * selection
	 * */
	public boolean isPopFileModeSelected() {
		if (isAdvancedAnnotParams) {
			return advancedFeat.isPopFileModeSelected();
		}
		return false;

	}

	/**
	 * Returns the path of the population file to load
	 */
	public String getPopulationFileName() {
		if (isAdvancedAnnotParams) {
			if (advancedFeat.isPopFileModeSelected()) {
				return advancedFeat.getPopFieldText();
			}
		}
		return null;
	}

	/**
	 * Returns a flag signaling whether annotation files were previously loaded
	 * */
	public boolean isPreloadedAnnotationSelected() {
		return useLoadedAnnotationFile;
	}

	/**
	 * Returns the name of the converter file selected
	 */
	public String getConverterFileName() {
		if (isAdvancedAnnotParams) {
			if (advancedFeat.isAnnBoxSelected()) {
				return advancedFeat.getConverterFileName();
			}
		}
		return null;
	}

	/**
	 * Returns the base file location for EASE file system
	 */
	public String getBaseFileLocation() {
		return supportFileLocation;
	}

	/**
	 * Returns the annotation key type.
	 */
	public String getAnnotationKeyType() {
		if (isAdvancedAnnotParams) {
			return advancedFeat.getAnnotationKeyType();
		}
		return ANNOTATION_LINK;

	}

	/**
	 * Returns a list of file names corresponding to files mapping indices to
	 * annotation terms (themes).
	 */
	public String[] getAnnToGOFileList() {
		if (isAdvancedAnnotParams) {
			return advancedFeat.getAnnToGOFileList();
		}
		return getAnnFileList();
	}

	/**
	 * Retrieves GO TERMS for current data set
	 * 
	 * @return absolute paths of GO TERM files
	 * */
	public String[] getAnnFileList() {
		String sep;
		File[] list = null;
		String[] paths = null;
		File file;
		String base = getBaseFileLocation();

		sep = System.getProperty("file.separator");
		file = new File(base + sep + "Data" + sep + "Class" + sep);
		if (file.isDirectory()) {
			list = file.listFiles();
			paths = new String[list.length];
			for (int i = 0; i < list.length; i++) {
				paths[i] = list[i].getAbsolutePath();
			}
		}
		return paths;
	}

	/**
	 * Returns the stat to report. If true then EaseScore, else Fisher's Exact.
	 */
	public boolean isEaseScoreSelected() {
		if (isAdvancedStatParams) {
			return (this.statParam.easeBox.isSelected());
		} else {
			return easeScore;
		}
	}

	/**
	 * Returns true if multiplicity corrections are selected.
	 */
	public boolean isCorrectPvaluesSelected() {
		return (isBonferroniSelected() || isStepDownBonferroniSelected()
				|| isSidakSelected() || isHochbergSelected());
	}

	/**
	 * Returns true if Bonferroni correction is selected, false if it is not.
	 */
	public boolean isBonferroniSelected() {
		if (isAdvancedStatParams) {
			return statParam.bonferroniBox.isSelected();
		} else {
			return bonferroni;
		}
	}

	/**
	 * Returns true if step down Bonferroni is selected, false if it is not.
	 */
	public boolean isStepDownBonferroniSelected() {
		if (isAdvancedStatParams) {
			return statParam.bonferroniStepBox.isSelected();
		} else {
			return stepDownBonf;
		}
	}

	/**
	 * Returns true is Sidak method correction is selected, false if it is not.
	 */
	public boolean isSidakSelected() {
		if (isAdvancedStatParams) {
			return statParam.sidakBox.isSelected();
		} else {
			return sidak;
		}
	}

	/**
	 * Returns true if Benjamini-Hochberg correction is selected, false if it is
	 * not.
	 */
	public boolean isHochbergSelected() {
		if (isAdvancedStatParams) {
			return statParam.hochbergBox.isSelected();
		} else {
			return hochberg;
		}
	}

	/**
	 * Returns true if bootstrapping permutations are selected, false if they
	 * are not.
	 */
	public boolean isPermutationAnalysisSelected() {
		if (isAdvancedStatParams) {
			return statParam.permBox.isSelected();
		} else {
			return probAnalysis;
		}
	}

	/**
	 * Returns the number of permutations to perform.
	 */
	public int getPermutationCount() {
		if (isAdvancedStatParams) {
			return Integer.parseInt(statParam.permField.getText());
		} else {
			return permCount;
		}
	}

	/**
	 * Returns true if result groups are to be trimmed
	 * */
	public boolean getTrimSelected() {
		if (isAdvancedStatParams) {
			return statParam.trimBox.isSelected();
		} else {
			return trimSelect;
		}
	}

	/**
	 * Returns true if hit percentage is specified
	 * */
	public boolean getTrimPercentSelected() {
		if (isAdvancedStatParams) {
			return statParam.trimPercentBox.isSelected();
		} else {
			return percentSelect;
		}
	}

	/**
	 * Returns the hit percentage specified
	 * */
	public String getTrimPercentValue() {
		if (isAdvancedStatParams) {
			return statParam.trimPercentField.getText();
		} else {
			return percentField;
		}
	}

	/**
	 * Returns the Minimum hit number specified
	 * */
	public String getTrimMinHits() {
		if (isAdvancedStatParams) {
			return statParam.trimNField.getText();
		} else {
			return hitField;
		}
	}

	/**
	 * Returns the trim options as two strings. The first string indicates the
	 * type of trim NO_TRIM, N_TRIM, PERCENT_TRIM. The second string indicates
	 * the value to be applied.
	 */
	public String[] getTrimOptions() {

		String[] options = new String[2];
		if (getTrimSelected()) {
			if (getTrimPercentSelected()) {
				options[0] = "PERCENT_TRIM";
				options[1] = getTrimPercentValue();
			} else {
				options[0] = "N_TRIM";
				options[1] = getTrimMinHits();
			}
		} else {
			options[0] = "NO_TRIM";
			options[1] = "0";
		}
		return options;
	}

	/**
	 * Returns whether NestedEase is to be used.
	 * */
	public boolean isNEaseSelected() {
		return nEasePanel.nEaseBox.isSelected();
	}

	/**
	 * Contains the controls for NestedEASE and Advanced statistical parameters
	 * 
	 * */

	protected class NEasePanel extends JPanel {
		// Nested EASE parameters
		protected JCheckBox nEaseBox;
		protected JLabel paramLabel;

		/* Button to modify statistical Parameters */
		JButton configureAdvancedOptions;

		public NEasePanel() {
			setBackground(Color.white);
			setLayout(new GridBagLayout());
			setBorder(BorderFactory.createTitledBorder(BorderFactory
					.createEtchedBorder(EtchedBorder.LOWERED),
					"Statistical Parameter Selection",
					TitledBorder.DEFAULT_JUSTIFICATION,
					TitledBorder.DEFAULT_POSITION, font, Color.black));

			nEaseBox = new JCheckBox("Run Nested EASE", false);
			nEaseBox.setActionCommand("run-nease");
			nEaseBox.addActionListener(listener);
			nEaseBox.setBackground(Color.white);

			configureAdvancedOptions = new JButton("Advanced");
			configureAdvancedOptions.setActionCommand("select_stat_parameters");
			configureAdvancedOptions.addActionListener(listener);
			configureAdvancedOptions
					.setToolTipText("Specifies statistical parameters.");

			paramLabel = new JLabel("  Modify Statistical Parameters");

			add(nEaseBox, new GridBagConstraints(0, 0, 1, 1, 0, 0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 95, 1, 0), 0, 0));
			add(paramLabel, new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 95, 0, 0), 0, 0));
			add(configureAdvancedOptions, new GridBagConstraints(1, 1, 1, 1,
					0.0, 0.0, GridBagConstraints.CENTER,
					GridBagConstraints.BOTH, new Insets(0, 0, 3, 130), 0, 0));

		}

	}

	/**
	 * Contains mode controls. (anal. or survey)
	 */
	protected class ModePanel extends JPanel {

		private static final long serialVersionUID = -4927037409254942407L;
		protected JRadioButton clusterAnalysisButton;
		protected JRadioButton slideSurveyButton;

		/**
		 * Constructs a mode panel.
		 * 
		 * @param haveClusters
		 */
		public ModePanel(boolean haveClusters) {

			super(new GridBagLayout());
			setLayout(new GridBagLayout());
			setBackground(Color.white);
			setBorder(BorderFactory.createTitledBorder(BorderFactory
					.createEtchedBorder(EtchedBorder.LOWERED),
					"Mode Selection", TitledBorder.DEFAULT_JUSTIFICATION,
					TitledBorder.DEFAULT_POSITION, font, Color.black));

			ButtonGroup bg = new ButtonGroup();
			clusterAnalysisButton = new JRadioButton("Cluster Analysis",
					haveClusters);
			clusterAnalysisButton.setFocusPainted(false);
			clusterAnalysisButton.setBackground(Color.white);
			clusterAnalysisButton.setHorizontalAlignment(JRadioButton.CENTER);
			bg.add(clusterAnalysisButton);

			clusterAnalysisButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					enableClusterSelection();
				}
			});

			slideSurveyButton = new JRadioButton("Annotation Survey");
			slideSurveyButton
					.setToolTipText("Surveys annotation loaded in the CURRENT viewer.");
			slideSurveyButton.setFocusPainted(false);
			slideSurveyButton.setBackground(Color.white);
			slideSurveyButton.setHorizontalAlignment(JRadioButton.CENTER);

			bg.add(slideSurveyButton);
			slideSurveyButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					disableClusterSelection();

				}
			});

			if (!haveClusters) {
				slideSurveyButton.setSelected(true);
				clusterAnalysisButton.setEnabled(false);
			}

			add(clusterAnalysisButton, new GridBagConstraints(0, 0, 1, 1, 1.0,
					0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 0), 0, 0));
			add(slideSurveyButton, new GridBagConstraints(1, 0, 1, 1, 1.0, 0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 0), 0, 0));
		}
	}

	/**
	 * The topmost panel in the InitDialog, containing the location information
	 * for the EASE file system and the update controls.
	 */
	protected class ConfigPanel extends ParameterPanel {

		private static final long serialVersionUID = -5298900627241870503L;

		JComboBox organismListBox;
		JComboBox arrayListBox;
		JLabel chooseOrg, chooseArray, statusLabel;
		JButton getEaseSupportFileButton;
		JButton configureExtras;

		public ConfigPanel() {

			super("Support Directory /Annotation Parameter Selection");
			setLayout(new GridBagLayout());

			getEaseSupportFileButton = new JButton("Download");
			getEaseSupportFileButton
					.setActionCommand("download-support-file-command");
			getEaseSupportFileButton.addActionListener(listener);
			getEaseSupportFileButton
					.setToolTipText("<html>Downloads EASE annotation files<br>for a selected species and array type.</html>");

			configureExtras = new JButton("Custom");
			configureExtras.setActionCommand("select_extra_parameters");
			configureExtras.addActionListener(listener);
			configureExtras
					.setToolTipText("Specifies support directories, population and Annotation files.");

			chooseOrg = new JLabel("         Organism");
			chooseArray = new JLabel("Array Platform");

			statusLabel = new JLabel("Select This");

			/*
			 * This conditional ensures that the lists of organism and arrays
			 * are populated correctly
			 */
			if (speciestoarrays == null || speciestoarrays.size() == 0) {
				organismListBox = new JComboBox();
				organismListBox.addItem("No organisms listed");
				organismListBox.setEnabled(false);

				arrayListBox = new JComboBox();
				arrayListBox.addItem("No species listed");
				arrayListBox.setEnabled(false);

				getEaseSupportFileButton.setEnabled(false);
			} else {

				if (speciestoarrays.size() > 0) {
					organismListBox = new JComboBox(new Vector<String>(
							speciestoarrays.keySet()));

					try {
						organismListBox.setSelectedItem(speciesName);
					} catch (NullPointerException npe) {
						organismListBox.setSelectedIndex(0);
					}

					if (organismListBox.getSelectedItem() == null) {
						arrayListBox = new JComboBox();
					} else {
						arrayListBox = new JComboBox();
						Vector<String> arraysForThisSpecies = speciestoarrays
								.get(organismListBox.getSelectedItem());
						for (int i = 0; i < arraysForThisSpecies.size(); i++) {
							arrayListBox.addItem(arraysForThisSpecies
									.elementAt(i));
						}
					}
					try {
						arrayListBox.setSelectedItem(arrayName);
					} catch (NullPointerException npe) {
						arrayListBox.setSelectedIndex(0);
					}
				}

				arrayListBox.setEnabled(true);

			}

			arrayListBox.addActionListener(listener);
			arrayListBox.setActionCommand("array-selected-command");
			organismListBox.addActionListener(listener);
			organismListBox.setActionCommand("organism-selected-command");

			add(organismListBox, new GridBagConstraints(1, 1, 1, 1, 1, 0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(5, 30, 0, 0), 0, 0));
			add(arrayListBox, new GridBagConstraints(1, 2, 1, 1, 1, 0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(5, 30, 0, 0), 0, 0));

			add(chooseOrg, new GridBagConstraints(0, 1, 1, 1, 0, 0,
					GridBagConstraints.EAST, GridBagConstraints.BOTH,
					new Insets(10, 30, 0, 0), 0, 0));

			add(chooseArray, new GridBagConstraints(0, 2, 1, 1, 0, 0,
					GridBagConstraints.EAST, GridBagConstraints.BOTH,
					new Insets(5, 30, 0, 0), 0, 0));

			add(statusLabel, new GridBagConstraints(3, 0, 1, 1, 0, 0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(5, 25, 0, 20), 0, 0));
			add(getEaseSupportFileButton, new GridBagConstraints(3, 1, 1, 1, 0,
					0, GridBagConstraints.EAST, GridBagConstraints.BOTH,
					new Insets(5, 25, 0, 20), 0, 0));

			add(configureExtras, new GridBagConstraints(3, 2, 1, 1, 0, 0,
					GridBagConstraints.EAST, GridBagConstraints.BOTH,
					new Insets(5, 25, 0, 20), 0, 0));

			/*
			 * Checks whether the pair of(organism, array) selected is in the
			 * repository or not
			 */
			try {
				boolean b = resourceManager
						.fileIsInRepository(new EASEEntrezSupportDataFile(
								organismListBox.getSelectedItem().toString(),
								arrayListBox.getSelectedItem().toString()));
				if (b) {
					getEaseSupportFileButton.setText("Select This");
				} else {
					getEaseSupportFileButton.setText("Download");
				}
			} catch (NullPointerException npe) {
				getEaseSupportFileButton.setText("Download");
			}
			updateSelection();
		}

		/* It is triggered after the user clicks the Select Button */
		private void onDownloadSupportFile() {
			EASEEntrezSupportDataFile esdf = null;
			/* Brings back the url of the file for the species and array */
			try {
				String species = organismListBox.getSelectedItem().toString();
				String array = arrayListBox.getSelectedItem().toString();
				esdf = new EASEEntrezSupportDataFile(species, array);
				f = resourceManager.getSupportFile(esdf, true);

				/*
				 * SupportFileLocation changes when the user picks any new
				 * organism/array combination
				 */
				supportFileLocation = f.getAbsolutePath();

				getEaseSupportFileButton.setText("Done");
				statusLabel.setText("     Selected");
				getEaseSupportFileButton.setEnabled(false);
				configureExtras.setText("Advanced");
				isSelected = true;
			} catch (SupportFileAccessError sfae) {
				statusLabel.setText("Failure");
				String easeURL = null;
				try {
					easeURL = esdf.getURL().toString();
				} catch (MalformedURLException mue) {
				}
				ShowThrowableDialog.show(parent,
						"Unable to download EASE files", true,
						ShowThrowableDialog.ERROR, sfae,
						"unable to download file from " + easeURL);
				sfae.printStackTrace();
			} catch (NullPointerException npe) {
				statusLabel.setText("Failure");
				ShowThrowableDialog.show(parent,
						"Unable to download EASE files", true,
						ShowThrowableDialog.ERROR, npe,
						"Unable to download EASE files at this time. ");
			}
		}

		public void selectSpecies() {
			arrayListBox.removeAllItems();
			Vector<String> arraysForThisSpecies = speciestoarrays
					.get(organismListBox.getSelectedItem());

			for (int i = 0; i < arraysForThisSpecies.size(); i++) {
				arrayListBox.addItem(arraysForThisSpecies.elementAt(i));
			}
		}

		public void updateSelection() {
			if (arrayListBox.getSelectedItem() == null) {
				return;
			}
			String selectedOrganism = organismListBox.getSelectedItem()
					.toString();
			String selectedArray = arrayListBox.getSelectedItem().toString();
			if (selectedOrganism != null && selectedArray != null) {
				if (resourceManager
						.fileIsInRepository(new EASEEntrezSupportDataFile(
								selectedOrganism, selectedArray))) {
					statusLabel.setText("Click to Select");
					getEaseSupportFileButton.setText("Select");
				} else {
					statusLabel.setText("Click to Download");
					getEaseSupportFileButton.setText("Download");
				}
				getEaseSupportFileButton.setEnabled(true);
				try {
					ResourcererAnnotationFileDefinition def = new ResourcererAnnotationFileDefinition(
							speciesName, arrayName);
					if(speciesName==null || arrayName==null){
						
					}else{
					annotationFile = resourceManager.getSupportFile(def, false);
					}
				} catch (SupportFileAccessError sfae) {
					sfae.printStackTrace();
					useLoadedAnnotationFile = false;
				}
			} else {
				getEaseSupportFileButton.setEnabled(false);
			}
		}

	}

	/**
	 * The class to listen to the dialog and check boxes items events.
	 */
	protected class EventListener extends DialogListener implements

	ItemListener {

		public void actionPerformed(ActionEvent e) {

			String command = e.getActionCommand();

			if (command.equals("select_stat_parameters")) {
				if (!isAdvancedStatParams) {
					statParam = new EASEStatParam(parent);
					statParamTemp=new EASEStatParam(parent);
					resultStat=statParam.showModal();
				} else {
					/*Copies the contents of the components in the current dialog*/
					copyStatElements(statParam,statParamTemp);
					resultStat=statParam.showModal();
				}
				if(resultStat==JOptionPane.OK_OPTION){
					/* Update default values if user hits OK */
					isAdvancedStatParams = true;
				}else{
					/*Copies the contents of the components from the previous version 
					 into the dialog*/
					copyStatElements(statParamTemp,statParam);
				}
				
			} else if (command.equals("select_extra_parameters")) {
				if(!isAdvancedAnnotParams){
					advancedFeat = new EASEAdvancedFeatures(parent,
					supportFileLocation, isClusterModeSelected(),
					repository, labels);
					advancedFeatTemp= new EASEAdvancedFeatures(parent,
							supportFileLocation, isClusterModeSelected(),
							repository, labels);
					resultAdvanced=advancedFeat.showModal();
				}else{
					copyAnnotationElements(advancedFeat,advancedFeatTemp);
					resultAdvanced=advancedFeat.showModal();
				}
				/* Update default values if user hits OK */
				if (resultAdvanced == JOptionPane.OK_OPTION) {
					updateSupportFileSelection();
					isAdvancedAnnotParams = true;
					
					/*Disables the Accept Button so that users know that they have chosen all parameters needed*/
					configPanel.configureExtras.setText("Configured");
					configPanel.statusLabel.setText("     Selected");
					configPanel.getEaseSupportFileButton.setText("Done");
					configPanel.getEaseSupportFileButton.setEnabled(false);
				}else{
					/* If not, keep current default values in place */
					copyAnnotationElements(advancedFeatTemp,advancedFeat);
				}
				
			} else if (command.equals("organism-selected-command")) {
				configPanel.selectSpecies();
				configPanel.updateSelection();
			} else if (command.equals("array-selected-command")) {
				configPanel.updateSelection();
			} else if (command.equals("download-support-file-command")) {
				Thread thread = new Thread(new Runnable() {
					public void run() {
						try {
							configPanel.onDownloadSupportFile();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

				});

				thread.setPriority(Thread.MIN_PRIORITY);
				thread.start();

			} else if (command.equals("ok-command")) {
				result = JOptionPane.OK_OPTION;
				// if the user has clicked on the select button or accessed
				// advanced options then proceed
				if (isSelected && !isAdvancedAnnotParams) {
					if (!useLoadedAnnotationFile) {						
						JOptionPane
								.showMessageDialog(
										parent,
										"You have not provided Annotation Files for the organism/array selected."
												+ "\n"
												+ "Please load annotation files into Mev by selecting Import Resourcerer Gene Annotation on the utilities menu.",
										"EASE Initialization: Missing Parameter",
										JOptionPane.WARNING_MESSAGE);
						/*To validate that the path of support files was
						 obtained*/
					} else {
						dispose();
					}
				} else if (!isSelected && !isAdvancedAnnotParams) {
					JOptionPane
							.showMessageDialog(
									parent,
									"You have not selected a source for support files."
											+ "\n"
											+ "Please select an organism/array plataform or go to advanced options to browse for a directory.",
									"EASE Initialization: Missing Parameter",
									JOptionPane.WARNING_MESSAGE);
					configPanel.getEaseSupportFileButton.requestFocusInWindow();

				}else {
					dispose();
				}

			} else if (command.equals("cancel-command")) {
				result = JOptionPane.CANCEL_OPTION;
				dispose();
			} else if (command.equals("reset-command")) {
				resetControls();
				result = JOptionPane.CANCEL_OPTION;
				return;
			} else if (command.equals("info-command")) {
				HelpWindow hw = new HelpWindow(EASEInitDialog.this,
						"EASE Initialization Dialog");
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

		/**
		 * Copies the contents of the components in one EASEAdvancedFeatures dialog to another
		 * */
		public void copyAnnotationElements(EASEAdvancedFeatures source,
				EASEAdvancedFeatures destination) {
	
			destination.configPanelExtension.supportFileLocationField.setText(source.configPanelExtension.supportFileLocationField.getText());	
			
			destination.popPanel.fileButton.setSelected(source.popPanel.fileButton.isSelected());
			destination.popPanel.dataButton.setSelected(source.popPanel.dataButton.isSelected());
			destination.popPanel.popField.setText(source.popPanel.popField.getText());
			destination.popPanel.popField.setBackground(source.popPanel.popField.getBackground());
			destination.popPanel.popField.setEnabled(source.popPanel.popField.isEnabled());
			destination.popPanel.browseButton.setEnabled(source.popPanel.browseButton.isEnabled());
			destination.popPanel.fileLabel.setEnabled(source.popPanel.fileLabel.isEnabled());

			destination.paramPanel.fieldNamesBox.setSelectedIndex(source.paramPanel.fieldNamesBox.getSelectedIndex());
			destination.paramPanel.useAnnBox.setSelected(source.paramPanel.useAnnBox.isSelected());
			destination.paramPanel.browserButton.setEnabled(source.paramPanel.browserButton.isEnabled());
			destination.paramPanel.converterFileField.setText(source.paramPanel.converterFileField.getText());
			destination.paramPanel.converterFileField.setEnabled(source.paramPanel.converterFileField.isEnabled());
			destination.paramPanel.converterFileField.setBackground(source.paramPanel.converterFileField.getBackground());
			destination.paramPanel.fileLabel.setEnabled(source.paramPanel.fileLabel.isEnabled());
			
			destination.paramPanel.removeButton.setEnabled(source.paramPanel.removeButton.isEnabled());		
	
			/*I pull out all objects contained in the source ListModel, clear out the destination ListModel 
			 * and add new elements to it */
			int sourceSize=source.paramPanel.annFileList.getModel().getSize();			
			Object [] baseFiles=((DefaultListModel) source.paramPanel.annFileList.getModel()).toArray();
		    ((DefaultListModel) destination.paramPanel.annFileList.getModel()).clear();
			
			for (int i = 0; i < sourceSize; i++) {
				((DefaultListModel) destination.paramPanel.annFileList.getModel()).addElement(baseFiles[i]);			
			}
		}

		/**
		 * Copies the contents of the components in one EASEStatParam dialog to another
		 * */
		public void copyStatElements(EASEStatParam source, EASEStatParam destination) {
			
			destination.fisherBox.setSelected(source.fisherBox.isSelected());
			destination.hochbergBox.setSelected(source.hochbergBox.isSelected());
			destination.trimBox.setSelected(source.trimBox.isSelected());
			destination.bonferroniBox.setSelected(source.bonferroniBox.isSelected());
			destination.bonferroniStepBox.setSelected(source.bonferroniStepBox.isSelected());
			destination.sidakBox.setSelected(source.sidakBox.isSelected());
			destination.permBox.setSelected(source.permBox.isSelected());
			destination.permField.setText(source.permField.getText());
			destination.trimNBox.setSelected(source.trimNBox.isSelected());
			destination.trimNField.setText(source.trimNField.getText());
			destination.trimPercentBox.setSelected(source.trimPercentBox.isSelected());
			destination.trimPercentField.setText(source.trimPercentField.getText());
			destination.permLabel.setEnabled(source.permLabel.isEnabled());
			destination.permField.setEnabled(source.permField.isEnabled());
			destination.trimNBox.setEnabled(source.trimNBox.isEnabled());
			destination.trimNLabel.setEnabled(source.trimNLabel.isEnabled());
			destination.trimNField.setEnabled(source.trimNField.isEnabled());
			destination.trimPercentBox.setEnabled(source.trimPercentBox.isEnabled());
			destination.trimPercentLabel.setEnabled(source.trimPercentLabel.isEnabled());
			destination.trimPercentField.setEnabled(source.trimPercentField.isEnabled());
		}

		/**
		 * It updates the variable supportFileLocation with the most recent
		 * selection for support file directories
		 * */
		public void updateSupportFileSelection() {
			supportFileLocation = advancedFeat.getNewSupportFileLocation();
		}

		public void itemStateChanged(ItemEvent e) {

			// okButton.setEnabled(genes_box.isSelected() ||
			// cluster_box.isSelected());
		}

		public void windowClosing(WindowEvent e) {

			result = JOptionPane.CLOSED_OPTION;
			dispose();
		}
	}

	/**
	 * Prints the currently-selected options to the console. For testing
	 * purposes.
	 */
	public String summarizeSelections() {
		if (isAdvancedAnnotParams) {
			String returnString = "";
			returnString += advancedFeat.getAnnBoxText() + ": "
					+ advancedFeat.isAnnBoxSelected();
			return returnString;
		}
		return "Annotation box was not selected";
	}

	public static void main(String[] args) {

		try {
			IResourceManager rm = new FileResourceManager(new File(new File(
					System.getProperty("user.home"), ".mev"), "repository"));
			String[] labels = new String[3];
			labels[0] = "TC#";
			labels[1] = "GB#";
			labels[2] = "Role";

			Hashtable<String, Vector<String>> speciestoarrays = new Hashtable<String, Vector<String>>();
			PipelinedAnnotationsFileDefinition speciestoarray = new PipelinedAnnotationsFileDefinition();
			try {
				File f = rm.getSupportFile(speciestoarray, true);
				speciestoarrays = speciestoarray.parseAnnotationListFile(f);
			} catch (SupportFileAccessError sfae) {
				sfae.printStackTrace();
				Vector<String> temp = new Vector<String>();
				temp.add("HG_U133A");
				temp.add("APPLERA_ABI1700");
				speciestoarrays.put("Human", temp);
				Vector<String> temp2 = new Vector<String>();
				temp2.add("junk");
				temp2.add("junk2");
				speciestoarrays.put("Junk", temp2);
			} catch (IOException ioe) {
				ioe.printStackTrace();
				Vector<String> temp = new Vector<String>();
				temp.add("HG_U133A");
				temp.add("APPLERA_ABI1700");
				speciestoarrays.put("Human", temp);
				Vector<String> temp2 = new Vector<String>();
				temp2.add("junk");
				temp2.add("junk2");
				speciestoarrays.put("Junk", temp2);
			}

			EASEInitDialog eid = new EASEInitDialog(new JFrame(),
					new ClusterRepository(0), labels, "", rm, "Human",
					"HG_U133A", speciestoarrays, false);

			eid.showModal();

			/*
			 * Prints all variables passed to a from all frames involving EASE
			 * analysis
			 */
//
//			 System.out.println("Advanced params active" + eid.isAdvancedAnnotParams);
//			 /* Testing File location */
//			 System.out.println("File Location: " + eid.supportFileLocation);
//			
//			 /* Testing Mode Selection */
//			 System.out.println("Is cluster Mode selected:"
//			 + eid.isClusterModeSelected());
//			
//			 System.out.println(eid.summarizeSelections());
//			 System.out.println("Annotation Key Type: "
//			 + eid.getAnnotationKeyType());
//			 System.out.println("Nested ease" + eid.isNEaseSelected());
//			
//			 System.out.println("These are the annotation files: ");
//			 String[] files = eid.getAnnToGOFileList();
//			
//			 for (int c = 0; c < files.length; c++) {
//			 System.out.println(files[c]);
//			 }
//			 System.out.println("Converter File name:" +eid.getConverterFileName());
//			
//			 System.out.println("Is popFile mode selected: "
//			 + eid.isPopFileModeSelected());
//			
//			 /* Testing of Statistical Parameter Selections */
//			 System.out.println("Ease score Selected:"
//			 + eid.isEaseScoreSelected());
//			 System.out.println("Bonferroni selected:"
//			 + eid.isBonferroniSelected());
//			 System.out.println("Step down bonferroni selected:"
//			 + eid.isStepDownBonferroniSelected());
//			 System.out.println("Sidak :" + eid.isSidakSelected());
//			 System.out.println("Hochberg: " + eid.isHochbergSelected());
//			 System.out.println("Perm selected:"
//			 + eid.isPermutationAnalysisSelected());
//			 System.out
//			 .println("the perm count is:" + eid.getPermutationCount());
//			 String[] argss = eid.getTrimOptions();
//			 System.out.println("Trim options:" + argss[0]);
//			 System.out.println("Trim options:" + argss[1]);
//			 
//			 System.out.println("This is the path of the class files"+ eid.getImpliesFileLocation());
//			 if(!new File(eid.getImpliesFileLocation()+"Implies").exists() ||
//		        		! new File(eid.getImpliesFileLocation()+"Implies").isDirectory()){
//				 System.out.println("The path provided for the implies file was incorrect");
//			 }
		} catch (RepositoryInitializationError rie) {
			rie.printStackTrace();
		}
	}

	/**
	 * Extracts the base file path from one of the GO linking annotation files
	 * */
	public String getImpliesFileLocation() {
		String [] path= getAnnToGOFileList();
		String anyPath= "";
		StringTokenizer tok; 
		String temporary="";
		if(path.length>0){
			tok=new StringTokenizer(path[0], sep);
			while(tok.hasMoreTokens()){
				anyPath=tok.nextToken();
				temporary+=anyPath+sep;
				if(anyPath.equals("Class")){
					break;
				}
				
			}
		}
		return temporary+"Implies";
	}

}
