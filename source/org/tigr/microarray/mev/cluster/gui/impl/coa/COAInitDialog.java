/*
 * COAInitDialog.java
 *
 * Created on September 16, 2004, 4:22 PM
 */

package org.tigr.microarray.mev.cluster.gui.impl.coa;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;

import org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog;

/**
 *
 * @author  nbhagaba
 */
public class COAInitDialog extends AlgorithmDialog {
    
    boolean okPressed = false;  
    JTextField numNeighborsField;
    
    /** Creates a new instance of COAInitDialog */
    public COAInitDialog(JFrame parentFrame, boolean modality) {
        super(parentFrame, "COA", modality);  
        setBounds(0, 0, 600, 200);
        setBackground(Color.white);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);   
        
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.NONE;       
        
        JPanel pane = new JPanel();
        pane.setBackground(Color.white);
        pane.setBorder(new EtchedBorder());
        pane.setLayout(gridbag);       
        
        JLabel numNeibsLabel= new JLabel("Number for neighbors for KNN imputation :");
        buildConstraints(constraints, 0, 0, 1, 1, 50, 100);
        gridbag.setConstraints(numNeibsLabel, constraints);
        pane.add(numNeibsLabel);     
        
        numNeighborsField = new JTextField("10", 7);
        buildConstraints(constraints, 1, 0, 1, 1, 50, 100);
        gridbag.setConstraints(numNeighborsField, constraints);
        pane.add(numNeighborsField);
        
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
    
    public int getNumNeighbors() {
        return Integer.parseInt(numNeighborsField.getText());
    }
    
    public class EventListener extends WindowAdapter implements ActionListener{
        
        public void actionPerformed(ActionEvent ae) {
            String command = ae.getActionCommand();
            if(command.equals("ok-command")){
                try {
                    int numNeibs = getNumNeighbors();
                    if (numNeibs <= 0) {
                        JOptionPane.showMessageDialog(null, "Invalid number of neighbors", "Error", JOptionPane.ERROR_MESSAGE);
                        return;                        
                    } else {
                        okPressed = true;
                        dispose();
                    }
                } catch (NumberFormatException nfe){
                    JOptionPane.showMessageDialog(null, "Invalid number of neighbors", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } else if (command.equals("reset-command")) {
                numNeighborsField.setText("10");
            } else if (command.equals("cancel-command")) {
                okPressed = false;
                dispose();
            } else if (command.equals("info-command")){                
            }
        }
    }
    
    public static void main (String[] args) {
        COAInitDialog cd = new COAInitDialog(new JFrame(), true);
        cd.setVisible(true);
    }
    
}
