/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
package org.tigr.microarray.mev.cluster.gui.impl.lem;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

import org.tigr.graph.GC;
import org.tigr.graph.GraphLine;
import org.tigr.graph.GraphPoint;
import org.tigr.graph.GraphTick;
import org.tigr.graph.GraphViewer;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.ParameterPanel;
import org.tigr.util.FloatMatrix;

/**
 * @author braisted
 * The LocusInfoDialog displays expresssion, annotation, and a graphical
 * display of a selected locus.  The dialog also allows the user to walk
 * over the LEM by advancing to the next or previous locus.  A web resource
 * related to the locus can be opened using a button on the dialog.
 */
public class LocusInfoDialog extends JDialog {

	/**Parent LEM
	 */	
	private LinearExpressionMapViewer lem;
	private ParameterPanel graphPanel;
	private InfoPanel infoPanel;
	private IData data;
	private Experiment experiment;
	private FloatMatrix meanMatrix;
	private Experiment fullExperiment;
	private FloatMatrix fullMatrix;
	private int locusIndex;
	private GraphViewer graph;
	private int [] replicates;
	private String locusName;
	private int numLoci;
	private int numSamples;
	private boolean isSelected;
	private JButton nextButton;
	private JButton prevButton;
	private JButton selectLocusButton;
	private String SELECT_LOCUS_BUTTON_TEXT = "Select";
	private String DESELECT_LOCUS_BUTTON_TEXT = "Deselect";
	private Icon selectIcon, deleteIcon;
	
	private Thread locusRunnerThread;
	private LocusRunner locusRunner;
	
