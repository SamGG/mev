/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/

package org.tigr.microarray.mev.cluster.gui.impl.lem;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.beans.Expression;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JViewport;

import org.tigr.microarray.mev.TMEV;
import org.tigr.microarray.mev.cluster.clusterUtil.Cluster;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IDisplayMenu;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.IViewer;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentUtil;
import org.tigr.util.FloatMatrix;

/**
 * @author braisted
 * 	 
 *  LinearExpressionMapViewer Description: Provides a multiple sample
 * expression view that is organized by chromosomal coordinates Each
 * chromosome, if there is more than one, will have a dedicated LEM in which
 * Loci are arranged based on coordinate information in a linear,
 * top-to-bottom fashion in the viewer. Samples are displayed in parallel
 * lines in the viewer.
 * 
 * Fixed arrow length indicates a more compressed view where sequence
 * lengths are not rendered based on length. In this mode all overlapping
 * sequences will be on the same line rather than offset and intergenic
 * spaces will be of uniform length.
 */
public class LinearExpressionMapViewer extends JPanel implements IViewer {

	/**
	 * Holds the reduced and sorted Experiment
	 */
	private Experiment experiment;

	/**
	 * Holds the full experiment (from IData, doesn't include trimmed elements)
	 */
	private Experiment fullExperiment;

	/**
	 * Holds Locus IDs, sorted by min. coord.
	 */
	private String[] sortedLocusIDs;

	/**
	 * Holds starting or lower coord. (the smaller of 5' or 3' coordinate)
	 */
	private int[] start;

	/**
	 * Holds ending or greater coord. (the greater of 5' or 3' coordinate)
	 */
	private int[] end;

	/**
	 * Holds replicate Experiment indices
	 */
	private int [][] replicates;
	
	/**
	 * Indicates direction of transcription relative to chr. coordinate system
	 */
	private boolean[] isForward;

	/**
	 * Holds information for rendering orfs that overlap
	 */
	private int[] strata;
	private String locusIDFieldName;
	
	private int maxStrata;

	private LEMHeader header;
		
	private int numberOfSamples;
	private int locusCount;

	private int bpPerPixel = 50;

	private IFramework framework;
	private IData data;
	
	//scaling options
	private boolean fixedLengthArrows = true;
	private boolean showOpenAreas = false;

	//arrow coordinate arrays for rendering
	private int[] x = new int[8];
	private int[] y = new int[8];

	private int minArrowLength = 15;
	private int maxArrowLength = 100;
	private int currArrowLength = 25;
	private int minArrowHead = 10;
	private int arrowWidth = 10;	
	private int currArrowHead;
	private int currArrowShank;
	private int wingWidth = 5;	
	private int maxIntergenicLength = 100;

	//column spacing is a function of maxStrata, fixedLengthArrows
	private int columnSpacing = 40;

	//pixel coordinate information, updated during scaling
	private int maxPixelCoord = 0;
	private int[] coordStarts;
	private int[] coordEnds;
	private int[] annYPos;

	private int currX;
	private int currY;
	
	private int X_ORIGIN = 20;
	private int Y_ORIGIN = 7;

	private int DEFAULT_COLUMN_SPACING = 30;
	private int DEFAULT_STRATA_SPACING = 20;

	//arrow spacing in y axis
	private int INTERGENIC_ESTIMATE = 5;
	private int CONTIG_ARROW_Y_SPACING = 2;
	private int NONCONTIG_ARROW_Y_SPACING = 10;

	private int COORD_LEFT_MARGIN = 25;
	private int COORD_SEPARATOR_MARGIN = 10;
	private int LEFT_LOCUS_MARGIN = 20;
	private int RIGHT_LOCUS_MARGIN = 30;
	private int RIGHT_MARGIN = 10;
	
	//annotation width (for X offset determination)
	private int fullAnnotationWidth;
	private int locusMaxWidth;
	private int startMaxWidth;
	private int endMaxWidth;
	private int annotationWidth;
	private int viewerWidth;
	private int fieldIndex = 0;

	//current color mode option
	private int colorMode;

	//color mode constants
	public static final int COLOR_MODE_GRADIENT = 0;
	public static final int COLOR_MODE_2_BIN = 1;
	public static final int COLOR_MODE_4_BIN = 2;

	//gradient images, grad. type flag, limits
	private BufferedImage posColorImage;
	private BufferedImage negColorImage;
	private boolean useDoubleGradient = true;

	//min and max for gradient option, set from main menu
	private float minValue = -3f;
	private float maxValue = 3f;
	private float midValue = 0f;	

	//bin colors
	private Color lowestColor = new Color(10, 159, 1);
	private Color lowerColor = new Color(187, 240, 181);
	private Color midPointColor = Color.white;
	private Color higherColor = new Color(243, 169, 160);
	private Color highestColor = Color.red;
	private Color missingColor = new Color(128, 128, 128);
	
	//bin cutoffs
	private float cutoff1 = -3f;
	private float cutoff2 = -1f;
	private float midBinValue = 0f;
	private float cutoff3 = 1f;
	private float cutoff4 = 3f;
	
	//highlight rendering fields
	private boolean highlighted = false;
	private int highlightStart;
	private int highlightEnd;
	private int highlightedIndex;
			
	//clip bound determination fields
	private int [][] boundingRanges;
	private int boundryCount = 20;
	private int clipY1;
	private int clipY2;

	private Vector activeInfoDialogs;
	
	//thumbnail and navigation aid
	private LEMThumbNail thumbnail;
	
	//Locus selection
	private Vector selectedIndicesVector;
	private LEMSelectionEditor selectionEditor;
	private boolean [] selected;

	private JPopupMenu menu;

	private boolean showAllReplicates = false;
	private int maxNumReps;
	private int maxEndBaseLocation;
	private int replicateSpacing = 22;
	private float replicateLengthFraction = 0.6f;
	
	//EH state-saving
	private int exptID;
	
	/* (non-Javadoc)
	 * @see org.tigr.microarray.mev.cluster.gui.IViewer#getExpression()
	 */
	public Expression getExpression(){
		return new Expression(this, this.getClass(), "new", 
				new Object[]{new Integer(exptID), fullExperiment.getMatrix(), experiment.getMatrix(), 
				fullExperiment.getColumns(), fullExperiment.getRows(), experiment.getColumns(), experiment.getRows(),
				this.sortedLocusIDs, this.start, this.end, this.replicates, this.isForward,
				this.strata, new String(""), this.locusIDFieldName});
	}
	
