/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: GDMExpSpotInfoDisplay.java,v $
 * $Revision: 1.3 $
 * $Date: 2005-03-10 20:22:00 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.gdm;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JViewport;
import javax.swing.ScrollPaneConstants;

import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.impl.GUIFactory;
import org.tigr.util.FloatMatrix;
import org.tigr.util.awt.ActionInfoDialog;
import org.tigr.util.awt.GBA;

public class GDMExpSpotInfoDisplay extends ActionInfoDialog {
    
    private int col;
    private int row;
    private FloatMatrix expDistMatrix;
    private FloatMatrix rawMatrix;
    
    private int LINEAR = 0; // to return just ratio
    private int LOG = 1;   //for log2(ratio)
    private String distanceMetric;
    private int vectorSize=0;
    
    private int rowMissingValues=0;
    private int colMissingValues=0;
    private int distanceBasedOn=0;
    private IData expData;
    private Experiment experiment;
    private Color rowExperimentColor;
    private Color colExperimentColor;
    
    public static Color zeroColor = Color.black;
    public static Color NaNColor = Color.gray;
    public static Color diagColor = Color.white;
    
    private boolean haveColor = true;
    
    public GDMExpSpotInfoDisplay(JFrame parent, IData expData, FloatMatrix gdMatrix,
    FloatMatrix rawMatrix, String distMetric, int col, int row) {
        super(parent, true);
        this.expData = expData;
        this.experiment = expData.getExperiment();
        
        this.expDistMatrix = gdMatrix;
        this.rawMatrix = rawMatrix;
        this.col = col;
        this.row = row;
        this.distanceMetric = distMetric;
        
        this.vectorSize = this.expData.getFeaturesSize();
        
        this.colExperimentColor = expData.getExperimentColor(col);
        this.rowExperimentColor = expData.getExperimentColor(row);
        
        if(colExperimentColor == null && rowExperimentColor == null){
            haveColor = false;
        } else {
            if(this.rowExperimentColor == null)
                this.rowExperimentColor = new Color(Integer.parseInt("FFFFCC",16));
            if(this.colExperimentColor == null)
                this.colExperimentColor = new Color(Integer.parseInt("FFFFCC",16));
        }
        this.setValues(col, row);
        init();
    }
    