	/**
	 * Constructs a Locus information dialog
	 * @param parent parent
	 * @param lem LinearExpressionMapViewr parent
	 * @param locusName locus name
	 * @param locusIndex current locus index
	 * @param experiment <code>Experiment</code> object containing filtered data
	 * @param fullExperiment <code>Experiment</code> object containing all data
	 * @param data IData object
	 * @param replicates replicate array, contains replicate indices for each locus
	 */
	public LocusInfoDialog(JFrame parent, LinearExpressionMapViewer lem, String locusName, int locusIndex, Experiment experiment, Experiment fullExperiment, IData data, int [] replicates) {
		super(parent, "Locus Information: "+locusName);
		this.lem = lem;	
		this.locusName = locusName;
		this.experiment = experiment;
		this.fullExperiment = fullExperiment;
		this.fullMatrix = fullExperiment.getMatrix();
		this.locusIndex = locusIndex;
		this.replicates = replicates;
		this.isSelected = lem.isLocusSelected(locusIndex);

		selectIcon = null;  //GUIFactory.getIcon("select_check24.gif");				
		deleteIcon = null;
		
		meanMatrix = experiment.getMatrix();
		fullMatrix = fullExperiment.getMatrix();		
		numLoci = meanMatrix.getRowDimension();
		this.data = data;		
		numSamples = experiment.getNumberOfSamples();
		
		getContentPane().setLayout(new GridBagLayout());

		graph = createGraph();
		graphPanel = new ParameterPanel("Expression Graph");
		graphPanel.setLayout(new GridBagLayout());			
		graphPanel.add(graph, new GridBagConstraints(0,0,1,1,1,1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(3,3,3,3), 0, 0));
		
		infoPanel = new InfoPanel();
		
		Listener listener = new Listener();
		
		JPanel buttonPanel = new JPanel(new GridBagLayout());
		prevButton = new JButton("<<  Previous");
		prevButton.setFocusPainted(false);
		prevButton.setPreferredSize(new Dimension(120,30));		
		prevButton.setSize(120,30);
		prevButton.setActionCommand("prev-locus-command");	
		prevButton.addActionListener(listener);
		
		nextButton = new JButton("Next  >>");
		nextButton.setFocusPainted(false);
		nextButton.setPreferredSize(new Dimension(120,30));
		nextButton.setSize(120,30);
		nextButton.setActionCommand("next-locus-command");
		nextButton.addActionListener(listener);
		
		String buttonText = "Select";
		selectLocusButton = new JButton(buttonText, selectIcon);
		
		if(isSelected) {
			selectLocusButton.setText("Deselect");
			selectLocusButton.setIcon(deleteIcon);
		}		
		
		selectLocusButton.setFocusPainted(false);
	
		selectLocusButton.setPreferredSize(new Dimension(120,30));
		selectLocusButton.setSize(120,30);
		selectLocusButton.setActionCommand("toggle-locus-selection-command");
		selectLocusButton.addActionListener(listener);		
		selectLocusButton.setFocusPainted(false);
		
		JButton openCMRButton = new JButton("Gene Page");
		openCMRButton.setPreferredSize(new Dimension(120, 30));
		openCMRButton.setSize(120,30);
		openCMRButton.setFocusPainted(false);
		openCMRButton.setActionCommand("open-web-command");
		openCMRButton.addActionListener(listener);
		
		validateButtons();
				
		buttonPanel.add(prevButton, new GridBagConstraints(0,0,1,1,1,1,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(3,5,3,0),0,0));
		buttonPanel.add(openCMRButton, new GridBagConstraints(1,0,1,1,1,1,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(3,10,3,10),0,0));		
		buttonPanel.add(selectLocusButton, new GridBagConstraints(2,0,1,1,1,1,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(3,0,3,10),0,0));
		buttonPanel.add(nextButton, new GridBagConstraints(3,0,1,1,1,1,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(3,0,3,5),0,0));
				
		getContentPane().add(graphPanel, new GridBagConstraints(0,0,1,1,1,1,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
		getContentPane().add(infoPanel, new GridBagConstraints(0,1,1,1,1,1,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));		
		getContentPane().add(buttonPanel, new GridBagConstraints(0,2,1,1,1,0.2,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));		
		
		addWindowListener(listener);
		
		pack();
	}
	
	
	/**
	 * Constructs the small graph of the current locus
	 * @return <code>GraphViewer</code> for the current locus
	 */
	private GraphViewer createGraph() {
		
		String [] names = new String[numSamples];
		
		for(int i = 0; i < numSamples; i++)
			names[i] = data.getSampleName(i);
		
		float [] meanValues = meanMatrix.A[locusIndex];
		
		float [][] values = new float[replicates.length][];
		for(int i = 0; i < values.length; i++) {
			values[i] = fullMatrix.A[replicates[i]];
		}
		
		float [] maxAndMin = getMaxAndMin(meanValues, values);
		
		//constant avoids max = min ==> upperY == lowerY, for flat genes
		int upperY = (int)Math.ceil(maxAndMin[0]+0.001f);
		int lowerY = (int)Math.floor(maxAndMin[1]-0.001f);	
						
		GraphViewer graph = new GraphViewer(null, 0, 425, 0, 300, 0, numSamples, lowerY, upperY, 40, 40, 40, 40, "Expression of Locus: "+locusName, "Sample Number", "Log\u2082(Cy5 / Cy3)");

		graph.setXAxisValue(lowerY);
		
		graph.setShowCoordinates(true);		

		Dimension size = new Dimension(250, 250);    		
		
		graph.setPreferredSize(size);
	    graph.setSize(size);
	    
		GraphTick tick = new GraphTick(0, 8, Color.black, GC.HORIZONTAL, GC.C, "", Color.black);		
	
		graph.addGraphElement(tick);

		//zero line
		graph.addGraphElement(new GraphLine(0, 0, numSamples, 0, Color.black));
		
		for(int i = 0; i < meanValues.length-1; i++) {
								
			tick = new GraphTick(i+1, 8, Color.black, GC.HORIZONTAL, GC.C, String.valueOf(i+1), Color.black);
			graph.addGraphElement(tick);
			
			if(!Float.isNaN(meanValues[i]) && !Float.isNaN(meanValues[i+1]))
				graph.addGraphElement(new GraphLine(i+1, meanValues[i], i+2, meanValues[i+1], Color.magenta));
			
			if(!Float.isNaN(meanValues[i]))			
				graph.addGraphElement(new GraphPoint(i+1, meanValues[i], Color.blue, 5));
		}
	
		tick = new GraphTick(meanValues.length, 8, Color.black, GC.HORIZONTAL, GC.C, String.valueOf(meanValues.length), Color.black);
		graph.addGraphElement(tick);

		if(!Float.isNaN(meanValues[meanValues.length-1]))			
			graph.addGraphElement(new GraphPoint(meanValues.length, meanValues[meanValues.length-1], Color.blue, 5));
		
		
		for(int i = 0; i < values.length; i++) {
			for(int j = 0; j < values[i].length; j++) {
				if(!Float.isNaN(values[i][j]))
					graph.addGraphElement(new GraphPoint(j+1, values[i][j], Color.blue, 2));				
			}
		}

        for (int i = lowerY; i <= upperY; i++) {
            if (i == 0) 
            	tick = new GraphTick(i, 8, Color.black, GC.VERTICAL, GC.C, "0", Color.black);
            else 
            	tick = new GraphTick(i, 8, Color.black, GC.VERTICAL, GC.C, "" + i, Color.black);            
            graph.addGraphElement(tick);
        }
        
        return graph;
	}
	
	/**
	 * Displays a centered locus info. dialog
	 */
	public void showInfo() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);
        show();
	}
	
