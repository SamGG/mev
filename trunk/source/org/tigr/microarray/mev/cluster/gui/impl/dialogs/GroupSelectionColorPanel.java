/*
 Copyright @ 1999-2007, The Institute for Genomic Research (TIGR).
 All rights reserved.
 */
/*
 * Created on Dec 4, 2006
 */
package org.tigr.microarray.mev.cluster.gui.impl.dialogs;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;


/**
 * @author braisted
 *
 * The GroupSelectionPanel extends JPanel and also implements IWizardParameterPanel
 * to support optional use in a StatProcessWizard by setting and clearing parameters
 * 
 */
public class GroupSelectionColorPanel extends JPanel implements IWizardParameterPanel{
	
	/**
	 * Indicates if there is only one fator, always true for now
	 */
	//private boolean isOneFactor = true;
	/**
	 * Group names
	 */
	private String [] groups;
	/**
	 * Factor names
	 */
	private String [] factors;
	/**
	 * Sample names
	 */
	private String [] samples;
	/**
	 * Current selecte group names
	 */
	private String selectedGroupName;	
	/**
	 * vector of JButtons for each sample
	 */
	private Vector assignmentButtons;
	/**
	 * sample Group fields
	 */
	private Vector sampleGroupFields;
	/**
	 * The main selection JPanel
	 */
	private JPanel selectionPanel;
	/**
	 * Label string 
	 */
	private String groupLabelStr = "Current Group to Assign:  ";	
	/**
	 * font for labels
	 */
	private Font labelFont;
	/**
	 * Algorithm data to populate 
	 */
	private AlgorithmData algData;
	/**
	 * Single Click Group assignment box
	 */
	private JComboBox groupBox;
	/**
	 * 
	 */
	private boolean toggleClickSelection;
	private JLabel toggleLabel;
	private JLabel groupBoxLabel;
	private Color foregroundColor, disabledForegroundColor;
	private Listener listener;
	
	/**
	 * 
	 * Basic constructor
	 */
	public GroupSelectionColorPanel() {
		super(new GridBagLayout());	
		toggleClickSelection = true;
		foregroundColor = UIManager.getColor("Label.foreground");
		disabledForegroundColor = UIManager.getColor("Label.disabledForeground");
	}
		
	
	
