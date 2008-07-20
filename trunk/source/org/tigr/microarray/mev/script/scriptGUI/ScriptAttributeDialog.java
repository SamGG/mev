/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * ScriptInitDialog.java
 *
 * Created on February 28, 2004, 12:38 AM
 */

package org.tigr.microarray.mev.script.scriptGUI;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.text.DateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.ParameterPanel;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.HelpWindow;
import org.tigr.microarray.mev.script.util.ScriptConstants;
/** The ScriptAttributeDialog permits the selection of basic script attributes.
 *
 * @author braisted
 */
public class ScriptAttributeDialog extends AlgorithmDialog {
    
    private int result = JOptionPane.CANCEL_OPTION;
    private JTextField dateField;
    private JTextField nameField;
    private JTextArea descriptionArea;
    private String dateString;
    
    /** Creates a new instance of ScriptInitDialog */
    public ScriptAttributeDialog() {
        super(new JFrame(), "Script Attribute Input", true);
        
        JLabel dateLabel = new JLabel("Creation Date: ");
        
        Date date  = new Date(System.currentTimeMillis());
        TimeZone tz = TimeZone.getDefault();
        DateFormat format = DateFormat.getDateTimeInstance();
        format.setTimeZone(tz);
        dateString = format.format(date);
        
        dateLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        dateField = new JTextField(dateString, 20);
        
        JLabel nameLabel = new JLabel("Script Name: ");
        nameLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        nameField = new JTextField(20);
        
        JLabel descLabel = new JLabel("Description: ");
        descLabel.setVerticalAlignment(SwingConstants.TOP);
        descLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        descriptionArea = new JTextArea(10, 25);
        JScrollPane pane = new JScrollPane(descriptionArea);
        pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        //descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        
        ParameterPanel parameterPanel = new ParameterPanel("Script Attributes");
        parameterPanel.setLayout(new GridBagLayout());
        parameterPanel.add(dateLabel, new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(0,0,10,0),0,0));
        parameterPanel.add(nameLabel, new GridBagConstraints(0,1,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(0,0,10,0),0,0));
        parameterPanel.add(descLabel, new GridBagConstraints(0,2,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
        parameterPanel.add(dateField, new GridBagConstraints(1,0,1,1,0,0,GridBagConstraints.WEST,GridBagConstraints.VERTICAL, new Insets(0,0,10,0),0,0));
        parameterPanel.add(nameField, new GridBagConstraints(1,1,1,1,0,0,GridBagConstraints.WEST,GridBagConstraints.VERTICAL, new Insets(0,0,10,0),0,0));
        parameterPanel.add(pane, new GridBagConstraints(1,2,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
        
        JPanel dialogPanel = new JPanel(new GridBagLayout());
        dialogPanel.setBackground(Color.white);
        dialogPanel.setBorder(BorderFactory.createLineBorder(Color.black));
        
        dialogPanel.add(parameterPanel, new GridBagConstraints(0,0,1,1,1.0,1.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(5,5,5,5),0,0));
        
        this.addContent(dialogPanel);
        this.setActionListeners(new Listener());
        pack();
        
        nameField.requestFocus();
        nameField.selectAll();
    }
    
    
    /** Creates a new instance of ScriptInitDialog */
    public ScriptAttributeDialog(int id, String name, String description) {
        super(new JFrame(), "Script Attribute Modification", true);
        
        dateField = new JTextField(String.valueOf(id));
        dateField.setEditable(false);
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
    
    private void resetControls() {
        dateField.setText(dateString);
        nameField.setText("");
        descriptionArea.setText("");
        nameField.requestFocus();
        nameField.selectAll();
    }
    
    /** Returns the creation date
     */    
    public String getDate() {
        return dateField.getText();
    }
    
    
    /** returns the script name
     */    
    public String getName() {
        return nameField.getText();
    }
    
    
    /** returns a description in a format ready for inclusion
     * in the XML.
     */    
    public String getFormattedDescription() {
        String text = descriptionArea.getText();
        
        //Until we enforce scrict format or wrapping we
        // just return the text as written.
        
        String formatText = "", line;
        int lineCount = descriptionArea.getLineCount();

        int startOffset, endOffset;
        
        if(lineCount < 1)
            return null;
        
        Document doc = descriptionArea.getDocument();
        
        try {
            for(int i = 0; i < lineCount; i++) {
                startOffset = descriptionArea.getLineStartOffset(i);
                endOffset = descriptionArea.getLineEndOffset(i);
                line = doc.getText(startOffset, (endOffset-startOffset));
                if(i>0) 
                    formatText += ScriptConstants.MEV_COMMENT_INDENT;
                formatText += line;
            }
        } catch (BadLocationException ble) {
            return null;
        }     
        return formatText;
    }
    
    /** Returns the description
     */    
    public String getDescription() {
        return this.descriptionArea.getText();
    }
    
  
    
    /**
     * The class to listen to the dialog events.
     */
    private class Listener extends DialogListener {
        
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            if (command.equals("ok-command")) {
                result = JOptionPane.OK_OPTION;
                dispose();
            } else if (command.equals("cancel-command")) {
                result = JOptionPane.CANCEL_OPTION;
                dispose();
            }else if (command.equals("reset-command")){
                resetControls();
            }else if(command.equals("info-command")){
                HelpWindow helpWindow = new HelpWindow(ScriptAttributeDialog.this, "Script Attribute Dialog");
                if(helpWindow.getWindowContent()){
                    helpWindow.setSize(450, 600);
                    helpWindow.setLocation();
                    helpWindow.show();
                }
                else{
                    helpWindow.dispose();
                }
            }
        }
        
        public void windowClosing(WindowEvent e) {
            result = JOptionPane.CLOSED_OPTION;
            dispose();
        }
        
    }
}
