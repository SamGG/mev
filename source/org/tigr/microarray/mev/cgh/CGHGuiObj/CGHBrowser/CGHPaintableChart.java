/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * CGHPaintableChart.java
 *
 * Created on December 29, 2002, 1:06 AM
 */

package org.tigr.microarray.mev.cgh.CGHGuiObj.CGHBrowser;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;

import com.klg.jclass.chart.JCAxis;
import com.klg.jclass.chart.beans.SimpleChart;

/**
 *
 * @author  Adam Margolin
 * @author Raktim Sinha
 */

public class CGHPaintableChart extends SimpleChart{
    Rectangle currentRect = null;
    Rectangle rectToDraw = null;
    Rectangle previousRectDrawn = new Rectangle();

    int startIndex;
    int stopIndex;

    /** Creates a new instance of CGHPaintableChart */
    public CGHPaintableChart() {
        super();
    }

    public void paint(Graphics g){
        super.paint(g);
        paintRect(g);
    }

    public void paintRect(Graphics g){
        updateRect();
        //If currentRect exists, paint a box on top.
        if (currentRect != null) {
            //Draw a rectangle on top of the image.

            g.setColor(Color.white);
            g.setXORMode(Color.yellow); //Color of line varies

            g.fillRect(currentRect.x, currentRect.y,
            currentRect.width, currentRect.height - 1);
        }
    }


    public void setSelectedCoordinates(int pointIndex){
        startIndex = pointIndex;
        stopIndex = -1;
        repaint();
        //updateRect();
    }

    public void updateRect(){
        if(startIndex == -1){
            return;
        }

        if(stopIndex == -1){
            highlightPoint();
        }else{
            highlightRegion();
        }
    }

    public void highlightPoint(){
        try{
            JCAxis yaxis = this.getDataView(0).getYAxis();

            // Get the region bounded by the Plot area and the selected area
            Point point = this.unpick(startIndex, getDataView(0).getSeries(0));
            int x = point.x - 5;
            int width = 10;

            Point topY = this.getDataView(0).unmap(point.getX(), yaxis.getMax());
            Point bottomY = this.getDataView(0).unmap(point.getY(), yaxis.getMin());
            int y = (int) topY.getY();
            int height = (int) (bottomY.getY() - topY.getY());

            currentRect = new Rectangle(x, y, width, height);
            //repaint();

        }catch (NullPointerException e){
            e.printStackTrace();
            System.out.println("Paintable char set selected coords npe");
        }
    }


    public void setSelectedCoordinates(int startIndex, int stopIndex){
        this.startIndex = startIndex;
        this.stopIndex = stopIndex;
        repaint();
        //updateRect();
    }

    public void highlightRegion(){
        try{
            JCAxis yaxis = this.getDataView(0).getYAxis();

            // Get the region bounded by the Plot area and the selected area
            Point pointStart = this.unpick(startIndex, getDataView(0).getSeries(0));
            Point pointStop = this.unpick(stopIndex, getDataView(0).getSeries(0));
            int x = pointStart.x;
            int xStop = pointStop.x;
            int width = xStop - x;

            Point topY = this.getDataView(0).unmap(pointStart.getX(), yaxis.getMax());
            Point bottomY = this.getDataView(0).unmap(pointStart.getY(), yaxis.getMin());
            int y = (int) topY.getY();
            int height = (int) (bottomY.getY() - topY.getY());
            currentRect = new Rectangle(x, y, width, height);
            //repaint();
        }catch (NullPointerException e){
            e.printStackTrace();
            //System.out.println("Paintable char set selected coords npe");
        }
    }

    public void deleteRect(){
        currentRect = null;
        startIndex = -1;
        stopIndex = -1;
    }
}
