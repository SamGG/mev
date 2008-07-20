/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/

package org.tigr.microarray.mev.cluster.gui.impl.dam;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.SampleSelectionPanel;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.HelpWindow;

public class DAMInitDialog extends AlgorithmDialog {
    
    int result = JOptionPane.CANCEL_OPTION;

    boolean skipGeneScreeningStep = false;
    boolean skipCrossValidationStep = false; 

    private SampleSelectionPanel sampleSelectionPanel;
    
    private JPanel assessPanel;
    private JPanel classPanel;
    private JPanel paramPanel;
    private JPanel processPanel;
    private JPanel assessSelectionPanel;

    private JCheckBox geneScreenBox;

    private JCheckBox validationCheckBox;
    private JRadioButton A0button;
    private JRadioButton A1button;
    private JRadioButton A2button;
   
    private JRadioButton PDAbutton;
    private JRadioButton QDAbutton;

    private JTextField numClassesField;
    private JTextField kValueField;
    private JTextField alphaValueField;
    
    private ButtonGroup assessSelection;
    private ButtonGroup classSelection;
    
    private JLabel alphaValueLabel;
    
    private Listener listener;
    
    /** Creates new form SVMTrain_ClassifierSelectDialog */
    public DAMInitDialog(java.awt.Frame parent, boolean modal) {
        super(parent, "DAM Initialization" ,modal);
        listener = new Listener();
                
        initComponents();
        okButton.setText("Next");
        sampleSelectionPanel.setButtonText("Classify Genes", "Classify Experiments");
        sampleSelectionPanel.setClusterGenesSelected(true);
        //sampleSelectionPanel.setClusterSamplesSelected(false);
        setActionListeners(listener);
       pack();
       setResizable(false);
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new java.awt.GridBagLayout());
        mainPanel.setBackground(Color.white);
        
        assessPanel = new JPanel(); 
        paramPanel = new JPanel();
        assessSelectionPanel = new JPanel();        

        A0button = new JRadioButton();
        A1button = new JRadioButton();
        A2button = new JRadioButton();    
// NEW 06-16-2004

        GridBagConstraints gbc1;
        
        setBackground(Color.lightGray);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
                closeDialog(evt);
            }
        });
        
        sampleSelectionPanel = new SampleSelectionPanel(Color.white, UIManager.getColor("Label.foreground"),true,"Classification Selection");
        
        gbc1 = new java.awt.GridBagConstraints();
        gbc1.gridx = 0;
        gbc1.gridy = 0;
        gbc1.weightx = 1;
        gbc1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gbc1.insets = new java.awt.Insets(4, 0, 0, 0);
        mainPanel.add(sampleSelectionPanel, gbc1);
        
/*****/
        processPanel = new JPanel(new GridBagLayout());
        processPanel.setBorder(new javax.swing.border.TitledBorder(new EtchedBorder(EtchedBorder.RAISED), "Data Screening", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), Color.black));
        processPanel.setBackground(Color.white);
        java.awt.GridBagConstraints gbc5;

        

        geneScreenBox = new JCheckBox("Enable Data Screening Step (ANOVA)", true);
        geneScreenBox.setHorizontalAlignment(JCheckBox.CENTER);
        geneScreenBox.setFocusPainted(false);
        geneScreenBox.setBackground(Color.white);
        geneScreenBox.setForeground(UIManager.getColor("Label.foreground"));
        geneScreenBox.setActionCommand("screen-data-command");
        geneScreenBox.addActionListener(listener);
        gbc5 = new java.awt.GridBagConstraints();
	gbc5.gridx = 0;
	gbc5.gridy = 0;
	gbc5.insets = new java.awt.Insets(0, 0, 10, 0);
        gbc5.fill = GridBagConstraints.BOTH;   
	gbc5.gridwidth = 2;
        gbc5.weightx = 1.0;             		
    	processPanel.add(geneScreenBox, gbc5);
        
        alphaValueLabel = new JLabel("Alpha Value");
        alphaValueLabel.setHorizontalAlignment(JLabel.RIGHT);        
        gbc5 = new java.awt.GridBagConstraints();
        gbc5.gridx = 0;
        gbc5.gridy = 1;
        gbc5.fill = GridBagConstraints.BOTH;   
        gbc5.insets = new java.awt.Insets(0, 0, 10, 0);
        gbc5.weightx = 1.0;             
        processPanel.add(alphaValueLabel, gbc5);
        
        alphaValueField = new JTextField("0.05", 8);
        alphaValueField.setHorizontalAlignment(JLabel.LEFT);
        gbc5 = new java.awt.GridBagConstraints();
        gbc5.gridx = 1;
        gbc5.gridy = 1;   
        gbc5.insets = new java.awt.Insets(0, 0, 10, 40);
        gbc5.weightx = 1.0;             
        processPanel.add(alphaValueField, gbc5);

        
        gbc1 = new java.awt.GridBagConstraints();
        gbc1.gridx = 0;
        gbc1.gridy = 1;
        gbc1.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(processPanel, gbc1);

