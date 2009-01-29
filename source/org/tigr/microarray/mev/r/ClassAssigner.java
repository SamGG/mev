/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
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
    	int numGroups = comboNames.length;	
		File file;		
		JFileChooser fileChooser = new JFileChooser("./data");	
		
		if(fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
			file = fileChooser.getSelectedFile();			
			try {
				PrintWriter pw = new PrintWriter(new FileWriter(file));
				
				//comment row
				Date currDate = new Date(System.currentTimeMillis());			
				String dateString = currDate.toString();;
				String userName = System.getProperty("user.name");
				
				pw.println("# Assignment File");
				pw.println("# User: "+userName+" Save Date: "+dateString);
				pw.println("#");
				
				//save group names..?
				
				pw.print("Module:\t");
				pw.println("USC");
				
				//Omits the 'Unknown' group from the Group Labels
				for (int i=0; i<numGroups-1; i++){
    				pw.print("Group "+(i+1)+" Label:\t");
					pw.println(comboNames[i]);
				}
								
				pw.println("#");
				
				pw.println("Sample Index\tSample Name\tGroup Assignment");

				for(int sample = 0; sample < vComboBox.size(); sample++) {
					JComboBox box = ( JComboBox ) this.vComboBox.elementAt(sample);
					pw.print(String.valueOf(sample+1)+"\t"); //sample index
					pw.print(rowNames[sample]+"\t");
					try{
						pw.println(comboNames[box.getSelectedIndex()]);
					}catch(Exception e){
						pw.println("Exclude");
					}
				}
				pw.flush();
				pw.close();			
			} catch (FileNotFoundException fnfe) {
				fnfe.printStackTrace();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}
	/**
	 * Loads file based assignments
	 */
	public void onLoadAssignments() {
		/**
		 * consider the following verifcations and policies
		 *-number of loaded samples and rows in the assigment file should match, if not warning and quit
		 *-each loaded file name should match a corresponding name in the assignment file, 1:1
		 *		-if names don't match, throw warning and inform that assignments are based on loaded order
		 *		 rather than a sample name
		 *-the number of levels of factor A and factor B specified previously when defining the design
		 *should match the number of levels in the assignment file, if not warning and quit
		 *-if the level names match the level names entered then the level names will be used to make assignments
		 *if not, then there will be a warning and the level index will be used.
		 *-make sure that each level label pairs to a particular level index, this is a format 
		 *-Note that all design labels in the assignment file will override existing labels
		 *this means updating the data structures in this class, and updating AlgorithmData to set appropriate fields
		 ***AlgorithmData modification requires a fixed vocab. for parameter names to be changed
		 *these fields are (factorAName, factorBName, factorANames (level names) and factorANames (level names)
		 *Wow, that was easy :)
		 */
		
		File file;		
		JFileChooser fileChooser = new JFileChooser("./data");
		
		if(fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
		
			file = fileChooser.getSelectedFile();
			
    		try {						
    			//first grab the data and close the file
    			BufferedReader br = new BufferedReader(new FileReader(file));
    			Vector<String> data = new Vector<String>();
    			String line;
    			while( (line = br.readLine()) != null)
    				data.add(line.trim());
    			
    			br.close();
    				
    			//build structures to capture the data for assingment information and for *validation
    			
    			//factor names
    			Vector<String> groupNames = new Vector<String>();
    			
    			
    			Vector<Integer> sampleIndices = new Vector<Integer>();
    			Vector<String> sampleNames = new Vector<String>();
    			Vector<String> groupAssignments = new Vector<String>();		
    			
    			//parse the data in to these structures
    			String [] lineArray;
    			//String status = "OK";
    			for(int row = 0; row < data.size(); row++) {
    				line = (String)(data.get(row));

    				//if not a comment line, and not the header line
    				if(!(line.startsWith("#")) && !(line.startsWith("SampleIndex"))) {
    					
    					lineArray = line.split("\t");
    					
    					//check what module saved the file
    					if(lineArray[0].startsWith("Module:")) {
    						if (!lineArray[1].equals("USC")){
    							Object[] optionst = { "Continue", "Cancel" };
    							if (JOptionPane.showOptionDialog(null, 
    		    						"The saved file was saved using a different module, "+lineArray[1]+". \n Would you like MeV to try to load it anyway?", 
    		    						"File type warning", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, 
    		    						optionst, optionst[0])==0)
    								continue;
    							return;
    						}
    						continue;
    					}
    					
    					//pick up group names
    					if(lineArray[0].startsWith("Group ") && lineArray[0].endsWith("Label:")) {
    						groupNames.add(lineArray[1]);
    						continue;
    					}

    					//non-comment line, non-header line and not a group label line
    					
    					try {
    						Integer.parseInt(lineArray[0]);
    					} catch ( NumberFormatException nfe) {
    						//if not parsable continue
    						continue;
    					}
    					
    					sampleIndices.add(new Integer(lineArray[0]));
    					sampleNames.add(lineArray[1]);
    					groupAssignments.add(lineArray[2]);	
    				}				
    			}
    			
    			//we have the data parsed, now validate, assign current data


    			if( rowNames.length != sampleNames.size()) {
    				System.out.println(rowNames.length+"  "+sampleNames.size());
    				//status = "number-of-samples-mismatch";
    				System.out.println(rowNames.length+ " s length " + sampleNames.size());
    				//warn and prompt to continue but omit assignments for those not represented				

    				JOptionPane.showMessageDialog(null, "<html>Error -- number of samples designated in assignment file ("+String.valueOf(sampleNames.size())+")<br>" +
    						                                   "does not match the number of samples loaded in MeV ("+rowNames.length+").<br>" +
    						                                   	"Assignments are not set.</html>", "File Compatibility Error", JOptionPane.ERROR_MESSAGE);
    				
    				return;
    			}

				for(int i=0; i<groupNames.size(); i++){
					if(!groupNames.get(i).equals(comboNames[i])){
						Object[] optionst = { "Use Current Labels", "Use Saved Labels" };
						if (JOptionPane.showOptionDialog(null, 
								"The saved file was saved using different group labels. \n Which labels should MeV use?", 
								"Unmatched label warning", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, 
								optionst, optionst[0])==0)
							break;
						if (!groupNames.get(groupNames.size()-1).equals(TEST_CLASS_STRING))
							groupNames.add(TEST_CLASS_STRING);
						this.comboNames = new String[groupNames.size()];
						for (int j=0; j<comboNames.length; j++){
							comboNames[j] = (String)groupNames.get(j);
						}
						
						for (int j=0; j<vComboBox.size(); j++){
							JComboBox box = (JComboBox)vComboBox.get(j);
							box.removeAllItems();
							for (int k=0; k<groupNames.size(); k++){
								box.addItem((String)groupNames.get(k));
							}
						}
						mainPanel.updateUI();
						break;
					}
						
				}
    			
    			Vector<String> currSampleVector = new Vector<String>();
    			for(int i = 0; i < rowNames.length; i++)
    				currSampleVector.add((String)rowNames[i]);
    			
    			int fileSampleIndex = 0;
    			int groupIndex = 0;
    			String groupName;
    			
    			for(int sample = 0; sample < rowNames.length; sample++) {
    				boolean doIndex = false;
    				for (int i=0;i<rowNames.length; i++){
    					if (i==sample)
    						continue;
    					if (rowNames[i].equals(rowNames[sample])){
    						doIndex=true;
    					}
    				}
    				fileSampleIndex = sampleNames.indexOf(rowNames[sample]);
    				if (fileSampleIndex==-1){
    					doIndex=true;
    				}
    				if (doIndex){
    					setStateBasedOnIndex(groupAssignments,groupNames);
    					break;
    				}
    				
    				groupName = (String)(groupAssignments.get(fileSampleIndex));
    				groupIndex = groupNames.indexOf(groupName);
    				//set state
    				try{
    					JComboBox box = (JComboBox)vComboBox.elementAt(sample);
    					box.setSelectedIndex(groupIndex);
    					if (groupIndex==-1)
    						box.setSelectedIndex(box.getItemCount()-1);
    				}catch (Exception e){
    					JComboBox box = (JComboBox)vComboBox.elementAt(sample);
						box.setSelectedIndex(box.getItemCount()-1); //set to last state... excluded
    				}
    			}
    			
    		} catch (Exception e) {
    			e.printStackTrace();
    			JOptionPane.showMessageDialog(null, "<html>The file format cannot be read.</html>", "File Compatibility Error", JOptionPane.ERROR_MESSAGE);
    		}
    	}
	}
	private void setStateBasedOnIndex(Vector<String>groupAssignments,Vector<String>groupNames){
		Object[] optionst = { "Continue", "Cancel" };
		if (JOptionPane.showOptionDialog(null, 
				"The saved file was saved using a different sample annotation. \n Would you like MeV to try to load it by index order?", 
				"File type warning", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, 
				optionst, optionst[0])==1)
			return;
		
		for(int sample = 0; sample < rowNames.length; sample++) {
			try{
				JComboBox box = (JComboBox)vComboBox.elementAt(sample);
				box.setSelectedIndex(groupNames.indexOf(groupAssignments.get(sample)));
			}catch(Exception e){
				JComboBox box = (JComboBox)vComboBox.elementAt(sample);
				box.setSelectedIndex(groupNames.size()-1);//set to last state... excluded
			}
		}
	}
	
	
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
