/*
 * Created on May 25, 2006
 */
package org.tigr.microarray.mev.r;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import org.tigr.microarray.mev.TMEV;
import org.tigr.microarray.mev.cluster.gui.impl.usc.Reader;
import org.tigr.microarray.mev.cluster.gui.impl.usc.USCTextFileFilter;

/**
 * This class is generalized for assigning some type of label to some type of 
 * entity.  Essentially, there is an array of named objects wherein each object 
 * in the array can be assigned an arbitrary value of another array
 * 
 * The array of assignees is displayed in a column with a JComboBox.  The JComboBox 
 * is populated with the Strings in the comboNamesP String[].  I chose JComboBoxes 
 * rather than an matrix of JCheckBoxes because it allows users to use the keyboard 
 * to select the pull down box item rather than being forced into using the mouse.  
 * This can be quite a bit less tedious for large lists.
 * 
 * This class also allows the user to save their assignments for obvious reasons.
 * @author iVu
 */
public class ClassAssigner {
	private boolean requireAllLabelsAssigned = true;
	private int minNumAssignsPerLabel = 3;
	
	private String[] rowNames;
	private String[] comboNames;
	private Vector vComboBox;
	
	private String borderTitle;
	private String message;
	
	private JPanel mainPanel;
	private JScrollPane jsp;

