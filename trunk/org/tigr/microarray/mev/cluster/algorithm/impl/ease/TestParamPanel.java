/*
 * TestParamPanel.java
 *
 * Created on August 29, 2003, 9:56 AM
 */

package org.tigr.microarray.mev.cluster.algorithm.impl.ease;

import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;


import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.Dimension;

import java.awt.event.KeyEvent;
import java.awt.event.ItemEvent;
import java.awt.event.WindowEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ItemListener;

import java.io.File;

import java.util.Vector;

import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JButton;
import javax.swing.UIManager;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ButtonGroup;
import javax.swing.BorderFactory;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
/**
 *
 * @author  braisted
 */
public class TestParamPanel extends JPanel {
    
        JTextField converterFileField;       
        JList fieldNamesList;        
        JList fileList;
        JButton browserButton;
        JTextField minClusterSizeField;
        String sep;       
        JComboBox fieldNamesBox;
                
        JList annFileList;
        Vector annVector;
        JButton removeButton;
        
        public TestParamPanel(String [] fieldNames) {
            
            Font font = new Font("Dialog", Font.BOLD, 12);
            
            //Conversion File Panel
            JPanel convPanel = new JPanel(new GridBagLayout());
            convPanel.setBackground(Color.white);           
            convPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Annotation Conversion File", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, font, Color.black));
            JCheckBox useAnnBox = new JCheckBox("use annotation converter", false);
            useAnnBox.setActionCommand("use-converter-command");
            useAnnBox.setBackground(Color.white);
            useAnnBox.setFocusPainted(false);
            
            converterFileField = new JTextField(30);
            converterFileField.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED, Color.lightGray, Color.gray)); 
            
            browserButton = new JButton("File Browser");
            browserButton.setActionCommand("converter-file-browser-command");
            browserButton.setFocusPainted(false);
            browserButton.setPreferredSize(new Dimension(150, 25));
            browserButton.setSize(150, 25);
           // browserButton.addActionListener(listener);
            
                        
            convPanel.add(useAnnBox, new GridBagConstraints(0,0,3,1,0.0,0.0,GridBagConstraints.WEST,GridBagConstraints.BOTH,new Insets(0,15,15,0),0,0));                        
            convPanel.add(new JLabel("File :"), new GridBagConstraints(0,1,1,1,0.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,15,15,0),0,0));
            convPanel.add(this.browserButton, new GridBagConstraints(0,2,3,1,0.0,0.0,GridBagConstraints.WEST,GridBagConstraints.VERTICAL,new Insets(0,15,0,0),0,0));
            convPanel.add(this.converterFileField, new GridBagConstraints(1,1,2,1,0.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,15,15,0),0,0));
 
            
            
            //Annotation file panel
             JPanel annPanel = new JPanel(new GridBagLayout());
            annPanel.setBackground(Color.white);           
            annPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Gene Annotation / Gene Ontology Linking Files", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, font, Color.black));
                       
            annVector = new Vector();
            annFileList = new JList(annVector);
            annFileList.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
            JScrollPane annPane = new JScrollPane(annFileList);
            
            JButton  annButton = new JButton("Add Files");
            annButton.setActionCommand("ann-file-browser-command");
            annButton.setFocusPainted(false);
            annButton.setPreferredSize(new Dimension(150, 25));
            annButton.setSize(150, 25);
            
            removeButton = new JButton("Remove Selected");
            removeButton.setActionCommand("ann-file-browser-command");
            removeButton.setFocusPainted(false);
            removeButton.setPreferredSize(new Dimension(150, 25));
            removeButton.setSize(150, 25);
            removeButton.setEnabled(false);

            JPanel fillPanel = new JPanel();
            fillPanel.setBackground(Color.white);
            annPanel.add(fillPanel, new GridBagConstraints(0,0,1,1,0.0,0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));                        
            annPanel.add(annButton, new GridBagConstraints(1,0,1,1,0.0,0.0, GridBagConstraints.EAST, GridBagConstraints.VERTICAL, new Insets(0,0,10,5), 0,0));            
            annPanel.add(removeButton, new GridBagConstraints(2,0,1,1,0.0,0.0, GridBagConstraints.CENTER, GridBagConstraints.VERTICAL, new Insets(0,5,10,0), 0,0));            
            annPanel.add(new JLabel("Files: "), new GridBagConstraints(0,1,1,1,0.0,0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(0,0,0,0), 0,0));
            annPanel.add(annPane, new GridBagConstraints(1,1,2,1,0.0,0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));            
      
            sep = System.getProperty("file.separator");
            String tempPath = System.getProperty("user.dir")+sep+"Data"+sep+"EASE";
            File file = new File(tempPath);
            Vector fileVector = new Vector();
            fileList = new JList(fileVector);
            if(file.exists()){
                String [] listFileNames = file.list();             
                for(int i = 0; i < listFileNames.length; i++){
                        File tempFile = new File(tempPath+sep+listFileNames[i]);
                        if(tempFile.isFile())
                            fileVector.add(listFileNames[i]);
                }
                if(fileVector.size() > 0){                    
                    converterFileField.setText(tempPath+sep+((String)fileVector.elementAt(0)));                                        
                }
            }
            
            fieldNamesList = new JList(fieldNames);
            if(this.fileList.getModel().getSize() > 0)
              //  this.fileList.setSelectionIndex(0);
             //   if(this.fieldNamesList.getSize() > 0)
          //  this.fieldNamesList.setSelectedIndex(0);

            this.fieldNamesBox = new JComboBox(fieldNames);
            fieldNamesBox.setEditable(false);

            
            minClusterSizeField = new JTextField(5);
            minClusterSizeField.setText("5");
                        
            JPanel contentPanel = new JPanel(new GridBagLayout());
            //contentPanel.setBackground(Color.white);
            
         //   Content Panels
            //Ease File Panel
            JPanel easeFilePanel = new JPanel(new GridBagLayout());
            
           this.setLayout(new GridBagLayout());
            
            JPanel annotKeyPanel = new JPanel(new GridBagLayout());
            annotKeyPanel.setBackground(Color.white);
            annotKeyPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "MeV Annotation Key  (\"Unique ID\")", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, font, Color.black));
                   
            annotKeyPanel.add(new JLabel("Annotation Key:  "), new GridBagConstraints(0,0,1,1,0.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0));
            annotKeyPanel.add(this.fieldNamesBox, new GridBagConstraints(1,0,1,1,0.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0));
          
            
            //annotKeyPanel.add(new JLabel("Minimum Cluster Size"), new GridBagConstraints(0,1,1,1,0.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0));
            //annotKeyPanel.add(this.minClusterSizeField, new GridBagConstraints(1,1,1,1,0.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0));                        

                        this.add(annotKeyPanel, new GridBagConstraints(0,0,1,1,1.0,1.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0));
            this.add(convPanel, new GridBagConstraints(0,1,1,1,1.0,1.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0));
            this.add(annPanel, new GridBagConstraints(0,2,1,1,1.0,1.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0));

       }
    
        public static void main(String [] args){
            javax.swing.JFrame f = new javax.swing.JFrame();
            String [] s = { "THC" , "GB", "UniGene" };
            TestParamPanel p = new TestParamPanel(s);
            f.getContentPane().add(p);
            f.setSize(550, 500);
            f.setVisible(true);
            f.setDefaultCloseOperation(javax.swing.JFrame.DISPOSE_ON_CLOSE);
        }
}
