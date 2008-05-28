/*
Copyright @ 1999-2007, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * Created on Feb 27, 2007
 */
package org.tigr.microarray.mev.cluster.gui.impl.dialogs;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.font.LineMetrics;
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
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.OverlayLayout;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.event.MouseInputAdapter;

import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;

/**
 * @author braisted
 * 
 * The TwoWaySelectionPanel is designed to take information such as factor names,
 * factor level names, and sample names and construct a dialog to help assign samples
 * to a two-way layout design.
 * 
 * The JPanel extension is composed of two main areas, a design grid on top which
 * reflects the layout of the design and a sample list panel below that shows sample
 * names and thier current factor assignments.  Sample labels are clicked and
 * draged into the design grid
 * 
 */
public class TwoWaySelectionPanel extends JPanel implements IWizardParameterPanel {

	/**
	 * Number of loaded samples
	 */	
	private int numSamples;
	/**
	 * factor A level name
	 */
	private String factorAName;
	/**
	 * factor B level name
	 */
	private String factorBName;
	/**
	 * factor A level names
	 */
	private String [] factorANames;
	/**
	 * factor B level names
	 */
	private String [] factorBNames;
	/**
	 * Sample annotation field names
	 */
	private String [] sampleFieldNames;
	/**
	 * sample annotation, each row corresponds to a particular field type (name)
	 * # cols = number of loaded samples
	 */
	private String [][] sampleAnn;
	/**
	 * design grid
	 */	
	private DesignGridPanel gridPanel;
	/**
	 * Holds the sample indices associated with each assignment in the design grid
	 */
	private Vector [][] sampleIndexAssignments;
	/**
	 * sample list panel, displays the loaded samples
	 */
	private SampleListPanel sampleListPanel;
	/**
	 * clear JPanel to catch events, show special renderings during drag events
	 */
	private GlassPanel glassPanel;
	/**
	 * Listens to mouse events for containers, usually gets events dispatched
	 * from glass panel
	 */
	private Listener listener;
	/**
	 * The currently selected sample index
	 */
	private int selectedSampleIndex;
	/**
	 * Holds the grid and list panels, global for access during event dispatch
	 */
	private JPanel selectorPanel;
	/**
	 * Pane contains the sample list panel, needs to validate on changes (e.g. column header) 
	 */
	private JScrollPane pane;
	/**
	 * Contains the main panels. **Needs to be a JPanel impl. to allow
	 * OverlayLayout on embedded component (rather than this).
	 */
	private StupidOverlayHackPanel mainPanel;
	/**
	 * AlgorithmData to recieve grouping data
	 */
	private AlgorithmData algData;
	
