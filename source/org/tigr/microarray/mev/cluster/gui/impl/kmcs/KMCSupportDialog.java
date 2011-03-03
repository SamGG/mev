/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: KMCSupportDialog.java,v $
 * $Revision: 1.4 $
 * $Date: 2006-02-23 20:59:52 $
 * $Author: caliente $
 * $State: Exp $
 */

package org.tigr.microarray.mev.cluster.gui.impl.kmcs;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;

import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DistanceMetricPanel;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.HCLSelectionPanel;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.SampleSelectionPanel;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.HelpWindow;

/**
 *
 * @author  nbhagaba
 * @version
 */
public class KMCSupportDialog extends AlgorithmDialog {
    
    
    SampleSelectionPanel sPanel;
    MeansOrMediansPanel mPanel;
    TopPanel tPanel;
    KMCParameterPanel kPanel;
    HCLSelectionPanel hclOpsPanel;
    DistanceMetricPanel metricPanel;
    
    boolean okPressed = false;
    
    /** Creates new KMCSupportDialog */
    public KMCSupportDialog(JFrame parentFrame, boolean modality, String globalMetricName, boolean globalAbsoluteSetting) {
        super(parentFrame, "KMS: K-Means/K-Medians Support", modality);
        setBounds(0, 0, 500, 550);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        JPanel pane = new JPanel();
        pane.setLayout(gridbag);
        
        sPanel = new SampleSelectionPanel(Color.white, UIManager.getColor("Label.foreground"),true,"Sample Selection");
        buildConstraints(constraints, 0, 0, 1, 1, 100, 10);
        gridbag.setConstraints(sPanel, constraints);
        pane.add(sPanel);
        
        metricPanel = new DistanceMetricPanel(globalMetricName, globalAbsoluteSetting, "Euclidean Distance", "KMCS", true, true);
        buildConstraints(constraints, 0, 1, 1, 1, 100, 10);
        gridbag.setConstraints(metricPanel, constraints);
        pane.add(metricPanel);
        
        mPanel = new MeansOrMediansPanel();
        buildConstraints(constraints, 0, 2, 1, 1, 100, 10);
        gridbag.setConstraints(mPanel, constraints);
        pane.add(mPanel);
        
        tPanel = new TopPanel();
        buildConstraints(constraints, 0, 3, 1, 1, 100, 30);
        gridbag.setConstraints(tPanel, constraints);
        pane.add(tPanel);
        
        kPanel = new KMCParameterPanel();
        buildConstraints(constraints, 0, 4, 1, 1, 0, 30);
        gridbag.setConstraints(kPanel, constraints);
        pane.add(kPanel);
        
        hclOpsPanel = new HCLSelectionPanel();
        buildConstraints(constraints, 0, 5, 1, 1, 0, 10);
        gridbag.setConstraints(hclOpsPanel, constraints);
        pane.add(hclOpsPanel);
        
        addContent(pane);
        setActionListeners(new Listener());
    }
    
