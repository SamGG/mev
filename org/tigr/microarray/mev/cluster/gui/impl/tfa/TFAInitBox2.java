/*
 * TFAInitBox2.java
 *
 * Created on February 12, 2004, 4:15 PM
 */

package org.tigr.microarray.mev.cluster.gui.impl.tfa;

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
 */
public class TFAInitBox2 extends AlgorithmDialog {
    
    public static final int JUST_ALPHA = 4;
    public static final int STD_BONFERRONI = 5;
    public static final int ADJ_BONFERRONI = 6;    
    public static final int MAX_T = 9;
    public static final int MIN_P = 10;
    
    boolean okPressed = false;
    private boolean oneSamplePerCell = false;
    Vector exptNames;
    String[] factorNames;
    int[] numFactorLevels;
    GroupExptsPanel gPanel;
    PValuePanel pPanel;
    PValueAdjustmentPanel pAdjPanel;
    HCLSelectionPanel hclOpsPanel;
    /** Creates a new instance of TFAInitBox2 */
    public TFAInitBox2(JFrame parentFrame, boolean modality, Vector exptNames, String[] factorNames, int[] numFactorLevels) {
        super(parentFrame, "Two-factor ANOVA Initialization", modality);
        this.exptNames = exptNames;
        this.factorNames = factorNames;
        this.numFactorLevels = numFactorLevels;
        
        setBounds(0, 0, 800, 720);
        setBackground(Color.white);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        
        JPanel pane = new JPanel();
        pane.setLayout(gridbag);
        
        gPanel = new GroupExptsPanel();
        
        buildConstraints(constraints, 0, 0, 1, 1, 100, 80);
        gridbag.setConstraints(gPanel, constraints);
        pane.add(gPanel);
        
        pPanel = new PValuePanel();
        buildConstraints(constraints, 0, 1, 1, 1, 0, 8);
        gridbag.setConstraints(pPanel, constraints);
        pane.add(pPanel);
        
        pAdjPanel = new PValueAdjustmentPanel();
        buildConstraints(constraints, 0, 2, 1, 1, 0, 8);
        gridbag.setConstraints(pAdjPanel, constraints);
        pane.add(pAdjPanel);
        
        hclOpsPanel = new HCLSelectionPanel();
        buildConstraints(constraints, 0, 3, 1, 1, 0, 4);
        gridbag.setConstraints(hclOpsPanel, constraints);
        pane.add(hclOpsPanel);
        
        addContent(pane);
        EventListener listener = new EventListener();
        setActionListeners(listener);
        this.addWindowListener(listener);
    }
    
    void buildConstraints(GridBagConstraints gbc, int gx, int gy, int gw, int gh, int wx, int wy) {
        gbc.gridx = gx;
        gbc.gridy = gy;
        gbc.gridwidth = gw;
        gbc.gridheight = gh;
        gbc.weightx = wx;
        gbc.weighty = wy;
    }
    
    class GroupExptsPanel extends JPanel {
        MultiGroupExperimentsPanel factorAPanel, factorBPanel;
        JPanel panel2;
        GroupExptsPanel() {
            this.setBorder(new TitledBorder(new EtchedBorder(), "Group Assignments", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), Color.black));
            setBackground(Color.white);
            factorAPanel = new MultiGroupExperimentsPanel(factorNames[0], numFactorLevels[0]);
            factorBPanel = new MultiGroupExperimentsPanel(factorNames[1], numFactorLevels[1]);
            
            GridBagLayout grid1 = new GridBagLayout();
            GridBagConstraints constraints = new GridBagConstraints();
            this.setLayout(grid1);
            
            buildConstraints(constraints, 0, 0, 1, 1, 50, 90);
            constraints.fill = GridBagConstraints.BOTH;
            grid1.setConstraints(factorAPanel, constraints);
            this.add(factorAPanel);
            
            buildConstraints(constraints, 1, 0, 1, 1, 50, 0);
            constraints.fill = GridBagConstraints.BOTH;
            grid1.setConstraints(factorBPanel, constraints);
            this.add(factorBPanel);
            
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
            
            
            //final int finNum = exptNames.size();
            
            resetButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    factorAPanel.reset();
                    factorBPanel.reset();
                }
            });
            
            final JFileChooser fc = new JFileChooser();
            fc.setCurrentDirectory(new File("Data"));
            
            saveButton.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent evt) {
                    int returnVal = fc.showSaveDialog(GroupExptsPanel.this);
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        File file = fc.getSelectedFile();
                        try {
                            PrintWriter out = new PrintWriter(new FileOutputStream(file));
                            int[] factorAGroupAssgn = factorAPanel.getGroupAssignments();
                            int[] factorBGroupAssgn = factorBPanel.getGroupAssignments();
                            for (int i = 0; i < factorAGroupAssgn.length; i++) {
                                out.print(factorAGroupAssgn[i]);
                                if (i < factorAGroupAssgn.length - 1) {
                                    out.print("\t");
                                }
                            }
                            out.println();
                            
                            for (int i = 0; i < factorBGroupAssgn.length; i++) {
                                out.print(factorBGroupAssgn[i]);
                                if (i < factorBGroupAssgn.length - 1) {
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
            
            
            
            
            loadButton.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent evt) {
                    int returnVal = fc.showOpenDialog(GroupExptsPanel.this);
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        try {
                            FileReader file = new FileReader(fc.getSelectedFile());
                            BufferedReader buff = new BufferedReader(file);
                            String line = buff.readLine();
                            //System.out.println(line);
                            StringSplitter st = new StringSplitter('\t');
                            st.init(line);
                            Vector factorAGroupsVector = new Vector();
                            Vector factorBGroupsVector = new Vector();
                            while (st.hasMoreTokens()) {
                                String current = st.nextToken();
                                factorAGroupsVector.add(new Integer(current));
                                //System.out.print(current);
                            }
                            
                            line = buff.readLine();
                            st.init(line);
                            while (st.hasMoreTokens()) {
                                String current = st.nextToken();
                                factorBGroupsVector.add(new Integer(current));
                                //System.out.print(current);
                            }
                            
                            buff.close();
                            int[] factorAGroupAssgn = factorAPanel.getGroupAssignments();
                            int[] factorBGroupAssgn = factorBPanel.getGroupAssignments();
                            if ((factorAGroupsVector.size() != factorAGroupAssgn.length) || (factorBGroupsVector.size() != factorBGroupAssgn.length)){
                                JOptionPane.showMessageDialog(gPanel, "Incompatible file!", "Error", JOptionPane.WARNING_MESSAGE);
                            } else {
                                int[] factorAAssignments = new int[factorAGroupsVector.size()];
                                int[] factorBAssignments = new int[factorBGroupsVector.size()];
                                for (int i = 0; i < factorAAssignments.length; i++) {
                                    factorAAssignments[i] = ((Integer)(factorAGroupsVector.get(i))).intValue();
                                    factorBAssignments[i] = ((Integer)(factorBGroupsVector.get(i))).intValue();
                                }
                                
                                factorAPanel.setGroupAssignments(factorAAssignments);
                                factorBPanel.setGroupAssignments(factorBAssignments);
                            }
                        } catch (Exception e) {
                            JOptionPane.showMessageDialog(gPanel, "Incompatible file!", "Error", JOptionPane.WARNING_MESSAGE);
                            factorAPanel.reset();
                            factorBPanel.reset();
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
            
            buildConstraints(constraints, 0, 1, 2, 1, 100, 5);
            constraints.anchor = GridBagConstraints.CENTER;
            //constraints.fill = GridBagConstraints.BOTH;
            grid1.setConstraints(panel2, constraints);
            this.add(panel2);
            
        }
        
        public void reset() {
            factorAPanel.reset();
            factorBPanel.reset();
        }
    }
    
    class MultiGroupExperimentsPanel extends JPanel {
        JLabel[] expLabels;
        JRadioButton[][] exptGroupRadioButtons;
        JRadioButton[] notInGroupRadioButtons;
        int numGroups;
        
        MultiGroupExperimentsPanel(String factorName, int numGroups) {
            this.setBorder(new TitledBorder(new EtchedBorder(), factorName + " assignments", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), Color.black));
            this.numGroups = numGroups;
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
            
            buildConstraints(constraints, 0, 0, 1, 1, 100, 100);
            constraints.fill = GridBagConstraints.BOTH;
            gridbag2.setConstraints(scroll, constraints);
            this.add(scroll);
            
        }
        
        public void reset() {
            for (int i = 0; i < exptNames.size(); i++) {
                exptGroupRadioButtons[0][i].setSelected(true);
            }
        }
        
        
        public int[] getGroupAssignments() {
            int[] groupAssignments = new int[exptNames.size()];
            
            for (int i = 0; i < groupAssignments.length; i++) {
                groupAssignments[i] = 0;
            }
            
            for (int i = 0; i < groupAssignments.length; i++) {
                for (int j = 0; j < numGroups; j++) {
                    if (exptGroupRadioButtons[j][i].isSelected()) {
                        groupAssignments[i] = j + 1;
                        break;
                    }
                }
            }
            
            return groupAssignments;
        }
        
        public void setGroupAssignments(int[] assignments) {
            for (int i = 0; i < assignments.length; i++) {
                if (assignments[i] == 0) {
                    notInGroupRadioButtons[i].setSelected(true);
                } else {
                    exptGroupRadioButtons[assignments[i] - 1][i].setSelected(true);
                }
            }
        }
        
    }
    
    class PValuePanel extends JPanel {
        JRadioButton tDistButton, permutButton; // randomGroupsButton, allCombsButton;
        JLabel numPermsLabel;
        JTextField timesField, alphaInputField;
        //JButton permParamButton;
        
        PValuePanel() {
            // this.setBorder(new TitledBorder(new EtchedBorder(), "P-Value parameters"));
            this.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED), "P-Value Parameters", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), Color.black));
            this.setBackground(Color.white);
            GridBagLayout gridbag = new GridBagLayout();
            GridBagConstraints constraints = new GridBagConstraints();
            //constraints.anchor = GridBagConstraints.WEST;
            //constraints.fill = GridBagConstraints.BOTH;
            this.setLayout(gridbag);
            
            //permParamButton = new JButton("Permutation parameters");
            //permParamButton.setEnabled(false);
            
            ButtonGroup chooseP = new ButtonGroup();
            
            tDistButton = new JRadioButton("p-values based on F-distribution", true);
            tDistButton.setFocusPainted(false);
            tDistButton.setForeground(UIManager.getColor("Label.foreground"));
            tDistButton.setBackground(Color.white);
            
            numPermsLabel = new JLabel("Enter number of permutations");
            numPermsLabel.setEnabled(false);
            
            timesField = new JTextField("1000", 7);
            timesField.setEnabled(false);
            timesField.setBackground(Color.darkGray);
            
            tDistButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    numPermsLabel.setEnabled(false);
                    timesField.setEnabled(false);
                    timesField.setBackground(Color.darkGray);                  
                    pAdjPanel.maxTButton.setEnabled(false);
                    pAdjPanel.minPButton.setEnabled(false);
                }
            });
            
            chooseP.add(tDistButton);
            
            permutButton = new JRadioButton("p-values based on permutation:  ", false);
            permutButton.setFocusPainted(false);
            permutButton.setForeground(UIManager.getColor("Label.foreground"));
            permutButton.setBackground(Color.white);
            
            permutButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    numPermsLabel.setEnabled(true);
                    timesField.setEnabled(true);
                    timesField.setBackground(Color.white);                  
                    //pAdjPanel.maxTButton.setEnabled(true);  //UNCOMMENT THIS WHEN MAXT METHOD HAS BEEN IMPLEMEMTED
                    //pAdjPanel.minPButton.setEnabled(true);  //UNCOMMENT THIS WHEN MINP METHOD HAS BEEN DEBUGGED                  
                }                
            });
            
            chooseP.add(permutButton);
            
            
            //constraints.anchor = GridBagConstraints.CENTER;
            
            //numCombsLabel = new JLabel("                                       ");
            //numCombsLabel.setOpaque(false);
            /*
            constraints.fill = GridBagConstraints.BOTH;
            buildConstraints(constraints, 1, 2, 1, 1, 0, 0);
            gridbag.setConstraints(numCombsLabel, constraints);
            this.add(numCombsLabel);
             */
            
            
            buildConstraints(constraints, 0, 0, 3, 1, 100, 30);
            //constraints.fill = GridBagConstraints.BOTH;
            constraints.anchor = GridBagConstraints.WEST;
            gridbag.setConstraints(tDistButton, constraints);
            this.add(tDistButton);
            
            buildConstraints(constraints, 0, 1, 1, 1, 30, 30);
            constraints.anchor = GridBagConstraints.WEST;
            gridbag.setConstraints(permutButton, constraints);
            this.add(permutButton);
            
            //JLabel numPermsLabel = new JLabel("Enter number of permutations");
            //numPermsLabel.setEnabled(false);
            buildConstraints(constraints, 1, 1, 1, 1, 30, 0);
            gridbag.setConstraints(numPermsLabel, constraints);
            this.add(numPermsLabel);
            
            buildConstraints(constraints, 2, 1, 1, 1, 40, 0);
            gridbag.setConstraints(timesField, constraints);
            this.add(timesField);
            
            JLabel alphaLabel = new JLabel("Enter critical p-value");
            buildConstraints(constraints, 0, 2, 2, 1, 60, 40);
            //constraints.anchor = GridBagConstraints.EAST;
            gridbag.setConstraints(alphaLabel, constraints);
            this.add(alphaLabel);
            
            alphaInputField = new JTextField("0.01", 7);
            buildConstraints(constraints, 1, 2, 1, 1, 40, 0);
            constraints.anchor = GridBagConstraints.WEST;
            gridbag.setConstraints(alphaInputField, constraints);
            this.add(alphaInputField);
        }
    }
    
    class PValueAdjustmentPanel extends JPanel {
        JRadioButton minPButton, maxTButton, justAlphaButton, stdBonfButton, adjBonfButton;
        PValueAdjustmentPanel() {
            //      this.setBorder(new TitledBorder(new EtchedBorder(), "Significance based on: "));
            this.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED), "Alpha Corrections", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), Color.black));
            this.setBackground(Color.white);
            GridBagLayout gridbag = new GridBagLayout();
            GridBagConstraints constraints = new GridBagConstraints();
            this.setLayout(gridbag);
            
            /*
            JLabel sigLabel = new JLabel("Significance based on: ");
            buildConstraints(constraints, 0, 0, 3, 1, 0, 50);
            //constraints.fill = GridBagConstraints.BOTH;
            gridbag.setConstraints(sigLabel, constraints);
            this.add(sigLabel);
             */
            
            ButtonGroup sigGroup = new ButtonGroup();
            
            justAlphaButton = new JRadioButton("just alpha (no correction)", true);
            justAlphaButton.setFocusPainted(false);
            justAlphaButton.setForeground(UIManager.getColor("Label.foreground"));
            justAlphaButton.setBackground(Color.white);
            sigGroup.add(justAlphaButton);
            
            stdBonfButton = new JRadioButton("standard Bonferroni correction", false);
            stdBonfButton.setEnabled(false);//enable this button when this option is implemented
            stdBonfButton.setFocusPainted(false);
            stdBonfButton.setForeground(UIManager.getColor("Label.foreground"));
            stdBonfButton.setBackground(Color.white);
            sigGroup.add(stdBonfButton);
            
            adjBonfButton = new JRadioButton("adjusted Bonferroni correction", false);
            adjBonfButton.setEnabled(false);//enable this button when this option is implemented
            adjBonfButton.setFocusPainted(false);
            adjBonfButton.setForeground(UIManager.getColor("Label.foreground"));
            adjBonfButton.setBackground(Color.white);
            sigGroup.add(adjBonfButton);
            
            minPButton = new JRadioButton("minP", false);
            minPButton.setEnabled(false);
            minPButton.setFocusPainted(false);
            minPButton.setForeground(UIManager.getColor("Label.foreground"));
            minPButton.setBackground(Color.white);
            sigGroup.add(minPButton);
            
            maxTButton = new JRadioButton("maxT", false);
            maxTButton.setEnabled(false);
            maxTButton.setFocusPainted(false);
            maxTButton.setForeground(UIManager.getColor("Label.foreground"));
            maxTButton.setBackground(Color.white);
            sigGroup.add(maxTButton);
            
            buildConstraints(constraints, 0, 0, 1, 1, 33, 50);
            //constraints.fill = GridBagConstraints.BOTH;
            gridbag.setConstraints(justAlphaButton, constraints);
            this.add(justAlphaButton);
            
            buildConstraints(constraints, 1, 0, 1, 1, 33, 0);
            //constraints.fill = GridBagConstraints.BOTH;
            gridbag.setConstraints(stdBonfButton, constraints);
            this.add(stdBonfButton);
            
            buildConstraints(constraints, 2, 0, 1, 1, 34, 0);
            //constraints.fill = GridBagConstraints.BOTH;
            gridbag.setConstraints(adjBonfButton, constraints);
            this.add(adjBonfButton);
            
            JPanel westfallYoungPanel = new JPanel();
            westfallYoungPanel.setBackground(Color.white);
            westfallYoungPanel.setBorder(new EtchedBorder());
            GridBagLayout grid2 = new GridBagLayout();
            westfallYoungPanel.setLayout(grid2);
            
            JLabel stepDownLabel = new JLabel("Step-down Westfall and Young methods (for permutations only): ");
            buildConstraints(constraints, 0, 0, 1, 1, 34, 100);
            //buildConstraints(constraints, 0, 1, 1, 1, 34, 50);
            ////constraints.fill = GridBagConstraints.BOTH;
            //constraints.anchor = GridBagConstraints.EAST;
            //gridbag.setConstraints(stepDownLabel, constraints);
            grid2.setConstraints(stepDownLabel, constraints);
            //this.add(stepDownLabel);
            westfallYoungPanel.add(stepDownLabel);
            
            //buildConstraints(constraints, 1, 1, 1, 1, 33, 0);
            buildConstraints(constraints, 1, 0, 1, 1, 33, 0);
            ////constraints.fill = GridBagConstraints.BOTH;
            //constraints.anchor = GridBagConstraints.WEST;
            //gridbag.setConstraints(minPButton, constraints);
            grid2.setConstraints(minPButton, constraints);
            //this.add(minPButton);
            westfallYoungPanel.add(minPButton);
            
            
            //buildConstraints(constraints, 2, 1, 1, 1, 33, 0);
            buildConstraints(constraints, 2, 0, 1, 1, 33, 0);
            ////constraints.fill = GridBagConstraints.BOTH;
            //constraints.anchor = GridBagConstraints.WEST;
            //gridbag.setConstraints(maxTButton, constraints);
            grid2.setConstraints(maxTButton, constraints);
            //this.add(maxTButton);
            westfallYoungPanel.add(maxTButton);
            
            buildConstraints(constraints, 0, 1, 3, 1, 100, 50);
            //constraints.fill = GridBagConstraints.BOTH;
            gridbag.setConstraints(westfallYoungPanel, constraints);
            this.add(westfallYoungPanel);
            /*
            JButton sButton = new JButton("significancePanel");
            buildConstraints(constraints, 0, 0, 1, 1, 100, 100);
            constraints.fill = GridBagConstraints.BOTH;
            gridbag.setConstraints(sButton, constraints);
            this.add(sButton);
             */
            
        }
    }
    
    public boolean allCellsHaveOneSample() {
        return this.oneSamplePerCell;
    }
    
    public boolean usePerms() {
        return pPanel.permutButton.isSelected();
    }
    
    public float getAlpha() {
        return Float.parseFloat(pPanel.alphaInputField.getText());
    }
    
    public int getNumPerms() {
        return Integer.parseInt(pPanel.timesField.getText());
    }
    
    public boolean isBalancedDesign() {
        boolean balanced = true;
        Vector[][] bothFactorAssignments = getBothFactorAssignments();     
        int[] cellSizes =new int[bothFactorAssignments.length*bothFactorAssignments[0].length];
        //System.out.println("cellSizes.length = " + cellSizes.length);
        int cellCounter = 0;
        for (int i = 0; i < bothFactorAssignments.length; i++) {
            for (int j = 0; j < bothFactorAssignments[i].length; j++) {
                cellSizes[cellCounter] = bothFactorAssignments[i][j].size();
                cellCounter++;
            }
        } 
        
        int numPerCell = cellSizes[0];
        
        for (int i = 1; i < cellSizes.length; i++) {
            if (cellSizes[i] != numPerCell) {
                balanced = false;
                break;
            }
        }
        
        return balanced;
    }
    
    public int getAdjustmentMethod() {
        if (pAdjPanel.justAlphaButton.isSelected()) {
            return this.JUST_ALPHA;
        } else if (pAdjPanel.stdBonfButton.isSelected()) {
            return this.STD_BONFERRONI;
        } else if (pAdjPanel.adjBonfButton.isSelected()){
            return this.ADJ_BONFERRONI;
        } else if (pAdjPanel.maxTButton.isSelected()) {
            return this.MAX_T;
        } else if (pAdjPanel.minPButton.isSelected()) {
            return this.MIN_P;
        } else {
            return -1;
        }
    }    
    
    public boolean drawTrees() {
        return this.hclOpsPanel.isHCLSelected();
    }
    
    public boolean isOkPressed() {
        return okPressed;
    }
    
    public void setVisible(boolean visible) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);
        super.setVisible(visible);
        if (visible) {
            //bPanel.okButton.requestFocus(); //UNCOMMMENT THIS LATER
        }
    }

    
    public class EventListener extends WindowAdapter implements ActionListener{
        
        public void actionPerformed(ActionEvent ae){
            
            String command = ae.getActionCommand();
            if(command.equals("ok-command")){
                Vector[][] bothFactorAssignments = getBothFactorAssignments();
                int[] cellSizes =new int[bothFactorAssignments.length*bothFactorAssignments[0].length];
                //System.out.println("cellSizes.length = " + cellSizes.length);
                int cellCounter = 0;
                for (int i = 0; i < bothFactorAssignments.length; i++) {
                    for (int j = 0; j < bothFactorAssignments[i].length; j++) {
                        cellSizes[cellCounter] = bothFactorAssignments[i][j].size();
                        cellCounter++;
                    }
                }
                if (cellSizes[0] == 1) {
                    boolean allOne = true;
                    for (int i = 1; i < cellSizes.length; i++) {
                        if (cellSizes[i] != 1) {
                            allOne = false;
                            //oneSamplePerCell = true;
                            break;
                        }
                    }
                    if (!allOne) {
                        JOptionPane.showMessageDialog(null, "All factor combinations must contain more than one sample, or else they must all contain exactly one sample each", "Error", JOptionPane.ERROR_MESSAGE);
                        okPressed = false;
                        return;
                    } else {
                        oneSamplePerCell = true;
                    }
                } else {
                    for (int i = 0; i < cellSizes.length; i++) {
                        if (cellSizes[i] < 2) {
                            JOptionPane.showMessageDialog(null, "All factor combinations must contain more than one sample, or else they must all contain exactly one sample each", "Error", JOptionPane.ERROR_MESSAGE);
                            okPressed = false;
                            return;                            
                        }
                    }
                }
                
                if (usePerms()) {
                    try {
                        int numPerms = getNumPerms();
                        if (numPerms <= 0) {
                            JOptionPane.showMessageDialog(null, "Number of permutations should be an integer > 0", "Error", JOptionPane.ERROR_MESSAGE);
                            okPressed = false;
                            return;                         
                        }
                        } catch (NumberFormatException nfe) {
                            JOptionPane.showMessageDialog(null, "Number of permutations should be an integer > 0", "Error", JOptionPane.ERROR_MESSAGE);
                            okPressed = false;
                            return;
                        }
                }
                
                try {
                    float alpha = getAlpha();
                    if ((alpha <= 0) || (alpha >= 1)) {
                        JOptionPane.showMessageDialog(null, "Critical p-value should be between 0 and 1", "Error", JOptionPane.ERROR_MESSAGE);
                        okPressed = false;
                        return;                        
                    }
                } catch (NumberFormatException nfe) {
                    JOptionPane.showMessageDialog(null, "Critical p-value should be between 0 and 1", "Error", JOptionPane.ERROR_MESSAGE);
                    okPressed = false;
                    return;                    
                }
                
                okPressed = true;
                hide();
                dispose();                
            }
            else if(command.equals("reset-command")){
                okPressed = false;
                gPanel.reset();
                return;
            }
            else if(command.equals("cancel-command")){
                okPressed = false;
                setVisible(false);
                dispose();
            }
            else if(command.equals("info-command")){
            }
        }
        
    }
    
    public int[] getFactorAAssignments() {
        return gPanel.factorAPanel.getGroupAssignments();
    }
    
    public int[] getFactorBAssignments() {
        return gPanel.factorBPanel.getGroupAssignments();
    }   
    
    public Vector[][] getBothFactorAssignments() {
        Vector[][] bothFactorAssignments = new Vector[numFactorLevels[0]][numFactorLevels[1]];
        
        for (int i = 0; i < bothFactorAssignments.length; i++) {
            for (int j = 0; j < bothFactorAssignments[i].length; j++) {
                bothFactorAssignments[i][j] = new Vector();
            }
        }
        
        int[] factorAAssgn = getFactorAAssignments();
        int[] factorBAssgn = getFactorBAssignments();
        
        for (int i = 0; i < factorAAssgn.length; i++) {
            if ((factorAAssgn[i] != 0)&&(factorBAssgn[i] != 0)) {
                bothFactorAssignments[factorAAssgn[i] - 1][factorBAssgn[i] - 1].add(new Integer(i));
            }
        }
        
        return bothFactorAssignments;
    }
}
