/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: KMCInitDialog.java,v $
 * $Revision: 1.3 $
 * $Date: 2005-02-24 20:23:57 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.kmc;

import java.awt.Color;
import java.awt.Toolkit;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.JTextField;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import javax.swing.JOptionPane;
import javax.swing.BorderFactory;

import org.tigr.microarray.mev.cluster.gui.impl.dialogs.*;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.*;

import org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog;

public class KMCInitDialog extends AlgorithmDialog {
    private int result;
    private int k, iter;
    private JTextField textField1;
    private JTextField textField2;
    private JRadioButton calcMeans;
    private JRadioButton calcMedians;
    private ButtonGroup meanMedianGroup;
    private SampleSelectionPanel sampleSelectionPanel;
    private HCLSelectionPanel hclPanel;
    private DistanceMetricPanel metricPanel;
    
    /**
     * Constructs a <code>KMCInitDialog</code> with specified clusters and
     * iterations parameters.
     */
    public KMCInitDialog(javax.swing.JFrame parent, int clusters, int iterations, String globalMetricName, boolean globalAbsoluteValue) {
        super(parent, "KMC: K-Means/K-Medians", true);
        k = clusters;
        iter = iterations;
        Listener listener = new Listener();
        addWindowListener(listener);
        
        sampleSelectionPanel = new SampleSelectionPanel(Color.white, UIManager.getColor("Label.foreground"),true,"Sample Selection");
        
        metricPanel = new DistanceMetricPanel(globalMetricName, globalAbsoluteValue, "Euclidean Distance", "KMC", true, true);
        
        meanMedianGroup = new ButtonGroup();
        calcMeans = new JRadioButton("Calculate K-Means", true);
        calcMeans.setFocusPainted(false);
        calcMeans.setBackground(Color.white);
        calcMeans.setForeground(UIManager.getColor("Label.foreground"));
        calcMedians = new JRadioButton("Calculate K-Medians", false);
        calcMedians.setFocusPainted(false);
        calcMedians.setBackground(Color.white);
        calcMedians.setForeground(UIManager.getColor("Label.foreground"));
        meanMedianGroup.add(calcMeans);
        meanMedianGroup.add(calcMedians);
        
        ParameterPanel parameters = new ParameterPanel();
        parameters.setLayout(new GridBagLayout());
        parameters.add(calcMeans, new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(5,0,0,10),0,0));
        parameters.add(calcMedians, new GridBagConstraints(1,0,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(5,10,0,0),0,0));
        parameters.add(new JLabel("Number of clusters"), new GridBagConstraints(0,1,1,1,0.0,0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(10,0,0,0),0,0));
        parameters.add(new JLabel("Maximum iterations"), new GridBagConstraints(0,2,1,1,0.0,0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(5,0,5,0),0,0));
        
        textField1 = new JTextField(String.valueOf(clusters), 7);
        textField2 = new JTextField(String.valueOf(iterations), 7);
        parameters.add(textField1, new GridBagConstraints(1,1,1,1,0,0,GridBagConstraints.WEST,GridBagConstraints.VERTICAL,new Insets(10,10,0,0),0,0));
        parameters.add(textField2, new GridBagConstraints(1,2,1,1,0,0,GridBagConstraints.WEST,GridBagConstraints.VERTICAL,new Insets(5,10,5,0),0,0));
        
        hclPanel = new HCLSelectionPanel();
        
        //construct mainPanel
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(Color.white);
        mainPanel.add(sampleSelectionPanel, new GridBagConstraints(0,0,1,1,1.0,0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0));
        mainPanel.add(metricPanel,  new GridBagConstraints(0,1,1,1,1.0,0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0));
        mainPanel.add(parameters, new GridBagConstraints(0,2,1,1,1.0,0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0));
        mainPanel.add(hclPanel, new GridBagConstraints(0,3,1,1,1.0,0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0));
     
        JPanel controlPanel = new JPanel(new BorderLayout());
        controlPanel.setForeground(Color.white);
        controlPanel.setBorder(BorderFactory.createLineBorder(Color.gray));
        controlPanel.setBackground(Color.white);
        controlPanel.add(mainPanel, BorderLayout.CENTER);
        
        addContent(controlPanel); 
        okButton.addActionListener(listener);
        cancelButton.addActionListener(listener);
        resetButton.addActionListener(listener);
        infoButton.addActionListener(listener);
        validate();
        setSize(420, 410);
        setResizable(false);
        pack();
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
     * Returns count of clusters.
     */
    public int getClusters() {
        return Integer.parseInt(textField1.getText());
    }
    
    /**
     * Returns count of iterations.
     */
    public int getIterations() {
        return Integer.parseInt(textField2.getText());
    }
    
    /**
     * Returns true if the hierarchical checkbox is selected.
     */
    public boolean isHierarchicalTree() {
        return hclPanel.isHCLSelected();
    }
    
    /**
     * Returns true if calculate k-means is selected, false
     * if k-medians is selected
     */
    public boolean calculateMeans(){
        return calcMeans.isSelected();
    }
    
    /**
     *
     */
    public boolean isClusterGenesSelected(){
        return this.sampleSelectionPanel.isClusterGenesSelected();
    }
    
    /**
     *  Resets the controls
     */
    private void onReset(){
        textField1.setText(String.valueOf(k));
        textField2.setText(String.valueOf(iter));
        sampleSelectionPanel.setClusterGenesSelected(true);
        hclPanel.setHCLSelected(false);
        calcMeans.setSelected(true);
        metricPanel.reset();
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
     * The class to listen to the dialog events.
     */
    private class Listener extends DialogListener {
        
        public void actionPerformed(ActionEvent e) {
            Object source = e.getSource();
            if (source == okButton) {
                try {         
                    int k = Integer.parseInt(textField1.getText());
                    if(k < 1){
                        textField1.requestFocus();
                        textField1.selectAll();
                        JOptionPane.showMessageDialog(KMCInitDialog.this, "Number of clusters must be greater than 0!", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    
                    int i = Integer.parseInt(textField2.getText());                    
                    if(i < 1){
                        textField2.requestFocus();
                        textField2.selectAll();
                        JOptionPane.showMessageDialog(KMCInitDialog.this, "Number of iterations must be greater than 0!", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }              
                    result = JOptionPane.OK_OPTION;
                } catch (NumberFormatException exception) {
                    JOptionPane.showMessageDialog(KMCInitDialog.this, "Entry format error.", "Error", JOptionPane.ERROR_MESSAGE);
                    result = JOptionPane.CANCEL_OPTION;
                    return;
                }
                dispose();
            } else if (source == cancelButton) {
                result = JOptionPane.CANCEL_OPTION;
                dispose();
            }
            else if (source == resetButton) {
                onReset();
            }
            else if (source == infoButton){
                HelpWindow hw = new HelpWindow(KMCInitDialog.this, "KMC Initialization Dialog");
                if(hw.getWindowContent()){
                    hw.setSize(450,650);
                    hw.setLocation();
                    hw.show();
                }
                else {
                    hw.setVisible(false);
                    hw.dispose();
                }                    
            }
        }
        
        public void windowClosing(WindowEvent e) {
            result = JOptionPane.CLOSED_OPTION;
            dispose();
        }
    }
    
    public static void main(String[] args) {
        KMCInitDialog dlg = new KMCInitDialog(new javax.swing.JFrame(), 10 , 50, "Pearson Correlation", true);
        
        dlg.showModal();
        System.exit(0);
    }
    
    protected void disposeDialog() {
    }
    
}