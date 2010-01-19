package org.tigr.microarray.mev.cluster.gui.impl.gsea;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.helpers.IExperimentHeader;


public class GenesetMembershipHeader extends JPanel implements IExperimentHeader{
	
	private ArrayList<String> gene_set=new ArrayList<String>();
	private static final int RECT_HEIGHT = 15;
	private static final int COLOR_BAR_HEIGHT = 15;
	private int elementWidth;
	private Insets insets = new Insets(0, 10, 0, 0);
	private int maxSampleLabelLength = 0;
	private int headerWidth = 0;
	private boolean isDrag = false;
	private boolean headerDrag = false;
	private boolean isSampleDrag = false;
	private boolean isShift = false;
	private boolean isShiftMove = false;
	 private boolean enableMoveable = true;
	 private int startColumn = 0;
	    private int startShift = 0;
	    private int endShift = 0;
	    private int startRow = 0;
	    private int labelLength = 0;
	    private int startShiftMove = 0;
	    private int endShiftMove = 0;
	private int sampleDragColumn = 0;
	private int headerDragRow = 0;
	private int dragColumn = 0;
	private int dragRow = 0;


	public  GenesetMembershipHeader(ArrayList<String>genesets) {
	
		setGene_set(genesets);
		  Listener listener = new Listener();
		  addMouseListener(listener);
		  addMouseMotionListener(listener);
	}
	
	 public void updateSizes(int useless, int setElementWidth) {
	      
	        setElementWidth(setElementWidth);
	        Graphics2D g = (Graphics2D)getGraphics();
	        if (g == null) {
	            return;
	        }
	       
	        FontMetrics hfm = g.getFontMetrics();
	        int maxHeight = 0;
	        String name;
	        int contentWidth=0;
	        final int size = this.gene_set.size();
	        for (int feature = 0; feature < size; feature++) {
	            name = this.gene_set.get(feature);
	            maxHeight = Math.max(maxHeight, hfm.stringWidth(name));
	        }
	        contentWidth = (this.gene_set.size())*elementWidth + insets.left + 4;
	        maxSampleLabelLength = maxHeight;
	        maxHeight += RECT_HEIGHT + hfm.getHeight() + 10;
	        setBackground(Color.white);
	        setSize(this.headerWidth, maxHeight);
	        setPreferredSize(new Dimension(this.headerWidth, maxHeight));
	        drawHeader(g);
	    }
	    
	    public void setHeaderWidth(int hw){
	    	this.headerWidth = hw;
	    }
	    
	
	
	
	/**
     * Paints the header into specified graphics.
     */
    public void paint(Graphics g1D) {
        super.paint(g1D);
        if (this.gene_set.size() < 1) {
            return;
        }
        Graphics2D g = (Graphics2D)g1D;
        drawHeader(g);
    }
    
    
    /**
     * Draws the header into specified graphics.
     */
    private void drawHeader(Graphics2D g) {
        final int samples = this.gene_set.size();
        
        if (samples == 0) {
            return;
        }
        
        int width = samples*elementWidth;
        
       
        FontMetrics hfm = g.getFontMetrics();
        int descent = hfm.getDescent();
        int fHeight = hfm.getHeight();
        
        g.setColor(Color.black);
        
       
        int h = -getSize().height + 5;
        // draw feature names
        String name;
        g.rotate(-Math.PI/2);
        for (int sample = 0; sample < samples; sample++) {
            name = this.gene_set.get(sample);
            g.drawString(name, h, descent + elementWidth*sample + elementWidth/2 + insets.left);
        }
     
       
    }  
    
    
    /**
     * Sets an element width.
     */
    private void setElementWidth(int width) {
        this.elementWidth = width;
        if (width > 12) {
            width = 12;
        }
        setFont(new Font("monospaced", Font.PLAIN, width));
    }
    
	
	public JComponent getContentComponent() {
		// TODO Auto-generated method stub
		return this;
	}

	
	public void setAntiAliasing(boolean isAntiAliasing) {
		// TODO Auto-generated method stub
		
	}

	
	public void setClusterIndex(int index) {
		// TODO Auto-generated method stub
		
	}

	
	public void setData(IData data) {
		// TODO Auto-generated method stub
		
	}

	
	public void setLeftInset(int leftMargin) {
		// TODO Auto-generated method stub
		
	}

	
	public void setNegAndPosColorImages(BufferedImage neg, BufferedImage pos) {
		// TODO Auto-generated method stub
		
	}

	
	public void setUseDoubleGradient(boolean useDouble) {
		// TODO Auto-generated method stub
		
	}

	
	public void setValues(float minValue, float maxValue) {
		// TODO Auto-generated method stub
		
	}
	
	
	  private class Listener extends MouseAdapter implements MouseMotionListener {

	
		public void mouseDragged(MouseEvent event) {
			 repaint();
            if (SwingUtilities.isRightMouseButton(event)) {
            	isShiftMove=false;
            	isSampleDrag = false;
                return;
            }
            if(event.isShiftDown()){
            	isSampleDrag = false;
            	return;
            }
            int column = findColumn(event.getX());
            int row = findRow(event.getY());
            if (column==-1){
            	isShiftMove=false;
            	isSampleDrag = false;
            	return;
            }
            if (isShift&&enableMoveable){
        		isShiftMove=true;
            	endShiftMove = column;
            	if (startShiftMove-endShiftMove>Math.min(startShift, endShift))
            		endShiftMove = startShiftMove-Math.min(startShift, endShift);
            	if (endShiftMove-startShiftMove>getGene_set().size()-Math.max(startShift, endShift)-1)
            		endShiftMove = startShiftMove+getGene_set().size()-Math.max(startShift, endShift)-1;
            	return;
            }
            sampleDragColumn = column;
        	isSampleDrag = true;

        	if (!enableMoveable){
            	isShiftMove=false;
            	isSampleDrag = false;
        	}
            if (!isLegalPosition(row, column)) {
            	headerDrag = false;
                return;
            }
            if (!headerDrag)
            	return;
            headerDragRow = row;
        	if (column<getGene_set().size()){
        		
        	} else{
        		headerDrag = false;
        		isSampleDrag = false;
        	}
			
		}

		public void mouseMoved(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}
	  
	  
	  
	  }
	  /**
	     * Finds column for specified x coordinate.
	     * @return -1 if column was not found.
	     */
	    private int findColumn(int targetx) {
	        int xSize = this.gene_set.size()*elementWidth;
	        if (targetx < insets.left) {
	            return -1;
	        }
	        if (targetx > (xSize + insets.left))
	        	return -1;
	        return (targetx - insets.left)/elementWidth;
	    }
	    
	    /**
	     * Finds row for specified y coordinate.
	     * @return -1 if row was not found.
	     */
	    private int findRow(int targety) {
	    	int length = 0;
	    	
	        if (targety >= this.getSize().height || targety < (length))
	            return -1;
	        return (targety - length)/COLOR_BAR_HEIGHT;
	    }
	    
	    private boolean isLegalPosition(int row, int column) {
	        if (isLegalRow(row) && isLegalColumn(column))
	            return true;
	        return false;
	    }  
	    
	    private boolean isLegalColumn(int column) {
	        if (column < 0 || column > (getGene_set().size() -1))
	            return false;
	        return true;
	    }
	    
	    private boolean isLegalRow(int row) {
	     
	      
	        return true;
	    }

		public ArrayList<String> getGene_set() {
			return gene_set;
		}

		public void setGene_set(ArrayList<String> gene_set) {
			this.gene_set = gene_set;
		}


	

}
