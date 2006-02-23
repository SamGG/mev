/*
Copyright @ 1999-2005, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * EaseFileUpdateDialog.java
 *
 * Created on January 19, 2005, 4:25 PM
 */
package org.tigr.microarray.mev.cluster.gui.impl.ease;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.HelpWindow;

/**
 *
 * @author  braisted
 */
public class EASEFileUpdateDialog extends AlgorithmDialog {

    private JComboBox animalSpeciesBox;
    private JComboBox plantSpeciesBox;
    private JComboBox animalArrayBox;
    private JComboBox plantArrayBox;
    private JLabel animalArrayLabel;
    private JLabel plantArrayLabel;
    
    private Hashtable plantHash, animalHash;
    private Vector plantKeys, animalKeys;

    private JTabbedPane pane;
    
    private int result = JOptionPane.CANCEL_OPTION;
    
    /** Creates a new instance of EaseFileUpdateDialog */
    public EASEFileUpdateDialog(JFrame parent, Vector plantKeys, Hashtable plantHash, Vector animalKeys, Hashtable animalHash) {
        super(parent, "Ease File Update Selection", true);
        this.plantHash = plantHash;
        this.animalHash = animalHash;
        this.plantKeys = plantKeys;
        this.animalKeys = animalKeys;
        
        Listener listener = new Listener();
        
        plantSpeciesBox = new JComboBox(plantKeys);
        plantSpeciesBox.setActionCommand("change-plant-species-command");
        plantSpeciesBox.addActionListener(listener);
        plantArrayBox = new JComboBox((Vector)((Vector)plantHash.get(plantKeys.elementAt(0))).clone());
        JLabel plantSpecLabel = new JLabel("Species");
        plantSpecLabel.setOpaque(false);
        plantArrayLabel = new JLabel("Arrays for "+(String)plantKeys.elementAt(0));
        plantArrayLabel.setOpaque(false);
        
        animalSpeciesBox = new JComboBox(animalKeys);        
        animalSpeciesBox.setActionCommand("change-animal-species-command");
        animalSpeciesBox.addActionListener(listener);
        animalArrayBox = new JComboBox((Vector)((Vector)animalHash.get(animalKeys.elementAt(0))).clone());        
        JLabel animalSpecLabel = new JLabel("Species");
        animalSpecLabel.setOpaque(false);
        animalArrayLabel = new JLabel("Arrays for "+(String)animalKeys.elementAt(0));
        animalArrayLabel.setOpaque(false);                
        
        JPanel animalPanel = new JPanel();
        animalPanel.setLayout(new GridBagLayout());
        animalPanel.setBackground(Color.white);

        animalPanel.add(animalSpecLabel, new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,5,0),0,0));
        animalPanel.add(animalSpeciesBox, new GridBagConstraints(0,1,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,20,0),0,0));
        animalPanel.add(animalArrayLabel, new GridBagConstraints(0,2,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,5,0),0,0));     
        animalPanel.add(animalArrayBox, new GridBagConstraints(0,3,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,10,0),0,0));
        
        JPanel plantPanel = new JPanel();
        plantPanel.setLayout(new GridBagLayout());
        plantPanel.setBackground(Color.white);        

        plantPanel.add(plantSpecLabel, new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,5,0),0,0));
        plantPanel.add(plantSpeciesBox, new GridBagConstraints(0,1,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,20,0),0,0));
        plantPanel.add(plantArrayLabel, new GridBagConstraints(0,2,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,5,0),0,0));     
        plantPanel.add(plantArrayBox, new GridBagConstraints(0,3,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,10,0),0,0));
                       
        pane = new JTabbedPane();
        pane.addTab("Animal Arrays", animalPanel);
        pane.addTab("Plant Arrays", plantPanel);
        
        addContent(pane);
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
    
    
    
    private void resetControls() {
        pane.setSelectedIndex(0);
        this.animalSpeciesBox.setSelectedIndex(0);
    }
    
    public String getArrayName() {
        String name;
        if(pane.getSelectedIndex() == 0) {
            name = (String)animalArrayBox.getSelectedItem();
        } else {
            name = (String)plantArrayBox.getSelectedItem();
        }
        return name;        
    }
    
    public String getSpeciesName() {
        String name;
        if(pane.getSelectedIndex() == 0) {
            name = (String)animalSpeciesBox.getSelectedItem();
        } else {
            name = (String)plantSpeciesBox.getSelectedItem();
        }
        return name;
    }
        /**
     * The class to listen to the dialog events.
     */
    private class Listener extends DialogListener {
        
        public void actionPerformed(ActionEvent e) {
            Object source = e.getSource();
            String command = e.getActionCommand();
            if(command.equals("change-plant-species-command")) {
                plantArrayBox.removeAllItems();                
                Vector v = (Vector)(plantHash.get(plantSpeciesBox.getSelectedItem()));
                for(int i = 0; i < v.size(); i++)
                    plantArrayBox.addItem(v.elementAt(i));
                plantArrayLabel.setText("Arrays for "+(String)plantSpeciesBox.getSelectedItem());
            } else if(command.equals("change-animal-species-command")) {
                animalArrayBox.removeAllItems();                
                Vector v = (Vector)(animalHash.get(animalSpeciesBox.getSelectedItem()));
                for(int i = 0; i < v.size(); i++)
                    animalArrayBox.addItem(v.elementAt(i));
                animalArrayLabel.setText("Arrays for "+(String)animalSpeciesBox.getSelectedItem());
            } else if (source == okButton) {
                result = JOptionPane.OK_OPTION;
                dispose();
            } else if (source == cancelButton) {
                result = JOptionPane.CANCEL_OPTION;
                dispose();
            } else if (source == resetButton) {
                resetControls();
            } else if (source == infoButton){
                HelpWindow hw = new HelpWindow(EASEFileUpdateDialog.this, "EASE File Update Dialog");
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