	public GroupSelectionColorPanel(AlgorithmData parameters) {
		super(new GridBagLayout());
		algData = parameters;
		toggleClickSelection = true;
		foregroundColor = UIManager.getColor("Label.foreground");
		disabledForegroundColor = UIManager.getColor("Label.disabledForeground");
	}
	
	
	public GroupSelectionColorPanel(AlgorithmData parameters, String [] groupNames, String [] sampleNames) {
		super(new GridBagLayout());
		algData = parameters;
		groups = groupNames;
		samples = sampleNames;		
		listener = new Listener();
		selectedGroupName = groups[0];
		toggleClickSelection = true;
		foregroundColor = UIManager.getColor("Label.foreground");
		disabledForegroundColor = UIManager.getColor("Label.disabledForeground");

		//construct the sample buttons
		
		Color [] groupColors = new Color[3];
		groupColors[0] = Color.BLUE;
		groupColors[1] = Color.YELLOW;
		groupColors[2] = Color.GRAY;
		
		assignmentButtons = buildAssignmentButtons(sampleNames.length, groups, groupColors);
		labelFont = new JButton().getFont();		
		//construct the corresponding text fields
		sampleGroupFields = buildSampleGroupFields(sampleNames.length, labelFont);	
		JPanel groupSelectionPanel = buildGroupPanelForComboBox("Select Group", "Current Group to Assign:  ", groupNames, listener);		
		selectionPanel = buildSelectionPanel(assignmentButtons, sampleGroupFields);	
		add(groupSelectionPanel, new GridBagConstraints(0, 0, 1, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,5,5,5), 0,0));
		add(selectionPanel, new GridBagConstraints(0, 1, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,5,5,5), 0,0));
		add(buildButtonPanel(), new GridBagConstraints(0, 2, 1, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,5,5,5), 0,0));
		//if(groups.length > 1)
	}
	
	
	public void initializeOneFactor(String [] groupNames, String [] sampleNames) {
		
		removeAll();
		
		groups = groupNames;
		samples = sampleNames;
		
		listener = new Listener();
		
		//initially set to first group for initialization
		selectedGroupName = groups[0];
		
		Color [] groupColors = new Color[20];
		groupColors[0] = Color.BLUE;
		groupColors[1] = Color.YELLOW;
		groupColors[2] = Color.RED;
		groupColors[3] = Color.CYAN;
		groupColors[4] = Color.ORANGE;
		
		groupColors[5] = Color.MAGENTA;
		groupColors[6] = Color.GREEN;
		groupColors[7] = Color.LIGHT_GRAY;
		groupColors[8] = Color.PINK;
		groupColors[9] = Color.GRAY;
		
		groupColors[10] = Color.BLACK;
		groupColors[11] = Color.WHITE;
		groupColors[12] = new Color(185,243,244); //light blue
		groupColors[13] = new Color(81,158,196); //deep wedgewood
		groupColors[14] = new Color(172, 206, 70); //Charteuse (sp?)
		
		groupColors[15] = new Color(255, 128, 128); //salmon
		groupColors[16] = new Color(128, 64, 0); //brown
		groupColors[17] = new Color(128,128,64); //olive
		groupColors[18] = new Color(128,128,92); //periwinkle
		groupColors[19] = new Color(119, 0, 176); //deep purlple
		
		//construct the sample buttons
		assignmentButtons = buildAssignmentButtons(samples.length, groups, groupColors);
		//sampleButtons = buildSampleButtons(sampleNames, listener);
		
		labelFont = new JButton().getFont();
		
		//construct the corresponding text fields
		sampleGroupFields = buildSampleFields(sampleNames, labelFont);
		JPanel instructionPanel = buildInstructionPanel(groupNames, listener);		
		selectionPanel = buildSelectionPanel(assignmentButtons, sampleGroupFields);
		
		add(instructionPanel, new GridBagConstraints(0, 0, 1, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,5,5,5), 0,0));
		add(selectionPanel, new GridBagConstraints(0, 1, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,5,5,5), 0,0));
		add(buildButtonPanel(), new GridBagConstraints(0, 2, 1, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,5,5,5), 0,0));
		
		if(groups.length > 1)
			selectedGroupName = groups[1];
		validate();
	}
	
	private JPanel constructHeader(JPanel lowerPanel) {
		JPanel header = new JPanel(new GridBagLayout());
		
		Component comp = lowerPanel.getComponent(0);

		JLabel label = new JLabel("<html><u>Sample</u></html>");
		label.setHorizontalAlignment(JLabel.CENTER);
		//label.setPreferredSize(comp.getPreferredSize());		
		//label.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		header.add(label, new GridBagConstraints(0, 0, 1, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,0,5,0), 0,0));

		comp = lowerPanel.getComponent(1);				
		label = new JLabel("<html><u>Assignment</u></html>");
		label.setHorizontalAlignment(JLabel.CENTER);
		//label.setPreferredSize(comp.getPreferredSize());		
		//label.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		header.add(label, new GridBagConstraints(1, 0, 1, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,0,5,0), 0,0));
		
		return header;
	}
	
	
	/**
	 public void initializeTwoFactor(String [] factorNames, String [] groupNames, String [] sampleNames) {
	 
	 removeAll();		
	 
	 factors = factorNames;
	 groups = groupNames;
	 samples = sampleNames;
	 
	 Listener listener = new Listener();
	 
	 //initially set to first group for initialization
	  selectedGroupName = groups[0];
	  
	  //construct the sample buttons
	   sampleButtons = buildSampleButtons(sampleNames, listener);
	   
	   labelFont = new JButton().getFont();
	   
	   //construct the corresponding text fields
	    sampleGroupFields = buildSampleGroupFields(sampleNames.length, labelFont);
	    
	    //construct the group selection panel
	     //JPanel groupSelectionPanel = buildGroupPanel(groupNames, listener);
	      JPanel groupSelectionPanel = buildGroupPanelForComboBox("Select Condition", "Current Condition to Assign: ", groupNames, listener);
	      JPanel factorSelectionPanel = buildGroupPanelForComboBox("Select Condition", "Current Condition to Assign: ", factorNames, listener);
	      
	      //construct the sample list, selection panel
	       //JPanel enclosing a JScrollPane, enclosing a pannel of buttons and text fields
	        Vector factorVector = new Vector(2);
	        factorVector.add(factors[0]);
	        factorVector.add(factors[1]);		
	        selectionPanel = buildTwoFactorSelectionPanel(sampleButtons, factorVector, sampleGroupFields);
	        
	        //have the panels, limit the interface size based on screen resolution
	         //35 pixel height.
	          
	          add(factorSelectionPanel, new GridBagConstraints(0, 0, 1, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,5,5,5), 0,0));	
	          add(groupSelectionPanel, new GridBagConstraints(1, 0, 1, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,5,5,5), 0,0));
	          add(selectionPanel, new GridBagConstraints(0, 1, 2, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,5,5,5), 0,0));
	          add(buildButtonPanel(), new GridBagConstraints(0, 2, 2, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,5,5,5), 0,0));
	          
	          
	          //adjustSizeToResolution();
	           
	           //get set with selection of the second group
	            if(groups.length > 1)
	            selectedGroupName = groups[1];
	            
	            validate();
	            }
	            */
	
	public void adjustSizeToResolution() {
		
		int maxSampleWidth = 0;
		int maxGroupWidth = 0;
		FontMetrics metrics = this.getGraphics().getFontMetrics();
		
		for(int i = 0; i < groups.length; i++) {			
			maxGroupWidth  =  Math.max(maxGroupWidth,metrics.stringWidth(groups[i]));			
		}
		
		for(int i = 0; i < samples.length; i++) {			
			maxSampleWidth  =  Math.max(maxSampleWidth,metrics.stringWidth(samples[i]));			
		}
		
		for(int i = 0; i < assignmentButtons.size(); i++) {
			((JButton)(assignmentButtons.get(i))).setPreferredSize(new Dimension(maxSampleWidth+50, 35));
		}
		
		for(int i = 0; i < sampleGroupFields.size(); i++) {
			((JLabel)(sampleGroupFields.get(i))).setPreferredSize(new Dimension(maxGroupWidth+50, 35));
		}
		
		Toolkit toolKit = Toolkit.getDefaultToolkit();		
		int h = (int)(0.7f*toolKit.getScreenSize().height);
		int w = Math.max(maxGroupWidth+maxSampleWidth+150, 350);
		int groupPanelWidth = metrics.stringWidth(groupLabelStr) + 30 + maxGroupWidth + 50;
		
		w = Math.max(w, groupPanelWidth);		
		setPreferredSize(new Dimension(w, h));
		setSize(new Dimension(w,h));
	}
	
	/**
	 * Implemented method from IWizardParameterPanel
	 */
	public void onDisplayed() {
		adjustSizeToResolution();			
	}
	
	private Vector buildAssignmentButtons(int numSamples, String [] groupNames, Color [] groupColors) {
		Vector buttons = new Vector(numSamples);
		ToggleColorButton button;
		
		for(int i = 0; i < numSamples; i++) {
			button = new ToggleColorButton(groupNames, groupColors, 0);
			button.setFocusPainted(false);
			//button = new ToggleColorButton(sampleNames[i]+" : ");
			//button.setHorizontalAlignment(JButton.RIGHT);
			button.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
			//button.addActionListener(listener);
			button.setToolTipText("Click to toggle assignment.");
			buttons.add(button);
		}		
		return buttons;
	}
	
	
	private Vector buildSampleGroupFields(int count, Font groupFont) {
		Vector fields = new Vector(count);
		JTextField field;
		
		for(int i = 0; i < count; i++) {
			field = new JTextField(selectedGroupName);
			field.setEditable(false);
			field.setFont(groupFont);
			if((i % 2) == 0) {
				field.setBackground(Color.lightGray);
			} else {
				//field.setBackground(Color.white);
			}	
			fields.add(field);
		}		
		return fields;
	}
	
	private Vector buildSampleFields(String [] sampleNames, Font groupFont) {
		Vector fields = new Vector(sampleNames.length);
		JLabel label;
		
		for(int i = 0; i < sampleNames.length; i++) {
			label = new JLabel(sampleNames[i]+" : ");
			label.setFont(groupFont);		
			label.setHorizontalAlignment(JLabel.CENTER);
			label.setBorder(BorderFactory.createLineBorder(Color.BLACK));
			fields.add(label);
		}		
		return fields;
	}
	
	private JPanel buildGroupPanel(String [] groupNames, Listener listener) {
		JPanel panel = new JPanel(new GridBagLayout());	
		panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "Select Group"));
		ButtonGroup bg = new ButtonGroup();
		JRadioButton button;
		
		for(int i = 0; i < groupNames.length; i++) {
			button = new JRadioButton(groupNames[i]);
			bg.add(button);
			button.setOpaque(false);
			button.setFocusPainted(false);
			if(i == 1)
				button.setSelected(true);
			button.addActionListener(listener);
			panel.add(button, new GridBagConstraints(0, i, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(3,0,2,0), 0,0));
		}				
		return panel;
	}
	
	
	private JPanel buildGroupPanelForComboBox(String panelTitle, String labelTitle, String [] groupNames, Listener listener) {
		
		JPanel panel = new JPanel(new GridBagLayout());	
		panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), panelTitle)); 
		JComboBox box = new JComboBox(groupNames);
		if(groups.length>1)
			box.setSelectedIndex(1);
		box.addActionListener(listener);		
		JLabel label = new JLabel(labelTitle);
		panel.add(label, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(3,0,2,0), 0,0));
		panel.add(box, new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(3,0,2,0), 0,0));		
		return panel;
	}
	
	private JPanel buildInstructionPanel(String [] groupNames, Listener listener) {
		
		JPanel panel = new JPanel(new GridBagLayout());	
		panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "Selection Instructions")); 
		//JComboBox box = new JComboBox(groupNames);
		//if(groups.length>1)
		//	box.setSelectedIndex(1);
		//box.addActionListener(listener);

		ButtonGroup bg = new ButtonGroup();
		JRadioButton toggleButton = new JRadioButton("Toggle Group Assignments", true);
		toggleButton.setOpaque(false);
		toggleButton.setActionCommand("set-toggle-mode-command");
		toggleButton.addActionListener(listener);
		bg.add(toggleButton);
		
		String text = "<html><font color=black>Left or right click on the assignment button to the right of the sample to toggle "+
			"the selected assignment.</font></html>";
		toggleLabel = new JLabel(text);
				
		panel.add(toggleButton, new GridBagConstraints(0, 0, 2, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10,10,0,10), 0,0));
		panel.add(toggleLabel, new GridBagConstraints(0, 1, 2, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,40,5,10), 0,0));

		JRadioButton menuButton = new JRadioButton("Single Click Selection");		
		menuButton.setOpaque(false);
		menuButton.setActionCommand("set-menu-mode-command");
		menuButton.addActionListener(listener);
		bg.add(menuButton);
		
		panel.add(menuButton, new GridBagConstraints(0, 2, 2, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10,10,0,10), 0,0));
	
		groupBox = new JComboBox(groupNames);
		if(groups.length>1)
			groupBox.setSelectedIndex(1);
		groupBox.addActionListener(listener);		
		groupBox.setEnabled(false);
		groupBoxLabel = new JLabel("Current Group Selection:");
		groupBoxLabel.setEnabled(false);
		
		panel.add(groupBoxLabel, new GridBagConstraints(0, 3, 1, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5,10,10,0), 0,0));
		panel.add(groupBox, new GridBagConstraints(1, 3, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,10,10,10), 0,0));
		
		//	panel.add(box, new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(3,0,2,0), 0,0));		
		return panel;
	}
	
	private JPanel buildSelectionPanel(Vector sampleButtons, Vector sampleLabels) {
		JPanel panel = new JPanel(new GridBagLayout());		
		for(int i = 0; i < sampleButtons.size(); i++) {
			panel.add(((Component)(sampleLabels.get(i))), new GridBagConstraints(0, i, 1, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2,0,2,0), 0,0));			
			panel.add(((Component)(sampleButtons.get(i))), new GridBagConstraints(1, i, 1, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2,0,2,0), 0,0));
		}		
		
		JScrollPane pane = new JScrollPane(panel);		
		JPanel scrollPanel = new JPanel(new GridBagLayout());
		scrollPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "Select Samples in Group"));				
		scrollPanel.add(pane, new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));
		
		pane.setColumnHeaderView(constructHeader(panel));
		
		return scrollPanel;
	}
	
	
	public JPanel buildButtonPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "Save/Load Group File"));		
		JPanel toReturn = new JPanel();
		toReturn.setLayout( new BoxLayout( toReturn, BoxLayout.X_AXIS ) );		
		Dimension dButton = new Dimension( 150, 20 );		
		String title = "Load 'n Save Assignments";
		Border greyLine = BorderFactory.createLineBorder( Color.black, 1 );
		TitledBorder border = BorderFactory.createTitledBorder( greyLine,
				title, TitledBorder.LEADING, TitledBorder.TOP);
		toReturn.setBorder( border );		
		JButton loadButton = new JButton("Load Assignments");
		loadButton.setPreferredSize( dButton );
		loadButton.setActionCommand("load-groupings-command");
		loadButton.addActionListener(listener);
		
		JButton saveButton = new JButton("Save Assignments");
		saveButton.setPreferredSize( dButton );
		saveButton.setActionCommand("save-groupings-command");
		saveButton.addActionListener(listener);
		
		toReturn.add( Box.createHorizontalGlue() );
		toReturn.add( saveButton );
		toReturn.add( Box.createRigidArea( new Dimension( 50, 20 ) ) );
		toReturn.add( loadButton );
		toReturn.add( Box.createHorizontalGlue() );
		
		return toReturn;	
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
				
				pw.println("# Assignment File");
				pw.println("# User: "+userName+" Save Date: "+dateString);
				pw.println("#");
				
				//save group names..?
				
				//print out the group names (skip the 'exclude' group
				for(int i = 0; i < groups.length-1; i++) {
					pw.print("Group "+String.valueOf(i+1)+" Label:\t");
					pw.println(this.groups[i]);
				}
								
				pw.println("#");
				
				pw.println("Sample Index\tSample Name\tGroup Assignment");

				int [] assignments;
				for(int sample = 0; sample < samples.length; sample++) {
					pw.print(String.valueOf(sample+1)+"\t"); //sample index
					pw.print(this.samples[sample]+"\t");
					pw.println(((JButton)(assignmentButtons.get(sample))).getText());
					
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
			Vector groupNames = new Vector();
			
			Vector sampleIndices = new Vector();
			Vector sampleNames = new Vector();
			Vector groupAssignments = new Vector();			
			
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


			if( this.samples.length != sampleNames.size()) {
				status = "number-of-samples-mismatch";
				System.out.println(samples.length+ " s length " + sampleNames.size());
				//warn and prompt to continue but omit assignments for those not represented				

				JOptionPane.showMessageDialog(this, "<html>Error -- number of samples designated in assignment file ("+String.valueOf(sampleNames.size())+")<br>" +
						                                   "does not match the number of samples loaded in MeV ("+samples.length+").<br>" +
						                                   	"Assingments are not set.</html>", "File Compatibility Error", JOptionPane.ERROR_MESSAGE);
				
				return;
			}
	
			//check the number of groups... account for 'excluded' by using +1
			if(this.groups.length != groupNames.size()+1) {
				status = "number-of-groups-mismatch";
				System.out.println(groups.length+ " g length " + groupNames.size());
				
				JOptionPane.showMessageDialog(this, "<html>Error -- number of groups designated in assignment file ("+String.valueOf(groupNames.size())+")<br>" +
                        "does not match the number of groups specified in previous step ("+String.valueOf(groups.length-1)+").<br>" +
                        	"Assingments are not set.</html>", "File Compatibility Error", JOptionPane.ERROR_MESSAGE);

				return;
			}
			
			Vector currSampleVector = new Vector();
			for(int i = 0; i < samples.length; i++)
				currSampleVector.add(samples[i]);
			
			//set all to excluded
			
			//change Group Names, assumes the group name list is the same
			for(int i = 0; i < groupNames.size(); i++)
				groups[i] = (String)(groupNames.get(i));
			//last group name is 'exclude' and won't change
			
			int fileSampleIndex = 0;
			int groupIndex = 0;
			String groupName;
			ToggleColorButton button;
			
			for(int sample = 0; sample < samples.length; sample++) {
				fileSampleIndex = sampleNames.indexOf(samples[sample]);
				groupName = (String)(groupAssignments.get(fileSampleIndex));
				
				groupIndex = groupNames.indexOf(groupName);
				
				button = (ToggleColorButton) (this.assignmentButtons.get(sample));
				//first set the current group names
				button.setStateNames(groups);
				//set state
				if(groupIndex != -1)
					button.setState(groupIndex);
				else
					button.setState(groups.length-1);  //set to last state... excluded
			}
			
			repaint();			
			//need to clear assignments, clear assignment booleans in sample list and re-init
			//maybe a specialized inti for the sample list panel.
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	}
	
	
	
	public class Listener implements ActionListener {
		
		public void actionPerformed(ActionEvent e) {
			
			Object source = e.getSource();
			String command = e.getActionCommand();
	
			if(source instanceof JButton) {
				
				if (command.equals("load-groupings-command")){
					loadAssignments();
				} else if (command.equals("save-groupings-command")) {
					saveAssignments();
				} else {
					/*
					int index = assignmentButtons.indexOf(source);
					((JTextField)(sampleGroupFields.get(index))).setText(selectedGroupName);
					validate();
					repaint();
					*/
				}
			} else if(command.equals("set-toggle-mode-command")) {
				toggleClickSelection = true;

				//work around for html label
				String text = "<html>Left or right click on the assignment button to the right of the sample to toggle "+
				"the selected assignment.</html>";
				toggleLabel.setForeground(foregroundColor);
				toggleLabel.setText(text);
				groupBox.setEnabled(false);
				groupBoxLabel.setEnabled(false);
				
			} else if (command.equals("set-menu-mode-command")) {
				toggleClickSelection = false;				
				
				
				String text = "<html>Left or right click on the assignment button to the right of the sample to toggle "+
				"the selected assignment.</html>";

				//toggleLabel.setText("");
				toggleLabel.setForeground(disabledForegroundColor);
				toggleLabel.setText(text);

				groupBox.setEnabled(true);
				groupBoxLabel.setEnabled(true);
				
			} else {
				selectedGroupName = (String)(((JComboBox)source).getSelectedItem());
			}
		}	
	}
	
	
	
	public static void main(String [] args) {
		
		
		String [] groups = {"Control", "Treated", "Treated 1", "Treated 2", 
				"Treated 3", "Treated 4", "Treated 5", "Exclude"};
		
		
		String [] sampleNames = {
				"Sample_1.txt",
				"Sample_2.txt",
				"Sample_3.txt",
				"Sample_4.txt",
				"Sample_5.txt",
				"Sample_6.txt",
				"Sample_7.txt",
				"Sample_8.txt",
				"Sample_9.txt",
				"Sample_10.txt",
				"Sample_11.txt",
				"Sample_12.txt",
				"Sample_13.txt",
				"Sample_14.txt",
				"Sample_15.txt",
				"Sample_16.txt",
				"Sample_17.txt",
				"Sample_18.txt",
				
				"Sample_19.txt",
				"Sample_20.txt",
				"Sample_21.txt",
				"Sample_22.txt",
				"Sample_23.txt",
				"Sample_24.txt",
				"Sample_25.txt",
				"Sample_26.txt",
				"Sample_27.txt",
				"Sample_28.txt",
				"Sample_29.txt",
				"Sample_30.txt"
				
		};
		
		if(args.length > 0) {
			int groupCnt = Integer.parseInt(args[0]);
			int sampleCnt = Integer.parseInt(args[1]);
			
			groups = new String[groupCnt];
			sampleNames = new String[sampleCnt];
			
			for(int i = 0; i < groupCnt; i++)
				groups[i] = "group_"+String.valueOf(i+1);
			
			for(int i = 0; i < sampleCnt; i++)
				sampleNames[i] = "sample_"+String.valueOf(i+1);
		}
		
		//GroupSelectionPanel d = new GroupSelectionPanel(new JFrame(), groups, sampleNames);
		
		
		String [] factors = {"Factor 1", "Factor 2"};
		GroupSelectionColorPanel d = new GroupSelectionColorPanel();

		d.initializeOneFactor(groups, sampleNames);
		
		JFrame frame = new JFrame();
		frame.getContentPane().add(d);
		
		frame.setLocation(500,200);
		frame.setVisible(true);
		d.adjustSizeToResolution();
		frame.setSize(d.getPreferredSize());
	}
	

	/**
	 * IWizardParameterPanel method to populate 
	 */
	public void populateAlgorithmData() {
		int [] groupAssignments = new int[sampleGroupFields.size()];
		String groupName;
		
		Vector groupNamesVector = new Vector(groups.length);
		
		for(int i = 0; i < groups.length; i++)
			groupNamesVector.add(groups[i]);
		
	
		for(int i = 0; i < assignmentButtons.size(); i++) {
			groupName = ((JButton)(assignmentButtons.get(i))).getText();;
			if(groupName.equals("Exclude")) {
				groupAssignments[i] = -1;
			} else {					
				//add 1 so first group is 1
				groupAssignments[i] = groupNamesVector.indexOf(groupName);
			}
		}		
		algData.addIntArray("group-assignments", groupAssignments);			
	}
	
	/**
	 * IWizardParameterPanel method to clear parameters 
	 */
	public void clearValuesFromAlgorithmData() {
		algData.getParams().getMap().remove("group-assignments");
	}
	
	public class ToggleColorButton extends JButton {
		
		String [] titles;
		Color [] colors;
//		Icon [] coloredIcons;
		int state;
		int numStates;
		boolean forward;
		boolean toggleMode;
		
		public ToggleColorButton(String [] titles, Color [] colorStates, int initialState) {
			super(titles[initialState]);
			setLayout(new GridBagLayout());
			forward = true;
			toggleMode = true;
			colors = colorStates;
			this.titles = titles;
			state = initialState;
			numStates = titles.length;

			setIconTextGap(50);

			this.addMouseListener(new MouseAdapter() { 
				public void mouseReleased(MouseEvent me) {
					forward = !me.isPopupTrigger();
					toggleState();
				}			
			});
		}
		
		public void toggleState() {
			
			if(toggleClickSelection) {			
				if(forward) {
					state = (++state)%numStates;
				} else {
					state--;
					if(state == -1)
						state = numStates-1;
				}
			} else {
				state = groupBox.getSelectedIndex();
			}
			setText(titles[state]);
			validate();
			repaint();			
		}
		
		public void setStateNames(String [] groupNames) {
			titles = groupNames;
		}
		
		public void setState(int s) {
			state = s;
			setText(titles[state]);
			validate();
			repaint();			
		}
		
		public void paint(Graphics g) {	
			super.paint(g);
			if(state != numStates-1) {
			g.setColor(colors[state]);
			g.fillRect(8, 5, getHeight()-10, getHeight()-10);	
			g.setColor(Color.black);
			g.drawRect(8, 5, getHeight()-10, getHeight()-10);	
			g.drawRect(9, 6, getHeight()-12, getHeight()-12);	
			}
		}
		
	}
}