	/**
	 * Retruns the current max and min for the locus means in the display
	 * @param means locus means
	 * @param vals raw values
	 * @return float array with index 0 with Max, index 1 with min
	 */
	private float [] getMaxAndMin(float [] means, float [][] vals) {
		float [] maxAndMin = new float[2];
		maxAndMin[0] = Float.NEGATIVE_INFINITY;
		maxAndMin[1] = Float.POSITIVE_INFINITY;
	
		for(int i = 0; i < means.length; i++) {
			
			if(!Float.isNaN(means[i]) && means[i] > maxAndMin[0])
				maxAndMin[0] = means[i];

			if(!Float.isNaN(means[i]) && means[i] < maxAndMin[1])
				maxAndMin[1] = means[i];
				
			for(int j = 0; j < vals.length; j++) {
				if(!Float.isNaN(vals[j][i]) && vals[j][i] > maxAndMin[0])
					maxAndMin[0] = vals[j][i];

				if(!Float.isNaN(vals[j][i]) && vals[j][i] < maxAndMin[1])
					maxAndMin[1] = vals[j][i];			
			}
		}
		
		if(maxAndMin[0] == Float.NEGATIVE_INFINITY)
			maxAndMin[0] = 0;
		if(maxAndMin[1] == Float.POSITIVE_INFINITY)
			maxAndMin[1] = 0;
		
		return maxAndMin;
	}
	
	/**
	 * Validates the buttons for the current locus index
	 */
	private void validateButtons() {			
		prevButton.setEnabled(locusIndex != 0);					
		nextButton.setEnabled(locusIndex != numLoci-1);		
		updateSelectionButton();			
	}

	/**
	 * Checks to see if locus is selected, updates the button to add locus
	 */
	public void checkSelection() {
		this.isSelected = lem.isLocusSelected(this.locusIndex);
		updateSelectionButton();
	}
	
	/**
	 * Updates the selection button based on state
	 */
	public void updateSelectionButton() {
		if(!isSelected) {
			this.selectLocusButton.setText(this.SELECT_LOCUS_BUTTON_TEXT);
			this.selectLocusButton.setIcon(selectIcon);
		} else {
			this.selectLocusButton.setText(this.DESELECT_LOCUS_BUTTON_TEXT);			
			this.selectLocusButton.setIcon(deleteIcon);
		}
	}
	
	/**
	 * Updates the Graph panel, on locus change
	 *
	 */
	private void updateGraphPanel() {
		GraphViewer graphViewer = createGraph();
		graphPanel.removeAll();
		graphPanel.add(graphViewer, new GridBagConstraints(0,0,1,1,1,1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(3,3,3,3), 0, 0));
		graphPanel.validate();
		graph = graphViewer;
	}
	
	/**
	 * Triggers jump to next locus
	 *
	 */
	private void getNextLocusInfo() {
		this.locusIndex++;
		this.locusName = lem.getLocusName(locusIndex);
		this.setTitle("Locus Information: "+locusName);
		this.replicates = lem.getReplicatesArray(locusIndex);
		this.isSelected = lem.isLocusSelected(locusIndex);
		updateGraphPanel();
		this.infoPanel.updateContent();
		validateButtons();
		//validate();
	}
	
	/**
	 * Pulls the locus information for the previous locus
	 *
	 */
	private void getPreviousLocusInfo() {
		this.locusIndex--;
		this.locusName = lem.getLocusName(locusIndex);
		this.setTitle("Locus Information: "+locusName);		
		this.replicates = lem.getReplicatesArray(locusIndex);
		this.isSelected = lem.isLocusSelected(locusIndex);		
		this.updateGraphPanel();
		this.infoPanel.updateContent();		
		validateButtons();
		//validate();
	}
	
