/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: KMCSuppInfoViewer.java,v $
 * $Revision: 1.2 $
 * $Date: 2003-12-08 18:38:09 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.kmcs;

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

public class KMCSuppInfoViewer extends ViewerAdapter {
    
    private JComponent header;
    private JTextArea  content;
    private boolean clusterGenes;
    private boolean unassignedExists;
    
    /**
     * Constructs a <code>KMCSuppInfoViewer</code> with specified
     * clusters and number of genes.
     */
    public KMCSuppInfoViewer(int[][] clusters, int genes) {
        header  = createHeader();
        this.clusterGenes = true;
        content = createContent(clusters, genes);
        setMaxWidth(content, header);
    }
    
    /**
     * Constructs a <code>KMCSuppInfoViewer</code> with specified
     * clusters and number of genes.
     */
    public KMCSuppInfoViewer(int[][] clusters, int genes, boolean unassignedExists,boolean clusterGenes) {
        header  = createHeader();
        this.unassignedExists = unassignedExists;
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
        panel.add(new JLabel("<html><body bgcolor='#FFFFFF'><font face='serif' size='5' color='#000080'><b>Cluster Information</b></font></body></html>"), gbc);
        return panel;
    }
    
    /**
     * Creates the viewer content component.
     */
    private JTextArea createContent(int[][] clusters, int genes) {
        JTextArea area = new JTextArea(clusters.length*3, 20);
        area.setEditable(false);
        area.setMargin(new Insets(0,10,0,0));
        StringBuffer sb = new StringBuffer(clusters.length*3*10);
        int counter;
        if(clusterGenes){
            if(!unassignedExists){
                for (counter = 0; counter < clusters.length; counter++) {
                    sb.append("Cluster "+(counter+1));
                    sb.append("\t");
                    sb.append("# of Genes in Cluster: "+clusters[counter].length);
                    sb.append("\n\t");
                    sb.append("% of Genes in Cluster: "+Math.round((float)clusters[counter].length/(float)genes*100f)+"%");
                    sb.append("\n\n");
                }
            }
            else{
                for (counter = 0; counter < clusters.length-1; counter++) {
                    sb.append("Cluster "+(counter+1));
                    sb.append("\t");
                    sb.append("# of Genes in Cluster: "+clusters[counter].length);
                    sb.append("\n\t");
                    sb.append("% of Genes in Cluster: "+Math.round((float)clusters[counter].length/(float)genes*100f)+"%");
                    sb.append("\n\n");
                }
                sb.append("Unassigned Genes ");
                sb.append("\n\t");
                sb.append("# of Unassighed Genes: "+clusters[counter].length);
                sb.append("\n\t");
                sb.append("% of Genes Unassigned: "+Math.round((float)clusters[counter].length/(float)genes*100f)+"%");
                sb.append("\n\n");
            }
            
        }
        else{
            
            if(!unassignedExists){
                for (counter = 0; counter < clusters.length; counter++) {
                    sb.append("Cluster "+(counter+1));
                    sb.append("\t");
                    sb.append("# of Experiments in Cluster: "+clusters[counter].length);
                    sb.append("\n\t");
                    sb.append("% of Experiments in Cluster: "+Math.round((float)clusters[counter].length/(float)genes*100f)+"%");
                    sb.append("\n\n");
                }
            } else {
                for (counter = 0; counter < clusters.length-1; counter++) {
                    sb.append("Cluster "+(counter+1));
                    sb.append("\t");
                    sb.append("# of Experiments in Cluster: "+clusters[counter].length);
                    sb.append("\n\t");
                    sb.append("% of Experiments in Cluster: "+Math.round((float)clusters[counter].length/(float)genes*100f)+"%");
                    sb.append("\n\n");
                }
                sb.append("Unassigned Experiments ");
                sb.append("\n\t");
                sb.append("# of Unassiged Experiments: "+clusters[counter].length);
                sb.append("\n\t");
                sb.append("% of Experiments Unassigned: "+Math.round((float)clusters[counter].length/(float)genes*100f)+"%");
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
