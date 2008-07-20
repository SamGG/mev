/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: DriftInterpolator.java,v $
 * $Revision: 1.4 $
 * $Date: 2006-02-23 20:59:56 $
 * $Author: caliente $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.terrain;

import java.util.Enumeration;

import javax.media.j3d.Alpha;
import javax.media.j3d.BoundingLeaf;
import javax.media.j3d.Interpolator;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.WakeupOnBehaviorPost;
import javax.vecmath.Matrix3d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

public class DriftInterpolator extends Interpolator {

    private static final int POST_ID = 0;

    private TransformGroup target;
    private float min_distance = 0.15f;
    // initial position
    private Vector3d start_v3d = new Vector3d();
    private Point3d  start_eye_p3d = new Point3d();
    private Vector3d start_up_v3d = new Vector3d();
    // desired position
    private Point3d finish_p3d = new Point3d();
    private Point3d lookat_p3d = new Point3d();

    public DriftInterpolator(TransformGroup target, BoundingLeaf boundingLeaf) {
        super(new Alpha(1, 3000));
        this.target = target;
        setSchedulingBoundingLeaf(boundingLeaf);
    }

    public void setMinDistance(float distance) {
        this.min_distance = distance;
    }

    public float getMinDistance() {
        return this.min_distance;
    }

    public void initialize() {
        wakeupOn(new WakeupOnBehaviorPost(this, POST_ID));
    }

    public void processStimulus(Enumeration enumeration) {
        Alpha alpha = getAlpha();
        doDrifting(alpha);
        if (alpha.finished()) {
            wakeupOn(new WakeupOnBehaviorPost(this, POST_ID));
        } else {
            wakeupOn(super.defaultWakeupCriterion);
        }
    }

    public void drift(Point3d point, Vector3f normal) {
        Transform3D target_t3d = new Transform3D();
        this.target.getTransform(target_t3d);
        target_t3d.get(this.start_v3d);        // position
        Matrix3d scale_m3d = new Matrix3d();
        target_t3d.getRotationScale(scale_m3d);// rotation
        Vector3d z_normal = new Vector3d();
        scale_m3d.getColumn(2, z_normal);      // z normal
        this.start_up_v3d.set(-scale_m3d.m10/scale_m3d.m22, scale_m3d.m00/scale_m3d.m22, 0);
        this.start_up_v3d.normalize();         // up vector
        // sets a desired position
        this.lookat_p3d.set(point);
        this.finish_p3d.x = point.x + min_distance*normal.x;
        this.finish_p3d.y = point.y + min_distance*normal.y;
        this.finish_p3d.z = point.z + min_distance*normal.z;
        // init eye
        Transform3D eye_t3d = new Transform3D();
        Vector3d temp_v3d = new Vector3d(0, 0, -1);
        eye_t3d.set(temp_v3d);
        target_t3d.mul(eye_t3d);
        target_t3d.get(temp_v3d);
        this.start_eye_p3d.set(temp_v3d);
        // start the motion
        getAlpha().setStartTime(System.currentTimeMillis());
        postId(POST_ID);
    }

    private void doDrifting(Alpha alpha) {
        Transform3D target_t3d = new Transform3D();
        this.target.getTransform(target_t3d);

        move(target_t3d, alpha);
        rotate(target_t3d, alpha);

        this.target.setTransform(target_t3d);
    }

    private void move(Transform3D target_t3d, Alpha alpha) {
        // getting current direction
        Vector3d target_v3d = new Vector3d();
        target_t3d.get(target_v3d);
        Matrix3d scale_m3d = new Matrix3d();
        target_t3d.getRotationScale(scale_m3d);
        Vector3d finish_v3d = new Vector3d(this.finish_p3d);
        Matrix3d diff_m3d = new Matrix3d();
        diff_m3d.m00 = finish_v3d.x - target_v3d.x;
        diff_m3d.m10 = finish_v3d.y - target_v3d.y;
        diff_m3d.m20 = finish_v3d.z - target_v3d.z;
        scale_m3d.transpose();
        scale_m3d.mul(diff_m3d);
        Vector3d direction_v3d = new Vector3d();
        scale_m3d.getColumn(0, direction_v3d);
        double desired_distance = distance(this.start_v3d, finish_v3d)*(1-alpha.value());
        if (desired_distance < 0.000001)
            return;
        double current_distance = distance(target_v3d, finish_v3d);
        direction_v3d.scale(1-desired_distance/current_distance);
        Transform3D move_t3d = new Transform3D();
        move_t3d.set(direction_v3d);
        target_t3d.mul(move_t3d);
    }

    private void rotate(Transform3D target_t3d, Alpha alpha) {
        Point3d diff_p3d = new Point3d();
        diff_p3d.sub(this.lookat_p3d, this.start_eye_p3d);
        diff_p3d.scale(alpha.value());
        Point3d curr_p3d = new Point3d();
        curr_p3d.add(this.start_eye_p3d, diff_p3d);

        Matrix3d scale_m3d = new Matrix3d();
        target_t3d.getRotationScale(scale_m3d);
        double up_y = this.start_up_v3d.y+(1-this.start_up_v3d.y)*alpha.value();
        double up_x = Math.sqrt(1-up_y*up_y);
        up_x = (scale_m3d.m10 < 0 && scale_m3d.m22 < 0 || scale_m3d.m10 > 0 && scale_m3d.m22 > 0) ? -up_x : up_x;
        Vector3d up_v3d = new Vector3d(up_x, up_y, 0);

        Vector3d current_v3d = new Vector3d();
        target_t3d.get(current_v3d);
        if (current_v3d.x == curr_p3d.x)
            curr_p3d.x -= 0.000001;
        if (current_v3d.y == curr_p3d.y)
            curr_p3d.y -= 0.000001;
        if (current_v3d.z == curr_p3d.z)
            curr_p3d.z -= 0.000001;
        Transform3D t3d = new Transform3D();
        t3d.lookAt(new Point3d(current_v3d), curr_p3d, up_v3d);
        t3d.invert();
        target_t3d.set(t3d);
    }

    private final double distance(Vector3d v1, Vector3d v2) {
        double x = v1.x - v2.x;
        double y = v1.y - v2.y;
        double z = v1.z - v2.z;
        return Math.sqrt(x*x + y*y + z*z);
    }
}
