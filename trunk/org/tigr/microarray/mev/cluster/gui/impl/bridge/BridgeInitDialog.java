/*
 * Created on Sep 1, 2005
 */
package org.tigr.microarray.mev.cluster.gui.impl.bridge;

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
import org.tigr.microarray.mev.r.RHyb;
import org.tigr.microarray.mev.r.RHybSet;
import org.tigr.microarray.mev.r.SpringUtilities;

/**
 * @author iVu
 */
public class BridgeInitDialog extends AlgorithmDialog {
	private int result;
	private int dataType;
	private boolean connAdded = false;
	
	private AdvListener al;
	
	//private BridgeHyb[] bridgeHybs;
	private RHyb[] rHybs;
	
	//private Vector vCy3Radio;
	private Vector vRHyb;
	
	private JCheckBox advCheckBox;
	private JSpinner burnInSpinner;
	private JSpinner numIterSpinner;
	private JSpinner thresholdSpinner;
	private JLabel burnInLabel;
	private JLabel numIterLabel;
	private JLabel thresholdLabel;
	private JButton addConnButton;
	private JComboBox connCombo;
	private JTextField newConn;
	
	private JRadioButton ratioButton;
	private JRadioButton affyButton;
	
	private JLabel hybColLabel;
	private JLabel cy3Label;
	private JLabel cy5Label;
	
	private String yNum = "IntB";
	private String yDen = "IntA";
	
	public static String DEFAULT_ADD_TEXT = "Enter a new location";
	
	
	
	
	/**
	 * @param parent
	 * @param title
	 * @param modal
	 */
	public BridgeInitDialog( Frame parent, String[] hybNames, int dataTypeP ) {
		super( parent, "Bridge Initialization Dialog", true );
		this.setSize( 600, 600 );
		this.dataType = dataTypeP;
		
		//create the listener for this dialog and add it
		Listener listener = new Listener();
		super.addWindowListener(listener);
		super.setActionListeners(listener);
		
		this.al = new AdvListener();
		
		//initialize variables
		this.vRHyb = new Vector();
		this.rHybs = new RHyb[ hybNames.length ];
		
		//create the GUI
		JPanel mainPanel = new JPanel();
		if( dataTypeP == IData.DATA_TYPE_TWO_INTENSITY ) {
			System.out.println( "dataType = TWO_INTENSITY" );
			mainPanel.add( this.createDataTypePanel(), BorderLayout.NORTH );
		} else {
			System.out.println( "dataType = Unknown" );
		}
		mainPanel.add( this.createHybPanel( hybNames, this.dataType ), BorderLayout.CENTER );
		mainPanel.add( this.createParamPanel(), BorderLayout.SOUTH );
		this.addContent( mainPanel );
	}//constructor
	
	
	private JPanel createDataTypePanel() {
		Font font11 = new Font( "Arial", Font.PLAIN, 11 );
		Font font = new Font( "Arial", Font.PLAIN, 12 );
		
		this.ratioButton = new JRadioButton( "2 Color Data" );
		this.affyButton = new JRadioButton( "Intensity Data" );
		this.ratioButton.setSelected( true );
		this.ratioButton.addActionListener( this.al );
		this.affyButton.addActionListener( this.al );
		
		ButtonGroup dataTypeGroup = new ButtonGroup();
		dataTypeGroup.add( this.ratioButton );
		dataTypeGroup.add( this.affyButton );
		
		JPanel returnPanel = new JPanel();
		returnPanel.setLayout( new BoxLayout( returnPanel, BoxLayout.X_AXIS ) );
		returnPanel.add( Box.createRigidArea( new Dimension( 75, 50 ) ) );
		returnPanel.add( this.ratioButton );
		returnPanel.add( Box.createRigidArea( new Dimension( 75, 50 ) ) );
		returnPanel.add( this.affyButton );
		returnPanel.add( Box.createRigidArea( new Dimension( 75, 50 ) ) );
		returnPanel.setBorder( BorderFactory.createTitledBorder( "Data Type" ) );
		
		return returnPanel;
	}//createDataTypePanel()
	
	
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
		//AdvListener al = new AdvListener();
		this.advCheckBox.addActionListener( this.al );
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
		
