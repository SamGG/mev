package org.tigr.microarray.mev.cluster.gui.impl.GSEA;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;

import org.tigr.microarray.mev.cluster.gui.impl.GSEA.GSEAInitBox2.EventListener;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog;
public class GSEAInitBox1 extends AlgorithmDialog{
	 
	private boolean okPressed = false;
	private JTextField number_factorField;
	   
	public GSEAInitBox1(JFrame parent, String title, boolean modal) {

		super(parent, "GSEA - Set Number of Factors", modal);
		 	okButton.setText("Next >");
	        setBounds(0, 0, 600, 200);
	        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); 
	        
	        GridBagLayout gridbag = new GridBagLayout();
	        GridBagConstraints constraints = new GridBagConstraints();
	        constraints.fill = GridBagConstraints.NONE;
	        
	        JPanel pane = new JPanel();
	        pane.setBorder(new EtchedBorder());
	        pane.setLayout(gridbag);   
	        
	        JLabel factorLabel = new JLabel("Enter number of factors: ");
	        buildConstraints(constraints, 0, 0, 1, 1, 25, 50);
	        gridbag.setConstraints(factorLabel, constraints);
	        pane.add(factorLabel);
	        
	        number_factorField = new JTextField("", 10);
	        buildConstraints(constraints, 1, 0, 1, 1, 25, 0);
	        gridbag.setConstraints(number_factorField, constraints);
	        pane.add(number_factorField);
	        
	        addContent(pane);
	        EventListener listener = new EventListener();        
	        setActionListeners(listener);
	        this.addWindowListener(listener);
	       
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
	
	
	 public String getNumberofFactors() {
	        return number_factorField.getText();
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
	                    if ((Integer.parseInt(getNumberofFactors()) > 3)) {
	                        JOptionPane.showMessageDialog(null, "Maximum number of factors is three", "Error", JOptionPane.ERROR_MESSAGE);
	                        return;
	                    }
	                    //If number of factors is zero, no need to go to the next screen
	                    if ((Integer.parseInt(getNumberofFactors()) == 0 )) {
	                    	  JOptionPane.showMessageDialog(null, "You must have atleast one factor", "Error", JOptionPane.ERROR_MESSAGE); 
	                    	//okPressed = false;
	                         //dispose();
	                        return;
	                    }else{
	                    	okPressed = true;
	                    }
	                    
	                  //  okPressed = true;
	                    dispose();
	                } catch (NumberFormatException nfe) {
	                    JOptionPane.showMessageDialog(null, "Invalid number of factors", "Error", JOptionPane.ERROR_MESSAGE);
	                }
	            } else if (command.equals("reset-command")) {
	                number_factorField.setText("");
	                okPressed = false;
	            } else if (command.equals("cancel-command")) {
	                okPressed = false;
	                dispose();
	            } else if (command.equals("info-command")){

			}
	        }
	        
	    }     

	
	
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		GSEAInitBox1 tBox = new GSEAInitBox1(new JFrame(), null,true);
	    tBox.setVisible(true);
	  //  System.out.println("Factor A = " + (tBox.getFactorAName()).length());
	    //String s = new String();
	    
	}

}
