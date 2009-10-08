package org.tigr.microarray.mev.cluster.gui.impl.gsea;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.beans.Expression;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.LinkedHashMap;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IDisplayMenu;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.IViewer;



public class TestStatisticViewer extends JPanel implements IViewer{
	//sortedTestStats will contain the name of the gene as key and its test statistic as value
	private LinkedHashMap<String, Float> sortedTestStats=new LinkedHashMap<String, Float>();
	private Object[]genes;
	private Object[]testStats;
	private boolean isAntiAliasing=false;
	private static final Color bColor = new Color(0, 0, 128);
	private DecimalFormat coordinateFormat;
	private FontMetrics metrics;
	private float stepsY;
	private float minYVal;
	private float maxYValue;
	private boolean referenceLinesOn=true;
	private int xOldEvent;
	private int yOldEvent;
	private boolean mouseInside = false;
	private boolean showCoordinates = true;
	private JPopupMenu popup;
	private java.util.EventListener eventListener;
	
	public TestStatisticViewer(LinkedHashMap<String, Float> sortedTStats){
		sortedTestStats=sortedTStats;
		genes=new String[sortedTestStats.keySet().size()];
		testStats=new Float[genes.length];
		genes=sortedTestStats.keySet().toArray();
		testStats=sortedTestStats.values().toArray();
		
		setBackground(Color.WHITE);
		coordinateFormat = new DecimalFormat();
    	coordinateFormat.setMaximumFractionDigits(3);
    	eventListener = new Listener();
    	this.addMouseMotionListener((MouseMotionListener) eventListener);
    	this.addMouseListener((MouseListener) eventListener);
    	

    	
	}
	
	
	
	/**
	 * maxYValue returns the value of maximum test statistics for a gene set
	 * @return
	 */
	public float getmaxYValue(){
		float maxYValue=Float.MIN_VALUE;
		Iterator<String>it=sortedTestStats.keySet().iterator();
		
		while(it.hasNext()){
			float temp=sortedTestStats.get(it.next()).floatValue();
			
			if(!Float.isNaN(temp)){
				maxYValue=Math.max(temp, maxYValue);
			}
			
		}
		return maxYValue;
	}
	
	
	public void setCurrentMaxYValue(float max) {
		if(!Float.isNaN(max))
			maxYValue=max;
		
	}
	
	public float getCurrentMaxYValue() {
		return maxYValue;
	}
	
	
	/**
	 * getMinYVal returns the value of minimum test statistic for a gene set
	 * @return
	 */
	public float getMinYValue(){
		float minYVal=Float.MAX_VALUE;
		Iterator<String>it=sortedTestStats.keySet().iterator();
		
		while(it.hasNext()){
			float temp=sortedTestStats.get(it.next()).floatValue();
			
			if(!Float.isNaN(temp)){
				minYVal=Math.min(temp, minYVal);
			}
			
		}
		return minYVal;
		
	}
	
	public void setCurrentMinYValue(float value) {
		if(!Float.isNaN(value))
			minYVal=value;
				
	}
	
	public float getCurrentMinYValue() {
		return minYVal;
	}
	
	
	/**
	 * getMaxXValue returns the total number of genes present in the geneset
	 * @return
	 */
	public int getMaxXValue(){
		return sortedTestStats.size();		
	}
	
	public void setStepsY(float stepY){
		stepsY=stepY;
	}
	
	
	public float getStepsY(){
		return stepsY;
	}
	
	
	public FontMetrics getFontMetric(){
		return metrics;
	}
	
	
	public void setFontMetric(FontMetrics metric){
		 metrics=metric;
	}
	
	public JComponent getContentComponent() {
		return this;
	}

	  /**
     * Paints chart into specified graphics.
     */
    public void paint(Graphics g) {
        FontMetrics metrics = g.getFontMetrics();
        Rectangle rect = new Rectangle(40, 20, getWidth()-80, getHeight() - 40 - getNamesWidth(metrics));
      
        paint((Graphics2D)g, rect);
    }
    
  
  
