/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * $RCSfile: PCAResultConfigDialog.java,v $
 * $Revision: 1.1.1.1 $
 * $Date: 2003-08-21 21:04:24 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.pca;

import java.awt.Frame;
import java.awt.Color;
import java.awt.Toolkit;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JTextField;
import javax.swing.JOptionPane;
import javax.swing.BorderFactory;
import javax.swing.border.EmptyBorder;

import org.tigr.microarray.mev.cluster.gui.impl.GUIFactory;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener;

import org.tigr.microarray.mev.cluster.gui.impl.dialogs.*;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.*;

public class PCAResultConfigDialog extends AlgorithmDialog {
    private int result;
    private JTextField textField1;
    private JTextField textField2;
    private JTextField textField3;
    private JTextField textField4;
    private JTextField textField5;
    private float pointSize;
    private float selectedPointSize;
    private float scaleAxisX;
    float scaleAxisY;
    float scaleAxisZ;
    
    /**
     * Constructs a <code>PCAResultConfigDialog</code> with specified initial parameters.
     */
    public PCAResultConfigDialog(Frame parent, float pointSize, float selectedPointSize, float scaleAxisX, float scaleAxisY, float scaleAxisZ) {
        super(new javax.swing.JFrame(), "PCA Result Configuration", true);
        this.pointSize = pointSize;
        this.selectedPointSize = selectedPointSize;
        this.scaleAxisX = scaleAxisX;
        this.scaleAxisY = scaleAxisY;
        this.scaleAxisZ = scaleAxisZ;
        
        Listener listener = new Listener();
        addWindowListener(listener);
        
        JPanel parameters = new JPanel(new GridLayout(0,2,10,0));
        parameters.setBorder(new EmptyBorder(20, 20, 20, 10));
        parameters.setBackground(Color.white);
        
        parameters.add(new JLabel("Scale axis X  "));
        
        textField1 = new JTextField(Float.toString(scaleAxisX), 5);
        parameters.add(textField1, BorderLayout.EAST);
        
        parameters.add(new JLabel("Scale axis Y  "));
        textField2 = new JTextField(Float.toString(scaleAxisY), 5);
        parameters.add(textField2, BorderLayout.EAST);
        
        parameters.add(new JLabel("Scale axis Z  "));
        textField3 = new JTextField(Float.toString(scaleAxisZ), 5);
        parameters.add(textField3, BorderLayout.EAST);
        
        parameters.add(new JLabel("Pointsize  "));
        textField4 = new JTextField(Float.toString(pointSize), 5);
        parameters.add(textField4, BorderLayout.EAST);
        
        parameters.add(new JLabel("Selected "));
        textField5 = new JTextField(Float.toString(selectedPointSize), 5);
        parameters.add(textField5, BorderLayout.EAST);
        
        
        JPanel panel2 = new JPanel(new BorderLayout());
        panel2.setBorder(new EmptyBorder(10, 0, 0, 0));
        
        
        JPanel panel3 = new JPanel(new BorderLayout());
        panel3.setForeground(Color.white);
        panel3.setBorder(BorderFactory.createLineBorder(Color.gray));
        panel3.setBackground(Color.white);
        panel3.add(parameters, BorderLayout.WEST);
        panel3.add(new JLabel(GUIFactory.getIcon("tigr_logo.gif")), BorderLayout.EAST);
        
        JPanel panel1 = new JPanel(new BorderLayout());
        panel1.add(panel2, BorderLayout.SOUTH);
        panel1.add(panel3, BorderLayout.NORTH);
        addContent(panel1);
        setActionListeners(listener);
        pack();
        setResizable(false);
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
     * Returns a scale of x axis.
     */
    public float getScaleAxisX() {
        return Float.parseFloat(textField1.getText());
    }
    
    /**
     * Returns a scale of y axis.
     */
    public float getScaleAxisY() {
        return Float.parseFloat(textField2.getText());
    }
    
    /**
     * Returns a scale of z axis.
     */
    public float getScaleAxisZ() {
        return Float.parseFloat(textField3.getText());
    }
    
    /**
     * Returns a point size.
     */
    public float getPointSize() {
        return Float.parseFloat(textField4.getText());
    }
    
    /**
     * Returns a selected point size.
     */
    public float getSelectedPointSize() {
        return Float.parseFloat(textField5.getText());
    }
    
    public static void main(String [] args){
        PCAResultConfigDialog dialog = new PCAResultConfigDialog(new Frame(), 1,1,5,5,5);
        dialog.showModal();
    }
    
    /**
     * The listener to listen to the dialog events.
     */
    private class Listener extends DialogListener {
        
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            if (command.equals("ok-command")) {
                try {
                    Float.parseFloat(textField1.getText());
                    Float.parseFloat(textField2.getText());
                    Float.parseFloat(textField3.getText());
                    Float.parseFloat(textField4.getText());
                    Float.parseFloat(textField5.getText());
                    result = JOptionPane.OK_OPTION;
                } catch (Exception exception) {
                    result = JOptionPane.CANCEL_OPTION;
                }
                dispose();
            } else if (command.equals("cancel-command")) {
                result = JOptionPane.CANCEL_OPTION;
                dispose();
            } else if (command.equals("reset-command")){
                textField1.setText(String.valueOf(scaleAxisX));
                textField2.setText(String.valueOf(scaleAxisY));
                textField3.setText(String.valueOf(scaleAxisZ));                
                textField4.setText(String.valueOf(pointSize));
                textField5.setText(String.valueOf(selectedPointSize));
            } else if (command.equals("info-command")){
                HelpWindow helpWindow = new HelpWindow(PCAResultConfigDialog.this, "PCA Result Configuration");
                if(helpWindow.getWindowContent()){
                    helpWindow.setSize(450, 600);
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
}