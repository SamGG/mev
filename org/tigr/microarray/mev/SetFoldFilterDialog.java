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
 * $RCSfile: SetFoldFilterDialog.java,v $
 * $Revision: 1.2 $
 * $Date: 2005-02-24 20:23:44 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.microarray.mev;


import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.*;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.KeyAdapter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import javax.swing.BorderFactory;
import javax.swing.border.*;
import javax.swing.JOptionPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import javax.swing.JScrollPane;

import org.tigr.microarray.mev.cluster.gui.impl.dialogs.*;

public class SetFoldFilterDialog extends AlgorithmDialog {
    
    private GroupExperimentsPanel gPanel;
    private JButton filter, cancel;
    private JLabel msg1Label, msg2Label, msg3Label, msg4Label;
    private int result;
    private FoldFilter ff;
    private String BOTH = "both";
    private String GREATER_THAN = ">";
    private String LESS_THAN = "<";
    protected boolean use_filter;
    
    public SetFoldFilterDialog(JFrame parent, String[] sample_names) {
        super(parent, "Set Fold Filter", true);
        
        setBounds(0, 0, 800, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        JPanel pane = new JPanel();
        pane.setBackground(Color.white);
        pane.setBorder(BorderFactory.createLineBorder(Color.black));
        pane.setLayout(new BorderLayout(2,2));
        Listener listener = new Listener();
        // filter = new JButton("OK");
        // filter.setActionCommand("ok-command");
        //filter.addActionListener(listener);
        // cancel = new JButton("Cancel");
        // cancel.setActionCommand("cancel-command");
        // cancel.addActionListener(listener);
        
        
        String msg1 = "Genes in Group A must be ";
        String msg2 = "than in Group B by ";
        String msg3 = " fold.";
        String msg4 = "\'both' will keep ALL genes whose fold change is greater than the threshold";
        
        String[] selection_options = {GREATER_THAN, LESS_THAN, BOTH};
        
        msg1Label = new JLabel(msg1);
        msg2Label = new JLabel(msg2);
        msg3Label = new JLabel(msg3);
        msg4Label = new JLabel(msg4);
        
        JTextField fold_change = new JTextField("2.0", 3);
        
        fold_change.addKeyListener( new VoteKeyListener() );
        fold_change.addFocusListener( new VoteFocusListener(0,1) );
        

        JList divider_list = new JList(selection_options);
        divider_list.setBorder(BorderFactory.createLineBorder(Color.black));
        divider_list.setBackground(Color.lightGray);
        divider_list.setVisibleRowCount(3);
        
        divider_list.addListSelectionListener(new SelectionListener());
        
        //getContentPane().add(pane);
        
        gPanel = new GroupExperimentsPanel(sample_names);
        
        this.ff = new FoldFilter(sample_names);
        int count;
        
        for (count = 0; count < gPanel.groupARadioButtons.length; count++) {
            gPanel.groupARadioButtons[count].addActionListener(new GroupListener(0, count));
        }
        
        for (count = 0; count < gPanel.groupBRadioButtons.length; count++) {
            gPanel.groupBRadioButtons[count].addActionListener(new GroupListener(1, count));
        }
        
        // should add another column of buttons for neither group
        
        pane.add(gPanel, BorderLayout.CENTER);
        
        JPanel bottom_panel = new JPanel();
        bottom_panel.setLayout(new GridLayout(2,1));
        
        JPanel sub1 = new JPanel();
        sub1.setBackground(Color.white);
        JPanel sub2 = new JPanel();
        sub2.setBackground(Color.white);
        // JPanel sub3 = new JPanel();
        // JPanel sub4 = new JPanel();
        
        sub1.setLayout(new FlowLayout(FlowLayout.LEFT));
        sub2.setLayout(new FlowLayout(FlowLayout.LEFT));
        //sub3.setLayout(new FlowLayout(FlowLayout.RIGHT));
        
        sub1.add(msg1Label);
        sub1.add(divider_list);
        sub1.add(msg2Label);
        sub1.add(fold_change);
        sub1.add(msg3Label);
        
        sub2.add(msg4Label);
        
        //sub3.add(filter);
        //sub3.add(cancel);
        
        bottom_panel.add(sub1);
        bottom_panel.add(sub2);
       // bottom_panel.add(sub3);
        
        pane.add(bottom_panel, BorderLayout.SOUTH);
        
        addContent(pane);
        setActionListeners(listener);
        
        addWindowListener(listener);
    }
    
    
    public static void main(String [] args){
        String [] names = new String[40];
        for(int i = 0; i < names.length; i++)
            names[i] = "name"+Integer.toString(i);
        SetFoldFilterDialog d = new SetFoldFilterDialog(new JFrame(), names);
        d.showModal();
    }
    
    
    public SetFoldFilterDialog(JFrame parent, String[] sample_names, FoldFilter ff){
        this(parent, sample_names);
        this.ff = ff;
    }
    
    
    public int showModal() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);
        show();
        return result;
    }
    
    public FoldFilter getFoldFilter() {
        //System.out.println(df.toString());
        return this.ff;
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
                getFoldFilter();//etectionCheckbox.getState();
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
            if (! ( (Character.isDigit(c) || (c == KeyEvent.VK_BACK_SPACE) || (c == KeyEvent.VK_PERIOD)  || (c == KeyEvent.VK_DELETE))) ){
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
            ff.set_fold_change(Float.parseFloat(content));
        }
    }
    
    class SelectionListener implements ListSelectionListener {
        public void valueChanged(ListSelectionEvent event){
            if (!event.getValueIsAdjusting()){
                JList optionList = (JList) event.getSource();
                String selection = (String) optionList.getSelectedValue();
                if (selection.equals(GREATER_THAN)){
                    ff.set_divider(GREATER_THAN);
                }
                if (selection.equals(LESS_THAN)){
                    ff.set_divider(LESS_THAN);
                }
                if(selection.equals(BOTH)){
                    ff.set_divider(BOTH);
                    
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
            if (!(ff.get_group_membership(file_index) == group) ){
                ff.set_group_membership(group, file_index);
            }
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
