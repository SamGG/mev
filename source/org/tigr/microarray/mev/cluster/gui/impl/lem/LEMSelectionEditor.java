/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
package org.tigr.microarray.mev.cluster.gui.impl.lem;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.table.AbstractTableModel;

import org.tigr.graph.GC;
import org.tigr.graph.GraphLine;
import org.tigr.graph.GraphPoint;
import org.tigr.graph.GraphTick;
import org.tigr.graph.GraphViewer;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.AlgorithmDialog;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.ParameterPanel;
import org.tigr.util.FloatMatrix;
import org.tigr.util.QSort;

/**
 * @author braisted
 * 
 * The LEMSelectionEditor presents a table of selected loci.
 * A graph representation of each selected loci can be displayed.
 * File output and list editing is supported.
 */
public class LEMSelectionEditor extends AlgorithmDialog {

	private JTable selectionList;
	private LinearExpressionMapViewer lem;
	private SelectionTableModel model;
	private JTable table;
	private JMenu saveSelectedMenu;
	private JMenu fileMenu;

	
	private JPanel mainpanel;
	private JTextPane infoPane;
	private ParameterPanel locusTablePanel;
	private String infoText;

	private IData data;
	private int numSamples;
	private Vector selectedIndices;
	private FloatMatrix meanMatrix;
	private JCheckBoxMenuItem coloredLociBox;
	private JMenuItem hideGraphItem;
	private boolean showGraph;
	
