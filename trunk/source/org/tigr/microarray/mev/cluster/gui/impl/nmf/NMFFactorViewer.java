/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: NMFFactorViewer,v $
 * $Revision: 1.7 $
 * $Date: 2009-10-2 15:51:05 $
 * $Author: dschlauch $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.nmf;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.beans.Expression;
import java.text.DecimalFormat;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.impl.ViewerAdapter;
import org.tigr.util.FloatMatrix;

public class NMFFactorViewer extends ViewerAdapter {
    
    private JComponent header;
    private JTextArea  content;
    private float cost[];
    private boolean w;
    private JLabel headerLabel;
    private FloatMatrix[] T;
    private DecimalFormat format;

    /**
     * Constructs a <code>NMFFactorViewer</code> with specified T-matrix.
     */
    public NMFFactorViewer(FloatMatrix[] T, float[] cost, boolean w) {
    	this(T, w);
    	this.cost = cost;
    }
    /**
     * Constructs a <code>NMFFactorViewer</code> with specified T-matrix.
     */
    public NMFFactorViewer(FloatMatrix[] T, boolean w) {
		this.T = T;
		this.w = w;
		format = new DecimalFormat();
		format.setMaximumFractionDigits(5);
		format.setMinimumFractionDigits(5);
		format.setGroupingUsed(false);
		
		header  = createHeader(headerLabel=new JLabel("<html><body bgcolor='#FFFFFF'><font face='serif' size='5' color='#000080'><b>Factor</b></font></body></html>"));
		content = createContent();
		setMaxWidth(content, header);
    }
    /**
     * @inheritDoc
     */
    public Expression getExpression(){
    	return new Expression(this, this.getClass(), "new", 
    			new Object[]{T});
    }
    
    /**
     * Updates the viewer with selected node object.
     */
    public void onSelected(IFramework framework) {
		Object userObject = framework.getUserObject();
		updateViewer(((Integer)userObject).intValue());
    }
    
    /**
     * Returns a content of the viewer.
     */
    public JComponent getContentComponent() {
    	return content;
    }
    
    /**
     * Returns a header of the viewer.
     */
    public JComponent getHeaderComponent() {
    	return header;
    }
    
    /**
     * Creates a header component.
     */
    private JComponent createHeader(JLabel label) {
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBackground(Color.white);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(10, 0, 10, 0);
		panel.add(label, gbc);
		return panel;
    }
    
    /**
     * Creates a content component.
     */
    private JTextArea createContent() {
		JTextArea area = new JTextArea(T[0].getRowDimension(), 20);
	        area.setMargin(new Insets(0, 10, 0, 0));       
		area.setEditable(false);
		return area;
    }
    
    /**
     * Updates the viewer for specified gene index.
     */
    private void updateViewer(int gen) {
		headerLabel.setText("<html><body bgcolor='#FFFFFF'><font face='serif' size='5' color='#000080'><b>Factor for run "+String.valueOf(gen+1)+"</b></font></body></html>");
		content.setText("");
		final int rows = T[gen].getRowDimension();
		final int cols = T[gen].getColumnDimension();
		int k = (w?cols:rows);
		if (cost[gen]>=0)
			k=k+2;
		float value;
		float[] values;
		StringBuffer sb = new StringBuffer(rows*10+k);
		if (w){
			values = new float[cols];
			float total = 0;
			for (int j=0; j<cols; j++) {
				values[j] = 0;
				for (int i=0; i<rows; i++) {
					values[j] = values[j] + T[gen].get(i, j);
				}
				total = total+values[j];
			}
			for (int i=0; i<values.length; i++){
				values[i] = (values[i]/total)*100f;
		    	sb.append("Factor "+(i+1)+" importance: " + format.format(values[i])+"\n");
			}
		} else {
			values = new float[rows];
			float total = 0;
			for (int j=0; j<rows; j++) {
				values[j] = 0;
				for (int i=0; i<cols; i++) {
					values[j] = values[j] + T[gen].get(j, i);
				}
				total = total+values[j];
			}
			for (int i=0; i<values.length; i++){
				values[i] = (values[i]/total)*100f;
		    	sb.append("Factor "+(i+1)+" Importance: " + format.format(values[i])+"\n");
			}
		}
		if (cost[gen]>=0)
			sb.append("Cost: "+cost[gen]+"\n");
		sb.append("\n");
		for (int i=0; i<rows; i++) {
			for (int j=0; j<cols; j++) {
			    value = T[gen].get(i, j);
			    if (Float.isNaN(value)) {
			    	sb.append(String.valueOf(Float.NaN)+"\t");
			    } else {
			    	sb.append(format.format(value)+"\t");
			    }
			}
	    	sb.append("\n");
		}
		content.setText(sb.toString());
		content.setCaretPosition(0);
    }
    
    /**
     * Synchronization header and content sizes.
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