	public LinearExpressionMapViewer(Integer exptID, FloatMatrix fullExptFloatMatrix, FloatMatrix redExptFloatMatrix, 
			int[] fullCols, int[] fullRows, int[] redCols, int[] redRows,
			String[] sortedLocusIDs,
			int[] sortedStartArray, int[] sortedEndArray, int [][] replicates, boolean[] isForward,
			int[] strata, String chrID, String locusIDFieldName){
		this(new Experiment(fullCols, fullRows, exptID.intValue(), fullExptFloatMatrix), new Experiment(redCols, redRows, 0, redExptFloatMatrix), sortedLocusIDs,
				sortedStartArray, sortedEndArray, replicates, isForward,
				strata, chrID, locusIDFieldName);
	}
	/**
	 * LinearExpressionMapViewer Description: Provides a multiple sample
	 * expression view that is organized by chromosomal coordinates Each
	 * chromosome, if there is more than one, will have a dedicated LEM in which
	 * Loci are arranged based on coordinate information in a linear,
	 * top-to-bottom fashion in the viewer. Samples are displayed in parallel
	 * lines in the viewer.
	 * 
	 * Fixed arrow length indicates a more compressed view where sequence
	 * lengths are not rendered based on length. In this mode all overlapping
	 * sequences will be on the same line rather than offset and intergenic
	 * spaces will be of uniform length.
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
	public LinearExpressionMapViewer(Experiment fullExperiment,
			Experiment reducedExperiment, String[] sortedLocusIDs,
			int[] sortedStartArray, int[] sortedEndArray, int [][] replicates, boolean[] isForward,
			int[] strata, String chrID, String locusIDFieldName) {
		
		this.fullExperiment = fullExperiment;
		this.experiment = reducedExperiment;
		this.numberOfSamples = experiment.getNumberOfSamples();
		this.sortedLocusIDs = sortedLocusIDs;
		this.start = sortedStartArray;
		this.end = sortedEndArray;
		this.replicates = replicates;
		this.isForward = isForward;
		this.strata = strata;
		this.locusIDFieldName = locusIDFieldName;
		this.locusCount = sortedLocusIDs.length;

		//constrain bin size to 50 loci
		boundryCount = locusCount/50;
		
		this.activeInfoDialogs = new Vector();
		this.selectedIndicesVector = new Vector();
		this.selected = new boolean[locusCount];
		
		this.maxNumReps = 0;
		for(int i = 0; i < replicates.length; i++) {
			this.maxNumReps = Math.max(this.maxNumReps, replicates[i].length);
		}
			
		//constrain initial size of *scaled* map to 500-10000 pixels high.
		if ((int) (sortedEndArray[sortedEndArray.length - 1] / bpPerPixel) > 10000)
			bpPerPixel = (int) (sortedEndArray[sortedEndArray.length - 1] / 10000);
		else if ((int) (sortedEndArray[sortedEndArray.length - 1] / bpPerPixel) < 500)
			bpPerPixel = (int) (sortedEndArray[sortedEndArray.length - 1] / 500);
		if (bpPerPixel == 0)
			bpPerPixel = 1;

		//initializes for scaled representation
		coordStarts = new int[start.length];
		coordEnds = new int[end.length];
		annYPos = new int[sortedLocusIDs.length];

		this.boundingRanges = new int[boundryCount][4];
		
		updateCoords();

		maxStrata = 0;
		maxEndBaseLocation = 0;
		for (int i = 0; i < strata.length; i++) {
			if (strata[i] > maxStrata) {
				maxStrata = strata[i];				
			}
			//reverse search for largest end location
			if(maxEndBaseLocation < end[end.length-i-1])
				maxEndBaseLocation = end[end.length-i-1];
		}

		updateColumnSpacing();
		setBackground(Color.white);

		negColorImage = createGradient(lowestColor, midPointColor);
		posColorImage = createGradient(midPointColor, highestColor);

		updateLocusAnnotationWidth();

		colorMode = COLOR_MODE_GRADIENT;
		
		header = new LEMHeader(experiment);
		header.setLeftInset(this.X_ORIGIN); // -arrowWidth/2-wingWidth);
		header.setArrowWingWidth(wingWidth);
		header.setArrowWidth(arrowWidth);

		LEMListener listener = new LEMListener();
		createPopupMenu(listener);
		this.addMouseListener(listener);
		this.addMouseMotionListener(listener);
	}
	/**
	 * EH - State-saving constructor
	 */
	public LinearExpressionMapViewer(Integer exptID, Integer numberOfSamples, String[] sortedLocusIDs,
			int[] sortedStartArray, int[] sortedEndArray, int [][] replicates, boolean[] isForward,
			int[] strata, String chrID, String locusIDFieldName) {
		this.exptID = exptID.intValue();
		this.numberOfSamples = numberOfSamples.intValue();
		this.sortedLocusIDs = sortedLocusIDs;
		this.start = sortedStartArray;
		this.end = sortedEndArray;
		this.replicates = replicates;
		this.isForward = isForward;
		this.strata = strata;
		this.locusIDFieldName = locusIDFieldName;
		this.locusCount = sortedLocusIDs.length;

		//constrain bin size to 50 loci
		boundryCount = locusCount/50;
		
		this.activeInfoDialogs = new Vector();
		this.selectedIndicesVector = new Vector();
		this.selected = new boolean[locusCount];
		
		this.maxNumReps = 0;
		for(int i = 0; i < replicates.length; i++) {
			this.maxNumReps = Math.max(this.maxNumReps, replicates[i].length);
		}
			
		//constrain initial size of *scaled* map to 500-10000 pixels high.
		if ((int) (sortedEndArray[sortedEndArray.length - 1] / bpPerPixel) > 10000)
			bpPerPixel = (int) (sortedEndArray[sortedEndArray.length - 1] / 10000);
		else if ((int) (sortedEndArray[sortedEndArray.length - 1] / bpPerPixel) < 500)
			bpPerPixel = (int) (sortedEndArray[sortedEndArray.length - 1] / 500);
		if (bpPerPixel == 0)
			bpPerPixel = 1;

		//initializes for scaled representation
		coordStarts = new int[start.length];
		coordEnds = new int[end.length];
		annYPos = new int[sortedLocusIDs.length];

		this.boundingRanges = new int[boundryCount][4];
		
		updateCoords();

		maxStrata = 0;
		maxEndBaseLocation = 0;
		for (int i = 0; i < strata.length; i++) {
			if (strata[i] > maxStrata) {
				maxStrata = strata[i];				
			}
			//reverse search for largest end location
			if(maxEndBaseLocation < end[end.length-i-1])
				maxEndBaseLocation = end[end.length-i-1];
		}

		updateColumnSpacing();
		setBackground(Color.white);

		negColorImage = createGradient(lowestColor, midPointColor);
		posColorImage = createGradient(midPointColor, highestColor);

		updateLocusAnnotationWidth();

		colorMode = COLOR_MODE_GRADIENT;
		

	}
	
	public Experiment getExperiment(){
		return experiment;
	}
	
	/**
	 * @see org.tigr.microarray.mev.cluster.gui.IViewer#setExperiment(org.tigr.microarray.mev.cluster.gui.Experiment)
	 */
	public void setExperiment(Experiment e) {
	}



	
	/**
	 * Paints the graphics context of the viewer
	 */
	public void paint(Graphics g) {
		super.paint(g);
		
		Font font = g.getFont();
		Font boldFont = new Font(font.getFontName(), Font.BOLD, font.getSize());
		FontMetrics fm = g.getFontMetrics();
		
		Graphics2D g2 = (Graphics2D) g;
		
		Object obj = this.getParent();
		Rectangle visRect = ((JViewport)obj).getViewRect();
		
		//obtain the visible rectangle to get clip bounds
		if(visRect != null) {
			clipY1 = visRect.y;
			clipY2 = clipY1+visRect.height;
		} else {
			visRect = g.getClipBounds();
			if(visRect != null) {
				clipY1 = visRect.y;
				clipY2 = clipY1+visRect.height;			
			} else {
				clipY1 = 0; 
				clipY2 = getHeight();
			}			
		}
		
		//clip boundry sections
		int [] bounds = getBoundingIndices(visRect.y, visRect.y+visRect.height);
		
		g.setColor(Color.blue);
		
		//hiding replicates		
		if(!this.showAllReplicates) {			
			
			currX = X_ORIGIN + this.arrowWidth/2 + this.wingWidth;
			currY = Y_ORIGIN;
			
			//highlighter
			if(highlighted) {
				Composite composite = g2.getComposite();					        
				g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f));
				
				if(fixedLengthArrows)
					g2.fillRect(0, highlightStart, visRect.width, highlightEnd - highlightStart);
				else {
					g2.setColor(Color.blue);
					g2.fillRect(0, highlightStart, columnSpacing*numberOfSamples+wingWidth+arrowWidth/2+COORD_LEFT_MARGIN/2, highlightEnd - highlightStart);
					g2.setColor(Color.black);
					g2.fillRect(columnSpacing*numberOfSamples+wingWidth+arrowWidth/2+COORD_LEFT_MARGIN/2, annYPos[highlightedIndex]- fm.getHeight(), this.viewerWidth-columnSpacing*numberOfSamples, 20);//visRect.width-columnSpacing*numberOfSamples, fm.getHeight()+fm.getDescent());			
				}
				g2.setComposite(composite);
			}
			
			//lines 
			for (int j = 0; j < this.numberOfSamples; j++) {
				if (visRect.y < 10)
					g2.fillRoundRect(currX - 5, 0, 10, 5, 3, 3);				
				
				g.drawLine(currX, visRect.y, currX, visRect.y + visRect.height);
				currX += columnSpacing;
			}
			
			for (int i = bounds[0]; i <= bounds[1]; i++) {
				
				//render selection markers
				if(selected[i]) {
					Color color = g.getColor();
					g.setColor(Color.red);
					g.fillRoundRect(4, coordStarts[i] + 2, 8, coordEnds[i] - coordStarts[i] - 4, 2, 1);
					g.setColor(Color.black);				
					g.drawRoundRect(4, coordStarts[i] + 2, 8, coordEnds[i] - coordStarts[i] - 4, 2, 2);
					g.setColor(color);
				}
				
				currX = X_ORIGIN + this.arrowWidth/2 + this.wingWidth;
				
				if(!fixedLengthArrows)
					currX += strata[i] * DEFAULT_STRATA_SPACING;
				
				//render arrows
				for (int j = 0; j < this.numberOfSamples; j++) {
					renderArrow(g2, this.experiment.get(i, j), currX,
							this.coordStarts[i], this.coordEnds[i], isForward[i], (highlighted && i == highlightedIndex));
					currX += columnSpacing;
				}
				
				//if strata is shifted, then offset the annotaiton
				if(!fixedLengthArrows)
					currX += (this.maxStrata - strata[i]) * DEFAULT_STRATA_SPACING;
				
				//render locus id and annotation field
				if(!selected[i])
					g.setColor(Color.black);			
				else
					g.setColor(Color.red);
				
				//render annotation
				renderAnnotation(g, i, currX, font, boldFont);				
			}
			
