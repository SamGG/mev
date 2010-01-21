/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: SOTAInitDialog.java,v $
 * $Revision: 1.3 $
 * $Date: 2005-03-10 20:22:06 $
 * $Author: braistedj $
 * $State: Exp $
 */

package org.tigr.microarray.mev.cluster.gui.impl.sota;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;

import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.UIManager;

import org.tigr.microarray.mev.cluster.algorithm.Algorithm;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DistanceMetricPanel;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.HCLSelectionPanel;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.SampleSelectionPanel;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.HelpWindow;

/**
 *
 * @author  braisted
 */
public class SOTAInitDialog extends AlgorithmDialog {
    
    public int result = JOptionPane.CANCEL_OPTION;
    private float initDiv = 0.01f;
    
    private SampleSelectionPanel sampleSelectionPanel;
    private javax.swing.ButtonGroup divideCritButtonGroup;
    private javax.swing.JPanel parameters;
    private javax.swing.JPanel growthCritPanel;
    private javax.swing.JLabel maxCyclesLabel;
    private javax.swing.JLabel maxDivLabel;
    private javax.swing.JTextField maxTreeDiv;
    private javax.swing.JTextField stopEpochCriteria;
    private javax.swing.JLabel epochImpLabel;
    private javax.swing.JTextField maxCyclesText;
    private javax.swing.JCheckBox runMaxCycles;
    private javax.swing.JTextField maxEpPerCycleText;
    private javax.swing.JLabel jLabel52;
    private javax.swing.JCheckBox runIterative;
    private javax.swing.JPanel migCritPanel;
    private javax.swing.JLabel jLabel57;
    private javax.swing.JTextField migFactor_w;
    private javax.swing.JLabel jLabel58;
    private javax.swing.JLabel jLabel49;
    private javax.swing.JTextField migFactor_s;
    private javax.swing.JTextField migFactor_p;
    private java.awt.Choice levelChoice;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel cellDivPanel;
    private javax.swing.JRadioButton useClusterDiversity;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JRadioButton useClusterVariance;
    private javax.swing.JLabel pValueLabel;
    private javax.swing.JTextField pValue;
    private HCLSelectionPanel hclOpsPanel;
    
    private DistanceMetricPanel metricPanel;
    private String globalMetricName;
    private boolean globalAbsoluteSetting;
    
    /** Creates new form JSOTAInitDialog */
    public SOTAInitDialog(java.awt.Frame parent, int factor, String globalMetricName, boolean globalAbsoluteSetting) {
        super(parent, "SOTA: Self Organizing Tree Algorithm", true);

        this.globalMetricName = globalMetricName;
        this.globalAbsoluteSetting = globalAbsoluteSetting;
        
        initComponents();
        
        if(factor == -1){
            maxTreeDiv.setText("0.90");
            initDiv = 0.90f;
        }
        //use for future feature
        this.runIterative.setVisible(false);
        
        Listener listener = new Listener();
        this.addWindowListener(listener);
        setActionListeners(listener);
        metricPanel.addActionListener(listener);
        
        this.useClusterVariance.addItemListener(listener);
        setSize(580,630);
      //pack();
    }
    
    private void updateMaxTreeDiversity() {
        int function = metricPanel.getMetricIndex();
        int factor;
        
        if ((function==Algorithm.PEARSON)           ||
        (function==Algorithm.PEARSONUNCENTERED) ||
        (function==Algorithm.PEARSONSQARED)     ||
        (function==Algorithm.COSINE)            ||
        (function==Algorithm.COVARIANCE)        ||
        (function==Algorithm.DOTPRODUCT)        ||
        (function==Algorithm.SPEARMANRANK)      ||
        (function==Algorithm.KENDALLSTAU)) {
            factor = -1;
        } else {
            factor = 1;
        }
        
         
        if(factor == -1){
            maxTreeDiv.setText("0.90");
            initDiv = 0.90f;
        } else {
            maxTreeDiv.setText("0.01");            
            
        }
    }
    