	/**
	 * 
	 * @author braisted
	 *
	 * Formatted text rendering panel to support delivery of locus information
	 */
	public class InfoPanel extends JPanel {
		private JTextPane textPane;
		private JScrollPane pane;
		
		/**
		 * Constructs an info panel for the current locus
		 */
		public InfoPanel() {
			setLayout(new GridBagLayout());
			
			String text = createContent();			
			textPane = new JTextPane(); 
			textPane.setContentType("text/html");
	
			textPane.setEditable(false);
			textPane.setText(text);
			textPane.setFont(new Font("Arial", Font.PLAIN, 10));
	        textPane.setMargin(new Insets(10,15,10,15));
			textPane.setBackground(new Color(Integer.parseInt("FFFFCC",16)));
	        
			pane = new JScrollPane(textPane);			
			add(pane, new GridBagConstraints(0,0,1,1,1,1,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));						
			//setPreferredSize(new Dimension(500, 450));
			setPreferredSize(new Dimension(250, 300));
			
			textPane.setCaretPosition(0);		
		}
		
		/**
		 * updates the panel for a new locus
		 */
		public void updateContent() {
			String newText = createContent();
			textPane.setText(newText);
			textPane.setCaretPosition(0);
			pane.validate();
		}
		
		/**
		 * Constructs conten
		 * @return String (html)
		 */
		private String createContent() {
			String [] fieldNames = data.getFieldNames();
			int numSamples = meanMatrix.getColumnDimension();
			
			int rowCount = fieldNames.length + 2;
			
			float [] sd = new float[numSamples];
			int [] validN = new int[numSamples];
			
			//populate sd and validN arrays
			getSDs(sd,validN);
			
			String text = "<html><body face=arial bgcolor = \"#FFFFCC\"><h1>Locus: " + locusName +"</h1>";
						
			text += "<h2>Expression Data</h2>";
			
			text += "<table border=3>";
			
			text += "<tr><td nowrap><b>Sample Number</b></td>";
			for(int i = 0; i < numSamples; i++) {
				text += "<td nowrap><b>" + String.valueOf(i+1) + "</b></td>";				
			}
			
			text += "</tr>";
			
			//sample name
			Vector sampleFields = data.getSampleAnnotationFieldNames();
			String fieldName = (String)(sampleFields.get(0));
			text += "<tr><td nowrap><b>Sample Name</b></td>";
			for(int i = 0; i < numSamples; i++) {
				text += "<td nowrap><b>" + data.getSampleAnnotation(i, fieldName) + "</b></td>";				
			}
				
			text += "</tr>";

			for(int i = 1; i < sampleFields.size(); i++ ){
				fieldName = (String)(sampleFields.get(i));
				text += "<tr><td nowrap><b>" + fieldName + "</b></td>";
				for(int j = 0; j < numSamples; j++) {
					text += "<td nowrap>" + data.getSampleAnnotation(j, fieldName) + "</td>";					
				}
				text += "</tr>";
			}
			
			//replicate count
			text += "<tr><td nowrap><b>Locus Reps/Slide</b></td>";			
			for(int i = 0; i < numSamples; i++) {
				text += "<td nowrap>" + String.valueOf(replicates.length) + "</td>";				
			}
			
			//validN count
			text += "</tr><tr><td nowrap><b>Rep. Values/Slide</b></td>";			
			for(int i = 0; i < numSamples; i++) {
				text += "<td nowrap>" + String.valueOf(validN[i]) + "</td>";
			}
						
			text += "</tr><tr><td nowrap><b>Mean</b></td>";			
			float value;
			for(int i = 0; i < numSamples; i++) {
				value = meanMatrix.A[locusIndex][i];
				if(!Float.isNaN(value))
					text += "<td nowrap>" + value + "</td>";				
				else
					text += "<td nowrap> -- </td>";				
			}
			
			text += "</tr><tr><td nowrap><b>SD</b></td>";			
			for(int i = 0; i < numSamples; i++) {
				value = sd[i];
				if(Float.isNaN(value) || validN[i] == 1)				
					text += "<td nowrap> -- </td>";
				else
					text += "<td nowrap>" + value + "</td>";								
			}
			
			text += "</tr></table>";
			
			//annotation
			text += "<h2>Annotation</h2>";
			
			text += "<table border = 3>";			
			if(replicates.length > 1)
				text+= "<th></th><th colspan="+ String.valueOf(replicates.length+1)+"><b>Locus Replicates<b></th>";

			for(int row = 0; row < fieldNames.length + 1; row++) {
				text += "<tr>";

				if(row == 0) {
					text += "<td nowrap><b>UID</b></td>";
				} else {
					text += "<td nowrap><b>" + fieldNames[row-1] + "</b></td>";
				}

				for(int rep = 0; rep < replicates.length; rep++) {
					if(row == 0)
						text += "<td nowrap>" + data.getSlideDataElement(0, fullExperiment.getGeneIndexMappedToData(replicates[rep])).getUID() + "</td>"; 
					else
						text += "<td>" + data.getElementAttribute(fullExperiment.getGeneIndexMappedToData(replicates[rep]), row-1) + "</td>"; 
				}
				text += "</tr>";
			}			
			text += "</table>";
			
			text += "</body></html>";

			return text;			
		}
		
		
		/**
		 * Updates standard deveiations for the replicates related to the displayed locus
		 * also updates the validNArray 
		 * @param sd SD array to be updated
		 * @param validNArray array showing valid replicate measuremet count for each
		 * hyb for the displayed locus
		 */
		private void getSDs(float [] sd, int [] validNArray){
			int numSamples = meanMatrix.getColumnDimension();
			float [] means = meanMatrix.A[locusIndex];
			int validN;
			float value;
			for(int sample = 0; sample < numSamples; sample++) {
				validN = 0;
				for(int rep = 0; rep < replicates.length; rep++) {			
					value = fullMatrix.get(replicates[rep], sample);
					if(!Float.isNaN(value)) {
						sd[sample] += Math.pow( (value-means[sample]),2);
						validN++;
					}
				}
				
				validNArray[sample] = validN;
				//check number of valid numbers
				//no valid entry use NaN
				if(validN == 0)
					sd[sample] = Float.NaN;

				//if validN == 1 set to zero
				else if(validN == 1)					
					sd[sample] = 0;
				
				else
					sd[sample] =  (float)Math.sqrt(sd[sample]/(float)(validN-1));				
			}									
		}
		
	}
	
