/*
Copyright @ 1999-2004, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * $RCSfile: SearchDialog.java,v $
 * $Revision: 1.1 $
 * $Date: 2005-02-24 20:23:44 $
 * $Author: braistedj $
 * $State: Exp $
 */

/*
 * GeneSearchDialog.java
 *
 * Created on September 8, 2004, 9:53 AM
 */

package org.tigr.microarray.mev;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;

import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;

import org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.ParameterPanel;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.HelpWindow;

/**
 *
 * @author  braisted
 */
public class SearchDialog extends AlgorithmDialog {
    
    private int result = JOptionPane.CANCEL_OPTION;
    
    private JPanel controlPanel;
    private JTextField termField;
    private JCheckBox caseSensBox;
    private JCheckBox entireWordBox;
    private JRadioButton geneSearchButton;
    private JRadioButton expSearchButton;
    private FieldsPanel fieldsPanel;
    private FieldsPanel sampleFieldsPanel;
    
    /** Creates a new instance of GeneSearchDialog */
    public SearchDialog(Frame parent, String [] fieldNames, String [] sampleNames) {
        super(parent, "Global Search", true);
        Listener listener = new Listener();
        
        //Main panel
        controlPanel = new JPanel(new GridBagLayout());
        controlPanel.setBackground(Color.white);
        
        //search gene or experiment selection panel
        ParameterPanel modePanel = new ParameterPanel("Search Mode");
        modePanel.setLayout(new GridBagLayout());
        ButtonGroup bg = new ButtonGroup();
        
        geneSearchButton = new JRadioButton("Gene Search", true);
        geneSearchButton.setFocusPainted(false);
        geneSearchButton.setBackground(Color.white);
        geneSearchButton.setActionCommand("search-mode-change");
        geneSearchButton.addActionListener(listener);
        bg.add(geneSearchButton);
        
        expSearchButton = new JRadioButton("Sample Search", true);
        expSearchButton.setFocusPainted(false);
        expSearchButton.setBackground(Color.white);
        expSearchButton.setActionCommand("search-mode-change");
        expSearchButton.addActionListener(listener);
        bg.add(expSearchButton);
        
        modePanel.add(geneSearchButton, new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,15,0,25),0,0));
        modePanel.add(expSearchButton, new GridBagConstraints(1,0,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,40,0,0),0,0));
        
        //search term panel
        ParameterPanel searchTermPanel = new ParameterPanel("Search Term");
        searchTermPanel.setLayout(new GridBagLayout());
        
        JLabel termLabel = new JLabel("Search Term:");
        termLabel.setOpaque(false);
        
        termField = new JTextField(25);
        
        caseSensBox = new JCheckBox("Case Sensitive", false);
        caseSensBox.setOpaque(false);
        caseSensBox.setFocusPainted(false);
        
        entireWordBox = new JCheckBox("Exact Match", false);
        entireWordBox.setOpaque(false);
        entireWordBox.setFocusPainted(false);
        
        //add components
        searchTermPanel.add(termLabel, new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.EAST, GridBagConstraints.VERTICAL, new Insets(5,20,0,0), 0, 0));
        searchTermPanel.add(termField, new GridBagConstraints(1,0,1,1,1,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,10,0,20), 0, 0));
        
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        panel.add(caseSensBox, new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,35), 0, 0));
        panel.add(entireWordBox, new GridBagConstraints(1,0,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,15,0,0), 0, 0));
        searchTermPanel.add(panel, new GridBagConstraints(0,1,2,1,1,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0, 0));
        
        //Now the search fields
        //Use an nxm layout option with rows over columns
        fieldsPanel = new FieldsPanel(fieldNames);
        //fieldsPanel.setLayout(new GridBagLayout());
        
        sampleFieldsPanel = new FieldsPanel(sampleNames);
        
        controlPanel.add(modePanel, new GridBagConstraints(0,0,1,1,1,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,5,5,5), 0, 0));
        controlPanel.add(searchTermPanel, new GridBagConstraints(0,1,1,1,1,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,5,5,5), 0, 0));
        controlPanel.add(fieldsPanel, new GridBagConstraints(0,2,1,1,1,1,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,5,5,5), 0, 0));
        
        addContent(controlPanel);
        setActionListeners(listener);
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
    
    
    public Vector getSelectedFields() {
        FieldsPanel fPanel;
        
        if(isGeneSearch())
            fPanel = fieldsPanel;
        else
            fPanel = sampleFieldsPanel;
        
        Vector v = new Vector();
        Vector boxes = fPanel.fieldBoxVector;
        JCheckBox box;
        for(int i = 0; i < boxes.size(); i++) {
            box = (JCheckBox)(boxes.elementAt(i));
            if(box.isSelected())
                v.add(box.getText());
        }
        return v;
    }
    
    public boolean isGeneSearch() {
        return geneSearchButton.isSelected();
    }
    
    public String getSearchTerm() {
        return termField.getText();
    }
    
    public boolean isCaseSensitive() {
        return caseSensBox.isSelected();
    }
    
    public boolean isWholeTermRequired() {
        //swiched label and polarity
        return entireWordBox.isSelected();
    }
    
    public AlgorithmData getSearchCriteria() {
        AlgorithmData params = new AlgorithmData();
        params.addParam("search-term", getSearchTerm());
        params.addParam("case-sensitive", String.valueOf(isCaseSensitive()));
        params.addParam("full-term", String.valueOf(isWholeTermRequired()));
        params.addParam("gene-search", String.valueOf(isGeneSearch()));        
        Vector v = getSelectedFields();
        String [] fieldNames = new String[v.size()];
        for(int i = 0; i < fieldNames.length; i++) {
            fieldNames[i] = (String)(v.elementAt(i));
        }        
        params.addStringArray("field-names", fieldNames);
        return params;
    }
    
    private void resetControls() {        
        geneSearchButton.setSelected(true);
        caseSensBox.setSelected(false);
        entireWordBox.setSelected(false);
        
        for(int i = 0; i < fieldsPanel.fieldBoxVector.size(); i++) {
            ((JCheckBox)(fieldsPanel.fieldBoxVector.elementAt(i))).setSelected(i == 0);
        }
        
        termField.setText("");
        termField.grabFocus();
        termField.selectAll();
    }
    
    private class FieldsPanel extends ParameterPanel {
        JScrollPane scrollPane;
        JList fieldList;
        Vector fieldBoxVector;
        
        JButton allButton;
        JButton resetSelButton;
        
        public FieldsPanel(String [] fields) {
            super("Searchable Fields");
            super.setLayout(new GridBagLayout());
            setLayout(new GridBagLayout());
            
            allButton = new JButton("Select All Fields");
            allButton.setFocusPainted(false);
            allButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    for(int i = 0; i < fieldBoxVector.size(); i++) {
                        ((JCheckBox)(fieldBoxVector.elementAt(i))).setSelected(true);
                    }
                }
            });
            
            resetSelButton = new JButton("Reset Selection");
            resetSelButton.setFocusPainted(false);
            resetSelButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    for(int i = 0; i < fieldBoxVector.size(); i++) {
                        ((JCheckBox)(fieldBoxVector.elementAt(i))).setSelected(i == 0);
                    }
                }
            });
            
            fieldBoxVector = new Vector();
            JPanel fieldPanel = new JPanel();
            fieldPanel.setLayout(new GridBagLayout());
            fieldPanel.setBackground(Color.white);
            //fieldPanel.setPreferredSize(new Dimension(200,200));
            //scrollPane.setSize(200,200);
            
            for(int i = 0; i < fields.length; i++) {
                addBox(fieldPanel, fieldBoxVector, fields[i], i);
            }
            
            fieldList = new JList(fieldBoxVector);
            fieldList.setSelectionMode(javax.swing.DefaultListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            fieldList.setVisibleRowCount(5);
            fieldList.setCellRenderer(new MyCellRenderer());
            scrollPane = new JScrollPane(fieldPanel);
            scrollPane.setPreferredSize(new Dimension(350,Math.min(200, fieldBoxVector.size()*30)));
            scrollPane.setSize(350, Math.min(200, fieldBoxVector.size()*30));
            
            super.add(allButton, new GridBagConstraints(0,0,1,1,0,0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,0,10,5), 0,0));
            super.add(resetSelButton, new GridBagConstraints(1,0,1,1,0,0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,5,10,0), 0,0));
            JPanel p = new JPanel();
            p.setBackground(Color.red);
            super.add(scrollPane, new GridBagConstraints(0,1,2,1,0,1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));
        }
    }
    
    private class MyCellRenderer implements ListCellRenderer {
        private JCheckBox box;
        
        public MyCellRenderer() {
            box = new JCheckBox();
            box.setFocusPainted(false);
            box.setOpaque(false);
        }
        
        public java.awt.Component getListCellRendererComponent(JList jList, Object obj, int param, boolean sel, boolean param4) {
            box.setText((String)obj);
            box.setSelected(sel);
            return box;
        }
        
    }
    
    private void addBox(JPanel p, Vector v, String s, int yPos) {
        JCheckBox box = new JCheckBox(s);
        box.setOpaque(false);
        box.setFocusPainted(false);
        box.setSelected(yPos == 0);
        p.add(box, new GridBagConstraints(0,yPos,1,1,1,0,GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0,10,0,0), 0,0));
        v.addElement(box);
    }
    
    
    private void setFieldsPanel() {
        if(!this.isGeneSearch()) {
            controlPanel.remove(fieldsPanel);
                controlPanel.add(sampleFieldsPanel, new GridBagConstraints(0,2,1,1,1,1,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,5,5,5), 0, 0));  
                controlPanel.validate();
                repaint();
           //     pack();
        } else {
            controlPanel.remove(sampleFieldsPanel);
                controlPanel.add(fieldsPanel, new GridBagConstraints(0,2,1,1,1,1,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,5,5,5), 0, 0));  
                controlPanel.validate();
                repaint();
            //    pack();            
        }        
    }
    
    public static void main(String [] args) {
        String [] s = new String[8];
        s[0] = "UID";
        s[1] = "TC#";
        s[2]= "GenBank";
        s[3] = "Locus Link Human gene Identifier this is a long field name";
        s[4] = "Unigene";
        
        s[5] = "UID";
        s[6] = "TC#";
        s[7]= "GenBank";
        String [] expFields = new String[2];
        expFields[0] = "Default Sample Name";
        expFields[1] = "Strain";
        SearchDialog dialog = new SearchDialog(new Frame(), s, expFields);
        dialog.showModal();        
    }
    
    
    
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
                HelpWindow hw = new HelpWindow(SearchDialog.this, "Search Dialog");
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
            } else if (command.equals("search-mode-change")) {
                setFieldsPanel();
                return;
        }
            dispose();
        }
        

        public void windowClosing(WindowEvent e) {
            result = JOptionPane.CLOSED_OPTION;
            dispose();
        }
    }
    
    
    
}
