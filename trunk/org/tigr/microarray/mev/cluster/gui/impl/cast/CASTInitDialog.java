/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: CASTInitDialog.java,v $
 * $Revision: 1.1.1.2 $
 * $Date: 2004-02-06 21:48:18 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.cast;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import org.tigr.util.awt.*;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.*;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.*;

public class CASTInitDialog extends AlgorithmDialog{
    
    protected GBA gba;
    protected EventListener eventListener;
    
    protected SampleSelectionPanel sampleSelectionPanel;
    protected HCLSelectionPanel hclOpsPanel;
    
    protected JPanel thresholdPanel;
    protected JLabel thresholdLabel;
    public JTextField thresholdTextField;
    
    protected JPanel mainPanel;
    
    private boolean okPressed = false;
    
    public CASTInitDialog(JFrame parent, boolean modal) {
        super(parent, "CAST: Cluster Affinity Search Technique", modal);
        
        initialize();
    }
    
    
    
    protected void initialize() {
        gba = new GBA();
        eventListener = new EventListener();
        sampleSelectionPanel = new SampleSelectionPanel(Color.white, UIManager.getColor("Label.foreground"),true,"Sample Selection");
        thresholdLabel = new JLabel("Threshold");
        thresholdTextField = new JTextField(10);
        thresholdTextField.setText("0.8");
        thresholdPanel = new JPanel();
        thresholdPanel.setLayout(new GridBagLayout());
        thresholdPanel.setBackground(Color.white);
        thresholdPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Threshold Parameter", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD,12), Color.black));
        gba.add(thresholdPanel, thresholdLabel, 0, 0, 1, 1, 0, 0, GBA.NONE, GBA.C, new Insets(5, 5, 10, 5), 0, 0);
        gba.add(thresholdPanel, thresholdTextField, 1, 0, 1, 1, 0, 0, GBA.H, GBA.C, new Insets(5, 5, 10, 5), 0, 0);
        
        hclOpsPanel = new HCLSelectionPanel();
        
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        mainPanel.setBackground(Color.white);
        gba.add(mainPanel, sampleSelectionPanel, 0, 0, 1, 1, 1, 1, GBA.H, GBA.C, new Insets(0, 0, 0, 0), 0, 0);
        gba.add(mainPanel, thresholdPanel, 0, 1, 1, 1, 1, 1, GBA.H, GBA.C, new Insets(0, 0, 0, 0), 0, 0);
        gba.add(mainPanel, hclOpsPanel, 0, 2, 1, 1, 1, 1, GBA.H, GBA.C, new Insets(0, 0, 0, 0), 0, 0);
        
        this.setResizable(false);
        
        setActionListeners(eventListener);
        addContent(mainPanel);
        setSize(420, 290);
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
    
    public boolean isOkPressed() {return this.okPressed;}
    
    public boolean isClusterGenes(){
        return sampleSelectionPanel.isClusterGenesSelected();
    }
    
    public boolean isHCLSelected(){
        return hclOpsPanel.isHCLSelected();
    }
    
    public void resetControls(){
        sampleSelectionPanel.setClusterGenesSelected(true);
        hclOpsPanel.setHCLSelected(false);
        thresholdTextField.setText("0.8");
    }
    
    protected class EventListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            String command = event.getActionCommand();
            if (command.equals("ok-command")) {
                okPressed = true;
                float val;
                try{
                    val = Float.parseFloat(thresholdTextField.getText());
                } catch (NumberFormatException e){
                    JOptionPane.showMessageDialog(CASTInitDialog.this, "Number format error.", "Number Format Error", JOptionPane.ERROR_MESSAGE);
                    thresholdTextField.requestFocus();
                    thresholdTextField.selectAll();
                    return;
                }
                if(val <= 0 || val > 1){
                    JOptionPane.showMessageDialog(CASTInitDialog.this, "Threshold must be > 0 and <= 1.0", "Input Value Error", JOptionPane.ERROR_MESSAGE);
                    thresholdTextField.requestFocus();
                    thresholdTextField.selectAll();
                    return;
                }
                dispose();
            } else if (command.equals("cancel-command")){
                dispose();
            } else if (command.equals("reset-command")){
                resetControls();
            } else if (command.equals("info-command")){
                HelpWindow helpWindow = new HelpWindow(CASTInitDialog.this, "CAST Initialization Dialog");
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
    }
    
    
    
    public static void main(String [] args){
        CASTInitDialog d = new CASTInitDialog(new JFrame(), true);
        d.show();
        System.exit(0);
    }
    
    protected void disposeDialog() {
        setVisible(false);
        dispose();
    }
    
}