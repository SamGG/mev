/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: KMCSupportDialog.java,v $
 * $Revision: 1.1.1.2 $
 * $Date: 2004-02-06 21:48:18 $
 * $Author: braisted $
 * $State: Exp $
 */

package org.tigr.microarray.mev.cluster.gui.impl.kmcs;

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
    
    boolean okPressed = false;
    
    /** Creates new KMCSupportDialog */
    public KMCSupportDialog(JFrame parentFrame, boolean modality) {
        super(parentFrame, "KMS: K-Means/K-Medians Support", modality);
        setBounds(0, 0, 500, 500);
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
        
        mPanel = new MeansOrMediansPanel();
        buildConstraints(constraints, 0, 1, 1, 1, 100, 10);
        gridbag.setConstraints(mPanel, constraints);
        pane.add(mPanel);
        
        tPanel = new TopPanel();
        buildConstraints(constraints, 0, 2, 1, 1, 100, 30);
        gridbag.setConstraints(tPanel, constraints);
        pane.add(tPanel);
        
        kPanel = new KMCParameterPanel();
        buildConstraints(constraints, 0, 3, 1, 1, 0, 30);
        gridbag.setConstraints(kPanel, constraints);
        pane.add(kPanel);
        
        hclOpsPanel = new HCLSelectionPanel();
        buildConstraints(constraints, 0, 4, 1, 1, 0, 10);
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
                HelpWindow hw = new HelpWindow(KMCSupportDialog.this, "KMS Initialization Dialog");
                if(hw.getWindowContent()){
                    hw.setSize(450,650);
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
            dispose();
        }
        
        
        public void windowClosing(WindowEvent e) {
            okPressed = false;
            dispose();
        }
    }
    
    
    public static void main(String[] args) {
        JFrame dummyFrame = new JFrame();
        KMCSupportDialog kSuppDialog = new KMCSupportDialog(dummyFrame, true);
        kSuppDialog.setVisible(true);
        System.exit(0);
    }
    
}
