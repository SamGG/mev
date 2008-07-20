/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * Created on Jun 8, 2004
 */
package org.tigr.microarray.mev.cluster.gui.impl.usc;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SpringLayout;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.HelpWindow;

/**
 * This is the first dialog box for the USC analysis.  This should ask the user to choose
 * how they'd like to do the analysis.
 * 
 * @author vu
 */
public class USCInitDialog extends AlgorithmDialog {
	//Static ints
	static int TRAIN_THEN_CLASSIFY = 0;
	static int CLASSIFY_FROM_FILE = 1;
	
	private int result;
	
	//JRatioButtons to be selected
	
	private JButton hideAdvance;
	private JButton showAdvance;
	
	private JRadioButton trainPlusClassify;
	private JRadioButton classifyFromFile;
	private JRadioButton cv;
	private JRadioButton loocv;
	private JSpinner fold;
	private JSpinner delta;
	private JSpinner numBins;
	private JTextField corrLo;
	private JTextField corrHi;
	private JTextField corrStep;
	
	private JSpinner numClass;
	private Dimension dSpin;
	private Dimension dLabel;
	private Dimension dField;
	
	private Vector vClassField;
	private Vector vLabel;
	
	private Hashtable htField;
	private Hashtable htLabel;
	
	private JPanel centerPanel;
	private JPanel requiredPanel;
	private JPanel numClassPanel;
	private JPanel classFieldPanel;
	private JPanel classLabelPanel;
	private JPanel advPanel;
	private JPanel defaultPanel;
	private JPanel borderPanel;
	private JPanel buttonPanel;
	

