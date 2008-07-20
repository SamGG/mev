/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * HCLPanel.java
 *
 * Created on March 5, 2003, 8:35 PM
 */

package org.tigr.microarray.mev.cluster.gui.impl.dialogs;

import java.awt.Color;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

public class HCLSelectionPanel extends JPanel {
    
    private JCheckBox hclCluster;
    
    /** Creates new HCLPanel */
    public HCLSelectionPanel() {
        super();
        this.setBackground(Color.white);
        Font font = new Font("Dialog", Font.BOLD, 12);
        this.setBorder( BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Hierarchical Clustering", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, font, Color.black));
        hclCluster = new JCheckBox("Construct Hierarchical Trees");
        hclCluster.setFocusPainted(false);
        hclCluster.setBackground(Color.white);
        hclCluster.setForeground(UIManager.getColor("Label.foreground"));
        add(hclCluster);
    }
    
    public HCLSelectionPanel(Color background){
        this();
        setBackground(background);
    }
    
    public boolean isHCLSelected(){
        return hclCluster.isSelected();
    }
    
    public void setHCLSelected(boolean value){
            hclCluster.setSelected(value);
    }
    
}
