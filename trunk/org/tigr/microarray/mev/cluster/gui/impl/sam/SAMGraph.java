/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * SAMGraph.java
 *
 * Created on January 2, 2003, 1:03 PM
 */

package org.tigr.microarray.mev.cluster.gui.impl.sam;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.DecimalFormat;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.tigr.util.awt.ActionInfoDialog;

/**
 *
 * @author  nbhagaba
 * @version
 */
public class SAMGraph extends ActionInfoDialog {
    private double[] observedYArray, expectedXArray;
    private double delta, maxDelta, minDelta;
    int studyDesign;
    boolean infiniteDeltaEncountered;
    double[] deltaGrid; 
    int[] numSigGenesByDelta; 
    double[] medNumFalse, false90th, FDRMedian, FDR90th;
    GraphPanel gPanel; 
    BottomPanel bPanel; 
    /** Creates new SAMGraph */
    public SAMGraph(JFrame parentFrame, int studyDesign, double[] expectedXArray, double[] observedYArray, double delta, double[] deltaGrid, int[] numSigGenesByDelta, double[] medNumFalse, double[] false90th, double[] FDRMedian, double[] FDR90th, boolean modality) {
        //super("SAM Graph");
        super(parentFrame, "SAM Graph", modality);
        this.studyDesign = studyDesign;
        this.observedYArray = observedYArray;
        this.expectedXArray = expectedXArray;  
        this.delta = delta;
        this.deltaGrid = deltaGrid;
        this.numSigGenesByDelta = numSigGenesByDelta;
        this.medNumFalse = medNumFalse;
        this.false90th = false90th;
        this.FDRMedian = FDRMedian;
        this.FDR90th = FDR90th;
        infiniteDeltaEncountered = false;
        setBounds(0, 0, 800, 700);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        JPanel pane = new JPanel();
        pane.setLayout(gridbag);
        
        gPanel = new GraphPanel(expectedXArray, observedYArray); 
        buildConstraints(constraints, 0, 0, 1, 1, 100, 70);
        gridbag.setConstraints(gPanel, constraints);
        pane.add(gPanel);
        
        bPanel = new BottomPanel(); 
        buildConstraints(constraints, 0, 1, 1, 1, 0, 30);
        gridbag.setConstraints(bPanel, constraints);
        pane.add(bPanel);        
        
        setContentPane(pane);
    }
    