			//untangle overlap issues by rendering highlighted text on top
			if(!fixedLengthArrows && highlighted) {
				blockSelectedTextArea(g2, currX, font);
				
				if(!selected[highlightedIndex])
					g.setColor(Color.black);			
				else
					g.setColor(Color.red);
				
				renderAnnotation(g, this.highlightedIndex, currX, font, boldFont);
			}
			
		} else {
			
			currX = X_ORIGIN + this.arrowWidth/2 + this.wingWidth;
			currY = Y_ORIGIN;
			
			int repYIndent = (currArrowLength - (int)(this.currArrowLength * replicateLengthFraction))/2;
			
			//background shading
			Composite composite = g2.getComposite();					        
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f));
			Color color = g.getColor();
			g.setColor(Color.yellow);
			for(int j = 0; j < this.numberOfSamples; j++) {
				if(j%2 == 0) {
					g.fillRect(currX-this.wingWidth-this.arrowWidth/2-5, visRect.y, columnSpacing, visRect.y + visRect.height);
				}
				currX += columnSpacing;
			}
			g.setColor(color);
			g2.setComposite(composite);
						
			//reset currX
			currX = X_ORIGIN + this.arrowWidth/2 + this.wingWidth;
			
			//lines 
			for (int j = 0; j < this.numberOfSamples; j++) {
				if (visRect.y < 10)
					g2.fillRoundRect(currX - 5, 0, 10, 5, 3, 3);								
				g.drawLine(currX, visRect.y, currX, visRect.y + visRect.height);				
				currX += columnSpacing;				
			}			
			
			//highlighter
			if(highlighted) {
				composite = g2.getComposite();					        
				g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f));
				if(fixedLengthArrows)
					g2.fillRect(0, highlightStart, visRect.width, highlightEnd - highlightStart);
				else {
					g2.fillRect(0, highlightStart, columnSpacing*numberOfSamples, highlightEnd - highlightStart);
					g2.fillRect(columnSpacing*numberOfSamples, annYPos[highlightedIndex]- fm.getHeight(), visRect.width-columnSpacing*numberOfSamples, fm.getHeight()+fm.getDescent());			
				}
				g2.setComposite(composite);
			}
			
			for (int i = bounds[0]; i <= bounds[1]; i++) {
				
				currX = X_ORIGIN + this.arrowWidth/2 + this.wingWidth;
				
				for (int j = 0; j < this.numberOfSamples; j++) {
					renderArrow(g2, this.experiment.get(i, j), currX,
							this.coordStarts[i], this.coordEnds[i], isForward[i], false);
					
					for(int k = 0; k < replicates[i].length; k++) {
						renderArrow(g2, this.fullExperiment.get(replicates[i][k], j), 
								currX + ((k+1) * replicateSpacing), coordStarts[i]+repYIndent, coordEnds[i]-repYIndent, isForward[i], false);
					}					
					currX += columnSpacing;
				}				
				currX += maxNumReps * replicateSpacing;
				
				//render locus id and annotation field
				g.setColor(Color.black);											
				renderAnnotation(g, i, currX, font, boldFont);
				
				if(selected[i]) {
					color = g.getColor();
					g.setColor(Color.red);
					g.fillRoundRect(4, coordStarts[i] + 2, 8, coordEnds[i] - coordStarts[i] - 4, 2, 1);
					g.setColor(Color.black);				
					g.drawRoundRect(4, coordStarts[i] + 2, 8, coordEnds[i] - coordStarts[i] - 4, 2, 2);
					g.setColor(color);
				}
			}						
		}
	}

	/**
	 * Renders annotatation for the locus index (i).
	 * @param g Graphics object
	 * @param i Locus index
	 * @param currX x pixel location to start annoation
	 * @param font current font
	 * @param boldFont bold font
	 */
	private void renderAnnotation(Graphics g, int i, int currX, Font font, Font boldFont) {
		if (isForward[i]) {
			g.drawString(String.valueOf(start[i]), currX - columnSpacing
					+ COORD_LEFT_MARGIN, annYPos[i]);
			g.drawString(String.valueOf(end[i]), currX - columnSpacing + this.COORD_LEFT_MARGIN
					+ startMaxWidth + this.COORD_SEPARATOR_MARGIN, annYPos[i]);
		} else {
			g.drawString(String.valueOf(end[i]),
					currX - columnSpacing + this.COORD_LEFT_MARGIN, annYPos[i]);
			g.drawString(String.valueOf(start[i]), currX - columnSpacing
					+ this.COORD_LEFT_MARGIN + startMaxWidth + this.COORD_SEPARATOR_MARGIN, annYPos[i]);
		}
		
		g.setFont(boldFont);
		g.drawString(sortedLocusIDs[i],
				currX - columnSpacing + this.COORD_LEFT_MARGIN + startMaxWidth + this.COORD_SEPARATOR_MARGIN
				+ endMaxWidth + this.LEFT_LOCUS_MARGIN, annYPos[i]);
		g.setFont(font);
		
		
		g.drawString(data.getElementAttribute(fullExperiment.getGeneIndexMappedToData(replicates[i][0]), fieldIndex),
				currX - columnSpacing + this.COORD_LEFT_MARGIN + startMaxWidth 
				+ this.COORD_SEPARATOR_MARGIN + endMaxWidth + this.LEFT_LOCUS_MARGIN + this.locusMaxWidth + this.RIGHT_LOCUS_MARGIN
				,annYPos[i]);

	}

	/**
	 * Used to bring hightlighted text to foreground when loci are scaled and overlap can occur
	 * @param g2 Graphics2D object
	 * @param currX current x cooridnate for rendering
	 * @param font current font
	 */
	private void blockSelectedTextArea(Graphics2D g2, int currX, Font font) {
		FontMetrics fm = g2.getFontMetrics();
		Color color = g2.getColor();

		//set background to white
		g2.setColor(Color.white);
		g2.fillRect(columnSpacing*numberOfSamples+wingWidth+arrowWidth/2+COORD_LEFT_MARGIN/2, annYPos[highlightedIndex]- fm.getHeight(), this.viewerWidth-columnSpacing*numberOfSamples, 20);//visRect.width-columnSpacing*numberOfSamples, fm.getHeight()+fm.getDescent());					
		
		Composite composite = g2.getComposite();					        
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f));

		g2.setColor(Color.black);
		g2.fillRect(columnSpacing*numberOfSamples+wingWidth+arrowWidth/2+COORD_LEFT_MARGIN/2, annYPos[highlightedIndex]- fm.getHeight(), this.viewerWidth-columnSpacing*numberOfSamples, 20);//visRect.width-columnSpacing*numberOfSamples, fm.getHeight()+fm.getDescent());					
			
		g2.setComposite(composite);

		//diagnostic test
		g2.drawRect(columnSpacing*numberOfSamples+wingWidth+arrowWidth/2+COORD_LEFT_MARGIN/2, annYPos[highlightedIndex]- fm.getHeight(), this.viewerWidth-columnSpacing*numberOfSamples, 20);//visRect.width-columnSpacing*numberOfSamples, fm.getHeight()+fm.getDescent());					
		
		g2.setColor(color);
	}

	
	/**
	 * Updates the spacing between sample columns.  This is updated when rendering
	 * and scaling options change.
	 */
	private void updateColumnSpacing() {
		if(this.fixedLengthArrows && !this.showAllReplicates)
			columnSpacing = DEFAULT_COLUMN_SPACING;
		else
			columnSpacing = DEFAULT_STRATA_SPACING * maxStrata + DEFAULT_COLUMN_SPACING;		

		if(this.showAllReplicates)
			columnSpacing = DEFAULT_COLUMN_SPACING + this.maxNumReps * this.replicateSpacing;
	}

	/**
	 * Renders a locus arrow
	 * @param g graphics 2D object
	 * @param value locus expression value
	 * @param xCenter x coordinate for center of arrow
	 * @param yStart y coordinate for arrow start
	 * @param yEnd y coordinate for arrow end
	 * @param forward indicates arrow is forward (down), or reverse (up)
	 * @param highlighted Indicates if the arrow is highlighted
	 */
	public void renderArrow(Graphics2D g, float value, int xCenter, int yStart,
			int yEnd, boolean forward, boolean highlighted) {
		Color startColor = g.getColor();
		currArrowHead = minArrowHead;
		currArrowShank = yEnd - yStart - currArrowHead;
		g.setColor(getColor(value));

		if (forward) {
			x[0] = xCenter;
			y[0] = yStart;
			x[1] = xCenter + arrowWidth / 2;
			y[1] = yStart;
			x[2] = xCenter + arrowWidth / 2;
			y[2] = yStart + currArrowShank;

			x[3] = xCenter + arrowWidth / 2 + wingWidth;
			y[3] = yStart + currArrowShank;
			x[4] = xCenter;
			y[4] = yStart + currArrowShank + currArrowHead;
			x[5] = xCenter - arrowWidth / 2 - wingWidth;
			y[5] = yStart + currArrowShank;
			x[6] = xCenter - arrowWidth / 2;
			y[6] = yStart + currArrowShank;
			x[7] = xCenter - arrowWidth / 2;
			y[7] = yStart;
		} else {
			x[0] = xCenter;
			y[0] = yEnd;
			x[1] = xCenter + arrowWidth / 2;
			y[1] = yEnd;
			x[2] = xCenter + arrowWidth / 2;
			y[2] = yEnd - currArrowShank;

			x[3] = xCenter + arrowWidth / 2 + wingWidth;
			y[3] = yEnd - currArrowShank;
			x[4] = xCenter;
			y[4] = yEnd - currArrowShank - currArrowHead;
			x[5] = xCenter - arrowWidth / 2 - wingWidth;
			y[5] = yEnd - currArrowShank;
			x[6] = xCenter - arrowWidth / 2;
			y[6] = yEnd - currArrowShank;
			x[7] = xCenter - arrowWidth / 2;
			y[7] = yEnd;
		}
		g.fillPolygon(x, y, 8);
		g.setColor(Color.BLACK);
		g.drawPolygon(x, y, 8);
		
		
		//render anchors if posible over lap
		if(!fixedLengthArrows) { 
			if(highlighted)
				g.setColor(Color.red);
			else
				g.setColor(Color.white);
			g.fillOval(xCenter-1, (yStart+yEnd)/2-1, 4, 4);
			g.setColor(Color.black);
			g.drawOval(xCenter-2, (yStart+yEnd)/2-2, 4, 4);
		}
		
		g.setColor(startColor);
	}


	/**
	 * Updates the pixel coordinates and the boundry array for cliping rendering support
	 * These coordinates are used to define locus arrow positions for rendering and are updated
	 * when scaling or rendering options are changed
	 */
	private void updateCoords() {

		int currY = Y_ORIGIN;
		int fontAscent = 0;

		Graphics g = getGraphics();
		if (g != null) {
			FontMetrics fm = g.getFontMetrics();
			fontAscent = fm.getAscent();
		}
		
		maxPixelCoord = 0;

		if (this.fixedLengthArrows && !showOpenAreas) {
			for (int i = 0; i < this.coordStarts.length; i++) {
				coordStarts[i] = currY;
				coordEnds[i] = currY + currArrowLength;
				currY = coordEnds[i] + CONTIG_ARROW_Y_SPACING;
				annYPos[i] = coordStarts[i] + (coordEnds[i] - coordStarts[i])/2 + fontAscent/2;
				maxPixelCoord = Math.max(maxPixelCoord, coordEnds[i]);
			}
		} else if (this.fixedLengthArrows && showOpenAreas) {
			coordStarts[0] = currY;
			coordEnds[0] = currY + currArrowLength;
			annYPos[0] = coordStarts[0] + (coordEnds[0] - coordStarts[0]) / 2;
			currY = coordEnds[0];

			for (int i = 1; i < this.coordStarts.length; i++) {
				if (start[i] - end[i - 1] > INTERGENIC_ESTIMATE) {
					coordStarts[i] = currY + NONCONTIG_ARROW_Y_SPACING;
					currY = coordStarts[i];
				} else
					coordStarts[i] = currY;

				coordEnds[i] = currY + currArrowLength;
				currY = coordEnds[i] + CONTIG_ARROW_Y_SPACING;
				annYPos[i] = coordStarts[i] + (coordEnds[i] - coordStarts[i])
						/ 2 + fontAscent / 2;

				maxPixelCoord = Math.max(maxPixelCoord, coordEnds[i]);
			}
		} else if (!this.fixedLengthArrows && showOpenAreas) {
			int offSet = 0;
			int oldEnd;
			int intergenicLength, diff;
			
			for (int i = 0; i < this.coordStarts.length; i++) {

				coordStarts[i] = offSet + start[i] / this.bpPerPixel + Y_ORIGIN;
				coordEnds[i] = offSet + end[i] / this.bpPerPixel + Y_ORIGIN;

				if (coordEnds[i] - coordStarts[i] < minArrowLength) {
					oldEnd = coordEnds[i];
					coordEnds[i] = coordStarts[i] + minArrowLength;

					//offset subtracts old length and adds in minArrowLength
					offSet += minArrowLength - (oldEnd - coordStarts[i]);
				}
				
				//consider intergenic length max when showing open areas
				//if not at i == 0 check coordStarts[i] vs coordEnds[i-1]
				//relative to the max intergenic length
				//if greater then subtract a constant from start and end
				//(sub. amount that exceedes intergenic max)
				//should only occur on strata == 0
				
				if(i > 0) {
					intergenicLength = coordStarts[i]-coordEnds[i-1];
					if( intergenicLength > this.maxIntergenicLength) {
						coordStarts[i] -= (intergenicLength - maxIntergenicLength);
						coordEnds[i] -= (intergenicLength - maxIntergenicLength);												
						offSet -= (intergenicLength-maxIntergenicLength);						
					}
				}
				
				annYPos[i] = coordStarts[i] + (coordEnds[i] - coordStarts[i])
						/ 2 + fontAscent / 2;
				
				maxPixelCoord = Math.max(maxPixelCoord, coordEnds[i]);	
			}
						
		} else if (!this.fixedLengthArrows && !showOpenAreas) {

			int oldEnd;
			
			for (int i = 0; i < this.coordStarts.length; i++) {
				coordStarts[i] = currY;// + start[i] / this.bpPerPixel;
				coordEnds[i] = currY + (end[i]-start[i]) / this.bpPerPixel;				
				if (coordEnds[i] - coordStarts[i] < minArrowLength) {
					coordEnds[i] = coordStarts[i] + minArrowLength;
					currY += minArrowLength+CONTIG_ARROW_Y_SPACING;
				} else {
					currY += coordEnds[i]-coordStarts[i] + CONTIG_ARROW_Y_SPACING;					
				}				
				annYPos[i] = coordStarts[i] + (coordEnds[i] - coordStarts[i])/ 2 + fontAscent / 2;								
				maxPixelCoord = Math.max(maxPixelCoord, coordEnds[i]);			
			}
		}
		
		//set viewer width
		viewerWidth = X_ORIGIN+columnSpacing*numberOfSamples-arrowWidth/2-wingWidth+fullAnnotationWidth;
			
		setPreferredSize(new Dimension(viewerWidth, maxPixelCoord + 5));
		setSize(viewerWidth, maxPixelCoord + 5);		
	
		setBoundingRanges();

		if(this.thumbnail != null) {
			this.thumbnail.updateCoords(coordStarts, coordEnds, maxPixelCoord);
		}
	}

	/**
	 * Sets the boundry ranges based on number of boundaries and the range of pixel coordinates
	 * These boundaries indicate locus indices that fall within the bounds of bins within the LEM
	 * Using these bins can speed the search for specific loci index ranges that bound a y coordinate
	 */
	private void setBoundingRanges() {		
		int chunkSize = (int)Math.ceil((this.maxPixelCoord+5)/(float)this.boundryCount);
		int [] range;
		for(int boundry = 0; boundry < boundingRanges.length; boundry++) {
			boundingRanges[boundry][0] = chunkSize*boundry;
			boundingRanges[boundry][1] = chunkSize*(boundry+1);
			range = getLociCoverage(chunkSize*boundry, chunkSize*(boundry+1));
			boundingRanges[boundry][2] = range[0];
			boundingRanges[boundry][3] = range[1];
		}
	}

	/**
	 * Returns the array of loci thata cover the [y2,y1] pixel range
	 * @param y1 upper y coord
	 * @param y2 lower y coord
	 * @return
	 */
	private int [] getLociCoverage(int y1, int y2) {
		int [] lowHigh = new int[2];
		lowHigh[0] = findNextLowestStart(y1);
		lowHigh[1] = findNextHighestStart(y2);		
		return lowHigh;		
	}

	/**
	 * recursive search for loci that contains the passed coord
	 * @param coord y coordinate
	 * @param index locus index
	 * @param start start index
	 * @param end end index
	 * @return
	 */
	public int findIndex(int coord, int index, int start, int end) {
		if(start == end)
			return start;
		if(coordStarts[index] > coord)		
			return findIndex(coord, start, (index-start)/2+start, index);
		else
			return findIndex(coord, index, (end-index)/2+start, end);
	}

	/**
	 * Returns the array of locus start values (low end values, sorted)
	 * @return
	 */
	public int [] getStartValues() {
		return this.start;
	}
	
	/**
	 * Returns the mean float matrix ordered by loci location
	 * @return
	 */
	public FloatMatrix getLocusMeanMatrix() {
		return this.experiment.getMatrix();
	}
	
	/**
	 * Returns the array of locus end values (higher end values)
	 * @return
	 */
	public int [] getEndValues() {
		return this.end;
	}
	
	/**
	 * Returns the array that indicates locus direction
	 * true == foward (down in LEM), false == reverse (up in LEM)
	 * @return
	 */
	public boolean [] getDirectionArray() {
		return this.isForward;
	}

	/**
	 * Return the array of locus ids
	 * @return
	 */
	public String [] getLocusIDArray() {
		return this.sortedLocusIDs;
	}
	
	/**
	 * Returns the strata array, each entry cooresponds to the strata of the loci
	 * @return
	 */
	public int [] getStrata() {
		return this.strata;
	}
	
	/**
	 * Returns the maximum locus strata in the viewer
	 * Strata are offset levels when loci overlap
	 * @return
	 */
	public int getMaxStrata() {
		return this.maxStrata;
	}
	
	/**
	 * Returns true if the given locus index is selected
	 * @param locusIndex
	 * @return
	 */
	public boolean isLocusSelected(int locusIndex) {
		return selected[locusIndex];
	}
	
	/**
	 * Returns true if arrow lengths are fixed
	 * @return
	 */
	public boolean areArrowLengthsFixed() {
		return this.fixedLengthArrows;
	}
	
	/**
	 * returns the locus id related to the locus index
	 * @param index locus Index
	 * @return
	 */
	public String getLocusID(int index) {
		return sortedLocusIDs[index];
	}

	/**
	 *  Returns the 5' end of the slocus (referenced by the locus index)
	 * @param index locus index
	 * @return
	 */
	public int getStart(int index) {
		return start[index];
	}

	/**
	 *  Returns the 5' end of the locus (referenced by the locus index)
	 * @param index locus index
	 * @return
	 */
	public int getEnd(int index) {
		return end[index];
	}

	/**
	 * Returns the number of spots that map to a locus index
	 * @param index locus index
	 * @return
	 */
	public int getNumReplicates(int index) {
		return replicates[index].length;
	}

	/**
	 * returns selected indices
	 * @return selected selected locus indices
	 */
	public int [] getSelectedIndices() {
		if(this.selectedIndicesVector.size() == 0) {
			return new int[0];
		}
		
		int [] indices = new int[selectedIndicesVector.size()];
		
		for(int i  = 0; i < indices.length; i++) {
			indices[i] = ((Integer)(this.selectedIndicesVector.get(i))).intValue();
		}
		return indices;
	}
	
	/**
	 * Stores spots that map to selected loci to the cluster manager
	 */
	private void storeSelectedLociSpotsToCluster() {
		int [] locusIndices = getSelectedIndices();
		
		if(locusIndices.length == 0) {
			JOptionPane.showMessageDialog(framework.getFrame(), "There are no loci selected.  Shift+Left Click to select.", "Empty Locus Selection List", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		
		int spotCount = 0;
		for(int i = 0; i < locusIndices.length; i++) {
			spotCount += replicates[locusIndices[i]].length;
		}
		int [] spotIndices = new int[spotCount];
		int cnt = 0;
		for(int i = 0; i < locusIndices.length; i++) {
			for(int j = 0; j < replicates[locusIndices[i]].length; j++) {
				spotIndices[cnt] = replicates[locusIndices[i]][j];
				cnt++;
			}
		}		
		framework.storeSubCluster(spotIndices, fullExperiment, Cluster.GENE_CLUSTER);
	}
	
	/**
	 * saves spots that map to selected loci
	 */
	private void saveSelectedLociSpots() {
		int [] indices = getSelectedIndices();
		
		if(indices.length == 0) {
			JOptionPane.showMessageDialog(framework.getFrame(), "There are no loci selected.  Shift+Left Click to select.", "Empty Locus Selection List", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		
		saveSpotsForLocusList(indices);
	}

	
	/**
	 * Saves the spots related to loci in the selected list to file (spot detial)
	 * @param locusIndices array of locus indices to save
	 */
	public void saveSpotsForLocusList(int locusIndices[]) {
		int spotCount = 0;
		for(int i = 0; i < locusIndices.length; i++) {
			spotCount += replicates[locusIndices[i]].length;
		}
		int [] spotIndices = new int[spotCount];
		int cnt = 0;
		for(int i = 0; i < locusIndices.length; i++) {
			for(int j = 0; j < replicates[locusIndices[i]].length; j++) {
				spotIndices[cnt] = replicates[locusIndices[i]][j];
				cnt++;
			}
		}
		
		try {
			ExperimentUtil.saveExperiment(framework.getFrame(), fullExperiment, data, spotIndices);
		} catch (Exception e) {
            JOptionPane.showMessageDialog(framework.getFrame(), "Cannot save cluster!", e.toString(), JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }		
	}
	
	/**
	 * Saves all loci to a matrix file.
	 */
	private void saveLocusMatrix() { 		
		JFileChooser chooser = new JFileChooser(TMEV.getFile("data"));

		if(chooser.showSaveDialog(framework.getFrame()) == JFileChooser.APPROVE_OPTION) {
			try {
				File file = chooser.getSelectedFile();
				BufferedWriter bw = new BufferedWriter(new FileWriter(file));
				
				Vector sampleFieldNames = data.getSampleAnnotationFieldNames();

				String headerLine = this.locusIDFieldName + "\tSpots/Locus\t" + "5' End\t" + "3' End\t";
				String fieldName;
				
				
				for(int i = 0; i < this.numberOfSamples; i++) {
					headerLine += data.getSampleAnnotation(i, (String)(sampleFieldNames.get(0)));
					if(i < numberOfSamples-1)
						headerLine += "\t";
					else
						headerLine += "\n";
				}
				
				bw.write(headerLine);
				
				if(sampleFieldNames.size() > 0) {
					
					//add addional headers
					for(int i = 1; i < sampleFieldNames.size(); i++) {
						fieldName = (String)(sampleFieldNames.get(i));
						headerLine = "\t\t\t"+fieldName+"\t";
						
						for(int j = 0; j < numberOfSamples; j++) {
							headerLine += data.getSampleAnnotation(j, fieldName);

							if(j < numberOfSamples-1)
								headerLine += "\t";
							else
								headerLine += "\n";							
						}
						bw.write(headerLine);
					}					
				}
				
				String line;
				int fivePrime, threePrime;
				
				for(int i = 0; i < sortedLocusIDs.length; i++) {

					//locus name
					line = sortedLocusIDs[i] + "\t" + String.valueOf(replicates[i].length) + "\t";

					//5' and 3' info
					if(this.isForward[i]) {
						fivePrime = this.start[i];
						threePrime = this.end[i];
					} else {
						fivePrime = this.end[i];
						threePrime = this.start[i];						
					}

					line += String.valueOf(fivePrime) + "\t" + String.valueOf(threePrime) + "\t";

					for(int j = 0; j < numberOfSamples; j++) {
						//add locus means
						line += String.valueOf(experiment.get(i,j));
						if(j < numberOfSamples-1)
							line += "\t";
						else
							line += "\n";							
					}
					bw.write(line);
				}
				bw.flush();
				bw.close();
			} catch (IOException ioe) {
	            JOptionPane.showMessageDialog(framework.getFrame(), "Cannot save loci!", ioe.toString(), JOptionPane.ERROR_MESSAGE);
	            ioe.printStackTrace();				
			} 
			
		}
		
	}
	
	
	/**
	 * Saves selecte loci to file
	 **/
	public void saveSelectedLoci() {
				
		int [] indices = getSelectedIndices();
		
		if(indices.length == 0) {
			JOptionPane.showMessageDialog(framework.getFrame(), "There are no loci selected.  Shift+Left Click to select.", "Empty Locus Selection List", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		
		for(int i  = 0; i < indices.length; i++) {
			indices[i] = ((Integer)(this.selectedIndicesVector.get(i))).intValue();
		}		
		saveLocusList(indices);		
	}
	
	
	/**
	 * Saves the list of loci to file
	 * @param locusIndices array of locus indices
	 */
	public void saveLocusList(int locusIndices[]) {
		JFileChooser chooser = new JFileChooser(TMEV.getFile("data"));
		
		if(chooser.showSaveDialog(framework.getFrame()) == JFileChooser.APPROVE_OPTION) {
			try {
				File file = chooser.getSelectedFile();
				BufferedWriter bw = new BufferedWriter(new FileWriter(file));
				
				Vector sampleFieldNames = data.getSampleAnnotationFieldNames();
				
				String headerLine = this.locusIDFieldName + "\tSpots/Locus\t" + "5' End\t" + "3' End\t";
				
				String fieldName;
				
				
				for(int i = 0; i < this.numberOfSamples; i++) {
					headerLine += data.getSampleAnnotation(i, (String)(sampleFieldNames.get(0)));
					if(i < numberOfSamples-1)
						headerLine += "\t";
					else
						headerLine += "\n";
				}
				
				bw.write(headerLine);
				
				if(sampleFieldNames.size() > 0) {
					
					//add addional headers
					for(int i = 1; i < sampleFieldNames.size(); i++) {
						fieldName = (String)(sampleFieldNames.get(i));
						headerLine = "\t\t\t"+fieldName+"\t";
						
						for(int j = 0; j < numberOfSamples; j++) {
							headerLine += data.getSampleAnnotation(j, fieldName);
							
							if(j < numberOfSamples-1)
								headerLine += "\t";
							else
								headerLine += "\n";							
						}
						bw.write(headerLine);
					}					
				}
				
				String line;
				
				for(int i = 0; i < locusIndices.length; i++) {
					
					//locus name
					line = sortedLocusIDs[locusIndices[i]] + "\t" + String.valueOf(replicates[locusIndices[i]].length) + "\t";
					
					int fivePrime, threePrime;
					
					//5' and 3' info
					if(this.isForward[i]) {
						fivePrime = this.start[i];
						threePrime = this.end[i];
					} else {
						fivePrime = this.end[i];
						threePrime = this.start[i];						
					}
					
					line += String.valueOf(fivePrime) + "\t" + String.valueOf(threePrime) + "\t";
					
					
					for(int j = 0; j < numberOfSamples; j++) {
						//add locus means
						line += String.valueOf(experiment.get(locusIndices[i],j));
						if(j < numberOfSamples-1)
							line += "\t";
						else
							line += "\n";							
					}
					bw.write(line);
				}
				bw.flush();
				bw.close();
			} catch (IOException ioe) {
				JOptionPane.showMessageDialog(framework.getFrame(), "Cannot save loci!", ioe.toString(), JOptionPane.ERROR_MESSAGE);
				ioe.printStackTrace();				
			} 			
		}		
	}

	
	/**
	 * opens a url related to the passed locus index
	 * @param index locus index
	 */
	public void linkToURL(int index) {	    
		JFrame frame = (JFrame)(JOptionPane.getFrameForComponent(this));        	    	
		ExperimentUtil.linkToURL(frame, fullExperiment, data, fullExperiment.getGeneIndexMappedToData(this.replicates[index][0]), locusIDFieldName, ExperimentUtil.lastSelectedAnnotationIndices);      	    
	}

	/**
	 * Returns the locus name for the give locus index
	 * @param index locus index
	 * @return
	 */
	public String getLocusName(int index) {
		if(index > this.sortedLocusIDs.length-1)
			return sortedLocusIDs[0];
		if(index < 0)
			return sortedLocusIDs[sortedLocusIDs.length-1];

		return sortedLocusIDs[index];
	}

	/**
	 * Returns the array of indices for spots that map to the provided locus index
	 * @param index locus index
	 * @return array of spots that map to the locus (indices map to full experiment)
	 */
	public int [] getReplicatesArray(int index) {
		if(index > this.replicates.length-1)
			return replicates[0];
		if(index < 0)
			return replicates[replicates.length-1];
		return replicates[index];
	}
	
	/** Updates viewer configuration
	 *  Updated fields are interdependent so that the minimum number
	 *  of fields is altered depending on selections.
	 * @param props
	 */
	public void setViewerSettings(Properties props) {		
		this.fixedLengthArrows = Boolean.valueOf(props.getProperty("fixed-arrows")).booleanValue();

		if(fixedLengthArrows) {
			this.currArrowLength = Integer.parseInt(props.getProperty("fixed-arrow-length"));
		} else {
			this.bpPerPixel = Integer.parseInt(props.getProperty("scaling-factor"));		
			this.minArrowLength = Integer.parseInt(props.getProperty("min-arrow-length"));
			this.maxArrowLength = Integer.parseInt(props.getProperty("max-arrow-length"));
		}		
		this.showOpenAreas = ! (Boolean.valueOf(props.getProperty("fixed-open")).booleanValue());
		if(this.showOpenAreas)
			this.maxIntergenicLength = Integer.parseInt(props.getProperty("max-open-length"));
		this.showAllReplicates = Boolean.valueOf(props.getProperty("show-replicates")).booleanValue();		
		updateViewer();
	}

	
	/**
	 *  Returns true if base coordinates is in a locus
	 * @param coord base location
	 * @return
	 */
	private boolean isOpen(int coord) {
		int index = 0;
		while (index < start.length && start[index] < coord) {
			if (start[index] < coord && end[index] > coord)
				return true;
		}
		return false;
	}
	
	/**
	 * sets the bin policy, informs header, repaints LEM and navigator
	 * @param mode
	 */
	private void setColorBinPolicy(int mode) {
		this.colorMode = mode;
		this.header.setColorBinPolicy(mode);
		header.repaint();
		repaint();
		if(thumbnail != null && thumbnail.isVisible()) {
			thumbnail.repaint();
		}		
	}


	/** Returns the content of the viewer
	 * 
	 */	
	public JComponent getContentComponent() {
		return this;
	}

 
	/**
	 * Returns the header
	 */
	public JComponent getHeaderComponent() {
		return header;
	}

 
	/**
	 * Returns a the row header, null for this viewer
	 */
	public JComponent getRowHeaderComponent() {
		return null;
	}

	/**
	 * Returns the corner component, null for this viewer
	 */
	public JComponent getCornerComponent(int cornerIndex) {
		return null;
	}

	/**
	 * Prepares the viewer for viewing when selected 
	 */
	public void onSelected(IFramework framework) {
		this.framework = framework;
		
		data = framework.getData();
		IDisplayMenu menu = framework.getDisplayMenu();
		
		updateLocusAnnotationWidth();
		updateCoords();
		
		this.maxValue = menu.getMaxRatioScale();
		this.minValue = menu.getMinRatioScale();
		this.midValue = menu.getMidRatioValue();
		this.posColorImage = menu.getPositiveGradientImage();
		this.negColorImage = menu.getNegativeGradientImage();
		this.useDoubleGradient = menu.getUseDoubleGradient();
		this.header.setNegAndPosColorImages(this.negColorImage, this.posColorImage);
		this.header.setUseDoubleGradient(useDoubleGradient);
		this.header.setValues(minValue, midValue, maxValue);
		
		int index = this.fieldIndex;
		this.fieldIndex = menu.getLabelIndex();
		
		if(index != fieldIndex) //change ann
			this.updateLocusAnnotationWidth();
		
		header.setData(data);
		header.setColumnSpacing(columnSpacing);		
		header.updateSizes(viewerWidth, columnSpacing);		
		
		if(this.selectionEditor == null)
			this.selectionEditor = new LEMSelectionEditor((JFrame)framework.getFrame(), this, selectedIndicesVector);		
	}

	/**
	 * Updates the header with current <code>IData</code>
	 */
	public void onDataChanged(IData data) {
		header.setData(data);		
	}
	
	/**
	 * Updates the viewer, header, and thumbnail (if visible), during a menu change.
	 */
	public void onMenuChanged(IDisplayMenu menu) {
        this.maxValue = menu.getMaxRatioScale();
        this.minValue = menu.getMinRatioScale();
        this.midValue = menu.getMidRatioValue();
        this.posColorImage = menu.getPositiveGradientImage();
        this.negColorImage = menu.getNegativeGradientImage();
        this.useDoubleGradient = menu.getUseDoubleGradient();
        int index = fieldIndex;
        this.fieldIndex = menu.getLabelIndex();
        if(index != fieldIndex) //change ann
        	this.updateLocusAnnotationWidth();

        this.header.setNegAndPosColorImages(this.negColorImage, this.posColorImage);
        this.header.setUseDoubleGradient(useDoubleGradient);
        this.header.setValues(minValue, midValue, maxValue);
        this.repaint();
		
        if(thumbnail != null && thumbnail.isVisible()) {
			thumbnail.repaint();
		}
	}

	public void onDeselected() {

	}

	public void onClosed() {
	}

	public BufferedImage getImage() {
		return null;
	}

	/**
	 * Returns null for this viewer, clusters do not apply
	 */
	public int[][] getClusters() {
		return null;
	}

	/**
	 * Returns the viewer type, gene or sample 
	 */	
	public int getViewerType() {
		return 0;
	}

	/**
	 * Computes annoation width and sets global variables related to component width
	 *
	 */
	private void updateLocusAnnotationWidth() {
		Graphics g = getGraphics();
		if (g == null)
			return;
		FontMetrics fm = g.getFontMetrics();
		if (fm == null)
			return;
		
		int currLocusWidth = 0;
		int currStartWidth = 0;
		int currEndWidth = 0;
		int currAnnWidth = 0;
		
		int maxLocusWidth = 0;
		int maxStartWidth = 0;
		int maxEndWidth = 0;
		int maxAnnWidth = 0;
				
		for (int i = 0; i < this.locusCount; i++) {

			//locus name width
			currLocusWidth = fm.stringWidth(this.sortedLocusIDs[i]);
			if (currLocusWidth > maxLocusWidth)
				maxLocusWidth = currLocusWidth;
		
			//start width
			currStartWidth = fm.stringWidth(this.isForward[i] ? String
					.valueOf(this.start[i]) : String.valueOf(this.end[i]));
			if (currStartWidth > maxStartWidth)
				maxStartWidth = currStartWidth;
		
			//end width
			currEndWidth = fm.stringWidth(this.isForward[i] ? String
					.valueOf(this.end[i]) : String.valueOf(this.start[i]));
			if (currEndWidth > maxEndWidth)
				maxEndWidth = currEndWidth;
			
			currAnnWidth = fm.stringWidth(data.getElementAttribute(fullExperiment.getGeneIndexMappedToData(replicates[i][0]), fieldIndex));
			if(currAnnWidth > maxAnnWidth)
				maxAnnWidth = currAnnWidth;
		}
		
		startMaxWidth = maxStartWidth;
		endMaxWidth = maxEndWidth;
		locusMaxWidth = maxLocusWidth;
		annotationWidth = maxAnnWidth;

		//add in factor for margins and for second field
		this.fullAnnotationWidth = this.COORD_LEFT_MARGIN + startMaxWidth + this.COORD_SEPARATOR_MARGIN + endMaxWidth 
			+ LEFT_LOCUS_MARGIN + locusMaxWidth + RIGHT_LOCUS_MARGIN + annotationWidth;
	}

	/**
	 * Returns a color that cooresponds the expression value.  this depends on colorMode status
	 * @param value expression value
	 * @return
	 */
	public Color getColor(float value) {
		if (Float.isNaN(value)) {
			return missingColor;
		}
		Color color;
		float maximum;
		int colorIndex, rgb = 0;
		
		if (colorMode == COLOR_MODE_GRADIENT) {
			if (useDoubleGradient) {
				maximum = value < midValue ? this.minValue : this.maxValue;
				colorIndex = (int) (255 * (value - midValue) / (maximum - midValue));
				colorIndex = colorIndex > 255 ? 255 : colorIndex;
				rgb = value < midValue ? negColorImage.getRGB(255 - colorIndex,
						0) : posColorImage.getRGB(colorIndex, 0);
			} else {
				float span = this.maxValue - this.minValue;
				if (value <= this.minValue)
					colorIndex = 0;
				else if (value >= maxValue)
					colorIndex = 255;
				else
					colorIndex = (int) (((value - this.minValue) / span) * 255);

				rgb = posColorImage.getRGB(colorIndex, 0);
			}
			color = new Color(rgb);
		} else if (colorMode == COLOR_MODE_2_BIN) {
			if(value <= cutoff1)
				color = lowestColor;
			else if(value >= cutoff4)
				color = highestColor;
			else
				color = midPointColor;			
		} else { //four bin system
			if(value < cutoff1)
				color = lowestColor;
			else if(value >= cutoff4)
				color = highestColor;
			else if(value >= cutoff3)
				color = higherColor;
			else if(value < cutoff2)
				color = lowerColor;
			else
				color = midPointColor;						
		}
		return color;
	}

	/**
	 * Creates gradient images
	 * @param c1 Low end color
	 * @param c2 High end color
	 * @return
	 */
	protected BufferedImage createGradient(Color c1, Color c2) {
		BufferedImage image = (BufferedImage) java.awt.GraphicsEnvironment
				.getLocalGraphicsEnvironment().getDefaultScreenDevice()
				.getDefaultConfiguration().createCompatibleImage(256, 1);
		Graphics2D graphics = image.createGraphics();
		GradientPaint gp = new GradientPaint(0, 0, c1, 255, 0, c2);
		graphics.setPaint(gp);
		graphics.drawRect(0, 0, 255, 1);
		return image;
	}
	
	/**
	 * Updates viewer settings following option changes
	 * -updates: column spacing, loci pixel coordinates, header column spacing
	 * header size, updates thumbnail/navigator, fires repaintng.
	 */
	private void updateViewer() {
		updateColumnSpacing();
		updateCoords();
		header.setColumnSpacing(columnSpacing);
		header.updateSizes(this.getWidth(), columnSpacing);
		repaint();
		header.repaint();
		if(thumbnail != null && thumbnail.isVisible()) {
			thumbnail.setIsFixedLength(this.fixedLengthArrows);
			thumbnail.repaint();
		}		
	}

	/**
	 * Toggles the optino to scale loci
	 */
	private void setScaleLoci() {
		this.fixedLengthArrows = !this.fixedLengthArrows;
		updateViewer();
	}
	
	/**
	 * Toggles the option to scale intergenic (open) regions	 *
	 */
	private void setScaleIntergenic() {
		this.showOpenAreas = !this.showOpenAreas;		
		updateViewer();
	}

	/**
	 * Checks location and shades the locus (highlights) if appropriate.
	 * Sets highlighted index and end an start coordinates
	 * @param x
	 * @param y
	 */
	private void shadeLoci(int x, int y) {
		if(x > X_ORIGIN && x < columnSpacing*numberOfSamples+this.wingWidth+this.arrowWidth/2) { 
			highlightedIndex = this.findNearestArrowMidPoint(y);
			if( highlightedIndex != -1) {
				setStatusText(highlightedIndex);
				highlightStart = coordStarts[highlightedIndex];
				highlightEnd = coordEnds[highlightedIndex];				
				highlighted = true;
				repaint();
			} else {
				//on lem but off locus
				setStatusText(-1);
				highlighted = false;
				repaint();
			}
		} else {
			setStatusText(-1);
			highlighted = false;
			repaint();
		}
	}

	/**
	 * Sets the highlighted locus name in the status bar, or default string in no locus is highlighted
	 * @param index locus index
	 */
	private void setStatusText(int index) {
		if(index == -1)
			framework.setStatusText(" TIGR MultiExperiment Viewer");
		else
			framework.setStatusText(" "+this.locusIDFieldName+": "+this.sortedLocusIDs[highlightedIndex]);
	}
	
	/**
	 * Finds teh closest locus anchor
	 * @param yCoord mouse y pixel coordinate
	 * @return
	 */
	private int findNearestArrowMidPoint(int yCoord) {		
		int minDeltaIndex = -1;
		int delta = Integer.MAX_VALUE;
		
		//get a collection of loci to interrogate				
		int [] bounds = this.getBoundingIndices(yCoord, yCoord);
		
		if(bounds[1]-bounds[0] <= 0)
			return minDeltaIndex;
				
		for(int i = bounds[0]; i <= bounds[1]; i++) {
			if((coordStarts[i] <= yCoord && coordEnds[i] >= yCoord)) {
				if(delta > Math.abs(yCoord - (coordEnds[i] + coordStarts[i])/2)) {
					minDeltaIndex = i;
					delta = Math.abs(yCoord - (coordEnds[i] + coordStarts[i])/2);
				}
			}
		}						
		return minDeltaIndex;
	}

	/** Launches a <code>LEMColorRangeSelector</code> to collect range and color information	 * 
	 */
	private void setBinColorRanges() {
		
		LEMColorRangeSelector selector = new LEMColorRangeSelector((JFrame)framework.getFrame(), this,
				this.lowestColor, this.lowerColor, this.higherColor,
				this.highestColor, this.cutoff1, this.cutoff2, this.midBinValue, this.cutoff3,
				this.cutoff4);

		selector.showModal();		
	}
	
	/**
	 * Public method to apply bin limits and colors
	 * @param c1
	 * @param c2
	 * @param mid
	 * @param c3
	 * @param c4
	 * @param color1
	 * @param color2
	 * @param color3
	 * @param color4
	 */
	public void setBinLimitsAndColors(float c1, float c2, float mid, float c3, float c4,
			Color color1, Color color2, Color color3, Color color4) {

		this.cutoff1 = c1;
		this.cutoff2 = c2;
		this.midBinValue = mid;
		this.cutoff3 = c3;
		this.cutoff4 = c4;

		this.lowestColor = color1;
		this.lowerColor = color2;
		this.midPointColor = Color.white;
		this.higherColor = color3;
		this.highestColor = color4;			

		header.setBinColors(lowestColor, lowerColor, higherColor, highestColor);
		header.setBinCutoffs(cutoff1, cutoff2, midBinValue, cutoff3, cutoff4);
		
		header.repaint();
		repaint();
		if(thumbnail != null && thumbnail.isVisible()) {
			thumbnail.repaint();
		}		
	}

	/**
	 * Shows locus information for the highlighted locus
	 * @param x x mouse coordinate
	 * @param y y mouse coordinate
	 */
	private void showInfo(int x, int y) {
		if(x > X_ORIGIN && x < columnSpacing*numberOfSamples) {
			int index = this.findNearestArrowMidPoint(y);
			
			if(index != -1) {
				launchInfo(index);
			}
		}			
	}

	
	/**
	 * Launches a <code>LocusInfoDialog</code> for the specified locus index
	 * @param index
	 */
	private void launchInfo(int index) {		
		LocusInfoDialog info = new LocusInfoDialog( (JFrame)(framework.getFrame()), this, sortedLocusIDs[index], index, experiment, fullExperiment, framework.getData(), replicates[index]); 
		this.activeInfoDialogs.add(info);
		info.showInfo();		
	}
	
	
	/**
	 * Removes the locus info dialog from the active dialog list
	 * @param info <code>LocusInfoDialog</code> object to remove
	 */
	public void removeInfoViewer(LocusInfoDialog info) {
		this.activeInfoDialogs.remove(info);
	}
	

	/**
	 * Displays the thumbnail navigation controller
	 *
	 */
	private void showThumbnail() {		
		//dispose of a current thumbnail and show new
		if(thumbnail != null) {
			thumbnail.dispose();
			thumbnail = null;
		}	
		thumbnail = new LEMThumbNail(framework, this, coordStarts, coordEnds, experiment.getMatrix(), cutoff4, cutoff1, highestColor, lowestColor, midPointColor, maxPixelCoord, clipY1, clipY2, this.locusIDFieldName);
		thumbnail.showThumbnail();
	}
	
	
	/**
	 * Returns the y-clip bounds as a two member int array, low and high clip bounds
	 * @return
	 */
	public int [] getYClipBounds(){
		int [] yBounds = new int[2];
		yBounds[0] = clipY1;
		yBounds[1] = clipY2;
		return yBounds;
	}
		
		
	/**
	 * Find s the next start relative to the y coordinate
	 * @param y y pixel location
	 * @return
	 */
	private int findNextLowestStart(int y) {
		int index = 0, i;
		for(i = 0; i < this.coordStarts.length; i++) {
			if(coordStarts[i] <= y && coordEnds[i] >= y)
				return i;
			if(coordStarts[i] > y) {
				if(i > 0)
					return i-1;
				else
					return 0;
			}
		}
		
		if(i >= this.coordStarts.length)
			return coordStarts.length-1;
		
		return index;
	}

	/**
	 * returns the loci that starts after location y
	 * @param y y location value to start the search.
	 * @return
	 */
	private int findNextHighestStart(int y) {
		int index = 0, i;
		for(i = this.coordStarts.length-1; i >= 0; i--) {
			if(coordStarts[i] <= y && coordEnds[i] >= y)
				return i;
			if(coordStarts[i] < y) {
				if(i < coordStarts.length-1)
					return i+1;
				else
					return coordStarts.length-1;
			}
		}		
		if(i < 0)
			return 1;
		
		return index;
	}

	
	/**
	 * Returns the locus indices that bound the specified y range
	 * @param y1 lower range limit
	 * @param y2 upper range limit
	 * @return returns two (2) indices that bound the specified y range
	 */
	private int [] getBoundingIndices(int y1, int y2) {
		int [] bounds = new int[2];
		bounds[0] = 0;
		bounds[1] = coordStarts.length-1;

		boolean setHigh = false, setLow = false;
		
		for(int i = 0; i < boundingRanges.length; i++) {
			if(!setLow && y1 >= boundingRanges[i][0] && y1 < boundingRanges[i][1]) {
				bounds[0] = boundingRanges[i][2];
				setLow = true;
			}
			if(!setHigh && y2 >= boundingRanges[i][0] && y2 < boundingRanges[i][1]) {
				bounds[1] = boundingRanges[i][3];				
				setHigh = true;
			}
			if(setHigh && setLow)
				return bounds;			
		}
		return bounds;
	}


	/**
	 * Moves viewer to specified locus ID
	 * @param locus locus name
	 * @return returns the locus index or -1 if not found
	 */
	public int jumpToLocus(String locus) {
		int loc = -1;
		for(int i = 0; i < this.sortedLocusIDs.length; i++) {
			if(locus.equals(sortedLocusIDs[i])) {
				loc = annYPos[i]-15;
				if(loc < 0)
					loc = 0;
					
				shadeLoci(X_ORIGIN,loc);
				framework.setContentLocation(0, loc);				
				return loc;
			}
		}
		//if not found return -1
		return loc;
	}
	

	/**
	 * Moves viewer to indicated base location
	 * @param bp base location
	 * @return returns the locus index or -1 if none found
	 */
	public int jumpToLocation(int bp) {
		int loc = -1;
		for(int i = 0; i < this.start.length-1; i++) {
			if(bp >= start[i] && bp <= start[i+1]) {

				//if it's in a gene
				if(bp >= start[i] && bp <= end[i]) {
					loc = annYPos[i]-15;
					if(loc < 0)
						loc = 0;
						
					shadeLoci(X_ORIGIN, annYPos[i]);
					framework.setContentLocation(0, loc);				
					return loc;						
				} else {
					loc = annYPos[i]-15;
					if(loc < 0)
						loc = 0;
						
					shadeLoci(X_ORIGIN, annYPos[i]);
					shadeLoci(X_ORIGIN, annYPos[i+1]);
					framework.setContentLocation(0, loc);				
					return loc;											
				}
			}
		}
		//if not found return -1
		return loc;
	}
	
	/**
	 * Launches the selection editor (list) or if already visible it centers the dialog
	 */
	private void showSelectionList() {		
		if(!selectionEditor.isVisible())
			selectionEditor.showDialog();
		else
			selectionEditor.centerDialog();
	}
	

	/**
	 * selects the locus closes to the mouse y coordinate
	 * @param x
	 * @param yCoord
	 */
	public void selectLocus(int x, int yCoord) {		
		if(x > X_ORIGIN - wingWidth - this.arrowWidth/2 && x < columnSpacing*numberOfSamples + wingWidth + arrowWidth/2) {
			
			int index = this.findNearestArrowMidPoint(yCoord);

			if(index == -1)
				return;
			
			selected[index] = !selected[index];
			if(selected[index]) {						
				this.selectedIndicesVector.add(new Integer(index));
				this.selectionEditor.fireLocusAdded();
			} else {
				this.selectedIndicesVector.remove(new Integer(index));					
				this.selectionEditor.fireLocusRemoved();
			}			
			updateInfoViewers();			
			repaint();
		}
	}
	
	
	/**
	 * Launches a dialog to select a base range.  Selects all loci that overlap the range
	 */
	public boolean selectBaseRange() {		
		int startBase, endBase;
		boolean selectionMade = false;
		
		LEMRangeSelectionDialog dialog = new LEMRangeSelectionDialog((JFrame)framework.getFrame(), start[0], maxEndBaseLocation);
		
		if(dialog.showModal() == JOptionPane.OK_OPTION) {
			
			startBase = dialog.getLowerLimit();
			endBase = dialog.getUpperLimit();
			
			int loc = 0;
			Vector indices = new Vector();
			
			for(loc = 0; loc < start.length; loc++) {
				if(haveOverLap(loc, startBase, endBase)) {				
					indices.add(new Integer(loc));			
				}
			}
			
			for(int i = 0; i < indices.size(); i++) {
				loc = ((Integer)indices.get(i)).intValue();
				
				if(!selected[loc]) {
					selected[loc] = true;
					this.selectedIndicesVector.add(indices.get(i));
					this.selectionEditor.fireLocusAdded();				
				}			
			}		
						
			if(indices.size()>0)
				selectionMade = true;
				
			updateInfoViewers();
			repaint();								
		}
		return selectionMade;
	}
	
	/**
	 * Checks to see if the locus indicated by locusIndex overlaps the coordinate range
	 * @param locusIndex locus index to check
	 * @param startBase start base location
	 * @param endBase end base location
	 * @return
	 */
	private boolean haveOverLap(int locusIndex, int startBase, int endBase) {		
		return ( (start[locusIndex] >= startBase && start[locusIndex] <= endBase)
				|| (end[locusIndex] >= startBase && end[locusIndex] <= endBase)
				|| (startBase >= start[locusIndex] && startBase <= end[locusIndex]));								
	}
	
	/**
	 * Updates all information viewers that are present.  Each checks the selection
	 * status of it's displayed locus.
	 */
	private void updateInfoViewers() {
		for(int i = 0; i < this.activeInfoDialogs.size(); i++)
			((LocusInfoDialog)(this.activeInfoDialogs.get(i))).checkSelection();
	}

	/**
	 * Toggles selection status of a locus given a locus index
	 * @param locusIndex locus index to toggle status
	 */
	public void toggleSelectedLocus(int locusIndex) {
		selected[locusIndex] = !selected[locusIndex];
		if(!selected[locusIndex]) {
			selectedIndicesVector.remove(new Integer(locusIndex));
			this.selectionEditor.fireLocusRemoved();
		} else {
			selectedIndicesVector.add(new Integer(locusIndex));			
			this.selectionEditor.fireLocusAdded();
		}
		updateInfoViewers();
		repaint();
	}	
	
	/**
	 * Launches the dialog to customize viewer scaling constraints
	 */
	private void customizeSettings() {			
		LEMViewerAttributeDialog dialog = new LEMViewerAttributeDialog(framework.getFrame(), this,
				this.fixedLengthArrows, !this.showOpenAreas, this.currArrowLength,
				this.minArrowLength, this.maxArrowLength, this.maxIntergenicLength, 
				this.bpPerPixel, this.showAllReplicates);

		dialog.showModal();		
	}


	/**
	 * Creates popup menu and adds lem listener to menu items
	 * @param listener event handler
	 */
	private void createPopupMenu(LEMListener listener) {
		menu = new JPopupMenu();
		ButtonGroup bg = new ButtonGroup();
		
		JMenuItem customizeItem = new JMenuItem("Customize Viewer");
		customizeItem.setActionCommand("customize-viewer-command");
		customizeItem.addActionListener(listener);

		JMenu colorMenu = new JMenu("Color Scale Options");

		JCheckBoxMenuItem item = new JCheckBoxMenuItem("Gradient Mode", true);
		item.setActionCommand("gradient-color-mode");
		item.addActionListener(listener);
		bg.add(item);
		colorMenu.add(item);

		item = new JCheckBoxMenuItem("3 Bin Mode", false);
		item.setActionCommand("3-bin-mode");
		item.addActionListener(listener);
		bg.add(item);
		colorMenu.add(item);

		item = new JCheckBoxMenuItem("5 Bin Mode", false);
		item.setActionCommand("5-bin-mode");
		item.addActionListener(listener);
		bg.add(item);
		colorMenu.add(item);
		
		colorMenu.addSeparator();
		
		JMenuItem colorItem = new JMenuItem("Bin Colors and Limits");
		colorItem.setActionCommand("bin-color-range-command");
		colorItem.addActionListener(listener);
		colorMenu.add(colorItem);		
				
		JMenuItem navItem = new JMenuItem("LEM Navigation");
		navItem.setActionCommand("show-thumbnail-command");		 
		navItem.addActionListener(listener);
		
		JMenuItem showSelectionListItem = new JMenuItem("Locus Selection Manager");
		showSelectionListItem.setActionCommand("show-selection-list-command");
		showSelectionListItem.addActionListener(listener);

		JMenuItem storeMenuItem = new JMenuItem("Store Selected Loci (as cluster)");	
		storeMenuItem.setActionCommand("store-cluster-command");
		storeMenuItem.addActionListener(listener);
						
		JMenuItem saveSelectedLociItem = new JMenuItem("Save Selected Loci (Locus Detail)");
		saveSelectedLociItem.setActionCommand("save-selected-loci-command");
		saveSelectedLociItem.addActionListener(listener);		
		
		JMenuItem saveSelectedLociSpotsItem = new JMenuItem("Save Selected Loci (Spot Detail)");		
		saveSelectedLociSpotsItem.setActionCommand("save-selected-loci-spots-command");
		saveSelectedLociSpotsItem.addActionListener(listener);		
		
	/*	JMenu selectionMenu = new JMenu("Locus Selection Operations");
		selectionMenu.add(showSelectionListItem);
		selectionMenu.addSeparator();
		selectionMenu.add(storeMenuItem);
		selectionMenu.addSeparator();
		selectionMenu.add(saveSelectedLociItem);
		selectionMenu.add(saveSelectedLociSpotsItem);
		*/		
		
		JMenuItem saveMatrixItem = new JMenuItem("Save All Loci");
		saveMatrixItem.setActionCommand("save-matrix-command");
		saveMatrixItem.addActionListener(listener);

		menu.add(navItem);
		menu.addSeparator();
		menu.add(customizeItem);
		menu.add(colorMenu);
		menu.addSeparator();				
		menu.add(showSelectionListItem);
		menu.addSeparator();		
		menu.add(storeMenuItem);
		menu.addSeparator();		
		menu.add(saveSelectedLociItem);
		menu.add(saveSelectedLociSpotsItem);
		menu.add(saveMatrixItem);
	}

	private void showPopup(int x, int y) {
		menu.show(this, x, y);
	}

	/**
	 * Handles LEM mouse and action events
	 * @author braisted
	 */
	public class LEMListener extends MouseAdapter implements ActionListener, MouseMotionListener {
	
		public void actionPerformed(ActionEvent ae) {
			String command = ae.getActionCommand();
			
			if(command.equals("gradient-color-mode")) {
				setColorBinPolicy(LinearExpressionMapViewer.COLOR_MODE_GRADIENT);
			} else if(command.equals("3-bin-mode")) {
				setColorBinPolicy(LinearExpressionMapViewer.COLOR_MODE_2_BIN);				
			} else if(command.equals("5-bin-mode")) {
				setColorBinPolicy(LinearExpressionMapViewer.COLOR_MODE_4_BIN);				
			} else if(command.equals("scale-loci-command")) {
				setScaleLoci();
			} else if(command.equals("scale-open-command")) {
				setScaleIntergenic();
			} else if(command.equals("show-thumbnail-command")) {
				showThumbnail();
			} else if(command.equals("bin-color-range-command")) {
				setBinColorRanges();
			} else if(command.equals("show-selection-list-command")) {			
				showSelectionList();				
			} else if(command.equals("customize-viewer-command")) {
				customizeSettings();
			} else if(command.equals("save-matrix-command")) {
				saveLocusMatrix();
			} else if(command.equals("store-cluster-command")) {
				storeSelectedLociSpotsToCluster();
			} else if(command.equals("save-selected-loci-command")) {
				saveSelectedLoci();
			} else if(command.equals("save-selected-loci-spots-command")) {
				saveSelectedLociSpots();
			}
		}
		
		public void mousePressed(MouseEvent me) {
			if(me.isPopupTrigger()) {
				showPopup(me.getX(), me.getY());
			} 
		}

		public void mouseReleased(MouseEvent me) {
			if(me.isPopupTrigger()) {
				showPopup(me.getX(), me.getY());
			} else {
				if(MouseEvent.getModifiersExText(me.getModifiersEx()).equalsIgnoreCase("Shift")) {
					selectLocus(me.getX(), me.getY());				
				} else 
					showInfo(me.getX(), me.getY());
			}			
		}
		
		public void mouseExited(MouseEvent me) {
			if(!menu.isVisible() || ( thumbnail != null && !thumbnail.isVisible())) {			
				highlighted = false;
				repaint();
			}
			setStatusText(-1);
		}

		public void mouseMoved(MouseEvent me) {						
			shadeLoci(me.getX(), me.getY());
		}
		
		public void mouseDragged(MouseEvent me) {
		}		
	}


	/**
	 * @see org.tigr.microarray.mev.cluster.gui.IViewer#getExperimentID()
	 */
	public int getExperimentID() {
		return this.exptID;
	}

	/**
	 * @see org.tigr.microarray.mev.cluster.gui.IViewer#setExperimentID(int)
	 */
	public void setExperimentID(int id) {
		this.exptID = id;
	}

		
}