	/**
	 * Signals to open web resource for the locus index
	 */
    private void linkToURL() {
    	lem.linkToURL(locusIndex);    
    }

    /**
     * 
     * @author braisted
     *
     * LocusRunner implements <code>Runnable</code> and allows smooth update
     * of the dialog when jumping to next locus
     */
    public class LocusRunner implements Runnable {

    	private boolean advance;
    	private long napTime = 3000;
    	
    	public LocusRunner(boolean advance, long napTime) {
    		this.advance = advance;
    		this.napTime = napTime;
    	}
    		
		public void run() {
			while(true) {		
				try {
					Thread.sleep(napTime);
				} catch (Exception e) {
					
				}
				if(advance)
					getNextLocusInfo();
				else
					getPreviousLocusInfo();
			}
		}
    	    	
    }
    
    /**
     * 
     * @author braisted
     *
     * Button listener
     */
	public class Listener extends WindowAdapter implements ActionListener{
	
		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			if(command.equals("next-locus-command")) {
				/*
				locusRunner = new LocusRunner(true, 3000);				
				locusRunnerThread = new Thread(locusRunner);
				locusRunnerThread.start();
				*/
				getNextLocusInfo();
			} else if(command.equals("prev-locus-command")) {
				/*
				if(locusRunnerThread != null) {
					locusRunnerThread = null;
					locusRunner = null;					
				}
				*/					
				getPreviousLocusInfo();
			} else if(command.equals("toggle-locus-selection-command")) {
				lem.toggleSelectedLocus(locusIndex);
				isSelected = !isSelected;
				//updateSelectionButton();				
			} else if(command.equals("open-web-command")) {
				linkToURL();
			} 
		}
		
		public void windowClosing(WindowEvent we) {
			lem.removeInfoViewer(LocusInfoDialog.this);
		}
		
	
	}
	
}
