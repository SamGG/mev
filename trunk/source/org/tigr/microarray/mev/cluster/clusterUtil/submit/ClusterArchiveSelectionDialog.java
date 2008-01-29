/*
Copyright @ 1999-2004, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * $RCSfile: ClusterArchiveSelectionDialog.java,v $
 * $Revision: 1.3 $
 * $Date: 2005-03-10 15:48:03 $
 * $Author: braistedj $
 * $State: Exp $
 */
/*
 * ClusterArchiveSelectionDialog.java
 *
 * Created on June 25, 2004, 10:09 AM
 */

package org.tigr.microarray.mev.cluster.clusterUtil.submit;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.event.ChangeListener;

import org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.HelpWindow;

/**
 *
 * @author  braisted
 */
public class ClusterArchiveSelectionDialog extends AlgorithmDialog {
    
    private RepositoryConfigParser parser;
    private RepositoryPane repPane;
    int result = JOptionPane.CANCEL_OPTION;
    
    /** Creates a new instance of ClusterArchiveSelectionDialog */
    public ClusterArchiveSelectionDialog(RepositoryConfigParser parser) {
        super(new JFrame(), "Cluster Archive Selection Dialog", true);
        this.parser = parser;
        
        okButton.setText("Submit");
        okButton.setEnabled(false);
        validate();
        repPane = new RepositoryPane();        
        addContent(repPane);
        setActionListeners(new Listener());
    }
    
    /** Creates a new instance of ClusterArchiveSelectionDialog */
    public ClusterArchiveSelectionDialog() {
        super(new JFrame(), "Cluster Archive Selection Dialog", true);
        JPanel panel = new JPanel();
        repPane = new RepositoryPane();
        panel.add(repPane);
        //       setContent(new RepositoryPane());
        pack();
    }
    
    
    /**
     * Shows the dialog.
     */
    public int showModal() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setSize((int)(screenSize.width/2), (int)(screenSize.height/1.6));
        setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);
        show();
        return result;
    }
    
    private void resetControls() {
        repPane.setSelectedIndex(0);
    }
    
    /** Returns the selected repository name
     */
    public String getSelectedRepositoryName() {
        return repPane.getSelectedRepositoryName();
    }
    
    
    private class RepositoryPane extends JTabbedPane {
        
        String [] repNames;
        
        public RepositoryPane() {
            super();            //get names
            // String [] repNames = parser.getRepositoryNames();
            
            //construct intro panel
            this.addTab("Introduction", constructIntroPanel());
            this.addChangeListener(new Listener());
            
            //get description for each pane
            repNames = parser.getRepositoryNames();
            
            for(int i = 0; i < repNames.length; i++) {
                this.addTab(repNames[i], new RepositoryPanel(repNames[i]));
            }
            
            
        }
        
        public String getSelectedRepositoryName() {
            return getTitleAt(getSelectedIndex());
        }
        
        
    }
    
    private JPanel constructIntroPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createLineBorder(Color.black));
        JTextPane pane = new JTextPane();
        pane.setEditable(false);
        pane.setMargin(new Insets(10,15,10,15));
        pane.setContentType("text/html");
        String text = new String();
        text += "<html><h2>External Cluster Repository Submission</h2><hr size=3>";
        text += "Welcome to MeV's cluster submission process.  Select a repository using the ";
        text += "tabbed panes in this dialog. ";
        text += "Information on each page will indicate the suitability of a repository for your gene list. ";
        text += "After selection of a repository, hit submit to be lead through the submission process. ";
        text += "<br><br>";
        text += "For first time submissions it might be required to register at the repostitory web site and ";
        text += "to use the user name and password during the cluster submission (See manual for details)</html>";
        
        pane.setText(text);
        panel.add(pane, new GridBagConstraints(0,0,1,1,1.0,1.0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));
        return panel;
    }
    
    /*
    public static void main(String [] args) {
        ClusterArchiveSelectionDialog dialog = new ClusterArchiveSelectionDialog(null);
        dialog.showModal();
    }
     */
    
    private class RepositoryPanel extends JPanel {
        
        private String repositoryName;
        
        public RepositoryPanel(String repName) {
            super(new GridBagLayout());
            repositoryName = repName;
            constructContent(repName);
        }
        
        private void constructContent(String repName) {
            String page = parser.getRepositoryPage(repName);
            
            JTextPane pane = new JTextPane();
            pane.setEditable(false);
            pane.setMargin(new Insets(10,15,10,15));
            pane.setContentType("text/html");
            
            pane.setText(page);
            JScrollPane scroll = new JScrollPane(pane);
            add(scroll, new GridBagConstraints(0,0,1,1,1.0,1.0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));            
            pane.setCaretPosition(0);
        }
        
    }
    /**
     * The class to listen to the dialog and check boxes items events.
     */
    private class Listener extends DialogListener implements ChangeListener {
        
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            if (command.equals("ok-command")) {
                result = JOptionPane.OK_OPTION;
                dispose();
            } else if (command.equals("cancel-command")) {
                result = JOptionPane.CANCEL_OPTION;
                dispose();
            }
            else if (command.equals("reset-command")){
                resetControls();
                result = JOptionPane.CANCEL_OPTION;
                return;
            }
            else if (command.equals("info-command")){
                HelpWindow hw = new HelpWindow(ClusterArchiveSelectionDialog.this, "Cluster Archive Submission");
                result = JOptionPane.CANCEL_OPTION;
                if(hw.getWindowContent()){
                    hw.setSize(450,600);
                    hw.setLocation();
                    hw.show();
                    return;
                }
                else {
                    hw.setVisible(false);
                    hw.dispose();
                    return;
                }
            }
            dispose();
        }
        
        public void windowClosing(WindowEvent e) {
            result = JOptionPane.CLOSED_OPTION;
            dispose();
        }
        
        public void stateChanged(javax.swing.event.ChangeEvent changeEvent) {
            okButton.setEnabled(repPane.getSelectedIndex() != 0);
        }
        
    }
    
    
}
