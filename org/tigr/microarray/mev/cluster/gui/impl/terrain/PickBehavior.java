/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: PickBehavior.java,v $
 * $Revision: 1.1.1.2 $
 * $Date: 2004-02-06 21:48:18 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.terrain;

import java.util.Enumeration;

import javax.media.j3d.*;

import com.sun.j3d.utils.picking.*;
import com.sun.j3d.utils.picking.behaviors.PickMouseBehavior;
import java.awt.event.MouseEvent;
import java.awt.AWTEvent;

public class PickBehavior extends Behavior {

    private PickCanvas pickCanvas;
    private PickListener listener;
    private WakeupOr wakeupCondition;

    public PickBehavior(BranchGroup branchgroup, Canvas3D canvas3d, Bounds bounds) {
        this.pickCanvas = new PickCanvas(canvas3d, branchgroup);
        this.pickCanvas.setTolerance(1f);
        this.pickCanvas.setMode(PickTool.GEOMETRY_INTERSECT_INFO);
        setSchedulingBounds(bounds);
    }

    public void setTolerance(float value) {
        this.pickCanvas.setTolerance(value);
    }

    public float getTolerance() {
        return this.pickCanvas.getTolerance();
    }

    public void initialize() {
        WakeupCriterion[] conditions = new WakeupCriterion[3];
        conditions[0] = new WakeupOnAWTEvent(MouseEvent.MOUSE_RELEASED);
        conditions[1] = new WakeupOnAWTEvent(MouseEvent.MOUSE_PRESSED);
        conditions[2] = new WakeupOnAWTEvent(MouseEvent.MOUSE_DRAGGED);
        this.wakeupCondition = new WakeupOr(conditions);
        wakeupOn(this.wakeupCondition);
    }

    public void setPickListener(PickListener listener) {
        this.listener = listener;
    }

    public void processStimulus(Enumeration enumeration) {
        while (enumeration.hasMoreElements()) {
            WakeupCriterion wakeupcriterion = (WakeupCriterion)enumeration.nextElement();
            if (wakeupcriterion instanceof WakeupOnAWTEvent) {
                AWTEvent[] events = ((WakeupOnAWTEvent)wakeupcriterion).getAWTEvent();
                if (events.length > 0) {
                    for (int i=0; i<events.length-1; i++)
                        if (events[i].getID() != events[i+1].getID())
                            processMouseEvent((MouseEvent)events[i]);
                    processMouseEvent((MouseEvent)events[events.length-1]);
                }
            }
        }
        wakeupOn(this.wakeupCondition);
    }

    private void processMouseEvent(MouseEvent event) {
        if (this.listener == null)
            return;
        switch (event.getID()) {
        case MouseEvent.MOUSE_PRESSED:
            this.listener.onMousePressed(event, this.pickCanvas);
            break;
        case MouseEvent.MOUSE_RELEASED:
            this.listener.onMouseReleased(event, this.pickCanvas);
            break;
        case MouseEvent.MOUSE_DRAGGED:
            this.listener.onMouseDragged(event, this.pickCanvas);
            break;
        }
    }
}
