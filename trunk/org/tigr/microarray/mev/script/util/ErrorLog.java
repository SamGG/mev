/*
 * ErrorLog.java
 *
 * Created on March 11, 2004, 12:02 PM
 */

package org.tigr.microarray.mev.script.util;

import java.util.Vector;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

import java.io.File;

import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;

import org.xml.sax.SAXParseException;

import org.tigr.microarray.mev.script.ScriptManager;
import org.tigr.microarray.mev.script.scriptGUI.ScriptXMLViewer;

import org.tigr.microarray.mev.cluster.gui.impl.GUIFactory;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.*;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.*;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog;

/**
 *
 * @author  braisted
 */
public class ErrorLog {
    /** fatal error list */
    Vector fatalErrors;
    /** dtd mismatch error list */
    Vector errors;
    /** warning list */
    Vector warnings;
    /** parameter warning list */
    Vector parameterErrors;
    /** script manager */
    ScriptManager manager;
    
    ErrorLogDialog eDialog;
    
    File scriptFile;
    
    int result = JOptionPane.CANCEL_OPTION;
    
    /** Creates a new instance of ErrorLog */
    public ErrorLog(ScriptManager manager) {
        this.manager = manager;
        reset();
        eDialog = new ErrorLogDialog();
    }
    
    public void reset() {
        fatalErrors = new Vector();
        errors = new Vector();
        warnings = new Vector();
        parameterErrors = new Vector();
    }
    
    public void setFile(File file) {
        scriptFile = file;
    }
    
    public boolean isValid() {
        return (fatalErrors.size() == 0 &&
        errors.size() == 0);
    }
    
    public boolean hasWarnings() {
        return warnings.size() != 0;
    }
    
    public boolean hasErrors() {
        return errors.size() != 0;
    }
    
    public boolean hasFatalErrors() {
        return fatalErrors.size() != 0;
    }
    
    public boolean hasParameterErrors() {
        return parameterErrors.size() != 0;
    }
    
    public boolean isEmpty() {
        return isValid() && !hasWarnings();
    }
    
    public void recordWarning(SAXParseException e) {
        warnings.add(e);
    }
    
    public void recordError(SAXParseException e) {
        
        System.out.println("RECORD THE ERROR!!!!!!!!!!!!!!!!!!");
        errors.add(e);
    }
    
    public void recordFatalError(SAXParseException e) {
        fatalErrors.add(e);
    }
    
    public void recordParameterError(ScriptParameterException e) {
        parameterErrors.add(e);
    }
    
    //Reporting Methods
    
    public void reportAllListings() {
        eDialog.updateContent();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        eDialog.setLocation((screenSize.width - eDialog.getSize().width)/2, (screenSize.height - eDialog.getSize().height)/2);
        eDialog.show();
    }
    
    public void reportFatalErrors() {
        eDialog.updateContent();
        eDialog.show();
    }
    
    public void reportErrors() {
        eDialog.updateContent();
        eDialog.show();
    }
    
    public void reportWarnings() {
        eDialog.updateContent();
        eDialog.show();
    }
    
    public void reportWarnings(String text) {
        
    }
    
    
    //API methods to get data
    public String [] getFataErrorsErrors() {
        return null;
    }
    
    public String [] getErrors() {
        return null;
    }
    
    public String [] getWarnings() {
        return null;
    }
    
    
    
    private class ErrorLogDialog extends AlgorithmDialog {
        JTextPane pane;
        String text;
        JScrollPane sPane;
        boolean isEditing;
        
        public ErrorLogDialog() {
            super(new JFrame(), "Script Error Log", false);
            isEditing = false;
            //set up button panel
            this.buttonPanel.remove(this.okButton);
            this.buttonPanel.remove(this.cancelButton);
            this.buttonPanel.remove(this.resetButton);
            this.cancelButton.setText("Edit Script");
            this.cancelButton.setActionCommand("edit-command");
            cancelButton.setSize(120,30);
            cancelButton.setPreferredSize(new Dimension(120,30));
            this.okButton.setText("Close Log");
            this.okButton.setActionCommand("close-command");
            okButton.setSize(120,30);
            okButton.setPreferredSize(new Dimension(120,30));
            buttonPanel.add(cancelButton, new GridBagConstraints(2,0,1,1,0.0,0.0,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0,0,0,5), 0,0));
            buttonPanel.add(okButton, new GridBagConstraints(3,0,1,1,0.0,0.0,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0,20,0,0), 0,0));
            this.buttonPanel.validate();
            
            
            pane = new JTextPane();
            pane.setEditable(false);
            pane.setMargin(new Insets(0,10,5,15));
            pane.setBackground(new Color(252, 253, 196));
            pane.setContentType("text/html");
            sPane = new JScrollPane(pane);
            addContent(sPane);
            setActionListeners(new Listener());
            
