/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: DomainUtil.java,v $
 * $Revision: 1.1.1.1 $
 * $Date: 2003-08-21 21:04:24 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.terrain;

import java.awt.*;
import java.awt.image.*;
import javax.vecmath.Point2f;

public class DomainUtil {

    private static final float MAX_HEIGHT = 0.2f;
    /**
     * @param locations - the array of (x,y) normalized coords from 0f to 1f values.
     * @param energy - the array of genes energy. 
     * @param size - a desired size of the output array.
     * @return a square array of heights.
     */
    public static float[][] getHeights(float[][] locations, int size, float sigma) {
        return getHeights(locations, size, sigma, new Point2f(), new Point2f(1, 1));
    }

    /**
     * @param locations - the array of (x,y) normalized coords from 0f to 1f values.
     * @param energy - the array of genes energy. 
     * @param size - a desired size of the output array.
     * @param p1 - left upper corner of the selection area.
     * @param p2 - right bottom corner of the selection area.
     * @return a square array of heights.
     */
    public static float[][] getHeights(float[][] locations, int size, float sigma, Point2f p1, Point2f p2) {
        float side_size = Math.max(Math.abs(p2.x-p1.x), Math.abs(p2.y-p1.y));
        float step = side_size/size;
        float x1 = p1.x+step/2;
        float y1 = p1.y+step/2;
        float x2;
        float y2;
        float sum;
        float[][] heights = new float[size][size];
        for (int i=0; i<size; i++) {
            for (int j=0; j<size; j++) {
                sum = 0f;
                for (int gene=0; gene<locations.length; gene++) {
                    x2 = locations[gene][0];
                    y2 = locations[gene][1];
                    sum += disperse(sigma, x1, y1, x2, y2);
                }
                heights[i][j] = sum;
                x1 += step;
            }
            x1 = p1.x+step/2;
            y1 += step;
        }
        normalize(heights);
        return heights;
    }

    private static void normalize(float[][] heights) {
        int size = heights.length;
        float min = Float.MAX_VALUE;
        float max = -Float.MAX_VALUE;
        for (int i=0; i<size; i++) {
            for (int j=0; j<size; j++) {
                min = Math.min(min, heights[i][j]);
                max = Math.max(max, heights[i][j]);
            }
        }
        float scale = max == min ? 1f : MAX_HEIGHT/(max-min);
        for (int i=0; i<size; i++) {
            for (int j=0; j<size; j++) {
                heights[i][j] = (heights[i][j]-min)*scale;
            }
        }
    }

    /**
     * @return a disperse value.
     */
    private static double disperse(float sigma, float x1, float y1, float x2, float y2) {
        float r2 = (x1-x2)*(x1-x2)+(y1-y2)*(y1-y2);
        float sigma2 = sigma*sigma;
        if (r2 > 4*sigma2) return 0;
        return Math.exp(-r2/(2*sigma2));
    }

    public static BufferedImage createGradientImage(int size) {
        BufferedImage gradient = new BufferedImage(size, 1, BufferedImage.TYPE_3BYTE_BGR);
        //BufferedImage image = (BufferedImage)this.createImage(size, 1);
        Graphics2D graphics = gradient.createGraphics();
        GradientPaint gp = new GradientPaint(0, 0, Color.green, size-1, 0, Color.red);
        graphics.setPaint(gp);
        graphics.drawRect(0, 0, size-1, 1);
        graphics.dispose();
        return gradient;
    }
}
