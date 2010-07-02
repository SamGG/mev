/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: OneWayANOVAInitBox.java,v $
 * $Revision: 1.10 $
 * $Date: 2006-11-07 17:27:40 $
 * $Author: dschlauch $
 * $State: Exp $
 */

package org.tigr.microarray.mev.cluster.gui.impl.surv;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.tigr.microarray.mev.ISlideData;
import org.tigr.microarray.mev.MultipleArrayData;
import org.tigr.microarray.mev.TMEV;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog;
import org.tigr.microarray.mev.cluster.gui.helpers.ClusterBrowser;
import org.tigr.microarray.mev.cluster.gui.helpers.ClusterSelector;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.HelpWindow;
import org.tigr.microarray.mev.cluster.clusterUtil.Cluster;
import org.tigr.microarray.mev.cluster.clusterUtil.ClusterRepository;
import org.tigr.util.FloatMatrix;

/**
 *
 * @author  eleanorahowe
 * @version 
 */
public class SURVInitBox extends AlgorithmDialog {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final int JUST_ALPHA = 1;
    public static final int STD_BONFERRONI = 2;
    public static final int ADJ_BONFERRONI = 3;
    public static final int MAX_T = 9; 
    public static final int FALSE_NUM = 12;
    public static final int FALSE_PROP = 13;    
    public static final int BUTTON_SELECTION = 14;
    public static final int CLUSTER_SELECTION = 15;
    public static final int COX_MODEL = 16;
    
    boolean okPressed = false;
    boolean okReady = false;
    boolean step2 = false;
    Vector<String> exptNames;  
    Vector<String> sampleAnnotationNames;    
    MultiClassPanel mPanel;
    JTabbedPane selectionPanel;
    HCLoptionPanel hclOpsPanel;
    ClusterRepository sampleRepository, geneRepository;
    JButton step2Button = new JButton("Continue...");
    JLabel infoLabel;
    JLabel infoLabel2;
    
    IData data;
    
