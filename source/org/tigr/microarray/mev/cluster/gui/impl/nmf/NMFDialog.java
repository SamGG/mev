/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * NMFDialog.java
 *
 * Created on September 10,2009, 11:05 AM
 */

package org.tigr.microarray.mev.cluster.gui.impl.nmf;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;

import org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.SampleSelectionPanel;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.HelpWindow;

public class NMFDialog extends AlgorithmDialog {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int result = JOptionPane.CANCEL_OPTION;
    
    private SampleSelectionPanel clusteringSelectionPanel;
    JTextField numRunsField, numClustersField, maxNumClustersField, numItersField;
    JRadioButton divergenceButton1, clusterBySamples, expScale;
    JCheckBox clustercb, multiFactors;
    JLabel numClustersLabel;
    
    /** Creates new NMFDialog */
    public NMFDialog(Frame frame) {
        super(frame, "NMF: Non-negative Matrix Factorization", true);
        setBounds(0, 0, 600, 200);
        setBackground(Color.white);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);   
        
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;       
        constraints.insets = new Insets(5,0,5,0);
        
        JPanel pane = new JPanel();
        pane.setBackground(Color.white);
        pane.setBorder(new EtchedBorder());
        pane.setLayout(gridbag);
        
        JPanel clusterTypePanel = new JPanel();
        clusterTypePanel.setBackground(Color.white);
        clusterTypePanel.setBorder(new javax.swing.border.TitledBorder(new javax.swing.border.EtchedBorder(), "Samples/Genes Selection", 
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, 
                new java.awt.Font("Dialog", 1, 12), Color.black));  
        GridBagLayout grid = new GridBagLayout();
        
        clusterBySamples= new JRadioButton("Cluster Samples");
        clusterBySamples.setSelected(true);
        clusterBySamples.setBackground(Color.white);
        buildConstraints(constraints, 0, 0, 1, 1, 50, 100);
        grid.setConstraints(clusterBySamples, constraints);
        clusterTypePanel.add(clusterBySamples);     

        JRadioButton clusterByGenes= new JRadioButton(" Cluster Genes");
        clusterByGenes.setBackground(Color.white);
        clusterByGenes.setSelected(false);
        buildConstraints(constraints, 0, 1, 1, 1, 50, 100);
        grid.setConstraints(clusterByGenes, constraints);
        clusterTypePanel.add(clusterByGenes);  
        
        ButtonGroup bg = new ButtonGroup();
        bg.add(clusterBySamples);
        bg.add(clusterByGenes);
        
        buildConstraints(constraints, 0, 0, 1, 1, 0, 50);
        gridbag.setConstraints(clusterTypePanel, constraints);
        pane.add(clusterTypePanel);   
        
        JPanel runParams = new JPanel();
        runParams.setBackground(Color.white);
        runParams.setBorder(new javax.swing.border.TitledBorder(new javax.swing.border.EtchedBorder(), "Run Parameters", 
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, 
                new java.awt.Font("Dialog", 1, 12), Color.black));  
        runParams.setLayout(grid);

        multiFactors = new JCheckBox("Run Multiple Factors");
        multiFactors.setBackground(Color.white);
        multiFactors.setSelected(false);
        multiFactors.addActionListener(new ActionListener(){
        	public void actionPerformed(ActionEvent ae){
    			maxNumClustersField.setEnabled(multiFactors.isSelected());
    			numClustersLabel.setText(multiFactors.isSelected() ? "Cluster range :":"Number of clusters :");
        	}
        });
        buildConstraints(constraints, 0, 0, 1, 1, 50, 100);
        grid.setConstraints(multiFactors, constraints);
        runParams.add(multiFactors);  
        
        JLabel numRunsLabel= new JLabel("Number of runs : ");
        buildConstraints(constraints, 0, 1, 1, 1, 50, 100);
        constraints.fill = GridBagConstraints.NONE;       
        constraints.anchor = GridBagConstraints.EAST;
        grid.setConstraints(numRunsLabel, constraints);
        runParams.add(numRunsLabel);     
        constraints.fill = GridBagConstraints.HORIZONTAL;       
        
