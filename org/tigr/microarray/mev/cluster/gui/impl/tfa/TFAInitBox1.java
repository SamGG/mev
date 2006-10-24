/*
Copyright @ 1999-2005, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * TFAInitBox1.java
 *
 * Created on February 11, 2004, 12:20 PM
 */

package org.tigr.microarray.mev.cluster.gui.impl.tfa;

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
public class TFAInitBox1 extends AlgorithmDialog {

    boolean okPressed = false;
    JTextField factorANameField, factorBNameField, factorALevelsField, factorBLevelsField;
    
    /** Creates a new instance of TFAInitBox1 */
    public TFAInitBox1(JFrame parentFrame, boolean modality) {
        super(parentFrame, "TFA - set factor names and levels", modality);
        okButton.setText("Next >");
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
        
        JLabel factorAName = new JLabel("Factor A name: ");
        buildConstraints(constraints, 0, 0, 1, 1, 25, 50);
        gridbag.setConstraints(factorAName, constraints);
        pane.add(factorAName);
        
        factorANameField = new JTextField("Factor A", 10);
        buildConstraints(constraints, 1, 0, 1, 1, 25, 0);
        gridbag.setConstraints(factorANameField, constraints);
        pane.add(factorANameField);
        
        JLabel factorALevels = new JLabel("No. of levels of Factor A: ");
        buildConstraints(constraints, 2, 0, 1, 1, 25, 0);
        gridbag.setConstraints(factorALevels, constraints);
        pane.add(factorALevels);
        
        factorALevelsField = new JTextField(10);
        buildConstraints(constraints, 3, 0, 1, 1, 25, 0);
        gridbag.setConstraints(factorALevelsField, constraints);
        pane.add(factorALevelsField);        
        
        JLabel factorBName = new JLabel("Factor B name: ");
        buildConstraints(constraints, 0, 1, 1, 1, 25, 50);
        gridbag.setConstraints(factorBName, constraints);
        pane.add(factorBName);   
        
        factorBNameField = new JTextField("Factor B", 10);
        buildConstraints(constraints, 1, 1, 1, 1, 25, 0);
        gridbag.setConstraints(factorBNameField, constraints);
        pane.add(factorBNameField);
        
        JLabel factorBLevels = new JLabel("No. of levels of Factor B: ");
        buildConstraints(constraints, 2, 1, 1, 1, 25, 0);
        gridbag.setConstraints(factorBLevels, constraints);
        pane.add(factorBLevels);
        
        factorBLevelsField = new JTextField(10);
        buildConstraints(constraints, 3, 1, 1, 1, 25, 0);
        gridbag.setConstraints(factorBLevelsField, constraints);
        pane.add(factorBLevelsField);      
        
        addContent(pane);
        EventListener listener = new EventListener();        
        setActionListeners(listener);
        this.addWindowListener(listener);        

        if (parentFrame==null)//AMP
        cancelButton.setEnabled(false);
  
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
    
    public String getFactorAName() {
        return factorANameField.getText();
    }
    
    public String getFactorBName() {
        return factorBNameField.getText();
    } 
    
    public int getNumFactorALevels() {
        return Integer.parseInt(factorALevelsField.getText());
    }
    
    public int getNumFactorBLevels() {
        return Integer.parseInt(factorBLevelsField.getText()); 
    }
    
    private boolean isBlank(String str) {
        boolean blank = true;
        char[] charArr = str.toCharArray();
        for (int i = 0; i < charArr.length; i++) {
            if (charArr[i] != ' ') {
                blank = false;
                break;
            }
        }
        return blank;
    }
    
    public class EventListener extends WindowAdapter implements ActionListener{
        
        public void actionPerformed(ActionEvent ae) {
            String command = ae.getActionCommand();
            if(command.equals("ok-command")){
                try {
                    if ((getNumFactorALevels() <= 1)||(getNumFactorBLevels() <= 1)) {
                        JOptionPane.showMessageDialog(null, "Invalid number of levels", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    if ( (getFactorAName().length() == 0) || (getFactorBName().length() == 0) || (isBlank(getFactorAName())) || (isBlank(getFactorBName())) ) {
                        JOptionPane.showMessageDialog(null, "Enter names for both factors", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    okPressed = true;
                    dispose();
                } catch (NumberFormatException nfe) {
                    JOptionPane.showMessageDialog(null, "Invalid number of levels", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else if (command.equals("reset-command")) {
                factorANameField.setText("");
                factorBNameField.setText("");
                factorALevelsField.setText("");
                factorBLevelsField.setText("");
                okPressed = false;
            } else if (command.equals("cancel-command")) {
                okPressed = false;
                dispose();
            } else if (command.equals("info-command")){

		}
        }
        
    }     
    
    public static void main(String[] args) {
        
    TFAInitBox1 tBox = new TFAInitBox1(new JFrame(), true);
    tBox.setVisible(true);
    System.out.println("Factor A = " + (tBox.getFactorAName()).length());
    //String s = new String();
    
    }    
    
}