            setSize(550, 600);
        }
        
        public void updateContent() {
            SAXParseException e;
            int lineNum;
            String lineNumStr;
            String msg;
            boolean showSep = false;
            
            text = new String();
            
            text += "<html><body>";
            
            //Fatal Errors
            if(fatalErrors.size() > 0) {
                showSep = true;
                text += "<h2>Fatal Errors</h2><h3>The following Fatal Error";
                if(fatalErrors.size() > 1)
                    text += "s";
                text += " occured during parsing and validation.</h2>";
                
                text += "<p>Note: Fatal Errors indicate that the input script had fundamental problems " +
                "in script construction such as unpaired tags.  Loading will be terminated "+
                "so that the reported errors can be corrected.  MeV does not have to be closed while "+
                "corrections are made to the input script.</p>";
                
                text += "<table border=3><th>Line</th><th>Error</th></tr>";
                for(int i = 0; i < fatalErrors.size(); i++){
                    e = (SAXParseException)fatalErrors.elementAt(i);
                    lineNum = e.getLineNumber();
                    if(lineNum >= 0)
                        lineNumStr = String.valueOf(lineNum);
                    else
                        lineNumStr = "N/A";
                    msg = e.getMessage();
                    text += "<tr><td align=center>"+lineNumStr+"</td><td>"+msg+"</td></tr>";
                }
                text += "</table>";
            }
            if(errors.size() > 0) {
                if(showSep)
                    text+="<br><hr size=3>";
                text += "<h2>Validation Errors</h2><h3>The following Validation Error";
                if(errors.size() > 1)
                    text += "s";
                text += " occured during parsing and validation.</h2>";
                text += "<p>Note: Validation Errors indicate that the input script did not conform " +
                "to the conventions of the document type definition.  Loading will be terminated "+
                "so that the reported errors can be corrected.  MeV does not have to be closed while "+
                "corrections are made to the input script.</p>";
                text += "<table border=3><tr><th>Line</th><th>Error</th></tr>";
                for(int i = 0; i < errors.size(); i++){
                    e = (SAXParseException)errors.elementAt(i);
                    lineNum = e.getLineNumber();
                    if(lineNum >= 0)
                        lineNumStr = String.valueOf(lineNum);
                    else
                        lineNumStr = "N/A";
                    msg = e.getMessage();
                    text += "<tr><td align=center>"+lineNumStr+"</td><td>"+msg+"</td></tr>";
                }
                text += "</table>";
            }
            if(warnings.size() > 0) {
                if(showSep)
                    text+="<br><hr size=3>";
                
                text += "<h2>Warnings</h2><h3>The following Warning";
                if(warnings.size() > 1)
                    text += "s";
                text += " occured during parsing and validation.</h2>";
                
                if(!showSep) { //only warnings
                    text += "<p>Note: The script has loaded with some reported warnings " +
                    "If you would like to stop the script load hit stop otherwise "+
                    "hit continue to complete the loading.</p>";
                }
                
                
                text += "<table border=3><th>Line</th><th>Error</th></tr>";
                for(int i = 0; i < warnings.size(); i++){
                    e = (SAXParseException)warnings.elementAt(i);
                    lineNum = e.getLineNumber();
                    if(lineNum >= 0)
                        lineNumStr = String.valueOf(lineNum);
                    else
                        lineNumStr = "N/A";
                    msg = e.getMessage();
                    text += "<tr><td align=center>"+lineNumStr+"</td><td>"+msg+"</td></tr>";
                }
                text += "</table>";
            }
            if(parameterErrors.size() > 0) {
                ScriptParameterException spe;
                Vector algVector = new Vector();
                
                String algName;
                
                Hashtable errorHash = new Hashtable();
                for(int i = 0; i < parameterErrors.size(); i++) {
                    spe =(ScriptParameterException)(parameterErrors.elementAt(i));
                    algName = spe.getAlgoritmName();
                    if(errorHash.containsKey(algName)) {
                        ((Vector)(errorHash.get(algName))).add(spe);
                    } else {
                        Vector v = new Vector();
                        v.add(spe);
                        errorHash.put(algName, v);
                    }
                }
                
                if(showSep)
                    text+="<br><hr size=3>";
                text+= "<h2>Script Parameter Validation Errors</h2><h3>The following Error";
                if(parameterErrors.size() > 1)
                    text+= "s";
                text+= " occurred during parameter validation.</h3>";
                text += "<p>Note: The parameter warnings are reported so that you are aware of "+
                        "possible script errors that could abort processing during script execution.</p>";
                text+= "<br><hr size=3>";    
                
                Vector eVector;
                Enumeration keys = errorHash.keys();
                int errorCount = 0;
                
                while(keys.hasMoreElements()) {
                    if(errorCount > 0)
                        text +="<br><hr size=3>";
                    errorCount++;                    
                    algName = (String)(keys.nextElement());
                    eVector = (Vector)(errorHash.get(algName));
                    text += "<table border=3><th>Algorithm</th><th>Alg. Index</th><th>Data Ref.</th><th>Key</th><th>Error</th>";
                    for(int i = 0; i < eVector.size(); i++) {
                        spe = (ScriptParameterException)(eVector.elementAt(i));
                        text += "<tr><td>"+algName+"</td><td>"+String.valueOf(spe.getAlgorithmIndex())+"</td><td>"+String.valueOf(spe.getDataReference())+"</td><td>"+spe.getKey()+"</td><td><b>"+spe.getMessage()+"</b></td></tr>";                                                
                    }
                    text += "</table>";

                    String table = manager.getValidParametersTable(algName);
                    if(table != null)
                        text += table;
                }                
                
            }
            text += "</body></html>";
            setText(text);
        }
        
        //TEST CODE
        public void setText(String t) {
            text = t;
            pane.setText(text);
            pane.setCaretPosition(0);
            pane.repaint();
        }
        
        /**
         * The class to listen to the dialog events.
         */
        private class Listener extends DialogListener {
            
            public void actionPerformed(ActionEvent e) {
                Object source = e.getSource();
                String cmd = e.getActionCommand();
                
                if (cmd.equals("close-command")) {
                    dispose();
                } else if (cmd.equals("edit-command")) {
                    if(isEditing)
                        return;
                    isEditing = true;
                    cancelButton.setEnabled(false);
                    
                    ScriptXMLViewer viewer = new ScriptXMLViewer(scriptFile);
                    
                    //if viewer actually loaded text
                    if(viewer.getText().length() > 10) {
                /*        JFrame frame = new JFrame("Script Editor");
                        
                        JScrollPane pane = new JScrollPane(viewer.getContentComponent());
                        pane.setRowHeaderView(viewer.getRowHeaderComponent());
                        
                        frame.getContentPane().add(pane);
                        frame.setSize(350, 400);
                        Point p = ErrorLogDialog.this.getLocation();
                        if(p != null)
                            frame.setLocation(p.x+30, p.y+30);
                        else {
                            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                            frame.setLocation((screenSize.width - frame.getSize().width)/2, (screenSize.height - frame.getSize().height)/2);
                  
                        }
                        frame.setVisible(true);
                 **/
                        Point p = ErrorLogDialog.this.getLocation();
                        int width = getWidth();
                        int h = sPane.getHeight();
                        
                        JScrollPane pane = new JScrollPane(viewer.getContentComponent());
                        pane.setRowHeaderView(viewer.getRowHeaderComponent());
                        
                        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sPane, pane);
                        splitPane.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
                        splitPane.setDividerLocation((int)(width*0.65));
                        splitPane.setPreferredSize(new Dimension(width+(int)(width/2), h));
                        splitPane.setSize(width+(int)(width/2), h);                      
                        addContent(splitPane);
                        pack();
                        if(p != null)
                            setLocation(p.x-((int)(width/4)), p.y);
                    } else {
                        JOptionPane.showMessageDialog(ErrorLogDialog.this, "Couldn't load script file into script editor.", "Script Editor Loading Error", JOptionPane.WARNING_MESSAGE);
                    }
                }
                
                else if (cmd.equals("info-command")){
                    HelpWindow hw = new HelpWindow(ErrorLogDialog.this, "Error Log");
                    if(hw.getWindowContent()){
                        hw.setSize(450,650);
                        hw.setLocation();
                        hw.show();
                    }
                    else {
                        hw.setVisible(false);
                        hw.dispose();
                    }
                }
            }
            
            public void windowClosing(WindowEvent e) {
                result = JOptionPane.CLOSED_OPTION;
                dispose();
            }
        }
        
    }
    
}

