/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: SVMProcessInitDialog.java,v $
 * $Revision: 1.2 $
 * $Date: 2005-02-24 20:23:45 $
 * $Author: braistedj $
 * $State: Exp $
 */

package org.tigr.microarray.mev.cluster.gui.impl.svm;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.HCLSelectionPanel;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.SampleSelectionPanel;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.HelpWindow;

public class SVMProcessInitDialog extends AlgorithmDialog {
    
    int result = JOptionPane.CANCEL_OPTION;
        
    private SampleSelectionPanel sampleSelectionPanel;
    
    private JPanel processPanel;
    private JRadioButton trainAndClassifyButton;
    private JRadioButton trainOnlyButton;
    private JRadioButton classifyOnlyButton;
    private JRadioButton oneOutValButton;
    private ButtonGroup processSelection;
    
    private HCLSelectionPanel hclOpsPanel;
    
    /** Creates new form SVMTrain_ClassifierSelectDialog */
    public SVMProcessInitDialog(java.awt.Frame parent, boolean modal) {
        super(new JFrame(), "SVM Process Initialization" ,modal);
        initComponents();
        okButton.setText("Continue");
        sampleSelectionPanel.setButtonText("Classify Genes", "Classify Samples");
        Listener listener = new Listener();
        setActionListeners(listener);
       pack();
       setResizable(false);
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new java.awt.GridBagLayout());
        mainPanel.setBackground(Color.white);
        
        processSelection = new ButtonGroup();        
        processPanel = new JPanel();
        trainAndClassifyButton = new JRadioButton();
        trainOnlyButton = new JRadioButton();
        classifyOnlyButton = new JRadioButton();    
        oneOutValButton = new JRadioButton();
        GridBagConstraints gridBagConstraints1;
        