    /** Creates new LIMMAInitBox */
    public SURVInitBox(JFrame parentFrame, 
    		boolean modality, 
    		Vector<String> exptNames, 
    		ClusterRepository sampleRepository, 
    		ClusterRepository geneRepository,
    		IData data, 
    		Vector<String> sampleAnnotationNames) {
        super(parentFrame, "SURV Initialization", modality);
        this.exptNames = exptNames;  
        this.sampleAnnotationNames = sampleAnnotationNames;
        this.sampleRepository = sampleRepository;
        this.geneRepository = geneRepository;
        this.data = data;
        
        setBounds(0, 0, 1000, 850);
        setBackground(Color.white);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        
        JPanel pane = new JPanel();
        pane.setLayout(gridbag);
        this.okButton.setEnabled(false);
          
        mPanel = new MultiClassPanel();

        
        buildConstraints(constraints, 0, 0, 1, 1, 100, 80);
        gridbag.setConstraints(mPanel, constraints);
        pane.add(mPanel);   
        
        addContent(pane);
        EventListener listener = new EventListener();
        setActionListeners(listener);
        this.addWindowListener(listener);  

        
    }

    
    public void setVisible(boolean visible) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);
        
        super.setVisible(visible);
        
        if (visible) {
            //bPanel.okButton.requestFocus(); //UNCOMMMENT THIS LATER
        }
    }
    private void buildConstraints(GridBagConstraints gbc, int gx, int gy, int gw, int gh, int wx, int wy, int anc) {
        gbc.gridx = gx;
        gbc.gridy = gy;
        gbc.gridwidth = gw;
        gbc.gridheight = gh;
        gbc.weightx = wx;
        gbc.weighty = wy;
        gbc.anchor = anc;
    }
    private void buildConstraints(GridBagConstraints gbc, int gx, int gy, int gw, int gh, int wx, int wy, int anc, int fill) {
        gbc.gridx = gx;
        gbc.gridy = gy;
        gbc.gridwidth = gw;
        gbc.gridheight = gh;
        gbc.weightx = wx;
        gbc.weighty = wy;
        gbc.anchor = anc;
        gbc.fill = fill;
    }
    private void buildConstraints(GridBagConstraints gbc, int gx, int gy, int gw, int gh, int wx, int wy) {
        gbc.gridx = gx;
        gbc.gridy = gy;
        gbc.gridwidth = gw;
        gbc.gridheight = gh;
        gbc.weightx = wx;
        gbc.weighty = wy;
    }
    
    
    public boolean isOkPressed() {
        return okPressed;
    }    
    
    public void enableOK(){
    	this.okButton.setEnabled(true);
    }
    
    public boolean drawTrees() {
        return this.hclOpsPanel.isHCLSelected();
    }   
    
    public boolean drawSigTreesOnly() {
        return hclOpsPanel.drawSigTreesOnly();
    }    

    class HCLoptionPanel extends JPanel {
    	 
        private JCheckBox hclCluster;  
        private JRadioButton sigOnly, allClusters;
        GridBagConstraints constraints = new GridBagConstraints();
        GridBagLayout gridbag = new GridBagLayout();
        /** Creates a new instance of HCLSigOnlyPanel */
        public HCLoptionPanel() {
            super();
            this.setBackground(Color.white);
            Font font = new Font("Dialog", Font.BOLD, 12);
            this.setLayout(gridbag);
            this.setBorder( BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Hierarchical Clustering", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, font, Color.black));
            hclCluster = new JCheckBox("Construct Hierarchical Trees for :            ");
            hclCluster.setFocusPainted(false);
            hclCluster.setBackground(Color.white);
            hclCluster.setForeground(UIManager.getColor("Label.foreground"));
            
            sigOnly = new JRadioButton("Significant genes only", true);
            sigOnly.setBackground(Color.white);
            sigOnly.setForeground(UIManager.getColor("Label.foreground"));     
            
            allClusters = new JRadioButton("All clusters", false);
            allClusters.setBackground(Color.white);
            allClusters.setForeground(UIManager.getColor("Label.foreground"));        

            sigOnly.setEnabled(false);
            allClusters.setEnabled(false);
            
            ButtonGroup allOrSig = new ButtonGroup();
            allOrSig.add(sigOnly);
            allOrSig.add(allClusters);
            
            hclCluster.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    if (e.getStateChange() == ItemEvent.DESELECTED) {
                        sigOnly.setEnabled(false);
                        allClusters.setEnabled(false);
                    } else {
                        sigOnly.setEnabled(true);
                        allClusters.setEnabled(true);                    
                    }
                }
            });        

            buildConstraints(constraints, 0, 0, 1, 1, 25, 10, GridBagConstraints.WEST);
            gridbag.setConstraints(hclCluster, constraints);
            add(hclCluster);
            buildConstraints(constraints, 0, 1, 1, 1, 25, 10);
            gridbag.setConstraints(sigOnly, constraints);
            add(sigOnly);
            buildConstraints(constraints, 0, 2, 1, 1, 25, 10);
            gridbag.setConstraints(allClusters, constraints);
            add(allClusters);
            
            JPanel dummyPanel = new JPanel();

            buildConstraints(constraints, 0, 3, 1, 1, 25, 100);
            gridbag.setConstraints(dummyPanel, constraints);
            dummyPanel.setBackground(Color.white);
            add(dummyPanel);
        }
        
        public HCLoptionPanel(Color background){
            this();
            setBackground(background);
        }
        
        public boolean isHCLSelected(){
            return hclCluster.isSelected();
        }  
        
        public boolean drawSigTreesOnly() {
            return sigOnly.isSelected();
        }
        
        public void setHCLSelected(boolean value){
                hclCluster.setSelected(value);
        }    
        
    }
    protected double getLambda() {
    	return new Double(mPanel.lambdaField.getText());
    }
    class MultiClassPanel extends JPanel {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		DesignPanel ngPanel;
        GridBagConstraints constraints;
        GridBagLayout gridbag;
        JPanel dummyPanel;
        ExperimentsSelectionPanel sampleSelectionPanel;
        ExperimentsSelectionPanel FactorAESP;
        ExperimentsSelectionPanel FactorBESP;
        ExperimentsSelectionPanel ConditionESP;
        ExperimentsSelectionPanel TimePointESP;
        JTabbedPane tabbedmulg;
        ClusterSelector groupsCS,factorACS,factorBCS,conditionCS,timepointCS;
        JLabel infoLabel;
        JLabel infoLabel2;
        int numGroups=2;
        int factorAlevels=-1;
        int factorBlevels=-1;
        float alpha;
        String factorAName;
        String factorBName;
        
        //Cox Model parameters.
        ClusterBrowser geneClusterBrowser;
        JTextField lambdaField;
        JLabel lambdaLabel;
        JPanel coxPanel;
        JRadioButton useAllGenes, useCluster;
        ButtonGroup geneSelection;
        
        public MultiClassPanel() {
        	
            constraints = new GridBagConstraints();
            gridbag = new GridBagLayout();
            this.setBackground(Color.white);
            this.setLayout(gridbag);
            ngPanel = new DesignPanel();

            buildConstraints(constraints, 0, 0, 1, 1, 100, 10);
            constraints.fill = GridBagConstraints.BOTH;
            gridbag.setConstraints(ngPanel, constraints);
            
            step2Button.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent evt) {
                	if (step2){
                		goBack();
                		return;
                	}
                	initiatePanels();
                }
            });
            
            JPanel topPanel =  new JPanel();
            topPanel.setBackground(Color.white);
            topPanel.setLayout(gridbag);
            buildConstraints(constraints, 0, 0, 1, 2, 75, 100);
            gridbag.setConstraints(ngPanel, constraints);
            topPanel.add(ngPanel);

            hclOpsPanel = new HCLoptionPanel();
            hclOpsPanel.setBorder(null);
            hclOpsPanel.setVisible(false);
            topPanel.add(hclOpsPanel);
            
            constraints = new GridBagConstraints();
            buildConstraints(constraints, 1, 1, 1, 1, 1, 0, GridBagConstraints.SOUTH, GridBagConstraints.NONE);
            gridbag.setConstraints(step2Button, constraints);
            topPanel.add(step2Button);
            
            constraints = new GridBagConstraints();
            constraints.fill = GridBagConstraints.BOTH;
            buildConstraints(constraints, 1, 1, 1, 1, 0, 10);
            topPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "SURV Parameters",TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, Color.black));
            buildConstraints(constraints, 0, 0,1,1,100,10);
            gridbag.setConstraints(topPanel, constraints);
            this.add(topPanel);
            
            infoLabel = new JLabel("Group Assignment");
            infoLabel.setMaximumSize(new Dimension(50,50));
            Font font = infoLabel.getFont();
            infoLabel.setFont(font.deriveFont(20.0f));
            buildConstraints(constraints, 0, 1, 1, 1, 0, 5,GridBagConstraints.CENTER, GridBagConstraints.NONE);
            gridbag.setConstraints(infoLabel, constraints);
            
            this.add(infoLabel, constraints);
            infoLabel2 = new JLabel("Please select the type of survival analysis to be run, then click 'Continue'.");
            buildConstraints(constraints, 0, 2, 1, 1, 100, 5,GridBagConstraints.CENTER);
            gridbag.setConstraints(infoLabel2, constraints);
            
            this.add(infoLabel2, constraints);
            
            
            buildConstraints(constraints, 0, 3, 1, 1, 100, 90);
            dummyPanel = new JPanel();
            dummyPanel.setBackground(Color.white);
            
            gridbag.setConstraints(dummyPanel, constraints);
            this.add(dummyPanel);
            
        }

        private void goBack(){
    		infoLabel.setVisible(true);
            infoLabel2.setVisible(true);
            ngPanel.numGroupsField.setEnabled(true);
            ngPanel.alphaField.setEnabled(true);
            ngPanel.oneClass.setEnabled(true);
            ngPanel.twoClass.setEnabled(true);
            ngPanel.factorALevel.setEnabled(true);
            ngPanel.factorBLevel.setEnabled(true);
            ngPanel.factorAName.setEnabled(true);
            ngPanel.factorBName.setEnabled(true);
            step2Button.setText("Continue...");
            step2 = false;
            tabbedmulg.setVisible(false);
            buildConstraints(constraints, 0, 1, 2, 1, 0, 90);
            constraints.fill = GridBagConstraints.BOTH;
            buildConstraints(constraints, 0, 3, 1, 1, 100, 90);
            gridbag.setConstraints(dummyPanel, constraints);
            MultiClassPanel.this.add(dummyPanel);
            step2Button.setSelected(false);
        }
        
        private void initiatePanels(){
            ngPanel.okPressed = true;
            okReady = true;
            try {
            	alpha = Float.parseFloat(ngPanel.alphaField.getText());
            	numGroups = 0;
            	if (getExperimentalDesign()==1)
            		numGroups = 1;
            	if (getExperimentalDesign()==2)
            		numGroups = 2;

            }catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(null, "Error reading parameter input.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (alpha>1||alpha<0){//checks alpha value
            	JOptionPane.showMessageDialog(null, "Please enter an alpha value between 0 and 1.", "Error", JOptionPane.ERROR_MESSAGE);
            	return;
            }
            if (numGroups<2&&getExperimentalDesign()!=4&&getExperimentalDesign()!=1){ //excludes factorial and one-class design when checking for enough timepoints
            	JOptionPane.showMessageDialog(null, "The number of groups must be greater than 1.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            JPanel selectionPanel = new JPanel();
            GridBagLayout gbg = new GridBagLayout();
            selectionPanel.setLayout(gbg);
            GridBagConstraints cnstr = new GridBagConstraints();

            buildConstraints(cnstr, 0, 0, 1, 1, 1, 1);
            cnstr.fill = GridBagConstraints.BOTH;
            JPanel clusterSelectorPanel = new JPanel();
            clusterSelectorPanel.setLayout(new GridBagLayout());
            JLabel clusterInstructions = new JLabel("Use the drop-down menus to assign clusters of samples to their corresponding groups.");
            GridBagConstraints c = new GridBagConstraints();
            c.gridwidth=2;
            clusterSelectorPanel.add(clusterInstructions, c);

            MultiClassPanel.this.remove(dummyPanel);
            
            //Cox model 
            if(getExperimentalDesign() == 1) {
            	lambdaLabel = new JLabel("Initial Lambda Value: ");
            	//TODO validate that only numbers are in here. 
            	lambdaField = new JTextField("10", 2);
            	sampleSelectionPanel = new ExperimentsSelectionPanel(exptNames, numGroups, ngPanel.getExperimentDesign(), "Group", true);

                useAllGenes=new JRadioButton("Build model from all loaded gene data.", true);
                useCluster=new JRadioButton("Build model from selected gene cluster.", false);          
                useAllGenes.setBackground(Color.white);
                useAllGenes.setBorder(null);
                useCluster.setBackground(Color.white);
                useCluster.setBorder(null);
                  
                geneSelection = new ButtonGroup();
                geneSelection.add(useAllGenes);
                geneSelection.add(useCluster);

                useAllGenes.addActionListener(new ActionListener(){
                  public void actionPerformed(ActionEvent ae) {
                	  if(useAllGenes.isSelected()) {
                		  geneClusterBrowser.setEnabled(false);
                	  } else {
                		  if (useCluster.isSelected()) {
                			  geneClusterBrowser.setEnabled(true);
                		  }
                	  }
                  }
                });
                useCluster.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent ae) {
                  	  if(useAllGenes.isSelected()) {
                  		  geneClusterBrowser.setEnabled(false);
                  	  } else {
                  		  if (useCluster.isSelected()) {
                  			  geneClusterBrowser.setEnabled(true);
                  		  }
                  	  }
                  		  
                    }
                  });

                geneClusterBrowser = new ClusterBrowser(geneRepository);
                //http://java.sun.com/docs/books/tutorial/uiswing/layout/gridbag.html
	            JPanel browserpanel = new JPanel();
	            browserpanel.setLayout(gbg);
	            browserpanel.setBackground(Color.white);
	            
                buildConstraints(constraints, 0, 0, 1, 1, 0, 0);
	            constraints.fill = GridBagConstraints.BOTH;
	            browserpanel.add(lambdaLabel, constraints);

                buildConstraints(constraints, 1, 0, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE);
	            constraints.fill = GridBagConstraints.BOTH;
	            browserpanel.add(lambdaField, constraints);
	            
                buildConstraints(constraints, 0, 1, 2, 1, 0, 0);
	            constraints.fill = GridBagConstraints.BOTH;
	            browserpanel.add(useAllGenes, constraints);


                buildConstraints(constraints, 0, 2, 2, 1, 0, 0);
	            constraints.fill = GridBagConstraints.BOTH;
                browserpanel.add(useCluster, constraints);

                buildConstraints(constraints, 2, 0, 1, 3, 1, 1, GridBagConstraints.EAST, GridBagConstraints.BOTH);
	            constraints.fill = GridBagConstraints.BOTH;
	            browserpanel.add(geneClusterBrowser, constraints);
	            
	            //If no gene clusters available, set useCluster to disabled
	            if(geneRepository.isEmpty()) {
	            	useCluster.setEnabled(false);
	            }
	            useAllGenes.setSelected(true);
	            tabbedmulg = new JTabbedPane();
//	            tabbedmulg.setBackground(Color.white);
	            tabbedmulg.add("Cox Proportional Hazard Model Options", browserpanel);
                buildConstraints(constraints, 0, 1, 5, 5, 0, 90);
	            constraints.fill = GridBagConstraints.BOTH;
	            gridbag.setConstraints(tabbedmulg, constraints);
	            MultiClassPanel.this.add(tabbedmulg);
	            MultiClassPanel.this.validate();
	            
            //Kaplan-Meier survival comparison
        	} else {
        		sampleSelectionPanel = new ExperimentsSelectionPanel(exptNames, numGroups, ngPanel.getExperimentDesign(), "Group", true);
        		selectionPanel.add(sampleSelectionPanel, cnstr);
        		cnstr.gridy++;
        		cnstr.weighty = 0;
        		selectionPanel.add(createSaveLoadPanel(), cnstr);
        		
        		if(getExperimentalDesign()==5){//ever occur?
        			groupsCS= new ClusterSelector(sampleRepository, numGroups, "Timepoint");
	            	groupsCS.setClusterType("Timepoint");
        		}else{
        			groupsCS= new ClusterSelector(sampleRepository, numGroups, "Class");
        			groupsCS.setClusterType("Class");
        		}
	            	
	            buildConstraints(c, 0, 1, 1, 1, 1, 1);
	            c.fill = GridBagConstraints.BOTH;
	            c.gridx = 1;
	            clusterSelectorPanel.add(groupsCS, c);

	            
	            
	            tabbedmulg = new JTabbedPane();
	            tabbedmulg.add("Button Selection", selectionPanel);
	            tabbedmulg.add("Cluster Selection", clusterSelectorPanel);
	            tabbedmulg.setSelectedIndex(1);
	            if (sampleRepository==null||sampleRepository.isEmpty())
	            	tabbedmulg.setSelectedIndex(0);
	            buildConstraints(constraints, 0, 1, 2, 1, 0, 90);
	            constraints.fill = GridBagConstraints.BOTH;
	            gridbag.setConstraints(tabbedmulg, constraints);
	            MultiClassPanel.this.add(tabbedmulg);
	            MultiClassPanel.this.validate();    
	            ngPanel.numGroupsField.setEnabled(false);
	            ngPanel.alphaField.setEnabled(false);
	            ngPanel.oneClass.setEnabled(false);
	            ngPanel.twoClass.setEnabled(false);
	            ngPanel.factorALevel.setEnabled(false);
	            ngPanel.factorBLevel.setEnabled(false);
	            ngPanel.factorAName.setEnabled(false);
	            ngPanel.factorBName.setEnabled(false);
        	}

            enableOK();
 
            step2Button.setText("<<< Go Back");
            infoLabel.setVisible(false);
            infoLabel2.setVisible(false);
            ngPanel.oneClass.setEnabled(false);
            ngPanel.twoClass.setEnabled(false);
            step2 = true;
        }
        
        class DesignPanel extends JPanel {
            private static final String LAST_SELECTED_SURVIVAL_FIELDNAME = "last-selected-survival-fieldname";
			private static final String LAST_SELECTED_CENSOREDSTATUS_FIELDNAME = "last-selected-censoredstatus-fieldname";
			JTextField factorAName, factorBName, factorALevel, factorBLevel, numGroupsField, alphaField;
            JLabel numGroupsLabel; 
            JPanel factorPanel;
            boolean okPressed = false;
            JRadioButton oneClass, twoClass;
            
            JLabel survivalLabel, survivalInfo, censoredLabel, censoredInfo, instructionLabel;
            JComboBox survival, censored;
            
            boolean validEventData = false;
            boolean validCensoringData = false;

            private void validateSurvival() {
            	String fieldname = survival.getSelectedItem().toString();
            	boolean canConvert = true;
            	for(int i=0; i<data.getExperiment().getNumberOfSamples(); i++) {
                	String ann = data.getSampleAnnotation(i, fieldname);
            		try {
            			Float.parseFloat(ann);
            		} catch(NumberFormatException nfe) {
            			canConvert = false;
            			break;
            		}
            	}
            	if(!canConvert) {
            		survivalInfo.setText("Not valid floating-point values.");
            		survivalInfo.setForeground(Color.red);
            		validEventData = false;
            	} else {
            		survivalInfo.setText("Data OK.");
            		survivalInfo.setForeground(Color.black);
            		validEventData = true;
            	}
            	TMEV.storeProperty(LAST_SELECTED_SURVIVAL_FIELDNAME, fieldname);
            }    
            private void validateCensoringData() {
            	String fieldname = censored.getSelectedItem().toString();
            	boolean canConvert = true;
            	for(int i=0; i<data.getExperiment().getNumberOfSamples(); i++) {
                	String ann = data.getSampleAnnotation(i, fieldname);
            		if(ann.equalsIgnoreCase("yes") ||
            				ann.equalsIgnoreCase("y") ||
            				ann.equalsIgnoreCase("true") ||
            				ann.equalsIgnoreCase("false") ||
            				ann.equalsIgnoreCase("no") ||
            				ann.equalsIgnoreCase("n") ||
            				ann.equalsIgnoreCase("censored") ||
            				ann.equalsIgnoreCase("uncensored") ) {
            			canConvert=true;
            		} else {
            			canConvert = false;
            			break;
            		}
            	}
            	if(!canConvert) {
            		censoredInfo.setText("Not valid true/false values.");
            		censoredInfo.setForeground(Color.red);
            		validCensoringData = false;
            	} else {
            		censoredInfo.setText("Data OK.");
            		censoredInfo.setForeground(Color.black);
            		validCensoringData = true;
            	}
            	TMEV.storeProperty(LAST_SELECTED_CENSOREDSTATUS_FIELDNAME, fieldname);
            }
            public DesignPanel() {
                setBackground(Color.white);
                GridBagLayout gridbag = new GridBagLayout();
                GridBagConstraints constraints = new GridBagConstraints();
                
                this.setLayout(gridbag);
                this.setMinimumSize(new Dimension(300,100));
                JLabel dataTypeLabel = new JLabel("Choose Type of Analysis: ");


                oneClass=new JRadioButton("Build a Cox proportional hazards model", false);
                twoClass=new JRadioButton("Kaplan-Meier Test between two groups", true);          
                oneClass.setBackground(Color.white);
                oneClass.setBorder(null);
                twoClass.setBackground(Color.white);
                twoClass.setBorder(null);
                  
                ButtonGroup dataType = new ButtonGroup();
                dataType.add(oneClass);
                dataType.add(twoClass);

                oneClass.addActionListener(new RadioButtonListener());
                twoClass.addActionListener(new RadioButtonListener());

                buildConstraints(constraints, 0, 0, 1, 1, 30, 100,GridBagConstraints.WEST);
                gridbag.setConstraints(dataTypeLabel, constraints);
                this.add(dataTypeLabel);
                buildConstraints(constraints, 0, 1, 1, 1, 30, 100);
                constraints.anchor = GridBagConstraints.WEST;
                gridbag.setConstraints(oneClass, constraints);
                this.add(oneClass);
                buildConstraints(constraints, 0, 2, 1, 1, 30, 100);
                constraints.anchor = GridBagConstraints.WEST;
                gridbag.setConstraints(twoClass, constraints);
                this.add(twoClass);


//selection of sample annotation fields...
                instructionLabel = new JLabel("Please choose the annotation containing the survival data.");
                survival = new JComboBox(sampleAnnotationNames);
                survivalLabel = new JLabel("Survival time or time to event (numeric): ");
                survivalInfo = new JLabel("Data Ok.");

                survival.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						validateSurvival();
					}
				});
                if(sampleAnnotationNames.contains(TMEV.getSettingForOption(LAST_SELECTED_SURVIVAL_FIELDNAME))) {
                	survival.setSelectedItem(TMEV.getSettingForOption(LAST_SELECTED_SURVIVAL_FIELDNAME));
                }
                
                censored = new JComboBox(sampleAnnotationNames);

                censoredLabel = new JLabel("Censoring flag (yes/no, censored/uncensored): ");
                censoredInfo = new JLabel("Data Ok.");

                censored.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						validateCensoringData();
					}
				});
                if(sampleAnnotationNames.contains(TMEV.getSettingForOption(LAST_SELECTED_CENSOREDSTATUS_FIELDNAME))) {
                	censored.setSelectedItem(TMEV.getSettingForOption(LAST_SELECTED_CENSOREDSTATUS_FIELDNAME));
                }
                
                buildConstraints(constraints, 1, 0, 2, 1, 30, 100);
                constraints.anchor = GridBagConstraints.WEST;
                gridbag.setConstraints(instructionLabel, constraints);
                this.add(instructionLabel);
                buildConstraints(constraints, 1, 1, 2, 1, 30, 100);
                constraints.anchor = GridBagConstraints.WEST;
                gridbag.setConstraints(survivalLabel, constraints);
                this.add(survivalLabel);
                buildConstraints(constraints, 1, 2, 1, 1, 30, 100);
                constraints.anchor = GridBagConstraints.WEST;
                gridbag.setConstraints(survival, constraints);
                this.add(survival);
                buildConstraints(constraints, 2, 2, 1, 1, 30, 100);
                constraints.anchor = GridBagConstraints.WEST;
                gridbag.setConstraints(survivalInfo, constraints);
                this.add(survivalInfo);
                validateSurvival();
                
                buildConstraints(constraints, 1, 3, 2, 1, 30, 100);
                constraints.anchor = GridBagConstraints.WEST;
                gridbag.setConstraints(censoredLabel, constraints);
                this.add(censoredLabel);
                buildConstraints(constraints, 1, 4, 1, 1, 30, 100);
                constraints.anchor = GridBagConstraints.WEST;
                gridbag.setConstraints(censored, constraints);
                this.add(censored);
                buildConstraints(constraints, 2, 4, 1, 1, 30, 100);
                constraints.anchor = GridBagConstraints.WEST;
                gridbag.setConstraints(censoredInfo, constraints);
                this.add(censoredInfo);
                validateCensoringData();
                
                
                numGroupsLabel = new JLabel("Number of groups: ");
                numGroupsLabel.setVisible(false);
                
                numGroupsField = new JTextField("4", 7);
                numGroupsField.setVisible(false);
                numGroupsField.setMinimumSize(new Dimension(50,20));
                
                factorPanel =  new JPanel();
                factorPanel.setBackground(Color.white);
                GridBagLayout gridbag2 = new GridBagLayout();
                factorPanel.setLayout(gridbag2);
                JLabel factorALabel = new JLabel("Factor A Name: ");
                buildConstraints(constraints, 0, 0, 1, 1, 30, 0);
                gridbag2.setConstraints(factorALabel, constraints);
                factorPanel.add(factorALabel);

                JLabel factorBLabel = new JLabel("Factor B Name: ");
                buildConstraints(constraints, 0, 1, 1, 1, 30, 0);
                gridbag2.setConstraints(factorBLabel, constraints);
                factorPanel.add(factorBLabel);
                
                factorAName = new JTextField("FactorA", 7);
                factorAName.setMinimumSize(new Dimension(50,20));
                buildConstraints(constraints, 1, 0, 1, 1, 30, 0);
                gridbag2.setConstraints(factorAName, constraints);
                factorPanel.add(factorAName);

                factorBName = new JTextField("FactorB", 7);
                factorBName.setMinimumSize(new Dimension(50,20));
                buildConstraints(constraints, 1, 1, 1, 1, 30, 0);
                gridbag2.setConstraints(factorBName, constraints);
                factorPanel.add(factorBName);

                JLabel levelALabel = new JLabel("    Number of Levels: ");
                buildConstraints(constraints, 2, 0, 1, 1, 30, 0);
                gridbag2.setConstraints(levelALabel, constraints);
                factorPanel.add(levelALabel);

                JLabel levelBLabel = new JLabel("    Number of Levels: ");
                buildConstraints(constraints, 2, 1, 1, 1, 30, 0);
                gridbag2.setConstraints(levelBLabel, constraints);
                factorPanel.add(levelBLabel);

                factorALevel = new JTextField("2", 7);
                factorALevel.setMinimumSize(new Dimension(50,20));
                buildConstraints(constraints, 3, 0, 1, 1, 30, 0);
                gridbag2.setConstraints(factorALevel, constraints);
                factorPanel.add(factorALevel);

                factorBLevel = new JTextField("2", 7);
                factorBLevel.setMinimumSize(new Dimension(50,20));
                buildConstraints(constraints, 3, 1, 1, 1, 30, 0);
                gridbag2.setConstraints(factorBLevel, constraints);
                factorPanel.add(factorBLevel);

                buildConstraints(constraints, 0, 5, 2, 1, 30, 0, GridBagConstraints.CENTER);
                constraints.ipady = 20;
                gridbag.setConstraints(factorPanel, constraints);
                factorPanel.setVisible(false);
                this.add(factorPanel);
                constraints.ipady = 0;
                
                JLabel alphaLabel = new JLabel("Significance Level: Alpha = ");
