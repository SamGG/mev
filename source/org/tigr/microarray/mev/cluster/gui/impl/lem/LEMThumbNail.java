/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
package org.tigr.microarray.mev.cluster.gui.impl.lem;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ToolTipManager;
import javax.swing.border.BevelBorder;

import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.impl.GUIFactory;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.ParameterPanel;
import org.tigr.util.FloatMatrix;

/**
 * @author braisted
 * 
 * LEMThumbNail is a dialog to support navigation within the LEM
 * Clicking on the Thumbnail moves the main display to that area
 * of the LEM.  Additional options allow navigation to specific
 * coordinates or specified locus.
 */
public class LEMThumbNail extends JDialog {
	
	private IFramework framework;
	private Canvas canvas;
	private ControlPanel navPanel;
	private JScrollPane pane;
	
	private LinearExpressionMapViewer parent;

	private int initHeight = 500;
	private int initWidth = 200;
	private int navInitWidth = 200;
	private int navInitHeight = 200;
	
	private int maxY = 0;
	
	private int [] startValues;
	private int [] endValues;
	private boolean [] isForward;
	private String [] locusIDs;
	private int [] strata;
	private int maxStrata;
	private boolean isFixedLength;
	
	public static final int TOP_WINDOW = 0;
	public static final int PREV_WINDOW = 1;
	public static final int NEXT_WINDOW = 2;
	public static final int END_WINDOW = 3;
	
