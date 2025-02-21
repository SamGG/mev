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

package org.tigr.microarray.mev.cluster.gui.impl.betr;

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
import java.io.FileOutputStream;
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
import javax.swing.JSeparator;

import org.tigr.microarray.mev.TMEV;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.HCLSigOnlyPanel;
import org.tigr.microarray.mev.cluster.gui.helpers.ClusterSelector;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.HelpWindow;
import org.tigr.microarray.mev.cluster.clusterUtil.ClusterRepository;

/**
 *
 * @author  dschlauch
 * @version 
 */
public class BETRInitBox extends AlgorithmDialog {

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
    
    boolean okPressed = false;
    boolean okReady = false;
    boolean step2 = false;
    Vector<String> exptNames;    
    MultiClassPanel mPanel;
    JTabbedPane selectionPanel;
    PermOrFDistPanel permPanel;
    PValuePanel pPanel;
    //HCLSelectionPanel hclOpsPanel;    
    HCLoptionPanel hclOpsPanel;
    ClusterRepository repository;
    JButton step2Button = new JButton("Continue...");
    
    /** Creates new BETRInitBox */
    public BETRInitBox(JFrame parentFrame, boolean modality, Vector<String> exptNames, ClusterRepository repository) {
        super(parentFrame, "BETR Initialization", modality);
        this.exptNames = exptNames;  
        this.repository = repository;
        
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


        
        JTabbedPane consolidatedPane = new JTabbedPane();
        permPanel = new PermOrFDistPanel();
        consolidatedPane.add("Permutations of F-Distribution", permPanel);        

        pPanel = new PValuePanel();
        consolidatedPane.add("P-Value/False Discovery Parameters", pPanel);
        
        buildConstraints(constraints, 0, 0, 1, 1, 100, 80);
        gridbag.setConstraints(mPanel, constraints);
        pane.add(mPanel);   
        
        addContent(pane);
        EventListener listener = new EventListener();
        setActionListeners(listener);
        this.addWindowListener(listener);  
        //pack();
    }

    
    public void setVisible(boolean visible) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);
        
        super.setVisible(visible);
        
        if (visible) {
            //bPanel.okButton.requestFocus(); //UNCOMMMENT THIS LATER
        }
    }
    
    void buildConstraints(GridBagConstraints gbc, int gx, int gy,
    int gw, int gh, int wx, int wy) {
        
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

            constraints.anchor = GridBagConstraints.WEST;
            buildConstraints(constraints, 0, 0, 1, 1, 25, 10);
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
    class MultiClassPanel extends JPanel {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		NumTimePointsPanel ngPanel;
        GridBagConstraints constraints;
        GridBagLayout gridbag;
        JPanel dummyPanel;
        MultiGroupExperimentsPanel mulgPanel;
        JTabbedPane tabbedmulg;
        ClusterSelector clusterSelectorCondition;
        ClusterSelector clusterSelectorTime;
        JLabel infoLabel;
        JLabel infoLabel2;
        int numTimePoints;
        float alpha;
        //Vector exptNames;
        
        public MultiClassPanel(/*Vector exptNames*/) {
            constraints = new GridBagConstraints();
            gridbag = new GridBagLayout();
            this.setBackground(Color.white);
            //this.exptNames = exptNames;
            this.setLayout(gridbag);
            ngPanel = new NumTimePointsPanel();

            buildConstraints(constraints, 0, 0, 1, 1, 100, 10);
            constraints.fill = GridBagConstraints.BOTH;
            gridbag.setConstraints(ngPanel, constraints);
            
            step2Button.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent evt) {
                	if (step2){
                		infoLabel.setVisible(true);
                        infoLabel2.setVisible(true);
                        ngPanel.numTimePointsField.setEnabled(true);
                        ngPanel.alphaField.setEnabled(true);
                        ngPanel.oneCondition.setEnabled(true);
                        ngPanel.twoConditions.setEnabled(true);
                        ngPanel.pairedData.setEnabled(true);
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
                	else{
	                    ngPanel.okPressed = true;
	                    okReady = true;
	                    try {
	                    	alpha = Float.parseFloat(ngPanel.alphaField.getText());
	                        numTimePoints = Integer.parseInt(ngPanel.numTimePointsField.getText());
	                        if (alpha>1||alpha<0){
	                        	JOptionPane.showMessageDialog(null, "alpha must be between 0 and 1!", "Error", JOptionPane.ERROR_MESSAGE);
	                            
	                        }else{
		                        if (numTimePoints < 2) {
		                            JOptionPane.showMessageDialog(null, "Number of time points must be greater than 1!", "Error", JOptionPane.ERROR_MESSAGE);
		                        } else {
		                            mulgPanel = new MultiGroupExperimentsPanel(exptNames, numTimePoints, ngPanel.getExperimentDesign());
		                            
		                            
		                            //JButton dummyButton  = new JButton("dummyButton");
		                            //dummyButton.setVisible(true);
		                            MultiClassPanel.this.remove(dummyPanel);
		                            tabbedmulg = new JTabbedPane();
		                            
		                            clusterSelectorCondition= new ClusterSelector(repository,2, "Condition");
		                            clusterSelectorTime= new ClusterSelector(repository,numTimePoints, "Time");
		                            if (repository!=null){
		                            	clusterSelectorCondition.setClusterType("Condition");
		                            	clusterSelectorTime.setClusterType("Time Points");
		                    		}
		                            JPanel clusterSelectorPanel = new JPanel();
		                            clusterSelectorPanel.setLayout(new GridBagLayout());
		                            String andconditions = "";
		                            if (ngPanel.getExperimentDesign()==2)
		                            	andconditions = " and Conditions";
		                            JLabel clusterInstructions = new JLabel("Use the drop-down menus to assign clusters of samples to their corresponding Time-Points"+andconditions+".");
		                            GridBagConstraints c = new GridBagConstraints();
		                            c.gridwidth=2;
		                            clusterSelectorPanel.add(clusterInstructions, c);
		                            
		                            c.fill = GridBagConstraints.BOTH;
		                            c.weighty =1;
		                            c.weightx = 1;
		                            c.gridx = 0;
		                            c.gridy = 1;
		                            c.gridwidth = 1;
		                            c.anchor = GridBagConstraints.PAGE_END;
		                            if (ngPanel.getExperimentDesign()==2)
		                            	clusterSelectorPanel.add(clusterSelectorCondition, c);
		                            c.gridx = 1;
		                            clusterSelectorPanel.add(clusterSelectorTime, c);
		                            
		                            
		                            
		                            
		                            tabbedmulg.add("Button Selection", mulgPanel);
		                            tabbedmulg.add("Cluster Selection", clusterSelectorPanel);
		                            tabbedmulg.setSelectedIndex(1);
		                            if (repository==null||repository.isEmpty())
		                            	tabbedmulg.setSelectedIndex(0);
		                            buildConstraints(constraints, 0, 1, 2, 1, 0, 90);
		                            constraints.fill = GridBagConstraints.BOTH;
		                            gridbag.setConstraints(tabbedmulg, constraints);
		                            MultiClassPanel.this.add(tabbedmulg);
		                            //MultiClassPanel.this.add(dummyButton);
		                            MultiClassPanel.this.validate();
		                            //step2Button.setEnabled(false);
		                            enableOK();
		                            ngPanel.numTimePointsField.setEnabled(false);
		                            ngPanel.alphaField.setEnabled(false);
		                            ngPanel.oneCondition.setEnabled(false);
		                            ngPanel.twoConditions.setEnabled(false);
		                            ngPanel.pairedData.setEnabled(false);
		                            //step2Button.setVisible(false);
		                            step2Button.setText("<<< Go Back");
		                            infoLabel.setVisible(false);
		                            infoLabel2.setVisible(false);
		                            step2 = true;
		                            
		                            
		                        }
	                        }
	                        //MultiClassPanel.this.repaint();
	                        //dispose();
	                    } catch (NumberFormatException nfe) {
	                        JOptionPane.showMessageDialog(null, "Please enter a value greater than 0 and less than 1!", "Error", JOptionPane.ERROR_MESSAGE);
	                    }
                	}
                }
            });
            
            JPanel topPanel =  new JPanel();
            topPanel.setBackground(Color.white);
            topPanel.setLayout(gridbag);
            buildConstraints(constraints, 0, 0, 1, 2, 75, 100);
            gridbag.setConstraints(ngPanel, constraints);
            topPanel.add(ngPanel);

            hclOpsPanel = new HCLoptionPanel();
            hclOpsPanel.setBorder(null);//.setBorder( BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, Color.black));
            buildConstraints(constraints, 1, 0, 1, 1, 25, 100);
            gridbag.setConstraints(hclOpsPanel, constraints);
            topPanel.add(hclOpsPanel);
            
            buildConstraints(constraints, 1, 1, 1, 1, 0, 10);
            //constraints.insets = new Insets(100,100,100,100);
            //constraints.fill = GridBagConstraints.NONE;
            gridbag.setConstraints(step2Button, constraints);
            topPanel.add(step2Button);

            topPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "BETR Parameters",TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, Color.black));
            buildConstraints(constraints, 0, 0,1,1,100,10);
            gridbag.setConstraints(topPanel, constraints);
            this.add(topPanel);
            
            infoLabel = new JLabel("Sample Group Assignment");
            constraints.fill = GridBagConstraints.NONE;
            constraints.anchor = GridBagConstraints.CENTER;
            infoLabel.setMaximumSize(new Dimension(50,50));
            Font font = infoLabel.getFont();
            infoLabel.setFont(font.deriveFont(20.0f));
            buildConstraints(constraints, 0, 1, 1, 1, 0, 5);
            gridbag.setConstraints(infoLabel, constraints);
            //infoLabel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "",TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, Color.black));
            
            this.add(infoLabel, constraints);
            infoLabel2 = new JLabel("Please select the type of BETR analysis to be run, then click 'Continue'.");
            constraints.anchor = GridBagConstraints.CENTER;
            buildConstraints(constraints, 0, 2, 1, 1, 100, 5);
            gridbag.setConstraints(infoLabel2, constraints);
            //infoLabel2.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "",TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, Color.black));
            
            this.add(infoLabel2, constraints);
            
            
            buildConstraints(constraints, 0, 3, 1, 1, 100, 90);
            dummyPanel = new JPanel();
            dummyPanel.setBackground(Color.white);
            
            gridbag.setConstraints(dummyPanel, constraints);
            this.add(dummyPanel);
        }
        
        class NumTimePointsPanel extends JPanel {
            JTextField numTimePointsField;
            JTextField alphaField;
            //JButton okButton;
            boolean okPressed = false;

            JRadioButton oneCondition;
            JRadioButton twoConditions;
            JRadioButton pairedData;
            public NumTimePointsPanel() {
                setBackground(Color.white);
                GridBagLayout gridbag = new GridBagLayout();
                GridBagConstraints constraints = new GridBagConstraints();
                
                //JPanel pane = new JPanel();
                this.setLayout(gridbag);
                
                JLabel dataTypeLabel = new JLabel("Type of data to run:   ");
                buildConstraints(constraints, 0, 0, 1, 1, 30, 100);
                constraints.anchor = GridBagConstraints.EAST;
                gridbag.setConstraints(dataTypeLabel, constraints);
                this.add(dataTypeLabel);
                
                twoConditions=new JRadioButton("2 Conditions", true);
                oneCondition=new JRadioButton("1 Condition", false);
                pairedData=new JRadioButton("Paired", false);
                oneCondition.setBackground(Color.white);
                oneCondition.setBorder(null);
                twoConditions.setBackground(Color.white);
                twoConditions.setBorder(null);
                pairedData.setBackground(Color.white);
                pairedData.setBorder(null);
                ButtonGroup dataType = new ButtonGroup();
                dataType.add(oneCondition);
                dataType.add(twoConditions);
                dataType.add(pairedData);
                buildConstraints(constraints, 1, 0, 1, 1, 30, 100);
                constraints.anchor = GridBagConstraints.WEST;
                gridbag.setConstraints(twoConditions, constraints);
                this.add(twoConditions);
                buildConstraints(constraints, 1, 1, 1, 1, 30, 100);
                constraints.anchor = GridBagConstraints.WEST;
                gridbag.setConstraints(oneCondition, constraints);
                this.add(oneCondition);
                buildConstraints(constraints, 1, 2, 1, 1, 30, 100);
                constraints.anchor = GridBagConstraints.WEST;
                gridbag.setConstraints(pairedData, constraints);
                this.add(pairedData);
                
                JLabel numTimePointsLabel = new JLabel("Number of time-points: ");
                buildConstraints(constraints, 0, 3, 1, 1, 30, 100);
                constraints.anchor = GridBagConstraints.EAST;
                gridbag.setConstraints(numTimePointsLabel, constraints);
                this.add(numTimePointsLabel);
                
                numTimePointsField = new JTextField("4", 7);
                numTimePointsField.setMinimumSize(new Dimension(50,20));
                constraints.anchor = GridBagConstraints.WEST;
                buildConstraints(constraints, 1, 3, 1, 1, 30, 0);
                gridbag.setConstraints(numTimePointsField, constraints);
                this.add(numTimePointsField);
                
                JLabel alphaLabel = new JLabel("Significance level: alpha = ");
                buildConstraints(constraints, 0, 4, 1, 1, 30, 100);
                constraints.anchor = GridBagConstraints.EAST;
                gridbag.setConstraints(alphaLabel, constraints);
                this.add(alphaLabel);
                
                alphaField = new JTextField(".05", 7);
                alphaField.setMinimumSize(new Dimension(50,20));
                constraints.anchor = GridBagConstraints.WEST;
                buildConstraints(constraints, 1, 4, 1, 1, 30, 0);
                gridbag.setConstraints(alphaField, constraints);
                this.add(alphaField);
                
                //step2Button = new JButton("OK");
                //buildConstraints(constraints, 2, 4, 2, 1, 40, 0);
                //gridbag.setConstraints(step2Button, constraints);
 
                //this.add(okButton);
                
            }
            
            public int getExperimentDesign(){
            	if (oneCondition.isSelected())
            		return 1;
            	if (twoConditions.isSelected())
            		return 2;
            	if (pairedData.isSelected())
            		return 3;
            	return 0;
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
        class MultiGroupExperimentsPanel extends JPanel {
            int numPanels = 0;
            JLabel[] expLabels;
            JRadioButton[][] exptTimeRadioButtons;
            JRadioButton[] notInTimeGroupRadioButtons;
            JRadioButton[][] exptConditionRadioButtons;
            MultiGroupExperimentsPanel(Vector<String> exptNames, int numTimePoints, int conditions) {
                this.setBorder(new TitledBorder(new EtchedBorder(), "Time/Condition Assignments", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), Color.black));
                setBackground(Color.white);

               // JPanel panel1 = new JPanel();
                expLabels = new JLabel[exptNames.size()];
                exptTimeRadioButtons = new JRadioButton[numTimePoints][exptNames.size()];
                if (conditions==2)
                    exptConditionRadioButtons = new JRadioButton[2][exptNames.size()];
                	
                numPanels = exptNames.size()/512 + 1;
                
                //groupARadioButtons = new JRadioButton[exptNames.size()];
                //groupBRadioButtons = new JRadioButton[exptNames.size()];
                notInTimeGroupRadioButtons = new JRadioButton[exptNames.size()];
                ButtonGroup chooseTime[] = new ButtonGroup[exptNames.size()];
                ButtonGroup chooseCondition[] = new ButtonGroup[exptNames.size()];
                
                GridBagLayout gridbag = new GridBagLayout();
                GridBagLayout gridbag2 = new GridBagLayout();
                GridBagConstraints constraints = new GridBagConstraints();
                this.setLayout(gridbag2);
//                panel1.setLayout(gridbag);
  

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
                    for (int j = 0; j < numTimePoints; j++) {
                        exptTimeRadioButtons[j][i] = new JRadioButton("Time " + (j) + "     ", false);
                        chooseTime[i].add(exptTimeRadioButtons[j][i]);
                    }
                    
                    
                    //set current panel
                    currPanel = i / 512;
                    
                    notInTimeGroupRadioButtons[i] = new JRadioButton("Unassigned", true);
                    chooseTime[i].add(notInTimeGroupRadioButtons[i]);
                    int twoCondRoom = 0;
                    if (conditions==2){
                    	exptConditionRadioButtons[0][i]=new JRadioButton("Condition 1",true);
	                    chooseCondition[i].add(exptConditionRadioButtons[0][i]);
	                    exptConditionRadioButtons[1][i]=new JRadioButton("Condition 2           |  ",true);
	                    chooseCondition[i].add(exptConditionRadioButtons[1][i]);
	                    	
                    	twoCondRoom = 3;
	                    buildConstraints(constraints, 0, i%512, 1, 1, 100, 100);
	                    gridbag.setConstraints(exptConditionRadioButtons[0][i], constraints);
	                    panels[currPanel].add(exptConditionRadioButtons[0][i]);
	                    buildConstraints(constraints, 1, i%512, 1, 1, 100, 100);
	                    gridbag.setConstraints(exptConditionRadioButtons[1][i], constraints);
	                    panels[currPanel].add(exptConditionRadioButtons[1][i]);
                    
	                    JSeparator sep = new JSeparator(JSeparator.VERTICAL);
	                    sep.setSize(22, 22);
	                    buildConstraints(constraints, 2, i%512, 1, 1, 100, 100);
	                    gridbag.setConstraints(sep, constraints);
	                    
	                    //panels[currPanel].add(sep);
	                    panels[currPanel].setBorder(BorderFactory.createEmptyBorder(
	                    		2, //top
	                    		0,     //left
	                    		2, //bottom
	                    		0));   //right
	                    panels[currPanel].add(sep);
                    }
                    
                    for (int j = 0; j < numTimePoints; j++) {
                        buildConstraints(constraints, j+twoCondRoom, i%512, 1, 1, 100, 100);
                        //constraints.fill = GridBagConstraints.BOTH;
                        gridbag.setConstraints(exptTimeRadioButtons[j][i], constraints);
                        panels[currPanel].add(exptTimeRadioButtons[j][i]);
                        // panel1.add(exptGroupRadioButtons[j][i]);
                    }
                    
                    buildConstraints(constraints, (numTimePoints + 1+twoCondRoom), i%512, 1, 1, 100, 100);
                    //constraints.fill = GridBagConstraints.BOTH;
                    gridbag.setConstraints(notInTimeGroupRadioButtons[i], constraints);
                    
                    
                    //panel1.add(notInGroupRadioButtons[i]);
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
                //exptNameHeaderPanel.HEIGHT = panel1.getHeight();
                for(int i = 0; i < exptNameHeaderPanels.length; i++) {
                    exptNameHeaderPanels[i] = new JPanel();
                    exptNameHeaderPanels[i].setSize(50, panels[i].getPreferredSize().height);
                    exptNameHeaderPanels[i].setPreferredSize(new Dimension(maxLabelWidth + 10, panels[i].getPreferredSize().height));
                    exptNameHeaderPanels[i].setLayout(exptHeaderGridbag);
                }
                //scroll.getRowHeader().setLayout(exptHeaderGridbag);
                
                //need to possibly add to additional panels if number of exp. excedes 512
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
                
                buildConstraints(constraints, 0, 0, 1, 1, 100, 90);
                constraints.fill = GridBagConstraints.BOTH;
                gridbag2.setConstraints(scroll, constraints);
                this.add(scroll);
                
                JLabel label1 = new JLabel("Note: Each time-point MUST each contain more than one sample.");
                if (ngPanel.getExperimentDesign()==2){
                	label1 = new JLabel ("Note: Each time-point MUST each contain more than one sample for both conditions.");
                }
                label1.setHorizontalAlignment(JLabel.CENTER);
                buildConstraints(constraints, 0, 1, 1, 1, 0, 5);
                constraints.anchor = GridBagConstraints.EAST;
                //constraints.fill = GridBagConstraints.BOTH;
                gridbag2.setConstraints(label1, constraints);
                this.add(label1);
                
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
                        	notInTimeGroupRadioButtons[i].setSelected(true);
                        	if (ngPanel.getExperimentDesign()==2){
                        		exptConditionRadioButtons[0][i].setSelected(true);
                        	}
                        }
                    }
                });
                
                final JFileChooser fc = new JFileChooser(TMEV.getDataPath());
                
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
                //constraints.fill = GridBagConstraints.BOTH;
                gridbag2.setConstraints(panel2, constraints);
                this.add(panel2);
                
          
            }
            
            /**
             *  resets all group assignments
             */
            protected void reset(){
                for (int i = 0; i < exptNames.size(); i++) {
                	notInTimeGroupRadioButtons[i].setSelected(true);
                	exptConditionRadioButtons[0][i].setSelected(true);
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
        				pw.println("BETR");
        				pw.print("Conditions:\t");
        				pw.println(ngPanel.getExperimentDesign());
        				for (int i=0; i<numTimePoints; i++){
            				pw.print("Group "+(i+1)+" Label:\t");
        					pw.println("Time "+i);
        				}
        								
        				pw.println("#");
        				
        				pw.println("Sample Index\tSample Name\tGroup Assignment");

        				int[] timeAssgn=getTimeAssignments();
        				
        				if (ngPanel.getExperimentDesign()!=2){
	        				for(int sample = 0; sample < exptNames.size(); sample++) {
	        					pw.print(String.valueOf(sample+1)+"\t"); //sample index
	        					pw.print(exptNames.get(sample)+"\t");
	        					if (timeAssgn[sample]!=0)
	        						pw.println("Time "+(timeAssgn[sample]-1));
	        					else
	        						pw.println("Exclude");
	        					
	        				}
	        			}else{
	        				int[] condAssgn=getConditionAssignments();
	        				for(int sample = 0; sample < exptNames.size(); sample++) {
	        					pw.print(String.valueOf(sample+1)+"\t"); //sample index
	        					pw.print(exptNames.get(sample)+"\t");
	        					if (timeAssgn[sample]!=0){
	        						pw.println("Time "+(timeAssgn[sample]-1)+"\tCondition "+condAssgn[sample]);
	        					}
	        					else
	        						pw.println("Exclude\tExclude");
	        					
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
	        			Vector<String> condNames = new Vector<String>();
	        			
	        			
	        			Vector<Integer> sampleIndices = new Vector<Integer>();
	        			Vector<String> sampleNames = new Vector<String>();
	        			Vector<String> groupAssignments = new Vector<String>();		
	        			Vector<String> condAssignments = new Vector<String>();		
	        			
	        			//parse the data in to these structures
	        			String [] lineArray;
	        			int cond=0;
	        			//String status = "OK";
	        			for(int row = 0; row < data.size(); row++) {
	        				line = (String)(data.get(row));
	
	        				//if not a comment line, and not the header line
	        				if(!(line.startsWith("#")) && !(line.startsWith("SampleIndex"))) {
	        					
	        					lineArray = line.split("\t");
	        					
	        					//check what module saved the file
	        					if(lineArray[0].startsWith("Module:")) {
	        						if (!lineArray[1].equals("BETR")){
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
	        					if(lineArray[0].startsWith("Cond")) {
	        						try {
	        							cond=Integer.parseInt(lineArray[1]);
		        					} catch ( NumberFormatException nfe) {
		        						//if not parsable continue
		        						continue;
		        					}
	        						continue;
	        					}
	        					if (cond==2){
	        						condNames.add("Condition 1");
	        						condNames.add("Condition 2");
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
	        					if (cond==2)
	        						condAssignments.add(lineArray[3]);
	        				}				
	        			}
	        			
	        			//we have the data parsed, now validate, assign current data
	
	
	        			if( exptNames.size() != sampleNames.size()) {
	        				System.out.println(exptNames.size()+"  "+sampleNames.size());
	        				//status = "number-of-samples-mismatch";
	        				System.out.println(exptNames.size()+ " s length " + sampleNames.size());
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
	        					setStateBasedOnIndex(groupAssignments,groupNames, cond,condAssignments, condNames);
	        					break;
	        				}
	        				
	        				
	        				groupName = (String)(groupAssignments.get(fileSampleIndex));
	        				groupIndex = groupNames.indexOf(groupName);
	        				
	        				if (cond==2){
		        				condName = (String)(condAssignments.get(fileSampleIndex));
		        				condIndex = condNames.indexOf(condName);
	        				}
	        				
	        				//set state
	        				try{
	        					exptTimeRadioButtons[groupIndex][sample].setSelected(true);
	        				}catch (Exception e){
	        					notInTimeGroupRadioButtons[sample].setSelected(true);  //set to last state... excluded
	        				}
	        				if (cond==2&&(ngPanel.getExperimentDesign()==2)){
		        				if(condIndex == 1)
		        					exptConditionRadioButtons[condIndex][sample].setSelected(true);
		        				else
		        					exptConditionRadioButtons[0][sample].setSelected(true);
	        				}
	        			}
	        			
	        			repaint();			
	        			//need to clear assignments, clear assignment booleans in sample list and re-init
	        			//maybe a specialized inti for the sample list panel.
	        		} catch (Exception e) {
	        			JOptionPane.showMessageDialog(this, "<html>The file format cannot be read.</html>", "File Compatibility Error", JOptionPane.ERROR_MESSAGE);
	        		}
	        	}
        	}
        	private void setStateBasedOnIndex(Vector<String>groupAssignments,Vector<String>groupNames, int cond,Vector<String>condAssignments, Vector<String>condNames){
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
        				condIndex = condNames.indexOf(condName);
    				}
        			try{
        				exptTimeRadioButtons[groupNames.indexOf(groupAssignments.get(sample))][sample].setSelected(true);
        				if (cond==2&&(ngPanel.getExperimentDesign()==2)){
	        				if(condIndex == 1)
	        					exptConditionRadioButtons[condIndex][sample].setSelected(true);
	        				else
	        					exptConditionRadioButtons[0][sample].setSelected(true);
        				}
        			}catch(Exception e){
    					notInTimeGroupRadioButtons[sample].setSelected(true);  //set to last state... excluded
        			}
        		}
        	}
        	
            
            
        
        	
        	
        }
        protected void reset(){
        	if (ngPanel.okPressed)
        		mulgPanel.reset();
        }
    }
    
    class PermOrFDistPanel extends JPanel {
        JRadioButton tDistButton, permutButton; // randomGroupsButton, allCombsButton;
        JLabel numPermsLabel;
        JTextField timesField;//, alphaInputField;
        //JButton permParamButton;
        
        PermOrFDistPanel() {
            // this.setBorder(new TitledBorder(new EtchedBorder(), "P-Value parameters"));
            this.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED), "Permutations or F-distribution", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), Color.black));
            this.setBackground(Color.white);
            GridBagLayout gridbag = new GridBagLayout();
            GridBagConstraints constraints = new GridBagConstraints();
            //constraints.anchor = GridBagConstraints.WEST;
            //constraints.fill = GridBagConstraints.BOTH;
            this.setLayout(gridbag);
            
            //permParamButton = new JButton("Permutation parameters");
            //permParamButton.setEnabled(false);
            
            ButtonGroup chooseP = new ButtonGroup();
            
            tDistButton = new JRadioButton("p-values based on F-distribution", true);
            tDistButton.setFocusPainted(false);
            tDistButton.setForeground(UIManager.getColor("Label.foreground"));
            tDistButton.setBackground(Color.white);
            
            numPermsLabel = new JLabel("Enter number of permutations");
            numPermsLabel.setEnabled(false);
            
            timesField = new JTextField("1000", 7);
            timesField.setEnabled(false);
            timesField.setBackground(Color.darkGray);
            
            tDistButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    numPermsLabel.setEnabled(false);
                    timesField.setEnabled(false);
                    timesField.setBackground(Color.darkGray); 
                    if (pPanel.maxTButton.isSelected() || pPanel.falseNumButton.isSelected() || pPanel.falsePropButton.isSelected()) {
                        pPanel.justAlphaButton.setSelected(true);
                    }
                    pPanel.maxTButton.setEnabled(false);                    
                    pPanel.falseNumButton.setEnabled(false);
                    pPanel.falsePropButton.setEnabled(false);
                    pPanel.falseNumField.setEnabled(false);
                    pPanel.falsePropField.setEnabled(false);                    
                    //pAdjPanel.minPButton.setEnabled(false);
                }
            });
            
            chooseP.add(tDistButton);
            
            permutButton = new JRadioButton("p-values based on permutation:  ", false);
            permutButton.setFocusPainted(false);
            permutButton.setForeground(UIManager.getColor("Label.foreground"));
            permutButton.setBackground(Color.white);
            
            permutButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    numPermsLabel.setEnabled(true);
                    timesField.setEnabled(true);
                    timesField.setBackground(Color.white);                  
                    pPanel.maxTButton.setEnabled(true);  //UNCOMMENT THIS WHEN MAXT METHOD HAS BEEN IMPLEMEMTED
                    //pAdjPanel.minPButton.setEnabled(true);  //UNCOMMENT THIS WHEN MINP METHOD HAS BEEN DEBUGGED    
                    pPanel.falseNumButton.setEnabled(true);
                    pPanel.falsePropButton.setEnabled(true);
                    pPanel.falseNumField.setEnabled(true);
                    pPanel.falsePropField.setEnabled(true);
                }                
            });
            
            chooseP.add(permutButton);
            
            
            //constraints.anchor = GridBagConstraints.CENTER;
            
            //numCombsLabel = new JLabel("                                       ");
            //numCombsLabel.setOpaque(false);
            /*
            constraints.fill = GridBagConstraints.BOTH;
            buildConstraints(constraints, 1, 2, 1, 1, 0, 0);
            gridbag.setConstraints(numCombsLabel, constraints);
            this.add(numCombsLabel);
             */
            
            
            buildConstraints(constraints, 0, 0, 3, 1, 100, 50);
            //constraints.fill = GridBagConstraints.BOTH;
            constraints.anchor = GridBagConstraints.WEST;
            gridbag.setConstraints(tDistButton, constraints);
            this.add(tDistButton);
            
            buildConstraints(constraints, 0, 1, 1, 1, 30, 50);
            constraints.anchor = GridBagConstraints.WEST;
            gridbag.setConstraints(permutButton, constraints);
            this.add(permutButton);
            
            //JLabel numPermsLabel = new JLabel("Enter number of permutations");
            //numPermsLabel.setEnabled(false);
            buildConstraints(constraints, 1, 1, 1, 1, 30, 0);
            gridbag.setConstraints(numPermsLabel, constraints);
            this.add(numPermsLabel);
            
            buildConstraints(constraints, 2, 1, 1, 1, 40, 0);
            gridbag.setConstraints(timesField, constraints);
            this.add(timesField);
            /*
            JLabel alphaLabel = new JLabel("Enter critical p-value");
            buildConstraints(constraints, 0, 2, 2, 1, 60, 40);
            //constraints.anchor = GridBagConstraints.EAST;
            gridbag.setConstraints(alphaLabel, constraints);
            this.add(alphaLabel);
            
            alphaInputField = new JTextField("0.01", 7);
            buildConstraints(constraints, 1, 2, 1, 1, 40, 0);
            constraints.anchor = GridBagConstraints.WEST;
            gridbag.setConstraints(alphaInputField, constraints);
            this.add(alphaInputField);
             */
        }
    }
    
    
    class PValuePanel extends JPanel {
        JTextField pValueInputField, falseNumField, falsePropField;
        JRadioButton justAlphaButton, stdBonfButton, adjBonfButton, maxTButton, falseNumButton, falsePropButton;
        
        public PValuePanel() {
            this.setBorder(new TitledBorder(new EtchedBorder(), "P-value / false discovery parameters", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), Color.black));
            GridBagLayout gridbag = new GridBagLayout();
            GridBagConstraints constraints = new GridBagConstraints();
            //constraints.fill = GridBagConstraints.BOTH;
            this.setBackground(Color.white);
            //JPanel pane = new JPanel();
            this.setLayout(gridbag);
            
            JLabel pValueLabel = new JLabel("Enter alpha (critical p-value): ");
            buildConstraints(constraints, 0, 0, 1, 1, 33, 25);
            constraints.anchor = GridBagConstraints.EAST;
            gridbag.setConstraints(pValueLabel, constraints);
            this.add(pValueLabel);
            
            pValueInputField = new JTextField("0.01", 7);
            buildConstraints(constraints, 1, 0, 1, 1, 33, 0);
            constraints.anchor = GridBagConstraints.WEST;
            gridbag.setConstraints(pValueInputField, constraints);
            this.add(pValueInputField);
            
            constraints.anchor = GridBagConstraints.CENTER;
            
            justAlphaButton = new JRadioButton("Just alpha (no correction)", true);
            justAlphaButton.setBackground(Color.white);
            //justAlphaButton.setVisible(false);
            stdBonfButton = new JRadioButton("Standard Bonferroni", false);
            stdBonfButton.setBackground(Color.white);
            //stdBonfButton.setVisible(false); // WILL BE MADE VISIBLE WHEN THESE OPTIONS ARE IMPLEMENTED
            adjBonfButton = new JRadioButton("Adjusted Bonferroni", false);
            adjBonfButton.setBackground(Color.white);
            //adjBonfButton.setVisible(false);// WILL BE MADE VISIBLE WHEN THESE OPTIONS ARE IMPLEMENTED
            maxTButton = new JRadioButton("Westfall-Young step-down maxT", false);
            maxTButton.setBackground(Color.white);
            maxTButton.setEnabled(false);
            
            falseNumButton = new JRadioButton("EITHER, The number of false significant genes should not exceed", false);
            falseNumButton.setEnabled(false);
            //falseNumButton.setEnabled(false);
            falseNumButton.setFocusPainted(false);
            falseNumButton.setForeground(UIManager.getColor("Label.foreground"));
            falseNumButton.setBackground(Color.white);
            //sigGroup.add(falseNumButton);            
            
            falsePropButton = new JRadioButton("OR, The proportion of false significant genes should not exceed", false);
            falsePropButton.setEnabled(false);
            //falsePropButton.setEnabled(false);
            falsePropButton.setFocusPainted(false);
            falsePropButton.setForeground(UIManager.getColor("Label.foreground"));
            falsePropButton.setBackground(Color.white);
            //sigGroup.add(falsePropButton);            
            
            falseNumField = new JTextField(10);
            falseNumField.setText("10");
            falseNumField.setEnabled(false);
            falsePropField = new JTextField(10);
            falsePropField.setText("0.05");
            falsePropField.setEnabled(false);            
            
            ButtonGroup chooseCorrection = new ButtonGroup();
            chooseCorrection.add(justAlphaButton);
            chooseCorrection.add(stdBonfButton);
            chooseCorrection.add(adjBonfButton);
            chooseCorrection.add(maxTButton);
            chooseCorrection.add(falseNumButton);
            chooseCorrection.add(falsePropButton);
            //stdBonfButton.setEnabled(false);
            //adjBonfButton.setEnabled(false);
            
            JPanel FDRPanel = new JPanel();
            FDRPanel.setBackground(Color.white);
            FDRPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED), "False discovery control (permutations only)", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), Color.black));
            GridBagLayout grid3 = new GridBagLayout(); 
            FDRPanel.setLayout(grid3);            
            
            JLabel FDRLabel = new JLabel("With confidence of [1 - alpha] : ");            
            constraints.anchor = GridBagConstraints.WEST;
            buildConstraints(constraints, 0, 0, 2, 1, 100, 34);
            grid3.setConstraints(FDRLabel, constraints);
            FDRPanel.add(FDRLabel);
            
            constraints.anchor = GridBagConstraints.CENTER;
            
            buildConstraints(constraints, 0, 1, 1, 1, 50, 33);
            constraints.anchor = GridBagConstraints.EAST;
            grid3.setConstraints(falseNumButton, constraints);
            FDRPanel.add(falseNumButton);       
            
            buildConstraints(constraints, 1, 1, 1, 1, 50, 0);
            constraints.anchor = GridBagConstraints.WEST;
            grid3.setConstraints(falseNumField, constraints);
            FDRPanel.add(falseNumField);    
            
            buildConstraints(constraints, 0, 2, 1, 1, 50, 33);
            constraints.anchor = GridBagConstraints.EAST;
            grid3.setConstraints(falsePropButton, constraints);
            FDRPanel.add(falsePropButton);    
            
            buildConstraints(constraints, 1, 2, 1, 1, 50, 0);
            constraints.anchor = GridBagConstraints.WEST;
            grid3.setConstraints(falsePropField, constraints);
            FDRPanel.add(falsePropField);    
            
            constraints.anchor = GridBagConstraints.CENTER;
            
           
            
            buildConstraints(constraints, 0, 1, 1, 1, 25, 25);
            gridbag.setConstraints(justAlphaButton, constraints);
            this.add(justAlphaButton);
 
            buildConstraints(constraints, 1, 1, 1, 1, 25, 0);
            gridbag.setConstraints(stdBonfButton, constraints);
            this.add(stdBonfButton);  
            
            buildConstraints(constraints, 2, 1, 1, 1, 25, 0);
            gridbag.setConstraints(adjBonfButton, constraints);
            this.add(adjBonfButton);     
            
            buildConstraints(constraints, 3, 1, 1, 1, 25, 0);
            gridbag.setConstraints(maxTButton, constraints);
            this.add(maxTButton); 
            
            buildConstraints(constraints, 0, 2, 4, 1, 100, 50);
            gridbag.setConstraints(FDRPanel, constraints);
            this.add(FDRPanel);            
        }
        
        protected void reset() {
            pValueInputField.setText("0.01");
            justAlphaButton.setSelected(true);
        }
        
    }
    
    public class EventListener extends WindowAdapter implements ActionListener{
        
        public void actionPerformed(ActionEvent ae) {
            String command = ae.getActionCommand();
            if(command.equals("ok-command")){
            	if (!okReady)
            		return;
            	if ((getTestDesign()==BETRInitBox.CLUSTER_SELECTION)&&(repository.isEmpty())){
            		JOptionPane.showMessageDialog(null, "Cluster Repository is Empty.", "Error", JOptionPane.WARNING_MESSAGE);
            		return;
            	}
                boolean tooFew = false;
                if (getDataDesign()==2){
	                int[] timeAssignments=getTimeAssignments();
	                int[] conditionAssignments = getConditionAssignments();
	                if (getTestDesign()==BETRInitBox.CLUSTER_SELECTION){
	                	timeAssignments=getClusterTimeAssignments();
	                	conditionAssignments = getClusterConditionAssignments();
	                }
	                if (timeAssignments==null)
	                	return;
	                if (conditionAssignments==null)
	                	return;
	                int numTimePoints = getNumTimePoints();
	                int[] timePointGroupSize = new int[numTimePoints];
	                
	                for (int condition = 0; condition<2; condition++){
		                for (int i = 0; i < timePointGroupSize.length; i++) {
		                    timePointGroupSize[i] = 0;
		                }
		                
		                for (int i = 0; i < timeAssignments.length; i++) {
		                    int currentGroup = timeAssignments[i];
		                    if (currentGroup != 0&& conditionAssignments[i]==condition+1) {
		                        timePointGroupSize[currentGroup - 1]++;
		                    }
		                }
		                
		                for (int i = 0; i < timePointGroupSize.length; i++) {
		                    if (timePointGroupSize[i] < 2) {
		                    	int ci = 0;
		                    	if (getTestDesign()==BETRInitBox.CLUSTER_SELECTION)
		                    		ci=1;
		                    	JOptionPane.showMessageDialog(null, "Each condition must have at least two samples for each time point. \n \n" +
		                    			"Condition "+(condition+1)+ ", time point "+(i+ci)+ " has "+timePointGroupSize[i] + " samples(s).",
		                    			"Error: Insufficient Data", JOptionPane.WARNING_MESSAGE);
		                        tooFew = true;
		                        break;
		                    }
		                }
	                }
            	}
                if (getDataDesign()==1||getDataDesign()==3){
                	int[] timeAssignments=getTimeAssignments();
 	                if (getTestDesign()==BETRInitBox.CLUSTER_SELECTION){
 	                	timeAssignments=getClusterTimeAssignments();
 	                }
 	                if (timeAssignments==null)
 	                	return;
 	                int numTimePoints = getNumTimePoints();
 	                int[] timePointGroupSize = new int[numTimePoints];
 	                
              
	                for (int i = 0; i < timePointGroupSize.length; i++) {
	                    timePointGroupSize[i] = 0;
	                }
	                
	                for (int i = 0; i < timeAssignments.length; i++) {
	                    int currentGroup = timeAssignments[i];
	                    if (currentGroup != 0) {
	                        timePointGroupSize[currentGroup - 1]++;
	                    }
	                }
 		                
	                for (int i = 0; i < timePointGroupSize.length; i++) {
	                    if (timePointGroupSize[i] < 2) {
	                    	int ci = 0;
	                    	if (getTestDesign()==BETRInitBox.CLUSTER_SELECTION)
	                    		ci=1;
	                    	JOptionPane.showMessageDialog(null, "Each condition must have at least two samples for each time point. \n \n" +
	                    			"Time point "+(i+ci)+ " has "+timePointGroupSize[i] + " samples(s).",
	                    			"Error: Insufficient Data", JOptionPane.WARNING_MESSAGE);
	                        tooFew = true;
	                        break;
	                    }
	                }
 	                
            	}
                if (!tooFew) {
                    try {               
                        if (pPanel.falseNumButton.isSelected()) {
                            if (!validateFalseNum()) {
                                okPressed = false;
                                return;
                            }
                        }
                        if (pPanel.falsePropButton.isSelected()) {
                            if (!validateFalseProp()) {
                                okPressed = false;
                                return;
                            }
                        }                     
                        //HERE, CHECK OTHER INPUTS: P-VALUE VALIDITY - 4/25/03
                        double d = Double.parseDouble(pPanel.pValueInputField.getText());
                        /*
                        if (usePerms()) {
                            int p = getNumPerms();
                        }
                         */
                        if ((d <= 0d)||(d > 1d) || (usePerms() && (getNumPerms() <= 1))) {
                            JOptionPane.showMessageDialog(null, "Valid inputs: 0 < alpha < 1, and # of permutations (integer only) > 1", "Error!", JOptionPane.ERROR_MESSAGE);                            
                        } else {
                            okPressed = true;
                            dispose();
                        }
                    } catch (NumberFormatException nfe) {
                        JOptionPane.showMessageDialog(null, "Valid inputs: 0 < alpha < 1, and # of permutations (integer only) > 1", "Error!", JOptionPane.ERROR_MESSAGE);
                    }

                }
            } else if (command.equals("reset-command")) {
                mPanel.reset();
                pPanel.reset();
            } else if (command.equals("cancel-command")) {
                okPressed = false;
                dispose();
            } else if (command.equals("info-command")){
            	HelpWindow.launchBrowser(BETRInitBox.this, "Bayesian Estimation of Temporal Regulation- Initialization Dialog");
		}
        }
        
    }
    
    public int[] getTimeAssignments() {
        int[] timeAssignments = new int[exptNames.size()];
        
        for (int i = 0; i < exptNames.size(); i++) {
            if (mPanel.mulgPanel.notInTimeGroupRadioButtons[i].isSelected()) {// "NOT IN GROUP" IS STORED AS ZERO, AND GROUP J IS STORED AS THE INTEGER J (I.E., THERE IS NO GROUP 0)
                timeAssignments[i] = 0;
            } else {
                for (int j = 0; j < mPanel.mulgPanel.exptTimeRadioButtons.length; j++) {
                    if (mPanel.mulgPanel.exptTimeRadioButtons[j][i].isSelected()) {
                        timeAssignments[i] = j + 1;
                        break;
                    }
                }
            }
        }
        return timeAssignments;
    }   
    public int[][] getTimeMatrix(){
    	int[] timeAssignments;
    	if (getTestDesign()==BETRInitBox.CLUSTER_SELECTION){
    		timeAssignments = getClusterTimeAssignments();
    	}else{
    		timeAssignments = getTimeAssignments();
    	}
    	int[] numEachTime = new int[getNumTimePoints()];
    	//System.out.println("getNumTimePoints() = "+getNumTimePoints());
    	//System.out.println("getTimeMatrix- timeAssignments.length = "+timeAssignments.length);
    	for (int i=0; i< timeAssignments.length; i++){
    		//System.out.println("timeAssignments[i]-1 = "+ (timeAssignments[i]-1));
    		//System.out.println("numEachTime[timeAssignments[i]-1] = "+numEachTime[timeAssignments[i]-1]);
    		if (timeAssignments[i]!=0)
    			numEachTime[timeAssignments[i]-1]++;
    	}
    	int[][]timeMatrix=new int[getNumTimePoints()][];
    	for (int i=0; i<getNumTimePoints(); i++){
    		timeMatrix[i]=new int[numEachTime[i]];
    	}
    	int[]nextEntry=new int[getNumTimePoints()];
    	for (int i=0; i< timeAssignments.length; i++){
    		if (timeAssignments[i]!=0){
	    		timeMatrix[timeAssignments[i]-1][nextEntry[timeAssignments[i]-1]] = i;
	    		nextEntry[timeAssignments[i]-1]++;
    		}
    	}
    	return timeMatrix;
    }
    
    public int[][] getConditionsMatrix(){
    	int[] conditionAssignments;
    	if (getTestDesign()==BETRInitBox.CLUSTER_SELECTION){
    		conditionAssignments = getClusterConditionAssignments();
    	}else{
    		conditionAssignments = getConditionAssignments();
    	}
    	
    	
    	
    	int[] numEachTime = new int[2];
    	for (int i=0; i< conditionAssignments.length; i++){
    		if (conditionAssignments[i]!=0)
    		numEachTime[conditionAssignments[i]-1]=numEachTime[conditionAssignments[i]-1]+1;
    	}
    	int[][]conditionMatrix = null;
    	if (getDataDesign()==2){
	    	conditionMatrix=new int[2][];
	    	for (int i=0; i<2; i++){
	    		conditionMatrix[i]=new int[numEachTime[i]];
	    	}
	    	int[]nextEntry=new int[getNumTimePoints()];
	    	for (int i=0; i< conditionAssignments.length; i++){
	    		if (conditionAssignments[i]!=0){
	    			conditionMatrix[conditionAssignments[i]-1][nextEntry[conditionAssignments[i]-1]] = i;
	    			nextEntry[conditionAssignments[i]-1]++;
	    		}
	    	}
    	}else if (getDataDesign()==1){
    		conditionMatrix = null;
    	}else{
	    	conditionMatrix=null;
    	}
    	return conditionMatrix;
    }
    
    public int[] getConditionAssignments() {
        int[] conditionAssignments = new int[exptNames.size()];
        
        for (int i = 0; i < exptNames.size(); i++) {
            if (mPanel.mulgPanel.notInTimeGroupRadioButtons[i].isSelected()) {// "NOT IN GROUP" IS STORED AS ZERO, AND GROUP J IS STORED AS THE INTEGER J (I.E., THERE IS NO GROUP 0)
                conditionAssignments[i] = 0;
            } else {
                for (int j = 0; j < mPanel.mulgPanel.exptConditionRadioButtons.length; j++) {
                    if (mPanel.mulgPanel.exptConditionRadioButtons[j][i].isSelected()) {
                        conditionAssignments[i] = j + 1;
                        break;
                    }
                }
            }
        }
        return conditionAssignments;
    }  
    public int getDataDesign() {
    	int design = -1;
    	if (mPanel.ngPanel.oneCondition.isSelected())
    		design = 1;
    	if (mPanel.ngPanel.twoConditions.isSelected())
    		design = 2;
    	if (mPanel.ngPanel.pairedData.isSelected())
    		design = 3;
    	return design;
    }
    
    public int getTestDesign() {
        int design = -1;
        if (mPanel.tabbedmulg.getSelectedIndex() == 0) {
        	design = BETRInitBox.BUTTON_SELECTION;
        	} else {
        	design = BETRInitBox.CLUSTER_SELECTION;
        }
        return design;
    }

    public int[] getClusterTimeAssignments(){
    	boolean doubleAssigned;
    	int[]groupAssignments = new int[exptNames.size()];
    	ArrayList[] arraylistArray = new ArrayList[mPanel.numTimePoints];
    	for (int i=0; i<mPanel.numTimePoints; i++){
    		int j = i+1;
    		arraylistArray[i] = mPanel.clusterSelectorTime.getGroupSamples("Time "+j);
    		
    	}
    	for (int i = 0; i < exptNames.size(); i++) {
    		doubleAssigned = false;
    		groupAssignments[i] = 0;
    		for (int j = 0;j<mPanel.numTimePoints;j++){
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
    
    public int[] getClusterConditionAssignments(){
    	boolean doubleAssigned;
    	int[]groupAssignments = new int[exptNames.size()];
    	ArrayList[] arraylistArray = new ArrayList[2];
    	for (int i=0; i<2; i++){
    		int j = i+1;
    		arraylistArray[i] = mPanel.clusterSelectorCondition.getGroupSamples("Condition "+j);
    		
    	}
    	for (int i = 0; i < exptNames.size(); i++) {
    		doubleAssigned = false;
    		groupAssignments[i] = 0;
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
	    			groupAssignments[i] = j+1;
	    			doubleAssigned = true;
	    		}
    		}
        }
    	return groupAssignments;
    }
    
    
    public int getNumTimePoints() {
        return mPanel.numTimePoints;
    }
    
    public boolean usePerms() {
        return this.permPanel.permutButton.isSelected();
    }
    
    public int getNumPerms() {
        return Integer.parseInt(this.permPanel.timesField.getText());
    }
    
    public double getPValue() {
        return Double.parseDouble(pPanel.pValueInputField.getText());
    }
    
    public float getAlpha() {
    	return Float.parseFloat(mPanel.ngPanel.alphaField.getText());
    }
    
    public int getFalseNum() {
        return Integer.parseInt(pPanel.falseNumField.getText());
    }
    
    public double getFalseProp() {
        return Double.parseDouble(pPanel.falsePropField.getText());
    }    
    
    public boolean validateFalseNum() {
        int a;
        try {
            String falseNum = pPanel.falseNumField.getText();
            a = Integer.parseInt(falseNum);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(BETRInitBox.this, "False number must be an integer >= 0", "Input Error", JOptionPane.WARNING_MESSAGE);
            pPanel.falseNumField.requestFocus();
            pPanel.falseNumField.selectAll();
            return false;
        }
        if (a < 0) {
            JOptionPane.showMessageDialog(BETRInitBox.this, "False number must be an integer >= 0", "Input Error", JOptionPane.WARNING_MESSAGE);
            pPanel.falseNumField.requestFocus();
            pPanel.falseNumField.selectAll();
            return false;          
        }
        return true;
    }
    
    public boolean validateFalseProp() {
        float a;
        try {
            String falseProp = pPanel.falsePropField.getText();
            a = Float.parseFloat(falseProp);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(BETRInitBox.this, "False proportion must be between 0 and 1", "Input Error", JOptionPane.WARNING_MESSAGE);
            pPanel.falsePropField.requestFocus();
            pPanel.falsePropField.selectAll();
            return false;
        }
        if ((a <= 0) || (a > 1)) {
            JOptionPane.showMessageDialog(BETRInitBox.this, "False proportion must be between 0 and 1", "Input Error", JOptionPane.WARNING_MESSAGE);
            pPanel.falsePropField.requestFocus();
            pPanel.falsePropField.selectAll();
            return false;          
        }
        return true;
    }       

    public int getCorrectionMethod() {
        int method = JUST_ALPHA;
        if (pPanel.justAlphaButton.isSelected()) {
            method = JUST_ALPHA;
        } else if (pPanel.stdBonfButton.isSelected()) {
            method = STD_BONFERRONI;
        } else if (pPanel.adjBonfButton.isSelected()) {
            method = ADJ_BONFERRONI;
        } else if (pPanel.maxTButton.isSelected()) {
            method = MAX_T;
        } else if (pPanel.falseNumButton.isSelected()) {
            method = FALSE_NUM;
        } else if (pPanel.falsePropButton.isSelected()) {
            method = FALSE_PROP;
        }
        
        return method;
    }
    
    
    public static void main(String[] args) {
        JFrame dummyFrame = new JFrame();
        Vector<String> dummyVect = new Vector<String>();
        for (int i = 0; i < 24; i++) {
            dummyVect.add("Expt " + i);
        }
        
        BETRInitBox oBox = new BETRInitBox(dummyFrame, true, dummyVect, null);
        oBox.setVisible(true);
        
    }
}
