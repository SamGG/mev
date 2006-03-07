/*
 * Created on Aug 26, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.tigr.microarray.mev.cluster.gui.impl.tease;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JLabel;

import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmParameters;


/**
 * @author Annie Liu
 * @version Aug 26, 2005
 *
 * TEASEInfoBox is representd in the viewer as a dot at the node.
 * info box is invoked when roll-over mouse event happen
 */
public class TEASEInfoBox extends JLabel{
	
	public final int SIZE = 8;
	public static final int displayLimit = 5;
	public static final double defaultUpperBoundary = 0.1;
	public static final double defaultLowerBoundary = 0.00001;

	private int stepSize;
	private float yPos;
	public int x;
	public int y;
	public int rootNode;
	private double upperBound;
	private double lowerBound;
	private boolean isCluster;
	private Color color;     //color of the dots, indicates the significance of the result 
	private String[][] matrix;   //a reference to the result matrix
	
	
	/**
	 * create an instance of TEASEInfoBox
	 * @param x
	 * @param y
	 * @param matrix
	 */
	public TEASEInfoBox(float x, float y, AlgorithmData data, int rootNode) {
		this.yPos = y;
		this.x = (int)x;
		this.rootNode = rootNode;
		//check if result valiable
		if (data == null)
			return;

		AlgorithmParameters param = data.getParams();

		this.matrix = (String[][])data.getObjectMatrix("result-matrix");
		this.upperBound = param.getDouble("upper-boundary");
		this.lowerBound = param.getDouble("lower-boundary");

		//System.out.println("matrix is null: " + (matrix == null));
//		System.out.println(matrix.length);
//		System.out.println("matrix[1] is null: " + (matrix[1] == null));
		if (matrix != null) {
			if (Double.valueOf(matrix[1][matrix[1].length - 1]).doubleValue() <= 1.1) 
				this.isCluster = true;
			else 
				this.isCluster = false;
		}
		setColor();
	}

	/**
	 * Format the text displayed in the info box using HTML tags
	 * @return text to be displayed in genesTree panel
	 */
	public String getInfoText() {
		if (matrix == null)
			return "";
		
		String text = "<html><body bgcolor = #ffffcc><table cellspacing = 10>";
		int midpoint;
		if (isCluster)
			midpoint = matrix[0].length - 6;
		else 
			midpoint = matrix[0].length - 3;
		
		for (int i = 0; i < TEASEInfoBox.displayLimit; i++) {
			text += "<tr>";
			for (int j = 1; j < midpoint; j++) {
				text += "<td align = left>" + this.matrix[i][j] + "</td>";
			}
			text += "<td align = left><em>" + this.matrix[i][midpoint] + "</em></td>";
			if (isCluster)
				text += "<td align = right>" + matrix[i][matrix[i].length-1] + "</td>";
			else 
				text += "<td align = right>" + matrix[i][midpoint+1] +  "\t" + matrix[i][midpoint+2] + "</td>";
			text += "</tr>";
		}
		return text;
	}
	
	/**
	 * Set the color of the dot according to the score 
	 * @param score
	 */
	public void setColor() {
		if (matrix == null)
			this.color = Color.BLACK;
		else if (this.isCluster) {
				double upperLog = Math.log(this.upperBound);
				double lowerLog = Math.log(this.lowerBound);
				double score = Math.log(Double.valueOf(this.matrix[1][this.matrix[1].length - 1]).doubleValue());
				double middle = (upperLog+lowerLog)/2;
				int distance;
	
				if (score < lowerLog) 
					this.color = new Color(255, 20, 0);
				else if (score > upperLog)
					this.color = new Color(0, 20, 255);
				else if (score < upperLog && score > middle) {
					distance = (int)(((upperLog - score)/(upperLog-middle)) * 255);
					this.color = new Color(distance, 20, 255);
				}
				else {      //between the two boundaries
					distance = (int)(((middle - score)/(middle-lowerLog)) * 255);
					this.color = new Color(255, 20, 255 - distance);
			}
		} else
			this.color = new Color(234, 72, 173);
	}
	
	/**
	 * paint the dot
	 * @param g
	 * @param size
	 */
	public void render(Graphics g) {
		this.y = (int)(this.yPos*this.stepSize + this.stepSize/2);
		g.setColor(this.color);
		//System.out.println("x = "+this.x + "  y = "+this.y);
		g.fillOval((int)(this.x - SIZE/2), (int)(this.y - SIZE/2), (int)SIZE, (int)SIZE);
	}
	
    /**
     * Updates the tree size with specified element size.
     */
    public void updateSize(Dimension elementSize) {
        if (this.stepSize == elementSize.height) {
        	return;
        }
        this.stepSize = elementSize.height;
    }
    
    /**
     * Reset the x position when the height of the tree is adjusted
     * @param x
     */
    public void resetX(int x) {
    	this.x = x;
    }
    
    /**
     * Set the score boundary to determine the color.
     * @param upper
     * @param lower
     */
    public void setColorBoundary(double upper, double lower) {
    	this.upperBound = upper;
    	this.lowerBound = lower;
    	setColor();
    }
    
    public double getUpperBound() {
    	return this.upperBound;
    }
    
    public double getLowerBound() {
    	return this.lowerBound;
    }
    
    public String toString() {
    	String text = "";
    	text += "x = " + this.x + "  y = " + this.y;
    	return text;
    }
}