	/**
	 * Constructs a LEMThumbNail, note that the LEM parent is included to support interaction
	 * @param framework framework object to act as a conduit outside LEM
	 * @param parent parent LEM
	 * @param startCoords array of start coordinates
	 * @param endCoords array of end coordinates
	 * @param fm FloatMatrix of locus values, passed on to canvas
	 * @param upperThr current upper threshold for locus display (bin)
	 * @param lowerThr current lower threshold for locus display (bin)
	 * @param upperColor upper bin color
	 * @param lowerColor lower bin color
	 * @param midColor midrange, neutral color
	 * @param parentHeight height of the actual LEM, used for scaling movement
	 * @param mainViewY1 initial Lower locus index extent of parent LEM
	 * @param mainViewY2 initial Upper locus index extent of parent LEM
	 * @param locusIDFieldName Locus id annotation field name 
	 */
	public LEMThumbNail(IFramework framework, LinearExpressionMapViewer parent, int [] startCoords, int [] endCoords, FloatMatrix fm, float upperThr, float lowerThr, Color upperColor, Color lowerColor, Color midColor, int parentHeight, int mainViewY1, int mainViewY2, String locusIDFieldName) {
		super(framework.getFrame(), "LEM Explorer", false);
		this.framework = framework;
		this.parent = parent;

		startValues = parent.getStartValues();
		endValues = parent.getEndValues();
		isForward = parent.getDirectionArray();
		locusIDs = parent.getLocusIDArray();
		strata = parent.getStrata();
		maxStrata = parent.getMaxStrata();
		isFixedLength = parent.areArrowLengthsFixed();
		
		Listener listener = new Listener();
		canvas = new Canvas(startCoords, endCoords, fm, parentHeight, mainViewY1, mainViewY2, listener);		
		navPanel = new ControlPanel(listener, locusIDFieldName);
		pane = new JScrollPane();
		pane.setViewportView(canvas);	
				
		Point parentLoc = parent.getParent().getLocationOnScreen();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	
		int h = Math.min(initHeight, screenSize.height - parentLoc.y);
		//take less but if too low set back to initHeight		
		setSizes(initWidth, h < 400 ? 400 : h);
		
		JPanel controlPanel = new JPanel();
		controlPanel.setLayout(new GridBagLayout());
		Dimension dim = new Dimension(initWidth, 50);
		controlPanel.setPreferredSize(dim);
		controlPanel.setSize(dim);

		JButton syncButton = new JButton("Update Overlay Window");
		syncButton.setFocusPainted(false);
		syncButton.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		syncButton.setActionCommand("sync-view-overlay-command");
		syncButton.addActionListener(listener);
		syncButton.setPreferredSize(new Dimension(150,30));
		syncButton.setSize(150,30);		
		controlPanel.add(syncButton, new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));
		
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new GridBagLayout());
		mainPanel.setBorder(BorderFactory.createLineBorder(Color.black));
	
		mainPanel.add(pane, new GridBagConstraints(0,0,1,1,1,0.8,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(3,3,0,3), 0,0));
		mainPanel.add(controlPanel, new GridBagConstraints(0,1,1,1,1,0.05,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,3,0,3), 0,0));
		mainPanel.add(navPanel, new GridBagConstraints(0,2,1,1,1,0.05,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,3,3,3), 0,0));
			
		getContentPane().add(mainPanel, BorderLayout.CENTER);
		navPanel.tabs.setSelectedIndex(1);
		pack();
		navPanel.tabs.setSelectedIndex(0);
	}
	
	/**
	 * Displays the thumbnail to the left of MeV's View panel, if that puts it on screen
	 * or else center
	 */
	public void showThumbnail() {		
		Point parentLoc = parent.getParent().getLocationOnScreen();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		
		if(parentLoc != null && parentLoc.x-getWidth() > 0 && parentLoc.y > 0) {
			setLocation(parentLoc.x-getWidth(), parentLoc.y-parent.getHeaderComponent().getHeight());       			
		} else {		
			setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);       
		}
		show();
	}
	
	/**
	 * Disposes of the LEMThumbNail dialog
	 *
	 */
	public void hideThumbnail() {
		dispose();
	}
	
	/**
	 * Set boolean to indicate if locus arrows are of fixed length or variable
	 * Required to help navigation of the parent LEM
	 * @param fixed true if arrow lengths are fixed
	 */
	public void setIsFixedLength(boolean fixed) {
		this.isFixedLength = fixed;
	}
	
	/** 
	 * Sets the size of the pane and canvase
	 * @param w width
	 * @param h height
	 */
	private void setSizes(int w, int h) {
		setSize(w,h);
		pane.setSize(w, h - this.navInitHeight);
		pane.setPreferredSize(new Dimension(w, h-this.navInitHeight));
		canvas.setSize(w,this.locusIDs.length+1);
		canvas.setPreferredSize(new Dimension(w, this.locusIDs.length));
	}
	
	/**
	 * Updates the coordinate valies and current parent height
	 * @param start list of start locations
	 * @param end list of end locations
	 * @param parentHeight parent height
	 */
	public void updateCoords(int [] start, int [] end, int parentHeight) {		
		canvas.updateCoords(start, end, parentHeight);	
	}
	
	/**
	 * Jumps to indiated location, y is the y pixel on the parent
	 * @param y
	 */
	private void jumpToLocation(int y) {
		if(y > maxY || !parent.isVisible())
			return;
		
		int [] yClipBounds = parent.getYClipBounds();

		float loc = (float)Math.min(canvas.start[y], canvas.end[y]);
		framework.setContentLocation(0, (int)(loc));
		
		canvas.viewerY1 = y;			
		canvas.viewerY2 = y + Math.round(maxY * ((yClipBounds[1]- yClipBounds[0])/(float)canvas.parentHeight));
		canvas.repaint();
	}	

	/**
	 * Jumps to the designated location on the main viewer
	 * @param y y-location on the parent viewer
	 */
	private void jumpToMainViewerLocation(int y) {	
		int [] yClipBounds = parent.getYClipBounds();

		//don't jump if the full viewer is in screen
		if(yClipBounds[1] - yClipBounds[0] > canvas.parentHeight)
			return;
		
		framework.setContentLocation(0, y);
		
		//need to set canvas overlay bounds
		canvas.viewerY1 = (int)(maxY*(y/(float)canvas.parentHeight));			
		canvas.viewerY2 = canvas.viewerY1 + Math.round(maxY * ((yClipBounds[1]- yClipBounds[0])/(float)canvas.parentHeight));
		canvas.repaint();
	}
	
	/**
	 * Scrolls the navigator pane to top
	 */
	private void scrollPaneToStart() {
		pane.getViewport().setViewPosition(new Point(0, 0));
		pane.validate();
	}
	
	/**
	 * Scrolls the navigator pane to the end
	 */
	private void scrollPaneToEnd() {
		pane.getViewport().setViewPosition(new Point(0, canvas.getHeight()-(canvas.viewerY2-canvas.viewerY2)));		
		pane.validate();
	}
	
	/**
	 * Advances parent lem and Thumbnail boundary indicator one
	 * viewable window specified by next, previous, or end cases
	 * @param mode
	 */
	private void windowJump(int mode) {
		int [] yClipBounds = parent.getYClipBounds();
		switch(mode) {
		case LEMThumbNail.TOP_WINDOW:
			jumpToMainViewerLocation(0);
			scrollPaneToStart();
			break;
		case LEMThumbNail.NEXT_WINDOW:
			if(yClipBounds[1] >= canvas.parentHeight)
				jumpToLocation(0);  //wrap to start
			else 
				jumpToMainViewerLocation(yClipBounds[1]);
			break;
		case LEMThumbNail.PREV_WINDOW:
			if(yClipBounds[0] < yClipBounds[1]-yClipBounds[0])
				windowJump(LEMThumbNail.END_WINDOW); //wrap to end
			else
				jumpToMainViewerLocation(2*yClipBounds[0] - yClipBounds[1]);
			break;
		case LEMThumbNail.END_WINDOW:
			jumpToMainViewerLocation(canvas.parentHeight - (yClipBounds[1]-yClipBounds[0]));
			scrollPaneToEnd();
			break;
		}
	}
	
	/**
	 * Advances lem and thumbnail to a specified location determined in a dialog
	 */
	private void jumpToBasePairLocataion(){	
		int loc = navPanel.getSelectedLocation();
		
		if(loc < 0) {
			JOptionPane.showMessageDialog(LEMThumbNail.this, "Improper BP location value.", "Format Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		int [] yClipBounds = parent.getYClipBounds();
		
		int viewerY = parent.jumpToLocation(loc);				
		//if found, move thumbnail overlay
		if(viewerY >= 0) {
			int y = Math.round(maxY*(viewerY/(float)canvas.parentHeight));
			canvas.viewerY1 = y;			
			canvas.viewerY2 = y + Math.round(maxY * ((yClipBounds[1]- yClipBounds[0])/(float)canvas.parentHeight));
			canvas.repaint();
		}
	}
	
	/**
	 * Advances LEM and thumbnail to a specified locus location
	 */
	private void jumpToLocusLocation() {
		String locusID = navPanel.getLocusID();
		
		int [] yClipBounds = parent.getYClipBounds();
		
		int viewerY = parent.jumpToLocus(locusID);
		//if found, move thumbnail overlay		
		if(viewerY >= 0) {
			int y = Math.round(maxY*(viewerY/(float)canvas.parentHeight));
			canvas.viewerY1 = y;			
			canvas.viewerY2 = y + Math.round(maxY * ((yClipBounds[1]- yClipBounds[0])/(float)canvas.parentHeight));
			canvas.repaint();
		}		
	}
	
	
	/**
	 * Coordinates overlay (indicator rectangle on thumbnail) to current
	 * viewable area on canvas
	 */
	private void syncOverlayPosition() {
		int [] yClipBounds = parent.getYClipBounds();
		canvas.viewerY1 = Math.round(yClipBounds[0]*(maxY/(float)canvas.parentHeight));		
		canvas.viewerY2 = canvas.viewerY1 + Math.round(maxY * ((yClipBounds[1]- yClipBounds[0])/(float)canvas.parentHeight));
		canvas.repaint();
	}
	
	

	/**
	 * 
	 * @author braisted
	 *
	 * The Canvas class provides a JPanel on which to render the thumbnail
	 * image of the LEM.
	 */
	public class Canvas extends JPanel {
		
		//Start and end arrays
		private int [] start;
		private int [] end;
	
		//values to show
		private FloatMatrix matrix;
		
		private int numSamples;
		
		//parent LEM's pixel height
		private int parentHeight;
		
		//canvas height
		private int currHeight;
		
		//cursor location
		private int cursorY;
		private boolean cursorOnTop = false;
		
		private int viewerY1 = 0;
		private int viewerY2 = 0;
		
		/**
		 * Constructs a canvas
		 * @param startCoords locus start locations
		 * @param endCoords locus end locations
		 * @param fm <code>FloatMatrix</code> of data values to show
		 * @param parentHeight initial height of the parent viewer
		 * @param y1 current upper visible limit in LEM
		 * @param y2 current lower visible limit in LEM
		 * @param listener event listener
		 */
		public Canvas(int [] startCoords, int [] endCoords, FloatMatrix fm, int parentHeight, int y1, int y2, Listener listener) {
			this.start = startCoords;
			this.end = endCoords;
			this.matrix = fm;
			this.parentHeight = parentHeight;
			this.numSamples = fm.getColumnDimension();
			initHeight = fm.getRowDimension();
			viewerY1 = (int)(initHeight*(y1/(float)parentHeight));
			viewerY2 = (int)(initHeight*(y2/(float)parentHeight));

			setBackground(Color.white);
			ToolTipManager.sharedInstance().registerComponent(this);			
			addMouseMotionListener(listener);
			addMouseListener(listener);
		}
		
		/**
		 * Provides tool tip text based on current cursor location
		 */
		public String getToolTipText() {			
			if(cursorY >= 0 && cursorY < start.length) {
				return locusIDs[cursorY]+", "+String.valueOf(startValues[cursorY]+", "+String.valueOf(endValues[cursorY]));
			}
			return null;
		}
		
		/**
		 * Updates coordinates and parrent height
		 * @param start start locations
		 * @param end end locations
		 * @param parentHeight parent height
		 */
		public void updateCoords(int [] start, int [] end, int parentHeight) {
			this.start = start;
			this.end = end;
			this.parentHeight = parentHeight;
			repaint();
		}

		
		/**
		 * renders the thumbnail
		 */
		public void paint(Graphics g) {
			super.paint(g);
			
			currHeight = getHeight();
			int currWidth = getWidth();
			float convFactor = (float)((float)currHeight/parentHeight);
			
			int xStep;
			if(!isFixedLength)
				xStep = (int)(currWidth/(numSamples * (maxStrata + 1)));
			else
				xStep = (int)(currWidth/numSamples);

			int elementInset = 0;
			int elementWidth = 0;
			if(xStep > 5) {
				elementInset = xStep/4;
				elementWidth = xStep - elementInset;
			}
			
			int currX;
			int currY = 0;
			int deltaY = 0;
			for(int i = 0; i < start.length; i++) {
				currX = 0;
	
				//try a fixed delta of 1
				deltaY = 1;
				
				//add an offset
				if(!isFixedLength)
					currX += strata[i]*xStep;
				
				for(int j = 0; j < numSamples; j++) {
					g.setColor(parent.getColor(matrix.get(i,j)));
					if(elementWidth > 0)
						g.fillRect(currX + elementInset, currY, elementWidth, deltaY);
					else				
						g.fillRect(currX, currY, xStep , deltaY);

					if(!isFixedLength)
						currX += xStep * (maxStrata+1);
					else
						currX += xStep;
				}
				currY += deltaY;
			}
			
			maxY = currY;
			
			g.setColor(Color.blue);			
			g.drawRect(0, viewerY1, currWidth, viewerY2-viewerY1);
			g.drawRect(1, viewerY1+1, currWidth-2, viewerY2-viewerY1-2);			
		}
	}
	
	
	/**
	 * @author braisted
	 *
	 * The control panel supports special navigation activities,
	 * locus navigation, location navigation, and window jumping
	 */
	public class ControlPanel extends JPanel {
	
		private JTextField idField;
		private JTextField locField;
		JTabbedPane tabs;
		
		/**
		 * Constructs the ControlPanel
		 * @param listener event listener
		 * @param locusIDFieldName locus annotation field name (label)
		 */
		public ControlPanel(Listener listener, String locusIDFieldName) {			
			tabs = new JTabbedPane();

			Dimension dim = new Dimension(navInitWidth, navInitHeight);
			
			tabs.setPreferredSize(dim);
			tabs.setSize(dim);
			
			ParameterPanel windowPanel = new ParameterPanel("Navigate by Window Steps");
			windowPanel.setLayout(new GridBagLayout());
			windowPanel.setPreferredSize(dim);
			windowPanel.setSize(dim);
			
			JButton prevWindowButton = new JButton("Prev.", GUIFactory.getIcon("nav_arrow_up.gif"));			
			prevWindowButton.setHorizontalTextPosition(JButton.LEFT);
			prevWindowButton.setFocusPainted(false);
			prevWindowButton.setActionCommand("jump-to-previous-command");
			prevWindowButton.addActionListener(listener);			
			
			JButton nextWindowButton = new JButton("Next", GUIFactory.getIcon("nav_arrow_down.gif"));
			nextWindowButton.setHorizontalTextPosition(JButton.LEFT);
			nextWindowButton.setFocusPainted(false);
			nextWindowButton.setActionCommand("jump-to-next-command");
			nextWindowButton.addActionListener(listener);			
			
			JButton topWindowButton = new JButton("Start", GUIFactory.getIcon("nav_arrow_top.gif"));
			topWindowButton.setHorizontalTextPosition(JButton.LEFT);
			topWindowButton.setFocusPainted(false);
			topWindowButton.setActionCommand("jump-to-top-command");
			topWindowButton.addActionListener(listener);	
			
			JButton bottomWindowButton = new JButton("End", GUIFactory.getIcon("nav_arrow_end.gif"));
			bottomWindowButton.setHorizontalTextPosition(JButton.LEFT);
			bottomWindowButton.setFocusPainted(false);
			bottomWindowButton.setActionCommand("jump-to-end-command");
			bottomWindowButton.addActionListener(listener);	
			
			//add components
			windowPanel.add(topWindowButton, new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,10,10),0,0));
			windowPanel.add(prevWindowButton, new GridBagConstraints(1,0,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,10,10,0),0,0));
			windowPanel.add(bottomWindowButton, new GridBagConstraints(0,1,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,10),0,0));
			windowPanel.add(nextWindowButton, new GridBagConstraints(1,1,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,10,0,0),0,0));
			
			tabs.add("Window Stepping", windowPanel);
			
			ParameterPanel idPanel = new ParameterPanel("Gene ID Navigation");
			idPanel.setLayout(new GridBagLayout());			
			idPanel.setPreferredSize(dim);
			idPanel.setSize(dim);

			JLabel geneIDLabel = new JLabel("Enter Gene ID:");			
			JLabel geneIDFieldLabel = new JLabel("("+locusIDFieldName+")");
			idField = new JTextField();
			JButton idGOButton = new JButton("Find Gene");
			idGOButton.setFocusPainted(false);
			idGOButton.setActionCommand("jump-to-id-command");
			idGOButton.setPreferredSize(new Dimension(120,30));
			idGOButton.setSize(120,30);
			idGOButton.addActionListener(listener);
			
			idPanel.add(geneIDLabel, new GridBagConstraints(0,0,1,1,0.5,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,5,0,5),0,0));
			idPanel.add(idField, new GridBagConstraints(1,0,1,1,1,0,GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
			idPanel.add(geneIDFieldLabel, new GridBagConstraints(0,1,2,1,1,0,GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0,5,5,0),0,0));
			idPanel.add(idGOButton, new GridBagConstraints(0,2,2,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0,0,0,0),0,0));
			
			tabs.add("Gene Locator", idPanel);
			
			ParameterPanel locPanel = new ParameterPanel("Location Navigation");
			locPanel.setLayout(new GridBagLayout());
			locPanel.setPreferredSize(dim);
			locPanel.setSize(dim);

			JLabel locLabel = new JLabel("Base Pair Location:");			
			locField = new JTextField();
			JButton locButton = new JButton("Find Location");
			locButton.setFocusPainted(false);
			locButton.setActionCommand("jump-to-location-command");
			locButton.setPreferredSize(new Dimension(120,30));
			locButton.setSize(120,30);
			locButton.addActionListener(listener);			
			
			locPanel.add(locLabel, new GridBagConstraints(0,0,1,1,0.5,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,5,0,5),0,0));
			locPanel.add(locField, new GridBagConstraints(1,0,1,1,1,0,GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0,5,0,0),0,0));
			locPanel.add(locButton, new GridBagConstraints(0,1,2,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(10,0,0,0),0,0));
			
			tabs.add("Base Locator", locPanel);
			
			setLayout(new GridBagLayout());
			add(tabs, new GridBagConstraints(0,0,1,1,1,1,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
		}
		
		/**
		 * Returns the locus ID in text field
		 * @return locus id
		 */
		public String getLocusID() {
			return this.idField.getText();
		}
		
		/**
		 * Returns the selected location
		 * @return location to go to, (chromosomal location)
		 */
		public int getSelectedLocation() {
			int location = -1;
			try {
				location = Integer.parseInt(this.locField.getText());
			} catch (NumberFormatException nfe) {
				//pass on the -1 as a format exception;
			}			
			return location;			
		}
		
	}
	
	/**
	 * 
	 * @author braisted
	 *
	 * Listener class to support navigation events
	 */
	public class Listener extends MouseAdapter implements ActionListener, MouseMotionListener {
		
		public void mouseClicked(MouseEvent me) {
			jumpToLocation(me.getY());			
		}
		
		public void mouseEntered(MouseEvent me) {
			canvas.cursorOnTop = true;			
		}
		
		public void mouseExited(MouseEvent me) {
			canvas.cursorOnTop = false;
		}
		/* (non-Javadoc)
		 * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
		 */
		public void mouseDragged(MouseEvent e) {
			
		}
		
		/* (non-Javadoc)
		 * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
		 */
		public void mouseMoved(MouseEvent e) {
			canvas.cursorY = e.getY();
		}
		
		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent ae) {
			String command = ae.getActionCommand();
			if(command.equals("sync-view-overlay-command")) {
				syncOverlayPosition();
			} else if (command.equals("jump-to-top-command")) {
				windowJump(LEMThumbNail.TOP_WINDOW);
			} else if (command.equals("jump-to-next-command")) {
				windowJump(LEMThumbNail.NEXT_WINDOW);
			} else if (command.equals("jump-to-previous-command")) {
				windowJump(LEMThumbNail.PREV_WINDOW);
			} else if (command.equals("jump-to-end-command")) {
				windowJump(LEMThumbNail.END_WINDOW);
			} else if (command.equals("jump-to-location-command")) {			
				jumpToBasePairLocataion();
			} else if (command.equals("jump-to-id-command")) {			
				jumpToLocusLocation();
			}
		}
		
	}
	
}