    /**
     * Paints chart into specified graphics and with specified bounds.
     */
    public void paint(Graphics2D g, Rectangle rect) {
		super.paint(g);
		FontMetrics metrics = g.getFontMetrics();
		setFontMetric(metrics);

		if (isAntiAliasing) {
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_OFF);
			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
					RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		}

		String str;
		int strWidth;

		if (rect.width < 5 || rect.height < 5) {
			return;
		}
		
		float previousStat=-1;
		//If miny =-1.8  and maxy=0.4, the plot goes off charts, so new way of calculating max
		float maxYValue = Math.max(Math.abs(getmaxYValue()), Math.abs(getMinYValue()));
		float minYValue = getMinYValue();
		
		
		// If maximum Y value is < 0, or 1. Just make maxY value 1 and
		// have steps be equal to 1/5. 5 was selected because that is the
		// maximum number of values that can be seen properly on the y axis
		if (maxYValue <= 0.0f || maxYValue < 1) {
		//	maxYValue = 1.0f;
			maxYValue = Math.max(1.0f, Math.abs(maxYValue));
			stepsY = (maxYValue) / 5;
			minYValue = stepsY;
			setCurrentMaxYValue(maxYValue);
			setCurrentMinYValue(minYValue);
		} else {

			// stepsY is calculated by adding up the maximum and minimum
			// observed statistic values and
			// dividing by a predefined number (5 in this case). This many
			// markings fit in better in the GUI with enough breathing room.

			if (getMinYValue() < 0)
				stepsY = (maxYValue - minYValue) / 5;
			else
				stepsY = (maxYValue + minYValue) / 5;
			setCurrentMaxYValue(maxYValue);
			setCurrentMinYValue(minYValue);
		}
		
		setStepsY(stepsY);

		// These values make sure that all Y axis values are visible and that
		// the boundaries of the rectangle do not exceed the JPanel
		Rectangle rectangle = new Rectangle(2 * getMaximumStringWidth(), 20,
				getWidth() - 80, getHeight() - 40 - getNamesWidth(metrics));
		final int left = rectangle.x;
		final int top = rectangle.y;
		
		if((left+rectangle.width)>getWidth()) {
			int diff=(left+rectangle.width)-getWidth();
			rectangle=new Rectangle(2 * getMaximumStringWidth(), 20,
				getWidth() - getMaximumStringWidth()-diff-80, getHeight() - 40 - getNamesWidth(metrics));
		}
		final int width = rectangle.width;
		final int height = rectangle.height;
	
		// System.out.println("max Y value:"+maxYValue);
		// System.out.println("min Y value:"+minYValue);
		// System.out.println("stepsY:"+stepsY);
		final int zeroValue = top + (int) Math.round(height / 2f);
		final int numberOfGenes = getMaxXValue();

		final float factor = height / (2f * maxYValue);
		final float stepX = width / (float) (numberOfGenes);

		// Draw x axis label
		g.setStroke(new BasicStroke(2));
		String xlabel = "Gene Indices";
		String ylabel = "Test Statistic";
		Font font = new Font("Dialog", Font.PLAIN, 12);

		g.setFont(font);
		g.drawString(xlabel, getWidth() / 2, getHeight());
		
//		System.out.println("left:"+left);
//		System.out.println("left-getMaximumStringWidth:"+(left-getMaximumStringWidth()));
//		System.out.println("ZeroValue:"+zeroValue);
		
	
		g.rotate(-Math.PI / 2.0, left-getMaximumStringWidth()-18,zeroValue );
    	g.drawString(ylabel, left-getMaximumStringWidth()-18, zeroValue);
    	g.rotate(Math.PI / 2.0, left-getMaximumStringWidth()-18, zeroValue);

		
		// draw rectangle
		g.setColor(Color.black);
		g.drawRect(left, top, width, height);
		//System.out.println("ZeroValue:"+zeroValue);
		
		// draw zero line
		g.setColor(Color.GREEN);
		g.setStroke(new BasicStroke(4));
		g.drawLine(left, zeroValue, left + width, zeroValue);
		
