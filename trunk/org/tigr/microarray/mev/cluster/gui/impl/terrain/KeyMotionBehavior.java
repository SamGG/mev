/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: KeyMotionBehavior.java,v $
 * $Revision: 1.1.1.2 $
 * $Date: 2004-02-06 21:48:18 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.terrain;

import java.awt.AWTEvent;
import java.util.Enumeration;
import java.awt.event.KeyEvent;

import javax.media.j3d.*;
import javax.vecmath.*;

public class KeyMotionBehavior extends Behavior {

    public static final int MOVE_LEFT_CMD  = 0;
    public static final int MOVE_RIGHT_CMD = 1;
    public static final int MOVE_FWD_CMD   = 2;
    public static final int MOVE_BWD_CMD   = 3;
    public static final int MOVE_UP_CMD    = 4;
    public static final int MOVE_DOWN_CMD  = 5;
    public static final int TURN_LEFT_CMD  = 6;
    public static final int TURN_RIGHT_CMD = 7;
    public static final int TURN_UP_CMD    = 8;
    public static final int TURN_DOWN_CMD  = 9;
    public static final int TILT_LEFT_CMD  = 10;
    public static final int TILT_RIGHT_CMD = 11;
    public static final int ROT_X_POS_CMD  = 12;
    public static final int ROT_Y_POS_CMD  = 13;
    public static final int ROT_Z_POS_CMD  = 14;
    public static final int ROT_X_NEG_CMD  = 15;
    public static final int ROT_Y_NEG_CMD  = 16;
    public static final int ROT_Z_NEG_CMD  = 17;
    public static final int RESET_CMD      = 18;

    private float STEP = 0.01f;
    private float UNGLE = (float)Math.PI/180f;

    private TransformGroup target;
    private Point3d basis;
    private Transform3D initialTransform = new Transform3D();
    private WakeupOnBehaviorPost wakeupCondition;

    public KeyMotionBehavior(TransformGroup target) {
        this.target = target;
        this.target.getTransform(this.initialTransform);
        this.wakeupCondition = new WakeupOnBehaviorPost(this, 0);
    }

    public void execute(int command) {
        postId(command);
    }

    public void onKeyEvent(KeyEvent e) {
        execute(key2command(e));
    }

    /**
     * Sets basis point for the target rotation.
     */
    public void setBasis(Point3d basis) {
        this.basis = basis;
    }

    public void initialize() {
        wakeupOn(this.wakeupCondition);
    }

    public void processStimulus(Enumeration criteria) {
        while (criteria.hasMoreElements()) {
            WakeupCriterion wakeup = (WakeupCriterion)criteria.nextElement();
            if (wakeup == this.wakeupCondition)
                processCommand(this.wakeupCondition.getTriggeringPostId());
        }
        wakeupOn(this.wakeupCondition);
    }

    private void processCommand(int id) {
        switch (id) {
        case MOVE_LEFT_CMD:
            move_left();
            break;
        case MOVE_RIGHT_CMD:
            move_right();
            break;
        case MOVE_FWD_CMD:
            move_forward();
            break;
        case MOVE_BWD_CMD:
            move_backward();
            break;
        case MOVE_UP_CMD:
            move_up();
            break;
        case MOVE_DOWN_CMD:
            move_down();
            break;
        case TURN_LEFT_CMD:
            turn_left();
            break;
        case TURN_RIGHT_CMD:
            turn_right();
            break;
        case TURN_UP_CMD:
            turn_up();
            break;
        case TURN_DOWN_CMD:
            turn_down();
            break;
        case TILT_LEFT_CMD:
            tilt_left();
            break;
        case TILT_RIGHT_CMD:
            tilt_right();
            break;
        case ROT_X_POS_CMD:
            rotate(this.basis, new AxisAngle4d(1, 0, 0, UNGLE));
            break;
        case ROT_Y_POS_CMD:
            rotate(this.basis, new AxisAngle4d(0, 1, 0, UNGLE));
            break;
        case ROT_Z_POS_CMD:
            rotate(this.basis, new AxisAngle4d(0, 0, 1, UNGLE));
            break;
        case ROT_X_NEG_CMD:
            rotate(this.basis, new AxisAngle4d(1, 0, 0, -UNGLE));
            break;
        case ROT_Y_NEG_CMD:
            rotate(this.basis, new AxisAngle4d(0, 1, 0, -UNGLE));
            break;
        case ROT_Z_NEG_CMD:
            rotate(this.basis, new AxisAngle4d(0, 0, 1, -UNGLE));
            break;
        case RESET_CMD:
            reset();
            break;
        }
    }