//                buildConstraints(constraints, 0, 6, 1, 1, 30, 100);
//                constraints.anchor = GridBagConstraints.EAST;
//                gridbag.setConstraints(alphaLabel, constraints);
//                this.add(alphaLabel);
                
                alphaField = new JTextField(".05", 7);
                alphaField.setMinimumSize(new Dimension(50,20));
//                constraints.anchor = GridBagConstraints.WEST;
//                buildConstraints(constraints, 1, 6, 1, 1, 30, 0);
//                gridbag.setConstraints(alphaField, constraints);
//                this.add(alphaField);
                
                
            }
            
            public int getExperimentDesign(){
            	if (oneClass.isSelected())
            		return 1;
            	if (twoClass.isSelected())
            		return 2;
            	return 2;
            }
            
            public void setVisible(boolean visible) {
                //Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                setLocation((MultiClassPanel.this.getWidth() - getSize().width)/2, (MultiClassPanel.this.getHeight() - getSize().height)/2);
                
                super.setVisible(visible);
                
                if (visible) {
                    //bPanel.okButton.requestFocus(); //UNCOMMMENT THIS LATER
                }
            }
            
            public boolean isOkPressed() {
                return okPressed;
            }
            
        }

        /**
    	 * Saves the assignments to file.
    	 * 
    	 * Comments include title, user, save date
    	 * Design information includes factor a and b labels and the level names for each factor
    	 * A header row is followed by sample index, sample name (primary, field index = 0),
    	 * them factor A assignment (text label) then factor B assignment (text label)
    	 */
    	private void saveAssignments() {
    		
    		File file;		
    		JFileChooser fileChooser = new JFileChooser(TMEV.getDataPath());	
    		
    		if(fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
    			file = fileChooser.getSelectedFile();			
    			try {
    				PrintWriter pw = new PrintWriter(new FileWriter(file));
    				
    				//comment row
    				Date currDate = new Date(System.currentTimeMillis());			
    				String dateString = currDate.toString();;
    				String userName = System.getProperty("user.name");
    				
    				pw.println("# Assignment File");
    				pw.println("# User: "+userName+" Save Date: "+dateString);
    				pw.println("#");
    				
    				//save group names..?
    				
    				pw.print("Module:\t");
    				pw.println("SURVIVAL");
    				pw.print("Design:\t");
    				pw.println(ngPanel.getExperimentDesign());
    				int groupMax;
    				if (ngPanel.getExperimentDesign()!=4)
    					groupMax=this.numGroups;
    				else
    					groupMax = Math.max(this.factorAlevels,this.factorBlevels);
    				for (int i=0; i<groupMax; i++){
        				pw.print("Group "+(i+1)+" Label:\t");
    					pw.println("Group "+(i+1));
    				}
    								
    				pw.println("#");
    				
    				pw.println("Sample Index\tSample Name\tGroup Assignment");

    				
    				if (ngPanel.getExperimentDesign()<4){
    					int[] groupAssgn=getGroupAssignments();
        				for(int sample = 0; sample < exptNames.size(); sample++) {
        					pw.print(String.valueOf(sample+1)+"\t"); //sample index
        					pw.print(exptNames.get(sample)+"\t");
        					if (groupAssgn[sample]!=0)
        						pw.println("Group "+(groupAssgn[sample]));
        					else
        						pw.println("Exclude");
        					
        				}
        			}else{
        					
        				for(int sample = 0; sample < exptNames.size(); sample++) {
        					pw.print(String.valueOf(sample+1)+"\t"); //sample index
        					pw.print(exptNames.get(sample)+"\t");
        					int a = 0;
        		        	int b = 0;
            				if (ngPanel.getExperimentDesign()==4){
	        		            for (int j = 0; j < mPanel.FactorAESP.assignmentRBs.length; j++) {
	        		                if (mPanel.FactorAESP.assignmentRBs[j][sample].isSelected()) {
	        		                    a = j+1;
	        		                    break;
	        		                }
	        		            }
	        		            for (int j = 0; j < mPanel.FactorBESP.assignmentRBs.length; j++) {
	        		                if (mPanel.FactorBESP.assignmentRBs[j][sample].isSelected()) {
	        		                    b = j+1;
	        		                    break;
	        		                }
	        		            }
        					}else{
	        		            for (int j = 0; j < mPanel.ConditionESP.assignmentRBs.length; j++) {
	        		                if (mPanel.ConditionESP.assignmentRBs[j][sample].isSelected()) {
	        		                    b = j+1;
	        		                    break;
	        		                }
	        		            }
	        		            for (int j = 0; j < mPanel.TimePointESP.assignmentRBs.length; j++) {
	        		                if (mPanel.TimePointESP.assignmentRBs[j][sample].isSelected()) {
	        		                    a = j+1;
	        		                    break;
	        		                }
	        		            }        						
        					}
    						pw.println((a==0?"Exclude":("Group "+(a)))+"\t"+(b==0?"Exclude":"Group "+(b)));
        				}
    				}
    				pw.flush();
    				pw.close();			
    			} catch (FileNotFoundException fnfe) {
    				fnfe.printStackTrace();
    			} catch (IOException ioe) {
    				ioe.printStackTrace();
    			}
    		}
    	}
        class ExperimentsSelectionPanel extends JPanel {
        	int design = 0;
            int numPanels = 0;
            JLabel[] expLabels;
            JRadioButton[][] assignmentRBs;
            JRadioButton[] notInTimeGroupRadioButtons;
            ExperimentsSelectionPanel(Vector<String> exptNames, int numGroups, int design, String title, boolean firstPanel) {
            	numGroups = 2;
//TODO selection...
            	this.design = design;
                this.setBorder(new TitledBorder(new EtchedBorder(), title+" Assignments", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), Color.black));
                setBackground(Color.white);

                expLabels = new JLabel[exptNames.size()];
                assignmentRBs = new JRadioButton[numGroups][exptNames.size()];
                numPanels = exptNames.size()/512 + 1;
                
                notInTimeGroupRadioButtons = new JRadioButton[exptNames.size()];
                ButtonGroup chooseTime[] = new ButtonGroup[exptNames.size()];
                ButtonGroup chooseCondition[] = new ButtonGroup[exptNames.size()];
                
                GridBagLayout gridbag = new GridBagLayout();
                GridBagLayout gridbag2 = new GridBagLayout();
                GridBagConstraints constraints = new GridBagConstraints();
                this.setLayout(gridbag2);

                JPanel [] panels = new JPanel[numPanels];
                
                int currPanel = 0;
                for(int i = 0; i < panels.length; i++) {
                    panels[i] = new JPanel(gridbag);
                }
                
                for (int i = 0; i < exptNames.size(); i++) {
                    String s1 = (String)(exptNames.get(i));
                    expLabels[i] = new JLabel(s1);
                    chooseTime[i] = new ButtonGroup();
                    chooseCondition[i] = new ButtonGroup();
//                    if (design==1){
//	                    assignmentRBs[0][i] = new JRadioButton("Include ", false);
//	                    chooseTime[i].add(assignmentRBs[0][i]);
//	                    assignmentRBs[0][i].setSelected(true);
//                    }else if (design==5){
//                    	for (int j = 0; j < numGroups; j++) {
//		                    assignmentRBs[j][i] = new JRadioButton(title+" " + (j) + "     ", false);
//		                    chooseTime[i].add(assignmentRBs[j][i]);
//		                }
//                    }else{
		                for (int j = 0; j < numGroups; j++) {
		                    assignmentRBs[j][i] = new JRadioButton("Group " + (j+1) + "     ", false);
		                    chooseTime[i].add(assignmentRBs[j][i]);
		                }
//                    }
                    
                    
                    //set current panel
                    currPanel = i / 512;
                    
                    notInTimeGroupRadioButtons[i] = new JRadioButton("Unassigned", true);
                    chooseTime[i].add(notInTimeGroupRadioButtons[i]);
                    int twoCondRoom = 0;
                    
                    for (int j = 0; j < numGroups; j++) {
                        buildConstraints(constraints, j+twoCondRoom, i%512, 1, 1, 100, 100);
                        gridbag.setConstraints(assignmentRBs[j][i], constraints);
                        panels[currPanel].add(assignmentRBs[j][i]);
                    }
                    
                    buildConstraints(constraints, (numGroups + 1+twoCondRoom), i%512, 1, 1, 100, 100);
                    gridbag.setConstraints(notInTimeGroupRadioButtons[i], constraints);
                    
                    
                    panels[currPanel].add(notInTimeGroupRadioButtons[i]);                    
                    
                    
                }
                
                int maxLabelWidth = 0;
                
                for (int i = 0; i < expLabels.length; i++) {
                    if (expLabels[i].getPreferredSize().getWidth() > maxLabelWidth) {
                        maxLabelWidth = (int)Math.ceil(expLabels[i].getPreferredSize().getWidth());
                    }
                }
                
                JPanel bigPanel = new JPanel(new GridBagLayout());
                
                for(int i = 0; i < numPanels; i++) {
                    bigPanel.add(panels[i] ,new GridBagConstraints(0,i,1,1,1,1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
                }
                
                JScrollPane scroll = new JScrollPane(bigPanel);
                scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
                scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
                
                
                
                JPanel [] exptNameHeaderPanels = new JPanel[this.numPanels];
                GridBagLayout exptHeaderGridbag = new GridBagLayout();
                for(int i = 0; i < exptNameHeaderPanels.length; i++) {
                    exptNameHeaderPanels[i] = new JPanel();
                    exptNameHeaderPanels[i].setSize(50, panels[i].getPreferredSize().height);
                    exptNameHeaderPanels[i].setPreferredSize(new Dimension(maxLabelWidth + 10, panels[i].getPreferredSize().height));
                    exptNameHeaderPanels[i].setLayout(exptHeaderGridbag);
                }
                
                //need to add to additional panels if number of samples exceeds 512
                for (int i = 0; i < expLabels.length; i++) {
                    currPanel = i / 512;
                    buildConstraints(constraints, 0, i%512, 1, 1, 100, 100);
                    constraints.fill = GridBagConstraints.BOTH;
                    exptHeaderGridbag.setConstraints(expLabels[i], constraints);
                    exptNameHeaderPanels[currPanel].add(expLabels[i]);
                }

                JPanel headerPanel = new JPanel(new GridBagLayout());
                for(int i = 0; i < exptNameHeaderPanels.length; i++) {
                    headerPanel.add(exptNameHeaderPanels[i], new GridBagConstraints(0,i,1,1,1,1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0)); 
                }
                
                scroll.setRowHeaderView(headerPanel);
                
                buildConstraints(constraints, 0, 0, 1, 1, 100, 90,GridBagConstraints.CENTER,GridBagConstraints.BOTH);
                gridbag2.setConstraints(scroll, constraints);
                this.add(scroll);
                
                
          
            }
            /**
             *  resets all group assignments
             */
            protected void reset(){
                for (int i = 0; i < exptNames.size(); i++) {
                	notInTimeGroupRadioButtons[i].setSelected(true);
                }
            }
        	
        }

    	/**
    	 * Loads file based assignments
    	 */
    	private void loadAssignments() {
    		/**
    		 * consider the following verifcations and policies
    		 *-number of loaded samples and rows in the assigment file should match, if not warning and quit
    		 *-each loaded file name should match a corresponding name in the assignment file, 1:1
    		 *		-if names don't match, throw warning and inform that assignments are based on loaded order
    		 *		 rather than a sample name
    		 *-the number of levels of factor A and factor B specified previously when defining the design
    		 *should match the number of levels in the assignment file, if not warning and quit
    		 *-if the level names match the level names entered then the level names will be used to make assignments
    		 *if not, then there will be a warning and the level index will be used.
    		 *-make sure that each level label pairs to a particular level index, this is a format 
    		 *-Note that all design labels in the assignment file will override existing labels
    		 *this means updating the data structures in this class, and updating AlgorithmData to set appropriate fields
    		 ***AlgorithmData modification requires a fixed vocab. for parameter names to be changed
    		 *these fields are (factorAName, factorBName, factorANames (level names) and factorANames (level names)
    		 *Wow, that was easy :)
    		 */
    		
    		File file;		
    		JFileChooser fileChooser = new JFileChooser(TMEV.getDataPath());
    		
    		if(fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
    		
    			file = fileChooser.getSelectedFile();
    			
        		try {						
        			//first grab the data and close the file
        			BufferedReader br = new BufferedReader(new FileReader(file));
        			Vector<String> data = new Vector<String>();
        			String line;
        			while( (line = br.readLine()) != null)
        				data.add(line.trim());
        			
        			br.close();
        				
        			//build structures to capture the data for assingment information and for *validation
        			
        			//factor names
        			Vector<String> groupNames = new Vector<String>();
//        			Vector<String> factorNames = new Vector<String>();
        			
        			
        			Vector<Integer> sampleIndices = new Vector<Integer>();
        			Vector<String> sampleNames = new Vector<String>();
        			Vector<String> groupAssignments = new Vector<String>();		
        			Vector<String> group2Assignments = new Vector<String>();		
        			
        			//parse the data in to these structures
        			String [] lineArray;
        			int design=0;
        			//String status = "OK";
        			for(int row = 0; row < data.size(); row++) {
        				line = (String)(data.get(row));

        				//if not a comment line, and not the header line
        				if(!(line.startsWith("#")) && !(line.startsWith("SampleIndex"))) {
        					
        					lineArray = line.split("\t");
        					
        					//check what module saved the file
        					if(lineArray[0].startsWith("Module:")) {
        						if (!lineArray[1].equals("SURVIVAL")){
        							Object[] optionst = { "Continue", "Cancel" };
        							if (JOptionPane.showOptionDialog(null, 
        		    						"The saved file was saved using a different module, "+lineArray[1]+". \n Would you like MeV to try to load it anyway?", 
        		    						"File type warning", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, 
        		    						optionst, optionst[0])==0)
        								continue;
        							return;
        						}
        						continue;
        					}
        					
        					//pick up group names
        					if(lineArray[0].startsWith("Group ") && lineArray[0].endsWith("Label:")) {
        						groupNames.add(lineArray[1]);
        						continue;
        					}
        					if(lineArray[0].startsWith("Design")) {
        						try {
        							design=Integer.parseInt(lineArray[1]);
	        					} catch ( NumberFormatException nfe) {
	        						//if not parsable continue
	        						continue;
	        					}
        						continue;
        					}
        						

        					//non-comment line, non-header line and not a group label line
        					
        					try {
        						Integer.parseInt(lineArray[0]);
        					} catch ( NumberFormatException nfe) {
        						//if not parsable continue
        						continue;
        					}
        					
        					sampleIndices.add(new Integer(lineArray[0]));
        					sampleNames.add(lineArray[1]);
        					groupAssignments.add(lineArray[2]);	
        					if (design==4||design==5)
        						group2Assignments.add(lineArray[3]);
        				}				
        			}
        			
        			//we have the data parsed, now validate, assign current data


        			if( exptNames.size() != sampleNames.size()) {
        				//status = "number-of-samples-mismatch";
//        				System.out.println(exptNames.size()+ " s length " + sampleNames.size());
        				//warn and prompt to continue but omit assignments for those not represented				

        				JOptionPane.showMessageDialog(this, "<html>Error -- number of samples designated in assignment file ("+String.valueOf(sampleNames.size())+")<br>" +
        						                                   "does not match the number of samples loaded in MeV ("+exptNames.size()+").<br>" +
        						                                   	"Assignments are not set.</html>", "File Compatibility Error", JOptionPane.ERROR_MESSAGE);
        				
        				return;
        			}
        			Vector<String> currSampleVector = new Vector<String>();
        			for(int i = 0; i < exptNames.size(); i++)
        				currSampleVector.add(exptNames.get(i));
        			
        			int fileSampleIndex = 0;
        			int groupIndex = 0;
        			String groupName;
        			String condName;
        			int condIndex = 0;
        			
        			for(int sample = 0; sample < exptNames.size(); sample++) {
        				boolean doIndex = false;
        				for (int i=0;i<exptNames.size(); i++){
        					if (i==sample)
        						continue;
        					if (exptNames.get(i).equals(exptNames.get(sample))){
        						doIndex=true;
        					}
        				}
        				fileSampleIndex = sampleNames.indexOf(exptNames.get(sample));
        				if (fileSampleIndex==-1){
        					doIndex=true;
        				}
        				if (doIndex){
        					setStateBasedOnIndex(groupAssignments, groupNames, design, group2Assignments);
        					break;
        				}
        				
        				groupName = (String)(groupAssignments.get(fileSampleIndex));
        				groupIndex = groupNames.indexOf(groupName);
    					if (design==4||design==5){
	        				condName = (String)(group2Assignments.get(fileSampleIndex));
	        				condIndex = groupNames.indexOf(condName);
        				}
        				
        				//set state
        				try{
                    		mPanel.sampleSelectionPanel.assignmentRBs[groupIndex][sample].setSelected(true);
        				}catch (Exception e){
                        	if (getExperimentalDesign()==4){
                        		mPanel.FactorAESP.notInTimeGroupRadioButtons[sample].setSelected(true);
                        		mPanel.FactorBESP.notInTimeGroupRadioButtons[sample].setSelected(true);
                        	} else if (getExperimentalDesign()==5){
                        		mPanel.TimePointESP.notInTimeGroupRadioButtons[sample].setSelected(true);
                        		mPanel.ConditionESP.notInTimeGroupRadioButtons[sample].setSelected(true);
                        	} else {
                        		mPanel.sampleSelectionPanel.notInTimeGroupRadioButtons[sample].setSelected(true);
                        	}
        				}
        			}
        			
        			repaint();			
        			//need to clear assignments, clear assignment booleans in sample list and re-init
        			//maybe a specialized inti for the sample list panel.
        		} catch (Exception e) {
        			e.printStackTrace();
        			JOptionPane.showMessageDialog(this, "<html>The file format cannot be read.</html>", "File Compatibility Error", JOptionPane.ERROR_MESSAGE);
        		}
        	}
    	}
    	private void setStateBasedOnIndex(Vector<String>groupAssignments,Vector<String>groupNames, int cond,Vector<String>condAssignments){
    		Object[] optionst = { "Continue", "Cancel" };
    		if (JOptionPane.showOptionDialog(null, 
					"The saved file was saved using a different sample annotation or has duplicate annotation. \n Would you like MeV to try to load it by index order?", 
					"File type warning", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, 
					optionst, optionst[0])==1)
				return;

			String condName;
			int condIndex = 0;
    		for(int sample = 0; sample < exptNames.size(); sample++) {
    			if (cond==2){
    				condName = (String)(condAssignments.get(sample));
    				condIndex = groupNames.indexOf(condName);
				}
    			try{
                	if (getExperimentalDesign()==4){
                		mPanel.FactorAESP.assignmentRBs[groupNames.indexOf(groupAssignments.get(sample))][sample].setSelected(true);
                		mPanel.FactorBESP.assignmentRBs[condIndex][sample].setSelected(true);
                	} else if (getExperimentalDesign()==5){
                		mPanel.TimePointESP.assignmentRBs[groupNames.indexOf(groupAssignments.get(sample))][sample].setSelected(true);
                		mPanel.ConditionESP.assignmentRBs[condIndex][sample].setSelected(true);
                	} else {
                		mPanel.sampleSelectionPanel.assignmentRBs[groupNames.indexOf(groupAssignments.get(sample))][sample].setSelected(true);
                	}
//    				exptTimeRadioButtons[groupNames.indexOf(groupAssignments.get(sample))][sample].setSelected(true);
//    				if (cond==2&&(ngPanel.getExperimentDesign()==2)){
//        				if(condIndex == 1)
//        					exptConditionRadioButtons[condIndex][sample].setSelected(true);
//        				else
//        					exptConditionRadioButtons[0][sample].setSelected(true);
//    				}
    			}catch(Exception e){
                	if (getExperimentalDesign()==4){
                		mPanel.FactorAESP.notInTimeGroupRadioButtons[sample].setSelected(true);
                		mPanel.FactorBESP.notInTimeGroupRadioButtons[sample].setSelected(true);
                	} else if (getExperimentalDesign()==5){
                		mPanel.TimePointESP.notInTimeGroupRadioButtons[sample].setSelected(true);
                		mPanel.ConditionESP.notInTimeGroupRadioButtons[sample].setSelected(true);
                	} else {
                		mPanel.sampleSelectionPanel.notInTimeGroupRadioButtons[sample].setSelected(true);
                	}
//					notInTimeGroupRadioButtons[sample].setSelected(true);  //set to last state... excluded
    			}
    		}
    	}
        private JPanel createSaveLoadPanel(){

            JPanel panel2 = new JPanel();
            GridBagLayout gridbag3 = new GridBagLayout();
            panel2.setLayout(gridbag3);
            panel2.setBackground(Color.white);
            JButton saveButton = new JButton("  Save settings  ");
            saveButton.setFocusPainted(false);
            saveButton.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED, Color.lightGray, Color.darkGray));
            JButton loadButton = new JButton("  Load settings  ");
            loadButton.setFocusPainted(false);
            loadButton.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED, Color.lightGray, Color.darkGray));
            JButton resetButton = new JButton("  Reset  ");
            resetButton.setFocusPainted(false);
            resetButton.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED, Color.lightGray, Color.darkGray));
            
            
            final int finNum = exptNames.size();
            
            resetButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    for (int i = 0; i < finNum; i++) {
                    	if (getExperimentalDesign()==4){
                    		mPanel.FactorAESP.notInTimeGroupRadioButtons[i].setSelected(true);
                    		mPanel.FactorBESP.notInTimeGroupRadioButtons[i].setSelected(true);
                    	} else if (getExperimentalDesign()==5){
                    		mPanel.TimePointESP.notInTimeGroupRadioButtons[i].setSelected(true);
                    		mPanel.ConditionESP.notInTimeGroupRadioButtons[i].setSelected(true);
                    	} else {
                    		mPanel.sampleSelectionPanel.notInTimeGroupRadioButtons[i].setSelected(true);
                    	}
                    }
                }
            });
            
            final JFileChooser fc = new JFileChooser();
            fc.setCurrentDirectory(new File("Data"));
            
            saveButton.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent evt) {
                	saveAssignments();
                }
            });
            
            
            //NEED TO REWORK THIS FOR MULTICLASS
            
            loadButton.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent evt) {
                	loadAssignments();
                	
                }
            });
            
            
            
            constraints.anchor = GridBagConstraints.CENTER;
            constraints.fill = GridBagConstraints.NONE;
            constraints.insets = new Insets(5,5,5,5);
            buildConstraints(constraints, 0, 0, 1, 1, 33, 100);
            gridbag3.setConstraints(saveButton, constraints);
            panel2.add(saveButton);
            
            buildConstraints(constraints, 1, 0, 1, 1, 33, 0);
            gridbag3.setConstraints(loadButton, constraints);
            panel2.add(loadButton);
            
            buildConstraints(constraints, 2, 0, 1, 1, 34, 0);
            gridbag3.setConstraints(resetButton, constraints);
            panel2.add(resetButton);
            constraints.insets = new Insets(0,0,0,0);
            buildConstraints(constraints, 0, 2, 1, 1, 0, 5);
            constraints.anchor = GridBagConstraints.CENTER;
