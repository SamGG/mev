/*
Copyright @ 1999-2005, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * DAMProcessInitDialog.java
 *
 */

package org.tigr.microarray.mev.cluster.gui.impl.dam;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.SampleSelectionPanel;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.HelpWindow;

public class DAMProcessInitDialog extends AlgorithmDialog {
    
    int result = JOptionPane.CANCEL_OPTION;
	boolean skipGeneScreeningStep = false;
	boolean skipCrossValidationStep = false;        
    private SampleSelectionPanel sampleSelectionPanel;
    
    private JPanel processPanel;
    private JPanel KPanel;
    private JRadioButton defKbutton;
    private JRadioButton calcKbutton;
    private JCheckBox skipGeneScreenBox;
    private JCheckBox skipCrossValBox;
    
    private ButtonGroup Kselection;
    
    
    /** Creates new form SVMTrain_ClassifierSelectDialog */
    public DAMProcessInitDialog(java.awt.Frame parent, boolean modal) {
        super(parent, "DAM Process Initialization" ,modal);

        okButton.setText("Next");

        Listener listener = new Listener();
        initComponents(listener);        
        setActionListeners(listener);
         
       pack();
       setResizable(false);
    }

    private void initComponents(Listener listener) {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(Color.white);
         
        setBackground(Color.lightGray);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
                closeDialog(evt);
            }
        });
        
       
        processPanel = new JPanel(new GridBagLayout());
        processPanel.setBorder(new javax.swing.border.TitledBorder(new EtchedBorder(EtchedBorder.RAISED), "DAM Process Selection", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), Color.black));
        processPanel.setBackground(Color.white);
        
        skipGeneScreenBox = new JCheckBox("Skip Gene Screening step (ANOVA)");
        skipGeneScreenBox.setFocusPainted(false);
        skipGeneScreenBox.setBackground(Color.white);
        skipGeneScreenBox.setForeground(UIManager.getColor("Label.foreground"));
        skipGeneScreenBox.addItemListener(listener);
  
        GridBagConstraints gbc1 = new GridBagConstraints();
	gbc1.gridx = 0;
	gbc1.gridy = 0;
	gbc1.weightx = 1;
	gbc1.fill = GridBagConstraints.HORIZONTAL;
	gbc1.insets = new java.awt.Insets(0, 60, 0, 0);
        gbc1.fill = GridBagConstraints.BOTH;   
        gbc1.weightx = 1.0;             		
    	processPanel.add(skipGeneScreenBox, gbc1);
          
        skipCrossValBox = new JCheckBox("Skip Cross Validation step (LOOCV)");
        skipCrossValBox.setFocusPainted(false);
        skipCrossValBox.setBackground(Color.white);
        skipCrossValBox.setForeground(UIManager.getColor("Label.foreground"));
        skipCrossValBox.addItemListener(listener);

	gbc1 = new GridBagConstraints();
	gbc1.gridx = 0;
	gbc1.gridy = 1;
	gbc1.weightx = 1;
        gbc1.fill = GridBagConstraints.BOTH;   
	gbc1.insets = new java.awt.Insets(0, 60, 0, 0);
        gbc1.weightx = 1.0;             
	processPanel.add(skipCrossValBox, gbc1);
				
        KPanel = new JPanel(new GridBagLayout()); 
        KPanel.setBorder(new javax.swing.border.TitledBorder(new EtchedBorder(EtchedBorder.RAISED), "Determining K value", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), Color.black));
        KPanel.setBackground(Color.white);

        Kselection = new ButtonGroup();                
        defKbutton = new JRadioButton();
        calcKbutton = new JRadioButton();

        defKbutton.setToolTipText("Use default K = 3");
        defKbutton.setSelected(true);
        defKbutton.setText("Default");
        defKbutton.setBackground(Color.white);
        defKbutton.setForeground(UIManager.getColor("Label.foreground"));
        Kselection.add(defKbutton);
        defKbutton.setFocusPainted(false);
        GridBagConstraints gbc2 = new java.awt.GridBagConstraints();
        gbc2.gridx = 0;
        gbc2.gridy = 0;
        gbc2.fill = GridBagConstraints.BOTH;   
	gbc2.insets = new java.awt.Insets(0, 40, 0, 0);
        gbc2.weightx = 1.0;                     
        KPanel.add(defKbutton, gbc2);

        calcKbutton.setToolTipText("Calculate optimum K from loaded data");
        calcKbutton.setText("Calculate");
        calcKbutton.setBackground(Color.white);
        calcKbutton.setForeground(UIManager.getColor("Label.foreground"));
        Kselection.add(calcKbutton);
        calcKbutton.setFocusPainted(false);
        gbc2 = new java.awt.GridBagConstraints();
        gbc2.gridx = 1;
        gbc2.gridy = 0;
        gbc2.fill = GridBagConstraints.BOTH;   
	gbc2.insets = new java.awt.Insets(0, 40, 0, 0);
        gbc2.weightx = 1.0;                     
        KPanel.add(calcKbutton, gbc2);
  
        GridBagConstraints gbc3 = new GridBagConstraints();
	gbc3.gridx = 0;
	gbc3.gridy = 0;
	gbc3.weightx = 1;
	gbc3.fill = GridBagConstraints.HORIZONTAL;
	gbc3.insets = new java.awt.Insets(4, 0, 0, 0);

	mainPanel.add(processPanel, gbc3);
		  
        gbc3 = new GridBagConstraints();
	gbc3.gridx = 0;
	gbc3.gridy = 1;
	gbc3.weightx = 1;
	gbc3.fill = GridBagConstraints.HORIZONTAL;
	gbc3.insets = new java.awt.Insets(4, 0, 0, 0);

	mainPanel.add(KPanel, gbc3);

        addContent(mainPanel);
    }
    
    /** Closes the dialog */
    private void closeDialog(WindowEvent evt) {
        setVisible(false);
        dispose();
    }
    
    
    public int showModal() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);
        show();
        return result;
    }
    
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        new DAMProcessInitDialog(new javax.swing.JFrame(), true).show();
        System.exit(0);
    }
     
    
    public boolean getSkipGeneSelectionValue() {
        return skipGeneScreenBox.isSelected();
    }
    
    public boolean getSkipLOOCVValue() {
        return skipCrossValBox.isSelected();
    }
    
    private class Listener extends DialogListener implements ItemListener {
        
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            if (command.equals("ok-command")) {
		result = JOptionPane.OK_OPTION;
                dispose();
            } else if (command.equals("cancel-command")) {
                result = JOptionPane.CANCEL_OPTION;
                dispose();
            } else if (command.equals("reset-command")){
               	skipGeneScreeningStep = false;               	
               	skipCrossValidationStep = false;               	                
                skipGeneScreenBox.setSelected(false);	
                skipCrossValBox.setSelected(false);	               
        		defKbutton.setSelected(true);                
            } else if (command.equals("info-command")){
               HelpWindow hw = new HelpWindow(DAMProcessInitDialog.this, "DAM Process Initialization Dialog");
                if(hw.getWindowContent()){
                    hw.setSize(450,650);
                    hw.setLocation();
                    hw.show();
                }
                else {
                    hw.setVisible(false);
                    hw.dispose();
                }   
            }
            
        }
        
        public void itemStateChanged(ItemEvent e) {

            Object source = e.getItemSelectable();

            if (source == skipGeneScreenBox) {
                if (skipGeneScreenBox.isSelected()) {                	
                	skipGeneScreeningStep = true;
                } else {
                 	skipGeneScreeningStep = false;               	
                }
            } else if (source == skipCrossValBox) {
                if (skipCrossValBox.isSelected()) {
 			skipCrossValidationStep = true;               	
                } else {
			skipCrossValidationStep = false;                	
                }
            }
        }
       
        
        public void windowClosing(WindowEvent e) {
            result = JOptionPane.CLOSED_OPTION;
            dispose();
        }
        
    }
    
}

