/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: QTCInitDialog.java,v $
 * $Revision: 1.1.1.1 $
 * $Date: 2003-08-21 21:04:24 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.qtc;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import org.tigr.util.awt.*;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.*;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.*;

public class QTCInitDialog extends AlgorithmDialog {
    
    protected GBA gba;
    protected EventListener eventListener;
    
    protected JPanel inputPanel;
    public JLabel diameterLabel;
    public JTextField diameterTextField;
    public JLabel clusterLabel;
    public JTextField clusterTextField;
    
    protected JPanel useAbsolutePanel;
    public JCheckBox useAbsoluteCheckBox;
    public int result;
    
    private SampleSelectionPanel sampleSelectionPanel;
    private HCLSelectionPanel hclOpsPanel;
    
    protected JPanel mainPanel;
    
    private boolean okPressed = false;
    
    public QTCInitDialog(JFrame parent, boolean modal) {
        super(parent, "QTC: QT Cluster", modal);
        
        initialize();
    }
    
    protected void initialize() {
        gba = new GBA();
        eventListener = new EventListener();
        
        diameterLabel = new JLabel("Maximum Cluster Diameter");    diameterLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        diameterTextField = new JTextField(5);
        
        diameterTextField.setText("0.5");
        clusterLabel = new JLabel("Minimum Cluster Population");
        
        clusterTextField = new JTextField(5);
        
        clusterTextField.setText("5");
        
        sampleSelectionPanel = new SampleSelectionPanel(Color.white, UIManager.getColor("Label.foreground"),true,"Sample Selection");
        
        inputPanel = new JPanel();
        inputPanel.setLayout(new GridBagLayout());
        inputPanel.setBackground(Color.white);
        gba.add(inputPanel, diameterLabel, 0, 0, 1, 1, 0, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
        gba.add(inputPanel, clusterLabel, 0, 1, 1, 0, 0, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
        gba.add(inputPanel, diameterTextField, 1, 0, 1, 1, 0, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
        gba.add(inputPanel, clusterTextField, 1, 1, 1, 1, 0, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
        
        useAbsoluteCheckBox = new JCheckBox("Use Absolute R");
        useAbsoluteCheckBox.setForeground(UIManager.getColor("Label.foreground"));
        useAbsoluteCheckBox.setBackground(Color.white);
        useAbsoluteCheckBox.setFocusPainted(false);
        
        useAbsolutePanel = new JPanel();
        useAbsolutePanel.setBackground(Color.white);
        useAbsolutePanel.setLayout(new GridBagLayout());
        gba.add(useAbsolutePanel, useAbsoluteCheckBox, 0, 0, 1, 1, 0, 0, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
        
        hclOpsPanel = new HCLSelectionPanel();
        
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        mainPanel.setBackground(Color.white);
        
        ParameterPanel parameters = new ParameterPanel();
        parameters.setLayout(new GridBagLayout());
        gba.add(parameters, inputPanel, 0, 0, 1, 1, 1, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
        gba.add(parameters, useAbsolutePanel, 0, 1, 2, 1, 0, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
        
        gba.add(mainPanel, sampleSelectionPanel, 0, 0, 1, 1, 1, 1, GBA.H, GBA.C, new Insets(0, 0, 0, 0), 0, 0);
        gba.add(mainPanel, parameters, 0, 1, 1, 1, 1, 1, GBA.H, GBA.C, new Insets(0, 0, 0, 0), 0, 0);
        gba.add(mainPanel, hclOpsPanel, 0, 2, 1, 1, 1, 1, GBA.H, GBA.C, new Insets(0, 0, 0, 0), 0, 0);
        
        
        setActionListeners(eventListener);
        addContent(mainPanel);
        
        pack();
    }
    
    public void setVisible(boolean visible) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);
        
        super.setVisible(visible);
        
        if (visible) {
            okButton.requestFocus();
        }
    }
    
  /*  protected void fireOkButtonEvent() {
   
        boolean useAbsolute;
        boolean drawTrees;
        double diameter;
        int clusterSize;
   
        useAbsolute = useAbsoluteCheckBox.isSelected();
        drawTrees = drawTreesCheckBox.isSelected();
   
        try {
            diameter = Double.parseDouble(diameterTextField.getText());
        } catch (NumberFormatException nfe) {
            diameter = 1;
        }
   
        try {
            clusterSize = Integer.parseInt(clusterTextField.getText());
        } catch (NumberFormatException nfe) {
            clusterSize = 1;
        }
   
        Hashtable hash = new Hashtable();
        hash.put(new String("useAbsolute"), new Boolean(useAbsolute));
        hash.put(new String("diameter"), new Double(diameter));
        hash.put(new String("clusterSize"), new Integer(clusterSize));
        hash.put(new String("drawTrees"), new Boolean(drawTrees));
        fireEvent(new ActionInfoEvent(this, hash));
    }
   */
    
    public boolean isOkPressed() {return this.okPressed;}
    
    /**
     *  Returns true if clustering genes is selectd
     */
    public boolean isClusterGenesSelected(){
        return this.sampleSelectionPanel.isClusterGenesSelected();
    }
    
    /**
     *  Returns true is HCL clustering is selected
     */
    public boolean isHCLSelected(){
        return this.hclOpsPanel.isHCLSelected();
    }
    
    /**
     * Resets controls
     */
    private void resetControls(){
        this.sampleSelectionPanel.setClusterGenesSelected(true);
        this.hclOpsPanel.setHCLSelected(false);
        this.clusterTextField.setText("5");
        this.diameterTextField.setText("0.5");
        this.useAbsoluteCheckBox.setSelected(false);
    }
    
    /**
     * Validates input
     */
    private boolean validInput(int k, float d){
        boolean valid = true;
        if( d <=0 ){
            JOptionPane.showMessageDialog(QTCInitDialog.this, "Diameter must be > 0", "Input Error!", JOptionPane.ERROR_MESSAGE);
            this.diameterTextField.requestFocus();
            this.diameterTextField.selectAll();
            valid = false;
        }
        else if( k < 1){
            JOptionPane.showMessageDialog(QTCInitDialog.this, "Population of a cluster must be > 0", "Input Error!", JOptionPane.ERROR_MESSAGE);
            this.clusterTextField.requestFocus();
            this.clusterTextField.selectAll();
            valid = false;
        }
        return valid;
    }
    
    
    public static void main(String [] agrs){
        QTCInitDialog hgid = new QTCInitDialog(new JFrame(), true);
        hgid.show();
        System.exit(0);
    }
    
    
    protected class EventListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            String command = event.getActionCommand();
            if (command.equals("ok-command")) {
                int progress = 0;
                int k;
                float d;
                try {
                    d = Float.parseFloat(diameterTextField.getText());
                    progress++;
                    k = Integer.parseInt(clusterTextField.getText());
                    progress++;
                    
                } catch (NumberFormatException nfe) {
                    if(progress == 0){
                        diameterTextField.requestFocus();
                        diameterTextField.selectAll();
                    } else if(progress == 1){
                        clusterTextField.requestFocus();
                        clusterTextField.selectAll();
                    }
                    JOptionPane.showMessageDialog(QTCInitDialog.this, "Number format error.", "Number Format Error", JOptionPane.ERROR_MESSAGE);                    
                    result = JOptionPane.CANCEL_OPTION;
                    okPressed = false;
                    return;
                }
                if(validInput(k,d)){
                    result = JOptionPane.OK_OPTION;
                    okPressed = true; 
                    dispose();
                   
                }
                else{
                    result = JOptionPane.CANCEL_OPTION;
                    okPressed = false;
                }
            } else if (command.equals("cancel-command")) {
                result = JOptionPane.CANCEL_OPTION;
                okPressed = false;
                dispose();
            } else if (command.equals("reset-command")) {
                resetControls();
            } else if (command.equals("info-command")) {
                HelpWindow hw = new HelpWindow(QTCInitDialog.this, "QTC Initialization Dialog");
                if(hw.getWindowContent()){
                    hw.setSize(450,500);
                    hw.setLocation();
                    hw.show();
                }
                else {
                    hw.setVisible(false);
                    hw.dispose();
                }
            }
        }
    }
}