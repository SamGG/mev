/*
 * HCLSigOnlyPanel.java
 *
 * Created on August 30, 2004, 1:40 PM
 */

package org.tigr.microarray.mev.cluster.gui.impl.dialogs;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

/**
 *
 * @author  nbhagaba
 */
public class HCLSigOnlyPanel  extends JPanel {
    
    private JCheckBox hclCluster;  
    private JRadioButton sigOnly, allClusters;
    
    /** Creates a new instance of HCLSigOnlyPanel */
    public HCLSigOnlyPanel() {
        super();
        this.setBackground(Color.white);
        Font font = new Font("Dialog", Font.BOLD, 12);
        this.setBorder( BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Hierarchical Clustering", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, font, Color.black));
        hclCluster = new JCheckBox("Construct Hierarchical Trees for :            ");
        hclCluster.setFocusPainted(false);
        hclCluster.setBackground(Color.white);
        hclCluster.setForeground(UIManager.getColor("Label.foreground"));
        
        sigOnly = new JRadioButton("Significant genes only", true);
        sigOnly.setBackground(Color.white);
        sigOnly.setForeground(UIManager.getColor("Label.foreground"));     
        
        allClusters = new JRadioButton("All clusters", false);
        allClusters.setBackground(Color.white);
        allClusters.setForeground(UIManager.getColor("Label.foreground"));        

        sigOnly.setEnabled(false);
        allClusters.setEnabled(false);
        
        ButtonGroup allOrSig = new ButtonGroup();
        allOrSig.add(sigOnly);
        allOrSig.add(allClusters);
        
        hclCluster.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.DESELECTED) {
                    sigOnly.setEnabled(false);
                    allClusters.setEnabled(false);
                } else {
                    sigOnly.setEnabled(true);
                    allClusters.setEnabled(true);                    
                }
            }
        });        
        
        add(hclCluster);
        add(sigOnly);
        add(allClusters);
    }
    
    public HCLSigOnlyPanel(Color background){
        this();
        setBackground(background);
    }
    
    public boolean isHCLSelected(){
        return hclCluster.isSelected();
    }  
    
    public boolean drawSigTreesOnly() {
        return sigOnly.isSelected();
    }
    
    public void setHCLSelected(boolean value){
            hclCluster.setSelected(value);
    }    
    
}
