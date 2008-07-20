/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * Created on Aug 16, 2005
 */
package org.tigr.microarray.mev.r;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.HelpWindow;


/**
 * @author iVu
 */
public class RamaConnectionDialog extends AlgorithmDialog {
	private int result;
	
	
	public static void main( String[] args ) {
		RamaConnectionDialog rcd = new RamaConnectionDialog( new Frame() );
		rcd.showModal();
		System.exit( 0 );
	}
	
	
	/**
	 * @param parent
	 * @param title
	 * @param modal
	 */
	public RamaConnectionDialog(Frame parent) {
		super( parent, "Rconnection", true );
		this.setResizable( true );
		this.setSize( 350, 200 );
		
		Listener listener = new Listener();
		super.addWindowListener(listener);
		super.setActionListeners(listener);
		
		this.addContent( this.initializeGUI() );
	}
	
	
	private JPanel initializeGUI() {
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout( new BoxLayout( mainPanel, BoxLayout.Y_AXIS ) );
		JLabel label = new JLabel( "You need to start the Rserve Daemon" );
		JLabel label2 = new JLabel( "Click on the info button (lower left) for more information" );
		JLabel label3 = new JLabel( "When Rserve is running, click OK" );
		label.setFont(new Font("Arial", Font.PLAIN, 14));
		label2.setFont(new Font("Arial", Font.PLAIN, 12));
		label3.setFont(new Font("Arial", Font.PLAIN, 14));
		JPanel panel1 = new JPanel();
		JPanel panel2 = new JPanel();
		JPanel panel3 = new JPanel();
		panel1.add( label );
		panel2.add( label2 );
		panel3.add( label3 );
		mainPanel.add( Box.createVerticalGlue() );
		mainPanel.add( panel1 );
		mainPanel.add( panel2 );
		mainPanel.add( panel3 );
		mainPanel.add( Box.createVerticalGlue() );
		return mainPanel;
	}
	
	
	/**
	 * Shows the dialog.
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
				result = JOptionPane.CANCEL_OPTION;
				return;
			} else if (command.equals("info-command")) {
				HelpWindow hw = new HelpWindow(RamaConnectionDialog.this, "RAMA Connection Dialog");
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