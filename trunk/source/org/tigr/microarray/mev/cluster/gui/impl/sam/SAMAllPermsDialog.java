/*
 * SAMAllPermsDialog.java
 *
 * Created on November 13, 2003, 3:50 PM
 */

package org.tigr.microarray.mev.cluster.gui.impl.sam;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.tigr.util.awt.ActionInfoDialog;

/**
 *
 * @author  nbhagaba
 */
public class SAMAllPermsDialog extends ActionInfoDialog {
    

    JRadioButton allPermsButton, somePermsButton;
    //JLabel numUniquePermsLabel;
    int numUniquePerms, numUserPerms;
    JButton okButton;
    boolean okPressed = false;    
    
    /** Creates a new instance of SAMAllPermsDialog */
    public SAMAllPermsDialog(JFrame parentFrame, boolean modality, int numUniquePerms, int numUserPerms) {
        super(parentFrame, "Use all possible unique permutations?", modality);
        this.numUniquePerms = numUniquePerms;
        this.numUserPerms = numUserPerms;
	setBounds(0, 0, 400, 150);
	setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);   
	GridBagLayout gridbag = new GridBagLayout();
	GridBagConstraints constraints = new GridBagConstraints();
	//constraints.fill = GridBagConstraints.BOTH;
	JPanel pane = new JPanel();
	pane.setLayout(gridbag);     
        
        constraints.anchor = GridBagConstraints.CENTER;
        
        JLabel numUniquePermsLabel = new JLabel("There are " + numUniquePerms + " unique permutations");
        
	buildConstraints(constraints, 0, 0, 1, 1, 100, 25);
	gridbag.setConstraints(numUniquePermsLabel, constraints);
	pane.add(numUniquePermsLabel);         
        
        allPermsButton = new JRadioButton("Use all of them", true);
        somePermsButton = new JRadioButton("Use just " + numUserPerms + " random permutations as previously specified", false);
        
        ButtonGroup chooseAllOrSome = new ButtonGroup();
        chooseAllOrSome.add(allPermsButton);
        chooseAllOrSome.add(somePermsButton);
        
        constraints.anchor = GridBagConstraints.WEST;        

	buildConstraints(constraints, 0, 1, 1, 1, 0, 25);
	gridbag.setConstraints(allPermsButton, constraints);
	pane.add(allPermsButton);     
        
	buildConstraints(constraints, 0, 2, 1, 1, 0, 25);
	gridbag.setConstraints(somePermsButton, constraints);
	pane.add(somePermsButton);   
        
        constraints.anchor = GridBagConstraints.CENTER;         
        
        okButton = new JButton("OK");
	buildConstraints(constraints, 0, 3, 1, 1, 0, 25);
	gridbag.setConstraints(okButton, constraints);
        okButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent evt) {
                okPressed = true;
                dispose();
            }
        });        
	pane.add(okButton);   
        
        setContentPane(pane);
    }
    
    public void setVisible(boolean visible) {
	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);
	
	super.setVisible(visible);
	
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
    
    public boolean useAllPerms() {
        return allPermsButton.isSelected();
    }

    public static void main(String[] args) {
        
	JFrame dummyFrame = new JFrame();
	SAMAllPermsDialog sDialog = new SAMAllPermsDialog(dummyFrame, true, 20, 100);
	sDialog.setVisible(true);
    }    
    
}




























