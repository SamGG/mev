/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: RelNetClusterList.java,v $
 * $Revision: 1.2 $
 * $Date: 2005-02-24 20:23:44 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.rn;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener;

public class RelNetClusterList extends JDialog {

    /**
     * Constructs the dialog with specified title and info to be 
     * inserted into a list.
     */
    public RelNetClusterList(Frame frame, String title, Object[] info) {
        super(frame, title, true);
        Listener listener = new Listener();
        addWindowListener(listener);

        JPanel listPanel = createListPanel(info);
        JPanel btnsPanel = createBtnsPanel(listener);

        getContentPane().setLayout(new GridBagLayout());
        getContentPane().add(listPanel, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
                                                                    ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        getContentPane().add(btnsPanel, new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0
                                                               ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(4, 8, 4, 8), 0, 0));
        pack();
    }

    /**
     * Creates a panel with JList.
     * @param info the array of objects to inserted into a list.
     */
    private JPanel createListPanel(Object[] info) {
        JPanel panel = new JPanel(new BorderLayout());
        JList list = new JList(info);
        JScrollPane scroll = new JScrollPane(list);
        scroll.setPreferredSize(new Dimension(250, 150));
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Creates a panel with 'ok' button.
     */
    private JPanel createBtnsPanel(ActionListener listener) {
        JPanel panel = new JPanel();

        JButton okButton = new JButton("OK");
        okButton.setActionCommand("ok-command");
        okButton.addActionListener(listener);
        panel.add(okButton);

        getRootPane().setDefaultButton(okButton);

        return panel;
    }

    /**
     * Shows the dialog.
     */
    public void showModal() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);
        show();
    }

    /**
     * The class to listen to action and window events.
     */
    private class Listener extends DialogListener {

        public void actionPerformed(ActionEvent e) {
            dispose();
        }

        public void windowClosing(WindowEvent e) {
            dispose();
        }
    }

}
