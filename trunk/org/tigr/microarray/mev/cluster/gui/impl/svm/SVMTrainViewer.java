/*
Copyright @ 1999-2004, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: SVMTrainViewer.java,v $
 * $Revision: 1.4 $
 * $Date: 2004-07-27 19:59:17 $
 * $Author: braisted $
 * $State: Exp $
 */

package org.tigr.microarray.mev.cluster.gui.impl.svm;

import java.awt.Font;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.FontMetrics;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;


import java.io.File;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;

import java.text.DecimalFormat;

import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.BorderFactory;
import javax.swing.JOptionPane;

import org.tigr.util.FloatMatrix;

import org.tigr.microarray.mev.TMEV;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IViewer;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IDisplayMenu;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.helpers.ExpressionFileFilter;
import org.tigr.microarray.mev.cluster.gui.helpers.ExpressionFileView;

public class SVMTrainViewer extends SVMResultViewer implements java.io.Serializable {
    public static final long serialVersionUID = 202018080001L;

    private float[] weights;
    private IData experiment;
    private Experiment analysisExperiment;
    private SVMData data;
    private GeneralInfo info;
    private boolean classifyGenes;
    
    /**
     * Constructs a <code>SVMTrainViewer</code> with specified data.
     */
    public SVMTrainViewer(IFramework framework, IData experiment, SVMData data, float[] weights, GeneralInfo info, boolean classifyGenes) {
        super(framework);
        this.experiment = experiment;
        this.data = data;
        this.weights = weights;
        this.info = info;
        this.classifyGenes = classifyGenes;
        resultPanel = new TrainResultPanel();        
        displayData();
        this.add(resultPanel,new GridBagConstraints(0,1,1,1,1.0,1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));
    }
    
    private void readObject(java.io.ObjectInputStream ois) throws java.io.IOException, ClassNotFoundException {
        this.analysisExperiment = (Experiment)ois.readObject();
        this.weights = (float [])ois.readObject();
        this.classifyGenes = ois.readBoolean();
        this.data = (SVMData)ois.readObject();
        this.info = (GeneralInfo)ois.readObject();
        
        MyListener listener = new MyListener();
        getContentComponent().addMouseListener(listener);
    }
    
    private void writeObject(java.io.ObjectOutputStream oos) throws java.io.IOException { 
        oos.writeObject(this.analysisExperiment);
        oos.writeObject(this.weights);
        oos.writeBoolean(this.classifyGenes);
        oos.writeObject(this.data);
        oos.writeObject(this.info);
    }
    
    
    public void onSelected(IFramework frm) {
        this.framework = frm;
        this.experiment = frm.getData();
        onMenuChanged(frm.getDisplayMenu());
    }
    
    
    /**
     * Displays train result.
     */
    protected void displayData() {
        if (weights == null) {
            return;
        }
        displayResult(this.classifyGenes);
    }
    
    
    private void displayResult(boolean genes){
        StringBuffer buffer = new StringBuffer();
        String Dummy;
        DecimalFormat format = new DecimalFormat();
        format.setMaximumFractionDigits(4);
        format.setMinimumFractionDigits(4);
        format.setGroupingUsed(false);
        if(data.classificationFile != null)
            buffer.append("Classification file: "+data.classificationFile.getPath()+"\n");
        else
            buffer.append("Classification file: None (SVM classification editor was used)\n");
        buffer.append("Constant : "+Float.toString(data.constant)+"\n");
        buffer.append("Coefficient : "+Float.toString(data.coefficient)+"\n");
        buffer.append("Power : "+Float.toString(data.power)+"\n");
        buffer.append("Diagonal factor : "+Float.toString(data.diagonalFactor)+"\n");
        buffer.append("Convergence threshold : "+Float.toString(data.convergenceThreshold)+"\n");
        //buffer.append("Normalize : "+data.normalize+"\n");
        buffer.append("Radial : "+data.radial+"\n");
        buffer.append("Width factor : "+Float.toString(data.widthFactor)+"\n");
        buffer.append("Use Constraint : "+data.constrainWeights+"\n");
        buffer.append("Positive Constraint : "+Float.toString(data.positiveConstraint)+"\n");
        buffer.append("Negative Constraint : "+Float.toString(data.negativeConstraint)+"\n");
        buffer.append("Seed : "+Float.toString(data.seed)+"\n");
        buffer.append("Calculation time : "+Float.toString( info.time )+" ms\n");
        buffer.append("Objective : "+Float.toString(data.objective1)+"\n\n");
        
        if(genes){
            if(labelIndex >= 0 && labelIndex < fieldNames.length)
                buffer.append(" Weights       " + fieldNames[labelIndex]);
            else
                buffer.append(" Weights");
        }        
        else
            buffer.append(" Weights       Experiments");
        
        Log.setText( buffer.toString());
        Log.setCaretPosition(0);
      //  Log.setSize(resultPanel.getWidth()*2, 16*Log.getFontMetrics(Log.getFont()).getHeight());
    }
    
