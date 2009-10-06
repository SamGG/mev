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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.UIManager;
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
    JTextField numRunsField, numClustersField, numItersField;
    JRadioButton divergenceButton1, clusterBySamples;
    
    /** Creates new NMFDialog */
    public NMFDialog(Frame frame) {
        super(frame, "NMF: Non-negative Matrix Factorization", true);
        setBounds(0, 0, 600, 200);
        setBackground(Color.white);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);   
        
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;       
        
        JPanel pane = new JPanel();
        pane.setBackground(Color.white);
        pane.setBorder(new EtchedBorder());
        pane.setLayout(gridbag);
        
        clusteringSelectionPanel = new SampleSelectionPanel(Color.white, UIManager.getColor("Label.foreground"),true,"Clustering Selection");
        clusteringSelectionPanel.setGeneButtonItemListener(new EventListener());
        clusteringSelectionPanel.setSampleButtonItemListener(new EventListener());
        buildConstraints(constraints, 0, 0, 2, 1, 100, 50);
        gridbag.setConstraints(clusteringSelectionPanel, constraints);
        

        JPanel clusterTypePanel = new JPanel();
        clusterTypePanel.setBackground(Color.white);
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
        
        
        
        
        pane.add(clusterTypePanel);   
        
        JPanel numRunsPanel = new JPanel();
        numRunsPanel.setBackground(Color.white);
        grid = new GridBagLayout();
        
        JLabel numRunsLabel= new JLabel("Number of runs :");
        buildConstraints(constraints, 0, 0, 1, 1, 50, 100);
        grid.setConstraints(numRunsLabel, constraints);
        numRunsPanel.add(numRunsLabel);     
        
        numRunsField = new JTextField("10", 7);
        buildConstraints(constraints, 1, 0, 1, 1, 50, 100);
        grid.setConstraints(numRunsField, constraints);
        numRunsPanel.add(numRunsField);    
        
        buildConstraints(constraints, 0, 1, 1, 1, 0, 50);
        gridbag.setConstraints(numRunsPanel, constraints);
        pane.add(numRunsPanel);   

        JPanel numClustersPanel = new JPanel();
        numClustersPanel.setBackground(Color.white);
        grid = new GridBagLayout();
        
        JLabel numClustersLabel= new JLabel("Number of clusters :");
        buildConstraints(constraints, 0, 0, 1, 1, 50, 100);
        grid.setConstraints(numClustersLabel, constraints);
        numClustersPanel.add(numClustersLabel);     
        
        numClustersField = new JTextField("4", 7);
        buildConstraints(constraints, 2, 0, 1, 1, 50, 100);
        grid.setConstraints(numClustersField, constraints);
        numClustersPanel.add(numClustersField);    
        
        buildConstraints(constraints, 0, 2, 1, 1, 0, 50);
        gridbag.setConstraints(numClustersPanel, constraints);
        pane.add(numClustersPanel); 

        JPanel numItersPanel = new JPanel();
        numItersPanel.setBackground(Color.white);
        grid = new GridBagLayout();
        
        JLabel numItersLabel= new JLabel("Maximum iterations :");
        buildConstraints(constraints, 0, 0, 1, 1, 50, 100);
        grid.setConstraints(numItersLabel, constraints);
        numItersPanel.add(numItersLabel);     
        
        numItersField = new JTextField("1000", 7);
        buildConstraints(constraints, 1, 0, 1, 1, 50, 100);
        grid.setConstraints(numItersField, constraints);
        numItersPanel.add(numItersField);    
        
        buildConstraints(constraints, 0, 3, 1, 1, 0, 50);
        gridbag.setConstraints(numItersPanel, constraints);
        pane.add(numItersPanel);   

        JPanel divergencePanel = new JPanel();
        divergencePanel.setBackground(Color.white);
        grid = new GridBagLayout();
        
        divergenceButton1= new JRadioButton("Euclidean Cost Measurement");
        divergenceButton1.setSelected(false);
        divergenceButton1.setBackground(Color.white);
        buildConstraints(constraints, 0, 0, 1, 1, 50, 100);
        grid.setConstraints(divergenceButton1, constraints);
        divergencePanel.add(divergenceButton1);     

        JRadioButton divergenceButton2= new JRadioButton(" Divergence Cost Measurement");
        divergenceButton2.setBackground(Color.white);
        divergenceButton2.setSelected(true);
        buildConstraints(constraints, 0, 1, 1, 1, 50, 100);
        grid.setConstraints(divergenceButton2, constraints);
        divergencePanel.add(divergenceButton2);  
        
        ButtonGroup bg1 = new ButtonGroup();
        bg1.add(divergenceButton1);
        bg1.add(divergenceButton2);
        
        buildConstraints(constraints, 0, 4, 1, 1, 0, 50);
        gridbag.setConstraints(divergencePanel, constraints);
        pane.add(divergencePanel); 
        
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

    public int getRValue() {
        return Integer.parseInt(numClustersField.getText());
    }   

	public boolean isDoSamples() {
		return clusterBySamples.isSelected();
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
