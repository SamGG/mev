/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: SOMInfoViewer.java,v $
 * $Revision: 1.3 $
 * $Date: 2004-02-05 21:11:04 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.som;

import java.awt.Color;
import java.awt.Insets;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JComponent;

import org.tigr.microarray.mev.cluster.gui.impl.ViewerAdapter;

public class SOMInfoViewer extends ViewerAdapter implements java.io.Serializable {
    
    private JComponent header;
    private JTextArea  content;
    private boolean clusterGenes = true;
    /**
     * Constructs a <code>KMCInfoViewer</code> with specified
     * clusters and number of genes.
     */
    public SOMInfoViewer(int[][] clusters, int genes) {
        header  = createHeader();
        content = createContent(clusters, genes);
        setMaxWidth(content, header);
    }
    
    /**
     * Constructs a <code>KMCInfoViewer</code> with specified
     * clusters and number of genes.
     */
    public SOMInfoViewer(int[][] clusters, int genes, boolean clusterGenes) {
        this.clusterGenes = clusterGenes;
        header  = createHeader();
        content = createContent(clusters, genes);
        setMaxWidth(content, header);
    }
    
    /**
     * Returns the component to be inserted into the framework scroll pane.
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
    private JTextArea createContent(int[][] clusters, int genes) {
        JTextArea area = new JTextArea(clusters.length*3, 20);
        area.setMargin(new Insets(0, 10, 0, 0));
        area.setEditable(false);
        StringBuffer sb = new StringBuffer(clusters.length*3*10);
        if(clusterGenes){
            for (int counter = 0; counter < clusters.length; counter++) {
                sb.append("Cluster "+(counter+1));
                sb.append("\t");
                sb.append("# of Genes in Cluster: "+clusters[counter].length);
                sb.append("\n\t");
                sb.append("% of Genes in Cluster: "+Math.round((float)clusters[counter].length/(float)genes*100f)+"%");
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
