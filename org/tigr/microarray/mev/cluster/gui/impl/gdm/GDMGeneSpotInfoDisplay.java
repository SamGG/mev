/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * $RCSfile: GDMGeneSpotInfoDisplay.java,v $
 * $Revision: 1.2 $
 * $Date: 2005-02-24 20:23:46 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.gdm;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;

import org.tigr.graph.GC;
import org.tigr.graph.GraphLine;
import org.tigr.graph.GraphPoint;
import org.tigr.graph.GraphTick;
import org.tigr.graph.GraphViewer;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.impl.GUIFactory;
import org.tigr.util.FloatMatrix;
import org.tigr.util.awt.ActionInfoDialog;
import org.tigr.util.awt.GBA;

public class GDMGeneSpotInfoDisplay extends ActionInfoDialog {
    
    private int colIndex;
    private int rowIndex;
    private int colNumber;
    private int rowNumber;
    private FloatMatrix geneDistMatrix;
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
    private Color rowGeneColor;
    private Color columnGeneColor;
    
    public static Color zeroColor = Color.black;
    public static Color NaNColor = Color.gray;
    public static Color diagColor = Color.white;
    
    private boolean haveColor = true;
    FloatMatrix data;
    
    public GDMGeneSpotInfoDisplay(JFrame parent, Experiment experiment, IData expData, FloatMatrix gdMatrix,
    FloatMatrix rawMatrix, String distMetric, int colIndex, int rowIndex, int column, int row) {
        super(parent, false);
        this.expData = expData;
        this.experiment = experiment;
        data = this.experiment.getMatrix();
        
        this.geneDistMatrix = gdMatrix;
        this.rawMatrix = rawMatrix;
        this.colIndex = colIndex;
        this.rowIndex = rowIndex;
        
        this.colNumber = column;
        this.rowNumber = row;
        this.distanceMetric = distMetric;
        
        this.vectorSize = this.expData.getFeaturesCount();
        
        this.rowGeneColor = expData.getProbeColor(experiment.getGeneIndexMappedToData(rowIndex));
        this.columnGeneColor = expData.getProbeColor(experiment.getGeneIndexMappedToData(colIndex));
        
        if(this.rowGeneColor == null && this.columnGeneColor == null)
            this.haveColor = false;
        else{            
         if(this.rowGeneColor == null)
            this.rowGeneColor = new Color(Integer.parseInt("FFFFCC",16));
        if(this.columnGeneColor == null)
            this.columnGeneColor = new Color(Integer.parseInt("FFFFCC",16));
        }
        
        this.setValues(colIndex, rowIndex);
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
        closeButton.setFocusPainted(false);
        JButton graphButton = new JButton("Expression Graph");
        graphButton.setActionCommand("show-graph-command");
        graphButton.addActionListener(listener);
        graphButton.setFocusPainted(false);
        
        contentPane.setLayout(new GridBagLayout());
        JScrollPane scrollPane = new JScrollPane(infoDisplayTextPane, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getViewport().setBackground(Color.white);
        scrollPane.getVerticalScrollBar().setValues(0, 10, 0, 100);
        
        GBA gba = new GBA();
        gba.add(contentPane, scrollPane, 0, 0, 3, 2, 1, 1, GBA.B, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
        
        gba.add(contentPane, graphButton, 0, 3, 1, 1, 0, 0, GBA.NONE, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
        gba.add(contentPane, closeButton, 1, 3, 1, 1, 0, 0, GBA.NONE, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
     //   gba.add(contentPane, spotImage, 2, 3, 1, 1, 0, 0, GBA.NONE, GBA.E, new Insets(5, 5, 5, 5), 0, 0);
        
        pack();
        setSize(550, 600);
        setResizable(true);
        setTitle("Gene Distance Spot Information");
        setLocation(400, 100);
        show();
    }
    
    private String createMessage(){
        int stringLength = 0;
        
        int colTrueRow = expData.getProbeRow(0,this.colNumber); // eCol.getRow(ISlideDataElement.BASE);
        int colTrueColumn = expData.getProbeColumn(0,this.colNumber);//= eCol.getColumn(ISlideDataElement.BASE);
        
        int rowTrueRow = expData.getProbeRow(0,this.rowNumber); // eRow.getRow(ISlideDataElement.BASE);
        int rowTrueColumn = expData.getProbeColumn(0,this.rowNumber);//eRow.getColumn(ISlideDataElement.BASE);
        
        String[] fieldNames = expData.getFieldNames();
        int num_fields = fieldNames.length;
        
        int displayRowNum = this.rowNumber + 1;
        int displayColNum = this.colNumber + 1;
        
        String colColor= "";
        String rowColor= "";
        if(haveColor){
            colColor = '#' + (Integer.toHexString(columnGeneColor.getRGB())).substring(2, 8);
            rowColor = '#' + (Integer.toHexString(rowGeneColor.getRGB())).substring(2, 8);
        }
        
        String message = "<h3>Annotation</h3>"; 
        message += "<table border=2 cellpadding=4 valign=top>" +
        "<tr><td  valign=top></td><td  align=top><b>Column Gene</b></td><td  align=top><b>Row Gene</b></td></tr>" +
        "<tr><td  valign=top><b>Row</b></td><td  valign=top>" + colTrueRow + "</td><td valign=top>" + rowTrueRow + "</td></tr>" +
        "<tr><td  valign=top><b>Column</b></td><td  valign=top>" + colTrueColumn + "</td><td valign=top>" + rowTrueColumn + "</td></tr>";
        if(haveColor)
            message += "<tr><td valign=top><b>Gene Color</b></td><td   valign=top bgcolor=" + colColor +"></td><td valign=top bgcolor=" + rowColor + "></td></tr>" ;
        
        for (int i = 0; i < num_fields; i++) {
            message += "<tr><td valign=top><b>" + fieldNames[i] + "</b></td><td  valign=top>" + expData.getElementAttribute(experiment.getGeneIndexMappedToData(colIndex), i) + "</td><td valign=top>" + expData.getElementAttribute(experiment.getGeneIndexMappedToData(rowIndex), i) + "</td></tr>";
        }       
        message += "</table>";
        
        message += "<h3>Distance Information</h3>" +
        "<table border=2 cellpadding=4 valign=top>" +
        "<tr><td valign=top><b>GDM Matrix Row</b></td><td valign=top>" +  displayRowNum + "</td></tr>" +
        "<tr><td valign=top><b>GDM Matrix Column</b></td><td valign=top>" + displayColNum + "</td></tr>" +
        "<tr><td valign=top><b>Scaled Gene Distance</b></td><td valign=top><b>" + geneDistMatrix.get(colIndex, rowIndex) + "<b></td></tr>" +
        "<tr><td valign=top><b>Actual Gene Distance</b></td><td valign=top><b>" + rawMatrix.get(colIndex, rowIndex) + "<b></td></tr>" +
        "<tr><td valign=top><b>Distance Metric</b></td><td valign=top>" + distanceMetric + "</td></tr>" +
        "<tr><td valign=top><b>Vector Size</b></td><td valign=top>" + this.vectorSize + "</td></tr>" +
        "<tr><td  valign=top><b>Missing Values</b></td><td  valign=top>" + colMissingValues + ", " + rowMissingValues + "</td></tr>"+
        "<tr><td valign=top><b>Distance Based on</b></td><td valign=top>" + this.distanceBasedOn + "</td></tr>";
        
        return message;
    }
    
    public void createGeneGraph() {
        JFrame graphFrame;
        GraphViewer graph;
        GraphPoint gp;
        GraphLine gl;
        GraphTick gt;
       
        //accumulate data
        float [] rowData = this.data.A[rowIndex];
        float [] colData = this.data.A[colIndex];
        float rowMin = Float.POSITIVE_INFINITY;
        float rowMax = Float.NEGATIVE_INFINITY;
        float colMin = Float.POSITIVE_INFINITY;
        float colMax = Float.NEGATIVE_INFINITY;
        int numberOfExp = this.data.getColumnDimension();
        float val;
        //get min and max
        for (int i = 0; i < numberOfExp; i++) {
            if(!Float.isNaN(rowData[i])){
                if(rowData[i] < rowMin)
                    rowMin = rowData[i];
                if(rowData[i] > rowMax)
                    rowMax = rowData[i];                
            }
            if(!Float.isNaN(colData[i])){
                if(colData[i] < colMin)
                    colMin = colData[i];
                if(colData[i] > colMax)
                    colMax = colData[i];                
            }
        }
        
        rowMin = Math.min(rowMin, colMin);
        rowMax = Math.max(rowMax, colMax); //use new row values for limits
        rowMin = (int)(rowMin - 1);  //extend the bounds
        rowMax = (int)(rowMax +1);
        if(rowMin > 0)
            rowMin = 0;
        if(rowMax < 0)
            rowMax = 0;
        
        graphFrame = new JFrame("Samples vs. Log Ratio");
        graph = new GraphViewer(graphFrame, 0, 500, 0, 500, 1, numberOfExp, rowMin, rowMax, 100, 100, 100, 100, "Sample vs. Log Ratio", "Sample Name", "Log2 (Cy5 / Cy3)");
        graph.setSubTitle("(red line = row gene, blue line = column gene)");
        
        graph.setXAxisValue(0);
        graph.setYAxisValue(1);

        
        //Add yellow grid lines
        for (int i = 1; i <= numberOfExp; i++) {
            gl = new GraphLine(i, rowMin, i, rowMax, Color.yellow);
            graph.addGraphElement(gl);
        }        
        for (int i = (int)rowMin; i <= rowMax; i++) {
            if (i != 0) {
                gl = new GraphLine(1, i, numberOfExp, i, Color.yellow);
                graph.addGraphElement(gl);
            }
        }
        
        //add graph lines
        for (int i = 0; i < numberOfExp - 1; i++) {  
            if(!Float.isNaN(rowData[i]) && !Float.isNaN(rowData[i+1])) {
                gl = new GraphLine(i+1, rowData[i], i+2, rowData[i+1],Color.red);
                graph.addGraphElement(gl);
            }
            if(!Float.isNaN(colData[i]) && !Float.isNaN(colData[i+1])) {
                gl = new GraphLine(i+1, colData[i], i+2, colData[i+1],Color.blue);
                graph.addGraphElement(gl);
            }
        }
                
        //add graph points
        for (int i = 0; i < numberOfExp; i++) {           
            if(!Float.isNaN(rowData[i])){
                gp = new GraphPoint(i+1, rowData[i], Color.blue, 3);
                graph.addGraphElement(gp);
            }
            if(!Float.isNaN(colData[i])){
                gp = new GraphPoint(i+1, colData[i], Color.red, 3);
                graph.addGraphElement(gp);
            }
        }
        
        for (int i = (int)rowMin; i <= rowMax; i++) {
            if (i == 0) gt = new GraphTick(i, 8, Color.black, GC.VERTICAL, GC.C, "0", Color.black);
            //else gt = new GraphTick(i, 8, Color.black, GC.VERTICAL, GC.C, "2.0E" + i, Color.black);
            else gt = new GraphTick(i, 8, Color.black, GC.VERTICAL, GC.C, "" + i, Color.black);
            graph.addGraphElement(gt);
        }
        
        for (int i = 1; i <= numberOfExp; i++) {
            gt = new GraphTick(i, 8, Color.black, GC.HORIZONTAL, GC.C, expData.getSampleName(i - 1), Color.black);
            graph.addGraphElement(gt);
        }
        
        graphFrame.setSize(500, 500);
        Toolkit toolkit = java.awt.Toolkit.getDefaultToolkit();
        Dimension d = toolkit.getScreenSize();
        graphFrame.setLocation((int)(d.getWidth()/2-250), (int)(d.getHeight()/2-250)); 
        graph.setVisible(true);
    }
        
    private void setValues(int colIndex, int rowIndex) {
        int columns = experiment.getNumberOfSamples();
        boolean [] rowMissingVector = new boolean[columns];
        boolean [] colMissingVector = new boolean[columns];
        
        for( int column=0; column< columns; column++) {
            rowMissingVector[column] = false;
            colMissingVector[column] = false;
        }
        
        for (int column = 0; column < columns; column++) {
            float value = experiment.get(rowIndex, column);
            if (Float.isNaN(value)) {
                rowMissingVector[column] = true;
                this.rowMissingValues ++;
            }
        }
        
        for (int column = 0; column < columns; column++) {
            float value = experiment.get(colIndex, column);
            if (Float.isNaN(value)) {
                colMissingVector[column] = true;
                this.colMissingValues ++;
            }
        }
        
        for(int column =0; column<columns; column++) {
            if ((rowMissingVector[column] == false) && (colMissingVector[column] == false) && (rowIndex != colIndex)) {
                this.distanceBasedOn++;
            }
        }
    }
    
    private void drawGeneColor() {
        
        Graphics g = getGraphics();
        
        if (g != null ){
            g.setColor(rowGeneColor);
            g.fillRect(0, 0, 10, 10);
            
            g.setColor(columnGeneColor);
            g.fillRect(20, 0, 10, 10);
        }
    }
    
    class EventListener implements ActionListener, KeyListener {
        
        public void actionPerformed(ActionEvent event) {
            String command = event.getActionCommand();
            if (command.equals("close")) {
                dispose();
            } else if (command.equals("show-graph-command")){
                createGeneGraph();
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
