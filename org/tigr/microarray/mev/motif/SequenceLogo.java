/*
Copyright @ 1999-2005, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: SequenceLogo.java,v $
 * $Revision: 1.2 $
 * $Date: 2005-02-24 20:23:50 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.microarray.mev.motif;

import java.awt.*;
import java.awt.geom.*;
import java.awt.font.*;

public class SequenceLogo {
    int[][] Matrix;
    public int NumberOfBases;
    
    public SequenceLogo(int[][] matrix) {
	Matrix=matrix;
	NumberOfBases=Matrix[0].length;
    }
    
    public void Paint(Graphics2D g2, int x, int y, int Width, int Height, int Start, boolean DrawYAxis) {
	double ScaleX=1;
	double ScaleY=1;
	double BaseY;
	double H;
	double R;
	double Sum;
	double BaseHeightFactor=4;
	double BaseWidth=0;
	double BaseHeight=0;
	int Minimum;
	int MaxIndex=0;
	boolean ElementFound=false;
	float[] PreviousBase=new float[NumberOfBases];
	String Base="";
	String Bases="ACGT";
	Rectangle2D Bounds;
	TextLayout Layout;
	Font LogoFont = new Font("serif",Font.BOLD, 400);
	g2.setFont(LogoFont);
	AffineTransform OldTransform=g2.getTransform();
	FontRenderContext frc = g2.getFontRenderContext();
	for (int k=0; k<4; k++) {
	    for (int i=0; i<NumberOfBases; i++) {
		g2.setTransform(OldTransform);
		Minimum=Integer.MAX_VALUE;
		H=0;
		Sum=0;
		for (int j=0; j<4; j++) Sum+=Math.abs(Matrix[j][i]);
		ElementFound=false;
		for (int j=0; j<4; j++) {
		    
		    if ((Matrix[j][i]>0) && (Matrix[j][i]<=Minimum)) {
			Minimum=Matrix[j][i];
			Base=Bases.substring(j,j+1);
			MaxIndex=j;
			BaseHeightFactor=Minimum*4;
			ElementFound=true;
		    }
		    if (Matrix[j][i]!=0) H+=((float)Math.abs(Matrix[j][i]/(double)Sum))*Math.log((float)Math.abs(Matrix[j][i]/(double)Sum))/Math.log(2);
		}
		H=-H;
		R=2-H;
		double Test=0.5;
		BaseHeightFactor=(float)Math.abs(Matrix[MaxIndex][i]/(double)Sum)*R;
		BaseHeightFactor*=0.5;  // Maximum is 2 bit NOT 1 !!
		if (Matrix[MaxIndex][i]>0) Matrix[MaxIndex][i]=-Matrix[MaxIndex][i];
		if (!ElementFound) continue;
		if (Base.compareTo("A")==0) g2.setColor(Color.green);
		if (Base.compareTo("C")==0) g2.setColor(Color.blue);
		if (Base.compareTo("G")==0) g2.setColor(Color.orange);
		if (Base.compareTo("T")==0) g2.setColor(Color.red);
		Layout = new TextLayout(Base,LogoFont, g2.getFontRenderContext());
		Bounds = Layout.getBounds();
		BaseY=y+Height-((int)Bounds.getHeight()+(int)Bounds.getY())*BaseHeightFactor;
		BaseHeight=(int)Bounds.getHeight();
		BaseWidth=(int)Bounds.getWidth();
		ScaleX=(Width)/((BaseWidth)*NumberOfBases);
		ScaleY=(Height*BaseHeightFactor)/BaseHeight;
		g2.scale(ScaleX,ScaleY);
		Layout.draw(g2,(float) (((x+1)/ScaleX-Bounds.getX()+i*BaseWidth)),(float) (BaseY/ScaleY-PreviousBase[i]/ScaleY));
		PreviousBase[i]+=(float)((BaseHeight)*ScaleY);
	    }
	}
	for (int i=0; i<NumberOfBases; i++) {
	    for (int j=0; j<4; j++) {
		Matrix[j][i]=Math.abs(Matrix[j][i]);
	    }
	}
	g2.setTransform(OldTransform);
	BaseHeightFactor=0.9;
	Layout = new TextLayout(String.valueOf(Start),LogoFont, g2.getFontRenderContext());
	Bounds = Layout.getBounds();
	BaseWidth=(int)Bounds.getHeight();
	ScaleX=(Width)/((BaseWidth)*NumberOfBases);
	ScaleY=ScaleX;
	ScaleX*=BaseHeightFactor;
	g2.scale(ScaleX,ScaleY);
	g2.setColor(Color.black);
	g2.rotate(-Math.PI/2.0);
	for (int i=0; i<NumberOfBases; i++) {
	    Layout = new TextLayout(String.valueOf(i+Start),LogoFont, g2.getFontRenderContext());
	    Bounds = Layout.getBounds();
	    BaseHeight=(int)Bounds.getWidth()+BaseWidth*0.5+Bounds.getX();
	    Layout.draw(g2,(float)((-y-Height)/ScaleY-BaseHeight),(float)((x/ScaleX+(i+1)/BaseHeightFactor*BaseWidth)));
	}
	if (DrawYAxis) {
	    Layout = new TextLayout("bits",LogoFont, g2.getFontRenderContext());
	    Bounds = Layout.getBounds();
	    BaseHeight=(int)Bounds.getWidth()+BaseWidth*0.5+Bounds.getX();
	    Layout.draw(g2,(float)((-y-Height/2.0)/ScaleY-BaseHeight/2.0),(float)(x/ScaleX-BaseWidth*0.25));
	    g2.rotate(Math.PI/2.0);
	    Layout = new TextLayout("0",LogoFont, g2.getFontRenderContext());
	    Bounds = Layout.getBounds();
	    BaseHeight=(int)Bounds.getHeight();
	    Layout.draw(g2,(float)((x/ScaleX)-BaseWidth),(float)((y+Height)/ScaleY));
	    Layout = new TextLayout("2",LogoFont, g2.getFontRenderContext());
	    Layout.draw(g2,(float)((x/ScaleX)-BaseWidth),(float)((y)/ScaleY+BaseHeight));
	}
	g2.setTransform(OldTransform);
	g2.setColor(new Color(0,0,128));
	g2.drawRect(x-1,y,Width+2,Height);
    }
}