    private void init(){
        
        Font infoDisplayFont = new Font("monospaced", Font.PLAIN, 10);
        JLabel spotImage = new JLabel(GUIFactory.getIcon("spot.gif"));
        EventListener listener = new EventListener();
        
        JTextPane infoDisplayTextPane = new JTextPane();
        infoDisplayTextPane.setContentType("text/html");
        infoDisplayTextPane.setFont(infoDisplayFont);
        infoDisplayTextPane.setEditable(false);
        infoDisplayTextPane.setBackground(new Color(Integer.parseInt("FFFFCC",16)));
        infoDisplayTextPane.setMargin(new Insets(10,15,10,10));
        infoDisplayTextPane.setText(createMessage());
        infoDisplayTextPane.setCaretPosition(0);
        infoDisplayTextPane.addKeyListener(listener);
        JButton closeButton = new JButton("Close");
        closeButton.setActionCommand("close");
        closeButton.addActionListener(listener);
        
        contentPane.setLayout(new GridBagLayout());
        JScrollPane scrollPane = new JScrollPane(infoDisplayTextPane, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getViewport().setBackground(Color.white);
        scrollPane.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
        scrollPane.getVerticalScrollBar().setValues(10, 10, 0, 100);
        
        GBA gba = new GBA();
        gba.add(contentPane, scrollPane, 0, 0, 3, 2, 1, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
        gba.add(contentPane, closeButton, 0, 3, 1, 1, 0, 0, GBA.NONE, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
        gba.add(contentPane, spotImage, 2, 3, 1, 1, 0, 0, GBA.NONE, GBA.E, new Insets(5, 5, 5, 5), 0, 0);
        pack();
        setSize(500, 500);
        setResizable(true);
        setTitle("Sample Distance Spot Information");
        setLocation(300, 100);
        show();
    }
    
    private String createMessage() {
        int stringLength = 0;
        
        String rowName = this.expData.getSampleName(this.row);
        String colName = this.expData.getSampleName(this.col);
        
        int displayRowNum = this.row + 1;
        int displayColNum = this.col + 1;
        
        String colColor= "";
        String rowColor= "";
        if(haveColor){
           colColor = '#' + (Integer.toHexString(colExperimentColor.getRGB())).substring(2, 8);
           rowColor = '#' + (Integer.toHexString(rowExperimentColor.getRGB())).substring(2, 8);
        }
        String message = "<h3>Experiment Information</h3>" +
        "<table border=2 cellpadding=4 valign=top >" +
        "<tr><td valign=top></td><td  valign=top><b>Column Experiment</b></td><td  valign=top><b>Row Experiment</b></td></tr>" +
        "<tr><td  valign=top><b>Name</b></td><td  valign=top>" + colName + "</td><td  valign=top>" + rowName + "</td></tr>";
        if(haveColor)
            message += "<tr><td  valign=top><b>Experiment Color</b></td><td   valign=top bgcolor=" + colColor + "></td><td  valign=top bgcolor=" + rowColor + "></td></tr>";
        
        message += "</table>" +
        "<h3>Experiment Distance</h3>" +
        "<table border=2 cellpadding=4 valign=top >" +
        "<tr><td  valign=top><b>GDM Matrix Row </b></td><td width=34% valign=top>" + displayRowNum + "</td></tr>" +
        "<tr><td  valign=top><b>GDM Matrix Column </b></td><td width=34% valign=top>" + displayColNum + "</td></tr>" +
        "<tr><td  valign=top><b>Scaled Experiment Distance</b></td><td width=34% valign=top>" + expDistMatrix.get(col, row) + "</td></tr>" +
        "<tr><td  valign=top><b>Actual Experiment Distance</b></td><td width=34% valign=top>" + rawMatrix.get(col, row) + "</td></tr>" +
        "<tr><td  valign=top><b>Distance Metric</b></td><td width=34% valign=top>" + distanceMetric + "</td></tr>" +
        "<tr><td  valign=top><b>Vector Size</b></td><td width=34% valign=top>" + this.vectorSize + "</td></tr>" +
        "<tr><td  valign=top><b>Missing Values</b></td><td  valign=top>" + colMissingValues + ", " + rowMissingValues + "</td></tr>" +
        "<tr><td  valign=top><b>Distance Based on</b></td><td width=34% valign=top>" + this.distanceBasedOn+ "</td></tr>";
        
        return message;
    }
    
    private void setValues(int col, int row) {
        
        int rows = this.expData.getFeaturesSize();
        boolean [] rowMissingVector = new boolean[rows];
        boolean [] colMissingVector = new boolean[rows];
        
        for( int i=0; i< rows; i++) {
            rowMissingVector[i] = false;
            colMissingVector[i] = false;
        }
        
        for (int i = 0; i < rows; i++) {
            float value = experiment.get(i, row);
            if (Float.isNaN(value)) {
                rowMissingVector[i] = true;
                this.rowMissingValues ++;
            }
        }
        
        for (int i = 0; i < rows; i++) {
            float value = experiment.get(i, col);
            if (Float.isNaN(value)) {
                colMissingVector[i] = true;
                this.colMissingValues ++;
            }
        }
        
        for(int i =0; i<rows; i++) {
            if ((rowMissingVector[i] == false) && (colMissingVector[i] == false) && (row != col)) {
                this.distanceBasedOn++;
            }
        }
    }
    
    private void drawExperimentColor() {
        
        Graphics g = getGraphics();

        if (g != null) {
            g.setColor(rowExperimentColor);
            g.fillRect(0, 0, 10, 10);
            
            g.setColor(colExperimentColor);
            g.fillRect(20, 0, 10, 10);
        }
    }
    
    
    class EventListener implements ActionListener, KeyListener {
        
        public void actionPerformed(ActionEvent event) {
            String command = event.getActionCommand();
            if (command.equals("close")) {
                dispose();
            }
        }
        
        public void keyPressed(KeyEvent event) {
            if (event.getKeyCode() == KeyEvent.VK_ENTER) {
                dispose();
            }
        }
        
        public void keyReleased(KeyEvent event) {}
        public void keyTyped(KeyEvent event) {}
    }
}
