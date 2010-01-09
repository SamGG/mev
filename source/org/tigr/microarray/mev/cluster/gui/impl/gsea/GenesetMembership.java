package org.tigr.microarray.mev.cluster.gui.impl.gsea;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.beans.Expression;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.tigr.microarray.mev.cluster.algorithm.impl.gsea.Geneset;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.GSEAExperiment;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IDisplayMenu;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.IViewer;

import org.tigr.util.FloatMatrix;

public class GenesetMembership extends JPanel implements IViewer {

	// First create an Experiment object with number of rows equal to genes
	// present in gene sets and data.
	// Number of columns id equal to number of gene sets +1. This can be
	// obtained form GSEAGUI.

	// Second step is to create a new ExperimentHeader class which would allow
	// painting geneset names instead of sample names
	// override the drawHeader(), updateSizes()

	// Third step is to create an experimentViewer with the experiment object
	// created in step one. GSEAExperimentViewer would need an additional
	// constructor where it overrides exrimentHeader
	// with the new custom experimentHeader object. Clusters would be equal to
	// numer of rows
	// in experiment object
	// ExperimentViewer should have popup menu and rightclick disabled because
	// 1. spot information will not be able to find the correct info. Number of
	// columns is not equal to number of samples but is equal to number
	// of gene sets.
	// 2. Rows are not equal to number of genes so number of rows is lesser than
	// the original experiment object
	// 3. We will be plotting test statistic and not expression values.

	private Vector<String> unique_genes;
	private Vector<String> gene_sets;
	private Geneset[] gSets;
	private GSEAExperiment experimentObject;
	private FloatMatrix experimentMatrix;
	private int[] columns;
	private FontMetrics metrics;
	private Dimension elementSize = new Dimension(20, 20);
	public static Color missingColor = new Color(128, 128, 128);
	public static Color maskColor = new Color(255, 255, 255, 128);
	private int firstSelectedRow = -1;
	private int lastSelectedRow = -1;
	private int firstSelectedColumn = -1;
	private int lastSelectedColumn = -1;
	private Insets insets = new Insets(0, 5, 0, 0);
	private boolean isDrawBorders = true;
	private boolean useDoubleGradient = true;
//	public BufferedImage posColorImage = createGradientImage(Color.black,
//			Color.red);
//	public BufferedImage negColorImage = createGradientImage(Color.green,
//			Color.black);
	private static final float INITIAL_MAX_VALUE = 3f;
	private static final float INITIAL_MIN_VALUE = -3f;
	private float maxValue = INITIAL_MAX_VALUE;
	private float minValue = INITIAL_MIN_VALUE;
	private float midValue = 0.0f;
	
	private int xOldEvent;
	private int yOldEvent;
	
	public GenesetMembership(Vector<String> uniquegenes,
			Vector<String> genesets, Geneset[] gset) {
		setLayout(new GridBagLayout());
		this.unique_genes = uniquegenes;
		this.gene_sets = genesets;
		this.experimentMatrix = new FloatMatrix(this.unique_genes.size(),
				this.gene_sets.size() );
		this.gSets = gset;
		createExperimentObject();
		addMouseListener(new Listener());
		addMouseMotionListener(new Listener());
		super.setAutoscrolls(true);
		setBackground(Color.white);
		System.out.println("genes:"+this.unique_genes.size());
		System.out.println("genesets:"+this.gene_sets.size());
		
	}

	/**
	 * @return createExperimentObject creates an Experiment object with
	 * 
	 *         rows=number of unique genes present in the expression data
	 *         columns=number of gene sets
	 * 
	 *         The zeroth column will contain the sorted test statistic of all
	 *         genes in the data set. Thereafter each cell [row=gene and
	 *         column=geneset] will contain the test statistic corresponding to
	 *         that gene, if it is present in the gene set and zero otherwise
	 * 
	 * 
	 * 
	 * 
	 */
	public void createExperimentObject() {

		// Rowsize is equal to the number of genes
		int rowSize = this.unique_genes.size();
		// System.out.println("row size of Amat:"+rowSize);

		// Colsize is equal to the number of genesets
		int colSize = this.gene_sets.size();
		// System.out.println("col size of Amat:"+colSize);

		int rowIndex = 0;
		int colIndex = 0;
		

		// Loop through the gene sets
		for (colIndex = 0; colIndex < colSize; colIndex++) {
			// Get the names of all genes in the current gene set
			ArrayList<String> _genesinGeneset = new ArrayList<String>();
			_genesinGeneset = gSets[colIndex].getGenesinGeneset();

			// Loop through all genes in the current gene set
			for (int j = 0; j < _genesinGeneset.size(); j++) {
				String Gene = (String) _genesinGeneset.get(j);
				Gene = Gene.trim();

				// Loop through all unique genes in the expression data
				for (int k = 0; k < rowSize; k++) {
					String uniq_genes = (String) this.unique_genes.get(k);
					uniq_genes = uniq_genes.trim();
					// Set the zeroth and current geneset column to test
					// statistic if gene in the gene set also present in data
					// set
					if (uniq_genes.equals(Gene)) {
						
						this.experimentMatrix.set(k, colIndex, 1);
					}

				}

			}

			

		}

//		experimentObject = new GSEAExperiment(this.experimentMatrix,
//				createColumns(this.gene_sets.size() + 1));--commented to test out genesigdb like graph
		experimentObject = new GSEAExperiment(this.experimentMatrix,
				createColumns(this.gene_sets.size()));
	}

