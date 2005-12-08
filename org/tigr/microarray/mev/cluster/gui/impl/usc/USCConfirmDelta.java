/*
 * Created on Apr 7, 2005
 */
package org.tigr.microarray.mev.cluster.gui.impl.usc;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.HelpWindow;

/**
 * @author iVu
 * 
 * Dialog to allow users to change the value of Delta or Rho for testing
 */
public class USCConfirmDelta extends AlgorithmDialog {
    private int result = 0;
    private double delta;
    private double rho;
    
    private JSpinner deltaSpinner;
    private JSpinner rhoSpinner;
    

    /**
     * @param frame
     * @param title
     * @param modal
     */
    public USCConfirmDelta(Frame frame, double sDelta, double sRho) {
        super((JFrame)frame, "Confirm Delta & Rho", true);
        this.setSize(250,200);
        
        this.delta = sDelta;
        this.rho = sRho;
        
		Listener listener = new Listener();
		super.addWindowListener(listener);
		super.setActionListeners(listener);
		
		this.createGUI();
    }//end constructor
    
    
    private void createGUI() {
        JPanel mainPanel = new JPanel();
        
        JPanel labelPanel = new JPanel();
        JLabel instructionLabel = new JLabel("Change Delta and Rho if you wish");
        labelPanel.add(instructionLabel);
        
        JLabel deltaLabel = new JLabel("Delta:");
        JLabel rhoLabel = new JLabel("Rho:");
        
        JPanel spinnerLabelPanel = new JPanel();
        BoxLayout spinnerLabelBoxLayout = new BoxLayout( spinnerLabelPanel, BoxLayout.Y_AXIS);
        spinnerLabelPanel.setLayout(spinnerLabelBoxLayout);
        
        spinnerLabelPanel.add(deltaLabel);
        spinnerLabelPanel.add(rhoLabel);
        
        JPanel spinnerPanel = new JPanel();
        BoxLayout spinnerBoxLayout = new BoxLayout( spinnerPanel, BoxLayout.Y_AXIS);
        spinnerPanel.setLayout(spinnerBoxLayout);
        
		SpinnerNumberModel deltaModel = new SpinnerNumberModel( delta, 0.0, 50, 0.1 );
		SpinnerNumberModel rhoModel = new SpinnerNumberModel( rho, 0.0, 1.0, 0.1 );
        
        this.deltaSpinner = new JSpinner(deltaModel);
        this.rhoSpinner = new JSpinner(rhoModel);
        
        spinnerPanel.add(deltaSpinner);
        spinnerPanel.add(rhoSpinner);
        
        JPanel centerPanel = new JPanel();
        centerPanel.add(spinnerLabelPanel);
        centerPanel.add(spinnerPanel);
        
        mainPanel.add(labelPanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        
        this.addContent(mainPanel);
    }//createGUI()
    
    
    public double getDelta() {
		SpinnerNumberModel model = ( SpinnerNumberModel ) this.deltaSpinner.getModel();
		return model.getNumber().doubleValue();
    }
    public double getRho() {
		SpinnerNumberModel model = ( SpinnerNumberModel ) this.rhoSpinner.getModel();
		return model.getNumber().doubleValue();
    }
    
    
    public static void main( String[] Args ) {
        USCConfirmDelta ucd = new USCConfirmDelta( new JFrame(), .8, .8 );
        int iTest = ucd.showModal();
        if( iTest == JOptionPane.OK_OPTION ) {
            System.out.println("OK_OPTION" + "\tdelta=" + ucd.getDelta());
        } else {
            System.out.println("Not OK_OPTION");
        }
    }
	
	
	/**
	 * Displays a dialog box with a message
	 * @param message
	 */
	public void error( String message ) {
		JOptionPane.showMessageDialog( this, message, "Input Error", JOptionPane.ERROR_MESSAGE );
	}//end error()
	
    
	/**
	 * Shows this AlgorithmDialog
	 * @return	
	 */
	public int showModal() {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);
		show();
		return result;
	}//end showModal()


/**
 * The class to listen to the dialog and check boxes items events.
 */
private class Listener extends DialogListener implements ItemListener {
    
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		if (command.equals("ok-command")) {
			result = JOptionPane.OK_OPTION;
			dispose();
		} else if (command.equals("cancel-command")) {
			result = JOptionPane.CANCEL_OPTION;
			dispose();
		} else if (command.equals("reset-command")) {
			//resetControls();
			result = JOptionPane.CANCEL_OPTION;
			return;
		} else if (command.equals("info-command")) {
			HelpWindow hw = new HelpWindow(USCConfirmDelta.this, "USC Confirm Dialog");
			result = JOptionPane.CANCEL_OPTION;
			if(hw.getWindowContent()){
				hw.setSize(450,600);
				hw.setLocation();
				hw.show();
				return;
			} else {
				hw.setVisible(false);
				hw.dispose();
				return;
			}
		}
		//dispose();
	}//end actionPerformed()
    
	public void itemStateChanged(ItemEvent e) {
		//okButton.setEnabled(genes_box.isSelected() || cluster_box.isSelected());
	}
    
	public void windowClosing(WindowEvent e) {
		result = JOptionPane.CLOSED_OPTION;
		dispose();
	}
}//end internal Listener class
}//end class
