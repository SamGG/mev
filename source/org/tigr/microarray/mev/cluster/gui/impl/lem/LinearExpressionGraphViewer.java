/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
package org.tigr.microarray.mev.cluster.gui.impl.lem;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.beans.Expression;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

import org.tigr.microarray.mev.TMEV;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IDisplayMenu;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.IViewer;

/**
 * @author braisted
 *
 * Class: LinearExpressionGraphViewer Description: Provides a multiple sample
 * expression view that is organized by chromosomal coordinates Each
 * chromosome, if there is more than one, will have a dedicated LEG in which
 * Loci are arranged based on coordinate order.
 * 
 */ 
public class LinearExpressionGraphViewer extends JPanel implements IViewer {

	private JSplitPane splitPane;
	private Graph graph;
	private SampleTable table;
	private IData idata;	
	private int numberOfSamples;
	private boolean overlay = false;
	private IFramework framework;
	private JPopupMenu popup;
	private boolean showRefLine = false;
	private int exptID = 0;
	
	//EH state-saving additions
	String chrID;
	String[] sortedLocusIDs;
	int[] sortedStartArray, sortedEndArray;
	Experiment fullExperiment, reducedExperiment;
	int[][] replicates;
	String locusIDFieldName;
	
	
	/**
	 * Class: LinearExpressionGraphViewer Description: Provides a multiple sample
	 * expression view that is organized by chromosomal coordinates Each
	 * chromosome, if there is more than one, will have a dedicated LEG in which
	 * Loci are arranged based on coordinate order.
	 * 
	 * 
	 * @param fullExperiment
	 *            The current Experiment from IFramwork
	 * @param reducedExperiment
	 *            The reduced and sorted (by min coord.) Experiment
	 * @param sortedLocusIDs
	 *            Sorted Locus IDs
	 * @param sortedStartArray
	 *            Sorted min coordinates for each loci.
	 * @param sortedEndArray
	 *            Sorted max coordinates for each loci.
	 * @param isForward
	 *            Indicates if a loci is transcribe forward or reverse (rel. to
	 *            coord. system)
	 * @param strata
	 *            renders loci that overlap as an offset from the main linear
	 *            map.
	 * @param chrID
	 *            Identifies the chromosome in view.
	 */		
	public LinearExpressionGraphViewer(Experiment fullExperiment,
			Experiment reducedExperiment, String[] sortedLocusIDs, int [] sortedStartArray,
		    int[] sortedEndArray, int [][] replicates,
			String chrID, String locusIDFieldName) {

		super(new GridBagLayout());
		
			this.numberOfSamples = fullExperiment.getNumberOfSamples();
			splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, graph, table);
			splitPane.setResizeWeight(1.0);
			splitPane.setDividerLocation(0.7);
			
			Listener listener = new Listener();
			popup = contructMenu(listener);
			
