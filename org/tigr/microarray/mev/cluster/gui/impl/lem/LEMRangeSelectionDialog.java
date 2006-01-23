/*
Copyright @ 1999-2006, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * Created on May 10, 2005
 */
package org.tigr.microarray.mev.cluster.gui.impl.lem;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.ParameterPanel;

/**
 * @author braisted
 * 
 * Allows range selection of loci
 */
public class LEMRangeSelectionDialog extends AlgorithmDialog {

	private int result = JOptionPane.CANCEL_OPTION;
	private JTextField lowField;
	private JTextField highField;
	private int upperCoord;
	
	/**
	 * Contructs the range selection dialog
	 * @param frame parent component
	 * @param lowerLimit initial lower limit
	 * @param upperLimit initial upper limit
	 */
	public LEMRangeSelectionDialog(JFrame frame, int lowerLimit, int upperLimit) {
		super(frame, "Locus Range Selection", true);
		
		upperCoord = upperLimit;		
		
		JLabel rangeLabel = new JLabel("Base Range Limits in Viewer:  ["+String.valueOf(lowerLimit)+", "+String.valueOf(upperLimit)+"]");
		
		JLabel lowLabel = new JLabel("Lower Base Location Limit: ");
		lowField = new JTextField(12);
		Dimension dim = new Dimension(140, 20);
		lowField.setPreferredSize(dim);
		lowField.setSize(dim);
		
		JLabel highLabel = new JLabel("Higher Base Location Limit: ");
		highField = new JTextField(12);		
		highField.setPreferredSize(dim);
		highField.setSize(dim);
		
		ParameterPanel panel = new ParameterPanel("Base Location Limits");
		panel.setLayout(new GridBagLayout());

		panel.add(rangeLabel, new GridBagConstraints(0,0,2,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(20,0,15,0),0,0));
		panel.add(lowLabel, new GridBagConstraints(0,1,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10,0,15,0),0,0));
		panel.add(lowField, new GridBagConstraints(1,1,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10,10,15,0),0,0));
		panel.add(highLabel, new GridBagConstraints(0,2,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,20,0),0,0));
		panel.add(highField, new GridBagConstraints(1,2,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,10,20,0),0,0));
		
		addContent(panel);
		setActionListeners(new Listener());
		
		pack();
	}
	
	/**
	 * Displays the dialog
	 * @return returns int values from <code>JOptionPane</code> such as OK_OPTION, CANCEL_OPTION
	 */
	public int showModal() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);
        show();
        return result;
	}
	
	/**
	 * Returns the selected lower limit
	 * @return lower limit coord.
	 */
	public int getLowerLimit() {
		return Integer.parseInt(lowField.getText());
	}

	/**
	 * returns the selected upper limit coord.
	 * @return upper limit coord.
	 */
	public int getUpperLimit() {
		return Integer.parseInt(highField.getText());
	}
	
	/**
	 * True if limits are valid, else false.
	 * Checks min < max
	 * @return true if valid, else false
	 */
	private boolean validateValues() {
		boolean valid = true;
		int v1, v2;
		try {
			v1 = Integer.parseInt(lowField.getText());
			v2 = Integer.parseInt(highField.getText());
			
			if(v2 <= v1) {
				valid = false;
				JOptionPane.showMessageDialog(this, "Range error: Upper Limit must be greater than lower limit.  Please check the entries.", "Range Error", JOptionPane.ERROR_MESSAGE);				
			} else if(v1 > upperCoord) {
				valid = false;
				JOptionPane.showMessageDialog(this, "Range error: The lower limit is off of the supplied base range.  Please check the entries.", "Range Error", JOptionPane.ERROR_MESSAGE);								
			} else if(v1 < 0 || v2 < 0) {
				valid = false;
				JOptionPane.showMessageDialog(this, "Range error: Values should be >= 0.  Please check the entries.", "Range Error", JOptionPane.ERROR_MESSAGE);												
			}
			
		} catch (NumberFormatException nfe) {
			JOptionPane.showMessageDialog(this, "The entered values must be integer values.  Please check the entries.", "Number Format Problem", JOptionPane.ERROR_MESSAGE);
			valid = false;
		}
		return valid;
	}
	
	/**
	 * @author braisted
	 *
	 * Handles dialog events
	 */
	public class Listener implements ActionListener {

		public void actionPerformed(ActionEvent ae) {
			String command = ae.getActionCommand();
			if(command.equals("ok-command")) {
				if(validateValues()) {
					result = JOptionPane.OK_OPTION;
					dispose();
				}
			} else if(command.equals("cancel-command")) {
				result = JOptionPane.CANCEL_OPTION;
				dispose();
			} else if(command.equals("reset-command")) {
				lowField.setText("");
				highField.setText("");
			} else if(command.equals("info-command")) {
				
			} 
		}
		
	}
	
	public static void main(String [] args) {
		LEMRangeSelectionDialog dialog = new LEMRangeSelectionDialog(new JFrame(), 154, 12345);
		dialog.showModal();
	}
	
}
