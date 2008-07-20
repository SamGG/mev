/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * DAMStatusDialog.java
 *
 */

package org.tigr.microarray.mev.cluster.gui.impl.dam;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.tigr.microarray.mev.cluster.gui.impl.GUIFactory;
/**
 *
 */
public class DAMStatusDialog extends JDialog {
    
    private JLabel statusLabel, iconLabel;
    
    /** Creates a new instance of DAMStatusDialog */
    public DAMStatusDialog(JFrame parentFrame, boolean modality) {
        super(parentFrame, modality);
        this.setTitle("DAM Classify Initialization");
        setBounds(0, 0, 400, 200);
        this.getContentPane().setBackground(Color.white);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        iconLabel = new JLabel(GUIFactory.getIcon("dialog_banner2.gif"));
        iconLabel.setOpaque(false);
        iconLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        statusLabel = new JLabel("DAM Status Dialog");
        statusLabel.setBackground(Color.blue);
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;    
        
        this.getContentPane().setLayout(gridbag);
        
        JPanel iconPanel = new JPanel();
        GridBagLayout grid1 = new GridBagLayout();
        
        iconPanel.setLayout(grid1);
        buildConstraints(constraints, 0, 0, 1, 1, 100, 100);
        grid1.setConstraints(iconPanel, constraints);
        iconPanel.add(iconLabel);
        
        //constraints.fill = GridBagConstraints.NONE;
        buildConstraints(constraints, 0, 0, 1, 1, 100, 3);
        gridbag.setConstraints(iconPanel, constraints);
        this.getContentPane().add(iconPanel);        
        
        buildConstraints(constraints, 0, 1, 1, 1, 0, 97);
        constraints.fill = GridBagConstraints.NONE;
        gridbag.setConstraints(statusLabel, constraints);
        this.getContentPane().add(statusLabel);  
        
        //setContentPane(this);
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
    
    public static void main (String[] args) {
        DAMStatusDialog ksDialog = new DAMStatusDialog(new JFrame(), true);
        ksDialog.setVisible(true);
    }
}
