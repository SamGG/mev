package org.tigr.microarray.mev.cluster.gui.impl.attract;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.beans.Expression;

import javax.swing.JComponent;
import javax.swing.JPanel;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;


import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IDisplayMenu;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.IViewer;
import org.tigr.util.FloatMatrix;
/**
 * SynExpressionViewer generates average gene expression profile plot for a synexpression group.
 * X axis comprises of samples (grouped according to corresponding factor levels)and Y axis consists
 * of log (gene expression values)
 * @author sarita
 *
 */
public class SynExpressionViewer extends JPanel implements IViewer {
	
	
	//Average gene expression profiles of genes across samples 
	private FloatMatrix synExpression;
	//Log of average gene expression profiles of genes across samples 
	private FloatMatrix logSynGeneExpression;
	//Correlated gene expression profiles. These are the genes in the expression data whose expression is correlated to genes 
	//found in synexpression groups
	private FloatMatrix corrGeneExpression;
	//Log of the correlated gene expression profile
	private FloatMatrix logCorrGeneExpression;
	//Group assignments as stored in AlgorithmData
	private int[][]groupAssignments;
	//Factor levels
	private int[]factorLevels;
	//Smallest value tobe plotted on Y axis
	private float minYValue;
	//Largest value to be plotted on y axis
	private float maxYValue;
	//Interval between Y axis markings
	private float stepsY;
	//List containing samples according to factor levels
	private LinkedList<Integer> reorderedSamples;
	//List containing number of samples in every factor level
	private LinkedList<Integer>numSamplesPerGroup;
	private DecimalFormat coordinateFormat;
	private FontMetrics metrics;
	private boolean isAntiAliasing=false;
	
	
	/**
	 * Constructs a SynExpressionViewer object
	 * @param expressionVals --expression value of genes in a synexpression group
	 * @param corGeneExpression -- expression value of genes correlated to the genes in synexpression group 
	 * @param grpAssign --sample group assignment
	 * @param fLevels --number of levels of the groups
	 */
	
	public SynExpressionViewer( FloatMatrix expressionVals, FloatMatrix corGeneExpression, int[][]grpAssign, int[]fLevels) {
		synExpression=expressionVals;
		corrGeneExpression=corGeneExpression;
		groupAssignments=grpAssign;
		factorLevels=fLevels;
		setBackground(Color.WHITE);
		coordinateFormat = new DecimalFormat();
    	coordinateFormat.setMaximumFractionDigits(3);
    
    	//first log expression values
    	logSynGeneExpression=findLog(synExpression);
    	logCorrGeneExpression=findLog(corrGeneExpression);
    	
    	//Calculate minimum and maximum values to be plotted on Y axis
    	calculateMaxYValue(logSynGeneExpression, logCorrGeneExpression);
    	calculateMinYValue(logSynGeneExpression, logCorrGeneExpression);
    	//Reorder the samples to group together ones which have same factor levels 
    	reorderedSamples=reorderSamplesByGroups(groupAssignments, factorLevels);
    	
    	
    	

	}
	
	
	
	/**
	 * Returns a list containing samples reordered by factor levels. For e.g.
	 * Factor = gender and levels are M and F and assume there are four samples
	 * Sample 1 M
	 * Sample 2 F
	 * Sample 3 M
	 * Sample 4 F
	 * This function would return a list which would look like (Sample1,  Sample3, Sample2, Sample4) 
	 * 
	 * This function would not work if there are multiple factors to account for.
	 * Attract only allows one factor, so it should be fine.
	 * 
	 * @param assignment
	 * @return
	 */
	public LinkedList<Integer> reorderSamplesByGroups(int[][]assignment, int[] factorLevels) {
		LinkedList<Integer>temp=new LinkedList<Integer>();
		numSamplesPerGroup=new LinkedList<Integer>();
		//Attract allows only one factor, so factor level array would have just one row (Each row in a factor level array corresponds to level of a factor)
		for(int level=1; level<(factorLevels[0]+1); level++) {
			for(int index=0; index<assignment[0].length; index++) {
				//Attract currently allows only one factor hence need to worry about just one row
				if(assignment[0][index]==level) {
					temp.add(index);
				}

			}
			//Sample number where a particular level ends. Required to plot vertical demarcating lines in graph.  
			if(temp.size()!=assignment[0].length) {
				numSamplesPerGroup.add(level-1, temp.size()-1);
			}
			
		}
		
		
		return temp;
	}
	
	
	/**
	 * 
	 * @param expressionVals --FloatMatrix containing average gene expression values of a synexpression group
	 * @return a float matrix containing the log2 of the gene expression values 
	 */
	public FloatMatrix findLog(FloatMatrix expressionVals) {
		FloatMatrix temp=new FloatMatrix(expressionVals.getRowDimension(), expressionVals.getColumnDimension());
		
		for(int col=0; col<expressionVals.getColumnDimension(); col++) {
			float log=new Float(Math.log(expressionVals.get(0, col))/Math.log(2)).floatValue();
			temp.set(0, col, log);
		}
		
	
		return temp;
	}
	
