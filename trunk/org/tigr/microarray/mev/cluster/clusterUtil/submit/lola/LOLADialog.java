/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * $RCSfile: LOLADialog.java,v $
 * $Revision: 1.1 $
 * $Date: 2004-07-22 15:29:12 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.clusterUtil.submit.lola;

import java.awt.Color;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.*;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.awt.event.TextEvent;
import java.awt.event.TextListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.JColorChooser;
import javax.swing.BorderFactory;
import javax.swing.border.LineBorder;
import javax.swing.border.BevelBorder;
import javax.swing.BorderFactory;
import javax.swing.border.*;
import javax.swing.JOptionPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JCheckBox;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JTextField;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import javax.swing.JScrollPane;

import org.tigr.util.awt.GBA;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.*;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.*;

import org.tigr.microarray.mev.cluster.gui.impl.dialogs.ParameterPanel;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.HelpWindow;

import org.tigr.microarray.mev.cluster.clusterUtil.*;

public class LOLADialog extends AlgorithmDialog {
    private GroupExperimentsPanel gPanel;
    private JButton lola_it, cancel;
    int result = JOptionPane.CANCEL_OPTION;
    //   private Cluster cluster;
    
    //jcb mod
    String [] sample_names;
    int [] sample_groupings;
    
    public LOLADialog(String frameTitle, String[] sample_names, Cluster cluster) {
        super(new JFrame(), frameTitle, true);
        this.sample_names = sample_names;
        this.sample_groupings = new int[sample_names.length];
        // this.cluster = cluster;
        
        setBounds(0, 0, 800, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        JPanel pane = new JPanel();
        pane.setBackground(Color.white);
        pane.setBorder(BorderFactory.createLineBorder(Color.black));
        pane.setLayout(new BorderLayout(1,2));
        Listener listener = new Listener();
        
        gPanel = new GroupExperimentsPanel(sample_names);
        
        int count;
        for (count = 0; count < gPanel.groupARadioButtons.length; count++) {
            gPanel.groupARadioButtons[count].addActionListener(new GroupListener(0, count));
        }
        
        for (count = 0; count < gPanel.groupBRadioButtons.length; count++) {
            gPanel.groupBRadioButtons[count].addActionListener(new GroupListener(1, count));
        }
        
        // should add another column of buttons for neither group
        
        pane.add(gPanel, BorderLayout.CENTER);
        
        addContent(pane);
        setActionListeners(listener);
        
        addWindowListener(listener);
    }
    
    //jcb mod to remove group assignment information from the cluster
    public int [] getGroupAssignments() {
        return sample_groupings;
    }
    
    public int showModal() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);
        show();
        return result;
    }
    
    private class Listener extends WindowAdapter implements ActionListener, KeyListener {
        public void actionPerformed(ActionEvent event) {
            String command = event.getActionCommand();
            if (command.equals("ok-command")) {
                onOk();
            } else if (command.equals("cancel-command")) {
                result = JOptionPane.CANCEL_OPTION;
                dispose();
            }
        }
        
        public void keyPressed(KeyEvent event) {
            if (event.getKeyCode() == KeyEvent.VK_ENTER) {
                onOk();
            }
        }
        
        public void windowClosing(WindowEvent e) {
            result = JOptionPane.CLOSED_OPTION;
            dispose();
        }
        
        public void keyReleased(KeyEvent event) {;}
        public void keyTyped(KeyEvent event) {;}
        
        
        
        private void onOk() {
            try {
                //getFoldFilter();//etectionCheckbox.getState();
                //String[] sn = cluster.getSampleNames();
                //for (int i = 0; i < sn.length; i++){
                //    System.out.println("Sample: "+sn[i]+" in group: " + cluster.get_grouping(i));
                //}
                result = JOptionPane.OK_OPTION;
            } catch (Exception exception) {
                result = JOptionPane.CANCEL_OPTION;
            }
            dispose();
        }
    }
    
    class GroupListener implements ActionListener {
        int file_index;
        int group;
        GroupListener(int group, int file_index){
            this.group = group;
            this.file_index = file_index;
        }
        public void actionPerformed(ActionEvent evt) {
            // original
            //  if (!(cluster.get_grouping(file_index) == group) ){
            //       cluster.set_grouping(file_index, group);
            //   }
            sample_groupings[file_index] = group;
        }
    }
    
    class GroupExperimentsPanel extends JPanel {
        
        JRadioButton[] groupARadioButtons, groupBRadioButtons, neitherGroupRadioButtons;
        GroupExperimentsPanel(String[] exptNames) {
            this.setBorder(new TitledBorder(new EtchedBorder(), "Group Assignments", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), Color.black));
            JPanel panel1 = new JPanel();
            JLabel[] expLabels = new JLabel[exptNames.length];
            groupARadioButtons = new JRadioButton[exptNames.length];
            groupBRadioButtons = new JRadioButton[exptNames.length];
            neitherGroupRadioButtons = new JRadioButton[exptNames.length];
            
            ButtonGroup chooseGroup[] = new ButtonGroup[exptNames.length];
            
            GridBagLayout gridbag = new GridBagLayout();
            GridBagLayout gridbag2 = new GridBagLayout();
            GridBagConstraints constraints = new GridBagConstraints();
            this.setLayout(gridbag2);
            panel1.setLayout(gridbag);
            
            for (int i = 0; i < exptNames.length; i++) {
                String s1 = (String)(exptNames[i]);
                expLabels[i] = new JLabel(s1);
                chooseGroup[i] = new ButtonGroup();
                groupARadioButtons[i] = new JRadioButton("Group A", true);
                chooseGroup[i].add(groupARadioButtons[i]);
                groupBRadioButtons[i] = new JRadioButton("Group B", false);
                chooseGroup[i].add(groupBRadioButtons[i]);
                
                neitherGroupRadioButtons[i] = new JRadioButton("Neither", false);
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
            // scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
            //scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
            scroll.setPreferredSize(new Dimension(400, 450));
            buildConstraints(constraints, 0, 0, 1, 1, 100, 90);
            constraints.fill = GridBagConstraints.BOTH;
            gridbag2.setConstraints(scroll, constraints);
            this.add(scroll);
            
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
        
    }
}





