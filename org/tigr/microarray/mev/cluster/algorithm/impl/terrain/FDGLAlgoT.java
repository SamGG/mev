/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: FDGLAlgoT.java,v $
 * $Revision: 1.1.1.2 $
 * $Date: 2004-02-06 21:48:18 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.algorithm.impl.terrain; 

import javax.vecmath.Vector2f;
import org.tigr.microarray.mev.cluster.algorithm.impl.util.*;

public class FDGLAlgoT { // Force Directed Graph Layout algorithm

    public static final float c_AttractiveRadius  = 0f;     // the radius, from which attractive force begin to work == min(0,R^2-c_AttractiveRadius^2; c_RepulsiveRadius^2)
    public static final float c_RepulsiveRadius   = 200f;   // the radius of repulsive force== min(0,c_RepulsiveRadius^2-R^2)
    public static final float c_dblFDGLAlgoThreshold = 1f;
    public static final int   c_iQuadTreeDepth    = 5;
    public static final int   c_iHistoryQueueSize = 10;

    private Vector2f[] m_arrPoints;  // the array of coordinates
    private SLink[][] m_arrLinks;    // the array of links
    private Vector2f m_rPt;

    private QuadTreeT m_QTree;               // QuadTree used for optimization puposes
    private InterfaceToObjects m_pInterface; // use this interface to have a deal with objects

    private Vector2f[] m_arrForceField; // defines the direction and intensivity of force affecting the node
    private float m_dblMaxForceLength;
    private int   m_iDoNotMove;
    private float m_arrEnergyHist[] = new float[] {-1,-1,-1,-1};  // TODO: change 10 to appropriate constant name
    private int   m_iEnergyIndex=0;

    public FDGLAlgoT(InterfaceToObjects pInterface) {
        m_pInterface = pInterface;
        m_QTree = new QuadTreeT(c_iQuadTreeDepth, pInterface);
        m_rPt = new Vector2f(0,0);
        m_iDoNotMove=-1; 
    }

    private void PushEnergy(float fltEnergyValue) {
        m_arrEnergyHist[3] = m_arrEnergyHist[2];
        m_arrEnergyHist[2] = m_arrEnergyHist[1];
        m_arrEnergyHist[1] = m_arrEnergyHist[0];
        m_arrEnergyHist[0] = fltEnergyValue;
    }

    static final float MAX_ENERGY = 100f; //1000f;

    public float getPercentage() {
        return 100f*m_iEnergyIndex/MAX_ENERGY;
    }

    public boolean shouldStop() {
        if (m_iEnergyIndex>MAX_ENERGY)
            return true;
        if (m_arrEnergyHist[3]<0)
            return false;
        // TODO:
        //(cool_max_force_4 != 0.0) &&
        //Math.abs(m_arrEnergyHist[0]-m_arrEnergyHist[2])/m_arrEnergyHist[0]<vibration &&
        //Math.abs(m_arrEnergyHist[1]-m_arrEnergyHist[3])/m_arrEnergyHist[1]<vibration &&
        return false;
    }

    // helper functions
    private static float CalcAttractiveForceFromR2(float R2) {
        float tmpVal=R2-(c_AttractiveRadius*c_AttractiveRadius);
        if (R2>(c_RepulsiveRadius*c_RepulsiveRadius))        // upper boundary, R2 is too much
            return(c_RepulsiveRadius*c_RepulsiveRadius);
        if (tmpVal<0)
            return 0;
        return tmpVal;
    }

    private static float CalcRepulsiveForceFromR2(float R2) {
        float tmpVal=c_RepulsiveRadius*c_RepulsiveRadius-R2;
        if (tmpVal<0)
            return 0;
        return tmpVal;
    }

    private Vector2f CalcAttractiveForcesAt(int iNode) {
        SLink[] rArrAdjNodes = m_arrLinks[iNode];
        int nAdjNodeSize= rArrAdjNodes.length;

        Vector2f vectResult = new Vector2f();
        if (nAdjNodeSize>0) {   // if no adjacent links
            float dblCurX=m_arrPoints[iNode].x;
            float dblCurY=m_arrPoints[iNode].y;

            Vector2f curVector = new Vector2f();
            for (int i=0; i<nAdjNodeSize; i++) {
                Vector2f rCurAdjNode = m_arrPoints[rArrAdjNodes[i].m_iToId];
                float CurWeight=rArrAdjNodes[i].m_Weight;
                curVector.set(rCurAdjNode.x-dblCurX, rCurAdjNode.y-dblCurY);

                float Norm=curVector.length();
                if (Norm<=0)
                    continue;
                float Norm2 =Norm*Norm;
                float attractive_factor  = (float)(CalcAttractiveForceFromR2(Norm2));
                curVector.scale(CurWeight*attractive_factor/Norm);
                vectResult.add(curVector);
            }
        }
        return vectResult;
    }