	public static String TEST_CLASS_STRING = "Unknown (Test)";
	
	
	/**
	 * Constructor
	 * @param rowNamesP	Names of the objects to be assigned something
	 * @param comboNamesP	Names of the assignment values
	 */
	public ClassAssigner( String[] rowNamesP, String[] comboNamesP ) {
		this.rowNames = rowNamesP;
		this.comboNames = comboNamesP;
		this.createAssigner();
	}//constructor
	public ClassAssigner( String[] rowNamesP, String[] comboNamesP, String borderTitleP, 
			String messageP ) {
		this.rowNames = rowNamesP;
		this.comboNames = comboNamesP;
		this.borderTitle = borderTitleP;
		this.message = messageP;
		this.createAssigner();
	}//constructor
	public ClassAssigner( String[] rowNamesP, String[] comboNamesP, 
			boolean requireAllLabelsAssignedP ) {
		this.rowNames = rowNamesP;
		this.comboNames = comboNamesP;
		this.requireAllLabelsAssigned = requireAllLabelsAssignedP;
		this.createAssigner();
	}//constructor
	public ClassAssigner( String[] rowNamesP, String[] comboNamesP, 
			boolean requireAllLabelsAssignedP, int minNumAssignsPerLabelP ) {
		this.rowNames = rowNamesP;
		this.comboNames = comboNamesP;
		this.requireAllLabelsAssigned = requireAllLabelsAssignedP;
		this.minNumAssignsPerLabel = minNumAssignsPerLabelP;
		this.createAssigner();
	}//constructor
	public ClassAssigner( String[] rowNamesP, String[] comboNamesP, String borderTitleP, 
			String messageP, boolean requireAllLabelsAssignedP, int minNumAssignsPerLabelP ) {
		this.rowNames = rowNamesP;
		this.comboNames = comboNamesP;
		this.borderTitle = borderTitleP;
		this.message = messageP;
		this.requireAllLabelsAssigned = requireAllLabelsAssignedP;
		this.minNumAssignsPerLabel = minNumAssignsPerLabelP;
		this.createAssigner();
	}//constructor

	
	/**
	 * Creates the GUI
	 */
	private void createAssigner() {
		this.mainPanel = new JPanel();
		//this.mainPanel.setPreferredSize( new Dimension( 300, 300 ) );
		this.vComboBox = new Vector();
		
		//some variables that will be used to construct the gui
		int rowKount = this.rowNames.length;
		int colKount = this.comboNames.length;
		Dimension dRowNameLabel = new Dimension( 250, 24 );
		Dimension dCombo = new Dimension( 150, 24 );
		Dimension dGap = new Dimension( 5, 24 );
		//Dimension dHorPadding = new Dimension( 20, 24 );
		
		//set panel attributes
		this.mainPanel.setLayout( new GridBagLayout() );
		GridBagConstraints c = new GridBagConstraints();
		
		//set some text as a message (instruction) if text supplied
		if( this.message != null ) {
			JLabel label = new JLabel( this.message );
			c.insets = new Insets( 25, 25, 25, 25 );
			c.gridy = 0;
			this.mainPanel.add( label, c );
		}//message
		
		//reset the insets for the rows
		c.insets = new Insets( 0, 0, 0, 4 );
		//loop through the rows
		for( int r = 0; r < rowKount; r ++ ) {
			//create a Label for this row
			JLabel rowLabel = new JLabel( this.rowNames[ r ] );
			rowLabel.setPreferredSize( dRowNameLabel );
			rowLabel.setHorizontalAlignment( JLabel.RIGHT );
			
			//now create the comboBox for this row
			JComboBox rowCombo = new JComboBox( this.comboNames );
			rowCombo.setPreferredSize( dCombo );
			
			//put this on its own JPanel
			JPanel rowPanel = new JPanel();
			rowPanel.setLayout( new BoxLayout( rowPanel, BoxLayout.X_AXIS ) );
			//rowPanel.add( Box.createHorizontalGlue() );
			rowPanel.add( rowLabel );
			rowPanel.add( Box.createRigidArea( dGap ) );
			rowPanel.add( rowCombo );
			//rowPanel.add( Box.createHorizontalGlue() );
			
			//shade every other row for readability
			if( r % 2 == 0 ) {
				rowPanel.setBackground( Color.LIGHT_GRAY );
			}
			
			//add row to main
			c.gridy = r + 1;
			this.mainPanel.add( rowPanel, c );
			
			//add the box to the Vector
			this.vComboBox.add( rowCombo );
		}//r
		
		//create a border if calling class supplied a title
		if( this.borderTitle != null ) {
			Border greyLine = BorderFactory.createLineBorder( Color.LIGHT_GRAY, 1 );
			Font font11 = new Font( "Arial", Font.PLAIN, 11 );
			TitledBorder border = BorderFactory.createTitledBorder( greyLine, 
					this.borderTitle, TitledBorder.LEADING, TitledBorder.TOP, font11 );
			this.mainPanel.setBorder( border );
		}//border
		
		this.jsp = new JScrollPane( this.mainPanel );
		this.jsp.setPreferredSize( new Dimension( 450, 350 ) );
	}//createAssigner()
	
	
	/**
	 * 
	 * @return
	 */
	public boolean verifyLabeling() {
		boolean toReturn = true;
		
		//just to make sure that we really want to verify labelling
		if( this.requireAllLabelsAssigned ) {
			//loop through the labels
			for( int i = 0; i < this.comboNames.length; i ++ ) {
				//disregard if this is the test label
				if( this.comboNames[ i ].equalsIgnoreCase( ClassAssigner.TEST_CLASS_STRING ) ) {
					//ignore
				} else {
					//now kount # of times it is selected in combos
					int occurKount = 0;
					for( int j = 0; j < this.vComboBox.size(); j ++ ) {
						JComboBox box = ( JComboBox ) this.vComboBox.elementAt( j );
						int iSelected = box.getSelectedIndex();
						if( iSelected == i ) {
							occurKount ++;
						}
					}//j
					
					//is this at least minimum # of occurences
					if( occurKount < this.minNumAssignsPerLabel ) {
						//not enough
						String sErr = "Class " + this.comboNames[ i ] + " has been assigned " 
							+ occurKount + " times.\r\nIt must be assigned at least " 
							+ this.minNumAssignsPerLabel + " times";
						this.error( sErr );
						toReturn = false;
						break;
					}
				}
			}//i
		} else {
			toReturn = true;
		}
		
		return toReturn;
	}//verifyLabeling()
	
	
	/**
	 * 
	 */
	public void onSaveAssignments() {
		StringBuffer sb = new StringBuffer();
		String newLine = "\r\n";
		for( int i = 0; i < this.vComboBox.size(); i ++ ) {
			JComboBox box = ( JComboBox ) this.vComboBox.elementAt( i );
			sb.append( box.getSelectedIndex() );
			sb.append( newLine );
		}//i
		
		//show a dialog so user can set save path
		//load the current path
		String dataPath = "/" + TMEV.getDataPath();
		if( dataPath == null ) {
			dataPath = "";
		}
		
		//pop up dialog for save
		JFileChooser chooser = new JFileChooser( dataPath );
		USCTextFileFilter textFilter = new USCTextFileFilter();
		chooser.addChoosableFileFilter( textFilter );
		int returnVal = chooser.showSaveDialog( new Frame() );
		if( returnVal == JFileChooser.APPROVE_OPTION ) {
			File saveFile;
			
			if( chooser.getFileFilter() == textFilter ) {
				//make sure to add .txt
				String path = chooser.getSelectedFile().getPath();
				if( path.toLowerCase().endsWith( "txt" ) ) {
					//great, already ok
					saveFile = new File( path );
				} else {
					//add it
					String subPath;
					int period = path.lastIndexOf( "." );
					if( period != -1 ) {
						System.out.println( "period  = -1" );
						subPath = path.substring( 0, period );
					} else {
						subPath = path;
					}
					String newPath = subPath + ".txt";
					saveFile = new File( newPath );
				}
			} else {
				saveFile = chooser.getSelectedFile();
			}
			
			this.writeFile( saveFile, sb.toString() );
		} else {
			//user cancelled
		}
	}//onSaveAssignments()
	
	
	/**
	 * Write the String s to File f
	 * @param f
	 * @param s
	 */
	private void writeFile( File f, String s ) {
		try {
			FileWriter fw = new FileWriter( f );
			fw.write( s );
			fw.flush();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
			this.error( e.getMessage() );
		}
	}//writeFile()
	
	
	/**
	 * 
	 */
	public void onLoadAssignments() {
		//show a file dialog
		GenericFileDialog gfd = new GenericFileDialog( new Frame(), "Load the Class File" );
		if( gfd.showModal() == JOptionPane.OK_OPTION ) {
			File fLoaded = gfd.getSelectedFile();
			//File testFile = new File( "/Users/iVu/Documents/Dev/MeV/TMEV3.1_RAMA1.0/data/AML/smallClassFile.txt" );
			try {
				this.readClassFile( fLoaded );
			} catch (Exception e) {
				e.printStackTrace();
				this.error( e.getMessage() );
			}
		} else {
			//user cancelled, do nothing
		}
	}//onLoadSelection()
	
	
	/**
	 * 
	 * @param fLoaded
	 * @throws Exception
	 */
	private void readClassFile( File fLoaded ) throws Exception {
		Reader r = new Reader();
		r.readFile( fLoaded );
		Vector vLine = r.getVNullLine( "MOTHRA" );
		
		//first check to see if there are the same number of slides
		if( vLine.size() == this.rowNames.length ) {
			//loop through the lines and alter the JComboBoxes accordingly
			for( int l = 0; l < vLine.size(); l ++ ) {
				String line = ( String ) vLine.elementAt( l );
				if( line.equals( ClassAssigner.TEST_CLASS_STRING ) ) {
					//select the test case
					this.setClassAsTest( l );
				} else {
					//select the appropriate class
					int i = Integer.parseInt( line );
					this.setSelectedClass( l, i );
				}
			}//l
		} else {
			throw new Exception( "Your Assignments File and loaded data don't appear to match" );
		}
	}//readClassFile()
	
	

