/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: ResampleTreeInitDialog.java,v $
 * $Revision: 1.1.1.2 $
 * $Date: 2004-02-06 21:48:18 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.st;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import org.tigr.util.awt.*;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.*;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.*;

public class ResampleTreeInitDialog extends AlgorithmDialog {
    
    protected GBA gba;
    protected EventListener eventListener;
    
    protected JPanel geneTreePanel;
    public JCheckBox drawGeneTreeCheckBox;
    protected JPanel geneTreeResamplingOptionsPanel;
    protected JRadioButton geneBootstrapGenes;
    protected JRadioButton geneBootstrapExpts;
    protected JRadioButton geneJackknifeGenes;
    protected JRadioButton geneJackknifeExpts;
    protected JRadioButton geneStandard;
    protected JPanel geneTreeIterationsPanel;
    protected JLabel geneTreeIterationsLabel;
    public JTextField geneTreeIterationsTextField;
    
    protected JPanel exptTreePanel;
    public JCheckBox drawExptTreeCheckBox;
    protected JPanel exptTreeResamplingOptionsPanel;
    protected JRadioButton exptBootstrapGenes;
    protected JRadioButton exptBootstrapExpts;
    protected JRadioButton exptJackknifeGenes;
    protected JRadioButton exptJackknifeExpts;
    protected JRadioButton exptStandard;
    protected JPanel exptTreeIterationsPanel;
    protected JLabel exptTreeIterationsLabel;
    public JTextField exptTreeIterationsTextField;
    
    protected JPanel linkagePanel;
    protected JRadioButton averageLinkage;
    protected JRadioButton completeLinkage;
    protected JRadioButton singleLinkage;
    
    protected JPanel topPanel;
    protected JPanel bottomPanel;
    
    protected ButtonGroup buttonGroup;
    
    protected int linkageStyle = 0;
    protected int geneTreeAnalysisOption = 0;
    protected int exptTreeAnalysisOption = 0;
    
    protected boolean cancelled = true;
    protected Color labelColor;
    
    public final static int NONE = 0;
    public final static int BOOT_EXPTS = 1;
    public final static int BOOT_GENES = 2;
    public final static int JACK_EXPTS = 3;
    public final static int JACK_GENES = 4;
    
    
    public ResampleTreeInitDialog(JFrame parent, boolean modal) {
        super(parent, "ST: Support Trees", modal);
        
        initialize();
    }
    
