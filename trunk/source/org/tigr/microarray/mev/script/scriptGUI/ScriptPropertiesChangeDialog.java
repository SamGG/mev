/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * ScriptPropertiesChangeDialog.java
 *
 * Created on March 10, 2004, 5:18 PM
 */

package org.tigr.microarray.mev.script.scriptGUI;

import java.util.StringTokenizer;

import javax.swing.JFrame;
import javax.swing.JTable;

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
