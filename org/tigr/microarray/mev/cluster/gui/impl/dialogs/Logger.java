/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: Logger.java,v $
 * $Revision: 1.1.1.1 $
 * $Date: 2003-08-21 21:04:25 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.dialogs;

import java.awt.Frame;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Insets;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

import org.tigr.microarray.mev.cluster.gui.impl.GUIFactory;

public class Logger extends JDialog {
    
    private JTextArea log = new JTextArea(10, 30);
    
    /**
     * Constructs a <code>Logger</code> with specified parent frame, title
     * and dialog listener.
     */
    public Logger(Frame parent, String title, DialogListener listener) {
	super(parent, title);
	JPanel logPanel = createLogPanel();
	JPanel btnsPanel = createBtnsPanel(listener);
	
	Container content = getContentPane();
	content.setLayout(new GridBagLayout());
	content.add(logPanel, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
	,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
	content.add(btnsPanel, new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0
	,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(4, 8, 4, 8), 0, 0));
	
	addWindowListener(listener);
	pack();
    }
    
    /**
     * Shows the dialog.
     */
    public void show() {
	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);
	super.show();
    }
    
    /**
     * Appends a specified string to the end of log.
     */
    public void append(String text) {
	log.append(text);
	log.setCaretPosition(log.getDocument().getLength());
    }
    
    /**
     * Creates a log panel.
     */
    private JPanel createLogPanel() {
	JPanel panel = new JPanel(new GridBagLayout());
	
	panel.setBorder(new EmptyBorder(10, 10, 10, 10));
	log.setTabSize(3);
	log.setEditable(false);
	log.setAutoscrolls(true);
	
	JScrollPane scroll = new JScrollPane(log);
	
	GridBagConstraints gbc = new GridBagConstraints();
	panel.add(new JLabel(GUIFactory.getIcon("dialog_banner2.gif")), gbc);
	gbc.gridy = 1;
	gbc.weightx = 1.0;
	gbc.weighty = 1.0;
	gbc.fill = GridBagConstraints.BOTH;
	
	panel.add(scroll, gbc);
	return panel;
    }
    
    /**
     * Creates a panel with cancel button.
     */
    private JPanel createBtnsPanel(ActionListener listener) {
	JPanel panel = new JPanel(new BorderLayout());
	JButton cancelButton = new JButton(" Cancel ");
        cancelButton.setMargin(new java.awt.Insets(5,5,5,5));
	cancelButton.setActionCommand("cancel-command");
	cancelButton.addActionListener(listener);
        cancelButton.setFocusPainted(false);
        cancelButton.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
	panel.add(cancelButton, BorderLayout.CENTER);
	getRootPane().setDefaultButton(cancelButton);
	return panel;
    }
    
}
