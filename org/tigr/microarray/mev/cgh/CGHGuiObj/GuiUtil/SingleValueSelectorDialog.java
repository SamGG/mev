/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * $RCSfile: SingleValueSelectorDialog.java,v $
 * $Revision: 1.2 $
 * $Date: 2006-02-03 14:36:59 $
 * $Author: raktim $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cgh.CGHGuiObj.GuiUtil;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import org.tigr.util.awt.GBA;

/**
 * @author Adam Margolin
 * @author Raktim Sinha
 */

public class SingleValueSelectorDialog extends JDialog {
    private int result;
    private Dimension size;
    private JLabel label;

    private JTextField txtValue;
    private GBA gba;

    public SingleValueSelectorDialog(Frame parent, String value) {
        super(parent, true);

        gba = new GBA();
        label = new JLabel("Value: ");
        txtValue = new JTextField(10);
        txtValue.setText(value);

        JButton okButton = new JButton("Okay");
        okButton.setActionCommand("ok-command");
        okButton.addActionListener(new Listener());
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setActionCommand("cancel-command");
        cancelButton.addActionListener(new Listener());

        Container content = getContentPane();
        content.setLayout(new GridBagLayout());
        gba.add(content, label, 0, 0, 1, 1, 0, 0, GBA.NONE, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
        gba.add(content, txtValue, 1, 0, 1, 1, 1, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
        gba.add(content, cancelButton, 0, 2, 1, 1, 0, 0, GBA.NONE, GBA.W, new Insets(5, 5, 5, 5), 0, 0);
        gba.add(content, okButton, 1, 2, 1, 1, 0, 0, GBA.NONE, GBA.E, new Insets(5, 5, 5, 5), 0, 0);

        setResizable(false);
        txtValue.grabFocus();
        getRootPane().setDefaultButton(okButton);
        pack();
    }

    public int showModal() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);
        show();
        return result;
    }

    public String getValue(){
        return txtValue.getText();
    }

    private class Listener extends WindowAdapter implements ActionListener {

        public void actionPerformed(ActionEvent event) {
            String command = event.getActionCommand();
            if (command.equals("ok-command")) {
                result = JOptionPane.OK_OPTION;
                dispose();
            } else if (command.equals("cancel-command")) {
                result = JOptionPane.CANCEL_OPTION;
                dispose();
            }
        }

        public void windowClosing(WindowEvent e) {
            result = JOptionPane.CLOSED_OPTION;
            dispose();
        }
    }

}