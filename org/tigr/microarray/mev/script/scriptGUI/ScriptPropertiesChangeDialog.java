/*
 * ScriptPropertiesChangeDialog.java
 *
 * Created on March 10, 2004, 5:18 PM
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
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JButton;
import javax.swing.JDialog;
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

import org.tigr.microarray.mev.script.util.ScriptConstants;

import org.tigr.microarray.mev.cluster.gui.impl.GUIFactory;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.*;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.*;

import org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog;
/**
 *
 * @author  braisted
 */
public class ScriptPropertiesChangeDialog extends AlgorithmDialog {
    JTable table;
    boolean [] badData;
    /** Creates a new instance of ScriptPropertiesChangeDialog */
    public ScriptPropertiesChangeDialog(String [] lines) {
        super(new JFrame(), "Value Editor", true);
        String [][] data = new String[lines.length][2];        
        data = getKeyValuePairs(lines);
        String [] header = new String[2];
        header[0] = "Key";
        header[1] = "Value"; 
        table = new JTable(data, header);
     //   table = new ScriptAlgorithmPropertyViewer(header, data);
    }
    
    private String [][] getKeyValuePairs(String [] lines) {
         String [][] data = new String[lines.length][2];
         badData = new boolean[lines.length];
         int cnt = 0;
         for(int i = 0; i < lines.length; i++) {
            data[cnt][0] = getKey(lines[i]);
            data[cnt][1] = getValue(lines[i]);
         }
         return data;   
    }
    
    private String getKey(String line) {
        int pnt = line.indexOf("key");
        StringTokenizer stok;
        String str, val; 
        if(pnt > 0 && line.length() < pnt+4) {
            str= line.substring(pnt);
            stok = new StringTokenizer(str, "\"");
            if(stok.countTokens() > 1){
                stok.nextToken();
                return stok.nextToken();
            }
        }
        return null;        
    }
    
    private String getValue(String line) {
                int pnt = line.indexOf("value");
        StringTokenizer stok;
        String str, val; 
        if(pnt > 0 && line.length() < pnt+4) {
            str= line.substring(pnt);
            stok = new StringTokenizer(str, "\"");
            if(stok.countTokens() > 1){
                stok.nextToken();
                return stok.nextToken();
            }
        }
        return null;      
    }
    /*
    private class ScriptAlgorithmPropertyViewer extends TableViewer {
        ScriptAlgorithmPropertyViewer(String [] header, String [][] data) {
            super(header, data);
        }
    }
     */
    
}
