/*
Copyright @ 1999-2004, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * PCASelectionDialog.java
 *
 * Created on February 27, 2003, 11:05 AM
 */

package org.tigr.microarray.mev.cluster.gui.impl.pca;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;

import org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.SampleSelectionPanel;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.CenteringModePanel;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.HelpWindow;

public class PCASelectionDialog extends AlgorithmDialog {
    
    private int result = JOptionPane.CANCEL_OPTION;
    
    private SampleSelectionPanel sampleSelectionPanel;
    private CenteringModePanel centeringModePanel;
    //Raktim - Modifications for 
    JTextField numNeighborsField;
    
    /** Creates new PCASelectionDialog */
    public PCASelectionDialog(Frame frame) {
        super(frame, "PCA: Principal Components Analysis", true);
        //setSize(300, 110);
        setBounds(0, 0, 600, 200);
        //super.setResizable(false);
        setBackground(Color.white);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);   
        
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;       
        
        JPanel pane = new JPanel();
        pane.setBackground(Color.white);
        pane.setBorder(new EtchedBorder());
        pane.setLayout(gridbag);
        
        sampleSelectionPanel = new SampleSelectionPanel(Color.white, UIManager.getColor("Label.foreground"),true,"Sample Selection");
        
        buildConstraints(constraints, 0, 0, 1, 1, 100, 50);
        gridbag.setConstraints(sampleSelectionPanel, constraints);
        pane.add(sampleSelectionPanel);   
        
        
        centeringModePanel = new CenteringModePanel(Color.white, UIManager.getColor("Label.foreground"),true,"Centering Mode");
        buildConstraints(constraints, 0, 1, 1, 1, 0, 50);
        gridbag.setConstraints(centeringModePanel, constraints);
        pane.add(centeringModePanel);  
        
        JPanel numNeibsPanel = new JPanel();
        numNeibsPanel.setBackground(Color.white);
        GridBagLayout grid2 = new GridBagLayout();
        
        JLabel numNeibsLabel= new JLabel("Number for neighbors for KNN imputation :");
        buildConstraints(constraints, 0, 0, 1, 1, 50, 100);
        grid2.setConstraints(numNeibsLabel, constraints);
        numNeibsPanel.add(numNeibsLabel);     
        
        numNeighborsField = new JTextField("10", 7);
        buildConstraints(constraints, 1, 0, 1, 1, 50, 100);
        grid2.setConstraints(numNeighborsField, constraints);
        numNeibsPanel.add(numNeighborsField);    
        
        buildConstraints(constraints, 0, 2, 1, 1, 0, 50);
        gridbag.setConstraints(numNeibsPanel, constraints);
        pane.add(numNeibsPanel);        
        
        setActionListeners(new EventListener());
       //addContent(sampleSelectionPanel);
        addContent(pane);
        pack();
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
    
    public int showModal(){
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);
        show();
        return result;
    }
    
    public boolean isClusterGenesSelected(){
        return sampleSelectionPanel.isClusterGenesSelected();
    }
    
    public boolean isCenteringNoneSelected(){
    	return centeringModePanel.isNoneSelected();
    }
    
    public boolean isCenteringMedianSelected(){
    	return centeringModePanel.isMedianSelected();
    }
    
    public void resetControls(){
        sampleSelectionPanel.setClusterGenesSelected(true);
    }
    
    public int getNumNeighbors() {
        return Integer.parseInt(numNeighborsField.getText());
    }    
    
    protected class EventListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            String command = event.getActionCommand();
            //  Object source = event.getSource();
            if (command.equals("ok-command")) {
               try {
                    int numNeibs = getNumNeighbors();
                    if (numNeibs <= 0) {
                        JOptionPane.showMessageDialog(null, "Invalid number of neighbors", "Error", JOptionPane.ERROR_MESSAGE);
                        return;                        
                    } else {
                        result = JOptionPane.OK_OPTION;
                        dispose();
                    }
               }  catch (NumberFormatException nfe){
                    JOptionPane.showMessageDialog(null, "Invalid number of neighbors", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } else if (command.equals("cancel-command")){
                result = JOptionPane.CANCEL_OPTION;
                dispose();
            } else if (command.equals("reset-command")){
                resetControls();
            } else if (command.equals("info-command")){
                HelpWindow helpWindow = new HelpWindow(PCASelectionDialog.this, "PCA Initialization Dialog");
                if(helpWindow.getWindowContent()){
                    helpWindow.setSize(450, 350);
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
        PCASelectionDialog dialog = new PCASelectionDialog(new Frame());
        int result = dialog.showModal();
        System.out.println("result = "+result);
        System.exit(0);
    }
    
}
