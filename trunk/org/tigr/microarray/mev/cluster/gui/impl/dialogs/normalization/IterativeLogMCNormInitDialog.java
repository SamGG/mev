/*
 * Copyright @ 2001-2002, The Institute for Genomic Research (TIGR).  
 * All rights reserved.
 *
 * LinRegNormInitDialog.java
 *
 * Created on March 31, 2003, 11:35 AM
 */

package org.tigr.microarray.mev.cluster.gui.impl.dialogs.normalization;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.*;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.*;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.*;


public class IterativeLogMCNormInitDialog extends AlgorithmDialog {
    
    private JComboBox sdComboBox;
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
    public IterativeLogMCNormInitDialog() {
        super(new JFrame(), "Iterative Log Mean Centering Normalization", true);
        sdComboBox = new JComboBox(SD_VALUES);
        sdComboBox.setEditable(false);
        sdComboBox.setSelectedIndex(4);
        
        JPanel sdPanel = new JPanel(new GridBagLayout());
        sdPanel.setBackground(Color.white);
        sdPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Standard Deviation Range"));
        
        sdPanel.add(sdComboBox, new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(30,0,30,0), 0, 0));
        
        addContent(sdPanel);
        setActionListeners(new Listener());
        setSize(new Dimension(450,220));
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
    
    public int showModal(){
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);
        this.show();
        this.dispose();
        return result;
    }
    
    public static void main(String [] args){
        IterativeLogMCNormInitDialog dialog = new IterativeLogMCNormInitDialog();
        dialog.show();
        System.out.println("SD = "+dialog.getSD());
        System.exit(0);
    }
    
    public class Listener implements ActionListener{
        
        public void actionPerformed(ActionEvent ae){
            String command = ae.getActionCommand();
            if(command.equals("ok-command")){
                result = JOptionPane.OK_OPTION;
                dispose();
            } else if(command.equals("reset-command")){
                sdComboBox.setSelectedIndex(4);
            } else if(command.equals("cancel-command")){
                result = JOptionPane.CANCEL_OPTION;
                dispose();
            } else if(command.equals("info-command")){
                HelpWindow hw = new HelpWindow(IterativeLogMCNormInitDialog.this, "Iterative Log Mean Centering Initialization Dialog");
                if(hw.getWindowContent()){
                    hw.setSize(480,555);
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
