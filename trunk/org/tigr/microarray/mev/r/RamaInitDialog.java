/*
 * Created on Jul 1, 2005
 */
package org.tigr.microarray.mev.r;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SpringLayout;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import org.tigr.microarray.mev.TMEV;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.HelpWindow;

/**
 * @author iVu
 */
public class RamaInitDialog extends AlgorithmDialog {
	private int result;
	private int dataType;
	private boolean connAdded = false;
	
	private RHyb[] ramaHybs;
	
	private Vector vCy3Radio;
	private Vector vRamaHyb;
	
	private JCheckBox advCheckBox;
	private JSpinner burnInSpinner;
	private JSpinner numIterSpinner;
	private JLabel burnInLabel;
	private JLabel numIterLabel;
	private JButton addConnButton;
	private JComboBox connCombo;
	private JTextField newConn;
	private JCheckBox allOutBox;
	private JLabel allOutLabel;
	
	public static String DEFAULT_ADD_TEXT = "or enter a new server";
	

	/**
	 * @param parent
	 * @param title
	 * @param modal
	 */
	public RamaInitDialog( Frame parent, String[] hybNames, int dataTypeP ) {
		super( parent, "RamaInitDialog", true );
		this.setResizable( true );
		this.setSize( 600, 500 );
		this.dataType = dataTypeP;
		
		Listener listener = new Listener();
		super.addWindowListener(listener);
		super.setActionListeners(listener);
		
		//initialize variables
		this.vCy3Radio = new Vector();
		this.vRamaHyb = new Vector();
		this.ramaHybs = new RHyb[ hybNames.length ];
		
		JPanel mainPanel = new JPanel();
		mainPanel.add( this.createTwoColorPanel( hybNames, this.dataType ), BorderLayout.CENTER );
		mainPanel.add( this.createParamPanel(), BorderLayout.SOUTH );
		this.addContent( mainPanel );
	}//constructor
	
	
	/**
	 * This is the main content window of the dialog box
	 * @return
	 */
	private JPanel createParamPanel() {
		Font font11 = new Font( "Arial", Font.PLAIN, 11 );
		Font font = new Font( "Arial", Font.PLAIN, 12 );
		Dimension dSpinner = new Dimension( 70, 25 );
		Dimension dArea = new Dimension( 140, 80 );
		Dimension dCombo = new Dimension( 170, 25 );
		Dimension dSpace = new Dimension( 100, 5 );
		Dimension dButton = new Dimension( 60, 20 );
		Dimension dAddConn = new Dimension( 120, 20 );
		
		//create a JCheckBox allowing user to activate Adv Params
		this.advCheckBox = new JCheckBox( "Advanced Parameters" );
		this.advCheckBox.setFont( font11 );
		AdvListener al = new AdvListener();
		this.advCheckBox.addActionListener( al );
		JPanel checkPanel = new JPanel();
		checkPanel.add( this.advCheckBox );
		
		//create adv params
		SpinnerNumberModel burnInModel = new SpinnerNumberModel( 1000, 1, 100000, 1 );
		SpinnerNumberModel numIterModel = new SpinnerNumberModel( 21000, 1, 100000, 1 );
		this.burnInSpinner = new JSpinner( burnInModel );
		this.numIterSpinner = new JSpinner( numIterModel );
		this.burnInSpinner.setPreferredSize( dSpinner );
		this.numIterSpinner.setPreferredSize( dSpinner );
		this.burnInSpinner.setMaximumSize( dSpinner );
		this.numIterSpinner.setMaximumSize( dSpinner );
		this.burnInLabel = new JLabel( "Burn In Period" );	//min.Iter
		this.numIterLabel = new JLabel( "# Iterations" );	//B
		this.burnInLabel.setFont( font11 );
		this.numIterLabel.setFont( font11 );
		this.allOutBox = new JCheckBox();
		this.allOutLabel = new JLabel( "log credible intervals" );
		this.allOutLabel.setFont( font11 );
		this.allOutBox.setSelected( true );
		
		//create connection pull down
		String[] connStrings = this.getConnString();
		this.connCombo = new JComboBox( connStrings );
		this.connCombo.setPreferredSize( dCombo );
		
		this.newConn = new JTextField( RamaInitDialog.DEFAULT_ADD_TEXT );
		this.newConn.setPreferredSize( dAddConn );
		this.newConn.setFont( font11 );
		this.addConnButton = new JButton( "Add" );
		this.addConnButton.addActionListener( al );
		this.addConnButton.setPreferredSize( dButton );
		this.addConnButton.setFont( font11 );
		JPanel addPanel = new JPanel();
		addPanel.add( this.newConn );
		addPanel.add( this.addConnButton );
		
		//add to its own panel
		JPanel comboPanel = new JPanel();
		BoxLayout box = new BoxLayout( comboPanel, BoxLayout.Y_AXIS ) ;
		comboPanel.setLayout( box );
		comboPanel.add( Box.createRigidArea( dSpace ) );
		comboPanel.add( this.connCombo );
		comboPanel.add( Box.createRigidArea( dSpace ) );
		comboPanel.add( addPanel );
		Border greyLine = BorderFactory.createLineBorder( Color.LIGHT_GRAY, 1 );
		TitledBorder border = BorderFactory.createTitledBorder( greyLine, 
				"Rserve Connection", TitledBorder.TOP, TitledBorder.CENTER, font11 );
		//
		comboPanel.setBorder( border );
		
		//disable as default
		this.burnInSpinner.setEnabled( false );
		this.numIterSpinner.setEnabled( false );
		this.allOutBox.setEnabled( false );
		this.burnInLabel.setForeground( Color.GRAY );
		this.numIterLabel.setForeground( Color.GRAY );
		this.allOutLabel.setForeground( Color.GRAY );
		
		JPanel paramPanel = new JPanel();
		SpringLayout sl = new SpringLayout();
		paramPanel.setLayout( sl );
		paramPanel.add( this.burnInLabel );
		paramPanel.add( this.burnInSpinner );
		paramPanel.add( this.numIterLabel );
		paramPanel.add( this.numIterSpinner );
		//paramPanel.add( this.allOutLabel );
		//paramPanel.add( this.allOutBox );
		SpringUtilities.makeCompactGrid( paramPanel, 2, 2, 5, 5, 5, 5 );
		
		JPanel leftPanel = new JPanel();
		leftPanel.setLayout( new BorderLayout() );
		leftPanel.add( checkPanel, BorderLayout.NORTH );
		leftPanel.add( paramPanel, BorderLayout.CENTER );
		leftPanel.setBorder( BorderFactory.createLineBorder( Color.LIGHT_GRAY, 1 ) );
		
		JPanel labelPanel = new JPanel();
		Color color = labelPanel.getBackground();
		JTextArea area = new JTextArea();
		area.setBackground( color );
		area.setForeground( Color.BLUE );
		area.setEditable( false );
		area.setPreferredSize( dArea );
		area.setFont( font );
		area.setText( "Now would be a good\ntime to start Rserve\n\nClick the info button\nfor help (lower left)" );
		labelPanel.add( area );
		
		JPanel returnPanel = new JPanel();
		returnPanel.add( leftPanel );
		returnPanel.add( labelPanel );
		returnPanel.add( comboPanel );
		
		return returnPanel;
	}//createParamPanel()
	
	
	//need to list the hybs and let user label exp vs control
	private JPanel createTwoColorPanel( String[] hybNames, int dataTypeP ) {
		//JPanel for listing the hybs and the radio buttons and check boxes
		JPanel listPanel = new JPanel();
		BoxLayout hybBox = new BoxLayout( listPanel, BoxLayout.Y_AXIS );
		listPanel.setLayout( hybBox );
		
		Dimension dLabel = new Dimension( 330, 15 );
		Dimension dCheck = new Dimension( 50, 15 );
		Dimension dRow = new Dimension( 450, 25 );
		Font font = new Font( "Arial", Font.PLAIN, 11 );
		
		//JLabel spacer
		JLabel spacer = new JLabel( "" );
		spacer.setMinimumSize( dCheck );
		spacer.setMaximumSize( dCheck );
		spacer.setPreferredSize( dCheck );
		
		//Column headers
		JLabel hybColLabel;
		JLabel cy3Label;
		JLabel cy5Label;
		if( dataTypeP == IData.DATA_TYPE_AFFY_ABS ) {
			hybColLabel = new JLabel( "For each chip, denote whether it is Treated or Control" );
			cy3Label = new JLabel( "Treated" );
			cy5Label = new JLabel( "Control" );
		} else {
			hybColLabel = new JLabel( "For each slide, mark Control Sample's dye color" );
			cy3Label = new JLabel( "Cy3" );
			cy5Label = new JLabel( "Cy5" );
		}
		hybColLabel.setFont( font );
		hybColLabel.setForeground( Color.gray );
		hybColLabel.setMinimumSize( dLabel );
		hybColLabel.setMaximumSize( dLabel );
		hybColLabel.setPreferredSize( dLabel );
		cy3Label.setFont( font );
		cy3Label.setForeground( Color.gray );
		cy3Label.setMinimumSize( dCheck );
		cy3Label.setMaximumSize( dCheck );
		cy3Label.setPreferredSize( dCheck );
		cy5Label.setFont( font );
		cy5Label.setForeground( Color.gray );
		cy5Label.setMinimumSize( dCheck );
		cy5Label.setMaximumSize( dCheck );
		cy5Label.setPreferredSize( dCheck );
		
		JPanel titlePanel = new JPanel();
		titlePanel.add( hybColLabel );
		titlePanel.add( cy3Label );
		titlePanel.add( cy5Label );
		listPanel.add( titlePanel );
		
		int iHyb = hybNames.length;
		int iHalf = iHyb / 2;
		
		for( int h = 0; h < iHyb; h ++ ) {
			//JLabel spacer
			JLabel spaceLabel = new JLabel( "" );
			spaceLabel.setMinimumSize( dCheck );
			spaceLabel.setMaximumSize( dCheck );
			spaceLabel.setPreferredSize( dCheck );
			
			//display the hyb name and radio buttons so user can label them
			JLabel hybLabel = new JLabel( hybNames[ h ] );
			hybLabel.setMinimumSize( dLabel );
			hybLabel.setMaximumSize( dLabel );
			hybLabel.setPreferredSize( dLabel );
			
			JRadioButton cy3Button = new JRadioButton();
			JRadioButton cy5Button = new JRadioButton();
			cy3Button.setMinimumSize( dCheck );
			cy3Button.setMaximumSize( dCheck );
			cy3Button.setPreferredSize( dCheck );
			cy5Button.setMinimumSize( dCheck );
			cy5Button.setMaximumSize( dCheck );
			cy5Button.setPreferredSize( dCheck );
			
			//add them to a group so they are mutually exclusive
			ButtonGroup sampleGroup = new ButtonGroup();
			sampleGroup.add( cy3Button );
			sampleGroup.add( cy5Button );
			//
			//by default, split the hybs in half for ease
			if( h < iHalf ) {
				cy3Button.setSelected( false );
				cy5Button.setSelected( true );
			} else {
				cy3Button.setSelected( true );
				cy5Button.setSelected( false );
			}
			
			JPanel rowPanel = new JPanel();
			rowPanel.add( hybLabel );
			rowPanel.add( cy3Button );
			rowPanel.add( cy5Button );
			rowPanel.setMaximumSize( dRow );
			rowPanel.setMinimumSize( dRow );
			rowPanel.setPreferredSize( dRow );
			
			//color every other row
			if( h % 2 == 0 ) {
				rowPanel.setBackground( Color.LIGHT_GRAY );
				cy3Button.setBackground( Color.LIGHT_GRAY );
				cy5Button.setBackground( Color.LIGHT_GRAY );
			}
			
			RHyb hyb;
			
			if( dataTypeP == IData.DATA_TYPE_AFFY_ABS ) {
				hyb = new RHyb( h, hybNames[ h ], cy3Button, 
						IData.DATA_TYPE_AFFY_ABS );
			} else {
				hyb = new RHyb( h, hybNames[ h ], cy3Button, 
						IData.DATA_TYPE_TWO_INTENSITY );
			}
			this.ramaHybs[ h ] = hyb;
			this.vRamaHyb.add( hyb );
			listPanel.add( rowPanel );
		}

		listPanel.add( Box.createVerticalGlue() );
		listPanel.setBorder( BorderFactory.createLineBorder( Color.LIGHT_GRAY, 1 ) );
		return listPanel;
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
	 * 
	 * @param message
	 */
	public void error( String message ) {
		JOptionPane.showMessageDialog( this, message, "Input Error", JOptionPane.ERROR_MESSAGE );
	}//end error()
	
	
	/**
	 * 
	 * @author iVu
	 */
	private class AdvListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			Object source = e.getSource();
			
			if( source == advCheckBox ) {
				onAdvancedClicked();
			}
			if( source == addConnButton ) {
				String s = ( String ) newConn.getText().trim();
				if( s.equals( RamaInitDialog.DEFAULT_ADD_TEXT ) ) {
					//do nothing
				} else if( s.equals( "" ) ) {
					//do nothing
				} else {
					int itemKount = connCombo.getItemCount();
					boolean alreadyThere = false;
					for( int i = 0; i < itemKount; i ++ ) {
						String item = ( String ) connCombo.getItemAt( i );
						if( s.equals( item ) ) {
							alreadyThere = true;
							break;
						}
					}
					if( ! alreadyThere ) {
						connAdded = true;
						connCombo.insertItemAt( s, 0 );
						connCombo.setSelectedIndex( 0 );
					}
				}
			}
		}//end actionPerformed()
	}//end class
	
	
	/**
	 * Enable or Disable the advanced parameters
	 *
	 */
	private void onAdvancedClicked() {
		if( this.advCheckBox.isSelected() ) {
			this.burnInSpinner.setEnabled( true );
			this.numIterSpinner.setEnabled( true );
			this.allOutBox.setEnabled( true );
			this.burnInLabel.setForeground( Color.BLACK );
			this.numIterLabel.setForeground( Color.BLACK );
			this.allOutLabel.setForeground( Color.BLACK );
		} else {
			this.burnInSpinner.setEnabled( false );
			this.numIterSpinner.setEnabled( false );
			this.allOutBox.setEnabled( false );
			this.burnInLabel.setForeground( Color.LIGHT_GRAY );
			this.numIterLabel.setForeground( Color.LIGHT_GRAY );
			this.allOutLabel.setForeground( Color.LIGHT_GRAY );
		}
	}//onAdvancedClicked()
	
	
	
	private boolean verifyHybs() {
		RHybSet rhs = this.getRamaHybSet();
		
		if( this.dataType == IData.DATA_TYPE_TWO_INTENSITY ) {
			if( rhs.isFlip() ) {
				//make sure there are at least 2 of each
				return this.atLeastTwo( rhs );
			} else {
				return true;
			}
		} else {	//Affy data
			return this.isBalanced( rhs );
		}
	}//verifyHybs()
	
	/**
	 * Can't average less than 2
	 * @param rhs
	 * @return
	 */
	private boolean atLeastTwo( RHybSet rhs ) {
		Vector vRamaHyb = rhs.getVRamaHyb();
		
		int iOne = 0;
		int iTwo = 0;
		
		for( int r = 0; r < vRamaHyb.size(); r ++ ) {
			RHyb hyb = ( RHyb ) vRamaHyb.elementAt( r );
			if( hyb.controlCy3() ) {
				iOne ++;
			} else {
				iTwo ++;
			}
		}//r
		
		if( iOne > 1 && iTwo > 1 ) {
			//good
			return true;
		} else {
			//bad
			this.error( "For dye-swap experiments, there must be at least 2 hybs of each color state" );
			return false;
		}
	}
	
	/**
	 * Tests to make sure there is a Treated for every Control and vice versa
	 * @param rhs
	 * @return
	 */
	private boolean isBalanced( RHybSet rhs ) {
		Vector vRamaHyb = rhs.getVRamaHyb();
		
		int iTreated = 0;
		int iControl = 0;
		
		for( int r = 0; r < vRamaHyb.size(); r ++ ) {
			RHyb hyb = ( RHyb ) vRamaHyb.elementAt( r );
			if( hyb.oneIsTreated() ) {
				iTreated ++;
			} else {
				iControl ++;
			}
		}//r
		
		if( iTreated == iControl ) {
			return true;
		} else {
			this.error( "For affy data, you must have the same # of Treated and Control Chips" );
			return false;
		}
	}
	
	
	/**
	 * Splits semicolon separated strings into String[]
	 * @param rPath
	 * @return
	 */
	private String[] getConnString() {
		//try to get the connection string from the pref file default is ("localhost:6311")
		String rPath = TMEV.getRPath();
		
		String[] toReturn;
		
		StringTokenizer st = new StringTokenizer( rPath, ";" );
		int tokenKount = st.countTokens();
		toReturn = new String[ tokenKount ];
		for( int i = 0; i < tokenKount; i ++ ) {
			String token = st.nextToken();
			toReturn[ i ] = token;
		}//i
		
		return toReturn;
	}//getConnString
	
    
	/**
	 * The class to listen to the dialog and check boxes items events.
	 */
	private class Listener extends DialogListener implements ItemListener {
        
		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			if (command.equals("ok-command")) {
				if( verifyHybs() ) {
					result = JOptionPane.OK_OPTION;
					dispose();
				}
			} else if (command.equals("cancel-command")) {
				result = JOptionPane.CANCEL_OPTION;
				dispose();
			} else if (command.equals("reset-command")) {
				//resetControls();
				result = JOptionPane.CANCEL_OPTION;
				return;
			} else if (command.equals("info-command")) {
				HelpWindow hw = new HelpWindow(RamaInitDialog.this, "RAMA Initialization Dialog");
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
	
	
	public boolean getAllOut() {
		return this.allOutBox.isSelected();
	}
	public int getBurnIn() {
		SpinnerNumberModel model = ( SpinnerNumberModel ) this.burnInSpinner.getModel();
		return model.getNumber().intValue();
	}
	public int getNumIter() {
		SpinnerNumberModel model = ( SpinnerNumberModel ) this.numIterSpinner.getModel();
		return model.getNumber().intValue();
	}
	public RHybSet getRamaHybSet() {
		return new RHybSet( this.vRamaHyb );
	}
	public String getSelectedConnString() {
		return ( String ) this.connCombo.getSelectedItem();
	}
	public String getRPathToWrite() {
		StringBuffer sb = new StringBuffer();
		for( int i = 0; i < this.connCombo.getItemCount(); i ++ ) {
			String s = ( String ) this.connCombo.getItemAt( i );
			if( i > 0 ) {
				sb.append( ";" );
			}
			sb.append( s );
		}
		return sb.toString();
	}
	public boolean connAdded() {
		return this.connAdded;
	}
	
	
	//	test harness
	public static void main(String [] args) {
		System.out.println( "RamaInitDialog invoked by main" );
		
		String[] names = new String[ 8 ];
		names[ 0 ] = "one";
		names[ 1 ] = "two";
		names[ 2 ] = "three";
		names[ 3 ] = "four";
		names[ 4 ] = "five";
		names[ 5 ] = "six";
		names[ 6 ] = "seven";
		names[ 7 ] = "eight";
		
		RamaInitDialog uid = new RamaInitDialog( new Frame(), names, IData.DATA_TYPE_AFFY_ABS );
		//RamaInitDialog uid = new RamaInitDialog( new Frame(), names, IData.DATA_TYPE_TWO_INTENSITY );
		uid.showModal();
	}//end main
}//end class


/*
*/
/*
	public Vector getVRamaHyb() {
		return this.vRamaHyb;
	}
	public Vector getVOne() {
		return this.vOne;
	}
	public Vector getVTwo() {
		return this.vTwo;
	}
	public Vector getVOneFlip() {
		return this.vOneFlip;
	}
	public Vector getVTwoFlip() {
		return this.vTwoFlip;
	}
	/**
	 * Creates a Vector of RamaHybs where: treated-Cy3 | control-Cy5
	 * @return
	 
	public Vector getVRamaHybTreatCy3() {
		Vector vReturn = new Vector();
		
		for( int h = 0; h < this.ramaHybs.length; h ++ ) {
			if( ! this.ramaHybs[ h ].controlCy3() ) {
				vReturn.add( this.ramaHybs[ h ] );
			}
		}
		
		return vReturn;
	}
	/**
	 * Creates a Vector of RamaHybs where: control-Cy3 | treated-Cy5
	 * @return
	 
	public Vector getVRamaHybTreatCy5() {
		Vector vReturn = new Vector();
		
		for( int h = 0; h < this.ramaHybs.length; h ++ ) {
			if( this.ramaHybs[ h ].controlCy3() ) {
				vReturn.add( this.ramaHybs[ h ] );
			}
		}
		
		return vReturn;
	}
	
	
	private JPanel createListPanel( String[] hybNames ) {
		Dimension dLabel = new Dimension( 180, 15 );
		Dimension dCheck = new Dimension( 20, 15 );
		Dimension dForty = new Dimension( 40, 15 );
		Font font = new Font( "Arial", Font.PLAIN, 11 );
		
		//JLabel spacer
		JLabel spacer = new JLabel( "" );
		spacer.setMinimumSize( dCheck );
		spacer.setMaximumSize( dCheck );
		spacer.setPreferredSize( dCheck );
		
		//JPanel for listing the hybs and the radio buttons and check boxes
		JPanel listPanel = new JPanel();
		BoxLayout hybBox = new BoxLayout( listPanel, BoxLayout.Y_AXIS );
		listPanel.setLayout( hybBox );
		
		//column labels
		JLabel hybColLabel = new JLabel( "Slide Name" );
		hybColLabel.setFont( font );
		hybColLabel.setForeground( Color.gray );
		hybColLabel.setMinimumSize( dLabel );
		hybColLabel.setMaximumSize( dLabel );
		hybColLabel.setPreferredSize( dLabel );
		JLabel expLabel = new JLabel( "Exp" );
		expLabel.setFont( font );
		expLabel.setForeground( Color.gray );
		expLabel.setMinimumSize( dCheck );
		expLabel.setMaximumSize( dCheck );
		expLabel.setPreferredSize( dCheck );
		JLabel conLabel = new JLabel( "Con" );
		conLabel.setFont( font );
		conLabel.setForeground( Color.gray );
		conLabel.setMinimumSize( dCheck );
		conLabel.setMaximumSize( dCheck );
		conLabel.setPreferredSize( dCheck );
		JLabel flipLabel = new JLabel( "Flip" );
		flipLabel.setFont( font );
		flipLabel.setForeground( Color.gray );
		flipLabel.setMinimumSize( dForty );
		flipLabel.setMaximumSize( dForty );
		flipLabel.setPreferredSize( dForty );
		JPanel titlePanel = new JPanel();
		titlePanel.add( hybColLabel );
		titlePanel.add( expLabel );
		titlePanel.add( conLabel );
		titlePanel.add( spacer );
		titlePanel.add( flipLabel );
		listPanel.add( titlePanel );
		
		//display a list of the hybs and ask
		int iHybs = hybNames.length;
		int iHalf = iHybs / 2;
		
		//loop through the hybs
		for( int h = 0; h < iHybs; h ++ ) {
			//JLabel spacer
			JLabel spaceLabel = new JLabel( "" );
			spaceLabel.setMinimumSize( dCheck );
			spaceLabel.setMaximumSize( dCheck );
			spaceLabel.setPreferredSize( dCheck );
			
			//display the hyb name and radio buttons so user can label them
			JLabel hybLabel = new JLabel( hybNames[ h ] );
			hybLabel.setMinimumSize( dLabel );
			hybLabel.setMaximumSize( dLabel );
			hybLabel.setPreferredSize( dLabel );
			
			JRadioButton oneButton = new JRadioButton();
			JRadioButton twoButton = new JRadioButton();
			oneButton.setMinimumSize( dCheck );
			oneButton.setMaximumSize( dCheck );
			oneButton.setPreferredSize( dCheck );
			twoButton.setMinimumSize( dCheck );
			twoButton.setMaximumSize( dCheck );
			twoButton.setPreferredSize( dCheck );
			this.vCy3Radio.add( twoButton );
			//this.vExpRadio.add( expButton );
			
			//add them to a group so they are mutually exclusive
			ButtonGroup sampleGroup = new ButtonGroup();
			sampleGroup.add( oneButton );
			sampleGroup.add( twoButton );
			
			JCheckBox flipBox = new JCheckBox();
			flipBox.setMinimumSize( dForty );
			flipBox.setMaximumSize( dForty );
			flipBox.setPreferredSize( dForty );
			this.vFlipCheck.add( flipBox );
			
			//by default, split the hybs in half for ease
			if( h < iHalf ) {
				oneButton.setSelected( false );
				twoButton.setSelected( true );
			} else {
				oneButton.setSelected( true );
				twoButton.setSelected( false );
			}
			
			//also check half the hybs as flipped
			if( h >= iHalf/2 && h < iHalf ) {
				flipBox.setSelected( true );
			} else if( h >= ( iHalf + iHalf / 2 ) ) {
				flipBox.setSelected( true );
			}
			
			JPanel rowPanel = new JPanel();
			rowPanel.add( hybLabel );
			rowPanel.add( oneButton );
			rowPanel.add( twoButton );
			rowPanel.add( spaceLabel );
			rowPanel.add( flipBox );
			Dimension dRow = new Dimension( 350, 25 );
			rowPanel.setMaximumSize( dRow );
			rowPanel.setMinimumSize( dRow );
			rowPanel.setPreferredSize( dRow );
			
			//color every other row
			if( h % 2 == 0 ) {
				rowPanel.setBackground( Color.LIGHT_GRAY );
				oneButton.setBackground( Color.LIGHT_GRAY );
				twoButton.setBackground( Color.LIGHT_GRAY );
				flipBox.setBackground( Color.LIGHT_GRAY );
			}
			
			RamaHyb hyb = new RamaHyb( h, hybNames[ h ], oneButton, flipBox );
			this.vRamaHyb.add( hyb );
			listPanel.add( rowPanel );
		}//h

		//listPanel.add( Box.createRigidArea( new Dimension( 100, 200 ) ) );
		listPanel.add( Box.createVerticalGlue() );
		listPanel.setBorder( BorderFactory.createLineBorder( Color.LIGHT_GRAY, 1 ) );
		return listPanel;
	}//initializeGUI()
*/

/*
private boolean verifyHybs() {
	this.vOne = new Vector();
	this.vOneFlip = new Vector();
	this.vTwo = new Vector();
	this.vTwoFlip = new Vector();
	
	//loop through the RamaHybs
	for( int h = 0; h < vRamaHyb.size(); h ++ ) {
		RamaHyb hyb = ( RamaHyb ) vRamaHyb.elementAt( h );
		
		//place it in the appropriate array
		if( hyb.controlCy3() ) {
			if( hyb.isFlip() ) {
				this.vOneFlip.add( hyb );
			} else {
				this.vOne.add( hyb );
			}
		} else {
			if( hyb.isFlip() ) {
				this.vTwoFlip.add( hyb );
			} else {
				this.vTwo.add( hyb );
			}
		}
	}//h
	
	//make sure there is balance
	if( vOne.size() != vTwo.size() ) {
		//problems
		this.error( "Rama will only work if there are the hybs are balanced"
				+ " for treatments and color flips" );
		return false;
	}
	
	return true;
}//verifyHybs()
*/