    public SAMGraph() {//just for testing
        super(new JFrame(), "SAM Graph", true);
        //this.setBorder(new EtchedBorder());
        setBounds(0, 0, 600, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        // no effect this.observedYArray = observedYArray;
        // no effect this.expectedXArray = expectedXArray;
        
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        JPanel pane = new JPanel();
        pane.setLayout(gridbag);
        
        gPanel = new GraphPanel(expectedXArray, observedYArray); 
        //GraphPanel gPanel = new GraphPanel(expectedXArray, observedYArray);
        buildConstraints(constraints, 0, 0, 1, 1, 100, 100);
        gridbag.setConstraints(gPanel, constraints);
        pane.add(gPanel);
        
        setContentPane(pane);
    }
    
    public void setVisible(boolean visible) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenSize.width - getSize().width)/2, (screenSize.height - getSize().height)/2);
        
        super.setVisible(visible);
        
        if (visible) {
            //bPanel.okButton.requestFocus(); //UNCOMMMENT THIS LATER
        }
    }
    
    void buildConstraints(GridBagConstraints gbc, int gx, int gy,
    int gw, int gh, int wx, int wy) {
        
        gbc.gridx = gx;
        gbc.gridy = gy;
        gbc.gridwidth = gw;
        gbc.gridheight = gh;
        gbc.weightx = wx;
        gbc.weighty = wy;
    }
    
    private float getMax(float[] array) {
        float max = Float.NEGATIVE_INFINITY;
        for (int i = 0; i < array.length; i++) {
            if (max < array[i]) {
                max = array[i];
            }
        }
        return max;
    }
    
    private float getMin(float[] array) {
        float min = Float.POSITIVE_INFINITY;
        for (int i = 0; i < array.length; i++) {
            if (min > array[i]) {
                min = array[i];
            }
        }
        return min;
    }
    
    private double getMax(double[] array) {
        double max = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < array.length; i++) {
            if (max < array[i]) {
                max = array[i];
            }
        }
        return max;
    }
    
    private double getMin(double[] array) {
        double min = Double.POSITIVE_INFINITY;
        for (int i = 0; i < array.length; i++) {
            if (min > array[i]) {
                min = array[i];
            }
        }
        return min;
    }    
    
    private class BottomPanel extends JPanel {
        
        JSlider deltaSlider;
        JTextField deltaTextField, foldChangeTextField;
        JLabel sigLabel, falseSigLabel;
        double maxValue; 
        double initDelta;
        JCheckBox useFoldChangeBox;
        JButton okButton;
        BottomPanel() {
            this.setBorder(new EtchedBorder());
            //deltaSlider = new JSlider(0, 100, 25);
            
            double[] diffValues = new double[observedYArray.length];

            for (int i = 0; i < diffValues.length; i++) {
                diffValues[i] = Math.abs(observedYArray[i] - expectedXArray[i]);
            }
            

            maxDelta = getMax(diffValues);
            minDelta = getMin(diffValues);            
            //float[] absDiffArray = new float[expectedXArray.length];
            
            //initDelta = (double)(0.25*maxDelta);
            
            maxValue = 0.0f;
            double initMax = getMax(observedYArray);
            double initMin = getMin(observedYArray);
            
            if (Math.abs(initMax) > Math.abs(initMin)) {
                //initDelta = (float)0.25*Math.abs(initMax);
                maxValue = Math.abs(initMax);
            } else {
                //initDelta = (float)0.25*Math.abs(initMin);
                maxValue = Math.abs(initMin);
            }
            
            initDelta = delta;
            
            deltaSlider = new JSlider(0, 1000, (int)Math.round(initDelta*1000/maxValue));
            
            deltaTextField = new JTextField("" + initDelta, 7);
            deltaTextField.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    int sliderValue;
                    try {
                        String s = deltaTextField.getText();
                        double f = Double.parseDouble(s);
                        sliderValue = (int)Math.round(f*1000/maxValue);
                        if (sliderValue >= 1000) {
                            //sliderValue = 100;
                            deltaSlider.setValue(1000);
                            deltaTextField.setText("" + (float)maxValue);
                        } else if (sliderValue <= 0) {
                            //sliderValue = 0;
                            deltaSlider.setValue(0);
                            deltaTextField.setText("" + 0.0f);
                        } else {
                            deltaSlider.setValue(sliderValue);
                            deltaTextField.setText("" + (float)f);
                        }
                        
                        //if ()
                    } catch (Exception exc) {
                        deltaSlider.setValue(250);
                        deltaTextField.setText("" + (float)initDelta);
                    }
                    
                    String dString = deltaTextField.getText();
                    delta = Double.parseDouble(dString);
                    sigLabel.setText("Number of Significant Genes: " + getNumSig(delta));
                    falseSigLabel.setText("Median number of false significant genes" + getNumFalseSig(delta));
                    gPanel.repaint();
                    
                }
            });
            deltaTextField.addFocusListener(new FocusListener() {
                public void focusLost(FocusEvent e) {
                    int sliderValue;
                    try {
                        String s = deltaTextField.getText();
                        double f = Double.parseDouble(s);
                        sliderValue = (int)Math.round(f*1000/maxValue);
                        if (sliderValue >= 1000) {
                            //sliderValue = 100;
                            deltaSlider.setValue(1000);
                            deltaTextField.setText("" + (float)maxValue);
                        } else if (sliderValue <= 0) {
                            //sliderValue = 0;
                            deltaSlider.setValue(0);
                            deltaTextField.setText("" + 0.0f);
                        } else {
                            deltaSlider.setValue(sliderValue);
                            deltaTextField.setText("" + (float)f);
                        }                        
                        //deltaSlider.setValue(sliderValue);
                    } catch (Exception exc) {
                        deltaSlider.setValue(250);
                        deltaTextField.setText("" + (float)initDelta);
                        
                    }  
                    String dString = deltaTextField.getText();
                    delta = Double.parseDouble(dString);
                    sigLabel.setText("Number of Significant Genes: " + getNumSig(delta));
                    falseSigLabel.setText("Median number of false significant genes: " + getNumFalseSig(delta));                    
                    gPanel.repaint();                    
                }

                public void focusGained(FocusEvent e) {
		}                
                
            });
            
            
            deltaSlider.addChangeListener(new ChangeListener(){
                public void stateChanged(ChangeEvent e) {
                    JSlider source = (JSlider)e.getSource();
                    //if (!source.getValueIsAdjusting()) {
                    int value = (int)source.getValue();
                    double displayValue = (double)(value*maxValue/1000);
                    if (value == 0) {
                        displayValue = 0.0f;
                    }
                    deltaTextField.setText("" + (float)displayValue);
                    //if (!source.getValueIsAdjusting()) {
                        String dString = deltaTextField.getText();
                        if (!Double.isInfinite(displayValue)) {
                            delta = Double.parseDouble(dString);
                        } else {
                            infiniteDeltaEncountered = true;
                            delta = Double.POSITIVE_INFINITY;
                        }
                        sigLabel.setText("Number of Significant Genes: " + getNumSig(delta));
                        falseSigLabel.setText("Median number of false significant genes: " + getNumFalseSig(delta));                        
                        gPanel.repaint();                            
                    //}
                    //}
                }
            });
            
            
            GridBagLayout gridbag = new GridBagLayout();
            GridBagConstraints constraints = new GridBagConstraints();
            //constraints.fill = GridBagConstraints.BOTH;
            //JPanel pane = new JPanel();
            this.setLayout(gridbag);    
            
            sigLabel = new JLabel("Number of significant genes: " + getNumSig(delta));
            buildConstraints(constraints, 0, 0, 2, 1, 50, 30);
            constraints.anchor = GridBagConstraints.WEST;
            gridbag.setConstraints(sigLabel, constraints);
            this.add(sigLabel);  
            
            falseSigLabel = new JLabel("Median number of falsely significant genes: " + getNumFalseSig(delta));
            buildConstraints(constraints, 1, 0, 2, 1, 50, 0);
            constraints.anchor = GridBagConstraints.EAST;
            gridbag.setConstraints(falseSigLabel, constraints);
            this.add(falseSigLabel);            
            
            JLabel label1 = new JLabel("Use slider to set delta value: ");
            buildConstraints(constraints, 0, 1, 1, 1, 25, 40);
            constraints.anchor = GridBagConstraints.EAST;
            gridbag.setConstraints(label1, constraints);
            this.add(label1); 
            
            buildConstraints(constraints, 1, 1, 1, 1, 25, 0);
            constraints.anchor = GridBagConstraints.WEST;
            gridbag.setConstraints(deltaSlider, constraints);
            this.add(deltaSlider);  
            
            JLabel label2 = new JLabel("Or enter delta value here: ");
            buildConstraints(constraints, 2, 1, 1, 1, 25, 0);
            constraints.anchor = GridBagConstraints.EAST;
            gridbag.setConstraints(label2, constraints);
            this.add(label2);            
            
            buildConstraints(constraints, 3, 1, 1, 1, 25, 0);
            constraints.anchor = GridBagConstraints.WEST;
            gridbag.setConstraints(deltaTextField, constraints);
            this.add(deltaTextField);  
            
            useFoldChangeBox = new JCheckBox("Use Fold Change:", false);
            if ((studyDesign == SAMInitDialog.TWO_CLASS_PAIRED) || (studyDesign == SAMInitDialog.TWO_CLASS_UNPAIRED)) {
                useFoldChangeBox.setEnabled(true);
            } else {
                useFoldChangeBox.setEnabled(false);
            }
            
            
            //useFoldChangeBox.setEnabled(false); // **** FOR NOW, UNTIL FOLD CHANGE OPTION IS IMPLEMENTED
            
            foldChangeTextField = new JTextField("", 7);
            foldChangeTextField.setBackground(Color.darkGray);
            foldChangeTextField.setEnabled(false); 
            
            useFoldChangeBox.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    if (e.getStateChange() == ItemEvent.DESELECTED) {
                        foldChangeTextField.setBackground(Color.darkGray);
                        foldChangeTextField.setText("");
                        foldChangeTextField.setEnabled(false);
                    } else if (e.getStateChange() == ItemEvent.SELECTED) {
                        foldChangeTextField.setBackground(Color.white);
                        foldChangeTextField.setText("2.0");
                        foldChangeTextField.setEnabled(true);
                    }
                }

            });
            buildConstraints(constraints, 0, 2, 1, 1, 30, 30);
            constraints.anchor = GridBagConstraints.EAST;
            gridbag.setConstraints(useFoldChangeBox, constraints);
            this.add(useFoldChangeBox);
            
            
            buildConstraints(constraints, 1, 2, 1, 1, 30, 0);
            constraints.anchor = GridBagConstraints.WEST;
            gridbag.setConstraints(foldChangeTextField, constraints);
            this.add(foldChangeTextField);  
            
            okButton = new JButton("OK");
            okButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    if (useFoldChangeBox.isSelected()) {
                        try {
                            String foldStr = foldChangeTextField.getText();
                            double fl = Double.parseDouble(foldStr);
                            //*** HERE, GET VALUES AND PASS THEM TO THE ALGORITHM, SAME AS BELOW
                            //hide();
                            dispose();
                        } catch (Exception e) {
                            JOptionPane.showMessageDialog(null, "Either deselect the Use Fold Change CheckBox or enter a valid fold change value", "Error!", JOptionPane.WARNING_MESSAGE);
                        }
                    } else {
                        //*** HERE, GET VALUES AND PASS THEM TO THE ALGORITHM
                        //hide();
                        dispose();
                    }
                }
            });
            
            buildConstraints(constraints, 2, 2, 2, 1, 40, 0);
            constraints.anchor = GridBagConstraints.CENTER;
            gridbag.setConstraints(okButton, constraints);
            this.add(okButton);            
            
        }
        

    }
    
    private String pubNumSig, pubNumFalseSigMed, pubNumFalseSig90th, pubFDRMed, pubFDR90th; 
    
    private String getNumSig(double currDel) {
        String numSig = "";
        int finalIndex = 0;
        if (currDel >=maxDelta) {
            numSig = "0";
        } else if (currDel <= minDelta) {
            numSig = String.valueOf(observedYArray.length);
        } else {
            int currentIndex = 0;
            for (int i = 0; i < deltaGrid.length; i++) {
                if (deltaGrid[i] > currDel) {
                    currentIndex = i;
                    break;
                }
            }
            
            if ((deltaGrid[currentIndex] - currDel) > (currDel - deltaGrid[currentIndex - 1])) {
                finalIndex = currentIndex - 1;
            } else {
                finalIndex = currentIndex;
            }
            numSig = String.valueOf(numSigGenesByDelta[finalIndex]);
            
            
        }
        pubNumSig = numSig;
        return numSig;
    }
    
    
    
    private String getNumFalseSig(double currDel) {
        String numFalse = "";
        String numFalse90th = "";
        int finalIndex = 0;
        if ((currDel >=maxDelta) || (currDel <= minDelta) ) {
            numFalse = "N/A        ";
            pubNumFalseSigMed = numFalse;
            pubNumFalseSig90th = numFalse;
            pubFDRMed = numFalse;
            pubFDR90th = numFalse;
            return numFalse;
        } else {
            int currentIndex = 0;
            for (int i = 0; i < deltaGrid.length; i++) {
                if (deltaGrid[i] > currDel) {
                    currentIndex = i;
                    break;
                }
            }
            
            if ((deltaGrid[currentIndex] - currDel) > (currDel - deltaGrid[currentIndex - 1])) {
                finalIndex = currentIndex - 1;
            } else {
                finalIndex = currentIndex;
            }
            DecimalFormat nf = new DecimalFormat();
            nf.setMaximumFractionDigits(5);
            nf.setMinimumFractionDigits(5);
            //numFalse = String.valueOf(medNumFalse[finalIndex]);
            numFalse = nf.format(medNumFalse[finalIndex]);
            pubNumFalseSig90th = nf.format(false90th[finalIndex]);
            pubFDRMed = nf.format(FDRMedian[finalIndex]);
            pubFDR90th = nf.format(FDR90th[finalIndex]);
        }
        
        
        pubNumFalseSigMed = numFalse;
        return numFalse;
    }  
    
    public String getNumSig() {
        return pubNumSig;
    }
    
    public String getNumFalseSigMed() {
        return pubNumFalseSigMed;
    }
    
    public String getNumFalseSig90th() {
        return pubNumFalseSig90th;
    }
    
    public String getFDRMedian() {
        return pubFDRMed;
    }
    
    public String getFDR90th() {
        return pubFDR90th;
    }
    
    private class GraphPanel extends JPanel {
        
        private double[] xArray, yArray;
        int originX, originY;
        
        GraphPanel(double[] xArray, double[] yArray) {
            this.setBorder(new LineBorder(Color.black));
            this.setBackground(Color.white);
            this.xArray = xArray;
            this.yArray = yArray;

        }
        /*
        public void paintComponent(Graphics g) {
            Graphics2D g2D = (Graphics2D)g;
            int maxX = this.getWidth();
            int maxY = this.getHeight();
            int midX = Math.round((float)(maxX/2));
            int midY = Math.round((float)(maxY/2));
            g2D.drawLine(0, midY, maxX, midY);
            g2D.drawLine(midX, 0, midX, maxY);
            //g2D.drawLine(0, 300, 600, 300);
            //g2D.drawLine(300, 0, 300, 600);
        }
         */
        
        public void paint(Graphics g) {
            super.paint(g);
            if ((studyDesign == SAMInitDialog.TWO_CLASS_UNPAIRED) || (studyDesign == SAMInitDialog.TWO_CLASS_PAIRED) || (studyDesign == SAMInitDialog.CENSORED_SURVIVAL) || (studyDesign == SAMInitDialog.ONE_CLASS)) {
                paint1(g);
            } else if (studyDesign == SAMInitDialog.MULTI_CLASS) {
                paint2(g);
            }
            
        }
        
        private void paint1(Graphics g) {
            Graphics2D g2D = (Graphics2D)g;
            int panelWidth = this.getWidth();
            int panelHeight = this.getHeight();
            originX = (int)Math.round((double)(this.getWidth()/2));
            originY = (int)Math.round((double)(this.getHeight()/2));
            double origMaxXValue = getMax(xArray);
            double origMaxYValue = getMax(yArray);
            double origMinXValue = getMin(xArray);
            double origMinYValue = getMin(yArray);            
            
            double xScalingFactor = getXScalingFactor(origMaxXValue, origMinXValue); // relative to originX and originY
            double yScalingFactor = getYScalingFactor(origMaxYValue, origMinYValue); // relative to originX and originY
            
            final float dash1[] = {10.0f};
            
            final BasicStroke dashed = new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash1, 0.0f);
            
            //System.out.println("xScalingFactor = " + xScalingFactor + ", yScalingFactor = " + yScalingFactor);
            
            //draw axes
            g2D.setStroke(new BasicStroke(2.0f));
            g2D.drawLine(0, (int)Math.round((double)(this.getHeight()/2)), this.getWidth(), (int)Math.round((double)(this.getHeight()/2)));
            g2D.drawLine((int)Math.round((double)(this.getWidth()/2)), 0, (int)Math.round((double)(this.getWidth()/2)), this.getHeight());
            
            double[] xIntervalArray = new double[6];
            double[] yIntervalArray = new double[6];
            
            double xIncrement = 0.0d; 
            double yIncrement = 0.0d;
            
            if (Math.abs(origMaxXValue) > Math.abs(origMinXValue)) {
                xIncrement = Math.abs((double)(origMaxXValue/5.0d));
            } else {
                xIncrement = Math.abs((double)(origMinXValue/5.0d));
            }

            if (Math.abs(origMaxYValue) > Math.abs(origMinYValue)) {
                yIncrement = Math.abs((double)(origMaxYValue/5.0d));
            } else {
                yIncrement = Math.abs((double)(origMinYValue/5.0d));
            }            
            
            double xCounter = 0.0d;
            double yCounter = 0.0d;
            for (int i = 0; i < xIntervalArray.length; i++) {
                xIntervalArray[i] = xCounter;
                xCounter = xCounter + xIncrement;
                yIntervalArray[i] = yCounter;
                yCounter = yCounter + yIncrement;
            }
            
            //draw x tick marks
            
            for (int i = 1; i < xIntervalArray.length; i++) {
                g2D.drawLine((int)Math.round(xIntervalArray[i]*xScalingFactor) +this.getWidth()/2, this.getHeight()/2 - 5, (int)Math.round(xIntervalArray[i]*xScalingFactor) +this.getWidth()/2, this.getHeight()/2 + 5);
            }
            
            for (int i = 1; i < xIntervalArray.length; i++) {
                g2D.drawLine(this.getWidth()/2 - (int)Math.round(xIntervalArray[i]*xScalingFactor), this.getHeight()/2 - 5, this.getWidth()/2 - (int)Math.round(xIntervalArray[i]*xScalingFactor), this.getHeight()/2 + 5);
            }  
            
            
            //draw  reference line of slope = 1
            g2D.drawLine(this.getWidth()/2 - (int)Math.round(xIntervalArray[5]*xScalingFactor), this.getHeight()/2 + (int)Math.round(xIntervalArray[5]*yScalingFactor), this.getWidth()/2 + (int)Math.round(xIntervalArray[5]*xScalingFactor), this.getHeight()/2 - (int)Math.round(xIntervalArray[5]*yScalingFactor));
            
            g2D.setColor(Color.gray);
            
            g2D.setStroke(new BasicStroke(1.0f));
            
            //draw horizonal grid lines
            for (int i = 1; i < yIntervalArray.length; i++) {
                g2D.drawLine(0, this.getHeight()/2 + (int)Math.round(yIntervalArray[i]*yScalingFactor), this.getWidth(), this.getHeight()/2 + (int)Math.round(yIntervalArray[i]*yScalingFactor));
            } 
            
            for (int i = 1; i < yIntervalArray.length; i++) {
                g2D.drawLine(0, this.getHeight()/2 - (int)Math.round(yIntervalArray[i]*yScalingFactor), this.getWidth(), this.getHeight()/2 - (int)Math.round(yIntervalArray[i]*yScalingFactor));
            }            
            
            g2D.setStroke(new BasicStroke(2.0f));
            g2D.setColor(Color.black);
            
            DecimalFormat nf = new DecimalFormat();
            nf.setMaximumFractionDigits(2);
            
            //tick labels
            for (int i = 1; i < xIntervalArray.length; i++) {
                g2D.drawString(nf.format((double)xIntervalArray[i]), (int)Math.round(xIntervalArray[i]*xScalingFactor) +this.getWidth()/2 - 10, this.getHeight()/2 + 20);
            }
            
            for (int i = 1; i < xIntervalArray.length; i++) {
                g2D.drawString("-" + nf.format((double)xIntervalArray[i]), this.getWidth()/2 - (int)Math.round(xIntervalArray[i]*xScalingFactor) - 10, this.getHeight()/2 + 20);
            }
            
            for (int i = 1; i < yIntervalArray.length; i++) {
                g2D.drawString(nf.format((double)yIntervalArray[i]), this.getWidth()/2 - 30, this.getHeight()/2 - (int)Math.round(yIntervalArray[i]*yScalingFactor) - 5);
            }
            
            for (int i = 1; i < yIntervalArray.length; i++) {
                g2D.drawString("-" + nf.format((double)yIntervalArray[i]), this.getWidth()/2 - 30, this.getHeight()/2 + (int)Math.round(yIntervalArray[i]*yScalingFactor) - 5);
            }            
            
            int deltaOffset = (int)Math.round(delta*yScalingFactor);
            
            //draw dashed lines for delta limits
            g2D.setStroke(dashed);
            
            g2D.drawLine(this.getWidth()/2 - (int)Math.round(xIntervalArray[5]*xScalingFactor), this.getHeight()/2 + (int)Math.round(xIntervalArray[5]*yScalingFactor) - deltaOffset, this.getWidth()/2 + (int)Math.round(xIntervalArray[5]*xScalingFactor), this.getHeight()/2 - (int)Math.round(xIntervalArray[5]*yScalingFactor) - deltaOffset);
            g2D.drawLine(this.getWidth()/2 - (int)Math.round(xIntervalArray[5]*xScalingFactor), this.getHeight()/2 + (int)Math.round(xIntervalArray[5]*yScalingFactor) + deltaOffset, this.getWidth()/2 + (int)Math.round(xIntervalArray[5]*xScalingFactor), this.getHeight()/2 - (int)Math.round(xIntervalArray[5]*yScalingFactor) + deltaOffset);
            
            g2D.setStroke(new BasicStroke(2.0f));
            
            boolean posSigEncountered = false;
            boolean negSigEncountered = false;

            int lowestPosSigIndex = 0;
            int highestNegSigIndex = 0;

            for (int i = 0; i < xArray.length; i++) {
                if ( (xArray[i] > 0.0d) && ((yArray[i] - xArray[i]) > delta) ) {
                    lowestPosSigIndex = i;
                    posSigEncountered = true;
                    break;
                }
            }

            for (int i = 0; i < xArray.length; i++) {
                if ( (xArray[i] < 0.0d) && ((xArray[i] - yArray[i]) > delta) ) {
                    highestNegSigIndex = i;
                    negSigEncountered = true; 
                }
            }  
            
            Color[] pointColor = new Color[xArray.length];
            
            if ((posSigEncountered)&&(negSigEncountered)) {
                for (int i = 0; i < (highestNegSigIndex + 1); i++) {
                    //negSigGenes.add(new Integer(sortedDArrayIndices[i]));
                    pointColor[i] = Color.green.darker();
                }

                for (int i = (highestNegSigIndex + 1); i < lowestPosSigIndex; i++) {
                    //nonSigGenes.add(new Integer(sortedDArrayIndices[i]));
                    pointColor[i] = Color.black;
                }

                for (int i = lowestPosSigIndex; i < xArray.length; i++) {
                    //posSigGenes.add(new Integer(sortedDArrayIndices[i]));
                    pointColor[i] = Color.red;
                }

            } else if((posSigEncountered)&&(!negSigEncountered)) {
                for (int i = 0; i < lowestPosSigIndex; i++) {
                    //nonSigGenes.add(new Integer(sortedDArrayIndices[i]));
                    pointColor[i] = Color.black;
                }

                for (int i = lowestPosSigIndex; i < xArray.length; i++) {
                    //posSigGenes.add(new Integer(sortedDArrayIndices[i]));
                    pointColor[i] = Color.red;
                }
            } else if ((!posSigEncountered) && (negSigEncountered)) {
                for (int i = 0; i < (highestNegSigIndex + 1); i++) {
                    //negSigGenes.add(new Integer(sortedDArrayIndices[i]));
                    pointColor[i] = Color.green.darker();
                }
                for (int i = (highestNegSigIndex + 1); i < xArray.length; i++) {
                    //nonSigGenes.add(new Integer[sortedDArrayIndices[i]]);
                    pointColor[i] = Color.black;
                }
            } else if ((!posSigEncountered) && (!negSigEncountered)) {
                for (int i = 0; i < xArray.length; i++) {
                    //nonSigGenes.add(new Integer(sortedDArrayIndices[i]));
                    pointColor[i] = Color.black;
                }
            }            
            
            //draw data points
            for (int i = 0; i < expectedXArray.length; i++) {
                /*
                if (((yArray[i] - xArray[i]) > delta) && (yArray[i] > 0.0d) ) {
                    g2D.setColor(Color.red);
                } else if (((yArray[i] - xArray[i]) < (-1.0d)*delta) && (yArray[i] < 0.0d) ) {
                    g2D.setColor(Color.green.darker());
                }
                 */
                g2D.setColor(pointColor[i]);
                drawPoint(g2D, xArray[i], yArray[i], getXScalingFactor(origMaxXValue, origMinXValue), getYScalingFactor(origMaxYValue, origMinYValue), 5);
                g2D.setColor(Color.black);
                //g2D.drawOval((midX + expectedXArray[i]),
            }
            
            g2D.drawString( "X axis = Expected, Y axis = Observed", this.getWidth()/2 + 25, this.getHeight() - 25);            
        }
        
        private void paint2(Graphics g) {
            Graphics2D g2D = (Graphics2D)g;
            int panelWidth = this.getWidth();
            int panelHeight = this.getHeight();
            originX = 30;
            originY = (int)Math.round((double)(this.getHeight() - 30));
            double origMaxXValue = getMax(xArray);
            double origMaxYValue = getMax(yArray);
            double origMinXValue = getMin(xArray);
            double origMinYValue = getMin(yArray);            
            
            double xScalingFactor = getXScalingFactor(origMaxXValue, origMinXValue); // relative to originX and originY
            double yScalingFactor = getYScalingFactor(origMaxYValue, origMinYValue); // relative to originX and originY
            
            final float dash1[] = {10.0f};
            
            final BasicStroke dashed = new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash1, 0.0f);
            
            //System.out.println("xScalingFactor = " + xScalingFactor + ", yScalingFactor = " + yScalingFactor);
            
            //draw axes
            g2D.setStroke(new BasicStroke(2.0f));
            g2D.drawLine(0, (int)Math.round((double)(this.getHeight() - 30)), this.getWidth(), (int)Math.round((double)(this.getHeight() - 30)));
            g2D.drawLine(30, 0, 30, this.getHeight());
            
            double[] xIntervalArray = new double[6];
            double[] yIntervalArray = new double[6];
            
            double xIncrement = 0.0d; 
            double yIncrement = 0.0d;
            
            if (Math.abs(origMaxXValue) > Math.abs(origMinXValue)) {
                xIncrement = Math.abs((double)(origMaxXValue/5.0d));
            } else {
                xIncrement = Math.abs((double)(origMinXValue/5.0d));
            }

            if (Math.abs(origMaxYValue) > Math.abs(origMinYValue)) {
                yIncrement = Math.abs((double)(origMaxYValue/5.0d));
            } else {
                yIncrement = Math.abs((double)(origMinYValue/5.0d));
            }            
            
            double xCounter = 0.0d;
            double yCounter = 0.0d;
            for (int i = 0; i < xIntervalArray.length; i++) {
                xIntervalArray[i] = xCounter;
                xCounter = xCounter + xIncrement;
                yIntervalArray[i] = yCounter;
                yCounter = yCounter + yIncrement;
            }
            
            //draw x tick marks
            
            for (int i = 1; i < xIntervalArray.length; i++) {
                g2D.drawLine((int)Math.round(xIntervalArray[i]*xScalingFactor) + 30, (this.getHeight() - 30) - 5, (int)Math.round(xIntervalArray[i]*xScalingFactor) + 30, (this.getHeight() - 30) + 5);
            }
            
            
            
            //for (int i = 1; i < xIntervalArray.length; i++) {
            //    g2D.drawLine(this.getWidth()/2 - (int)Math.round(xIntervalArray[i]*xScalingFactor), this.getHeight()/2 - 5, this.getWidth()/2 - (int)Math.round(xIntervalArray[i]*xScalingFactor), this.getHeight()/2 + 5);
            //}  
            
            
            //draw reference line of slope = 1
            g2D.drawLine(30, this.getHeight() - 30, 30 + (int)Math.round(xIntervalArray[5]*xScalingFactor), this.getHeight() - 30 - (int)Math.round(xIntervalArray[5]*yScalingFactor));
            
            g2D.setColor(Color.gray);
            
            g2D.setStroke(new BasicStroke(1.0f));
            
            //draw horizonal grid lines
            for (int i = 1; i < yIntervalArray.length; i++) {
                g2D.drawLine(0, this.getHeight() - 30 - (int)Math.round(yIntervalArray[i]*yScalingFactor), this.getWidth(), this.getHeight() - 30 - (int)Math.round(yIntervalArray[i]*yScalingFactor));
            } 
            
            //for (int i = 1; i < yIntervalArray.length; i++) {
            //    g2D.drawLine(0, this.getHeight()/2 - (int)Math.round(yIntervalArray[i]*yScalingFactor), this.getWidth(), this.getHeight()/2 - (int)Math.round(yIntervalArray[i]*yScalingFactor));
            //}            
            
            g2D.setStroke(new BasicStroke(2.0f));
            g2D.setColor(Color.black);
            
            DecimalFormat nf = new DecimalFormat();
            nf.setMaximumFractionDigits(2);
            
            //tick labels
            for (int i = 1; i < xIntervalArray.length; i++) {
                g2D.drawString(nf.format((double)xIntervalArray[i]), (int)Math.round(xIntervalArray[i]*xScalingFactor) + 10, this.getHeight() - 5);
            }
            
            //for (int i = 1; i < xIntervalArray.length; i++) {
            //    g2D.drawString("-" + nf.format((double)xIntervalArray[i]), this.getWidth()/2 - (int)Math.round(xIntervalArray[i]*xScalingFactor) - 10, this.getHeight()/2 + 20);
            //}
            
            for (int i = 1; i < yIntervalArray.length; i++) {
                g2D.drawString(nf.format((double)yIntervalArray[i]), 5, this.getHeight() - 30 - (int)Math.round(yIntervalArray[i]*yScalingFactor) - 5);
            }
            
            //for (int i = 1; i < yIntervalArray.length; i++) {
            //    g2D.drawString("-" + nf.format((double)yIntervalArray[i]), this.getWidth()/2 - 30, this.getHeight()/2 + (int)Math.round(yIntervalArray[i]*yScalingFactor) - 5);
            //}            
            
            int deltaOffset = (int)Math.round(delta*yScalingFactor);
            
            //draw dashed lines for delta limits
            g2D.setStroke(dashed);
            
            g2D.drawLine(30, this.getHeight() - 30 - deltaOffset, 30 + (int)Math.round(xIntervalArray[5]*xScalingFactor), this.getHeight() - 30 - (int)Math.round(xIntervalArray[5]*yScalingFactor) - deltaOffset);
            g2D.drawLine(30, this.getHeight() - 30 + deltaOffset, 30 + (int)Math.round(xIntervalArray[5]*xScalingFactor), this.getHeight() - 30 - (int)Math.round(xIntervalArray[5]*yScalingFactor) + deltaOffset);
            
            g2D.setStroke(new BasicStroke(2.0f));
            
            boolean posSigEncountered = false;
            boolean negSigEncountered = false;

            int lowestPosSigIndex = 0;
            int highestNegSigIndex = 0;

            for (int i = 0; i < xArray.length; i++) {
                if ( (xArray[i] > 0.0d) && ((yArray[i] - xArray[i]) > delta) ) {
                    lowestPosSigIndex = i;
                    posSigEncountered = true;
                    break;
                }
            }

            for (int i = 0; i < xArray.length; i++) {
                if ( (xArray[i] < 0.0d) && ((xArray[i] - yArray[i]) > delta) ) {
                    highestNegSigIndex = i;
                    negSigEncountered = true; 
                }
            }  
            
            Color[] pointColor = new Color[xArray.length];
            
            if ((posSigEncountered)&&(negSigEncountered)) {
                for (int i = 0; i < (highestNegSigIndex + 1); i++) {
                    //negSigGenes.add(new Integer(sortedDArrayIndices[i]));
                    pointColor[i] = Color.green.darker();
                }

                for (int i = (highestNegSigIndex + 1); i < lowestPosSigIndex; i++) {
                    //nonSigGenes.add(new Integer(sortedDArrayIndices[i]));
                    pointColor[i] = Color.black;
                }

                for (int i = lowestPosSigIndex; i < xArray.length; i++) {
                    //posSigGenes.add(new Integer(sortedDArrayIndices[i]));
                    pointColor[i] = Color.red;
                }

            } else if((posSigEncountered)&&(!negSigEncountered)) {
                for (int i = 0; i < lowestPosSigIndex; i++) {
                    //nonSigGenes.add(new Integer(sortedDArrayIndices[i]));
                    pointColor[i] = Color.black;
                }

                for (int i = lowestPosSigIndex; i < xArray.length; i++) {
                    //posSigGenes.add(new Integer(sortedDArrayIndices[i]));
                    pointColor[i] = Color.red;
                }
            } else if ((!posSigEncountered) && (negSigEncountered)) {
                for (int i = 0; i < (highestNegSigIndex + 1); i++) {
                    //negSigGenes.add(new Integer(sortedDArrayIndices[i]));
                    pointColor[i] = Color.green.darker();
                }
                for (int i = (highestNegSigIndex + 1); i < xArray.length; i++) {
                    //nonSigGenes.add(new Integer[sortedDArrayIndices[i]]);
                    pointColor[i] = Color.black;
                }
            } else if ((!posSigEncountered) && (!negSigEncountered)) {
                for (int i = 0; i < xArray.length; i++) {
                    //nonSigGenes.add(new Integer(sortedDArrayIndices[i]));
                    pointColor[i] = Color.black;
                }
            }            
            
            //draw data points
            for (int i = 0; i < expectedXArray.length; i++) {

                g2D.setColor(pointColor[i]);
                drawPoint(g2D, xArray[i], yArray[i], getXScalingFactor(origMaxXValue, origMinXValue), getYScalingFactor(origMaxYValue, origMinYValue), 5);
                g2D.setColor(Color.black);
                //g2D.drawOval((midX + expectedXArray[i]),
            }
            
            g2D.drawString( "X axis = Expected, Y axis = Observed", 40, 20); 
             
                       
        }        
        
        private void drawPoint(Graphics2D g2D, double xValue, double yValue, double xScale, double yScale, int diameter) {
            int xRaw = (int)Math.round(xValue*xScale);
            int yRaw = (int)Math.round(yValue*yScale);
            //System.out.println("xValue = " + xValue + " , yValue = " + yValue + ", xRaw = " + xRaw + ", yRaw  = " + yRaw);
            
            int xCoord = 0;
            int yCoord = 0;
            
            //if (xValue >= 0) {
            if ((studyDesign == SAMInitDialog.TWO_CLASS_UNPAIRED) || (studyDesign == SAMInitDialog.TWO_CLASS_PAIRED) || (studyDesign == SAMInitDialog.CENSORED_SURVIVAL) || (studyDesign == SAMInitDialog.ONE_CLASS)) {
                xCoord = (int)Math.round((double)(this.getWidth()/2)) + xRaw;
            //} else {
                //xCoord = Math.round((float)(this.getWidth()/2)) - xRaw;
            //}
            
            //if (yValue >= 0) {
                yCoord = (int)Math.round((double)(this.getHeight()/2)) - yRaw;
            //} else {
                //yCoord = Math.round((float)(this.getHeight()/2)) + yRaw;
            //}
            } else if (studyDesign == SAMInitDialog.MULTI_CLASS) {
                xCoord = 30 + xRaw; 
                yCoord = (int)Math.round((double)(this.getHeight() - 30)) - yRaw;                
            }
            
            g2D.fillOval(xCoord, yCoord, diameter, diameter);
            
        }
         
        
       private double getXScalingFactor(double maxValue, double minValue) {
           double largest = 1;
           if ((maxValue > 0)&&(minValue > 0)) {
               largest = maxValue;
           } else if ((maxValue > 0)&&(minValue < 0)) {
               if (maxValue > Math.abs(minValue)) {
                   largest = maxValue;
               } else {
                   largest = Math.abs(minValue);
               }
           } else if (maxValue <= 0) {
               largest = Math.abs(minValue);
           } else if (minValue == 0) {
               largest = maxValue;
           }
           
           double scalingFactor =0; 
           if ((studyDesign == SAMInitDialog.TWO_CLASS_UNPAIRED) || (studyDesign == SAMInitDialog.TWO_CLASS_PAIRED) || (studyDesign == SAMInitDialog.CENSORED_SURVIVAL) || (studyDesign == SAMInitDialog.ONE_CLASS)) {
               scalingFactor = (this.getWidth()/2 - 50)/largest;
           } else if (studyDesign == SAMInitDialog.MULTI_CLASS) {
               scalingFactor = ((this.getWidth() - 50) - 30)/largest;
           }
           
           return scalingFactor;
       }

       private double getYScalingFactor(double maxValue, double minValue) {
           double largest = 1;
           if ((maxValue > 0)&&(minValue > 0)) {
               largest = maxValue;
           } else if ((maxValue > 0)&&(minValue < 0)) {
               if (maxValue > Math.abs(minValue)) {
                   largest = maxValue;
               } else {
                   largest = Math.abs(minValue);
               }
           } else if (maxValue <= 0) {
               largest = Math.abs(minValue);
           } else if (minValue == 0) {
               largest = maxValue;
           }
           
          double scalingFactor = 0; 
          if ((studyDesign == SAMInitDialog.TWO_CLASS_UNPAIRED) || (studyDesign == SAMInitDialog.TWO_CLASS_PAIRED) || (studyDesign == SAMInitDialog.CENSORED_SURVIVAL) || (studyDesign == SAMInitDialog.ONE_CLASS)) {
              scalingFactor = (this.getHeight()/2 - 50)/largest;
          } else if (studyDesign == SAMInitDialog.MULTI_CLASS) {
              scalingFactor = ((this.getHeight() - 30) - 50)/largest;
          }
           
           return scalingFactor;
       }       
       
        
    }
    
    public boolean useFoldChange() {
        return bPanel.useFoldChangeBox.isSelected();
    }
    
    public double getFoldChangeValue() {
        String s2 = bPanel.foldChangeTextField.getText();
        return Double.parseDouble(s2);
    }
    
    public double getDelta() {
        String s = bPanel.deltaTextField.getText();
        if (infiniteDeltaEncountered) {
            return Double.POSITIVE_INFINITY;
        } else {
            return Double.parseDouble(s);
        }
    }
    
    public static void main(String[] args) {
        SAMGraph sg = new SAMGraph();
        sg.setVisible(true);
    }
    
}