    private void initComponents() {
        
        sampleSelectionPanel = new SampleSelectionPanel(Color.white, UIManager.getColor("Label.foreground"), true, "Sample Selection");
        
        metricPanel = new DistanceMetricPanel(globalMetricName, globalAbsoluteSetting, "Pearson Correlation", "SOTA", true, true);
                
        maxCyclesLabel = new javax.swing.JLabel();
        maxDivLabel = new javax.swing.JLabel();
        maxTreeDiv = new javax.swing.JTextField();
        maxTreeDiv.setMinimumSize(new Dimension(100,20));
        stopEpochCriteria = new javax.swing.JTextField();
        stopEpochCriteria.setMinimumSize(new Dimension(100,20));
        epochImpLabel = new javax.swing.JLabel();
        maxCyclesText = new javax.swing.JTextField();
        maxCyclesText.setMinimumSize(new Dimension(100,20));
        runMaxCycles = new javax.swing.JCheckBox();
        maxEpPerCycleText = new javax.swing.JTextField();
        maxEpPerCycleText.setMinimumSize(new Dimension(100,20));
        jLabel52 = new javax.swing.JLabel();
        runIterative = new javax.swing.JCheckBox();
        migCritPanel = new javax.swing.JPanel();
        jLabel57 = new javax.swing.JLabel();
        migFactor_w = new javax.swing.JTextField();
        migFactor_w.setMinimumSize(new Dimension(100,20));
        jLabel58 = new javax.swing.JLabel();
        jLabel49 = new javax.swing.JLabel();
        migFactor_s = new javax.swing.JTextField();
        migFactor_s.setMinimumSize(new Dimension(100,20));
        migFactor_p = new javax.swing.JTextField();
        migFactor_p.setMinimumSize(new Dimension(100,20));
        levelChoice = new java.awt.Choice();
        for(int i = 5; i >= 0; i--){
            this.levelChoice.addItem(String.valueOf(i));
        }
        this.levelChoice.select("5");
        jLabel1 = new javax.swing.JLabel();
        cellDivPanel = new javax.swing.JPanel();
        useClusterDiversity = new javax.swing.JRadioButton();
        jPanel1 = new javax.swing.JPanel();
        useClusterVariance = new javax.swing.JRadioButton();
        pValueLabel = new javax.swing.JLabel();
        pValue = new javax.swing.JTextField();
        pValue.setMinimumSize(new Dimension(20,20));
        hclOpsPanel = new HCLSelectionPanel();
        
        
        //GROWTH CRITERIA PANEL
        growthCritPanel = new javax.swing.JPanel();
        growthCritPanel.setLayout(new GridBagLayout());
        growthCritPanel.setBackground(Color.white);
        growthCritPanel.setBorder(new javax.swing.border.TitledBorder(new javax.swing.border.LineBorder(Color.gray), "Growth Termination Criteria", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 1, 12), Color.black));
        
        maxCyclesLabel.setText("Max. Cycles");
        maxCyclesLabel.setToolTipText("Maximum training cycles (final cluster number = MaxCycles +1)");
        
        maxDivLabel.setText("Max. Cell Diversity");
        maxDivLabel.setToolTipText("Training will stop when the most diverse cell crosses this limit. (for Euclidean diversity approaches 0, for Pearson diversity approaches 1.0)  ");
        
        maxTreeDiv.setColumns(6);
        maxTreeDiv.setText("0.01");
        
        stopEpochCriteria.setColumns(6);
        stopEpochCriteria.setText("0.0001");
        
        epochImpLabel.setText("Min. Epoch Error Improvement");
        epochImpLabel.setToolTipText("If improvement in relative tree error changes by less than this, current cycle ends");
        
        maxCyclesText.setColumns(6);
        maxCyclesText.setText("10");
        
        runMaxCycles.setText("Run Maximum  Number of Cycles (unrestricted growth)");
        runMaxCycles.setBackground(Color.white);
        runMaxCycles.setForeground(UIManager.getColor("Label.foreground"));
        runMaxCycles.setAlignmentX(JCheckBox.CENTER_ALIGNMENT);
        runMaxCycles.setFocusPainted(false);
        
        maxEpPerCycleText.setColumns(6);
        maxEpPerCycleText.setText("1000");
        
