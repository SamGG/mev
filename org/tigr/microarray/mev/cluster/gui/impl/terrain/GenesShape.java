/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: GenesShape.java,v $
 * $Revision: 1.2 $
 * $Date: 2005-02-24 20:24:10 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.terrain;

import java.awt.Color;

import javax.media.j3d.Appearance;
import javax.media.j3d.Geometry;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.GeometryUpdater;
import javax.media.j3d.LineArray;
import javax.media.j3d.Material;
import javax.media.j3d.PointArray;
import javax.media.j3d.PointAttributes;
import javax.media.j3d.QuadArray;
import javax.media.j3d.Shape3D;
import javax.media.j3d.TriangleStripArray;
import javax.vecmath.Color3f;
import javax.vecmath.Point2f;

import org.tigr.microarray.mev.cluster.gui.impl.util.IntArray;

import com.sun.j3d.utils.geometry.Primitive;
import com.sun.j3d.utils.geometry.Sphere;

public class GenesShape extends Shape3D implements GeometryUpdater {

    public static final int POINTS  = 0;
    public static final int CUBES   = 1;
    public static final int SPHERES = 2;

    private int type = POINTS;
    private float size = 0.005f;
    private float[] coords;  // reference to this shape geometry coordinaties
    private float[] colors;  // reference to this shape vertices colors
    private float[] normals; // reference to this shape vertices normals

    private float[][] locations;
    private IntArray shapeIndices;
    private Point2f start = new Point2f();
    private Point2f end = new Point2f();
    // temporary members to handle updating callback
    private int[] colorIndices;
    private Color3f[] colors_c3f;

    public GenesShape(int type, float[][] locations, Point2f start, Point2f end) {
        this.type = type;
        this.locations = locations;
        this.start.set(start);
        this.end.set(end);
        setCapability(ENABLE_PICK_REPORTING);
        setCapability(ALLOW_GEOMETRY_READ);
        setCapability(ALLOW_GEOMETRY_WRITE);
        this.shapeIndices = getShapeIndices();
        initBuffers();
        initData();
        setGeometry(createGeometry());
        setAppearance(createAppearance());
        setBoundsAutoCompute(false);
    }

    public void setType(int type) {
        this.type = type;
        initBuffers();
        initData();
        setGeometry(createGeometry());
    }

    public void setZoom(Point2f start, Point2f end) {
        this.start.set(start);
        this.end.set(end);
        this.shapeIndices = getShapeIndices();
        initBuffers();
        initData();
        setGeometry(createGeometry());
    }

    public void updateColors(int[] colorIndices, Color[] colors) {
        this.colorIndices = colorIndices;
        this.colors_c3f = new Color3f[colors.length];
        for (int i=0; i<this.colors_c3f.length; i++)
            this.colors_c3f[i] = new Color3f(colors[i]);
        ((GeometryArray)getGeometry()).updateData(this);
    }

    public int getShapeIndex(int vertexIndex) {
        if (vertexIndex < 0 || vertexIndex >= this.coords.length/3)
            return -1;
        switch (this.type) {
        case POINTS:
            return this.shapeIndices.get(vertexIndex);
        case CUBES:
            return this.shapeIndices.get(3*vertexIndex/cubeverts.length);
        case SPHERES:
            return this.shapeIndices.get(3*vertexIndex/sphereverts.length);
        }
        return -1;
    }

    private void initBuffers() {
        int size = this.shapeIndices.getSize()*getVerticesNumber(this.type)*3;
        this.coords  = new float[size]; 
        this.colors  = new float[size];
        this.normals = new float[size];
    }

    private Geometry createGeometry() {
        GeometryArray geometry = null;
        if (this.shapeIndices.getSize() < 1) {
            geometry = createEmptyGeometry();
            return geometry;
        } else
            switch (this.type) {
            case POINTS:
                geometry = createPointsGeometry();
                break;
            case CUBES:
                geometry = createCubesGeometry();
                break;
            case SPHERES:
                geometry = createSpheresGeometry();
                break;
            }
        setCapabilities(geometry);
        geometry.setCoordRefFloat(this.coords);
        geometry.setColorRefFloat(this.colors);
        geometry.setNormalRefFloat(this.normals);
        return geometry;
    }

    private void setCapabilities(Geometry geometry) {
        geometry.setCapability(GeometryArray.ALLOW_INTERSECT);
        geometry.setCapability(GeometryArray.ALLOW_COUNT_READ);
        geometry.setCapability(GeometryArray.ALLOW_FORMAT_READ);
        geometry.setCapability(GeometryArray.ALLOW_REF_DATA_READ);
        geometry.setCapability(GeometryArray.ALLOW_REF_DATA_WRITE);
    }