	/**
	 * Returns max width of sample names.
	 */
	protected int getNamesWidth(FontMetrics metrics) {
		int maxWidth = 0;
		for (int i = 0; i < this.gene_sets.size(); i++) {
			maxWidth = Math.max(maxWidth, metrics.stringWidth(this.gene_sets
					.get(i)));
		}
		return maxWidth;
	}
	
	
	/**
	 * Returns max width of Gene names.
	 */
	protected int getGeneNamesWidth(Graphics2D g) {
		int maxWidth = 0;
		
		for (int i = 0; i < this.unique_genes.size(); i++) {
			
			int temp = Math.max(maxWidth, metrics.stringWidth(this.unique_genes
					.get(i)));
			if(temp < g.getClipBounds().width) {
				maxWidth=temp;
			}
			
		}
		
		System.out.println("Max gene names width:"+maxWidth);
		return maxWidth;
	}
	
	
	
	  /**
     * Paints chart into specified graphics.
     *
    public void paint(Graphics g) {
     
        paint((Graphics2D)g);
    }
    
  
  
	

	
	/**
	 * 
	 * 
	 * 
	 */

	public void paint(Graphics g1) {
		
		super.paint(g1);
		Graphics2D g =  (Graphics2D)g1;
		setFont(new Font("monospaced", Font.PLAIN, elementSize.height));
		
		// Paint the gene set names as column names
		drawSampleNames(g);
		repaint();
		
		
		FontMetrics metrics = g.getFontMetrics();
		setFontMetric(metrics);
		
		if (this.elementSize.getHeight() < 1)
			return;
		final int samples = experimentObject.getNumberOfSamples();
		
			
		
		Rectangle rectangle=g.getClipBounds();
		final int y =  getTopIndex(rectangle.y+getNamesWidth(metrics)+15);
		final int height = getBottomIndex(rectangle.y + rectangle.height, this.unique_genes.size());
		final int x = getLeftIndex(rectangle.x);
		final int width =  getRightIndex(rectangle.x+rectangle.width, samples);
	
				
		int expressionRowIndex = 0;
		int expressionColIndex = 0;
		
		// Paint the expression data points
		for (int column = x; column < width; column++) {

			for (int row = y; row < height; row++) {
				fillRectAt(g1, row, column, expressionRowIndex,
						expressionColIndex);

				if (expressionRowIndex < this.unique_genes.size()-1) {
					expressionRowIndex = expressionRowIndex + 1;
				}
			}
			if (expressionColIndex < samples) {
				expressionColIndex = expressionColIndex + 1;
			}
		}

		updateSize();
	
		// Paint the names of the genes
		 int uniqX = elementSize.width*samples+10;
		 int annY;
		for(int row=y; row<height; row++ ) {
			annY = (row+1)*elementSize.height;
            g.drawString(this.unique_genes.get(row), uniqX + insets.left, annY-1);
		}
		
		repaint();
		
	}
	
	
	
	
	public void drawSampleNames(Graphics2D g) {
		FontMetrics metrics=g.getFontMetrics();
		
		int descent = metrics.getDescent();
		int samples=experimentObject.getNumberOfSamples();
		
		if(samples==0)
			return;
		
		setSize(elementSize.width*samples, getNamesWidth(metrics));
        int h = -getSize().height + 5;
		   
        g.setColor(Color.black);
        g.rotate(-Math.PI/2);
        for (int sample = 0; sample < samples; sample++) {
            String name = this.gene_sets.get(sample);
            g.drawString(name, h, descent + elementSize.width*sample + elementSize.width/2 + insets.left);
        }
       g.rotate(Math.PI/2);
	}
	
	
   
	/**
	 * Fills rect with specified row and column.
	 */
	private void fillRectAt(Graphics g, int row, int column,
			int expressionRowIndex, int expressionColIndex) {

		if (column > (experimentObject.getNumberOfSamples() ))
			return;
		int x = column * elementSize.width + insets.left;
		int y = row * elementSize.height;

		boolean mask = this.firstSelectedRow >= 0 && this.lastSelectedRow >= 0
				&& (row < this.firstSelectedRow || row > this.lastSelectedRow);
		mask = (mask || this.firstSelectedColumn >= 0
				&& this.lastSelectedColumn >= 0
				&& (column < this.firstSelectedColumn || column > this.lastSelectedColumn));
		if(this.experimentObject.get(expressionRowIndex,
				expressionColIndex)==1) {
			g.setColor(Color.RED);
		}else {
			g.setColor(Color.white);
		}

		g.fillRect(x, y, elementSize.width, elementSize.height);
		if (mask) {
			g.setColor(maskColor);
			g.fillRect(x, y, elementSize.width, elementSize.height);
		}
		if (this.isDrawBorders) {
			g.setColor(Color.black);
			g.drawRect(x, y, elementSize.width - 1, elementSize.height - 1);
		}
	}