    private Vector2f CalcRepulsiveForcesAtBruteForce(float dblX, float dblY) {
        // Complexity:O(n^2) where n is the number of nodes in the graph
        int iSize=m_arrPoints.length;
        Vector2f vectResult = new Vector2f();
        Vector2f curVector = new Vector2f();
        for (int i=0; i<iSize; i++) {
            curVector.set(dblX-m_arrPoints[i].x, dblY-m_arrPoints[i].y);
            float Norm=curVector.length();
            float Norm2=Norm*Norm;
            if (Norm2<=0)
                continue;
            if (Norm<=0)
                continue;
            float repulsive_factor  = (float)(CalcRepulsiveForceFromR2(Norm2));
            curVector.scale(repulsive_factor/Norm);
            vectResult.add(curVector);
        }
        return vectResult;
    }

    protected Vector2f CalcRepulsiveForcesAt(float X, float Y) {
        // Complexity:O(n*log2(n), uses QuadTree structure
        m_rPt.x=X;
        m_rPt.y=Y;
        return GetRepulsiveForceFrom(0);
    }

    protected Vector2f GetRepulsiveForceFrom(int iNode) {
        Vector2f ptRes = new Vector2f(0,0);
        if (iNode<0)
            return ptRes;
        QuadTreeT.SNode rQuadTreeNode = m_QTree.m_arrNodes[iNode];  // Current node
        if (rQuadTreeNode.m_iPointNumBehind == 0)
            return ptRes;
        if (rQuadTreeNode.m_iPointNumBehind==1) {
            // prepare ptRes
            ptRes.x=m_rPt.x-m_arrPoints[rQuadTreeNode.m_arrPointsIds[0]].x;
            ptRes.y=m_rPt.y-m_arrPoints[rQuadTreeNode.m_arrPointsIds[0]].y;
            float Norm=ptRes.length();
            if (Norm<=0)
                return ptRes;
            float Norm2=Norm*Norm;
            float repulsive_factor  = (float)(CalcRepulsiveForceFromR2(Norm2));
            ptRes.scale(repulsive_factor/Norm);
            return ptRes;
        }
        boolean bPtInCurRect = rQuadTreeNode.m_Rect.PtInRect(m_rPt);
        boolean bUseAveragePt = false;
        boolean bIsLeaf = rQuadTreeNode.IsLeaf();
        if (!bPtInCurRect)
            bUseAveragePt=rQuadTreeNode.m_Rect.Distance(m_rPt)/Math.max(rQuadTreeNode.m_Rect.Width(), rQuadTreeNode.m_Rect.Height())>=c_dblFDGLAlgoThreshold;    // TODO: may be change to quadratic criteria?
        if (!bIsLeaf&&!bUseAveragePt) {   //then definitely go down
            int iChild=m_QTree.GetChild(iNode, QuadTreeT.LEFT_UP);
            ptRes =GetRepulsiveForceFrom(iChild);
            iChild=m_QTree.GetChild(iNode, QuadTreeT.RIGHT_UP);
            ptRes.add(GetRepulsiveForceFrom(iChild));
            iChild=m_QTree.GetChild(iNode, QuadTreeT.LEFT_DOWN);
            ptRes.add(GetRepulsiveForceFrom(iChild));
            iChild=m_QTree.GetChild(iNode, QuadTreeT.RIGHT_DOWN);
            ptRes.add(GetRepulsiveForceFrom(iChild));
            return ptRes;
        }
        if (bUseAveragePt) {  // the condition to use the average point 
            // prepare ptRes
            ptRes.x=m_rPt.x-rQuadTreeNode.m_ptAvg.x;
            ptRes.y=m_rPt.y-rQuadTreeNode.m_ptAvg.y;
            //System.out.println("rQuadTreeNode.m_ptAvg="+rQuadTreeNode.m_ptAvg);

            float Norm=ptRes.length();
            if (Norm<=0)
                return ptRes;
            float Norm2=Norm*Norm;
            float repulsive_factor  = (float)(CalcRepulsiveForceFromR2(Norm2));
            if (repulsive_factor<=0) {
                ptRes.x=0;
                ptRes.y=0;
                return ptRes;
            }
            ptRes.scale(repulsive_factor*(float)rQuadTreeNode.m_iPointNumBehind/Norm);
            return ptRes;
        }
        // otherwise, brute force strategy
        //ASSERT(bIsLeaf);

        Vector2f curVector = new Vector2f();
        int iSize=rQuadTreeNode.m_arrPointsIds.length;
        for (int i=0; i<iSize; i++) {
            curVector.set(m_rPt.x-m_arrPoints[rQuadTreeNode.m_arrPointsIds[i]].x, m_rPt.y-m_arrPoints[rQuadTreeNode.m_arrPointsIds[i]].y);
            float Norm=curVector.length();
            if (Norm<=0)
                return ptRes;
            float Norm2=Norm*Norm;
            float repulsive_factor  = (float)(CalcRepulsiveForceFromR2(Norm2));
            curVector.scale(repulsive_factor/Norm);
            ptRes.add(curVector);
        }
        return ptRes;
    }