    private Appearance createAppearance() {
        Appearance appearance = new Appearance();
        appearance.setMaterial(new Material());
        appearance.setPointAttributes(new PointAttributes(this.size*1000f, true));
        return appearance;
    }

    private void initData() {
        initCoords();
        initColors();
        initNormals();
    }

    private void initCoords() {
        int shapeIndex = 0;
        float x, y;
        float scale = Math.max(end.x-start.x, end.y-start.y);
        for (int i=0; i<locations.length; i++) {
            x = locations[i][0];
            y = locations[i][1];
            if (x>=start.x && x<=start.x+scale && y>=start.y && y<=start.y+scale)
                setCoords(shapeIndex++, (x-start.x)/scale, 0, (y-start.y)/scale);
        }
    }

    private void setCoords(int shapeIndex, float x, float y, float z) {
        switch (this.type) {
        case POINTS:
            setPointCoords(shapeIndex, x, y, z);
            break;
        case CUBES:
            setCubeCoords(shapeIndex, x, y, z);
            break;
        case SPHERES:
            setSphereCoords(shapeIndex, x, y, z);
            break;
        }
    }

    private void setPointCoords(int shapeIndex, float x, float y, float z) {
        int pos = shapeIndex*3;
        this.coords[pos++] = x;
        this.coords[pos++] = y;
        this.coords[pos++] = z;
    }

    private void setCubeCoords(int shapeIndex, float x, float y, float z) {
        int pos = shapeIndex*cubeverts.length;
        for (int i=0; i<cubeverts.length; i+=3) {
            this.coords[pos++] = cubeverts[i+0]*this.size + x;
            this.coords[pos++] = cubeverts[i+1]*this.size + y;
            this.coords[pos++] = cubeverts[i+2]*this.size + z;
        }
    }

    private void setSphereCoords(int shapeIndex, float x, float y, float z) {
        int pos = shapeIndex*sphereverts.length;
        for (int i=0; i<sphereverts.length; i+=3) {
            this.coords[pos++] = sphereverts[i+0]*this.size + x;
            this.coords[pos++] = sphereverts[i+1]*this.size + y;
            this.coords[pos++] = sphereverts[i+2]*this.size + z;
        }
    }

    private void initColors() {
        for (int i=0; i<this.colors.length; i++)
            this.colors[i] = 0.6f;
        if (this.colors_c3f == null || this.colorIndices == null)
            return;
        int colorIndex;
        for (int i=0; i<this.shapeIndices.getSize(); i++) {
            colorIndex = this.colorIndices[this.shapeIndices.get(i)];
            if (colorIndex >= 0)
                setShapeColor(i, this.colors_c3f[colorIndex]);
        }
    }

    private void setShapeColor(int shapeIndex, Color3f color) {
        int vertexNumber = getVerticesNumber(this.type);
        int pos = vertexNumber*3*shapeIndex;
        for (int i=0; i<vertexNumber; i++) {
            this.colors[pos++] = color.x;
            this.colors[pos++] = color.y;
            this.colors[pos++] = color.z;
        }
    }

    private void initNormals() {
        switch (this.type) {
        case POINTS:
            initPointsNormals();
            break;
        case CUBES:
            initCubesNormals();
            break;
        case SPHERES:
            initSpheresNormals();
            break;
        }
    }

    private void initPointsNormals() {
        for (int i=0; i<this.normals.length; i+=3) {
            this.normals[i+0] = 0f;
            this.normals[i+1] = 0f;
            this.normals[i+2] = 1f;
        }
    }

    private void initCubesNormals() {
        for (int i=0; i<this.normals.length; i+=cubenormals.length)
            for (int j=0; j<cubenormals.length; j++)
                this.normals[i+j] = cubenormals[j];
    }

    private void initSpheresNormals() {
        for (int i=0; i<this.normals.length; i+=spherenormals.length)
            for (int j=0; j<spherenormals.length; j++)
                this.normals[i+j] = spherenormals[j];
    }

    private GeometryArray createEmptyGeometry() {
        GeometryArray geometry = new LineArray(2, GeometryArray.COORDINATES);
        geometry.setCoordinates(0, new float[] {0,0,0,0,0,0});
        geometry.setCapability(GeometryArray.ALLOW_COUNT_READ);
        geometry.setCapability(GeometryArray.ALLOW_FORMAT_READ);
        geometry.setCapability(GeometryArray.ALLOW_COORDINATE_READ);
        return geometry;
    }

