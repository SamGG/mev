/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * SAMInitDialog.java
 *
 * Created on November 7, 2002, 2:06 PM
 */

package org.tigr.microarray.mev.cluster.gui.impl.sam;

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
public class SAMInitDialog extends AlgorithmDialog {
 
    ImageIcon forwardImage, backImage; 
    JTabbedPane tabPane;
    GroupExperimentsPanel gPanel;
    TwoClassPairedMainPanel tcpmPanel;
    MultiClassPanel mPanel;
    CensoredSurvivalPanel csPanel;
    OneClassPanel oneCPanel;
    S0AndQValueCalcPanel sqPanel;
    PermutationsPanel pPanel;
    ImputationPanel iPanel;
    OKCancelPanel oPanel;
    
    boolean okPressed = false, allUniquePermsUsed = false;
    Vector exptNames;
    int numGenes, numUniquePerms;
    HCLSelectionPanel hclOpsPanel;
    //JFrame parentFrame;
    
    public static final int GROUP_A = 1;
    public static final int GROUP_B = 2;
    public static final int NEITHER_GROUP = 3;
    
    public static final int TWO_CLASS_UNPAIRED = 4;
    public static final int TWO_CLASS_PAIRED = 5;
    public static final int MULTI_CLASS = 6;
    public static final int CENSORED_SURVIVAL = 7;
    public static final int ONE_CLASS = 8;    
    