	private LocusGraph locusGraph;

	
	public LEMSelectionEditor (JFrame frame, LinearExpressionMapViewer lem, Vector selectedIndices) {
		super(frame, "LEM Locus Selection List", false);
		
		this.lem = lem;
		this.meanMatrix = lem.getLocusMeanMatrix();
		numSamples = lem.getExperiment().getNumberOfSamples();
		
		this.selectedIndices = selectedIndices;
		showGraph = true;
		
		// infoPanel
		infoPane = new JTextPane();
		infoPane.setContentType("text/html");
		infoPane.setEditable(false);
		infoPane.setBackground(Color.white);
		infoPane.setBorder(BorderFactory.createLineBorder(Color.black, 1));

		infoText = "<html><body><font size = 5><b><u><center>Locus Selection List</center></u></b></font>";
		infoText += "<center>Use <b>shift-left-click</b> on a locus arrow to add to the list.<br></center>";
		infoText += "</body></html>";
		infoPane.setText(infoText);
		
		infoPane.setMargin(new Insets(5,20,5,20));
		Dimension dim = new Dimension(200, 80);
		infoPane.setPreferredSize(dim);
		infoPane.setSize(dim);
		
		//menu
		JMenuBar menu = new JMenuBar();
		Listener listener = new Listener();
		
		fileMenu = new JMenu("File");		
		
		saveSelectedMenu = new JMenu("Save Selected Loci");
				
		JMenuItem item = new JMenuItem("Locus Level Detail");
		item.setActionCommand("save-selected-loci-detail-command");
		item.addActionListener(listener);
		saveSelectedMenu.add(item);
				
		item = new JMenuItem("Spot Level Detail");
		item.setActionCommand("save-selected-spot-detail-command");
		item.addActionListener(listener);
		saveSelectedMenu.add(item);
		
		fileMenu.add(saveSelectedMenu);
		
		JMenu saveAllMenu = new JMenu("Save All Loci");
		
		item = new JMenuItem("Locus Level Detail");
		item.setActionCommand("save-all-loci-detail-command");
		item.addActionListener(listener);
		saveAllMenu.add(item);
				
		item = new JMenuItem("Spot Level Detail");
		item.setActionCommand("save-all-spot-detail-command");
		item.addActionListener(listener);
		saveAllMenu.add(item);

		fileMenu.add(saveAllMenu);
		
		JMenu selectMenu = new JMenu("Select");
		
		item = new JMenuItem("Locus List Selection");
		item.setActionCommand("loci-list-selection-command");
		item.addActionListener(listener);
		selectMenu.add(item);
		
		item = new JMenuItem("Base Range Selection");
		item.setActionCommand("base-range-selection-command");
		item.addActionListener(listener);
		selectMenu.add(item);
		
		/*JMenu viewMenu = new JMenu("View");
		showExpressionGraphItem = new JMenuItem("Show Expression Graphs");
		
		if(selectedIndices.size() == 0)
			showExpressionGraphItem.setEnabled(false);

		showExpressionGraphItem.setActionCommand("toggle-show-expression-graph-command");
			*/
		
		JMenu viewerMenu = new JMenu("Graph Options");
		
		hideGraphItem = new JMenuItem("Hide Locus Graph");
		hideGraphItem.setActionCommand("toggle-hide-graph-command");
		hideGraphItem.addActionListener(listener);
		viewerMenu.add(hideGraphItem);
		
		coloredLociBox = new JCheckBoxMenuItem("Multi-colored Graphs", false);
		coloredLociBox.setActionCommand("toggle-multi-colored-graphs");
		coloredLociBox.addActionListener(listener);
		viewerMenu.add(coloredLociBox);
		
		menu.add(fileMenu);
		menu.add(selectMenu);
		menu.add(viewerMenu);
		menu.setBorder(BorderFactory.createLineBorder(Color.black));
		menu.setMinimumSize(new Dimension(100, 20));
		menu.setMaximumSize(new Dimension(1000, 20));
				
		//set the menu bar (via cast)
		JDialog dialog = (JDialog)this;		

		//JTable construction
		table = new JTable();
		table.addMouseListener(listener);
		table.addKeyListener(listener);
		
		Vector headerVector = new Vector();
		headerVector.add("Locus");
		headerVector.add("5'");
		headerVector.add("3'");
		headerVector.add("# spots/locus");
		
		model = new SelectionTableModel(selectedIndices, headerVector);
		table.setModel(model);		
		
		JScrollPane pane = new JScrollPane(table);
		dim = new Dimension(550, 150);
		pane.setPreferredSize(dim);
		pane.setSize(dim);
				
		mainpanel = new JPanel();
		mainpanel.setLayout(new GridBagLayout());
		
		locusTablePanel = new ParameterPanel("Selected Loci");
		locusTablePanel.setLayout(new GridBagLayout());

		dim.height = 200;
		locusTablePanel.setPreferredSize(dim);
		locusTablePanel.setSize(dim);
		
		locusTablePanel.add(pane, new GridBagConstraints(0,0,1,1,1,1,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));

		mainpanel.add(menu, new GridBagConstraints(0,0,1,1,1,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
		mainpanel.add(infoPane, new GridBagConstraints(0,1,1,1,1,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));

		locusGraph = new LocusGraph();
		
		mainpanel.add(locusGraph, new GridBagConstraints(0,2,1,1,1,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));		
		mainpanel.add(locusTablePanel, new GridBagConstraints(0,3,1,1,1,1,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
		
		//alter button text
		okButton.setText("Close");
		resetButton.setText("Remove Selected");
		dim = new Dimension(120, 30);
		resetButton.setPreferredSize(dim);
		resetButton.setSize(dim);
		cancelButton.setText("Clear All");
		cancelButton.setPreferredSize(dim);
		cancelButton.setSize(dim);
		
		validateMenuItemsAndButtons();
		
		addContent(mainpanel);
		setActionListeners(listener);
		addWindowListener(listener);
		pack();
	}
	
	/**
	 * updates the information text to reflect list changes, row count
	 *
	 */
	public void updateSelectionText() {
		infoText = "<html><body><font size = 5><b><u><center>Locus Selection List</center></u></b></font>";
		infoText += "<center>Use <b>shift-left-click</b> on a locus arrow to add to the list.<br>";
		infoText += "Number of Loci in the List: <b>"+this.model.getRowCount()+"</b></center></body></html>";
		infoPane.setText(infoText);
		infoPane.setCaretPosition(0);
	}
	
	/**
	 * validates the state of buttons based on the list population
	 */
	private void validateMenuItemsAndButtons() {
		boolean isNotEmpty = (table.getRowCount() > 0);
		fileMenu.setEnabled(isNotEmpty);
		resetButton.setEnabled(isNotEmpty);
		cancelButton.setEnabled(isNotEmpty);
		
		int [] selRows = table.getSelectedRows();
		saveSelectedMenu.setEnabled(selRows!=null && selRows.length > 0);
	}
	
	/**
	 * Displays the dialog centered on screen
	 */
	public void showDialog() {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();	    
		setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);	    
		validateMenuItemsAndButtons();
		updateSelectionText();
		show();	    
	}
	
	/**
	 * Method to re-center the dialog
	 */
	public void centerDialog() {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();	    
		setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);	    		
	}
	
	/**
	 * handles additions to loucs list
	 */
	public void fireLocusAdded() {
		model.sortByMinLocation();
		model.fireRowInserted();
		validateMenuItemsAndButtons();
		updateSelectionText();
		locusGraph.refresh();
		//validate();
	}
	
	/**
	 * Handles removals from the locus list
	 */
	public void fireLocusRemoved() {
		model.sortByMinLocation();
		model.fireRowDeleted();
		validateMenuItemsAndButtons();
		updateSelectionText();
		locusGraph.refresh();
		//validate();
	}
	
	/**
	 * Remove a selected loci based on JTable row selection
	 */
	public void removeSelectedLoci() {
		int [] rows = table.getSelectedRows();
		if(rows.length == 0)
			return;
		model.removeSelectedRows(rows);		
		lem.repaint();		
	}
	
	/**
	 * Clears the list
	 * @return true if successful
	 */
	public boolean clearAll() {
		boolean cleared = false;
		if(JOptionPane.showConfirmDialog(this, "Are you sure you are ready to clear the list?", "Clear All Selected Loci", JOptionPane.YES_NO_OPTION ) == JOptionPane.YES_OPTION ) {
			model.removeAllRows();
			cleared = true;
			lem.repaint();
		}
		return cleared;
	}
	
	/**
	 * Retruns the locus indices corresponding to selected rows
	 * @return array of locus indices
	 */
	private int [] getSelectedLocusIndices() {
		int [] rows = table.getSelectedRows();

		if(rows.length == 0)
			return new int[0];

		return model.getLocusIndices(rows);		
	}
	
	/**
	 * Saves the selected loci
	 */
	public void saveSelectedLoci() {
		lem.saveLocusList(getSelectedLocusIndices());
	}
	
	/**
	 * Saves the selected loci, spot level detail
	 */
	public void saveSelectedSpotDetail() {
		lem.saveSpotsForLocusList(getSelectedLocusIndices());
	}
	
	/**
	 * Saves all loci in the list to file
	 */
	public void saveAllLoci() {
		lem.saveLocusList(model.getAllLocusIndices());
	}
	
	/**
	 * Saves all loci in the list to file, spot level detail (replicate data)
	 */
	public void saveAllSpotDetai() {
		lem.saveSpotsForLocusList(model.getAllLocusIndices());
	}

	/**
	 * Trigger a selection loci based on a location range
	 */
	public void baseRangeSelection() {
		if(lem.selectBaseRange())
			locusGraph.refresh();
	}
	
	/**
	 * Toggles the display of locus graphs
	 */
	private void toggleLocusGraph() {
		//toggle show graph
		showGraph = !this.showGraph;
		this.coloredLociBox.setEnabled(showGraph);

		int componentCount = mainPanel.getComponentCount();
		//remove graph
		if(!showGraph) {
			this.hideGraphItem.setText("Show Locus Graph");
			//remove table and graph
			mainpanel.remove(this.locusTablePanel);
			mainpanel.remove(this.locusGraph);			

			//replace table in y pos = 2
			mainpanel.add(locusTablePanel, new GridBagConstraints(0,2,1,1,1,1,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));			
		} else {
			this.hideGraphItem.setText("Hide Locus Graph");
			//remove table
			mainpanel.remove(this.locusTablePanel);			

			locusGraph.refresh();
			
			//insert graph and table
			mainpanel.add(locusGraph, new GridBagConstraints(0,2,1,1,1,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));		
			mainpanel.add(locusTablePanel, new GridBagConstraints(0,3,1,1,1,1,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));			
		}
		validate();
		pack();		
	}
	
	
	/**
	 * 
	 * @author braisted
	 *
	 * Selection model for the locus table
	 */
	public class SelectionTableModel extends AbstractTableModel {
		
		/**
		 * Indices of selected orfs
		 */
		private Vector indices;
		private Vector headerNames;
		private int [] sortedIndices;
		
		/**
		 * Constructor
		 * @param data Data Vector
		 * @param header header names
		 */
		public SelectionTableModel(Vector data, Vector header) {			
			indices = data;
			this.headerNames = header;
		}
		
		/**
		 * Returns the value at row and col
		 */
		public Object getValueAt(int row, int col) {
			if(col == 0)
				return lem.getLocusID(getLocusIndex(row));
			else if(col == 1)
				return new Integer(lem.getStart(getLocusIndex(row)));
			else if(col == 2)
				return new Integer(lem.getEnd(getLocusIndex(row)));
			else
				return new Integer(lem.getNumReplicates(getLocusIndex(row)));				
		}
		
		/**
		 * Returns a locus index corresponding to the given row
		 * @param row row index
		 * @return locus index
		 */
		private int getLocusIndex(int row) {
			row = sortedIndices[row];
			return ((Integer)(indices.get(row))).intValue();
		}

		/**
		 * Returns locus indices corresponding the indicated rows
		 * @param rows row indices for which to pull locus indices
		 * @return list of locus indices
		 */
		public int [] getLocusIndices(int [] rows) {
			int [] locusIndices = new int[rows.length];
			
			for(int i = 0; i < locusIndices.length; i++) {
				locusIndices[i] = getLocusIndex(rows[i]);
			}			
			return locusIndices;
		}

		/**
		 * Returns all locus indices in the list
		 * @return all locus indices
		 */
		public int [] getAllLocusIndices() {			
			int [] locusIndices = new int[getRowCount()];
			
			for(int i = 0; i < locusIndices.length; i++) {
				locusIndices[i] = getLocusIndex(i);
			}			
			return locusIndices;
		}

		/**
		 * Returns the column count of the table
		 */
		public int getColumnCount() {
			return 4;
		}

		/**
		 * Returns the number of rows in the table
		 */
		public int getRowCount() {
			return indices.size();
		}

		/**
		 * Returns the column name specified by col
		 */
		public String getColumnName(int col) {
			return (String)(headerNames.get(col));
		}
		
		/**
		 * updates the table when rows are inserted
		 */
		public void fireRowInserted() {			
			this.fireTableRowsInserted(indices.size()-1, indices.size()-1);		
		}
		
		/**
		 * updates the table when rows are deleted
		 */
		public void fireRowDeleted() {
			if(indices.size() > 0)
				//this.fireTableRowsDeleted(row, row);
				this.fireTableRowsDeleted(indices.size()-1, indices.size()-1);
			else
				this.fireTableRowsDeleted(-1, -1);				
		}
		
		/**
		 * Removes specified rows
		 * @param rows table rows to remove
		 */
		public void removeSelectedRows(int [] rows) {
			Object [] objs = new Object[rows.length];

			for(int i = 0; i < rows.length; i++) {
				objs[i] = indices.get(sortedIndices[rows[i]]);
			}
			
			for(int i = 0; i < rows.length; i++) {
				//let lem mod shared structure, and selected boolean
				lem.toggleSelectedLocus( ((Integer)objs[i]).intValue() );
				fireRowDeleted();
			}
			sortByMinLocation();
		}
		
		/**
		 * Remove all rows
		 */
		public void removeAllRows() {
			int [] rows = new int[indices.size()];
			for(int i = 0; i < rows.length; i++)
				rows[i] = i;
			removeSelectedRows(rows);
			sortByMinLocation();
		}
		
		/**
		 * sorts locus rows by the smaller coordinate of each locus
		 *
		 */
		public void sortByMinLocation() {
								
			float [] minCoord = new float[this.getRowCount()];
			
			if(minCoord.length == 0) {
				sortedIndices = new int[0];
				return;
			}
			
			for(int row = 0; row < minCoord.length; row++)
				minCoord[row]= (float)Math.min(lem.getStart(((Integer)indices.get(row)).intValue()),lem.getEnd(((Integer)indices.get(row)).intValue()));

			QSort qsort = new QSort(minCoord);			
			sortedIndices = qsort.getOrigIndx();								
		}		
	}
	
	/**
	 * 
	 * @author braisted
	 *
	 * LocusGraph zero or more graphs present in the table
	 * Two main modes exist, 1.) Monochrom overlay mode and
	 * 2.) Multicolored display with key to associate locus with graph line
	 */
	private class LocusGraph extends JPanel {
	
		private GraphViewer graph;
		private Vector lineColorVector;
		private int numberOfLineColors;
		private boolean multiColored;
		private KeyPanel key;
		
		private JScrollPane keyPane;
		
		/**
		 * Constructs a LocusGraph object
		 */
		public LocusGraph() {
			setLayout(new GridBagLayout());
			setBackground(Color.white);
			setBorder(BorderFactory.createLineBorder(Color.black));			
			multiColored = false;
			
			lineColorVector = new Vector();
			lineColorVector.add(Color.magenta);
			lineColorVector.add(new Color(0, 143, 0));
			lineColorVector.add(Color.cyan);
			lineColorVector.add(Color.blue);
			lineColorVector.add(Color.red);
			lineColorVector.add(Color.black);
			lineColorVector.add(Color.pink);
			lineColorVector.add(Color.gray);
			lineColorVector.add(Color.green);
			numberOfLineColors = lineColorVector.size();
			
			graph = createGraphViewer();
			
			add(graph, new GridBagConstraints(0,0,1,1,1,1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
		
			key = new KeyPanel();
			keyPane = new JScrollPane(key);
			
			Dimension dim = new Dimension(180, 150);
			keyPane.setPreferredSize(dim);
			keyPane.setSize(dim);
		}

		/**
		 * Constructs a new graph viewer based current selection
		 */
		public void createNewGraph() {
			GraphViewer newGraph = createGraphViewer();
			
			//replace component
			this.removeAll();
			
			if(multiColored) {
				//share space
				key.updateSize();				
				add(newGraph, new GridBagConstraints(0,0,1,1,0.7,1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));			
				add(keyPane, new GridBagConstraints(1,0,1,1,0.3,1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));							
			} else {
				add(newGraph, new GridBagConstraints(0,0,1,1,1,1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));							
			}
			
			graph = newGraph;			
			validate();
		}
		
		/**
		 * Refreshes the display, important after locus addition
		 */
		public void refresh() {
			createNewGraph();
			key.updateSize();				
		}
		
		/**
		 * Toggles the monochrome/multi-colored option
		 */
		public void toggleMultiColored() {
			multiColored = !multiColored;
			refresh();				
		}
		
		/**
		 * Returns the line color for the specified table row index
		 * @param index row index
		 * @return line color
		 */
		public Color getLineColor(int index) {
			return (Color)(lineColorVector.get(index%numberOfLineColors));
		}
				
		/**
		 * Constructs a new <code>GraphViewer</code>
		 * @return new GraphViewer
		 */
		private GraphViewer createGraphViewer() {

			int locusIndex;
			String locusName;
			float [] meanValues;
			Color lineColor = Color.magenta;			
			String [] names = new String[numSamples];	
			int rows [] = table.getSelectedRows();			
			int [] indices = model.getLocusIndices(table.getSelectedRows());				
			float [] maxAndMin = getMaxAndMin(meanMatrix, indices);
			
			//constant avoids max = min ==> upperY == lowerY, for flat genes
			int upperY = (int)Math.ceil(maxAndMin[0]+0.001f);
			int lowerY = (int)Math.floor(maxAndMin[1]-0.001f);	
							
			GraphViewer graph = new GraphViewer(null, 0, 425, 0, 300, 0, numSamples, lowerY, upperY, 40, 40, 40, 40, "Mean Locus Expression", "Sample Number", "Log\u2082(Cy5 / Cy3)");
			graph.setXAxisValue(lowerY);			
			graph.setShowCoordinates(true);		

			Dimension size = new Dimension(250, 250);    		
			
			graph.setPreferredSize(size);
		    graph.setSize(size);
		    
			GraphTick tick = new GraphTick(0, 8, Color.black, GC.HORIZONTAL, GC.C, "", Color.black);		
		
			graph.addGraphElement(tick);

			//zero line
			graph.addGraphElement(new GraphLine(0, 0, numSamples, 0, Color.black));

			//ticks, and last tick
			for(int i = 0; i < numSamples-1; i++) {									
				tick = new GraphTick(i+1, 8, Color.black, GC.HORIZONTAL, GC.C, String.valueOf(i+1), Color.black);
				graph.addGraphElement(tick);
			}
			tick = new GraphTick(numSamples, 8, Color.black, GC.HORIZONTAL, GC.C, String.valueOf(numSamples), Color.black);
			graph.addGraphElement(tick);

			
			for(int locus = 0; locus < indices.length; locus++) {
				locusIndex = indices[locus];
				lineColor = (Color)(lineColorVector.get(locus%numberOfLineColors));
				for(int i = 0; i < numSamples-1; i++) {
									
					if(!Float.isNaN(meanMatrix.A[indices[locus]][i]) && !Float.isNaN(meanMatrix.A[indices[locus]][i+1])) {
						if(multiColored)
							graph.addGraphElement(new GraphLine(i+1, meanMatrix.A[indices[locus]][i], i+2, meanMatrix.A[indices[locus]][i+1], lineColor));
						else
							graph.addGraphElement(new GraphLine(i+1, meanMatrix.A[indices[locus]][i], i+2, meanMatrix.A[indices[locus]][i+1], Color.magenta));							
					}
				
					if(!Float.isNaN(meanMatrix.A[indices[locus]][i]))			
						graph.addGraphElement(new GraphPoint(i+1, meanMatrix.A[indices[locus]][i], Color.blue, 5));
			
				}
				
				if(!Float.isNaN(meanMatrix.A[indices[locus]][numSamples-1]))			
					graph.addGraphElement(new GraphPoint(numSamples, meanMatrix.A[indices[locus]][numSamples-1], Color.blue, 5));
								
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
		 * Returns the min and max values, in that order in a two member float array
		 * for the float matrix and indicated row indices
		 * @param meanMatrix input matrix
		 * @param indices row indices
		 * @return float array with min value and max value
		 */
		private float [] getMaxAndMin(FloatMatrix meanMatrix, int [] indices) {
			float [] maxAndMin = new float[2];
			maxAndMin[0] = Float.NEGATIVE_INFINITY;
			maxAndMin[1] = Float.POSITIVE_INFINITY;
			int index;
			
			for(int i = 0; i < indices.length; i++) {
				index = indices[i];
				for(int j = 0; j < meanMatrix.A[index].length; j++) {
					if(!Float.isNaN(meanMatrix.A[index][j])) {
						maxAndMin[0] = Math.max(maxAndMin[0], meanMatrix.A[index][j]);
						maxAndMin[1] = Math.min(maxAndMin[1], meanMatrix.A[index][j]);
					}
				}
			}
			
			if(maxAndMin[0] == Float.NEGATIVE_INFINITY)
				maxAndMin[0] = 0;
			if(maxAndMin[1] == Float.POSITIVE_INFINITY)
				maxAndMin[1] = 0;
			
			return maxAndMin;
		}
		
		/**
		 * 
		 * @author braisted
		 *
		 * The KeyPanel displays the color scheme for locus lines in the display
		 * while in the multicolor display mode
		 */
		public class KeyPanel extends JPanel {
			private JScrollPane pane;
			private int maxIDWidth;
			
			/**
			 * Constructs a new key panel
			 */
			public KeyPanel() {
				super();
				setBackground(Color.white);
			}

			/**
			 * Updates the key panel size, important after locus list changes
			 */
			public void updateSize() {
				int [] indices = table.getSelectedRows();

				FontMetrics fm = getFontMetrics(getFont());
				if(fm == null) {
					return;
				}
				
				int w;
				int h = (int)(fm.getHeight() * 1.5) * indices.length;				
				h = Math.max(locusGraph.getHeight(), h);

				int stringWidth = 0;
							
				for(int i = 0; i < indices.length; i++) {					
					stringWidth = Math.max(stringWidth, fm.stringWidth((String)(model.getValueAt(indices[i], 0))));
				}

				w = stringWidth + 75;
				
				setPreferredSize(new Dimension(w,h));
				setSize(w,h);				
			}
			
			/**
			 * Paints the panel information, line previews, to the specified Graphics object
			 */
			public void paint(Graphics g) {
				super.paint(g);
				FontMetrics fm  = g.getFontMetrics();
				int h = (int)(fm.getHeight() * 1.5);
				int offset = h;
				String id;
								
				int [] indices = table.getSelectedRows();
				
				for(int i = 0; i < indices.length; i++) {					
					//id = (String)(model.getValueAt(indices[i], 0));
					id = (String)table.getValueAt(indices[i], 0);
					
					g.setColor(getLineColor(i));
					g.drawLine(7, offset-3, 55, offset-3);

					g.setColor(Color.blue);									
					g.fillRect(5, offset-5, 5, 5);
					g.fillRect(55, offset-5, 5, 5);					

					g.setColor(Color.black);
					g.drawString(id, 75, offset);

					offset += h;
				}								
			}
			
			
		}
		
	}
	

	/**
	 * @author braisted
	 *
	 * Listener listens to button and menu <code>ActionEvent</code>s and delegates
	 * event handling. 
	 */
	private class Listener extends WindowAdapter implements ActionListener, MouseListener, KeyListener
	{

		public void actionPerformed(ActionEvent ae) {
			String command = ae.getActionCommand();
			if(command.equals("ok-command")) {
				dispose();;
			} else if(command.equals("cancel-command")) {
				//prompt to check then clear list
				if(clearAll()) {						
					validateMenuItemsAndButtons();					
					locusGraph.refresh();
				}
			} else if(command.equals("reset-command")) {
				removeSelectedLoci();
				validateMenuItemsAndButtons();				
				locusGraph.refresh();
			} else if(command.equals("info-command")) {
				
			} else if(command.equals("save-selected-spot-detail-command")) {
				saveSelectedSpotDetail();
			} else if(command.equals("save-selected-loci-detail-command")) {
				saveSelectedLoci();
			} else if(command.equals("save-all-loci-detail-command")) {
				saveAllLoci();
			} else if(command.equals("save-all-spot-detail-command")) {
				saveAllSpotDetai();
			} else if(command.equals("loci-list-selection-command")) {
				
			} else if(command.equals("base-range-selection-command")) {
				baseRangeSelection();
			} else if(command.equals("toggle-multi-colored-graphs")) {
				locusGraph.toggleMultiColored();
			} else if(command.equals("toggle-hide-graph-command")) {
				toggleLocusGraph();
			}
		}
		
		public void mouseClicked(MouseEvent e) {	
			validateMenuItemsAndButtons();
		}

	
		public void mouseEntered(MouseEvent e) {		
		}


		public void mouseExited(MouseEvent e) {	
		}


		public void mousePressed(MouseEvent e) {	
			validateMenuItemsAndButtons();
		}


		public void mouseReleased(MouseEvent e) {
			validateMenuItemsAndButtons();
			locusGraph.refresh();
		}

		public void keyPressed(KeyEvent e) {

		}

	
		public void keyReleased(KeyEvent e) {
			if(e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN) {
				locusGraph.refresh();
			}	
		}

		public void keyTyped(KeyEvent e) {
	
		}
		
	}
	
}
