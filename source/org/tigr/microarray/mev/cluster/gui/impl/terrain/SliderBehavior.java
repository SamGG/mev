/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: SliderBehavior.java,v $
 * $Revision: 1.4 $
 * $Date: 2006-02-23 20:59:56 $
 * $Author: caliente $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.terrain;

import java.awt.event.MouseEvent;
import java.util.Enumeration;

import javax.media.j3d.Behavior;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.WakeupCriterion;
import javax.media.j3d.WakeupOnBehaviorPost;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.vecmath.Vector3d;

public class SliderBehavior extends Behavior implements ChangeListener {

    private TransformGroup target;
    private WakeupCriterion criterion;
    private double scale = 0.01;

    private JSlider slider;

    public SliderBehavior(TransformGroup target) {
        this.target = target;
        this.slider = new JSlider();
        this.slider.addChangeListener(this);
    }

    public void setOrientation(int orientation) {
        this.slider.setOrientation(orientation);
    }

    public void setMinimum(int min) {
        this.slider.setMinimum(min);
    }

    public void setMaximum(int max) {
        this.slider.setMaximum(max);
    }

    public void setValue(int value) {
        this.slider.setValue(value);
    }

    public void setScale(double scale) {
        this.scale = scale;
    }

    public JSlider getSlider() {
        return this.slider;
    }

    public void initialize() {
        this.criterion = new WakeupOnBehaviorPost(this, MouseEvent.MOUSE_DRAGGED);
        wakeupOn(criterion);
    }

    public void processStimulus(Enumeration enumeration) {
        double value = scale*this.slider.getValue();

        Transform3D target_t3d = new Transform3D();
        this.target.getTransform(target_t3d);

        Vector3d target_v3d = new Vector3d();
        target_t3d.get(target_v3d);
        target_v3d.y = value - target_v3d.y;

        Transform3D t3d = new Transform3D();
        t3d.set(target_v3d);

        target_t3d.mul(t3d);
        this.target.setTransform(target_t3d);

        wakeupOn(criterion);
    }

    public void stateChanged(ChangeEvent e) {
        postId(MouseEvent.MOUSE_DRAGGED);
    }
}