	/**
	 * Calculates the maximum value to be plotted on the Y axis. Since expression profile of correlated genes
	 * in the entire expression data is shown along side the expression profile of the synexpression group,
	 * maximum Y value is the maximum among both the expression profiles. 
	 * 
	 * @param corrExpGrp
	 * @param synExpGrp
	 */
	
	public void calculateMaxYValue(FloatMatrix synExpGrp, FloatMatrix corrExpGrp) {
		maxYValue=Float.MIN_VALUE;
		for(int colIndex=0; colIndex<synExpGrp.getColumnDimension(); colIndex++) {
			
			maxYValue=Math.max(maxYValue, synExpGrp.get(0, colIndex));
			maxYValue=Math.max(maxYValue, corrExpGrp.get(0, colIndex));
		}
			
		
	}
	
	
	/**
	 * Calculates the minimum value to be plotted on the Y axis. Since expression profile of correlated genes
	 * in the entire expression data is shown along side the expression profile of the synexpression group,
	 * minimum Y value is the minimum among both the expression profiles. 
	 * 
	 * @param corrExpGrp
	 * @param synExpGrp
	 */
	
	public void calculateMinYValue(FloatMatrix synExpGrp, FloatMatrix corrExpGrp) {
		minYValue=Float.MAX_VALUE;
		for(int colIndex=0; colIndex<synExpGrp.getColumnDimension(); colIndex++) {
			
			minYValue=Math.min(minYValue, synExpGrp.get(0, colIndex));
			minYValue=Math.min(minYValue, corrExpGrp.get(0, colIndex));
		}
			
		
	}
	

	
	/**
	 * 
	 * @returns the number of samples in the data
	 */
	public int getMaxXValue() {
		return synExpression.getColumnDimension();
	}

	

	/**
	 * 
	 * @return the minimum Y value
	 */
	
	public float getMinYValue() {
		return minYValue;
	}


	
	/**
	 * 
	 * @returns the maximum Y value
	 */
	public float getMaxYValue() {
		return maxYValue;
	}

	public void setStepsY(float stepY){
		stepsY=stepY;
	}
	
	
	public float getStepsY(){
		return stepsY;
	}
	
	/**
	 * Calculates the maximum width of the values to be plotted on the Y axis.
	 * Helps make sure that all Y axis values are visible.
	 * 
	 * @return maximum string width 
	 */
	public int getMaximumStringWidth() {
		int maxWidth = Integer.MIN_VALUE;
		float step = getStepsY();
		for (float i = getMinYValue(); i <= getMaxYValue(); i = i + step) {
			String str = coordinateFormat.format(i);
			int strWidth = metrics.stringWidth(str);

			if (strWidth > maxWidth)
				maxWidth = strWidth;

			str = coordinateFormat.format(-i);
			strWidth = metrics.stringWidth(str);

			if (strWidth > maxWidth)
				maxWidth = strWidth;

		}

		return maxWidth;
	}
	
	
	public FontMetrics getFontMetrics() {
		return metrics;
	}


	public void setFontMetric(FontMetrics metrics) {
		this.metrics = metrics;
	}


	  /**
     * Paints chart into specified graphics.
     */
    public void paint(Graphics g) {
        FontMetrics metrics = g.getFontMetrics();
        paint((Graphics2D)g);
    }
    
  
  
    /**
     * Paints chart into specified graphics .
     */
    public void paint(Graphics2D g) {
    	super.paint(g);
		FontMetrics metrics = g.getFontMetrics();
		setFontMetric(metrics);

		if (isAntiAliasing) {
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_OFF);
			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
					RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		}
		
		
		
		//If miny =-1.8  and maxy=0.4, the plot goes off charts, so new way of calculating max
		float maxYValue = Math.max(Math.abs(getMaxYValue()), Math.abs(getMinYValue()));
		float minYValue = getMinYValue();
		
		
		stepsY=(maxYValue/5);
		
		setStepsY(stepsY);
		
		// These values make sure that all Y axis values are visible and that
		// the boundaries of the rectangle do not exceed the JPanel
		Rectangle rectangle = new Rectangle(2 * getMaximumStringWidth(), 20,
				getWidth() - 80, getHeight() - 40 -getNamesWidth(metrics));
		
		
		if((rectangle.x+rectangle.width)>getWidth()) {
			int diff=(rectangle.x+rectangle.width)-getWidth();
			rectangle=new Rectangle(2 * getMaximumStringWidth(), 20,
				getWidth() - getMaximumStringWidth()-diff-80, getHeight() - 40-getNamesWidth(metrics));
		}
		
