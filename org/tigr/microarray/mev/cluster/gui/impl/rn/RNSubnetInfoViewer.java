/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * $RCSfile: RNSubnetInfoViewer.java,v $
 * $Revision: 1.2 $
 * $Date: 2003-12-08 18:36:58 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.rn;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.tigr.microarray.mev.cluster.gui.impl.ViewerAdapter;

public class RNSubnetInfoViewer extends ViewerAdapter {
    
    private JComponent header;
    private JTextArea  content;
    private boolean clusterGenes;
    private int [] orderedIndices;
    
    /**
     * Constructs a <code>RNSubnetInfoViewer</code> with specified
     * clusters and number of genes.
     */
    public RNSubnetInfoViewer(int[][] clusters, int genes) {
        header  = createHeader();
        this.clusterGenes = true;
        content = createContent(clusters, genes);
        setMaxWidth(content, header);
    }
    
    /**
     * Constructs a <code>RNSubnetInfoViewer</code> with specified
     * clusters and number of genes.
     */
    public RNSubnetInfoViewer(int[][] clusters, int [] orderedIndices, int genes, boolean clusterGenes) {
        this.orderedIndices = orderedIndices;
        header  = createHeader();
        this.clusterGenes = clusterGenes;
        content = createContent(clusters, genes);
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
        panel.add(new JLabel("<html><body bgcolor='#FFFFFF'><font face='serif' size='5' color='#000080'><b>Subnet Information</b></font></body></html>"), gbc);
        return panel;
    }
    
    /**
     * Creates the viewer content component.
     */
    private JTextArea createContent(int[][] clusters, int genes) {
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setMargin(new Insets(0, 10, 0, 0));
        StringBuffer sb = new StringBuffer(clusters.length*6*25);
        int stringLength = 0;
        String dummy = "";
        if(clusterGenes){
            for (int counter = 0; counter < clusters.length; counter++) {
                dummy = "";
                if(clusters[orderedIndices[counter]].length > 1){
                    dummy += ("Subnet "+(counter+1));
                    dummy += ("\t");
                    dummy += ("# of Genes in Subnet: "+clusters[orderedIndices[counter]].length);
                    dummy += ("\n\t");
                    dummy += ("% of Genes in Subnet: "+Math.round((float)clusters[orderedIndices[counter]].length/(float)genes*100f)+"%");
                    dummy += ("\n\n");
                    sb.append(dummy);
                    stringLength += dummy.length();
                }
            }
        }
        else{
            for (int counter = 0; counter < clusters.length; counter++) {
                dummy = "";
                if(clusters[orderedIndices[counter]].length > 1){
                    dummy += ("Subnet "+(counter+1));
                    dummy += ("\t");
                    dummy += ("# of Experiments in Subnet: "+clusters[orderedIndices[counter]].length);
                    dummy += ("\n\t");
                    dummy += ("% of Experiments in Subnet: "+Math.round((float)clusters[orderedIndices[counter]].length/(float)genes*100f)+"%");
                    dummy += ("\n\n");
                                        sb.append(dummy);
                    stringLength += dummy.length();
                }
            }
        }
        String str = sb.substring(0, stringLength);
        area.setText(str);
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
