/*
 * GroupExperimentsPanel.java
 *
 * Created on November 22, 2003, 4:36 AM
 */

package org.tigr.microarray.mev.cgh.CGHGuiObj.GuiUtil;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.tigr.util.StringSplitter;

/**
 *
 * @author  Adam Margolin
 */
public class GroupExperimentsPanel extends JPanel {
    public static final int GROUP_A = 1;
    public static final int GROUP_B = 2;
    public static final int NEITHER_GROUP = 3;

    GroupExperimentsPanel gPanel;
    Vector exptNames;
    JLabel[] expLabels;
    JRadioButton[] groupARadioButtons, groupBRadioButtons, neitherGroupRadioButtons;
    public GroupExperimentsPanel(Vector exptNames) {
        gPanel = this;
        this.exptNames = exptNames;
        this.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED), "Group Assignments", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), Color.black));
        this.setBackground(Color.white);
        JPanel panel1 = new JPanel();
        // panel1.setBackground(Color.white);
        expLabels = new JLabel[exptNames.size()];
        groupARadioButtons = new JRadioButton[exptNames.size()];
        groupBRadioButtons = new JRadioButton[exptNames.size()];
        neitherGroupRadioButtons = new JRadioButton[exptNames.size()];
        //ButtonGroup chooseGroup[] = new ButtonGroup[exptNames.size()];
        ButtonGroup chooseGroup[] = new ButtonGroup[2];
        chooseGroup[0] = new ButtonGroup();
        chooseGroup[1] = new ButtonGroup();
        
        GridBagLayout gridbag = new GridBagLayout();
        GridBagLayout gridbag2 = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();
        this.setLayout(gridbag2);
        panel1.setLayout(gridbag);
        
        for (int i = 0; i < exptNames.size(); i++) {
            String s1 = (String)(exptNames.get(i));//permut
            expLabels[i] = new JLabel(s1);
            expLabels[i].setForeground(Color.black);
            
            groupARadioButtons[i] = new JRadioButton("Group A", false);
            
            groupBRadioButtons[i] = new JRadioButton("Group B", false);
            
            //neitherGroupRadioButtons[i] = new JRadioButton("Neither group", false);
            
            buildConstraints(constraints, 0, i, 1, 1, 25, 100);
            
            gridbag.setConstraints(expLabels[i], constraints);
            panel1.add(expLabels[i]);
            
            buildConstraints(constraints, 1, i, 1, 1, 25, 100);
            gridbag.setConstraints(groupARadioButtons[i], constraints);
            panel1.add(groupARadioButtons[i]);
            
            buildConstraints(constraints, 2, i, 1, 1, 25, 100);
            gridbag.setConstraints(groupBRadioButtons[i], constraints);
            panel1.add(groupBRadioButtons[i]);
            
            //buildConstraints(constraints, 3, i, 1, 1, 25, 100);
            //gridbag.setConstraints(neitherGroupRadioButtons[i], constraints);
            //panel1.add(neitherGroupRadioButtons[i]);
            
            //chooseGroup[i] = new ButtonGroup();
            //chooseGroup[i].add(groupARadioButtons[i]);
            //chooseGroup[i].add(groupBRadioButtons[i]);
            //chooseGroup[i].add(neitherGroupRadioButtons[i]);
            chooseGroup[0].add(groupARadioButtons[i]);
            chooseGroup[1].add(groupBRadioButtons[i]);
        }
        
        if(exptNames.size() > 0){
            groupARadioButtons[0].setSelected(true);
        }
        if(exptNames.size() > 1){
            groupBRadioButtons[1].setSelected(true);
        }
        
        JScrollPane scroll = new JScrollPane(panel1);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scroll.setBorder(BorderFactory.createLineBorder(Color.black,2));
        
        buildConstraints(constraints, 0, 0, 1, 1, 100, 90);
        constraints.fill = GridBagConstraints.BOTH;
        gridbag2.setConstraints(scroll, constraints);
        this.add(scroll);
        
        /*
        JLabel label1 = new JLabel("                                                Note: Group A and Group B  MUST each contain more than one experiment.");
        buildConstraints(constraints, 0, 1, 1, 1, 0, 5);
        constraints.anchor = GridBagConstraints.EAST;
        gridbag2.setConstraints(label1, constraints);
        this.add(label1);
        
         */
        
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
        //this.add(panel2);
        
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
    
    void buildConstraints(GridBagConstraints gbc, int gx, int gy,
    int gw, int gh, int wx, int wy) {
        
        gbc.gridx = gx;
        gbc.gridy = gy;
        gbc.gridwidth = gw;
        gbc.gridheight = gh;
        gbc.weightx = wx;
        gbc.weighty = wy;
    }
    
    public int[] getGroupAssignments() {
        int[] groupAssignments = {-1, -1};
        
        for (int i = 0; i < exptNames.size(); i++) {
            if (gPanel.groupARadioButtons[i].isSelected()) {
                groupAssignments[0] = i;
            }if (gPanel.groupBRadioButtons[i].isSelected()) {
                groupAssignments[1] = i;
            }
        }
        
        /*
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
         */
        
        return groupAssignments;
    }
}
