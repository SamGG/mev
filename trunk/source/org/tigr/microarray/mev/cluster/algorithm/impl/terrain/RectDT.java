/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: RectDT.java,v $
 * $Revision: 1.3 $
 * $Date: 2005-03-10 15:45:30 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.algorithm.impl.terrain;

import javax.vecmath.Vector2f;

public class RectDT {
    
    public float m_Left;
    public float m_Top;
    public float m_Right;
    public float m_Bottom;

    public RectDT() {
    }

    public RectDT(RectDT rIn) {
        set(rIn);
    }

    public void set(RectDT rIn) {
        m_Left=rIn.m_Left;
        m_Top=rIn.m_Top;
        m_Right=rIn.m_Right;
        m_Bottom=rIn.m_Bottom;
    }

    public float Width() {
        return m_Right-m_Left;
    }

    public float Height() {
        return m_Bottom-m_Top;
    }

    public void NormalizeRect() {
        if (m_Right<m_Left) {
            float Tmp=m_Right;
            m_Right=m_Left;
            m_Left=Tmp;
        }
        if (m_Bottom<m_Top) {
            float Tmp=m_Bottom;
            m_Bottom=m_Top;
            m_Top=Tmp;
        }
    }

    public void SetRect(Vector2f ptDLeftTop, Vector2f ptDRightBottom) {
        m_Left=ptDLeftTop.x;
        m_Top=ptDLeftTop.y;
        m_Right=ptDRightBottom.x;
        m_Bottom=ptDRightBottom.y;
    }

    public void MakeEmpty() {
        m_Left = Float.MAX_VALUE; m_Top=Float.MAX_VALUE; m_Right=-Float.MAX_VALUE; m_Bottom=-Float.MAX_VALUE;
    }

    public void IncludePoint(Vector2f rPt) {
        if (m_Left>rPt.x)
            m_Left=rPt.x;
        if (m_Right<rPt.x)
            m_Right=rPt.x;
        if (m_Top>rPt.y)
            m_Top=rPt.y;
        if (m_Bottom<rPt.y)
            m_Bottom=rPt.y;
    }

    public boolean PtInRect(Vector2f rIn) {
        return m_Left<=rIn.x && m_Right>=rIn.x && m_Top<=rIn.y && m_Bottom>=rIn.y;
    }

    public static float _hypot(float x, float y) {
        return(float)Math.sqrt(x*x + y*y);
    }

    public float Distance(Vector2f rIn) {
        if (rIn.y<m_Top) {
            if (rIn.x<m_Left)
                return(float)_hypot(rIn.x-m_Left, rIn.y-m_Top);
            if (rIn.x>m_Right)
                return(float)_hypot(rIn.x-m_Right, rIn.y-m_Top);
            return m_Top-rIn.y;
        }
        if (rIn.y>m_Bottom) {
            if (rIn.x<m_Left)
                return(float)_hypot(rIn.x-m_Left, rIn.y-m_Bottom);
            if (rIn.x>m_Right)
                return(float)_hypot(rIn.x-m_Right, rIn.y-m_Bottom);
            return rIn.y-m_Bottom;
        } else {
            if (rIn.x<=m_Left)
                return m_Left-rIn.x;
            if (rIn.x>=m_Right)
                return rIn.x-m_Right;
        }
        return 0f;
    }
}
