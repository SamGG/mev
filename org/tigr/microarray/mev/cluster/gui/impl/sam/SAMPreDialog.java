/*
Copyright @ 1999-2005, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * SAMPreDialog.java
 *
 * Created on January 31, 2003, 1:45 PM
 */

package org.tigr.microarray.mev.cluster.gui.impl.sam;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.tigr.util.awt.ActionInfoDialog;

/**
 *
 * @author  nbhagaba
 * @version 
 */
public class SAMPreDialog extends ActionInfoDialog {
    
    JRadioButton previousButton, newButton; 
    JButton okButton, cancelButton;
    JCheckBox drawTreesBox;
    private JRadioButton sigOnly, allClusters;    
    boolean okPressed = false;

    /** Creates new SAMPreDialog */
    public SAMPreDialog(JFrame parentFrame, boolean modality) {
        super(parentFrame, "SAM Initialization", modality);
	setBounds(0, 0, 350, 150);
	setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	GridBagLayout gridbag = new GridBagLayout();
	GridBagConstraints constraints = new GridBagConstraints();
	//constraints.fill = GridBagConstraints.BOTH;
	JPanel pane = new JPanel();
	pane.setLayout(gridbag); 
        
        constraints.anchor = GridBagConstraints.WEST;

        previousButton = new JRadioButton("Use SAM Graph and input parameters from last run", true);
        newButton = new JRadioButton("Start new analysis", false);
        ButtonGroup choosePrevOrNew = new ButtonGroup();
        choosePrevOrNew.add(previousButton);
        choosePrevOrNew.add(newButton);
        
        sigOnly = new JRadioButton("Significant genes only", true);        
        allClusters = new JRadioButton("All clusters", false);
        
        sigOnly.setEnabled(false);
        allClusters.setEnabled(false);
        
        ButtonGroup allOrSig = new ButtonGroup();
        allOrSig.add(sigOnly);
        allOrSig.add(allClusters);        
        
	buildConstraints(constraints, 0, 0, 2, 1, 0, 30);
	gridbag.setConstraints(previousButton, constraints);
	pane.add(previousButton); 
        
	buildConstraints(constraints, 0, 1, 2, 1, 0, 30);
	gridbag.setConstraints(newButton, constraints);
	pane.add(newButton);  
        
        drawTreesBox = new JCheckBox("Draw Hierarchichal Trees for: ", false);
        
        drawTreesBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.DESELECTED) {
                    sigOnly.setEnabled(false);
                    allClusters.setEnabled(false);
                } else {
                    sigOnly.setEnabled(true);
                    allClusters.setEnabled(true);                    
                }
            }
        }); 
        
	buildConstraints(constraints, 0, 2, 2, 1, 0, 30);
	gridbag.setConstraints(drawTreesBox, constraints);
	pane.add(drawTreesBox);         
        
        constraints.anchor = GridBagConstraints.CENTER;
        
	buildConstraints(constraints, 0, 3, 1, 1, 50, 10);
        gridbag.setConstraints(sigOnly, constraints);
        pane.add(sigOnly);
        
	buildConstraints(constraints, 1, 3, 1, 1, 50, 10);
        gridbag.setConstraints(allClusters, constraints);
        pane.add(allClusters);        
        
        okButton = new JButton("OK");
	buildConstraints(constraints, 0, 4, 1, 1, 50, 10);
	gridbag.setConstraints(okButton, constraints);
        okButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent evt) {
                okPressed = true;
                dispose();
            }
        });
	pane.add(okButton); 
        
        cancelButton = new JButton("Cancel");
	buildConstraints(constraints, 1, 4, 1, 1, 50, 0);
	gridbag.setConstraints(cancelButton, constraints);
        cancelButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent evt) {            
                okPressed = false;
                dispose();
            }
        });
	pane.add(cancelButton); 
        
        setContentPane(pane);
    }
    
    public void setVisible(boolean visible) {
	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);
	
	super.setVisible(visible);
	
	if (visible) {
	    //bPanel.okButton.requestFocus(); //UNCOMMMENT THIS LATER
	}
    }
    
    void buildConstraints(GridBagConstraints gbc, int gx, int gy,
    int gw, int gh, int wx, int wy) {
	
	gbc.gridx = gx;
	gbc.gridy = gy;
	gbc.gridwidth = gw;
	gbc.gridheight = gh;
	gbc.weightx = wx;
	gbc.weighty = wy;
    }  
    
    
    public boolean isOkPressed() {
	return okPressed;
    }  
    
    public boolean usePrevious() {
        return previousButton.isSelected();
    }
    
    public boolean drawTrees() {
        return drawTreesBox.isSelected();
    }
    
    public boolean drawSigTreesOnly() {
        return sigOnly.isSelected();
    }    
    
    public static void main(String[] args) {
        
	JFrame dummyFrame = new JFrame();
	SAMPreDialog sDialog = new SAMPreDialog(dummyFrame, true);
	sDialog.setVisible(true);
    }    

}
