/*
Copyright @ 1999-2004, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * ScriptValueChangeDialog.java
 *
 * Created on March 10, 2004, 11:36 AM
 */

package org.tigr.microarray.mev.script.scriptGUI;


import java.awt.Frame;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Component;
import java.awt.Insets;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.JCheckBox;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import javax.swing.JOptionPane;
import javax.swing.BorderFactory;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;

import org.tigr.microarray.mev.action.ActionManager;

import org.tigr.microarray.mev.script.ScriptManager;
import org.tigr.microarray.mev.script.util.ParameterAttributes;
import org.tigr.microarray.mev.script.util.ScriptConstants;

import org.tigr.microarray.mev.cluster.gui.impl.GUIFactory;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.*;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.*;

import org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog;

/**
 *
 * @author  braisted
 */
public class ScriptValueChangeDialog extends AlgorithmDialog {
    
    private int result = JOptionPane.CANCEL_OPTION;
    private ScriptManager manager;
    private String algName;
    private String origLine;
    private String line;
    private String key;
    private String origValue;
    private String valueType;
    
    private String leadingSpaces;
    private JLabel prefix;
    private String value;
    private JLabel suffix;
    
    private JComponent valueComponent;
    
    private Hashtable parameterHash;
    
    /** Creates a new instance of ScriptValueChangeDialog
     * @param origLine original line from xml fiels
     * @param algName Algorithm name
     * @param manager
     */
    public ScriptValueChangeDialog(String origLine, String algName, ScriptManager manager) {
        super(new JFrame(), "Value Editor", true);
        this.manager = manager;
        this.algName = algName;
        this.origLine = origLine;
        leadingSpaces = new String();
        
        initializeValues();
        
        ParameterPanel paramPanel = new ParameterPanel("Script Line");
        paramPanel.setLayout(new GridBagLayout());
        
        JLabel hintLabel = null;
        int lineBottomInset = 0;
        
    /*    if(valueType != null) {
            if(valueType.equals("unknown"))
                hintLabel = new JLabel("(The proper value type could not be determined.)");
            else if(!valueType.equals("boolean"))
                hintLabel = new JLabel("(Hint: The input value type appears to be "+valueType+".)");
            else
                lineBottomInset = 15;
        }
     */
        
        paramPanel.add(prefix, new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,lineBottomInset,0), 0, 0));
        paramPanel.add(valueComponent, new GridBagConstraints(1,0,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,2,lineBottomInset,2), 0, 0));
        paramPanel.add(suffix, new GridBagConstraints(2,0,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,lineBottomInset,0), 0, 0));
        if(hintLabel != null)
            paramPanel.add(hintLabel, new GridBagConstraints(0,1,3,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(20,0,5,0), 0, 0));
        
        JButton paramButton = new JButton("View Valid Parameters");
        paramButton.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        paramButton.setFocusPainted(false);
        paramButton.addActionListener(new ParameterListener());
        paramButton.setPreferredSize(new Dimension(180, 30));
        paramButton.setSize(180, 30);
        paramPanel.add(paramButton, new GridBagConstraints(0,1,3,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(20,0,5,0), 0, 0));
                
        parameterHash = manager.getParameterHash(algName);
        
        ButtonPanel bp = new ButtonPanel();
        
        supplantButtonPanel(bp);
        addContent(paramPanel);
        pack();
    }
    
    private void initializeValues() {
        String strPre;
        String strPost;
        int pnt = origLine.indexOf("value");
        
        if(pnt >= 0) {
            
            strPre = origLine.substring(0, pnt);
            
            leadingSpaces = "";
            
            char [] array = strPre.toCharArray();
            int i = 0;
            while( i < array.length && array[i] == ' ' ){
                leadingSpaces += " ";
                i++;
            }
            
            strPre = strPre.trim();
            
            strPost = origLine.substring(pnt);
            
            pnt = strPost.indexOf("\"");
            value = strPost.substring(pnt+1);
            strPost = new String(value);
            
            pnt = value.indexOf("\"");
            value = value.substring(0, pnt);
            value.trim();
            
            strPost = strPost.substring(pnt+1);
            
            prefix = createJLabel(strPre+" value=\"");
            suffix = createJLabel("\""+strPost);
            valueComponent = getValueComponent(value);        
        } else {
            prefix = createJLabel(" ");
            suffix = createJLabel(" ");
            valueComponent = new JLabel("Incorrect construction");
        }
        
        //get key
        pnt = origLine.indexOf("key");
        if(pnt >= 0) {
            strPost = origLine.substring(pnt);
            pnt = strPost.indexOf("\""); //leading quote
            strPost = strPost.substring(pnt+1);
            pnt = strPost.indexOf("\"");
            this.key = strPost.substring(0, pnt);
            this.key = this.key.trim();
        }
        
        
        //  }
    }
    
    private JLabel createJLabel(String text) {
        JLabel label = new JLabel(text);
        label.setBackground(Color.white);
        return label;
    }
    
    private JComponent getValueComponent(String val) {
        String type = "unknown";
        if(val.equals("true") || val.equals("false")) {
            String [] vals = new String[2];
            vals[0] = "true";
            vals[1] = "false";
            JComboBox box = new JComboBox(vals);
            if(val.equals("true"))
                box.setSelectedIndex(0);
            else
                box.setSelectedIndex(1);
            type = "boolean";
            valueType = type;
            return box;
        } else {
            boolean haveType = false;
            char [] array = val.toCharArray();
            for(int i = 0; i < array.length; i++) {
                if(Character.isLetter(array[i]) && array[i] != '.'){
                    type = "a character string";
                    haveType = true;
                    break;
                }
            }
            
            int dotCnt = 0;
            
            if(!haveType) {  //must be integer or fp
                for(int i = 0; i < array.length; i++) {
                    if(array[i] == '.'){
                        dotCnt++;
                    }
                }
                if(dotCnt == 1) {
                    type = "a floating point number";
                    haveType = true;
                } else if (dotCnt > 1) {
                    type = "a character string";
                    haveType = true;
                } else if (dotCnt == 0) {
                    type = "an integer number";
                    haveType = true;
                }
            }
            
            
            
            JTextField valField = new JTextField(val, val.length()+3);
            valueType = type;
            return valField;
        }
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
    
    public void onReset() {
        
    }
    
    /** Returns the currently selected and possibly edited XML line
     */
    public String getLine() {
        return line;
    }
    
    /** Returns the edited line
     */
    public String getValue() {
        if(valueComponent != null) {
            if(valueComponent instanceof JTextField)
                return ((JTextField)valueComponent).getText();
            if(valueComponent instanceof JComboBox)
                return (String)(((JComboBox)valueComponent).getSelectedItem());
        }
        return null;
    }
    
    private boolean validateValue(String val) {
        ParameterAttributes atts = manager.getParameterAttributes(this.algName, this.key);
        
        if(atts == null) {
            JOptionPane.showMessageDialog(this, "Parameter value, type, and constraints could not be verified.\n"+
            "Value will be committed but please consider it's validity.", "Parameter Validataion Warning", JOptionPane.WARNING_MESSAGE);
            return true;
        }
        
        String type = atts.getValueType();
        if(!checkType(val, type)) {
            JOptionPane.showMessageDialog(this, "Parameter type seems incorrect ( requires: "+type+" )","Parameter Validataion Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        if(atts.hasConstraints()) {
            String minStr = atts.getMin();
            String maxStr = atts.getMax();
            
            if(!checkConstraints(val, type, minStr, maxStr)){
                JOptionPane.showMessageDialog(this, "Parameter falls outside the constraints.","Parameter Validataion Error", JOptionPane.ERROR_MESSAGE);
                return false;
                
            }
            
        }           
        return true;
    }
    
    
    private boolean checkConstraints(String val, String type, String min, String max) {
        if(type.equals("int")){
            int value = Integer.parseInt(val);
            if(!min.equals("") && !max.equals(""))
                return (value >= Integer.parseInt(min) && value <= Integer.parseInt(max));
            else if(!max.equals(""))
                return (value <= Integer.parseInt(max));
            else if(!min.equals(""))
                return (value >= Integer.parseInt(min));
            return true;
        } else if(type.equals("float")){
            float value = Float.parseFloat(val);
            if(!min.equals("") && !max.equals(""))
                return (value >= Float.parseFloat(min) && value <= Float.parseFloat(max));
            else if(!max.equals(""))
                return (value <= Float.parseFloat(max));
            else if(!min.equals(""))
                return (value >= Float.parseFloat(min));
            return true;
        } else if(type.equals("long")){
            long value = Long.parseLong(val);
            if(!min.equals("") && !max.equals(""))
                return (value >= Long.parseLong(min) && value <= Long.parseLong(max));
            else if(!max.equals(""))
                return (value <= Long.parseLong(max));
            else if(!min.equals(""))
                return (value >= Long.parseLong(min));
            return true;
        } else if(type.equals("double")){
            double value = Double.parseDouble(val);
            if(!min.equals("") && !max.equals(""))
                return (value >= Double.parseDouble(min) && value <= Double.parseDouble(max));
            else if(!max.equals(""))
                return (value <= Double.parseDouble(max));
            else if(!min.equals(""))
                return (value >= Double.parseDouble(min));
            return true;
        }
        return true;
    }
    
    private boolean checkType(String val, String type) {
        try {
            if(type.equals("int"))
                Integer.parseInt(val);
            else if(type.equals("float"))
                Float.parseFloat(val);
            else if(type.equals("long"))
                Long.parseLong(val);
            else if(type.equals("double"))
                Double.parseDouble(val);
        } catch (Exception e) {
            return false;
        }
        return true;
    }
    
    
    private class ParameterListener implements ActionListener {
        public void actionPerformed(ActionEvent ae) {
            if(manager != null && algName != null) {
                String table = manager.getValidParametersTable(algName);
                if(table != null && table.length() > 0) {
                    JFrame frame = new JFrame("Valid Parameters for "+algName);
                    javax.swing.JDialog d = new JDialog(ScriptValueChangeDialog.this, "Valid Parameters for "+algName);
                    JTextPane pane = new JTextPane();
                    pane.setContentType("text/html");
                    pane.setText(table);
                    pane.setEditable(false);
                    pane.setBackground(new Color(Integer.parseInt("FFFFCC",16)));
                    pane.setMargin(new Insets(5,10,5,10));
                    JScrollPane spane = new JScrollPane(pane);
                    d.getContentPane().add(spane);
                    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                    d.setSize(screenSize.width/3, (int)(screenSize.height/1.5));
                    d.setLocation((screenSize.width - d.getSize().width)/2, (screenSize.height - d.getSize().height)/2);
                    d.setVisible(true);
                }
            }
        }
    }
    
    
    private class ButtonPanel extends JPanel {
        
        private JButton refreshBut;
        private JButton resetBut;
        private JButton cancelBut;
        private JButton okBut;
        private JButton infoButton;
        
        public ButtonPanel() {
            super(new GridBagLayout());
            ButtonListener listener = new ButtonListener();
            JPanel buttonsPanel = new JPanel(new GridLayout(1, 3, 10, 10));

            resetBut = new JButton("Reset");
            resetBut.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
            resetBut.setFocusPainted(false);
            resetBut.setActionCommand("reset-cmd");
            resetBut.addActionListener(listener);
            
            buttonsPanel.add(resetBut);
            
            cancelBut = new JButton("Cancel");
            cancelBut.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
            cancelBut.setSize(60,30);
            cancelBut.setPreferredSize(new Dimension(60,30));
            cancelBut.setFocusPainted(false);
            cancelBut.setActionCommand("cancel-cmd");
            cancelBut.addActionListener(listener);
            buttonsPanel.add(cancelBut);
            
            okBut = new JButton("Commit");
            okBut.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED, new Color(240,240,240), new Color(180,180,180), new Color(10,0,0), new Color(10,10,10) ));
            okBut.setSize(60,30);
            okBut.setPreferredSize(new Dimension(60,30));
            okBut.setFocusPainted(false);
            okBut.setActionCommand("commit-cmd");
            okBut.addActionListener(listener);
            buttonsPanel.add(okBut);
            
            infoButton = new JButton(null, GUIFactory.getIcon("Information24.gif"));
            infoButton.setActionCommand("info-cmd");
            infoButton.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
            infoButton.setSize(30,30);
            infoButton.setPreferredSize(new Dimension(30,30));
            infoButton.setFocusPainted(false);
            infoButton.addActionListener(listener);
            
            add(infoButton, new GridBagConstraints(0,0,1,1,1.0,1.0,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,0,0,0),0,0));
            add(buttonsPanel, new GridBagConstraints(1,0,1,1,1.0,1.0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
        }
        
        public class ButtonListener implements ActionListener {
            
            public void actionPerformed(ActionEvent ae) {
                String cmd = ae.getActionCommand();
                if(cmd.equals("apply-cmd")) {
                    
                } else if(cmd.equals("cancel-cmd")) {                    
                    dispose();
                } else if(cmd.equals("commit-cmd")) {
                    result = JOptionPane.OK_OPTION;
                    
                    if(valueType != null && valueType.equals("boolean")){
                        value = (String)((JComboBox)valueComponent).getSelectedItem();
                        line = leadingSpaces + prefix.getText()+value+suffix.getText();
                        dispose();
                    } else {
                        value = ((JTextField)valueComponent).getText();
                        if(validateValue(value)) {
                            line = leadingSpaces + prefix.getText()+value+suffix.getText();
                            dispose();
                        }
                    }
                } else if(cmd.equals("reset-cmd")) {

                } else if(cmd.equals("info-cmd")) {
                    HelpWindow hw = new HelpWindow(ScriptValueChangeDialog.this, "Script Value Input Dialog");
                    if(hw.getWindowContent()){
                        hw.setSize(450,650);
                        hw.setLocation();
                        hw.show();
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
