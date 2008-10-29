/*
Copyright @ 1999-2005, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * TFAInitBox1.java
 *
 * Created on February 11, 2004, 12:20 PM
 */

package org.tigr.microarray.mev.cluster.gui.impl.GSEA;

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
 * @author  Sarita Nair
 */
public class GSEAInitBox2 extends AlgorithmDialog {

    boolean okPressed = false;
    JTextField factorANameField, factorBNameField, factorALevelsField, factorBLevelsField, factorCNameField, factorCLevelsField;
    int num_factors;
    int[] factor_levels=new int[1];
    /** Creates a new instance of GSEAInitBox2 */
    public GSEAInitBox2(JFrame parentFrame, boolean modality, int num_factors) {
        super(parentFrame, "GSEA - set factor names and levels", modality);
        setNumberofFactors(num_factors);
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
        
        if(num_factors==1){
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
        	
        	
        }
        
        
        if(num_factors==2){
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
        	
        	
        }
        
        if(num_factors==3){
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
        	
        	JLabel factorCName = new JLabel("Factor C name: ");
        	buildConstraints(constraints, 0, 2, 1, 1, 25, 50);
        	gridbag.setConstraints(factorCName, constraints);
        	pane.add(factorCName);   

        	factorCNameField = new JTextField("Factor C", 10);
        	buildConstraints(constraints, 1, 2, 1, 1, 25, 0);
        	gridbag.setConstraints(factorCNameField, constraints);
        	pane.add(factorCNameField);

        	JLabel factorCLevels = new JLabel("No. of levels of Factor C: ");
        	buildConstraints(constraints, 2, 2, 1, 1, 25, 0);
        	gridbag.setConstraints(factorCLevels, constraints);
        	pane.add(factorCLevels);

        	factorCLevelsField = new JTextField(10);
        	buildConstraints(constraints, 3, 2, 1, 1, 25, 0);
        	gridbag.setConstraints(factorCLevelsField, constraints);
        	pane.add(factorCLevelsField);   
        	
        	
        }
        
   
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
    
    public void setNumberofFactors(int num){
    	num_factors=num;
    }
    
    public int getNumberofFactors(){
    	return num_factors;
    }
    
    public String getFactorAName() {
        return factorANameField.getText();
    }
    
    public String getFactorBName() {
        return factorBNameField.getText();
    } 
    
    public String getFactorCName() {
        return factorCNameField.getText();
    } 
    
    
    public int getNumFactorALevels() {
        return Integer.parseInt(factorALevelsField.getText());
    }
    
    public int getNumFactorBLevels() {
        return Integer.parseInt(factorBLevelsField.getText()); 
    }
   
    public int getNumFactorCLevels() {
        return Integer.parseInt(factorCLevelsField.getText()); 
    }
   
    public int[]getAllFactorLevels(){
    	if(getNumberofFactors() ==3){
    		factor_levels=new int[3];
    		factor_levels[0]=Integer.parseInt(factorALevelsField.getText());
    		factor_levels[1]=Integer.parseInt(factorBLevelsField.getText());
    		factor_levels[2]=Integer.parseInt(factorCLevelsField.getText());
    	}else if(getNumberofFactors()==2){
    		factor_levels=new int[2];
        	factor_levels[0]=Integer.parseInt(factorALevelsField.getText());
        	factor_levels[1]=Integer.parseInt(factorBLevelsField.getText());	
    	}else if(getNumberofFactors() ==1){
    		factor_levels=new int[1];
        	factor_levels[0]=Integer.parseInt(factorALevelsField.getText());
    	}//May be need to make provision for factor ==0, what happens to level?
    	
    	

    	return this.factor_levels;
    }
    
    
    protected boolean isBlank(String str) {
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
                	if(getNumberofFactors()== 3){
                		if ((getNumFactorALevels() <= 1)||(getNumFactorBLevels() <= 1)||(getNumFactorCLevels() <= 1)) {
                			JOptionPane.showMessageDialog(null, "Number of levels per factor must be more than one", "Error", JOptionPane.ERROR_MESSAGE);
                			return;
                		}
                		if ( (getFactorAName().length() == 0) || (getFactorBName().length() == 0) || (getFactorCName().length() == 0)
                				|| (isBlank(getFactorCName()))||(isBlank(getFactorAName())) || (isBlank(getFactorBName())) ) {
                			JOptionPane.showMessageDialog(null, "Enter names for all factors", "Error", JOptionPane.ERROR_MESSAGE);
                			return;
                		}
                	}else if(getNumberofFactors()== 2){
                		if ((getNumFactorALevels() <= 1)||(getNumFactorBLevels() <= 1)) {
                			JOptionPane.showMessageDialog(null, "Number of levels per factor must be more than one", "Error", JOptionPane.ERROR_MESSAGE);
                			return;
                		}
                		if ( (getFactorAName().length() == 0) || (getFactorBName().length() == 0)
                				||(isBlank(getFactorAName())) || (isBlank(getFactorBName())) ) {
                			JOptionPane.showMessageDialog(null, "Enter names for all factors", "Error", JOptionPane.ERROR_MESSAGE);
                			return;
                		}
                
                	}else{
                		if ((getNumFactorALevels() <= 1)) {
                			JOptionPane.showMessageDialog(null, "Number of levels per factor must be more than one", "Error", JOptionPane.ERROR_MESSAGE);
                			return;
                		}
                		if ( (getFactorAName().length() == 0) ||(isBlank(getFactorAName())) ) {
                			JOptionPane.showMessageDialog(null, "Enter names for all factors", "Error", JOptionPane.ERROR_MESSAGE);
                			return;
                		}
                
                		
                	}
                	
                	
                	
                	
                	
                	
                	
                    okPressed = true;
                    dispose();
                } catch (NumberFormatException nfe) {
                    JOptionPane.showMessageDialog(null, "Invalid number of levels", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else if (command.equals("reset-command")) {
            	System.out.println("num_factors:"+num_factors);
            	if(num_factors == 3){
            		factorANameField.setText("");
            		factorBNameField.setText("");
            		factorCNameField.setText("");

            		factorALevelsField.setText("");
            		factorBLevelsField.setText("");
            		factorCLevelsField.setText("");
            	}else if(num_factors == 2){
            		factorANameField.setText("");
            		factorBNameField.setText("");
            		
            		factorALevelsField.setText("");
            		factorBLevelsField.setText("");
            	
            	}else{
            		factorANameField.setText("");
            		factorALevelsField.setText("");
            	}

                okPressed = false;
            } else if (command.equals("cancel-command")) {
                okPressed = false;
                dispose();
            } else if (command.equals("info-command")){

		}
        }
        
    }     
    
    public static void main(String[] args) {
        
    GSEAInitBox2 tBox = new GSEAInitBox2(new JFrame(), true, 2);
    tBox.setVisible(true);
    System.out.println("Factor A = " + (tBox.getFactorAName()).length());
    //String s = new String();
    
    }    
    
}