			//EH state-saving
			this.chrID = chrID;
			this.sortedLocusIDs = sortedLocusIDs;
			this.sortedStartArray = sortedStartArray;
			this.sortedEndArray = sortedEndArray;
			this.replicates = replicates;
			this.locusIDFieldName = locusIDFieldName;
			this.fullExperiment = fullExperiment;
			this.reducedExperiment = reducedExperiment;
	}

	/**
	 * @see org.tigr.microarray.mev.cluster.gui.IViewer#getExpression()
	 */
	public Expression getExpression(){
		return new Expression(this, this.getClass(), "new", 
			new Object[]{this.fullExperiment, this.reducedExperiment, 
				this.sortedLocusIDs, this.sortedStartArray,
				this.sortedEndArray, this.replicates, 
				this.chrID, this.locusIDFieldName}); 
	}
	
	/**
	 * Updates the viewer to display the display mode
	 *
	 */
	private void updateViewerModeView() {
		this.removeAll();
		if(overlay) {					
			add(splitPane, new GridBagConstraints(0,0,1,1,1,1,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));

			//start in connected points view mode to make overlay meaningful
			graph.getGraphComponent().enableDiscreteValueOverlay(false);
			graph.getGraphComponent().enableOffsetLinesMode(false);
			
			graph.validateView();
			splitPane.validate();
			splitPane.setDividerLocation(0.7);
			repaint();
		} else {		
			graph.removeGraphComponent();
			add(graph.getGraphComponent(), new GridBagConstraints(0,0,1,1,1,1,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));			
			graph.getGraphComponent().refreshGraph();
		}		
	}
	
	/**
	 * Graph customization.
	 * Pulls graph properties, constructs customization dialog, directs graph to
	 * conform to input values.
	 */
	private void customizeGraph() {
		Hashtable props = graph.getGraphComponent().getGraphProperties();
		
		GraphScaleCustomizationDialog dialog = new GraphScaleCustomizationDialog((JFrame)framework.getFrame(), true, props);		
		
		if(dialog.showModal() == JOptionPane.OK_OPTION) {

			//grab the graph component						
			LEMGraphViewer lemGraph = graph.getGraphComponent();			
			
			//collect settings and forward to graph viewer
			int yRangeMode = dialog.getYRangeMode();
			
			lemGraph.setYAxisRangeMode(yRangeMode);

			
			if(yRangeMode == 1 ) {//GraphScaleCustomizationDialog.YRANGE_OPTION_CUSTOM_RANGE) {
				lemGraph.setYRange(dialog.getYMin(), dialog.getYMax());
				lemGraph.setTicInterval(dialog.getYTicInterval());
			}
			
			lemGraph.setShowXAxis(dialog.showXAxisLine());
			lemGraph.setXAxisStroke(dialog.getXAxisStyle());
			lemGraph.setXAxisColor(dialog.getXAxisColor());
			lemGraph.setXAxisCrossPoint(dialog.getXAxisCrossPoint());

			lemGraph.enableOffsetLinesMode(dialog.isOffsetLinesModeSelected());
			lemGraph.setOffsetLinesMidpoint(dialog.getOffsetMidpoint());
			
			lemGraph.setOffsetLinesMin(dialog.getOffsetMin());
			lemGraph.setOffsetLinesMax(dialog.getOffsetMax());
			lemGraph.enableDiscreteValueOverlay(dialog.getShowOverlay());
			
			lemGraph.refreshGraph();
		}
		
	}
	
	/**
	 * Return the graph's content component
	 */
	public JComponent getContentComponent() {
		return this;
	}

	/**
	 * returns the header component
	 */
	public JComponent getHeaderComponent() {
	    
	    //JB, 5/5/06 state saving requires that graph be created
	    //in onSelected but getHeaderComponent() is called first
	    //construct graph to provide linked header rather than a new header
	    
		
	    if(graph == null){
	        graph = new Graph(fullExperiment, reducedExperiment.getMatrix().A, chrID, sortedLocusIDs, sortedStartArray, sortedEndArray);
	    
	       // JB 5/5/06 can't construct table without idata
	       // so set graph line and marker colors in onSelected 
	       // graph.setSampleLineColors(table.lineColorVector);
	       // graph.setSampleMarkerColors(table.markerColorVector);
	        graph.getGraphComponent().addMouseListener(new Listener());
	        graph.getGraphComponent().enableOverlay(overlay);
	        splitPane.setTopComponent(graph);
	    }
	    return graph.getGraphComponent().getHeaderComponent();
	}

	/**
	 * Returns the row header component 
	 */
	public JComponent getRowHeaderComponent() {
		return null;
	}

	/**
	 * Returns the cornoer component
	 */
	public JComponent getCornerComponent(int cornerIndex) {
		return null;
	}

	/**
	 * Prepares the viewer for display
	 * Framework is MeV's IFramework object
	 */
	public void onSelected(IFramework framework) {
		this.framework = framework;
		this.idata = framework.getData();
	
		if(table == null) {
			table = new SampleTable();
			//JB: 5/5/06 add to bottom component of split pane
			splitPane.setBottomComponent(table);
		}
			
		if(graph == null){
			graph = new Graph(fullExperiment, reducedExperiment.getMatrix().A, chrID, sortedLocusIDs, sortedStartArray, sortedEndArray);
			graph.setSampleLineColors(table.lineColorVector);
			graph.setSampleMarkerColors(table.markerColorVector);
			graph.getGraphComponent().addMouseListener(new Listener());
			graph.getGraphComponent().enableOverlay(overlay);
			//JB: 5/5/06 add to top component of split pane
			splitPane.setTopComponent(graph);
		}	
		
		//set these to coordinate
		graph.setSampleLineColors(table.lineColorVector);
		graph.setSampleMarkerColors(table.markerColorVector);
		
		updateViewerModeView();
		graph.onSelected(framework);
	}


	/**
	 * handles data changes, IViewer interface method
	 */
	public void onDataChanged(IData data) {		
	}


	/**
	 * handles menu option changes (display menu)
	 */
	public void onMenuChanged(IDisplayMenu menu) {
		graph.getGraphComponent().onMenuChanged(menu);		
	}

	/**
	 * Handles deselection of the node
	 */
	public void onDeselected() { }

	/**
	 * Handles closing event
	 */
	public void onClosed() { }

	/**
	 * Returns the image, handled in MAV
	 */
	public BufferedImage getImage() {
		return null;
	}

	/**
	 * Returns the cluster index object
	 */
	public int[][] getClusters() {
		return null;
	}

	/**
	 * returns the experiment
	 */
	public Experiment getExperiment() {
		return null;
	}

	/**
	 * Returns the viewer type 
	 */
	public int getViewerType() {		
		return 0;
	}

	/**
	 * Builds the menu
	 *
	 * @param listener 
	 * @return
	 */
	private JPopupMenu contructMenu(Listener listener) {
		JPopupMenu menu = new JPopupMenu();
		
		JMenu submenu = new JMenu("Viewer Mode");
		ButtonGroup bg = new ButtonGroup();
		
		JRadioButtonMenuItem buttonItem = new JRadioButtonMenuItem("Tile Graphs", true);
		buttonItem.setFocusPainted(false);
		buttonItem.setActionCommand("tile-mode-command");
		buttonItem.addActionListener(listener);
		bg.add(buttonItem);
		submenu.add(buttonItem);		
				
		buttonItem = new JRadioButtonMenuItem("Overlay Graphs");
		buttonItem.setFocusPainted(false);
		buttonItem.setActionCommand("overlay-mode-command");
		buttonItem.addActionListener(listener);
		bg.add(buttonItem);
		submenu.add(buttonItem);		
		
		menu.add(submenu);
		
		JMenuItem item = new JMenuItem("Customize Graph");
		item.setFocusPainted(false);
		item.setActionCommand("customize-graph-range-command");
		item.addActionListener(listener);
		menu.addSeparator();
		menu.add(item);
		
		JCheckBoxMenuItem box = new JCheckBoxMenuItem("Show Locus Reference Line", false);
		box.setFocusPainted(false);
		box.setActionCommand("show-reference-line-command");
		box.addActionListener(listener);
		menu.add(box);
		
		item = new JMenuItem("Zoom Out (reset X range)");
		item.setFocusPainted(false);
		item.setActionCommand("reset-x-range-command");
		item.addActionListener(listener);
		menu.addSeparator();
		menu.add(item);
				
		return menu;
	}
	
	/**
	 * 
	 * @author braisted
	 *
	 * The SampleTable object displays the hybs that can be displayed in graphs
	 * This class extends JTable and contains a JTable objext.
	 */
	public class SampleTable extends JPanel {

		protected JTable table;
		protected SampleTableModel model;
		protected JScrollPane pane;
		
	    protected Vector lineColorVector;
	    protected Vector markerColorVector;
	    protected int numberOfLineColors;
	    protected int numberOfMarkerColors;
	    private int ROW_HEIGHT = 20;
	    private int VISIBLE_ROWS = 8;
	    
		public SampleTable() {
			super(new GridBagLayout());
			
			//default line colors
			lineColorVector = new Vector();
			lineColorVector.add(Color.lightGray);
			lineColorVector.add(Color.pink);
			lineColorVector.add(new Color(108,108,255)); //darker lavender
			lineColorVector.add(new Color(128,180,128)); //sage green
			lineColorVector.add(new Color(201,70,20)); //burnt sienna
			lineColorVector.add(new Color(77,140,149)); //dark green-blue			
			lineColorVector.add(new Color(173,52,131)); //dark magenta
			numberOfLineColors = lineColorVector.size();
			
			//default marker colors
			markerColorVector = new Vector();						
			markerColorVector.add(Color.blue);
			markerColorVector.add(Color.black);
			markerColorVector.add(new Color(92, 74, 145));
			markerColorVector.add(Color.gray);
			markerColorVector.add(new Color(7,120,67));			
			numberOfMarkerColors = markerColorVector.size();

			//get sample names
			Vector headerNames = idata.getSampleAnnotationFieldNames(); 
			int numSampleAnnFields = headerNames.size();
			//append additional columns
			headerNames.add("Key");
			headerNames.add("Line Color");
			headerNames.add("Marker Color");			
			
			Object [][] data = new Object[numberOfSamples][headerNames.size()];

			//construct new vectors for line and marker colors
			Vector newLineColorVector = new Vector();
			Vector newMarkerColorVector = new Vector();
			
			table = new JTable(); //just a temp to get default sel. background
			
			for(int i = 0; i < data.length; i++) {
				for(int j = 0; j < data[0].length; j++) {
					if(j < numSampleAnnFields)
						data[i][j] = idata.getSampleAnnotation(i, (String)headerNames.get(j));
					else {
						if(j == data[0].length-2) {
							data [i][j] = (Color)lineColorVector.get(i%numberOfLineColors);
							newLineColorVector.add(data[i][j]);
						} else if(j == data[0].length-1) {
							data [i][j] = (Color)markerColorVector.get(i%numberOfMarkerColors);
							newMarkerColorVector.add(data[i][j]);
						} else if(j == data[0].length-3) {
							data [i][j] = new LinePreview((Color)markerColorVector.get(i%numberOfMarkerColors), (Color)lineColorVector.get(i%numberOfLineColors), table.getSelectionBackground());
						}
					}
				}
			}
			
			//assign vectors reflecting current table
			lineColorVector = newLineColorVector;
			markerColorVector = newMarkerColorVector;			
									
			model = new SampleTableModel(data, headerNames);
			table = new JTable(model);		
						
			table.setRowHeight(20);
			LEGTableCellRenderer renderer = new LEGTableCellRenderer();
			table.setDefaultRenderer(Color.class, renderer);
			table.setDefaultRenderer(LinePreview.class, renderer);
			
			table.addMouseListener(new TableListener());
			
			pane = new JScrollPane(table);			
			pane.setColumnHeaderView(table.getTableHeader());
			
			add(pane, new GridBagConstraints(0,0,1,1,1,1,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));					
	
			table.getSelectionModel().setSelectionInterval(0,0);
			Dimension dim = new Dimension(400, Math.min(VISIBLE_ROWS*ROW_HEIGHT, data.length*ROW_HEIGHT));

			setPreferredSize(dim);
			setSize(dim);
		}
		
		/**
		 * handles mouse events such as table row selection and graph display
		 */
		public void processMouseEvent(MouseEvent me) {
			if(me.isPopupTrigger())
				return;
				
			int row = table.getSelectedRow();
			int col = table.getSelectedColumn();
						
			if(col >= table.getColumnCount()-2) {		
				Color color = JColorChooser.showDialog(LinearExpressionGraphViewer.this, "Color Selection", (Color)(table.getValueAt(row,col)));
				if(color != null) {					
					table.setValueAt(color, row, col);
					getPreviewPanel(row).setColors(getLineColor(row), getMarkerColor(col));
					if(col == table.getColumnCount()-2) {
						//update the vector
						lineColorVector.setElementAt(color, row);
						//update the graph
						graph.setSampleLineColors(lineColorVector);
					} else {
						//update the vector
						markerColorVector.setElementAt(color, row);						
						//update the graph
						graph.setSampleMarkerColors(markerColorVector);
					}
					//repaint updated color scheme
					graph.getGraphComponent().repaint();
				}
				table.repaint();
			}
		}
		
		/**
		 * Returns the line color for a particular row
		 * @param row row index
		 * @return line color
		 */
		private Color getLineColor(int row) {
			return (Color)(table.getValueAt(row, table.getColumnCount()-2));
		}

		/**
		 * Returns the marker color for a particular row
		 * @param row row index
		 * @return marker color
		 */
		private Color getMarkerColor(int row) {
			return (Color)(table.getValueAt(row, table.getColumnCount()-1));
		}
		
		/**
		 * Returns the <code>LinePreview</code> for the given row
		 * @param row row index
		 * @return line style preview object
		 */
		private LinePreview getPreviewPanel(int row) {
			return ((LinePreview)(table.getValueAt(row, table.getColumnCount()-3)));
		}

		/**
		 * Returns the selected rows in the table
		 * @return row indices
		 */
		public int [] getSelectedRows() {
			return table.getSelectedRows();
		}
		
		/**
		 * sets the row selection
		 * @param start first row index
		 * @param end last row index
		 */
		public void setSelectedRows(int start, int end) {
			table.getSelectionModel().setSelectionInterval(start, end);
			table.repaint();
		}
		
		/**
		 * 
		 * @author braisted
		 *
		 * Custom table model.  Holds and delivers table data.
		 */
		public class SampleTableModel extends AbstractTableModel {

			//data
			private Object [][] data;
			//header names
			private Vector headerNames;
			
			/**
			 * Constructor
			 * @param data table data
			 * @param header header names
			 */
			public SampleTableModel(Object data [][], Vector header) {
				super();
				this.data = data;
				this.headerNames = header;
			}
			
			/**
			 * Returns the number of columns
			 */
			public int getColumnCount() {				
				if(data.length == 0)
					return 0;
				else
					return data[0].length;
			}

			/**
			 * Returns the row count
			 */
			public int getRowCount() {
				return data.length;
			}
			
			/**
			 * Returns the column name for col (column index)
			 */
			public String getColumnName(int col) {
				return (String)(headerNames.get(col));
			}

			/**
			 * Returns the value at the specified location
			 */
			public Object getValueAt(int rowIndex, int columnIndex) {
				return data[rowIndex][columnIndex];
			}
			
			/**
			 * Sets the value at the specified location
			 */
			public void setValueAt(Object obj, int row, int col) {
				data[row][col] = obj;
			}
			
			/**
			 * Returns true if the cell at row, col is editable
			 * (Color columns)
			 */
		    public boolean isCellEditable(int row, int col) {		    	
		    	//last two color columns are editable		    	
		    	return (col >= table.getColumnCount()-2);
		    }
		    
		    /**
		     * Returns the class of the specified column
		     */
		    public Class getColumnClass(int col) {
		    	if(col == headerNames.size()-3)
		    		return LinePreview.class;
		    	if(col >= headerNames.size()-2)
		    		return Color.class;		    	
		    	return String.class;	    	
		    }
			
		}
		
		/**
		 * 
		 * @author braisted
		 *
		 * Custom renderer for the table that can show Strings or line previews
		 */
		public class LEGTableCellRenderer implements TableCellRenderer {

			private JPanel panel;

			/**
			 * Constructs a renderer
			 */
			public LEGTableCellRenderer() {
				panel = new JPanel();
			}
			
			/**
			 * Retruns the renderer component, see TableCellRenderer for details
			 */
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
				
				//if it's a preview, set it selected, return value
				if(value instanceof LinePreview) {
					((LinePreview)value).setSelected(isSelected);
					return (LinePreview)value;
				}
				
				//if it's a color return it
				if(value instanceof Color) {
					panel.setBackground((Color)value);
					return panel;
				}
				
				//else return null (String cells handle themselves)
				return null;
			}			
		}
		
	
		/**
		 * 
		 * @author braisted
		 *
		 * LinePreview class extends JPanel and render the line and marker colors
		 * 
		 */
		public class LinePreview extends JPanel {
			private Color markerColor;
			private Color lineColor;
			private Color backgroundColor;
			private Color selectedBackgroundColor;
			private int x1, x2, y;
			private boolean selected;
			
			/**
			 * Constructs a LinePreview
			 * @param marker marker color
			 * @param line line color
			 * @param selBackgroundColor background color
			 */
			public LinePreview(Color marker, Color line, Color selBackgroundColor) {
				super();
				markerColor = marker; 
				lineColor = line;
				setBackground(Color.white);
				backgroundColor = Color.white;
				selectedBackgroundColor = selBackgroundColor;				
				this.setPreferredSize(new Dimension(90, 25));
				x1 = 10;
				x2 = 10;
				y = 10;
			}
			
			/**
			 * Sets marker color
			 * @param color marker color
			 */
			public void setMarkerColor(Color color) {
				markerColor = color;
			}
			
			/**
			 * Sets line color
			 * @param color line color
			 */
			public void setLineColor(Color color) {
				lineColor = color;
			}
			
			/**
			 * Sets line and marker color
			 * @param c1 line color
			 * @param c2 marker color
			 */
			public void setColors(Color c1, Color c2) {
				lineColor = c1;
				markerColor = c2;
			}
			
			/**
			 * Sets the selected field
			 * @param selected true if preview is selected
			 */
			public void setSelected(boolean selected) {
				this.selected = selected;
			}
			
			/**
			 * Renders the line preview
			 */
			public void paint(Graphics g) {
				if(!selected)
					setBackground(backgroundColor);
				else
					setBackground(selectedBackgroundColor);
				
				
				super.paint(g);
				Graphics2D g2 = (Graphics2D)g;
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				Dimension dim = this.getSize();
			
				g.setColor(lineColor);
				g.drawLine(x1, y, dim.width-x2, y);
				g.drawLine(x1, y+1, dim.width-x2, y+1);
				
				g.setColor(markerColor);
				g.fillOval(x1, y-1, 4,4);
				g.fillOval(dim.width - x2, y-1, 4, 4);								
			}
			
		}
	}
	
	/**
	 * 
	 * @author braisted
	 *
	 * The Graph class contains the LEMGraphViewer which actually displays the graph
	 * this JPanel extention serves as a conatainer that can be embedded into the viewer.
	 */
	public class Graph extends JPanel {
		private LEMGraphViewer graph;
		private JScrollBar horizGraphBar;		
		private boolean compressX;		
		JScrollPane pane;
		
		/**
		 * Constructs the Graph object
		 * @param fullExp the full experiment
		 * @param data expression data
		 * @param title graph's title
		 * @param locusNames list of locus names
		 * @param start start coordinates
		 * @param end end coordinates
		 */
		public Graph(Experiment fullExp, float [][] data, String title, String [] locusNames, int [] start, int [] end) {
			super(new GridBagLayout());
			setBackground(Color.white);
			
			Hashtable initProps = getInitialProperties();
			
			graph = new LEMGraphViewer(fullExp, data, title, initProps, locusNames, start, end);
			pane = new JScrollPane(graph);
			
			add(pane, new GridBagConstraints(0,0,1,1,1,1,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));

			int indices [] = new int[]{0};

			setGraphsToDisplay(indices);
			setBackground(Color.green);
		}
		
		/**
		 * Builds the initial properties for the viewer
		 * @return hashtable of properties
		 */
		private Hashtable getInitialProperties() {
			Hashtable props = new Hashtable();

			props.put("is-overlay-mode", new Boolean(false));
	    	props.put("y-range-mode", new Integer(GraphScaleCustomizationDialog.YRANGE_OPTION_DISPLAY_MENU));    		    	
	    	props.put("y-axis-symetry", new Boolean(false));
	    	
	    	props.put("show-x-axis", new Boolean(true));    	
	    	props.put("x-axis-color", Color.lightGray);
	    	props.put("x-axis-stroke", new BasicStroke(1f));
	    	
	    	props.put("offset-lines-mode", new Boolean(true));
	    	props.put("offset-graph-midpoint", new Float(0f));
  	
	    	props.put("offset-graph-min", new Float(0f));
	    	props.put("offset-graph-max", new Float(0f));
			props.put("show-discrete-overlay", new Boolean(false));				    	
				    	
			return props;
		}
		
		/**
		 * Sets the sample line colors
		 * @param lineColors line colors
		 */		
		public void setSampleLineColors(Vector lineColors) {
			graph.setSampleLineColors(lineColors);
		}

		/**
		 * Sets the marker colors
		 * @param markerColors
		 */
		public void setSampleMarkerColors(Vector markerColors) {
			graph.setSampleMarkerColors(markerColors);
		}
		
		/**
		 * sets the graphs to display
		 * @param selectedGraphs list of graph indices
		 */
		public void setGraphsToDisplay(int [] selectedGraphs) {
			graph.setGraphsToDisplay(selectedGraphs);
		}
		
		/**
		 * IViewer helper to pass on the LEMGraphViewer
		 * @param framework
		 */
		public void onSelected(IFramework framework) {
			graph.onSelected(framework);
		}
		
		/**
		 * returns the graph component
		 * @return LEMGraphViewer conponent
		 */
		public LEMGraphViewer getGraphComponent() {
			return graph;
		}
		
		/**
		 * Validates the viewer, following resizing
		 */
		public void validateView() {
			pane.setViewportView(graph);
			validate();
		}

		/**
		 * Removes the current graph component
		 * 
		 * often used during state changes
		 */
		public void removeGraphComponent() {
			pane.remove(graph);
		}

	}
	
	
	/**
	 * 
	 * @author braisted
	 *
	 * Listens to table events, updates viewer list to display
	 */
	public class TableListener extends MouseAdapter{
		public void mouseReleased(MouseEvent me) {
			graph.setGraphsToDisplay(table.getSelectedRows());
			table.processMouseEvent(me);
		}
	}
	
	
	/**
	 * 
	 * @author braisted
	 *
	 * Menu listener
	 */
	public class Listener extends MouseAdapter implements ActionListener {

		public void actionPerformed(ActionEvent ae) {
			String command = ae.getActionCommand();
		
			if(command.equals("overlay-mode-command")) {
				overlay = true;
				graph.getGraphComponent().enableOverlay(overlay);
				splitPane.validate();
				table.setSelectedRows(0,0); //select first row
				graph.setGraphsToDisplay(table.getSelectedRows());
				updateViewerModeView();
				framework.refreshCurrentViewer();
				framework.getFrame().validate();
				framework.getFrame().repaint();				
			} else if(command.equals("tile-mode-command")) {
				overlay = false;			
				graph.getGraphComponent().enableOverlay(overlay);				
				updateViewerModeView();
				framework.refreshCurrentViewer();
				framework.getFrame().validate();
				framework.getFrame().repaint();							
			} else if(command.equals("customize-graph-range-command")) {
				customizeGraph();
			} else if(command.equals("show-reference-line-command")) {
				showRefLine = !showRefLine;
				graph.getGraphComponent().toggleReferenceLine();
			} else if(command.equals("reset-x-range-command")) {
				graph.getGraphComponent().resetXRange();
			}
		}
		
		public void mouseReleased(MouseEvent me) {
			if(me.isPopupTrigger()) {
				popup.show(graph.getGraphComponent(), me.getX(), me.getY());	
				return;
			}
			
			int column = table.getSelectedRows()[0];
			
		}

		public void mousePressed(MouseEvent me) {
			if(me.isPopupTrigger())
				popup.show(graph.getGraphComponent(), me.getX(), me.getY());				
		}
	}


	/**
	 * @see org.tigr.microarray.mev.cluster.gui.IViewer#setExperiment(org.tigr.microarray.mev.cluster.gui.Experiment)
	 */
	public void setExperiment(Experiment e) {
		
	}



	/* (non-Javadoc)
	 * @see org.tigr.microarray.mev.cluster.gui.IViewer#getExperimentID()
	 */
	public int getExperimentID() {
		return this.exptID;
	}



	/* (non-Javadoc)
	 * @see org.tigr.microarray.mev.cluster.gui.IViewer#setExperimentID(int)
	 */
	public void setExperimentID(int id) {
		this.exptID = id;
	}


	
}
