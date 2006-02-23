/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: TerrainBillboard.java,v $
 * $Revision: 1.4 $
 * $Date: 2006-02-23 20:59:56 $
 * $Author: caliente $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.terrain;

import javax.media.j3d.Billboard;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.WakeupCondition;
import javax.media.j3d.WakeupOnElapsedFrames;
import javax.media.j3d.WakeupOnTransformChange;
import javax.vecmath.Point3f;

public class TerrainBillboard extends Billboard {

    private WakeupOnTransformChange wakeupFrameTG;

    public TerrainBillboard(TransformGroup transformgroup, int i, Point3f point3f, TransformGroup platformTransform) {
        super(transformgroup, i, point3f); 
        this.wakeupFrameTG = new WakeupOnTransformChange(platformTransform);
    }

    public void initialize() {
        super.wakeupOn(new WakeupOnElapsedFrames(0, true));
    }

    protected void wakeupOn(WakeupCondition wakeupcondition) {
        super.wakeupOn(this.wakeupFrameTG);
    }
}
