/*
 * GeneAnnotationImportDialog.java
 *
 * Created on February 3, 2005, 4:54 PM
 */

package org.tigr.microarray.mev;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.ParameterPanel;

import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.HelpWindow;


/**
 *
 * @author  braisted
 */
public class GeneAnnotationImportDialog extends AlgorithmDialog {
    
    private JComboBox dataKeyBox, annFileKeyBox;
    private String [] dataAnnKeys;
    private String [] annFileKeys;
    
    private ColumnNamesPanel fieldSelectionPanel;
    
    private int result = JOptionPane.CANCEL_OPTION;
    
    /** Creates a new instance of GeneAnnotationImportDialog */
    public GeneAnnotationImportDialog(JFrame frame, String [] dataAnnKeys, String [] annFileKeys) {
        super(frame, "Append Gene Annotation", true);
        
        this.dataAnnKeys = dataAnnKeys;
        this.annFileKeys = annFileKeys;

        JLabel dataKeyLabel = new JLabel("Gene Identifier (from current data):");
        dataKeyBox = new JComboBox(dataAnnKeys);
        
        JLabel fileKeyLabel = new JLabel("Corresponding Gene Identifier from Input File:");        
        annFileKeyBox = new JComboBox(annFileKeys);
        
        String text = "<html><body>Note: The Gene identifiers from the loaded data and the input annoation<br>";
        text += "file should correspond to the same annoation type.   These identifiers<br>";
        text += "are used to map annotation in the file to the proper genes loaded in MeV.</html>";
        
        JLabel explanationLabel = new JLabel(text);

        fieldSelectionPanel = new ColumnNamesPanel(annFileKeys);
        
        ParameterPanel panel = new ParameterPanel("Annotation Key Selection");
        panel.setLayout(new GridBagLayout());
        
        panel.add(dataKeyLabel, new GridBagConstraints(0,0,1,1,0,0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(20,10,0,10),0,0));
        panel.add(dataKeyBox, new GridBagConstraints(0,1,1,1,0,0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,10,0,10),0,0));
        panel.add(fileKeyLabel, new GridBagConstraints(0,2,1,1,0,0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(25,10,0,10),0,0));
        panel.add(annFileKeyBox, new GridBagConstraints(0,3,1,1,0,0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,10,20,10),0,0));        
        panel.add(explanationLabel, new GridBagConstraints(0,4,1,1,1,0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,10,20,10),0,0));        
        //panel.add(new JSeparator(JSeparator.HORIZONTAL), new GridBagConstraints(0,5,1,1,0,0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,0,20,0),0,0));        
        panel.add(fieldSelectionPanel, new GridBagConstraints(0,5,1,1,0,0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,0,20,0),0,0));        
        
        setActionListeners(new Listener());
        addContent(panel);
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
    
    /** Returns the annotation key to identify genes in the annotation file
     */
    public String getFileAnnotationKey() {
        return (String)this.annFileKeyBox.getSelectedItem();
    }
    
    /** Returns the annotation key to identify genes in the loaded data
     */
    public String getDataAnnotationKey() {
        return (String)this.dataKeyBox.getSelectedItem();
    }

    /** Resets the controls to the initial state
     */
    private void resetControls() {
        this.dataKeyBox.setSelectedIndex(0);
        this.annFileKeyBox.setSelectedIndex(0);
        this.fieldSelectionPanel.resetControls();
    }
    
    public String [] getSelectedAnnotationFields() {
        return fieldSelectionPanel.getSelectedColNames();
    }
    
    /**
     * The class to listen to the dialog and check boxes items events.
     */
    private class Listener extends DialogListener {
        
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            if (command.equals("ok-command")) {
                if(getSelectedAnnotationFields().length < 1) {
                    JOptionPane.showMessageDialog(GeneAnnotationImportDialog.this, "You must select at least one annotation field to import.", "Empty Annotation Field Selection", JOptionPane.ERROR_MESSAGE);
                    return;   
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
                HelpWindow hw = new HelpWindow(GeneAnnotationImportDialog.this, "Gene Annotation Import");
                result = JOptionPane.CANCEL_OPTION;
                if(hw.getWindowContent()){
                    hw.setSize(600,650);
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

    
    private class ColumnNamesPanel extends JPanel {
        
        JCheckBox[] columnNameBoxes;
        
        JButton selectAllButton, clearAllButton;
        
        ColumnNamesPanel(String [] columnHeaders) {
    
            setLayout(new GridBagLayout());
            setBorder(BorderFactory.createTitledBorder("Select Annotation Fields to Append"));
            this.setBackground(Color.white);
            
            columnNameBoxes = new JCheckBox[columnHeaders.length];
            boolean selected = true;
            for (int i= 0; i < columnNameBoxes.length; i++) {
                if(columnHeaders[i].equals("UID") || columnHeaders[i].equals("R") || columnHeaders[i].equals("C"))
                    selected = false;
                else
                    selected = true;
                
                columnNameBoxes[i] = new JCheckBox(columnHeaders[i], selected);
                columnNameBoxes[i].setOpaque(false);
                columnNameBoxes[i].setFocusPainted(false);
            }
            
            JPanel checkBoxPanel = createCheckBoxPanel();
            JScrollPane scroll = new JScrollPane(checkBoxPanel);
            scroll.setPreferredSize(new Dimension(450, 150));
            
            JButton selectAllButton = new JButton("Select All");
            JButton clearAllButton = new JButton("Clear All");
            
            selectAllButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    for (int i = 0; i < columnNameBoxes.length; i++) {
                        columnNameBoxes[i].setSelected(true);
                    }
                }
            });
            
            clearAllButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    for (int i = 0; i < columnNameBoxes.length; i++) {
                        columnNameBoxes[i].setSelected(false);
                    }
                }
            });    
            add(selectAllButton, new GridBagConstraints(0,0,1,1,50,10, GridBagConstraints.WEST, GridBagConstraints.VERTICAL, new Insets(5,10,5,0),0,0));       
            add(scroll, new GridBagConstraints(0,1,2,1,100,90, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,5,5,5),0,0));            
        }
        
        private JPanel createCheckBoxPanel() {
            JPanel panel1 = new JPanel();
            panel1.setBackground(Color.white);
            panel1.setLayout(new GridBagLayout());    
            for (int i = 0; i < columnNameBoxes.length; i++) {    
                panel1.add(columnNameBoxes[i], new GridBagConstraints(0,i,1,1,100,0,GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0,5,0,0), 0, 0));
            }
            return panel1;
        }
        
        public int[] getSelectedCols() {
            Vector selColsVector = new Vector();
            for (int i = 0; i < columnNameBoxes.length; i++) {
                if (columnNameBoxes[i].isSelected()) {
                    selColsVector.add(new Integer(i + 1));
                }                
            }
            
            int[] selCols = new int[selColsVector.size()];
            for (int i = 0; i < selCols.length; i++) {
                selCols[i] = ((Integer)(selColsVector.get(i))).intValue();
            }
            
            return selCols;
        }
        
        public String[] getSelectedColNames() {
            Vector selColNamesVector = new Vector();
            for (int i = 0; i < columnNameBoxes.length; i++) {
                if (columnNameBoxes[i].isSelected()) {
                    selColNamesVector.add(columnNameBoxes[i].getText());
                }                
            }    
            
            String[] selColNames = new String[selColNamesVector.size()];
            for (int i = 0; i < selColNames.length; i++) {
                selColNames[i] = (String)(selColNamesVector.get(i));
            }
            
            return selColNames;
        }
        
        public void resetControls() {
            boolean selected = true;
            for (int i= 0; i < columnNameBoxes.length; i++) {
                if(columnNameBoxes[i].getText().equals("UID") || columnNameBoxes[i].getText().equals("R") || columnNameBoxes[i].getText().equals("C"))
                    selected = false;
                else
                    selected = true;
                
                columnNameBoxes[i].setSelected(selected);            }   
        }        
    }    
    
    
    public static void main(String [] args) {
        String [] s1 = new String[3];
        String [] s2 = new String[5];
        
        s1[0] = "UID";
        s1[1] = "GB";
        s1[2] = "TC";

        s2[0] = "UID";
        s2[1] = "R";
        s2[2] = "C";
        s2[3] = "Tigr TC";
        s2[4] = "GenBank";
        
        GeneAnnotationImportDialog dialog = new GeneAnnotationImportDialog(new JFrame(), s1, s2);
        dialog.showModal();
        System.exit(0);
    }
    
    
    
}
