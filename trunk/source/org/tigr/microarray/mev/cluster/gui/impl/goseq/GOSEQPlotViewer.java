/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: GOSEQPlotViewer.java,v $
 * $Revision: 1.7 $
 * $Date: 2010-09-24 15:51:05 $
 * $Author: dschlauch $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.goseq;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.beans.Expression;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.impl.GUIFactory;
import org.tigr.microarray.mev.cluster.gui.impl.ViewerAdapter;
import org.tigr.util.FloatMatrix;

public class GOSEQPlotViewer extends ViewerAdapter implements java.io.Serializable {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JComponent content;
	IFramework framework;
    FloatMatrix data;
    String[] labels;
    String biasString = "Transcript Length";
    int[] samplesOrder;
	private float minValue;
	private double maxValue;
	int ticks = 21;
    static boolean orderedSamples = false;
    protected static final String ORDERED_SAMPLES = "order-samples";

    /**
     */
    public GOSEQPlotViewer(float[][] data, String biasString) {
    	this(new FloatMatrix(data), biasString);
    }
    
    /**
     */
    public GOSEQPlotViewer(FloatMatrix data, String biasString) {
    	this.data = data;
    	this.biasString = biasString;
    	initLabels();
    	content = createContent(data.A, labels, minValue, maxValue);
    }
    /**
     * @inheritDoc
     */
    @Override
	public Expression getExpression(){
    	return new Expression(this, this.getClass(), "new", 
    			new Object[]{data, biasString});
    }
    
    @Override
	public void onSelected(IFramework framework){
    	this.framework = framework;
    	content.updateUI();
    	content.repaint();
    }
    private void initLabels(){
    	float minValue = 0.0f;
    	float maxValue = Float.NEGATIVE_INFINITY;
    	for (int i=1; i<data.A.length; i++){
        	for (int j=0; j<data.A[i].length; j++){
	    		if (data.A[i][j]<minValue)
	    			minValue = data.A[i][j];
	    		if (data.A[i][j]>maxValue)
	    			maxValue = data.A[i][j];
        	}
    	}
    	this.minValue = minValue;
    	this.maxValue = maxValue*1.1f;

    	float min = 0;
    	String[] ccLabels = new String[ticks];
    	float max = data.A[0][data.A[0].length-1];//the highest value for the x axis
    	
    	for (int i=0; i<ticks; i++){
    		ccLabels[i] = Integer.toString((int)((i*(max-min))/(ticks-1)));
    	}
    	labels = ccLabels;
    	this.samplesOrder = new int[labels.length];
    	
    }
    /**
     * Returns the viewer content.
     */
    @Override
	public JComponent getContentComponent() {
    	return content;
    }
    
    /**
     * Creates the viewer content.
     */
    private JComponent createContent(float[][] data, String[] labels, double minValue, double maxValue) {
    	return new Plot(data, labels, minValue, maxValue);
    }
    
    /** Returns a component to be inserted into the scroll pane row header
     */
    @Override
	public JComponent getRowHeaderComponent() {
        return null;
    }
    
    /**
     * The class to draw the viewer content.
     */
    private class Plot extends JPanel {

    	/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		protected JPopupMenu popup;
	    double maxValue = 1.0;
	    double minValue = 0.7;
	    Color[] colors;
		private static final int left = 80;
		private static final int top  = 40;
		private float[][] data;
		private String[] labels;
		private Font font = new Font("monospaced", Font.BOLD, 10);
		
		/**
		 * Constructs a <code>Plot</code> with specified S-matrix.
		 */
		public Plot(float[][] data, String[] labels, double minValue, double maxValue) {
		    setBackground(Color.white);
		    setFont(font);
		    this.data = data;
		    this.labels = labels;
		    this.minValue = minValue;
		    this.maxValue = maxValue;
		    this.colors = new Color[data.length];
		    fillColors();
	        init();
		}
		
