package org.tigr.microarray.mev.cluster.gui.impl.usc;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SpringLayout;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.tigr.microarray.mev.r.ClassAssigner;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.HelpWindow;

/*
 * Created on Nov 22, 2004
 */

/**
 * This is the 1st Dialog Box that should be displayed.  It allows a user to decide to 
 * either train the algorithm using a set of training data or to load the training results 
 * from a file.
 * @author vu
 */
public class USCClassDialog extends AlgorithmDialog {
	//Static ints
	static int TRAIN_THEN_CLASSIFY = 0;
	static int CLASSIFY_FROM_FILE = 1;
	
	private int result;
	
	private JPanel mainPanel;						//
	private JPanel radioPanel;						//Radio buttons
	private JPanel labelPanel;						//
	private JPanel fieldPanel;							//JTextFields
	private JPanel textPanel;							//JLabels for JTextFields
	private JPanel paramPanel;						//Advanced Parameters
	
	private JRadioButton trainClassify;		//train then classify
	private JRadioButton fileClassify;			//use a file to classify
	
	private JSpinner numClasses;				//
	private Vector vField;								//Vector of JTextFields for Class Labels
	private Vector vLabel;								//Vector of JLabels of Class Labels
	
	private JCheckBox advActive;				//
	//private JButton advDeactive;					//
	
	private JSpinner numFolds;						//# Cross Validation fold
	private JSpinner numBins;						//Number of deltas
	private JSpinner deltaMax;						//Maximum delta
	private JSpinner numXVal;						//# Cross Validation runs
	private JTextField corrLo;							//
	private JTextField corrHi;							//
	private JTextField corrStep;					//
	
	private Dimension dField;						//
	private Dimension dLabel;						//

	private JLabel numLabel;							//
	private JLabel foldLabel;							//
	private JLabel xValLabel;							//
	private JLabel binLabel;							//
	private JLabel deltaLabel;						//
	private JLabel loLabel;								//
	private JLabel hiLabel;								//
	private JLabel stepLabel;							//
	
	
	
