/*
Copyright @ 1999-2005, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: OpenBehavior.java,v $
 * $Revision: 1.1 $
 * $Date: 2005-03-10 15:20:14 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.dam;

import java.util.Enumeration;
import java.awt.event.KeyEvent;

import javax.media.j3d.Behavior;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.WakeupCriterion;
import javax.media.j3d.WakeupOnAWTEvent;
import javax.media.j3d.WakeupOnElapsedFrames;

public class OpenBehavior extends Behavior {
    private TransformGroup targetTG;
    private WakeupCriterion wakeupNextFrame;
    private WakeupCriterion AWTEventCondition;
    private Transform3D t3D = new Transform3D();
    private Transform3D actualTr = new Transform3D();
    private double doorAngle, stepAngle;
    private int mode = 0;
    
    /**
     * Constructs an <code>OpenBehavior</code> for specified target.
     */
    OpenBehavior(TransformGroup targetTG){
	this.targetTG = targetTG;
	AWTEventCondition = new WakeupOnAWTEvent(KeyEvent.KEY_PRESSED);
	wakeupNextFrame = new WakeupOnElapsedFrames(0);
    }
    
    /**
     * Initializes wake up condition.
     */
    public void initialize() {
	this.wakeupOn(AWTEventCondition);
	doorAngle = 0.0;
	stepAngle = 0.0;
    }
    
    /**
     * Process keyboard events.
     */
    public void processStimulus(Enumeration criteria) {
	if (criteria.nextElement().equals(AWTEventCondition)) {
	    mode = -1;
	    switch (((KeyEvent)((WakeupOnAWTEvent)AWTEventCondition).getAWTEvent()[0]).getKeyCode()) {
		case KeyEvent.VK_X: mode = 0; break;
		case KeyEvent.VK_Y: mode = 1; break;
		case KeyEvent.VK_Z: mode = 2; break;
		case KeyEvent.VK_RIGHT: mode = 1; break;
		case KeyEvent.VK_DOWN: mode = 0; break;
		case KeyEvent.VK_LEFT: mode = 3; break;
		case KeyEvent.VK_UP: mode = 4; break;
		default:;
	    }
	}
	if (mode != -1) {
	    if (doorAngle < Math.PI/2.0) {
		stepAngle = Math.PI/20.0;
		doorAngle += Math.PI/20.0;
		if (doorAngle > (Math.PI/2.0)) {
		    doorAngle = (Math.PI/2.0);
		}
		targetTG.getTransform(actualTr);
		switch (mode) {
		    case 0:
			t3D.rotX(stepAngle);
			break;
		    case 1:
			t3D.rotY(stepAngle);
			break;
		    case 2:
			t3D.rotZ(stepAngle);
			break;
		    case 3:
			stepAngle = -Math.PI/20.0;
			t3D.rotY(stepAngle);
			break;
		    case 4:
			stepAngle = -Math.PI/20.0;
			t3D.rotX(stepAngle);
			break;
		    default: {}
		}
		t3D.mul(actualTr);
		targetTG.setTransform(t3D);
		wakeupOn(wakeupNextFrame);
	    } else {
		doorAngle = 0.0;
		wakeupOn(AWTEventCondition);
	    }
	} else {
	    doorAngle = 0.0;
	    wakeupOn(AWTEventCondition);
	}
    }
}
