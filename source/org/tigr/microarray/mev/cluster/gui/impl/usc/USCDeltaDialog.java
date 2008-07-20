/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * Created on Dec 3, 2004
 */
package org.tigr.microarray.mev.cluster.gui.impl.usc;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowEvent;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ToolTipManager;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.dialogHelpUtil.HelpWindow;
import org.tigr.microarray.mev.r.TableSorter;

/**
 * Dialog allows user to choose the Delta and Rho values to use for classification.
 * 
 * @author vu
 */
public class USCDeltaDialog extends AlgorithmDialog {
	private USCFoldResult[] foldResults;		//USCFoldResult[ numFolds ]
	private USCXValResult xValResults;			//
	private USCDeltaRhoResult[][][] xResult;
	private int buttonPushed;
	private JPanel mainPanel;
	private JCheckBox saveBox;
	private Vector vRow;
	private JTable jt;
	
	
	/**
	 * Deal with the case where #Folds=1, in which case, don't display mistakes or calls
	 * @param results
	 * @param foldIsOne
	 */
	public USCDeltaDialog( USCXValResult results, boolean foldIsOne ) {
		super( new JFrame(), "Choose your parameters", true );
		
		this.setSize( 600, 400 );
		
		this.xValResults = results;
		
		this.createGUI( this.xValResults, foldIsOne );
		
		Listener listener = new Listener();
		super.addWindowListener( listener );
		super.setActionListeners( listener );
	}
	public USCDeltaDialog(USCDeltaRhoResult[][][] xResultP, boolean foldIsOne) {
		super( new JFrame(), "Choose your parameters", true );
		this.setSize( 600, 400);;
		
		this.xResult = xResultP;
		
		this.createGUI(this.xResult, foldIsOne);
		
		Listener listener = new Listener();
		super.addWindowListener( listener );
		super.setActionListeners( listener );
	}
	
	
	/**
	 * 
	 * @param results USCFoldResult[ numDelta * numRho ] (50 * 6 = 300 default)
	 */
	private void createGUI( USCXValResult results, boolean foldIsOne ) {
		this.mainPanel = new JPanel( new BorderLayout() );
		
		JPanel radioPanel = this.createRadioPanel( results, foldIsOne );
		this.mainPanel.add( radioPanel, BorderLayout.CENTER );
		
		this.mainPanel.add( this.createSavePanel(), BorderLayout.SOUTH );
		
		this.addContent( this.mainPanel );
	}//createGUI()
	private void createGUI( USCDeltaRhoResult[][][] xResult, boolean foldIsOne ) {
		this.mainPanel = new JPanel( new BorderLayout() );
		
		JPanel radioPanel = this.createRadioPanel( xResult, foldIsOne );
		this.mainPanel.add( radioPanel, BorderLayout.CENTER );
		this.mainPanel.add( this.createSavePanel(), BorderLayout.SOUTH );
		this.addContent( this.mainPanel );
	}
	
	
	/**
	 * 
	 * @param results USCFoldResult[ numDelta * numRho ] (50 * 6 = 300 default)
	 * @return
	 */
	private JPanel createRadioPanel( USCDeltaRhoResult[][][] results, boolean foldIsOne ) {
		JPanel toReturn = new JPanel();
		
		//store the USCRow objects
		this.vRow = this.createRows( results );
		
		//column headers
		String[] header;
		String[] toolTips;
		Object[][] table;
		
		if( foldIsOne ) {
			header = new String[ 4 ];
			header[ 0 ] = "";
			header[ 1 ] = "Avg#Genes";
			header[ 2 ] = "Delta";
			header[ 3 ] = "Rho";
			toolTips = new String[ 4 ];
			toolTips[ 0 ] = "Choose the Delta/Rho combination to use";
			toolTips[ 1 ] = "Average # of Genes Used to classify over all CrossValidation folds";
			toolTips[ 2 ] = "Amount of Shrinkage applied to Class Centroids";
			toolTips[ 3 ] = "Lower Threshold of Gene Correlation";
			
			table = new Object[ this.vRow.size() ][ 4 ];
			for( int i = 0; i < this.vRow.size(); i ++ ) {
				USCRow row = ( USCRow ) this.vRow.elementAt( i );
				table[ i ][ 0 ] = row.getButton();
				table[ i ][ 1 ] = row.getNumGenes();
				table[ i ][ 2 ] = row.getDelta();
				table[ i ][ 3 ] = row.getRho();
			}//i
		} else {
			header = new String [ 5 ];
			header[ 0 ] = "";
			header[ 1 ] = "#Mistakes";
			header[ 2 ] = "Avg#Genes";
			header[ 3 ] = "Delta";
			header[ 4 ] = "Rho";
			toolTips = new String[ 5 ];
			toolTips[ 0 ] = "Choose the Delta/Rho combination to use";
			toolTips[ 1 ] = "Total # of Mistaken Class Assignments";
			toolTips[ 2 ] = "Average # of Genes Used to classify over all CrossValidation folds";
			toolTips[ 3 ] = "Amount of Shrinkage applied to Class Centroids";
			toolTips[ 4 ] = "Lower Threshold of Gene Correlation";
			
			table = new Object[ this.vRow.size() ][ 5 ];
			for( int i = 0; i < this.vRow.size(); i ++ ) {
				USCRow row = ( USCRow ) this.vRow.elementAt( i );
				table[ i ][ 0 ] = row.getButton();
				table[ i ][ 1 ] = row.getNumErrors();
				table[ i ][ 2 ] = row.getNumGenes();
				table[ i ][ 3 ] = row.getDelta();
				table[ i ][ 4 ] = row.getRho();
			}//i
		}
		
		//the table data
		DefaultTableModel dm = new DefaultTableModel();
		dm.setDataVector( table, header );
		
		//create the JTable
		TableSorter sorter = new TableSorter( dm );
		this.jt = new JTable( sorter ) {
			public void tableChanged(TableModelEvent e) {
				super.tableChanged(e);
				repaint();
			}
		};
		sorter.setTableHeader( this.jt.getTableHeader() );
		if( foldIsOne ) {
			this.jt.setPreferredScrollableViewportSize(new Dimension(245, 240));
		
			//set column widths
			this.jt.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );
			TableColumn column = null;
			for ( int i = 0; i < table[ 0 ].length; i ++ ) {
				column = jt.getColumnModel().getColumn(i);
				if (i == 0) {
					column.setPreferredWidth(20);
					column.setMinWidth(20);
				} else if( i == 1 || i == 2 ) {
					column.setPreferredWidth(75);
					column.setMinWidth(75);
				} else if( i == 3 ) {
					column.setPreferredWidth(55);
					column.setMinWidth(55);
				}
			}
		} else {
			this.jt.setPreferredScrollableViewportSize(new Dimension(280, 240));
		
			//set column widths
			this.jt.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );
			TableColumn column = null;
			for ( int i = 0; i < table[ 0 ].length; i ++ ) {
				column = jt.getColumnModel().getColumn(i);
				if (i == 0) {
					column.setPreferredWidth(20);
					column.setMinWidth(20);
				} else if( i == 1 ) {
					column.setPreferredWidth(75);
					column.setMinWidth(75);
				} else if( i == 2 ) {
					column.setPreferredWidth(75);
					column.setMinWidth(75);
				} else if(i == 3 || i == 4 ) {
					column.setPreferredWidth(55);
					column.setMinWidth(55);
				} else {
					column.setPreferredWidth(50);
					column.setMinWidth(50);
				}
			}
		}
		
		this.jt.getColumn("").setCellRenderer( new RadioButtonRenderer() );
		this.jt.getColumn("").setCellEditor(new RadioButtonEditor(new JCheckBox()));
		
		//set tool tips for columns
		ColumnHeaderToolTips tips = new ColumnHeaderToolTips();
		for( int t = 0; t < toolTips.length; t ++ ) {
			TableColumn col = this.jt.getColumnModel().getColumn( t );
			tips.setToolTip( col, toolTips[ t ] );
		}
		JTableHeader head = this.jt.getTableHeader();
		head.addMouseMotionListener(tips);
		ToolTipManager.sharedInstance().setInitialDelay( 0 );

		//group the radio buttons
		ButtonGroup group1 = new ButtonGroup();
		for( int i = 0; i < this.vRow.size(); i ++ ) {
			group1.add( ( JRadioButton ) dm.getValueAt( i, 0 ) );
		}
		
		//add components and return
		JScrollPane jsp = new JScrollPane( this.jt );
		toReturn.add( jsp );
		
		return toReturn;
	}//createRadioPanel()
	private JPanel createRadioPanel( USCXValResult results, boolean foldIsOne ) {
		JPanel toReturn = new JPanel();
		
		//store the USCRow objects
		this.vRow = this.createRows( results );
		
		//column headers
		String[] header;
		String[] toolTips;
		Object[][] table;
		
		if( foldIsOne ) {
			header = new String[ 4 ];
			header[ 0 ] = "";
			header[ 1 ] = "Avg#Genes";
			header[ 2 ] = "Delta";
			header[ 3 ] = "Rho";
			toolTips = new String[ 4 ];
			toolTips[ 0 ] = "Choose the Delta/Rho combination to use";
			toolTips[ 1 ] = "Average # of Genes Used to classify over all CrossValidation folds";
			toolTips[ 2 ] = "Amount of Shrinkage applied to Class Centroids";
			toolTips[ 3 ] = "Lower Threshold of Gene Correlation";
			
			table = new Object[ this.vRow.size() ][ 4 ];
			for( int i = 0; i < this.vRow.size(); i ++ ) {
				USCRow row = ( USCRow ) this.vRow.elementAt( i );
				table[ i ][ 0 ] = row.getButton();
				table[ i ][ 1 ] = row.getNumGenes();
				table[ i ][ 2 ] = row.getDelta();
				table[ i ][ 3 ] = row.getRho();
			}//i
		} else {
			header = new String [ 5 ];
			header[ 0 ] = "";
			header[ 1 ] = "#Mistakes";
			header[ 2 ] = "Avg#Genes";
			header[ 3 ] = "Delta";
			header[ 4 ] = "Rho";
			toolTips = new String[ 5 ];
			toolTips[ 0 ] = "Choose the Delta/Rho combination to use";
			toolTips[ 1 ] = "Total # of Mistaken Class Assignments";
			toolTips[ 2 ] = "Average # of Genes Used to classify over all CrossValidation folds";
			toolTips[ 3 ] = "Amount of Shrinkage applied to Class Centroids";
			toolTips[ 4 ] = "Lower Threshold of Gene Correlation";
			
			table = new Object[ this.vRow.size() ][ 5 ];
			for( int i = 0; i < this.vRow.size(); i ++ ) {
				USCRow row = ( USCRow ) this.vRow.elementAt( i );
				table[ i ][ 0 ] = row.getButton();
				table[ i ][ 1 ] = row.getNumErrors();
				table[ i ][ 2 ] = row.getNumGenes();
				table[ i ][ 3 ] = row.getDelta();
				table[ i ][ 4 ] = row.getRho();
			}//i
		}
		
		//the table data
		DefaultTableModel dm = new DefaultTableModel();
		dm.setDataVector( table, header );
		
		//create the JTable
		TableSorter sorter = new TableSorter( dm );
		this.jt = new JTable( sorter ) {
			public void tableChanged(TableModelEvent e) {
				super.tableChanged(e);
				repaint();
			}
		};
		sorter.setTableHeader( this.jt.getTableHeader() );
		if( foldIsOne ) {
			this.jt.setPreferredScrollableViewportSize(new Dimension(245, 240));
		
			//set column widths
			this.jt.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );
			TableColumn column = null;
			for ( int i = 0; i < table[ 0 ].length; i ++ ) {
				column = jt.getColumnModel().getColumn(i);
				if (i == 0) {
					column.setPreferredWidth(20);
					column.setMinWidth(20);
				} else if( i == 1 || i == 2 ) {
					column.setPreferredWidth(75);
					column.setMinWidth(75);
				} else if( i == 3 ) {
					column.setPreferredWidth(55);
					column.setMinWidth(55);
				}
			}
		} else {
			this.jt.setPreferredScrollableViewportSize(new Dimension(280, 240));
		
			//set column widths
			this.jt.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );
			TableColumn column = null;
			for ( int i = 0; i < table[ 0 ].length; i ++ ) {
				column = jt.getColumnModel().getColumn(i);
				if (i == 0) {
					column.setPreferredWidth(20);
					column.setMinWidth(20);
				} else if( i == 1 ) {
					column.setPreferredWidth(75);
					column.setMinWidth(75);
				} else if( i == 2 ) {
					column.setPreferredWidth(75);
					column.setMinWidth(75);
				} else if(i == 3 || i == 4 ) {
					column.setPreferredWidth(55);
					column.setMinWidth(55);
				} else {
					column.setPreferredWidth(50);
					column.setMinWidth(50);
				}
			}
		}
		
		this.jt.getColumn("").setCellRenderer( new RadioButtonRenderer() );
		this.jt.getColumn("").setCellEditor(new RadioButtonEditor(new JCheckBox()));
		
		//set tool tips for columns
		ColumnHeaderToolTips tips = new ColumnHeaderToolTips();
		for( int t = 0; t < toolTips.length; t ++ ) {
			TableColumn col = this.jt.getColumnModel().getColumn( t );
			tips.setToolTip( col, toolTips[ t ] );
		}
		JTableHeader head = this.jt.getTableHeader();
		head.addMouseMotionListener(tips);
		ToolTipManager.sharedInstance().setInitialDelay( 0 );

		//group the radio buttons
		ButtonGroup group1 = new ButtonGroup();
		for( int i = 0; i < this.vRow.size(); i ++ ) {
			group1.add( ( JRadioButton ) dm.getValueAt( i, 0 ) );
		}
		
		//add components and return
		JScrollPane jsp = new JScrollPane( this.jt );
		toReturn.add( jsp );
		
		return toReturn;
	}//createRadioPanel()
	
	
	/**
	 * 
	 * @return
	 */
	private JPanel createSavePanel() {
		JPanel toReturn = new JPanel();
		
		this.saveBox = new JCheckBox( "Save Training Results" );
		this.saveBox.setSelected( true );
		
		toReturn.add( saveBox );
		
		return toReturn;
	}//createSavePanel()
	
	
	/**
	 * Compile the results into useable form
	 * @param results
	 * @return
	 */
	private Vector createRows( USCDeltaRhoResult[][][] results ) {
		Vector vReturn = new Vector();
		
		ButtonGroup bg = new ButtonGroup();
		
		int resultKount = results[0].length;
		
		//loop through results
		for( int r = 0; r < results[ 0 ][ 0 ].length; r ++ ) {
			int iCorrect = 0;
			int iMistake = 0;
			int iNumGenes = 0;
			int iResults = 0;
			double delta = 0;
			double rho = 0;
			
			//compile fold results
			for(int f = 0; f < results[ 0 ].length; f ++ ) {
				for( int m = 0; m < results.length; m ++ ) {
					USCDeltaRhoResult dr = results[ m ][ f ][ r ];
					if(!dr.isNull()){
						iMistake += dr.getMistake();
						iCorrect += dr.getCorrect();
						iNumGenes += dr.getNumGene();
						iResults ++;
						delta = dr.getDelta();
						rho = dr.getRho();
					}
				}//m
			}//f
			
			DecimalFormat df = new DecimalFormat( "###.#" );
			
			double mistakeAvg = ( double ) iMistake / ( double ) iResults;
			double numGenesAvg = ( double ) iNumGenes / ( double ) iResults;
			//String sMistake = df.format( mistakeAvg ) + " / " + Integer.toString( iResults );
			String sMistake = df.format( mistakeAvg );
			String sNumGenes = df.format( numGenesAvg );
			String sDelta = df.format( delta );
			String sRho = df.format( rho );
			
			USCRow row = new USCRow( sMistake, sNumGenes, sDelta, sRho );
			bg.add( row.getButton() );
			vReturn.add( row );
		}//r
		
		return vReturn;
	}//createRows()
	private Vector createRows( USCXValResult xValResults ) {
		Vector vReturn = new Vector();
		
		ButtonGroup bg = new ButtonGroup();
		
		int resultKount = xValResults.getFoldResult( 0 )[ 0 ].getResultKount();
		
		//loop through the delta/rho combos
		for( int r = 0; r < resultKount; r ++ ) {
			int iCorrect = 0;
			int iMistake = 0;
			int iTotalCalls = 0;
			int iTotalNumGenes = 0;
			int iGenesDenom = 0;
			boolean hasResult = false;
			double delta = 0;
			double rho = 0;
			
			//loop through the xValRuns
			for( int x = 0; x < xValResults.getXValKount(); x ++ ) {
				//get the USCFoldResult[] for this xValRun
				USCFoldResult[] foldResultArray = xValResults.getFoldResult( x );
				
				//loop through the folds in each xValRun
				for( int f = 0; f < foldResultArray.length; f ++ ) {
					USCFoldResult foldResult = foldResultArray[ f ];
					
					//see if this fold produced a result
					if( foldResult.hasResult( r ) ) {
						//if it hasn't already been marked as true, mark it as true
						if( ! hasResult ) { hasResult = true; }
						
						//get the d/r result for this fold
						USCResult result = foldResult.getResult( r );
						delta = result.getDelta();
						rho = result.getRho();
						
						//keep track of total num genes for avg later
						iTotalNumGenes += result.getNumGenesUsed();
						iGenesDenom ++;
						
						//get the scores of the hybs of interest
						double[][] dScores = result.getDiscScores();
						USCHyb[] testArray = foldResult.getTestArray();
						
						//test the testHybs to see if the assignments were indeed correct
						for( int h = 0; h < dScores.length; h ++ ) {
							double dMin = 99999999;
							 int iMin = 0;
							 
							 //loop through disc scores to find the min
							 for( int c = 0; c < dScores[ h ].length; c ++ ) {
							 	if( dScores[ h ][ c ] < dMin ) {
							 		dMin = dScores[ h ][ c ];
							 		iMin = c;
							 	}
							 }//end c
							 
							 //validate the assignment
							 if( testArray[ h ].getUniqueLabelIndex() == iMin ) {
							 	iCorrect ++;
							 	iTotalCalls ++;
							 } else {
							 	iMistake ++;
							 	iTotalCalls ++;
							 }
						}//end h
					}//end if
				}//end f
			}//end x
			
			//avg the number of genes used
			double fNumGenes;
			if( iGenesDenom > 0 ) {
				fNumGenes = ( double ) iTotalNumGenes / ( double ) iGenesDenom;
			} else {
				fNumGenes = 0.0f;
			}
			
			//compile result data
			if( hasResult ) {
				USCRow row = new USCRow( iMistake, iTotalCalls, fNumGenes, delta, rho );
				bg.add( row.getButton() );
				vReturn.add( row );
				
				//set the default as selected
				if( vReturn.size() == 1 ) {
					row.getButton().setSelected( true );
				}//end if
			}//end if
		}//end r
		
		return vReturn;
	}//createRows()
	
	
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
		return buttonPushed;
	}//end showModal()
	
	
	/**
	 * 
	 * @author vu
	 */
	private class RadioButtonRenderer implements TableCellRenderer {
		public Component getTableCellRendererComponent(JTable table, Object value,
		boolean isSelected, boolean hasFocus, int row, int column) {
			if (value==null) return null;
				return (Component)value;
		}
	}//end class
	
	
	/**
	 * 
	 * @author vu
	 */
	private class RadioButtonEditor extends DefaultCellEditor implements ItemListener {
		private JRadioButton button;

		public RadioButtonEditor(JCheckBox checkBox) {
			super(checkBox);
		}

		public Component getTableCellEditorComponent( JTable table, Object value, 
		boolean isSelected, int row, int column) {
			if (value == null)
				return null;
			button = (JRadioButton) value;
			button.addItemListener(this);
			return (Component) value;
		}

		public Object getCellEditorValue() {
			button.removeItemListener(this);
			return button;
		}

		public void itemStateChanged(ItemEvent e) {
			super.fireEditingStopped();
		}
	}//end class
	
    
	/**
	 * The class to listen to the dialog and check boxes items events.
	 */
	private class Listener extends DialogListener implements ItemListener {
        
		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			if (command.equals("ok-command")) {
				buttonPushed = JOptionPane.OK_OPTION;
				dispose();
			} else if (command.equals("cancel-command")) {
				buttonPushed = JOptionPane.CANCEL_OPTION;
				dispose();
			} else if (command.equals("reset-command")) {
				//resetControls();
				buttonPushed = JOptionPane.CANCEL_OPTION;
				return;
			} else if (command.equals("info-command")) {
				HelpWindow hw = new HelpWindow(USCDeltaDialog.this, "USC Delta Dialog");
				buttonPushed = JOptionPane.CANCEL_OPTION;
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
			buttonPushed = JOptionPane.CLOSED_OPTION;
			dispose();
		}
	}//end internal Listener class
	
	
	/**
	 * 
	 * @author vu
	 */
	private class ColumnHeaderToolTips extends MouseMotionAdapter {
		// Current column whose tooltip is being displayed.
		// This variable is used to minimize the calls to setToolTipText().
		TableColumn curCol;

		// Maps TableColumn objects to tooltips
		Map tips = new HashMap();

		// If tooltip is null, removes any tooltip text.
		public void setToolTip(TableColumn col, String tooltip) {
			if (tooltip == null) {
				tips.remove(col);
			} else {
				tips.put(col, tooltip);
			}
		}//end setToolTip

		public void mouseMoved(MouseEvent evt) {
			TableColumn col = null;
			JTableHeader header = (JTableHeader) evt.getSource();
			JTable table = header.getTable();
			TableColumnModel colModel = table.getColumnModel();
			int vColIndex = colModel.getColumnIndexAtX(evt.getX());

			// Return if not clicked on any column header
			if (vColIndex >= 0) {
				col = colModel.getColumn(vColIndex);
			}

			if (col != curCol) {
				header.setToolTipText((String) tips.get(col));
				curCol = col;
			}
		}
	}//end class

	//--------------------------------------Getters & Setters----------------------------------
	public USCRow getSelectedRow() {
		USCRow toReturn = null;
		
		int selectedIndex = 0;
		for( int i = 0; i < this.jt.getRowCount(); i ++ ) {
			TableModel dm = this.jt.getModel();
			JRadioButton rButton = ( JRadioButton ) dm.getValueAt( i, 0 );
			
			if( rButton.isSelected() ) {
				//find the USCRow object of this selected JRadioButton
				for( int r = 0; r < this.vRow.size(); r ++ ) {
					USCRow row = ( USCRow ) this.vRow.elementAt( r );
					if( rButton == row.getButton() ) {
						toReturn = row;
					}
				}//end r
				
				break;
			}
		}
		return toReturn;
	}//end getSelectedRow()
	public boolean saveTraining() {
		return this.saveBox.isSelected();
	}
}//end class