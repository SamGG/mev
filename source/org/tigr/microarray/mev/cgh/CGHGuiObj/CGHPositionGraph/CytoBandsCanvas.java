/*
 * CytoBandsPanel.java
 *
 * Created on March 18, 2003, 7:02 PM
 */

package org.tigr.microarray.mev.cgh.CGHGuiObj.CGHPositionGraph;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.geom.RoundRectangle2D;

import org.tigr.microarray.mev.cgh.CGHDataModel.CytoBandsModel;
import org.tigr.microarray.mev.cgh.CGHDataObj.CytoBand;

/**
 *
 * @author  Adam Margolin
 * @author Raktim Sinha
 */

public class CytoBandsCanvas extends javax.swing.JPanel {

    private Insets insets;

    private double unitLength;
    int rectWidth = 80;

    int origXOffset = 5;
    int origYOffset = 5;

    int rectSpacing = 5;

    int cytoBandWidth = 60;

    int fontSize = 12;

    CytoBandsModel model;

    int chromosomeIndex = 0;

    int rulerOffset;
    int cytoBandsOffset;

    public CytoBandsCanvas(Insets insets){
        //this.insets = new Insets(insets.top, 0, insets.bottom, 0);
        this.insets = insets;
        rulerOffset = insets.left + 20;
        cytoBandsOffset = rulerOffset + 10;

        initComponents();
    }

    private void initComponents(){
        setBackground(Color.black);
        //setPreferredSize(new Dimension(cytoBandWidth + insets.left + insets.right, 400));
        //setMaximumSize(new Dimension(cytoBandWidth + insets.left + insets.right, 10000));
        setPreferredSize(new Dimension(cytoBandWidth + cytoBandsOffset + insets.right, 400));
        setMaximumSize(new Dimension(cytoBandWidth + cytoBandsOffset + insets.right, 10000));
    }


    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2 = (Graphics2D)g;

        Dimension d = getSize();

        double width = d.width;
        double height = d.height;
        //Draw the ruler on the side
        g2.setPaint(Color.white);

        //int rulerX = (int) origXOffset - 15;


        g2.drawLine(rulerOffset, insets.top, rulerOffset, (int)height);
        //g2.setFont(new Font("", 0, 12));
        //g2.drawString("Position x 1,000,000", 5f, 15f);
        g2.setFont(new Font("", 0, 10));
        for(int i = 0; i < model.getMaxPosition(); i+= 10000000){
            String pos = i + "";
            String trimmedPos;
            if(pos.length() < 6){
                trimmedPos = pos;
            }else{
                trimmedPos = pos.substring(0, pos.length() - 6);
            }
            g2.drawString(trimmedPos, insets.left, (int) (insets.top + i * unitLength));
        }


        CytoBand curCytoBand;
        //Rectangle curRect;
        RoundRectangle2D curRect;
        for(int bandIndex = 0; bandIndex < model.getNumCytoBands(); bandIndex++){
            curCytoBand =  model.getCytoBandAt(bandIndex);
            int bandStart = curCytoBand.getChromStart();
            int bandStop = curCytoBand.getChromEnd();

            int rectY = (int) (insets.top + bandStart * unitLength);
            double dRectHeight = (bandStop - bandStart) * unitLength;
            int rectHeight;
            if(dRectHeight < 1 && dRectHeight > 0){
                rectHeight = 1;
            }else{
                rectHeight = (int)dRectHeight;
            }

            //curRect = new Rectangle(insets.left, rectY, cytoBandWidth, rectHeight);
            cytoBandsOffset = rulerOffset + 5;
            //curRect = new RoundRectangle2D.Float((float)insets.left, (float) rectY, (float) cytoBandWidth, (float) rectHeight,
            //    5f,5f);
            curRect = new RoundRectangle2D.Float(cytoBandsOffset, (float) rectY, (float) cytoBandWidth, (float) rectHeight,
                5f,5f);

            Color curCytoColor = model.getDataPointColor(bandIndex);
            g2.setPaint(curCytoColor);
            g2.fill(curRect);

            if(curCytoColor == Color.darkGray){
                g2.setColor(Color.white);
            }else{
                g2.setColor(Color.black);
            }

            int fontSize = 10;
            if(rectHeight < fontSize + 1){
                fontSize = rectHeight - 1;
            }

            int stringY = rectY + (rectHeight / 2) + (fontSize / 2);

            g2.setFont(new Font("", 0, fontSize));
            //g2.drawString(curCytoBand.getName(), insets.left, stringY);
            g2.drawString(curCytoBand.getName(), cytoBandsOffset, stringY);

        }

        curCytoBand = null;
    }

    /** Getter for property model.
     * @return Value of property model.
     */
    public CytoBandsModel getModel() {
        return model;
    }

    /** Setter for property model.
     * @param model New value of property model.
     */
    public void setModel(CytoBandsModel model) {
        this.model = model;
    }

    /** Getter for property chromosomeIndex.
     * @return Value of property chromosomeIndex.
     */
    public int getChromosomeIndex() {
        return chromosomeIndex;
    }

    /** Setter for property chromosomeIndex.
     * @param chromosomeIndex New value of property chromosomeIndex.
     */
    public void setChromosomeIndex(int chromosomeIndex) {
        this.chromosomeIndex = chromosomeIndex;
    }

    public void onSelected(){
        model.setChromosomeIndex(chromosomeIndex);
    }

    /** Getter for property unitLength.
     * @return Value of property unitLength.
     */
    public double getUnitLength() {
        return unitLength;
    }

    /** Setter for property unitLength.
     * @param unitLength New value of property unitLength.
     */
    public void setUnitLength(double unitLength) {
        this.unitLength = unitLength;
    }

}