	/**
	 * @param parent
	 */
	public USCInitDialog(Frame parent) {
		super(new JFrame(), "USC:Uncorrelated Shrunken Centroid Classification", true);
		this.setResizable( true );
		this.setSize( 550, 450 );
		Dimension dButton = new Dimension( 140, 20 );
		Dimension dField = new Dimension( 20, 20 );
		Dimension dSpin = new Dimension( 100, 20 );
		AdvListener al = new AdvListener();
		
		//Radio button options to find out what user wants to do
		this.trainPlusClassify = new JRadioButton("Train then Classify");
		this.trainPlusClassify.setSelected(true);
		this.classifyFromFile = new JRadioButton("Classify from file");
		this.classifyFromFile.setSelected(false);
		//group buttons so only 1 is selected at a time
		ButtonGroup selectionGroup = new ButtonGroup();
		selectionGroup.add(this.trainPlusClassify);
		selectionGroup.add(this.classifyFromFile);
		//place the radio buttons onto radioPanel and arrange
		JPanel radioPanel = new JPanel();
		radioPanel.add( this.trainPlusClassify );
		radioPanel.add( this.classifyFromFile );
		radioPanel.setBorder( BorderFactory.createTitledBorder( "Analysis Mode" ) );
		
		//numClassPanel to let user tell how many classes there are and their names
		SpinnerNumberModel classModel = new SpinnerNumberModel( 2, 1, 100, 1 );
		this.numClass = new JSpinner( classModel );
		this.numClass.setMaximumSize( dSpin );
		this.numClass.addChangeListener( new SpinListener() );
		JLabel classLabel = new JLabel( "# of Classes" );
		classLabel.setLabelFor( this.numClass );
		//place the spinner on a subPanel
		this.numClassPanel = new JPanel(  );
		this.numClassPanel.setLayout( new SpringLayout() );
		this.numClassPanel.setSize( new Dimension( 300, 300 ) );
		this.numClassPanel.add( classLabel );
		this.numClassPanel.add( this.numClass );
		SpringUtilities.makeCompactGrid( this.numClassPanel, 2, 1, 0, 0, 0, 0 );
		
		//
		this.classLabelPanel = new JPanel();
		BoxLayout labelBoxLayout = new BoxLayout( this.classLabelPanel, BoxLayout.Y_AXIS );
		this.classLabelPanel.setLayout( labelBoxLayout );
		
		//create classFieldPanel
		this.classFieldPanel = new JPanel();
		BoxLayout fieldBoxLayout =  new BoxLayout( this.classFieldPanel, BoxLayout.Y_AXIS );
		this.classFieldPanel.setLayout( fieldBoxLayout );
		this.vLabel = new Vector();
		this.vClassField = new Vector();
		this.initClassFields( 2 );
		
		//add the class stuff to requiredPanel
		this.requiredPanel = new JPanel( new SpringLayout() );
		this.requiredPanel.add( this.numClassPanel );
		this.requiredPanel.add( this.classLabelPanel );
		this.requiredPanel.add( this.classFieldPanel );
		//this.requiredPanel.setBorder( BorderFactory.createTitledBorder( "Required" ) );
		SpringUtilities.makeCompactGrid( this.requiredPanel, 1, 3, 20, 0, 20, 0 );
		
		//add the radio and spinner panels to radioPanel
		this.centerPanel = new JPanel( new SpringLayout() );
		this.centerPanel.setBackground( Color.BLACK );
		this.centerPanel.add( radioPanel );
		this.centerPanel.add( this.requiredPanel );
		SpringUtilities.makeCompactGrid( this.centerPanel, 2, 1, 0, 0, 0, 0 );
		
		//default Panel
		this.showAdvance = new JButton( "Advanced Options" );
		this.showAdvance.setMaximumSize( dButton );
		this.showAdvance.addActionListener( al );
		this.defaultPanel = new JPanel();
		this.defaultPanel.add( this.showAdvance );
		
		//advanced Panel
		this.hideAdvance = new JButton( "Hide Advanced" );
		this.hideAdvance.setMaximumSize( dButton );
		this.hideAdvance.addActionListener( al );
		this.buttonPanel = new JPanel();
		this.buttonPanel.add( this.hideAdvance );
		
		//advanced params
		this.cv = new JRadioButton( "Standard" );
		this.cv.setSelected( true );
		this.loocv = new JRadioButton( "LOOCV" );
		this.loocv.setSelected( false );
		//group as mutually exclusive
		ButtonGroup xValidationGroup = new ButtonGroup();
		xValidationGroup.add( this.cv );
		xValidationGroup.add( this.loocv );
		//place the radio buttons onto a panel
		JPanel paramSub = new JPanel();
		paramSub.setBorder( BorderFactory.createTitledBorder( "X Validation Algorithm" ) );
		paramSub.setLayout( new BoxLayout( paramSub, BoxLayout.Y_AXIS ) );
		paramSub.add( this.cv );
		paramSub.add( this.loocv );
		SpinnerNumberModel foldModel = new SpinnerNumberModel( 5, 1, 50, 1 );
		SpinnerNumberModel binModel = new SpinnerNumberModel( 50, 1, 200, 1 );
		SpinnerNumberModel deltaModel = new SpinnerNumberModel( 20, 1, 200, 1 );
		this.fold = new JSpinner( foldModel );
		this.fold.setMaximumSize( dSpin );
		this.numBins = new JSpinner( binModel );
		this.numBins.setMaximumSize( dSpin );
		this.delta = new JSpinner( deltaModel );
		this.delta.setMaximumSize( dSpin );
		this.corrLo = new JTextField( "0" );
		this.corrLo.setMaximumSize( dSpin );
		this.corrHi = new JTextField( "1.0" );
		this.corrHi.setMaximumSize( dSpin );
		this.corrStep = new JTextField( "0.1" );
		this.corrStep.setMaximumSize( dSpin );
		JLabel foldLabel = new JLabel( "Numbaaaaer of Folds" );
		JLabel binLabel = new JLabel( "# of Bins" );
		JLabel deltaLabel = new JLabel( "Delta High" );
		JLabel corrLoLabel = new JLabel( "Corr Low" );
		JLabel corrHiLabel = new JLabel( "Corr High" );
		JLabel corrStepLabel = new JLabel( "Corr Step" );
		foldLabel.setLabelFor( this.fold );
		corrLoLabel.setLabelFor( this.corrLo );
		corrHiLabel.setLabelFor( this.corrHi );
		corrStepLabel.setLabelFor( this.corrStep );
		JPanel fieldPanel = new JPanel();
		fieldPanel.setLayout( new SpringLayout() );
		fieldPanel.setBorder( BorderFactory.createTitledBorder( "Validation Parameters" ) );
		fieldPanel.add( foldLabel );
		fieldPanel.add( this.fold );
		fieldPanel.add( binLabel );
		fieldPanel.add( this.numBins );
		fieldPanel.add( deltaLabel );
		fieldPanel.add( this.delta );
		fieldPanel.add( corrLoLabel );
		fieldPanel.add( this.corrLo );
		fieldPanel.add( corrHiLabel );
		fieldPanel.add( this.corrHi );
		fieldPanel.add( corrStepLabel );
		fieldPanel.add( this.corrStep );
		SpringUtilities.makeCompactGrid( fieldPanel, 6, 2, 0, 0, 5, 0 );
		
		this.advPanel = new JPanel();
		this.advPanel.setLayout( new SpringLayout() );
		this.advPanel.add( this.hideAdvance );
		this.advPanel.add( paramSub );
		this.advPanel.add( fieldPanel );
		SpringUtilities.makeCompactGrid( this.advPanel, 3, 1, 155, 0, 155, 0 );
		
		//listen for actions
		Listener listener = new Listener();
		super.addWindowListener(listener);
		super.setActionListeners(listener);
		/*
		//finally, display it
		this.borderPanel = new JPanel();
		this.borderPanel.setLayout( new BorderLayout() );
		this.borderPanel.add( this.centerPanel, BorderLayout.CENTER );
		this.borderPanel.add( this.defaultPanel, BorderLayout.SOUTH );
		this.addContent( this.borderPanel );
		*/
		this.borderPanel = new JPanel();
		this.borderPanel.setLayout( new SpringLayout() );
		this.borderPanel.add( this.centerPanel );
		this.borderPanel.add( this.defaultPanel );
		SpringUtilities.makeCompactGrid( this.borderPanel, 2, 1, 0, 0, 0, 0 );
		this.addContent( this.borderPanel );
	}//end constructor
	
	
	private void classList() {
		SpinnerNumberModel model = ( SpinnerNumberModel ) this.numClass.getModel();
		int i = model.getNumber().intValue();
		
		if( i < this.vClassField.size() ) {
			this.removeClassFields( this.vClassField.size() - i );
		} else if( i > this.vClassField.size() ) {
			this.addClassFields( i - this.vClassField.size() );
		} else {
			//do nothing
		}
	}//end classList()
	
	
	private void initClassFields( int numToAdd ) {
		int iField = this.vClassField.size();
		
		for( int i = 0; i < numToAdd; i ++ ) {
			JLabel label = new JLabel( "Class " + ( ( iField + 1 ) + i ) + " Label:" );
			label.setMaximumSize( this.dField );
			label.setMinimumSize( this.dField );
			label.setPreferredSize( this.dField );
			
			JTextField field = new JTextField( "" );
			field.setMaximumSize( this.dField );
			field.setMinimumSize( this.dField );
			field.setPreferredSize( this.dField );
			
			label.setLabelFor( field );
			
			//this.classLabelPanel.add( label );
			//this.vLabel.add( label );
			this.classFieldPanel.add( field );
			this.vClassField.add( field );
		}
		
		this.getRootPane().revalidate();
		
		//SpringUtilities.makeCompactGrid( this.classFieldPanel, numToAdd, 2, 0, 0, 0, 0 );
	}//end addClassFields()
	
	
	private void addClassFields( int numToAdd ) {
		int iField = this.vClassField.size();
		
		for( int i = 0; i < numToAdd; i ++ ) {
			JLabel label = new JLabel( "Class " + ( ( iField + 1 ) + i ) + " Label:" );
			label.setMaximumSize( this.dField );
			label.setMinimumSize( this.dField );
			label.setPreferredSize( this.dField );
			
			JTextField field = new JTextField( "" );
			field.setMaximumSize( this.dField );
			field.setMinimumSize( this.dField );
			field.setPreferredSize( this.dField );
			
			label.setLabelFor( field );
			
			//this.classLabelPanel.add( label );
			//this.vLabel.add( label );
			this.classFieldPanel.add( field );
			this.vClassField.add( field );
		}

		this.requiredPanel.repaint();
		//this.classFieldPanel.repaint();
		this.getRootPane().revalidate();
		//pack();
		
		//SpringUtilities.makeCompactGrid( this.classFieldPanel, numToAdd, 2, 0, 0, 0, 0 );

		//this.refreshClassFields();
	}//end addClassFields()
	
	
	private void removeClassFields( int numToRemove ) {
		System.out.println( "removeClassFields() called" );
		
		for( int i = 0; i < numToRemove; i ++ ) {
			int iField = this.vClassField.size() - ( i + 1 );
			JTextField field = ( JTextField ) this.vClassField.elementAt( iField );
			this.classFieldPanel.remove( field );
			this.vClassField.remove( iField );
			
			//JLabel label = ( JLabel ) this.vLabel.elementAt( iField );
			//this.classLabelPanel.remove( label );
			//this.vLabel.remove( iField );
		}

		this.requiredPanel.repaint();
		//this.classFieldPanel.repaint();
		this.getRootPane().revalidate();
		//pack();
		//this.refreshClassFields();
	}
	
	
	private void onShowAdvanced() {
		this.borderPanel.remove( this.defaultPanel );
		this.borderPanel.add( this.advPanel );
		
		this.getRootPane().revalidate();
		pack();
	}//end onAdvanced()
	
	
	private void onHideAdvanced() {
		this.getFold();
		
		this.borderPanel.remove( this.advPanel );
		this.borderPanel.add( this.defaultPanel );
		
		this.getRootPane().revalidate();
		pack();
	}
	
	
	public int getSelectedAction() {
		if(this.trainPlusClassify.isSelected() == true) {
			return USCInitDialog.TRAIN_THEN_CLASSIFY;
		} else {
			return USCInitDialog.CLASSIFY_FROM_FILE;
		}
	}//end getSelectedAction()
	
    
	/**
	 * Shows the dialog.
	 */
	public int showModal() {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);
		show();
		return result;
	}//end showModal()
	
	
	public void error( String message ) {
		JOptionPane.showMessageDialog( this, message, "Input Error", JOptionPane.ERROR_MESSAGE );
	}//end error()
	
	
	private class AdvListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			Object source = e.getSource();
			
			System.out.println( source.toString() );
			
			if( source == showAdvance ) {
				onShowAdvanced();
			} else if( source == hideAdvance ) {
				onHideAdvanced();
			}
		}//end actionPerformed()
	}//end class
	
	
	private class SpinListener implements ChangeListener {
		public void stateChanged(ChangeEvent e) {
			classList();
		}
	}//end class
	
    
	/**
	 * The class to listen to the dialog and check boxes items events.
	 */
	private class Listener extends DialogListener implements ItemListener {
        
		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			if (command.equals("ok-command")) {
				//if( validateFields() ) {
					result = JOptionPane.OK_OPTION;
					dispose();
				//} else {
					//do nothing
				//}
			} else if (command.equals("cancel-command")) {
				result = JOptionPane.CANCEL_OPTION;
				dispose();
			} else if (command.equals("reset-command")) {
				//resetControls();
				result = JOptionPane.CANCEL_OPTION;
				return;
			} else if (command.equals("info-command")) {
				HelpWindow hw = new HelpWindow(USCInitDialog.this, "USC Initialization Dialog");
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
	
	//test harness
	public static void main(String [] args) {
		System.out.println( "invoked by main" );
		
		USCInitDialog uid = new USCInitDialog(new javax.swing.JFrame("Test"));
		uid.showModal();
	}//end main
	
	
	public int getNumClasses() {
		SpinnerNumberModel model = ( SpinnerNumberModel ) this.numClass.getModel();
		Number N = model.getNumber();
		return N.intValue();
	}
	public double getCorrLo() {
		String sCorr = this.corrLo.getText();
		Double DCorr = new Double( sCorr );
		return DCorr.doubleValue();
	}
	public double getCorrHi() {
		String sCorr = this.corrHi.getText();
		Double DCorr = new Double( sCorr );
		return DCorr.doubleValue();
	}
	public double getCorrStep() {
		String sCorr = this.corrStep.getText();
		Double DCorr = new Double( sCorr );
		return DCorr.doubleValue();
	}
	public int getFold() {
		SpinnerNumberModel model = ( SpinnerNumberModel ) this.fold.getModel();
		Number N = model.getNumber();
		return N.intValue();
	}
	public int getNumBins() {
		SpinnerNumberModel model = ( SpinnerNumberModel ) this.numBins.getModel();
		Number N = model.getNumber();
		return N.intValue();
	}
	public int getDeltaHi() {
		SpinnerNumberModel model = ( SpinnerNumberModel ) this.delta.getModel();
		Number N = model.getNumber();
		return N.intValue();
	}
	public boolean doLoocv() {
		if( this.loocv.isSelected() ) {
			return true;
		} else {
			return false;
		}
	}
}//end class
		
/*
//old code, but hesitant to trash
super(new JFrame(), "USC:Uncorrelated Shrunken Centroid Classification", true);
this.setResizable(false);
this.setSize(500,300);
		
this.htField = new Hashtable();
this.htLabel = new Hashtable();
		
this.dSpin = new Dimension( 50, 20 );
this.dLabel = new Dimension( 100, 20 );
this.dField = new Dimension( 150, 20 );
		
this.radioPanel.setLayout( new BoxLayout( this.radioPanel, BoxLayout.Y_AXIS ) );
this.classPanel = new JPanel();
this.classLabelPanel = new JPanel();
this.classLabelPanel.setLayout( new BoxLayout( this.classLabelPanel, BoxLayout.Y_AXIS ) );
this.classFieldPanel = new JPanel();
this.classFieldPanel.setLayout( new BoxLayout( this.classFieldPanel, BoxLayout.Y_AXIS ) );
		
JLabel spinnerLabel = new JLabel( "# of Classes" );
spinnerLabel.setPreferredSize( dLabel );
spinnerLabel.setMaximumSize( dLabel );
spinnerLabel.setMinimumSize( dLabel );
this.classLabelPanel.add( spinnerLabel );
		
SpinnerNumberModel spinModel = new SpinnerNumberModel( 2, 1, 25, 1);
this.spinner = new JSpinner( spinModel );
this.spinner.addChangeListener( this );
this.spinner.setPreferredSize( dSpin );
this.spinner.setMaximumSize( dSpin );
this.spinner.setMinimumSize( dSpin );
this.classFieldPanel.add( spinner );
		
//Radio button options to find out what user wants to do
this.trainPlusClassify = new JRadioButton("Train USC then Classify");
this.trainPlusClassify.setSelected(true);
this.trainPlusClassify.setHorizontalAlignment(JRadioButton.RIGHT);
radioPanel.add( this.trainPlusClassify );
		
this.classifyFromFile = new JRadioButton("Classify from a file");
this.classifyFromFile.setSelected(false);
this.classifyFromFile.setHorizontalAlignment(JRadioButton.RIGHT);
this.radioPanel.add( this.classifyFromFile );
		
//group buttons so only 1 is selected at a time
ButtonGroup selectionGroup = new ButtonGroup();
selectionGroup.add(this.trainPlusClassify);
selectionGroup.add(this.classifyFromFile);
		
this.classPanel.add( this.classLabelPanel );
this.classPanel.add( this.classFieldPanel );
JScrollPane scrollPane = new JScrollPane( this.classPanel );
JSplitPane jsp = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT, this.radioPanel, scrollPane );
		
//listen for actions
Listener listener = new Listener();
super.addWindowListener(listener);
super.setActionListeners(listener);
		
this.displayClassInputs();
		
//finally, display it
this.addContent( jspl );
*/
	
	/*
	private boolean validateFields() {
		boolean toReturn = true;
		
		Vector vLabel = new Vector();
		Vector vField = new Vector();
		
		//first make sure each class has some text in it
		Enumeration enKey = this.htField.keys();
		while( enKey.hasMoreElements() ) {
			String key = ( String ) enKey.nextElement();
			
			JLabel label = ( JLabel ) this.htLabel.get( key );
			JTextField field = ( JTextField ) this.htField.get( key );
			vLabel.add( label.getText() );
			vField.add( field.getText() );
			
			if( field.getText() == null || field.getText().equalsIgnoreCase( "" ) ) {
				toReturn = false;
				this.error( label.getText() + " cannot be blank.  Please enter a label" );
				break;
			}
		}
		
		boolean breakOuter = false;
		
		//if none are blank, look out for duplicates
		for( int i = 0; i < vField.size(); i ++ ) {
			String sI = ( String ) vField.elementAt( i );
			String labelI = ( String ) vLabel.elementAt( i );
			for( int j = 0; j < vField.size(); j ++ ) {
				String sJ = ( String ) vField.elementAt( j );
				String labelJ = ( String ) vLabel.elementAt( j );
				
				if( i != j ) {
					if( sI.equals( sJ ) ) {
						toReturn = false;
						this.error( labelI + "(" + sI + ") and " + labelJ + "(" + sJ + ") cannot be the same" );
						breakOuter = true;
						break;
					}
				}
			}//end j
			
			if( breakOuter ) {
				break;
			}
		}//end i
		return toReturn;
	}
	*/

	/*
	public int getNumClasses() {
		Integer I = ( Integer ) this.spinner.getValue();
		int numClasses = I.intValue();
		return numClasses;
	}
	
	
	public void removeClassInputs() {
		Enumeration enLabel = this.htLabel.keys();
		while( enLabel.hasMoreElements() ) {
			String key = ( String ) enLabel.nextElement();
			
			JLabel label = ( JLabel ) this.htLabel.get( key );
			JTextField field = ( JTextField ) this.htField.get( key );
			this.classLabelPanel.remove( label );
			this.classFieldPanel.remove( field );
		}
		
		this.htField.clear();
		this.htLabel.clear();

		this.getRootPane().revalidate();
	}//end removeClassInputs()
	
	
	public void displayClassInputs() {
		this.removeClassInputs();
		
		Integer I = ( Integer ) this.spinner.getValue();
		int numClasses = I.intValue();
		
		for( int i = 0; i < numClasses; i ++ ) {
			String sLabel = Integer.toString( i );
			String sLabe2Display = "Class " + Integer.toString( i + 1 ) + " Label";
			JLabel label = new JLabel( sLabe2Display );
			label.setPreferredSize( this.dLabel );
			label.setMaximumSize( this.dLabel );
			label.setMinimumSize( this.dLabel );
			this.classLabelPanel.add( label );
			
			JTextField field = new JTextField();
			field.setPreferredSize( this.dField );
			field.setMaximumSize( this.dField );
			field.setMinimumSize( this.dField );
			this.classFieldPanel.add( field );
			
			this.htField.put( sLabel, field );
			this.htLabel.put( sLabel, label );
			
			label.setVisible( true );
		}//end i
		
		this.getRootPane().revalidate();
	}//end displayClassInputs();


	//
	public void stateChanged(ChangeEvent e) {
		this.displayClassInputs();
	}//end stateChanged()
	
	
	public Hashtable getHtLabel() {
		return this.htLabel;
	}
	public Hashtable getHtField() {
		return this.htField;
	}
	*/