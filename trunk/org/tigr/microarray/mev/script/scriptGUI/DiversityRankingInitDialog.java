/*
Copyright @ 1999-2004, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * DiversityRankingInitDialog.java
 *
 * Created on May 30, 2004, 11:26 PM
 */

package org.tigr.microarray.mev.script.scriptGUI;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;

import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.ParameterPanel;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.HelpWindow;

/**
 *
 * @author  braisted
 */
public class DiversityRankingInitDialog extends AlgorithmDialog {
    
    /** result
     */    
    private int result;
    /** number of clusters input field
     */    
    private JTextField numClustersField;
    /** min cluster size input control
     */    
    private JTextField minClusterSizeField;
    /** selects centroid based diversity method
     */    
    private JRadioButton centroidDiversityButton;
    /** selects gene-to-gene based diversity
     */    
    private JRadioButton geneDiversityButton;
    
    
    /** Creates a new instance of DiversityRankingInitDialog
     * @param parent parent component
     */
    public DiversityRankingInitDialog(JFrame parent) {
        super(parent, "Diversity Ranking Cluster Selection", true);
        
        ParameterPanel paramPanel = new ParameterPanel("Selection Parameters");
        paramPanel.setLayout(new GridBagLayout());
        
        JLabel clusterLabel = new JLabel("Desired Number of Clusters");
        numClustersField = new JTextField("3", 5);
        JLabel sizeLabel = new JLabel("Minimum Cluster Size (# genes)");
        minClusterSizeField = new JTextField("10");
        
        ButtonGroup bg = new ButtonGroup();
        centroidDiversityButton = new JRadioButton("Rank Clusters on Centroid Based Diversity", true);
        centroidDiversityButton.setOpaque(false);
        bg.add(centroidDiversityButton);
        centroidDiversityButton.setFocusPainted(false);
        
        geneDiversityButton = new JRadioButton("Rank Clusters on Intra-gene Based Diversity");
        geneDiversityButton.setOpaque(false);
        bg.add(geneDiversityButton);
        geneDiversityButton.setFocusPainted(false);
        
        paramPanel.add(clusterLabel, new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,10), 0,0));
        paramPanel.add(numClustersField, new GridBagConstraints(1,0,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,10,0,0), 0,0));
        
        paramPanel.add(sizeLabel, new GridBagConstraints(0,1,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,0,0,10), 0,0));
        paramPanel.add(minClusterSizeField, new GridBagConstraints(1,1,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,10,0,0), 0,0));
        
        paramPanel.add(centroidDiversityButton, new GridBagConstraints(0,2,2,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(15,0,0,0), 0,0));
        paramPanel.add(geneDiversityButton, new GridBagConstraints(0,3,2,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,0,10,0), 0,0));
        
        addContent(paramPanel);
        setActionListeners(new Listener());
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
    
    /** returns the desired number of clusters.
     * @return  */    
    public int getClusterNumber() {
        return Integer.parseInt(this.numClustersField.getText());
    }
    
    /** Returns min cluster size desired
     */    
    public int getClusterSize() {
        return Integer.parseInt(this.minClusterSizeField.getText());
    }
    
    /** if true selection is centroid based, else gene-to-gene based.
     */    
    public boolean isCentroidBased() {
        return this.centroidDiversityButton.isSelected();
    }
    
    /** resets controls
     */    
    private void resetControls() {
        this.numClustersField.setText("3");
        this.minClusterSizeField.setText("10");
        this.centroidDiversityButton.setSelected(true);
    }
    
    /** validates parameters
     */    
    private boolean validateParameters() {
        int level = 0;
        int n, c;
        try {
            n = Integer.parseInt(this.numClustersField.getText());
            level++;
            c = Integer.parseInt(this.minClusterSizeField.getText());
            level++;
        } catch (Exception e) {
            if(level == 0) {
                JOptionPane.showMessageDialog(this, "The cluster number should be an integer > 0.\n" +
                "Please enter a new value.", "Parameter Type Mismatch", JOptionPane.ERROR_MESSAGE);
                this.numClustersField.selectAll();
                this.numClustersField.grabFocus();
                return false;
            } else {
                JOptionPane.showMessageDialog(this, "The minimum cluster population should be an integer > 0.\n" +
                "Please enter a new value.", "Parameter Type Mismatch", JOptionPane.ERROR_MESSAGE);
                this.minClusterSizeField.selectAll();
                this.minClusterSizeField.grabFocus();
                return false;
            }
        }
        
        //type matches, check the values
        
        if(n < 1) {
            JOptionPane.showMessageDialog(this, "The cluster number should be an integer > 0.\n" +
            "Please enter a new value greater than zero.", "Parameter Value Error", JOptionPane.ERROR_MESSAGE);
            this.numClustersField.selectAll();
            this.numClustersField.grabFocus();
            return false;            
        } else if(c < 1) {
            JOptionPane.showMessageDialog(this, "The minimum cluster size should be an integer > 0.\n" +
            "Please enter a new value greater than zero.", "Parameter Value Error", JOptionPane.ERROR_MESSAGE);
            this.numClustersField.selectAll();
            this.numClustersField.grabFocus();
            return false;
        }        
        return true;
    }
    
    public static void main(String [] args) {
        DiversityRankingInitDialog dialog = new DiversityRankingInitDialog(new JFrame());
        dialog.showModal();
    }
   
    
    /**
     * The class to listen to the dialog events.
     */
    private class Listener extends DialogListener {
        
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            
            if (command.equals("ok-command")) {
                if(validateParameters()){
                    dispose();
                    result = JOptionPane.OK_OPTION;
                }
                return;
            } else if (command.equals("cancel-command")) {
                result = JOptionPane.CANCEL_OPTION;
                dispose();
                return;
            }
            else if (command.equals("reset-command")) {
                resetControls();
            }
            else if (command.equals("info-command")){
                HelpWindow hw = new HelpWindow(DiversityRankingInitDialog.this, "Diversity Ranking Cluster Selection");
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
    
}
