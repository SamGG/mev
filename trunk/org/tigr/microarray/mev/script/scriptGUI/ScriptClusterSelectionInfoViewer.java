/*
Copyright @ 1999-2004, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * $RCSfile: ScriptClusterSelectionInfoViewer.java,v $
 * $Revision: 1.2 $
 * $Date: 2004-07-27 20:02:27 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.microarray.mev.script.scriptGUI;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;

import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.gui.impl.ViewerAdapter;

/** The ScriptClusterSelectionInfoViewer presents the results of cluster selection algorithms
 * and the criteria upon which the selections were made.
 */
public class ScriptClusterSelectionInfoViewer extends ViewerAdapter implements java.io.Serializable {
    public static final long serialVersionUID = 1000102010301020001L;
    
    private JComponent header;
    private JTextPane  content;
    private boolean clusterGenes;
    
    /** Constructs a <code>ScriptClusterSelectionInfoViewer</code> with specified
     * clusters and number of genes.
     * @param algData
     */
    public ScriptClusterSelectionInfoViewer(AlgorithmData algData) {
        header  = createHeader();
        this.clusterGenes = algData.getParams().getBoolean("process-gene-clusters");
        content = createContent(algData);
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
        panel.add(new JLabel("<html><body bgcolor='#FFFFFF'><font face='serif' size='5' color='#000080'><b>Cluster Selection Information</b></font></body></html>"), gbc);
        return panel;
    }
    
    /**
     * Creates the viewer content component.
     */
    private JTextPane createContent(AlgorithmData algData) {
        
        String algName = algData.getParams().getString("name");
        int numOfDesiredClusters = algData.getParams().getInt("desired-cluster-count");
        String [] div = algData.getStringArray("diversity-value-array");
        String [] pop = algData.getStringArray("cluster-population-array");
        int popLimit = algData.getParams().getInt("minimum-cluster-size");
        
        JTextPane area = new JTextPane();
        area.setContentType("text/html");
        area.setEditable(false);
        area.setMargin(new Insets(0, 10, 0, 0));
        
        String text;
        
        int population;
        
        if(algName.equals("Diversity Ranking Cluster Selection")) {
            boolean useCentroid = algData.getParams().getBoolean("use-centroid-based-variability");
            
            text = "<html><body bgcolor='#FFFFFF'><font face='serif' size='5'>";
            text += "<br>Number of Desired Clusters: "+numOfDesiredClusters+"<br>";
            text += "Minimum Cluster Size (population): "+popLimit+"<br>";
            if(useCentroid)
                text += "Diversity Measurement: Centroid Based Diversity (mean gene-to-centroid dist.)<br>";
            else
                text += "Diversity Measurement: Intra-gene Based Diversity (mean gene-to-gene dist.)<br>";
            
            text += "<br><br>Note: Clusters are sorted by diversity.  Selected clusters are in <b>bold</b> type.<br>";
            
            
            text += "<table cellpadding=10><th><u>Div. Rank</u></th><th><u>Diversity</u></th><th><u>Population</u></td>";
            int clusterCount = 0;
            for(int i = 0; i < div.length; i++) {
                population = Integer.parseInt(pop[i]);
                if(population >= popLimit && clusterCount < numOfDesiredClusters) {
                    text += "<tr align=center><td><b>"+(i+1)+"</b></td><td><b>"+div[i]+"</b></td><td><b>"+pop[i]+"</b></td></tr>";
                    clusterCount++;
                }
                else
                    text += "<tr align=center><td>"+(i+1)+"</td><td>"+div[i]+"</td><td>"+pop[i]+"</td></tr>";
            }
                text += "</table></body></html>";
        } else {
            boolean useVariance = algData.getParams().getBoolean("use-centroid-variance");
            
            text = "<html><body bgcolor='#FFFFFF'><font face='serif' size='5'>";
            text += "<br>Number of Desired Clusters: "+numOfDesiredClusters+"<br>";
            text += "Minimum Cluster Size (population): "+popLimit+"<br>";
            if(useVariance) {
                text += "Selection Criteria: Centroid Variance<br>";
                text += "<br><br>Note: Clusters are sorted by decreasing variance.  Selected clusters are in <b>bold</b> type.<br>";
            text += "<table cellpadding=10><th><u>Var. Rank</u></th><th><u>Variance</u></th><th><u>Population</u></td>";
            } else {
                text += "Selection Criteria: Centroid Entropy<br>";            
                text += "<br><br>Note: Clusters are sorted by decreasing entropy.  Selected clusters are in <b>bold</b> type.<br>";
            text += "<table cellpadding=10><th><u>Entropy Rank</u></th><th><u>Entropy</u></th><th><u>Population</u></td>";
            }
            
            int clusterCount = 0;
            for(int i = 0; i < div.length; i++) {
                population = Integer.parseInt(pop[i]);
                if(population >= popLimit && clusterCount < numOfDesiredClusters) {
                    text += "<tr align=center><td><b>"+(i+1)+"</b></td><td><b>"+div[i]+"</b></td><td><b>"+pop[i]+"</b></td></tr>";
                    clusterCount++;
                }
                else
                    text += "<tr align=center><td>"+(i+1)+"</td><td>"+div[i]+"</td><td>"+pop[i]+"</td></tr>";
            }
            text += "</table></body></html>";
        }
        
        area.setText(text);
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