    private GeometryArray createPointsGeometry() {
        PointArray geometry = new PointArray(this.coords.length/3,
                                             GeometryArray.COORDINATES |
                                             GeometryArray.COLOR_3     |
                                             GeometryArray.NORMALS     |
                                             GeometryArray.BY_REFERENCE
                                            );
        return geometry;
    }

    private GeometryArray createCubesGeometry() {
        GeometryArray geometry = new QuadArray(this.coords.length/3,
                                               GeometryArray.COORDINATES |
                                               GeometryArray.COLOR_3     |
                                               GeometryArray.NORMALS     |
                                               GeometryArray.BY_REFERENCE
                                              );
        return geometry;
    }

    private GeometryArray createSpheresGeometry() {
        int[] stripVertexCounts = new int[spherestrips.length*this.shapeIndices.getSize()];
        for (int i=0; i<stripVertexCounts.length; i+=spherestrips.length)
            for (int j=0; j<spherestrips.length; j++)
                stripVertexCounts[i+j] = spherestrips[j];
        GeometryArray geometry = new TriangleStripArray(this.coords.length/3,
                                                        GeometryArray.COORDINATES |
                                                        GeometryArray.COLOR_3     |
                                                        GeometryArray.NORMALS     |
                                                        GeometryArray.BY_REFERENCE,
                                                        stripVertexCounts
                                                       );
        return geometry;
    }

    private IntArray getShapeIndices() {
        IntArray indices = new IntArray();
        float x, y;
        float scale = Math.max(this.end.x-this.start.x, this.end.y-this.start.y);
        for (int i=0; i<this.locations.length; i++) {
            x = this.locations[i][0];
            y = this.locations[i][1];
            if (x>=this.start.x && x<=this.start.x+scale && 
                y>=this.start.y && y<=this.start.y+scale)
                indices.add(i);
        }
        return indices;
    }

    public void updateData(Geometry geometry) {
        initColors();
    }

    private static int getVerticesNumber(int shapeType) {
        switch (shapeType) {
        case POINTS:
            return 1;
        case CUBES:
            return cubeverts.length/3;
        case SPHERES:
            return sphereverts.length/3;
        }
        return 0;
    }

    private static final float cubeverts[] = {
        1f, -1f,  1f,  1f,  1f,  1f, -1f,  1f,  1f, -1f, -1f,  1f, // front
        -1f, -1f, -1f, -1f,  1f, -1f,  1f,  1f, -1f,  1f, -1f, -1f, // back
        1f, -1f, -1f,  1f,  1f, -1f,  1f,  1f,  1f,  1f, -1f,  1f, // right
        -1f, -1f,  1f, -1f,  1f,  1f, -1f,  1f, -1f, -1f, -1f, -1f, // left
        1f,  1f,  1f,  1f,  1f, -1f, -1f,  1f, -1f, -1f,  1f,  1f, // upper
        -1f, -1f,  1f, -1f, -1f, -1f,  1f, -1f, -1f,  1f, -1f,  1f  // bottom
    };

    private static final float cubenormals[] = {
        0,  0,  1,  0,  0,  1,  0,  0,  1,  0,  0,  1, // front 
        0,  0, -1,  0,  0, -1,  0,  0, -1,  0,  0, -1, // back  
        1,  0,  0,  1,  0,  0,  1,  0,  0,  1,  0,  0, // right 
        -1,  0,  0, -1,  0,  0, -1,  0,  0, -1,  0,  0, // left  
        0,  1,  0,  0,  1,  0,  0,  1,  0,  0,  1,  0, // upper 
        0, -1,  0,  0, -1,  0,  0, -1,  0,  0, -1,  0  // bottom
    };

    private static final float sphereverts[];
    private static final float spherenormals[];
    private static final int   spherestrips[];

    static {
        Sphere sphere = new Sphere(1f, Primitive.GENERATE_NORMALS, 9);
        TriangleStripArray geometry = (TriangleStripArray)sphere.getShape().getGeometry();
        int count = geometry.getVertexCount();
        sphereverts   = new float[count*3];
        spherenormals = new float[count*3];
        spherestrips  = new int[geometry.getNumStrips()];
        geometry.getCoordinates(0, sphereverts);
        geometry.getNormals(0, spherenormals);
        geometry.getStripVertexCounts(spherestrips);
    }
}
