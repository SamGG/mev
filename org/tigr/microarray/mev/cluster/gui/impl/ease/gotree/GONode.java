/*
Copyright @ 1999-2006, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * GONode.java
 *
 * Created on August 11, 2004, 10:33 AM
 */

package org.tigr.microarray.mev.cluster.gui.impl.ease.gotree;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.Point2D;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.text.DecimalFormat;
import java.util.Vector;

import org.tigr.microarray.mev.cluster.gui.helpers.ktree.ITreeNode;
import org.tigr.microarray.mev.cluster.gui.helpers.ktree.ITreeNodeRenderer;
import org.tigr.microarray.mev.cluster.gui.helpers.ktree.KNodeImpl;

/**
 *
 * @author  braisted
 */
public class GONode extends KNodeImpl implements ITreeNodeRenderer {
    /** GO ID
     */
    private String goID;
    /** GO Term
     */
    private String goTerm;
    /** GO Category
     */
    private String goCategory;
    /** p-value
     */
    private double pValue;
    /** number of list hits
     */
    private int listHits;
    /** number of population hits
     */
    private int listSize;
    /** list size
     */
    private int popHits;
    /** population size
     */
    private int popSize;
    /** lower p-value threshold
     */
    private double lowerThr;
    /** upper p-value threshold
     */
    private double upperThr;
    /** cluster index
     */
    private int clusterIndex = -1;
    
    
    //Rendering fields
    
    private int renderingHint;
    private int VERBOSE_HEIGHT = 130;
    private int VERBOSE_WIDTH = 125;
    private int NON_VERBOSE_HEIGHT = 15;
    private int NON_VERBOSE_WIDTH = 15;
    
    private int GO_ID_HEIGHT = 20;
    private int GO_TERM_HEIGHT = 70;
    private int GO_STAT_HEIGHT = 20;
    private int GO_POP_HEIGHT = 20;
    
    public void setParents(ITreeNode[] p){
    	Vector v = new Vector(p.length);
    	for(int i=0; i<p.length; i++){
    		v.add(p[i]);
    	}
    	this.setParents(v);
    }
    public void setChildren(ITreeNode[] c){
    	Vector v = new Vector(c.length);
    	for(int i=0; i<c.length; i++){
    		v.add(c[i]);
    	}
    	this.setChildren(v);
    }
    public GONode(){super();}
    public GONode(String goID, String goTerm, String goCategory, double pValue, int clusterHits, int clusterSize,
    int popHits, int popSize, int clusterIndex) {
        super();
        this.goID = goID;
        this.goTerm = goTerm;
        this.goCategory = goCategory;
        this.pValue = pValue;
        this.listHits = clusterHits;
        this.listSize = clusterSize;
        this.popHits = popHits;
        this.popSize = popSize;
        this.clusterIndex = clusterIndex;
    }
    
    public GONode(GONode origNode) {
        super();
        this.goID = origNode.getGOID();
        this.goTerm = origNode.getTerm();
        this.goCategory = origNode.getCategory();
        this.pValue = origNode.getPValue();
        this.listHits = origNode.getListHits();
        this.listSize = origNode.getListSize();
        this.popHits = origNode.getPopHits();
        this.popSize = origNode.getPopSize();        
        this.upperThr = origNode.getUpperThr();
        this.lowerThr = origNode.getLowerThr();
        this.children = origNode.getChildren();
        this.parents = origNode.getParents();
        this.level = origNode.getLevel();
        this.verboseRendering = origNode.isVerboseRendering();
        this.clusterIndex = origNode.getClusterIndex();
    }
    
    public String getTerm() {
        return goTerm;
    }
    
    public String getGOID() {
        return goID;
    }
    
    public double getPValue() {
        return pValue;
    }
    
    public String getCategory() {
        return this.goCategory;
    }
    
    public int getListHits() {
        return listHits;
    }
    
    public int getListSize() {
        return listSize;
    }
    
    public int getPopHits() {
        return popHits;
    }
    
    public int getPopSize() {
        return popSize;
    }
    
    public void setTerm(String term) {
        this.goTerm = term;
    }
    
    public void setListHits(int hits) {
        this.listHits = hits;
    }
 
    public void setListSize(int size) {
        this.listSize = size;
    }
    
    public void setPopHits(int hits) {
        this.popHits = hits;
    }
    
    public void setPopSize(int size) {
        this.popSize = size;
    }
    
    public void setLowerThr(double lower) {
       lowerThr = lower;
    }
    
    public void setUpperThr(double upper) {
        upperThr = upper;
    }
    
    public double getLowerThr() {
        return lowerThr;
    }
    
    public double getUpperThr() {
        return upperThr;
    }
    
    public int getClusterIndex() {
        return clusterIndex;
    }

    public void addChild(GONode node) {
        super.addChild(node);
    }
    
