/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: QuadTreeT.java,v $
 * $Revision: 1.4 $
 * $Date: 2006-02-23 20:59:46 $
 * $Author: caliente $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.algorithm.impl.terrain;

import javax.vecmath.Vector2f;

import org.tigr.microarray.mev.cluster.algorithm.impl.util.IntArray;

public class QuadTreeT {

    public SNode[] m_arrNodes;
    private InterfaceToObjects m_rInterface;

    private final static int c_iMaxQuadTreeTDepth = 15;
    // helper methods
    private int GetLinearSize(int iDepth) {
        return(int)((Math.pow(4,iDepth)-1.0)/3.0+0.5);
    }

    private void Clear() {
        int n=m_arrNodes.length;
        for (int i=0; i<n; i++)
            m_arrNodes[i].Destroy();
    }

    private void SetNode(int iInd, int[] rArrIds) {
        //ASSERT(iInd>=0 && UINT(iInd)<m_arrNodes.size());
        if (rArrIds.length<=0)
            return;
        // calculate midpt, averagept, rectBounding
        int nSize=rArrIds.length;
        Vector2f ptSum = new Vector2f(), ptAvg = new Vector2f();
        Vector2f ptCur = new Vector2f();
        Vector2f ptTmp = new Vector2f();
        RectDT rectBounding = new RectDT();
        rectBounding.MakeEmpty();
        for (int i=0; i<nSize; i++) {
            m_rInterface.GetObjectGeom(rArrIds[i], ptTmp);
            ptCur.x=ptTmp.x;
            ptCur.y=ptTmp.y;
            rectBounding.IncludePoint(ptCur);
            ptSum.add(ptCur);
        }
        ptAvg.set(ptSum);
        ptAvg.scale(1f/(float)nSize); // ptAvg is average point now
        Vector2f midPoint = new Vector2f((rectBounding.m_Right+rectBounding.m_Left)/2, (rectBounding.m_Bottom+rectBounding.m_Top)/2);

        // instantiate current node
        m_arrNodes[iInd].m_ptMid.set(ptAvg);     //middle point(the point to divide the rect)
        m_arrNodes[iInd].m_ptAvg.set(ptAvg);         //average(center of mass) point... TODO: change midpoint to average point
        m_arrNodes[iInd].m_Rect.set(rectBounding);   // bounding of the objects
        m_arrNodes[iInd].m_iPointNumBehind=nSize;// the number of objects inside the rectangle(belonging to bounding rectangle)

        int iChildInd=GetChild(iInd, LEFT_UP);
        if (iChildInd<0||nSize==1) {  // if this is leaf by structure of QuadTreeT
            m_arrNodes[iInd].SetLeaf(); //invalidate middle point
            m_arrNodes[iInd].m_arrPointsIds=rArrIds;
            return;
        }

        //divide the set of nodes to the four subsets for each quadrant(around midPoint)
        IntArray arrLeftUpIds = new IntArray(), arrRightUpIds = new IntArray();
        IntArray arrLeftDownIds = new IntArray(), arrRightDownIds = new IntArray();
        for (int i=0; i<nSize; i++) {
            m_rInterface.GetObjectGeom(rArrIds[i], ptTmp);
            ptCur.set(ptTmp);
            
            if (ptCur.x<=midPoint.x) {                // On the left
                if (ptCur.y<=midPoint.y)
                    arrLeftUpIds.add(rArrIds[i]);           // Left-upper corner
                else
                    arrLeftDownIds.add(rArrIds[i]);     // Left-down corner
            } else {                                            // On the right
                if (ptCur.y<=midPoint.y)
                    arrRightUpIds.add(rArrIds[i]);      // Right-upper corner
                else
                    arrRightDownIds.add(rArrIds[i]);        // Right-down corner
            }
        }

        // call SetNode(int iInd, const vector<Vector2D>& rArrIn, const vector<int>& rArrIds) recursively for each quadrant
        //ASSERT(iChildInd>iInd && UINT(iChildInd)<m_arrNodes.size());
        SetNode(iChildInd, arrLeftUpIds.toArray());
        arrLeftUpIds = null;

        iChildInd=GetChild(iInd, RIGHT_UP);
        //ASSERT(iChildInd>iInd && UINT(iChildInd)<m_arrNodes.size());
        SetNode(iChildInd, arrRightUpIds.toArray());
        arrRightUpIds = null;

        iChildInd=GetChild(iInd, LEFT_DOWN);
        //ASSERT(iChildInd>iInd && UINT(iChildInd)<m_arrNodes.size());
        SetNode(iChildInd, arrLeftDownIds.toArray());
        arrLeftDownIds = null;

        iChildInd=GetChild(iInd, RIGHT_DOWN);
        //ASSERT(iChildInd>iInd && UINT(iChildInd)<m_arrNodes.size());
        SetNode(iChildInd, arrRightDownIds.toArray());
        arrRightDownIds = null;
    }

    public static class SNode {
        // properties
        Vector2f m_ptMid = new Vector2f();   // 'middle' point
        Vector2f m_ptAvg = new Vector2f();   // average point
        public RectDT m_Rect = new RectDT();      // m_ptMid==f(m_Rect)
        int m_iPointNumBehind;
        int[] m_arrPointsIds;

        // methods
        public SNode() {
            Destroy();
        }

        public void Destroy() {
            m_arrPointsIds = null; 
            m_ptMid.set(Float.MAX_VALUE, Float.MAX_VALUE);
            m_iPointNumBehind = 0;
        }
        public void Init(Vector2f rIn) {
            Destroy(); m_ptMid.set(rIn);
        }
        public void Init(int[] rIn) {
            Destroy(); m_arrPointsIds=rIn;
        }

        public boolean IsLeaf() {
            return m_ptMid.x == Float.MAX_VALUE && m_ptMid.y == Float.MAX_VALUE;
        }
        public void SetLeaf() {
            m_ptMid.set(Float.MAX_VALUE, Float.MAX_VALUE);
        }
    }


    // Construction
    QuadTreeT(int iDepth, InterfaceToObjects rInterface) {
        if (iDepth > c_iMaxQuadTreeTDepth)
            throw new IllegalArgumentException("The tree depth can't be more than "+c_iMaxQuadTreeTDepth);
        m_rInterface = rInterface;
        int iSize=GetLinearSize(iDepth);
        m_arrNodes = new SNode[iSize];
        for (int i=0; i<m_arrNodes.length; i++)
            m_arrNodes[i] = new SNode();
    }

    // Initialization
    void Initialize() {
        Clear();
        int[] arrObjIds = m_rInterface.GetAllObjectsIds();  //retrieve all obj ids from the interface to the objects
        SetNode(0, arrObjIds);
    }

    // Quad Tree navigation methods
    int GetParent(int iChild) {             // get the index of iChild's parent node in quad tree, or -1 if no any
        return(iChild>0)?((iChild >> 2 )):0;   //just divide to 4
    }
    int GetChild(int iChild, int eDir) {    // go down to appropriate direction from iChild node to eDir... returns -1 if such operation is not available
        int iNewInd=(iChild<<2)+eDir;
        if (iNewInd<0 || iNewInd>=m_arrNodes.length)
            iNewInd=-1;
        return iNewInd;
    }

    public static final int LEFT_UP=1, RIGHT_UP=2, LEFT_DOWN=3, RIGHT_DOWN=4;
}
