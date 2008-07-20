/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * KNNClassifyOrValidateDialog.java
 *
 * Created on September 23, 2003, 11:05 AM
 */

package org.tigr.microarray.mev.cluster.gui.impl.knnc;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;

import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.EtchedBorder;

import org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.HelpWindow;

/**
 *
 * @author  nbhagaba
 */
public class KNNClassifyOrValidateDialog extends AlgorithmDialog {
    
    boolean okPressed = false;
    JRadioButton classifyButton, validateButton;
    
    /** Creates a new instance of KNNClassifyOrValidateDialog */
    public KNNClassifyOrValidateDialog(JFrame parentFrame, boolean modality) {
        super(parentFrame, "KNN classify or validate", modality);
        okButton.setText("Next >");
        setBounds(0, 0, 500, 200);
        setBackground(Color.white);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);     
        
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.NONE;
        
        JPanel pane = new JPanel();
        pane.setBackground(Color.white);
        pane.setBorder(new EtchedBorder());
        pane.setLayout(gridbag);
        
        classifyButton = new JRadioButton("Classify", true);
        classifyButton.setBackground(Color.white);
        validateButton = new JRadioButton("Validate", false);
        //validateButton.setEnabled(false);
        validateButton.setBackground(Color.white);
        
        ButtonGroup classifyOrValidate =new ButtonGroup();
        classifyOrValidate.add(classifyButton);
        classifyOrValidate.add(validateButton);
        
        buildConstraints(constraints, 0, 0, 1, 1, 50, 100);
        gridbag.setConstraints(classifyButton, constraints);
        pane.add(classifyButton);

        buildConstraints(constraints, 1, 0, 1, 1, 50, 0);
        gridbag.setConstraints(validateButton, constraints);
        pane.add(validateButton);        
        
        addContent(pane);
        EventListener listener = new EventListener();        
        setActionListeners(listener);
        this.addWindowListener(listener);         
    }
    
    public void setVisible(boolean visible) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);
        
        super.setVisible(visible);
    }    
    
    void buildConstraints(GridBagConstraints gbc, int gx, int gy,
    int gw, int gh, int wx, int wy) {
        
        gbc.gridx = gx;
        gbc.gridy = gy;
        gbc.gridwidth = gw;
        gbc.gridheight = gh;
        gbc.weightx = wx;
        gbc.weighty = wy;
    } 
    
    public boolean isOkPressed() {
        return okPressed;
    }   
    
    public boolean classify() {
        return classifyButton.isSelected();
    }
    
    public class EventListener extends WindowAdapter implements ActionListener{
        
        public void actionPerformed(ActionEvent ae) {
            String command = ae.getActionCommand();
            if(command.equals("ok-command")){
                okPressed = true;
                dispose();
            } else if (command.equals("reset-command")) {
                okPressed = false;
                classifyButton.setSelected(true);
                validateButton.setSelected(false);
            } else if (command.equals("cancel-command")) {
                okPressed = false;
                dispose();
            } else if (command.equals("info-command")){
               
                HelpWindow hw = new HelpWindow(KNNClassifyOrValidateDialog.this, "KNNC Mode Selection");
                okPressed = false;
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
        }
        
    }    
}
