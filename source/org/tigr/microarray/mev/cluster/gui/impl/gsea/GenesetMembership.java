package org.tigr.microarray.mev.cluster.gui.impl.gsea;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
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

import javax.swing.SwingUtilities;

import org.tigr.microarray.mev.cluster.algorithm.impl.gsea.Geneset;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.GSEAExperiment;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IDisplayMenu;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.IViewer;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentHeader;

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
	private ArrayList<String>sampleNames=new java.util.ArrayList<String>();
	private Geneset[] gSets;
	private GSEAExperiment experimentObject;
	private FloatMatrix experimentMatrix;
	private GenesetMembershipHeader header;
	private int contentWidth = 0;
	
	private int[] columns;
	private FontMetrics metrics;
	private Dimension elementSize = new Dimension(20, 10);
	public static Color missingColor = new Color(254, 254, 254);
	public static Color maskColor = new Color(255, 255, 255, 128);
	public BufferedImage posColorImage = createGradientImage(Color.black, Color.red);
	public BufferedImage negColorImage = createGradientImage(Color.green, Color.black);
	
	private int firstSelectedRow = -1;
	private int lastSelectedRow = -1;
	private int firstSelectedColumn = -1;
	private int lastSelectedColumn = -1;
	private Insets insets = new Insets(0, 10, 0, 0);
	private boolean isDrawBorders = true;
	
	private boolean useDoubleGradient = false;
	private static final float INITIAL_MAX_VALUE = 3f;
	private static final float INITIAL_MIN_VALUE = -3f;
	private float maxValue = INITIAL_MAX_VALUE;
	private float minValue = INITIAL_MIN_VALUE;
	private float midValue = 0.0f;
	
	
	private int width=0;
	private int height=0;
	
	private Graphics2D graphics;	
	private Font font=new Font("monospaced", Font.PLAIN, elementSize.height);
	
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
	
	
		setUnique_genes(uniquegenes);
		setGene_sets(genesets);
		setSampleNames(genesets);
		this.experimentMatrix = new FloatMatrix(this.unique_genes.size(),
				this.gene_sets.size()+1);
		this.gSets = gset;
		createExperimentObject();
		this.header=new GenesetMembershipHeader(getSampleNames());
		this.addMouseListener(new Listener());
		this.addMouseMotionListener(new Listener());
		setBackground(Color.white);
		
		
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

		
		int colIndex = 0;
		int matrixColIndex = 1;

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
						this.experimentMatrix.set(k, 0, gSets[colIndex].getGeneSetElement(j).getTestStat());
						this.experimentMatrix.set(k, matrixColIndex, gSets[colIndex].getGeneSetElement(j).getTestStat());
						
					}
					//else
						//this.experimentMatrix.set(k, matrixColIndex, Float.NaN);

			}

			

		}
			matrixColIndex = matrixColIndex + 1;
		}
		experimentObject = new GSEAExperiment(this.experimentMatrix,createColumns(this.gene_sets.size()+1));
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
		metrics=g.getFontMetrics();
		for (int i = 0; i < this.unique_genes.size(); i++) {
			
			 maxWidth = Math.max(maxWidth, metrics.stringWidth(this.unique_genes
					.get(i)));
		
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
		g1.setFont(font);
		graphics =  (Graphics2D)g1;
		
		Rectangle bounds = graphics.getClipBounds();
		final int top = getTopIndex(bounds.y);
		final int bottom = getBottomIndex(bounds.y+bounds.height, getUnique_genes().size());
        final int left = getLeftIndex(bounds.x);
        final int right = getRightIndex(bounds.x+bounds.width, getGene_sets().size()+1);

	
		if (this.elementSize.getHeight() < 1)
			return;
		final int samples = getGene_sets().size();

		     //  draw rectangles
	        for (int column=left; column<right; column++) {
	            for (int row=top; row<bottom; row++) {
	                fillRectAt(graphics, row, column);
	            }
	        }
	        
	       
	
	     // Paint the names of the genes
	        graphics.setFont(font);
			graphics.setStroke(new BasicStroke(2));
			graphics.setColor(Color.black);
	
			
			if(right>=samples) {
				 int uniqX = elementSize.width*samples+30;
				 int row=top;
				 for (int rowIndex=0; rowIndex<this.unique_genes.size(); rowIndex++) {
					 
					 String name=getUnique_genes().get(rowIndex);
	                   int annY = (row+1)*elementSize.height;
	                    graphics.drawString(name, uniqX + insets.left, annY-1);
	                    
	                    if(row<bottom)
	                    	row=row+1;
	                }
		
				 
			}
		
		
	}
	

	
	
	 /**
     * Fills rect with specified row and column.
     */
    private void fillRectAt(Graphics g, int row, int column) {
        if (column > (experimentObject.getNumberOfSamples()-1)|| row> experimentObject.getNumberOfGenes()-1)
        	return;
        int x = column*elementSize.width + insets.left;
        int y = row*elementSize.height;
        boolean mask = this.firstSelectedRow >= 0 && this.lastSelectedRow >= 0 && (row < this.firstSelectedRow || row > this.lastSelectedRow);
        mask = (mask || this.firstSelectedColumn >= 0 && this.lastSelectedColumn >= 0 && (column < this.firstSelectedColumn || column > this.lastSelectedColumn));
        
        g.setColor(getColor(this.experimentObject.get(row, column)));       
      
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
     * Creates a gradient image with specified initial colors.
     */
    public BufferedImage createGradientImage(Color color1, Color color2) {
        BufferedImage image = (BufferedImage)java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(256,1);
        Graphics2D graphics = image.createGraphics();
        GradientPaint gp = new GradientPaint(0, 0, color1, 255, 0, color2);
        graphics.setPaint(gp);
        graphics.drawRect(0, 0, 255, 1);
        return image;
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    private Color getColor(float value) {
        if (Float.isNaN(value)) {
            return missingColor;
        }
        
        float maximum;
        int colorIndex, rgb;
        
        if(useDoubleGradient) {
        	maximum = value < midValue ? this.minValue : this.maxValue;
			colorIndex = (int) (255 * (value-midValue) / (maximum - midValue));
			if(colorIndex<0)
				colorIndex=-colorIndex;
			colorIndex = colorIndex > 255 ? 255 : colorIndex;
			rgb = value < midValue ? negColorImage.getRGB(255 - colorIndex, 0)
					: posColorImage.getRGB(colorIndex, 0);
        } else {
        	float span = this.maxValue - this.minValue;
        	if(value <= minValue)
        		colorIndex = 0;
        	else if(value >= maxValue)
        		colorIndex = 255;
        	else
        		colorIndex = (int)(((value - this.minValue)/span) * 255);
         	
        	rgb = posColorImage.getRGB(colorIndex,0);
        }
        return new Color(rgb);
    }
	
    
   
    
    
	/**
     * Updates size of this viewer.
     */
    private void updateSize() {
    
    	
         setFont(new Font("monospaced", Font.PLAIN, elementSize.height));
         Graphics2D g = (Graphics2D)getGraphics();
         int width = elementSize.width*getGene_sets().size() + 1 + insets.left;
         width += 20+getGeneNamesWidth(g);
        
        
         this.contentWidth = width;
         
         int height = elementSize.height*getUnique_genes().size()+1;
         setSize(width, height);


         if (header.getSize().width < width){
         	setSize(width, height);
         	setPreferredSize(new Dimension(width, height));
         }else{
         	setSize(new Dimension(header.getSize().width, getSize().height));
         	setPreferredSize(new Dimension(header.getSize().width, getSize().height));
         }

         
         header.setHeaderWidth(width);
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
        int ySize = this.unique_genes.size()*elementSize.height;
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
        GSEAInfoDisplay display;
        public void mouseClicked(MouseEvent event) {
        	//System.out.println("mouse clicked");
        	 if (experimentObject.getNumberOfSamples() == 0 || event.isShiftDown())
                 return;
          
        	 int column = findColumn(event.getX());
             int row = findRow(event.getY());
                     
             //mouse on heat map
             if (isLegalPosition(row, column)&& (column < experimentObject.getNumberOfSamples())) {
            	 if(column==0)
                 display =new GSEAInfoDisplay(null, "Geneset Membership Info Dialog", false, "NA", getUnique_genes().get(row), Float.toString(experimentObject.get(row,column)));
            	 else
            		 display =new GSEAInfoDisplay(null, "Geneset Membership Info Dialog", false, getGene_sets().get(column-1), getUnique_genes().get(row), Float.toString(experimentObject.get(row,column)));	 
            	 
             }

        		
        	}
           
       
        
        public void mouseMoved(MouseEvent event) {
        	
        	
        	
        	 
        }
        
        public void mouseEntered(MouseEvent event) {
        //	System.out.println("mouse entered");
        	
        	
        	
        	
        	
        	
        	
        }
        
        
        public void mouseExited(MouseEvent event) {
        	      
           
        }
        
        
        public void mouseDragged(MouseEvent event) {
    
        }
        
        
        /** Called when the mouse has been pressed. */
        public void mousePressed(MouseEvent event) {
      
        }

        /** Called when the mouse has been released. */
        public void mouseReleased(MouseEvent event) {
      
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
		return header;
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
		updateSize();
		header.updateSizes(header.getSize().width, elementSize.width);
		
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
	
	/**
     * Returns content width
     */
    public int getContentWidth(){
    	updateSize();
        return contentWidth;
    }

	public ArrayList<String> getSampleNames() {
		return sampleNames;
	}

	public void setSampleNames(ArrayList<String> sNames) {
		
		for(int index=0; index<sNames.size()+1; index++) {
			if(index==0)
			sampleNames.add(index,"Test Statistic" );
			else
				sampleNames.add(index,sNames.get(index-1) );
		}
		
	}
	
	
}