		// draw X items
		g.setStroke(new BasicStroke(2));
		g.setColor(Color.black);
		for (int i = 1; i <= getMaxXValue(); i++) {
			g.drawLine(left + (int) Math.round(i * stepX), top + height - 5,
					left + (int) Math.round(i * stepX), top + height);
			g.drawLine(left + (int) Math.round(i * stepX), top, left
					+ (int) Math.round(i * stepX), top + 5);
		}
		// draw Y items
		for (float i = stepsY; i <= maxYValue; i = i + stepsY) {
			g.drawLine(left, zeroValue - (int) Math.round(i * factor),
					left + 5, zeroValue - (int) Math.round(i * factor));
			g.drawLine(left, zeroValue + (int) Math.round(i * factor),
					left + 5, zeroValue + (int) Math.round(i * factor));
		}

		// draw X samples names
		g.rotate(-Math.PI / 2.0);
		
		final int max_name_width = getNamesWidth(metrics);
		for (int i = 1; i < numberOfGenes + 1; i++) {
			g.drawString(Integer.toString(i), -height - top - 10
					- max_name_width, left + (int) Math.round(i * stepX) + 3);

		}
		g.rotate(Math.PI / 2.0);

		// draw Y axis markings
		g.setColor(bColor);
		
		strWidth = getMaximumStringWidth();
		for (float i = stepsY; i < maxYValue+1+stepsY; i = i + stepsY) {

			str = coordinateFormat.format(i);
			// Draw the string on to the panel. To make sure the spacing is
			// uniform between the markings, use the maximum string width.
			g.drawString(str, left - 10 - strWidth, zeroValue + 5
					- (int) Math.round(i * factor));
			str = coordinateFormat.format(-i);
			g.drawString(str, left - 10 - strWidth, zeroValue + 5
					+ (int) Math.round(i * factor));
		}
		g.drawString(getMaxXValue() + " Genes", left + 10, top + 20);

		// Plot the points in graph and join the points with a line
	
		Iterator<String> keys = sortedTestStats.keySet().iterator();
		
