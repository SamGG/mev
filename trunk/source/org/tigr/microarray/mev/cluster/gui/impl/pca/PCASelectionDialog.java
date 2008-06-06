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
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;
import javax.swing.JButton;

import org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.SampleSelectionPanel;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.CenteringModePanel;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.HelpWindow;
import org.tigr.microarray.mev.cluster.algorithm.impl.ExperimentUtil;

public class PCASelectionDialog extends AlgorithmDialog {
    
    private int result = JOptionPane.CANCEL_OPTION;
    
    private SampleSelectionPanel sampleSelectionPanel;
    private CenteringModePanel centeringModePanel;
    private JPanel CovarianceMatrixType;
    private JPanel shortcutSelectionPanel;
    private JCheckBox shortcutCheckBox;
    private javax.swing.JRadioButton nnMatrix;
    private javax.swing.JRadioButton mmMatrix;
    private javax.swing.ButtonGroup matrixButtonGroup;
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
        sampleSelectionPanel.setGeneButtonItemListener(new EventListener());
        sampleSelectionPanel.setSampleButtonItemListener(new EventListener());
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
        CovarianceMatrixType = new JPanel();
        
        CovarianceMatrixType.setBorder(new javax.swing.border.TitledBorder(new javax.swing.border.EtchedBorder(), "Algorithm Optimization", 
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, 
                new java.awt.Font("Dialog", 1, 12), Color.black)); 
        nnMatrix = new javax.swing.JRadioButton();
        mmMatrix = new javax.swing.JRadioButton();
        matrixButtonGroup = new javax.swing.ButtonGroup();
        nnMatrix.setText("Complete Algorithm");
        nnMatrix.setSelected(false);
        nnMatrix.setEnabled(false);
        nnMatrix.setBackground(Color.white);
        mmMatrix.setText("Optimized for speed (recommended)");
        mmMatrix.setSelected(true);
        mmMatrix.setEnabled(false);
        mmMatrix.setBackground(Color.white);

        matrixButtonGroup.add(mmMatrix);
        matrixButtonGroup.add(nnMatrix);
        CovarianceMatrixType.setLayout(new GridBagLayout());
        
        CovarianceMatrixType.add(mmMatrix, new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,0,5,0), 0,0));
        CovarianceMatrixType.add(nnMatrix, new GridBagConstraints(0,1,1,1,0,0,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,0,5,0), 0,0));
        //CovarianceMatrixType.add(new JLabel("    -    "),new GridBagConstraints(2,0,1,1,0,0,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0,0,5,0), 0,0));
        CovarianceMatrixType.add(new JLabel("Most analyses by samples can be sufficiently approximated by running the faster algorithm."), new GridBagConstraints(0,2,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,5,0), 0,0));
        CovarianceMatrixType.add(new JLabel("The complete algorithm will dramatically increase calculation time for larger data sets."), new GridBagConstraints(0,3,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,5,0), 0,0));
        CovarianceMatrixType.add(new JLabel("Click INFO for more information."), new GridBagConstraints(0,4,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,5,0), 0,0));
        CovarianceMatrixType.setEnabled(false);
        CovarianceMatrixType.setBackground(Color.white);
        pane.add(CovarianceMatrixType);

        buildConstraints(constraints, 0, 3, 1, 1, 0, 50);
        gridbag.setConstraints(CovarianceMatrixType, constraints);
        
        shortcutSelectionPanel = new JPanel();
        shortcutCheckBox = new JCheckBox("Utilize shortcut");
        shortcutCheckBox.setSelected(true);
        shortcutCheckBox.setFocusPainted(false);
        shortcutCheckBox.setBackground(Color.white);
        shortcutCheckBox.setForeground(UIManager.getColor("Label.foreground"));
        shortcutCheckBox.addItemListener(new EventListener());
        shortcutSelectionPanel.add(shortcutCheckBox);
        
        shortcutSelectionPanel.setBackground(Color.white);
        buildConstraints(constraints, 0, 3, 1, 1, 0, 50);
        gridbag.setConstraints(shortcutSelectionPanel, constraints);
        
        //pane.add(shortcutSelectionPanel);
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
    public boolean isShortcutUsed(){
    	return mmMatrix.isSelected();
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
    
    protected class EventListener implements ActionListener, ItemListener {
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
        public void itemStateChanged(ItemEvent e) {
        	if (e.getSource()==sampleSelectionPanel.getSampleButton()){
        		
        			nnMatrix.setEnabled(true);
        	        mmMatrix.setEnabled(true);
        	}
        	if (e.getSource()==sampleSelectionPanel.getGeneButton()){
        			nnMatrix.setEnabled(false);
        	        mmMatrix.setEnabled(false);
        	        mmMatrix.setSelected(true);
        		
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
