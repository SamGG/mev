/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: Landscape.java,v $
 * $Revision: 1.1.1.2 $
 * $Date: 2004-02-06 21:48:18 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.terrain;

import java.awt.*; 
import java.awt.image.*;

import javax.vecmath.*;
import javax.media.j3d.*;

import com.sun.j3d.utils.image.TextureLoader;
import com.sun.j3d.utils.geometry.GeometryInfo;
import com.sun.j3d.utils.geometry.NormalGenerator;
import com.sun.j3d.utils.picking.PickTool;
import java.awt.geom.AffineTransform;
import java.util.Arrays;

public class Landscape extends Shape3D implements GeometryUpdater {

    private float[][] heights;
    private float[] coords;  // reference to this shape geometry coordinaties
    private float[] colors;  // reference to this shape vertices colors
    private float[] normals; // reference to this shape vertices normals

    public Landscape(float[][] heights) {
        this.heights = heights;
        setCapability(ALLOW_GEOMETRY_READ);
        setCapability(ALLOW_GEOMETRY_WRITE);
        setCapability(ALLOW_APPEARANCE_READ);
        setCapability(ENABLE_PICK_REPORTING);
        initBuffers(heights.length);
        // create the shape geometry coords
        updateCoords(this.coords, this.heights);
        updateColors(this.colors, this.heights);
        updateNormals(this.normals, this.heights);
        // set geometry and appearance
        setGeometry(createGeometry(heights.length, this.coords, this.colors, this.normals));
        setAppearance(createAppearance());
    }

    private void initBuffers(int size) {
        this.coords  = new float[(size-1)*size*2*3]; 
        this.colors  = new float[(size-1)*size*2*3];
        this.normals = new float[(size-1)*size*2*3];
    }

    public void setHeights(float[][] heights) {
        if (this.heights.length != heights.length) {
            initBuffers(heights.length);
            setGeometry(createGeometry(heights.length, this.coords, this.colors, this.normals));
        }
        this.heights = heights;
        ((GeometryArray)getGeometry()).updateData(this);
    }

    public void setPoligonMode(int mode) {
        Appearance appearance = getAppearance();
        PolygonAttributes pa = appearance.getPolygonAttributes();
        pa.setPolygonMode(mode);
    }

    private Geometry createGeometry(int size, float[] coords, float[] colors, float[] normals) {
        int[] stripVertexCounts = new int[size-1];
        Arrays.fill(stripVertexCounts, size*2);

        GeometryArray geometry = new TriangleStripArray(coords.length/3,
                                                        GeometryArray.COORDINATES |
                                                        GeometryArray.COLOR_3     |
                                                        GeometryArray.NORMALS     |
                                                        GeometryArray.BY_REFERENCE,
                                                        stripVertexCounts
                                                       );
        geometry.setCapability(GeometryArray.ALLOW_COUNT_READ);
        geometry.setCapability(GeometryArray.ALLOW_FORMAT_READ);
        geometry.setCapability(GeometryArray.ALLOW_REF_DATA_READ);
        geometry.setCapability(GeometryArray.ALLOW_REF_DATA_WRITE);
        geometry.setCoordRefFloat(coords);
        geometry.setColorRefFloat(colors);
        geometry.setNormalRefFloat(normals);
        return geometry;
    }

    private Appearance createAppearance() {
        PolygonAttributes pa = new PolygonAttributes();
        pa.setCapability(PolygonAttributes.ALLOW_MODE_WRITE);
        pa.setCullFace(PolygonAttributes.CULL_NONE);

        Appearance appearance = new Appearance();
        appearance.setCapability(Appearance.ALLOW_POLYGON_ATTRIBUTES_READ);
        appearance.setCapability(Appearance.ALLOW_POLYGON_ATTRIBUTES_WRITE);
        appearance.setPolygonAttributes(pa);
        appearance.setMaterial(new Material());
        return appearance;
    }