		final int left = rectangle.x;
		final int top = rectangle.y;
	
		final int width = rectangle.width;
		final int height = rectangle.height;
		final int zeroValue = top + (int) Math.round(height / 2f);
		final int numberOfSamples = getMaxXValue();
		int oldSynYCoord=0;
    	int oldSynXCoord=0;
    	
    	int oldCorYCoord=0;
    	int oldCorXCoord=0;

		final float factor = height / (5);
		final float stepX = width / (float) (numberOfSamples);
		//Keep a track of number of groups. Used later on to draw the 'Group 1', 'Group 2' etc on to the plot
		int grpNumber=1;
		//Keep track of x coordinates of vertical line.
		int verticalLineCoord=-1;

		// Draw x axis label
		g.setStroke(new BasicStroke(2));
		String xlabel = "Groups";
		String ylabel = "Log2(expression)";
		Font font = new Font("Dialog", Font.PLAIN, 12);

		g.setFont(font);
		g.drawString(xlabel, getWidth() / 2, getHeight());
		
//		System.out.println("left:"+left);
//		System.out.println("left-getMaximumStringWidth:"+(left-getMaximumStringWidth()));
//		System.out.println("stepsY:"+stepsY);
//		System.out.println("MaxYValue:"+getMaxYValue());
		
		g.rotate(-Math.PI / 2.0, left-getMaximumStringWidth(),zeroValue);
    	g.drawString(ylabel, left-getMaximumStringWidth()-18, zeroValue);
    	g.rotate(Math.PI / 2.0, left-getMaximumStringWidth(), zeroValue);
    	
    	// draw rectangle
		g.setColor(Color.black);
		g.drawRect(left, top, width, height);
		//System.out.println("ZeroValue:"+zeroValue);
		
		
		// draw X items
		g.setStroke(new BasicStroke(2));
		g.setColor(Color.black);
		for (int i = 1; i <= getMaxXValue(); i++) {
			g.drawLine(left + (int) Math.round(i * stepX), top + height - 5,
					left + (int) Math.round(i * stepX), top + height);
			g.drawLine(left + (int) Math.round(i * stepX), top, left
					+ (int) Math.round(i * stepX), top + 5);
			//Check if the index is where vertical line needs to be drawn, if so draw
			
			//Draw the name of the factor level as well.
			
			
		}
	
		// draw Y axis markings and labels
    	int yaxis=(int)(top+height-factor);
    	
    	int strWidth=getMaximumStringWidth();
		for (float i = getStepsY(); i < getMaxYValue()+getStepsY(); i = i + getStepsY()) {
					
			g.drawLine(left, yaxis,	left + 5, yaxis);
			g.drawString(coordinateFormat.format(i), left-10-strWidth, yaxis);
			yaxis=yaxis-(int)factor;
			
			if(yaxis<top && i==getMaxYValue()) {
				yaxis=top;
			}

		}
		