		g.setColor(new Color(255, 140, 0));
		g.setStroke(new BasicStroke(6));
		for (int index = 1; index < numberOfGenes + 1; index++) {

			float tStat = (Float) sortedTestStats.get(keys.next()).floatValue();
			if(index==1) {
				previousStat=tStat;
			}
//			System.out.println("gene index:"+index);
//			System.out.println("Plotting point:"+tStat);
		
			
			
			int xcoord=left + (int) Math.round(index * stepX) + 3;
		//	System.out.println("Calculated x coordinate:"+tStat);
			if(tStat<0) {
			int ycoord=zeroValue- (int) Math.round(tStat * factor);
		//	System.out.println("Calculated y coordinate:"+ycoord);
			
			g.setStroke(new BasicStroke(6));
			g.drawLine(xcoord,ycoord,xcoord,ycoord);
			if(index!=1) {
				int x1=left + (int) Math.round((index-1) * stepX) + 3;
				int y1=zeroValue- (int) Math.round(previousStat * factor);
				int x2=left + (int) Math.round(index * stepX) + 3;
				int y2=ycoord;
				g.setStroke(new BasicStroke(3));
				g.drawLine(x1,y1,x2,y2);
				previousStat=tStat;
			}
			}else if(tStat >=0) {
				int ycoord=zeroValue- (int) Math.round(tStat * factor);
				//System.out.println("Calculated y coordinate:"+ycoord);
				
				g.setStroke(new BasicStroke(6));
				g.drawLine(xcoord,ycoord,xcoord,ycoord);
				if(index!=1) {
					int x1=left + (int) Math.round((index-1) * stepX) + 3;
					int y1=zeroValue- (int) Math.round(previousStat * factor);
					int x2=left + (int) Math.round(index * stepX) + 3;
					int y2=ycoord;
					g.setStroke(new BasicStroke(3));
					g.drawLine(x1,y1,x2,y2);
					previousStat=tStat;
				}
				
			}
			
				

		}
		
		
		 if (referenceLinesOn) { // Grid tracing is active
				int x = getXOldEvent();
				int y = getYOldEvent();
				//System.out.println("value of x is:"+x);
				//System.out.println("value of y is:"+y);
				int coordinateWidth;
				double xVal = 0;
				double yVal = 0;

				boolean onGraph = ((x <= (width+left))
						&& (x >= left)
						&& (y <= (top+height))
						&& (y >= top));
				
				if (onGraph) {

					this.setCursor(Cursor.CROSSHAIR_CURSOR);

					
					xVal=Math.round((x-left)/stepX)-1;
					yVal = ((getCurrentMinYValue() - y) / factor)
							+ getCurrentMinYValue();
					
					//g.setColor(Color.magenta);
					///g.drawLine(x, (int)Math.round(getCurrentMinYValue()), x, (int)Math.round(getCurrentMaxYValue()));
					//g.drawLine(1, y, numberOfGenes+1, y);

					if (this.showCoordinates) {
						
						coordinateWidth = metrics.stringWidth(coordinateFormat.format(xVal)
								+ ", " + coordinateFormat.format(yVal));
						Composite comp = g.getComposite();
						g.setComposite(AlphaComposite.getInstance(
							AlphaComposite.SRC_OVER, 0.2f));
						
										
						
						
						if((int)xVal<genes.length+1 && (int)xVal>=0) {
						g.setColor(Color.blue);
						//g.fillRect(x, y, (metrics.stringWidth("Test stat:")+30), metrics.stringWidth("Test stat"));
						g.setComposite(comp);
						g.setColor(bColor);
						g.drawString("Gene: "+genes[(int)xVal], x , y - 20);
						g.drawString("Test stat:"+(testStats[(int)xVal]).toString(), x , y);
						}
						
					}
				} else {
					this.setCursor(Cursor.DEFAULT_CURSOR);
				}
			}
		
	
		
	}


   
    
    
    public int getMaximumStringWidth(){
    	int maxWidth=Integer.MIN_VALUE;
    	float step=getStepsY();
    	for(float i=getMinYValue(); i<=getmaxYValue(); i=i+step){
    		String str=coordinateFormat.format(i);
        	int strWidth = metrics.stringWidth(str);
    		
        	if(strWidth > maxWidth)
        		maxWidth=strWidth;
        	
        	str=coordinateFormat.format(-i);
        	strWidth = metrics.stringWidth(str);
    		
        	if(strWidth > maxWidth)
        		maxWidth=strWidth;
        	
        	
    	}
    	
     	
    	return maxWidth;
    }
    
    
    
    
    /**
     * Sets the anti-aliasing attribute.
     */
    public void setAntiAliasing(boolean value) {
        this.isAntiAliasing = value;
    }
  
    
    /**
     * Returns max width of gene indices.
     */
    protected int getNamesWidth(FontMetrics metrics) {
        int maxWidth = 0;
        for (int i=1; i<=getMaxXValue(); i++) {
            maxWidth = Math.max(maxWidth, metrics.stringWidth(Integer.toString(i)));
        }
        return maxWidth;
    }



	@Override
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
		return null;
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
		return 0;
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
		
	}



	
	public void setExperiment(Experiment e) {
		// TODO Auto-generated method stub
		
	}



	
	public void setExperimentID(int id) {
		// TODO Auto-generated method stub
		
	}
  
    private class Listener implements ActionListener, MouseListener, MouseMotionListener{
    	
    	
    	
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			if (e.getActionCommand() == "Reference Lines") {
                referenceLinesOn = (! referenceLinesOn);
                repaint();
            }	
		}
		
		public void mouseDragged(MouseEvent e) {
			// TODO Auto-generated method stub
			 int x = e.getX();
             int y = e.getY();
             
		}

		
		public void mouseMoved(MouseEvent e) {
			// TODO Auto-generated method stub
			 int x = e.getX();
	         int y = e.getY();
//	          System.out.println("x in mouse moved:"+x);
//	          System.out.println("y in mouse moved:"+y);
	            setXOldEvent(x);
	            setYOldEvent(y);
	            repaint();
		}


		
		public void mouseClicked(MouseEvent e) {
			// TODO Auto-generated method stub
			 if(e.getModifiers() == MouseEvent.BUTTON3_MASK){
		            popup.show(e.getComponent(), e.getX(), e.getY());   
		            }
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
	

}
