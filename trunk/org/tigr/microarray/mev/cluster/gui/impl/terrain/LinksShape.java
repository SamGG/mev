/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: LinksShape.java,v $
 * $Revision: 1.2 $
 * $Date: 2005-02-24 20:24:10 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.terrain;

import java.awt.geom.Rectangle2D;

import javax.media.j3d.Appearance;
import javax.media.j3d.Geometry;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.LineArray;
import javax.media.j3d.LineAttributes;
import javax.media.j3d.Material;
import javax.media.j3d.Shape3D;
import javax.vecmath.Point2f;

import org.tigr.microarray.mev.cluster.gui.impl.util.FloatArray;

public class LinksShape extends Shape3D {

    private int[][] subnets;
    private float[][] weights;
    private float[][] locations;

    private Point2f left_up = new Point2f(0f,0f);
    private Point2f right_bottom = new Point2f(1f,1f);
    private float threshold = 0.8f;
    private boolean visible = false;
    private static final float[][] EMPTY_BUF = new float[][] {{0,0,0,0,0,0} , {0,0,0,0,0,0}};

    public LinksShape(int[][] subnets, float[][] weights, float[][] locations) {
        setCapability(ALLOW_GEOMETRY_READ);
        setCapability(ALLOW_GEOMETRY_WRITE);
        setCapability(ALLOW_APPEARANCE_READ);
        this.subnets = subnets;
        this.weights = weights;
        this.locations = locations;
        updateGeometry();
        setAppearance(createAppearance());
    }

    public void setZoom(Point2f left_up, Point2f right_bottom) {
        this.left_up.set(left_up);
        this.right_bottom.set(right_bottom);
        updateGeometry();
    }

    public void setThreshold(float threshold) {
        this.threshold = threshold;
        updateGeometry();
    }

    public float getThreshold() {
        return this.threshold;
    }

    public void setVisible(boolean value) {
        if (this.visible == value)
            return;
        this.visible = value;
        updateGeometry();
    }

    public void setLinksWidth(float value) {
        Appearance appearance = getAppearance();
        LineAttributes la = appearance.getLineAttributes();
        la.setLineWidth(value);
    }

    public float getLinksWidth() {
        Appearance appearance = getAppearance();
        LineAttributes la = appearance.getLineAttributes();
        return la.getLineWidth();
    }

    private void updateGeometry() {
        setGeometry(createGeometry());
    }

    private Geometry createGeometry() {
        float[][] vertCoords = createLinksCoordinaties();
        GeometryArray geometry = new LineArray(vertCoords[0].length/3, GeometryArray.COORDINATES | GeometryArray.COLOR_3);
        geometry.setCoordinates(0, vertCoords[0]);
        geometry.setColors(0, vertCoords[1]);
        geometry.setCapability(Geometry.ALLOW_INTERSECT);           
        geometry.setCapability(GeometryArray.ALLOW_COUNT_READ);     
        geometry.setCapability(GeometryArray.ALLOW_FORMAT_READ);    
        geometry.setCapability(GeometryArray.ALLOW_COORDINATE_READ);
        return geometry;
    }

    private float[][] createLinksCoordinaties() {
        if (!this.visible)
            return EMPTY_BUF;
        Point2f p1 = new Point2f();
        Point2f p2 = new Point2f();
        Point2f n1 = new Point2f();
        Point2f n2 = new Point2f();
        Rectangle2D.Float bounds = new Rectangle2D.Float(0,0,1,1);
        float scale = 1f/Math.max(right_bottom.x-left_up.x, right_bottom.y-left_up.y);
        int coordPos = 0;
        FloatArray coords = new FloatArray(100);
        FloatArray colors = new FloatArray(100);
        for (int i=0; i<subnets.length; i++)
            if (subnets[i].length > 1) {
                float x1 = (locations[i][0]-left_up.x)*scale;
                float y1 = (locations[i][1]-left_up.y)*scale;
                if (x1>=0f && x1<=1f && y1>=0f && y1<=1f)
                    for (int j=1; j<subnets[i].length; j++) {
                        float x2 = (locations[subnets[i][j]][0]-left_up.x)*scale;
                        float y2 = (locations[subnets[i][j]][1]-left_up.y)*scale;
                        p1.set(x1, y1);
                        p2.set(x2, y2);
                        if (Math.abs(weights[i][j]) > threshold && isLineIntersects(bounds, p1, p2, n1, n2)) {
                            coords.add(n1.x);
                            coords.add(0);
                            coords.add(n1.y);
                            coords.add(n2.x);
                            coords.add(0);
                            coords.add(n2.y);
                            colors.add(weights[i][j]);
                            colors.add(0);
                            colors.add(1-weights[i][j]);
                            colors.add(weights[i][j]);
                            colors.add(0);
                            colors.add(1-weights[i][j]);
                        }
                    }
            }
        if (coords.getSize() == 0)
            return EMPTY_BUF;
        float[][] result = new float[2][];
        result[0] = coords.toArray();
        result[1] = colors.toArray();
        return result;
    }

    /**
     * Checkes if (p1, p2) line intersects with vertical (x, y1, y2) one.
     * @return point the intersection coordinate.
     */
    private final boolean isIntersectVerticalLine(Point2f p1, Point2f p2, float x, float y1, float y2, Point2f point) {
        if ((p1.x < x && p2.x < x) || ((p1.x > x && p2.x > x))) {
            return false;
        }
        float tan = (p2.y - p1.y)/(p2.x - p1.x);
        float delta = tan*(x - p1.x);
        point.x = x;
        point.y = p1.y + delta;
        return point.y > Math.min(y1, y2) && point.y < Math.max(y1, y2);
    }

