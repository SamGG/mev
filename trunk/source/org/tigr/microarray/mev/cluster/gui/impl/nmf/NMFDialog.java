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
    
    JTextField numRunsField, rankField, maxRankField, numItersField, randomSeedField, cutoffField, checkFreqField;
    JRadioButton divergenceButton1, clusterBySamples, expScale;
    JCheckBox clustercb, multiRanks, adjustCB, randomSeedCB, doMax;
    JLabel rankLabel, conCutoff, checkFreq;

    
    /** Creates new NMFDialog */
    public NMFDialog(Frame frame) {
        super(frame, "NMF: Non-negative Matrix Factorization", true);
        setBounds(0, 0, 600, 200);
        setBackground(Color.white);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);   
        
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;       
        constraints.insets = new Insets(3,0,3,0);
        
        JPanel pane = new JPanel();
        pane.setBackground(Color.white);
        pane.setBorder(new EtchedBorder());
        pane.setLayout(gridbag);
        
        JPanel clusterTypePanel = new JPanel();
        clusterTypePanel.setBackground(Color.white);
        clusterTypePanel.setBorder(new javax.swing.border.TitledBorder(new javax.swing.border.EtchedBorder(), "Samples/genes selection", 
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
        runParams.setBorder(new javax.swing.border.TitledBorder(new javax.swing.border.EtchedBorder(), "Run parameters", 
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, 
                new java.awt.Font("Dialog", 1, 12), Color.black));  
        runParams.setLayout(grid);

        multiRanks = new JCheckBox("Run multiple ranks");
        multiRanks.setBackground(Color.white);
        multiRanks.setSelected(false);
        multiRanks.addActionListener(new ActionListener(){
        	public void actionPerformed(ActionEvent ae){
    			maxRankField.setEnabled(multiRanks.isSelected());
    			rankLabel.setText(multiRanks.isSelected() ? "Rank range :":"Rank value :");
        	}
        });
        buildConstraints(constraints, 0, 0, 1, 1, 50, 100);
        grid.setConstraints(multiRanks, constraints);
        runParams.add(multiRanks);  
        
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
        
        rankLabel= new JLabel("Rank value : ");
        constraints.fill = GridBagConstraints.NONE;       
        buildConstraints(constraints, 0, 2, 1, 1, 50, 100);
        grid.setConstraints(rankLabel, constraints);
        runParams.add(rankLabel);     
        constraints.fill = GridBagConstraints.HORIZONTAL;  
        
        rankField = new JTextField("2", 7);
        buildConstraints(constraints, 1, 2, 1, 1, 50, 100);
        grid.setConstraints(rankField, constraints);
        runParams.add(rankField);    

        JLabel maxRankLabel= new JLabel(" - ");
        maxRankLabel.setEnabled(false);
        buildConstraints(constraints, 2, 2, 1, 1, 50, 100);
        grid.setConstraints(maxRankLabel, constraints);
        runParams.add(maxRankLabel);   

        maxRankField = new JTextField("4", 7);
        maxRankField.setEnabled(false);
        buildConstraints(constraints, 3, 2, 1, 1, 50, 100);
        grid.setConstraints(maxRankField, constraints);
        runParams.add(maxRankField);    
        
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
        
        doMax= new JCheckBox("Always perform maximum iterations");
        doMax.setBackground(Color.white);
        doMax.setSelected(true);
        doMax.addActionListener(new ActionListener(){
        	public void actionPerformed(ActionEvent ae){
        		conCutoff.setEnabled(!doMax.isSelected());
        		cutoffField.setEnabled(!doMax.isSelected());
        		checkFreq.setEnabled(!doMax.isSelected());
        		checkFreqField.setEnabled(!doMax.isSelected());
        	}
        });
        constraints.fill = GridBagConstraints.NONE;          
        constraints.anchor = GridBagConstraints.WEST;
        buildConstraints(constraints, 0, 4, 1, 1, 50, 100);
        grid.setConstraints(doMax, constraints);
        runParams.add(doMax);     
        constraints.fill = GridBagConstraints.HORIZONTAL;  

        conCutoff= new JLabel("Cost convergence cutoff: ");   
        conCutoff.setEnabled(false);
        constraints.anchor = GridBagConstraints.EAST;
        constraints.fill = GridBagConstraints.NONE;       
        buildConstraints(constraints, 0, 5, 1, 1, 50, 100);
        grid.setConstraints(conCutoff, constraints);
        runParams.add(conCutoff);    
        constraints.fill = GridBagConstraints.HORIZONTAL; 

        cutoffField = new JTextField("1.0", 7);  
        cutoffField.setEnabled(false);
        buildConstraints(constraints, 1, 5, 1, 1, 50, 100);
        grid.setConstraints(cutoffField, constraints);
        runParams.add(cutoffField);   

        checkFreq= new JLabel("Check Frequency: ");      
        checkFreq.setEnabled(false); 
        constraints.anchor = GridBagConstraints.EAST;
        constraints.fill = GridBagConstraints.NONE;  
        buildConstraints(constraints, 0, 6, 1, 1, 50, 100);
        grid.setConstraints(checkFreq, constraints);
        runParams.add(checkFreq);    
        constraints.fill = GridBagConstraints.HORIZONTAL; 

        checkFreqField = new JTextField("40", 7);  
        checkFreqField.setEnabled(false);
        buildConstraints(constraints, 1, 6, 1, 1, 50, 100);
        grid.setConstraints(checkFreqField, constraints);
        runParams.add(checkFreqField);   
        
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
        

        adjustCB = new JCheckBox("Always adjust data");
        adjustCB.setBackground(Color.white);
        adjustCB.setSelected(false);
        buildConstraints(constraints, 0, 2, 2, 1, 50, 100);
        grid.setConstraints(adjustCB, constraints);
        normalizationPanel.add(adjustCB);  
        
        constraints.insets = new Insets(10,0,10,0);
        JRadioButton subtractMin= new JRadioButton("Subtract minimum value");
        subtractMin.setBackground(Color.white);
        subtractMin.setSelected(true);
        buildConstraints(constraints, 0, 3, 1, 1, 50, 100);
        grid.setConstraints(subtractMin, constraints);
        normalizationPanel.add(subtractMin);  

        expScale = new JRadioButton("Exponentially scale");
        expScale.setSelected(false);
        expScale.setBackground(Color.white);
        buildConstraints(constraints, 1, 3, 1, 1, 50, 100);
        grid.setConstraints(expScale, constraints);
        normalizationPanel.add(expScale);  
        constraints.insets = new Insets(0,0,0,0);
        
        ButtonGroup bg2 = new ButtonGroup();
        bg2.add(expScale);
        bg2.add(subtractMin);
        
        buildConstraints(constraints, 0, 5, 1, 1, 0, 50);
        gridbag.setConstraints(normalizationPanel, constraints);
        pane.add(normalizationPanel); 
        
        JPanel randomSeedPanel = new JPanel();
        randomSeedPanel.setBackground(Color.white);
        randomSeedPanel.setBorder(new javax.swing.border.TitledBorder(new javax.swing.border.EtchedBorder(), "Random number generation", 
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, 
                new java.awt.Font("Dialog", 1, 12), Color.black));  
        randomSeedPanel.setLayout(grid);

        randomSeedCB = new JCheckBox("Use random number generator seed:");
        randomSeedCB.setBackground(Color.white);
        randomSeedCB.setSelected(false);
        randomSeedCB.addActionListener(new ActionListener(){
        	public void actionPerformed(ActionEvent ae){
        		randomSeedField.setEnabled(randomSeedCB.isSelected());
        	}
        });
        buildConstraints(constraints, 0, 0, 1, 1, 50, 100);
        grid.setConstraints(randomSeedCB, constraints);
        randomSeedPanel.add(randomSeedCB);  
         
        
        randomSeedField = new JTextField("12345", 7);
        randomSeedField.setEnabled(false);
        buildConstraints(constraints, 1, 0, 1, 1, 50, 100);
        grid.setConstraints(randomSeedField, constraints);
        randomSeedPanel.add(randomSeedField);    
        
        
        buildConstraints(constraints, 0, 6, 1, 1, 0, 50);
        gridbag.setConstraints(randomSeedPanel, constraints);
        pane.add(randomSeedPanel);  
        

        JPanel clusterPanel = new JPanel();
        clusterPanel.setBackground(Color.white);
        clusterPanel.setBorder(new javax.swing.border.TitledBorder(new javax.swing.border.EtchedBorder(), "Clusters", 
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, 
                new java.awt.Font("Dialog", 1, 12), Color.black));  
        clusterPanel.setLayout(grid);
        clustercb = new JCheckBox("Store results as clusters");
        clustercb.setBackground(Color.white);
        clustercb.setSelected(false);
        buildConstraints(constraints, 0, 0, 1, 1, 50, 100);
        grid.setConstraints(clustercb, constraints);
        clusterPanel.add(clustercb);  
        buildConstraints(constraints, 0, 7, 1, 1, 50, 100);
        gridbag.setConstraints(clusterPanel, constraints);
        pane.add(clusterPanel);  
        
        
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
    
    public void resetControls(){

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

    public boolean isStoreClusters() {
        return clustercb.isSelected();
    } 

    public boolean isMultiRank() {
        return multiRanks.isSelected();
    } 

    public long getRandomSeed() {
    	if (randomSeedCB.isSelected())
    		return Long.parseLong(this.randomSeedField.getText());
    	else
    		return -1;
    } 
    
    public int getRValue() {
        return Integer.parseInt(rankField.getText());
    }   

    public int getMaxRValue() {
        return this.multiRanks.isSelected()? Integer.parseInt(maxRankField.getText()): getRValue();
    }   

	public boolean isClusterSamples() {
		return clusterBySamples.isSelected();
	}

	public boolean isAdjustData() {
		return adjustCB.isSelected();
	}
	
	public boolean isExpScale() {
		return expScale.isSelected();
	}

	public boolean isDoMaxIters() {
		return doMax.isSelected();
	}
	
	public float getCutoff() {
        return Float.parseFloat(cutoffField.getText());
    }

    public int getCheckFreq() {
        return Integer.parseInt(checkFreqField.getText());
    }  
    
    protected class EventListener implements ActionListener, ItemListener {
        public void actionPerformed(ActionEvent event) {
            String command = event.getActionCommand();
            if (command.equals("ok-command")) {
               try {
                    int numRuns = getNumRuns();
                    int maxR = getMaxRValue();
                    int minR = getRValue();
                    int iterations = getMaxIterations();
                    if (numRuns <= 0) {
                        JOptionPane.showMessageDialog(null, "Invalid number of runs.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;                        
                    }
                    if (maxR <= minR && isMultiRank()) {
                        JOptionPane.showMessageDialog(null, "The maximum number of runs must be greater than the minimum number of runs, obviously.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;                        
                    }
                    if (minR <= 1) {
                        JOptionPane.showMessageDialog(null, "The NMF rank must be at least 2.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;                        
                    }
                    if (iterations <= 0) {
                        JOptionPane.showMessageDialog(null, "Invalid number of iterations.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;                        
                    }
                    result = JOptionPane.OK_OPTION;
                    dispose();
                    
               }  catch (NumberFormatException nfe){
                    JOptionPane.showMessageDialog(null, "Invalid input.\n\nPlease check your parameters and try again.", "Error", JOptionPane.ERROR_MESSAGE);
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
