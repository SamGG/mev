/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: ValuesViewer.java,v $
 * $Revision: 1.2 $
 * $Date: 2003-12-08 18:16:07 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.pca;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Color;
import java.awt.Dimension;
import java.text.DecimalFormat;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.tigr.util.FloatMatrix;
import org.tigr.microarray.mev.cluster.gui.impl.ViewerAdapter;

public class ValuesViewer extends ViewerAdapter {
    
    private JComponent header;
    private JComponent content;
    
    /**
     * Constructs a <code>ValuesViewer</code> with specified S-matrix.
     */
    public ValuesViewer(FloatMatrix S) {
	content = createContent(S);
	header = createHeader();
	setMaxWidth(content, header);
    }
    
    /**
     * Returns the viewer content.
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
	JLabel label = new JLabel("<html><body bgcolor='#FFFFFF'><font face='serif' size='5' color='#000080'><b>Eigenvalues</b></font></body></html>");
	GridBagConstraints gbc = new GridBagConstraints();
	gbc.fill = GridBagConstraints.HORIZONTAL;
	gbc.insets = new Insets(10, 0, 10, 0);
	panel.add(label, gbc);
	return panel;
    }
    
    /**
     * Creates the viewer content.
     */
    private JComponent createContent(FloatMatrix S) {
	JTextArea area = new JTextArea();
	area.setEditable(false);
        area.setMargin(new Insets(0, 10, 0, 0));	
	DecimalFormat format = new DecimalFormat();
	format.setMinimumIntegerDigits(2);
	format.setMaximumFractionDigits(3);
	format.setMinimumFractionDigits(3);
	format.setGroupingUsed(false);
	float sum = 0f;
	final int columns = S.getColumnDimension();
	for (int i=0; i<columns; i++) {
	    sum += S.get(i, i);
	}
	float factor = 100f/sum;
	for (int i=0; i<columns; i++) {
	    area.append("Principal Component "+new Integer(i+1).toString()+"\t"+format.format(S.get(i,i))+"\t"+format.format(S.get(i,i)*factor)+" %\n");
	}
	if (columns > 1) {
	    area.append("\n");
	    area.append("First 2 components: "+format.format((S.get(0,0)+S.get(1,1))*factor)+" %\n");
	    if (columns > 2) {
		area.append("First 3 components: "+format.format((S.get(0,0)+S.get(1,1)+S.get(2,2))*factor)+" %\n");
	    }
	}
	return area;
    }
    
    /**
     * Synchronize header and content sizes.
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
