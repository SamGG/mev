package org.tigr.microarray.mev.cluster.gui.impl.gsea;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;

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

	private Vector<String> unique_genes=new java.util.Vector<String>();
	private ArrayList<String> gene_sets=new java.util.ArrayList<String>();
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
	private Insets insets = new Insets(10, 10, 0, 0);
	private boolean isDrawBorders = true;
	
	
	    
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
	private boolean referenceLinesOn=true;
	private boolean mouseOnMap = false;
	private int mouseRow = 0;
	private int mouseColumn = 0;
	private int dragRow = 0;
	private int dragColumn = 0;
	private int width=0;
	private int height=0;
	
	
	public int getCurrentWidth() {
		return width;
	}

	public void setCurrentWidth(int width) {
		this.width = width;
	}

	public int getCurrentHeight() {
		return height;
	}

	public void setCurrentHeight(int height) {
		this.height = height;
	}

	public GenesetMembership(Vector<String> uniquegenes,
			ArrayList<String> genesets, Geneset[] gset) {
	//	setLayout(new GridBagLayout());
		setUnique_genes(uniquegenes);
		setGene_sets(genesets);
		this.experimentMatrix = new FloatMatrix(this.unique_genes.size(),
				this.gene_sets.size() );
		this.gSets = gset;
		createExperimentObject();
		this.addMouseListener(new Listener());
		this.addMouseMotionListener(new Listener());
	//	super.setAutoscrolls(true);
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
	
		return maxWidth;
	}
	
	
	
	

	
	/**
	 * 
	 * 
	 * 
	 */

	public void paint(Graphics g1) {
		
		super.paint(g1);
		Graphics2D g =  (Graphics2D)g1;
		g.setFont(new Font("monospaced", Font.PLAIN, elementSize.height));

		FontMetrics metrics = g.getFontMetrics();
		setFontMetric(metrics);

		if (this.elementSize.getHeight() < 1)
			return;
		final int samples = getGene_sets().size();

		Rectangle rectangle=g.getClipBounds();
		final int originalY =  (rectangle.y+getNamesWidth(metrics)+15);
		final int height = originalY+elementSize.height*getUnique_genes().size();
		final int originalX = (rectangle.x)+insets.left;
		final int width =  originalX+elementSize.width*samples;
		int currentX=originalX;
		int currentY=originalY;
		
		
		// Paint the gene set names as column names
		drawSampleNames(g);
		
		
	// Paint the names of the genes
		g.setStroke(new BasicStroke(2));
		g.setColor(Color.black);
		for(int row=0; row<this.unique_genes.size(); row++ ) {
			String name=this.unique_genes.get(row);
	        g.drawString(name, width-originalX, g.getFontMetrics().getAscent()+originalY+elementSize.height*row + elementSize.height/2);
	           
			}
		repaint();
		
		
		// Paint the expression data points


	/*	for (int column = 0; column < this.gene_sets.size(); column++) {

			for (int row = 0; row < this.unique_genes.size(); row++) {
				fillRectAt(g1, currentX, currentY, row,
						column);
				
				currentY=currentY+elementSize.height;

			} 	  	
		
				currentX=currentX+elementSize.width;
			currentY=originalY;

		}*/
		
		 Rectangle bounds = g.getClipBounds();
	        final int top = getTopIndex(bounds.y+getNamesWidth(metrics)+20);
	        final int bottom = getBottomIndex(top+bounds.height, getUnique_genes().size());
	        final int left = getLeftIndex(bounds.x);
	        final int right = getRightIndex(bounds.x+bounds.width, samples);
	      
	        int x, y;
	        // draw rectangles
	        for (int column=left; column<right; column++) {
	            for (int row=top; row<bottom; row++) {
	                fillRectAt(g, row, column);
	            }
	        }
	
	//	drawGeneNames(width, height, g1, originalY);
		
		
		//


		
	}
	
	public void drawGeneNames(int width, int height, Graphics g, int ycoordinate) {
		//int newWidth=width+40;
		//setSize(newWidth, height);
		
		int descent = g.getFontMetrics().getDescent();
		 int uniqX = width+5;
		 int annY=ycoordinate;
		 
		 g.setColor(Color.black);
		for(int row=0; row<this.unique_genes.size(); row++ ) {
			String name=this.unique_genes.get(row);
           g.drawString(name, uniqX, descent + elementSize.height*row + elementSize.height/2);
           //annY=annY+elementSize.height;
		}
		
		
		
		
	}
	
	
	
	
	
	
	public void drawSampleNames(Graphics2D g) {
		FontMetrics metrics=g.getFontMetrics();
		
		int descent = metrics.getDescent();
		int samples=getGene_sets().size();
		
		if(samples==0)
			return;
		
	//	setSize(elementSize.width*samples, getNamesWidth(metrics)+15+g.getClipBounds().y);
       // int h = -getSize().height + 5;
		int h = -(getNamesWidth(metrics)+15+g.getClipBounds().y) + 5;
		  
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
    private void fillRectAt(Graphics g, int row, int column) {
        if (column > (experimentObject.getNumberOfSamples() -1))
        	return;
        int x = column*elementSize.width + insets.left;
        int y = row*elementSize.height;
        boolean mask = this.firstSelectedRow >= 0 && this.lastSelectedRow >= 0 && (row < this.firstSelectedRow || row > this.lastSelectedRow);
        mask = (mask || this.firstSelectedColumn >= 0 && this.lastSelectedColumn >= 0 && (column < this.firstSelectedColumn || column > this.lastSelectedColumn));
        
        if(experimentObject.get(row, column)==1) {
        	 g.setColor(Color.red);
        }else
        	g.setColor(Color.white);
       
        g.fillRect(x, y, elementSize.width, elementSize.height);
        if (mask) {
            g.setColor(maskColor);
            g.fillRect(x, y, elementSize.width, elementSize.height);
        }
        if (this.isDrawBorders) {
            g.setColor(Color.black);
            g.drawRect(x, y, elementSize.width-1, elementSize.height-1);
        }
    }
	
	
   
	/**
	 * Fills rect with specified row and column.
	 */
	private void fillRectAt(Graphics g, int row, int column,
			int expressionRowIndex, int expressionColIndex) {
		
	
		if(column> getWidth())
			return;
		
		int x = row ;
		int y = column;
		
	
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
     * Draws rect with specified row, column and color.
     */
    private void drawRectAt(Graphics g, int row, int column, Color color) {
        g.setColor(color);
        if (column>=experimentObject.getNumberOfSamples()){
        	return;
        }
        else{
        g.drawRect(column*elementSize.width + insets.left, row*elementSize.height, elementSize.width-1, elementSize.height-1);
    }
    }
	
	
	/**
     * Updates size of this viewer.
     */
    private void updateSize(int width, int height) {
    	 setFont(new Font("monospaced", Font.PLAIN, elementSize.height));
    	 
         int new_width = width+10;
         int new_height = height;
         
         setSize(new_width, new_height);
         setCurrentWidth(new_width);
         setCurrentHeight(new_height);
        
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

	  /**
     * Finds column for specified x coordinate.
     * @return -1 if column was not found.
     */
    private int findColumn(int targetx) {
        int xSize = experimentObject.getNumberOfSamples()*elementSize.width;
        if (targetx < insets.left) {
            return -1;
        }
        if (targetx >= (xSize + insets.left) && (targetx < (xSize + insets.left+this.elementSize.width + 10)))
        	return (targetx - insets.left-5)/elementSize.width;
        return (targetx - insets.left)/elementSize.width;
    }
    
    /**
     * Finds row for specified y coordinate.
     * @return -1 if row was not found.
     */
    private int findRow(int targety) {
        int ySize = this.gene_sets.size()*elementSize.height;
        if (targety >= ySize || targety < 0)
            return -1;
        return targety/elementSize.height;
    }
    
    private boolean isLegalPosition(int row, int column) {
        if (isLegalRow(row) && isLegalColumn(column))
            return true;
        return false;
    }
    
    private boolean isLegalColumn(int column) {
        if (column < 0 || column > (experimentObject.getNumberOfSamples() -1))
            return false;
        return true;
    }
    
    private boolean isLegalRow(int row) {
        if (row < 0 || row > this.gene_sets.size() -1)
            return false;
        return true;
    }
    
	
	/**
     * The class to listen to mouse events.
     */
    private class Listener extends MouseAdapter implements MouseMotionListener {
        
        private String oldStatusText;
        private int oldRow = -1;
        private int oldColumn = -1;
        private int startColumn = 0;
        private int startRow = 0;
        
        public void mouseClicked(MouseEvent event) {
            if (SwingUtilities.isRightMouseButton(event)) {
                return;
            }
          
        		return;
        	}
           
       
        
        public void mouseMoved(MouseEvent event) {
        	  if (experimentObject.getNumberOfSamples() == 0 || event.isShiftDown())
                  return;
              int column = findColumn(event.getX());
              int row = findRow(event.getY());
              Graphics g = null;
              g = getGraphics();
              Graphics2D g2d=(Graphics2D)g;
             
              //mouse on heat map
              if (isLegalPosition(row, column)&& (column < experimentObject.getNumberOfSamples())) {
                  drawRectAt(g, row, column, Color.black);
                
					g.drawString("Gene set: "+getGene_sets().get(findColumn(column)), findRow(row) , findColumn(column) - 20);
					g.drawString("Gene:"+getUnique_genes().get(findRow(row)), findRow(row) , findColumn(column));
                
              }
              //mouse on different rectangle, but still on the map
              if (!isCurrentPosition(row, column)&&isLegalPosition(row, column)){
              	mouseOnMap = true;
              	mouseRow = row;
              	mouseColumn = column;
              	g.drawString("Gene set: "+getGene_sets().get(findColumn(mouseColumn)), findRow(mouseRow) , findColumn(mouseColumn) - 20);
				g.drawString("Gene:"+getUnique_genes().get(findRow(mouseRow)), findRow(mouseRow) , findColumn(mouseColumn));
            
//                       	
              	repaint();
              }
              if (g != null) {
                  g.dispose();
              }
             
        }
        
        public void mouseEntered(MouseEvent event) {
        	
        }
        
        public void mouseExited(MouseEvent event) {
//        	mouseOnMap = false;
//        	
//        	repaint();
//        	if (isLegalPosition(oldRow, oldColumn)) {
//                Graphics g = getGraphics();
//                fillRectAt(g, oldRow, oldColumn);
//                g.dispose();
//            }
//            setOldPosition(-1, -1);
//            framework.setStatusText(oldStatusText);
//            repaint();
        
           
        }
        
        public void mouseDragged(MouseEvent event) {
        	repaint();
            if (SwingUtilities.isRightMouseButton(event)) {
                return;
            }
            int column = findColumn(event.getX());
            int row = findRow(event.getY());
            if (!isLegalPosition(row, column)) {
            	
                return;
            }
          
            dragColumn = column;
            dragRow = row;

        
         
        }
        /** Called when the mouse has been pressed. */
        public void mousePressed(MouseEvent event) {
            if (SwingUtilities.isRightMouseButton(event)) {
                return;
            }
            startColumn = findColumn(event.getX());
            startRow = findRow(event.getY());
            if ((!isLegalPosition(startRow, startColumn))||event.isShiftDown()||startColumn<experimentObject.getNumberOfSamples())
            	return;
         

            dragColumn = startColumn;
            dragRow = startRow;
         

           
        }

        /** Called when the mouse has been released. */
        public void mouseReleased(MouseEvent event) {
	     	
	        int endColumn = findColumn(event.getX());
	        if (endColumn < experimentObject.getNumberOfSamples())
	        	return;
	        int endRow = findRow(event.getY());
	        if (!isLegalPosition(startRow, startColumn)) {
	            return;
	        }
	      	
	     }
        
        
        private void setOldPosition(int row, int column) {
            oldColumn = column;
            oldRow = row;
        }
        
        private boolean isCurrentPosition(int row, int column) {
            return(row == oldRow && column == oldColumn);
        }
        
       
    }
   
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
			
		repaint();
	}

	public void setExperiment(Experiment e) {
		// TODO Auto-generated method stub

	}

	public void setExperimentID(int id) {
		// TODO Auto-generated method stub

	}

	
	public JComponent getContentComponent() {
		// TODO Auto-generated method stub
		revalidate();
		return this;
	}

	public Vector<String> getUnique_genes() {
		return unique_genes;
	}

	public void setUnique_genes(Vector<String> unique_genes) {
		this.unique_genes = unique_genes;
	}

	

	public void setGene_sets(ArrayList<String> gene_sets) {
		this.gene_sets = gene_sets;
	}

	public ArrayList<String> getGene_sets() {
		return gene_sets;
	}
	
	public Dimension getMinimumSize() {
		if(getCurrentWidth()==0 || getCurrentHeight()==0)
		return new Dimension(elementSize.width*getGene_sets().size()+100, elementSize.height*getUnique_genes().size());
		else
			return new Dimension(getCurrentWidth(), getCurrentHeight());
	}
	
	public Dimension getPreferredSize() {
		return getMinimumSize();
	}
	
	
	
}
