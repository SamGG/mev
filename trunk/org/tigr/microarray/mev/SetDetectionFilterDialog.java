/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
 
This software is provided "AS IS".  TIGR makes no warranties, express
or implied, including no representation or warranty with respect to
the performance of the software and derivatives or their safety,
effectiveness, or commercial viability.  TIGR does not warrant the
merchantability or fitness of the software and derivatives for any
particular purpose, or that they may be exploited without infringing
the copyrights, patent rights or property rights of others. TIGR shall
not be liable for any claim, demand or action for any loss, harm,
illness or other damage or injury arising from access to or use of the
software or associated information, including without limitation any
direct, indirect, incidental, exemplary, special or consequential
damages.
 
This software program may not be sold, leased, transferred, exported
or otherwise disclaimed to anyone, in whole or in part, without the
prior written consent of TIGR.
 */
/*
 * $RCSfile: SetDetectionFilterDialog.java,v $
 * $Revision: 1.1.1.1 $
 * $Date: 2003-08-21 21:04:24 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.microarray.mev;


import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Toolkit;
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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import java.awt.Component;
import javax.swing.BoxLayout;

import javax.swing.border.*;
import javax.swing.JOptionPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JCheckBox;
import javax.swing.JButton;
import javax.swing.JDialog;
//import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import javax.swing.JScrollPane;

import org.tigr.microarray.mev.cluster.gui.impl.dialogs.*;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.*;
//import javax.swing.SpinnerNumberModel;

import org.tigr.util.awt.GBA;

public class SetDetectionFilterDialog extends AlgorithmDialog {
    
    private GroupExperimentsPanel gPanel;
    private JButton filter, cancel;
    private JLabel msg1Label, msg2Label, msg3Label, tail1Label, tail2Label;
    //private JTextField grp_1_field, grp_2_field;
    private int result;
    private DetectionFilter df;
    protected boolean use_filter;
    
    
    
    public SetDetectionFilterDialog(JFrame parent, String[] sample_names) {
        super(parent, "Set Detection Filter", true);
        
        setBounds(0, 0, 800, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        JPanel pane = new JPanel();
        pane.setBackground(Color.white);
        pane.setBorder(BorderFactory.createLineBorder(Color.black));
        pane.setLayout(new BorderLayout(2,2));
        Listener listener = new Listener();
        // filter = new JButton("OK");
        //  filter.setActionCommand("ok-command");
        //filter.addActionListener(listener);
        //  cancel = new JButton("Cancel");
        //  cancel.setActionCommand("cancel-command");
        //  cancel.addActionListener(listener);
        
        
        String msg1 = "Group A: Gene must be called (P)resent in:";
        String msg2 = "Group B: Gene must be called (P)resent in:";
        String msg3 = "Select one of the following: ";
        
        String tail1 = "out of the total in Group A";
        String tail2 = "out of the total in Group B";
        String[] selection_options = {"OR", "AND"};
        
        msg1Label = new JLabel(msg1);
        msg2Label = new JLabel(msg2);
        msg3Label = new JLabel(msg3);
        tail1Label = new JLabel(tail1);
        tail2Label = new JLabel(tail2);
        
        /*
        SpinnerNumberModel model1 = new SpinnerNumberModel(1, 0, 100, 1);
        SpinnerNumberModel model2 = new SpinnerNumberModel(1, 0, 100, 1);
         
        model1.addChangeListener(new VoteListener(0, 1));
        model2.addChangeListener(new VoteListener(1, 0));
         
        JSpinner spinner1 = new JSpinner(model1);
        JSpinner spinner2 = new JSpinner(model2);
         */
        
        JTextField vote1 = new JTextField("1", 2);
        JTextField vote2 = new JTextField("1", 2);
        
        vote1.addKeyListener( new VoteKeyListener() );
        vote2.addKeyListener( new VoteKeyListener() );
        vote1.addFocusListener( new VoteFocusListener(0,1) );
        vote2.addFocusListener( new VoteFocusListener(1,1) );
        
        
        JList use_both = new JList(selection_options);
        use_both.setBorder(BorderFactory.createLineBorder(Color.black));
        use_both.setBackground(Color.lightGray);
        use_both.setVisibleRowCount(2);
        
        use_both.addListSelectionListener(new SelectionListener());
        
        //  getContentPane().add(pane);
        
        gPanel = new GroupExperimentsPanel(sample_names);
        gPanel.setBackground(Color.white);
        
        this.df = new DetectionFilter(sample_names);
        int count;
        
        for (count = 0; count < gPanel.groupARadioButtons.length; count++) {
            gPanel.groupARadioButtons[count].addActionListener(new GroupListener(0, count));
        }
        
        for (count = 0; count < gPanel.groupBRadioButtons.length; count++) {
            gPanel.groupBRadioButtons[count].addActionListener(new GroupListener(1, count));
        }
        
        pane.add(gPanel, BorderLayout.CENTER);
        
        JPanel bottom_panel = new JPanel();
        bottom_panel.setLayout(new GridLayout(3,1));
        
        JPanel sub1 = new JPanel();
        sub1.setBackground(Color.white);
        JPanel sub2 = new JPanel();
        sub2.setBackground(Color.white);
        JPanel sub3 = new JPanel();
        sub3.setBackground(Color.white);
        //   JPanel sub4 = new JPanel();
        
        sub1.setLayout(new FlowLayout(FlowLayout.LEFT));
        sub2.setLayout(new FlowLayout(FlowLayout.LEFT));
        sub3.setLayout(new FlowLayout(FlowLayout.LEFT));
        //      sub4.setLayout(new FlowLayout(FlowLayout.RIGHT));
        
        sub1.add(msg1Label);
        //sub1.add(spinner1);
        sub1.add(vote1);
        sub1.add(tail1Label);
        
        // And Or selectionsub2.add()
        sub2.add(msg3Label);
        sub2.add(use_both);
        
        sub3.add(msg2Label);
        //sub3.add(spinner2);
        sub3.add(vote2);
        sub3.add(tail2Label);
        
        //  sub4.add(filter);
        //  sub4.add(cancel);
        
        bottom_panel.add(sub1);
        bottom_panel.add(sub2);
        bottom_panel.add(sub3);
        //     bottom_panel.add(sub4);
        
        pane.add(bottom_panel, BorderLayout.SOUTH);
        this.addContent(pane);
        this.setActionListeners(listener);
        
        addWindowListener(listener);
        pack();
    }
    
    public static void main(String [] args){
        String [] names = new String[40];
            for(int i = 0; i < names.length; i++)
                names[i] = "name"+Integer.toString(i);
        SetDetectionFilterDialog d = new SetDetectionFilterDialog(new JFrame(), names);
        d.showModal();
    }
    
    public SetDetectionFilterDialog(JFrame parent, String[] sample_names, DetectionFilter df){
        this(parent, sample_names);
        this.df = df;
    }
    
    
    public int showModal() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);
        show();
        return result;
    }
    
    public DetectionFilter getDetectionFilter() {
        //System.out.println(df.toString());
        return this.df;
    }
    
    private class Listener extends WindowAdapter implements ActionListener, KeyListener, ItemListener {
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
        
        public void itemStateChanged(ItemEvent is_checked){
            if (is_checked.getStateChange() == ItemEvent.SELECTED) {
                use_filter = true;
            }
            else {
                use_filter = false;
            }
        }
        
        private void onOk() {
            try {
                getDetectionFilter();//etectionCheckbox.getState();
                result = JOptionPane.OK_OPTION;
            } catch (Exception exception) {
                result = JOptionPane.CANCEL_OPTION;
            }
            dispose();
        }
    }
    