        jLabel52.setText("Max. epochs/cycle");
        jLabel52.setToolTipText("Maximum training epochs per cycle");
     //   growthCritPanel.add(runIterative);
        
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });
        
        growthCritPanel.add(maxCyclesLabel, new GridBagConstraints(0,0,1,1,1.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,30,5,0),0,0));
        growthCritPanel.add(maxCyclesText, new GridBagConstraints(1,0,1,1,0.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,5,5,0),0,0));
        growthCritPanel.add(maxDivLabel, new GridBagConstraints(2,0,1,1,1.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,40,5,0),0,0));
        growthCritPanel.add(maxTreeDiv, new GridBagConstraints(3,0,1,1,0.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,5,5,20),0,0));
        growthCritPanel.add(jLabel52, new GridBagConstraints(0,1,1,1,1.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,30,0,0),0,0));
        growthCritPanel.add(maxEpPerCycleText, new GridBagConstraints(1,1,1,1,0.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,5,0,0),0,0));
        growthCritPanel.add(epochImpLabel, new GridBagConstraints(2,1,1,1,1.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,40,0,0),0,0));
        growthCritPanel.add(stopEpochCriteria, new GridBagConstraints(3,1,1,1,0.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,5,0,20),0,0));
        growthCritPanel.add(runMaxCycles, new GridBagConstraints(0,2,4,1,0.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(5,90,0,10),0,0));
        
        migCritPanel.setLayout(new GridBagLayout());
        migCritPanel.setBorder(new javax.swing.border.TitledBorder(new javax.swing.border.EtchedBorder(), "Cetroid Migration and Neighborhood Parameters", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 1, 12), Color.black));
        migCritPanel.setBackground(Color.white);
        
        jLabel57.setText("Winning Cell Migration Weight");
        jLabel57.setToolTipText("Factor to apply to winning cell migration");
        migFactor_w.setColumns(5);
        migFactor_w.setText("0.01");
        
        jLabel58.setText("Parent Cell Migration Weight");
        jLabel58.setToolTipText("Factor to apply to parent cell migration");
        migFactor_p.setColumns(5);
        migFactor_p.setText("0.005");
        
        jLabel49.setText("Sister Cell Migration Weight");
        jLabel49.setToolTipText("Factor to apply to sister cell migration");
        migFactor_s.setColumns(5);
        migFactor_s.setText("0.001");
        
        JPanel pPanel = new JPanel();
        pPanel.setBackground(Color.white);
        pPanel.setLayout(new GridBagLayout());
        pPanel.add(jLabel1, new GridBagConstraints(0,0,1,1,1.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0));
        pPanel.add(levelChoice, new GridBagConstraints(1,0,1,1,1.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,15,0,20),0,0));
        
        jLabel1.setText("Neighborhood Level");
        jLabel1.setToolTipText("Determines extent of redistribution neighborhood");
        
        levelChoice.setSize(50, 35);
        
        migCritPanel.add(jLabel57, new GridBagConstraints(0,0,1,1,0.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(5,0,5,0),0,0) );
        migCritPanel.add(jLabel58, new GridBagConstraints(0,1,1,1,0.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,0,5,0),0,0));
        migCritPanel.add(jLabel49, new GridBagConstraints(0,2,1,1,0.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,0,5,0),0,0));
        migCritPanel.add(migFactor_w, new GridBagConstraints(1,0,1,1,0.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(5,25,5,20),0,0));
        migCritPanel.add(migFactor_p, new GridBagConstraints(1,1,1,1,0.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,25,5,20),0,0));
        migCritPanel.add(migFactor_s, new GridBagConstraints(1,2,1,1,0.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,25,5,20),0,0));
        migCritPanel.add(pPanel, new GridBagConstraints(2,0,1,3,0.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0));
        
        cellDivPanel.setLayout(new GridBagLayout());
        cellDivPanel.setBackground(Color.white);
        cellDivPanel.setBorder(new javax.swing.border.TitledBorder(new javax.swing.border.EtchedBorder(), "Cell Division Criteria", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 1, 12), Color.black));
        
        useClusterDiversity.setToolTipText("Use cell resource to determin cell to divide");
        useClusterDiversity.setSelected(true);
        useClusterDiversity.setText("Use Cell Diversity  ( mean dist(gene,centroid) )");
        useClusterDiversity.setAlignmentX(JRadioButton.CENTER_ALIGNMENT);
        useClusterDiversity.setBackground(Color.white);
        useClusterDiversity.setFocusPainted(false);
        useClusterDiversity.setForeground(UIManager.getColor("Label.foreground"));
        divideCritButtonGroup = new javax.swing.ButtonGroup();
        divideCritButtonGroup.add(useClusterDiversity);
        
        jPanel1.setLayout(new GridBagLayout());
        jPanel1.setBackground(Color.white);
        
        useClusterVariance.setToolTipText("Use variance and p Value to stop cell division");
        useClusterVariance.setText("Use Cell Variability ( max( dist(g(i), g(j)) ) )");
        useClusterVariance.setBackground(Color.white);
        useClusterVariance.setFocusPainted(false);
        useClusterVariance.setForeground(UIManager.getColor("Label.foreground"));
        
        divideCritButtonGroup.add(useClusterVariance);
        
        pValueLabel.setText("p Value");
        pValueLabel.setBackground(Color.white);
        pValue.setText("0.05");
        pValue.setEnabled(false);
        
        jPanel1.add(useClusterVariance, new GridBagConstraints(0,0,1,1,0.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,0,0,30),0,0));
        jPanel1.add(pValueLabel, new GridBagConstraints(1,0,1,1,0.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,20,0,0),0,0));
        jPanel1.add(pValue, new GridBagConstraints(2,0,1,1,0.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,10,0,30),0,0));
        
        cellDivPanel.add(useClusterDiversity, new GridBagConstraints(0,0,3,1,1.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,90,0,0),0,0));
        cellDivPanel.add(jPanel1, new GridBagConstraints(0,1,3,1,1.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,10,5,10),0,0));
        
        //Main Parameter panel
        parameters = new javax.swing.JPanel();
        parameters.setLayout(new GridBagLayout());
        parameters.setPreferredSize(new java.awt.Dimension(520, 412));
        parameters.setMaximumSize(new java.awt.Dimension(32767, 690));
        parameters.add(sampleSelectionPanel, new GridBagConstraints(0,0,1,1,1.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0));
        parameters.add(metricPanel, new GridBagConstraints(0,1,1,1,1.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0)); 
        parameters.add(growthCritPanel, new GridBagConstraints(0,2,1,1,1.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0));
        parameters.add(migCritPanel, new GridBagConstraints(0,3,1,1,1.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0));
        parameters.add(cellDivPanel, new GridBagConstraints(0,4,1,1,1.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0));
        parameters.add(hclOpsPanel, new GridBagConstraints(0,5,1,1,1.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0));
        
        addContent(parameters);
    }
    
    
    /** Closes the dialog */
    private void closeDialog(java.awt.event.WindowEvent evt) {
        setVisible(false);
        dispose();
    }
    
    /**
     * Shows the dialog.
     */
    public int showModal() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);
        show();
        return result;
    }
    
    /**
     * Retrieves integer values corresponding to supplied key
     */
    public int getInt(String key) throws NumberFormatException{
        
        String inputText;
        int value = 0;
        
        if(key.equals("maxCycles")){
            inputText = this.maxCyclesText.getText();
            value = (Integer.parseInt(inputText));
        }
        else if(key.equals("maxEpochsPerCycle")){
            inputText = this.maxEpPerCycleText.getText();
            value = (Integer.parseInt(inputText));
        }
        else if(key.equals("neighborhood-level")){
            value = Integer.parseInt(levelChoice.getSelectedItem());
        }
        return value;
    }
    
    
    /**
     * Returns the currently selected metric
     */
    public int getDistanceMetric() {
        return metricPanel.getMetricIndex();
    }
    
    /**
     *  Returns true if the absolute checkbox is selected, else false
     */
    public boolean isAbsoluteDistance() {
        return metricPanel.getAbsoluteSelection();
    }
    
    
    /**
     * Retrieves float values corresponding to supplied key
     */
    public float getFloat(String key) throws NumberFormatException{
        
        String inputText;
        float value = 0;
        
        if(key.equals("maxTreeDiv")){
            inputText = maxTreeDiv.getText();
            value = Float.parseFloat(inputText);
        }
        else if(key.equals("epochStopCriteria")){
            inputText = stopEpochCriteria.getText();
            value = Float.parseFloat(inputText);
        }
        else if(key.equals("migFactor_w")){
            inputText = migFactor_w.getText();
            value = Float.parseFloat(inputText);
        }
        else if(key.equals("migFactor_p")){
            inputText = migFactor_p.getText();
            value = Float.parseFloat(inputText);
        }
        else if(key.equals("migFactor_s")){
            inputText = migFactor_s.getText();
            value = Float.parseFloat(inputText);
        }
        else if(key.equals("pValue")){
            inputText = pValue.getText();
            value = Float.parseFloat(inputText);
        }
        return value;
    }
    
    /**
     * Retrieves boolean values corresponding to supplied key
     */
    public boolean getBoolean(String key){
        boolean value = false;
        
        if(key.equals("runToMaxCycles")){
            value = runMaxCycles.isSelected();
        }
        else if(key.equals("useVariance")){
            value = useClusterVariance.isSelected();
        }
        else if(key.equals("calcClusterHCL")){
            value = this.hclOpsPanel.isHCLSelected();
        }
        else if(key.equals("runIterative")){
            value = runIterative.isSelected();
        }
        else if(key.equals("clusterGenes")){
            value = this.sampleSelectionPanel.isClusterGenesSelected();
        }
        return value;
    }
    
    private void resetControls(){
        this.sampleSelectionPanel.setClusterGenesSelected(true);
        this.maxCyclesText.setText("10");
        this.maxTreeDiv.setText(Float.toString(this.initDiv));
        this.maxEpPerCycleText.setText("1000");
        this.stopEpochCriteria.setText("0.0001");
        this.runMaxCycles.setSelected(false);
        this.migFactor_w.setText("0.01");
        this.migFactor_p.setText("0.005");
        this.migFactor_s.setText("0.001");
        this.levelChoice.select(0);
        this.hclOpsPanel.setHCLSelected(false);
        this.useClusterDiversity.setSelected(true);
        this.pValue.setText("0.05");
        metricPanel.reset();
        updateMaxTreeDiversity();
    }
    
    private boolean validateValues(){
        boolean result = true;
        int i;
        float f;
        try{
            i = Integer.parseInt(this.maxCyclesText.getText());
            if(i <= 0){
                JOptionPane.showMessageDialog(this, "Input Value Error. Max cycles must be > 0.  Please try again.", "Input error", JOptionPane.WARNING_MESSAGE);
                this.maxCyclesText.requestFocus();
                this.maxCyclesText.selectAll();
                return false;
            }
            
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Input Format Error. "+nfe.getMessage()+" is not valid.  Please try again.", "Input error", JOptionPane.WARNING_MESSAGE);
            this.maxCyclesText.requestFocus();
            this.maxCyclesText.selectAll();
            return false;
        }
        try{
            i = Integer.parseInt(this.maxEpPerCycleText.getText());
            if(i <= 0){
                JOptionPane.showMessageDialog(this, "Input Value Error. Max epochs/cycle must be > 0.  Please try again.", "Input error", JOptionPane.WARNING_MESSAGE);
                this.maxEpPerCycleText.requestFocus();
                this.maxEpPerCycleText.selectAll();
                return false;
            }
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Input Format Error. "+nfe.getMessage()+" is not valid.  Please try again.", "Input error", JOptionPane.WARNING_MESSAGE);
            this.maxEpPerCycleText.requestFocus();
            this.maxEpPerCycleText.selectAll();
            return false;
        }
        if(this.useClusterDiversity.isSelected()){
            try{
                f = Float.parseFloat(this.maxTreeDiv.getText());
                if(f <= 0){
                    JOptionPane.showMessageDialog(this, "Input Value Error. Max Tree Diversity must be > 0.  Please try again.", "Input error", JOptionPane.WARNING_MESSAGE);
                    this.maxTreeDiv.requestFocus();
                    this.maxTreeDiv.selectAll();
                    return false;
                }
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(this, "Input Format Error. "+nfe.getMessage()+" is not valid.  Please try again.", "Input error", JOptionPane.WARNING_MESSAGE);
                this.maxTreeDiv.requestFocus();
                this.maxTreeDiv.selectAll();
                return false;
            }
        }
        
        try{
            f = Float.parseFloat(this.stopEpochCriteria.getText());
            if(f <= 0){
                JOptionPane.showMessageDialog(this, "Input Value Error. Epoch Improvment Limit  must be > 0.  Please try again.", "Input error", JOptionPane.WARNING_MESSAGE);
                this.stopEpochCriteria.requestFocus();
                this.stopEpochCriteria.selectAll();
                return false;
            }
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Input Format Error. "+nfe.getMessage()+" is not valid.  Please try again.", "Input error", JOptionPane.WARNING_MESSAGE);
            this.stopEpochCriteria.requestFocus();
            this.stopEpochCriteria.selectAll();
            return false;
        }
        
        
        try{
            f = Float.parseFloat(this.migFactor_w.getText());
            if(f <= 0){
                JOptionPane.showMessageDialog(this, "Input Value Error. Winning Cell Migration Weight must be > 0.  Please try again.", "Input error", JOptionPane.WARNING_MESSAGE);
                this.migFactor_w.requestFocus();
                this.migFactor_w.selectAll();
                return false;
            }
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Input Format Error. "+nfe.getMessage()+" is not valid.  Please try again.", "Input error", JOptionPane.WARNING_MESSAGE);
            this.migFactor_w.requestFocus();
            this.migFactor_w.selectAll();
            return false;
        }
        try{
            f = Float.parseFloat(this.migFactor_p.getText());
            if(f <= 0){
                JOptionPane.showMessageDialog(this, "Input Value Error. Parent Cell Migration Weight must be > 0.  Please try again.", "Input error", JOptionPane.WARNING_MESSAGE);
                this.migFactor_p.requestFocus();
                this.migFactor_p.selectAll();
                return false;
            }
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Input Format Error. "+nfe.getMessage()+" is not valid.  Please try again.", "Input error", JOptionPane.WARNING_MESSAGE);
            this.migFactor_p.requestFocus();
            this.migFactor_p.selectAll();
            return false;
        }
        try{
            f = Float.parseFloat(this.migFactor_s.getText());
            if(f <= 0){
                JOptionPane.showMessageDialog(this, "Input Value Error. Sister Cell Migration Weight must be > 0.  Please try again.", "Input error", JOptionPane.WARNING_MESSAGE);
                this.migFactor_s.requestFocus();
                this.migFactor_s.selectAll();
                return false;
            }
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Input Format Error. "+nfe.getMessage()+" is not valid.  Please try again.", "Input error", JOptionPane.WARNING_MESSAGE);
            this.migFactor_s.requestFocus();
            this.migFactor_s.selectAll();
            return false;
        }
        
        if(this.useClusterVariance.isSelected()){
            try{
                f = Float.parseFloat(this.pValue.getText());
                if(f <= 0 || f >=1.0){
                    JOptionPane.showMessageDialog(this, "Input Value Error. Alpha Value must be > 0 and < 1.0.  Please try again.", "Input error", JOptionPane.WARNING_MESSAGE);
                    pValue.requestFocus();
                    pValue.selectAll();
                    return false;
                }
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(this, "Input Format Error. "+nfe.getMessage()+" is not valid.  Please try again.", "Input error", JOptionPane.WARNING_MESSAGE);
                pValue.requestFocus();
                pValue.selectAll();
                return false;
            }
        }
        
        
        
        
        
        return result;
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        new SOTAInitDialog(new java.awt.Frame(), 1, "Euclidean Distance", false).show();
        System.exit(0);
    }
    
    protected void disposeDialog() {
    }
    
    /**
     * The class to listen to the dialog and check boxes items events.
     */
    private class Listener extends DialogListener implements ItemListener, ActionListener {
        
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            if (command.equals("ok-command")) {
                if(validateValues())
                    result = JOptionPane.OK_OPTION;
                else
                    return;
            } else if (command.equals("cancel-command")) {
                result = JOptionPane.CANCEL_OPTION;
            } else if (command.equals("reset-command")){
                result = JOptionPane.CANCEL_OPTION;
                resetControls();
                return;
            } else if (command.equals("info-command")){
                result = JOptionPane.CANCEL_OPTION;
                HelpWindow help = new HelpWindow(SOTAInitDialog.this, "SOTA Initialization Dialog");
                if(help.getWindowContent()){
                    help.setSize(500,650);
                    help.setLocation();
                    help.show();
                    return;
                }
                else{
                    help.dispose();
                    return;
                }
            } else if (e.getSource() == metricPanel) {
                updateMaxTreeDiversity();
                return;
            }
            dispose();
        }
        
        public void itemStateChanged(ItemEvent e) {
            
            if(useClusterVariance.isSelected()){
                pValue.setEnabled(true);
                maxTreeDiv.setEnabled(false);
                repaint();
            }
            else{
                pValue.setEnabled(false);
                maxTreeDiv.setEnabled(true);
                repaint();
            }
        }
        
        public void windowClosing(WindowEvent e) {
            result = JOptionPane.CLOSED_OPTION;
            dispose();
        }
    }
    
    
    
    
    
    
}