    public void updateData(Geometry geometry) {
        updateCoords(this.coords, this.heights);
        updateColors(this.colors, this.heights);
        updateNormals(this.normals, this.heights);
    }

    /**
     * Creates a triangulated surface from the two dimensional array of heights.
     */
    public static void updateCoords(float[] coords, float[][] heights) {
        final int size = heights.length;
        final int stride = 2*3;
        float step = 1f/size;
        float x = step/2;
        float y = step/2;
        for (int i=0; i<size-1; i++) {
            for (int j=0; j<size; j++) {
                int pos = (i*size+j)*stride;
                //0
                coords[pos++] = x;
                coords[pos++] = heights[i][j];
                coords[pos++] = y;
                //1
                coords[pos++] = x;
                coords[pos++] = heights[i+1][j];
                coords[pos++] = y+step;

                x += step;
            }
            x = step/2;
            y += step;
        }
    }

    public static void updateColors(float[] colors, float[][] heights) {
        final int size = heights.length;
        final int stride = 2*3;
        float step = 1f/size;
        float x = step/2;
        float y = step/2;
        float scale = (float)(size-1)/getMaxValue(heights);           
        // create gradient image                                      
        BufferedImage gradient = DomainUtil.createGradientImage(size);
        int rgb1, rgb2;
        for (int i=0; i<size-1; i++) {
            for (int j=0; j<size; j++) {
                int pos = (i*size+j)*stride;

                rgb1 = gradient.getRGB((int)(heights[i  ][j]*scale), 0);
                rgb2 = gradient.getRGB((int)(heights[i+1][j]*scale), 0);

                colors[pos++] = (float)((rgb1 >> 16) & 0xFF)/255f;
                colors[pos++] = (float)((rgb1 >>  8) & 0xFF)/255f;
                colors[pos++] = (float)((rgb1 >>  0) & 0xFF)/255f;

                colors[pos++] = (float)((rgb2 >> 16) & 0xFF)/255f;
                colors[pos++] = (float)((rgb2 >>  8) & 0xFF)/255f;
                colors[pos++] = (float)((rgb2 >>  0) & 0xFF)/255f;

                x += step;
            }
            x = step/2;
            y += step;
        }
    }

    protected static void updateNormals(float[] normals, float[][] heights) {
        final int size = heights.length;
        float[][][] buff = new float[size][size][3]; // vertices normals;
        float step = 1f/size;
        Vector3f v0 = new Vector3f();
        Vector3f south = new Vector3f();
        Vector3f east  = new Vector3f();
        Vector3f normal = new Vector3f();
        for (int i=0; i<size; i++) {
            for (int j=0; j<size; j++) {
                if (i == size-1) { // last row
                    south.set(0f, 0f, step);
                } else {
                    south.set(0f, heights[i+1][j], step);
                }
                if (j == size-1) { // last column
                    east.set(step, 0f, 0f);
                } else {
                    east.set(step, heights[i][j+1], 0f);
                }
                v0.set(0, heights[i][j], 0);
                south.sub(v0);
                east.sub(v0);
                normal.cross(south, east);
                normal.normalize();
                buff[i][j][0] = normal.x;
                buff[i][j][1] = normal.y;
                buff[i][j][2] = normal.z;
            }
        }
        // copying data
        for (int i=0; i<size-1; i++) {
            for (int j=0; j<size; j++) {
                int pos = (i*size+j)*6;
                normals[pos++] = buff[i][j][0];
                normals[pos++] = buff[i][j][1];
                normals[pos++] = buff[i][j][2];
                normals[pos++] = buff[i+1][j][0];
                normals[pos++] = buff[i+1][j][1];
                normals[pos++] = buff[i+1][j][2];
            }
        }
    }

    private static float getMaxValue(float[][] heights) {
        final int size = heights.length;
        float max = -Float.MAX_VALUE;
        for (int i=0; i<size; i++)
            for (int j=0; j<size; j++)
                max = Math.max(max, heights[i][j]);
        return max;
    }
}


