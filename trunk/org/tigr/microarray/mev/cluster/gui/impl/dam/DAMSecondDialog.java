/*
Copyright @ 1999-2005, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * DAMSecondDialog.java
 *
 */

package org.tigr.microarray.mev.cluster.gui.impl.dam;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;

import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.EtchedBorder;

import org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog;

public class DAMSecondDialog extends AlgorithmDialog {
    
    boolean okPressed = false;
    JRadioButton stopButton, continueButton;
    
    /** Creates a new instance of DAMSecondDialog */
    public DAMSecondDialog(JFrame parentFrame, boolean modality) {
        super(parentFrame, "DAM classify - continue with analysis?", modality);
        //okButton.setText("Next >");
        setBounds(0, 0, 500, 200);
        setBackground(Color.white);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); 
        
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.NONE;
        
        JPanel pane = new JPanel();
        pane.setBackground(Color.white);
        pane.setBorder(new EtchedBorder());
        pane.setLayout(gridbag);   
        
        stopButton = new JRadioButton("Stop here, don't classify", true);
        stopButton.setBackground(Color.white);
        continueButton = new JRadioButton("Proceed with classification", false);
        continueButton.setBackground(Color.white);     
        
        ButtonGroup stopOrContinue =new ButtonGroup();
        stopOrContinue.add(stopButton);
        stopOrContinue.add(continueButton); 
        
        buildConstraints(constraints, 0, 0, 1, 1, 100, 50);
        gridbag.setConstraints(stopButton, constraints);
        pane.add(stopButton);

        buildConstraints(constraints, 0, 1, 1, 1, 0, 50);
        gridbag.setConstraints(continueButton, constraints);
        pane.add(continueButton);        
        
        addContent(pane);
        EventListener listener = new EventListener();        
        setActionListeners(listener);
        this.addWindowListener(listener);        
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
    
    public boolean proceed() {
        return continueButton.isSelected();
    }
    
    public class EventListener extends WindowAdapter implements ActionListener{
        
        public void actionPerformed(ActionEvent ae) {
            String command = ae.getActionCommand();
            if(command.equals("ok-command")){
                okPressed = true;
                dispose();
            } else if (command.equals("reset-command")) {
                okPressed = false;
                stopButton.setSelected(true);
                continueButton.setSelected(false);
            } else if (command.equals("cancel-command")) {
                okPressed = false;
                dispose();
            } else if (command.equals("info-command")){
                /*
                HelpWindow hw = new HelpWindow(DAMSecondDialog.this, "DAM Second Dialog");
                okPressed = false;
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
                 */
		}
        }
        
    }    
    
}
