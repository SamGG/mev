/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: KMCInfoViewer.java,v $
 * $Revision: 1.8 $
 * $Date: 2006-06-30 17:52:19 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.kmc;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.beans.Expression;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.tigr.microarray.mev.cluster.gui.impl.ViewerAdapter;

public class KMCInfoViewer extends ViewerAdapter implements java.io.Serializable {
//    public static final long serialVersionUID = 202007020001L;
    
    private JComponent header;
    private JTextArea  content;
    private boolean clusterGenes;
    
    public JTextArea getContent(){return content;}
    
    /**
     * Constructs a <code>KMCInfoViewer</code> with specified
     * clusters and number of genes.
     */
    public KMCInfoViewer(int[][] clusters, int genes, int [] convIteration) {
        header  = createHeader();
        this.clusterGenes = true;
        content = createContent(clusters, genes, convIteration);
        setMaxWidth(content, header);        
    }
    
    /**
     * Constructs a <code>KMCInfoViewer</code> with specified
     * clusters and number of genes.
     */
    public KMCInfoViewer(int[][] clusters, int genes, int [] convIteration, boolean clusterGenes) {
        header  = createHeader();
        this.clusterGenes = clusterGenes;
        content = createContent(clusters, genes, convIteration);
        setMaxWidth(content, header);
    }
    /**
     * This constructor is used by the state-saving code for class persistence. 
     * Do not delete or alter.
     * 
     * @param content
     * @param clusterGenes
     */
    public KMCInfoViewer(JTextArea content, Boolean clusterGenes) { 	 
        this(content, clusterGenes.booleanValue()); 	 
    }
    /**
     * This constructor is used by the state-saving code for class persistence. 
     * Do not delete or alter.
     * 
     * @param content
     * @param clusterGenes
     */
    public KMCInfoViewer(JTextArea content, boolean clusterGenes) { 	 
        header = createHeader(); 	 
        this.content = content; 	 
        this.clusterGenes = clusterGenes; 	 
        setMaxWidth(content, header); 	 
    }
    
    /**
     * @inheritDoc
     * 
     */
    public Expression getExpression(){
    	return new Expression(this, this.getClass(), "new", 
    			new Object[]{this.getContentComponent(), new Boolean(this.clusterGenes)});
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
        panel.add(new JLabel("<html><body bgcolor='#FFFFFF'><font face='serif' size='5' color='#000080'><b>Cluster Information</b></font></body></html>"), gbc);
        return panel;
    }
    
    /**
     * Creates the viewer content component.
     */
    private JTextArea createContent(int[][] clusters, int genes, int [] convIter) {
        JTextArea area = new JTextArea(clusters.length*3, 20);
        area.setEditable(false);
        area.setMargin(new Insets(0, 10, 0, 0));
        StringBuffer sb = new StringBuffer(clusters.length*3*10);
        if(clusterGenes){
            for (int counter = 0; counter < clusters.length; counter++) {
                sb.append("Cluster "+(counter+1));
                sb.append("\t");
                sb.append("# of Genes in Cluster: "+clusters[counter].length);
                sb.append("\n\t");
                sb.append("% of Genes in Cluster: "+Math.round((float)clusters[counter].length/(float)genes*100f)+"%");
                sb.append("\n\t");
                sb.append("Last Iteration with Gene Exchange: "+convIter[counter]);                
                sb.append("\n\n");
            }
        }
        else{
            for (int counter = 0; counter < clusters.length; counter++) {
                sb.append("Cluster "+(counter+1));
                sb.append("\t");
                sb.append("# of Experiments in Cluster: "+clusters[counter].length);
                sb.append("\n\t");
                sb.append("% of Experiments in Cluster: "+Math.round((float)clusters[counter].length/(float)genes*100f)+"%");
                sb.append("\n\t");
                sb.append("Last Iteration with Experiment Exchange: "+convIter[counter]);                
                sb.append("\n\n");
            }
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