/****/
   
        classSelection = new ButtonGroup();        
        classPanel = new JPanel();
        PDAbutton = new JRadioButton();
        QDAbutton = new JRadioButton();
        classPanel.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gbc3;
        
        classPanel.setBorder(new javax.swing.border.TitledBorder(new EtchedBorder(EtchedBorder.RAISED), "Classification Algorithm Selection", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), Color.black));
        classPanel.setBackground(Color.white);
        PDAbutton.setToolTipText("Polychotomous Discrimination Algorithm");
        PDAbutton.setSelected(true);
        PDAbutton.setText("PDA");        
        PDAbutton.setBackground(Color.white);
        PDAbutton.setHorizontalAlignment(JRadioButton.CENTER);
        classSelection.add(PDAbutton);
        PDAbutton.setForeground(UIManager.getColor("Label.foreground"));
        PDAbutton.setFocusPainted(false);

        gbc3 = new java.awt.GridBagConstraints();
        gbc3.gridx = 0;
        gbc3.gridy = 0;
        gbc3.fill = GridBagConstraints.BOTH;   
        gbc3.insets = new java.awt.Insets(10, 0, 10, 0);
        gbc3.weightx = 1.0;             
        classPanel.add(PDAbutton, gbc3);
        
        QDAbutton.setToolTipText("Quadratic Discriminant Analysis Algorithm");
        QDAbutton.setText("QDA");
        QDAbutton.setBackground(Color.white);
        QDAbutton.setForeground(UIManager.getColor("Label.foreground"));
        QDAbutton.setHorizontalAlignment(JRadioButton.CENTER);
        classSelection.add(QDAbutton);
        QDAbutton.setFocusPainted(false);
        gbc3 = new java.awt.GridBagConstraints();
        gbc3.gridx = 1;
        gbc3.gridy = 0;
        gbc3.fill = GridBagConstraints.BOTH;   
        gbc3.insets = new java.awt.Insets(10, 0, 10, 0);
        gbc3.weightx = 1.0;             
        classPanel.add(QDAbutton, gbc3);

        gbc1 = new java.awt.GridBagConstraints();
        gbc1.gridx = 0;
        gbc1.gridy = 2;
        gbc1.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(classPanel, gbc1);
      
/******/

        paramPanel.setLayout(new java.awt.GridBagLayout());
        paramPanel.setBorder(new javax.swing.border.TitledBorder(new EtchedBorder(EtchedBorder.RAISED), "DAM Classification Parameters", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), Color.black));
        paramPanel.setBackground(Color.white);
        java.awt.GridBagConstraints gbc4;

        JLabel numClassesLabel = new JLabel("Number of Classes");
        numClassesLabel.setHorizontalAlignment(JLabel.RIGHT);
        paramPanel.add(numClassesLabel);
        gbc4 = new java.awt.GridBagConstraints();
        gbc4.gridx = 0;
        gbc4.gridy = 0;
        gbc4.fill = GridBagConstraints.BOTH;   
        gbc4.insets = new java.awt.Insets(0, 0, 0, 0);
        gbc4.weightx = 1.0;             
        paramPanel.add(numClassesLabel, gbc4);
        
        numClassesField = new JTextField("3", 8);
        paramPanel.add(numClassesField);      
        gbc4 = new java.awt.GridBagConstraints();
        gbc4.gridx = 1;
        gbc4.gridy = 0;
        gbc4.insets = new java.awt.Insets(0, 0, 0, 40);
        gbc4.weightx = 1.0;             
        paramPanel.add(numClassesField, gbc4);

        JLabel kValueLabel = new JLabel("Number Of Components");
        kValueLabel.setHorizontalAlignment(JLabel.RIGHT);
        gbc4 = new java.awt.GridBagConstraints();
        gbc4.gridx = 0;
        gbc4.gridy = 1;
        gbc4.fill = GridBagConstraints.BOTH;   
        gbc4.insets = new java.awt.Insets(10, 0, 10, 0);
        gbc4.weightx = 1.0;             
        paramPanel.add(kValueLabel, gbc4);
        
        kValueField = new JTextField("3", 8);   
        kValueField.setHorizontalAlignment(JTextField.LEFT);
        gbc4 = new java.awt.GridBagConstraints();
        gbc4.gridx = 1;
        gbc4.gridy = 1;
        gbc4.insets = new java.awt.Insets(10, 0, 10, 40);
        gbc4.weightx = 1.0;             
        paramPanel.add(kValueField, gbc4);

        gbc1 = new java.awt.GridBagConstraints();
        gbc1.gridx = 0;
        gbc1.gridy = 3;
        gbc1.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(paramPanel, gbc1);