	/**
	 * @param parent
	 */
	public USCClassDialog( Frame parent ) {
		super( ( JFrame ) parent, "USC: Uncorrelated Shrunken Centroid", true );
		this.setSize( 350, 525 );
		//this.setSize( 350, 415 );
		
		//
		this.dLabel = new Dimension( 55, 20 );
		this.dField = new Dimension( 140, 20 );
		Dimension dSpin = new Dimension( 45, 20 );
		Dimension dButton = new Dimension( 140, 20 );
		
		//the mainPanel is the big one that will be placed in the AlgorithmDialog
		this.mainPanel = new JPanel();
		
		//need Listeners for the JCheckBox and JSpinner and the Dialog itself
		SpinListener sl = new SpinListener();
		ButtonListener bl = new ButtonListener();
		Listener listener = new Listener();
		super.addWindowListener(listener);
		super.setActionListeners(listener);
		
		//create the sub panels
		this.createRadioPanel( bl, dSpin );
		this.createLabelPanel( sl, 2 );
		this.createParamPanel( bl, dSpin );
		
		//add the sub panels to mainPanel
		this.mainPanel.setLayout( new BorderLayout() );
		this.mainPanel.add( this.radioPanel, BorderLayout.WEST );
		this.mainPanel.add( this.paramPanel, BorderLayout.EAST );
		this.mainPanel.add( this.labelPanel, BorderLayout.NORTH );
		
		//add mainPanel to AlgorithmDialog
		JScrollPane scrollPane = new JScrollPane( this.mainPanel );
		this.addContent( scrollPane );
	}//end constructor
	
	
	public static void main( String[] args ) {
		System.out.println( "invoked by main()" );
		
		USCClassDialog d = new USCClassDialog( new JFrame() );
		d.showModal();
	}
	
	
	/**
	 * Creates the JPanel containing the JRadioButton group that allows the user to 
	 * choose whether they'd like to train data using a training set or using a trained 
	 * file to use to classify their data
	 * @param dSpin
	 */
	private void createRadioPanel( ButtonListener bl, Dimension dSpin ) {
		this.radioPanel = new JPanel();
		BoxLayout box = new BoxLayout( this.radioPanel, BoxLayout.Y_AXIS );
		this.radioPanel.setLayout( box );
		this.radioPanel.setBorder( BorderFactory.createTitledBorder("Analysis Mode"));
		
		//create radio buttons and group them
		this.trainClassify = new JRadioButton( "Train & Classify" );
		this.trainClassify.setSelected( true );
		this.trainClassify.addItemListener( bl );
		this.fileClassify = new JRadioButton( "Classify from File" );
		this.fileClassify.setSelected( false );
		this.fileClassify.addItemListener( bl );
		ButtonGroup group = new ButtonGroup();
		group.add( this.trainClassify );
		group.add( this.fileClassify );
		
		this.radioPanel.add(Box.createVerticalGlue());
		this.radioPanel.add( this.trainClassify );
		this.radioPanel.add( this.fileClassify );
		this.radioPanel.add(Box.createVerticalGlue());
	}//end createRadioPanel()
	
	
	/**
	 * Creates the JPanel containing the JSpinner for # of Classes and the corresponding 
	 * number of JTextFields and their JLabels for the user to enter the class labels
	 * @param dField	Dimension of the JTextFields
	 * @param dText		Dimension of the JLabels
	 * @param kount		Default number of classes
	 */
	private void createLabelPanel( SpinListener sl, int kount ) {
		this.vField = new Vector();
		this.vLabel = new Vector();
		
		this.labelPanel = new JPanel( new BorderLayout() );
		this.labelPanel.setBorder( BorderFactory.createTitledBorder( "Enter all Class Labels") );
		
		//JSpinner, the class labels and the labels for class labels should go on labelPanel
		this.fieldPanel = new JPanel();
		BoxLayout fieldBoxLayout = new BoxLayout( this.fieldPanel, BoxLayout.Y_AXIS );
		this.fieldPanel.setLayout( fieldBoxLayout );
		
		this.textPanel = new JPanel();
		BoxLayout textBoxLayout = new BoxLayout( this.textPanel, BoxLayout.Y_AXIS );
		this.textPanel.setLayout( textBoxLayout );
		
		JPanel centerPanel = new JPanel();
		centerPanel.add( this.textPanel );
		centerPanel.add( this.fieldPanel );
		
		for( int i = 0; i < kount; i ++ ) {
			JLabel label = new JLabel( "Label " + ( i + 1 ) );
			label.setMaximumSize( this.dLabel );
			label.setMinimumSize( this.dLabel );
			label.setPreferredSize( this.dLabel );
			
			JTextField field = new JTextField();
			field.setMaximumSize( this.dField );
			field.setMinimumSize( this.dField );
			field.setPreferredSize( this.dField );
			
			textPanel.add( label );
			fieldPanel.add( field );
			
			this.vLabel.add( label );
			this.vField.add( field );
		}
		
		//create a sub JPanel for JSpinner
		JPanel spinPanel = new JPanel();
		
		//create JSpinner for # of classes
		SpinnerNumberModel model = new SpinnerNumberModel( kount, 2, 50, 1 );
		this.numClasses = new JSpinner( model );
		this.numClasses.setMaximumSize( this.dLabel );
		this.numLabel = new JLabel( "# of Classes" );
		spinPanel.add( this.numLabel );
		spinPanel.add( this.numClasses );
		this.numClasses.addChangeListener( sl );
		
		this.labelPanel.add( spinPanel, BorderLayout.NORTH );
		this.labelPanel.add( centerPanel, BorderLayout.CENTER );
	}//end createLabelPanel()
	
	
	/**
	 * Creates the JPanel containing all the Advanced options.  By default, they will 
	 * be instantiated with default values and disabled.  If the user would like to change 
	 * any values, they will have to click on the advActive JCheckBox to enable the 
	 * parameters.
	 * @param cl	ItemListener
	 * @param d		Dimension of the JSpinners and JTextFields
	 */
	private void createParamPanel( ButtonListener cl, Dimension d ) {
		this.paramPanel = new JPanel( new SpringLayout() );
		this.paramPanel.setBorder( BorderFactory.createTitledBorder("Advanced Parameters"));
		//BoxLayout bl = new BoxLayout( this.paramPanel, BoxLayout.Y_AXIS );
		//this.paramPanel.setLayout( bl );
		
		this.advActive = new JCheckBox( "Advanced" );
		this.advActive.addItemListener( cl );
		JLabel advLabel = new JLabel( " " );
		
		SpinnerNumberModel numClassModel = ( SpinnerNumberModel ) this.numClasses.getModel();
		int numClasses = numClassModel.getNumber().intValue();
		int defaultFold = 3;
		int defaultXVal = 5;
		
		SpinnerNumberModel foldModel = new SpinnerNumberModel( defaultFold, 1, 100, 1 );
		this.numFolds = new JSpinner( foldModel );
		this.numFolds.setMaximumSize( d );
		this.numFolds.setMinimumSize( d );
		this.numFolds.setPreferredSize( d );
		this.foldLabel = new JLabel( "# Folds" );
		
		SpinnerNumberModel xValModel = new SpinnerNumberModel( defaultXVal, 1, 100, 1 );
		this.numXVal = new JSpinner( xValModel );
		this.numXVal.setMaximumSize( d );
		this.numXVal.setMinimumSize( d );
		this.numXVal.setPreferredSize( d );
		this.xValLabel = new JLabel( "# CV runs" );
		
		SpinnerNumberModel numBinsModel = new SpinnerNumberModel( 20, 1, 500, 1 );
		this.numBins = new JSpinner( numBinsModel );
		this.numBins.setMaximumSize( d );
		this.numBins.setMinimumSize( d );
		this.numBins.setPreferredSize( d );
		this.binLabel = new JLabel( "# Bins" );
		
		SpinnerNumberModel deltaMaxModel = new SpinnerNumberModel( 10, 1, 500, 1 );
		this.deltaMax = new JSpinner( deltaMaxModel );
		this.deltaMax.setMaximumSize( d );
		this.deltaMax.setMinimumSize( d );
		this.deltaMax.setPreferredSize( d );
		this.deltaLabel = new JLabel( "Max Delta" );
		
		this.corrLo = new JTextField( "0.5" );
		this.corrLo.setMaximumSize( d );
		this.corrLo.setMinimumSize( d );
		this.corrLo.setPreferredSize( d );
		this.loLabel = new JLabel( "Corr Low" );
		
		this.corrHi = new JTextField( "1.0" );
		this.corrHi.setMaximumSize( d );
		this.corrHi.setMinimumSize( d );
		this.corrHi.setPreferredSize( d );
		this.hiLabel = new JLabel( "Corr High" );
		
		this.corrStep = new JTextField( "0.1" );
		this.corrStep.setMaximumSize( d );
		this.corrStep.setMinimumSize( d );
		this.corrStep.setPreferredSize( d );
		this.stepLabel = new JLabel( "Corr Step" );
		
		this.advActive.setAlignmentX( Component.CENTER_ALIGNMENT );
		this.numFolds.setAlignmentX( Component.CENTER_ALIGNMENT );
		this.numXVal.setAlignmentX( Component.CENTER_ALIGNMENT );
		this.numBins.setAlignmentX( Component.CENTER_ALIGNMENT );
		this.deltaMax.setAlignmentX( Component.CENTER_ALIGNMENT );
		this.corrLo.setAlignmentX( Component.CENTER_ALIGNMENT );
		this.corrHi.setAlignmentX( Component.CENTER_ALIGNMENT );
		this.corrStep.setAlignmentX( Component.CENTER_ALIGNMENT );

		this.foldLabel.setForeground( Color.gray );
		this.binLabel.setForeground( Color.gray );
		this.xValLabel.setForeground( Color.gray );
		this.deltaLabel.setForeground( Color.gray );
		this.loLabel.setForeground( Color.gray );
		this.hiLabel.setForeground( Color.gray );
		this.stepLabel.setForeground( Color.gray );
		
		this.paramPanel.add( advLabel );
		this.paramPanel.add( this.advActive );
		this.paramPanel.add( this.foldLabel );
		this.paramPanel.add( this.numFolds );
		this.paramPanel.add( this.xValLabel );
		this.paramPanel.add( this.numXVal );
		this.paramPanel.add( this.binLabel );
		this.paramPanel.add( this.numBins );
		this.paramPanel.add( this.deltaLabel );
		this.paramPanel.add( this.deltaMax );
		this.paramPanel.add( this.loLabel );
		this.paramPanel.add( this.corrLo );
		this.paramPanel.add( this.hiLabel );
		this.paramPanel.add( this.corrHi );
		this.paramPanel.add( this.stepLabel );
		this.paramPanel.add( this.corrStep );
		
		SpringUtilities.makeCompactGrid( this.paramPanel, 8, 2, 0, 0, 10, 0 );
		
		this.setParamEnabled( false );
	}//end createParamPanel()
	
	
	/**
	 * The user clicked on the advActive JCheckBox.  Enable or Disable as necessary
	 * @param isEnabled
	 */
	private void setParamEnabled( boolean isEnabled ) {
		if( isEnabled ) {
			this.foldLabel.setForeground( Color.BLACK );
			this.xValLabel.setForeground( Color.BLACK );
			this.binLabel.setForeground( Color.BLACK );
			this.deltaLabel.setForeground( Color.BLACK );
			this.loLabel.setForeground( Color.BLACK );
			this.hiLabel.setForeground( Color.BLACK );
			this.stepLabel.setForeground( Color.BLACK );
		} else {
			this.foldLabel.setForeground( Color.GRAY );
			this.xValLabel.setForeground( Color.GRAY );
			this.binLabel.setForeground( Color.GRAY );
			this.deltaLabel.setForeground( Color.GRAY );
			this.loLabel.setForeground( Color.GRAY );
			this.hiLabel.setForeground( Color.GRAY );
			this.stepLabel.setForeground( Color.GRAY );
		}
		
		this.corrHi.setEnabled( isEnabled );
		this.corrLo.setEnabled( isEnabled );
		this.corrStep.setEnabled( isEnabled );
		this.numBins.setEnabled( isEnabled );
		this.deltaMax.setEnabled( isEnabled );
		this.numFolds.setEnabled( isEnabled );
		this.numXVal.setEnabled( isEnabled );
	}//end setParamEnabled()
	
	
	/**
	 * Enables of Disables the Advanced Parameters JCheckBox and the Parameters
	 * @param isEnabled
	 */
	private void setAdvEnabled( boolean isEnabled ) {
		if( isEnabled ) {
			this.advActive.setEnabled( true );
			if( this.advActive.isSelected() ) {
				this.setParamEnabled( true );
			} else {
				this.setParamEnabled( false );
			}
		} else {
			this.advActive.setEnabled( false );
			this.setParamEnabled( false );
		}
	}
	
	
	/**
	 * Enables or Disables the numClasses JSpinner and the corresponding JTextFields 
	 * @param isEnabled
	 */
	private void setLabelEnabled( boolean isEnabled ) {
		this.numClasses.setEnabled( isEnabled );
		if( isEnabled ) {
			this.numLabel.setForeground( Color.BLACK );
		} else {
			this.numLabel.setForeground( Color.GRAY );
		}
		for( int i = 0; i < this.vField.size(); i ++ ) {
			JTextField field = ( JTextField ) this.vField.elementAt( i );
			field.setEnabled( isEnabled );
			
			JLabel label = ( JLabel ) this.vLabel.elementAt( i );
			if( isEnabled ) {
				label.setForeground( Color.BLACK ); 
			} else {
				label.setForeground( Color.GRAY );
			}
		}
	}//end setLabelEnabled()
	
	
	/**
	 * The user clicked on the numClasses JSpinner.  Either add or remove JTextFields 
	 * as necessary
	 */
	private void numClassesChanged() {
		SpinnerNumberModel model = ( SpinnerNumberModel ) this.numClasses.getModel();
		int i = model.getNumber().intValue();
		
		if( i > this.vField.size() ) {
			this.addFields( i - this.vField.size() );
		} else if( i < this.vField.size() ) {
			this.removeFields( this.vField.size() - i );
		}
	}//end numClassesChanged()
	
	
	/**
	 * Adds classLabel JTextFields and their JLabels to the GUI and the Vectors that 
	 * keep track of them - vField and vLabel respectively
	 * @param numToAdd
	 */
	private void addFields( int numToAdd ) {
		int currentNum = this.vField.size();
		
		for( int i = 0; i < numToAdd; i ++ ) {
			int nextIndex = currentNum + i;
			
			JTextField field = new JTextField();
			field.setMaximumSize( this.dField );
			field.setMinimumSize( this.dField );
			field.setPreferredSize( this.dField );
			this.fieldPanel.add( field );
			
			JLabel label = new JLabel( "Label " + ( nextIndex + 1 ) );
			label.setMaximumSize( this.dLabel );
			label.setMinimumSize( this.dLabel );
			label.setPreferredSize( this.dLabel );
			this.textPanel.add( label );
			
			this.vField.add( field );
			this.vLabel.add( label );
		}
		
		this.getRootPane().revalidate();
	}//end addFields()
	
	
	/**
	 * Removes class label JTextFields and their JLabels from the GUI and the Vectors 
	 * that keep track of them vField and vLabel respectively.
	 * @param numToRemove
	 */
	private void removeFields( int numToRemove ) {
		int currentNum = this.vField.size();
		
		for( int i = 0; i < numToRemove; i ++ ) {
			JTextField field = ( JTextField ) this.vField.elementAt( currentNum - 1 );
			JLabel label = ( JLabel ) this.vLabel.elementAt( currentNum - 1 );

			this.fieldPanel.remove( field );
			this.textPanel.remove( label );
			this.vField.remove( field );
			this.vLabel.remove( label );
		}//end i
		
		this.getRootPane().revalidate();
	}//end removeFields()
	
	
	/**
	 * Makes sure all the class labels have been accounted for.  Displays an error dialog
	 * when somethign is awry.
	 * @return
	 */
	private boolean validateLabels() {
		boolean toReturn = true;
		
		Vector v = new Vector();
		
		for( int i = 0; i < this.vField.size(); i ++ ) {
			JTextField field = ( JTextField ) this.vField.elementAt( i );
			
			//make sure there are no blank fields
			if( field.getText() == null || field.getText().equals( "" ) ) {
				this.error( "Please make sure every class has a valid Label" );
				toReturn = false;
				break;
			}
			
			//make sure this label doesn't already exist
			for( int j = 0; j < v.size(); j ++ ) {
				String s = ( String ) v.elementAt( j );
				if( s.equalsIgnoreCase( field.getText() ) ) {
					this.error( "There is more than 1 class with the label - " + s );
					toReturn = false;
					break;
				}
			}
			
			//add a new and novel class label
			v.add( field.getText() );
		}//end i
		
		return toReturn;
	}//end validateLabels()
	
	
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
	
	
	private class SpinListener implements ChangeListener {
		public void stateChanged(ChangeEvent e) {
			numClassesChanged();
		}
	}
	
	
	private class ButtonListener implements ItemListener {
		public void itemStateChanged(ItemEvent e) {
			if( e.getSource() == advActive ) {
				if( e.getStateChange() == ItemEvent.DESELECTED ) {
					setParamEnabled( false );
				} else if( e.getStateChange() == ItemEvent.SELECTED ) {
					setParamEnabled( true );
				}
			} else if( e.getSource() == trainClassify ) {
				if( e.getStateChange() == ItemEvent.SELECTED ) {
					setLabelEnabled( true );
					setAdvEnabled( true );
				}
			} else if( e.getSource() == fileClassify ) {
				if( e.getStateChange() == ItemEvent.SELECTED ) {
					setLabelEnabled( false );
					setAdvEnabled( false );
				}
			}
		}//end itemStateChanged()
	}//end ButtonListener class
	
    
	/**
	 * The class to listen to the dialog and check boxes items events.
	 */
	private class Listener extends DialogListener implements ItemListener {
        
		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			if (command.equals("ok-command")) {
				if( trainClassify.isSelected() ) {
					//need to validate class labels
					if( validateLabels() ) {
						result = JOptionPane.OK_OPTION;
						dispose();
					} else {
						//do nothing
					}
				} else {
					//loading a file
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
				HelpWindow hw = new HelpWindow(USCClassDialog.this, "USC Initialization Dialog");
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
	
	
	//--------------------------------------Getters & Setters----------------------------------
	public int getAnalysisOption() {
		if( this.trainClassify.isSelected() ) {
			return USCClassDialog.TRAIN_THEN_CLASSIFY;
		} else {
			return USCClassDialog.CLASSIFY_FROM_FILE;
		}
	}
	public int getNumClasses() {
		SpinnerNumberModel model = ( SpinnerNumberModel ) this.numClasses.getModel();
		return model.getNumber().intValue();
	}
	public int getFolds() {
		SpinnerNumberModel model = ( SpinnerNumberModel ) this.numFolds.getModel();
		return model.getNumber().intValue();
	}
	public int getXValRuns() {
		SpinnerNumberModel model = ( SpinnerNumberModel ) this.numXVal.getModel();
		return model.getNumber().intValue();
	}
	public int getNumBins() {
		SpinnerNumberModel model = ( SpinnerNumberModel ) this.numBins.getModel();
		return model.getNumber().intValue();
	}
	public int getDeltaMax() {
		SpinnerNumberModel model = ( SpinnerNumberModel ) this.deltaMax.getModel();
		return model.getNumber().intValue();
	}
	public double getCorrLo() {
		Double D = new Double( this.corrLo.getText() );
		return D.doubleValue();
	}
	public double getCorrHi() {
		Double D = new Double( this.corrHi.getText() );
		return D.doubleValue();
	}
	public double getCorrStep() {
		Double D = new Double( this.corrStep.getText() );
		return D.doubleValue();
	}
	public String[] getClassLabels() {
		String[] toReturn = new String[ this.vField.size() + 1 ];
		
		for( int i = 0; i < this.vField.size(); i ++ ) {
			JTextField field = ( JTextField ) this.vField.elementAt( i );
			toReturn[ i ] = field.getText();
		}//end i
		
		//add the option to let algorithm know this is a test hyb
		toReturn[ this.vField.size() ] = ClassAssigner.TEST_CLASS_STRING;
		
		return toReturn;
	}
}//end class