/*
    class VoteListener implements ChangeListener {
        // (P) calls required for each group
        private int required;
        private int group_index;
        VoteListener(int group, int req){
            this.required = req;
            this.group_index = group;
        }
 
        public void stateChanged(ChangeEvent e){
            SpinnerNumberModel m = (SpinnerNumberModel) e.getSource();
            df.set_num_required(group_index, m.getNumber().intValue() );
            //System.out.println("Num required: " + m.getNumber().intValue());
        }
 
    }
 */
    
    // verifies numeric input for vote textfield
    class VoteKeyListener extends KeyAdapter {
        
        //keyText.addKeyListener(
        VoteKeyListener() {
            super();
        }
        
        public void keyTyped(KeyEvent e) {
            char c = e.getKeyChar();
            if (! ( (Character.isDigit(c) || (c == KeyEvent.VK_BACK_SPACE) || (c == KeyEvent.VK_DELETE))) ){
                getToolkit().beep();
                e.consume();
            }
        }
    }
    
    class VoteFocusListener extends FocusAdapter {
        private int required;
        private int group_index;
        VoteFocusListener(int group, int req){
            this.required = req;
            this.group_index = group;
        }
        public void focusLost(FocusEvent e) {
            JTextField textField = (JTextField)e.getSource();
            String content = textField.getText();
            df.set_num_required(group_index, Integer.parseInt(content));
        }
    }
    
    class SelectionListener implements ListSelectionListener {
        public void valueChanged(ListSelectionEvent event){
            if (!event.getValueIsAdjusting()){
                JList optionList = (JList) event.getSource();
                String selection = (String) optionList.getSelectedValue();
                if (selection.equals("AND")){
                    df.set_both(true);
                    //System.out.println("AND");
                    
                }
                else{
                    df.set_both(false);
                    //System.out.println("OR");
                }
            }
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
            //Change df state
            //System.out.println("file_index: "+file_index);
            //System.out.println("group: " + group);
            //if (evt.getSource() == gPanel.groupBRadioButtons[file_index]) {
            
            if (!(df.get_group_membership(file_index) == group) ){
                df.set_group_membership(group, file_index);
                // System.out.println("switched to ???");
                // add code to dynamically change total in message below
            }
            //}
        }
    }
    
    class GroupExperimentsPanel extends JPanel {
        
        JRadioButton[] groupARadioButtons, groupBRadioButtons;
        GroupExperimentsPanel(String[] exptNames) {
            this.setBorder(new TitledBorder(new EtchedBorder(), "Group Assignments", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), Color.black));
            JPanel panel1 = new JPanel();
            JLabel[] expLabels = new JLabel[exptNames.length];
            groupARadioButtons = new JRadioButton[exptNames.length];
            groupBRadioButtons = new JRadioButton[exptNames.length];
            //neitherGroupRadioButtons = new JRadioButton[exptNames.length];
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
            }
            
            JScrollPane scroll = new JScrollPane(panel1);
            // scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
            //scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
            scroll.setPreferredSize(new Dimension(400, 300));
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