

package org.tigr.microarray.mev.cluster.gui.impl.sam;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.tigr.util.awt.ActionInfoDialog;

/**
 *
 * @author  wwang
 */
public class SAMLoadFileDialog extends ActionInfoDialog {
    
    JButton okButton;
    boolean okPressed = false;    
    
    /** Creates a new instance of SAMLoadFileDialog */
    public SAMLoadFileDialog(JFrame parentFrame, boolean modality) {
        super(parentFrame, "Group or Class Load Dialog", modality);
	setBounds(0, 0, 600, 150);
	setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);   
	GridBagLayout gridbag = new GridBagLayout();
	GridBagConstraints constraints = new GridBagConstraints();
	//constraints.fill = GridBagConstraints.BOTH;
	JPanel pane = new JPanel();
	pane.setLayout(gridbag);     
    constraints.anchor = GridBagConstraints.CENTER;    
    pane.setBackground(Color.white);
    JTextArea loadInfo = new JTextArea("Your experiment set is too big. Please check the manual about how to load group or class in file.");       
	buildConstraints(constraints, 0, 0, 1, 1, 20, 25);
	gridbag.setConstraints(loadInfo, constraints);
	pane.add(loadInfo);         
  
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
    
    
}




























