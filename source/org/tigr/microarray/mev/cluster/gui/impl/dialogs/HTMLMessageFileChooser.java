/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * MessageFileChooser.java
 *
 * Created on January 10, 2005, 10:31 AM
 */

package org.tigr.microarray.mev.cluster.gui.impl.dialogs;

/**
 *
 * @author  braisted
 */

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.filechooser.FileFilter;

public class HTMLMessageFileChooser extends JDialog {
    
    private JFileChooser chooser;
    private String approveString;
    private int result = JOptionPane.CANCEL_OPTION;
    private JPanel msgPanel;
    
    /** Creates a new instance of MessageFileChooser */
    public HTMLMessageFileChooser(Frame parent, String title, String msg, String filePath, boolean modal) {
        super(parent, title, modal);
        chooser = new JFileChooser(filePath);
        chooser.setMultiSelectionEnabled(false);        
        chooser.addActionListener(new Listener());
        
        JTextPane pane = new JTextPane();
        pane.setEditable(false);
        pane.setContentType("text/html");
        pane.setBackground(Color.white);
       
        pane.setText(msg);
        
        msgPanel = new JPanel(new GridBagLayout());
        msgPanel.add(pane, new GridBagConstraints(0,0,1,1,1,1,GridBagConstraints.NORTH, GridBagConstraints.BOTH, new Insets(0,5,10,5), 0,0));
        msgPanel.setBackground(Color.white);
        msgPanel.setBorder(BorderFactory.createLineBorder(Color.black));
        
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.add(msgPanel, new GridBagConstraints(0,0,1,1,1,1,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,5,5,5), 0,0));
        contentPanel.add(chooser, new GridBagConstraints(0,1,1,1,1,1,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));        
        
        this.setContentPane(contentPanel);
        pack();
    }
            
    public HTMLMessageFileChooser(Frame parent, String title, String msg, File file, boolean modal) {
        super(parent, title, modal);
        chooser = new JFileChooser(file);
        chooser.setMultiSelectionEnabled(false);       
        chooser.addActionListener(new Listener());
        
        JTextPane pane = new JTextPane();
        pane.setEditable(false);
        pane.setContentType("text/html");
        pane.setBackground(Color.white);
       
        pane.setText(msg);
        
        JPanel msgPanel = new JPanel(new GridBagLayout());
        msgPanel.add(pane, new GridBagConstraints(0,0,1,1,1,1,GridBagConstraints.NORTH, GridBagConstraints.BOTH, new Insets(0,5,10,5), 0,0));
        msgPanel.setBackground(Color.white);
        msgPanel.setBorder(BorderFactory.createLineBorder(Color.black));
        
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.add(msgPanel, new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,5,5,5), 0,0));
        contentPanel.add(chooser, new GridBagConstraints(0,1,1,1,1,1,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));        
        
        this.setContentPane(contentPanel);
        pack();        
    }
    
    public int showModal() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);        
        this.setVisible(true);
        return result;
    }

    public File getSelectedFile() {
        return chooser.getSelectedFile();
    }
    
    public JFileChooser getFileChooser() {
        return chooser;
    }
    
    public void setFileFilter(FileFilter filter) {
        this.chooser.setFileFilter(filter);
    }
    
    public void setApproveButtonText(String s) {
        chooser.setApproveButtonText(s);
        approveString = s;
    }
    
    public void setPreferredMessageSize(Dimension dim) {
        this.msgPanel.setSize(dim);
        this.msgPanel.setPreferredSize(dim);
    }
    
    private class Listener implements ActionListener {
        
        public void actionPerformed(ActionEvent ae) {
            String cmd = ae.getActionCommand();
 
            if (cmd.equals(JFileChooser.APPROVE_SELECTION)) {
                result = JFileChooser.APPROVE_OPTION;
                dispose();
            } else {
               result = JFileChooser.CANCEL_OPTION;                
               dispose();                
            }
        }        
    }
    
    public static void main(String [] args) {
        
        String msg = "<html><center><h1>Append Sample Annotation</h1></center>";
        msg += "A sample annotation file can be selected for import so that alternative labels or ";
        msg += "additional sample information can be appended to the exsiting sample annotation.";
        msg += "The file should be a tab-delimited text file containing one header row for annotation labels.";
        msg += "The file may contain multiple columns of annotation with each column containing a header.";
        msg += "The annotation for each sample is organized in rows corresponding to the order of the loaded samples.";
        msg += "If annotation is missing for a sample the entry in that sample row can be left blank.";
        
        HTMLMessageFileChooser mfc = new HTMLMessageFileChooser(new Frame(), "Test Dialog", msg, "C:/Temp", true);
        //mfc.setApproveButtonText("Load");
        //mfc.setPreferredMessageSize(new Dimension(350,100));
        mfc.setSize(new Dimension(500,600));
        
        mfc.showModal();
    }
    
}
