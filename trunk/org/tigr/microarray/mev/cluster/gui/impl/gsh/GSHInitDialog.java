/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: GSHInitDialog.java,v $
 * $Revision: 1.1.1.1 $
 * $Date: 2003-08-21 21:04:25 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.gsh;

import java.awt.Frame;
import java.awt.Color;
import java.awt.Toolkit;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;

import javax.swing.JLabel;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.UIManager;
import javax.swing.JCheckBox;
import javax.swing.JTextField;
import javax.swing.JOptionPane;
import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;
import javax.swing.BorderFactory;
import javax.swing.border.EmptyBorder;

import org.tigr.microarray.mev.cluster.gui.impl.GUIFactory;

import org.tigr.microarray.mev.cluster.gui.impl.dialogs.*;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.*;



public class GSHInitDialog extends AlgorithmDialog {
    
    private int result;
    private JTextField textField1;
    private JTextField textField2;
    private JTextField textField3;
    
    private SampleSelectionPanel sampleSelectionPanel;
    private HCLSelectionPanel hclOpsPanel;
    
    public GSHInitDialog(Frame parent, int clusters, int fm, int st) {
        super(new JFrame(), "GSH: Gene Shaving", true);
        
        Listener listener = new Listener();
        addWindowListener(listener);
        
        //sample selction panel
        sampleSelectionPanel = new SampleSelectionPanel(Color.white, UIManager.getColor("Label.foreground"),true,"Sample Selection");
        
        //parameter panel
        ParameterPanel parameters = new ParameterPanel();
        parameters.setLayout(new GridBagLayout());
        
        //add parameter controls
        GridBagConstraints gbc = new GridBagConstraints();
        
        gbc.insets = new Insets(0,0,0,0);
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridy = 0;
        parameters.add(new JLabel("Number of clusters"), gbc);
        gbc.gridy = 1;
        parameters.add(new JLabel("Number of permuted matrices"), gbc);
        gbc.gridy = 2;
        parameters.add(new JLabel("Number of permutations/matrix"), gbc);
        
        gbc.gridwidth = 2;
        textField1 = new JTextField(String.valueOf(clusters), 5);
        textField2 = new JTextField(String.valueOf(fm), 5);
        textField3 = new JTextField(String.valueOf(st), 5);
        gbc.insets = new Insets(0, 10, 0, 0);
        gbc.gridx = 1;
        gbc.gridy = 0;
        parameters.add(textField1, gbc);
        gbc.gridy = 1;
        parameters.add(textField2, gbc);
        gbc.gridy = 2;
        parameters.add(textField3, gbc);
        
        //HCL selection panel
        hclOpsPanel = new HCLSelectionPanel();
        
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new GridBagLayout());
        contentPanel.setBackground(Color.white);
        contentPanel.add(this.sampleSelectionPanel, new GridBagConstraints(0,0,1,1,1.0,0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0));
        contentPanel.add(parameters, new GridBagConstraints(0,1,1,1,0.0,0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0));
        contentPanel.add(this.hclOpsPanel, new GridBagConstraints(0,2,1,1,0.0,0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0));
        
        addContent(contentPanel);
        setActionListeners(listener);
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
     * Returns true if clustering genes is selected
     */
    public boolean isClusterGenesSelected(){
        return this.sampleSelectionPanel.isClusterGenesSelected();
    }
    
    
    public int getFM() {
        return Integer.parseInt(textField2.getText());
    }
    
    public int getST() {
        return Integer.parseInt(textField2.getText());
    }
    
    /**
     * Returns true if the hierarchical checkbox is selected.
     */
    public boolean isHierarchicalTree() {
        return this.hclOpsPanel.isHCLSelected();
    }
    
    /**
     * Resets controls
     */
    private void resetControls(){
        this.sampleSelectionPanel.setClusterGenesSelected(true);
        this.hclOpsPanel.setHCLSelected(false);
        this.textField1.setText("10");
        this.textField2.setText("20");
        this.textField3.setText("5");
    }
    
    /**
     * Validates input values
     */
    private boolean validInput(int k, int p, int s){
        boolean valid = true;
        if(k < 1 ){
            JOptionPane.showMessageDialog(GSHInitDialog.this, "Number of clusters must be > 0", "Input Error!", JOptionPane.ERROR_MESSAGE);
            this.textField1.requestFocus();
            this.textField1.selectAll();
            valid = false;
        }
        else if(p <= 0 ){
            JOptionPane.showMessageDialog(GSHInitDialog.this, "Number of permuted matricies must be > 0", "Input Error!", JOptionPane.ERROR_MESSAGE);
            this.textField2.requestFocus();
            this.textField2.selectAll();
            valid = false;
        }
        else if(s < 0 ){
            JOptionPane.showMessageDialog(GSHInitDialog.this, "Number permutations must be >= 0", "Input Error!", JOptionPane.ERROR_MESSAGE);
            this.textField3.requestFocus();
            this.textField3.selectAll();
            valid = false;
        }
        return valid;
    }
    
    /**
     * The class to listen to the dialog events.
     */
    private class Listener extends DialogListener {
        
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            if (command.equals("ok-command")) {
                int progress = 0;
                int k, p, s;
                try {
                    k = Integer.parseInt(textField1.getText());
                    progress++;
                    p = Integer.parseInt(textField2.getText());
                    progress++;
                    s = Integer.parseInt(textField3.getText());
                    progress++;
                    result = JOptionPane.OK_OPTION;
                } catch (NumberFormatException nfe) {
                    if(progress == 0){
                        textField1.requestFocus();
                        textField1.selectAll();
                    } else if(progress == 1){
                        textField2.requestFocus();
                        textField2.selectAll();
                    } else if(progress == 2){
                        textField3.requestFocus();
                        textField3.selectAll();
                    }
                    JOptionPane.showMessageDialog(GSHInitDialog.this, "Number format error.", "Number Format Error", JOptionPane.ERROR_MESSAGE);
                    result = JOptionPane.CANCEL_OPTION;
                    return;
                }
                if(validInput(k,p,s))
                    dispose();
                else{
                    result = JOptionPane.CANCEL_OPTION;
                }
            } else if (command.equals("cancel-command")) {
                result = JOptionPane.CANCEL_OPTION;
                dispose();
            } else if (command.equals("reset-command")) {
                resetControls();
            } else if (command.equals("info-command")) {
                HelpWindow hw = new HelpWindow(GSHInitDialog.this, "GSH Initialization Dialog");
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
        
        public void windowClosing(WindowEvent e) {
            result = JOptionPane.CLOSED_OPTION;
            dispose();
        }
    }
    
    public static void main(String[] args) {
        GSHInitDialog dlg = new GSHInitDialog(new Frame(), 0 ,0,0);
        dlg.showModal();
        System.exit(0);
    }
}