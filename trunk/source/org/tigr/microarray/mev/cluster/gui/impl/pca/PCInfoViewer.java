/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: PCInfoViewer.java,v $
 * $Revision: 1.7 $
 * $Date: 2006-03-24 15:51:05 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.pca;

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

public class PCInfoViewer extends ViewerAdapter {
    
    private JComponent header;
    private JTextArea  content;
    
    private JLabel headerLabel;
    private FloatMatrix T;
    private DecimalFormat format;
    
    /**
     * Constructs a <code>PCInfoViewer</code> with specified T-matrix.
     */
    public PCInfoViewer(FloatMatrix T) {
	this.T = T;
	format = new DecimalFormat();
	format.setMaximumFractionDigits(5);
	format.setMinimumFractionDigits(5);
	format.setGroupingUsed(false);
	
	header  = createHeader(headerLabel=new JLabel("<html><body bgcolor='#FFFFFF'><font face='serif' size='5' color='#000080'><b>Eigenvector</b></font></body></html>"));
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
	JTextArea area = new JTextArea(T.getRowDimension(), 20);
        area.setMargin(new Insets(0, 10, 0, 0));       
	area.setEditable(false);
	return area;
    }
    
    /**
     * Updates the viewer for specified gene index.
     */
    private void updateViewer(int gen) {
	headerLabel.setText("<html><body bgcolor='#FFFFFF'><font face='serif' size='5' color='#000080'><b>Eigenvector "+String.valueOf(gen+1)+"</b></font></body></html>");
	content.setText("");
	final int rows = T.getRowDimension();
	StringBuffer sb = new StringBuffer(rows*10);
	float value;
	for (int i=0; i<rows; i++) {
	    value = T.get(i, gen);
	    if (Float.isNaN(value)) {
		sb.append(String.valueOf(Float.NaN)+"\n");
	    } else {
		sb.append(format.format(value)+"\n");
	    }
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
