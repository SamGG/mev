/*
Copyright @ 1999-2002, The Institute for Genomic Research (TIGR).
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
 * $RCSfile: ListDialog.java,v $
 * $Revision: 1.1.1.2 $
 * $Date: 2004-02-06 21:48:18 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.dialogs;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class ListDialog extends JDialog {

    /**
     * Constructs the dialog with specified title and info to be 
     * inserted into a list.
     */
    public ListDialog(Frame frame, String title, Object[] info) {
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
        scroll.setPreferredSize(new Dimension(300, 200));
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