    public void DoNotMove(int i) {
        m_iDoNotMove=i;
    }

    public void CalculateForceField() {  // Force vector field
        int iSize=m_arrPoints.length;
        m_arrForceField = new Vector2f[iSize];
        m_dblMaxForceLength=0;

        // Let's build QuadTree first. This will be used in CalcRepulsiveForcesAt method
        m_QTree.Initialize();

        for (int i=0; i<iSize; i++) {
            m_arrForceField[i]=CalcAttractiveForcesAt(i);
            m_arrForceField[i].add(CalcRepulsiveForcesAt(m_arrPoints[i].x, m_arrPoints[i].y)); //BruteForce
            float dblCurLength=m_arrForceField[i].length();
            if (m_dblMaxForceLength<dblCurLength)   // find also the max length of forces
                m_dblMaxForceLength=dblCurLength;
        }
    }

    public void MoveSystem() {           // Moves system depending on vector field
        m_iEnergyIndex++;

//     // Test ob Graph schwingt 
//     if ( (cool_max_force_4 != 0.0) &&
//          (fabs((cool_max_force_1 - cool_max_force_3) / cool_max_force_1) < springembedder_rf_settings.vibration) &&
//          (fabs((cool_max_force_2 - cool_max_force_4) / cool_max_force_2) < springembedder_rf_settings.vibration) )
//     {  cool_phase = 2;
//     }
//     if ( cool_phase == 1 )
//     {  temperature = sqrt( max_force );
//     }
//     else 
//     {  // cool_phase 2 
//        temperature = sqrt( max_force ) / 15;
//     }

        if (m_dblMaxForceLength<=0)
            return;

        float dblFactor=(float)Math.sqrt((Math.sqrt(m_dblMaxForceLength)));   // TODO: do something with dblFActor
        if (dblFactor<=0)
            return;
        int iSize=m_arrPoints.length;

        for (int i=0; i<iSize; i++) {
            if (i==m_iDoNotMove)
                continue;
            float  abs_force = m_arrForceField[i].length();
            if (abs_force < dblFactor) {
                m_arrPoints[i].x+=m_arrForceField[i].x; 
                m_arrPoints[i].y+=m_arrForceField[i].y;
            } else {
                m_arrPoints[i].x+=m_arrForceField[i].x/abs_force*dblFactor; 
                m_arrPoints[i].y+=m_arrForceField[i].y/abs_force*dblFactor;
            }
        }
        PushEnergy(m_dblMaxForceLength);
    }

    public void UpdateSource() {
        // Update the source coordinates via InterfaceToObjects
        // TODO: calc energies here
        m_pInterface.SetObjectGeom(m_arrPoints/*, Terrain.createEnergy(m_arrPoints.length)*/);
    }

    public void InitFromInterface() {
        // Initialization(caching) of internal data from the interface to objects
        int[] arrObjIds = m_pInterface.GetAllObjectsIds();

        int n=arrObjIds.length;
        // Alloc memory first
        m_arrLinks = new SLink[n][];
        m_arrPoints = new Vector2f[n];
        for (int i=0; i<m_arrPoints.length; i++)
            m_arrPoints[i] = new Vector2f();
        //m_arrEnergy.SetSize(n);

        Vector2f pdDbl = new Vector2f();
        for (int i=0; i<n; i++) {
            //m_arrEnergy[i]=0;
            m_pInterface.GetObjectGeom(arrObjIds[i], pdDbl);
            m_arrPoints[i].x=pdDbl.x;
            m_arrPoints[i].y=pdDbl.y;
            FloatArray arrWeight = new FloatArray(); 
            IntArray arrAdjNodes = new IntArray(); 
            m_pInterface.GetAdjInfoFor(arrObjIds[i], arrAdjNodes, arrWeight);
            int nAdjNodesCouns=arrAdjNodes.getSize();
            m_arrLinks[i] = new SLink[nAdjNodesCouns];
            for (int j=0; j<nAdjNodesCouns; j++) {    // fill up adj nodes
                m_arrLinks[i][j] = new SLink();
                m_arrLinks[i][j].m_iToId=arrAdjNodes.get(j);
                m_arrLinks[i][j].m_Weight=arrWeight.get(j);
            }
        }
    }

    public QuadTreeT GetQuadTree() {
        return m_QTree;
    }

    public class SLink {
        int m_iToId;
        float m_Weight;
    }
}
