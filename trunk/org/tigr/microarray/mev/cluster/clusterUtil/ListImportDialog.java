/*
Copyright @ 1999-2004, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * GeneListImportDialog.java
 *
 * Created on June 17, 2004, 10:52 AM
 */

package org.tigr.microarray.mev.cluster.clusterUtil;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;

import java.awt.event.WindowEvent;
import java.awt.event.ActionEvent;

import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;

import org.tigr.microarray.mev.cluster.gui.impl.GUIFactory;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener;

import org.tigr.microarray.mev.cluster.gui.impl.dialogs.ParameterPanel;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.HelpWindow;

/**
 *
 * @author  braisted
 */
    

public class ListImportDialog extends AlgorithmDialog {
    
    private Vector annFields;
    private JComboBox listBox;
    private JTextPane pane;
    private int result;
    
    /** Creates a new instance of GeneListImportDialog */
    public ListImportDialog(String [] fieldNames, boolean geneList) {
        super(new JFrame(), geneList ? "Gene List Import Dialog" : "Experiment List Import Dialog", true);
        annFields = new Vector();
        for(int i = 0; i < fieldNames.length; i++){
            annFields.addElement(fieldNames[i]);
        }
        
        
        ParameterPanel paramPanel;
        if(geneList)
            paramPanel = new ParameterPanel("Gene List Import Parameters");
        else
            paramPanel = new ParameterPanel("Experiment List Import Parameters");
        
        paramPanel.setLayout(new GridBagLayout());
        
        JLabel listLabel;
        if(geneList)
            listLabel = new JLabel("Gene ID Type:");        
        else
            listLabel = new JLabel("Experiment ID Type:");        
            
        listBox = new JComboBox(annFields);
        
        
        if(annFields.size() > 0)
            listBox.setSelectedIndex(0);
        
        JLabel textLabel;       
        textLabel = new JLabel("Paste List (ctrl-v):");
        pane = new JTextPane();
        pane.setPreferredSize(new Dimension(125, 200));
        
        JScrollPane scroll = new JScrollPane(pane);
        scroll.getViewport().setViewSize(new Dimension(125, 200));
        scroll.setBorder(BorderFactory.createLineBorder(Color.black, 2));

        paramPanel.add(listLabel, new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.EAST, GridBagConstraints.VERTICAL, new Insets(10,0,0,20), 0,0));
        paramPanel.add(listBox, new GridBagConstraints(1,0,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10,0,0,0), 0,0));
        paramPanel.add(textLabel, new GridBagConstraints(0,1,1,1,0,0,GridBagConstraints.NORTHEAST, GridBagConstraints.NONE, new Insets(20,0,0,20), 0,0));
        paramPanel.add(scroll, new GridBagConstraints(1,1,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(20,0,10,0), 0,0));
        
        addContent(paramPanel);
        setActionListeners(new Listener());
        pack();
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
    
    /**
     * Resets controls to default initial settings
     */
    private void resetControls(){
        listBox.setSelectedIndex(0);
        pane.setText("");
    }
    
    public String getFieldName() {        
        return (String)(listBox.getSelectedItem());
    }
    
    public String [] getList() {
        String text = pane.getText();
        StringTokenizer stok = new StringTokenizer(text, "\n");
        String [] outputList = new String[stok.countTokens()];
        int cnt = 0;
        while(stok.hasMoreTokens()) {
            outputList[cnt] = stok.nextToken().trim();
            cnt++;
        }
        return outputList;
    }
    
        /**
     * The class to listen to the dialog and check boxes items events.
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
            }
            else if (command.equals("reset-command")){
                resetControls();
                result = JOptionPane.CANCEL_OPTION;
                return;
            }
            else if (command.equals("info-command")){
                HelpWindow hw = new HelpWindow(ListImportDialog.this, "List Import Dialog");
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
    }
    
}
