/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: RelNetSelectionDlg.java,v $
 * $Revision: 1.1.1.2 $
 * $Date: 2004-02-06 21:48:18 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.rn;

import java.awt.Frame;
import java.awt.Color;
import java.awt.Toolkit;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Container;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.border.BevelBorder;

import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener;

public class RelNetSelectionDlg extends JDialog {

    public static final int DEGREE_TYPE = 0;
    public static final int GENEID_TYPE = 1;
    public static final String CONDITION_GREATER_THAN = "Greater than";
    public static final String CONDITION_EQUAL_TO     = "Equal to";
    public static final String CONDITION_LESS_THAN    = "Less than";
    public static final String CONDITION_LIKE         = "Like";
    public static final String CONDITION_BETWEEN      = "Between";

    private int result;

    private int type;
    private JComboBox conditionCombo;
    private JPanel degreePanel;
    private JComboBox  degreeCombo_1;
    private JComboBox  degreeCombo_2;
    private JTextField geneidField;

    /**
     * Construct the dialog with specified type.
     */
    public RelNetSelectionDlg(Frame frame, int type) {
        super(frame, "Select Nodes", true);
        this.type = type;

        Listener listener = new Listener();

        this.degreeCombo_1 = createDegreeCombo_1();
        this.degreeCombo_2 = createDegreeCombo_2();

        JPanel selectionPanel = createSelectionPanel(listener, type);
        JPanel btnsPanel = createBtnsPanel(listener);

        Container content = getContentPane();
        content.setLayout(new GridBagLayout());
        content.add(selectionPanel, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
                                                           ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        content.add(btnsPanel, new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0
                                                      ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(4, 8, 4, 8), 0, 0));
        addWindowListener(listener);
        pack();
    }

