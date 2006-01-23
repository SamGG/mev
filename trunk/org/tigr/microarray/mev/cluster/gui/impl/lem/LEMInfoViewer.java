/*
Copyright @ 1999-2006, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
package org.tigr.microarray.mev.cluster.gui.impl.lem;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;

import org.tigr.microarray.mev.cluster.gui.impl.ViewerAdapter;

/**
 * @author braisted
 * 
 * LEMInfoViewer displays text describing locus mapping
 */
public class LEMInfoViewer extends ViewerAdapter implements java.io.Serializable {
    
    private JComponent header;
    private JTextPane  content;
    
    private JLabel label;
    
    /**
     * Constructs a <code>LEMInfoViewer</code> with specified
     * clusters and number of genes.
     */
    public LEMInfoViewer(String locusField, String startField, String endField, boolean hasMultipleChr, String chrField, boolean useFileInput, String coordFileName,
    	int totSpotCount, int lemSpotCount, int numberOfMappedSpots, String [] chrNames, int [] mappingCounts, int [] locusCounts) {    	
        header  = createHeader();
        content = createContent(locusField, startField, endField, hasMultipleChr, chrField, useFileInput, coordFileName, totSpotCount, lemSpotCount, numberOfMappedSpots, chrNames, mappingCounts, locusCounts);
        setMaxWidth(content, header);        
    }
        
    /**
     * Returns component to be inserted into the framework scroll pane.
     */
    public JComponent getContentComponent() {
    	//return label;
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
        panel.add(new JLabel("<html><body bgcolor='#FFFFFF'><font face='serif' color='#000080'><h1>LEM Construction Summary<h1></font></body></html>"), gbc);
        return panel;
    }
    
    /**
     * Creates the viewer content component.
     */
    private JTextPane createContent(String locusField, String startField, String endField, boolean hasMultipleChr, String chrField, boolean useFileInput, String coordFileName,
    		int totSpotCount, int lemSpotCount, int numberOfMappedSpots, String [] chrNames, int [] mappingCounts, int [] locusCounts) {

    	JTextPane area = new JTextPane();
        area.setContentType("text/html");
        area.setEditable(false);
        area.setMargin(new Insets(0, 10, 0, 0));
        Font font = new Font("Serif", Font.PLAIN, 10);
        area.setFont(font);
 
        String text = "<html><body><font face=\"sanserif\">";// color='#000080'>";

        text += "<h2>Parameters</h2>";
        text += "<b>Locus ID Annotation Field:</b>"+locusField+"<br>";
        text += "<b>Use Coordinate File:</b>"+(useFileInput?"Yes":"No")+"<br>";
        if(useFileInput)
            text += "<b>Coordinate File:</b>"+coordFileName+"<br>";

        text += "<b>Multiple Chromosomes Indicated:</b>"+(hasMultipleChr?"Yes":"No")+"<br>";
                
        if(!useFileInput) {
        	if(hasMultipleChr)
                text += "<b>Chromosome ID Annotation Field:</b>"+chrField+"<br>";
        		
        	text += "<b>5' End Annotation Field:</b>"+startField+"<br>";
            text += "<b>3' End Annotation Field:</b>"+endField+"<br>";        	
        }
        
        text += "<h2>Global Mapping Information</h2>";
        if(totSpotCount != lemSpotCount) {
            text += "<b>Number of Spots Loaded in MeV: </b>"+String.valueOf(totSpotCount)+"<br>";
        	text += "<b>Number of Spots Entering LEM (after filtering): </b>"+String.valueOf(lemSpotCount)+"<br>";
        } else {
        	text += "<b>Number of Spots Entering LEM: </b>"+String.valueOf(lemSpotCount)+"<br>";        	
        }
        
        text += "<b>Number of Spots Mapped to Loci: </b>"+String.valueOf(numberOfMappedSpots)+"<br>";
        text += "<b>Fraction of Spots Mapped to Loci: </b>"+String.valueOf((numberOfMappedSpots)/(float)lemSpotCount)+"<br>";        
        text += "<b>Number of Unique Chromosome IDs: </b>"+String.valueOf(locusCounts.length)+"<br>";
        
        text += "<h2>Chromosomal Mapping Information</h2>";
        
        for(int i = 0; i < chrNames.length; i++) {
        	text += "<b>Chromosome ID:</b>"+(String)chrNames[i]+"<br>";
        	text += "<b>Number of Unique Locus IDs Found:</b>"+String.valueOf(locusCounts[i])+"<br>";
        	text += "<b>Number of Spots Mapped to Loci on Chromosome:</b>"+String.valueOf(mappingCounts[i])+"<br><br>";
        }
        
        text += "</font></body></html>";
        
        area.setText(text);
        area.setCaretPosition(0);
        
        label = new JLabel(text);
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
