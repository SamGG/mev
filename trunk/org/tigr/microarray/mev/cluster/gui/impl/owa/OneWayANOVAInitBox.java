/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: OneWayANOVAInitBox.java,v $
 * $Revision: 1.2 $
 * $Date: 2004-02-13 18:16:45 $
 * $Author: braisted $
 * $State: Exp $
 */

package org.tigr.microarray.mev.cluster.gui.impl.owa;

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

import org.tigr.microarray.mev.cluster.gui.impl.dialogs.*;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.*;

/**
 *
 * @author  nbhagaba
 * @version 
 */
public class OneWayANOVAInitBox extends AlgorithmDialog {

    public static final int JUST_ALPHA = 1;
    public static final int STD_BONFERRONI = 2;
    public static final int ADJ_BONFERRONI = 3;
     
    boolean okPressed = false;
    Vector exptNames;    
    MultiClassPanel mPanel; 
    PValuePanel pPanel;
    HCLSelectionPanel hclOpsPanel;    
    
    /** Creates new OneWayANOVAInitBox */
    public OneWayANOVAInitBox(JFrame parentFrame, boolean modality, Vector exptNames) {
        super(parentFrame, "One-way ANOVA Initialization", modality);
        this.exptNames = exptNames;  
        setBounds(0, 0, 700, 720);
        setBackground(Color.white);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        
        JPanel pane = new JPanel();
        pane.setLayout(gridbag);
        
        mPanel = new MultiClassPanel();

        buildConstraints(constraints, 0, 0, 1, 1, 100, 80);
        gridbag.setConstraints(mPanel, constraints);
        pane.add(mPanel);     

        pPanel = new PValuePanel();
        buildConstraints(constraints, 0, 1, 1, 1, 0, 10);
        gridbag.setConstraints(pPanel, constraints);
        pane.add(pPanel);
        
        hclOpsPanel = new HCLSelectionPanel();
        buildConstraints(constraints, 0, 2, 1, 1, 0, 10);
        gridbag.setConstraints(hclOpsPanel, constraints);
        pane.add(hclOpsPanel);        
        
        addContent(pane);
        EventListener listener = new EventListener();
        setActionListeners(listener);
        this.addWindowListener(listener);        
    }

    
    public void setVisible(boolean visible) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);
        
        super.setVisible(visible);
        
        if (visible) {
            //bPanel.okButton.requestFocus(); //UNCOMMMENT THIS LATER
        }
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
    
    
    public boolean isOkPressed() {
        return okPressed;
    }    
    
    public boolean drawTrees() {
        return this.hclOpsPanel.isHCLSelected();
    }    
    
    class MultiClassPanel extends JPanel {
        NumGroupsPanel ngPanel;
        GridBagConstraints constraints;
        GridBagLayout gridbag;
        JPanel dummyPanel;
        MultiGroupExperimentsPanel mulgPanel;
        int numGroups;
        //Vector exptNames;
        
        public MultiClassPanel(/*Vector exptNames*/) {
            constraints = new GridBagConstraints();
            gridbag = new GridBagLayout();
            this.setBackground(Color.white);
            //this.exptNames = exptNames;
            this.setLayout(gridbag);
            ngPanel = new NumGroupsPanel();
            
            buildConstraints(constraints, 0, 0, 1, 1, 100, 10);
            constraints.fill = GridBagConstraints.BOTH;
            gridbag.setConstraints(ngPanel, constraints);
            ngPanel.okButton.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent evt) {
                    ngPanel.okPressed = true;
                    try {
                        numGroups = Integer.parseInt(ngPanel.numGroupsField.getText());
                        if (numGroups <= 2) {
                            JOptionPane.showMessageDialog(null, "Please enter a positive integer > 2!", "Error", JOptionPane.ERROR_MESSAGE);
                        } else {
                            mulgPanel = new MultiGroupExperimentsPanel(exptNames, numGroups);
                            //System.out.println("OK Pressed");
                            //JButton dummyButton  = new JButton("dummyButton");
                            buildConstraints(constraints, 0, 1, 1, 1, 0, 90);
                            constraints.fill = GridBagConstraints.BOTH;
                            gridbag.setConstraints(mulgPanel, constraints);
                            //dummyButton.setVisible(true);
                            MultiClassPanel.this.remove(dummyPanel);
                            MultiClassPanel.this.add(mulgPanel);
                            //MultiClassPanel.this.add(dummyButton);
                            MultiClassPanel.this.validate();
                            ngPanel.okButton.setEnabled(false);
                            ngPanel.numGroupsField.setEnabled(false);
                        }
                        //MultiClassPanel.this.repaint();
                        //dispose();
                    } catch (NumberFormatException nfe) {
                        JOptionPane.showMessageDialog(null, "Please enter a positive integer > 2!", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
            this.add(ngPanel);
            
            
            buildConstraints(constraints, 0, 1, 1, 1, 0, 90);
            dummyPanel = new JPanel();
            dummyPanel.setBackground(Color.white);
            gridbag.setConstraints(dummyPanel, constraints);
            this.add(dummyPanel);
        }
        
        
        class NumGroupsPanel extends JPanel {
            JTextField numGroupsField;
            JButton okButton;
            boolean okPressed = false;
            
            public NumGroupsPanel() {
                setBackground(Color.white);
                GridBagLayout gridbag = new GridBagLayout();
                GridBagConstraints constraints = new GridBagConstraints();
                
                //JPanel pane = new JPanel();
                this.setLayout(gridbag);
                
                JLabel numGroupsLabel = new JLabel("Number of groups ");
                buildConstraints(constraints, 0, 0, 1, 1, 30, 100);
                constraints.anchor = GridBagConstraints.EAST;
                gridbag.setConstraints(numGroupsLabel, constraints);
                this.add(numGroupsLabel);
                
                numGroupsField = new JTextField("", 7);
                constraints.anchor = GridBagConstraints.WEST;
                buildConstraints(constraints, 1, 0, 1, 1, 30, 0);
                gridbag.setConstraints(numGroupsField, constraints);
                this.add(numGroupsField);
                
                okButton = new JButton("OK");
                buildConstraints(constraints, 2, 0, 1, 1, 40, 0);
                gridbag.setConstraints(okButton, constraints);
 
                this.add(okButton);
                
            }
            
            
            
            public void setVisible(boolean visible) {
                //Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                setLocation((MultiClassPanel.this.getWidth() - getSize().width)/2, (MultiClassPanel.this.getHeight() - getSize().height)/2);
                
                super.setVisible(visible);
                
                if (visible) {
                    //bPanel.okButton.requestFocus(); //UNCOMMMENT THIS LATER
                }
            }
            
            public boolean isOkPressed() {
                return okPressed;
            }
            
        }
        
        class MultiGroupExperimentsPanel extends JPanel {
            JLabel[] expLabels;
            JRadioButton[][] exptGroupRadioButtons;
            JRadioButton[] notInGroupRadioButtons;
            MultiGroupExperimentsPanel(Vector exptNames, int numGroups) {
                this.setBorder(new TitledBorder(new EtchedBorder(), "Group Assignments", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), Color.black));
                setBackground(Color.white);
                JPanel panel1 = new JPanel();
                expLabels = new JLabel[exptNames.size()];
                exptGroupRadioButtons = new JRadioButton[numGroups][exptNames.size()];
                //groupARadioButtons = new JRadioButton[exptNames.size()];
                //groupBRadioButtons = new JRadioButton[exptNames.size()];
                notInGroupRadioButtons = new JRadioButton[exptNames.size()];
                ButtonGroup chooseGroup[] = new ButtonGroup[exptNames.size()];
                
                GridBagLayout gridbag = new GridBagLayout();
                GridBagLayout gridbag2 = new GridBagLayout();
                GridBagConstraints constraints = new GridBagConstraints();
                this.setLayout(gridbag2);
                panel1.setLayout(gridbag);
                
                for (int i = 0; i < exptNames.size(); i++) {
                    String s1 = (String)(exptNames.get(i));
                    expLabels[i] = new JLabel(s1);
                    chooseGroup[i] = new ButtonGroup();
                    for (int j = 0; j < numGroups; j++) {
                        exptGroupRadioButtons[j][i] = new JRadioButton("Group " + (j + 1) + "     ", j == 0? true: false);
                        chooseGroup[i].add(exptGroupRadioButtons[j][i]);
                    }

                    notInGroupRadioButtons[i] = new JRadioButton("Not in groups", false);
                    chooseGroup[i].add(notInGroupRadioButtons[i]);
                    
                
                    for (int j = 0; j < numGroups; j++) {
                        buildConstraints(constraints, j, i, 1, 1, 100, 100);
                        //constraints.fill = GridBagConstraints.BOTH;
                        gridbag.setConstraints(exptGroupRadioButtons[j][i], constraints);
                        panel1.add(exptGroupRadioButtons[j][i]);
                    }
                    
                    buildConstraints(constraints, (numGroups + 1), i, 1, 1, 100, 100);
                    //constraints.fill = GridBagConstraints.BOTH;
                    gridbag.setConstraints(notInGroupRadioButtons[i], constraints);
                    panel1.add(notInGroupRadioButtons[i]);
                    
                    
                    
                }
                
                int maxLabelWidth = 0;
                
                for (int i = 0; i < expLabels.length; i++) {
                    if (expLabels[i].getPreferredSize().getWidth() > maxLabelWidth) {
                        maxLabelWidth = (int)Math.ceil(expLabels[i].getPreferredSize().getWidth());
                    }
                }
                
                JScrollPane scroll = new JScrollPane(panel1);
                scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
                scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
                
                
                
                JPanel exptNameHeaderPanel = new JPanel();
                GridBagLayout exptHeaderGridbag = new GridBagLayout();
                //exptNameHeaderPanel.HEIGHT = panel1.getHeight();
                //System.out.println("panel1.preferredSise().height = " + panel1.getPreferredSize().height);
                exptNameHeaderPanel.setSize(50, panel1.getPreferredSize().height);
                exptNameHeaderPanel.setPreferredSize(new Dimension(maxLabelWidth + 10, panel1.getPreferredSize().height));
                exptNameHeaderPanel.setLayout(exptHeaderGridbag);
                //scroll.getRowHeader().setLayout(exptHeaderGridbag);
                
                
                for (int i = 0; i < expLabels.length; i++) {
                    buildConstraints(constraints, 0, i, 1, 1, 100, 100);
                    constraints.fill = GridBagConstraints.BOTH;
                    exptHeaderGridbag.setConstraints(expLabels[i], constraints);
                    exptNameHeaderPanel.add(expLabels[i]);
                }
                
                scroll.setRowHeaderView(exptNameHeaderPanel);
                
                buildConstraints(constraints, 0, 0, 1, 1, 100, 90);
                constraints.fill = GridBagConstraints.BOTH;
                gridbag2.setConstraints(scroll, constraints);
                this.add(scroll);
                
                JLabel label1 = new JLabel("Note: Each group MUST each contain more than one experiment.");
                label1.setHorizontalAlignment(JLabel.CENTER);
                buildConstraints(constraints, 0, 1, 1, 1, 0, 5);
                constraints.anchor = GridBagConstraints.EAST;
                //constraints.fill = GridBagConstraints.BOTH;
                gridbag2.setConstraints(label1, constraints);
                this.add(label1);
                
                JPanel panel2 = new JPanel();
                GridBagLayout gridbag3 = new GridBagLayout();
                panel2.setLayout(gridbag3);
                panel2.setBackground(Color.white);
                JButton saveButton = new JButton("  Save settings  ");
                saveButton.setFocusPainted(false);
                saveButton.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED, Color.lightGray, Color.darkGray));
                JButton loadButton = new JButton("  Load settings  ");
                loadButton.setFocusPainted(false);
                loadButton.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED, Color.lightGray, Color.darkGray));
                JButton resetButton = new JButton("  Reset  ");
                resetButton.setFocusPainted(false);
                resetButton.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED, Color.lightGray, Color.darkGray));
                
                
                final int finNum = exptNames.size();
                
                resetButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        for (int i = 0; i < finNum; i++) {
                            exptGroupRadioButtons[0][i].setSelected(true);
                        }
                    }
                });
                
                final JFileChooser fc = new JFileChooser();
                fc.setCurrentDirectory(new File("Data"));
                
                saveButton.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent evt) {
                        int returnVal = fc.showSaveDialog(MultiGroupExperimentsPanel.this);
                        if (returnVal == JFileChooser.APPROVE_OPTION) {
                            File file = fc.getSelectedFile();
                            try {
                                PrintWriter out = new PrintWriter(new FileOutputStream(file));
                                int[] groupAssgn = getGroupAssignments();
                                for (int i = 0; i < groupAssgn.length; i++) {
                                    out.print(groupAssgn[i]);
                                    if (i < groupAssgn.length - 1) {
                                        out.print("\t");
                                    }
                                }
                                out.println();
                                out.flush();
                                out.close();
                            } catch (Exception e) {
                                //e.printStackTrace();
                            }
                            //this is where a real application would save the file.
                            //log.append("Saving: " + file.getName() + "." + newline);
                        } else {
                            //log.append("Save command cancelled by user." + newline);
                        }
                    }
                });
                
                
                //NEED TO REWORK THIS FOR MULTICLASS
                
                loadButton.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent evt) {
                        int returnVal = fc.showOpenDialog(MultiGroupExperimentsPanel.this);
                        if (returnVal == JFileChooser.APPROVE_OPTION) {
                            try {
                                FileReader file = new FileReader(fc.getSelectedFile());
                                BufferedReader buff = new BufferedReader(file);
                                String line = buff.readLine();
                                //System.out.println(line);
                                StringSplitter st = new StringSplitter('\t');
                                st.init(line);
                                Vector groupsVector = new Vector();
                                while (st.hasMoreTokens()) {
                                    String current = st.nextToken();
                                    groupsVector.add(new Integer(current));
                                    //System.out.print(current);
                                }
                                buff.close();
                                int[] groupAssgn = getGroupAssignments();
                                if (groupsVector.size() != groupAssgn.length) {
                                    JOptionPane.showMessageDialog(mPanel, "Incompatible file!", "Error", JOptionPane.WARNING_MESSAGE);
                                } else {
                                    for (int i = 0; i < groupsVector.size(); i++) {
                                        int currentGroup = ((Integer)groupsVector.get(i)).intValue();
                                        if (currentGroup != 0) {
                                            exptGroupRadioButtons[currentGroup - 1][i].setSelected(true);
                                        } else {
                                            notInGroupRadioButtons[i].setSelected(true);
                                        }
 
                                    }
                                }
                            } catch (Exception e) {
                                JOptionPane.showMessageDialog(mPanel, "Incompatible file!", "Error", JOptionPane.WARNING_MESSAGE);
                                for (int i = 0; i < finNum; i++) {
                                    exptGroupRadioButtons[0][i].setSelected(true);
                                }
                                //e.printStackTrace();
                            }
                            
                            //this is where a real application would save the file.
                            //log.append("Saving: " + file.getName() + "." + newline);
                        } else {
                            //log.append("Save command cancelled by user." + newline);
                        }
                    }
                });
                //
                
                
                constraints.anchor = GridBagConstraints.CENTER;
                constraints.fill = GridBagConstraints.NONE;
                constraints.insets = new Insets(5,5,5,5);
                buildConstraints(constraints, 0, 0, 1, 1, 33, 100);
                gridbag3.setConstraints(saveButton, constraints);
                panel2.add(saveButton);
                
                buildConstraints(constraints, 1, 0, 1, 1, 33, 0);
                gridbag3.setConstraints(loadButton, constraints);
                panel2.add(loadButton);
                
                buildConstraints(constraints, 2, 0, 1, 1, 34, 0);
                gridbag3.setConstraints(resetButton, constraints);
                panel2.add(resetButton);
                constraints.insets = new Insets(0,0,0,0);
                buildConstraints(constraints, 0, 2, 1, 1, 0, 5);
                constraints.anchor = GridBagConstraints.CENTER;
                //constraints.fill = GridBagConstraints.BOTH;
                gridbag2.setConstraints(panel2, constraints);
                this.add(panel2);
                
            /*
            JButton gButton = new JButton("groupExpts");
            buildConstraints(constraints, 0, 0, 1, 1, 100, 100);
            constraints.fill = GridBagConstraints.BOTH;
            gridbag.setConstraints(gButton, constraints);
            this.add(gButton);
             */
                
            }
            
            /**
             *  resets group assignments
             */
            private void reset(){
                for (int i = 0; i < exptNames.size(); i++) {
                    exptGroupRadioButtons[0][i].setSelected(true);
                }
            }
        }
        private void reset(){
            mulgPanel.reset();
        }
    }
    
    class PValuePanel extends JPanel {
        JTextField pValueInputField;
        JRadioButton justAlphaButton, stdBonfButton, adjBonfButton;
        
        public PValuePanel() {
            this.setBorder(new TitledBorder(new EtchedBorder(), "P-value parameters", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), Color.black));
            GridBagLayout gridbag = new GridBagLayout();
            GridBagConstraints constraints = new GridBagConstraints();
            //constraints.fill = GridBagConstraints.BOTH;
            this.setBackground(Color.white);
            //JPanel pane = new JPanel();
            this.setLayout(gridbag);
            
            JLabel pValueLabel = new JLabel("Enter alpha (critical p-value): ");
            buildConstraints(constraints, 0, 0, 1, 1, 33, 50);
            constraints.anchor = GridBagConstraints.EAST;
            gridbag.setConstraints(pValueLabel, constraints);
            this.add(pValueLabel);
            
            pValueInputField = new JTextField("0.01", 7);
            buildConstraints(constraints, 1, 0, 1, 1, 33, 0);
            constraints.anchor = GridBagConstraints.WEST;
            gridbag.setConstraints(pValueInputField, constraints);
            this.add(pValueInputField);
            
            constraints.anchor = GridBagConstraints.CENTER;
            
            justAlphaButton = new JRadioButton("Just alpha (no correction)", true);
            justAlphaButton.setBackground(Color.white);
            justAlphaButton.setVisible(false);
            stdBonfButton = new JRadioButton("Standard Bonferroni correction", false);
            stdBonfButton.setBackground(Color.white);
            stdBonfButton.setVisible(false); // WILL BE MADE VISIBLE WHEN THESE OPTIONS ARE IMPLEMENTED
            adjBonfButton = new JRadioButton("Adjusted Bonferroni correction", false);
            adjBonfButton.setBackground(Color.white);
            adjBonfButton.setVisible(false);// WILL BE MADE VISIBLE WHEN THESE OPTIONS ARE IMPLEMENTED
            
            ButtonGroup chooseCorrection = new ButtonGroup();
            chooseCorrection.add(justAlphaButton);
            chooseCorrection.add(stdBonfButton);
            chooseCorrection.add(adjBonfButton);
            
            stdBonfButton.setEnabled(false);
            adjBonfButton.setEnabled(false);
            
            buildConstraints(constraints, 0, 1, 1, 1, 33, 50);
            gridbag.setConstraints(justAlphaButton, constraints);
            this.add(justAlphaButton);
 
            buildConstraints(constraints, 1, 1, 1, 1, 33, 0);
            gridbag.setConstraints(stdBonfButton, constraints);
            this.add(stdBonfButton);  
            
            buildConstraints(constraints, 2, 1, 1, 1, 34, 0);
            gridbag.setConstraints(adjBonfButton, constraints);
            this.add(adjBonfButton);            
        }
        
        private void reset() {
            pValueInputField.setText("0.01");
            justAlphaButton.setSelected(true);
        }
        
    }
    
    public class EventListener extends WindowAdapter implements ActionListener{
        
        public void actionPerformed(ActionEvent ae) {
            String command = ae.getActionCommand();
            if(command.equals("ok-command")){
                boolean tooFew = false;
                int[] grpAssignments = getGroupAssignments();
                int numGroups = getNumGroups();
                int[] groupSize = new int[numGroups];
                
                for (int i = 0; i < groupSize.length; i++) {
                    groupSize[i] = 0;
                }
                
                for (int i = 0; i < grpAssignments.length; i++) {
                    int currentGroup = grpAssignments[i];
                    if (currentGroup != 0) {
                        groupSize[currentGroup - 1]++;
                    }
                }
                
                        /*
                        for (int i = 0; i < grpAssignments.length; i++) {
                            System.out.println("grpAssignments[" + i + "] = "  + grpAssignments[i]);
                        }
                         
                        for (int i = 0; i < groupSize.length; i++) {
                            System.out.println("groupSize[" + i + "] = " + groupSize[i] + " (group " + (i +1) + ")");
                        }
                         */
                
                for (int i = 0; i < groupSize.length; i++) {
                    if (groupSize[i] <= 1) {
                        JOptionPane.showMessageDialog(null, "Each group must contain more than one experiment", "Error", JOptionPane.WARNING_MESSAGE);
                        tooFew = true;
                        break;
                    }
                }
                
                if (!tooFew) {
                    try {
                        //HERE, CHECK OTHER INPUTS: P-VALUE VALIDITY - 4/25/03
                        double d = Double.parseDouble(pPanel.pValueInputField.getText());
                        if ((d <= 0d)||(d > 1d)) {
                            JOptionPane.showMessageDialog(null, "P-value must be between 0 and 1", "Error!", JOptionPane.ERROR_MESSAGE);
                        } else {
                            okPressed = true;
                            dispose();
                        }
                    } catch (NumberFormatException nfe) {
                        JOptionPane.showMessageDialog(null, "P-value must be between 0 and 1", "Error!", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else if (command.equals("reset-command")) {
                mPanel.reset();
                pPanel.reset();
            } else if (command.equals("cancel-command")) {
                okPressed = false;
                dispose();
            } else if (command.equals("info-command")){
                HelpWindow hw = new HelpWindow(OneWayANOVAInitBox.this, "One Way ANOVA Initialization Dialog");
                okPressed = false;
                if(hw.getWindowContent()){
                    hw.setSize(450,600);
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
        }
        
    }
    
    public int[] getGroupAssignments() {
        int[] groupAssignments = new int[exptNames.size()];
        
        for (int i = 0; i < exptNames.size(); i++) {
            if (mPanel.mulgPanel.notInGroupRadioButtons[i].isSelected()) {// "NOT IN GROUP" IS STORED AS ZERO, AND GROUP J IS STORED AS THE INTEGER J (I.E., THERE IS NO GROUP 0)
                groupAssignments[i] = 0;
            } else {
                for (int j = 0; j < mPanel.mulgPanel.exptGroupRadioButtons.length; j++) {
                    if (mPanel.mulgPanel.exptGroupRadioButtons[j][i].isSelected()) {
                        groupAssignments[i] = j + 1;
                        break;
                    }
                }
            }
        }
        
        
        return groupAssignments;
    }   
    
    public int getNumGroups() {
        return mPanel.numGroups;
    }
    
    public double getPValue() {
        return Double.parseDouble(pPanel.pValueInputField.getText());
    }

    public int getCorrectionMethod() {
        int method = JUST_ALPHA;
        if (pPanel.justAlphaButton.isSelected()) {
            method = JUST_ALPHA;
        } else if (pPanel.stdBonfButton.isSelected()) {
            method = STD_BONFERRONI;
        } else {
            method = ADJ_BONFERRONI;
        }
        
        return method;
    }
    
    
    public static void main(String[] args) {
        JFrame dummyFrame = new JFrame();
        Vector dummyVect = new Vector();
        for (int i = 0; i < 100; i++) {
            dummyVect.add("Expt " + i);
        }
        
        OneWayANOVAInitBox oBox = new OneWayANOVAInitBox(dummyFrame, true, dummyVect);
        oBox.setVisible(true);
        
    }
}
