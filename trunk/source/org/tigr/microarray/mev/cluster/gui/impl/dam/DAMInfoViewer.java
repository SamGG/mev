/*
Copyright @ 1999-2005, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * DAMInfoViewer.java
 *
 */

package org.tigr.microarray.mev.cluster.gui.impl.dam;

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

public class DAMInfoViewer extends ViewerAdapter implements java.io.Serializable {
    
    private JComponent header;
    private JTextArea  content;
    private boolean clusterGenes;
    private int numClasses;
    private int numUsedGenes;
    private int numUnUsedGenes;
    private double alpha;
    private int algorithmSelection;
    private boolean isPDA;
    private boolean preSelectGenes;
    
    /**
     * Constructs a <code>KMCInfoViewer</code> with specified
     * clusters and number of genes.
     */
    public DAMInfoViewer(int[][] clusters, int genes, int numClasses, int numUsedGenes, int numUnUsedGenes, double alpha, int algorithmSelection, boolean isPDA, boolean preSelectGenes) {
        header  = createHeader();
        this.clusterGenes = true;
        this.numClasses = numClasses;
        this.numUsedGenes = numUsedGenes;
        this.numUnUsedGenes = numUnUsedGenes;
        this.alpha = alpha;
        this.algorithmSelection = algorithmSelection;
        this.isPDA = isPDA;
        this.preSelectGenes = preSelectGenes;
        content = createContent(clusters, genes);
        setMaxWidth(content, header);
    }
    public DAMInfoViewer(JTextArea content, JComponent header){
    	this.content = content;
    	this.header = header;
        setMaxWidth(content, header);
    }
    
    /**
     * Constructs a <code>KMCInfoViewer</code> with specified
     * clusters and number of genes.
     */
    public DAMInfoViewer(int[][] clusters, int genes, boolean clusterGenes, int numClasses, int numUsedGenes, int numUnUsedGenes, double alpha, int algorithmSelection, boolean isPDA, boolean preSelectedGenes) {
        header  = createHeader();
        this.clusterGenes = clusterGenes;
        this.numClasses = numClasses;
        this.numUsedGenes = numUsedGenes;
        this.numUnUsedGenes = numUnUsedGenes;
        this.alpha = alpha;
        this.algorithmSelection = algorithmSelection;
        this.isPDA = isPDA;
        this.preSelectGenes = preSelectedGenes;
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
        area.setMargin(new Insets(0, 10, 0, 0));
        StringBuffer sb = new StringBuffer(clusters.length*3*10);
        int numberOfGenes = numUsedGenes + numUnUsedGenes;
        
        if(clusterGenes){
            for (int counter = 2*numClasses; counter < 3*numClasses; counter++) {
                sb.append("Class "+(counter+1 - 2*numClasses));
                sb.append("\t");
                sb.append("# of Genes in Class: "+clusters[counter].length);
                sb.append("\n\t");
                sb.append("% of Genes in Class: "+Math.round((float)clusters[counter].length/(float)genes*100f)+"%");
                sb.append("\n\n");
            }
            if (preSelectGenes) {
                sb.append("alpha value =" + alpha);
                
                sb.append("\n\n");
                sb.append("# of Experiments: " + numberOfGenes);
                sb.append("\n");
                sb.append("# of Selected Experiments: " + numUsedGenes);
                sb.append("\n");
                sb.append("# of Experiments not Selected: " + numUnUsedGenes);
                sb.append("\n");
            } else {
                sb.append("No Experiment Screening Applied");
            }
            
        }
        else{
            for (int counter = 2*numClasses; counter < 3*numClasses; counter++) {
                sb.append("Class "+(counter+1 - 2*numClasses));
                sb.append("\t");
                sb.append("# of Experiments in Class: "+clusters[counter].length);
                sb.append("\n\t");
                sb.append("% of Experiments in Class: "+Math.round((float)clusters[counter].length/(float)genes*100f)+"%");
                sb.append("\n\n");
            }
            if (preSelectGenes) {
                sb.append("alpha value =" + alpha);
                
                sb.append("\n\n");
                sb.append("# of Genes: " + numberOfGenes);
                sb.append("\n");
                sb.append("# of Selected Genes: " + numUsedGenes);
                sb.append("\n");
                sb.append("# of Genes not Selected: " + numUnUsedGenes);
                sb.append("\n");
            } else {
                sb.append("No Gene Screening Applied");
            }
        }
        
        sb.append("\n");
        
        switch (algorithmSelection) {
            case 0:
                sb.append("Classification Algorithm: A0");
                break;
            case 1:
                sb.append("Classification Algorithm: A1");
                break;
            case 2:
                sb.append("Classification Algorithm: A2");
                break;
            case 3:
                sb.append("Classification Algorithm: Initial Classification");
                break;
            default:
                break;
        }
        sb.append("\n");
        sb.append("\n");
        if (isPDA) {
            sb.append("Classification Method: PDA");
        } else {
            sb.append("Classification Method: QDA");
        }
        sb.append("\n\n");
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
}

