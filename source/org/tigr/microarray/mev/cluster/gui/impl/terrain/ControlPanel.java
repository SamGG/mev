/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: ControlPanel.java,v $
 * $Revision: 1.4 $
 * $Date: 2006-02-23 20:59:56 $
 * $Author: caliente $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.terrain;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.media.j3d.Behavior;
import javax.media.j3d.BoundingLeaf;
import javax.media.j3d.TransformGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class ControlPanel extends JPanel implements ChangeListener {

    private SliderBehavior sliderBehavior;
    private JLabel label;
    private double scale = 0.01;
    private double curSliderValue = 0;

    public ControlPanel(TransformGroup sliderTarget, KeyMotionBehavior keyMotionBehavior, BoundingLeaf boundingLeaf) {
        setBorder(new BevelBorder(BevelBorder.RAISED));
        setLayout(new GridBagLayout());
        setPreferredSize(new Dimension(110, 130));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // label
        this.label = new JLabel("Map position: 0.0");
        gbc.gridx  = 0;
        gbc.gridy  = 0;
        gbc.weightx = 1.0;
        add(this.label, gbc);

        // slider
        this.sliderBehavior = createSliderBehavior(sliderTarget, boundingLeaf, this);
        gbc.gridx  = 0;
        gbc.gridy  = 1;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.NORTHWEST;

        add(this.sliderBehavior.getSlider(), gbc);

        gbc.gridx  = 1;
        gbc.gridy  = 0;
        gbc.weightx = 0.0;
        gbc.weighty = 1.0;
        gbc.gridheight = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        add(new NavigationPanel(keyMotionBehavior), gbc);
    }

    public Behavior getSliderBehavior() {
        return this.sliderBehavior;
    }

    public double getSliderValue() {
        return this.curSliderValue;
    }

    public void stateChanged(ChangeEvent e) {
        this.curSliderValue = this.scale*((JSlider)e.getSource()).getValue();
        this.label.setText("Map position: "+String.valueOf(this.curSliderValue));
    }

    protected SliderBehavior createSliderBehavior(TransformGroup sliderTarget, BoundingLeaf boundingLeaf, ChangeListener l) {
        SliderBehavior sliderBehavior = new SliderBehavior(sliderTarget);
        sliderBehavior.setSchedulingBoundingLeaf(boundingLeaf);
        sliderBehavior.getSlider().addChangeListener(l);
        sliderBehavior.setOrientation(SwingUtilities.HORIZONTAL);
        sliderBehavior.setMinimum(-30);
        sliderBehavior.setMaximum(30);
        sliderBehavior.setValue(0);
        sliderBehavior.setScale(this.scale);
        return sliderBehavior;
    }


}
