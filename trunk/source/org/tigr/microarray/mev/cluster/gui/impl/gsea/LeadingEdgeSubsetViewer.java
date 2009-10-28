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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IDisplayMenu;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.IViewer;


public class LeadingEdgeSubsetViewer extends JPanel implements IViewer{
	
	private LinkedHashMap<String, Float>sorted;
	private float maxIncrementalSum;
	private int peakGeneIndex;
	private DecimalFormat coordinateFormat;
	private FontMetrics metrics;
	private float stepsY;
	private float minYVal;
	private float maxYValue;
	private boolean isAntiAliasing=false;
	private Color bColor=Color.BLACK;
	private float[]incrementalSum;
	private boolean referenceLinesOn=true;
	private int xOldEvent;
	private int yOldEvent;
	private boolean mouseInside = false;
	private boolean showCoordinates = true;
	private java.util.EventListener eventListener;
	private Object[]genes;
	
	
	public LeadingEdgeSubsetViewer(LinkedHashMap<String, Float>sortedTStat) {
		sorted=sortedTStat;
		genes=new String[sortedTStat.keySet().size()];
		genes=sortedTStat.keySet().toArray();
		new LinkedHashMap<String, Float>();
		coordinateFormat = new DecimalFormat();
    	coordinateFormat.setMaximumFractionDigits(3);
		incrementalSum=new float[sorted.keySet().size()];
		incrementalSum=getIncrementalSubsetSum();
		setBackground(Color.WHITE);
	 
    	eventListener = new Listener();
    	this.addMouseMotionListener((MouseMotionListener) eventListener);
    	this.addMouseListener((MouseListener) eventListener);
    	

	}

	
	
	public float[] getIncrementalSubsetSum(){
		float temp=Float.MIN_VALUE;
		Iterator keys=sorted.keySet().iterator();
		ArrayList tempArray=new ArrayList();
		
		
		for(int j=0; j<sorted.size(); j++) {
			String key=(String)keys.next();
			float value=Float.parseFloat(coordinateFormat.format(((Float)sorted.get(key)).floatValue()));
		
			
			tempArray.add(value);
			float tempSum=0.0f;
			
			
			for(int incrIndex=0; incrIndex<tempArray.size(); incrIndex++) {
				tempSum=tempSum+((Float)tempArray.get(incrIndex)).floatValue();
				
			}
		
			incrementalSum[j]=Float.parseFloat(coordinateFormat.format(tempSum/Math.sqrt(j+1)));
			if(incrementalSum[j]==Math.max(temp, incrementalSum[j])) {
				temp=incrementalSum[j];
				setPeakGeneIndex(j);
			}
			
//			System.out.println("Key:"+key);
//			System.out.println("incremental sum:"+incrementalSum[j]);
		}
		
	//	System.out.println("-----------------------------------------------");
		setMaximumIncrementalSum(temp);
		
		
		return incrementalSum;
	}
	
	
	
	public void setMaximumIncrementalSum(float max) {
		maxIncrementalSum=max;
	}
	
	public float getMaximumIncrementalSum() {
		return maxIncrementalSum;
	}
	
	public void setPeakGeneIndex(int index) {
		peakGeneIndex=index;
	}
	