    protected void initialize() {
        gba = new GBA();
        labelColor = UIManager.getColor("Label.foreground");
        eventListener = new EventListener();
        
        drawGeneTreeCheckBox = new JCheckBox("Draw Gene Tree", true);
        this.drawGeneTreeCheckBox.setFocusPainted(false);
        this.drawGeneTreeCheckBox.setBackground(Color.white);
        this.drawGeneTreeCheckBox.setForeground(labelColor);
        buttonGroup = new ButtonGroup();
        geneBootstrapGenes = new JRadioButton("Bootstrap Genes");
        this.geneBootstrapGenes.setFocusPainted(false);
        this.geneBootstrapGenes.setBackground(Color.white);
        this.geneBootstrapGenes.setForeground(labelColor);
        buttonGroup.add(geneBootstrapGenes);
        geneBootstrapExpts = new JRadioButton("Bootstrap Experiments");
        this.geneBootstrapExpts.setFocusPainted(false);
        this.geneBootstrapExpts.setBackground(Color.white);
        this.geneBootstrapExpts.setForeground(labelColor);
        buttonGroup.add(geneBootstrapExpts);
        geneJackknifeGenes = new JRadioButton("Jackknife Genes");
        this.geneJackknifeGenes.setFocusPainted(false);
        this.geneJackknifeGenes.setBackground(Color.white);
        this.geneJackknifeGenes.setForeground(labelColor);
        buttonGroup.add(geneJackknifeGenes);
        geneJackknifeExpts = new JRadioButton("Jackknife Experiments");
        this.geneJackknifeExpts.setFocusPainted(false);
        this.geneJackknifeExpts.setBackground(Color.white);
        this.geneJackknifeExpts.setForeground(labelColor);
        buttonGroup.add(geneJackknifeExpts);
        geneStandard = new JRadioButton("No resampling", true);
        this.geneStandard.setFocusPainted(false);
        this.geneStandard.setBackground(Color.white);
        this.geneStandard.setForeground(labelColor);
        buttonGroup.add(geneStandard);
        
        geneTreeIterationsLabel = new JLabel("Iterations");
        geneTreeIterationsTextField = new JTextField("100");
        
        geneTreeIterationsPanel = new JPanel();
        geneTreeIterationsPanel.setLayout(new GridBagLayout());
        this.geneTreeIterationsPanel.setBackground(Color.white);
        gba.add(geneTreeIterationsPanel, geneTreeIterationsLabel, 0, 0, 1, 1, 0, 0, GBA.NONE, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
        gba.add(geneTreeIterationsPanel, geneTreeIterationsTextField, 1, 0, 1, 1, 1, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
        
        geneTreeResamplingOptionsPanel = new JPanel();
        geneTreeResamplingOptionsPanel.setLayout(new GridBagLayout());
        this.geneTreeResamplingOptionsPanel.setBackground(Color.white);
        geneTreeResamplingOptionsPanel.setBorder(new TitledBorder(new EtchedBorder(), "Resampling Options", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), Color.black));
        gba.add(geneTreeResamplingOptionsPanel, geneBootstrapGenes, 0, 0, 1, 1, 1, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
        gba.add(geneTreeResamplingOptionsPanel, geneBootstrapExpts, 0, 1, 1, 1, 1, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
        gba.add(geneTreeResamplingOptionsPanel, geneJackknifeGenes, 0, 2, 1, 1, 1, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
        gba.add(geneTreeResamplingOptionsPanel, geneJackknifeExpts, 0, 3, 1, 1, 1, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
        gba.add(geneTreeResamplingOptionsPanel, geneStandard, 0, 4, 1, 1, 1, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
        
        geneTreePanel = new JPanel();
        geneTreePanel.setLayout(new GridBagLayout());
        this.geneTreePanel.setBackground(Color.white);
        geneTreePanel.setBorder(new TitledBorder(new EtchedBorder(), "Gene Tree", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), Color.black));
        
        gba.add(geneTreePanel, drawGeneTreeCheckBox, 0, 0, 1, 1, 1, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
        gba.add(geneTreePanel, geneTreeResamplingOptionsPanel, 0, 1, 1, 1, 1, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
        gba.add(geneTreePanel, geneTreeIterationsPanel, 0, 2, 1, 1, 1, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
        
        drawExptTreeCheckBox = new JCheckBox("Draw Experiment Tree", true);
        drawExptTreeCheckBox.setFocusPainted(false);
        drawExptTreeCheckBox.setBackground(Color.white);
        drawExptTreeCheckBox.setForeground(labelColor);
        buttonGroup = new ButtonGroup();
        exptBootstrapGenes = new JRadioButton("Bootstrap Genes");
        this.exptBootstrapGenes.setFocusPainted(false);
        this.exptBootstrapGenes.setBackground(Color.white);
        this.exptBootstrapGenes.setForeground(labelColor);
        buttonGroup.add(exptBootstrapGenes);
        this.exptBootstrapExpts = new JRadioButton("Bootstrap Experiments");
        this.exptBootstrapExpts.setFocusPainted(false);
        this.exptBootstrapExpts.setBackground(Color.white);
        this.exptBootstrapExpts.setForeground(labelColor);
        buttonGroup.add(exptBootstrapExpts);
        exptJackknifeGenes = new JRadioButton("Jackknife Genes");
        this.exptJackknifeGenes.setFocusPainted(false);
        this.exptJackknifeGenes.setBackground(Color.white);
        this.exptJackknifeGenes.setForeground(labelColor);
        buttonGroup.add(exptJackknifeGenes);
        exptJackknifeExpts = new JRadioButton("Jackknife Experiments");
        this.exptJackknifeExpts.setFocusPainted(false);
        this.exptJackknifeExpts.setBackground(Color.white);
        this.exptJackknifeExpts.setForeground(labelColor);
        buttonGroup.add(exptJackknifeExpts);
        exptStandard = new JRadioButton("No resampling", true);
        this.exptStandard.setFocusPainted(false);
        this.exptStandard.setBackground(Color.white);
        this.exptStandard.setForeground(labelColor);
        buttonGroup.add(exptStandard);
        
        exptTreeIterationsLabel = new JLabel("Iterations");
        exptTreeIterationsTextField = new JTextField("100");
        
        exptTreeIterationsPanel = new JPanel();
        exptTreeIterationsPanel.setLayout(new GridBagLayout());
        this.exptTreeIterationsPanel.setBackground(Color.white);
        gba.add(exptTreeIterationsPanel, exptTreeIterationsLabel, 0, 0, 1, 1, 0, 0, GBA.NONE, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
        gba.add(exptTreeIterationsPanel, exptTreeIterationsTextField, 1, 0, 1, 1, 1, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
        
        exptTreeResamplingOptionsPanel = new JPanel();
        exptTreeResamplingOptionsPanel.setLayout(new GridBagLayout());
        this.exptTreeResamplingOptionsPanel.setBackground(Color.white);
        exptTreeResamplingOptionsPanel.setBorder(new TitledBorder(new EtchedBorder(), "Resampling Options", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), Color.black));
        
        gba.add(exptTreeResamplingOptionsPanel, exptBootstrapGenes, 0, 0, 1, 1, 1, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
        gba.add(exptTreeResamplingOptionsPanel, exptBootstrapExpts, 0, 1, 1, 1, 1, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
        gba.add(exptTreeResamplingOptionsPanel, exptJackknifeGenes, 0, 2, 1, 1, 1, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
        gba.add(exptTreeResamplingOptionsPanel, exptJackknifeExpts, 0, 3, 1, 1, 1, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
        gba.add(exptTreeResamplingOptionsPanel, exptStandard, 0, 4, 1, 1, 1, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
        
        exptTreePanel = new JPanel();
        exptTreePanel.setLayout(new GridBagLayout());
        this.exptTreePanel.setBackground(Color.white);
        exptTreePanel.setBorder(new TitledBorder(new EtchedBorder(), "Experiment Tree", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), Color.black));
        
        gba.add(exptTreePanel, drawExptTreeCheckBox, 0, 0, 1, 1, 1, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
        gba.add(exptTreePanel, exptTreeResamplingOptionsPanel, 0, 1, 1, 1, 1, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
        gba.add(exptTreePanel, exptTreeIterationsPanel, 0, 2, 1, 1, 1, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
        
        topPanel = new JPanel();
        topPanel.setLayout(new GridBagLayout());
        topPanel.setBackground(Color.white);
        gba.add(topPanel, geneTreePanel, 0, 0, 1, 1, 1, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
        gba.add(topPanel, exptTreePanel, 1, 0, 1, 1, 1, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
        
        buttonGroup = new ButtonGroup();
        averageLinkage = new JRadioButton("Average Linkage", true);
        this.averageLinkage.setFocusPainted(false);
        this.averageLinkage.setBackground(Color.white);
        this.averageLinkage.setForeground(labelColor);
        buttonGroup.add(averageLinkage);
        completeLinkage = new JRadioButton("Complete Linkage", false);
        this.completeLinkage.setFocusPainted(false);
        this.completeLinkage.setBackground(Color.white);
        this.completeLinkage.setForeground(labelColor);
        buttonGroup.add(completeLinkage);
        singleLinkage = new JRadioButton("Single Linkage", false);
        this.singleLinkage.setFocusPainted(false);
        this.singleLinkage.setBackground(Color.white);
        this.singleLinkage.setForeground(labelColor);
        buttonGroup.add(singleLinkage);
        
        linkagePanel = new JPanel();
        linkagePanel.setLayout(new GridBagLayout());
        linkagePanel.setBackground(Color.white);
        linkagePanel.setBorder(new TitledBorder(new EtchedBorder(), "Linkage Method", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), Color.black));
        
        gba.add(linkagePanel, averageLinkage, 0, 0, 1, 1, 1, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
        gba.add(linkagePanel, completeLinkage, 1, 0, 1, 1, 1, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
        gba.add(linkagePanel, singleLinkage, 2, 0, 1, 1, 1, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
        
        
        bottomPanel = new JPanel();
        bottomPanel.setLayout(new GridBagLayout());
        bottomPanel.setBackground(Color.white);
        gba.add(bottomPanel, linkagePanel, 0, 0, 2, 1, 1, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
        
        JPanel contentPane = new JPanel();
        contentPane.setLayout(new GridBagLayout());
        contentPane.setBackground(Color.white);
        
        gba.add(contentPane, topPanel, 0, 0, 1, 1, 1, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
        gba.add(contentPane, bottomPanel, 0, 1, 1, 1, 1, 1, GBA.B, GBA.C, new Insets(0, 5, 5, 5), 0, 0);
        
        addContent(contentPane);
        setActionListeners(eventListener);
        
        pack();
        setResizable(false);
    }
    
    public void setVisible(boolean visible) {
        
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);
        
        super.setVisible(visible);
        if (visible) {
            
            
        }
    }
    
    public int getMethod(){
        return linkageStyle;
    }
    
    public boolean isCancelled() {
        return cancelled;
    }
    
    public int getGeneTreeAnalysisOption() {
        return geneTreeAnalysisOption;
    }
    
    public int getExptTreeAnalysisOption() {
        return exptTreeAnalysisOption;
    }
    
    protected void fireOkButtonEvent() {
        
        int geneTreeStyle;
        int exptTreeStyle;
        
        boolean drawGeneTree = drawGeneTreeCheckBox.isSelected();
        int geneTreeIterations = Integer.parseInt(geneTreeIterationsTextField.getText());
        boolean drawExptTree = drawExptTreeCheckBox.isSelected();
        int exptTreeIterations = Integer.parseInt(exptTreeIterationsTextField.getText());
        
        
        if (geneBootstrapGenes.isSelected()) geneTreeAnalysisOption = BOOT_GENES;
        else if (geneBootstrapExpts.isSelected()) geneTreeAnalysisOption = BOOT_EXPTS;
        else if (geneJackknifeGenes.isSelected()) geneTreeAnalysisOption = JACK_GENES;
        else if (geneJackknifeExpts.isSelected()) geneTreeAnalysisOption = JACK_EXPTS;
        else geneTreeAnalysisOption = NONE;
        
        if (exptBootstrapGenes.isSelected()) exptTreeAnalysisOption = BOOT_GENES;
        else if (exptBootstrapExpts.isSelected()) exptTreeAnalysisOption = BOOT_EXPTS;
        else if (exptJackknifeGenes.isSelected()) exptTreeAnalysisOption = JACK_GENES;
        else if (exptJackknifeExpts.isSelected()) exptTreeAnalysisOption = JACK_EXPTS;
        else exptTreeAnalysisOption = NONE;
        
        
        if (completeLinkage.isSelected()) linkageStyle = 1;
        else if (singleLinkage.isSelected()) linkageStyle = -1;
        else linkageStyle = 0;
    }
    
    private void resetControls(){
        this.drawGeneTreeCheckBox.setSelected(true);
        this.drawExptTreeCheckBox.setSelected(true);
        this.geneStandard.setSelected(true);
        this.exptStandard.setSelected(true);
        this.averageLinkage.setSelected(true);
        this.geneTreeIterationsTextField.setText("100");
        this.exptTreeIterationsTextField.setText("100");
    }
    
    public static void main(String [] args){
        ResampleTreeInitDialog dialog = new ResampleTreeInitDialog(new JFrame(), true);
        dialog.show();
        System.exit(0);
    }
    
    protected class EventListener implements ActionListener {
        
        public void actionPerformed(ActionEvent e){
            String command = e.getActionCommand();
            
            int exptIter, geneIter;
            if (command.equals("ok-command")) {
                if(ResampleTreeInitDialog.this.drawExptTreeCheckBox.isSelected()){
                    try{
                        exptIter = Integer.parseInt(exptTreeIterationsTextField.getText());                        
                        if(exptIter < 1){
                            exptTreeIterationsTextField.requestFocus();
                            exptTreeIterationsTextField.selectAll();
                            JOptionPane.showMessageDialog(ResampleTreeInitDialog.this, "Number of iterations must be > 0", "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }                                                
                    } catch (NumberFormatException e1) {
                        exptTreeIterationsTextField.requestFocus();
                        exptTreeIterationsTextField.selectAll();
                        JOptionPane.showMessageDialog(ResampleTreeInitDialog.this, "Entry format error.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
                if(ResampleTreeInitDialog.this.drawGeneTreeCheckBox.isSelected()){
                    try{
                        geneIter = Integer.parseInt(geneTreeIterationsTextField.getText());                        
                        if(geneIter < 1){
                            geneTreeIterationsTextField.requestFocus();
                            geneTreeIterationsTextField.selectAll();
                            JOptionPane.showMessageDialog(ResampleTreeInitDialog.this, "Number of iterations must be > 0", "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    } catch (NumberFormatException e1) {
                        geneTreeIterationsTextField.requestFocus();
                        geneTreeIterationsTextField.selectAll();
                        JOptionPane.showMessageDialog(ResampleTreeInitDialog.this, "Entry format error.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }                    
                }
                fireOkButtonEvent();
                cancelled = false;
            } else if (command.equals("cancel-command")) {
                cancelled = true;
            }else if (command.equals("reset-command")){
                resetControls();
                cancelled = false;
                return;
            }
            else if (command.equals("info-command")){
                HelpWindow hw = new HelpWindow(ResampleTreeInitDialog.this, "ST Initialization Dialog");
                cancelled = false;
                if(hw.getWindowContent()){
                    hw.setSize(450,650);
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
            dispose();
        }
        
    }
}