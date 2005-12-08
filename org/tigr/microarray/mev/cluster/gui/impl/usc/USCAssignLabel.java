/*
 * Created on Nov 23, 2004
 */
package org.tigr.microarray.mev.cluster.gui.impl.usc;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SpringLayout;

import org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.HelpWindow;

/**
 * Dialog box for user to choose what type of analysis to run.  If training, user
 * enters the class labels here.
 * 
 * @author vu
 */
public class USCAssignLabel extends AlgorithmDialog {
	static final String TEST_LABEL = "Unknown (Test)";
	
	private int result;
	
	private String[] userLabelArray;
	
	private JPanel mainPanel;
	
	private Vector vCombo;
	private Vector vLabel;
	

	/**
	 * 
	 * @param hybArray		[ String ] of hyb names
	 * @param labelArray	[ String ] of labels entered by user in ClassDialog
	 */
	public USCAssignLabel( String[] hybArray, String[] labelArray ) {
		super( new JFrame(), "USCAssignLabel", true );
		
		this.userLabelArray = labelArray;

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		if( ( ( hybArray.length * 20 ) + 150 ) > screenSize.getHeight() ) {
			this.setSize( 555, ( int ) screenSize.getHeight() - 50 );
		} else {
			this.setSize( 555, ( hybArray.length * 20 ) + 150 );
		}
		
		this.initializeGUI( hybArray, this.userLabelArray );
		
		//add the AlgorithmDialog Listener
		Listener l = new Listener();
		super.addWindowListener( l );
		super.setActionListeners( l );
	}//end constructor
	
	
	/**
	 * 
	 * @param hybArray
	 * @param labelArray
	 */
	private void initializeGUI( String[] hybArray, String[] labelArray ) {
		Dimension dLabel = new Dimension( 350, 20 );
		Dimension dCombo = new Dimension( 150, 20 );
		
		int iHyb = hybArray.length;
		int iLabel = labelArray.length;
		
		this.vCombo = new Vector();
		this.vLabel = new Vector();
		
		//
		JPanel selectionPanel = new JPanel( new SpringLayout() );
		selectionPanel.setBorder( BorderFactory.createTitledBorder( "Assign Labels" ) );
		
		//loop through hybs
		for( int h = 0; h < iHyb; h ++ ) {
			JLabel label = new JLabel( hybArray[ h ] );
			label.setMaximumSize( dLabel );
			label.setMinimumSize( dLabel );
			label.setPreferredSize( dLabel );
			label.setBackground( Color.WHITE );
			label.setHorizontalAlignment( JLabel.LEFT );
			label.setVerticalAlignment( JLabel.CENTER );
			
			JComboBox comboBox = new JComboBox( labelArray );
			comboBox.setMaximumSize( dCombo );
			comboBox.setMinimumSize( dCombo );
			comboBox.setPreferredSize( dCombo );

			selectionPanel.add( comboBox ); 
			selectionPanel.add( label );
			
			this.vCombo.add( comboBox );
			this.vLabel.add( label );
		}//end h (hybs)
		
		SpringUtilities.makeCompactGrid( selectionPanel, iHyb, 2, 0, 5 , 5, 0 );
		
		this.mainPanel = new JPanel();
		this.mainPanel.add( selectionPanel );
		
		JScrollPane jsp = new JScrollPane( this.mainPanel );
		
		this.addContent( jsp );
	}//end initializeGUI()
	
	
	/**
	 * Displays a dialog box with a message
	 * @param message
	 */
	private void error( String message ) {
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
	 * Make sure that all the userLabels have been assigned to at least one hyb so
	 * we don't have any unaccounted for labels
	 * @return
	 */
	private boolean validateLabels() {
		boolean toReturn = true;
		
		//make sure that all unique labels are represented
		String[] labels = this.getHybLabels();
		
		//loop through the labels entered by the user
		for( int i = 0; i < this.userLabelArray.length; i ++ ) {
			if( this.userLabelArray[ i ].equals( USCAssignLabel.TEST_LABEL ) ) {
				//do nothing because user doesn't have to test anything if they don't want to 
			} else {
				boolean labelFound = false;
				
				//make sure that this label appears at least once in the assignments
				for( int j = 0; j < labels.length; j ++ ) {
					if( this.userLabelArray[ i ].toLowerCase().equals( labels[ j ].toLowerCase() ) ) {
						labelFound = true;
						break;
					}
				}
				
				if( labelFound ) {
					//proceed
				} else {
					//wasn't found, return false
					this.error( "You haven't assigned " + this.userLabelArray[ i ].toUpperCase() 
					+ " to any hybs." );
					toReturn = false;
					break;
				}
			}
		}
		
		return toReturn;
	}//validateLabels()
	
    
	/**
	 * The class to listen to the dialog and check boxes items events.
	 */
	private class Listener extends DialogListener implements ItemListener {
        
		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			if (command.equals("ok-command")) {
				if( validateLabels() ) {
					result = JOptionPane.OK_OPTION;
					dispose();
				} else {
					//do nothing
				}
			} else if (command.equals("cancel-command")) {
				result = JOptionPane.CANCEL_OPTION;
				dispose();
			} else if (command.equals("reset-command")) {
				//resetControls();
				result = JOptionPane.CANCEL_OPTION;
				return;
			} else if (command.equals("info-command")) {
				HelpWindow hw = new HelpWindow(USCAssignLabel.this, "USC Assign Label Dialog");
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
	
	
	public static void main( String[] args ) {
		System.out.println( "invoked by main()" );
		
		String[] hybs = { "011101_16011521000000_S01_A01.txt", 
			"011101_16011521000001_S01_A01.txt", 
			"011101_16011521000002_S01_A01.txt", 
			"011101_16011521000003_S01_A01.txt", 
			"011101_16011521000004_S01_A01.txt", 
			"011101_16011521000005_S01_A01.txt", 
			"011101_16011521000006_S01_A01.txt", 
			"011101_16011521000007_S01_A01.txt", 
			"011101_16011521000008_S01_A01.txt", 
			"011101_16011521000009_S01_A01.txt", 
			"011101_16011521000010_S01_A01.txt", 
			 };
		String[] labels = { "tumor", "normal", "flu"  };
		
		USCAssignLabel d = new USCAssignLabel( hybs, labels );
		d.showModal();
	}
	
	
	//--------------------------------------Getters & Setters----------------------------------
	public String[] getHybLabels() {
		String[] toReturn = new String[ this.vCombo.size() ];
		
		for( int i = 0; i < this.vCombo.size(); i ++ ) {
			JComboBox combo = ( JComboBox ) this.vCombo.elementAt( i );
			toReturn[ i ] = ( String ) combo.getSelectedItem();
		}
		
		return toReturn;
	}
	public String getHybLabel( int hybIndex ) {
		JComboBox combo = ( JComboBox ) this.vCombo.elementAt( hybIndex );
		return ( String ) combo.getSelectedItem();
	}
	public String[][] getHybLabelPairs() {
		String[][] toReturn = new String[ this.vCombo.size() ][ 2 ];
		
		for( int i = 0; i < this.vCombo.size(); i ++ ) {
			JLabel label = ( JLabel ) this.vLabel.elementAt( i );
			toReturn[ i ][ 0 ] = label.getText();
			
			JComboBox combo = ( JComboBox ) this.vCombo.elementAt( i );
			toReturn[ i ][ 1 ] = ( String ) combo.getSelectedItem();
		}
		
		return toReturn;
	}
	public String[] getHybLabelPair( int hybIndex ) {
		String[] toReturn = new String[ 2 ];
		
		JLabel label = ( JLabel ) this.vLabel.elementAt( hybIndex );
		toReturn[ 0 ] = label.getText();
		
		JComboBox combo = ( JComboBox ) this.vCombo.elementAt( hybIndex );
		toReturn[ 1 ] = ( String ) combo.getSelectedItem();
		
		return toReturn;
	}
}//end class