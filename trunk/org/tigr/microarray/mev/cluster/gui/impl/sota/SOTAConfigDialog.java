/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * $RCSfile: SOTAConfigDialog.java,v $
 * $Revision: 1.3 $
 * $Date: 2005-03-10 20:22:06 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.sota;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.tigr.microarray.mev.cluster.gui.impl.GUIFactory;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.HelpWindow;

public class SOTAConfigDialog extends AlgorithmDialog {
    
    private int result;
    private JTextField zeroTextField;
    private JTextField minTextField;
    private JTextField maxTextField;
    private float zThr;
    private int minPixDist;
    private int maxPixDist;
    
    /**
     * Constructs the dialog.
     */
    public SOTAConfigDialog(Frame parent, float zeroThreshold, int minPixelDistance, int maxPixelDistance) {
        super(parent, "Tree Configuration", true);
        zThr = zeroThreshold;
        minPixDist = minPixelDistance;
        maxPixDist = maxPixelDistance;
        
        Listener listener = new Listener();
        addWindowListener(listener);
        
        JPanel parameters1 = new JPanel(new GridLayout(0, 1));
        parameters1.setBorder(new EmptyBorder(10, 5, 10, 0));
        parameters1.setBackground(Color.white);
        parameters1.add(new JLabel("Distance threshold"));
        parameters1.add(new JLabel("Minimum pixel distance"));
        parameters1.add(new JLabel("Maximum pixel distance"));
        
        JPanel parameters2 = new JPanel(new GridLayout(0, 1));
        parameters2.setBorder(new EmptyBorder(10, 5, 10, 0));
        parameters2.setBackground(Color.white);
        String z = String.valueOf(zeroThreshold);
        z = z.substring(0, Math.min(5, z.length()));
        zeroTextField = new JTextField(z, 4);
        parameters2.add(zeroTextField, BorderLayout.EAST);
        minTextField = new JTextField(String.valueOf(minPixelDistance), 4);
        parameters2.add(minTextField, BorderLayout.EAST);
        maxTextField = new JTextField(String.valueOf(maxPixelDistance), 4);
        parameters2.add(maxTextField, BorderLayout.EAST);
        
        JButton button1 = new JButton("OK");
        button1.setActionCommand("ok-command");
        button1.addActionListener(listener);
        button1.setFocusPainted(false);
        JButton button2 = new JButton("Cancel");
        button2.setActionCommand("cancel-command");
        button2.addActionListener(listener);
        button2.setFocusPainted(false);
        
        JPanel buttons = new JPanel(new GridLayout(0, 2));
        buttons.add(button1);
        buttons.add(button2);
        
        JPanel panel3 = new JPanel(new BorderLayout());
        panel3.setForeground(Color.white);
        panel3.setBorder(BorderFactory.createLineBorder(Color.gray));
        
        JLabel imageControl2 = new JLabel(GUIFactory.getIcon("tigr_logo.gif"));
        panel3.setBackground(Color.white);
        panel3.add(parameters1, BorderLayout.WEST);
        panel3.add(parameters2, BorderLayout.CENTER);
        panel3.add(imageControl2, BorderLayout.EAST);
        
        JPanel panel1 = new JPanel(new BorderLayout());
        //	panel1.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel1.add(panel3, BorderLayout.NORTH);
        addContent(panel1);
        setActionListeners(listener);
        this.pack();
        setResizable(false);
    }
    
    /**
     * Show the dialog in screen's center.
     */
    public int showModal() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);
        show();
        return result;
    }
    
    /**
     * Returns choosed zero threshold value.
     */
    public float getZeroThreshold() {
        return Float.parseFloat(zeroTextField.getText());
    }
    
    /**
     * Returns choosed min distance value.
     */
    public int getMinDistance() {
        return Integer.parseInt(minTextField.getText());
    }
    
    /**
     * Returns choosed max distance value.
     */
    public int getMaxDistance() {
        return Integer.parseInt(maxTextField.getText());
    }
    
    /**
     * The class to listen to the dialog events.
     */
    private class Listener extends DialogListener {
        
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            if (command.equals("ok-command")) {
                try {
                    Float.parseFloat(zeroTextField.getText());
                    Integer.parseInt(minTextField.getText());
                    Integer.parseInt(maxTextField.getText());
                    result = JOptionPane.OK_OPTION;
                    dispose();
                } catch (Exception exc) {
                    result = JOptionPane.CANCEL_OPTION;
                }
            } else if (command.equals("cancel-command")) {
                result = JOptionPane.CANCEL_OPTION;
                dispose();
            } else if(command.equals("reset-command")){
                //reset to inital values
                zeroTextField.setText(String.valueOf(zThr));
                minTextField.setText(String.valueOf(minPixDist));
                maxTextField.setText(String.valueOf(maxPixDist));                
            } else if(command.equals("info-command")){
                HelpWindow helpWindow = new HelpWindow(SOTAConfigDialog.this, "SOTA Tree Properties");
                if(helpWindow.getWindowContent()){
                    helpWindow.setSize(450, 500);
                    helpWindow.setLocation();
                    helpWindow.show();
                }
                else{
                    helpWindow.dispose();
                }
            }
            
        }
        
        public void windowClosing(WindowEvent e) {
            result = JOptionPane.CLOSED_OPTION;
            dispose();
        }
    }
    
    public static void main(String [] args){
        SOTAConfigDialog d = new SOTAConfigDialog(new Frame(), 2,3,4);
        d.show();
    }
}