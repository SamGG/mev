/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: TtestInitDialog.java,v $
 * $Revision: 1.3 $
 * $Date: 2004-01-13 17:33:45 $
 * $Author: nbhagaba $
 * $State: Exp $
 */

package org.tigr.microarray.mev.cluster.gui.impl.ttest;

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
public class TtestInitDialog extends AlgorithmDialog {
    
    GroupExperimentsPanel gPanel;
    OneClassPanel oPanel;
    PValuePanel pPanel;
    SignificancePanel sPanel;
    HCLSelectionPanel hclOpsPanel;
    Vector exptNames;
    JTabbedPane chooseDesignPane;
    
    public static final int GROUP_A = 1;
    public static final int GROUP_B = 2;
    public static final int NEITHER_GROUP = 3;
    public static final int JUST_ALPHA = 4;
    public static final int STD_BONFERRONI = 5;
    public static final int ADJ_BONFERRONI = 6;
    public static final int BETWEEN_SUBJECTS = 7;
    public static final int ONE_CLASS = 8;
    public static final int MAX_T = 9;
    public static final int MIN_P = 10;
    
    boolean okPressed = false;
    boolean permParamOkPressed = false;
    private int userNumCombs = 0;
    private boolean allCombsUsed = false;
    private int allPossCombs;
    private Color LABEL_COLOR = UIManager.getColor("Label.foreground");
    
    
    boolean tooMany = false;
    int count;
    
