/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: ControlPanel.java,v $
 * $Revision: 1.1.1.1 $
 * $Date: 2003-08-21 21:04:24 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.terrain;

import java.awt.*;
 
import javax.swing.*;
import javax.swing.border.BevelBorder;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import javax.media.j3d.*;

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