    /**
     * Saves train result to a file.
     */
    protected void onSaveResult() {
        File SVMFile;
        final JFileChooser fc = new JFileChooser(TMEV.getFile("data/"));
        fc.addChoosableFileFilter(new SVMFileFilter());
        fc.setFileView(new SVMFileView());
        int returnVal = fc.showSaveDialog(JOptionPane.getFrameForComponent(this));
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            SVMFile = fc.getSelectedFile();
        } else return;
        try {
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(SVMFile)));

            String logText = Log.getText();
            logText = logText.substring(0, logText.lastIndexOf("Weights"));
            out.write( logText  );
            if(classifyGenes){
                if(labelIndex >= 0 && labelIndex < fieldNames.length)
                    out.write("Weights\t"+fieldNames[labelIndex]+"\t\n");
                else
                    out.write("Weights\t\n");
            }
            else
                out.write("Weights\tExperiments\t\n");
            for(int i = 0; i < weights.length; i++){
                if(classifyGenes)
                    out.write(String.valueOf(weights[i])+"\t"+experiment.getElementAttribute(getMultipleArrayDataRow(i), labelIndex)+ "\t\n");
                else
                    out.write(String.valueOf(weights[i])+"\t"+experiment.getFullSampleName(i)+ "\t\n");
            }
            out.flush();
            out.close();
            out = null;
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog( this, "Error writing to file "+SVMFile.getPath()+"!","Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    
    protected Dimension updateSize(){
        return ((TrainResultPanel)resultPanel).updateSize();
    }
    
    /** Returns a component to be inserted into the scroll pane row header
     */
    public JComponent getRowHeaderComponent() {
        return null;
    }    
    
    /** Returns the corner component corresponding to the indicated corner,
     * posibly null
     */
    public JComponent getCornerComponent(int cornerIndex) {
        return null;  
    }
    
    public class TrainResultPanel extends JPanel{
        int lineHeight = 20;
        int indexLength = 1;
        DecimalFormat floatFormat;
        String spacerString;
        
        public TrainResultPanel(){
            floatFormat = new DecimalFormat();
            floatFormat.setMaximumFractionDigits(4);
            floatFormat.setMinimumFractionDigits(4);
            floatFormat.setGroupingUsed(false);
            
            setBackground(Color.white);
            Dimension d = updateSize();
            setSize(d.width, weights.length * lineHeight + 10);
           setPreferredSize(new Dimension(d.width, weights.length * lineHeight + 5));
        }
        
        public void paint(Graphics g){
            super.paint(g);
            
            g.setFont(new Font("monospaced", Font.PLAIN, 14));
            FontMetrics fm =g.getFontMetrics();
            //int lineHeight = fm.getHeight() + vertSpace;
            Rectangle rect = g.getClipBounds();
            int index;
            int top = getTopIndex(rect.y);
            int bottom = getBottomIndex(rect.y+rect.height, weights.length + 1);
            
            for(int i = top; i < bottom; i++){
                index = i;
                if(!isLegalIndex(index))
                    continue;
                spacerString = getSpacerString(floatFormat.format(weights[index]));
                if(classifyGenes){
                    g.drawString(" "+floatFormat.format(weights[index]) + spacerString + experiment.getElementAttribute(getMultipleArrayDataRow(index), labelIndex), 10, (i+1)*lineHeight);  //map i to real data using exp
                }
                else{
                    g.drawString(" "+floatFormat.format(weights[index]) + spacerString + experiment.getSampleName(index), 10, (i+1)*lineHeight);  //map i to real data using exp
                }
            }
        }
        
        private boolean isLegalIndex(int i){
            return (i >=0 && i < weights.length);
        }
        
        private int getTopIndex(int top) {
            if (top < 0) {
                return 0;
            }
            return top/lineHeight;
        }
        
        private int getBottomIndex(int bottom, int limit) {
            if (bottom < 0) {
                return 0;
            }
            int result = bottom/lineHeight+1;
            return result > limit ? limit : result;
        }
        
        protected String getSpacerString(String weight){
            
            String s = "";
            for(int i = 0; i < (15 - weight.length()); i++)
                s += " ";
            return s;
        }
        
        protected Dimension updateSize(){
            FontMetrics fm = this.getFontMetrics(new Font("monospaced", Font.PLAIN, 14));
            int len = 0;
            int numElem;
            indexLength = String.valueOf(weights.length).length();
            String s;
            // String [] spacerStrings;
            
            floatFormat = new DecimalFormat();
            floatFormat.setMaximumFractionDigits(4);
            floatFormat.setMinimumFractionDigits(4);
            floatFormat.setGroupingUsed(false);
            
            if(classifyGenes)
                numElem = experiment.getFeaturesSize();
            else
                numElem = experiment.getFeaturesCount();
            
            for(int i = 0; i < numElem; i++){
                spacerString = getSpacerString(floatFormat.format(weights[i]));
              
                if(classifyGenes){
                    s = (" "+floatFormat.format(weights[i]) + spacerString + experiment.getElementAttribute(getMultipleArrayDataRow(i), labelIndex));  //map i to real data using exp
                }
                else{
                    s =(" "+floatFormat.format(weights[i]) + spacerString + experiment.getSampleName(i));  //map i to real data using exp
                }
               
                len = Math.max(len, fm.stringWidth(s));
            }
            setSize(len + 10, getHeight());
            setPreferredSize(new Dimension(len+10, getHeight()));
            
            return new Dimension(len+10, getHeight());
        }
     
        
    }
}