    public void addParent(GONode node) {
        super.addParent(node);
    }

    
    //Rendering methods
    
    public int getToolTipHeight() {
    	return VERBOSE_HEIGHT;
    }

    public int getToolTipWidth() {
    	return VERBOSE_WIDTH;
    }

    /**
     * Sets the rendering hint
     */
    public void setRenderingHint(int hint) {
        renderingHint = hint;
        setDimensions();
    }
    
    /**
     * Renderer menthod to display a GONode
     */
    public void renderNode(Graphics2D g2, int x, int y, int modifier) {
        Color origColor = g2.getColor();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Composite composite = g2.getComposite();
        
        if(modifier == ITreeNodeRenderer.NON_PATH_NODE) {
            AlphaComposite alphaComp = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f);
            g2.setComposite(alphaComp);
        }
        
        int yOffset = 0;
            this.x = x;
            this.y = y;
            
        if(verboseRendering) {
            this.x = x;
            this.y = y;
            
            //draw 2 pixel border
            g2.drawRect(x,y,w,h);
            yOffset += 1;
            g2.drawRect(x+1, y+yOffset, w-2, h-2);

            //render go id area according to pValue
            if(pValue > upperThr)
                g2.setColor(Color.green);
            else if(pValue <= upperThr && pValue > lowerThr)
                g2.setColor(Color.orange);
            else
                g2.setColor(Color.red); 
            
            yOffset += 1;
            g2.fillRect(x+2, y+yOffset, w-3, GO_ID_HEIGHT);
            yOffset += GO_ID_HEIGHT;
            
            //reset color
            g2.setColor(origColor);
            
            //render go id text
            TextLayout layout = new TextLayout(goID, g2.getFont(), g2.getFontRenderContext());
            layout.draw(g2, x + (w - layout.getAdvance())/2, y + layout.getAscent() + (this.GO_ID_HEIGHT - layout.getAscent())/2);
            
            //render go id divider
            g2.drawLine(x, y + yOffset, x+w, y + yOffset);
            
            //render go term text
            int yLoc = y + yOffset +2;
            int marginLoc = x + 6;
            renderGoTermText(g2, marginLoc, yLoc, x, y);
            
            //render go term divider
            g2.drawLine(x, y + GO_TERM_HEIGHT + yOffset, x+w, y + GO_TERM_HEIGHT + yOffset);
            yOffset += GO_TERM_HEIGHT;
            
            //render p-value
            //renderPVaue(g2, yOffset);
            DecimalFormat format;
            
            String pStr;
            if (pValue < 0.0001){    
                format = new DecimalFormat("0.#####E00");
                pStr = format.format(pValue);
            } else {
                format = new DecimalFormat("0.#####");
                pStr = format.format(pValue);
            }
                
            layout = new TextLayout("p = "+pStr, g2.getFont(), g2.getFontRenderContext());
            layout.draw(g2, x + (w - layout.getAdvance())/2, y + yOffset + layout.getAscent() + (this.GO_STAT_HEIGHT - layout.getAscent())/2);
                   
            g2.drawLine(x, y + GO_STAT_HEIGHT + yOffset, x+w, y + GO_STAT_HEIGHT + yOffset);
            yOffset += GO_STAT_HEIGHT;
            
            layout = new TextLayout("("+String.valueOf(listHits)+")   ("+String.valueOf(popHits)+")", g2.getFont(), g2.getFontRenderContext());
            layout.draw(g2, x + (w - layout.getAdvance())/2, y + yOffset + layout.getAscent() + (this.GO_POP_HEIGHT - layout.getAscent())/2 - 2);
     
            if(modifier == ITreeNodeRenderer.SELECTED_NODE) {
                g2.setColor(Color.red);
                g2.drawRect(x-3, y-3, w+6, h+6);
                g2.drawRect(x-2, y-2, w+4, h+4);
                g2.setColor(origColor);
            } else if(modifier == ITreeNodeRenderer.PATH_NODE) {
                g2.setColor(Color.blue);
                g2.drawRect(x-3, y-3, w+6, h+6);
                g2.drawRect(x-2, y-2, w+4, h+4);                
                g2.setColor(origColor);
            }
            
        } else {
                    
            //render go id area according to pValue
            if(pValue > upperThr)
                g2.setColor(Color.green);
            else if(pValue <= upperThr && pValue > lowerThr)
                g2.setColor(Color.orange);
            else
                g2.setColor(Color.red); 

            g2.fillOval(x,y,w,h);
            g2.setColor(Color.black);
            g2.drawOval(x,y,w,h);
            g2.setColor(origColor);
            
            if(modifier == ITreeNodeRenderer.SELECTED_NODE) {
                g2.setColor(Color.red);
                g2.drawOval(x-3, y-3, w+6, h+6);
                g2.drawOval(x-2, y-2, w+4, h+4);   
                g2.setColor(origColor);
            } else if(modifier == ITreeNodeRenderer.PATH_NODE) {
                g2.setColor(Color.blue);
                g2.drawOval(x-3, y-3, w+6, h+6);
                g2.drawOval(x-2, y-2, w+4, h+4);                
                g2.setColor(origColor);
            }
        }
        g2.setComposite(composite);        
    }
    
    /**
     * 10/9/06 jcb
     * Render verbose tool tip.  This is called when node style is minimal
     * and the node is under the cursor.
     */
    public void renderVerboseTip(Graphics2D g2) {
    	
    	int x = this.x + this.NON_VERBOSE_WIDTH/2;
    	int y = this.y + this.NON_VERBOSE_HEIGHT/2;
    	
    	renderingHint = ITreeNodeRenderer.RENDERING_HINT_VERBOSE;
    	setDimensions();
    	
    	Color origColor = g2.getColor();
    	Composite origComposite = g2.getComposite();

        AlphaComposite alphaComp = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8f);
        g2.setComposite(alphaComp);

        //fill the center rectangle with a transparent overlay        
        g2.setColor(Color.white);
        g2.fillRect(x+2,y+2,w-4,h-4);
        g2.setColor(origColor);
        
    	int yOffset = 0;

    	if(pValue > upperThr)
            g2.setColor(Color.green);
        else if(pValue <= upperThr && pValue > lowerThr)
            g2.setColor(Color.orange);
        else
            g2.setColor(Color.red); 
        
        
        yOffset += 2;
        g2.fillRect(x+2, y+yOffset, w-3, GO_ID_HEIGHT);

        //reset composite to render lines and text darker
        g2.setComposite(origComposite);
        
        //reset color
        g2.setColor(origColor);
        
        yOffset = 0;
        //draw 2 pixel border
        g2.drawRect(x,y,w,h);
        yOffset += 1;
        g2.drawRect(x+1, y+yOffset, w-2, h-2);
        
        //set it below the two pixel line
        yOffset += 1;
        
        //go below the go id area
        yOffset += GO_ID_HEIGHT;

        
        //render go id text
        TextLayout layout = new TextLayout(goID, g2.getFont(), g2.getFontRenderContext());
        layout.draw(g2, x + (w - layout.getAdvance())/2, y + layout.getAscent() + (this.GO_ID_HEIGHT - layout.getAscent())/2);
        
        //render go id divider
        g2.drawLine(x, y + yOffset, x+w, y + yOffset);
        
        //render go term text
        int yLoc = y + yOffset +2;
        int marginLoc = x + 6;
        renderGoTermText(g2, marginLoc, yLoc, x, y);
        
        //render go term divider
        g2.drawLine(x, y + GO_TERM_HEIGHT + yOffset, x+w, y + GO_TERM_HEIGHT + yOffset);
        yOffset += GO_TERM_HEIGHT;
        
        //render pValue
    	//renderPVaue(g2, yOffset);
    	DecimalFormat format;
        
        String pStr;
        if (pValue < 0.0001){    
            format = new DecimalFormat("0.#####E00");
            pStr = format.format(pValue);
        } else {
            format = new DecimalFormat("0.#####");
            pStr = format.format(pValue);
        }
            
        layout = new TextLayout("p = "+pStr, g2.getFont(), g2.getFontRenderContext());
        layout.draw(g2, x + (w - layout.getAdvance())/2, y + yOffset + layout.getAscent() + (this.GO_STAT_HEIGHT - layout.getAscent())/2);
       
    	
    	//layout = new TextLayout("p = "+pStr, g2.getFont(), g2.getFontRenderContext());
    	//layout.draw(g2, x + (w - layout.getAdvance())/2, y + yOffset + layout.getAscent() + (this.GO_STAT_HEIGHT - layout.getAscent())/2);
    	
    	g2.drawLine(x, y + GO_STAT_HEIGHT + yOffset, x+w, y + GO_STAT_HEIGHT + yOffset);
    	yOffset += GO_STAT_HEIGHT;            
        
        layout = new TextLayout("("+String.valueOf(listHits)+")   ("+String.valueOf(popHits)+")", g2.getFont(), g2.getFontRenderContext());            	       
        layout.draw(g2, x + (w - layout.getAdvance())/2, y + yOffset + layout.getAscent() + (this.GO_POP_HEIGHT - layout.getAscent())/2 - 2);
        
    	renderingHint = ITreeNodeRenderer.RENDERING_HINT_MINIMAL;
    	setDimensions();
    }
    
    private void renderPVaue(Graphics2D g2, int yOffset) {
        DecimalFormat format = new DecimalFormat();
        format.setMaximumFractionDigits(5);
        format.setMaximumIntegerDigits(1);
        format.setMinimumIntegerDigits(1);
        format.setMinimumFractionDigits(4);
        String pStr = format.format(pValue);
        TextLayout layout = new TextLayout("p = "+pStr, g2.getFont(), g2.getFontRenderContext());
        layout.draw(g2, x + (w - layout.getAdvance())/2, y + yOffset + layout.getAscent() + (this.GO_STAT_HEIGHT - layout.getAscent())/2);
    }
    
    
    /**
     * Modified 10/9/06
     * 
     * @param g2 Graphics object
     * @param marginLoc upper break in go term box
     * @param yLoc Y location of text
     * @param x  upper left x of the text box.
     * @param y  upper left y of the text box.
     */
    private void renderGoTermText(Graphics2D g2, int marginLoc, int yLoc, int x, int y) {
    	Rectangle origClip = g2.getClipBounds();
    	
    	//set appropriate clip size in case text is too long
    	Rectangle newClip = new Rectangle(x, yLoc, x+w < origClip.x+origClip.width ? w : origClip.x+origClip.width-x, 
    	yLoc + this.GO_TERM_HEIGHT < origClip.y+origClip.height ? this.GO_TERM_HEIGHT : origClip.y+origClip.height-yLoc);

    	if(!origClip.intersects(newClip)) 
    		return;

    	// avoid clip for text entering the header
    	if(newClip.y < origClip.y)
    		newClip.y = origClip.y;
    
    	g2.setClip(newClip);    	

    	TextLayout layout;

        AttributedString goTermAttStr = new AttributedString(goTerm);
        goTermAttStr.addAttribute(TextAttribute.JUSTIFICATION, TextAttribute.JUSTIFICATION_NONE);
        goTermAttStr.addAttribute(TextAttribute.FONT, new Font("Arial", Font.PLAIN, 12));
  
        int width = g2.getFontMetrics().stringWidth(goTerm);
        if(width > 4 * w)
            goTermAttStr.addAttribute(TextAttribute.FONT, new Font("Arial", Font.PLAIN, 11));
        
        AttributedCharacterIterator iter = goTermAttStr.getIterator();
        FontRenderContext frc = new FontRenderContext(null, true, true);
        LineBreakMeasurer lbm = new LineBreakMeasurer(iter, frc);
        
        Vector layouts = new Vector();
        Vector penPos = new Vector();
        Point2D.Float penPoint;
        while(lbm.getPosition() < iter.getEndIndex()) {
            layout = lbm.nextLayout(w - 2*5);
            
            yLoc += layout.getAscent();
                        
            penPoint = new Point2D.Float(marginLoc, yLoc);
            
            if(layouts.size() > 0) {
                TextLayout prevLine = (TextLayout)layouts.elementAt(layouts.size()-1);                
                prevLine = prevLine.getJustifiedLayout((w-2*5));
                layouts.setElementAt(prevLine, layouts.size()-1);
            }
            
            penPos.addElement(penPoint);
            layouts.addElement(layout);
            yLoc += layout.getDescent() + layout.getLeading();
        }
        
        //have layouts and pen positions
        for(int i = 0; i < layouts.size(); i++) {
            layout = (TextLayout)(layouts.elementAt(i));
            penPoint = (Point2D.Float)(penPos.elementAt(i));
            layout.draw(g2, penPoint.x, penPoint.y);
        }
        
        g2.setClip(origClip);
    }


    
    private void setDimensions() {
        if(renderingHint == ITreeNodeRenderer.RENDERING_HINT_VERBOSE) {
            w = VERBOSE_WIDTH;
            h = VERBOSE_HEIGHT;
            verboseRendering = true;
        } else {
            w = NON_VERBOSE_WIDTH;
            h = NON_VERBOSE_HEIGHT;
            verboseRendering = false;
        }
    }
    

	/**
	 * @return Returns the goCategory.
	 */
	public String getGoCategory() {
		return goCategory;
	}
	/**
	 * @param goCategory The goCategory to set.
	 */
	public void setGoCategory(String goCategory) {
		this.goCategory = goCategory;
	}
	/**
	 * @return Returns the goID.
	 */
	public String getGoID() {
		return goID;
	}
	/**
	 * @param goID The goID to set.
	 */
	public void setGoID(String goID) {
		this.goID = goID;
	}
	/**
	 * @return Returns the goTerm.
	 */
	public String getGoTerm() {
		return goTerm;
	}
	/**
	 * @param goTerm The goTerm to set.
	 */
	public void setGoTerm(String goTerm) {
		this.goTerm = goTerm;
	}
	/**
	 * @param clusterIndex The clusterIndex to set.
	 */
	public void setClusterIndex(int clusterIndex) {
		this.clusterIndex = clusterIndex;
	}
	/**
	 * @param value The pValue to set.
	 */
	public void setPValue(double value) {
		pValue = value;
	}
}