        numRunsField = new JTextField("10", 7);
        buildConstraints(constraints, 1, 1, 1, 1, 50, 100);
        grid.setConstraints(numRunsField, constraints);
        runParams.add(numRunsField);    
        
        numClustersLabel= new JLabel("Number of clusters : ");
        constraints.fill = GridBagConstraints.NONE;       
        buildConstraints(constraints, 0, 2, 1, 1, 50, 100);
        grid.setConstraints(numClustersLabel, constraints);
        runParams.add(numClustersLabel);     
        constraints.fill = GridBagConstraints.HORIZONTAL;  
        
        numClustersField = new JTextField("2", 7);
        buildConstraints(constraints, 1, 2, 1, 1, 50, 100);
        grid.setConstraints(numClustersField, constraints);
        runParams.add(numClustersField);    

        JLabel maxNumClustersLabel= new JLabel(" - ");
        maxNumClustersLabel.setEnabled(false);
        buildConstraints(constraints, 2, 2, 1, 1, 50, 100);
        grid.setConstraints(maxNumClustersLabel, constraints);
        runParams.add(maxNumClustersLabel);   

        maxNumClustersField = new JTextField("4", 7);
        maxNumClustersField.setEnabled(false);
        buildConstraints(constraints, 3, 2, 1, 1, 50, 100);
        grid.setConstraints(maxNumClustersField, constraints);
        runParams.add(maxNumClustersField);    
        
        JLabel numItersLabel= new JLabel("Maximum iterations : ");
        constraints.fill = GridBagConstraints.NONE;       
        buildConstraints(constraints, 0, 3, 1, 1, 50, 100);
        grid.setConstraints(numItersLabel, constraints);
        runParams.add(numItersLabel);     
        constraints.fill = GridBagConstraints.HORIZONTAL;  
        
        numItersField = new JTextField("1000", 7);
        buildConstraints(constraints, 1, 3, 1, 1, 50, 100);
        grid.setConstraints(numItersField, constraints);
        runParams.add(numItersField);    
        
        buildConstraints(constraints, 0, 2, 1, 1, 0, 50);
        gridbag.setConstraints(runParams, constraints);
        pane.add(runParams);  

        JPanel divergencePanel = new JPanel();
        divergencePanel.setBackground(Color.white);
        divergencePanel.setBorder(new javax.swing.border.TitledBorder(new javax.swing.border.EtchedBorder(), "Update rules and cost measurement", 
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, 
                new java.awt.Font("Dialog", 1, 12), Color.black));  
           
        JRadioButton divergenceButton2= new JRadioButton(" Divergence");
        divergenceButton2.setBackground(Color.white);
        divergenceButton2.setSelected(true);
        buildConstraints(constraints, 0, 0, 1, 1, 50, 100);
        grid.setConstraints(divergenceButton2, constraints);
        divergencePanel.add(divergenceButton2);  

        divergenceButton1= new JRadioButton("Euclidean");
        divergenceButton1.setSelected(false);
        divergenceButton1.setBackground(Color.white);
        buildConstraints(constraints, 0, 1, 1, 1, 50, 100);
        grid.setConstraints(divergenceButton1, constraints);
        divergencePanel.add(divergenceButton1);  
        
        ButtonGroup bg1 = new ButtonGroup();
        bg1.add(divergenceButton1);
        bg1.add(divergenceButton2);
        
        buildConstraints(constraints, 0, 4, 1, 1, 0, 50);
        gridbag.setConstraints(divergencePanel, constraints);
        pane.add(divergencePanel); 
        
        JPanel normalizationPanel = new JPanel();
        normalizationPanel.setBackground(Color.white);
        normalizationPanel.setBorder(new javax.swing.border.TitledBorder(new javax.swing.border.EtchedBorder(), "Data matrix pre-processing", 
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, 
                new java.awt.Font("Dialog", 1, 12), Color.black));  
        normalizationPanel.setLayout(grid);

