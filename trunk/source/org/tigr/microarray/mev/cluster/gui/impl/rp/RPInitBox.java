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

package org.tigr.microarray.mev.cluster.gui.impl.rp;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
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

import org.tigr.microarray.mev.cluster.gui.impl.BETR.BETRInitBox;
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
public class RPInitBox extends AlgorithmDialog {

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
    Vector<String> exptNames;    
    MultiClassPanel mPanel;
    JTabbedPane selectionPanel;
    //PermOrFDistPanel permPanel;
    PValuePanel pPanel;
    //HCLSelectionPanel hclOpsPanel;    
    HCLSigOnlyPanel hclOpsPanel;
    ClusterRepository repository;
    JButton step2Button = new JButton("Continue...");
    
    /** Creates new RPInitBox */
    public RPInitBox(JFrame parentFrame, boolean modality, Vector<String> exptNames, ClusterRepository repository) {
        super(parentFrame, "RP Initialization", modality);
        this.exptNames = exptNames;  
        this.repository = repository;
        setBounds(0, 0, 600, 850);
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
        //permPanel = new PermOrFDistPanel();
        //buildConstraints(constraints, 0, 1, 1, 1, 0, 5);
        //gridbag.setConstraints(permPanel, constraints);
        //consolidatedPane.add("Permutations or F-Distribution", permPanel);        

        pPanel = new PValuePanel();
        //buildConstraints(constraints, 0, 2, 1, 1, 0, 10);
        //gridbag.setConstraints(pPanel, constraints);
        consolidatedPane.add("P-Value/False Discovery Parameters", pPanel);
        
        hclOpsPanel = new HCLSigOnlyPanel();
        //buildConstraints(constraints, 0, 3, 1, 1, 0, 5);
        //gridbag.setConstraints(hclOpsPanel, constraints);
        consolidatedPane.add("Hierarchical Clusters", hclOpsPanel);  
        buildConstraints(constraints, 0, 1, 1, 1, 0, 20);
        gridbag.setConstraints(consolidatedPane, constraints);
        pane.add(consolidatedPane);   
        
        buildConstraints(constraints, 0, 1, 1, 1, 0, 20);
        gridbag.setConstraints(hclOpsPanel, constraints);
        //mPanel.add(hclOpsPanel, constraints);
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
        //ClusterSelector clusterSelectorTime;
        //int numTimePoints;
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
            
//            step2Button.addActionListener(new ActionListener(){
//                public void actionPerformed(ActionEvent evt) {
                    ngPanel.okPressed = true;
                    okReady = true;
                    try {
                    	alpha = Float.parseFloat(ngPanel.alphaField.getText());
                        //numTimePoints = Integer.parseInt(ngPanel.numTimePointsField.getText());
                        if (alpha>1||alpha<0){
                        	JOptionPane.showMessageDialog(null, "alpha must be between 0 and 1!", "Error", JOptionPane.ERROR_MESSAGE);
                            
                        }else{
	                            mulgPanel = new MultiGroupExperimentsPanel(exptNames, 1, ngPanel.getExperimentDesign());
	                            
	                            
	                            //JButton dummyButton  = new JButton("dummyButton");
	                            //dummyButton.setVisible(true);
	                            //MultiClassPanel.this.remove(dummyPanel);
	                            tabbedmulg = new JTabbedPane();
	                            
	                            clusterSelectorCondition= new ClusterSelector(repository,1, "Samples");
	                            //clusterSelectorTime= new ClusterSelector(repository,numTimePoints, "Time");
	                            if (repository!=null){
	                            	clusterSelectorCondition.setClusterType("Condition");
	                            	//clusterSelectorTime.setClusterType("Time Points");
	                    		}
	                            JPanel clusterSelectorPanel = new JPanel();
	                            clusterSelectorPanel.setLayout(new GridBagLayout());

	                            GridBagConstraints c = new GridBagConstraints();
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
	                            //clusterSelectorPanel.add(clusterSelectorTime, c);
	                            
	                            
	                            
	                            
	                            tabbedmulg.add("Button Selection", mulgPanel);
	                            tabbedmulg.add("Cluster Selection", clusterSelectorPanel);
	                            tabbedmulg.setSelectedIndex(1);
	                            if (repository==null||repository.isEmpty())
	                            	tabbedmulg.setSelectedIndex(0);
	                            buildConstraints(constraints, 1, 0, 1, 3, 100, 100);
	                            constraints.fill = GridBagConstraints.BOTH;
	                            gridbag.setConstraints(tabbedmulg, constraints);
	                            MultiClassPanel.this.add(tabbedmulg);
	                            //MultiClassPanel.this.add(dummyButton);
	                            MultiClassPanel.this.validate();
	                            step2Button.setEnabled(false);
	                            enableOK();
	                            ngPanel.numTimePointsField.setEnabled(false);
	                            ngPanel.alphaField.setEnabled(false);
	                            ngPanel.oneCondition.setEnabled(false);
	                            ngPanel.twoConditions.setEnabled(false);
	                            ngPanel.pairedData.setEnabled(false);
	                            step2Button.setVisible(false);
	                        }
                        
                        //MultiClassPanel.this.repaint();
                        //dispose();
                    } catch (NumberFormatException nfe) {
                        JOptionPane.showMessageDialog(null, "Please enter a value greater than 0 and less than 1!", "Error", JOptionPane.ERROR_MESSAGE);
                    }
//                }
//            });
            //this.add(ngPanel);

            hclOpsPanel = new HCLSigOnlyPanel();
            hclOpsPanel.setBorder(null);//.setBorder( BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, Color.black));
            buildConstraints(constraints, 1, 0, 1, 1, 100, 10);
            gridbag.setConstraints(hclOpsPanel, constraints);
            //this.add(hclOpsPanel);

            buildConstraints(constraints, 0, 1, 2, 1, 100, 10);
            //constraints.insets = new Insets(100,100,100,100);
            constraints.fill = GridBagConstraints.NONE;
            gridbag.setConstraints(step2Button, constraints);
            this.add(step2Button);
            
            buildConstraints(constraints, 0, 1, 1, 1, 0, 90);
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
                
                oneCondition=new JRadioButton("1 Condition", false);
                twoConditions=new JRadioButton("2 Conditions", true);
                pairedData=new JRadioButton("Paired Data", false);
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
                numTimePointsField.setMinimumSize(new Dimension(numTimePointsField.getSize().height, 30));
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
            JCheckBox[] exptCheckBoxes;
            JRadioButton[] notInTimeGroupRadioButtons;
            JRadioButton[][] exptConditionRadioButtons;
            MultiGroupExperimentsPanel(Vector<String> exptNames, int numTimePoints, int conditions) {
                this.setBorder(new TitledBorder(new EtchedBorder(), "Time/Condition Assignments", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), Color.black));
                setBackground(Color.white);

               // JPanel panel1 = new JPanel();
                expLabels = new JLabel[exptNames.size()];
                exptCheckBoxes = new JCheckBox[exptNames.size()];
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
                        exptCheckBoxes[i] = new JCheckBox("",true);
                        //chooseTime[i].add(exptTimeRadioButtons[i]);
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
	                    //panels[currPanel].add(exptConditionRadioButtons[0][i]);
	                    buildConstraints(constraints, 1, i%512, 1, 1, 100, 100);
	                    gridbag.setConstraints(exptConditionRadioButtons[1][i], constraints);
	                    //panels[currPanel].add(exptConditionRadioButtons[1][i]);
                    
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
                    
                    
                    buildConstraints(constraints, twoCondRoom, i%512, 1, 1, 100, 100);
                        //constraints.fill = GridBagConstraints.BOTH;
                        gridbag.setConstraints(exptCheckBoxes[i], constraints);
                        panels[currPanel].add(exptCheckBoxes[i]);
                        // panel1.add(exptGroupRadioButtons[j][i]);
                    
                    
                    buildConstraints(constraints, (numTimePoints + 1+twoCondRoom), i%512, 1, 1, 100, 100);
                    //constraints.fill = GridBagConstraints.BOTH;
                    gridbag.setConstraints(notInTimeGroupRadioButtons[i], constraints);
                    
                    
                    //panel1.add(notInGroupRadioButtons[i]);
                    //panels[currPanel].add(notInTimeGroupRadioButtons[i]);                    
                    
                    
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
                
                //JLabel label1 = new JLabel("Note: Each time-point MUST each contain more than one sample.");
                //if (ngPanel.getExperimentDesign()==2){
                //	label1 = new JLabel ("Note: Each time-point MUST each contain more than one sample for both conditions.");
                //}
                //label1.setHorizontalAlignment(JLabel.CENTER);
                //buildConstraints(constraints, 0, 1, 1, 1, 0, 5);
                //constraints.anchor = GridBagConstraints.EAST;
                //constraints.fill = GridBagConstraints.BOTH;
                //gridbag2.setConstraints(label1, constraints);
                //this.add(label1);
                
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
                
                final JFileChooser fc = new JFileChooser();
                fc.setCurrentDirectory(new File("Data"));
                
                saveButton.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent evt) {
                        int returnVal = fc.showSaveDialog(MultiGroupExperimentsPanel.this);
                        if (returnVal == JFileChooser.APPROVE_OPTION) {
                            File file = fc.getSelectedFile();
                            try {
                                PrintWriter out = new PrintWriter(new FileOutputStream(file));
                                int[] timeAssgn=null;
                                if (getTestDesign()==RPInitBox.CLUSTER_SELECTION){
                                	//timeAssgn=getClusterTimeAssignments();
                                }
                                if (getTestDesign()==RPInitBox.BUTTON_SELECTION){
                                	//timeAssgn=getTimeAssignments();
                                } 
                                for (int i = 0; i < timeAssgn.length; i++) {
                                    out.print(timeAssgn[i]);
                                    if (i < timeAssgn.length - 1) {
                                        out.print("\n");
                                    }
                                }
                                
                                if (ngPanel.getExperimentDesign()==2){
                                	out.print("\n");
                                	out.println("cond");
                                	int[] condAssgn=null;
                                    if (getTestDesign()==RPInitBox.CLUSTER_SELECTION){
                                    	condAssgn=getClusterConditionAssignments();
                                    }
                                	if (getTestDesign()==RPInitBox.BUTTON_SELECTION){
                                    	condAssgn=getInGroupAssignments();
                                    } 
                                    for (int i = 0; i < condAssgn.length; i++) {
                                        out.print(condAssgn[i]);
                                        if (i < condAssgn.length - 1) {
                                            out.print("\n");
                                        }
                                    }
                                }
                                
                                out.println();
                                
                                out.flush();
                                out.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            //this is where a real application would save the file.
                            //log.append("Saving: " + file.getName() + "." + newline);
                        } else {
                            //log.append("Save command cancelled by user." + newline);
                        }
                    }
                });
                
                
                //NEED TO REWORK THIS FOR MULTICLASS
                
                loadButton.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent evt) {
                        int returnVal = fc.showOpenDialog(MultiGroupExperimentsPanel.this);
                        if (returnVal == JFileChooser.APPROVE_OPTION) {
                            try {
                                FileReader file = new FileReader(fc.getSelectedFile());
                                BufferedReader buff = new BufferedReader(file);
                               // String line = buff.readLine();
                               // StringSplitter st = new StringSplitter('\t');
                                //st.init(line);
                                Vector<Integer> timesVector = new Vector<Integer>();
                                Vector<Integer> condVector = new Vector<Integer>();
                                String current;
                                while ((current = buff.readLine()) != null) {
                                    //current = st.nextToken();
                                	if (current.equalsIgnoreCase("cond"))
                                		break;
                                    timesVector.add(new Integer(current));
                                }
                                while ((current = buff.readLine()) != null) {
                                    //current = st.nextToken();
                                	if (current.equalsIgnoreCase("cond"))
                                		break;
                                    condVector.add(new Integer(current));
                                }
                                buff.close();
                                int[] timeAssgn=null;
                                if (getTestDesign()==RPInitBox.CLUSTER_SELECTION){
                                	//timeAssgn=getClusterTimeAssignments();
                                }
                                if (getTestDesign()==RPInitBox.BUTTON_SELECTION){
                                	//timeAssgn=getTimeAssignments();
                                } 
                                if (timesVector.size() != timeAssgn.length) {
                                    JOptionPane.showMessageDialog(mPanel, "Incompatible file! Unequal samples", "Error", JOptionPane.WARNING_MESSAGE);
                                } else {
                                    for (int i = 0; i < timesVector.size(); i++) {
                                        int currentTime = ((Integer)timesVector.get(i)).intValue();
                                        if (currentTime != 0) {
                                            exptCheckBoxes[i].setSelected(true);
                                        } else {
                                            notInTimeGroupRadioButtons[i].setSelected(true);
                                        }
                                    }
                                    if (ngPanel.getExperimentDesign()==2){
                                    	for (int i = 0; i < condVector.size(); i++) {
                                            int currentTime = ((Integer)condVector.get(i)).intValue();
                                            if (currentTime != 0) {
                                                exptConditionRadioButtons[currentTime - 1][i].setSelected(true);
                                            } else {
                                                //notInGroupRadioButtons[i].setSelected(true);
                                            }
                                        }
                                    }
                                    
                                }
                            } catch (Exception e) {
                                JOptionPane.showMessageDialog(mPanel, "Incompatible file error!", "Error", JOptionPane.WARNING_MESSAGE);
                                
                                e.printStackTrace();
                            }
                            
                            //this is where a real application would save the file.
                            //log.append("Saving: " + file.getName() + "." + newline);
                        } else {
                            //log.append("Save command cancelled by user." + newline);
                        }
                    }
                });
                //
                
                
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
                
            /*
            JButton gButton = new JButton("groupExpts");
            buildConstraints(constraints, 0, 0, 1, 1, 100, 100);
            constraints.fill = GridBagConstraints.BOTH;
            gridbag.setConstraints(gButton, constraints);
            this.add(gButton);
             */
                
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
        }
        protected void reset(){
        	if (ngPanel.okPressed)
        		mulgPanel.reset();
        }
    }
    
