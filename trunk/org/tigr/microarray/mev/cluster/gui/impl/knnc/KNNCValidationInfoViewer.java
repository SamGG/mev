/*
Copyright @ 1999-2004, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * KNNCValidationInfoViewer.java
 *
 * Created on May 17, 2004, 12:11 PM
 */

package org.tigr.microarray.mev.cluster.gui.impl.knnc;

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
public class KNNCValidationInfoViewer extends ViewerAdapter implements java.io.Serializable {
    public static final long serialVersionUID = 202009020001L;
    
    private JComponent header;
    private JTextArea  content;
    private int[] origNumInFiltTrgSetByClass, numberCorrectlyClassifiedByClass, numberIncorrectlyClassifiedByClass;
    
    /** Creates a new instance of KNNCValidationInfoViewer */
    public KNNCValidationInfoViewer(int[] origNumInFiltTrgSetByClass, int[] numberCorrectlyClassifiedByClass, int[] numberIncorrectlyClassifiedByClass) {
        this.origNumInFiltTrgSetByClass = origNumInFiltTrgSetByClass;
        this.numberCorrectlyClassifiedByClass = numberCorrectlyClassifiedByClass;
        this.numberIncorrectlyClassifiedByClass = numberIncorrectlyClassifiedByClass;
        header  = createHeader();
        content = createContent();
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
        panel.add(new JLabel("<html><body bgcolor='#FFFFFF'><font face='serif' size='5' color='#000080'><b>Leave one out cross-validation (LOOCV) statistics</b></font></body></html>"), gbc);        
        return panel;
    }   
    
    private JTextArea createContent(){
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setMargin(new Insets(0, 10, 0, 0));  
        StringBuffer sb = new StringBuffer();
        for (int i = 1; i < this.origNumInFiltTrgSetByClass.length; i++) {
            sb.append("Class " + i + ":\n\t");
            sb.append("Original number* of training set elements in class = " + this.origNumInFiltTrgSetByClass[i] + "\n\t");
            sb.append("Number of training set elements correctly assigned to class by LOOCV = " + this.numberCorrectlyClassifiedByClass[i] + "\n\t");
            sb.append("Number of training set elements falsely assigned to class by LOOCV = " + this.numberIncorrectlyClassifiedByClass[i] + "\n");
        }
        sb.append("\n\n*Indicates number of training set elements in a given class after variance filtering, if applied.");
        
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
