/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: SURVInfoViewer.java,v $
 * $Revision: 1.8 $
 * $Date: 2006-06-30 17:52:19 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.surv;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.beans.Expression;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.tigr.microarray.mev.cluster.gui.impl.ViewerAdapter;

public class SURVInfoViewer extends ViewerAdapter implements java.io.Serializable {
//    public static final long serialVersionUID = 202007020001L;
    
    private JComponent header;
    private JTextArea  content;
    
    private SURVAlgorithmData resultData;
    
    public JTextArea getContent(){return content;}
    

    public SURVInfoViewer(SURVAlgorithmData data) {
    	this.resultData = data;
    	header  = createHeader();
        content = createContent();
        setMaxWidth(content, header);  
    }

    /**
     * This constructor is used by the state-saving code for class persistence. 
     * Do not delete or alter.
     * 
     * @param content
     * @param clusterGenes
     */
    public SURVInfoViewer(JTextArea content) { 	 
        this(content, true); 	 
    }
    /**
     * This constructor is used by the state-saving code for class persistence. 
     * Do not delete or alter.
     * 
     * @param content
     * @param clusterGenes
     */
    public SURVInfoViewer(JTextArea content, boolean clusterGenes) { 	 
        header = createHeader(); 	 
        this.content = content; 	 
        setMaxWidth(content, header); 	 
    }
    
    /**
     * @inheritDoc
     * 
     */
    public Expression getExpression(){
    	return new Expression(this, this.getClass(), "new", 
    			new Object[]{this.getContentComponent()});
    }
    
    /**
     * Returns component to be inserted into the framework scroll pane.
     */
    public JComponent getContentComponent() {
        return content;
    }
    
    /**
     * Returns the viewer header.
     */
    public JComponent getHeaderComponent() {
        return header;
    }
    
    /**
     * Creates the viewer header.
     */
    private JComponent createHeader() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.white);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 0, 10, 0);
        panel.add(new JLabel("<html><body bgcolor='#FFFFFF'><font face='serif' size='5' color='#000080'><b>Survival Module Results</b></font></body></html>"), gbc);
        return panel;
    }
    
    /**
     * Creates the viewer content component.
     */
    private JTextArea createContent() {
    	
        JTextArea area = new JTextArea(5, 80);
        area.setEditable(false);
        area.setMargin(new Insets(0, 10, 0, 0));
        StringBuffer sb = new StringBuffer(2+3*3*10);
        
        
        if(resultData.isComparison()) {
	        //Differential survival output
	        sb.append("\n\nKaplan-Meier Survival Comparison.\n");
	        sb.append("\n");
	        sb.append("Chi-squared: " + resultData.getChiSquare() + "\n");
	        sb.append("p-value at 1 degree of freedom: " + resultData.getPValue() + "\n\n");
	        if(resultData.getPValue() < .05) {
	            sb.append("Survival difference is significant at p-value cutoff of 0.05.\n");       	
	        } else {
	            sb.append("Survival difference is NOT significant at p-value cutoff of 0.05.\n");       	
	        }
	        if(resultData.getPValue() < .01) {
	            sb.append("Survival difference is significant at p-value cutoff of 0.01.\n");       	
	        } else {
	            sb.append("Survival difference is NOT significant at p-value cutoff of 0.01.\n");       	
	        }
	        
	        sb.append("\n");
	        sb.append("Cluster\tMedian\tSize\tObserved\tExpected\n");
	        for(int i=0; i<resultData.getExpected().size(); i++) {
	        	sb.append(
	        			(i+1) + "\t" + 
	        			resultData.getSizes().get(i) + "\t" + 
	        			resultData.getMedians().get(i) + "\t" + 
	        			resultData.getObserved().get(i) + "\t" + 
	        			resultData.getExpected().get(i) + "\n"
	        	);
	        }
	        
	    } else {
	        //Cox model output
	    	int nonzero = 0;
	    	Vector<Double> temp = resultData.getPenalizedCoefficients();
	    	for(int i=0; i<temp.size(); i++) {
	    		if(temp.get(i) != 0)
	    			nonzero = nonzero +1;
	    	}
	        sb.append("\n\nCox Proportional Hazard Model. L1 Norm penalty applied for variable selection. \n");
	        sb.append("\n");
	        sb.append("Lambda Penalty:" + resultData.getL1penalty() + " at lambda = " + resultData.getLambda() + "\n");
	        sb.append("Model log Likelihood:" + resultData.getlogLikelihood() + "\n");
	        sb.append("Model log Likelihood, calculated by cross-validation:" + resultData.getCrossValLik() + "\n");
	        sb.append("Coefficients calculated for " + temp.size() + " input genes. " + nonzero + " were non-zero.");
//	        if(resultData.getWeights() != null)
//	        	sb.append("Weight:" + resultData.getWeights() + "\n");
	    }
	        
	        area.setText(sb.toString());
	        area.setCaretPosition(0);
	        return area;
	}
    
    /**
     * Synchronize content and header sizes.
     */
    private void setMaxWidth(JComponent content, JComponent header) {
        int c_width = content.getPreferredSize().width;
        int h_width = header.getPreferredSize().width;
        if (c_width > h_width) {
            header.setPreferredSize(new Dimension(c_width, header.getPreferredSize().height));
        } else {
            content.setPreferredSize(new Dimension(h_width, content.getPreferredSize().height));
        }
    }
    
    /** Returns a component to be inserted into the scroll pane row header
     */
    public JComponent getRowHeaderComponent() {
        return null;
    }
    
}
