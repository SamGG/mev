/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
package org.tigr.microarray.mev;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;

import org.tigr.microarray.mev.resources.SupportFileAccessError;

public class ShowThrowableDialog extends JDialog {

    private JComponent message;
    private JComponent stack;
    private JPanel mainPanel;

    /**
     * Constructs the dialog with specified title, modal flag, type to display 
     * throwable object.
     */
    private ShowThrowableDialog(Frame frame, String title, boolean modal, int type, Throwable t, String friendlyMessage) {
        super(frame, title, modal);
        Listener listener = new Listener();
        addWindowListener(listener);

        if(friendlyMessage == null || friendlyMessage.equals(""))
            this.message = createMessageContent(type, t);
        else
        	this.message = new JEditorPane("text/html", friendlyMessage);
        
        this.stack   = createStackContent(t);

        this.mainPanel = createMainPanel();
        this.mainPanel.add(this.message, BorderLayout.CENTER);
        JPanel btnsPanel = createBtnsPanel(listener);

        getContentPane().setLayout(new GridBagLayout());
        getContentPane().add(mainPanel, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
                                                               ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        getContentPane().add(btnsPanel, new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0
                                                               ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(4, 8, 4, 8), 0, 0));
        pack();
    }

    /**
     * Displays the dialog with specified title.
     */
    public static void show(Frame frame, String title, Throwable t) {
        show(frame, title, true, t);
    }

    /**
     * Displays the dialog with specified title and modal state.
     */
    public static void show(Frame frame, String title, boolean modal, Throwable t) {
        show(frame, title, modal, JOptionPane.ERROR_MESSAGE, t);
    }

    /**
     * Displays the dialog with specified title, modal state and message type.
     */
    public static void show(Frame frame, String title, boolean modal, int type, Throwable t, String friendlyMessage) {
        ShowThrowableDialog dlg = new ShowThrowableDialog(frame, title, modal, type, t, friendlyMessage);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        dlg.setLocation((screenSize.width - dlg.getSize().width)/2, (screenSize.height - dlg.getSize().height)/2);
        dlg.setVisible(true);
    }
    public static void show(Frame frame, String title, boolean modal, int type, Throwable t) {
    	show(frame, title, modal, type, t, null);   
    }

    /**
     * Returns the icon to use for the passed in type.
     */
    private Icon getIconForType(int messageType) {
        if (messageType < 0 || messageType > 3)
            return null;
        switch (messageType) {
        case 0:
            return UIManager.getIcon("OptionPane.errorIcon");
        case 1:
            return UIManager.getIcon("OptionPane.informationIcon");
        case 2:
            return UIManager.getIcon("OptionPane.warningIcon");
        case 3:
            return UIManager.getIcon("OptionPane.questionIcon");
        }
        return null;
    }

    /**
     * Creates a component to render message content.
     */
    private JComponent createMessageContent(int type, Throwable t) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        panel.add(new JLabel(getIconForType(type)), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;

        gbc.insets.left = 20;
        panel.add(new JLabel(t.getMessage()), gbc);
        return panel;
    }

    /**
     * Returns a throwable object stack trace.
     */
    private String getTrace(Throwable t) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(stream);
        t.printStackTrace(ps);
        return stream.toString();
    }

    /**
     * Creates a component to render stack trace.
     */
    private JComponent createStackContent(Throwable t) {
        JTextArea area = new JTextArea(getTrace(t));
        area.setEditable(false);
        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(250, 150));
        return scroll;
    }

    /**
     * Sets main panel content.
     */
    private void setContent(JComponent content) {
        this.mainPanel.removeAll();
        this.mainPanel.add(content, BorderLayout.CENTER);
        this.mainPanel.validate();
        this.mainPanel.repaint();
    }

    /**
     * Creates the main panel.
     */
    private JPanel createMainPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new BevelBorder(BevelBorder.RAISED));
        panel.setPreferredSize(new Dimension(400, 100));
        return panel;
    }

    /**
     * Creates a panel with 'OK' and 'Stack' buttons.
     */
    private JPanel createBtnsPanel(ActionListener listener) {
        GridLayout gridLayout = new GridLayout();
        JPanel panel = new JPanel(gridLayout);

        JButton okButton = new JButton("OK");
        okButton.setActionCommand("ok-command");
        okButton.addActionListener(listener);
        panel.add(okButton);

        JToggleButton stackButton = new JToggleButton("Stack");
        stackButton.setActionCommand("stack-command");
        stackButton.addActionListener(listener);
        gridLayout.setHgap(4);
        panel.add(stackButton);

        getRootPane().setDefaultButton(okButton);

        return panel;
    }

    /**
     * Sets message or stack content to be displayed.
     */
    private void onStackTrace(boolean flag) {
        if (flag) {
            setContent(this.stack);
        } else {
            setContent(this.message);
        }
    }

    /**
     * Listener to listen to window and action events.
     */
    private class Listener extends WindowAdapter implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            if (command.equals("ok-command")) {
                dispose();
            } else if (command.equals("stack-command")) {
                AbstractButton button = (AbstractButton)e.getSource();
                onStackTrace(button.isSelected());
            }
        }

        public void windowClosing(WindowEvent e) {
            dispose();
        }
    }
    /**
     * Main class for testing. Creates a new dialog and shows it. 
     * @param args
     */
    public static void main(String[] args) {
	    IOException ioe = new IOException();
	    SupportFileAccessError sfae = new SupportFileAccessError("testing, testing", ioe);
	    	ShowThrowableDialog.show(new JFrame(), "Test ShowThrowableDialog", new Exception("First line is really really really really really really really really really really really really really really really really really really really really really really really really really long."));
	    	ShowThrowableDialog.show(new JFrame(), "Test ShowThrowableDialog", sfae);
	    	ShowThrowableDialog.show(new JFrame(), "Test ShowThrowableDialog", true, 1, sfae, "friendlymessage");
    	System.exit(0);
    }
}
