/*
Copyright @ 1999-2004, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * $RCSfile: SVMOneOutViewer.java,v $
 * $Revision: 1.5 $
 * $Date: 2005-02-24 20:23:45 $
 * $Author: braistedj $
 * $State: Exp $
 */

package org.tigr.microarray.mev.cluster.gui.impl.svm;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Rectangle;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.tigr.microarray.mev.TMEV;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.helpers.ExpressionFileFilter;
import org.tigr.microarray.mev.cluster.gui.helpers.ExpressionFileView;
import org.tigr.util.FloatMatrix;

public class SVMOneOutViewer extends SVMResultViewer implements java.io.Serializable {
    public static final long serialVersionUID = 202018060001L;
    
    // calculation results
    private IData  experiment;
    private FloatMatrix discriminant;
    private SVMData data;
    private GeneralInfo info;
    private boolean classifyGenes;
    float [] classes;
    float [] discr;
    int [] initClasses;
    float [] classMatch;
    
    int [] elementScores;
    int [] iterationScores;
    int nonNeuts;
    
    /**
     * Constructs a <code>SVMClassifyViewer</code> with specified data.
     */
    public SVMOneOutViewer(IFramework framework, IData experiment, SVMData data, FloatMatrix discriminant, GeneralInfo info, boolean classifyGenes, int [] initClasses, int [] elementScores, int [] iterationScores, int nonNeuts ) {
        super(framework);
        this.experiment   = experiment;
        this.discriminant = discriminant;
        this.data = data;
        this.info = info;
        this.classifyGenes = classifyGenes;
        this.initClasses = initClasses;
        this.elementScores = elementScores;
        this.iterationScores = iterationScores;
        this.nonNeuts = nonNeuts;
        
        FloatMatrix M = discriminant.transpose();
        classes = M.A[0];
        discr = M.A[1];
        resultPanel = new ClassifyResultPanel();
        displayData();
        this.add(resultPanel,new GridBagConstraints(0,1,1,1,1.0,1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));
    }
    

    
    private void readObject(java.io.ObjectInputStream ois) throws java.io.IOException, ClassNotFoundException {
        
        this.discr = (float [])ois.readObject();
        this.classes = (float [])ois.readObject();
        
        this.initClasses = (int [])ois.readObject();
        this.classMatch = (float [])ois.readObject();
        this.elementScores = (int [])ois.readObject();
        this.iterationScores = (int [])ois.readObject();
        this.nonNeuts = ois.readInt();
        
        this.classifyGenes = ois.readBoolean();
        this.data = (SVMData)ois.readObject();
        this.info = (GeneralInfo)ois.readObject();
        
        MyListener listener = new MyListener();
        getContentComponent().addMouseListener(listener);
    }
    
