/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: BETRInfoViewer.java,v $
 * $Revision: 1.9 $
 * $Date: 2008-08-06 16:56:57 $
 * $Author: dschlauch $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.attract;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.tigr.microarray.mev.cluster.gui.impl.ViewerAdapter;

/**
 *
 * @author  dschlauch
 * @version 
 */
public class ATTRACTInfoViewer extends ViewerAdapter {
    
    private JComponent header;
    private JTextArea  content;
	private Object[] keggNames;
    public ATTRACTInfoViewer(int[][] clusters, int genes, int dd, int numGroups, Object[] keggNames) {
		this.keggNames = keggNames;
		header  = createHeader();
		content = createContent(clusters, genes);
		setMaxWidth(content, header);        
    }
    public ATTRACTInfoViewer(JTextArea content, JComponent header){
    	this.header = header;
    	this.content = content;
    	setMaxWidth(content, header);        
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
	panel.add(new JLabel("<html><body bgcolor='#FFFFFF'><font face='serif' size='5' color='#000080'><b>Pathway Information</b></font></body></html>"), gbc);
	return panel;
    }
    
    /**
     * Creates the viewer content component.
     */
    private JTextArea createContent(int[][] clusters, int genes) {
		JTextArea area = new JTextArea(clusters.length+5, 20);
		area.setEditable(false);        
	        area.setMargin(new Insets(0, 10, 0, 0));        
		StringBuffer sb = new StringBuffer(clusters.length*3*10);
		for (int counter = 0; counter < clusters.length; counter++) {
		    
			sb.append(keggNames[counter]);
			sb.append("\t\t\t");
	    	
			sb.append("# of Significant Genes: " +clusters[counter].length);
			sb.append("\n");		    
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
