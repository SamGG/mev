/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/

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
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;

import org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.HelpWindow;
/**
 *
 * @author  braisted
 * @version
 */ 
public class RatioStatsNormInitDialog extends AlgorithmDialog {
    
    private JComboBox ciComboBox;
    private int result = JOptionPane.CANCEL_OPTION;
    
    private final String[] CI_VALUES = {
        "95%", "99%"
    };
    
    /** Creates new LinRegNormInitDialog */
    public RatioStatsNormInitDialog() {
        super(new JFrame(), "Ratio Statistics Normalization", true);

        ciComboBox = new JComboBox(CI_VALUES);
        ciComboBox.setEditable(false);
        ciComboBox.setSelectedIndex(0);
      
        JPanel ciPanel = new JPanel(new GridBagLayout());
        ciPanel.setBackground(Color.white);
        ciPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Confidence Interval"));

        ciPanel.add(ciComboBox, new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(30,0,30,0), 0, 0));

        addContent(ciPanel);
        setActionListeners(new Listener());
        setSize(new Dimension(450,220));
        setResizable(false);
        pack();
    }
    
    public int getCI(){
        int ci = 0;
        String ciString = (String) ciComboBox.getSelectedItem();
        ciString = ciString.substring(0, 2);
        try{
            ci = Integer.parseInt(ciString);
        } catch (Exception e) { }
        return ci;
    }
        
    public int showModal(){
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);
        this.show();
        this.dispose();
        return result;
    }
    
    public static void main(String [] args){
        RatioStatsNormInitDialog dialog = new RatioStatsNormInitDialog();
        dialog.show();
         System.out.println("ci = "+dialog.getCI());
        System.exit(0);
    }
    
    public class Listener implements ActionListener{
        
        public void actionPerformed(ActionEvent ae){
            String command = ae.getActionCommand();
            if(command.equals("ok-command")){
                result = JOptionPane.OK_OPTION;
                dispose();
            } else if(command.equals("reset-command")){
                ciComboBox.setSelectedIndex(0);
            } else if(command.equals("cancel-command")){
                result = JOptionPane.CANCEL_OPTION;
                dispose();
            } else if(command.equals("info-command")){
                HelpWindow hw = new HelpWindow(RatioStatsNormInitDialog.this, "Ratio Statistics Initialization Dialog");
                if(hw.getWindowContent()){
                    hw.setSize(450,420);
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