	public int getPeakGeneIndex() {
	 return peakGeneIndex;	
	}
	
	
	/**
	 * maxYValue returns the value of maximum test statistics for a gene set
	 * @return
	 */
	public float getmaxYValue(){
		float maxYValue=getMaximumIncrementalSum();
		
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
				
		for(int index=0; index<incrementalSum.length; index++) {
			float temp=incrementalSum[index];
			
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
		return sorted.size();		
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
	
	
	  /**
     * Paints chart into specified graphics.
     */
    public void paint(Graphics g) {
        FontMetrics metrics = g.getFontMetrics();
        Rectangle rect = new Rectangle(40, 20, getWidth()-80, getHeight() - 40 - getNamesWidth(metrics));
      
        paint((Graphics2D)g, rect);
    }
    
    
    
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

    	if (rect.width < 5 || rect.height < 5) {
    		return;
    	}

    	float maxYValue = getmaxYValue();
    	float minYValue = getMinYValue();
    	final  float dash1[] = {10.0f};
	    final  BasicStroke dashed = new BasicStroke(3.0f, 
	                                          BasicStroke.CAP_BUTT, 
	                                          BasicStroke.JOIN_MITER, 
	                                          10.0f, dash1, 0.0f);
    	
    	setCurrentMaxYValue(maxYValue);
    	setCurrentMinYValue(minYValue);

    	// stepsY is calculated by dividing the maximum observed statistic values 
    	// by a predefined number (5 in this case). This many
    	// markings fit in better in the GUI with enough breathing room.
    	//Maximum value is what matters the most in this plot. Some times the difference
    	//between maximum value and minimum is not big enough and results in less than 5 points getting plotted on the
    	//graph. To avoid this, i just divide whatever the maximum value is by 5


   		stepsY=(maxYValue/5);
	
    	


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

    	
    	final int numberOfGenes = getMaxXValue();
    	int oldycoord=0;
    	int oldxcoord=0;

    	final float factor = height / (5);
       	final float stepX = width / (float) (numberOfGenes);

    	// Draw x axis label
    	g.setStroke(new BasicStroke(2));
    	String xlabel = "Genes ranked by test statistic";
    	String ylabel = "J G  Statistic  for  incremental  subsets";
    	Font font = new Font("Dialog", Font.PLAIN, 12);

    	g.setFont(font);
    	g.drawString(xlabel, getWidth() / 2, getHeight());

    	
    

    	// draw rectangle
    	g.setColor(Color.black);
    	g.drawRect(left, top, width, height);
    
    	g.rotate(-Math.PI / 2.0, left-getMaximumStringWidth()-18,(top+(height/2)) );
    	g.drawString(ylabel, left-getMaximumStringWidth()-18, ((height/2)+top));
    	g.rotate(Math.PI / 2.0, left-getMaximumStringWidth()-18, ((height/2)+top));


    	g.setStroke(new BasicStroke(2));
    	g.setColor(Color.black);

    	// draw Y axis markings
    	//Maximum y value is critical here. Indicates the maximum J-G statistic. 
    	//Y axis contains values from lowest y value to the highest. 
//    	System.out.println("max Y value:"+getCurrentMaxYValue());
//    	System.out.println("min Y value:"+getCurrentMinYValue());
//    	System.out.println("stepY:"+getStepsY());
//

    	// draw Y axis markings and labels
    	int yaxis=(int)(top+height-factor);
    	
    	int strWidth=getMaximumStringWidth();
		for (float i = getStepsY(); i < getCurrentMaxYValue()+getStepsY(); i = i + getStepsY()) {
			
			
			g.drawLine(left, yaxis,	left + 5, yaxis);
			g.drawString(coordinateFormat.format(i), left-10-strWidth, yaxis);
			yaxis=yaxis-(int)factor;
			
			if(yaxis<top && i==getCurrentMaxYValue()) {
				yaxis=top;
			}

		}
		
		g.drawString(getMaxXValue() + " Genes", left + 10, top + 20);


    	//Draw the markings above x axis labels. If there are more than 80 samples, sample names get squished. 
    	//so restrict the number of markings and sample names to what would fit in and be displayed properly in the rectangle 
    	
    	if(numberOfGenes <=80) {
    	for (int i = 1; i <= getMaxXValue(); i++) {
    		g.drawLine(left + (int) Math.round(i * stepX), top + height - 5,
    				left + (int) Math.round(i * stepX), top + height);

    	}
    	}else {
    		int temp=(numberOfGenes/5);
    		int gap=numberOfGenes/temp;
    		for (int i = 1; i <numberOfGenes+1; i=i+gap) {
        		g.drawLine(left + (int) Math.round(i * stepX), top + height - 5,
        				left + (int) Math.round(i * stepX), top + height);

        	}
    	}
    	// draw X samples names
    	g.rotate(-Math.PI / 2.0);

    	final int max_name_width = getNamesWidth(metrics);
    	
    	if(numberOfGenes<=80) {
        	for (int i = 1; i < numberOfGenes + 1; i++) {
    		g.drawString(Integer.toString(i), -height - top - 10
    				- max_name_width, left + (int) Math.round(i * stepX) + 3);

        	}

    	}else {
    		int temp=(numberOfGenes/5);
    		int gap=numberOfGenes/temp;
    		for (int i = 1; i < numberOfGenes+1; i=i+gap) {
        		g.drawString(Integer.toString(i), -height - top - 10
        				- max_name_width, left + (int) Math.round(i * stepX) + 3);

        	}
    	}
    	g.rotate(Math.PI / 2.0);
    	
    
		for (int index = 1; index < numberOfGenes + 1; index++) {
			
		
			float tStat =incrementalSum[index-1] ;
			
			int xcoord=left + (int) Math.round(index * stepX) + 3;
			int ycoord=(int)(top+height-factor);
			
		
			
			int count=0;
			
			g.setColor(new Color(128,0,128));
			g.setStroke(new BasicStroke(6));
		
			for(float ind=getStepsY();ind<=getmaxYValue()+getStepsY();ind=ind+getStepsY() ) {
				 String t=coordinateFormat.format(ind);
				 float tempVar=Float.parseFloat(t);
				 float step=Float.parseFloat(coordinateFormat.format(getStepsY()));
				
				 if(tStat<getStepsY()) {
					 ycoord=top+height;
					 g.drawLine(xcoord,ycoord,xcoord,ycoord);
					
					 break;
				 }
				 
				if(tStat<tempVar && tStat>(tempVar-step)) {
					
					float unknown=((10*Math.abs(tempVar))-(10*Math.abs(tStat)))/step;
					ycoord=(int)(ycoord-factor*count);
					ycoord=	Math.round(ycoord+(unknown*10));
					g.drawLine(xcoord,ycoord,xcoord,ycoord);
					break;
				}else if(tStat==tempVar) {
					if(tStat==getStepsY()) {
						ycoord=(int)(top+height-factor);
						g.drawLine(xcoord,ycoord,xcoord,ycoord);
					}else {
							
					ycoord=(int)(ycoord-factor*count);
					
					g.drawLine(xcoord,ycoord,xcoord,ycoord);
					
		
					}
					
					
					break;
				}
				
				if(count < 4) {
						count=count+1;
						
					}
				
			
			}
			if(index==1) {
				oldycoord=ycoord;
				oldxcoord=xcoord;
			}else {
				g.setColor(new Color(128,0,128));
				g.setStroke(new BasicStroke(6));
				g.drawLine(oldxcoord,oldycoord,xcoord,ycoord);
				oldxcoord=xcoord;
				oldycoord=ycoord;
			}
			//Draw the red vertical line that denotes peak
			if(index==(getPeakGeneIndex()+1)) {
				g.setColor(Color.RED);
				g.setStroke(dashed);
				g.drawLine(xcoord,ycoord,xcoord,(top+height));
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
					g.drawString("Test stat:"+Float.toString(incrementalSum[(int)xVal]), x , y);
					}
					
				}
			} else {
				this.setCursor(Cursor.DEFAULT_CURSOR);
			}
		}
    	
  
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
    
    public String[][] getLeadingEdgeGenes() {
    	String[][] leadinggenes=new String[getPeakGeneIndex()+1][2];
    	Iterator<String> it=sorted.keySet().iterator();
    	for(int index=0; index<getPeakGeneIndex()+1; index++) {
    		leadinggenes[index][0]=it.next();
    		leadinggenes[index][1]=Float.toString(incrementalSum[index]);
    		
    	
    		
    	}
    
    	
    	
    	return leadinggenes;
    	
    }
    
    
    
    
    
    
	public int[][] getClusters() {
		// TODO Auto-generated method stub
		return null;
	}

	
	public JComponent getContentComponent() {
		return this;
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

	
	

	 public Expression getExpression(){
	    	return new Expression(this, this.getClass(), "new", 
	    			new Object[]{sorted});
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
		           // popup.show(e.getComponent(), e.getX(), e.getY());   
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
