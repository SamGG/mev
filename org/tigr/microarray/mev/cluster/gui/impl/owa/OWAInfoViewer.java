/*
Copyright @ 1999-2004, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: OWAInfoViewer.java,v $
 * $Revision: 1.4 $
 * $Date: 2004-07-27 19:59:16 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.owa;


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
 * @version 
 */
public class OWAInfoViewer extends ViewerAdapter implements java.io.Serializable {
    public static final long serialVersionUID = 202010020001L;
    
    private JComponent header;
    private JTextArea  content;
    /** Creates new OWAInfoViewer */
    public OWAInfoViewer(int[][] clusters, int genes) {
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
	for (int counter = 0; counter < clusters.length; counter++) {
	    if (counter == 0) {
		sb.append("Significant genes ");
		sb.append("\t");
		sb.append("# of Significant Genes: " +clusters[counter].length);
		sb.append("\n\t\t");
		sb.append("% of Genes that are Signficant: "+Math.round((float)clusters[counter].length/(float)genes*100f)+"%");
		sb.append("\n\n");
	    } else {
		sb.append("Non-significant genes ");
		sb.append("\t");
		sb.append("# of non-significant Genes: " +clusters[counter].length);
		sb.append("\n\t\t");
		sb.append("% of Genes that are not signficant: "+Math.round((float)clusters[counter].length/(float)genes*100f)+"%");
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