//            gridbag2.setConstraints(panel2, constraints);
        	return panel2;
        }
        protected void reset(){
        	if (ngPanel.okPressed){
        		sampleSelectionPanel.reset();
        	}
        }
    }
    
  
    public class RadioButtonListener implements ActionListener{
        public void actionPerformed(ActionEvent ae) {
        	if (getExperimentalDesign()==3){
        		mPanel.ngPanel.numGroupsLabel.setText("Number of Groups: ");
        		mPanel.ngPanel.numGroupsField.setVisible(true);
        		mPanel.ngPanel.numGroupsLabel.setVisible(true);
        		mPanel.ngPanel.factorPanel.setVisible(false);
        	}else if (getExperimentalDesign()==4){
        		mPanel.ngPanel.numGroupsField.setVisible(false);
        		mPanel.ngPanel.numGroupsLabel.setVisible(false);
        		mPanel.ngPanel.factorPanel.setVisible(true);
        		
        	}else if (getExperimentalDesign()==5){
        		mPanel.ngPanel.numGroupsLabel.setText("Number of Timepoints: ");
        		mPanel.ngPanel.numGroupsField.setVisible(true);
        		mPanel.ngPanel.numGroupsLabel.setVisible(true);
        		mPanel.ngPanel.factorPanel.setVisible(false);
        		
        	}else{
        		mPanel.ngPanel.numGroupsField.setVisible(false);
        		mPanel.ngPanel.numGroupsLabel.setVisible(false);
        		mPanel.ngPanel.factorPanel.setVisible(false);
        	}
        }
    	
    }
    public class EventListener extends WindowAdapter implements ActionListener{
        
        public void actionPerformed(ActionEvent ae) {
            String command = ae.getActionCommand();
            if(command.equals("ok-command")){
            	if (!okReady)
            		return;
            	if ((getSelectionDesign()==SURVInitBox.CLUSTER_SELECTION)&&(sampleRepository.isEmpty())){
            		JOptionPane.showMessageDialog(null, "Cluster Repository is Empty.", "Error", JOptionPane.WARNING_MESSAGE);
            		return;
            	}
            	//check parameters
            	if (!isParamSufficient())
            		return;
                okPressed = true;
            	dispose();
            } else if (command.equals("reset-command")) {
                mPanel.reset();
            } else if (command.equals("cancel-command")) {
                okPressed = false;
                dispose();
            } else if (command.equals("info-command")){
                HelpWindow hw = new HelpWindow(SURVInitBox.this, "Survival Analysis Initialization Dialog");
                okPressed = false;
                if(hw.getWindowContent()){
                    hw.setSize(450,600);
                    hw.setLocation();
                    hw.setVisible(true);
                    return;
                }
                else {
                    hw.setVisible(false);
                    hw.dispose();
                    return;
                }
            }
        }
    }
    /**
     * Checks to make sure samples have been properly assigned to groups for each experimental design.
     * 
     * @return true, if the group assignment is sufficient.
     * false, if the group assignment is lacking.
     */
    private boolean isParamSufficient(){
    	switch (getExperimentalDesign()){
	    	case 1:{
	    		//TODO 
	    		try {
	    			Double test = new Double(mPanel.lambdaField.getText());
	    		} catch (Exception e) {
	    			JOptionPane.showMessageDialog(null, "Please enter a number into the field '" + mPanel.lambdaLabel.getText() + "'.", "Error", JOptionPane.WARNING_MESSAGE);
	    			return false;
	    		}
	    		if(mPanel.ngPanel.validEventData && 
	    			mPanel.ngPanel.validCensoringData) {
	    		} else {
	    			JOptionPane.showMessageDialog(null, "Please select valid event and censoring data.", "Error", JOptionPane.WARNING_MESSAGE);
	        		return false;
	    		}
	    		return true;
	    	}
	    	case 2:{
	    		int[] inc = new int[2];
	    		int[] grpAssign = getGroupAssignments();
	    		for (int i=0; i<grpAssign.length; i++){
	    			if (grpAssign[i]!=0)
	    				inc[grpAssign[i]-1]++;
	    		}
	    		if (inc[0] < 2 || inc[1] < 2){
	    			JOptionPane.showMessageDialog(null, "Please select at least 2 samples for each group.", "Error", JOptionPane.WARNING_MESSAGE);
	        		return false;
	    		}
	    		if(mPanel.ngPanel.validEventData && 
		    			mPanel.ngPanel.validCensoringData) {
		    		} else {
		    			JOptionPane.showMessageDialog(null, "Please select valid event and censoring data.", "Error", JOptionPane.WARNING_MESSAGE);
		        		return false;
	    		}
	    		return true;
	    	}	
    	}
    	return false;
    }
    
    public int[] getGroupAssignments() {
    	if (getExperimentalDesign()<4)
    		return getSimpleGroupAssignments();
    	if (getExperimentalDesign()==4)
    		return getFactorGroupAssignments();
    	if (getExperimentalDesign()==5)
    		return getTimeCourseGroupAssignments();
        return null;
    }  
    
    /** Returns the cluster selected for analysis or null if none selected.
     * @return  */
    public Cluster getSelectedGeneCluster(){
    	if(geneRepository.isEmpty())
    		return null;
    	else 
    		return mPanel.geneClusterBrowser.getSelectedCluster();
    }
    private int[] getClusterSelectorGroupAssignments(){
    	boolean doubleAssigned;
    	int[]groupAssignments = new int[exptNames.size()];
    	ArrayList[] arraylistArray = new ArrayList[mPanel.numGroups];
    	for (int i=0; i<mPanel.numGroups; i++){
    		int j = i+1;
    		arraylistArray[i] = mPanel.groupsCS.getGroupSamples("Class "+j);
    		
    	}
    	for (int i = 0; i < exptNames.size(); i++) {
    		doubleAssigned = false;
    		groupAssignments[i] = 0;
    		for (int j = 0;j<mPanel.numGroups;j++){
	    		if (arraylistArray[j].contains(i)){
	    			if (doubleAssigned){
	    		        Object[] optionst = { "OK" };
	    				JOptionPane.showOptionDialog(null, 
	    						"The clusters you have chosen have overlapping samples. \n Each group must contain unique samples.", 
	    						"Multiple Ownership Error", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, 
	    						optionst, optionst[0]);
	    				return null;

	    			}
	    			groupAssignments[i] = j+1;
	    			doubleAssigned = true;
	    		}
    		}
        }
    	return groupAssignments;
    }
    
    private int[] getSimpleGroupAssignments() {
    	if (getSelectionDesign()==SURVInitBox.CLUSTER_SELECTION)
    		return getClusterSelectorGroupAssignments();
        int[] groupAssignments = new int[exptNames.size()];
        for (int i = 0; i < exptNames.size(); i++) {
            if (mPanel.sampleSelectionPanel.notInTimeGroupRadioButtons[i].isSelected()) {// "NOT IN GROUP" IS STORED AS ZERO, AND GROUP J IS STORED AS THE INTEGER J (I.E., THERE IS NO GROUP 0)
                groupAssignments[i] = 0;
            } else {
                for (int j = 0; j < mPanel.sampleSelectionPanel.assignmentRBs.length; j++) {
                    if (mPanel.sampleSelectionPanel.assignmentRBs[j][i].isSelected()) {
                        groupAssignments[i] = j + 1;
                        break;
                    }
                }
            }
        }
        return groupAssignments;
    }
    private int[] getTimeCourseGroupAssignments() {
    	if (getSelectionDesign()==SURVInitBox.CLUSTER_SELECTION)
    		return getClusterSelectorTimeCourseAssignments();
    	int[]timeCourseGroupAssignments = new int[exptNames.size()];

        for (int i = 0; i < exptNames.size(); i++) {
        	if (mPanel.ConditionESP.notInTimeGroupRadioButtons[i].isSelected()||mPanel.TimePointESP.notInTimeGroupRadioButtons[i].isSelected()){
        		timeCourseGroupAssignments[i]=0;
        		continue;
        	}
        	int a = 0;
        	int b = 0;
            for (int j = 0; j < mPanel.ConditionESP.assignmentRBs.length; j++) {
                if (mPanel.ConditionESP.assignmentRBs[j][i].isSelected()) {
                    a = j;
                    break;
                }
            }
            for (int j = 0; j < mPanel.TimePointESP.assignmentRBs.length; j++) {
                if (mPanel.TimePointESP.assignmentRBs[j][i].isSelected()) {
                    b = j;
                    break;
                }
            }
            timeCourseGroupAssignments[i]=a*getNumGroups()+b+1;
        }
    	return timeCourseGroupAssignments;
    }
    
    private int[] getClusterSelectorTimeCourseAssignments() {
    	boolean doubleAssigned;
    	int[][]groupAssignments = new int[2][exptNames.size()];
    	ArrayList[] arraylistArray = new ArrayList[mPanel.numGroups];
    	for (int i=0; i<mPanel.numGroups; i++){
    		int j = i+1;
    		arraylistArray[i] = mPanel.timepointCS.getGroupSamples("Time-Point "+j);
    	}
    	for (int i = 0; i < exptNames.size(); i++) {
    		doubleAssigned = false;
    		groupAssignments[0][i] = 0;
    		for (int j = 0;j<mPanel.numGroups;j++){
	    		if (arraylistArray[j].contains(i)){
	    			if (doubleAssigned){
	    		        Object[] optionst = { "OK" };
	    				JOptionPane.showOptionDialog(null, 
	    						"The clusters you have chosen have overlapping samples. \n Each group must contain unique samples.", 
	    						"Multiple Ownership Error", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, 
	    						optionst, optionst[0]);
	    				return null;

	    			}
	    			groupAssignments[0][i] = j+1;
	    			doubleAssigned = true;
	    		}
    		}
        }
    	arraylistArray = new ArrayList[2];
    	for (int i=0; i<2; i++){
    		int j = i+1;
    		arraylistArray[i] = mPanel.conditionCS.getGroupSamples("Condition "+j);
    	}
    	for (int i = 0; i < exptNames.size(); i++) {
    		doubleAssigned = false;
    		groupAssignments[1][i] = 0;
    		for (int j = 0;j<2;j++){
	    		if (arraylistArray[j].contains(i)){
	    			if (doubleAssigned){
	    		        Object[] optionst = { "OK" };
	    				JOptionPane.showOptionDialog(null, 
	    						"The clusters you have chosen have overlapping samples. \n Each group must contain unique samples.", 
	    						"Multiple Ownership Error", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, 
	    						optionst, optionst[0]);
	    				return null;

	    			}
	    			groupAssignments[1][i] = j+1;
	    			doubleAssigned = true;
	    		}
    		}
        }
    	int[] groupAssignments2 = new int[exptNames.size()];
    	for (int i=0; i<groupAssignments2.length; i++){
    		if (groupAssignments[0][i]==0||groupAssignments[1][i]==0)
    			groupAssignments2[i]=0;
    		else
    			groupAssignments2[i] = (groupAssignments[1][i]-1)*mPanel.numGroups+(groupAssignments[0][i]-1)+1;
    	}
    	return groupAssignments2;
	}


	private int[] getFactorGroupAssignments() {
    	if (getSelectionDesign()==SURVInitBox.CLUSTER_SELECTION)
    		return getClusterSelectorFactorAssignments();
    	int[]factorGroupAssignments = new int[exptNames.size()];

        for (int i = 0; i < exptNames.size(); i++) {
        	if (mPanel.FactorAESP.notInTimeGroupRadioButtons[i].isSelected()||mPanel.FactorBESP.notInTimeGroupRadioButtons[i].isSelected()){
        		factorGroupAssignments[i]=0;
        		continue;
        	}
        	int a = 0;
        	int b = 0;
            for (int j = 0; j < mPanel.FactorAESP.assignmentRBs.length; j++) {
                if (mPanel.FactorAESP.assignmentRBs[j][i].isSelected()) {
                    a = j;
                    break;
                }
            }
            for (int j = 0; j < mPanel.FactorBESP.assignmentRBs.length; j++) {
                if (mPanel.FactorBESP.assignmentRBs[j][i].isSelected()) {
                    b = j;
                    break;
                }
            }
            factorGroupAssignments[i]=a*this.getNumFactorBGroups()+b+1;
        }
    	return factorGroupAssignments;
    }
    private int[] getClusterSelectorFactorAssignments() {
    	boolean doubleAssigned;
    	int[][]groupAssignments = new int[2][exptNames.size()];
    	ArrayList[] arraylistArray = new ArrayList[getNumFactorBGroups()];
    	for (int i=0; i<getNumFactorBGroups(); i++){
    		int j = i+1;
    		arraylistArray[i] = mPanel.factorACS.getGroupSamples("Level "+j);
    	}
    	for (int i = 0; i < exptNames.size(); i++) {
    		doubleAssigned = false;
    		groupAssignments[0][i] = 0;
    		for (int j = 0;j<getNumFactorBGroups();j++){
	    		if (arraylistArray[j].contains(i)){
	    			if (doubleAssigned){
	    		        Object[] optionst = { "OK" };
	    				JOptionPane.showOptionDialog(null, 
	    						"The clusters you have chosen have overlapping samples. \n Each group must contain unique samples.", 
	    						"Multiple Ownership Error", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, 
	    						optionst, optionst[0]);
	    				return null;

	    			}
	    			groupAssignments[0][i] = j+1;
	    			doubleAssigned = true;
	    		}
    		}
        }
    	arraylistArray = new ArrayList[2];
    	for (int i=0; i<2; i++){
    		int j = i+1;
    		arraylistArray[i] = mPanel.factorBCS.getGroupSamples("Level "+j);
    	}
    	for (int i = 0; i < exptNames.size(); i++) {
    		doubleAssigned = false;
    		groupAssignments[1][i] = 0;
    		for (int j = 0;j<2;j++){
	    		if (arraylistArray[j].contains(i)){
	    			if (doubleAssigned){
	    		        Object[] optionst = { "OK" };
	    				JOptionPane.showOptionDialog(null, 
	    						"The clusters you have chosen have overlapping samples. \n Each group must contain unique samples.", 
	    						"Multiple Ownership Error", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, 
	    						optionst, optionst[0]);
	    				return null;

	    			}
	    			groupAssignments[1][i] = j+1;
	    			doubleAssigned = true;
	    		}
    		}
        }
    	int[] groupAssignments2 = new int[exptNames.size()];
    	for (int i=0; i<groupAssignments2.length; i++){
    		if (groupAssignments[0][i]==0||groupAssignments[1][i]==0)
    			groupAssignments2[i]=0;
    		else
    			groupAssignments2[i] = (groupAssignments[0][i]-1)*this.getNumFactorBGroups()+(groupAssignments[1][i]-1)+1;
    	}
    	return groupAssignments2;
	}


	public int[][] getGroupMatrix(){
    	int[] timeAssignments;
//    	if (getSelectionDesign()==LIMMAInitBox.CLUSTER_SELECTION){
//    		timeAssignments = getClusterGroupAssignments();
//    	}else{
    		timeAssignments = getGroupAssignments();
//    	}
    	int[] numEachTime = new int[getNumGroups()];
    	for (int i=0; i< timeAssignments.length; i++){
    		if (timeAssignments[i]!=0)
    			numEachTime[timeAssignments[i]-1]++;
    	}
    	int[][]timeMatrix=new int[getNumGroups()][];
    	for (int i=0; i<getNumGroups(); i++){
    		timeMatrix[i]=new int[numEachTime[i]];
    	}
    	int[]nextEntry=new int[getNumGroups()];
    	for (int i=0; i< timeAssignments.length; i++){
    		if (timeAssignments[i]!=0){
	    		timeMatrix[timeAssignments[i]-1][nextEntry[timeAssignments[i]-1]] = i;
	    		nextEntry[timeAssignments[i]-1]++;
    		}
    	}
    	return timeMatrix;
    }
	public String getCensoredField() {
		return mPanel.ngPanel.censored.getSelectedItem().toString();
	}
	public String getSurvivalField() {
		return mPanel.ngPanel.survival.getSelectedItem().toString();
	}
    /**
     * 
     * @return
     * 
     */
    public int getExperimentalDesign() {
    	int dataType = -1;
    	if (mPanel.ngPanel.oneClass.isSelected())
    		dataType = 1;
    	if (mPanel.ngPanel.twoClass.isSelected())
    		dataType = 2;
    	return dataType;
    }
    
    public int getSelectionDesign() {
        int design = -1;
        if(mPanel.ngPanel.oneClass.isSelected())
        	design = SURVInitBox.COX_MODEL;
        else if (mPanel.tabbedmulg.getSelectedIndex() == 0) {
        	design = SURVInitBox.BUTTON_SELECTION;
        } else {
        	design = SURVInitBox.CLUSTER_SELECTION;
        }
        return design;
    }