    public void setVisible(boolean visible) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);
        
        super.setVisible(visible);
        
        if (visible) {
            
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
    
    
    class MeansOrMediansPanel extends JPanel {
        JRadioButton meansButton, mediansButton;
        
        MeansOrMediansPanel() {
            this.setBorder(new TitledBorder(new EtchedBorder(), "Means or Medians", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), Color.black));
            this.setBackground(Color.white);
            GridBagLayout gridbag = new GridBagLayout();
            GridBagConstraints constraints = new GridBagConstraints();
            this.setLayout(gridbag);
            
            meansButton = new JRadioButton("Calculate means", true);
            meansButton.setBackground(Color.white);
            meansButton.setForeground(UIManager.getColor("Label.foreground"));
            meansButton.setFocusPainted(false);
            meansButton.setHorizontalAlignment(SwingConstants.CENTER);
            
            mediansButton = new JRadioButton("Calculate medians", false);
            mediansButton.setBackground(Color.white);
            mediansButton.setForeground(UIManager.getColor("Label.foreground"));
            mediansButton.setFocusPainted(false);
            mediansButton.setHorizontalAlignment(SwingConstants.CENTER);
            
            ButtonGroup chooseMeansOrMedians = new ButtonGroup();
            
            chooseMeansOrMedians.add(meansButton);
            chooseMeansOrMedians.add(mediansButton);
            
            buildConstraints(constraints, 0, 0, 1, 1, 50, 100);
            gridbag.setConstraints(meansButton, constraints);
            this.add(meansButton);
            
            buildConstraints(constraints, 1, 0, 1, 1, 50, 0);
            gridbag.setConstraints(mediansButton, constraints);
            this.add(mediansButton);
        }
    }
    
    
    class TopPanel extends JPanel {
        JTextField numRepsInputField, thresholdInputField;
        
        TopPanel() {
            this.setBorder(new TitledBorder(new EtchedBorder(), "Parameters for K-Means / K-Medians repetitions", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), Color.black));
            this.setBackground(Color.white);
            GridBagLayout gridbag = new GridBagLayout();
            GridBagConstraints constraints = new GridBagConstraints();
            //constraints.fill = GridBagConstraints.NONE;
            this.setLayout(gridbag);
            
            JLabel numRepsLabel = new JLabel("Number of k-means / k-medians runs ");
            buildConstraints(constraints, 0, 0, 1, 1, 50, 50);
            gridbag.setConstraints(numRepsLabel, constraints);
            this.add(numRepsLabel);
            
            numRepsInputField = new JTextField("10", 7);
            buildConstraints(constraints, 1, 0, 1, 1, 50, 0);
            gridbag.setConstraints(numRepsInputField, constraints);
            this.add(numRepsInputField);
            
            JLabel thresholdLabel = new JLabel("Threshold % of occurrence in same cluster");
            buildConstraints(constraints, 0, 1, 1, 1, 0, 50);
            gridbag.setConstraints(thresholdLabel, constraints);
            this.add(thresholdLabel);
            
            thresholdInputField = new JTextField("80", 7);
            buildConstraints(constraints, 1, 1, 1, 1, 0, 0);
            gridbag.setConstraints(thresholdInputField, constraints);
            this.add(thresholdInputField);
            
            
        }
    }
    
    class KMCParameterPanel extends JPanel {
        
        JTextField numClustersInputField, numIterationsInputField;
        
        KMCParameterPanel() {
            this.setBorder(new TitledBorder(new EtchedBorder(), "Parameters for each K-Means / K-Medians run", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), Color.black));
            this.setBackground(Color.white);
            GridBagLayout gridbag = new GridBagLayout();
            GridBagConstraints constraints = new GridBagConstraints();
            this.setLayout(gridbag);
            
            JLabel numClustersLabel = new JLabel("Number of clusters ");
            buildConstraints(constraints, 0, 0, 1, 1, 50, 50);
            gridbag.setConstraints(numClustersLabel, constraints);
            this.add(numClustersLabel);
            
            numClustersInputField = new JTextField("10", 7);
            buildConstraints(constraints, 1, 0, 1, 1, 50, 0);
            gridbag.setConstraints(numClustersInputField, constraints);
            this.add(numClustersInputField);
            
            JLabel iterationsLabel = new JLabel("Maximum number of iterations ");
            buildConstraints(constraints, 0, 1, 1, 1, 0, 50);
            gridbag.setConstraints(iterationsLabel, constraints);
            this.add(iterationsLabel);
            
            numIterationsInputField = new JTextField("50", 7);
            buildConstraints(constraints, 1, 1, 1, 1, 0, 0);
            gridbag.setConstraints(numIterationsInputField, constraints);
            this.add(numIterationsInputField);
        }
    }
    
    public boolean isOkPressed() {
        return okPressed;
    }
    
    public int getNumReps() {
        int numReps;
        
        String s1 = tPanel.numRepsInputField.getText();
        numReps = Integer.parseInt(s1);
        return numReps;
    }
    
    public double getThresholdPercent() {
        double thresholdPercent;
        
        String s1 = tPanel.thresholdInputField.getText();
        thresholdPercent = Double.parseDouble(s1);
        return thresholdPercent;
    }
    
    public int getNumClusters() {
        int numClusters;
        
        String s1 = kPanel.numClustersInputField.getText();
        numClusters = Integer.parseInt(s1);
        
        return numClusters;
    }
    
    public int getIterations() {
        int iterations;
        
        String s1 = kPanel.numIterationsInputField.getText();
        iterations = Integer.parseInt(s1);
        
        return iterations;
    }
    
    public boolean isDrawTrees() {
        boolean drawTrees = hclOpsPanel.isHCLSelected();
        return drawTrees;
    }
    
    public boolean meansChosen() {
        return mPanel.meansButton.isSelected();
    }
    
    public boolean isClusterGenes(){
        return sPanel.isClusterGenesSelected();
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
    public boolean getAbsoluteSelection() {
        return metricPanel.getAbsoluteSelection();
    }
    
    /**
     * Resets to initial values
     */
    private void resetControls(){
        this.sPanel.setClusterGenesSelected(true);
        this.mPanel.meansButton.setSelected(true);
        this.tPanel.numRepsInputField.setText("10");
        this.tPanel.thresholdInputField.setText("80");
        this.kPanel.numClustersInputField.setText("10");
        this.kPanel.numIterationsInputField.setText("50");
        this.hclOpsPanel.setHCLSelected(false);
        this.metricPanel.reset();
    }
    
    /**
     * Validates the input
     */
    private boolean validInput(int n, double p, int k, int i){
        boolean  valid = true;
        if(n < 1){
            tPanel.numRepsInputField.requestFocus();
            tPanel.numRepsInputField.selectAll();
            JOptionPane.showMessageDialog(KMCSupportDialog.this, "Number of repetitions must be > 0", "Input Error!", JOptionPane.ERROR_MESSAGE);            
            valid = false;
        } else if(p <= 0 || p > 100){
            tPanel.thresholdInputField.requestFocus();
            tPanel.thresholdInputField.selectAll();
            JOptionPane.showMessageDialog(KMCSupportDialog.this, "Threshold % must be > 0 and <= 100", "Input Error!", JOptionPane.ERROR_MESSAGE);            
            valid = false;
        }else if(k < 1){
            kPanel.numClustersInputField.requestFocus();
            kPanel.numClustersInputField.selectAll();
            JOptionPane.showMessageDialog(KMCSupportDialog.this, "Number of clusters must be > 0", "Input Error!", JOptionPane.ERROR_MESSAGE);            
            valid = false;
        } else if(i < 1){
            JOptionPane.showMessageDialog(KMCSupportDialog.this, "Number of iterations must be > 0", "Input Error!", JOptionPane.ERROR_MESSAGE);
            kPanel.numIterationsInputField.requestFocus();
            kPanel.numIterationsInputField.selectAll();
            valid = false;
        }
        return valid;                
    }
    
    /**
     * The class to listen to the dialog and check boxes items events.
     */
    private class Listener extends DialogListener {
        
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            if (command.equals("ok-command")) {
                okPressed = true;
                int n, k, i, progress = 0;
                double p;
                try{
                    n = Integer.parseInt(tPanel.numRepsInputField.getText());
                    progress++;
                    p = Double.parseDouble(tPanel.thresholdInputField.getText());
                    progress++;
                    k = Integer.parseInt(kPanel.numClustersInputField.getText());
                    progress++;
                    i = Integer.parseInt(kPanel.numIterationsInputField.getText());
                    progress++;
                } catch (NumberFormatException e1){
                    switch(progress){
                        case 0:{
                            tPanel.numRepsInputField.requestFocus();
                            tPanel.numRepsInputField.selectAll();
                            break;
                        }
                        case 1:{
                            tPanel.thresholdInputField.requestFocus();
                            tPanel.thresholdInputField.selectAll();
                            break;
                        }
                        case 2:{
                            kPanel.numClustersInputField.requestFocus();
                            kPanel.numClustersInputField.selectAll();
                            break;
                        }
                        case 3:{
                            kPanel.numIterationsInputField.requestFocus();
                            kPanel.numIterationsInputField.selectAll();
                            break;
                        }
                    }
                    JOptionPane.showMessageDialog(KMCSupportDialog.this, "Entry format error.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if(!validInput(n,p,k,i)){
                    return;
                }
                
                
            } else if (command.equals("cancel-command")) {
                okPressed = false;
            }
            else if (command.equals("reset-command")){
                resetControls();
                return;
            }
            else if (command.equals("info-command")){
            	HelpWindow.launchBrowser(KMCSupportDialog.this, "KMS Initialization Dialog");
            }
            dispose();
        }
        
        
        public void windowClosing(WindowEvent e) {
            okPressed = false;
            dispose();
        }
    }
    
    
    public static void main(String[] args) {
        JFrame dummyFrame = new JFrame();
        KMCSupportDialog kSuppDialog = new KMCSupportDialog(dummyFrame, true, "Euclidean Distance", false);
        kSuppDialog.setVisible(true);
        System.exit(0);
    }
    
}