        JLabel noNegs1 = new JLabel("The data matrix may not contain negative values.");
        JLabel noNegs2 = new JLabel("If negative values exist, select a way to adjust the data.");
        buildConstraints(constraints, 0, 0, 2, 1, 50, 100);
        grid.setConstraints(noNegs1, constraints);
        buildConstraints(constraints, 0, 1, 2, 1, 50, 100);
        grid.setConstraints(noNegs2, constraints);
        normalizationPanel.add(noNegs1);     
        normalizationPanel.add(noNegs2);     
        
        constraints.insets = new Insets(10,0,10,0);
        JRadioButton subtractMin= new JRadioButton("Subtract minimum value");
        subtractMin.setBackground(Color.white);
        subtractMin.setSelected(true);
        buildConstraints(constraints, 0, 2, 1, 1, 50, 100);
        grid.setConstraints(subtractMin, constraints);
        normalizationPanel.add(subtractMin);  

        expScale = new JRadioButton("Exponentially scale");
        expScale.setSelected(false);
        expScale.setBackground(Color.white);
        buildConstraints(constraints, 1, 2, 1, 1, 50, 100);
        grid.setConstraints(expScale, constraints);
        normalizationPanel.add(expScale);  
        constraints.insets = new Insets(0,0,0,0);
        
        ButtonGroup bg2 = new ButtonGroup();
        bg2.add(expScale);
        bg2.add(subtractMin);
        
        buildConstraints(constraints, 0, 5, 1, 1, 0, 50);
        gridbag.setConstraints(normalizationPanel, constraints);
        pane.add(normalizationPanel); 
        
        clustercb = new JCheckBox("Store results as clusters");
        clustercb.setBackground(Color.white);
        clustercb.setSelected(false);
        buildConstraints(constraints, 0, 6, 1, 1, 50, 100);
        gridbag.setConstraints(clustercb, constraints);
        pane.add(clustercb);  
        
        
        setActionListeners(new EventListener());
        addContent(pane);
        pack();
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
    
    public int showModal(){
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);
        setVisible(true);
        return result;
    }
    
    public boolean isClusterGenesSelected(){
        return clusteringSelectionPanel.isClusterGenesSelected();
    }
    
    public void resetControls(){
        clusteringSelectionPanel.setClusterGenesSelected(true);
    }
    
    public int getNumRuns() {
        return Integer.parseInt(numRunsField.getText());
    }    

	public int getMaxIterations() {
		return Integer.parseInt(this.numItersField.getText());
	}
    
    
    public boolean getDivergence() {
        return !divergenceButton1.isSelected();
    }   

    public boolean getStoreClusters() {
        return clustercb.isSelected();
    } 
    
    public int getRValue() {
        return Integer.parseInt(numClustersField.getText());
    }   

	public boolean isDoSamples() {
		return clusterBySamples.isSelected();
	}
	
	public boolean isExpScale() {
		return expScale.isSelected();
	}
    
    protected class EventListener implements ActionListener, ItemListener {
        public void actionPerformed(ActionEvent event) {
            String command = event.getActionCommand();
            if (command.equals("ok-command")) {
               try {
                    int numNeibs = getNumRuns();
                    if (numNeibs <= 0) {
                        JOptionPane.showMessageDialog(null, "Invalid number of iterations", "Error", JOptionPane.ERROR_MESSAGE);
                        return;                        
                    } else {
                        result = JOptionPane.OK_OPTION;
                        dispose();
                    }
               }  catch (NumberFormatException nfe){
                    JOptionPane.showMessageDialog(null, "Invalid number of iterations", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } else if (command.equals("cancel-command")){
                result = JOptionPane.CANCEL_OPTION;
                dispose();
            } else if (command.equals("reset-command")){
                resetControls();
            } else if (command.equals("info-command")){
                HelpWindow helpWindow = new HelpWindow(NMFDialog.this, "NMF Initialization Dialog");
                if(helpWindow.getWindowContent()){
                    helpWindow.setSize(450, 350);
                    helpWindow.setLocation();
                    helpWindow.setVisible(true);
                }
                else{
                    helpWindow.dispose();
                }
            }
        }
        public void itemStateChanged(ItemEvent e) {
        	
        }
    }
    
    public static void main(String [] args){
        NMFDialog dialog = new NMFDialog(new Frame());
        int result = dialog.showModal();
        System.out.println("result = "+result);
        System.exit(0);
    }

}
