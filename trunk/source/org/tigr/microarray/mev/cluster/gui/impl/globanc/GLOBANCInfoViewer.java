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
package org.tigr.microarray.mev.cluster.gui.impl.globanc;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

import org.tigr.microarray.mev.cluster.gui.impl.ViewerAdapter;
import org.tigr.util.FloatMatrix;

/**
 *
 * @author  dschlauch
 * @version 
 */
public class GLOBANCInfoViewer extends ViewerAdapter {
    
    private JComponent header;
    private JComponent rowheader;
    private JTextArea  content;
    private JTable table;
//    private int dataDesign;
//    private int numGroups;
	private String[] geneListNames;
	private FloatMatrix resultsMatrix;
	
    /** Creates new BETRInfoViewer */
    public GLOBANCInfoViewer(int[][] clusters, String[] geneListNames, FloatMatrix resultsMatrix, int genes, int dd, int numGroups) {
    	this.resultsMatrix = resultsMatrix;
    	this.geneListNames = geneListNames;
//		this.numGroups = numGroups;
//		this.dataDesign = dd;
		createHeader();
		rowheader = createRowHeader();
		content = createContent(clusters, genes);
		setMaxWidth(content, header);        
		this.getRowHeaderComponent();
    }
	public GLOBANCInfoViewer(JTextArea content, JComponent header){
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
    
	private JScrollPane createRowHeader() {
		JTextArea area = new JTextArea(this.resultsMatrix.A.length*3, 20);
		area.setEditable(false);        
	        area.setMargin(new Insets(0, 10, 0, 0));        
		StringBuffer sb = new StringBuffer(this.resultsMatrix.A.length*3*10);
		for (int counter = 0; counter < this.resultsMatrix.A.length; counter++) {

			sb.append(this.geneListNames[counter]);
			sb.append("\n\n");
			
		}
		area.setText(sb.toString());
		area.setCaretPosition(0);
		JScrollPane jsp = new JScrollPane(area);
		jsp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		return jsp;
	    	    
	}
    /**
     * Creates the viewer header.
     */
    private JComponent createHeader() {
    	header = new JPanel(new GridBagLayout());
    	header.setBackground(Color.gray);
        GridBagLayout gridbag = new GridBagLayout();
        header.setLayout(gridbag);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.NONE;
		gbc.gridx = 1;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(10, 0, 10, 0);
		header.add(new JLabel("<html><body bgcolor='#FFFFFF'><font face='serif' size='5' color='#000080'><b>Gene List Information</b></font></body></html>"), gbc);
		JTextArea headerLabels = new JTextArea();
		headerLabels.setEditable(false);        
		StringBuffer sb = new StringBuffer();
		sb.append("Genes");
		sb.append("\t");
		sb.append("F-Value");
		sb.append("\t");
		sb.append("p.perm");
		sb.append("\t");
		sb.append("p.approx");
		headerLabels.setText(sb.toString());
		headerLabels.setCaretPosition(0);
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0, 0, 0, 0);
		header.add(headerLabels,gbc);
		return header;
    }
    
    /**
     * Creates the viewer content component.
     */
    private JTextArea createContent(int[][] clusters, int genes) {
		JTextArea area = new JTextArea(clusters.length*3, 20);
		area.setEditable(false);        
	        area.setMargin(new Insets(0, 10, 0, 0));        
		StringBuffer sb = new StringBuffer(clusters.length*3*10);
		for (int counter = 0; counter < clusters.length; counter++) {
	
//			sb.append(this.geneListNames[counter]);
//			sb.append("\t");
			sb.append((int)resultsMatrix.A[counter][0]);
			sb.append("\t");
			sb.append(resultsMatrix.A[counter][1]);
			sb.append("\t");
			sb.append(resultsMatrix.A[counter][2]);
			sb.append("\t");
			sb.append(resultsMatrix.A[counter][3]);
			sb.append("\n\n");
			
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
        return rowheader;
    }
    
}
