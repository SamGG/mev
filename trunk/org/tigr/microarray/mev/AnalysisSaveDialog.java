/*
 * AnalysisSaveDialog.java
 *
 * Created on January 29, 2004, 4:06 PM
 */

package org.tigr.microarray.mev;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
/**
 *
 * @author  braisted
 */
public class AnalysisSaveDialog extends org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog {
    
    int result = JOptionPane.NO_OPTION;
    JCheckBox askAgainBox;    
    
    /** Creates a new instance of AnalysisSaveDialog */
    public AnalysisSaveDialog(JFrame frame) {
        super(frame, "Save Analysis Check", true);
        Listener listener = new Listener();
        
        JPanel buttonPanel = new JPanel(new GridBagLayout());
        JButton okButton = new JButton("Yes");
        okButton.setFocusPainted(false);
        okButton.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        okButton.setPreferredSize(new Dimension(50, 30));
        okButton.setActionCommand("yes");
        okButton.addActionListener(listener);
        
        JButton noButton = new JButton("No");
        noButton.setFocusPainted(false);
        noButton.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        noButton.setPreferredSize(new Dimension(50, 30));
        noButton.setActionCommand("no");
        noButton.addActionListener(listener);
        
        buttonPanel.add(okButton, new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,20), 0,0));
        buttonPanel.add(noButton, new GridBagConstraints(1,0,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,20,0,0), 0,0));
        
        JPanel paramPanel = new JPanel(new GridBagLayout());
        paramPanel.setBackground(Color.white);
        paramPanel.setBorder(BorderFactory.createLineBorder(Color.black));
        JLabel promptLabel = new JLabel("You are exiting without saving the state of the current analysis.");
        promptLabel.setHorizontalAlignment(JLabel.CENTER);
        JLabel promptLabel2 = new JLabel("Would you like to save the analysis?");
        promptLabel2.setHorizontalAlignment(JLabel.CENTER);
        
        askAgainBox = new JCheckBox("Don't show this dialog again", false);
        askAgainBox.setBackground(Color.white);
        askAgainBox.setFocusPainted(false);
        
        paramPanel.add(promptLabel, new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(20,0,0,0), 0, 0));
        paramPanel.add(promptLabel2, new GridBagConstraints(0,1,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(5,0,0,0), 0, 0));
        
        paramPanel.add(askAgainBox, new GridBagConstraints(0,2,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(30,0,0,0), 0, 0));
        
        this.supplantButtonPanel(buttonPanel);
        this.addContent(paramPanel);
        
        this.setSize(470, 240);
    }
    
    
    public int showModal(){
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);        
        this.show();        
        return result;
    }
    
    public void disposeDialog() { this.dispose(); }
    
    public boolean askAgain() {
        return !this.askAgainBox.isSelected();
    }
    
    public class Listener implements ActionListener {
        public void actionPerformed(ActionEvent ae) {
            String command = ae.getActionCommand();
            if(command.equals("yes"))
                result = JOptionPane.YES_OPTION;
            else
                result = JOptionPane.NO_OPTION;
            disposeDialog();
        }
        
    }
    
    
    public static void main(String [] args) {
        AnalysisSaveDialog dialog = new AnalysisSaveDialog(new JFrame());
        int result = dialog.showModal();
        System.out.println("result = "+result);
        System.out.println("ask again = "+dialog.askAgain());
    }
    
}