/*****/       
        assessSelection = new ButtonGroup();        

        assessSelectionPanel.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gbc2;
        
        assessSelectionPanel.setBackground(Color.white);

// NEW 06-16-2004

   /*     A3button.setToolTipText("Initial Classification");
        A3button.setSelected(true);
        A3button.setText("Initial Classification");
        A3button.setBackground(Color.white);
        assessSelection.add(A3button);
        A3button.setForeground(UIManager.getColor("Label.foreground"));
        A3button.setFocusPainted(false);
        gbc2 = new java.awt.GridBagConstraints();
        gbc2.gridx = 0;
        gbc2.gridy = 1;
        gbc2.fill = GridBagConstraints.BOTH;   
        gbc2.insets = new java.awt.Insets(0, 40, 0, 0);
	gbc2.gridwidth = 1;
	gbc2.gridheight = 1;
        gbc2.weightx = 1.0;     
    */    
        
        validationCheckBox = new JCheckBox("Enable Validation", false);
        validationCheckBox.setOpaque(true);
        validationCheckBox.setBackground(Color.white);
        validationCheckBox.setFocusPainted(false);
        validationCheckBox.setHorizontalAlignment(JCheckBox.CENTER);
        validationCheckBox.setActionCommand("validation-command");
        validationCheckBox.addActionListener(listener);
        assessSelectionPanel.add(validationCheckBox, new GridBagConstraints(0,0,3,1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,0,5,0), 0, 0));
        
        
        
      //  assessSelectionPanel.add(A3button, gbc2);

        A0button.setToolTipText("A0 Assessment Algorithm");
        A0button.setSelected(true);
        A0button.setEnabled(false);
        A0button.setText("A0");
        A0button.setBackground(Color.white);
        assessSelection.add(A0button);
        A0button.setForeground(UIManager.getColor("Label.foreground"));
        A0button.setFocusPainted(false);
        gbc2 = new java.awt.GridBagConstraints();
        gbc2.gridx = 0;
        gbc2.gridy = 1;
        gbc2.fill = GridBagConstraints.BOTH;   
        gbc2.insets = new java.awt.Insets(0, 40, 10, 0);
	gbc2.gridwidth = 1;
	gbc2.gridheight = 1;
        gbc2.weightx = 1.0;     
        
        assessSelectionPanel.add(A0button, gbc2);
        
        A1button.setToolTipText("A1 Assessment Algorithm");
        A1button.setText("A1");
        A1button.setEnabled(false);
        A1button.setBackground(Color.white);
        assessSelection.add(A1button);
        A1button.setForeground(UIManager.getColor("Label.foreground"));
        A1button.setFocusPainted(false);
        gbc2 = new java.awt.GridBagConstraints();
        gbc2.gridx = 1;
        gbc2.gridy = 1;
        gbc2.fill = GridBagConstraints.BOTH;   
        gbc2.insets = new java.awt.Insets(0, 30, 10, 0);
	gbc2.gridwidth = 1;
	gbc2.gridheight = 1;
        gbc2.weightx = 1.0;     
        
        assessSelectionPanel.add(A1button, gbc2);
        
        A2button.setToolTipText("A2 Assessment Algorithm");
        A2button.setText("A2");
        A2button.setEnabled(false);
        A2button.setBackground(Color.white);
        assessSelection.add(A2button);
        A2button.setForeground(UIManager.getColor("Label.foreground"));
        A2button.setFocusPainted(false);
        gbc2 = new GridBagConstraints();
        gbc2.gridx = 2;
        gbc2.gridy = 1;
        gbc2.fill = GridBagConstraints.BOTH;   
        gbc2.insets = new java.awt.Insets(0, 30, 10, 0);
	gbc2.gridwidth = 1;
	gbc2.gridheight = 1;
        gbc2.weightx = 1.0;             

        assessSelectionPanel.add(A2button, gbc2);


        assessPanel.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gbc6;

        assessPanel.setBorder(new javax.swing.border.TitledBorder(new EtchedBorder(EtchedBorder.RAISED), "Validation Selection", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), Color.black));
        assessPanel.setBackground(Color.white);

        assessSelectionPanel.setBackground(Color.white);
        assessSelectionPanel.setForeground(UIManager.getColor("Label.foreground"));
        gbc6 = new java.awt.GridBagConstraints();
        gbc6.gridx = 0;
        gbc6.gridy = 0;
        gbc6.fill = GridBagConstraints.BOTH;   
        gbc6.weightx = 1.0;     
        
        assessPanel.add(assessSelectionPanel, gbc6);

