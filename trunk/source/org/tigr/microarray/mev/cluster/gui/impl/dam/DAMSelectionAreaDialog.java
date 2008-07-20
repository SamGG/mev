/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: DAMSelectionAreaDialog.java,v $
 */
package org.tigr.microarray.mev.cluster.gui.impl.dam;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.tigr.microarray.mev.cluster.gui.impl.GUIFactory;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.HelpWindow;

public class DAMSelectionAreaDialog extends AlgorithmDialog {
    private int result;
    private JTextField textField1;
    private JTextField textField2;
    private JTextField textField3;
    private JTextField textField4;
    private JTextField textField5;
    private JTextField textField6;
    
    private float positionX;
    private float positionY;
    private float positionZ;
    private float sizeX;
    private float sizeY;
    private float sizeZ;
    
    /**
     * Constructs a <code>DAMSelectionAreaDialog</code> with specified initial parameters.
     */
    public DAMSelectionAreaDialog(Frame parent, float positionX, float positionY, float positionZ,
    float sizeX, float sizeY, float sizeZ) {
        super(new javax.swing.JFrame(), "DAM selection area configuration", true);
        
        this.positionX = positionX;
        this.positionY = positionY;
        this.positionZ = positionZ;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
        
        Listener listener = new Listener();
        addWindowListener(listener);
        
        JPanel parameters = new JPanel(new GridLayout(0, 2, 10, 0));
        parameters.setBorder(new EmptyBorder(20, 20, 20, 10));
        parameters.setBackground(Color.white);
        
        parameters.add(new JLabel("Position X  "));
        textField1 = new JTextField(Float.toString(positionX), 5);
        parameters.add(textField1, BorderLayout.EAST);
        
        parameters.add(new JLabel("Position Y  "));
        textField2 = new JTextField(Float.toString(positionY), 5);
        parameters.add(textField2, BorderLayout.EAST);
        
        parameters.add(new JLabel("Position Z  "));
        textField3 = new JTextField(Float.toString(positionZ), 5);
        parameters.add(textField3, BorderLayout.EAST);
        
        parameters.add(new JLabel("Size X  "));
        textField4 = new JTextField(Float.toString(sizeX), 5);
        parameters.add(textField4, BorderLayout.EAST);
        
        parameters.add(new JLabel("Size Y "));
        textField5 = new JTextField(Float.toString(sizeY), 5);
        parameters.add(textField5, BorderLayout.EAST);
        
        parameters.add(new JLabel("Size Z "));
        textField6 = new JTextField(Float.toString(sizeZ), 5);
        parameters.add(textField6, BorderLayout.EAST);
        
        
        JPanel panel3 = new JPanel(new BorderLayout());
        panel3.setForeground(Color.white);
        panel3.setBorder(BorderFactory.createLineBorder(Color.gray));
        panel3.setBackground(Color.white);
        panel3.add(parameters, BorderLayout.WEST);
        panel3.add(new JLabel(GUIFactory.getIcon("dialog_button_bar.gif")), BorderLayout.EAST);
        
        JPanel panel1 = new JPanel(new BorderLayout());
        panel1.add(panel3, BorderLayout.CENTER);
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
     * Returns x position.
     */
    public float getPositionX() {
        return Float.parseFloat(textField1.getText());
    }
    
    /**
     * Returns y position.
     */
    public float getPositionY() {
        return Float.parseFloat(textField2.getText());
    }
    
    /**
     * Returns z position.
     */
    public float getPositionZ() {
        return Float.parseFloat(textField3.getText());
    }
    
    /**
     * Returns x size.
     */
    public float getSizeX() {
        return Float.parseFloat(textField4.getText());
    }
    
    /**
     * Returns y size.
     */
    public float getSizeY() {
        return Float.parseFloat(textField5.getText());
    }
    
    /**
     * Returns z size.
     */
    public float getSizeZ() {
        return Float.parseFloat(textField6.getText());
    }
    
    public static void main(String [] args){
        DAMSelectionAreaDialog dialog = new DAMSelectionAreaDialog(new Frame(), 0,0,0,5,5,5);
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
                    Float.parseFloat(textField6.getText());
                    result = JOptionPane.OK_OPTION;
                } catch (Exception exception) {
                    result = JOptionPane.CANCEL_OPTION;
                }
                dispose();
            } else if (command.equals("cancel-command")) {
                result = JOptionPane.CANCEL_OPTION;
                dispose();
            } else if (command.equals("reset-command")){                
                textField1.setText(String.valueOf(positionX));
                textField2.setText(String.valueOf(positionY));
                textField3.setText(String.valueOf(positionZ));
                textField4.setText(String.valueOf(sizeX));
                textField5.setText(String.valueOf(sizeY));
                textField6.setText(String.valueOf(sizeZ));
            } else if (command.equals("info-command")){
                HelpWindow helpWindow = new HelpWindow(DAMSelectionAreaDialog.this, "DAM Selection Area Configuration");
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
