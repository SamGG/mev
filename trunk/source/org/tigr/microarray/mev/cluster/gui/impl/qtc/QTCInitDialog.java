/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: QTCInitDialog.java,v $
 * $Revision: 1.3 $
 * $Date: 2005-03-10 20:22:06 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.qtc;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;

import org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DistanceMetricPanel;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.HCLSelectionPanel;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.ParameterPanel;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.SampleSelectionPanel;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.HelpWindow;
import org.tigr.util.awt.GBA;

public class QTCInitDialog extends AlgorithmDialog {
    
    protected GBA gba;
    protected EventListener eventListener;
    
    protected JPanel inputPanel;
    public JLabel diameterLabel;
    public JTextField diameterTextField;
    public JLabel clusterLabel;
    public JTextField clusterTextField;
    
    public int result;
    
    private SampleSelectionPanel sampleSelectionPanel;
    private HCLSelectionPanel hclOpsPanel;
    
    protected JPanel mainPanel;
    
    private boolean okPressed = false;
    
    private DistanceMetricPanel metricPanel;
    
    public QTCInitDialog(JFrame parent, boolean modal, String globalMetricName, boolean globalAbsoluteValue) {
        super(parent, "QTC: QT Cluster", modal);
        
        initialize(globalMetricName, globalAbsoluteValue);
    }
    