/*
        skipCrossValBox = new JCheckBox("Skip Cross Validation step (LOOCV)");
        skipCrossValBox.setFocusPainted(false);
        skipCrossValBox.setBackground(Color.white);
        skipCrossValBox.setForeground(UIManager.getColor("Label.foreground"));
        skipCrossValBox.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);

	gbc6 = new GridBagConstraints();
	gbc6.gridx = 0;
	gbc6.gridy = 1;
        gbc6.fill = GridBagConstraints.BOTH;   
	gbc6.insets = new java.awt.Insets(0, 40, 0, 0);
        gbc6.weightx = 1.0;             
	assessPanel.add(skipCrossValBox, gbc6);
*/

        gbc1 = new java.awt.GridBagConstraints();
        gbc1.gridx = 0;
        gbc1.gridy = 4;
        gbc1.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(assessPanel, gbc1);

        addContent(mainPanel);
    }
    
    /** Closes the dialog */
    private void closeDialog(WindowEvent evt) {
        setVisible(false);
        dispose();
    }
    
    /**
     * Returns true if gene DAM's are to be evaluated
     */
    public boolean isEvaluateGenesSelected(){
        return sampleSelectionPanel.isClusterGenesSelected();
    }
    
    /**
     *  Returns a constant indicating which SVM process to run
     *  Train and classify, train only, classify only
     */
    public int getAssessmentSelection(){
        //if validation is not selected return A3 indicating only initial classification
        if(!this.validationCheckBox.isSelected())
            return DAMGUI.A3;
        
        if(this.A2button.isSelected())
            return DAMGUI.A2;
        else if(this.A1button.isSelected())
            return DAMGUI.A1;
        else  if (this.A0button.isSelected())
            return DAMGUI.A0;
        else 
            return DAMGUI.A3;
    }


    /**
     */
    public boolean isPDASelected(){
        if (this.PDAbutton.isSelected()) 
             return true;
        else 
             return false;
    }
    
    public int getNumClasses() {
        String s = numClassesField.getText();
        return Integer.parseInt(s);
    }
   
    public int getKValue() {
        String s = kValueField.getText();
        return Integer.parseInt(s);
    }
   
    public double getAlphaValue() {
        String s = alphaValueField.getText();
        return Double.parseDouble(s);
    }
   

    /**
     * Returns boolean selection for calculating HCL on SVM results
     */
     /*
    public boolean getHCLSelection(){
        return hclOpsPanel.isHCLSelected();
    }
    */
    
    public int showModal() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);
        show();
        return result;
    }
    
    
    
     public void resetControls(){
        geneScreenBox.setSelected(true);
        this.alphaValueLabel.setEnabled(true);
        this.alphaValueField.setEnabled(true);

        sampleSelectionPanel.setClusterGenesSelected(true);
        validationCheckBox.setSelected(false);
        A0button.setEnabled(false);
        A1button.setEnabled(false);
        A2button.setEnabled(false);
        
        PDAbutton.setSelected(true);
        numClassesField.setText("3");
        kValueField.setText("3");
        alphaValueField.setText("0.05");
        skipGeneScreeningStep = false;               	
        skipCrossValidationStep = false;               	                
        geneScreenBox.setSelected(true);	
        	
    }
   
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        new DAMInitDialog(new javax.swing.JFrame(), true).show();
        System.exit(0);
    }
    
      
    public boolean getSkipGeneSelectionValue() {
        return !geneScreenBox.isSelected();
    }
    
/*
    public boolean getSkipLOOCVValue() {
        return skipCrossValBox.isSelected();
    }
*/
    
    private class Listener extends DialogListener {
        
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            if (command.equals("ok-command")) {
                    result = JOptionPane.OK_OPTION;
                dispose();
            } else if (command.equals("cancel-command")) {
                result = JOptionPane.CANCEL_OPTION;
                dispose();
            } else if (command.equals("reset-command")){
                resetControls(); 
            } else if (command.equals("screen-data-command")) {
                alphaValueField.setEnabled(geneScreenBox.isSelected());
                alphaValueLabel.setEnabled(geneScreenBox.isSelected());
            } else if (command.equals("validation-command")) {
                A0button.setEnabled(validationCheckBox.isSelected());
                A1button.setEnabled(validationCheckBox.isSelected());
                A2button.setEnabled(validationCheckBox.isSelected());
            } else if (command.equals("info-command")){
               HelpWindow hw = new HelpWindow(DAMInitDialog.this, "DAM Initialization Dialog");
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
        
        public void windowClosing(WindowEvent e) {
            result = JOptionPane.CLOSED_OPTION;
            dispose();
        }
        
    }
    
}