//    class PermOrFDistPanel extends JPanel {
//        JRadioButton tDistButton, permutButton; // randomGroupsButton, allCombsButton;
//        
//        //, alphaInputField;
//        //JButton permParamButton;
//        
//        PermOrFDistPanel() {
//            // this.setBorder(new TitledBorder(new EtchedBorder(), "P-Value parameters"));
//            this.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED), "Permutations or F-distribution", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), Color.black));
//            this.setBackground(Color.white);
//            GridBagLayout gridbag = new GridBagLayout();
//            GridBagConstraints constraints = new GridBagConstraints();
//            //constraints.anchor = GridBagConstraints.WEST;
//            //constraints.fill = GridBagConstraints.BOTH;
//            this.setLayout(gridbag);
//            
//            //permParamButton = new JButton("Permutation parameters");
//            //permParamButton.setEnabled(false);
//            
//            ButtonGroup chooseP = new ButtonGroup();
//            
//            tDistButton = new JRadioButton("p-values based on F-distribution", true);
//            tDistButton.setFocusPainted(false);
//            tDistButton.setForeground(UIManager.getColor("Label.foreground"));
//            tDistButton.setBackground(Color.white);
//            
//            numPermsLabel = new JLabel("Enter number of permutations");
//            numPermsLabel.setEnabled(false);
//            
//            timesField = new JTextField("1000", 7);
//            timesField.setEnabled(false);
//            timesField.setBackground(Color.darkGray);
//            
//            tDistButton.addActionListener(new ActionListener() {
//                public void actionPerformed(ActionEvent evt) {
//                    numPermsLabel.setEnabled(false);
//                    timesField.setEnabled(false);
//                    timesField.setBackground(Color.darkGray); 
//                    if (pPanel.maxTButton.isSelected() || pPanel.falseNumButton.isSelected() || pPanel.falsePropButton.isSelected()) {
//                        pPanel.justAlphaButton.setSelected(true);
//                    }
//                    pPanel.maxTButton.setEnabled(false);                    
//                    pPanel.falseNumButton.setEnabled(false);
//                    pPanel.falsePropButton.setEnabled(false);
//                    pPanel.falseNumField.setEnabled(false);
//                    pPanel.falsePropField.setEnabled(false);                    
//                    //pAdjPanel.minPButton.setEnabled(false);
//                }
//            });
//            
//            chooseP.add(tDistButton);
//            
//            permutButton = new JRadioButton("p-values based on permutation:  ", false);
//            permutButton.setFocusPainted(false);
//            permutButton.setForeground(UIManager.getColor("Label.foreground"));
//            permutButton.setBackground(Color.white);
//            
//            permutButton.addActionListener(new ActionListener() {
//                public void actionPerformed(ActionEvent evt) {
//                    numPermsLabel.setEnabled(true);
//                    timesField.setEnabled(true);
//                    timesField.setBackground(Color.white);                  
//                    pPanel.maxTButton.setEnabled(true);  //UNCOMMENT THIS WHEN MAXT METHOD HAS BEEN IMPLEMEMTED
//                    //pAdjPanel.minPButton.setEnabled(true);  //UNCOMMENT THIS WHEN MINP METHOD HAS BEEN DEBUGGED    
//                    pPanel.falseNumButton.setEnabled(true);
//                    pPanel.falsePropButton.setEnabled(true);
//                    pPanel.falseNumField.setEnabled(true);
//                    pPanel.falsePropField.setEnabled(true);
//                }                
//            });
//            
//            chooseP.add(permutButton);
//            
//            
//            //constraints.anchor = GridBagConstraints.CENTER;
//            
//            //numCombsLabel = new JLabel("                                       ");
//            //numCombsLabel.setOpaque(false);
//            /*
//            constraints.fill = GridBagConstraints.BOTH;
//            buildConstraints(constraints, 1, 2, 1, 1, 0, 0);
//            gridbag.setConstraints(numCombsLabel, constraints);
//            this.add(numCombsLabel);
//             */
//            
//            
//            buildConstraints(constraints, 0, 0, 3, 1, 100, 50);
//            //constraints.fill = GridBagConstraints.BOTH;
//            constraints.anchor = GridBagConstraints.WEST;
//            gridbag.setConstraints(tDistButton, constraints);
//            this.add(tDistButton);
//            
//            buildConstraints(constraints, 0, 1, 1, 1, 30, 50);
//            constraints.anchor = GridBagConstraints.WEST;
//            gridbag.setConstraints(permutButton, constraints);
//            this.add(permutButton);
//            
//            //JLabel numPermsLabel = new JLabel("Enter number of permutations");
//            //numPermsLabel.setEnabled(false);
//            buildConstraints(constraints, 1, 1, 1, 1, 30, 0);
//            gridbag.setConstraints(numPermsLabel, constraints);
//            this.add(numPermsLabel);
//            
//            buildConstraints(constraints, 2, 1, 1, 1, 40, 0);
//            gridbag.setConstraints(timesField, constraints);
//            this.add(timesField);
//            /*
//            JLabel alphaLabel = new JLabel("Enter critical p-value");
//            buildConstraints(constraints, 0, 2, 2, 1, 60, 40);
//            //constraints.anchor = GridBagConstraints.EAST;
//            gridbag.setConstraints(alphaLabel, constraints);
//            this.add(alphaLabel);
//            
//            alphaInputField = new JTextField("0.01", 7);
//            buildConstraints(constraints, 1, 2, 1, 1, 40, 0);
//            constraints.anchor = GridBagConstraints.WEST;
//            gridbag.setConstraints(alphaInputField, constraints);
//            this.add(alphaInputField);
//             */
//        }
//    }
    
    
    class PValuePanel extends JPanel {
    	JLabel numPermsLabel;
    	JTextField timesField;
        JTextField pValueInputField, falseNumField, falsePropField;
        JRadioButton falseNumButton, falsePropButton;
        
        public PValuePanel() {
            this.setBorder(new TitledBorder(new EtchedBorder(), "P-value / false discovery parameters", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), Color.black));
            GridBagLayout gridbag = new GridBagLayout();
            GridBagConstraints constraints = new GridBagConstraints();
            //constraints.fill = GridBagConstraints.BOTH;
            this.setBackground(Color.white);
            //JPanel pane = new JPanel();
            this.setLayout(gridbag);
            numPermsLabel = new JLabel("Enter number of permutations");
            //numPermsLabel.setEnabled(false);
            buildConstraints(constraints, 0, 0, 1, 1, 33, 25);
            constraints.anchor = GridBagConstraints.EAST;
            gridbag.setConstraints(numPermsLabel, constraints);
            this.add(numPermsLabel);
            
            timesField = new JTextField("100", 7);
            timesField.setMinimumSize(new Dimension(50, 20));
            //timesField.setEnabled(false);
            //timesField.setBackground(Color.darkGray);
            buildConstraints(constraints, 1, 0, 1, 1, 33, 25);
            constraints.anchor = GridBagConstraints.WEST;
            gridbag.setConstraints(timesField, constraints);
            this.add(timesField);
            
            JLabel pValueLabel = new JLabel("Enter alpha (critical p-value): ");
            buildConstraints(constraints, 0, 1, 1, 1, 33, 25);
            constraints.anchor = GridBagConstraints.EAST;
            gridbag.setConstraints(pValueLabel, constraints);
            this.add(pValueLabel);
            
            pValueInputField = new JTextField("0.01", 7);
            pValueInputField.setMinimumSize(new Dimension(50, 20));
            buildConstraints(constraints, 1, 1, 1, 1, 33, 0);
            constraints.anchor = GridBagConstraints.WEST;
            gridbag.setConstraints(pValueInputField, constraints);
            this.add(pValueInputField);
            
            constraints.anchor = GridBagConstraints.CENTER;
            
            
            falseNumButton = new JRadioButton("EITHER, The number of false significant genes should not exceed", false);
            //falseNumButton.setEnabled(false);
            falseNumButton.setFocusPainted(false);
            falseNumButton.setForeground(UIManager.getColor("Label.foreground"));
            falseNumButton.setBackground(Color.white);
            //sigGroup.add(falseNumButton);            
            
            falsePropButton = new JRadioButton("OR, The proportion of false significant genes should not exceed", false);
            //falsePropButton.setEnabled(false);
            falsePropButton.setFocusPainted(false);
            falsePropButton.setForeground(UIManager.getColor("Label.foreground"));
            falsePropButton.setBackground(Color.white);
            //sigGroup.add(falsePropButton);            
            
            falseNumField = new JTextField(10);
            falseNumField.setText("10");
            falseNumField.setMinimumSize(new Dimension(50, 20));
            falsePropField = new JTextField(10);
            falsePropField.setText("0.05");
            falsePropField.setMinimumSize(new Dimension(50, 20));          
            
            ButtonGroup chooseCorrection = new ButtonGroup();
            chooseCorrection.add(falseNumButton);
            chooseCorrection.add(falsePropButton);
            //stdBonfButton.setEnabled(false);
            //adjBonfButton.setEnabled(false);
            
            JPanel FDRPanel = new JPanel();
            FDRPanel.setBackground(Color.white);
            FDRPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED), "False discovery control ", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), Color.black));
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
           
            
            buildConstraints(constraints, 0, 3, 4, 1, 100, 50);
            gridbag.setConstraints(FDRPanel, constraints);
            this.add(FDRPanel);            
        }
        
        protected void reset() {
            pValueInputField.setText("0.01");
        }
        
    }
    
    public class EventListener extends WindowAdapter implements ActionListener{
        
        public void actionPerformed(ActionEvent ae) {
            String command = ae.getActionCommand();
            if(command.equals("ok-command")){
            	if (!okReady)
            		return;
            	if ((getTestDesign()==RPInitBox.CLUSTER_SELECTION)&&(repository.isEmpty())){
            		JOptionPane.showMessageDialog(null, "Cluster Repository is Empty.", "Error", JOptionPane.WARNING_MESSAGE);
            		return;
            	}
                boolean tooFew = false;
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
                        int[] inGroupAssignments;
                        if (getTestDesign()==RPInitBox.CLUSTER_SELECTION){
                        	inGroupAssignments=getClusterConditionAssignments();
                        }else{
                        	inGroupAssignments=getInGroupAssignments();
                        }
                        int inNum = 0;
                        while(true){
                        	
                        	if (inGroupAssignments[inNum]==1)
                        		break;
                        	inNum++;
                        	if (inNum==inGroupAssignments.length){
                        		JOptionPane.showMessageDialog(null, "No samples have been assigned to the analysis.", "Error!", JOptionPane.ERROR_MESSAGE);
                        		okPressed = false;
                        		return;
                        	}
                        }
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
                HelpWindow hw = new HelpWindow(RPInitBox.this, "Rank Products- Initialization Dialog");
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
    
//    public int[] getTimeAssignments() {
//        int[] timeAssignments = new int[exptNames.size()];
//        
//        for (int i = 0; i < exptNames.size(); i++) {
//            if (mPanel.mulgPanel.notInTimeGroupRadioButtons[i].isSelected()) {// "NOT IN GROUP" IS STORED AS ZERO, AND GROUP J IS STORED AS THE INTEGER J (I.E., THERE IS NO GROUP 0)
//                timeAssignments[i] = 0;
//            } else {
//                for (int j = 0; j < mPanel.mulgPanel.exptTimeRadioButtons.length; j++) {
//                    if (mPanel.mulgPanel.exptTimeRadioButtons[i].isSelected()) {
//                        timeAssignments[i] = j + 1;
//                        break;
//                    }
//                }
//            }
//        }
//        return timeAssignments;
//    }   
//    public int[][] getTimeMatrix(){
//    	int[] timeAssignments;
//    	if (getTestDesign()==RPInitBox.CLUSTER_SELECTION){
//    		//timeAssignments = getClusterTimeAssignments();
//    	}else{
//    		timeAssignments = getTimeAssignments();
//    	}
//    	int[] numEachTime = new int[getNumTimePoints()];
//    	//System.out.println("getNumTimePoints() = "+getNumTimePoints());
//    	//System.out.println("getTimeMatrix- timeAssignments.length = "+timeAssignments.length);
//    	for (int i=0; i< timeAssignments.length; i++){
//    		//System.out.println("timeAssignments[i]-1 = "+ (timeAssignments[i]-1));
//    		//System.out.println("numEachTime[timeAssignments[i]-1] = "+numEachTime[timeAssignments[i]-1]);
//    		if (timeAssignments[i]!=0)
//    			numEachTime[timeAssignments[i]-1]++;
//    	}
 //   	int[][]timeMatrix=new int[getNumTimePoints()][];
//    	for (int i=0; i<getNumTimePoints(); i++){
//    		timeMatrix[i]=new int[numEachTime[i]];
//    	}
//    	int[]nextEntry=new int[getNumTimePoints()];
//    	for (int i=0; i< timeAssignments.length; i++){
//    		if (timeAssignments[i]!=0){
//	    		timeMatrix[timeAssignments[i]-1][nextEntry[timeAssignments[i]-1]] = i;
//	    		nextEntry[timeAssignments[i]-1]++;
//    		}
//    	}
//    	return timeMatrix;
//    }
    
    public int[][] getConditionsMatrix(){
    	int[] conditionAssignments;
    	if (getTestDesign()==RPInitBox.CLUSTER_SELECTION){
    		conditionAssignments = getClusterConditionAssignments();
    	}else{
    		conditionAssignments = getInGroupAssignments();
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
	    	int[]nextEntry=new int[2];
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
    
    public int[] getInGroupAssignments() {
        int[] conditionAssignments = new int[exptNames.size()];
        
        for (int i = 0; i < exptNames.size(); i++) {
            if (mPanel.mulgPanel.exptCheckBoxes[i].isSelected()) {// "NOT IN GROUP" IS STORED AS ZERO, AND GROUP J IS STORED AS THE INTEGER J (I.E., THERE IS NO GROUP 0)
                conditionAssignments[i] = 1;
            } else {
                conditionAssignments[i] = 0;
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
        	design = RPInitBox.BUTTON_SELECTION;
        	} else {
        	design = RPInitBox.CLUSTER_SELECTION;
        }
        return design;
    }
    
    public int[] getClusterConditionAssignments(){
    	boolean doubleAssigned;
    	int[]groupAssignments = new int[exptNames.size()];
    	ArrayList[] arraylistArray = new ArrayList[2];
    	for (int i=0; i<2; i++){
    		int j = i+1;
    		arraylistArray[i] = mPanel.clusterSelectorCondition.getGroupSamples("Samples "+j);
    		
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
    
    
//    public int getNumTimePoints() {
//        return mPanel.numTimePoints;
//    }
    
    public boolean usePerms() {
        return true;//this.pPanel.permutButton.isSelected();
    }
    
    public int getNumPerms() {
        return Integer.parseInt(this.pPanel.timesField.getText());
    }
    
    public float getPValue() {
        return Float.parseFloat(pPanel.pValueInputField.getText());
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
            JOptionPane.showMessageDialog(RPInitBox.this, "False number must be an integer >= 0", "Input Error", JOptionPane.WARNING_MESSAGE);
            pPanel.falseNumField.requestFocus();
            pPanel.falseNumField.selectAll();
            return false;
        }
        if (a < 0) {
            JOptionPane.showMessageDialog(RPInitBox.this, "False number must be an integer >= 0", "Input Error", JOptionPane.WARNING_MESSAGE);
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
            JOptionPane.showMessageDialog(RPInitBox.this, "False proportion must be between 0 and 1", "Input Error", JOptionPane.WARNING_MESSAGE);
            pPanel.falsePropField.requestFocus();
            pPanel.falsePropField.selectAll();
            return false;
        }
        if ((a <= 0) || (a > 1)) {
            JOptionPane.showMessageDialog(RPInitBox.this, "False proportion must be between 0 and 1", "Input Error", JOptionPane.WARNING_MESSAGE);
            pPanel.falsePropField.requestFocus();
            pPanel.falsePropField.selectAll();
            return false;          
        }
        return true;
    }       

    public int getCorrectionMethod() {
        int method = JUST_ALPHA;
        if (pPanel.falseNumButton.isSelected()) {
            method = FALSE_NUM;
        } else if (pPanel.falsePropButton.isSelected()) {
            method = FALSE_PROP;
        }
        
        return method;
    }
    
    
    public static void main(String[] args) {
        JFrame dummyFrame = new JFrame();
        Vector<String> dummyVect = new Vector<String>();
        for (int i = 0; i < 10; i++) {
            dummyVect.add("Expt " + i);
        }
        
        RPInitBox oBox = new RPInitBox(dummyFrame, true, dummyVect, null);
        oBox.setVisible(true);
        
    }
}
