/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: SelectionShape.java,v $
 * $Revision: 1.1.1.1 $
 * $Date: 2003-08-21 21:04:24 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.terrain;

import javax.vecmath.*;
import javax.media.j3d.*;
import java.util.Arrays;
import com.sun.j3d.utils.geometry.*;

public class SelectionShape extends Shape3D {

    private Point3d from = new Point3d();
    private Point3d to = new Point3d();

    public SelectionShape() {
        setCapability(ALLOW_GEOMETRY_READ);
        setGeometry(createGeometry());
        setAppearance(createAppearance());
    }

    public void startSelection(Point3d from) {
        if (from == null)
            return;
        this.from = from;
    }

    public void dragSelection(Point3d to) {
        if (to == null)
            return;
        this.to = to;
        updateCoords((GeometryArray)getGeometry(), this.from, this.to);
    }

    public boolean hasSelection() {
        return !from.equals(to);
    }

    public Point2f getStartCoords() {
        float x1 = (float)Math.min(this.from.x, this.to.x);
        float z1 = (float)Math.min(this.from.z, this.to.z);
        return new Point2f(x1, z1);
    }

    public Point2f getEndCoords() {
        float x2 = (float)Math.max(this.from.x, this.to.x);
        float z2 = (float)Math.max(this.from.z, this.to.z);
        return new Point2f(x2, z2);
    }

    public void clearSelection() {
        this.from.set(0, 0, 0);
        this.to.set(0, 0, 0);
        updateCoords((GeometryArray)getGeometry(), this.from, this.to);
    }

    protected Appearance createAppearance() {
        PolygonAttributes pa = new PolygonAttributes();
        pa.setCullFace(PolygonAttributes.CULL_NONE);

        TransparencyAttributes ta = new TransparencyAttributes();
        ta.setTransparency(0.5f);
        ta.setTransparencyMode(TransparencyAttributes.BLENDED);

        Material material = new Material();
        material.setDiffuseColor(0.5f, 0.5f, 0.5f);

        Appearance appearance = new Appearance();
        appearance.setTransparencyAttributes(ta);
        appearance.setPolygonAttributes(pa);
        appearance.setMaterial(material);
        return appearance;
    }

    protected Geometry createGeometry() {
        QuadArray geometry = new QuadArray(24, GeometryArray.COORDINATES | GeometryArray.NORMALS);
        geometry.setCapability(GeometryArray.ALLOW_COORDINATE_WRITE);
        geometry.setNormals(0, normals);
        geometry.setCapability(Geometry.ALLOW_INTERSECT);           
        geometry.setCapability(GeometryArray.ALLOW_COUNT_READ);     
        geometry.setCapability(GeometryArray.ALLOW_FORMAT_READ);    
        geometry.setCapability(GeometryArray.ALLOW_COORDINATE_READ);
        return geometry;
    }

    public void updateData(Geometry geometry) {
        updateCoords((GeometryArray)geometry, this.from, this.to);
    }

    protected void updateCoords(GeometryArray geometry, Point3d from, Point3d to) {
        float x1 = (float)Math.min(from.x, to.x);
        float z1 = (float)Math.min(from.z, to.z);
        float x2 = (float)Math.max(from.x, to.x);
        float z2 = (float)Math.max(from.z, to.z);
        float y1 = 0f;
        float y2 = from.equals(to) ? 0f : 0.25f;

        Point3f p3f = new Point3f();
        //front
        p3f.set(x1, y1, z2); geometry.setCoordinate(0 , p3f);
        p3f.set(x2, y1, z2); geometry.setCoordinate(1 , p3f);
        p3f.set(x2, y2, z2); geometry.setCoordinate(2 , p3f);
        p3f.set(x1, y2, z2); geometry.setCoordinate(3 , p3f);
        //back
        p3f.set(x1, y1, z1); geometry.setCoordinate(4 , p3f);
        p3f.set(x1, y2, z1); geometry.setCoordinate(5 , p3f);
        p3f.set(x2, y2, z1); geometry.setCoordinate(6 , p3f);
        p3f.set(x2, y1, z1); geometry.setCoordinate(7 , p3f);
        //left
        p3f.set(x1, y1, z1); geometry.setCoordinate(8 , p3f);
        p3f.set(x1, y1, z2); geometry.setCoordinate(9 , p3f);
        p3f.set(x1, y2, z2); geometry.setCoordinate(10, p3f);
        p3f.set(x1, y2, z1); geometry.setCoordinate(11, p3f);
        //right
        p3f.set(x2, y1, z2); geometry.setCoordinate(12, p3f);
        p3f.set(x2, y1, z1); geometry.setCoordinate(13, p3f);
        p3f.set(x2, y2, z1); geometry.setCoordinate(14, p3f);
        p3f.set(x2, y2, z2); geometry.setCoordinate(15, p3f);
        //upper
        p3f.set(x1, y2, z1); geometry.setCoordinate(16, p3f);
        p3f.set(x1, y2, z2); geometry.setCoordinate(17, p3f);
        p3f.set(x2, y2, z2); geometry.setCoordinate(18, p3f);
        p3f.set(x2, y2, z1); geometry.setCoordinate(19, p3f);
        //bottom                                     
        p3f.set(x1, y1, z1); geometry.setCoordinate(20, p3f);
        p3f.set(x2, y1, z1); geometry.setCoordinate(21, p3f);
        p3f.set(x2, y1, z2); geometry.setCoordinate(22, p3f);
        p3f.set(x1, y1, z2); geometry.setCoordinate(23, p3f);
    }

    private static final float normals[] = {
           0,  0,  1,  0,  0,  1,  0,  0,  1,  0,  0,  1, // front 
           0,  0, -1,  0,  0, -1,  0,  0, -1,  0,  0, -1, // back  
          -1,  0,  0, -1,  0,  0, -1,  0,  0, -1,  0,  0, // left  
           1,  0,  0,  1,  0,  0,  1,  0,  0,  1,  0,  0, // right 
           0,  1,  0,  0,  1,  0,  0,  1,  0,  0,  1,  0, // upper 
           0, -1,  0,  0, -1,  0,  0, -1,  0,  0, -1,  0  // bottom
    };
}