    /**
     * Creates the main panel.
     */
    private JPanel createSelectionPanel(ActionListener listener, int type) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new BevelBorder(BevelBorder.RAISED));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // labels...
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx  = 0;
        gbc.gridy  = 0;
        panel.add(new JLabel("Condition:"), gbc);
        gbc.gridx  = 0;
        gbc.gridy  = 1;
        switch (type) {
        case DEGREE_TYPE:
            panel.add(new JLabel("Degree:"), gbc);
            break;
        case GENEID_TYPE:
            panel.add(new JLabel("Element ID:"), gbc);
            break;
        }
        // fields...
        this.conditionCombo = createConditionCombo(listener, type);
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.gridx  = 1;
        gbc.gridy  = 0;
        panel.add(conditionCombo, gbc);

        gbc.gridx  = 1;
        gbc.gridy  = 1;

        switch (type) {
        case DEGREE_TYPE:
            degreePanel = createDegreePanel();
            panel.add(degreePanel, gbc);
            break;
        case GENEID_TYPE:
            geneidField = new JTextField();
            panel.add(geneidField, gbc);
            break;
        }
        return panel;
    }

    /**
     * Creates a combo with a type specific list of conditions.
     */
    private JComboBox createConditionCombo(ActionListener listener, int type) {
        JComboBox combo = new JComboBox();
        switch (type) {
        case DEGREE_TYPE:
            combo.addItem(CONDITION_GREATER_THAN);
            combo.addItem(CONDITION_EQUAL_TO);
            combo.addItem(CONDITION_LESS_THAN);
            combo.addItem(CONDITION_BETWEEN);
            break;
        case GENEID_TYPE:
            combo.addItem(CONDITION_EQUAL_TO);
            combo.addItem(CONDITION_LIKE);
            break;
        }
        combo.setActionCommand("condition-changed");
        combo.addActionListener(listener);
        return combo;
    }

    /**
     * Creates a combo for first degree parameter.
     */
    private JComboBox createDegreeCombo_1() {
        JComboBox combo = new JComboBox();
        combo.setEditable(true);
        for (int i=0; i < 10; i++) {
            combo.addItem(String.valueOf(i));
        }
        return combo;
    }

    /**
     * Creates a combo for second degree parameter.
     */
    private JComboBox createDegreeCombo_2() {
        JComboBox combo = new JComboBox();
        combo.setEditable(true);
        for (int i= 10; --i >= 0;) {
            combo.addItem(String.valueOf(i));
        }
        return combo;
    }

    /**
     * Creates a panel with a degree combo.
     */
    private JPanel createDegreePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        panel.add(this.degreeCombo_1, gbc);
        return panel;
    }

    /**
     * Updates a degree panel.
     */
    private void updateDegreePanel(boolean two_combo) {
        this.degreePanel.removeAll();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        if (two_combo) {
            gbc.gridx = gbc.gridy = 0;
            gbc.weightx = 0.5;
            this.degreePanel.add(this.degreeCombo_1, gbc);
            gbc.gridx = 1;
            gbc.insets.left = 5;
            this.degreePanel.add(this.degreeCombo_2, gbc);
        } else {
            gbc.weightx = 1.0;
            this.degreePanel.add(this.degreeCombo_1, gbc);
        }
        this.degreePanel.validate();
        this.degreePanel.repaint();
    }

    /**
     * Creates a panel with 'ok' and 'cancel' buttons.
     */
    private JPanel createBtnsPanel(ActionListener listener) {
        GridLayout gridLayout = new GridLayout();
        JPanel panel = new JPanel(gridLayout);

        JButton okButton = new JButton("OK");
        okButton.setActionCommand("ok-command");
        okButton.addActionListener(listener);
        panel.add(okButton);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setActionCommand("cancel-command");
        cancelButton.addActionListener(listener);
        gridLayout.setHgap(4);
        panel.add(cancelButton);

        getRootPane().setDefaultButton(okButton);

        return panel;
    }

    /**
     * Shows the dialog.
     */
    public int showModal() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);
        show();
        return result;
    }

    /**
     * Returns a condition.
     */
    public String getCondition() {
        return(String)conditionCombo.getSelectedItem();
    }

    /**
     * Returns a gene id.
     */
    public String getGeneID() {
        return geneidField.getText();
    }

    /**
     * Returns a degree value.
     */
    private int getDegree() {
        return Integer.parseInt((String)degreeCombo_1.getSelectedItem());
    }

    /**
     * Returns min degree value.
     */
    public int getMinDegree() {
        if (!getCondition().equals(CONDITION_BETWEEN)) {
            return getDegree();
        }
        return Math.min(Integer.parseInt((String)degreeCombo_1.getSelectedItem()), Integer.parseInt((String)degreeCombo_2.getSelectedItem()));
    }

    /**
     * Returns max degree value.
     */
    public int getMaxDegree() {
        if (!getCondition().equals(CONDITION_BETWEEN)) {
            return getDegree();
        }
        return Math.max(Integer.parseInt((String)degreeCombo_1.getSelectedItem()), Integer.parseInt((String)degreeCombo_2.getSelectedItem()));
    }

    /**
     * Updates the degree panel if it is necessary.
     */
    private void onConditionChanged() {
        if (this.type == DEGREE_TYPE)
            if (getCondition().equals(CONDITION_BETWEEN))
                updateDegreePanel(true);
            else
                updateDegreePanel(false);
    }

    /**
     * The class to listen to dialog events.
     */
    private class Listener extends DialogListener {

        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            if (command.equals("ok-command")) {
                try {
                    switch (type) {
                    case DEGREE_TYPE:
                        Integer.parseInt((String)degreeCombo_1.getSelectedItem());
                        if (getCondition().equals(CONDITION_BETWEEN)) {
                            Integer.parseInt((String)degreeCombo_2.getSelectedItem());
                        }
                        break;
                    }
                    result = JOptionPane.OK_OPTION;  
                } catch (Exception exception) {
                    result = JOptionPane.CANCEL_OPTION;
                }
                dispose();
            } else if (command.equals("cancel-command")) {
                result = JOptionPane.CANCEL_OPTION;
                dispose();
            } else if (command.equals("condition-changed")) {
                onConditionChanged();
            }
        }

        public void windowClosing(WindowEvent e) {
            result = JOptionPane.CLOSED_OPTION;
            dispose();
        }
    }

    /*public static void main(String[] args) {
        javax.swing.JFrame frame = new javax.swing.JFrame("Test");
        final int type = DEGREE_TYPE; //GENEID_TYPE;
        while (true) {
            RelNetSelectionDlg dialog = new RelNetSelectionDlg(frame, type);
            if (dialog.showModal() != JOptionPane.OK_OPTION) {
                System.exit(0);
            }
            switch (type) {
            case DEGREE_TYPE:
                System.out.println(dialog.getCondition());
                System.out.println(dialog.getDegree());
                break;
            case GENEID_TYPE:
                System.out.println(dialog.getCondition());
                System.out.println(dialog.getGeneID());
                break;
            }
        }
    }*/
}
