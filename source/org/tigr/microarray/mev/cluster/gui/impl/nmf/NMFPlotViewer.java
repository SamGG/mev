/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: NMFPlotViewer.java,v $
 * $Revision: 1.7 $
 * $Date: 2006-03-24 15:51:05 $
 * $Author: dschlauch $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.nmf;

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

import org.tigr.microarray.mev.cluster.gui.impl.GUIFactory;
import org.tigr.microarray.mev.cluster.gui.impl.ViewerAdapter;

public class NMFPlotViewer extends ViewerAdapter implements java.io.Serializable {
    
    private JComponent content;
    float[][] data;
    String[] labels;
    int[] samplesOrder;
    static boolean orderedSamples = false;
    protected static final String ORDERED_SAMPLES = "order-samples";
    /**
     */
    public NMFPlotViewer(float[] data, String[] labels) {
    	float[][] data2 = new float[1][];
    	data2[0] = data;
    	float minValue = Float.POSITIVE_INFINITY;
    	float maxValue = 1.0f;
    	for (int i=0; i<data2.length; i++){
        	for (int j=0; j<data2[i].length; j++){
	    		if (data2[i][j]<minValue)
	    			minValue = data2[i][j];
	    		if (data2[i][j]>maxValue)
	    			maxValue = data2[i][j];
        	}
    	}
    	this.data = data2;
    	this.labels = labels;
    	this.samplesOrder = new int[labels.length];
    	content = createContent(data2, labels, minValue-.05f, maxValue);
    }
    /**
     */
    public NMFPlotViewer(float[][] data, String[] labels) {
    	float minValue = Float.POSITIVE_INFINITY;
    	float maxValue = 1.0f;
    	for (int i=0; i<data.length; i++){
        	for (int j=0; j<data[i].length; j++){
	    		if (data[i][j]<minValue)
	    			minValue = data[i][j];
	    		if (data[i][j]>maxValue)
	    			maxValue = data[i][j];
        	}
    	}
    	this.data = data;
    	this.labels = labels;
    	this.samplesOrder = new int[labels.length];
    	content = createContent(data, labels, minValue-.05f, maxValue);
    }
    /**
     * @inheritDoc
     */
    public Expression getExpression(){
    	return new Expression(this, this.getClass(), "new", 
    			new Object[]{data});
    }
    
    /**
     * Returns the viewer content.
     */
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
    public JComponent getRowHeaderComponent() {
        return null;
    }
    
    /**
     * The class to draw the viewer content.
     */
    private class Plot extends JPanel {

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
	    	orderSamples();
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
		    double stepX = plotWidth/(data[0].length-1.0);
		    for (int i=1; i<data.length; i++) {
		    	g.drawLine(left+(int)Math.round(i*stepX), top+plotHeight-5, left+(int)Math.round(i*stepX), top+plotHeight);
		    }

	    	for (int j=0; j<data.length; j++){
			    g.setColor(colors[j]);
			    double factor = (double)plotHeight;///(double)(scale);
			    int prevValue = -(int)Math.round((data[j][orderedSamples? samplesOrder[0]:0]-minValue)*factor/scale);
			    int curValue;
			    int zeroValue = top+plotHeight;
			    // draw chart
			    for (int i=1; i<data[j].length; i++) {
					curValue = -(int)Math.round((data[j][orderedSamples? samplesOrder[i]:i]-minValue)*factor/scale);
					g.drawLine(left+(int)Math.round((i-1)*stepX), zeroValue+prevValue, left+(int)Math.round(i*stepX), zeroValue+curValue);
					prevValue = curValue;
			    }
			    // draw points
			    g.setColor(new Color(0,0,128));
			    for (int i=0; i<data[j].length; i++) {
					curValue=-(int)Math.round((data[j][orderedSamples? samplesOrder[i]:i]-minValue)*factor/scale);
					g.fillOval(left+(int)Math.round(i*stepX)-3, zeroValue+curValue-3, 6, 6);
			    }
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
		    for (int i=0; i<labels.length; i++) {
				str = String.valueOf(labels[orderedSamples? samplesOrder[i]:i]);
				width  = metrics.stringWidth(str);
				g.drawString(str,-top-plotHeight-10-width, left+5+(int)Math.round(i*stepX));
		    }
		    ((Graphics2D)g).rotate(Math.PI/2.0);
		}
		private void setOrdered(){
			orderedSamples = !orderedSamples;
	        Listener listener = new Listener();
	        this.popup = createJPopupMenu(listener);
		}
        private void orderSamples() {
    		int[] winner = new int[data[0].length];
    		float[] ratios = new float[data[0].length];
    		
    		for (int i=0; i<data[0].length; i++){
    			float max = 0;
    			for (int j=0; j<data.length; j++){
    				if (data[j][i]>max){
    					max = data[j][i];
    					winner[i]=j;
    				}
    			}
    		}

    		for (int i=0; i<data[0].length; i++){
    			float minRatio = Float.POSITIVE_INFINITY;
    			for (int j=0; j<data.length; j++){
    				if (i!=j && (data[winner[i]][i]/data[j][i]<minRatio)){
    					minRatio = data[winner[i]][i]/data[j][i];
    					ratios[i] = minRatio;
    				}
    			}
    		}
    		int index = 0;
    		for (int cluster=0; cluster<data.length; cluster++){
        		for (int i=0; i<samplesOrder.length; i++){
        			if (winner[i]==cluster){
        				samplesOrder[index] = i;
        				index++;
        			}
        		}
    		}
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

	        menuItem = new JMenuItem("Arrange/reset Samples", GUIFactory.getIcon("new16.gif"));
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
			public void mouseClicked(MouseEvent event) {
	            if (SwingUtilities.isRightMouseButton(event)) {
	                return;
	            }
	        }

	        /** Called when the mouse has been released. */
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
		float[][] fa = {{.99f, .88f, .72f, .1f}};
		String[]sa = {"2 Clusters", "3 Clusters", "4 Clusters", "5 Clusters"};
    	NMFPlotViewer pv = new NMFPlotViewer(fa, sa);
    	JDialog jd = new JDialog();
    	jd.add(pv.getContentComponent());
    	jd.setSize(800, 800);
    	jd.setModal(true);
    	jd.setVisible(true);
    	System.exit(0);
    	 
    }
}