    /** Creates new TtestInitDialog */
    public TtestInitDialog(JFrame parentFrame, boolean modality, Vector exptNames) {
        super(parentFrame, "TTEST: T-test", modality);
        this.exptNames = exptNames;
        setBounds(0, 0, 800, 800);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        JPanel pane = new JPanel();
        pane.setLayout(gridbag);
        
        chooseDesignPane = new JTabbedPane();
        
        sPanel = new SignificancePanel();
        
        pPanel = new PValuePanel();
        
        gPanel = new GroupExperimentsPanel(exptNames);
        for (count = 0; count < gPanel.groupARadioButtons.length; count++) {
            gPanel.groupARadioButtons[count].addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    //if (evt.getSource() == gPanel.groupARadioButtons[count]) {
                    pPanel.tDistButton.setSelected(true);
                    sPanel.justAlphaButton.setSelected(true);
                    sPanel.maxTButton.setSelected(false);
                    sPanel.maxTButton.setEnabled(false);
                    sPanel.minPButton.setSelected(false);
                    sPanel.minPButton.setEnabled(false);                    
                    pPanel.randomGroupsButton.setEnabled(false);
                    pPanel.allCombsButton.setEnabled(false);
                    pPanel.timesField.setEnabled(false);
                    pPanel.numCombsLabel.setText("                                                                            ");
                    //}
                }
                
            });
        }
        
        for (count = 0; count < gPanel.groupBRadioButtons.length; count++) {
            gPanel.groupBRadioButtons[count].addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    //if (evt.getSource() == gPanel.groupBRadioButtons[count]) {
                    pPanel.tDistButton.setSelected(true);
                    sPanel.justAlphaButton.setSelected(true);
                    sPanel.maxTButton.setSelected(false);
                    sPanel.maxTButton.setEnabled(false);
                    sPanel.minPButton.setSelected(false);
                    sPanel.minPButton.setEnabled(false);                    
                    pPanel.randomGroupsButton.setEnabled(false);
                    pPanel.allCombsButton.setEnabled(false);
                    pPanel.timesField.setEnabled(false);
                    pPanel.numCombsLabel.setText("                                                                            ");
                    //}
                }
                
            });
        }
        
        for (count = 0; count < gPanel.neitherGroupRadioButtons.length; count++) {
            gPanel.neitherGroupRadioButtons[count].addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    //if (evt.getSource() == gPanel.neitherGroupRadioButtons[count]) {
                    pPanel.tDistButton.setSelected(true);
                    sPanel.justAlphaButton.setSelected(true);
                    sPanel.maxTButton.setSelected(false);
                    sPanel.maxTButton.setEnabled(false);
                    sPanel.minPButton.setSelected(false);
                    sPanel.minPButton.setEnabled(false);                     
                    pPanel.randomGroupsButton.setEnabled(false);
                    pPanel.allCombsButton.setEnabled(false);
                    pPanel.timesField.setEnabled(false);
                    pPanel.numCombsLabel.setText("                                                                            ");
                    //}
                }
                
            });
        }
        
        count = 0;
        
       
        
        oPanel = new OneClassPanel();
        
        for (int i = 0; i < oPanel.includeExpts.length; i++) {
            oPanel.includeExpts[i].addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    //if (evt.getSource() == gPanel.neitherGroupRadioButtons[count]) {
                    pPanel.tDistButton.setSelected(true);
                    sPanel.justAlphaButton.setSelected(true);
                    sPanel.maxTButton.setSelected(false);
                    sPanel.maxTButton.setEnabled(false);
                    sPanel.minPButton.setSelected(false);
                    sPanel.minPButton.setEnabled(false);                     
                    pPanel.randomGroupsButton.setEnabled(false);
                    pPanel.allCombsButton.setEnabled(false);
                    pPanel.timesField.setEnabled(false);
                    pPanel.numCombsLabel.setText("                                                                            ");
                    //}
                }                
            });
        }
        
        chooseDesignPane.add("One-class", oPanel);
        chooseDesignPane.add("Between subjects", gPanel);
        /*
        buildConstraints(constraints, 0, 0, 1, 1, 100, 45);
        gridbag.setConstraints(chooseDesignPane, constraints);
        pane.add(chooseDesignPane);
         */
        
        pPanel.tDistButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                sPanel.justAlphaButton.setSelected(true);
                sPanel.maxTButton.setSelected(false);
                sPanel.maxTButton.setEnabled(false);
                sPanel.minPButton.setSelected(false);
                sPanel.minPButton.setEnabled(false);                
            }
        });
        
        pPanel.permutButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                if (evt.getSource() == pPanel.permutButton) {                    
                    sPanel.maxTButton.setEnabled(true);
                    //sPanel.minPButton.setEnabled(true);    // **** ENABLE THIS WHEN MINP MTHOD HAS BEEN DEBUGGED  - 1/12/2004            
                    if (getTestDesign() == TtestInitDialog.BETWEEN_SUBJECTS) {
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
                            pPanel.numCombsLabel.setForeground(Color.red);
                            pPanel.numCombsLabel.setText("Error! Group A and Group B must each contain more than one experiment");
                            //JOptionPane.showMessageDialog(gPanel, "Group A and Group B must each contain more than one experiment", "Error", JOptionPane.WARNING_MESSAGE);
                        } else {

                            int numCombs = 0;
                            pPanel.numCombsLabel.setForeground(Color.black);
                            pPanel.numCombsLabel.setText("There are too many unique permutations                                  ");
                            pPanel.allCombsButton.setEnabled(false);
                            pPanel.randomGroupsButton.setEnabled(true);
                            pPanel.randomGroupsButton.setSelected(true);
                            pPanel.timesField.setEnabled(true);
                            pPanel.timesField.setBackground(Color.white);
                            pPanel.timesField.setText("100");
                            //tooMany = true;
                            if ((grpACounter + grpBCounter) <= 20) {
                                numCombs = getNumCombs((grpACounter + grpBCounter), grpACounter);
                                pPanel.numCombsLabel.setForeground(Color.black);
                                pPanel.numCombsLabel.setText("There are " + numCombs + " unique permutations                                ");
                                allPossCombs = numCombs;
                                pPanel.allCombsButton.setEnabled(true);
                                pPanel.randomGroupsButton.setEnabled(true);
                            }
                        }
                    } else if (getTestDesign() == TtestInitDialog.ONE_CLASS) {
                        pPanel.numCombsLabel.setForeground(Color.black);
                        int validNum = getNumValidOneClassExpts();
                        if (validNum <= 1) {
                            pPanel.numCombsLabel.setForeground(Color.red);
                            pPanel.numCombsLabel.setText("Error! Choose at least two experiments");
                            pPanel.randomGroupsButton.setEnabled(false); 
                            pPanel.timesField.setBackground(Color.gray);
                            pPanel.timesField.setEnabled(false);
                            
                        } else if (validNum <= 29) {
                            pPanel.numCombsLabel.setText("There are " + (int)Math.pow(2, validNum) + " possible combinations                    ");
                            pPanel.randomGroupsButton.setEnabled(true);
                            pPanel.allCombsButton.setEnabled(true);
                            pPanel.timesField.setBackground(Color.white);
                            pPanel.timesField.setEnabled(true);                           
                        } else {
                            pPanel.numCombsLabel.setText("There are too many unique combinations                                       ");
                            pPanel.timesField.setBackground(Color.white);
                            pPanel.timesField.setEnabled(true);  
                            pPanel.randomGroupsButton.setEnabled(true);
                            pPanel.randomGroupsButton.setSelected(true);                            
                        }
                                              
                        //pPanel.allCombsButton.setEnabled(false);
                        /*
                        pPanel.randomGroupsButton.setEnabled(true);
                        pPanel.randomGroupsButton.setSelected(true);
                        pPanel.timesField.setEnabled(true);
                        pPanel.timesField.setBackground(Color.white);
                        pPanel.timesField.setText("100");  
                         */                    
                    }
                }
            }
        });
        /*
        pPanel.tDistButton.addActionListener(new ActionListener() {
            public void actionPerformed (ActionEvent evt) {
                if (evt.getSource() == pPanel.tDistButton) {
                        pPanel.permParamButton.setEnabled(false);
                        oPanel.okButton.setEnabled(true);
         
         
                }
         
                }
            });
        pPanel.permutButton.addActionListener(new ActionListener() {
            public void actionPerformed (ActionEvent evt) {
                    if (evt.getSource() == pPanel.permutButton) {
                        pPanel.permParamButton.setEnabled(true);
                        if (permParamOkPressed == false) {
                            oPanel.okButton.setEnabled(false);
         
                        }
                }
            }
         
            });
         
         
         */
        /*
          pPanel.permParamButton.addActionListener(new ActionListener() {
            public void actionPerformed (ActionEvent evt) {
                    if (evt.getSource() == pPanel.permParamButton) {
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
                        JOptionPane.showMessageDialog(gPanel, "Group A and Group B must each contain more than one experiment", "Error", JOptionPane.WARNING_MESSAGE);
                    } else {
         
                        //if (pPanel.permutButton.isSelected()) {
                            //System.out.println("grpACounter = " + grpACounter + ", grpBCounter = " + grpBCounter);
                            int numCombs = 0;
                            boolean tooMany = true;
                            if ((grpACounter + grpBCounter) <= 20) {
                                numCombs = getNumCombs((grpACounter + grpBCounter), grpACounter);
                                tooMany = false;
                            }
         
                            NumPermutationsDialog numPermsDialog = new NumPermutationsDialog(new JFrame(), true, numCombs, tooMany);
                        //
                       //     numPermsDialog.okay.addActionListener(new ActionListener() {
                        //        public void actionPerformed (ActionEvent evt) {
                        //            //if (evt.getSource() == numPermsDialog.okay) {
                        //                permParamOkPressed = true;
                        //                oPanel.okButton.setEnabled(true);
                        //                hide();
                        //                dispose();
                        //            //}
                       //         }
                      //      });
                      //      numPermsDialog.cancel.addActionListener(new ActionListener() {
                       //         public void actionPerformed (ActionEvent evt) {
                       //             //if (evt.getSource() == numPermsDialog.cancel) {
                        //                permParamOkPressed = false;
                        //                oPanel.okButton.setEnabled(false);
                        //                hide();
                       //                 dispose();
                                    //}
                        //        }
                       //     });
                        //
                            //numPermsDialog.show();
                       //     numPermsDialog.setVisible(true);
                        //}
         
                        }
                    }
                }
         
            });
         */
        
        
        chooseDesignPane.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                pPanel.tDistButton.setSelected(true);
                sPanel.justAlphaButton.setSelected(true);
                sPanel.maxTButton.setSelected(false);
                sPanel.maxTButton.setEnabled(false);
                sPanel.minPButton.setSelected(false);
                sPanel.minPButton.setEnabled(false);                
                pPanel.allCombsButton.setEnabled(false);
                pPanel.randomGroupsButton.setEnabled(false);
                pPanel.numCombsLabel.setForeground(Color.black);
                pPanel.numCombsLabel.setText(""); 
                pPanel.timesField.setEnabled(false);
                if (getTestDesign() == TtestInitDialog.ONE_CLASS) {
                    pPanel.numCombsLabel.setForeground(Color.black);
                    pPanel.numCombsLabel.setText("");                     
                }
            }
        });
         

        buildConstraints(constraints, 0, 0, 1, 1, 100, 45);
        gridbag.setConstraints(chooseDesignPane, constraints);
        pane.add(chooseDesignPane);        
        
        buildConstraints(constraints, 0, 1, 1, 1, 0, 25);
        gridbag.setConstraints(pPanel, constraints);
        pane.add(pPanel);
        
        //sPanel = new SignificancePanel();
        buildConstraints(constraints, 0, 2, 1, 1, 0, 20);
        gridbag.setConstraints(sPanel, constraints);
        pane.add(sPanel);
        
        hclOpsPanel = new HCLSelectionPanel();
        buildConstraints(constraints, 0, 3, 1, 1, 0, 10);
        gridbag.setConstraints(hclOpsPanel, constraints);
        
        pane.add(hclOpsPanel);
        addContent(pane);
        setActionListeners(new EventListener());
    }
    
    
    public void setVisible(boolean visible) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);
        
        super.setVisible(visible);
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
    
    class GroupExperimentsPanel extends JPanel {
        JLabel[] expLabels;
        JRadioButton[] groupARadioButtons, groupBRadioButtons, neitherGroupRadioButtons;
        GroupExperimentsPanel(Vector exptNames) {
            this.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED), "Group Assignments", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), Color.black));
            this.setBackground(Color.white);
            JPanel panel1 = new JPanel();
            // panel1.setBackground(Color.white);
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
                String s1 = (String)(exptNames.get(i));//permut
                expLabels[i] = new JLabel(s1);
                expLabels[i].setForeground(Color.black);
                chooseGroup[i] = new ButtonGroup();
                groupARadioButtons[i] = new JRadioButton("Group A", true);
                //      groupARadioButtons[i].setBackground(Color.white);
                //      groupARadioButtons[i].setForeground(LABEL_COLOR);
                chooseGroup[i].add(groupARadioButtons[i]);
                groupBRadioButtons[i] = new JRadioButton("Group B", false);
                //groupBRadioButtons[i].setBackground(Color.white);
                //groupBRadioButtons[i].setForeground(LABEL_COLOR);
                chooseGroup[i].add(groupBRadioButtons[i]);
                
                neitherGroupRadioButtons[i] = new JRadioButton("Neither group", false);
                // neitherGroupRadioButtons[i].setBackground(Color.white);
                //neitherGroupRadioButtons[i].setForeground(LABEL_COLOR);
                chooseGroup[i].add(neitherGroupRadioButtons[i]);
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
            scroll.setBorder(BorderFactory.createLineBorder(Color.black,2));
            
            buildConstraints(constraints, 0, 0, 1, 1, 100, 90);
            constraints.fill = GridBagConstraints.BOTH;
            gridbag2.setConstraints(scroll, constraints);
            this.add(scroll);
            
            JLabel label1 = new JLabel("                                                Note: Group A and Group B  MUST each contain more than one experiment.");
            buildConstraints(constraints, 0, 1, 1, 1, 0, 5);
            constraints.anchor = GridBagConstraints.EAST;
            //constraints.fill = GridBagConstraints.BOTH;
            gridbag2.setConstraints(label1, constraints);
            this.add(label1);
            
            JPanel panel2 = new JPanel();
            panel2.setBackground(Color.white);
            GridBagLayout gridbag3 = new GridBagLayout();
            panel2.setLayout(gridbag3);
            
            JButton saveButton = new JButton(" Save grouping ");
            saveButton.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED, Color.gray, new Color(185,185,185), Color.darkGray, Color.darkGray));
            saveButton.setFocusPainted(false);
            saveButton.setPreferredSize(new Dimension(100,30));
            JButton loadButton = new JButton(" Load grouping ");
            loadButton.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED, Color.gray, new Color(185,185,185), Color.darkGray, Color.darkGray));
            loadButton.setFocusPainted(false);
            loadButton.setPreferredSize(new Dimension(100,30));
            JButton resetButton = new JButton(" Reset ");
            resetButton.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED, Color.gray, new Color(185,185,185), Color.darkGray, Color.darkGray));
            resetButton.setFocusPainted(false);
            resetButton.setPreferredSize(new Dimension(55,30));
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
            constraints.insets = new Insets(0,5,0,5);
            
            buildConstraints(constraints, 0, 0, 1, 1, 33, 100);
            gridbag3.setConstraints(saveButton, constraints);
            panel2.add(saveButton);
            
            buildConstraints(constraints, 1, 0, 1, 1, 33, 0);
            gridbag3.setConstraints(loadButton, constraints);
            panel2.add(loadButton);
            
            buildConstraints(constraints, 2, 0, 1, 1, 34, 0);
            gridbag3.setConstraints(resetButton, constraints);
            panel2.add(resetButton);
            
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
        public void reset(){
            final int finNum = exptNames.size();
            for (int i = 0; i < finNum; i++) {
                groupARadioButtons[i].setSelected(true);
            }
        }
    }
    
    class PValuePanel extends JPanel {
        JRadioButton tDistButton, permutButton, randomGroupsButton, allCombsButton;
        JLabel numCombsLabel;
        JTextField timesField, alphaInputField;
        //JButton permParamButton;
        
        PValuePanel() {
            // this.setBorder(new TitledBorder(new EtchedBorder(), "P-Value parameters"));
            this.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED), "P-Value Parameters", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), Color.black));
            this.setBackground(Color.white);
            GridBagLayout gridbag = new GridBagLayout();
            GridBagConstraints constraints = new GridBagConstraints();
            constraints.anchor = GridBagConstraints.WEST;
            //constraints.fill = GridBagConstraints.BOTH;
            this.setLayout(gridbag);
            
            //permParamButton = new JButton("Permutation parameters");
            //permParamButton.setEnabled(false);
            
            ButtonGroup chooseP = new ButtonGroup();
            
            tDistButton = new JRadioButton("p-values based on t-distribution", true);
            tDistButton.setFocusPainted(false);
            tDistButton.setForeground(UIManager.getColor("Label.foreground"));
            tDistButton.setBackground(Color.white);
            /*
            tDistButton.addActionListener(new ActionListener() {
            public void actionPerformed (ActionEvent evt) {
                    if (evt.getSource() == tDistButton) {
                        permParamButton.setEnabled(false);
                    }
                }
            });
             */
            chooseP.add(tDistButton);
            
            permutButton = new JRadioButton("p-values based on permutation:  ", false);
            permutButton.setFocusPainted(false);
            permutButton.setForeground(UIManager.getColor("Label.foreground"));
            permutButton.setBackground(Color.white);
            /*
            permutButton.addActionListener(new ActionListener() {
            public void actionPerformed (ActionEvent evt) {
                    if (evt.getSource() == permutButton) {
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
                        JOptionPane.showMessageDialog(gPanel, "Group A and Group B must each contain more than one experiment", "Error", JOptionPane.WARNING_MESSAGE);
                    } else {
             
                        //if (pPanel.permutButton.isSelected()) {
                            //System.out.println("grpACounter = " + grpACounter + ", grpBCounter = " + grpBCounter);
                            int numCombs = 0;
                            tooMany = true;
                            if ((grpACounter + grpBCounter) <= 20) {
                                numCombs = getNumCombs((grpACounter + grpBCounter), grpACounter);
                                tooMany = false;
                            }
                    }
                }
            });
             */
            
            //permutButton.setEnabled(false);
            chooseP.add(permutButton);
            /*
            buildConstraints(constraints, 0, 0, 1, 1, 0, 20);
            //constraints.fill = GridBagConstraints.BOTH;
            gridbag.setConstraints(tDistButton, constraints);
            this.add(tDistButton);
             */
            
            /*
            JLabel line1 = new JLabel("___________________________________________________________________________________________________");
            buildConstraints(constraints, 0, 1, 4, 1, 100, 5);
            gridbag.setConstraints(line1, constraints);
            this.add(line1);
             */
            
            
               /*
            buildConstraints(constraints, 0, 2, 1, 1, 0, 15);
            //constraints.fill = GridBagConstraints.BOTH;
            //constraints.anchor = GridBagConstraints.EAST;
            gridbag.setConstraints(permutButton, constraints);
            this.add(permutButton);
                */
            
            /*
            buildConstraints(constraints, 1, 1, 1, 1, 0, 0);
            //constraints.fill = GridBagConstraints.BOTH;
            constraints.anchor = GridBagConstraints.WEST;
            gridbag.setConstraints(permParamButton, constraints);
            this.add(permParamButton);
             */
            
            //constraints.anchor = GridBagConstraints.CENTER;
            
            numCombsLabel = new JLabel("                                       ");
            numCombsLabel.setOpaque(false);
            /*
            constraints.fill = GridBagConstraints.BOTH;
            buildConstraints(constraints, 1, 2, 1, 1, 0, 0);
            gridbag.setConstraints(numCombsLabel, constraints);
            this.add(numCombsLabel);
             */
            
            JPanel panel1 = new JPanel();
            panel1.setBackground(Color.white);
            GridBagLayout grid1 = new GridBagLayout();
            panel1.setLayout(grid1);
            
            buildConstraints(constraints, 0, 0, 1, 1, 30, 50);
            //constraints.fill = GridBagConstraints.BOTH;
            grid1.setConstraints(tDistButton, constraints);
            panel1.add(tDistButton);
            
            buildConstraints(constraints, 0, 1, 1, 1, 30, 50);
            constraints.anchor = GridBagConstraints.WEST;
            grid1.setConstraints(permutButton, constraints);
            panel1.add(permutButton);
            
            buildConstraints(constraints, 1, 1, 1, 1, 70, 0);
            constraints.ipadx = 50;
            constraints.anchor = GridBagConstraints.WEST;
            grid1.setConstraints(numCombsLabel, constraints);
            panel1.add(numCombsLabel);
            
            constraints.ipadx = 100;
            //constraints.ipady = 100;
            
            buildConstraints(constraints, 0, 0, 1, 1, 100, 20);
            //constraints.fill = GridBagConstraints.BOTH;
            constraints.anchor = GridBagConstraints.WEST;
            gridbag.setConstraints(panel1, constraints);
            this.add(panel1);
            
            //DONE UPTO HERE -- 9/18/02
            
            constraints.fill = GridBagConstraints.NONE;
            constraints.anchor = GridBagConstraints.EAST;
            constraints.ipadx = 0;
            constraints.ipady = 0;
            
            JPanel panel2 = new JPanel();
            panel2.setBackground(Color.white);
            GridBagLayout grid2 = new GridBagLayout();
            panel2.setLayout(grid2);
            panel2.setBorder(new EtchedBorder());
            
            
            randomGroupsButton = new JRadioButton("Randomly group experiments ", true);
            randomGroupsButton.setFocusPainted(false);
            randomGroupsButton.setForeground(UIManager.getColor("Label.foreground"));
            randomGroupsButton.setBackground(Color.white);
            randomGroupsButton.setEnabled(false);
            
            allCombsButton = new JRadioButton("Use all permutations                 ", false);
            allCombsButton.setFocusPainted(false);
            allCombsButton.setForeground(UIManager.getColor("Label.foreground"));
            allCombsButton.setBackground(Color.white);
            allCombsButton.setEnabled(false);
            
            
            ButtonGroup chooseAllOrRandom = new ButtonGroup();
            
            buildConstraints(constraints, 0, 0, 1, 1, 60, 50);
            grid2.setConstraints(randomGroupsButton, constraints);
            panel2.add(randomGroupsButton);
            
            timesField = new JTextField("100", 7);
            timesField.setEnabled(false);
            buildConstraints(constraints, 1, 0, 1, 1, 20, 0);
            constraints.anchor = GridBagConstraints.WEST;
            grid2.setConstraints(timesField, constraints);
            panel2.add(timesField);
            
            JLabel timesLabel = new JLabel("times");
            timesLabel.setOpaque(false);
            buildConstraints(constraints, 2, 0, 1, 1, 20, 0);
            grid2.setConstraints(timesLabel, constraints);
            panel2.add(timesLabel);
            
            buildConstraints(constraints, 0, 1, 1, 1, 60, 0);
            constraints.anchor = GridBagConstraints.EAST;
            grid2.setConstraints(allCombsButton, constraints);
            panel2.add(allCombsButton);
            
            chooseAllOrRandom.add(randomGroupsButton);
            chooseAllOrRandom.add(allCombsButton);
            
            allCombsButton.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent evt) {
                    if (evt.getSource() == allCombsButton) {
                        timesField.setText("");
                        timesField.setBackground(Color.gray);
                        timesField.setEnabled(false);
                    }
                }
            });
            
            randomGroupsButton.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent evt) {
                    if (evt.getSource() == randomGroupsButton) {
                        timesField.setText("100");
                        timesField.setBackground(Color.white);
                        timesField.setEnabled(true);
                    }
                }
            });
            
            tDistButton.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent evt) {
                    if (evt.getSource() ==tDistButton) {
                        randomGroupsButton.setEnabled(false);
                        allCombsButton.setEnabled(false);
                        timesField.setEnabled(false);
                        numCombsLabel.setText("                                                                            ");
                    }
                }
            });
            
            buildConstraints(constraints, 0, 1, 1, 1, 100, 60);
            constraints.anchor = GridBagConstraints.NORTH;
            constraints.fill = GridBagConstraints.VERTICAL;
            gridbag.setConstraints(panel2, constraints);
            this.add(panel2);
            /*
            JLabel line2 = new JLabel("___________________________________________________________________________________________________");
            buildConstraints(constraints, 0, 5, 4, 1, 100, 5);
            gridbag.setConstraints(line2, constraints);
            this.add(line2);
             */
            
            constraints.fill = GridBagConstraints.NONE;
            
            JPanel panel3 = new JPanel();
            panel3.setBackground(Color.white);
            GridBagLayout grid3 = new GridBagLayout();
            panel3.setLayout(grid3);
            
            JLabel alphaLabel = new JLabel("Overall alpha (critical p-value):                   ");
            alphaLabel.setOpaque(false);
            buildConstraints(constraints, 0, 0, 1, 1, 75, 100);
            constraints.anchor = GridBagConstraints.WEST;
            //constraints.fill = GridBagConstraints.BOTH;
            grid3.setConstraints(alphaLabel, constraints);
            panel3.add(alphaLabel);
            
            alphaInputField = new JTextField("0.01", 7);
            buildConstraints(constraints, 1, 0, 1, 1, 25, 0);
            constraints.anchor = GridBagConstraints.WEST;
            grid3.setConstraints(alphaInputField, constraints);
            panel3.add(alphaInputField);
            
            buildConstraints(constraints, 0, 2, 1, 1, 0, 20);
            gridbag.setConstraints(panel3, constraints);
            this.add(panel3);
            
            /*
            JButton pButton = new JButton("pValuePanel");
            buildConstraints(constraints, 0, 0, 1, 1, 100, 100);
            constraints.fill = GridBagConstraints.BOTH;
            gridbag.setConstraints(pButton, constraints);
            this.add(pButton);
             */
        }
    }
    
    class SignificancePanel extends JPanel {
        JRadioButton minPButton, maxTButton, justAlphaButton, stdBonfButton, adjBonfButton;
        SignificancePanel() {
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
            stdBonfButton.setFocusPainted(false);
            stdBonfButton.setForeground(UIManager.getColor("Label.foreground"));
            stdBonfButton.setBackground(Color.white);
            sigGroup.add(stdBonfButton);
            
            adjBonfButton = new JRadioButton("adjusted Bonferroni correction", false);
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
    
    class OkCancelPanel extends JPanel {
        JButton okButton, cancelButton;
        JCheckBox drawTreesBox;
        OkCancelPanel() {
            this.setBorder(new TitledBorder(new EtchedBorder(), "General"));
            GridBagLayout gridbag = new GridBagLayout();
            GridBagConstraints constraints = new GridBagConstraints();
            this.setLayout(gridbag);
            
            drawTreesBox = new JCheckBox("Draw hierarchical trees", false);
            buildConstraints(constraints, 0, 0, 2, 1, 100, 50);
            //constraints.fill = GridBagConstraints.BOTH;
            gridbag.setConstraints(drawTreesBox, constraints);
            this.add(drawTreesBox);
            
            okButton = new JButton("OK");
            buildConstraints(constraints, 0, 1, 1, 1, 50, 50);
            //constraints.fill = GridBagConstraints.BOTH;
            gridbag.setConstraints(okButton, constraints);
            this.add(okButton);
            
            cancelButton = new JButton("Cancel");
            buildConstraints(constraints, 1, 1, 1, 1, 50, 0);
            //constraints.fill = GridBagConstraints.BOTH;
            gridbag.setConstraints(cancelButton, constraints);
            this.add(cancelButton);
            
            /*
            JButton oButton = new JButton("OkCancelPanel");
            buildConstraints(constraints, 0, 0, 1, 1, 100, 100);
            constraints.fill = GridBagConstraints.BOTH;
            gridbag.setConstraints(oButton, constraints);
            this.add(oButton);
             */
        }
    }
    
    /*
    class NumPermutationsDialog extends ActionInfoDialog {
        JRadioButton allCombs, randomCombs;
        JTextField numCombsInputField;
        JButton okay, cancel;
        boolean okayPressed = false;
        //int numCombs;
        public NumPermutationsDialog(JFrame parentFrame, boolean modality, final int numCombs, boolean tooMany) {
            super(parentFrame, "Number of permutations", modality);
            //Listener listener = new Listener();
            //addWindowListener(listener);
            //this.numCombs = numCombs;
            setBounds(0, 0, 500, 200);
            //setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            GridBagLayout gridbag = new GridBagLayout();
            GridBagConstraints constraints = new GridBagConstraints();
            JPanel pane = new JPanel();
            pane.setLayout(gridbag);
     
            String numCombsString = "";
     
            if(tooMany) {
                numCombsString = "There are too many unique ways of grouping experiments";
            } else {
                numCombsString = "There are " + numCombs + " unique ways of grouping experiments";
            }
     
            JLabel numCombsLabel = new JLabel(numCombsString);
            buildConstraints(constraints, 0, 0, 2, 1, 0, 25);
            gridbag.setConstraints(numCombsLabel, constraints);
            pane.add(numCombsLabel);
     
            ButtonGroup choosePermutOption = new ButtonGroup();
     
            allCombs = new JRadioButton("     Use all combinations", false);
            if (tooMany) {
                allCombs.setEnabled(false);
            }
            randomCombs = new JRadioButton("Randomly group experiments: ", true);
            numCombsInputField = new JTextField("100", 7);
            numCombsInputField.setBackground(Color.white);
            numCombsInputField.setEnabled(true);
     
     
            allCombs.addActionListener(new ActionListener(){
                public void actionPerformed (ActionEvent evt) {
                    if (evt.getSource() == allCombs) {
                        numCombsInputField.setText("");
                        numCombsInputField.setBackground(Color.gray);
                        numCombsInputField.setEnabled(false);
                    }
                }
            });
     
            randomCombs.addActionListener(new ActionListener(){
                public void actionPerformed (ActionEvent evt) {
                    if (evt.getSource() == randomCombs) {
                        numCombsInputField.setText("100");
                        numCombsInputField.setBackground(Color.white);
                        numCombsInputField.setEnabled(true);
                    }
                }
            });
     
     
            choosePermutOption.add(allCombs);
            choosePermutOption.add(randomCombs);
     
            buildConstraints(constraints, 0, 1, 2, 1, 0, 25);
            gridbag.setConstraints(randomCombs, constraints);
            pane.add(randomCombs);
     
            buildConstraints(constraints, 0, 2, 1, 1, 50, 25);
            constraints.anchor = GridBagConstraints.EAST;
            gridbag.setConstraints(numCombsInputField, constraints);
            pane.add(numCombsInputField);
     
            JLabel timesLabel = new JLabel("  times");
            buildConstraints(constraints, 1, 2, 1, 1, 50, 0);
            constraints.anchor = GridBagConstraints.WEST;
            gridbag.setConstraints(timesLabel, constraints);
            pane.add(timesLabel);
     
            constraints.anchor = GridBagConstraints.CENTER;
     
            buildConstraints(constraints, 0, 3, 2, 1, 0, 25);
            gridbag.setConstraints(allCombs, constraints);
            pane.add(allCombs);
     
            okay = new JButton("OK");
     
            okay.addActionListener(new ActionListener() {
                  public void actionPerformed (ActionEvent evt) {
                      boolean validated = false;
                  if (evt.getSource() == okay) {
                    if (allCombs.isSelected()) {
                        userNumCombs = numCombs;
                        validated = true;
                        allCombsUsed = true;
                    } else {
                        try {
                           allCombsUsed = false;
                           String s1 = numCombsInputField.getText();
                           if (Integer.parseInt(s1) < 1) {
                               JOptionPane.showMessageDialog(null, "Number of times must be a positive integer", "Error", JOptionPane.WARNING_MESSAGE);
                               userNumCombs = 100;
                               validated = false;
                           } else {
                                userNumCombs =  Integer.parseInt(s1);
                                validated = true;
                           }
                        } catch (NumberFormatException nfe) {
                           JOptionPane.showMessageDialog(null, "Number of times must be a positive integer", "Error", JOptionPane.WARNING_MESSAGE);
                           userNumCombs = 100;
                           validated = false;
                        }
                    }
     
                    System.out.println("userNumCombs = " + userNumCombs);
                    if (validated) {
                        permParamOkPressed = true;
                        oPanel.okButton.setEnabled(true);
                        hide();
                        dispose();
                    }
                  }
                }
             });
            buildConstraints(constraints, 0, 4, 1, 1, 0, 25);
            gridbag.setConstraints(okay, constraints);
            pane.add(okay);
     
            cancel = new JButton("Cancel");
            cancel.addActionListener(new ActionListener() {
                public void actionPerformed (ActionEvent evt) {
                if (evt.getSource() == cancel) {
                    permParamOkPressed = false;
                    oPanel.okButton.setEnabled(false);
                    hide();
                    dispose();
                 }
               }
            });
            //
            cancel.addActionListener(new ActionListener() {
            public void actionPerformed (ActionEvent evt) {
                if (evt.getSource() == cancel) {
                    okayPressed = false;
                    hide();
                    dispose();
                }
            }
        });
           //
            buildConstraints(constraints, 1, 4, 1, 1, 0, 0);
            gridbag.setConstraints(cancel, constraints);
            pane.add(cancel);
     
     
     
     
            setContentPane(pane);
     
        }
     
        public void setVisible(boolean visible) {
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);
     
            super.setVisible(visible);
     
                    if (visible) {
                            okay.requestFocus(); //UNCOMMMENT THIS LATER
                    }
            }
     
    }
     */
    
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
    
    
    public boolean isOkPressed() {
        return okPressed;
    }
    
    public boolean isDrawTrees() {
        return hclOpsPanel.isHCLSelected();
    }
    
    public int getTestDesign() {
        int design = -1;
        if (chooseDesignPane.getSelectedIndex() == 0) {
            design = TtestInitDialog.ONE_CLASS;
        } else if (chooseDesignPane.getSelectedIndex() == 1){
            design = TtestInitDialog.BETWEEN_SUBJECTS;
        }
        return design;
    }
    
    public int[] getGroupAssignments() {
        int[] groupAssignments = new int[exptNames.size()];
        for (int i = 0; i < exptNames.size(); i++) {
            if (gPanel.groupARadioButtons[i].isSelected()) {
                groupAssignments[i] = GROUP_A;
            } else if (gPanel.groupBRadioButtons[i].isSelected()) {
                groupAssignments[i] = GROUP_B;
            } else {
                groupAssignments[i] = NEITHER_GROUP;
            }
        }
        
        return groupAssignments;
    }
    
    public boolean isPermut() {
        return pPanel.permutButton.isSelected();
    }
    
    public int getUserNumCombs() {
        String s1 = pPanel.timesField.getText();
        int num = 0;
        if (!useAllCombs()) {
            num = Integer.parseInt(s1);
        } else {
            if (getTestDesign() == TtestInitDialog.BETWEEN_SUBJECTS) {
                num = allPossCombs;
            } else if (getTestDesign() == TtestInitDialog.ONE_CLASS) {
                num = (int)Math.pow(2, getNumValidOneClassExpts());
            }
        }
        return num;
    }
    
    public double getAlphaValue() {
        String s1 = pPanel.alphaInputField.getText();
        
        return Double.parseDouble(s1);
    }
    
    public int[] getOneClassAssignments() {
        int[] oneClassAssignments = new int[oPanel.includeExpts.length];
        
        for (int i = 0; i < oneClassAssignments.length; i++) {
            if (oPanel.includeExpts[i].isSelected()) {
                oneClassAssignments[i] = 1;
            } else {
                oneClassAssignments[i] = 0;
            }
        }
        
        return oneClassAssignments;
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
    
    public double getOneClassMean() {
        return Double.parseDouble(oPanel.meanField.getText());
    }
    
    
    public boolean validateAlpha(String alpha){
        float a;
        try{
            a = Float.parseFloat(alpha);
        } catch (NumberFormatException e){
            JOptionPane.showMessageDialog(TtestInitDialog.this, "Alpha value is not a valid input value.", "Input Error", JOptionPane.WARNING_MESSAGE);
            pPanel.alphaInputField.requestFocus();
            pPanel.alphaInputField.selectAll();
            return false;
        }
        if(a <= 0 || a >= 1){
            JOptionPane.showMessageDialog(TtestInitDialog.this, "Alpha value must be > 0 and < 1.0", "Input Error", JOptionPane.WARNING_MESSAGE);
            pPanel.alphaInputField.requestFocus();
            pPanel.alphaInputField.selectAll();
            return false;
        }
        return true;
    }
    
    public boolean validatePermutations(String n){
        int i;
        try{
            i = Integer.parseInt(n);
        } catch (NumberFormatException e){
            JOptionPane.showMessageDialog(TtestInitDialog.this, "Number of Permutations is not a valid input value.", "Input Error", JOptionPane.WARNING_MESSAGE);
            pPanel.timesField.requestFocus();
            pPanel.timesField.selectAll();
            return false;
        }
        if(i <= 0){
            JOptionPane.showMessageDialog(TtestInitDialog.this, "Number of Permutations must be > 0", "Input Error", JOptionPane.WARNING_MESSAGE);
            pPanel.timesField.requestFocus();
            pPanel.timesField.selectAll();
            return false;
        }
        return true;
    }
    
    
    public class EventListener implements ActionListener{
        
        public void actionPerformed(ActionEvent ae){
            
            String command = ae.getActionCommand();
            
            if(command.equals("ok-command")){
                if (getTestDesign() == TtestInitDialog.BETWEEN_SUBJECTS) {
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
                        JOptionPane.showMessageDialog(gPanel, "Group A and Group B must each contain more than one experiment", "Error", JOptionPane.WARNING_MESSAGE);
                    } else {
                        String alpha = pPanel.alphaInputField.getText();

                        float a;
                        if(pPanel.permutButton.isSelected() && pPanel.randomGroupsButton.isSelected()){
                            String iter = pPanel.timesField.getText();
                            if(!validatePermutations(iter)){
                                okPressed = false;
                                return;                            
                            }
                        }                    
                        if(!validateAlpha(alpha)){
                        okPressed = false;
                            return;
                        }                                                                                    
                        okPressed = true;
                        hide();
                        dispose();
                    }
                } else if (getTestDesign() == TtestInitDialog.ONE_CLASS) {
                    try {
                        float mean = Float.parseFloat(oPanel.meanField.getText());
                    } catch (NumberFormatException nfe) {
                        JOptionPane.showMessageDialog(oPanel, "Invalid value entered for mean", "Error", JOptionPane.WARNING_MESSAGE);
                        okPressed = false;
                        return;
                    }
                    
                    if (getNumValidOneClassExpts() < 2) {
                        JOptionPane.showMessageDialog(oPanel, "Select at least two experiments", "Error", JOptionPane.WARNING_MESSAGE);
                        okPressed = false;
                        return;      
                    }
                    
                    String alpha = pPanel.alphaInputField.getText();
                    
                    float a;
                    if(pPanel.permutButton.isSelected() && pPanel.randomGroupsButton.isSelected()){
                        String iter = pPanel.timesField.getText();
                        if(!validatePermutations(iter)){
                            okPressed = false;
                            return;
                        }
                    }
                    if(!validateAlpha(alpha)){
                        okPressed = false;
                        return;
                    } 
                    
                    okPressed = true;
                    hide();
                    dispose();                    

                }
            }
            else if(command.equals("reset-command")){
                if (getTestDesign() == TtestInitDialog.BETWEEN_SUBJECTS) {
                    gPanel.reset();
                } else if (getTestDesign() == TtestInitDialog.ONE_CLASS) {
                    oPanel.reset();
                }

                pPanel.tDistButton.setSelected(true);
                pPanel.randomGroupsButton.setEnabled(false);
                pPanel.allCombsButton.setEnabled(false);
                pPanel.timesField.setEnabled(false);
                pPanel.timesField.setBackground(Color.white);
                pPanel.timesField.setText("100");
                pPanel.numCombsLabel.setText("                                                                            ");
                pPanel.alphaInputField.setText("0.01");
                sPanel.justAlphaButton.setSelected(true);
                hclOpsPanel.setHCLSelected(false);

            }
            else if(command.equals("cancel-command")){
                okPressed = false;
                setVisible(false);
                dispose();
            }
            else if(command.equals("info-command")){
                HelpWindow helpWindow = new HelpWindow(TtestInitDialog.this, "TTEST Initialization Dialog");
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
    
    public int getSignificanceMethod() {
        if (sPanel.justAlphaButton.isSelected()) {
            return this.JUST_ALPHA;
        } else if (sPanel.stdBonfButton.isSelected()) {
            return this.STD_BONFERRONI;
        } else if (sPanel.adjBonfButton.isSelected()){
            return this.ADJ_BONFERRONI;
        } else if (sPanel.maxTButton.isSelected()) {
            return this.MAX_T;
        } else {
            return this.MIN_P;
        }
    }
    
    public boolean useAllCombs() {
        return pPanel.allCombsButton.isSelected();
    }
    
    public static void main(String[] args) {
        
        
        
        JFrame dummyFrame = new JFrame();
        Vector nameVector = new Vector();
        
        for (int i = 0; i < 10; i++) {
            nameVector.add("Exp " + i);
        }
        TtestInitDialog  tDialog= new TtestInitDialog(dummyFrame, true, nameVector);

        for (int i = 0; i < 50; i++) {
            System.out.println("2^" + i + " = " + (int)Math.pow(2, i));
        }
        
        tDialog.setVisible(true);
        
        System.exit(0);
    }
    
}
