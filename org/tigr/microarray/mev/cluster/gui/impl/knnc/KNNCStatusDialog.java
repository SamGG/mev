/*
 * KNNCStatusDialog.java
 *
 * Created on September 24, 2003, 3:53 PM
 */

package org.tigr.microarray.mev.cluster.gui.impl.knnc;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import org.tigr.graph.*;
import org.tigr.util.*;
import org.tigr.util.awt.*;


import org.tigr.microarray.mev.cluster.gui.impl.GUIFactory;
/**
 *
 * @author  nbhagaba
 */
public class KNNCStatusDialog extends JDialog {
    
    private JLabel statusLabel, iconLabel;
    
    /** Creates a new instance of KNNCStatusDialog */
    public KNNCStatusDialog(JFrame parentFrame, boolean modality) {
        super(parentFrame, modality);
        this.setTitle("KNN Classify Initialization");
        setBounds(0, 0, 400, 200);
        this.getContentPane().setBackground(Color.white);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        iconLabel = new JLabel(GUIFactory.getIcon("dialog_banner2.gif"));
        iconLabel.setOpaque(false);
        iconLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        statusLabel = new JLabel("KNN Classify initialization in progress...");
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
        KNNCStatusDialog ksDialog = new KNNCStatusDialog(new JFrame(), true);
        ksDialog.setVisible(true);
    }
}