    private void writeObject(java.io.ObjectOutputStream oos) throws java.io.IOException { 
        oos.writeObject(this.discr);
        oos.writeObject(this.classes);
        
        oos.writeObject(this.initClasses);
        oos.writeObject(this.classMatch);
        oos.writeObject(this.elementScores);
        oos.writeObject(this.iterationScores);
        oos.writeInt(this.nonNeuts);
        
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
     * Displays data.
     */
    protected void displayData() {
        displayResult(this.classifyGenes);
        //  this.resultPanel.repaint();
    }
    
    private void displayResult(boolean genes){
        String Dummy;
        StringBuffer buffer = new StringBuffer();
        DecimalFormat format = new DecimalFormat();
        DecimalFormat format2 = new DecimalFormat();
        format.setMaximumFractionDigits(4);
        format.setMinimumFractionDigits(4);
        format.setGroupingUsed(false);
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
        buffer.append("Calculation time : "+Float.toString(info.time)+" ms\n");
        buffer.append("Objective : "+Float.toString(data.objective1)+"\n\n");
        buffer.append(        "                                  Element           Iteration\n");
        if(genes){
            if(labelIndex >= 0 && labelIndex < fieldNames.length)
                buffer.append(" Index  Init. Class.  Discr.     Score      %      Score     %    "+fieldNames[labelIndex]);
            else
                buffer.append(" Index  Init. Class.  Discr.     Score      %      Score     %");
        }
        else
            buffer.append(" Index  Init. Class.  Discr.     Score     %      Score     %       Experiment");
        Log.setText( buffer.toString());
        Log.setCaretPosition(0);
        //     Log.setSize(resultPanel.getWidth()*2, 15*Log.getFontMetrics(Log.getFont()).getHeight());
    }
    
    /**
     * Saves train result to a file.
     */
    protected void onSaveResult() {
        File SVMFile;
        final JFileChooser fc = new JFileChooser(TMEV.getFile("data/"));
        fc.addChoosableFileFilter(new ExpressionFileFilter());
        fc.setFileView(new ExpressionFileView());
        int returnVal = fc.showSaveDialog(JOptionPane.getFrameForComponent(this));
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            SVMFile = fc.getSelectedFile();
        } else return;
        try {
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(SVMFile)));
            int discrLength = discriminant.getRowDimension();
            String logText = Log.getText();
            logText = logText.substring(0, logText.lastIndexOf("Element"));
            out.write( logText  );
            if(classifyGenes){
                if(labelIndex >= 0 && labelIndex < fieldNames.length)
                    out.write("Index\tInitClass\tClass.\tDiscr.\tElementScore\tElement %\tIterationScore\tIteration %\t"+fieldNames[labelIndex]+"\t\n");
                else
                    out.write("Index\tInitClass\tClass.\tDiscr.\tElementScore\tElement %\tIterationScore\tIteration %\t\n");
            }
            else
                out.write("Index\tInitClass\tClass.\tDiscr.\tElementScore\tElement %\tIterationScore\tIteration %\tExperiment\t\n");
            for(int i = 0; i < discrLength; i++){
                out.write(((ClassifyResultPanel)resultPanel).getTabbedLine(i)+"\n");
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
        return ((ClassifyResultPanel)resultPanel).updateSize();
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
    
    
    public class ClassifyResultPanel extends JPanel{
        int lineHeight = 20;
        
        int indexLength = 1;
        DecimalFormat floatFormat;
        DecimalFormat intFormat;
        DecimalFormat indexFormat;
        DecimalFormat singleFloatFormat;
        String [] spacerStrings;
        
        public ClassifyResultPanel(){
            
            floatFormat = new DecimalFormat();
            floatFormat.setMaximumFractionDigits(4);
            floatFormat.setMinimumFractionDigits(4);
            floatFormat.setPositivePrefix("+");
            floatFormat.setGroupingUsed(false);
            
            singleFloatFormat = new DecimalFormat();
            singleFloatFormat.setMaximumFractionDigits(1);
            singleFloatFormat.setMinimumFractionDigits(1);
            singleFloatFormat.setGroupingUsed(false);
            
            intFormat = new DecimalFormat();
            intFormat.setMinimumFractionDigits(0);
            intFormat.setMaximumFractionDigits(0);
            intFormat.setPositivePrefix("+");
            intFormat.setGroupingUsed(false);
            
            indexFormat = new DecimalFormat();
            indexFormat = new DecimalFormat();
            indexFormat.setMinimumFractionDigits(0);
            indexFormat.setMaximumFractionDigits(0);
            indexLength = String.valueOf(classes.length).length();
            indexFormat.setMinimumIntegerDigits(indexLength);
            indexFormat.setGroupingUsed(false);
            
            setBackground(Color.white);
            Dimension d = updateSize();
            setSize(d.width, classes.length * lineHeight + 10);
            setPreferredSize(new Dimension(d.width, classes.length * lineHeight + 5));
        }
        
        public void paint(Graphics g){
            super.paint(g);
            
            g.setFont(new Font("monospaced", Font.PLAIN, 14));
            FontMetrics fm =g.getFontMetrics();
            //int lineHeight = fm.getHeight() + vertSpace;
            Rectangle rect = g.getClipBounds();
            String [] spacerStrings;
            int index;
            
            int top = getTopIndex(rect.y);
            int bottom = getBottomIndex(rect.y+rect.height, classes.length + 1);
            
            for(int i = top; i < bottom; i++){
                if(!isLegalIndex(i))
                    continue;
                g.drawString(getLine(i), 10, (i+1)*lineHeight);
            }
        }
        
        private boolean isLegalIndex(int i){
            return (i >=0 && i < classes.length);
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
        
        private String getLine(int index){
            String str = " ";
            
            int indexLength = indexFormat.format(index+1).length();
            int discrLength = floatFormat.format(discr[index]).length();
            int elementScoreLength = (String.valueOf(elementScores[index])+"/"+String.valueOf(iterationScores.length)).length();
            int elementPercLength = singleFloatFormat.format((elementScores[index]/((double)iterationScores.length))*100.0).length();
            int iterScoreLength = (String.valueOf(iterationScores[index])+"/"+String.valueOf(nonNeuts)).length();
            int iterPercLength = singleFloatFormat.format((iterationScores[index]/((double)nonNeuts))*100.0).length();
            
            
            str += indexFormat.format(index+1);
            
            for(int i = indexLength; i < 8; i++)
                str += " ";
            if(initClasses[index] >= 0)
                str += " ";
            str += intFormat.format(initClasses[index]);
            str += "    ";
            if(classes[index] >= 0)
                str += " ";
            
            str += intFormat.format(classes[index]);
            str += "    ";
            if(discr[index] >= 0)
                str += " ";
            
            str += floatFormat.format(discr[index]);
            // for(int i = discrLength; i < 12; i++)
            //     str += " ";
            for(int i = 0; i < 9 - elementScoreLength; i++)
                str += " ";
            str += String.valueOf(elementScores[index])+"/"+String.valueOf(iterationScores.length);
            for(int i = 0; i < 8 - elementPercLength; i++)
                str += " ";
            str += singleFloatFormat.format((elementScores[index]/((double)iterationScores.length))*100.0);
            
            
            for(int i = 0; i < 9 - iterScoreLength; i++)
                str += " ";
            str += String.valueOf(iterationScores[index])+"/"+String.valueOf(nonNeuts);
            for(int i = 0; i < 8 - iterPercLength; i++)
                str += " ";
            str += singleFloatFormat.format((iterationScores[index]/((double)nonNeuts))*100.0);
            str += "      ";
            
            if(classifyGenes){
                str += experiment.getElementAttribute(getMultipleArrayDataRow(index), labelIndex);  //map i to real data using exp
            } else {
                str += experiment.getSampleName(index);  //map i to real data using exp
            }
            
            return str;
        }
        
        
        
        private String getTabbedLine(int index){
            String str = new String();
            str += indexFormat.format(index+1);
            str += "\t ";
            str += intFormat.format(initClasses[index]);
            str += "\t";
            str += intFormat.format(classes[index]);
            str += "\t";
            str += floatFormat.format(discr[index]);
            str += "\t";
            str += "( "+String.valueOf(elementScores[index])+" / "+String.valueOf(iterationScores.length)+" )";
            str += "\t";
            str += singleFloatFormat.format((elementScores[index]/((double)iterationScores.length))*100.0);
            str += "\t";
            str += "( "+String.valueOf(iterationScores[index])+" / "+String.valueOf(nonNeuts)+" )";
            str += "\t";
            str += singleFloatFormat.format((iterationScores[index]/((double)nonNeuts))*100.0);
            str += "\t";
            if(classifyGenes){
                str += experiment.getElementAttribute(getMultipleArrayDataRow(index), labelIndex);  //map i to real data using exp
            } else {
                str += experiment.getSampleName(index);  //map i to real data using exp
            }
            return str;
        }
        
        
        protected String [] getSpacerStrings(int indexLength, String clas, String discr){
            
            String [] s = new String[3];
            int s2_length;
            s[0] = s[1] = s[2] = "";
            for(int i = indexLength; i < 10 ; i++){
                s[0] += " ";
            }
            if(clas.length() == 1)
                s[0] += " ";
            s[1] = "     ";
            s2_length = 13 - discr.length();
            for(int i = 0; i < s2_length;i++){
                s[2] += " ";
            }
            return s;
        }
        
        protected Dimension updateSize(){
            FontMetrics fm = this.getFontMetrics(new Font("monospaced", Font.PLAIN, 14));
            int len = 0;
            int numElem;
            indexLength = String.valueOf(classes.length).length();
            String s;
            // String [] spacerStrings;
            
            floatFormat = new DecimalFormat();
            floatFormat.setMaximumFractionDigits(4);
            floatFormat.setMinimumFractionDigits(4);
            floatFormat.setGroupingUsed(false);
            
            intFormat = new DecimalFormat();
            intFormat.setMinimumFractionDigits(0);
            intFormat.setMaximumFractionDigits(0);
            intFormat.setGroupingUsed(false);
            
            indexFormat = new DecimalFormat();
            indexFormat = new DecimalFormat();
            indexFormat.setMinimumFractionDigits(0);
            indexFormat.setMaximumFractionDigits(0);
            indexFormat.setMinimumIntegerDigits(indexLength);
            indexFormat.setGroupingUsed(false);
            
            if(classifyGenes)
                numElem = experiment.getFeaturesSize();
            else
                numElem = experiment.getFeaturesCount();
            
            for(int i = 0; i < numElem; i++){
                s = getLine(i);
                len = Math.max(len, fm.stringWidth(s));
            }
            
            setSize(len + 10, getHeight());
            setPreferredSize(new Dimension(len+10, getHeight()));
            
            return new Dimension(len+10, getHeight());
        }
        
    }
    
}