	/**
     * Updates size of this viewer.
     */
    private void updateSize() {
    	 setFont(new Font("monospaced", Font.PLAIN, elementSize.height));
         Graphics2D g = (Graphics2D)getGraphics();
         int width = elementSize.width*experimentObject.getNumberOfSamples() + 1 + insets.left;
         width += 20+40;
         int height = elementSize.height*experimentObject.getNumberOfGenes()+5+40;
         setSize(width, height);

    }
    

	public int[][] createSpotIndices(int count) {

		int[][] spotindicies = new int[1][count];
		for (int i = 0; i < count; i++) {
			spotindicies[0][i] = i;

		}
		return spotindicies;
	}

	/**
	 * createColumns generates an integer array, that contains the number of
	 * experiments(samples). Needed to populate GSEAExperiment
	 * 
	 * 
	 */
	public int[] createColumns(int count) {
		columns = new int[count];
		for (int i = 0; i < count; i++) {
			columns[i] = i;
		}
		return columns;
	}

	public FontMetrics getFontMetric() {
		return metrics;
	}

	public void setFontMetric(FontMetrics metric) {
		metrics = metric;
	}

	private int getTopIndex(int top) {
		if (top < 0) {
			return 0;
		}
		return top / elementSize.height;
	}

	private int getLeftIndex(int left) {
		if (left < insets.left) {
			return 0;
		}
		return (left - insets.left) / elementSize.width;
	}

	private int getRightIndex(int right, int limit) {
		if (right < 0) {
			return 0;
		}
		int result = right / elementSize.width + 1;
		return result > limit ? limit : result;
	}

	private int getBottomIndex(int bottom, int limit) {
		if (bottom < 0) {
			return 0;
		}
		int result = bottom / elementSize.height + 1;
		return result > limit ? limit : result;
	}
	
	
 private class Listener implements MouseMotionListener, MouseListener{

	
	public void mouseDragged(MouseEvent e) {
		// TODO Auto-generated method stub
		int x = e.getX();
        int y = e.getY();
        Rectangle r = new Rectangle(e.getX(), e.getY(), 1, 1);
        ((JPanel)e.getSource()).scrollRectToVisible(r);
        System.out.println("mouse drageed");
	}


	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub
		 int x = e.getX();
         int y = e.getY();
//          System.out.println("x in mouse moved:"+x);
//          System.out.println("y in mouse moved:"+y);
            setXOldEvent(x);
            setYOldEvent(y);
            repaint();
	}


	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}


	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}


	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		setXOldEvent(-1);
        setYOldEvent(-1);
        repaint();
	}


	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	 
 }

 public void setCursor(int cursor) {setCursor(Cursor.getPredefinedCursor(cursor));}
 public void setXOldEvent(int xEvent) {this.xOldEvent = xEvent;}
 public int getXOldEvent() {return this.xOldEvent;}
 public void setYOldEvent(int yEvent) {this.yOldEvent = yEvent;}
 public int getYOldEvent() {return this.yOldEvent;}

	
	public int[][] getClusters() {
		// TODO Auto-generated method stub
		return null;
	}

	

	public JComponent getCornerComponent(int cornerIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	public Experiment getExperiment() {
		// TODO Auto-generated method stub
		return experimentObject;
	}

	public int getExperimentID() {
		// TODO Auto-generated method stub
		return 0;
	}

	public Expression getExpression() {
		// TODO Auto-generated method stub
		return null;
	}

	public JComponent getHeaderComponent() {
		// TODO Auto-generated method stub
		return null;
	}

	public BufferedImage getImage() {
		// TODO Auto-generated method stub
		return null;
	}

	public JComponent getRowHeaderComponent() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getViewerType() {
		// TODO Auto-generated method stub
		return -1;
	}

	public void onClosed() {
		// TODO Auto-generated method stub

	}

	public void onDataChanged(IData data) {
		// TODO Auto-generated method stub

	}

	public void onDeselected() {
		// TODO Auto-generated method stub

	}

	public void onMenuChanged(IDisplayMenu menu) {
		// TODO Auto-generated method stub

	}

	public void onSelected(IFramework framework) {
		// TODO Auto-generated method stub
		//repaint();
	}

	public void setExperiment(Experiment e) {
		// TODO Auto-generated method stub

	}

	public void setExperimentID(int id) {
		// TODO Auto-generated method stub

	}

	
	public JComponent getContentComponent() {
		// TODO Auto-generated method stub
		
		return this;
	}

}
