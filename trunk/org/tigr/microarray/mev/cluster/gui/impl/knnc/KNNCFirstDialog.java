/*
 * KNNCFirstDialog.java
 *
 * Created on September 22, 2003, 3:27 PM
 */

package org.tigr.microarray.mev.cluster.gui.impl.knnc;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import org.tigr.graph.*;
import org.tigr.util.*;
import org.tigr.util.awt.*;

import org.tigr.microarray.mev.cluster.gui.impl.dialogs.*;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.*;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.Experiment;

/**
 *
 * @author  nbhagaba
 */
public class KNNCFirstDialog extends AlgorithmDialog {
    
    boolean okPressed = false;
    JRadioButton genesButton, expsButton, createNewTrgSetButton, useExistTrgSetButton;
    JCheckBox useVarianceFilterBox, useCorrelFilterBox;
    JTextField numGenesField, pValueField, numClassesField, numNeighborsField, numPermsField;
    JLabel varLabel, corrLabel, numPermsLabel;
    
    HCLSelectionPanel hclOpsPanel;
    
    IFramework framework;
    Experiment experiment;
    
    KNNClassificationEditor knnEditor;
    
    int numExps, numGenes;
    
    /** Creates a new instance of KNNCFirstDialog */
    public KNNCFirstDialog(JFrame parentFrame, boolean modality, IFramework framework_1) {
        super(parentFrame, "KNN Classification", modality);
        okButton.setText("Next >");
        setBounds(0, 0, 550, 600);
        setBackground(Color.white);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        this.framework = framework_1;
        this.experiment = framework.getData().getExperiment();
        //this.knnEditor = new KNNClassificationEditor(framework, true, 5);
        
        numExps = experiment.getNumberOfSamples();
        numGenes = experiment.getNumberOfGenes();
        
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(gridbag);
        
        /*
        JPanel classifyPanel = new JPanel();
        GridBagLayout classGrid = new GridBagLayout();
        classifyPanel.setLayout(classGrid);
         */
        
        useVarianceFilterBox = new JCheckBox("Use variance filter (if unchecked, use all genes)", false);
        useCorrelFilterBox = new JCheckBox("Use correlation filter", false);
        
        JPanel genesOrExpsPanel = new JPanel();
        genesOrExpsPanel.setBackground(Color.white);
        genesOrExpsPanel.setBorder(new TitledBorder("Classify genes or experiments"));
        GridBagLayout grid1 = new GridBagLayout();
        genesOrExpsPanel.setLayout(grid1);
        
        genesButton = new JRadioButton("Classify genes", true);
        genesButton.setBackground(Color.white);
        genesButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                useVarianceFilterBox.setText("Use variance filter (if unchecked, use all genes)");
                //useCorrelFilterBox.setText("Use correlation filter (if unchecked, use all genes)");
                varLabel.setText("          Use only the following number of highest-variance genes: ");
            }
        });
        expsButton = new JRadioButton("Classify experiments", false);
        expsButton.setBackground(Color.white);
        expsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                useVarianceFilterBox.setText("Use variance filter (if unchecked, use all expts)");
                varLabel.setText("          Use only the following number of highest-variance expts: ");
                //useCorrelFilterBox.setText("Use correlation filter (if unchecked, use all expts)");
            }
        });
        ButtonGroup genesOrExpsGroup = new ButtonGroup();
        genesOrExpsGroup.add(genesButton);
        genesOrExpsGroup.add(expsButton);
        
        buildConstraints(constraints, 0, 0, 1, 1, 50, 100);
        grid1.setConstraints(genesButton, constraints);
        genesOrExpsPanel.add(genesButton);
        
        buildConstraints(constraints, 1, 0, 1, 1, 50, 0);
        grid1.setConstraints(expsButton, constraints);
        genesOrExpsPanel.add(expsButton);
        
        buildConstraints(constraints, 0, 0, 1, 1, 100, 10);
        constraints.fill = GridBagConstraints.BOTH;
        gridbag.setConstraints(genesOrExpsPanel, constraints);
        mainPanel.add(genesOrExpsPanel);
        
        
        constraints.fill = GridBagConstraints.NONE;
        
        JPanel variancePanel = new JPanel();
        variancePanel.setBackground(Color.white);
        variancePanel.setBorder(new TitledBorder("Variance filter"));
        GridBagLayout grid3 = new GridBagLayout();
        variancePanel.setLayout(grid3);
        
        //useVarianceFilterBox = new JCheckBox("Use variance filter (if unchecked, use all vectors)", false);
        useVarianceFilterBox.setBackground(Color.white);
        varLabel = new JLabel("          Use only the following number of highest-variance genes: ");
        varLabel.setEnabled(false);
        numGenesField = new JTextField("1000",7);
        numGenesField.setEnabled(false);
        numGenesField.setBackground(Color.gray);
        
        useVarianceFilterBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent evt) {
                if (evt.getStateChange() == ItemEvent.DESELECTED) {
                    varLabel.setEnabled(false);
                    numGenesField.setEnabled(false);
                    numGenesField.setBackground(Color.gray);
                } else {
                    varLabel.setEnabled(true);
                    numGenesField.setEnabled(true);
                    numGenesField.setBackground(Color.white);
                }
            }
        });
        
        constraints.anchor = GridBagConstraints.WEST;
        buildConstraints(constraints, 0, 0, 1, 1, 100, 50);
        grid3.setConstraints(useVarianceFilterBox, constraints);
        variancePanel.add(useVarianceFilterBox);
        
        //JLabel varLabel = new JLabel("Use only the following number of highest-variance vectors: ");
        buildConstraints(constraints, 0, 1, 1, 1, 70, 50);
        grid3.setConstraints(varLabel, constraints);
        variancePanel.add(varLabel);
        
        //numGenesField = new JTextField(7);
        buildConstraints(constraints, 1, 1, 1, 1, 30, 0);
        grid3.setConstraints(numGenesField, constraints);
        variancePanel.add(numGenesField);
        
        //constraints.anchor = GridBagConstraints.CENTER;
        
        buildConstraints(constraints, 0, 1, 1, 1, 100, 20);
        constraints.fill = GridBagConstraints.BOTH;
        gridbag.setConstraints(variancePanel, constraints);
        mainPanel.add(variancePanel);
        
        constraints.fill = GridBagConstraints.NONE;
        
        
        JPanel correlPanel = new JPanel();
        correlPanel.setBackground(Color.white);
        correlPanel.setBorder(new TitledBorder("Correlation filter"));
        GridBagLayout grid2 = new GridBagLayout();
        correlPanel.setLayout(grid2);
        
        //useCorrelFilterBox = new JCheckBox("Use correlation filter", false);
        useCorrelFilterBox.setBackground(Color.white);
        corrLabel = new JLabel("          Cutoff p-value for correlation: ");
        corrLabel.setEnabled(false);
        numPermsLabel = new JLabel("          Number of permutations for correlation test: ");
        numPermsLabel.setEnabled(false);
        pValueField = new JTextField("0.01",7);
        pValueField.setEnabled(false);
        pValueField.setBackground(Color.gray);
        numPermsField = new JTextField("1000",7);
        numPermsField.setEnabled(false);
        numPermsField.setBackground(Color.gray);
        
        useCorrelFilterBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent evt) {
                if (evt.getStateChange() == ItemEvent.DESELECTED) {
                    corrLabel.setEnabled(false);
                    numPermsLabel.setEnabled(false);
                    pValueField.setEnabled(false);
                    pValueField.setBackground(Color.gray);
                    numPermsField.setEnabled(false);
                    numPermsField.setBackground(Color.gray);
                } else {
                    corrLabel.setEnabled(true);
                    numPermsLabel.setEnabled(true);
                    pValueField.setEnabled(true);
                    pValueField.setBackground(Color.white);
                    numPermsField.setEnabled(true);
                    numPermsField.setBackground(Color.white);
                }
            }
        });
        
        buildConstraints(constraints, 0, 0, 1, 1, 100, 34);
        grid2.setConstraints(useCorrelFilterBox, constraints);
        correlPanel.add(useCorrelFilterBox);
        
        //corrLabel = new JLabel("Cutoff p-value for correlation: ");
        buildConstraints(constraints, 0, 1, 1, 1, 70, 33);
        grid2.setConstraints(corrLabel, constraints);
        correlPanel.add(corrLabel);
        
        //pValueField = new JTextField(7);
        buildConstraints(constraints, 1, 1, 1, 1, 30, 0);
        grid2.setConstraints(pValueField, constraints);
        correlPanel.add(pValueField);
        
        buildConstraints(constraints, 0, 2, 1, 1, 70, 33);
        grid2.setConstraints(numPermsLabel, constraints);
        correlPanel.add(numPermsLabel);
        
        //pValueField = new JTextField(7);
        buildConstraints(constraints, 1, 2, 1, 1, 30, 0);
        grid2.setConstraints(numPermsField, constraints);
        correlPanel.add(numPermsField);
        
        buildConstraints(constraints, 0, 2, 1, 1, 100, 20);
        constraints.fill = GridBagConstraints.BOTH;
        gridbag.setConstraints(correlPanel, constraints);
        mainPanel.add(correlPanel);
        
        constraints.fill = GridBagConstraints.NONE;
        
        
        JPanel knnParamPanel = new JPanel();
        knnParamPanel.setBackground(Color.white);
        knnParamPanel.setBorder(new TitledBorder("KNN classification parameters"));
        GridBagLayout grid4 = new GridBagLayout();
        knnParamPanel.setLayout(grid4);
        
        JLabel numClassesLabel = new JLabel("          Number of classes");
        buildConstraints(constraints, 0, 0, 1, 1, 50, 50);
        grid4.setConstraints(numClassesLabel, constraints);
        knnParamPanel.add(numClassesLabel);
        
        numClassesField = new JTextField("5", 7);
        buildConstraints(constraints, 1, 0, 1, 1, 50, 0);
        grid4.setConstraints(numClassesField, constraints);
        knnParamPanel.add(numClassesField);
        
        JLabel numNeighborsLabel = new JLabel("          Number of neighbors");
        buildConstraints(constraints, 0, 1, 1, 1, 50, 50);
        grid4.setConstraints(numNeighborsLabel, constraints);
        knnParamPanel.add(numNeighborsLabel);
        
        numNeighborsField = new JTextField("3", 7);
        buildConstraints(constraints, 1, 1, 1, 1, 50, 0);
        grid4.setConstraints(numNeighborsField, constraints);
        knnParamPanel.add(numNeighborsField);
        
        buildConstraints(constraints, 0, 3, 1, 1, 100, 20);
        constraints.fill = GridBagConstraints.BOTH;
        gridbag.setConstraints(knnParamPanel, constraints);
        mainPanel.add(knnParamPanel);
        
        constraints.fill = GridBagConstraints.NONE;
        
        JPanel makeTrainingSetPanel = new JPanel();
        makeTrainingSetPanel.setBackground(Color.white);
        makeTrainingSetPanel.setBorder(new TitledBorder("Create / import training set"));
        GridBagLayout grid5 = new GridBagLayout();
        makeTrainingSetPanel.setLayout(grid5);
        
        createNewTrgSetButton = new JRadioButton("Create new training set from data", true);
        createNewTrgSetButton.setBackground(Color.white);
        useExistTrgSetButton = new JRadioButton("Use previously created training set from file", false);
        useExistTrgSetButton.setBackground(Color.white);
        
        ButtonGroup newOrExisting = new ButtonGroup();
        newOrExisting.add(createNewTrgSetButton);
        newOrExisting.add(useExistTrgSetButton);
        
        buildConstraints(constraints, 0, 0, 1, 1, 100, 50);
        constraints.anchor = GridBagConstraints.CENTER;
        grid5.setConstraints(createNewTrgSetButton, constraints);
        makeTrainingSetPanel.add(createNewTrgSetButton);
        
        buildConstraints(constraints, 0, 1, 1, 1, 0, 50);
        grid5.setConstraints(useExistTrgSetButton, constraints);
        makeTrainingSetPanel.add(useExistTrgSetButton);
        
        buildConstraints(constraints, 0, 4, 1, 1, 100, 20);
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.BOTH;
        gridbag.setConstraints(makeTrainingSetPanel, constraints);
        mainPanel.add(makeTrainingSetPanel);
        
        constraints.fill = GridBagConstraints.NONE;
        
        hclOpsPanel = new HCLSelectionPanel();
        buildConstraints(constraints, 0, 5, 1, 1, 100, 10);
        constraints.fill = GridBagConstraints.BOTH;
        gridbag.setConstraints(hclOpsPanel, constraints);
        mainPanel.add(hclOpsPanel);
        
        constraints.fill = GridBagConstraints.NONE;
        
        addContent(mainPanel);
        EventListener listener = new EventListener();
        setActionListeners(listener);
        this.addWindowListener(listener);
    }
    
    public void setVisible(boolean visible) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);
        
        super.setVisible(visible);
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
    
    public boolean drawTrees() {
        return this.hclOpsPanel.isHCLSelected();
    }
    
    public boolean classifyGenes() {
        return genesButton.isSelected();
    }
    
    public int getNumClasses() {
        String s = numClassesField.getText();
        return Integer.parseInt(s);
    }
    
    public boolean createNewTrgSet() {
        return createNewTrgSetButton.isSelected();
    }
    
    public boolean useVarianceFilter() {
        return useVarianceFilterBox.isSelected();
    }
    
    public int getNumVectors() {
        return Integer.parseInt(numGenesField.getText());
    }
    
    public boolean useCorrelFilter() {
        return useCorrelFilterBox.isSelected();
    }
    
    public double getCorrPValue() {
        return Double.parseDouble(pValueField.getText());
    }
    
    public int getNumPerms() {
        return Integer.parseInt(numPermsField.getText());
    }
    
    public int getNumNeighbors() {
        return Integer.parseInt(numNeighborsField.getText());
    }
    
    public class EventListener extends WindowAdapter implements ActionListener{
        
        public void actionPerformed(ActionEvent ae) {
            String command = ae.getActionCommand();
            if(command.equals("ok-command")){
                try {
                    if (useVarianceFilterBox.isSelected()) {
                        int val = (new Integer(numGenesField.getText())).intValue();
                        if (genesButton.isSelected()) {
                            if (val <= 0 || val > numGenes) {
                                JOptionPane.showMessageDialog(KNNCFirstDialog.this, "Number of highest-variance genes must be > 0 and <= " + numGenes, "Error!", JOptionPane.WARNING_MESSAGE);
                                return;
                            }
                        } else {
                            if (val <= 0 || val > numExps) {
                                JOptionPane.showMessageDialog(KNNCFirstDialog.this, "Number of highest-variance expts must be > 0 and <= " + numExps, "Error!", JOptionPane.WARNING_MESSAGE);
                                return;
                            }
                        }
                    }
                    
                    if (useCorrelFilterBox.isSelected()) {
                        double val2 = (new Double(pValueField.getText())).doubleValue();
                        if (val2 < 0 || val2 > 1) {
                            JOptionPane.showMessageDialog(KNNCFirstDialog.this, "Correlation p-value must be between 0 and 1", "Error!", JOptionPane.WARNING_MESSAGE);
                            return;
                        }
                        double validateNumPerms = (new Double(numPermsField.getText())).doubleValue();
                        if (validateNumPerms <= 0) {
                            JOptionPane.showMessageDialog(KNNCFirstDialog.this, "Number of permutations must be > 0", "Error!", JOptionPane.WARNING_MESSAGE);
                            return;
                        }
                    }
                    
                    int val3 = (new Integer(numClassesField.getText())).intValue();
                    if ( (val3 <= 0 || val3 >= numGenes) && (genesButton.isSelected()) ) {
                        JOptionPane.showMessageDialog(KNNCFirstDialog.this, "Number of classes must be > 0 and < " + numGenes, "Error!", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    if ( (val3 <= 0 || val3 >= numExps) && (expsButton.isSelected()) ) {
                        JOptionPane.showMessageDialog(KNNCFirstDialog.this, "Number of classes must be > 0 and < " + numExps, "Error!", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    int val4 = (new Integer(numNeighborsField.getText())).intValue();
                    if (genesButton.isSelected()) {
                        if (val4 <= 0 || val4 > numGenes) {
                            JOptionPane.showMessageDialog(KNNCFirstDialog.this, "Number of neighbors must be > 0 and <= " + numGenes, "Error!", JOptionPane.WARNING_MESSAGE);
                            return;
                        }
                    } else {
                        if (val4 <= 0 || val4 > numExps) {
                            JOptionPane.showMessageDialog(KNNCFirstDialog.this, "Number of neighbors must be > 0 and <= " + numExps, "Error!", JOptionPane.WARNING_MESSAGE);
                            return;
                        }
                    }
                    okPressed = true;
                    dispose();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(KNNCFirstDialog.this, "Invalid format for at least one parameter", "Error!", JOptionPane.WARNING_MESSAGE);
                }
                //okPressed = true;
                //dispose();
            } else if (command.equals("reset-command")) {
                okPressed = false;
                genesButton.setSelected(true);
                expsButton.setSelected(false);
                useVarianceFilterBox.setSelected(false);
                useCorrelFilterBox.setSelected(false);
                useVarianceFilterBox.setText("Use variance filter (if unchecked, use all genes)");
                //useCorrelFilterBox.setText("Use correlation filter (if unchecked, use all genes)");
                varLabel.setText("          Use only the following number of highest-variance genes: ");
                numGenesField.setText("1000");
                pValueField.setText("0.01");
                numPermsField.setText("1000");
                numClassesField.setText("5");
                numNeighborsField.setText("3");
                //trgSetFromCurrent.setSelected(true);
                //createNewTrgSetButton.setSelected(true);
                //annotSelectBox.setSelectedIndex(0);
                hclOpsPanel.setHCLSelected(false);
            } else if (command.equals("cancel-command")) {
                okPressed = false;
                dispose();
            } else if (command.equals("info-command")){
                HelpWindow hw = new HelpWindow(KNNCFirstDialog.this, "KNNC Parameter Selections");
                okPressed = false;
                if(hw.getWindowContent()){
                    hw.setSize(450,600);
                    hw.setLocation();
                    hw.show();
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
}