	//--------------------------------------Getters & Setters----------------------------------
	public void setSelectedClass( int iHyb, int iSelectedIndex ) {
		JComboBox box = ( JComboBox ) this.vComboBox.elementAt( iHyb );
		box.setSelectedIndex( iSelectedIndex );
		this.mainPanel.repaint();
	}
	public void setClassAsTest( int iHyb ) {
		JComboBox box = ( JComboBox ) this.vComboBox.elementAt( iHyb );
		int iTest = this.comboNames.length - 1;
		box.setSelectedIndex( iTest );
		this.mainPanel.repaint();
	}
	public void setRequireAllLabelsAssigned( boolean b ) {
		this.requireAllLabelsAssigned = b;
	}
	public boolean getRequireAllLabelsAssigned() {
		return this.requireAllLabelsAssigned;
	}
	public void setMinAssignsPerLabel( int i ) {
		this.minNumAssignsPerLabel = i;
	}
	public int getMinNumAssignsPerLabel() {
		return this.minNumAssignsPerLabel;
	}
	/**
	 * Returns a Vector containing all the JComboBoxes in order
	 * @return
	 */
	public Vector getVComboBox() {
		return this.vComboBox;
	}
	/**
	 * Returns the index of the selected item in the JComboBox specified by
	 * rowIndex
	 * @param rowIndex
	 * @return
	 */
	public int getSelectedIndex( int rowIndex ) {
		int toReturn = -1;
		
		JComboBox box = ( JComboBox ) this.vComboBox.elementAt( rowIndex );
		toReturn = box.getSelectedIndex();
		
		return toReturn;
	}
	/**
	 * Returns the String displayed in the JComboBox specified by rowIndex
	 * @param rowIndex
	 * @return
	 */
	public String getSelectedString( int rowIndex ) {
		String toReturn = null;
		
		JComboBox box = ( JComboBox ) this.vComboBox.elementAt( rowIndex );
		toReturn = ( String ) box.getSelectedItem();
		
		return toReturn;
	}
	public boolean hasTest() {
		boolean toReturn = false;
		
		for( int i = 0; i < this.vComboBox.size(); i ++ ) {
			JComboBox box = ( JComboBox ) this.vComboBox.elementAt( i );
			String selectedString = ( String ) box.getSelectedItem();
			if( selectedString.equals( ClassAssigner.TEST_CLASS_STRING ) ) {
				toReturn = true;
				break;
			}
			/*
			if( box.getSelectedIndex() != this.comboNames.length ) {
				toReturn = true;
				break;
			}
			*/
		}
		
		return toReturn;
	}
	/*
	public JPanel getMainPanel() {
		return this.mainPanel;
	}
	*/
	public JScrollPane getScrollPane() {
		return this.jsp;
	}
	//-------------------------------------- End Getters & Setters----------------------------------
	
	/**
	 * Displays an error dialog
	 * @param message
	 */
	public void error( String message ) {
		JOptionPane.showMessageDialog( new JFrame(), 
				message, "Input Error", JOptionPane.ERROR_MESSAGE );
	}//end error()
	
	
	/**
	 * Test Harness
	 * @param args
	 */
	public static void main( String[] args ) {
		String[] rows = new String[ 25 ];
		for( int i = 0; i < rows.length; i ++ ) {
			rows[ i ] = "Slide:" + Integer.toString( i );
		}
		String[] combos = new String[ 3 ];
		combos[ 0 ] = "Test";
		combos[ 1 ] = "Tumor";
		combos[ 2 ] = "Normal";
		
		
		JFrame jf = new JFrame();
		jf.setSize( 400, 600 );
		ClassAssigner ca = new ClassAssigner( rows, combos );
		System.out.println( ca.getSelectedString( 0 ) );
		jf.setContentPane( ca.getScrollPane() );
		jf.show();
	}
}
