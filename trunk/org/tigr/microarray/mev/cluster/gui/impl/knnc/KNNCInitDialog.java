/*
 * KNNCInitDialog.java
 *
 * Created on September 3, 2003, 2:32 PM
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
public class KNNCInitDialog extends AlgorithmDialog {
    
    boolean okPressed = false;
    JRadioButton genesButton, expsButton, createNewTrgSetButton, useExistTrgSetButton, trgSetFromCurrent, trgSetNotFromCurrent;
    JButton classEditorButton, browseTrgFileButton, previewTrgSetButton;
    JCheckBox useVarianceFilterBox, useCorrelFilterBox;
    JTextField numGenesField, pValueField, numClassesField, numNeighborsField, pathFileField;
    JComboBox annotSelectBox;
    JLabel varLabel, corrLabel, pathLabel, questionLabel;
    JPanel trgSetSpecPanel;
    JTabbedPane classifyOrValidatePane;
    
    HCLSelectionPanel hclOpsPanel;
    
    IFramework framework;
    Experiment experiment;
    KNNClassificationEditor knnEditor;
    
    int numExps, numGenes;
    
    /** Creates a new instance of KNNCInitDialog */
    public KNNCInitDialog(JFrame parentFrame, boolean modality, IFramework framework_1) {
        super(parentFrame, "KNN Classification", modality);
        setBounds(0, 0, 550, 800);
        setBackground(Color.white);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        this.framework = framework_1;
        this.experiment = framework.getData().getExperiment();
        this.knnEditor = new KNNClassificationEditor(framework, true, 5);
        
        numExps = experiment.getNumberOfSamples();
        numGenes = experiment.getNumberOfGenes();
        
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();
        //constraints.fill = GridBagConstraints.BOTH;
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(gridbag);
        
        JPanel classifyPanel = new JPanel();
        GridBagLayout classGrid = new GridBagLayout();
        classifyPanel.setLayout(classGrid);
        
        
        JPanel genesOrExpsPanel = new JPanel();
        genesOrExpsPanel.setBackground(Color.white);
        genesOrExpsPanel.setBorder(new TitledBorder("Classify genes or experiments"));
        GridBagLayout grid1 = new GridBagLayout();
        genesOrExpsPanel.setLayout(grid1);
        
        genesButton = new JRadioButton("Classify genes", true);
        genesButton.setBackground(Color.white);
        expsButton = new JRadioButton("Classify experiments", false);
        expsButton.setBackground(Color.white);
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
        classGrid.setConstraints(genesOrExpsPanel, constraints);
        classifyPanel.add(genesOrExpsPanel);
        
        constraints.fill = GridBagConstraints.NONE;
        
        JPanel variancePanel = new JPanel();
        variancePanel.setBackground(Color.white);
        variancePanel.setBorder(new TitledBorder("Variance filter"));
        GridBagLayout grid3 = new GridBagLayout();
        variancePanel.setLayout(grid3);
        
        useVarianceFilterBox = new JCheckBox("Use variance filter (if unchecked, use all vectors)", false);
        useVarianceFilterBox.setBackground(Color.white);
        varLabel = new JLabel("Use only the following number of highest-variance vectors: ");
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
        
        buildConstraints(constraints, 0, 1, 1, 1, 100, 10);
        constraints.fill = GridBagConstraints.BOTH;
        classGrid.setConstraints(variancePanel, constraints);
        classifyPanel.add(variancePanel);
        
        constraints.fill = GridBagConstraints.NONE;
        
        
        JPanel correlPanel = new JPanel();
        correlPanel.setBackground(Color.white);
        correlPanel.setBorder(new TitledBorder("Correlation filter"));
        GridBagLayout grid2 = new GridBagLayout();
        correlPanel.setLayout(grid2);
        
        useCorrelFilterBox = new JCheckBox("Use correlation filter", false);
        useCorrelFilterBox.setBackground(Color.white);
        corrLabel = new JLabel("Cutoff p-value for correlation: ");
        corrLabel.setEnabled(false);
        pValueField = new JTextField("0.01",7);
        pValueField.setEnabled(false);
        pValueField.setBackground(Color.gray);
        
        useCorrelFilterBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent evt) {
                if (evt.getStateChange() == ItemEvent.DESELECTED) {
                    corrLabel.setEnabled(false);
                    pValueField.setEnabled(false);
                    pValueField.setBackground(Color.gray);
                } else {
                    corrLabel.setEnabled(true);
                    pValueField.setEnabled(true);
                    pValueField.setBackground(Color.white);
                }
            }
        });
        
        buildConstraints(constraints, 0, 0, 1, 1, 100, 50);
        grid2.setConstraints(useCorrelFilterBox, constraints);
        correlPanel.add(useCorrelFilterBox);
        
        //corrLabel = new JLabel("Cutoff p-value for correlation: ");
        buildConstraints(constraints, 0, 1, 1, 1, 70, 50);
        grid2.setConstraints(corrLabel, constraints);
        correlPanel.add(corrLabel);
        
        //pValueField = new JTextField(7);
        buildConstraints(constraints, 1, 1, 1, 1, 30, 0);
        grid2.setConstraints(pValueField, constraints);
        correlPanel.add(pValueField);
        
        buildConstraints(constraints, 0, 2, 1, 1, 100, 10);
        constraints.fill = GridBagConstraints.BOTH;
        classGrid.setConstraints(correlPanel, constraints);
        classifyPanel.add(correlPanel);
        
        constraints.fill = GridBagConstraints.NONE;
        
        
        JPanel knnParamPanel = new JPanel();
        knnParamPanel.setBackground(Color.white);
        knnParamPanel.setBorder(new TitledBorder("KNN classification parameters"));
        GridBagLayout grid4 = new GridBagLayout();
        knnParamPanel.setLayout(grid4);
        
        JLabel numClassesLabel = new JLabel("Number of classes");
        buildConstraints(constraints, 0, 0, 1, 1, 50, 50);
        grid4.setConstraints(numClassesLabel, constraints);
        knnParamPanel.add(numClassesLabel);
        
        numClassesField = new JTextField("5", 7);
        buildConstraints(constraints, 1, 0, 1, 1, 50, 0);
        grid4.setConstraints(numClassesField, constraints);
        knnParamPanel.add(numClassesField);
        
        JLabel numNeighborsLabel = new JLabel("Number of neighbors");
        buildConstraints(constraints, 0, 1, 1, 1, 50, 50);
        grid4.setConstraints(numNeighborsLabel, constraints);
        knnParamPanel.add(numNeighborsLabel);
        
        numNeighborsField = new JTextField("3", 7);
        buildConstraints(constraints, 1, 1, 1, 1, 50, 0);
        grid4.setConstraints(numNeighborsField, constraints);
        knnParamPanel.add(numNeighborsField);
        
        buildConstraints(constraints, 0, 3, 1, 1, 100, 10);
        constraints.fill = GridBagConstraints.BOTH;
        classGrid.setConstraints(knnParamPanel, constraints);
        classifyPanel.add(knnParamPanel);
        
        constraints.fill = GridBagConstraints.NONE;
        
        
        JPanel makeTrainingSetPanel = new JPanel();
        makeTrainingSetPanel.setBackground(Color.white);
        makeTrainingSetPanel.setBorder(new TitledBorder("Create / import training set"));
        GridBagLayout grid5 = new GridBagLayout();
        makeTrainingSetPanel.setLayout(grid5);
        
        trgSetSpecPanel = new JPanel();
        trgSetSpecPanel.setBackground(Color.gray);
        trgSetSpecPanel.setBorder(new TitledBorder("Training file specifications"));
        GridBagLayout grid6 = new GridBagLayout();
        trgSetSpecPanel.setLayout(grid6);
        
        questionLabel = new JLabel("Was training file created from current data set?");
        buildConstraints(constraints, 0, 0, 1, 1, 100, 25);
        grid6.setConstraints(questionLabel, constraints);
        trgSetSpecPanel.add(questionLabel);
        
        String[] annotFields = {"field1", "field2", "field3", "field4"}; // for now
        annotSelectBox = new JComboBox(annotFields);
        annotSelectBox.setEnabled(false);
        
        trgSetFromCurrent = new JRadioButton("Yes", true);
        trgSetFromCurrent.setBackground(Color.gray);
        trgSetNotFromCurrent = new JRadioButton("No - select annotation column of current data set to match to: ", false);
        trgSetNotFromCurrent.setBackground(Color.gray);
        
        trgSetFromCurrent.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent evt) {
                if (evt.getStateChange() == ItemEvent.SELECTED) {
                    annotSelectBox.setEnabled(false);
                } else {
                    annotSelectBox.setEnabled(true);
                }
            }
        });
        
        ButtonGroup currentOrNot = new ButtonGroup();
        currentOrNot.add(trgSetFromCurrent);
        currentOrNot.add(trgSetNotFromCurrent);
        
        buildConstraints(constraints, 0, 1, 1, 1, 100, 25);
        grid6.setConstraints(trgSetFromCurrent, constraints);
        trgSetSpecPanel.add(trgSetFromCurrent);
        
        buildConstraints(constraints, 0, 2, 1, 1, 80, 25);
        grid6.setConstraints(trgSetNotFromCurrent, constraints);
        trgSetSpecPanel.add(trgSetNotFromCurrent);
        
        //String[] annotFields = {"field1", "field2", "field3", "field4"}; // for now
        //annotSelectBox = new JComboBox(annotFields);
        buildConstraints(constraints, 1, 2, 1, 1, 20, 0);
        grid6.setConstraints(annotSelectBox, constraints);
        trgSetSpecPanel.add(annotSelectBox);
        
        previewTrgSetButton = new JButton("View / modify training set assignments from file");
        constraints.anchor = GridBagConstraints.CENTER;
        buildConstraints(constraints, 0, 3, 1, 1, 100, 25);
        grid6.setConstraints(previewTrgSetButton, constraints);
        trgSetSpecPanel.add(previewTrgSetButton);
        
        
        
        createNewTrgSetButton = new JRadioButton("Create new training set from data   >>", true);
        createNewTrgSetButton.setBackground(Color.white);
        useExistTrgSetButton = new JRadioButton("Use previously created training set from file   >>", false);
        useExistTrgSetButton.setBackground(Color.white);
        
        classEditorButton = new JButton("Classificaton editor ...");
        classEditorButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                knnEditor = new KNNClassificationEditor(framework, classifyGenes(), getNumClasses());
                knnEditor.setVisible(true);
            }
        });
        browseTrgFileButton = new JButton("Browse files ...");
        pathLabel = new JLabel("Selected file: ");
        pathFileField = new JTextField(80);
        
        browseTrgFileButton.setEnabled(false);
        pathLabel.setEnabled(false);
        pathFileField.setEnabled(false);
        questionLabel.setEnabled(false);
        trgSetFromCurrent.setEnabled(false);
        trgSetNotFromCurrent.setEnabled(false);
        annotSelectBox.setEnabled(false);
        previewTrgSetButton.setEnabled(false);
        
        createNewTrgSetButton.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent evt) {
                if (evt.getStateChange() == ItemEvent.SELECTED) {
                    trgSetSpecPanel.setBackground(Color.gray);
                    browseTrgFileButton.setEnabled(false);
                    pathLabel.setEnabled(false);
                    pathFileField.setEnabled(false);
                    questionLabel.setEnabled(false);
                    trgSetFromCurrent.setEnabled(false);
                    trgSetFromCurrent.setBackground(Color.gray);
                    trgSetNotFromCurrent.setEnabled(false);
                    trgSetNotFromCurrent.setBackground(Color.gray);
                    annotSelectBox.setEnabled(false);
                    previewTrgSetButton.setEnabled(false);
                } else {
                    trgSetSpecPanel.setBackground(Color.white);
                    browseTrgFileButton.setEnabled(true);
                    pathLabel.setEnabled(true);
                    pathFileField.setEnabled(true);
                    questionLabel.setEnabled(true);
                    trgSetFromCurrent.setEnabled(true);
                    trgSetFromCurrent.setBackground(Color.white);
                    trgSetNotFromCurrent.setEnabled(true);
                    trgSetNotFromCurrent.setBackground(Color.white);
                    if (trgSetFromCurrent.isSelected()) {
                        annotSelectBox.setEnabled(false);
                    } else {
                        annotSelectBox.setEnabled(true);
                    }
                    previewTrgSetButton.setEnabled(true);
                }
            }
        });
        
        useExistTrgSetButton.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent evt) {
                if (evt.getStateChange() == ItemEvent.SELECTED) {
                    classEditorButton.setEnabled(false);
                } else {
                    classEditorButton.setEnabled(true);
                }
            }
        });
        
        ButtonGroup newOrExisting = new ButtonGroup();
        newOrExisting.add(createNewTrgSetButton);
        newOrExisting.add(useExistTrgSetButton);
        
        buildConstraints(constraints, 0, 0, 1, 1, 50, 10);
        grid5.setConstraints(createNewTrgSetButton, constraints);
        makeTrainingSetPanel.add(createNewTrgSetButton);
        
        //classEditorButton = new JButton("Classificaton editor ...");
        buildConstraints(constraints, 1, 0, 1, 1, 50, 0);
        grid5.setConstraints(classEditorButton, constraints);
        makeTrainingSetPanel.add(classEditorButton);
        
        buildConstraints(constraints, 0, 1, 1, 1, 50, 10);
        grid5.setConstraints(useExistTrgSetButton, constraints);
        makeTrainingSetPanel.add(useExistTrgSetButton);
        
        //browseTrgFileButton = new JButton("Browse files ...");
        buildConstraints(constraints, 1, 1, 1, 1, 50, 0);
        grid5.setConstraints(browseTrgFileButton, constraints);
        makeTrainingSetPanel.add(browseTrgFileButton);
        
        //pathLabel = new JLabel("Selected file: ");
        constraints.anchor = GridBagConstraints.WEST;
        buildConstraints(constraints, 0, 2, 1, 1, 100, 5);
        grid5.setConstraints(pathLabel, constraints);
        makeTrainingSetPanel.add(pathLabel);
        
        //pathFileField = new JTextField(60);
        pathFileField.setBackground(Color.lightGray);
        pathFileField.setEditable(false);
        pathFileField.setText("No file currently selected");
        constraints.fill = GridBagConstraints.BOTH;
        constraints.anchor = GridBagConstraints.WEST;
        buildConstraints(constraints, 0, 3, 2, 1, 100, 5);
        grid5.setConstraints(pathFileField, constraints);
        makeTrainingSetPanel.add(pathFileField);
        
        constraints.fill = GridBagConstraints.NONE;
        //constraints.anchor = GridBagConstraints.CENTER;
        
        /*
        JPanel trgSetSpecPanel = new JPanel();
        trgSetSpecPanel.setBackground(Color.white);
        trgSetSpecPanel.setBorder(new TitledBorder("Training file specifications"));
        GridBagLayout grid6 = new GridBagLayout();
        trgSetSpecPanel.setLayout(grid6);
         
        JLabel questionLabel = new JLabel("Was training file created from current data set?");
        buildConstraints(constraints, 0, 0, 1, 1, 100, 25);
        grid6.setConstraints(questionLabel, constraints);
        trgSetSpecPanel.add(questionLabel);
         
        trgSetFromCurrent = new JRadioButton("Yes", true);
        trgSetFromCurrent.setBackground(Color.white);
        trgSetNotFromCurrent = new JRadioButton("No - select annotation column of current data set to match to: ", false);
        trgSetNotFromCurrent.setBackground(Color.white);
        ButtonGroup currentOrNot = new ButtonGroup();
        currentOrNot.add(trgSetFromCurrent);
        currentOrNot.add(trgSetNotFromCurrent);
         
        buildConstraints(constraints, 0, 1, 1, 1, 100, 25);
        grid6.setConstraints(trgSetFromCurrent, constraints);
        trgSetSpecPanel.add(trgSetFromCurrent);
         
        buildConstraints(constraints, 0, 2, 1, 1, 80, 25);
        grid6.setConstraints(trgSetNotFromCurrent, constraints);
        trgSetSpecPanel.add(trgSetNotFromCurrent);
         
        String[] annotFields = {"field1", "field2", "field3", "field4"}; // for now
        annotSelectBox = new JComboBox(annotFields);
        buildConstraints(constraints, 1, 2, 1, 1, 20, 0);
        grid6.setConstraints(annotSelectBox, constraints);
        trgSetSpecPanel.add(annotSelectBox);
         
        previewTrgSetButton = new JButton("View / modify training set assignments from file");
        constraints.anchor = GridBagConstraints.CENTER;
        buildConstraints(constraints, 0, 3, 1, 1, 100, 25);
        grid6.setConstraints(previewTrgSetButton, constraints);
        trgSetSpecPanel.add(previewTrgSetButton);
         */
        
        buildConstraints(constraints, 0, 4, 2, 1, 100, 70);
        constraints.fill = GridBagConstraints.BOTH;
        constraints.anchor = GridBagConstraints.EAST;
        grid5.setConstraints(trgSetSpecPanel, constraints);
        makeTrainingSetPanel.add(trgSetSpecPanel);
        
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.WEST;
        
        buildConstraints(constraints, 0, 4, 1, 1, 100, 50);
        constraints.fill = GridBagConstraints.BOTH;
        classGrid.setConstraints(makeTrainingSetPanel, constraints);
        classifyPanel.add(makeTrainingSetPanel);
        
        constraints.fill = GridBagConstraints.NONE;
        
        hclOpsPanel = new HCLSelectionPanel();
        buildConstraints(constraints, 0, 5, 1, 1, 100, 10);
        constraints.fill = GridBagConstraints.BOTH;
        classGrid.setConstraints(hclOpsPanel, constraints);
        classifyPanel.add(hclOpsPanel);
        
        constraints.fill = GridBagConstraints.NONE;
        
        classifyOrValidatePane = new JTabbedPane();
        classifyOrValidatePane.add("Classify", classifyPanel);
        constraints.fill = GridBagConstraints.BOTH;
        buildConstraints(constraints, 0, 0, 1, 1, 100, 100);
        gridbag.setConstraints(classifyOrValidatePane, constraints);
        mainPanel.add(classifyOrValidatePane);
        
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
    
    public class EventListener extends WindowAdapter implements ActionListener{
        
        public void actionPerformed(ActionEvent ae) {
            String command = ae.getActionCommand();
            if(command.equals("ok-command")){
                okPressed = true;
                dispose();
            } else if (command.equals("reset-command")) {
                okPressed = false;
                genesButton.setSelected(true);
                expsButton.setSelected(false);
                useVarianceFilterBox.setSelected(false);
                useCorrelFilterBox.setSelected(false);
                numClassesField.setText("5");
                numNeighborsField.setText("3");
                trgSetFromCurrent.setSelected(true);
                createNewTrgSetButton.setSelected(true);
                annotSelectBox.setSelectedIndex(0);
                hclOpsPanel.setHCLSelected(false);
            } else if (command.equals("cancel-command")) {
                okPressed = false;
                dispose();
            } else if (command.equals("info-command")){
                
                HelpWindow hw = new HelpWindow(KNNCInitDialog.this, "KNNC Parameter Selections");
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
    
    public static void main(String[] args) {
        
      //  KNNCInitDialog kDialog = new KNNCInitDialog(new JFrame(), true);
      //  kDialog.setVisible(true);
        
    }
    
}
