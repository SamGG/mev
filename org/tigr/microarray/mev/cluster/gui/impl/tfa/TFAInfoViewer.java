/*
Copyright @ 1999-2004, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: TFAInfoViewer.java,v $
 * $Revision: 1.2 $
 * $Date: 2004-07-27 19:59:17 $
 * $Author: braisted $
 * $State: Exp $
 */
/*
 * TFAInfoViewer.java
 *
 * Created on February 27, 2004, 2:10 PM
 */

package org.tigr.microarray.mev.cluster.gui.impl.tfa;

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

/**
 *
 * @author  nbhagaba
 */
public class TFAInfoViewer extends ViewerAdapter implements java.io.Serializable {
    public static final long serialVersionUID = 202020010001L;

    private JComponent header;
    private JTextArea  content;  
    String[] factorNames;
    
    /** Creates a new instance of TFAInfoViewer */
    public TFAInfoViewer(int[][] clusters, int genes, String[] factorNames) {
        this.factorNames = factorNames;
	header  = createHeader();
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
        String[] clusterLabels = {factorNames[0] + " significant", factorNames[1] + " significant", "Interaction signficant", factorNames[0] + " non-significant", factorNames[1] + " non-significant", "Interaction non-signficant", "Non-significant for all effects"};
	for (int counter = 0; counter < clusters.length; counter++) {
            sb.append(clusterLabels[counter] + " ");
            sb.append("\t");
            sb.append("# of Genes: " +clusters[counter].length);
            sb.append("\n\t\t");
            sb.append("% of Genes out of total: "+Math.round((float)clusters[counter].length/(float)genes*100f)+"%");
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
        return null;
    }    
    
}