    protected void initialize(String globalMetricName, boolean globalAbsoluteValue) {
        gba = new GBA();
        eventListener = new EventListener();
        
        diameterLabel = new JLabel("Maximum Cluster Diameter");    diameterLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        diameterTextField = new JTextField(5);
        
        diameterTextField.setText("0.5");
        clusterLabel = new JLabel("Minimum Cluster Population");
        
        clusterTextField = new JTextField(5);
        
        clusterTextField.setText("5");
        
        sampleSelectionPanel = new SampleSelectionPanel(Color.white, UIManager.getColor("Label.foreground"),true,"Sample Selection");
         
        metricPanel = new DistanceMetricPanel(globalMetricName, globalAbsoluteValue, "Pearson Correlation", "QTC", true, true);
 
        inputPanel = new JPanel();
        inputPanel.setLayout(new GridBagLayout());
        inputPanel.setBackground(Color.white);
        gba.add(inputPanel, diameterLabel, 0, 0, 1, 1, 0, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
        gba.add(inputPanel, clusterLabel, 0, 1, 1, 0, 0, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
        gba.add(inputPanel, diameterTextField, 1, 0, 1, 1, 0, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
        gba.add(inputPanel, clusterTextField, 1, 1, 1, 1, 0, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
                      
        hclOpsPanel = new HCLSelectionPanel();
        
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        mainPanel.setBackground(Color.white);
        
        ParameterPanel parameters = new ParameterPanel();
        parameters.setLayout(new GridBagLayout());
        gba.add(parameters, inputPanel, 0, 0, 1, 1, 1, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
       
        gba.add(mainPanel, sampleSelectionPanel, 0, 0, 1, 1, 1, 1, GBA.H, GBA.C, new Insets(0, 0, 0, 0), 0, 0);
        gba.add(mainPanel, metricPanel, 0, 1, 1, 1, 1, 1, GBA.H, GBA.C, new Insets(0, 0, 0, 0), 0, 0);
        gba.add(mainPanel, parameters, 0, 2, 1, 1, 1, 1, GBA.H, GBA.C, new Insets(0, 0, 0, 0), 0, 0);
        gba.add(mainPanel, hclOpsPanel, 0, 3, 1, 1, 1, 1, GBA.H, GBA.C, new Insets(0, 0, 0, 0), 0, 0);
        
        
        setActionListeners(eventListener);
        addContent(mainPanel);
        
        pack();
    }
    
    public void setVisible(boolean visible) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);
        
        super.setVisible(visible);
        
        if (visible) {
            okButton.requestFocus();
        }
    }
    
  /*  protected void fireOkButtonEvent() {
   
        boolean useAbsolute;
        boolean drawTrees;
        double diameter;
        int clusterSize;
   
        useAbsolute = useAbsoluteCheckBox.isSelected();
        drawTrees = drawTreesCheckBox.isSelected();
   
        try {
            diameter = Double.parseDouble(diameterTextField.getText());
        } catch (NumberFormatException nfe) {
            diameter = 1;
        }
   
        try {
            clusterSize = Integer.parseInt(clusterTextField.getText());
        } catch (NumberFormatException nfe) {
            clusterSize = 1;
        }
   
        Hashtable hash = new Hashtable();
        hash.put(new String("useAbsolute"), new Boolean(useAbsolute));
        hash.put(new String("diameter"), new Double(diameter));
        hash.put(new String("clusterSize"), new Integer(clusterSize));
        hash.put(new String("drawTrees"), new Boolean(drawTrees));
        fireEvent(new ActionInfoEvent(this, hash));
    }
   */
    
    public boolean isOkPressed() {return this.okPressed;}
    
    /**
     *  Returns true if clustering genes is selectd
     */
    public boolean isClusterGenesSelected(){
        return this.sampleSelectionPanel.isClusterGenesSelected();
    }
    
    /**
     *  Returns true is HCL clustering is selected
     */
    public boolean isHCLSelected(){
        return this.hclOpsPanel.isHCLSelected();
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
     * Resets controls
     */
    private void resetControls(){
        this.sampleSelectionPanel.setClusterGenesSelected(true);
        this.hclOpsPanel.setHCLSelected(false);
        this.clusterTextField.setText("5");
        this.diameterTextField.setText("0.5");
        this.metricPanel.reset();
    }
    
    /**
     * Validates input
     */
    private boolean validInput(int k, float d){
        boolean valid = true;
        if( d <=0 ){
            JOptionPane.showMessageDialog(QTCInitDialog.this, "Diameter must be > 0", "Input Error!", JOptionPane.ERROR_MESSAGE);
            this.diameterTextField.requestFocus();
            this.diameterTextField.selectAll();
            valid = false;
        }
        else if( k < 1){
            JOptionPane.showMessageDialog(QTCInitDialog.this, "Population of a cluster must be > 0", "Input Error!", JOptionPane.ERROR_MESSAGE);
            this.clusterTextField.requestFocus();
            this.clusterTextField.selectAll();
            valid = false;
        }
        return valid;
    }
    
    
    public static void main(String [] agrs){
        QTCInitDialog hgid = new QTCInitDialog(new JFrame(), true, "Euclidean Distance", false);
        hgid.show();
        System.exit(0);
    }
    
    
    protected class EventListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            String command = event.getActionCommand();
            if (command.equals("ok-command")) {
                int progress = 0;
                int k;
                float d;
                try {
                    d = Float.parseFloat(diameterTextField.getText());
                    progress++;
                    k = Integer.parseInt(clusterTextField.getText());
                    progress++;
                    
                } catch (NumberFormatException nfe) {
                    if(progress == 0){
                        diameterTextField.requestFocus();
                        diameterTextField.selectAll();
                    } else if(progress == 1){
                        clusterTextField.requestFocus();
                        clusterTextField.selectAll();
                    }
                    JOptionPane.showMessageDialog(QTCInitDialog.this, "Number format error.", "Number Format Error", JOptionPane.ERROR_MESSAGE);                    
                    result = JOptionPane.CANCEL_OPTION;
                    okPressed = false;
                    return;
                }
                if(validInput(k,d)){
                    result = JOptionPane.OK_OPTION;
                    okPressed = true; 
                    dispose();
                   
                }
                else{
                    result = JOptionPane.CANCEL_OPTION;
                    okPressed = false;
                }
            } else if (command.equals("cancel-command")) {
                result = JOptionPane.CANCEL_OPTION;
                okPressed = false;
                dispose();
            } else if (command.equals("reset-command")) {
                resetControls();
            } else if (command.equals("info-command")) {
                HelpWindow hw = new HelpWindow(QTCInitDialog.this, "QTC Initialization Dialog");
                if(hw.getWindowContent()){
                    hw.setSize(450,500);
                    hw.setLocation();
                    hw.show();
                }
                else {
                    hw.setVisible(false);
                    hw.dispose();
                }
            }
        }
    }
}
