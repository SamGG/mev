/*
 * Copyright @ 2001-2005, The Institute for Genomic Research (TIGR).  
 * All rights reserved.
 *
 * LinRegNormInitDialog.java
 *
 * Created on March 31, 2003, 11:35 AM
 */

package org.tigr.microarray.mev.cluster.gui.impl.dialogs.normalization;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.EtchedBorder;

import org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.HelpWindow;
/**
 *
 * @author  braisted
 * @version 
 */
public class LinRegNormInitDialog extends AlgorithmDialog {
    
    private JComboBox sdComboBox;
    JRadioButton blockButton, globalButton;
    private int result = JOptionPane.CANCEL_OPTION;
    
    private final String[] SD_VALUES = {
        "+/-0.50 SD",
        "+/-1.00 SD",
        "+/-1.50 SD",
        "+/-1.96 SD",
        "+/-2.00 SD",
        "+/-2.50 SD",
        "+/-3.00 SD"
    };
    
    /** Creates new LinRegNormInitDialog */
    public LinRegNormInitDialog() {
        super(new JFrame(), "Iterative Linear Regression", true);
        blockButton = new JRadioButton("Block Mode", true);
        blockButton.setFocusPainted(false);
        blockButton.setBackground(Color.white);
        blockButton.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        globalButton = new JRadioButton("Global Mode");
        globalButton.setFocusPainted(false);
        globalButton.setBackground(Color.white);
        globalButton.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        ButtonGroup bg = new ButtonGroup();
        bg.add(blockButton);
        bg.add(globalButton);
        sdComboBox = new JComboBox(SD_VALUES);
        sdComboBox.setEditable(false);
        sdComboBox.setSelectedIndex(4);
        
        JPanel parameters = new JPanel(new GridBagLayout());
        parameters.setBackground(Color.white);
        parameters.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Normalization Parameters"));
        
        
        JPanel modePanel = new JPanel(new GridBagLayout());
        modePanel.setBackground(Color.white);
        modePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Normalization Mode"));
        
        JPanel sdPanel = new JPanel(new GridBagLayout());
        sdPanel.setBackground(Color.white);
        sdPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Standard Deviation Range"));
        
        
        modePanel.add(blockButton, new GridBagConstraints(0,0,1,1,1.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(15,0,15,0), 0, 0));
        modePanel.add(globalButton, new GridBagConstraints(1,0,1,1,1.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(15,0,15,0), 0, 0));
        sdPanel.add(sdComboBox, new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(20,0,20,0), 0, 0));
        
        parameters.add(modePanel, new GridBagConstraints(0,0,1,1,1.0,1.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,0,0,0), 0, 0));
        parameters.add(sdPanel, new GridBagConstraints(0,1,1,1,1.0,1.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,0,0,0), 0, 0));
        
        addContent(parameters);
        setActionListeners(new Listener());
        setSize(new Dimension(450,350));
        setResizable(false);   
        pack();
    }
    
    public float getSD(){
        float sd = 0;
        String sdString = (String) sdComboBox.getSelectedItem();
        sdString = sdString.substring(3, 7);
        try{
            sd = Float.parseFloat(sdString);
        } catch (Exception e) { }
        return sd;
    }
    
    public String getMode(){
        if(blockButton.isSelected())
            return "block";
        else
            return "global";
    }
    
    public int showModal(){
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);
        this.show();
        this.dispose();
        return result;
    }
    
    public static void main(String [] args){
        LinRegNormInitDialog dialog = new LinRegNormInitDialog();
        dialog.showModal();
        System.exit(0);
    }
    
    public class Listener implements ActionListener{
        
        public void actionPerformed(ActionEvent ae){
            String command = ae.getActionCommand();
            if(command.equals("ok-command")){
                result = JOptionPane.OK_OPTION;
                dispose();
            } else if(command.equals("reset-command")){
                blockButton.setSelected(true);
                sdComboBox.setSelectedIndex(4);
            } else if(command.equals("cancel-command")){
                result = JOptionPane.CANCEL_OPTION;
                dispose();
            } else if(command.equals("info-command")){
                HelpWindow hw = new HelpWindow(LinRegNormInitDialog.this, "Linear Regression Initialization Dialog");
                if(hw.getWindowContent()){
                    hw.setSize(450,550);
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
