/*
Copyright @ 1999-2004, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * $RCSfile: RNElementSeedInfoViewer.java,v $
 * $Revision: 1.3 $
 * $Date: 2004-07-27 19:59:16 $
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

import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.impl.ViewerAdapter;

public class RNElementSeedInfoViewer extends ViewerAdapter implements java.io.Serializable {
    public static final long serialVersionUID = 202014030001L;
    
    private JComponent header;
    private JTextArea  content;
    private boolean clusterGenes;
    private Experiment experiment;
    private int [] indices;
    /**
     * Constructs a <code>RNElementSeedInfoViewer</code> with specified
     * clusters and number of genes.
     */
    public RNElementSeedInfoViewer(int[][] clusters, int genes) {
        header  = createHeader();
        this.clusterGenes = true;
        content = createContent(clusters, genes);
        setMaxWidth(content, header);
    }
    
    /**
     * Constructs a <code>RNElementSeedInfoViewer</code> with specified
     * clusters and number of genes.
     */
    public RNElementSeedInfoViewer(int[][] clusters,  Experiment experiment, int [] indices, int genes, boolean clusterGenes) {
        this.indices = indices;
        this.experiment = experiment;
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
        panel.add(new JLabel("<html><body bgcolor='#FFFFFF'><font face='serif' size='5' color='#000080'><b>Element Seed Cluster Information</b></font></body></html>"), gbc);
        return panel;
    }
    
    /**
     * Creates the viewer content component.
     */
    private JTextArea createContent(int[][] clusters, int genes) {
        //JTextArea area = new JTextArea(clusters.length*3*10, 20);
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setMargin(new Insets(0, 10, 0, 0));
        StringBuffer sb = new StringBuffer(clusters.length*6*25);
        String dummy = "";
        int stringLength = 0;
        if(clusterGenes){
            for (int counter = 0; counter < clusters.length; counter++) {
                dummy = "";
                if(clusters[indices[counter]].length > 1){                    
                    dummy += ("Seed Gene Index "+(experiment.getGeneIndexMappedToData(clusters[counter][0])+1));
                    dummy += ("\t");
                    dummy += ("# of Genes in Cluster: "+clusters[indices[counter]].length);
                    dummy += ("\n\t\t");
                    dummy += ("% of Genes in Cluster: "+Math.round((float)clusters[indices[counter]].length/(float)genes*100f)+"%");
                    dummy += ("\n\n");
                    sb.append(dummy);
                    stringLength += dummy.length();
                }
            }
        }
        else{
            for (int counter = 0; counter < clusters.length; counter++) {
                dummy = "";
                if(clusters[indices[counter]].length > 1){
                    dummy += ("Seed Experiment Index "+(clusters[counter][0]+1));
                    dummy += ("\t");
                    dummy += ("# of Experiments in Cluster: "+clusters[indices[counter]].length);
                    dummy += ("\n\t\t");
                    dummy += ("% of Experiments in Cluster: "+Math.round((float)clusters[indices[counter]].length/(float)genes*100f)+"%");
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