	public TwoWaySelectionPanel(AlgorithmData data, String [] sampleFieldNames, String [][] sampleAnn) {
		super(new GridBagLayout());
		algData = data;
		this.numSamples = sampleAnn[0].length;
		this.sampleFieldNames = sampleFieldNames;
		this.sampleAnn = sampleAnn;
		selectedSampleIndex = -1;
	}
	
	
	public TwoWaySelectionPanel(AlgorithmData data, String factorAName, String factorBName, String [] factorANames,
			String [] factorBNames, String [] sampleFieldNames, String [][] sampleAnn) {
		
		super(new GridBagLayout()); 		
		this.algData = data;
		listener = new Listener();
		//assume that sample annotation at least has one field
		numSamples = sampleAnn[0].length;

		//set the labels and sample annotation and ann field names
		this.factorAName = factorAName;
		this.factorBName = factorBName;
		this.factorANames = factorANames;
		this.factorBNames = factorBNames;
		this.sampleFieldNames = sampleFieldNames;
		this.sampleAnn = sampleAnn;
		selectedSampleIndex = -1;
		
		//panel to contain mainPanel and the glass overlay panel
		selectorPanel = new JPanel(new GridBagLayout());		
		//panel to contain grid and list panels
		mainPanel = new StupidOverlayHackPanel();		
	
		//initialize assignment vectors
		sampleIndexAssignments = new Vector[factorANames.length][factorBNames.length];		
		for(int i = 0; i < sampleIndexAssignments.length; i++)
			for(int j = 0; j < sampleIndexAssignments[i].length; j++)
				sampleIndexAssignments[i][j] = new Vector();

		//initialize the grid panel, 
		gridPanel = new DesignGridPanel();
		//create tje sample list panel
		sampleListPanel = new SampleListPanel();

		//scroll pane to hold the list pane
		pane = new JScrollPane();		
		pane.setViewportView(sampleListPanel);	
		pane.setColumnHeaderView(sampleListPanel.headerPanel);
		pane.setColumnHeaderView(sampleListPanel.getHeaderPanel());
		pane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black),"Sample List and Assignments"));
		
		//add the grid and list (in scroll pane) to the selector panel
		selectorPanel.add(gridPanel, new GridBagConstraints(0,0,1,1,1,0.5, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,5,2,5),0,0));
		selectorPanel.add(pane, new GridBagConstraints(0,1,1,1,1,0.5, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,5,5,5),0,0));

		//construct the glass overlay panel
		glassPanel = new GlassPanel();		

		//add the glasspanel and selector panel to the main panel
		//overlay but glass ends up on top... somehow
		mainPanel.add(glassPanel);
		mainPanel.add(selectorPanel);
		//set glass panel visible to allow it to function
		glassPanel.setVisible(true);		
		//add the main panel and construct and add the button panel
		add(mainPanel, new GridBagConstraints(0,0,1,1,1,1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
		add(constructButtonPanel(), new GridBagConstraints(0,1,1,1,1,0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,5,2,5),0,0));		
	}	

	/**
	 * Sets labels and constructs panels prior to display
	 * @param factorAName factor A's name
	 * @param factorBName factor B's name
	 * @param factorANames factor A label names
	 * @param factorBNames factor B label names
	 */
	public void initialize(String factorAName, String factorBName, String [] factorANames,
			String [] factorBNames) {

		listener = new Listener();

		//clear if a revisit
		removeAll();
		
		//set the labels and sample annotation and ann field names
		this.factorAName = factorAName;
		this.factorBName = factorBName;
		this.factorANames = factorANames;
		this.factorBNames = factorBNames;

		//panel to contain mainPanel and the glass overlay panel
		selectorPanel = new JPanel(new GridBagLayout());		
		//panel to contain grid and list panels
		mainPanel = new StupidOverlayHackPanel();		
	
		//initialize assignment vectors
		sampleIndexAssignments = new Vector[factorANames.length][factorBNames.length];		
		for(int i = 0; i < sampleIndexAssignments.length; i++)
			for(int j = 0; j < sampleIndexAssignments[i].length; j++)
				sampleIndexAssignments[i][j] = new Vector();

		//initialize the grid panel, 
		gridPanel = new DesignGridPanel();
		//create tje sample list panel
		sampleListPanel = new SampleListPanel();

		//scroll pane to hold the list pane
		pane = new JScrollPane();		
		pane.setViewportView(sampleListPanel);	
		pane.setColumnHeaderView(sampleListPanel.headerPanel);
		pane.setColumnHeaderView(sampleListPanel.getHeaderPanel());
		pane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black),"Sample List and Assignments"));
		
		//add the grid and list (in scroll pane) to the selector panel
		selectorPanel.add(gridPanel, new GridBagConstraints(0,0,1,1,1,0.5, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,5,2,5),0,0));
		selectorPanel.add(pane, new GridBagConstraints(0,1,1,1,1,0.5, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,5,5,5),0,0));

		//construct the glass overlay panel
		glassPanel = new GlassPanel();		

		//add the glasspanel and selector panel to the main panel
		//overlay but glass ends up on top... somehow
		mainPanel.add(glassPanel);
		mainPanel.add(selectorPanel);
		//set glass panel visible to allow it to function
		glassPanel.setVisible(true);		
		//add the main panel and construct and add the button panel
		add(mainPanel, new GridBagConstraints(0,0,1,1,1,1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
		add(constructButtonPanel(), new GridBagConstraints(0,1,1,1,1,0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,5,2,5),0,0));		
	}

	
	/**
	 * Constructs the load and save button panel
	 * @return the constructed panel
	 */
	public JPanel constructButtonPanel() {
		JPanel buttonPanel = new JPanel(new GridBagLayout());
		buttonPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "Load 'n Save Assignments"));
		Dimension buttonDim = new Dimension(200, 30);
		JButton loadButton = new JButton("Load Design Assignments");
		loadButton.setToolTipText("<html><center>Loads saved design file.<br>Requires consistent design grid dimensions.</center><html>");
		loadButton.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		loadButton.setFocusPainted(false);
		loadButton.setActionCommand("load-design-command");
		loadButton.setPreferredSize(buttonDim);
		loadButton.setSize(buttonDim);
		loadButton.addActionListener(listener);
		
		JButton saveButton = new JButton("Save Design Assignments");
		saveButton.setToolTipText("<html><center>Saves design assignments to file.<html>");
		saveButton.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		saveButton.setFocusPainted(false);
		saveButton.setActionCommand("save-design-command");
		saveButton.setPreferredSize(buttonDim);
		saveButton.setSize(buttonDim);
		saveButton.addActionListener(listener);
		
		buttonPanel.add(loadButton, new GridBagConstraints(0,0, 1, 1,0.5, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,20,5,10),1,1));
		buttonPanel.add(saveButton, new GridBagConstraints(1,0, 1, 1,0.5, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,10,5,20),1,1));
						
		//set an approximate size, height of buttons is most important aesthetic
		//buttonPanel.setPreferredSize(new Dimension(2*buttonDim.width+100, buttonDim.height+100));
		
		return buttonPanel;
	}
	
	/**
	 * Updates the size of the panel, needs to be aware of screen dims
	 */
	public void updateSize() {

		int w = gridPanel.getWidth();
		int gridH = gridPanel.getHeight();

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

		int maxHeight = (int)(screenSize.height * 0.65f);
		int maxWidth = (int)(screenSize.width * 0.6f);

		int minHeight = (int)(screenSize.height * 0.5f);
		int minWidth = (int)(screenSize.width * 0.6f);
		
		w = Math.max(Math.min(Math.max(w, sampleListPanel.getPreferredSize().width), maxWidth), minWidth);
		int h = Math.max(Math.min(gridH+sampleListPanel.getPreferredSize().height+sampleListPanel.getHeaderPanel().getHeight(), maxHeight), minHeight);
		
	
		mainPanel.setSize(w,h);
		mainPanel.setPreferredSize(new Dimension(w,h));
		pane.validate();
	}
	
	/**
	 * Removes an assignment for a particular sample index
	 * @param sampleIndex sample index
	 */
	private void removeAssignment(int sampleIndex) {
		boolean removed = false;
		Integer sampleInt = new Integer(sampleIndex);
		for(int i = 0; i < sampleIndexAssignments.length;i++) {
			for(int j = 0; j < sampleIndexAssignments[i].length; j++) {
				if(sampleIndexAssignments[i][j].remove(sampleInt))
					break;  //break when you find and remove
			}
		}
		//repaint grid
		gridPanel.repaint();
	}
	
	/**
	 * Returns the assignments in a 2D array (factor A in rows, factor B in cols)
	 * @return assignments
	 */
	public Vector [][] getAssignments() {
		return sampleIndexAssignments;
	}
	
	/**
	 * Saves the assignments to file.
	 * 
	 * Comments include title, user, save date
	 * Design information includes factor a and b labels and the level names for each factor
	 * A header row is followed by sample index, sample name (primary, field index = 0),
	 * them factor A assignment (text label) then factor B assignment (text label)
	 */
	private void saveAssignments() {
		
		File file;		
		JFileChooser fileChooser = new JFileChooser("./data");	
		
		if(fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			file = fileChooser.getSelectedFile();			
			try {
				PrintWriter pw = new PrintWriter(new FileWriter(file));
				
				//comment row
				Date currDate = new Date(System.currentTimeMillis());			
				String dateString = currDate.toString();;
				String userName = System.getProperty("user.name");
				
				pw.println("# Two Factor Assignment File");
				pw.println("# User: "+userName+" Save Date: "+dateString);
				pw.println("#");
				
				pw.println("Factor A Label:\t"+factorAName);
				pw.print("Factor A Levels:");
				for(int i = 0; i < factorANames.length; i++)
					pw.print("\t"+factorANames[i]);
				pw.println("");
				
				pw.println("Factor B Label:"+"\t"+factorBName);
				pw.print("Factor B Levels:");
				for(int i = 0; i < factorBNames.length; i++)
					pw.print("\t"+factorBNames[i]);
				pw.println("");
				pw.println("#");
				
				//header row
				pw.println("SampleIndex\tPrimarySampleName\t"+factorAName+"\t"+factorBName);
				
				int [] assignments;
				for(int sample = 0; sample < numSamples; sample++) {
					pw.print(String.valueOf(sample+1)+"\t"); //sample index
					
					pw.print(sampleAnn[0][sample]+"\t");     //primary sample name
					
					assignments = getGroupAssignments(sample);
					
					if(assignments[0] == -1)
						pw.println("Exclude\tExclude");
					else {
						//have an assignment
						pw.print(factorANames[assignments[0]]+"\t"); //factor A level name			
						pw.println(factorBNames[assignments[1]]+"\t"); //factor B level name
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
	private void loadAssignments() {
		//consider the following verifcations and policies
		//-number of loaded samples and rows in the assigment file should match, if not warning and quit
		//-each loaded file name should match a corresponding name in the assignment file, 1:1
		//		-if names don't match, throw warning and inform that assignments are based on loaded order
		//		 rather than a sample name
		//-the number of levels of factor A and factor B specified previously when defining the design
		// should match the number of levels in the assignment file, if not warning and quit
		//-if the level names match the level names entered then the level names will be used to make assignments
		// if not, then there will be a warning and the level index will be used.
		//-make sure that each level label pairs to a particular level index, this is a format 
		//-Note that all design labels in the assignment file will override existing labels
		// this means updating the data structures in this class, and updating AlgorithmData to set appropriate fields
		// **AlgorithmData modification requires a fixed vocab. for parameter names to be changed
		// these fields are (factorAName, factorBName, factorANames (level names) and factorANames (level names)
		//Wow, that was easy :)
		
		File file;		
		JFileChooser fileChooser = new JFileChooser("./data");
		
		if(fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
		
			file = fileChooser.getSelectedFile();
			
		try {						
			//first grab the data and close the file
			BufferedReader br = new BufferedReader(new FileReader(file));
			Vector data = new Vector();
			String line;
			while( (line = br.readLine()) != null)
				data.add(line.trim());
			br.close();
				
			//build structures to capture the data for assingment information and for *validation
			
			//factor names
			String newFactorAName ="";
			String newFactorBName ="";
			
			String [] newFactorANames = null;			
			String [] newFactorBNames = null;
			//these vectors allows us to get 'indexOf(label)' for assignment grid
			Vector factorALevelNames = new Vector();
			Vector factorBLevelNames = new Vector();		
			//these vectors hold the file assignments
			Vector factorALevelAssign = new Vector();			
			Vector factorBLevelAssign = new Vector();
			
			//full vector of sample indices and primary names
			//names or possibly indices will map data to assignmens
			Vector newSampleIndices = new Vector();
			Vector samplePrimaryNames = new Vector();
						
			//parse the data in to these structures
			String [] lineArray;
			String status = "OK";
			int index;
			boolean validData = true;
			for(int row = 0; row < data.size(); row++) {
				line = (String)(data.get(row));

				//if not a comment line, and not the header line
				if(!(line.startsWith("#")) && !(line.startsWith("SampleIndex"))) {
					
					lineArray = line.split("\t");
					
					//Design Information
					if(line.startsWith("Factor")) {
						Vector levelVector;
						
						if(line.startsWith("Factor A Label")) {
							 newFactorAName = lineArray[1].trim();
						} else if(line.startsWith("Factor B Label")) {
							 newFactorBName = lineArray[1].trim();							
						} else if(line.startsWith("Factor A Levels")) {
							newFactorANames = new String[lineArray.length-1];
							for(int level = 1; level < lineArray.length; level++) {
								newFactorANames[level-1] = lineArray[level].trim();
								factorALevelNames.add(lineArray[level].trim());
							}
						} else if(line.startsWith("Factor B Levels")) {
							newFactorBNames = new String[lineArray.length-1];
							for(int level = 1; level < lineArray.length; level++) {
								newFactorBNames[level-1] = lineArray[level].trim();
								factorBLevelNames.add(lineArray[level].trim());
							}							
						}						
						continue;
					}
					
					//verify length
					if(lineArray.length != 4) {
						validData = false;
						status = "invalid-line-length";
						System.out.println("incorr. line length");
						break;
					}
					

										
					//grab the sample index, column 0
					try {
						index = Integer.parseInt(lineArray[0])-1;
						newSampleIndices.add(new Integer(index));
					} catch (NumberFormatException nfe) {
						System.out.println("ERROR: Sample index value (column 1 in the file) is not an integer.");
						return;
					}
					
					//grab the primary name, column 1
					samplePrimaryNames.add(lineArray[1].trim());
										

					//factor A
					factorALevelAssign.add(lineArray[2].trim());

					//factor B
					factorBLevelAssign.add(lineArray[3].trim());
				}				
			}
			
			//we have the data parsed, now validate, assign current data

			//get the appearant dimensions for the array
			int numFactorALevels = newFactorANames.length;
			int numFactorBLevels = newFactorBNames.length;

			//new 2d array of assignments
			Vector [][] newAssignments = new Vector[numFactorALevels][numFactorBLevels];
			for(int i = 0; i < numFactorALevels; i++)
				for(int j = 0; j < numFactorBLevels; j++)
					newAssignments[i][j] = new Vector();

			//unassigned samples
			Vector unassignedSamples = new Vector();
			
			
			
			//verify design dimenstions
			if(numFactorALevels != sampleIndexAssignments.length
					|| numFactorBLevels != sampleIndexAssignments[0].length) {
				status = "design-dimension-mismatch";
				//warn and quit... or posibly swap dimensions... argh...
				System.out.println("design dim. error");
				return;				
			}
			
			//verify number of samples
			if(numSamples != newSampleIndices.size()) {
				status = "number-of-samples-mismatch";
			
				
				//warn and prompt to continue but omit assignments for those not represented
				
			}
			
			//now for the actual assignments
			
			//maybe validate that each row has a unique sample name
			//make a vector of primary sampleNames
			Vector loadedSampleNames = new Vector();
			for(int sample = 0; sample < numSamples; sample++) {
				if(!loadedSampleNames.contains(sampleAnn[0][sample]))
					loadedSampleNames.add(sampleAnn[0][sample]);
			}

			//have a unique list of primary sample names, loaded
			//check if names are unique
			boolean uniqueNames = true;;
			if(numSamples > loadedSampleNames.size()) {
				uniqueNames = false;
				//warn of non-unique names, indicate that assignments will be
				//based only on those samples represented
			}
			
			//check for coverage, how many of the loaded samples have file assignment rows.
			int numSamplesWithAssignments = 0;
			for(int sample = 0; sample < numSamples; sample++) {
				if(samplePrimaryNames.contains(sampleAnn[0][sample]))
					numSamplesWithAssignments++;				
			}

			boolean isCovered = true;
			if(numSamplesWithAssignments < numSamples) {
				isCovered = false;
				//warn that some samples will not have file based assignments but will be excluded
			}
			
			//NOW assign to groups

			int fileRowIndex, factorAIndex, factorBIndex, sampleIndex;
			String levelAName, levelBName;
			String sampleName;

			for(int sample = 0; sample < numSamples; sample++) {
				sampleName = sampleAnn[0][sample];

				fileRowIndex = samplePrimaryNames.indexOf(sampleName);
				
				//check if the current loaded sample has an assignment
				if(fileRowIndex != -1) {
					factorAIndex = factorALevelNames.indexOf(factorALevelAssign.get(fileRowIndex));
					factorBIndex = factorBLevelNames.indexOf(factorBLevelAssign.get(fileRowIndex));
					
					//make sure it is not to be excluded
					if(!((String)(factorALevelAssign.get(fileRowIndex))).equals("Exclude")) {					
						newAssignments[factorAIndex][factorBIndex].add(new Integer(sample));
					}
				} else {
					//no assignment for that sample
			
				}
			}
			
			//update index assignments
			this.sampleIndexAssignments = newAssignments;
			//factor A names
			this.factorANames = newFactorANames;
			//update factor B names
			this.factorBNames = newFactorBNames;
			
			//set factor a and factor b names
			this.factorAName = newFactorAName;
			this.factorBName = newFactorBName;
			
			//update the sample list panel
			sampleListPanel.refreshFactorLevelAssignments();
			
			//set boolean to have grid panel determine sizes of lables etc.
			gridPanel.areMinDimsSet = false;
			
			repaint();			
			//need to clear assignments, clear assignment booleans in sample list and re-init
			//maybe a specialized inti for the sample list panel.
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	}
	
	/**
	 * Gets group assignments for a particular sample, returns factor level indices
	 * possibly -1 if no assignment has been made
	 * @param sampleIndex
	 * @return factorA index and factorB index in array pos. 0 and 1
	 */
	private int [] getGroupAssignments(int sampleIndex) {
		int [] factorAandBAssignments = new int[2];
		
		Integer sampleInt = new Integer(sampleIndex);
		
		int factorAIndex, factorBIndex = 0;
		int a =-1 , b = -1;
		boolean foundIt = false;
		for(factorAIndex = 0; factorAIndex < sampleIndexAssignments.length; factorAIndex++) {
			for(factorBIndex = 0; factorBIndex < sampleIndexAssignments[factorAIndex].length; factorBIndex++) {
				if(sampleIndexAssignments[factorAIndex][factorBIndex].contains(sampleInt)) {
					foundIt = true;
					a = factorAIndex;
					b = factorBIndex;
					break;
				}
			}			
		}
		
		if(foundIt) {
			factorAandBAssignments[0] = a;
			factorAandBAssignments[1] = b;
		} else {
			factorAandBAssignments[0] = -1;
			factorAandBAssignments[1] = -1;
		}					
		return factorAandBAssignments;
	}
	
	
	/*test*/
	public static void main(String[] args) {
		String factorAName = "Strain";
		String factorBName = "Time";
		String [] factorANames = {"Wildtype", "Mutant X"};
		String [] factorBNames = {"10 min.", "20 min.", "30 min.", "40 min."};
		String [] sampleFieldNames = {"Default Slide Name", "Strain Name", "Exposure Time"};
		String [][] sampleAnn = new String [3][16];
		
		for(int i = 0; i < sampleAnn.length; i++) {
			for(int j = 0; j < sampleAnn[i].length; j++) {
				if(i == 0)
					sampleAnn[i][j] = "SamplexxxxxxxxFile "+j;
				else if(i == 1) {
					if(j<8)
						sampleAnn[i][j] = "Strain A";
					else
						sampleAnn[i][j] = "Strain B";						
				
				} else {
					if(j>11)
						sampleAnn[i][j] = "T = 40";						
					else if(j>7)
						sampleAnn[i][j] = "T = 30";						
					else if(j>3)
						sampleAnn[i][j] = "T = 20";
					else
						sampleAnn[i][j] = "T = 10";												
				}
			}
		}
		
		TwoWaySelectionPanel twsp = new TwoWaySelectionPanel(new AlgorithmData(),
		factorAName,factorBName,factorANames,
		factorBNames, sampleFieldNames, sampleAnn);
		twsp.updateSize();
		JFrame frame = new JFrame();
	
		frame.getContentPane().add(twsp);
		
		frame.pack();

		frame.setVisible(true);
		//frame.validate();
	}

	/**
	 * This class extends JPanel and displays sample index, name(s), and
	 * the assignments.  Supports click and drag options by determining
	 * and setting selected sample
	 * 
	 * @author braisted
	 */
	public class SampleListPanel extends JPanel {

		/**
		 * Holds the annotation feild indices to display
		 */
		private Vector annFieldVector;
		/**
		 * Holds JLabels for factor A assignments, updated during assignment
		 */
		private Vector factorALabelVector;
		/**
		 * Holds JLabels for factor B assignments, updated during assignment
		 */
		private Vector factorBLabelVector;
		/**
		 * Holds sample name labels, helps determine index based on responding
		 * label's index in the vector
		 */
		private Vector sampleLabelVector;
		/**
		 * holds reset buttons, list maintained so that state can be updated
		 * easily during assignment
		 */
		private Vector resetButtonVector;
		/**
		 * Header panel, need access to set into ScrollPane on updates
		 */
		private JPanel headerPanel;
		/**
		 * Holds vector to check if a sample is assigned, helps for updates
		 * and rendering changes
		 */
		boolean [] isAssigned;
		
		/**
		 * Constructs a new SampleListPanel
		 */
		public SampleListPanel() {
			super(new GridBagLayout());

			isAssigned = new boolean[numSamples];			
			addMouseListener(listener);
			addMouseMotionListener(listener);
			
			//annfield index to display vector, init to first index
			annFieldVector = new Vector();
			annFieldVector.add(new Integer(0));
			
			factorALabelVector = new Vector(numSamples);
			factorBLabelVector = new Vector(numSamples);
			
			//handle the layou
			initialize();
		}
		
		/**
		 * This handles the layout and can be called during construction
		 * or during refresh of information or after additional sample ann feilds
		 * have been added.
		 */
		private void initialize() {
			removeAll();

			Dimension resetButtonDim = new Dimension(60, 25);
			
			resetButtonVector = new Vector();	
			
			sampleLabelVector = new Vector();
			
			boolean haveMultipleAnnFields = (sampleAnn.length > 1);
			int numAnnFields = annFieldVector.size();
			int numCols = numAnnFields + 4;
			
			int currRow = 0;
			int currCol = 0;
			
			Insets labelInsets = new Insets(2,5,2,5);
						
			JLabel indexLabel;
			JButton resetButton;
			JLabel sampleLabel;			
			JLabel factorALabel;
			JLabel factorBLabel;
			int currSampleFieldIndex;
			
			Toolkit toolkit = Toolkit.getDefaultToolkit();
			Cursor handCursor = new Cursor(Cursor.HAND_CURSOR);
			JLabel lastIndexLabel = null;
			
			for(int sample = 0; sample < numSamples; sample++) {
				currCol = 0;
				indexLabel = new JLabel(String.valueOf(sample+1));
				lastIndexLabel = indexLabel;
				//add index label
				add(indexLabel, new GridBagConstraints(currCol, currRow, 1, 1, 0, 1, GridBagConstraints.CENTER, GridBagConstraints.VERTICAL, labelInsets, 1,1));				
				currCol++;
				
				resetButton = new JButton("Reset");
				resetButton.setEnabled(isAssigned[sample]);
				resetButton.setFocusPainted(false);
				//resetButton.setToolTipText("Resets selections to Exclude");
				resetButton.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
				resetButton.setPreferredSize(resetButtonDim);
				resetButton.setSize(resetButtonDim);
				resetButton.setActionCommand("reset-command");
				resetButton.addActionListener(listener);
				resetButton.addMouseListener(listener);
				
				//add clear button
				currCol++;
				add(resetButton, new GridBagConstraints(currCol, currRow, 1, 1, 0, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, labelInsets, 1,1));
				resetButtonVector.add(resetButton);
				currCol++;
				
				//make and add sample annotation labels
				int borderWidth = 3;
				for(int fieldIndex = 0; fieldIndex < numAnnFields; fieldIndex++) {
					sampleLabel = new JLabel(sampleAnn[((Integer)annFieldVector.get(fieldIndex)).intValue()][sample]);
					sampleLabel.setHorizontalAlignment(JLabel.CENTER);
					
					borderWidth = 1;
					if(fieldIndex == 0) {
						if(!isAssigned[sample])
							borderWidth = 3;
						//sampleLabel.setToolTipText("Click and drag to design grid above.");
						sampleLabel.setFocusable(true);
						sampleLabel.addMouseListener(listener);
						sampleLabel.addMouseMotionListener(listener);						
						sampleLabelVector.add(sampleLabel);
						sampleLabel.setCursor(handCursor);
					}
					
					sampleLabel.setBorder(BorderFactory.createLineBorder(Color.black, borderWidth));

					//add sample label
					add(sampleLabel, new GridBagConstraints(currCol, currRow, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, labelInsets, 1,1));				
					currCol++;
				}
				
				if(!isAssigned[sample])
					factorALabel = new JLabel("Exclude");
				else
					factorALabel = (JLabel)(factorALabelVector.get(sample));
				
				factorALabel.setBorder(BorderFactory.createLineBorder(Color.black, 1));
				factorALabel.setHorizontalAlignment(JLabel.CENTER);
				if(factorALabelVector.size() < numSamples)
					factorALabelVector.add(factorALabel);
				else
					factorALabelVector.set(sample, factorALabel);
				//add factor A label
				add(factorALabel, new GridBagConstraints(currCol, currRow, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, labelInsets, 1,1));				
				currCol++;
				

				if(!isAssigned[sample])
					factorBLabel = new JLabel("Exclude");
				else
					factorBLabel = (JLabel)(factorBLabelVector.get(sample));
					
				factorBLabel.setBorder(BorderFactory.createLineBorder(Color.black, 1));
				factorBLabel.setHorizontalAlignment(JLabel.CENTER);
				if(factorBLabelVector.size() < numSamples)
					factorBLabelVector.add(factorBLabel);
				else
					factorBLabelVector.set(sample, factorBLabel);
				
				//add factor B label
				add(factorBLabel, new GridBagConstraints(currCol, currRow, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, labelInsets, 1,1));								
				currCol++;
				
				currRow++;
			}			
			headerPanel = createHeaderPanel(numCols, lastIndexLabel.getPreferredSize(), resetButtonDim);			
		}
		
		
		/**
		 * This updates the factor assignments, used after file load
		 */
		public void refreshFactorLevelAssignments() {
			
			int [] factorAandBAssignments;
			JLabel label;
			
			for(int sample = 0; sample < factorALabelVector.size(); sample++) {
				//get assignments
				factorAandBAssignments  = getGroupAssignments(sample);			
				
				if(factorAandBAssignments[0] != -1) {
					label = (JLabel)(factorALabelVector.get(sample));
					label.setText(factorANames[factorAandBAssignments[0]]);
					
					label = (JLabel)(factorBLabelVector.get(sample));
					label.setText(factorBNames[factorAandBAssignments[1]]);
					
					label = (JLabel)(sampleLabelVector.get(sample));
					label.setBorder(BorderFactory.createLineBorder(Color.black, 1));
					
					isAssigned[sample] = true;
					
					((JButton)(resetButtonVector.get(sample))).setEnabled(true);
					
				} else {
					label = (JLabel)(factorALabelVector.get(sample));
					label.setText("exclude");

					label = (JLabel)(factorBLabelVector.get(sample));
					label.setText("exclude");
		
					label = (JLabel)(sampleLabelVector.get(sample));
					label.setBorder(BorderFactory.createLineBorder(Color.black, 3));

					isAssigned[sample] = false;					
					
					((JButton)(resetButtonVector.get(sample))).setEnabled(false);				
					
				}				
			}
		}
		
		
		/**
		 * Constructs the header panel
		 * @param numCols number of list columns
		 * @param indexDim size to render the index labels
		 * @param resetDim size to render the area over the reset buttons
		 * @return header panel
		 */
		private JPanel createHeaderPanel(int numCols, Dimension indexDim, Dimension resetDim) {
			JPanel panel = new JPanel(new GridBagLayout());
			JPanel fillPanel;
			int currY = 0;
			int currAnnFieldIndex = 0;
			
			Insets labelInsets = new Insets(2,5,2,5);
			
			JButton sampleAnnButton = new JButton("  Select Sample Annotation Fields  ");
			sampleAnnButton.setPreferredSize(new Dimension(200, 30));
			sampleAnnButton.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
			sampleAnnButton.setFocusPainted(false);
			sampleAnnButton.setActionCommand("select-sample-ann-command");
			sampleAnnButton.addActionListener(listener);
			sampleAnnButton.addMouseListener(listener);
			sampleAnnButton.setEnabled(sampleFieldNames.length>1);
			
			//add the button
			panel.add(sampleAnnButton, new GridBagConstraints(0, currY, numCols, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.VERTICAL, new Insets(5,10,5,1), 1,1));
			
			currY++;
			
			for(int i = 0; i < numCols; i++) {
				if(i == 0) {
					fillPanel = new JPanel();			
					Dimension dim = getComponent(i).getPreferredSize();
					dim.width += 10;
					dim.height += 4;
					//fillPanel.setBorder(BorderFactory.createLineBorder(Color.black));
					fillPanel.setPreferredSize(dim);
					fillPanel.setSize(dim);	
					panel.add(fillPanel, new GridBagConstraints(i, currY, 1, 1, 0, 1, GridBagConstraints.CENTER, GridBagConstraints.VERTICAL, labelInsets, 1,1));
				} else if(i == 1) {
					fillPanel = new JPanel();						
					//fillPanel.setBorder(BorderFactory.createLineBorder(Color.black));
					fillPanel.setPreferredSize(resetDim);
					fillPanel.setSize(resetDim);						
					panel.add(fillPanel, new GridBagConstraints(i, currY, 1, 1, 0, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, labelInsets, 1,1));						
				} else {
					String labelText;
					
					if(i == numCols-2)
						labelText = factorAName;
					else if(i == numCols-1)
						labelText = factorBName;
					else {
						//annotation field header
						labelText = sampleFieldNames[((Integer)(annFieldVector.get(currAnnFieldIndex))).intValue()];
						currAnnFieldIndex++;
					}
					
					labelText = "<html><b><u>"+labelText+"</html>";
			
					JLabel label = new JLabel(labelText);
					//label.setBorder(BorderFactory.createLineBorder(Color.black));
					
					label.setFont(new Font("Arial", Font.BOLD, 16));
					label.setHorizontalAlignment(JLabel.CENTER);		
					label.setPreferredSize(getComponent(i).getPreferredSize());
					label.setSize(getComponent(i).getPreferredSize());
					panel.add(label, new GridBagConstraints(i, currY, 1, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.BOTH, labelInsets, 1,1));
				}
			}			
			return panel;
		}
		
		
		/**
		 * returns the header panel
		 * @return header panel
		 */
		public JPanel getHeaderPanel() {
			return headerPanel;
		}
		
		/**
		 * Updates an assignment.
		 * @param sampleNumber sample index
		 * @param factorAIndex factor A level index
		 * @param factorBIndex factor B level index
		 */
		private void updateAssignment(int sampleNumber, int factorAIndex, int factorBIndex) {
			((JLabel)(factorALabelVector.get(sampleNumber))).setText(factorANames[factorAIndex]);
			((JLabel)(factorBLabelVector.get(sampleNumber))).setText(factorBNames[factorBIndex]);
			((JLabel)(sampleLabelVector.get(sampleNumber))).setBorder(BorderFactory.createLineBorder(Color.gray,1));			
			isAssigned[sampleNumber] = true;
			((JButton)(resetButtonVector.get(sampleNumber))).setEnabled(true);						
			validate();
			repaint();
		}
		
		/**
		 * Reset a selection based on the reset button source index
		 * @param source event source
		 */
		private void resetSelection(Object source) {
			int sampleIndex = resetButtonVector.indexOf(source);
			((JLabel)(factorALabelVector.get(sampleIndex))).setText("Exclude");
			((JLabel)(factorBLabelVector.get(sampleIndex))).setText("Exclude");
			((JLabel)(sampleLabelVector.get(sampleIndex))).setBorder(BorderFactory.createLineBorder(Color.black,3));
			isAssigned[sampleIndex] = false;
			((JButton)(resetButtonVector.get(sampleIndex))).setEnabled(false);			
			validate();
			repaint();						
			//remove from vector and repaint
			removeAssignment(sampleIndex);
		}
		
		/**
		 * Resets a selection given a sample index
		 * @param sampleIndex index
		 */
		private void resetSelection(int sampleIndex) {
			((JLabel)(factorALabelVector.get(sampleIndex))).setText("Exclude");
			((JLabel)(factorBLabelVector.get(sampleIndex))).setText("Exclude");
			((JLabel)(sampleLabelVector.get(sampleIndex))).setBorder(BorderFactory.createLineBorder(Color.black,3));
			validate();
			repaint();						
			//remove from vector and repaint
			removeAssignment(sampleIndex);
		}

		/**
		 * Returns the selected sample's name, for glass panel rendering
		 * @param index sample index
		 * @return name
		 */
		private String getSelectedSampleName(int index) {
			return ("["+((JLabel)(sampleLabelVector.get(index))).getText()+"]");
		}


		/**
		 * Modifies displayed annotation in the list panel
		 */
		private void modifyDisplayedSampleAnnotation() {
			AnnotationFieldSelectionDialog dialog = new AnnotationFieldSelectionDialog(new JFrame(), sampleFieldNames);
			if(dialog.showModal() == JOptionPane.OK_OPTION) {
				annFieldVector.clear();			
				//add the default sample names
				annFieldVector.add(new Integer(0));
				int [] selectedIndices = dialog.getSelectedFieldIndices();
				for(int i = 0; i < selectedIndices.length; i++) {
					if(selectedIndices[i] > 0)
						annFieldVector.add(new Integer(selectedIndices[i]));
				}			
				initialize();
				pane.setColumnHeaderView(headerPanel);
				validate();
			}
		}

		/**
		 * processes mouse click
		 * @param mousePoint mouse position
		 */
		private void handleMouseClick(Point mousePoint) {
			Point myPoint = getLocation();			
			Object comp = this.getComponentAt(mousePoint.x, mousePoint.y - myPoint.y-gridPanel.getHeight()-headerPanel.getHeight());
			int resetButtonIndex = resetButtonVector.indexOf(comp);			
			if(comp != null ) {
				if(resetButtonIndex != -1) {
					resetSelection(resetButtonIndex);
				}		
			}		
		}

		/**
		 * processes mouse click given source
		 * @param comp
		 */
		private void handleMouseClick(Object comp) {
			Point myPoint = getLocation();
			int resetButtonIndex = resetButtonVector.indexOf(comp);			
			if(comp != null ) {
				if(resetButtonIndex != -1) {
					resetSelection(resetButtonIndex);
				} 
				this.grabFocus();
			}			
		}		
		
		/**
		 * Processes mouse press events
		 * @param mousePoint
		 */
		private void handleMousePress(Point mousePoint) {
			Point myPoint = getLocation();
			Object comp = this.getComponentAt(mousePoint.x, mousePoint.y - myPoint.y-gridPanel.getHeight()-headerPanel.getHeight());
			int labelIndex = sampleLabelVector.indexOf(comp);
			
			if(comp != null ) {
				//ok to set to -1 if not a label
				selectedSampleIndex = labelIndex;				
			}
		}

		/**
		 * Processes mouse press events
		 * @param mousePoint
		 */
		public void handleMousePress(Object comp) {
			int labelIndex = sampleLabelVector.indexOf(comp);			
			if(comp != null ) {
				//ok to set to -1 if not a label
				selectedSampleIndex = labelIndex;				
			}
		}
		
		/**
		 * Processes mouseove events
		 * @param mousePoint
		 */
		public void handleMouseOver(Object comp) {
			Point myPoint = getLocation();
			int labelIndex = sampleLabelVector.indexOf(comp);
			
			if(labelIndex != -1 ) {				
				glassPanel.setMouseOverMode(true);
			} else {				
				glassPanel.setMouseOverMode(false);									
			}		
		}

		/**
		 * Updates displayed annotation
		 */
		public void onAnnotationSelection() {			
			AnnotationFieldSelectionDialog dialog = new AnnotationFieldSelectionDialog(new JFrame(), sampleFieldNames);
			if(dialog.showModal() == JOptionPane.OK_OPTION) {
				annFieldVector.clear();			
				//add the default sample names
				annFieldVector.add(new Integer(0));
				int [] selectedIndices = dialog.getSelectedFieldIndices();
				for(int i = 0; i < selectedIndices.length; i++) {
					if(selectedIndices[i] > 0)
						annFieldVector.add(new Integer(selectedIndices[i]));
				}			
				initialize();
				pane.setColumnHeaderView(headerPanel);
			}
			this.grabFocus();
		}
	}
	
	/**
	 * 
	 * DesignGridPanel displays the experimental design and is the drag
	 * destination during selection
	 * 
	 * @author braisted
	 */
	public class DesignGridPanel extends JPanel {

		boolean areMinDimsSet = false;		

		private int PRE_FACTOR_A_LABEL_WIDTH = 15;
		private int PRE_FACTOR_B_LABEL_HEIGHT = 15;
		private int PRE_LABEL_A_WIDTH = 15;
		private int POST_LABEL_A_WIDTH = 15;		
		private int PRE_LABEL_B_HEIGHT = 15;
		private int POST_LABEL_B_HEIGHT = 15;
		private int RIGHT_MARGIN = 25;
		private int BOTTOM_MARGIN = 25;
		private int MIN_LABEL_A_YPAD = 10;
		private int MIN_LABEL_B_XPAD = 10;

		//these will vary
		int factorALabelWidth;
		int factorALabelHeight;
		int factorBLabelWidth;
		int factorBLabelHeight;

		FontMetrics fm;
		
		//rendering locations
		int currX, currY, xStep, yStep;
		int rectX, rectY, rectW, rectH;
		//encloses grid
		Rectangle mainRect;
			
		//rendering guids to show mouse over cell
		private int selectedRow;
		private int selectedCol;
	
		//more rendering tips
		private boolean isOnRect = false;
		private boolean isDragging = false;
		private boolean haveSample = false;
		private int cursorX, cursorY;
		
		//two basic fonts
		private Font plainFont;
		private Font boldFont;
		
		//set and draw on graphics in each cell
		private String sampleCountStr;
				
		/**
		 * Constructs a new grid panel, relies on global variables
		 * for labels and design dimensions
		 */
		public DesignGridPanel() {
			super();
			//setBackground(Color.white);
			setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "Experimental Design Grid"));			
			plainFont = new Font("Arial", Font.PLAIN, 14);
			boldFont = new Font("Arial", Font.BOLD, 16);
			selectedRow = -1;
			selectedCol = -1;
			addMouseListener(listener);
			addMouseMotionListener(listener);
		}
		
		
		/**
		 * Paints the grid component
		 */
		public void paint(Graphics g) {
			super.paint(g);
	
			//on first paint set the minimum dimensions
			if(!areMinDimsSet) {
				g.setFont(plainFont);
				setMinDims(g);
				areMinDimsSet = true;
			}
			
			currY = fm.getHeight()+PRE_FACTOR_B_LABEL_HEIGHT;
			currX = this.PRE_FACTOR_A_LABEL_WIDTH + this.PRE_LABEL_A_WIDTH + POST_LABEL_A_WIDTH + this.factorALabelWidth;
			
			g.setFont(boldFont);
			
			//factor B String
			g.drawString(factorBName, currX + (getWidth()-currX-RIGHT_MARGIN)/2-fm.stringWidth(factorBName)/2, currY);

			//factor A String
			//think rotated
			currX = fm.getHeight()/2+fm.stringWidth(factorAName)/2;			
			Graphics2D g2 = (Graphics2D)g;			
			//rotate, draw, rotate back
			g2.rotate(-Math.PI/2);//, currX, fm.getHeight()+PRE_FACTOR_A_LABEL_WIDTH);
			FontMetrics fm2 = g2.getFontMetrics();
			g.drawString(factorAName, -fm2.stringWidth(factorAName)/2-(getHeight()+this.PRE_FACTOR_B_LABEL_HEIGHT+this.factorBLabelHeight+this.PRE_LABEL_B_HEIGHT+this.POST_LABEL_B_HEIGHT)/2, fm.getHeight()+PRE_FACTOR_A_LABEL_WIDTH);						
			//set curr X to the normal orientation
			g2.rotate(Math.PI/2);//, currX, fm.getHeight()+PRE_FACTOR_A_LABEL_WIDTH);
			currX = fm.getHeight()+PRE_FACTOR_A_LABEL_WIDTH;
						
			//now factor A labels
			currX += PRE_LABEL_A_WIDTH;
			currY += POST_LABEL_B_HEIGHT;
			
			g.setFont(plainFont);
			g2.setStroke(new BasicStroke(2));
			
			currY = PRE_FACTOR_B_LABEL_HEIGHT + fm.getHeight() + PRE_LABEL_B_HEIGHT + POST_LABEL_B_HEIGHT + this.factorBLabelHeight;
			yStep = (getHeight() - currY - BOTTOM_MARGIN)/factorANames.length - this.factorALabelHeight;

			//currY = PRE_FACTOR_B_LABEL_HEIGHT + fm.getHeight() + PRE_LABEL_B_HEIGHT + this.factorBLabelHeight;			
			currY += yStep/2;
			for(int i = 0; i < factorANames.length; i++) {
				currY += this.factorALabelHeight;
				if(i != selectedRow)
					g.setFont(plainFont);
				else
					g.setFont(boldFont);
				g.drawString(factorANames[i], currX, currY);
				currY += yStep;
			}
			
			//now factor B labels
			currY = PRE_FACTOR_B_LABEL_HEIGHT + fm.getHeight() + PRE_LABEL_B_HEIGHT + this.factorBLabelHeight;
			
			currX += this.factorALabelWidth + this.POST_LABEL_A_WIDTH; 
			
			int cornerX = currX;
			
			xStep = (getWidth()-currX-RIGHT_MARGIN)/factorBNames.length - this.factorBLabelWidth;
			currX += xStep/2;
			for(int i = 0; i < factorBNames.length; i++) {
				if(i != selectedCol)
					g.setFont(plainFont);
				else
					g.setFont(boldFont);				
				g.drawString(factorBNames[i], currX, currY);
				currX += xStep + this.factorBLabelWidth;
			}
			
			currX = cornerX;
			currY += POST_LABEL_B_HEIGHT;
			
			rectX = currX;
			rectY = currY;			
			rectW = getWidth()-currX-RIGHT_MARGIN;
			rectH =	getHeight() - currY - BOTTOM_MARGIN;
			mainRect = new Rectangle(rectX, rectY, rectW, rectH);
			g.drawRect(rectX, rectY, rectW, rectH);
			
			for(int i = 0; i < factorANames.length-1; i++) {
				g.drawLine(rectX, rectY+(i+1)*rectH/factorANames.length, rectX+rectW, rectY+(i+1)*rectH/factorANames.length);
			}

			for(int i = 0; i < factorBNames.length-1; i++) {
				g.drawLine(rectX+(i+1)*rectW/factorBNames.length, rectY, rectX+(i+1)*rectW/factorBNames.length, rectY+rectH);
			}

			if(isOnRect) {
				Composite comp = g2.getComposite();
				//int bufX = rectH%2;
				for(int i = 0; i < factorANames.length; i++) {
					for(int j = 0; j < factorBNames.length; j++) {
						if(i == selectedRow && j == selectedCol) {
							g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
							g.setColor(Color.yellow);
						} else if((i == selectedRow || j == selectedCol)&& (i < selectedRow || j < selectedCol)) {
							g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f));
							g.setColor(Color.blue);														
						} else {							
							continue;
						}
						g.fillRect(rectX + j*rectW/factorBNames.length + 3,
								rectY + (int)(i*rectH/(float)factorANames.length)+3,
								rectW/factorBNames.length-6, (int)(rectH/(float)factorANames.length)-6);						
					}
				}
				g2.setComposite(comp);
				g2.setColor(Color.black);				
			}

			g.setFont(plainFont);
			
			for(int i = 0; i < factorANames.length; i++) {
				for(int j = 0; j < factorBNames.length; j++) {	
					sampleCountStr = String.valueOf(sampleIndexAssignments[i][j].size());
					currX = rectX + j*rectW/factorBNames.length + (rectW/factorBNames.length)/2 - fm.stringWidth(sampleCountStr)/2;
					currY = rectY + i*rectH/factorANames.length + (rectH/factorANames.length)/2 + (int)fm.getLineMetrics(sampleCountStr, g).getHeight()/2;					
					g.drawString(sampleCountStr, currX, currY);
				}
			}
		}
		
		/**
		 * Setter for on rectangle boolean
		 * @param onRect
		 */
		private void setOnRect(boolean onRect) {
			isOnRect = onRect;
		}
		
		/**
		 * Sets dimentsion, constrains to min and uses screen size
		 * @param g Graphics env.
		 */
		private void setMinDims(Graphics g) {			
			fm = g.getFontMetrics();
			int minWidth, minHeight;
			int minCurrWidth = 0;
			int minCurrHeight = 0;

			int maxBLabelWidth = 0;
			int maxBLabelHeight = 0;
			for(int i = 0; i < factorBNames.length; i++) {
				maxBLabelWidth = Math.max(maxBLabelWidth, fm.stringWidth(factorBNames[i]));
				maxBLabelHeight = Math.max(maxBLabelHeight, (int)(fm.getLineMetrics(factorBNames[i],g)).getHeight());
			}
			
			minCurrHeight = maxBLabelHeight;
			
			//set the width max * num labels
			minCurrWidth = factorBNames.length * (maxBLabelWidth + MIN_LABEL_B_XPAD);			
			//add one more xpad and right margin
			minCurrWidth += MIN_LABEL_B_XPAD+RIGHT_MARGIN;
			minCurrWidth += PRE_FACTOR_A_LABEL_WIDTH + PRE_LABEL_A_WIDTH+POST_LABEL_A_WIDTH;
						
			int maxALabelWidth = 0;
			int maxALabelHeight = 0;
			for(int i = 0; i < factorANames.length; i++) {
				maxALabelHeight = Math.max(maxALabelHeight, (int)(fm.getLineMetrics(factorANames[i],g)).getHeight());
				maxALabelWidth = Math.max(maxALabelWidth, fm.stringWidth(factorANames[i]));
			}
			
			//set globals
			factorALabelHeight = maxALabelHeight;
			factorALabelWidth = maxALabelWidth;
			factorBLabelHeight = maxBLabelHeight;
			factorBLabelWidth = maxBLabelWidth;

			
			//finish min width			
			minCurrWidth += maxALabelWidth;
			minCurrWidth += fm.getLineMetrics(factorAName,g).getHeight();
			
			//finish min height
			minCurrHeight = factorANames.length * (maxALabelHeight + MIN_LABEL_A_YPAD);
			minCurrHeight += PRE_FACTOR_B_LABEL_HEIGHT + PRE_LABEL_B_HEIGHT + POST_LABEL_B_HEIGHT;
			minCurrHeight += fm.getLineMetrics(factorBName,g).getHeight();
		
	
			minWidth = Math.max(minCurrWidth, getWidth());
			minHeight = Math.max(minCurrHeight, getHeight());
			
			setPreferredSize(new Dimension(minWidth, minHeight+100));
			setSize(minWidth, minHeight+100);
			selectorPanel.validate();
			//this.setMinimumSize(new Dimension(minWidth+100, minHeight+100));
		}

		/**
		 * Handles mouse moved events
		 * @param x x coord
		 * @param y y coord
		 */
		public void onMouseMoved(int x, int y) {
			if(mainRect == null)
				return;
			
			if(this.mainRect.contains(x,y)) {
				isOnRect = true;
				selectedRow = (int)((y-mainRect.y)/(float)mainRect.height * factorANames.length);
				selectedCol = (int)((x-mainRect.x)/(float)mainRect.width * factorBNames.length);				
				//repaint();
			} else {
				if(isOnRect) {
					selectedRow = -1;
					selectedCol = -1;
					isOnRect = false;
					//repaint();
				}
			}
		}
		
		/**
		 * Handles mouse moved released
		 * @param x x coord
		 * @param y y coord
		 */
		public void onMouseReleased(int x, int y) {
						
			if(mainRect == null || selectedSampleIndex == -1)
				return;
			
			if(this.mainRect.contains(x,y)) {
				isOnRect = true;
				selectedRow = (int)((y-mainRect.y)/(float)mainRect.height * factorANames.length);
				selectedCol = (int)((x-mainRect.x)/(float)mainRect.width * factorBNames.length);				
				
				//remove (check) for previous assignment
				removeAssignment(selectedSampleIndex);
				//set the new assignment
				sampleIndexAssignments[selectedRow][selectedCol].add(new Integer(selectedSampleIndex));
				//update the interface labels
				sampleListPanel.updateAssignment(selectedSampleIndex, selectedRow, selectedCol);
			} else {
				selectedSampleIndex = -1;
				if(isOnRect) {
					selectedRow = -1;
					selectedCol = -1;
					isOnRect = false;				
				}
			}
			selectedSampleIndex = -1;
			repaint();
		}
		
		/**
		 * sets Dragging boolean
		 * @param dragging
		 */
		public void setIsDragging(boolean dragging) {
			this.isDragging = dragging;
		}
		
		/**
		 * gets dragging boolean
		 * @return
		 */
		public boolean isDragging() {
			return isDragging;
		}
		
		/**
		 * sets curror x pos
		 * @param x x pos
		 */
		public void setCursorX(int x) {
			cursorX = x;
		}

		/**
		 * sets curror y pos
		 * @param y y pos
		 */
		public void setCursorY(int y) {
			cursorY = y;
		}
		
		/**
		 * sets if a sample is being moved
		 * @param withSample hey.. I've got a sample... maybe...
		 */
		public void setHaveSample(boolean withSample) {
			haveSample = withSample;
		}
	}
	

	/**
	 * Listener for all events, many are dispatched from the glassPanel
	 * @author braisted
	 */
	public class Listener extends MouseAdapter implements ActionListener, MouseMotionListener {

		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			if(command.equals("reset-command"))
				sampleListPanel.resetSelection(e.getSource());
			else if(command.equals("select-sample-ann-command")) {
				sampleListPanel.onAnnotationSelection();
				validate();
			} else if(command.equals("save-design-command")) {
				saveAssignments();
			} else if(command.equals("load-design-command")) {
				loadAssignments();
			}
		}
	
		public void mouseDragged(MouseEvent e) {
			gridPanel.setIsDragging(true);
			gridPanel.setHaveSample(true);
			int x = e.getX();
			int y = e.getY();
			if(e.getSource() == gridPanel) {
				gridPanel.setCursorX(x);
				gridPanel.setCursorY(y);
			
				gridPanel.onMouseMoved(x,y);
			}
		}

		public void mouseMoved(MouseEvent e) {
			gridPanel.setIsDragging(false);
			gridPanel.setHaveSample(false);
			int x = e.getX();
			int y = e.getY();
			if(e.getSource() == gridPanel) {
				gridPanel.onMouseMoved(x,y);
			}
			
			sampleListPanel.handleMouseOver(e.getSource());
		}
		
		public void mouseReleased(MouseEvent e) {
			gridPanel.setHaveSample(false);
			gridPanel.setIsDragging(false);

			if(e.getSource() == gridPanel) {
			int x = e.getX();
			int y = e.getY();
			gridPanel.setCursorX(x);
			gridPanel.setCursorY(y);
			gridPanel.onMouseReleased(x,y);
			} else
				selectedSampleIndex = -1;
			
		}

		public void mousePressed(MouseEvent e) {
			Object source = e.getSource();
			sampleListPanel.handleMousePress(source);
		}

		public void mouseClicked(MouseEvent e) {
			Object source = e.getSource();
			sampleListPanel.handleMouseClick(source);
		}
	}

	/**
	 * Specialized JPanel, not opaque but 'visible' renders mouse tool tip
	 * that renders sample name during drags.
	 * 
	 * @author braisted
	 */
	public class GlassPanel extends JPanel {
		/**
		 * Shows cursor pos.
		 */
		private Point p = new Point(0,0);
		/**
		 * indicates if mouse if over
		 */
		private boolean mouseOverMode;
		/**
		 * Instruction string 1 and 2 for click and drag
		 */
		private String instructionStr;
		private String instructionStr2;
		/**
		 * coord. vars.
		 */
		private int strH, strH2, strW;
		private int w, h;
		
		/**
		 * Constructs a GlassPanel
		 */				
		public GlassPanel() {
			setOpaque(false);  //not opaque
			MouseHandler mh = new MouseHandler(); //handler will dipatch events
			addMouseListener(mh);
			addMouseMotionListener(mh);
			addMouseWheelListener(mh);
			setVisible(false);
			instructionStr = "Click to select sample, then";							 
			instructionStr2 ="drag into design grid above."; 
		}
		
		public void setMouseOverMode(boolean over) {
			mouseOverMode = over;
		}
		
		public boolean getMouseOverMode() {
			return mouseOverMode;
		}
		
		/**
		 * Gets events and displatches them on to components
		 */
		final class MouseHandler extends MouseInputAdapter implements MouseWheelListener {
			
			public void mousePressed(MouseEvent e) {
				p.setLocation(e.getPoint());				
				redispatchMouseEvent(e,true);
				repaint();
			}
			
			public void mouseDragged(MouseEvent e) {
				p.setLocation(e.getPoint());
				redispatchMouseEvent(e,true);
			}
			
			public void mouseReleased(MouseEvent e) {
				p.setLocation(e.getPoint());

				//dispatch to component to handle actions
				redispatchMouseEvent(e,true);

				//set this to released to render glass panel clear
				selectedSampleIndex = -1;
				repaint();
			}
			
			public void mouseMoved(MouseEvent e) {
				p.setLocation(e.getPoint());
				redispatchMouseEvent(e, true);
				//-5 for insets
				gridPanel.onMouseMoved(p.x-5,p.y-5);
			}
			
			public void mouseExited(MouseEvent e) {
				p.setLocation(e.getPoint());
				redispatchMouseEvent(e, true);
				//direct call on grid panel to correct possible missed exit
				gridPanel.onMouseMoved(p.x,p.y);				
			}
			
			public void mouseClicked(MouseEvent e) {
				redispatchMouseEvent(e, true);
			}
		
			private void redispatchMouseEvent(MouseEvent e,
					boolean repaint) {
		
				Point glassPanePoint = e.getPoint();
				Container container = selectorPanel;
				Point containerPoint = SwingUtilities.convertPoint(
						glassPanel,
						glassPanePoint,
						selectorPanel);
				
				if (containerPoint.y < 0) { //we're not in the content pane
					//Could have special code to handle mouse events over
					//the menu bar or non-system window decorations, such as
					//the ones provided by the Java look and feel.
				} else {
					//The mouse event is probably over the content pane.
					//Find out exactly which component it's over.
					Component component =
						SwingUtilities.getDeepestComponentAt(
								container,
								containerPoint.x,
								containerPoint.y);
					
					if ((component != null)) {
						if(e instanceof MouseWheelEvent) {
							Point componentPoint = SwingUtilities.convertPoint(
									glassPanel,
									glassPanePoint,
									component);	
						
							MouseWheelEvent mwe = (MouseWheelEvent)e;
							
							component.dispatchEvent(new MouseWheelEvent(component,
									e.getID(),
									e.getWhen(),
									e.getModifiers(),
									componentPoint.x,
									componentPoint.y,
									e.getClickCount(),
									e.isPopupTrigger(),
									mwe.getScrollType(),
									mwe.getScrollAmount(),
									mwe.getWheelRotation()));
						} else {

						Point componentPoint = SwingUtilities.convertPoint(
								glassPanel,
								glassPanePoint,
								component);
						component.dispatchEvent(new MouseEvent(component,
								e.getID(),
								e.getWhen(),
								e.getModifiers(),
								componentPoint.x,
								componentPoint.y,
								e.getClickCount(),
								e.isPopupTrigger()));
					}
				}
				
				//Update the glass pane if requested.
				if (repaint) {
					//glassPanel.setPoint(glassPanePoint);
					glassPanel.repaint();
				}
				}
			}

			public void mouseWheelMoved(MouseWheelEvent e) {
				redispatchMouseEvent(e,true);				
			}		
		}
		
		
		/**
		 * paints it
		 */
		public void paint(Graphics g) {
			
			super.paint(g);
			
			//indicates a selection has been made, render the sample tag
			if(selectedSampleIndex != -1) {
				String sampleName = sampleListPanel.getSelectedSampleName(selectedSampleIndex);
				FontMetrics fm = g.getFontMetrics();
				LineMetrics lm = fm.getLineMetrics(sampleName,g);				
				g.setColor(new Color(Integer.parseInt("FF0000",16)));				
				g.fill3DRect(p.x-5, p.y-5,10,10, true);				
				w = fm.stringWidth(sampleName);
				h = (int)(lm.getHeight());
				g.setColor(Color.white);
				g.fillRect(p.x+8,p.y-h/2-2,w+4,h+4);
				g.setColor(Color.black);
				g.drawString(sampleName, p.x+10,p.y+5);
				g.drawRect(p.x+8,p.y-h/2-2,w+4,h+4);
				g.drawLine(p.x-7, p.y, p.x+7, p.y);
				g.drawLine(p.x-1, p.y-7,p.x-1,p.y+7);				
			} else if(this.mouseOverMode) {
				//over a grabable object
				Graphics2D g2 = (Graphics2D)g;
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);		
				FontMetrics fm = g.getFontMetrics();
				LineMetrics lm = fm.getLineMetrics(instructionStr,g);
				strW = fm.stringWidth(instructionStr);
				strH = (int)lm.getHeight();
				lm = fm.getLineMetrics(instructionStr2,g);
				strH2 = (int)lm.getHeight();				
				h = strH + strH2 + 8;
				w = strW + 10;	
				g2.setStroke(new BasicStroke(2));
				g2.setColor(new Color(253,244,136));			
				Composite comp = g2.getComposite();
				g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.9f));
				g2.fillRoundRect(p.x, p.y-h, w, h, 8, 8);				
				g2.setComposite(comp);			
				g2.setColor(Color.black);				
				g2.drawRoundRect(p.x, p.y-h, w, h, 8, 8);								
				g2.drawString(instructionStr, p.x+5, p.y-h+strH);
				g2.drawString(instructionStr2, p.x+5, p.y-5);				
				mouseOverMode = false;
			}
		}
	}
	
	/**
	 * To use overlay outside the main class need to embed it into a JPanel
	 * implementation, bug?? if not complains that OverlayLayout can't be shared
	 * 
	 * @author braisted
	 */
	public class StupidOverlayHackPanel extends JPanel {		
		public StupidOverlayHackPanel() {
			super();
			setLayout(new OverlayLayout(this));
		}		
	}

	/**
	 * IWizardParemterPanel method to capture parameters
	 */
	public void populateAlgorithmData() {
		// to capture factor A name, factor B name
		//factor A and B level names
		//int [] factor a groupings
		//int [] factor b groupings
		
		//grab these in case changed by file input
		algData.addParam("factor-A-name", factorAName);
		algData.addParam("factor-B-name", factorBName);
		algData.addStringArray("factor-A-level-names", factorANames);
		algData.addStringArray("factor-B-level-names", factorBNames);
		
		//assignment vector
		int [][] groupAssignments = getFactorAssignmentArrays();
		algData.addIntArray("factor-A-group-assignments", groupAssignments[0]);
		algData.addIntArray("factor-B-group-assignments", groupAssignments[1]);		
		
		algData.addParam("number-of-factor-A-levels", String.valueOf(factorANames.length));
		algData.addParam("number-of-factor-B-levels", String.valueOf(factorBNames.length));
	}


	
	/**
	 * IWizardParemterPanel method to clear parameters
	 */
	public void clearValuesFromAlgorithmData() {
		algData.getParams().getMap().remove("factor-A-name");
		algData.getParams().getMap().remove("factor-B-name");
		algData.getParams().getMap().remove("factor-A-level-names");
		algData.getParams().getMap().remove("factor-B-level-names");
		algData.getParams().getMap().remove("factor-A-grouping-array");
		algData.getParams().getMap().remove("factor-B-grouping-array");		
	}

	/**
	 * IWizardParemterPanel method to clear parameters
	 */
	public void onDisplayed() {
		this.updateSize();
	}
	
	/**
	 * Builds teh factor group assignment
	 * @return returns two arrays, one for each factor, each having #samples entries
	 * Each entry is a vector level index
	 */
	private int [][] getFactorAssignmentArrays() {
		int [][] factorGroupings = new int[2][numSamples];
		int [] factorGroupAssignments;
		for(int sample = 0; sample < numSamples; sample++) {
			factorGroupAssignments = getGroupAssignments(sample);
			factorGroupings[0][sample] = factorGroupAssignments[0];
			factorGroupings[1][sample] = factorGroupAssignments[1];
		}
		return factorGroupings;
	}
	
	public class AnnotationFieldSelectionDialog extends AlgorithmDialog {
		private String [] annFields;
		private JList fieldList;
		private int result;
		
		public AnnotationFieldSelectionDialog(JFrame parent, String [] annotationFields) {
			super(parent, "Annotation Field Selection", true);
			annFields = annotationFields;
			fieldList = new JList(annFields);
			ListSelectionModel smp;
			fieldList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			fieldList.setVisibleRowCount(2);
			fieldList.setBorder(BorderFactory.createLineBorder(Color.black));
			JLabel label = new JLabel("<html>Select one or more sample annotation fields below.<br>(Use Ctrl left click to select multiple fields)</html>");			
			JPanel mainPanel = new JPanel(new GridBagLayout());			
			mainPanel.add(label, new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(20,0,10,0),0,0));
			mainPanel.add(fieldList, new GridBagConstraints(0,1,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,20,0),0,0));			
			addContent(mainPanel);				
			okButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					result = JOptionPane.OK_OPTION;
					dispose();
				}				
			});			
			cancelButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					result = JOptionPane.CANCEL_OPTION;
					dispose();
				}				
			});
			pack();
		}
		
		public String [] getSelectedFieldNames() {
			Object [] selObjs = fieldList.getSelectedValues();
			String [] fieldNames = new String[selObjs.length];
			for(int i = 0; i < fieldNames.length; i++)
				fieldNames[i] = (String)(selObjs[i]);
			return fieldNames;
		}
		
		public int [] getSelectedFieldIndices() {
			return fieldList.getSelectedIndices();
		}
		
	    public int showModal() {
	        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	        setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);
	        show();
	        return result;
	    }		
	}
}