		//Plot the points
		//Loop through the samples (reordered as per their factor levels) and plot
		//syn expression profile and the corresponding correlated gene expression profile
		for (int index = 0; index < reorderedSamples.size(); index++) {

			// Retrieve the log of synexpression value for a sample
			float synExpressionValue = logSynGeneExpression.get(0,
					reorderedSamples.get(index));
			// Retrieve the log of correlated gene expression for a sample
			float corExpressionValue = logCorrGeneExpression.get(0,
					reorderedSamples.get(index));

			// syn x coordinate
			int synXCoord = left + (int) Math.round(index * stepX) + 3;
			// syn y coordinate
			int synYCoord = (int) (top + height - factor);
			// cor x coordinate
			int corXCoord = left + (int) Math.round(index * stepX) + 3;
			// cor Y coord
			int corYCoord = (int) (top + height - factor);
			
			
			int count = 0;
			g.setStroke(new BasicStroke(4));

			for (float ind = getStepsY(); ind <= getMaxYValue() + getStepsY(); ind = ind
					+ getStepsY()) {
				String t = coordinateFormat.format(ind);
				float tempVar = Float.parseFloat(t);
				float step = Float.parseFloat(coordinateFormat
						.format(getStepsY()));

				if (synExpressionValue < getStepsY()) {
					synYCoord = top + height;
					g.setColor(new Color(255, 69, 0));
					g.drawLine(synXCoord, synYCoord, synXCoord, synYCoord);

				} else if (synExpressionValue < tempVar
						&& synExpressionValue > (tempVar - step)) {

					float unknown = ((10 * Math.abs(tempVar)) - (10 * Math
							.abs(synExpressionValue)))
							/ step;
					synYCoord = (int) (synYCoord - factor * count);
					synYCoord = Math.round(synYCoord + (unknown * 10));
					g.setColor(new Color(255, 69, 0));
					g.drawLine(synXCoord, synYCoord, synXCoord, synYCoord);

				} else if (synExpressionValue == tempVar) {
					if (synExpressionValue == getStepsY()) {
						synYCoord = (int) (top + height - factor);
						g.setColor(new Color(255, 69, 0));
						g.drawLine(synXCoord, synYCoord, synXCoord, synYCoord);

					} else {

						synYCoord = (int) (synYCoord - factor * count);
						g.setColor(new Color(255, 69, 0));
						g.drawLine(synXCoord, synYCoord, synXCoord, synYCoord);

					}

				} else if (corExpressionValue < getStepsY()) {
					corYCoord = top + height;
					g.setColor(new Color(105, 105, 105));
					g.drawLine(corXCoord, corYCoord, corXCoord, corYCoord);

				} else if (corExpressionValue < tempVar
						&& corExpressionValue > (tempVar - step)) {

					float unknown = ((10 * Math.abs(tempVar)) - (10 * Math
							.abs(corExpressionValue)))
							/ step;
					corYCoord = (int) (corYCoord - factor * count);
					corYCoord = Math.round(corYCoord + (unknown * 10));
					g.setColor(new Color(105, 105, 105));
					g.drawLine(corXCoord, corYCoord, corXCoord, corYCoord);

				} else if (corExpressionValue == tempVar) {
					if (corExpressionValue == getStepsY()) {
						corYCoord = (int) (top + height - factor);
						g.setColor(new Color(105, 105, 105));
						g.drawLine(corXCoord, corYCoord, corXCoord, corYCoord);

					} else {

						corYCoord = (int) (corYCoord - factor * count);
						g.setColor(new Color(105, 105, 105));
						g.drawLine(corXCoord, corYCoord, corXCoord, corYCoord);
					}

				}

				if (count < 4) {
					count = count + 1;

				}

			}
			//Draw vertical demarcating line. If there are 10 samples, 7 of the samples are MALE and 3 are FEMALE.
			//There need be just one vertical line at sample number 7
			if(numSamplesPerGroup.contains(index)) {
				g.setColor(Color.BLACK);
				g.drawLine(synXCoord,top,synXCoord,(top+height));
				
				//Draw the string 'Group1', 'Group2' etc on to the graph. If there are say 7 samples in group1, the string would be drawn between
				//sample 3 & sample 4. The instance field verticalLineCoord keeps a track of the previous x coordinate where the line was drawn.
				if(verticalLineCoord==-1) {
					g.drawString("Group "+grpNumber, (synXCoord) / 2, getHeight());
					verticalLineCoord=synXCoord;
				}else {
					g.drawString("Group "+grpNumber, (synXCoord+verticalLineCoord) / 2, getHeight()-5);
					verticalLineCoord=synXCoord;
				}
				//Attract allows only one factor, there fore need to check just the zeroth factor level
				if(grpNumber <= factorLevels[0]) {
					grpNumber=grpNumber+1;
				}
			}
			
			if(index==numberOfSamples-1) {
				g.setColor(Color.BLACK);
				g.drawString("Group "+grpNumber, (synXCoord+verticalLineCoord) / 2, getHeight()-5);
				
			}

			if (index == 0) {
				oldSynYCoord = synYCoord;
				oldSynXCoord = synXCoord;
				oldCorXCoord = corXCoord;
				oldCorYCoord = corYCoord;
			} else {
				g.setColor(new Color(255, 69, 0));
				// g.setStroke(new BasicStroke(4));
				g.drawLine(oldSynXCoord, oldSynYCoord, synXCoord, synYCoord);
				oldSynXCoord = synXCoord;
				oldSynYCoord = synYCoord;

				g.setColor(new Color(105, 105, 105));
				g.drawLine(oldCorXCoord, oldCorYCoord, corXCoord, corYCoord);
				oldCorXCoord = corXCoord;
				oldCorYCoord = corYCoord;

			}

		}

    }
	
    public JComponent getContentComponent() {
		return this;
	}
    
    
    /**
     * Returns max width of gene indices.
     */
    protected int getNamesWidth(FontMetrics metrics) {
        int maxWidth = 0;
        //for (int i=1; i<=getMaxXValue(); i++) {
            maxWidth = Math.max(maxWidth, metrics.stringWidth("Group 1"));
        //}
        return maxWidth;
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
		return null;
	}

	
	public int getExperimentID() {
		// TODO Auto-generated method stub
		return 0;
	}

	
	public Expression getExpression() {
		return new Expression(this, this.getClass(), "new", 
    			new Object[]{synExpression, corrGeneExpression, groupAssignments,factorLevels});
			
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
	
	
	
	
	
	
	
	

}