//    public int[] getClusterGroupAssignments(){
//    	boolean doubleAssigned;
//    	int[]groupAssignments = new int[exptNames.size()];
//    	ArrayList[] arraylistArray = new ArrayList[mPanel.numGroups];
//    	for (int i=0; i<mPanel.numGroups; i++){
//    		int j = i+1;
//    		arraylistArray[i] = mPanel.factorBCS.getGroupSamples("Time "+j);
//    		
//    	}
//    	for (int i = 0; i < exptNames.size(); i++) {
//    		doubleAssigned = false;
//    		groupAssignments[i] = 0;
//    		for (int j = 0;j<mPanel.numGroups;j++){
//	    		if (arraylistArray[j].contains(i)){
//	    			if (doubleAssigned){
//	    		        Object[] optionst = { "OK" };
//	    				JOptionPane.showOptionDialog(null, 
//	    						"The clusters you have chosen have overlapping samples. \n Each group must contain unique samples.", 
//	    						"Multiple Ownership Error", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, 
//	    						optionst, optionst[0]);
//	    				return null;
//
//	    			}
//	    			
//	    			groupAssignments[i] = j+1;
//	    			doubleAssigned = true;
//	    		}
//    		}
//        }
//    	return groupAssignments;
//    }
    
    public int getNumGroups() {
    	if (getExperimentalDesign()==4)
    		return getNumFactorAGroups()*getNumFactorBGroups();
        return mPanel.numGroups;
    }
    public int getNumFactorAGroups() {
        return mPanel.factorAlevels;
    }
    public int getNumFactorBGroups() {
        return mPanel.factorBlevels;
    }
    public String getFactorAName() {
        return mPanel.factorAName;
    }
    public String getFactorBName() {
        return mPanel.factorBName;
    }
    public float getAlpha() {
    	return Float.parseFloat(mPanel.ngPanel.alphaField.getText());
    }
    
    
    public static void main(String[] args) {
        JFrame dummyFrame = new JFrame();
        Vector<String> dummyVect = new Vector<String>();
       	//100 genes by 24 samples
       	float[][] A = new float[100][24];
       	int[] indices = new int[24];
        for (int i = 0; i < 24; i++) {
            dummyVect.add("Expt " + i);
            for(int j=0; j<100; j++) {
            	A[j][i] = new Float(Math.random());
            }
            indices[i] = i;
        }
        Vector<String> dummySampleAnns = new Vector<String>();
       	dummySampleAnns.add("event");
       	dummySampleAnns.add("status");
       	
       	MultipleArrayData data = new MultipleArrayData();
       	ArrayList<ISlideData> features = new ArrayList<ISlideData>();
       	data.setFeaturesList(features);
       	
//       	
        SURVInitBox oBox = new SURVInitBox(dummyFrame, true, dummyVect, new ClusterRepository(0), new ClusterRepository(0), data, dummySampleAnns);
        oBox.setVisible(true);
        System.exit(0);
    }
}
