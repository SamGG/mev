/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: HCLInitDialog.java,v $
 * $Revision: 1.1.1.1 $
 * $Date: 2003-08-21 21:04:25 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.hcl;

import java.awt.Color;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.Dimension;

import java.awt.event.KeyEvent;
import java.awt.event.ItemEvent;
import java.awt.event.WindowEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ItemListener;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JDialog;
import javax.swing.JButton;
import javax.swing.UIManager;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import javax.swing.BorderFactory;
import javax.swing.border.EmptyBorder;

import org.tigr.microarray.mev.cluster.gui.impl.GUIFactory;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener;

import org.tigr.microarray.mev.cluster.gui.impl.dialogs.ParameterPanel;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.HelpWindow;

public class HCLInitDialog extends AlgorithmDialog {//JDialog {
    
    private int result;
    
    private JCheckBox genes_box;
    private JCheckBox cluster_box;
    private JRadioButton ALC;
    private JRadioButton CLC;
    private JRadioButton SLC;
    
    /**
     * Constructs the dialog.
     */
    public HCLInitDialog(Frame parent) {
        super(new JFrame(), "HCL: Hierarchical Clustering", true);
        setResizable(false);
        
        Listener listener = new Listener();
        addWindowListener(listener);
        
        ALC = new JRadioButton("Average linkage clustering");
        ALC.setBackground(Color.white);
        ALC.setFocusPainted(false);
        ALC.setForeground(UIManager.getColor("Label.foreground"));
        ALC.setMnemonic(KeyEvent.VK_A);
        ALC.setSelected(true);
        
        CLC = new JRadioButton("Complete linkage clustering");
        CLC.setBackground(Color.white);
        CLC.setFocusPainted(false);
        CLC.setForeground(UIManager.getColor("Label.foreground"));
        CLC.setMnemonic(KeyEvent.VK_C);
        
        SLC = new JRadioButton("Single linkage clustering");
        SLC.setBackground(Color.white);
        SLC.setFocusPainted(false);
        SLC.setForeground(UIManager.getColor("Label.foreground"));
        SLC.setMnemonic(KeyEvent.VK_S);
        
        // Group the radio buttons.
        ButtonGroup group = new ButtonGroup();
        group.add(ALC);
        group.add(CLC);
        group.add(SLC);
        
        genes_box = new JCheckBox("Cluster genes");
        genes_box.setSelected(true);
        genes_box.setFocusPainted(false);
        genes_box.setBackground(Color.white);
        genes_box.setForeground(UIManager.getColor("Label.foreground"));
        genes_box.addItemListener(listener);
        
        cluster_box = new JCheckBox("Cluster experiments");
        cluster_box.setSelected(true);
        cluster_box.setFocusPainted(false);
        cluster_box.setBackground(Color.white);
        cluster_box.setForeground(UIManager.getColor("Label.foreground"));
        cluster_box.addItemListener(listener);
        
        JPanel parameters = new JPanel(new GridLayout(0, 2, 10, 10));
        //    ParameterPanel parameters = new ParameterPanel();
        parameters.setLayout(new GridLayout(0, 2, 10, 10));
        parameters.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        parameters.setBackground(Color.white);
        parameters.setForeground(Color.black);
        parameters.add(ALC);
        parameters.add(genes_box);
        parameters.add(CLC);
        parameters.add(cluster_box);
        parameters.add(SLC);
        
        ParameterPanel parameterPanel = new ParameterPanel();
        parameterPanel.add(parameters);
        
        addContent(parameterPanel);
        setActionListeners(listener);
        
        //this.getContentPane().add(panel1, BorderLayout.CENTER);
        this.pack();
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
     * Resets controls to default initial settings
     */
    private void resetControls(){
        ALC.setSelected(true);
        genes_box.setSelected(true);
        cluster_box.setSelected(true);
    }
    
    /**
     * Returns true, if genes check box is selected.
     */
    public boolean isClusterGenes() {
        return genes_box.isSelected();
    }
    
    /**
     * Returns true, if cluster check box is selected.
     */
    public boolean isClusterExperience() {
        return cluster_box.isSelected();
    }
    
    /**
     * Returns a method code.
     * @return 0 for ALC method, 1 for CLC or -1 otherwise.
     */
    public int getMethod() {
        if (ALC.isSelected()) {
            return 0;
        }
        if (CLC.isSelected()) {
            return 1;
        }
        return -1;
    }
    
    /**
     * The class to listen to the dialog and check boxes items events.
     */
    private class Listener extends DialogListener implements ItemListener {
        
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            if (command.equals("ok-command")) {
                result = JOptionPane.OK_OPTION;
                dispose();
            } else if (command.equals("cancel-command")) {
                result = JOptionPane.CANCEL_OPTION;
                dispose();
            }
            else if (command.equals("reset-command")){
                resetControls();
                result = JOptionPane.CANCEL_OPTION;
                return;
            }
            else if (command.equals("info-command")){
                HelpWindow hw = new HelpWindow(HCLInitDialog.this, "HCL Initialization Dialog");
                result = JOptionPane.CANCEL_OPTION;
                if(hw.getWindowContent()){
                    hw.setSize(450,600);
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
        
        public void itemStateChanged(ItemEvent e) {
            okButton.setEnabled(genes_box.isSelected() || cluster_box.isSelected());
        }
        
        public void windowClosing(WindowEvent e) {
            result = JOptionPane.CLOSED_OPTION;
            dispose();
        }
    }
    
    public static void main(String[] args) {
        javax.swing.JFrame frame = new javax.swing.JFrame("Test");
        while (true) {
            HCLInitDialog dialog = new HCLInitDialog(frame);
            if (dialog.showModal() != JOptionPane.OK_OPTION) {
                System.exit(0);
            }
            System.out.println("===============================");
            System.out.println(dialog.isClusterGenes());
            System.out.println(dialog.isClusterExperience());
            System.out.println(dialog.getMethod());
        }
    }
    
    protected void disposeDialog() {
    }
    
}