        setBackground(Color.lightGray);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
                closeDialog(evt);
            }
        });
        
        sampleSelectionPanel = new SampleSelectionPanel(Color.white, UIManager.getColor("Label.foreground"),true,"Classification Selection");
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 0;
        gridBagConstraints1.weightx = 1;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints1.insets = new java.awt.Insets(4, 0, 0, 0);
        mainPanel.add(sampleSelectionPanel, gridBagConstraints1);
        
        processPanel.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gridBagConstraints2;
        
        processPanel.setBorder(new javax.swing.border.TitledBorder(new EtchedBorder(EtchedBorder.RAISED), "SVM Process Selection", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), Color.black));
        processPanel.setBackground(Color.white);
        trainAndClassifyButton.setToolTipText("Train SMV then immediately classify");
        trainAndClassifyButton.setSelected(true);
        trainAndClassifyButton.setText("Train SVM then Classify");
        trainAndClassifyButton.setBackground(Color.white);
        trainAndClassifyButton.setForeground(UIManager.getColor("Label.foreground"));
        processSelection.add(trainAndClassifyButton);
        trainAndClassifyButton.setFocusPainted(false);
        gridBagConstraints2 = new java.awt.GridBagConstraints();
        gridBagConstraints2.gridx = 0;
        gridBagConstraints2.gridy = 0;
        gridBagConstraints2.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints2.insets = new java.awt.Insets(5, 0, 5, 0);
        processPanel.add(trainAndClassifyButton, gridBagConstraints2);
        
        trainOnlyButton.setToolTipText("Train SVM only... output are result SVM weights");
        trainOnlyButton.setText("Train SVM (skip classify)");
        trainOnlyButton.setBackground(Color.white);
        trainOnlyButton.setForeground(UIManager.getColor("Label.foreground"));
        processSelection.add(trainOnlyButton);
        trainOnlyButton.setFocusPainted(false);
        gridBagConstraints2 = new java.awt.GridBagConstraints();
        gridBagConstraints2.gridx = 0;
        gridBagConstraints2.gridy = 1;
        gridBagConstraints2.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints2.insets = new java.awt.Insets(5, 0, 5, 0);
        processPanel.add(trainOnlyButton, gridBagConstraints2);
        
        classifyOnlyButton.setToolTipText("Trains current data using and SVM file of weights");
        classifyOnlyButton.setText("Classify using existing SVM file");
        classifyOnlyButton.setBackground(Color.white);
        classifyOnlyButton.setForeground(UIManager.getColor("Label.foreground"));
        processSelection.add(classifyOnlyButton);
        classifyOnlyButton.setFocusPainted(false);
        gridBagConstraints2 = new GridBagConstraints();
        gridBagConstraints2.gridx = 0;
        gridBagConstraints2.gridy = 2;
        gridBagConstraints2.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints2.insets = new java.awt.Insets(5, 0, 5, 0);
        processPanel.add(classifyOnlyButton, gridBagConstraints2);
        
        oneOutValButton.setToolTipText("One-out Iterative Validation");
        oneOutValButton.setText("One-out Iterative Validation * (see information page)");
        oneOutValButton.setBackground(Color.white);
        oneOutValButton.setForeground(UIManager.getColor("Label.foreground"));
        processSelection.add(oneOutValButton);
        oneOutValButton.setFocusPainted(false);
        gridBagConstraints2 = new GridBagConstraints();
        gridBagConstraints2.gridx = 0;
        gridBagConstraints2.gridy = 3;
        gridBagConstraints2.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints2.insets = new java.awt.Insets(5, 0, 5, 0);
        processPanel.add(oneOutValButton, gridBagConstraints2);        
        
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 1;
        gridBagConstraints1.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(processPanel, gridBagConstraints1);
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 2;
        gridBagConstraints1.fill = GridBagConstraints.HORIZONTAL;
        
        
        hclOpsPanel = new HCLSelectionPanel();
        mainPanel.add(hclOpsPanel, gridBagConstraints1);
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 2;
        gridBagConstraints1.fill = GridBagConstraints.HORIZONTAL;
        
        addContent(mainPanel);
    }
    
    /** Closes the dialog */
    private void closeDialog(WindowEvent evt) {
        setVisible(false);
        dispose();
    }
    
    /**
     * Returns true if gene SVM's are to be evaluated
     */
    public boolean isEvaluateGenesSelected(){
        return sampleSelectionPanel.isClusterGenesSelected();
    }
    
    /**
     *  Returns a constant indicating which SVM process to run
     *  Train and classify, train only, classify only
     */
    public int getSVMProcessSelection(){
        if(this.trainAndClassifyButton.isSelected())
            return SVMGUI.TRAIN_AND_CLASSIFY;
        else if(this.trainOnlyButton.isSelected())
            return SVMGUI.TRAIN_ONLY;
        else if(this.classifyOnlyButton.isSelected())
            return SVMGUI.CLASSIFY_ONLY;
        else
            return SVMGUI.ONE_OUT_VALIDATION;
    }
    
    /**
     * Returns boolean selection for calculating HCL on SVM results
     */
    public boolean getHCLSelection(){
        return hclOpsPanel.isHCLSelected();
    }
    
    public int showModal() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);
        show();
        return result;
    }
    
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        new SVMProcessInitDialog(new javax.swing.JFrame(), true).show();
        System.exit(0);
    }
    
    
    
    
    
    private class Listener extends DialogListener {
        
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            if (command.equals("ok-command")) {
                    result = JOptionPane.OK_OPTION;
                dispose();
            } else if (command.equals("cancel-command")) {
                result = JOptionPane.CANCEL_OPTION;
                dispose();
            } else if (command.equals("reset-command")){
                sampleSelectionPanel.setClusterGenesSelected(true);
                hclOpsPanel.setHCLSelected(false);
                trainAndClassifyButton.setSelected(true);
            } else if (command.equals("info-command")){
               HelpWindow hw = new HelpWindow(SVMProcessInitDialog.this, "SVM Process Initialization Dialog");
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