		SpinnerNumberModel threshModel = new SpinnerNumberModel( .5d, 0, 1000, .1d );
		this.thresholdSpinner = new JSpinner( threshModel );
		this.thresholdSpinner.setPreferredSize( dSpinner );
		this.thresholdSpinner.setMaximumSize( dSpinner );
		this.thresholdSpinner.setMinimumSize( dSpinner );
		this.thresholdLabel = new JLabel( "Post.P Threshold" );
		this.thresholdLabel.setFont( font11);
		
		//create connection pull down
		String[] connStrings = this.getConnString( TMEV.getRPath() );
		//String[] connStrings = { "a", "b", "c" };
		this.connCombo = new JComboBox( connStrings );
		this.connCombo.setPreferredSize( dCombo );
		
		this.newConn = new JTextField( BridgeInitDialog.DEFAULT_ADD_TEXT );
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
		comboPanel.add( addPanel );
		comboPanel.add( Box.createRigidArea( dSpace ) );
		comboPanel.add( this.connCombo );
		Border greyLine = BorderFactory.createLineBorder( Color.LIGHT_GRAY, 1 );
		TitledBorder border = BorderFactory.createTitledBorder( greyLine, 
				"Rserve Connection", TitledBorder.TOP, TitledBorder.CENTER, font11 );
		//
		comboPanel.setBorder( border );
		
		//disable as default
		this.burnInSpinner.setEnabled( false );
		this.numIterSpinner.setEnabled( false );
		this.thresholdSpinner.setEnabled( false );
		this.burnInLabel.setForeground( Color.GRAY );
		this.numIterLabel.setForeground( Color.GRAY );
		this.thresholdLabel.setForeground( Color.GRAY );
		
