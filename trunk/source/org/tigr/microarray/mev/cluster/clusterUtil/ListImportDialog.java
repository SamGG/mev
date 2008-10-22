/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
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
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.*;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JTextField;

import org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.ParameterPanel;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.HelpWindow;

/**
 *
 * @author  braisted
 */
    

public class ListImportDialog extends AlgorithmDialog {
    
    private Vector annFields;
    private JComboBox listBox;
    private JTextPane pane;
    private JCheckBox[] checkBoxes;
    private List theList;
    private List otherList;
    private int result = JOptionPane.CANCEL_OPTION;
    private boolean bin = false;
    private JTextField lowerField;
    private JTextField upperField;
 
    
    /** Creates a new instance of GeneListImportDialog */
    public ListImportDialog(java.awt.Frame parent, String [] fieldNames, boolean geneList, String[] genes) {
        super(parent, geneList ? "Gene List Import Dialog" : "Sample List Import Dialog", true);
        annFields = new Vector();
        for(int i = 0; i < fieldNames.length; i++){
            annFields.addElement(fieldNames[i]);
        }

        ParameterPanel paramPanel;
        if(geneList)
            paramPanel = new ParameterPanel("Gene List Import Parameters");
        else
            paramPanel = new ParameterPanel("Sample List Import Parameters");
        
        paramPanel.setLayout(new GridBagLayout());
        
        JLabel listLabel;
        if(geneList)
            listLabel = new JLabel("Gene ID Type:");        
        else
            listLabel = new JLabel("Sample ID Type:");        
            
        listBox = new JComboBox(annFields);
        
        
        if(annFields.size() > 0)
            listBox.setSelectedIndex(0);
        
        JLabel textLabel;       
        textLabel = new JLabel("Paste List (ctrl-v):");
        pane = new JTextPane();
        pane.setPreferredSize(new Dimension(125, 200));
        if(genes != null) {
        	String genelist="";
        	for(int i=0; i<genes.length; i++)
        		genelist += genes[i] + "\n";
        	pane.setText(genelist);
        }
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
    
    /** Creates a new instance of binned GeneListImportDialog */
    public ListImportDialog(java.awt.Frame parent, String [] fieldNames, boolean geneList, boolean auto, boolean bin) {
        super(parent, geneList ? "Gene List Import Dialog" : "Sample List Import Dialog", true);
        this.bin =bin;
        annFields = new Vector<String>();
        for(int i = 0; i < fieldNames.length; i++){
            annFields.addElement(fieldNames[i]);
        }
        checkBoxes = new JCheckBox[annFields.size()];
        JComboBox comboBox = new JComboBox();
        ParameterPanel paramPanel;
        if(geneList)
            paramPanel = new ParameterPanel("Gene List Import Parameters");
        else
            paramPanel = new ParameterPanel("Sample List Import Parameters");
        
        paramPanel.setLayout(new GridBagLayout());
        
        JLabel listLabel;
        if(geneList)
            listLabel = new JLabel("Gene ID Type:");        
        else
            listLabel = new JLabel("Sample ID Type:");      
        JLabel lowerLimit = new JLabel("Lower Limit: ");
        JLabel upperLimit = new JLabel("Upper Limit: ");
        lowerField = new JTextField("");
        upperField = new JTextField("");
        lowerField.setSize(50, 20);
        lowerField.setMinimumSize(new Dimension(20,10));
        listBox = new JComboBox(annFields);
        pane = new JTextPane();
        pane.setPreferredSize(new Dimension(125, 200));
        
        JScrollPane scroll = new JScrollPane(pane);
        scroll.getViewport().setViewSize(new Dimension(125, 200));
        scroll.setBorder(BorderFactory.createLineBorder(Color.black, 2));
        
        
        if(annFields.size() > 0)
            listBox.setSelectedIndex(0);
        
        for (int i=0; i<annFields.size(); i++){
        	checkBoxes[i] = new JCheckBox(fieldNames[i], false);
        	comboBox.add(checkBoxes[i]);
        }
        
        theList = new List(16, false);
        otherList = new List(8, false);
        for (int i=0; i<annFields.size(); i++){
        	theList.add(fieldNames[i]);
        }

        paramPanel.add(listLabel, new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10,0,0,20), 0,0)); 
        paramPanel.add(listBox, new GridBagConstraints(1,0,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10,0,0,0), 0,0));

        paramPanel.add(lowerLimit, new GridBagConstraints(0,1,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10,0,0,20), 0,0)); 
        paramPanel.add(upperLimit, new GridBagConstraints(0,2,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10,0,0,20), 0,0)); 
        
        paramPanel.add(lowerField, new GridBagConstraints(1,1,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10,0,0,20), 0,0)); 
        paramPanel.add(upperField, new GridBagConstraints(1,2,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10,0,0,20), 0,0)); 
        
        addContent(paramPanel);
        setActionListeners(new Listener());
        pack();
    }    
    
    /** Creates a new instance of GeneListImportDialog */
    public ListImportDialog(java.awt.Frame parent, String [] fieldNames, boolean geneList, boolean auto) {
        super(parent, geneList ? "Automatic Gene List Import Dialog" : "Automatic Sample List Import Dialog", true);
        annFields = new Vector();
        for(int i = 0; i < fieldNames.length; i++){
            annFields.addElement(fieldNames[i]);
        }
        checkBoxes = new JCheckBox[annFields.size()];
        JComboBox comboBox = new JComboBox();
        ParameterPanel paramPanel;
        if(geneList)
            paramPanel = new ParameterPanel("Gene List Import Parameters");
        else
            paramPanel = new ParameterPanel("Sample List Import Parameters");
        
        paramPanel.setLayout(new GridBagLayout());
        
        JLabel listLabel;
        if(geneList)
            listLabel = new JLabel("Available Gene ID Types:");        
        else
            listLabel = new JLabel("Available Sample ID Types:");        
        JLabel otherListLabel;
        if(geneList)
            otherListLabel = new JLabel("Selected Gene ID Types:");        
        else
            otherListLabel = new JLabel("Selected Sample ID Types:"); 
            
        listBox = new JComboBox(annFields);
        pane = new JTextPane();
        pane.setPreferredSize(new Dimension(125, 200));
        
        JScrollPane scroll = new JScrollPane(pane);
        scroll.getViewport().setViewSize(new Dimension(125, 200));
        scroll.setBorder(BorderFactory.createLineBorder(Color.black, 2));
        
        
        if(annFields.size() > 0)
            listBox.setSelectedIndex(0);
        
        for (int i=0; i<annFields.size(); i++){
        	checkBoxes[i] = new JCheckBox(fieldNames[i], false);
        	comboBox.add(checkBoxes[i]);
        }
        
        theList = new List(16, false);
        otherList = new List(8, false);
        JLabel selectorLabel = new JLabel("Add:");
        Button selector = new Button(">>>>");
        JLabel unSelectorLabel = new JLabel("Delete:");
        Button unSelector = new Button("<<<<");
        selector.setActionCommand("select-command");
        unSelector.setActionCommand("unselect-command");
        selector.addActionListener(new Listener());
        unSelector.addActionListener(new Listener());
        selector.setSize(50, 30);
        for (int i=0; i<annFields.size(); i++){
        	theList.add(fieldNames[i]);
        }

        paramPanel.add(listLabel, new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.EAST, GridBagConstraints.VERTICAL, new Insets(10,0,0,20), 0,0)); 
        paramPanel.add(otherListLabel, new GridBagConstraints(2,0,1,1,0,0,GridBagConstraints.EAST, GridBagConstraints.VERTICAL, new Insets(10,0,0,20), 0,0)); 
        paramPanel.add(theList, new GridBagConstraints(0,1,1,5,0,0,GridBagConstraints.EAST, GridBagConstraints.VERTICAL, new Insets(10,0,0,20), 0,0));
        paramPanel.add(selector, new GridBagConstraints(1,2,1,1,0,0,GridBagConstraints.EAST, GridBagConstraints.VERTICAL, new Insets(10,0,0,20), 0,0));
        paramPanel.add(selectorLabel, new GridBagConstraints(1,1,1,1,0,0,GridBagConstraints.EAST, GridBagConstraints.VERTICAL, new Insets(10,0,0,20), 0,0));
        paramPanel.add(unSelector, new GridBagConstraints(1,4,1,1,0,0,GridBagConstraints.EAST, GridBagConstraints.VERTICAL, new Insets(10,0,0,20), 0,0));
        paramPanel.add(unSelectorLabel, new GridBagConstraints(1,3,1,1,0,0,GridBagConstraints.EAST, GridBagConstraints.VERTICAL, new Insets(10,0,0,20), 0,0));
        paramPanel.add(otherList, new GridBagConstraints(2,1,1,5,0,0,GridBagConstraints.EAST, GridBagConstraints.VERTICAL, new Insets(10,0,0,20), 0,0));
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
    /**
     * 
     * @return returns an array of selected fields
     */
    public List getSelectedFields(){
    	return otherList;
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
    
    public float getLowerLimit(){
    	return Float.parseFloat(this.lowerField.getText());
    }
    public float getUpperLimit(){
    	return Float.parseFloat(this.upperField.getText());
    }
    
        /**
     * The class to listen to the dialog and check boxes items events.
     */
    private class Listener extends DialogListener {
        
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            if (command.equals("ok-command")) {
            	if (bin){
            		try{
            			if (Float.parseFloat(upperField.getText()) < Float.parseFloat(lowerField.getText())){
            				JOptionPane.showMessageDialog(null, "Upper limit must be greater than lower limit.", "Error", JOptionPane.ERROR_MESSAGE);
            				return;
            			}
            		}catch (NumberFormatException nfe) {
            			JOptionPane.showMessageDialog(null, "Please enter numerical values for the upper and lower limits.", "Error", JOptionPane.ERROR_MESSAGE);
            			return;
            		}
            	}
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
            else if (command.equals("select-command")){  
            	if (theList.getSelectedItem()==null)
            		return;
            	otherList.add(theList.getSelectedItem());
            	theList.remove((theList.getSelectedIndex()));
            	return;
            }
            else if (command.equals("unselect-command")){ 
            	if (otherList.getSelectedItem()==null)
            		return;
            	theList.add(otherList.getSelectedItem());
            	otherList.remove((otherList.getSelectedIndex()));
            	return;
            }
            dispose();
        }
        
        public void windowClosing(WindowEvent e) {
            result = JOptionPane.CANCEL_OPTION;
            dispose();
        }
        
    }
    public static void main(String[] args){
    	String[] qwe ={"qwe","werhjkhjkhjk"};
    	ListImportDialog lid = new ListImportDialog(new java.awt.Frame(), qwe, true, false, true);
    	if(lid.showModal() == JOptionPane.OK_OPTION) {
    		System.out.println("done");
    	}
    }
    
}