		private void fillColors() {
			Color[] colorOrder = {Color.red, Color.green, Color.blue, Color.yellow, Color.ORANGE, Color.black};
			for (int i=0; i<colors.length; i++){
				if (i<colorOrder.length){
					colors[i] = colorOrder[i];
				} else {
		            int red = (int)(Math.random()*256);
		            int green = (int)(Math.random()*256);
		            int blue = (int)(Math.random()*256);
		            colors[i] = new Color(red, green, blue);
				}
			}
		}

		/**
		 * Paints the viewer content into specified graphics.
		 */
		@Override
		public void paint(Graphics g) {
		    super.paint(g);
		    // drawing rectangle
		    int plotWidth  = getWidth()  - 160;
		    int plotHeight = getHeight() - 200;
		    if (plotWidth < 5 || plotHeight < 5) 
		    	return;
		    g.setColor(Color.black);
		    g.drawRect(left, top, plotWidth, plotHeight);
		    
		    int steps = 10;
		    double stepValue = (maxValue-minValue)/steps;
		    if (Double.isNaN(maxValue))
		    	return;
		    float scale = (float)(maxValue-minValue);
		    // drawing left marks
		    double stepY = plotHeight/(double)(steps);
		    for (int i=1; i<steps; i++) {
		    	g.drawLine(left, top + (int)Math.round(i*stepY), left+5, top+(int)Math.round(i*stepY));
		    }
		    // drawing right marks
		    double stepX = plotWidth/(ticks-1.0);
		    for (int i=1; i<ticks; i++) {
		    	g.drawLine(left+(int)Math.round(i*stepX), top+plotHeight-5, left+(int)Math.round(i*stepX), top+plotHeight);
		    }

		    // draw labels
		    int width;
		    String str;
		    FontMetrics metrics = g.getFontMetrics();
		    for (int i=0; i <= steps; i++) {
				str = String.valueOf(Math.round(100.00*(maxValue-stepValue*i))/100.00);
				width  = metrics.stringWidth(str);
				g.drawString(str, left-10-width, top+(int)Math.round(i*stepY)+5);
		    }
		    ((Graphics2D)g).rotate(-Math.PI/2.0);
		    g.setColor(Color.blue);
		    int labelFreq = labels.length/100+1;
		    stepX = plotWidth/(ticks-1.0);
		    for (int i=0; i<labels.length/labelFreq; i++) {
				str = String.valueOf(labels[orderedSamples? samplesOrder[i*labelFreq]:i*labelFreq]);
				width  = metrics.stringWidth(str);
				g.drawString(str,-top-plotHeight-10-width, left+5+(int)Math.round(i*labelFreq*stepX));
		    }
		    ((Graphics2D)g).rotate(Math.PI/2.0);
		    
		    //Draw titles
		    g.setFont(new Font("monospaced", Font.BOLD, 20));
		    g.drawString(biasString, plotWidth/2, top + plotHeight + 100);
		    ((Graphics2D)g).rotate(-Math.PI/2.0);
		    g.drawString("Probability of Differential Expression", -top -(plotHeight/2)-200, 20);
		    ((Graphics2D)g).rotate(Math.PI/2.0);

	    	for (int j=1; j<data.length; j++){
			    g.setColor(colors[j]);
			    double factor = plotHeight;///(double)(scale);
			    int prevValue = -(int)Math.round((data[j][orderedSamples? samplesOrder[0]:0]-minValue)*factor/scale);
			    int curValue;
			    float maxXAxis = data[0][data[0].length-1]/plotWidth;
			    int zeroValue = top+plotHeight;
			    // draw points
			    g.setColor(Color.blue);
			    if (j==1)
				    for (int i=0; i<data[j].length; i++) {
						curValue=-(int)Math.round((data[j][orderedSamples? samplesOrder[i]:i]-minValue)*factor/scale);
						g.fillOval(left+(int)(data[0][i]/maxXAxis)-3, zeroValue+curValue-3, 6, 6);
				    }
			    // draw chart
			    g.setColor(Color.red);
			    if (j==2)
				    for (int i=1; i<data[j].length; i++) {
						curValue = -(int)Math.round((data[j][orderedSamples? samplesOrder[i]:i]-minValue)*factor/scale);
						g.drawLine(left+(int)(data[0][i-1]/maxXAxis), zeroValue+prevValue, left+(int)(data[0][i]/maxXAxis), zeroValue+curValue);
						prevValue = curValue;
				    }
	    	}
		    
		}
		private void setOrdered(){
			orderedSamples = !orderedSamples;
	        Listener listener = new Listener();
	        this.popup = createJPopupMenu(listener);
		}
		