		JPanel paramPanel = new JPanel();
		SpringLayout sl = new SpringLayout();
		paramPanel.setLayout( sl );
		paramPanel.add( this.burnInLabel );
		paramPanel.add( this.burnInSpinner );
		paramPanel.add( this.numIterLabel );
		paramPanel.add( this.numIterSpinner );
		paramPanel.add( this.thresholdLabel );
		paramPanel.add( this.thresholdSpinner );
		SpringUtilities.makeCompactGrid( paramPanel, 3, 2, 5, 5, 5, 5 );
		
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
	private JPanel createHybPanel( String[] hybNames, int dataTypeP ) {
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
		
		if( dataTypeP == IData.DATA_TYPE_AFFY_ABS ) {
			this.hybColLabel = new JLabel( "For each chip, denote whether it is Treated or Control" );
			this.cy3Label = new JLabel( "Treated" );
			this.cy5Label = new JLabel( "Control" );
			this.yNum = "IntB";
			this.yDen = "IntA";
		} else {
			this.hybColLabel = new JLabel( "For each slide, mark the Control Sample's dye color" );
			this.cy3Label = new JLabel( "Cy3" );
			this.cy5Label = new JLabel( "Cy5" );
			this.yNum = "Cy5";
			this.yDen = "Cy3";
		}
		this.hybColLabel.setFont( font );
		this.hybColLabel.setForeground( Color.DARK_GRAY );
		this.hybColLabel.setMinimumSize( dLabel );
		this.hybColLabel.setMaximumSize( dLabel );
		this.hybColLabel.setPreferredSize( dLabel );
		this.cy3Label.setFont( font );
		this.cy3Label.setForeground( Color.DARK_GRAY );
		this.cy3Label.setMinimumSize( dCheck );
		this.cy3Label.setMaximumSize( dCheck );
		this.cy3Label.setPreferredSize( dCheck );
		this.cy5Label.setFont( font );
		this.cy5Label.setForeground( Color.DARK_GRAY );
		this.cy5Label.setMinimumSize( dCheck );
		this.cy5Label.setMaximumSize( dCheck );
		this.cy5Label.setPreferredSize( dCheck );
		
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
			this.rHybs[ h ] = hyb;
			this.vRHyb.add( hyb );
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
				if( s.equals( BridgeInitDialog.DEFAULT_ADD_TEXT ) ) {
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
			if( source == ratioButton ) {
				onDataTypeSelected();
			}
			if( source == affyButton ) {
				onDataTypeSelected();
			}
		}//end actionPerformed()
	}//end class
	
	
	private void onDataTypeSelected() {
		//user selected either ratio or intensity data, refresh display
		if( this.affyButton.isSelected() ) {
			//affy data
			hybColLabel.setText( "For each chip, denote whether it is Treated or Control" );
			cy3Label.setText( "Treated" );
			cy5Label.setText( "Control" );
			this.repaint();
			
			//set the y axis labels accordingly
			this.yNum = "IntB";
			this.yDen = "IntA";
		} else {
			//ratio data
			hybColLabel.setText( "For each slide, mark the Control Sample's dye color" );
			cy3Label.setText( "Cy3" );
			cy5Label.setText( "Cy5" );
			this.repaint();
			
			this.yNum = "Cy5";
			this.yDen = "Cy3";
		}
		/*
		if( dataTypeP == IData.DATA_TYPE_AFFY_ABS ) {
			this.hybColLabel = new JLabel( "For each chip, denote whether it is Treated or Control" );
			this.cy3Label = new JLabel( "Treated" );
			this.cy5Label = new JLabel( "Control" );
		} else {
			this.hybColLabel = new JLabel( "For each slide, mark Control Sample's dye color" );
			this.cy3Label = new JLabel( "Cy3" );
			this.cy5Label = new JLabel( "Cy5" );
		}*/
	}//onDataTypeSelected()
	
	
	/**
	 * Enable or Disable the advanced parameters
	 *
	 */
	private void onAdvancedClicked() {
		if( this.advCheckBox.isSelected() ) {
			this.burnInSpinner.setEnabled( true );
			this.numIterSpinner.setEnabled( true );
			this.thresholdSpinner.setEnabled( true );
			this.burnInLabel.setForeground( Color.BLACK );
			this.numIterLabel.setForeground( Color.BLACK );
			this.thresholdLabel.setForeground( Color.BLACK );
		} else {
			this.burnInSpinner.setEnabled( false );
			this.numIterSpinner.setEnabled( false );
			this.thresholdSpinner.setEnabled( false );
			this.burnInLabel.setForeground( Color.LIGHT_GRAY );
			this.numIterLabel.setForeground( Color.LIGHT_GRAY );
			this.thresholdLabel.setForeground( Color.LIGHT_GRAY );
		}
	}//onAdvancedClicked()
	
	
	/**
	 * Splits semicolon separated strings into String[]
	 * @param rPath
	 * @return
	 */
	private String[] getConnString( String rPath ) {
		String[] toReturn;
		
		StringTokenizer st = new StringTokenizer( rPath, ";" );
		int tokenKount = st.countTokens();
		toReturn = new String[ tokenKount ];
		for( int i = 0; i < tokenKount; i ++ ) {
			String token = st.nextToken();
			toReturn[ i ] = token;
		}
		
		return toReturn;
	}//getConnString
	
    
	/**
	 * The class to listen to the dialog and check boxes items events.
	 */
	private class Listener extends DialogListener implements ItemListener {
        
		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			if (command.equals("ok-command")) {
				//if( verifyHybs() ) {
					result = JOptionPane.OK_OPTION;
					dispose();
				//}
			} else if (command.equals("cancel-command")) {
				result = JOptionPane.CANCEL_OPTION;
				dispose();
			} else if (command.equals("reset-command")) {
				//resetControls();
				result = JOptionPane.CANCEL_OPTION;
				return;
			} else if (command.equals("info-command")) {
				HelpWindow hw = new HelpWindow(BridgeInitDialog.this, "Bridge Initialization Dialog");
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
	
	
	public int getBurnIn() {
		SpinnerNumberModel model = ( SpinnerNumberModel ) this.burnInSpinner.getModel();
		return model.getNumber().intValue();
	}
	public int getNumIter() {
		SpinnerNumberModel model = ( SpinnerNumberModel ) this.numIterSpinner.getModel();
		return model.getNumber().intValue();
	}
	public double getThreshold() {
		SpinnerNumberModel model = ( SpinnerNumberModel ) this.thresholdSpinner.getModel();
		return model.getNumber().doubleValue();
	}
	public RHybSet getBridgeHybSet() {
		return new RHybSet( this.vRHyb );
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
	public String getYNum() {
		return this.yNum;
	}
	public String getYDen() {
		return this.yDen;
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
		
		//BridgeInitDialog uid = new BridgeInitDialog( new Frame(), names, IData.DATA_TYPE_AFFY_ABS );
		BridgeInitDialog uid = new BridgeInitDialog( new Frame(), names, IData.DATA_TYPE_TWO_INTENSITY );
		uid.showModal();
	}//end main
	
	
	//
}//end class