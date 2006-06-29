/*
 * Created on Nov 23, 2004
 */
package org.tigr.microarray.mev.cluster.gui.impl.usc;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import org.tigr.microarray.mev.r.ClassAssigner;
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
	private int result;
	
	private String[] userLabelArray;
	
	private JPanel mainPanel;
	
	//private Vector vCombo;
	//private Vector vLabel;
	
	private JButton loadButton;
	private JButton saveButton;
	
	private ClassAssigner ca;
	

	/**
	 * 
	 * @param hybArray		[ String ] of hyb names
	 * @param labelArray	[ String ] of labels entered by user in ClassDialog
	 */
	public USCAssignLabel( String[] hybArray, String[] labelArray ) {
		super( new JFrame(), "USCAssignLabel", true );
		
		this.userLabelArray = labelArray;
		
		/*
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		if( ( ( hybArray.length * 20 ) + 150 ) > screenSize.getHeight() ) {
			this.setSize( 555, ( int ) screenSize.getHeight() - 50 );
		} else {
			this.setSize( 555, ( hybArray.length * 20 ) + 150 );
		}
		*/
		this.setSize( 555, 600 );
		
		//this.initializeGUI( hybArray, this.userLabelArray );
		this.initGUI( hybArray, labelArray );
		
		//add the AlgorithmDialog Listener
		Listener l = new Listener();
		super.addWindowListener( l );
		super.setActionListeners( l );
	}//end constructor
	
	
	private void initGUI( String[] hybs, String[] labels ) {
		Dimension dLabel = new Dimension( 350, 20 );
		Dimension dCombo = new Dimension( 150, 20 );
		
		int iHyb = hybs.length;
		int iLabel = labels.length;
		
		JPanel selectionPanel = new JPanel( new SpringLayout() );
		selectionPanel.setBorder( BorderFactory.createTitledBorder( "Assign Labels" ) );
		
		this.ca = new ClassAssigner( hybs, labels, true, 3 );
		//JPanel assignPanel = this.ca.getMainPanel();
		
		//JScrollPane jsp = new JScrollPane( assignPanel );
		
		this.mainPanel = new JPanel();
		this.mainPanel.add( this.ca.getScrollPane(), BorderLayout.NORTH );
		this.mainPanel.add( this.createButtonPanel(), BorderLayout.SOUTH );
		this.addContent( this.mainPanel );
	}
	
	
	private JPanel createButtonPanel() {
		JPanel toReturn = new JPanel();
		toReturn.setLayout( new BoxLayout( toReturn, BoxLayout.X_AXIS ) );
		
		Dimension dButton = new Dimension( 150, 20 );
		
		String title = "Assignments Files";
		Border greyLine = BorderFactory.createLineBorder( Color.LIGHT_GRAY, 1 );
		Font font11 = new Font( "Arial", Font.PLAIN, 11 );
		TitledBorder border = BorderFactory.createTitledBorder( greyLine, 
				title, TitledBorder.LEADING, TitledBorder.TOP, font11 );
		toReturn.setBorder( border );
		
		this.loadButton = new JButton( "Load Assignments" );
		this.loadButton.setPreferredSize( dButton );
		
		this.saveButton = new JButton( "Save Assignments" );
		this.saveButton.setPreferredSize( dButton );
		
		//add listener
		AdvListener al = new AdvListener();
		this.loadButton.addActionListener( al );
		this.saveButton.addActionListener( al );
		
		toReturn.add( Box.createHorizontalGlue() );
		toReturn.add( this.saveButton );
		toReturn.add( Box.createRigidArea( new Dimension( 50, 20 ) ) );
		toReturn.add( this.loadButton );
		toReturn.add( Box.createHorizontalGlue() );
		
		return toReturn;
	}
	
	
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
	 * 
	 * @author iVu
	 */
	private class AdvListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			Object source = e.getSource();
			
			if( source == loadButton ) {
				ca.onLoadAssignments();
			} else if( source == saveButton ) {
				ca.onSaveAssignments();
			}
		}//end actionPerformed()
	}//end class
	
    
	/**
	 * The class to listen to the dialog and check boxes items events.
	 */
	private class Listener extends DialogListener implements ItemListener {
        
		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			if (command.equals("ok-command")) {
				if( ca.verifyLabeling() ) {
					result = JOptionPane.OK_OPTION;
					dispose();
				} else {
					//do nothing
				}
				/*
				if( validateLabels() ) {
					result = JOptionPane.OK_OPTION;
					dispose();
				} else {
					//do nothing
				}
				*/
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
		String[] toReturn = new String[ this.ca.getVComboBox().size() ];
		
		for( int i = 0; i < this.ca.getVComboBox().size(); i ++ ) {
			toReturn[ i ] = this.ca.getSelectedString( i );
		}
		
		return toReturn;
	}
}//end class





/**
 * 
 * @param hybArray
 * @param labelArray
 */
/*
private void initializeGUI( String[] hybArray, String[] labelArray ) {
	Dimension dLabel = new Dimension( 350, 20 );
	Dimension dCombo = new Dimension( 150, 20 );
	
	int iHyb = hybArray.length;
	int iLabel = labelArray.length;
	
	//this.vCombo = new Vector();
	//this.vLabel = new Vector();
	
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
		
		//this.vCombo.add( comboBox );
		//this.vLabel.add( label );
	}//end h (hybs)
	
	SpringUtilities.makeCompactGrid( selectionPanel, iHyb, 2, 0, 5 , 5, 0 );
	
	this.mainPanel = new JPanel();
	this.mainPanel.add( selectionPanel );
	
	JScrollPane jsp = new JScrollPane( this.mainPanel );
	
	this.addContent( jsp );
}//end initializeGUI()
*/
	/*
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
	*/


/**
 * Make sure that all the userLabels have been assigned to at least one hyb so
 * we don't have any unaccounted for labels
 * @return
 */
/*
private boolean validateLabels() {
	boolean toReturn = true;
	
	//make sure that all unique labels are represented
	String[] labels = this.getHybLabels();
	
	//loop through the labels entered by the user
	for( int i = 0; i < this.userLabelArray.length; i ++ ) {
		if( this.userLabelArray[ i ].equals( ClassAssigner.TEST_CLASS_STRING ) ) {
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
	
	if( toReturn ) {
		//should also validate number of hybs/label to make sure USC can run
		for( int i = 0; i < this.userLabelArray.length; i ++ ) {
			String label = this.userLabelArray[ i ];
			
			if( label.equals( ClassAssigner.TEST_CLASS_STRING ) ) {
				//don't do anything
			} else {
				int iHyb = 0;
				
				//now count the number of hybs for this label
				for( int j = 0; j < labels.length; j ++ ) {
					if( label.equalsIgnoreCase( labels[ j ] ) ) {
						iHyb ++;
					}
				}
				
				if( iHyb < 3 ) {
					//problem
					this.error( "There must be at least 3 experiments per class\r\n  " 
							+ label + " only contains " + iHyb );
					toReturn = false;
					break;
				}
			}
		}//end i
	}
	
	return toReturn;
}//validateLabels()
*/