		private void init(){
	        Listener listener = new Listener();
	        this.popup = createJPopupMenu(listener);
	        addMouseListener(listener);
	        addMouseMotionListener(listener);
	        repaint();
		}

		/**
		 * Creates a popup menu.
		 */
		protected JPopupMenu createJPopupMenu(ActionListener listener) {
		    JPopupMenu popup = new JPopupMenu();
		    addMenuItems(popup, listener);
		    return popup;
		}

	    /**
	     * Adds viewer specific menu items.
	     */
	    protected void addMenuItems(JPopupMenu menu, ActionListener listener) {
	        JMenuItem menuItem;

	        menuItem = new JMenuItem("Arrange/reset Samples order", GUIFactory.getIcon("new16.gif"));
	        menuItem.setActionCommand(ORDERED_SAMPLES);
	        menuItem.addActionListener(listener);
	        menu.add(menuItem);
	    }
	    /**
	     * The class to listen to mouse events.
	     */
	    private class Listener extends MouseAdapter implements MouseMotionListener,ActionListener {
	        

		    public void actionPerformed(ActionEvent e) {
		        String command = e.getActionCommand();

		        if (command.equals(ORDERED_SAMPLES)){
		        	setOrdered();
		        	repaint();
		        }
		    }
			@Override
			public void mouseClicked(MouseEvent event) {
	            if (SwingUtilities.isRightMouseButton(event)) {
	                return;
	            }
	        }

	        /** Called when the mouse has been released. */
	        @Override
			public void mouseReleased(MouseEvent event) {
		        maybeShowPopup(event);
	        	if (event.isPopupTrigger())
	        		return;
	        }
		    private void maybeShowPopup(MouseEvent e) {
		    	if (e.isPopupTrigger()){
		    		popup.show(e.getComponent(), e.getX(), e.getY());
		    		repaint();
		    	}
		    }
		    // Raktim - Added to make Java 1.5 happy
			//@Override
			public void mouseDragged(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			// Raktim - Added to make Java 1.5 happy
			//@Override
			public void mouseMoved(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
	    }
    }
    public static void main(String[] args){
    	float[][] fm = new float[20][20];
    	for (int i=0; i<fm.length; i++){
    		for (int j=0; j<fm[i].length; j++){
    			fm[i][j] = 0;
    		}
    	}
		fm[0][0] = 2000;
		fm[1][1] = 2000;
		fm[0][1] = 4000;
		fm[1][0] = 4000;
		for (int i=0; i<fm.length; i++){
			fm[i][i] = i;
		}
		fm[0][0] = 20;
		float[][] fa = {{.09f, .28f, .32f, .6f, .8f, 1.2f,2.09f, 3.28f, 4.32f, 5.6f, 6.8f, 11.2f},
				{.99f, .88f, .72f, .1f, .3f, -.2f,.09f, .28f, .32f, .6f, .8f, 1.2f},
				{.99f, 1.1f, 1.72f, 2.1f, .4f, 0f,.28f, .76f, .32f, .6f, .8f, 1.2f}};
		GOSEQPlotViewer pv = new GOSEQPlotViewer(fa, "tran");
    	JDialog jd = new JDialog();
    	pv.onSelected(null);
    	jd.add(pv.getContentComponent());
    	jd.setSize(1200, 800);
    	jd.setBackground(Color.yellow);
    	jd.setForeground(Color.blue);
    	jd.setModal(true);
    	jd.setVisible(true);
    	System.exit(0);
    	 
    }
}