    /** Creates new SAMInitDialog */
    public SAMInitDialog(JFrame parentFrame, boolean modality, Vector exptNames, int numGenes) {
        
        super(parentFrame, "SAM Initialization", modality);
        //this.parentFrame = parentFrame;
        this.exptNames = exptNames;
        this.numGenes = numGenes;
        this.numUniquePerms = 0;
        setBounds(0, 0, 700, 800);
        setBackground(Color.white);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        forwardImage = new ImageIcon(Toolkit.getDefaultToolkit().getImage(this.getClass().getClassLoader().getResource("org/tigr/images/Forward24.gif")));        
        backImage = new ImageIcon(Toolkit.getDefaultToolkit().getImage(this.getClass().getClassLoader().getResource("org/tigr/images/Back24.gif")));        
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        
        JPanel pane = new JPanel();
        pane.setLayout(gridbag);
        
        javax.swing.UIManager.put("TabbedPane.selected", Color.white);
        tabPane = new JTabbedPane();
        gPanel = new GroupExperimentsPanel(exptNames);
        tabPane.add("Two-class unpaired", gPanel);
        tcpmPanel = new TwoClassPairedMainPanel();
        tabPane.add("Two-class paired", tcpmPanel);
        mPanel = new MultiClassPanel(/*exptNames*/);
        tabPane.add("Multi-class", mPanel);
        csPanel = new CensoredSurvivalPanel(exptNames);
        tabPane.add("Censored survival", csPanel);
        oneCPanel = new OneClassPanel();
        tabPane.add("One-Class", oneCPanel);
        //tabPane.setEnabledAt(1, false);
        //    tabPane.setBackground(Color.white);
        
        /*
        tabPane.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                SAMInitDialog.this.validate();
            }
        });
         */
        buildConstraints(constraints, 0, 0, 1, 1, 100, 75);
        
        gridbag.setConstraints(tabPane, constraints);
        
        pane.add(tabPane);
        
        /*
        JButton topButton = new JButton("topPanel");
        buildConstraints(constraints, 0, 0, 1, 1, 100, 50);
        gridbag.setConstraints(topButton, constraints);
        pane.add(topButton);
         */
        
        pPanel = new PermutationsPanel();
        buildConstraints(constraints, 0, 1, 1, 1, 0, 5);
        gridbag.setConstraints(pPanel, constraints);
        pane.add(pPanel);

        sqPanel = new S0AndQValueCalcPanel();
        buildConstraints(constraints, 0, 2, 1, 1, 0, 10);
        gridbag.setConstraints(sqPanel, constraints);
        pane.add(sqPanel);        
        
        /*
        JButton numPermsButton = new JButton("numPermsPanel");
        buildConstraints(constraints, 0, 1, 1, 1, 0, 15);
        gridbag.setConstraints(numPermsButton, constraints);
        pane.add(numPermsButton);
         */
        
        iPanel = new ImputationPanel();
        buildConstraints(constraints, 0, 3, 1, 1, 0, 5);
        gridbag.setConstraints(iPanel, constraints);
        pane.add(iPanel);
        
        /*
        JButton imputeButton = new JButton("imputationPanel");
        buildConstraints(constraints, 0, 2, 1, 1, 0, 15);
        gridbag.setConstraints(imputeButton, constraints);
        pane.add(imputeButton);
         */
        
        hclOpsPanel = new HCLSelectionPanel();
        buildConstraints(constraints, 0, 4, 1, 1, 0, 5);
        gridbag.setConstraints(hclOpsPanel, constraints);
        pane.add(hclOpsPanel);
        
        /*
        JButton bottomButton = new JButton("okCancelPanel");
        buildConstraints(constraints, 0, 3, 1, 1, 0, 20);
        gridbag.setConstraints(bottomButton, constraints);
        pane.add(bottomButton);
         */
        addContent(pane);
        EventListener listener = new EventListener();
        setActionListeners(listener);
        this.addWindowListener(listener);
        //setContentPane(pane);
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
    
    public boolean isSaveMatrix() {
        return iPanel.saveMatrixChkBox.isSelected();
    }
    
    class GroupExperimentsPanel extends JPanel {
        JLabel[] expLabels;
        JRadioButton[] groupARadioButtons, groupBRadioButtons, neitherGroupRadioButtons;
        GroupExperimentsPanel(Vector exptNames) {
            //   this.setBorder(new TitledBorder(new EtchedBorder(), "Group Assignments"));
            this.setBorder(new TitledBorder(new EtchedBorder(), "Group Assignments", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), Color.black));
            setBackground(Color.white);
            JPanel panel1 = new JPanel();
            expLabels = new JLabel[exptNames.size()];
            groupARadioButtons = new JRadioButton[exptNames.size()];
            groupBRadioButtons = new JRadioButton[exptNames.size()];
            neitherGroupRadioButtons = new JRadioButton[exptNames.size()];
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
                groupARadioButtons[i] = new JRadioButton("Group A", true);
                chooseGroup[i].add(groupARadioButtons[i]);
                groupBRadioButtons[i] = new JRadioButton("Group B", false);
                chooseGroup[i].add(groupBRadioButtons[i]);
                neitherGroupRadioButtons[i] = new JRadioButton("Neither group", false);
                chooseGroup[i].add(neitherGroupRadioButtons[i]);
                
                /*
                groupARadioButtons[i].addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        int[] groupCount = getGroupCount();
                        if ((groupCount[0] < 2)|| (groupCount[1] < 2)) {
                            pPanel.permsInfoLabel.setForeground(Color.red);
                            pPanel.permsInfoLabel.setText("Group A and Group B must each contain more than one experiment");
                            pPanel.useAllPermsButton.setEnabled(false);
                        } else if ((groupCount[0] + groupCount[1]) > 20) {
                            pPanel.permsInfoLabel.setForeground(Color.black);
                            pPanel.permsInfoLabel.setText("There are too many possible permutations");
                            pPanel.useAllPermsButton.setEnabled(false);
                 
                        } else {
                            pPanel.permsInfoLabel.setForeground(Color.black);
                            int numCombs = getNumCombs(groupCount[0] + groupCount[1], groupCount[0]);
                            pPanel.permsInfoLabel.setText("There are " + numCombs + " permutations");
                            pPanel.useAllPermsButton.setEnabled(true);
                        }
                    }
                });
                 
                groupBRadioButtons[i].addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        int[] groupCount = getGroupCount();
                        if ((groupCount[0] < 2)|| (groupCount[1] < 2)) {
                            pPanel.permsInfoLabel.setForeground(Color.red);
                            pPanel.permsInfoLabel.setText("Group A and Group B must each contain more than one experiment");
                            pPanel.useAllPermsButton.setEnabled(false);
                        } else if ((groupCount[0] + groupCount[1]) > 20) {
                            pPanel.permsInfoLabel.setForeground(Color.black);
                            pPanel.permsInfoLabel.setText("There are too many possible permutations");
                            pPanel.useAllPermsButton.setEnabled(false);
                 
                        } else {
                            pPanel.permsInfoLabel.setForeground(Color.black);
                            int numCombs = getNumCombs(groupCount[0] + groupCount[1], groupCount[0]);
                            pPanel.permsInfoLabel.setText("There are " + numCombs + " permutations");
                            pPanel.useAllPermsButton.setEnabled(true);
                        }
                    }
                });
                 
                    neitherGroupRadioButtons[i].addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        int[] groupCount = getGroupCount();
                        if ((groupCount[0] < 2)|| (groupCount[1] < 2)) {
                            pPanel.permsInfoLabel.setForeground(Color.red);
                            pPanel.permsInfoLabel.setText("Group A and Group B must each contain more than one experiment");
                            pPanel.useAllPermsButton.setEnabled(false);
                        } else if ((groupCount[0] + groupCount[1]) > 20) {
                            pPanel.permsInfoLabel.setForeground(Color.black);
                            pPanel.permsInfoLabel.setText("There are too many possible permutations");
                            pPanel.useAllPermsButton.setEnabled(false);
                 
                        } else {
                            pPanel.permsInfoLabel.setForeground(Color.black);
                            int numCombs = getNumCombs(groupCount[0] + groupCount[1], groupCount[0]);
                            pPanel.permsInfoLabel.setText("There are " + numCombs + " permutations");
                            pPanel.useAllPermsButton.setEnabled(true);
                        }
                    }
                });
                 */
                
                buildConstraints(constraints, 0, i, 1, 1, 25, 100);
                //constraints.fill = GridBagConstraints.BOTH;
                gridbag.setConstraints(expLabels[i], constraints);
                panel1.add(expLabels[i]);
                
                buildConstraints(constraints, 1, i, 1, 1, 25, 100);
                //constraints.fill = GridBagConstraints.BOTH;
                gridbag.setConstraints(groupARadioButtons[i], constraints);
                panel1.add(groupARadioButtons[i]);
                
                buildConstraints(constraints, 2, i, 1, 1, 25, 100);
                //constraints.fill = GridBagConstraints.BOTH;
                gridbag.setConstraints(groupBRadioButtons[i], constraints);
                panel1.add(groupBRadioButtons[i]);
                
                buildConstraints(constraints, 3, i, 1, 1, 25, 100);
                //constraints.fill = GridBagConstraints.BOTH;
                gridbag.setConstraints(neitherGroupRadioButtons[i], constraints);
                panel1.add(neitherGroupRadioButtons[i]);
                
                
                
            }
            
            JScrollPane scroll = new JScrollPane(panel1);
            scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
            scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
            
            buildConstraints(constraints, 0, 0, 1, 1, 100, 90);
            constraints.fill = GridBagConstraints.BOTH;
            gridbag2.setConstraints(scroll, constraints);
            this.add(scroll);
            
            JLabel label1 = new JLabel("Note: Group A and Group B  MUST each contain more than one experiment.");
            label1.setHorizontalAlignment(JLabel.CENTER);
            buildConstraints(constraints, 0, 1, 1, 1, 0, 5);
            constraints.anchor = GridBagConstraints.CENTER;
            constraints.fill = GridBagConstraints.BOTH;
            gridbag2.setConstraints(label1, constraints);
            this.add(label1);
            
            JPanel panel2 = new JPanel();
            panel2.setBackground(Color.white);
            GridBagLayout gridbag3 = new GridBagLayout();
            panel2.setLayout(gridbag3);
            
            JButton saveButton = new JButton("  Save grouping  ");
            saveButton.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED, Color.lightGray, Color.darkGray));
            saveButton.setSize(180,30);
            saveButton.setPreferredSize(new Dimension(180,30));
            JButton loadButton = new JButton("  Load grouping  ");
            loadButton.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED, Color.lightGray, Color.darkGray));
            loadButton.setPreferredSize(new Dimension(180,30));
            loadButton.setSize(180,30);
            JButton resetButton = new JButton("  Reset  ");
            resetButton.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED, Color.lightGray, Color.darkGray));
            resetButton.setSize(180,30);
            resetButton.setPreferredSize(new Dimension(180,30));
            final int finNum = exptNames.size();
            
            resetButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    for (int i = 0; i < finNum; i++) {
                        groupARadioButtons[i].setSelected(true);
                    }
                }
            });
            
            final JFileChooser fc = new JFileChooser();
            fc.setCurrentDirectory(new File("Data"));
            
            saveButton.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent evt) {
                    int returnVal = fc.showSaveDialog(GroupExperimentsPanel.this);
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
            
            loadButton.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent evt) {
                    int returnVal = fc.showOpenDialog(GroupExperimentsPanel.this);
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
                                JOptionPane.showMessageDialog(gPanel, "Incompatible file!", "Error", JOptionPane.WARNING_MESSAGE);
                            } else {
                                for (int i = 0; i < groupsVector.size(); i++) {
                                    int currentExpt = ((Integer)groupsVector.get(i)).intValue();
                                    if (currentExpt == 1) {
                                        groupARadioButtons[i].setSelected(true);
                                    } else if (currentExpt == 2) {
                                        groupBRadioButtons[i].setSelected(true);
                                    } else if (currentExpt == 3) {
                                        neitherGroupRadioButtons[i].setSelected(true);
                                    } else {
                                        for (int j = 0; j < finNum; j++) {
                                            groupARadioButtons[j].setSelected(true);
                                        }
                                        JOptionPane.showMessageDialog(gPanel, "Incompatible file!", "Error", JOptionPane.WARNING_MESSAGE);
                                        break;
                                    }
                                }
                            }
                        } catch (Exception e) {
                            JOptionPane.showMessageDialog(gPanel, "Incompatible file!", "Error", JOptionPane.WARNING_MESSAGE);
                            //e.printStackTrace();
                        }
                        
                        //this is where a real application would save the file.
                        //log.append("Saving: " + file.getName() + "." + newline);
                    } else {
                        //log.append("Save command cancelled by user." + newline);
                    }
                }
            });
            
            constraints.anchor = GridBagConstraints.CENTER;
            constraints.fill = GridBagConstraints.NONE;
            constraints.insets = new Insets(5,5,5,5);
            buildConstraints(constraints, 0, 0, 1, 1, 33, 10);
            gridbag3.setConstraints(saveButton, constraints);
            panel2.add(saveButton);
            
            buildConstraints(constraints, 1, 0, 1, 1, 33, 10);
            gridbag3.setConstraints(loadButton, constraints);
            panel2.add(loadButton);
            
            buildConstraints(constraints, 2, 0, 1, 1, 33, 10);
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
        /*
         *  Resets group selections
         */
        private void reset(){
            for (int i = 0; i < exptNames.size(); i++) {
                groupARadioButtons[i].setSelected(true);
            }
        }
    }
    
    class CensoredSurvivalPanel extends JPanel {
        ExptTimeField[] fields;
        //JLabel[] expLabels;
        //JRadioButton[] censoredRadioButtons, deadRadioButtons;
        //JCheckBox[] inAnalysisCheckBox;
        //JTextField[] timeInputField;
        //JRadioButton currentCensoredRadioButton, currentDeadRadioButton;
        //JTextField currentTimeInputField;
        //JLabel timeLabel;
        //int counter;
        CensoredSurvivalPanel(Vector exptNames) {
            //            this.setBorder(new TitledBorder(new EtchedBorder(), "Time / State Assignments"));
            this.setBorder(new TitledBorder(new EtchedBorder(), "Time / State Assignments", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), Color.black));
            setBackground(Color.white);
            JPanel panel1 = new JPanel();
            fields = new ExptTimeField[exptNames.size()];
            //expLabels = new JLabel[exptNames.size()];
            //censoredRadioButtons = new JRadioButton[exptNames.size()];
            //deadRadioButtons = new JRadioButton[exptNames.size()];
            //inAnalysisCheckBox = new  JCheckBox[exptNames.size()];
            //timeInputField = new JTextField[exptNames.size()];
            //ButtonGroup chooseGroup[] = new ButtonGroup[exptNames.size()];
            //dummyCounters = new int[exptNames.size()];
            
            GridBagLayout gridbag = new GridBagLayout();
            GridBagLayout gridbag2 = new GridBagLayout();
            GridBagConstraints constraints = new GridBagConstraints();
            this.setLayout(gridbag2);
            panel1.setLayout(gridbag);
            
            for (int i = 0; i < exptNames.size(); i++) {
                String s1 = (String)(exptNames.get(i));
                fields[i] = new ExptTimeField(s1);
                /*
                expLabels[i] = new JLabel(s1);
                timeInputField[i] = new JTextField(7);
                chooseGroup[i] = new ButtonGroup();
                censoredRadioButtons[i] = new JRadioButton("Censored", true);
                chooseGroup[i].add(censoredRadioButtons[i]);
                deadRadioButtons[i] = new JRadioButton("Dead", false);
                chooseGroup[i].add(deadRadioButtons[i]);
                inAnalysisCheckBox[i] = new JCheckBox("", true);
                 
                timeLabel = new JLabel("Time: ");
                 */
                
                buildConstraints(constraints, 0, i, 1, 1, 20, 100);
                //constraints.fill = GridBagConstraints.BOTH;
                gridbag.setConstraints(fields[i].inAnalysisCheckBox, constraints);
                
                //addAnalysisCheckBoxListener(inAnalysisCheckBox[i], expLabels[i], timeInputField[i], censoredRadioButtons[i], deadRadioButtons[i], timeLabel);
                //dummyCounters[i] = i;
                /*
                inAnalysisCheckBox[i].addItemListener(new ItemListener() {
                    public void itemStateChanged(ItemEvent e) {
                        if (e.getStateChange() == ItemEvent.DESELECTED) {
                            currentExpLabel.setEnabled(false);
                            currentTimeInputField.setBackground(Color.darkGray);
                            currentTimeInputField.setEnabled(false);
                            currentCensoredRadioButton.setEnabled(false);
                            currentDeadRadioButton.setEnabled(false);
                 
                        }
                 
                        if (e.getStateChange() == ItemEvent.SELECTED) {
                            currentExpLabel.setEnabled(true);
                            currentTimeInputField.setBackground(Color.white);
                            currentTimeInputField.setEnabled(true);
                            currentCensoredRadioButton.setEnabled(true);
                            currentDeadRadioButton.setEnabled(true);
                        }
                 
                    }
                });
                 */
                panel1.add(fields[i].inAnalysisCheckBox);
                
                buildConstraints(constraints, 1, i, 1, 1, 20, 100);
                //constraints.fill = GridBagConstraints.BOTH;
                gridbag.setConstraints(fields[i].expLabel, constraints);
                panel1.add(fields[i].expLabel);
                
                
                buildConstraints(constraints, 2, i, 1, 1, 20, 100);
                //constraints.fill = GridBagConstraints.BOTH;
                constraints.anchor = GridBagConstraints.EAST;
                gridbag.setConstraints(fields[i].timeLabel, constraints);
                panel1.add(fields[i].timeLabel);
                
                buildConstraints(constraints, 3, i, 1, 1, 20, 100);
                //constraints.fill = GridBagConstraints.BOTH;
                constraints.anchor = GridBagConstraints.WEST;
                gridbag.setConstraints(fields[i].timeInputField, constraints);
                panel1.add(fields[i].timeInputField);
                
                constraints.anchor = GridBagConstraints.CENTER;
                buildConstraints(constraints, 4, i, 1, 1, 20, 100);
                //constraints.fill = GridBagConstraints.BOTH;
                gridbag.setConstraints(fields[i].censoredRadioButton, constraints);
                panel1.add(fields[i].censoredRadioButton);
                
                buildConstraints(constraints, 5, i, 1, 1, 20, 100);
                //constraints.fill = GridBagConstraints.BOTH;
                gridbag.setConstraints(fields[i].deadRadioButton, constraints);
                panel1.add(fields[i].deadRadioButton);
                
                
                
            }
            
            JScrollPane scroll = new JScrollPane(panel1);
            scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
            scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
            
            buildConstraints(constraints, 0, 0, 1, 1, 100, 90);
            constraints.fill = GridBagConstraints.BOTH;
            gridbag2.setConstraints(scroll, constraints);
            this.add(scroll);
            /*
            JLabel label1 = new JLabel("Note: Group A and Group B  MUST each contain more than one experiment.");
            buildConstraints(constraints, 0, 1, 1, 1, 0, 5);
            constraints.anchor = GridBagConstraints.EAST;
            //constraints.fill = GridBagConstraints.BOTH;
            gridbag2.setConstraints(label1, constraints);
            this.add(label1);
             */
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
                        fields[i].inAnalysisCheckBox.setSelected(true);
                        fields[i].timeInputField.setText("0.0");
                        fields[i].censoredRadioButton.setSelected(true);
                    }
                }
            });
            
            final JFileChooser fc = new JFileChooser();
            fc.setCurrentDirectory(new File("Data"));
            
            saveButton.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent evt) {
                    int returnVal = fc.showSaveDialog(CensoredSurvivalPanel.this);
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        File file = fc.getSelectedFile();
                        try {
                            PrintWriter out = new PrintWriter(new FileOutputStream(file));
                            //int[] groupAssgn = getGroupAssignments();
                            for (int i = 0; i < finNum; i++) {
                                if (fields[i].inAnalysisCheckBox.isSelected()) {
                                    out.print(1);
                                    out.print("\t");
                                    if (fields[i].timeInputField.getText() == "") {
                                        out.print("0.0");
                                    } else {
                                        out.print(fields[i].timeInputField.getText());
                                    }
                                    out.print("\t");
                                    if (fields[i].censoredRadioButton.isSelected()) {
                                        out.print(1);
                                    } else {
                                        out.print(0);
                                    }
                                    //DONE UP TO HERE 3/20/03
                                } else {
                                    out.print(0);
                                }
                                out.println();
                            }
                            //out.println();
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
                    int returnVal = fc.showOpenDialog(CensoredSurvivalPanel.this);
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        try {
                            FileReader file = new FileReader(fc.getSelectedFile());
                            BufferedReader buff = new BufferedReader(file);
                            String currentLine;
                            Vector inputVector = new Vector();
                            //System.out.println(line);
                            //StringSplitter st = new StringSplitter('\t');
                            while((currentLine = buff.readLine()) != null) {
                                inputVector.add(currentLine);
                            }
                            buff.close();
                            for (int i = 0; i < inputVector.size(); i++) {
                                double[] currentSettings = getCurrentSettings((String)inputVector.get(i));
                                if (currentSettings[0] == 0) {
                                    fields[i].inAnalysisCheckBox.setSelected(false);
                                } else if (currentSettings[0] == 1) {
                                    fields[i].inAnalysisCheckBox.setSelected(true);
                                    fields[i].timeInputField.setText("" + currentSettings[1]);
                                    if (currentSettings[2] == 0) {
                                        fields[i].deadRadioButton.setSelected(true);
                                    } else if (currentSettings[2] == 1) {
                                        fields[i].censoredRadioButton.setSelected(true);
                                    } else {
                                        for (int k = 0; k < finNum; k++) {
                                            fields[k].inAnalysisCheckBox.setSelected(false);
                                        }
                                        JOptionPane.showMessageDialog(gPanel, "Incompatible file!", "Error", JOptionPane.WARNING_MESSAGE);
                                        break;
                                    }
                                } else { // if currentSettings[0] != 0 or 1
                                    for (int k = 0; k < finNum; k++) {
                                        fields[k].inAnalysisCheckBox.setSelected(false);
                                    }
                                    JOptionPane.showMessageDialog(gPanel, "Incompatible file!", "Error", JOptionPane.WARNING_MESSAGE);
                                    break;
                                }
                            }
                            
                            
                            
                            /*
                            st.init(line);
                            Vector groupsVector = new Vector();
                            while (st.hasMoreTokens()) {
                                String current = st.nextToken();
                                groupsVector.add(new Integer(current));
                                //System.out.print(current);
                            }
                             
                            int[] groupAssgn = getGroupAssignments();
                            if (groupsVector.size() != groupAssgn.length) {
                                JOptionPane.showMessageDialog(gPanel, "Incompatible file!", "Error", JOptionPane.WARNING_MESSAGE);
                            } else {
                                for (int i = 0; i < groupsVector.size(); i++) {
                                    int currentExpt = ((Integer)groupsVector.get(i)).intValue();
                                    if (currentExpt == 1) {
                                        groupARadioButtons[i].setSelected(true);
                                    } else if (currentExpt == 2) {
                                        groupBRadioButtons[i].setSelected(true);
                                    } else if (currentExpt == 3) {
                                        neitherGroupRadioButtons[i].setSelected(true);
                                    } else {
                                        for (int j = 0; j < finNum; j++) {
                                            groupARadioButtons[j].setSelected(true);
                                        }
                                        JOptionPane.showMessageDialog(gPanel, "Incompatible file!", "Error", JOptionPane.WARNING_MESSAGE);
                                        break;
                                    }
                                }
                            }*/
                        } catch (Exception e) {
                            for (int k = 0; k < finNum; k++) {
                                fields[k].inAnalysisCheckBox.setSelected(false);
                            }
                            JOptionPane.showMessageDialog(gPanel, "Incompatible file!", "Error", JOptionPane.WARNING_MESSAGE);
                            //e.printStackTrace();
                        }
                        
                        //this is where a real application would save the file.
                        //log.append("Saving: " + file.getName() + "." + newline);
                    } else {
                        //log.append("Save command cancelled by user." + newline);
                    }
                }
            });
            
            
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
            buildConstraints(constraints, 0, 1, 1, 1, 0, 10);
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
        
        /*
         *  Resets valuse to default values
         */
        private void reset(){
            for (int i = 0; i < exptNames.size(); i++) {
                fields[i].inAnalysisCheckBox.setSelected(true);
                fields[i].timeInputField.setText("0.0");
                fields[i].censoredRadioButton.setSelected(true);
            }
        }
        
        /*
        private void addAnalysisCheckBoxListener(JCheckBox inAnalysisCheckBox, JLabel expLabel, JTextField timeInputField, JRadioButton censoredRadioButton, JRadioButton deadRadioButton, JLabel timeLabel) {
            inAnalysisCheckBox.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    if (e.getStateChange() == ItemEvent.DESELECTED) {
                        expLabel.setEnabled(false);
                        timeLabel.setEnabled(false);
                        timeInputField.setBackground(Color.darkGray);
                        timeInputField.setEnabled(false);
                        censoredRadioButton.setEnabled(false);
                        deadRadioButton.setEnabled(false);
         
                    }
         
                    if (e.getStateChange() == ItemEvent.SELECTED) {
                        expLabel.setEnabled(true);
                        timeLabel.setEnabled(false);
                        timeInputField.setBackground(Color.white);
                        timeInputField.setEnabled(true);
                        censoredRadioButton.setEnabled(true);
                        deadRadioButton.setEnabled(true);
                    }
         
                }
            });
        }
         */
        
        private class ExptTimeField {
            JCheckBox inAnalysisCheckBox;
            JLabel expLabel, timeLabel;
            JTextField timeInputField;
            JRadioButton censoredRadioButton, deadRadioButton;
            public ExptTimeField(String exptName) {
                inAnalysisCheckBox = new JCheckBox("", true);
                expLabel = new JLabel(exptName);
                timeLabel = new JLabel("Time: ");
                timeInputField = new JTextField("0.0", 7);
                censoredRadioButton = new JRadioButton("Censored", true);
                deadRadioButton = new JRadioButton("Dead", false);
                ButtonGroup chooseGroup = new ButtonGroup();
                chooseGroup.add(censoredRadioButton);
                chooseGroup.add(deadRadioButton);
                inAnalysisCheckBox.addItemListener(new ItemListener() {
                    public void itemStateChanged(ItemEvent e) {
                        if (e.getStateChange() == ItemEvent.DESELECTED) {
                            expLabel.setEnabled(false);
                            timeLabel.setEnabled(false);
                            timeInputField.setText("");
                            timeInputField.setBackground(Color.darkGray);
                            timeInputField.setEnabled(false);
                            censoredRadioButton.setEnabled(false);
                            deadRadioButton.setEnabled(false);
                            
                        }
                        
                        if (e.getStateChange() == ItemEvent.SELECTED) {
                            expLabel.setEnabled(true);
                            timeLabel.setEnabled(true);
                            timeInputField.setText("0.0");
                            timeInputField.setBackground(Color.white);
                            timeInputField.setEnabled(true);
                            censoredRadioButton.setEnabled(true);
                            deadRadioButton.setEnabled(true);
                        }
                        
                    }
                });
            }
            
        }
        
        private double[] getCurrentSettings(String currentLine) {
            double[] currentSettings;
            Vector currentVector = new Vector();
            StringSplitter st = new StringSplitter('\t');
            st.init(currentLine);
            
            while (st.hasMoreTokens()) {
                String current = st.nextToken();
                currentVector.add(new Double(current));
            }
            
            currentSettings = new double[currentVector.size()];
            for (int i = 0; i < currentSettings.length; i++) {
                currentSettings[i] = ((Double)currentVector.get(i)).doubleValue();
            }
            
            return currentSettings;
            
        }
        
        
    }
    
    class OneClassPanel extends JPanel {
        JTextField meanField;
        JCheckBox[] includeExpts;
        JButton saveButton, loadButton, resetButton;
        OneClassPanel() {
            this.setBackground(Color.white);
            JLabel meanLabel = new JLabel("Enter the mean value to be tested against: ");
            meanField = new JTextField("0", 7);
            includeExpts = new JCheckBox[exptNames.size()];
            
            GridBagLayout gridbag = new GridBagLayout();
            GridBagConstraints constraints = new GridBagConstraints();
            this.setLayout(gridbag);
            
            JPanel exptPanel = new JPanel();
            GridBagLayout grid1 = new GridBagLayout();
            exptPanel.setLayout(grid1);
            
            //System.out.println("exptNames.size()" + exptNames.size());
            
            for (int i = 0; i < exptNames.size(); i++) {
                //JLabel expLabel = new JLabel((String)(exptNames.get(i)));
                includeExpts[i] = new JCheckBox((String)(exptNames.get(i)), true);
                buildConstraints(constraints, 0, i, 1, 1, 100, 100);
                grid1.setConstraints(includeExpts[i], constraints);
                exptPanel.add(includeExpts[i]);
            }
            
            JScrollPane scroll = new JScrollPane(exptPanel);
            scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
            //scroll.add(exptPanel);
            
            JPanel enterMeanPanel = new JPanel();
            GridBagLayout grid2 = new GridBagLayout();
            enterMeanPanel.setLayout(grid2);            
            /*
            constraints.fill = GridBagConstraints.BOTH;
            buildConstraints(constraints, 0, 0, 1, 1, 50, 100);
            gridbag.setConstraints(scroll, constraints);
            this.add(scroll); 
             */           
            
            constraints.fill = GridBagConstraints.NONE;
            buildConstraints(constraints, 0, 0, 1, 1, 50, 100);
            constraints.anchor = GridBagConstraints.EAST;
            grid2.setConstraints(meanLabel, constraints);
            enterMeanPanel.add(meanLabel);
            
            buildConstraints(constraints, 1, 0, 1, 1, 50, 0);
            constraints.anchor = GridBagConstraints.WEST;
            grid2.setConstraints(meanField, constraints);
            enterMeanPanel.add(meanField);    
            
            JScrollPane scroll2 = new JScrollPane(enterMeanPanel);
            
            JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scroll, scroll2);
            split.setOneTouchExpandable(true);
            split.setDividerLocation(150);
           
            constraints.fill = GridBagConstraints.BOTH;
            buildConstraints(constraints, 0, 0, 1, 1, 100, 80);
            gridbag.setConstraints(split, constraints);
            this.add(split);  
            
            constraints.fill = GridBagConstraints.NONE;
            constraints.anchor = GridBagConstraints.CENTER;
            
            JPanel lsrPanel = new JPanel();
            loadButton = new JButton("Load settings");
            saveButton = new JButton("Save settings");
            resetButton = new JButton("Reset");
            
            resetButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    for (int i = 0; i < includeExpts.length; i++) {
                        includeExpts[i].setSelected(true);
                    }
                }
            });
            
            final JFileChooser fc = new JFileChooser();
            fc.setCurrentDirectory(new File("Data"));  
            
            saveButton.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent evt) {
                    int returnVal = fc.showSaveDialog(OneClassPanel.this);  
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        File file = fc.getSelectedFile();
                        try {
                            PrintWriter out = new PrintWriter(new FileOutputStream(file));
                            for (int i = 0; i < includeExpts.length; i++) {
                                if (includeExpts[i].isSelected()) {
                                    out.print(1);
                                } else {
                                    out.print(0);
                                }
                                if (i < includeExpts.length - 1) {
                                    out.print("\t");
                                }
                            }
                            out.println();
                            out.flush();
                            out.close();                            
                        } catch (Exception e) {
                        }
                    } else {
                    }
                }
            });
            
            loadButton.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent evt) {
                    int returnVal = fc.showOpenDialog(OneClassPanel.this);
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        try {
                            FileReader file = new FileReader(fc.getSelectedFile());
                            BufferedReader buff = new BufferedReader(file);     
                            String line = buff.readLine();
                            //System.out.println(line);
                            StringSplitter st = new StringSplitter('\t');
                            st.init(line);  
                            Vector includeExptsVector = new Vector();
                            while (st.hasMoreTokens()) {
                                String current = st.nextToken();
                                includeExptsVector.add(new Integer(current));
                                //System.out.print(current);
                            }
                            buff.close();
                            if (includeExptsVector.size() != includeExpts.length) {
                                JOptionPane.showMessageDialog(gPanel, "Incompatible file!", "Error", JOptionPane.WARNING_MESSAGE);
                            } else {
                                for (int i = 0; i < includeExpts.length; i++) {
                                    int currentState = ((Integer)(includeExptsVector.get(i))).intValue();
                                    if (currentState == 0) {
                                        includeExpts[i].setSelected(false);
                                    } else if (currentState == 1) {
                                        includeExpts[i].setSelected(true);
                                    }else {
                                        for (int j = 0; j < includeExpts.length; j++) {
                                            includeExpts[j].setSelected(true);
                                        }
                                        JOptionPane.showMessageDialog(gPanel, "Incompatible file!", "Error", JOptionPane.WARNING_MESSAGE);
                                        break;                                        
                                    }
                                }
                            }
                        } catch (Exception e) {
                            JOptionPane.showMessageDialog(gPanel, "Incompatible file!", "Error", JOptionPane.WARNING_MESSAGE); 
                            
                        }
                    } else {
                    }
                }
            });
            
            
            GridBagLayout grid3 = new GridBagLayout();
            lsrPanel.setLayout(grid3);
            
            buildConstraints(constraints, 0, 0, 1, 1, 33, 100);
            grid3.setConstraints(saveButton, constraints);
            lsrPanel.add(saveButton);
            
            buildConstraints(constraints, 1, 0, 1, 1, 33, 0);
            grid3.setConstraints(loadButton, constraints);
            lsrPanel.add(loadButton);            
            
            buildConstraints(constraints, 2, 0, 1, 1, 33, 0);
            grid3.setConstraints(resetButton, constraints);
            lsrPanel.add(resetButton);            
            
            //constraints.fill = GridBagConstraints.BOTH;
            buildConstraints(constraints, 0, 1, 1, 1, 0, 20);
            gridbag.setConstraints(lsrPanel, constraints);
            this.add(lsrPanel);            
        }
        
        
        public void reset() {
            for (int i = 0; i < includeExpts.length; i++) {
                includeExpts[i].setSelected(true);
            }
            meanField.setText("0");
        }
        

    }    
    
    class TwoClassPairedMainPanel extends JPanel {
        TwoClassPairedPanel tcpPanel;
        JButton saveButton, resetButton, loadButton;
        GridBagConstraints constraints;
        GridBagLayout gridbag;  
        
        public TwoClassPairedMainPanel() {
            tcpPanel = new TwoClassPairedPanel();
            JPanel bottomPanel = new JPanel();
            bottomPanel.setBackground(Color.white);
            constraints = new GridBagConstraints();
            gridbag = new GridBagLayout();   
            this.setLayout(gridbag);
            
            buildConstraints(constraints, 0, 0, 1, 1, 100, 90);
            constraints.fill = GridBagConstraints.BOTH;
            gridbag.setConstraints(tcpPanel, constraints);
            this.add(tcpPanel);
            
            GridBagLayout grid1 = new GridBagLayout();
            bottomPanel.setLayout(grid1);
            
            saveButton = new JButton("Save pairings");
            
            final JFileChooser fc = new JFileChooser();
            fc.setCurrentDirectory(new File("Data"));
            
            saveButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    int returnVal = fc.showSaveDialog(TwoClassPairedMainPanel.this); 
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        File file = fc.getSelectedFile(); 
                        try {
                            PrintWriter out = new PrintWriter(new FileOutputStream(file));
                            for (int i = 0; i < tcpPanel.pairedAExpts.size(); i++) {
                                int currentA = ((Integer)(tcpPanel.pairedAExpts.get(i))).intValue();
                                int currentB = ((Integer)(tcpPanel.pairedBExpts.get(i))).intValue();
                                out.print(currentA);
                                out.print("\t");
                                out.print(currentB);
                                out.print("\t");
                                out.println();
                            }
                            out.flush();
                            out.close();
                        } catch (Exception e) {
                        }
                    } else {
                    }
                }
            });
            constraints.fill = GridBagConstraints.NONE;
            buildConstraints(constraints, 0, 0, 1, 1, 33, 100);
            grid1.setConstraints(saveButton, constraints);
            bottomPanel.add(saveButton);       
            
            loadButton = new JButton("Load pairings");
            
            loadButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    tcpPanel.reset();
                    int returnVal = fc.showOpenDialog(TwoClassPairedMainPanel.this);  
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        try {
                            FileReader file = new FileReader(fc.getSelectedFile());
                            BufferedReader buff = new BufferedReader(file);
                            String currentLine;  
                            while((currentLine = buff.readLine()) != null) {
                                StringSplitter st = new StringSplitter('\t');
                                st.init(currentLine);
                                
                                for (int i = 0; i < 2; i++) {
                                    String s = st.nextToken();
                                    if (i == 0) {
                                        tcpPanel.pairedAExpts.add(new Integer(s));
                                    } else if (i == 1) {
                                        tcpPanel.pairedBExpts.add(new Integer(s));
                                    }
                                }
                                
                            }
                            buff.close();
                            
                            for (int i = 0; i < tcpPanel.pairedAExpts.size(); i++) {
                                int currA = ((Integer)(tcpPanel.pairedAExpts.get(i))).intValue();
                                int currB = ((Integer)(tcpPanel.pairedBExpts.get(i))).intValue();
                                String currPair = "A: " + (String)(exptNames.get(currA)) + " - B: " + (String)(exptNames.get(currB));
                                tcpPanel.exptButtons[currA].setEnabled(false);
                                tcpPanel.exptButtons[currB].setEnabled(false); 
                                tcpPanel.pairedListModel.addElement(currPair);
                                
                            }
                            if (tcpPanel.pairedAExpts.size() > 0) {
                                tcpPanel.removeABPairButton.setEnabled(true);
                                tcpPanel.pairedExptsList.setSelectedIndex(tcpPanel.pairedListModel.size() - 1);
                            }
                            
                        } catch (Exception exc) {
                            JOptionPane.showMessageDialog(tcpPanel, "Incompatible file!", "Error", JOptionPane.WARNING_MESSAGE);                            
                        }
                    } else {
                    }
                }
            });
            
            buildConstraints(constraints, 1, 0, 1, 1, 33, 100);
            grid1.setConstraints(loadButton, constraints);
            bottomPanel.add(loadButton);     
            
            resetButton = new JButton("Reset");
            
            resetButton.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent evt) {
                    tcpPanel.reset();
                }
            });
            
            buildConstraints(constraints, 2, 0, 1, 1, 34, 100);
            grid1.setConstraints(resetButton, constraints);
            bottomPanel.add(resetButton);    
            
            buildConstraints(constraints, 0, 1, 1, 1, 0, 10);
            //constraints.fill = GridBagConstraints.BOTH;
            gridbag.setConstraints(bottomPanel, constraints);
            this.add(bottomPanel);             
        }
    }
    
    class TwoClassPairedPanel extends JPanel {
        ExperimentButton[] exptButtons;
        GridBagConstraints constraints;
        GridBagLayout gridbag;
        JTextField currentATextField, currentBTextField;
        JButton removeCurrentAButton, removeCurrentBButton, loadABPairButton, removeABPairButton;
        PairedExperimentsPanel pairPanel;
        JList pairedExptsList;
        DefaultListModel pairedListModel;
        boolean currentAFilled, currentBFilled;
        int currentAExpt, currentBExpt;
        Vector pairedAExpts, pairedBExpts;
        public TwoClassPairedPanel() {
            currentAExpt = -1;
            currentBExpt = -1;
            currentAFilled = false;
            currentBFilled = false;
            pairedAExpts = new Vector();
            pairedBExpts = new Vector();
            constraints = new GridBagConstraints();
            gridbag = new GridBagLayout();
            //this.setBackground(Color.white);
            this.setLayout(gridbag);  
            /*
            currentATextField = new JTextField("", 10);
            currentBTextField = new JTextField("", 10);
            currentATextField.setBackground(Color.white);
            currentBTextField.setBackground(Color.white);
            currentATextField.setEditable(false);
            currentBTextField.setEditable(false);   
             */      
            
            pairedListModel = new DefaultListModel();
            pairedExptsList = new JList(pairedListModel);
            
            JPanel exptNamesPanel = new JPanel();
            GridBagLayout grid1 = new GridBagLayout();
            exptNamesPanel.setLayout(grid1);
            exptButtons = new ExperimentButton[exptNames.size()];
            
            int maxWidth = 0;
            int maxNameLength = 0;
            
            for (int i = 0; i < exptNames.size(); i++) {
                //String s = (String)(exptNames.get(i));
                exptButtons[i] = new ExperimentButton(i);
                
                if (exptButtons[i].getPreferredSize().getWidth() > maxWidth) {
                    maxWidth = (int)Math.ceil(exptButtons[i].getPreferredSize().getWidth());
                }
                
                String s = (String)(exptNames.get(i));
                int currentNameLength = s.length();
                
                if (currentNameLength > maxNameLength) {
                    maxNameLength = currentNameLength;
                }
                
                buildConstraints(constraints, 0, i, 1, 1, 100, 100);
                grid1.setConstraints(exptButtons[i], constraints);
                exptNamesPanel.add(exptButtons[i]);
            }
            
            currentATextField = new JTextField("", maxNameLength + 2);
            currentBTextField = new JTextField("", maxNameLength + 2);
            //currentATextField.setSize(maxWidth + 5, 80);
            //currentBTextField.setSize(maxWidth + 5, 80);
            //currentATextField.setPreferredSize(new Dimension(maxWidth + 5, 80));
            //currentBTextField.setPreferredSize(new Dimension(maxWidth + 5, 80));
            
            currentATextField.setBackground(Color.white);
            currentBTextField.setBackground(Color.white);
            currentATextField.setEditable(false);
            currentBTextField.setEditable(false);   
            
            JScrollPane scroll = new JScrollPane(exptNamesPanel);
            scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
            scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);            
            
            buildConstraints(constraints, 0, 0, 1, 1, 40, 100);
            constraints.fill =GridBagConstraints.BOTH;
            gridbag.setConstraints(scroll, constraints);
            this.add(scroll);
            
            constraints.fill = GridBagConstraints.NONE;
            
            JPanel currentSelectionPanel = new JPanel();
            GridBagLayout grid2 = new GridBagLayout();
            currentSelectionPanel.setLayout(grid2);
            removeCurrentAButton = new JButton("< Remove A");
            removeCurrentBButton = new JButton("< Remove B");
            loadABPairButton = new JButton("   Load Pair >>   ");
            removeABPairButton = new JButton("<< Remove Pair");
            removeCurrentAButton.setEnabled(false);
            removeCurrentBButton.setEnabled(false);
            loadABPairButton.setEnabled(false); 
            removeABPairButton.setEnabled(false);
            removeCurrentAButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    exptButtons[currentAExpt].setEnabled(true);
                    currentAExpt = -1;
                    currentATextField.setText("");
                    currentAFilled = false;
                    removeCurrentAButton.setEnabled(false);
                    loadABPairButton.setEnabled(false);
                }
            });
            
            removeCurrentBButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    exptButtons[currentBExpt].setEnabled(true);
                    currentBExpt = -1;
                    currentBTextField.setText("");
                    currentBFilled = false;
                    removeCurrentBButton.setEnabled(false);
                    loadABPairButton.setEnabled(false);                    
                }
            });     
            
            loadABPairButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    String currentPair = "A: " + (String)(exptNames.get(currentAExpt)) + " - B: " + (String)(exptNames.get(currentBExpt));
                    pairedListModel.addElement(currentPair);
                    pairedAExpts.add(new Integer(currentAExpt));
                    pairedBExpts.add(new Integer(currentBExpt));
                    currentAExpt = -1;
                    currentBExpt = -1;
                    currentATextField.setText("");
                    currentBTextField.setText("");
                    currentAFilled = false;
                    currentBFilled = false;
                    removeCurrentAButton.setEnabled(false);
                    removeCurrentBButton.setEnabled(false);
                    loadABPairButton.setEnabled(false); 
                    removeABPairButton.setEnabled(true);
                    pairedExptsList.setSelectedIndex(pairedListModel.size() - 1);
                }
            });
            
            removeABPairButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    int index = pairedExptsList.getSelectedIndex();
                    pairedListModel.removeElementAt(index);
                    int removedAIndex = ((Integer)(pairedAExpts.remove(index))).intValue();
                    int removedBIndex = ((Integer)(pairedBExpts.remove(index))).intValue();
                    exptButtons[removedAIndex].setEnabled(true);
                    exptButtons[removedBIndex].setEnabled(true);
                    if (pairedListModel.isEmpty()) {
                        removeABPairButton.setEnabled(false);
                    } else {
                        pairedExptsList.setSelectedIndex(pairedListModel.size() - 1);
                    }
                }
            });

            JScrollPane currentAScroll = new JScrollPane(currentATextField);
            currentAScroll.setMinimumSize(new Dimension(90, 50));
            JScrollPane currentBScroll = new JScrollPane(currentBTextField);
            currentBScroll.setMinimumSize(new Dimension(90, 50));
            
            currentAScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
            currentAScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);   
            
            currentBScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
            currentBScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);            
            
            buildConstraints(constraints, 0, 0, 1, 1, 20, 50);
            grid2.setConstraints(removeCurrentAButton, constraints);
            currentSelectionPanel.add(removeCurrentAButton);
            
            JLabel aLabel = new JLabel(" Current A: ");
            buildConstraints(constraints, 1, 0, 1, 1, 20, 0);
            grid2.setConstraints(aLabel, constraints);
            currentSelectionPanel.add(aLabel);    
            
            buildConstraints(constraints, 2, 0, 1, 1, 60, 0);
            constraints.fill = GridBagConstraints.BOTH;
            //constraints.ipady = 100;
            grid2.setConstraints(currentAScroll, constraints);
            currentSelectionPanel.add(currentAScroll);   
            
            //constraints.ipady = 0;
            constraints.fill = GridBagConstraints.NONE;
            
            buildConstraints(constraints, 0, 1, 1, 1, 20, 50);
            grid2.setConstraints(removeCurrentBButton, constraints);
            currentSelectionPanel.add(removeCurrentBButton);   
            
            JLabel bLabel = new JLabel("Current B: ");
            buildConstraints(constraints, 1, 1, 1, 1, 20, 0);
            grid2.setConstraints(bLabel, constraints);
            currentSelectionPanel.add(bLabel);  
            
            buildConstraints(constraints, 2, 1, 1, 1, 60, 0);
            constraints.fill = GridBagConstraints.BOTH;
            //constraints.ipady = 100;
            grid2.setConstraints(currentBScroll, constraints);
            currentSelectionPanel.add(currentBScroll);   
            
            //constraints.ipady = 0;
            constraints.fill = GridBagConstraints.NONE;
            
            buildConstraints(constraints, 1, 0, 1, 1, 10, 0);
            //constraints.fill = GridBagConstraints.HORIZONTAL;
            //constraints.ipadx = 200;
            gridbag.setConstraints(currentSelectionPanel, constraints);
            this.add(currentSelectionPanel);   
            
            constraints.fill = GridBagConstraints.NONE;
            //constraints.ipadx = 0;
            
            JPanel pairButtonsPanel = new JPanel();
            GridBagLayout grid3 = new GridBagLayout();
            pairButtonsPanel.setLayout(grid3);

            buildConstraints(constraints, 0, 0, 1, 1, 100, 50);
            grid3.setConstraints(loadABPairButton, constraints);
            pairButtonsPanel.add(loadABPairButton);

            buildConstraints(constraints, 0, 1, 1, 1, 0, 50);
            grid3.setConstraints(removeABPairButton, constraints);
            pairButtonsPanel.add(removeABPairButton);            
            
            buildConstraints(constraints, 2, 0, 1, 1, 5, 0);
            gridbag.setConstraints(pairButtonsPanel, constraints);
            this.add(pairButtonsPanel);  
            
            //pairPanel = new PairedExperimentsPanel();
            buildConstraints(constraints, 3, 0, 1, 1, 45, 0);
            constraints.fill = GridBagConstraints.BOTH;
            JScrollPane pairScroll = new JScrollPane(pairedExptsList);
            pairScroll.setBorder(new TitledBorder("Paired Experiments"));
            gridbag.setConstraints(pairScroll, constraints);
            this.add(pairScroll);              
            
        }
        
        public void reset() {
            for (int i = 0; i < exptButtons.length; i++) {
                exptButtons[i].setEnabled(true);
                currentATextField.setText("");
                currentBTextField.setText("");
                removeCurrentAButton.setEnabled(false);
                removeCurrentBButton.setEnabled(false);
                loadABPairButton.setEnabled(false);
                removeABPairButton.setEnabled(false);
                pairedListModel.clear();
                currentAFilled = false;
                currentBFilled = false;
                currentAExpt = -1;
                currentBExpt = -1;
                pairedAExpts.clear();
                pairedBExpts.clear();
            }
        }
        
        class ExperimentButton extends JButton {
            String s;
            int index;
            public ExperimentButton(int i) {
                this.index = i;
                s = (String)(exptNames.get(i));
                this.setText(s);
                this.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        if ((currentAFilled)&&(currentBFilled)) {
                            JOptionPane.showMessageDialog(null, "Clear at least one current field first!", "Error", JOptionPane.ERROR_MESSAGE);
                        } else if (!currentAFilled) {
                            currentAExpt = index;
                            currentATextField.setText(s);
                            currentAFilled = true;
                            ExperimentButton.this.setEnabled(false);
                            removeCurrentAButton.setEnabled(true);
                        } else if (!currentBFilled) {
                            currentBExpt = index;
                            currentBTextField.setText(s);
                            currentBFilled = true;
                            ExperimentButton.this.setEnabled(false);
                            removeCurrentBButton.setEnabled(true);
                        }
                        
                        if ((currentAFilled) && (currentBFilled)) {
                            loadABPairButton.setEnabled(true);
                        } else {
                            loadABPairButton.setEnabled(false);
                        }
                    }
                });
            }
        }
        
        class PairedExperimentsPanel extends JPanel {
            public PairedExperimentsPanel() {
                //this.setBorder(new TitledBorder("Paired Experiments"));
            }
        }
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
                            JOptionPane.showMessageDialog(null, "Please enter a positive integer >= 2!", "Error", JOptionPane.ERROR_MESSAGE);
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
                        JOptionPane.showMessageDialog(null, "Please enter a positive integer >= 2!", "Error", JOptionPane.ERROR_MESSAGE);
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
                //super(parentFrame, "Multiclass design: number of groups", modality);
                //setBounds(0, 0, 350, 150);
                //setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
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
                /*
                okButton.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent evt) {
                        okPressed = true;
                        //dispose();
                    }
                });
                 */
                this.add(okButton);
                /*
                cancelButton = new JButton("Cancel");
                buildConstraints(constraints, 1, 1, 1, 1, 0, 0);
                gridbag.setConstraints(cancelButton, constraints);
                cancelButton.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent evt) {
                        okPressed = false;
                        dispose();
                    }
                });
                pane.add(cancelButton);
                 */
                
                //setContentPane(pane);
                
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
                this.setBorder(new TitledBorder(new EtchedBorder(), "Group Assignments"));
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
                        /*
                        groupARadioButtons[i] = new JRadioButton("Group A", true);
                        chooseGroup[i].add(groupARadioButtons[i]);
                        groupBRadioButtons[i] = new JRadioButton("Group B", false);
                        chooseGroup[i].add(groupBRadioButtons[i]);
                         */
                    notInGroupRadioButtons[i] = new JRadioButton("Not in groups", false);
                    chooseGroup[i].add(notInGroupRadioButtons[i]);
                    
                /*
                groupARadioButtons[i].addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        int[] groupCount = getGroupCount();
                        if ((groupCount[0] < 2)|| (groupCount[1] < 2)) {
                            pPanel.permsInfoLabel.setForeground(Color.red);
                            pPanel.permsInfoLabel.setText("Group A and Group B must each contain more than one experiment");
                            pPanel.useAllPermsButton.setEnabled(false);
                        } else if ((groupCount[0] + groupCount[1]) > 20) {
                            pPanel.permsInfoLabel.setForeground(Color.black);
                            pPanel.permsInfoLabel.setText("There are too many possible permutations");
                            pPanel.useAllPermsButton.setEnabled(false);
                 
                        } else {
                            pPanel.permsInfoLabel.setForeground(Color.black);
                            int numCombs = getNumCombs(groupCount[0] + groupCount[1], groupCount[0]);
                            pPanel.permsInfoLabel.setText("There are " + numCombs + " permutations");
                            pPanel.useAllPermsButton.setEnabled(true);
                        }
                    }
                });
                 
                groupBRadioButtons[i].addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        int[] groupCount = getGroupCount();
                        if ((groupCount[0] < 2)|| (groupCount[1] < 2)) {
                            pPanel.permsInfoLabel.setForeground(Color.red);
                            pPanel.permsInfoLabel.setText("Group A and Group B must each contain more than one experiment");
                            pPanel.useAllPermsButton.setEnabled(false);
                        } else if ((groupCount[0] + groupCount[1]) > 20) {
                            pPanel.permsInfoLabel.setForeground(Color.black);
                            pPanel.permsInfoLabel.setText("There are too many possible permutations");
                            pPanel.useAllPermsButton.setEnabled(false);
                 
                        } else {
                            pPanel.permsInfoLabel.setForeground(Color.black);
                            int numCombs = getNumCombs(groupCount[0] + groupCount[1], groupCount[0]);
                            pPanel.permsInfoLabel.setText("There are " + numCombs + " permutations");
                            pPanel.useAllPermsButton.setEnabled(true);
                        }
                    }
                });
                 
                    neitherGroupRadioButtons[i].addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        int[] groupCount = getGroupCount();
                        if ((groupCount[0] < 2)|| (groupCount[1] < 2)) {
                            pPanel.permsInfoLabel.setForeground(Color.red);
                            pPanel.permsInfoLabel.setText("Group A and Group B must each contain more than one experiment");
                            pPanel.useAllPermsButton.setEnabled(false);
                        } else if ((groupCount[0] + groupCount[1]) > 20) {
                            pPanel.permsInfoLabel.setForeground(Color.black);
                            pPanel.permsInfoLabel.setText("There are too many possible permutations");
                            pPanel.useAllPermsButton.setEnabled(false);
                 
                        } else {
                            pPanel.permsInfoLabel.setForeground(Color.black);
                            int numCombs = getNumCombs(groupCount[0] + groupCount[1], groupCount[0]);
                            pPanel.permsInfoLabel.setText("There are " + numCombs + " permutations");
                            pPanel.useAllPermsButton.setEnabled(true);
                        }
                    }
                });
                 */
                    
                    /*
                    buildConstraints(constraints, 0, i, 1, 1, 100, 100);
                    //constraints.fill = GridBagConstraints.BOTH;
                    gridbag.setConstraints(expLabels[i], constraints);
                    panel1.add(expLabels[i]);
                     */
                    
                    for (int j = 0; j < numGroups; j++) {
                        buildConstraints(constraints, j, i, 1, 1, 100, 100);
                        //constraints.fill = GridBagConstraints.BOTH;
                        gridbag.setConstraints(exptGroupRadioButtons[j][i], constraints);
                        panel1.add(exptGroupRadioButtons[j][i]);
                    }
                        /*
                        buildConstraints(constraints, 1, i, 1, 1, 100, 100);
                        //constraints.fill = GridBagConstraints.BOTH;
                        gridbag.setConstraints(groupARadioButtons[i], constraints);
                        panel1.add(groupARadioButtons[i]);
                         
                        buildConstraints(constraints, 2, i, 1, 1, 100, 100);
                        //constraints.fill = GridBagConstraints.BOTH;
                        gridbag.setConstraints(groupBRadioButtons[i], constraints);
                        panel1.add(groupBRadioButtons[i]);
                         */
                    
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
                
                
                /*
                JViewport rowHeader = new JViewport();
                rowHeader.setPreferredSize(new Dimension(50, panel1.getHeight()));
                scroll.setRowHeader(rowHeader);
                 
                GridBagLayout rowHeaderGrid = new GridBagLayout();
                rowHeader.setLayout(rowHeaderGrid);
                 
                for (int i = 0; i < expLabels.length; i++) {
                    buildConstraints(constraints, 0, i, 1, 1, 100, 100);
                    constraints.fill = GridBagConstraints.BOTH;
                    rowHeaderGrid.setConstraints(expLabels[i], constraints);
                    rowHeader.add(expLabels[i]);
                }
                 */
                
                
                
                //rowHeader.setLayout(rowHeaderGrid);
                /*
                JScrollPane rowHeaderScroll = new JScrollPane(exptNameHeaderPanel);
                rowHeaderScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
                rowHeaderScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
                 */
                
                for (int i = 0; i < expLabels.length; i++) {
                    buildConstraints(constraints, 0, i, 1, 1, 100, 100);
                    constraints.fill = GridBagConstraints.BOTH;
                    exptHeaderGridbag.setConstraints(expLabels[i], constraints);
                    exptNameHeaderPanel.add(expLabels[i]);
                }
                
                //exptNameHeaderPanel.setMinimumSize(new Dimension(50, panel1.getHeight()));
                
                /*
                JViewport rowHeaderViewport = new JViewport();
                //rowHeaderViewport.setViewSize(new Dimension(70, panel1.getPreferredSize().height));
                //rowHeaderViewport.setPreferredSize(new Dimension(70, panel1.getPreferredSize().height));
                rowHeaderViewport.setView(exptNameHeaderPanel);
                 */
                
                //rowHeader.setPreferredSize(new Dimension(50, panel1.getHeight()));
                //scroll.setRowHeader(rowHeader);
                scroll.setRowHeaderView(exptNameHeaderPanel);
                /*
                JScrollBar exptHeaderScrollBar = new JScrollBar(JScrollBar.HORIZONTAL);
                exptHeaderScrollBar.addAdjustmentListener(new AdjustmentListener() {
                    public void adjustmentValueChanged(AdjustmentEvent e) {
                    }
                });
                 
                scroll.setCorner(JScrollPane.LOWER_LEFT_CORNER, exptHeaderScrollBar);
                 */
                
                
                /*
                GridBagLayout rowHeaderGrid = new GridBagLayout();
                rowHeader.setLayout(rowHeaderGrid);
                buildConstraints(constraints, 0, 0, 1, 1, 100, 100);
                constraints.fill = GridBagConstraints.BOTH;
                rowHeaderGrid.setConstraints(exptNameHeaderPanel, constraints);
                rowHeader.add(exptNameHeaderPanel);
                 */
                
                //scroll.setRowHeaderView(exptNameHeaderPanel);
                //scroll.getRowHeader().setSize(50, panel1.getHeight());
                //scroll.getRowHeader().setMinimumSize(new Dimension(50, panel1.getHeight()));
                
                
                
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
                                    JOptionPane.showMessageDialog(gPanel, "Incompatible file!", "Error", JOptionPane.WARNING_MESSAGE);
                                } else {
                                    for (int i = 0; i < groupsVector.size(); i++) {
                                        int currentGroup = ((Integer)groupsVector.get(i)).intValue();
                                        if (currentGroup != 0) {
                                            exptGroupRadioButtons[currentGroup - 1][i].setSelected(true);
                                        } else {
                                            notInGroupRadioButtons[i].setSelected(true);
                                        }
                                            /*
                                            if (currentExpt == 1) {
                                                groupARadioButtons[i].setSelected(true);
                                            } else if (currentExpt == 2) {
                                                groupBRadioButtons[i].setSelected(true);
                                            } else if (currentExpt == 3) {
                                                neitherGroupRadioButtons[i].setSelected(true);
                                            } else {
                                                for (int j = 0; j < finNum; j++) {
                                                    groupARadioButtons[j].setSelected(true);
                                                }
                                                JOptionPane.showMessageDialog(gPanel, "Incompatible file!", "Error", JOptionPane.WARNING_MESSAGE);
                                                break;
                                            }
                                             */
                                    }
                                }
                            } catch (Exception e) {
                                JOptionPane.showMessageDialog(gPanel, "Incompatible file!", "Error", JOptionPane.WARNING_MESSAGE);
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
    
    class PermutationsPanel extends JPanel {
        //JLabel permsInfoLabel;
        //JRadioButton useAllPermsButton, numPermsButton;
        JLabel numPermsLabel;
        JTextField numPermsInputField;
        PermutationsPanel() {
            this.setBorder(new TitledBorder(new EtchedBorder(), "Number of permutations", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), Color.black));
            setBackground(Color.white);
            numPermsLabel = new JLabel("Enter number of permutations:   ");
            numPermsInputField = new JTextField("100", 7);
            /*
            permsInfoLabel = new JLabel("Group A and Group B must each contain more than one experiment");
            permsInfoLabel.setForeground(Color.red);
            numPermsInputField = new JTextField("100", 7);
            useAllPermsButton = new JRadioButton("Use all permutations", false);
            useAllPermsButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    numPermsInputField.setBackground(Color.gray);
                    numPermsInputField.setText("");
                }
            });
            useAllPermsButton.setEnabled(false);
            numPermsButton = new JRadioButton("Enter number of permutations", true);
            numPermsButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    numPermsInputField.setBackground(Color.white);
                    numPermsInputField.setText("100");
                }
            });
            ButtonGroup choosePerms = new ButtonGroup();
            choosePerms.add(useAllPermsButton);
            choosePerms.add(numPermsButton);
             */
            
            GridBagLayout gridbag = new GridBagLayout();
            GridBagConstraints constraints = new GridBagConstraints();
            this.setLayout(gridbag);
            
            buildConstraints(constraints, 0, 0, 1, 1, 50, 100);
            constraints.anchor = GridBagConstraints.EAST;
            gridbag.setConstraints(numPermsLabel, constraints);
            this.add(numPermsLabel);
            
            buildConstraints(constraints, 1, 0, 1, 1, 50, 0);
            constraints.anchor = GridBagConstraints.WEST;
            gridbag.setConstraints(numPermsInputField, constraints);
            this.add(numPermsInputField);
            
            
            /*
             //buildConstraints(constraints, 0, 1, 1, 1, 0, 25);
            buildConstraints(constraints, 0, 1, 2, 1, 0, 40);
            gridbag.setConstraints(useAllPermsButton, constraints);
            this.add(useAllPermsButton);
             
             //buildConstraints(constraints, 0, 2, 1, 1, 0, 25);
            buildConstraints(constraints, 0, 2, 1, 1, 60, 40);
            constraints.anchor = GridBagConstraints.EAST;
            gridbag.setConstraints(numPermsButton, constraints);
            this.add(numPermsButton);
             
             //buildConstraints(constraints, 0, 3, 1, 1, 0, 25);
            buildConstraints(constraints, 1, 2, 1, 1, 40, 0);
           constraints.anchor = GridBagConstraints.WEST;
            gridbag.setConstraints(numPermsInputField, constraints);
            this.add(numPermsInputField);
             */
        }
        
        public void reset(){
            this.numPermsInputField.setText("100");
        }
        
    }
    
    class S0AndQValueCalcPanel extends JPanel {
        JComboBox s0SelectBox;
        JTextField s0EntryField;
        JRadioButton s0SelectButton, s0EntryButton, qYesButton, qNoButton;
        
        S0AndQValueCalcPanel() {
            this.setBorder(new TitledBorder(new EtchedBorder(), "S0 and Q Value parameters", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), Color.black));            
            String[] s0SelectOptions = {"5th percentile", "50th percentile", "90th percentile", "Minimum S Value", "Tusher et al. method (slow!)"};
            setBackground(Color.white);
            s0SelectBox = new JComboBox(s0SelectOptions);
            //s0SelectBox.setBackground(Color.white);
            s0SelectButton = new JRadioButton("Select S0 using", true);
            s0SelectButton.setBackground(Color.white);
            s0EntryButton = new JRadioButton(" OR Enter s0 percentile (0-100)", false);
            s0EntryButton.setBackground(Color.white);
            s0EntryField = new JTextField("", 7);
            s0EntryField.setEnabled(false);
            s0EntryField.setBackground(Color.darkGray);         
            ButtonGroup s0Group = new ButtonGroup();
            s0SelectButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    s0SelectBox.setEnabled(true);
                    s0EntryField.setText("");
                    s0EntryField.setEnabled(false);
                    s0EntryField.setBackground(Color.darkGray);
                }
            });
            s0EntryButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    s0EntryField.setText("10");
                    s0EntryField.setEnabled(true);
                    s0EntryField.setBackground(Color.white);
                    s0SelectBox.setEnabled(false);
                }
            });            
            s0Group.add(s0SelectButton);
            s0Group.add(s0EntryButton);
            
            qYesButton = new JRadioButton("Yes (slow!)", false);
            qYesButton.setBackground(Color.white);
            qNoButton = new JRadioButton("No (quick)", true);
            qNoButton.setBackground(Color.white);
            ButtonGroup qGroup = new ButtonGroup();
            qGroup.add(qYesButton);
            qGroup.add(qNoButton);
            
            GridBagLayout gridbag = new GridBagLayout();
            GridBagConstraints constraints = new GridBagConstraints();
            this.setLayout(gridbag);

            buildConstraints(constraints, 0, 0, 1, 1, 25, 50);
            constraints.anchor = GridBagConstraints.EAST;
            gridbag.setConstraints(s0SelectButton, constraints);
            this.add(s0SelectButton);            
            
            buildConstraints(constraints, 1, 0, 1, 1, 25, 0);
            constraints.anchor = GridBagConstraints.WEST;
            gridbag.setConstraints(s0SelectBox, constraints);
            this.add(s0SelectBox);  
            
            buildConstraints(constraints, 2, 0, 1, 1, 25, 0);
            constraints.anchor = GridBagConstraints.EAST;
            gridbag.setConstraints(s0EntryButton, constraints);
            this.add(s0EntryButton);   
            
            
            buildConstraints(constraints, 3, 0, 1, 1, 25, 0);
            constraints.anchor = GridBagConstraints.WEST;
            gridbag.setConstraints(s0EntryField, constraints);
            this.add(s0EntryField);   
            
            JLabel qLabel = new JLabel("Calculate q-values?  ");
            buildConstraints(constraints, 0, 1, 1, 1, 10, 50);
            constraints.anchor = GridBagConstraints.EAST;
            gridbag.setConstraints(qLabel, constraints);
            this.add(qLabel);   
            
            buildConstraints(constraints, 1, 1, 1, 1, 10, 0);
            constraints.anchor = GridBagConstraints.EAST;
            gridbag.setConstraints(qNoButton, constraints);
            this.add(qNoButton);
            
            buildConstraints(constraints, 2, 1, 2, 1, 80, 0);
            constraints.anchor = GridBagConstraints.WEST;
            gridbag.setConstraints(qYesButton, constraints);
            this.add(qYesButton);             
            
        }
        
        public void reset() {
            s0SelectButton.setSelected(true);
            s0SelectBox.setEnabled(true);
            s0SelectBox.setSelectedIndex(0);
            s0EntryField.setText("");
            s0EntryField.setBackground(Color.darkGray);
            s0EntryField.setEnabled(false);
            qNoButton.setSelected(true);
        }
    }
    
    class ImputationPanel extends JPanel {
        JRadioButton kNearestButton, rowAverageButton;
        JTextField numNeighborsField;
        JLabel numNeighborsLabel;
        String numNeighborsText;
        JCheckBox saveMatrixChkBox;
        ImputationPanel() {
            //           this.setBorder(new TitledBorder(new EtchedBorder(), "Imputation Engine"));
            this.setBorder(new TitledBorder(new EtchedBorder(), "Imputation Engine", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), Color.black));
            setBackground(Color.white);
            numNeighborsText = "10";
            if (numGenes < 10) {
                numNeighborsText = Integer.toString(numGenes);
            }
            numNeighborsField = new JTextField(numNeighborsText, 7);
            numNeighborsLabel = new JLabel("Number of neighbors:       ");
            kNearestButton = new JRadioButton("K-nearest neighbors imputer", true);
            kNearestButton.setBackground(Color.white);
            kNearestButton.setFocusPainted(false);
            kNearestButton.setForeground(UIManager.getColor("Label.foreground"));
            rowAverageButton = new JRadioButton("Row average imputer              ", false);
            rowAverageButton.setBackground(Color.white);
            rowAverageButton.setFocusPainted(false);
            rowAverageButton.setForeground(UIManager.getColor("Label.foreground"));
            kNearestButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    numNeighborsLabel.setEnabled(true);
                    numNeighborsField.setEnabled(true);
                    numNeighborsField.setBackground(Color.white);
                    numNeighborsField.setText(numNeighborsText);
                }
            });
            
            rowAverageButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    numNeighborsLabel.setEnabled(false);
                    numNeighborsField.setEnabled(false);
                    numNeighborsField.setBackground(Color.gray);
                    numNeighborsField.setText("");
                }
            });
            ButtonGroup chooseKOrRowAvg = new ButtonGroup();
            chooseKOrRowAvg.add(kNearestButton);
            chooseKOrRowAvg.add(rowAverageButton);
            
            GridBagLayout gridbag = new GridBagLayout();
            GridBagConstraints constraints = new GridBagConstraints();
            this.setLayout(gridbag);
            
            //buildConstraints(constraints, 0, 0, 1, 1, 100, 25);
            buildConstraints(constraints, 0, 0, 1, 1, 40, 50);
            constraints.anchor = GridBagConstraints.EAST;
            gridbag.setConstraints(kNearestButton, constraints);
            this.add(kNearestButton);
            
            
            
            buildConstraints(constraints, 1, 0, 1, 1, 30, 0);
            constraints.anchor = GridBagConstraints.EAST;
            gridbag.setConstraints(numNeighborsLabel, constraints);
            this.add(numNeighborsLabel);
            
            
            buildConstraints(constraints, 2, 0, 1, 1, 30, 0);
            constraints.anchor = GridBagConstraints.WEST;
            gridbag.setConstraints(numNeighborsField, constraints);
            this.add(numNeighborsField);
            
            buildConstraints(constraints, 0, 1, 1, 1, 0, 50);
            constraints.anchor = GridBagConstraints.EAST;
            gridbag.setConstraints(rowAverageButton, constraints);
            this.add(rowAverageButton);
            
            buildConstraints(constraints, 1, 1, 1, 1, 0, 50);
            //  constraints.anchor = GridBagConstraints.EAST;
            JPanel dummy = new JPanel();
            dummy.setBackground(Color.white);
            gridbag.setConstraints(dummy, constraints);
            this.add(dummy);
            
            buildConstraints(constraints, 2, 1, 1, 1, 0, 50);
            //  constraints.anchor = GridBagConstraints.EAST;
            gridbag.setConstraints(dummy, constraints);
            this.add(dummy);
            
            
            constraints.anchor = GridBagConstraints.CENTER;
            buildConstraints(constraints, 0, 2, 3, 1, 0, 50);
            saveMatrixChkBox = new JCheckBox("Save Imputed Matrix", false);
            saveMatrixChkBox.setBackground(Color.white);
            saveMatrixChkBox.setFocusPainted(false);
            saveMatrixChkBox.setForeground(UIManager.getColor("Label.foreground"));
            gridbag.setConstraints(saveMatrixChkBox, constraints);
            this.add(saveMatrixChkBox);
        }
        
        private void reset(){
            this.numNeighborsField.setText(this.numNeighborsText);
            this.numNeighborsField.setBackground(Color.white);
            this.numNeighborsField.setEnabled(true);
            this.kNearestButton.setSelected(true);
            this.saveMatrixChkBox.setSelected(false);
        }
    }
    
    
    class OKCancelPanel extends JPanel {
        JButton okButton, cancelButton;
        JCheckBox drawTreesBox;
        
        OKCancelPanel() {
            okButton = new JButton("OK");
            cancelButton = new JButton("Cancel");
            drawTreesBox = new JCheckBox("Draw Hierarchical Trees");
            
            okButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    if (getStudyDesign() == SAMInitDialog.TWO_CLASS_UNPAIRED) {
                        // if (evt.getSource() == okButton) {
                        int[] grpAssignments = getGroupAssignments();
                        int grpACounter = 0;
                        int grpBCounter = 0;
                        for (int i = 0; i < grpAssignments.length; i++) {
                            if (grpAssignments[i] == GROUP_A) {
                                grpACounter++;
                            } else if (grpAssignments[i] == GROUP_B) {
                                grpBCounter++;
                            }
                        }
                        if ((grpACounter < 2) || (grpBCounter < 2)) {
                            JOptionPane.showMessageDialog(null, "Group A and Group B must contain more than one experiment", "Error", JOptionPane.WARNING_MESSAGE);
                        } /*else if ((Integer.parseInt(iPanel.numNeighborsField.getText()) > numGenes) || (Integer.parseInt(iPanel.numNeighborsField.getText()) <= 0)) {
                            JOptionPane.showMessageDialog(null, "Number of neighbors must be  > 0, and <= the total number of genes", "Error", JOptionPane.WARNING_MESSAGE);
                        } else if (Integer.parseInt(pPanel.numPermsInputField.getText()) < 0) {
                            JOptionPane.showMessageDialog(null, "Number of permutations must be > 0", "Error", JOptionPane.WARNING_MESSAGE);
                        } */else {
                            try {
                                int numCombs = 0;
                                //if (!useAllCombs()) {
                                numCombs = getUserNumCombs();
                                //}
                                int numNeibs = 0;
                                if (useKNearest()) {
                                    numNeibs = getNumNeighbors();
                                }
                                okPressed = true;
                                dispose();
                            } catch (NumberFormatException nfe) {
                                JOptionPane.showMessageDialog(null, "Invalid parameter value(s)!", "Error", JOptionPane.ERROR_MESSAGE);
                            }
                            
                            //okPressed = true;
                            //hide();
                            //dispose();
                        }
                        // }
                    } else if (getStudyDesign() == SAMInitDialog.MULTI_CLASS) {
                        boolean tooFew = false;
                        int[] grpAssignments = getGroupAssignments();
                        int numGroups = getMultiClassNumGroups();
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
                                int numCombs = 0;
                                //if (!useAllCombs()) {
                                numCombs = getUserNumCombs();
                                //}
                                int numNeibs = 0;
                                if (useKNearest()) {
                                    numNeibs = getNumNeighbors();
                                }
                                okPressed = true;
                                dispose();
                            } catch (NumberFormatException nfe) {
                                JOptionPane.showMessageDialog(null, "Invalid parameter value(s)!", "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    } else if (getStudyDesign() == SAMInitDialog.CENSORED_SURVIVAL) {
                        try {
                            boolean allSame = true;
                            int selectedCounter = 0;
                            double previousTime = 0;
                            for (int i = 0; i < exptNames.size(); i++) {
                                if (csPanel.fields[i].inAnalysisCheckBox.isSelected()) {
                                    selectedCounter++;
                                    double d = (new Double(csPanel.fields[i].timeInputField.getText())).doubleValue();
                                    if (i > 0) {
                                        if (previousTime != d) {
                                            allSame = false;
                                        }
                                    }
                                    previousTime = d;
                                }
                            }
                            int numCombs = 0;
                            //if (!useAllCombs()) {
                            numCombs = getUserNumCombs();
                            //}
                            int numNeibs = 0;
                            if (useKNearest()) {
                                numNeibs = getNumNeighbors();
                            }
                            if (selectedCounter < 2) {
                                JOptionPane.showMessageDialog(null, "At least 2 samples must be selected!", "Error", JOptionPane.ERROR_MESSAGE);
                            } else if (allSame) {
                                JOptionPane.showMessageDialog(null, "At least one of the survival time values must be different from the rest!", "Error", JOptionPane.ERROR_MESSAGE);
                            } else {
                                okPressed = true;
                                dispose();
                            }
                        } catch (NumberFormatException nfe){
                            JOptionPane.showMessageDialog(null, "Invalid parameter value(s)!", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            });
            
            cancelButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    //  if (evt.getSource() == cancelButton) {
                    okPressed = false;
                    //hide();
                    dispose();
                    // }
                }
            });
            
            GridBagLayout gridbag = new GridBagLayout();
            GridBagConstraints constraints = new GridBagConstraints();
            this.setLayout(gridbag);
            //constraints.fill = GridBagConstraints.BOTH;
            buildConstraints(constraints, 0, 0, 2, 1, 0, 50);
            gridbag.setConstraints(drawTreesBox, constraints);
            this.add(drawTreesBox);
            
            buildConstraints(constraints, 0, 1, 1, 1, 50, 50);
            //constraints.anchor = GridBagConstraints.CENTER;
            gridbag.setConstraints(okButton, constraints);
            this.add(okButton);
            
            buildConstraints(constraints, 1, 1, 1, 1, 50, 0);
            //constraints.anchor = GridBagConstraints.CENTER;
            gridbag.setConstraints(cancelButton, constraints);
            this.add(cancelButton);
        }
    }
    
    
    public int getStudyDesign() {
        int studyDesign = TWO_CLASS_UNPAIRED;
        if (tabPane.getSelectedIndex() == 0) {
            studyDesign = TWO_CLASS_UNPAIRED;
        } else if (tabPane.getSelectedIndex() == 1) {
            studyDesign = SAMInitDialog.TWO_CLASS_PAIRED;
        } else if (tabPane.getSelectedIndex() == 2) {
            studyDesign = SAMInitDialog.MULTI_CLASS;
        } else if (tabPane.getSelectedIndex() == 3) {
            studyDesign = SAMInitDialog.CENSORED_SURVIVAL;
        } else if (tabPane.getSelectedIndex() == 4) {
            studyDesign = SAMInitDialog.ONE_CLASS;
        }
        return studyDesign;
    }
    
    public int[] getGroupAssignments() {
        int[] groupAssignments = new int[exptNames.size()];
        if (getStudyDesign() == TWO_CLASS_UNPAIRED) {
            for (int i = 0; i < exptNames.size(); i++) {
                if (gPanel.groupARadioButtons[i].isSelected()) {
                    groupAssignments[i] = GROUP_A;
                } else if (gPanel.groupBRadioButtons[i].isSelected()) {
                    groupAssignments[i] = GROUP_B;
                } else {
                    groupAssignments[i] = NEITHER_GROUP;
                }
            }
        } else if (getStudyDesign() == MULTI_CLASS) { //  THAT "NOT IN GROUP" IS STORED AS ZERO, AND GROUP J IS STORED AS THE INTEGER J (I.E., THERE IS NO GROUP 0)
            for (int i = 0; i < exptNames.size(); i++) {
                if (mPanel.mulgPanel.notInGroupRadioButtons[i].isSelected()) {
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
        } else if (getStudyDesign() == ONE_CLASS) {
            return getOneClassAssignments();
        }
        
        return groupAssignments;
    }
    
    public int[] getOneClassAssignments() {
        int[] oneClassAssignments = new int[oneCPanel.includeExpts.length];
        
        for (int i = 0; i < oneClassAssignments.length; i++) {
            if (oneCPanel.includeExpts[i].isSelected()) {
                oneClassAssignments[i] = 1;
            } else {
                oneClassAssignments[i] = 0;
            }
        }
        
        return oneClassAssignments;
    }    
    
    public double getOneClassMean() {
        return Double.parseDouble(oneCPanel.meanField.getText());
    }    
    
    public int getMultiClassNumGroups() {
        return Integer.parseInt(mPanel.ngPanel.numGroupsField.getText());
    }
    
    public int getNumValidOneClassExpts() {
        int validNum = 0;
        int[] oca = getOneClassAssignments();
        
        for (int i =0; i < oca.length; i++) {
            if (oca[i] == 1) {
                validNum++;
            }
        }
        
        return validNum;
    }    
    
    
    public int[] getGroupCount() {
        int[] groupAssignments = getGroupAssignments();
        int groupACount = 0;
        int groupBCount = 0;
        
        for (int i = 0; i < groupAssignments.length; i++) {
            if (groupAssignments[i] == GROUP_A) {
                groupACount++;
            } else if (groupAssignments[i] == GROUP_B) {
                groupBCount++;
            }
        }
        
        int[] groupCount = new int[2];
        groupCount[0] = groupACount;
        groupCount[1] = groupBCount;
        
        return groupCount;
    }
    
    public boolean[] isInSurvivalAnalysis() {
        boolean[] inAnalysis = new boolean[csPanel.fields.length];
        for (int i = 0; i < inAnalysis.length; i++) {
            if (csPanel.fields[i].inAnalysisCheckBox.isSelected()) {
                inAnalysis[i] = true;
            } else {
                inAnalysis[i] = false;
            }
        }
        return inAnalysis;
    }
    
    public double[] getSurvivalTimes() {
        double[] survivalTimes = new double[exptNames.size()];
        for (int i = 0; i < survivalTimes.length; i++) {
            try {
                String s = csPanel.fields[i].timeInputField.getText();
                double d = (new Double(s)).doubleValue();
                survivalTimes[i] = d;
                //System.out.println("SAMInitDialog.getSurvivalTimes(): i =  " + i + ", s = " + s + ", d = " + d);
            } catch (NumberFormatException nfe){
                //nfe.printStackTrace();
                survivalTimes[i] = 0;
            }
        }
        return survivalTimes;
    }
    
    public boolean[] isCensored() {
        boolean[] censored = new boolean[exptNames.size()];
        for (int i = 0; i  < censored.length; i++) {
            if (csPanel.fields[i].censoredRadioButton.isSelected()) {
                censored[i] = true;
            } else {
                censored[i] = false;
            }
        }
        return censored;
    }
    
    public Vector getPairedAExpts() {
        return tcpmPanel.tcpPanel.pairedAExpts;
    }
    
    public Vector getPairedBExpts() {
        return tcpmPanel.tcpPanel.pairedBExpts;
    }
    
    private long factorial(int n) {
        if ((n==1) || (n == 0)) {
            return 1;
        }
        else {
            return factorial(n-1) * n;
        }
    }
    
    private int getNumCombs(int n, int k) { // nCk
        
        /*
        System.out.println("n = " + n);
        System.out.println("k = " + k);
        System.out.println("Numerator: factorial(n) = " + factorial(n));
        System.out.println("factorial(k) = " + factorial(k));
        System.out.println("factorial(n-k) = " + factorial(n-k));
         */
        return Math.round(factorial(n)/(factorial(k)*factorial(n-k)));
    }
    
    public int getNumNeighbors() {
        String s = iPanel.numNeighborsField.getText();
        return Integer.parseInt(s);
    }
    
    public int getUserNumCombs() {
        String s = pPanel.numPermsInputField.getText();
        return Integer.parseInt(s);
    }
    
    
    public boolean useKNearest() {
        return iPanel.kNearestButton.isSelected();
    }
    
    /*
    public boolean useAllCombs() {
        return pPanel.useAllPermsButton.isSelected();
    }
     */
    
    public boolean drawTrees() {
        return this.hclOpsPanel.isHCLSelected();
    }
    
    public boolean useTusherEtAlS0() {
        if (sqPanel.s0SelectBox.getSelectedIndex() == 4) {
            return true;
        } else {
            return false;
        }
    }
    
    public double getPercentile() {
        if (sqPanel.s0SelectButton.isSelected()) {
            int index = sqPanel.s0SelectBox.getSelectedIndex();
            if (index == 0) {
                return 5d;
            } else if (index == 1) {
                return 50d;
            } else if (index == 2) {
                return 90d;
            } else if (index == 3) {
                return 0d;
            } else if (index == 4) {
                return -1d;
            }
        } else {
            return Double.parseDouble(sqPanel.s0EntryField.getText());
        }
        return 0d;
    }
    
    public boolean calculateQLowestFDR() {
        if (sqPanel.qYesButton.isSelected()){
            return true;
        } else {
            return false;
        }
    }
    
    public int getNumUniquePerms() {
        return this.numUniquePerms;
    }
    
    public boolean useAllUniquePerms() {
        return this.allUniquePermsUsed;
    }
    
    private int getNumUnique2ClassUnpairedPerms(int n, int k) { // nCk
        return Math.round(factorial(n)/(factorial(k)*factorial(n-k)));
    }    
    
    
    public class EventListener extends WindowAdapter implements ActionListener{
        public void actionPerformed(ActionEvent ae){
            String command = ae.getActionCommand();
            if(command.equals("ok-command")){
                try {
                    if (sqPanel.s0EntryButton.isSelected()) {
                        double d = Double.parseDouble(sqPanel.s0EntryField.getText());
                        if ((d < 0)||(d > 100)) {
                            JOptionPane.showMessageDialog(null, "Enter a valid percentile between 0 and 100!", "Error!", JOptionPane.ERROR_MESSAGE);
                            return;                          
                        }
                    }
                } catch (NumberFormatException nfe) {
                    JOptionPane.showMessageDialog(null, "Enter a valid percentile between 0 and 100!", "Error!", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                try {
                    int numCombs = 0;
                    //if (!useAllCombs()) {
                    numCombs = getUserNumCombs();
                    //}
                    int numNeibs = 0;
                    if (useKNearest()) {
                        numNeibs = getNumNeighbors();
                    }                  
                    
                } catch (NumberFormatException nfe) {
                    JOptionPane.showMessageDialog(null, "Invalid parameter value(s)!", "Error", JOptionPane.ERROR_MESSAGE); 
                    return;
                }
                if (getStudyDesign() == SAMInitDialog.TWO_CLASS_UNPAIRED) {
                    // if (evt.getSource() == okButton) {
                    int[] grpAssignments = getGroupAssignments();
                    int grpACounter = 0;
                    int grpBCounter = 0;
                    for (int i = 0; i < grpAssignments.length; i++) {
                        if (grpAssignments[i] == GROUP_A) {
                            grpACounter++;
                        } else if (grpAssignments[i] == GROUP_B) {
                            grpBCounter++;
                        }
                    }
                    if ((grpACounter < 2) || (grpBCounter < 2)) {
                        JOptionPane.showMessageDialog(null, "Group A and Group B must contain more than one experiment", "Error", JOptionPane.WARNING_MESSAGE);
                    } /*else if ((Integer.parseInt(iPanel.numNeighborsField.getText()) > numGenes) || (Integer.parseInt(iPanel.numNeighborsField.getText()) <= 0)) {
                            JOptionPane.showMessageDialog(null, "Number of neighbors must be  > 0, and <= the total number of genes", "Error", JOptionPane.WARNING_MESSAGE);
                        } else if (Integer.parseInt(pPanel.numPermsInputField.getText()) < 0) {
                            JOptionPane.showMessageDialog(null, "Number of permutations must be > 0", "Error", JOptionPane.WARNING_MESSAGE);
                        } */else {
                            try {
                                
                                int numCombs = 0;
                                //if (!useAllCombs()) {
                                numCombs = getUserNumCombs();
                                //}
                                int numNeibs = 0;
                                if (useKNearest()) {
                                    numNeibs = getNumNeighbors();
                                }
                                 
                                if ((grpACounter + grpBCounter) <= 20) {
                                    numUniquePerms = getNumUnique2ClassUnpairedPerms((grpACounter + grpBCounter), grpACounter);
                                    SAMAllPermsDialog sapDialog = new SAMAllPermsDialog(SAMGUI.SAMFrame, true, numUniquePerms, numCombs);
                                    sapDialog.setVisible(true);
                                    allUniquePermsUsed = sapDialog.useAllPerms();
                                }
                                okPressed = true;
                                javax.swing.UIManager.put("TabbedPane.selected", Color.lightGray);
                                
                                dispose();
                            } catch (NumberFormatException nfe) {
                                JOptionPane.showMessageDialog(null, "Invalid parameter value(s)!", "Error", JOptionPane.ERROR_MESSAGE);
                            }
                            
                            //okPressed = true;
                            //hide();
                            //dispose();
                        }
                    // }
                } else if (getStudyDesign() == SAMInitDialog.TWO_CLASS_PAIRED) {
                    if (tcpmPanel.tcpPanel.pairedListModel.size() < 2) {
                        JOptionPane.showMessageDialog(null, "Need at least two pairs of experiments!", "Error", JOptionPane.ERROR_MESSAGE);
                    } else {
                        if (tcpmPanel.tcpPanel.pairedListModel.size() <= 29) {
                            int numCombs = getUserNumCombs();
                            numUniquePerms = (int)(Math.pow(2, tcpmPanel.tcpPanel.pairedListModel.size()));
                            SAMAllPermsDialog sapDialog = new SAMAllPermsDialog(SAMGUI.SAMFrame, true, numUniquePerms, numCombs);
                            sapDialog.setVisible(true);
                            allUniquePermsUsed = sapDialog.useAllPerms();                            
                        }
                        okPressed = true;
                        javax.swing.UIManager.put("TabbedPane.selected", Color.lightGray);
                        dispose();
                    }
                    
                } else if (getStudyDesign() == SAMInitDialog.MULTI_CLASS) {
                    boolean tooFew = false;
                    int[] grpAssignments = getGroupAssignments();
                    int numGroups = getMultiClassNumGroups();
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
                            int numCombs = 0;
                            //if (!useAllCombs()) {
                            numCombs = getUserNumCombs();
                            //}
                            int numNeibs = 0;
                            if (useKNearest()) {
                                numNeibs = getNumNeighbors();
                            }
                            okPressed = true;
                            javax.swing.UIManager.put("TabbedPane.selected", Color.lightGray);
                            
                            dispose();
                        } catch (NumberFormatException nfe) {
                            JOptionPane.showMessageDialog(null, "Invalid parameter value(s)!", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                     
                } else if (getStudyDesign() == SAMInitDialog.CENSORED_SURVIVAL) {
                    try {
                        boolean allSame = true;
                        int selectedCounter = 0;
                        double previousTime = 0;
                        for (int i = 0; i < exptNames.size(); i++) {
                            if (csPanel.fields[i].inAnalysisCheckBox.isSelected()) {
                                selectedCounter++;
                                double d = (new Double(csPanel.fields[i].timeInputField.getText())).doubleValue();
                                if (i > 0) {
                                    if (previousTime != d) {
                                        allSame = false;
                                    }
                                }
                                previousTime = d;
                            }
                        }
                        int numCombs = 0;
                        //if (!useAllCombs()) {
                        numCombs = getUserNumCombs();
                        //}
                        int numNeibs = 0;
                        if (useKNearest()) {
                            numNeibs = getNumNeighbors();
                        }
                        if (selectedCounter < 2) {
                            JOptionPane.showMessageDialog(null, "At least 2 samples must be selected!", "Error", JOptionPane.ERROR_MESSAGE);
                        } else if (allSame) {
                            JOptionPane.showMessageDialog(null, "At least one of the survival time values must be different from the rest!", "Error", JOptionPane.ERROR_MESSAGE);
                        } else {
                            if (selectedCounter <= 10) {
                                numUniquePerms = (int)(factorial(selectedCounter));
                                SAMAllPermsDialog sapDialog = new SAMAllPermsDialog(SAMGUI.SAMFrame, true, numUniquePerms, numCombs);
                                sapDialog.setVisible(true);
                                allUniquePermsUsed = sapDialog.useAllPerms();
                            }                            
                            okPressed = true;
                            javax.swing.UIManager.put("TabbedPane.selected", Color.lightGray);
                            dispose();
                        }
                    } catch (NumberFormatException nfe){
                        JOptionPane.showMessageDialog(null, "Invalid parameter value(s)!", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else if (getStudyDesign() == SAMInitDialog.ONE_CLASS) {
                    try {
                        double ocm = getOneClassMean();
                        if (getNumValidOneClassExpts() < 2) {
                            JOptionPane.showMessageDialog(null, "At least 2 experiments must be selected for one-class test!", "Error", JOptionPane.ERROR_MESSAGE);                            
                        } else {
                            if (getNumValidOneClassExpts() <= 29) {
                                int numCombs = getUserNumCombs();
                                numUniquePerms = (int)(Math.pow(2, getNumValidOneClassExpts()));
                                SAMAllPermsDialog sapDialog = new SAMAllPermsDialog(SAMGUI.SAMFrame, true, numUniquePerms, numCombs);
                                sapDialog.setVisible(true);
                                allUniquePermsUsed = sapDialog.useAllPerms();
                            }                            
                            okPressed = true;
                            javax.swing.UIManager.put("TabbedPane.selected", Color.lightGray);
                            dispose();
                        }
                    } catch (NumberFormatException nfe) {
                        JOptionPane.showMessageDialog(null, "Invalid value for one-class mean!", "Error", JOptionPane.ERROR_MESSAGE);                        
                    }
                }
            }  // ends handling ok-command
            else if(command.equals("reset-command")){
                if(getStudyDesign() == SAMInitDialog.TWO_CLASS_UNPAIRED)
                    gPanel.reset();
                else if(getStudyDesign() == SAMInitDialog.MULTI_CLASS && mPanel.mulgPanel != null)
                    mPanel.reset();
                else if(getStudyDesign() == SAMInitDialog.CENSORED_SURVIVAL)
                    csPanel.reset();
                sqPanel.reset();
                pPanel.reset();
                iPanel.reset();
                tcpmPanel.tcpPanel.reset();
                hclOpsPanel.setHCLSelected(false);
            }
            else if(command.equals("cancel-command")){
                okPressed = false;
                //hide();
                javax.swing.UIManager.put("TabbedPane.selected", Color.lightGray);
                dispose();
            }
            else if(command.equals("info-command")){
                HelpWindow help = new HelpWindow(SAMInitDialog.this, "SAM Initialization Dialog");
                if(help.getWindowContent()){
                    help.setSize(500,650);
                    help.setLocation();
                    help.show();
                    return;
                }
            }
        }
        
        public void windowClosing(WindowEvent we){
            javax.swing.UIManager.put("TabbedPane.selected", Color.lightGray);
            //System.out.println("Sam is closed");
        }
        
    }
    
    public static void main(String[] args) {
        
        JFrame dummyFrame = new JFrame();
        Vector dummyVect = new Vector();
        for (int i = 0; i < 100; i++) {
            dummyVect.add("Expt " + i);
        }
        SAMInitDialog sDialog = new SAMInitDialog(dummyFrame, true, dummyVect, 5);
        sDialog.setVisible(true);
        System.exit(0);
        
    }
    
}
