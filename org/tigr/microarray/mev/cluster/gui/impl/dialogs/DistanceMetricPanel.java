/*
Copyright @ 1999-2004, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * DistanceMetricPanel.java
 *
 * Created on November 15, 2004, 2:32 PM
 */

package org.tigr.microarray.mev.cluster.gui.impl.dialogs;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;

import javax.swing.JLabel;
import javax.swing.JPanel;

import javax.swing.border.EtchedBorder;

import org.tigr.microarray.mev.cluster.algorithm.Algorithm;

/**
 *
 * @author  braisted
 */
public class DistanceMetricPanel extends JPanel {
    
    private JCheckBox absBox;
    private JComboBox metricBox;
    
    private String globalFunctionName;
    private boolean globalAbsoluteValue;
    
    private ActionListener listener;
    
    /** Creates a new instance of DistanceMetricPanel */
    public DistanceMetricPanel(String globalFunctionName, boolean globalAbsoluteValue, String defaultMetricName, String algName, boolean showTitle, boolean whiteBackground ) {
        super(new GridBagLayout());
        
        this.globalAbsoluteValue = globalAbsoluteValue;
        this.globalFunctionName = globalFunctionName;
        
        if(whiteBackground)
            setBackground(Color.white);
        
        if(showTitle)
            setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED),"Distance Metric Selection"));
        
        JLabel globalMetricLabel = new JLabel("Current Global Metric: "+globalFunctionName);        
        JLabel localSettingLabel = new JLabel("Current Metric: ");

        absBox = new JCheckBox("Use Absolute Distance", globalAbsoluteValue);
        absBox.setOpaque(false);
        absBox.setFocusPainted(false);
         Vector metrics = buildMetricVector();
        metricBox = new JComboBox(metrics);    
        metricBox.addActionListener(new Listener());
        if(globalFunctionName.equals("not defined"))
            setMetricSelection(defaultMetricName);
        else
            setMetricSelection(globalFunctionName);
        
        metricBox.addActionListener(new Listener());
        
        enableAbsolute(globalFunctionName);
        
        JLabel defaultDistanceLabel = new JLabel("(The default distance metric for "+algName+ " is "+defaultMetricName+")");
        
        add(localSettingLabel, new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,10),0,0));
        add(metricBox, new GridBagConstraints(1,0,1,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
        add(defaultDistanceLabel, new GridBagConstraints(0,1,2,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10,0,0,0),0,0));
        add(absBox, new GridBagConstraints(0,2,2,1,0,0,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10,0,0,0),0,0));
    }
    
    public int getMetricIndex() {
        DistanceMetric metric = (DistanceMetric)(metricBox.getSelectedItem());
        return metric.getIndex();
    }
    
    public boolean getAbsoluteSelection() {
        return absBox.isSelected();
    }
    
    public void reset() {        
        setMetricSelection(globalFunctionName);
        enableAbsolute(globalFunctionName);
        absBox.setSelected(this.globalAbsoluteValue);
    }
    
    private Vector buildMetricVector() {
        Vector distanceVector = new Vector();
        
        distanceVector.addElement(new DistanceMetric("Euclidean Distance", Algorithm.EUCLIDEAN));
        distanceVector.addElement(new DistanceMetric("Manhattan Distance", Algorithm.MANHATTAN));
        distanceVector.addElement(new DistanceMetric("Average Dot Product", Algorithm.DOTPRODUCT));
        distanceVector.addElement(new DistanceMetric("Pearson Correlation", Algorithm.PEARSON));
        distanceVector.addElement(new DistanceMetric("Pearson Uncentered", Algorithm.PEARSONUNCENTERED));
        distanceVector.addElement(new DistanceMetric("Pearson Squared", Algorithm.PEARSONSQARED));
        distanceVector.addElement(new DistanceMetric("Cosine Correlation", Algorithm.COSINE));
        distanceVector.addElement(new DistanceMetric("Covariance Value", Algorithm.COVARIANCE));
        distanceVector.addElement(new DistanceMetric("Spearman Rank Correlation", Algorithm.SPEARMANRANK));
        distanceVector.addElement(new DistanceMetric("Kendall's Tau", Algorithm.KENDALLSTAU));
        distanceVector.addElement(new DistanceMetric("Mutual Information", Algorithm.MUTUALINFORMATION));
        return distanceVector;
    }
     
    private void enableAbsolute(String metricName) {
        if(metricName.equals("Pearson Correlation")
        || metricName.equals("Pearson Uncentered")
        || metricName.equals("Cosine Correlation")
        || metricName.equals("Average Dot Product"))
            absBox.setEnabled(true);
        else
            absBox.setEnabled(false);
    }
    
    private void setMetricSelection(String metricName) {
        for(int i = 0; i < metricBox.getItemCount(); i++) {
            if((((DistanceMetric)(metricBox.getItemAt(i))).toString()).equals(metricName)) {
                metricBox.setSelectedIndex(i);
                break;
            }                
        }
    }
    
    public void addActionListener(ActionListener listener) {
        this.listener = listener;
    }
    
    private void fireActionEvent() {
        ActionEvent ae = new ActionEvent(this, 0, "distance_metric_selection_command");
        listener.actionPerformed(ae);
    }
    
    private class DistanceMetric {
        private String name;
        private int index;
        
        public DistanceMetric(String name, int index) {
            this.name = name;
            this.index = index;
        }
        
        public String toString() {
            return name;
        }
        
        public int getIndex() {
            return index;
        }
    }
    
    
    private class Listener implements ActionListener {
        
        public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
            enableAbsolute(((DistanceMetric)(metricBox.getSelectedItem())).toString());
            if(listener != null)
                fireActionEvent();
        }        

    }
    
    public static void main(String [] args){ 
        DistanceMetricPanel panel = new DistanceMetricPanel("Pearson Correlation", true, "Euclidean Distance", "HCL", true, true);
        JFrame frame = new JFrame();
        frame.getContentPane().add(panel);
        frame.setSize(400, 200);
        frame.setVisible(true);
    }
}