    private static int key2command(KeyEvent e) {
        int code = e.getKeyCode();
        if (e.isShiftDown()) {
            switch (code) {
            case KeyEvent.VK_LEFT:
                return TURN_LEFT_CMD;
            case KeyEvent.VK_RIGHT:
                return TURN_RIGHT_CMD;
            case KeyEvent.VK_UP:
                return TURN_UP_CMD;
            case KeyEvent.VK_DOWN:
                return TURN_DOWN_CMD;
            }
        } else if (e.isControlDown()) {
            switch (code) {
            case KeyEvent.VK_LEFT:
                return TILT_LEFT_CMD;
            case KeyEvent.VK_RIGHT:
                return TILT_RIGHT_CMD;
            case KeyEvent.VK_X:
                return ROT_X_POS_CMD;
            case KeyEvent.VK_Y:
                return ROT_Y_POS_CMD;
            case KeyEvent.VK_Z:
                return ROT_Z_POS_CMD;
            }
        } else {
            switch (code) {
            case KeyEvent.VK_LEFT:
                return MOVE_LEFT_CMD;
            case KeyEvent.VK_RIGHT:
                return MOVE_RIGHT_CMD;
            case KeyEvent.VK_UP:
                return MOVE_FWD_CMD;
            case KeyEvent.VK_DOWN:
                return MOVE_BWD_CMD;
            case KeyEvent.VK_PAGE_UP:
                return MOVE_UP_CMD;
            case KeyEvent.VK_PAGE_DOWN:
                return MOVE_DOWN_CMD;
            case KeyEvent.VK_X:
                return ROT_X_NEG_CMD;
            case KeyEvent.VK_Y:
                return ROT_Y_NEG_CMD;
            case KeyEvent.VK_Z:
                return ROT_Z_NEG_CMD;
            case KeyEvent.VK_R:
                return RESET_CMD;
            }
        }
        return -1;
    }

    protected void reset() {
        this.target.setTransform(this.initialTransform);
    }

    protected void turn_left() {
        turnHorizontal(UNGLE);
    }

    protected void turn_right() {
        turnHorizontal(-UNGLE);
    }

    protected void turn_up() {
        turnVertical(UNGLE);
    }

    protected void turn_down() {
        turnVertical(-UNGLE);
    }

    protected void tilt_left() {
        turnFront(UNGLE);
    }

    protected void tilt_right() {
        turnFront(-UNGLE);
    }

    protected void move_left() {
        move(new Vector3f(-STEP, 0f, 0f));
    }

    protected void move_right() {
        move(new Vector3f(STEP, 0f, 0f));
    }

    protected void move_backward() {
        move(new Vector3f(0f, 0f, STEP));
    }

    protected void move_forward() {
        move(new Vector3f(0f, 0f, -STEP));
    }

    protected void move_up() {
        move(new Vector3f(0f, STEP, 0f));
    }

    protected void move_down() {
        move(new Vector3f(0f, -STEP, 0f));
    }

    protected void rotate(Point3d basis, AxisAngle4d axis) {
        if (basis == null || axis == null)
            return;
        // get target transform
        Transform3D target_t3d = new Transform3D();
        this.target.getTransform(target_t3d);
        // get scale matrix
        Matrix3d scale_m3d = new Matrix3d();
        target_t3d.getRotationScale(scale_m3d);
        // get direction
        Vector3d vector3d = new Vector3d();
        target_t3d.get(vector3d);
        // diff distance
        Point3d target_p3d = new Point3d(vector3d);
        Point3d diff_p3d   = new Point3d(basis);
        diff_p3d.sub(target_p3d);
        // calculate direction to the basis point
        Matrix3d basis_m3d = new Matrix3d();
        basis_m3d.m00 = diff_p3d.x;
        basis_m3d.m10 = diff_p3d.y;
        basis_m3d.m20 = diff_p3d.z;
        scale_m3d.transpose();
        scale_m3d.mul(basis_m3d);
        Vector3d direction_v3d = new Vector3d();
        scale_m3d.getColumn(0, direction_v3d);
        // move to basis point
        Transform3D move_t3d = new Transform3D();
        move_t3d.set(direction_v3d);
        target_t3d.mul(move_t3d);
        // rotate
        Transform3D rotx_t3d = new Transform3D();
        rotx_t3d.set(axis);
        target_t3d.mul(rotx_t3d);
        // move back
        direction_v3d.negate();
        move_t3d.set(direction_v3d);
        target_t3d.mul(move_t3d);
        this.target.setTransform(target_t3d);
    }

    protected void move(Vector3f direction) {
        Transform3D transform = new Transform3D();
        transform.set(direction);
        mul(transform);
    }

    protected void turnHorizontal(double ungle) {
        Transform3D transform = new Transform3D();
        transform.rotY(ungle);
        mul(transform);
    }

    protected void turnFront(double ungle) {
        Transform3D transform = new Transform3D();
        transform.rotZ(ungle);
        mul(transform);
    }

    protected void turnVertical(double ungle) {
        Transform3D transform = new Transform3D();
        transform.rotX(ungle);
        mul(transform);
    }

    private void mul(Transform3D transform) {
        Transform3D t3D = new Transform3D();
        this.target.getTransform(t3D);
        t3D.mul(transform);
        this.target.setTransform(t3D);
    }
}
