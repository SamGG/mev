/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * Created on Aug 25, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.tigr.microarray.mev.cluster.gui.impl.tease;

import java.awt.Color;
import java.awt.Component;
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
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.tigr.microarray.mev.TMEV;
import org.tigr.microarray.mev.annotation.AnnotationFieldConstants;
import org.tigr.microarray.mev.cluster.clusterUtil.Cluster;
import org.tigr.microarray.mev.cluster.gui.helpers.ClusterBrowser;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DistanceMetricPanel;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.ParameterPanel;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.HelpWindow;
import org.tigr.microarray.mev.cluster.gui.impl.ease.EASEGUI;
import org.tigr.microarray.mev.cluster.gui.impl.ease.EASEInitDialog;
import org.tigr.microarray.mev.cluster.gui.impl.ease.EASESupportDataFile;
import org.tigr.microarray.mev.resources.IResourceManager;
import org.tigr.microarray.mev.resources.ResourcererAnnotationFileDefinition;
import org.tigr.microarray.mev.resources.SupportFileAccessError;


/**
 * @author Annie Liu
 * @version Aug 25, 2005
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class TEASEInitDialog extends AlgorithmDialog{
	
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int result = JOptionPane.CANCEL_OPTION;
    
    private ConfigPanel configPanel;
    private ModePanel modePanel;
    private PopSelectionPanel popPanel;
    private ColorBoundaryPanel colorPanel;
    private ClusterBrowser browser;
    private EventListener listener;
    private EaseParameterPanel easeParamPanel;
    private HCLSelectionPanel hclPanel;
    private AlphaPanel alphaPanel;
    private JTabbedPane tabbedPane;
    private Font font;
    private String sep;
    private Frame parent;

    private static String ANNOTATION_LINK = AnnotationFieldConstants.TGI_TC;
    protected boolean useLoadedAnnotationFile = false;
    protected File defaultEaseFileLocation;
    
    protected String speciesName;
    protected String arrayName;
    protected IResourceManager resourceManager;
    protected Hashtable<String, Vector<String>> speciestoarrays;
    protected File annotationFile;

    
    /** Creates a new instance of EaseInitDialog
     * @param parent Parent Frame
     * @param repository Cluster repository to construct <CODE>ClusterBrowser</CODE>
     * @param annotationLabels Annotation types
     */
    public TEASEInitDialog(Frame parent, String [] annotationLabels, String globalMetricName, 
    		boolean globalAbsoluteDistance, boolean showDistancePanel, File defaultEaseFileLocation, IResourceManager rm, String speciesName, String arrayName, Hashtable<String, Vector<String>> speciestoarrays) {
        super(parent, "TEASE: TEASE Annotation Analysis", true);
        this.speciesName = speciesName;
        this.arrayName = arrayName;
        this.resourceManager = rm;
        this.speciestoarrays = speciestoarrays;
        
        if(defaultEaseFileLocation == null) {
        	this.useLoadedAnnotationFile = false;
        	defaultEaseFileLocation = new File(TMEV.getSettingForOption(TEASEGUI.LAST_TEASE_FILE_LOCATION));
        } else {
        	this.useLoadedAnnotationFile = true;
            this.defaultEaseFileLocation = defaultEaseFileLocation;
        }
        if(defaultEaseFileLocation == null || !defaultEaseFileLocation.canRead()) {
        	defaultEaseFileLocation = new File("./data/ease");
        }
        
        
        this.parent = parent;
        this.font = new Font("Dialog", Font.BOLD, 12);
        this.listener = new EventListener();
        addWindowListener(this.listener);
        
        //Tabbed pane creation
        this.tabbedPane = new JTabbedPane();
        
        //config panel        
        this.configPanel = new ConfigPanel();        
        
        //create hcl panel and add to tabbed pane
        this.hclPanel = new HCLSelectionPanel(globalMetricName, globalAbsoluteDistance, showDistancePanel);
        this.hclPanel.setTreeSelectionControls(false);
        this.tabbedPane.add("HCL Clustering", hclPanel);
        
        //create population panel, add to popNClusterPanel, add popNClusterPanel to tabbed pane
        JPanel popNClusterPanel = new JPanel(new GridBagLayout());
        popNClusterPanel.setBackground(Color.white);
        this.popPanel = new PopSelectionPanel();
        this.colorPanel = new ColorBoundaryPanel();
        popNClusterPanel.add(this.popPanel, new GridBagConstraints(0,0,1,1,1.0,0.0,GridBagConstraints.CENTER,
        		GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
        popNClusterPanel.add(this.colorPanel, new GridBagConstraints(0,1,1,1,1.0,0.0,GridBagConstraints.CENTER,
        		GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
        this.tabbedPane.add("Population/Color", popNClusterPanel);
        
        //create ease parameter panel and add to tabbed pane
        this.easeParamPanel = new EaseParameterPanel(annotationLabels);
        this.tabbedPane.add("Annotation Parameters", easeParamPanel);
        
        //create analysis parameter panel and add to tabbed pane
        this.alphaPanel = new AlphaPanel();
        this.tabbedPane.add("Statistical Parameters", alphaPanel);

        //create a j panel to be added to frame
        JPanel parameters = new JPanel(new GridBagLayout());
        parameters.setBackground(Color.white);
        
        //mode panel
        this.modePanel = new ModePanel();
        
        parameters.add(this.configPanel, new GridBagConstraints(0,0,1,1,1.0,0,GridBagConstraints.CENTER,
        		GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));       
        parameters.add(this.modePanel, new GridBagConstraints(0,1,1,1,1.0,0,GridBagConstraints.CENTER,
        		GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
        parameters.add(this.tabbedPane, new GridBagConstraints(0,2,1,1,1.0,1.0,GridBagConstraints.CENTER,
        		GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
        
        this.addContent(parameters);
        this.setActionListeners(this.listener);
        
        this.setSize(570,750);
    }
    
    public File getDefaultBaseFileLocation() {
    	return defaultEaseFileLocation;
    }
    /** Shows the dialog.
     * @return  */
    public int showModal() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);
        setVisible(true);
        return result;
    }
    
    /**
     * Resets controls to default initial settings
     */
    private void resetControls(){
        //reset HCL tab
    	this.hclPanel.ALC.setSelected(true);
        this.hclPanel.metricPanel.reset();
        this.hclPanel.maxField.setText("100");
        this.hclPanel.minField.setText("10");
        
        //reset 
        this.alphaPanel.fisherBox.setSelected(true);
    }
    /**
     * get upper boundary for color coded dots (blue)
     * @return
     */
    public String getUpperBoundary() {
    	return this.colorPanel.upperField.getText();
    }
    
    /**
     * get lower boundary for color coded dots (red)
     * @return
     */
    public String getLowerBoundary() {
    	return this.colorPanel.lowerField.getText();
    }
    
    public String isHCLOnly() {
    	return String.valueOf(this.modePanel.hclOnlyButton.isSelected());
    }
    
    /** I
     * ndicates if mode is cluster analysis, if not mode is annotation survey.
     * @return  
     */
    public boolean isClusterModeSelected(){
        return this.modePanel.clusterAnalysisButton.isSelected();
    }
    
    /** Returns the cluster selected for analysis.
     * @return  */
    public Cluster getSelectedCluster(){
        return this.browser.getSelectedCluster();
    }
    
    
    public boolean isPopFileModeSelected() {
        return popPanel.fileButton.isSelected();
    } 
    public boolean isPreloadedAnnotationSelected() {
    	return popPanel.preloadedAnnotationButton.isSelected();
    }
    /**
     * Returns a method code.
     * @return 0 for ALC method, 1 for CLC or -1 otherwise.
     */
    public int getMethod() {
        if (this.hclPanel.ALC.isSelected()) {
            return 0;
        }
        if (this.hclPanel.CLC.isSelected()) {
            return 1;
        }
        return -1;
    }
    
    /**
     * Returns the currently selected metric
     */
    public int getDistanceMetric() {
        return this.hclPanel.metricPanel.getMetricIndex();
    }
    
    /**
     *  Returns true if the absolute checkbox is selected, else false
     */
    public boolean getAbsoluteSelection() {
        return this.hclPanel.metricPanel.getAbsoluteSelection();
    }
    
    /** Returns the population fille to load
     */
    public String getPopulationFileName() {
        return this.popPanel.getPopFile();
    }
    
    /** Returns the name of the converter file selected.
     * If none selected null is returned.
     */
    public String getConverterFileName(){
        return easeParamPanel.getConverterFileName();
    }
    
    /** Returns the minimum clusters size if trimming result.
     */
    public int getMinClusterSize() {
        String value = easeParamPanel.minClusterSizeField.getText();
        try {
            int size = Integer.parseInt(value);
            return size;
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    /** Returns the base file location for EASE file system
     */
    public String getBaseFileLocation() {
        return configPanel.getBaseFileLocation();
    }
    
    /** Returns the annotation key type.
     */
    public String getAnnotationKeyType(){
        return easeParamPanel.getAnnotationKeyType();
    }
    
    
    /** Returns a list of file names corresponding to files mapping
     * indices to annotation terms (themes).
     */
    public String [] getAnnToGOFileList(){
        return this.easeParamPanel.getAnnToGOFileList();
    }
    
    /**
     * Return the max number of genes in EASE analysis specified by user
     * @return 
     */
    public String getMaxNumber() {
    	return this.hclPanel.maxField.getText();
    }
    
    /**
     * Return the min number of genes in EASE analysis specified by user
     * @return 
     */
    public String getMinNumber() {
    	return this.hclPanel.minField.getText();
    }
    
    public boolean isGeneTreeSelected() {
    	return this.hclPanel.genes_box.isSelected();
    }
    
    public boolean isSampleTreeSelected() {
    	return this.hclPanel.sample_box.isSelected();
    }
    
    /** Returns the stat to report.  If true then EaseScore, else Fisher's Exact.
     */
    public boolean isEaseScoreSelected(){
        return (this.alphaPanel.easeBox.isSelected());
    }
    
    /** Returns true if multiplicity corrections are
     * selected.
     */
    public boolean isCorrectPvaluesSelected(){
        return (this.isBonferroniSelected() || this.isStepDownBonferroniSelected() || this.isSidakSelected());
    }
    
    /** Returns true if Bonferroni correction is selected.
     */
    public boolean isBonferroniSelected(){
        return alphaPanel.bonferroniBox.isSelected();
    }
    
    /** Returns true if step down Bonferroni is selected.
     */
    public boolean isStepDownBonferroniSelected(){
        return alphaPanel.bonferroniStepBox.isSelected();
    }
    
    /** Returns true is Sidak method correction is selected
     */
    public boolean isSidakSelected(){
        return alphaPanel.sidakBox.isSelected();
    }
    
    /** Returns true if bootstrapping permutations
     * are selected.
     */
    public boolean isPermutationAnalysisSelected(){
        return alphaPanel.permBox.isSelected();
    }
    
    /** Returns the number of permutations to perform.
     */
    public int getPermutationCount(){
        return Integer.parseInt(alphaPanel.permField.getText());
    }
    
    /** Returns the trim options as two strings.
     * The first string indicates the type of trim
     * NO_TRIM, N_TRIM, PERCENT_TRIM.  The second string indicates the value to be applied.
     */
    public String [] getTrimOptions(){
        String [] options = new String[2];
        if ( alphaPanel.trimBox.isSelected() ){
            if ( alphaPanel.trimPercentBox.isSelected() ){
                options[0] = "PERCENT_TRIM";
                options[1] = alphaPanel.trimPercentField.getText();
            } else {
                options[0] = "N_TRIM";
                options[1] = alphaPanel.trimNField.getText();
            }
        } else {
            options[0] = "NO_TRIM";
            options[1] = "0";
        }
        return options;
    }
    
    
    
    /** Contains mode controls. (anal. or survey)
     */
    private class ModePanel extends JPanel {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private JRadioButton clusterAnalysisButton;
        private JRadioButton slideSurveyButton;
        private JRadioButton hclOnlyButton;
        
        /** Constructs a mode panel.
         * @param haveClusters
         */
        public ModePanel(){
            super(new GridBagLayout());
            setLayout(new GridBagLayout());
            setBackground(Color.white);
            setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), 
            		"Mode Selection", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
					font, Color.black));
            
            ButtonGroup bg = new ButtonGroup();
            clusterAnalysisButton = new JRadioButton("Cluster Analysis");
            clusterAnalysisButton.setFocusPainted(false);
            clusterAnalysisButton.setBackground(Color.white);
            clusterAnalysisButton.setHorizontalAlignment(JRadioButton.CENTER);
            clusterAnalysisButton.setSelected(true);
            clusterAnalysisButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    popPanel.setEnableControls(true);
                    colorPanel.setEnableControls(true);
                    easeParamPanel.setEnableControls(true);
                    alphaPanel.setEnableControls(true);
                }
            });
            bg.add(clusterAnalysisButton);
            
            slideSurveyButton = new JRadioButton("Annotation Survey");
            slideSurveyButton.setToolTipText("Surveys annotation loaded in the CURRENT viewer.");
            slideSurveyButton.setFocusPainted(false);
            slideSurveyButton.setBackground(Color.white);
            slideSurveyButton.setHorizontalAlignment(JRadioButton.CENTER);
            slideSurveyButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    popPanel.setEnableControls(false);
                    colorPanel.setEnableControls(false);
                    easeParamPanel.setEnableControls(true);
                    alphaPanel.setEnableControls(true);
                    hclPanel.setTreeSelectionControls(false);
                }
            });
            bg.add(slideSurveyButton);
            
            hclOnlyButton = new JRadioButton("HCL Only");
            hclOnlyButton.setToolTipText("Clustering without searching");
            hclOnlyButton.setFocusPainted(false);
            hclOnlyButton.setBackground(Color.white);
            hclOnlyButton.setHorizontalAlignment(JRadioButton.CENTER);
            hclOnlyButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    popPanel.setEnableControls(false);
                    colorPanel.setEnableControls(false);
                    easeParamPanel.setEnableControls(false);
                    alphaPanel.setEnableControls(false);
                    hclPanel.setTreeSelectionControls(true);
                }
            });
            bg.add(hclOnlyButton);
            
            add(hclOnlyButton, new GridBagConstraints(0,0,1,1,1.0,0,GridBagConstraints.CENTER, 
            		GridBagConstraints.BOTH, new Insets(0,0,0,0), 0, 0));
            add(clusterAnalysisButton, new GridBagConstraints(1,0,1,1,1.0,0,GridBagConstraints.CENTER, 
            		GridBagConstraints.BOTH, new Insets(0,0,0,0), 0, 0));
            add(slideSurveyButton, new GridBagConstraints(2,0,GridBagConstraints.REMAINDER,1,1.0,0,
            		GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0, 0));

        }
    }
    
    private class PopSelectionPanel extends ParameterPanel {
        
		JRadioButton preloadedAnnotationButton;
        JRadioButton fileButton;
        JRadioButton dataButton;
        JTextField popField;
        JButton browseButton;
        JLabel fileLabel;
        
        public PopSelectionPanel() {
            super("Population Selection");
            setLayout(new GridBagLayout());
            
            ButtonGroup bg = new ButtonGroup();
            
            if(useLoadedAnnotationFile) {
            	preloadedAnnotationButton = new JRadioButton("Use loaded array population as background", true);
            } else {
                preloadedAnnotationButton = new JRadioButton("Use loaded array population as background (annotation not loaded)", true);
            }
            preloadedAnnotationButton.setBackground(Color.white);
            preloadedAnnotationButton.setFocusPainted(false);
            bg.add(preloadedAnnotationButton);
            preloadedAnnotationButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                	//disable 
                	easeParamPanel.fieldNamesBox.setSelectedItem(ANNOTATION_LINK);
                	easeParamPanel.fieldNamesBox.setEnabled(!preloadedAnnotationButton.isSelected());
                	easeParamPanel.useAnnBox.setEnabled(!preloadedAnnotationButton.isSelected());
                    browseButton.setEnabled(!preloadedAnnotationButton.isSelected());
                    popField.setEnabled(!preloadedAnnotationButton.isSelected());
                    popField.setBackground(Color.lightGray);
                    fileLabel.setEnabled(!preloadedAnnotationButton.isSelected());
                    
                }
            });  


            fileButton = new JRadioButton("Select Background Population from File");
            fileButton.setBackground(Color.white);
            fileButton.setFocusPainted(false);
            bg.add(fileButton);
            
            fileButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                	easeParamPanel.fieldNamesBox.setEnabled(!preloadedAnnotationButton.isSelected());
                	easeParamPanel.useAnnBox.setEnabled(!preloadedAnnotationButton.isSelected());
                    browseButton.setEnabled(fileButton.isSelected());
                    popField.setEnabled(fileButton.isSelected());
                    popField.setBackground(Color.white);
                    fileLabel.setEnabled(fileButton.isSelected());
                    
                }
            });
            
            dataButton = new JRadioButton("Select Background Population from Current Viewer");
            dataButton.setBackground(Color.white);
            dataButton.setFocusPainted(false);
            bg.add(dataButton);
            dataButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    browseButton.setEnabled(fileButton.isSelected());
                    popField.setEnabled(fileButton.isSelected());
                    popField.setBackground(Color.lightGray);
                    fileLabel.setEnabled(fileButton.isSelected());
                    
                }
            });
            
            browseButton = new JButton("File Browser");
            browseButton.setFocusPainted(false);
            browseButton.setPreferredSize(new Dimension(150, 25));
            browseButton.setSize(150, 25);
            browseButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                	JFileChooser chooser = new JFileChooser(new File(getBaseFileLocation(), "Lists"));
                    chooser.setDialogTitle("Population File Selection");
                    chooser.setMultiSelectionEnabled(false);
                    if(chooser.showOpenDialog(parent) == JOptionPane.OK_OPTION){
                        updatePopField(chooser.getSelectedFile().getPath());
                        if(TMEV.getSettingForOption(TEASEGUI.LAST_TEASE_FILE_LOCATION) == null) {
	                        TMEV.storeProperty(TEASEGUI.LAST_TEASE_FILE_LOCATION, chooser.getSelectedFile().getParentFile().getParentFile().getParentFile().getParent());
                        }
                    }
                }
            });
            
            fileLabel = new JLabel("File: ");
            popField = new JTextField(25);
            


            //Enable the preloaded population annotation options only if the annotation is available. 
            //Otherwise, "population from file" is the default option.
            fileButton.setSelected(!useLoadedAnnotationFile);
            preloadedAnnotationButton.setSelected(useLoadedAnnotationFile);
            preloadedAnnotationButton.setEnabled(useLoadedAnnotationFile);
            
            browseButton.setEnabled(fileButton.isSelected());
            popField.setEnabled(fileButton.isSelected());
            fileLabel.setEnabled(fileButton.isSelected());

            if(fileButton.isSelected())
                popField.setBackground(Color.white);
            else
            	popField.setBackground(Color.lightGray);


            add(preloadedAnnotationButton, 	new GridBagConstraints(0,0,3,1,1,0,GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(10,30,0,0), 0,0));
            add(fileButton, 				new GridBagConstraints(0,1,3,1,1,0,GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(10,30,0,0), 0,0));
            add(fileLabel, 					new GridBagConstraints(0,2,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,30,0,0), 0,0));
            add(popField, 					new GridBagConstraints(1,2,1,1,1,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,10,0,0), 0,0));
            add(browseButton, 				new GridBagConstraints(2,2,1,1,0,0,GridBagConstraints.CENTER,	GridBagConstraints.BOTH, new Insets(5,25,0,20), 0,0));
            
            add(dataButton, 				new GridBagConstraints(0,3,3,1,1,0,GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(15,30,20,0), 0,0));
        }
        
        protected void setEnableControls(boolean enable) {
        	preloadedAnnotationButton.setEnabled(enable && useLoadedAnnotationFile);
        	easeParamPanel.fieldNamesBox.setEnabled(!preloadedAnnotationButton.isSelected());
        	easeParamPanel.useAnnBox.setEnabled(!preloadedAnnotationButton.isSelected());
            fileButton.setEnabled(enable);
            dataButton.setEnabled(enable);
            popField.setEnabled(enable);
            browseButton.setEnabled(enable);
            fileLabel.setEnabled(enable);
            setOpaque(enable);
            tabbedPane.setEnabledAt(0, enable);
        }
        
        protected void updatePopField(String file) {
            this.popField.setText(file);
        }
        
        protected String getPopFile() {
            return popField.getText();
        }        
    }
    


    /** Contains annotation parameter controls.
     */
    private class EaseParameterPanel extends JPanel {
        
        JTextField converterFileField;
        JList fileList;
        JButton browserButton;
        JTextField minClusterSizeField;
        JComboBox fieldNamesBox;
        
        JList annFileList;
        Vector annVector;
        JButton removeButton;
        JCheckBox useAnnBox;
        JLabel fileLabel;
        
        /** Constructs a new EaseParameterPanel
         * @param fieldNames annotation types
         */
        public EaseParameterPanel(String [] fieldNames) {
            //Conversion File Panel
            JPanel convPanel = new JPanel(new GridBagLayout());
            convPanel.setBackground(Color.white);
            convPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(
            		EtchedBorder.LOWERED), "Annotation Conversion File", TitledBorder.DEFAULT_JUSTIFICATION, 
					TitledBorder.DEFAULT_POSITION, font, Color.black));
            useAnnBox = new JCheckBox("use annotation converter", false);
            useAnnBox.setActionCommand("use-converter-command");
            useAnnBox.addActionListener(listener);
            useAnnBox.setBackground(Color.white);
            useAnnBox.setFocusPainted(false);
            useAnnBox.setEnabled(!useLoadedAnnotationFile);
            
            
            converterFileField = new JTextField(30);
            converterFileField.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED, 
            		Color.lightGray, Color.gray));
            converterFileField.setEnabled(useAnnBox.isEnabled());
            converterFileField.setBackground(Color.lightGray);
            
            browserButton = new JButton("File Browser");
            browserButton.setActionCommand("converter-file-browser-command");
            browserButton.setFocusPainted(false);
            browserButton.setPreferredSize(new Dimension(150, 25));
            browserButton.setSize(150, 25);
            browserButton.addActionListener(listener);
            browserButton.setEnabled(useAnnBox.isEnabled());
            
            fileLabel = new JLabel("File :");
            fileLabel.setEnabled(false);
            convPanel.add(useAnnBox, new GridBagConstraints(0,0,3,1,0.0,0.0,GridBagConstraints.WEST,
            		GridBagConstraints.BOTH,new Insets(0,15,15,0),0,0));
            convPanel.add(fileLabel, new GridBagConstraints(0,1,1,1,0.0,0.0,GridBagConstraints.CENTER,
            		GridBagConstraints.BOTH,new Insets(0,15,15,0),0,0));
            convPanel.add(this.browserButton, new GridBagConstraints(0,2,3,1,0.0,0.0,GridBagConstraints.WEST,
            		GridBagConstraints.VERTICAL,new Insets(0,15,0,0),0,0));
            convPanel.add(this.converterFileField, new GridBagConstraints(1,1,2,1,0.0,0.0,
            		GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,15,15,0),0,0));
            
            //Annotation file panel
            JPanel annPanel = new JPanel(new GridBagLayout());
            annPanel.setBackground(Color.white);
            annPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(
            		EtchedBorder.LOWERED), "Gene Annotation / Gene Ontology Linking Files", 
					TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, font, Color.black));
            
            //annVector = new Vector();
            annFileList = new JList(new DefaultListModel());
            annFileList.setCellRenderer(new ListRenderer());
            annFileList.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
            JScrollPane annPane = new JScrollPane(annFileList);
            
            JButton  annButton = new JButton("Add Files");
            annButton.setActionCommand("ann-file-browser-command");
            annButton.addActionListener(listener);
            annButton.setFocusPainted(false);
            annButton.setPreferredSize(new Dimension(150, 25));
            annButton.setSize(150, 25);
            
            removeButton = new JButton("Remove Selected");
            removeButton.setActionCommand("remove-ann-file-command");
            removeButton.addActionListener(listener);
            removeButton.setFocusPainted(false);
            removeButton.setPreferredSize(new Dimension(150, 25));
            removeButton.setSize(150, 25);
            removeButton.setEnabled(false);
            
            JPanel fillPanel = new JPanel();
            fillPanel.setBackground(Color.white);
            annPanel.add(fillPanel, new GridBagConstraints(0,0,1,1,0.0,0.0, GridBagConstraints.CENTER, 
            		GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));
            annPanel.add(annButton, new GridBagConstraints(1,0,1,1,0.0,0.0, GridBagConstraints.EAST, 
            		GridBagConstraints.VERTICAL, new Insets(0,0,10,5), 0,0));
            annPanel.add(removeButton, new GridBagConstraints(2,0,1,1,0.0,0.0, GridBagConstraints.CENTER, 
            		GridBagConstraints.VERTICAL, new Insets(0,5,10,0), 0,0));
            annPanel.add(new JLabel("Files: "), new GridBagConstraints(0,1,1,1,0.0,0.0, 
            		GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(0,0,0,0), 0,0));
            annPanel.add(annPane, new GridBagConstraints(1,1,2,1,0.0,1.0, GridBagConstraints.CENTER, 
            		GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));
            
            sep = System.getProperty("file.separator");
            File file = new File(getBaseFileLocation());
            String tempPath = file.getPath();
            Vector<String> fileVector = new Vector<String>();
            fileList = new JList(fileVector);
            if(file.exists()){
                String [] listFileNames = file.list();
                for(int i = 0; i < listFileNames.length; i++){
                    File tempFile = new File(tempPath+sep+listFileNames[i]);
                    if(tempFile.isFile()) {
                        fileVector.add(listFileNames[i]);
                	}
                }
                if(fileVector.size() > 0){
                    converterFileField.setText(tempPath+sep+((String)fileVector.elementAt(0)));
                }
            }
            

          	converterFileField.setText("");
            
            this.fieldNamesBox = new JComboBox(fieldNames);
            this.fieldNamesBox.setEditable(false);
            this.fieldNamesBox.setEnabled(!useLoadedAnnotationFile);
            this.fieldNamesBox.setSelectedItem(ANNOTATION_LINK);
            
            minClusterSizeField = new JTextField(5);
            minClusterSizeField.setText("5");
            
            JPanel contentPanel = new JPanel(new GridBagLayout());
            
            JPanel easeFilePanel = new JPanel(new GridBagLayout());
            
            this.setLayout(new GridBagLayout());
            
            JPanel annotKeyPanel = new JPanel(new GridBagLayout());
            annotKeyPanel.setBackground(Color.white);
            annotKeyPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(
            		EtchedBorder.LOWERED), "MeV Annotation Key  (\"Unique ID\")", 
					TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, font, Color.black));
            
            annotKeyPanel.add(new JLabel("Annotation Key:  "), new GridBagConstraints(0,0,1,1,0.0,0.0,
            		GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0));
            annotKeyPanel.add(this.fieldNamesBox, new GridBagConstraints(1,0,1,1,0.0,0.0,
            		GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0));
            
            this.add(annotKeyPanel, new GridBagConstraints(0,0,1,1,1.0,0.0,GridBagConstraints.CENTER,
            		GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0));
            this.add(convPanel, new GridBagConstraints(0,1,1,1,1.0,0.0,GridBagConstraints.CENTER,
            		GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0));
            this.add(annPanel, new GridBagConstraints(0,2,1,1,1.0,1.0,GridBagConstraints.CENTER,
            		GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0));
        }
        
        private void updateFileDirectoryField(){
            
            File file = new File((String)this.fileList.getSelectedValue());
            if(file == null)
                return;
            
            String tempPath = file.getParent();
            int fileIndex = this.fileList.getSelectedIndex();
            String fileName = (String)(this.fileList.getModel().getElementAt(this.fileList.getSelectedIndex()));
            this.converterFileField.setText(tempPath+sep+fileName);
        }
        
        private void setEnableControls(boolean enable) {
        	setOpaque(enable);
        	tabbedPane.setEnabledAt(2, enable);
        	converterFileField.setEnabled(enable);
        	fileList.setEnabled(enable);
        	browserButton.setEnabled(enable);
        	minClusterSizeField.setEnabled(enable);
        	fieldNamesBox.setEnabled(enable);
        	annFileList.setEnabled(enable);
        	removeButton.setEnabled(enable);
        	useAnnBox.setEnabled(enable);
        	fileLabel.setEnabled(enable);
        }
        
        protected void updateAnnFileList(File [] files){
            File file;
            for(int i = 0; i < files.length; i++){
                file = files[i];
                if(!((DefaultListModel) annFileList.getModel()).contains(file)){
                    ((DefaultListModel) annFileList.getModel()).addElement(file);
                }
            }
            annFileList.validate();
        }
        
        /** Returns the converter file name (or null if none)
         */
        public String getConverterFileName(){
            if(this.useAnnBox.isSelected())
                return converterFileField.getText();
            return null;
        }
        
        /** Returns the annotation type string.
         */
        public String getAnnotationKeyType(){
            return (String)this.fieldNamesBox.getSelectedItem();
        }
        
        protected class EaseListListener implements ListSelectionListener {
            public void valueChanged(ListSelectionEvent listSelectionEvent) {
                updateFileDirectoryField();
            }
        }
        
        protected void updateConverterFileField(String field){
            this.converterFileField.setText(field);
        }
        
        /** Returns the list of annotation-theme mapping files.
         */
        public String [] getAnnToGOFileList(){
            String [] fileNames = new String[((DefaultListModel) annFileList.getModel()).size()];
            for(int i = 0; i < fileNames.length; i++){
                fileNames[i] = ((File)(((DefaultListModel)annFileList.getModel()).elementAt(i))).getPath();
            }
            return fileNames;
        }
        
        public void removeSelectedFiles(){
            int [] indices = annFileList.getSelectedIndices();
            
            for(int i = 0; i < indices.length; i++){
                // annFileList.remove(indices[i]);
                ((DefaultListModel)annFileList.getModel()).removeElementAt(indices[i]);
            }
            if(annFileList.getModel().getSize() < 1){
                this.removeButton.setEnabled(false);
                okButton.setEnabled(false);
            }
            annFileList.validate();
        }
        
        protected class ListRenderer extends DefaultListCellRenderer {
            public Component getListCellRendererComponent(JList list, Object value, int index, 
            		boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                File file = (File) value;
                setText(file.getName());
                return this;
            }
        }
    }
    
    private class HCLSelectionPanel extends JPanel {
    	 private JRadioButton ALC;
    	 private JRadioButton CLC;
    	 private JRadioButton SLC;
    	 private JTextField maxField;
    	 private JTextField minField; 
    	 private JCheckBox genes_box;
    	 private JCheckBox sample_box;
    	 private DistanceMetricPanel metricPanel;
    	 private JPanel treeSelectionPanel;
    	 /**
         * Constructs the dialog.
         */
        public HCLSelectionPanel(String globalMetricName, boolean globalAbsoluteDistance, 
        		boolean showDistancePanel) {
            super(new GridBagLayout());
            setBackground(Color.white);
            
            metricPanel = new DistanceMetricPanel(globalMetricName, globalAbsoluteDistance, 
            		"Euclidean Distance", "HCL", true, true);
            metricPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(
            		EtchedBorder.LOWERED), "Distance Metric Selection", TitledBorder.DEFAULT_JUSTIFICATION, 
					TitledBorder.DEFAULT_POSITION, font, Color.black));
            
            //create and set up cluster size specification panel
            JPanel numberPanel = new JPanel(new GridBagLayout());
            numberPanel.setBackground(Color.white);
            numberPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(
            		EtchedBorder.LOWERED), "Cluster size Specification", TitledBorder.DEFAULT_JUSTIFICATION, 
					TitledBorder.DEFAULT_POSITION, font, Color.black));
            
            this.maxField = new JTextField("100", 6);
            this.maxField.setBackground(Color.white);
            
            this.minField = new JTextField("10", 6);
            this.minField.setBackground(Color.white);
            
            JLabel maxLabel = new JLabel("Max");
            maxLabel.setBackground(Color.white);
            
            JLabel minLabel = new JLabel("Min");
            minLabel.setBackground(Color.white);
            
            numberPanel.add(maxLabel, new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.CENTER,
            		GridBagConstraints.BOTH, new Insets(5,0,5,0),0,0));
            numberPanel.add(maxField, new GridBagConstraints(1,0,1,1,0,0,GridBagConstraints.CENTER,
            		GridBagConstraints.BOTH, new Insets(0,0,5,0),0,0));
            numberPanel.add(minLabel, new GridBagConstraints(0,1,1,1,0,0,GridBagConstraints.CENTER,
            		GridBagConstraints.BOTH, new Insets(0,0,5,0),0,0));
            numberPanel.add(minField, new GridBagConstraints(1,1,1,1,0,0,GridBagConstraints.CENTER,
            		GridBagConstraints.BOTH, new Insets(0,0,5,0),0,0));
            
            //create and set up cluster size specification panel
            treeSelectionPanel = new JPanel(new GridBagLayout());
            treeSelectionPanel.setBackground(Color.white);
            treeSelectionPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(
            		EtchedBorder.LOWERED), "Tree Selection", TitledBorder.DEFAULT_JUSTIFICATION, 
					TitledBorder.DEFAULT_POSITION, font, Color.black));
            genes_box = new JCheckBox("Gene Tree");
            genes_box.setSelected(true);
            genes_box.setFocusPainted(false);
            genes_box.setBackground(Color.white);
            genes_box.setForeground(UIManager.getColor("Label.foreground"));
            genes_box.addItemListener(listener);
            
            sample_box = new JCheckBox("Sample Tree");
            sample_box.setSelected(false);
            sample_box.setFocusPainted(false);
            sample_box.setBackground(Color.white);
            sample_box.setForeground(UIManager.getColor("Label.foreground"));
            sample_box.addItemListener(listener);
            
            treeSelectionPanel.add(genes_box, new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.CENTER,
            		GridBagConstraints.BOTH, new Insets(5,0,5,20), 0,0));
            treeSelectionPanel.add(sample_box, new GridBagConstraints(1,0,1,1,0,0,GridBagConstraints.CENTER, 
            		GridBagConstraints.BOTH, new Insets(5,20,5,0), 0,0));
            
            //create and set up parameter panel
            ParameterPanel linkageMethodPanel = new ParameterPanel("Linkage Method Selection");
            linkageMethodPanel.setLayout(new GridBagLayout());
            linkageMethodPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(
            		EtchedBorder.LOWERED), "Linkage Method Selection", TitledBorder.DEFAULT_JUSTIFICATION, 
					TitledBorder.DEFAULT_POSITION, font, Color.black));
            
            ALC = new JRadioButton("Average linkage clustering");
            ALC.setBackground(Color.white);
            ALC.setFocusPainted(false);
            ALC.setForeground(UIManager.getColor("Label.foreground"));
            ALC.setMnemonic(KeyEvent.VK_A);
            ALC.setSelected(true);
            
            CLC = new JRadioButton("Complete linkage clustering");
            CLC.setBackground(Color.white);
            CLC.setFocusPainted(false);
            CLC.setForeground(UIManager.getColor("Label.foreground"));
            CLC.setMnemonic(KeyEvent.VK_C);
            
            SLC = new JRadioButton("Single linkage clustering");
            SLC.setBackground(Color.white);
            SLC.setFocusPainted(false);
            SLC.setForeground(UIManager.getColor("Label.foreground"));
            SLC.setMnemonic(KeyEvent.VK_S);
            
            linkageMethodPanel.add(ALC, new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.CENTER, 
            		GridBagConstraints.BOTH, new Insets(5,0,5,0), 0,0));
            linkageMethodPanel.add(CLC, new GridBagConstraints(0,1,1,1,0,0,GridBagConstraints.CENTER, 
            		GridBagConstraints.BOTH, new Insets(0,0,5,0), 0,0));
            linkageMethodPanel.add(SLC, new GridBagConstraints(0,2,1,1,0,0,GridBagConstraints.CENTER, 
            		GridBagConstraints.BOTH, new Insets(0,0,5,0), 0,0));
            
            // Group the radio buttons.
            ButtonGroup group = new ButtonGroup();
            group.add(ALC);
            group.add(CLC);
            group.add(SLC);
            
            if(showDistancePanel) {
                this.add(treeSelectionPanel, new GridBagConstraints(0,0,1,1,1,0,GridBagConstraints.CENTER, 
                		GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));
                this.add(metricPanel, new GridBagConstraints(0,1,1,1,1,0,GridBagConstraints.CENTER, 
                		GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));
                this.add(linkageMethodPanel, new GridBagConstraints(0,2,1,1,1,0,GridBagConstraints.CENTER, 
                		GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));
                this.add(numberPanel, new GridBagConstraints(0,3,1,1,1,0,GridBagConstraints.CENTER, 
                		GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));
            } else {
                this.add(treeSelectionPanel, new GridBagConstraints(0,0,1,1,1,0,GridBagConstraints.CENTER, 
                		GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));
                this.add(linkageMethodPanel, new GridBagConstraints(0,1,1,1,1,0,GridBagConstraints.CENTER, 
                		GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));
                this.add(numberPanel, new GridBagConstraints(0,2,1,1,1,0,GridBagConstraints.CENTER, 
                		GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));
            }
            setActionListeners(listener); 
        }
        
        public void setTreeSelectionControls(boolean show) {
        	treeSelectionPanel.setEnabled(show);
        	genes_box.setEnabled(show);
        	sample_box.setEnabled(show);
        }
        
    }
    
    /** Contains statistical parameter controls.
     */
    private class AlphaPanel extends JPanel{
        
        //Stats
        private JCheckBox fisherBox;
        private JCheckBox easeBox;
        //mult. corrections
        private JCheckBox bonferroniBox;
        private JCheckBox sidakBox;
        private JCheckBox bonferroniStepBox;
        private JCheckBox permBox;
        private JTextField permField;
        private JLabel permLabel;
        //Trim params
        private JCheckBox trimBox;
        private JCheckBox trimNBox;
        private JLabel trimNLabel;
        private JTextField trimNField;
        private JCheckBox trimPercentBox;
        private JLabel trimPercentLabel;
        private JTextField trimPercentField;
        
        
        /** Constucts a new AlphaPanel.
         */
        public AlphaPanel(){
            super(new GridBagLayout());
            setBackground(Color.white);
            
            //STAT PANEL
            JPanel statPanel = new JPanel(new GridBagLayout());
            statPanel.setBackground(Color.white);
            statPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(
            		EtchedBorder.LOWERED), "Reported Statistic", TitledBorder.DEFAULT_JUSTIFICATION, 
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
            
            statPanel.add(fisherBox, new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.CENTER,
            		GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
            statPanel.add(easeBox, new GridBagConstraints(0,1,1,1,0,0,GridBagConstraints.CENTER,
            		GridBagConstraints.BOTH, new Insets(0,0,10,0),0,0));
            
            
            //P-value Correction Panel
            JPanel correctionPanel = new JPanel(new GridBagLayout());
            correctionPanel.setBackground(Color.white);
            correctionPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(
            		EtchedBorder.LOWERED), "Multiplicity Corrections", TitledBorder.DEFAULT_JUSTIFICATION, 
					TitledBorder.DEFAULT_POSITION, font, Color.black));
            
            bonferroniBox = new JCheckBox("Bonferroni Correction", false);
            bonferroniBox.setBackground(Color.white);
            bonferroniBox.setFocusPainted(false);
            
            bonferroniStepBox = new JCheckBox("Bonferroni Step Down Correction", false);
            bonferroniStepBox.setBackground(Color.white);
            bonferroniStepBox.setFocusPainted(false);
            
            sidakBox = new JCheckBox("Sidak Method", false);
            sidakBox.setBackground(Color.white);
            sidakBox.setFocusPainted(false);
            
            permBox = new JCheckBox("Resampling Probability Analysis", false);
            permBox.setActionCommand("permutation-analysis-command");
            permBox.setBackground(Color.white);
            permBox.setFocusPainted(false);
            permBox.addActionListener(listener);
            //permBox.setEnabled(false);
            
            permField = new JTextField("1000", 10);
            permField.setBackground(Color.white);
            permField.setEnabled(false);
            
            permLabel = new JLabel("Number of Permutations");
            permLabel.setBackground(Color.white);
            permLabel.setEnabled(false);
            
            correctionPanel.add(bonferroniBox, new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.WEST,
            		GridBagConstraints.BOTH, new Insets(10,0,0,0),0,0));
            correctionPanel.add(sidakBox, new GridBagConstraints(1,0,1,1,0,0,GridBagConstraints.CENTER,
            		GridBagConstraints.BOTH, new Insets(10,0,0,0),0,0));
            correctionPanel.add(bonferroniStepBox, new GridBagConstraints(0,1,2,1,0,0,GridBagConstraints.WEST,
            		GridBagConstraints.BOTH, new Insets(0,0,10,0),0,0));
            correctionPanel.add(permBox, new GridBagConstraints(0,2,2,1,0,0,GridBagConstraints.WEST,
            		GridBagConstraints.BOTH, new Insets(0,0,5,0),0,0));
            correctionPanel.add(permLabel, new GridBagConstraints(0,3,1,1,0,0,GridBagConstraints.CENTER,
            		GridBagConstraints.BOTH, new Insets(0,0,15,0),0,0));
            correctionPanel.add(permField, new GridBagConstraints(1,3,1,1,0,0,GridBagConstraints.CENTER,
            		GridBagConstraints.VERTICAL, new Insets(0,0,15,0),0,0));
            
            //Trim Panel
            JPanel trimPanel = new JPanel(new GridBagLayout());
            trimPanel.setBackground(Color.white);
            trimPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(
            		EtchedBorder.LOWERED), "Trim Parameters", TitledBorder.DEFAULT_JUSTIFICATION, 
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
            
            trimPanel.add(trimBox, new GridBagConstraints(0,0,3,1,0,0,GridBagConstraints.CENTER,
            		GridBagConstraints.BOTH, new Insets(0,0,20,0),0,0));
            
            trimPanel.add(trimNBox, new GridBagConstraints(0,1,1,1,0,0,GridBagConstraints.CENTER,
            		GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
            trimPanel.add(trimNLabel, new GridBagConstraints(1,1,1,1,0,0,GridBagConstraints.EAST,
            		GridBagConstraints.BOTH, new Insets(0,20,0,15),0,0));
            trimPanel.add(trimNField, new GridBagConstraints(2,1,1,1,0,0,GridBagConstraints.CENTER,
            		GridBagConstraints.NONE, new Insets(0,0,0,0),0,0));
            
            trimPanel.add(trimPercentBox, new GridBagConstraints(0,2,1,1,0,0,GridBagConstraints.CENTER,
            		GridBagConstraints.BOTH, new Insets(10,0,0,0),0,0));
            trimPanel.add(trimPercentLabel, new GridBagConstraints(1,2,1,1,0,0,GridBagConstraints.EAST,
            		GridBagConstraints.BOTH, new Insets(10,20,0,15),0,0));
            trimPanel.add(trimPercentField, new GridBagConstraints(2,2,1,1,0,0,GridBagConstraints.CENTER,
            		GridBagConstraints.NONE, new Insets(10,0,0,0),0,0));
            
            //Add panels to main panel
            add(statPanel, new GridBagConstraints(0,0,1,1,1.0,0,GridBagConstraints.CENTER,
            		GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
            add(correctionPanel, new GridBagConstraints(0,1,1,1,1.0,0,GridBagConstraints.CENTER,
            		GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
            add(trimPanel, new GridBagConstraints(0,2,1,1,1.0,1.0,GridBagConstraints.CENTER,
            		GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
        }
        
        
        private void setEnableControls(boolean enable) {
        	setOpaque(enable);
        	tabbedPane.setEnabledAt(3,enable);
        }
        /** Indicates if permutations are selected.
         */
        public boolean performPermutations(){
            return permBox.isSelected();
        }
        
        public void setEnablePermutations(){
            permLabel.setEnabled(permBox.isSelected());
            permField.setEnabled(permBox.isSelected());
        }
        
        public void validateTrimOptions(){
            if(this.trimBox.isSelected()){
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
    }

    /**
	 * The topmost panel in the InitDialog, containing the location
	 * information for the EASE filesystem and the update controls.
	 */
    public class ConfigPanel extends ParameterPanel {
		private static final long serialVersionUID = -5298900627241870503L;

		JComboBox organismListBox;
		JComboBox arrayListBox;
		JLabel chooseOrg, chooseArray, browseLabel, statusLabel;
		JButton getEaseSupportFileButton;
		JButton browseSupportFileButton;
		JTextField supportFileLocationField;

		public ConfigPanel() {
			super("File Updates and Configuration");
			setLayout(new GridBagLayout());
				

			getEaseSupportFileButton = new JButton("Download");
			getEaseSupportFileButton.setActionCommand("download-support-file-command");
			getEaseSupportFileButton.addActionListener(listener);
			getEaseSupportFileButton.setToolTipText("<html>Downloads EASE annotation files<br>for a selected species and array type.</html>");

			browseSupportFileButton = new JButton("Browse");
			browseSupportFileButton.setActionCommand("select-file-base-command");
			browseSupportFileButton.addActionListener(listener);

			supportFileLocationField = new JTextField(getDefaultBaseFileLocation().getAbsolutePath(), 25);
			supportFileLocationField.setEditable(true);

			chooseOrg = new JLabel("Organism");
			chooseArray = new JLabel("Array Platform");
			browseLabel = new JLabel("or Browse for another Ease data file system:");
			statusLabel = new JLabel("Click to download");

			if(speciestoarrays == null || speciestoarrays.size() == 0) {
				organismListBox = new JComboBox();
				organismListBox.addItem("No organisms listed");
				organismListBox.setEnabled(false);
				
				arrayListBox = new JComboBox();
				arrayListBox.addItem("No species listed");
				arrayListBox.setEnabled(false);
			} else {
				
				organismListBox = new JComboBox(new Vector<String>(speciestoarrays.keySet()));
	
				try {
					organismListBox.setSelectedItem(speciesName);
				} catch (NullPointerException npe) {/* Leave as default */}
				arrayListBox = new JComboBox(speciestoarrays.get(organismListBox.getSelectedItem()));
				
				try {
					arrayListBox.setSelectedItem(arrayName);
				} catch (NullPointerException npe) {/* Leave as default */}
				
				arrayListBox.setEnabled(true); 

			}

			arrayListBox.addActionListener(listener);
			arrayListBox.setActionCommand("array-selected-command");
			organismListBox.addActionListener(listener);
			organismListBox.setActionCommand("organism-selected-command");
	
			add(chooseOrg, 				new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.WEST, 	GridBagConstraints.BOTH, new Insets(5, 30, 0, 0), 0, 0));
			add(chooseArray, 				new GridBagConstraints(0, 1, 1, 1, 0, 0, GridBagConstraints.WEST, 	GridBagConstraints.BOTH, new Insets(5, 30, 0, 0), 0, 0));
			add(organismListBox, 			new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, 	GridBagConstraints.BOTH, new Insets(5, 30, 0, 0), 0, 0));
			add(arrayListBox, 				new GridBagConstraints(1, 1, 1, 1, 0, 0, GridBagConstraints.CENTER, 	GridBagConstraints.BOTH, new Insets(5, 30, 0, 0), 0, 0));
			add(statusLabel, 				new GridBagConstraints(4, 0, 1, 1, 0, 0, GridBagConstraints.EAST, 	GridBagConstraints.BOTH, new Insets(5, 25, 0, 20),0, 0));
			add(getEaseSupportFileButton, 	new GridBagConstraints(4, 1, 1, 1, 0, 0, GridBagConstraints.EAST, 	GridBagConstraints.BOTH, new Insets(5, 25, 0, 20), 0, 0));
			add(browseLabel, 				new GridBagConstraints(0, 2, 2, 1, 0, 0, GridBagConstraints.WEST, 	GridBagConstraints.BOTH, new Insets(10, 30, 0, 0),0, 0));
			add(supportFileLocationField, 	new GridBagConstraints(0, 3, 2, 1, 1, 0, GridBagConstraints.WEST, 	GridBagConstraints.BOTH, new Insets(10, 30, 5, 0), 0, 0));
			add(browseSupportFileButton, 	new GridBagConstraints(4, 3, 1, 1, 0, 0, GridBagConstraints.EAST, 	GridBagConstraints.BOTH, new Insets(5, 25, 5, 20), 0, 0));

			try {
				boolean b = resourceManager.fileIsInRepository(new EASESupportDataFile(organismListBox.getSelectedItem().toString(), arrayListBox.getSelectedItem().toString()));
				if(b) {
					getEaseSupportFileButton.setText("Select This");
				} else {
					getEaseSupportFileButton.setText("Download");
				}
			} catch (NullPointerException npe) {
				getEaseSupportFileButton.setText("Download");
			}
			updateSelection();
		}

		private void onDownloadSupportFile() {
			try {
				File f = resourceManager.getSupportFile(new EASESupportDataFile(organismListBox.getSelectedItem().toString(), arrayListBox.getSelectedItem().toString()), true);
				supportFileLocationField.setText(f.getAbsolutePath());
				getEaseSupportFileButton.setText("Select This");
				statusLabel.setText("Selected");
				getEaseSupportFileButton.setEnabled(false);
			} catch (SupportFileAccessError sfae) {
				statusLabel.setText("Failure");
				sfae.printStackTrace();
			} catch (NullPointerException npe) {
				statusLabel.setText("Failure");
			}
		}
		
		public void browseForSupportFiles() {
			String startDir = TMEV.getSettingForOption(EASEGUI.LAST_EASE_FILE_LOCATION);
			if (startDir == null)
				startDir = supportFileLocationField.getText();
			File file = new File(startDir);
			if (!file.exists()) {
				file = TMEV.getFile("data/ease");
				if (file == null) {
					file = new File(System.getProperty("user.dir"));
				}
				TMEV.storeProperty(EASEGUI.LAST_EASE_FILE_LOCATION, file.toString());
			}
			JFileChooser chooser = new JFileChooser(file);
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			if (chooser.showOpenDialog(TEASEInitDialog.this) == JOptionPane.OK_OPTION) {
				supportFileLocationField.setText(chooser.getSelectedFile().getAbsolutePath());
				TMEV.storeProperty(EASEGUI.LAST_EASE_FILE_LOCATION, supportFileLocationField.getText());
			}
		}
		public String getBaseFileLocation() {
			return supportFileLocationField.getText();
		}
		public void selectSpecies() {
			arrayListBox.removeAllItems();
			Vector<String> arraysForThisSpecies = speciestoarrays.get(organismListBox.getSelectedItem());
			for (int i = 0; i < arraysForThisSpecies.size(); i++) {
				arrayListBox.addItem(arraysForThisSpecies.elementAt(i));
			}
		}
		public void updateSelection() {
			if(arrayListBox.getSelectedItem() == null) {
				return;
			}
			String selectedOrganism = organismListBox.getSelectedItem().toString();
			String selectedArray = arrayListBox.getSelectedItem().toString();
			if(selectedOrganism != null && selectedArray != null) {
				if(resourceManager.fileIsInRepository(new EASESupportDataFile(selectedOrganism, selectedArray))) {
					statusLabel.setText("Click to Select");
					getEaseSupportFileButton.setText("Select");
				} else {
					statusLabel.setText("Click to Download");
					getEaseSupportFileButton.setText("Download");
				}
				getEaseSupportFileButton.setEnabled(true);
			        try {
			        	ResourcererAnnotationFileDefinition def = new ResourcererAnnotationFileDefinition(speciesName, arrayName);
			        	annotationFile = resourceManager.getSupportFile(def, false);
			        } catch (SupportFileAccessError sfae) {
			        	//disable population from file button
			        	useLoadedAnnotationFile = false;
				        popPanel.fileButton.setSelected(true);
				        popPanel.preloadedAnnotationButton.setSelected(false);
				        popPanel.preloadedAnnotationButton.setEnabled(false);
			        }
			} else {
				getEaseSupportFileButton.setEnabled(false);
			}
		}

	}

    
    private class ColorBoundaryPanel extends ParameterPanel {
    	private JTextField upperField;
    	private JTextField lowerField;
    	private JLabel upperLabel;
    	private JLabel lowerLabel;
    	
    	public ColorBoundaryPanel() {
    		super("Assign Color Gradient");
    		setLayout(new GridBagLayout());
    		
    		this.upperLabel = new JLabel("Upper bound score (Blue -> not significant)");
    		this.lowerLabel = new JLabel("Lower bound score (Red -> significant)");
    		this.upperField = new JTextField("1.0E-1", 8);
    		this.lowerField = new JTextField("1.0E-5", 8);
    		
            add(this.upperLabel, new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.CENTER,
            		GridBagConstraints.BOTH, new Insets(5,0,5,0),0,0));
            add(this.upperField, new GridBagConstraints(1,0,1,1,0,0,GridBagConstraints.CENTER,
            		GridBagConstraints.BOTH, new Insets(0,0,5,0),0,0));
            add(this.lowerLabel, new GridBagConstraints(0,1,1,1,0,0,GridBagConstraints.CENTER,
            		GridBagConstraints.BOTH, new Insets(0,0,5,0),0,0));
            add(this.lowerField, new GridBagConstraints(1,1,1,1,0,0,GridBagConstraints.CENTER,
            		GridBagConstraints.BOTH, new Insets(0,0,5,0),0,0));
    	}
    	
    	public void setEnableControls(boolean enable) {
    		this.upperLabel.setEnabled(enable);
    		this.lowerLabel.setEnabled(enable);
    		this.upperField.setEnabled(enable);
    		this.lowerField.setEnabled(enable);
    		setOpaque(enable);
    	}
    }
    
    /**
     * The class to listen to the dialog and check boxes items events.
     */
    private class EventListener extends DialogListener implements ItemListener {
        
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            if (command.equals("use-converter-command")) {
                if(easeParamPanel.useAnnBox.isSelected()){
                    easeParamPanel.browserButton.setEnabled(true);
                    easeParamPanel.converterFileField.setEnabled(true);
                    easeParamPanel.converterFileField.setBackground(Color.white);
                    easeParamPanel.fileLabel.setEnabled(true);
                } else {
                    easeParamPanel.browserButton.setEnabled(false);
                    easeParamPanel.converterFileField.setEnabled(false);
                    easeParamPanel.converterFileField.setBackground(Color.lightGray);
                    easeParamPanel.fileLabel.setEnabled(false);
                }
            } else if (command.equals("converter-file-browser-command")){
                File convertFile = new File(getBaseFileLocation() + sep + "Data" + sep + "Convert");
                JFileChooser chooser = new JFileChooser(convertFile);
                chooser.setDialogTitle("Annotation Converter Selection");
                chooser.setMultiSelectionEnabled(false);
                if(chooser.showOpenDialog(parent) == JOptionPane.OK_OPTION){
                    easeParamPanel.updateConverterFileField(chooser.getSelectedFile().getPath());
                    if(TMEV.getSettingForOption(TEASEGUI.LAST_TEASE_FILE_LOCATION) == null)
                    	TMEV.storeProperty(TEASEGUI.LAST_TEASE_FILE_LOCATION, chooser.getSelectedFile().getParentFile().getParentFile().getParent());
                }
                return;
            } else if (command.equals("ann-file-browser-command")){
                File classFile = new File(getBaseFileLocation()+ sep + "Data" + sep + "Class" + sep);
                if(!classFile.canRead())
                 	classFile = new File("." + sep + "data" + sep + "ease");
                JFileChooser chooser = new JFileChooser(classFile);
                chooser.setDialogTitle("Annotation --> GO Term, File(s) Selection");
                chooser.setMultiSelectionEnabled(true);
                if(chooser.showOpenDialog(parent) == JOptionPane.OK_OPTION){
                    easeParamPanel.updateAnnFileList(chooser.getSelectedFiles());
                    easeParamPanel.removeButton.setEnabled(true);
                    if(TMEV.getSettingForOption(TEASEGUI.LAST_TEASE_FILE_LOCATION) == null)
                    	TMEV.storeProperty(TEASEGUI.LAST_TEASE_FILE_LOCATION, chooser.getSelectedFiles()[0].getParentFile().getParentFile().getParent());
                    okButton.setEnabled(true);
                }
            } else if (command.equals("remove-ann-file-command")){
                easeParamPanel.removeSelectedFiles();
            } else if (command.equals("permutation-analysis-command")){
                alphaPanel.setEnablePermutations();
            } else if (command.equals("trim-result-command")){
                alphaPanel.validateTrimOptions();
            } else if (command.equals("select-file-base-command")) {
                configPanel.browseForSupportFiles();
            } else if (command.equals("ok-command")) {
                result = JOptionPane.OK_OPTION;
                if(isClusterModeSelected() && popPanel.fileButton.isSelected()) {
                    String fileName = popPanel.popField.getText();
                    if(fileName == null || fileName.equals("") || fileName.equals(" ")) {
                        JOptionPane.showMessageDialog(parent, "You have selected to use a population file but have not "+
                        "entered a file name.  \nPlease enter a file or use the file browser to select a file.", "EASE Initialization: Missing Parameter", JOptionPane.WARNING_MESSAGE);
                        tabbedPane.setSelectedIndex(0);
                        popPanel.popField.grabFocus();
                        popPanel.popField.selectAll();
                        popPanel.popField.setCaretPosition(0);
                        return;
                    }
                }
                
                if(getAnnToGOFileList().length == 0  && !modePanel.hclOnlyButton.isSelected()) {
                    JOptionPane.showMessageDialog(parent, "You have not selected any gene annotation/gene ontology linking files. \n"+
                    "Please enter files or use the browser to select files.", "EASE Initialization: Missing Parameter", JOptionPane.WARNING_MESSAGE);
                    tabbedPane.setSelectedIndex(1);
                    easeParamPanel.browserButton.grabFocus();
                    return;
                }
                
                if(easeParamPanel.useAnnBox.isSelected()) {
                    String fileName = easeParamPanel.getConverterFileName();
                    if( fileName == null || fileName.equals("") || fileName.equals(" ") ) {
                        JOptionPane.showMessageDialog(parent, "You have selected to use an annotation conversion file but have not made a file selection.\n" +
                        "Please enter a file name or browse to select a file.", "EASE Initialization: Missing Parameter", JOptionPane.WARNING_MESSAGE);
                        tabbedPane.setSelectedIndex(1);
                        easeParamPanel.browserButton.grabFocus();
                        return;
                    }
                }                
                dispose();
            } else if (command.equals("cancel-command")) {
                result = JOptionPane.CANCEL_OPTION;
                dispose();
            } else if (command.equals("reset-command")){
                resetControls();
                result = JOptionPane.CANCEL_OPTION;
                return;
            } else if (command.equals("info-command")){
                HelpWindow hw = new HelpWindow(TEASEInitDialog.this, "EASE Initialization Dialog");
                result = JOptionPane.CANCEL_OPTION;
                if(hw.getWindowContent()){
                    hw.setSize(600,600);
                    hw.setLocation();
                    hw.show();
                }
                else {
                    hw.setVisible(false);
                    hw.dispose();
                }
            }
        }
        
        public void itemStateChanged(ItemEvent e) {
            //okButton.setEnabled(genes_box.isSelected() || cluster_box.isSelected());
        }
        
        public void windowClosing(WindowEvent e) {
            result = JOptionPane.CLOSED_OPTION;
            dispose();
        }
    }
    
    public static void main(String [] args) {
        String [] labels = new String [3];
        labels[0] = "TC#";
        labels[1] = "GB#";
        labels[2] = "Role";

        EASEInitDialog eid = new EASEInitDialog(new JFrame(), labels);
        eid.showModal();
    }
}
