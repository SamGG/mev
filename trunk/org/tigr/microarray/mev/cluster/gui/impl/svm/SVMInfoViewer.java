/*
Copyright @ 1999-2004, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: SVMInfoViewer.java,v $
 * $Revision: 1.4 $
 * $Date: 2004-07-27 19:59:17 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.svm;

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

public class SVMInfoViewer extends ViewerAdapter implements java.io.Serializable {
    public static final long serialVersionUID = 202018050001L;
    
    private JComponent header;
    private JTextArea  content;
    private boolean classifyGenes = true;
    private int svmMode = 0;
    
    
    
    private int numberOfPositives = 0;
    private int numberOfTruePositives = 0;
    private int numberOfFalseNegatives = 0;
    
    private int numberOfNegatives = 0;
    private int numberOfTrueNegatives = 0;
    private int numberOfFalsePositives = 0;
    
    
    /**
     * Constructs a <code>SVMInfoViewer</code>
     */
    public SVMInfoViewer(int numPosExamples, int numNegExamples, int numNeutExamples, int numPos, int numTruePos, int numFalseNeg, int numNeg, int numTrueNeg, int numFalsePos, int numPosRecFromNeut, int numNegRecFromNeut, boolean classifyGenes, int svmMode) {
        header  = createHeader();
        this.classifyGenes = classifyGenes;
        this.svmMode = svmMode;
        content = createContent(numPosExamples, numNegExamples, numNeutExamples, numPos, numTruePos, numFalseNeg, numNeg, numTrueNeg, numFalsePos, numPosRecFromNeut, numNegRecFromNeut);
        setMaxWidth(content, header);
    }
    
    /**
     * Constructs a <code>SVMInfoViewer</code>  when initial classification is unknown, SVMMode == CLASSIFY_ONLY
     */
    public SVMInfoViewer(int numPos, int numNeg, boolean classifyGenes, int svmMode) {
        header  = createHeader();
        this.classifyGenes = classifyGenes;
        this.svmMode = svmMode;
        content = createContent(0, 0, 0, numPos, 0, 0, numNeg, 0, 0, 0, 0);
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
        panel.add(new JLabel("<html><body bgcolor='#FFFFFF'><font face='serif' size='5' color='#000080'><b>Classification Information</b></font></body></html>"), gbc);
        return panel;
    }
    
    /**
     * Creates the viewer content component.
     */
    private JTextArea createContent(int numPosExamples, int numNegExamples, int numNeutExamples, int numPos, int numTruePos, int numFalseNeg, int numNeg, int numTrueNeg, int numFalsePos, int numPosRecFromNeut, int numNegRecFromNeut) {
        JTextArea area = new JTextArea(20, 20);
        area.setMargin(new Insets(0, 10, 0, 0));
        area.setEditable(false);
        StringBuffer sb = new StringBuffer(800);
        if(svmMode == SVMGUI.TRAIN_AND_CLASSIFY)
            sb.append("SVM Mode: Training and Classification\n\n");
        else
            sb.append("SVM Mode: Classification using SVM Weight File\n\n");
        
        if(classifyGenes){
            sb.append("Total Number of Genes: "+ (numPos + numNeg)+"\n\n");
            sb.append("Positive Genes\n");
            if(svmMode == SVMGUI.TRAIN_AND_CLASSIFY)
                sb.append("# of Genes initially selected as Positive examples: "+numPosExamples+"\n");
            
            sb.append("# of Genes classified as Positive (Total Positives): "+numPos+"\n");
            if(svmMode == SVMGUI.TRAIN_AND_CLASSIFY){
                sb.append("# of Genes retained in Positive class (True Positives): " + numTruePos+"\n");
                sb.append("# of Genes recruited into Positive class from Negatives (False Negatives): " + numFalseNeg+"\n");
                if(numNeutExamples > 0)
                    sb.append("# of Genes recruited into Positive class from Neutrals: " + numPosRecFromNeut+"\n");
            }
            sb.append("\n");
            
            sb.append("Negative Genes\n");
            if(svmMode == SVMGUI.TRAIN_AND_CLASSIFY)
                sb.append("# of Genes initially selected as negative examples: "+numNegExamples+"\n");
            
            sb.append("# of Genes classified as negative (Total Negatives): "+numNeg+"\n");
            if(svmMode == SVMGUI.TRAIN_AND_CLASSIFY){
                sb.append("# of Genes retained in negative class (True Negatives): " + numTrueNeg+"\n");
                sb.append("# of Genes recruited into negative class from Positives (False Positives): " + numFalsePos+"\n");
                if(numNeutExamples > 0)
                    sb.append("# of Genes recruited into Negative class from Neutrals: " + numNegRecFromNeut+"\n");
            }
            sb.append("\n");
            if(numNeutExamples != 0){
                sb.append("Neutral Genes");
                sb.append("\n");
                sb.append("# of Genes initially selected as neutral examples: "+numNeutExamples+"\n");
                sb.append("\n");
            }
            
        }
        else{
            sb.append("Total Number of Experiments: "+ (numPos + numNeg)+"\n\n");
            sb.append("Positive Experiments");
            sb.append("\n");
            if(svmMode == SVMGUI.TRAIN_AND_CLASSIFY)
                sb.append("# of Experiments initially selected as positive examples: "+numPosExamples+"\n");
            
            sb.append("# of Experiments classified as positive (Total Positives): "+numPos+"\n");
            if(svmMode == SVMGUI.TRAIN_AND_CLASSIFY){
                sb.append("# of Experiments retained in positive class (True Positives): " + numTruePos+"\n");
                sb.append("# of Experiments recruited into positive class from Negatives (False Negatives): " + numFalseNeg+"\n");
                if(numNeutExamples > 0)
                    sb.append("# of Experiments recruited into Positive class from Neutrals: " + numPosRecFromNeut+"\n");
            }
            sb.append("\n");
            
            sb.append("Negative Experiments");
            sb.append("\n");
            if(svmMode == SVMGUI.TRAIN_AND_CLASSIFY)
                sb.append("# of Experiments initially selected as negative examples: "+numNegExamples+"\n");
            
            sb.append("# of Experiments classified as negative (Total Negatives): "+numNeg+"\n");
            if(svmMode == SVMGUI.TRAIN_AND_CLASSIFY){
                sb.append("# of Experiments retained in negative class (True Negatives): " + numTrueNeg+"\n");
                sb.append("# of Experiments recruited into negative class from Positives (False Positives): " + numFalsePos+"\n");
                if(numNeutExamples > 0)
                    sb.append("# of Experiments recruited into Positive class from Neutrals: " + numNegRecFromNeut+"\n");
            }
            sb.append("\n");
            if(numNeutExamples != 0 && svmMode == SVMGUI.TRAIN_AND_CLASSIFY){
                sb.append("Neutral Experiments");
                sb.append("\n");
                sb.append("# of Experiments initially selected as neutral examples: "+numNeutExamples+"\n");
                sb.append("\n");
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