    /**
     * Checkes if (p1, p2) line intersects with horizontal (y, x1, x2) one.
     * @return point the intersection coordinate.
     */
    private final boolean isIntersectHorizontalLine(Point2f p1, Point2f p2, float y, float x1, float x2, Point2f point) {
        if ((p1.y < y && p2.y < y) || ((p1.y > y && p2.y > y))) {
            return false;
        }
        float tan = (p2.y - p1.y)/(p2.x - p1.x);
        float delta = (y - p1.y)/tan;
        point.x = p1.x + delta;
        point.y = y;
        return point.x > Math.min(x1, x2) && point.x < Math.max(x1, x2);
    }

    /**
     * Checkes if points p1 or p2 are an internal point of a rect.
     * @return n the coordinaties of an internal point.
     */
    private final boolean isInternalPoint(Rectangle2D.Float rect, Point2f p1, Point2f p2, Point2f n) {
        boolean p1b = rect.contains(p1.x, p1.y);
        boolean p2b = rect.contains(p2.x, p2.y);
        if (p1b) {
            n.set(p1);
            return true;
        }
        if (p2b) {
            n.set(p2);
            return true;
        }
        return false;
    }

    /**
     * Checkes if (p1, p2) line intersects rect.
     * @return n1, n2 points which is intersection of the line and the rect.
     */
    private final boolean isLineIntersects(Rectangle2D.Float rect, Point2f p1, Point2f p2, Point2f n1, Point2f n2) {
        n1.set(p1);
        n2.set(p2);
        if (rect.contains(p1.x, p1.y) && rect.contains(p2.x, p2.y)) {
            return true;
        }
        if (p1.x < rect.x && p2.x < rect.x) {
            return false;
        }
        if (p1.y < rect.y && p2.y < rect.y) {
            return false;
        }
        if (p1.x > rect.x+rect.width && p2.x > rect.x+rect.width) {
            return false;
        }
        if (p1.y > rect.y+rect.height && p2.y > rect.y+rect.height) {
            return false;
        }
        if (p1.x == p2.x) {
            // vertical line
            if (p1.y < rect.y) {
                n1.y = rect.y;
            } else if (p1.y > rect.y+rect.height) {
                n1.y = rect.y+rect.height;
            }
            if (p2.y < rect.y) {
                n2.y = rect.y;
            } else if (p2.y > rect.y+rect.height) {
                n2.y = rect.y+rect.height;
            }
            return true;
        }
        if (p1.y == p2.y) {
            // horizontal line
            if (p1.x < rect.x) {
                n1.x = rect.x;
            } else if (p1.x > rect.x+rect.width) {
                n1.x = rect.x+rect.width;
            }
            if (p2.x < rect.x) {
                n2.x = rect.x;
            } else if (p2.x > rect.x+rect.width) {
                n2.x = rect.x+rect.width;
            }
            return true;
        }
        if (isIntersectVerticalLine(p1, p2, rect.x, rect.y, rect.y+rect.height, n1)) {
            if (isIntersectVerticalLine(p1, p2, rect.x+rect.width, rect.y, rect.y+rect.height, n2)) {
            } else if (isIntersectHorizontalLine(p1, p2, rect.y, rect.x, rect.x+rect.width, n2)) {
            } else if (isIntersectHorizontalLine(p1, p2, rect.y+rect.height, rect.x, rect.x+rect.width, n2)) {
            } else if (isInternalPoint(rect, p1, p2, n2)) {
            } else {
                return false;
            }
            return true;
        } else if (isIntersectVerticalLine(p1, p2, rect.x+rect.width, rect.y, rect.y+rect.height, n1)) {
            if (isIntersectHorizontalLine(p1, p2, rect.y+rect.height, rect.x, rect.x+rect.width, n2)) {
            } else if (isIntersectHorizontalLine(p1, p2, rect.y, rect.x, rect.x+rect.width, n2)) {
            } else if (isInternalPoint(rect, p1, p2, n2)) {
            } else {
                return false;
            }
            return true;
        } else if (isIntersectHorizontalLine(p1, p2, rect.y, rect.x, rect.x+rect.width, n1)) {
            if (isIntersectHorizontalLine(p1, p2, rect.y+rect.height, rect.x, rect.x+rect.width, n2)) {
            } else if (isInternalPoint(rect, p1, p2, n2)) {
            } else {
                return false;
            }
            return true;
        } else if (isIntersectHorizontalLine(p1, p2, rect.y+rect.height, rect.x, rect.x+rect.width, n1)) {
            if (isInternalPoint(rect, p1, p2, n2)) {
            } else {
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * Creates the LinksShape appearance.
     */
    protected Appearance createAppearance() {
        LineAttributes la = new LineAttributes();
        la.setCapability(LineAttributes.ALLOW_WIDTH_READ);
        la.setCapability(LineAttributes.ALLOW_WIDTH_WRITE);

        Appearance appearance = new Appearance();
        appearance.setCapability(Appearance.ALLOW_LINE_ATTRIBUTES_READ);
        appearance.setCapability(Appearance.ALLOW_LINE_ATTRIBUTES_WRITE);
        appearance.setLineAttributes(la);
        appearance.setMaterial(new Material());
        return appearance;
    }
}
